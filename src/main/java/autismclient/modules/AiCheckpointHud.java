package autismclient.modules;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class AiCheckpointHud {
   private static final Matrix4f PROJECTION = new Matrix4f();
   private static final Vector4f CLIP = new Vector4f();

   private AiCheckpointHud() {
   }

   public static void render(autismclient.gui.f.SyncManager ui, List<autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil> checkpoints) {
      if (!checkpoints.isEmpty() && WorldProjectionHelper.init(PROJECTION)) {
         Vec3 camera = WorldProjectionHelper.init();
         GuiGraphicsExtractor graphics = ui.init_GuiGraphicsExtract();
         autismclient.gui.ui.UiColorScheme colors = ui.init_TableIndex().init_JsonUtil();

         for (int i = 0; i < checkpoints.size(); i++) {
            Vec3 feet = checkpoints.get(i).feet();
            CLIP.set((float)(feet.x - camera.x), (float)(feet.y - camera.y + 0.2), (float)(feet.z - camera.z), 1.0F);
            PROJECTION.transform(CLIP);
            if (!(CLIP.w <= 0.05F) && !(Math.abs(CLIP.x) >= CLIP.w) && !(Math.abs(CLIP.y) >= CLIP.w)) {
               int sx = Math.round((1.0F + CLIP.x / CLIP.w) * ui.init_I() * 0.5F);
               int sy = Math.round((1.0F - CLIP.y / CLIP.w) * ui.load_I() * 0.5F);
               String label = Integer.toString(i + 1);
               int labelWidth = ui.init_PoolEntry().init(label);
               int boxWidth = Math.max(16, labelWidth + 8);
               autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(sx - boxWidth / 2, sy - 25, boxWidth, 25), 5, colors.sync);
               ui.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, label, sx - labelWidth / 2, sy - 22, colors.prev);
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(sx - 4, sy - 10, 8, 8), true, colors.clear);
            }
         }
      }
   }
}
