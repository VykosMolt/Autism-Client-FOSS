package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.Module;
import autismclient.modules.ak;
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
         init();
         return 1;
      });
   }

   private static void init() {
      Module xray = al.init_Module_Str("xray");
      if (xray == null) {
         AutismClientMessaging.sendPrefixed("§cXray module not registered.");
      } else {
         AutismClientMessaging.sendPrefixed("§e--- OreSim ---");
         AutismClientMessaging.sendPrefixed(
            "§7enabled: §f" + xray.isEnabled() + "§7  mode: §f" + xray.value("mode") + "§7  style: §f" + xray.value("render-style")
         );
         Long seed = ak.load_Long(xray);
         autismclient.util.oresim.BufferHelper.Inner_StreamUtil seedStatus = ak.init_BufferHelper_Strea(xray);
         AutismClientMessaging.sendPrefixed(
            "§7seed: "
               + (
                  seedStatus == autismclient.util.oresim.BufferHelper.Inner_StreamUtil.read
                     ? "§cINVALID"
                     : (seed == null ? "§cNONE - enter a signed 64-bit value" : "§f" + seed)
               )
               + "§7  from: §fthe World Seed text field"
         );
         AutismClientMessaging.sendPrefixed(
            "§7saved scope: §f"
               + AutismWaypoints.init_Str_Minecraft(Minecraft.getInstance())
               + "§7; the same world/server scoping as waypoints. No world or server seed is read."
         );
         AutismClientMessaging.sendPrefixed("§7ore list entries: §f" + ak.load_I(xray) + "§7  families: §f" + Integer.bitCount(ak.read_I(xray)));
         String worldgen = autismclient.util.oresim.StreamUtil.read_Z()
            ? "§cFAILED"
            : (
               autismclient.util.oresim.StreamUtil.load_Z()
                  ? "§e" + autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH().name().toLowerCase(Locale.ROOT)
                  : (
                     autismclient.util.oresim.StreamUtil.init_Z()
                        ? "§aready"
                        : "§c" + autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH().name().toLowerCase(Locale.ROOT)
                  )
            );
         AutismClientMessaging.sendPrefixed("§7local Minecraft 26.2 worldgen: " + worldgen);
         if (autismclient.util.oresim.StreamUtil.read_Z()
            || autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH() == autismclient.util.oresim.StreamUtil.Inner_BufferHelper.update) {
            AutismClientMessaging.sendPrefixed("§c" + autismclient.util.oresim.StreamUtil.init_Str());
         }

         AutismClientMessaging.sendPrefixed(
            "§7chunks simulated: §f" + autismclient.util.oresim.StreamUtil.init_I() + "§7  positions: §f" + autismclient.util.oresim.StreamUtil.load_I()
         );
         AutismClientMessaging.sendPrefixed(
            "§7generation source: §flocal vanilla registries + terrain + carvers + biome decoration§7; received server chunks are not inputs."
         );
         AutismClientMessaging.sendPrefixed("§7nearby real ore: §f" + ak.init_Str());
         AutismClientMessaging.sendPrefixed("§7matched = real ore within 5 blocks; touchingAir = of those, exposed; drawn = also in line of sight.");
      }
   }
}
