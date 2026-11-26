package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.ck;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class XCarryCommand extends Command {
   public XCarryCommand() {
      super("xcarry", "Stash the held item into an XCarry slot: craft1..craft5 / helmet / chestplate / leggings / boots / offhand / cursor.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.DataUtil.init_Str();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "xcarry <slot>");
         AutismClientMessaging.sendPrefixed("§7Slots: §fcraft1§7..§fcraft5§7, §fhelmet§7, §fchestplate§7, §fleggings§7, §fboots§7, §foffhand§7, §fcursor§7.");
         return 1;
      });
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("slot", autismclient.commands.a.SyncManager.init())
            .executes(ctx -> init(autismclient.commands.a.SyncManager.init(ctx, "slot")))
      );
   }

   private static int init(int targetSlot) {

      Minecraft mc = Minecraft.getInstance();
      LocalPlayer p = mc.player;
      if (p != null && mc.gameMode != null) {
         ItemStack heldStack = p.getMainHandItem().copy();
         if (heldStack == null || heldStack.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§cHold an item first.");
            return 1;
         }

         if (p.containerMenu != p.inventoryMenu) {
            AutismClientMessaging.sendPrefixed("§cOpen your inventory first (E), then run the command.");
            return 1;
         }

         AbstractContainerMenu container = p.containerMenu;
         int hotbar = p.getInventory().getSelectedSlot();
         int sourceSlotId = 36 + hotbar;
         ck shared = ck.init_ck();
         boolean prevBypass = shared.close_Z();
         shared.flush(true);

         try {
            mc.gameMode.handleContainerInput(container.containerId, sourceSlotId, 0, ContainerInput.PICKUP, p);
            if (container.getCarried().isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cCould not pick up the held item.");
               return 1;
            }

            String itemName = heldStack.getHoverName().getString();
            String slotName = autismclient.commands.a.SyncManager.init_Str_I(targetSlot);
            LinkedHashSet<Integer> forcedSlots = init(shared);
            boolean forcedCursor = shared.sync_Z() && shared.flush_Z();
            if (targetSlot == -2147483648) {
               shared.init_V_Set_Z(forcedSlots, true);
               shared.sync(true);
               shared.update(true);
               AutismClientMessaging.sendPrefixed("§aHolding §f" + itemName + "§a on cursor (XCarry).");
               return 1;
            }

            if (targetSlot >= 0 && targetSlot < container.slots.size()) {
               mc.gameMode.handleContainerInput(container.containerId, targetSlot, 0, ContainerInput.PICKUP, p);
               boolean placed = container.getCarried().isEmpty()
                  && targetSlot >= 0
                  && targetSlot < container.slots.size()
                  && !container.slots.get(targetSlot).getItem().isEmpty();
               if (!placed && !container.getCarried().isEmpty()) {
                  mc.gameMode.handleContainerInput(container.containerId, sourceSlotId, 0, ContainerInput.PICKUP, p);
               }

               if (!placed) {
                  AutismClientMessaging.sendPrefixed("§cCould not stash into §f" + slotName + "§c.");
                  return 1;
               } else {
                  forcedSlots.add(targetSlot);
                  shared.init_V_Set_Z(forcedSlots, forcedCursor);
                  shared.sync(true);
                  shared.update(true);
                  AutismClientMessaging.sendPrefixed("§aStashed §f" + itemName + "§a into §f" + slotName + "§a.");
                  return 1;
               }
            } else {
               AutismClientMessaging.sendPrefixed("§cInvalid slot (" + targetSlot + ").");
               mc.gameMode.handleContainerInput(container.containerId, sourceSlotId, 0, ContainerInput.PICKUP, p);
               return 1;
            }
         } finally {
            shared.flush(prevBypass);
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cNot in a world.");
         return 1;
      }
   }

   private static LinkedHashSet<Integer> init(ck shared) {
      LinkedHashSet<Integer> slots = new LinkedHashSet<>();
      if (shared != null && shared.sync_Z()) {
         Set<Integer> existing = shared.init_Set();
         if (existing != null) {
            for (Integer slot : existing) {
               if (slot != null && slot >= 0) {
                  slots.add(slot);
               }
            }
         }
      }

      return slots;
   }
}
