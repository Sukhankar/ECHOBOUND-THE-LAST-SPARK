package com.echobound.ui.menu;

/**
 * Opening story narration, shown once between the loading screen and the title menu —
 * a sequence of full-screen text cards that fade in, hold, and fade out, matching the
 * world/tone already established in docs/CHARACTER_BIBLE.md (Spark energy vs. Corruption,
 * Rin as the runner who ventures into the silence). Skippable at any time.
 */
public class CinematicIntro {
    private static final String[] LINES = {
        "Long before the silence, the world sang.",
        "Every ember, every river, every voice — carried by the Spark.",
        "Then the Conductor's song broke, and Corruption spread where the Spark once flowed.",
        "One by one, the great resonances fell dark.",
        "Rin was born after the silence began, and never once heard the world sing.",
        "But a single ember survived — small, stubborn, and alive.",
        "Its name is Pip. And it remembers the melody.",
        "ECHOBOUND: THE LAST SPARK"
    };

    private static final float FADE_IN = 0.8f;
    private static final float HOLD = 2.6f;
    private static final float FADE_OUT = 0.6f;
    private static final float LINE_DURATION = FADE_IN + HOLD + FADE_OUT;

    private float elapsed = 0.0f;
    private boolean finished = false;
    private boolean skipped = false;

    public void update(float dt) {
        if (finished) return;
        elapsed += dt;
        if (elapsed >= LINE_DURATION * LINES.length) {
            finished = true;
        }
    }

    /** Jumps straight to the end — pressing any key/click during the cinematic calls this. */
    public void skip() {
        skipped = true;
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean wasSkipped() {
        return skipped;
    }

    public void reset() {
        elapsed = 0.0f;
        finished = false;
        skipped = false;
    }

    public String getCurrentLine() {
        int index = Math.min(LINES.length - 1, (int) (elapsed / LINE_DURATION));
        return LINES[index];
    }

    public boolean isLastLine() {
        return (int) (elapsed / LINE_DURATION) >= LINES.length - 1;
    }

    /** 0.0 (invisible) to 1.0 (fully visible), following the current line's fade in/hold/out. */
    public float getCurrentLineAlpha() {
        float lineTime = elapsed % LINE_DURATION;
        if (lineTime < FADE_IN) {
            return lineTime / FADE_IN;
        } else if (lineTime < FADE_IN + HOLD) {
            return 1.0f;
        } else {
            float fadeOutT = lineTime - FADE_IN - HOLD;
            return Math.max(0.0f, 1.0f - fadeOutT / FADE_OUT);
        }
    }

    public int getTotalLines() {
        return LINES.length;
    }
}
