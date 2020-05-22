/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.springframework.data.gemfire.tests.mock.beans.factory.config;

import static org.mockito.Mockito.doReturn;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.distributed.DistributedSystem;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A Spring {@link BeanPostProcessor} implementation that applies mocks and spies to
 * Spring Data GemFire / Spring Data Geode (SDG) and Apache Geode / VMware GemFire
 * {@link Object objects}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCacheFactory
 * @see org.apache.geode.cache.client.PoolFactory
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @since 0.0.1
 */
public class GemFireMockObjectsBeanPostProcessor implements BeanPostProcessor {

	protected static final boolean DEFAULT_USE_SINGLETON_CACHE = false;

	protected static final String GEMFIRE_PROPERTIES_BEAN_NAME = "gemfireProperties";

	private volatile boolean useSingletonCache;

	private final AtomicReference<Properties> gemfireProperties = new AtomicReference<>(new Properties());

	public static GemFireMockObjectsBeanPostProcessor newInstance() {
		return newInstance(DEFAULT_USE_SINGLETON_CACHE);
	}

	public static GemFireMockObjectsBeanPostProcessor newInstance(boolean useSingletonCache) {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = new GemFireMockObjectsBeanPostProcessor();

		beanPostProcessor.useSingletonCache = useSingletonCache;

		return beanPostProcessor;
	}

	@Nullable @Override
	public Object postProcessBeforeInitialization(@NonNull Object bean, String beanName) throws BeansException {

		return isGemFireProperties(bean, beanName) ? set((Properties) bean)
			: bean instanceof CacheFactoryBean ? spyOnCacheFactoryBean((CacheFactoryBean) bean, isUsingSingletonCache())
			: bean instanceof PoolFactoryBean ? mockThePoolFactoryBean((PoolFactoryBean) bean)
			: bean;
	}

	@Nullable @Override
	public Object postProcessAfterInitialization(@NonNull Object bean, String beanName) throws BeansException {

		if (bean instanceof GemFireCache) {

			GemFireCache gemfireCache = (GemFireCache) bean;

			DistributedSystem distributedSystem = gemfireCache.getDistributedSystem();

			doReturn(getGemFireProperties()).when(distributedSystem).getProperties();
		}

		return bean;
	}

	private boolean isGemFireProperties(Object bean, String beanName) {
		return bean instanceof Properties && GEMFIRE_PROPERTIES_BEAN_NAME.equals(beanName);
	}

	protected boolean isUsingSingletonCache() {
		return this.useSingletonCache;
	}

	private Object set(Properties gemfireProperties) {

		this.gemfireProperties.set(gemfireProperties);

		return gemfireProperties;
	}

	protected Properties getGemFireProperties() {
		return this.gemfireProperties.get();
	}

	private Object spyOnCacheFactoryBean(CacheFactoryBean bean, boolean useSingletonCache) {

		return bean instanceof ClientCacheFactoryBean
			? SpyingClientCacheFactoryInitializer.spyOn((ClientCacheFactoryBean) bean, useSingletonCache)
			: SpyingCacheFactoryInitializer.spyOn(bean, useSingletonCache);
	}

	private Object mockThePoolFactoryBean(PoolFactoryBean bean) {
		return MockingPoolFactoryInitializer.mock(bean);
	}

	protected static class SpyingCacheFactoryInitializer
			implements CacheFactoryBean.CacheFactoryInitializer<CacheFactory> {

		protected static CacheFactoryBean spyOn(CacheFactoryBean cacheFactoryBean, boolean useSingletonCache) {

			cacheFactoryBean.setCacheFactoryInitializer(new SpyingCacheFactoryInitializer(useSingletonCache));

			return cacheFactoryBean;
		}

		private final boolean useSingletonCache;

		protected SpyingCacheFactoryInitializer(boolean useSingletonCache) {
			this.useSingletonCache = useSingletonCache;
		}

		protected boolean isUsingSingletonCache() {
			return this.useSingletonCache;
		}

		@Override
		public CacheFactory initialize(CacheFactory cacheFactory) {
			return GemFireMockObjectsSupport.spyOn(cacheFactory, isUsingSingletonCache());
		}
	}

	protected static class SpyingClientCacheFactoryInitializer
			implements CacheFactoryBean.CacheFactoryInitializer<ClientCacheFactory> {

		protected static ClientCacheFactoryBean spyOn(ClientCacheFactoryBean clientCacheFactoryBean,
				boolean useSingletonCache) {

			clientCacheFactoryBean
				.setCacheFactoryInitializer(new SpyingClientCacheFactoryInitializer(useSingletonCache));

			return clientCacheFactoryBean;
		}

		private final boolean useSingletonCache;

		protected SpyingClientCacheFactoryInitializer(boolean useSingletonCache) {
			this.useSingletonCache = useSingletonCache;
		}

		protected boolean isUsingSingletonCache() {
			return this.useSingletonCache;
		}

		@Override
		public ClientCacheFactory initialize(ClientCacheFactory clientCacheFactory) {
			return GemFireMockObjectsSupport.spyOn(clientCacheFactory, isUsingSingletonCache());
		}
	}

	protected static class MockingPoolFactoryInitializer implements PoolFactoryBean.PoolFactoryInitializer {

		protected static PoolFactoryBean mock(PoolFactoryBean poolFactoryBean) {

			poolFactoryBean.setPoolFactoryInitializer(new MockingPoolFactoryInitializer());

			return poolFactoryBean;
		}

		@Override
		public PoolFactory initialize(PoolFactory poolFactory) {
			return GemFireMockObjectsSupport.mockPoolFactory();
		}
	}
}
