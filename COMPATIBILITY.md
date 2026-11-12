# Compatibility checks — September 30, 2026

Target: Minecraft 1.20.1, Fabric Loader 0.17.2, Fabric API 0.92.6,
AiT 1.2.12-release+mc.1.20.1, and TARDIS Refined 2.1.7 (Fabric).

## Automated checks

`./gradlew build --offline` compiles Java 17 bytecode and produces the remapped
jar. `check` includes `fuelRegressionChecks`, using Fabric's actual transaction
implementation and a fake TARDIS reservoir. It covers aborted simulations,
nested commit/abort, capacity/depletion, net-zero transfers, fractional AU,
repeated single-FE transfers at AiT capacity, and disconnected storage.

The fuel participant is shared across Power Cores in the same interior. Fuel
setters run only after the outer transaction commits, preventing simulated
emptying from consuming AiT emergency power or triggering Refined fuel-loss
side effects. Sonic charging preserves existing fractional fuel.

AiT boolean setters invoke its normal controls. Throttle changes use
`pressControl`; direct setters cannot bypass throttle-specific rules. Exact
coordinates validate finite integers and destination-world bounds. Direct
setters reject active flight sequences; use `pressControl` for those sequences.

## Startup checks

Dedicated-server initialization is checked with both TARDIS mods plus Create
6.0.8.1 and CC:Tweaked 1.120.2, and separately with no optional mods, AiT only,
and TARDIS Refined only. These checks stop at `eula=false`: they establish
loader/initializer compatibility, not working gameplay or world persistence.
Upstream mixin and data-fixer warnings may still appear with the optional mods.

## In-game checks still required

Use a disposable test world with the targeted mod versions:

1. Place a Power Core in each mod's interior. Check fuel and FE readings,
   insert/extract with a compatible cable, and verify the 100 FE/unit ratio.
2. Simulate/cancel extraction at empty and nearly empty fuel. Check that power,
   emergency fuel, and flight are unchanged. Repeat with two Power Cores.
3. Charge/discharge an AiT sonic, including one-FE transfers near full/empty.
4. Read Binary Core status through a wired CC modem. Test allowed/refused
   controls, refueling during flight, siege during flight, invalid coordinates,
   and flight-sequence controls. Refined direct controls are unsupported.
5. Verify Create Display Links and save/reload fuel in both interiors.
6. Place cores outside an interior: no TARDIS connection or energy transfer.

Compatibility with newer releases and client rendering has not been verified.
