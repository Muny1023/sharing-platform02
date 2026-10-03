package org.example.accountservice.seata;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Sends the successful branch of the Seata demo through the Gateway. */
public final class SeataCommitClient {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final DateTimeFormatter KEY_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private SeataCommitClient() {
    }

    public static void main(String[] args) throws Exception {
        String token = value(args, 0, SeataTestConfig.SEATA_TEST_JWT);
        String baseUrl = value(args, 1, SeataTestConfig.SEATA_TEST_BASE_URL, DEFAULT_BASE_URL);
        String demoKey = value(args, 2, SeataTestConfig.SEATA_TEST_COMMIT_DEMO_KEY,
                "seata-success-" + LocalDateTime.now().format(KEY_TIME));
        String body = """
                {
                  "demoKey": "%s",
                  "accountNote": "账号提交分支",
                  "contentNote": "内容提交分支"
                }
                """.formatted(demoKey);

        HttpResponse<String> response = send(baseUrl + "/api/seata/demo", token, body);
        System.out.println("Seata 提交测试");
        System.out.println("demoKey: " + demoKey);
        System.out.println("HTTP " + response.statusCode());
        System.out.println(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("提交事务请求失败，请检查上面的响应和服务日志");
        }
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

    private static String bearer(String token) {
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
