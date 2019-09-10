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
package org.springframework.data.gemfire.tests.logging.slf4j.logback;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit Tests for {@link TestAppender}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.slf4j.Logger
 * @see org.slf4j.LoggerFactory
 * @see org.springframework.data.gemfire.tests.logging.slf4j.logback.TestAppender
 * @since 0.0.5.RELEASE
 */
public class TestAppenderUnitTests {

	private static final Logger logger = LoggerFactory.getLogger(TestAppenderUnitTests.class);

	@Test
	public void logEventsAppendedCorrectly() {

		TestAppender testAppender = TestAppender.getInstance();

		assertThat(testAppender).isNotNull();
		assertThat(testAppender.lastLogMessage()).isNull();

		LoggableObject object = new LoggableObject();

		object.logAnError();
		object.logAnInfoMessage();
		object.logAWarning();

		assertThat(testAppender.lastLogMessage()).isEqualTo("WARN TEST");
		assertThat(testAppender.lastLogMessage()).isEqualTo("ERROR TEST");
		assertThat(testAppender.lastLogMessage()).isNull();
	}

	static class LoggableObject {

		public void logAnError() {
			logger.error("ERROR TEST");
		}

		public void logAnInfoMessage() {
			logger.info("INFO TEST");
		}

		public void logAWarning() {
			logger.warn("WARN TEST");
		}
	}
}
