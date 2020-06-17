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
package org.springframework.data.gemfire.tests.integration.context.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collections;

import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.gemfire.tests.integration.context.event.GemFireGarbageCollectorApplicationListener.GemFireGarbageFileFilter;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.test.context.event.AfterTestExecutionEvent;
import org.springframework.test.context.event.AfterTestMethodEvent;

/**
 * Unit Tests for {@link GemFireGarbageCollectorApplicationListener}.
 *
 * @author John Blum
 * @see java.io.File
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.data.gemfire.tests.integration.context.event.GemFireGarbageCollectorApplicationListener
 * @since 0.0.17
 */
@SuppressWarnings("unchecked")
public class GemFireGarbageCollectorApplicationListenerUnitTests {

	private static File mockDirectory(String pathname) {

		File mockFile = mock(File.class, pathname);

		doReturn(new File(pathname).getAbsolutePath()).when(mockFile).getAbsolutePath();
		doReturn(true).when(mockFile).isDirectory();
		doReturn(pathname).when(mockFile).getName();

		return mockFile;
	}

	private static File mockFile(String pathname) {

		File mockFile = mock(File.class, pathname);

		doReturn(new File(pathname).getAbsolutePath()).when(mockFile).getAbsolutePath();
		doReturn(true).when(mockFile).isFile();
		doReturn(pathname).when(mockFile).getName();

		return mockFile;
	}

	@Test
	public void constructNewGemFireGarbageCollectorApplicationListener() {

		File searchDirectory = new File("/path/to/gemfire/junk");

		Iterable<Class<? extends ApplicationEvent>> eventTypes = Collections.singleton(AfterTestMethodEvent.class);

		GemFireGarbageCollectorApplicationListener listener =
			new GemFireGarbageCollectorApplicationListener(searchDirectory, eventTypes);

		assertThat(listener).isNotNull();
		assertThat(listener.getApplicationContext().orElse(null)).isNull();
		assertThat(listener.getConfiguredGemFireGarbageCollectorEventTypes())
			.containsExactly(AfterTestMethodEvent.class);
		assertThat(listener.getLogger()).isNotNull();
		assertThat(listener.getLogger().getName()).isEqualTo(GemFireGarbageCollectorApplicationListener.class.getName());
		assertThat(listener.isTryCleanDiskStoreFilesEnabled()).isFalse();
		assertThat(listener.getSearchDirectory()).isEqualTo(searchDirectory);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createNewGemFireGarbageCollectorApplicationListener() {

		GemFireGarbageCollectorApplicationListener listener =
			GemFireGarbageCollectorApplicationListener.create(AfterTestExecutionEvent.class)
				.tryCleanDiskStoreFiles(true);

		assertThat(listener).isNotNull();
		assertThat(listener.getApplicationContext().orElse(null)).isNull();
		assertThat(listener.getConfiguredGemFireGarbageCollectorEventTypes())
			.containsExactly(AfterTestExecutionEvent.class);
		assertThat(listener.getLogger()).isNotNull();
		assertThat(listener.getLogger().getName()).isEqualTo(GemFireGarbageCollectorApplicationListener.class.getName());
		assertThat(listener.isTryCleanDiskStoreFilesEnabled()).isTrue();
		assertThat(listener.getSearchDirectory())
			.isEqualTo(GemFireGarbageCollectorApplicationListener.DEFAULT_SEARCH_DIRECTORY);
	}

	@Test
	@SuppressWarnings("all")
	public void setAndGetApplicationContext() {

		ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

		GemFireGarbageCollectorApplicationListener listener = GemFireGarbageCollectorApplicationListener.create();

		assertThat(listener).isNotNull();
		assertThat(listener.getApplicationContext().orElse(null)).isNull();

		listener.setApplicationContext(mockApplicationContext);

		assertThat(listener.getApplicationContext().orElse(null)).isEqualTo(mockApplicationContext);

		listener.setApplicationContext(null);

		assertThat(listener.getApplicationContext().orElse(null)).isNull();
	}

	@Test
	public void onApplicationEventCallsCollectGemFireGarbageAndCollectGemFireDiskStoreFiles() {

		ApplicationEvent mockApplicationEvent = mock(ApplicationEvent.class);

		GemFireGarbageCollectorApplicationListener listener = spy(GemFireGarbageCollectorApplicationListener.create());

		doReturn(true).when(listener).isGemFireGarbageCollectorEvent(any());
		doReturn(true).when(listener).isTryCleanDiskStoreFilesEnabled();
		doNothing().when(listener).collectGemFireDiskStoreFiles();
		doNothing().when(listener).collectGemFireGarbage(any());

		listener.onApplicationEvent(mockApplicationEvent);

		verify(listener, times(1)).isGemFireGarbageCollectorEvent(eq(mockApplicationEvent));
		verify(listener, times(1))
			.collectGemFireGarbage(eq(GemFireGarbageCollectorApplicationListener.DEFAULT_SEARCH_DIRECTORY));
		verify(listener, times(1)).isTryCleanDiskStoreFilesEnabled();
		verify(listener, times(1)).collectGemFireDiskStoreFiles();
	}

	@Test
	public void isGemFireCollectorEventWithMatchingEventReturnsTrue() {

		AfterTestClassEvent mockEvent = mock(AfterTestClassEvent.class);

		assertThat(GemFireGarbageCollectorApplicationListener.create()
			.isGemFireGarbageCollectorEvent(mockEvent)).isTrue();
	}

	@Test
	public void isGemFireCollectorEventWithMatchingEventSubtypeReturnsTrue() {

		ApplicationEvent mockEvent = mock(AfterTestMethodEventSubType.class);

		assertThat(GemFireGarbageCollectorApplicationListener.create(AfterTestMethodEvent.class)
			.isGemFireGarbageCollectorEvent(mockEvent)).isTrue();
	}

	@Test
	public void isGemFireCollectorEventWithNonMatchingEventReturnsFalse() {

		ApplicationEvent mockEvent = mock(AfterTestClassEvent.class);

		assertThat(GemFireGarbageCollectorApplicationListener.create(AfterTestMethodEvent.class)
			.isGemFireGarbageCollectorEvent(mockEvent)).isFalse();
	}

	@Test
	public void isGemFireCollectorEventWithNullIsNullSafeReturnsFalse() {
		assertThat(GemFireGarbageCollectorApplicationListener.create().isGemFireGarbageCollectorEvent(null)).isFalse();
	}

	@Test
	public void gemfireGarbageFileFilterAcceptsAllDirectories() {

		GemFireGarbageFileFilter fileFilter = GemFireGarbageFileFilter.INSTANCE;

		assertThat(fileFilter).isNotNull();
		assertThat(fileFilter.accept(mockDirectory("GemFire"))).isTrue();
		assertThat(fileFilter.accept(mockDirectory("GEODE"))).isTrue();
		assertThat(fileFilter.accept(mockDirectory("Hazelcast"))).isTrue();
		assertThat(fileFilter.accept(mockDirectory("redis"))).isTrue();
	}

	@Test
	public void gemfireGarbageFileFilterAcceptsFilesWithMatchingExtension() {

		GemFireGarbageFileFilter fileFilter = GemFireGarbageFileFilter.INSTANCE;

		assertThat(fileFilter).isNotNull();
		assertThat(fileFilter.accept(mockFile("gem.dat"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.gfs"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.crf"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.drf"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.IF"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.krf"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.lk"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.lOg"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.Pid"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.proPerTies"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gem.XML"))).isTrue();
	}

	@Test
	public void gemfireGarbageFileFilterAcceptsFilesWithMatchingName() {

		GemFireGarbageFileFilter fileFilter = GemFireGarbageFileFilter.INSTANCE;

		assertThat(fileFilter).isNotNull();
		assertThat(fileFilter.accept(mockFile("BACKUP123"))).isTrue();
		assertThat(fileFilter.accept(mockFile("cache.json"))).isTrue();
		assertThat(fileFilter.accept(mockDirectory("ConfigDiskDir"))).isTrue();
		assertThat(fileFilter.accept(mockFile("DEFAULT"))).isTrue();
		assertThat(fileFilter.accept(mockFile("DRLK_IF"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gfSecurity.props"))).isTrue();
		assertThat(fileFilter.accept(mockFile("gemfire.props"))).isTrue();
		assertThat(fileFilter.accept(mockFile("Geode.junk"))).isTrue();
		assertThat(fileFilter.accept(mockFile("locator123view.tmp"))).isTrue();
		assertThat(fileFilter.accept(mockFile("OVERFLOW_tmp"))).isTrue();
	}

	@Test
	public void gemfireGarbageFileFilterDeniesFilesWithNonMatchingExtension() {

		GemFireGarbageFileFilter fileFilter = GemFireGarbageFileFilter.INSTANCE;

		assertThat(fileFilter).isNotNull();
		assertThat(fileFilter.accept(mockFile("gem.data"))).isFalse();
		assertThat(fileFilter.accept(mockFile("gem.props"))).isFalse();
		assertThat(fileFilter.accept(mockFile("gem.ifElse"))).isFalse();
		assertThat(fileFilter.accept(mockFile("gem.JSON"))).isFalse();
		assertThat(fileFilter.accept(mockFile("gem.l0g"))).isFalse();
		assertThat(fileFilter.accept(mockFile("gem.sys"))).isFalse();
		assertThat(fileFilter.accept(mockFile("log.txt"))).isFalse();
	}

	@Test
	public void gemfireGarbageFileFilterDeniesFilesWithNonMatchingName() {

		GemFireGarbageFileFilter fileFilter = GemFireGarbageFileFilter.INSTANCE;

		assertThat(fileFilter).isNotNull();
		assertThat(fileFilter.accept(mockFile("DAT.backup"))).isFalse();
		assertThat(fileFilter.accept(mockFile("Hazelcast"))).isFalse();
		assertThat(fileFilter.accept(mockFile("redis"))).isFalse();
	}

	@Test
	public void gemfireGarbageFileFilterDeniesNullFiles() {

		assertThat(GemFireGarbageFileFilter.INSTANCE).isNotNull();
		assertThat(GemFireGarbageFileFilter.INSTANCE.accept(null)).isFalse();
	}

	static class AfterTestMethodEventSubType extends AfterTestMethodEvent {

		AfterTestMethodEventSubType() {
			super(mock(TestContext.class));
		}
	}
}
