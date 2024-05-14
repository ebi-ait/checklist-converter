package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@JsonPropertyOrder({"label", "url"})
@Data
public class UrlLink {
  @JacksonXmlProperty(localName = "LABEL")
  private String label;

  @JacksonXmlProperty(localName = "URL")
  private String url;
}