package com.echobound.arcade;

public class ArcadeCabinet {
    public final String cabinetName;
    private int highScore = 0;
    private int totalPlays = 0;
    private int lifetimeTokensEarned = 0;

    public ArcadeCabinet(String cabinetName) {
        this.cabinetName = cabinetName;
    }

    public int submitGameScore(int score) {
        totalPlays++;
        if (score > highScore) {
            highScore = score;
        }
        // 1 arcade token per 100 points
        int tokens = score / 100;
        lifetimeTokensEarned += tokens;
        return tokens;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getTotalPlays() {
        return totalPlays;
    }

    public int getLifetimeTokensEarned() {
        return lifetimeTokensEarned;
    }
}
