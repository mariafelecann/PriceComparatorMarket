package org.mariafelecan.pricecomparatormarket.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mariafelecan.pricecomparatormarket.Domain.*;
import org.mariafelecan.pricecomparatormarket.Repository.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPriceEntryRepository productPriceEntryRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBestActiveDiscounts_returnsSortedLimitedList() {
        LocalDate today = LocalDate.now();
        Discount d1 = new Discount();
        d1.setStartDate(today.minusDays(1));
        d1.setEndDate(today.plusDays(2));
        d1.setDiscountPercent(10.0);

        Discount d2 = new Discount();
        d2.setStartDate(today.minusDays(1));
        d2.setEndDate(today.plusDays(2));
        d2.setDiscountPercent(25.0);

        Discount d3 = new Discount();
        d3.setStartDate(today.minusDays(1));
        d3.setEndDate(today.plusDays(2));
        d3.setDiscountPercent(15.0);




        when(discountRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today))
                .thenReturn(Arrays.asList(d1, d2, d3));

        List<Discount> result = productService.getBestActiveDiscounts(2);

        assertEquals(2, result.size());
        assertEquals(25.0, result.get(0).getDiscountPercent());
        assertEquals(15.0, result.get(1).getDiscountPercent());
    }

    @Test
    void testGetNewDiscounts_returnsCorrectList() {
        LocalDate date = LocalDate.of(2024, 5, 23);
        Discount d1 = new Discount();
        Discount d2 = new Discount();

        when(discountRepository.findByStartDate(date)).thenReturn(List.of(d1, d2));

        List<Discount> result = productService.getNewDiscounts(date);

        assertEquals(2, result.size());
        verify(discountRepository, times(1)).findByStartDate(date);
    }

    @Test
    void testCreatePriceAlert_createsAndReturnsAlert() {
        Product product = new Product();
        product.setProductId("P123");

        when(productRepository.findByProductId("P123")).thenReturn(Optional.of(product));

        PriceAlert savedAlert = new PriceAlert();
        savedAlert.setProduct(product);
        savedAlert.setTargetPrice(new BigDecimal("10"));

        when(priceAlertRepository.save(any(PriceAlert.class))).thenReturn(savedAlert);

        PriceAlert result = productService.createPriceAlert("P123", new BigDecimal("10"));

        assertNotNull(result);
        assertEquals(product, result.getProduct());
        assertEquals(new BigDecimal("10"), result.getTargetPrice());
    }

    @Test
    void testCreatePriceAlert_throwsWhenProductNotFound() {
        when(productRepository.findByProductId("BAD_ID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                productService.createPriceAlert("BAD_ID", new BigDecimal("9.99"))
        );
    }

    @Test
    void testCheckForTriggeredPriceAlerts_triggersCorrectly() {
        Product product = new Product();
        product.setName("Test Product");

        PriceAlert alert = new PriceAlert();
        alert.setProduct(product);
        alert.setTargetPrice(new BigDecimal("10"));
        alert.setTriggered(false);

        ProductPriceEntry entry = new ProductPriceEntry();
        entry.setProduct(product);
        entry.setPrice(new BigDecimal("9"));
        entry.setDate(LocalDate.now());

        when(priceAlertRepository.findByTriggeredFalse()).thenReturn(List.of(alert));
        when(productPriceEntryRepository.findFirstByProductOrderByDateDescIdDesc(product)).thenReturn(Optional.of(entry));
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(i -> i.getArgument(0));

        List<PriceAlert> result = productService.checkForTriggeredPriceAlerts();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isTriggered());
    }

    @Test
    void testCheckForTriggeredPriceAlerts_doesNotTriggerForHighPrice() {
        Product product = new Product();
        product.setName("Test Product");

        PriceAlert alert = new PriceAlert();
        alert.setProduct(product);
        alert.setTargetPrice(new BigDecimal("10"));
        alert.setTriggered(false);

        ProductPriceEntry entry = new ProductPriceEntry();
        entry.setProduct(product);
        entry.setPrice(new BigDecimal("15"));
        entry.setDate(LocalDate.now());

        when(priceAlertRepository.findByTriggeredFalse()).thenReturn(List.of(alert));
        when(productPriceEntryRepository.findFirstByProductOrderByDateDescIdDesc(product)).thenReturn(Optional.of(entry));

        List<PriceAlert> result = productService.checkForTriggeredPriceAlerts();

        assertTrue(result.isEmpty());
        assertFalse(alert.isTriggered());
    }
}
