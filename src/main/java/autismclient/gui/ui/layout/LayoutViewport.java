package autismclient.gui.ui.layout;

import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LayoutViewport {
   private final float init;
   private final float load;

   private LayoutViewport(float uiWidth, float uiHeight) {
      this.init = Math.max(1.0F, uiWidth);
      this.load = Math.max(1.0F, uiHeight);
   }

   public static LayoutViewport init_RingBuffer_F(float density) {
      return new LayoutViewport(cA.init_I(), cA.load_I());
   }

   public float init() {
      return this.init;
   }

   public float load() {
      return this.load;
   }

   public float read() {
      return 1.0F;
   }

   public float write() {
      return 1.0F;
   }

   public float init_F_D(double screenX) {
      return (float)screenX;
   }

   public float load(double screenY) {
      return (float)screenY;
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context) {
      cA.init_V_GuiGraphicsExtract(context);
   }

   public void load(GuiGraphicsExtractor context) {
      cA.load_V_GuiGraphicsExtract(context);
   }

   public void init(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2) {
      int left = (int)Math.floor(Math.min(x1, x2));
      int top = (int)Math.floor(Math.min(y1, y2));
      int right = (int)Math.ceil(Math.max(x1, x2));
      int bottom = (int)Math.ceil(Math.max(y1, y2));
      autismclient.gui.ui.UiClipStack.getInstance()
         .push(context, autismclient.gui.f.NetHelper.init(left, top, Math.max(0, right - left), Math.max(0, bottom - top)));
   }

   public void read(GuiGraphicsExtractor context) {
      autismclient.gui.ui.UiClipStack.getInstance().pop(context);
   }
}
