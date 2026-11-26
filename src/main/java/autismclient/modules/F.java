package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.PkgCls_c;
import autismclient.util.cI;
import autismclient.util.cS;
import autismclient.util.cV;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class F {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final PkgCls_c.Inner_DataUtil init_c_DataUtil = new PkgCls_c.Inner_DataUtil(32768);
   private static final Matrix4f init_Matrix4f = new Matrix4f();
   private static final Vector4f init_Vector4f = new Vector4f();
   private static final ItemStack init_ItemStack = new ItemStack(Items.END_PORTAL_FRAME);

   private F() {
   }

   private static EndPortalModule init_E() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.PathHelper.init()) {
         return null;
      } else {
         return init_Minecraft.level != null && init_Minecraft.player != null && !init_Minecraft.gui.hud.isHidden() && !aD.init_Z() && !am.clear()
            ? EndPortalModule.init_E()
            : null;
      }
   }

   public static boolean init_Z() {
      EndPortalModule m = init_E();
      if (m == null) {
         return false;
      }

      Vec3 target = m.init_Vec3();
      return !m.init_List_3().isEmpty() || m.write_Z() && target != null || init(m, target, Q.init());
   }

   private static boolean init(EndPortalModule module, Vec3 target, Vec3 camera) {
      if (module.sync() && cV.init(camera, target, am.init_I())) {
         int x = (int)Math.floor(target.x) >> 4;
         int z = (int)Math.floor(target.z) >> 4;
         return init_Minecraft.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false) != null;
      } else {
         return false;
      }
   }

   public static boolean load() {
      EndPortalModule m = init_E();
      return m != null && m.update_Z() && init_Minecraft.gui.screen() == null;
   }

   public static void init(PoseStack matrices) {
      EndPortalModule module = init_E();
      if (module != null) {
         Vec3 camera = Q.init();
         Vec3 target = module.init_Vec3();
         PkgCls_c source = init_c_DataUtil.init();
         int color = module.init_I_2();
         if (!module.init_List_3().isEmpty()) {
            VertexConsumer fill = source.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());

            for (BlockPos pos : module.init_List_3()) {
               cI.init(matrices.last(), fill, init(pos).move(camera.scale(-1.0)), cV.init(color, 32));
            }
         }

         if (init(module, target, camera)) {
            cV.Inner_DataUtil column = cV.init(camera, target, init_Minecraft.level.getMinY(), init_Minecraft.level.getMaxY() + 1.0);
            if (column != null) {
               init(matrices, source.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough()), column, color);
            }
         }

         VertexConsumer lines = source.init(autismclient.render.AutismRenderTypes.tracerEspLines());

         for (BlockPos pos : module.init_List_3()) {
            cI.init(matrices.last(), lines, init(pos).inflate(0.003).move(camera.scale(-1.0)), cV.init(color, 221), 1.5F);
         }

         if (module.write_Z() && target != null) {
            Vec3 relative = target.subtract(camera);
            if (relative.lengthSqr() > 36864.0) {
               relative = relative.normalize().scale(192.0);
            }

            Vector3fc forward = init_Minecraft.gameRenderer.mainCamera().forwardVector();
            Vector3fc up = init_Minecraft.gameRenderer.mainCamera().upVector();
            Vec3 fwdVec = new Vec3(forward.x(), forward.y(), forward.z());
            Vec3 upVec = new Vec3(up.x(), up.y(), up.z());
            ar.drawClippedSegment(
               matrices.last(), lines, fwdVec.x * 2.0, fwdVec.y * 2.0, fwdVec.z * 2.0, relative.x, relative.y, relative.z, fwdVec, upVec, color, 1.5F
            );
         }

         source.init();
      }
   }

   private static void init(PoseStack matrices, VertexConsumer fill, cV.Inner_DataUtil column, int color) {
      init(matrices, fill, column, column.read(), column.write(), color, 10, 28, 88);
   }

   private static void init(
      PoseStack matrices, VertexConsumer fill, cV.Inner_DataUtil column, double minY, double maxY, int color, int outer, int middle, int core
   ) {
      cI.init(matrices.last(), fill, column.init(column.sync(), minY, maxY), cV.init(color, outer));
      cI.init(matrices.last(), fill, column.init(column.sync() * 0.5, minY, maxY), cV.init(color, middle));
      cI.init(matrices.last(), fill, column.init(column.update(), minY, maxY), cV.init(color, core));
   }

   private static AABB init(BlockPos p) {
      return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 0.8125, p.getZ() + 1);
   }

   private static F.Inner_DataUtil init(GuiGraphicsExtractor graphics, EndPortalModule module) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, init_Minecraft.font, 0, 0);
      Vec3 target = module.init_Vec3();
      float scale = module.init_F();
      String label = module.read_Z_2()
         ? cV.init(target, module.close(), module.open())
         : cV.init(init_Minecraft.player.position(), target, module.close(), module.open());
      String prompt = cV.init(target != null, EndPortalModule.load_Z(), module.flush());
      if (!prompt.isEmpty()) {
         label = prompt;
      }

      autismclient.gui.f.b.TaskTimer.Inner_StreamUtil size = autismclient.gui.f.b.TaskTimer.init(
         ui.init_PoolEntry().init(label), 9, !label.isEmpty(), target != null
      );
      autismclient.gui.f.b.TaskTimer.Inner_DataUtil placement;
      if (target == null) {
         placement = autismclient.gui.f.b.TaskTimer.init(0.0F, 0.0F, -1.0F, 180.0, ui.init_I(), ui.load_I(), size, scale, 104.0F * scale);
      } else {
         if (!Q.init(init_Matrix4f)) {
            return null;
         }

         Vec3 camera = Q.init();
         double y = module.close() ? target.y : camera.y;
         init_Vector4f.set((float)(target.x - camera.x), (float)(y - camera.y), (float)(target.z - camera.z), 1.0F);
         init_Matrix4f.transform(init_Vector4f);
         float yaw = init_Minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.yRot;
         float ring = 76.0F + (Math.max(size.init(), size.load()) / 2.0F + 24.0F) * scale;
         placement = autismclient.gui.f.b.TaskTimer.init(
            init_Vector4f.x, init_Vector4f.y, init_Vector4f.w, cS.init(camera, target, yaw), ui.init_I(), ui.load_I(), size, scale, ring
         );
      }

      return placement == null ? null : new F.Inner_DataUtil(placement, size, label, target != null, scale, module.init_I_2());
   }

   public static autismclient.gui.f.NetHelper init_NetHelper(GuiGraphicsExtractor graphics) {
      EndPortalModule module = init_E();
      if (module == null) {
         return autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
      }

      F.Inner_DataUtil marker = init(graphics, module);
      return marker == null
         ? autismclient.gui.f.NetHelper.init(0, 0, 0, 0)
         : autismclient.gui.f.b.TaskTimer.init(marker.init_TaskTimer_DataUtil, marker.init_TaskTimer_StreamUt, marker.init_F);
   }

   public static void init_V(GuiGraphicsExtractor graphics) {
      EndPortalModule module = init_E();
      if (module != null && module.update_Z() && init_Minecraft.gui.screen() == null) {
         F.Inner_DataUtil marker = init(graphics, module);
         if (marker != null) {
            autismclient.gui.f.b.TaskTimer.init(
               autismclient.gui.f.UiHelper.init(graphics, init_Minecraft.font, 0, 0),
               marker.init_TaskTimer_DataUtil,
               marker.init_TaskTimer_StreamUt,
               marker.init_F,
               init_ItemStack,
               marker.init_Str,
               marker.init_I,
               marker.init_Z
            );
         }
      }
   }

   private record Inner_DataUtil(
      autismclient.gui.f.b.TaskTimer.Inner_DataUtil init_TaskTimer_DataUtil,
      autismclient.gui.f.b.TaskTimer.Inner_StreamUtil init_TaskTimer_StreamUt,
      String init_Str,
      boolean init_Z,
      float init_F,
      int init_I
   ) {
   }
}
