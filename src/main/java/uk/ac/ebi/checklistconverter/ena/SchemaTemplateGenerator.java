package uk.ac.ebi.checklistconverter.ena;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import uk.ac.ebi.checklistconverter.exception.MalformedSchemaException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SchemaTemplateGenerator {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String getBioSamplesSchema(String schemaId, String title,
                                           String description, List<Map<String, String>> propertyList) {
    VelocityEngine vEngine = new VelocityEngine();
    vEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    vEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    vEngine.init();

    // Collect all the attributes with requirements
    List<String> required = new ArrayList<>();
    List<String> recommended = new ArrayList<>();
    StringWriter attributeWriter = new StringWriter();
    Template template = vEngine.getTemplate("templates/biosamples_property_template.json");
    for (Map<String, String> p : propertyList) {
      VelocityContext ctx = new VelocityContext();
      ctx.put("property", p.get("property"));
      ctx.put("property_type", p.get("property_type"));
      ctx.put("property_description", p.get("property_description").replace("\"", "'"));
      template.merge(ctx, attributeWriter);
      attributeWriter.append(",\n");

      if (p.get("requirement").equalsIgnoreCase("mandatory")) {
        required.add(p.get("property"));
      } else if (p.get("requirement").equalsIgnoreCase("recommended")) {
        recommended.add(p.get("property"));
      }
    }
    String properties = attributeWriter.toString();
    properties = properties.substring(0, properties.length() - 2); //remove last comma

    // Write everything to main template
    StringWriter schemaWriter = new StringWriter();
    template = vEngine.getTemplate("templates/biosamples_template.vm");
    VelocityContext ctx = new VelocityContext();
    ctx.put("schema_id", schemaId);
    ctx.put("schema_title", title);
    ctx.put("schema_description", description);
    ctx.put("properties", properties);
    ctx.put("required", new JSONArray(required));
    ctx.put("recommended", new JSONArray(recommended));
    template.merge(ctx, schemaWriter);

    return prettyPrintJson(schemaWriter.toString());
  }

  public static String getEnaSchema(String schemaId, String title,
                                    String description, List<Map<String, String>> propertyList) {
    VelocityEngine vEngine = new VelocityEngine();
    vEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    vEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    vEngine.init();

    StringWriter attributeWriter = new StringWriter();
    Template attributeTemplate = vEngine.getTemplate("templates/ena_attribute.vm");
    for (Map<String, String> p : propertyList) {
      VelocityContext ctx = new VelocityContext();
      ctx.put("attribute_name", p.get("property"));
      ctx.put("synonyms", p.get("synonyms"));
      ctx.put("attribute_type", p.get("property_type"));
      ctx.put("description", p.get("property_description").replace("\"", "'"));
      ctx.put("units", p.get("units"));

      if (p.get("requirement").equalsIgnoreCase("mandatory")) {
        ctx.put("required", 1);
        ctx.put("recommended", true);
      } else if (p.get("requirement").equalsIgnoreCase("recommended")) {
        ctx.put("required", 0);
        ctx.put("recommended", true);
      } else {
        ctx.put("required", 0);
        ctx.put("recommended", false);
      }

      attributeTemplate.merge(ctx, attributeWriter);
      attributeWriter.append(",\n");
    }

    String attributeString = attributeWriter.toString();
    attributeString = attributeString.substring(0, attributeString.length() - 2); //remove last comma

    // Write everything to main template
    StringWriter schemaWriter = new StringWriter();
    Template schemaTemplate = vEngine.getTemplate("templates/ena_schema.vm");
    VelocityContext ctx = new VelocityContext();
    ctx.put("schema_id", schemaId);
    ctx.put("schema_title", title);
    ctx.put("schema_description", description);
    ctx.put("required_attributes", attributeString);
    schemaTemplate.merge(ctx, schemaWriter);

    return prettyPrintJson(schemaWriter.toString());
  }

  public static String getStringTemplate(String regex, int minLength, int maxLength, String format) {
    Map<String, Object> template = new LinkedHashMap<>();
    template.put("type", "string");
    if (regex != null && !regex.isEmpty()) {
      template.put("pattern", regex);
    }
    if (format != null && !format.isEmpty()) {
      template.put("format", format);
    }
    if (minLength > 0) {
      template.put("minLength", minLength);
    }
    if (maxLength > 0) {
      template.put("maxLength", maxLength);
    }

    return getJsonString(template);
  }

  public static String getEnumTemplate(List<String> enums) {
    return getJsonString(Map.of("enum", enums));
  }

  public static String getNumberTemplate(double multipleOf, double minimum,
                                         double maximum, double exclusiveMinimum, double exclusiveMaximum) {
    Map<String, Object> template = new LinkedHashMap<>();
    template.put("type", "number");
    template.put("multipleOf", multipleOf);
    template.put("minimum", minimum);
    template.put("exclusiveMinimum", exclusiveMinimum);
    template.put("maximum", maximum);
    template.put("exclusiveMaximum", exclusiveMaximum);

    return getJsonString(template);
  }

  public static String getIntegerTemplate() {
    return getJsonString(Map.of("type", "integer"));
  }

  public static String getJsonString(Object o) {
    String template;
    try {
      template = mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      log.error("Failed to write JSON string, " + e.getMessage());
      throw new MalformedSchemaException("Failed to write JSON string, " + e.getMessage());
    }
    return template;
  }

  public static JsonNode getJson(String s) {
    JsonNode jsonNode;
    try {
      jsonNode = mapper.readValue(s, JsonNode.class);
    } catch (JsonProcessingException e) {
      log.error("Failed to write string to JSON, " + e.getMessage());
      throw new MalformedSchemaException("Failed to write string to JSON, " + e.getMessage());
    }
    return jsonNode;
  }

  public static String prettyPrintJson(String jsonString) {
    String prttyJsonString;
    try {
      Object o = mapper.readValue(jsonString, Object.class);
      prttyJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse string, malformed JSON", e);
      throw new MalformedSchemaException("Failed to parse JSON, " + e.getMessage());
    }
    return prttyJsonString;
  }
}
