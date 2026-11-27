package autismclient.gui.screens;

import autismclient.util.bi;
import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class VirtualScaledScreen extends Screen {
   protected VirtualScaledScreen(Component title) {
      super(title);
   }

   protected static String trim(String value) {
      return value == null ? "" : value.trim();
   }

   protected static MouseButtonEvent toVirtual(MouseButtonEvent event) {
      return new MouseButtonEvent(cA.init_D_D(event.x()), cA.init_D_D(event.y()), event.buttonInfo());
   }

   protected int virtualWidth() {
      int width = cA.init_I();
      return width <= 0 ? this.width : width;
   }

   protected int virtualHeight() {
      int height = cA.load_I();
      return height <= 0 ? this.height : height;
   }

   protected void drawCard(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int fill) {
      autismclient.gui.ui.widgets.SectionCardRenderer.init(autismclient.gui.f.UiHelper.init(graphics, this.font, -10000, -10000), autismclient.gui.f.NetHelper.init(x, y, w, h), fill);
   }

   protected void toast(String message, int accentColor) {
      if (message != null && !message.isBlank() && this.minecraft != null) {
         this.minecraft.execute(() -> bi.init(message, accentColor));
      }
   }
}
