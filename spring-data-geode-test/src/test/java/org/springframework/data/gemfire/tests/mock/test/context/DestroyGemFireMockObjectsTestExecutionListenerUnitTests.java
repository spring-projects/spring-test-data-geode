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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.data.gemfire.tests.extensions.spring.test.context.event.TestContextEventType;
import org.springframework.test.context.TestContext;

/**
 * Unit Tests for {@link DestroyGemFireMockObjectsTestExecutionListener}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.mockito.junit.MockitoJUnitRunner
 * @see org.springframework.data.gemfire.tests.extensions.spring.test.context.event.TestContextEventType
 * @see org.springframework.data.gemfire.tests.mock.test.context.DestroyGemFireMockObjectsTestExecutionListener
 * @since 0.0.16
 */
@RunWith(MockitoJUnitRunner.class)
public class DestroyGemFireMockObjectsTestExecutionListenerUnitTests {

	@Mock
	private TestContext mockTestContext;

	@Test
	public void enableDestroyOnValidTestContextEventTypesReturnsTrue() {

		DestroyGemFireMockObjectsTestExecutionListener listener = new DestroyGemFireMockObjectsTestExecutionListener();

		assertThat(listener).isNotNull();
		assertThat(DestroyGemFireMockObjectsTestExecutionListener.getInstance().orElse(null)).isSameAs(listener);
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_CLASS)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isFalse();
		assertThat(listener.enableDestroyOnEventType(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.enableDestroyOnEventType(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_METHOD)).isFalse();
		assertThat(listener.enableDestroyOnEventType(TestContextEventType.BEFORE_TEST_METHOD)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_METHOD)).isTrue();
	}

	@Test
	public void enableDestroyWithNullIsNullSafeReturnsFalse() {

		DestroyGemFireMockObjectsTestExecutionListener listener = new DestroyGemFireMockObjectsTestExecutionListener();

		assertThat(listener).isNotNull();
		assertThat(DestroyGemFireMockObjectsTestExecutionListener.getInstance().orElse(null)).isSameAs(listener);
		assertThat(listener.enableDestroyOnEventType(null)).isFalse();
	}

	@Test
	public void disableDestroyOnValidTestContextEventTypesReturnsTrue() {

		DestroyGemFireMockObjectsTestExecutionListener listener = new DestroyGemFireMockObjectsTestExecutionListener();

		assertThat(listener).isNotNull();
		assertThat(DestroyGemFireMockObjectsTestExecutionListener.getInstance().orElse(null)).isSameAs(listener);
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_CLASS)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isFalse();
		assertThat(listener.disableDestroyOnEventType(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isFalse();
		assertThat(listener.enableDestroyOnEventType(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();
		assertThat(listener.disableDestroyOnEventType(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();
		assertThat(listener.disableDestroyOnEventType(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.PREPARE_TEST_INSTANCE)).isFalse();
	}

	@Test
	public void disableDestroyWithNullIsNullSafeReturnsFalse() {

		DestroyGemFireMockObjectsTestExecutionListener listener = new DestroyGemFireMockObjectsTestExecutionListener();

		assertThat(listener).isNotNull();
		assertThat(DestroyGemFireMockObjectsTestExecutionListener.getInstance().orElse(null)).isSameAs(listener);
		assertThat(listener.disableDestroyOnEventType(null)).isFalse();
	}

	@Test
	public void isDestroyOnEventTypeEnableIsNullSafe() {

		DestroyGemFireMockObjectsTestExecutionListener listener = new DestroyGemFireMockObjectsTestExecutionListener();

		assertThat(listener).isNotNull();
		assertThat(DestroyGemFireMockObjectsTestExecutionListener.getInstance().orElse(null)).isSameAs(listener);
		assertThat(listener.isDestroyOnEventTypeEnabled(null)).isFalse();
	}

	@Test
	public void beforeTestClassIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_CLASS)).isFalse();

		listener.beforeTestClass(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void beforeTestClassDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.BEFORE_TEST_CLASS)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_CLASS)).isTrue();

		listener.beforeTestClass(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void prepareTestInstanceIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.PREPARE_TEST_INSTANCE)).isFalse();

		listener.prepareTestInstance(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void prepareTestInstanceDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.PREPARE_TEST_INSTANCE)).isTrue();

		listener.prepareTestInstance(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void beforeTestMethodIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_METHOD)).isFalse();

		listener.beforeTestMethod(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void beforeTestMethodDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.BEFORE_TEST_METHOD)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_METHOD)).isTrue();

		listener.beforeTestMethod(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void beforeTestExecutionIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_EXECUTION)).isFalse();

		listener.beforeTestExecution(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void beforeTestExecutionDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.BEFORE_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.BEFORE_TEST_EXECUTION)).isTrue();

		listener.beforeTestExecution(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestExecutionIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isFalse();

		listener.afterTestExecution(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestExecutionDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_EXECUTION)).isTrue();

		listener.afterTestExecution(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestMethodIsDisabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_METHOD)).isFalse();

		listener.afterTestMethod(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestMethodDestroysGemFireMockObjectsWhenEnabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.enableDestroyOnEventType(TestContextEventType.AFTER_TEST_METHOD)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_METHOD)).isTrue();

		listener.afterTestMethod(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestClassIsEnabledByDefault() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		doNothing().when(listener).destroyGemFireMockObjects();

		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_CLASS)).isTrue();

		listener.afterTestClass(this.mockTestContext);

		verify(listener, times(1)).destroyGemFireMockObjects();
	}

	@Test
	public void afterTestClassWillNotDestroyGemFireMockObjectsWhenDisabled() {

		DestroyGemFireMockObjectsTestExecutionListener listener =
			spy(new DestroyGemFireMockObjectsTestExecutionListener());

		assertThat(listener.disableDestroyOnEventType(TestContextEventType.AFTER_TEST_CLASS)).isTrue();
		assertThat(listener.isDestroyOnEventTypeEnabled(TestContextEventType.AFTER_TEST_CLASS)).isFalse();

		listener.afterTestClass(this.mockTestContext);

		verify(listener, never()).destroyGemFireMockObjects();
	}
}
