package org.example.accountservice.seata;

/** Local-only settings for running the Seata HTTP demo clients from the IDE. */
public final class SeataTestConfig {
    /** Paste a current application JWT here when running the clients without arguments. */
    public static final String SEATA_TEST_JWT = "";

    public static final String SEATA_TEST_BASE_URL = "http://localhost:8080";
    public static final String SEATA_TEST_COMMIT_DEMO_KEY = "";
    public static final String SEATA_TEST_ROLLBACK_DEMO_KEY = "";

    private SeataTestConfig() {
    }
}
