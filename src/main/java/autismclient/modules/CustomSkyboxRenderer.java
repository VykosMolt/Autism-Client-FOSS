package autismclient.modules;

import autismclient.util.AutismBufferSource;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CustomSkyboxRenderer {
   private static final AutismBufferSource.LazySource BUFFER = new AutismBufferSource.LazySource(786432);
   private static final float[][] FACE_VERTS = new float[][]{
      {-1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F},
      {1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, -1.0F, -1.0F},
      {1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F},
      {-1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F},
      {-1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F},
      {-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F}
   };
   private static final float[][] FACE_UVS = new float[][]{
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F}
   };
   private static final Identifier[] FACE_TEXTURES = new Identifier[]{
      panorama("panorama_0"), panorama("panorama_1"), panorama("panorama_2"), panorama("panorama_3"), panorama("panorama_5"), panorama("panorama_4")
   };
   private static final float init_F = 64.0F;
   private static final float load_F = 32.0F;
   private static final float SPIN_RATE = 0.286F;
   private static float spinAngle;
   private static long lastFrameNanos;

   private CustomSkyboxRenderer() {
   }

   public static boolean isActive() {
      if (aD.init_Z()) {
         return false;
      }

      WorldModule w = worldModule();
      return w != null && w.isEnabled() && w.bool("skybox") && Minecraft.getInstance().level != null;
   }

   public static void render(float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      WorldModule w = worldModule();
      if (mc.level != null && w != null) {
         boolean recolor = w.bool("skybox-recolor");
         int customColor = am.init_I_Module(w, "skybox-color", -50373);
         float speed = (float)Math.max(0.5, Math.min(3.0, w.decimal("skybox-speed")));
         float spin = spin(w.bool("skybox-spin"), speed);
         Matrix4f rotation = new Matrix4f().rotationY(-spin * 0.017453292F).rotateX(3.700098F);
         Vector3f corner = new Vector3f();

         try {
            AutismBufferSource bufferSource = BUFFER.init();

            for (int face = 0; face < FACE_VERTS.length; face++) {
               Identifier texture = recolor
                  ? ThemeTextureRecolorer.recolor(FACE_TEXTURES[face], ThemePaletteManager.getPalette().targetArgb(ThemePaletteManager.ColorRole.BACKDROP))
                  : ThemeTextureRecolorer.recolor(FACE_TEXTURES[face], customColor);
               if (texture != null) {
                  VertexConsumer buffer = bufferSource.buffer(autismclient.render.AutismRenderTypes.waypointDiscSeeThrough(texture));
                  float[] verts = FACE_VERTS[face];
                  float[] uvs = FACE_UVS[face];

                  for (int i = 0; i < 4; i++) {
                     rotation.transformPosition(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2], corner);
                     buffer.addVertex(corner.x * 64.0F, corner.y * 64.0F, corner.z * 64.0F).setUv(uvs[i * 2], uvs[i * 2 + 1]).setColor(-1);
                  }
               }
            }

            bufferSource.drawAll();
         } catch (Throwable var16) {
         }
      }
   }

   private static float spin(boolean spinning, float speed) {
      long now = System.nanoTime();
      if (lastFrameNanos == 0L) {
         lastFrameNanos = now;
      }

      float dt = (float)(now - lastFrameNanos) / 1.0E9F;
      lastFrameNanos = now;
      if (spinning && dt > 0.0F) {
         spinAngle = (spinAngle + Math.min(dt, 0.25F) * 0.286F * speed) % 360.0F;
      }

      return spinning ? spinAngle : 0.0F;
   }

   private static Identifier panorama(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/" + name + ".png");
   }

   private static WorldModule worldModule() {
      return al.init_Module_Str("world") instanceof WorldModule w ? w : null;
   }
}
