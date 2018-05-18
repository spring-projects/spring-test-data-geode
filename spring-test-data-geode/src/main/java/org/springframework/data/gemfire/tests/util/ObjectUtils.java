package org.springframework.data.gemfire.tests.util;

/**
 * {@link ObjectUtils} is a utility class for working with {@link Object objects}.
 *
 * @author John Blum
 * @see java.lang.Object
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class ObjectUtils {

	public static <T> T doSafeOperation(ExceptionThrowingOperation<T> operation) {
		return doSafeOperation(operation, null);
	}

	public static <T> T doSafeOperation(ExceptionThrowingOperation<T> operation, T defaultValue) {

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
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}

	public interface ExceptionThrowingOperation<T> {
		T doExceptionThrowingOperation() throws Exception;
	}
}
