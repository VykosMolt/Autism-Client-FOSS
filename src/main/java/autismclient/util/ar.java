package autismclient.util;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class ar {
   private ar() {
   }

   public static String init(String currentPath) {
      String current = currentPath == null ? "" : currentPath.trim();
      PointerBuffer filters = BufferUtils.createPointerBuffer(4);
      ByteBuffer gif = MemoryUtil.memASCII("*.gif");
      ByteBuffer png = MemoryUtil.memASCII("*.png");
      ByteBuffer jpg = MemoryUtil.memASCII("*.jpg");
      ByteBuffer jpeg = MemoryUtil.memASCII("*.jpeg");
      filters.put(gif).put(png).put(jpg).put(jpeg).rewind();

      try {
         String selected = TinyFileDialogs.tinyfd_openFileDialog("Select Image", current.isBlank() ? null : current, filters, "GIF / PNG / JPEG images", false);
         return selected != null && !selected.isBlank() ? selected : null;
      } finally {
         MemoryUtil.memFree(gif);
         MemoryUtil.memFree(png);
         MemoryUtil.memFree(jpg);
         MemoryUtil.memFree(jpeg);
      }
   }
}
