package dev.paradoxutilities.compat.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.tardis.TardisFuelStorage;
import dev.paradoxutilities.tardis.TardisAccess;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class BinaryCorePeripheral implements IPeripheral {
    private final BinaryCoreBlockEntity core;

    public BinaryCorePeripheral(BinaryCoreBlockEntity core) { this.core = core; }

    @Override public String getType() { return "binary_core"; }
    @Override public Object getTarget() { return core; }
    @Override public boolean equals(IPeripheral other) {
        return other instanceof BinaryCorePeripheral peripheral && peripheral.core == core;
    }

    @LuaFunction(mainThread = true)
    public boolean isConnected() { return core.getTardis() != null; }

    @LuaFunction(mainThread = true)
    public Map<String, Object> getTardisInfo() throws LuaException {
        TardisAccess tardis = requireTardisAccess();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", tardis.provider());
        result.put("name", tardis.name());
        result.put("status", tardis.overallStatus());
        result.put("flightStatus", tardis.flightStatus());
        result.put("refuelingStatus", tardis.refuelingStatus());
        result.put("refueling", tardis.refueling());
        result.put("crashing", tardis.crashing());
        result.put("flightProgress", tardis.flightProgress());
        result.put("flightTicks", tardis.flightTicks());
        result.put("targetFlightTicks", tardis.targetFlightTicks());
        result.put("fuel", tardis.fuel());
        result.put("fuelCapacity", tardis.maximumFuel());
        result.put("energy", TardisFuelStorage.toEnergy(tardis.fuel()));
        result.put("energyCapacity", TardisFuelStorage.toEnergy(tardis.maximumFuel()));
        result.put("artron", tardis.fuel());
        result.put("artronCapacity", tardis.maximumFuel());
        result.put("flightEvent", tardis.flightEvent());
        return result;
    }

    @LuaFunction(mainThread = true)
    public List<String> listControls() {
        TardisAccess tardis = core.getTardis();
        return tardis == null ? List.of() : tardis.listControls();
    }

    @LuaFunction(mainThread = true)
    public Map<String, Object> getControls() throws LuaException {
        TardisAccess tardis = requireTardisAccess();
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String control : tardis.listControls()) values.put(control, tardis.getControl(control));
        } catch (TardisAccess.ControlException exception) {
            throw new LuaException(exception.getMessage());
        }
        return values;
    }

    @LuaFunction(mainThread = true)
    public Object getControl(String name) throws LuaException {
        TardisAccess tardis = requireControlTardis();
        try {
            return tardis.getControl(name);
        } catch (TardisAccess.ControlException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    @LuaFunction(mainThread = true)
    public String pressControl(IArguments arguments) throws LuaException {
        String name = arguments.getString(0);
        boolean leftClick = arguments.optBoolean(1, false);
        TardisAccess tardis = requireControlTardis();
        try {
            return tardis.pressControl(name, leftClick, requireControlContext());
        } catch (TardisAccess.ControlException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    @LuaFunction(mainThread = true)
    public Object setControl(IArguments arguments) throws LuaException {
        String name = arguments.getString(0);
        TardisAccess tardis = requireControlTardis();
        try {
            return tardis.setControl(name, arguments.get(1), requireControlContext());
        } catch (TardisAccess.ControlException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    private TardisAccess requireTardisAccess() throws LuaException {
        TardisAccess tardis = core.getTardis();
        if (tardis == null) throw new LuaException("Binary Core is not inside a supported TARDIS");
        return tardis;
    }

    private ServerPlayerEntity requirePlayer() throws LuaException {
        if (!(core.getWorld() instanceof ServerWorld world)) throw new LuaException("Binary Core is not server-side");
        PlayerEntity nearest = world.getClosestPlayer(core.getPos().getX() + 0.5, core.getPos().getY() + 0.5,
                core.getPos().getZ() + 0.5, -1, false);
        if (!(nearest instanceof ServerPlayerEntity serverPlayer)) {
            throw new LuaException("A player must be inside the TARDIS to operate console controls");
        }
        return serverPlayer;
    }

    private TardisAccess requireControlTardis() throws LuaException {
        TardisAccess tardis = requireTardisAccess();
        if (!tardis.supportsControls()) {
            throw new LuaException("Direct console controls are not available for " + tardis.provider());
        }
        return tardis;
    }

    private TardisAccess.ControlContext requireControlContext() throws LuaException {
        ServerPlayerEntity player = requirePlayer();
        return new TardisAccess.ControlContext(player, (ServerWorld) core.getWorld(), core.getPos());
    }
}
