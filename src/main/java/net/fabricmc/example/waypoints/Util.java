package net.fabricmc.example.waypoints;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class Util {
    private Util() {}

    public static class Vec3d {
        public final double x, y, z;

        public Vec3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3d subtract(Vec3d other) {
            return new Vec3d(this.x - other.x, this.y - other.y, this.z - other.z);
        }

        public double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public static Vec3d fromBlockPos(BlockPos pos) {
            return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }
    }

    public static double distanceMeters(Vec3d a, Vec3d b) {
        return a.subtract(b).length();
    }

    public static double bearingDeg(ServerPlayerEntity p, Vec3d target) {
        Vec3d playerPos = new Vec3d(p.getX(), p.getY(), p.getZ());
        Vec3d diff = target.subtract(playerPos);
        double dx = diff.x;
        double dz = diff.z;
        double angleRad = Math.atan2(-dx, dz); // 0 = North
        double angleDeg = Math.toDegrees(angleRad);
        angleDeg = (angleDeg + 360.0) % 360.0;

        double playerYaw = (p.getYaw() % 360.0 + 360.0) % 360.0;
        double relativeAngle = angleDeg - ((-playerYaw + 180.0 + 360.0) % 360.0);
        return (relativeAngle + 360.0) % 360.0;
    }

    public static String dirArrow(double deg) {
        String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
        int sector = (int) Math.floor(((deg + 22.5) % 360) / 45.0);
        return arrows[sector];
    }
