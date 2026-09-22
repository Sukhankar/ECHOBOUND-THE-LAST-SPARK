package com.echobound.building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureManager {
    private final Map<Integer, PlacedStructure> structures = new HashMap<>();
    private int nextStructureId = 1;

    public PlacedStructure placeStructure(BuildingStructureType type, float x, float y, float z) {
        PlacedStructure structure = new PlacedStructure(nextStructureId++, type, x, y, z);
        structures.put(structure.id, structure);
        return structure;
    }

    public boolean removeStructure(int id) {
        return structures.remove(id) != null;
    }

    public int getStructureCount(BuildingStructureType type) {
        int count = 0;
        for (PlacedStructure s : structures.values()) {
            if (s.active && s.type == type) {
                count++;
            }
        }
        return count;
    }

    public boolean isSafeFromCorruption(float x, float y, float z) {
        for (PlacedStructure s : structures.values()) {
            if (s.active && s.type == BuildingStructureType.SPARK_BEACON) {
                if (s.isInRange(x, y, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<PlacedStructure> getAll() {
        return new ArrayList<>(structures.values());
    }

    public int getTotalStructures() {
        return structures.size();
    }
}
