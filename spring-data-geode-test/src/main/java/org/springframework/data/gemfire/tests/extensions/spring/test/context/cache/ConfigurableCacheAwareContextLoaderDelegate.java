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
package org.springframework.data.gemfire.tests.extensions.spring.test.context.cache;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringProperties;
import org.springframework.data.gemfire.tests.util.SpringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link ConfigurableCacheAwareContextLoaderDelegate} is a Spring {@link TestContext} framework
 * {@link DefaultCacheAwareContextLoaderDelegate} class extension and implementation used to enable
 * the configuration of {@link TestContext} caching via {@link SpringProperties}
 * or Java {@link System#getProperties() System properties}.
 *
 * @author John Blum
 * @see java.lang.System#getProperties()
 * @see java.util.concurrent.atomic.AtomicReference
 * @see java.util.function.Function
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.SpringProperties
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.MergedContextConfiguration
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.cache.ContextCache
 * @see org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
 * @since 0.0.27
 */
@SuppressWarnings("unused")
public class ConfigurableCacheAwareContextLoaderDelegate extends DefaultCacheAwareContextLoaderDelegate {

	protected static final boolean DEFAULT_SPRING_TEST_CONTEXT_CACHE_ENABLED = true;

	protected static final String SPRING_TEST_CONTEXT_CACHE_ENABLED_PROPERTY = "spring.test.context.cache.enabled";

	private final AtomicReference<Boolean> springTestContextCacheEnabled = new AtomicReference<>(null);

	private final AtomicReference<MergedContextConfigurationAndApplicationContextPair> applicationContextReference =
		new AtomicReference<>();

	/**
	 * Constructs a new instance of {@link ConfigurableCacheAwareContextLoaderDelegate} initialized with
	 * a default {@link ContextCache}.
	 *
	 * @see org.springframework.test.context.cache.ContextCache
	 */
	public ConfigurableCacheAwareContextLoaderDelegate() { }

	/**
	 * Constructs a new instance of {@link ConfigurableCacheAwareContextLoaderDelegate} initialized with
	 * the given, required {@link ContextCache}.
	 *
	 * @param contextCache {@link ContextCache} used to cache the {@link ApplicationContext} by configuration
	 * created by the Spring {@link TestContext} framework for testing purposes.
	 * @throws IllegalArgumentException if {@link ContextCache} is {@literal null}.
	 * @see org.springframework.test.context.cache.ContextCache
	 */
	public ConfigurableCacheAwareContextLoaderDelegate(@NonNull ContextCache contextCache) {
		super(contextCache);
	}

	protected boolean isSpringTestContextCacheEnabled() {
		return springTestContextCacheEnabled.updateAndGet(cacheEnabled -> cacheEnabled != null ? cacheEnabled
			: getSpringTestContextCacheEnabledResolvingFunction().apply(DEFAULT_SPRING_TEST_CONTEXT_CACHE_ENABLED));
	}

	@SuppressWarnings("all")
	protected Function<Boolean, Boolean> getSpringTestContextCacheEnabledResolvingFunction() {

		return defaultCacheEnabled -> {

			String cacheEnabledProperty = SpringProperties.getProperty(SPRING_TEST_CONTEXT_CACHE_ENABLED_PROPERTY);

			boolean resolvedCacheEnabled =
				(!StringUtils.hasText(cacheEnabledProperty) || Boolean.parseBoolean(cacheEnabledProperty))
					&& defaultCacheEnabled;

			return resolvedCacheEnabled;
		};
	}

	protected boolean isApplicationContextActive() {

		return Optional.ofNullable(applicationContextReference.get())
			.filter(MergedContextConfigurationAndApplicationContextPair::isActive)
			.isPresent();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("all")
	public boolean isContextLoaded(@NonNull MergedContextConfiguration mergedContextConfiguration) {

		boolean contextLoaded = (isSpringTestContextCacheEnabled() && super.isContextLoaded(mergedContextConfiguration))
			|| isApplicationContextActive();

		return contextLoaded;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public @NonNull ApplicationContext loadContext(@NonNull MergedContextConfiguration mergedContextConfiguration) {

		return isSpringTestContextCacheEnabled()
			? super.loadContext(mergedContextConfiguration)
			: loadContext(mergedContextConfiguration, this::loadContextInternalWithExceptionHandling);
	}

	private @NonNull ApplicationContext loadContext(@NonNull MergedContextConfiguration mergedContextConfiguration,
			@NonNull Function<MergedContextConfiguration, ApplicationContext> contextLoaderFunction) {

		MergedContextConfigurationAndApplicationContextPair pair = this.applicationContextReference
			.updateAndGet(ref -> ref != null
				? ref.update(mergedContextConfiguration, contextLoaderFunction)
				: MergedContextConfigurationAndApplicationContextPair
					.from(mergedContextConfiguration, contextLoaderFunction.apply(mergedContextConfiguration)));

		return pair.getApplicationContext();
	}

	private @NonNull ApplicationContext loadContextInternalWithExceptionHandling(
			@NonNull MergedContextConfiguration mergedContextConfiguration) {

		try {
			return loadContextInternal(mergedContextConfiguration);
		}
		catch (Exception cause) {
			throw newIllegalStateException(cause, "Failed to load ApplicationContext for context configuration [%s]",
				mergedContextConfiguration);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void closeContext(@NonNull MergedContextConfiguration mergedContextConfiguration,
			DirtiesContext.HierarchyMode hierarchyMode) {

		if (isSpringTestContextCacheEnabled()) {
			super.closeContext(mergedContextConfiguration, hierarchyMode);
		}
		else {
			closeApplicationContext(mergedContextConfiguration);
		}
	}

	private boolean closeApplicationContext(@NonNull MergedContextConfiguration contextConfiguration) {

		return Optional.ofNullable(applicationContextReference.get())
			.map(pair -> pair.closeApplicationContextIfMatch(contextConfiguration))
			.orElse(false);
	}

	protected static class MergedContextConfigurationAndApplicationContextPair {

		protected static @NonNull MergedContextConfigurationAndApplicationContextPair from(
				@NonNull MergedContextConfiguration contextConfiguration, @NonNull ApplicationContext applicationContext) {

			return new MergedContextConfigurationAndApplicationContextPair(contextConfiguration, applicationContext);
		}

		private final ApplicationContext applicationContext;

		private final MergedContextConfiguration mergedContextConfiguration;

		protected MergedContextConfigurationAndApplicationContextPair(
			@NonNull MergedContextConfiguration mergedContextConfiguration,
			@NonNull ApplicationContext applicationContext) {

			Assert.notNull(mergedContextConfiguration, "MergedContextConfiguration must not be null");
			Assert.notNull(applicationContext, "ApplicationContext must not be null");

			this.mergedContextConfiguration = mergedContextConfiguration;
			this.applicationContext = applicationContext;
		}

		protected @NonNull ApplicationContext getApplicationContext() {
			return this.applicationContext;
		}

		protected @NonNull MergedContextConfiguration getMergedContextConfiguration() {
			return this.mergedContextConfiguration;
		}

		protected boolean isActive() {

			return Optional.ofNullable(getApplicationContext())
				.filter(SpringUtils::isApplicationContextActive)
				.isPresent();
		}

		protected boolean isMatch(@Nullable MergedContextConfiguration mergedContextConfiguration) {
			return getMergedContextConfiguration().equals(mergedContextConfiguration);
		}

		protected boolean isNotMatch(@Nullable MergedContextConfiguration mergedContextConfiguration) {
			return !isMatch(mergedContextConfiguration);
		}

		protected boolean isUpdatable(@Nullable MergedContextConfiguration mergedContextConfiguration) {

			return Objects.nonNull(mergedContextConfiguration)
				&& closeApplicationContextIfNotMatch(mergedContextConfiguration)
				&& !isActive();
		}

		protected boolean closeApplicationContextIfMatch(
				@Nullable MergedContextConfiguration mergedContextConfiguration) {

			return isMatch(mergedContextConfiguration)
				&& SpringUtils.closeApplicationContext(getApplicationContext());
		}

		protected boolean closeApplicationContextIfNotMatch(
				@Nullable MergedContextConfiguration mergedContextConfiguration) {

			return isNotMatch(mergedContextConfiguration)
				&& SpringUtils.closeApplicationContext(getApplicationContext());
		}

		protected @NonNull MergedContextConfigurationAndApplicationContextPair update(
				@Nullable MergedContextConfiguration mergedContextConfiguration,
				@NonNull Function<MergedContextConfiguration, ApplicationContext> contextLoaderFunction) {

			return isUpdatable(mergedContextConfiguration)
				? from(mergedContextConfiguration, contextLoaderFunction.apply(mergedContextConfiguration))
				: this;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			if (!(obj instanceof MergedContextConfigurationAndApplicationContextPair)) {
				return false;
			}

			MergedContextConfigurationAndApplicationContextPair that =
				(MergedContextConfigurationAndApplicationContextPair) obj;

			return ObjectUtils.nullSafeEquals(this.getMergedContextConfiguration(), that.getMergedContextConfiguration())
				&& ObjectUtils.nullSafeEquals(this.getApplicationContext(), that.getApplicationContext());
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public int hashCode() {

			int hashValue = 17;

			hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getMergedContextConfiguration());
			hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getApplicationContext());

			return hashValue;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public String toString() {
			return String.format("MergedContextConfiguration [%1$s] for ApplicationContext [%2$s]",
				getMergedContextConfiguration(), getApplicationContext());
		}
	}
}
