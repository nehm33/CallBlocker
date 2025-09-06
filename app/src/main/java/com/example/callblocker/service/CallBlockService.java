package com.example.callblocker.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.ServiceCompat;

public class CallBlockService extends Service {
    private static final String TAG = "CallBlockService";
    private static final int FOREGROUND_SERVICE_ID = 1000;
    private NotificationService notificationService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service créé");
        notificationService = new NotificationService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service démarré");

        try {
            startForegroundService();
            return START_STICKY; // Redémarre automatiquement si tué par le système
        } catch (Exception e) {
            Log.e(TAG, "Erreur démarrage service en premier plan: " + e.getMessage());
            // Service normal sans premier plan en cas d'erreur
            return START_NOT_STICKY;
        }
    }

    private void startForegroundService() {
        try {
            Notification notification = notificationService.createForegroundServiceNotification();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
                ServiceCompat.startForeground(
                        this,
                        FOREGROUND_SERVICE_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                );
            } else { // API 29+
                startForeground(FOREGROUND_SERVICE_ID, notification);
            }

            Log.d(TAG, "Service en premier plan démarré avec succès");

        } catch (SecurityException e) {
            Log.e(TAG, "Erreur de permission pour service en premier plan: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "Erreur générale service en premier plan: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service détruit");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}