// RunRecord.java - 在 com.example.myapplication 包中创建
package com.example.myapplication;

public class RunRecord {
    private String recordId;
    private String userId;
    private long startTime;
    private long duration;
    private double totalDistance;
    private double averagePace;
    private String trackPoints;

    public RunRecord() {}

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

    public double getAveragePace() { return averagePace; }
    public void setAveragePace(double averagePace) { this.averagePace = averagePace; }

    public String getTrackPoints() { return trackPoints; }
    public void setTrackPoints(String trackPoints) { this.trackPoints = trackPoints; }
}