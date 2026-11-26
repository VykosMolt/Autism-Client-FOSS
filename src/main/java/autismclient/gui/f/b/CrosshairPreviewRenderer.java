package autismclient.gui.f.b;

import autismclient.modules.CrosshairRenderer;
import autismclient.modules.CrosshairRenderState;
import autismclient.util.cA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class CrosshairPreviewRenderer {
   private CrosshairPreviewRenderer() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, CrosshairRenderState.Inner_DataUtil look) {
      if (bounds.update() > 8 && bounds.sync() > 8) {
         BufferHelper.init_V(context, bounds, "Preview");
         int top = BufferHelper.init_I(context, bounds, "Preview") + 2;
         load(context, autismclient.gui.f.NetHelper.init(bounds.read() + 4, top, bounds.update() - 8, bounds.load() - top - 4), look);
      }
   }

   public static void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper box, CrosshairRenderState.Inner_DataUtil look) {
      if (box.update() > 0 && box.sync() > 0) {
         GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
         int half = box.update() / 2;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(box.read(), box.write(), half, box.sync()), -15395044);
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(box.read() + half, box.write(), box.update() - half, box.sync()), -2499357
         );
         float pixelsPerUnit = (float)(2.0 * cA.init_D());
         cA.init(graphics, box.read(), box.write(), box.init(), box.load());

         try {
            CrosshairRenderer.init_V_GuiGraphicsExtract_2(
               graphics,
               look,
               box.read() + half,
               box.write() + box.sync() / 2.0,
               pixelsPerUnit,
               Math.max(1, Math.round((Math.min(box.update(), box.sync()) - 4) * pixelsPerUnit))
            );
         } finally {
            graphics.disableScissor();
         }
      }
   }
}
