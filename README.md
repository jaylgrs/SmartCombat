# SmartCombat

SmartCombat is a Minecraft Paper plugin focused on combat logging prevention and fair PvP gameplay.

## Features

- **Combat Logging Protection** — Prevent players from escaping combat by disconnecting.
- **Anti Same-IP** — Detect and prevent combat abuse involving players connected from the same IP address.
- **Anti Spam Kill** — Prevent repeated kills from being exploited for unfair advantages.
- **Combat Cooldown ActionBar** — Display the remaining combat cooldown in the player's action bar.

## Requirements

- Java 25
- Minecraft Paper server
- A compatible Paper API version

## Development

SmartCombat is developed using Java and Gradle in Visual Studio Code.

### Build

Run the following command in the project directory using PowerShell:

```powershell
.\gradlew.bat build
```

The compiled plugin JAR will be available in:

```text
build/libs/
```

## Installation

1. Build the plugin.
2. Locate the generated JAR file inside `build/libs/`.
3. Place the JAR file in your Paper server's `plugins` folder.
4. Start or restart the server.

## Project Status

SmartCombat is currently under development. Features and configurations may change as development progresses.

## License

See the `LICENSE` file for licensing information.