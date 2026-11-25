package autismclient.compat263;

import java.nio.ByteBuffer;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;

public final class CompatGLFWImage {
   private CompatGLFWImage() {
   }

   public static CompatGLFWImage.Buffer malloc(int count, MemoryStack stack) {
      return new CompatGLFWImage.Buffer(Math.max(0, count));
   }

   public static final class Buffer {
      final int[] widths;
      final int[] heights;
      final ByteBuffer[] pixels;
      private int pos;

      Buffer(int capacity) {
         this.widths = new int[capacity];
         this.heights = new int[capacity];
         this.pixels = new ByteBuffer[capacity];
      }

      public CustomBuffer position(int index) {
         this.pos = index;
         return null;
      }

      public CompatGLFWImage.Buffer width(int width) {
         if (this.pos >= 0 && this.pos < this.widths.length) {
            this.widths[this.pos] = width;
         }

         return this;
      }

      public CompatGLFWImage.Buffer height(int height) {
         if (this.pos >= 0 && this.pos < this.heights.length) {
            this.heights[this.pos] = height;
         }

         return this;
      }

      public CompatGLFWImage.Buffer pixels(ByteBuffer buf) {
         if (this.pos >= 0 && this.pos < this.pixels.length) {
            this.pixels[this.pos] = buf;
         }

         return this;
      }
   }
}
