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
import java.time.Duration;
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
import org.springframework.data.gemfire.tests.integration.annotation.GemFireResourceCollectorConfiguration;
import org.springframework.data.gemfire.tests.util.FileSystemUtils;
import org.springframework.data.gemfire.tests.util.ThreadUtils;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring {@link ApplicationListener} implementation used to listen for and trigger the GemFire/Geode
 * resource collection algorithm.
 *
 * @author John Blum
 * @see java.io.File
 * @see java.io.FileFilter
 * @see java.util.function.Predicate
 * @see org.apache.geode.cache.DiskStore
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.data.gemfire.tests.integration.annotation.GemFireResourceCollectorConfiguration
 * @see org.springframework.test.context.event.AfterTestClassEvent
 * @since 0.0.17
 */
public class GemFireResourceCollectorApplicationListener
		implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {

	protected static final int DEFAULT_DELETE_ATTEMPTS = 2;

	protected static final long DEFAULT_DELETE_TIMED_WAIT_INTERVAL = Duration.ofMillis(250).toMillis();

	protected static final File DEFAULT_SEARCH_DIRECTORY = FileSystemUtils.WORKING_DIRECTORY;

	/**
	 * Factory method used to construct a new instance of {@link GemFireResourceCollectorApplicationListener}
	 * initialized with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode
	 * resource collection algorithm.
	 *
	 * By default, the search will begin in the application working directory.
	 *
	 * @param gemfireResourceCollectorEventTypes array of {@link ApplicationEvent} objects triggering the GemFire/Geode
	 * resource collection algorithm.
	 * @return a new instance of {@link GemFireResourceCollectorApplicationListener}.
	 * @see #GemFireResourceCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 */
	@SuppressWarnings("unchecked")
	public static GemFireResourceCollectorApplicationListener create(
			@Nullable Class<? extends ApplicationEvent>... gemfireResourceCollectorEventTypes) {

		return create(DEFAULT_SEARCH_DIRECTORY,
			Arrays.asList(ArrayUtils.nullSafeArray(gemfireResourceCollectorEventTypes, Class.class)));
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireResourceCollectorApplicationListener}
	 * initialized with the {@link File filesystem directory location} used to begin the search and collection of
	 * GemFire/Geode resources along with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode
	 * resource collection algorithm.
	 *
	 * @param searchDirectory {@link File} referring to the filesystem directory location to begin the search;
	 * defaults to the application working directory if {@link File} is {@literal null}.
	 * @param gemfireResourceCollectorEventTypes array of {@link ApplicationEvent} objects triggering the GemFire/Geode
	 * resource collection algorithm.
	 * @return a new instance of {@link GemFireResourceCollectorApplicationListener}.
	 * @see #GemFireResourceCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 * @see java.io.File
	 */
	@SuppressWarnings("unchecked")
	public static GemFireResourceCollectorApplicationListener create(@Nullable File searchDirectory,
			@Nullable Class<? extends ApplicationEvent>... gemfireResourceCollectorEventTypes) {

		return create(searchDirectory, Arrays.asList(ArrayUtils.nullSafeArray(gemfireResourceCollectorEventTypes, Class.class)));
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireResourceCollectorApplicationListener}
	 * initialized with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode
	 * resource collection algorithm.
	 *
	 * By default, the search will begin in the application working directory.
	 *
	 * @param gemfireResourceCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects triggering
	 * the GemFire/Geode resource collection algorithm.
	 * @return a new instance of {@link GemFireResourceCollectorApplicationListener}.
	 * @see #GemFireResourceCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public static GemFireResourceCollectorApplicationListener create(
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireResourceCollectorEventTypes) {

		return new GemFireResourceCollectorApplicationListener(gemfireResourceCollectorEventTypes);
	}

	/**
	 * Factory method used to construct a new instance of {@link GemFireResourceCollectorApplicationListener}
	 * initialized with the {@link File filesystem directory location} used to begin the search and collection of
	 * GemFire/Geode resources along with the {@link ApplicationEvent} objects that trigger the Gemfire/Geode
	 * resource collection algorithm.
	 *
	 * @param searchDirectory {@link File} referring to the filesystem directory location to begin the search;
	 * defaults to the application working directory if {@link File} is {@literal null}.
	 * @param gemfireResourceCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects triggering
	 * the GemFire/Geode resource collection algorithm.
	 * @return a new instance of {@link GemFireResourceCollectorApplicationListener}.
	 * @see #GemFireResourceCollectorApplicationListener(File, Iterable)
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 * @see java.io.File
	 */
	public static GemFireResourceCollectorApplicationListener create(File searchDirectory,
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireResourceCollectorEventTypes) {

		return new GemFireResourceCollectorApplicationListener(searchDirectory, gemfireResourceCollectorEventTypes);
	}

	private boolean tryCleanDiskStoreFilesEnabled = GemFireResourceCollectorConfiguration.DEFAULT_CLEAN_DISK_STORE_FILES;

	private ApplicationContext applicationContext;

	private final File searchDirectory;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Set<Class<? extends ApplicationEvent>> gemfireResourceCollectorEventTypes;

	/**
	 * Constructs a new instance of {@link GemFireResourceCollectorApplicationListener} initialized with
	 * an {@link Iterable} of {@link ApplicationEvent} objects triggering the GemFire/Geode
	 * resource collection algorithm.
	 *
	 * The search will begin in the application working directory.
	 *
	 * @param gemfireResourceCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects that trigger
	 * GemFire/Geode resource collection.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public GemFireResourceCollectorApplicationListener(
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireResourceCollectorEventTypes) {

		this(DEFAULT_SEARCH_DIRECTORY, gemfireResourceCollectorEventTypes);
	}

	/**
	 * Constructs a new instance of {@link GemFireResourceCollectorApplicationListener} initialized with
	 * a {@link File} referring to the directory to begin the search for GemFire/Geode resources along with
	 * an {@link Iterable} of {@link ApplicationEvent} objects triggering the GemFire/Geode resource collection
	 * algorithm.
	 *
	 * @param searchDirectory {@link File} referring to the directory begin collecting GemFire/Geode resources.
	 * @param gemfireResourceCollectorEventTypes {@link Iterable} of {@link ApplicationEvent} objects that trigger
	 * GemFire/Geode resource collection.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.lang.Iterable
	 */
	public GemFireResourceCollectorApplicationListener(@Nullable File searchDirectory,
			@Nullable Iterable<Class<? extends ApplicationEvent>> gemfireResourceCollectorEventTypes) {

		this.searchDirectory = searchDirectory != null ? searchDirectory : DEFAULT_SEARCH_DIRECTORY;

		Set<Class<? extends ApplicationEvent>> resolvedGemFireResourceCollectorEventTypes =
			StreamSupport.stream(CollectionUtils.nullSafeIterable(gemfireResourceCollectorEventTypes).spliterator(), false)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		resolvedGemFireResourceCollectorEventTypes = !resolvedGemFireResourceCollectorEventTypes.isEmpty()
			? resolvedGemFireResourceCollectorEventTypes
			: Collections.singleton(AfterTestClassEvent.class);

		this.gemfireResourceCollectorEventTypes = Collections.unmodifiableSet(resolvedGemFireResourceCollectorEventTypes);
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
	 * resource collection algorithm.
	 *
	 * @return a configured {@link Set} of {@link ApplicationEvent} {@link Class types} that trigger the GemFire/Geode
	 * resource collection algorithm.
	 * @see org.springframework.context.ApplicationEvent
	 * @see java.util.Set
	 */
	protected @NonNull Set<Class<? extends ApplicationEvent>> getConfiguredGemFireResourceCollectorEventTypes() {
		return this.gemfireResourceCollectorEventTypes;
	}

	/**
	 * Determines whether the given {@link ApplicationEvent} is a configured event for triggering the GemFire/Geode
	 * resource collection algorithm.
	 *
	 * @param event {@link ApplicationEvent} to evaluate.
	 * @return a boolean value determining whether the given {@link ApplicationEvent} is a configured event
	 * for triggering the GemFire/Geode resource collection algorithm.
	 * @see org.springframework.context.ApplicationEvent
	 * @see #getConfiguredGemFireResourceCollectorEventTypes()
	 */
	protected boolean isGemFireResourceCollectorEvent(@Nullable ApplicationEvent event) {

		for (Class<? extends ApplicationEvent> gemfireResourceCollectorEventType
			: getConfiguredGemFireResourceCollectorEventTypes()) {

			if (gemfireResourceCollectorEventType.isInstance(event)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the configured SLF4J {@link Logger} to log events and messages originating from this
	 * {@link ApplicationListener} during the GemFire/Geode resource collection algorithm.
	 *
	 * @return the configured SLF4 {@link Logger}.
	 * @see org.slf4j.Logger
	 */
	protected @NonNull Logger getLogger() {
		return this.logger;
	}

	/**
	 * Returns the configured {@link File directory} referring to the filesystem location to begin the search for
	 * GemFire/Geode resources and other garbage.
	 *
	 * @return the configured {@link File directory} referring to the filesystem location to begin the search for
	 * GemFire/Geode resources and other garbage.
	 * @see java.io.File
	 */
	protected @NonNull File getSearchDirectory() {
		return this.searchDirectory;
	}

	/**
	 * Determines whether this {@link ApplicationListener GemFire/Geode Resource Collector} should try to cleanup all
	 * {@link DiskStore} {@link File Files} as well.
	 *
	 * {@link DiskStore} {@link File Files} are maintained in {@link DiskStore#getDiskDirs()}.
	 *
	 * @return a boolean value indicating whether this {@link ApplicationListener GemFire/Geode Resource Collector}
	 * should try to cleanup all {@link DiskStore} {@link File Files}.
	 */
	protected boolean isTryCleanDiskStoreFilesEnabled() {
		return this.tryCleanDiskStoreFilesEnabled;
	}

	/**
	 * Handles a Spring Container {@link ApplicationEvent} by determining whether the event is a configured event
	 * for triggering the GemFire/Geode resource collection.
	 *
	 * @param event {@link ApplicationEvent} to evaluate.
	 * @see #isGemFireResourceCollectorEvent(ApplicationEvent)
	 * @see #collectGemFireResources(File)
	 * @see org.springframework.context.ApplicationEvent
	 */
	@Override
	public void onApplicationEvent(@NonNull ApplicationEvent event) {

		if (isGemFireResourceCollectorEvent(event)) {
			collectGemFireResources(getSearchDirectory());
		}

		if (isTryCleanDiskStoreFilesEnabled())
			collectGemFireDiskStoreFiles();
	}

	/**
	 * A {@link File} referring to the filesystem directory location to begin the search for and collection of
	 * GemFire/Geode resources and other garbage.
	 *
	 * @param directory {@link File} referring to the filesystem directory location to begin the search
	 * and resource collection process; must not be {@literal null}.
	 * @throws IllegalArgumentException if {@link File} is {@literal null} or not a directory.
	 * @see java.io.File
	 */
	protected void collectGemFireResources(@NonNull File directory) {

		Assert.isTrue(FileSystemUtils.isDirectory(directory),
			() -> String.format("File [%s] must be a directory", directory));

		for (File file : FileSystemUtils.safeListFiles(directory, GemFireResourceFileFilter.INSTANCE)) {
			if (FileSystemUtils.isDirectory(file)) {
				collectGemFireResources(file);
			}
			else {
				if (!tryDelete(file)) {
					file.deleteOnExit();
				}
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
	 * @return this {@link GemFireResourceCollectorApplicationListener}.
	 */
	public GemFireResourceCollectorApplicationListener tryCleanDiskStoreFiles(boolean enable) {
		this.tryCleanDiskStoreFilesEnabled = enable;
		return this;
	}

	/**
	 * Tries to delete the given {@link File} referring to a single {@link File#isFile() "file"}
	 * (not a {@literal directory}) in the file system.
	 *
	 * @param file {@link File} to delete.
	 * @return a boolean value indicating whether the given {@link File} was successfully deleted.
	 * @see #tryDelete(File, int, long)
	 * @see java.io.File
	 */
	protected boolean tryDelete(@Nullable File file) {
		return tryDelete(file, DEFAULT_DELETE_ATTEMPTS, DEFAULT_DELETE_TIMED_WAIT_INTERVAL);
	}

	/**
	 * Tries to delete the given {@link File} referring to a single {@link File#isFile() "file"}
	 * (not a {@literal directory}) in the file system.
	 *
	 * @param file {@link File} to delete.
	 * @param attempts {@link Integer} indicating the number of attemps to try and delete the {@link File}.
	 * @param waitDurationBetweenAttempts {@link Long} indicating the number of milliseconds to wait
	 * between delete attempts.
	 * @return a boolean value indicating whether the given {@link File} was successfully deleted.
	 * @see java.io.File
	 */
	@SuppressWarnings("all")
	protected boolean tryDelete(@Nullable File file, int attempts, long waitDurationBetweenAttempts) {

		if (FileSystemUtils.isFile(file)) {

			long interval = waitDurationBetweenAttempts;
			long duration = attempts * interval;

			return ThreadUtils.timedWait(duration, interval, file::delete);
		}

		return false;
	}

	/**
	 * {@link FileFilter} implementation matching {@link File directories} or GemFire/Geode resources (e.g. files).
	 */
	protected static class GemFireResourceFileFilter implements FileFilter {

		protected static final GemFireResourceFileFilter INSTANCE = new GemFireResourceFileFilter();

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
			return getDirectoryFileFilter().accept(pathname) || isGemFireResource(pathname);
		}

		protected boolean isGemFireResource(@Nullable File file) {
			return Objects.nonNull(file) && isFileExtensionOrFileNameMatch(file);
		}

		protected boolean isFileExtensionOrFileNameMatch(@NonNull File file) {
			return getGemFireFileExtensionFilter().accept(file) || getGemFireFilenameFilter().test(file);
		}
	}
}
