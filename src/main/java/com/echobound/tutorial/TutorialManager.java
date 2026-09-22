package com.echobound.tutorial;

import java.util.EnumSet;
import java.util.Set;

public class TutorialManager {
    private final Set<TutorialStep> completedSteps = EnumSet.noneOf(TutorialStep.class);
    private int currentStepIndex = 0;
    private final TutorialStep[] steps = TutorialStep.values();

    public TutorialStep getCurrentStep() {
        if (currentStepIndex >= 0 && currentStepIndex < steps.length) {
            return steps[currentStepIndex];
        }
        return null;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void completeStep(TutorialStep step) {
        if (step != null) {
            completedSteps.add(step);
            if (getCurrentStep() == step) {
                advance();
            }
        }
    }

    public boolean isStepCompleted(TutorialStep step) {
        return completedSteps.contains(step);
    }

    public void advance() {
        if (currentStepIndex < steps.length) {
            currentStepIndex++;
        }
    }

    public boolean isAllCompleted() {
        return completedSteps.size() >= steps.length;
    }

    public int getTotalSteps() {
        return steps.length;
    }

    public int getCompletedCount() {
        return completedSteps.size();
    }

    public void reset() {
        completedSteps.clear();
        currentStepIndex = 0;
    }
}
