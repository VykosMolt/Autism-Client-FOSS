package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiJson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AiScaffold {
   static final int init_I = 4096;
   private static final LinkedHashMap<Long, Block> init_LinkedHashMap = new LinkedHashMap<Long, Block>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Long, Block> eldest) {
         return this.size() > 4096;
      }
   };
   private static final Set<AiScaffold.Inner_StreamUtil> init_Set = Collections.newSetFromMap(new IdentityHashMap<>());
   private static final Map<Path, LinkedHashMap<Long, Block>> init_Map = new LinkedHashMap<>();
   private static final Map<Path, CompletableFuture<Void>> load_Map = new LinkedHashMap<>();
   private static final Map<Path, Map<Long, Block>> read = new LinkedHashMap<>();
   private static final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(
      Thread.ofPlatform().daemon(true).name("AI scaffold journal").factory()
   );
   private static final LinkedHashMap<Long, AiScaffold.Inner_DataUtil> load_LinkedHashMap = new LinkedHashMap<>();

   public static AiScaffold.Inner_StreamUtil init_a_StreamUtil() {
      return new AiScaffold.Inner_StreamUtil();
   }

   public static AiScaffold.Inner_StreamUtil init(Collection<BlockPos> existing) {
      AiScaffold.Inner_StreamUtil scope = new AiScaffold.Inner_StreamUtil();

      for (BlockPos pos : existing) {
         Block block = init_Block(pos);
         if (block != null) {
            scope.init_LinkedHashMap.put(pos.asLong(), block);
         }
      }

      return scope;
   }

   public static AiScaffold.Inner_StreamUtil init(Path journal, Collection<BlockPos> existing) {
      Path key = journal.toAbsolutePath().normalize();
      LinkedHashMap<Long, Block> records = init_Map.get(key);
      if (records == null) {
         records = new LinkedHashMap<>();
         CompletableFuture<Void> pending = load_Map.get(key);
         if (pending == null || pending.isDone() && !pending.isCompletedExceptionally()) {
            if (Files.isRegularFile(key)) {
               try {
                  JsonArray rows = JsonParser.parseString(Files.readString(key)).getAsJsonArray();
                  if (rows.size() > 131072) {
                     throw new IllegalArgumentException("Scaffold journal exceeds the build limit");
                  }

                  for (JsonElement row : rows) {
                     JsonObject entry = row.getAsJsonObject();
                     Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(entry.get("block").getAsString())).orElse(null);
                     if (block != null) {
                        records.put(new BlockPos(entry.get("x").getAsInt(), entry.get("y").getAsInt(), entry.get("z").getAsInt()).asLong(), block);
                     }
                  }
               } catch (IOException | RuntimeException e) {
                  AutismClientAddon.LOG.warn("[AI] Could not restore scaffold ownership: {}", e.getMessage());
                  records.clear();
               }
            }
         } else {
            records.putAll(read.getOrDefault(key, Map.of()));
         }

         if (init_Map.size() >= 64) {
            for (Path cached : init_Map.keySet()) {
               if (!auditInUse(cached)) {
                  init_Map.remove(cached);
                  break;
               }
            }
         }

         init_Map.put(key, records);
      }

      for (BlockPos pos : existing) {
         Block block = init_Block(pos);
         if (block != null) {
            records.put(pos.asLong(), block);
         }
      }

      return new AiScaffold.Inner_StreamUtil(records, key);
   }

   private AiScaffold() {
   }

   private static boolean auditInUse(Path journal) {
      for (AiScaffold.Inner_StreamUtil scope : init_Set) {
         if (journal.equals(scope.init_Path)) {
            return true;
         }
      }

      return false;
   }

   public static void init(BlockPos cell, Block block) {
      init_LinkedHashMap.put(cell.asLong(), block);

      for (AiScaffold.Inner_StreamUtil scope : init_Set) {
         if (scope.init_LinkedHashMap.put(cell.asLong(), block) != block) {
            scope.init_J++;
         }
      }
   }

   public static void init_V(BlockPos cell) {
      Block was = init_LinkedHashMap.remove(cell.asLong());
      if (was != null) {
         ArrayList<AiScaffold.Inner_StreamUtil> holders = new ArrayList<>();

         for (AiScaffold.Inner_StreamUtil scope : init_Set) {
            if (scope.init_LinkedHashMap.containsKey(cell.asLong())) {
               holders.add(scope);
            }
         }

         if (load_LinkedHashMap.size() > 512) {
            load_LinkedHashMap.remove(load_LinkedHashMap.keySet().iterator().next());
         }

         load_LinkedHashMap.put(cell.asLong(), new AiScaffold.Inner_DataUtil(was, holders, System.nanoTime()));
      }

      Minecraft client = Minecraft.getInstance();
      ClientLevel level = client == null ? null : client.level;
      if (was != null && level != null && init(was, level.getBlockState(cell).getBlock())) {
         String caller = StackWalker.getInstance()
            .walk(
               frames -> frames.skip(1L)
                  .limit(3L)
                  .map(f -> f.getClassName().substring(f.getClassName().lastIndexOf(46) + 1) + ":" + f.getLineNumber())
                  .reduce((a, b) -> a + " < " + b)
                  .orElse("?")
            );
         AutismClientAddon.LOG.info("[AI] scaffold record dropped while standing at {} by {}", cell.toShortString(), caller);
      }

      for (AiScaffold.Inner_StreamUtil scope : init_Set) {
         if (scope.init_LinkedHashMap.remove(cell.asLong()) != null) {
            scope.init_J++;
         }
      }

      for (LinkedHashMap<Long, Block> job : init_Map.values()) {
         job.remove(cell.asLong());
      }
   }

   public static void init(BlockPos pos, BlockState state) {
      if (!load_LinkedHashMap.isEmpty()) {
         AiScaffold.Inner_DataUtil forgotten = load_LinkedHashMap.remove(pos.asLong());
         if (forgotten != null && System.nanoTime() - forgotten.nanos() <= 10000000000L && init(forgotten.block(), state.getBlock())) {
            init_LinkedHashMap.put(pos.asLong(), forgotten.block());

            for (AiScaffold.Inner_StreamUtil scope : forgotten.scopes()) {
               if (init_Set.contains(scope) && scope.init_LinkedHashMap.put(pos.asLong(), forgotten.block()) != forgotten.block()) {
                  scope.init_J++;
               }
            }

            AutismClientAddon.LOG.info("[AI] scaffold at {} came back (the server undid its break): recorded again", pos.toShortString());
         }
      }
   }

   public static Block init_Block(BlockPos cell) {
      return init_LinkedHashMap.get(cell.asLong());
   }

   public static boolean init_Z(BlockPos cell) {
      if (init_Block(cell) != null) {
         return true;
      }

      for (AiScaffold.Inner_StreamUtil scope : init_Set) {
         if (scope.init_LinkedHashMap.containsKey(cell.asLong())) {
            return true;
         }
      }

      return false;
   }

   public static boolean init(Block recorded, Block live) {
      return recorded != null && (recorded == live || init(recorded) && init(live));
   }

   private static boolean init(Block block) {
      return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM;
   }

   public static List<BlockPos> init(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      List<BlockPos> out = new ArrayList<>();

      for (long key : init_LinkedHashMap.keySet()) {
         BlockPos cell = BlockPos.of(key);
         if (cell.getX() >= minX && cell.getX() <= maxX && cell.getY() >= minY && cell.getY() <= maxY && cell.getZ() >= minZ && cell.getZ() <= maxZ) {
            out.add(cell);
         }
      }

      out.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
      return out;
   }

   static int init_I() {
      return init_LinkedHashMap.size();
   }

   public static void init_V() {
      init_LinkedHashMap.clear();
      load_LinkedHashMap.clear();
      init_Set.clear();
      init_Map.clear();
   }

   private record Inner_DataUtil(Block block, List<AiScaffold.Inner_StreamUtil> scopes, long nanos) {
   }

   public static final class Inner_StreamUtil implements AutoCloseable {
      private final LinkedHashMap<Long, Block> init_LinkedHashMap;
      private final Path init_Path;
      private long init_J;
      private long load = -1L;
      private CompletableFuture<Void> init_CompletableFuture = CompletableFuture.completedFuture(null);

      private Inner_StreamUtil() {
         this(new LinkedHashMap<>(), null);
      }

      private Inner_StreamUtil(LinkedHashMap<Long, Block> records, Path journal) {
         this.init_LinkedHashMap = records;
         this.init_Path = journal;
         AiScaffold.init_Set.add(this);
      }

      public List<BlockPos> init_List() {
         return this.init_LinkedHashMap.keySet().stream().map(BlockPos::of).toList();
      }

      public Block init(BlockPos pos) {
         return this.init_LinkedHashMap.get(pos.asLong());
      }

      public void init_V() {
         if (this.init_Path != null && (this.load != this.init_J || this.init_CompletableFuture.isCompletedExceptionally())) {
            LinkedHashMap<Long, Block> snapshot = new LinkedHashMap<>(this.init_LinkedHashMap);
            JsonArray rows = new JsonArray();
            snapshot.forEach(
               (cell, block) -> {
                  BlockPos pos = BlockPos.of(cell);
                  rows.add(
                     AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "block", BuiltInRegistries.BLOCK.getKey(block).toString())
                  );
               }
            );
            this.load = this.init_J;
            CompletableFuture<Void> previous = AiScaffold.load_Map.getOrDefault(this.init_Path, CompletableFuture.completedFuture(null));
            this.init_CompletableFuture = previous.handle((ignored, failure) -> null).thenRunAsync(() -> {
               try {
                  AiJson.writeAtomicJson(this.init_Path, rows);
               } catch (IOException e) {
                  AutismClientAddon.LOG.warn("[AI] Could not save scaffold ownership: {}", e.getMessage());
                  throw new CompletionException(e);
               }
            }, AiScaffold.init_ExecutorService);
            AiScaffold.load_Map.put(this.init_Path, this.init_CompletableFuture);
            AiScaffold.read.put(this.init_Path, snapshot);
            if (AiScaffold.load_Map.size() > 64) {
               Iterator<Entry<Path, CompletableFuture<Void>>> iterator = AiScaffold.load_Map.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Path, CompletableFuture<Void>> write = iterator.next();
                  if (!write.getKey().equals(this.init_Path) && write.getValue().isDone() && !AiScaffold.auditInUse(write.getKey())) {
                     AiScaffold.read.remove(write.getKey());
                     iterator.remove();
                  }
               }
            }
         }
      }

      CompletableFuture<Void> init_CompletableFuture() {
         return this.init_CompletableFuture;
      }

      @Override
      public void close() {
         this.init_V();
         if (this.init_Path != null) {
            // Nothing saves this scope after close, so wait for the write and retry it once if it failed.
            try {
               this.init_CompletableFuture.join();
            } catch (RuntimeException failed) {
               this.init_V();
               try {
                  this.init_CompletableFuture.join();
               } catch (RuntimeException again) {
                  AutismClientAddon.LOG.warn("[AI] Scaffold ownership for {} was not saved", this.init_Path);
               }
            }
         }

         AiScaffold.init_Set.remove(this);
      }
   }
}
