package net.fabricmc.example.waypoints;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.fabricmc.example.waypoints.Util.bearingDeg;
import static net.fabricmc.example.waypoints.Util.dirArrow;
import static net.fabricmc.example.waypoints.Util.distanceMeters;

public final class CompassTracker {
    private static MinecraftServer SERVER;
    private static int tickCounter = 0;

    private CompassTracker() {}

    public static void init(MinecraftServer server) {
        SERVER = server;
    }

    public static void onServerTick() {
        if (SERVER == null) return;
        tickCounter++;
        if (tickCounter % 20 != 0) return; // every ~1s

        for (ServerPlayerEntity p : SERVER.getPlayerManager().getPlayerList()) {
            var wp = WaypointStore.ACTIVE.get(p.getUuid());
            if (wp == null) continue;

            String playerDim = p.getEntityWorld().getRegistryKey().getValue().toString();
            if (!playerDim.equals(wp.dimension())) {
                p.sendMessage(Text.literal("§7[wp] §fTarget in different dimension: §e" + wp.dimension()), true);
                continue;
            }

            int dist = distanceMeters(p.getBlockPos(), wp.pos());
            double bearing = bearingDeg(p, wp.pos());
            String arrow = dirArrow(bearing);
            String name = (wp.name() == null || wp.name().isEmpty()) ? "target" : wp.name();

            p.sendMessage(Text.literal(arrow + " " + dist + "m · \"" + name + "\""), true);
        }
    }
}
