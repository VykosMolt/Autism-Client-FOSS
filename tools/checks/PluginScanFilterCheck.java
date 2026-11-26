package autismclient.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.ToolProvider;

public class PluginScanFilterCheck {
   // AutismClientAddon.LOG needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("scancheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }
      System.setProperty("check.config", Files.createTempDirectory("scancheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }
      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(PluginScanFilterCheck.class.getName()).getMethod("run").invoke(null);
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL: " + what);
         System.exit(1);
      }
   }

   static Object state(String query, String kind) throws Exception {
      Class<?> stateClass = Class.forName("autismclient.util.AutismServerInfoOverlay$Inner_RenderState");
      Class<?> kindClass = Class.forName("autismclient.util.AutismServerInfoOverlay$Inner_SyncManager");
      Constructor<?> ctor = stateClass.getDeclaredConstructor(String.class, kindClass, String.class);
      ctor.setAccessible(true);
      Field kindField = kindClass.getDeclaredField(kind);
      kindField.setAccessible(true);
      return ctor.newInstance(query, kindField.get(null), null);
   }

   public static void run() throws Exception {
      Class<?> overlay = AutismServerInfoOverlay.class;
      Method observable = overlay.getDeclaredMethod("auditIsObservableCommandCompletion", String.class);
      observable.setAccessible(true);
      for (String yes : new String[]{"/", "/pl", "/ver x", "/version ", "/bukkit:ver ", "/essentials:about ", "/help ", "/? w", "/bukkit:help "}) {
         check((Boolean) observable.invoke(null, yes), "observable: " + yes);
      }
      for (String no : new String[]{"/msg bob", "/tp ", "/w x", "hello"}) {
         check(!(Boolean) observable.invoke(null, no), "not observable: " + no);
      }

      Class<?> stateClass = Class.forName("autismclient.util.AutismServerInfoOverlay$Inner_RenderState");
      Method rootProbe = overlay.getDeclaredMethod("auditIsRootProbe", stateClass);
      rootProbe.setAccessible(true);
      check((Boolean) rootProbe.invoke(null, state("/", "USER")), "/ is a root probe");
      check((Boolean) rootProbe.invoke(null, state("/a", "USER")), "/a is a root probe");
      check((Boolean) rootProbe.invoke(null, state("/ ", "ROOT")), "scanner root kind is a root probe");
      check(!(Boolean) rootProbe.invoke(null, state("/ab", "USER")), "/ab is not a root probe");
      check(!(Boolean) rootProbe.invoke(null, (Object) null), "null is not a root probe");

      Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      Object instance = ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(overlay);
      Method batch = overlay.getDeclaredMethod("auditIsPlayerListBatch", List.class, Set.class, boolean.class, boolean.class);
      batch.setAccessible(true);
      List<String> commands = new ArrayList<>();
      for (int i = 0; i < 130; i++) {
         commands.add("zzcmd" + i);
      }
      check(!(Boolean) batch.invoke(instance, commands, Set.of(), false, true), "large root probe is kept");
      check((Boolean) batch.invoke(instance, commands, Set.of(), false, false), "large non-root batch still dropped");
      check((Boolean) batch.invoke(instance, List.of("alice", "bob", "carol"), Set.of("alice", "bob", "carol"), false, true), "player batch still detected");

      // Through the batch loop: a dropped batch still keeps its namespaced tokens.
      AutismServerInfoOverlay live = new AutismServerInfoOverlay(null);
      setField(live, "tick_Z", true);
      Class<?> replyClass = Class.forName("autismclient.util.AutismServerInfoOverlay$Inner_BufferHelper");
      Constructor<?> replyCtor = replyClass.getDeclaredConstructor(long.class, boolean.class, stateClass, stateClass, List.class);
      replyCtor.setAccessible(true);
      List<String> tokens = new ArrayList<>(commands);
      tokens.add("zzns:zzkept");
      @SuppressWarnings("unchecked")
      List<Object> pending = (List<Object>) getField(live, "write_List");
      pending.add(replyCtor.newInstance(0L, true, state("/ab", "USER"), null, tokens));
      Method drain = overlay.getDeclaredMethod("close", boolean.class);
      drain.setAccessible(true);
      drain.invoke(live, true);
      Map<?, ?> kept = (Map<?, ?>) getField(live, "poll_Map");
      check(kept.containsKey("zzns"), "namespaced token survives a dropped batch: " + kept.keySet());
      System.out.println("PASS PluginScanFilterCheck");
   }

   static Object getField(Object o, String name) throws Exception {
      Field f = o.getClass().getDeclaredField(name);
      f.setAccessible(true);
      return f.get(o);
   }

   static void setField(Object o, String name, Object value) throws Exception {
      Field f = o.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(o, value);
   }
}
