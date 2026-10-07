package org.example.cloudgateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Manual verification for Nacos-backed client-side load balancing.
 * Start Nacos and two account-service instances before running this test.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.cloud.nacos.discovery.enabled=true",
                "spring.cloud.nacos.discovery.register-enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.sentinel.enabled=false"
        })
class LoadBalancerVerificationTest {
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Test
    void choosesAccountServiceInstancesFourTimes() {
        System.out.println("开始验证 account-service 负载均衡：");
        for (int i = 1; i <= 4; i++) {
            ServiceInstance chosen = loadBalancerClient.choose("account-service");
            assertNotNull(chosen, "Nacos 中没有可用的 account-service 实例");
            System.out.printf("第 %d 次选择：%s:%d%n", i, chosen.getHost(), chosen.getPort());
        }
    }
}
