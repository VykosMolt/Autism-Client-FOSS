package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class OverlayLayoutBase {
   protected static final int HEADER_HEIGHT = 14;
   protected static final int WINDOW_RADIUS = 8;
   private autismclient.gui.ui.EaseOutAnimator collapseAnimator;
   private int animatedHeight = -1;

   public static int headerHeight() {
      return 14;
   }

   protected OverlayBounds clampCurrentBounds(AutismOverlay overlay) {
      return this.clampToScreen(overlay, overlay.getBounds());
   }

   protected OverlayBounds clampToScreen(AutismOverlay overlay, OverlayBounds bounds) {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.getWindow() != null && bounds != null ? clampBounds(bounds, overlay.getMinWidth(), overlay.getMinHeight(), cA.init_I(), cA.load_I()) : bounds;
   }

   static OverlayBounds clampBounds(OverlayBounds bounds, int overlayMinWidth, int overlayMinHeight, int screenWidth, int screenHeight) {
      if (bounds == null) {
         return null;
      } else if (screenWidth > 0 && screenHeight > 0) {
         int safeMargin = 4;
         int availableWidth = Math.max(1, screenWidth - safeMargin * 2);
         int availableHeight = Math.max(14, screenHeight - safeMargin * 2);
         int minWidth = Math.min(overlayMinWidth, availableWidth);
         int minHeight = Math.min(overlayMinHeight, availableHeight);
         int width = Math.max(minWidth, Math.min(bounds.width, availableWidth));
         int height = Math.max(minHeight, Math.min(bounds.height, availableHeight));
         int renderedHeight = bounds.collapsed ? 14 : height;
         int x = Math.max(safeMargin, Math.min(bounds.x, Math.max(safeMargin, screenWidth - safeMargin - width)));
         int y = Math.max(safeMargin, Math.min(bounds.y, Math.max(safeMargin, screenHeight - safeMargin - renderedHeight)));
         return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
      } else {
         return bounds;
      }
   }

   protected int animateFrameHeight(OverlayBounds bounds, boolean collapsed) {
      if (bounds == null) {
         return 14;
      }

      int full = Math.max(14, bounds.height);
      float target = collapsed ? 0.0F : 1.0F;
      if (this.collapseAnimator == null) {
         this.collapseAnimator = new autismclient.gui.ui.EaseOutAnimator(target, 180);
      }

      float reveal = this.collapseAnimator.animateTo(target);
      this.animatedHeight = 14 + Math.round((full - 14) * reveal);
      return this.animatedHeight;
   }

   protected void snapCollapseState(boolean collapsed) {
      if (this.collapseAnimator != null) {
         this.collapseAnimator.snapTo(collapsed ? 0.0F : 1.0F);
      }
   }

   public boolean isCollapseAnimating() {
      return this.collapseAnimator != null && this.collapseAnimator.isAnimating();
   }

   public int lastAnimatedHeight() {
      return this.animatedHeight;
   }

   protected boolean isVisuallyCollapsed(boolean collapsed) {
      return this.animatedHeight < 0 ? collapsed : this.animatedHeight <= 15;
   }

   protected void renderWindowFrame(
      GuiGraphicsExtractor context, int mouseX, int mouseY, OverlayBounds bounds, String title, boolean collapsed, boolean activeDrag
   ) {
      this.renderWindowFrame(autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, mouseX, mouseY), bounds, title, collapsed, activeDrag);
   }

   protected void renderWindowFrame(autismclient.gui.f.SyncManager ui, OverlayBounds bounds, String title, boolean collapsed, boolean activeDrag) {
      boolean active = activeDrag || this.isWindowActive();
      int frameHeight = this.animateFrameHeight(bounds, collapsed);
      autismclient.gui.ui.widgets.OverlayFrameHelper.drawFrame(
         ui,
         autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, frameHeight),
         14,
         title,
         collapsed,
         active,
         ui.write() >= bounds.y && ui.write() < bounds.y + 14
      );
   }

   protected boolean beginBodyClip(GuiGraphicsExtractor context, OverlayBounds bounds, boolean collapsed) {
      int frameHeight = this.animateFrameHeight(bounds, collapsed);
      return frameHeight <= 15
         ? false
         : autismclient.gui.ui.widgets.OverlayFrameHelper.beginClip(context, autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, frameHeight), 14, false);
   }

   protected void endBodyClip(GuiGraphicsExtractor context, boolean clipped) {
      autismclient.gui.ui.widgets.OverlayFrameHelper.endClip(context, clipped);
   }

   protected void renderInactiveDim(GuiGraphicsExtractor context, OverlayBounds bounds, boolean collapsed, boolean activeDrag) {
      int frameHeight = this.animateFrameHeight(bounds, collapsed);
      if (!activeDrag && !this.isWindowActive()) {
         autismclient.gui.sdf.SdfShapeRenderer.fillRoundRect(
            context, bounds.x + 1, bounds.y + 1, Math.max(0, bounds.width - 2), Math.max(0, frameHeight - 2), 7.0F, 603979776
         );
      }
   }

   protected int alignHeightToStep(int innerHeight, int rowStep) {
      int safeInnerHeight = Math.max(0, innerHeight);
      int safeRowStep = Math.max(1, rowStep);
      if (safeInnerHeight != 0 && safeRowStep > 1) {
         int aligned = safeInnerHeight / safeRowStep * safeRowStep;
         return aligned > 0 ? aligned : Math.min(safeInnerHeight, safeRowStep);
      } else {
         return safeInnerHeight;
      }
   }

   protected int quantizeScrollOffset(int offset, int stepSize, int maxScroll) {
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

   protected int renderedHeight(OverlayBounds bounds, boolean collapsed) {
      if (bounds == null) {
         return 14;
      } else {
         return collapsed ? 14 : Math.max(14, bounds.height);
      }
   }

   protected boolean isOverHeaderCloseButton(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.headerCloseButtonBounds(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected boolean isOverHeaderCollapseToggle(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.isClickHeaderCollapse() ? false : this.headerToggleBounds(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected boolean isOverHeaderControl(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.isOverHeaderCloseButton(mouseX, mouseY, bounds) || this.isOverHeaderCollapseToggle(mouseX, mouseY, bounds);
   }

   private boolean isWindowActive() {
      return this instanceof AutismOverlay overlay ? OverlayManager.getInstance().isFocused(overlay) || OverlayManager.getInstance().isTopmost(overlay) : true;
   }

   private autismclient.gui.f.NetHelper headerBounds(OverlayBounds bounds) {
      return autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, 14);
   }

   private autismclient.gui.f.NetHelper headerCloseButtonBounds(OverlayBounds bounds) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.load(this.headerBounds(bounds), 14);
   }

   private autismclient.gui.f.NetHelper headerToggleBounds(OverlayBounds bounds) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.read(this.headerBounds(bounds), 14);
   }

   private boolean isClickHeaderCollapse() {
      return this instanceof AutismOverlay overlay && overlay.supportsHeaderCollapseClick();
   }
}
