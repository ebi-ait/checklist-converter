package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@JsonPropertyOrder({"xrefLink", "urlLink"})
@Data
public class Link {
  @JsonProperty("xrefLink")
  @JacksonXmlProperty(localName = "XREF_LINK")
  private XrefLink xrefLink;

  @JsonProperty("urlLink")
  @JacksonXmlProperty(localName = "URL_LINK")
  private UrlLink urlLink;
}