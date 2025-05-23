package org.mariafelecan.pricecomparatormarket.utilis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnitConverter {

    private static final Map<String, BigDecimal> MASS_CONVERSIONS_TO_GRAMS;
    private static final Map<String, BigDecimal> VOLUME_CONVERSIONS_TO_MILLILITERS;
    private static final Map<String, String> STANDARD_BASE_UNITS;

    static {
        Map<String, BigDecimal> massMap = new HashMap<>();
        massMap.put("g", new BigDecimal("1"));
        massMap.put("gram", new BigDecimal("1"));
        massMap.put("gr", new BigDecimal("1")); // Common abbreviation
        massMap.put("kg", new BigDecimal("1000"));
        massMap.put("kilogram", new BigDecimal("1000"));
        MASS_CONVERSIONS_TO_GRAMS = Collections.unmodifiableMap(massMap);

        Map<String, BigDecimal> volumeMap = new HashMap<>();
        volumeMap.put("ml", new BigDecimal("1"));
        volumeMap.put("milliliter", new BigDecimal("1"));
        volumeMap.put("l", new BigDecimal("1000"));
        volumeMap.put("litru", new BigDecimal("1000"));
        volumeMap.put("litri", new BigDecimal("1000"));
        VOLUME_CONVERSIONS_TO_MILLILITERS = Collections.unmodifiableMap(volumeMap);

        Map<String, String> baseUnitMap = new HashMap<>();
        baseUnitMap.put("mass", "g");
        baseUnitMap.put("volume", "ml");
        baseUnitMap.put("count", "bucata");
        STANDARD_BASE_UNITS = Collections.unmodifiableMap(baseUnitMap);
    }


    public static String getUnitFamily(String unit) {
        if (unit == null) return "unknown";
        String lowerUnit = unit.toLowerCase().trim();
        if (MASS_CONVERSIONS_TO_GRAMS.containsKey(lowerUnit)) return "mass";
        if (VOLUME_CONVERSIONS_TO_MILLILITERS.containsKey(lowerUnit)) return "volume";
        if (lowerUnit.equals("bucata") || lowerUnit.equals("pieces") || lowerUnit.equals("piece")) return "count";
        return "unknown";
    }

    public static String getStandardBaseUnit(String unitFamily) {
        return STANDARD_BASE_UNITS.get(unitFamily.toLowerCase());
    }


    public static BigDecimal convertToStandardBaseUnit(BigDecimal quantity, String originalUnit) {
        if (quantity == null || originalUnit == null) {
            throw new IllegalArgumentException("Quantity and originalUnit cannot be null.");
        }

        String lowerOriginalUnit = originalUnit.toLowerCase().trim();
        String unitFamily = getUnitFamily(lowerOriginalUnit);

        switch (unitFamily) {
            case "mass":
                BigDecimal factorMass = MASS_CONVERSIONS_TO_GRAMS.get(lowerOriginalUnit);
                if (factorMass == null) {
                    throw new IllegalArgumentException("Unsupported mass unit: " + originalUnit);
                }
                return quantity.multiply(factorMass);
            case "volume":
                BigDecimal factorVolume = VOLUME_CONVERSIONS_TO_MILLILITERS.get(lowerOriginalUnit);
                if (factorVolume == null) {
                    throw new IllegalArgumentException("Unsupported volume unit: " + originalUnit);
                }
                return quantity.multiply(factorVolume);
            case "count":
                return quantity;
            default:
                throw new IllegalArgumentException("Unsupported unit family or unit: " + originalUnit);
        }
    }


    public static BigDecimal calculatePricePerStandardUnit(BigDecimal price, BigDecimal packageQuantity, String packageUnit) {
        if (price == null || packageQuantity == null || packageUnit == null || packageQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal convertedPackageQuantity = convertToStandardBaseUnit(packageQuantity, packageUnit);
            if (convertedPackageQuantity.compareTo(BigDecimal.ZERO) > 0) {
                return price.divide(convertedPackageQuantity, 4, RoundingMode.HALF_UP);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error in unit conversion for price per unit calculation: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}