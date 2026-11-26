package autismclient.ai.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ChunkMemoryCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL ChunkMemoryCheck: " + what);
         System.exit(1);
      }
   }

   static Object field(Object o, String name) throws Exception {
      Field f = AiChunkMemory.class.getDeclaredField(name);
      f.setAccessible(true);
      return f.get(o);
   }

   static void set(Object o, String name, Object value) throws Exception {
      Field f = AiChunkMemory.class.getDeclaredField(name);
      f.setAccessible(true);
      f.set(o, value);
   }

   // Runs queued IO jobs by waiting for a no-op behind them on the single worker.
   static void drain(AiChunkMemory m) throws Exception {
      ((ExecutorService) field(m, "init_ExecutorService")).submit(() -> {}).get(10, TimeUnit.SECONDS);
   }

   @SuppressWarnings("unchecked")
   public static void main(String[] args) throws Exception {
      Method save = AiChunkMemory.class.getDeclaredMethod("init_V");
      save.setAccessible(true);
      Path tmp = Files.createTempDirectory("chunkmem");
      short[] flags = new short[256 * 4];
      flags[0] = 1;
      AiChunkMemory m = AiChunkMemory.init_StateCache();
      Set<Long> dirty = (Set<Long>) field(m, "load_Set");
      Set<Long> read = (Set<Long>) field(m, "read_Set");
      Map<Long, ?> memory = (Map<Long, ?>) field(m, "init_LinkedHashMap");

      // (a) a failed write puts the chunk back in the dirty set.
      Path blocked = tmp.resolve("blocked");
      Files.writeString(blocked, "not a directory");
      set(m, "init_Path", blocked.resolve("dim"));
      m.init(0, 0, 60, 4, flags, Map.of());
      save.invoke(m);
      drain(m);
      check(dirty.contains(AiGridRevisions.init_J(0, 0)), "failed save must re-mark the chunk dirty");

      // (d) a written region is not marked read, so the file's other chunks still get loaded.
      Path dir = tmp.resolve("ok");
      set(m, "init_Path", dir);
      save.invoke(m);
      drain(m);
      check(Files.isRegularFile(dir.resolve("r.0.0.bin.gz")) && dirty.isEmpty(), "save after the failure writes the region");
      check(!read.contains(AiGridRevisions.init_J(0, 0)), "written region must not be marked read");
      synchronized (field(m, "init_Obj")) {
         memory.clear();
      }
      check(m.init_ClassCache_FileCac_I(0, 60, 0) == AiPathfinder.CellInfo.UNLOADED, "first query misses");
      drain(m);
      check(m.init_ClassCache_FileCac_I(0, 60, 0) == AiPathfinder.CellInfo.CACHED_SOLID, "region file is read back on a miss");
      check(read.contains(AiGridRevisions.init_J(0, 0)), "successful read is recorded");

      // (b) a corrupt region is moved aside and marked read, so it is read once.
      Files.writeString(dir.resolve("r.1.0.bin.gz"), "garbage");
      m.init_ClassCache_FileCac_I(32 * 16, 60, 0);
      drain(m);
      check(!Files.exists(dir.resolve("r.1.0.bin.gz")), "corrupt region file must be moved aside");
      try (var files = Files.list(dir)) {
         check(files.anyMatch(f -> f.getFileName().toString().startsWith("r.1.0.bin.gz.corrupt-")), "corrupt region kept as .corrupt-*");
      }
      check(read.contains(AiGridRevisions.init_J(1, 0)), "corrupt region is marked read");

      // An unreadable region is not marked read and is not retried within 30 s.
      Path locked = dir.resolve("r.2.0.bin.gz");
      Files.writeString(locked, "x");
      check(locked.toFile().setReadable(false) && !Files.isReadable(locked), "cannot make a file unreadable here (running as root?)");
      m.init_ClassCache_FileCac_I(64 * 16, 60, 0);
      drain(m);
      check(!read.contains(AiGridRevisions.init_J(2, 0)), "IO failure must not be marked read");
      locked.toFile().setReadable(true);
      Files.write(locked, Files.readAllBytes(dir.resolve("r.0.0.bin.gz")));
      m.init_ClassCache_FileCac_I(64 * 16, 60, 0);
      drain(m);
      check(!read.contains(AiGridRevisions.init_J(2, 0)), "IO failure is not retried within 30 s");

      // (c) old .corrupt-* files are pruned down to four.
      for (int i = 0; i < 7; i++) {
         Path c = dir.resolve("r.9." + i + ".bin.gz.corrupt-" + i);
         Files.writeString(c, "x");
         c.toFile().setLastModified(1_000_000L + i * 1000L);
      }
      m.init(0, 0, 60, 4, flags, Map.of());
      save.invoke(m);
      drain(m);
      long left;
      try (var files = Files.list(dir)) {
         left = files.filter(f -> f.getFileName().toString().contains(".corrupt-")).count();
      }
      check(left == 4, "corrupt files kept: " + left);
      check(Files.exists(dir.resolve("r.9.6.bin.gz.corrupt-6")) && !Files.exists(dir.resolve("r.9.0.bin.gz.corrupt-0")), "newest corrupt files are the ones kept");
      System.out.println("PASS ChunkMemoryCheck");
   }
}
