# 🧪 BetterPotionsExtended

![Version](https://img.shields.io/badge/version-1.0.7-blue.svg)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/betterpotionsextended?logo=modrinth&label=Modrinth%20Downloads)](https://modrinth.com/plugin/betterpotionsextended)
![Platform](https://img.shields.io/badge/platform-Paper-orange.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1+-green.svg)

An advanced, high-performance sandbox utility engine designed to seamlessly override, extend, and visually display custom potion mechanics across all configurations—fully supporting Tier I, Tier II, and 1.21 Trial Chamber variants like Weaving via real-time interactive UI controls.

---

## 🚀 Features

* ⚡ **Total Potion Customization:** Configure completely custom durations for Normal, Extended, Splash, and Lingering tracks across both **Tier I** and **Tier II**.
* 📦 **Dynamic Inventory Synchronizer:** Automatically hides deceptive client-side vanilla tooltips and replaces them with custom status lines (e.g. `Strength II (03:00)`) matching your exact server configs.
* 🌈 **Visual Editor Dashboard:** Open a 54-slot colored in-game GUI (`/bpe potions`) where every potion bottle accurately displays its real vanilla fluid texture and colors (including Speed, Jump Boost, and Trial Chamber potions).
* 🎛️ **Live In-Game Fine Tuning:** Modify durations up or down in 30-second increments with interactive buttons, inspect live values via the center preview bottle, and effortlessly toggle between **Tier I** and **Tier II** settings.
* 🐢 **Full Mechanics & Amplifier Accuracy:** Preserves complex vanilla status effects and amplifiers, including Turtle Master's dual Slowness and Resistance curves.
* 🔄 **Hot-Reload Support:** Configuration changes save instantly and sync live across all online players without requiring a server restart.
* 🛡️ **Ultra-Lightweight:** Written using native Paper event intercepts and optimized explicitly for high-population production environments.

---

## 🔧 Commands & Permissions

All subcommands feature intelligent, deep **Tab-Completion** that dynamically populates online player names, valid potion keys, item forms, and tier selections as you type.

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/bpe potions` | Opens the interactive visual GUI configuration dashboard. | `betterpotionsextended.admin` |
| `/bpe give <player> <potion> [type] [tier]` | Instantly spawns a custom potion stack into a player's inventory. | `betterpotionsextended.admin` |
| `/bpe reload` | Reloads the `config.yml` settings live into the server memory. | `betterpotionsextended.admin` |

### Command Examples:
* `/bpe give Steve WEAVING splash`
* `/bpe give Alex STRENGTH normal 2` (Gives Strength II)
* `/bpe give Alex SPEED lingering 1`

---

## 📦 Installation

1. Download the latest compiled jar: `better-potions-extended-1.0.7.jar`.
2. Drop the jar file into your server's `/plugins/` folder.
3. Start or restart your server.
4. Configure your desired durations using `/bpe potions` or by editing `plugins/BetterPotionsExtended/config.yml`.
5. Grant the permission node `betterpotionsextended.admin` to your administrator groups.

---

## 🛠️ Default Configuration File

```yaml
# BetterPotionsExtended Configuration
# All durations are defined in seconds. Set a value to -1 to fall back to vanilla defaults.

potions:
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
  WEAVING:
    tier_1:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45
```
