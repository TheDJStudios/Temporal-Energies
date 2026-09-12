package dev.paradoxutilities.compat.computercraft;

import dan200.computercraft.api.peripheral.PeripheralLookup;
import dev.paradoxutilities.ParadoxUtilities;

public final class ComputerCraftCompat {
    private ComputerCraftCompat() {}

    public static void initialize() {
        PeripheralLookup.get().registerForBlockEntity(
                (blockEntity, side) -> new BinaryCorePeripheral(blockEntity),
                ParadoxUtilities.BINARY_CORE_BLOCK_ENTITY);
    }
}
