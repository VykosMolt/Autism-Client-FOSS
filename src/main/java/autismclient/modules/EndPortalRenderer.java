package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismBufferSource;
import autismclient.util.WorldLineRenderer;
import autismclient.util.StrongholdTriangulator;
import autismclient.util.EndPortalDisplayHelper;
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

public final class EndPortalRenderer {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final AutismBufferSource.LazySource BUFFERS = new AutismBufferSource.LazySource(32768);
   private static final Matrix4f PROJECTION = new Matrix4f();
   private static final Vector4f SCRATCH_VECTOR = new Vector4f();
   private static final ItemStack FRAME_ICON = new ItemStack(Items.END_PORTAL_FRAME);

   private EndPortalRenderer() {
   }

   private static EndPortalModule activeModule() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.PathHelper.init()) {
         return null;
      } else {
         return MINECRAFT.level != null && MINECRAFT.player != null && !MINECRAFT.gui.hud.isHidden() && !aD.init_Z() && !am.clear()
            ? EndPortalModule.active()
            : null;
      }
   }

   public static boolean init_Z() {
      EndPortalModule m = activeModule();
      if (m == null) {
         return false;
      }

      Vec3 target = m.target();
      return !m.frames().isEmpty() || m.showsTracer() && target != null || init(m, target, WorldProjectionHelper.cameraPosition());
   }

   private static boolean init(EndPortalModule module, Vec3 target, Vec3 camera) {
      if (module.sync() && EndPortalDisplayHelper.init(camera, target, am.init_I())) {
         int x = (int)Math.floor(target.x) >> 4;
         int z = (int)Math.floor(target.z) >> 4;
         return MINECRAFT.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false) != null;
      } else {
         return false;
      }
   }

   public static boolean showsGuide() {
      EndPortalModule m = activeModule();
      return m != null && m.showsGuide() && MINECRAFT.gui.screen() == null;
   }

   public static void render(PoseStack matrices) {
      EndPortalModule module = activeModule();
      if (module != null) {
         Vec3 camera = WorldProjectionHelper.cameraPosition();
         Vec3 target = module.target();
         AutismBufferSource source = BUFFERS.init();
         int color = module.color();
         if (!module.frames().isEmpty()) {
            VertexConsumer fill = source.buffer(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());

            for (BlockPos pos : module.frames()) {
               WorldLineRenderer.box(matrices.last(), fill, frameBox(pos).move(camera.scale(-1.0)), EndPortalDisplayHelper.withOpacity(color, 32));
            }
         }

         if (init(module, target, camera)) {
            EndPortalDisplayHelper.BeamColumn column = EndPortalDisplayHelper.beam(camera, target, MINECRAFT.level.getMinY(), MINECRAFT.level.getMaxY() + 1.0);
            if (column != null) {
               init(matrices, source.buffer(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough()), column, color);
            }
         }

         VertexConsumer lines = source.buffer(autismclient.render.AutismRenderTypes.tracerEspLines());

         for (BlockPos pos : module.frames()) {
            WorldLineRenderer.box(matrices.last(), lines, frameBox(pos).inflate(0.003).move(camera.scale(-1.0)), EndPortalDisplayHelper.withOpacity(color, 221), 1.5F);
         }

         if (module.showsTracer() && target != null) {
            Vec3 relative = target.subtract(camera);
            if (relative.lengthSqr() > 36864.0) {
               relative = relative.normalize().scale(192.0);
            }

            Vector3fc forward = MINECRAFT.gameRenderer.mainCamera().forwardVector();
            Vector3fc up = MINECRAFT.gameRenderer.mainCamera().upVector();
            Vec3 fwdVec = new Vec3(forward.x(), forward.y(), forward.z());
            Vec3 upVec = new Vec3(up.x(), up.y(), up.z());
            ar.drawClippedSegment(
               matrices.last(), lines, fwdVec.x * 2.0, fwdVec.y * 2.0, fwdVec.z * 2.0, relative.x, relative.y, relative.z, fwdVec, upVec, color, 1.5F
            );
         }

         source.drawAll();
      }
   }

   private static void init(PoseStack matrices, VertexConsumer fill, EndPortalDisplayHelper.BeamColumn column, int color) {
      init(matrices, fill, column, column.read(), column.write(), color, 10, 28, 88);
   }

   private static void init(
      PoseStack matrices, VertexConsumer fill, EndPortalDisplayHelper.BeamColumn column, double minY, double maxY, int color, int outer, int middle, int core
   ) {
      WorldLineRenderer.box(matrices.last(), fill, column.box(column.sync(), minY, maxY), EndPortalDisplayHelper.withOpacity(color, outer));
      WorldLineRenderer.box(matrices.last(), fill, column.box(column.sync() * 0.5, minY, maxY), EndPortalDisplayHelper.withOpacity(color, middle));
      WorldLineRenderer.box(matrices.last(), fill, column.box(column.update(), minY, maxY), EndPortalDisplayHelper.withOpacity(color, core));
   }

   private static AABB frameBox(BlockPos p) {
      return new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 0.8125, p.getZ() + 1);
   }

   private static EndPortalRenderer.PortalMarker markerFor(GuiGraphicsExtractor graphics, EndPortalModule module) {
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, MINECRAFT.font, 0, 0);
      Vec3 target = module.target();
      float scale = module.init_F();
      String label = module.showsCoordinates()
         ? EndPortalDisplayHelper.init(target, module.close(), module.open())
         : EndPortalDisplayHelper.init(MINECRAFT.player.position(), target, module.close(), module.open());
      String prompt = EndPortalDisplayHelper.prompt(target != null, EndPortalModule.load_Z(), module.flush());
      if (!prompt.isEmpty()) {
         label = prompt;
      }

      autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgeSize size = autismclient.gui.ui.widgets.WorldIndicatorBadge.size(
         ui.init_PoolEntry().measureWidth(label), 9, !label.isEmpty(), target != null
      );
      autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgePlacement placement;
      if (target == null) {
         placement = autismclient.gui.ui.widgets.WorldIndicatorBadge.place(0.0F, 0.0F, -1.0F, 180.0, ui.init_I(), ui.load_I(), size, scale, 104.0F * scale);
      } else {
         if (!WorldProjectionHelper.projection(PROJECTION)) {
            return null;
         }

         Vec3 camera = WorldProjectionHelper.cameraPosition();
         double y = module.close() ? target.y : camera.y;
         SCRATCH_VECTOR.set((float)(target.x - camera.x), (float)(y - camera.y), (float)(target.z - camera.z), 1.0F);
         PROJECTION.transform(SCRATCH_VECTOR);
         float yaw = MINECRAFT.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.yRot;
         float ring = 76.0F + (Math.max(size.init(), size.load()) / 2.0F + 24.0F) * scale;
         placement = autismclient.gui.ui.widgets.WorldIndicatorBadge.place(
            SCRATCH_VECTOR.x, SCRATCH_VECTOR.y, SCRATCH_VECTOR.w, StrongholdTriangulator.relativeYaw(camera, target, yaw), ui.init_I(), ui.load_I(), size, scale, ring
         );
      }

      return placement == null ? null : new EndPortalRenderer.PortalMarker(placement, size, label, target != null, scale, module.color());
   }

   public static autismclient.gui.f.NetHelper hudBounds(GuiGraphicsExtractor graphics) {
      EndPortalModule module = activeModule();
      if (module == null) {
         return autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
      }

      EndPortalRenderer.PortalMarker marker = markerFor(graphics, module);
      return marker == null
         ? autismclient.gui.f.NetHelper.init(0, 0, 0, 0)
         : autismclient.gui.ui.widgets.WorldIndicatorBadge.bounds(marker.init_TaskTimer_DataUtil, marker.init_TaskTimer_StreamUt, marker.init_F);
   }

   public static void renderHud(GuiGraphicsExtractor graphics) {
      EndPortalModule module = activeModule();
      if (module != null && module.showsGuide() && MINECRAFT.gui.screen() == null) {
         EndPortalRenderer.PortalMarker marker = markerFor(graphics, module);
         if (marker != null) {
            autismclient.gui.ui.widgets.WorldIndicatorBadge.render(
               autismclient.gui.f.UiHelper.init(graphics, MINECRAFT.font, 0, 0),
               marker.init_TaskTimer_DataUtil,
               marker.init_TaskTimer_StreamUt,
               marker.init_F,
               FRAME_ICON,
               marker.label,
               marker.init_I,
               marker.init_Z
            );
         }
      }
   }

   private record PortalMarker(
      autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgePlacement init_TaskTimer_DataUtil,
      autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgeSize init_TaskTimer_StreamUt,
      String label,
      boolean init_Z,
      float init_F,
      int init_I
   ) {
   }
}
