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
   private static final AutismBufferSource.Inner_DataUtil init_c_DataUtil = new AutismBufferSource.Inner_DataUtil(786432);
   private static final float[][] init_FArr2 = new float[][]{
      {-1.0F, -1.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F},
      {1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F, 1.0F, -1.0F, -1.0F},
      {1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, -1.0F, -1.0F},
      {-1.0F, -1.0F, -1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, 1.0F},
      {-1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, -1.0F},
      {-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, 1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F}
   };
   private static final float[][] load_FArr2 = new float[][]{
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F},
      {0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F},
      {0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F}
   };
   private static final Identifier[] init_IdentifierArr = new Identifier[]{
      init("panorama_0"), init("panorama_1"), init("panorama_2"), init("panorama_3"), init("panorama_5"), init("panorama_4")
   };
   private static final float init_F = 64.0F;
   private static final float load_F = 32.0F;
   private static final float read = 0.286F;
   private static float write;
   private static long init_J;

   private CustomSkyboxRenderer() {
   }

   public static boolean isActive() {
      if (aD.init_Z()) {
         return false;
      }

      WorldModule w = init_bi();
      return w != null && w.isEnabled() && w.bool("skybox") && Minecraft.getInstance().level != null;
   }

   public static void init(float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      WorldModule w = init_bi();
      if (mc.level != null && w != null) {
         boolean recolor = w.bool("skybox-recolor");
         int customColor = am.init_I_Module(w, "skybox-color", -50373);
         float speed = (float)Math.max(0.5, Math.min(3.0, w.decimal("skybox-speed")));
         float spin = init(w.bool("skybox-spin"), speed);
         Matrix4f rotation = new Matrix4f().rotationY(-spin * 0.017453292F).rotateX(3.700098F);
         Vector3f corner = new Vector3f();

         try {
            AutismBufferSource bufferSource = init_c_DataUtil.init();

            for (int face = 0; face < init_FArr2.length; face++) {
               Identifier texture = recolor
                  ? ThemeTextureRecolorer.init_Identifier(init_IdentifierArr[face], ThemePaletteManager.getPalette().targetArgb(ThemePaletteManager.ColorRole.BACKDROP))
                  : ThemeTextureRecolorer.init_Identifier(init_IdentifierArr[face], customColor);
               if (texture != null) {
                  VertexConsumer buffer = bufferSource.init(autismclient.render.AutismRenderTypes.waypointDiscSeeThrough(texture));
                  float[] verts = init_FArr2[face];
                  float[] uvs = load_FArr2[face];

                  for (int i = 0; i < 4; i++) {
                     rotation.transformPosition(verts[i * 3], verts[i * 3 + 1], verts[i * 3 + 2], corner);
                     buffer.addVertex(corner.x * 64.0F, corner.y * 64.0F, corner.z * 64.0F).setUv(uvs[i * 2], uvs[i * 2 + 1]).setColor(-1);
                  }
               }
            }

            bufferSource.init();
         } catch (Throwable var16) {
         }
      }
   }

   private static float init(boolean spinning, float speed) {
      long now = System.nanoTime();
      if (init_J == 0L) {
         init_J = now;
      }

      float dt = (float)(now - init_J) / 1.0E9F;
      init_J = now;
      if (spinning && dt > 0.0F) {
         write = (write + Math.min(dt, 0.25F) * 0.286F * speed) % 360.0F;
      }

      return spinning ? write : 0.0F;
   }

   private static Identifier init(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/" + name + ".png");
   }

   private static WorldModule init_bi() {
      return al.init_Module_Str("world") instanceof WorldModule w ? w : null;
   }
}
