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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.File;
import java.util.UUID;

import org.apache.geode.cache.DiskStore;

/**
 * The {@link DiskStoreMockObjects} class is a mock objects class allowing users to manually mock Apache Geode
 * or Pivotal GemFire {@link DiskStore} objects and related objects in the {@literal org.apache.geode.cache} package.
 *
 * @author John Blum
 * @see org.apache.geode.cache.DiskStore
 * @see org.mockito.Mockito
 * @see org.springframework.data.gemfire.tests.mock.MockObjectsSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class DiskStoreMockObjects extends MockObjectsSupport {

	public static DiskStore mockDiskStore(String name, boolean allowForceCompaction, boolean autoCompact,
			int compactionThreshold, File[] diskDirectories, int[] diskDirectorySizes, float diskUsageCriticalPercentage,
			float diskUsageWarningPercentage, long maxOplogSize, int queueSize, long timeInterval, int writeBufferSize) {

		DiskStore mockDiskStore = mock(DiskStore.class, withSettings().name(name).lenient());

		when(mockDiskStore.getAllowForceCompaction()).thenReturn(allowForceCompaction);
		when(mockDiskStore.getAutoCompact()).thenReturn(autoCompact);
		when(mockDiskStore.getCompactionThreshold()).thenReturn(compactionThreshold);
		when(mockDiskStore.getDiskDirs()).thenReturn(diskDirectories);
		when(mockDiskStore.getDiskDirSizes()).thenReturn(diskDirectorySizes);
		when(mockDiskStore.getDiskStoreUUID()).thenReturn(UUID.randomUUID());
		when(mockDiskStore.getDiskUsageCriticalPercentage()).thenReturn(diskUsageCriticalPercentage);
		when(mockDiskStore.getDiskUsageWarningPercentage()).thenReturn(diskUsageWarningPercentage);
		when(mockDiskStore.getMaxOplogSize()).thenReturn(maxOplogSize);
		when(mockDiskStore.getName()).thenReturn(name);
		when(mockDiskStore.getQueueSize()).thenReturn(queueSize);
		when(mockDiskStore.getTimeInterval()).thenReturn(timeInterval);
		when(mockDiskStore.getWriteBufferSize()).thenReturn(writeBufferSize);

		return mockDiskStore;
	}
}
