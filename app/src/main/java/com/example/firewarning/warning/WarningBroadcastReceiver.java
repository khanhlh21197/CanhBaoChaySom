package com.example.firewarning.warning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WarningBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(
                new Intent(context, WarningService.class));
//        int resID = context.getResources().getIdentifier(fname,
//                "raw",
//                context.getPackageName());
//        mediaPlayer = MediaPlayer.create(context, resID);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
//
//                Intent serviceIntent = new Intent(context, WarningService.class);
//                context.startService(serviceIntent);
//            } else {
//                Toast.makeText(context.getApplicationContext(), "Alarm Manager just ran", Toast.LENGTH_LONG).show();
//            }
//        }
    }
}
