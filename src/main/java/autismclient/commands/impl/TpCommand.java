package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.g.PkgCls_a;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import net.minecraft.client.Minecraft;

public final class TpCommand extends Command {
   public TpCommand() {
      super("tp", "Ground-first HClip teleport.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         if (AutismLiteVariant.enabled()) {
            init_V("TP is unavailable in Lite.");
         } else {
            init_V(PkgCls_a.init_Str_Str(""));
         }

         return 1;
      });
      root.then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("arguments", StringArgumentType.greedyString()).suggests((ctx, suggestions) -> {
         String remaining = suggestions.getRemaining();
         String bare = remaining.startsWith("\"") ? remaining.substring(1) : remaining;
         String lower = bare.toLowerCase(Locale.ROOT);

         for (String value : new String[]{"~ ~ ~", "stop", "status", "config 20 500", "reset"}) {
            if (value.startsWith(remaining)) {
               suggestions.suggest(value);
            }
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            for (AutismWaypoints.Waypoint wp : AutismWaypoints.init_AutismWaypoints().init_List_Str(AuditWaypointScope.current(mc))) {
               String name = wp.name();
               if (name.toLowerCase(Locale.ROOT).startsWith(lower)) {
                  suggestions.suggest(name.contains(" ") ? "\"" + name + "\"" : name);
               }
            }
         }

         return suggestions.buildFuture();
      }).executes(ctx -> {
         if (AutismLiteVariant.enabled()) {
            init_V("TP is unavailable in Lite.");
         } else {
            init_V(PkgCls_a.init_Str_Str(init_Str(StringArgumentType.getString(ctx, "arguments"))));
         }

         return 1;
      }));
   }

   private static String init_Str(String args) {
      String trimmed = args == null ? "" : args.trim();
      if (trimmed.isEmpty()) {
         return args;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return args;
      }

      String scope = AuditWaypointScope.current(mc);
      if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
         return init(AutismWaypoints.init_AutismWaypoints().init_AutismWaypoints_Wa(scope, trimmed.substring(1, trimmed.length() - 1)), args);
      }

      String lower = trimmed.toLowerCase(Locale.ROOT);
      return !lower.equals("stop") && !lower.equals("status") && !lower.equals("reset") && !lower.startsWith("config") && !init_Z(trimmed)
         ? init(AutismWaypoints.init_AutismWaypoints().init_AutismWaypoints_Wa(scope, trimmed), args)
         : args;
   }

   private static String init(AutismWaypoints.Waypoint wp, String fallback) {
      return wp == null ? fallback : wp.x() + " " + wp.y() + " " + wp.z();
   }

   private static boolean init_Z(String value) {
      String[] parts = value.split("\\s+");
      if (parts.length < 3) {
         return false;
      }

      for (int i = 0; i < 3; i++) {
         if (!parts[i].startsWith("~")) {
            try {
               Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
               return false;
            }
         }
      }

      return true;
   }

   private static void init_V(String result) {
      String text = result != null && !result.isBlank()
         ? result
         : "Usage: " + autismclient.commands.CommandManager.getPrefix() + "tp <x> <y> <z> [maxPackets] [pauseMs]";
      String color = !text.startsWith("TP started") && !text.startsWith("TP defaults")
         ? (!text.startsWith("Usage") && !text.startsWith("No active") ? "§7" : "§e")
         : "§a";
      AutismClientMessaging.sendPrefixed(color + text);
   }
}
