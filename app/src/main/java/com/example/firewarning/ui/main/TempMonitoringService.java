package com.example.firewarning.ui.main;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.example.firewarning.MainActivity;
import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.utils.FireBaseCallBack;
import com.example.firewarning.warning.NotificationIntentService;
import com.example.firewarning.warning.WarningService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;

public class TempMonitoringService extends LifecycleService implements Serializable {
    public static final String EXTRA_BUTTON_CLICKED = "EXTRA_BUTTON_CLICKED";
    public static MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
    private Timer timer;
    public int counter = 0;
    public static MediaPlayer mediaPlayer;
    public static Intent warningService;

    public TempMonitoringService() {
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    public void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  " + (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        isRunning.setValue(true);

        initMedia();

        if (!CommonActivity.isNullOrEmpty(intent)
                && !CommonActivity.isNullOrEmpty(intent.getExtras())
                && !CommonActivity.isNullOrEmpty(intent.getExtras().getString("idDevice"))) {
            String idDevice = "";
            idDevice = Objects.requireNonNull(intent.getExtras()).getString("idDevice");
            Log.v("TempMonitoringService", "onStartCommand");
            startTimer();
//            MainViewModel viewModel = new MainViewModel();
//            if (!CommonActivity.isNullOrEmpty(idDevice)) {
//                viewModel.getAllDevices().observe(Objects.requireNonNull(this), dataSnapshot -> {
//                    getData(dataSnapshot, devices -> {
//                        if (devices != null) {
//                            for (int i = 0; i < devices.size(); i++) {
//                                Device device = devices.get(i);
//                                if (idDevice.contains(device.getId())) {
//                                    if (CommonActivity.isNullOrEmpty(device.getNO()) || CommonActivity.isNullOrEmpty(device.getNG()))
//                                        return;
//                                    if (Double.parseDouble(device.getNO()) > Double.parseDouble(device.getNG())) {
//                                        createNotification(device, i);
//                                    }
//                                }
//                            }
//                        }
//                    });
//                });
//            }
            observeDevice(idDevice);
        }
        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(1);
            stopForeground(9);
        }

        isRunning.setValue(false);

        stopService(warningService);

        Log.v("TempMonitoringService", "onDestroy");
        super.onDestroy();

        stoptimertask();
//auto restart service
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        broadcastIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(broadcastIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getData(DataSnapshot dataSnapshot, FireBaseCallBack<ArrayList<Device>> callBack) {
        GenericTypeIndicator<ArrayList<Device>> t = new GenericTypeIndicator<ArrayList<Device>>() {
        };
        ArrayList<Device> devices = dataSnapshot.getValue(t);
        callBack.afterDataChanged(devices);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification(Device device, int idNoti) {
        RemoteViews notificationLayout =
                new RemoteViews(getPackageName(), R.layout.notification_monitoring);
        String displayName = (device.getName() != null) ? device.getName() : device.getId();
        notificationLayout.setTextViewText(R.id.message, "Nhiệt độ bất thường trên "
                + displayName);

        Intent stopWarning = new Intent(this, NotificationIntentService.class);
        stopWarning.putExtra("idNoti", idNoti);
        stopWarning.setAction("stopWarning");

        notificationLayout.setOnClickPendingIntent(R.id.removeWarning,
                PendingIntent.getService(this, 0, stopWarning, PendingIntent.FLAG_UPDATE_CURRENT));
//        notificationLayout.setString(R.id.message, null, "Nhiệt độ đo được: " + ng + " trên thiết bị: " + idDevice);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        //clear just on clicked
        mBuilder.setOngoing(true);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);
        Intent ii = new Intent(getApplicationContext(), MainActivity.class);
        ii.putExtra("menuFragment", "DetailDeviceFragment");
        ii.putExtra("idDevice", device.getId());

        mBuilder.setSmallIcon(R.drawable.ic_flame);
        mBuilder.setCustomContentView(notificationLayout);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
            mBuilder.setChannelId(channelId);
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(idNoti, mBuilder.build());
        }
        startMedia();
    }

    private void initMedia() {
        mediaPlayer = MediaPlayer.create(this, R.raw.warning);
        mediaPlayer.setOnCompletionListener(mp -> stopSelf());

        warningService = new Intent(this, WarningService.class);
    }

    private void startMedia() {
        WarningService.isRunning.observe(this, aBoolean -> {
            if (!aBoolean) {
                startService(warningService);
            }
        });
    }

    public static void stopMedia() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.prepare();
                    Log.d("mediaPlayer", "stopped");
                }
                mediaPlayer.release();
            }
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.firewarning";
        String channelName = "Background TempMonitoring Service";

        //get Bundle Data

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_warning_red)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Đang theo dõi nhiệt độ")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
//                .setStyle(bigText)
                .build();
        startForeground(9, notification);
    }

    private PendingIntent onButtonNotificationClick(@IdRes int id) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_BUTTON_CLICKED, id);
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference deviceRef = database.getReference("devices");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private void observeDevice(String idDevice) {
        getData().subscribe(devices -> {
            deviceRef.removeEventListener(deviceEventListener);
            observer(devices, idDevice);
        });
//        getFBData(item -> {
//            GenericTypeIndicator<ArrayList<Device>> t = new GenericTypeIndicator<ArrayList<Device>>() {
//            };
//            ArrayList<Device> devices = item.getValue(t);
//            observer(devices, idDevice);
//        });
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void observer(ArrayList<Device> devices, String idDevice) {
        HashSet<String> idSet = new HashSet<>(Arrays.asList(idDevice.split(",")));
        if (devices != null) {
            for (Device device : devices) {
                if (idSet.contains(device.getId())) {
                    followChild(device.getIndex()).subscribe(device1 -> {
                        if (CommonActivity.isNullOrEmpty(device1.getNO())
                                || CommonActivity.isNullOrEmpty(device1.getNG()))
                            return;
                        try {
                            if (Double.parseDouble(device1.getNO()) > Double.parseDouble(device1.getNG())) {
                                createNotification(device1, device.getIndex());
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
//        if (devices != null) {
//            for (Device device : devices) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    if (idDevice.contains(device.getId())) {
//                        int index = devices.indexOf(device);
//                        deviceRef.child(String.valueOf(index))
//                                .addValueEventListener(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        Device device1 = dataSnapshot.getValue(Device.class);
//                                        if (device1 == null) return;
//                                        if (CommonActivity.isNullOrEmpty(device1.getNO()) || CommonActivity.isNullOrEmpty(device1.getNG()))
//                                            return;
//                                        if (Double.parseDouble(device1.getNO()) > Double.parseDouble(device1.getNG())) {
//                                            createNotification(device1, index);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//                    }
//                }
//            }
//        }
    }

    private Observable<Device> followChild(int index) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(index))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Device device1 = dataSnapshot.getValue(Device.class);
                            if (device1 == null) return;
                            emitter.onNext(device1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        });
    }

    private void getFBData(FireBaseCallBack<DataSnapshot> fireBaseCallBack) {
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fireBaseCallBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Observable<ArrayList<Device>> getData() {
        return Observable.create(emitter -> {
            deviceEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<ArrayList<Device>> t = new GenericTypeIndicator<ArrayList<Device>>() {
                    };
                    ArrayList<Device> devices = dataSnapshot.getValue(t);
                    emitter.onNext(devices);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            deviceRef.addValueEventListener(deviceEventListener);
        });
    }

    private ValueEventListener deviceEventListener;
}
