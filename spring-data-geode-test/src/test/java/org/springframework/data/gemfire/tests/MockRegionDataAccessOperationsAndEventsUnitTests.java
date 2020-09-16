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
package org.springframework.data.gemfire.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import org.apache.geode.cache.CacheListener;
import org.apache.geode.cache.CacheLoader;
import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.EntryNotFoundException;
import org.apache.geode.cache.LoaderHelper;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.RegionService;

import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;
import org.springframework.data.gemfire.tests.support.MapBuilder;

/**
 * Unit Tests for Mock {@link Region} Data Access Operations and Events.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.CacheListener
 * @see org.apache.geode.cache.CacheLoader
 * @see org.apache.geode.cache.CacheWriter
 * @see org.apache.geode.cache.EntryEvent
 * @see org.apache.geode.cache.LoaderHelper
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionAttributes
 * @see org.apache.geode.cache.RegionService
 * @since 0.0.3
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class MockRegionDataAccessOperationsAndEventsUnitTests {

	private Region<Object, Object> mockRegion;

	@Mock
	private RegionAttributes<Object, Object> mockRegionAttributes;

	@Mock
	private RegionService mockRegionService;

	@Before
	public void setup() {

		this.mockRegion = GemFireMockObjectsSupport.mockRegion(this.mockRegionService,
			"MockRegion", this.mockRegionAttributes);
	}

	@After
	public void tearDown() {
		GemFireMockObjectsSupport.destroy();
	}

	@Test
	public void putGetRemoveIsSuccessful() {

		assertThat(this.mockRegion).doesNotContainKey(1);
		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.put(1, "test")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("test");
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.remove(1)).isEqualTo("test");
		assertThat(this.mockRegion).doesNotContainKey(1);
		assertThat(this.mockRegion).hasSize(0);
	}

	@Test
	public void putInvalidateGetIsSuccessful() {

		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.put(1, "test"));
		assertThat(this.mockRegion).hasSize(1);

		this.mockRegion.invalidate(1);

		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get(1)).isNull();
	}

	@Test
	public void putGetInvalidateGetPutGetIsSuccessful() {

		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.put(1, "test"));
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("test");

		this.mockRegion.invalidate(1);

		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get(1)).isNull();
		assertThat(this.mockRegion.put(1, "mock")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("mock");
	}

	@Test
	public void cacheLoaderLoadsValueOnCacheMiss() {

		assertThat(this.mockRegion).doesNotContainKey("key");
		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.get("key")).isNull();

		AtomicInteger counter = new AtomicInteger(0);

		CacheLoader<Object, Object> mockCacheLoader = mock(CacheLoader.class);

		when(mockCacheLoader.load(any(LoaderHelper.class))).thenAnswer(invocation -> counter.incrementAndGet());

		this.mockRegion.getAttributesMutator().setCacheLoader(mockCacheLoader);

		assertThat(this.mockRegion.get("key")).isEqualTo(1);
		assertThat(this.mockRegion.get("key")).isEqualTo(1);
		assertThat(this.mockRegion).containsKey("key");
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.remove("key")).isEqualTo(1);
		assertThat(this.mockRegion).doesNotContainKey("key");
		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.get("key")).isEqualTo(2);
		assertThat(this.mockRegion).containsKey("key");
		assertThat(this.mockRegion).hasSize(1);

		this.mockRegion.invalidate("key");

		assertThat(this.mockRegion).containsKey("key");
		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.get("key")).isEqualTo(3);
		assertThat(this.mockRegion).containsKey("key");
		assertThat(this.mockRegion).hasSize(1);

		verify(mockCacheLoader, times(3)).load(isA(LoaderHelper.class));
	}

	@Test(expected = EntryNotFoundException.class)
	public void invalidateWithNonExistingKeyThrowsException() {

		try {
			this.mockRegion.invalidate(1);
		}
		catch (EntryNotFoundException expected) {

			assertThat(expected).hasMessage("Entry with key [1] not found");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void invalidateFiresCacheListenerAfterInvalidateEntryEvent() {

		assertThat(this.mockRegion.put(1, "test")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("test");

		CacheListener<Object, Object> mockCacheListener = mock(CacheListener.class);

		doAnswer(invocation -> {

			EntryEvent<Object, Object> entryEvent = invocation.getArgument(0);

			assertThat(entryEvent).isNotNull();
			assertThat(entryEvent.getKey()).isEqualTo(1);
			assertThat(entryEvent.getNewValue()).isNull();
			assertThat(entryEvent.getOldValue()).isEqualTo("test");
			assertThat(entryEvent.getRegion()).isEqualTo(this.mockRegion);

			return null;

		}).when(mockCacheListener).afterInvalidate(any(EntryEvent.class));

		this.mockRegion.getAttributesMutator().addCacheListener(mockCacheListener);
		this.mockRegion.invalidate(1);

		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion.get(1)).isNull();

		this.mockRegion.invalidate(1);

		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion.get(1)).isNull();

		verify(mockCacheListener, times(1)).afterInvalidate(isA(EntryEvent.class));
		verifyNoMoreInteractions(mockCacheListener);
	}

	@Test
	public void putFiresCacheWriterThenCacheListenersEntryCreateAndUpdateEvents() {

		Answer<Void> createEntryEvent = invocation -> {

			EntryEvent<Object, Object> entryEvent = invocation.getArgument(0);

			assertThat(entryEvent.getKey()).isEqualTo("key");
			assertThat(entryEvent.getOldValue()).isNull();
			assertThat(entryEvent.getNewValue()).isEqualTo(1);
			assertThat(entryEvent.getRegion()).isEqualTo(this.mockRegion);

			return null;

		};

		Answer<Void> updateEntryEvent = invocation -> {

			EntryEvent<Object, Object> entryEvent = invocation.getArgument(0);

			assertThat(entryEvent.getKey()).isEqualTo("key");
			assertThat(entryEvent.getOldValue()).isEqualTo(1);
			assertThat(entryEvent.getNewValue()).isEqualTo(2);
			assertThat(entryEvent.getRegion()).isEqualTo(this.mockRegion);

			return null;

		};

		CacheListener<Object, Object> mockCacheListener = mock(CacheListener.class);

		doAnswer(createEntryEvent).when(mockCacheListener).afterCreate(any(EntryEvent.class));
		doAnswer(updateEntryEvent).when(mockCacheListener).afterUpdate(any(EntryEvent.class));

		CacheWriter<Object, Object> mockCacheWriter = mock(CacheWriter.class);

		doAnswer(createEntryEvent).when(mockCacheWriter).beforeCreate(any(EntryEvent.class));
		doAnswer(updateEntryEvent).when(mockCacheWriter).beforeUpdate(any(EntryEvent.class));

		this.mockRegion.getAttributesMutator().addCacheListener(mockCacheListener);
		this.mockRegion.getAttributesMutator().setCacheWriter(mockCacheWriter);

		assertThat(this.mockRegion.put("key", 1)).isNull();
		assertThat(this.mockRegion.put("key", 2)).isEqualTo(1);

		InOrder ordered = inOrder(this.mockRegion, mockCacheWriter, mockCacheListener);

		ordered.verify(this.mockRegion, times(1)).put(eq("key"), eq(1));
		ordered.verify(mockCacheWriter, times(1)).beforeCreate(isA(EntryEvent.class));
		ordered.verify(mockCacheListener, times(1)).afterCreate(isA(EntryEvent.class));
		ordered.verify(this.mockRegion, times(1)).put(eq("key"), eq(2));
		ordered.verify(mockCacheWriter, times(1)).beforeUpdate(isA(EntryEvent.class));
		ordered.verify(mockCacheListener, times(1)).afterUpdate(isA(EntryEvent.class));
		verifyNoMoreInteractions(mockCacheListener);
		verifyNoMoreInteractions(mockCacheWriter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putNullValue() {

		try {
			this.mockRegion.put(1, null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Value is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = CacheWriterException.class)
	public void putStoppedByCacheWriterException() {

		CacheListener<Object, Object> mockCacheListener = mock(CacheListener.class);

		CacheWriter<Object, Object> mockCacheWriter = mock(CacheWriter.class);

		doThrow(newIllegalStateException("TEST")).when(mockCacheWriter).beforeCreate(any(EntryEvent.class));

		this.mockRegion.getAttributesMutator().addCacheListener(mockCacheListener);
		this.mockRegion.getAttributesMutator().setCacheWriter(mockCacheWriter);

		try {
			this.mockRegion.put(1, "test");
		}
		catch (CacheWriterException cause) {

			assertThat(cause).hasMessage("Create/Update Error");
			assertThat(cause).hasCauseInstanceOf(IllegalStateException.class);
			assertThat(cause.getCause()).hasMessage("TEST");
			assertThat(cause.getCause()).hasNoCause();

			throw cause;
		}
		finally {

			assertThat(this.mockRegion).doesNotContainKey(1);
			assertThat(this.mockRegion).hasSize(0);

			verify(mockCacheWriter, times(1)).beforeCreate(isA(EntryEvent.class));
			verifyNoInteractions(mockCacheListener);
		}
	}

	@Test
	public void removeFiresCacheWriterThenCacheListenerEntryDestroyEvent() {

		Answer<Void> destroyEntryEvent = invocation -> {

			EntryEvent<Object, Object> entryEvent = invocation.getArgument(0);

			assertThat(entryEvent.getKey()).isEqualTo(1);
			assertThat(entryEvent.getNewValue()).isNull();
			assertThat(entryEvent.getOldValue()).isEqualTo("test");
			assertThat(entryEvent.getRegion()).isEqualTo(this.mockRegion);

			return null;

		};

		CacheListener<Object, Object> mockCacheListener = mock(CacheListener.class);

		doAnswer(destroyEntryEvent).when(mockCacheListener).afterDestroy(any(EntryEvent.class));

		CacheWriter<Object, Object> mockCacheWriter = mock(CacheWriter.class);

		doAnswer(destroyEntryEvent).when(mockCacheWriter).beforeDestroy(any(EntryEvent.class));

		assertThat(this.mockRegion.put(1, "test")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);

		this.mockRegion.getAttributesMutator().addCacheListener(mockCacheListener);
		this.mockRegion.getAttributesMutator().setCacheWriter(mockCacheWriter);

		assertThat(this.mockRegion.remove(1)).isEqualTo("test");
		assertThat(this.mockRegion).doesNotContainKey(1);
		assertThat(this.mockRegion).hasSize(0);

		InOrder ordered = inOrder(this.mockRegion, mockCacheWriter, mockCacheListener);

		ordered.verify(this.mockRegion, times(1)).remove(eq(1));
		ordered.verify(mockCacheWriter, times(1)).beforeDestroy(isA(EntryEvent.class));
		ordered.verify(mockCacheListener, times(1)).afterDestroy(isA(EntryEvent.class));
		verifyNoMoreInteractions(mockCacheListener);
		verifyNoMoreInteractions(mockCacheWriter);
	}

	@Test(expected = CacheWriterException.class)
	public void removeStoppedByCacheWriterException() {

		CacheListener<Object, Object> mockCacheListener = mock(CacheListener.class);

		CacheWriter<Object, Object> mockCacheWriter = mock(CacheWriter.class);

		doThrow(newIllegalStateException("TEST")).when(mockCacheWriter).beforeDestroy(any(EntryEvent.class));

		assertThat(this.mockRegion.put(1, "test")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion).hasSize(1);

		this.mockRegion.getAttributesMutator().addCacheListener(mockCacheListener);
		this.mockRegion.getAttributesMutator().setCacheWriter(mockCacheWriter);

		try {
			this.mockRegion.remove(1);
		}
		catch (CacheWriterException expected) {

			assertThat(expected).hasMessage("Destroy Error");
			assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
			assertThat(expected.getCause()).hasMessage("TEST");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {

			assertThat(this.mockRegion).containsKey(1);
			assertThat(this.mockRegion).hasSize(1);

			verify(mockCacheWriter, times(1)).beforeDestroy(isA(EntryEvent.class));
			verifyNoInteractions(mockCacheListener);
		}
	}

	@Test
	public void mockingUnsupportedPutAllWorksAsExpected() {

		doAnswer(invocation -> {

			Map<Object, Object> keysValues = invocation.getArgument(0);

			for (Map.Entry<Object, Object> entry : keysValues.entrySet()) {
				this.mockRegion.put(entry.getKey(), entry.getValue());
			}

			return null;

		}).when(this.mockRegion).putAll(any(Map.class));

		Map<Object, Object> keysValues = MapBuilder.newMapBuilder()
			.put(1, "mock")
			.put(2, "test")
			.put(3, "stub")
			.build();

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.putAll(keysValues);

		assertThat(this.mockRegion).hasSize(keysValues.size());
		assertThat(this.mockRegion).containsKeys(1, 2, 3);
		assertThat(this.mockRegion.get(1)).isEqualTo("mock");
		assertThat(this.mockRegion.get(2)).isEqualTo("test");
		assertThat(this.mockRegion.get(3)).isEqualTo("stub");
	}

	@Test
	@SuppressWarnings("all")
	public void mockingUnsupportedPutIfAbsentWorksAsExpected() {

		doAnswer(invocation -> {

			Object key = invocation.getArgument(0);
			Object value = invocation.getArgument(1);
			Object existingValue = null;

			synchronized (this.mockRegion) {

				existingValue = this.mockRegion.get(key);

				if (Objects.isNull(existingValue)) {
					this.mockRegion.put(key, value);
				}
			}

			return existingValue;

		}).when(this.mockRegion).putIfAbsent(any(), any());

		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion.putIfAbsent(1, "test")).isNull();
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("test");
		assertThat(this.mockRegion.putIfAbsent(1, "mock")).isEqualTo("test");
		assertThat(this.mockRegion).containsKey(1);
		assertThat(this.mockRegion.get(1)).isEqualTo("test");
	}

	@Test
	public void mapRegionClearIsCorrect() {

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.put(1, "TEST");
		this.mockRegion.put(2, "MOCK");

		assertThat(this.mockRegion).hasSize(2);

		this.mockRegion.clear();

		assertThat(this.mockRegion).hasSize(0);
	}

	@Test
	public void mapRegionContainsKeyIsCorrect() {

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.put(1, "TEST");

		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.containsKey(1)).isTrue();
		assertThat(this.mockRegion.containsKey(1.0d)).isFalse();
	}

	@Test
	public void mapRegionContainsValueIsCorrect() {

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.put(2, "MOCK");

		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion.containsValue("MOCK")).isTrue();
		assertThat(this.mockRegion.containsValue("TEST")).isFalse();
		assertThat(this.mockRegion.containsValue("Mock")).isFalse();
		assertThat(this.mockRegion.containsValue("mock")).isFalse();
	}

	@Test
	public void regionContainsValueForKeyIsCorrect() {

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.put(1, "TEST");
		this.mockRegion.put(2, "MOCK");
		this.mockRegion.invalidate(2);

		assertThat(this.mockRegion).hasSize(2);
		assertThat(this.mockRegion.containsValueForKey(1)).isTrue();
		assertThat(this.mockRegion.containsValueForKey(1L)).isFalse();
		assertThat(this.mockRegion.containsValueForKey(2)).isFalse();
		assertThat(this.mockRegion.containsValueForKey(3)).isFalse();
	}

	@Test
	public void mapForEachIsCorrect() {

		Set<String> entryStrings = new HashSet<>(2);

		assertThat(this.mockRegion).hasSize(0);

		this.mockRegion.put(1, "TEST");
		this.mockRegion.put(2, "MOCK");

		assertThat(this.mockRegion).hasSize(2);

		this.mockRegion.forEach((key, value) -> entryStrings.add(String.format("%1$s-%2$s", key, value)));

		assertThat(entryStrings).containsExactlyInAnyOrder("1-TEST", "2-MOCK");
	}

	@Test
	public void regionGetAllKeysIsCorrect() {

		Map<Object, Object> expectedResults = MapBuilder.newMapBuilder()
			.put(1, "ONE")
			.put(2, "TWO")
			.put(3, "THREE")
			.put(4, "FOUR")
			.put(5, null)
			.build();

		List<Object> keys = Arrays.asList(null, 1, 2, null, 3, null, 4, 5);

		CacheLoader<Object, Object> mockCacheLoader = mock(CacheLoader.class);

		doAnswer(invocation -> {

			LoaderHelper<Object, Object> loaderHelper = invocation.getArgument(0);

			Object key = loaderHelper.getKey();

			return key != null && key.equals(3) ? "THREE" : null;

		}).when(mockCacheLoader).load(any(LoaderHelper.class));

		RegionAttributes<Object, Object> mockRegionAttributes = this.mockRegion.getAttributes();

		doReturn(mockCacheLoader).when(mockRegionAttributes).getCacheLoader();

		this.mockRegion.put(1, "ONE");
		this.mockRegion.put(2, "TWO");
		this.mockRegion.put(4, "FOUR");

		Map<Object, Object> actualResults = this.mockRegion.getAll(keys);

		assertThat(actualResults).isNotNull();
		assertThat(actualResults).isEqualTo(expectedResults);

		verify(this.mockRegion, times(5)).get(isNotNull());
		verify(mockCacheLoader, times(2)).load(isA(LoaderHelper.class));
		verify(mockRegionAttributes, times(2)).getCacheLoader();
		verifyNoMoreInteractions(mockCacheLoader);
	}

	@Test
	public void mapGetOrDefaultIsCorrect() {

		this.mockRegion.put(1, "TEST");

		assertThat(this.mockRegion.getOrDefault(1, "MOCK")).isEqualTo("TEST");
		assertThat(this.mockRegion.getOrDefault(2, "MOCK")).isEqualTo("MOCK");
	}

	@Test
	public void mapRegionIsEmptyIsCorrect() {

		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion).isEmpty();

		this.mockRegion.put(1, "TEST");

		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion).isNotEmpty();

		this.mockRegion.remove(1);

		assertThat(this.mockRegion).hasSize(0);
		assertThat(this.mockRegion).isEmpty();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void mapRegionKeySetIsCorrect() {

		Set keys = this.mockRegion.keySet();

		assertThat(keys).isNotNull();
		assertThat(keys).isEmpty();

		this.mockRegion.put(1, "TEST");

		keys = this.mockRegion.keySet();

		assertThat(keys).isNotNull();
		assertThat(keys).hasSize(1);
		assertThat(keys).containsExactlyInAnyOrder(1);

		this.mockRegion.put(2, "MOCK");

		keys = this.mockRegion.keySet();

		assertThat(keys).isNotNull();
		assertThat(keys).hasSize(2);
		assertThat(keys).containsExactlyInAnyOrder(1, 2);

		this.mockRegion.remove(1);

		keys = this.mockRegion.keySet();

		assertThat(keys).isNotNull();
		assertThat(keys).hasSize(1);
		assertThat(keys).containsExactlyInAnyOrder(2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mapRegionKeySetIsUnmodifiable() {
		this.mockRegion.keySet().add(1);
	}

	@Test
	public void regionLocalClearCallsRegionClear() {

		assertThat(this.mockRegion).isEmpty();

		this.mockRegion.put(1, "TEST");

		assertThat(this.mockRegion).hasSize(1);

		this.mockRegion.localClear();

		assertThat(this.mockRegion).isEmpty();

		verify(this.mockRegion, times(1)).clear();
	}

	@Test
	public void regionLocalInvalidateCallsRegionInvalidate() {

		this.mockRegion.put(1, "TEST");

		assertThat(this.mockRegion.get(1)).isEqualTo("TEST");

		this.mockRegion.localInvalidate(1);

		assertThat(this.mockRegion.get(1)).isNull();

		verify(this.mockRegion, times(1)).invalidate(eq(1));
	}

	@Test
	public void mapRegionPutAllIsCorrect() {

		Map<Object, Object> map = MapBuilder.newMapBuilder()
			.put(1, "TEST")
			.put(2, "MOCK")
			.build();

		assertThat(this.mockRegion).isEmpty();

		this.mockRegion.putAll(map);

		assertThat(this.mockRegion).hasSize(map.size());
		assertThat(this.mockRegion.getAll(map.keySet())).isEqualTo(map);

		verify(this.mockRegion, times(1)).put(eq(1), eq("TEST"));
		verify(this.mockRegion, times(1)).put(eq(2), eq("MOCK"));
	}

	@Test
	public void regionRemoveAllKeysIsCorrect() {

		this.mockRegion.put(1, "TEST");
		this.mockRegion.put(2, "MOCK");
		this.mockRegion.put(3, "NULL");

		assertThat(this.mockRegion).hasSize(3);

		this.mockRegion.removeAll(Arrays.asList(1, 2));

		assertThat(this.mockRegion).hasSize(1);
		assertThat(this.mockRegion).doesNotContainKeys(1, 2);
		assertThat(this.mockRegion.get(3)).isEqualTo("NULL");

		verify(this.mockRegion, times(1)).remove(eq(1));
		verify(this.mockRegion, times(1)).remove(eq(2));
		verify(this.mockRegion, never()).remove(eq(3));
	}

	@Test
	public void mapRegionValuesIsCorrect() {

		this.mockRegion.put(1, "TEST");
		this.mockRegion.put(2, "MOCK");
		this.mockRegion.put(3, "NULL");

		Collection<Object> values = this.mockRegion.values();

		assertThat(values).isNotNull();
		assertThat(values).hasSize(3);
		assertThat(values).containsExactlyInAnyOrder("TEST", "MOCK", "NULL");

		this.mockRegion.put(3, "THREE");
		this.mockRegion.put(4, "FOUR");

		values = this.mockRegion.values();

		assertThat(values).isNotNull();
		assertThat(values).hasSize(4);
		assertThat(values).containsExactlyInAnyOrder("TEST", "MOCK", "THREE", "FOUR");

		this.mockRegion.removeAll(Arrays.asList(3, 4));

		values = this.mockRegion.values();

		assertThat(values).isNotNull();
		assertThat(values).hasSize(2);
		assertThat(values).containsExactlyInAnyOrder("TEST", "MOCK");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void mapRegionValuesIsUnmodifiable() {
		this.mockRegion.values().add("TEST");
	}
}
