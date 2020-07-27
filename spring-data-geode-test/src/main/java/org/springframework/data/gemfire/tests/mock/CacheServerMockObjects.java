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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.server.ClientSubscriptionConfig;
import org.apache.geode.cache.server.ServerLoad;
import org.apache.geode.cache.server.ServerLoadProbe;
import org.apache.geode.cache.server.ServerMetrics;

/**
 * The {@link CacheServerMockObjects} class is a mock objects class allowing users to manually mock Apache Geode
 * or VMware GemFire {@link CacheServer} objects and related objects in the {@literal org.apache.geode.cache.server}
 * package.
 *
 * @author John Blum
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.apache.geode.cache.server.ClientSubscriptionConfig
 * @see org.apache.geode.cache.server.ServerLoad
 * @see org.apache.geode.cache.server.ServerLoadProbe
 * @see org.apache.geode.cache.server.ServerMetrics
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.mock.MockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CacheServerMockObjects extends MockObjectsSupport {

	// TODO mock ClientSessions and InterestRegistrationListeners
	public static CacheServer mockCacheServer(String bindAddress, ClientSubscriptionConfig clientSubscriptionConfig,
			String hostnameForClients, long loadPollInterval, ServerLoadProbe serverLoadProbe, int maxConnections,
			int maxMessageCount, int maxThreads, int maxTimeBetweenPings, int messageTimeToLive, int port,
			boolean running, int socketBufferSize, boolean tcpNoDelay) throws Exception {

		AtomicBoolean runningState = new AtomicBoolean(running);

		CacheServer mockCacheServer = mock(CacheServer.class, withSettings().lenient());

		when(mockCacheServer.getBindAddress()).thenReturn(bindAddress);
		when(mockCacheServer.getHostnameForClients()).thenReturn(hostnameForClients);
		when(mockCacheServer.getLoadPollInterval()).thenReturn(loadPollInterval);
		when(mockCacheServer.getLoadProbe()).thenReturn(serverLoadProbe);
		when(mockCacheServer.getMaxConnections()).thenReturn(maxConnections);
		when(mockCacheServer.getMaximumMessageCount()).thenReturn(maxMessageCount);
		when(mockCacheServer.getMaximumTimeBetweenPings()).thenReturn(maxTimeBetweenPings);
		when(mockCacheServer.getMaxThreads()).thenReturn(maxThreads);
		when(mockCacheServer.getMessageTimeToLive()).thenReturn(messageTimeToLive);
		when(mockCacheServer.getPort()).thenReturn(port);
		when(mockCacheServer.isRunning()).thenAnswer(newGetter(runningState));
		when(mockCacheServer.getSocketBufferSize()).thenReturn(socketBufferSize);
		when(mockCacheServer.getTcpNoDelay()).thenReturn(tcpNoDelay);

		Optional.ofNullable(clientSubscriptionConfig)
			.ifPresent(it -> when(mockCacheServer.getClientSubscriptionConfig()).thenReturn(it));

		Optional.ofNullable(serverLoadProbe).ifPresent(it -> when(mockCacheServer.getLoadProbe()).thenReturn(it));

		doAnswer(invocation -> {
			runningState.set(true);
			Optional.ofNullable(mockCacheServer.getLoadProbe()).ifPresent(ServerLoadProbe::open);
			return null;
		}).when(mockCacheServer).start();

		doAnswer(invocation -> {
			runningState.set(false);
			Optional.ofNullable(mockCacheServer.getLoadProbe()).ifPresent(ServerLoadProbe::close);
			return null;
		}).when(mockCacheServer).stop();

		return mockCacheServer;
	}

	public static ClientSubscriptionConfig mockClientSubscriptionConfig(int capacity, String diskStoreName,
			String evictionPolicy) {

		ClientSubscriptionConfig mockClientSubscriptionConfig =
			mock(ClientSubscriptionConfig.class, withSettings().lenient());

		when(mockClientSubscriptionConfig.getCapacity()).thenReturn(capacity);
		when(mockClientSubscriptionConfig.getDiskStoreName()).thenReturn(diskStoreName);
		when(mockClientSubscriptionConfig.getEvictionPolicy()).thenReturn(evictionPolicy);

		return mockClientSubscriptionConfig;
	}

	// TODO file Apache Geode JIRA ticket to refactor the final ServerLoad class into a proper interface!
	public static ServerLoad mockServerLoad(float connectionLoad, float loadPerConnection,
			float loadPerSubscriptionConnection, float subscriptionConnectionLoad) {

		return new ServerLoad(connectionLoad, loadPerConnection, subscriptionConnectionLoad,
			loadPerSubscriptionConnection);
	}

	public static ServerLoadProbe mockServerLoadProbe() {
		return mock(ServerLoadProbe.class);
	}

	public static ServerMetrics mockServerMetrics(int clientCount, int connectionCount, int maxConnections,
			int subscriptionConnectionCount) {

		ServerMetrics mockServerMetrics = mock(ServerMetrics.class, withSettings().lenient());

		when(mockServerMetrics.getClientCount()).thenReturn(clientCount);
		when(mockServerMetrics.getConnectionCount()).thenReturn(connectionCount);
		when(mockServerMetrics.getMaxConnections()).thenReturn(maxConnections);
		when(mockServerMetrics.getSubscriptionConnectionCount()).thenReturn(subscriptionConnectionCount);

		return mockServerMetrics;
	}
}
