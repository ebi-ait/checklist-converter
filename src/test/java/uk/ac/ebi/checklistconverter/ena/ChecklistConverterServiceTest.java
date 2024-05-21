package uk.ac.ebi.checklistconverter.ena;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
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
  void getChecklist() throws IOException {
    String schema = checklistConverterService.getChecklist("ERC000011");
    JsonNode schemaJson = objectMapper.readValue(schema, JsonNode.class);

    File file = resourceLoader.getResource("classpath:samples/ERC000011/SAMEA115394517.json").getFile();
//    File file = resourceLoader.getResource("classpath:samples/ERC000011/invalid.json").getFile();
    JsonNode sampleJson = objectMapper.readValue(file, JsonNode.class);

    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.set("data", sampleJson);
    requestNode.set("schema", schemaJson);
    String requestString = objectMapper.writeValueAsString(requestNode);

    RestTemplate restTemplate = new RestTemplate();
    URI uri = URI.create("http://localhost:3020/validate");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request =
        new HttpEntity<String>("{\"schema\":{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\",\"properties\":{\"alias\":{\"description\":\"A sample unique identifier in a submission.\",\"type\":\"string\"},\"taxonId\":{\"description\":\"The taxonomy id for the sample species.\",\"type\":\"integer\"},\"taxon\":{\"description\":\"The taxonomy name for the sample species.\",\"type\":\"string\"},\"releaseDate\":{\"description\":\"Date from which this sample is released publicly.\",\"type\":\"string\",\"format\":\"date\"},\"disease\":{\"description\":\"The disease for the sample species.\",\"type\":\"string\",\"graphRestriction\":{\"ontologies\":[\"obo:mondo\",\"obo:efo\"],\"classes\":[\"MONDO:0000001\",\"PATO:0000461\"],\"queryFields\":[\"obo_id\",\"label\"],\"includeSelf\":true}},\"disease_id\":{\"description\":\"The ontology id for the disease sample species.\",\"type\":\"string\",\"graphRestriction\":{\"ontologies\":[\"obo:mondo\",\"obo:efo\"],\"classes\":[\"MONDO:0000001\",\"PATO:0000461\"],\"includeSelf\":true}}},\"required\":[\"alias\",\"taxonId\"]},\"data\":{\"alias\":\"MA456\",\"taxonId\":9606,\"disease\":\"glioblastoma\",\"disease_id\":\"MONDO:0018177\"}}", headers);
    request = new HttpEntity<>(requestString, headers);

    ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, request, String.class);
    JsonNode root = objectMapper.readTree(responseEntity.getBody());

    System.out.println(root);

    assertEquals("[]", responseEntity.getBody());

//    Mono<JsonNode> requestBody = new ObjectNode("", "");
//    Mono<JsonNode> entityMono = webClient.post()
//        .uri("")
//        .contentType(MediaType.APPLICATION_JSON)
//        .body()
//        .exchangeToMono(response -> {
//          if (response.statusCode().equals(HttpStatus.OK)) {
//            return response.bodyToMono(JsonNode.class);
//          }
//          else {
//            // Turn to error
//            return response.createError();
//          }
//        });
  }
}