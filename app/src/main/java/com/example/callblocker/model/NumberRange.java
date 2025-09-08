package com.example.callblocker.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NumberRange {
    private String name;
    private String prefix;
    private boolean isActive;

    public boolean isNumberInRange(String phoneNumber) {
        if (!isActive || phoneNumber == null) {
            return false;
        }

        // Nettoyer le numéro (enlever espaces, tirets, etc.)
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-()]", "");
        String cleanPrefix = prefix.replaceAll("[\\s\\-()]", "");

        if (cleanNumber.startsWith("+") && !cleanNumber.startsWith("+33")) {
            return false;
        }

        if (cleanNumber.startsWith("+33")) {
            cleanNumber = "0" + cleanNumber.substring(3);
        }

        return cleanNumber.startsWith(cleanPrefix);
    }
}