package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.ArrayList;
import java.util.List;

public class RunForegroundService extends Service implements AMapLocationListener {

    private AMapLocationClient mClient;
    private boolean isPaused = false;
    private static final String CHANNEL_ID = "run_channel";
    private static final int NOTI_ID = 1;

    // 存储后台定位点，用于距离计算
    private List<AMapLocation> backgroundLocations = new ArrayList<>();
    private final IBinder binder = new RunServiceBinder();

    public class RunServiceBinder extends Binder {
        public RunForegroundService getService() {
            return RunForegroundService.this;
        }

        public List<AMapLocation> getBackgroundLocations() {
            return new ArrayList<>(backgroundLocations);
        }

        public void clearBackgroundLocations() {
            backgroundLocations.clear();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE", "服务创建");
        startForeground(NOTI_ID, buildNotification());
        initLocation();
    }

    private void initLocation() {
        try {
            Log.d("SERVICE", "初始化后台定位");
            mClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption mOption = new AMapLocationClientOption();

            // 优化后台定位配置
            mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mOption.setInterval(3000); // 缩短间隔到3秒
            mOption.setOnceLocation(false);

            // 提高后台定位精度
            mOption.setSensorEnable(true);
            mOption.setWifiScan(true);
            mOption.setLocationCacheEnable(true);
            mOption.setGpsFirst(true); // GPS优先

            // 设置定位参数
            mOption.setHttpTimeOut(20000);
            mOption.setNeedAddress(false);

            mClient.setLocationOption(mOption);
            mClient.setLocationListener(this);
            mClient.startLocation();

            Log.d("SERVICE", "后台定位已启动，间隔: 3秒");
        } catch (Exception e) {
            Log.e("SERVICE", "后台定位初始化失败", e);
        }
    }

    @Override
    public void onLocationChanged(AMapLocation loc) {
        if (loc == null || loc.getErrorCode() != 0 || isPaused) {
            Log.d("SERVICE", "定位无效或已暂停");
            return;
        }

        // 精度过滤：只处理精度较好的点
        if (loc.getAccuracy() > 25) {
            Log.d("SERVICE", "精度较差，跳过: " + loc.getAccuracy() + "米");
            return;
        }

        Log.d("SERVICE", "后台定位: " + loc.getLatitude() + ", " + loc.getLongitude() +
                ", 精度: " + loc.getAccuracy() + "米");

        // 保存后台定位点
        backgroundLocations.add(loc);

        // 限制存储数量，避免内存溢出
        if (backgroundLocations.size() > 100) {
            backgroundLocations.remove(0);
        }

        // 发送广播给 MainActivity
        Intent i = new Intent("RUN_LOCATION");
        i.putExtra("lat", loc.getLatitude());
        i.putExtra("lng", loc.getLongitude());
        i.putExtra("accuracy", loc.getAccuracy());
        i.putExtra("time", loc.getTime());
        i.putExtra("speed", loc.getSpeed()); // 添加速度信息
        i.putExtra("from_background", true); // 标记来自后台

        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    /* ---------- 前台通知 ---------- */
    private Notification buildNotification() {
        createNotificationChannel();
        Intent stop = new Intent(this, RunForegroundService.class);
        stop.setAction("STOP");

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent stopPi = PendingIntent.getService(this, 0, stop, flags);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("跑步中 - 后台记录")
                .setContentText("正在记录轨迹和距离")
                .setOngoing(true)
                .addAction(new NotificationCompat.Action(0, "停止", stopPi))
                .build();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(
                    CHANNEL_ID, "跑步服务", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("跑步轨迹记录服务");
            c.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(c);
        }
    }

    /* ---------- 服务命令 ---------- */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if ("STOP".equals(action)) {
                stopForeground(true);
                stopSelf();
                return START_NOT_STICKY;
            } else if ("PAUSE".equals(action)) {
                isPaused = true;
                if (mClient != null) {
                    mClient.stopLocation();
                }
                updatePauseNotification();
            } else if ("RESUME".equals(action)) {
                isPaused = false;
                if (mClient != null) {
                    mClient.startLocation();
                }
                updateRunningNotification();
            }
        }
        return START_STICKY;
    }

    private void updatePauseNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setContentTitle("跑步已暂停")
                .setContentText("点击继续记录轨迹")
                .setOngoing(true)
                .build();

        NotificationManager mgr = getSystemService(NotificationManager.class);
        if (mgr != null) {
            mgr.notify(NOTI_ID, notification);
        }
    }

    private void updateRunningNotification() {
        Notification notification = buildNotification();
        NotificationManager mgr = getSystemService(NotificationManager.class);
        if (mgr != null) {
            mgr.notify(NOTI_ID, notification);
        }
    }

    /* ---------- 绑定服务 ---------- */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /* ---------- 销毁 ---------- */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient != null) {
            mClient.stopLocation();
            mClient.onDestroy();
        }
        backgroundLocations.clear();
        Log.d("SERVICE", "服务销毁");
    }
}