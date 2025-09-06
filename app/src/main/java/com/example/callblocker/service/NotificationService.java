package com.example.callblocker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import com.example.callblocker.MainActivity;
import com.example.callblocker.R;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationService {
    private static final String CHANNEL_ID = "call_blocker_channel";
    private static final String CHANNEL_NAME = "Appels Bloqués";
    private static final int NOTIFICATION_ID = 1001;

    private final Context context;

    public void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Notifications pour les appels bloqués");
        channel.enableVibration(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showBlockedCallNotification(String phoneNumber) {
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_block)
                .setContentTitle("Appel bloqué")
                .setContentText("Numéro bloqué: " + phoneNumber)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 300, 300});

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public Notification createForegroundServiceNotification() {
        createNotificationChannel();

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Bloqueur d'appels actif")
                .setContentText("Service de blocage d'appels en cours d'exécution")
                .setSmallIcon(R.drawable.ic_block)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}