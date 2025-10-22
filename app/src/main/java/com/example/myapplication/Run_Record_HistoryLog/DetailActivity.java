package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private TextView tvDate, tvDistance, tvDuration, tvPace, tvPoints, tvCalories;
    private MapView mapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d(TAG, "DetailActivity 创建");

        initViews();
        initMap(savedInstanceState);
        loadRecordData();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void initViews() {
        try {
            tvDate = findViewById(R.id.tv_date);
            tvDistance = findViewById(R.id.tv_distance);
            tvDuration = findViewById(R.id.tv_duration);
            tvPace = findViewById(R.id.tv_pace);
            tvPoints = findViewById(R.id.tv_points);
            tvCalories = findViewById(R.id.tv_calories);
            mapView = findViewById(R.id.map_view);

            Log.d(TAG, "视图初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "视图初始化失败", e);
            Toast.makeText(this, "界面初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap(Bundle savedInstanceState) {
        try {
            mapView.onCreate(savedInstanceState);
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(true);
            aMap.getUiSettings().setCompassEnabled(true);
            Log.d(TAG, "地图初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "地图初始化失败", e);
        }
    }

    private void loadRecordData() {
        String recordId = getIntent().getStringExtra("recordId");
        Log.d(TAG, "接收到的 recordId: " + recordId);

        if (recordId == null || recordId.isEmpty()) {
            Toast.makeText(this, "记录ID为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "开始查询数据库，recordId: " + recordId);
                RunRecordEntity record = AppDatabase.getInstance(DetailActivity.this)
                        .runRecordDao()
                        .getRecordById(recordId);

                Log.d(TAG, "数据库查询结果: " + (record != null ? "找到记录" : "记录为空"));

                runOnUiThread(() -> {
                    if (record != null) {
                        displayRecord(record);
                    } else {
                        Toast.makeText(DetailActivity.this, "记录不存在", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "加载记录数据失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(DetailActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayRecord(RunRecordEntity record) {
        try {
            Log.d(TAG, "显示记录数据: " + record.getRecordId());

            // 基本信息
            displayBasicInfo(record);

            // 计算并显示卡路里
            displayCalories(record);

            // 显示轨迹地图
            displayTrackMap(record);

        } catch (Exception e) {
            Log.e(TAG, "显示记录时出错", e);
            Toast.makeText(this, "显示数据时出错", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBasicInfo(RunRecordEntity record) {
        // 日期时间
        String dateStr = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
                .format(new Date(record.getStartTime()));
        tvDate.setText(dateStr);

        // 距离
        double distanceKm = record.getTotalDistance() / 1000;
        tvDistance.setText(String.format(Locale.getDefault(), "%.2f", distanceKm));

        // 时长
        tvDuration.setText(formatDuration(record.getDuration()));

        // 配速
        tvPace.setText(formatPace(record.getAveragePace()));

        // 轨迹点数
        try {
            String pointsJson = record.getTrackPoints();
            int pointCount = pointsJson != null ? pointsJson.split("\\{").length - 1 : 0;
            tvPoints.setText("轨迹点数: " + pointCount);
        } catch (Exception e) {
            tvPoints.setText("轨迹数据: 无");
        }
    }

    private void displayCalories(RunRecordEntity record) {
        try {
            // 简单卡路里计算公式：距离(km) × 体重(kg) × 1.036
            double weight = 70.0; // 假设平均体重
            double distanceKm = record.getTotalDistance() / 1000;
            double calories = distanceKm * weight * 1.036;

            tvCalories.setText(String.format(Locale.getDefault(),
                    "消耗卡路里: %.0f 大卡", calories));
        } catch (Exception e) {
            tvCalories.setText("消耗卡路里: 计算错误");
        }
    }

    private void displayTrackMap(RunRecordEntity record) {
        try {
            String trackPointsJson = record.getTrackPoints();
            if (trackPointsJson == null || trackPointsJson.isEmpty()) {
                Toast.makeText(this, "无轨迹数据", Toast.LENGTH_SHORT).show();
                return;
            }

            List<LatLng> trackPoints = parseTrackPoints(trackPointsJson);
            if (trackPoints.isEmpty()) {
                Toast.makeText(this, "轨迹数据为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 绘制轨迹
            drawTrackOnMap(trackPoints);

            // 移动地图到轨迹区域
            moveCameraToTrack(trackPoints);

        } catch (Exception e) {
            Log.e(TAG, "显示轨迹地图失败", e);
            Toast.makeText(this, "地图显示失败", Toast.LENGTH_SHORT).show();
        }
    }

    private List<LatLng> parseTrackPoints(String trackPointsJson) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(trackPointsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject point = jsonArray.getJSONObject(i);
                double lat = point.getDouble("latitude");
                double lng = point.getDouble("longitude");
                points.add(new LatLng(lat, lng));
            }
            Log.d(TAG, "解析到轨迹点数: " + points.size());
        } catch (Exception e) {
            Log.e(TAG, "解析轨迹数据失败", e);
        }
        return points;
    }

    private void drawTrackOnMap(List<LatLng> trackPoints) {
        if (aMap == null || trackPoints.size() < 2) return;

        // 清除之前的轨迹
        aMap.clear();

        // 绘制新轨迹
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(trackPoints)
                .width(12f)
                .color(Color.argb(200, 33, 150, 243))  // 半透明蓝色
                .zIndex(1);

        aMap.addPolyline(polylineOptions);
        Log.d(TAG, "轨迹绘制完成，点数: " + trackPoints.size());
    }

    private void moveCameraToTrack(List<LatLng> trackPoints) {
        if (trackPoints.isEmpty()) return;

        if (trackPoints.size() == 1) {
            // 只有一个点，直接移动
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(trackPoints.get(0), 16f));
        } else {
            // 多个点，计算边界
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : trackPoints) {
                builder.include(point);
            }
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
        }
    }

    private String formatDuration(long ms) {
        try {
            long s = ms / 1000;
            long h = s / 3600; s %= 3600;
            long m = s / 60;   s %= 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
        } catch (Exception e) {
            return "00:00:00";
        }
    }

    private String formatPace(double paceMinPerKm) {
        try {
            if (paceMinPerKm <= 0) return "--:--";
            int min = (int) paceMinPerKm;
            int sec = (int) ((paceMinPerKm - min) * 60);
            return String.format(Locale.getDefault(), "%d:%02d", min, sec);
        } catch (Exception e) {
            return "--:--";
        }
    }

    /* ----------------- 地图生命周期管理 ----------------- */
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}