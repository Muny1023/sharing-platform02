package org.example.cloudcommon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalServiceHeadersTest {
    @Test
    void validatesOnlyTheConfiguredNonBlankToken() {
        assertTrue(InternalServiceHeaders.hasValidToken("secret", "secret"));
        assertFalse(InternalServiceHeaders.hasValidToken("secret", "other"));
        assertFalse(InternalServiceHeaders.hasValidToken("", ""));
        assertFalse(InternalServiceHeaders.hasValidToken(null, "secret"));
    }
}
