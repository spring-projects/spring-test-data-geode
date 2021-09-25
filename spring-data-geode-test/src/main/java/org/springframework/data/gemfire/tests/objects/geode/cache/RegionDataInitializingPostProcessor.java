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
package org.springframework.data.gemfire.tests.objects.geode.cache;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.geode.cache.Region;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Spring {@link Component} used to initialize an Apache Geode {@link Region} with data.
 *
 * @author John Blum
 * @see java.util.Map
 * @see java.util.function.Function
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.event.ContextRefreshedEvent
 * @see org.springframework.context.event.EventListener
 * @see org.springframework.stereotype.Component
 * @since 0.0.26
 */
@Component
@SuppressWarnings("unused")
public class RegionDataInitializingPostProcessor<T> {

	public static <T> EntityIdentifierBuilder<T> withRegion(@NonNull String regionBeanName) {
		return new EntityIdentifierBuilder<>(new RegionDataInitializingPostProcessor<>(regionBeanName));
	}

	private Function<T, Object> entityIdentifierFunction;

	private final Map<Object, T> regionData = new ConcurrentHashMap<>();

	private final String regionBeanName;

	protected RegionDataInitializingPostProcessor(@NonNull String regionBeanName) {

		Assert.hasText(regionBeanName, String.format("Region bean name [%s] must be specified", regionBeanName));

		this.regionBeanName = regionBeanName;
	}

	public @NonNull String getRegionBeanName() {
		return this.regionBeanName;
	}

	protected @NonNull Function<T, Object> getEntityIdentifierFunction() {
		return this.entityIdentifierFunction;
	}

	public @NonNull Map<Object, T> getRegionData() {
		return this.regionData;
	}

	protected boolean isTargetRegion(Object bean, String beanName) {
		return bean instanceof Region && getRegionBeanName().equals(beanName);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void initializeTargetRegionWithData(@NonNull ContextRefreshedEvent event) {

		resolveTargetRegion(event)
			.ifPresent(resolvedTargetRegion -> resolvedTargetRegion.putAll(getRegionData()));
	}

	protected Optional<ApplicationContext> resolveApplicationContext(@NonNull ContextRefreshedEvent event) {

		return Optional.ofNullable(event)
			.map(ContextRefreshedEvent::getApplicationContext)
			.map(Optional::of)
			.orElseThrow(() ->
				newIllegalStateException("Failed to resolve ApplicationContext from ContextRefreshedEvent [%s]", event));
	}

	@SuppressWarnings("unchecked")
	protected Optional<Region<Object, T>> resolveTargetRegion(@NonNull ContextRefreshedEvent event) {

		Region<Object, T> resolvedTargetRegion = resolveApplicationContext(event)
			.map(applicationContext -> applicationContext.getBean(getRegionBeanName(), Region.class))
			.orElseThrow(() -> newIllegalStateException("Failed to resolve Region bean [%s] from ApplicationContext",
				getRegionBeanName()));

		return Optional.of(resolvedTargetRegion);
	}

	public RegionDataInitializingPostProcessor<T> store(T entity) {
		getRegionData().put(getEntityIdentifierFunction().apply(entity), entity);
		return this;
	}

	protected RegionDataInitializingPostProcessor<T> useEntityIdentifierFunction(
		@NonNull Function<T, Object> entityIdentifierFunction) {

		Assert.notNull(entityIdentifierFunction,
			"The Function used to resolve the entity's identify (identifier) must not be null");

		this.entityIdentifierFunction = entityIdentifierFunction;

		return this;
	}

	@SuppressWarnings("unused")
	public static class EntityIdentifierBuilder<T> {

		private final RegionDataInitializingPostProcessor<T> postProcessor;

		private EntityIdentifierBuilder(@NonNull RegionDataInitializingPostProcessor<T> postProcessor) {
			Assert.notNull(postProcessor, "RegionDataInitializingPostProcess must not be null");
			this.postProcessor = postProcessor;
		}

		public RegionDataInitializingPostProcessor<T> useAsEntityIdentifier(
				@NonNull Function<T, Object> entityIdentifierFunction) {

			return this.postProcessor.useEntityIdentifierFunction(entityIdentifierFunction);
		}
	}
}
