package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public class ButtonNode extends LayoutNode {
   private String init_Str;
   private ButtonNode.Variant init_ByteReader_StreamU;
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
   private ButtonNode.TextAlign init_ByteReader_DataUti = ButtonNode.TextAlign.CENTER;
   private l init_l = l.read;
   private boolean write_Z;
   private boolean update_Z;
   private ConnectedButtonRenderer.Inner_DataUtil init_MethodUtil_DataUti = ConnectedButtonRenderer.init;
   private float open;
   private boolean sync_Z;
   private long init_J_2 = System.nanoTime();

   private Identifier init_Identifier_CharEncoder_I(autismclient.gui.f.b.CharEncoder theme, int height) {
      return theme.init_Identifier_l(this.init_l);
   }

   public ButtonNode(String text, ButtonNode.Variant variant, Runnable onPress) {
      this.init_Str = text == null ? "" : text;
      this.init_ByteReader_StreamU = variant == null ? ButtonNode.Variant.SECONDARY : variant;
      this.init_Runnable = onPress;
      this.nodeHeight = 13.0F;
   }

   public ButtonNode init_ByteReader_Str(String text) {
      String next = text == null ? "" : text;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode init_ByteReader_ByteReader_StreamU(ButtonNode.Variant variant) {
      this.init_ByteReader_StreamU = variant == null ? ButtonNode.Variant.SECONDARY : variant;
      return this;
   }

   public ButtonNode init_ByteReader_Runnable(Runnable onPress) {
      this.init_Runnable = onPress;
      return this;
   }

   public ButtonNode init_ByteReader_F(float minWidth) {
      float next = Math.max(0.0F, minWidth);
      if (Float.compare(this.sync_F, next) != 0) {
         this.sync_F = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode load_ByteReader_F(float maxWidth) {
      float next = Math.max(this.sync_F, maxWidth);
      if (Float.compare(this.flush, next) != 0) {
         this.flush = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode read_ByteReader_F(float preferredWidth) {
      if (Float.compare(this.close, preferredWidth) != 0) {
         this.close = preferredWidth;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode init_ByteReader_I(int horizontalPadding) {
      int next = Math.max(0, horizontalPadding);
      if (this.init_I != next) {
         this.init_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode load_ByteReader_I(int fixedHeight) {
      int next = Math.max(1, fixedHeight);
      if (this.load_I != next) {
         this.load_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode read_ByteReader_I(int textYOffset) {
      this.read_I = textYOffset;
      return this;
   }

   public ButtonNode init_ByteReader_Identifier(Identifier leadingIcon) {
      if (!Objects.equals(this.init_Identifier, leadingIcon)) {
         this.init_Identifier = leadingIcon;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode write(int iconSize) {
      int next = Math.max(1, iconSize);
      if (this.write_I != next) {
         this.write_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode update(int iconGap) {
      int next = Math.max(0, iconGap);
      if (this.update_I != next) {
         this.update_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode init_ByteReader_ByteReader_DataUti(ButtonNode.TextAlign contentAlignment) {
      this.init_ByteReader_DataUti = contentAlignment == null ? ButtonNode.TextAlign.CENTER : contentAlignment;
      return this;
   }

   public ButtonNode init_ByteReader_l(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.init_l != next) {
         this.init_l = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode init_ByteReader_Z(boolean connectedStyle) {
      this.write_Z = connectedStyle;
      return this;
   }

   public ButtonNode load_ByteReader_Z(boolean connectedToggle) {
      this.update_Z = connectedToggle;
      if (connectedToggle) {
         this.write_Z = true;
      }

      return this;
   }

   public ButtonNode init_ByteReader_MethodUtil_DataUti(ConnectedButtonRenderer.Inner_DataUtil connectedEdges) {
      this.init_MethodUtil_DataUti = connectedEdges == null ? ConnectedButtonRenderer.load : connectedEdges;
      return this;
   }

   public ButtonNode read_ByteReader_Z(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      int measureHeight = this.load_I > 0 ? this.load_I : context.theme().update();
      Identifier font = this.init_Identifier_CharEncoder_I(context.theme(), measureHeight);
      int scaledHorizontalPadding = context.theme().load(this.init_I);
      int scaledIconSize = context.theme().load(this.write_I);
      int scaledIconGap = context.theme().load(this.update_I);
      float fittedWidth = UiLayoutMath.measurePaddedWidthFloat(
         context.font(), this.init_Str, font, context.theme().load(l.read), scaledHorizontalPadding, this.sync_F, this.flush
      );
      if (this.init_Identifier != null) {
         fittedWidth += scaledIconSize + scaledIconGap;
      }

      return this.close > 0.0F ? UiLayoutMath.clamp(Math.max(this.close, fittedWidth), this.sync_F, this.flush) : UiLayoutMath.clamp(fittedWidth, this.sync_F, this.flush);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int resolvedFixedHeight = this.load_I > 0 ? this.load_I : context.theme().update();
      int contentHeight = context.theme().init_I_l(this.init_l) + context.theme().load(4);
      return Math.max(12, Math.max(resolvedFixedHeight, contentHeight));
   }

   @Override
   public void render(LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawH = Math.round(this.nodeHeight);
         boolean hovered = this.nodeEnabled && this.containsPoint(context.mouseX(), context.mouseY());
         if (this.write_Z) {
            this.load(context, drawX, drawY, drawW, drawH, hovered);
         } else {
            this.init_V_SyncManager(context, drawX, drawY, drawW, drawH, hovered);
         }
      }
   }

   private void init_V_SyncManager(LayoutContext context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      autismclient.gui.f.b.CachePool.Inner_DataUtil buttonTone = this.init_CachePool_DataUtil();
      if (this.init_Identifier == null) {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.init_Str, buttonTone, hovered, false);
      } else {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.init_Str, this.init_Identifier, buttonTone, hovered, false, 0.0F);
      }

      if (!this.nodeEnabled) {
         autismclient.gui.f.PacketUtil.init(context.graphics(), bounds.init(1), 1711276032);
      }
   }

   private void load(LayoutContext context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      if (this.update_Z) {
         float target = this.init_ByteReader_StreamU == ButtonNode.Variant.SUCCESS ? 1.0F : 0.0F;
         if (!this.sync_Z) {
            this.open = target;
            this.sync_Z = true;
         } else {
            this.open = this.init_F_F_F(this.open, target);
         }

         ConnectedButtonRenderer.init(ui, bounds, this.init_Str, this.init_Identifier, hovered, this.open, this.init_MethodUtil_DataUti);
      } else {
         ConnectedButtonRenderer.init(ui, bounds, this.init_Str, this.init_Identifier, this.init_CachePool_DataUtil(), hovered, this.init_MethodUtil_DataUti);
      }
   }

   boolean init_Z_2() {
      return this.write_Z;
   }

   int init_I_SyncManager(autismclient.gui.f.SyncManager ui) {
      return this.update_Z ? ConnectedButtonRenderer.init(ui, this.open) : ConnectedButtonRenderer.init(ui, this.init_CachePool_DataUtil());
   }

   float init_F() {
      return ConnectedButtonRenderer.init_F_CachePool_DataUtil(this.init_CachePool_DataUtil(), this.update_Z, this.update_Z ? this.open : 0.0F);
   }

   private autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil() {
      return init_CachePool_DataUtil_ByteReader_StreamU(this.init_ByteReader_StreamU);
   }

   public static autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil_ByteReader_StreamU(ButtonNode.Variant variant) {
      autismclient.gui.f.b.CachePool.Inner_DataUtil var10000;
      switch (variant) {
         case PRIMARY:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.read;
            break;
         case SECONDARY:
         case GHOST:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
            break;
         case DANGER:
            var10000 = autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
            break;
         case SUCCESS:
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
   public boolean mouseClicked(LayoutContext context, float mouseX, float mouseY, int button) {
      if (this.nodeEnabled && this.containsPoint(mouseX, mouseY) && button == 0) {
         if (this.init_Runnable != null) {
            this.init_Runnable.run();
         }

         return true;
      } else {
         return false;
      }
   }

   public enum TextAlign {
      CENTER,
      START;
   }

   public enum Variant {
      PRIMARY,
      SECONDARY,
      GHOST,
      DANGER,
      SUCCESS;
   }
}
