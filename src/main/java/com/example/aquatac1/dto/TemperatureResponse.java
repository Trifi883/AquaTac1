package com.example.aquatac1.dto;


import java.util.List;

public class TemperatureResponse {
    private List<TemperatureReading> readings;
    private int count;

    // Constructors
    public TemperatureResponse(List<TemperatureReading> readings) {
        this.readings = readings;
        this.count = readings.size();
    }

    // Getters
    public List<TemperatureReading> getReadings() { return readings; }
    public int getCount() { return count; }
}