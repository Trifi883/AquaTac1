package com.example.aquatac1.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WaterQualityReading {
    private String readingId;  // Changed from locationId to readingId (the push ID)
    private String location;   // Changed from locationName to location (matches DB field)
    private Double temperature;
    private Double ph;
    private Double turbidity;
    private Double dissolvedOxygen;
    private String timestamp;
    private Integer id;        // Optional: Keep if you still need this numeric ID
    private boolean safeForSwimming;
    private boolean safeForFishing;
    private double waterQualityScore;

    // Constructors
    public WaterQualityReading() {}

    // Getters and Setters
    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getPh() {
        return ph;
    }

    public void setPh(Double ph) {
        this.ph = ph;
    }

    public Double getTurbidity() {
        return turbidity;
    }

    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }

    public Double getDissolvedOxygen() {
        return dissolvedOxygen;
    }

    public void setDissolvedOxygen(Double dissolvedOxygen) {
        this.dissolvedOxygen = dissolvedOxygen;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Helper method to get timestamp as LocalDateTime
    public LocalDateTime getTimestampAsDateTime() {
        if (timestamp == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timestamp, formatter);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isSafeForSwimming() {
        return safeForSwimming;
    }

    public void setSafeForSwimming(boolean safeForSwimming) {
        this.safeForSwimming = safeForSwimming;
    }

    public boolean isSafeForFishing() {
        return safeForFishing;
    }

    public void setSafeForFishing(boolean safeForFishing) {
        this.safeForFishing = safeForFishing;
    }

    public double getWaterQualityScore() {
        return waterQualityScore;
    }

    public void setWaterQualityScore(double waterQualityScore) {
        this.waterQualityScore = waterQualityScore;
    }

    // Method to calculate safety
    public void calculateSafety() {
        // Swimming safety criteria
        this.safeForSwimming = (temperature != null && temperature >= 18 && temperature <= 30) &&
                (ph != null && ph >= 6.5 && ph <= 8.5) &&
                (turbidity != null && turbidity <= 5) &&
                (dissolvedOxygen != null && dissolvedOxygen >= 5);

        // Fishing safety criteria
        this.safeForFishing = (temperature != null && temperature >= 15 && temperature <= 25) &&
                (ph != null && ph >= 6 && ph <= 9) &&
                (turbidity != null && turbidity <= 10) &&
                (dissolvedOxygen != null && dissolvedOxygen >= 4);
    }

    @Override
    public String toString() {
        return "WaterQualityReading{" +
                "readingId='" + readingId + '\'' +
                ", location='" + location + '\'' +
                ", temperature=" + temperature +
                ", ph=" + ph +
                ", turbidity=" + turbidity +
                ", dissolvedOxygen=" + dissolvedOxygen +
                ", timestamp='" + timestamp + '\'' +
                ", id=" + id +
                ", safeForSwimming=" + safeForSwimming +
                ", safeForFishing=" + safeForFishing +
                ", waterQualityScore=" + waterQualityScore +
                '}';
    }
}