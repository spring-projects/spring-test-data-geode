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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.gemfire.tests.extensions.spring.context.annotation.DependencyOfBeanFactoryPostProcessor;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport.TestContextCacheLifecycleListenerAdapter;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * Spring {@link ContextCustomizer} implementation used to register the {@link ConfigurableApplicationContext}
 * with the {@link TestContextCacheLifecycleListenerAdapter} as an {@link ApplicationEventPublisher}.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.ContextCustomizer
 * @since 0.0.23
 */
public class DependencyOfAnnotationContextCustomizer implements ContextCustomizer {

	/**
	 * @inheritDoc
	 */
	@Override
	public void customizeContext(@NonNull ConfigurableApplicationContext applicationContext,
			@NonNull MergedContextConfiguration mergedConfig) {

		applicationContext.addBeanFactoryPostProcessor(new DependencyOfBeanFactoryPostProcessor());

		TestContextCacheLifecycleListenerAdapter.getInstance().setApplicationEventPublisher(applicationContext);
	}
}
