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

package org.springframework.data.gemfire.tests.util;

import java.util.Comparator;

/**
 * The IdentityHashCodeComparator class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class IdentityHashCodeComparator implements Comparator<Object> {

	public static final IdentityHashCodeComparator INSTANCE = new IdentityHashCodeComparator();

	@Override
	public int compare(Object objectOne, Object objectTwo) {

		int objectOneHashCode = System.identityHashCode(objectOne);
		int objectTwoHashCode = System.identityHashCode(objectTwo);

		// Cannot use subtraction; Must be careful of overflow/underflow.
		return objectOneHashCode < objectTwoHashCode ? -1
			: objectOneHashCode > objectTwoHashCode ? 1
			: 0;
	}
}
