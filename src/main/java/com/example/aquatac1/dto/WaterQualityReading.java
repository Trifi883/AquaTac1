package com.example.aquatac1.dto;

public class WaterQualityReading {
    private String locationId;
    private String locationName;
    private Double temperature;
    private Double ph;
    private Double turbidity;
    private Double dissolvedOxygen;
    private String timestamp;
    private Integer id;
    private boolean safeForSwimming;
    private boolean safeForFishing;

    // Constructors
    public WaterQualityReading() {}
    private double waterQualityScore;

    // Getters and Setters
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Double getTurbidity() { return turbidity; }
    public void setTurbidity(Double turbidity) { this.turbidity = turbidity; }

    public Double getDissolvedOxygen() { return dissolvedOxygen; }
    public void setDissolvedOxygen(Double dissolvedOxygen) { this.dissolvedOxygen = dissolvedOxygen; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public boolean isSafeForSwimming() { return safeForSwimming; }
    public void setSafeForSwimming(boolean safeForSwimming) {
        this.safeForSwimming = safeForSwimming;
    }

    public boolean isSafeForFishing() { return safeForFishing; }
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
}