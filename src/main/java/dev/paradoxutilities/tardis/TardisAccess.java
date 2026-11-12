package dev.paradoxutilities.tardis;

import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Loader-safe view of the TARDIS data used by Paradox Utilities blocks. */
public interface TardisAccess {
    String provider();

    String name();

    double fuel();

    double maximumFuel();

    void setFuel(double fuel);

    String overallStatus();

    String flightStatus();

    boolean refueling();

    boolean crashing();

    int flightProgress();

    default long flightTicks() {
        return 0L;
    }

    default long targetFlightTicks() {
        return 0L;
    }

    default String flightEvent() {
        return "";
    }

    default String refuelingStatus() {
        return refueling() ? "Refueling" : "Not refueling";
    }

    default boolean supportsControls() {
        return false;
    }

    default List<String> listControls() {
        return List.of();
    }

    default Object getControl(String name) throws ControlException {
        throw new ControlException("Direct console controls are not available for " + provider());
    }

    default Object setControl(String name, Object value, ControlContext context) throws ControlException {
        throw new ControlException("Direct console controls are not available for " + provider());
    }

    default String pressControl(String name, boolean leftClick, ControlContext context) throws ControlException {
        throw new ControlException("Direct console controls are not available for " + provider());
    }

    record ControlContext(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {}

    final class ControlException extends Exception {
        public ControlException(String message) {
            super(message);
        }
    }
}
