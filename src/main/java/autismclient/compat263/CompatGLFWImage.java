package autismclient.compat263;

import java.nio.ByteBuffer;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;

public final class CompatGLFWImage {
   private CompatGLFWImage() {
   }

   public static CompatGLFWImage.Buffer malloc(int var0, MemoryStack var1) {
      return new CompatGLFWImage.Buffer();
   }

   public static final class Buffer {
      public CustomBuffer position(int var1) {
         return null;
      }

      public CompatGLFWImage.Buffer width(int var1) {
         return this;
      }

      public CompatGLFWImage.Buffer height(int var1) {
         return this;
      }

      public CompatGLFWImage.Buffer pixels(ByteBuffer var1) {
         return this;
      }
   }
}
