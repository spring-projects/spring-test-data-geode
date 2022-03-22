/*
 *  Copyright 2017-present the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Properties;

import org.junit.Test;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.distributed.DistributedSystem;

import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.util.PropertiesBuilder;

/**
 * Unit Tests for {@link GemFireMockObjectsBeanPostProcessor}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.distributed.DistributedSystem
 * @see org.springframework.data.gemfire.CacheFactoryBean
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.springframework.data.gemfire.client.PoolFactoryBean
 * @see org.springframework.data.gemfire.tests.mock.beans.factory.config.GemFireMockObjectsBeanPostProcessor
 * @see org.springframework.data.gemfire.util.PropertiesBuilder
 * @since 0.0.16
 */
public class GemFireMockObjectsBeanPostProcessorUnitTests {

	@Test
	public void postProcessBeforeInitializationWithGemFireProperties() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = GemFireMockObjectsBeanPostProcessor.newInstance();

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.isUsingSingletonCache())
			.isEqualTo(GemFireMockObjectsBeanPostProcessor.DEFAULT_USE_SINGLETON_CACHE);

		Properties gemfireProperties = new PropertiesBuilder()
			.setProperty("name", "postProcessBeforeInitializationWithGemFirePropertiesTest")
			.setProperty("log-level", "error")
			.build();

		assertThat(beanPostProcessor.getGemFireProperties()).isNotSameAs(gemfireProperties);

		assertThat(beanPostProcessor.postProcessBeforeInitialization(gemfireProperties,
			GemFireMockObjectsBeanPostProcessor.GEMFIRE_PROPERTIES_BEAN_NAME)).isEqualTo(gemfireProperties);

		assertThat(beanPostProcessor.getGemFireProperties()).isSameAs(gemfireProperties);
	}

	@Test
	public void postProcessBeforeInitializationWithNonGemFireProperties() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = GemFireMockObjectsBeanPostProcessor.newInstance();

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.isUsingSingletonCache())
			.isEqualTo(GemFireMockObjectsBeanPostProcessor.DEFAULT_USE_SINGLETON_CACHE);

		Properties applicationProperties = new PropertiesBuilder()
			.setProperty("spring.application.name", "postProcessBeforeInitializationWithNonGemFirePropertiesTest")
			.setProperty("log-level", "error")
			.build();

		assertThat(beanPostProcessor.getGemFireProperties()).isNotSameAs(applicationProperties);

		assertThat(beanPostProcessor.postProcessBeforeInitialization(applicationProperties,
			"applicationProperties")).isEqualTo(applicationProperties);

		assertThat(beanPostProcessor.getGemFireProperties()).isNotSameAs(applicationProperties);
	}

	@Test
	public void postProcessBeforeInitializationWithCacheFactoryBean() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = GemFireMockObjectsBeanPostProcessor.newInstance(true);

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.isUsingSingletonCache()).isTrue();

		CacheFactoryBean cacheFactoryBean = spy(new CacheFactoryBean());

		assertThat(beanPostProcessor.postProcessBeforeInitialization(cacheFactoryBean, "peerCache"))
			.isEqualTo(cacheFactoryBean);

		verify(cacheFactoryBean, times(1))
			.setCacheFactoryInitializer(isA(GemFireMockObjectsBeanPostProcessor.SpyingCacheFactoryInitializer.class));
	}

	@Test
	public void postProcessBeforeInitializationWithClientCacheFactoryBean() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = GemFireMockObjectsBeanPostProcessor.newInstance(true);

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.isUsingSingletonCache()).isTrue();

		ClientCacheFactoryBean clientCacheFactoryBean = spy(new ClientCacheFactoryBean());

		assertThat(beanPostProcessor.postProcessBeforeInitialization(clientCacheFactoryBean, "clientCache"))
			.isEqualTo(clientCacheFactoryBean);

		verify(clientCacheFactoryBean, times(1))
			.setCacheFactoryInitializer(isA(GemFireMockObjectsBeanPostProcessor.SpyingClientCacheFactoryInitializer.class));
	}

	@Test
	public void postProcessBeforeInitializationWithPoolFactoryBean() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = GemFireMockObjectsBeanPostProcessor.newInstance();

		assertThat(beanPostProcessor).isNotNull();
		assertThat(beanPostProcessor.isUsingSingletonCache())
			.isEqualTo(GemFireMockObjectsBeanPostProcessor.DEFAULT_USE_SINGLETON_CACHE);

		PoolFactoryBean poolFactoryBean = spy(new PoolFactoryBean());

		assertThat(beanPostProcessor.postProcessBeforeInitialization(poolFactoryBean, "gemfirePool"))
			.isEqualTo(poolFactoryBean);

		verify(poolFactoryBean, times(1))
			.setPoolFactoryInitializer(isA(GemFireMockObjectsBeanPostProcessor.MockingPoolFactoryInitializer.class));
	}

	@Test
	public void postProcessBeforeInitializationWithRegularBean() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = spy(new GemFireMockObjectsBeanPostProcessor());

		Object bean = new Object();

		assertThat(beanPostProcessor.postProcessBeforeInitialization(bean, "regularBean")).isEqualTo(bean);

		verify(beanPostProcessor, times(1))
			.postProcessBeforeInitialization(eq(bean), eq("regularBean"));
		verifyNoMoreInteractions(beanPostProcessor);
	}

	@Test
	public void postProcessAfterInitializationWithGemFireCache() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = spy(new GemFireMockObjectsBeanPostProcessor());

		Properties gemfireProperties = new PropertiesBuilder()
			.setProperty("name", "postProcessAfterInitializationWithGemFireCacheTest")
			.setProperty("log-level", "error")
			.build();

		doReturn(gemfireProperties).when(beanPostProcessor).getGemFireProperties();

		GemFireCache mockCache = mock(GemFireCache.class);

		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

		doReturn(mockDistributedSystem).when(mockCache).getDistributedSystem();

		assertThat(beanPostProcessor.postProcessAfterInitialization(mockCache, "gemfireCache"))
			.isEqualTo(mockCache);

		assertThat(mockCache.getDistributedSystem().getProperties()).isSameAs(gemfireProperties);

		verify(beanPostProcessor, times(1)).getGemFireProperties();
	}

	@Test
	public void postProcessAfterInitializationWithRegularBean() {

		GemFireMockObjectsBeanPostProcessor beanPostProcessor = spy(new GemFireMockObjectsBeanPostProcessor());

		Object bean = new Object();

		assertThat(beanPostProcessor.postProcessAfterInitialization(bean, "regularBean"))
			.isEqualTo(bean);

		verify(beanPostProcessor, times(1))
			.postProcessAfterInitialization(eq(bean), eq("regularBean"));
		verifyNoMoreInteractions(beanPostProcessor);
	}
}
