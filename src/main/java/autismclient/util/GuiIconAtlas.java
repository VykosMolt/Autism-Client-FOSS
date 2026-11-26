package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class GuiIconAtlas {
   public static final Identifier CLOSE = iconId("x");
   public static final Identifier DOT = iconId("dot");
   public static final Identifier CHECK = iconId("check");
   public static final Identifier CHEVRON_DOWN = iconId("chevron_down");
   public static final Identifier CHEVRON_UP = iconId("chevron_up");
   public static final Identifier CHEVRON_RIGHT = iconId("chevron_right");
   public static final Identifier PLAY = iconId("play");
   public static final Identifier STOP = iconId("stop");
   public static final Identifier DELETE = iconId("x");
   public static final Identifier EDIT = iconId("edit");
   public static final Identifier DUPLICATE = iconId("duplicate");
   public static final Identifier PLUS = iconId("plus");
   public static final Identifier MINUS = iconId("minus");
   public static final Identifier PAUSE = iconId("pause");
   public static final Identifier SESSIONS = iconId("sessions");
   public static final Identifier SETTINGS = iconId("settings");
   public static final Identifier QUEUE = iconId("queue");
   public static final Identifier PIN = iconId("pin");
   public static final Identifier REFRESH = iconId("refresh");
   private static final int ICON_TEXTURE_SIZE = 512;

   private GuiIconAtlas() {
   }

   private static Identifier iconId(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/icons/" + name + ".png");
   }

   public static void draw(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int size, int color) {
      draw(graphics, icon, x, y, size, size, color);
   }

   public static void draw(GuiGraphicsExtractor graphics, Identifier icon, int x, int y, int w, int h, int color) {
      if (!autismclient.gui.sdf.SdfIconRenderer.init(graphics, icon, x, y, w, h, color)) {
         graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0.0F, 0.0F, w, h, 512, 512, 512, 512, color);
      }
   }
}
