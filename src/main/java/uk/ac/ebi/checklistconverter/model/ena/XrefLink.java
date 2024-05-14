package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@JsonPropertyOrder({"db", "id"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class XrefLink {
  @JacksonXmlProperty(localName = "DB")
  private String db;

  @JacksonXmlProperty(localName = "ID")
  private String id;
}