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
package org.springframework.data.gemfire.tests.integration.annotation;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.geode.cache.DiskStore;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.AfterTestClassEvent;

/**
 * The {@link EnableGemFireResourceCollector} annotation enables the cleanup of resources (e.g. files) and other garbage
 * left behind by Apache Geode (or VMware GemFire) after the GemFire/Geode process shuts down, especially in a test
 * context to avoid conflicts between test runs.
 *
 * @author John Blum
 * @see java.lang.annotation.Documented
 * @see java.lang.annotation.Inherited
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 * @see org.apache.geode.cache.GemFireCache
 * @see org.springframework.context.annotation.Import
 * @since 0.0.17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(GemFireResourceCollectorConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableGemFireResourceCollector {

	/**
	 * Determines the Spring {@link ApplicationEvent ApplicationEvents} that trigger the framework to cleanup after
	 * Apache Geode / VMware GemFire given the junk it leaves behind after a process (e.g. CacheServer, Locator,
	 * Manager, etc) terminates.
	 *
	 * @return an array of {@link ApplicationEvent ApplicationEvents} that trigger the GemFire/Geode resource
	 * and garbage collection algorithm.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Class
	 */
	Class<? extends ApplicationEvent>[] collectOnEvents() default { AfterTestClassEvent.class };

	/**
	 * Tries to cleanup all the {@link File Files} left behind by GemFire/Geode {@link DiskStore DiskStores}.
	 *
	 * @return a boolean value indicating whether the GemFire Resource Collector should cleanup all {@link File Files}
	 * left behind by GemFire/Geode {@link DiskStore DiskStores}, whether for persistence or overflow;
	 * defaults to {@literal false}.
	 */
	boolean tryCleanDiskStoreFiles() default GemFireResourceCollectorConfiguration.DEFAULT_CLEAN_DISK_STORE_FILES;

}
