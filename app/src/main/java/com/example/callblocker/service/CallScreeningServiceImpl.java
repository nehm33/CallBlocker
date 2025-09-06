package com.example.callblocker.service;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

public class CallScreeningServiceImpl extends CallScreeningService {
    private static final String TAG = "CallScreeningService";
    private RangeManager rangeManager;
    private NotificationService notificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        rangeManager = new RangeManager(this);
        notificationService = new NotificationService(this);
    }

    @Override
    public void onScreenCall(Call.Details callDetails) {
        String phoneNumber = null;

        if (callDetails.getHandle() != null) {
            phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
        }

        Log.d(TAG, "Screening call from: " + phoneNumber);

        CallResponse.Builder responseBuilder = new CallResponse.Builder();

        if (phoneNumber != null && rangeManager.shouldBlockNumber(phoneNumber)) {
            Log.i(TAG, "Blocking call from: " + phoneNumber);

            // Bloquer et rejeter l'appel
            responseBuilder
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);

            // Afficher notification de blocage
            notificationService.showBlockedCallNotification(phoneNumber);

        } else {
            // Autoriser l'appel
            responseBuilder
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);
        }

        respondToCall(callDetails, responseBuilder.build());
    }
}