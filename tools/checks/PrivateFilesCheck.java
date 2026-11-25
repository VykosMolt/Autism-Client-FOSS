package autismclient.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import javax.tools.ToolProvider;

public class PrivateFilesCheck {
   // AutismClientAddon.FOLDER needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("privatecheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }

      System.setProperty("check.config", Files.createTempDirectory("privatecheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }

      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(PrivateFilesCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      File folder = new File(System.getProperty("check.config"), "autism");
      folder.mkdirs();
      String json = "{\"modules\":{\"auto-login\":{\"enabled\":true,\"settings\":{\"password\":\"S3CRET\"}}}}";
      AutismConfig.writeToDisk(json);
      AutismConfig.writeToDisk(json);
      expectPrivate(new File(folder, "config.json"));
      expectPrivate(new File(folder, "config.json.bak"));
      System.out.println("PASS PrivateFilesCheck: config.json and config.json.bak are 0600");
   }

   private static void expectPrivate(File f) throws Exception {
      String mode = f.exists() ? PosixFilePermissions.toString(Files.getPosixFilePermissions(f.toPath())) : "missing";
      if (!mode.equals("rw-------")) {
         System.out.println("FAIL PrivateFilesCheck: " + f.getName() + " is " + mode);
         System.exit(1);
      }
   }
}
