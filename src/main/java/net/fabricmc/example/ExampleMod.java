package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.example.waypoints.CompassTracker;
import net.fabricmc.example.waypoints.WaypointCommands;
import net.fabricmc.example.waypoints.WaypointStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
public static final String MOD_ID = "fabric-example-mod";
public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

@Override
public void onInitialize() {
LOGGER.info("Waypoints & Compass mod initializing…");

ServerLifecycleEvents.SERVER_STARTED.register(server -> {
WaypointStore.init(server);
CompassTracker.init(server);
LOGGER.info("WaypointStore and CompassTracker ready");
});

ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
WaypointStore.shutdown();
});

ServerTickEvents.END_SERVER_TICK.register(server -> {
CompassTracker.onServerTick();
});

CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
WaypointCommands.register(dispatcher);
});
}
}
