package autismclient.ai;

import autismclient.AutismClientAddon;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

final class SchematicRecording {
   static final int flowState = 4096;
   private static final int readEx_I = 16384;
   private static final long putAt_J = 2000000L;
   private static final int value_I = 8;
   private final ClientLevel level;
   private final Path fill_Path;
   private String readEx_Str;
   private final boolean includeEntities;
   private final int minY;
   private final int maxY;
   private final Long2ObjectOpenHashMap<SchematicCapture.Inner_CachePool> chunks = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectLinkedOpenHashMap<LevelChunk> putAt_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   private int minChunkX = 2147483647;
   private int maxChunkX = -2147483648;
   private int minChunkZ = 2147483647;
   private int maxChunkZ = -2147483648;
   private long estimatedBytes;
   private int capturedChunks;
   private boolean recording = true;
   private boolean finishing;
   private boolean limitReached;
   private Future<?> putAt_Future;
   volatile boolean finished;
   volatile boolean cancelled;
   volatile boolean failed;
   volatile int progress = -1;
   volatile String status = "Recording";
   volatile String error = "";
   volatile Path putAt_Path;

   SchematicRecording(Minecraft var1, Path var2, String var3, boolean var4) {
      if (var1.level != null && var1.player != null) {
         this.level = var1.level;
         this.fill_Path = var2;
         this.readEx_Str = SchematicCapture.normalizeName(var3);
         this.includeEntities = var4;
         this.minY = this.level.getMinY();
         this.maxY = this.level.getMaxY();
         int var5 = var1.player.getBlockX() >> 4;
         int var6 = var1.player.getBlockZ() >> 4;
         var var7 = new ArrayList<>(SchematicWorkspace.loadedChunks(this.level));
         var7.sort(Comparator.comparingLong(var2x -> {
            long var3x = (long)var2x.getPos().x() - var5;
            long var5x = (long)var2x.getPos().z() - var6;
            return var3x * var3x + var5x * var5x;
         }));

         for (LevelChunk var9 : var7) {
            this.putAt_V_ClientLevel_LevelChunk(this.level, var9);
         }
      } else {
         throw new IllegalStateException("Join a world first");
      }
   }

   boolean isRecording() {
      return this.recording;
   }

   boolean fill_Z() {
      return this.limitReached;
   }

   int chunkCount() {
      return this.chunks.size();
   }

   String chunkLabel() {
      return this.chunkCount() + (this.chunkCount() == 1 ? " chunk" : " chunks");
   }

   int progress() {
      return this.finished ? this.progress : mixState(this.chunkCount(), this.putAt_Long2ObjectLinkedO.size());
   }

   static int mixState(int var0, int var1) {
      return var0 == 0 ? 0 : (int)(100L * var0 / ((long)var0 + var1));
   }

   void putAt_V_ClientLevel_LevelChunk(ClientLevel var1, LevelChunk var2) {
      if (this.recording && var1 == this.level) {
         long var3 = var2.getPos().pack();
         if (!this.chunks.containsKey(var3)) {
            if (this.chunks.size() + this.putAt_Long2ObjectLinkedO.size() >= 16384 && !this.putAt_Long2ObjectLinkedO.containsKey(var3)) {
               this.limitReached = true;
               this.finish();
            } else {
               this.putAt_Long2ObjectLinkedO.put(var3, var2);
            }
         }
      }
   }

   void tick(Minecraft var1) {
      if ((this.recording || this.finishing) && !this.finished && !this.failed && !this.cancelled) {
         try {
            if (var1.level != this.level || var1.player == null) {
               this.finish();
            }

            this.drainPending();
            if (this.finishing && this.putAt_Long2ObjectLinkedO.isEmpty()) {
               this.startPacking();
            }
         } catch (Exception var3) {
            AutismClientAddon.LOG.warn("Schematic recording stopped early", var3);
            this.startPacking();
         }
      }
   }

   void putAt_V_Str(String var1) {
      if (this.recording) {
         this.readEx_Str = SchematicCapture.normalizeName(var1);
         this.finish();
      }
   }

   void finish() {
      if (this.recording && !this.finished && !this.failed && !this.cancelled) {
         this.recording = false;
         this.finishing = true;
         this.status = "Finishing capture";
         if (this.putAt_Long2ObjectLinkedO.isEmpty()) {
            this.startPacking();
         }
      }
   }

   private void startPacking() {
      this.recording = this.finishing = false;
      this.finished = true;
      this.putAt_Long2ObjectLinkedO.clear();
      this.progress = 1;
      this.status = "Packing recording";
      String var1 = this.readEx_Str;
      var var2 = new ArrayList<>(this.chunks.values());
      if (var2.isEmpty()) {
         this.error = "No chunks were recorded";
         this.status = "Save failed";
         this.failed = true;
      } else {
         try {
            this.putAt_Future = SchematicLibrary.EXECUTOR.submit(
                  () -> {
                     try {
                        this.status = "Trimming recording";
                        var2.sort(
                           Comparator.<SchematicCapture.Inner_CachePool>comparingInt(var0 -> var0.chunkPos.z())
                              .thenComparingInt(var0 -> var0.chunkPos.x())
                        );
                        this.putAt_Path = SchematicCapture.save(this.fill_Path, var1, var2, true, var1xx -> this.progress = 5 + var1xx * 85 / 100);
                        this.progress = 100;
                        this.status = "Saved";
                        this.failed = true;
                     } catch (CancellationException var8) {
                        this.cancelled = true;
                        this.status = "Cancelled";
                     } catch (Exception var9) {
                        this.error = SchematicLibrary.describeError(var9);
                        this.status = "Save failed";
                        this.failed = true;
                     } finally {
                        Thread.interrupted();
                     }
                  }
               );
         } catch (RejectedExecutionException var4) {
            this.finished = false;
            this.error = "Schematic files are busy. Try again";
            this.status = "Save failed";
            this.failed = true;
         }
      }
   }

   void cancel() {
      this.recording = this.finishing = false;
      this.cancelled = true;
      this.putAt_Long2ObjectLinkedO.clear();
      if (this.putAt_Future != null) {
         this.putAt_Future.cancel(true);
      }

      this.status = "Recording cancelled";
      this.progress = -1;
   }

   private void drainPending() {
      if (!this.putAt_Long2ObjectLinkedO.isEmpty()) {
         long var1 = System.nanoTime() + 2000000L;

         for (int var3 = 0; var3 < 8 && !this.putAt_Long2ObjectLinkedO.isEmpty() && System.nanoTime() < var1; var3++) {
            this.putAt_V_NetHelper_JsonCach(this.putAt_NetHelper_JsonCach_LevelChunk(this.putAt_Long2ObjectLinkedO.removeFirst()));
         }
      }
   }

   private void putAt_V_NetHelper_JsonCach(SchematicCapture.Inner_CachePool var1) {
      boolean var2 = !var1.init_Map.isEmpty() || !var1.blockEntityTags.isEmpty() || !var1.entityTags.isEmpty();
      if (var2) {
         int var3 = Math.min(this.minChunkX, var1.chunkPos.x());
         int var4 = Math.max(this.maxChunkX, var1.chunkPos.x());
         int var5 = Math.min(this.minChunkZ, var1.chunkPos.z());
         int var6 = Math.max(this.maxChunkZ, var1.chunkPos.z());
         int var7 = 2147483647;
         int var8 = -2147483648;

         for (int var10 : var1.init_Map.keySet()) {
            var7 = Math.min(var7, var10);
            var8 = Math.max(var8, var10);
         }

         for (CompoundTag var14 : var1.blockEntityTags) {
            int var11 = var1.area.min().getY() + var14.getIntOr("y", 0) >> 4;
            var7 = Math.min(var7, var11);
            var8 = Math.max(var8, var11);
         }

         long var13 = var1.entityTags.isEmpty() ? bytesPerChunk(var7, var8) : var1.area.volume();
         if (exceedsSpan(var3, var4, var5, var6) || this.estimatedBytes + var13 > 134217728L) {
            this.limitReached = true;
            this.startPacking();
            return;
         }

         this.minChunkX = var3;
         this.maxChunkX = var4;
         this.minChunkZ = var5;
         this.maxChunkZ = var6;
         this.estimatedBytes += var13;
         this.capturedChunks++;
      }

      this.chunks.put(var1.chunkPos.pack(), var1);
      if (this.capturedChunks >= 4096 || this.chunks.size() >= 16384) {
         this.limitReached = true;
         this.startPacking();
      }
   }

   static boolean exceedsSpan(int var0, int var1, int var2, int var3) {
      return ((long)var1 - var0 + 1L) * 16L > 4096L || ((long)var3 - var2 + 1L) * 16L > 4096L;
   }

   static long bytesPerChunk(int var0, int var1) {
      return var1 < var0 ? 0L : 256L * ((long)var1 - var0 + 1L) * 16L;
   }

   private SchematicCapture.Inner_CachePool putAt_NetHelper_JsonCach_LevelChunk(LevelChunk var1) {
      int var2 = var1.getPos().x();
      int var3 = var1.getPos().z();
      SchematicCapture.Inner_DataUtil var4 = new SchematicCapture.Inner_DataUtil(
         new BlockPos(var2 << 4, this.minY, var3 << 4), new BlockPos((var2 << 4) + 15, this.maxY, (var3 << 4) + 15)
      );
      SchematicCapture.Inner_CachePool var5 = new SchematicCapture.Inner_CachePool(new ChunkPos(var2, var3), var4);
      LevelChunkSection[] var6 = var1.getSections();

      for (int var7 = 0; var7 < var6.length; var7++) {
         LevelChunkSection var8 = var6[var7];
         if (var8 != null && !var8.hasOnlyAir()) {
            var5.init_Map.put(var1.getSectionYFromSectionIndex(var7), var8.getStates().copy());
         }
      }

      if (!var5.init_Map.isEmpty()) {
         for (Entry var14 : var1.getBlockEntities().entrySet()) {
            BlockPos var9 = (BlockPos)var14.getKey();
            BlockEntity var10 = (BlockEntity)var14.getValue();
            if (var9 != null && var10 != null) {
               CompoundTag var11 = var10.saveWithFullMetadata(this.level.registryAccess());
               var11.putInt("x", var9.getX() - var4.min().getX());
               var11.putInt("y", var9.getY() - var4.min().getY());
               var11.putInt("z", var9.getZ() - var4.min().getZ());
               var5.blockEntityTags.add(var11);
            }
         }
      }

      if (this.includeEntities) {
         AABB var13 = new AABB(var4.min().getX(), this.minY, var4.min().getZ(), var4.max().getX() + 1, this.maxY + 1, var4.max().getZ() + 1);
         this.putAt_V_NetHelper_JsonCach(
            var5, var4, this.level.getEntities((Entity)null, var13, var0 -> !(var0 instanceof Player) && !var0.isPassenger())
         );
      }

      return var5;
   }

   private void putAt_V_NetHelper_JsonCach(SchematicCapture.Inner_CachePool var1, SchematicCapture.Inner_DataUtil var2, List<Entity> var3) {
      for (Entity var5 : var3) {
         if (var2.contains(var5.getX(), var5.getY(), var5.getZ())) {
            TagValueOutput var6 = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.level.registryAccess());
            if (var5.save(var6)) {
               CompoundTag var7 = var6.buildResult();
               SchematicCapture.relativizeEntityPos(var7, var2.min());
               var1.entityTags.add(var7);
            }
         }
      }
   }
}
