package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class GuiIconAtlas {
   public static final Identifier init_Identifier = init("x");
   public static final Identifier load = init("dot");
   public static final Identifier read = init("check");
   public static final Identifier write = init("chevron_down");
   public static final Identifier update = init("chevron_up");
   public static final Identifier sync = init("chevron_right");
   public static final Identifier flush = init("play");
   public static final Identifier close = init("stop");
   public static final Identifier open = init("x");
   public static final Identifier poll = init("edit");
   public static final Identifier push = init("duplicate");
   public static final Identifier pop = init("plus");
   public static final Identifier tick = init("minus");
   public static final Identifier run = init("pause");
   public static final Identifier get = init("sessions");
   public static final Identifier set = init("settings");
   public static final Identifier put = init("queue");
   public static final Identifier add = init("pin");
   public static final Identifier clear = init("refresh");
   private static final int init_I = 512;

   private GuiIconAtlas() {
   }

   private static Identifier init(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/icons/" + name + ".png");
   }

   public static void init(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int size, int color) {
      init(graphics, icon, x, y, size, size, color);
   }

   public static void init(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int w, int h, int color) {
      if (!autismclient.gui.e.SdfIconRenderer.init(graphics, icon, x, y, w, h, color)) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0.0F, 0.0F, w, h, 512, 512, 512, 512, color);
      }
   }
}
