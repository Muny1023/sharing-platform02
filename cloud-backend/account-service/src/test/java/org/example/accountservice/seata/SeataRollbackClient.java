package org.example.accountservice.seata;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Sends the intentionally failing branch of the Seata demo through the Gateway. */
public final class SeataRollbackClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final DateTimeFormatter KEY_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SeataRollbackClient() {
    }

    public static void main(String[] args) throws Exception {
        String token = value(args, 0, SeataTestConfig.SEATA_TEST_JWT);
        String baseUrl = value(args, 1, SeataTestConfig.SEATA_TEST_BASE_URL, DEFAULT_BASE_URL);
        String demoKey = value(args, 2, SeataTestConfig.SEATA_TEST_ROLLBACK_DEMO_KEY,
                "seata-rollback-" + LocalDateTime.now().format(KEY_TIME));
        String body = """
                {
                  "demoKey": "%s",
                  "accountNote": "账号回滚分支",
                  "contentNote": "内容回滚分支"
                }
                """.formatted(demoKey);

        HttpResponse<String> response = send(baseUrl + "/api/seata/demo?rollback=true", token, body);
        System.out.println("Seata 回滚测试");
        System.out.println("demoKey: " + demoKey);
        System.out.println("HTTP " + response.statusCode());
        System.out.println(response.body());
        System.out.println("回滚请求预期会返回错误；请用上面的 demoKey 查询两张 seata_demo_* 表。");
    }

    private static HttpResponse<String> send(String url, String token, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", bearer(token))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String bearer(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private static String value(String[] args, int index, String configured) {
        String value = args.length > index && args[index] != null && !args[index].isBlank()
                ? args[index] : configured;
        if (value.isBlank()) {
            throw new IllegalArgumentException("请在 SeataTestConfig 中填写 JWT");
        }
        return value;
    }

    private static String value(String[] args, int index, String configured, String fallback) {
        String value = args.length > index && args[index] != null && !args[index].isBlank()
                ? args[index] : configured;
        return value.isBlank() ? fallback : value;
    }
}
