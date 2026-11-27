package autismclient.util;

import autismclient.util.macro.MacroExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenMacroStopWidget {
   private static final int WIDTH = 172;
   private static final int load = 10;
   private static final int read = 58;
   private static final int write = 16;
   private static final int update = 3;
   private static final int sync = 40;
   private static volatile autismclient.gui.f.NetHelper bounds;

   private ScreenMacroStopWidget() {
   }

   public static boolean isVisible(Minecraft mc) {
      if (MacroExecutor.hasVisibleActiveRun() && !autismclient.modules.aD.init_Z()) {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         return module == null || !module.isActive() || mc.gui.screen() instanceof LevelLoadingScreen;
      } else {
         return false;
      }
   }

   public static void render(GuiGraphicsExtractor context, Screen screen, int mouseX, int mouseY, autismclient.gui.f.NetHelper... avoid) {
      bounds = null;
      Minecraft mc = Minecraft.getInstance();
      int panelH = MacroStatusHudRenderer.layoutHeight(mc.font, 172, 10);
      if (panelH > 0) {
         int x = Math.max(0, cA.init_I() - 172);
         int h = panelH + 3 + 16;
         int y = init_I_Screen(screen, avoid, x, 172, h);
         if (y + h > cA.load_I()) {
            y = 0;
         }

         MacroStatusHudRenderer.init(context, mc.font, x, y, 172, 10, y > 0, true, false);
         autismclient.gui.f.NetHelper stop = autismclient.gui.f.NetHelper.init(x + 172 - 58 - 4, y + panelH + 3, 58, 16);
         int vx = cA.init_I_D(mouseX);
         int vy = cA.init_I_D(mouseY);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, mc.font, vx, vy);
         autismclient.gui.f.b.CachePool.init(ui, stop, "Stop", GuiIconAtlas.STOP, autismclient.gui.f.b.CachePool.Inner_DataUtil.update, stop.init_Z(vx, vy), false, 0.0F);
         bounds = stop;
      }
   }

   public static boolean handleClick(double mouseX, double mouseY) {
      autismclient.gui.f.NetHelper stop = bounds;
      if (stop != null && isVisible(Minecraft.getInstance())) {
         if (!stop.init_Z(cA.init_I_D(mouseX), cA.init_I_D(mouseY))) {
            return false;
         }

         bounds = null;
         MacroExecutor.stopAll();
         bi.init("Macro stopped", -14249);
         return true;
      } else {
         return false;
      }
   }

   static autismclient.gui.f.NetHelper getBounds() {
      return bounds;
   }

   private static int init_I_Screen(Screen screen, autismclient.gui.f.NetHelper[] avoid, int x, int w, int h) {
      int y = 0;

      for (int pass = 0; pass < 8; pass++) {
         boolean moved = false;
         if (avoid != null) {
            for (autismclient.gui.f.NetHelper box : avoid) {
               if (box != null
                  && box.update() > 0
                  && box.sync() > 0
                  && box.read() < x + w
                  && box.read() + box.update() > x
                  && box.write() < y + h
                  && box.write() + box.sync() > y) {
                  y = box.write() + box.sync() + 3;
                  moved = true;
               }
            }
         }

         if (screen == null) {
            if (!moved) {
               break;
            }
         } else {
            for (GuiEventListener child : screen.children()) {
               if (child instanceof AbstractWidget widget && widget.visible && widget.getHeight() <= 40) {
                  int left = cA.init_I_D(widget.getX());
                  int right = cA.init_I_D(widget.getRight());
                  int top = cA.init_I_D(widget.getY());
                  int bottom = cA.init_I_D(widget.getBottom());
                  if (left < x + w && right > x && top < y + h && bottom > y) {
                     y = bottom + 3;
                     moved = true;
                  }
               }
            }

            if (!moved) {
               break;
            }
         }
      }

      return y;
   }
}
