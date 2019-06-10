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
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.mock.config.GemFireMockObjectsBeanPostProcessor;

/**
 * The {@link GemFireMockObjectsConfiguration} class is a Spring {@link Configuration @Configuration} class
 * containing bean definitions to configure GemFire Object mocking.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.core.annotation.AnnotationAttributes
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @see org.springframework.data.gemfire.tests.mock.config.GemFireMockObjectsBeanPostProcessor
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireMockObjectsConfiguration implements ApplicationListener<ContextClosedEvent>, ImportAware {

	private boolean useSingletonCache = false;

	@Override @SuppressWarnings("all")
	public void setImportMetadata(AnnotationMetadata importingClassMetadata) {

		Optional.of(importingClassMetadata)
			.filter(this::isAnnotationPresent)
			.map(this::getAnnotationAttributes)
			.ifPresent(enableGemFireMockObjectsAttributes ->
				this.useSingletonCache = enableGemFireMockObjectsAttributes.getBoolean("useSingletonCache"));
	}

	private Class<? extends Annotation> getAnnotationType() {
		return EnableGemFireMockObjects.class;
	}

	private boolean isAnnotationPresent(AnnotationMetadata importingClassMetadata) {
		return isAnnotationPresent(importingClassMetadata, getAnnotationType());
	}

	private boolean isAnnotationPresent(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> annotationType) {

		return importingClassMetadata.hasAnnotation(annotationType.getName());
	}

	private AnnotationAttributes getAnnotationAttributes(AnnotationMetadata importingClassMetadata) {
		return getAnnotationAttributes(importingClassMetadata, getAnnotationType());
	}

	private AnnotationAttributes getAnnotationAttributes(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> annotationType) {

		return AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(annotationType.getName()));
	}

	@Bean
	public BeanPostProcessor gemfireMockObjectsBeanPostProcessor() {
		return GemFireMockObjectsBeanPostProcessor.newInstance(this.useSingletonCache);
	}

	@EventListener
	public void releaseMockObjectResources(ContextClosedEvent event) {
		GemFireMockObjectsSupport.destroy();
	}

	@Override @SuppressWarnings("all")
	public void onApplicationEvent(ContextClosedEvent event) {
		releaseMockObjectResources(event);
	}
}
