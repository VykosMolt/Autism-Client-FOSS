package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.util.bK;
import autismclient.util.WorldLineRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HoleEspModule extends Module implements autismclient.util.PvpHoleScanner.ScanSubscriber {
   private static final String init_Str = "Box";
   private static final String load_Str = "GlowingPlane";
   private static final double init_D = 0.19607843137254902;
   private static final double load_D = 0.39215686274509803;
   private static final float init_F = 1.5F;
   private static final double read_D = 0.002;
   private static final double write_D = 0.002;
   private static volatile boolean init_Z;
   private static HoleEspModule cachedInstance;
   private int settingsRevision = -1;
   private boolean load_Z = true;
   private boolean fill = true;
   private boolean outline = true;
   private int horizontalDistance = 32;
   private int verticalDistance = 8;
   private double distanceFade = 0.3;
   private double glowHeight = 0.7;
   private int maxHoles = 512;
   private int bedrockColor = -15089316;
   private int defaultColor = -575461;
   private int oneByTwoColor = -13255988;
   private int twoByTwoColor = -536805;
   private boolean scannerRegistered;

   public HoleEspModule() {
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

   public static void register() {
      ensureRenderHook();
   }

   @Override
   public void onEnable() {
      this.settingsRevision = -1;
      ensureRenderHook();
      this.registerScanner();
   }

   @Override
   public void onDisable() {
      this.settingsRevision = -1;
      this.unregisterScanner();
   }

   @Override
   public void onGameLeft() {
      this.settingsRevision = -1;
      this.unregisterScanner();
   }

   @Override
   public void tick() {
      this.registerScanner();
      this.refreshSettings();
      autismclient.util.PvpHoleScanner.load();
   }

   @Override
   public String info() {
      return this.choice("mode");
   }

   @Override
   public int init_I_32() {
      this.refreshSettings();
      return this.horizontalDistance;
   }

   @Override
   public int load_I_17() {
      this.refreshSettings();
      return this.verticalDistance;
   }

   private void registerScanner() {
      if (!this.scannerRegistered) {
         autismclient.util.PvpHoleScanner.init(this);
         this.scannerRegistered = true;
      }
   }

   private void unregisterScanner() {
      if (this.scannerRegistered) {
         this.scannerRegistered = false;
         autismclient.util.PvpHoleScanner.load(this);
      }
   }

   private void refreshSettings() {
      int revision = al.init_I();
      if (revision != this.settingsRevision) {
         this.settingsRevision = revision;
         this.load_Z = !"Box".equals(this.choice("mode"));
         this.fill = this.bool("fill");
         this.outline = this.bool("outline");
         this.horizontalDistance = this.integer("horizontal-distance");
         this.verticalDistance = this.integer("vertical-distance");
         this.distanceFade = this.decimal("distance-fade");
         this.glowHeight = this.decimal("glow-height");
         this.maxHoles = this.integer("max-holes");
         this.bedrockColor = am.init_I_Module(this, "color-1x1-bedrock", -15089316);
         this.defaultColor = am.init_I_Module(this, "color-1x1", -575461);
         this.oneByTwoColor = am.init_I_Module(this, "color-1x2", -13255988);
         this.twoByTwoColor = am.init_I_Module(this, "color-2x2", -536805);
      }
   }

   private static HoleEspModule instance() {
      HoleEspModule cached = cachedInstance;
      if (cached != null) {
         return cached;
      } else {
         return al.init_Module_Str("hole-esp") instanceof HoleEspModule hole ? (cachedInstance = hole) : null;
      }
   }

   private static synchronized void ensureRenderHook() {
      if (!init_Z) {
         init_Z = true;
         LevelRenderEvents.COLLECT_SUBMITS
            .register(
               context -> {
                  try {
                     HoleEspModule module = instance();
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

                     module.refreshSettings();
                     if (!module.fill && !module.outline) {
                        return;
                     }

                     List<autismclient.util.PvpHoleScanner.Hole> holes = autismclient.util.PvpHoleScanner.holes();
                     if (holes.isEmpty()) {
                        return;
                     }

                     Vec3 camera = context.levelState().cameraRenderState.pos;
                     Vec3 player = MC.player.position();
                     if (module.fill) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              context.poseStack(),
                              autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                              (pose, buffer) -> module.drawHoles(pose, buffer, holes, camera, player)
                           );
                     }

                     if (module.outline) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              context.poseStack(),
                              autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(),
                              (pose, buffer) -> module.renderHoles(pose, buffer, holes, camera, player)
                           );
                     }
                  } catch (Throwable var5) {
                  }
               }
            );
      }
   }

   private void drawHoles(Pose pose, VertexConsumer buffer, List<autismclient.util.PvpHoleScanner.Hole> holes, Vec3 camera, Vec3 player) {
      long perf = bK.load_J();
      int drawn = 0;

      for (autismclient.util.PvpHoleScanner.Hole hole : holes) {
         if (drawn >= this.maxHoles) {
            break;
         }

         double fade = this.fadeFactor(hole.pos(), player);
         if (!(fade <= 0.0)) {
            drawn++;
            int color = scaleAlpha(this.colorFor(hole.type()), 0.19607843137254902 * fade);
            if (color != 0) {
               AABB box = hole.box().move(-camera.x, -camera.y, -camera.z);
               if (this.load_Z) {
                  double floorY = box.minY + 0.002;
                  drawFlatQuad(pose, buffer, box.minX, box.maxX, box.minZ, box.maxZ, floorY, color);
                  if (this.glowHeight > 0.0) {
                     this.drawGlowWalls(pose, buffer, box, floorY, color);
                  }
               } else {
                  drawBoxFaces(pose, buffer, box.inflate(0.002), color);
               }
            }
         }
      }

      bK.init("frame.holeEspFill", perf);
   }

   private void renderHoles(Pose pose, VertexConsumer buffer, List<autismclient.util.PvpHoleScanner.Hole> holes, Vec3 camera, Vec3 player) {
      long perf = bK.load_J();
      int drawn = 0;

      for (autismclient.util.PvpHoleScanner.Hole hole : holes) {
         if (drawn >= this.maxHoles) {
            break;
         }

         double fade = this.fadeFactor(hole.pos(), player);
         if (!(fade <= 0.0)) {
            drawn++;
            int color = scaleAlpha(this.colorFor(hole.type()), 0.39215686274509803 * fade);
            if (color != 0) {
               AABB box = hole.box().move(-camera.x, -camera.y, -camera.z);
               if (this.load_Z) {
                  drawOutlineAt(pose, buffer, box, box.minY + 0.002, color);
               } else {
                  drawBoxEdges(pose, buffer, box.inflate(0.002), color);
               }
            }
         }
      }

      bK.init("frame.holeEspOutline", perf);
   }

   private int colorFor(autismclient.util.PvpHoleScanner.HoleType type) {
      int var10000;
      switch (type) {
         case ONE_BY_ONE_BEDROCK:
            var10000 = this.bedrockColor;
            break;
         case ONE_BY_ONE:
            var10000 = this.defaultColor;
            break;
         case ONE_BY_TWO:
            var10000 = this.oneByTwoColor;
            break;
         case TWO_BY_TWO:
            var10000 = this.twoByTwoColor;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private double fadeFactor(BlockPos pos, Vec3 player) {
      double dx = player.x - pos.getX();
      double dy = player.y - pos.getY();
      double dz = player.z - pos.getZ();
      if (Math.abs(dy) > this.verticalDistance || Math.abs(dx) > this.horizontalDistance || Math.abs(dz) > this.horizontalDistance) {
         return 0.0;
      }

      if (this.distanceFade <= 0.0) {
         return 1.0;
      }

      double verticalFraction = Math.abs(dy) / this.verticalDistance;
      double horizontalFraction = Math.sqrt(dx * dx + dz * dz) / this.horizontalDistance;
      double fade = (1.0 - Math.max(verticalFraction, horizontalFraction)) / this.distanceFade;
      return fade <= 0.0 ? 0.0 : Math.min(1.0, fade);
   }

   private static int scaleAlpha(int argb, double multiplier) {
      int alpha = (int)Math.round((argb >>> 24 & 0xFF) * multiplier);
      return alpha <= 0 ? 0 : Math.min(255, alpha) << 24 | argb & 16777215;
   }

   private static void drawFlatQuad(Pose pose, VertexConsumer buffer, double minX, double maxX, double minZ, double maxZ, double y, int color) {
      drawQuad(pose, buffer, minX, y, minZ, maxX, y, minZ, maxX, y, maxZ, minX, y, maxZ, color);
      drawQuad(pose, buffer, minX, y, maxZ, maxX, y, maxZ, maxX, y, minZ, minX, y, minZ, color);
   }

   private void drawGlowWalls(Pose pose, VertexConsumer buffer, AABB box, double baseY, int color) {
      int top = color & 16777215;
      double topY = baseY + this.glowHeight;
      drawGradientWall(pose, buffer, box.minX, box.minZ, box.maxX, box.minZ, baseY, topY, color, top);
      drawGradientWall(pose, buffer, box.maxX, box.minZ, box.maxX, box.maxZ, baseY, topY, color, top);
      drawGradientWall(pose, buffer, box.maxX, box.maxZ, box.minX, box.maxZ, baseY, topY, color, top);
      drawGradientWall(pose, buffer, box.minX, box.maxZ, box.minX, box.minZ, baseY, topY, color, top);
   }

   private static void drawGradientWall(Pose pose, VertexConsumer buffer, double x1, double z1, double x2, double z2, double bottomY, double topY, int bottom, int top) {
      drawGradientQuad(pose, buffer, x1, bottomY, z1, bottom, x2, bottomY, z2, bottom, x2, topY, z2, top, x1, topY, z1, top);
      drawGradientQuad(pose, buffer, x1, topY, z1, top, x2, topY, z2, top, x2, bottomY, z2, bottom, x1, bottomY, z1, bottom);
   }

   private static void drawOutlineAt(Pose pose, VertexConsumer buffer, AABB box, double y, int color) {
      WorldLineRenderer.init(pose, buffer, box.minX, y, box.minZ, box.maxX, y, box.minZ, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, box.maxX, y, box.minZ, box.maxX, y, box.maxZ, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, box.maxX, y, box.maxZ, box.minX, y, box.maxZ, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, box.minX, y, box.maxZ, box.minX, y, box.minZ, color, 1.5F);
   }

   private static void drawBoxEdges(Pose pose, VertexConsumer buffer, AABB box, int color) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      WorldLineRenderer.init(pose, buffer, x1, y1, z1, x2, y1, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y1, z1, x2, y1, z2, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y1, z2, x1, y1, z2, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x1, y1, z2, x1, y1, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x1, y2, z1, x2, y2, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y2, z1, x2, y2, z2, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y2, z2, x1, y2, z2, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x1, y2, z2, x1, y2, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x1, y1, z1, x1, y2, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y1, z1, x2, y2, z1, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x2, y1, z2, x2, y2, z2, color, 1.5F);
      WorldLineRenderer.init(pose, buffer, x1, y1, z2, x1, y2, z2, color, 1.5F);
   }

   private static void drawBoxFaces(Pose pose, VertexConsumer buffer, AABB box, int color) {
      drawQuad(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
      drawQuad(pose, buffer, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
      drawQuad(pose, buffer, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   private static void drawQuad(
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
      drawGradientQuad(pose, buffer, x1, y1, z1, color, x2, y2, z2, color, x3, y3, z3, color, x4, y4, z4, color);
   }

   private static void drawGradientQuad(
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
