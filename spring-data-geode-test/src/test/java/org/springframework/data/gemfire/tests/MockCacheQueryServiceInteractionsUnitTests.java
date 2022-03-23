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
package org.springframework.data.gemfire.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.Test;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.types.CollectionType;
import org.apache.geode.cache.query.types.ObjectType;

import org.springframework.data.gemfire.tests.mock.GemFireMockObjectsSupport;

/**
 * Unit Tests for GemFire/Geode {@literal Mock} {@link GemFireCache} {@link QueryService} and {@link Query OQL Queries}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.query.QueryService
 * @see org.apache.geode.cache.query.Query
 * @see org.apache.geode.cache.query.SelectResults
 * @see org.apache.geode.cache.query.types.CollectionType
 * @see org.apache.geode.cache.query.types.ObjectType
 * @since 0.0.19
 */
public class MockCacheQueryServiceInteractionsUnitTests {

	private void assertSelectResults(SelectResults<Object> selectResults) {

		assertThat(selectResults).isNotNull();
		assertThat(selectResults.asList()).isEmpty();
		assertThat(selectResults.asSet()).isEmpty();
		assertThat(selectResults.isModifiable()).isFalse();
		assertThat(selectResults.occurrences("MOCK")).isZero();
		assertThat(selectResults.occurrences("TEST")).isZero();

		CollectionType collectionType = selectResults.getCollectionType();

		assertThat(collectionType).isNotNull();
		assertThat(collectionType.allowsDuplicates()).isFalse();
		assertThat(collectionType.isOrdered()).isFalse();

		ObjectType objectType = collectionType.getElementType();

		assertThat(objectType).isNotNull();
		assertThat(objectType.getSimpleClassName()).isEqualTo(Object.class.getSimpleName());
		assertThat(objectType.isCollectionType()).isFalse();
		assertThat(objectType.isMapType()).isFalse();
		assertThat(objectType.isStructType()).isFalse();
		assertThat(objectType.resolveClass()).isEqualTo(Object.class);

		Iterator<?> iterator = selectResults.iterator();

		assertThat(iterator).isNotNull();
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void queryServiceInteractionsAndQueryExecutionIsCorrect() throws Exception {

		String queryString = "SELECT * FROM /Example WHERE id = $1";

		ClientCache mockClientCache = GemFireMockObjectsSupport.mockClientCache();

		assertThat(mockClientCache).isNotNull();

		QueryService mockQueryService = mockClientCache.getQueryService();

		assertThat(mockQueryService).isNotNull();

		Query mockQuery = mockQueryService.newQuery(queryString);

		assertThat(mockQuery).isNotNull();
		assertThat(mockQuery.getQueryString()).isEqualTo(queryString);
		assertThat(mockQuery.getStatistics()).isNotNull();

		Object results = mockQuery.execute();

		assertThat(results).isInstanceOf(SelectResults.class);

		SelectResults<Object> mockSelectResults = (SelectResults<Object>) results;

		assertSelectResults(mockSelectResults);

		results = mockQuery.execute(1);

		assertThat(results).isInstanceOf(SelectResults.class);

		mockSelectResults = (SelectResults<Object>) results;

		assertSelectResults(mockSelectResults);
	}
}
