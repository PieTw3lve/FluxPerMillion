<h1 align="center">FluxPerMillion</h1>

<p align="center">
	<img src="https://github.com/PieTw3lve/FluxPerMillion/actions/workflows/maven.yml/badge.svg" alt="Build and Publish"></a>
	<img src="https://img.shields.io/github/v/release/PieTw3lve/FluxPerMillion" alt="GitHub release (latest by date)">
	<img src="https://img.shields.io/badge/Minecraft-1.20.2--1.20.4-orange.svg" alt="Supported versions">
	<img src="https://img.shields.io/badge/SpigotMC-yellow.svg" alt="SpigotMC">
	<a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL v3"></a>
</p>

## Information

FluxPerMillion is a Minecraft plugin that adds an environmental twist to the gameplay. It introduces a new game mechanic, Flux, which is a measure of the environmental impact of player actions. The Flux value increases when players perform actions that would harm the environment in the real world, such as deforestation or excessive fishing, and decreases when players perform eco-friendly actions, such as planting trees or using renewable resources.

## Features

- **Real-time Tracking**: Monitor player actions that contribute to Flux.
- **Sustainable Practices**: Encourage eco-friendly practices like composting, reforestation, and recycling.
- **Environmental Awareness**: Raise consciousness about real-world environmental issues, fostering player engagement in conservation efforts, fire prevention, and pollution control.
- **Customizable Settings**: Configure Flux values, thresholds, decay rates, rewards, and penalties to suit your server's gameplay.
- **Deadly Disasters Support**: Integrate with Deadly Disasters to enhance gameplay with environmental challenges.
- **PlaceholderAPI Support**: Display Flux values in chat, action bar, and scoreboard using PlaceholderAPI placeholders.

## Commands

| Command                                  | Description                          | Permission |
| ---------------------------------------- | ------------------------------------ | ---------- |
| `/fpm inspect <player>`                  | Examine a player's Flux value.       | default    |
| `/fpm toggle`                            | Toggle Flux meter visibility.        | default    |
| `/fpm status`                            | View the Flux meter status.          | default    |
| `/fpm lookup <player> <duration> <page>` | Lookup a player's Flux activity.     | fpm.lookup |
| `/fpm reload`                            | Reload FluxPerMillion configuration. | fpm.reload |

## PlaceholderAPI

| Placeholder                         | Description                               |
| ----------------------------------- | ----------------------------------------- |
| `%fluxpermillion_points%`           | Returns the current Flux value.           |
| `%fluxpermillion_status_level%`     | Returns the Flux meter status level.      |
| `%fluxpermillion_status_color%`     | Returns the Flux meter status color code. |
| `%fluxpermillion_max_points%`       | Returns the maximum Flux meter value.     |
| `%fluxpermillion_min_points%`       | Returns the minimum Flux meter value.     |
| `%fluxpermillion_percentage%`       | Returns the Flux meter percentage.        |
| `%fluxpermillion_percentage_color%` | Returns the Flux meter color code.        |

## Soft Dependencies

- [Deadly Disasters](https://www.spigotmc.org/resources/deadly-disasters.90806/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## Reporting Issues

- If you encounter any issues with the plugin, please report them [here](https://github.com/PieTw3lve/FluxPerMillion/issues).
