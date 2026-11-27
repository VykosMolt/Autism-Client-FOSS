package autismclient.gui.ui.layout;

public abstract class LayoutNode {
   private static volatile long layoutRevision = 1L;
   protected float nodeX;
   protected float nodeY;
   protected float nodeWidth;
   protected float nodeHeight;
   protected boolean nodeVisible = true;
   protected boolean nodeEnabled = true;
   protected boolean nodeGrowX = false;
   protected float hoverProgress = 0.0F;

   public static long revision() {
      return layoutRevision;
   }

   protected void markLayoutDirty() {
      layoutRevision++;
   }

   public float x() {
      return this.nodeX;
   }

   public float y() {
      return this.nodeY;
   }

   public float width() {
      return this.nodeWidth;
   }

   public float height() {
      return this.nodeHeight;
   }

   public LayoutNode setBounds(float x, float y, float width, float height) {
      float nextWidth = Math.max(0.0F, width);
      float nextHeight = Math.max(0.0F, height);
      if (Float.compare(this.nodeX, x) != 0
         || Float.compare(this.nodeY, y) != 0
         || Float.compare(this.nodeWidth, nextWidth) != 0
         || Float.compare(this.nodeHeight, nextHeight) != 0) {
         this.nodeX = x;
         this.nodeY = y;
         this.nodeWidth = nextWidth;
         this.nodeHeight = nextHeight;
         this.markLayoutDirty();
      }

      return this;
   }

   public LayoutNode setVisible(boolean visible) {
      if (this.nodeVisible != visible) {
         this.nodeVisible = visible;
         this.markLayoutDirty();
      }

      return this;
   }

   public LayoutNode setEnabled(boolean enabled) {
      if (this.nodeEnabled != enabled) {
         this.nodeEnabled = enabled;
         this.markLayoutDirty();
      }

      return this;
   }

   public LayoutNode setGrowX(boolean growX) {
      if (this.nodeGrowX != growX) {
         this.nodeGrowX = growX;
         this.markLayoutDirty();
      }

      return this;
   }

   public boolean isVisible() {
      return this.nodeVisible;
   }

   public boolean isEnabled() {
      return this.nodeEnabled;
   }

   public boolean isGrowX() {
      return this.nodeGrowX;
   }

   public boolean containsPoint(float px, float py) {
      return this.nodeVisible && px >= this.nodeX && px <= this.nodeX + this.nodeWidth && py >= this.nodeY && py <= this.nodeY + this.nodeHeight;
   }

   protected float stepTowards(float current, float target, float speed, float delta) {
      float amount = Math.max(0.04F, delta * speed);
      if (current < target) {
         return Math.min(target, current + amount);
      } else {
         return current > target ? Math.max(target, current - amount) : current;
      }
   }

   protected float stepHover(boolean hovered, float delta) {
      this.hoverProgress = this.stepTowards(this.hoverProgress, hovered ? 1.0F : 0.0F, 7.5F, delta);
      return this.hoverProgress;
   }

   public float measurePreferredWidth(LayoutContext context) {
      return this.nodeWidth;
   }

   public float measureHeight(LayoutContext context, float availableWidth) {
      return this.nodeHeight;
   }

   public void layout(LayoutContext context) {
   }

   public void render(LayoutContext context) {
   }

   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      return false;
   }

   public boolean mouseReleased(LayoutContext context, float mouseX, float mouseY, int button) {
      return false;
   }

   public boolean mouseDragged(LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      return false;
   }

   public boolean mouseScrolled(LayoutContext context, float mouseX, float mouseY, float amount) {
      return false;
   }

   public boolean keyPressed(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      return false;
   }

   public boolean charTyped(LayoutContext context, char chr, int modifiers) {
      return false;
   }
}
