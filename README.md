# 🧪 BetterPotionsExtended

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Paper-orange.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1+-green.svg)

An advanced, high-performance sandbox utility engine designed to seamlessly override, extend, and visually display custom potion mechanics across all configurations—fully supporting 1.21 Trial Chamber variants like Weaving via real-time interactive UI controls.

---

## 🚀 Features

* ⚡ **Total Potion Customization:** Configure completely custom durations for Normal, Extended, Splash, and Lingering tracks.
* 📦 **Dynamic Inventory Synchronizer:** Automatically hides deceptive client-side vanilla tooltips and replaces them with a custom blue status line matching your config timers.
* 🌈 **Visual Editor Dashboard:** Open a beautifully colored in-game GUI where every potion bottle matches its actual vanilla liquid texture color.
* 🛠️ **In-Game Fine Tuning:** Click interactive buttons inside the GUI to add or subtract 30 seconds from any potion track on the fly.
* 🔄 **Hot-Reload Support:** Configuration changes save instantly and update live across the server without requiring a restart.
* 🛡️ **Ultra-Lightweight:** Written using native event intercepts and optimized explicitly for high-population Paper environments.

---

## 🔧 Commands & Permissions

All subcommands feature intelligent, deep **Tab-Completion** that dynamically populates online player names, valid potion keys, and item forms as you type.

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/bpe potions` | Opens the interactive visual GUI configuration editor. | `betterpotionsextended.admin` |
| `/bpe give <player> <potion> [type]` | Instantly spawns a custom potion stack into a player's inventory. | `betterpotionsextended.admin` |
| `/bpe reload` | Reloads the `config.yml` settings live into the server memory. | `betterpotionsextended.admin` |

### Command Examples:
* `/bpe give Jelani WEAVING splash`
* `/bpe give Alex STRENGTH normal`

---

## 📦 Installation

1. Download the latest compiled jar from the `target/` directory: `better-potions-extended-1.0.0.jar`.
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
  SPEED:
    tier_1:
      normal_duration: 360
      extended_duration: 480
      splash_duration: 270
      lingering_duration: 90
  WEAVING:
    tier_1:
      normal_duration: 180
      splash_duration: 135
      lingering_duration: 45
