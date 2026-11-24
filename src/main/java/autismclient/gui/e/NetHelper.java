package autismclient.gui.e;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record NetHelper(
   RenderPipeline init_RenderPipeline,
   TextureSetup init_TextureSetup,
   Matrix3x2fc init_Matrix3x2fc,
   float[] init_FArr,
   int init_I,
   int load_I,
   int read,
   float init_F,
   float load_F,
   @Nullable ScreenRectangle init_ScreenRectangle,
   @Nullable ScreenRectangle load_ScreenRectangle
) implements GuiElementRenderState {
   public static @Nullable NetHelper init(
      RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, float[] quads, int quadCount, int color, @Nullable ScreenRectangle scissorArea
   ) {
      return init(pipeline, textureSetup, pose, quads, quadCount, color, color, 0.0F, 1.0F, scissorArea);
   }

   public static @Nullable NetHelper init(
      RenderPipeline pipeline,
      TextureSetup textureSetup,
      Matrix3x2fc pose,
      float[] quads,
      int quadCount,
      int topColor,
      int bottomColor,
      float gradientY,
      float gradientHeight,
      @Nullable ScreenRectangle scissorArea
   ) {
      if (quadCount <= 0) {
         return null;
      }

      float minX = 3.4028235E38F;
      float minY = 3.4028235E38F;
      float maxX = -3.4028235E38F;
      float maxY = -3.4028235E38F;

      for (int i = 0; i < quadCount; i++) {
         int o = i * 8;
         minX = Math.min(minX, quads[o]);
         minY = Math.min(minY, quads[o + 1]);
         maxX = Math.max(maxX, quads[o + 2]);
         maxY = Math.max(maxY, quads[o + 3]);
      }

      int bx = (int)Math.floor(minX);
      int by = (int)Math.floor(minY);
      ScreenRectangle bounds = new ScreenRectangle(bx, by, Math.max(1, (int)Math.ceil(maxX) - bx), Math.max(1, (int)Math.ceil(maxY) - by))
         .transformMaxBounds(pose);
      if (scissorArea != null) {
         bounds = scissorArea.intersection(bounds);
         if (bounds == null) {
            return null;
         }
      }

      topColor = autismclient.gui.f.TaskQueue.init_I_I(topColor);
      bottomColor = autismclient.gui.f.TaskQueue.init_I_I(bottomColor);
      return new NetHelper(
         pipeline, textureSetup, pose, quads, quadCount, topColor, bottomColor, gradientY, Math.max(0.001F, gradientHeight), scissorArea, bounds
      );
   }

   @Override
   public void buildVertices(VertexConsumer consumer) {
      for (int i = 0; i < this.init_I; i++) {
         int o = i * 8;
         int top = this.init(this.init_FArr[o + 1]);
         int bottom = this.init(this.init_FArr[o + 3]);
         consumer.addVertexWith2DPose(this.init_Matrix3x2fc, this.init_FArr[o], this.init_FArr[o + 1])
            .setUv(this.init_FArr[o + 4], this.init_FArr[o + 5])
            .setColor(top);
         consumer.addVertexWith2DPose(this.init_Matrix3x2fc, this.init_FArr[o], this.init_FArr[o + 3])
            .setUv(this.init_FArr[o + 4], this.init_FArr[o + 7])
            .setColor(bottom);
         consumer.addVertexWith2DPose(this.init_Matrix3x2fc, this.init_FArr[o + 2], this.init_FArr[o + 3])
            .setUv(this.init_FArr[o + 6], this.init_FArr[o + 7])
            .setColor(bottom);
         consumer.addVertexWith2DPose(this.init_Matrix3x2fc, this.init_FArr[o + 2], this.init_FArr[o + 1])
            .setUv(this.init_FArr[o + 6], this.init_FArr[o + 5])
            .setColor(top);
      }
   }

   private int init(float y) {
      if (this.load_I == this.read) {
         return this.load_I;
      }

      float t = Math.clamp((y - this.init_F) / this.load_F, 0.0F, 1.0F);
      int result = 0;

      for (int shift = 0; shift <= 24; shift += 8) {
         int start = this.load_I >>> shift & 0xFF;
         int end = this.read >>> shift & 0xFF;
         result |= Math.round(start + (end - start) * t) << shift;
      }

      return result;
   }

   @Override
   public RenderPipeline pipeline() {
      return this.init_RenderPipeline;
   }

   @Override
   public TextureSetup textureSetup() {
      return this.init_TextureSetup;
   }

   @Override
   public @Nullable ScreenRectangle scissorArea() {
      return this.init_ScreenRectangle;
   }

   @Override
   public @Nullable ScreenRectangle bounds() {
      return this.load_ScreenRectangle;
   }
}
