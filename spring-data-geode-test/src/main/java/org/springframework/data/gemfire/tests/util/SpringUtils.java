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
package org.springframework.data.gemfire.tests.util;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

/**
 * Abstract utility class containing functions to process Spring Framework objects and components (beans).
 *
 * @author John Blum
 * @see java.util.function.Function
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.data.gemfire.util.SpringUtils
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SpringUtils extends org.springframework.data.gemfire.util.SpringUtils {

	public static final Function<ConfigurableApplicationContext, Boolean> APPLICATION_CONTEXT_CLOSING_FUNCTION =
		applicationContext -> {

			if (applicationContext != null) {
				applicationContext.close();
				return true;
			}

			return false;
		};

	/**
	 * Closes the given optionally provided {@link ApplicationContext}.
	 *
	 * @param applicationContext {@link ApplicationContext} to close.
	 * @return a boolean value indicating whether the {@link ApplicationContext} could be closed successfully or not.
	 * @see org.springframework.context.ApplicationContext
	 */
	public static boolean closeApplicationContext(@Nullable ApplicationContext applicationContext) {

		return Optional.ofNullable(applicationContext)
			.filter(ConfigurableApplicationContext.class::isInstance)
			.map(ConfigurableApplicationContext.class::cast)
			.map(APPLICATION_CONTEXT_CLOSING_FUNCTION)
			.orElse(false);
	}
}
