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
package org.springframework.data.gemfire.tests.process;

/**
 * The {@link JavaProcessRunner} interface is a {@link FunctionalInterface} and extension of the {@link ProcessRunner}
 * interface to encapsulate the runtime parameters for running (executing) a Java/JVM {@link Process}.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.tests.process.ProcessRunner
 * @since 1.0.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface JavaProcessRunner extends ProcessRunner {

	/**
	 * Gets the {@link String classpath} used to run the Java/JVM {@link Process}.
	 *
	 * Defaults to the parent JVM {@link Process} classpath as defined by {@code System.getProperty("java.class.path}).
	 *
	 * @return the {@link String classpath} used to run the Java/JVM {@link Process}.
	 */
	default String getClassPath() {
		return System.getProperty("java.class.path");
	}

	/**
	 * Gets the Java {@link Class} with the main method to run.
	 *
	 * @return the Java {@link Class} with the main method to run.
	 * @see java.lang.Class
	 */
	default Class<?> getMainClass() {
		return null;
	}
}
