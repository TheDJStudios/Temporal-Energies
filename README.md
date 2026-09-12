# Paradox Utilities

Paradox Utilities is a Fabric 1.20.1 addon for Adventures in Time (AiT). It adds
two TARDIS utility blocks and keeps Create and CC:Tweaked as optional integrations.

## Power Core

Place the Power Core inside a TARDIS interior and connect any Fabric Energy API
compatible cable or machine to any face. It inserts into and extracts directly
from the TARDIS Artron reservoir at **100 FE = 1 Artron Unit (AU)**. It has no
private buffer, respects AiT's fuel capacity, and is inert outside a TARDIS.

With Create installed, a Display Link can select TARDIS energy stored, input
rate, or output rate from the Power Core.

Recipe:

```text
=#=
#C#
=#=

= iron ingot, # redstone, C clock
```

## Binary Core (optional CC:Tweaked integration)

The Binary Core is registered for world/save stability but only appears in the
Paradox Utilities creative tab when CC:Tweaked is installed. Attach a wired or
wireless modem and use it as the `binary_core` peripheral.

Peripheral methods:

- `isConnected()`
- `getTardisInfo()` — overall, flight, refueling, event, energy, and timing data
- `listControls()` and `getControls()`
- `getControl(name)` and `setControl(name, value)`
- `pressControl(name, leftClick?)` — operates every AiT registry control using
  normal AiT permission, power, subsystem, security, and sequence checks

Operating or setting a control requires a player inside the TARDIS. This gives
AiT a real player context for its access checks. Controls without a persistent
value report `"action"` and should be activated with `pressControl`.

With Create installed, the Binary Core provides overall TARDIS status, flight
status/progress, and refueling status Display Link sources. Active AiT flight
sequences are shown as `Flight - <event name>`.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.17.2 or newer
- Fabric API 0.92.6+1.20.1 or newer
- Adventures in Time 1.2.12-release+mc.1.20.1 or newer
- AiT's required dependencies (AmbleKit, YACL, and Multi-Dimension API)
- Fabric Energy API 3.0.0 (bundled)
- Optional: Create Fabric 6.0.8.1 or newer
- Optional: CC:Tweaked 1.120.2 or newer

## Build

Run Gradle with Java 21 or newer. The produced mod is still compiled for Java 17:

```sh
./gradlew build
```

The distributable jar is written to `build/libs/`.
