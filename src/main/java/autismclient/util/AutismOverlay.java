package autismclient.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface AutismOverlay {
   void render(GuiGraphicsExtractor var1, int var2, int var3, float var4);

   boolean mouseClicked(double var1, double var3, int var5);

   boolean mouseReleased(double var1, double var3, int var5);

   boolean mouseDragged(double var1, double var3, int var5, double var6, double var8);

   boolean mouseScrolled(double var1, double var3, double var5);

   boolean keyPressed(int var1, int var2, int var3);

   boolean charTyped(char var1, int var2);

   boolean isVisible();

   void setVisible(boolean var1);

   default boolean isClosing() {
      return OverlayManager.getInstance().isClosing(this);
   }

   boolean containsPoint(double var1, double var3);

   boolean isOverHeader(double var1, double var3);

   default int getZPriority() {
      return 0;
   }

   default boolean isCollapsed() {
      return false;
   }

   default void setCollapsed(boolean collapsed) {
   }

   default void toggleCollapsed() {
      this.setCollapsed(!this.isCollapsed());
   }

   default boolean hasFocusedTextInput() {
      return false;
   }

   default void clearInputFocus() {
   }

   default boolean isCapturingKeybind() {
      return false;
   }

   default String getOverlayId() {
      return this.getClass().getSimpleName();
   }

   default AutismOverlay.RenderLayer getRenderLayer() {
      return AutismOverlay.RenderLayer.HOST_SCREEN;
   }

   default boolean isPersistent() {
      return false;
   }

   default boolean shouldRememberOnDismiss() {
      return false;
   }

   default OverlayBounds getBounds() {
      return new OverlayBounds(0, 0, this.getMinWidth(), this.getMinHeight(), this.isVisible(), this.isCollapsed());
   }

   default void setBounds(OverlayBounds bounds) {
   }

   default int getMinWidth() {
      return 240;
   }

   default int getMinHeight() {
      return 140;
   }

   default boolean isResizable() {
      return false;
   }

   default boolean isOverResizeGrip(double mouseX, double mouseY) {
      return this.isResizable() && this.isVisible() && !this.isCollapsed() ? autismclient.gui.ui.widgets.ResizeGripRenderer.hovered(this.getBounds(), mouseX, mouseY) : false;
   }

   default void resizeTo(int width, int height) {
      OverlayBounds b = this.getBounds();
      this.setBounds(new OverlayBounds(b.x, b.y, Math.max(this.getMinWidth(), width), Math.max(this.getMinHeight(), height), b.visible, b.collapsed));
   }

   default void onResizeFinished() {
   }

   default void resetSize() {
   }

   default boolean supportsHeaderCollapseClick() {
      return false;
   }

   default void saveBounds() {
      AutismSessionState.getInstance().setOverlayBounds(this.getOverlayId(), this.getBounds());
   }

   default void loadBounds() {
      OverlayBounds layout = AutismSessionState.getInstance().getOverlayBounds(this.getOverlayId());
      if (layout != null) {
         this.setBounds(layout);
      }
   }

   enum RenderLayer {
      HOST_SCREEN,
      MODULE_MENU,
      CONTAINER_GUI,
      BACKGROUND_STATUS;
   }
}
