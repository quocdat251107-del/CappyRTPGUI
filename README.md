# CappyRTPGUI
<div align="center">
  <h1>🌟 CappyRTPGUI 🌟</h1>
  <p><i>A modern, visually stunning, and multi-platform UI frontend for BetterRTP.</i></p>

  [![Paper](https://img.shields.io/badge/Paper-1.21+-333333?style=flat-square&logo=papermc)](https://papermc.io/)
  [![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java)](https://adoptium.net/)
  [![BetterRTP](https://img.shields.io/badge/BetterRTP-Required-green?style=flat-square)](https://modrinth.com/plugin/betterrtp)
</div>

---

## 📖 Overview
**CappyRTPGUI** completely overhauls the standard `/rtp` command by replacing it with a beautiful, interactive Graphical User Interface (GUI). It intelligently adapts to the player's client platform, ensuring the best possible experience whether they are on PC, Console, or Mobile!

## ✨ Features
* 🖥️ **Paper Dialog API (Java 1.21.3+)**: Modern Java Edition clients get a beautiful native popup featuring a centered 3D rotating world icon!
* 📱 **Bedrock / Floodgate Support**: Mobile and console players (via GeyserMC) receive a clean, native `SimpleForm` UI with custom icon routing.
* 📦 **Chest GUI Fallback (ViaVersion)**: Older Java clients automatically fallback to a safe 27-slot chest GUI to prevent client crashes or dropped packets.
* ⚡ **Zero Reflection**: Uses 100% native BetterRTP API hooks. No internal reflection, meaning no more `NoSuchMethodException` crashes on updates!
* 🎨 **Fully Customizable**: Edit every single message, title, and item icon via a clean English configuration.

## 📥 Installation
1. Ensure your server is running **Paper 1.21+** and **Java 21**.
2. Download and install the required dependencies:
   * [BetterRTP](https://modrinth.com/plugin/betterrtp) (Required)
   * [Floodgate](https://geysermc.org/) (Optional, for Bedrock UIs)
   * [ViaVersion](https://modrinth.com/plugin/viaversion) (Optional, for older Java client fallbacks)
3. Place `CappyRTPGUI-1.0.0.jar` into your `plugins/` folder.
4. Restart the server and enjoy!

## 💻 Commands & Permissions
| Command | Permission | Description |
|---|---|---|
| `/rtp` | `cappyrtp.use` | Opens the world selection GUI. |
| `/rtp reload` | `cappyrtp.reload` | Reloads the configuration files. |

**World-Specific Permissions:**
* `cappyrtp.world.overworld`
* `cappyrtp.world.nether`
* `cappyrtp.world.end`

## ⚙️ Configuration
The plugin generates a `config.yml` allowing you to configure the default destinations, GUI slots for the chest fallback, and text elements for the Java Dialogs. For Bedrock, `bedrock_gui.yml` allows you to bind texture paths or URLs to buttons.

---
<div align="center">
  <i>Built with ❤️ by CappyRTP Dev</i>
</div>
