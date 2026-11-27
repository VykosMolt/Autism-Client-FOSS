package autismclient.gui.ui.widgets;

import autismclient.AutismClientAddon;
import autismclient.util.bK;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayLayoutBase;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class OverlayWindowLayerAdapter implements autismclient.gui.ui.UiLayerComponent {
   private final AutismOverlay init_cX;
   private final autismclient.gui.ui.VisibilityTransition init_CharEncoder;
   private boolean init_Z;
   private boolean load;
   private boolean read;
   private boolean write = true;
   private static long init_J;

   public OverlayWindowLayerAdapter(AutismOverlay overlay) {
      this.init_cX = overlay;
      this.init_CharEncoder = new autismclient.gui.ui.VisibilityTransition(overlay.isVisible());
   }

   public void init_V_Z(boolean zoom) {
      this.write = zoom;
   }

   public void load(boolean renderSuppressed) {
      this.init_Z = renderSuppressed;
   }

   public void init_V() {
      this.init_CharEncoder.init_V(this.init_cX.isVisible());
   }

   public void load_V() {
      this.init_CharEncoder.load_V(this.init_cX.isVisible());
   }

   public void read() {
      this.init_CharEncoder.init_V();
   }

   public boolean init_Z() {
      return this.init_CharEncoder.init_Z(this.init_cX.isVisible());
   }

   public boolean load_Z() {
      return this.init_CharEncoder.load_Z(this.init_cX.isVisible());
   }

   public void read(boolean hoverBlocked) {
      this.load = hoverBlocked;
   }

   public void write(boolean inputSuppressed) {
      this.read = inputSuppressed;
   }

   @Override
   public autismclient.gui.f.NetHelper getBounds() {
      OverlayBounds bounds = this.init_cX.getBounds();
      return autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, bounds.collapsed ? OverlayLayoutBase.headerHeight() : bounds.height);
   }

   @Override
   public void setBounds(autismclient.gui.f.NetHelper bounds) {
      if (bounds != null) {
         OverlayBounds current = this.init_cX.getBounds();
         this.init_cX.setBounds(new OverlayBounds(bounds.read(), bounds.write(), bounds.update(), bounds.sync(), current.visible, current.collapsed));
      }
   }

   @Override
   public autismclient.gui.f.NetHelper getInteractiveBounds() {
      return !this.read && this.init_cX.isVisible() ? this.getBounds() : null;
   }

   @Override
   public void render(autismclient.gui.f.SyncManager context) {
      if (!this.init_Z) {
         boolean visible = this.init_cX.isVisible();
         if (this.init_CharEncoder.load_Z(visible)) {
            boolean block = !visible || this.load;
            long perfStart = bK.load_J();
            autismclient.gui.f.NetHelper b = this.getBounds();
            GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
            int scissorDepth = autismclient.gui.ui.UiClipStack.getInstance().depth();

            try {
               this.init_CharEncoder.init(graphics, b, this.write, () -> this.init(context, block));
            } catch (Throwable t) {
               long nowMs = System.currentTimeMillis();
               if (nowMs - init_J > 5000L) {
                  init_J = nowMs;
                  AutismClientAddon.LOG.warn("[Overlay] {} failed to draw (visible={})", this.init_cX.getOverlayId(), visible, t);
               }
            } finally {
               autismclient.gui.ui.UiClipStack.getInstance().popToDepth(graphics, scissorDepth);
            }

            if (perfStart != 0L) {
               bK.init("overlay." + this.init_cX.getOverlayId(), perfStart);
            }
         }
      }
   }

   private void init(autismclient.gui.f.SyncManager context, boolean block) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      int mouseX = block ? -10000 : context.read();
      int mouseY = block ? -10000 : context.write();
      boolean clip = this.init_cX instanceof OverlayLayoutBase window && window.isCollapseAnimating() && window.lastAnimatedHeight() > 0;
      if (clip) {
         OverlayBounds bounds = this.init_cX.getBounds();
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(graphics, autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, ((OverlayLayoutBase)this.init_cX).lastAnimatedHeight()));
      }

      try {
         this.init_cX.render(graphics, mouseX, mouseY, context.init_F());
      } finally {
         if (clip) {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }
      }

      this.init_V_SyncManager(context, mouseX, mouseY);
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, int mouseX, int mouseY) {
      if (this.init_cX.isResizable() && this.init_cX.isVisible() && !this.init_cX.isCollapsed() && !(this.init_cX instanceof OverlayLayoutBase window && window.isCollapseAnimating())) {
         OverlayBounds bounds = this.init_cX.getBounds();
         boolean active = OverlayManager.getInstance().isResizing(this.init_cX);
         boolean hover = !active && ResizeGripRenderer.init(bounds, mouseX, mouseY);
         ResizeGripRenderer.init(context.init_GuiGraphicsExtract(), context.init_TableIndex().colors(), bounds, hover, active);
         if (hover || active) {
            ResizeGripRenderer.init(context.init_GuiGraphicsExtract());
         }
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseClicked(int mouseX, int mouseY, int button) {
      this.init_cX.mouseClicked(mouseX, mouseY, button);
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseReleased(int mouseX, int mouseY, int button) {
      return init_EventBus_Z(this.init_cX.mouseReleased(mouseX, mouseY, button));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return init_EventBus_Z(this.init_cX.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onMouseScrolled(int mouseX, int mouseY, double amount) {
      this.init_cX.mouseScrolled(mouseX, mouseY, amount);
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult onKeyPressed(int key, int scanCode, int modifiers) {
      return init_EventBus_Z(this.init_cX.keyPressed(key, scanCode, modifiers));
   }

   @Override
   public autismclient.gui.ui.UiInputResult onCharTyped(char chr) {
      return this.init(chr, 0);
   }

   public autismclient.gui.ui.UiInputResult init(char chr, int modifiers) {
      return init_EventBus_Z(this.init_cX.charTyped(chr, modifiers));
   }

   private static autismclient.gui.ui.UiInputResult init_EventBus_Z(boolean handled) {
      return handled ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
   }
}
