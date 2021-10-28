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
package org.springframework.data.gemfire.tests.process;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.data.gemfire.tests.util.FileSystemUtils;

/**
 * The {@link ProcessRunner} interface is a {@link FunctionalInterface} encapsulating the contract, logic and strategy
 * for running (executing) an Operating System (OS) [JVM] {@link Process}.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.lang.Process
 * @see org.springframework.data.gemfire.tests.process.ProcessExecutor
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @since 1.0.0
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ProcessRunner {

	/**
	 * Gets the {@link File working directory} in which the {@link Process} will run.
	 *
	 * Default to the user directory as defined by {@code System.getProperty("user.dir")}.
	 *
	 * @return {@link File} representing the {@literal working directory} in which the {@link Process} will run.
	 * @see java.io.File
	 */
	default File getWorkingDirectory() {
		return FileSystemUtils.WORKING_DIRECTORY;
	}

	/**
	 * Runs a [JVM] {@link Process} defined by the this {@literal run} method.
	 *
	 * @param arguments array of {@link String} arguments passed to the program at runtime.
	 * @return the {@link ProcessWrapper} representing the [JVM] {@link Process}.
	 * @throws IOException if the {@link Process} could not be run.
	 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
	 */
	ProcessWrapper run(String... arguments) throws IOException;

	/**
	 * Runs a [JVM] {@link Process} defined by the this {@literal run} method.
	 *
	 * @param arguments {@link List} of {@link String} arguments passed to the program at runtime.
	 * @return the {@link ProcessWrapper} representing the [JVM] {@link Process}.
	 * @throws IOException if the {@link Process} could not be run.
	 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
	 * @see #run(String...)
	 */
	default ProcessWrapper run(List<String> arguments) throws IOException {
		return run(arguments.toArray(new String[0]));
	}
}
