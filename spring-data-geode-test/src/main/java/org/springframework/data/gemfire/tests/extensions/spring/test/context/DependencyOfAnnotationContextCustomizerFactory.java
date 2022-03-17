/*
 *  Copyright 2017-present the original author or authors.
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

import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.TestContext;

/**
 * Spring {@link ContextCustomizerFactory} implementation to create a {@link DependencyOfAnnotationContextCustomizer}
 * used to customize the Spring {@link ConfigurableApplicationContext} created by
 * the Spring {@link TestContext} framework.
 *
 * @author John Blum
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.tests.extensions.spring.test.context.DependencyOfAnnotationContextCustomizer
 * @see org.springframework.test.context.ContextCustomizer
 * @see org.springframework.test.context.ContextCustomizerFactory
 * @since 0.0.23
 */
public class DependencyOfAnnotationContextCustomizerFactory implements ContextCustomizerFactory {

	/**
	 * @inheritDoc
	 */
	@Override
	public @NonNull ContextCustomizer createContextCustomizer(@NonNull Class<?> testClass,
			@NonNull List<ContextConfigurationAttributes> configAttributes) {

		return new DependencyOfAnnotationContextCustomizer();
	}
}
