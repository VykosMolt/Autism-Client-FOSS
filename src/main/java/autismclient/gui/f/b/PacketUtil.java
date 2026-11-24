package autismclient.gui.f.b;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class PacketUtil {
   private PacketUtil() {
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      int x,
      int y,
      int width,
      int height,
      String label,
      ByteReader.Inner_StreamUtil variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      init(graphics, font, x, y, width, height, label, variant, enabled, l.read, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      int x,
      int y,
      int width,
      int height,
      String label,
      ByteReader.Inner_StreamUtil variant,
      boolean enabled,
      l tone,
      int mouseX,
      int mouseY
   ) {
      ByteReader button = ByteReader.init(x, y, width, height, Component.literal(label), ignored -> {});
      button.read(width);
      button.init_ByteReader_ByteReader_StreamU(variant);
      button.init_ByteReader_l(tone);
      button.init_Z = enabled;
      ByteReader.init(graphics, font, button, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height, String label, boolean state, String animationKey, int mouseX, int mouseY
   ) {
      init(graphics, font, x, y, width, height, label, state, animationKey, true, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      int x,
      int y,
      int width,
      int height,
      String label,
      boolean state,
      String animationKey,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      ByteReader button = ByteReader.init(x, y, width, height, Component.literal(label), ignored -> {});
      button.read(width);
      button.init_ByteReader_ByteReader_StreamU(state ? ByteReader.Inner_StreamUtil.read : ByteReader.Inner_StreamUtil.load);
      button.init_ByteReader_Z(state).init_ByteReader_Str(animationKey);
      button.init_Z = enabled;
      ByteReader.init(graphics, font, button, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height, String label, boolean selected, int mouseX, int mouseY
   ) {
      init(graphics, font, x, y, width, height, label, selected ? ByteReader.Inner_StreamUtil.write : ByteReader.Inner_StreamUtil.update, true, mouseX, mouseY);
   }
}
