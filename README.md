# ECHOBOUND: THE LAST SPARK

> **"The world lost its Echo. We are the ones who bring it back."**

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=for-the-badge)]()
[![Genre](https://img.shields.io/badge/Genre-Open--World%20Pixel%20Sandbox%20RPG-purple.svg?style=for-the-badge)]()

---

## Overview

**ECHOBOUND: THE LAST SPARK** is an open-world pixelated arcade adventure + sandbox + action RPG built from the ground up in standard Java without external runtime dependencies. 

The world was powered by the First Spark, an energy born from light, movement, and sound. One night every Resonance Tower went silent. Machines went rogue, and the world lost its Echo. **Rin**, a young Spark Runner, along with **Pip**, a tiny floating spark creature, embark on a perilous journey across a living, voxelized open world to recover the Echo Cores before **The Conductor** locks the world into static entropy forever.

---

## Key Features

### 1. 3D Voxel Sandbox Traversal & Building
- **Full 3D Movement**: Move freely across $X, Y, Z$ coordinates with responsive ground acceleration, variable jump heights, wall sliding, wall jumping, scarf gliding, and 8-directional dashing.
- **Voxel World Generation**: Procedural heightmap generation, continuous chunk streaming, raycast mining, and collision-checked adjacent block placement.
- **Echo Sandbox Clone**: Record player traversal and mining actions for 6 seconds and play them back in real time for automated multi-point harvesting and combat teamwork.
- **Day/Night & Weather Engine**: 24-hour day/night cycle featuring ambient illumination shifts and 5 dynamic weather states (Clear, Rain, Storm, Snow, Eclipse).

### 2. Magic Schools & Dual Combinations
- **8 Elemental Schools**: Ember, Tide, Gale, Terra, Volt, Bloom, Void, Echo.
- **Signature Dual Combinations**:
  - *Fire Tornado* (Ember + Gale)
  - *Storm Burst* (Tide + Volt)
  - *Thorn Fortress* (Terra + Bloom)
  - *Phantom Copy* (Void + Echo)
  - *Magma Hammer* (Ember + Terra)
  - *Blizzard Dash* (Tide + Gale)

### 3. Weapons, Runecrafting & Equipment
- **14 Item Categories & Integer-ID Registry**: Weapons, Tools, Armor, Magic, Food, Potions, Materials, Quest Items, Relics, Treasure, Building, Keys, Maps, Companion Items.
- **3-Socket Weapon Runecrafting**: Socket Sharp, Flame, Frost, Volt, Echo, and Void runes with dynamic compound name generation (e.g. *"Sharp Flame Echo Iron Sword"*).
- **6 Equipment Slots**: Head, Body, Gloves, Boots, Charm, Core with passive modifiers (e.g., +30% dash speed, +50% mining speed, night vision).
- **5 Backpack Expansions**: Starter Bag (16 slots) $\to$ Explorer Pack (24) $\to$ Adventurer Pack (32) $\to$ Sky Pack (40) $\to$ Echo Vault (48).

### 4. Life-Sim Sandbox Systems
- **8 Crafting Stations**: Campfire, Workbench, Forge, Loom, Alchemy Table, Cooking Hearth, Rune Table, Ancient Forge.
- **Cooking & Buffs**: 6 Gourmet dishes with timed stat boosts (Speed, Jump height, Spark energy regeneration, and elemental immunity).
- **Alchemy**: 6 Potions including subterranean treasure scent and stealth phials.
- **Farming & Climate Mutation**: 5 Crops with soil hydration stages and environmental mutations (Ember Seed near heat $\to$ *Flameflower*; Tide Seed in cold $\to$ *Frost Orchid*).
- **Companions & Mounts**: 5 Tameable pets with unique perks (+35% Volt damage, +25% armor, illumination, secret detection) and 4 mounts with specialized speeds and terrains.

### 5. Factions, Living NPCs & 500-Subchapter Storyline
- **7 World Factions**: Spark Keepers, Clockwork Guild, Deep Seekers, Sky Nomads, Tide Shamans, Thorn Wardens, and Void Outcasts with reputation standings from Hostile (-100) to Revered (+100).
- **Living NPCs**: Time-of-day schedules and dynamic weather sheltering (e.g., blacksmith retreating into indoor workshop during storms).
- **4-Tier Quest Architecture**: Visible (Main story), Discoverable (NPC town notices), Hidden (Ruins/caves), and Secret Chains (Cryptic multi-stage lore).
- **20-Chapter Progression Engine**: 20 Major Chapters containing 500 individual narrative subchapters tracking complete world restoration.

### 6. Endgame & Endless Sandbox
- **World Bosses**: Multi-phase encounters with protective shields, attack telegraphs, and distinct arenas.
- **Nighttime Corruption Swarms**: Shadow entity raids scaling with world days, repelled by player-constructed Spark Beacons.
- **Player Settlements**: Build safe zones, automated defense pylons, and persistent vaults.
- **Museum System**: 4 Exhibit wings (Relics, Fossils, Minerals, Flora) with 40 collectible specimens and cumulative global passives.
- **Arcade Cabinets & Endless Rifts**: Play retro arcade mini-games for tokens and unlock Endless Rift Portals and Creative Sandbox Mode.

---

## Technical Architecture & Performance Rules

- **Zero External Dependencies**: Pure Java 17+ utilizing standard Java2D, Swing, and deterministic math.
- **Pixel-First Scaling**: Internal render buffers (`PIXEL_SAVER` 320×180, `PIXEL_STANDARD` 426×240, `PIXEL_PLUS` 640×360) scaled cleanly to screen resolution via nearest-neighbor interpolation.
- **Flat Memory Footprint**: Compact chunks stored as flat `byte[2048]` arrays ($16 \times 16 \times 8$) with sparse delta serialization.
- **Hot-Path Object Pooling**: Pre-allocated projectile and particle pools eliminating runtime garbage collection pauses.

---

## Project Structure

```
src/main/java/com/echobound/
├── alchemy/       # Potion types, brewing and timed effect manager
├── arcade/        # Retro arcade cabinet mini-games and post-game endless engine
├── boss/          # World bosses, multi-phase logic, and nighttime corruption swarms
├── building/      # Player structures, safe zones, and defense pylons
├── combat/        # 3-Socket rune weapon modification and combat math
├── companion/     # Pets with passive perks and mount speed overrides
├── cooking/       # Gourmet dishes and timed nutritional buffs
├── core/          # Window management, input handling, and resolution profiles
├── crafting/      # 8 Crafting stations, recipes, and material deduction
├── entity/        # Player and entity states
├── faction/       # 7 Factions, reputation rankings, and merchant discounts
├── farming/       # Crop growth, soil hydration, and climate mutations
├── fx/            # Camera shake, particles, and visual effects
├── items/         # 14 Item categories, equipment slots, and backpack tiers
├── magic/         # 8 Magic schools, dual spell combinations, and ancient relics
├── museum/        # 4 Museum wings, 40 donations, and passive milestone perks
├── npc/           # Living NPC definitions, routines, and weather sheltering
├── physics/       # 2D platformer collision and jump feel
├── physics3d/     # 3D spatial math and AABB3D volume collision
├── pool/          # Zero-allocation object pools for hot paths
├── quest/         # 4-Tier quest management and progression
├── sandbox/       # Voxel world chunks, renderer, day/night cycles, and Echo clone
├── story/         # 20 Major chapters and 500 subchapters progression engine
├── test/          # 6 Complete automated verification suites
└── world/         # Level data and tile mappings
```

---

## Getting Started

### Prerequisites
- **JDK 17** or higher (OpenJDK 17/21 recommended)
- **Git**

### Build and Verify

Compile all source files and run the full verification test suite:

```bash
# 1. Compile
javac -d bin $(find src/main/java -name "*.java")

# 2. Run all verification suites
java -cp bin com.echobound.test.PhysicsVerification
java -cp bin com.echobound.test.SandboxVerification
java -cp bin com.echobound.test.Part2Verification
java -cp bin com.echobound.test.Part3Verification
java -cp bin com.echobound.test.Part4Verification
java -cp bin com.echobound.test.Part5Verification
```

### Run the Game

```bash
# Launch the 3D Voxel Sandbox Engine
java -cp bin com.echobound.sandbox.SandboxMain
```

---

## Controls

| Key | Action |
| :--- | :--- |
| **W / S** | Move Forward / Backward ($Y$-axis) |
| **A / D** | Move Left / Right ($X$-axis) |
| **Space** | Jump / Double Jump / Glide (Hold) |
| **Shift** | 8-Directional Dash |
| **Left Click** | Mine targeted Voxel Block |
| **Right Click** | Place Quickslot Voxel Block |
| **E** | Record / Replay Echo Clone Loop |
| **T** | Cycle Weather (Clear / Rain / Storm / Snow / Eclipse) |
| **1 - 5** | Select Quickslot Block Type |
