package com.example.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.ServiceSettings;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import android.content.ServiceConnection;
import android.content.ComponentName;

import android.os.IBinder;

import android.widget.EditText;

import java.util.Locale;
public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

//    static{
////        System.loadLibrary("libAMapSDK_MAP_v10_1_500");
//            System.load("D://android_studio_projects//1 (3)//MyApplication (2)//MyApplication//app//src//main//jniLibs//arm64-v8a//libAMapSDK_MAP_v10_1_500.so");
//    }


    /* ====================== 视图组件 ====================== */
    private TextView tvTimer, tvDistance, tvInfo, tvPace;
    private Button btnRun, btnStop;
    private MapView mapView;

    /* ====================== 地图相关 ====================== */
    private AMap aMap;
    private Polyline polyline;
    private final List<LatLng> trackPoints = new ArrayList<>();

    /* ====================== 定位相关 ====================== */
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;

    /* ====================== 跑步状态管理 ====================== */
    private enum RunState { IDLE, RUNNING, PAUSED }
    private RunState state = RunState.IDLE;

    private long startTime = 0;
    private long totalPauseDuration = 0;
    private long pauseTime = 0;
    private double totalDistance = 0;
    private LatLng lastLatLng = null;
    private boolean isFirstLocation = true;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    /* ====================== 配速计算 ====================== */
    private double lastPaceDistance = 0;
    private long lastPaceTime = 0;
    private static final double PACE_SEGMENT_DISTANCE = 100.0;
    private List<Double> paceHistory = new ArrayList<>();

    /* ====================== 权限和常量 ====================== */
    private static final int PERM_CODE = 100;
    private static final int FOREGROUND_SERVICE_ID = 1;

    /* ====================== 广播接收器 ====================== */
    private BroadcastReceiver locReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            long time = intent.getLongExtra("time", 0);
            float acc = intent.getFloatExtra("accuracy", 999);
            handleLocation(lat, lng, time, acc);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);
//
//        setContentView(R.layout.activity_main1);
//        EditText name=this.findViewById(R.id.name);
//        EditText password =this.findViewById(R.id.password);
//        EditText confirm_password=this.findViewById(R.id.confirm_password);
//        Button login=this.findViewById(R.id.button_login);
//        Button register=this.findViewById(R.id.button_register);
//        MySqliteOpenHelper sql= new MySqliteOpenHelper(this);

        initViews(savedInstanceState);
        initPrivacy();
        initLocation();
        checkingAndroidVersion();
        setupButtonListeners();
        updateButtons();
        requestBatteryWhiteList();
        requestPerm();

        findViewById(R.id.btn_history).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }

    private void initViews(Bundle savedInstanceState) {
        tvTimer = findViewById(R.id.tv_timer);
        tvDistance = findViewById(R.id.tv_distance);
        tvInfo = findViewById(R.id.tv_content);
        tvPace = findViewById(R.id.tv_pace);
        btnRun = findViewById(R.id.btn_run);
        btnStop = findViewById(R.id.btn_stop);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        initMap();
    }

    private void initMap() {
        aMap = mapView.getMap();
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
        aMap.setMinZoomLevel(15f);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps));
        myLocationStyle.strokeColor(Color.BLUE);
        myLocationStyle.radiusFillColor(Color.argb(50, 0, 0, 255));

        aMap.setOnMapLoadedListener(() -> {
            Log.d("MAP", "地图加载成功");
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "地图加载成功", Toast.LENGTH_SHORT).show());
        });

        aMap.setMyLocationStyle(myLocationStyle);
    }

    private void setupButtonListeners() {
        btnRun.setOnClickListener(v -> toggleRunPause());
        btnStop.setOnClickListener(v -> stopRun());
    }

    private void initPrivacy() {
        Context ctx = this;
        AMapLocationClient.updatePrivacyShow(ctx, true, true);
        AMapLocationClient.updatePrivacyAgree(ctx, true);
        MapsInitializer.updatePrivacyShow(ctx, true, true);
        MapsInitializer.updatePrivacyAgree(ctx, true);
        ServiceSettings.updatePrivacyShow(ctx, true, true);
        ServiceSettings.updatePrivacyAgree(ctx, true);
    }

    private void initLocation() {
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationOption = new AMapLocationClientOption();

            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            mLocationOption.setOnceLocation(false);
            mLocationOption.setOnceLocationLatest(false);
            mLocationOption.setNeedAddress(false); // 简化配置，不需要地址
            mLocationOption.setWifiScan(true);
            mLocationOption.setLocationCacheEnable(false);
            mLocationOption.setHttpTimeOut(20000);

            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.setLocationListener(this);

        } catch (Exception e) {
            Log.e("LOCATION", "定位初始化失败", e);
            Toast.makeText(this, "定位初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /* ====================== 权限管理 ====================== */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPerm();
        } else {
            startLoc();
        }
    }

    @AfterPermissionGranted(PERM_CODE)
    private void requestPerm() {
        String[] perms = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            perms = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION

            };
        }
        if (EasyPermissions.hasPermissions(this, perms)) {
            startLoc();
        } else {
            EasyPermissions.requestPermissions(this, "需要定位权限来记录运动轨迹", PERM_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (requestCode == PERM_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startLoc();
            } else {
                Toast.makeText(this, "定位权限被拒绝，无法记录运动轨迹", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLoc() {
        if (mLocationClient != null) {
            try {
                mLocationClient.startLocation();
                Log.d("LOCATION", "定位服务已启动");
                tvInfo.setText("正在定位中...");
            } catch (Exception e) {
                Log.e("LOCATION", "启动定位失败", e);
                Toast.makeText(this, "启动定位失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* ====================== 跑步状态控制 ====================== */
    private void toggleRunPause() {
        switch (state) {
            case IDLE:
                startRun();
                break;
            case RUNNING:
                pauseRun();
                break;
            case PAUSED:
                resumeRun();
                break;
        }
    }

    private void startRun() {
        Log.d("RUN", "开始新的跑步");
        state = RunState.RUNNING;

        // 重置所有数据
        startTime = SystemClock.elapsedRealtime();
        totalPauseDuration = 0;
        totalDistance = 0;
        lastLatLng = null;
        trackPoints.clear();
        isFirstLocation = true;

        // 重置配速计算
        lastPaceDistance = 0;
        lastPaceTime = 0;
        paceHistory.clear();

        // 清除轨迹
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }

        // 更新UI
        tvTimer.setText("00:00:00");
        tvDistance.setText("0.00 km");
        tvPace.setText("--:--");
        tvInfo.setText("开始跑步...");

        // 启动服务
        startLoc();
        startForegroundService();
        startTimer();
        updateButtons();
    }

    private void resumeRun() {
        Log.d("RUN", "继续跑步");
        state = RunState.RUNNING;

        // 恢复时重置配速计算段
        lastPaceDistance = totalDistance;
        lastPaceTime = SystemClock.elapsedRealtime() - startTime - totalPauseDuration;

        // 计算暂停期间的时间
        if (pauseTime > 0) {
            long currentPauseDuration = SystemClock.elapsedRealtime() - pauseTime;
            totalPauseDuration += currentPauseDuration;
            pauseTime = 0;
        }

        startLoc();
        startTimer();

        Intent intent = new Intent(this, RunForegroundService.class);
        intent.setAction("RESUME");
        startService(intent);

        tvInfo.setText("继续跑步...");
        updateButtons();
    }

    private void pauseRun() {
        Log.d("RUN", "暂停跑步");
        state = RunState.PAUSED;

        showAveragePace();
        pauseTime = SystemClock.elapsedRealtime();

        timerHandler.removeCallbacks(timerRunnable);
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }

        Intent intent = new Intent(this, RunForegroundService.class);
        intent.setAction("PAUSE");
        startService(intent);

        tvInfo.setText("已暂停");
        updateButtons();
    }

    private void stopRun() {
        Log.d("RUN", "结束跑步");
        state = RunState.IDLE;

        Intent intent = new Intent(this, RunForegroundService.class);
        intent.setAction("STOP");
        startService(intent);

        timerHandler.removeCallbacks(timerRunnable);
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
        pauseTime = 0;

        String time = tvTimer.getText().toString();
        double distanceKm = totalDistance / 1000;
        String msg = String.format("跑步结束！用时 %s  距离 %.2f km", time, distanceKm);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        uploadRunRecordToBackend();
        tvInfo.setText("跑步结束");
        updateButtons();
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, RunForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /* ====================== 计时器管理 ====================== */
    private void startTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (state == RunState.RUNNING) {
                    long currentTime = SystemClock.elapsedRealtime();
                    long effectiveRunTime;

                    if (pauseTime > 0) {
                        effectiveRunTime = pauseTime - startTime - totalPauseDuration;
                    } else {
                        effectiveRunTime = currentTime - startTime - totalPauseDuration;
                    }

                    int sec = (int) (effectiveRunTime / 1000);
                    int min = sec / 60;
                    int hour = min / 60;
                    tvTimer.setText(String.format("%02d:%02d:%02d", hour, min % 60, sec % 60));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    /* ====================== 定位回调处理 ====================== */
    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location == null) {
            Log.e("LOCATION_DEBUG", "收到的 location 对象为 null");
            return;
        }

        // 记录定位信息
        Log.d("LOCATION_DEBUG", "经纬度: " + location.getLatitude() + ", " + location.getLongitude());
        Log.d("LOCATION_DEBUG", "精度: " + location.getAccuracy());
        Log.d("LOCATION_DEBUG", "提供者: " + location.getProvider());

        if (location.getErrorCode() != 0) {
            String errorText = "定位错误[" + location.getErrorCode() + "]: " + location.getErrorInfo();
            Log.e("LOCATION_DEBUG", errorText);
            tvInfo.setText(errorText);
            return;
        }

        // 简化地址显示，只显示精度信息
        String displayText = String.format("精度: %.1f米 | 定位成功", location.getAccuracy());
        tvInfo.setText(displayText);

        // 确保地图监听器被设置
        if (mListener != null) {
            mListener.onLocationChanged(location);
        }

        // 只有当在跑步状态时才处理轨迹
        if (state == RunState.RUNNING) {
            handleLocation(location.getLatitude(), location.getLongitude(),
                    location.getTime(), location.getAccuracy());
        }
    }

    private void handleLocation(double lat, double lng, long time, float accuracy) {
        Log.d("LOCATION", "处理定位: " + lat + ", " + lng + ", 精度: " + accuracy);

        LatLng current = new LatLng(lat, lng);

        // 精度过滤
        if (accuracy > 25) {
            Log.d("LOCATION", "精度较差，跳过该点: " + accuracy);
            return;
        }

        // 首次定位处理
        if (isFirstLocation) {
            isFirstLocation = false;
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 17f));
            Log.d("LOCATION", "首次定位，移动地图到当前位置");
            lastLatLng = current;
        }

        // 添加到轨迹点
        trackPoints.add(current);
        Log.d("TRACK", "轨迹点数量: " + trackPoints.size());

        // 计算距离
        if (lastLatLng != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    lastLatLng.latitude, lastLatLng.longitude,
                    current.latitude, current.longitude, results);
            float distance = results[0];

            Log.d("DISTANCE", "两点距离: " + distance + "米");

            // 距离滤波
            if (distance < 50 && distance > 0.1) {
                totalDistance += distance;
                updateDistanceDisplay();
                Log.d("DISTANCE", "新增距离: " + distance + "米, 总距离: " + totalDistance + "米");

                updatePaceDisplay();
            } else {
                Log.d("DISTANCE", "距离异常，跳过: " + distance + "米");
            }
        }

        lastLatLng = current;
        updateTrackPolyline();

        // 定期跟随位置
        if (trackPoints.size() % 3 == 0) {
            aMap.animateCamera(CameraUpdateFactory.newLatLng(current));
        }
    }
    /* ====================== 轨迹绘制 ====================== */
    private void updateTrackPolyline() {
        if (trackPoints.size() < 2) return;

        if (polyline != null) {
            polyline.remove();
        }

        polyline = aMap.addPolyline(new PolylineOptions()
                .addAll(trackPoints)
                .width(8f)
                .color(Color.parseColor("#2196F3")));
    }

    /* ====================== 配速计算和显示 ====================== */
    private void showAveragePace() {
        if (totalDistance <= 0) {
            tvPace.setText("--:--");
            return;
        }

        long runningTime = SystemClock.elapsedRealtime() - startTime - totalPauseDuration;
        double averagePace = calculateCurrentPace(totalDistance, runningTime);
        updatePaceUI(averagePace);

        Log.d("PACE", "平均配速: " + averagePace + " min/km");
    }

    private void updatePaceDisplay() {
        if (state != RunState.RUNNING) {
            return;
        }

        long currentTime = SystemClock.elapsedRealtime();
        long runningTime = currentTime - startTime - totalPauseDuration;

        double distanceSinceLastPace = totalDistance - lastPaceDistance;
        long timeSinceLastPace = runningTime - lastPaceTime;

        boolean shouldUpdatePace = distanceSinceLastPace >= PACE_SEGMENT_DISTANCE ||
                timeSinceLastPace >= 30000;

        if (shouldUpdatePace && distanceSinceLastPace > 10) {
            double currentPace = calculateCurrentPace(distanceSinceLastPace, timeSinceLastPace);
            updatePaceUI(currentPace);
            paceHistory.add(currentPace);
            lastPaceDistance = totalDistance;
            lastPaceTime = runningTime;

            Log.d("PACE", "实时配速: " + currentPace + " min/km, 距离增量: " +
                    String.format("%.1f", distanceSinceLastPace) + "m");
        }
    }

    private double calculateCurrentPace(double distanceMeters, long timeMillis) {
        if (distanceMeters <= 0 || timeMillis <= 0) {
            return 0;
        }

        double distanceKm = distanceMeters / 1000.0;
        double timeMinutes = timeMillis / 1000.0 / 60.0;

        if (distanceKm <= 0) {
            return 0;
        }

        return timeMinutes / distanceKm;
    }

    private void updatePaceUI(double pace) {
        if (pace <= 0 || Double.isInfinite(pace) || Double.isNaN(pace)) {
            tvPace.setText("--:--");
            tvPace.setTextColor(Color.GRAY);
            return;
        }

        int minutes = (int) pace;
        int seconds = (int) ((pace - minutes) * 60);
        String paceText = String.format("%d:%02d", minutes, seconds);
        tvPace.setText(paceText);

        setPaceColorHint((float) pace);
    }

    private void setPaceColorHint(float currentPace) {
        int color;
        if (currentPace < 5.0f) {
            color = Color.GREEN;
        } else if (currentPace < 7.0f) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }
        tvPace.setTextColor(color);
    }

    /* ====================== 数据上传 ====================== */
    private String convertTrackPointsToJson() {
        try {
            if (trackPoints == null || trackPoints.isEmpty()) {
                return "[]";
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < trackPoints.size(); i++) {
                LatLng point = trackPoints.get(i);
                sb.append("{\"latitude\":").append(point.latitude)
                        .append(",\"longitude\":").append(point.longitude)
                        .append("}");
                if (i < trackPoints.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();

        } catch (Exception e) {
            Log.e("DATA_CONVERT", "轨迹点转换失败", e);
            return "[]";
        }
    }
    // 处理后台定位点的距离计算
    private void processBackgroundLocations() {
        try {
            // 绑定服务获取后台定位数据
            Intent serviceIntent = new Intent(this, RunForegroundService.class);
            bindService(serviceIntent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    RunForegroundService.RunServiceBinder binder =
                            (RunForegroundService.RunServiceBinder) service;
                    List<AMapLocation> bgLocations = binder.getBackgroundLocations();

                    if (bgLocations != null && !bgLocations.isEmpty()) {
                        Log.d("BACKGROUND", "处理后台定位点: " + bgLocations.size());
                        calculateBackgroundDistance(bgLocations);
                        binder.clearBackgroundLocations();
                    }

                    unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("SERVICE", "服务连接断开");
                }
            }, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.e("BACKGROUND", "处理后台定位点失败", e);
        }
    }

    // 计算后台定位点的距离
    private void calculateBackgroundDistance(List<AMapLocation> bgLocations) {
        if (bgLocations.size() < 2 || lastLatLng == null) return;

        double additionalDistance = 0;
        LatLng previousPoint = lastLatLng;

        for (AMapLocation location : bgLocations) {
            LatLng currentPoint = new LatLng(location.getLatitude(), location.getLongitude());

            // 计算两点距离
            float[] results = new float[1];
            Location.distanceBetween(
                    previousPoint.latitude, previousPoint.longitude,
                    currentPoint.latitude, currentPoint.longitude, results);
            float distance = results[0];

            // 距离滤波
            if (distance < 50 && distance > 0.1) {
                additionalDistance += distance;
                Log.d("BACKGROUND", "后台距离增量: " + distance + "米");
            }

            previousPoint = currentPoint;
        }

        // 更新总距离
        if (additionalDistance > 0) {
            totalDistance += additionalDistance;
            updateDistanceDisplay();
            Log.d("BACKGROUND", "后台总距离增量: " + additionalDistance + "米");
        }
    }

    // 更新距离显示
    private void updateDistanceDisplay() {
        runOnUiThread(() -> {
            String distanceText = String.format(Locale.getDefault(), "%.2f km", totalDistance / 1000);
            tvDistance.setText(distanceText);
            Log.d("DISTANCE", "更新总距离: " + totalDistance + "米");
        });
    }
    private void uploadRunRecordToBackend() {
        try {
            // 计算跑步时长（毫秒）
            long runningTime = SystemClock.elapsedRealtime() - startTime - totalPauseDuration;

            // 计算平均配速
            double averagePace = calculateCurrentPace(totalDistance, runningTime);

            // 创建实体对象并保存到数据库
            RunRecordEntity record = new RunRecordEntity(
                    "run_" + System.currentTimeMillis(), // recordId
                    "test_user_001", // userId
                    System.currentTimeMillis(), // startTime (使用当前时间戳，而不是相对时间)
                    runningTime, // duration
                    totalDistance,
                    averagePace,
                    convertTrackPointsToJson() // trackPoints
            );

            // 保存到数据库
            new Thread(() -> {
                try {
                    AppDatabase.getInstance(MainActivity.this).runRecordDao().insert(record);
                    Log.d("DB", "跑步记录已保存: " + record.getRecordId());
                    Log.d("DB", "距离: " + record.getTotalDistance() + "米");
                    Log.d("DB", "时长: " + record.getDuration() + "毫秒");
                    Log.d("DB", "轨迹点数: " + trackPoints.size());

                    // 在主线程显示保存成功的提示
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "记录已保存", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    Log.e("DB", "保存记录失败", e);
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();

        } catch (Exception e) {
            Log.e("UPLOAD", "创建跑步记录异常", e);
            Toast.makeText(this, "数据保存异常: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void requestBatteryWhiteList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }
    /* ====================== 定位源接口 ====================== */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        Log.d("LOCATION", "定位源激活");
    }

    @Override
    public void deactivate() {
        mListener = null;
        Log.d("LOCATION", "定位源停用");
    }

    /* ====================== 按钮状态刷新 ====================== */
    private void updateButtons() {
        switch (state) {
            case IDLE:
                btnRun.setText("开始跑步");
                btnStop.setEnabled(false);
                break;
            case RUNNING:
                btnRun.setText("暂停");
                btnStop.setEnabled(true);
                break;
            case PAUSED:
                btnRun.setText("继续");
                btnStop.setEnabled(true);
                break;
        }
    }

    /* ====================== 生命周期管理 ====================== */
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locReceiver, new IntentFilter("RUN_LOCATION"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        // APP回到前台时，处理后台积累的定位点
        if (state == RunState.RUNNING) {
            processBackgroundLocations();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mapView.onDestroy();
    }
}

