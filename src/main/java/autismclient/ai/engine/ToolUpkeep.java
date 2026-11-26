package autismclient.ai.engine;

import java.util.List;

public final class ToolUpkeep {
   public static final int init = 2;

   private ToolUpkeep() {
   }

   public static ToolUpkeep.Inner_DataUtil init(List<ToolUpkeep.Inner_StreamUtil> tools) {
      ToolUpkeep.Inner_StreamUtil keep = null;
      ToolUpkeep.Inner_StreamUtil swap = null;

      for (ToolUpkeep.Inner_StreamUtil tool : tools) {
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
         return new ToolUpkeep.Inner_DataUtil.Inner_StreamUtil(keep.slot());
      } else {
         return swap != null ? new ToolUpkeep.Inner_DataUtil.Inner_CachePool(swap.slot()) : new ToolUpkeep.Inner_DataUtil.Inner_DataUtil_2();
      }
   }

   public sealed interface Inner_DataUtil permits ToolUpkeep.Inner_DataUtil.Inner_StreamUtil, ToolUpkeep.Inner_DataUtil.Inner_CachePool, ToolUpkeep.Inner_DataUtil.Inner_DataUtil_2 {
      record Inner_CachePool(int backpackSlot) implements ToolUpkeep.Inner_DataUtil {
      }

      record Inner_DataUtil_2() implements ToolUpkeep.Inner_DataUtil {
      }

      record Inner_StreamUtil(int hotbarSlot) implements ToolUpkeep.Inner_DataUtil {
      }
   }

   public record Inner_StreamUtil(int slot, int durabilityLeft, boolean suitable, boolean disposable) {
      public Inner_StreamUtil(int slot, int durabilityLeft, boolean suitable) {
         this(slot, durabilityLeft, suitable, false);
      }
   }
}
