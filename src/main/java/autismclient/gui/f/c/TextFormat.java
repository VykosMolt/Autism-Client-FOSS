package autismclient.gui.f.c;

import autismclient.gui.f.b.ListPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class TextFormat {
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder;
   private final TableIndex init_TableIndex;
   private final float init_F;
   private long init_J = -9223372036854775808L;
   private float load = 0.0F / 0.0F;
   private float read = 0.0F / 0.0F;
   private float write = 0.0F / 0.0F;
   private float update = 0.0F / 0.0F;
   private float sync = 0.0F / 0.0F;
   private float flush = 0.0F / 0.0F;

   public TextFormat(autismclient.gui.f.b.CharEncoder theme, TableIndex root) {
      this(theme, root, 2.0F);
   }

   public TextFormat(autismclient.gui.f.b.CharEncoder theme, TableIndex root, float density) {
      this.init_CharEncoder = theme == null ? new autismclient.gui.f.b.CharEncoder() : theme;
      this.init_TableIndex = root;
      this.init_F = density <= 0.0F ? 2.0F : density;
   }

   public RingBuffer init_RingBuffer() {
      return RingBuffer.init_RingBuffer_F(this.init_F);
   }

   public void init(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
      if (this.init_TableIndex != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            viewport.init_V_GuiGraphicsExtract(drawContext);

            try {
               SyncManager context = new SyncManager(
                  drawContext, mc.font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), delta
               );
               this.init(context, viewport);
               this.init_TableIndex.init_V_SyncManager(context);
            } finally {
               viewport.load(drawContext);
            }
         }
      }
   }

   public void init_V() {
      this.init_J = -9223372036854775808L;
   }

   private void init(SyncManager context, RingBuffer viewport) {
      long revision = TableIndex.init_J();
      boolean viewportChanged = Float.compare(this.load, viewport.init()) != 0 || Float.compare(this.read, viewport.load()) != 0;
      boolean rootBoundsChanged = Float.compare(this.write, this.init_TableIndex.load_F()) != 0
         || Float.compare(this.update, this.init_TableIndex.read_F()) != 0
         || Float.compare(this.sync, this.init_TableIndex.write_F()) != 0
         || Float.compare(this.flush, this.init_TableIndex.update_F()) != 0;
      if (viewportChanged || rootBoundsChanged || revision != this.init_J) {
         this.init_TableIndex.read_V_SyncManager(context);
         this.init_J = TableIndex.init_J();
         this.load = viewport.init();
         this.read = viewport.load();
         this.write = this.init_TableIndex.load_F();
         this.update = this.init_TableIndex.read_F();
         this.sync = this.init_TableIndex.write_F();
         this.flush = this.init_TableIndex.update_F();
      }
   }

   public SyncManager init_SyncManager() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.font != null) {
         RingBuffer viewport = this.init_RingBuffer();
         return new SyncManager(null, mc.font, viewport, this.init_CharEncoder, 0.0F, 0.0F, 0.0F);
      } else {
         return null;
      }
   }

   public float init(float availableWidth) {
      if (this.init_TableIndex == null) {
         return 0.0F;
      }

      SyncManager context = this.init_SyncManager();
      return context == null ? 0.0F : this.init_TableIndex.init_F_SyncManager_F(context, availableWidth);
   }

   public boolean init_Z_D_2(double mouseX, double mouseY, int button) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), 0.0F);
            return this.init_TableIndex.init_Z_SyncManager_2(context, viewport.init_F_D(mouseX), viewport.load(mouseY), button);
         } else {
            return false;
         }
      }
   }

   public boolean load(double mouseX, double mouseY, int button) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), 0.0F);
            return this.init_TableIndex.load_Z_SyncManager(context, viewport.init_F_D(mouseX), viewport.load(mouseY), button);
         } else {
            return false;
         }
      }
   }

   public boolean init(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), 0.0F);
            return this.init_TableIndex
               .init_Z_SyncManager(
                  context, viewport.init_F_D(mouseX), viewport.load(mouseY), button, (float)(deltaX / viewport.read()), (float)(deltaY / viewport.write())
               );
         } else {
            return false;
         }
      }
   }

   public boolean init_Z_D(double mouseX, double mouseY, double amount) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, viewport.init_F_D(mouseX), viewport.load(mouseY), 0.0F);
            return this.init_TableIndex.init_Z_SyncManager(context, viewport.init_F_D(mouseX), viewport.load(mouseY), (float)amount);
         } else {
            return false;
         }
      }
   }

   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, 0.0F, 0.0F, 0.0F);
            return this.init_TableIndex.init_Z_SyncManager_3(context, keyCode, scanCode, modifiers);
         } else {
            return false;
         }
      }
   }

   public boolean init(char chr, int modifiers) {
      if (this.init_TableIndex == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            RingBuffer viewport = this.init_RingBuffer();
            SyncManager context = new SyncManager(null, mc.font, viewport, this.init_CharEncoder, 0.0F, 0.0F, 0.0F);
            return this.init_TableIndex.init_Z_SyncManager_2(context, chr, modifiers);
         } else {
            return false;
         }
      }
   }

   public boolean init_Z() {
      return this.init_Z(this.init_TableIndex);
   }

   public void load() {
      this.init_V(this.init_TableIndex);
   }

   private boolean init_Z(TableIndex node) {
      if (node != null && node.write_Z()) {
         if (node instanceof ListPool input && input.load_Z()) {
            return true;
         } else {
            if (node instanceof HashUtil container) {
               for (TableIndex child : container.init_List_2()) {
                  if (this.init_Z(child)) {
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

   private void init_V(TableIndex node) {
      if (node != null && node.write_Z()) {
         if (node instanceof ListPool input) {
            input.init_V_Z(false);
         }

         if (node instanceof HashUtil container) {
            for (TableIndex child : container.init_List_2()) {
               this.init_V(child);
            }
         }
      }
   }
}
