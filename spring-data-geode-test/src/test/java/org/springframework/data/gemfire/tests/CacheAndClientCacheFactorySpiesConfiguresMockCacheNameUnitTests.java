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
package org.springframework.data.gemfire.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.cache.GemFireCacheImpl;

import org.junit.After;
import org.junit.Test;

import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;

/**
 * Unit Tests for spying on {@link CacheFactory} and {@link ClientCacheFactory} objects as well as asserting
 * the configuration of the mock cache instance created by the factories.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientCacheFactory
 * @see org.apache.geode.distributed.internal.DistributionConfig
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @since 0.0.6
 */
public class CacheAndClientCacheFactorySpiesConfiguresMockCacheNameUnitTests {

	@After
	public void tearDown() {
		GemFireMockObjectsSupport.destroy();
	}

	@Test
	public void cacheFactorySpyConfiguresMockCacheName() {

		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty(DistributionConfig.NAME_NAME, "MockCacheName");

		CacheFactory cacheFactory = GemFireMockObjectsSupport.spyOn(new CacheFactory(gemfireProperties));

		assertThat(cacheFactory).isNotNull();

		Cache mockCache = cacheFactory.create();

		assertThat(mockCache).isNotNull();
		assertThat(mockCache).isNotInstanceOf(GemFireCacheImpl.class);
		assertThat(mockCache.getDistributedSystem()).isNotNull();
		assertThat(mockCache.getDistributedSystem().getProperties()).isNotNull();
		assertThat(mockCache.getDistributedSystem().getProperties().getProperty(DistributionConfig.NAME_NAME))
			.isEqualTo("MockCacheName");
		assertThat(mockCache.getName()).isEqualTo("MockCacheName");
	}

	@Test
	public void clientCacheFactorySpyConfigureMockClientCacheName() {

		ClientCacheFactory clientCacheFactory = GemFireMockObjectsSupport.spyOn(new ClientCacheFactory()
			.set(DistributionConfig.NAME_NAME, "MockClientCacheName"));

		assertThat(clientCacheFactory).isNotNull();

		ClientCache mockClientCache = clientCacheFactory.create();

		assertThat(mockClientCache).isNotNull();
		assertThat(mockClientCache).isNotInstanceOf(GemFireCacheImpl.class);
		assertThat(mockClientCache.getDistributedSystem()).isNotNull();
		assertThat(mockClientCache.getDistributedSystem().getProperties()).isNotNull();
		assertThat(mockClientCache.getDistributedSystem().getProperties().getProperty(DistributionConfig.NAME_NAME))
			.isEqualTo("MockClientCacheName");
		assertThat(mockClientCache.getName()).isEqualTo("MockClientCacheName");
	}
}
