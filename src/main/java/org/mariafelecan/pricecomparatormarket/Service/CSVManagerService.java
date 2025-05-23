package org.mariafelecan.pricecomparatormarket.Service;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.mariafelecan.pricecomparatormarket.Domain.*;
import org.mariafelecan.pricecomparatormarket.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CSVManagerService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductPriceEntryRepository productPriceEntryRepository;

    @Autowired
    private DiscountRepository discountRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void importAllCsvFiles() throws IOException, CsvValidationException {
        Path resourceDirectory = Path.of("src/main/resources/csv");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourceDirectory, "*.csv")) {
            for (Path file : stream) {
                String filename = file.getFileName().toString();

                if (filename.matches("^[a-zA-Z]+_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {

                    String[] parts = filename.replace(".csv", "").split("_");
                    String store = parts[0];
                    LocalDate date = LocalDate.parse(parts[1]);
                    importProductCsv(file, store, date);
                } else if (filename.matches("^[a-zA-Z]+_discount_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {

                    String[] parts = filename.replace(".csv", "").split("_");
                    String store = parts[0];
                    importDiscountCsv(file, store);
                }
            }
        }
    }


    public void importProductCsv(Path csvPath, String storeName, LocalDate date) throws IOException, CsvValidationException {
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext();
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                if (line.length < 8) continue;

                String productId = line[0].trim();
                String productName = line[1].trim();
                String category = line[2].trim();
                String brand = line[3].trim();
                double quantity = Double.parseDouble(line[4].trim());
                String unit = line[5].trim();
                BigDecimal priceValue = new BigDecimal(line[6].trim());
                String currency = line[7].trim();

                Product product = productRepository.findByProductId(productId);
                if (product == null) {
                    product = new Product();
                    product.setProductId(productId);
                    product.setName(productName);
                    product.setBrand(brand);
                    product.setCategory(category);
                    product.setGrammage(quantity);
                    product.setUnit(unit);
                    product = productRepository.save(product);
                }

                ProductPriceEntry priceEntry = new ProductPriceEntry();
                priceEntry.setProduct(product);
                priceEntry.setPrice(priceValue);
                priceEntry.setCurrency(currency);
                priceEntry.setStore(storeName);
                priceEntry.setDate(date);
                productPriceEntryRepository.save(priceEntry);
            }
        }
    }

    public void importDiscountCsv(Path csvPath, String storeName) throws IOException, CsvValidationException {
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 9) continue;

                String productId = line[0].trim();
                String productName = line[1].trim();
                String brand = line[2].trim();
                double quantity = Double.parseDouble(line[3].trim());
                String unit = line[4].trim();
                String category = line[5].trim();
                LocalDate fromDate = LocalDate.parse(line[6].trim(), DATE_FORMAT);
                LocalDate toDate = LocalDate.parse(line[7].trim(), DATE_FORMAT);
                int discountPercent = Integer.parseInt(line[8].trim());

                Product product = productRepository.findByProductId(productId);
                if (product == null) {
                    product = new Product();
                    product.setName(productName);
                    product.setBrand(brand);
                    product.setCategory(category);
                    product.setGrammage(quantity);
                    product.setUnit(unit);
                    product = productRepository.save(product);
                }

                Optional<ProductPriceEntry> priceEntryOpt = productPriceEntryRepository
                        .findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(product, storeName, fromDate);

                if (priceEntryOpt.isPresent()) {
                    ProductPriceEntry priceEntry = priceEntryOpt.get();

                    Discount discount = new Discount();
                    discount.setProductPriceEntry(priceEntry);
                    discount.setStartDate(fromDate);
                    discount.setEndDate(toDate);
                    discount.setDiscountPercent(discountPercent);


                    discountRepository.save(discount);
                } else {

                    System.out.println("No matching price entry found for product: " + product.getName());
                }


            }
        }
    }
}
