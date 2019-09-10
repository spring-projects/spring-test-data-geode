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
package org.springframework.data.gemfire.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The ClientServerIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ClientServerIntegrationTests.TestGeodeClientConfiguration.class)
@SuppressWarnings("unused")
public class ClientServerIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static final String GEODE_LOG_LEVEL = "error";

	@BeforeClass
	public static void startGeodeServer() throws IOException {
		startGemFireServer(TestGeodeServerConfiguration.class);
	}

	@Resource(name = "Example")
	private Region<Object, Object> example;

 	@Test
	public void clientServerInteractionSuccessful() {

 		assertThat(this.example.put(1L, "test")).isNull();
 		assertThat(this.example.get(1L)).isEqualTo("test");
	}

	@ClientCacheApplication(logLevel = GEODE_LOG_LEVEL)
	//@Import(ClientServerIntegrationTestsConfiguration.class)
	static class TestGeodeClientConfiguration extends ClientServerIntegrationTestsConfiguration {

		@Bean("Example")
		public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setClose(false);
			exampleRegion.setShortcut(ClientRegionShortcut.PROXY);

			return exampleRegion;
		}
	}

	@CacheServerApplication(name = "ClientServerIntegrationTests", logLevel = GEODE_LOG_LEVEL)
	static class TestGeodeServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(TestGeodeServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}

		@Bean("Example")
		public PartitionedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

			PartitionedRegionFactoryBean<Object, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

			exampleRegion.setCache(gemfireCache);
			exampleRegion.setClose(false);
			exampleRegion.setPersistent(false);

			return exampleRegion;
		}
	}
}
