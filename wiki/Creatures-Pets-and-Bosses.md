# 🐉 Creatures, Pets & Bosses: Sea Kings of the Voxel Seas

> *"There are beasts in the deep mist that have lived since before the Resonance fell. You don't hunt them for sport; you earn their respect, or you become part of the terrain."* — Sylvan, Chronicler of the Wilds

---

## 🐾 1. The Living Wildlife Ecosystem

The wilderness of Aethelgard isn't populated by brain-dead wandering sprites. Every creature operates on a deterministic **AI State Machine** (`IDLE` $\to$ `PATROL` $\to$ `CHASE` $\to$ `ATTACK` $\to$ `FLEE`), responding dynamically to player proximity, combat noise, time of day, and environmental hazards:

```
[ IDLE ]  <---- Distance > 200px (Resting / Foraging)
   |
[ CHASE ] <---- Distance <= 200px (Locks onto Player)
   |
[ ATTACK ]<---- Distance <= 26px (Executes Melee / Ranged Strike)
   |
[ FLEE ]  <---- HP < 20% (For Passive / Neutral Wildlife)
```

### Wildlife Temperament Classes:

1. **PASSIVE (Docile Fauna)**: Wild Hares, Mountain Sheep, Meadow Deer. They flee immediately when startled and drop raw cooking ingredients (Tender Meat, Wool, Leather).
2. **NEUTRAL (Territorial Beasts)**: Timber Wolves, Stone Boars, Cavern Bats. They mind their own business until the player enters their territory (within 5 blocks) or attacks first.
3. **HOSTILE (Corrupted automatons & Shadow creeps)**: Shadow Creepers, Sand Skitterers, Clockwork Drones. Hyper-aggressive, chase on sight, and coordinate flank attacks.

---

## 🐺 2. The 5 Tameable Pets & Companion Perks

Just like how Luffy has Chopper and the Straw Hats tame giant Sea Kings and Kraken, Rin can tame wild creatures across the biomes using specialized **Taming Treats** crafted at the Cooking Hearth:

```
+-----------------------------------------------------------------------------------------+
|                                TAMEABLE COMPANIONS GUIDE                                |
+------------------+-------------------+--------------------+-----------------------------+
| Companion        | Biome Habitat     | Taming Food        | Unique Active Perk          |
+------------------+-------------------+--------------------+-----------------------------+
| Ember Fox        | Ember Wastes      | Spiced Jerky       | +35% Volt & Ember Fire DMG  |
| Frosthound Wolf  | Glacial Tundra    | Frozen Fish Fillet | +25% Armor & Frost Slow Bite|
| Cavern Owl       | Subterranean Caves| Glowberry Mash     | 18-block Radar & Night Vision|
| Jade Tortoise    | Whispering Greens | Sweet Greens Salad | +50 HP Pool & Shield Barrier|
| Spark Sprite     | Ancient Ruins     | Pure Echo Shard    | +40% Mining & Move Speed    |
+------------------+-------------------+--------------------+-----------------------------+
```

### Companion Care & Leveling:
- **Affection System**: Companions gain XP as you explore, fight alongside them, and feed them their favorite dishes.
- **Auto-Loot Gathering**: Tamed pets pick up loose item drops in an 8-block radius around Rin, saving you from inventory hassle during chaotic mob encounters.
- **Combat Assistance**: Tamed wolves and foxes engage hostile mobs, drawing aggro and dealing continuous bleed/burn damage.

---

## 🏇 3. Mounts & Cross-Country Traversal

Walking everywhere across the infinite voxel terrain? We don't do that here. Mount up:

1. **Stallion of the Plains**: Standard horse mount. High linear speed ($320\text{ px/s}$), capable of clearing 2-block vertical steps automatically without jumping.
2. **Armored Boar**: Slower ($220\text{ px/s}$), but plows directly through loose dirt, sand, and foliage blocks without losing momentum.
3. **Glacial Elk**: Ignores ice slippage and deep snow slowing debuffs; excels in high-altitude mountain climbing.

---

## 👑 4. The Legendary Bosses (The Emperors of the Sea)

In the far corners of the map dwell three mythical entities of apocalyptic scale. Defeating them drops legendary materials required to forge the highest tier of gear:

### 🐉 The Obsidian Dragon (The "Kaido" of EchoBound)
* **Location**: The Caldera Peak at the heart of the Ember Wastes.
* **Threat Level**: Emperor Tier (Level 45+ Recommended).
* **Appearance**: Colossal black-scaled dragon pulsing with internal magma veins. Wingspan spans 48 screen blocks.
* **Phases**:
  - *Phase 1 (Grounded)*: Crushing tail sweeps, forward fire breath sweeping 180 degrees, and ground shockwaves.
  - *Phase 2 (Airborne / Fire Rain)*: Takes flight; summons meteor showers across the arena. Player must use Gale Dash and Scarf Gliding between elevated ruins to strike its wings.
  - *Phase 3 (Molten Awakening)*: Arena floods with rising lava. The Dragon enrages with +50% attack speed and fires the *Hellfire Beam* that incinerates solid stone blocks.
* **Loot**: *Dragon Heartstone*, *Draconic Scales* (forges Dragonscale Plate Armor, granting complete fire immunity and +80 HP).

### 🦅 The Solar Phoenix (The "Marco" of the Sky)
* **Location**: Floating Sky Isle above the Whispering Grasslands.
* **Specialty**: Emits brilliant solar radiance. When defeated, it does not die; it transforms into a golden egg that can be hatched into a legendary flying companion, granting the player a passive self-revive perk!

### 🦄 The Celestial Unicorn
* **Location**: Deep sacred grove revealed only during a full moon.
* **Specialty**: Fastest mount in the entire game ($450\text{ px/s}$). Can literally gallop across open water and air currents as if running on solid glass.
