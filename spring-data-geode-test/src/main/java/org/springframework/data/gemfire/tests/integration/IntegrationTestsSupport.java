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

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.internal.InternalDataSerializer;
import org.apache.geode.internal.net.SocketCreatorFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.util.FileUtils;
import org.springframework.data.gemfire.util.CollectionUtils;

/**
 * The {@link IntegrationTestsSupport} class is an abstract base class supporting integration tests
 * with either Apache Geode or Pivotal GemFire in a Spring context.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.time.LocalDateTime
 * @see java.util.concurrent.TimeUnit
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class IntegrationTestsSupport {

	protected static final boolean DEFAULT_DEBUG_GEMFIRE_QUERIES = false;

	protected static final boolean DEBUG_GEMFIRE_QUERIES = Boolean.getBoolean("spring.data.gemfire.query.debug");

	protected static final long DEFAULT_WAIT_DURATION = TimeUnit.SECONDS.toMillis(30);
	protected static final long DEFAULT_WAIT_INTERVAL = 500L; // milliseconds

	protected static final String DIRECTORY_DELETE_ON_EXIT_PROPERTY = "spring.data.gemfire.test.directory.delete-on-exit";
	protected static final String GEMFIRE_LOG_FILE = "gemfire-server.log";
	protected static final String GEMFIRE_LOG_FILE_PROPERTY = "spring.data.gemfire.log.file";
	protected static final String GEMFIRE_LOG_LEVEL = "error";
	protected static final String GEMFIRE_LOG_LEVEL_PROPERTY = "spring.data.gemfire.log.level";
	protected static final String GEMFIRE_QUERY_VERBOSE_PROPERTY = "gemfire.Query.VERBOSE";
	protected static final String SYSTEM_PROPERTIES_LOG_FILE = "system-properties.log";
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
	public static void clearAllGemFireGeodeAndSpringDotPrefixedSystemProperties() {

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

	@Before
	public void configureQueryDebugging() {

		if (isQueryDebuggingEnabled()) {
			System.setProperty(GEMFIRE_QUERY_VERBOSE_PROPERTY, Boolean.TRUE.toString());
		}
	}

	@AfterClass
	public static void destroyAllGemFireMockObjects() {
		GemFireMockObjectsSupport.destroy();
	}

	@AfterClass
	public static void unregisterAllDataSerializers() {

		stream(nullSafeArray(InternalDataSerializer.getSerializers(), DataSerializer.class))
			.map(DataSerializer::getId)
			.forEach(InternalDataSerializer::unregister);
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

	protected static String asApplicationName(Class<?> type) {
		return type.getSimpleName();
	}

	protected static String asDirectoryName(Class<?> type) {
		return String.format("%1$s-%2$s", asApplicationName(type),
			LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")));
	}

	protected static File createDirectory(String pathname) {
		return createDirectory(new File(pathname));
	}

	protected static File createDirectory(File directory) {

		assertThat(directory.isDirectory() || directory.mkdirs())
			.as(String.format("Failed to create directory [%s]", directory)).isTrue();

		if (isDeleteDirectoryOnExit()) {
			directory.deleteOnExit();
		}

		return directory;
	}

	protected static boolean isDeleteDirectoryOnExit() {
		return Boolean.valueOf(System.getProperty(DIRECTORY_DELETE_ON_EXIT_PROPERTY, Boolean.TRUE.toString()));
	}

	protected boolean isQueryDebuggingEnabled() {
		return DEBUG_GEMFIRE_QUERIES || withQueryDebugging();
	}

	protected static String getClassNameAsPath(Class type) {
		return type.getName().replaceAll("\\.", "/");
	}

	protected static String getClassNameAsPath(Object obj) {
		return getClassNameAsPath(obj.getClass());
	}

	protected static String getPackageNameAsPath(Class type) {
		return type.getPackage().getName().replaceAll("\\.", "/");
	}

	protected static String getPackageNameAsPath(Object obj) {
		return getPackageNameAsPath(obj.getClass());
	}

	protected static String getContextXmlFileLocation(Class type) {
		return getClassNameAsPath(type).concat("-context.xml");
	}

	protected static String getServerContextXmlFileLocation(Class type) {
		return getClassNameAsPath(type).concat("-server-context.xml");
	}

	protected static String logFile() {
		return logFile(GEMFIRE_LOG_FILE);
	}

	protected static String logFile(String defaultLogFilePathname) {
		return System.getProperty(GEMFIRE_LOG_FILE_PROPERTY, defaultLogFilePathname);
	}

	protected static String logLevel() {
		return logLevel(GEMFIRE_LOG_LEVEL);
	}

	protected static String logLevel(String defaultLogLevel) {
		return System.getProperty(GEMFIRE_LOG_LEVEL_PROPERTY, defaultLogLevel);
	}

	protected static void logSystemProperties() throws IOException {
		FileUtils.write(new File(SYSTEM_PROPERTIES_LOG_FILE),
			String.format("%s", CollectionUtils.toString(System.getProperties())));
	}

	protected static boolean waitOn(Condition condition) {
		return waitOn(condition, DEFAULT_WAIT_DURATION);
	}

	protected static boolean waitOn(Condition condition, long duration) {
		return waitOn(condition, duration, DEFAULT_WAIT_INTERVAL);
	}

	@SuppressWarnings("all")
	protected static boolean waitOn(Condition condition, long duration, long interval) {

		long resolvedInterval = Math.max(Math.min(interval, duration), 1);
		long timeout = System.currentTimeMillis() + duration;

		try {
			while (!condition.evaluate() && System.currentTimeMillis() < timeout) {
				synchronized (condition) {
					TimeUnit.MILLISECONDS.timedWait(condition, resolvedInterval);
				}
			}
		}
		catch (InterruptedException cause) {
			Thread.currentThread().interrupt();
		}

		return condition.evaluate();
	}

	protected void withDeleteDirectoryOnExit(boolean delete) {
		System.setProperty(DIRECTORY_DELETE_ON_EXIT_PROPERTY, String.valueOf(delete));
	}

	protected boolean withQueryDebugging() {
		return DEFAULT_DEBUG_GEMFIRE_QUERIES;
	}

	@FunctionalInterface
	protected interface Condition {
		boolean evaluate();
	}
}
