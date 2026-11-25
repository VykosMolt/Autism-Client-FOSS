package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.util.bK;
import autismclient.util.cI;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class P extends Module implements autismclient.util.aj.Inner_ConfigStore {
   private static final String init_Str = "Box";
   private static final String load_Str = "GlowingPlane";
   private static final double init_D = 0.19607843137254902;
   private static final double load_D = 0.39215686274509803;
   private static final float init_F = 1.5F;
   private static final double read_D = 0.002;
   private static final double write_D = 0.002;
   private static volatile boolean init_Z;
   private static P init_P;
   private int init_I = -1;
   private boolean load_Z = true;
   private boolean read_Z = true;
   private boolean write_Z = true;
   private int load_I = 32;
   private int read_I = 8;
   private double update_D = 0.3;
   private double sync_D = 0.7;
   private int write_I = 512;
   private int update_I = -15089316;
   private int sync_I = -575461;
   private int flush = -13255988;
   private int close = -536805;
   private boolean update_Z;

   public P() {
      super("hole-esp", "HoleESP", ae.update, "Highlights safe holes.");
      this.add(new ChoiceSetting("mode", "Mode", "GlowingPlane", "Box", "GlowingPlane").description("Full box or glowing floor.").build());
      this.add(new IntSetting("horizontal-distance", "Horizontal Distance", 32, 4, 128, 1).description("Scan radius on X/Z.").build());
      this.add(new IntSetting("vertical-distance", "Vertical Distance", 8, 4, 64, 1).description("Scan radius on Y.").build());
      this.add(new DoubleSetting("distance-fade", "Distance Fade", 0.3, 0.0, 1.0, 0.05).description("Fraction of radius that fades.").build());
      this.add(new BoolSetting("fill", "Fill", true).description("Draw the translucent face.").build());
      this.add(new BoolSetting("outline", "Outline", true).description("Draw the edges.").build());
      this.add(
         new DoubleSetting("glow-height", "Glow Height", 0.7, 0.0, 1.0, 0.05)
            .description("Height of the fading walls.")
            .visibleWhen(() -> !"Box".equals(this.choice("mode")))
            .build()
      );
      this.add(new IntSetting("max-holes", "Max Holes", 512, 16, 4096, 16).description("Most holes drawn at once.").build());
      this.add(new ColorSetting("color-1x1-bedrock", "1x1 Bedrock", -15089316).group("Colors").description("A 1x1 walled in bedrock.").build());
      this.add(new ColorSetting("color-1x1", "1x1", -575461).group("Colors").description("A one-block hole.").build());
      this.add(new ColorSetting("color-1x2", "1x2", -13255988).group("Colors").description("A two-block hole.").build());
      this.add(new ColorSetting("color-2x2", "2x2", -536805).group("Colors").description("A four-block hole.").build());
   }

   public static void init_V_12() {
      update();
   }

   @Override
   public void onEnable() {
      this.init_I = -1;
      update();
      this.load_V_9();
   }

   @Override
   public void onDisable() {
      this.init_I = -1;
      this.read();
   }

   @Override
   public void onGameLeft() {
      this.init_I = -1;
      this.read();
   }

   @Override
   public void tick() {
      this.load_V_9();
      this.write();
      autismclient.util.aj.load();
   }

   @Override
   public String info() {
      return this.choice("mode");
   }

   @Override
   public int init_I_32() {
      this.write();
      return this.load_I;
   }

   @Override
   public int load_I_17() {
      this.write();
      return this.read_I;
   }

   private void load_V_9() {
      if (!this.update_Z) {
         autismclient.util.aj.init(this);
         this.update_Z = true;
      }
   }

   private void read() {
      if (this.update_Z) {
         this.update_Z = false;
         autismclient.util.aj.load(this);
      }
   }

   private void write() {
      int revision = al.init_I();
      if (revision != this.init_I) {
         this.init_I = revision;
         this.load_Z = !"Box".equals(this.choice("mode"));
         this.read_Z = this.bool("fill");
         this.write_Z = this.bool("outline");
         this.load_I = this.integer("horizontal-distance");
         this.read_I = this.integer("vertical-distance");
         this.update_D = this.decimal("distance-fade");
         this.sync_D = this.decimal("glow-height");
         this.write_I = this.integer("max-holes");
         this.update_I = am.init_I_Module(this, "color-1x1-bedrock", -15089316);
         this.sync_I = am.init_I_Module(this, "color-1x1", -575461);
         this.flush = am.init_I_Module(this, "color-1x2", -13255988);
         this.close = am.init_I_Module(this, "color-2x2", -536805);
      }
   }

   private static P init_P() {
      P cached = init_P;
      if (cached != null) {
         return cached;
      } else {
         return al.init_Module_Str("hole-esp") instanceof P hole ? (init_P = hole) : null;
      }
   }

   private static synchronized void update() {
      if (!init_Z) {
         init_Z = true;
         LevelRenderEvents.COLLECT_SUBMITS
            .register(
               context -> {
                  try {
                     P module = init_P();
                     if (module == null || !module.isEnabled()) {
                        return;
                     }

                     if (aD.init_Z()) {
                        return;
                     }

                     if (MC == null || MC.level == null || MC.player == null || MC.gui.hud.isHidden()) {
                        return;
                     }

                     if (am.clear()) {
                        return;
                     }

                     module.write();
                     if (!module.read_Z && !module.write_Z) {
                        return;
                     }

                     List<autismclient.util.aj.Inner_DataUtil> holes = autismclient.util.aj.init_List();
                     if (holes.isEmpty()) {
                        return;
                     }

                     Vec3 camera = context.levelState().cameraRenderState.pos;
                     Vec3 player = MC.player.position();
                     if (module.read_Z) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              context.poseStack(),
                              autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                              (pose, buffer) -> module.init(pose, buffer, holes, camera, player)
                           );
                     }

                     if (module.write_Z) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              context.poseStack(),
                              autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(),
                              (pose, buffer) -> module.load_V_PoseStack_Pose(pose, buffer, holes, camera, player)
                           );
                     }
                  } catch (Throwable var5) {
                  }
               }
            );
      }
   }

   private void init(Pose pose, VertexConsumer buffer, List<autismclient.util.aj.Inner_DataUtil> holes, Vec3 camera, Vec3 player) {
      long perf = bK.load_J();
      int drawn = 0;

      for (autismclient.util.aj.Inner_DataUtil hole : holes) {
         if (drawn >= this.write_I) {
            break;
         }

         double fade = this.init_D_BlockPos_Vec3(hole.init_BlockPos(), player);
         if (!(fade <= 0.0)) {
            drawn++;
            int color = init_I_I_D(this.init(hole.init_aj_StreamUtil()), 0.19607843137254902 * fade);
            if (color != 0) {
               AABB box = hole.init_AABB().move(-camera.x, -camera.y, -camera.z);
               if (this.load_Z) {
                  double floorY = box.minY + 0.002;
                  init(pose, buffer, box.minX, box.maxX, box.minZ, box.maxZ, floorY, color);
                  if (this.sync_D > 0.0) {
                     this.init(pose, buffer, box, floorY, color);
                  }
               } else {
                  load(pose, buffer, box.inflate(0.002), color);
               }
            }
         }
      }

      bK.init("frame.holeEspFill", perf);
   }

   private void load_V_PoseStack_Pose(Pose pose, VertexConsumer buffer, List<autismclient.util.aj.Inner_DataUtil> holes, Vec3 camera, Vec3 player) {
      long perf = bK.load_J();
      int drawn = 0;

      for (autismclient.util.aj.Inner_DataUtil hole : holes) {
         if (drawn >= this.write_I) {
            break;
         }

         double fade = this.init_D_BlockPos_Vec3(hole.init_BlockPos(), player);
         if (!(fade <= 0.0)) {
            drawn++;
            int color = init_I_I_D(this.init(hole.init_aj_StreamUtil()), 0.39215686274509803 * fade);
            if (color != 0) {
               AABB box = hole.init_AABB().move(-camera.x, -camera.y, -camera.z);
               if (this.load_Z) {
                  load_V_PoseStack_Pose_2(pose, buffer, box, box.minY + 0.002, color);
               } else {
                  init(pose, buffer, box.inflate(0.002), color);
               }
            }
         }
      }

      bK.init("frame.holeEspOutline", perf);
   }

   private int init(autismclient.util.aj.Inner_StreamUtil type) {
      int var10000;
      switch (type) {
         case init:
            var10000 = this.update_I;
            break;
         case load:
            var10000 = this.sync_I;
            break;
         case read:
            var10000 = this.flush;
            break;
         case write:
            var10000 = this.close;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private double init_D_BlockPos_Vec3(BlockPos pos, Vec3 player) {
      double dx = player.x - pos.getX();
      double dy = player.y - pos.getY();
      double dz = player.z - pos.getZ();
      if (Math.abs(dy) > this.read_I || Math.abs(dx) > this.load_I || Math.abs(dz) > this.load_I) {
         return 0.0;
      }

      if (this.update_D <= 0.0) {
         return 1.0;
      }

      double verticalFraction = Math.abs(dy) / this.read_I;
      double horizontalFraction = Math.sqrt(dx * dx + dz * dz) / this.load_I;
      double fade = (1.0 - Math.max(verticalFraction, horizontalFraction)) / this.update_D;
      return fade <= 0.0 ? 0.0 : Math.min(1.0, fade);
   }

   private static int init_I_I_D(int argb, double multiplier) {
      int alpha = (int)Math.round((argb >>> 24 & 0xFF) * multiplier);
      return alpha <= 0 ? 0 : Math.min(255, alpha) << 24 | argb & 16777215;
   }

   private static void init(Pose pose, VertexConsumer buffer, double minX, double maxX, double minZ, double maxZ, double y, int color) {
      init(pose, buffer, minX, y, minZ, maxX, y, minZ, maxX, y, maxZ, minX, y, maxZ, color);
      init(pose, buffer, minX, y, maxZ, maxX, y, maxZ, maxX, y, minZ, minX, y, minZ, color);
   }

   private void init(Pose pose, VertexConsumer buffer, AABB box, double baseY, int color) {
      int top = color & 16777215;
      double topY = baseY + this.sync_D;
      init(pose, buffer, box.minX, box.minZ, box.maxX, box.minZ, baseY, topY, color, top);
      init(pose, buffer, box.maxX, box.minZ, box.maxX, box.maxZ, baseY, topY, color, top);
      init(pose, buffer, box.maxX, box.maxZ, box.minX, box.maxZ, baseY, topY, color, top);
      init(pose, buffer, box.minX, box.maxZ, box.minX, box.minZ, baseY, topY, color, top);
   }

   private static void init(Pose pose, VertexConsumer buffer, double x1, double z1, double x2, double z2, double bottomY, double topY, int bottom, int top) {
      init(pose, buffer, x1, bottomY, z1, bottom, x2, bottomY, z2, bottom, x2, topY, z2, top, x1, topY, z1, top);
      init(pose, buffer, x1, topY, z1, top, x2, topY, z2, top, x2, bottomY, z2, bottom, x1, bottomY, z1, bottom);
   }

   private static void load_V_PoseStack_Pose_2(Pose pose, VertexConsumer buffer, AABB box, double y, int color) {
      cI.init(pose, buffer, box.minX, y, box.minZ, box.maxX, y, box.minZ, color, 1.5F);
      cI.init(pose, buffer, box.maxX, y, box.minZ, box.maxX, y, box.maxZ, color, 1.5F);
      cI.init(pose, buffer, box.maxX, y, box.maxZ, box.minX, y, box.maxZ, color, 1.5F);
      cI.init(pose, buffer, box.minX, y, box.maxZ, box.minX, y, box.minZ, color, 1.5F);
   }

   private static void init(Pose pose, VertexConsumer buffer, AABB box, int color) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      cI.init(pose, buffer, x1, y1, z1, x2, y1, z1, color, 1.5F);
      cI.init(pose, buffer, x2, y1, z1, x2, y1, z2, color, 1.5F);
      cI.init(pose, buffer, x2, y1, z2, x1, y1, z2, color, 1.5F);
      cI.init(pose, buffer, x1, y1, z2, x1, y1, z1, color, 1.5F);
      cI.init(pose, buffer, x1, y2, z1, x2, y2, z1, color, 1.5F);
      cI.init(pose, buffer, x2, y2, z1, x2, y2, z2, color, 1.5F);
      cI.init(pose, buffer, x2, y2, z2, x1, y2, z2, color, 1.5F);
      cI.init(pose, buffer, x1, y2, z2, x1, y2, z1, color, 1.5F);
      cI.init(pose, buffer, x1, y1, z1, x1, y2, z1, color, 1.5F);
      cI.init(pose, buffer, x2, y1, z1, x2, y2, z1, color, 1.5F);
      cI.init(pose, buffer, x2, y1, z2, x2, y2, z2, color, 1.5F);
      cI.init(pose, buffer, x1, y1, z2, x1, y2, z2, color, 1.5F);
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
      init(pose, buffer, x1, y1, z1, color, x2, y2, z2, color, x3, y3, z3, color, x4, y4, z4, color);
   }

   private static void init(
      Pose pose,
      VertexConsumer buffer,
      double x1,
      double y1,
      double z1,
      int c1,
      double x2,
      double y2,
      double z2,
      int c2,
      double x3,
      double y3,
      double z3,
      int c3,
      double x4,
      double y4,
      double z4,
      int c4
   ) {
      buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(c1);
      buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(c2);
      buffer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(c3);
      buffer.addVertex(pose, (float)x4, (float)y4, (float)z4).setColor(c4);
   }
}
