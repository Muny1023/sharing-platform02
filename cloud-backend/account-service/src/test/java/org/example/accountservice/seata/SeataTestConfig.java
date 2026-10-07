package org.example.accountservice.seata;

/** Local-only settings for running the Seata HTTP demo clients from the IDE. */
public final class SeataTestConfig {
    /** Paste a current application JWT here when running the clients without arguments. */
    public static final String SEATA_TEST_JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoidGVzdCIsImlkIjoxLCJleHAiOjE3OTE2Mjc1MDYsImlhdCI6MTc5MTAyMjcwNiwianRpIjoiMTE0YjZjNWItZTBmOC00ZDc4LThiNjMtNTVjM2IzNzU0MGRiIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV91c2VyIl19.Kgys0SZ9aYtaq6o_JLijS4afPLAcqIRSrDS0I7BdLYo";

    public static final String SEATA_TEST_BASE_URL = "http://localhost:8080";
    public static final String SEATA_TEST_COMMIT_DEMO_KEY = "";
    public static final String SEATA_TEST_ROLLBACK_DEMO_KEY = "";

    private SeataTestConfig() {
    }
}
