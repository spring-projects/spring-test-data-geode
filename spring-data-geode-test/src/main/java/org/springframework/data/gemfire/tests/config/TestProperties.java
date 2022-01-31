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
package org.springframework.data.gemfire.tests.config;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalStateException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link TestProperties} used to configure the underlying resources in the Spring Test for Apache Geode Framework.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see java.util.Properties
 * @see org.springframework.core.io.Resource
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class TestProperties implements Iterable<String> {

	public static final boolean NORMALIZE_SYSTEM_PROPERTY_NAMES = true;

	public static final String SYSTEM_PROPERTY_PREFIX = "system.";
	public static final String TEST_PROPERTIES_PATH = "test.properties";

	private static TestProperties testPropertiesReference;

	/**
	 * Factory method used to construct a new, and get the {@literal single} instance of, {@link TestProperties}
	 * initialized withg a {@literal test.properties} resource in the application classpath if it exists.
	 *
	 * @return a new {@link TestProperties}.
	 * @throws TestConfigurationException if the {@literal test.properties} classpath resource exists,
	 * but could not be loaded.
	 * @see #TestProperties()
	 */
	public static synchronized TestProperties getInstance() {

		if (testPropertiesReference == null) {
			testPropertiesReference = new TestProperties();
		}

		return testPropertiesReference;
	}

	private final Properties testProperties;

	/**
	 * Constructs a new instance of {@link TestProperties} initialized with a {@literal test.properties} resource
	 * in the application classpath if it exists.
	 *
	 * @throws TestConfigurationException if the {@literal test.properties} classpath resource exists,
	 * but could not be loaded.
	 */
	public TestProperties() {
		this.testProperties = new Properties(resolveDefaultTestProperties());
	}

	private @NonNull Properties resolveDefaultTestProperties() {

		Properties defaultTestProperties = new Properties();

		Resource defaultTestPropertiesResource = getTestPropertiesResource();

		if (defaultTestPropertiesResource.exists()) {
			try (InputStream in = defaultTestPropertiesResource.getInputStream()) {
				defaultTestProperties.load(in);
			}
			catch (IOException cause) {

				String message = String.format("Failed to load test configuration [%s]", defaultTestPropertiesResource);

				throw new TestConfigurationException(message, cause);
			}
		}

		return defaultTestProperties;
	}

	/**
	 * Gets a reference to the {@literal mutable} test {@link Properties} managed by {@literal this}
	 * {@link TestProperties} instance.
	 *
	 * @return a reference to the {@literal mutable} test {@link Properties}.
	 * @see java.util.Properties
	 */
	protected @NonNull Properties getTestProperties() {
		return this.testProperties;
	}

	/**
	 * Gets a {@link Resource} referring to {@literal test.properties} in the application classpath.
	 *
	 * @return a {@link Resource} referring to {@literal test.properties} in the application classpath.
	 * @see org.springframework.core.io.ClassPathResource
	 * @see org.springframework.core.io.Resource
	 */
	protected @NonNull Resource getTestPropertiesResource() {
		return new ClassPathResource(TEST_PROPERTIES_PATH);
	}

	/**
	 * Determines whether the given {@link String named} property is a test
	 * {@link System#getProperties() System property}.
	 *
	 * A {@literal test} {@link System#getProperties() System property} is any property prefixed with {@literal system.}.
	 * This property would be added to the {@link System#getProperties()} if {@link #configureSystemProperties()}
	 * were called.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property.
	 * @return a boolean valued indicating whether the given {@link String named} property is a test
	 * {@link System#getProperties() System property}.
\	 */
	protected boolean isTestSystemProperty(@Nullable String propertyName) {
		return propertyName != null && propertyName.startsWith(SYSTEM_PROPERTY_PREFIX);
	}

	/**
	 * Resolves the given {@link String named} property to a {@literal non-null} property {@link String name}.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property to resolve as
	 * a {@literal non-null} {@link String value}
	 * @return the resolved property {@link String name}.
	 */
	protected @NonNull String toNonNullPropertyName(@Nullable String propertyName) {
		return String.valueOf(propertyName);
	}

	/**
	 * Converts the given {@link String named} property into a test system property prefixed with {@literal system.}.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property to convert into
	 * a test system property name.
	 * @return the given {@link String named} property as a test system property prefixed with {@literal system.}.
	 * @see #isTestSystemProperty(String)
	 * @see #toNonNullPropertyName(String)
	 */
	protected @NonNull String toSystemPropertyName(@Nullable String propertyName) {

		return isTestSystemProperty(propertyName) ? propertyName
			: SYSTEM_PROPERTY_PREFIX.concat(toNonNullPropertyName(propertyName));
	}

	/**
	 * Clears all properties from {@literal this} {@link TestProperties} instance.
	 */
	public void clear() {
		getTestProperties().clear();
	}

	/**
	 * Clears all configured test properties from {@link System#getProperties()}.
	 */
	public void clearSystemProperties() {
		getSystemPropertyNames().forEach(System::clearProperty);
	}

	/**
	 * Configures {@link System#getProperties()} with all {@literal test.properties} prefixed with {@literal system.}.
	 */
	public void configureSystemProperties() {

		getSystemPropertyNames()
			.forEach(property -> System.setProperty(stripSystemPropertyPrefix(property), get(property)));
	}

	/**
	 * Gets the {@link String value} of the given {@link String named} property.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property;
	 * must not be {@literal null}.
	 * @return the {@link String value} for the given {@link String named} property,
	 * or {@literal null} if a given property is undefined.
	 */
	public @Nullable String get(@NonNull String propertyName) {

		Properties testProperties = getTestProperties();

		String resolvedPropertyName = toNonNullPropertyName(propertyName);
		String propertyValue = testProperties.getProperty(resolvedPropertyName);

		return propertyValue != null ? propertyValue
			: testProperties.getProperty(toSystemPropertyName(resolvedPropertyName));
	}

	/**
	 * Gets the {@link String value} of the given {@link String named} property, or returns
	 * the {@link String defaultPropertyValue} if the given {@link String named} property
	 * is undefined.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property; must not be {@literal null}.
	 * @param defaultPropertyValue {@link String} containing the value to return if the given {@link String named}
	 * property is undefined.
	 * @return a {@link String value} for the given {@link String named} property, or {@link String defaultPropertyValue}
	 * if a given {@link String named} property is undefined.
	 * @see #get(String)
	 */
	public @Nullable String get(@NonNull String propertyName, @Nullable String defaultPropertyValue) {

		String propertyValue = get(propertyName);

		return propertyValue != null ? propertyValue : defaultPropertyValue;
	}

	/**
	 * Gets a {@link Set} of property {@link String names} for all properties defined in {@literal test.properties}.
	 *
	 * @return a {@link Set} of property {@link String names} for all properties defined in {@literal test.properties}.
	 * @see java.util.Set
	 */
	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(getTestProperties().stringPropertyNames());
	}

	/**
	 * Gets a {@link Set} of {@literal system} property {@link String names} from {@literal test.properties}.
	 *
	 * @return a {@link Set} of {@literal system} property {@link String names} from {@literal test.properties}.
	 * @see #getSystemPropertyNames(boolean)
	 * @see java.util.Set
	 */
	public Set<String> getSystemPropertyNames() {
		return getSystemPropertyNames(NORMALIZE_SYSTEM_PROPERTY_NAMES);
	}

	/**
	 * Gets a {@link Set} of {@literal system} property {@link String names} from {@literal test.properties}.
	 *
	 * @param normalize boolean value indicating whether to strip the System property prefix ({@literal system.})
	 * from the property {@link String name} before adding the property {@link String name} to the {@link Set}.
	 * @return a {@link Set} of {@literal system} property {@link String names} from {@literal test.properties}.
	 * @see #isTestSystemProperty(String)
	 * @see #stripSystemPropertyPrefix(String)
	 * @see java.util.Set
	 */
	public Set<String> getSystemPropertyNames(boolean normalize) {

		return getPropertyNames().stream()
			.filter(this::isTestSystemProperty)
			.map(propertyName -> normalize ? stripSystemPropertyPrefix(propertyName) : propertyName)
			.collect(Collectors.toSet());
	}

	String stripSystemPropertyPrefix(@NonNull String propertyName) {

		return propertyName.startsWith(SYSTEM_PROPERTY_PREFIX)
			? propertyName.substring(SYSTEM_PROPERTY_PREFIX.length())
			: propertyName;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Iterator<String> iterator() {
		return getPropertyNames().iterator();
	}

	/**
	 * Gets the {@link String value} of the given {@link String named} property.
	 *
	 * The given {@link String named} property must have a {@literal non-null}, {@literal non-empty}
	 * and {@literal non-blank} {@link String value}.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property to get.
	 * @return the {@link String value} of the given {@link String named} property.
	 * @throws IllegalStateException if the given {@link String named} property is undefined.
	 * @see #get(String)
	 */
	public @NonNull String requireProperty(@NonNull String propertyName) {

		return Optional.ofNullable(get(propertyName))
			.filter(StringUtils::hasText)
			.orElseThrow(() -> newIllegalStateException("Property [%s] is undefined", propertyName));
	}

	/**
	 * Sets the given {@link String named} property to the given {@link String propertyValue}.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property to set.
	 * @param propertyValue {@link String} containing the {@literal value} of the property to set.
	 * @return {@literal this} {@link TestProperties}.
	 * @throws IllegalArgumentException if the property {@link String name} of {@link String value}
	 * are {@literal null}.
	 */
	public @Nullable TestProperties set(@NonNull String propertyName, @NonNull String propertyValue) {

		Assert.hasText(propertyName, String.format("PropertyName [%s] must not be null or empty", propertyName));
		Assert.notNull(propertyValue, "PropertyValue must not be null");

		getTestProperties().setProperty(propertyName, propertyValue);

		return this;
	}

	/**
	 * Unset the given {@link String named} property.
	 *
	 * @param propertyName {@link String} containing the {@literal name} of the property to unsed.
	 * @return the {@link String value} of the given {@link String named} property
	 * or {@literal null} if the property was undefined.
	 */
	public @Nullable String unset(@NonNull String propertyName) {
		return propertyName != null ? (String) getTestProperties().remove(propertyName) : null;
	}
}
