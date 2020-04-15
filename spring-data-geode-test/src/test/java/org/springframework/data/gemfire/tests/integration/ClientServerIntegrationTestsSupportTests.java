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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Integration Tests for {@link ClientServerIntegrationTestsSupport}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.tests.integration.ClientServerIntegrationTests
 * @since 0.0.14.RELEASE
 */
public class ClientServerIntegrationTestsSupportTests extends ClientServerIntegrationTestsSupport {

	@Test
	public void allocatedPortsAreDifferent() throws IOException {

		int expectedSize = 3;

		Set<Integer> allocatedPorts = new HashSet<>();

		for (int index = 0; index < expectedSize; index++) {
			allocatedPorts.add(findAndReserveAvailablePort());
		}

		assertThat(allocatedPorts).hasSize(expectedSize);
	}
}
