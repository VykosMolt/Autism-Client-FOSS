package autismclient.ai.engine;

import java.util.List;

public final class ToolUpkeep {
   public static final int MIN_DURABILITY_LEFT = 2;

   private ToolUpkeep() {
   }

   public static ToolUpkeep.UpkeepDecision decide(List<ToolUpkeep.ToolSlot> tools) {
      ToolUpkeep.ToolSlot keep = null;
      ToolUpkeep.ToolSlot swap = null;

      for (ToolUpkeep.ToolSlot tool : tools) {
         if (tool.suitable() && (tool.durabilityLeft() < 0 || tool.durabilityLeft() > (tool.disposable() ? 0 : 2))) {
            if (tool.slot() < 9) {
               if (keep == null || tool.durabilityLeft() > keep.durabilityLeft()) {
                  keep = tool;
               }
            } else if (swap == null || tool.durabilityLeft() > swap.durabilityLeft()) {
               swap = tool;
            }
         }
      }

      if (keep != null) {
         return new ToolUpkeep.UpkeepDecision.KeepHotbar(keep.slot());
      } else {
         return swap != null ? new ToolUpkeep.UpkeepDecision.SwapFromBackpack(swap.slot()) : new ToolUpkeep.UpkeepDecision.NoUsableTool();
      }
   }

   public sealed interface UpkeepDecision permits ToolUpkeep.UpkeepDecision.KeepHotbar, ToolUpkeep.UpkeepDecision.SwapFromBackpack, ToolUpkeep.UpkeepDecision.NoUsableTool {
      record SwapFromBackpack(int backpackSlot) implements ToolUpkeep.UpkeepDecision {
      }

      record NoUsableTool() implements ToolUpkeep.UpkeepDecision {
      }

      record KeepHotbar(int hotbarSlot) implements ToolUpkeep.UpkeepDecision {
      }
   }

   public record ToolSlot(int slot, int durabilityLeft, boolean suitable, boolean disposable) {
      public ToolSlot(int slot, int durabilityLeft, boolean suitable) {
         this(slot, durabilityLeft, suitable, false);
      }
   }
}
