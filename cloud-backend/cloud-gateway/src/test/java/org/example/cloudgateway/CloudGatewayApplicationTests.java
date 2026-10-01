package org.example.cloudgateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	void sentinelNacosDataSourceIsAvailable() throws ClassNotFoundException {
		Class.forName("com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource");
	}

	@Test
	void accountRoutesUseServiceDiscovery() throws Exception {
		Properties properties = new Properties();
		Path config = Path.of("src/main/resources/application.properties");
		try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		assertEquals("lb://account-service", properties.getProperty("spring.cloud.gateway.server.webflux.routes[0].uri"));
		assertEquals("lb://account-service", properties.getProperty("spring.cloud.gateway.server.webflux.routes[1].uri"));
		assertEquals("lb://account-service", properties.getProperty("spring.cloud.gateway.server.webflux.routes[2].uri"));
		assertEquals("Path=/api/seata/**", properties.getProperty("spring.cloud.gateway.server.webflux.routes[2].predicates[0]"));
		assertEquals("lb://content-service", properties.getProperty("spring.cloud.gateway.server.webflux.routes[3].uri"));
		assertEquals("lb://content-service", properties.getProperty("spring.cloud.gateway.server.webflux.routes[8].uri"));
		assertEquals("Path=/api/ai/**,/internal/ai/**",
				properties.getProperty("spring.cloud.gateway.server.webflux.routes[8].predicates[0]"));
		assertTrue(properties.getProperty("spring.cloud.sentinel.transport.dashboard")
				.contains("localhost:8858"));
		Properties cors = loadYamlProperties();
		assertEquals("http://localhost:5173", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowedOrigins[0]"));
		assertEquals("http://127.0.0.1:5173", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowedOrigins[1]"));
		assertEquals("GET", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowedMethods[0]"));
		assertEquals("OPTIONS", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowedMethods[4]"));
		assertEquals("true", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.add-to-simple-url-handler-mapping"));
		assertEquals("gw-flow", properties.getProperty("spring.cloud.sentinel.datasource.gateway-flow.nacos.rule-type"));
		assertEquals("429", properties.getProperty("spring.cloud.sentinel.scg.fallback.response-status"));
	}

	private Properties loadYamlProperties() {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(new ClassPathResource("application.yml"));
		Properties properties = factory.getObject();
		assertNotNull(properties);
		return properties;
	}

}
