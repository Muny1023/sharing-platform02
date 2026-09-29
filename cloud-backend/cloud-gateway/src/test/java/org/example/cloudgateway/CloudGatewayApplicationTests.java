package org.example.cloudgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
		"spring.config.location=classpath:/application-test.properties",
		"spring.cloud.nacos.discovery.enabled=false",
		"spring.cloud.nacos.config.enabled=false",
		"spring.autoconfigure.exclude=com.alibaba.cloud.nacos.NacosConfigAutoConfiguration,com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration,com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration,com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration,com.alibaba.cloud.nacos.NacosServiceAutoConfiguration"
})
class CloudGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void accountRoutesUseServiceDiscovery() throws Exception {
		Properties properties = new Properties();
		Path config = Path.of("src/main/resources/application.properties");
		try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		assertEquals("lb://account-service", properties.getProperty("spring.cloud.gateway.routes[0].uri"));
		assertEquals("lb://account-service", properties.getProperty("spring.cloud.gateway.routes[1].uri"));
	}

}
