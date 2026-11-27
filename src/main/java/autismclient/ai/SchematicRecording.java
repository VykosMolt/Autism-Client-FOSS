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
   private final ClientLevel putAt_ClientLevel;
   private final Path fill_Path;
   private String readEx_Str;
   private final boolean value_Z;
   private final int prevEx_I;
   private final int prev_I;
   private final Long2ObjectOpenHashMap<SchematicCapture.Inner_CachePool> putAt_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectLinkedOpenHashMap<LevelChunk> putAt_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   private int chunkAt_I = 2147483647;
   private int state = -2147483648;
   private int closeOf = 2147483647;
   private int pollAt = -2147483648;
   private long fill_J;
   private int pollOf;
   private boolean prevEx_Z = true;
   private boolean prev_Z;
   private boolean chunkAt_Z;
   private Future<?> putAt_Future;
   volatile boolean putAt_Z;
   volatile boolean fill_Z;
   volatile boolean readEx_Z;
   volatile int flowTemp = -1;
   volatile String putAt_Str = "Recording";
   volatile String fill_Str = "";
   volatile Path putAt_Path;

   SchematicRecording(Minecraft var1, Path var2, String var3, boolean var4) {
      if (var1.level != null && var1.player != null) {
         this.putAt_ClientLevel = var1.level;
         this.fill_Path = var2;
         this.readEx_Str = SchematicCapture.normalizeName(var3);
         this.value_Z = var4;
         this.prevEx_I = this.putAt_ClientLevel.getMinY();
         this.prev_I = this.putAt_ClientLevel.getMaxY();
         int var5 = var1.player.getBlockX() >> 4;
         int var6 = var1.player.getBlockZ() >> 4;
         var var7 = new ArrayList<>(SchematicWorkspace.loadedChunks(this.putAt_ClientLevel));
         var7.sort(Comparator.comparingLong(var2x -> {
            long var3x = (long)var2x.getPos().x() - var5;
            long var5x = (long)var2x.getPos().z() - var6;
            return var3x * var3x + var5x * var5x;
         }));

         for (LevelChunk var9 : var7) {
            this.putAt_V_ClientLevel_LevelChunk(this.putAt_ClientLevel, var9);
         }
      } else {
         throw new IllegalStateException("Join a world first");
      }
   }

   boolean putAt_Z() {
      return this.prevEx_Z;
   }

   boolean fill_Z() {
      return this.chunkAt_Z;
   }

   int flowState() {
      return this.putAt_Long2ObjectOpenHas.size();
   }

   String putAt_Str() {
      return this.flowState() + (this.flowState() == 1 ? " chunk" : " chunks");
   }

   int flowTemp() {
      return this.putAt_Z ? this.flowTemp : mixState(this.flowState(), this.putAt_Long2ObjectLinkedO.size());
   }

   static int mixState(int var0, int var1) {
      return var0 == 0 ? 0 : (int)(100L * var0 / ((long)var0 + var1));
   }

   void putAt_V_ClientLevel_LevelChunk(ClientLevel var1, LevelChunk var2) {
      if (this.prevEx_Z && var1 == this.putAt_ClientLevel) {
         long var3 = var2.getPos().pack();
         if (!this.putAt_Long2ObjectOpenHas.containsKey(var3)) {
            if (this.putAt_Long2ObjectOpenHas.size() + this.putAt_Long2ObjectLinkedO.size() >= 16384 && !this.putAt_Long2ObjectLinkedO.containsKey(var3)) {
               this.chunkAt_Z = true;
               this.putAt_V();
            } else {
               this.putAt_Long2ObjectLinkedO.put(var3, var2);
            }
         }
      }
   }

   void putAt_V_Minecraft(Minecraft var1) {
      if ((this.prevEx_Z || this.prev_Z) && !this.putAt_Z && !this.readEx_Z && !this.fill_Z) {
         try {
            if (var1.level != this.putAt_ClientLevel || var1.player == null) {
               this.putAt_V();
            }

            this.value();
            if (this.prev_Z && this.putAt_Long2ObjectLinkedO.isEmpty()) {
               this.readEx();
            }
         } catch (Exception var3) {
            AutismClientAddon.LOG.warn("Schematic recording stopped early", var3);
            this.readEx();
         }
      }
   }

   void putAt_V_Str(String var1) {
      if (this.prevEx_Z) {
         this.readEx_Str = SchematicCapture.normalizeName(var1);
         this.putAt_V();
      }
   }

   void putAt_V() {
      if (this.prevEx_Z && !this.putAt_Z && !this.readEx_Z && !this.fill_Z) {
         this.prevEx_Z = false;
         this.prev_Z = true;
         this.putAt_Str = "Finishing capture";
         if (this.putAt_Long2ObjectLinkedO.isEmpty()) {
            this.readEx();
         }
      }
   }

   private void readEx() {
      this.prevEx_Z = this.prev_Z = false;
      this.putAt_Z = true;
      this.putAt_Long2ObjectLinkedO.clear();
      this.flowTemp = 1;
      this.putAt_Str = "Packing recording";
      String var1 = this.readEx_Str;
      var var2 = new ArrayList<>(this.putAt_Long2ObjectOpenHas.values());
      if (var2.isEmpty()) {
         this.fill_Str = "No chunks were recorded";
         this.putAt_Str = "Save failed";
         this.readEx_Z = true;
      } else {
         try {
            this.putAt_Future = SchematicLibrary.EXECUTOR.submit(
                  () -> {
                     try {
                        this.putAt_Str = "Trimming recording";
                        var2.sort(
                           Comparator.<SchematicCapture.Inner_CachePool>comparingInt(var0 -> var0.chunkPos.z())
                              .thenComparingInt(var0 -> var0.chunkPos.x())
                        );
                        this.putAt_Path = SchematicCapture.init(this.fill_Path, var1, var2, true, var1xx -> this.flowTemp = 5 + var1xx * 85 / 100);
                        this.flowTemp = 100;
                        this.putAt_Str = "Saved";
                        this.readEx_Z = true;
                     } catch (CancellationException var8) {
                        this.fill_Z = true;
                        this.putAt_Str = "Cancelled";
                     } catch (Exception var9) {
                        this.fill_Str = SchematicLibrary.describeError(var9);
                        this.putAt_Str = "Save failed";
                        this.readEx_Z = true;
                     } finally {
                        Thread.interrupted();
                     }
                  }
               );
         } catch (RejectedExecutionException var4) {
            this.putAt_Z = false;
            this.fill_Str = "Schematic files are busy. Try again";
            this.putAt_Str = "Save failed";
            this.readEx_Z = true;
         }
      }
   }

   void fill_V() {
      this.prevEx_Z = this.prev_Z = false;
      this.fill_Z = true;
      this.putAt_Long2ObjectLinkedO.clear();
      if (this.putAt_Future != null) {
         this.putAt_Future.cancel(true);
      }

      this.putAt_Str = "Recording cancelled";
      this.flowTemp = -1;
   }

   private void value() {
      if (!this.putAt_Long2ObjectLinkedO.isEmpty()) {
         long var1 = System.nanoTime() + 2000000L;

         for (int var3 = 0; var3 < 8 && !this.putAt_Long2ObjectLinkedO.isEmpty() && System.nanoTime() < var1; var3++) {
            this.putAt_V_NetHelper_JsonCach(this.putAt_NetHelper_JsonCach_LevelChunk(this.putAt_Long2ObjectLinkedO.removeFirst()));
         }
      }
   }

   private void putAt_V_NetHelper_JsonCach(SchematicCapture.Inner_CachePool var1) {
      boolean var2 = !var1.init_Map.isEmpty() || !var1.blockEntityTags.isEmpty() || !var1.load.isEmpty();
      if (var2) {
         int var3 = Math.min(this.chunkAt_I, var1.chunkPos.x());
         int var4 = Math.max(this.state, var1.chunkPos.x());
         int var5 = Math.min(this.closeOf, var1.chunkPos.z());
         int var6 = Math.max(this.pollAt, var1.chunkPos.z());
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

         long var13 = var1.load.isEmpty() ? putAt_J_I_I(var7, var8) : var1.area.volume();
         if (putAt(var3, var4, var5, var6) || this.fill_J + var13 > 134217728L) {
            this.chunkAt_Z = true;
            this.readEx();
            return;
         }

         this.chunkAt_I = var3;
         this.state = var4;
         this.closeOf = var5;
         this.pollAt = var6;
         this.fill_J += var13;
         this.pollOf++;
      }

      this.putAt_Long2ObjectOpenHas.put(var1.chunkPos.pack(), var1);
      if (this.pollOf >= 4096 || this.putAt_Long2ObjectOpenHas.size() >= 16384) {
         this.chunkAt_Z = true;
         this.readEx();
      }
   }

   static boolean putAt(int var0, int var1, int var2, int var3) {
      return ((long)var1 - var0 + 1L) * 16L > 4096L || ((long)var3 - var2 + 1L) * 16L > 4096L;
   }

   static long putAt_J_I_I(int var0, int var1) {
      return var1 < var0 ? 0L : 256L * ((long)var1 - var0 + 1L) * 16L;
   }

   private SchematicCapture.Inner_CachePool putAt_NetHelper_JsonCach_LevelChunk(LevelChunk var1) {
      int var2 = var1.getPos().x();
      int var3 = var1.getPos().z();
      SchematicCapture.Inner_DataUtil var4 = new SchematicCapture.Inner_DataUtil(
         new BlockPos(var2 << 4, this.prevEx_I, var3 << 4), new BlockPos((var2 << 4) + 15, this.prev_I, (var3 << 4) + 15)
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
               CompoundTag var11 = var10.saveWithFullMetadata(this.putAt_ClientLevel.registryAccess());
               var11.putInt("x", var9.getX() - var4.min().getX());
               var11.putInt("y", var9.getY() - var4.min().getY());
               var11.putInt("z", var9.getZ() - var4.min().getZ());
               var5.blockEntityTags.add(var11);
            }
         }
      }

      if (this.value_Z) {
         AABB var13 = new AABB(var4.min().getX(), this.prevEx_I, var4.min().getZ(), var4.max().getX() + 1, this.prev_I + 1, var4.max().getZ() + 1);
         this.putAt_V_NetHelper_JsonCach(
            var5, var4, this.putAt_ClientLevel.getEntities((Entity)null, var13, var0 -> !(var0 instanceof Player) && !var0.isPassenger())
         );
      }

      return var5;
   }

   private void putAt_V_NetHelper_JsonCach(SchematicCapture.Inner_CachePool var1, SchematicCapture.Inner_DataUtil var2, List<Entity> var3) {
      for (Entity var5 : var3) {
         if (var2.contains(var5.getX(), var5.getY(), var5.getZ())) {
            TagValueOutput var6 = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.putAt_ClientLevel.registryAccess());
            if (var5.save(var6)) {
               CompoundTag var7 = var6.buildResult();
               SchematicCapture.init(var7, var2.min());
               var1.load.add(var7);
            }
         }
      }
   }
}
