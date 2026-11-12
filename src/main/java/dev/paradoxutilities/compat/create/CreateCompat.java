package dev.paradoxutilities.compat.create;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import dev.paradoxutilities.ParadoxUtilities;
import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.block.entity.PowerCoreBlockEntity;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class CreateCompat {
    private CreateCompat() {}

    public static void initialize() {
        registerPower("power_stored", 20,
                core -> formatEnergy(core.energyStorage().getAmount()) + " FE");
        registerPower("power_input", 1,
                core -> formatEnergy(core.getInputRate()) + " FE/t in");
        registerPower("power_output", 1,
                core -> formatEnergy(core.getOutputRate()) + " FE/t out");
        registerBinary("overall_status", core -> core.getTardis() == null
                ? "Not connected" : core.getTardis().overallStatus());
        registerBinary("flight_status", core -> core.getTardis() == null
                ? "Not connected" : core.getTardis().flightStatus());
        registerBinary("refueling_status", core -> core.getTardis() == null
                ? "Not connected" : core.getTardis().refuelingStatus());
    }

    private static void registerPower(String id, int refresh,
                                      Function<PowerCoreBlockEntity, String> value) {
        CoreSource<PowerCoreBlockEntity> source = new CoreSource<>(PowerCoreBlockEntity.class,
                refresh, value);
        Registry.register(CreateBuiltInRegistries.DISPLAY_SOURCE, ParadoxUtilities.id(id), source);
        DisplaySource.BY_BLOCK.add(ParadoxUtilities.POWER_CORE, source);
    }

    private static void registerBinary(String id, Function<BinaryCoreBlockEntity, String> value) {
        CoreSource<BinaryCoreBlockEntity> source = new CoreSource<>(BinaryCoreBlockEntity.class,
                10, value);
        Registry.register(CreateBuiltInRegistries.DISPLAY_SOURCE, ParadoxUtilities.id(id), source);
        DisplaySource.BY_BLOCK.add(ParadoxUtilities.BINARY_CORE, source);
    }

    private static String formatEnergy(long energy) {
        return String.format(Locale.ROOT, "%,d", energy);
    }

    private static final class CoreSource<T extends BlockEntity> extends SingleLineDisplaySource {
        private final Class<T> type;
        private final int refresh;
        private final Function<T, String> value;

        private CoreSource(Class<T> type, int refresh, Function<T, String> value) {
            this.type = type;
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
    }
}
