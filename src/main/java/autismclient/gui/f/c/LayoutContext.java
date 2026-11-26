package autismclient.gui.f.c;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class LayoutContext {
   private final GuiGraphicsExtractor init_GuiGraphicsExtract;
   private final Font init_Font;
   private final LayoutViewport init_RingBuffer;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder;
   private final float init_F;
   private final float load;
   private final float read;
   private final float write;

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
      this.init_GuiGraphicsExtract = drawContext;
      this.init_Font = textRenderer;
      this.init_RingBuffer = viewport;
      this.init_CharEncoder = theme;
      this.init_F = mouseX;
      this.load = mouseY;
      this.read = delta;
      this.write = Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public GuiGraphicsExtractor init_GuiGraphicsExtract() {
      return this.init_GuiGraphicsExtract;
   }

   public Font init_Font() {
      return this.init_Font;
   }

   public LayoutViewport init_RingBuffer() {
      return this.init_RingBuffer;
   }

   public autismclient.gui.f.b.CharEncoder init_CharEncoder() {
      return this.init_CharEncoder;
   }

   public float init_F() {
      return this.init_F;
   }

   public float load() {
      return this.load;
   }

   public float read() {
      return this.read;
   }

   public float write() {
      return this.write;
   }

   public LayoutContext init_SyncManager_F(float alpha) {
      return new LayoutContext(
         this.init_GuiGraphicsExtract, this.init_Font, this.init_RingBuffer, this.init_CharEncoder, this.init_F, this.load, this.read, alpha
      );
   }

   public int init_I_I(int color) {
      return init(color, this.write);
   }

   public void init(Identifier textureId, int x1, int y1, int x2, int y2) {
      if (this.init_GuiGraphicsExtract != null && textureId != null) {
         this.init_GuiGraphicsExtract.blit(textureId, x1, y1, x2, y2, 0.0F, 1.0F, 0.0F, 1.0F);
         if (this.write < 0.999F) {
            int overlayAlpha = Math.max(0, Math.min(255, Math.round((1.0F - this.write) * 255.0F)));
            this.init_GuiGraphicsExtract.fill(x1, y1, x2, y2, overlayAlpha << 24 | 460553);
         }
      }
   }

   public static int init(int color, float alpha) {
      int a = color >>> 24 & 0xFF;
      int scaled = Math.max(0, Math.min(255, Math.round(a * Math.max(0.0F, Math.min(1.0F, alpha)))));
      return scaled << 24 | color & 16777215;
   }
}
