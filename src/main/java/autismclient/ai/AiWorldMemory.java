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
   private static final LinkedHashMap<Long, AiWorldMemory.Inner_DataUtil> init_LinkedHashMap = new LinkedHashMap<>(64, 0.75F, true);
   private static final Map<Long, Long> init_Map = new HashMap<>();
   private static final Map<Long, AiWorldMemory.Inner_StreamUtil> load_Map = new LinkedHashMap<>();
   private static Object init_Obj;
   private static Object load_Obj;
   private static String init_Str = "";
   private static String load_Str = "";
   private static long init_J;
   private static long load_J;
   private static int write_I;
   private static long read_J;
   private static long write_J;
   private static boolean init_Z;
   private static boolean load_Z;

   private AiWorldMemory() {
   }

   static void init(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         load(mc);
         long now = AiThreats.init_J();
         if (now >= write_J) {
            write_J = now + 200L;
            load();
         }

         if (now >= init_J) {
            init_J = now + 20L;
            int cx = mc.player.getBlockX() >> 4;
            int cz = mc.player.getBlockZ() >> 4;

            for (int[] offset : AiWorldScan.init(6)) {
               int x = cx + offset[0];
               int z = cz + offset[1];
               LevelChunk live = mc.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
               if (live != null) {
                  long key = autismclient.ai.engine.AiGridRevisions.init_J(x, z);
                  AiWorldMemory.Inner_DataUtil remembered = init_LinkedHashMap.get(key);
                  int revision = autismclient.ai.engine.AiGridRevisions.init(key);
                  if (remembered == null) {
                     init_LinkedHashMap.put(key, new AiWorldMemory.Inner_DataUtil(live, revision));
                  } else if (remembered.init_WeakReference.get() != live || remembered.read != revision || remembered.init_Z && now - remembered.init_J > 600L) {
                     remembered.init(live, revision);
                  }
               }
            }

            while (init_LinkedHashMap.size() > 256) {
               init_LinkedHashMap.remove(init_LinkedHashMap.keySet().iterator().next());
            }
         }

         long stop = System.nanoTime() + 750000L;
         int budget = 4096;
         ArrayList<Entry<Long, AiWorldMemory.Inner_DataUtil>> entries = new ArrayList<>(init_LinkedHashMap.entrySet());

         for (int examined = 0; examined < entries.size(); examined++) {
            Entry<Long, AiWorldMemory.Inner_DataUtil> entry = entries.get(Math.floorMod(write_I++, entries.size()));
            AiWorldMemory.Inner_DataUtil chunk = entry.getValue();
            if (!chunk.init_Z) {
               int cx = (int)(entry.getKey() >> 32);
               int cz = (int)entry.getKey().longValue();
               LevelChunk live = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
               if (live != null) {
                  int revision = autismclient.ai.engine.AiGridRevisions.init(entry.getKey());
                  if (chunk.init_WeakReference.get() != live || chunk.read != revision) {
                     chunk.init(live, revision);
                  }

                  LevelChunkSection[] sections = live.getSections();

                  while (chunk.init_I < sections.length && budget > 0) {
                     LevelChunkSection section = sections[chunk.init_I];
                     if (section != null && !section.hasOnlyAir()) {
                        while (chunk.load_I < 4096 && budget-- > 0) {
                           if ((chunk.load_I & 63) == 0 && System.nanoTime() >= stop) {
                              return;
                           }

                           int cell = chunk.load_I++;
                           int x = cell & 15;
                           int z = cell >> 4 & 15;
                           int y = cell >> 8;
                           BlockPos pos = new BlockPos((cx << 4) + x, (live.getSectionYFromSectionIndex(chunk.init_I) << 4) + y, (cz << 4) + z);
                           chunk.init(pos, section.getBlockState(x, y, z));
                        }

                        if (chunk.load_I >= 4096) {
                           chunk.init_I++;
                           chunk.load_I = 0;
                        }
                     } else {
                        chunk.init_I++;
                        chunk.load_I = 0;
                     }
                  }

                  chunk.init_J = now;
                  if (chunk.init_I >= sections.length) {
                     chunk.init_Z = true;
                  }

                  load_J++;
                  if (budget <= 0 || System.nanoTime() >= stop) {
                     break;
                  }
               }
            }
         }
      } else if (init_Obj != null) {
         init_V();
      }
   }

   static void init(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == init_Obj && mc.getConnection() == load_Obj && AiTerrain.isLoaded(mc.level, pos)) {
         AiWorldMemory.Inner_DataUtil chunk = init_LinkedHashMap.get(autismclient.ai.engine.AiGridRevisions.init_J(pos.getX() >> 4, pos.getZ() >> 4));
         if (load_Map.remove(pos.asLong()) != null) {
            load_J++;
         }

         if (chunk != null) {
            chunk.init(pos, mc.level.getBlockState(pos));
            load_J++;
         }
      }
   }

   private static void load(Minecraft mc) {
      String current = AiWorldIdentity.init(mc);
      String dim = AiWorldIdentity.load(mc);
      if (init_Obj != mc.level || load_Obj != mc.getConnection() || !init_Str.equals(current) || !load_Str.equals(dim)) {
         init_V();
         init_Obj = mc.level;
         load_Obj = mc.getConnection();
         init_Str = current;
         load_Str = dim;
      }

      AiService service = AiService.load_cc();
      if (!init_Z && service != null && init_Obj != null) {
         String requestedWorld = init_Str;
         String requestedDimension = load_Str;
         Object requestedLevel = init_Obj;
         Object requestedConnection = load_Obj;
         long requestedGeneration = read_J;
         init_Z = service.init_Z_Str(
            init_Str,
            load_Str,
            stored -> {
               Minecraft client = Minecraft.getInstance();
               if (read_J == requestedGeneration
                  && init_Obj == requestedLevel
                  && load_Obj == requestedConnection
                  && client.level == requestedLevel
                  && client.getConnection() == requestedConnection
                  && AiWorldIdentity.init(client).equals(requestedWorld)
                  && AiWorldIdentity.load(client).equals(requestedDimension)) {
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
                                    AiWorldMemory.Inner_DataUtil indexed = init_LinkedHashMap.get(autismclient.ai.engine.AiGridRevisions.init_J(pos.getX() >> 4, pos.getZ() >> 4));
                                    if ((indexed == null || !indexed.init_Map.containsKey(pos.asLong()))
                                       && (!AiTerrain.isLoaded(client.level, pos) || block.equals(AiWorldScan.init(client.level.getBlockState(pos))))) {
                                       load_Map.put(pos.asLong(), new AiWorldMemory.Inner_StreamUtil(pos, category, block, -1L, -1L, observed));
                                    }
                                 }
                              }
                           }
                        }
                     } catch (RuntimeException var18) {
                     }
                  }

                  load_Z = true;
                  load_J++;
                  load();
               }
            }
         );
      }
   }

   private static void init_V() {
      if (init_Obj != null) {
         load();
      }

      init_LinkedHashMap.clear();
      init_Map.clear();
      load_Map.clear();
      load_Obj = null;
      init_Obj = null;
      load_Str = "";
      init_Str = "";
      write_J = 0L;
      init_J = 0L;
      write_I = 0;
      load_Z = false;
      init_Z = false;
      read_J++;
      load_J++;
   }

   private static void load() {
      AiService service = AiService.load_cc();
      if (service != null && init_Obj != null && load_Z) {
         Map<Long, AiWorldMemory.Inner_StreamUtil> sampled = new LinkedHashMap<>();
         int examined = 0;

         label49:
         for (AiWorldMemory.Inner_DataUtil chunk : init_LinkedHashMap.values()) {
            for (AiWorldMemory.Inner_StreamUtil fact : chunk.init_Map.values()) {
               sampled.put(fact.pos.asLong(), fact);
               if (++examined >= 4096) {
                  break label49;
               }
            }
         }

         for (AiWorldMemory.Inner_StreamUtil fact : load_Map.values()) {
            sampled.putIfAbsent(fact.pos.asLong(), fact);
         }

         ArrayList<AiWorldMemory.Inner_StreamUtil> ordered = new ArrayList<>(sampled.values());
         ordered.sort(Comparator.comparingLong(AiWorldMemory.Inner_StreamUtil::observedAtMillis).reversed());
         JsonArray saved = new JsonArray();
         Map<String, Integer> counts = new HashMap<>();

         for (AiWorldMemory.Inner_StreamUtil fact : ordered) {
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

         service.init_V_Str(init_Str, load_Str, saved);
      }
   }

   static void init(String resetWorld) {
      if (resetWorld == null || resetWorld.equals(init_Str)) {
         load_Map.clear();
         load_Z = true;
         init_Z = true;
         read_J++;
         load_J++;
      }
   }

   private static String init_Str(BlockState state) {
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

   static JsonObject init_JsonObject() {
      Minecraft mc = Minecraft.getInstance();
      return AiJson.objectOf(
         "world",
         AiWorldIdentity.init(mc),
         "dimension",
         AiWorldIdentity.load(mc),
         "tick",
         AiThreats.init_J(),
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
         AiWorldIdentity.init(mc).contains("/session/") ? "session" : "saved-world",
         "indexCellBudgetPerTick",
         4096,
         "indexTimeBudgetNanos",
         750000,
         "candidateCapPerCategoryPerChunk",
         32
      );
   }

   static List<BlockPos> init(Predicate<BlockState> target, int radiusChunks, int perChunk, int cap) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level == init_Obj && mc.player != null && mc.getConnection() == load_Obj) {
         List<BlockPos> out = new ArrayList<>();
         int cx = mc.player.getBlockX() >> 4;
         int cz = mc.player.getBlockZ() >> 4;

         label41:
         for (int[] offset : AiWorldScan.init(radiusChunks)) {
            AiWorldMemory.Inner_DataUtil chunk = init_LinkedHashMap.get(autismclient.ai.engine.AiGridRevisions.init_J(cx + offset[0], cz + offset[1]));
            if (chunk != null) {
               int found = 0;
               Iterator var12 = chunk.init_Map.values().iterator();

               while (true) {
                  if (var12.hasNext()) {
                     AiWorldMemory.Inner_StreamUtil fact = (AiWorldMemory.Inner_StreamUtil)var12.next();
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

   static JsonObject init(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         load(mc);
         String requested = AiJson.getString(args, "category", "");
         int limit = Math.max(1, Math.min(32, AiJson.getInt(args, "limit", 12)));
         int radius = Math.max(16, Math.min(192, AiJson.getInt(args, "radius", 96)));
         BlockPos origin = mc.player.blockPosition();
         List<AiWorldMemory.Inner_StreamUtil> found = new ArrayList<>();
         int available = 0;
         int complete = 0;

         for (Entry<Long, AiWorldMemory.Inner_DataUtil> entry : init_LinkedHashMap.entrySet()) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            boolean loaded = mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null;
            if (loaded) {
               available++;
            }

            if (loaded && entry.getValue().init_Z && entry.getValue().read == autismclient.ai.engine.AiGridRevisions.init(entry.getKey())) {
               complete++;
            }

            for (AiWorldMemory.Inner_StreamUtil fact : entry.getValue().init_Map.values()) {
               if ((requested.isEmpty() || requested.equals(fact.category))
                  && !(fact.pos.distSqr(origin) > (double)radius * radius)
                  && (!loaded || fact.block.equals(AiWorldScan.init(mc.level.getBlockState(fact.pos))))) {
                  found.add(fact);
               }
            }
         }

         for (AiWorldMemory.Inner_StreamUtil fact : load_Map.values()) {
            if ((requested.isEmpty() || requested.equals(fact.category)) && !(fact.pos.distSqr(origin) > (double)radius * radius)) {
               AiWorldMemory.Inner_DataUtil indexed = init_LinkedHashMap.get(autismclient.ai.engine.AiGridRevisions.init_J(fact.pos.getX() >> 4, fact.pos.getZ() >> 4));
               if ((indexed == null || !indexed.init_Map.containsKey(fact.pos.asLong()))
                  && (!AiTerrain.isLoaded(mc.level, fact.pos) || fact.block.equals(AiWorldScan.init(mc.level.getBlockState(fact.pos))))) {
                  found.add(fact);
               }
            }
         }

         found.sort(Comparator.comparingDouble(factx -> factx.pos.distSqr(origin) + (AiTerrain.isLoaded(mc.level, factx.pos) ? 0 : 4096)));
         JsonArray candidates = new JsonArray();

         for (AiWorldMemory.Inner_StreamUtil fact : found.subList(0, Math.min(limit, found.size()))) {
            candidates.add(init(mc, fact));
         }

         JsonArray entities = new JsonArray();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isAlive() && !(entity.distanceToSqr(mc.player) > radius * radius) && entities.size() < limit) {
               String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
               if (entity instanceof LivingEntity || AiProjectiles.init_Z(entity) || entity instanceof EndCrystal) {
                  JsonObject observed = AiJson.objectOf(
                     "world",
                     init_Str,
                     "dimension",
                     load_Str,
                     "id",
                     entity.getId(),
                     "uuid",
                     entity.getUUID().toString(),
                     "type",
                     id,
                     "position",
                     entity.blockPosition().toShortString(),
                     "observedTick",
                     AiThreats.init_J(),
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
            init_JsonObject(),
            "indexRevision",
            load_J,
            "terrainRevision",
            autismclient.ai.engine.AiGridRevisions.init_J(),
            "candidates",
            candidates,
            "entities",
            entities,
            "frontiers",
            init(mc, limit),
            "coverage",
            AiJson.objectOf(
               "indexedChunks",
               init_LinkedHashMap.size(),
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

   private static JsonObject init(Minecraft mc, AiWorldMemory.Inner_StreamUtil fact) {
      boolean live = AiTerrain.isLoaded(mc.level, fact.pos);
      JsonObject out = AiJson.objectOf(
         "world",
         init_Str,
         "dimension",
         load_Str,
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
         live ? AiThreats.init_J() : fact.tick,
         "observedAtMs",
         live ? System.currentTimeMillis() : fact.observedAtMillis,
         "source",
         "client-block-state",
         "revision",
         live ? autismclient.ai.engine.AiGridRevisions.init_J() : fact.revision,
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

         if (List.of("diamond", "iron", "gold").contains(fact.category) && AiWorldScan.init(state).contains("ore")) {
            out.add("observedVein", load(mc, fact));
         }

         if (!state.getFluidState().isEmpty()) {
            out.addProperty("fluidSource", state.getFluidState().isSource());
         }

         if (state.getBlock() instanceof EndPortalFrameBlock) {
            out.addProperty("hasEye", state.getValue(EndPortalFrameBlock.HAS_EYE));
            out.addProperty("facing", state.getValue(EndPortalFrameBlock.FACING).getName());
         }

         if (mc.level.getBlockEntity(fact.pos) instanceof SpawnerBlockEntity spawner) {
            out.addProperty("spawnerMob", AiWorldScan.init(spawner));
         }

         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos stand = fact.pos.relative(direction);
            if (AiTerrain.isLoaded(mc.level, stand)
               && AiTerrain.isLoaded(mc.level, stand.below())
               && AiTerrain.isLoaded(mc.level, stand.above())
               && AiCombat.init_Z_Minecraft_BlockPos(mc, stand)
               && !autismclient.ai.engine.AiEnvironmentHazards.init(mc.level, stand)
               && !mc.level.getBlockState(stand.below()).getCollisionShape(mc.level, stand.below()).isEmpty()
               && mc.level.getBlockState(stand).getCollisionShape(mc.level, stand).isEmpty()
               && mc.level.getBlockState(stand.above()).getCollisionShape(mc.level, stand.above()).isEmpty()
               && mc.level.getFluidState(stand).isEmpty()
               && mc.level.getFluidState(stand.above()).isEmpty()
               && !init_Z(mc.level.getBlockState(stand))
               && !init_Z(mc.level.getBlockState(stand.above()))
               && !init_Z(mc.level.getBlockState(stand.below()))) {
               out.addProperty("candidateStandingPosition", stand.toShortString());
               out.addProperty("reachability", "local stance observed; route and interaction ray still require validation");
               break;
            }
         }
      }

      return out;
   }

   private static JsonObject load(Minecraft mc, AiWorldMemory.Inner_StreamUtil anchor) {
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
               if (anchor.category.equals(init_Str(state)) && AiWorldScan.init(state).contains("ore")) {
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
      init_Map.put(autismclient.ai.engine.AiGridRevisions.init_J(at.getX() >> 4, at.getZ() >> 4), AiThreats.init_J() + 600L);
      if (init_Map.size() > 256) {
         init_Map.remove(init_Map.keySet().iterator().next());
      }
   }

   private static boolean init_Z(BlockState state) {
      return state.is(BlockTags.FIRE)
         || state.is(Blocks.MAGMA_BLOCK)
         || state.is(Blocks.CACTUS)
         || state.is(Blocks.SWEET_BERRY_BUSH)
         || state.is(Blocks.WITHER_ROSE)
         || state.is(Blocks.POWDER_SNOW)
         || state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
   }

   private static JsonArray init(Minecraft mc, int limit) {
      JsonArray result = new JsonArray();
      List<BlockPos> borders = new ArrayList<>();

      for (Entry<Long, AiWorldMemory.Inner_DataUtil> entry : init_LinkedHashMap.entrySet()) {
         if (entry.getValue().init_Z) {
            int cx = (int)(entry.getKey() >> 32);
            int cz = (int)entry.getKey().longValue();
            if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null) {
               for (Direction direction : Plane.HORIZONTAL) {
                  int x = cx + direction.getStepX();
                  int z = cz + direction.getStepZ();
                  long key = autismclient.ai.engine.AiGridRevisions.init_J(x, z);
                  if (init_Map.getOrDefault(key, 0L) <= AiThreats.init_J() && !init_LinkedHashMap.containsKey(key)) {
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

   private static final class Inner_DataUtil {
      WeakReference<LevelChunk> init_WeakReference;
      final Map<Long, AiWorldMemory.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
      final Map<String, Integer> load_Map = new HashMap<>();
      int init_I;
      int load_I;
      int read;
      long init_J;
      boolean init_Z;

      Inner_DataUtil(LevelChunk source, int revision) {
         this.init_WeakReference = new WeakReference<>(source);
         this.read = revision;
      }

      void init(LevelChunk source, int revision) {
         this.init_WeakReference = new WeakReference<>(source);
         this.read = revision;
         this.init_I = this.load_I = 0;
         this.init_Z = false;
         this.init_Map.clear();
         this.load_Map.clear();
      }

      void init(BlockPos pos, BlockState state) {
         AiWorldMemory.load_Map.remove(pos.asLong());
         AiWorldMemory.Inner_StreamUtil old = this.init_Map.remove(pos.asLong());
         if (old != null) {
            this.load_Map.computeIfPresent(old.category, (key, count) -> Math.max(0, count - 1));
            AiWorldMemory.load_J++;
         }

         String category = AiWorldMemory.init_Str(state);
         if (category != null && this.load_Map.getOrDefault(category, 0) < 32) {
            this.init_Map
               .put(
                  pos.asLong(),
                  new AiWorldMemory.Inner_StreamUtil(
                     pos.immutable(), category, AiWorldScan.init(state), AiThreats.init_J(), autismclient.ai.engine.AiGridRevisions.init_J(), System.currentTimeMillis()
                  )
               );
            AiWorldMemory.load_J++;
            this.load_Map.merge(category, 1, Integer::sum);
         }
      }
   }

   private record Inner_StreamUtil(BlockPos pos, String category, String block, long tick, long revision, long observedAtMillis) {
   }
}
