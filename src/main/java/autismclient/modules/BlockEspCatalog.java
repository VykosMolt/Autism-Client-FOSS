package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BlockEspCatalog {
   private static volatile BlockEspCatalog.Inner_DataUtil init_ad_DataUtil = new BlockEspCatalog.Inner_DataUtil("", Set.of());
   private static volatile String init_Str;
   private static volatile List<String> init_List = List.of();
   static final int init_I = -855688389;
   private static final int load = 2048;
   private static final Set<Block> init_Set = Set.of(Blocks.BARRIER);
   private static final int read = -50373;

   private BlockEspCatalog() {
   }

   static List<String> init(Module module) {
      String value = module == null ? "" : module.value("blocks");
      if (value == null) {
         value = "";
      }

      if (!value.equals(init_Str)) {
         List<String> ids = new ArrayList<>();

         for (String raw : value.split("\\|")) {
            String id = init_Str(raw);
            if (!id.isEmpty() && !ids.contains(id)) {
               ids.add(id);
            }
         }

         init_List = List.copyOf(ids);
         init_Str = value;
      }

      return init_List;
   }

   static boolean init_Z(Module module, String blockId) {
      return blockId != null && init(module).contains(blockId.toLowerCase(Locale.ROOT));
   }

   static int init_I(String blockId) {
      Block block = init_Block(blockId);
      return block == null ? -855688389 : -872415232 | am.init(block);
   }

   static int init_I(Module module, String blockId) {
      return am.init_I_Module(module, "color-" + blockId, init_I(blockId));
   }

   static Block init_Block(String blockId) {
      if (blockId != null && !blockId.isBlank()) {
         Identifier parsed = Identifier.tryParse(init_Str(blockId));
         if (parsed == null) {
            return null;
         }

         Block block = BuiltInRegistries.BLOCK.getOptional(parsed).orElse(Blocks.AIR);
         return block == Blocks.AIR ? null : block;
      } else {
         return null;
      }
   }

   static void init(Module module, ClientLevel level, Player player, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (module != null && level != null && player != null && (boxEmit != null || traceEmit != null)) {
         BlockEspCatalog.Inner_DataUtil selection = init_ad_DataUtil(module.value("blocks"));
         if (!selection.init_Set().isEmpty()) {
            int maxTargets = init_I_Str(module.value("max-targets"), 1024, 64, 8192);
            Map<Block, Integer> colors = new IdentityHashMap<>();
            StringBuilder stamp = new StringBuilder(selection.init_Str());

            for (Block block : selection.init_Set()) {
               Identifier id = BuiltInRegistries.BLOCK.getKey(block);
               int color = init_I(module, id.toString());
               colors.put(block, color);
               stamp.append('|').append(Integer.toHexString(color));
            }

            Set<Block> targets = selection.init_Set();
            if (!AutismLiteVariant.enabled() && BedEspModule.init_Z_3()) {
               targets = new LinkedHashSet<>(targets);
               targets.removeIf(blockx -> blockx instanceof BedBlock);
               stamp.append("|bed-esp");
            }

            if (!AutismLiteVariant.enabled() && EndPortalModule.init_Z_5()) {
               targets = new LinkedHashSet<>(targets);
               targets.remove(Blocks.END_PORTAL_FRAME);
               stamp.append("|end-portal");
            }

            init(level, player, targets, stamp.toString(), maxTargets, colors, EspChunkWorker.init_af, boxEmit, traceEmit);
         }
      }
   }

   static void load(Module module, ClientLevel level, Player player, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (module != null && level != null && player != null && (boxEmit != null || traceEmit != null)) {
         int maxTargets = init_I_Str(module.value("max-targets"), 1024, 64, 8192);
         init(level, player, init_Set, "minecraft:barrier", maxTargets, Map.of(Blocks.BARRIER, -50373), EspChunkWorker.update, boxEmit, traceEmit);
      }
   }

   private static void init(
      ClientLevel level,
      Player player,
      Set<Block> targets,
      String stampKey,
      int maxTargets,
      Map<Block, Integer> colors,
      EspChunkWorker cache,
      BiConsumer<AABB, Integer> boxEmit,
      BiConsumer<Vec3, Integer> traceEmit
   ) {
      int chunkRadius = am.init_I();
      double maxDistSq = 2.0 * (chunkRadius * 16.0 + 16.0) * (chunkRadius * 16.0 + 16.0);
      EspChunkWorker.init_V_Obj(level);
      int playerBlockY = player.getBlockY();
      String stamp = stampKey + "|b" + (playerBlockY >> 3);
      long gameTime = level.getGameTime();
      ClientChunkCache chunks = level.getChunkSource();
      Vec3 playerPos = player.position();
      int playerChunkX = player.chunkPosition().x();
      int playerChunkZ = player.chunkPosition().z();
      int emitted = 0;

      for (int radius = 0; radius <= chunkRadius && emitted < maxTargets; radius++) {
         for (int dx = -radius; dx <= radius && emitted < maxTargets; dx++) {
            for (int dz = -radius; dz <= radius && emitted < maxTargets; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                  int chunkX = playerChunkX + dx;
                  int chunkZ = playerChunkZ + dz;
                  if (!(init(playerPos, chunkX, chunkZ) > maxDistSq)) {
                     LevelChunk chunk = chunks.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                     if (chunk != null) {
                        List<EspChunkWorker.Inner_CachePool> entries = cache.initAsync(
                           chunk, gameTime, stamp, (scanned, out) -> init(level, scanned, targets, colors, playerBlockY, out)
                        );
                        if (entries.size() > 1) {
                           entries = new ArrayList<>(entries);
                           entries.sort(Comparator.comparingDouble(e -> init_D_Vec3(playerPos, e.init_Vec3().x, e.init_Vec3().y, e.init_Vec3().z)));
                        }

                        for (EspChunkWorker.Inner_CachePool entry : entries) {
                           if (emitted >= maxTargets) {
                              break;
                           }

                           Vec3 trace = entry.init_Vec3();
                           if (!(init_D_Vec3(playerPos, trace.x, trace.y, trace.z) > maxDistSq)) {
                              if (boxEmit != null) {
                                 boxEmit.accept(entry.init_AABB(), entry.init_I());
                              }

                              if (traceEmit != null) {
                                 traceEmit.accept(trace, entry.init_I());
                              }

                              emitted++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static double init(Vec3 playerPos, int chunkX, int chunkZ) {
      double minX = chunkX << 4;
      double minZ = chunkZ << 4;
      double dx = playerPos.x < minX ? minX - playerPos.x : (playerPos.x > minX + 16.0 ? playerPos.x - minX - 16.0 : 0.0);
      double dz = playerPos.z < minZ ? minZ - playerPos.z : (playerPos.z > minZ + 16.0 ? playerPos.z - minZ - 16.0 : 0.0);
      return dx * dx + dz * dz;
   }

   private static void init(ClientLevel level, LevelChunk chunk, Set<Block> targets, Map<Block, Integer> colors, int playerBlockY, List<EspChunkWorker.Inner_CachePool> out) {
      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();
      int playerBand = playerBlockY >> 4;
      int playerSy = playerBlockY & 15;
      MutableBlockPos mutable = new MutableBlockPos();
      Predicate<BlockState> isTarget = statex -> targets.contains(statex.getBlock());
      Integer[] sectionOrder = new Integer[sections.length];

      for (int i = 0; i < sectionOrder.length; i++) {
         sectionOrder[i] = i;
      }

      Arrays.sort(sectionOrder, Comparator.comparingInt(ix -> Math.abs(chunk.getSectionYFromSectionIndex(ix) - playerBand)));
      Integer[] var31 = sectionOrder;
      int var15 = var31.length;

      for (int var16 = 0; var16 < var15; var16++) {
         int sectionIndex = var31[var16];
         if (out.size() >= 2048) {
            break;
         }

         LevelChunkSection section = sections[sectionIndex];
         if (section != null && !section.hasOnlyAir() && section.maybeHas(isTarget)) {
            int band = chunk.getSectionYFromSectionIndex(sectionIndex);
            int baseY = band << 4;

            for (int sy : init(band, playerBand, playerSy)) {
               if (out.size() >= 2048) {
                  break;
               }

               int y = baseY + sy;
               if (!chunk.isOutsideBuildHeight(y)) {
                  for (int sx = 0; sx < 16 && out.size() < 2048; sx++) {
                     int x = minX + sx;

                     for (int sz = 0; sz < 16 && out.size() < 2048; sz++) {
                        int z = minZ + sz;
                        BlockState state = section.getBlockState(sx, sy, sz);
                        if (targets.contains(state.getBlock())) {
                           mutable.set(x, y, z);
                           out.add(
                              new EspChunkWorker.Inner_CachePool(
                                 init(mutable, state), new Vec3(x + 0.5, y + 0.5, z + 0.5), colors.getOrDefault(state.getBlock(), -855688389)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int[] init(int band, int playerBand, int playerSy) {
      int[] order = new int[16];
      if (band != playerBand) {
         boolean below = band < playerBand;

         for (int i = 0; i < 16; i++) {
            order[i] = below ? 15 - i : i;
         }

         return order;
      } else {
         int index = 0;

         for (int d = 0; d < 16 && index < 16; d++) {
            if (playerSy + d < 16) {
               order[index++] = playerSy + d;
            }

            if (d > 0 && playerSy - d >= 0) {
               order[index++] = playerSy - d;
            }
         }

         return order;
      }
   }

   private static AABB init(BlockPos pos, BlockState state) {
      if (state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
         return new AABB(pos);
      }

      try {
         VoxelShape shape = state.getShape(EmptyBlockGetter.INSTANCE, pos);
         return shape != null && !shape.isEmpty() ? shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()) : new AABB(pos);
      } catch (Throwable ignored) {
         return new AABB(pos);
      }
   }

   private static BlockEspCatalog.Inner_DataUtil init_ad_DataUtil(String value) {
      String safe = value == null ? "" : value;
      BlockEspCatalog.Inner_DataUtil cached = init_ad_DataUtil;
      if (cached.init_Str().equals(safe)) {
         return cached;
      }

      Set<Block> blocks = new LinkedHashSet<>();

      for (String raw : safe.split("\\|")) {
         String id = init_Str(raw);
         if (!id.isEmpty()) {
            Identifier parsed = Identifier.tryParse(id);
            if (parsed != null) {
               Block block = BuiltInRegistries.BLOCK.getOptional(parsed).orElse(Blocks.AIR);
               if (block != Blocks.AIR && block != Blocks.BARRIER) {
                  blocks.add(block);
               }
            }
         }
      }

      BlockEspCatalog.Inner_DataUtil next = new BlockEspCatalog.Inner_DataUtil(safe, Set.copyOf(blocks));
      init_ad_DataUtil = next;
      return next;
   }

   private static String init_Str(String raw) {
      if (raw == null) {
         return "";
      } else {
         String value = raw.trim().toLowerCase(Locale.ROOT);
         if (value.isEmpty()) {
            return "";
         } else {
            return value.contains(":") ? value : "minecraft:" + value;
         }
      }
   }

   private static double init_D_Vec3(Vec3 from, double x, double y, double z) {
      double dx = from.x - x;
      double dy = from.y - y;
      double dz = from.z - z;
      return dx * dx + dy * dy + dz * dz;
   }

   private static int init_I_Str(String value, int fallback, int min, int max) {
      try {
         return Math.max(min, Math.min(max, Integer.parseInt(value)));
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private record Inner_DataUtil(String init_Str, Set<Block> init_Set) {
   }
}
