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
        // Updated to point to "locations/location1"
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

                for (DataSnapshot readingSnapshot : dataSnapshot.getChildren()) {
                    Double temp = readingSnapshot.child("temperature").getValue(Double.class);
                    Double ph = readingSnapshot.child("ph").getValue(Double.class);
                    Double turbidity = readingSnapshot.child("turbidity").getValue(Double.class);
                    Double dissolvedOxygen = readingSnapshot.child("dissolved_oxygen").getValue(Double.class);

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
                        result.put("readingId", readingSnapshot.getKey());
                        result.put("location", readingSnapshot.child("location").getValue(String.class));
                        result.put("safeForSwimming",
                                WaterQualitySafetyChecker.isSafeForSwimming(temp, ph, turbidity, dissolvedOxygen));
                        result.put("safeForFishing",
                                WaterQualitySafetyChecker.isSafeForFishing(temp, ph, turbidity, dissolvedOxygen));
                        result.put("waterQualityScore",
                                WaterQualitySafetyChecker.calculateWaterQualityScore(temp, ph, turbidity, dissolvedOxygen));
                        result.put("timestamp", readingSnapshot.child("timestamp").getValue(String.class));

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

                for (DataSnapshot readingSnapshot : dataSnapshot.getChildren()) {
                    WaterQualityReading reading = mapSnapshotToReading(readingSnapshot);
                    reading.calculateSafety();
                    readings.add(reading);
                }

                // Sort by timestamp instead of ID if needed
                readings.sort(Comparator.comparing(WaterQualityReading::getTimestamp).reversed());
                future.complete(readings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<WaterQualityReading> getReadingById(String readingId) {
        CompletableFuture<WaterQualityReading> future = new CompletableFuture<>();

        databaseRef.child(readingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.complete(null);
                    return;
                }

                WaterQualityReading reading = mapSnapshotToReading(dataSnapshot);
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

                for (DataSnapshot readingSnapshot : dataSnapshot.getChildren()) {
                    WaterQualityReading reading = mapSnapshotToReading(readingSnapshot);
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

                readings.sort(Comparator.comparing(WaterQualityReading::getTimestamp).reversed());
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
        reading.setReadingId(snapshot.getKey());
        reading.setLocation(snapshot.child("location").getValue(String.class));
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

    public CompletableFuture<Double> getWaterQualityScoreById(String readingId) {
        CompletableFuture<Double> future = new CompletableFuture<>();

        databaseRef.child(readingId).addListenerForSingleValueEvent(new ValueEventListener() {
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

    // New method to add a reading
    public CompletableFuture<String> addReading(WaterQualityReading reading) {
        CompletableFuture<String> future = new CompletableFuture<>();

        DatabaseReference newRef = databaseRef.push();
        newRef.setValue(reading, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(ref.getKey());
            }
        });

        return future;
    }

    // Updated to search within readings
    public CompletableFuture<List<WaterQualityReading>> getReadingsByLocationName(String locationName) {
        CompletableFuture<List<WaterQualityReading>> future = new CompletableFuture<>();

        databaseRef.orderByChild("location").equalTo(locationName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<WaterQualityReading> readings = new ArrayList<>();

                        if (!dataSnapshot.exists()) {
                            future.complete(readings);
                            return;
                        }

                        for (DataSnapshot readingSnapshot : dataSnapshot.getChildren()) {
                            WaterQualityReading reading = mapSnapshotToReading(readingSnapshot);
                            readings.add(reading);
                        }

                        readings.sort(Comparator.comparing(WaterQualityReading::getTimestamp).reversed());
                        future.complete(readings);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(error.toException());
                    }
                });

        return future;
    }

    /**
     * Gets complete history for all locations
     * @return CompletableFuture with list of all historical readings
     */
    public CompletableFuture<List<WaterQualityReading>> getHistoryForAllLocations() {
        CompletableFuture<List<WaterQualityReading>> future = new CompletableFuture<>();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    List<WaterQualityReading> allReadings = new ArrayList<>();

                    if (!dataSnapshot.exists()) {
                        future.complete(allReadings);
                        return;
                    }

                    // First collect all readings with their natural Firebase order keys
                    Map<String, WaterQualityReading> readingsMap = new LinkedHashMap<>(); // Preserves insertion order

                    for (DataSnapshot locationNode : dataSnapshot.getChildren()) {
                        for (DataSnapshot readingSnapshot : locationNode.getChildren()) {
                            try {
                                WaterQualityReading reading = mapSnapshotToReading(readingSnapshot);
                                reading.calculateSafety();
                                // Use the push ID as key to maintain Firebase's natural order
                                readingsMap.put(readingSnapshot.getKey(), reading);
                            } catch (Exception e) {
                                System.err.println("Error processing reading " + readingSnapshot.getKey() + ": " + e.getMessage());
                            }
                        }
                    }

                    // Convert to list while maintaining insertion order
                    List<WaterQualityReading> sortedReadings = new ArrayList<>(readingsMap.values());

                    // If you want explicit timestamp sorting instead:
                    // sortedReadings.sort(Comparator.comparing(WaterQualityReading::getTimestamp).reversed());

                    future.complete(sortedReadings);

                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    /**
     * Gets history for a specific location
     * @param locationName The name of the location to get history for
     * @return CompletableFuture with list of historical readings for the specified location
     */
    public CompletableFuture<List<WaterQualityReading>> getHistoryForLocation(String locationName) {
        CompletableFuture<List<WaterQualityReading>> future = new CompletableFuture<>();

        // Query all locations but filter by location name
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WaterQualityReading> locationReadings = new ArrayList<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot locationNode : dataSnapshot.getChildren()) {
                        for (DataSnapshot readingSnapshot : locationNode.getChildren()) {
                            String readingLocation = readingSnapshot.child("location").getValue(String.class);
                            if (locationName.equalsIgnoreCase(readingLocation)) {
                                WaterQualityReading reading = mapSnapshotToReading(readingSnapshot);
                                reading.calculateSafety();
                                locationReadings.add(reading);
                            }
                        }
                    }
                }

                // Sort by timestamp descending
                locationReadings.sort(Comparator.comparing(WaterQualityReading::getTimestamp).reversed());
                future.complete(locationReadings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }
}