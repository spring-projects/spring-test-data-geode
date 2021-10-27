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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringProperties;
import org.springframework.lang.NonNull;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.util.StringUtils;

/**
 * {@link ConfigurableCacheAwareContextLoaderDelegate} is a Spring {@link TestContext} framework
 * {@link DefaultCacheAwareContextLoaderDelegate} class implementation and extension that enables the configuration of
 * {@link TestContext} caching via {@link SpringProperties} and Java {@link System#getProperties() System properties}
 * configuration.
 *
 * @author John Blum
 * @see java.lang.System
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.SpringProperties
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

	public ConfigurableCacheAwareContextLoaderDelegate() { }

	public ConfigurableCacheAwareContextLoaderDelegate(@NonNull ContextCache contextCache) {
		super(contextCache);
	}

	protected boolean isSpringTestContextCacheEnabled() {
		return springTestContextCacheEnabled.updateAndGet(cacheEnabled -> cacheEnabled != null ? cacheEnabled
			: getSpringTestContextCacheEnabledResolver().apply(DEFAULT_SPRING_TEST_CONTEXT_CACHE_ENABLED));
	}

	@SuppressWarnings("all")
	protected Function<Boolean, Boolean> getSpringTestContextCacheEnabledResolver() {

		return defaultCacheEnabled -> {

			String cacheEnabledProperty = SpringProperties.getProperty(SPRING_TEST_CONTEXT_CACHE_ENABLED_PROPERTY);

			boolean resolvedCacheEnabled = !StringUtils.hasText(cacheEnabledProperty) && defaultCacheEnabled;

			return resolvedCacheEnabled;
		};
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public @NonNull ApplicationContext loadContext(@NonNull MergedContextConfiguration mergedContextConfiguration) {

		try {
			return isSpringTestContextCacheEnabled()
				? super.loadContext(mergedContextConfiguration)
				: loadContextInternal(mergedContextConfiguration);
		}
		catch (Exception cause) {
			throw newIllegalStateException(cause, "Failed to load ApplicationContext for configuration [%s]",
				mergedContextConfiguration);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isContextLoaded(@NonNull MergedContextConfiguration mergedContextConfiguration) {
		return isSpringTestContextCacheEnabled() && super.isContextLoaded(mergedContextConfiguration);
	}
}
