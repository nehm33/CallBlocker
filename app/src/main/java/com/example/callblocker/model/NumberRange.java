package com.example.callblocker.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NumberRange {
    private String name;
    private String startNumber;
    private String endNumber;
    private boolean isActive;

    public boolean isNumberInRange(String phoneNumber) {
        if (!isActive || phoneNumber == null) {
            return false;
        }

        // Nettoyer le numéro (enlever espaces, tirets, etc.)
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-\\+\\(\\)]", "");
        String cleanStart = startNumber.replaceAll("[\\s\\-\\+\\(\\)]", "");
        String cleanEnd = endNumber.replaceAll("[\\s\\-\\+\\(\\)]", "");

        // Vérifier si le numéro est dans la plage
        try {
            long number = Long.parseLong(cleanNumber);
            long start = Long.parseLong(cleanStart);
            long end = Long.parseLong(cleanEnd);

            return number >= start && number <= end;
        } catch (NumberFormatException e) {
            // Comparaison lexicographique si conversion échoue
            return cleanNumber.compareTo(cleanStart) >= 0 &&
                    cleanNumber.compareTo(cleanEnd) <= 0;
        }
    }
}