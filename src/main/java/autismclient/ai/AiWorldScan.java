package autismclient.ai;

import autismclient.mixin.accessor.AutismBaseSpawnerAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;

final class AiWorldScan {
   private AiWorldScan() {
   }

   static List<int[]> init(int radiusChunks) {
      List<int[]> out = new ArrayList<>();

      for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
         for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
            out.add(new int[]{dx, dz, dx * dx + dz * dz});
         }
      }

      out.sort(Comparator.comparingInt(c -> c[2]));
      return out;
   }

   static List<BlockPos> init(Predicate<BlockState> target, int radiusChunks, int perChunk, int cap) {
      List<BlockPos> indexed = AiWorldMemory.init(target, radiusChunks, perChunk, cap);
      if (!indexed.isEmpty()) {
         return indexed;
      }

      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      List<BlockPos> out = new ArrayList<>();
      if (level != null && mc.player != null) {
         int pcx = mc.player.blockPosition().getX() >> 4;
         int pcz = mc.player.blockPosition().getZ() >> 4;

         for (int[] c : init(radiusChunks)) {
            if (out.size() >= cap) {
               break;
            }

            LevelChunk chunk = level.getChunkSource().getChunk(pcx + c[0], pcz + c[1], ChunkStatus.FULL, false);
            if (chunk != null) {
               int found = 0;
               LevelChunkSection[] sections = chunk.getSections();
               int minX = chunk.getPos().getMinBlockX();
               int minZ = chunk.getPos().getMinBlockZ();

               for (int i = 0; i < sections.length && found < perChunk && out.size() < cap; i++) {
                  LevelChunkSection section = sections[i];
                  if (section != null && !section.hasOnlyAir() && section.maybeHas(target)) {
                     int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

                     for (int sy = 0; sy < 16 && found < perChunk; sy++) {
                        for (int sx = 0; sx < 16 && found < perChunk; sx++) {
                           for (int sz = 0; sz < 16 && found < perChunk; sz++) {
                              if (target.test(section.getBlockState(sx, sy, sz))) {
                                 out.add(new BlockPos(minX + sx, baseY + sy, minZ + sz));
                                 found++;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         BlockPos feet = mc.player.blockPosition();
         out.sort(Comparator.comparingDouble(p -> p.distSqr(feet)));
         return out;
      } else {
         return out;
      }
   }

   static List<BlockPos> init(int radiusChunks, int perChunk, int cap) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      List<BlockPos> out = new ArrayList<>();
      if (level != null && mc.player != null) {
         Predicate<BlockState> lava = s -> s.getBlock() == Blocks.LAVA && s.getFluidState().isSource();
         int pcx = mc.player.blockPosition().getX() >> 4;
         int pcz = mc.player.blockPosition().getZ() >> 4;
         MutableBlockPos above = new MutableBlockPos();

         for (int[] c : init(radiusChunks)) {
            if (out.size() >= cap) {
               break;
            }

            LevelChunk chunk = level.getChunkSource().getChunk(pcx + c[0], pcz + c[1], ChunkStatus.FULL, false);
            if (chunk != null) {
               int found = 0;
               LevelChunkSection[] sections = chunk.getSections();
               int minX = chunk.getPos().getMinBlockX();
               int minZ = chunk.getPos().getMinBlockZ();

               for (int i = sections.length - 1; i >= 0 && found < perChunk && out.size() < cap; i--) {
                  LevelChunkSection section = sections[i];
                  if (section != null && !section.hasOnlyAir() && section.maybeHas(lava)) {
                     int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

                     for (int sy = 15; sy >= 0 && found < perChunk; sy--) {
                        for (int sx = 0; sx < 16 && found < perChunk; sx++) {
                           for (int sz = 0; sz < 16 && found < perChunk; sz++) {
                              if (lava.test(section.getBlockState(sx, sy, sz)) && chunk.getBlockState(above.set(minX + sx, baseY + sy + 1, minZ + sz)).isAir()) {
                                 out.add(new BlockPos(minX + sx, baseY + sy, minZ + sz));
                                 found++;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         BlockPos feet = mc.player.blockPosition();
         out.sort(Comparator.comparingDouble(p -> p.distSqr(feet)));
         return out;
      } else {
         return out;
      }
   }

   static boolean init_Z_ClientLevel_BlockPos(ClientLevel level, BlockPos pos) {
      FluidState fluid = level.getFluidState(pos);
      return fluid.isSource() && fluid.is(FluidTags.LAVA) && level.getBlockState(pos.above()).isAir();
   }

   static BlockPos init_BlockPos_Predicate_I(Predicate<BlockState> target, int radiusChunks) {
      List<BlockPos> hits = init(target, radiusChunks, 4, 64);
      return hits.isEmpty() ? null : hits.get(0);
   }

   static List<BlockPos> init_List_Str_I(String mobId, int radiusChunks) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      List<BlockPos> out = new ArrayList<>();
      if (level != null && mc.player != null) {
         int pcx = mc.player.blockPosition().getX() >> 4;
         int pcz = mc.player.blockPosition().getZ() >> 4;

         for (int[] c : init(radiusChunks)) {
            LevelChunk chunk = level.getChunkSource().getChunk(pcx + c[0], pcz + c[1], ChunkStatus.FULL, false);
            if (chunk != null) {
               for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                  BlockEntity be = entry.getValue();
                  if (be instanceof SpawnerBlockEntity spawner && mobId.equals(init(spawner))) {
                     out.add(entry.getKey());
                  }
               }
            }
         }

         BlockPos feet = mc.player.blockPosition();
         out.sort(Comparator.comparingDouble(p -> p.distSqr(feet)));
         return out;
      } else {
         return out;
      }
   }

   static String init(SpawnerBlockEntity spawner) {
      try {
         SpawnData data = ((AutismBaseSpawnerAccessor)spawner.getSpawner()).autism$getNextSpawnData();
         if (data == null) {
            return null;
         }

         String id = data.getEntityToSpawn().getString("id").orElse("");
         return id.isBlank() ? null : id.toLowerCase(Locale.ROOT);
      } catch (Throwable ignored) {
         return null;
      }
   }

   static List<Entity> init_List_Str_D(String typeId, double range) {
      Minecraft mc = Minecraft.getInstance();
      List<Entity> out = new ArrayList<>();
      if (mc.level != null && mc.player != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (e.isAlive() && !(e.distanceToSqr(mc.player) > range * range) && BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals(typeId)) {
               out.add(e);
            }
         }

         out.sort(Comparator.comparingDouble(ex -> ex.distanceToSqr(mc.player)));
         return out;
      } else {
         return out;
      }
   }

   static String init(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
   }
}
