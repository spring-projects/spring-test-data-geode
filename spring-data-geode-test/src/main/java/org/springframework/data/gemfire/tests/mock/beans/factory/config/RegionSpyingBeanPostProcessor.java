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
package org.springframework.data.gemfire.tests.mock.beans.factory.config;

import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.geode.cache.Region;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Spring {@link BeanPostProcessor} that creates spies for all managed {@link Region Regions} (beans)
 * in the Spring {@link ApplicationContext}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.mockito.Mockito#spy(Object)
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationContext
 * @since 0.0.22
 */
@SuppressWarnings("unused")
public class RegionSpyingBeanPostProcessor implements BeanPostProcessor {

	private final Set<String> regionBeanNames;

	public RegionSpyingBeanPostProcessor(String... regionBeanNames) {
		this(Arrays.asList(ArrayUtils.nullSafeArray(regionBeanNames, String.class)));
	}

	public RegionSpyingBeanPostProcessor(@NonNull Iterable<String> regionBeanNames) {

		this.regionBeanNames =
			StreamSupport.stream(CollectionUtils.nullSafeIterable(regionBeanNames).spliterator(), false)
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet());
	}

	protected boolean isRegion(@Nullable Object target) {
		return target instanceof Region;
	}

	protected boolean isRegionBeanNameMatch(@NonNull String beanName) {

		return this.regionBeanNames.isEmpty()
			|| (StringUtils.hasText(beanName) && this.regionBeanNames.contains(beanName));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
		return isRegion(bean) && isRegionBeanNameMatch(beanName) ? doSpy(bean) : bean;
	}

	protected @Nullable <T> T doSpy(@Nullable T target) {
		return target != null ? spy(target) : target;
	}
}
