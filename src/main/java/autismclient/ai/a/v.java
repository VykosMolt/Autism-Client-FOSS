package autismclient.ai.a;

import java.util.List;

public final class v {
   public static final int init = 2;

   private v() {
   }

   public static v.Inner_DataUtil init(List<v.Inner_StreamUtil> tools) {
      v.Inner_StreamUtil keep = null;
      v.Inner_StreamUtil swap = null;

      for (v.Inner_StreamUtil tool : tools) {
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
         return new v.Inner_DataUtil.Inner_StreamUtil(keep.slot());
      } else {
         return swap != null ? new v.Inner_DataUtil.Inner_CachePool(swap.slot()) : new v.Inner_DataUtil.Inner_DataUtil_2();
      }
   }

   public sealed interface Inner_DataUtil permits v.Inner_DataUtil.Inner_StreamUtil, v.Inner_DataUtil.Inner_CachePool, v.Inner_DataUtil.Inner_DataUtil_2 {
      record Inner_CachePool(int backpackSlot) implements v.Inner_DataUtil {
      }

      record Inner_DataUtil_2() implements v.Inner_DataUtil {
      }

      record Inner_StreamUtil(int hotbarSlot) implements v.Inner_DataUtil {
      }
   }

   public record Inner_StreamUtil(int slot, int durabilityLeft, boolean suitable, boolean disposable) {
      public Inner_StreamUtil(int slot, int durabilityLeft, boolean suitable) {
         this(slot, durabilityLeft, suitable, false);
      }
   }
}
