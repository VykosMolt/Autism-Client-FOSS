package autismclient.util;

import autismclient.api.hud.HudElementProvider;
import autismclient.api.hud.HudElements;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;

public class HudProviderGuardCheck {
   // AutismClientAddon.LOG needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("hudcheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }
      System.setProperty("check.config", Files.createTempDirectory("hudcheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }
      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(HudProviderGuardCheck.class.getName()).getMethod("run").invoke(null);
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL: " + what);
         System.exit(1);
      }
   }

   static Object call(String name, String id) throws Exception {
      Method m = al.class.getDeclaredMethod(name, String.class);
      m.setAccessible(true);
      return m.invoke(null, id);
   }

   @SuppressWarnings("unchecked")
   public static void run() throws Exception {
      HudElementProvider broken = (HudElementProvider) java.lang.reflect.Proxy.newProxyInstance(
         HudProviderGuardCheck.class.getClassLoader(), new Class<?>[]{HudElementProvider.class}, (p, m, a) -> {
            if (m.getName().equals("id")) {
               return "check:broken";
            }
            throw new IllegalStateException("addon bug in " + m.getName());
         });
      Field providers = HudElements.class.getDeclaredField("PROVIDERS");
      providers.setAccessible(true);
      ((Map<String, HudElementProvider>) providers.get(null)).put("check:broken", broken);

      String id = "check:broken";
      check("check:broken".equals(call("init_Str_Str", id)), "label falls back to id");
      check("".equals(call("load_Str", id)), "description falls back to empty");
      check("TOP_LEFT".equals(call("read_Str", id)), "anchor falls back to TOP_LEFT");
      check(Arrays.equals(new int[]{0, 0}, (int[]) call("init_IArr_Str", id)), "position falls back to 0,0");
      check(Boolean.FALSE.equals(call("update_Z", id)), "enabled falls back to false");
      System.out.println("PASS HudProviderGuardCheck");
   }
}
