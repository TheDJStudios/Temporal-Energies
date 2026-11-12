package dev.paradoxutilities.block;

import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.tardis.TardisAccess;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class BinaryCoreBlock extends BlockWithEntity {
    public BinaryCoreBlock(Settings settings) { super(settings); }

    @Override public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Nullable
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BinaryCoreBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof BinaryCoreBlockEntity core)) {
            player.sendMessage(Text.translatable("message.paradox_utilities.not_connected")
                    .formatted(Formatting.RED), true);
            return ActionResult.CONSUME;
        }
        TardisAccess tardis = core.getTardis();
        if (tardis == null) {
            player.sendMessage(Text.translatable("message.paradox_utilities.not_connected")
                    .formatted(Formatting.RED), true);
            return ActionResult.CONSUME;
        }
        player.sendMessage(Text.literal(tardis.overallStatus()).formatted(Formatting.AQUA), true);
        return ActionResult.CONSUME;
    }
}
