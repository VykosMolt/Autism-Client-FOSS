package autismclient.modules;

import autismclient.compat263.Compat263;
import autismclient.util.AutismBufferSource;
import autismclient.util.WorldLineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class PearlGhostRenderer {
   private static final AutismBufferSource.LazySource BUFFERS = new AutismBufferSource.LazySource(786432);
   private static final Vector3f NORMAL = new Vector3f();
   private static final AvatarRenderState AVATAR_STATE = new AvatarRenderState();
   private static PlayerModel wideModel;
   private static PlayerModel slimModel;
   private static Object modelSet;
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline load_RenderPipeline;
   private static RenderPipeline read;

   private PearlGhostRenderer() {
   }

   public static boolean init() {
      DangerModule danger = DangerModule.instance();
      return danger != null && danger.isEnabled() && !aD.init_Z() && danger.open_Z_6();
   }

   public static void init(PoseStack pose) {
      if (init()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            DangerModule danger = DangerModule.instance();
            Vec3 camera = WorldProjectionHelper.init();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            AutismBufferSource buffers = BUFFERS.init();
            VertexConsumer lines = buffers.init(autismclient.render.AutismRenderTypes.aiPlanLines());

            for (IncomingProjectileScanner.ProjectileWarning warning : danger.read_List_2()) {
               if (!warning.entity().isRemoved()) {
                  int color = danger.warningColor(warning);
                  List<Vec3> points = warning.init_A_DataUtil().points();
                  if (danger.isKindOptionEnabled(warning.kind(), "trajectory") && points.size() > 1) {
                     Entity source = warning.entity();
                     Vec3 offset = new Vec3(
                        Mth.lerp(partial, source.xOld, source.getX()) - source.getX(),
                        Mth.lerp(partial, source.yOld, source.getY()) - source.getY(),
                        Mth.lerp(partial, source.zOld, source.getZ()) - source.getZ()
                     );
                     Vec3 previous = points.getFirst().add(offset);

                     for (int i = 1; i < points.size(); i++) {
                        Vec3 next = points.get(i);
                        if (i < 4) {
                           next = next.add(offset.scale(1.0 - i / 4.0));
                        }

                        if (!warning.init_Z() || (i & 1) == 1) {
                           drawLine(pose, lines, previous, next, camera, color & 16777215 | (warning.init_Z() ? -1610612736 : -671088640), 1.5F);
                        }

                        previous = next;
                     }
                  }

                  if (danger.isKindOptionEnabled(warning.kind(), "trajectory") && warning.init_A_DataUtil().init_Vec3() != null) {
                     Vec3 at = warning.kind() == IncomingProjectileScanner.ProjectileKind.PEARL
                        ? warning.init_A_DataUtil().load()
                        : warning.init_A_DataUtil().init_Vec3();
                     drawCircle(pose, lines, at.add(0.0, 0.025, 0.0), warning.kind() == IncomingProjectileScanner.ProjectileKind.PEARL ? 0.48 : 0.24, camera, color);
                  }
               }
            }

            init(pose.last(), lines, danger.blastPredictions(), camera);
            if (!danger.blastPredictions().isEmpty()) {
               VertexConsumer fill = buffers.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());
               load(pose.last(), fill, danger.blastPredictions(), camera);
            }

            if (danger.isKindEnabled(IncomingProjectileScanner.ProjectileKind.PEARL) && danger.bool("pearl-ghost")) {
               ArrayList<IncomingProjectileScanner.ProjectileWarning> ghosts = new ArrayList<>(danger.write_List());
               ghosts.removeIf(
                  warningx -> warningx.init_A_DataUtil().load() == null
                     || !(warningx.shooter() instanceof AbstractClientPlayer)
                     || !warningx.shooter().isAlive()
                     || warningx.entity().isRemoved()
               );
               ghosts.sort(Comparator.<IncomingProjectileScanner.ProjectileWarning>comparingDouble(warningx -> warningx.init_A_DataUtil().load().distanceToSqr(camera)).reversed());

               for (IncomingProjectileScanner.ProjectileWarning warning : ghosts) {
                  init_V_Minecraft(mc, pose, buffers, warning, camera, partial, danger.decimal("ghost-opacity"));
               }
            }

            buffers.init();
         }
      }
   }

   private static void init_V_Minecraft(
      Minecraft mc, PoseStack pose, AutismBufferSource buffers, IncomingProjectileScanner.ProjectileWarning warning, Vec3 camera, float partial, double opacity
   ) {
      init(
         mc,
         pose,
         buffers,
         (AbstractClientPlayer)warning.shooter(),
         warning.init_A_DataUtil().load(),
         camera,
         partial,
         warning.init_I(),
         opacity * (warning.init_Z() ? 0.7 : 1.0),
         0
      );
   }

   static void init(Pose pose, VertexConsumer lines, List<FireballBlastPredictor.DestroyedBlock> blocks, Vec3 camera) {
      for (FireballBlastPredictor.DestroyedBlock block : blocks) {
         AABB box = new AABB(block.pos()).inflate(0.002).move(camera.scale(-1.0));
         int alpha = (int)Math.round(90.0 + 125.0 * block.init_D());
         WorldLineRenderer.init(pose, lines, box, 16729413 | alpha << 24, 1.5F);
      }
   }

   static void load(Pose pose, VertexConsumer fill, List<FireballBlastPredictor.DestroyedBlock> blocks, Vec3 camera) {
      for (FireballBlastPredictor.DestroyedBlock block : blocks) {
         AABB box = new AABB(block.pos()).inflate(0.001).move(camera.scale(-1.0));
         int alpha = (int)Math.round(14.0 + 35.0 * block.init_D());
         WorldLineRenderer.init(pose, fill, box, 16729413 | alpha << 24);
      }
   }

   static void init(
      Minecraft mc, PoseStack pose, AutismBufferSource buffers, AbstractClientPlayer owner, Vec3 teleport, Vec3 camera, float partial, int tint, double opacity, int pass
   ) {
      if (modelSet != mc.getEntityModels()) {
         modelSet = mc.getEntityModels();
         wideModel = new PlayerModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
         slimModel = new PlayerModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
      }

      RenderPipeline pipeline = pass == 1 ? load_RenderPipeline : (pass == 2 ? read : init_RenderPipeline);
      if (pipeline == null) {
         RenderPipeline base = RenderPipelines.ENTITY_TRANSLUCENT_CULL;
         String suffix = pass == 1 ? "_visible" : (pass == 2 ? "_occluded" : "");
         pipeline = Compat263.createRenderPipeline(
            Identifier.fromNamespaceAndPath("autismclient", "pipeline/pearl_ghost" + suffix),
            Compat263.getVertexShader(base),
            Identifier.fromNamespaceAndPath("autismclient", "core/pearl_ghost"),
            base.getShaderDefines(),
            base.getBindGroupLayouts().stream().filter(layout -> Compat263.getSamplers(layout).isEmpty()).toList(),
            base.getColorTargetStates().toArray(new ColorTargetState[0]),
            pass == 0 ? null : new DepthStencilState(pass == 1 ? CompareOp.GREATER_THAN_OR_EQUAL : CompareOp.LESS_THAN, false),
            base.getPolygonMode(),
            true,
            base.getVertexFormatBindings().toArray(new VertexFormat[0]),
            base.getPrimitiveTopology(),
            base.getSortKey()
         );
         if (pass == 1) {
            load_RenderPipeline = pipeline;
         } else if (pass == 2) {
            read = pipeline;
         } else {
            init_RenderPipeline = pipeline;
         }
      }

      PlayerSkin skin = owner.getSkin();
      PlayerModel model = skin.model().name().equalsIgnoreCase("slim") ? slimModel : wideModel;
      AVATAR_STATE.ageInTicks = owner.tickCount + partial;
      AVATAR_STATE.mainArm = owner.getMainArm();
      AVATAR_STATE.skin = skin;
      model.setupAnim(AVATAR_STATE);
      model.hat.visible = model.jacket.visible = model.leftPants.visible = model.rightPants.visible = false;
      model.leftSleeve.visible = model.rightSleeve.visible = false;
      Vec3 at = teleport.subtract(camera);
      int alpha = (int)Math.round(255.0 * opacity);
      pose.pushPose();

      try {
         pose.translate(at.x, at.y, at.z);
         pose.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partial, owner.yRotO, owner.getYRot()))));
         pose.scale(-1.0F, -1.0F, 1.0F);
         pose.translate(0.0, -1.501, 0.0);
         model.renderToBuffer(
            pose,
            buffers.init(autismclient.render.AutismRenderTypes.pearlGhost(pipeline)),
            15728880,
            OverlayTexture.NO_OVERLAY,
            tint & 16777215 | alpha << 24
         );
      } finally {
         pose.popPose();
      }
   }

   private static void drawCircle(PoseStack pose, VertexConsumer lines, Vec3 at, double radius, Vec3 camera, int color) {
      for (int i = 0; i < 48; i++) {
         double a = i * 3.141592653589793 / 24.0;
         double b = (i + 1) * 3.141592653589793 / 24.0;
         drawLine(
            pose, lines, at.add(Math.cos(a) * radius, 0.0, Math.sin(a) * radius), at.add(Math.cos(b) * radius, 0.0, Math.sin(b) * radius), camera, color, 2.0F
         );
      }
   }

   private static void drawLine(PoseStack stack, VertexConsumer buffer, Vec3 from, Vec3 to, Vec3 camera, int color, float width) {
      Vec3 delta = to.subtract(from);
      if (!(delta.lengthSqr() < 1.0E-10)) {
         NORMAL.set((float)delta.x, (float)delta.y, (float)delta.z).normalize();
         Pose pose = stack.last();
         buffer.addVertex(pose, (float)(from.x - camera.x), (float)(from.y - camera.y), (float)(from.z - camera.z))
            .setColor(color)
            .setNormal(pose, NORMAL)
            .setLineWidth(width);
         buffer.addVertex(pose, (float)(to.x - camera.x), (float)(to.y - camera.y), (float)(to.z - camera.z))
            .setColor(color)
            .setNormal(pose, NORMAL)
            .setLineWidth(width);
      }
   }
}
