package autismclient.ai.a;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class TextFormat {
   private static final ThreadLocal<TextFormat.Inner_DataUtil> init_ThreadLocal = ThreadLocal.withInitial(TextFormat.Inner_DataUtil::new);
   private static final TextFormat.Inner_ConfigStore init_TextFormat_ConfigS = new TextFormat.Inner_ConfigStore() {
      @Override
      public boolean init(Object world, BlockPos at) {
         return ((ClientLevel)world).isInsideBuildHeight(at);
      }

      @Override
      public long init_J(Object world) {
         return ((ClientLevel)world).getGameTime();
      }

      @Override
      public Object init_Obj(Object world, int cx, int cz) {
         return ((ClientLevel)world).getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
      }

      @Override
      public boolean init_Z(Object chunk) {
         for (LevelChunkSection section : ((LevelChunk)chunk).getSections()) {
            if (section != null && !section.hasOnlyAir() && section.maybeHas(state -> state.getBlock() instanceof PotentSulfurBlock)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public List<TextFormat.Inner_BufferHelper> init_List(Object chunk, int x, int z) {
         LevelChunk loaded = (LevelChunk)chunk;
         LevelChunkSection[] sections = loaded.getSections();
         List<TextFormat.Inner_BufferHelper> ranges = new ArrayList<>();

         for (int section = 0; section < sections.length; section++) {
            LevelChunkSection data = sections[section];
            if (data != null && !data.hasOnlyAir() && data.maybeHas(state -> state.getBlock() instanceof PotentSulfurBlock)) {
               for (int y = 0; y < 16; y++) {
                  TextFormat.Inner_BufferHelper range = TextFormat.init(
                     (loaded.getSectionYFromSectionIndex(section) << 4) + y, data.getBlockState(x & 15, y, z & 15)
                  );
                  if (range != null) {
                     ranges.add(range);
                  }
               }
            }
         }

         return List.copyOf(ranges);
      }
   };

   private TextFormat() {
   }

   public static boolean init(ClientLevel level, BlockPos at) {
      return init(level, init_TextFormat_ConfigS, at);
   }

   static boolean init(Object world, TextFormat.Inner_ConfigStore source, BlockPos at) {
      if (world == null) {
         return true;
      }

      TextFormat.Inner_DataUtil cache = init_ThreadLocal.get();
      cache.init(world);

      try {
         if (!source.init(world, at)) {
            return true;
         }

         long tick = source.init_J(world);
         int lx = at.getX() & 15;
         int lz = at.getZ() & 15;
         if (lx > 0 && lx < 15 && lz > 0 && lz < 15) {
            int cx = at.getX() >> 4;
            int cz = at.getZ() >> 4;
            Object chunk = source.init_Obj(world, cx, cz);
            if (chunk == null) {
               return true;
            }

            TextFormat.Inner_StreamUtil summary = init(cache, world, source, chunk, cx, cz, IoWorker.init(IoWorker.init_J(cx, cz)), tick);
            if (summary == null) {
               return true;
            }

            if (!summary.containsSulfur) {
               return false;
            }
         }

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (init(cache, world, source, at.offset(dx, 0, dz), tick)) {
                  return true;
               }
            }
         }

         return false;
      } catch (RuntimeException racingChunk) {
         cache.init_LinkedHashMap.clear();
         cache.load.clear();
         return true;
      }
   }

   private static boolean init(TextFormat.Inner_DataUtil cache, Object world, TextFormat.Inner_ConfigStore source, BlockPos at, long tick) {
      int cx = at.getX() >> 4;
      int cz = at.getZ() >> 4;
      Object chunk = source.init_Obj(world, cx, cz);
      if (chunk == null) {
         return true;
      }

      long key = BlockPos.asLong(at.getX(), 0, at.getZ());
      long chunkKey = IoWorker.init_J(at.getX() >> 4, at.getZ() >> 4);
      int revision = IoWorker.init(chunkKey);
      TextFormat.Inner_StreamUtil summary = init(cache, world, source, chunk, cx, cz, revision, tick);
      if (summary == null) {
         return true;
      }

      if (!summary.containsSulfur) {
         return false;
      }

      TextFormat.Inner_CachePool cached = cache.init_LinkedHashMap.get(key);
      if (cached == null || cached.chunk.get() != chunk || cached.revision != revision || tick - cached.tick > 200L || tick < cached.tick) {
         List<TextFormat.Inner_BufferHelper> ranges = List.copyOf(source.init_List(chunk, at.getX(), at.getZ()));
         if (!init(world, source, chunk, cx, cz, revision)) {
            return true;
         }

         cached = new TextFormat.Inner_CachePool(new WeakReference<>(chunk), revision, tick, ranges);
         cache.init_LinkedHashMap.put(key, cached);

         while (cache.init_LinkedHashMap.size() > 1024) {
            cache.init_LinkedHashMap.remove(cache.init_LinkedHashMap.keySet().iterator().next());
         }
      }

      for (TextFormat.Inner_BufferHelper range : cached.ranges) {
         if (at.getY() >= range.bottom && at.getY() <= range.top) {
            return true;
         }
      }

      return false;
   }

   private static TextFormat.Inner_StreamUtil init(
      TextFormat.Inner_DataUtil cache, Object world, TextFormat.Inner_ConfigStore source, Object chunk, int cx, int cz, int revision, long tick
   ) {
      long key = IoWorker.init_J(cx, cz);
      TextFormat.Inner_StreamUtil summary = cache.load.get(key);
      if (summary == null || summary.chunk.get() != chunk || summary.revision != revision || tick - summary.tick > 200L || tick < summary.tick) {
         boolean contains = source.init_Z(chunk);
         if (!init(world, source, chunk, cx, cz, revision)) {
            return null;
         }

         summary = new TextFormat.Inner_StreamUtil(new WeakReference<>(chunk), revision, tick, contains);
         cache.load.put(key, summary);

         while (cache.load.size() > 384) {
            cache.load.remove(cache.load.keySet().iterator().next());
         }
      }

      return summary;
   }

   private static boolean init(Object world, TextFormat.Inner_ConfigStore source, Object chunk, int cx, int cz, int revision) {
      return IoWorker.init(IoWorker.init_J(cx, cz)) == revision && source.init_Obj(world, cx, cz) == chunk;
   }

   static TextFormat.Inner_BufferHelper init(int bottom, BlockState state) {
      return state.getBlock() instanceof PotentSulfurBlock && !state.getValue(PotentSulfurBlock.STATE).toString().equalsIgnoreCase("dry")
         ? new TextFormat.Inner_BufferHelper(bottom - 1, bottom + 24)
         : null;
   }

   record Inner_BufferHelper(int bottom, int top) {
   }

   private record Inner_CachePool(WeakReference<Object> chunk, int revision, long tick, List<TextFormat.Inner_BufferHelper> ranges) {
   }

   interface Inner_ConfigStore {
      boolean init(Object var1, BlockPos var2);

      long init_J(Object var1);

      Object init_Obj(Object var1, int var2, int var3);

      boolean init_Z(Object var1);

      List<TextFormat.Inner_BufferHelper> init_List(Object var1, int var2, int var3);
   }

   private static final class Inner_DataUtil {
      final LinkedHashMap<Long, TextFormat.Inner_CachePool> init_LinkedHashMap = new LinkedHashMap<>(64, 0.75F, true);
      final LinkedHashMap<Long, TextFormat.Inner_StreamUtil> load = new LinkedHashMap<>(64, 0.75F, true);
      WeakReference<Object> init_WeakReference = new WeakReference<>(null);

      void init(Object world) {
         if (this.init_WeakReference.get() != world) {
            this.init_LinkedHashMap.clear();
            this.load.clear();
            this.init_WeakReference = new WeakReference<>(world);
         }
      }
   }

   private record Inner_StreamUtil(WeakReference<Object> chunk, int revision, long tick, boolean containsSulfur) {
   }
}
