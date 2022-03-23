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

package org.springframework.data.gemfire.tests.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The {@link StackTraceUtils} class is a utility class for working with stack trace frames (elements)
 * of the current {@link Thread}.
 *
 * @author John Blum
 * @see StackTraceElement
 * @see Thread
 * @see org.springframework.data.gemfire.tests.util.ThreadUtils
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public abstract class StackTraceUtils extends ThreadUtils {

	private static final AtomicBoolean tracingEnabled = new AtomicBoolean(false);

	public static @NonNull String getUniversalTraceIdentifier() {

		String id = UUID.randomUUID().toString();
		String[] idElements = id.split("-");

		return String.format("%s%s%s", idElements[0].substring(0, 3), idElements[3],
			idElements[idElements.length - 1].substring(idElements[idElements.length - 1].length() - 3));
	}

	public static @NonNull StackTraceElement getCaller() {
		return getCaller(Thread.currentThread());
	}

	public static @NonNull StackTraceElement getCaller(@NonNull Thread thread) {
		return thread.getStackTrace()[2];
	}

	public static @NonNull String getCallerName(@NonNull StackTraceElement element) {
		return String.format("%1$%s.%2$s", element.getClassName(), element.getMethodName());
	}

	public static @NonNull String getCallerSimpleName(@NonNull StackTraceElement element) {

		String resolvedClassName = safeResolveClass(element)
			.map(Class::getSimpleName)
			.orElseGet(() -> {

				String className = element.getClassName();
				int index = element.getClassName().lastIndexOf(".");

				return index > -1 ? className.substring(index) : className;
			});

		return String.format("%1$%s.%2$s", resolvedClassName, element.getMethodName());
	}

	public static @Nullable StackTraceElement getTestCaller() {
		return getTestCaller(Thread.currentThread());
	}

	public static @Nullable StackTraceElement getTestCaller(@NonNull Thread thread) {

		return Arrays.stream(thread.getStackTrace())
			.filter(StackTraceUtils::isTestSuiteClass)
			.filter(StackTraceUtils::isTestCaseMethod)
			.findFirst()
			.orElse(null);
	}

	private static boolean isTestCaseMethod(StackTraceElement element) {

		boolean result = element.getMethodName().toLowerCase().startsWith("test");

		try {
			result |= resolveMethod(element).isAnnotationPresent(org.junit.Test.class);
		}
		catch (ClassNotFoundException | NoSuchMethodException ignore) { }

		return result;
	}

	private static boolean isTestSuiteClass(StackTraceElement element) {

		boolean result = element.getClassName().toLowerCase().endsWith("test");

		try {
			result |= resolveClass(element).isAssignableFrom(junit.framework.TestCase.class);
		}
		catch (ClassNotFoundException ignore) { }

		return result;
	}

	public static @NonNull String getStackTrace() {

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		List<StackTraceElement> resolvedStackTrace = Arrays.stream(stackTrace)
			.filter(stackTraceElementFilter())
			.collect(Collectors.toList());

		StringWriter writer = new StringWriter();

		Throwable throwable = new Throwable("STACK TRACE DUMP");

		throwable.setStackTrace(resolvedStackTrace.toArray(new StackTraceElement[resolvedStackTrace.size()]));
		throwable.printStackTrace(new PrintWriter(writer));

		return writer.toString();
	}

	private static Predicate<StackTraceElement> stackTraceElementFilter() {
		return element -> !StackTraceUtils.class.getName().equals(element.getClassName());
	}

	public static boolean isTracingEnabled() {
		return tracingEnabled.get();
	}

	public static Class<?> resolveClass(StackTraceElement element) throws ClassNotFoundException {
		return Class.forName(element.getClassName());
	}

	public static Optional<Class<?>> safeResolveClass(StackTraceElement element) {

		try {
			return Optional.of(resolveClass(element));
		}
		catch (ClassNotFoundException ignore) {
			return Optional.empty();
		}
	}

	public static Method resolveMethod(StackTraceElement element)
			throws ClassNotFoundException, NoSuchMethodException {

		return resolveClass(element).getMethod(element.getMethodName());
	}

	public static Optional<Method> safeResolveMethod(StackTraceElement element) {

		try {
			return Optional.of(resolveMethod(element));
		}
		catch (ClassNotFoundException | NoSuchMethodException ignore) {
			return Optional.empty();
		}
	}

	public static void whenTracingEnabled(@NonNull Consumer<String> stackTraceConsumer) {

		if (isTracingEnabled()) {
			stackTraceConsumer.accept(getStackTrace());
		}
	}

	public void withTracing() {
		tracingEnabled.set(true);
	}

	public void withoutTracing() {
		tracingEnabled.set(false);
	}
}
