package autismclient.util;

import com.google.gson.Gson;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;

public class WaypointStoreCheck {
   // AutismClientAddon.FOLDER needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("waypointcheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }

      System.setProperty("check.config", Files.createTempDirectory("waypointcheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }

      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(WaypointStoreCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      File folder = new File(System.getProperty("check.config"), "autism");
      folder.mkdirs();
      String nether = AuditWaypointScope.qualify("[2001:db8::2]:25565", "minecraft:the_nether");
      Map<String, List<AutismWaypoints.Waypoint>> backup = new LinkedHashMap<>();
      backup.put("example.com:25565", List.of(new AutismWaypoints.Waypoint("Base", 1, 2, 3, 0, 1L)));
      backup.put("example.com", List.of(new AutismWaypoints.Waypoint("Base", 4, 5, 6, 0, 2L)));
      backup.put(nether, List.of(new AutismWaypoints.Waypoint("Portal", 7, 8, 9, 0, 3L)));
      backup.put("Survival: Hard", List.of(new AutismWaypoints.Waypoint("Home", 0, 64, 0, 0, 4L)));
      Files.writeString(new File(folder, "waypoints.json").toPath(), "{\"example.com\": [{\"name\": \"Base\", \"x\": \"oops\"");
      Files.writeString(new File(folder, "waypoints.json.bak").toPath(), new Gson().toJson(backup));

      AutismWaypoints store = AutismWaypoints.getInstance();
      String[] corrupt = folder.list((dir, name) -> name.startsWith("waypoints.json.corrupt-"));
      check(corrupt != null && corrupt.length == 1, "corrupt file was not kept aside");
      for (Map.Entry<String, List<AutismWaypoints.Waypoint>> e : backup.entrySet()) {
         check(store.list(e.getKey()).equals(e.getValue()), "backup not restored unchanged under " + e.getKey());
      }

      File primary = new File(folder, "waypoints.json");
      File bak = new File(folder, "waypoints.json.bak");
      await(primary, "Base");
      check(primary.exists() && Files.readString(primary.toPath()).contains("Base"), "backup recovery did not rewrite primary waypoints.json");
      store.init("example.com", new AutismWaypoints.Waypoint("Later", 0, 0, 0, 0, 5L));
      await(primary, "Later");
      store.init("example.com", new AutismWaypoints.Waypoint("Latest", 0, 0, 0, 0, 6L));
      await(primary, "Latest");
      await(bak, "Later");
      check(Files.readString(bak.toPath()).contains("Later") && !Files.readString(bak.toPath()).contains("Latest"), "backup is not the previous save");
      System.out.println("PASS WaypointStoreCheck: corrupt file kept, backup restored, previous save kept as backup");
      System.exit(0);
   }

   private static void await(File file, String text) throws Exception {
      long deadline = System.currentTimeMillis() + 5000L;
      while (!(file.exists() && Files.readString(file.toPath()).contains(text)) && System.currentTimeMillis() < deadline) {
         Thread.sleep(100L);
      }
   }

   private static void check(boolean ok, String message) {
      if (!ok) {
         System.out.println("FAIL WaypointStoreCheck: " + message);
         System.exit(1);
      }
   }
}
