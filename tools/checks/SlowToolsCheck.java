package autismclient.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// Clipboard writes and Spotify backend probes must not block the calling (render/client) thread when the tools hang.
// Runs each case in a child JVM whose PATH holds slow fake tools only; the real clipboard and media players are never touched.
public class SlowToolsCheck {
   public static void main(String[] args) throws Exception {
      if (args.length == 2) {
         child(args[0], Path.of(args[1]));
         return;
      }

      Path bin = Files.createTempDirectory("slowtools-bin");
      Path out = bin.resolve("clip.out");
      for (String tool : List.of("xclip", "xsel", "wl-copy", "wl-paste")) {
         fake(bin, tool, "cat > '" + out + "'; sleep 2; exit 0");
      }

      run("clipboard", bin, out);
      Path shBin = Files.createTempDirectory("slowtools-sh");
      fake(shBin, "sh", "sleep 3; exit 1");
      run("spotify", shBin, out);
      System.out.println("PASS SlowToolsCheck: clipboard write and Spotify probes return at once with hung tools");
   }

   private static void fake(Path dir, String name, String body) throws Exception {
      Path tool = dir.resolve(name);
      Files.writeString(tool, "#!/bin/bash\n" + body + "\n");
      tool.toFile().setExecutable(true);
   }

   private static void run(String mode, Path bin, Path out) throws Exception {
      String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
      ProcessBuilder pb = new ProcessBuilder(java, "-Djava.awt.headless=true", "-cp", System.getProperty("java.class.path"), SlowToolsCheck.class.getName(), mode, out.toString());
      Map<String, String> env = pb.environment();
      env.keySet().removeIf(k -> k.equals("DISPLAY") || k.startsWith("WAYLAND") || k.startsWith("XDG_SESSION"));
      env.put("PATH", bin + File.pathSeparator + "/usr/bin" + File.pathSeparator + "/bin");
      pb.inheritIO();
      if (pb.start().waitFor() != 0) {
         System.out.println("FAIL SlowToolsCheck: " + mode);
         System.exit(1);
      }
   }

   private static void child(String mode, Path out) throws Exception {
      if (mode.equals("clipboard")) {
         long start = System.nanoTime();
         n.init_V("hello");
         long ms = (System.nanoTime() - start) / 1000000L;
         check(ms < 500L, "clipboard write blocked the caller for " + ms + " ms");
         check(n.init_Str().equals("hello"), "pending write not visible to a read");
         long deadline = System.currentTimeMillis() + 5000L;
         while (!(Files.exists(out) && Files.readString(out).equals("hello")) && System.currentTimeMillis() < deadline) {
            Thread.sleep(100L);
         }

         check(Files.exists(out) && Files.readString(out).equals("hello"), "clipboard was never written");
      } else {
         check(net.minecraft.util.Util.getPlatform() == net.minecraft.util.Util.OS.LINUX, "premise: Util loads and reports Linux");
         long start = System.nanoTime();
         MusicHudService.load_V();
         MusicHudService.init_V();
         long ms = (System.nanoTime() - start) / 1000000L;
         check(ms < 500L, "Spotify probes blocked the caller for " + ms + " ms");
      }

      System.exit(0);
   }

   private static void check(boolean ok, String message) {
      if (!ok) {
         System.out.println("FAIL SlowToolsCheck: " + message);
         System.exit(1);
      }
   }
}
