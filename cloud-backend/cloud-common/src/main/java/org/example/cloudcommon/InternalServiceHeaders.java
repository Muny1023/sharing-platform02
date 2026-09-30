package org.example.cloudcommon;

/**
 * Header names and validation rules shared by Java service calls.
 */
public final class InternalServiceHeaders {
    public static final String INTERNAL_TOKEN = "X-Internal-Token";
    public static final String AUTHORIZATION = "Authorization";

    private InternalServiceHeaders() {
    }

    public static boolean hasValidToken(String expected, String actual) {
        return expected != null && !expected.isBlank()
                && actual != null && expected.equals(actual);
    }
}
