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

package org.springframework.data.gemfire.tests.integration;

import static org.springframework.data.gemfire.tests.process.ProcessExecutor.launch;
import static org.springframework.data.gemfire.util.ArrayUtils.asArray;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.server.CacheServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.data.gemfire.tests.util.SocketUtils;

/**
 * The {@link ClientServerIntegrationTestsSupport} class is a abstract base class encapsulating common functionality
 * to support the implementation of GemFire client/server tests.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.lang.Process
 * @see java.net.InetSocketAddress
 * @see java.net.ServerSocket
 * @see java.net.Socket
 * @see java.time.LocalDateTime
 * @see java.util.concurrent.TimeUnit
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.process.ProcessExecutor
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public abstract class ClientServerIntegrationTestsSupport extends IntegrationTestsSupport {

	public static final String DEFAULT_HOSTNAME = "localhost";
	public static final String GEMFIRE_CACHE_SERVER_PORT_PROPERTY = "spring.data.gemfire.cache.server.port";
	public static final String GEMFIRE_LOCALHOST_PORT = "localhost[%d]";
	public static final String GEMFIRE_POOL_SERVERS_PROPERTY = "spring.data.gemfire.pool.servers";

	protected static final String DEBUG_ENDPOINT = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005";
	protected static final String DEBUGGING_ENABLED_PROPERTY = "spring.data.gemfire.test.debugging.enabled";
	protected static final String PROCESS_RUN_MANUAL_PROPERTY = "spring.data.gemfire.test.process.run-manual";

	protected static int findAvailablePort() throws IOException {

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(0));

			return serverSocket.getLocalPort();
		}
		finally {
			SocketUtils.close(serverSocket);
		}
	}

	protected static int intValue(Number number) {
		return number != null ? number.intValue() : 0;
	}

	protected static boolean isProcessRunAuto() {
		return !isProcessRunManual();
	}

	protected static boolean isProcessRunManual() {
		return Boolean.getBoolean(PROCESS_RUN_MANUAL_PROPERTY);
	}

	protected static ProcessWrapper run(Class<?> type, String... arguments) throws IOException {
		return run(createDirectory(asDirectoryName(type)), type, arguments);
	}

	protected static ProcessWrapper run(File workingDirectory, Class<?> type, String... arguments) throws IOException {
		return isProcessRunAuto() ? launch(createDirectory(workingDirectory), type, arguments) : null;
	}

	protected static ProcessWrapper run(String classpath, Class<?> type, String... arguments) throws IOException {
		return run(createDirectory(asDirectoryName(type)), classpath, type, arguments);
	}

	protected static ProcessWrapper run(File workingDirectory, String classpath, Class<?> type, String... arguments)
			throws IOException {

		return isProcessRunAuto() ? launch(createDirectory(workingDirectory), classpath, type, arguments) : null;
	}

	protected static AnnotationConfigApplicationContext runSpringApplication(Class<?> annotatedClass, String... args) {
		return runSpringApplication(asArray(annotatedClass), args);
	}

	protected static AnnotationConfigApplicationContext runSpringApplication(Class<?>[] annotatedClasses,
			String... args) {

		AnnotationConfigApplicationContext applicationContext =
			new AnnotationConfigApplicationContext(annotatedClasses);

		applicationContext.registerShutdownHook();

		return applicationContext;
	}

	protected static boolean stop(ProcessWrapper process) {
		return stop(process, DEFAULT_WAIT_DURATION);
	}

	protected static boolean stop(ProcessWrapper process, long duration) {

		return Optional.ofNullable(process)
			.map(it -> {

				it.stop(duration);

				if (it.isNotRunning() && isDeleteDirectoryOnExit()) {
					FileSystemUtils.deleteRecursive(it.getWorkingDirectory());
				}

				return it.isRunning();

			})
			.orElse(true);
	}

	protected static boolean waitForCacheServerToStart(CacheServer cacheServer) {
		return waitForServerToStart(cacheServer.getBindAddress(), cacheServer.getPort(), DEFAULT_WAIT_DURATION);
	}

	protected static boolean waitForCacheServerToStart(CacheServer cacheServer, long duration) {
		return waitForServerToStart(cacheServer.getBindAddress(), cacheServer.getPort(), duration);
	}

	@SuppressWarnings("all")
	protected static boolean waitForServerToStart(String host, int port) {
		return waitForServerToStart(host, port, DEFAULT_WAIT_DURATION);
	}

	protected static boolean waitForServerToStart(String host, int port, long duration) {

		AtomicBoolean connected = new AtomicBoolean(false);

		return waitOn(() -> {

			Socket socket = null;

			try {
				if (!connected.get()) {
					socket = new Socket(host, port);
					connected.set(true);
				}
			}
			catch (IOException ignore) { }
			finally {
				SocketUtils.close(socket);
			}

			return connected.get();

		}, duration);
	}

	protected static int waitForProcessToStop(Process process, File directory) {
		return waitForProcessToStop(process, directory, DEFAULT_WAIT_DURATION);
	}

	protected static int waitForProcessToStop(Process process, File directory, long duration) {

		long timeout = System.currentTimeMillis() + duration;

		try {
			while (process.isAlive() && System.currentTimeMillis() < timeout) {
				if (process.waitFor(DEFAULT_WAIT_INTERVAL, TimeUnit.MILLISECONDS)) {
					return process.exitValue();
				}
			}
		}
		catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}

		return process.isAlive() ? -1 : process.exitValue();
	}
}
