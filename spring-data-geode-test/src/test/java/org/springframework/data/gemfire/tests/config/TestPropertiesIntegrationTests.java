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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Test;

import org.springframework.core.io.Resource;

/**
 * Integration Tests for {@link TestProperties}.
 *
 * @author John Blum
 * @see java.lang.System#getProperties()
 * @see org.junit.Test
 * @see org.springframework.data.gemfire.tests.config.TestProperties
 * @since 1.0.0
 */
public class TestPropertiesIntegrationTests {

	@AfterClass
	public static void clearSystemProperties() {
		TestProperties.getInstance().clearSystemProperties();
	}

	@Test
	public void constructTestProperties() {

		TestProperties properties = new TestProperties();

		assertThat(properties).isNotNull();
		assertThat(properties.getTestProperties()).isNotNull();
		assertThat(properties).isEmpty();
	}

	@Test
	public void constructedTestPropertiesIsNotTheSingleton() {

		TestProperties properties = new TestProperties();
		TestProperties singleProperties = TestProperties.getInstance();

		assertThat(properties).isNotNull();
		assertThat(properties).isNotSameAs(singleProperties);
	}

	@Test
	public void singleTestPropertiesInstanceIsSame() {

		TestProperties properties = TestProperties.getInstance();

		assertThat(properties).isNotNull();
		assertThat(properties).isSameAs(TestProperties.getInstance());
	}

	@Test
	public void destroySingleTestPropertiesInstance() {

		TestProperties testProperties = TestProperties.getInstance()
			.set("port", "12345");

		assertThat(testProperties).isNotNull();
		assertThat(testProperties.get("port")).isEqualTo("12345");

		TestProperties.destroy();

		TestProperties newTestProperties = TestProperties.getInstance();

		assertThat(newTestProperties).isNotSameAs(testProperties);
		assertThat(newTestProperties).isEmpty();
	}

	@Test
	public void getTestPropertiesResourceIsCorrect() {

		Resource testPropertiesResource = TestProperties.getInstance().getTestPropertiesResource();

		assertThat(testPropertiesResource).isNotNull();
		assertThat(testPropertiesResource.getFilename()).isEqualTo(TestProperties.TEST_PROPERTIES_PATH);
	}

	@Test
	public void isTestSystemPropertyWithSystemProperty() {
		assertThat(TestProperties.getInstance().isTestSystemProperty("system.property-name")).isTrue();
	}

	@Test
	public void isTestSystemPropertyWithNullProperty() {
		assertThat(TestProperties.getInstance().isTestSystemProperty(null)).isFalse();
	}

	@Test
	public void isTestSystemPropertyWithNonSystemProperties() {

		TestProperties testProperties = TestProperties.getInstance();

		assertThat(testProperties.isTestSystemProperty("systemPropertyName")).isFalse();
		assertThat(testProperties.isTestSystemProperty("sys.prop.name")).isFalse();
		assertThat(testProperties.isTestSystemProperty("spring.property.name")).isFalse();
		assertThat(testProperties.isTestSystemProperty("log.property.name")).isFalse();
		assertThat(testProperties.isTestSystemProperty("property.name")).isFalse();
		assertThat(testProperties.isTestSystemProperty("propertyName")).isFalse();
	}

	@Test
	public void toNonNullPropertyNameWithNonNullProperty() {
		assertThat(TestProperties.getInstance().toNonNullPropertyName("testProperty")).isEqualTo("testProperty");
	}

	@Test
	public void toNonNullPropertyNameWithNullProperty() {
		assertThat(TestProperties.getInstance().toNonNullPropertyName(null)).isEqualTo("null");
	}

	@Test
	public void toSystemPropertyNameWithSystemProperty() {
		assertThat(TestProperties.getInstance().toSystemPropertyName("system.property")).isEqualTo("system.property");
	}

	@Test
	public void toSystemPropertyNameWithNullProperty() {
		assertThat(TestProperties.getInstance().toSystemPropertyName(null)).isEqualTo("system.null");
	}

	@Test
	public void toSystemPropertyNameWithNonNullProperty() {
		assertThat(TestProperties.getInstance().toSystemPropertyName("testProperty")).isEqualTo("system.testProperty");
	}

	@Test
	public void clearProperties() {

		TestProperties properties = TestProperties.getInstance();

		properties.getTestProperties().setProperty("abc", "ONE");
		properties.getTestProperties().setProperty("xyz", "TWO");

		assertThat(properties).contains("abc", "xyz");

		properties.clear();

		assertThat(properties).isEmpty();
	}

	@Test
	public void get() {

		TestProperties properties = TestProperties.getInstance();

		properties.getTestProperties().setProperty("myProperty", "X");

		assertThat(properties).contains("myProperty");
		assertThat(properties.get("myProperty")).isEqualTo("X");
	}

	@Test
	public void getNullPropertyIsNullSafe() {
		assertThat(TestProperties.getInstance().get(null)).isNull();
	}

	@Test
	public void getSystemProperty() {

		TestProperties properties = TestProperties.getInstance();

		properties.getTestProperties().setProperty("system.app.testProperty", "X");

		assertThat(properties).contains("system.app.testProperty");
		assertThat(properties.get("system.app.testProperty")).isEqualTo("X");
		assertThat(properties.get("app.testProperty")).isEqualTo("X");
	}

	@Test
	public void getUndefinedProperty() {
		assertThat(TestProperties.getInstance().get("undefinedProperty")).isNull();
	}

	@Test
	public void getUndefinedPropertyWithDefaultValue() {
		assertThat(TestProperties.getInstance().get("undefinedProperty", "TEST"))
			.isEqualTo("TEST");
	}

	@Test
	public void requireDefinedProperty() {

		TestProperties properties = TestProperties.getInstance();

		properties.getTestProperties().setProperty("definedProperty", "X");

		assertThat(properties).contains("definedProperty");
		assertThat(properties.requireProperty("definedProperty")).isEqualTo("X");
	}

	@Test(expected = IllegalStateException.class)
	public void requireUndefinedProperty() {

		try {
			TestProperties properties = TestProperties.getInstance();

			assertThat(properties).doesNotContain("undefinedProperty");

			properties.requireProperty("undefinedProperty");
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Property [undefinedProperty] is undefined");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalStateException.class)
	public void requireEmptyProperty() {

		try {
			TestProperties properties = TestProperties.getInstance();

			properties.getTestProperties().put("emptyProperty", "");

			assertThat(properties).contains("emptyProperty");

			properties.requireProperty("emptyProperty");
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Property [emptyProperty] is undefined");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			TestProperties.getInstance().getTestProperties().remove("emptyProperty");
		}
	}

	@Test(expected = IllegalStateException.class)
	public void requireBlankProperty() {

		try {
			TestProperties properties = TestProperties.getInstance();

			properties.getTestProperties().put("blankProperty", "  ");

			assertThat(properties).contains("blankProperty");

			properties.requireProperty("blankProperty");
		}
		catch (IllegalStateException expected) {

			assertThat(expected).hasMessage("Property [blankProperty] is undefined");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			TestProperties.getInstance().getTestProperties().remove("blankProperty");
		}
	}

	@Test
	public void set() {

		TestProperties properties = TestProperties.getInstance()
			.set("propertyOne", "ONE")
			.set("propertyTwo", "TWO");

		assertThat(properties).isNotNull();
		assertThat(properties).contains("propertyOne", "propertyTwo");
		assertThat(properties).contains("propertyOne", "propertyTwo");
	}

	@Test
	public void setAndGetProperty() {

		TestProperties properties = TestProperties.getInstance();

		assertThat(properties).isNotNull();
		assertThat(properties.set("testProperty", "A")).isSameAs(properties);
		assertThat(properties.get("testProperty")).isEqualTo("A");
		assertThat(properties.set("testProperty", "B")).isSameAs(properties);
		assertThat(properties.get("testProperty")).isEqualTo("B");
	}

	@Test
	public void setAndUnsetProperty() {

		TestProperties properties = TestProperties.getInstance();

		assertThat(properties.set("mockProperty", "X")).isSameAs(properties);
		assertThat(properties.get("mockProperty")).isEqualTo("X");
		assertThat(properties.unset("mockProperty")).isEqualTo("X");
		assertThat(properties.get("mockProperty")).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
	public void setWithNullPropertyName() {

		try {
			TestProperties.getInstance().set(null, "TEST");
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("PropertyName [null] must not be null or empty");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void setWithNullPropertyValue() {

		try {
			TestProperties.getInstance().set("invalidProperty", null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("PropertyValue must not be null");
			assertThat(expected).hasNoCause();

			throw expected;
		}
		finally {
			assertThat(TestProperties.getInstance()).doesNotContain("invalidProperty");
		}
	}

	@Test
	public void stripSystemPropertyPrefix() {

		TestProperties properties = TestProperties.getInstance();

		assertThat(properties.stripSystemPropertyPrefix("testProperty")).isEqualTo("testProperty");
		assertThat(properties.stripSystemPropertyPrefix("systemProperty")).isEqualTo("systemProperty");
		assertThat(properties.stripSystemPropertyPrefix("mockProperty")).isEqualTo("mockProperty");
		assertThat(properties.stripSystemPropertyPrefix("sys.prop")).isEqualTo("sys.prop");
		assertThat(properties.stripSystemPropertyPrefix("SYSTEM.mockProperty")).isEqualTo("SYSTEM.mockProperty");
		assertThat(properties.stripSystemPropertyPrefix("system.testProperty")).isEqualTo("testProperty");
	}

	@Test
	public void unsetNullPropertyIsNullSafe() {
		assertThat(TestProperties.getInstance().unset(null)).isNull();
	}

	@Test
	public void configureAndClearSystemProperties() {

		try {
			TestProperties properties = TestProperties.getInstance()
				.set("system.propOne", "X")
				.set("propTwo", "Y")
				.set("system.propThree", "Z")
				.set("SYSTEM.propFour", "F");

			assertThat(System.getProperties()).doesNotContainKeys("system.propOne", "propOne", "propTwo",
				"system.propThree", "propThree", "SYSTEM.propFour", "propFour");

			properties.configureSystemProperties();

			assertThat(System.getProperties()).containsKeys("propOne", "propThree");
			assertThat(System.getProperties())
				.doesNotContainKeys("system.propOne", "propTwo", "system.propThree", "SYSTEM.propFour", "propFour");
			assertThat(System.getProperty("propOne")).isEqualTo("X");
			assertThat(System.getProperty("system.propOne")).isNull();
			assertThat(System.getProperty("propTwo")).isNull();
			assertThat(System.getProperty("propThree")).isEqualTo("Z");
			assertThat(System.getProperty("system.propThree")).isNull();
			assertThat(System.getProperty("propFour")).isNull();
			assertThat(System.getProperty("SYSTEM.propFour")).isNull();

			properties.clearSystemProperties();

			assertThat(System.getProperties()).doesNotContainKeys("system.propOne", "propOne", "propTwo",
				"system.propThree", "propThree", "SYSTEM.propFour", "propFour");
		}
		finally {
			Arrays.asList("propOne", "propThree").forEach(System::clearProperty);
			assertThat(System.getProperties()).doesNotContainKeys("propOne", "propThree");
		}
	}
}
