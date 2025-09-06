package com.example.callblocker.service;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.callblocker.model.NumberRange;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;

@RequiredArgsConstructor
public class RangeManager {
    private static final String PREFS_NAME = "CallBlockerPrefs";
    private static final String RANGES_KEY = "number_ranges";

    private final Context context;
    private final Gson gson = new Gson();

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
        List<NumberRange> ranges = loadRanges();
        return ranges.stream()
                .anyMatch(range -> range.isNumberInRange(phoneNumber));
    }
}