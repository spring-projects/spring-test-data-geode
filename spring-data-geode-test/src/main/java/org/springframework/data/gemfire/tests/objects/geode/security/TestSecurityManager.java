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
package org.springframework.data.gemfire.tests.objects.geode.security;

import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_PASSWORD_PROPERTY;
import static org.springframework.data.gemfire.config.annotation.support.AutoConfiguredAuthenticationInitializer.SECURITY_USERNAME_PROPERTY;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.security.AuthenticationFailedException;
import org.springframework.util.StringUtils;

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

	public static final String SECURITY_USERNAME = "testUser";
	public static final String SECURITY_PASSWORD = "&t35t9@55w0rd!";

	private final ConcurrentMap<String, String> authorizedUsers;

	public static TestSecurityManager getInstance() {

		return Optional.ofNullable(instance.get())
			.orElseThrow(() -> newIllegalStateException("No TestSecurityManager was initialized"));
	}

	public TestSecurityManager() {
		this.authorizedUsers = new ConcurrentHashMap<>();
		this.authorizedUsers.putIfAbsent(SECURITY_USERNAME, SECURITY_PASSWORD);
		instance.compareAndSet(null, this);
	}

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException {
		String username = credentials.getProperty(SECURITY_USERNAME_PROPERTY);
		String password = credentials.getProperty(SECURITY_PASSWORD_PROPERTY);

		return Optional.ofNullable(identify(username, password)).orElseThrow(() ->
				new AuthenticationFailedException(String.format("User [%s] is not authorized", username)));	}

	private Principal identify(String username, String password) {
		return (isIdentified(username, password) ? TestPrincipal.newPrincipal(username) : null);
	}

	private boolean isIdentified(String username, String password) {

		return Optional.ofNullable(username)
				.filter(StringUtils::hasText)
				.map(user -> getAuthorizedUsers().get(user))
				.map(userPassword -> userPassword.equals(password))
				.orElse(false);
	}

	protected Map<String, String> getAuthorizedUsers() {
		return Collections.unmodifiableMap(this.authorizedUsers);
	}

	public static final class TestPrincipal implements java.security.Principal, java.io.Serializable {

		private final String name;

		public static TestPrincipal newPrincipal(String username) {
			return new TestPrincipal(username);
		}

		public TestPrincipal(String name) {
			this.name = Optional.ofNullable(name).filter(StringUtils::hasText)
					.orElseThrow(() -> newIllegalArgumentException("Name is required"));
		}

		@Override
		public String getName() {
			return this.name;
		}
	}
}
