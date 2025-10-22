package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RunRecordAdapter extends RecyclerView.Adapter<RunRecordAdapter.VH> {

    public interface OnItemClickListener{
        void onItemClick(RunRecordEntity record);
    }

    private final List<RunRecordEntity> list;
    private final OnItemClickListener listener;

    public RunRecordAdapter(List<RunRecordEntity> list, OnItemClickListener listener){
        this.list = list;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder{
        TextView tvDate, tvDistance, tvDuration, tvPace;
        VH(@NonNull View itemView) {
            super(itemView);
            tvDate     = itemView.findViewById(R.id.tv_date);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPace     = itemView.findViewById(R.id.tv_pace);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_run_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RunRecordEntity r = list.get(position);
        h.tvDate.setText(new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault())
                .format(new Date(r.getStartTime())));

        h.tvDistance.setText(String.format(Locale.getDefault(),
                "%.2f 公里", r.getTotalDistance() / 1000));

        h.tvDuration.setText("时长 " + formatDuration(r.getDuration()));

        h.tvPace.setText("配速 " + formatPace(r.getAveragePace()));

        h.itemView.setOnClickListener(v -> listener.onItemClick(r));
    }

    @Override
    public int getItemCount() { return list.size(); }

    /* ----------------- 工具方法 ----------------- */
    private String formatDuration(long ms){
        long s = ms / 1000;
        long h = s / 3600; s %= 3600;
        long m = s / 60;   s %= 60;
        return h > 0 ? String.format(Locale.getDefault(),
                "%02d:%02d:%02d", h, m, s)
                : String.format(Locale.getDefault(),
                "%02d:%02d", m, s);
    }

    private String formatPace(double paceMinPerKm){
        if (paceMinPerKm <= 0) return "--:--";
        int min = (int) paceMinPerKm;
        int sec = (int) ((paceMinPerKm - min) * 60);
        return String.format(Locale.getDefault(), "%d:%02d", min, sec);
    }
}