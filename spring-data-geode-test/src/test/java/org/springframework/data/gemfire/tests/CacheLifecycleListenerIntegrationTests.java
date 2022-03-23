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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.extensions.spring.context.annotation.DependencyOf;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests testing and asserting {@link GemFireCache} lifecycle events published as
 * {@link ApplicationEvent ApplicationEvents} in a Spring context.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 0.0.23
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class CacheLifecycleListenerIntegrationTests extends IntegrationTestsSupport {

	private static GemFireCache staticCache;
	private static TestCacheLifecycleListener staticListener;

	@Autowired
	private GemFireCache cache;

	@Autowired
	private TestCacheLifecycleListener listener;

	@Before
	public void setup() {

		assertThat(this.cache).isNotNull();
		assertThat(this.cache.isClosed()).isFalse();
		assertThat(this.cache.getName()).isEqualTo("CacheLifecycleListenerIntegrationTests");
		assertThat(this.listener).isNotNull();

		staticCache = this.cache;
		staticListener = this.listener;
	}

	@Test
	@DirtiesContext
	public void verifyCacheCreatedEventFired() {

		verify(this.listener, times(1)).handleCacheCreated(argThat(cacheCreatedEvent -> {

			assertThat(cacheCreatedEvent).isNotNull();
			assertThat(cacheCreatedEvent.getCache()).isEqualTo(this.cache);

			return true;
		}));
	}

	@AfterClass
	public static void verifyCacheClosedEventFired() {

		assertThat(staticCache).isNotNull();
		assertThat(staticListener).isNotNull();

		verify(staticListener, times(1)).handleCacheClosed(argThat(cacheClosedEvent -> {

			assertThat(cacheClosedEvent).isNotNull();
			assertThat(cacheClosedEvent.getCache()).isEqualTo(staticCache);

			return true;
		}));
	}

	@ClientCacheApplication(name = "CacheLifecycleListenerIntegrationTests")
	static class TestConfiguration {

		@Bean
		TestCacheLifecycleListener cacheLifecycleListener() {
			return spy(new TestCacheLifecycleListener());
		}
	}

	@Component
	@DependencyOf("gemfireCache")
	static class TestCacheLifecycleListener {

		@EventListener(CacheCreatedEvent.class)
		public void handleCacheCreated(CacheCreatedEvent event) {
			assertThat(event).isNotNull();
		}

		@EventListener(CacheClosedEvent.class)
		public void handleCacheClosed(CacheClosedEvent event) {
			assertThat(event).isNotNull();
		}
	}
}
