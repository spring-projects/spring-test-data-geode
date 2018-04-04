package org.springframework.data.gemfire;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects;
import org.springframework.data.gemfire.util.RegionUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit tests for an Apache Geode {@link ClientCache} application using mock objects.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.tests.mock.annotation.EnableGemFireMockObjects
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class MockClientCacheApplicationTest {

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void exampleRegionIsMocked() {

    assertThat(this.example).isNotNull();
    assertThat(this.example.getFullPath()).isEqualTo(RegionUtils.toRegionPath("Example"));
    assertThat(this.example.getName()).isEqualTo("Example");
    assertThat(this.example.put(1, "test")).isNull();
    assertThat(this.example.get(1)).isEqualTo("test");
  }

  @EnableGemFireMockObjects
  @ClientCacheApplication
  static class TestConfiguration {

    @Bean("Example")
    public ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

      exampleRegion.setCache(gemfireCache);
      exampleRegion.setClose(false);
      exampleRegion.setShortcut(ClientRegionShortcut.LOCAL);

      return exampleRegion;
    }

  }
}
