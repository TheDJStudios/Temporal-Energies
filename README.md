# TemporalEnergies

TemporalEnergies is a server-and-client Fabric 1.20.1 addon for
[Adventures in Time](https://modrinth.com/mod/ait). It adds a Power Translator
block that exposes the owning TARDIS's Artron reservoir through the Fabric
Energy API.

## Using the Power Translator

1. Place the block anywhere inside a TARDIS interior.
2. Connect an Energy API-compatible cable or machine to any face.
3. Energy can be inserted into or extracted from the TARDIS automatically.
4. Right-click the block to see its connection state and current charge.

The conversion ratio is **100 Energy = 1 Artron Unit (AU)**. AiT's 50,000 AU
capacity therefore appears to energy networks as 5,000,000 Energy. The block
has no independent buffer: transactions operate directly on the TARDIS's main
fuel handler and respect AiT's fuel limits. Outside an AiT TARDIS dimension the
block is inert.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.16.10 or newer
- Fabric API 0.92.3+1.20.1
- Adventures in Time 1.2.12-release+mc.1.20.1 or newer
- Fabric Energy API 3.0.0 (bundled in this mod's jar)
- AiT's own required dependencies (AmbleKit and YACL)

## Build

Use Java 17 or newer:

## Disclaimers and disclosures
This mod was made with help from OpenAI Codex (Generative AI)

```sh
./gradlew build
```

The distributable jar is written to `build/libs/`.
