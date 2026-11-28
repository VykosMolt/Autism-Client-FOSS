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
   private static final LinkedHashMap<Long, Block> placedBlocks = new LinkedHashMap<Long, Block>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<Long, Block> eldest) {
         return this.size() > 4096;
      }
   };
   private static final Set<AiScaffold.JobScope> openScopes = Collections.newSetFromMap(new IdentityHashMap<>());
   private static final Map<Path, LinkedHashMap<Long, Block>> journalRecords = new LinkedHashMap<>();
   private static final Map<Path, CompletableFuture<Void>> pendingSaves = new LinkedHashMap<>();
   private static final Map<Path, Map<Long, Block>> recordedBlocks = new LinkedHashMap<>();
   private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(
      Thread.ofPlatform().daemon(true).name("AI scaffold journal").factory()
   );
   private static final LinkedHashMap<Long, AiScaffold.ScaffoldRecord> forgottenRecords = new LinkedHashMap<>();

   public static AiScaffold.JobScope openJobScope() {
      return new AiScaffold.JobScope();
   }

   public static AiScaffold.JobScope init(Collection<BlockPos> existing) {
      AiScaffold.JobScope scope = new AiScaffold.JobScope();

      for (BlockPos pos : existing) {
         Block block = recordedBlock(pos);
         if (block != null) {
            scope.ownedBlocks.put(pos.asLong(), block);
         }
      }

      return scope;
   }

   public static AiScaffold.JobScope openScope(Path journal, Collection<BlockPos> existing) {
      Path key = journal.toAbsolutePath().normalize();
      LinkedHashMap<Long, Block> records = journalRecords.get(key);
      if (records == null) {
         records = new LinkedHashMap<>();
         CompletableFuture<Void> pending = pendingSaves.get(key);
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
            records.putAll(recordedBlocks.getOrDefault(key, Map.of()));
         }

         if (journalRecords.size() >= 64) {
            for (Path cached : journalRecords.keySet()) {
               if (!auditInUse(cached)) {
                  journalRecords.remove(cached);
                  break;
               }
            }
         }

         journalRecords.put(key, records);
      }

      for (BlockPos pos : existing) {
         Block block = recordedBlock(pos);
         if (block != null) {
            records.put(pos.asLong(), block);
         }
      }

      return new AiScaffold.JobScope(records, key);
   }

   private AiScaffold() {
   }

   private static boolean auditInUse(Path journal) {
      for (AiScaffold.JobScope scope : openScopes) {
         if (journal.equals(scope.journal)) {
            return true;
         }
      }

      return false;
   }

   public static void recordPlaced(BlockPos cell, Block block) {
      placedBlocks.put(cell.asLong(), block);

      for (AiScaffold.JobScope scope : openScopes) {
         if (scope.ownedBlocks.put(cell.asLong(), block) != block) {
            scope.revision++;
         }
      }
   }

   public static void forget(BlockPos cell) {
      Block was = placedBlocks.remove(cell.asLong());
      if (was != null) {
         ArrayList<AiScaffold.JobScope> holders = new ArrayList<>();

         for (AiScaffold.JobScope scope : openScopes) {
            if (scope.ownedBlocks.containsKey(cell.asLong())) {
               holders.add(scope);
            }
         }

         if (forgottenRecords.size() > 512) {
            forgottenRecords.remove(forgottenRecords.keySet().iterator().next());
         }

         forgottenRecords.put(cell.asLong(), new AiScaffold.ScaffoldRecord(was, holders, System.nanoTime()));
      }

      Minecraft client = Minecraft.getInstance();
      ClientLevel level = client == null ? null : client.level;
      if (was != null && level != null && matches(was, level.getBlockState(cell).getBlock())) {
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

      for (AiScaffold.JobScope scope : openScopes) {
         if (scope.ownedBlocks.remove(cell.asLong()) != null) {
            scope.revision++;
         }
      }

      for (LinkedHashMap<Long, Block> job : journalRecords.values()) {
         job.remove(cell.asLong());
      }
   }

   public static void onBlockChanged(BlockPos pos, BlockState state) {
      if (!forgottenRecords.isEmpty()) {
         AiScaffold.ScaffoldRecord forgotten = forgottenRecords.remove(pos.asLong());
         if (forgotten != null && System.nanoTime() - forgotten.nanos() <= 10000000000L && matches(forgotten.block(), state.getBlock())) {
            placedBlocks.put(pos.asLong(), forgotten.block());

            for (AiScaffold.JobScope scope : forgotten.scopes()) {
               if (openScopes.contains(scope) && scope.ownedBlocks.put(pos.asLong(), forgotten.block()) != forgotten.block()) {
                  scope.revision++;
               }
            }

            AutismClientAddon.LOG.info("[AI] scaffold at {} came back (the server undid its break): recorded again", pos.toShortString());
         }
      }
   }

   public static Block recordedBlock(BlockPos cell) {
      return placedBlocks.get(cell.asLong());
   }

   public static boolean isScaffold(BlockPos cell) {
      if (recordedBlock(cell) != null) {
         return true;
      }

      for (AiScaffold.JobScope scope : openScopes) {
         if (scope.ownedBlocks.containsKey(cell.asLong())) {
            return true;
         }
      }

      return false;
   }

   public static boolean matches(Block recorded, Block live) {
      return recorded != null && (recorded == live || isDirtLike(recorded) && isDirtLike(live));
   }

   private static boolean isDirtLike(Block block) {
      return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM;
   }

   public static List<BlockPos> recordedIn(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      List<BlockPos> out = new ArrayList<>();

      for (long key : placedBlocks.keySet()) {
         BlockPos cell = BlockPos.of(key);
         if (cell.getX() >= minX && cell.getX() <= maxX && cell.getY() >= minY && cell.getY() <= maxY && cell.getZ() >= minZ && cell.getZ() <= maxZ) {
            out.add(cell);
         }
      }

      out.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
      return out;
   }

   static int unusedPlacedCount() {
      return placedBlocks.size();
   }

   public static void reset() {
      placedBlocks.clear();
      forgottenRecords.clear();
      openScopes.clear();
      journalRecords.clear();
   }

   private record ScaffoldRecord(Block block, List<AiScaffold.JobScope> scopes, long nanos) {
   }

   public static final class JobScope implements AutoCloseable {
      private final LinkedHashMap<Long, Block> ownedBlocks;
      private final Path journal;
      private long revision;
      private long savedRevision = -1L;
      private CompletableFuture<Void> pendingSave = CompletableFuture.completedFuture(null);

      private JobScope() {
         this(new LinkedHashMap<>(), null);
      }

      private JobScope(LinkedHashMap<Long, Block> records, Path journal) {
         this.ownedBlocks = records;
         this.journal = journal;
         AiScaffold.openScopes.add(this);
      }

      public List<BlockPos> ownedCells() {
         return this.ownedBlocks.keySet().stream().map(BlockPos::of).toList();
      }

      public Block recorded(BlockPos pos) {
         return this.ownedBlocks.get(pos.asLong());
      }

      public void persist() {
         if (this.journal != null && (this.savedRevision != this.revision || this.pendingSave.isCompletedExceptionally())) {
            LinkedHashMap<Long, Block> snapshot = new LinkedHashMap<>(this.ownedBlocks);
            JsonArray rows = new JsonArray();
            snapshot.forEach(
               (cell, block) -> {
                  BlockPos pos = BlockPos.of(cell);
                  rows.add(
                     AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "block", BuiltInRegistries.BLOCK.getKey(block).toString())
                  );
               }
            );
            this.savedRevision = this.revision;
            CompletableFuture<Void> previous = AiScaffold.pendingSaves.getOrDefault(this.journal, CompletableFuture.completedFuture(null));
            this.pendingSave = previous.handle((ignored, failure) -> null).thenRunAsync(() -> {
               try {
                  AiJson.writeAtomicJson(this.journal, rows);
               } catch (IOException e) {
                  AutismClientAddon.LOG.warn("[AI] Could not save scaffold ownership: {}", e.getMessage());
                  throw new CompletionException(e);
               }
            }, AiScaffold.SAVE_EXECUTOR);
            AiScaffold.pendingSaves.put(this.journal, this.pendingSave);
            AiScaffold.recordedBlocks.put(this.journal, snapshot);
            if (AiScaffold.pendingSaves.size() > 64) {
               Iterator<Entry<Path, CompletableFuture<Void>>> iterator = AiScaffold.pendingSaves.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Path, CompletableFuture<Void>> write = iterator.next();
                  if (!write.getKey().equals(this.journal) && write.getValue().isDone() && !AiScaffold.auditInUse(write.getKey())) {
                     AiScaffold.recordedBlocks.remove(write.getKey());
                     iterator.remove();
                  }
               }
            }
         }
      }

      CompletableFuture<Void> unusedPendingSave() {
         return this.pendingSave;
      }

      @Override
      public void close() {
         this.persist();
         if (this.journal != null) {
            // Nothing saves this scope after close, so wait for the write and retry it once if it failed.
            try {
               this.pendingSave.join();
            } catch (RuntimeException failed) {
               this.persist();
               try {
                  this.pendingSave.join();
               } catch (RuntimeException again) {
                  AutismClientAddon.LOG.warn("[AI] Scaffold ownership for {} was not saved", this.journal);
               }
            }
         }

         AiScaffold.openScopes.remove(this);
      }
   }
}
