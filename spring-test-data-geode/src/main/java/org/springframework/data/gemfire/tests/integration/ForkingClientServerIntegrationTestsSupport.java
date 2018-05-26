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

package org.springframework.data.gemfire.tests.integration;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;

/**
 * The {@link ForkingClientServerIntegrationTestsSupport} class...
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @since 1.0.0
 */
@SuppressWarnings("unused")
// TODO: this is a WIP; I need to figure out client/server logistics when launching a CacheServer;
// this class will be replaced by a JUnit Rule anyhow
public abstract class ForkingClientServerIntegrationTestsSupport extends ClientServerIntegrationTestsSupport {

	private static volatile ProcessWrapper gemfireServer;

	@BeforeClass
	public static void startGemFireServer() throws IOException {

		int availablePort = findAvailablePort();

		gemfireServer = run(GemFireServerConfiguration.class,
			String.format("-D%s=%d", GEMFIRE_CACHE_SERVER_PORT_PROPERTY, availablePort));

		waitForServerToStart("localhost", availablePort);

		System.setProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY, String.valueOf(availablePort));
	}

	@AfterClass
	public static void stopGemFireServer() {
		System.clearProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY);
		stop(gemfireServer);
		gemfireServer = null;
	}

	protected Optional<ProcessWrapper> getGemFireServerProcess() {
		return Optional.ofNullable(gemfireServer);
	}

	@Configuration
	public static class BaseGemFireClientConfiguration {

		@Bean
		ClientCacheConfigurer clientCachePoolPortConfigurer(
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":40404}") int port) {

			return (beanName, clientCacheFactoryBean) -> clientCacheFactoryBean.setServers(
				Collections.singletonList(new ConnectionEndpoint("localhost", port)));
		}
	}

	@EnablePdx
	@CacheServerApplication(name = "ForkingClientServerIntegrationTestsSupport", logLevel = GEMFIRE_LOG_LEVEL)
	public static class GemFireServerConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(GemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}
	}
}
