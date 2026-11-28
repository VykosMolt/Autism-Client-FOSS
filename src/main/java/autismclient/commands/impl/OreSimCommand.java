package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.Module;
import autismclient.modules.OreSimEspScanner;
import autismclient.modules.al;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismWaypoints;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import net.minecraft.client.Minecraft;

public class OreSimCommand extends Command {
   private static final String init = "§7";
   private static final String load = "§f";
   private static final String read = "§c";
   private static final String write = "§e";
   private static final String update = "§a";

   public OreSimCommand() {
      super("oresim", "Report why OreSim is or is not predicting.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         printStatus();
         return 1;
      });
   }

   private static void printStatus() {
      Module xray = al.init_Module_Str("xray");
      if (xray == null) {
         AutismClientMessaging.sendPrefixed("§cXray module not registered.");
      } else {
         AutismClientMessaging.sendPrefixed("§e--- OreSim ---");
         AutismClientMessaging.sendPrefixed(
            "§7enabled: §f" + xray.isEnabled() + "§7  mode: §f" + xray.value("mode") + "§7  style: §f" + xray.value("render-style")
         );
         Long seed = OreSimEspScanner.seed(xray);
         autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil seedStatus = OreSimEspScanner.seedInfo(xray);
         AutismClientMessaging.sendPrefixed(
            "§7seed: "
               + (
                  seedStatus == autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil.INVALID
                     ? "§cINVALID"
                     : (seed == null ? "§cNONE - enter a signed 64-bit value" : "§f" + seed)
               )
               + "§7  from: §fthe World Seed text field"
         );
         AutismClientMessaging.sendPrefixed(
            "§7saved scope: §f"
               + AutismWaypoints.scopeFor(Minecraft.getInstance())
               + "§7; the same world/server scoping as waypoints. No world or server seed is read."
         );
         AutismClientMessaging.sendPrefixed("§7ore list entries: §f" + OreSimEspScanner.targetCount(xray) + "§7  families: §f" + Integer.bitCount(OreSimEspScanner.read_I(xray)));
         String worldgen = autismclient.util.oresim.OreSimEngine.hasError()
            ? "§cFAILED"
            : (
               autismclient.util.oresim.OreSimEngine.load_Z()
                  ? "§e" + autismclient.util.oresim.OreSimEngine.status().name().toLowerCase(Locale.ROOT)
                  : (
                     autismclient.util.oresim.OreSimEngine.isReady()
                        ? "§aready"
                        : "§c" + autismclient.util.oresim.OreSimEngine.status().name().toLowerCase(Locale.ROOT)
                  )
            );
         AutismClientMessaging.sendPrefixed("§7local Minecraft 26.2 worldgen: " + worldgen);
         if (autismclient.util.oresim.OreSimEngine.hasError()
            || autismclient.util.oresim.OreSimEngine.status() == autismclient.util.oresim.OreSimEngine.EngineStatus.UNVERIFIED_WORLDGEN) {
            AutismClientMessaging.sendPrefixed("§c" + autismclient.util.oresim.OreSimEngine.errorMessage());
         }

         AutismClientMessaging.sendPrefixed(
            "§7chunks simulated: §f" + autismclient.util.oresim.OreSimEngine.simulatedChunkCount() + "§7  positions: §f" + autismclient.util.oresim.OreSimEngine.positionCount()
         );
         AutismClientMessaging.sendPrefixed(
            "§7generation source: §flocal vanilla registries + terrain + carvers + biome decoration§7; received server chunks are not inputs."
         );
         AutismClientMessaging.sendPrefixed("§7nearby real ore: §f" + OreSimEspScanner.debugCounters());
         AutismClientMessaging.sendPrefixed("§7matched = real ore within 5 blocks; touchingAir = of those, exposed; drawn = also in line of sight.");
      }
   }
}
