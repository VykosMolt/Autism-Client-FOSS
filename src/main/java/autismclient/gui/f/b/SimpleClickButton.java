package autismclient.gui.f.b;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class SimpleClickButton {
   private final autismclient.gui.f.NetHelper init_NetHelper;
   private final Component init_Component;
   private final Runnable init_Runnable;
   private final CachePool.Inner_DataUtil init_CachePool_DataUtil;
   private boolean init_Z = true;

   public SimpleClickButton(int x, int y, int width, int height, Component label, CachePool.Inner_DataUtil tone, Runnable action) {
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, width, height);
      this.init_Component = label == null ? Component.empty() : label;
      this.init_CachePool_DataUtil = tone == null ? CachePool.Inner_DataUtil.init : tone;
      this.init_Runnable = action;
   }

   public SimpleClickButton init(boolean active) {
      this.init_Z = active;
      return this;
   }

   public void init(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      CachePool.init(
         autismclient.gui.f.UiHelper.init(graphics, font, mouseX, mouseY),
         this.init_NetHelper,
         this.init_Component.getString(),
         this.init_CachePool_DataUtil,
         this.init_Z && this.init_NetHelper.init_Z(mouseX, mouseY),
         false
      );
   }

   public boolean init(double mouseX, double mouseY, int mouseButton) {
      if (this.init_Z && mouseButton == 0 && this.init_NetHelper.init_Z((int)mouseX, (int)mouseY)) {
         if (this.init_Runnable != null) {
            this.init_Runnable.run();
         }

         return true;
      } else {
         return false;
      }
   }
}
