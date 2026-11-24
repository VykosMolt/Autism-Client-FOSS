package autismclient.util.oresim;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

public class SaveRetryCheck {
   // AutismClientAddon.FOLDER needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("retrycheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }
      System.setProperty("check.config", Files.createTempDirectory("retrycheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }
      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(SaveRetryCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      File folder = new File(System.getProperty("check.config"), "autism");
      folder.mkdirs();
      File saved = new File(folder, "ore-sim-seeds.json");
      File blocker = new File(folder, "ore-sim-seeds.json.tmp");
      // A directory where the temp file goes makes the first save fail.
      blocker.mkdirs();
      AutismOreSimSeedStore store = AutismOreSimSeedStore.init_AutismOreSimSeedSt();
      store.init_V("check-scope", "12345");
      Thread.sleep(700L);
      if (saved.exists()) {
         System.out.println("FAIL: premise, first save should have failed");
         System.exit(1);
      }
      blocker.delete();
      long deadline = System.currentTimeMillis() + 5000L;
      while (!saved.exists() && System.currentTimeMillis() < deadline) {
         Thread.sleep(100L);
      }
      if (!saved.exists() || !Files.readString(saved.toPath()).contains("12345")) {
         System.out.println("FAIL: failed save was not retried");
         System.exit(1);
      }

      File waypointsSaved = new File(folder, "waypoints.json");
      File waypointsBlocker = new File(folder, "waypoints.json.tmp");
      waypointsBlocker.mkdirs();
      autismclient.util.AutismWaypoints.init_AutismWaypoints()
         .init("check-scope", new autismclient.util.AutismWaypoints.Waypoint("checkpoint", 1, 2, 3, 0, 0L));
      Thread.sleep(700L);
      if (waypointsSaved.exists()) {
         System.out.println("FAIL: premise, first waypoint save should have failed");
         System.exit(1);
      }
      waypointsBlocker.delete();
      deadline = System.currentTimeMillis() + 5000L;
      while (!waypointsSaved.exists() && System.currentTimeMillis() < deadline) {
         Thread.sleep(100L);
      }
      if (!waypointsSaved.exists() || !Files.readString(waypointsSaved.toPath()).contains("checkpoint")) {
         System.out.println("FAIL: failed waypoint save was not retried");
         System.exit(1);
      }
      System.out.println("PASS SaveRetryCheck");
   }
}
