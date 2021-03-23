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
package org.springframework.data.gemfire;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Tests for Spring's {@link TestContext}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 0.0.23
 */
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration
@SuppressWarnings("unused")
public class SpringTestContextIntegrationTests {

	private static final Set<Integer> applicationContextsIdentityHashCodes =
		Collections.synchronizedSet(new HashSet<>());

	@Autowired
	private ApplicationContext applicationContext;

	@AfterClass
	public static void runAfterTestClass() {
		assertThat(applicationContextsIdentityHashCodes).hasSize(1);
	}

	@Before
	public void setup() {
		applicationContextsIdentityHashCodes.add(System.identityHashCode(this.applicationContext));
	}

	@Test
	//@DirtiesContext
	public void testCaseOne() {
		assertThat(this.applicationContext).isNotNull();
	}

	@Test
	public void testCaseTwo() {
		assertThat(this.applicationContext).isNotNull();
	}

	@Test
	public void testCaseThree() {
		assertThat(this.applicationContext).isNotNull();
	}

	@Configuration
	static class TestConfiguration { }

}
