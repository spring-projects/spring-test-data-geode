/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package build

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class GemFireServerPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		project.tasks.create('gemfireServer', GemFireServerTask)

		project.tasks.integrationTest.doLast {
			println 'Stopping Apache Geode Server...'
			project.tasks.gemfireServer.process?.destroy()
		}

		if (project.tasks.findByName("bootRun") != null) {
			project.tasks.integrationTest.dependsOn project.tasks.gemfireServer
			project.tasks.integrationTest.doFirst {
				systemProperties['spring.data.gemfire.cache.server.port'] = project.tasks.gemfireServer.port
				systemProperties['spring.data.gemfire.pool.servers'] = "localhost[${project.tasks.gemfireServer.port}]"
			}
		}

		project.tasks.findByName("prepareAppServerForIntegrationTests")?.configure { task ->
			task.dependsOn project.tasks.gemfireServer
			task.doFirst {
				project.gretty {
					jvmArgs = [
						"-Dspring.data.gemfire.cache.server.port=${project.tasks.gemfireServer.port}",
						"-Dspring.data.gemfire.pool.servers=localhost[${project.tasks.gemfireServer.port}]"
					]
				}
			}
		}
	}

	static int availablePort() {
		new ServerSocket(0).withCloseable { socket ->
			socket.localPort
		}
	}

	static class GemFireServerTask extends DefaultTask {

		@Internal
		def mainClassName = "sample.server.GemFireServer"

		@Internal
		def port

		@Internal
		def process

		@Input
		boolean debug

		@TaskAction
		def greet() {

			port = availablePort()
			println "Starting Apache Geode Server on port [$port]..."

			def out = debug ? System.err : new StringBuilder()
			def err = debug ? System.err : new StringBuilder()

			String classpath = project.sourceSets.main.runtimeClasspath.collect { it }.join(File.pathSeparator)
			String gemfireLogLevel = System.getProperty('spring.data.gemfire.cache.log-level', 'warning')
			String javaHome = System.getProperty("java.home");

			javaHome = javaHome == null || javaHome.isEmpty() ? System.getenv("JAVA_HOME") : javaHome;
			javaHome = javaHome.endsWith(File.separator) ? javaHome : javaHome.concat(File.separator);

			String javaCommand = javaHome + "bin" + File.separator + "java";

			String[] commandLine = [
				javaCommand, '-server', '-ea', '-classpath', classpath,
				//"-Dgemfire.log-file=gemfire-server.log",
				"-Dgemfire.log-level=${gemfireLogLevel}",
				"-Dspring.data.gemfire.cache.server.port=${port}",
				mainClassName
			]

			//println commandLine

			process = commandLine.execute()

			if (project.tasks.findByName("appRun") != null) {
				project.tasks.appRun.ext.process = process
			}

			process.consumeProcessOutput(out, err)
		}
	}
}
