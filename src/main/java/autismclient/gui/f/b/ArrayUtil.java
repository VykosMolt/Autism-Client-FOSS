package autismclient.gui.f.b;

import autismclient.AutismClientAddon;
import autismclient.util.bK;
import autismclient.util.bk;
import autismclient.util.cF;
import autismclient.util.cH;
import autismclient.util.cX;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ArrayUtil implements autismclient.gui.f.StateCache {
   private final cX init_cX;
   private final autismclient.gui.f.CharEncoder init_CharEncoder;
   private boolean init_Z;
   private boolean load;
   private boolean read;
   private boolean write = true;
   private static long init_J;

   public ArrayUtil(cX overlay) {
      this.init_cX = overlay;
      this.init_CharEncoder = new autismclient.gui.f.CharEncoder(overlay.sync_Z());
   }

   public void init_V_Z(boolean zoom) {
      this.write = zoom;
   }

   public void load(boolean renderSuppressed) {
      this.init_Z = renderSuppressed;
   }

   public void init_V() {
      this.init_CharEncoder.init_V(this.init_cX.sync_Z());
   }

   public void load_V() {
      this.init_CharEncoder.load_V(this.init_cX.sync_Z());
   }

   public void read() {
      this.init_CharEncoder.init_V();
   }

   public boolean init_Z() {
      return this.init_CharEncoder.init_Z(this.init_cX.sync_Z());
   }

   public boolean load_Z() {
      return this.init_CharEncoder.load_Z(this.init_cX.sync_Z());
   }

   public void read(boolean hoverBlocked) {
      this.load = hoverBlocked;
   }

   public void write(boolean inputSuppressed) {
      this.read = inputSuppressed;
   }

   @Override
   public autismclient.gui.f.NetHelper init_NetHelper() {
      cH bounds = this.init_cX.init_cH();
      return autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, bounds.load_Z ? cF.open_I() : bounds.write);
   }

   @Override
   public void init_V_NetHelper(autismclient.gui.f.NetHelper bounds) {
      if (bounds != null) {
         cH current = this.init_cX.init_cH();
         this.init_cX.init_V_cH(new cH(bounds.read(), bounds.write(), bounds.update(), bounds.sync(), current.init_Z, current.load_Z));
      }
   }

   @Override
   public autismclient.gui.f.NetHelper load_NetHelper() {
      return !this.read && this.init_cX.sync_Z() ? this.init_NetHelper() : null;
   }

   @Override
   public void init_V_SyncManager(autismclient.gui.f.SyncManager context) {
      if (!this.init_Z) {
         boolean visible = this.init_cX.sync_Z();
         if (this.init_CharEncoder.load_Z(visible)) {
            boolean block = !visible || this.load;
            long perfStart = bK.load_J();
            autismclient.gui.f.NetHelper b = this.init_NetHelper();
            GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
            int scissorDepth = autismclient.gui.f.HashUtil.init_HashUtil().init_I();

            try {
               this.init_CharEncoder.init(graphics, b, this.write, () -> this.init(context, block));
            } catch (Throwable t) {
               long nowMs = System.currentTimeMillis();
               if (nowMs - init_J > 5000L) {
                  init_J = nowMs;
                  AutismClientAddon.LOG.warn("[Overlay] {} failed to draw (visible={})", this.init_cX.init_Str(), visible, t);
               }
            } finally {
               autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, scissorDepth);
            }

            if (perfStart != 0L) {
               bK.init("overlay." + this.init_cX.init_Str(), perfStart);
            }
         }
      }
   }

   private void init(autismclient.gui.f.SyncManager context, boolean block) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      int mouseX = block ? -10000 : context.read();
      int mouseY = block ? -10000 : context.write();
      boolean clip = this.init_cX instanceof cF window && window.add_Z() && window.poll_I() > 0;
      if (clip) {
         cH bounds = this.init_cX.init_cH();
         autismclient.gui.f.HashUtil.init_HashUtil()
            .init(graphics, autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, ((cF)this.init_cX).poll_I()));
      }

      try {
         this.init_cX.init_V_GuiGraphicsExtract_15(graphics, mouseX, mouseY, context.init_F());
      } finally {
         if (clip) {
            autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
         }
      }

      this.init_V_SyncManager(context, mouseX, mouseY);
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, int mouseX, int mouseY) {
      if (this.init_cX.read_Z() && this.init_cX.sync_Z() && !this.init_cX.flush_Z() && !(this.init_cX instanceof cF window && window.add_Z())) {
         cH bounds = this.init_cX.init_cH();
         boolean active = bk.init_bk().read_Z(this.init_cX);
         boolean hover = !active && ValueSink.init(bounds, mouseX, mouseY);
         ValueSink.init(context.init_GuiGraphicsExtract(), context.init_TableIndex().init_JsonUtil(), bounds, hover, active);
         if (hover || active) {
            ValueSink.init(context.init_GuiGraphicsExtract());
         }
      }
   }

   @Override
   public autismclient.gui.f.EventBus init_EventBus_I_2(int mouseX, int mouseY, int button) {
      this.init_cX.init_Z_D_3(mouseX, mouseY, button);
      return autismclient.gui.f.EventBus.HANDLED;
   }

   @Override
   public autismclient.gui.f.EventBus load(int mouseX, int mouseY, int button) {
      return init_EventBus_Z(this.init_cX.load_Z_D_3(mouseX, mouseY, button));
   }

   @Override
   public autismclient.gui.f.EventBus init_EventBus_I(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return init_EventBus_Z(this.init_cX.init_Z_D(mouseX, mouseY, button, deltaX, deltaY));
   }

   @Override
   public autismclient.gui.f.EventBus init_EventBus_I(int mouseX, int mouseY, double amount) {
      this.init_cX.init_Z_D_2(mouseX, mouseY, amount);
      return autismclient.gui.f.EventBus.HANDLED;
   }

   @Override
   public autismclient.gui.f.EventBus read(int key, int scanCode, int modifiers) {
      return init_EventBus_Z(this.init_cX.init_Z_I(key, scanCode, modifiers));
   }

   @Override
   public autismclient.gui.f.EventBus init_EventBus_C(char chr) {
      return this.init(chr, 0);
   }

   public autismclient.gui.f.EventBus init(char chr, int modifiers) {
      return init_EventBus_Z(this.init_cX.init_Z_C_I(chr, modifiers));
   }

   private static autismclient.gui.f.EventBus init_EventBus_Z(boolean handled) {
      return handled ? autismclient.gui.f.EventBus.HANDLED : autismclient.gui.f.EventBus.IGNORED;
   }
}
