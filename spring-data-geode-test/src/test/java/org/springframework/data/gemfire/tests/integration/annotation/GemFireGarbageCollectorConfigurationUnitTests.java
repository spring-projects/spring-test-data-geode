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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.tests.integration.context.event.GemFireGarbageCollectorApplicationListener;
import org.springframework.data.gemfire.tests.support.MapBuilder;
import org.springframework.data.gemfire.tests.util.ReflectionUtils;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.test.context.event.AfterTestExecutionEvent;
import org.springframework.test.context.event.AfterTestMethodEvent;

/**
 * Unit Tests for {@link GemFireGarbageCollectorConfiguration}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.integration.annotation.GemFireGarbageCollectorConfiguration
 * @since 0.0.17
 */
public class GemFireGarbageCollectorConfigurationUnitTests {

	@Test
	public void setImportMetadataParsesConfiguration() {

		Map<String, Object> annotationAttributes = MapBuilder.<String, Object>newMapBuilder()
			.put("collectOnEvents", new Class[] { AfterTestMethodEvent.class, AfterTestExecutionEvent.class })
			.put("tryCleanDiskStoreFiles", true)
			.build();

		AnnotationMetadata mockAnnotationMetadata = mock(AnnotationMetadata.class);

		doReturn(true).when(mockAnnotationMetadata)
			.hasAnnotation(EnableGemFireGarbageCollector.class.getName());
		doReturn(annotationAttributes).when(mockAnnotationMetadata)
			.getAnnotationAttributes(EnableGemFireGarbageCollector.class.getName());

		GemFireGarbageCollectorConfiguration configuration = new GemFireGarbageCollectorConfiguration();

		assertThat(configuration.getGemFireGarbageCollectorEventTypes()).containsExactly(AfterTestClassEvent.class);
		assertThat(configuration.isTryCleanDiskStoreFiles()).isFalse();

		configuration.setImportMetadata(mockAnnotationMetadata);

		assertThat(configuration.getGemFireGarbageCollectorEventTypes())
			.containsExactly(AfterTestMethodEvent.class, AfterTestExecutionEvent.class);
		assertThat(configuration.isTryCleanDiskStoreFiles()).isTrue();

		verify(mockAnnotationMetadata, times(1))
			.hasAnnotation(eq(EnableGemFireGarbageCollector.class.getName()));
		verify(mockAnnotationMetadata, times(1))
			.getAnnotationAttributes(eq(EnableGemFireGarbageCollector.class.getName()));
	}

	@Test
	public void createsGemFireGarbageCollectorApplicationListenerWithDefaultConfiguration()
			throws NoSuchFieldException {

		GemFireGarbageCollectorConfiguration configuration = new GemFireGarbageCollectorConfiguration();

		assertThat(configuration.getGemFireGarbageCollectorEventTypes()).containsExactly(AfterTestClassEvent.class);
		assertThat(configuration.isTryCleanDiskStoreFiles()).isFalse();

		GemFireGarbageCollectorApplicationListener listener =
			(GemFireGarbageCollectorApplicationListener) configuration.gemfireGarbageCollectorApplicationListener();

		assertThat(ReflectionUtils.<Set<Class<?>>>getFieldValue(listener, "gemfireGarbageCollectorEventTypes"))
			.containsExactly(AfterTestClassEvent.class);

		assertThat(ReflectionUtils.<Boolean>getFieldValue(listener, "tryCleanDiskStoreFilesEnabled"))
			.isFalse();
	}

	@Test
	public void createsGemFireGarbageCollectorApplicationListenerWithCustomConfiguration()
			throws NoSuchFieldException {

		GemFireGarbageCollectorConfiguration configuration = spy(new GemFireGarbageCollectorConfiguration());

		doReturn(new Class<?>[] { AfterTestMethodEvent.class, AfterTestExecutionEvent.class })
			.when(configuration).getGemFireGarbageCollectorEventTypes();
		doReturn(true).when(configuration).isTryCleanDiskStoreFiles();

		assertThat(configuration.getGemFireGarbageCollectorEventTypes())
			.containsExactly(AfterTestMethodEvent.class, AfterTestExecutionEvent.class);
		assertThat(configuration.isTryCleanDiskStoreFiles()).isTrue();

		GemFireGarbageCollectorApplicationListener listener =
			(GemFireGarbageCollectorApplicationListener) configuration.gemfireGarbageCollectorApplicationListener();

		assertThat(ReflectionUtils.<Set<Class<?>>>getFieldValue(listener, "gemfireGarbageCollectorEventTypes"))
			.containsExactlyInAnyOrder(AfterTestMethodEvent.class, AfterTestExecutionEvent.class);

		assertThat(ReflectionUtils.<Boolean>getFieldValue(listener, "tryCleanDiskStoreFilesEnabled"))
			.isTrue();
	}
}
