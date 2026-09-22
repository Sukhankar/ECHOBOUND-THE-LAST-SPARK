package com.echobound.faction;

public enum ReputationStanding {
    HOSTILE(-100, -50),
    SUSPICIOUS(-49, -1),
    NEUTRAL(0, 24),
    FRIENDLY(25, 59),
    HONORED(60, 89),
    REVERED(90, 100);

    public final int minScore;
    public final int maxScore;

    ReputationStanding(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static ReputationStanding fromScore(int score) {
        if (score <= -50) return HOSTILE;
        if (score < 0) return SUSPICIOUS;
        if (score < 25) return NEUTRAL;
        if (score < 60) return FRIENDLY;
        if (score < 90) return HONORED;
        return REVERED;
    }
}
