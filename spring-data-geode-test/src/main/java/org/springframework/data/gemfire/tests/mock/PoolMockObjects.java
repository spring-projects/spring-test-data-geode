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

package org.springframework.data.gemfire.tests.mock;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.query.QueryService;

/**
 * The {@link PoolMockObjects} class is a mock objects class allowing users to manually mock Apache Geode
 * or Pivotal GemFire client {@link Pool} objects and related objects in the {@literal org.apache.geode.cache.client}
 * package.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.Pool
 * @see org.mockito.Mockito
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class PoolMockObjects extends MockObjectsSupport {

	public static Pool mockPool(String name, boolean initialDestroyedState, int freeConnectionTimeout, long idleTimeout,
			int loadConditioningInterval, List<InetSocketAddress> locators, int maxConnections, int minConnections,
			boolean multiUserAuthentication, List<InetSocketAddress> onlineLocators, int pendingEventCount,
			long pingInterval, boolean prSingleHopEnabled, QueryService queryService, int readTimeout, int retryAttempts,
			String serverGroup, List<InetSocketAddress> servers, int socketBufferSize, int socketConnectTimeout,
			int statisticInterval, int subscriptionAckInterval, boolean subscriptionEnabled,
			int subscriptionMessageTrackingTimeout, int subscriptionRedundancy, int subscriptionTimeoutMultiplier,
			boolean threadLocalConnections) {

		AtomicBoolean destroyed = new AtomicBoolean(initialDestroyedState);

		Pool mockPool = mock(Pool.class, name);

		when(mockPool.isDestroyed()).thenAnswer(newGetter(destroyed));
		when(mockPool.getFreeConnectionTimeout()).thenReturn(freeConnectionTimeout);
		when(mockPool.getIdleTimeout()).thenReturn(idleTimeout);
		when(mockPool.getLoadConditioningInterval()).thenReturn(loadConditioningInterval);
		when(mockPool.getLocators()).thenReturn(locators);
		when(mockPool.getMaxConnections()).thenReturn(maxConnections);
		when(mockPool.getMinConnections()).thenReturn(minConnections);
		when(mockPool.getMultiuserAuthentication()).thenReturn(multiUserAuthentication);
		when(mockPool.getName()).thenReturn(name);
		when(mockPool.getOnlineLocators()).thenReturn(onlineLocators);
		when(mockPool.getPendingEventCount()).thenReturn(pendingEventCount);
		when(mockPool.getPingInterval()).thenReturn(pingInterval);
		when(mockPool.getPRSingleHopEnabled()).thenReturn(prSingleHopEnabled);
		when(mockPool.getQueryService()).thenReturn(queryService);
		when(mockPool.getReadTimeout()).thenReturn(readTimeout);
		when(mockPool.getRetryAttempts()).thenReturn(retryAttempts);
		when(mockPool.getServerGroup()).thenReturn(serverGroup);
		when(mockPool.getServers()).thenReturn(servers);
		when(mockPool.getSocketBufferSize()).thenReturn(socketBufferSize);
		when(mockPool.getSocketConnectTimeout()).thenReturn(socketConnectTimeout);
		when(mockPool.getStatisticInterval()).thenReturn(statisticInterval);
		when(mockPool.getSubscriptionAckInterval()).thenReturn(subscriptionAckInterval);
		when(mockPool.getSubscriptionEnabled()).thenReturn(subscriptionEnabled);
		when(mockPool.getSubscriptionMessageTrackingTimeout()).thenReturn(subscriptionMessageTrackingTimeout);
		when(mockPool.getSubscriptionRedundancy()).thenReturn(subscriptionRedundancy);
		when(mockPool.getSubscriptionTimeoutMultiplier()).thenReturn(subscriptionTimeoutMultiplier);
		when(mockPool.getThreadLocalConnections()).thenReturn(threadLocalConnections);

		doAnswer(newSetter(destroyed, true)).when(mockPool).destroy();
		doAnswer(newSetter(destroyed, true)).when(mockPool).destroy(anyBoolean());

		return mockPool;
	}
}
