package autismclient.gui.ui.layout;

public abstract class LayoutNode {
   private static volatile long init_J = 1L;
   protected float init_F;
   protected float load_F;
   protected float read_F;
   protected float write;
   protected boolean init_Z = true;
   protected boolean load_Z = true;
   protected boolean read_Z = false;
   protected float update = 0.0F;

   public static long init_J() {
      return init_J;
   }

   protected void load_V() {
      init_J++;
   }

   public float load_F() {
      return this.init_F;
   }

   public float read_F() {
      return this.load_F;
   }

   public float write_F() {
      return this.read_F;
   }

   public float update_F() {
      return this.write;
   }

   public LayoutNode init_TableIndex_F(float x, float y, float width, float height) {
      float nextWidth = Math.max(0.0F, width);
      float nextHeight = Math.max(0.0F, height);
      if (Float.compare(this.init_F, x) != 0
         || Float.compare(this.load_F, y) != 0
         || Float.compare(this.read_F, nextWidth) != 0
         || Float.compare(this.write, nextHeight) != 0) {
         this.init_F = x;
         this.load_F = y;
         this.read_F = nextWidth;
         this.write = nextHeight;
         this.load_V();
      }

      return this;
   }

   public LayoutNode load_TableIndex_Z(boolean visible) {
      if (this.init_Z != visible) {
         this.init_Z = visible;
         this.load_V();
      }

      return this;
   }

   public LayoutNode read_TableIndex_Z(boolean enabled) {
      if (this.load_Z != enabled) {
         this.load_Z = enabled;
         this.load_V();
      }

      return this;
   }

   public LayoutNode init_TableIndex_Z(boolean growX) {
      if (this.read_Z != growX) {
         this.read_Z = growX;
         this.load_V();
      }

      return this;
   }

   public boolean write_Z() {
      return this.init_Z;
   }

   public boolean update_Z() {
      return this.load_Z;
   }

   public boolean sync_Z() {
      return this.read_Z;
   }

   public boolean init_Z_F_F(float px, float py) {
      return this.init_Z && px >= this.init_F && px <= this.init_F + this.read_F && py >= this.load_F && py <= this.load_F + this.write;
   }

   protected float init_F_F(float current, float target, float speed, float delta) {
      float amount = Math.max(0.04F, delta * speed);
      if (current < target) {
         return Math.min(target, current + amount);
      } else {
         return current > target ? Math.max(target, current - amount) : current;
      }
   }

   protected float init_F_Z_F(boolean hovered, float delta) {
      this.update = this.init_F_F(this.update, hovered ? 1.0F : 0.0F, 7.5F, delta);
      return this.update;
   }

   public float init_F_SyncManager(LayoutContext context) {
      return this.read_F;
   }

   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      return this.write;
   }

   public void read_V_SyncManager(LayoutContext context) {
   }

   public void init_V_SyncManager(LayoutContext context) {
   }

   public boolean init_Z_SyncManager_2(LayoutContext context, float mouseX, float mouseY, int button) {
      return false;
   }

   public boolean load_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, int button) {
      return false;
   }

   public boolean init_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      return false;
   }

   public boolean init_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, float amount) {
      return false;
   }

   public boolean init_Z_SyncManager_3(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      return false;
   }

   public boolean init_Z_SyncManager_2(LayoutContext context, char chr, int modifiers) {
      return false;
   }
}
