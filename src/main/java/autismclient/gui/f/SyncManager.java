package autismclient.gui.f;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SyncManager {
   private final GuiGraphicsExtractor init_GuiGraphicsExtract;
   private final UiTheme init_TableIndex;
   private final UiTextRenderer init_PoolEntry;
   private final int init_I;
   private final int load;
   private final int read;
   private final int write;
   private final float init_F;
   private final boolean init_Z;

   public SyncManager(GuiGraphicsExtractor graphics, UiTheme theme, UiTextRenderer text, int screenWidth, int screenHeight, int mouseX, int mouseY, float delta) {
      this(graphics, theme, text, screenWidth, screenHeight, mouseX, mouseY, delta, false);
   }

   private SyncManager(
      GuiGraphicsExtractor graphics,
      UiTheme theme,
      UiTextRenderer text,
      int screenWidth,
      int screenHeight,
      int mouseX,
      int mouseY,
      float delta,
      boolean flatSurfaces
   ) {
      this.init_GuiGraphicsExtract = graphics;
      this.init_TableIndex = theme;
      this.init_PoolEntry = text;
      this.init_I = screenWidth;
      this.load = screenHeight;
      this.read = mouseX;
      this.write = mouseY;
      this.init_F = delta;
      this.init_Z = flatSurfaces;
   }

   public GuiGraphicsExtractor init_GuiGraphicsExtract() {
      return this.init_GuiGraphicsExtract;
   }

   public UiTheme init_TableIndex() {
      return this.init_TableIndex;
   }

   public UiTextRenderer init_PoolEntry() {
      return this.init_PoolEntry;
   }

   public int init_I() {
      return this.init_I;
   }

   public int load_I() {
      return this.load;
   }

   public int read() {
      return this.read;
   }

   public int write() {
      return this.write;
   }

   public float init_F() {
      return this.init_F;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public SyncManager init_SyncManager() {
      return this.init_Z
         ? this
         : new SyncManager(
            this.init_GuiGraphicsExtract, this.init_TableIndex, this.init_PoolEntry, this.init_I, this.load, this.read, this.write, this.init_F, true
         );
   }

   public SyncManager load_SyncManager() {
      return new SyncManager(
         this.init_GuiGraphicsExtract, this.init_TableIndex, this.init_PoolEntry, this.init_I, this.load, -2147483648, -2147483648, this.init_F, this.init_Z
      );
   }
}
