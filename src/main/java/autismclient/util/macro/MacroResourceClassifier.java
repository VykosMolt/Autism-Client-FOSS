package autismclient.util.macro;

import java.util.EnumSet;
import java.util.List;

public final class MacroResourceClassifier {
   private MacroResourceClassifier() {
   }

   public static EnumSet<MacroResourceClassifier.MacroResource> classify(MacroAction action) {
      EnumSet<MacroResourceClassifier.MacroResource> resources = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);
      if (action instanceof ToolsAction) {
         return resources;
      }

      if (action instanceof ClickAction || action instanceof PlaceAction || action instanceof UseItemPhaseAction || action instanceof OpenContainerAction || action instanceof InteractEntityAction) {
         resources.addAll(
            EnumSet.of(
               MacroResourceClassifier.MacroResource.INPUT,
               MacroResourceClassifier.MacroResource.ROTATION,
               MacroResourceClassifier.MacroResource.INVENTORY,
               MacroResourceClassifier.MacroResource.GUI,
               MacroResourceClassifier.MacroResource.WORLD_ACTION
            )
         );
      }

      if (action instanceof VClipAction || action instanceof HClipAction || action instanceof PacedTpAction) {
         resources.addAll(EnumSet.of(MacroResourceClassifier.MacroResource.INPUT, MacroResourceClassifier.MacroResource.NAVIGATION, MacroResourceClassifier.MacroResource.NETWORK));
      }

      if (action instanceof ClickAction
         || action instanceof InventoryAction
         || action instanceof ItemAction
         || action instanceof DropAction
         || action instanceof CraftAction
         || action instanceof StoreItemAction
         || action instanceof InventoryAuditAction
         || action instanceof XCarryAction
         || action instanceof SwapSlotsAction
         || action instanceof SelectSlotAction) {
         resources.add(MacroResourceClassifier.MacroResource.INVENTORY);
         resources.add(MacroResourceClassifier.MacroResource.GUI);
      }

      if (action instanceof WaitDurabilityAction durability && durability.useNext) {
         resources.add(MacroResourceClassifier.MacroResource.INVENTORY);
         resources.add(MacroResourceClassifier.MacroResource.GUI);
      }

      if (action instanceof OpenContainerAction
         || action instanceof InteractEntityAction
         || action instanceof CloseGuiAction
         || action instanceof SaveGuiAction
         || action instanceof RestoreGuiAction
         || action instanceof DesyncAction
         || action instanceof NbtBookAction
         || action instanceof CustomMenuAction) {
         resources.add(MacroResourceClassifier.MacroResource.GUI);
         resources.add(MacroResourceClassifier.MacroResource.NETWORK);
      }

      if (action instanceof SendPacketAction || action instanceof PacketAction || action instanceof PacketClickAction || action instanceof PayloadAction || action instanceof PayAction || action instanceof SendChatAction) {
         resources.add(MacroResourceClassifier.MacroResource.NETWORK);
      }

      if (action instanceof DelayPacketsAction) {
         resources.add(MacroResourceClassifier.MacroResource.NETWORK);
         resources.add(MacroResourceClassifier.MacroResource.PACKET_DELAY);
      }

      if (action instanceof UseItemAction) {
         resources.add(MacroResourceClassifier.MacroResource.INPUT);
         resources.add(MacroResourceClassifier.MacroResource.NETWORK);
      }

      if (action instanceof MoveAction || action instanceof SneakAction || action instanceof SprintAction || action instanceof JumpAction) {
         resources.add(MacroResourceClassifier.MacroResource.INPUT);
      }

      if (action instanceof RotateAction || action instanceof LookAtBlockAction) {
         resources.add(MacroResourceClassifier.MacroResource.ROTATION);
      }

      if (action instanceof GoToAction || action instanceof MineAction || action instanceof InstaBreakAction || action instanceof BreakAction) {
         resources.add(MacroResourceClassifier.MacroResource.NAVIGATION);
         resources.add(MacroResourceClassifier.MacroResource.INPUT);
         resources.add(MacroResourceClassifier.MacroResource.ROTATION);
         resources.add(MacroResourceClassifier.MacroResource.NETWORK);
      }

      if (action instanceof ak) {
         resources.add(MacroResourceClassifier.MacroResource.WORLD_ACTION);
      }

      if (action instanceof VillagerDupeAction) {
         resources.add(MacroResourceClassifier.MacroResource.INVENTORY);
         resources.add(MacroResourceClassifier.MacroResource.GUI);
      }

      if (action instanceof WaitVillagerItemAction wait && wait.holdEmerald) {
         resources.add(MacroResourceClassifier.MacroResource.INVENTORY);
      }

      return resources;
   }

   public static EnumSet<MacroResourceClassifier.MacroResource> classifyAll(List<MacroAction> actions) {
      EnumSet<MacroResourceClassifier.MacroResource> resources = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);
      if (actions == null) {
         return resources;
      }

      for (MacroAction action : actions) {
         if (action != null && action.isEnabled() && RaceAction.load(action)) {
            resources.addAll(classify(action));
         }
      }

      return resources;
   }

   public enum MacroResource {
      INVENTORY,
      GUI,
      INPUT,
      ROTATION,
      NAVIGATION,
      NETWORK,
      PACKET_DELAY,
      WORLD_ACTION;
   }
}
