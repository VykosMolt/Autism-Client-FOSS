package autismclient.modules;

import autismclient.util.cA;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class BedDangerHudRenderer {
   private BedDangerHudRenderer() {
   }

   public static boolean init() {
      DangerModule module = DangerModule.instance();
      return module != null && module.isEnabled() && module.bool("bed-warning") && !module.bedProximity().isEmpty();
   }

   static void init(GuiGraphicsExtractor graphics, DangerModule module) {
      Minecraft mc = Minecraft.getInstance();
      if (init() && mc.player != null && mc.gui.screen() == null && !aD.init_Z()) {
         autismclient.util.AutismHudManager.init_V_GuiGraphicsExtract_2(graphics, mc.font, "bed_danger");
      }
   }

   private static BedDangerHudRenderer.Inner_DataUtil init_u_DataUtil(Font font) {
      DangerModule module = DangerModule.instance();
      List<Component> lines = init()
         ? module.bedProximity().stream().map(group -> group.init(module.bool("bed-distances"))).toList()
         : List.of(Component.literal("Player  20 m").withColor(16755285));
      float scale = module == null ? 1.0F : (float)module.decimal("bed-warning-scale");
      int available = Math.max(1, (int)(cA.init_I() / scale) - 16);
      int width = Math.min(available, Math.max(140, lines.stream().mapToInt(font::width).max().orElse(0) + 16));
      int height = 25 + lines.size() * 13;
      scale = Math.min(scale, Math.max(1.0F, cA.load_I() - 8.0F) / height);
      return new BedDangerHudRenderer.Inner_DataUtil(lines, width, height, scale);
   }

   public static int[] size(Font font) {
      BedDangerHudRenderer.Inner_DataUtil layout = init_u_DataUtil(font);
      return new int[]{Math.max(1, (int)Math.ceil(layout.init_I * layout.init_F)), Math.max(1, (int)Math.ceil(layout.load * layout.init_F))};
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean editor) {
      if (editor || init()) {
         BedDangerHudRenderer.Inner_DataUtil layout = init_u_DataUtil(font);
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, font, 0, 0);
         graphics.pose().pushMatrix();

         try {
            graphics.pose().translate(x, y);
            graphics.pose().scale(layout.init_F, layout.init_F);
            autismclient.gui.f.b.BufferHelper.init_V(context, autismclient.gui.f.NetHelper.init(0, 0, layout.init_I, layout.load), "Bed Danger");
            int rowY = 24;

            for (Component line : layout.init_List) {
               float fit = Math.min(1.0F, (layout.init_I - 12.0F) / Math.max(1, font.width(line)));
               graphics.pose().pushMatrix();
               try {
                  graphics.pose().translate((layout.init_I - font.width(line) * fit) / 2.0F, rowY);
                  graphics.pose().scale(fit, fit);
                  graphics.text(font, line, 0, 0, -1, true);
               } finally {
                  graphics.pose().popMatrix();
               }
               rowY += 13;
            }
         } finally {
            graphics.pose().popMatrix();
         }
      }
   }

   private record Inner_DataUtil(List<Component> init_List, int init_I, int load, float init_F) {
   }
}
