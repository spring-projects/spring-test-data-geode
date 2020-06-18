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
package org.springframework.data.gemfire.tests.mock.annotation;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.data.gemfire.tests.mock.beans.factory.config.GemFireMockObjectsBeanPostProcessor;
import org.springframework.data.gemfire.tests.mock.context.event.DestroyGemFireMockObjectsApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.test.context.event.AfterTestClassEvent;

/**
 * The {@link GemFireMockObjectsConfiguration} class is a Spring {@link Configuration @Configuration} class
 * containing bean definitions to configure GemFire Object mocking.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @see org.springframework.data.gemfire.tests.mock.beans.factory.config.GemFireMockObjectsBeanPostProcessor
 * @see org.springframework.data.gemfire.tests.mock.context.event.DestroyGemFireMockObjectsApplicationListener
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireMockObjectsConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	public static final boolean DEFAULT_USE_SINGLETON_CACHE = false;

	private boolean useSingletonCache = DEFAULT_USE_SINGLETON_CACHE;

	@SuppressWarnings("unchecked")
	private Class<? extends ApplicationEvent>[] destroyEventTypes = new Class[] { AfterTestClassEvent.class };

	@Override
	@SuppressWarnings("unchecked")
	public void setImportMetadata(@NonNull AnnotationMetadata importingClassMetadata) {

		Optional.of(importingClassMetadata)
			.filter(this::isAnnotationPresent)
			.map(this::getAnnotationAttributes)
			.ifPresent(enableGemFireMockObjectsAttributes -> {

				this.destroyEventTypes = (Class<? extends ApplicationEvent>[])
					enableGemFireMockObjectsAttributes.getClassArray("destroyOnEvents");

				this.useSingletonCache =
					enableGemFireMockObjectsAttributes.getBoolean("useSingletonCache");
			});
	}

	protected @NonNull Class<? extends Annotation> getAnnotationType() {
		return EnableGemFireMockObjects.class;
	}

	protected Class<? extends ApplicationEvent>[] getConfiguredDestroyEventTypes() {
		return this.destroyEventTypes;
	}

	protected boolean isUseSingletonCacheConfigured() {
		return this.useSingletonCache;
	}

	@Bean
	public BeanPostProcessor gemfireMockObjectsBeanPostProcessor() {
		return GemFireMockObjectsBeanPostProcessor.newInstance(isUseSingletonCacheConfigured());
	}

	@Bean
	public ApplicationListener<ApplicationEvent> destroyGemFireMockObjectsApplicationListener() {
		return DestroyGemFireMockObjectsApplicationListener.newInstance(getConfiguredDestroyEventTypes());
	}
}
