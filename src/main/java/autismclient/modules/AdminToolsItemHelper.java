package autismclient.modules;

import autismclient.util.AutismAdminToolsOverlay;
import autismclient.util.OverlayManager;
import net.minecraft.world.item.ItemStack;

public final class AdminToolsItemHelper {
   private AdminToolsItemHelper() {
   }

   public static boolean isAdminTool(ItemStack stack) {
      return al.init_Module_Str("admin-tools") instanceof f.Inner_DataUtil adminTools ? adminTools.init(stack, false) : false;
   }

   public static boolean load(ItemStack stack) {
      if (!isAdminTool(stack)) {
         return false;
      }

      try {
         AutismAdminToolsOverlay overlay = AutismAdminToolsOverlay.getInstance();
         OverlayManager manager = OverlayManager.getInstance();
         manager.register(overlay);
         overlay.setVisible(true);
         overlay.read_V_2();
         manager.bringToFront(overlay);
      } catch (Throwable var3) {
      }

      return true;
   }
}
