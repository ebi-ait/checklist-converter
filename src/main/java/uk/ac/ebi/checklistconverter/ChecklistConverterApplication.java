package uk.ac.ebi.checklistconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.checklistconverter.ena.ChecklistConverterService;

import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
public class ChecklistConverterApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChecklistConverterApplication.class, args);
  }

  @Bean
  public Function<String, String> convertChecklist() {
    ChecklistConverterService checklistConverterService = new ChecklistConverterService();
    return checklistConverterService::getChecklist;
  }

  @Bean
  public Supplier<String> convertChecklists() {
    ChecklistConverterService checklistConverterService = new ChecklistConverterService();
    return checklistConverterService::getChecklists;
  }

}
