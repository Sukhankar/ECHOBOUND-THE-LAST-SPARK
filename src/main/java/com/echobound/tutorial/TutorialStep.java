package com.echobound.tutorial;

public enum TutorialStep {
    MOVEMENT(
        "1. Awakening Traversal",
        "Move with [W/A/S/D]. Jump & Double-Jump with [SPACE]. 8-Directional Dash with [SHIFT]."
    ),
    MINING(
        "2. Voxel Harvesting",
        "Target nearby dirt, stone, or trees and hold [Left Click] to mine raw building materials."
    ),
    CRAFTING(
        "3. Foundry & Stations",
        "Press [C] to open the Crafting window. Use [A/D] to select stations and [ENTER] to craft."
    ),
    MAGIC(
        "4. Dual Magic Resonance",
        "Press [Q] for Fire Tornado (Ember+Gale) or [E] for Storm Burst (Tide+Volt) combat bursts."
    ),
    ECHO(
        "5. Echo Clone Replication",
        "Press [X] to record and replay your movement loop to activate dual pressure plates."
    ),
    TAMING(
        "6. Wildlife & Taming",
        "Press [F] near a passive creature (fox, glowbat, cat, turtle, bird) to tame it as a companion."
    );

    public final String title;
    public final String instruction;

    TutorialStep(String title, String instruction) {
        this.title = title;
        this.instruction = instruction;
    }
}
