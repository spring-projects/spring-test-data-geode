/*
 *  Copyright 2018 the original author or authors.
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
package org.springframework.data.gemfire.tests.integration.config;

import static org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTestsSupport.GEMFIRE_CACHE_SERVER_PORT_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.server.CacheServer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.config.annotation.CacheServerConfigurer;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.util.ClassUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClientServerIntegrationTestsConfiguration} class is a Spring {@link Configuration} class that registers
 * a {@link ClientCacheConfigurer} used to configure the {@link ClientCache} {@literal DEFAULT} {@link Pool} port
 * to connect to the launched Apache Geode/Pivotal GemFire Server during integration testing.
 *
 * Additionally, this class registers a {@link CacheServerConfigurer} to configure the {@link CacheServer} port.
 *
 * Finally, this class provides a Spring {@link Configuration} class enable the embedded Locator and Manager services
 * in a server providing the {@literal locator-manager} Spring profile is activated.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.Pool
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.config.annotation.CacheServerConfigurer
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class ClientServerIntegrationTestsConfiguration {

	private static final int DEFAULT_PORT = CacheServer.DEFAULT_PORT;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected Logger getLogger() {
		return this.logger;
	}

	@Bean
	@Conditional(SpringBootIsAbsentCondition.class)
	// Required bean to resolve property placeholders in Spring @Value annotations.
	static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	CacheServerConfigurer cacheServerPortConfigurer(
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":" + DEFAULT_PORT + "}") int port) {

		return (beanName, cacheServerFactoryBean) -> cacheServerFactoryBean.setPort(port);
	}

	@Bean
	ClientCacheConfigurer clientCachePoolPortConfigurer(
			@Value("${" + GEMFIRE_CACHE_SERVER_PORT_PROPERTY + ":" + DEFAULT_PORT + "}") int port) {

		return (beanName, clientCacheFactoryBean) -> {

			List<ConnectionEndpoint> servers =
				Collections.singletonList(new ConnectionEndpoint("localhost", port));

			clientCacheFactoryBean.setServers(servers);
		};
	}

	@Configuration
	@EnableLocator
	@EnableLogging
	@EnableManager(start = true)
	@Profile("locator-manager")
	static class LocatorManagerConfiguration { }

	public static class SpringBootIsAbsentCondition implements Condition {

		@Override
		@SuppressWarnings("all")
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

			return !ClassUtils.isPresent("org.springframework.boot.SpringApplication",
				Thread.currentThread().getContextClassLoader());
		}
	}
}
