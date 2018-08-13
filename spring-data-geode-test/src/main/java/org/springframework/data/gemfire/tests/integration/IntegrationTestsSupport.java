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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.internal.net.SocketCreatorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;

/**
 * The {@link IntegrationTestsSupport} class is an abstract base class supporting integration tests
 * with either Apache Geode or Pivotal GemFire in a Spring context.
 *
 * @author John Blum
 * @see org.apache.geode.cache.GemFireCache
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class IntegrationTestsSupport {

	protected static final long DEFAULT_WAIT_DURATION = TimeUnit.SECONDS.toMillis(30);
	protected static final long DEFAULT_WAIT_INTERVAL = 500L; // milliseconds

	protected static final String GEMFIRE_LOG_FILE = "gemfire-server.log";
	protected static final String GEMFIRE_LOG_FILE_PROPERTY = "spring.data.gemfire.log.file";
	protected static final String GEMFIRE_LOG_LEVEL = "error";
	protected static final String GEMFIRE_LOG_LEVEL_PROPERTY = "spring.data.gemfire.log.level";
	protected static final String TEST_GEMFIRE_LOG_LEVEL = "error";

	private static final Predicate<String> GEMFIRE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("gemfire");

	private static final Predicate<String> GEODE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("geode");

	private static final Predicate<String> SPRING_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("spring");

	private static final Predicate<String> ALL_SYSTEM_PROPERTIES_NAME_PREDICATE =
		GEMFIRE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE
			.or(GEODE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE)
			.or(SPRING_DOT_SYSTEM_PROPERTY_NAME_PREDICATE);

	@BeforeClass
	public static void clearAllSpringApacheGeodeAndPivotalGemFireDotPrefixedSystemProperties() {

		List<String> springSystemProperties = System.getProperties().stringPropertyNames().stream()
			.filter(ALL_SYSTEM_PROPERTIES_NAME_PREDICATE)
			.collect(Collectors.toList());

		springSystemProperties.forEach(System::clearProperty);
	}

	@BeforeClass
	public static void closeAnyExistingGemFireCacheInstanceBeforeTestExecution() {
		closeGemFireCacheWaitOnCloseEvent();
	}

	@BeforeClass
	public static void closeAnyExistingSocketConfigurationBeforeTestExecution() {
		SocketCreatorFactory.close();
		//SSLConfigurationFactory.close();
	}

	@AfterClass
	public static void destroyAllGemFireMockObjects() {
		GemFireMockObjectsSupport.destroy();
	}

	public static void closeGemFireCacheWaitOnCloseEvent() {
		closeGemFireCacheWaitOnCloseEvent(DEFAULT_WAIT_DURATION);
	}

	public static void closeGemFireCacheWaitOnCloseEvent(long duration) {

		AtomicBoolean closed = new AtomicBoolean(false);

		waitOn(() -> {
			try {
				return Optional.ofNullable(GemfireUtils.resolveGemFireCache())
					.filter(cache -> !closed.get())
					.filter(cache -> !cache.isClosed())
					.map(IntegrationTestsSupport::close)
					.map(GemFireCache::isClosed)
					.orElse(true);
			}
			catch (CacheClosedException ignore) {
				closed.set(true);
				return true;
			}
		}, duration);
	}

	private static GemFireCache close(GemFireCache cache) {

		return Optional.ofNullable(cache)
			.map(it -> {
				cache.close();
				return cache;
			}).orElse(cache);
	}

	protected static boolean waitOn(Condition condition) {
		return waitOn(condition, DEFAULT_WAIT_DURATION);
	}

	@SuppressWarnings("all")
	protected static boolean waitOn(Condition condition, long duration) {

		long timeout = System.currentTimeMillis() + duration;

		try {
			while (!condition.evaluate() && System.currentTimeMillis() < timeout) {
				synchronized (condition) {
					TimeUnit.MILLISECONDS.timedWait(condition, DEFAULT_WAIT_INTERVAL);
				}
			}
		}
		catch (InterruptedException cause) {
			Thread.currentThread().interrupt();
		}

		return condition.evaluate();
	}

	@FunctionalInterface
	protected interface Condition {
		boolean evaluate();
	}
}
