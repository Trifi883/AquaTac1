package com.example.aquatac1.config;

import com.example.aquatac1.dto.WaterQualityReading;
import com.example.aquatac1.service.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/water-quality")
public class WaterQualityController {
    private final FirebaseService firebaseService;

    public WaterQualityController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @GetMapping("/readings")
    public ResponseEntity<List<WaterQualityReading>> getAllReadings() {
        try {
            List<WaterQualityReading> readings = firebaseService.getAllLocationReadings().get();
            return ResponseEntity.ok(readings);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/readings/{readingId}")
    public ResponseEntity<WaterQualityReading> getReadingById(
            @PathVariable String readingId) {
        try {
            WaterQualityReading reading = firebaseService.getReadingById(readingId).get();
            if (reading == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(reading);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/safety-analysis")
    public ResponseEntity<List<Map<String, Object>>> getSafetyAnalysis(
            @RequestParam(required = false) Boolean safeForSwimming,
            @RequestParam(required = false) Boolean safeForFishing,
            @RequestParam(required = false) Double minTemp,
            @RequestParam(required = false) Double maxTemp,
            @RequestParam(required = false) Double minPh,
            @RequestParam(required = false) Double maxPh,
            @RequestParam(required = false) Double maxTurbidity,
            @RequestParam(required = false) Double minDissolvedOxygen,
            @RequestParam(required = false) Double minWaterQualityScore) {

        try {
            List<Map<String, Object>> results = firebaseService
                    .getSafetyAnalysisResults(minTemp, maxTemp, minPh, maxPh,
                            maxTurbidity, minDissolvedOxygen)
                    .get();

            // Apply additional safety filters if requested
            if (safeForSwimming != null || safeForFishing != null || minWaterQualityScore != null) {
                results = results.stream()
                        .filter(r -> {
                            boolean matches = true;
                            if (safeForSwimming != null) {
                                matches = matches && (boolean)r.get("safeForSwimming") == safeForSwimming;
                            }
                            if (safeForFishing != null) {
                                matches = matches && (boolean)r.get("safeForFishing") == safeForFishing;
                            }
                            if (minWaterQualityScore != null) {
                                double score = (double)r.get("waterQualityScore");
                                matches = matches && score >= minWaterQualityScore;
                            }
                            return matches;
                        })
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(results);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/readings/{readingId}/safety")
    public ResponseEntity<Map<String, Boolean>> getReadingSafety(
            @PathVariable String readingId) {
        try {
            WaterQualityReading reading = firebaseService
                    .getReadingById(readingId)
                    .get();

            if (reading == null) {
                return ResponseEntity.notFound().build();
            }

            reading.calculateSafety();

            Map<String, Boolean> safetyInfo = new HashMap<>();
            safetyInfo.put("safeForSwimming", reading.isSafeForSwimming());
            safetyInfo.put("safeForFishing", reading.isSafeForFishing());

            return ResponseEntity.ok(safetyInfo);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/top-readings")
    public ResponseEntity<List<Map<String, Object>>> getTopReadingsByWaterQuality(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> results = firebaseService
                    .getSafetyAnalysisResults(null, null, null, null, null, null)
                    .get();

            // Sort by water quality score descending
            results.sort((a, b) -> Double.compare(
                    (double)b.get("waterQualityScore"),
                    (double)a.get("waterQualityScore")
            ));

            // Limit results
            if (results.size() > limit) {
                results = results.subList(0, limit);
            }

            return ResponseEntity.ok(results);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/readings/{readingId}/water-quality-score")
    public ResponseEntity<Map<String, Object>> getWaterQualityScore(
            @PathVariable String readingId) {
        try {
            Double score = firebaseService.getWaterQualityScoreById(readingId).get();

            if (score == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("readingId", readingId);
            response.put("waterQualityScore", score);
            response.put("qualityLevel", interpretWaterQualityScore(score));

            return ResponseEntity.ok(response);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/readings/location/{locationName}")
    public ResponseEntity<List<WaterQualityReading>> getReadingsByLocationName(
            @PathVariable String locationName) {
        try {
            List<WaterQualityReading> readings = firebaseService.getReadingsByLocationName(locationName).get();
            return ResponseEntity.ok(readings);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/readings")
    public ResponseEntity<Map<String, Object>> addReading(@RequestBody WaterQualityReading reading) {
        try {
            String readingId = firebaseService.addReading(reading).get();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("readingId", readingId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "status", "error",
                            "message", "Failed to add reading"
                    ));
        }
    }

    private String interpretWaterQualityScore(double score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 50) return "Fair";
        if (score >= 25) return "Poor";
        return "Very Poor";
    }

    // Add these new endpoints to your controller

    @GetMapping("/history/all")
    public ResponseEntity<List<WaterQualityReading>> getAllLocationsHistory() {
        try {
            List<WaterQualityReading> history = firebaseService.getHistoryForAllLocations().get();
            return ResponseEntity.ok(history);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history/{locationName}")
    public ResponseEntity<List<WaterQualityReading>> getLocationHistory(
            @PathVariable String locationName) {
        try {
            List<WaterQualityReading> history = firebaseService.getHistoryForLocation(locationName).get();
            if (history.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(history);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}