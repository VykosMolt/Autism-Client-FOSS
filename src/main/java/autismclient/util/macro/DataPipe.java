package autismclient.util.macro;

import java.util.EnumSet;
import java.util.List;

public final class DataPipe {
   private DataPipe() {
   }

   public static EnumSet<DataPipe.Inner_DataUtil> init(MacroAction action) {
      EnumSet<DataPipe.Inner_DataUtil> resources = EnumSet.noneOf(DataPipe.Inner_DataUtil.class);
      if (action instanceof ToolsAction) {
         return resources;
      }

      if (action instanceof ClickAction || action instanceof PlaceAction || action instanceof UseItemPhaseAction || action instanceof OpenContainerAction || action instanceof InteractEntityAction) {
         resources.addAll(
            EnumSet.of(
               DataPipe.Inner_DataUtil.INPUT,
               DataPipe.Inner_DataUtil.ROTATION,
               DataPipe.Inner_DataUtil.INVENTORY,
               DataPipe.Inner_DataUtil.GUI,
               DataPipe.Inner_DataUtil.WORLD_ACTION
            )
         );
      }

      if (action instanceof VClipAction || action instanceof HClipAction || action instanceof PacedTpAction) {
         resources.addAll(EnumSet.of(DataPipe.Inner_DataUtil.INPUT, DataPipe.Inner_DataUtil.NAVIGATION, DataPipe.Inner_DataUtil.NETWORK));
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
         resources.add(DataPipe.Inner_DataUtil.INVENTORY);
         resources.add(DataPipe.Inner_DataUtil.GUI);
      }

      if (action instanceof WaitDurabilityAction durability && durability.useNext) {
         resources.add(DataPipe.Inner_DataUtil.INVENTORY);
         resources.add(DataPipe.Inner_DataUtil.GUI);
      }

      if (action instanceof OpenContainerAction
         || action instanceof InteractEntityAction
         || action instanceof CloseGuiAction
         || action instanceof SaveGuiAction
         || action instanceof RestoreGuiAction
         || action instanceof DesyncAction
         || action instanceof NbtBookAction
         || action instanceof CustomMenuAction) {
         resources.add(DataPipe.Inner_DataUtil.GUI);
         resources.add(DataPipe.Inner_DataUtil.NETWORK);
      }

      if (action instanceof SendPacketAction || action instanceof PacketAction || action instanceof PacketClickAction || action instanceof PayloadAction || action instanceof PayAction || action instanceof SendChatAction) {
         resources.add(DataPipe.Inner_DataUtil.NETWORK);
      }

      if (action instanceof DelayPacketsAction) {
         resources.add(DataPipe.Inner_DataUtil.NETWORK);
         resources.add(DataPipe.Inner_DataUtil.PACKET_DELAY);
      }

      if (action instanceof UseItemAction) {
         resources.add(DataPipe.Inner_DataUtil.INPUT);
         resources.add(DataPipe.Inner_DataUtil.NETWORK);
      }

      if (action instanceof MoveAction || action instanceof SneakAction || action instanceof SprintAction || action instanceof JumpAction) {
         resources.add(DataPipe.Inner_DataUtil.INPUT);
      }

      if (action instanceof RotateAction || action instanceof LookAtBlockAction) {
         resources.add(DataPipe.Inner_DataUtil.ROTATION);
      }

      if (action instanceof GoToAction || action instanceof MineAction || action instanceof InstaBreakAction || action instanceof BreakAction) {
         resources.add(DataPipe.Inner_DataUtil.NAVIGATION);
         resources.add(DataPipe.Inner_DataUtil.INPUT);
         resources.add(DataPipe.Inner_DataUtil.ROTATION);
         resources.add(DataPipe.Inner_DataUtil.NETWORK);
      }

      if (action instanceof ak) {
         resources.add(DataPipe.Inner_DataUtil.WORLD_ACTION);
      }

      if (action instanceof VillagerDupeAction) {
         resources.add(DataPipe.Inner_DataUtil.INVENTORY);
         resources.add(DataPipe.Inner_DataUtil.GUI);
      }

      if (action instanceof WaitVillagerItemAction wait && wait.holdEmerald) {
         resources.add(DataPipe.Inner_DataUtil.INVENTORY);
      }

      return resources;
   }

   public static EnumSet<DataPipe.Inner_DataUtil> init(List<MacroAction> actions) {
      EnumSet<DataPipe.Inner_DataUtil> resources = EnumSet.noneOf(DataPipe.Inner_DataUtil.class);
      if (actions == null) {
         return resources;
      }

      for (MacroAction action : actions) {
         if (action != null && action.isEnabled() && RaceAction.load(action)) {
            resources.addAll(init(action));
         }
      }

      return resources;
   }

   public enum Inner_DataUtil {
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
