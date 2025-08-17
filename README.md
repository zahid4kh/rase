# Rase

A simple endless runner game built with Kotlin and Compose for Desktop. Navigate your car through lanes, collect coins, and avoid obstacles while adjusting game settings to your preference.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg?logo=kotlin)](https://kotlinlang.org) [![Compose](https://img.shields.io/badge/Compose-1.8.0-blue.svg?logo=jetpack-compose)](https://www.jetbrains.com/lp/compose-multiplatform/)

## Gameplay

- **Movement**: Use WASD keys to control your car
- **Objective**: Collect yellow coins to increase your score
- **Obstacles**: Avoid red barriers to prevent game over
- **Pause**: Press Escape to pause and access settings

## Features

- Endless scrolling gameplay with dynamic spawning
- Adjustable game settings (car size, car speed, world speed)
- Pause and resume functionality
- Score tracking
- Cross-platform compatibility

## Installation

### JAR (All Platforms)
Download `rase-1.0.0.jar` from [releases](https://github.com/zahid4kh/rase/releases) and run:
```bash
java -jar rase-1.0.0.jar
```

### Linux (Debian/Ubuntu)
Download `rase_1.0.0_all.deb` from [releases](https://github.com/zahid4kh/rase/releases) and install:
```bash
sudo dpkg -i rase_1.0.0_all.deb
```

## Development

### Prerequisites
- JDK 17 or later
- Kotlin 2.1.20 or later

### Running from Source
```bash
./gradlew run
```

### Building
```bash
./gradlew packageDistributionForCurrentOS
```

## Controls

|  Key   | Action     |
|:------:|------------|
|   W    | Move up    |
|   S    | Move down  |
|   A    | Move left  |
|   D    | Move right |
| Escape | Pause/Menu |