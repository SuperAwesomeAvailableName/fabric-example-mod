package net.fabricmc.example.waypoints;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.fabricmc.example.waypoints.WaypointStore.ACTIVE;

public final class WaypointCommands {
    private WaypointCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> d) {
        d.register(CommandManager.literal("wp")
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        String dim = p.getEntityWorld().getRegistryKey().getValue().toString();
                        var pos = p.getBlockPos();
                        String name = StringArgumentType.getString(ctx, "name");
                        var wp = new WaypointStore.Waypoint(pos.getX(), pos.getY(), pos.getZ(), dim, name);
                        WaypointStore.set(p.getUuid(), name, wp);
                        ctx.getSource().sendFeedback(() -> Text.literal("§aSaved waypoint §e" + name + " §7@ " + pos.getX()+","+pos.getY()+","+pos.getZ()+" §7in §e" + dim), false);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("goto")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        String name = StringArgumentType.getString(ctx, "name");
                        var wp = WaypointStore.find(p.getUuid(), name);
                        if (wp == null) {
                            ctx.getSource().sendError(Text.literal("No waypoint named '" + name + "'."));
                            return 0;
                        }
                        ACTIVE.put(p.getUuid(), wp);
                        ctx.getSource().sendFeedback(() -> Text.literal("§aTracking §e" + name + " §7(§f" + wp.x()+","+wp.y()+","+wp.z()+"§7)"), false);
                        return 1;
                    })
                )
            )
            .then(CommandManager.literal("list")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    var map = WaypointStore.get(p.getUuid());
                    if (map.isEmpty()) {
                        ctx.getSource().sendFeedback(() -> Text.literal("§7No waypoints yet. Use §e/wp set <name>"), false);
                        return 1;
                    }
                    ctx.getSource().sendFeedback(() -> Text.literal("§aWaypoints:"), false);
                    map.forEach((n, wp) ->
                        ctx.getSource().sendFeedback(() ->
                            Text.literal(" - §e" + n + "§7 @ (" + wp.x()+","+wp.y()+","+wp.z()+") §7" + wp.dimension()), false)
                    );
                    return 1;
                })
            )
            .then(CommandManager.literal("share")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                        String name = StringArgumentType.getString(ctx, "name");
                        var wp = WaypointStore.find(p.getUuid(), name);
                        if (wp == null) {
                            ctx.getSource().sendError(Text.literal("No waypoint named '" + name + "'."));
                            return 0;
                        }

                        String cmd = String.format("/wp track_from %d %d %d %s \"%s\"",
                                wp.x(), wp.y(), wp.z(), wp.dimension(), name);

                        String json = String.format(
                            "{\"text\":\"[Track '%s' at (%d, %d, %d)]\",\"color\":\"yellow\"," +
                            "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"%s\"}," +
                            "\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to track this waypoint\",\"color\":\"gray\"}}}",
                            name, wp.x(), wp.y(), wp.z(), cmd.replace("\"","\\\"")
                        );

                        ctx.getSource().getServer().getCommandManager()
                            .executeWithPrefix(ctx.getSource(), "tellraw @a " + json);

                        // NOTE: sendFeedback expects Supplier<Text> in your mappings:
                        ctx.getSource().sendFeedback(() -> Text.literal("§aShared §e" + name), false);
                        return 1;
                    })
                )
            )


            .then(CommandManager.literal("track_from")
                .then(CommandManager.argument("x", IntegerArgumentType.integer())
                    .then(CommandManager.argument("y", IntegerArgumentType.integer())
                        .then(CommandManager.argument("z", IntegerArgumentType.integer())
                            .then(CommandManager.argument("dimension", StringArgumentType.string())
                                .then(CommandManager.argument("label", StringArgumentType.greedyString())
                                    .executes(ctx -> {
                                        ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                        int x = IntegerArgumentType.getInteger(ctx, "x");
                                        int y = IntegerArgumentType.getInteger(ctx, "y");
                                        int z = IntegerArgumentType.getInteger(ctx, "z");
                                        String dim = StringArgumentType.getString(ctx, "dimension");
                                        String label = StringArgumentType.getString(ctx, "label").replace("\"", "");
                                        var wp = new WaypointStore.Waypoint(x, y, z, dim, label);
                                        ACTIVE.put(p.getUuid(), wp);
                                        ctx.getSource().sendFeedback(() -> Text.literal("§aTracking §e" + label + " §7(" + x+","+y+","+z+")"), false);
                                        return 1;
                                    })
                                )
                            )
                        )
                    )
                )
            )
            .then(CommandManager.literal("clear")
                .executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                    ACTIVE.remove(p.getUuid());
                    ctx.getSource().sendFeedback(() -> Text.literal("§7Tracking cleared."), false);
                    return 1;
                })
            )
        );
    }
}
