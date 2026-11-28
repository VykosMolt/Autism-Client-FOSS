package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiJson;
import autismclient.ai.AiWorldIdentity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class AiChunkMemory {
   public static final int init_I = 0;
   public static final int load_I = 1;
   public static final int read_I = 2;
   public static final int write_I = 3;
   static final int update = 128;
   private static final int flush = 4096;
   static final int sync = 256;
   private static final int close = 256;
   private static final long init_J = 20000000000L;
   private static final int open = 32;
   private static final int poll = 1094929713;
   private static final int push = 2;
   private static final long load_J = 64000000L;
   private static final Set<String> init_Set = Set.of(
      "ancient_debris",
      "chest",
      "trapped_chest",
      "ender_chest",
      "barrel",
      "spawner",
      "trial_spawner",
      "vault",
      "beacon",
      "end_portal",
      "end_portal_frame",
      "budding_amethyst",
      "obsidian",
      "crying_obsidian",
      "nether_portal",
      "crafting_table",
      "furnace",
      "blast_furnace",
      "smoker",
      "hay_block",
      "wheat",
      "carrots",
      "potatoes",
      "beetroots",
      "gold_block"
   );
   private final Object init_Obj = new Object();
   private final LinkedHashMap<Long, AiChunkMemory.ChunkRecord> init_LinkedHashMap = new LinkedHashMap<Long, AiChunkMemory.ChunkRecord>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Long, AiChunkMemory.ChunkRecord> eldest) {
         return this.size() > 4096;
      }
   };
   private final Set<Long> load_Set = new HashSet<>();
   private final Set<Long> read_Set = ConcurrentHashMap.newKeySet();
   private final Set<Long> write_Set = ConcurrentHashMap.newKeySet();
   private final Map<Long, Long> auditReadFailedAt = new ConcurrentHashMap<>();
   private final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "autism-ai-chunkmemory");
      thread.setDaemon(true);
      return thread;
   });
   private volatile Path init_Path;
   private Object load_Obj;
   private volatile long generation;
   private long write_J;
   private static final AiChunkMemory instance = new AiChunkMemory();
   private static volatile Path load_Path;

   private static long[] init_JArr(int height) {
      long[] observed = new long[height];
      Arrays.fill(observed, System.currentTimeMillis());
      return observed;
   }

   public static AiChunkMemory getInstance() {
      return instance;
   }

   public static void init(Path baseDir) {
      load_Path = baseDir;
   }

   public static void init(Minecraft mc) {
      instance.load_V_Minecraft(mc);
   }

   public static int init_I(int flags) {
      if ((flags & 104) != 0) {
         return 3;
      } else if ((flags & 4) != 0) {
         return 2;
      } else {
         return (flags & 2) != 0 ? 0 : 1;
      }
   }

   public static boolean init(String path) {
      return path != null && (path.endsWith("_ore") || init_Set.contains(path));
   }

   static int init_I_BArr_I(byte[] packed, int index) {
      return packed[index >> 2] >> ((index & 3) << 1) & 3;
   }

   private static void init_V_BArr(byte[] packed, int index, int cls) {
      packed[index >> 2] = (byte)(packed[index >> 2] | (byte)(cls << ((index & 3) << 1)));
   }

   private static byte[] init_BArr_SArr(short[] flags, int yLo, int height) {
      byte[] packed = new byte[height * 64];

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 0; y < height; y++) {
               int source = (x * 16 + z) * height + y;
               if ((flags[source] & 1) != 0) {
                  init_V_BArr(packed, y * 256 + z * 16 + x, init_I(flags[source]));
               }
            }
         }
      }

      return packed;
   }

   public void init(int cx, int cz, int yLo, int height, short[] flags, Map<Integer, String> notables) {
      if (height > 0 && height <= 128) {
         byte[] fresh = init_BArr_SArr(flags, yLo, height);
         Map<Integer, String> freshNotables = notables == null ? Map.of() : notables;
         long key = AiGridRevisions.chunkKey(cx, cz);
         synchronized (this.init_Obj) {
            AiChunkMemory.ChunkRecord old = this.init_LinkedHashMap.get(key);
            AiChunkMemory.ChunkRecord merged = old == null
               ? new AiChunkMemory.ChunkRecord(yLo, height, fresh, init(freshNotables))
               : init_StateCache_DataUti_StateCache_DataUti(old, yLo, height, fresh, freshNotables);
            this.init_LinkedHashMap.put(key, merged);
            this.load_Set.add(key);
         }
      }
   }

   private static Map<Integer, String> init(Map<Integer, String> notables) {
      if (notables.size() <= 256) {
         return Map.copyOf(notables);
      }

      Map<Integer, String> capped = new HashMap<>();

      for (Entry<Integer, String> entry : notables.entrySet()) {
         if (capped.size() >= 256) {
            break;
         }

         capped.put(entry.getKey(), entry.getValue());
      }

      return capped;
   }

   private static AiChunkMemory.ChunkRecord init_StateCache_DataUti_StateCache_DataUti(
      AiChunkMemory.ChunkRecord old, int freshYLo, int freshHeight, byte[] fresh, Map<Integer, String> freshNotables
   ) {
      int newLo = Math.min(old.yLo, freshYLo);
      int newHi = Math.max(old.yLo + old.load, freshYLo + freshHeight);
      if (newHi - newLo > 128) {
         newLo = freshYLo;
         newHi = freshYLo + freshHeight;
      }

      int newHeight = newHi - newLo;
      byte[] merged = new byte[newHeight * 64];
      long[] observed = new long[newHeight];

      for (int y = Math.max(old.yLo, newLo); y < Math.min(old.yLo + old.load, newHi); y++) {
         observed[y - newLo] = old.observed[y - old.yLo];

         for (int i = 0; i < 256; i++) {
            int cls = init_I_BArr_I(old.packed, (y - old.yLo) * 256 + i);
            if (cls != 0) {
               init_V_BArr(merged, (y - newLo) * 256 + i, cls);
            }
         }
      }

      for (int y = Math.max(freshYLo, newLo); y < Math.min(freshYLo + freshHeight, newHi); y++) {
         observed[y - newLo] = System.currentTimeMillis();
         System.arraycopy(fresh, (y - freshYLo) * 256 / 4, merged, (y - newLo) * 256 / 4, 64);
      }

      Map<Integer, String> notables = new HashMap<>();

      for (Entry<Integer, String> entry : old.notables.entrySet()) {
         int y = old.yLo + entry.getKey() / 256;
         if (y >= newLo && y < newHi && (y < freshYLo || y >= freshYLo + freshHeight)) {
            notables.put(entry.getKey() + (old.yLo - newLo) * 256, entry.getValue());
         }
      }

      for (Entry<Integer, String> entry : freshNotables.entrySet()) {
         int y = freshYLo + entry.getKey() / 256;
         if (y >= newLo && y < newHi && notables.size() < 256) {
            notables.put(entry.getKey() + (freshYLo - newLo) * 256, entry.getValue());
         }
      }

      return new AiChunkMemory.ChunkRecord(newLo, newHeight, merged, notables, observed);
   }

   public AiPathfinder.CellInfo probeCell(int x, int y, int z) {
      AiChunkMemory.ChunkRecord mem = this.init_StateCache_DataUti_I_I(x >> 4, z >> 4);
      if (mem == null) {
         return AiPathfinder.CellInfo.UNLOADED;
      }

      int ry = y - mem.yLo;
      if (ry >= 0 && ry < mem.load && mem.observed[ry] != 0L) {
         AiPathfinder.CellInfo var10000;
         switch (init_I_BArr_I(mem.packed, ry * 256 + (z & 15) * 16 + (x & 15))) {
            case 1:
               var10000 = AiPathfinder.CellInfo.CACHED_SOLID;
               break;
            case 2:
               var10000 = AiPathfinder.CellInfo.CACHED_WATER;
               break;
            case 3:
               var10000 = AiPathfinder.CellInfo.CACHED_HAZARD;
               break;
            default:
               var10000 = AiPathfinder.CellInfo.CACHED_AIR;
         }

         return var10000;
      } else {
         return AiPathfinder.CellInfo.UNLOADED;
      }
   }

   public AiPathfinder.LiveGridView.SectionSlice sectionSlice(int cx, int cz, int yLo, int height, int revision) {
      AiChunkMemory.ChunkRecord mem = this.init_StateCache_DataUti_I_I(cx, cz);
      if (mem == null) {
         return null;
      }

      short[] flags = new short[256 * height];
      byte[] top16 = new byte[256 * height];
      short[] digX4 = new short[256 * height];
      byte[] mult10 = new byte[256 * height];
      Arrays.fill(digX4, (short)-1);
      Arrays.fill(mult10, (byte)10);
      int lo = Math.max(yLo, mem.yLo);
      int hi = Math.min(yLo + height, mem.yLo + mem.load);

      for (int y = lo; y < hi; y++) {
         if (mem.observed[y - mem.yLo] != 0L) {
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  int cls = init_I_BArr_I(mem.packed, (y - mem.yLo) * 256 + z * 16 + x);
                  int i = (x * 16 + z) * height + (y - yLo);
                  int f = 2049;
                  switch (cls) {
                     case 1:
                        top16[i] = 16;
                        break;
                     case 2:
                        f |= 6;
                        mult10[i] = 0;
                        break;
                     case 3:
                        f |= 8;
                        break;
                     default:
                        f |= 2;
                  }

                  flags[i] = (short)f;
               }
            }
         }
      }

      return new AiPathfinder.LiveGridView.SectionSlice(flags, top16, digX4, mult10, revision);
   }

   private AiChunkMemory.ChunkRecord init_StateCache_DataUti_I_I(int cx, int cz) {
      long key = AiGridRevisions.chunkKey(cx, cz);
      AiChunkMemory.ChunkRecord mem;
      synchronized (this.init_Obj) {
         mem = this.init_LinkedHashMap.get(key);
      }

      if (mem == null) {
         this.init_V_I_I(cx, cz);
      }

      return mem;
   }

   public List<AiChunkMemory.RememberedBlock> findRemembered(BlockPos origin, int radius, Predicate<String> idMatch) {
      List<Entry<Long, AiChunkMemory.ChunkRecord>> snapshot;
      synchronized (this.init_Obj) {
         snapshot = new ArrayList<>(this.init_LinkedHashMap.entrySet());
      }

      double radiusSqr = (double)radius * radius;
      List<AiChunkMemory.RememberedBlock> out = new ArrayList<>();

      for (Entry<Long, AiChunkMemory.ChunkRecord> entry : snapshot) {
         AiChunkMemory.ChunkRecord mem = entry.getValue();
         if (!mem.notables.isEmpty()) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            int nearestX = Math.clamp(origin.getX(), cx << 4, (cx << 4) + 15);
            int nearestZ = Math.clamp(origin.getZ(), cz << 4, (cz << 4) + 15);
            double dx = nearestX - origin.getX();
            double dz = nearestZ - origin.getZ();
            if (!(dx * dx + dz * dz > radiusSqr)) {
               for (Entry<Integer, String> notable : mem.notables.entrySet()) {
                  if (idMatch.test(notable.getValue())) {
                     int index = notable.getKey();
                     BlockPos pos = new BlockPos((cx << 4) + (index & 15), mem.yLo + (index >> 8), (cz << 4) + (index >> 4 & 15));
                     double distanceSqr = pos.distSqr(origin);
                     if (distanceSqr <= radiusSqr) {
                        out.add(new AiChunkMemory.RememberedBlock(pos, Math.sqrt(distanceSqr), true, mem.observed[index >> 8]));
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   public static List<AiChunkMemory.RememberedBlock> merge(List<AiChunkMemory.RememberedBlock> live, List<AiChunkMemory.RememberedBlock> cached, int limit) {
      Map<Long, AiChunkMemory.RememberedBlock> byCell = new LinkedHashMap<>();

      for (AiChunkMemory.RememberedBlock hit : live) {
         byCell.putIfAbsent(hit.pos().asLong(), hit);
      }

      for (AiChunkMemory.RememberedBlock hit : cached) {
         byCell.putIfAbsent(hit.pos().asLong(), hit);
      }

      List<AiChunkMemory.RememberedBlock> merged = new ArrayList<>(byCell.values());
      merged.sort(Comparator.comparingDouble(AiChunkMemory.RememberedBlock::distance));
      return merged.size() <= limit ? merged : merged.subList(0, limit);
   }

   private void load_V_Minecraft(Minecraft mc) {
      Object level = mc.level;
      if (level != this.load_Obj) {
         this.init_V();
         this.generation++;
         synchronized (this.init_Obj) {
            this.init_LinkedHashMap.clear();
            this.load_Set.clear();
         }

         this.read_Set.clear();
         this.write_Set.clear();
         this.auditReadFailedAt.clear();
         this.load_Obj = level;
         Path base = load_Path;
         this.init_Path = level != null && mc.player != null && base != null ? base.resolve(AiJson.sha256Hex(AiWorldIdentity.currentWorldKey(mc) + "/dimension/" + AiWorldIdentity.currentDimensionId(mc))) : null;
      }

      if (this.init_Path != null && System.nanoTime() - this.write_J >= 20000000000L) {
         this.write_J = System.nanoTime();
         this.init_V();
      }
   }

   private void init_V() {
      Path dir = this.init_Path;
      long generation = this.generation;
      Map<Long, AiChunkMemory.ChunkRecord> saving;
      synchronized (this.init_Obj) {
         if (this.load_Set.isEmpty()) {
            return;
         }

         saving = new HashMap<>();

         for (Long key : this.load_Set) {
            AiChunkMemory.ChunkRecord mem = this.init_LinkedHashMap.get(key);
            if (mem != null) {
               saving.put(key, mem);
            }
         }

         this.load_Set.clear();
      }

      if (dir != null) {
         this.init_ExecutorService.execute(() -> this.init_V_Path(dir, saving, generation));
      }
   }

   private void init_V_Path(Path dir, Map<Long, AiChunkMemory.ChunkRecord> saving, long generation) {
      try {
         Map<Long, Map<Long, AiChunkMemory.ChunkRecord>> byRegion = new HashMap<>();

         for (Entry<Long, AiChunkMemory.ChunkRecord> entry : saving.entrySet()) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            byRegion.computeIfAbsent(AiGridRevisions.chunkKey(cx >> 5, cz >> 5), k -> new HashMap<>()).put(entry.getKey(), entry.getValue());
         }

         Files.createDirectories(dir);

         for (Entry<Long, Map<Long, AiChunkMemory.ChunkRecord>> region : byRegion.entrySet()) {
            int rx = (int)(region.getKey() >> 32);
            int rz = (int)region.getKey().longValue();
            Map<Long, AiChunkMemory.ChunkRecord> merged = new HashMap<>();
            Path file = init_Path_Path(dir, rx, rz);
            if (Files.exists(file)) {
               try {
                  merged.putAll(init(Files.readAllBytes(file)).chunks());
               } catch (Exception corrupt) {
                  Files.move(file, dir.resolve(file.getFileName() + ".corrupt-" + System.currentTimeMillis()), StandardCopyOption.REPLACE_EXISTING);
               }
            }

            merged.putAll(region.getValue());
            Path temp = dir.resolve(file.getFileName() + ".tmp");
            Files.write(temp, init_BArr_I(rx, rz, merged));
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         }

         load_V_Path(dir);
      } catch (Exception failure) {
         synchronized (this.init_Obj) {
            if (generation == this.generation) {
               this.load_Set.addAll(saving.keySet());
            }
         }

         AutismClientAddon.LOG.warn("[AI] chunk memory save to {} failed; will retry", dir, failure);
      }
   }

   private void init_V_I_I(int cx, int cz) {
      Path dir = this.init_Path;
      long generation = this.generation;
      if (dir != null) {
         long regionKey = AiGridRevisions.chunkKey(cx >> 5, cz >> 5);
         Long failedAt = this.auditReadFailedAt.get(regionKey);
         if (failedAt != null && System.nanoTime() - failedAt < 30000000000L) {
            return;
         }

         if (!this.read_Set.contains(regionKey) && this.write_Set.add(regionKey)) {
            this.init_ExecutorService.execute(() -> {
               boolean loaded = false;
               try {
                  Path file = init_Path_Path(dir, cx >> 5, cz >> 5);
                  if (Files.isRegularFile(file) && Files.size(file) <= 64000000L) {
                     byte[] bytes = Files.readAllBytes(file);
                     AiChunkMemory.RegionFile region = null;
                     try {
                        region = init(bytes);
                     } catch (Exception corrupt) {
                        Files.move(file, dir.resolve(file.getFileName() + ".corrupt-" + System.currentTimeMillis()), StandardCopyOption.REPLACE_EXISTING);
                        load_V_Path(dir);
                     }

                     if (region != null) {
                        synchronized (this.init_Obj) {
                           if (generation == this.generation && dir.equals(this.init_Path)) {
                              region.chunks().forEach(this.init_LinkedHashMap::putIfAbsent);
                           }
                        }
                     }
                  }

                  loaded = true;
               } catch (Exception var17) {
               } finally {
                  if (generation == this.generation && dir.equals(this.init_Path)) {
                     this.write_Set.remove(regionKey);
                     if (loaded) {
                        this.read_Set.add(regionKey);
                     } else {
                        this.auditReadFailedAt.put(regionKey, System.nanoTime());
                     }
                  }
               }
            });
         }
      }
   }

   private static void load_V_Path(Path dir) throws IOException {
      try (Stream<Path> files = Files.list(dir)) {
         List<Path> sorted = files.sorted(Comparator.comparingLong(f -> f.toFile().lastModified())).toList();
         List<Path> regions = sorted.stream().filter(f -> f.getFileName().toString().endsWith(".bin.gz")).toList();
         List<Path> corrupt = sorted.stream().filter(f -> f.getFileName().toString().contains(".bin.gz.corrupt-")).toList();

         for (int i = 0; i < regions.size() - 256; i++) {
            Files.deleteIfExists(regions.get(i));
         }

         for (int i = 0; i < corrupt.size() - 4; i++) {
            Files.deleteIfExists(corrupt.get(i));
         }
      }
   }

   private static Path init_Path_Path(Path dir, int rx, int rz) {
      return dir.resolve("r." + rx + "." + rz + ".bin.gz");
   }

   static byte[] init_BArr_I(int regionX, int regionZ, Map<Long, AiChunkMemory.ChunkRecord> chunks) throws IOException {
      Map<String, Integer> palette = new LinkedHashMap<>();

      for (AiChunkMemory.ChunkRecord mem : chunks.values()) {
         for (String id : mem.notables.values()) {
            palette.putIfAbsent(id, palette.size());
         }
      }

      ByteArrayOutputStream raw = new ByteArrayOutputStream();

      try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(raw))) {
         out.writeInt(1094929713);
         out.writeInt(2);
         out.writeInt(regionX);
         out.writeInt(regionZ);
         out.writeInt(palette.size());

         for (String id : palette.keySet()) {
            out.writeUTF(id);
         }

         out.writeInt(chunks.size());

         for (Entry<Long, AiChunkMemory.ChunkRecord> entry : chunks.entrySet()) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            AiChunkMemory.ChunkRecord mem = entry.getValue();
            out.writeByte(cx - regionX * 32);
            out.writeByte(cz - regionZ * 32);
            out.writeShort(mem.yLo);
            out.writeShort(mem.load);
            out.write(mem.packed);

            for (long observed : mem.observed) {
               out.writeLong(observed);
            }

            out.writeShort(mem.notables.size());

            for (Entry<Integer, String> notable : mem.notables.entrySet()) {
               out.writeShort(notable.getKey());
               out.writeShort(palette.get(notable.getValue()));
            }
         }
      }

      return raw.toByteArray();
   }

   static AiChunkMemory.RegionFile init(byte[] data) throws IOException {
      try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))) {
         if (in.readInt() != 1094929713 || in.readInt() != 2) {
            throw new IOException("Unsupported chunk-memory region");
         }

         int regionX = in.readInt();
         int regionZ = in.readInt();
         int paletteSize = in.readInt();
         if (paletteSize < 0 || paletteSize > 65536) {
            throw new IOException("Bad chunk-memory palette");
         }

         List<String> palette = new ArrayList<>(paletteSize);

         for (int i = 0; i < paletteSize; i++) {
            palette.add(in.readUTF());
         }

         int chunkCount = in.readInt();
         if (chunkCount < 0 || chunkCount > 1024) {
            throw new IOException("Bad chunk count");
         }

         Map<Long, AiChunkMemory.ChunkRecord> chunks = new HashMap<>();

         for (int c = 0; c < chunkCount; c++) {
            int cx = regionX * 32 + in.readUnsignedByte();
            int cz = regionZ * 32 + in.readUnsignedByte();
            int yLo = in.readShort();
            int height = in.readShort();
            if (height <= 0 || height > 128) {
               throw new IOException("Bad band height");
            }

            byte[] packed = new byte[height * 64];
            in.readFully(packed);
            long[] observed = new long[height];

            for (int y = 0; y < height; y++) {
               observed[y] = in.readLong();
               if (observed[y] < 0L) {
                  throw new IOException("Bad coverage timestamp");
               }
            }

            int notableCount = in.readShort();
            if (notableCount < 0 || notableCount > 256) {
               throw new IOException("Bad notable count");
            }

            Map<Integer, String> notables = new HashMap<>();

            for (int n = 0; n < notableCount; n++) {
               int index = in.readShort() & '\uffff';
               int paletteIndex = in.readShort() & '\uffff';
               if (index >= height * 256 || paletteIndex >= paletteSize) {
                  throw new IOException("Bad notable entry");
               }

               notables.put(index, palette.get(paletteIndex));
            }

            chunks.put(AiGridRevisions.chunkKey(cx, cz), new AiChunkMemory.ChunkRecord(yLo, height, packed, notables, observed));
         }

         return new AiChunkMemory.RegionFile(regionX, regionZ, chunks);
      }
   }

   public record RememberedBlock(BlockPos pos, double distance, boolean cached, long observedAtMillis) {
      public RememberedBlock(BlockPos pos, double distance, boolean cached) {
         this(pos, distance, cached, cached ? 0L : System.currentTimeMillis());
      }
   }

   static final class ChunkRecord {
      final int yLo;
      final int load;
      final byte[] packed;
      final Map<Integer, String> notables;
      final long[] observed;

      ChunkRecord(int yLo, int height, byte[] packed, Map<Integer, String> notables) {
         this(yLo, height, packed, notables, AiChunkMemory.init_JArr(height));
      }

      ChunkRecord(int yLo, int height, byte[] packed, Map<Integer, String> notables, long[] observed) {
         this.yLo = yLo;
         this.load = height;
         this.packed = packed;
         this.notables = notables;
         this.observed = observed;
      }
   }

   record RegionFile(int regionX, int regionZ, Map<Long, AiChunkMemory.ChunkRecord> chunks) {
   }
}
