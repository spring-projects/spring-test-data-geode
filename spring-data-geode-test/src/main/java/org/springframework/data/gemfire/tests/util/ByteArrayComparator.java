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
package org.springframework.data.gemfire.tests.util;

import java.util.Comparator;

import org.springframework.util.Assert;

/**
 * The ByteArrayComparator class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class ByteArrayComparator implements Comparator<byte[]> {

	public static final ByteArrayComparator INSTANCE = new ByteArrayComparator();

	@Override
	public int compare(byte[] bytesOne, byte[] bytesTwo) {

		int bytesOneLength = bytesOne.length;
		int bytesTwoLength = bytesTwo.length;

		// Cannot use subtraction; Must be careful of overflow/underflow.
		return bytesOneLength < bytesTwoLength ? -1
			: bytesOneLength > bytesTwoLength ? 1
			: compareByteByByte(bytesOne, bytesTwo);
	}

	private int compareByteByByte(byte[] bytesOne, byte[] bytesTwo) {

		String errorMessage =
			"The length of the byte arrays do no match; byte array 1 is [%d] and byte array 2 is [%d]";

		Assert.isTrue(bytesOne.length == bytesTwo.length,
			String.format(errorMessage, bytesOne.length, bytesTwo.length));

		for (int index = 0; index < bytesOne.length; index++) {

			// Subtraction is OK here since a byte cannot cause an int to either overflow or underflow.
			int diff = (int) bytesOne[index] - (int) bytesTwo[index];

			if (diff != 0) {
				return diff;
			}
		}

		return 0;
	}
}
