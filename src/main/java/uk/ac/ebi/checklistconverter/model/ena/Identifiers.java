package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

import java.util.List;

@JsonPropertyOrder({"externalAccessions", "secondaryAccessions"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Identifiers {
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "EXTERNAL_ID")
  private List<ExternalAccession> externalAccessions;
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "SECONDARY_ID")
  private List<String> secondaryAccessions;

  @JsonPropertyOrder({"db", "id"})
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Data
  public static class ExternalAccession {
    @JacksonXmlProperty(localName = "namespace", isAttribute = true)
    private String db;

    @JsonAlias("")
    @JacksonXmlText
    private String id;
  }
}