package autismclient.util;

import net.minecraft.client.Minecraft;

public final class ScreenTitleCopier {
   private static final Minecraft MC = Minecraft.getInstance();

   private ScreenTitleCopier() {
   }

   public static void copy() {
      if (MC.gui.screen() != null && MC.keyboardHandler != null) {
         String title = MC.gui.screen().getTitle() == null ? "" : MC.gui.screen().getTitle().getString();
         MC.keyboardHandler.setClipboard(title);
         bi.init("GUI title copied.");
      } else {
         bi.write("Copy failed: no screen.");
      }
   }
}
