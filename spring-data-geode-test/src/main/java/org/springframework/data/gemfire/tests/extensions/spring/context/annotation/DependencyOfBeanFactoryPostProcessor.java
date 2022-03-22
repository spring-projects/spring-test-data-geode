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
package org.springframework.data.gemfire.tests.extensions.spring.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.apache.shiro.util.StringUtils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.SpringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Spring {@link BeanFactoryPostProcessor} implementation used to post process {@link BeanDefinition BeanDefinitions}
 * annotated with {@link DependencyOf} annotations.
 *
 * @author John Blum
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.data.gemfire.tests.extensions.spring.context.annotation.DependencyOf
 * @since 0.0.23
 */
public class DependencyOfBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	protected static final Class<? extends Annotation> DEPENDENCY_OF_TYPE = DependencyOf.class;

	protected static final String VALUE_ATTRIBUTE_NAME = "value";

	/**
	 * @inheritDoc
	 */
	@Override
	public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

		String[] dependencyOfAnnotatedBeanNames =
			ArrayUtils.nullSafeArray(beanFactory.getBeanNamesForAnnotation(DEPENDENCY_OF_TYPE), String.class);

		for (String beanName : dependencyOfAnnotatedBeanNames) {

			Annotation dependencyOf = beanFactory.findAnnotationOnBean(beanName, DEPENDENCY_OF_TYPE);

			Optional.ofNullable(dependencyOf)
				.map(this::getAnnotationAttributes)
				.map(this::getValueAttribute)
				.ifPresent(dependentBeanNames -> {
					for (String dependentBeanName : dependentBeanNames) {
						Optional.ofNullable(dependentBeanName)
							.filter(StringUtils::hasText)
							.map(beanFactory::getBeanDefinition)
							.ifPresent(dependentBeanDefinition ->
								SpringUtils.addDependsOn(dependentBeanDefinition, beanName));
					}
				});
		}
	}

	private @Nullable AnnotationAttributes getAnnotationAttributes(@NonNull Annotation annotation) {

		return annotation != null
			? AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(annotation))
			: null;
	}

	private @Nullable String[] getValueAttribute(@NonNull AnnotationAttributes annotationAttributes) {

		return annotationAttributes != null
			? annotationAttributes.getStringArray(VALUE_ATTRIBUTE_NAME)
			: null;
	}
}
