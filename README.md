# Vast Multiplayer Server

Multiplayer server using ECS implementation through [Artemis ODB](https://github.com/junkdog/artemis-odb) and networking with [Toast Haste](https://github.com/nhn/toast-haste.framework).

## Build

Mac:

`./gradlew fatJar`

Windows:

`gradlew.bat fatJar`

## Run

`java -jar build/libs/vast-server-all.jar -monitor`

The `monitor` argument will enable the terminal monitor where most aspects of the server simulation can be inspected.

### Controls

`Arrow keys` Move camera  (faster if holding `Shift`)

`-` Zoom out (faster if holding `Shift`)

`+` Zoom in (faster if holding `Shift`)

`r` Reset camera position

`p` Focus on next player entity, displaying its components

`f` Focus on nearest entity, displaying its components

`n` Toggle player names

`i` Toggle entity ids

`s` Show system times (sorted by time and name)
