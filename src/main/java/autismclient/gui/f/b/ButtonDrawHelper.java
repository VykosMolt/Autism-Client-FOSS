package autismclient.gui.f.b;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ButtonDrawHelper {
   private ButtonDrawHelper() {
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      int x,
      int y,
      int width,
      int height,
      String label,
      OverlayActionButton.Inner_StreamUtil variant,
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
      OverlayActionButton.Inner_StreamUtil variant,
      boolean enabled,
      l tone,
      int mouseX,
      int mouseY
   ) {
      OverlayActionButton button = OverlayActionButton.init(x, y, width, height, Component.literal(label), ignored -> {});
      button.read(width);
      button.init_ByteReader_ByteReader_StreamU(variant);
      button.init_ByteReader_l(tone);
      button.init_Z = enabled;
      OverlayActionButton.init(graphics, font, button, mouseX, mouseY);
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
      OverlayActionButton button = OverlayActionButton.init(x, y, width, height, Component.literal(label), ignored -> {});
      button.read(width);
      button.init_ByteReader_ByteReader_StreamU(state ? OverlayActionButton.Inner_StreamUtil.SUCCESS : OverlayActionButton.Inner_StreamUtil.DANGER);
      button.init_ByteReader_Z(state).init_ByteReader_Str(animationKey);
      button.init_Z = enabled;
      OverlayActionButton.init(graphics, font, button, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height, String label, boolean selected, int mouseX, int mouseY
   ) {
      init(graphics, font, x, y, width, height, label, selected ? OverlayActionButton.Inner_StreamUtil.PRIMARY : OverlayActionButton.Inner_StreamUtil.GHOST, true, mouseX, mouseY);
   }
}
