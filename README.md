# Anti-Clean

A Paper **1.21.11** plugin that protects kill loot from being cleaned up or stolen.

## How it works

- When a player kills another player, the victim's entire inventory (including armor and offhand) is stored in a **double chest** placed near the death location instead of dropping on the ground.
- For **45 seconds** (configurable), only the killer can open that chest.
- During those 45 seconds, other players **cannot damage the killer** — their hits do nothing.
- If the killer **attacks someone** during the protection window, their protection is **cancelled**: the chest becomes public and they can be attacked again.
- While protected, the chest cannot be looted by hoppers, broken by other players, or destroyed by explosions.

## Installation

1. Download the latest `.jar` from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart the server.

## Configuration

`plugins/AntiClean/config.yml`:

```yaml
protection-duration: 45
```

## Requirements

- Paper 1.21.11 (or compatible)
- Java 21+

## License

All rights reserved. See LICENSE if provided.
