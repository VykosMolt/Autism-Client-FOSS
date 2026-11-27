package autismclient.util;

import autismclient.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class SkeletonEspRenderer {
   private static final double init_D = 0.72;
   private static final double load = 1.35;
   private static final double read = 0.3;
   private static final double write = 0.11;
   private static final double update = 0.32;
   private static final double sync = 0.72;
   private static final double flush = 0.66;
   private static final double close = 0.3;
   private static final double open = 0.3;
   private static final double poll = 48.0;
   private static final AutismBufferSource.Inner_DataUtil init_c_DataUtil = new AutismBufferSource.Inner_DataUtil(786432);
   private static volatile List<SkeletonEspRenderer.Inner_DataUtil> init_List = List.of();

   private SkeletonEspRenderer() {
   }

   public static void register() {
      LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
         init_List = List.of();
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.level != null && mc.player != null && !autismclient.modules.aD.init_Z() && !mc.gui.hud.isHidden()) {
            Module esp = autismclient.modules.al.init_Module_Str("esp");
            if (esp != null && esp.isEnabled() && Boolean.parseBoolean(esp.value("skeleton")) && !autismclient.modules.am.clear()) {
               boolean distanceColor = "Distance".equals(esp.value("skeleton-color-mode"));
               int staticColor = autismclient.modules.am.init_I_Module(esp, "skeleton-color", -1);
               float width = (float)Math.max(0.5, Math.min(6.0, init(esp.value("skeleton-width"), 2.0)));
               Vec3 camera = context.levelState().cameraRenderState.pos;
               float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
               List<SkeletonEspRenderer.Inner_DataUtil> bones = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof LivingEntity living && !living.isRemoved() && living != mc.player && autismclient.modules.am.load_Z(entity)) {
                     int color = distanceColor ? init(camera, living, partial) : staticColor;
                     init_V_List(bones, living, partial, camera, color, width);
                  }
               }

               if (!bones.isEmpty()) {
                  init_List = bones;
               }
            }
         }
      });
   }

   public static boolean hasSkeletons() {
      return !init_List.isEmpty();
   }

   public static void init(PoseStack matrices) {
      List<SkeletonEspRenderer.Inner_DataUtil> bones = init_List;
      if (!bones.isEmpty()) {
         init_List = List.of();
         AutismBufferSource bufferSource = init_c_DataUtil.init();
         VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.tracerEspLines());
         Pose pose = matrices.last();

         for (SkeletonEspRenderer.Inner_DataUtil bone : bones) {
            init_V_PoseStack_Pose(pose, buffer, bone.init_Vec3, bone.load, bone.init_I, bone.init_F);
         }

         bufferSource.init();
      }
   }

   private static void init_V_List(List<SkeletonEspRenderer.Inner_DataUtil> bones, LivingEntity e, float partial, Vec3 cam, int color, float width) {
      double px = Mth.lerp(partial, e.xOld, e.getX());
      double py = Mth.lerp(partial, e.yOld, e.getY());
      double pz = Mth.lerp(partial, e.zOld, e.getZ());
      float bodyYaw = e.yBodyRotO + Mth.wrapDegrees(e.yBodyRot - e.yBodyRotO) * partial;
      float headYaw = e.yHeadRotO + Mth.wrapDegrees(e.yHeadRot - e.yHeadRotO) * partial;
      float netHeadYaw = Mth.wrapDegrees(headYaw - bodyYaw);
      float headPitch = e.getViewXRot(partial);
      float limbPos = e.walkAnimation.position(partial);
      float limbSpeed = Math.min(1.0F, e.walkAnimation.speed(partial));
      boolean crouch = e.isCrouching();
      double s = Math.max(0.1, e.getBbHeight() / 1.8);
      double hipY = 0.72 * s;
      double neckY = 1.35 * s;
      double hipHalf = 0.11 * s;
      double shoulderHalf = 0.32 * s;
      double legLen = 0.72 * s;
      double armLen = 0.66 * s;
      double headLen = 0.3 * s;
      float t = limbPos * 0.6662F;
      float legR = Mth.cos(t) * 1.4F * limbSpeed;
      float legL = Mth.cos(t + 3.1415927F) * 1.4F * limbSpeed;
      double swingR = Math.cos(t) * limbSpeed;
      double swingL = -Math.cos(t) * limbSpeed;
      double age = e.tickCount + partial;
      double bobPitch = Math.sin(age * 0.067) * 0.05;
      double bobRoll = Math.cos(age * 0.09) * 0.03 + 0.03;
      double crouchArm = crouch ? 0.3 : 0.0;
      boolean rightDominant = e.getMainArm() == HumanoidArm.RIGHT;
      boolean rightHeld = !(rightDominant ? e.getMainHandItem() : e.getOffhandItem()).isEmpty();
      boolean leftHeld = !(rightDominant ? e.getOffhandItem() : e.getMainHandItem()).isEmpty();
      boolean using = e.isUsingItem();
      boolean rightUsing = using && e.getUsedItemHand() == InteractionHand.MAIN_HAND == rightDominant;
      boolean leftUsing = using && !rightUsing;
      double pitchR = init(swingR, rightHeld, rightUsing) - bobPitch + crouchArm;
      double pitchL = init(swingL, leftHeld, leftUsing) + bobPitch + crouchArm;
      double[] pelvis = new double[]{0.0, hipY, 0.0};
      double[] neck = new double[]{0.0, neckY, 0.0};
      double[] hipL = new double[]{-hipHalf, hipY, 0.0};
      double[] hipR = new double[]{hipHalf, hipY, 0.0};
      double[] shL = new double[]{-shoulderHalf, neckY, 0.0};
      double[] shR = new double[]{shoulderHalf, neckY, 0.0};
      double[] footL = new double[]{-hipHalf, hipY - legLen * Math.cos(legL), -legLen * Math.sin(legL)};
      double[] footR = new double[]{hipHalf, hipY - legLen * Math.cos(legR), -legLen * Math.sin(legR)};
      double[] handR = init_DArr_D(1.0, shoulderHalf, neckY, armLen, pitchR, bobRoll);
      double[] handL = init_DArr_D(-1.0, shoulderHalf, neckY, armLen, pitchL, bobRoll);
      if (crouch) {
         init(neck, hipY);
         init(shL, hipY);
         init(shR, hipY);
         init(handL, hipY);
         init(handR, hipY);
      }

      double[] head = init(neck, headLen, headPitch, netHeadYaw);
      double rad = Math.toRadians(bodyYaw);
      double sin = Math.sin(rad);
      double cos = Math.cos(rad);
      Vec3 wPelvis = init(px, py, pz, pelvis, sin, cos, cam);
      Vec3 wNeck = init(px, py, pz, neck, sin, cos, cam);
      Vec3 wHipL = init(px, py, pz, hipL, sin, cos, cam);
      Vec3 wHipR = init(px, py, pz, hipR, sin, cos, cam);
      Vec3 wShL = init(px, py, pz, shL, sin, cos, cam);
      Vec3 wShR = init(px, py, pz, shR, sin, cos, cam);
      Vec3 wFootL = init(px, py, pz, footL, sin, cos, cam);
      Vec3 wFootR = init(px, py, pz, footR, sin, cos, cam);
      Vec3 wHandL = init(px, py, pz, handL, sin, cos, cam);
      Vec3 wHandR = init(px, py, pz, handR, sin, cos, cam);
      Vec3 wHead = init(px, py, pz, head, sin, cos, cam);
      init(bones, wPelvis, wNeck, color, width);
      init(bones, wHipL, wHipR, color, width);
      init(bones, wShL, wShR, color, width);
      init(bones, wNeck, wHead, color, width);
      init(bones, wShL, wHandL, color, width);
      init(bones, wShR, wHandR, color, width);
      init(bones, wHipL, wFootL, color, width);
      init(bones, wHipR, wFootR, color, width);
   }

   private static double init(double swing, boolean holdingItem, boolean using) {
      if (using) {
         return swing * 0.5 + 0.9;
      } else {
         return holdingItem ? swing * 0.5 + 0.31415927 : swing;
      }
   }

   private static double[] init_DArr_D(double side, double shoulderHalf, double neckY, double armLen, double pitch, double roll) {
      double cp = Math.cos(pitch);
      double sp = Math.sin(pitch);
      double cr = Math.cos(roll);
      double sr = Math.sin(roll);
      return new double[]{side * (shoulderHalf + armLen * cp * sr), neckY - armLen * cp * cr, armLen * sp};
   }

   private static double[] init(double[] neck, double headLen, float headPitch, float netHeadYaw) {
      double hpr = Math.toRadians(headPitch);
      double upY = headLen * Math.cos(hpr);
      double upZ = headLen * Math.sin(hpr);
      double hyr = Math.toRadians(netHeadYaw);
      return new double[]{neck[0] + upZ * Math.sin(hyr), neck[1] + upY, neck[2] + upZ * Math.cos(hyr)};
   }

   private static void init(double[] p, double pivotY) {
      double ry = p[1] - pivotY;
      double rz = p[2];
      double c = Math.cos(0.3);
      double s = Math.sin(0.3);
      p[1] = pivotY + ry * c - rz * s;
      p[2] = ry * s + rz * c;
   }

   private static Vec3 init(double px, double py, double pz, double[] local, double sin, double cos, Vec3 cam) {
      double lx = local[0];
      double ly = local[1];
      double lz = local[2];
      double wx = px + lx * -cos + lz * -sin;
      double wz = pz + lx * -sin + lz * cos;
      double wy = py + ly;
      return new Vec3(wx - cam.x, wy - cam.y, wz - cam.z);
   }

   private static void init_V_PoseStack_Pose(Pose entry, VertexConsumer buffer, Vec3 a, Vec3 b, int color, float width) {
      Vector3f normal = new Vector3f((float)(b.x - a.x), (float)(b.y - a.y), (float)(b.z - a.z));
      if (!(normal.lengthSquared() <= 1.0E-8F)) {
         normal.normalize();
         float x1 = (float)a.x;
         float y1 = (float)a.y;
         float z1 = (float)a.z;
         float x2 = (float)b.x;
         float y2 = (float)b.y;
         float z2 = (float)b.z;
         buffer.addVertex(entry, x1, y1, z1).setColor(color).setNormal(entry, normal).setLineWidth(width);
         float distToCam = new Vector3f(x1, y1, z1).negate().dot(normal);
         float length = new Vector3f(x2, y2, z2).sub(x1, y1, z1).length();
         if (distToCam > 0.0F && distToCam < length) {
            Vector3f mid = new Vector3f(normal).mul(distToCam).add(x1, y1, z1);
            buffer.addVertex(entry, mid.x, mid.y, mid.z).setColor(color).setNormal(entry, normal).setLineWidth(width);
            buffer.addVertex(entry, mid.x, mid.y, mid.z).setColor(color).setNormal(entry, normal).setLineWidth(width);
         }

         buffer.addVertex(entry, x2, y2, z2).setColor(color).setNormal(entry, normal).setLineWidth(width);
      }
   }

   private static void init(List<SkeletonEspRenderer.Inner_DataUtil> bones, Vec3 a, Vec3 b, int color, float width) {
      bones.add(new SkeletonEspRenderer.Inner_DataUtil(a, b, color, width));
   }

   private static double init(String value, double fallback) {
      if (value == null) {
         return fallback;
      }

      try {
         return Double.parseDouble(value.trim());
      } catch (NumberFormatException e) {
         return fallback;
      }
   }

   private static int init(Vec3 cam, LivingEntity e, float partial) {
      double dx = Mth.lerp(partial, e.xOld, e.getX()) - cam.x;
      double dy = Mth.lerp(partial, e.yOld, e.getY()) - cam.y;
      double dz = Mth.lerp(partial, e.zOld, e.getZ()) - cam.z;
      return autismclient.modules.am.init_I_D_D(Math.sqrt(dx * dx + dy * dy + dz * dz), 48.0);
   }

   private record Inner_DataUtil(Vec3 init_Vec3, Vec3 load, int init_I, float init_F) {
   }
}
