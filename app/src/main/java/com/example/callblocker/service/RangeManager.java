package com.example.callblocker.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.callblocker.model.NumberRange;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RangeManager {
    private static final String PREFS_NAME = "CallBlockerPrefs";
    private static final String RANGES_KEY = "number_ranges";

    private final Context context;
    private final Gson gson = new Gson();
    private TelemarketingBlocker telemarketingBlocker;

    private TelemarketingBlocker getTelemarketingBlocker() {
        if (telemarketingBlocker == null) {
            telemarketingBlocker = new TelemarketingBlocker(context);
        }
        return telemarketingBlocker;
    }

    public void saveRanges(List<NumberRange> ranges) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(ranges);
        prefs.edit().putString(RANGES_KEY, json).apply();
    }

    public List<NumberRange> loadRanges() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(RANGES_KEY, "[]");

        Type listType = new TypeToken<List<NumberRange>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public void addRange(NumberRange range) {
        List<NumberRange> ranges = loadRanges();
        ranges.add(range);
        saveRanges(ranges);
    }

    public void removeRange(int index) {
        List<NumberRange> ranges = loadRanges();
        if (index >= 0 && index < ranges.size()) {
            ranges.remove(index);
            saveRanges(ranges);
        }
    }

    public boolean shouldBlockNumber(String phoneNumber) {
        // Vérification des plages personnalisées
        List<NumberRange> ranges = loadRanges();
        boolean blockedByRange = ranges.stream()
                .anyMatch(range -> range.isNumberInRange(phoneNumber));

        // Vérification du blocage automatique
        boolean blockedByAutoFilters = getTelemarketingBlocker()
                .shouldBlockNumber(phoneNumber);

        return blockedByRange || blockedByAutoFilters;
    }

    public String getBlockReason(String phoneNumber) {
        // Vérifier d'abord les plages personnalisées
        List<NumberRange> ranges = loadRanges();
        for (NumberRange range : ranges) {
            if (range.isNumberInRange(phoneNumber)) {
                return "Bloqué par la plage: " + range.getName();
            }
        }

        // Vérifier le blocage automatique
        if (getTelemarketingBlocker().shouldBlockNumber(phoneNumber)) {
            return getTelemarketingBlocker().getBlockReason(phoneNumber);
        }

        return "Numéro non bloqué";
    }

    public boolean isTelemarketingBlockingEnabled() {
        return getTelemarketingBlocker().isTelemarketingBlockingEnabled();
    }

    public void setTelemarketingBlockingEnabled(boolean enabled) {
        getTelemarketingBlocker().setTelemarketingBlockingEnabled(enabled);
    }

    public boolean isPremiumBlockingEnabled() {
        return getTelemarketingBlocker().isPremiumBlockingEnabled();
    }

    public void setPremiumBlockingEnabled(boolean enabled) {
        getTelemarketingBlocker().setPremiumBlockingEnabled(enabled);
    }

    public boolean isSuspiciousPatternsEnabled() {
        return getTelemarketingBlocker().isSuspiciousPatternsEnabled();
    }

    public void setSuspiciousPatternsEnabled(boolean enabled) {
        getTelemarketingBlocker().setSuspiciousPatternsEnabled(enabled);
    }
}