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

public final class CachedGpuMeshBuffer implements AutoCloseable {
   private static final int SCRATCH_SIZE = 1048576;
   private final String label;
   private GpuBuffer init_GpuBuffer;
   private PrimitiveTopology init_PrimitiveTopology;
   private int indexCount;

   public CachedGpuMeshBuffer(String label) {
      this.label = label;
   }

   public boolean hasBuffer() {
      return this.init_GpuBuffer != null && !this.init_GpuBuffer.isClosed() && this.indexCount > 0;
   }

   public boolean init(RenderType type, Consumer<VertexConsumer> emit) {
      this.release();

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
            this.indexCount = state.indexCount();
            this.init_GpuBuffer = RenderSystem.getDevice().createBuffer(() -> this.label, 32, mesh.vertexBuffer());
         }

         return this.hasBuffer();
      }
   }

   public void init(RenderType type, Matrix4fc framePose, double offsetX, double offsetY, double offsetZ) {
      if (this.hasBuffer()) {
         AutoStorageIndexBuffer sequential = RenderSystem.getSequentialBuffer(this.init_PrimitiveTopology);
         GpuBuffer indices = sequential.getBuffer(this.indexCount);
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();

         try {
            if (framePose != null) {
               modelView.mul(framePose);
            }

            modelView.translate((float)offsetX, (float)offsetY, (float)offsetZ);
            Compat263.drawFromBuffer(type.prepare(), this.init_GpuBuffer, indices, sequential.type(), 0, 0, this.indexCount);
         } finally {
            modelView.popMatrix();
         }
      }
   }

   public void release() {
      GpuBuffer buffer = this.init_GpuBuffer;
      this.init_GpuBuffer = null;
      this.indexCount = 0;
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
      this.release();
   }
}
