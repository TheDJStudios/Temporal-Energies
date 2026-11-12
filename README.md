# Paradox Utilities

Paradox Utilities is a Fabric 1.20.1 TARDIS utility addon. Its two utility
blocks support Adventures in Time (AiT) and TARDIS Refined, while Create and
CC:Tweaked integrations are optional.

## Power Core

Place the Power Core inside an AiT or TARDIS Refined interior and connect any
Fabric Energy API compatible cable or machine to any face. It inserts into and
extracts directly from the TARDIS fuel reservoir at **100 FE = 1 TARDIS fuel
unit**. It has no private buffer, respects the active TARDIS's fuel capacity,
and is inert outside a supported TARDIS.

With Create installed, a Display Link can select TARDIS energy stored, input
rate, or output rate from the Power Core.

## AiT Sonic Screwdriver charging

When AiT is installed, its sonic screwdriver exposes a standard Fabric Energy
item storage. Any compatible charger can fill it and any compatible discharger
can drain it. The conversion is the same as the Power Core (**100 FE = 1 AU**),
and the reported capacity follows AiT's own sonic fuel limit.

Recipe:

```text
=#=
#C#
=#=

= iron ingot, # redstone, C clock
```

## Binary Core (optional CC:Tweaked integration)

The Binary Core is registered for world/save stability but only appears in the
Paradox Utilities creative tab when CC:Tweaked is installed. Attach a wired modem and use it as the `binary_core` peripheral.

Peripheral methods:

- `isConnected()`
- `getTardisInfo()` — provider, overall, flight, refueling, event, energy, and timing data
- `listControls()` and `getControls()`
- `getControl(name)` and `setControl(name, value)`
- `pressControl(name, leftClick?)` — operates every AiT registry control using
  normal AiT permission, power, subsystem, security, and sequence checks

Operating or setting a control requires a player inside the TARDIS. This gives
AiT a real player context for its access checks. Controls without a persistent
value report `"action"` and should be activated with `pressControl`.
Boolean `setControl` calls run the actual AiT control and report an error when
AiT refuses the requested state. Exact destination coordinates and direction
accept finite integers; coordinates must be within the destination world's bounds.
Use `pressControl` for throttle changes and during active flight sequences.

When TARDIS Refined is installed, the Binary Core reports its status and fuel
data through CC:Tweaked and Create. Direct console-control methods remain AiT-only.

With Create installed, the Binary Core provides overall TARDIS status, flight
status/progress, and refueling status Display Link sources. Active AiT flight
sequences are shown as `Flight - <event name>`.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.17.2 or newer
- Fabric API 0.92.6+1.20.1 or newer
- Fabric Energy API 3.0.0 (bundled)
- At least one supported TARDIS mod:
  - Adventures in Time 1.2.12-release+mc.1.20.1 (targeted version) (plus its required dependencies)
  - TARDIS Refined 2.1.7 (Fabric, targeted version)
- Optional: Create Fabric 6.0.8.1 or newer
- Optional: CC:Tweaked 1.120.2 or newer

## Build

Run Gradle with Java 21 or newer. The produced mod is still compiled for Java 17:

```sh
./gradlew build
```

The distributable jar is written to `build/libs/`.

`./gradlew check` also runs fuel-transaction regression checks. In-game checks
are still required for cables/chargers, console controls, flight, and save/reload.
A successful compile does not establish compatibility with newer TARDIS versions.
See [COMPATIBILITY.md](COMPATIBILITY.md) for check coverage and the in-game checklist.

## Disclaimers and disclosures
This mod was made with help from OpenAI Codex (Generative AI)
