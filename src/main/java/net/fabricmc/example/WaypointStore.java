package net.fabricmc.example.waypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WaypointStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Waypoint>>() {}.getType();
    private static Path BASE_DIR;

    public static final Map<UUID, Map<String, Waypoint>> WAYPOINTS = new HashMap<>();
    public static final Map<UUID, Waypoint> ACTIVE = new HashMap<>();

    public record Waypoint(int x, int y, int z, String dimension, String name) {
        public BlockPos pos() { return new BlockPos(x, y, z); }
    }

    private WaypointStore() {}

    public static void init(MinecraftServer server) {
        BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve("waypoints");
        try { Files.createDirectories(BASE_DIR); } catch (IOException ignored) {}
    }

    public static void shutdown() {
        WAYPOINTS.forEach((uuid, map) -> save(uuid));
    }

    public static Map<String, Waypoint> get(UUID uuid) {
        return WAYPOINTS.computeIfAbsent(uuid, WaypointStore::load);
    }

    public static void set(UUID uuid, String name, Waypoint wp) {
        Map<String, Waypoint> m = get(uuid);
        m.put(name.toLowerCase(), wp);
        save(uuid);
    }

    public static Waypoint find(UUID uuid, String name) {
        return get(uuid).get(name.toLowerCase());
    }

    public static void remove(UUID uuid, String name) {
        Map<String, Waypoint> m = get(uuid);
        m.remove(name.toLowerCase());
        save(uuid);
    }

    private static Map<String, Waypoint> load(UUID uuid) {
        Path p = BASE_DIR.resolve(uuid + ".json");
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                Map<String, Waypoint> m = GSON.fromJson(r, MAP_TYPE);
                return (m != null) ? m : new HashMap<>();
            } catch (IOException ignored) {}
        }
        return new HashMap<>();
    }

    private static void save(UUID uuid) {
        Path p = BASE_DIR.resolve(uuid + ".json");
        try (Writer w = Files.newBufferedWriter(p)) {
            GSON.toJson(WAYPOINTS.getOrDefault(uuid, Map.of()), w);
        } catch (IOException ignored) {}
    }
}
