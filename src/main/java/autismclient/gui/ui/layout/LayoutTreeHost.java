package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.FocusableInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class LayoutTreeHost {
   private final autismclient.gui.f.b.CharEncoder theme;
   private final LayoutNode root;
   private final float density;
   private long lastLayoutRevision = -9223372036854775808L;
   private float lastViewportWidth = 0.0F / 0.0F;
   private float lastViewportHeight = 0.0F / 0.0F;
   private float lastRootX = 0.0F / 0.0F;
   private float lastRootY = 0.0F / 0.0F;
   private float lastRootWidth = 0.0F / 0.0F;
   private float lastRootHeight = 0.0F / 0.0F;

   public LayoutTreeHost(autismclient.gui.f.b.CharEncoder theme, LayoutNode root) {
      this(theme, root, 2.0F);
   }

   public LayoutTreeHost(autismclient.gui.f.b.CharEncoder theme, LayoutNode root, float density) {
      this.theme = theme == null ? new autismclient.gui.f.b.CharEncoder() : theme;
      this.root = root;
      this.density = density <= 0.0F ? 2.0F : density;
   }

   public LayoutViewport createViewport() {
      return LayoutViewport.create(this.density);
   }

   public void render(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
      if (this.root != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            viewport.pushTransform(drawContext);

            try {
               LayoutContext context = new LayoutContext(
                  drawContext, mc.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), delta
               );
               this.ensureLayout(context, viewport);
               this.root.render(context);
            } finally {
               viewport.popTransform(drawContext);
            }
         }
      }
   }

   public void invalidateLayout() {
      this.lastLayoutRevision = -9223372036854775808L;
   }

   private void ensureLayout(LayoutContext context, LayoutViewport viewport) {
      long revision = LayoutNode.revision();
      boolean viewportChanged = Float.compare(this.lastViewportWidth, viewport.width()) != 0 || Float.compare(this.lastViewportHeight, viewport.height()) != 0;
      boolean rootBoundsChanged = Float.compare(this.lastRootX, this.root.x()) != 0
         || Float.compare(this.lastRootY, this.root.y()) != 0
         || Float.compare(this.lastRootWidth, this.root.width()) != 0
         || Float.compare(this.lastRootHeight, this.root.height()) != 0;
      if (viewportChanged || rootBoundsChanged || revision != this.lastLayoutRevision) {
         this.root.layout(context);
         this.lastLayoutRevision = LayoutNode.revision();
         this.lastViewportWidth = viewport.width();
         this.lastViewportHeight = viewport.height();
         this.lastRootX = this.root.x();
         this.lastRootY = this.root.y();
         this.lastRootWidth = this.root.width();
         this.lastRootHeight = this.root.height();
      }
   }

   public LayoutContext createMeasureContext() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.font != null) {
         LayoutViewport viewport = this.createViewport();
         return new LayoutContext(null, mc.font, viewport, this.theme, 0.0F, 0.0F, 0.0F);
      } else {
         return null;
      }
   }

   public float measureHeight(float availableWidth) {
      if (this.root == null) {
         return 0.0F;
      }

      LayoutContext context = this.createMeasureContext();
      return context == null ? 0.0F : this.root.measureHeight(context, availableWidth);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
            return this.root.mouseClicked(context, viewport.toUiX(mouseX), viewport.toUiY(mouseY), button);
         } else {
            return false;
         }
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
            return this.root.mouseReleased(context, viewport.toUiX(mouseX), viewport.toUiY(mouseY), button);
         } else {
            return false;
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
            return this.root
               .mouseDragged(
                  context, viewport.toUiX(mouseX), viewport.toUiY(mouseY), button, (float)(deltaX / viewport.scaleX()), (float)(deltaY / viewport.scaleY())
               );
         } else {
            return false;
         }
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, viewport.toUiX(mouseX), viewport.toUiY(mouseY), 0.0F);
            return this.root.mouseScrolled(context, viewport.toUiX(mouseX), viewport.toUiY(mouseY), (float)amount);
         } else {
            return false;
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, 0.0F, 0.0F, 0.0F);
            return this.root.keyPressed(context, keyCode, scanCode, modifiers);
         } else {
            return false;
         }
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.root == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            LayoutViewport viewport = this.createViewport();
            LayoutContext context = new LayoutContext(null, mc.font, viewport, this.theme, 0.0F, 0.0F, 0.0F);
            return this.root.charTyped(context, chr, modifiers);
         } else {
            return false;
         }
      }
   }

   public boolean hasFocusedInput() {
      return this.hasFocusedInputIn(this.root);
   }

   public void clearFocus() {
      this.clearFocusIn(this.root);
   }

   private boolean hasFocusedInputIn(LayoutNode node) {
      if (node != null && node.isVisible()) {
         if (node instanceof FocusableInput input && input.isFocused()) {
            return true;
         } else {
            if (node instanceof LayoutContainer container) {
               for (LayoutNode child : container.getChildren()) {
                  if (this.hasFocusedInputIn(child)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private void clearFocusIn(LayoutNode node) {
      if (node != null && node.isVisible()) {
         if (node instanceof FocusableInput input) {
            input.setFocused(false);
         }

         if (node instanceof LayoutContainer container) {
            for (LayoutNode child : container.getChildren()) {
               this.clearFocusIn(child);
            }
         }
      }
   }
}
