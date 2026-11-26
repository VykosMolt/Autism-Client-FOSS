package autismclient.util;

import autismclient.modules.Module;
import autismclient.modules.c;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class KeyHolder {
   private KeyHolder() {
   }

   public static void init() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if ((AutismLiteVariant.enabled() || autismclient.e.UiHelper.init()) && !autismclient.modules.aD.init_Z()) {
                  Module module = autismclient.modules.al.init_Module_Str("bed-defender");
                  Minecraft mc = Minecraft.getInstance();
                  if (module instanceof autismclient.modules.BedDefenderModule bed
                     && bed.isEnabled()
                     && Boolean.parseBoolean(bed.value("visualize"))
                     && mc.level != null
                     && mc.player != null
                     && !mc.gui.hud.isHidden()) {
                     List<autismclient.modules.BedDefenderModule.Inner_CachePool> cells = bed.init_List_5();
                     if (!cells.isEmpty()) {
                        ClientLevel level = mc.level;
                        Vec3 camera = context.levelState().cameraRenderState.pos;
                        ArrayList<autismclient.modules.BedDefenderModule.Inner_CachePool> sorted = new ArrayList<>(cells);
                        sorted.sort(
                           Comparator.<autismclient.modules.BedDefenderModule.Inner_CachePool>comparingDouble(cell -> cell.init_BlockPos().distToCenterSqr(camera))
                              .reversed()
                        );
                        Map<BlockPos, autismclient.modules.BedDefenderModule.Inner_CachePool> solid = new HashMap<>();

                        for (autismclient.modules.BedDefenderModule.Inner_CachePool cell : sorted) {
                           solid.put(cell.init_BlockPos(), cell);
                        }

                        int alpha = Math.clamp(Integer.parseInt(bed.value("ghost-opacity")), 5, 80) * 255 / 100;
                        int ghostTint = bed.init_I_6();
                        int nextLayer = sorted.stream().mapToInt(autismclient.modules.BedDefenderModule.Inner_CachePool::init_I).min().orElse(1);
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              context.poseStack(),
                              autismclient.render.AutismRenderTypes.aiGhost(),
                              (pose, buffer) -> {
                                 int quads = 0;

                                 for (autismclient.modules.BedDefenderModule.Inner_CachePool cell : sorted) {
                                    BlockPos p = cell.init_BlockPos();
                                    if (level.isLoaded(p) && level.getBlockState(p).canBeReplaced()) {
                                       float fade = cell.init_I() <= nextLayer ? 1.0F : c.init(Math.sqrt(new AABB(p).distanceToSqr(camera)));
                                       int layerAlpha = Math.round(alpha * fade);
                                       if (layerAlpha != 0) {
                                          autismclient.util.oresim.OreGhostModelCache.Inner_StreamUtil model = autismclient.util.oresim.OreGhostModelCache.init(
                                             cell.init_BlockState()
                                          );

                                          for (autismclient.util.oresim.OreGhostModelCache.Inner_DataUtil face : model.init_List()) {
                                             if (++quads > 4096) {
                                                return;
                                             }

                                             Direction d = face.init_Direction();
                                             autismclient.modules.BedDefenderModule.Inner_CachePool neighbour = d == null ? null : solid.get(p.relative(d));
                                             if (neighbour == null || cell.init_I() < neighbour.init_I()) {
                                                float shade = face.load() == null ? 1.0F : level.cardinalLighting().byFace(face.load());
                                                BlockTintSource tintSource = face.init_I() < 0
                                                   ? null
                                                   : mc.getBlockColors().getTintSource(cell.init_BlockState(), face.init_I());
                                                int tint = tintSource == null ? 16777215 : tintSource.colorInWorld(cell.init_BlockState(), level, p);
                                                if (!cell.init_Z()) {
                                                   tint = tint & 0xFF0000 | (tint >> 8 & 0xFF) * 155 / 255 << 8 | (tint & 0xFF) * 175 / 255;
                                                }

                                                int r = (int)((tint >> 16 & 0xFF) * (ghostTint >> 16 & 0xFF) / 255.0F * shade);
                                                int g = (int)((tint >> 8 & 0xFF) * (ghostTint >> 8 & 0xFF) / 255.0F * shade);
                                                int b = (int)((tint & 0xFF) * (ghostTint & 0xFF) / 255.0F * shade);
                                                int color = layerAlpha << 24 | r << 16 | g << 8 | b;
                                                float[] data = face.init_FArr();

                                                for (int v = 0; v < 4; v++) {
                                                   int k = v * 5;
                                                   buffer.addVertex(
                                                         pose,
                                                         (float)(p.getX() - camera.x + data[k]),
                                                         (float)(p.getY() - camera.y + data[k + 1]),
                                                         (float)(p.getZ() - camera.z + data[k + 2])
                                                      )
                                                      .setUv(data[k + 3], data[k + 4])
                                                      .setColor(color);
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           );
                     }
                  }
               }
            }
         );
   }
}
