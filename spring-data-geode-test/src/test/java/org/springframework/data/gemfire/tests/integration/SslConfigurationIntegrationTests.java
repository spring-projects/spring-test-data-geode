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
package org.springframework.data.gemfire.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.Test;

import org.apache.geode.internal.net.SSLConfigurationFactory;

import org.springframework.util.ReflectionUtils;

/**
 * Integration Tests asserting that {@link IntegrationTestsSupport} clears the SSL configuration of Apache Geode
 * between test case runs.
 *
 * @author John Blum
 * @see org.apache.geode.internal.net.SSLConfigurationFactory
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @since 0.0.8
 */
public class SslConfigurationIntegrationTests {

	private SSLConfigurationFactory getInstance() {

		Method getInstance = ReflectionUtils.findMethod(SSLConfigurationFactory.class, "getInstance");

		return Optional.ofNullable(getInstance)
			.map(method -> {

				ReflectionUtils.makeAccessible(method);

				return (SSLConfigurationFactory) ReflectionUtils.invokeMethod(method, null);
			})
			.orElse(null);
	}

	@Test
	public void getInstanceReturnsSameReferenceBeforeCloseThenReturnsDifferentReferenceAfterClose() {

		SSLConfigurationFactory sslConfigurationFactoryOne = getInstance();

		assertThat(sslConfigurationFactoryOne).isNotNull();
		assertThat(getInstance()).isSameAs(sslConfigurationFactoryOne);

		IntegrationTestsSupport.closeAnySslConfiguration();

		SSLConfigurationFactory sslConfigurationFactoryTwo = getInstance();

		assertThat(sslConfigurationFactoryTwo).isNotNull();
		assertThat(sslConfigurationFactoryTwo).isNotSameAs(sslConfigurationFactoryOne);
	}
}
