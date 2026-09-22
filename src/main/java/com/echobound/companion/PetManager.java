package com.echobound.companion;

import java.util.EnumSet;
import java.util.Set;

public class PetManager {
    private final Set<PetType> tamedPets = EnumSet.noneOf(PetType.class);
    private PetType activePet = null;

    public void tamePet(PetType pet) {
        if (pet != null) {
            tamedPets.add(pet);
            if (activePet == null) {
                activePet = pet;
            }
        }
    }

    public boolean isTamed(PetType pet) {
        return tamedPets.contains(pet);
    }

    public void setActivePet(PetType pet) {
        if (tamedPets.contains(pet)) {
            this.activePet = pet;
        }
    }

    public PetType getActivePet() {
        return activePet;
    }

    public boolean hasPerk(PetType pet) {
        return activePet == pet;
    }

    public float getLightningDamageMultiplier() {
        return (activePet == PetType.SPARK_CAT) ? 1.35f : 1.0f;
    }

    public float getArmorDefenseMultiplier() {
        return (activePet == PetType.MOSS_TURTLE) ? 1.25f : 1.0f;
    }

    public boolean emitsLight() {
        return activePet == PetType.GLOWBAT;
    }

    public boolean detectsSecrets() {
        return activePet == PetType.CLOUD_BIRD;
    }

    public boolean detectsTreasure() {
        return activePet == PetType.FOX;
    }

    public int getTamedCount() {
        return tamedPets.size();
    }
}
