package org.mariafelecan.pricecomparatormarket.Controller;

import com.opencsv.exceptions.CsvValidationException;
import org.mariafelecan.pricecomparatormarket.Domain.Discount;
import org.mariafelecan.pricecomparatormarket.Domain.PriceAlert;
import org.mariafelecan.pricecomparatormarket.Domain.ProductPriceEntry;
import org.mariafelecan.pricecomparatormarket.Service.CSVManagerService;
import org.mariafelecan.pricecomparatormarket.Service.ProductService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PriceComparatorController {

    private final ProductService productService;
    private final CSVManagerService csvManagerService;

    public PriceComparatorController(ProductService productService, CSVManagerService csvManagerService) {
        this.productService = productService;
        this.csvManagerService = csvManagerService;
    }

    @PostMapping("/import/all")
    public ResponseEntity<String> importAllCsvFiles() {
        try {
            csvManagerService.importAllCsvFiles();
            return ResponseEntity.ok("CSV import initiated successfully!");
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


    @PostMapping("/basket-optimization")
    public ResponseEntity<Map<String, List<ProductPriceEntry>>> getOptimizedShoppingBasket(
            @RequestBody Map<String, BigDecimal> productExternalIdsWithQuantities) {
        Map<String, List<ProductPriceEntry>> optimizedBasket = productService.getOptimizedShoppingBasket(productExternalIdsWithQuantities);
        if (optimizedBasket.isEmpty() && !productExternalIdsWithQuantities.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(optimizedBasket);
    }

    @GetMapping("/discounts/best")
    public ResponseEntity<List<Discount>> getBestDiscounts(@RequestParam(defaultValue = "10") int limit) {
        List<Discount> discounts = productService.getBestActiveDiscounts(limit);
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/discounts/new")
    public ResponseEntity<List<Discount>> getNewDiscounts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<Discount> discounts = productService.getNewDiscounts(queryDate);
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/products/price-history")
    public ResponseEntity<List<ProductPriceEntry>> getPriceHistory(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ProductPriceEntry> priceHistory = productService.getPriceHistory(
                productId, storeName, category, brand, startDate, endDate
        );
        return ResponseEntity.ok(priceHistory);
    }

    @GetMapping("/products/{productId}/substitutes")
    public ResponseEntity<List<ProductPriceEntry>> getProductSubstitutes(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductPriceEntry> substitutes = productService.getProductSubstitutesAndRecommendations(productId, limit);
        if (substitutes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(substitutes);
    }

    @PostMapping("/alerts/price")
    public ResponseEntity<PriceAlert> createPriceAlert(
            @RequestParam String productId,
            @RequestParam BigDecimal targetPrice) {
        PriceAlert alert = productService.createPriceAlert(productId, targetPrice);
        return ResponseEntity.status(201).body(alert);
    }
}