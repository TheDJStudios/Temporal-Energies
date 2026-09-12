package dev.paradoxutilities.tardis;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.handler.travel.TravelHandlerBase;
import java.util.Locale;

public final class TardisStatus {
    private TardisStatus() {}

    public static String overall(ServerTardis tardis) {
        if (tardis == null) return "Not connected";
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

    public static String flight(ServerTardis tardis) {
        if (tardis == null) return "Not connected";
        if (tardis.travel().isCrashing()) return "Crashing";
        TravelHandlerBase.State state = tardis.travel().getState();
        if (state == TravelHandlerBase.State.FLIGHT) {
            return String.format(Locale.ROOT, "In flight - %d%%", tardis.travel().getDurationAsPercentage());
        }
        return switch (state) {
            case DEMAT -> "Taking off";
            case MAT -> "Landing";
            case LANDED -> "Landed";
            case FLIGHT -> "In flight";
        };
    }

    public static String refueling(ServerTardis tardis) {
        return tardis == null ? "Not connected" : tardis.isRefueling() ? "Refueling" : "Not refueling";
    }

    private static String humanize(String id) {
        String[] words = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
