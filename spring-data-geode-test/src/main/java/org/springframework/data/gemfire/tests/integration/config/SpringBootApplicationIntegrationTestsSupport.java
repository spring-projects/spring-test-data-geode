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

package org.springframework.data.gemfire.tests.integration.config;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;

/**
 * The {@link SpringBootApplicationIntegrationTestsSupport} class is an extension of Spring Test
 * for Apache Geode/Pivotal GemFire's {@link SpringApplicationContextIntegrationTestsSupport} class
 * used to construct a new Spring {@link ConfigurableApplicationContext} using Spring Boot's
 * {@link SpringApplicationBuilder} class.
 *
 * @author John Blum
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SpringBootApplicationIntegrationTestsSupport
		extends SpringApplicationContextIntegrationTestsSupport {

	protected static final String[] EMPTY_STRING_ARRAY = {};

	protected String[] getArguments() {
		return EMPTY_STRING_ARRAY;
	}

	protected WebApplicationType getWebApplicationType() {
		return WebApplicationType.NONE;
	}

	@Override
	protected ConfigurableApplicationContext newApplicationContext(Class<?>... annotatedClasses) {

		return setApplicationContext(new SpringApplicationBuilder(annotatedClasses)
			.initializers(this::processBeforeRefresh)
			.web(getWebApplicationType())
			.build()
			.run(getArguments()));
	}
}
