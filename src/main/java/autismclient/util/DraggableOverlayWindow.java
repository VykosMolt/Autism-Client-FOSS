package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class DraggableOverlayWindow extends OverlayLayoutBase implements AutismOverlay {
   private final String overlayId;
   protected int windowX;
   protected int windowY;
   protected int windowWidth;
   protected int windowHeight;
   protected boolean windowVisible;
   protected boolean windowCollapsed;
   private OverlayContextMenu<?> activeContextMenu;
   protected boolean windowPinned;
   private OverlayWindowSizeStore windowSizeStore;
   private int lastMouseX;
   private int lastMouseY;

   protected DraggableOverlayWindow() {
      this(null, 0, 0);
   }

   protected DraggableOverlayWindow(String overlayId, int initialWidth, int initialHeight) {
      this.overlayId = overlayId;
      this.windowWidth = initialWidth;
      this.windowHeight = initialHeight;
      this.windowPinned = AutismSessionState.getInstance().isOverlayPinned(this.getOverlayId());
   }

   protected boolean supportsPinning() {
      return false;
   }

   @Override
   public boolean isPersistent() {
      return this.windowPinned;
   }

   @Override
   public AutismOverlay.RenderLayer getRenderLayer() {
      return this.windowPinned ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : AutismOverlay.super.getRenderLayer();
   }

   protected void togglePinned() {
      this.windowPinned = !this.windowPinned;
      AutismSessionState.getInstance().setOverlayPinned(this.getOverlayId(), this.windowPinned);
      OverlayManager.getInstance().refreshRenderLayer(this);
      this.saveBounds();
   }

   public boolean isPinned() {
      return this.windowPinned;
   }

   public void setPinned(boolean value) {
      if (this.windowPinned != value) {
         this.togglePinned();
      }
   }

   protected autismclient.gui.f.NetHelper getPinButtonRect(OverlayBounds bounds) {
      autismclient.gui.f.NetHelper window = autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, 14);
      autismclient.gui.f.NetHelper bar = autismclient.gui.ui.widgets.WindowHeaderHelper.headerBounds(window, 14);
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.WindowHeaderHelper.closeButton(window, 14);
      int size = Math.min(12, Math.max(9, 10));
      int x = close.read() - 3 - size;
      int y = bar.write() + Math.max(1, (bar.sync() - size) / 2);
      return autismclient.gui.f.NetHelper.init(x, y, size, size);
   }

   protected boolean isOverPinButton(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.supportsPinning() && this.getPinButtonRect(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected void renderPinButton(GuiGraphicsExtractor context, int mouseX, int mouseY, OverlayBounds bounds) {
      if (this.supportsPinning()) {
         autismclient.gui.f.NetHelper pin = this.getPinButtonRect(bounds);
         autismclient.gui.ui.UiColorScheme colors = autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, mouseX, mouseY)
            .init_TableIndex()
            .colors();
         boolean hovered = pin.init_Z(mouseX, mouseY);
         int color = this.windowPinned ? colors.accent : (hovered ? colors.textPrimary : colors.textSecondary);
         GuiIconAtlas.draw(context, GuiIconAtlas.PIN, pin.read(), pin.write(), pin.update(), pin.sync(), color);
      }
   }

   @Override
   public String getOverlayId() {
      return this.overlayId != null ? this.overlayId : this.getClass().getSimpleName();
   }

   @Override
   public boolean isVisible() {
      return this.windowVisible;
   }

   @Override
   public void setVisible(boolean v) {
      this.windowVisible = v;
      if (!v) {
         this.clearFocusAndDismissMenu();
      }

      this.saveBounds();
   }

   @Override
   public boolean isCollapsed() {
      return this.windowCollapsed;
   }

   @Override
   public void setCollapsed(boolean c) {
      if (this.windowCollapsed != c) {
         this.windowCollapsed = c;
         if (c) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds c = this.clampToScreen(this, bounds);
         this.windowX = c.x;
         this.windowY = c.y;
         this.windowWidth = c.width;
         this.windowHeight = c.height;
         this.windowVisible = c.visible;
         if (this.windowCollapsed != c.collapsed) {
            this.snapCollapseState(c.collapsed);
         }

         this.windowCollapsed = c.collapsed;
      }
   }

   protected final OverlayWindowSizeStore getSizeStore() {
      if (this.windowSizeStore == null) {
         this.windowSizeStore = new OverlayWindowSizeStore(this.getOverlayId());
      }

      return this.windowSizeStore;
   }

   protected static int getMaxScreenWindowWidth() {
      return Math.max(1, cA.init_I() - 8);
   }

   protected static int getMaxScreenWindowHeight() {
      return Math.max(1, cA.load_I() - 8);
   }

   protected int resolveWindowWidth(int autoWidth, int minWidth) {
      return this.isResizable() ? this.getSizeStore().width(autoWidth, minWidth, getMaxScreenWindowWidth()) : autoWidth;
   }

   protected int resolveWindowHeight(int autoHeight, int minHeight) {
      return this.isResizable() ? this.getSizeStore().height(autoHeight, minHeight, getMaxScreenWindowHeight()) : autoHeight;
   }

   protected int getResizeMinWidth() {
      return this.getMinWidth();
   }

   protected int getResizeMinHeight() {
      return this.getMinHeight();
   }

   protected boolean hasCustomStoredSize() {
      return this.isResizable() && this.getSizeStore().hasSavedSize();
   }

   @Override
   public void resizeTo(int width, int height) {
      if (!this.isResizable()) {
         AutismOverlay.super.resizeTo(width, height);
      } else {
         OverlayBounds b = this.getBounds();
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         int minW = this.getResizeMinWidth();
         int minH = this.getResizeMinHeight();
         int w = OverlayWindowSizeStore.clamp(width, minW, screenW > 0 ? screenW - 4 - b.x : width);
         int h = OverlayWindowSizeStore.clamp(height, minH, screenH > 0 ? screenH - 4 - b.y : height);
         OverlayBounds clamped = clampBounds(new OverlayBounds(b.x, b.y, w, h, b.visible, b.collapsed), minW, minH, screenW, screenH);
         this.getSizeStore().save(clamped.width, clamped.height);
         this.setBounds(clamped);
         this.onSizeChanged();
      }
   }

   @Override
   public void onResizeFinished() {
      if (this.isResizable()) {
         this.getSizeStore().save();
      }
   }

   @Override
   public void resetSize() {
      if (this.isResizable()) {
         this.getSizeStore().clear();
         this.onSizeChanged();
      }
   }

   protected void onSizeChanged() {
   }

   @Override
   protected boolean isOverHeaderControl(double mouseX, double mouseY, OverlayBounds bounds) {
      return super.isOverHeaderControl(mouseX, mouseY, bounds) || this.isOverPinButton(mouseX, mouseY, bounds);
   }

   @Override
   public boolean containsPoint(double mx, double my) {
      if (!this.windowVisible) {
         return false;
      }

      int frameH = this.getRenderedFrameHeight();
      boolean overPanel = mx >= this.windowX && mx <= this.windowX + this.windowWidth && my >= this.windowY && my <= this.windowY + frameH;
      return this.activeContextMenu != null && this.activeContextMenu.containsPoint(mx, my) ? true : overPanel;
   }

   @Override
   public boolean isOverHeader(double mx, double my) {
      if (!this.windowVisible) {
         return false;
      } else if (!(mx < this.windowX) && !(mx > this.windowX + this.windowWidth)) {
         return !(my < this.windowY) && !(my > this.windowY + 14) ? !this.isOverHeaderControl(mx, my, this.getBounds()) : false;
      } else {
         return false;
      }
   }

   protected int getRenderedFrameHeight() {
      return this.windowCollapsed ? 14 : this.windowHeight;
   }

   protected void setActiveContextMenu(OverlayContextMenu<?> menu) {
      this.activeContextMenu = menu;
   }

   protected OverlayContextMenu<?> getActiveContextMenu() {
      return this.activeContextMenu;
   }

   protected final void clearFocusAndDismissMenu() {
      this.clearInputFocus();
      if (this.activeContextMenu != null) {
         this.activeContextMenu.close();
      }
   }

   protected int maskMouseCoordIfMenuCovers(int mouse, int axis) {
      if (this.activeContextMenu != null && this.activeContextMenu.isOpen()) {
         int otherAxis = axis == 0 ? this.lastMouseY : this.lastMouseX;
         boolean covered = axis == 0 ? this.activeContextMenu.containsPoint(mouse, otherAxis) : this.activeContextMenu.containsPoint(otherAxis, mouse);
         return covered ? -10000 : mouse;
      } else {
         return mouse;
      }
   }

   protected final void recordMousePosition(int mx, int my) {
      this.lastMouseX = mx;
      this.lastMouseY = my;
   }

   protected int maskMouseXIfMenuCovers(int mx, int my) {
      return this.activeContextMenu != null && this.activeContextMenu.containsPoint(mx, my) ? -10000 : mx;
   }

   protected int maskMouseYIfMenuCovers(int mx, int my) {
      return this.activeContextMenu != null && this.activeContextMenu.containsPoint(mx, my) ? -10000 : my;
   }
}
