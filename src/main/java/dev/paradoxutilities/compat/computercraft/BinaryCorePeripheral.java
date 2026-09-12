package dev.paradoxutilities.compat.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.registry.impl.ControlRegistry;
import dev.paradoxutilities.block.entity.BinaryCoreBlockEntity;
import dev.paradoxutilities.block.entity.PowerCoreBlockEntity;
import dev.paradoxutilities.tardis.TardisStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
        ServerTardis tardis = requireTardis();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", tardis.stats().getName());
        result.put("status", TardisStatus.overall(tardis));
        result.put("flightStatus", TardisStatus.flight(tardis));
        result.put("refuelingStatus", TardisStatus.refueling(tardis));
        result.put("refueling", tardis.isRefueling());
        result.put("crashing", tardis.travel().isCrashing());
        result.put("flightProgress", tardis.travel().getDurationAsPercentage());
        result.put("flightTicks", tardis.travel().getFlightTicks());
        result.put("targetFlightTicks", tardis.travel().getTargetTicks());
        result.put("energy", (long) Math.floor(tardis.fuel().getCurrentFuel()
                * PowerCoreBlockEntity.ENERGY_PER_ARTRON));
        result.put("energyCapacity", (long) Math.floor(tardis.fuel().getMaxFuel()
                * PowerCoreBlockEntity.ENERGY_PER_ARTRON));
        result.put("artron", tardis.fuel().getCurrentFuel());
        result.put("artronCapacity", tardis.fuel().getMaxFuel());
        result.put("flightEvent", tardis.sequence().hasActiveSequence()
                ? tardis.sequence().getActiveSequence().id().toString() : "");
        return result;
    }

    @LuaFunction(mainThread = true)
    public List<String> listControls() {
        return ControlRegistry.REGISTRY.stream().map(control -> control.id().toString()).sorted().toList();
    }

    @LuaFunction(mainThread = true)
    public Map<String, Object> getControls() throws LuaException {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String control : listControls()) values.put(control, readValue(requireTardis(), path(control)));
        return values;
    }

    @LuaFunction(mainThread = true)
    public Object getControl(String name) throws LuaException {
        requireControl(name);
        return readValue(requireTardis(), path(name));
    }

    @LuaFunction(mainThread = true)
    public String pressControl(IArguments arguments) throws LuaException {
        String name = arguments.getString(0);
        boolean leftClick = arguments.optBoolean(1, false);
        ServerTardis tardis = requireTardis();
        ServerPlayerEntity player = requirePlayer();
        Control control = requireControl(name);
        if (!control.canRun(tardis, player)) throw new LuaException("The TARDIS rejected that control");
        Control.Result result = control.handleRun(tardis, player, (ServerWorld) core.getWorld(), core.getPos(), leftClick);
        return result.name().toLowerCase(Locale.ROOT);
    }

    @LuaFunction(mainThread = true)
    public Object setControl(IArguments arguments) throws LuaException {
        String name = arguments.getString(0);
        String key = path(name);
        ServerTardis tardis = requireTardis();
        ServerPlayerEntity player = requirePlayer();
        Control control = requireControl(name);
        if (!control.canRun(tardis, player)) throw new LuaException("The TARDIS rejected that control");
        int integerValue = switch (key) {
            case "direction", "throttle", "x", "y", "z" -> arguments.getInt(1);
            default -> 0;
        };

        switch (key) {
            case "antigravs" -> tardis.travel().antigravs().set(arguments.getBoolean(1));
            case "protocol_116" -> tardis.travel().autopilot(arguments.getBoolean(1));
            case "protocol_3" -> tardis.cloak().cloaked().set(arguments.getBoolean(1));
            case "direction" -> tardis.travel().forceDestination(destination ->
                    destination.rotation((byte) Math.floorMod(integerValue, 16)));
            case "door_control" -> { if (arguments.getBoolean(1)) tardis.door().openDoors(); else tardis.door().closeDoors(); }
            case "door_lock" -> tardis.door().setLocked(arguments.getBoolean(1));
            case "alarms" -> { if (arguments.getBoolean(1)) tardis.alarm().enable(); else tardis.alarm().disable(); }
            case "protocol_813" -> tardis.stats().hailMary().set(arguments.getBoolean(1));
            case "handbrake" -> tardis.travel().handbrake(arguments.getBoolean(1));
            case "power" -> { if (arguments.getBoolean(1)) tardis.fuel().enablePower(); else tardis.fuel().disablePower(); }
            case "refueler" -> tardis.setRefueling(arguments.getBoolean(1));
            case "protocol_19" -> tardis.stats().security().set(arguments.getBoolean(1));
            case "shields" -> { if (arguments.getBoolean(1)) tardis.shields().enable(); else tardis.shields().disableAll(); }
            case "protocol_1913" -> tardis.siege().setActive(arguments.getBoolean(1));
            case "throttle" -> tardis.travel().speed(integerValue);
            case "x" -> tardis.travel().forceDestination(destination -> destination.pos(
                    integerValue, destination.getPos().getY(), destination.getPos().getZ()));
            case "y" -> tardis.travel().forceDestination(destination -> destination.pos(
                    destination.getPos().getX(), integerValue, destination.getPos().getZ()));
            case "z" -> tardis.travel().forceDestination(destination -> destination.pos(
                    destination.getPos().getX(), destination.getPos().getY(), integerValue));
            default -> throw new LuaException("Control '" + name + "' is an action; use pressControl instead");
        }
        return readValue(tardis, key);
    }

    private Object readValue(ServerTardis tardis, String key) {
        BlockPos destination = tardis.travel().destination().getPos();
        return switch (key) {
            case "antigravs" -> tardis.travel().antigravs().get();
            case "protocol_116" -> tardis.travel().autopilot();
            case "protocol_3" -> tardis.cloak().cloaked().get();
            case "direction" -> (int) tardis.travel().destination().getRotation();
            case "door_control" -> tardis.door().isOpen();
            case "door_lock" -> tardis.door().locked();
            case "alarms" -> tardis.alarm().isEnabled();
            case "protocol_813" -> tardis.stats().hailMary().get();
            case "handbrake" -> tardis.travel().handbrake();
            case "power" -> tardis.fuel().hasPower();
            case "refueler" -> tardis.isRefueling();
            case "protocol_19" -> tardis.stats().security().get();
            case "shields" -> tardis.shields().shielded().get();
            case "protocol_1913" -> tardis.siege().isActive();
            case "throttle" -> tardis.travel().speed();
            case "x" -> destination.getX();
            case "y" -> destination.getY();
            case "z" -> destination.getZ();
            default -> "action";
        };
    }

    private ServerTardis requireTardis() throws LuaException {
        ServerTardis tardis = core.getTardis();
        if (tardis == null) throw new LuaException("Binary Core is not inside a TARDIS");
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

    private Control requireControl(String name) throws LuaException {
        Identifier id = Identifier.tryParse(name.contains(":") ? name : "ait:" + name);
        if (id == null) throw new LuaException("Invalid control id: " + name);
        return ControlRegistry.fromId(id).orElseThrow(() -> new LuaException("Unknown control: " + name));
    }

    private static String path(String name) {
        int separator = name.indexOf(':');
        return separator < 0 ? name : name.substring(separator + 1);
    }
}
