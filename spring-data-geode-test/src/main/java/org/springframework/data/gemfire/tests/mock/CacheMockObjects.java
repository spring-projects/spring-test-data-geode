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

import static org.apache.geode.internal.util.CollectionUtils.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.control.ResourceManager;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.DistributedSystem;
import org.springframework.data.gemfire.util.RegionUtils;

/**
 * The {@link CacheMockObjects} class is a mock objects class allowing users to mock Apache Geode or Pivotal GemFire
 * {@link GemFireCache} objects and related objects (e.g. {@link DistributedSystem}, {@link ResourceManager},
 * {@link Region}, etc).
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.control.ResourceManager
 * @see org.apache.geode.distributed.DistributedMember
 * @see org.apache.geode.distributed.DistributedSystem
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CacheMockObjects {

	@SuppressWarnings("unchecked")
	public static <T extends GemFireCache> T mockGemFireCache(T mockGemFireCache,
			DistributedSystem distributedSystem, ResourceManager resourceManager, Region<?, ?>... regions) {

		T theMockGemFireCache = mockGemFireCache != null ? mockGemFireCache : (T) mock(GemFireCache.class);

		when(theMockGemFireCache.getDistributedSystem()).thenReturn(distributedSystem);
		when(theMockGemFireCache.getResourceManager()).thenReturn(resourceManager);

		Optional.ofNullable(regions)
			.filter(it -> it.length != 0)
			.ifPresent(it ->  when(theMockGemFireCache.rootRegions()).thenReturn(asSet(it)));

		return theMockGemFireCache;
	}

	public static ClientCache mockClientCache(DistributedSystem distributedSystem, ResourceManager resourceManager,
			Region<?, ?>... regions) {

		return mockGemFireCache(mock(ClientCache.class), distributedSystem, resourceManager, regions);
	}

	public static Cache mockPeerCache(DistributedSystem distributedSystem, ResourceManager resourceManager,
			Region<?, ?>... regions) {

		return mockGemFireCache(mock(Cache.class), distributedSystem, resourceManager, regions);
	}

	public static DistributedSystem mockDistributedSystem(DistributedMember distributedMember) {

		DistributedSystem mockDistributedSystem = mock(DistributedSystem.class);

		when(mockDistributedSystem.getDistributedMember()).thenReturn(distributedMember);

		return mockDistributedSystem;
	}

	public static DistributedMember mockDistributedMember(String name, String... groups) {

		DistributedMember mockDistributeMember = mock(DistributedMember.class, name);

		when(mockDistributeMember.getName()).thenReturn(name);
		when(mockDistributeMember.getGroups()).thenReturn(Arrays.asList(groups));
		when(mockDistributeMember.getId()).thenReturn(UUID.randomUUID().toString());

		return mockDistributeMember;

	}

	public static ResourceManager mockResourceManager(float criticalHeapPercentage, float criticalOffHeapPercentage,
			float evictionHeapPercentage, float evictionOffHeapPercentage) {

		ResourceManager mockResourceManager = mock(ResourceManager.class);

		when(mockResourceManager.getCriticalHeapPercentage()).thenReturn(criticalHeapPercentage);
		when(mockResourceManager.getCriticalOffHeapPercentage()).thenReturn(criticalOffHeapPercentage);
		when(mockResourceManager.getEvictionHeapPercentage()).thenReturn(evictionHeapPercentage);
		when(mockResourceManager.getEvictionOffHeapPercentage()).thenReturn(evictionOffHeapPercentage);

		return mockResourceManager;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Region<K, V> mockRegion(String name, DataPolicy dataPolicy) {

		Region<K, V> mockRegion = mock(Region.class, name);

		when(mockRegion.getName()).thenReturn(RegionUtils.toRegionName(name));
		when(mockRegion.getFullPath()).thenReturn(RegionUtils.toRegionPath(name));

		RegionAttributes<K, V> mockRegionAttributes = mock(RegionAttributes.class,
			String.format("%sRegionAttributes", name));

		when(mockRegionAttributes.getDataPolicy()).thenReturn(dataPolicy);
		when(mockRegion.getAttributes()).thenReturn(mockRegionAttributes);

		return mockRegion;
	}
}
