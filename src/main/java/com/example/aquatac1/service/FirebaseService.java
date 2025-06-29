package com.example.aquatac1.service;

import com.google.firebase.database.*;
import com.example.aquatac1.dto.WaterQualityReading;
import com.example.aquatac1.utility.WaterQualitySafetyChecker;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {
    private final DatabaseReference databaseRef;

    public FirebaseService(FirebaseDatabase firebaseDatabase) {
        this.databaseRef = firebaseDatabase.getReference("locations");
    }

    public CompletableFuture<List<Map<String, Object>>> getSafetyAnalysisResults(
            Double minTemp, Double maxTemp,
            Double minPh, Double maxPh,
            Double maxTurbidity,
            Double minDissolvedOxygen) {

        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> results = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    future.complete(results);
                    return;
                }

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    Double temp = locationSnapshot.child("temperature").getValue(Double.class);
                    Double ph = locationSnapshot.child("ph").getValue(Double.class);
                    Double turbidity = locationSnapshot.child("turbidity").getValue(Double.class);
                    Double dissolvedOxygen = locationSnapshot.child("dissolved_oxygen").getValue(Double.class);

                    // Apply quality filters if provided
                    boolean matchesFilters = true;
                    if (minTemp != null && temp < minTemp) matchesFilters = false;
                    if (maxTemp != null && temp > maxTemp) matchesFilters = false;
                    if (minPh != null && ph < minPh) matchesFilters = false;
                    if (maxPh != null && ph > maxPh) matchesFilters = false;
                    if (maxTurbidity != null && turbidity > maxTurbidity) matchesFilters = false;
                    if (minDissolvedOxygen != null && dissolvedOxygen < minDissolvedOxygen) matchesFilters = false;

                    if (matchesFilters) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("locationId", locationSnapshot.getKey());
                        result.put("locationName", locationSnapshot.child("location").getValue(String.class));
                        result.put("safeForSwimming",
                                WaterQualitySafetyChecker.isSafeForSwimming(temp, ph, turbidity, dissolvedOxygen));
                        result.put("safeForFishing",
                                WaterQualitySafetyChecker.isSafeForFishing(temp, ph, turbidity, dissolvedOxygen));
                        result.put("waterQualityScore",
                                WaterQualitySafetyChecker.calculateWaterQualityScore(temp, ph, turbidity, dissolvedOxygen));

                        results.add(result);
                    }
                }

                future.complete(results);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<List<WaterQualityReading>> getAllLocationReadings() {
        CompletableFuture<List<WaterQualityReading>> future = new CompletableFuture<>();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WaterQualityReading> readings = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    future.complete(readings);
                    return;
                }

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    WaterQualityReading reading = mapSnapshotToReading(locationSnapshot);
                    reading.calculateSafety();
                    readings.add(reading);
                }

                readings.sort(Comparator.comparing(WaterQualityReading::getId));
                future.complete(readings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<WaterQualityReading> getReadingByLocation(String locationId) {
        CompletableFuture<WaterQualityReading> future = new CompletableFuture<>();

        databaseRef.child(locationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.complete(null);
                    return;
                }

                WaterQualityReading reading = new WaterQualityReading();
                reading.setLocationId(locationId);
                reading.setLocationName(dataSnapshot.child("location").getValue(String.class));
                reading.setTemperature(dataSnapshot.child("temperature").getValue(Double.class));
                reading.setPh(dataSnapshot.child("ph").getValue(Double.class));
                reading.setTurbidity(dataSnapshot.child("turbidity").getValue(Double.class));
                reading.setDissolvedOxygen(dataSnapshot.child("dissolved_oxygen").getValue(Double.class));
                reading.setTimestamp(dataSnapshot.child("timestamp").getValue(String.class));
                reading.setId(dataSnapshot.child("id").getValue(Integer.class));

                future.complete(reading);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<List<WaterQualityReading>> getReadingsWithSafetyAnalysis(
            Double minTemp, Double maxTemp,
            Double minPh, Double maxPh,
            Double maxTurbidity,
            Double minDissolvedOxygen) {

        CompletableFuture<List<WaterQualityReading>> future = new CompletableFuture<>();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WaterQualityReading> readings = new ArrayList<>();

                if (!dataSnapshot.exists()) {
                    future.complete(readings);
                    return;
                }

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    WaterQualityReading reading = mapSnapshotToReading(locationSnapshot);
                    reading.calculateSafety();

                    // Apply filters if provided
                    boolean matchesFilters = true;

                    if (minTemp != null && reading.getTemperature() < minTemp) {
                        matchesFilters = false;
                    }
                    if (maxTemp != null && reading.getTemperature() > maxTemp) {
                        matchesFilters = false;
                    }
                    if (minPh != null && reading.getPh() < minPh) {
                        matchesFilters = false;
                    }
                    if (maxPh != null && reading.getPh() > maxPh) {
                        matchesFilters = false;
                    }
                    if (maxTurbidity != null && reading.getTurbidity() > maxTurbidity) {
                        matchesFilters = false;
                    }
                    if (minDissolvedOxygen != null &&
                            reading.getDissolvedOxygen() < minDissolvedOxygen) {
                        matchesFilters = false;
                    }

                    if (matchesFilters) {
                        readings.add(reading);
                    }
                }

                readings.sort(Comparator.comparing(WaterQualityReading::getId));
                future.complete(readings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    private WaterQualityReading mapSnapshotToReading(DataSnapshot snapshot) {
        WaterQualityReading reading = new WaterQualityReading();
        reading.setLocationId(snapshot.getKey());
        reading.setLocationName(snapshot.child("location").getValue(String.class));
        reading.setTemperature(snapshot.child("temperature").getValue(Double.class));
        reading.setPh(snapshot.child("ph").getValue(Double.class));
        reading.setTurbidity(snapshot.child("turbidity").getValue(Double.class));
        reading.setDissolvedOxygen(snapshot.child("dissolved_oxygen").getValue(Double.class));
        reading.setTimestamp(snapshot.child("timestamp").getValue(String.class));
        reading.setId(snapshot.child("id").getValue(Integer.class));
        reading.setWaterQualityScore(
                WaterQualitySafetyChecker.calculateWaterQualityScore(
                        reading.getTemperature(),
                        reading.getPh(),
                        reading.getTurbidity(),
                        reading.getDissolvedOxygen()
                )
        );
        return reading;
    }

    public CompletableFuture<Double> getWaterQualityScoreByLocationId(String locationId) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        databaseRef.child(locationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.complete(null);
                    return;
                }

                Double temp = dataSnapshot.child("temperature").getValue(Double.class);
                Double ph = dataSnapshot.child("ph").getValue(Double.class);
                Double turbidity = dataSnapshot.child("turbidity").getValue(Double.class);
                Double dissolvedOxygen = dataSnapshot.child("dissolved_oxygen").getValue(Double.class);

                double score = WaterQualitySafetyChecker.calculateWaterQualityScore(
                        temp, ph, turbidity, dissolvedOxygen);

                future.complete(score);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }
    public CompletableFuture<String> getLocationIdByName(String locationName) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Query for exact match (case-sensitive)
        databaseRef.orderByChild("location").equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            future.complete(null);
                            return;
                        }

                        // Get the first matching location's ID
                        DataSnapshot firstMatch = dataSnapshot.getChildren().iterator().next();
                        future.complete(firstMatch.getKey());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });

        return future;
    }
}