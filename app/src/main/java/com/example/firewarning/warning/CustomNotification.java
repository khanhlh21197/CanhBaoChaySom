package com.example.firewarning.warning;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.firewarning.R;

public class CustomNotification extends NotificationCompat.Builder {
    private Context context;
    private int notificationId;

    public CustomNotification(@NonNull Context context, @NonNull String channelId, int notificationId) {
        super(context, channelId);
        this.context = context;
        this.notificationId = notificationId;
        setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        setContentTitle("Warning!!!");
        setContentText("Nhiệt độ vượt quá mức cho phép !!!");
        setPriority(NotificationCompat.PRIORITY_HIGH);
        setAutoCancel(true);

        String channelName = "Channel";
        String description = "description";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(description);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void showNotify(){
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(notificationId, build());
    }
}
