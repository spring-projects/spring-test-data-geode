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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.geode.cache.DiskStore;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.gemfire.tests.integration.annotation.GemFireGarbageCollectorConfiguration;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring {@link ApplicationListener} implementation used to listen for and trigger the GemFire/Geode garbage collection
 * process.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.io.FileFilter
 * @see org.apache.geode.cache.DiskStore
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.data.gemfire.tests.integration.annotation.GemFireGarbageCollectorConfiguration
 * @see org.springframework.test.context.event.AfterTestClassEvent
 * @since 0.0.17
 */
public class GemFireGarbageCollectorApplicationListener
		implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

	protected static final File DEFAULT_SEARCH_DIRECTORY = FileSystemUtils.WORKING_DIRECTORY;

	/**
	 * Factory method used to construct a new instance of {@link GemFireGarbageCollectorApplicationListener}
	 * initialized with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode garbage collection process.
	 *
	 * By default, the search will begin in the application working directory.
	 *
	 * @param gemfireGarbageCollectorEventTypes array of {@link ApplicationEvent} objects triggering the GemFire/Geode
	 * garbage collection process.
	 * @return a new instance of {@link GemFireGarbageCollectorApplicationListener}.
	 * @see #GemFireGarbageCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 */
	@SuppressWarnings("unchecked")
	public static GemFireGarbageCollectorApplicationListener create(
			@Nullable Class<? extends ApplicationEvent>... gemfireGarbageCollectorEventTypes) {

		return create(DEFAULT_SEARCH_DIRECTORY,
			Arrays.asList(ArrayUtils.nullSafeArray(gemfireGarbageCollectorEventTypes, Class.class)));
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireGarbageCollectorApplicationListener}
	 * initialized with the {@link File filesystem directory location} used to begin the search and collection of
	 * GemFire/Geode garbage along with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode garbage
	 * collection process.
	 *
	 * @param searchDirectory {@link File} referring to the filesystem directory location to begin the search;
	 * defaults to the application working directory if {@link File} is {@literal null}.
	 * @param gemfireGarbageCollectorEventTypes array of {@link ApplicationEvent} objects triggering the GemFire/Geode
	 * garbage collection process.
	 * @return a new instance of {@link GemFireGarbageCollectorApplicationListener}.
	 * @see #GemFireGarbageCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 * @see java.io.File
	 */
	@SuppressWarnings("unchecked")
	public static GemFireGarbageCollectorApplicationListener create(@Nullable File searchDirectory,
			@Nullable Class<? extends ApplicationEvent>... gemfireGarbageCollectorEventTypes) {

		return create(searchDirectory, Arrays.asList(ArrayUtils.nullSafeArray(gemfireGarbageCollectorEventTypes, Class.class)));
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireGarbageCollectorApplicationListener}
	 * initialized with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode garbage collection process.
	 *
	 * By default, the search will begin in the application working directory.
	 *
	 * @param gemfireGarbageCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects triggering
	 * the GemFire/Geode garbage collection process.
	 * @return a new instance of {@link GemFireGarbageCollectorApplicationListener}.
	 * @see #GemFireGarbageCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public static GemFireGarbageCollectorApplicationListener create(
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireGarbageCollectorEventTypes) {

		return new GemFireGarbageCollectorApplicationListener(gemfireGarbageCollectorEventTypes);
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireGarbageCollectorApplicationListener}
	 * initialized with the {@link File filesystem directory location} used to begin the search and collection of
	 * GemFire/Geode garbage along with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode garbage
	 * collection process.
	 *
	 * @param searchDirectory {@link File} referring to the filesystem directory location to begin the search;
	 * defaults to the application working directory if {@link File} is {@literal null}.
	 * @param gemfireGarbageCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects triggering
	 * the GemFire/Geode garbage collection process.
	 * @return a new instance of {@link GemFireGarbageCollectorApplicationListener}.
	 * @see #GemFireGarbageCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 * @see java.io.File
	 */
	public static GemFireGarbageCollectorApplicationListener create(File searchDirectory,
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireGarbageCollectorEventTypes) {

		return new GemFireGarbageCollectorApplicationListener(searchDirectory, gemfireGarbageCollectorEventTypes);
	}

	private boolean tryCleanDiskStoreFilesEnabled = GemFireGarbageCollectorConfiguration.DEFAULT_CLEAN_DISK_STORE_FILES;

	private ApplicationContext applicationContext;

	private final File searchDirectory;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Set<Class<? extends ApplicationEvent>> gemfireGarbageCollectorEventTypes;

	/**
	 * Constructs a new instance of {@link GemFireGarbageCollectorApplicationListener} initialized with
	 * an {@link Iterable} of {@link ApplicationEvent} objects that trigger a GemFire/Geode garbage collection.
	 *
	 * The search will begin in the application working directory.
	 *
	 * @param gemfireGarbageCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects that trigger
	 * GemFire/Geode garbage collection.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public GemFireGarbageCollectorApplicationListener(
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireGarbageCollectorEventTypes) {

		this(FileSystemUtils.WORKING_DIRECTORY, gemfireGarbageCollectorEventTypes);
	}

	/**
	 * Constructs a new instance of {@link GemFireGarbageCollectorApplicationListener} initialized with
	 * a {@link File} referring to the directory to begin the search for GemFire/Geode garbage along with
	 * an {@link Iterable} of {@link ApplicationEvent} objects that trigger a GemFire/Geode garbage collection.
	 *
	 * @param searchDirectory {@link File} referring to the directory begin collecting GemFire/Geode garbage.
	 * @param gemfireGarbageCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects that trigger
	 * GemFire/Geode garbage collection.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public GemFireGarbageCollectorApplicationListener(@Nullable File searchDirectory,
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireGarbageCollectorEventTypes) {

		this.searchDirectory = searchDirectory != null ? searchDirectory : DEFAULT_SEARCH_DIRECTORY;

		Set<Class<? extends ApplicationEvent>> resolvedGemFireGarbageCollectorEventTypes =
			StreamSupport.stream(CollectionUtils.nullSafeIterable(gemfireGarbageCollectorEventTypes).spliterator(), false)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		resolvedGemFireGarbageCollectorEventTypes = !resolvedGemFireGarbageCollectorEventTypes.isEmpty()
			? resolvedGemFireGarbageCollectorEventTypes
			: Collections.singleton(AfterTestClassEvent.class);

		this.gemfireGarbageCollectorEventTypes = Collections.unmodifiableSet(resolvedGemFireGarbageCollectorEventTypes);
	}

	/**
	 * Configures a reference to the Spring {@link ApplicationContext} in which this {@link ApplicationListener}
	 * is registered.
	 *
	 * @param applicationContext reference to the Spring {@link ApplicationContext}.
	 * @see org.springframework.context.ApplicationContext
	 */
	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Returns an {@link Optional} reference to the configured Spring {@link ApplicationContext}.
	 *
	 * @return an {@link Optional} reference to the configured Spring {@link ApplicationContext}.
	 * @see org.springframework.context.ApplicationContext
	 * @see #setApplicationContext(ApplicationContext)
	 * @see java.util.Optional
	 */
	protected Optional<ApplicationContext> getApplicationContext() {
		return Optional.ofNullable(this.applicationContext);
	}

	/**
	 * Returns a configured {@link Set} of {@link ApplicationEvent} {@link Class types} that trigger the GemFire/Geode
	 * garbage collection process.
	 *
	 * @return a configured {@link Set} of {@link ApplicationEvent} {@link Class types} that trigger the GemFire/Geode
	 * garbage collection process.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.util.Set
	 */
	protected @NonNull Set<Class<? extends ApplicationEvent>> getConfiguredGemFireGarbageCollectorEventTypes() {
		return this.gemfireGarbageCollectorEventTypes;
	}

	/**
	 * Determines whether the given {@link ApplicationEvent} is a configured event for triggering the GemFire/Geode
	 * garbage collection process.
	 *
	 * @param event {@link ApplicationEvent} to evaluate.
	 * @return a boolean value determining whether the given {@link ApplicationEvent} is a configured event
	 * for triggering the GemFire/Geode garbage collection process.
	 * @see org.springframework.context.ApplicationEvent
	 * @see #getConfiguredGemFireGarbageCollectorEventTypes()
	 */
	protected boolean isGemFireGarbageCollectorEvent(@Nullable ApplicationEvent event) {

		for (Class<? extends ApplicationEvent> gemfireGarbageCollectorEventType
			: getConfiguredGemFireGarbageCollectorEventTypes()) {

			if (gemfireGarbageCollectorEventType.isInstance(event)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the configured SLF4J {@link Logger} to log events and messages originating from this
	 * {@link ApplicationListener} during the GemFire/Geode garbage collection process.
	 *
	 * @return the configured SLF4 {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	/**
	 * Returns the configured {@link File directory} referring to the filesystem location to begin the search for
	 * GemFire/Geode garbage.
	 *
	 * @return the configured {@link File directory} referring to the filesystem location to begin the search for
	 * GemFire/Geode garbage.
	 * @see java.io.File
	 */
	protected @NonNull File getSearchDirectory() {
		return this.searchDirectory;
	}

	/**
	 * Determines whether this {@link ApplicationListener GemFire/Geode Garbage Collector} should try to cleanup all
	 * {@link DiskStore} {@link File Files} as well.
	 *
	 * {@link DiskStore} {@link File Files} are maintained in {@link DiskStore#getDiskDirs()}.
	 *
	 * @return a boolean value indicating whether this {@link ApplicationListener GemFire/Geode Garbage Collector}
	 * should try to cleanup all {@link DiskStore} {@link File Files}.
	 */
	protected boolean isTryCleanDiskStoreFilesEnabled() {
		return this.tryCleanDiskStoreFilesEnabled;
	}

	/**
	 * Handles a Spring Container {@link ApplicationEvent} by determining whether the event is a configured event
	 * for triggering the GemFire/Geode garbage collection.
	 *
	 * @param event {@link ApplicationEvent} to evaluate.
	 * @see #isGemFireGarbageCollectorEvent(ApplicationEvent)
	 * @see #collectGemFireGarbage(File)
	 * @see org.springframework.context.ApplicationEvent
	 */
	@Override
	public void onApplicationEvent(@NonNull ApplicationEvent event) {

		if (isGemFireGarbageCollectorEvent(event)) {
			collectGemFireGarbage(getSearchDirectory());
		}

		if (isTryCleanDiskStoreFilesEnabled())
			collectGemFireDiskStoreFiles();
	}

	/**
	 * A {@link File} referring to the filesystem directory location to begin the search for and collection of
	 * GemFire/Geode garbage.
	 *
	 * @param directory {@link File} referring to the filesystem directory location to begin the search
	 * and collection process; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link File} is {@literal null} or not a directory.
	 * @see java.io.File
	 */
	protected void collectGemFireGarbage(@NonNull File directory) {

		Assert.isTrue(FileSystemUtils.isDirectory(directory),
			() -> String.format("File [%s] must be a directory", directory));

		for (File file : FileSystemUtils.safeListFiles(directory, GemFireGarbageFileFilter.INSTANCE)) {
			if (FileSystemUtils.isDirectory(file)) {
				FileSystemUtils.deleteRecursive(file);
			}
			else {
				file.delete();
			}
		}

		if (FileSystemUtils.isEmpty(directory)) {
			directory.delete();
		}
	}

	/**
	 * Collects and cleans up all {@link File Files} created from the {@link DiskStore DiskStores} created by
	 * the application.
	 */
	protected void collectGemFireDiskStoreFiles() {

		getApplicationContext()
			.map(it -> it.getBeansOfType(DiskStore.class))
			.map(Map::values)
			.ifPresent(diskStores -> diskStores.stream()
				.filter(Objects::nonNull)
				.forEach(diskStore -> {
					for (File directory : ArrayUtils.nullSafeArray(diskStore.getDiskDirs(), File.class)) {
						try {
							FileSystemUtils.deleteRecursive(directory);
						}
						catch (Throwable ignore) {
							getLogger().warn("Unable to delete DiskStore directory [{}]", directory);
						}
					}
				}));
	}

	/**
	 * Configures whether to try and cleanup all {@link File Files} generated from managed {@link DiskStore DiskStores}.
	 *
	 * @param enable boolean value dis/enabling {@link DiskStore} {@link File} cleanup.
	 * @return this {@link GemFireGarbageCollectorApplicationListener}.
	 */
	public GemFireGarbageCollectorApplicationListener tryCleanDiskStoreFiles(boolean enable) {
		this.tryCleanDiskStoreFilesEnabled = enable;
		return this;
	}

	/**
	 * {@link FileFilter} implementation matching {@link File directories} or GemFire/Geode garbage.
	 */
	protected static class GemFireGarbageFileFilter implements FileFilter {

		protected static final GemFireGarbageFileFilter INSTANCE = new GemFireGarbageFileFilter();

		protected static final FileFilter DIRECTORY_FILE_FILTER = FileSystemUtils.DirectoryOnlyFilter.INSTANCE;

		// https://gemfire.docs.pivotal.io/910/geode/managing/disk_storage/file_names_and_extensions.html
		private static final FileFilter GEMFIRE_FILE_EXTENSION_FILTER = FileSystemUtils.CompositeFileFilter.or(
			new FileSystemUtils.FileExtensionFilter(".dat"),
			new FileSystemUtils.FileExtensionFilter(".gfs"),
			new FileSystemUtils.FileExtensionFilter(".crf"),
			new FileSystemUtils.FileExtensionFilter(".drf"),
			new FileSystemUtils.FileExtensionFilter(".if"),
			new FileSystemUtils.FileExtensionFilter(".krf"),
			new FileSystemUtils.FileExtensionFilter(".lk"),
			new FileSystemUtils.FileExtensionFilter(".log"),
			new FileSystemUtils.FileExtensionFilter(".pid"),
			new FileSystemUtils.FileExtensionFilter(".properties"),
			new FileSystemUtils.FileExtensionFilter(".xml")
		);

		private static final Set<String> GEMFIRE_FILE_NAMES =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
				"backup",
				"cache",
				"configdiskdir",
				"default",
				"drlk_if",
				"gfsecurity",
				"gemfire",
				"geode",
				"locator",
				"overflow"
			)));

		private static final Predicate<File> GEMFIRE_FILE_NAME_FILTER = file ->
			Objects.nonNull(file) && GEMFIRE_FILE_NAMES.stream().anyMatch(file.getName().toLowerCase()::startsWith);

		protected FileFilter getDirectoryFileFilter() {
			return DIRECTORY_FILE_FILTER;
		}

		protected FileFilter getGemFireFileExtensionFilter() {
			return GEMFIRE_FILE_EXTENSION_FILTER;
		}

		protected Predicate<File> getGemFireFilenameFilter() {
			return GEMFIRE_FILE_NAME_FILTER;
		}

		@Override
		public boolean accept(File pathname) {
			return getDirectoryFileFilter().accept(pathname) || isGemFireGarbage(pathname);
		}

		protected boolean isGemFireGarbage(@Nullable File file) {
			return Objects.nonNull(file) && isFileExtensionOrFileNameMatch(file);
		}

		protected boolean isFileExtensionOrFileNameMatch(@NonNull File file) {
			return getGemFireFileExtensionFilter().accept(file) || getGemFireFilenameFilter().test(file);
		}
	}
}
