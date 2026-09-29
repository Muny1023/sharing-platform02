package org.example.accountservice;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureDocumentationTest {

	@Test
	void architectureContextAndDecisionsArePresent() {
		Path projectRoot = Path.of("..", "..").toAbsolutePath().normalize();

		assertTrue(Files.isRegularFile(projectRoot.resolve("CONTEXT.md")));
		assertTrue(Files.isRegularFile(projectRoot.resolve("docs/adr/0001-service-boundaries.md")));
		assertTrue(Files.isRegularFile(projectRoot.resolve("docs/adr/0006-eventual-consistency-for-notifications.md")));
	}

	@Test
	void backendAggregationContainsAllJavaServices() throws Exception {
		Path projectRoot = Path.of("..", "..").toAbsolutePath().normalize();
		var modules = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(projectRoot.resolve("cloud-backend/pom.xml").toFile())
				.getElementsByTagName("module");

		assertEquals(5, modules.getLength());
	}

	@Test
	void migratedServicesInheritCloudBackendParent() throws Exception {
		Path projectRoot = Path.of("..", "..").toAbsolutePath().normalize();
		for (String service : new String[]{"content-service", "notification-service"}) {
			var parents = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(projectRoot.resolve("cloud-backend/" + service + "/pom.xml").toFile())
					.getElementsByTagName("parent");
			assertEquals("cloud-backend", ((org.w3c.dom.Element) parents.item(0))
					.getElementsByTagName("artifactId")
					.item(0)
					.getTextContent());
		}
	}

	@Test
	void servicesUseOptionalNacosConfigurationImport() throws Exception {
		Path projectRoot = Path.of("..", "..").toAbsolutePath().normalize();
		for (String service : new String[]{"cloud-gateway", "account-service", "content-service", "notification-service"}) {
			Properties properties = new Properties();
			Path config = projectRoot.resolve("cloud-backend/" + service + "/src/main/resources/application.properties");
			try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			String configImport = properties.getProperty("spring.config.import");
			assertTrue(configImport.startsWith("optional:nacos:"));
			assertTrue(configImport.endsWith(".yaml"));
			assertTrue(properties.containsKey("spring.cloud.nacos.discovery.server-addr"));
			assertTrue(properties.containsKey("spring.cloud.nacos.config.server-addr"));
		}
	}
}
