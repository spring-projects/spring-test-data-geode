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

package org.springframework.data.gemfire.tests.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.apache.geode.cache.asyncqueue.AsyncEventQueue;
import org.apache.geode.cache.wan.GatewaySender;

/**
 * The {@link AsyncEventQueueMockObjects} class is a mock objects class allowing users to manually mock Apache Geode
 * or Pivotal GemFire {@link AsyncEventQueue} objects and related objects in the
 * {@literal org.apache.geode.cache.asyncqueue} package.
 *
 * @author John Blum
 * @see org.apache.geode.cache.asyncqueue.AsyncEventQueue
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.mock.MockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AsyncEventQueueMockObjects {

	public static AsyncEventQueue mockAsyncEventQueue(String id, boolean batchConflationEnabled, int batchSize,
			int batchTimeInterval, String diskStoreName, boolean diskSynchronous, int dispatcherThreads,
			boolean forwardExpirationDestroy, int maximumQueueMemory, GatewaySender.OrderPolicy orderPolicy,
			boolean parallel, boolean persistent, boolean primary, int size) {

		AsyncEventQueue mockAsyncEventQueue = mock(AsyncEventQueue.class, withSettings().name(id).lenient());

		when(mockAsyncEventQueue.getId()).thenReturn(id);
		when(mockAsyncEventQueue.isBatchConflationEnabled()).thenReturn(batchConflationEnabled);
		when(mockAsyncEventQueue.getBatchSize()).thenReturn(batchSize);
		when(mockAsyncEventQueue.getBatchTimeInterval()).thenReturn(batchTimeInterval);
		when(mockAsyncEventQueue.getDiskStoreName()).thenReturn(diskStoreName);
		when(mockAsyncEventQueue.isDiskSynchronous()).thenReturn(diskSynchronous);
		when(mockAsyncEventQueue.getDispatcherThreads()).thenReturn(dispatcherThreads);
		when(mockAsyncEventQueue.isForwardExpirationDestroy()).thenReturn(forwardExpirationDestroy);
		when(mockAsyncEventQueue.getMaximumQueueMemory()).thenReturn(maximumQueueMemory);
		when(mockAsyncEventQueue.getOrderPolicy()).thenReturn(orderPolicy);
		when(mockAsyncEventQueue.isParallel()).thenReturn(parallel);
		when(mockAsyncEventQueue.isPersistent()).thenReturn(persistent);
		when(mockAsyncEventQueue.isPrimary()).thenReturn(primary);
		when(mockAsyncEventQueue.size()).thenReturn(size);

		return mockAsyncEventQueue;
	}
}
