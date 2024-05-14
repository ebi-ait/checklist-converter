package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@JsonPropertyOrder({"tag", "value", "unit"})
@Data
public class Attribute {
  @JacksonXmlProperty(localName = "TAG")
  private String tag;

  @JacksonXmlProperty(localName = "VALUE")
  private String value;

  @JacksonXmlProperty(localName = "UNITS")
  private String unit;
}
