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
   private static final Map<String, Boolean> toolAvailability = new ConcurrentHashMap<>();
   private static volatile boolean fallbackLogged;
   private static volatile String cachedText;
   private static volatile boolean awtUnavailable;
   private static final AtomicInteger auditPendingWrites = new AtomicInteger();
   private static final ExecutorService auditWriter = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-clipboard-write");
      t.setDaemon(true);
      return t;
   });

   private SystemClipboardHelper() {
   }

   public static String getText() {
      if (auditPendingWrites.get() > 0) {
         return cachedText;
      }

      Minecraft mc = Minecraft.getInstance();
      String external;
      if (mc != null && mc.isSameThread()) {
         // On the render thread wait at most 500 ms for the tools, then fall back to GLFW.
         FutureTask<String> task = new FutureTask<>(SystemClipboardHelper::auditReadExternal);
         startDaemon(task);
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
         String glfw = readGlfw();
         if (!glfw.isEmpty()) {
            return glfw;
         } else {
            String local = cachedText;
            if (local != null) {
               logFallbackOnce("read");
               return local;
            } else {
               return "";
            }
         }
      }
   }

   private static String auditReadExternal() {
      for (String[] command : readCommands()) {
         if (hasTool(command[0])) {
            String out = runReadCommand(command);
            if (out != null && !out.isEmpty()) {
               return out;
            }
         }
      }

      return readAwt();
   }

   public static void setText(String text) {
      String value = text == null ? "" : text;
      cachedText = value;
      auditPendingWrites.incrementAndGet();
      // The tools can take seconds; writes run in order on one worker so copying never blocks the render thread.
      auditWriter.execute(() -> {
         try {
            for (String[] command : writeCommands()) {
               if (hasTool(command[0]) && runWriteCommand(command, value)) {
                  return;
               }
            }

            if (!writeAwt(value)) {
               logFallbackOnce("write");
               writeGlfw(value);
            }
         } finally {
            auditPendingWrites.decrementAndGet();
         }
      });
   }

   private static String[][] readCommands() {
      return isWayland()
         ? new String[][]{{"wl-paste", "--no-newline"}, {"xclip", "-selection", "clipboard", "-o"}, {"xsel", "--clipboard", "--output"}}
         : new String[][]{{"xclip", "-selection", "clipboard", "-o"}, {"xsel", "--clipboard", "--output"}, {"wl-paste", "--no-newline"}};
   }

   private static String[][] writeCommands() {
      return isWayland()
         ? new String[][]{{"wl-copy"}, {"xclip", "-selection", "clipboard", "-i"}, {"xsel", "--clipboard", "--input"}}
         : new String[][]{{"xclip", "-selection", "clipboard", "-i"}, {"xsel", "--clipboard", "--input"}, {"wl-copy"}};
   }

   private static boolean isWayland() {
      String sessionType = System.getenv("XDG_SESSION_TYPE");
      if (sessionType != null && sessionType.toLowerCase(Locale.ROOT).contains("wayland")) {
         return true;
      }

      String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
      return waylandDisplay != null && !waylandDisplay.isBlank();
   }

   private static boolean hasTool(String tool) {
      return toolAvailability.computeIfAbsent(tool, SystemClipboardHelper::probeTool);
   }

   private static boolean probeTool(String tool) {
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

   private static boolean runWriteCommand(String[] command, String text) {
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

   private static String runReadCommand(String[] command) {
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

   private static String readAwt() {
      if (isAwtUnavailable()) {
         return null;
      }

      FutureTask<String> task = new FutureTask<>(
         () -> Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor) instanceof String s ? s : null
      );
      startDaemon(task);

      try {
         return task.get(3000L, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
         awtUnavailable = true;
         task.cancel(true);
         return null;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return null;
      } catch (Exception e) {
         return null;
      }
   }

   private static boolean writeAwt(String text) {
      if (isAwtUnavailable()) {
         return false;
      }

      FutureTask<Boolean> task = new FutureTask<>(() -> {
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
         return true;
      });
      startDaemon(task);

      try {
         return Boolean.TRUE.equals(task.get(3000L, TimeUnit.MILLISECONDS));
      } catch (TimeoutException e) {
         awtUnavailable = true;
         task.cancel(true);
         return false;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return false;
      } catch (Exception e) {
         return false;
      }
   }

   private static boolean isAwtUnavailable() {
      if (awtUnavailable) {
         return true;
      }

      try {
         if (GraphicsEnvironment.isHeadless()) {
            awtUnavailable = true;
         }
      } catch (Throwable t) {
         awtUnavailable = true;
      }

      return awtUnavailable;
   }

   private static void startDaemon(FutureTask<?> task) {
      Thread thread = new Thread(task, "autism-clipboard-awt");
      thread.setDaemon(true);
      thread.start();
   }

   private static String readGlfw() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         long window = mc.getWindow().handle();
         if (window == 0L) {
            return "";
         }

         if (mc.isSameThread()) {
            return glfwRead(window);
         }

         CompletableFuture<String> future = new CompletableFuture<>();
         mc.execute(() -> future.complete(glfwRead(window)));

         try {
            return future.get(2L, TimeUnit.SECONDS);
         } catch (Exception e) {
            return "";
         }
      } else {
         return "";
      }
   }

   private static void writeGlfw(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         long window = mc.getWindow().handle();
         if (window != 0L) {
            if (mc.isSameThread()) {
               glfwWrite(window, text);
            } else {
               mc.execute(() -> glfwWrite(window, text));
            }
         }
      }
   }

   private static String glfwRead(long window) {
      try {
         String value = CompatGLFW.glfwGetClipboardString(window);
         return value != null ? value : "";
      } catch (Throwable t) {
         return "";
      }
   }

   private static void glfwWrite(long window, String text) {
      try {
         CompatGLFW.glfwSetClipboardString(window, text);
      } catch (Throwable var4) {
      }
   }

   private static void logFallbackOnce(String operation) {
      if (!fallbackLogged) {
         fallbackLogged = true;
         AutismClientAddon.LOG.info("[Autism] Clipboard {}: no native tool (wl-copy/xclip/xsel) or AWT path worked; using GLFW/in-client fallback", operation);
      }
   }
}
