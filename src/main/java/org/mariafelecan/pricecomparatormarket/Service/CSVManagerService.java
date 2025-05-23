package org.mariafelecan.pricecomparatormarket.Service;


import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
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
import java.util.Arrays;
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
                System.out.println(filename);
                if (filename.matches("^[a-zA-Z]+_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {
                    System.out.println("filename matches "+filename);
                    String[] parts = filename.replace(".csv", "").split("_");
                    String store = parts[0];
                    LocalDate date = LocalDate.parse(parts[1]);

                    importProductCsv(file, store, date);
                } else if (filename.matches("^[a-zA-Z]+_discounts_\\d{4}-\\d{2}-\\d{2}\\.csv$")) {
                    System.out.println("discount matches");
                    String[] parts = filename.replace(".csv", "").split("_");
                    String store = parts[0];
                    importDiscountCsv(file, store);
                }
            }
        }
    }


    public void importProductCsv(Path csvPath, String storeName, LocalDate date) throws IOException, CsvValidationException {
        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder()
                             .withSeparator(';')
                             .build())
                     .withSkipLines(1)
                     .build()) {

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
                if(productId.equals("P001"))
                {
                    System.out.println("zuzu");
                }
                Optional<Product> productOpt = productRepository.findByProductId(productId);
                if (productOpt.isEmpty()) {
                    System.err.println("Product with external ID " + productId + " not found.");
//                    continue;
                }
                Product product ;

                if (productOpt.isEmpty()) {
                    product = new Product();
                    product.setProductId(productId);
                    product.setName(productName);
                    product.setBrand(brand);
                    product.setCategory(category);
                    product.setGrammage(quantity);
                    product.setUnit(unit);

                    productRepository.save(product);
                    System.out.println(productId + "saved");
                }
                else{
                    product = productOpt.get();
                    System.out.println(productId + "found already");
                    //productRepository.save(product);
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
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder()
                             .withSeparator(';')
                             .build())
                     .withSkipLines(1)
                     .build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 9) continue;

                String productId = line[0].trim();
                System.out.println(productId);
                String productName = line[1].trim();
                System.out.println(productName);
                String brand = line[2].trim();
                System.out.println(brand);
                double quantity = Double.parseDouble(line[3].trim());
                System.out.println(quantity);
                String unit = line[4].trim();
                System.out.println(unit);
                String category = line[5].trim();
                System.out.println(category);
                LocalDate fromDate = LocalDate.parse(line[6].trim(), DATE_FORMAT);
                System.out.println(fromDate);
                LocalDate toDate = LocalDate.parse(line[7].trim(), DATE_FORMAT);
                System.out.println(toDate);
                int discountPercent = Integer.parseInt(line[8].trim());
                System.out.println(discountPercent);
                Optional<Product> productOpt = productRepository.findByProductId(productId);
                System.out.println(productOpt);
                if (productOpt.isEmpty()) {
                    System.err.println("Product with external ID " + productId + " not found.");
//                    continue;
                }

                Product product;
                if (productOpt.isEmpty()) {
                    product = new Product();
                    product.setName(productName);
                    product.setBrand(brand);
                    product.setCategory(category);
                    product.setGrammage(quantity);
                    product.setUnit(unit);
                    product = productRepository.save(product);
                }
                else{
                    product = productOpt.get();
                }
                Optional<ProductPriceEntry> priceEntryOpt = productPriceEntryRepository
                        .findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(product, storeName, fromDate);
                System.out.println("in discounts dupa price entry");
                if (priceEntryOpt.isPresent()) {
                    ProductPriceEntry priceEntry = priceEntryOpt.get();

                    Discount discount = new Discount();
                    discount.setProductPriceEntry(priceEntry);
                    discount.setStartDate(fromDate);
                    discount.setEndDate(toDate);
                    discount.setDiscountPercent(discountPercent);
                    System.out.println("discount found");

                    discountRepository.save(discount);
                } else {

                    System.out.println("No matching price entry found for product: " + product.getName());
                }


            }
        }
    }
}
