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
package org.springframework.data.gemfire.tests.mock.test.context.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.test.context.event.AfterTestExecutionEvent;
import org.springframework.test.context.event.AfterTestMethodEvent;
import org.springframework.test.context.event.BeforeTestClassEvent;
import org.springframework.test.context.event.BeforeTestExecutionEvent;
import org.springframework.test.context.event.BeforeTestMethodEvent;
import org.springframework.test.context.event.PrepareTestInstanceEvent;
import org.springframework.test.context.event.TestContextEvent;

/**
 * Unit Tests for {@link TestContextEventType}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType
 * @see org.springframework.test.context.event.TestContextEvent
 * @since 0.0.16
 */
public class TestContextEventTypeUnitTest {

	@Test
	public void fromTestContextEventToEnum() {

		List<TestContextEvent> events = Arrays.asList(
			mock(BeforeTestClassEvent.class),
			mock(PrepareTestInstanceEvent.class),
			mock(BeforeTestMethodEvent.class),
			mock(BeforeTestExecutionEvent.class),
			mock(AfterTestExecutionEvent.class),
			mock(AfterTestMethodEvent.class),
			mock(AfterTestClassEvent.class)
		);

		events.forEach(event -> {

			TestContextEventType eventType = TestContextEventType.from(event);

			assertThat(eventType).isNotNull();
			assertThat(eventType.getTestContextEventType()).isAssignableFrom(event.getClass());
		});
	}

	@Test
	public void fromTestContextEventReturnsNull() {
		assertThat(TestContextEventType.from(mock(TestContextEvent.class))).isNull();
	}

	@Test
	public void fromNullIsNullSafeAndReturnsNull() {
		assertThat(TestContextEventType.from(null)).isNull();
	}
}
