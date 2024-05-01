package com.example.OpenSearchTestContainerDemo.ComposeContainer;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.io.File;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Map;

public class OpenSearchComposeContainer {
    private final int port;
    private final String indexSettingFile;
    private final String initialDataFile;
    private DockerComposeContainer<?> openSearchNode;
    private boolean isStarted = false;

    public OpenSearchComposeContainer(int port, String indexSettingFile, String initialDataFile) {
        this.port = port;
        this.indexSettingFile = indexSettingFile;
        this.initialDataFile = initialDataFile;
    }

    public void startCluster() {
        if (!isStarted) {
            System.out.println("Starting OpenSearch node...");
            String serviceName = "opensearch";
            openSearchNode =
                    new DockerComposeContainer<>(
                            new File("./src/test/resources/docker-compose.yml"))
                            .withExposedService(serviceName, port)
                            .withOptions()
                            .withEnv(
                                    Map.of(
                                            "OS_PORT",
                                            String.valueOf(port),
                                            "INDEX_SETTING_FILE",
                                            indexSettingFile,
                                            "INITIAL_DATA_FILE",
                                            initialDataFile))
                            .waitingFor(
                                    serviceName,
                                    new HttpWaitStrategy()
                                            .forPort(port)
                                            .forStatusCodeMatching(
                                                    response ->
                                                            response == HttpURLConnection.HTTP_OK)
                                            .withStartupTimeout(Duration.ofMinutes(2)));

            openSearchNode.start();
            isStarted = true;
            System.out.println("OpenSearch node started.");
        }
    }

    public DockerComposeContainer<?> getClusterNode() {
        if (!isStarted) {
            throw new IllegalStateException(
                    "Cluster has not been started. Call startCluster() first.");
        }
        return openSearchNode;
    }

    public void stopCluster() {
        if (isStarted) {
            System.out.println("Stopping OpenSearch node...");
            openSearchNode.stop();
            isStarted = false;
            System.out.println("OpenSearch node stopped.");
        }
    }
}
