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
package org.springframework.data.gemfire.tests.extensions.spring.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AliasFor;

/**
 * The {@link DependencyOf} annotation is the inverse of Spring's {@link DependsOn} annotation allowing a bean
 * to declare itself as a dependency of another bean in the Spring container.
 *
 * For example, with Spring's {@link DependsOn} annotation, a bean A can say that it {@literal depends on} bean B.
 * However, with the {@link DependencyOf} annotation, a bean B can say it is a required dependency for bean A,
 * or rather that bean A depends on bean B.
 *
 * Therefore, the following bean definitions for A & B are equivalent:
 *
 * <code>
 *
 * @Configuration
 * public class ConfigurationOne {
 *
 *   @Bean
 *   @DependsOn("b")
 *   public A a() {
 *     return new A();
 *   }
 *
 *   @Bean
 *   public B b() {
 *     return new B();
 *   }
 * }
 * </code>
 *
 *
 * And...
 *
 * <code>
 * @Configuration
 * public class ConfigurationTwo {
 *
 *   @Bean
 *   public A a() {
 *     return new A();
 *   }
 *
 *   @Bean
 *   @DependencyOf("a")
 *   public B b() {
 *     return new B();
 *   }
 * }
 * </code>
 *
 * One advantage of this approach is that bean A does not need to know all the beans it is possibly dependent on,
 * especially at runtime when additional collaborators or dependencies maybe added dynamically to the classpath,
 * Therefore, additional dependencies of A can be added to the configuration automatically, over time without
 * having to go back and modify the bean definition for A.
 *
 * This feature is experimental.
 *
 * @author John Blum
 * @see java.lang.annotation.Documented
 * @see java.lang.annotation.Inherited
 * @see java.lang.annotation.Retention
 * @see java.lang.annotation.Target
 * @see org.springframework.context.annotation.DependsOn
 * @since 0.0.23
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@SuppressWarnings("unused")
public @interface DependencyOf {

	@AliasFor("value")
	String[] beanNames() default {};

	@AliasFor("beanNames")
	String[] value() default {};

}
