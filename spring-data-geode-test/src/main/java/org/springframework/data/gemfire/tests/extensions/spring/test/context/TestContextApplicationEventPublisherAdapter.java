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
package org.springframework.data.gemfire.tests.extensions.spring.test.context;

import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestContext;
import org.springframework.util.Assert;

/**
 * {@literal Adapter} used to adapt the {@link TestContext} API as an {@link ApplicationEventPublisher}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationEventPublisher
 * @see org.springframework.test.context.TestContext
 * @since 0.0.23
 */
@SuppressWarnings("unused")
public class TestContextApplicationEventPublisherAdapter implements ApplicationEventPublisher {

	protected static @NonNull TestContextApplicationEventPublisherAdapter from(@NonNull TestContext testContext) {
		return new TestContextApplicationEventPublisherAdapter(testContext);
	}

	private final TestContext testContext;

	/**
	 * Constructs a new instance of {@link TestContextApplicationEventPublisherAdapter} initialized with the given,
	 * required {@link TestContext}.
	 *
	 * @param testContext Spring {@link TestContext} to be adapted as a Spring {@link ApplicationEventPublisher}.
	 * @throws IllegalArgumentException if {@link TestContext} is {@literal null}.
	 * @see org.springframework.test.context.TestContext
	 */
	protected TestContextApplicationEventPublisherAdapter(@NonNull TestContext testContext) {
		Assert.notNull(testContext, "TestContext must not be null");
		this.testContext = testContext;
	}

	/**
	 * Returns a reference to the configured {@link TestContext}.
	 *
	 * @return a reference to the configured {@link TestContext}; never {@literal null}.
	 * @see org.springframework.test.context.TestContext
	 */
	protected @NonNull TestContext getTestContext() {
		return this.testContext;
	}

	/**
	 * Returns an {@link Optional} reference to the {@link ApplicationEventPublisher} if available.
	 *
	 * The {@link ApplicationEventPublisher}, or rather Spring {@link ApplicationContext}, is resolved from
	 * the {@link TestContext} depending on whether the {@link ApplicationContext} has been initialized yet.
	 *
	 * @return an {@link Optional} reference to the {@link ApplicationEventPublisher} if available.
	 * @see org.springframework.context.ApplicationEventPublisher
	 * @see java.util.Optional
	 * @see #getTestContext()
	 */
	protected Optional<ApplicationEventPublisher> getApplicationEventPublisher() {

		return Optional.ofNullable(getTestContext())
			.filter(TestContext::hasApplicationContext)
			.map(TestContext::getApplicationContext);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void publishEvent(@NonNull Object event) {
		getApplicationEventPublisher().ifPresent(applicationEventPublisher ->
			applicationEventPublisher.publishEvent(event));
	}
}
