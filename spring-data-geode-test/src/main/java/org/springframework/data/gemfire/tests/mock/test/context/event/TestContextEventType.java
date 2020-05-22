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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.test.context.event.AfterTestExecutionEvent;
import org.springframework.test.context.event.AfterTestMethodEvent;
import org.springframework.test.context.event.BeforeTestClassEvent;
import org.springframework.test.context.event.BeforeTestExecutionEvent;
import org.springframework.test.context.event.BeforeTestMethodEvent;
import org.springframework.test.context.event.PrepareTestInstanceEvent;
import org.springframework.test.context.event.TestContextEvent;
import org.springframework.util.Assert;

/**
 * An enumeration of {@link TestContextEvent} types in the Spring {@link TestContext} test framework.
 *
 * @author John Blum
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.event.TestContextEvent
 * @since 0.0.16
 */
@SuppressWarnings("unused")
public enum TestContextEventType {

	BEFORE_TEST_CLASS(BeforeTestClassEvent.class),
	PREPARE_TEST_INSTANCE(PrepareTestInstanceEvent.class),
	BEFORE_TEST_METHOD(BeforeTestMethodEvent.class),
	BEFORE_TEST_EXECUTION(BeforeTestExecutionEvent.class),
	AFTER_TEST_EXECUTION(AfterTestExecutionEvent.class),
	AFTER_TEST_METHOD(AfterTestMethodEvent.class),
	AFTER_TEST_CLASS(AfterTestClassEvent.class);


	public static @Nullable TestContextEventType from(@Nullable TestContextEvent event) {

		for (TestContextEventType eventType : values()) {
			if (eventType.getTestContextEventType().isInstance(event)) {
				return eventType;
			}
		}

		return null;
	}

	private final Class<? extends TestContextEvent> eventType;

	/**
	 * Constructs a new instance of the {@link TestContextEventType} enumeration creating a new enumerated value
	 * initialized with a {@link TestContextEvent} {@link Class type}.
	 *
	 * @param eventType {@link TestContextEvent} {@link Class type} on which this enumerated type is based.
	 * @throws IllegalArgumentException if the {@link TestContextEvent} {@link Class type} is {@literal null}.
	 * @see org.springframework.test.context.event.TestContextEvent
	 * @see java.lang.Class
	 */
	TestContextEventType(Class<? extends TestContextEvent> eventType) {

		Assert.notNull(eventType,
			"The Class type of the TestContextEvent for this enumerated value must not be null");

		this.eventType = eventType;
	}

	/**
	 * Returns the {@link TestContextEvent} {@link Class type} on which this enumerated type is based.
	 *
	 * @return the {@link TestContextEvent} {@link Class type} on which this enumerated type is based.
	 * @see org.springframework.test.context.event.TestContextEvent
	 * @see java.lang.Class
	 */
	public @NonNull Class<? extends TestContextEvent> getTestContextEventType() {
		return eventType;
	}
}
