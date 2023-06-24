/*
 *  Copyright 2017-present the original author or authors.
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
package org.springframework.data.gemfire.tests.util;

import java.io.FileFilter;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Java {@link FileFilter} implementation capable of being composed
 *
 * @author John Blum
 * @see java.lang.FunctionalInterface
 * @see java.io.FileFilter
 * @since 0.3.5-RAJ
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ComposableFileFilter extends FileFilter {

	default @NonNull ComposableFileFilter andThen(@Nullable ComposableFileFilter fileFilter) {
		return fileFilter == null ? this
			: file -> this.accept(file) && fileFilter.accept(file);
	}

	default @NonNull ComposableFileFilter orThen(@Nullable ComposableFileFilter fileFilter) {
		return fileFilter == null ? this
			: file -> this.accept(file) || fileFilter.accept(file);
	}
}
