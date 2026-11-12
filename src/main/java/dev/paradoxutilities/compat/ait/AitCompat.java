package dev.paradoxutilities.compat.ait;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.control.Control;
import dev.amble.ait.core.tardis.handler.travel.TravelHandlerBase;
import dev.amble.ait.core.world.TardisServerWorld;
import dev.amble.ait.registry.impl.ControlRegistry;
import dev.paradoxutilities.tardis.TardisAccess;
import dev.paradoxutilities.tardis.TardisAccessManager;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class AitCompat {
    private AitCompat() {}

    public static void initialize() {
        TardisAccessManager.registerOptionalProvider(AitCompat::find);
        SonicEnergyCompat.initialize();
    }

    @Nullable
    private static TardisAccess find(World world) {
        if (!(world instanceof TardisServerWorld tardisWorld)) return null;
        ServerTardis tardis = tardisWorld.getTardis();
        return tardis == null ? null : new AitAccess(tardis);
    }

    private static final class AitAccess implements TardisAccess {
        private final ServerTardis tardis;

        private AitAccess(ServerTardis tardis) {
            this.tardis = tardis;
        }

        @Override public String provider() { return "ait"; }
        @Override public String name() { return tardis.stats().getName(); }
        @Override public double fuel() { return tardis.fuel().getCurrentFuel(); }
        @Override public double maximumFuel() { return tardis.fuel().getMaxFuel(); }
        @Override public void setFuel(double fuel) { tardis.fuel().setCurrentFuel(fuel); }
        @Override public boolean refueling() { return tardis.isRefueling(); }
        @Override public boolean crashing() { return tardis.travel().isCrashing(); }
        @Override public int flightProgress() { return tardis.travel().getDurationAsPercentage(); }
        @Override public long flightTicks() { return tardis.travel().getFlightTicks(); }
        @Override public long targetFlightTicks() { return tardis.travel().getTargetTicks(); }

        @Override
        public String flightEvent() {
            return tardis.sequence().hasActiveSequence()
                    ? tardis.sequence().getActiveSequence().id().toString()
                    : "";
        }

        @Override
        public String overallStatus() {
            if (tardis.travel().isCrashing()) return "Crashing";
            return switch (tardis.travel().getState()) {
                case DEMAT -> "Taking off";
                case MAT -> "Landing";
                case FLIGHT -> tardis.sequence().hasActiveSequence()
                        ? "Flight - " + humanize(tardis.sequence().getActiveSequence().id().getPath())
                        : "In flight";
                case LANDED -> tardis.isRefueling() ? "Refueling" : "Idle";
            };
        }

        @Override
        public String flightStatus() {
            if (tardis.travel().isCrashing()) return "Crashing";
            TravelHandlerBase.State state = tardis.travel().getState();
            if (state == TravelHandlerBase.State.FLIGHT) {
                return String.format(Locale.ROOT, "In flight - %d%%", flightProgress());
            }
            return switch (state) {
                case DEMAT -> "Taking off";
                case MAT -> "Landing";
                case LANDED -> "Landed";
                case FLIGHT -> "In flight";
            };
        }

        @Override public boolean supportsControls() { return true; }

        @Override
        public List<String> listControls() {
            return ControlRegistry.REGISTRY.stream().map(control -> control.id().toString()).sorted().toList();
        }

        @Override
        public Object getControl(String name) throws ControlException {
            Control control = requireControl(name);
            return control.id().getNamespace().equals("ait") ? readValue(path(name)) : "action";
        }

        @Override
        public String pressControl(String name, boolean leftClick, ControlContext context) throws ControlException {
            Control control = requireControl(name);
            if (!control.canRun(tardis, context.player())) {
                throw new ControlException("The TARDIS rejected that control");
            }
            Control.Result result = control.handleRun(tardis, context.player(), context.world(), context.pos(), leftClick);
            return result.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public Object setControl(String name, Object value, ControlContext context) throws ControlException {
            String key = path(name);
            Control control = requireControl(name);
            if (!control.id().getNamespace().equals("ait")) {
                throw new ControlException("Use pressControl for addon controls");
            }
            if (!control.canRun(tardis, context.player())) {
                throw new ControlException("The TARDIS rejected that control");
            }
            if (tardis.sequence().hasActiveSequence()) {
                throw new ControlException("Use pressControl during an active flight sequence");
            }
            Object current = readValue(key);
            if (current instanceof Boolean) {
                boolean requested = bool(value, name);
                if (!current.equals(requested)) {
                    // Run the real control so flight-state restrictions and side effects are preserved.
                    String result = pressControl(name, false, context);
                    if (result.equals("failure") || result.equals("sequence")) {
                        throw new ControlException("The TARDIS rejected that control change");
                    }
                }
                Object actual = readValue(key);
                if (!actual.equals(requested)) {
                    throw new ControlException("The TARDIS did not accept the requested value");
                }
                return actual;
            }
            if (!key.equals("x") && !key.equals("y") && !key.equals("z") && !key.equals("direction")) {
                throw new ControlException("Use pressControl to adjust '" + name + "'");
            }
            int integerValue = integer(value, name);
            if (key.equals("direction")) {
                if (integerValue < 0 || integerValue > 15) {
                    throw new ControlException("Direction must be between 0 and 15");
                }
                tardis.travel().forceDestination(destination -> destination.rotation((byte) integerValue));
            } else {
                var destination = tardis.travel().destination();
                var targetWorld = destination.getWorld();
                BlockPos old = destination.getPos();
                BlockPos target = switch (key) {
                    case "x" -> new BlockPos(integerValue, old.getY(), old.getZ());
                    case "y" -> new BlockPos(old.getX(), integerValue, old.getZ());
                    default -> new BlockPos(old.getX(), old.getY(), integerValue);
                };
                if (targetWorld == null || !targetWorld.getWorldBorder().contains(target)
                        || target.getY() < targetWorld.getBottomY() || target.getY() >= targetWorld.getTopY()) {
                    throw new ControlException("Destination is outside the target world's bounds");
                }
                // Use AiT's destination setter, which also checks the world border.
                tardis.travel().destination(destination.pos(target));
            }
            return readValue(key);
        }

        private Object readValue(String key) {
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

        private static Control requireControl(String name) throws ControlException {
            Identifier id = Identifier.tryParse(name.contains(":") ? name : "ait:" + name);
            if (id == null) throw new ControlException("Invalid control id: " + name);
            return ControlRegistry.fromId(id).orElseThrow(() -> new ControlException("Unknown control: " + name));
        }
    }

    private static int integer(Object value, String name) throws TardisAccess.ControlException {
        if (value instanceof Number number) {
            double numeric = number.doubleValue();
            if (Double.isFinite(numeric) && numeric == Math.rint(numeric)
                    && numeric >= Integer.MIN_VALUE && numeric <= Integer.MAX_VALUE) {
                return (int) numeric;
            }
        }
        throw new TardisAccess.ControlException("Control '" + name + "' requires a finite 32-bit integer");
    }

    private static boolean bool(Object value, String name) throws TardisAccess.ControlException {
        if (value instanceof Boolean bool) return bool;
        throw new TardisAccess.ControlException("Control '" + name + "' requires a boolean");
    }

    private static String path(String name) {
        int separator = name.indexOf(':');
        return separator < 0 ? name : name.substring(separator + 1);
    }

    private static String humanize(String id) {
        String[] words = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
