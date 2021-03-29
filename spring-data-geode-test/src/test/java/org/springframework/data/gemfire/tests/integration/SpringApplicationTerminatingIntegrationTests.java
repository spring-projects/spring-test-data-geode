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
package org.springframework.data.gemfire.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.tests.process.ProcessWrapper;

/**
 * Integration Tests testing and asserting Spring Application termination configuration.
 *
 * @author John Blum
 * @see java.time.Duration
 * @see java.time.Instant
 * @see org.junit.Test
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Profile
 * @see org.springframework.data.gemfire.tests.process.ProcessWrapper
 * @since 0.0.24
 */
@SuppressWarnings("unused")
public class SpringApplicationTerminatingIntegrationTests extends ForkingClientServerIntegrationTestsSupport {

	private static Instant startTime;

	private static ProcessWrapper springApplicationProcess;

	@BeforeClass
	public static void startSpringApplication() throws IOException {

		springApplicationProcess = run(TestSpringApplicationConfiguration.class,
			"-Dspring.profiles.active=SpringApplicationTerminator");

		startTime = Instant.now();
	}

	@AfterClass
	public static void assertSpringApplicationTerminated() {

		Condition springApplicationTerminatedCondition = () -> springApplicationProcess.isNotRunning();

		waitOn(springApplicationTerminatedCondition, Duration.ofSeconds(10L).toMillis());

		assertThat(Duration.between(startTime, Instant.now())).isGreaterThan(Duration.ofSeconds(5));
	}

	@Test
	public void springApplicationIsRunning() {

		assertThat(springApplicationProcess).isNotNull();
		assertThat(springApplicationProcess.isAlive()).isTrue();
		assertThat(springApplicationProcess.isRunning()).isTrue();
	}

	@Configuration
	@Profile("SpringApplicationTerminator")
	@Import(SpringApplicationTerminatorConfiguration.class)
	static class TestSpringApplicationConfiguration {

		public static void main(String[] args) {

			AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

			applicationContext.register(TestSpringApplicationConfiguration.class);
			applicationContext.registerShutdownHook();

			new Scanner(System.in).nextLine();
		}

		@Bean
		SpringApplicationTerminatorConfigurer terminatorConfigurer() {

			return new SpringApplicationTerminatorConfigurer() {

				@Override
				public Duration delay() {
					return Duration.ofSeconds(5);
				}
			};
		}
	}
}
