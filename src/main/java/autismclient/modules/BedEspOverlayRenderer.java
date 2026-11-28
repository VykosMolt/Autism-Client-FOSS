package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismBufferSource;
import autismclient.util.cA;
import autismclient.util.WorldLineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class BedEspOverlayRenderer {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final AutismBufferSource.LazySource init_c_DataUtil = new AutismBufferSource.LazySource(65536);

   private BedEspOverlayRenderer() {
   }

   private static List<BedEspModule.BedInfo> init_List() {
      BedEspModule esp = BedEspModule.active();
      if (esp != null) {
         return esp.beds();
      }

      BedEspModule.BedInfo target = BedNukerModule.init_ClassCache_DataUti();
      return target == null ? List.of() : List.of(target);
   }

   public static boolean isActive() {
      return !AutismLiteVariant.enabled() && !autismclient.e.HashUtil.init()
         ? false
         : init_Minecraft.level != null
            && init_Minecraft.player != null
            && !init_List().isEmpty()
            && !aD.init_Z()
            && !init_Minecraft.gui.hud.isHidden()
            && !am.clear();
   }

   public static void init(PoseStack matrices) {
      if (isActive()) {
         Vec3 camera = WorldProjectionHelper.cameraPosition();
         AutismBufferSource source = init_c_DataUtil.init();
         List<BedEspModule.BedInfo> beds = init_List();
         boolean path = BedEspModule.policy().load();
         VertexConsumer fill = source.buffer(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());

         for (BedEspModule.BedInfo bed : beds) {
            int rgb = bed.color().getTextureDiffuseColor();
            WorldLineRenderer.init(matrices.last(), fill, bed.box().move(camera.scale(-1.0)), init(rgb, 32));
            if (path && bed.init_Z()) {
               for (int n = 0; n < bed.load_List().size(); n++) {
                  WorldLineRenderer.init(matrices.last(), fill, new AABB(bed.load_List().get(n)).inflate(0.005).move(camera.scale(-1.0)), n == 0 ? 540999579 : 419417696);
               }
            }
         }

         VertexConsumer lines = source.buffer(autismclient.render.AutismRenderTypes.tracerEspLines());

         for (BedEspModule.BedInfo bed : beds) {
            WorldLineRenderer.init(
               matrices.last(), lines, bed.box().inflate(0.004).move(camera.scale(-1.0)), init(bed.color().getTextureDiffuseColor(), 204), 1.5F
            );
            if (path && bed.init_Z()) {
               for (int n = 0; n < bed.load_List().size(); n++) {
                  WorldLineRenderer.init(
                     matrices.last(), lines, new AABB(bed.load_List().get(n)).inflate(0.007).move(camera.scale(-1.0)), n == 0 ? -868286565 : -855650720, 1.5F
                  );
               }
            }
         }

         source.drawAll();
      }
   }

   public static void init(GuiGraphicsExtractor graphics) {
      if (isActive() && BedEspModule.policy().read()) {
         Matrix4f matrix = new Matrix4f();
         if (WorldProjectionHelper.projection(matrix)) {
            Vec3 camera = WorldProjectionHelper.cameraPosition();
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, init_Minecraft.font, 0, 0);
            int rowHeight = Math.max(
               12,
               (int)Math.ceil(
                     autismclient.gui.sdf.MsdfTextRenderer.glyphBoxHeight(
                        autismclient.gui.sdf.MsdfFontFace.BODY,
                        autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
                     )
                  )
                  + 2
            );
            int iconSize = Math.min(12, rowHeight - 2);

            for (BedEspModule.BedInfo bed : init_List()) {
               double distance = Math.sqrt(bed.box().distanceToSqr(camera));
               float alpha = DistanceFadeCurve.fade(distance);
               if (!(alpha < 0.005F)) {
                  float[] point = init(matrix, camera, bed.labelPos());
                  if (point != null) {
                     String name = bed.color().getName().replace('_', ' ');
                     name = Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Bed";
                     String time = bed.init_Str().isEmpty() ? String.format(Locale.ROOT, "%.1fs", bed.ticks() / 20.0) : bed.init_Str();
                     int width = ui.init_PoolEntry().measureWidth(name) + 8 + ui.init_PoolEntry().measureWidth(time);
                     float[] widths = new float[1 + bed.materials().size()];
                     widths[0] = width + 4;

                     for (int i = 0; i < bed.materials().size(); i++) {
                        BedEspModule.CoverMaterial material = bed.materials().get(i);
                        widths[i + 1] = ui.init_PoolEntry().measureWidth(material.name())
                           + 3
                           + ui.init_PoolEntry().measureWidth("x" + material.count())
                           + (material.icon().isEmpty() ? 0 : 4 + iconSize)
                           + 4;
                     }

                     int height = rowHeight * widths.length;
                     graphics.pose().pushMatrix();

                     try {
                        graphics.pose().translate(point[0], point[1]);
                        float scale = NameTagsRenderer.scaleFor(0.72, true, distance);
                        graphics.pose().scale(scale, scale);
                        graphics.pose().translate(0.0F, -height - 2);
                        int text = autismclient.gui.f.PacketUtil.init(-1, alpha);
                        int count = autismclient.gui.f.PacketUtil.init(init(ui.init_TableIndex().colors().accent), alpha);
                        int background = autismclient.gui.f.PacketUtil.init(-1879048192, alpha);
                        autismclient.gui.f.PacketUtil.init(graphics, 0.0F, 0.0F, widths, rowHeight, rowHeight / 2.0F, background);
                        graphics.pose().pushMatrix();
                        try {
                           graphics.pose().translate(-width * 0.5F, 0.0F);
                           int y = ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(0, 0, width, rowHeight));
                           autismclient.gui.f.b.k.init(
                              graphics,
                              init_Minecraft.font,
                              name,
                              autismclient.gui.f.a.DataUtil.init,
                              autismclient.gui.f.PacketUtil.init(init(bed.color().getTextureDiffuseColor()), alpha),
                              0,
                              y,
                              true
                           );
                           autismclient.gui.f.b.k.init(
                              graphics,
                              init_Minecraft.font,
                              time,
                              autismclient.gui.f.a.DataUtil.init,
                              autismclient.gui.f.PacketUtil.init(-2039584, alpha),
                              width - ui.init_PoolEntry().measureWidth(time),
                              y,
                              true
                           );
                        } finally {
                           graphics.pose().popMatrix();
                        }
                        int row = rowHeight;

                        for (BedEspModule.CoverMaterial material : bed.materials()) {
                           String amount = "x" + material.count();
                           int rowWidth = ui.init_PoolEntry().measureWidth(material.name())
                              + 3
                              + ui.init_PoolEntry().measureWidth(amount)
                              + (material.icon().isEmpty() ? 0 : 4 + iconSize);
                           graphics.pose().pushMatrix();
                           try {
                              graphics.pose().translate(-rowWidth * 0.5F, 0.0F);
                              int y = ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(0, row, rowWidth, rowHeight));
                              autismclient.gui.f.b.k.init(graphics, init_Minecraft.font, material.name(), autismclient.gui.f.a.DataUtil.init, text, 0, y, true);
                              int x = ui.init_PoolEntry().measureWidth(material.name()) + 3;
                              autismclient.gui.f.b.k.init(graphics, init_Minecraft.font, amount, autismclient.gui.f.a.DataUtil.init, count, x, y, true);
                              if (!material.icon().isEmpty()) {
                                 init(
                                    graphics,
                                    material.icon(),
                                    x + ui.init_PoolEntry().measureWidth(amount) + 4,
                                    row + (rowHeight - iconSize) / 2,
                                    iconSize,
                                    alpha
                                 );
                              }
                           } finally {
                              graphics.pose().popMatrix();
                           }
                           row += rowHeight;
                        }
                     } finally {
                        graphics.pose().popMatrix();
                     }
                  }
               }
            }
         }
      }
   }

   static int init(int argb, int alpha) {
      return Math.clamp(alpha, 0, 255) << 24 | argb & 16777215;
   }

   static int init(int rgb) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      double light = 0.2126 * r + 0.7152 * g + 0.0722 * b;
      double lift = light < 180.0 ? (180.0 - light) / (255.0 - light) : 0.0;
      return 0xFF000000 | (int)Math.round(r + (255 - r) * lift) << 16 | (int)Math.round(g + (255 - g) * lift) << 8 | (int)Math.round(b + (255 - b) * lift);
   }

   private static void init(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, int size, float alpha) {
      graphics.blit(
         RenderPipelines.GUI_TEXTURED,
         Identifier.withDefaultNamespace("textures/item/" + BuiltInRegistries.ITEM.getKey(init(item)).getPath() + ".png"),
         x,
         y,
         0.0F,
         0.0F,
         size,
         size,
         16,
         16,
         16,
         16,
         autismclient.gui.f.PacketUtil.init(-1, alpha)
      );
   }

   static Item init(ItemStack stack) {
      if (stack.is(Items.NETHERITE_PICKAXE)) {
         return Items.DIAMOND_PICKAXE;
      } else if (stack.is(Items.NETHERITE_AXE)) {
         return Items.DIAMOND_AXE;
      } else if (stack.is(Items.NETHERITE_SHOVEL)) {
         return Items.DIAMOND_SHOVEL;
      } else {
         return stack.is(Items.NETHERITE_HOE) ? Items.DIAMOND_HOE : stack.getItem();
      }
   }

   private static float[] init(Matrix4f matrix, Vec3 camera, Vec3 pos) {
      Vector4f clip = new Vector4f((float)(pos.x - camera.x), (float)(pos.y - camera.y), (float)(pos.z - camera.z), 1.0F);
      matrix.transform(clip);
      if (clip.w <= 0.01F) {
         return null;
      }

      float x = clip.x / clip.w;
      float y = clip.y / clip.w;
      return !(Math.abs(x) > 1.2) && !(Math.abs(y) > 1.2) ? new float[]{(x + 1.0F) * 0.5F * cA.init_I(), (1.0F - y) * 0.5F * cA.load_I()} : null;
   }
}
