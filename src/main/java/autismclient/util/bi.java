package autismclient.util;

import autismclient.gui.f.b.ToastNotificationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class bi {
   private static final int init_I = -14249;
   private static final ToastNotificationStack init_f = new ToastNotificationStack(2100L, 140.0F, 220.0F, 4, 4, 18);

   private static int load() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-13248397, ThemePaletteManager.Inner_DataUtil.SUCCESS);
   }

   private bi() {
   }

   public static void init(String message) {
      init(message != null && !message.isBlank() ? message : "Copied.", load());
   }

   public static void load(String message) {
      init(message, load());
   }

   public static void read(String message) {
      init(message, -14249);
   }

   public static void write(String message) {
      init(message, -42149);
   }

   public static void init(String message, int accentColor) {
      if (!autismclient.modules.aD.write_Z()) {
         init_f.init(message, accentColor);
      }
   }

   public static void init(GuiGraphicsExtractor graphics) {
      if (autismclient.modules.aD.write_Z()) {
         init_V();
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (graphics != null && mc != null && mc.font != null) {
            NameScrapeController.init(graphics);
            if (init_f.init_Z()) {
               int width = Math.min(320, Math.max(120, cA.init_I() - 16));
               int x = Math.max(8, (cA.init_I() - width) / 2);
               init_f.init(autismclient.gui.f.UiHelper.init(graphics, mc.font, -1, -1), x, 8, width);
            }
         }
      }
   }

   public static boolean init_Z() {
      return autismclient.modules.aD.write_Z() ? false : init_f.init_Z() || NameScrapeController.init_Z();
   }

   public static int init_I() {
      return init_f.init_I();
   }

   public static void init_V() {
      init_f.init_V();
   }
}
