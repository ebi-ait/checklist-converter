package uk.ac.ebi.checklistconverter.ena;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChecklistConverterServiceTest {

  @Autowired
  ChecklistConverterService checklistConverterService;
  @Autowired
  ResourceLoader resourceLoader;
  @Autowired
  ObjectMapper objectMapper;

  WebClient webClient = WebClient.builder().baseUrl("http://localhost:3020/validate").build();

  @Test
  void valid_json_returns_empty_validation_results() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000011");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000011/SAMEA115394517.json").getFile();
      JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

      requestNode = objectMapper.createObjectNode();
      requestNode.set("data", sampleJson);
      requestNode.set("schema", schemaJson);

      response = webClient.post()
          .uri("http://localhost:3020/validate")
          .bodyValue(requestNode)
          .retrieve()
          .toEntity(JsonNode.class)
          .toFuture()
          .get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (response.getBody() == null) {
      throw new TestAbortedException("Response body can not be empty");
    }
    assertEquals(0, response.getBody().size());
  }

  @Test
  void invalid_json_should_output_schema_error() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000011");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000011/invalid_geo_location.json").getFile();
      JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

      requestNode = objectMapper.createObjectNode();
      requestNode.set("data", sampleJson);
      requestNode.set("schema", schemaJson);

      response = webClient.post()
          .uri("http://localhost:3020/validate")
          .bodyValue(requestNode)
          .retrieve()
          .toEntity(JsonNode.class)
          .toFuture()
          .get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (response.getBody() == null) {
      throw new TestAbortedException("Response body can not be empty");
    }
    assertTrue(response.getBody().get(0).get("errors").get(0).asText().contains("must be equal to one of the allowed values"));
  }

  @Test
  void synonyms_should_be_converted_correctly_for_field_names() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000024");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000024/SAMEA110262237.json").getFile();
      JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

      requestNode = objectMapper.createObjectNode();
      requestNode.set("data", sampleJson);
      requestNode.set("schema", schemaJson);

      response = webClient.post()
          .uri("http://localhost:3020/validate")
          .bodyValue(requestNode)
          .retrieve()
          .toEntity(JsonNode.class)
          .toFuture()
          .get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (response.getBody() == null) {
      throw new TestAbortedException("Response body can not be empty");
    }
    assertEquals(0, response.getBody().size());
  }

  @Test
  void synonyms_should_be_converted_correctly_for_field_values() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000049");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000049/SAMEA112184320.json").getFile();
      JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

      requestNode = objectMapper.createObjectNode();
      requestNode.set("data", sampleJson);
      requestNode.set("schema", schemaJson);

      response = webClient.post()
          .uri("http://localhost:3020/validate")
          .bodyValue(requestNode)
          .retrieve()
          .toEntity(JsonNode.class)
          .toFuture()
          .get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (response.getBody() == null) {
      throw new TestAbortedException("Response body can not be empty");
    }
    assertEquals(0, response.getBody().size());
  }

  @Test
  void units_should_be_converted_correctly() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000052");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000052/SAMEA7025236.json").getFile();
      JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

      requestNode = objectMapper.createObjectNode();
      requestNode.set("data", sampleJson);
      requestNode.set("schema", schemaJson);

      response = webClient.post()
          .uri("http://localhost:3020/validate")
          .bodyValue(requestNode)
          .retrieve()
          .toEntity(JsonNode.class)
          .toFuture()
          .get();
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (response.getBody() == null) {
      throw new TestAbortedException("Response body can not be empty");
    }
    assertFalse(response.getBody().isEmpty());
  }
}