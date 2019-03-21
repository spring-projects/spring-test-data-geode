/*
 *  Copyright 2018 the original author or authors.
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

/**
 * {@link ObjectUtils} is a utility class for performing different opeations on {@link Object objects}.
 *
 * @author John Blum
 * @see java.lang.Object
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class ObjectUtils {

	public static <T> T doOperationSafely(ExceptionThrowingOperation<T> operation) {
		return doOperationSafely(operation, null);
	}

	public static <T> T doOperationSafely(ExceptionThrowingOperation<T> operation, T defaultValue) {

		try {
			return operation.doExceptionThrowingOperation();
		}
		catch (Exception ignore) {
			return defaultValue;
		}
	}

	public static <T> T rethrowAsRuntimeException(ExceptionThrowingOperation<T> operation) {

		try {
			return operation.doExceptionThrowingOperation();
		}
		catch (RuntimeException cause) {
			throw cause;
		}
		catch (Throwable cause) {
			throw new RuntimeException(cause);
		}
	}

	@FunctionalInterface
	public interface ExceptionThrowingOperation<T> {
		T doExceptionThrowingOperation() throws Exception;
	}
}
