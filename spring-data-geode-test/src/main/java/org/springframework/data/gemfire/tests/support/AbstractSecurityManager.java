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

package org.springframework.data.gemfire.tests.support;

import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.shiro.authz.AuthorizationException;

/**
 * The {@link AbstractSecurityManager} class is an abstract base class supporting implementations of
 * {@link org.apache.geode.security.SecurityManager}.
 *
 * @author John Blum
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSecurityManager implements org.apache.geode.security.SecurityManager {

	@Override
	public void init(Properties securityProps) { }

	@Override
	public Object authenticate(Properties credentials) throws AuthenticationFailedException {
		throw new AuthenticationFailedException("Access Denied");
	}

	@Override
	public boolean authorize(Object principal, ResourcePermission permission) {
		throw new AuthorizationException("Not Authorized");
	}

	@Override
	public void close() { }

}
