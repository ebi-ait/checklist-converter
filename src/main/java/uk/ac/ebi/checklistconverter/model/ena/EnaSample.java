package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@JsonPropertyOrder({
    "alias",
    "accession",
    "identifiers",
    "centerName",
    "brokerName",
    "title",
    "description",
    "organism",
    "attributes",
    "links"
})
@Data
public class EnaSample {
  @JacksonXmlProperty(localName = "alias", isAttribute = true)
  @JsonAlias("refName")
  private String alias;

  @JacksonXmlProperty(localName = "accession", isAttribute = true)
  private String accession;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "IDENTIFIERS")
  private Identifiers identifiers;

  @JacksonXmlProperty(localName = "center_name", isAttribute = true)
  private String centerName;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @JacksonXmlProperty(localName = "broker_name", isAttribute = true)
  private String brokerName;

  @JacksonXmlProperty(localName = "TITLE")
  private String title;

  @JacksonXmlProperty(localName = "DESCRIPTION")
  private String description;

  @JacksonXmlProperty(localName = "SAMPLE_NAME")
  private Organism organism;

  @JacksonXmlElementWrapper(localName = "SAMPLE_ATTRIBUTES")
  @JacksonXmlProperty(localName = "SAMPLE_ATTRIBUTE")
  private List<Attribute> attributes;

  @JacksonXmlElementWrapper(localName = "SAMPLE_LINKS")
  @JacksonXmlProperty(localName = "SAMPLE_LINK")
  private List<Link> links;
}