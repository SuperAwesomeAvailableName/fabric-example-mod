package net.fabricmc.example.waypoints;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class Util {
    private Util() {}

    public static int distanceMeters(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dz = a.getZ() - b.getZ();
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    public static double bearingDeg(ServerPlayerEntity p, BlockPos target) {
        double dx = target.getX() + 0.5 - p.getX();
        double dz = target.getZ() + 0.5 - p.getZ();
        double angleRad = Math.atan2(-dx, dz); // 0 = North
        double angleDeg = Math.toDegrees(angleRad);
        angleDeg = (angleDeg + 360.0) % 360.0;

        double playerYaw = (p.getYaw() % 360.0 + 360.0) % 360.0;
        double rel = angleDeg - ((-playerYaw + 180.0 + 360.0) % 360.0);
        rel = (rel + 360.0) % 360.0;
        return rel;
    }

    public static String dirArrow(double deg) {
        int sector = (int)Math.floor(((deg + 22.5) % 360) / 45.0);
        return switch (sector) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            default -> "↖";
        };
    }




}
