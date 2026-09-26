# 🎮 Gameplay, Traversal & Echo Clone: Gear 2nd Agility & Mirage Automation

> *"Movement in this game isn't just about getting from point A to point B. It's an art form. If you aren't wall-jumping into a scarf glide while your Echo Clone mines a titanium vein behind you, what are you even doing?"* — Rin, Master Spark Runner

---

## 🏃 1. High-Octane Traversal Mechanics (Gear 2nd Mode)

Movement in EchoBound was designed following modern platformer physics (inspired by *Celeste*, *Dead Cells*, and *Hollow Knight*). Rin has zero input delay, high-responsiveness, and forgiving mechanical tolerances:

```
[ GROUND SPRINT ] ---> [ WALL SLIDE / KICK ] ---> [ SCARF GLIDE ] ---> [ 8-WAY DASH ]
(High Acceleration)     (Climb Vertical Cliffs)   (Cross Abyss Chasms)  (I-Frame Burst)
```

### Traversal Capabilities:

1. **Ground Sprint & Acceleration**:
   - Holding `Shift` activates Spark Sprint ($260\text{ px/s}$).
   - Includes subtle coyote time (5 frames after walking off an edge where you can still jump) and jump buffering (pressing jump 4 frames before landing still registers upon touchdown).

2. **Wall Cling & Wall Jumping**:
   - Sliding down vertical rock faces reduces descent speed by 75%.
   - Pressing `Space` while wall sliding executes an outward angled **Wall Kick**, allowing players to scale sheer mountain gorges without placing blocks.

3. **Verlet Scarf Gliding**:
   - Holding `Space` mid-air unfurls Rin's conductive cyan scarf.
   - The scarf uses a 5-node **Verlet integration** physics rig ($x_{new} = 2x - x_{prev} + a \cdot \Delta t^2$) with air drag = 0.94.
   - Reduces falling speed to a gentle drift, converting downward velocity into horizontal glide momentum ($240\text{ px/s}$).

4. **8-Directional Spark Dash**:
   - Double-tapping a movement key or pressing the dedicated Dash key fires a lightning-fast kinetic burst.
   - Grants **12 Invulnerability Frames (I-Frames)** to phase cleanly through enemy attacks, spike traps, or laser barrages.
   - Breaches through fragile cracked blocks.

---

## 👥 2. The 6-Second Echo Sandbox Clone (Mirage Tempo)

This is one of the most innovative systems in EchoBound. Rin has the ability to record her own spacetime frequency and project a living **Echo Clone**:

```
[ ACTIVATE RECORD ] ---> Perform 6 Seconds of Actions ---> [ DEPLOY ECHO CLONE ]
(Press [R] Hotkey)       (Mining, Chopping, Attacking)      (Repeats Loop Autonomously)
```

### How to Use the Echo Clone:
1. Press `[R]` to begin recording. A cyan stopwatch HUD rings around Rin.
2. Spend the next 6 seconds performing any sequence of actions:
   - Mine an ore vein with your pickaxe.
   - Swing your sword at a mob spawn choke-point.
   - Run along a path placing stone bricks.
3. Press `[R]` again (or wait for the 6-second timer to finish).
4. An ethereal, translucent cyan Echo Clone manifests and loops that exact sequence!

### Tactical Applications:
- **Dual-Vein Speed Mining**: While your clone mines out an entire copper node, you can mine an adjacent iron vein, doubling your resource collection rate.
- **Combat Mirage Decoy**: Record a sequence of aggressive slashes; deploy the clone to pin down an elite mob while you flank from behind for massive critical backstabs.
- **Puzzle Automation**: Use the clone to stand on remote pressure plates while you slip through closing portcullis gates.

---

## 🧱 3. 3D Voxel Building, Raycast Mining & Autotiling

The world is constructed out of interactive 3D voxels rendered with nearest-neighbor integer scaling:

- **Raycast Mining**: Hold `Left-Click` with a tool equipped to project a raycast into the cursor's world-coordinate. Mining speed scales with tool material tier (Wood $\to$ Stone $\to$ Iron $\to$ Titanium).
- **Adjacent Block Placement**: `Right-Click` with blocks selected from your hotbar to snap blocks seamlessly to the target surface.
- **Red Blob Bitmask Autotiling**: Terrain uses 4-bit 16-tile adjacency mapping. Dirt, stone, grass, and snow automatically calculate surrounding neighbors, generating organic beveled edges, cliff corners, and natural cave junctions without hard visual seams.

---

## 🎮 4. Master Controls & Hotkey Directory

Keep this reference open on your second monitor. Every key is mapped for maximum ergonomic flow:

```
+-----------------------------------------------------------------------------------------+
|                                    CONTROLS DIRECTORY                                   |
+--------------------------+------------------------------+-------------------------------+
| Key / Input              | Action                       | Context                       |
+--------------------------+------------------------------+-------------------------------+
| W, A, S, D               | Move (Up/Down/Left/Right)    | Exploration & Traversal       |
| Space                    | Jump / Wall Kick / Glider    | Tap to jump, hold to glide    |
| Shift (Hold)             | Sprint / Dash Burst          | High-speed traversal          |
| Left Mouse Button        | Attack / Mine / Select       | Primary interaction           |
| Right Mouse Button       | Place Block / Secondary Use  | Building & consumable use     |
| Mouse Scroll / Keys 1-8  | Hotbar Quickslot Selection   | Fast item switching           |
| [E]                      | Open Inventory & Equipment   | Gear, runes, and bags         |
| [C]                      | Open Crafting Menu           | Recipes & blueprints          |
| [Q]                      | Open Quest Log & Chronicles  | Mission objectives & lore     |
| [R]                      | Record / Deploy Echo Clone   | 6-second spacetime automation |
| [H] / [F1]               | Help / Tutorial Manual       | In-game interactive guide     |
| [Esc]                    | Pause Menu / Resume / Options| Settings, audio, & save game  |
| [F3]                     | Debug Info & Coordinates     | FPS, chunk coords, entity count|
| [F4]                     | Silhouette Mode Toggle       | Readability accessibility     |
+--------------------------+------------------------------+-------------------------------+
```

---

## 💡 5. Pro Tips for New Spark Runners

1. **Never Waste Daylight**: Spend the sunny hours chopping wood, mining surface ores, and finding safe shelter. Night brings out Shadow Creepers with amplified aggro ranges.
2. **Always Pack Warm Stew**: If you plan to explore Glacial Ridge, hypothermia will continuously drain your stamina unless you have warm food buffs active.
3. **Listen to Pip's Chimes**: Pip's pitch shifts higher when rare subterranean chests are nearby. Follow the sound, dig down, and claim your loot!
4. **Use Number Keys 1-8**: Always keep your Melee Weapon in slot 1, Pickaxe in slot 2, Health Potions in slot 3, and Torches in slot 4 for instant muscle-memory reaction.
