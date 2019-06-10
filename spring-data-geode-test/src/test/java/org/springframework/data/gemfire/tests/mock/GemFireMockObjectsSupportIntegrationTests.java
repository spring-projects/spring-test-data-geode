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

package org.springframework.data.gemfire.tests.mock;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.data.gemfire.tests.support.AbstractSecurityManager;

/**
 * Integration tests for {@link GemFireMockObjectsSupport}.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @since 1.0.0
 */
public class GemFireMockObjectsSupportIntegrationTests extends IntegrationTestsSupport {

	@After
	public void tearDown() {

		GemFireMockObjectsSupport.destroy();

		TestSecurityManager.constructed.set(false);
		TestSecurityManager.destroyed.set(false);

		TestSecurityPostProcessor.constructed.set(false);
	}

	@Test
	public void constructsGemFireObjectsFromPropertiesSuccessfully() {

		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty("name", "TestConstructsGemFireObjectsFromPropertiesSuccessfully");
		gemfireProperties.setProperty("security-manager", TestSecurityManager.class.getName());

		assertThat(TestSecurityManager.constructed.get()).isFalse();

		GemFireMockObjectsSupport.spyOn(new CacheFactory(gemfireProperties)).create();

		assertThat(TestSecurityManager.constructed.get()).isTrue();
	}

	@Test
	public void destroysConstructedGemFireObjectsFromPropertiesSuccessfully() {

		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty("name", "TestConstructsGemFireObjectsFromPropertiesSuccessfully");
		gemfireProperties.setProperty("security-manager", TestSecurityManager.class.getName());
		gemfireProperties.setProperty("security-post-processor", TestSecurityPostProcessor.class.getName());

		assertThat(TestSecurityManager.constructed.get()).isFalse();
		assertThat(TestSecurityManager.destroyed.get()).isFalse();
		assertThat(TestSecurityPostProcessor.constructed.get()).isFalse();

		GemFireMockObjectsSupport.spyOn(new CacheFactory(gemfireProperties)).create();

		assertThat(TestSecurityManager.constructed.get()).isTrue();
		assertThat(TestSecurityManager.destroyed.get()).isFalse();
		assertThat(TestSecurityPostProcessor.constructed.get()).isTrue();

		GemFireMockObjectsSupport.destroyGemFireObjects();

		assertThat(TestSecurityManager.destroyed.get()).isTrue();
	}

	@Test
	public void storesGemFirePropertiesSuccessfully() {

		try {

			System.setProperty("gemfire.name", "TestStoresGemFirePropertiesSuccessfully");
			System.setProperty("gemfire.log-level", "config");
			System.setProperty("gemfire.locators", "skullbox[12345]");
			System.setProperty("non-gemfire.property", "test");

			Properties gemfireProperties = new Properties();

			gemfireProperties.setProperty("log-level", "info");
			gemfireProperties.setProperty("jmx-manager-port", "1199");
			gemfireProperties.setProperty("groups", "test,mock");

			CacheFactory mockCacheFactory =
				GemFireMockObjectsSupport.spyOn(new CacheFactory(gemfireProperties));

			mockCacheFactory.set("groups", "qa,test,testers");
			mockCacheFactory.set("conserve-sockets", "true");

			Cache mockCache = mockCacheFactory.create();

			assertThat(mockCache).isNotNull();
			assertThat(mockCache.getDistributedSystem()).isNotNull();

			Properties actualGemFireProperties = mockCache.getDistributedSystem().getProperties();

			assertThat(actualGemFireProperties).isNotNull();
			assertThat(actualGemFireProperties).hasSize(6);
			assertThat(actualGemFireProperties.getProperty("name")).isEqualTo("TestStoresGemFirePropertiesSuccessfully");
			assertThat(actualGemFireProperties.getProperty("log-level")).isEqualTo("config");
			assertThat(actualGemFireProperties.getProperty("locators")).isEqualTo("skullbox[12345]");
			assertThat(actualGemFireProperties.getProperty("jmx-manager-port")).isEqualTo("1199");
			assertThat(actualGemFireProperties.getProperty("groups")).isEqualTo("qa,test,testers");
			assertThat(actualGemFireProperties.getProperty("conserve-sockets")).isEqualTo("true");
		}
		finally {
			System.clearProperty("gemfire.name");
			System.clearProperty("gemfire.log-level");
			System.clearProperty("non-gemfire.property");
		}
	}

	public static final class TestSecurityManager extends AbstractSecurityManager implements DisposableBean {

		private static final AtomicBoolean constructed = new AtomicBoolean(false);
		private static final AtomicBoolean destroyed = new AtomicBoolean(false);

		public TestSecurityManager() {
			constructed.set(true);
		}

		@Override
		public void destroy() {
			destroyed.set(true);
		}
	}

	public static final class TestSecurityPostProcessor {

		private static final AtomicBoolean constructed = new AtomicBoolean(false);

		public TestSecurityPostProcessor() {
			constructed.set(true);
		}
	}
}
