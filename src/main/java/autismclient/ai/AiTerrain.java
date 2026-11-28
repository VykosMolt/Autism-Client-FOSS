package autismclient.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class AiTerrain {
   private AiTerrain() {
   }

   static boolean isLoaded(ClientLevel level, BlockPos position) {
      return level != null
         && level.isInsideBuildHeight(position)
         && level.getChunkSource().getChunk(position.getX() >> 4, position.getZ() >> 4, ChunkStatus.FULL, false) != null;
   }

   static List<BlockPos> findFluidSources(Minecraft mc, Block block, int radius, int vertical) {
      return findFluidSources(mc, block, radius, vertical, false);
   }

   static List<BlockPos> findFluidSources(Minecraft mc, Block block, int radius, int vertical, boolean separateShores) {
      if (mc.player != null && mc.level != null) {
         BlockPos origin = mc.player.blockPosition();
         int low = Math.max(mc.level.getMinY(), origin.getY() - vertical);
         int high = Math.min(mc.level.getMaxY() - 1, origin.getY() + vertical);

         record Inner_DataUtil(LevelChunkSection data, int x, int z, int y, double distance) {
         }

         List<Inner_DataUtil> sections = new ArrayList<>();

         for (int cx = origin.getX() - radius >> 4; cx <= origin.getX() + radius >> 4; cx++) {
            for (int cz = origin.getZ() - radius >> 4; cz <= origin.getZ() + radius >> 4; cz++) {
               if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
                  for (int sy = low >> 4; sy <= high >> 4; sy++) {
                     int index = mc.level.getSectionIndexFromSectionY(sy);
                     LevelChunkSection[] array = chunk.getSections();
                     if (index >= 0 && index < array.length) {
                        LevelChunkSection section = array[index];
                        if (section != null && !section.hasOnlyAir() && section.maybeHas(s -> s.is(block))) {
                           sections.add(new Inner_DataUtil(section, cx, cz, sy, origin.distSqr(new BlockPos(cx * 16 + 8, sy * 16 + 8, cz * 16 + 8))));
                        }
                     }
                  }
               }
            }
         }

         sections.sort(Comparator.comparingDouble(Inner_DataUtil::distance));
         List<BlockPos> result = new ArrayList<>();
         AiTerrain.SiteSampler sites = separateShores ? new AiTerrain.SiteSampler(origin) : null;

         for (Inner_DataUtil section : sections) {
            for (int y = 0; y < 16; y++) {
               for (int z = 0; z < 16; z++) {
                  for (int x = 0; x < 16; x++) {
                     int wy = section.y() * 16 + y;
                     int wx = section.x() * 16 + x;
                     int wz = section.z() * 16 + z;
                     if (wy >= low && wy <= high && Math.abs(wx - origin.getX()) <= radius && Math.abs(wz - origin.getZ()) <= radius) {
                        BlockState state = section.data().getBlockState(x, y, z);
                        if (state.is(block) && state.getFluidState().isSource()) {
                           if (sites == null) {
                              result.add(new BlockPos(wx, wy, wz));
                           } else {
                              sites.record(wx, wy, wz);
                           }
                        }
                     }
                  }
               }
            }

            if (sites == null && result.size() >= 512) {
               break;
            }
         }

         if (sites != null) {
            return sites.closest(64);
         }

         result.sort(Comparator.comparingDouble(origin::distSqr));
         return List.copyOf(result.subList(0, Math.min(512, result.size())));
      } else {
         return List.of();
      }
   }

   static double nearestBlockDistSqr(Minecraft mc, Collection<Block> blocks, int radius) {
      return nearestBlockDistSqr(mc, blocks, radius, pos -> true);
   }

   static double nearestBlockDistSqr(Minecraft mc, Collection<Block> blocks, int radius, Predicate<BlockPos> accept) {
      if (mc.player != null && mc.level != null && !blocks.isEmpty()) {
         Set<Block> wanted = Collections.newSetFromMap(new IdentityHashMap<>());
         wanted.addAll(blocks);
         BlockPos origin = mc.player.blockPosition();
         int vertical = Math.min(radius, 24);
         int minY = Math.max(mc.level.getMinY(), origin.getY() - vertical);
         int maxY = Math.min(mc.level.getMaxY() - 1, origin.getY() + vertical);
         double px = mc.player.getX();
         double py = mc.player.getY();
         double pz = mc.player.getZ();

         record Inner_StreamUtil(LevelChunkSection section, int cx, int cz, int sy, double near) {
         }

         List<Inner_StreamUtil> sections = new ArrayList<>();

         for (int cx = origin.getX() - radius >> 4; cx <= origin.getX() + radius >> 4; cx++) {
            for (int cz = origin.getZ() - radius >> 4; cz <= origin.getZ() + radius >> 4; cz++) {
               if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
                  LevelChunkSection[] array = chunk.getSections();

                  for (int sy = minY >> 4; sy <= maxY >> 4; sy++) {
                     int index = mc.level.getSectionIndexFromSectionY(sy);
                     if (index >= 0 && index < array.length) {
                        LevelChunkSection section = array[index];
                        if (section != null && !section.hasOnlyAir() && section.maybeHas(s -> wanted.contains(s.getBlock()))) {
                           double dx = Math.max(0.0, Math.max((cx << 4) - px, px - ((cx << 4) + 16)));
                           double dy = Math.max(0.0, Math.max((sy << 4) - py, py - ((sy << 4) + 16)));
                           double dz = Math.max(0.0, Math.max((cz << 4) - pz, pz - ((cz << 4) + 16)));
                           sections.add(new Inner_StreamUtil(section, cx, cz, sy, dx * dx + dy * dy + dz * dz));
                        }
                     }
                  }
               }
            }
         }

         sections.sort((a, b) -> Double.compare(a.near(), b.near()));
         double best = 1.7976931348623157E308;

         for (Inner_StreamUtil s : sections) {
            if (s.near() >= best) {
               break;
            }

            for (int y = 0; y < 16; y++) {
               int wy = (s.sy() << 4) + y;
               if (wy >= minY && wy <= maxY) {
                  for (int z = 0; z < 16; z++) {
                     for (int x = 0; x < 16; x++) {
                        int wx = (s.cx() << 4) + x;
                        int wz = (s.cz() << 4) + z;
                        if (Math.abs(wx - origin.getX()) <= radius
                           && Math.abs(wz - origin.getZ()) <= radius
                           && wanted.contains(s.section().getBlockState(x, y, z).getBlock())) {
                           double ddx = wx + 0.5 - px;
                           double ddy = wy + 0.5 - py;
                           double ddz = wz + 0.5 - pz;
                           double d = ddx * ddx + ddy * ddy + ddz * ddz;
                           if (d < best && accept.test(new BlockPos(wx, wy, wz))) {
                              best = d;
                           }
                        }
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return 1.7976931348623157E308;
      }
   }

   static final class SiteSampler {
      final BlockPos origin;
      final Map<Long, BlockPos> nearestPerCell = new HashMap<>();

      SiteSampler(BlockPos origin) {
         this.origin = origin;
      }

      void record(int x, int y, int z) {
         long key = BlockPos.asLong(x >> 3, y >> 3, z >> 3);
         BlockPos old = this.nearestPerCell.get(key);
         double dx = x - this.origin.getX();
         double dy = y - this.origin.getY();
         double dz = z - this.origin.getZ();
         if (old == null || dx * dx + dy * dy + dz * dz < this.origin.distSqr(old)) {
            this.nearestPerCell.put(key, new BlockPos(x, y, z));
         }
      }

      List<BlockPos> closest(int limit) {
         return this.nearestPerCell.values().stream().sorted(Comparator.comparingDouble(this.origin::distSqr)).limit(limit).toList();
      }
   }
}
