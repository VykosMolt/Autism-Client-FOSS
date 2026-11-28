package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.compat263.CompatGLFWImage;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackMetadataResources;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class WindowIconManager {
   public static final String WINDOW_TITLE = "AUTISM Client";
   private static final String ICON_PATH = "assets/autismclient/icons/window/";
   private static final int[] ICON_SIZES = new int[]{16, 32, 48, 128, 256};
   private static final int init_I = 60;
   private static boolean init_Z = false;
   private static int load_I;

   private WindowIconManager() {
   }

   public static void apply(Minecraft client) {
      if (!init_Z && client != null && client.getWindow() != null) {
         init_Z = true;
         scheduleApply(client);
      }
   }

   public static void scheduleApply(Minecraft client) {
      load_I = 60;
      retryApply(client);
   }

   public static void tick(Minecraft client) {
      if (load_I > 0) {
         retryApply(client);
      }
   }

   private static void retryApply(Minecraft client) {
      if (load_I > 0) {
         load_I--;
         if (client != null && client.getWindow() != null) {
            boolean hidden = autismclient.modules.aD.init_Z() || AutismLiteVariant.enabled();
            boolean ok;
            if (hidden) {
               client.updateTitle();
               ok = applyIcons(client);
            } else {
               client.getWindow().setTitle("AUTISM Client");
               ok = applyIconsToWindow(client.getWindow().handle());
            }

            if (ok) {
               load_I = 0;
            }
         }
      }
   }

   private static boolean applyIcons(Minecraft client) {
      try {
         IconSet iconSet = SharedConstants.getCurrentVersion().stable() ? IconSet.RELEASE : IconSet.SNAPSHOT;
         client.getWindow().setIcon((PackMetadataResources)client.getVanillaPackResources(), iconSet);
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private static boolean applyIconsToWindow(long windowHandle) {
      if (windowHandle == 0L) {
         return false;
      }

      List<ByteBuffer> allocatedBuffers = new ArrayList<>(ICON_SIZES.length);

      try (MemoryStack stack = MemoryStack.stackPush()) {
         CompatGLFWImage.Buffer icons = CompatGLFWImage.malloc(ICON_SIZES.length, stack);

         for (int i = 0; i < ICON_SIZES.length; i++) {
            int size = ICON_SIZES[i];

            try (
               InputStream stream = openIcon("icon_" + size + "x" + size + ".png");
               NativeImage image = NativeImage.read(stream);
            ) {
               ByteBuffer pixels = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
               allocatedBuffers.add(pixels);
               pixels.asIntBuffer().put(image.getPixelsABGR());
               icons.position(i);
               icons.width(image.getWidth());
               icons.height(image.getHeight());
               icons.pixels(pixels);
            }
         }

         CompatGLFW.glfwSetWindowIcon(windowHandle, icons);
         return true;
      } catch (Throwable ignored) {
         return false;
      } finally {
         allocatedBuffers.forEach(MemoryUtil::memFree);
      }
   }

   private static InputStream openIcon(String fileName) throws IOException {
      InputStream stream = WindowIconManager.class.getClassLoader().getResourceAsStream("assets/autismclient/icons/window/" + fileName);
      if (stream == null) {
         throw new IOException("Missing Autism window icon resource: " + fileName);
      } else {
         return stream;
      }
   }
}
