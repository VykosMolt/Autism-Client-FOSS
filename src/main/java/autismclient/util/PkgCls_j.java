package autismclient.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class PkgCls_j {
   private static final List<PkgCls_j.Inner_DataUtil> init_List = new ArrayList<>();
   private static final List<PkgCls_j.Inner_StreamUtil> load = new ArrayList<>();
   private static final PkgCls_c.Inner_DataUtil init_c_DataUtil = new PkgCls_c.Inner_DataUtil(4194304);

   private PkgCls_j() {
   }

   public static void init_V() {
      init_List.clear();
      load.clear();
   }

   public static boolean init_Z() {
      return !init_List.isEmpty();
   }

   public static void init(
      Model<?> model,
      Object state,
      Pose pose,
      RenderType visibleType,
      RenderType occludedType,
      int light,
      int overlay,
      int visibleColor,
      int occludedColor,
      TextureAtlasSprite sprite
   ) {
      init_List.add(new PkgCls_j.Inner_DataUtil(model, state, pose, visibleType, occludedType, light, overlay, visibleColor, occludedColor, sprite));
   }

   public static void init(Model<?> model, Object state, Pose pose, RenderType type, int light, int overlay, int color, TextureAtlasSprite sprite) {
      load.add(new PkgCls_j.Inner_StreamUtil(model, state, pose, type, light, overlay, color, sprite));
   }

   public static void init(PoseStack framePose) {
      if (!init_List.isEmpty()) {
         List<PkgCls_j.Inner_DataUtil> bodies = List.copyOf(init_List);
         List<PkgCls_j.Inner_StreamUtil> layers = List.copyOf(load);
         init_V();
         PkgCls_c buffers = init_c_DataUtil.init();

         for (PkgCls_j.Inner_DataUtil body : bodies) {
            init(
               buffers,
               framePose,
               body.init_Model(),
               body.init_Obj(),
               body.init_PoseStack_Pose(),
               body.load_RenderType(),
               body.init_I(),
               body.load_I(),
               body.write(),
               body.init_TextureAtlasSprite()
            );
         }

         for (PkgCls_j.Inner_DataUtil body : bodies) {
            init(
               buffers,
               framePose,
               body.init_Model(),
               body.init_Obj(),
               body.init_PoseStack_Pose(),
               body.init_RenderType(),
               body.init_I(),
               body.load_I(),
               body.read(),
               body.init_TextureAtlasSprite()
            );
         }

         for (PkgCls_j.Inner_StreamUtil layer : layers) {
            init(
               buffers,
               framePose,
               layer.init_Model(),
               layer.init_Obj(),
               layer.init_PoseStack_Pose(),
               layer.init_RenderType(),
               layer.init_I(),
               layer.load(),
               layer.read(),
               layer.init_TextureAtlasSprite()
            );
         }

         buffers.init();
      }
   }

   private static void init(
      PkgCls_c buffers,
      PoseStack framePose,
      Model<?> model,
      Object state,
      Pose modelPose,
      RenderType type,
      int light,
      int overlay,
      int color,
      TextureAtlasSprite sprite
   ) {
      framePose.pushPose();

      try {
         framePose.mulPose(modelPose.pose());
         VertexConsumer consumer = buffers.init(type);
         if (sprite != null) {
            consumer = sprite.wrap(consumer);
         }

         Model rawModel = model;
         rawModel.setupAnim(state);
         rawModel.renderToBuffer(framePose, consumer, light, overlay, color);
      } finally {
         framePose.popPose();
      }
   }

   private record Inner_DataUtil(
      Model<?> init_Model,
      Object init_Obj,
      Pose init_PoseStack_Pose,
      RenderType init_RenderType,
      RenderType load_RenderType,
      int init_I,
      int load_I,
      int read,
      int write,
      TextureAtlasSprite init_TextureAtlasSprite
   ) {
   }

   private record Inner_StreamUtil(
      Model<?> init_Model,
      Object init_Obj,
      Pose init_PoseStack_Pose,
      RenderType init_RenderType,
      int init_I,
      int load,
      int read,
      TextureAtlasSprite init_TextureAtlasSprite
   ) {
   }
}
