package autismclient.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

public class PerfCounterCheck {
   // bK logs through AutismClientAddon, whose FOLDER needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("perfcheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }

      System.setProperty("check.config", Files.createTempDirectory("perfcheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }

      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(PerfCounterCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      List<Thread> threads = new ArrayList<>();
      for (int t = 0; t < 8; t++) {
         Thread thread = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
               bK.init("race", System.nanoTime() - 1L);
            }
         });
         threads.add(thread);
         thread.start();
      }

      for (Thread thread : threads) {
         thread.join();
      }

      long samples = bK.init_Map().get("race")[0];
      if (samples != 800000L) {
         System.out.println("FAIL PerfCounterCheck: expected 800000 samples, got " + samples);
         System.exit(1);
      }

      System.out.println("PASS PerfCounterCheck: 8 threads x 100000 samples counted exactly");
   }
}
