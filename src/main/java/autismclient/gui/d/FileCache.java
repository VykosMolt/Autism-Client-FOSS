package autismclient.gui.d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FileCache extends Button {
   private final Identifier init_Identifier;
   private final int init_I;
   private final int load;
   private final int read;

   public FileCache(int x, int y, int width, int height, Component label, Identifier icon, int textureSize, int iconSize, OnPress onPress) {
      this(x, y, width, height, label, icon, textureSize, textureSize, iconSize, onPress);
   }

   public FileCache(int x, int y, int width, int height, Component label, Identifier icon, int textureWidth, int textureHeight, int iconSize, OnPress onPress) {
      super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
      this.init_Identifier = icon;
      this.init_I = Math.max(1, textureWidth);
      this.load = Math.max(1, textureHeight);
      this.read = Math.max(1, iconSize);
   }

   @Override
   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      Font font = Minecraft.getInstance().font;
      int color = this.active ? -1 : -7703436;
      if (this.init_Identifier == null) {
         int textW = font.width(this.getMessage());
         graphics.text(font, this.getMessage(), this.getX() + (this.getWidth() - textW) / 2, this.init(font), color);
      } else {
         int drawSize = Math.min(this.read, Math.max(1, Math.min(this.getWidth() - 4, this.getHeight() - 4)));
         boolean hasRoomForText = this.getWidth() >= drawSize + font.width(this.getMessage()) + 18;
         int iconX = hasRoomForText ? this.getX() + 3 : this.getX() + (this.getWidth() - drawSize) / 2;
         int iconY = this.getY() + (this.getHeight() - drawSize) / 2;
         graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            this.init_Identifier,
            iconX,
            iconY,
            0.0F,
            0.0F,
            drawSize,
            drawSize,
            this.init_I,
            this.load,
            this.init_I,
            this.load,
            color
         );
         if (hasRoomForText) {
            int textX = iconX + drawSize + 4;
            graphics.text(font, this.getMessage(), textX, this.init(font), color);
         }
      }
   }

   private int init(Font font) {
      int spareHeight = Math.max(0, this.getHeight() - 9);
      return this.getY() + (spareHeight + 1) / 2 + 1;
   }
}
