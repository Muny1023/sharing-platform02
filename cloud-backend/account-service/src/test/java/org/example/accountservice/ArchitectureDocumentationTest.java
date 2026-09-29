package org.example.accountservice;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
