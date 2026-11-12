package dev.paradoxutilities.block.entity;

import dev.paradoxutilities.ParadoxUtilities;
import dev.paradoxutilities.tardis.TardisAccess;
import dev.paradoxutilities.tardis.TardisAccessManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class BinaryCoreBlockEntity extends BlockEntity {
    public BinaryCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ParadoxUtilities.BINARY_CORE_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    public TardisAccess getTardis() {
        return world == null ? null : TardisAccessManager.find(world);
    }
}
