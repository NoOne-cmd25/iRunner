package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RunRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView rv = findViewById(R.id.rv_records);
        rv.setLayoutManager(new LinearLayoutManager(this));

        /* 查询 + 刷新 */
        loadData();

        /* 返回按钮 */
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadData() {
        new Thread(() -> {
            List<RunRecordEntity> list = AppDatabase.getInstance(this)
                    .runRecordDao()
                    .getAllRecords();

            // 添加调试日志
            Log.d("HISTORY", "查询到记录数量: " + list.size());
            for (RunRecordEntity record : list) {
                Log.d("HISTORY", "记录: " + record.getRecordId() +
                        ", 距离: " + record.getTotalDistance() +
                        ", 时间: " + record.getStartTime());
            }

            runOnUiThread(() -> {
                if (list.isEmpty()) {
                    Toast.makeText(this, "暂无记录", Toast.LENGTH_SHORT).show();
                    Log.d("HISTORY", "数据库中没有记录");
                } else {
                    Toast.makeText(this, "加载了 " + list.size() + " 条记录", Toast.LENGTH_SHORT).show();
                }

                adapter = new RunRecordAdapter(list, record -> {
                    /* 点击跳转详情页（待实现） */
                    Intent i = new Intent(this, DetailActivity.class);
                    i.putExtra("recordId", record.getRecordId());
                    startActivity(i);
                });
                ((RecyclerView) findViewById(R.id.rv_records)).setAdapter(adapter);
            });
        }).start();
    }
}