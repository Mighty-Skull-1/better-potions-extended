# 🧪 BetterPotionsExtended

<div align="center">

[![Modrinth Version](https://img.shields.io/badge/version-1.0.7-blue.svg?style=for-the-badge)](https://modrinth.com/plugin/betterpotionsextended)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/betterpotionsextended?logo=modrinth&label=Downloads&color=00AF5C&style=for-the-badge)](https://modrinth.com/plugin/betterpotionsextended)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Spigot-orange.svg?style=for-the-badge)](https://papermc.io)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-brightgreen.svg?style=for-the-badge)](https://www.minecraft.net)
[![Java](https://img.shields.io/badge/Java-21+-red.svg?style=for-the-badge)](https://adoptium.net)

**An advanced, high-performance sandbox utility engine designed to seamlessly override, extend, and visually configure custom potion mechanics across all tiers—fully supporting 1.21 Trial Chamber variants via real-time interactive UI controls!**

[Download Latest Release](https://modrinth.com/plugin/betterpotionsextended) • [Report Issues](https://github.com/Mighty-Skull-1/better-potions-extended/issues) • [Source Code](https://github.com/Mighty-Skull-1/better-potions-extended)

</div>

---

## ✨ Why BetterPotionsExtended?

In vanilla Minecraft, potion durations are rigid and hardcoded. Vanilla client tooltips also frequently display inaccurate durations when modified server-side, confusing players. 

**BetterPotionsExtended** solves this completely:
* **No Client Mods Needed:** 100% server-side. Vanilla players connect without downloading any mods or resource packs.
* **Smart Tooltip Synchronizer:** Automatically hides misleading vanilla tooltips and injects clean, color-accurate status lines (e.g. `Strength II (03:00)`) matching your server's exact configuration.
* **Interactive In-Game Dashboard:** No need to edit config files manually in a terminal! Type `/bpe potions` to tune durations live in-game with real fluid-colored bottles.

---

## 🚀 Key Features

* ⚡ **Total Duration Customization:** Configure independent timers for **Normal**, **Extended**, **Splash**, and **Lingering** tracks across both **Tier I** and **Tier II**.
* 🎛️ **Full Tier I & Tier II Support:** Seamlessly toggle between modifying Tier I and Tier II potion curves straight from the in-game GUI.
* 🌈 **Authentic Fluid Colors:** The 54-slot dashboard accurately colors every bottle to match its real liquid visuals in-game (including Speed, Jump Boost, and 1.21 Trial Chamber variants).
* 👁️ **Live Center Preview Bottle:** The edit panel features a central preview potion bottle that displays live updated durations and fluid colors as you click `+30s` / `-30s`.
* 🐢 **True Vanilla Amplifier Fidelity:** Complex multi-effect potions like **Turtle Master** preserve their exact vanilla amplifier curves (Slowness IV / Resistance III for Tier I, and Slowness VI / Resistance IV for Tier II).
* 🏹 **1.21+ Trial Chamber Support:** Built-in native support for **Weaving**, **Wind Charged**, **Oozing**, and **Infested** potions.
* 🔄 **Instant Hot-Reload:** Configuration modifications sync immediately to active player inventories without requiring a server reboot.
* 🛡️ **High-Performance Architecture:** Optimized using lightweight event intercepts designed specifically for high-population production Paper and Purpur environments.

---

## 🎮 Interactive GUI Dashboard (`/bpe potions`)

When you run `/bpe potions`, an interactive **54-slot visual dashboard** opens:

```text
 ┌────────────────────────────────────────────────────────┐
 │ [Strength]  [Speed]   [Slowness]  [Regen]   [Poison]...│ ➔ Tier I & II hover stats
 │ [FireRes]   [WaterBr] [NightVis]  [Invis]   [Jump]...  │ ➔ Real liquid fluid colors
 │ [TurtleM]   [Weaving] [WindChrg]  [Oozing]  [Infest]...│ ➔ 1.21 Trial Chamber support
 │                                                        │
 │ [Reload Config]             [Close Dashboard]          │ ➔ Live administrative tools
 └────────────────────────────────────────────────────────┘
```

Clicking any potion opens the **Modify Panel**:
* 🟢 **Top Row:** Click ingredients (Sugar, Redstone, Gunpowder, Dragon's Breath) to add **+30s**.
* 🧪 **Center Slot:** Preview potion bottle showing live configured times and accurate fluid tint.
* 🔴 **Middle Row:** Click ingredients (Fermented Eye, Repeater, Flint, Glass Bottle) to subtract **-30s**.
* 🌟 **Glowstone Dust Button:** Instantly toggle between configuring **Tier I** and **Tier II**.
* 🚪 **Barrier Button:** Return to the main dashboard.

---

## 🧪 Supported Potions

| Category | Supported Potion Types | Available Tracks |
| :--- | :--- | :--- |
| **Combat & Classic** | Strength, Speed (Swiftness), Slowness, Regeneration, Poison, Weakness | Normal, Extended, Splash, Lingering (Tier I & II) |
| **Survival & Utility** | Fire Resistance, Water Breathing, Night Vision, Invisibility, Jump Boost (Leaping), Slow Falling, Turtle Master, Luck, Haste | Normal, Extended, Splash, Lingering (Tier I & II) |
| **1.21 Trial Chamber** | Weaving, Wind Charged, Oozing, Infested | Normal, Splash, Lingering |

---

## 🔧 Commands & Permissions

All subcommands feature intelligent, deep **Tab-Completion** that dynamically populates online player names, valid potion keys, forms, and tiers as you type.

| Command | Description | Default Permission |
| :--- | :--- | :--- |
| `/bpe potions` | Opens the 54-slot interactive visual GUI configuration dashboard. | `betterpotionsextended.admin` (OP) |
| `/bpe give <player> <potion> [type] [tier]` | Spawns a custom potion stack directly into a player's inventory. | `betterpotionsextended.admin` (OP) |
| `/bpe reload` | Hot-reloads `config.yml` from disk and synchronizes active player tooltips. | `betterpotionsextended.admin` (OP) |

### Command Examples:
```bash
# Give Steve a Splash Potion of Weaving
/bpe give Steve WEAVING splash

# Give Alex a Tier II Normal Potion of Strength
/bpe give Alex STRENGTH normal 2

# Give Alex a Tier I Lingering Potion of Speed
/bpe give Alex SPEED lingering 1
```

---

## ⚙️ Configuration (`config.yml`)

All durations are defined in **seconds**. Set any value to `-1` if you want to fall back to vanilla Minecraft defaults:

```yaml
# BetterPotionsExtended Configuration
# Durations are defined in seconds. Set a value to -1 to fall back to vanilla defaults.

potions:
  # --- CLASSIC & COMBAT POTIONS ---
  STRENGTH:
    tier_1:
      normal_duration: 360
      extended_duration: 480
      splash_duration: 270
      lingering_duration: 90
    tier_2:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45

  SPEED:
    tier_1:
      normal_duration: 360
      extended_duration: 480
      splash_duration: 270
      lingering_duration: 90
    tier_2:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45

  # --- 1.21+ TRIAL CHAMBER POTIONS ---
  WEAVING:
    tier_1:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45

  WIND_CHARGED:
    tier_1:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45

messages:
  no-permission: "&cYou do not have permission to execute this command."
  reload-success: "&a[BetterPotionsExtended] Configuration successfully reloaded from the server!"
```

---

## 📦 Installation

1. Download **`better-potions-extended-1.0.7.jar`** from [Modrinth](https://modrinth.com/plugin/betterpotionsextended) or your target build folder.
2. Place the `.jar` file into your server's `plugins/` directory.
3. Restart or start your server.
4. Open the visual editor in-game using `/bpe potions` or customize `plugins/BetterPotionsExtended/config.yml`.
5. Grant the permission node `betterpotionsextended.admin` to your staff or admin groups.

---

## 🧩 Compatibility

* **Minecraft Version:** `1.21.x` (including `1.21.1` and `1.21.11+`)
* **Server Software:** Paper, Purpur, Spigot
* **Java Version:** Java 21 or higher
* **Client Compatibility:** 100% Vanilla compatible (Bedrock via Geyser supported)

---

## 🤝 Community & Support

* 🐛 **Bug Reports & Feature Requests:** [GitHub Issues](https://github.com/Mighty-Skull-1/better-potions-extended/issues)
* 💻 **Source Code:** [GitHub Repository](https://github.com/Mighty-Skull-1/better-potions-extended)
* 📦 **Modrinth Page:** [BetterPotionsExtended on Modrinth](https://modrinth.com/plugin/betterpotionsextended)

---

<div align="center">
  <sub>Created with ❤️ by <b>Mighty</b></sub>
</div>
