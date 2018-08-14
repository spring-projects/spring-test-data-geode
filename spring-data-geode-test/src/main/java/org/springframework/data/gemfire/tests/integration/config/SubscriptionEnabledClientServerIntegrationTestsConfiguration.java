/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package org.springframework.data.gemfire.tests.integration.config;

import static org.springframework.data.gemfire.util.CollectionUtils.nullSafeMap;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.client.internal.PoolImpl;
import org.apache.geode.management.membership.ClientMembership;
import org.apache.geode.management.membership.ClientMembershipEvent;
import org.apache.geode.management.membership.ClientMembershipListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionShortcutWrapper;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;
import org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport;
import org.springframework.data.gemfire.tests.util.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * The {@link SubscriptionEnabledClientServerIntegrationTestsConfiguration} class is a base Spring {@link Configuration}
 * class supporting Apache Geode or Pivotal GemFire client/server integration tests when subscriptions are enabled.
 *
 * Subscriptions must be enabled when {@literal Registering Interests} or {@literal Continuous Queries (CQ)}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.apache.geode.cache.client.internal.PoolImpl
 * @see org.apache.geode.management.membership.ClientMembership
 * @see org.apache.geode.management.membership.ClientMembershipListenerAdapter
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class SubscriptionEnabledClientServerIntegrationTestsConfiguration
		extends ClientServerIntegrationTestsConfiguration {

	private static final boolean DEFAULT_SUBSCRIPTION_QUEUE_CONNECTION_FAILURE = true;

	private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

	private static final CountDownLatch LATCH = new CountDownLatch(1);

	private static final String GEMFIRE_CACHE_SERVER_PORT_PROPERTY =
		ClientServerIntegrationTestsSupport.GEMFIRE_CACHE_SERVER_PORT_PROPERTY;

	private static final String SPRING_DATA_GEODE_POOL_NAME = GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME;
	private static final String GEMFIRE_DEFAULT_POOL_NAME = "DEFAULT";

	private static final String LOCALHOST = ClientServerIntegrationTestsSupport.DEFAULT_HOSTNAME;

	protected Long getSocketConnectTimeout() {
		return resolveTimeout() / 2;
	}

	protected Long getTimeout() {
		return DEFAULT_TIMEOUT;
	}

	protected boolean isThrowExceptionOnSubscriptionQueueConnectionFailure() {
		return DEFAULT_SUBSCRIPTION_QUEUE_CONNECTION_FAILURE;
	}

	private long resolveSocketConnectTimeout() {

		Long socketConnectTimeout = getSocketConnectTimeout();

		long resolvedTimeout = resolveTimeout();
		long resolvedSocketConnectTimeout = socketConnectTimeout != null ? socketConnectTimeout : resolvedTimeout;

		return Math.min(Math.max(resolvedSocketConnectTimeout, 0), resolvedTimeout / 2);
	}

	private long resolveTimeout() {

		Long timeout = getTimeout();

		return Math.max(timeout != null ? timeout : DEFAULT_TIMEOUT, 0);
	}

	@Bean
	BeanPostProcessor clientCachePoolSocketConnectTimeoutBeanPostProcessor() {

		return new BeanPostProcessor() {

			@Nullable @Override @SuppressWarnings("all")
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (bean instanceof ClientCacheFactoryBean) {
					((ClientCacheFactoryBean) bean).setSocketConnectTimeout(
						Long.valueOf(resolveSocketConnectTimeout()).intValue());
				}
				else if (bean instanceof PoolFactoryBean) {
					((PoolFactoryBean) bean).setSocketConnectTimeout(
						Long.valueOf(resolveSocketConnectTimeout()).intValue());
				}

				return bean;
			}
		};
	}

	@Bean
	BeanPostProcessor clientServerReadyBeanPostProcessor(ListableBeanFactory beanFactory,
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":40404}") int port) {

		return new BeanPostProcessor() {

			private final AtomicBoolean verifyGemFireServerIsRunning = new AtomicBoolean(true);

			@Nullable @Override @SuppressWarnings("all")
			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

				if (isGemFireServerRunningVerificationEnabled(bean, beanName)) {
					try {
						verifyClientCacheMemberJoined();
						verifyClientCacheSubscriptionQueueConnectionsEstablished();
					}
					catch (InterruptedException cause) {
						Thread.currentThread().interrupt();
					}
				}

				return bean;
			}

			private boolean isGemFireServerRunningVerificationEnabled(Object bean, String beanName) {

				return isVeryImportantBean(bean, beanName)
					&& verifyGemFireServerIsRunning.compareAndSet(true, false);
			}

			private boolean isVeryImportantBean(Object bean, String beanName) {
				return isContinuousQueryListenerContainer(bean) || isClientProxyRegion(bean);
			}

			private boolean isContinuousQueryListenerContainer(Object bean) {
				return bean instanceof ContinuousQueryListenerContainer;
			}

			private boolean isClientProxyRegion(Object bean) {

				if (bean instanceof ClientRegionFactoryBean) {

					ClientRegionFactoryBean<?, ?> clientRegionFactoryBean = (ClientRegionFactoryBean) bean;

					return clientRegionFactoryBean.getPoolName()
						.filter(StringUtils::hasText)
						.map(it -> true)
						.orElseGet(() -> resolveClientRegionShortcut(clientRegionFactoryBean)
							.map(ClientRegionShortcutWrapper::valueOf)
							.filter(ClientRegionShortcutWrapper::isProxy)
							.isPresent());
				}

				return false;
			}

			@SuppressWarnings("unchecked")
			private Optional<ClientRegionShortcut> resolveClientRegionShortcut(
					ClientRegionFactoryBean<?, ?> clientRegionFactoryBean) {

				try {

					Method resolveClientRegionShortcut = ClientRegionFactoryBean.class
						.getDeclaredMethod("resolveClientRegionShortcut");

					resolveClientRegionShortcut.setAccessible(true);

					return Optional.ofNullable((ClientRegionShortcut)
						ReflectionUtils.invokeMethod(resolveClientRegionShortcut, clientRegionFactoryBean));
				}
				catch (Throwable ignore) {
					return Optional.empty();
				}
			}

			@SuppressWarnings("all")
			private void verifyClientCacheMemberJoined() throws InterruptedException {

				String errorMessage =
					String.format("CacheServer failed to start on host [%s] and port [%d]", LOCALHOST, port);

				Assert.state(LATCH.await(resolveTimeout(), TimeUnit.MILLISECONDS), errorMessage);
			}

			@SuppressWarnings("all")
			private void verifyClientCacheSubscriptionQueueConnectionsEstablished() {

				resolvePools().stream()
					.filter(pool -> pool.getSubscriptionEnabled())
					.filter(pool -> pool instanceof PoolImpl)
					.map(pool -> (PoolImpl) pool)
					.forEach(pool -> {

						long timeout = System.currentTimeMillis() + resolveTimeout();

						while (System.currentTimeMillis() < timeout && !pool.isPrimaryUpdaterAlive()) {
							synchronized (pool) {
								ObjectUtils.doOperationSafely(() -> {
									TimeUnit.MILLISECONDS.timedWait(pool, 500L);
									return null;
								});
							}
						}

						String errorMessage = String.format("ClientCache subscription queue connection not established;"
								+ " Pool [%s] has configuration [locators = %s, servers = %s]",
							pool, pool.getLocators(), pool.getServers());

						if (isThrowExceptionOnSubscriptionQueueConnectionFailure()) {
							Assert.state(pool.isPrimaryUpdaterAlive(), errorMessage);
						}
						else if (getLogger().isWarnEnabled()){
							getLogger().warn(errorMessage);
						}
					});
			}

			// TODO: PoolManager.getAll() will not include the "DEFAULT" Pool
			private Collection<Pool> resolvePools() {

				eagerlyInitializeSpringManagedPoolBeans();

				return nullSafeMap(PoolManager.getAll()).values();
			}

			private void eagerlyInitializeSpringManagedPoolBeans() {

				beanFactory.getBeansOfType(PoolFactoryBean.class).keySet()
					.forEach(beanName -> beanFactory.getBean(beanName, Pool.class));
			}
		};
	}

	@PostConstruct
	public void registerClientMembershipListener() {

		ClientMembership.registerClientMembershipListener(new ClientMembershipListenerAdapter() {

			@Override
			public void memberJoined(ClientMembershipEvent event) {
				LATCH.countDown();
			}
		});
	}
}
