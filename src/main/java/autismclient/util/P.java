package autismclient.util;

import autismclient.compat263.Compat263;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.MeshData.DrawState;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import java.util.function.Consumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

public final class P implements AutoCloseable {
   private static final int init_I = 1048576;
   private final String init_Str;
   private GpuBuffer init_GpuBuffer;
   private PrimitiveTopology init_PrimitiveTopology;
   private int load;

   public P(String label) {
      this.init_Str = label;
   }

   public boolean init_Z() {
      return this.init_GpuBuffer != null && !this.init_GpuBuffer.isClosed() && this.load > 0;
   }

   public boolean init(RenderType type, Consumer<VertexConsumer> emit) {
      this.init_V();

      try (ByteBufferBuilder scratch = new ByteBufferBuilder(1048576)) {
         BufferBuilder builder = new BufferBuilder(scratch, type.primitiveTopology(), type.format());
         emit.accept(builder);
         MeshData mesh = builder.build();
         if (mesh == null) {
            return false;
         }

         try (mesh) {
            DrawState state = mesh.drawState();
            if (state.indexCount() <= 0) {
               return false;
            }

            this.init_PrimitiveTopology = state.primitiveTopology();
            this.load = state.indexCount();
            this.init_GpuBuffer = RenderSystem.getDevice().createBuffer(() -> this.init_Str, 32, mesh.vertexBuffer());
         }

         return this.init_Z();
      }
   }

   public void init(RenderType type, Matrix4fc framePose, double offsetX, double offsetY, double offsetZ) {
      if (this.init_Z()) {
         AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(this.init_PrimitiveTopology);
         GpuBuffer indices = sequential.getBuffer(this.load);
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();

         try {
            if (framePose != null) {
               modelView.mul(framePose);
            }

            modelView.translate((float)offsetX, (float)offsetY, (float)offsetZ);
            Compat263.drawFromBuffer(type.prepare(), this.init_GpuBuffer, indices, sequential.type(), 0, 0, this.load);
         } finally {
            modelView.popMatrix();
         }
      }
   }

   public void init_V() {
      GpuBuffer buffer = this.init_GpuBuffer;
      this.init_GpuBuffer = null;
      this.load = 0;
      this.init_PrimitiveTopology = null;
      if (buffer != null && !buffer.isClosed()) {
         if (RenderSystem.isOnRenderThread()) {
            buffer.close();
         } else {
            RenderSystem.queueFencedTask(buffer::close);
         }
      }
   }

   @Override
   public void close() {
      this.init_V();
   }
}
