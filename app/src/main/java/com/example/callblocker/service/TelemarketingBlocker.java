package com.example.callblocker.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TelemarketingBlocker {
    private static final String PREFS_NAME = "CallBlockerPrefs";
    private static final String TELEMARKETING_ENABLED_KEY = "telemarketing_blocking_enabled";
    private static final String PREMIUM_BLOCKING_ENABLED_KEY = "premium_blocking_enabled";
    private static final String SUSPICIOUS_PATTERNS_ENABLED_KEY = "suspicious_patterns_enabled";

    private final Context context;

    // Préfixes communs des centres d'appels en France
    private static final List<String> TELEMARKETING_PREFIXES = Arrays.asList(
            "0162", "0163", "0270", "0271", "0377", "0378", "0424", "0425",
            "0568", "0569", "0948", "0949"
    );

    // Numéros courts suspects (surtaxés)
    private static final List<String> PREMIUM_NUMBERS = Arrays.asList(
            "08", "3", "1", "36"  // 08xx, 3xxx, 1xxx, 36xx
    );

    // Patterns pour détecter les numéros générés automatiquement
    private static final List<Pattern> SUSPICIOUS_PATTERNS = Arrays.asList(
            Pattern.compile("^01[0-9]{8}$"), // 01 + 8 chiffres consécutifs
            Pattern.compile("^0[1-9](.)\\1{7}$"), // Chiffres répétitifs (ex: 0122222222)
            Pattern.compile("^0[1-9]1234567[0-9]$"), // Séquence 1234567
            Pattern.compile("^0[1-9]87654321$") // Séquence inverse
    );

    // Getters et Setters pour chaque option
    public boolean isTelemarketingBlockingEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(TELEMARKETING_ENABLED_KEY, false);
    }

    public void setTelemarketingBlockingEnabled(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(TELEMARKETING_ENABLED_KEY, enabled).apply();
    }

    public boolean isPremiumBlockingEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREMIUM_BLOCKING_ENABLED_KEY, false);
    }

    public void setPremiumBlockingEnabled(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREMIUM_BLOCKING_ENABLED_KEY, enabled).apply();
    }

    public boolean isSuspiciousPatternsEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SUSPICIOUS_PATTERNS_ENABLED_KEY, false);
    }

    public void setSuspiciousPatternsEnabled(boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SUSPICIOUS_PATTERNS_ENABLED_KEY, enabled).apply();
    }

    public boolean shouldBlockNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        String cleanNumber = phoneNumber.replaceAll("[\\s\\-()]", "");

        // Supprimer le préfixe international français
        if (cleanNumber.startsWith("+33")) {
            cleanNumber = "0" + cleanNumber.substring(3);
        } else if (cleanNumber.startsWith("0033")) {
            cleanNumber = "0" + cleanNumber.substring(4);
        }

        // Vérification selon les options activées
        if (isTelemarketingBlockingEnabled() && isPrefixSuspicious(cleanNumber)) {
            return true;
        }

        if (isPremiumBlockingEnabled() && isPremiumNumber(cleanNumber)) {
            return true;
        }

        return isSuspiciousPatternsEnabled() &&
                (hasSuspiciousPattern(cleanNumber) || isSequentialNumber(cleanNumber));
    }

    private boolean isPrefixSuspicious(String number) {
        return TELEMARKETING_PREFIXES.stream()
                .anyMatch(number::startsWith);
    }

    private boolean isPremiumNumber(String number) {
        // Numéros surtaxés
        if (number.startsWith("08") && number.length() == 10) {
            String indicator = number.substring(2, 3);
            return "1245679".contains(indicator); // 081x, 082x, 084x, etc.
        }

        // Numéros courts surtaxés
        return number.startsWith("36") ||
                (number.startsWith("3") && number.length() == 4);
    }

    private boolean hasSuspiciousPattern(String number) {
        return SUSPICIOUS_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(number).matches());
    }

    private boolean isSequentialNumber(String number) {
        if (number.length() != 10) return false;

        // Vérifier séquences croissantes/décroissantes
        String digits = number.substring(2); // Ignorer le préfixe 0X

        boolean ascending = true;
        boolean descending = true;
        boolean repeating = true;

        for (int i = 1; i < digits.length(); i++) {
            int current = Character.getNumericValue(digits.charAt(i));
            int previous = Character.getNumericValue(digits.charAt(i-1));

            if (current != previous + 1) ascending = false;
            if (current != previous - 1) descending = false;
            if (current != previous) repeating = false;
        }

        return ascending || descending || repeating;
    }

    public String getBlockReason(String phoneNumber) {
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-()]", "");

        if (cleanNumber.startsWith("+33")) {
            cleanNumber = "0" + cleanNumber.substring(3);
        } else if (cleanNumber.startsWith("0033")) {
            cleanNumber = "0" + cleanNumber.substring(4);
        }

        if (isTelemarketingBlockingEnabled() && isPrefixSuspicious(cleanNumber)) {
            return "Préfixe de centre d'appels détecté";
        }
        if (isPremiumBlockingEnabled() && isPremiumNumber(cleanNumber)) {
            return "Numéro surtaxé détecté";
        }
        if (isSuspiciousPatternsEnabled() && hasSuspiciousPattern(cleanNumber)) {
            return "Motif suspect détecté";
        }
        if (isSuspiciousPatternsEnabled() && isSequentialNumber(cleanNumber)) {
            return "Numéro généré automatiquement";
        }

        return "Numéro non bloqué par les filtres automatiques";
    }
}