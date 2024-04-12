<h1 align="center">FluxPerMillion</h1>

<p align="center">
	<img src="https://github.com/PieTw3lve/FluxPerMillion/actions/workflows/maven.yml/badge.svg" alt="Build and Publish"></a>
	<img src="https://img.shields.io/github/v/release/PieTw3lve/FluxPerMillion" alt="GitHub release (latest by date)">
	<img src="https://img.shields.io/badge/Minecraft-1.20.2--1.20.4-orange.svg" alt="Supported versions">
	<img src="https://img.shields.io/badge/SpigotMC-yellow.svg" alt="SpigotMC">
	<a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL v3"></a>
</p>

## Information

This Bukkit/Spigot plugin aims to introduce environmental mechanics into Minecraft gameplay. It includes features such as tracking player actions affecting global warming, implementing sustainable practices, and promoting environmental awareness within the game. The plugin provides tools for server owners to monitor and manage environmental impacts, encouraging players to engage in eco-friendly behaviors. With customizable settings and interactive gameplay elements, this plugin offers a unique way to explore environmental themes in the Minecraft universe

## Features

- **Real-time Tracking**: Monitor player actions that contribute to Flux.
- **Sustainable Practices**: Implement eco-friendly practices, such as using compost bins, replanting trees, and recycling materials.
- **Environmental Awareness**: Promote awareness of environmental issues and encourage players to take action such as animal conservation, fire prevention, and pollution control.
- **Customizable Settings**: Configure the plugin to suit your server's needs, including Flux thresholds, rewards, and penalties.
- **Deadly Disasters Support**: Integrate with Deadly Disasters to create environmental challenges and natural disasters based on player actions.
- **PlaceholderAPI Support**: Display Flux values and status in custom messages and scoreboards.

## Commands

| Command                                  | Description                     | Permission |
| ---------------------------------------- | ------------------------------- | ---------- |
| `/fpm inspect <player>`                  | Inspect a player's Flux value.  | default    |
| `/fpm toggle`                            | Toggles Flux meter visibility.  | default    |
| `/fpm status`                            | Display the Flux status.        | default    |
| `/fpm lookup <player> <duration> <page>` | Lookup a player's Flux history. | fpm.lookup |
| `/fpm reload`                            | Reload FluxPerMillion.          | fpm.reload |

## PlaceholderAPI

| Placeholder                         | Description                           |
| ----------------------------------- | ------------------------------------- |
| `%fluxpermillion_points%`           | Display the total Flux value.         |
| `%fluxpermillion_status%`           | Display the Flux meter status.        |
| `%fluxpermillion_max_points%`       | Display the maximum Flux meter value. |
| `%fluxpermillion_min_points%`       | Display the minimum Flux meter value. |
| `%fluxpermillion_percentage%`       | Display the Flux meter percentage.    |
| `%fluxpermillion_percentage_color%` | Display the Flux meter color code.    |

## Soft Dependencies

- [Deadly Disasters](https://www.spigotmc.org/resources/deadly-disasters.90806/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## Reporting Issues

- If you encounter any issues with the plugin, please report them [here](https://github.com/PieTw3lve/FluxPerMillion/issues).
