package autismclient.gui.d;

import autismclient.util.ThemePaletteManager;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class StreamGate extends Button {
   private static final int init_I = -15594737;
   private final autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil;
   private final Supplier<String> init_Supplier;
   private boolean init_Z;

   public StreamGate(int x, int y, int width, int height, Component label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress onPress) {
      this(x, y, width, height, label, tone, null, onPress);
   }

   public StreamGate(
      int x, int y, int width, int height, Component label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Supplier<String> dynamicLabel, OnPress onPress
   ) {
      super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
      this.init_CachePool_DataUtil = tone == null ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init : tone;
      this.init_Supplier = dynamicLabel;
   }

   public StreamGate init(boolean toggled) {
      this.init_Z = toggled;
      return this;
   }

   @Override
   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      Font font = Minecraft.getInstance().font;
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(this.getX(), this.getY(), this.getWidth(), this.getHeight());
      autismclient.gui.f.PacketUtil.init(graphics, bounds, ThemePaletteManager.init_I_I_ct_DataUtil(-15594737, ThemePaletteManager.Inner_DataUtil.BUTTON));
      boolean hovered = this.active
         && mouseX >= this.getX()
         && mouseX < this.getX() + this.getWidth()
         && mouseY >= this.getY()
         && mouseY < this.getY() + this.getHeight();
      String text = this.init_Supplier != null ? this.init_Supplier.get() : this.getMessage().getString();
      autismclient.gui.f.b.CachePool.init(
         autismclient.gui.f.UiHelper.init(graphics, font, mouseX, mouseY),
         bounds,
         text,
         this.init_Z ? this.init_CachePool_DataUtil : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hovered,
         this.init_Z
      );
   }
}
