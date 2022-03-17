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
package org.springframework.data.gemfire.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.distributed.Locator;
import org.apache.geode.internal.InternalDataSerializer;
import org.apache.geode.internal.cache.CacheLifecycleListener;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.internal.net.SSLConfigurationFactory;
import org.apache.geode.internal.net.SocketCreatorFactory;

import org.apache.shiro.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.support.GemfireBeanFactoryLocator;
import org.springframework.data.gemfire.tests.config.TestProperties;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.data.gemfire.tests.util.FileUtils;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * The {@link IntegrationTestsSupport} class is an abstract base class supporting integration tests
 * with either Apache Geode or VMware GemFire in a Spring context.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.time.LocalDateTime
 * @see java.util.concurrent.TimeUnit
 * @see java.util.function.Function
 * @see java.util.function.Predicate
 * @see org.apache.geode.DataSerializer
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.distributed.Locator
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.env.StandardEnvironment
 * @see org.springframework.data.gemfire.support.GemfireBeanFactoryLocator
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class IntegrationTestsSupport {

	protected static final boolean DEFAULT_DEBUG_GEMFIRE_QUERIES = false;

	protected static final boolean DEBUG_GEMFIRE_QUERIES = Boolean.getBoolean("spring.data.gemfire.query.debug");

	protected static final long DEFAULT_WAIT_DURATION = TimeUnit.SECONDS.toMillis(30);
	protected static final long DEFAULT_WAIT_INTERVAL = 500L; // milliseconds

	protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd-hh-mm-ss";
	protected static final String DIRECTORY_DELETE_ON_EXIT_PROPERTY = "spring.data.gemfire.test.directory.delete-on-exit";
	protected static final String DIRECTORY_NAME_FORMAT = "%1$s-%2$s";
	protected static final String GEMFIRE_LOG_FILE = "gemfire-server.log";
	protected static final String GEMFIRE_LOG_FILE_PROPERTY = "spring.data.gemfire.log.file";
	protected static final String GEMFIRE_LOG_LEVEL = "error";
	protected static final String GEMFIRE_LOG_LEVEL_PROPERTY = "spring.data.gemfire.log.level";
	protected static final String GEMFIRE_QUERY_VERBOSE_PROPERTY = "gemfire.Query.VERBOSE";
	protected static final String SYSTEM_PROPERTIES_LOG_FILE = "system-properties.log";
	protected static final String TEST_GEMFIRE_LOG_LEVEL = "error";

	private static final AtomicReference<ConfigurableApplicationContext> applicationContextReference =
		new AtomicReference<>(null);

	private static final Predicate<String> JAVAX_NET_SSL_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("javax.net.ssl");

	private static final Predicate<String> GEMFIRE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("gemfire");

	private static final Predicate<String> GEODE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("geode");

	private static final Predicate<String> SPRING_DOT_SYSTEM_PROPERTY_NAME_PREDICATE =
		propertyName -> String.valueOf(propertyName).toLowerCase().startsWith("spring");

	private static final Predicate<String> ALL_SYSTEM_PROPERTIES_NAME_PREDICATE = JAVAX_NET_SSL_NAME_PREDICATE
		.or(GEMFIRE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE)
		.or(GEODE_DOT_SYSTEM_PROPERTY_NAME_PREDICATE)
		.or(SPRING_DOT_SYSTEM_PROPERTY_NAME_PREDICATE);

	private static final Predicate<String> SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME =
		StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME::equals;

	private static final Predicate<String> SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME =
		StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME::equals;

	private static final Predicate<String> RETAINED_PROPERTY_SOURCE_NAMES = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
		.or(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);

	private static final TestContextCacheLifecycleListenerAdapter cacheLifecycleListener =
		TestContextCacheLifecycleListenerAdapter.getInstance();

	@Autowired(required = false)
	private ConfigurableApplicationContext applicationContext;

	/**
	 * Sets a reference to the configured Spring {@link ConfigurableApplicationContext}.
	 *
	 * @param <T> specific {@link Class type} of {@link ConfigurableApplicationContext}.
	 * @param applicationContext reference to the current, configured Spring {@link ConfigurableApplicationContext}.
	 * @return the given reference to the Spring {@link ConfigurableApplicationContext}.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see #getOptionalApplicationContext()
	 * @see #getApplicationContext()
	 */
	protected @Nullable <T extends ConfigurableApplicationContext> T setApplicationContext(
			@Nullable T applicationContext) {

		this.applicationContext = applicationContext;

		return applicationContext;
	}

	/**
	 * Gets a reference to the configured Spring {@link ConfigurableApplicationContext}.
	 *
	 * @param <T> specific {@link Class type} of {@link ConfigurableApplicationContext}.
	 * @return a reference to the configured Spring {@link ConfigurableApplicationContext}; maybe {@literal null}.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see #setApplicationContext(ConfigurableApplicationContext)
	 * @see #getOptionalApplicationContext()
	 */
	@SuppressWarnings("unchecked")
	protected @Nullable <T extends ConfigurableApplicationContext> T getApplicationContext() {
		return (T) this.applicationContext;
	}

	/**
	 * Gets an {@link Optional} reference to the configured Spring {@link ConfigurableApplicationContext}.
	 *
	 * @param <T> specific {@link Class type} of {@link ConfigurableApplicationContext}.
	 * @return an {@link Optional} reference to the configured Spring {@link ConfigurableApplicationContext}.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see #setApplicationContext(ConfigurableApplicationContext)
	 * @see #getApplicationContext()
	 * @see java.util.Optional
	 */
	protected <T extends ConfigurableApplicationContext> Optional<T> getOptionalApplicationContext() {
		return Optional.ofNullable(getApplicationContext());
	}

	/**
	 * Gets a required reference to the configured Spring {@link ConfigurableApplicationContext}.
	 *
	 * @param <T> specific {@link Class type} of {@link ConfigurableApplicationContext}.
	 * @return an {@literal non-null} reference to the configured Spring {@link ConfigurableApplicationContext}.
	 * @throws IllegalStateException if the Spring {@link ConfigurableApplicationContext} was not initialized.
	 * @see org.springframework.context.ConfigurableApplicationContext
	 * @see #setApplicationContext(ConfigurableApplicationContext)
	 * @see #getOptionalApplicationContext()
	 * @see #getApplicationContext()
	 */
	protected <T extends ConfigurableApplicationContext> T requireApplicationContext() {
		return this.<T>getOptionalApplicationContext()
			.orElseThrow(() -> newIllegalStateException("An ApplicationContext was not initialized"));
	}

	/**
	 * Clears all Java, VMware Tanzu GemFire, Apache Geode and Spring {@link System#getProperties() System Properties}
	 * after test (class/suite) execution.
	 *
	 * @see java.lang.System#getProperties()
	 */
	@AfterClass
	public static void clearAllJavaGemFireGeodeAndSpringDotPrefixedSystemProperties() {

		List<String> allSystemPropertyNames = System.getProperties().stringPropertyNames().stream()
			.filter(ALL_SYSTEM_PROPERTIES_NAME_PREDICATE)
			.collect(Collectors.toList());

		allSystemPropertyNames.forEach(System::clearProperty);
	}

	/**
	 * Clears (removes) all non-standard Spring {@link PropertySource PropertySources} from the Spring
	 * {@link Environment} after test (class/suite) execution.
	 *
	 * Only {@link System#getProperties() System Properties} and {@literal Environment Variables} are standard.
	 *
	 * @see org.springframework.core.env.Environment
	 */
	@AfterClass
	public static void clearNonStandardSpringEnvironmentPropertySources() {

		Optional.ofNullable(applicationContextReference.get())
			.map(ConfigurableApplicationContext::getEnvironment)
			.map(ConfigurableEnvironment::getPropertySources)
			.ifPresent(propertySources -> {
				for (PropertySource<?> propertySource : propertySources) {
					if (Objects.nonNull(propertySource)) {

						String propertySourceName = propertySource.getName();

						if (StringUtils.hasText(propertySourceName)) {
							if (!RETAINED_PROPERTY_SOURCE_NAMES.test(propertySource.getName())) {
								propertySources.remove(propertySourceName);
							}
						}
					}
				}
			});
	}

	/**
	 * Clears all test {@link System#getProperties()} after a test class (suite) execution.
	 *
	 * @see org.springframework.data.gemfire.tests.config.TestProperties
	 */
	@AfterClass
	public static void clearTestSystemProperties() {
		TestProperties.getInstance().clearSystemProperties();
	}

	/**
	 * Closes the use of all Spring {@literal BeanFactoryLocators} after test (class/suite) execution.
	 */
	@AfterClass
	public static void closeAllBeanFactoryLocators() {
		GemfireBeanFactoryLocator.clear();
	}

	/**
	 * Closes any Apache Geode {@link GemFireCache} after test (class/suite) execution.
	 *
	 * @see org.apache.geode.cache.GemFireCache
	 */
	@AfterClass
	public static void closeAnyGemFireCache() {
		closeGemFireCacheWaitOnCacheClosedEvent();
	}

	/**
	 * Closes any Apache Geode {@link Locator} after test (class/suite) execution.
	 *
	 * @see org.apache.geode.distributed.Locator
	 */
	@AfterClass
	public static void closeAnyGemFireLocator() {
		stopGemFireLocatorWaitOnStopEvent();
	}

	/**
	 * Closes any Apache Geode {@link java.net.Socket} configuration after test (class/suite) execution.
	 *
	 * @see java.net.Socket
	 */
	@AfterClass
	public static void closeAnySocketConfiguration() {
		SocketCreatorFactory.close();
	}

	/**
	 * Closes any Apache Geode {@literal SSL} configuration after test (class/suite) execution.
	 */
	@AfterClass
	public static void closeAnySslConfiguration() {

		//SSLConfigurationFactory.close();

		synchronized (SSLConfigurationFactory.class) {
			try {

				Field instance = ReflectionUtils.findField(SSLConfigurationFactory.class, "instance",
					SSLConfigurationFactory.class);

				Optional.ofNullable(instance)
					.ifPresent(field -> {
						ReflectionUtils.makeAccessible(field);
						ReflectionUtils.setField(field, null, null);
					});
			}
			catch (Throwable ignore) {
				// Not much we can do about it now!
			}
		}
	}

	/**
	 * Deletes any Apache Geode process ID ({@literal PID}) {@link File Files} after test (class/suite) execution.
	 *
	 * @see java.io.File
	 */
	@AfterClass
	public static void deleteAllGemFireProcessIdFiles() {

		FileFilter fileFilter = file -> file != null
			&& file.getName().startsWith("vf.gf")
			&& file.getName().endsWith(".pid");

		FileSystemUtils.deleteRecursive(FileSystemUtils.WORKING_DIRECTORY, fileFilter);
	}

	/**
	 * Destroys the use of all Apache Geode / VMware Tanzu GemFire mock objects and resources held by the mock objects
	 * after test (class/suite) execution.
	 */
	@AfterClass
	public static void destroyAllGemFireMockObjects() {
		GemFireMockObjectsSupport.destroy();
	}

	/**
	 * Unregisters all Apache Geode {@link DataSerializer DataSerializers} from Apache Geode's serialization framework
	 * (subsystem) after test (class/suite) execution.
	 *
	 * @see org.apache.geode.DataSerializer
	 */
	@AfterClass
	public static void unregisterAllDataSerializers() {

		Arrays.stream(ArrayUtils.nullSafeArray(InternalDataSerializer.getSerializers(), DataSerializer.class))
			.map(DataSerializer::getId)
			.forEach(InternalDataSerializer::unregister);
	}

	/**
	 * Unregisters all Apache Geode {@link Function Functions} from Apache Geode's {@link FunctionService}
	 * after test (class/suite) execution.
	 *
	 * @see org.apache.geode.cache.execute.Function
	 * @see org.apache.geode.cache.execute.FunctionService
	 */
	@AfterClass
	public static void unregisterFunctions() {

		CollectionUtils.nullSafeMap(FunctionService.getRegisteredFunctions())
			.forEach((functionId, function) -> FunctionService.unregisterFunction(functionId));
	}

	/**
	 * Loads any {@literal test.properties} before test execution.
	 *
	 * Additionally, any {@link System} defined test properties (i.e. properties prefixed with {@literal system.})
	 * are set in the {@link System#getProperties()}.
	 *
	 * @see org.springframework.data.gemfire.tests.config.TestProperties
	 */
	@BeforeClass
	public static void loadTestProperties() {
		TestProperties.getInstance().configureSystemProperties();
	}

	/**
	 * Configures (eanbles) Apache Geode {@literal OQL} query debugging before test (case/method) execution.
	 */
	@Before
	public void configureQueryDebugging() {

		if (isQueryDebuggingEnabled()) {
			System.setProperty(GEMFIRE_QUERY_VERBOSE_PROPERTY, Boolean.TRUE.toString());
		}
	}

	/**
	 * Stores a reference to the optionally autowired/injected Spring {@link ApplicationContext} in the global context.
	 *
	 * @see org.springframework.context.ConfigurableApplicationContext
	 */
	@Before
	public void referenceApplicationContext() {
		applicationContextReference.set(this.applicationContext);
	}

	public static void closeApplicationContext(@Nullable ApplicationContext applicationContext) {

		Optional.ofNullable(applicationContext)
			.filter(ConfigurableApplicationContext.class::isInstance)
			.map(ConfigurableApplicationContext.class::cast)
			.ifPresent(ConfigurableApplicationContext::close);
	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent() {
		closeGemFireCacheWaitOnCacheClosedEvent(DEFAULT_WAIT_DURATION);
	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent(long duration) {
		closeGemFireCacheWaitOnCacheClosedEvent(GemfireUtils::resolveGemFireCache, duration);
	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent(@NonNull Supplier<GemFireCache> cacheSupplier) {
		closeGemFireCacheWaitOnCacheClosedEvent(cacheSupplier, DEFAULT_WAIT_DURATION);
	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent(@NonNull Supplier<GemFireCache> cacheSupplier,
			@NonNull Function<GemFireCache, GemFireCache> cacheClosingFunction) {

		closeGemFireCacheWaitOnCacheClosedEvent(cacheSupplier, cacheClosingFunction, DEFAULT_WAIT_DURATION);
	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent(@NonNull Supplier<GemFireCache> cacheSupplier,
			long duration) {

		Function<GemFireCache, GemFireCache> cacheClosingFunction = cacheToClose -> {

			if (GemfireUtils.isClient(cacheToClose)) {
				((ClientCache) cacheToClose).close(false);
			}
			else {
				cacheToClose.close();
			}

			return cacheToClose;
		};

		closeGemFireCacheWaitOnCacheClosedEvent(cacheSupplier, cacheClosingFunction, duration);

	}

	public static void closeGemFireCacheWaitOnCacheClosedEvent(@NonNull Supplier<GemFireCache> cacheSupplier,
			@NonNull Function<GemFireCache, GemFireCache> cacheClosingFunction, long duration) {

		AtomicBoolean closed = new AtomicBoolean(false);

		waitOn(() -> {
			try {
				return Optional.ofNullable(cacheSupplier.get())
					.filter(cache -> !closed.get())
					.map(cacheClosingFunction)
					.map(cacheLifecycleListener::isClosed)
					.orElse(true);
			}
			catch (CacheClosedException ignore) {
				closed.set(true);
				return true;
			}
		}, duration);
	}

	public static void stopGemFireLocatorWaitOnStopEvent() {
		stopGemFireLocatorWaitOnStopEvent(DEFAULT_WAIT_DURATION);
	}

	public static void stopGemFireLocatorWaitOnStopEvent(long duration) {

		AtomicBoolean stopped = new AtomicBoolean(false);

		waitOn(() -> {
			try {
				return Optional.ofNullable(Locator.getLocator())
					.filter(it -> !stopped.get())
					.map(IntegrationTestsSupport::stop)
					.map(it -> {
						stopped.set(!Locator.hasLocator());
						return stopped.get();
					})
					.orElse(true);
			}
			catch (Exception ignore) {
				stopped.set(true);
				return true;
			}
		}, duration);
	}

	private static @Nullable Locator stop(@Nullable Locator locator) {

		return Optional.ofNullable(locator)
			.map(it -> {
				it.stop();
				return it;
			})
			.orElse(locator);
	}

	protected static @NonNull String asApplicationName(@NonNull Class<?> type) {
		return type.getSimpleName();
	}

	protected static @NonNull String asDirectoryName(@NonNull Class<?> type) {
		return String.format(DIRECTORY_NAME_FORMAT, asApplicationName(type),
			LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
	}

	protected static @NonNull File createDirectory(@NonNull String pathname) {
		return createDirectory(new File(pathname));
	}

	protected static @NonNull File createDirectory(@NonNull File directory) {

		assertThat(directory)
			.describedAs("A File reference to the directory to create must not be null")
			.isNotNull();

		assertThat(directory.isDirectory() || directory.mkdirs())
			.describedAs(String.format("Failed to create directory [%s]", directory))
			.isTrue();

		if (isDeleteDirectoryOnExit()) {
			directory.deleteOnExit();
		}

		return directory;
	}

	protected static boolean isDeleteDirectoryOnExit() {
		return !System.getProperties().containsKey(DIRECTORY_DELETE_ON_EXIT_PROPERTY)
			|| Boolean.getBoolean(DIRECTORY_DELETE_ON_EXIT_PROPERTY);
	}

	protected boolean isQueryDebuggingEnabled() {
		return DEBUG_GEMFIRE_QUERIES || withQueryDebugging();
	}

	@SuppressWarnings("rawtypes")
	protected static @NonNull String getClassNameAsPath(@NonNull Class type) {
		return type.getName().replaceAll("\\.", "/");
	}

	protected static @NonNull String getClassNameAsPath(@NonNull Object obj) {
		return getClassNameAsPath(obj.getClass());
	}

	@SuppressWarnings("rawtypes")
	protected static @NonNull String getPackageNameAsPath(@NonNull Class type) {
		return type.getPackage().getName().replaceAll("\\.", "/");
	}

	protected static @NonNull String getPackageNameAsPath(@NonNull Object obj) {
		return getPackageNameAsPath(obj.getClass());
	}

	@SuppressWarnings("rawtypes")
	protected static @NonNull String getContextXmlFileLocation(@NonNull Class type) {
		return getClassNameAsPath(type).concat("-context.xml");
	}

	@SuppressWarnings("rawtypes")
	protected static @NonNull String getServerContextXmlFileLocation(@NonNull Class type) {
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

	protected static boolean waitOn(@NonNull Condition condition) {
		return waitOn(condition, DEFAULT_WAIT_DURATION);
	}

	protected static boolean waitOn(@NonNull Condition condition, long duration) {
		return waitOn(condition, duration, DEFAULT_WAIT_INTERVAL);
	}

	@SuppressWarnings("all")
	protected static boolean waitOn(@NonNull Condition condition, long duration, long interval) {

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

	protected void usingDeleteDirectoryOnExit(boolean delete) {
		System.setProperty(DIRECTORY_DELETE_ON_EXIT_PROPERTY, String.valueOf(delete));
	}

	protected boolean withQueryDebugging() {
		return DEFAULT_DEBUG_GEMFIRE_QUERIES;
	}

	@FunctionalInterface
	protected interface Condition {
		boolean evaluate();
	}

	protected static abstract class AbstractApplicationEventPublisherCacheLifecycleListenerAdapter
			implements ApplicationEventPublisherAware, CacheLifecycleListener {

		private ApplicationEventPublisher applicationEventPublisher;

		/**
		 * @inheritDoc
		 */
		@Override
		public void setApplicationEventPublisher(@Nullable ApplicationEventPublisher applicationEventPublisher) {
			this.applicationEventPublisher = applicationEventPublisher;
		}

		protected Optional<ApplicationEventPublisher> getApplicationEventPublisher() {
			return Optional.ofNullable(this.applicationEventPublisher);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public void cacheCreated(InternalCache cache) {
			getApplicationEventPublisher().ifPresent(eventPublisher ->
				eventPublisher.publishEvent(new CacheCreatedEvent(cache)));
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public void cacheClosed(InternalCache cache) {
			getApplicationEventPublisher().ifPresent(eventPublisher ->
				eventPublisher.publishEvent(new CacheClosedEvent(cache)));
		}
	}

	public static final class TestContextCacheLifecycleListenerAdapter
			extends AbstractApplicationEventPublisherCacheLifecycleListenerAdapter {

		private static final AtomicReference<TestContextCacheLifecycleListenerAdapter> INSTANCE =
			new AtomicReference<>(null);

		public static TestContextCacheLifecycleListenerAdapter getInstance() {
			return INSTANCE.updateAndGet(instance -> instance != null ? instance
				: newTestContextCacheLifecycleListenerAdapter());
		}

		private static TestContextCacheLifecycleListenerAdapter newTestContextCacheLifecycleListenerAdapter() {
			return registerCacheLifecycleListener(new TestContextCacheLifecycleListenerAdapter());
		}

		private static @NonNull <T extends CacheLifecycleListener> T registerCacheLifecycleListener(@NonNull T listener) {
			GemFireCacheImpl.addCacheLifecycleListener(listener);
			return listener;
		}

		private final Map<GemFireCache, Object> cacheInstances = Collections.synchronizedMap(new WeakHashMap<>());

		private TestContextCacheLifecycleListenerAdapter() { }

		public boolean isClosed(@Nullable GemFireCache cache) {
			return cache == null || (cache.isClosed() && isCacheClosed(cache));
		}

		private boolean isCacheClosed(@Nullable GemFireCache cache) {
			return !isOpen(cache);
		}

		public boolean isOpen(@Nullable GemFireCache cache) {
			return this.cacheInstances.containsKey(cache);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public void cacheCreated(@NonNull InternalCache cache) {

			if (cache != null) {
				this.cacheInstances.put(cache, this);
				super.cacheCreated(cache);
			}
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public void cacheClosed(@NonNull InternalCache cache) {

			if (cache != null) {
				this.cacheInstances.remove(cache, this);
				super.cacheClosed(cache);
			}
		}
	}

	protected static class AbstractCacheEvent extends ApplicationEvent {

		protected static <T> T requireNonNull(@NonNull T target, String message) {
			Assert.notNull(target, message);
			return target;
		}

		protected AbstractCacheEvent(@NonNull GemFireCache cache) {
			super(requireNonNull(cache, "GemFireCache must not be null"));
		}

		public @NonNull GemFireCache getCache() {
			return (GemFireCache) getSource();
		}
	}

	public static class CacheCreatedEvent extends AbstractCacheEvent {

		public CacheCreatedEvent(@NonNull GemFireCache cache) {
			super(cache);
		}
	}

	public static class CacheClosedEvent extends AbstractCacheEvent {

		public CacheClosedEvent(@NonNull GemFireCache cache) {
			super(cache);
		}
	}
}
