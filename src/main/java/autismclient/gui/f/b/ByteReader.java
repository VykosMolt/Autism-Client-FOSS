package autismclient.gui.f.b;

import autismclient.util.cs;
import autismclient.util.ct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ByteReader {
   private static final CharEncoder init_CharEncoder = new CharEncoder();
   private static final int init_I = 4;
   public boolean init_Z = true;
   public boolean load_Z = true;
   private int load_I;
   private int read;
   private int write;
   private int update;
   private Component init_Component;
   private ByteReader.Inner_StreamUtil init_ByteReader_StreamU = ByteReader.Inner_StreamUtil.init;
   private l init_l = l.read;
   private Boolean init_Boolean;
   private String init_Str;
   private Identifier init_Identifier;
   private Boolean load_Boolean;
   private final ByteReader.Inner_DataUtil init_ByteReader_DataUti;
   private final ByteReader.Inner_DataUtil load_ByteReader_DataUti;

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

      String label = cs.init(message == null ? "" : message.getString());
      Identifier font = init_Identifier_I(height);
      int measured = j.init_I_Font(renderer, label, font, init_CharEncoder.load(l.read), 4, requestedWidth, 2147483647);
      return Math.max(requestedWidth, measured);
   }

   private ByteReader(
      int x, int y, int width, int height, Component message, ByteReader.Inner_DataUtil primaryAction, ByteReader.Inner_DataUtil secondaryAction
   ) {
      this.load_I = x;
      this.read = y;
      this.write = width;
      this.update = height;
      this.init_Component = message == null ? Component.empty() : message;
      this.init_ByteReader_DataUti = primaryAction;
      this.load_ByteReader_DataUti = secondaryAction;
   }

   public static ByteReader init(int x, int y, int width, int height, Component message, ByteReader.Inner_DataUtil pressAction) {
      return new ByteReader(x, y, init(width, height, message), height, message, pressAction, null);
   }

   public static ByteReader init(
      int x, int y, int width, int height, Component message, ByteReader.Inner_DataUtil pressAction, ByteReader.Inner_DataUtil secondaryPressAction
   ) {
      return new ByteReader(x, y, init(width, height, message), height, message, pressAction, secondaryPressAction);
   }

   public ByteReader init_ByteReader_l(l tone) {
      this.init_l = tone == null ? l.read : tone;
      return this;
   }

   public ByteReader init_ByteReader_Z(boolean toggleState) {
      this.init_Boolean = toggleState;
      return this;
   }

   public ByteReader init_ByteReader_Str(String animationKey) {
      this.init_Str = animationKey;
      return this;
   }

   public ByteReader init_ByteReader_Identifier(Identifier icon) {
      this.init_Identifier = icon;
      return this;
   }

   public ByteReader load(boolean selected) {
      this.load_Boolean = selected;
      return this;
   }

   public static boolean init(ByteReader button, double mouseX, double mouseY, int mouseButton) {
      if (button != null && button.load_Z && button.init_Z) {
         if ((mouseButton == 0 || mouseButton == 1) && button.init(mouseX, mouseY)) {
            ByteReader.Inner_DataUtil action = mouseButton == 1 ? button.load_ByteReader_DataUti : button.init_ByteReader_DataUti;
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

   public static void init(GuiGraphicsExtractor context, Font textRenderer, ByteReader button, int mouseX, int mouseY) {
      if (button != null && button.load_Z) {
         if (button.init_Identifier != null) {
            boolean iconHovered = button.init(mouseX, mouseY);
            RingBuffer.init(
               context,
               textRenderer,
               autismclient.gui.f.NetHelper.init(button.load_I, button.read, button.write, button.update),
               button.init_Identifier,
               iconHovered,
               button.init_Z,
               button.init_ByteReader_StreamU == ByteReader.Inner_StreamUtil.load
            );
         } else {
            String label = cs.init(button.init_Component.getString());
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
            ByteReader.Inner_StreamUtil variant = button.init_ByteReader_StreamU;
            if (variant == ByteReader.Inner_StreamUtil.init && inferredDanger) {
               variant = ByteReader.Inner_StreamUtil.load;
            }

            if (variant == ByteReader.Inner_StreamUtil.init && toggleOn) {
               variant = ByteReader.Inner_StreamUtil.read;
            }

            if (variant == ByteReader.Inner_StreamUtil.init && toggleOff) {
               variant = ByteReader.Inner_StreamUtil.load;
            }

            boolean hovered = button.init(mouseX, mouseY);
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, textRenderer, mouseX, mouseY);
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(button.load_I, button.read, button.write, button.update);
            boolean allowInferredToggle = button.init_ByteReader_StreamU != ByteReader.Inner_StreamUtil.sync
               && button.init_ByteReader_StreamU != ByteReader.Inner_StreamUtil.flush;
            Boolean resolvedToggleState = button.init_Boolean != null
               ? button.init_Boolean
               : (allowInferredToggle ? (toggleOn ? Boolean.TRUE : (toggleOff ? Boolean.FALSE : null)) : null);
            if (resolvedToggleState != null) {
               DataUtil.init(
                  ui, bounds, label, resolvedToggleState, hovered && button.init_Z, button.init_Str == null ? init_Str_ByteReader(button) : button.init_Str
               );
            } else {
               boolean var10000;
               if (button.load_Boolean != null) {
                  var10000 = button.load_Boolean;
               } else {
                  switch (variant) {
                     case load:
                     case read:
                     case write:
                     case sync:
                        var10000 = true;
                        break;
                     case update:
                     default:
                        var10000 = false;
                  }
               }

               boolean on = var10000;
               CachePool.init(ui, bounds, label, init_CachePool_DataUtil_ByteReader_StreamU(variant), hovered && button.init_Z, on);
            }

            if (!button.init_Z && !Boolean.TRUE.equals(button.load_Boolean)) {
               int radius = 3;
               autismclient.gui.f.PacketUtil.load(context, bounds, radius, ct.init_I_I_ct_DataUtil(-2009060816, ct.Inner_DataUtil.load));
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

   public ByteReader.Inner_StreamUtil init_ByteReader_StreamU() {
      return this.init_ByteReader_StreamU;
   }

   public ByteReader init_ByteReader_ByteReader_StreamU(ByteReader.Inner_StreamUtil variant) {
      this.init_ByteReader_StreamU = variant == null ? ByteReader.Inner_StreamUtil.init : variant;
      return this;
   }

   private boolean init(double mouseX, double mouseY) {
      return mouseX >= this.load_I && mouseX < this.load_I + this.write && mouseY >= this.read && mouseY < this.read + this.update;
   }

   private static CachePool.Inner_DataUtil init_CachePool_DataUtil_ByteReader_StreamU(ByteReader.Inner_StreamUtil variant) {
      CachePool.Inner_DataUtil var10000;
      switch (variant) {
         case init:
         case update:
         case flush:
            var10000 = CachePool.Inner_DataUtil.init;
            break;
         case load:
            var10000 = CachePool.Inner_DataUtil.update;
            break;
         case read:
         case sync:
            var10000 = CachePool.Inner_DataUtil.write;
            break;
         case write:
            var10000 = CachePool.Inner_DataUtil.read;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_ByteReader(ByteReader button) {
      return "overlay-toggle:" + button.load_I + ":" + button.read + ":" + button.write + ":" + button.update + ":" + button.init_l;
   }

   @FunctionalInterface
   public interface Inner_DataUtil {
      void onPress(ByteReader var1);
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }
}
