package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.SulfurSpikeBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class AiWorldMemory {
   private static final int init_I = 256;
   private static final int load_I = 32;
   private static final int read_I = 4096;
   private static final LinkedHashMap<Long, AiWorldMemory.ChunkScan> chunkScans = new LinkedHashMap<>(64, 0.75F, true);
   private static final Map<Long, Long> unusedChunkMap = new HashMap<>();
   private static final Map<Long, AiWorldMemory.Landmark> savedLandmarks = new LinkedHashMap<>();
   private static Object trackedLevel;
   private static Object trackedConnection;
   private static String worldKey = "";
   private static String dimension = "";
   private static long nextScanTick;
   private static long worldRevision;
   private static int rescanCursor;
   private static long generation;
   private static long nextSaveTick;
   private static boolean discoveryLoaded;
   private static boolean dirty;

   private AiWorldMemory() {
   }

   static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         syncWorld(mc);
         long now = AiThreats.currentClientTick();
         if (now >= nextSaveTick) {
            nextSaveTick = now + 200L;
            saveDiscoveries();
         }

         if (now >= nextScanTick) {
            nextScanTick = now + 20L;
            int cx = mc.player.getBlockX() >> 4;
            int cz = mc.player.getBlockZ() >> 4;

            for (int[] offset : AiWorldScan.sortedChunkOffsets(6)) {
               int x = cx + offset[0];
               int z = cz + offset[1];
               LevelChunk live = mc.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
               if (live != null) {
                  long key = autismclient.ai.engine.AiGridRevisions.chunkKey(x, z);
                  AiWorldMemory.ChunkScan remembered = chunkScans.get(key);
                  int revision = autismclient.ai.engine.AiGridRevisions.revisionOf(key);
                  if (remembered == null) {
                     chunkScans.put(key, new AiWorldMemory.ChunkScan(live, revision));
                  } else if (remembered.chunkRef.get() != live || remembered.revision != revision || remembered.complete && now - remembered.scannedAt > 600L) {
                     remembered.rebind(live, revision);
                  }
               }
            }

            while (chunkScans.size() > 256) {
               chunkScans.remove(chunkScans.keySet().iterator().next());
            }
         }

         long stop = System.nanoTime() + 750000L;
         int budget = 4096;
         ArrayList<Entry<Long, AiWorldMemory.ChunkScan>> entries = new ArrayList<>(chunkScans.entrySet());

         for (int examined = 0; examined < entries.size(); examined++) {
            Entry<Long, AiWorldMemory.ChunkScan> entry = entries.get(Math.floorMod(rescanCursor++, entries.size()));
            AiWorldMemory.ChunkScan chunk = entry.getValue();
            if (!chunk.complete) {
               int cx = (int)(entry.getKey() >> 32);
               int cz = (int)entry.getKey().longValue();
               LevelChunk live = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
               if (live != null) {
                  int revision = autismclient.ai.engine.AiGridRevisions.revisionOf(entry.getKey());
                  if (chunk.chunkRef.get() != live || chunk.revision != revision) {
                     chunk.rebind(live, revision);
                  }

                  LevelChunkSection[] sections = live.getSections();

                  while (chunk.sectionCursor < sections.length && budget > 0) {
                     LevelChunkSection section = sections[chunk.sectionCursor];
                     if (section != null && !section.hasOnlyAir()) {
                        while (chunk.cellCursor < 4096 && budget-- > 0) {
                           if ((chunk.cellCursor & 63) == 0 && System.nanoTime() >= stop) {
                              return;
                           }

                           int cell = chunk.cellCursor++;
                           int x = cell & 15;
                           int z = cell >> 4 & 15;
                           int y = cell >> 8;
                           BlockPos pos = new BlockPos((cx << 4) + x, (live.getSectionYFromSectionIndex(chunk.sectionCursor) << 4) + y, (cz << 4) + z);
                           chunk.observe(pos, section.getBlockState(x, y, z));
                        }

                        if (chunk.cellCursor >= 4096) {
                           chunk.sectionCursor++;
                           chunk.cellCursor = 0;
                        }
                     } else {
                        chunk.sectionCursor++;
                        chunk.cellCursor = 0;
                     }
                  }

                  chunk.scannedAt = now;
                  if (chunk.sectionCursor >= sections.length) {
                     chunk.complete = true;
                  }

                  worldRevision++;
                  if (budget <= 0 || System.nanoTime() >= stop) {
                     break;
                  }
               }
            }
         }
      } else if (trackedLevel != null) {
         saveAndClear();
      }
   }

   static void observeChange(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == trackedLevel && mc.getConnection() == trackedConnection && AiTerrain.isLoaded(mc.level, pos)) {
         AiWorldMemory.ChunkScan chunk = chunkScans.get(autismclient.ai.engine.AiGridRevisions.chunkKey(pos.getX() >> 4, pos.getZ() >> 4));
         if (savedLandmarks.remove(pos.asLong()) != null) {
            worldRevision++;
         }

         if (chunk != null) {
            chunk.observe(pos, mc.level.getBlockState(pos));
            worldRevision++;
         }
      }
   }

   private static void syncWorld(Minecraft mc) {
      String current = AiWorldIdentity.currentWorldKey(mc);
      String dim = AiWorldIdentity.currentDimensionId(mc);
      if (trackedLevel != mc.level || trackedConnection != mc.getConnection() || !worldKey.equals(current) || !dimension.equals(dim)) {
         saveAndClear();
         trackedLevel = mc.level;
         trackedConnection = mc.getConnection();
         worldKey = current;
         dimension = dim;
      }

      AiService service = AiService.getExistingInstance();
      if (!discoveryLoaded && service != null && trackedLevel != null) {
         String requestedWorld = worldKey;
         String requestedDimension = dimension;
         Object requestedLevel = trackedLevel;
         Object requestedConnection = trackedConnection;
         long requestedGeneration = generation;
         discoveryLoaded = service.loadDiscoveryMemory(
            worldKey,
            dimension,
            stored -> {
               Minecraft client = Minecraft.getInstance();
               if (generation == requestedGeneration
                  && trackedLevel == requestedLevel
                  && trackedConnection == requestedConnection
                  && client.level == requestedLevel
                  && client.getConnection() == requestedConnection
                  && AiWorldIdentity.currentWorldKey(client).equals(requestedWorld)
                  && AiWorldIdentity.currentDimensionId(client).equals(requestedDimension)) {
                  for (JsonElement entry : stored) {
                     try {
                        JsonObject saved = entry.getAsJsonObject();
                        String[] xyz = AiJson.getString(saved, "position", "").split(",");
                        if (xyz.length == 3) {
                           BlockPos pos = new BlockPos(Integer.parseInt(xyz[0].trim()), Integer.parseInt(xyz[1].trim()), Integer.parseInt(xyz[2].trim()));
                           if (Math.abs((long)pos.getX()) <= 30000000L && Math.abs((long)pos.getZ()) <= 30000000L) {
                              String block = AiJson.getString(saved, "block", "");
                              String category = AiJson.getString(saved, "category", "");
                              if (!block.isEmpty() && !category.isEmpty() && block.length() <= 256 && category.length() <= 64) {
                                 long observed = saved.has("observedAtMs") ? saved.get("observedAtMs").getAsLong() : 0L;
                                 if (observed >= 0L && observed <= System.currentTimeMillis()) {
                                    AiWorldMemory.ChunkScan indexed = chunkScans.get(autismclient.ai.engine.AiGridRevisions.chunkKey(pos.getX() >> 4, pos.getZ() >> 4));
                                    if ((indexed == null || !indexed.landmarks.containsKey(pos.asLong()))
                                       && (!AiTerrain.isLoaded(client.level, pos) || block.equals(AiWorldScan.blockId(client.level.getBlockState(pos))))) {
                                       savedLandmarks.put(pos.asLong(), new AiWorldMemory.Landmark(pos, category, block, -1L, -1L, observed));
                                    }
                                 }
                              }
                           }
                        }
                     } catch (RuntimeException var18) {
                     }
                  }

                  dirty = true;
                  worldRevision++;
                  saveDiscoveries();
               }
            }
         );
      }
   }

   private static void saveAndClear() {
      if (trackedLevel != null) {
         saveDiscoveries();
      }

      chunkScans.clear();
      unusedChunkMap.clear();
      savedLandmarks.clear();
      trackedConnection = null;
      trackedLevel = null;
      dimension = "";
      worldKey = "";
      nextSaveTick = 0L;
      nextScanTick = 0L;
      rescanCursor = 0;
      dirty = false;
      discoveryLoaded = false;
      generation++;
      worldRevision++;
   }

   private static void saveDiscoveries() {
      AiService service = AiService.getExistingInstance();
      if (service != null && trackedLevel != null && dirty) {
         Map<Long, AiWorldMemory.Landmark> sampled = new LinkedHashMap<>();
         int examined = 0;

         label49:
         for (AiWorldMemory.ChunkScan chunk : chunkScans.values()) {
            for (AiWorldMemory.Landmark fact : chunk.landmarks.values()) {
               sampled.put(fact.pos.asLong(), fact);
               if (++examined >= 4096) {
                  break label49;
               }
            }
         }

         for (AiWorldMemory.Landmark fact : savedLandmarks.values()) {
            sampled.putIfAbsent(fact.pos.asLong(), fact);
         }

         ArrayList<AiWorldMemory.Landmark> ordered = new ArrayList<>(sampled.values());
         ordered.sort(Comparator.comparingLong(AiWorldMemory.Landmark::observedAtMillis).reversed());
         JsonArray saved = new JsonArray();
         Map<String, Integer> counts = new HashMap<>();

         for (AiWorldMemory.Landmark fact : ordered) {
            if (counts.getOrDefault(fact.category, 0) < 32) {
               saved.add(
                  AiJson.objectOf(
                     "position", fact.pos.toShortString(), "category", fact.category, "block", fact.block, "observedAtMs", fact.observedAtMillis
                  )
               );
               counts.merge(fact.category, 1, Integer::sum);
               if (saved.size() >= 512) {
                  break;
               }
            }
         }

         service.saveDiscoveryMemory(worldKey, dimension, saved);
      }
   }

   static void reset(String resetWorld) {
      if (resetWorld == null || resetWorld.equals(worldKey)) {
         savedLandmarks.clear();
         dirty = true;
         discoveryLoaded = true;
         generation++;
         worldRevision++;
      }
   }

   private static String categoryOf(BlockState state) {
      String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      if (id.contains("diamond_ore")) {
         return "diamond";
      } else if (id.contains("iron_ore")) {
         return "iron";
      } else if (id.contains("gold_ore") || id.equals("gold_block")) {
         return "gold";
      } else if (state.is(BlockTags.LOGS)) {
         return "wood";
      } else if (id.equals("water") || id.equals("lava")) {
         return id;
      } else if (state.getBlock() instanceof PotentSulfurBlock || state.getBlock() instanceof SulfurSpikeBlock) {
         return "sulfur_hazard";
      } else if (id.equals("obsidian") || id.equals("crying_obsidian")) {
         return id;
      } else if (id.equals("spawner")) {
         return "spawner";
      } else if (id.equals("end_portal_frame")) {
         return "end_portal_frame";
      } else if (id.equals("end_portal")) {
         return "central_exit_or_end_portal";
      } else if (id.equals("end_gateway")) {
         return "end_gateway";
      } else if (id.equals("nether_portal")) {
         return "nether_portal";
      } else if (id.equals("bedrock")) {
         return "fountain_evidence";
      } else if (id.contains("nether_brick")) {
         return "fortress_evidence";
      } else if (id.contains("polished_blackstone") || id.equals("chiseled_polished_blackstone")) {
         return "bastion_evidence";
      } else if (id.contains("stone_brick")) {
         return "stronghold_evidence";
      } else if (id.endsWith("_bed") || id.endsWith("_wool")) {
         return "bed_supplies";
      } else if (id.contains("chest") || id.equals("barrel")) {
         return "container";
      } else if (!id.equals("crafting_table") && !id.equals("furnace") && !id.equals("smoker") && !id.equals("blast_furnace")) {
         return List.of("hay_block", "wheat", "carrots", "potatoes", "beetroots", "sweet_berry_bush", "melon", "pumpkin").contains(id) ? "food" : null;
      } else {
         return "station";
      }
   }

   static JsonObject snapshotJson() {
      Minecraft mc = Minecraft.getInstance();
      return AiJson.objectOf(
         "world",
         AiWorldIdentity.currentWorldKey(mc),
         "dimension",
         AiWorldIdentity.currentDimensionId(mc),
         "tick",
         AiThreats.currentClientTick(),
         "clientLoadedBlocks",
         true,
         "trackedEntities",
         true,
         "synchronizedInventory",
         true,
         "unopenedContainerContents",
         "not_observable",
         "unloadedTerrain",
         "historical leads only",
         "remoteStructures",
         "unknown; exploration required",
         "hiddenMobState",
         "not_observable",
         "integratedServerPresent",
         mc.getSingleplayerServer() != null,
         "integratedServerWorldScan",
         "not_enabled",
         "seedPredictionPermittedForSpeedrun",
         false,
         "worldScope",
         AiWorldIdentity.currentWorldKey(mc).contains("/session/") ? "session" : "saved-world",
         "indexCellBudgetPerTick",
         4096,
         "indexTimeBudgetNanos",
         750000,
         "candidateCapPerCategoryPerChunk",
         32
      );
   }

   static List<BlockPos> indexed(Predicate<BlockState> target, int radiusChunks, int perChunk, int cap) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level == trackedLevel && mc.player != null && mc.getConnection() == trackedConnection) {
         List<BlockPos> out = new ArrayList<>();
         int cx = mc.player.getBlockX() >> 4;
         int cz = mc.player.getBlockZ() >> 4;

         label41:
         for (int[] offset : AiWorldScan.sortedChunkOffsets(radiusChunks)) {
            AiWorldMemory.ChunkScan chunk = chunkScans.get(autismclient.ai.engine.AiGridRevisions.chunkKey(cx + offset[0], cz + offset[1]));
            if (chunk != null) {
               int found = 0;
               Iterator var12 = chunk.landmarks.values().iterator();

               while (true) {
                  if (var12.hasNext()) {
                     AiWorldMemory.Landmark fact = (AiWorldMemory.Landmark)var12.next();
                     if (!AiTerrain.isLoaded(mc.level, fact.pos) || !target.test(mc.level.getBlockState(fact.pos))) {
                        continue;
                     }

                     out.add(fact.pos);
                     if (++found < perChunk && out.size() < cap) {
                        continue;
                     }
                  }

                  if (out.size() >= cap) {
                     break label41;
                  }
                  break;
               }
            }
         }

         out.sort(Comparator.comparingDouble(pos -> pos.distSqr(mc.player.blockPosition())));
         return out;
      } else {
         return List.of();
      }
   }

   static JsonObject describe(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         syncWorld(mc);
         String requested = AiJson.getString(args, "category", "");
         int limit = Math.max(1, Math.min(32, AiJson.getInt(args, "limit", 12)));
         int radius = Math.max(16, Math.min(192, AiJson.getInt(args, "radius", 96)));
         BlockPos origin = mc.player.blockPosition();
         List<AiWorldMemory.Landmark> found = new ArrayList<>();
         int available = 0;
         int complete = 0;

         for (Entry<Long, AiWorldMemory.ChunkScan> entry : chunkScans.entrySet()) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            boolean loaded = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null;
            if (loaded) {
               available++;
            }

            if (loaded && entry.getValue().complete && entry.getValue().revision == autismclient.ai.engine.AiGridRevisions.revisionOf(entry.getKey())) {
               complete++;
            }

            for (AiWorldMemory.Landmark fact : entry.getValue().landmarks.values()) {
               if ((requested.isEmpty() || requested.equals(fact.category))
                  && !(fact.pos.distSqr(origin) > (double)radius * radius)
                  && (!loaded || fact.block.equals(AiWorldScan.blockId(mc.level.getBlockState(fact.pos))))) {
                  found.add(fact);
               }
            }
         }

         for (AiWorldMemory.Landmark fact : savedLandmarks.values()) {
            if ((requested.isEmpty() || requested.equals(fact.category)) && !(fact.pos.distSqr(origin) > (double)radius * radius)) {
               AiWorldMemory.ChunkScan indexed = chunkScans.get(autismclient.ai.engine.AiGridRevisions.chunkKey(fact.pos.getX() >> 4, fact.pos.getZ() >> 4));
               if ((indexed == null || !indexed.landmarks.containsKey(fact.pos.asLong()))
                  && (!AiTerrain.isLoaded(mc.level, fact.pos) || fact.block.equals(AiWorldScan.blockId(mc.level.getBlockState(fact.pos))))) {
                  found.add(fact);
               }
            }
         }

         found.sort(Comparator.comparingDouble(factx -> factx.pos.distSqr(origin) + (AiTerrain.isLoaded(mc.level, factx.pos) ? 0 : 4096)));
         JsonArray candidates = new JsonArray();

         for (AiWorldMemory.Landmark fact : found.subList(0, Math.min(limit, found.size()))) {
            candidates.add(landmarkJson(mc, fact));
         }

         JsonArray entities = new JsonArray();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isAlive() && !(entity.distanceToSqr(mc.player) > radius * radius) && entities.size() < limit) {
               String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
               if (entity instanceof LivingEntity || AiProjectiles.isProjectile(entity) || entity instanceof EndCrystal) {
                  JsonObject observed = AiJson.objectOf(
                     "world",
                     worldKey,
                     "dimension",
                     dimension,
                     "id",
                     entity.getId(),
                     "uuid",
                     entity.getUUID().toString(),
                     "type",
                     id,
                     "position",
                     entity.blockPosition().toShortString(),
                     "observedTick",
                     AiThreats.currentClientTick(),
                     "source",
                     "tracked-client-entity",
                     "knowledge",
                     "KNOWN",
                     "availability",
                     "client-tracked",
                     "verifyIdentityBeforeAction",
                     true
                  );
                  if (entity instanceof Piglin piglin) {
                     observed.addProperty("baby", piglin.isBaby());
                  }

                  if (entity instanceof EnderDragon dragon) {
                     observed.add(
                        "dragonHead",
                        AiJson.objectOf(
                           "position",
                           dragon.head.blockPosition().toShortString(),
                           "phase",
                           String.valueOf(dragon.getPhaseManager().getCurrentPhase().getPhase()),
                           "source",
                           "client-synchronized phase and part geometry"
                        )
                     );
                  }

                  entities.add(observed);
               }
            }
         }

         return AiJson.objectOf(
            "capabilities",
            snapshotJson(),
            "indexRevision",
            worldRevision,
            "terrainRevision",
            autismclient.ai.engine.AiGridRevisions.globalRevision(),
            "candidates",
            candidates,
            "entities",
            entities,
            "frontiers",
            scanBordersJson(mc, limit),
            "coverage",
            AiJson.objectOf(
               "indexedChunks",
               chunkScans.size(),
               "availableChunks",
               available,
               "fullyIndexedAvailableChunks",
               complete,
               "partial",
               complete < available,
               "samplingBounded",
               true
            ),
            "emptyResultMeaning",
            "no matching indexed evidence; never global absence"
         );
      } else {
         throw new IllegalArgumentException("World memory requires a connected player");
      }
   }

   private static JsonObject landmarkJson(Minecraft mc, AiWorldMemory.Landmark fact) {
      boolean live = AiTerrain.isLoaded(mc.level, fact.pos);
      JsonObject out = AiJson.objectOf(
         "world",
         worldKey,
         "dimension",
         dimension,
         "position",
         fact.pos.toShortString(),
         "block",
         fact.block,
         "category",
         fact.category,
         "knowledge",
         live ? (fact.category.endsWith("_evidence") ? "SUSPECTED" : "KNOWN") : "STALE",
         "availability",
         live ? "client-loaded" : "unavailable",
         "observedTick",
         live ? AiThreats.currentClientTick() : fact.tick,
         "observedAtMs",
         live ? System.currentTimeMillis() : fact.observedAtMillis,
         "source",
         "client-block-state",
         "revision",
         live ? autismclient.ai.engine.AiGridRevisions.globalRevision() : fact.revision,
         "indexedRevision",
         fact.revision,
         "verifyBeforeAction",
         true,
         "estimatedTravelTicks",
         Math.sqrt(fact.pos.distSqr(mc.player.blockPosition())) / 0.22,
         "travelCostIsEstimate",
         true,
         "rankingReason",
         "fresh nearby observations precede stale leads; actual route, excavation and hazard cost remain unverified",
         "risk",
         "route hazards and escape must be checked by the action controller",
         "reachability",
         "unverified",
         "requiredTool",
         "resolve current harvest rules before mining"
      );
      if (fact.category.equals("container")) {
         out.addProperty("contents", "unknown until synchronized interaction");
      }

      if (live) {
         BlockState state = mc.level.getBlockState(fact.pos);
         if (state.requiresCorrectToolForDrops()) {
            JsonArray tools = new JsonArray();

            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = mc.player.getInventory().getItem(slot);
               if (!stack.isEmpty() && AiRunResources.hasMinDurability(stack, 0) && stack.isCorrectToolForDrops(state)) {
                  tools.add(
                     AiJson.objectOf(
                        "slot", slot, "item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), "destroySpeed", stack.getDestroySpeed(state)
                     )
                  );
               }
            }

            out.add("observedHarvestTools", tools);
            out.addProperty(
               "requiredTool",
               tools.isEmpty() ? "no currently carried tool satisfies the active harvest rule" : "select a current suitable tool and revalidate durability"
            );
         }

         if (List.of("diamond", "iron", "gold").contains(fact.category) && AiWorldScan.blockId(state).contains("ore")) {
            out.add("observedVein", veinJson(mc, fact));
         }

         if (!state.getFluidState().isEmpty()) {
            out.addProperty("fluidSource", state.getFluidState().isSource());
         }

         if (state.getBlock() instanceof EndPortalFrameBlock) {
            out.addProperty("hasEye", state.getValue(EndPortalFrameBlock.HAS_EYE));
            out.addProperty("facing", state.getValue(EndPortalFrameBlock.FACING).getName());
         }

         if (mc.level.getBlockEntity(fact.pos) instanceof SpawnerBlockEntity spawner) {
            out.addProperty("spawnerMob", AiWorldScan.spawnerEntityId(spawner));
         }

         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos stand = fact.pos.relative(direction);
            if (AiTerrain.isLoaded(mc.level, stand)
               && AiTerrain.isLoaded(mc.level, stand.below())
               && AiTerrain.isLoaded(mc.level, stand.above())
               && AiCombat.isSafeStep(mc, stand)
               && !autismclient.ai.engine.AiEnvironmentHazards.isHazardous(mc.level, stand)
               && !mc.level.getBlockState(stand.below()).getCollisionShape(mc.level, stand.below()).isEmpty()
               && mc.level.getBlockState(stand).getCollisionShape(mc.level, stand).isEmpty()
               && mc.level.getBlockState(stand.above()).getCollisionShape(mc.level, stand.above()).isEmpty()
               && mc.level.getFluidState(stand).isEmpty()
               && mc.level.getFluidState(stand.above()).isEmpty()
               && !isHazard(mc.level.getBlockState(stand))
               && !isHazard(mc.level.getBlockState(stand.above()))
               && !isHazard(mc.level.getBlockState(stand.below()))) {
               out.addProperty("candidateStandingPosition", stand.toShortString());
               out.addProperty("reachability", "local stance observed; route and interaction ray still require validation");
               break;
            }
         }
      }

      return out;
   }

   private static JsonObject veinJson(Minecraft mc, AiWorldMemory.Landmark anchor) {
      ArrayDeque<BlockPos> pending = new ArrayDeque<>();
      HashSet<BlockPos> examined = new HashSet<>();
      JsonArray cells = new JsonArray();
      boolean partial = false;
      pending.add(anchor.pos);

      while (!pending.isEmpty() && examined.size() < 128) {
         BlockPos pos = pending.removeFirst();
         if (examined.add(pos)) {
            if (!AiTerrain.isLoaded(mc.level, pos)) {
               partial = true;
            } else {
               BlockState state = mc.level.getBlockState(pos);
               if (anchor.category.equals(categoryOf(state)) && AiWorldScan.blockId(state).contains("ore")) {
                  cells.add(pos.toShortString());

                  for (Direction direction : Direction.values()) {
                     pending.add(pos.relative(direction));
                  }
               }
            }
         }
      }

      return AiJson.objectOf(
         "knownOreBlocks",
         cells.size(),
         "positions",
         cells,
         "partialCoverage",
         partial || !pending.isEmpty(),
         "expectedYield",
         "unknown until current harvest and loot rules produce actual drops",
         "unseenOreAssumed",
         false
      );
   }

   static void load(BlockPos at) {
      unusedChunkMap.put(autismclient.ai.engine.AiGridRevisions.chunkKey(at.getX() >> 4, at.getZ() >> 4), AiThreats.currentClientTick() + 600L);
      if (unusedChunkMap.size() > 256) {
         unusedChunkMap.remove(unusedChunkMap.keySet().iterator().next());
      }
   }

   private static boolean isHazard(BlockState state) {
      return state.is(BlockTags.FIRE)
         || state.is(Blocks.MAGMA_BLOCK)
         || state.is(Blocks.CACTUS)
         || state.is(Blocks.SWEET_BERRY_BUSH)
         || state.is(Blocks.WITHER_ROSE)
         || state.is(Blocks.POWDER_SNOW)
         || state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
   }

   private static JsonArray scanBordersJson(Minecraft mc, int limit) {
      JsonArray result = new JsonArray();
      List<BlockPos> borders = new ArrayList<>();

      for (Entry<Long, AiWorldMemory.ChunkScan> entry : chunkScans.entrySet()) {
         if (entry.getValue().complete) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null) {
               for (Direction direction : Plane.HORIZONTAL) {
                  int x = cx + direction.getStepX();
                  int z = cz + direction.getStepZ();
                  long key = autismclient.ai.engine.AiGridRevisions.chunkKey(x, z);
                  if (unusedChunkMap.getOrDefault(key, 0L) <= AiThreats.currentClientTick() && !chunkScans.containsKey(key)) {
                     borders.add(new BlockPos((cx << 4) + 8 + direction.getStepX() * 7, mc.player.getBlockY(), (cz << 4) + 8 + direction.getStepZ() * 7));
                  }
               }
            }
         }
      }

      borders.sort(Comparator.comparingDouble(posx -> posx.distSqr(mc.player.blockPosition())));

      for (BlockPos pos : borders.subList(0, Math.min(limit, borders.size()))) {
         result.add(
            AiJson.objectOf(
               "position", pos.toShortString(), "goal", "load and inspect adjacent unindexed terrain", "knowledge", "UNOBSERVED", "routeMustBeVerified", true
            )
         );
      }

      return result;
   }

   private static final class ChunkScan {
      WeakReference<LevelChunk> chunkRef;
      final Map<Long, AiWorldMemory.Landmark> landmarks = new LinkedHashMap<>();
      final Map<String, Integer> categoryCounts = new HashMap<>();
      int sectionCursor;
      int cellCursor;
      int revision;
      long scannedAt;
      boolean complete;

      ChunkScan(LevelChunk source, int revision) {
         this.chunkRef = new WeakReference<>(source);
         this.revision = revision;
      }

      void rebind(LevelChunk source, int revision) {
         this.chunkRef = new WeakReference<>(source);
         this.revision = revision;
         this.sectionCursor = this.cellCursor = 0;
         this.complete = false;
         this.landmarks.clear();
         this.categoryCounts.clear();
      }

      void observe(BlockPos pos, BlockState state) {
         AiWorldMemory.savedLandmarks.remove(pos.asLong());
         AiWorldMemory.Landmark old = this.landmarks.remove(pos.asLong());
         if (old != null) {
            this.categoryCounts.computeIfPresent(old.category, (key, count) -> Math.max(0, count - 1));
            AiWorldMemory.worldRevision++;
         }

         String category = AiWorldMemory.categoryOf(state);
         if (category != null && this.categoryCounts.getOrDefault(category, 0) < 32) {
            this.landmarks
               .put(
                  pos.asLong(),
                  new AiWorldMemory.Landmark(
                     pos.immutable(), category, AiWorldScan.blockId(state), AiThreats.currentClientTick(), autismclient.ai.engine.AiGridRevisions.globalRevision(), System.currentTimeMillis()
                  )
               );
            AiWorldMemory.worldRevision++;
            this.categoryCounts.merge(category, 1, Integer::sum);
         }
      }
   }

   private record Landmark(BlockPos pos, String category, String block, long tick, long revision, long observedAtMillis) {
   }
}
