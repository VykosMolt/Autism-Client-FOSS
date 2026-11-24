package autismclient.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

public class WaypointEmptyCheck {
   // AutismClientAddon.FOLDER needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("emptycheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }

      System.setProperty("check.config", Files.createTempDirectory("emptycheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }

      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(WaypointEmptyCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      File folder = new File(System.getProperty("check.config"), "autism");
      folder.mkdirs();
      File bak = new File(folder, "waypoints.json.bak");
      Files.writeString(new File(folder, "waypoints.json").toPath(), "");
      Files.writeString(bak.toPath(), "{\"example.com\":[{\"name\":\"Base\",\"x\":1,\"y\":2,\"z\":3,\"color\":0,\"createdMs\":1,\"death\":false}]}");
      AutismWaypoints store = AutismWaypoints.init_AutismWaypoints();
      check(store.init_List_Str("example.com").size() == 1, "zero-byte waypoints.json loaded as an empty store, backup ignored");
      String[] corrupt = folder.list((dir, name) -> name.startsWith("waypoints.json.corrupt-"));
      check(corrupt != null && corrupt.length == 1, "zero-byte waypoints.json was not moved aside");

      store.init("other", new AutismWaypoints.Waypoint("New", 0, 0, 0, 0, 2L));
      File primary = new File(folder, "waypoints.json");
      long deadline = System.currentTimeMillis() + 5000L;
      while (!(primary.exists() && Files.readString(primary.toPath()).contains("New")) && System.currentTimeMillis() < deadline) {
         Thread.sleep(100L);
      }

      check(Files.readString(primary.toPath()).contains("Base"), "save lost the restored waypoints");
      check(Files.readString(bak.toPath()).contains("Base"), "the good backup was overwritten");
      System.out.println("PASS WaypointEmptyCheck: empty waypoints.json counts as unreadable, backup kept");
      System.exit(0);
   }

   private static void check(boolean ok, String message) {
      if (!ok) {
         System.out.println("FAIL WaypointEmptyCheck: " + message);
         System.exit(1);
      }
   }
}
