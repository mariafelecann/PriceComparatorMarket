package org.mariafelecan.pricecomparatormarket.Service;

import org.mariafelecan.pricecomparatormarket.Domain.*;
import org.mariafelecan.pricecomparatormarket.Repository.*;
import org.mariafelecan.pricecomparatormarket.utilis.UnitConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPriceEntryRepository productPriceEntryRepository;
    private final DiscountRepository discountRepository;
    private final PriceAlertRepository priceAlertRepository;

    public ProductService(ProductRepository productRepository,
                          ProductPriceEntryRepository productPriceEntryRepository,
                          DiscountRepository discountRepository,
                          PriceAlertRepository priceAlertRepository) {
        this.productRepository = productRepository;
        this.productPriceEntryRepository = productPriceEntryRepository;
        this.discountRepository = discountRepository;
        this.priceAlertRepository = priceAlertRepository;

    }
    private Optional<ProductPriceEntry> getLatestPriceForProduct(Product product, String storeName) {
        if (storeName != null && !storeName.isEmpty()) {
            return productPriceEntryRepository.findFirstByProductAndStoreOrderByDateDescIdDesc(product, storeName);
        } else {
            return productPriceEntryRepository.findFirstByProductOrderByDateDescIdDesc(product);
        }
    }


//    @Transactional(readOnly = true)
//    public Map<String, ProductPriceEntry> getOptimizedShoppingBasket(Map<String, BigDecimal> productExternalIdsWithQuantities) {
//        Map<String, ProductPriceEntry> optimizedBasket = new HashMap<>();
//        LocalDate today = LocalDate.now();
//
//        for (Map.Entry<String, BigDecimal> entry : productExternalIdsWithQuantities.entrySet()) {
//            String productId = entry.getKey();
//
//            Optional<Product> productOpt = productRepository.findByProductId(productId);
//            if (productOpt.isEmpty()) {
//                System.err.println("Product with external ID " + productId + " not found for basket optimization.");
//                continue;
//            }
//            Product product = productOpt.get();
//
//            List<ProductPriceEntry> allPricesForProduct = productPriceEntryRepository.findByProductProductIdAndDateBetween(
//                    product.getProductId(), today.minusWeeks(2), today.plusDays(1)
//            );
//
//            Optional<ProductPriceEntry> cheapestPrice = allPricesForProduct.stream()
//                    .filter(ppe -> ppe.getDate().isEqual(today) || ppe.getDate().isAfter(today.minusDays(7)))
//                    .min(Comparator.comparing(ProductPriceEntry::getPrice));
//
//            if (cheapestPrice.isEmpty()) {
//                cheapestPrice = getLatestPriceForProduct(product, null);
//            }
//
//            cheapestPrice.ifPresent(ppe -> optimizedBasket.put(productId, ppe));
//        }
//        return optimizedBasket;
//    }

    public Map<String, List<ProductPriceEntry>> getOptimizedShoppingBasket(Map<String, BigDecimal> productExternalIdsWithQuantities) {

        Map<String, ProductPriceEntry> cheapestPricesForEachProduct = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Map.Entry<String, BigDecimal> entry : productExternalIdsWithQuantities.entrySet()) {
            String productId = entry.getKey();

//            Optional<Product> productOpt = productRepository.findByProductId(productId);
//            if (productOpt.isEmpty()) {
//                System.err.println("Product with external ID " + productId + " not found for basket optimization.");
//                continue;
//            }
            String cleanedProductId = productId.trim();
            Optional<Product> productOpt = productRepository.findByProductIdCustomCaseInsensitive(cleanedProductId);
            if (productOpt.isEmpty()) {
                System.err.println("DEBUG: Product with ID '" + cleanedProductId + "' not found using custom case-insensitive query.");
                continue;
            }
            Product product = productOpt.get();

            List<ProductPriceEntry> allPricesForProduct = productPriceEntryRepository.findByProductProductIdAndDateBetween(
                    product.getProductId(), today.minusWeeks(2), today.plusDays(1)
            );

            Optional<ProductPriceEntry> cheapestPrice = allPricesForProduct.stream()
                    .filter(ppe -> ppe.getDate().isEqual(today) || ppe.getDate().isAfter(today.minusDays(7)))
                    .min(Comparator.comparing(ProductPriceEntry::getPrice));

            if (cheapestPrice.isEmpty()) {
                cheapestPrice = getLatestPriceForProduct(product, null);
            }

            cheapestPrice.ifPresent(ppe -> cheapestPricesForEachProduct.put(productId, ppe));
        }

        return cheapestPricesForEachProduct.values().stream()
                .collect(Collectors.groupingBy(ProductPriceEntry::getStore));
    }

    @Transactional(readOnly = true)
    public List<Discount> getBestActiveDiscounts(int limit) {
        LocalDate today = LocalDate.now();
        List<Discount> activeDiscounts = discountRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);

        return activeDiscounts.stream()
                .sorted(Comparator.comparing(Discount::getDiscountPercent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Discount> getNewDiscounts(LocalDate date) {

        return discountRepository.findByStartDate(date);
    }


    @Transactional(readOnly = true)
    public List<ProductPriceEntry> getPriceHistory(
            String productId,
            String storeName,
            String category,
            String brand,
            LocalDate startDate,
            LocalDate endDate) {

        if (productId != null) {
            //Optional<Product> productOpt = productRepository.findByProductId(productId);
            String cleanedProductId = productId.trim();
            Optional<Product> productOpt = productRepository.findByProductIdCustomCaseInsensitive(cleanedProductId);
            if (productOpt.isEmpty()) {
                System.err.println("DEBUG: Product with ID '" + cleanedProductId + "' not found using custom case-insensitive query.");
                return List.of();
            }

//            if (productOpt.isEmpty()) {
//
//                return List.of();
//            }
            if (storeName != null && !storeName.isEmpty()) {
                return productPriceEntryRepository.findByProductProductIdAndStoreAndDateBetween(productId, storeName, startDate, endDate);
            } else {
                return productPriceEntryRepository.findByProductProductIdAndDateBetween(productId, startDate, endDate);
            }
        } else if (category != null && !category.isEmpty()) {
            List<Product> products = productRepository.findByCategoryIgnoreCase(category);
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            if (productIds.isEmpty()) return List.of();

            return productPriceEntryRepository.findAll().stream()
                    .filter(ppe -> productIds.contains(ppe.getProduct().getId()) &&
                            !ppe.getDate().isBefore(startDate) && !ppe.getDate().isAfter(endDate))
                    .collect(Collectors.toList());

        } else if (brand != null && !brand.isEmpty()) {
            List<Product> products = productRepository.findByBrandIgnoreCase(brand);
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            if (productIds.isEmpty()) return List.of();

            return productPriceEntryRepository.findAll().stream()
                    .filter(ppe -> productIds.contains(ppe.getProduct().getId()) &&
                            !ppe.getDate().isBefore(startDate) && !ppe.getDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Transactional(readOnly = true)
    public List<ProductPriceEntry> getProductSubstitutesAndRecommendations(Long originalProductId, int limit) {
        Optional<Product> originalProductOpt = productRepository.findById(originalProductId);
        if (originalProductOpt.isEmpty()) {
            return List.of();
        }
        Product originalProduct = originalProductOpt.get();
        String category = originalProduct.getCategory();

        if (category == null || category.isEmpty()) {
            return List.of();
        }

        List<Product> productsInSameCategory = productRepository.findByCategoryIgnoreCase(category);

        return productsInSameCategory.stream()
                .map(product -> getLatestPriceForProduct(product, null))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ppe -> !ppe.getProduct().getId().equals(originalProductId))
                .sorted(Comparator.comparing(ppe -> {
                    Product productForPriceEntry = ppe.getProduct();
                    if (productForPriceEntry == null || productForPriceEntry.getGrammage() == 0.0 || productForPriceEntry.getUnit() == null || productForPriceEntry.getUnit().isEmpty()) {

                        return ppe.getPrice();
                    }
                    return UnitConverter.calculatePricePerStandardUnit(
                            ppe.getPrice(),
                            BigDecimal.valueOf(productForPriceEntry.getGrammage()),
                            productForPriceEntry.getUnit()
                    );
                }))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public PriceAlert createPriceAlert(String productExternalId, BigDecimal targetPrice) {
        Optional<Product> productOpt = productRepository.findByProductId(productExternalId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product with ID " + productExternalId + " not found.");
        }
        Product product = productOpt.get();

        PriceAlert alert = new PriceAlert();
        alert.setProduct(product);
        alert.setTargetPrice(targetPrice);


        return priceAlertRepository.save(alert);
    }

    @Transactional
    public List<PriceAlert> checkForTriggeredPriceAlerts() {

        List<PriceAlert> activeAlerts = priceAlertRepository.findByTriggeredFalse();
        List<PriceAlert> triggeredAlerts = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        for (PriceAlert alert : activeAlerts) {
            Optional<ProductPriceEntry> latestPriceOpt = getLatestPriceForProduct(alert.getProduct(), null);
            if (latestPriceOpt.isPresent()) {
                ProductPriceEntry latestPrice = latestPriceOpt.get();

                if (latestPrice.getPrice().compareTo(alert.getTargetPrice()) <= 0 && latestPrice.getDate().isEqual(today)) {
                    alert.setTriggered(true);
                    priceAlertRepository.save(alert);
                    triggeredAlerts.add(alert);

                    System.out.println("ALERT TRIGGERED for Product: " + alert.getProduct().getName() +
                            " (Target: " + alert.getTargetPrice() + " RON, Current: " + latestPrice.getPrice() + " RON)");
                }
            }
        }
        return triggeredAlerts;
    }

}
