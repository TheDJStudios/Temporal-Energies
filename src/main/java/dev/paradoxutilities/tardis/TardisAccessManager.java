package dev.paradoxutilities.tardis;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class TardisAccessManager {
    private static final List<Function<World, TardisAccess>> OPTIONAL_PROVIDERS = new ArrayList<>();

    private static final Map<World, TardisFuelStorage> FUEL_STORAGES = new WeakHashMap<>();

    private TardisAccessManager() {}

    public static TardisFuelStorage energyStorage(World world) {
        return FUEL_STORAGES.computeIfAbsent(world, key -> {
            WeakReference<World> reference = new WeakReference<>(key);
            return new TardisFuelStorage(() -> {
                World current = reference.get();
                return current == null ? null : find(current);
            });
        });
    }

    public static void registerOptionalProvider(Function<World, TardisAccess> provider) {
        OPTIONAL_PROVIDERS.add(Objects.requireNonNull(provider));
    }

    @Nullable
    public static TardisAccess find(World world) {
        if (world.isClient()) return null;
        for (Function<World, TardisAccess> provider : OPTIONAL_PROVIDERS) {
            TardisAccess access = provider.apply(world);
            if (access != null) return access;
        }
        return null;
    }

}
