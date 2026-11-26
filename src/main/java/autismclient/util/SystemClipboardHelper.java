package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.CompatGLFW;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;

public final class SystemClipboardHelper {
   private static final long init_J = 3000L;
   private static final Map<String, Boolean> init_Map = new ConcurrentHashMap<>();
   private static volatile boolean init_Z;
   private static volatile String init_Str;
   private static volatile boolean load;
   private static final AtomicInteger auditPendingWrites = new AtomicInteger();
   private static final ExecutorService auditWriter = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-clipboard-write");
      t.setDaemon(true);
      return t;
   });

   private SystemClipboardHelper() {
   }

   public static String init_Str() {
      if (auditPendingWrites.get() > 0) {
         return init_Str;
      }

      Minecraft mc = Minecraft.getInstance();
      String external;
      if (mc != null && mc.isSameThread()) {
         // On the render thread wait at most 500 ms for the tools, then fall back to GLFW.
         FutureTask<String> task = new FutureTask<>(SystemClipboardHelper::auditReadExternal);
         init(task);
         try {
            external = task.get(500L, TimeUnit.MILLISECONDS);
         } catch (Exception e) {
            task.cancel(true);
            external = null;
         }
      } else {
         external = auditReadExternal();
      }

      if (external != null && !external.isEmpty()) {
         return external;
      } else {
         String glfw = read();
         if (!glfw.isEmpty()) {
            return glfw;
         } else {
            String local = init_Str;
            if (local != null) {
               read_V("read");
               return local;
            } else {
               return "";
            }
         }
      }
   }

   private static String auditReadExternal() {
      for (String[] command : init_StrArr2()) {
         if (init_Z(command[0])) {
            String out = init(command);
            if (out != null && !out.isEmpty()) {
               return out;
            }
         }
      }

      return load_Str();
   }

   public static void init_V(String text) {
      String value = text == null ? "" : text;
      init_Str = value;
      auditPendingWrites.incrementAndGet();
      // The tools can take seconds; writes run in order on one worker so copying never blocks the render thread.
      auditWriter.execute(() -> {
         try {
            for (String[] command : load_StrArr2()) {
               if (init_Z(command[0]) && init(command, value)) {
                  return;
               }
            }

            if (!read_Z(value)) {
               read_V("write");
               load_V(value);
            }
         } finally {
            auditPendingWrites.decrementAndGet();
         }
      });
   }

   private static String[][] init_StrArr2() {
      return init_Z()
         ? new String[][]{{"wl-paste", "--no-newline"}, {"xclip", "-selection", "clipboard", "-o"}, {"xsel", "--clipboard", "--output"}}
         : new String[][]{{"xclip", "-selection", "clipboard", "-o"}, {"xsel", "--clipboard", "--output"}, {"wl-paste", "--no-newline"}};
   }

   private static String[][] load_StrArr2() {
      return init_Z()
         ? new String[][]{{"wl-copy"}, {"xclip", "-selection", "clipboard", "-i"}, {"xsel", "--clipboard", "--input"}}
         : new String[][]{{"xclip", "-selection", "clipboard", "-i"}, {"xsel", "--clipboard", "--input"}, {"wl-copy"}};
   }

   private static boolean init_Z() {
      String sessionType = System.getenv("XDG_SESSION_TYPE");
      if (sessionType != null && sessionType.toLowerCase(Locale.ROOT).contains("wayland")) {
         return true;
      }

      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
      return waylandDisplay != null && !waylandDisplay.isBlank();
   }

   private static boolean init_Z(String tool) {
      return init_Map.computeIfAbsent(tool, SystemClipboardHelper::load_Z);
   }

   private static boolean load_Z(String tool) {
      Process process = null;

      try {
         process = new ProcessBuilder("sh", "-c", "command -v " + tool).redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD).start();
         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return false;
         } else {
            return process.exitValue() == 0;
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return false;
      }
   }

   private static boolean init(String[] command, String text) {
      Process process = null;

      try {
         process = new ProcessBuilder(command).redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD).start();

         try (OutputStream stdin = process.getOutputStream()) {
            stdin.write(text.getBytes(StandardCharsets.UTF_8));
         }

         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return false;
         } else {
            return process.exitValue() == 0;
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return false;
      }
   }

   private static String init(String[] command) {
      Process process = null;

      try {
         process = new ProcessBuilder(command).redirectError(Redirect.DISCARD).start();
         Process child = process;
         CompletableFuture<byte[]> reader = CompletableFuture.supplyAsync(() -> {
            try {
               return child.getInputStream().readAllBytes();
            } catch (IOException ex) {
               return new byte[0];
            }
         });
         if (!process.waitFor(3000L, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return null;
         } else {
            byte[] out = reader.get(1L, TimeUnit.SECONDS);
            return process.exitValue() != 0 ? null : new String(out, StandardCharsets.UTF_8);
         }
      } catch (Exception e) {
         if (process != null) {
            process.destroyForcibly();
         }

         return null;
      }
   }

   private static String load_Str() {
      if (load_Z()) {
         return null;
      }

      FutureTask<String> task = new FutureTask<>(
         () -> Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor) instanceof String s ? s : null
      );
      init(task);

      try {
         return task.get(3000L, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
         load = true;
         task.cancel(true);
         return null;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception e) {
         return null;
      }
   }

   private static boolean read_Z(String text) {
      if (load_Z()) {
         return false;
      }

      FutureTask<Boolean> task = new FutureTask<>(() -> {
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
         return true;
      });
      init(task);

      try {
         return Boolean.TRUE.equals(task.get(3000L, TimeUnit.MILLISECONDS));
      } catch (TimeoutException e) {
         load = true;
         task.cancel(true);
         return false;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return false;
      } catch (Exception e) {
         return false;
      }
   }

   private static boolean load_Z() {
      if (load) {
         return true;
      }

      try {
         if (GraphicsEnvironment.isHeadless()) {
            load = true;
         }
      } catch (Throwable t) {
         load = true;
      }

      return load;
   }

   private static void init(FutureTask<?> task) {
      Thread thread = new Thread(task, "autism-clipboard-awt");
      thread.setDaemon(true);
      thread.start();
   }

   private static String read() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         long window = mc.getWindow().handle();
         if (window == 0L) {
            return "";
         }

         if (mc.isSameThread()) {
            return init(window);
         }

         CompletableFuture<String> future = new CompletableFuture<>();
         mc.execute(() -> future.complete(init(window)));

         try {
            return future.get(2L, TimeUnit.SECONDS);
         } catch (Exception e) {
            return "";
         }
      } else {
         return "";
      }
   }

   private static void load_V(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         long window = mc.getWindow().handle();
         if (window != 0L) {
            if (mc.isSameThread()) {
               init(window, text);
            } else {
               mc.execute(() -> init(window, text));
            }
         }
      }
   }

   private static String init(long window) {
      try {
         String value = CompatGLFW.glfwGetClipboardString(window);
         return value != null ? value : "";
      } catch (Throwable t) {
         return "";
      }
   }

   private static void init(long window, String text) {
      try {
         CompatGLFW.glfwSetClipboardString(window, text);
      } catch (Throwable var4) {
      }
   }

   private static void read_V(String operation) {
      if (!init_Z) {
         init_Z = true;
         AutismClientAddon.LOG.info("[Autism] Clipboard {}: no native tool (wl-copy/xclip/xsel) or AWT path worked; using GLFW/in-client fallback", operation);
      }
   }
}
