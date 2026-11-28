package autismclient.util;

import autismclient.compat263.Compat263;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.StagedVertexBuffer.Draw;
import net.minecraft.client.renderer.StagedVertexBuffer.ExecuteInfo;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class AutismBufferSource implements AutoCloseable {
   private final StagedVertexBuffer stagedBuffer;
   private final List<Draw> draws = new ArrayList<>();
   private final List<RenderType> load = new ArrayList<>();

   public AutismBufferSource() {
      this(4194304);
   }

   public AutismBufferSource(int bufferSize) {
      this.stagedBuffer = new StagedVertexBuffer(() -> "AutismBufferSource", bufferSize);
   }

   public VertexConsumer init(RenderType renderType) {
      if (!this.load.isEmpty() && this.load.getLast() == renderType && renderType.canConsolidateConsecutiveGeometry()) {
         return this.stagedBuffer.getVertexBuilder(this.draws.getLast());
      }

      Draw draw = this.stagedBuffer
         .appendDraw(renderType.format(), renderType.primitiveTopology(), renderType.sortOnUpload() ? RenderSystem.getProjectionType().vertexSorting() : null);
      this.draws.add(draw);
      this.load.add(renderType);
      return this.stagedBuffer.getVertexBuilder(draw);
   }

   public void init() {
      try {
         if (this.draws.isEmpty()) {
            return;
         }

         this.stagedBuffer.upload();

         for (int i = 0; i < this.draws.size(); i++) {
            this.init(this.load.get(i), this.draws.get(i));
         }
      } finally {
         this.draws.clear();
         this.load.clear();
         this.stagedBuffer.endFrame();
      }
   }

   @Override
   public void close() {
      this.stagedBuffer.close();
   }

   private void init(RenderType type, Draw draw) {
      ExecuteInfo info = this.stagedBuffer.getExecuteInfo(draw);
      if (info != null) {
         Compat263.drawFromBuffer(type.prepare(), info);
      }
   }

   public static final class LazySource {
      private final int bufferSize;
      private AutismBufferSource current;

      public LazySource(int bufferSize) {
         this.bufferSize = bufferSize;
      }

      public AutismBufferSource init() {
         AutismBufferSource current = this.current;
         if (current == null) {
            current = new AutismBufferSource(this.bufferSize);
            this.current = current;
         }

         return current;
      }
   }
}
