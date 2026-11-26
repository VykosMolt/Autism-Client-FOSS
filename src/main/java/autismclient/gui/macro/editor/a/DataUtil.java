package autismclient.gui.macro.editor.a;

import autismclient.gui.f.NetHelper;
import autismclient.gui.f.b.ByteReader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class DataUtil {
   public static final int init = 52;
   public static final int load = 13;

   private DataUtil() {
   }

   public static ByteReader init(
      GuiGraphicsExtractor graphics,
      Font font,
      NetHelper bounds,
      autismclient.gui.macro.editor.StreamUtil mode,
      boolean capturing,
      boolean active,
      int mouseX,
      int mouseY,
      Runnable action
   ) {
      return init(graphics, font, bounds, mode, capturing, active, mouseX, mouseY, null, null, action);
   }

   public static ByteReader init(
      GuiGraphicsExtractor graphics,
      Font font,
      NetHelper bounds,
      autismclient.gui.macro.editor.StreamUtil mode,
      boolean capturing,
      boolean active,
      int mouseX,
      int mouseY,
      String idleLabel,
      String capturingLabel,
      Runnable action
   ) {
      autismclient.gui.macro.editor.StreamUtil resolvedMode = mode == null ? autismclient.gui.macro.editor.StreamUtil.NONE : mode;
      Runnable resolvedAction = action == null ? () -> {} : action;
      ByteReader button = ByteReader.init(
         bounds.read(),
         bounds.write(),
         bounds.update(),
         bounds.sync(),
         Component.literal(init(resolvedMode, capturing, idleLabel, capturingLabel)),
         ignored -> resolvedAction.run()
      );
      button.init_ByteReader_ByteReader_StreamU(capturing ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.PRIMARY);
      button.init_Z = active;
      ByteReader.init(graphics, font, button, mouseX, mouseY);
      return button;
   }

   public static String init(autismclient.gui.macro.editor.StreamUtil mode, boolean capturing) {
      return init(mode, capturing, null, null);
   }

   public static String init(autismclient.gui.macro.editor.StreamUtil mode, boolean capturing, String idleLabel, String capturingLabel) {
      if (capturing && capturingLabel != null && !capturingLabel.isBlank()) {
         return capturingLabel;
      }

      if (!capturing && idleLabel != null && !idleLabel.isBlank()) {
         return idleLabel;
      }

      if (capturing) {
         return "Done";
      }

      String var10000;
      switch (mode == null ? autismclient.gui.macro.editor.StreamUtil.NONE : mode) {
         case ITEM_SLOT:
         case PACKET_NAME:
         case CUSTOM_MENU_INPUT:
         case CUSTOM_MENU_BUTTON:
         case CUSTOM_MENU_TITLE:
            var10000 = "Pick";
            break;
         case PACKET_CLICK_TARGET:
            var10000 = "Re-capture";
            break;
         case BLOCK_ID:
         case ENTITY_ID:
         case BLOCK_CATALOG:
         case NONE:
            var10000 = "Capture";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }
}
