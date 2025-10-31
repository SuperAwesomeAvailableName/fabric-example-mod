package net.fabricmc.example.waypoints;

import net.fabricmc.example.waypoints.Util.Vec3d;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CompassTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompassTracker.class);
    private static MinecraftServer SERVER;
    private static int tickCounter = 0;

    private CompassTracker() {}

    public static void init(MinecraftServer server) {
        SERVER = server;
    }

    public static void onServerTick() {
        try {
            if (SERVER == null) return;
            tickCounter++;
            if (tickCounter % 20 != 0) return; // every ~1s

            for (ServerPlayerEntity p : SERVER.getPlayerManager().getPlayerList()) {
                try {
                    updatePlayerCompass(p);
                } catch (Exception e) {
                    LOGGER.error("Error updating compass for player: " + p.getName().getString(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error in CompassTracker.onServerTick", e);
        }
    }

    private static void updatePlayerCompass(ServerPlayerEntity p) {
        var wp = WaypointStore.ACTIVE.get(p.getUuid());
        if (wp == null) return;

        String playerDim = p.getEntityWorld().getRegistryKey().getValue().toString();
        if (!playerDim.equals(wp.dimension())) {
            p.sendMessage(Text.literal("§7[wp] §fTarget in different dimension: §e" + wp.dimension()), true);
            return;
        }

        Vec3d playerPos = new Vec3d(p.getX(), p.getY(), p.getZ());
        double dist = Util.distanceMeters(playerPos, wp.pos());
        double bearing = Util.bearingDeg(p, wp.pos());
        String arrow = Util.dirArrow(bearing);
        String name = (wp.name() == null || wp.name().isEmpty()) ? "target" : wp.name();

        p.sendMessage(Text.literal(arrow + " " + String.format("%.1f", dist) + "m · \"" + name + "\""), true);
        }
}
