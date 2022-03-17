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

package org.springframework.data.gemfire.tests.mock.support;

import java.lang.reflect.Method;

/**
 * The {@link MockObjectInvocationException} class is an extension of {@link MockObjectsException} to categorize
 * problems with {@link Method method} invocations on {@link Object Mock Objects}.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.tests.mock.support.MockObjectsException
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public class MockObjectInvocationException extends MockObjectsException {

	/**
	 * Constructs a new instance of the {@link MockObjectInvocationException} class with no message or underlying cause.
	 */
	public MockObjectInvocationException() {
	}

	/**
	 * Constructs a new instance of the {@link MockObjectInvocationException} class initialized with
	 * the given {@link String message} describing the problem.
	 *
	 * @param message {@link String} describing the problem.
	 * @see String
	 */
	public MockObjectInvocationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of the {@link MockObjectInvocationException} class initialized with
	 * the given {@link Throwable cause} of the underlying problem.
	 *
	 * @param cause {@link Throwable} object containing the cause of this exception.
	 * @see Throwable
	 */
	public MockObjectInvocationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new instance of the {@link MockObjectInvocationException} class initialized with
	 * the given {@link String message} describing the underlying problem as well as the {@link Throwable cause}
	 * of the underlying problem.
	 *
	 * @param message {@link String} describing the problem.
	 * @param cause {@link Throwable} object containing the cause of this exception.
	 * @see Throwable
	 * @see String
	 */
	public MockObjectInvocationException(String message, Throwable cause) {
		super(message, cause);
	}
}
