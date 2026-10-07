package org.example.cloudgateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.config.location=classpath:/application-test.yml",
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
		Properties properties = loadYamlProperties();
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
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowed-origins[0]"));
		assertEquals("http://127.0.0.1:5173", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowed-origins[1]"));
		assertEquals("GET", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowed-methods[0]"));
		assertEquals("OPTIONS", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowed-methods[4]"));
		assertEquals("true", cors.getProperty(
				"spring.cloud.gateway.server.webflux.globalcors.add-to-simple-url-handler-mapping"));
		assertEquals("response", cors.getProperty("spring.cloud.sentinel.scg.fallback.mode"));
		assertEquals("429", cors.getProperty("spring.cloud.sentinel.scg.fallback.response-status"));
		assertEquals("application/json;charset=UTF-8",
				cors.getProperty("spring.cloud.sentinel.scg.fallback.content-type"));
		assertEquals("{\"code\":429,\"data\":null,\"message\":\"请求量过大，请稍后重试\"}",
				cors.getProperty("spring.cloud.sentinel.scg.fallback.response-body"));
		assertEquals("gw-flow", properties.getProperty("spring.cloud.sentinel.datasource.gateway-flow.nacos.rule-type"));
		assertEquals("${NACOS_USERNAME:nacos}", properties.getProperty(
				"spring.cloud.sentinel.datasource.gateway-flow.nacos.username"));
		assertEquals("${NACOS_PASSWORD:123456}", properties.getProperty(
				"spring.cloud.sentinel.datasource.gateway-flow.nacos.password"));
		assertEquals("${NACOS_USERNAME:nacos}", properties.getProperty(
				"spring.cloud.sentinel.datasource.gateway-degrade.nacos.username"));
		assertEquals("${NACOS_PASSWORD:123456}", properties.getProperty(
				"spring.cloud.sentinel.datasource.gateway-degrade.nacos.password"));
	}

	private Properties loadYamlProperties() {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(new ClassPathResource("application.yml"));
		Properties properties = factory.getObject();
		assertNotNull(properties);
		return properties;
	}

}
