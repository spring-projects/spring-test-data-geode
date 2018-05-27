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

import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
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
// TODO: this class is a WIP; I need to figure out client/server configuration and logistics
// when launching a CacheServer; this class will be replaced by a JUnit Rule anyhow
public abstract class ForkingClientServerIntegrationTestsSupport extends ClientServerIntegrationTestsSupport {

	private static ProcessWrapper gemfireServer;

	public static void startGemFireServer(Class<?> gemfireServerConfigurationClass, String... arguments)
			throws IOException {

		int availablePort = setAndGetCacheServerPortProperty();

		List<String> argumentList = new ArrayList<>();

		argumentList.addAll(Arrays.asList(nullSafeArray(arguments, String.class)));
		argumentList.add(String.format("-D%s=%d", GEMFIRE_CACHE_SERVER_PORT_PROPERTY, availablePort));

		setGemFireServerProcess(run(gemfireServerConfigurationClass,
			argumentList.toArray(new String[argumentList.size()])));

		waitForServerToStart("localhost", availablePort);
	}

	protected static int setAndGetCacheServerPortProperty() throws IOException {

		int availablePort = findAvailablePort();

		System.setProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY, String.valueOf(availablePort));

		return availablePort;
	}

	@AfterClass
	public static void stopGemFireServer() {
		getGemFireServerProcess().ifPresent(ForkingClientServerIntegrationTestsSupport::stop);
		setGemFireServerProcess(null);
	}

	@AfterClass
	public static void clearCacheServerPortProperty() {
		System.clearProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY);
	}

	protected static synchronized Optional<ProcessWrapper> getGemFireServerProcess() {
		return Optional.ofNullable(gemfireServer);
	}

	protected static synchronized void setGemFireServerProcess(ProcessWrapper gemfireServerProcess) {
		gemfireServer = gemfireServerProcess;
	}

	@EnablePdx
	@ClientCacheApplication(logLevel = GEMFIRE_LOG_FILE)
	protected static class BaseGemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration { }

	@EnablePdx
	@CacheServerApplication(name = "ForkingClientServerIntegrationTestsSupport", logLevel = GEMFIRE_LOG_LEVEL)
	public static class BaseGemFireServerConfiguration extends ClientServerIntegrationTestsConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(BaseGemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}
	}
}
