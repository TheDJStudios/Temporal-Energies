package dev.temporalenergies.block;

import java.util.Locale;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.temporalenergies.block.entity.PowerTranslatorBlockEntity;
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

public final class PowerTranslatorBlock extends BlockWithEntity {
    public PowerTranslatorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PowerTranslatorBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof PowerTranslatorBlockEntity translator)) {
            return ActionResult.PASS;
        }

        ServerTardis tardis = translator.getTardis();
        if (tardis == null) {
            player.sendMessage(Text.translatable("message.temporalenergies.not_connected")
                    .formatted(Formatting.RED), true);
            return ActionResult.CONSUME;
        }

        player.sendMessage(Text.translatable("message.temporalenergies.connected",
                format(tardis.fuel().getCurrentFuel()),
                format(tardis.fuel().getMaxFuel()),
                translator.energyStorage().getAmount(),
                translator.energyStorage().getCapacity()).formatted(Formatting.AQUA), true);
        return ActionResult.CONSUME;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%,.2f", value);
    }
}
