package dev.paradoxutilities.block.entity;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.world.TardisServerWorld;
import dev.paradoxutilities.ParadoxUtilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class BinaryCoreBlockEntity extends BlockEntity {
    public BinaryCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ParadoxUtilities.BINARY_CORE_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public ServerTardis getTardis() {
        return world instanceof TardisServerWorld tardisWorld ? tardisWorld.getTardis() : null;
    }
}
