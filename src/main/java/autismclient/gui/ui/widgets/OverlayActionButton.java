package autismclient.gui.ui.widgets;

import autismclient.util.ThemedTextHelper;
import autismclient.util.ThemePaletteManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import autismclient.gui.f.b.CachePool;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.l;

public final class OverlayActionButton {
   private static final CharEncoder THEME = new CharEncoder();
   private static final int HORIZONTAL_PADDING = 4;
   public boolean active = true;
   public boolean visible = true;
   private int x;
   private int y;
   private int width;
   private int height;
   private Component message;
   private OverlayActionButton.ButtonTone tone = OverlayActionButton.ButtonTone.SECONDARY;
   private l textTone = l.read;
   private Boolean toggleState;
   private String animationKey;
   private Identifier icon;
   private Boolean selected;
   private final OverlayActionButton.PressHandler primaryAction;
   private final OverlayActionButton.PressHandler secondaryAction;

   private static Identifier fontForHeight(int height) {
      return THEME.init_Identifier_l(l.read);
   }

   private static int resolveWidth(int requestedWidth, int height, Component message) {
      if (requestedWidth > 0) {
         return requestedWidth;
      }

      Minecraft client = Minecraft.getInstance();
      Font renderer = client == null ? null : client.font;
      if (renderer == null) {
         return requestedWidth;
      }

      String label = ThemedTextHelper.init(message == null ? "" : message.getString());
      Identifier font = fontForHeight(height);
      int measured = UiLayoutMath.measurePaddedWidth(renderer, label, font, THEME.load(l.read), 4, requestedWidth, 2147483647);
      return Math.max(requestedWidth, measured);
   }

   private OverlayActionButton(
      int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler primaryAction, OverlayActionButton.PressHandler secondaryAction
   ) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.message = message == null ? Component.empty() : message;
      this.primaryAction = primaryAction;
      this.secondaryAction = secondaryAction;
   }

   public static OverlayActionButton create(int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler pressAction) {
      return new OverlayActionButton(x, y, resolveWidth(width, height, message), height, message, pressAction, null);
   }

   public static OverlayActionButton create(
      int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler pressAction, OverlayActionButton.PressHandler secondaryPressAction
   ) {
      return new OverlayActionButton(x, y, resolveWidth(width, height, message), height, message, pressAction, secondaryPressAction);
   }

   public OverlayActionButton setTextTone(l tone) {
      this.textTone = tone == null ? l.read : tone;
      return this;
   }

   public OverlayActionButton setToggleState(boolean toggleState) {
      this.toggleState = toggleState;
      return this;
   }

   public OverlayActionButton setAnimationKey(String animationKey) {
      this.animationKey = animationKey;
      return this;
   }

   public OverlayActionButton setIcon(Identifier icon) {
      this.icon = icon;
      return this;
   }

   public OverlayActionButton setSelected(boolean selected) {
      this.selected = selected;
      return this;
   }

   public static boolean handleClick(OverlayActionButton button, double mouseX, double mouseY, int mouseButton) {
      if (button != null && button.visible && button.active) {
         if ((mouseButton == 0 || mouseButton == 1) && button.containsPoint(mouseX, mouseY)) {
            OverlayActionButton.PressHandler action = mouseButton == 1 ? button.secondaryAction : button.primaryAction;
            if (action == null) {
               return false;
            }

            action.onPress(button);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void render(GuiGraphicsExtractor context, Font textRenderer, OverlayActionButton button, int mouseX, int mouseY) {
      if (button != null && button.visible) {
         if (button.icon != null) {
            boolean iconHovered = button.containsPoint(mouseX, mouseY);
            IconButtonRenderer.renderIcon(
               context,
               textRenderer,
               autismclient.gui.f.NetHelper.init(button.x, button.y, button.width, button.height),
               button.icon,
               iconHovered,
               button.active,
               button.tone == OverlayActionButton.ButtonTone.DANGER
            );
         } else {
            String label = ThemedTextHelper.init(button.message.getString());
            String upperLabel = label.toUpperCase();
            boolean inferredDanger = "x".equalsIgnoreCase(label) || label.toLowerCase().contains("delete") || label.toLowerCase().contains("clear");
            boolean toggleOn = label.startsWith("[x]")
               || label.startsWith("[X]")
               || label.startsWith("[✓]")
               || upperLabel.equals("ON")
               || upperLabel.endsWith(": ON")
               || upperLabel.equals("MODE: ENABLE")
               || upperLabel.equals("ENABLE")
               || upperLabel.equals("ENABLED")
               || upperLabel.endsWith(": ENABLED");
            boolean toggleOff = label.startsWith("[ ]")
               || upperLabel.equals("OFF")
               || upperLabel.endsWith(": OFF")
               || upperLabel.equals("MODE: DISABLE")
               || upperLabel.equals("DISABLE")
               || upperLabel.equals("DISABLED")
               || upperLabel.endsWith(": DISABLED");
            OverlayActionButton.ButtonTone variant = button.tone;
            if (variant == OverlayActionButton.ButtonTone.SECONDARY && inferredDanger) {
               variant = OverlayActionButton.ButtonTone.DANGER;
            }

            if (variant == OverlayActionButton.ButtonTone.SECONDARY && toggleOn) {
               variant = OverlayActionButton.ButtonTone.SUCCESS;
            }

            if (variant == OverlayActionButton.ButtonTone.SECONDARY && toggleOff) {
               variant = OverlayActionButton.ButtonTone.DANGER;
            }

            boolean hovered = button.containsPoint(mouseX, mouseY);
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, textRenderer, mouseX, mouseY);
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(button.x, button.y, button.width, button.height);
            boolean allowInferredToggle = button.tone != OverlayActionButton.ButtonTone.FILTER_ON
               && button.tone != OverlayActionButton.ButtonTone.FILTER_OFF;
            Boolean resolvedToggleState = button.toggleState != null
               ? button.toggleState
               : (allowInferredToggle ? (toggleOn ? Boolean.TRUE : (toggleOff ? Boolean.FALSE : null)) : null);
            if (resolvedToggleState != null) {
               AnimatedToggleButtonRenderer.draw(
                  ui, bounds, label, resolvedToggleState, hovered && button.active, button.animationKey == null ? defaultAnimationKey(button) : button.animationKey
               );
            } else {
               boolean var10000;
               if (button.selected != null) {
                  var10000 = button.selected;
               } else {
                  switch (variant) {
                     case DANGER:
                     case SUCCESS:
                     case PRIMARY:
                     case FILTER_ON:
                        var10000 = true;
                        break;
                     case GHOST:
                     default:
                        var10000 = false;
                  }
               }

               boolean on = var10000;
               CachePool.init(ui, bounds, label, toButtonStyle(variant), hovered && button.active, on);
            }

            if (!button.active && !Boolean.TRUE.equals(button.selected)) {
               int radius = 3;
               autismclient.gui.f.PacketUtil.load(context, bounds, radius, ThemePaletteManager.recolor(-2009060816, ThemePaletteManager.ColorRole.OUTLINE));
               autismclient.gui.f.PacketUtil.load(context, bounds.init(1), Math.max(1, radius - 1), 1711276032);
            }
         }
      }
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getHeight() {
      return this.height;
   }

   public Component getMessage() {
      return this.message;
   }

   public void setMessage(Component message) {
      this.message = message == null ? Component.empty() : message;
   }

   public OverlayActionButton.ButtonTone getTone() {
      return this.tone;
   }

   public OverlayActionButton setTone(OverlayActionButton.ButtonTone variant) {
      this.tone = variant == null ? OverlayActionButton.ButtonTone.SECONDARY : variant;
      return this;
   }

   private boolean containsPoint(double mouseX, double mouseY) {
      return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
   }

   private static CachePool.Inner_DataUtil toButtonStyle(OverlayActionButton.ButtonTone variant) {
      CachePool.Inner_DataUtil var10000;
      switch (variant) {
         case SECONDARY:
         case GHOST:
         case FILTER_OFF:
            var10000 = CachePool.Inner_DataUtil.init;
            break;
         case DANGER:
            var10000 = CachePool.Inner_DataUtil.update;
            break;
         case SUCCESS:
         case FILTER_ON:
            var10000 = CachePool.Inner_DataUtil.write;
            break;
         case PRIMARY:
            var10000 = CachePool.Inner_DataUtil.read;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String defaultAnimationKey(OverlayActionButton button) {
      return "overlay-toggle:" + button.x + ":" + button.y + ":" + button.width + ":" + button.height + ":" + button.textTone;
   }

   @FunctionalInterface
   public interface PressHandler {
      void onPress(OverlayActionButton var1);
   }

   public enum ButtonTone {
      SECONDARY,
      DANGER,
      SUCCESS,
      PRIMARY,
      GHOST,
      FILTER_ON,
      FILTER_OFF;
   }
}
