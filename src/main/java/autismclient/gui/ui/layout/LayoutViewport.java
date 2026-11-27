package autismclient.gui.ui.layout;

import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LayoutViewport {
   private final float uiWidth;
   private final float uiHeight;

   private LayoutViewport(float uiWidth, float uiHeight) {
      this.uiWidth = Math.max(1.0F, uiWidth);
      this.uiHeight = Math.max(1.0F, uiHeight);
   }

   public static LayoutViewport create(float density) {
      return new LayoutViewport(cA.init_I(), cA.load_I());
   }

   public float width() {
      return this.uiWidth;
   }

   public float height() {
      return this.uiHeight;
   }

   public float scaleX() {
      return 1.0F;
   }

   public float scaleY() {
      return 1.0F;
   }

   public float toUiX(double screenX) {
      return (float)screenX;
   }

   public float toUiY(double screenY) {
      return (float)screenY;
   }

   public void pushTransform(GuiGraphicsExtractor context) {
      cA.init_V_GuiGraphicsExtract(context);
   }

   public void popTransform(GuiGraphicsExtractor context) {
      cA.load_V_GuiGraphicsExtract(context);
   }

   public void pushClip(GuiGraphicsExtractor context, float x1, float y1, float x2, float y2) {
      int left = (int)Math.floor(Math.min(x1, x2));
      int top = (int)Math.floor(Math.min(y1, y2));
      int right = (int)Math.ceil(Math.max(x1, x2));
      int bottom = (int)Math.ceil(Math.max(y1, y2));
      autismclient.gui.ui.UiClipStack.getInstance()
         .push(context, autismclient.gui.f.NetHelper.init(left, top, Math.max(0, right - left), Math.max(0, bottom - top)));
   }

   public void popClip(GuiGraphicsExtractor context) {
      autismclient.gui.ui.UiClipStack.getInstance().pop(context);
   }
}
