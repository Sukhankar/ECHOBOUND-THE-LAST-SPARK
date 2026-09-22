package com.echobound.puzzle;

import com.echobound.physics3d.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PuzzleManager {
    private final List<PuzzleMechanism> puzzles = new ArrayList<>();
    private int nextId = 1;

    public PuzzleMechanism createPressurePlatePuzzle(Vec3 plateA, Vec3 plateB) {
        PuzzleMechanism p = new PuzzleMechanism(nextId++, PuzzleType.DUAL_PRESSURE_PLATES, plateA, plateB);
        puzzles.add(p);
        return p;
    }

    public PuzzleMechanism createBrazierPuzzle(Vec3 brazierPos) {
        PuzzleMechanism p = new PuzzleMechanism(nextId++, PuzzleType.ELEMENTAL_BRAZIER, brazierPos, null);
        puzzles.add(p);
        return p;
    }

    public void update(Vec3 playerPos, Vec3 echoPos) {
        for (PuzzleMechanism p : puzzles) {
            p.updateState(playerPos, echoPos);
        }
    }

    public void onElementalHit(Vec3 hitPos) {
        for (PuzzleMechanism p : puzzles) {
            p.triggerElementalHit(hitPos);
        }
    }

    public int getSolvedCount() {
        int count = 0;
        for (PuzzleMechanism p : puzzles) {
            if (p.isSolved) count++;
        }
        return count;
    }

    public int getTotalPuzzleCount() {
        return puzzles.size();
    }
}
