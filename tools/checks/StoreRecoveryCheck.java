package autismclient.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.ToolProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

public class StoreRecoveryCheck {
   static final class Store extends df<String> {
      final File file;

      Store(File file) {
         this.file = file;
      }

      protected File init_File() {
         return this.file;
      }

      protected String init_Str() {
         return "items";
      }

      protected String init_Obj_CompoundTag(CompoundTag tag) {
         return tag.getStringOr("v", "");
      }

      protected CompoundTag init_CompoundTag_Obj(String item) {
         CompoundTag tag = new CompoundTag();
         tag.putString("v", item);
         return tag;
      }

      protected String load_Str() {
         return "check store";
      }
   }

   static void write(File file, String... items) throws Exception {
      ListTag list = new ListTag();
      for (String item : items) {
         CompoundTag tag = new CompoundTag();
         tag.putString("v", item);
         list.add(tag);
      }
      CompoundTag root = new CompoundTag();
      root.put("items", list);
      NbtIo.write(root, file.toPath());
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL: " + what);
         System.exit(1);
      }
   }

   static File[] corrupt(File dir) {
      return dir.listFiles((d, n) -> n.startsWith("s.nbt.corrupt-"));
   }

   // AutismClientAddon.LOG needs FabricLoader: rerun with a stub whose config dir is a temp dir.
   public static void main(String[] args) throws Exception {
      File stub = Files.createTempDirectory("dfcheck-stub").toFile();
      File src = new File(stub, "FabricLoader.java");
      Files.writeString(src.toPath(), "package net.fabricmc.loader.api; public interface FabricLoader { java.nio.file.Path getConfigDir();"
         + " static FabricLoader getInstance() { return () -> java.nio.file.Path.of(System.getProperty(\"check.config\")); } }");
      if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", stub.getPath(), src.getPath()) != 0) {
         throw new IllegalStateException("stub compile failed");
      }
      System.setProperty("check.config", Files.createTempDirectory("dfcheck-config").toString());
      List<URL> urls = new ArrayList<>();
      urls.add(stub.toURI().toURL());
      for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
         urls.add(new File(entry).toURI().toURL());
      }
      URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
      loader.loadClass(StoreRecoveryCheck.class.getName()).getMethod("run").invoke(null);
   }

   public static void run() throws Exception {
      byte[] garbage = "not nbt at all".getBytes();

      File dir1 = Files.createTempDirectory("dfcheck").toFile();
      File f1 = new File(dir1, "s.nbt");
      Files.write(f1.toPath(), garbage);
      write(new File(dir1, "s.nbt.bak"), "a", "b");
      Store s1 = new Store(f1);
      s1.load_V();
      check(s1.init_List().equals(Arrays.asList("a", "b")), "corrupt primary + good bak loads bak, got " + s1.init_List());
      check(corrupt(dir1).length == 1 && Arrays.equals(Files.readAllBytes(corrupt(dir1)[0].toPath()), garbage), "corrupt primary kept aside");
      dj.init(5000L);
      check(f1.exists(), "bak recovery writes primary back to disk");
      Store s1Second = new Store(f1);
      s1Second.load_V();
      check(s1Second.init_List().equals(Arrays.asList("a", "b")), "second launch keeps recovered data, got " + s1Second.init_List());

      File dirMissing = Files.createTempDirectory("dfcheck").toFile();
      File fMissing = new File(dirMissing, "s.nbt");
      write(new File(dirMissing, "s.nbt.bak"), "m1", "m2");
      Store sMissing = new Store(fMissing);
      sMissing.load_V();
      check(sMissing.init_List().equals(Arrays.asList("m1", "m2")), "missing primary + good bak loads bak, got " + sMissing.init_List());
      dj.init(5000L);
      check(fMissing.exists(), "missing primary recovery recreates primary file");

      File dir2 = Files.createTempDirectory("dfcheck").toFile();
      File f2 = new File(dir2, "s.nbt");
      File bak2 = new File(dir2, "s.nbt.bak");
      Files.write(f2.toPath(), garbage);
      Files.write(bak2.toPath(), garbage);
      Store s2 = new Store(f2);
      s2.load_V();
      check(s2.close_I() == 0, "both corrupt loads empty");
      s2.init_List.add("new");
      s2.read_V();
      dj.init(5000L);
      check(corrupt(dir2).length == 1 && Arrays.equals(Files.readAllBytes(corrupt(dir2)[0].toPath()), garbage), "save kept the corrupt original");
      check(Arrays.equals(Files.readAllBytes(bak2.toPath()), garbage), "save did not clobber the bak");
      Store again = new Store(f2);
      again.load_V();
      check(again.init_List().equals(Arrays.asList("new")), "new save readable");

      File dir3 = Files.createTempDirectory("dfcheck").toFile();
      File f3 = new File(dir3, "s.nbt");
      File bak3 = new File(dir3, "s.nbt.bak");
      write(f3, "x");
      Store s3 = new Store(f3);
      s3.load_V();
      Files.write(f3.toPath(), garbage);
      s3.read_V();
      dj.init(5000L);
      check(!bak3.exists(), "unparseable primary is not copied to bak");
      write(f3, "x");
      s3.read_V();
      dj.init(5000L);
      Store b3 = new Store(bak3);
      b3.load_V();
      check(b3.init_List().equals(Arrays.asList("x")), "parseable primary is copied to bak");

      System.out.println("PASS StoreRecoveryCheck");
   }
}
