/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package org.springframework.data.gemfire.tests.integration;

import java.util.Optional;

import org.junit.After;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The {@link SpringApplicationContextIntegrationTestsSupport} class is an extension of {@link IntegrationTestsSupport}
 * for writing Integration Tests involving a Spring {@link ApplicationContext}.
 *
 * This class contains functionality common to all Integration Tests involving a Spring {@link ApplicationContext}
 * and can be extended to create, acquire a reference and close the {@link ApplicationContext}
 * on test class completion, properly.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SpringApplicationContextIntegrationTestsSupport extends IntegrationTestsSupport {

	private volatile ConfigurableApplicationContext applicationContext;

	@After
	public void closeApplicationContext() {
		Optional.ofNullable(this.applicationContext).ifPresent(ConfigurableApplicationContext::close);
	}

	protected ConfigurableApplicationContext newApplicationContext(Class<?>... annotatedClasses) {

		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

		applicationContext.register(annotatedClasses);
		applicationContext.registerShutdownHook();
		processBeforeRefresh(applicationContext);
		applicationContext.refresh();

		this.applicationContext = applicationContext;

		return applicationContext;
	}

	protected ConfigurableApplicationContext processBeforeRefresh(ConfigurableApplicationContext applicationContext) {
		return applicationContext;
	}

	protected <T extends ConfigurableApplicationContext> T setApplicationContext(T applicationContext) {

		this.applicationContext = applicationContext;

		return applicationContext;
	}

	@SuppressWarnings("unchecked")
	protected <T extends ConfigurableApplicationContext> T getApplicationContext() {
		return (T) this.applicationContext;
	}

	protected <T> T getBean(Class<T> requiredType) {
		return getApplicationContext().getBean(requiredType);
	}

	protected <T> T getBean(String beanName, Class<T> requiredType) {
		return getApplicationContext().getBean(beanName, requiredType);
	}
}
