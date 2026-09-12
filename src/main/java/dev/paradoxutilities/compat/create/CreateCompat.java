package dev.paradoxutilities.compat.create;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.paradoxutilities.ParadoxUtilities;
import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.block.entity.PowerCoreBlockEntity;
import dev.paradoxutilities.tardis.TardisStatus;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class CreateCompat {
    private CreateCompat() {}

    public static void initialize() {
        registerPower("power_stored", "display_source.paradox_utilities.power_stored", 20,
                core -> formatEnergy(core.energyStorage().getAmount()) + " FE");
        registerPower("power_input", "display_source.paradox_utilities.power_input", 1,
                core -> formatEnergy(core.getInputRate()) + " FE/t in");
        registerPower("power_output", "display_source.paradox_utilities.power_output", 1,
                core -> formatEnergy(core.getOutputRate()) + " FE/t out");
        registerBinary("overall_status", "display_source.paradox_utilities.overall_status",
                core -> TardisStatus.overall(core.getTardis()));
        registerBinary("flight_status", "display_source.paradox_utilities.flight_status",
                core -> TardisStatus.flight(core.getTardis()));
        registerBinary("refueling_status", "display_source.paradox_utilities.refueling_status",
                core -> TardisStatus.refueling(core.getTardis()));
    }

    private static void registerPower(String id, String translationKey, int refresh,
                                      Function<PowerCoreBlockEntity, String> value) {
        CoreSource<PowerCoreBlockEntity> source = new CoreSource<>(PowerCoreBlockEntity.class,
                translationKey, refresh, value);
        Registry.register(CreateBuiltInRegistries.DISPLAY_SOURCE, ParadoxUtilities.id(id), source);
        DisplaySource.BY_BLOCK_ENTITY.add(ParadoxUtilities.POWER_CORE_BLOCK_ENTITY, source);
    }

    private static void registerBinary(String id, String translationKey,
                                       Function<BinaryCoreBlockEntity, String> value) {
        CoreSource<BinaryCoreBlockEntity> source = new CoreSource<>(BinaryCoreBlockEntity.class,
                translationKey, 10, value);
        Registry.register(CreateBuiltInRegistries.DISPLAY_SOURCE, ParadoxUtilities.id(id), source);
        DisplaySource.BY_BLOCK_ENTITY.add(ParadoxUtilities.BINARY_CORE_BLOCK_ENTITY, source);
    }

    private static String formatEnergy(long energy) {
        return String.format(Locale.ROOT, "%,d", energy);
    }

    private static final class CoreSource<T extends BlockEntity> extends SingleLineDisplaySource {
        private final Class<T> type;
        private final String translationKey;
        private final int refresh;
        private final Function<T, String> value;

        private CoreSource(Class<T> type, String translationKey, int refresh, Function<T, String> value) {
            this.type = type;
            this.translationKey = translationKey;
            this.refresh = refresh;
            this.value = value;
        }

        @Override
        protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            BlockEntity blockEntity = context.getSourceBlockEntity();
            return type.isInstance(blockEntity) ? Text.literal(value.apply(type.cast(blockEntity))) : Text.empty();
        }

        @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
        @Override public int getPassiveRefreshTicks() { return refresh; }
        @Override protected String getTranslationKey() { return translationKey; }
    }
}
