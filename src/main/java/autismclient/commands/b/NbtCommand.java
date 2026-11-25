package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ay;
import autismclient.util.bi;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NbtCommand extends Command {
   public NbtCommand() {
      super("nbt", "Inspect or copy the held item's components (NBT).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         init_V_2();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("get").executes(ctx -> {
         init_V_2();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("copy").executes(ctx -> {
         load();
         return 1;
      }));
   }

   private static ItemStack init_ItemStack() {
      Minecraft mc = Minecraft.getInstance();
      Player player = AutismLiteVariant.enabled() ? (mc == null ? null : mc.player) : autismclient.util.g.PoolEntry.init_Player();
      if (player == null) {
         return ItemStack.EMPTY;
      }

      ItemStack main = player.getMainHandItem();
      return !main.isEmpty() ? main : player.getOffhandItem();
   }

   private static void init_V_2() {
      ItemStack stack = init_ItemStack();
      if (stack.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cHold an item in either hand first.");
      } else if (!AutismItemNbtInspectOverlay.init_Z_ItemStack(stack)) {
         AutismClientMessaging.sendPrefixed("§cCould not open the NBT inspector.");
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            AutismItemNbtInspectOverlay overlay = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(mc.font);
            mc.execute(() -> {
               if (mc.gui.screen() == null) {
                  mc.gui.setScreen(new autismclient.gui.d.TaskTimer(overlay));
               }
            });
         }
      }
   }

   private static void load() {
      ItemStack stack = init_ItemStack();
      if (stack.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cHold an item in either hand first.");
      } else {
         try {
            Minecraft.getInstance().keyboardHandler.setClipboard(ay.init(stack));
            bi.init("Copied /give command.");
         } catch (Throwable t) {
            bi.write("Copy failed.");
         }
      }
   }
}
