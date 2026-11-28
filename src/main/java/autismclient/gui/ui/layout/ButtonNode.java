package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.ConnectedButtonRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public class ButtonNode extends LayoutNode {
   private String label;
   private ButtonNode.Variant variant;
   private Runnable onPress;
   private float minWidth = 24.0F;
   private float maxWidth = 3.4028235E38F;
   private float preferredWidth = -1.0F;
   private int horizontalPadding = 5;
   private int fixedHeight = -1;
   private int textYOffset = 0;
   private Identifier leadingIcon;
   private int iconSize = 10;
   private int iconGap = 4;
   private ButtonNode.TextAlign textAlign = ButtonNode.TextAlign.CENTER;
   private l textTone = l.read;
   private boolean connectedStyle;
   private boolean connectedToggle;
   private ConnectedButtonRenderer.ConnectedEdges connectedEdges = ConnectedButtonRenderer.init;
   private float toggleProgress;
   private boolean toggleInitialized;
   private long lastAnimNanos = System.nanoTime();

   private Identifier resolveFont(autismclient.gui.f.b.CharEncoder theme, int height) {
      return theme.init_Identifier_l(this.textTone);
   }

   public ButtonNode(String text, ButtonNode.Variant variant, Runnable onPress) {
      this.label = text == null ? "" : text;
      this.variant = variant == null ? ButtonNode.Variant.SECONDARY : variant;
      this.onPress = onPress;
      this.nodeHeight = 13.0F;
   }

   public ButtonNode setText(String text) {
      String next = text == null ? "" : text;
      if (!this.label.equals(next)) {
         this.label = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setVariant(ButtonNode.Variant variant) {
      this.variant = variant == null ? ButtonNode.Variant.SECONDARY : variant;
      return this;
   }

   public ButtonNode setOnPress(Runnable onPress) {
      this.onPress = onPress;
      return this;
   }

   public ButtonNode setMinWidth(float minWidth) {
      float next = Math.max(0.0F, minWidth);
      if (Float.compare(this.minWidth, next) != 0) {
         this.minWidth = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setMaxWidth(float maxWidth) {
      float next = Math.max(this.minWidth, maxWidth);
      if (Float.compare(this.maxWidth, next) != 0) {
         this.maxWidth = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setPreferredWidth(float preferredWidth) {
      if (Float.compare(this.preferredWidth, preferredWidth) != 0) {
         this.preferredWidth = preferredWidth;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setHorizontalPadding(int horizontalPadding) {
      int next = Math.max(0, horizontalPadding);
      if (this.horizontalPadding != next) {
         this.horizontalPadding = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setFixedHeight(int fixedHeight) {
      int next = Math.max(1, fixedHeight);
      if (this.fixedHeight != next) {
         this.fixedHeight = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setTextYOffset(int textYOffset) {
      this.textYOffset = textYOffset;
      return this;
   }

   public ButtonNode setLeadingIcon(Identifier leadingIcon) {
      if (!Objects.equals(this.leadingIcon, leadingIcon)) {
         this.leadingIcon = leadingIcon;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setIconSize(int iconSize) {
      int next = Math.max(1, iconSize);
      if (this.iconSize != next) {
         this.iconSize = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setIconGap(int iconGap) {
      int next = Math.max(0, iconGap);
      if (this.iconGap != next) {
         this.iconGap = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setTextAlign(ButtonNode.TextAlign contentAlignment) {
      this.textAlign = contentAlignment == null ? ButtonNode.TextAlign.CENTER : contentAlignment;
      return this;
   }

   public ButtonNode setTextTone(l tone) {
      l next = tone == null ? l.read : tone;
      if (this.textTone != next) {
         this.textTone = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ButtonNode setConnectedStyle(boolean connectedStyle) {
      this.connectedStyle = connectedStyle;
      return this;
   }

   public ButtonNode setConnectedToggle(boolean connectedToggle) {
      this.connectedToggle = connectedToggle;
      if (connectedToggle) {
         this.connectedStyle = true;
      }

      return this;
   }

   public ButtonNode setConnectedEdges(ConnectedButtonRenderer.ConnectedEdges connectedEdges) {
      this.connectedEdges = connectedEdges == null ? ConnectedButtonRenderer.load : connectedEdges;
      return this;
   }

   public ButtonNode withGrowX(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      int measureHeight = this.fixedHeight > 0 ? this.fixedHeight : context.theme().update();
      Identifier font = this.resolveFont(context.theme(), measureHeight);
      int scaledHorizontalPadding = context.theme().load(this.horizontalPadding);
      int scaledIconSize = context.theme().load(this.iconSize);
      int scaledIconGap = context.theme().load(this.iconGap);
      float fittedWidth = UiLayoutMath.measurePaddedWidthFloat(
         context.font(), this.label, font, context.theme().load(l.read), scaledHorizontalPadding, this.minWidth, this.maxWidth
      );
      if (this.leadingIcon != null) {
         fittedWidth += scaledIconSize + scaledIconGap;
      }

      return this.preferredWidth > 0.0F ? UiLayoutMath.clamp(Math.max(this.preferredWidth, fittedWidth), this.minWidth, this.maxWidth) : UiLayoutMath.clamp(fittedWidth, this.minWidth, this.maxWidth);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      int resolvedFixedHeight = this.fixedHeight > 0 ? this.fixedHeight : context.theme().update();
      int contentHeight = context.theme().init_I_l(this.textTone) + context.theme().load(4);
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
         if (this.connectedStyle) {
            this.renderConnected(context, drawX, drawY, drawW, drawH, hovered);
         } else {
            this.renderStandard(context, drawX, drawY, drawW, drawH, hovered);
         }
      }
   }

   private void renderStandard(LayoutContext context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      autismclient.gui.f.b.CachePool.Inner_DataUtil buttonTone = this.resolveButtonTone();
      if (this.leadingIcon == null) {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.label, buttonTone, hovered, false);
      } else {
         autismclient.gui.f.b.CachePool.init(ui, bounds, this.label, this.leadingIcon, buttonTone, hovered, false, 0.0F);
      }

      if (!this.nodeEnabled) {
         autismclient.gui.f.PacketUtil.init(context.graphics(), bounds.init(1), 1711276032);
      }
   }

   private void renderConnected(LayoutContext context, int drawX, int drawY, int drawW, int drawH, boolean hovered) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
         context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
      );
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
      if (this.connectedToggle) {
         float target = this.variant == ButtonNode.Variant.SUCCESS ? 1.0F : 0.0F;
         if (!this.toggleInitialized) {
            this.toggleProgress = target;
            this.toggleInitialized = true;
         } else {
            this.toggleProgress = this.stepToggleAnimation(this.toggleProgress, target);
         }

         ConnectedButtonRenderer.init(ui, bounds, this.label, this.leadingIcon, hovered, this.toggleProgress, this.connectedEdges);
      } else {
         ConnectedButtonRenderer.render(ui, bounds, this.label, this.leadingIcon, this.resolveButtonTone(), hovered, this.connectedEdges);
      }
   }

   boolean isConnectedStyle() {
      return this.connectedStyle;
   }

   int resolveConnectedColor(autismclient.gui.f.SyncManager ui) {
      return this.connectedToggle ? ConnectedButtonRenderer.init(ui, this.toggleProgress) : ConnectedButtonRenderer.init(ui, this.resolveButtonTone());
   }

   float resolveConnectedWeight() {
      return ConnectedButtonRenderer.init_F_CachePool_DataUtil(this.resolveButtonTone(), this.connectedToggle, this.connectedToggle ? this.toggleProgress : 0.0F);
   }

   private autismclient.gui.f.b.CachePool.Inner_DataUtil resolveButtonTone() {
      return toButtonTone(this.variant);
   }

   public static autismclient.gui.f.b.CachePool.Inner_DataUtil toButtonTone(ButtonNode.Variant variant) {
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

   private float stepToggleAnimation(float current, float target) {
      long now = System.nanoTime();
      float delta = Math.max(0.0F, Math.min(0.05F, (float)(now - this.lastAnimNanos) / 1.0E9F));
      this.lastAnimNanos = now;
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
         if (this.onPress != null) {
            this.onPress.run();
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
