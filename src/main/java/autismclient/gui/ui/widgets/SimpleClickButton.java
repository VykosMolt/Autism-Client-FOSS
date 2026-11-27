package autismclient.gui.ui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import autismclient.gui.f.b.CachePool;

public final class SimpleClickButton {
   private final autismclient.gui.f.NetHelper init_NetHelper;
   private final Component init_Component;
   private final Runnable action;
   private final CachePool.Inner_DataUtil init_CachePool_DataUtil;
   private boolean active = true;

   public SimpleClickButton(int x, int y, int width, int height, Component label, CachePool.Inner_DataUtil tone, Runnable action) {
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, width, height);
      this.init_Component = label == null ? Component.empty() : label;
      this.init_CachePool_DataUtil = tone == null ? CachePool.Inner_DataUtil.init : tone;
      this.action = action;
   }

   public SimpleClickButton init(boolean active) {
      this.active = active;
      return this;
   }

   public void init(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      CachePool.init(
         autismclient.gui.f.UiHelper.init(graphics, font, mouseX, mouseY),
         this.init_NetHelper,
         this.init_Component.getString(),
         this.init_CachePool_DataUtil,
         this.active && this.init_NetHelper.init_Z(mouseX, mouseY),
         false
      );
   }

   public boolean init(double mouseX, double mouseY, int mouseButton) {
      if (this.active && mouseButton == 0 && this.init_NetHelper.init_Z((int)mouseX, (int)mouseY)) {
         if (this.action != null) {
            this.action.run();
         }

         return true;
      } else {
         return false;
      }
   }
}
