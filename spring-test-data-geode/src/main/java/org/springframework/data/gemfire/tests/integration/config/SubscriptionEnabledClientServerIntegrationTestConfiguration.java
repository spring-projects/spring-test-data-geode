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

import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.client.internal.PoolImpl;
import org.apache.geode.management.membership.ClientMembership;
import org.apache.geode.management.membership.ClientMembershipEvent;
import org.apache.geode.management.membership.ClientMembershipListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport;
import org.springframework.util.Assert;

/**
 * The {@link SubscriptionEnabledClientServerIntegrationTestConfiguration} class is a base Spring {@link Configuration}
 * class supporting Apache Geode or Pivotal GemFire client/server integration tests when subscriptions are enabled.
 *
 * Subscriptions must be enabled when {@literal Registering Interests} or {@literal Continuous Queries (CQ)}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.apache.geode.management.membership.ClientMembership
 * @see org.apache.geode.management.membership.ClientMembershipListenerAdapter
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class SubscriptionEnabledClientServerIntegrationTestConfiguration {

	private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(60);

	private static final CountDownLatch LATCH = new CountDownLatch(1);

	private static final String GEMFIRE_CACHE_SERVER_PORT_PROPERTY =
		ClientServerIntegrationTestsSupport.GEMFIRE_CACHE_SERVER_PORT_PROPERTY;

	private static final String GEMFIRE_DEFAULT_POOL_NAME = "DEFAULT";

	private static final String LOCALHOST = ClientServerIntegrationTestsSupport.DEFAULT_HOSTNAME;

	@Bean
	BeanPostProcessor clientServerReadyBeanPostProcessor(
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":40404}") int port) {

		return new BeanPostProcessor() {

			private final AtomicBoolean checkGemFireServerIsRunning = new AtomicBoolean(true);

			private final AtomicReference<Pool> defaultPool = new AtomicReference<>(null);

			@SuppressWarnings("all")
			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

				if (shouldCheckWhetherGemFireServerIsRunning(bean, beanName)) {
					try {
						validateClientCacheNotified();
						validateClientCacheSubscriptionQueueConnectionEstablished();
					}
					catch (InterruptedException cause) {
						Thread.currentThread().interrupt();
					}
				}

				return bean;
			}

			private boolean shouldCheckWhetherGemFireServerIsRunning(Object bean, String beanName) {

				return isGemFireRegion(bean, beanName)
					? checkGemFireServerIsRunning.compareAndSet(true, false)
					: whenGemFireCache(bean, beanName);
			}

			private boolean isGemFireRegion(Object bean, String beanName) {
				return bean instanceof Region;
			}

			private boolean whenGemFireCache(Object bean, String beanName) {

				if (bean instanceof ClientCache) {
					defaultPool.compareAndSet(null, ((ClientCache) bean).getDefaultPool());
				}

				return false;
			}

			private void validateClientCacheNotified() throws InterruptedException {

				boolean didNotTimeout = LATCH.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

				Assert.state(didNotTimeout, String.format(
					"Apache Geode CacheServer failed to start on host [%s] and port [%d]", LOCALHOST, port));
			}

			@SuppressWarnings("all")
			private void validateClientCacheSubscriptionQueueConnectionEstablished() throws InterruptedException {

				boolean clientCacheSubscriptionQueueConnectionEstablished = false;

				Pool pool = resolvePool(this.defaultPool.get(),
					GemfireConstants.DEFAULT_GEMFIRE_POOL_NAME, GEMFIRE_DEFAULT_POOL_NAME);

				if (pool instanceof PoolImpl) {

					long timeout = System.currentTimeMillis() + DEFAULT_TIMEOUT;

					while (System.currentTimeMillis() < timeout && !((PoolImpl) pool).isPrimaryUpdaterAlive()) {
						synchronized (pool) {
							TimeUnit.MILLISECONDS.timedWait(pool, 500L);
						}

					}

					clientCacheSubscriptionQueueConnectionEstablished |= ((PoolImpl) pool).isPrimaryUpdaterAlive();
				}

				Assert.state(clientCacheSubscriptionQueueConnectionEstablished,
					String.format("ClientCache subscription queue connection not established;"
							+ " Apache Geode Pool was [%s];"
							+ " Apache Geode Pool configuration was [locators = %s, servers = %s]",
						pool, pool.getLocators(), pool.getServers()));
			}

			private Pool resolvePool(Pool pool, String... poolNames) {

				return Optional.ofNullable(pool)
					.orElseGet(() -> Arrays.stream(nullSafeArray(poolNames, String.class))
						.map(PoolManager::find)
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null));
			}
		};
	}

	@Bean
	ClientCacheConfigurer registerClientMembershipListener() {

		return (beanName, bean) ->

			ClientMembership.registerClientMembershipListener(new ClientMembershipListenerAdapter() {

				@Override
				public void memberJoined(ClientMembershipEvent event) {
					LATCH.countDown();
				}
			});
	}
}
