package autismclient.util;

import autismclient.AutismClientAddon;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

public final class ar {
   private static final AtomicReference<ImageDialog> PENDING_DIALOG = new AtomicReference<>();

   private ar() {
   }

   /** Opens the native dialog and delivers a selection on the client thread. */
   public static void pickImage(String currentPath, Consumer<String> onSelected) {
      Minecraft client = Minecraft.getInstance();
      AutismConfig profile = AutismConfig.getGlobal();
      client.schedule(() -> showImageDialog(
         client.getWindow().handle(),
         currentPath,
         selected -> client.schedule(() -> {
            if (profile == AutismConfig.getGlobal()) {
               onSelected.accept(selected);
            }
         }),
         error -> client.schedule(() -> {
            AutismClientAddon.LOG.warn("Could not open the image picker: {}", error);
            AutismClientMessaging.sendPrefixed("§cCould not open the image picker. Enter the image path manually.");
         })
      ));
   }

   private static void showImageDialog(long window, String currentPath, Consumer<String> onSelected, Consumer<String> onError) {
      ImageDialog dialog = new ImageDialog(onSelected, onError);
      if (!PENDING_DIALOG.compareAndSet(null, dialog)) {
         return;
      }

      String location = currentPath == null || currentPath.isBlank() ? null : currentPath.trim();
      try {
         SDLDialog.SDL_ShowOpenFileDialog(NativeDialog.CALLBACK, 0L, window, NativeDialog.FILTER, location, false);
      } catch (RuntimeException | LinkageError error) {
         if (PENDING_DIALOG.compareAndSet(dialog, null)) {
            dialog.onError().accept(error.toString());
         }
      }
   }

   private static void onDialogClosed(long userdata, long fileList, int filter) {
      ImageDialog dialog = PENDING_DIALOG.get();
      if (dialog == null) {
         return;
      }

      try {
         if (fileList == MemoryUtil.NULL) {
            dialog.onError().accept(SDLError.SDL_GetError());
         } else {
            long path = MemoryUtil.memGetAddress(fileList);
            if (path != MemoryUtil.NULL) {
               // SDL releases the native file list when this callback returns.
               String selected = MemoryUtil.memUTF8(path);
               if (!selected.isBlank()) {
                  dialog.onSelected().accept(selected);
               }
            }
         }
      } finally {
         PENDING_DIALOG.compareAndSet(dialog, null);
      }
   }

   private record ImageDialog(Consumer<String> onSelected, Consumer<String> onError) {
   }

   private static final class NativeDialog {
      // SDL reads the filters asynchronously. Keep the array and both strings alive with the callback.
      private static final ByteBuffer FILTER_NAME = MemoryUtil.memASCII("GIF / PNG / JPEG images");
      private static final ByteBuffer FILTER_EXTENSIONS = MemoryUtil.memASCII("gif;png;jpg;jpeg");
      private static final SDL_DialogFileFilter.Buffer FILTER = SDL_DialogFileFilter.create(1)
         .name(FILTER_NAME)
         .pattern(FILTER_EXTENSIONS);
      private static final SDL_DialogFileCallback CALLBACK = SDL_DialogFileCallback.create(ar::onDialogClosed);
   }
}
