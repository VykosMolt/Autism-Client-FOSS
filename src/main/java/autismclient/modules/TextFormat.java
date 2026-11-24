package autismclient.modules;

import autismclient.util.AutismAdminToolsOverlay;
import autismclient.util.bk;
import net.minecraft.world.item.ItemStack;

public final class TextFormat {
   private TextFormat() {
   }

   public static boolean init(ItemStack stack) {
      return al.init_Module_Str("admin-tools") instanceof f.Inner_DataUtil adminTools ? adminTools.init(stack, false) : false;
   }

   public static boolean load(ItemStack stack) {
      if (!init(stack)) {
         return false;
      }

      try {
         AutismAdminToolsOverlay overlay = AutismAdminToolsOverlay.init_AutismAdminToolsOv();
         bk manager = bk.init_bk();
         manager.init_V(overlay);
         overlay.init_V_Z(true);
         overlay.read_V_2();
         manager.update_V(overlay);
      } catch (Throwable var3) {
      }

      return true;
   }
}
