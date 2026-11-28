package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.al;
import autismclient.modules.WaypointsModule;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismWaypoints;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;

public final class WaypointsCommand extends Command {
   private static final int DEFAULT_COLOR = -12855297;

   public WaypointsCommand() {
      super("waypoints", "Save and manage waypoints.", "wp");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> this.addHere());
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("gui").executes(ctx -> this.openGui()));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("list").executes(ctx -> this.openGui()));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("clear").executes(ctx -> this.clearAll()));
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("del").executes(ctx -> this.usage()))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", StringArgumentType.greedyString())
                  .suggests(autismclient.commands.CommandSuggestions::tick)
                  .executes(ctx -> this.delete(StringArgumentType.getString(ctx, "name")))
            )
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Double>argument("x", DoubleArgumentType.doubleArg())
            .suggests(autismclient.commands.CommandSuggestions::pop)
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Double>argument("y", DoubleArgumentType.doubleArg())
                  .suggests(autismclient.commands.CommandSuggestions::pop)
                  .then(
                     RequiredArgumentBuilder.<AutismCommandSource, Double>argument("z", DoubleArgumentType.doubleArg())
                        .suggests(autismclient.commands.CommandSuggestions::pop)
                        .executes(
                           ctx -> this.addAt(
                              DoubleArgumentType.getDouble(ctx, "x"), DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z")
                           )
                        )
                  )
            )
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("args", StringArgumentType.greedyString())
            .executes(ctx -> this.init_I_Str_3(StringArgumentType.getString(ctx, "args")))
      );
   }

   private int init_I_Str_3(String args) {
      String trimmed = args == null ? "" : args.trim();
      if (trimmed.equalsIgnoreCase("gui") || trimmed.equalsIgnoreCase("list")) {
         return this.openGui();
      }

      if (trimmed.equalsIgnoreCase("clear")) {
         return this.clearAll();
      }

      if (trimmed.regionMatches(true, 0, "del ", 0, 4)) {
         return this.delete(trimmed.substring(4));
      }

      Minecraft mc = Minecraft.getInstance();
      double[] coords = parseCoords(
         trimmed,
         mc != null && mc.player != null ? mc.player.getX() : 0.0,
         mc != null && mc.player != null ? mc.player.getY() : 0.0,
         mc != null && mc.player != null ? mc.player.getZ() : 0.0
      );
      return coords != null ? this.addAt(coords[0], coords[1], coords[2]) : this.usage();
   }

   private int usage() {
      AutismClientMessaging.sendPrefixed(
         "§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "wp [x y z] | gui | del <name> | clear  §7(coordinates accept ~ and ~N)"
      );
      return 1;
   }

   private int addHere() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         AutismWaypoints store = AutismWaypoints.getInstance();
         String scope = AuditWaypointScope.current(mc);
         int x = (int)Math.floor(mc.player.getX());
         int y = (int)Math.floor(mc.player.getY());
         int z = (int)Math.floor(mc.player.getZ());
         String name = store.nextName(scope, "Waypoint");
         store.init(scope, new AutismWaypoints.Waypoint(name, x, y, z, waypointColor(), System.currentTimeMillis()));
         AutismClientMessaging.sendPrefixed("§aWaypoint saved: §f" + name + " §7(" + x + " " + y + " " + z + ")");
         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cNot in a world.");
         return 1;
      }
   }

   private int addAt(double dx, double dy, double dz) {
      Minecraft mc = Minecraft.getInstance();
      AutismWaypoints store = AutismWaypoints.getInstance();
      String scope = AuditWaypointScope.current(mc);
      int x = floorToInt(dx);
      int y = floorToInt(dy);
      int z = floorToInt(dz);
      String name = store.nextName(scope, "Waypoint");
      store.init(scope, new AutismWaypoints.Waypoint(name, x, y, z, waypointColor(), System.currentTimeMillis()));
      AutismClientMessaging.sendPrefixed("§aWaypoint saved: §f" + name + " §7(" + x + " " + y + " " + z + ")");
      return 1;
   }

   private int delete(String name) {
      String trimmed = name == null ? "" : name.trim();
      if (trimmed.isEmpty()) {
         return this.usage();
      }

      String scope = AuditWaypointScope.current(Minecraft.getInstance());
      if (AutismWaypoints.getInstance().remove(scope, trimmed)) {
         AutismClientMessaging.sendPrefixed("§aWaypoint deleted: §f" + trimmed);
      } else {
         AutismClientMessaging.sendPrefixed("§cNo waypoint named '" + trimmed + "'.");
      }

      return 1;
   }

   private int clearAll() {
      String scope = AuditWaypointScope.current(Minecraft.getInstance());
      AutismWaypoints store = AutismWaypoints.getInstance();
      int count = store.list(scope).size();
      store.clearScope(scope);
      AutismClientMessaging.sendPrefixed(count == 0 ? "§7No waypoints to clear." : "§aCleared " + count + " waypoint" + (count == 1 ? "" : "s") + ".");
      return 1;
   }

   private int openGui() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.gui != null) {
         mc.execute(() -> mc.gui.setScreen(new autismclient.gui.screens.WaypointsScreen(mc.gui.screen())));
         return 1;
      } else {
         return 1;
      }
   }

   private static int waypointColor() {
      return al.init_Module_Str("waypoints") instanceof WaypointsModule waypoints ? waypoints.init_I_23() : -12855297;
   }

   static double[] parseCoords(String args, double px, double py, double pz) {
      String[] parts = args.trim().split("\\s+");
      if (parts.length != 3) {
         return null;
      }

      double[] base = new double[]{px, py, pz};
      double[] out = new double[3];

      try {
         for (int i = 0; i < 3; i++) {
            String part = parts[i];
            if (part.startsWith("~")) {
               out[i] = base[i] + (part.length() == 1 ? 0.0 : Double.parseDouble(part.substring(1)));
            } else {
               out[i] = Double.parseDouble(part);
            }

            if (!Double.isFinite(out[i])) {
               return null;
            }
         }

         return out;
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private static int floorToInt(double value) {
      return Double.isFinite(value) ? (int)Math.floor(value) : 0;
   }
}
