package autismclient.gui.ui.widgets;

import autismclient.AutismClientAddon;
import autismclient.util.bK;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayLayoutBase;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class OverlayWindowLayerAdapter implements autismclient.gui.ui.UiLayerComponent {
   private final AutismOverlay overlay;
   private final autismclient.gui.ui.VisibilityTransition visibility;
   private boolean init_Z;
   private boolean hoverBlocked;
   private boolean inputSuppressed;
   private boolean zoomEnabled = true;
   private static long init_J;

   public OverlayWindowLayerAdapter(AutismOverlay overlay) {
      this.overlay = overlay;
      this.visibility = new autismclient.gui.ui.VisibilityTransition(overlay.isVisible());
   }

   public void setZoomEnabled(boolean zoom) {
      this.zoomEnabled = zoom;
   }

   public void setRenderSuppressed(boolean renderSuppressed) {
      this.init_Z = renderSuppressed;
   }

   public void animateVisibility() {
      this.visibility.animateTo(this.overlay.isVisible());
   }

   public void snapVisibility() {
      this.visibility.snapTo(this.overlay.isVisible());
   }

   public void restartVisibility() {
      this.visibility.restart();
   }

   public boolean isFadingOut() {
      return this.visibility.isFadingOut(this.overlay.isVisible());
   }

   public boolean shouldRender() {
      return this.visibility.shouldRender(this.overlay.isVisible());
   }

   public void setHoverBlocked(boolean hoverBlocked) {
      this.hoverBlocked = hoverBlocked;
   }

   public void setInputSuppressed(boolean inputSuppressed) {
      this.inputSuppressed = inputSuppressed;
   }

   @Override
   public autismclient.gui.f.NetHelper getBounds() {
      OverlayBounds bounds = this.overlay.getBounds();
      return autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, bounds.collapsed ? OverlayLayoutBase.headerHeight() : bounds.height);
   }

   @Override
   public void setBounds(autismclient.gui.f.NetHelper bounds) {
      if (bounds != null) {
         OverlayBounds current = this.overlay.getBounds();
         this.overlay.setBounds(new OverlayBounds(bounds.read(), bounds.write(), bounds.update(), bounds.sync(), current.visible, current.collapsed));
      }
   }

   @Override
   public autismclient.gui.f.NetHelper getInteractiveBounds() {
      return !this.inputSuppressed && this.overlay.isVisible() ? this.getBounds() : null;
   }

   @Override
   public void render(autismclient.gui.f.SyncManager context) {
      if (!this.init_Z) {
         boolean visible = this.overlay.isVisible();
         if (this.visibility.shouldRender(visible)) {
            boolean block = !visible || this.hoverBlocked;
            long perfStart = bK.load_J();
            autismclient.gui.f.NetHelper b = this.getBounds();
            GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
            int scissorDepth = autismclient.gui.ui.UiClipStack.getInstance().depth();

            try {
               this.visibility.render(graphics, b, this.zoomEnabled, () -> this.renderContent(context, block));
            } catch (Throwable t) {
               long nowMs = System.currentTimeMillis();
               if (nowMs - init_J > 5000L) {
                  init_J = nowMs;
                  AutismClientAddon.LOG.warn("[Overlay] {} failed to draw (visible={})", this.overlay.getOverlayId(), visible, t);
               }
            } finally {
               autismclient.gui.ui.UiClipStack.getInstance().popToDepth(graphics, scissorDepth);
            }

            if (perfStart != 0L) {
               bK.init("overlay." + this.overlay.getOverlayId(), perfStart);
            }
         }
      }
   }

   private void renderContent(autismclient.gui.f.SyncManager context, boolean block) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      int mouseX = block ? -10000 : context.read();
      int mouseY = block ? -10000 : context.write();
      boolean clip = this.overlay instanceof OverlayLayoutBase window && window.isCollapseAnimating() && window.lastAnimatedHeight() > 0;
      if (clip) {
         OverlayBounds bounds = this.overlay.getBounds();
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(graphics, autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, ((OverlayLayoutBase)this.overlay).lastAnimatedHeight()));
      }

      try {
         this.overlay.render(graphics, mouseX, mouseY, context.init_F());
      } finally {
         if (clip) {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }
      }

      this.init_V_SyncManager(context, mouseX, mouseY);
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, int mouseX, int mouseY) {
      if (this.overlay.isResizable() && this.overlay.isVisible() && !this.overlay.isCollapsed() && !(this.overlay instanceof OverlayLayoutBase window && window.isCollapseAnimating())) {
         OverlayBounds bounds = this.overlay.getBounds();
         boolean active = OverlayManager.getInstance().isResizing(this.overlay);
         boolean hover = !active && ResizeGripRenderer.init(bounds, mouseX, mouseY);
         ResizeGripRenderer.init(context.init_GuiGraphicsExtract(), context.init_TableIndex().colors(), bounds, hover, active);
         if (hover || active) {
            ResizeGripRenderer.init(context.init_GuiGraphicsExtract());
         }
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseClicked(int mouseX, int mouseY, int button) {
      this.overlay.mouseClicked(mouseX, mouseY, button);
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseReleased(int mouseX, int mouseY, int button) {
      return toResult(this.overlay.mouseReleased(mouseX, mouseY, button));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return toResult(this.overlay.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseScrolled(int mouseX, int mouseY, double amount) {
      this.overlay.mouseScrolled(mouseX, mouseY, amount);
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult onKeyPressed(int key, int scanCode, int modifiers) {
      return toResult(this.overlay.keyPressed(key, scanCode, modifiers));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onCharTyped(char chr) {
      return this.typeChar(chr, 0);
   }

   public autismclient.gui.ui.UiInputResult typeChar(char chr, int modifiers) {
      return toResult(this.overlay.charTyped(chr, modifiers));
   }

   private static autismclient.gui.ui.UiInputResult toResult(boolean handled) {
      return handled ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
   }
}
