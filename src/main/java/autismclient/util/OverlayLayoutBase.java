package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class OverlayLayoutBase {
   protected static final int update = 14;
   protected static final int sync = 8;
   private autismclient.gui.ui.EaseOutAnimator init_RingBuffer;
   private int init_I = -1;

   public static int open_I() {
      return 14;
   }

   protected OverlayBounds init_cH_cX(AutismOverlay overlay) {
      return this.init_cH_cX_cH(overlay, overlay.init_cH());
   }

   protected OverlayBounds init_cH_cX_cH(AutismOverlay overlay, OverlayBounds bounds) {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.getWindow() != null && bounds != null ? init_cH_cH(bounds, overlay.init_I(), overlay.load_I(), cA.init_I(), cA.load_I()) : bounds;
   }

   static OverlayBounds init_cH_cH(OverlayBounds bounds, int overlayMinWidth, int overlayMinHeight, int screenWidth, int screenHeight) {
      if (bounds == null) {
         return null;
      } else if (screenWidth > 0 && screenHeight > 0) {
         int safeMargin = 4;
         int availableWidth = Math.max(1, screenWidth - safeMargin * 2);
         int availableHeight = Math.max(14, screenHeight - safeMargin * 2);
         int minWidth = Math.min(overlayMinWidth, availableWidth);
         int minHeight = Math.min(overlayMinHeight, availableHeight);
         int width = Math.max(minWidth, Math.min(bounds.read, availableWidth));
         int height = Math.max(minHeight, Math.min(bounds.write, availableHeight));
         int renderedHeight = bounds.load_Z ? 14 : height;
         int x = Math.max(safeMargin, Math.min(bounds.init_I, Math.max(safeMargin, screenWidth - safeMargin - width)));
         int y = Math.max(safeMargin, Math.min(bounds.load_I, Math.max(safeMargin, screenHeight - safeMargin - renderedHeight)));
         return new OverlayBounds(x, y, width, height, bounds.init_Z, bounds.load_Z);
      } else {
         return bounds;
      }
   }

   protected int init_I_cH_Z(OverlayBounds bounds, boolean collapsed) {
      if (bounds == null) {
         return 14;
      }

      int full = Math.max(14, bounds.write);
      float target = collapsed ? 0.0F : 1.0F;
      if (this.init_RingBuffer == null) {
         this.init_RingBuffer = new autismclient.gui.ui.EaseOutAnimator(target, 180);
      }

      float reveal = this.init_RingBuffer.init_F_F(target);
      this.init_I = 14 + Math.round((full - 14) * reveal);
      return this.init_I;
   }

   protected void sync_V_Z(boolean collapsed) {
      if (this.init_RingBuffer != null) {
         this.init_RingBuffer.init_V_F(collapsed ? 0.0F : 1.0F);
      }
   }

   public boolean add_Z() {
      return this.init_RingBuffer != null && this.init_RingBuffer.init_Z();
   }

   public int poll_I() {
      return this.init_I;
   }

   protected boolean init_Z_Z(boolean collapsed) {
      return this.init_I < 0 ? collapsed : this.init_I <= 15;
   }

   protected void init_V_GuiGraphicsExtract_6(
      GuiGraphicsExtractor context, int mouseX, int mouseY, OverlayBounds bounds, String title, boolean collapsed, boolean activeDrag
   ) {
      this.init_V_SyncManager_10(autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, mouseX, mouseY), bounds, title, collapsed, activeDrag);
   }

   protected void init_V_SyncManager_10(autismclient.gui.f.SyncManager ui, OverlayBounds bounds, String title, boolean collapsed, boolean activeDrag) {
      boolean active = activeDrag || this.open_Z_3();
      int frameHeight = this.init_I_cH_Z(bounds, collapsed);
      autismclient.gui.ui.widgets.OverlayFrameHelper.init(
         ui,
         autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, frameHeight),
         14,
         title,
         collapsed,
         active,
         ui.write() >= bounds.load_I && ui.write() < bounds.load_I + 14
      );
   }

   protected boolean init_Z_GuiGraphicsExtract(GuiGraphicsExtractor context, OverlayBounds bounds, boolean collapsed) {
      int frameHeight = this.init_I_cH_Z(bounds, collapsed);
      return frameHeight <= 15
         ? false
         : autismclient.gui.ui.widgets.OverlayFrameHelper.init(context, autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, frameHeight), 14, false);
   }

   protected void init_V_GuiGraphicsExtract_Z(GuiGraphicsExtractor context, boolean clipped) {
      autismclient.gui.ui.widgets.OverlayFrameHelper.init(context, clipped);
   }

   protected void init_V_GuiGraphicsExtract_14(GuiGraphicsExtractor context, OverlayBounds bounds, boolean collapsed, boolean activeDrag) {
      int frameHeight = this.init_I_cH_Z(bounds, collapsed);
      if (!activeDrag && !this.open_Z_3()) {
         autismclient.gui.sdf.SdfShapeRenderer.init(
            context, bounds.init_I + 1, bounds.load_I + 1, Math.max(0, bounds.read - 2), Math.max(0, frameHeight - 2), 7.0F, 603979776
         );
      }
   }

   protected int sync_I(int innerHeight, int rowStep) {
      int safeInnerHeight = Math.max(0, innerHeight);
      int safeRowStep = Math.max(1, rowStep);
      if (safeInnerHeight != 0 && safeRowStep > 1) {
         int aligned = safeInnerHeight / safeRowStep * safeRowStep;
         return aligned > 0 ? aligned : Math.min(safeInnerHeight, safeRowStep);
      } else {
         return safeInnerHeight;
      }
   }

   protected int init_I_I_3(int offset, int stepSize, int maxScroll) {
      int clampedMax = Math.max(0, maxScroll);
      int clampedOffset = Math.max(0, Math.min(offset, clampedMax));
      int safeStepSize = Math.max(1, stepSize);
      if (safeStepSize <= 1) {
         return clampedOffset;
      }

      int quantized = clampedOffset / safeStepSize * safeStepSize;
      if (quantized > clampedMax) {
         quantized = clampedMax / safeStepSize * safeStepSize;
      }

      return Math.max(0, Math.min(quantized, clampedMax));
   }

   protected int load_I_cH_Z(OverlayBounds bounds, boolean collapsed) {
      if (bounds == null) {
         return 14;
      } else {
         return collapsed ? 14 : Math.max(14, bounds.write);
      }
   }

   protected boolean write(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.load(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected boolean init_Z_D(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.poll_Z_17() ? false : this.read(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected boolean read_Z_D_2(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.write(mouseX, mouseY, bounds) || this.init_Z_D(mouseX, mouseY, bounds);
   }

   private boolean open_Z_3() {
      return this instanceof AutismOverlay overlay ? OverlayManager.init_bk().update_Z(overlay) || OverlayManager.init_bk().write_Z(overlay) : true;
   }

   private autismclient.gui.f.NetHelper init(OverlayBounds bounds) {
      return autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, 14);
   }

   private autismclient.gui.f.NetHelper load(OverlayBounds bounds) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.load(this.init(bounds), 14);
   }

   private autismclient.gui.f.NetHelper read(OverlayBounds bounds) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.read(this.init(bounds), 14);
   }

   private boolean poll_Z_17() {
      return this instanceof AutismOverlay overlay && overlay.load_Z();
   }
}
