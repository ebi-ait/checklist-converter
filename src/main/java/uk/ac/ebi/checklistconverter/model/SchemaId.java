package uk.ac.ebi.checklistconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchemaId {
  private String id;
  private String domain;
  private String name;
  private String version;
}
