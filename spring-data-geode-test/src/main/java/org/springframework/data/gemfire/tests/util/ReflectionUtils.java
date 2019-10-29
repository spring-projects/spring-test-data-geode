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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.util.Assert;

/**
 * Utility {@link Class} for performing reflective and introspective Java {@link Object} operations.
 *
 * @author John Blum
 * @see java.lang.Class
 * @see java.lang.Object
 * @see java.lang.reflect.Constructor
 * @see java.lang.reflect.Field
 * @see java.lang.reflect.Method
 * @see org.springframework.util.ReflectionUtils
 * @since 0.0.10
 */
@SuppressWarnings("unused")
public abstract class ReflectionUtils extends org.springframework.util.ReflectionUtils {

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object target, String fieldName) throws NoSuchFieldException {

		Assert.notNull(target, "Target object must not be null");
		Assert.hasText(fieldName, String.format("Field name [%s] must be specified", fieldName));

		Field field = findField(target.getClass(), fieldName);

		return Optional.ofNullable(field)
			.map(ReflectionUtils::makeAccessibleReturnField)
			.map(it -> (T) getField(it, target))
			.orElseThrow(() ->
				new NoSuchFieldException(String.format("Field with name [%s] was not found on Object of type [%s]",
					fieldName, target.getClass().getName())));
	}

	public static Constructor makeAccessibleReturnConstructor(Constructor constructor) {
		makeAccessible(constructor);
		return constructor;
	}

	public static Field makeAccessibleReturnField(Field field) {
		makeAccessible(field);
		return field;
	}

	public static Method makeAccessibleReturnMethod(Method method) {
		makeAccessible(method);
		return method;
	}
}
