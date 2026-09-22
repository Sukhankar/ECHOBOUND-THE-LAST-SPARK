# ECHOBOUND: THE LAST SPARK — CHARACTER BIBLE & WORLD DESIGN SPECIFICATION

## I. RESEARCH FOUNDATIONS & 1-LINE TAKEAWAYS

1. **Shape Language & Silhouette Readability**: Game silhouettes must communicate role, alignment, and threat level instantly in solid black through exaggerated primary and secondary geometric forms.
2. **t3ssel8r, "Giving Personality to Procedural Animations using Math"**: Second-order dynamical systems ($f$ natural frequency, $\zeta$ damping ratio, $r$ initial response) yield expressive, physically organic anticipation, lag, and overshoot for procedural rigs.
3. **Verlet Integration Rope/Cloth**: Particle chains updated with Verlet integration ($x_{new} = 2x - x_{prev} + a \cdot \Delta t^2$) and iterative distance relaxation create silky, jitter-free secondary motion for Rin's scarf and dangling cables.
4. **Red Blob Games Autotiling & Grid Algorithms**: Bitmask adjacency mapping (4-bit 16-tile or 8-bit marching corners) transforms raw ASCII maps into seamless, organic environments with beveled edges and inner junctions.
5. **Squirrel Eiserloh, "Juicing Your Cameras With Math"**: Trauma-based camera shake squaring a normalized trauma parameter ($trauma^2$) creates impactful, crisp explosions that decay smoothly into imperceptible micro-rumble.
6. **Celeste / Maddy Thorson Game-Feel Techniques**: Forgiving tolerances (coyote time, jump buffering, apex float, and procedural squash-and-stretch) prioritize player intent over rigid simulation.

---

## II. CORE DESIGN LAWS

1. **Gameplay First**: Visuals and procedural rigs scale; collision boxes are strictly separated from character silhouettes. Animation never blocks or delays tick-one input.
2. **Silhouette Law (F4 Toggle)**: Every entity must be 100% identifiable when filled solid black at native 1x resolution (640x360).
3. **Shape Language**:
   - **Circles**: Friendly, warm, playful, buoyant. (Pip, friendly emblems, core nodes).
   - **Squares**: Grounded, stable, structural, immovable. (Mira, heavy boots, stone guardians, industrial platforms).
   - **Triangles**: Speed, agility, sharp danger, aggression. (Rin's scarf & jacket, enemy spikes, pounce telegraphs).
4. **Chromatic Alignment**:
   - **Allies / Spark Tech**: Cyan family (`#4FF0FF`, `#00C8E6`, `#E0FFFF`).
   - **Corruption / Conductor Error**: Magenta/Purple family (`#D23CFF`, `#9E00D9`, `#FF3296`).
   - **Neutral / World**: Amber (`#FFB347`), Moss (`#386641`), Rust (`#8C3B1A`), Slate (`#161A24`).
5. **Seeded Determinism**: Gameplay mechanics use a dedicated deterministic PRNG (`SplitMix64`). Visual particles, cosmetic dust, and background twitches use a separate PRNG to guarantee Echo replay bit-identity.

---

## III. CHARACTER SPECIFICATIONS

### 1. RIN (The Spark Runner)
- **Role & Personality**: Agile, courageous young Spark Runner who ventures into silence to rekindle the world's voice. Bold, curious, and kinetically restless.
- **Dominant & Secondary Shapes**: Dominant **Triangle** (speed, razor agility in scarf, jacket flare, stride), Secondary **Circle** (curious oversized goggles, round head, friendly energy).
- **Proportions**: Drawn ~16×24 px. Gameplay Hitbox: 10×14 px.
- **Color Palette**:
  - Base Jacket: `#F2803A` (Warm Sunroot Orange)
  - Shadow: `#9E381A` (Cool Burnt Umber)
  - Highlight: `#FFA666` (Warm Peach)
  - Scarf & Spark Energy: `#4FF0FF` (Electric Cyan)
  - Boots: `#3B2A2A` (Heavy Leather Bronze)
  - Hair: `#241E38` (Midnight Indigo)
  - Goggles: `#FFD166` (Sun Amber)
- **Silhouette Features in Solid Black**:
  1. Flowing 5-node segmented energy scarf waving backwards based on velocity.
  2. Bulbous brow line formed by the forehead goggles.
  3. Heavy flared runner boots contrasting a lean triangular jacket hem.
- **Personality in Motion (Second-Order Dynamics)**:
  - Torso/Head Spring: $f = 4.2\text{ Hz}$, $\zeta = 0.55$, $r = 1.20$ (high-energy, eager anticipation, snappy overshoot).
  - Scarf Verlet: Gravity $g = 220\text{ px/s}^2$, drag = $0.94$, stiffness iterations = 4.
- **Animation States**: `IDLE` (breathing, fidget blink, 4s idle looking around), `RUN` (velocity lean, foot cycle), `JUMP_RISE` (vertical stretch), `JUMP_APEX` (tuck float), `FALL` (scarf flutters upward), `LAND` (squash 1.3×), `DASH` (horizontal spear stretch + trailing ghosts), `WALL_SLIDE` (tilted friction hold with gauntlet contact sparks).
- **Voice**: Crisp synthesized square-wave pulse blips with ascending chirp on jump.

### 2. PIP (The Last Spark Fragment)
- **Role & Personality**: Tiny sentient spark creature rescued by Rin. Playful, loyal, vocal only through musical acoustics and glyph thoughts.
- **Dominant & Secondary Shapes**: Dominant **Circle** (pure organic orb, safe, friendly), Secondary **Triangle** (sharp flickering top spark antenna).
- **Proportions**: Drawn ~10×10 px. Floats ~14 px behind/above Rin's shoulder.
- **Color Palette**:
  - Core: `#FFFFFF` (Pure Spark)
  - Inner Glow: `#4FF0FF` (Cyan Resonator)
  - Outer Aura: `#0096B4` (Deep Turquoise)
  - Antenna Spike: `#FFDF6D` (Golden Filament)
- **Silhouette Features in Solid Black**:
  1. Small circular body with dual orbiting satellite motes.
  2. Single vertical triangular antenna flicking upwards.
  3. Smooth teardrop trailing wake during high-speed chases.
- **Personality in Motion (Second-Order Dynamics)**:
  - Spring-Damped Follower: $f = 2.2\text{ Hz}$, $\zeta = 0.70$, $r = 0.20$ (liquid-smooth, floating companion lag with gentle settle).
- **Animation States**: `FLOAT_IDLE` (bobbing sine wave), `FOLLOW` (spring stretch toward Rin), `EXCITED` (rapid 360° spin loop), `WARN` (flickering pulse near corruption).
- **Voice**: Dual harmonic sine whistles (high melody glissando).
- **Region Color Variants**: Sunroot (Cyan/Gold), Rustrail (Warm Amber/White), Neon Harbor (Electric Blue/Violet).

### 3. MIRA (The Rustrail Engineer)
- **Role & Personality**: Practical, grease-stained engineer in the hub workshop who rebuilds Spark devices from scrap. Gruff, dependable, deeply observant.
- **Dominant & Secondary Shapes**: Dominant **Square** (grounded stance, blocky torso, heavy tool apron), Secondary **Circle** (round welding visor rested on brow).
- **Proportions**: Drawn ~18×26 px.
- **Color Palette**:
  - Overalls: `#445566` (Steel Slate)
  - Apron: `#8C5028` (Heavy Canvas Leather)
  - Tool Grip: `#FFB347` (Industrial Safety Yellow)
  - Skin: `#D29672` (Warm Bronze)
  - Visor Glass: `#00E5FF` (Glow Cyan)
- **Silhouette Features in Solid Black**:
  1. Wide, heavy rectangular shoulders with blocky tool pouch on hip.
  2. Massive wrench silhouette slung across her back.
  3. Raised hemispherical welding visor atop head.
- **Personality in Motion (Second-Order Dynamics)**:
  - Stance: $f = 1.4\text{ Hz}$, $\zeta = 0.95$, $r = 0.0$ (heavy, grounded, deliberate, zero flutter).
- **Animation States**: `TINKER_IDLE` (tap-testing wrench, checking notes), `TALK` (nodding gesture), `UPGRADE_CRAFT` (burst of welding sparks).
- **Voice**: Low-frequency resonant triangle-wave clinks.

### 4. JAX (The Ancient Chip Hunter)
- **Role & Personality**: Flamboyant wanderer who searches ruins for ancient data chips. Sleek, secretive, and loves dramatic flair.
- **Dominant & Secondary Shapes**: Dominant **Triangle** (slanted trench coat tails, pointed boots, sharp collar), Secondary **Rectangle** (slender scanner monocle and scroll tube).
- **Proportions**: Drawn ~16×28 px.
- **Color Palette**:
  - Coat: `#2A2F45` (Midnight Navy)
  - Scarf: `#C83250` (Crimson Velvet)
  - Monocle Beam: `#4FF0FF` (Cyan Lens)
  - Highlights: `#E2C044` (Gilded Brass)
- **Silhouette Features in Solid Black**:
  1. Long triangular coat tails parting behind legs.
  2. High popped asymmetrical collar.
  3. Slender monocle projection arm jutting forward from temple.
- **Personality in Motion (Second-Order Dynamics)**:
  - Sway: $f = 2.0\text{ Hz}$, $\zeta = 0.45$, $r = 0.8$ (dramatic, swaggering coat physics).
- **Animation States**: `LEAN_IDLE` (flipping a chip, checking horizon), `OFFER_QUEST` (extending arm holding holographic chip).
- **Voice**: Rapid staccato brass-like plucked chirps.

### 5. ORI (The Broken Technician)
- **Role & Personality**: Ancient machine operator wandering near the final gate. Haunted, poetic, seeking peaceful closure for the world's machines.
- **Dominant & Secondary Shapes**: Dominant **Rectangle** (hollow, stooped monastic posture, long vertical tunic), Secondary **Circle** (dimly glowing spark sphere embedded in wooden staff).
- **Proportions**: Drawn ~16×24 px.
- **Color Palette**:
  - Cowl: `#383540` (Faded Ash)
  - Robes: `#222026` (Worn Charcoal)
  - Staff Core: `#B48CFF` (Fading Twilight Violet)
- **Silhouette Features in Solid Black**:
  1. Severe hunched posture with deep drooping hood silhouette.
  2. Vertical staff topped with a suspended sphere.
  3. Trailing hem touching the ground.
- **Personality in Motion**:
  - Drift: $f = 0.9\text{ Hz}$, $\zeta = 1.0$, $r = 0.0$ (slow, solemn, glacial breathing).
- **Animation States**: `CHANTS_IDLE` (staff gentle pulse), `REVEAL` (points staff toward Neon Spire).
- **Voice**: Soft choral fifth-interval drone.

### 6. THE CONDUCTOR (Antagonist — Echo Protocol)
- **Role & Personality**: Supreme AI guardian of the world grid. Believes variation and noise are fatal errors; seeks total stasis and predictable perfection.
- **Dominant & Secondary Shapes**: Dominant **Circle** (nested, concentric mathematical rings revolving in counter-rotation), Secondary **Triangle** (rigid needle frequency spikes radiating outward).
- **Proportions**: Drawn ~40×56 px.
- **Color Palette**:
  - Outer Rings: `#111116` (Pitch Obsidian)
  - Geometric Highlights: `#F0F4F8` (Pristine Ceramic White)
  - Error Aura: `#D23CFF` (Overclock Magenta)
  - Frequency Core: `#00F5D4` (Stolen Spark Frequency)
- **Silhouette Features in Solid Black**:
  1. Concentric hovering disconnected segmented rings.
  2. Central diamond ocular eye suspended in void.
  3. Symmetrical radiating frequency spikes that expand during attacks.
- **Personality in Motion**:
  - Mechanical Precision: $f = 5.0\text{ Hz}$, $\zeta = 1.0$, $r = 0.0$ (instant, non-human deceleration, perfect harmonic oscillation).
- **Animation States**: `MONITOR` (rings gyrate), `SWEEP_ARM` (mechanical ring blades whip across arena), `ECHO_MIRROR` (replicates Rin's pose in dark magenta), `SILENCE` (rings lock into singular monolithic disc).
- **Voice**: Pure sine frequency harmonics with metallic low-pass vocoder click.

---

## IV. CORRUPTED ENEMIES (Magenta Threat System)

### 1. CRAWLER (Patrol Insect Mech)
- **HP**: 1 | **Hitbox**: 14×10 px
- **Dominant / Secondary**: **Square** body with jagged **Triangle** legs.
- **Color**: `#33142A` (Obsidian Shell), `#D23CFF` (Corrupt Magenta Veins), `#FF3296` (Sensor Eye).
- **Silhouette**: Low squat silhouette, 4 segmented scorpion-style legs scurrying with alternating gait.
- **Behavior**: Patrols solid ledge; reverses on wall or cliff edge. Speed 60 px/s.

### 2. BUZZ DRONE (Aerial Diver)
- **HP**: 2 | **Hitbox**: 12×12 px
- **Dominant / Secondary**: Inverted **Triangle** body with **Circle** spinning rotor ring.
- **Color**: `#281034` (Chassis), `#E040FB` (Rotor Glow), `#FF0055` (Targeting Laser).
- **Silhouette**: Sharp downward wedge with dual spinning rotor halos and hanging stinger needle.
- **Behavior**: Sine-wave patrol in air ($y = y_0 + \sin(\omega t) \cdot 18$). If Rin is within 120 px beneath, flashes red and dives at 260 px/s.

### 3. THORN TURRET (Organic Plant/Machine Hybrid)
- **HP**: 3 | **Hitbox**: 16×16 px
- **Dominant / Secondary**: Grounded **Square** pod with 3-petal **Triangle** cannon blossom.
- **Color**: `#1A1B2F` (Root Base), `#BA26FF` (Petal Shield), `#FF5588` (Charged Core).
- **Silhouette**: Bulky pedestal anchoring a 3-prong blossom barrel that tracks Rin with rotational steps.
- **Behavior**: Stationary. Rotates toward Rin. Every 2.2 s charges up for 0.4 s (pulsing telegraph) and fires a fast linear needle projectile.

### 4. DELIVERY BOT (Rogue Cargo Unit)
- **HP**: 2 | **Hitbox**: 14×16 px
- **Dominant / Secondary**: Boxy **Rectangle** torso atop single high-speed gyro **Circle** wheel.
- **Color**: `#3A1E45` (Heavy Crate Body), `#00F0FF` (Flickering broken headlight), `#D23CFF` (Corrupt Motor exhaust).
- **Silhouette**: Tall top-heavy box balancing on a spinning circle wheel, with dual warning antenna rods.
- **Behavior**: Idles until Rin enters horizontal sightline (160 px). Horn sounds, flashes warning hazard light, and rocket-charges in a straight line at 220 px/s.

---

## V. BOSS SPECIFICATIONS

### 1. ROOT GUARDIAN (Stage 1 Mini-Boss)
- **HP**: 12 | **Hitbox**: 44×44 px
- **Dominant / Secondary**: Giant stone **Square** chest and fists wrapped in organic **Circle** vine coils.
- **Color**: `#2C3531` (Ancient Granite), `#4F772D` (Sunroot Moss), `#E040FB` (Corrupt Vine Core).
- **Phases**:
  - *Phase 1*: Fist Slam creating ground shockwaves; exposes glowing back vine core.
  - *Phase 2*: Double Fist Smash + falling debris.
  - *Phase 3*: Dual pressure plates lock core behind an energy shield. Player must Echo-Shift one plate while standing on the other to break the shield!

### 2. IRON MANTIS (Stage 2 Boss)
- **HP**: 16 | **Hitbox**: 36×36 px
- **Dominant / Secondary**: Menacing **Triangle** scythe blades and chassis with segmented **Square** armor plates.
- **Color**: `#3D261A` (Oxidized Iron), `#FF8C42` (Hazard Orange), `#D23CFF` (Corrupt Hydraulic Fluid).
- **Phases**:
  - *Phase 1*: Wall scurrying, telegraphs pounce landing target on floor. Player baits pounce onto electrified floor panels to stun.
  - *Phase 2*: Arena floor partially collapses, mantis sweeps scythes across remaining platforms.
  - *Phase 3*: High-speed wall ricochets leaving corruption flame trails.
