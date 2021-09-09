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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.AfterClass;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.client.ClientCache;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.SpringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link ForkingClientServerIntegrationTestsSupport} class is an abstract base class used to configure
 * and bootstrap Apache Geode or VMware GemFire Server {@link Cache} and {@link ClientCache} applications.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.integration.config.ClientServerIntegrationTestsConfiguration
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class ForkingClientServerIntegrationTestsSupport extends ClientServerIntegrationTestsSupport {

	private static ProcessWrapper gemfireServer;

	public static void startGemFireServer(@NonNull Class<?> gemfireServerConfigurationClass, String... arguments)
			throws IOException {

		int availablePort = setAndGetPoolPortProperty(setAndGetCacheServerPortProperty(findAndReserveAvailablePort()));

		List<String> argumentList = new ArrayList<>(Arrays.asList(ArrayUtils.nullSafeArray(arguments, String.class)));

		argumentList.add(String.format("-D%s=%d", GEMFIRE_CACHE_SERVER_PORT_PROPERTY, availablePort));

		setGemFireServerProcess(run(gemfireServerConfigurationClass, argumentList.toArray(new String[0])));

		waitForServerToStart("localhost", availablePort);
	}

	public static void startGeodeServer(@NonNull Class<?> geodeServerConfigurationClass, String... arguments)
			throws IOException {

		startGemFireServer(geodeServerConfigurationClass, arguments);
	}

	protected static int setAndGetCacheServerPortProperty(int port) {

		System.setProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY, String.valueOf(port));

		return port;
	}

	protected static int setAndGetPoolPortProperty(int port) {

		System.setProperty(GEMFIRE_POOL_SERVERS_PROPERTY, String.format(GEMFIRE_LOCALHOST_PORT, port));

		return port;
	}

	@AfterClass
	public static void stopGemFireServer() {
		getGemFireServerProcess().ifPresent(ForkingClientServerIntegrationTestsSupport::stop);
		setGemFireServerProcess(null);
	}

	@AfterClass
	public static void clearCacheServerPortAndPoolPortProperties() {
		System.clearProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY);
		System.clearProperty(GEMFIRE_POOL_SERVERS_PROPERTY);
	}

	protected static synchronized void setGemFireServerProcess(@Nullable ProcessWrapper gemfireServerProcess) {
		gemfireServer = gemfireServerProcess;
	}

	protected static synchronized Optional<ProcessWrapper> getGemFireServerProcess() {
		return Optional.ofNullable(gemfireServer);
	}

	@EnablePdx
	@ClientCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
	public static class BaseGemFireClientConfiguration extends ClientServerIntegrationTestsConfiguration { }

	@EnablePdx
	@CacheServerApplication(name = "ForkingClientServerIntegrationTestsSupport", logLevel = GEMFIRE_LOG_LEVEL)
	public static class BaseGemFireServerConfiguration extends ClientServerIntegrationTestsConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext =
				new AnnotationConfigApplicationContext(BaseGemFireServerConfiguration.class);

			applicationContext.registerShutdownHook();
		}
	}

	@Configuration
	public static class SpringApplicationTerminatorConfiguration {

		@Bean
		public SpringApplicationTerminatorConfigurer springApplicationTerminatorConfigurer() {
			return SpringApplicationTerminatorConfigurer.EMPTY;
		}

		@EventListener(classes = ContextRefreshedEvent.class)
		public void configureTerminator(@NonNull ContextRefreshedEvent event) {

			Function<ApplicationContext, SpringApplicationTerminatorConfigurer> safeSpringApplicationTerminatorConfigurerResolver =
				applicationContext -> SpringUtils.safeGetValue(() ->
					applicationContext.getBean(SpringApplicationTerminatorConfigurer.class),
						(SpringApplicationTerminatorConfigurer) null);

			Runnable springApplicationTerminatorRunnable = () -> System.exit(-1);

			ThreadFactory springApplicationTerminatorThreadFactory = runnable -> {

				Thread springApplicationTerminatorThread =
					new Thread(runnable, "Spring Application Terminator Thread");

				springApplicationTerminatorThread.setDaemon(true);
				springApplicationTerminatorThread.setPriority(Thread.NORM_PRIORITY);

				return springApplicationTerminatorThread;
			};

			Optional.ofNullable(event)
				.map(ContextRefreshedEvent::getApplicationContext)
				.map(safeSpringApplicationTerminatorConfigurerResolver)
				.filter(SpringApplicationTerminatorConfigurer::isNotEmpty)
				.filter(this::delayIsGreaterThanZero)
				.ifPresent(configurer ->
					Executors.newScheduledThreadPool(1, springApplicationTerminatorThreadFactory)
						.schedule(springApplicationTerminatorRunnable, configurer.delay().toMillis(),
							TimeUnit.MILLISECONDS)
				);
		}

		private boolean delayIsGreaterThanZero(@NonNull SpringApplicationTerminatorConfigurer configurer) {

			return Optional.ofNullable(configurer)
				.map(SpringApplicationTerminatorConfigurer::delay)
				.map(delay -> Duration.ZERO.compareTo(delay) > 0)
				.orElse(false);
		}
	}

	public interface SpringApplicationTerminatorConfigurer {

		SpringApplicationTerminatorConfigurer EMPTY = new SpringApplicationTerminatorConfigurer() { };

		default boolean isEmpty() {
			return EMPTY.equals(this);
		}

		default boolean isNotEmpty() {
			return !isEmpty();
		}

		default Duration delay() {
			return Duration.ZERO;
		}
	}
}
