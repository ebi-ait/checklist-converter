package uk.ac.ebi.checklistconverter.model.ena;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"taxonId", "commonName", "scientificName"})
@Data
public class Organism {
  @JacksonXmlProperty(localName = "TAXON_ID")
  private String taxonId;

  @JacksonXmlProperty(localName = "COMMON_NAME")
  private String commonName;

  @JacksonXmlProperty(localName = "SCIENTIFIC_NAME")
  private String scientificName;

  // For backward compatibility with old XML only.
  @JacksonXmlProperty(localName = "ANONYMIZED_NAME")
  private String anonymizedName;

  // For backward compatibility with old XML only.
  @JacksonXmlProperty(localName = "INDIVIDUAL_NAME")
  private String individualName;
}
