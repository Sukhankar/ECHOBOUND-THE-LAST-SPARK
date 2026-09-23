package com.echobound.entity.mob;

/**
 * Governs how a creature reacts to the player, independent of its stats. Added alongside a
 * batch of new wildlife (§3/§4 of the world-expansion brief: passive animals, neutral
 * creatures, small pests) so MobEntity can support more than "always hostile" without a
 * parallel entity system — same MobEntity/MobManager, just a per-type behavior switch.
 */
public enum Temperament {
    /** Wanders on its own; flees if the player gets close. Never attacks. Tameable creatures are passive. */
    PASSIVE,
    /** Wanders on its own; ignores the player at a distance and doesn't flee, but won't initiate combat. */
    NEUTRAL,
    /** The existing behavior: detects, chases, and attacks the player within range. */
    HOSTILE
}
