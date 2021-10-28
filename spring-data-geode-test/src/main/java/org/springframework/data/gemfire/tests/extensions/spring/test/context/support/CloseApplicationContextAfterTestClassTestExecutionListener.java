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
package org.springframework.data.gemfire.tests.extensions.spring.test.context.support;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringProperties;
import org.springframework.lang.NonNull;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Spring {@link TestContext} framework {@link org.springframework.test.context.TestExecutionListener} used to
 * close the {@link org.springframework.context.ApplicationContext} after test class execution.
 *
 * This {@link TestExecutionListener} is configurable via {@link SpringProperties}
 * or by declaring Java {@link System#getProperties() System properties}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.core.SpringProperties
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.TestExecutionListener
 * @see org.springframework.test.context.support.AbstractTestExecutionListener
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class CloseApplicationContextAfterTestClassTestExecutionListener extends AbstractTestExecutionListener {

	protected static final boolean DEFAULT_SPRING_TEST_CONTEXT_CLOSED = false;

	protected static final String SPRING_TEST_CONTEXT_CLOSE_PROPERTY = "spring.test.context.close";

	protected static final DirtiesContext.HierarchyMode DEFAULT_HIERARCHY_MODE =
		DirtiesContext.HierarchyMode.CURRENT_LEVEL;

	private final AtomicReference<Boolean> springTestContextCloseEnabled = new AtomicReference<>();

	/**
	 * @inheritDoc
	 */
	@Override
	public int getOrder() {
		return 100_000;
	}

	protected boolean isSpringTestContextCloseEnabled() {
		return this.springTestContextCloseEnabled.updateAndGet(closeEnabled -> closeEnabled != null ? closeEnabled
			: getSpringTestContextCloseEnabledResolvingFunction().apply(DEFAULT_SPRING_TEST_CONTEXT_CLOSED));
	}

	@SuppressWarnings("all")
	protected Function<Boolean, Boolean> getSpringTestContextCloseEnabledResolvingFunction() {

		return defaultCloseEnabled -> {

			String closeEnabledProperty = SpringProperties.getProperty(SPRING_TEST_CONTEXT_CLOSE_PROPERTY);

			boolean resolvedCloseEnabled = Boolean.parseBoolean(closeEnabledProperty) || defaultCloseEnabled;

			return resolvedCloseEnabled;
		};
	}

	/**
	 * Closes the {@link ApplicationContext} associated with the given, required {@link TestContext}
	 * after the test class instance executes.
	 *
	 * This operation is implemented by marking the {@link ApplicationContext} as dirty.
	 *
	 * @param testContext Spring {@link TestContext}
	 * @throws Exception if the {@link ApplicationContext} close operation fails.
	 * @see org.springframework.test.context.TestContext#markApplicationContextDirty(DirtiesContext.HierarchyMode)
	 * @see #isSpringTestContextCloseEnabled()
	 */
	@Override
	public void afterTestClass(@NonNull TestContext testContext) throws Exception {

		if (isSpringTestContextCloseEnabled()) {
			super.afterTestClass(testContext);
			testContext.markApplicationContextDirty(DEFAULT_HIERARCHY_MODE);
		}
	}
}
