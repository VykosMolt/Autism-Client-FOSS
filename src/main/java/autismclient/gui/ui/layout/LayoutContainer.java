package autismclient.gui.ui.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LayoutContainer extends LayoutNode {
   protected final List<LayoutNode> children = new ArrayList<>();

   public <T extends LayoutNode> T addChild(T child) {
      if (child != null) {
         this.children.add(child);
         this.markLayoutDirty();
      }

      return child;
   }

   public void clearChildren() {
      if (!this.children.isEmpty()) {
         this.children.clear();
         this.markLayoutDirty();
      }
   }

   public List<LayoutNode> getChildren() {
      return Collections.unmodifiableList(this.children);
   }

   protected abstract void layoutChildren(LayoutContext var1);

   @Override
   public void layout(LayoutContext context) {
      if (this.nodeVisible) {
         this.layoutChildren(context);

         for (LayoutNode child : this.children) {
            if (child != null && child.isVisible()) {
               child.layout(context);
            }
         }
      }
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         for (LayoutNode child : this.children) {
            if (child != null && child.isVisible()) {
               child.render(context);
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.containsPoint(mouseX, mouseY) && child.mouseClicked(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseReleased(LayoutContext context, float mouseX, float mouseY, int button) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.mouseReleased(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseDragged(LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.mouseDragged(context, mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseScrolled(LayoutContext context, float mouseX, float mouseY, float amount) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.containsPoint(mouseX, mouseY) && child.mouseScrolled(context, mouseX, mouseY, amount)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean keyPressed(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.keyPressed(context, keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean charTyped(LayoutContext context, char chr, int modifiers) {
      if (!this.nodeVisible) {
         return false;
      }

      for (int i = this.children.size() - 1; i >= 0; i--) {
         LayoutNode child = this.children.get(i);
         if (child != null && child.isVisible() && child.charTyped(context, chr, modifiers)) {
            return true;
         }
      }

      return false;
   }
}
