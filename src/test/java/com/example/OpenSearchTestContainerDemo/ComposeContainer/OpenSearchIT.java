package com.example.OpenSearchTestContainerDemo.ComposeContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenSearchIT extends AbstractOpenSearchIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    @Qualifier("restClient1")
    private RestClient restClient1;

    @Autowired
    @Qualifier("restClient2")
    private RestClient restClient2;

    @BeforeEach
    void setup() {
        refreshIndices(restClient1);
        refreshIndices(restClient2);
    }

    private void refreshIndices(RestClient restClient) {
        try {
            restClient.performRequest(new Request("GET", "/_refresh"));
        } catch (IOException e) {
            throw new IllegalStateException("refreshIndices failed", e);
        }
    }

    private String buildMatchAllQuery() {
        ObjectNode queryNode = OBJECT_MAPPER.createObjectNode();
        ObjectNode matchAllNode = OBJECT_MAPPER.createObjectNode();
        matchAllNode.putObject("match_all");
        queryNode.set("query", matchAllNode);
        queryNode.put("from", 0);
        queryNode.put("size", 1000);

        try {
            return OBJECT_MAPPER.writeValueAsString(queryNode);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build match all query", e);
        }
    }

    private Response performSearchRequest(RestClient restClient, String index, String jsonQuery) {
        try {
            Request request = new Request("GET", "/" + index + "/_search");
            request.setJsonEntity(jsonQuery);
            return restClient.performRequest(request);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Failed to perform search request on index %s", index), e);
        }
    }

    private List<String> parseDocuments(Response response) {
        try {
            JsonNode jsonNode =
                    OBJECT_MAPPER.readTree(
                            EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            return StreamSupport.stream(jsonNode.get("hits").get("hits").spliterator(), false)
                    .map(hitNode -> hitNode.get("_source"))
                    .map(JsonNode::toString)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("parseDocuments failed. body=" + response, e);
        }
    }

    private void checkOpenSearchTestContainer_QueryAllData_FindAllIngestedData(
            RestClient restClient, int expectedSize) {
        String index = "books";

        Response response = performSearchRequest(restClient, index, buildMatchAllQuery());

        List<String> result = parseDocuments(response);
        assertThat(response.getStatusLine().getStatusCode())
                .withFailMessage("Failed to fetch all documents from index %s: %s", index, response)
                .isBetween(200, 299);
        assertThat(result)
                .withFailMessage("Expected %d documents, but got %d", expectedSize, result.size())
                .hasSize(expectedSize);
    }

    @Test
    void checkOpenSearchTestContainer1_QueryAllData_FindAllIngestedData() {
        checkOpenSearchTestContainer_QueryAllData_FindAllIngestedData(restClient1, 10);
    }

    @Test
    void checkOpenSearchTestContainer2_QueryAllData_FindAllIngestedData() {
        checkOpenSearchTestContainer_QueryAllData_FindAllIngestedData(restClient2, 20);
    }
}
