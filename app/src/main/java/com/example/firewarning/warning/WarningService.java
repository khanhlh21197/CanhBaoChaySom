package com.example.firewarning.warning;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;

public class WarningService extends Service implements MediaPlayer.OnCompletionListener {
    public static Vibrator v;
    public MediaPlayer mediaPlayer;
    private String fname = "warning.mp3";
    public static MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
//        int resID = getResources().getIdentifier(fname, "raw", getPackageName());
        mediaPlayer = MediaPlayer.create(this, R.raw.warning);
        mediaPlayer.setOnCompletionListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean status = intent.getBooleanExtra("status", false);
        System.out.println("WarningService.onStartCommand: " + status);
        isRunning.setValue(true);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Log.d("mediaPlayer", "started");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning.setValue(false);
        if (v != null && v.hasVibrator()) {
            v.cancel();
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Log.d("mediaPlayer", "stopped");
        }
        mediaPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }

    private void vibrate() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!CommonActivity.isNullOrEmpty(v)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(3000);
            }
        }
    }
}
