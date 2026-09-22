package com.echobound.farming;

import java.util.HashMap;
import java.util.Map;

public class FarmingManager {
    public static class CropPlot {
        public final int x;
        public final int y;
        public final int z;
        public final CropType cropType;
        public int currentStage = 0;
        public float stageTimer = 0.0f;
        public boolean isWatered = false;
        public boolean isMutated = false;
        public String mutatedName = null;

        public CropPlot(int x, int y, int z, CropType cropType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.cropType = cropType;
        }
    }

    private final Map<Long, CropPlot> plots = new HashMap<>();

    private static long getPlotKey(int x, int y, int z) {
        return (((long) x) << 40) | (((long) (y & 0xFFFFF)) << 20) | (z & 0xFFFFFL);
    }

    public boolean plantCrop(int x, int y, int z, CropType crop) {
        long key = getPlotKey(x, y, z);
        if (plots.containsKey(key)) return false;
        plots.put(key, new CropPlot(x, y, z, crop));
        return true;
    }

    public CropPlot getPlot(int x, int y, int z) {
        return plots.get(getPlotKey(x, y, z));
    }

    public void waterPlot(int x, int y, int z) {
        CropPlot plot = getPlot(x, y, z);
        if (plot != null) {
            plot.isWatered = true;
        }
    }

    public void update(float dt, boolean isNearHeatSource, boolean isNearColdSource) {
        for (CropPlot plot : plots.values()) {
            if (plot.currentStage < plot.cropType.maxStages) {
                // Growth is twice as fast when watered
                float rate = plot.isWatered ? 2.0f : 1.0f;
                plot.stageTimer += dt * rate;

                // Check environmental climate mutation
                if (plot.cropType == CropType.EMBER_SEED && isNearHeatSource && !plot.isMutated) {
                    plot.isMutated = true;
                    plot.mutatedName = "Flameflower";
                } else if (plot.cropType == CropType.TIDE_SEED && isNearColdSource && !plot.isMutated) {
                    plot.isMutated = true;
                    plot.mutatedName = "Frost Orchid";
                }

                if (plot.stageTimer >= plot.cropType.growthTimePerStage) {
                    plot.stageTimer = 0.0f;
                    plot.currentStage++;
                    plot.isWatered = false; // Needs re-watering for next stage
                }
            }
        }
    }

    public String harvest(int x, int y, int z) {
        long key = getPlotKey(x, y, z);
        CropPlot plot = plots.get(key);
        if (plot != null && plot.currentStage >= plot.cropType.maxStages) {
            plots.remove(key);
            return plot.isMutated ? plot.mutatedName : plot.cropType.harvestProduct;
        }
        return null;
    }

    public int getActivePlotCount() {
        return plots.size();
    }
}
