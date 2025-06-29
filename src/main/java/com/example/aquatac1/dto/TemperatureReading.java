package com.example.aquatac1.dto;

public class TemperatureReading {
    private Double valeur;
    private String unite;
    private String timestamp;
    private String id; // The Firebase push ID

    // Constructors, getters, and setters
    public TemperatureReading() {}

    public TemperatureReading(Double valeur, String unite, String timestamp, String id) {
        this.valeur = valeur;
        this.unite = unite;
        this.timestamp = timestamp;
        this.id = id;
    }

    // Getters and setters
    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}