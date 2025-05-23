package org.mariafelecan.pricecomparatormarket.Controller;

import com.opencsv.exceptions.CsvValidationException;
import org.mariafelecan.pricecomparatormarket.Service.CSVManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/import")
public class CSVManagerController {
    private final CSVManagerService csvManagerService;
    public CSVManagerController(CSVManagerService csvManagerService) {
        this.csvManagerService = csvManagerService;
    }
    @PostMapping("/all")
    public ResponseEntity<String> importAllCsvs() {
        try {
            csvManagerService.importAllCsvFiles();
            return ResponseEntity.ok("All CSV files imported successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error during CSV import: " + e.getMessage());
        } catch (CsvValidationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("CSV Validation Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("An unexpected error occurred during CSV import: " + e.getMessage());
        }

    }

}
