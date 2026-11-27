package autismclient.gui.ui.layout;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class LayoutContext {
   private final GuiGraphicsExtractor drawContext;
   private final Font textRenderer;
   private final LayoutViewport viewport;
   private final autismclient.gui.f.b.CharEncoder theme;
   private final float mouseX;
   private final float mouseY;
   private final float delta;
   private final float alpha;

   public LayoutContext(
      GuiGraphicsExtractor drawContext, Font textRenderer, LayoutViewport viewport, autismclient.gui.f.b.CharEncoder theme, float mouseX, float mouseY, float delta
   ) {
      this(drawContext, textRenderer, viewport, theme, mouseX, mouseY, delta, 1.0F);
   }

   public LayoutContext(
      GuiGraphicsExtractor drawContext,
      Font textRenderer,
      LayoutViewport viewport,
      autismclient.gui.f.b.CharEncoder theme,
      float mouseX,
      float mouseY,
      float delta,
      float alpha
   ) {
      this.drawContext = drawContext;
      this.textRenderer = textRenderer;
      this.viewport = viewport;
      this.theme = theme;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.delta = delta;
      this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public GuiGraphicsExtractor graphics() {
      return this.drawContext;
   }

   public Font font() {
      return this.textRenderer;
   }

   public LayoutViewport viewport() {
      return this.viewport;
   }

   public autismclient.gui.f.b.CharEncoder theme() {
      return this.theme;
   }

   public float mouseX() {
      return this.mouseX;
   }

   public float mouseY() {
      return this.mouseY;
   }

   public float delta() {
      return this.delta;
   }

   public float alpha() {
      return this.alpha;
   }

   public LayoutContext withAlpha(float alpha) {
      return new LayoutContext(
         this.drawContext, this.textRenderer, this.viewport, this.theme, this.mouseX, this.mouseY, this.delta, alpha
      );
   }

   public int applyAlpha(int color) {
      return scaleAlpha(color, this.alpha);
   }

   public void blitTexture(Identifier textureId, int x1, int y1, int x2, int y2) {
      if (this.drawContext != null && textureId != null) {
         this.drawContext.blit(textureId, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, 1.0F);
         if (this.alpha < 0.999F) {
            int overlayAlpha = Math.max(0, Math.min(255, Math.round((1.0F - this.alpha) * 255.0F)));
            this.drawContext.fill(x1, y1, x2, y2, overlayAlpha << 24 | 460553);
         }
      }
   }

   public static int scaleAlpha(int color, float alpha) {
      int a = color >>> 24 & 0xFF;
      int scaled = Math.max(0, Math.min(255, Math.round(a * Math.max(0.0F, Math.min(1.0F, alpha)))));
      return scaled << 24 | color & 16777215;
   }
}
