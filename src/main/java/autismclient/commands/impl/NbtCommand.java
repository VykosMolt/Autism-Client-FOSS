package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.ItemSnbtCodec;
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
         inspectHeld();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("get").executes(ctx -> {
         inspectHeld();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("copy").executes(ctx -> {
         load();
         return 1;
      }));
   }

   private static ItemStack heldStack() {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc == null ? null : mc.player;
      if (player == null) {
         return ItemStack.EMPTY;
      }

      ItemStack main = player.getMainHandItem();
      return !main.isEmpty() ? main : player.getOffhandItem();
   }

   private static void inspectHeld() {
      ItemStack stack = heldStack();
      if (stack.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cHold an item in either hand first.");
      } else if (!AutismItemNbtInspectOverlay.inspectStack(stack)) {
         AutismClientMessaging.sendPrefixed("§cCould not open the NBT inspector.");
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            AutismItemNbtInspectOverlay overlay = AutismItemNbtInspectOverlay.getOrCreate(mc.font);
            mc.execute(() -> {
               if (mc.gui.screen() == null) {
                  mc.gui.setScreen(new autismclient.gui.screens.AutismOverlaysScreen(overlay));
               }
            });
         }
      }
   }

   private static void load() {
      ItemStack stack = heldStack();
      if (stack.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cHold an item in either hand first.");
      } else {
         try {
            Minecraft.getInstance().keyboardHandler.setClipboard(ItemSnbtCodec.giveCommand(stack));
            bi.init("Copied /give command.");
         } catch (Throwable t) {
            bi.write("Copy failed.");
         }
      }
   }
}
