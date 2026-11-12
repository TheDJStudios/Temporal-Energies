package dev.paradoxutilities.compat.tardisrefined;

import dev.paradoxutilities.tardis.TardisAccess;
import dev.paradoxutilities.tardis.TardisAccessManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import whocraft.tardis_refined.common.capability.tardis.TardisLevelOperator;
import whocraft.tardis_refined.common.tardis.manager.TardisPilotingManager;

public final class TardisRefinedCompat {
    private TardisRefinedCompat() {}

    public static void initialize() {
        TardisAccessManager.registerOptionalProvider(TardisRefinedCompat::find);
    }

    @Nullable
    private static TardisAccess find(World world) {
        if (!(world instanceof ServerWorld serverWorld)
                || !world.getRegistryKey().getValue().getNamespace().equals("tardis_refined")) {
            return null;
        }
        return TardisLevelOperator.get(serverWorld).map(RefinedAccess::new).orElse(null);
    }

    private static final class RefinedAccess implements TardisAccess {
        private final TardisLevelOperator tardis;

        private RefinedAccess(TardisLevelOperator tardis) {
            this.tardis = tardis;
        }

        private TardisPilotingManager piloting() {
            return tardis.getPilotingManager();
        }

        @Override
        public String provider() {
            return "tardis_refined";
        }

        @Override
        public String name() {
            String id = tardis.getLevelKey().getValue().getPath();
            return "TARDIS Refined " + id.substring(0, Math.min(5, id.length()));
        }

        @Override
        public double fuel() {
            return piloting().getFuel();
        }

        @Override
        public double maximumFuel() {
            return piloting().getMaximumFuel();
        }

        @Override
        public void setFuel(double fuel) {
            piloting().setFuel(fuel);
        }

        @Override
        public String overallStatus() {
            if (piloting().isCrashing()) return "Crashing";
            if (piloting().isTakingOff()) return "Taking off";
            if (piloting().isLanding()) return "Landing";
            if (piloting().isInFlight()) return "In flight";
            return piloting().isPassivelyRefuelling() ? "Refueling" : "Idle";
        }

        @Override
        public String flightStatus() {
            if (piloting().isCrashing()) return "Crashing";
            if (piloting().isTakingOff()) return "Taking off";
            if (piloting().isLanding()) return "Landing";
            return piloting().isInFlight() ? "In flight - " + flightProgress() + "%" : "Landed";
        }

        @Override
        public boolean refueling() {
            return piloting().isPassivelyRefuelling();
        }

        @Override
        public boolean crashing() {
            return piloting().isCrashing();
        }

        @Override
        public int flightProgress() {
            return Math.round(piloting().getFlightPercentageCovered() * 100.0F);
        }
    }
}
