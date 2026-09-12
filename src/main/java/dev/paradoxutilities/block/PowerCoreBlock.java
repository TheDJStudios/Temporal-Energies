package dev.paradoxutilities.block;

import java.util.Locale;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.paradoxutilities.block.entity.PowerCoreBlockEntity;
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

public final class PowerCoreBlock extends BlockWithEntity {
    public PowerCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PowerCoreBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof PowerCoreBlockEntity core)) return ActionResult.PASS;

        ServerTardis tardis = core.getTardis();
        if (tardis == null) {
            player.sendMessage(Text.translatable("message.paradox_utilities.not_connected")
                    .formatted(Formatting.RED), true);
            return ActionResult.CONSUME;
        }

        player.sendMessage(Text.translatable("message.paradox_utilities.power_connected",
                format(tardis.fuel().getCurrentFuel()),
                format(tardis.fuel().getMaxFuel()),
                core.energyStorage().getAmount(),
                core.energyStorage().getCapacity()).formatted(Formatting.AQUA), true);
        return ActionResult.CONSUME;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }
}
