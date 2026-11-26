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
   private static final CharEncoder init_CharEncoder = new CharEncoder();
   private static final int init_I = 4;
   public boolean init_Z = true;
   public boolean load_Z = true;
   private int load_I;
   private int read;
   private int write;
   private int update;
   private Component init_Component;
   private OverlayActionButton.ButtonTone init_ByteReader_StreamU = OverlayActionButton.ButtonTone.SECONDARY;
   private l init_l = l.read;
   private Boolean init_Boolean;
   private String init_Str;
   private Identifier init_Identifier;
   private Boolean load_Boolean;
   private final OverlayActionButton.PressHandler init_ByteReader_DataUti;
   private final OverlayActionButton.PressHandler load_ByteReader_DataUti;

   private static Identifier init_Identifier_I(int height) {
      return init_CharEncoder.init_Identifier_l(l.read);
   }

   private static int init(int requestedWidth, int height, Component message) {
      if (requestedWidth > 0) {
         return requestedWidth;
      }

      Minecraft client = Minecraft.getInstance();
      Font renderer = client == null ? null : client.font;
      if (renderer == null) {
         return requestedWidth;
      }

      String label = ThemedTextHelper.init(message == null ? "" : message.getString());
      Identifier font = init_Identifier_I(height);
      int measured = UiLayoutMath.measurePaddedWidth(renderer, label, font, init_CharEncoder.load(l.read), 4, requestedWidth, 2147483647);
      return Math.max(requestedWidth, measured);
   }

   private OverlayActionButton(
      int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler primaryAction, OverlayActionButton.PressHandler secondaryAction
   ) {
      this.load_I = x;
      this.read = y;
      this.write = width;
      this.update = height;
      this.init_Component = message == null ? Component.empty() : message;
      this.init_ByteReader_DataUti = primaryAction;
      this.load_ByteReader_DataUti = secondaryAction;
   }

   public static OverlayActionButton init(int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler pressAction) {
      return new OverlayActionButton(x, y, init(width, height, message), height, message, pressAction, null);
   }

   public static OverlayActionButton init(
      int x, int y, int width, int height, Component message, OverlayActionButton.PressHandler pressAction, OverlayActionButton.PressHandler secondaryPressAction
   ) {
      return new OverlayActionButton(x, y, init(width, height, message), height, message, pressAction, secondaryPressAction);
   }

   public OverlayActionButton init_ByteReader_l(l tone) {
      this.init_l = tone == null ? l.read : tone;
      return this;
   }

   public OverlayActionButton init_ByteReader_Z(boolean toggleState) {
      this.init_Boolean = toggleState;
      return this;
   }

   public OverlayActionButton init_ByteReader_Str(String animationKey) {
      this.init_Str = animationKey;
      return this;
   }

   public OverlayActionButton init_ByteReader_Identifier(Identifier icon) {
      this.init_Identifier = icon;
      return this;
   }

   public OverlayActionButton load(boolean selected) {
      this.load_Boolean = selected;
      return this;
   }

   public static boolean init(OverlayActionButton button, double mouseX, double mouseY, int mouseButton) {
      if (button != null && button.load_Z && button.init_Z) {
         if ((mouseButton == 0 || mouseButton == 1) && button.init(mouseX, mouseY)) {
            OverlayActionButton.PressHandler action = mouseButton == 1 ? button.load_ByteReader_DataUti : button.init_ByteReader_DataUti;
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

   public static void init(GuiGraphicsExtractor context, Font textRenderer, OverlayActionButton button, int mouseX, int mouseY) {
      if (button != null && button.load_Z) {
         if (button.init_Identifier != null) {
            boolean iconHovered = button.init(mouseX, mouseY);
            IconButtonRenderer.init(
               context,
               textRenderer,
               autismclient.gui.f.NetHelper.init(button.load_I, button.read, button.write, button.update),
               button.init_Identifier,
               iconHovered,
               button.init_Z,
               button.init_ByteReader_StreamU == OverlayActionButton.ButtonTone.DANGER
            );
         } else {
            String label = ThemedTextHelper.init(button.init_Component.getString());
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
            OverlayActionButton.ButtonTone variant = button.init_ByteReader_StreamU;
            if (variant == OverlayActionButton.ButtonTone.SECONDARY && inferredDanger) {
               variant = OverlayActionButton.ButtonTone.DANGER;
            }

            if (variant == OverlayActionButton.ButtonTone.SECONDARY && toggleOn) {
               variant = OverlayActionButton.ButtonTone.SUCCESS;
            }

            if (variant == OverlayActionButton.ButtonTone.SECONDARY && toggleOff) {
               variant = OverlayActionButton.ButtonTone.DANGER;
            }

            boolean hovered = button.init(mouseX, mouseY);
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, textRenderer, mouseX, mouseY);
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(button.load_I, button.read, button.write, button.update);
            boolean allowInferredToggle = button.init_ByteReader_StreamU != OverlayActionButton.ButtonTone.FILTER_ON
               && button.init_ByteReader_StreamU != OverlayActionButton.ButtonTone.FILTER_OFF;
            Boolean resolvedToggleState = button.init_Boolean != null
               ? button.init_Boolean
               : (allowInferredToggle ? (toggleOn ? Boolean.TRUE : (toggleOff ? Boolean.FALSE : null)) : null);
            if (resolvedToggleState != null) {
               AnimatedToggleButtonRenderer.init(
                  ui, bounds, label, resolvedToggleState, hovered && button.init_Z, button.init_Str == null ? init_Str_ByteReader(button) : button.init_Str
               );
            } else {
               boolean var10000;
               if (button.load_Boolean != null) {
                  var10000 = button.load_Boolean;
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
               CachePool.init(ui, bounds, label, init_CachePool_DataUtil_ByteReader_StreamU(variant), hovered && button.init_Z, on);
            }

            if (!button.init_Z && !Boolean.TRUE.equals(button.load_Boolean)) {
               int radius = 3;
               autismclient.gui.f.PacketUtil.load(context, bounds, radius, ThemePaletteManager.recolor(-2009060816, ThemePaletteManager.ColorRole.OUTLINE));
               autismclient.gui.f.PacketUtil.load(context, bounds.init(1), Math.max(1, radius - 1), 1711276032);
            }
         }
      }
   }

   public int init_I() {
      return this.load_I;
   }

   public void init_V_I(int x) {
      this.load_I = x;
   }

   public int load() {
      return this.read;
   }

   public void load(int y) {
      this.read = y;
   }

   public int read() {
      return this.write;
   }

   public void read(int width) {
      this.write = width;
   }

   public int write() {
      return this.update;
   }

   public Component init_Component() {
      return this.init_Component;
   }

   public void init_V_Component(Component message) {
      this.init_Component = message == null ? Component.empty() : message;
   }

   public OverlayActionButton.ButtonTone init_ByteReader_StreamU() {
      return this.init_ByteReader_StreamU;
   }

   public OverlayActionButton init_ByteReader_ByteReader_StreamU(OverlayActionButton.ButtonTone variant) {
      this.init_ByteReader_StreamU = variant == null ? OverlayActionButton.ButtonTone.SECONDARY : variant;
      return this;
   }

   private boolean init(double mouseX, double mouseY) {
      return mouseX >= this.load_I && mouseX < this.load_I + this.write && mouseY >= this.read && mouseY < this.read + this.update;
   }

   private static CachePool.Inner_DataUtil init_CachePool_DataUtil_ByteReader_StreamU(OverlayActionButton.ButtonTone variant) {
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

   private static String init_Str_ByteReader(OverlayActionButton button) {
      return "overlay-toggle:" + button.load_I + ":" + button.read + ":" + button.write + ":" + button.update + ":" + button.init_l;
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
