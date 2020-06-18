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

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.data.gemfire.tests.integration.context.event.GemFireResourceCollectorApplicationListener;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.test.context.event.AfterTestClassEvent;

/**
 * Spring {@link Configuration} class used to register beans that collect resources and other garbage irresponsibly
 * left behind by Apache Geode when its processes shutdown, particularly in a test context in order to avoid conflicts
 * and interference between test runs.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.data.gemfire.tests.integration.context.event.GemFireResourceCollectorApplicationListener
 * @since 0.0.17
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireResourceCollectorConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	public static final boolean DEFAULT_CLEAN_DISK_STORE_FILES = false;

	private boolean tryCleanDiskStoreFiles = DEFAULT_CLEAN_DISK_STORE_FILES;

	@SuppressWarnings("unchecked")
	private Class<? extends ApplicationEvent>[] collectorEventTypes = new Class[] { AfterTestClassEvent.class };

	@Override
	@SuppressWarnings("unchecked")
	public void setImportMetadata(@NonNull AnnotationMetadata importMetadata) {

		Optional.of(importMetadata)
			.filter(this::isAnnotationPresent)
			.map(this::getAnnotationAttributes)
			.ifPresent(enableGemFireResourceCollectorAttributes -> {

				this.collectorEventTypes = (Class<? extends ApplicationEvent>[])
					enableGemFireResourceCollectorAttributes.getClassArray("collectOnEvents");

				this.tryCleanDiskStoreFiles =
					enableGemFireResourceCollectorAttributes.getBoolean("tryCleanDiskStoreFiles");
			});
	}

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return EnableGemFireResourceCollector.class;
	}

	@SuppressWarnings("unchecked")
	protected @NonNull Class<? extends ApplicationEvent>[] getConfiguredCollectorEventTypes() {
		return ArrayUtils.nullSafeArray(this.collectorEventTypes, Class.class);
	}

	protected boolean isTryCleanDiskStoreFiles() {
		return this.tryCleanDiskStoreFiles;
	}

	@Bean
	ApplicationListener<ApplicationEvent> gemfireResourceCollectorApplicationListener() {
		return GemFireResourceCollectorApplicationListener.create(getConfiguredCollectorEventTypes())
			.tryCleanDiskStoreFiles(isTryCleanDiskStoreFiles());
	}
}
