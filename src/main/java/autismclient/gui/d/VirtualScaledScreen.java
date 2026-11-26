package autismclient.gui.d;

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

   protected static String init_Str_Str(String value) {
      return value == null ? "" : value.trim();
   }

   protected static MouseButtonEvent init_MouseButtonEvent_MouseButtonEvent(MouseButtonEvent event) {
      return new MouseButtonEvent(cA.init_D_D(event.x()), cA.init_D_D(event.y()), event.buttonInfo());
   }

   protected int init_I() {
      int width = cA.init_I();
      return width <= 0 ? this.width : width;
   }

   protected int load_I() {
      int height = cA.load_I();
      return height <= 0 ? this.height : height;
   }

   protected void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int fill) {
      autismclient.gui.f.b.SectionCardRenderer.init(autismclient.gui.f.UiHelper.init(graphics, this.font, -10000, -10000), autismclient.gui.f.NetHelper.init(x, y, w, h), fill);
   }

   protected void init_V_Str_I(String message, int accentColor) {
      if (message != null && !message.isBlank() && this.minecraft != null) {
         this.minecraft.execute(() -> bi.init(message, accentColor));
      }
   }
}
