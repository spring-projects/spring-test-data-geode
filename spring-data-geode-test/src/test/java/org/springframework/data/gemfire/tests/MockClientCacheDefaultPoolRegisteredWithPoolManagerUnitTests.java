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
package org.springframework.data.gemfire.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnablePool;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests for registering the {@link ClientCache} {@literal DEFAULT} {@link Pool} with the {@link PoolManager}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.client.PoolManager
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnablePool
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class MockClientCacheDefaultPoolRegisteredWithPoolManagerUnitTests {

	@Autowired
	@Qualifier("DEFAULT")
	private Pool defaultPool;

	@Autowired
	@Qualifier("MOCK")
	private Pool mockPool;

	@Autowired
	@Qualifier("TEST")
	private Pool testPool;

	@AfterClass
	public static void tearDown() {

		GemFireMockObjectsSupport.destroy();

		assertThat(PoolManager.find("DEFAULT")).isNull();
		assertThat(PoolManager.find("MOCK")).isNull();
		assertThat(PoolManager.find("TEST")).isNull();
	}

	@Before
	public void setup() {

		assertThat(this.defaultPool).isNotNull();
		assertThat(this.defaultPool.getName()).isEqualTo("DEFAULT");

		assertThat(this.mockPool).isNotNull();
		assertThat(this.mockPool.getName()).isEqualTo("MOCK");
	}

	@Test
	@DirtiesContext
	//@Ignore("Apache Geode/VMware GemFire does not support Mock Pools")
	public void defaultPoolRegisteredWithPoolManager() {

		Pool defaultPool = PoolManager.find("DEFAULT");

		assertThat(defaultPool).isNotNull();
		assertThat(defaultPool.getName()).isEqualTo("DEFAULT");
		assertThat(defaultPool).isSameAs(this.defaultPool);
	}

	@Test
	public void mockPoolRegisteredWithPoolManager() {

		Pool mockPool = PoolManager.find("MOCK");

		assertThat(mockPool).isNotNull();
		assertThat(mockPool.getName()).isEqualTo("MOCK");
		assertThat(mockPool).isSameAs(this.mockPool);
	}

	@Test
	public void testPoolRegisteredWithPoolManager() {

		Pool testPool = PoolManager.find("TEST");

		assertThat(testPool).isNotNull();
		assertThat(testPool.getName()).isEqualTo("TEST");
		assertThat(testPool.getLocators()).containsExactly(new InetSocketAddress("skullbox", 12345));
		assertThat(testPool).isSameAs(this.testPool);
	}

	@ClientCacheApplication
	@EnableGemFireMockObjects
	@EnablePool(name = "DEFAULT")
	static class TestConfiguration {

		@Bean("MOCK")
		Pool mockPool() {
			return GemFireMockObjectsSupport.mockPoolFactory().create("MOCK");
		}

		@Bean("TEST")
		PoolFactoryBean testPool() {

			PoolFactoryBean testPool = new PoolFactoryBean();

			testPool.setName("TEST");
			testPool.setLocators(Collections.singleton(new ConnectionEndpoint("skullbox", 12345)));

			return testPool;
		}
	}
}
