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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Unit Tests for {@link GemFireMockObjectsConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.context.event.ContextClosedEvent
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.tests.mock.annotation.GemFireMockObjectsConfiguration
 * @since 0.0.16
 */
public class GemFireMockObjectsConfigurationUnitTests {

	@Test
	public void setImportMetadataConfiguresSingletonCacheAndSuppressContextClosedEvents() {

		GemFireMockObjectsConfiguration configuration = new GemFireMockObjectsConfiguration();

		assertThat(configuration.isSuppressOnContextClosedEvent()).isFalse();
		assertThat(configuration.isUseSingletonCacheConfigured()).isFalse();

		Map<String, Object> enableGemFireMockObjectsAttributes = new HashMap<>();

		enableGemFireMockObjectsAttributes.put("suppressOnContextClosedEventHandler", true);
		enableGemFireMockObjectsAttributes.put("useSingletonCache", true);

		AnnotationMetadata mockAnnotationMetadata = mock(AnnotationMetadata.class);

		doReturn(true).when(mockAnnotationMetadata)
			.hasAnnotation(eq(EnableGemFireMockObjects.class.getName()));

		doReturn(enableGemFireMockObjectsAttributes).when(mockAnnotationMetadata)
			.getAnnotationAttributes(eq(EnableGemFireMockObjects.class.getName()));

		configuration.setImportMetadata(mockAnnotationMetadata);

		assertThat(configuration.isSuppressOnContextClosedEvent()).isTrue();
		assertThat(configuration.isUseSingletonCacheConfigured()).isTrue();

		verify(mockAnnotationMetadata, times(1))
			.hasAnnotation(eq(EnableGemFireMockObjects.class.getName()));
		verify(mockAnnotationMetadata, times(1))
			.getAnnotationAttributes(eq(EnableGemFireMockObjects.class.getName()));
	}

	@Test
	public void destroysGemFireMockObjectsWhenSuppressOnContextClosedEventIsFalse() {

		ContextClosedEvent mockEvent = mock(ContextClosedEvent.class);

		GemFireMockObjectsConfiguration configuration = spy(new GemFireMockObjectsConfiguration());

		doReturn(false).when(configuration).isSuppressOnContextClosedEvent();
		doNothing().when(configuration).destroyGemFireMockObjects();

		configuration.onApplicationEvent(mockEvent);

		verify(configuration, times(1)).destroyGemFireMockObjects();
		verify(configuration, times(1)).releaseMockObjectResources(eq(mockEvent));
	}

	@Test
	public void doesNotDestroyGemFireMockObjectsWhenSuppressOnContextClosedEventIsTrue() {

		ContextClosedEvent mockEvent = mock(ContextClosedEvent.class);

		GemFireMockObjectsConfiguration configuration = spy(new GemFireMockObjectsConfiguration());

		doReturn(true).when(configuration).isSuppressOnContextClosedEvent();
		doNothing().when(configuration).destroyGemFireMockObjects();

		configuration.onApplicationEvent(mockEvent);

		verify(configuration, never()).destroyGemFireMockObjects();
		verify(configuration, times(1)).releaseMockObjectResources(eq(mockEvent));
	}
}
