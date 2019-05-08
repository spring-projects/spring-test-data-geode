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
package org.springframework.data.gemfire.tests.objects.geode.security;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.security.AuthenticationFailedException;

/**
 * Test {@link org.apache.geode.security.SecurityManager} implementation used for testing purposes (only).
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TestSecurityManager implements org.apache.geode.security.SecurityManager {

	private static final AtomicReference<TestSecurityManager> instance = new AtomicReference<>(null);

	public static TestSecurityManager getInstance() {

		return Optional.ofNullable(instance.get())
			.orElseThrow(() -> newIllegalStateException("No TestSecurityManager was initialized"));
	}

	public TestSecurityManager() {
		instance.compareAndSet(null, this);
	}

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException {
		throw new UnsupportedOperationException("Not Implemented");
	}
}
