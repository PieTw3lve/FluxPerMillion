<h1 align="center">FluxPerMillion</h1>

<p align="center">
	<img src="https://img.shields.io/github/actions/workflow/status/PieTw3lve/FluxPerMillion/maven.yml?style=for-the-badge&logo=GitHub" alt="Build and Publish"></a>
	<img src="https://img.shields.io/github/v/release/PieTw3lve/FluxPerMillion?display_name=tag&style=for-the-badge&label=Release" alt="GitHub release (latest by date)">
	<img src="https://img.shields.io/badge/Minecraft-1.20.2--1.21.1-orange.svg?style=for-the-badge" alt="Supported versions">
	<a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge" alt="License: GPL v3"></a>
</p>

## Information

FluxPerMillion is a Minecraft plugin that adds an environmental twist to the gameplay. It introduces a new game mechanic, Flux, which is a measure of the environmental impact of player actions. The Flux value increases when players perform actions that would harm the environment in the real world, such as deforestation or excessive fishing, and decreases when players perform eco-friendly actions, such as planting trees or using renewable resources.

## Features

- **Real-time Tracking**: Monitor player actions that contribute to Flux.
- **Sustainable Practices**: Encourage eco-friendly practices like composting, reforestation, and animal preservation.
- **Environmental Awareness**: Raise consciousness about real-world environmental issues, suc as fire prevention and pollution.
- **New Game Mechanics**: With the addition of Flux, players can now execute specific actions that were previously impossible in vanilla Minecraft to either increase or decrease Flux.
- **Customizable Settings**: Configure Flux values, thresholds, decay rates, custom gameplay mechanics, rewards, and penalties to suit your server's needs.

## Commands

| Command                                    | Description                       | Permission |
| ------------------------------------------ | --------------------------------- | ---------- |
| `/fpm help`                                | Display the help menu.            | default    |
| `/fpm inspect <player>`                    | Examine a player's Flux value.    | default    |
| `/fpm toggle`                              | Toggle the Flux meter visibility. | default    |
| `/fpm status`                              | View the Flux meter status.       | default    |
| `/fpm add <player> <amount> <category>`    | Add Flux to a player.             | fpm.add    |
| `/fpm remove <player> <amount> <category>` | Remove Flux from a player.        | fpm.remove |
| `/fpm lookup <player> <duration> <page>`   | Lookup a player's Flux activity.  | fpm.lookup |
| `/fpm reload`                              | Reload configuration.             | fpm.reload |

## Placeholders

| Placeholder                         | Description                               |
| ----------------------------------- | ----------------------------------------- |
| `%fluxpermillion_points%`           | Returns the current Flux value.           |
| `%fluxpermillion_status_level%`     | Returns the Flux meter status level.      |
| `%fluxpermillion_status_color%`     | Returns the Flux meter status color code. |
| `%fluxpermillion_max_points%`       | Returns the maximum Flux meter value.     |
| `%fluxpermillion_offset_points%`    | Returns the offset Flux meter value.      |
| `%fluxpermillion_percentage%`       | Returns the Flux meter percentage.        |
| `%fluxpermillion_percentage_color%` | Returns the Flux meter color code.        |

## Soft Dependencies

- [Deadly Disasters](https://www.spigotmc.org/resources/deadly-disasters.90806/)
  - Customize and configure deadly disasters to trigger at specific Flux thresholds.
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
  - Offers placeholders to display Flux values in chat, the action bar, and on scoreboards.

## Reporting Issues

- If you encounter any issues with the plugin, please report them [here](https://github.com/PieTw3lve/FluxPerMillion/issues).
