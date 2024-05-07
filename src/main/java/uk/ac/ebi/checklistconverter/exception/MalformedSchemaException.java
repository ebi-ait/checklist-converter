package uk.ac.ebi.checklistconverter.exception;

/*
Custom exception for json schema errors
 */

import lombok.NonNull;

public class MalformedSchemaException extends RuntimeException {
  public MalformedSchemaException(String message, Throwable cause) {
    super(message, cause);
  }

  public MalformedSchemaException(@NonNull String message) {
    super(message);
  }
}
