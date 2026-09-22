package com.echobound.boss;

public class WorldBoss {
    public final WorldBossType type;
    public int currentHealth;
    public int currentPhase = 1;
    public boolean isShielded = false;
    public float telegraphTimer = 0.0f;
    public boolean isTelegraphing = false;
    public String currentAttackTelegraph = null;
    public boolean isDefeated = false;

    public WorldBoss(WorldBossType type) {
        this.type = type;
        this.currentHealth = type.maxHealth;
    }

    public void takeDamage(int amount) {
        if (isDefeated) return;
        if (isShielded) {
            amount = (int) (amount * 0.25f); // 75% damage reduction when shielded
        }

        currentHealth = Math.max(0, currentHealth - amount);

        // Check phase transitions
        float healthPct = (float) currentHealth / type.maxHealth;
        if (type.phaseCount >= 2 && healthPct <= 0.50f && currentPhase < 2) {
            transitionToPhase(2);
        }
        if (type.phaseCount >= 3 && healthPct <= 0.20f && currentPhase < 3) {
            transitionToPhase(3);
        }
        if (type.phaseCount >= 4 && healthPct <= 0.05f && currentPhase < 4) {
            transitionToPhase(4);
        }

        if (currentHealth <= 0) {
            isDefeated = true;
            isTelegraphing = false;
            currentAttackTelegraph = null;
        }
    }

    private void transitionToPhase(int newPhase) {
        this.currentPhase = newPhase;
        this.isShielded = true; // Gain temporary shield during transition
    }

    public void startAttackTelegraph(String attackName, float duration) {
        if (isDefeated) return;
        this.isTelegraphing = true;
        this.currentAttackTelegraph = attackName;
        this.telegraphTimer = duration;
    }

    public void update(float dt) {
        if (isDefeated) return;

        if (isTelegraphing) {
            telegraphTimer -= dt;
            if (telegraphTimer <= 0) {
                isTelegraphing = false;
                currentAttackTelegraph = null;
            }
        }
    }

    public void breakShield() {
        this.isShielded = false;
    }
}
