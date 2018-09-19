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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.cache.wan.GatewayReceiver;
import org.apache.geode.cache.wan.GatewaySender;

/**
 * The {@link GatewayMockObjects} class is a mock objects class allowing users to manually mock Apache Geode
 * or Pivotal GemFire {@link GatewayReceiver} and {@link GatewaySender} objects and related objects in
 * the {@literal org.apache.geode.cache.wan} package.
 *
 * @author John Blum
 * @see org.apache.geode.cache.wan.GatewayReceiver
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.mock.MockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class GatewayMockObjects extends MockObjectsSupport {

	public static GatewayReceiver mockGatewayReceiver(String bindAddress, int endPort, String host,
			String hostnameForSenders, boolean manualStart, int maxTimeBetweenPings, int port,
			boolean initialRunningState, CacheServer server, int socketBufferSize, int startPort) throws IOException {

		AtomicBoolean running = new AtomicBoolean(initialRunningState);

		GatewayReceiver mockGatewayReceiver = mock(GatewayReceiver.class, withSettings().lenient());

		when(mockGatewayReceiver.getBindAddress()).thenReturn(bindAddress);
		when(mockGatewayReceiver.getEndPort()).thenReturn(endPort);
		when(mockGatewayReceiver.getHost()).thenReturn(host);
		//when(mockGatewayReceiver.getHostnameForSenders()).thenReturn(hostnameForSenders);
		when(mockGatewayReceiver.isManualStart()).thenReturn(manualStart);
		when(mockGatewayReceiver.getMaximumTimeBetweenPings()).thenReturn(maxTimeBetweenPings);
		when(mockGatewayReceiver.getPort()).thenReturn(port);
		when(mockGatewayReceiver.isRunning()).thenAnswer(newGetter(running));
		when(mockGatewayReceiver.getServer()).thenReturn(server);
		when(mockGatewayReceiver.getSocketBufferSize()).thenReturn(socketBufferSize);
		when(mockGatewayReceiver.getStartPort()).thenReturn(startPort);

		doAnswer(newSetter(running, true)).when(mockGatewayReceiver).start();
		doAnswer(newSetter(running, false)).when(mockGatewayReceiver).stop();

		return mockGatewayReceiver;
	}

	public static GatewaySender mockGatewaySender(String id, int alertThreshold, boolean batchConflationEnabled,
			int batchSize, int batchTimeInterval, String diskStoreName, boolean diskSynchronous, int dispatcherThreads,
			int maxQueueMemory, int maxParallelismForReplicatedRegion, GatewaySender.OrderPolicy orderPolicy,
			boolean parallel, boolean persistent, int remoteDistributedSystemId, boolean initialRunningState,
			int socketBufferSize, int socketReadTimeout) {

		AtomicBoolean paused = new AtomicBoolean(false);
		AtomicBoolean running = new AtomicBoolean(initialRunningState);
		AtomicBoolean runningState = new AtomicBoolean(running.get());

		GatewaySender mockGatewaySender = mock(GatewaySender.class, withSettings().name(id).lenient());

		when(mockGatewaySender.getAlertThreshold()).thenReturn(alertThreshold);
		when(mockGatewaySender.isBatchConflationEnabled()).thenReturn(batchConflationEnabled);
		when(mockGatewaySender.getBatchSize()).thenReturn(batchSize);
		when(mockGatewaySender.getBatchTimeInterval()).thenReturn(batchTimeInterval);
		when(mockGatewaySender.getDiskStoreName()).thenReturn(diskStoreName);
		when(mockGatewaySender.isDiskSynchronous()).thenReturn(diskSynchronous);
		when(mockGatewaySender.getDispatcherThreads()).thenReturn(dispatcherThreads);
		when(mockGatewaySender.getId()).thenReturn(id);
		when(mockGatewaySender.getMaximumQueueMemory()).thenReturn(maxQueueMemory);
		when(mockGatewaySender.getMaxParallelismForReplicatedRegion()).thenReturn(maxParallelismForReplicatedRegion);
		when(mockGatewaySender.getOrderPolicy()).thenReturn(orderPolicy);
		when(mockGatewaySender.isParallel()).thenReturn(parallel);
		when(mockGatewaySender.isPaused()).thenAnswer(newGetter(paused));
		when(mockGatewaySender.isPersistenceEnabled()).thenReturn(persistent);
		when(mockGatewaySender.isRunning()).thenAnswer(newGetter(running));
		when(mockGatewaySender.getRemoteDSId()).thenReturn(remoteDistributedSystemId);
		when(mockGatewaySender.getSocketBufferSize()).thenReturn(socketBufferSize);
		when(mockGatewaySender.getSocketReadTimeout()).thenReturn(socketReadTimeout);

		doAnswer(invocation -> {

			paused.set(true);
			running.set(false);

			return null;

		}).when(mockGatewaySender).pause();

		doAnswer(invocation -> {

			paused.set(false);
			running.set(runningState.get());

			return null;

		}).when(mockGatewaySender).resume();

		doAnswer(invocation -> {

			running.set(true);
			runningState.set(true);

			return null;

		}).when(mockGatewaySender).start();

		doAnswer(invocation -> {

			running.set(false);
			runningState.set(false);

			return null;

		}).when(mockGatewaySender).stop();

		return mockGatewaySender;
	}
}
