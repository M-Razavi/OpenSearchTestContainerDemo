package com.example.OpenSearchTestContainerDemo.ComposeContainer;

import com.example.OpenSearchTestContainerDemo.OpenSearchTestContainerDemoApplication;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = OpenSearchTestContainerDemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true")
@ContextConfiguration(classes = AbstractOpenSearchIT.Configuration.class)
@AutoConfigureMockMvc
abstract class AbstractOpenSearchIT {

    private static final int port1 = 9500;
    private static final int port2 = 9700;
    static OpenSearchComposeContainer openSearchContainer1;
    static OpenSearchComposeContainer openSearchContainer2;

    @Autowired
    protected ApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        openSearchContainer1 =
                new OpenSearchComposeContainer(
                        port1, "index-settings.json", "index-bulk-payload.json");
        openSearchContainer1.startCluster();
        DockerComposeContainer<?> node1 = openSearchContainer1.getClusterNode();
        Assertions.assertTrue(node1.getContainerByServiceName("opensearch").isPresent());

        openSearchContainer2 =
                new OpenSearchComposeContainer(
                        port2, "index-settings.json", "index-bulk-payload2.json");
        openSearchContainer2.startCluster();
        DockerComposeContainer<?> node2 = openSearchContainer2.getClusterNode();
        Assertions.assertTrue(node2.getContainerByServiceName("opensearch").isPresent());
    }

    @AfterAll
    public static void afterAll() {
        openSearchContainer1.stopCluster();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        DockerComposeContainer<?> masterNode1 = openSearchContainer1.getClusterNode();
        Assertions.assertTrue(masterNode1.getContainerByServiceName("opensearch").isPresent());
        int mappedPort1 = masterNode1.getServicePort("opensearch", port1);
        String host1 = masterNode1.getServiceHost("opensearch", port1);
        registry.add(
                "config.opensearch.hosts1", () -> host1 + ":" + mappedPort1);
        DockerComposeContainer<?> masterNode2 = openSearchContainer2.getClusterNode();

        Assertions.assertTrue(masterNode2.getContainerByServiceName("opensearch").isPresent());
        int mappedPort2 = masterNode2.getServicePort("opensearch", port2);
        String host2 = masterNode2.getServiceHost("opensearch", port2);
        registry.add(
                "config.opensearch.hosts2", () -> host2 + ":" + mappedPort2);
    }

    @TestConfiguration
    static class Configuration {

        @Bean
        public RestClientBuilder createRestClientBuilder1() {
            DockerComposeContainer<?> masterNode = openSearchContainer1.getClusterNode();
            HttpHost httpHost =
                    new HttpHost(
                            masterNode.getServiceHost("opensearch", port1),
                            masterNode.getServicePort("opensearch", port1),
                            "http");
            return RestClient.builder(httpHost);
        }

        @Bean
        public RestClientBuilder createRestClientBuilder2() {
            DockerComposeContainer<?> masterNode = openSearchContainer2.getClusterNode();
            HttpHost httpHost =
                    new HttpHost(
                            masterNode.getServiceHost("opensearch", port2),
                            masterNode.getServicePort("opensearch", port2),
                            "http");
            return RestClient.builder(httpHost);
        }

        @Bean(destroyMethod = "close")
        public RestHighLevelClient restHighLevelClient1() {
            return new RestHighLevelClient(createRestClientBuilder1());
        }

        @Bean(destroyMethod = "close")
        public RestHighLevelClient restHighLevelClient2() {
            return new RestHighLevelClient(createRestClientBuilder2());
        }

        @Bean
        public RestClient restClient1() {
            return createRestClientBuilder1().build();
        }

        @Bean
        public RestClient restClient2() {
            return createRestClientBuilder2().build();
        }
    }
}
