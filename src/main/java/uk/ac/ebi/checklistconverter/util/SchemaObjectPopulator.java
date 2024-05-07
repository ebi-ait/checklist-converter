package uk.ac.ebi.checklistconverter.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.ac.ebi.checklistconverter.model.Schema;
import uk.ac.ebi.checklistconverter.model.SchemaId;

import java.util.Objects;

@Component
public class SchemaObjectPopulator {

    public void populateSchema(Schema schema) {
        JsonNode node = schema.getSchema();
        String id = Objects.requireNonNull(node.get("$id"), "$id field cannot be null!").asText();
        SchemaId schemaId = SchemaIdExtractor.validateSchemaId(id);
        populateWithSchemaId(schema, schemaId);
        populateAuthority(schema);
    }

    private void populateAuthority(Schema schema) {
        String authority = schema.getAuthority();
        if (authority == null || authority.isEmpty()) {
            schema.setAuthority("BIOSAMPLES");
        }
    }

    private void populateWithSchemaId(Schema schema, SchemaId schemaId) {
        JsonNode node = schema.getSchema();
        schema.setId(schemaId.getId());
        schema.setName(schemaId.getName());
        schema.setVersion(schemaId.getVersion());
        schema.setDomain(schemaId.getDomain());
        schema.setTitle(node.get("title") != null ? node.get("title").asText() : "");
        schema.setDescription(node.get("description") != null ? node.get("description").asText() : "");
    }
}
