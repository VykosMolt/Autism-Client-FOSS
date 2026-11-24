package autismclient.util.macro;

import net.minecraft.nbt.CompoundTag;

final class p {
   private p() {
   }

   static void init(CompoundTag tag, MacroAction action) {
      if (tag != null && action != null) {
         tag.putBoolean("listenDuringPreviousAction", action.listensDuringPreviousAction());
      }
   }

   static void load(CompoundTag tag, MacroAction action) {
      if (tag != null && action != null) {
         action.setListenDuringPreviousAction(tag.getBooleanOr("listenDuringPreviousAction", false));
      }
   }
}
