package autismclient.util;

import autismclient.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class KillAuraHitMarkerRenderer {
   private static final long init_J = 500L;
   private static final int init_I = 16726843;
   private static final int load_I = 1509899067;
   private static volatile AABB init_AABB;
   private static volatile long load_J;
   private static Module init_Module;

   private KillAuraHitMarkerRenderer() {
   }

   public static void init_V() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (!autismclient.modules.aD.init_Z()) {
                  Module module = init_Module();
                  if (module != null && module.isEnabled() && Boolean.parseBoolean(module.value("hit-marker"))) {
                     Minecraft mc = Minecraft.getInstance();
                     if (mc != null && mc.level != null && mc.player != null && !mc.gui.hud.isHidden()) {
                        Vec3 camera = context.levelState().cameraRenderState.pos;
                        PoseStack poseStack = context.poseStack();
                        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                        LivingEntity target = init(module);
                        long now = System.currentTimeMillis();
                        float wave = 0.5F + 0.5F * (float)Math.sin(now * 0.001 * 3.141592653589793 * 2.0 * 1.2);
                        AABB targetBox = null;
                        if (target != null && target.isAlive()) {
                           Vec3 interpolated = target.getPosition(partialTick);
                           targetBox = target.getBoundingBox()
                              .inflate(0.06)
                              .move(interpolated.subtract(target.position()))
                              .move(-camera.x, -camera.y, -camera.z);
                        }

                        if (targetBox != null) {
                           int targetFillColor = 16 + (int)(16.0F * wave) << 24 | 16726843;
                           AABB fillBox = targetBox;
                           context.submitNodeCollector()
                              .submitCustomGeometry(
                                 poseStack,
                                 autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                                 (pose, buffer) -> load(pose, buffer, fillBox, targetFillColor)
                              );
                           int bracketColor = 208 + (int)(47.0F * wave) << 24 | 16726843;
                           AABB bracketBox = targetBox;
                           context.submitNodeCollector()
                              .submitCustomGeometry(
                                 poseStack,
                                 autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(),
                                 (pose, buffer) -> init(pose, buffer, bracketBox, bracketColor)
                              );
                        }

                        AABB marker = init_AABB;
                        if (marker != null && now - load_J < 500L) {
                           context.submitNodeCollector()
                              .submitCustomGeometry(
                                 poseStack,
                                 autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                                 (pose, buffer) -> load(pose, buffer, marker.move(-camera.x, -camera.y, -camera.z), 1509899067)
                              );
                        }
                     }
                  }
               }
            }
         );
   }

   public static void init(AABB box) {
      if (box != null) {
         init_AABB = box;
         load_J = System.currentTimeMillis();
      }
   }

   public static void load() {
      init_AABB = null;
   }

   private static Module init_Module() {
      Module module = init_Module;
      if (module == null) {
         module = init_Module = autismclient.modules.al.init_Module_Str("kill-aura");
      }

      return module;
   }

   private static LivingEntity init(Module module) {
      return module instanceof autismclient.modules.KillAuraModule aura ? aura.init_LivingEntity_3() : null;
   }

   private static void init(Pose pose, VertexConsumer buffer, AABB box, int color) {
      double bracket = Math.min(0.35, Math.min(box.getXsize(), Math.min(box.getYsize(), box.getZsize())) * 0.3);
      if (!(bracket <= 0.0)) {
         for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? box.minX : box.maxX;
            double y = (corner & 2) == 0 ? box.minY : box.maxY;
            double z = (corner & 4) == 0 ? box.minZ : box.maxZ;
            double dx = (corner & 1) == 0 ? bracket : -bracket;
            double dy = (corner & 2) == 0 ? bracket : -bracket;
            double dz = (corner & 4) == 0 ? bracket : -bracket;
            cI.init(pose, buffer, x, y, z, x + dx, y, z, color, 2.0F);
            cI.init(pose, buffer, x, y, z, x, y + dy, z, color, 2.0F);
            cI.init(pose, buffer, x, y, z, x, y, z + dz, color, 2.0F);
         }
      }
   }

   private static void load(Pose pose, VertexConsumer buffer, AABB box, int color) {
      init(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
      init(pose, buffer, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);
      init(pose, buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
      init(pose, buffer, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
      init(pose, buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);
      init(pose, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   private static void init(
      Pose pose,
      VertexConsumer buffer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int color
   ) {
      buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color);
      buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color);
      buffer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(color);
      buffer.addVertex(pose, (float)x4, (float)y4, (float)z4).setColor(color);
   }
}
