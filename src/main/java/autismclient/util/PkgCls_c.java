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

public final class PkgCls_c implements AutoCloseable {
   private final StagedVertexBuffer init_StagedVertexBuffer;
   private final List<Draw> init_List = new ArrayList<>();
   private final List<RenderType> load = new ArrayList<>();

   public PkgCls_c() {
      this(4194304);
   }

   public PkgCls_c(int bufferSize) {
      this.init_StagedVertexBuffer = new StagedVertexBuffer(() -> "AutismBufferSource", bufferSize);
   }

   public VertexConsumer init(RenderType renderType) {
      if (!this.load.isEmpty() && this.load.getLast() == renderType && renderType.canConsolidateConsecutiveGeometry()) {
         return this.init_StagedVertexBuffer.getVertexBuilder(this.init_List.getLast());
      }

      Draw draw = this.init_StagedVertexBuffer
         .appendDraw(renderType.format(), renderType.primitiveTopology(), renderType.sortOnUpload() ? RenderSystem.getProjectionType().vertexSorting() : null);
      this.init_List.add(draw);
      this.load.add(renderType);
      return this.init_StagedVertexBuffer.getVertexBuilder(draw);
   }

   public void init() {
      try {
         if (this.init_List.isEmpty()) {
            return;
         }

         this.init_StagedVertexBuffer.upload();

         for (int i = 0; i < this.init_List.size(); i++) {
            this.init(this.load.get(i), this.init_List.get(i));
         }
      } finally {
         this.init_List.clear();
         this.load.clear();
         this.init_StagedVertexBuffer.endFrame();
      }
   }

   @Override
   public void close() {
      this.init_StagedVertexBuffer.close();
   }

   private void init(RenderType type, Draw draw) {
      ExecuteInfo info = this.init_StagedVertexBuffer.getExecuteInfo(draw);
      if (info != null) {
         Compat263.drawFromBuffer(type.prepare(), info);
      }
   }

   public static final class Inner_DataUtil {
      private final int init_I;
      private PkgCls_c init_c;

      public Inner_DataUtil(int bufferSize) {
         this.init_I = bufferSize;
      }

      public PkgCls_c init() {
         PkgCls_c current = this.init_c;
         if (current == null) {
            current = new PkgCls_c(this.init_I);
            this.init_c = current;
         }

         return current;
      }
   }
}
