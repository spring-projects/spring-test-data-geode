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

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

import org.apache.shiro.util.Assert;

/**
 * The ObjectToByteArrayComparator class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class ObjectToByteArrayComparator implements Comparator<Object> {

	public static final ObjectToByteArrayComparator INSTANCE = new ObjectToByteArrayComparator();

	@Override
	public int compare(Object objectOne, Object objectTwo) {

		return ByteArrayComparator.INSTANCE
			.compare(fromSerializableToByteArray(objectOne), fromSerializableToByteArray(objectTwo));
	}

	private Serializable assertSerializable(Object target) {

		Assert.isInstanceOf(Serializable.class, target);

		return (Serializable) target;
	}

	private byte[] fromSerializableToByteArray(Object target) {
		return toByteArray(assertSerializable(target));
	}

	private byte[] toByteArray(Object target) {

		try {
			return IOUtils.serializeObject(assertSerializable(target));
		}
		catch (IOException cause) {
			throw newIllegalArgumentException(cause, "Object [%s] could not be serialized", target);
		}
	}
}
