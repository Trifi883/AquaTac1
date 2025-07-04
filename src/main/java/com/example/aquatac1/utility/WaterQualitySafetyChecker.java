package com.example.aquatac1.utility;

public class WaterQualitySafetyChecker {
    public static boolean isSafeForSwimming(Double temp, Double ph, Double turbidity, Double dissolvedOxygen) {
        return (temp != null && temp >= 18 && temp <= 30) &&
                (ph != null && ph >= 6.5 && ph <= 8.5) &&
                (turbidity != null && turbidity <= 5) &&
                (dissolvedOxygen != null && dissolvedOxygen >= 5);
    }

    public static boolean isSafeForFishing(Double temp, Double ph, Double turbidity, Double dissolvedOxygen) {
        return (temp != null && temp >= 15 && temp <= 25) &&
                (ph != null && ph >= 6 && ph <= 9) &&
                (turbidity != null && turbidity <= 10) &&
                (dissolvedOxygen != null && dissolvedOxygen >= 4);
    }


    public static double calculateWaterQualityScore(Double temp, Double ph, Double turbidity, Double dissolvedOxygen) {
        // Normalize each parameter to a 0-100 scale based on ideal ranges
        double tempScore = calculateTemperatureScore(temp);
        double phScore = calculatePhScore(ph);
        double turbidityScore = calculateTurbidityScore(turbidity);
        double oxygenScore = calculateOxygenScore(dissolvedOxygen);

        // Weighted average - you can adjust these weights as needed
        return (tempScore * 0.25) + (phScore * 0.25) +
                (turbidityScore * 0.25) + (oxygenScore * 0.25);
    }

    private static double calculateTemperatureScore(Double temp) {
        // Ideal range 18-22°C (adjust based on your requirements)
        if (temp == null) return 0;
        if (temp >= 18 && temp <= 22) return 100;
        if (temp < 10 || temp > 30) return 0;
        // Linear interpolation for values outside ideal range but within limits
        if (temp < 18) return 100 * (temp - 10) / (18 - 10);
        return 100 * (30 - temp) / (30 - 22);
    }

    private static double calculatePhScore(Double ph) {
        // Ideal range 6.5-8.5
        if (ph == null) return 0;
        if (ph >= 6.5 && ph <= 8.5) return 100;
        if (ph < 4 || ph > 10) return 0;
        if (ph < 6.5) return 100 * (ph - 4) / (6.5 - 4);
        return 100 * (10 - ph) / (10 - 8.5);
    }

    private static double calculateTurbidityScore(Double turbidity) {
        // Lower is better, ideal < 5 NTU
        if (turbidity == null) return 0;
        if (turbidity <= 5) return 100;
        if (turbidity >= 50) return 0;
        return 100 * (50 - turbidity) / (50 - 5);
    }

    private static double calculateOxygenScore(Double oxygen) {
        // Higher is better, ideal > 6 mg/L
        if (oxygen == null) return 0;
        if (oxygen >= 8) return 100;
        if (oxygen <= 2) return 0;
        return 100 * (oxygen - 2) / (8 - 2);
    }
}