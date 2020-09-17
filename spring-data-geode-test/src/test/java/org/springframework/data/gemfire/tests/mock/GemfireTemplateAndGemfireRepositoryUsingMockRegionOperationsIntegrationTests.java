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
package org.springframework.data.gemfire.tests.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Integration Tests for SDG's {@link GemfireTemplate} and SD[G] {@link GemfireRepository Repositories}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.cache.Region
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.repository.GemfireRepository
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 0.0.19
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class GemfireTemplateAndGemfireRepositoryUsingMockRegionOperationsIntegrationTests {

	private final Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	@Qualifier("customersTemplate")
	private GemfireTemplate customersTemplate;

	@Resource(name = "Customers")
	private Region<Long, Customer> customers;

	@Before
	public void setup() {

		assertThat(this.customers).isNotNull();
		assertThat(this.customers.getName()).isEqualTo("Customers");

		this.customers.put(this.jonDoe.getId(), this.jonDoe);

		assertThat(this.customers).hasSize(1);
		assertThat(this.customers.get(this.jonDoe.getId())).isEqualTo(this.jonDoe);
	}

	@Test
	public void gemfireRepositoryCountIsCorrect() {
		assertThat(this.customerRepository.count()).isEqualTo(1L);
	}

	@Test
	public void gemfireRepositoryFindByIdIsCorrect() {

		assertThat(this.customerRepository.findById(this.jonDoe.getId()).orElse(null)).isEqualTo(this.jonDoe);

		verify(this.customers, atLeastOnce()).get(eq(this.jonDoe.getId()));
	}

	@Test
	public void gemfireTemplateGetIsCorrect() {

		assertThat(this.customersTemplate.<Long, Customer>get(this.jonDoe.getId())).isEqualTo(this.jonDoe);

		verify(this.customers, atLeastOnce()).get(eq(this.jonDoe.getId()));
	}

	@EnableGemFireMockObjects
	@ClientCacheApplication
	@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
	static class TestConfiguration {

		@Bean
		GemfireTemplate customersTemplate(@Qualifier("Customers") Region<Long, Customer> customersRegion) {
			return new GemfireTemplate(customersRegion);
		}

		@Bean
		GemfireRepositoryFactoryBean<CustomerRepository, Customer, Long> customersRepositoryFactoryBean() {

			GemfireRepositoryFactoryBean<CustomerRepository, Customer, Long> repositoryFactoryBean =
				new GemfireRepositoryFactoryBean<>(CustomerRepository.class);

			repositoryFactoryBean.setGemfireMappingContext(new GemfireMappingContext());

			return repositoryFactoryBean;
		}
	}

	@Data
	@RequiredArgsConstructor(staticName = "newCustomer")
	@org.springframework.data.gemfire.mapping.annotation.Region("Customers")
	static class Customer {

		@Id @NonNull
		private Long id;

		@NonNull
		private String name;

	}

	interface CustomerRepository extends GemfireRepository<Customer, Long> { }

}
