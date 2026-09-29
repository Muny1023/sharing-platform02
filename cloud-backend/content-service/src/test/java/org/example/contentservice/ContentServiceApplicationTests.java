package org.example.contentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.config.location=classpath:/application-test.properties",
		"spring.cloud.nacos.discovery.enabled=false",
		"spring.cloud.nacos.config.enabled=false",
		"spring.autoconfigure.exclude=com.alibaba.cloud.nacos.NacosConfigAutoConfiguration,com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration,com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration,com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration,com.alibaba.cloud.nacos.NacosServiceAutoConfiguration"
})
class ContentServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
