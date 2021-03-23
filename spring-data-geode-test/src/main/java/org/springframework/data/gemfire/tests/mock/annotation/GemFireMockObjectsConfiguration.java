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
package org.springframework.data.gemfire.tests.mock.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.geode.cache.Region;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.support.SimpleGemfireRepository;
import org.springframework.data.gemfire.tests.mock.beans.factory.config.GemFireMockObjectsBeanPostProcessor;
import org.springframework.data.gemfire.tests.mock.context.event.DestroyGemFireMockObjectsApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.context.event.AfterTestClassEvent;
import org.springframework.util.Assert;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The {@link GemFireMockObjectsConfiguration} class is a Spring {@link Configuration @Configuration} class
 * containing bean definitions to configure GemFire Object mocking.
 *
 * @author John Blum
 * @see java.lang.annotation.Annotation
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.core.type.AnnotationMetadata
 * @see org.springframework.data.gemfire.config.annotation.support.AbstractAnnotationConfigSupport
 * @see org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport
 * @see org.springframework.data.gemfire.tests.mock.beans.factory.config.GemFireMockObjectsBeanPostProcessor
 * @see org.springframework.data.gemfire.tests.mock.context.event.DestroyGemFireMockObjectsApplicationListener
 * @since 0.0.1
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireMockObjectsConfiguration extends AbstractAnnotationConfigSupport implements ImportAware {

	public static final boolean DEFAULT_USE_SINGLETON_CACHE = false;

	private boolean useSingletonCache = DEFAULT_USE_SINGLETON_CACHE;

	@SuppressWarnings("unchecked")
	private Class<? extends ApplicationEvent>[] destroyEventTypes = new Class[] { AfterTestClassEvent.class };

	@Override
	@SuppressWarnings("unchecked")
	public void setImportMetadata(@NonNull AnnotationMetadata importingClassMetadata) {

		Optional.of(importingClassMetadata)
			.filter(this::isAnnotationPresent)
			.map(this::getAnnotationAttributes)
			.ifPresent(enableGemFireMockObjectsAttributes -> {

				this.destroyEventTypes = (Class<? extends ApplicationEvent>[])
					enableGemFireMockObjectsAttributes.getClassArray("destroyOnEvents");

				this.useSingletonCache =
					enableGemFireMockObjectsAttributes.getBoolean("useSingletonCache");
			});
	}

	protected @NonNull Class<? extends Annotation> getAnnotationType() {
		return EnableGemFireMockObjects.class;
	}

	protected @NonNull Class<? extends ApplicationEvent>[] getConfiguredDestroyEventTypes() {
		return this.destroyEventTypes;
	}

	protected boolean isUseSingletonCacheConfigured() {
		return this.useSingletonCache;
	}

	@Bean
	public ApplicationListener<ApplicationEvent> destroyGemFireMockObjectsApplicationListener() {
		return DestroyGemFireMockObjectsApplicationListener.newInstance(getConfiguredDestroyEventTypes());
	}

	@Bean
	public BeanPostProcessor gemfireMockObjectsBeanPostProcessor() {
		return GemFireMockObjectsBeanPostProcessor.newInstance(isUseSingletonCacheConfigured());
	}

	@Bean
	public BeanPostProcessor gemfireRepositoryBeanPostProcessor() {

		return new BeanPostProcessor() {

			public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
					throws BeansException {

				if (bean instanceof GemfireRepository) {

					getRegion(bean)
						.ifPresent(region -> {
							if (bean instanceof Advised) {

								Advised advisedBean = (Advised) bean;

								advisedBean.addAdvice(0, new CountMethodInterceptor(region));
							}
						});
				}

				return bean;
			}

			@SuppressWarnings("unchecked")
			private Optional<Region<?, ?>> getRegion(@Nullable Object bean) {

				return Optional.ofNullable(bean)
					.map(AopProxyUtils::getSingletonTarget)
					.filter(SimpleGemfireRepository.class::isInstance)
					.map(SimpleGemfireRepository.class::cast)
					.map(SimpleGemfireRepository::getRegion);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	private static class CountMethodInterceptor implements MethodInterceptor {

		private static final String COUNT_METHOD_NAME = "count";

		private final Region region;

		private CountMethodInterceptor(@NonNull Region region) {

			Assert.notNull(region, "Region must not be null");

			this.region = region;
		}

		private @NonNull Region<?, ?> getRegion() {
			return this.region;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			Method method = invocation.getMethod();

			return isCountMethod(method) ? Long.valueOf(getRegion().size()) : invocation.proceed();
		}

		private boolean isCountMethod(Method method) {
			return method != null && COUNT_METHOD_NAME.equals(method.getName());
		}
	}
}
