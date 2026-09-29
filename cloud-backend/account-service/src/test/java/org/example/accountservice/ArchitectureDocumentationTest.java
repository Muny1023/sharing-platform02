package org.example.accountservice;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureDocumentationTest {

	@Test
	void architectureContextAndDecisionsArePresent() {
		Path projectRoot = Path.of("..", "..").toAbsolutePath().normalize();

		assertTrue(Files.isRegularFile(projectRoot.resolve("CONTEXT.md")));
		assertTrue(Files.isRegularFile(projectRoot.resolve("docs/adr/0001-service-boundaries.md")));
		assertTrue(Files.isRegularFile(projectRoot.resolve("docs/adr/0006-eventual-consistency-for-notifications.md")));
	}
}
