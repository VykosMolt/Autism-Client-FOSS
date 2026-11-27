package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import autismclient.gui.f.b.l;

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
      OverlayActionButton.ButtonTone variant,
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
      OverlayActionButton.ButtonTone variant,
      boolean enabled,
      l tone,
      int mouseX,
      int mouseY
   ) {
      OverlayActionButton button = OverlayActionButton.create(x, y, width, height, Component.literal(label), ignored -> {});
      button.setWidth(width);
      button.setTone(variant);
      button.setTextTone(tone);
      button.active = enabled;
      OverlayActionButton.render(graphics, font, button, mouseX, mouseY);
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
      OverlayActionButton button = OverlayActionButton.create(x, y, width, height, Component.literal(label), ignored -> {});
      button.setWidth(width);
      button.setTone(state ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.DANGER);
      button.setToggleState(state).setAnimationKey(animationKey);
      button.active = enabled;
      OverlayActionButton.render(graphics, font, button, mouseX, mouseY);
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height, String label, boolean selected, int mouseX, int mouseY
   ) {
      init(graphics, font, x, y, width, height, label, selected ? OverlayActionButton.ButtonTone.PRIMARY : OverlayActionButton.ButtonTone.GHOST, true, mouseX, mouseY);
   }
}
