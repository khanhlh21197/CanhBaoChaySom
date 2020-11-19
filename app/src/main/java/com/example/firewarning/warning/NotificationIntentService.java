package com.example.firewarning.warning;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.example.firewarning.ui.main.TempMonitoringService;

import java.util.Objects;

public class NotificationIntentService extends IntentService {
    public NotificationIntentService() {
        super("name");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case "stopWarning":
                    int idNoti = intent.getIntExtra("idNoti", 0);
                    NotificationManager notificationManager = (NotificationManager)
                            getSystemService(Context.
                                    NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancelAll();
                    }
                    Handler stopWarningHandler = new Handler(Looper.getMainLooper());
                    stopWarningHandler.post(() -> {
                        Intent warningService
                                = new Intent(NotificationIntentService.this, TempMonitoringService.class);
                        stopService(warningService);
                    });
                    break;
            }
        }
    }
}
