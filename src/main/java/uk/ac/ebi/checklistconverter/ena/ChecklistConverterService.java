package uk.ac.ebi.checklistconverter.ena;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.checklistconverter.exception.ApplicationStateException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChecklistConverterService {
  String enaChecklistBaseUrl = "https://www.ebi.ac.uk/ena/browser/api/xml/";
  @Value("${spring.application.name}")
  String outputPath;

  private static String getTypedTemplate(Field field) {
    String fieldTypeTemplate;
    if (field.getFieldType().getTextChoiceField() != null) {
      fieldTypeTemplate = SchemaTemplateGenerator.getEnumTemplate(
          field.getFieldType().getTextChoiceField().getTextValue()
              .stream().map(TextValue::getValue).collect(Collectors.toList()));
    } else if (field.getFieldType().getTextField() != null) {
      String regex = field.getFieldType().getTextField().getRegex() != null ? field.getFieldType().getTextField().getRegex() : "";
      fieldTypeTemplate = SchemaTemplateGenerator.getStringTemplate(regex, 0, 0, "");
    } else {
      fieldTypeTemplate = SchemaTemplateGenerator.getStringTemplate("", 0, 0, "");
    }

    return fieldTypeTemplate;
  }

  public String getChecklist(String checklistId) {
    String jsonSchema;
    String jsonSchemaEna;
    try {
      EnaChecklist enaChecklist = getEnaChecklist(checklistId);
      List<Map<String, String>> properties = listProperties(enaChecklist);
      String schemaId = getSchemaId(enaChecklist);
      String title = enaChecklist.getChecklist().getDescriptor().getName();
      String description = enaChecklist.getChecklist().getDescriptor().getDescription();
      jsonSchema = SchemaTemplateGenerator.getBioSamplesSchema(schemaId, title, description, properties);
      jsonSchemaEna = SchemaTemplateGenerator.getEnaSchema(schemaId, title, description, properties);

      saveSchema(checklistId + "-ENA.json", jsonSchemaEna);
      saveSchema(checklistId + "-BSD.json", jsonSchema);
    } catch (Exception e) {
      log.error("Could not GET checklist: " + checklistId, e);
      throw new ApplicationStateException("Could not retrieve checklist for " + checklistId);
    }
    return jsonSchemaEna;
  }

  public String getChecklists() {
    List<String> accessions = getEnaChecklists();
    List<String> checklists = new ArrayList<>();
    for (String accession : accessions) {
      checklists.add(getChecklist(accession));
    }
    return checklists.toString();
  }

  public void saveSchema(String filename, String schema) {
    String path = getClass().getClassLoader().getResource("schema").getPath() + "/";
    path = "./schema/";
    try (PrintWriter out = new PrintWriter(path + filename)) {
      out.println(schema);
    } catch (FileNotFoundException e) {
      log.error("Failed to find the file ", e);
      throw new RuntimeException(e);
    }
  }

  private EnaChecklist getEnaChecklist(String checklistId) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new EnaErrorHandler());
    URI uri = URI.create(enaChecklistBaseUrl + checklistId);
    EnaChecklist enaChecklist = restTemplate.getForObject(uri, EnaChecklist.class);
    Objects.requireNonNull(enaChecklist, "Failed to retrieve ENA checklist");

    return enaChecklist;
  }

  private List<String> getEnaChecklists() {
    List<String> enaChecklists = new ArrayList<>();
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new EnaErrorHandler());
    URI uri = URI.create("https://www.ebi.ac.uk/ena/browser/api/summary/ERC000001-ERC999999?offset=0&limit=100");
    JsonNode allChecklistsJson = restTemplate.getForObject(uri, JsonNode.class);
    Objects.requireNonNull(allChecklistsJson, "Failed to retrieve ENA checklist");

    JsonNode checklistsJson = allChecklistsJson.get("summaries");
    if (checklistsJson.isArray()) {
      for (JsonNode n : checklistsJson) {
        enaChecklists.add(n.get("accession").textValue());
      }
    }

    return enaChecklists;
  }

  private String getSchemaId(EnaChecklist enaChecklist) {
    String schemaName = "ENA-" + enaChecklist.getChecklist().getDescriptor().getName().replace(" ", "_");
    String schemaVersion = "1.0.0";
    return String.format("https://www.ebi.ac.uk/biosamples/schemas/%s/%s", schemaName, schemaVersion);
  }

  private List<Map<String, String>> listProperties(EnaChecklist enaChecklist) {
    return enaChecklist.getChecklist().getDescriptor().getFieldGroups().stream().
        flatMap(group -> group.getFields().stream())
        .map(f -> Map.of("property", f.getName(),
                "synonyms", getStringRepresentationOfSynonyms(f.getSynonyms(), f.getName()),
            "property_type", getTypedTemplate(f),
            "property_description", f.getDescription() == null ? "" : f.getDescription(),
            "requirement", f.getMandatory()))
        .collect(Collectors.toList());
  }

  private static String getStringRepresentationOfSynonyms(List<String> synonyms, String fieldName) {
    List<String> names = new ArrayList<>();
    names.add(fieldName);
    if (!CollectionUtils.isEmpty(synonyms)) {
      names.addAll(synonyms);
    }
    try {
      return new ObjectMapper().writeValueAsString(names);
    } catch (JsonProcessingException e) {
      log.error("Failed to convert field_name and synonyms of the attribute: " + fieldName, e);
      throw new RuntimeException(e);
    }
  }

  static class EnaErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
      return !clientHttpResponse.getStatusCode().equals(HttpStatus.OK);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
      throw new ApplicationStateException("Could not retrieve checklist: " + clientHttpResponse.getStatusText());
    }
  }
}
