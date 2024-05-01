# OpenSearchTestContainerDemo

This project is a Java application built with Spring Boot and Gradle. Its main purpose is to demonstrate the use of
Testcontainers with OpenSearch, to manage multiple instances of OpenSearch containers simultaneously. Each instance can
have separate initialization setup for index creation and initial data.

The Docker image used for initializing the OpenSearch instances is sourced
from [M-Razavi/opensearch-seed](https://github.com/M-Razavi/opensearch-seed).

This capability is particularly useful in scenarios where you need to test the behavior of your application against
different configurations of OpenSearch clusters. For example, you might want to test how your application behaves when
connected to an OpenSearch cluster with different index settings or initial data. Additionally, it can be used for
integration testing of segregated data. This means you can have separate OpenSearch instances for different sets of
data, ensuring that tests for each data set are isolated and do not interfere with each other.

## OpenSearchComposeContainer

The `OpenSearchComposeContainer` class is a key part of this project. It is used to manage an OpenSearch service within
a Docker container for testing purposes. This class allows you to start and stop an OpenSearch cluster, and retrieve the
DockerComposeContainer instance representing the cluster node. Each instance of `OpenSearchComposeContainer` can be
configured with a unique port, index setting file, and initial data file, allowing for separate initialization setup for
each OpenSearch cluster.

### Usage

To use `OpenSearchComposeContainer`, you need to instantiate it with the desired port, index setting file, and initial
data file. Here is an example:

```java
OpenSearchComposeContainer openSearchContainer = new OpenSearchComposeContainer(9200, "indexSetting.json", "initialData.json");
```

After creating an instance, you can start the OpenSearch cluster with the `startCluster()` method:

```java
openSearchContainer.startCluster();
```

You can retrieve the DockerComposeContainer instance representing the cluster node with the `getClusterNode()` method:

```java
DockerComposeContainer<?> masterNode = openSearchContainer.getClusterNode();
```

Finally, you can stop the OpenSearch cluster with the `stopCluster()` method:

```java
openSearchContainer.stopCluster();
```

### Multiple Instances

You can create multiple instances of `OpenSearchComposeContainer` to manage multiple OpenSearch clusters simultaneously.
Each instance should be configured with a unique port, index setting file, and initial data file to avoid conflicts and
allow for separate initialization setup. Here is an example:

```java
OpenSearchComposeContainer openSearchContainer1 = new OpenSearchComposeContainer(9200, "indexSetting1.json", "initialData1.json");
OpenSearchComposeContainer openSearchContainer2 = new OpenSearchComposeContainer(9201, "indexSetting2.json", "initialData2.json");

openSearchContainer1.

startCluster();
openSearchContainer2.

startCluster();

// Do some work with the clusters...

openSearchContainer1.

stopCluster();
openSearchContainer2.

stopCluster();
```

## Running Tests

To run the tests, use the following command in the project root directory:

```bash
./gradlew test
```

This way, you can manage multiple instances of OpenSearch containers, each with its own separate initialization setup
for index creation and initial data.

## Prerequisites

- Java 17
- Docker

## Dependencies

The project uses the following dependencies:

- Spring Boot
- OpenSearch Rest High Level Client
- Apache HttpClient
- Testcontainers

## License

This project is open source and available under the [MIT License](LICENSE).