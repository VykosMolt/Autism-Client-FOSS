package autismclient.modules;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class OreSimEspScanner {
   static final int init_I = 5;
   public static final int load_I = 8192;
   private static final int write_I = 2048;
   private static final int update = 64;
   private static final int sync = 4;
   private static final int flush = 96;
   private static final long init_J = 400000000L;
   private static final int close = 25;
   private static int open;
   private static int poll;
   private static boolean init_Z;
   private static Long init_Long;
   private static long load_J;
   private static final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private static final long[] init_JArr = new long[8192];
   private static final int[] init_IArr = new int[8192];
   private static final double[] init_DArr = new double[8192];
   private static final EspChunkWorker.EspBox[] init_af_CachePoolArr = new EspChunkWorker.EspBox[8192];
   static final int read_I = -3355444;
   private static final OreSimEspScanner.Inner_StreamUtil init_ak_StreamUtil = new OreSimEspScanner.Inner_StreamUtil("", Set.of(), List.of(), 0);
   private static volatile OreSimEspScanner.Inner_StreamUtil load_ak_StreamUtil = init_ak_StreamUtil;
   private static volatile OreSimEspScanner.Inner_StreamUtil read_ak_StreamUtil = init_ak_StreamUtil;
   private static int push = -2147483648;
   private static boolean load_Z;
   private static boolean read_Z;
   private static int pop = -2147483648;
   private static final Object2IntOpenHashMap<Block> init_Object2IntOpenHash = new Object2IntOpenHashMap<>();
   private static volatile autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil init_BufferHelper_DataU = autismclient.util.oresim.OreSimSeedParser.init(null);
   private static final long read_J = TimeUnit.MINUTES.toNanos(2L);
   private static OreSimEspScanner.Inner_StreamUtil write_ak_StreamUtil;
   private static boolean[] init_ZArr = new boolean[0];
   private static volatile int tick;
   private static volatile int run;
   private static volatile int get;
   private static volatile int set;

   private OreSimEspScanner() {
   }

   private static void init_V(Module xray) {
      int revision = al.init_I();
      if (revision != push) {
         push = revision;
         load_Z = xray != null && "OreSim".equals(xray.value("mode"));
         read_Z = xray != null && "ESP".equals(xray.value("render-style"));
      }
   }

   static boolean init_Z(Module xray) {
      if (xray == null) {
         return false;
      }

      init_V(xray);
      return load_Z;
   }

   static boolean load_Z(Module xray) {
      if (xray == null) {
         return false;
      }

      init_V(xray);
      return read_Z;
   }

   static boolean read_Z(Module xray) {
      return xray != null && xray.isEnabled() && load_Z(xray);
   }

   static boolean write_Z(Module xray) {
      return xray != null && xray.isEnabled() && init_Z(xray) && !load_Z(xray);
   }

   static boolean update(Module xray) {
      return xray != null && xray.isEnabled() && !load_Z(xray);
   }

   static float init_F(Module xray) {
      return 0.3F;
   }

   private static OreSimEspScanner.Inner_StreamUtil init_ak_StreamUtil(Module xray) {
      if (xray == null) {
         return init_ak_StreamUtil;
      } else if (init_Z(xray)) {
         OreSimEspScanner.Inner_StreamUtil next = init(xray.value("oresim-ores"), read_ak_StreamUtil);
         read_ak_StreamUtil = next;
         return next;
      } else {
         OreSimEspScanner.Inner_StreamUtil next = init(xray.value("whitelist"), load_ak_StreamUtil);
         load_ak_StreamUtil = next;
         return next;
      }
   }

   private static OreSimEspScanner.Inner_StreamUtil init(String raw, OreSimEspScanner.Inner_StreamUtil cached) {
      String safe = raw == null ? "" : raw;
      if (cached.init_Str().equals(safe)) {
         return cached;
      }

      Set<Block> blocks = new LinkedHashSet<>();
      List<String> nonOre = new ArrayList<>();
      int mask = 0;

      for (String token : safe.split("\\|")) {
         String id = token.trim().toLowerCase(Locale.ROOT);
         if (!id.isEmpty()) {
            if (!id.contains(":")) {
               id = "minecraft:" + id;
            }

            Identifier parsed = Identifier.tryParse(id);
            if (parsed != null) {
               Block block = BuiltInRegistries.BLOCK.getOptional(parsed).orElse(Blocks.AIR);
               if (block != Blocks.AIR) {
                  blocks.add(block);
                  autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil family = autismclient.util.oresim.OreBlockCatalog.init(id);
                  if (family != null) {
                     mask |= 1 << family.ordinal();
                  } else if (!nonOre.contains(id)) {
                     nonOre.add(id);
                  }
               }
            }
         }
      }

      return new OreSimEspScanner.Inner_StreamUtil(safe, Set.copyOf(blocks), List.copyOf(nonOre), mask);
   }

   static int init_I(Module xray) {
      return init_ak_StreamUtil(xray).init_I();
   }

   static boolean init_Z(Module xray, autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil kind) {
      return kind != null && (init_ak_StreamUtil(xray).init_I() & 1 << kind.ordinal()) != 0;
   }

   static List<String> init_List(Module xray) {
      if (xray == null) {
         return List.of();
      }

      OreSimEspScanner.Inner_StreamUtil next = init(xray.value("whitelist"), load_ak_StreamUtil);
      load_ak_StreamUtil = next;
      return next.init_List();
   }

   static boolean init(Module xray, String id) {
      return init_List(xray).contains(id);
   }

   static int init_I(Module xray, autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil kind) {
      return am.init_I_Module(xray, kind.init(), kind.init_I);
   }

   static int init(Module xray, Block block) {
      if (block == null) {
         return -3355444;
      }

      int revision = al.init_I();
      if (revision != pop) {
         pop = revision;
         init_Object2IntOpenHash.clear();
      }

      int cached = init_Object2IntOpenHash.getOrDefault(block, -2147483648);
      if (cached != -2147483648) {
         return cached;
      }

      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil family = id == null ? null : autismclient.util.oresim.OreBlockCatalog.init(id.toString());
      int color = family != null ? init_I(xray, family) : (id == null ? -3355444 : am.init_I_Module(xray, "block-color-" + id, -3355444));
      init_Object2IntOpenHash.put(block, color);
      return color;
   }

   static Long init_Long(Module xray) {
      autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil parsed = autismclient.util.oresim.OreSimSeedParser.init(xray == null ? null : xray.value("oresim-seed"));
      init_BufferHelper_DataU = parsed;
      return parsed.init_Long();
   }

   public static autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil seedInfo(Module xray) {
      init_Long(xray);
      return init_BufferHelper_DataU.init_BufferHelper_Strea();
   }

   public static Long seed(Module xray) {
      return init_Long(xray);
   }

   public static int targetCount(Module xray) {
      return init_ak_StreamUtil(xray).init_Set().size();
   }

   public static int read_I(Module xray) {
      return init_I(xray);
   }

   static int write_I(Module xray) {
      int configured = xray == null ? 3 : xray.integer("oresim-radius");
      return Math.max(1, Math.min(configured, 8));
   }

   static void init(Module xray, ClientLevel level, Player player) {
      if (init_Z(xray) && xray != null && xray.isEnabled() && level != null && player != null) {
         autismclient.util.oresim.OreSimEngine.read_V();
         Long parsedSeed = init_Long(xray);
         int mask = init_I(xray);
         long now = System.nanoTime();
         if (!init_Z || !Objects.equals(parsedSeed, init_Long)) {
            init_Z = true;
            init_Long = parsedSeed;
            load_J = now + 400000000L;
            autismclient.util.oresim.OreSimEngine.init_V();
         } else if (parsedSeed != null && mask != 0 && now >= load_J) {
            autismclient.util.oresim.OreSimEngine.init(level, player.blockPosition(), parsedSeed, write_I(xray), mask);
            if (++open >= 4) {
               open = 0;
               load(xray, level, player);
            }
         } else {
            autismclient.util.oresim.OreSimEngine.init_V();
         }
      } else {
         autismclient.util.oresim.OreSimEngine.load_V();
      }
   }

   public static void init_V() {
      if (autismclient.util.oresim.OreSimEngine.init(read_J)) {
         init_Z = false;
         init_Long = null;
         load_J = 0L;
      }
   }

   private static void load(Module xray, ClientLevel level, Player player) {
      OreSimEspScanner.Inner_StreamUtil selection = init_ak_StreamUtil(xray);
      if (!selection.init_Set().isEmpty()) {
         Vec3 eyes = player.getEyePosition();
         double maxDistSq = 25.0;
         MutableBlockPos cursor = new MutableBlockPos();
         int[] rays = new int[]{0};
         autismclient.util.oresim.OreSimEngine.init_V_BlockPos(player.blockPosition(), 5, (stateId, packed) -> {
            BlockState predicted = autismclient.util.oresim.OreBlockCatalog.Inner_StreamUtil.init_BlockState(stateId);
            if (predicted == null) {
               return false;
            } else {
               int x = BlockPos.getX(packed);
               int y = BlockPos.getY(packed);
               int z = BlockPos.getZ(packed);
               double cx = x + 0.5 - eyes.x;
               double cy = y + 0.5 - eyes.y;
               double cz = z + 0.5 - eyes.z;
               if (cx * cx + cy * cy + cz * cz > maxDistSq) {
                  return false;
               } else {
                  cursor.set(x, y, z);
                  BlockState actual = level.getBlockState(cursor);
                  if (actual.getBlock() == predicted.getBlock()) {
                     return false;
                  } else {
                     boolean open = actual.isAir() || init(level, cursor);
                     if (!open) {
                        return false;
                     } else if (!init_LongOpenHashSet.add(packed)) {
                        return false;
                     } else {
                        return rays[0] >= 96 ? false : init(level, player, eyes, cursor.immutable(), actual.isAir(), rays);
                     }
                  }
               }
            }
         });
         if (++poll >= 25 || init_LongOpenHashSet.size() > 4096) {
            poll = 0;
            init_LongOpenHashSet.clear();
         }
      }
   }

   static long init(Module xray, ClientLevel level) {
      return init_Z(xray) ? (long)autismclient.util.oresim.OreSimEngine.read_I() << 1 | 1L : (long)EspChunkWorker.revision() << 1;
   }

   private static boolean init(OreSimEspScanner.Inner_StreamUtil selection, int stateId) {
      BlockState state = autismclient.util.oresim.OreBlockCatalog.Inner_StreamUtil.init_BlockState(stateId);
      return state != null && selection.init_Set().contains(state.getBlock());
   }

   private static boolean[] init_ZArr(OreSimEspScanner.Inner_StreamUtil selection) {
      int count = autismclient.util.oresim.OreBlockCatalog.Inner_StreamUtil.init();
      boolean[] table = init_ZArr;
      if (selection == write_ak_StreamUtil && table.length == count) {
         return table;
      }

      table = new boolean[count];

      for (int id = 0; id < count; id++) {
         table[id] = init(selection, id);
      }

      write_ak_StreamUtil = selection;
      init_ZArr = table;
      return table;
   }

   private static IntPredicate init_IntPredicate(OreSimEspScanner.Inner_StreamUtil selection) {
      boolean[] table = init_ZArr(selection);
      return stateId -> stateId >= 0 && stateId < table.length && table[stateId];
   }

   static int init(Module xray, Player player, long[] outPos, int[] outState) {
      if (xray != null && xray.isEnabled() && player != null && init_Z(xray)) {
         OreSimEspScanner.Inner_StreamUtil selection = init_ak_StreamUtil(xray);
         if (selection.init_Set().isEmpty()) {
            return 0;
         }

         Vec3 eyes = player.getEyePosition();
         return autismclient.util.oresim.OreSimEngine.init(eyes.x, eyes.y, eyes.z, write_I(xray), init_IntPredicate(selection), 8192, outPos, outState);
      } else {
         return 0;
      }
   }

   static void init(Module xray, ClientLevel level, Player player, BiConsumer<AABB, Integer> emit) {
      if (emit != null && xray != null && xray.isEnabled() && level != null && player != null) {
         if (init_Z(xray)) {
            init(xray, player, emit);
         } else {
            load(xray, level, player, emit);
         }
      }
   }

   private static void init(Module xray, Player player, BiConsumer<AABB, Integer> emit) {
      OreSimEspScanner.Inner_StreamUtil selection = init_ak_StreamUtil(xray);
      if (!selection.init_Set().isEmpty()) {
         Vec3 eyes = player.getEyePosition();
         int count = autismclient.util.oresim.OreSimEngine.init(eyes.x, eyes.y, eyes.z, write_I(xray), init_IntPredicate(selection), 8192, init_JArr, init_IArr);
         int[] colors = new int[autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil.values().length];

         for (autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil kind : autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil.values()) {
            colors[kind.ordinal()] = init_I(xray, kind);
         }

         for (int i = 0; i < count; i++) {
            autismclient.util.oresim.OreBlockCatalog.Inner_DataUtil kind = autismclient.util.oresim.OreBlockCatalog.Inner_StreamUtil.init_CachePool_DataUtil(init_IArr[i]);
            emit.accept(init(init_JArr[i]), kind == null ? -3355444 : colors[kind.ordinal()]);
         }
      }
   }

   private static void load(Module xray, ClientLevel level, Player player, BiConsumer<AABB, Integer> emit) {
      OreSimEspScanner.Inner_StreamUtil selection = init_ak_StreamUtil(xray);
      if (!selection.init_Set().isEmpty()) {
         EspChunkWorker.setLevel(level);
         Object2IntMap<Block> colors = new Object2IntOpenHashMap<>();
         colors.defaultReturnValue(-3355444);
         int colorsHash = 0;

         for (Block block : selection.init_Set()) {
            int color = init(xray, block);
            colors.put(block, color);
            colorsHash = colorsHash * 31 + color;
         }

         int chunkRadius = am.init_I();
         int playerBlockY = player.getBlockY();
         String stamp = selection.init_Str() + "|" + colorsHash + "|b" + (playerBlockY >> 4);
         long gameTime = level.getGameTime();
         ClientChunkCache chunks = level.getChunkSource();
         int playerChunkX = player.chunkPosition().x();
         int playerChunkZ = player.chunkPosition().z();
         Vec3 eyes = player.getEyePosition();
         double[] heapDist = init_DArr;
         EspChunkWorker.EspBox[] heapEntry = init_af_CachePoolArr;
         int size = 0;

         for (int ring = 0; ring <= chunkRadius; ring++) {
            if (size == 8192) {
               double ringMin = (ring - 1) * 16.0;
               if (ringMin > 0.0 && ringMin * ringMin > heapDist[0]) {
                  break;
               }
            }

            for (int dx = -ring; dx <= ring; dx++) {
               for (int dz = -ring; dz <= ring; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                     LevelChunk chunk = chunks.getChunk(playerChunkX + dx, playerChunkZ + dz, ChunkStatus.FULL, false);
                     if (chunk != null) {
                        for (EspChunkWorker.EspBox entry : EspChunkWorker.flush
                           .initAsync(chunk, gameTime, stamp, (scanned, out) -> init(scanned, selection, colors, playerBlockY, out))) {
                           Vec3 center = entry.tracer();
                           double px = center.x - eyes.x;
                           double py = center.y - eyes.y;
                           double pz = center.z - eyes.z;
                           double distSq = px * px + py * py + pz * pz;
                           if (size < 8192) {
                              heapDist[size] = distSq;
                              heapEntry[size] = entry;
                              init(heapDist, heapEntry, size++);
                           } else if (distSq < heapDist[0]) {
                              heapDist[0] = distSq;
                              heapEntry[0] = entry;
                              load(heapDist, heapEntry, size);
                           }
                        }
                     }
                  }
               }
            }
         }

         for (int i = 0; i < size; i++) {
            emit.accept(heapEntry[i].box(), heapEntry[i].color());
            heapEntry[i] = null;
         }
      }
   }

   private static void init(double[] dist, EspChunkWorker.EspBox[] entries, int index) {
      while (index > 0) {
         int parent = index - 1 >>> 1;
         if (!(dist[parent] >= dist[index])) {
            init(dist, entries, parent, index);
            index = parent;
            continue;
         }
         break;
      }
   }

   private static void load(double[] dist, EspChunkWorker.EspBox[] entries, int size) {
      int index = 0;

      while (true) {
         int left = index * 2 + 1;
         if (left >= size) {
            break;
         }

         int largest = left;
         int right = left + 1;
         if (right < size && dist[right] > dist[left]) {
            largest = right;
         }

         if (dist[index] >= dist[largest]) {
            break;
         }

         init(dist, entries, index, largest);
         index = largest;
      }
   }

   private static void init(double[] dist, EspChunkWorker.EspBox[] entries, int a, int b) {
      double d = dist[a];
      dist[a] = dist[b];
      dist[b] = d;
      EspChunkWorker.EspBox e = entries[a];
      entries[a] = entries[b];
      entries[b] = e;
   }

   private static void init(LevelChunk chunk, OreSimEspScanner.Inner_StreamUtil selection, Object2IntMap<Block> colors, int playerBlockY, List<EspChunkWorker.EspBox> out) {
      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();
      int playerBand = playerBlockY >> 4;
      Set<Block> targets = selection.init_Set();
      Predicate<BlockState> isTarget = statex -> targets.contains(statex.getBlock());
      int[] order = new int[sections.length];
      int i = 0;

      while (i < order.length) {
         order[i] = i++;
      }

      for (int ix = 1; ix < order.length; ix++) {
         int value = order[ix];
         int key = Math.abs(chunk.getSectionYFromSectionIndex(value) - playerBand);

         int j;
         for (j = ix - 1; j >= 0 && Math.abs(chunk.getSectionYFromSectionIndex(order[j]) - playerBand) > key; j--) {
            order[j + 1] = order[j];
         }

         order[j + 1] = value;
      }

      for (int index : order) {
         if (out.size() >= 2048) {
            break;
         }

         LevelChunkSection section = sections[index];
         if (section != null && !section.hasOnlyAir() && section.maybeHas(isTarget)) {
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;

            for (int sy = 0; sy < 16 && out.size() < 2048; sy++) {
               for (int sx = 0; sx < 16 && out.size() < 2048; sx++) {
                  for (int sz = 0; sz < 16 && out.size() < 2048; sz++) {
                     BlockState state = section.getBlockState(sx, sy, sz);
                     Block block = state.getBlock();
                     if (targets.contains(block)) {
                        int x = minX + sx;
                        int y = baseY + sy;
                        int z = minZ + sz;
                        out.add(new EspChunkWorker.EspBox(new AABB(new BlockPos(x, y, z)), new Vec3(x + 0.5, y + 0.5, z + 0.5), colors.getInt(block)));
                     }
                  }
               }
            }
         }
      }
   }

   static void init(Module xray, ClientLevel level, Player player, OreSimEspScanner.Inner_DataUtil sink) {
      if (sink != null && init_Z(xray) && level != null && player != null) {
         OreSimEspScanner.Inner_StreamUtil selection = init_ak_StreamUtil(xray);
         if (!selection.init_Set().isEmpty()) {
            BlockPos center = player.blockPosition();
            Vec3 eyes = player.getEyePosition();
            double maxDistSq = 25.0;
            MutableBlockPos cursor = new MutableBlockPos();
            List<BlockPos> candidates = null;
            int matched = 0;
            int touching = 0;

            for (int dx = -5; dx <= 5; dx++) {
               for (int dy = -5; dy <= 5; dy++) {
                  for (int dz = -5; dz <= 5; dz++) {
                     int x = center.getX() + dx;
                     int y = center.getY() + dy;
                     int z = center.getZ() + dz;
                     double cx = x + 0.5 - eyes.x;
                     double cy = y + 0.5 - eyes.y;
                     double cz = z + 0.5 - eyes.z;
                     if (!(cx * cx + cy * cy + cz * cz > maxDistSq)) {
                        cursor.set(x, y, z);
                        BlockState state = level.getBlockState(cursor);
                        if (selection.init_Set().contains(state.getBlock())) {
                           matched++;
                           if (init(level, cursor)) {
                              touching++;
                              if (candidates == null) {
                                 candidates = new ArrayList<>();
                              }

                              candidates.add(cursor.immutable());
                           }
                        }
                     }
                  }
               }
            }

            tick = matched;
            run = touching;
            if (candidates == null) {
               get = 0;
               set = 0;
            } else {
               LongOpenHashSet predicted = new LongOpenHashSet();
               autismclient.util.oresim.OreSimEngine.init_V_BlockPos_2(center, 6, predicted);
               int emitted = 0;
               int skippedPredicted = 0;
               int[] rays = new int[]{0};

               for (BlockPos pos : candidates) {
                  if (emitted >= 64) {
                     break;
                  }

                  if (predicted.contains(pos.asLong())) {
                     skippedPredicted++;
                  } else if (init(level, player, eyes, pos, rays)) {
                     sink.accept(pos, level.getBlockState(pos));
                     emitted++;
                  }
               }

               get = skippedPredicted;
               set = emitted;
            }
         }
      }
   }

   public static String debugCounters() {
      return "matched=" + tick + " touchingAir=" + run + " alreadyPredicted=" + get + " drawn=" + set;
   }

   private static boolean init(ClientLevel level, BlockPos pos) {
      MutableBlockPos neighbour = new MutableBlockPos();

      for (Direction direction : Direction.values()) {
         neighbour.setWithOffset(pos, direction);
         if (level.getBlockState(neighbour).isAir()) {
            return true;
         }
      }

      return false;
   }

   private static boolean init(ClientLevel level, Player player, Vec3 eyes, BlockPos pos, boolean empty, int[] rays) {
      if (!empty) {
         return init(level, player, eyes, pos, rays);
      }

      rays[0]++;
      BlockHitResult hit = level.clip(new ClipContext(eyes, Vec3.atCenterOf(pos), net.minecraft.world.level.ClipContext.Block.VISUAL, Fluid.NONE, player));
      return hit.getType() == Type.MISS;
   }

   private static boolean init(ClientLevel level, Player player, Vec3 eyes, BlockPos pos, int[] rays) {
      if (init(level, player, eyes, Vec3.atCenterOf(pos), pos, rays)) {
         return true;
      }

      for (Direction direction : Direction.values()) {
         double faceX = pos.getX() + 0.5 + direction.getStepX() * 0.5;
         double faceY = pos.getY() + 0.5 + direction.getStepY() * 0.5;
         double faceZ = pos.getZ() + 0.5 + direction.getStepZ() * 0.5;
         if (!((eyes.x - faceX) * direction.getStepX() + (eyes.y - faceY) * direction.getStepY() + (eyes.z - faceZ) * direction.getStepZ() <= 0.0)
            && init(level, player, eyes, new Vec3(faceX, faceY, faceZ), pos, rays)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init(ClientLevel level, Player player, Vec3 from, Vec3 to, BlockPos target, int[] rays) {
      rays[0]++;
      BlockHitResult hit = level.clip(new ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.VISUAL, Fluid.NONE, player));
      return hit.getType() == Type.MISS || hit.getBlockPos().equals(target);
   }

   private static AABB init(long packed) {
      return new AABB(new BlockPos(BlockPos.getX(packed), BlockPos.getY(packed), BlockPos.getZ(packed)));
   }

   static String statusLine(Module xray) {
      if (xray != null && xray.isEnabled() && init_Z(xray)) {
         boolean shaderCullMode = !load_Z(xray) && am.pop();
         Long parsedSeed = init_Long(xray);
         if (init_BufferHelper_DataU.init_BufferHelper_Strea() == autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil.INVALID) {
            return "invalid seed";
         } else if (parsedSeed == null) {
            return "no seed";
         } else if (init_I(xray) == 0) {
            return "no ores";
         } else if (autismclient.util.oresim.OreSimEngine.init_StreamUtil_BufferH() == autismclient.util.oresim.OreSimEngine.Inner_BufferHelper.UNSUPPORTED_DIMENSION) {
            return "unsupported dimension";
         } else if (autismclient.util.oresim.OreSimEngine.init_StreamUtil_BufferH() == autismclient.util.oresim.OreSimEngine.Inner_BufferHelper.UNVERIFIED_WORLDGEN) {
            return "unverified worldgen";
         } else if (autismclient.util.oresim.OreSimEngine.read_Z()) {
            return "worldgen failed";
         } else {
            autismclient.util.oresim.OreSimEngine.Inner_ConfigStore progress = autismclient.util.oresim.OreSimEngine.init_StreamUtil_ConfigS();
            if (progress.load() > 0) {
               return init(progress);
            } else if (shaderCullMode) {
               return "Shader cull mode";
            } else {
               return autismclient.util.oresim.OreSimEngine.init_StreamUtil_BufferH() == autismclient.util.oresim.OreSimEngine.Inner_BufferHelper.LOADING_CONTEXT
                  ? "loading worldgen"
                  : "waiting";
            }
         }
      } else {
         return "";
      }
   }

   static String init(autismclient.util.oresim.OreSimEngine.Inner_ConfigStore progress) {
      return progress.init_I() + "/" + progress.load() + " chunks";
   }

   @FunctionalInterface
   public interface Inner_DataUtil {
      void accept(BlockPos var1, BlockState var2);
   }

   private record Inner_StreamUtil(String init_Str, Set<Block> init_Set, List<String> init_List, int init_I) {
   }
}
