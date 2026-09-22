package com.echobound.puzzle;

import com.echobound.physics3d.Vec3;

public class PuzzleMechanism {
    public final int id;
    public final PuzzleType type;
    public final Vec3 plateAPos;
    public final Vec3 plateBPos;
    public boolean plateAOccupied = false;
    public boolean plateBOccupied = false;
    public boolean isSolved = false;

    public PuzzleMechanism(int id, PuzzleType type, Vec3 plateAPos, Vec3 plateBPos) {
        this.id = id;
        this.type = type;
        this.plateAPos = plateAPos;
        this.plateBPos = plateBPos;
    }

    public void updateState(Vec3 playerPos, Vec3 echoClonePos) {
        if (isSolved) return;

        if (type == PuzzleType.DUAL_PRESSURE_PLATES) {
            boolean playerOnA = playerPos != null && playerPos.distanceTo(plateAPos) <= 1.5f;
            boolean playerOnB = playerPos != null && playerPos.distanceTo(plateBPos) <= 1.5f;

            boolean echoOnA = echoClonePos != null && echoClonePos.distanceTo(plateAPos) <= 1.5f;
            boolean echoOnB = echoClonePos != null && echoClonePos.distanceTo(plateBPos) <= 1.5f;

            plateAOccupied = playerOnA || echoOnA;
            plateBOccupied = playerOnB || echoOnB;

            // Both plates must be depressed simultaneously
            if (plateAOccupied && plateBOccupied) {
                isSolved = true;
            }
        }
    }

    public void triggerElementalHit(Vec3 hitPos) {
        if (isSolved) return;
        if (type == PuzzleType.ELEMENTAL_BRAZIER && plateAPos.distanceTo(hitPos) <= 1.5f) {
            isSolved = true;
        }
    }
}
