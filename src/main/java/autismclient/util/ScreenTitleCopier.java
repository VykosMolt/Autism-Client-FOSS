package autismclient.util;

import net.minecraft.client.Minecraft;

public final class ScreenTitleCopier {
   private static final Minecraft init = Minecraft.getInstance();

   private ScreenTitleCopier() {
   }

   public static void copy() {
      if (init.gui.screen() != null && init.keyboardHandler != null) {
         String title = init.gui.screen().getTitle() == null ? "" : init.gui.screen().getTitle().getString();
         init.keyboardHandler.setClipboard(title);
         bi.init("GUI title copied.");
      } else {
         bi.write("Copy failed: no screen.");
      }
   }
}
