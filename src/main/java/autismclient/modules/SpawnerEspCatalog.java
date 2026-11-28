package autismclient.modules;

import autismclient.mixin.accessor.AutismBaseSpawnerAccessor;
import autismclient.mixin.accessor.AutismTrialSpawnerStateDataAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpawnerEspCatalog {
   private static final int init_I = 512;
   public static final List<SpawnerEspCatalog.Inner_DataUtil> TARGETS = List.of(
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:zombie", "Zombie", -865361062),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:skeleton", "Skeleton", -857151256),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:spider", "Spider", -863290822),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:cave_spider", "Cave Spider", -868243019),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:blaze", "Blaze", -855667686),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:silverfish", "Silverfish", -859190839),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:breeze", "Breeze", -861287456),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:husk", "Husk", -861368214),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:stray", "Stray", -861943842),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:bogged", "Bogged", -866222769),
      new SpawnerEspCatalog.Inner_DataUtil("minecraft:slime", "Slime", -864299446)
   );
   public static final String DEFAULT_IDS;
   private static final Map<String, SpawnerEspCatalog.Inner_DataUtil> init_Map = new LinkedHashMap<>();
   private static volatile String load_Str;
   private static volatile List<String> load_List = List.of();
   private static final int load_I = -855688389;

   private SpawnerEspCatalog() {
   }

   public static boolean isSelected(Module module, String entityId) {
      return entityId == null ? false : init(module).contains(entityId.toLowerCase(Locale.ROOT));
   }

   public static List<String> init(Module module) {
      String value = module == null ? "" : module.value("spawner-list");
      if (value == null) {
         value = "";
      }

      if (!value.equals(load_Str)) {
         List<String> ids = new ArrayList<>();

         for (String raw : value.split("\\|")) {
            String token = raw.trim().toLowerCase(Locale.ROOT);
            if (!token.isEmpty()) {
               ids.add(token);
            }
         }

         load_List = List.copyOf(ids);
         load_Str = value;
      }

      return load_List;
   }

   public static boolean init(String entityId) {
      return entityId != null && init_Map.containsKey(entityId.toLowerCase(Locale.ROOT));
   }

   public static int init_I(Module module, String entityId) {
      String key = "color-" + entityId;
      String stored = module == null ? "" : module.value(key);
      if (stored != null && !stored.isBlank()) {
         return am.init_I_Module(module, key, -855688389);
      }

      SpawnerEspCatalog.Inner_DataUtil natural = init_Map.get(entityId);
      return natural != null ? natural.defaultColor() : -855688389;
   }

   static void init(Module module, ClientLevel level, Player player, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (module != null && level != null && player != null && (boxEmit != null || traceEmit != null)) {
         String listValue = module.value("spawner-list");
         if (listValue != null && !listValue.isBlank()) {
            int chunkRadius = am.init_I();
            double maxDistSq = 2.0 * (chunkRadius * 16.0 + 16.0) * (chunkRadius * 16.0 + 16.0);
            Vec3 playerPos = player.position();
            int playerChunkX = player.chunkPosition().x();
            int playerChunkZ = player.chunkPosition().z();
            EspChunkWorker.setLevel(level);
            String stamp = "spawner|" + listValue + "|" + init_Str(module, listValue);
            long gameTime = level.getGameTime();
            ClientChunkCache chunks = level.getChunkSource();

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
               for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                  LevelChunk chunk = chunks.getChunk(playerChunkX + dx, playerChunkZ + dz, ChunkStatus.FULL, false);
                  if (chunk != null) {
                     for (EspChunkWorker.EspBox entry : EspChunkWorker.spawners.scan(chunk, gameTime, stamp, (scanned, out) -> init(level, scanned, module, listValue, out))) {
                        Vec3 trace = entry.tracer();
                        if (!(init(playerPos, trace.x, trace.y, trace.z) > maxDistSq)) {
                           if (boxEmit != null) {
                              boxEmit.accept(entry.box(), entry.color());
                           }

                           if (traceEmit != null) {
                              traceEmit.accept(trace, entry.color());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void init(ClientLevel level, LevelChunk chunk, Module module, String listValue, List<EspChunkWorker.EspBox> out) {
      for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
         if (out.size() >= 512) {
            break;
         }

         BlockPos pos = entry.getKey();
         BlockEntity be = entry.getValue();
         if (pos != null) {
            String mobId;
            if (be instanceof SpawnerBlockEntity spawner) {
               mobId = init(spawner);
            } else {
               if (!(be instanceof TrialSpawnerBlockEntity trialSpawner)) {
                  continue;
               }

               mobId = init(trialSpawner);
            }

            if (mobId != null && isSelected(module, mobId)) {
               out.add(new EspChunkWorker.EspBox(init(level, pos), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), init_I(module, mobId)));
            }
         }
      }
   }

   private static String init(SpawnerBlockEntity spawner) {
      try {
         BaseSpawner baseSpawner = spawner.getSpawner();
         SpawnData data = ((AutismBaseSpawnerAccessor)baseSpawner).autism$getNextSpawnData();
         return init(data);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String init(TrialSpawnerBlockEntity blockEntity) {
      try {
         TrialSpawner trialSpawner = blockEntity.getTrialSpawner();
         if (trialSpawner == null) {
            return null;
         }

         TrialSpawnerStateData data = trialSpawner.getStateData();
         if (data == null) {
            return null;
         }

         Optional<SpawnData> next = ((AutismTrialSpawnerStateDataAccessor)data).autism$getNextSpawnData();
         return next != null && !next.isEmpty() ? init(next.get()) : null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static String init(SpawnData data) {
      if (data == null) {
         return null;
      }

      CompoundTag tag = data.getEntityToSpawn();
      String id = tag == null ? "" : tag.getString("id").orElse("");
      return id.isBlank() ? null : id.toLowerCase(Locale.ROOT);
   }

   private static String init_Str(Module module, String listValue) {
      StringBuilder sig = new StringBuilder();

      for (String raw : listValue.split("\\|")) {
         String id = raw.trim().toLowerCase(Locale.ROOT);
         if (!id.isEmpty()) {
            sig.append(id).append('=').append(init_I(module, id)).append(';');
         }
      }

      return sig.toString();
   }

   private static AABB init(ClientLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      VoxelShape shape = state.getShape(level, pos);
      return shape != null && !shape.isEmpty() ? shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()) : new AABB(pos);
   }

   private static double init(Vec3 from, double x, double y, double z) {
      double dx = from.x - x;
      double dy = from.y - y;
      double dz = from.z - z;
      return dx * dx + dy * dy + dz * dz;
   }

   static {
      StringBuilder def = new StringBuilder();

      for (SpawnerEspCatalog.Inner_DataUtil target : TARGETS) {
         init_Map.put(target.id(), target);
         if (def.length() > 0) {
            def.append('|');
         }

         def.append(target.id());
      }

      DEFAULT_IDS = def.toString();
   }

   public record Inner_DataUtil(String id, String load, int defaultColor) {
   }
}
