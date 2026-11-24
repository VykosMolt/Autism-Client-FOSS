package autismclient.gui.f.c;

import autismclient.gui.f.b.MethodUtil;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public class ByteReader extends TableIndex {
   private String init_Str;
   private ByteReader.Inner_StreamUtil init_ByteReader_StreamU;
   private Runnable init_Runnable;
   private float sync_F = 24.0F;
   private float flush = 3.4028235E38F;
   private float close = -1.0F;
   private int init_I = 5;
   private int load_I = -1;
   private int read_I = 0;
   private Identifier init_Identifier;
   private int write_I = 10;
   private int update_I = 4;
   private ByteReader.Inner_DataUtil init_ByteReader_DataUti = ByteReader.Inner_DataUtil.init;
   private l init_l = l.read;
   private boolean write_Z;
   private boolean update_Z;
   private MethodUtil.Inner_DataUtil init_MethodUtil_DataUti = MethodUtil.init;
   private float open;
   private boolean sync_Z;
   private long init_J_2 = System.nanoTime();

   private Identifier init_Identifier_CharEncoder_I(autismclient.gui.f.b.CharEncoder theme, int height) {
      return theme.init_Identifier_l(this.init_l);
   }

   public ByteReader(String text, ByteReader.Inner_StreamUtil variant, Runnable onPress) {
      this.init_Str = text == null ? "" : text;
      this.init_ByteReader_StreamU = variant == null ? ByteReader.Inner_StreamUtil.load : variant;
      this.init_Runnable = onPress;
      this.write = 13.0F;
   }

   public ByteReader init_ByteReader_Str(String text) {
      String next = text == null ? "" : text;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader init_ByteReader_ByteReader_StreamU(ByteReader.Inner_StreamUtil variant) {
      this.init_ByteReader_StreamU = variant == null ? ByteReader.Inner_StreamUtil.load : variant;
      return this;
   }

   public ByteReader init_ByteReader_Runnable(Runnable onPress) {
      this.init_Runnable = onPress;
      return this;
   }

   public ByteReader init_ByteReader_F(float minWidth) {
      float next = Math.max(0.0F, minWidth);
      if (Float.compare(this.sync_F, next) != 0) {
         this.sync_F = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader load_ByteReader_F(float maxWidth) {
      float next = Math.max(this.sync_F, maxWidth);
      if (Float.compare(this.flush, next) != 0) {
         this.flush = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader read_ByteReader_F(float preferredWidth) {
      if (Float.compare(this.close, preferredWidth) != 0) {
         this.close = preferredWidth;
         this.load_V();
      }

      return this;
   }

   public ByteReader init_ByteReader_I(int horizontalPadding) {
      int next = Math.max(0, horizontalPadding);
      if (this.init_I != next) {
         this.init_I = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader load_ByteReader_I(int fixedHeight) {
      int next = Math.max(1, fixedHeight);
      if (this.load_I != next) {
         this.load_I = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader read_ByteReader_I(int textYOffset) {
      this.read_I = textYOffset;
      return this;
   }

   public ByteReader init_ByteReader_Identifier(Identifier leadingIcon) {
      if (!Objects.equals(this.init_Identifier, leadingIcon)) {
         this.init_Identifier = leadingIcon;
         this.load_V();
      }

      return this;
   }

   public ByteReader write(int iconSize) {
      int next = Math.max(1, iconSize);
      if (this.write_I != next) {
         this.write_I = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader update(int iconGap) {
      int next = Math.max(0, iconGap);
      if (this.update_I != next) {
         this.update_I = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader init_ByteReader_ByteReader_DataUti(ByteReader.Inner_DataUtil contentAlignment) {
      this.init_ByteReader_DataUti = contentAlignment == null ? ByteReader.Inner_DataUtil.init : contentAlignment;
      return this;
   }

   public ByteReader init_ByteReader_l(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.init_l != next) {
         this.init_l = next;
         this.load_V();
      }

      return this;
   }

   public ByteReader init_ByteReader_Z(boolean connectedStyle) {
      this.write_Z = connectedStyle;
      return this;
   }

   public ByteReader load_ByteReader_Z(boolean connectedToggle) {
      this.update_Z = connectedToggle;
      if (connectedToggle) {
         this.write_Z = true;
      }

      return this;
   }

   public ByteReader init_ByteReader_MethodUtil_DataUti(MethodUtil.Inner_DataUtil connectedEdges) {
      this.init_MethodUtil_DataUti = connectedEdges == null ? MethodUtil.load : connectedEdges;
      return this;
   }

   public ByteReader read_ByteReader_Z(boolean growX) {
      super.init_TableIndex_Z(growX);
      return this;
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      int measureHeight = this.load_I > 0 ? this.load_I : context.init_CharEncoder().update();
      Identifier font = this.init_Identifier_CharEncoder_I(context.init_CharEncoder(), measureHeight);
      int scaledHorizontalPadding = context.init_CharEncoder().load(this.init_I);
      int scaledIconSize = context.init_CharEncoder().load(this.write_I);
      int scaledIconGap = context.init_CharEncoder().load(this.update_I);
      float fittedWidth = j.init_F_Font(
         context.init_Font(), this.init_Str, font, context.init_CharEncoder().load(l.read), scaledHorizontalPadding, this.sync_F, this.flush
      );
      if (this.init_Identifier != null) {
         fittedWidth += scaledIconSize + scaledIconGap;
      }

      return this.close > 0.0F ? j.init_F_F(Math.max(this.close, fittedWidth), this.sync_F, this.flush) : j.init_F_F(fittedWidth, this.sync_F, this.flush);
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      int resolvedFixedHeight = this.load_I > 0 ? this.load_I : context.init_CharEncoder().update();
      int contentHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(4);
      return Math.max(12, Math.max(resolvedFixedHeight, contentHeight));
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawW = Math.round(this.read_F);
         int drawH = Math.round(this.write);
         boolean hovered = this.load_Z && this.init_Z_F_F(context.init_F(), context.load());
         if (this.write_Z) {
            this.load(context, drawX, drawY, drawW, drawH, hovered);
         } else {
            this.init_V_SyncManager(context, drawX, drawY, drawW, drawH, hovered);
         }
      }
   }

   private void init_V_SyncManager(SyncManager context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      autismclient.gui.f.b.CachePool.Inner_DataUtil buttonTone = this.init_CachePool_DataUtil();
      if (this.init_Identifier == null) {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.init_Str, buttonTone, hovered, false);
      } else {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.init_Str, this.init_Identifier, buttonTone, hovered, false, 0.0F);
      }

      if (!this.load_Z) {
         autismclient.gui.f.PacketUtil.init(context.init_GuiGraphicsExtract(), bounds.init(1), 1711276032);
      }
   }

   private void load(SyncManager context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      if (this.update_Z) {
         float target = this.init_ByteReader_StreamU == ByteReader.Inner_StreamUtil.update ? 1.0F : 0.0F;
         if (!this.sync_Z) {
            this.open = target;
            this.sync_Z = true;
         } else {
            this.open = this.init_F_F_F(this.open, target);
         }

         MethodUtil.init(ui, bounds, this.init_Str, this.init_Identifier, hovered, this.open, this.init_MethodUtil_DataUti);
      } else {
         MethodUtil.init(ui, bounds, this.init_Str, this.init_Identifier, this.init_CachePool_DataUtil(), hovered, this.init_MethodUtil_DataUti);
      }
   }

   boolean init_Z_2() {
      return this.write_Z;
   }

   int init_I_SyncManager(autismclient.gui.f.SyncManager ui) {
      return this.update_Z ? MethodUtil.init(ui, this.open) : MethodUtil.init(ui, this.init_CachePool_DataUtil());
   }

   float init_F() {
      return MethodUtil.init_F_CachePool_DataUtil(this.init_CachePool_DataUtil(), this.update_Z, this.update_Z ? this.open : 0.0F);
   }

   private autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil() {
      return init_CachePool_DataUtil_ByteReader_StreamU(this.init_ByteReader_StreamU);
   }

   public static autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil_ByteReader_StreamU(ByteReader.Inner_StreamUtil variant) {
      autismclient.gui.f.b.CachePool.Inner_DataUtil var10000;
      switch (variant) {
         case init:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.read;
            break;
         case load:
         case read:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
            break;
         case write:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
            break;
         case update:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.write;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private float init_F_F_F(float current, float target) {
      long now = System.nanoTime();
      float delta = Math.max(0.0F, Math.min(0.05F, (float)(now - this.init_J_2) / 1.0E9F));
      this.init_J_2 = now;
      float step = delta / 0.16F;
      if (current < target) {
         return Math.min(target, current + step);
      } else {
         return current > target ? Math.max(target, current - step) : current;
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(SyncManager context, float mouseX, float mouseY, int button) {
      if (this.load_Z && this.init_Z_F_F(mouseX, mouseY) && button == 0) {
         if (this.init_Runnable != null) {
            this.init_Runnable.run();
         }

         return true;
      } else {
         return false;
      }
   }

   public enum Inner_DataUtil {
      init,
      load;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
