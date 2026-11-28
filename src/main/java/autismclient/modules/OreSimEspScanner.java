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
   private static final int MAX_VISIBILITY_RAYS = 96;
   private static final long RESCAN_INTERVAL_NANOS = 400000000L;
   private static final int VISIBILITY_CACHE_PERIOD = 25;
   private static int predictionScanCounter;
   private static int visibilityCacheTicks;
   private static boolean seedParsed;
   private static Long lastSeed;
   private static long nextRescanAt;
   private static final LongOpenHashSet visibilityChecked = new LongOpenHashSet();
   private static final long[] positionBuffer = new long[8192];
   private static final int[] stateBuffer = new int[8192];
   private static final double[] heapDistances = new double[8192];
   private static final EspChunkWorker.EspBox[] heapEntries = new EspChunkWorker.EspBox[8192];
   static final int read_I = -3355444;
   private static final OreSimEspScanner.BlockSelection EMPTY_SELECTION = new OreSimEspScanner.BlockSelection("", Set.of(), List.of(), 0);
   private static volatile OreSimEspScanner.BlockSelection whitelistSelection = EMPTY_SELECTION;
   private static volatile OreSimEspScanner.BlockSelection oreSimSelection = EMPTY_SELECTION;
   private static int modeRevision = -2147483648;
   private static boolean oreSimMode;
   private static boolean espStyle;
   private static int colorRevision = -2147483648;
   private static final Object2IntOpenHashMap<Block> blockMaskCache = new Object2IntOpenHashMap<>();
   private static volatile autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil seedParse = autismclient.util.oresim.OreSimSeedParser.parse(null);
   private static final long PAUSE_RESET_NANOS = TimeUnit.MINUTES.toNanos(2L);
   private static OreSimEspScanner.BlockSelection tableSelection;
   private static boolean[] stateTable = new boolean[0];
   private static volatile int lastMatched;
   private static volatile int lastTouchingAir;
   private static volatile int lastAlreadyPredicted;
   private static volatile int lastDrawn;

   private OreSimEspScanner() {
   }

   private static void refresh(Module xray) {
      int revision = al.init_I();
      if (revision != modeRevision) {
         modeRevision = revision;
         oreSimMode = xray != null && "OreSim".equals(xray.value("mode"));
         espStyle = xray != null && "ESP".equals(xray.value("render-style"));
      }
   }

   static boolean isOreSimMode(Module xray) {
      if (xray == null) {
         return false;
      }

      refresh(xray);
      return oreSimMode;
   }

   static boolean isEspStyle(Module xray) {
      if (xray == null) {
         return false;
      }

      refresh(xray);
      return espStyle;
   }

   static boolean drawsBoxes(Module xray) {
      return xray != null && xray.isEnabled() && isEspStyle(xray);
   }

   static boolean drawsGhosts(Module xray) {
      return xray != null && xray.isEnabled() && isOreSimMode(xray) && !isEspStyle(xray);
   }

   static boolean update(Module xray) {
      return xray != null && xray.isEnabled() && !isEspStyle(xray);
   }

   static float init_F(Module xray) {
      return 0.3F;
   }

   private static OreSimEspScanner.BlockSelection selection(Module xray) {
      if (xray == null) {
         return EMPTY_SELECTION;
      } else if (isOreSimMode(xray)) {
         OreSimEspScanner.BlockSelection next = parseSelection(xray.value("oresim-ores"), oreSimSelection);
         oreSimSelection = next;
         return next;
      } else {
         OreSimEspScanner.BlockSelection next = parseSelection(xray.value("whitelist"), whitelistSelection);
         whitelistSelection = next;
         return next;
      }
   }

   private static OreSimEspScanner.BlockSelection parseSelection(String raw, OreSimEspScanner.BlockSelection cached) {
      String safe = raw == null ? "" : raw;
      if (cached.source().equals(safe)) {
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
                  autismclient.util.oresim.OreBlockCatalog.OreKind family = autismclient.util.oresim.OreBlockCatalog.kindOf(id);
                  if (family != null) {
                     mask |= 1 << family.ordinal();
                  } else if (!nonOre.contains(id)) {
                     nonOre.add(id);
                  }
               }
            }
         }
      }

      return new OreSimEspScanner.BlockSelection(safe, Set.copyOf(blocks), List.copyOf(nonOre), mask);
   }

   static int selectionMask(Module xray) {
      return selection(xray).init_I();
   }

   static boolean init_Z(Module xray, autismclient.util.oresim.OreBlockCatalog.OreKind kind) {
      return kind != null && (selection(xray).init_I() & 1 << kind.ordinal()) != 0;
   }

   static List<String> selectedIds(Module xray) {
      if (xray == null) {
         return List.of();
      }

      OreSimEspScanner.BlockSelection next = parseSelection(xray.value("whitelist"), whitelistSelection);
      whitelistSelection = next;
      return next.entries();
   }

   static boolean isSelected(Module xray, String id) {
      return selectedIds(xray).contains(id);
   }

   static int colorFor(Module xray, autismclient.util.oresim.OreBlockCatalog.OreKind kind) {
      return am.init_I_Module(xray, kind.init(), kind.init_I);
   }

   static int blockColor(Module xray, Block block) {
      if (block == null) {
         return -3355444;
      }

      int revision = al.init_I();
      if (revision != colorRevision) {
         colorRevision = revision;
         blockMaskCache.clear();
      }

      int cached = blockMaskCache.getOrDefault(block, -2147483648);
      if (cached != -2147483648) {
         return cached;
      }

      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      autismclient.util.oresim.OreBlockCatalog.OreKind family = id == null ? null : autismclient.util.oresim.OreBlockCatalog.kindOf(id.toString());
      int color = family != null ? colorFor(xray, family) : (id == null ? -3355444 : am.init_I_Module(xray, "block-color-" + id, -3355444));
      blockMaskCache.put(block, color);
      return color;
   }

   static Long seedFor(Module xray) {
      autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil parsed = autismclient.util.oresim.OreSimSeedParser.parse(xray == null ? null : xray.value("oresim-seed"));
      seedParse = parsed;
      return parsed.seed();
   }

   public static autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil seedInfo(Module xray) {
      seedFor(xray);
      return seedParse.status();
   }

   public static Long seed(Module xray) {
      return seedFor(xray);
   }

   public static int targetCount(Module xray) {
      return selection(xray).blocks().size();
   }

   public static int read_I(Module xray) {
      return selectionMask(xray);
   }

   static int radius(Module xray) {
      int configured = xray == null ? 3 : xray.integer("oresim-radius");
      return Math.max(1, Math.min(configured, 8));
   }

   static void init(Module xray, ClientLevel level, Player player) {
      if (isOreSimMode(xray) && xray != null && xray.isEnabled() && level != null && player != null) {
         autismclient.util.oresim.OreSimEngine.resume();
         Long parsedSeed = seedFor(xray);
         int mask = selectionMask(xray);
         long now = System.nanoTime();
         if (!seedParsed || !Objects.equals(parsedSeed, lastSeed)) {
            seedParsed = true;
            lastSeed = parsedSeed;
            nextRescanAt = now + 400000000L;
            autismclient.util.oresim.OreSimEngine.reset();
         } else if (parsedSeed != null && mask != 0 && now >= nextRescanAt) {
            autismclient.util.oresim.OreSimEngine.init(level, player.blockPosition(), parsedSeed, radius(xray), mask);
            if (++predictionScanCounter >= 4) {
               predictionScanCounter = 0;
               checkNearbyPredictions(xray, level, player);
            }
         } else {
            autismclient.util.oresim.OreSimEngine.reset();
         }
      } else {
         autismclient.util.oresim.OreSimEngine.pause();
      }
   }

   public static void init_V() {
      if (autismclient.util.oresim.OreSimEngine.pausedLongerThan(PAUSE_RESET_NANOS)) {
         seedParsed = false;
         lastSeed = null;
         nextRescanAt = 0L;
      }
   }

   private static void checkNearbyPredictions(Module xray, ClientLevel level, Player player) {
      OreSimEspScanner.BlockSelection selection = selection(xray);
      if (!selection.blocks().isEmpty()) {
         Vec3 eyes = player.getEyePosition();
         double maxDistSq = 25.0;
         MutableBlockPos cursor = new MutableBlockPos();
         int[] rays = new int[]{0};
         autismclient.util.oresim.OreSimEngine.forEachPosition(player.blockPosition(), 5, (stateId, packed) -> {
            BlockState predicted = autismclient.util.oresim.OreBlockCatalog.StateRegistry.stateById(stateId);
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
                     boolean open = actual.isAir() || touchesAir(level, cursor);
                     if (!open) {
                        return false;
                     } else if (!visibilityChecked.add(packed)) {
                        return false;
                     } else {
                        return rays[0] >= 96 ? false : isVisible(level, player, eyes, cursor.immutable(), actual.isAir(), rays);
                     }
                  }
               }
            }
         });
         if (++visibilityCacheTicks >= 25 || visibilityChecked.size() > 4096) {
            visibilityCacheTicks = 0;
            visibilityChecked.clear();
         }
      }
   }

   static long init(Module xray, ClientLevel level) {
      return isOreSimMode(xray) ? (long)autismclient.util.oresim.OreSimEngine.revision() << 1 | 1L : (long)EspChunkWorker.revision() << 1;
   }

   private static boolean matchesState(OreSimEspScanner.BlockSelection selection, int stateId) {
      BlockState state = autismclient.util.oresim.OreBlockCatalog.StateRegistry.stateById(stateId);
      return state != null && selection.blocks().contains(state.getBlock());
   }

   private static boolean[] stateTableFor(OreSimEspScanner.BlockSelection selection) {
      int count = autismclient.util.oresim.OreBlockCatalog.StateRegistry.count();
      boolean[] table = stateTable;
      if (selection == tableSelection && table.length == count) {
         return table;
      }

      table = new boolean[count];

      for (int id = 0; id < count; id++) {
         table[id] = matchesState(selection, id);
      }

      tableSelection = selection;
      stateTable = table;
      return table;
   }

   private static IntPredicate statePredicate(OreSimEspScanner.BlockSelection selection) {
      boolean[] table = stateTableFor(selection);
      return stateId -> stateId >= 0 && stateId < table.length && table[stateId];
   }

   static int init(Module xray, Player player, long[] outPos, int[] outState) {
      if (xray != null && xray.isEnabled() && player != null && isOreSimMode(xray)) {
         OreSimEspScanner.BlockSelection selection = selection(xray);
         if (selection.blocks().isEmpty()) {
            return 0;
         }

         Vec3 eyes = player.getEyePosition();
         return autismclient.util.oresim.OreSimEngine.nearest(eyes.x, eyes.y, eyes.z, radius(xray), statePredicate(selection), 8192, outPos, outState);
      } else {
         return 0;
      }
   }

   static void init(Module xray, ClientLevel level, Player player, BiConsumer<AABB, Integer> emit) {
      if (emit != null && xray != null && xray.isEnabled() && level != null && player != null) {
         if (isOreSimMode(xray)) {
            emitPredicted(xray, player, emit);
         } else {
            emitScanned(xray, level, player, emit);
         }
      }
   }

   private static void emitPredicted(Module xray, Player player, BiConsumer<AABB, Integer> emit) {
      OreSimEspScanner.BlockSelection selection = selection(xray);
      if (!selection.blocks().isEmpty()) {
         Vec3 eyes = player.getEyePosition();
         int count = autismclient.util.oresim.OreSimEngine.nearest(eyes.x, eyes.y, eyes.z, radius(xray), statePredicate(selection), 8192, positionBuffer, stateBuffer);
         int[] colors = new int[autismclient.util.oresim.OreBlockCatalog.OreKind.values().length];

         for (autismclient.util.oresim.OreBlockCatalog.OreKind kind : autismclient.util.oresim.OreBlockCatalog.OreKind.values()) {
            colors[kind.ordinal()] = colorFor(xray, kind);
         }

         for (int i = 0; i < count; i++) {
            autismclient.util.oresim.OreBlockCatalog.OreKind kind = autismclient.util.oresim.OreBlockCatalog.StateRegistry.kindById(stateBuffer[i]);
            emit.accept(boxAt(positionBuffer[i]), kind == null ? -3355444 : colors[kind.ordinal()]);
         }
      }
   }

   private static void emitScanned(Module xray, ClientLevel level, Player player, BiConsumer<AABB, Integer> emit) {
      OreSimEspScanner.BlockSelection selection = selection(xray);
      if (!selection.blocks().isEmpty()) {
         EspChunkWorker.setLevel(level);
         Object2IntMap<Block> colors = new Object2IntOpenHashMap<>();
         colors.defaultReturnValue(-3355444);
         int colorsHash = 0;

         for (Block block : selection.blocks()) {
            int color = blockColor(xray, block);
            colors.put(block, color);
            colorsHash = colorsHash * 31 + color;
         }

         int chunkRadius = am.init_I();
         int playerBlockY = player.getBlockY();
         String stamp = selection.source() + "|" + colorsHash + "|b" + (playerBlockY >> 4);
         long gameTime = level.getGameTime();
         ClientChunkCache chunks = level.getChunkSource();
         int playerChunkX = player.chunkPosition().x();
         int playerChunkZ = player.chunkPosition().z();
         Vec3 eyes = player.getEyePosition();
         double[] heapDist = heapDistances;
         EspChunkWorker.EspBox[] heapEntry = heapEntries;
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
                        for (EspChunkWorker.EspBox entry : EspChunkWorker.oreSim
                           .initAsync(chunk, gameTime, stamp, (scanned, out) -> scanChunk(scanned, selection, colors, playerBlockY, out))) {
                           Vec3 center = entry.tracer();
                           double px = center.x - eyes.x;
                           double py = center.y - eyes.y;
                           double pz = center.z - eyes.z;
                           double distSq = px * px + py * py + pz * pz;
                           if (size < 8192) {
                              heapDist[size] = distSq;
                              heapEntry[size] = entry;
                              siftUp(heapDist, heapEntry, size++);
                           } else if (distSq < heapDist[0]) {
                              heapDist[0] = distSq;
                              heapEntry[0] = entry;
                              siftDown(heapDist, heapEntry, size);
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

   private static void siftUp(double[] dist, EspChunkWorker.EspBox[] entries, int index) {
      while (index > 0) {
         int parent = index - 1 >>> 1;
         if (!(dist[parent] >= dist[index])) {
            swap(dist, entries, parent, index);
            index = parent;
            continue;
         }
         break;
      }
   }

   private static void siftDown(double[] dist, EspChunkWorker.EspBox[] entries, int size) {
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

         swap(dist, entries, index, largest);
         index = largest;
      }
   }

   private static void swap(double[] dist, EspChunkWorker.EspBox[] entries, int a, int b) {
      double d = dist[a];
      dist[a] = dist[b];
      dist[b] = d;
      EspChunkWorker.EspBox e = entries[a];
      entries[a] = entries[b];
      entries[b] = e;
   }

   private static void scanChunk(LevelChunk chunk, OreSimEspScanner.BlockSelection selection, Object2IntMap<Block> colors, int playerBlockY, List<EspChunkWorker.EspBox> out) {
      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();
      int playerBand = playerBlockY >> 4;
      Set<Block> targets = selection.blocks();
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

   static void init(Module xray, ClientLevel level, Player player, OreSimEspScanner.OreSink sink) {
      if (sink != null && isOreSimMode(xray) && level != null && player != null) {
         OreSimEspScanner.BlockSelection selection = selection(xray);
         if (!selection.blocks().isEmpty()) {
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
                        if (selection.blocks().contains(state.getBlock())) {
                           matched++;
                           if (touchesAir(level, cursor)) {
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

            lastMatched = matched;
            lastTouchingAir = touching;
            if (candidates == null) {
               lastAlreadyPredicted = 0;
               lastDrawn = 0;
            } else {
               LongOpenHashSet predicted = new LongOpenHashSet();
               autismclient.util.oresim.OreSimEngine.collectPositions(center, 6, predicted);
               int emitted = 0;
               int skippedPredicted = 0;
               int[] rays = new int[]{0};

               for (BlockPos pos : candidates) {
                  if (emitted >= 64) {
                     break;
                  }

                  if (predicted.contains(pos.asLong())) {
                     skippedPredicted++;
                  } else if (isBlockVisible(level, player, eyes, pos, rays)) {
                     sink.accept(pos, level.getBlockState(pos));
                     emitted++;
                  }
               }

               lastAlreadyPredicted = skippedPredicted;
               lastDrawn = emitted;
            }
         }
      }
   }

   public static String debugCounters() {
      return "matched=" + lastMatched + " touchingAir=" + lastTouchingAir + " alreadyPredicted=" + lastAlreadyPredicted + " drawn=" + lastDrawn;
   }

   private static boolean touchesAir(ClientLevel level, BlockPos pos) {
      MutableBlockPos neighbour = new MutableBlockPos();

      for (Direction direction : Direction.values()) {
         neighbour.setWithOffset(pos, direction);
         if (level.getBlockState(neighbour).isAir()) {
            return true;
         }
      }

      return false;
   }

   private static boolean isVisible(ClientLevel level, Player player, Vec3 eyes, BlockPos pos, boolean empty, int[] rays) {
      if (!empty) {
         return isBlockVisible(level, player, eyes, pos, rays);
      }

      rays[0]++;
      BlockHitResult hit = level.clip(new ClipContext(eyes, Vec3.atCenterOf(pos), net.minecraft.world.level.ClipContext.Block.VISUAL, Fluid.NONE, player));
      return hit.getType() == Type.MISS;
   }

   private static boolean isBlockVisible(ClientLevel level, Player player, Vec3 eyes, BlockPos pos, int[] rays) {
      if (rayReaches(level, player, eyes, Vec3.atCenterOf(pos), pos, rays)) {
         return true;
      }

      for (Direction direction : Direction.values()) {
         double faceX = pos.getX() + 0.5 + direction.getStepX() * 0.5;
         double faceY = pos.getY() + 0.5 + direction.getStepY() * 0.5;
         double faceZ = pos.getZ() + 0.5 + direction.getStepZ() * 0.5;
         if (!((eyes.x - faceX) * direction.getStepX() + (eyes.y - faceY) * direction.getStepY() + (eyes.z - faceZ) * direction.getStepZ() <= 0.0)
            && rayReaches(level, player, eyes, new Vec3(faceX, faceY, faceZ), pos, rays)) {
            return true;
         }
      }

      return false;
   }

   private static boolean rayReaches(ClientLevel level, Player player, Vec3 from, Vec3 to, BlockPos target, int[] rays) {
      rays[0]++;
      BlockHitResult hit = level.clip(new ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.VISUAL, Fluid.NONE, player));
      return hit.getType() == Type.MISS || hit.getBlockPos().equals(target);
   }

   private static AABB boxAt(long packed) {
      return new AABB(new BlockPos(BlockPos.getX(packed), BlockPos.getY(packed), BlockPos.getZ(packed)));
   }

   static String statusLine(Module xray) {
      if (xray != null && xray.isEnabled() && isOreSimMode(xray)) {
         boolean shaderCullMode = !isEspStyle(xray) && am.pop();
         Long parsedSeed = seedFor(xray);
         if (seedParse.status() == autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil.INVALID) {
            return "invalid seed";
         } else if (parsedSeed == null) {
            return "no seed";
         } else if (selectionMask(xray) == 0) {
            return "no ores";
         } else if (autismclient.util.oresim.OreSimEngine.status() == autismclient.util.oresim.OreSimEngine.EngineStatus.UNSUPPORTED_DIMENSION) {
            return "unsupported dimension";
         } else if (autismclient.util.oresim.OreSimEngine.status() == autismclient.util.oresim.OreSimEngine.EngineStatus.UNVERIFIED_WORLDGEN) {
            return "unverified worldgen";
         } else if (autismclient.util.oresim.OreSimEngine.hasError()) {
            return "worldgen failed";
         } else {
            autismclient.util.oresim.OreSimEngine.GenerationProgress progress = autismclient.util.oresim.OreSimEngine.progress();
            if (progress.load() > 0) {
               return progressText(progress);
            } else if (shaderCullMode) {
               return "Shader cull mode";
            } else {
               return autismclient.util.oresim.OreSimEngine.status() == autismclient.util.oresim.OreSimEngine.EngineStatus.LOADING_CONTEXT
                  ? "loading worldgen"
                  : "waiting";
            }
         }
      } else {
         return "";
      }
   }

   static String progressText(autismclient.util.oresim.OreSimEngine.GenerationProgress progress) {
      return progress.done() + "/" + progress.load() + " chunks";
   }

   @FunctionalInterface
   public interface OreSink {
      void accept(BlockPos var1, BlockState var2);
   }

   private record BlockSelection(String source, Set<Block> blocks, List<String> entries, int init_I) {
   }
}
