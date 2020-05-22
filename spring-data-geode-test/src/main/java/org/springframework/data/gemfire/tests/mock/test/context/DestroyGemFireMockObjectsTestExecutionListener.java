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
package org.springframework.data.gemfire.tests.mock.test.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.event.TestContextEvent;

/**
 * A Spring {@link TestExecutionListener} implementation listening for and handling different {@link TestContextEvent}
 * in order to destroy all GemFire/Geode {@link Object Mock Objects} at the appropriate test lifecycle event.
 *
 * By default, event handling for {@link TestContextEventType#AFTER_TEST_CLASS} is enabled and GemFire/Geode
 * {@link Object Mock Objects} will be destroyed when this event occurs.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @see org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.TestExecutionListener
 * @see org.springframework.test.context.event.TestContextEvent
 * @since 0.0.16
 */
public class DestroyGemFireMockObjectsTestExecutionListener implements TestExecutionListener {

	private static final AtomicReference<DestroyGemFireMockObjectsTestExecutionListener> instance =
		new AtomicReference<>(null);

	/**
	 * Returns an {@link Optional} reference to the constructed {@link DestroyGemFireMockObjectsTestExecutionListener}
	 * created by the Spring {@link TestContext} test framework on test execution.
	 *
	 * @return an {@link Optional} reference to a {@link DestroyGemFireMockObjectsTestExecutionListener} instance.
	 * @see java.util.Optional
	 */
	public static Optional<DestroyGemFireMockObjectsTestExecutionListener> getInstance() {
		return Optional.ofNullable(instance.get());
	}

	private final Set<TestContextEventType> destroyOnEventTypes =
		Collections.synchronizedSet(new HashSet<>(TestContextEventType.values().length));

	/**
	 * Constructs a new instance of the {@link DestroyGemFireMockObjectsTestExecutionListener}.
	 */
	public DestroyGemFireMockObjectsTestExecutionListener() {
		instance.set(this);
		enableDestroyOnEventType(TestContextEventType.AFTER_TEST_CLASS);
	}

	/**
	 * Disables event handling and destruction of GemFire/Geode {@link Object Mock Objects} for
	 * the given {@link TestContextEventType}.
	 *
	 * @param eventType {@link TestContextEventType} to disable event handling for.
	 * @return a boolean value indicating whether event handling for the given {@link TestContextEventType}
	 * was successfully disabled.
	 * @see org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType
	 * @see #enableDestroyOnEventType(TestContextEventType)
	 */
	public boolean disableDestroyOnEventType(@Nullable TestContextEventType eventType) {

		return eventType != null
			&& (this.destroyOnEventTypes.remove(eventType) || !this.destroyOnEventTypes.contains(eventType));
	}

	/**
	 * Enables event handling and destruction of GemFire/Geode {@link Object Mock Objects} for
	 * the given {@link TestContextEventType}.
	 *
	 * @param eventType {@link TestContextEventType} to enable event handling for.
	 * @return a boolean value indicating whether event handling for the given {@link TestContextEventType}
	 * was successfully enabled.
	 * @see org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType
	 * @see #disableDestroyOnEventType(TestContextEventType)
	 */
	public boolean enableDestroyOnEventType(@Nullable TestContextEventType eventType) {

		return eventType != null
			&& (this.destroyOnEventTypes.add(eventType) || this.destroyOnEventTypes.contains(eventType));
	}

	/**
	 * Determines whether event handling for the given {@link TestContextEventType} is enabled.
	 *
	 * @param eventType {{@link TestContextEventType} to evaluate.
	 * @return a boolean value indicating whether event handling for the given {@link TestContextEventType} is enabled.
	 * @see org.springframework.data.gemfire.tests.mock.test.context.event.TestContextEventType
	 */
	protected boolean isDestroyOnEventTypeEnabled(@Nullable TestContextEventType eventType) {
		return this.destroyOnEventTypes.contains(eventType);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void beforeTestClass(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_CLASS)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void prepareTestInstance(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.PREPARE_TEST_INSTANCE)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void beforeTestMethod(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_METHOD)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void beforeTestExecution(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_EXECUTION)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void afterTestExecution(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void afterTestMethod(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_METHOD)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void afterTestClass(TestContext testContext) {

		if (isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_CLASS)) {
			destroyGemFireMockObjects();
		}
	}

	/**
	 * Destroys all GemFire/Geode {@link Object Mock Objects}.
	 *
	 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport#destroy()
	 */
	protected void destroyGemFireMockObjects() {
		GemFireMockObjectsSupport.destroy();
	}
}
