package uk.ac.ebi.checklistconverter.ena;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
//    String schema = checklistConverterService.getChecklist("ERC000011");
//    JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
//
////    File file = resourceLoader.getResource("classpath:samples/ERC000011/SAMEA115394517.json").getFile();
//    File file = resourceLoader.getResource("classpath:samples/ERC000011/invalid.json").getFile();
//    JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);
//
//    ObjectNode requestNode = objectMapper.createObjectNode();
//    requestNode.set("data", sampleJson);
//    requestNode.set("schema", schemaJson);
//    String requestString = objectMapper.writeValueAsString(requestNode);
//
//    RestTemplate restTemplate = new RestTemplate();
//    URI uri = URI.create("http://localhost:3020/validate");
//
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//    HttpEntity<String> request = new HttpEntity<>(requestString, headers);
//
//    ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, request, String.class);
//    JsonNode root = objectMapper.readTree(responseEntity.getBody());
//
//    System.out.println(root);
//
//    assertEquals("[]", responseEntity.getBody());



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
  void invalid_checklist_should_output_schema_error() {
    ResponseEntity<JsonNode> response;

    try {
      ObjectNode requestNode;
      String schema = checklistConverterService.getChecklist("ERC000011");
      JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);
      File file = resourceLoader.getResource("classpath:samples/ERC000011/invalid.json").getFile();
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
    assertEquals("must be equal to constant", response.getBody().get(0).get("errors").get(0).asText());
  }
}