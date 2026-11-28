package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.util.AutismWaypoints;
import autismclient.util.oresim.AutismOreSimSeedStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure.GenerationStub;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.phys.Vec3;

final class AiOracle {
   static final int init_I = 8;
   static final int load_I = 32;
   static final int read = 2;
   static final int write = 16;
   private static final int update = 8;
   private static final int sync = 4096;
   private static final long QUERY_DEADLINE_NANOS = 60000000000L;
   private static final int flush = 400;
   private static final double THREAT_RADIUS = 24.0;
   private static final int close = 25;
   private static final int open = 3;
   private static final ExecutorService WORLDGEN_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "ai-oracle-structures");
      thread.setDaemon(true);
      return thread;
   });
   private static final Object WORLDGEN_LOCK = new Object();
   private static final LinkedHashMap<String, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> WORLDGEN_CACHE = new LinkedHashMap<String, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap>(
      4, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<String, autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap> eldest) {
         return this.size() > 3;
      }
   };
   private static long hideVillagesBit = -1L;
   private static final AtomicInteger enableCount = new AtomicInteger();
   static final Map<String, AiOracle.StructureSpec> STRUCTURES = buildStructureSpecs();
   private static final int poll = 64;

   private AiOracle() {
   }

   static void enable(boolean on) {
      if (on) {
         enableCount.incrementAndGet();
      } else {
         enableCount.updateAndGet(n -> Math.max(0, n - 1));
      }
   }

   static boolean isActive() {
      return enableCount.get() > 0;
   }

   static AiOperation create(JsonObject args) {
      AiOracle.OracleQuery q = parseQuery(AiJson.getString(args, "query", ""));
      int cap = clampCap(AiJson.getInt(args, "cap", 8));
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (!isActive() || q.subject() != AiOracle.QuerySubject.ORE && q.subject() != AiOracle.QuerySubject.STRUCTURE) {
            AiOperation var10000;
            switch (q.subject()) {
               case LAVA:
                  var10000 = scanBlocks("lava", state -> state.getBlock() == Blocks.LAVA && state.getFluidState().isSource(), cap, radiusChunks(args));
                  break;
               case WATER:
                  var10000 = scanBlocks("water", state -> state.getBlock() == Blocks.WATER, cap, radiusChunks(args));
                  break;
               case VILLAGE:
                  var10000 = scanVillages(cap, radiusChunks(args));
                  break;
               case THREATS:
                  var10000 = scanThreats(cap);
                  break;
               case ORE:
                  var10000 = locateOre(q.ore(), cap, AiJson.getString(args, "seed", ""));
                  break;
               case BLOCK:
                  var10000 = scanBlocks(q.blockId(), blockMatcher(q.blockId()), cap, radiusChunks(args));
                  break;
               case STRUCTURE:
                  var10000 = locateStructureOp(q.structure(), cap, AiJson.getString(args, "seed", ""));
                  break;
               default:
                  throw new MatchException(null, null);
            }

            return var10000;
         } else {
            throw new IllegalArgumentException("No seed knowledge in a seed-free run: find it by observation or exploration");
         }
      } else {
         throw new IllegalArgumentException("Oracle queries require a connected living player");
      }
   }

   static AiOracle.OracleQuery parseQuery(String raw) {
      String q = raw == null ? "" : raw.strip().toLowerCase(Locale.ROOT);
      if (q.isEmpty()) {
         throw new IllegalArgumentException(
            "query is required: lava, water, village, threats, an ore kind (coal, copper, diamond, debris, emerald, gold, iron, lapis, quartz, redstone or ancient_debris), a structure id (stronghold, fortress, ... or structure:<id>), or a registry block id (minecraft:xxx)"
         );
      }

      switch (q) {
         case "lava":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.LAVA, null, null, null);
         case "water":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.WATER, null, null, null);
         case "village":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.VILLAGE, null, null, null);
         case "threats":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.THREATS, null, null, null);
         case "structure":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.STRUCTURE, null, null, structureSpec("stronghold"));
         case "ancient_debris":
            return new AiOracle.OracleQuery(AiOracle.QuerySubject.ORE, autismclient.util.oresim.OreBlockCatalog.OreKind.DEBRIS, null, null);
         default:
            if (q.startsWith("structure:")) {
               return structureQuery(q.substring("structure:".length()));
            } else {
               for (autismclient.util.oresim.OreBlockCatalog.OreKind kind : autismclient.util.oresim.OreBlockCatalog.OreKind.values()) {
                  if (kind.init_Str.equals(q)) {
                     return new AiOracle.OracleQuery(AiOracle.QuerySubject.ORE, kind, null, null);
                  }
               }

               AiOracle.StructureSpec direct = structureSpec(q);
               if (direct != null) {
                  return new AiOracle.OracleQuery(AiOracle.QuerySubject.STRUCTURE, null, null, direct);
               } else {
                  Identifier id = Identifier.tryParse(q);
                  if (id == null) {
                     throw new IllegalArgumentException(
                        "Unknown oracle query \""
                           + q
                           + "\"; expected lava, water, village, threats, an ore kind, a structure id, or a registry block id (minecraft:xxx)"
                     );
                  } else if (BuiltInRegistries.BLOCK.getOptional(id).isEmpty()) {
                     throw new IllegalArgumentException(
                        "Unknown block \"" + id + "\"; use an existing registry block id (e.g. minecraft:lava, minecraft:stone)"
                     );
                  } else {
                     return new AiOracle.OracleQuery(AiOracle.QuerySubject.BLOCK, null, id.toString(), null);
                  }
               }
            }
      }
   }

   private static AiOracle.OracleQuery structureQuery(String name) {
      AiOracle.StructureSpec spec = structureSpec(name);
      if (spec == null) {
         throw new IllegalArgumentException("Unknown structure \"" + name + "\"; known structures: " + String.join(", ", STRUCTURES.keySet()));
      } else {
         return new AiOracle.OracleQuery(AiOracle.QuerySubject.STRUCTURE, null, null, spec);
      }
   }

   static AiOracle.StructureSpec structureSpec(String name) {
      if (name == null) {
         return null;
      }

      String key = name.strip().toLowerCase(Locale.ROOT);
      int colon = key.indexOf(58);
      if (colon >= 0) {
         key = key.substring(colon + 1);
      }

      return STRUCTURES.get(key);
   }

   private static Map<String, AiOracle.StructureSpec> buildStructureSpecs() {
      Set<String> ow = Set.of("overworld");
      Set<String> ne = Set.of("nether");
      Set<String> en = Set.of("end");
      Set<String> owNe = Set.of("overworld", "nether");
      Map<String, AiOracle.StructureSpec> m = new LinkedHashMap<>();
      m.put("stronghold", new AiOracle.StructureSpec("stronghold", List.of("minecraft:stronghold"), ow));
      m.put("fortress", new AiOracle.StructureSpec("fortress", List.of("minecraft:fortress"), ne));
      m.put("bastion_remnant", new AiOracle.StructureSpec("bastion_remnant", List.of("minecraft:bastion_remnant"), ne));
      m.put("bastion", m.get("bastion_remnant"));
      m.put("nether_fossil", new AiOracle.StructureSpec("nether_fossil", List.of("minecraft:nether_fossil"), ne));
      m.put("end_city", new AiOracle.StructureSpec("end_city", List.of("minecraft:end_city"), en));
      m.put("endcity", m.get("end_city"));
      m.put(
         "village",
         new AiOracle.StructureSpec(
            "village",
            List.of("minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga"),
            ow
         )
      );
      m.put("village_plains", new AiOracle.StructureSpec("village_plains", List.of("minecraft:village_plains"), ow));
      m.put("village_desert", new AiOracle.StructureSpec("village_desert", List.of("minecraft:village_desert"), ow));
      m.put("village_savanna", new AiOracle.StructureSpec("village_savanna", List.of("minecraft:village_savanna"), ow));
      m.put("village_snowy", new AiOracle.StructureSpec("village_snowy", List.of("minecraft:village_snowy"), ow));
      m.put("village_taiga", new AiOracle.StructureSpec("village_taiga", List.of("minecraft:village_taiga"), ow));
      m.put("mansion", new AiOracle.StructureSpec("mansion", List.of("minecraft:mansion"), ow));
      m.put("monument", new AiOracle.StructureSpec("monument", List.of("minecraft:monument"), ow));
      m.put("pillager_outpost", new AiOracle.StructureSpec("pillager_outpost", List.of("minecraft:pillager_outpost"), ow));
      m.put("outpost", m.get("pillager_outpost"));
      m.put(
         "ruined_portal",
         new AiOracle.StructureSpec(
            "ruined_portal",
            List.of(
               "minecraft:ruined_portal",
               "minecraft:ruined_portal_desert",
               "minecraft:ruined_portal_jungle",
               "minecraft:ruined_portal_mountain",
               "minecraft:ruined_portal_ocean",
               "minecraft:ruined_portal_swamp",
               "minecraft:ruined_portal_nether"
            ),
            owNe
         )
      );
      m.put("ruined_portal_desert", new AiOracle.StructureSpec("ruined_portal_desert", List.of("minecraft:ruined_portal_desert"), ow));
      m.put("ruined_portal_jungle", new AiOracle.StructureSpec("ruined_portal_jungle", List.of("minecraft:ruined_portal_jungle"), ow));
      m.put("ruined_portal_mountain", new AiOracle.StructureSpec("ruined_portal_mountain", List.of("minecraft:ruined_portal_mountain"), ow));
      m.put("ruined_portal_ocean", new AiOracle.StructureSpec("ruined_portal_ocean", List.of("minecraft:ruined_portal_ocean"), ow));
      m.put("ruined_portal_swamp", new AiOracle.StructureSpec("ruined_portal_swamp", List.of("minecraft:ruined_portal_swamp"), ow));
      m.put("ruined_portal_nether", new AiOracle.StructureSpec("ruined_portal_nether", List.of("minecraft:ruined_portal_nether"), ne));
      m.put("mineshaft", new AiOracle.StructureSpec("mineshaft", List.of("minecraft:mineshaft", "minecraft:mineshaft_mesa"), ow));
      m.put("mineshaft_mesa", new AiOracle.StructureSpec("mineshaft_mesa", List.of("minecraft:mineshaft_mesa"), ow));
      m.put("ancient_city", new AiOracle.StructureSpec("ancient_city", List.of("minecraft:ancient_city"), ow));
      m.put("buried_treasure", new AiOracle.StructureSpec("buried_treasure", List.of("minecraft:buried_treasure"), ow));
      m.put("desert_pyramid", new AiOracle.StructureSpec("desert_pyramid", List.of("minecraft:desert_pyramid"), ow));
      m.put("jungle_pyramid", new AiOracle.StructureSpec("jungle_pyramid", List.of("minecraft:jungle_pyramid"), ow));
      m.put("igloo", new AiOracle.StructureSpec("igloo", List.of("minecraft:igloo"), ow));
      m.put("ocean_ruin", new AiOracle.StructureSpec("ocean_ruin", List.of("minecraft:ocean_ruin_cold", "minecraft:ocean_ruin_warm"), ow));
      m.put("ocean_ruin_cold", new AiOracle.StructureSpec("ocean_ruin_cold", List.of("minecraft:ocean_ruin_cold"), ow));
      m.put("ocean_ruin_warm", new AiOracle.StructureSpec("ocean_ruin_warm", List.of("minecraft:ocean_ruin_warm"), ow));
      m.put("shipwreck", new AiOracle.StructureSpec("shipwreck", List.of("minecraft:shipwreck", "minecraft:shipwreck_beached"), ow));
      m.put("shipwreck_beached", new AiOracle.StructureSpec("shipwreck_beached", List.of("minecraft:shipwreck_beached"), ow));
      m.put("swamp_hut", new AiOracle.StructureSpec("swamp_hut", List.of("minecraft:swamp_hut"), ow));
      m.put("trail_ruins", new AiOracle.StructureSpec("trail_ruins", List.of("minecraft:trail_ruins"), ow));
      m.put("trial_chambers", new AiOracle.StructureSpec("trial_chambers", List.of("minecraft:trial_chambers"), ow));
      return Collections.unmodifiableMap(m);
   }

   static int oreKindMask(autismclient.util.oresim.OreBlockCatalog.OreKind kind) {
      return 1 << kind.ordinal();
   }

   static int clampCap(int cap) {
      return Math.max(1, Math.min(32, cap));
   }

   static int clampRadius(int radiusChunks) {
      return Math.max(2, Math.min(16, radiusChunks));
   }

   static List<AiOracle.BlockHit> sortByDistance(List<AiOracle.BlockHit> found) {
      List<AiOracle.BlockHit> ordered = new ArrayList<>(found);
      ordered.sort(Comparator.comparingDouble(AiOracle.BlockHit::distanceSq));
      return ordered;
   }

   static List<int[]> chunksAround(int centerX, int centerZ, int radiusChunks) {
      List<int[]> chunks = new ArrayList<>();

      for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
         for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
            chunks.add(new int[]{centerX + dx, centerZ + dz});
         }
      }

      chunks.sort(
         Comparator.<int[]>comparingInt(c -> (c[0] - centerX) * (c[0] - centerX) + (c[1] - centerZ) * (c[1] - centerZ))
            .thenComparingInt(c -> c[0])
            .thenComparingInt(c -> c[1])
      );
      return chunks;
   }

   static AiOracle.ResolvedSeed resolveSeed(String seedArg, AiOracle.SeedProbe probe) {
      String raw = seedArg == null ? "" : seedArg.strip();
      if (!raw.isEmpty()) {
         autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil parsed = autismclient.util.oresim.OreSimSeedParser.parse(raw);
         if (!parsed.isValid()) {
            throw new IllegalArgumentException("seed must be a signed decimal long, got \"" + raw + "\"");
         } else {
            return new AiOracle.ResolvedSeed(parsed.seed(), "argument");
         }
      } else {
         Long local = probe.integratedSeed();
         if (local != null) {
            return new AiOracle.ResolvedSeed(local, "singleplayer");
         } else {
            autismclient.util.oresim.OreSimSeedParser.Inner_DataUtil stored = autismclient.util.oresim.OreSimSeedParser.parse(probe.storedSeed());
            if (stored.isValid()) {
               return new AiOracle.ResolvedSeed(stored.seed(), "stored");
            } else if (stored.status() == autismclient.util.oresim.OreSimSeedParser.Inner_StreamUtil.INVALID) {
               throw new IllegalArgumentException(
                  "The stored oresim-seed for this server is not a signed decimal long; fix the xray module's oresim-seed setting or pass the seed argument"
               );
            } else {
               throw new IllegalArgumentException(
                  "No world seed available for prediction: pass the seed argument, play singleplayer (seed resolves automatically), or set the xray module's oresim-seed setting for this server"
               );
            }
         }
      }
   }

   private static AiOracle.SeedProbe seedProbe(final Minecraft mc) {
      return new AiOracle.SeedProbe() {
         @Override
         public Long integratedSeed() {
            IntegratedServer server = mc.getSingleplayerServer();
            return server == null ? null : server.getWorldGenSettings().options().seed();
         }

         @Override
         public String storedSeed() {
            return AutismOreSimSeedStore.getInstance().seedFor(AutismWaypoints.scopeFor(mc), "");
         }
      };
   }

   private static int radiusChunks(JsonObject args) {
      return clampRadius(AiJson.getInt(args, "radiusChunks", autismclient.modules.am.init_I()));
   }

   private static Predicate<BlockState> blockMatcher(String blockId) {
      Block block = BuiltInRegistries.BLOCK
         .getOptional(Identifier.parse(blockId))
         .orElseThrow(() -> new IllegalArgumentException("Unknown block \"" + blockId + "\""));
      return state -> state.getBlock() == block;
   }

   private static long hideVillagesMask() {
      if (hideVillagesBit < 0L) {
         for (int i = 0; i < autismclient.modules.StructureStorageFilter.STRUCTURES.size(); i++) {
            if ("hide-villages".equals(autismclient.modules.StructureStorageFilter.STRUCTURES.get(i).id())) {
               hideVillagesBit = 1L << i;
               break;
            }
         }

         if (hideVillagesBit < 0L) {
            throw new IllegalStateException("ModuleStorageStructures.ALL has no village entry");
         }
      }

      return hideVillagesBit;
   }

   private static AiOperation locateOre(final autismclient.util.oresim.OreBlockCatalog.OreKind kind, final int cap, String seedArg) {
      final Minecraft mc = Minecraft.getInstance();
      final AiOracle.ResolvedSeed seed = resolveSeed(seedArg, seedProbe(mc));
      final int mask = oreKindMask(kind);
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + 60000000000L;
         int simTicks;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               autismclient.util.oresim.OreSimEngine.requestScan(mc.level, mc.player.blockPosition(), seed.value(), 8, mask);
               if (autismclient.util.oresim.OreSimEngine.hasError()) {
                  return this.finish(
                     AiOperation.Status.FAILED,
                     "Ore simulation failed (status "
                        + autismclient.util.oresim.OreSimEngine.status()
                        + "): "
                        + autismclient.util.oresim.OreSimEngine.errorMessage(),
                     new JsonObject()
                  );
               }

               if (autismclient.util.oresim.OreSimEngine.status() == autismclient.util.oresim.OreSimEngine.EngineStatus.UNSUPPORTED_DIMENSION) {
                  return this.finish(
                     AiOperation.Status.FAILED,
                     "Ore simulation supports only the Overworld, Nether and End; the current dimension "
                        + mc.level.dimension().identifier()
                        + " is unsupported",
                     new JsonObject()
                  );
               }

               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(
                     AiOperation.Status.TIMED_OUT,
                     "Ore simulation did not finish within 60s (status " + autismclient.util.oresim.OreSimEngine.status() + ")",
                     this.statusData()
                  );
               }

               if (!autismclient.util.oresim.OreSimEngine.isReady()) {
                  return this.running("Loading vanilla worldgen context for seed simulation");
               }

               autismclient.util.oresim.OreSimEngine.GenerationProgress progress = autismclient.util.oresim.OreSimEngine.progress();
               if (!progress.isComplete() && ++this.simTicks < 400) {
                  return this.running("Simulating ore generation (" + progress.done() + "/" + progress.total() + " chunks)");
               }

               Vec3 eye = mc.player.getEyePosition();
               long[] outPos = new long[cap];
               int[] outState = new int[cap];
               int n = autismclient.util.oresim.OreSimEngine.nearest(
                  eye.x,
                  eye.y,
                  eye.z,
                  8,
                  stateId -> autismclient.util.oresim.OreBlockCatalog.StateRegistry.kindById(stateId) == kind,
                  cap,
                  outPos,
                  outState
               );
               JsonArray positions = new JsonArray();

               for (int i = 0; i < n; i++) {
                  BlockPos p = BlockPos.of(outPos[i]);
                  positions.add(
                     AiJson.objectOf(
                        "x",
                        p.getX(),
                        "y",
                        p.getY(),
                        "z",
                        p.getZ(),
                        "distance",
                        AiOracle.round1(Math.sqrt(eye.distanceToSqr(Vec3.atCenterOf(p)))),
                        "kind",
                        kind.init_Str,
                        "predicted",
                        true
                     )
                  );
               }

               return this.finish(
                  AiOperation.Status.SUCCEEDED,
                  n
                     + " predicted "
                     + kind.init_Str
                     + " position(s), nearest-first; predicted from world seed simulation (seed from "
                     + seed.source()
                     + "); confirm by observation",
                  AiJson.objectOf(
                     "query",
                     kind.init_Str,
                     "predicted",
                     true,
                     "observed",
                     false,
                     "seedSource",
                     seed.source(),
                     "note",
                     "predicted from world seed simulation; confirm by observation",
                     "simulatedChunks",
                     progress.done(),
                     "totalChunks",
                     progress.total(),
                     "positions",
                     positions
                  )
               );
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
            }
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.statusData());
         }

         JsonObject statusData() {
            autismclient.util.oresim.OreSimEngine.GenerationProgress p = autismclient.util.oresim.OreSimEngine.progress();
            return AiJson.objectOf(
               "engineStatus", autismclient.util.oresim.OreSimEngine.status().name(), "simulatedChunks", p.done(), "totalChunks", p.total()
            );
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
            if (!this.cancelled) {
               this.cancelled = true;
               autismclient.util.oresim.OreSimEngine.pause();
            }
         }
      };
   }

   private static AiOperation locateStructureOp(final AiOracle.StructureSpec spec, final int cap, String seedArg) {
      final Minecraft mc = Minecraft.getInstance();
      final AiOracle.ResolvedSeed seed = resolveSeed(seedArg, seedProbe(mc));
      final ResourceKey<Level> dimension = mc.level.dimension();
      final String dim = dimensionName(dimension);
      if (dim == null) {
         throw new IllegalArgumentException("Structure locating supports only the Overworld, Nether and End");
      } else {
         return !spec.dimensions().contains(dim)
            ? AiGameOperations.immediate(
               AiOperation.Status.FAILED,
               spec.label() + " does not generate in the " + dim + " (generates in: " + String.join(", ", spec.dimensions()) + ")",
               AiJson.objectOf("query", spec.label(), "dimension", dim, "predicted", true, "observed", false)
            )
            : new AiOperation() {
               final long deadlineNanos = System.nanoTime() + 60000000000L;
               final AtomicBoolean cancelled = new AtomicBoolean();
               volatile CompletableFuture<AiOracle.StructureSearchOutcome> task;

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled.get() && mc.player != null && mc.level != null) {
                     if (this.task == null) {
                        BlockPos origin = mc.player.blockPosition();
                        this.task = CompletableFuture.supplyAsync(
                           () -> AiOracle.locateStructure(seed.value(), dimension, spec, origin, cap, this.cancelled), AiOracle.WORLDGEN_EXECUTOR
                        );
                     }

                     if (!this.task.isDone()) {
                        if (System.nanoTime() > this.deadlineNanos) {
                           this.cancel();
                           return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "Structure location did not finish within 60s", this.statusData());
                        } else {
                           return AiGameOperations.result(
                              AiOperation.Status.RUNNING, "Locating " + spec.label() + " from world seed simulation (off-thread vanilla worldgen)", this.statusData()
                           );
                        }
                     } else {
                        AiOracle.StructureSearchOutcome outcome;
                        try {
                           outcome = this.task.get();
                        } catch (Exception error) {
                           return this.finish(AiOperation.Status.FAILED, "Structure location failed: " + error, new JsonObject());
                        }

                        if (this.cancelled.get()) {
                           return this.finish(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
                        }

                        if (outcome.error() != null) {
                           return this.finish(
                              AiOperation.Status.FAILED,
                              outcome.error(),
                              AiJson.objectOf("query", spec.label(), "dimension", dim, "predicted", true, "observed", false)
                           );
                        }

                        JsonArray positions = new JsonArray();

                        for (AiOracle.StructureHit hit : outcome.hits()) {
                           positions.add(
                              AiJson.objectOf(
                                 "x",
                                 hit.x(),
                                 "y",
                                 hit.y(),
                                 "z",
                                 hit.z(),
                                 "distance",
                                 AiOracle.round1(Math.sqrt(hit.distanceSq())),
                                 "structure",
                                 hit.structureId(),
                                 "predicted",
                                 true
                              )
                           );
                        }

                        return this.finish(
                           AiOperation.Status.SUCCEEDED,
                           positions.size()
                              + " predicted "
                              + spec.label()
                              + " position(s), nearest-first; predicted from world seed simulation (seed from "
                              + seed.source()
                              + "); confirm by observation",
                           AiJson.objectOf(
                              "query",
                              spec.label(),
                              "dimension",
                              dim,
                              "predicted",
                              true,
                              "observed",
                              false,
                              "seedSource",
                              seed.source(),
                              "note",
                              "predicted from world seed simulation; confirm by observation",
                              "positions",
                              positions
                           )
                        );
                     }
                  } else {
                     this.cancel();
                     return AiGameOperations.result(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
                  }
               }

               JsonObject statusData() {
                  return AiJson.objectOf("query", spec.label(), "dimension", dim, "worker", "ai-oracle-structures");
               }

               AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
                  this.cancel();
                  return AiGameOperations.result(status, message, data);
               }

               @Override
               public void cancel() {
                  this.cancelled.set(true);
                  CompletableFuture<AiOracle.StructureSearchOutcome> running = this.task;
                  if (running != null) {
                     running.cancel(true);
                  }
               }
            };
      }
   }

   private static AiOracle.StructureSearchOutcome locateStructure(
      long seed, ResourceKey<Level> dimension, AiOracle.StructureSpec spec, BlockPos origin, int cap, AtomicBoolean cancelled
   ) {
      try {
         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context = worldGen(seed, dimension);
         if (cancelled.get()) {
            return new AiOracle.StructureSearchOutcome(List.of(), null);
         }

         RegistryLookup<Structure> registry = context.registries().lookupOrThrow(Registries.STRUCTURE);
         List<Holder<Structure>> wanted = new ArrayList<>();

         for (String id : spec.ids()) {
            registry.get(ResourceKey.create(Registries.STRUCTURE, Identifier.parse(id))).ifPresent(wanted::add);
         }

         if (wanted.isEmpty()) {
            return new AiOracle.StructureSearchOutcome(List.of(), "None of " + spec.ids() + " exists in the vanilla 26.2 structure registry");
         } else {
            List<AiOracle.StructureHit> hits = findStructures(context, wanted, origin, cap, cancelled);
            if (cancelled.get()) {
               return new AiOracle.StructureSearchOutcome(List.of(), null);
            } else {
               return hits.isEmpty()
                  ? new AiOracle.StructureSearchOutcome(
                     List.of(),
                     "No " + spec.label() + " within 25 structure sectors of " + origin.toShortString() + "; seed simulation cannot reliably search further"
                  )
                  : new AiOracle.StructureSearchOutcome(hits, null);
            }
         }
      } catch (Throwable error) {
         return new AiOracle.StructureSearchOutcome(
            List.of(), "Structure location failed: " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage())
         );
      }
   }

   private static autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap worldGen(long seed, ResourceKey<Level> dimension) {
      String key = seed + "@" + dimension.identifier();
      synchronized (WORLDGEN_LOCK) {
         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap existing = WORLDGEN_CACHE.get(key);
         if (existing != null) {
            return existing;
         }

         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap created = autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap.create(seed, dimension);
         WORLDGEN_CACHE.put(key, created);
         return created;
      }
   }

   private static List<AiOracle.StructureHit> findStructures(
      autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context, List<Holder<Structure>> wanted, BlockPos origin, int cap, AtomicBoolean cancelled
   ) {
      ChunkGeneratorStructureState state = context.structureState();
      long seed = state.getLevelSeed();
      Map<StructurePlacement, Set<Holder<Structure>>> scans = new LinkedHashMap<>();

      for (Holder<Structure> holder : wanted) {
         for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
            scans.computeIfAbsent(placement, p -> new LinkedHashSet<>()).add(holder);
         }
      }

      List<AiOracle.StructureHit> hits = new ArrayList<>();

      for (Entry<StructurePlacement, Set<Holder<Structure>>> entry : scans.entrySet()) {
         if (cancelled.get() || hits.size() >= cap) {
            break;
         }

         StructurePlacement placement = entry.getKey();
         if (placement instanceof ConcentricRingsStructurePlacement rings) {
            List<ChunkPos> positions = state.getRingPositionsFor(rings);
            if (positions != null) {
               List<ChunkPos> ordered = new ArrayList<>(positions);
               ordered.sort(
                  Comparator.comparingDouble(
                     c -> origin.distSqr(new BlockPos(SectionPos.sectionToBlockCoord(c.x(), 8), 0, SectionPos.sectionToBlockCoord(c.z(), 8)))
                  )
               );

               for (ChunkPos candidate : ordered) {
                  if (cancelled.get() || hits.size() >= cap) {
                     break;
                  }

                  collectStructureHits(context, entry.getValue(), candidate, origin, seed, hits);
               }
            }
         } else if (placement instanceof RandomSpreadStructurePlacement spread) {
            int spacing = spread.spacing();
            int originX = SectionPos.blockToSectionCoord(origin.getX());
            int originZ = SectionPos.blockToSectionCoord(origin.getZ());

            for (int radius = 0; radius <= 25 && hits.size() < cap && !cancelled.get(); radius++) {
               for (int x = -radius; x <= radius && hits.size() < cap; x++) {
                  for (int z = -radius; z <= radius && hits.size() < cap; z++) {
                     if (Math.max(Math.abs(x), Math.abs(z)) == radius) {
                        if (cancelled.get()) {
                           break;
                        }

                        ChunkPos candidate = spread.getPotentialStructureChunk(seed, originX + spacing * x, originZ + spacing * z);
                        if (spread.applyAdditionalChunkRestrictions(candidate.x(), candidate.z(), seed)) {
                           collectStructureHits(context, entry.getValue(), candidate, origin, seed, hits);
                        }
                     }
                  }
               }
            }
         }
      }

      hits.sort(Comparator.comparingDouble(AiOracle.StructureHit::distanceSq));
      return hits.size() > cap ? new ArrayList<>(hits.subList(0, cap)) : hits;
   }

   private static void collectStructureHits(
      autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context,
      Set<Holder<Structure>> structures,
      ChunkPos candidate,
      BlockPos origin,
      long seed,
      List<AiOracle.StructureHit> hits
   ) {
      for (Holder<Structure> holder : structures) {
         Structure structure = holder.value();
         GenerationContext generation = new GenerationContext(
            context.registryAccess(),
            context.chunkGenerator(),
            context.biomeSource(),
            context.randomState().createClimateSampler(null),
            context.randomState(),
            context.structureTemplates(),
            seed,
            candidate,
            context.heightAccessor(),
            structure.biomes()::contains
         );
         Optional<GenerationStub> stub = structure.findValidGenerationPoint(generation);
         if (!stub.isEmpty()) {
            BlockPos pos = stub.get().position();
            hits.add(new AiOracle.StructureHit(pos.getX(), pos.getY(), pos.getZ(), structureId(holder), origin.distSqr(pos)));
            return;
         }
      }
   }

   private static String structureId(Holder<Structure> holder) {
      return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
   }

   private static String dimensionName(ResourceKey<Level> dimension) {
      if (Level.OVERWORLD.equals(dimension)) {
         return "overworld";
      } else if (Level.NETHER.equals(dimension)) {
         return "nether";
      } else {
         return Level.END.equals(dimension) ? "end" : null;
      }
   }

   static AiOperation findBiome(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         throw new IllegalArgumentException("Biome location requires a connected living player");
      }

      if (isActive()) {
         throw new IllegalArgumentException("No seed knowledge in a seed-free run: find the biome by exploration");
      }

      final String biomeId = resolveBiomeId(AiJson.getString(args, "biome", ""), biomeIds(mc));
      final int radiusBlocks = Math.max(64, Math.min(16384, AiJson.getInt(args, "radiusBlocks", 4096)));
      final AiOracle.ResolvedSeed seed = resolveSeed(AiJson.getString(args, "seed", ""), seedProbe(mc));
      ResourceKey<Level> dimension = args.has("dimension") ? parseDimension(AiJson.getString(args, "dimension", "")) : mc.level.dimension();
      if (dimension == null) {
         throw new IllegalArgumentException("dimension must be overworld, nether or end");
      }

      final ResourceKey<Level> targetDimension = dimension;
      final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)));
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
         final AtomicBoolean cancelled = new AtomicBoolean();
         volatile CompletableFuture<AiOracle.BiomeSearchOutcome> task;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled.get() && mc.player != null && mc.level != null) {
               if (this.task == null) {
                  BlockPos origin = mc.player.blockPosition();
                  this.task = CompletableFuture.supplyAsync(
                     () -> AiOracle.locateBiome(seed.value(), targetDimension, biomeId, origin, radiusBlocks, this.cancelled),
                     AiOracle.WORLDGEN_EXECUTOR
                  );
               }

               if (!this.task.isDone()) {
                  if (System.nanoTime() > this.deadlineNanos) {
                     this.cancel();
                     return AiGameOperations.result(AiOperation.Status.TIMED_OUT, "Biome location did not finish within " + timeoutSeconds + "s", this.statusData());
                  } else {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING,
                        "Sampling vanilla biome generation for " + biomeId + " (off-thread, seed from " + seed.source() + ")",
                        this.statusData()
                     );
                  }
               } else {
                  AiOracle.BiomeSearchOutcome outcome;
                  try {
                     outcome = this.task.get();
                  } catch (Exception error) {
                     return this.finish(AiOperation.Status.FAILED, "Biome location failed: " + error, new JsonObject());
                  }

                  if (this.cancelled.get()) {
                     return this.finish(AiOperation.Status.CANCELLED, "Biome location cancelled", new JsonObject());
                  } else if (outcome.error() != null) {
                     this.cancel();
                     return AiOperation.Result.failed(
                        AiFailure.UNREACHABLE, outcome.error(), AiJson.objectOf("biome", biomeId, "predicted", true, "observed", false).toString()
                     );
                  } else {
                     AiOracle.BiomeHit hit = outcome.hit();
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        "Predicted "
                           + hit.biomeId()
                           + " at "
                           + hit.x()
                           + ", "
                           + hit.y()
                           + ", "
                           + hit.z()
                           + " ("
                           + AiOracle.round1(Math.sqrt(hit.distanceSq()))
                           + " blocks); predicted from world seed simulation (seed from "
                           + seed.source()
                           + "); confirm by observation",
                        AiJson.objectOf(
                           "biome",
                           hit.biomeId(),
                           "predicted",
                           true,
                           "observed",
                           false,
                           "seedSource",
                           seed.source(),
                           "note",
                           "predicted from world seed simulation; confirm by observation",
                           "position",
                           AiJson.objectOf("x", hit.x(), "y", hit.y(), "z", hit.z()),
                           "distance",
                           AiOracle.round1(Math.sqrt(hit.distanceSq()))
                        )
                     );
                  }
               }
            } else {
               this.cancel();
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Biome location cancelled", new JsonObject());
            }
         }

         JsonObject statusData() {
            return AiJson.objectOf("biome", biomeId, "worker", "ai-oracle-structures");
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
            this.cancelled.set(true);
            CompletableFuture<AiOracle.BiomeSearchOutcome> running = this.task;
            if (running != null) {
               running.cancel(true);
            }
         }
      };
   }

   private static AiOracle.BiomeSearchOutcome locateBiome(
      long seed, ResourceKey<Level> dimension, String biomeId, BlockPos origin, int radiusBlocks, AtomicBoolean cancelled
   ) {
      try {
         autismclient.util.oresim.worldgen.VanillaWorldGenBootstrap context = worldGen(seed, dimension);
         if (cancelled.get()) {
            return new AiOracle.BiomeSearchOutcome(null, null);
         }

         BiomeSource biomeSource = context.biomeSource();
         int quartY = origin.getY() >> 2;
         int centerCellX = Math.floorDiv(origin.getX(), 64);
         int centerCellZ = Math.floorDiv(origin.getZ(), 64);
         int radiusCells = Math.max(1, radiusBlocks / 64);

         for (int[] cell : cellsByRing(centerCellX, centerCellZ, radiusCells)) {
            if (cancelled.get()) {
               return new AiOracle.BiomeSearchOutcome(null, null);
            }

            int x = cell[0] * 64 + 32;
            int z = cell[1] * 64 + 32;
            Holder<Biome> biome = biomeSource.createCachingResolver(context.randomState()).getNoiseBiome(x >> 2, quartY, z >> 2);
            String sampled = biomeId(biome);
            if (biomeId.equals(sampled)) {
               double dx = x - origin.getX();
               double dz = z - origin.getZ();
               return new AiOracle.BiomeSearchOutcome(new AiOracle.BiomeHit(x, origin.getY(), z, sampled, dx * dx + dz * dz), null);
            }
         }

         return new AiOracle.BiomeSearchOutcome(
            null, "No " + biomeId + " within " + radiusBlocks + " blocks of " + origin.toShortString() + " (seed-simulated biome sampling at 64-block cells)"
         );
      } catch (Throwable error) {
         return new AiOracle.BiomeSearchOutcome(
            null, "Biome location failed: " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage())
         );
      }
   }

   static String resolveBiomeId(String spec, Collection<String> biomeIds) {
      String clean = spec == null ? "" : spec.trim().toLowerCase(Locale.ROOT);
      if (clean.isEmpty()) {
         throw new IllegalArgumentException("biome is required: a registry id like minecraft:plains or a substring like \"desert\"");
      }

      for (String id : biomeIds) {
         if (id.equals(clean)) {
            return id;
         }
      }

      String bare = clean.contains(":") ? clean : "minecraft:" + clean;

      for (String id : biomeIds) {
         if (id.equals(bare)) {
            return id;
         }
      }

      List<String> matches = new ArrayList<>();

      for (String id : biomeIds) {
         if (id.contains(clean)) {
            matches.add(id);
         }
      }

      if (!matches.isEmpty()) {
         if (matches.size() == 1) {
            return matches.get(0);
         }

         for (String id : matches) {
            if (id.substring(id.indexOf(58) + 1).equals(clean)) {
               return id;
            }
         }

         Collections.sort(matches);
         throw new IllegalArgumentException(
            "Ambiguous biome \""
               + spec
               + "\": "
               + String.join(", ", matches.subList(0, Math.min(5, matches.size())))
               + (matches.size() > 5 ? ", ..." : "")
               + "; use a full registry id"
         );
      } else {
         List<String> near = new ArrayList<>();

         for (String id : biomeIds) {
            if (near.size() >= 5) {
               break;
            }

            String path = id.substring(id.indexOf(58) + 1);
            if (clean.contains(path)) {
               near.add(id);
            }
         }

         throw new IllegalArgumentException("Unknown biome \"" + spec + "\"" + (near.isEmpty() ? "" : " (did you mean " + String.join(", ", near) + "?)"));
      }
   }

   static List<int[]> cellsByRing(int centerCellX, int centerCellZ, int radiusCells) {
      List<int[]> cells = new ArrayList<>();

      for (int radius = 0; radius <= radiusCells; radius++) {
         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                  cells.add(new int[]{centerCellX + dx, centerCellZ + dz});
               }
            }
         }
      }

      return cells;
   }

   private static List<String> biomeIds(Minecraft mc) {
      return mc.level.registryAccess().lookupOrThrow(Registries.BIOME).keySet().stream().map(Identifier::toString).toList();
   }

   private static String biomeId(Holder<Biome> holder) {
      return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
   }

   private static ResourceKey<Level> parseDimension(String name) {
      ResourceKey var10000;
      switch (name.trim().toLowerCase(Locale.ROOT)) {
         case "overworld":
         case "minecraft:overworld":
            var10000 = Level.OVERWORLD;
            break;
         case "nether":
         case "the_nether":
         case "minecraft:the_nether":
            var10000 = Level.NETHER;
            break;
         case "end":
         case "the_end":
         case "minecraft:the_end":
            var10000 = Level.END;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   private static AiOperation scanBlocks(final String label, final Predicate<BlockState> target, final int cap, final int radiusChunks) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + 60000000000L;
         final List<AiOracle.BlockHit> found = new ArrayList<>();
         List<int[]> chunkOrder;
         int nextChunk;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Block scan deadline reached", this.statusData());
               }

               if (this.chunkOrder == null) {
                  this.chunkOrder = AiOracle.chunksAround(mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4, radiusChunks);
               }

               Vec3 eye = mc.player.getEyePosition();

               for (int budget = 8; budget > 0 && this.nextChunk < this.chunkOrder.size() && this.found.size() < 4096; budget--) {
                  int[] c = this.chunkOrder.get(this.nextChunk++);
                  LevelChunk chunk = mc.level.getChunkSource().getChunk(c[0], c[1], ChunkStatus.FULL, false);
                  if (chunk != null) {
                     AiOracle.collectMatches(mc.level, chunk, target, eye, this.found);
                  }
               }

               if (this.nextChunk < this.chunkOrder.size() && this.found.size() < 4096) {
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Scanning loaded chunks for " + label + " (" + this.nextChunk + "/" + this.chunkOrder.size() + ")", this.statusData()
                  );
               }

               List<AiOracle.BlockHit> ordered = AiOracle.sortByDistance(this.found);
               JsonArray positions = new JsonArray();

               for (int i = 0; i < ordered.size() && i < cap; i++) {
                  AiOracle.BlockHit f = ordered.get(i);
                  positions.add(
                     AiJson.objectOf(
                        "x", f.x(), "y", f.y(), "z", f.z(), "distance", AiOracle.round1(Math.sqrt(f.distanceSq())), "block", label, "observed", true
                     )
                  );
               }

               return this.finish(
                  AiOperation.Status.SUCCEEDED,
                  positions.size() + " observed " + label + " position(s) in loaded terrain, nearest-first",
                  AiJson.objectOf(
                     "query",
                     label,
                     "observed",
                     true,
                     "predicted",
                     false,
                     "matches",
                     this.found.size(),
                     "scannedChunks",
                     this.nextChunk,
                     "positions",
                     positions
                  )
               );
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Oracle scan cancelled", new JsonObject());
            }
         }

         JsonObject statusData() {
            return AiJson.objectOf(
               "query", label, "scannedChunks", this.nextChunk, "totalChunks", this.chunkOrder == null ? 0 : this.chunkOrder.size(), "matches", this.found.size()
            );
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
            this.cancelled = true;
         }
      };
   }

   private static void collectMatches(ClientLevel level, LevelChunk chunk, Predicate<BlockState> target, Vec3 origin, List<AiOracle.BlockHit> out) {
      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();

      for (int i = 0; i < sections.length && out.size() < 4096; i++) {
         LevelChunkSection section = sections[i];
         if (section != null && !section.hasOnlyAir() && section.maybeHas(target)) {
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

            for (int sy = 0; sy < 16 && out.size() < 4096; sy++) {
               int y = baseY + sy;
               if (!level.isOutsideBuildHeight(y)) {
                  for (int sx = 0; sx < 16 && out.size() < 4096; sx++) {
                     for (int sz = 0; sz < 16 && out.size() < 4096; sz++) {
                        BlockState state = section.getBlockState(sx, sy, sz);
                        if (target.test(state)) {
                           int x = minX + sx;
                           int z = minZ + sz;
                           out.add(new AiOracle.BlockHit(x, y, z, origin.distanceToSqr(x + 0.5, y + 0.5, z + 0.5)));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static AiOperation scanVillages(final int cap, final int radiusChunks) {
      final Minecraft mc = Minecraft.getInstance();
      final long mask = hideVillagesMask();
      return new AiOperation() {
         final long deadlineNanos = System.nanoTime() + 60000000000L;
         final List<AiOracle.BlockHit> found = new ArrayList<>();
         List<int[]> chunkOrder;
         int nextChunk;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Village scan deadline reached", this.statusData());
               }

               if (this.chunkOrder == null) {
                  this.chunkOrder = AiOracle.chunksAround(mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4, radiusChunks);
               }

               Vec3 eye = mc.player.getEyePosition();

               for (int budget = 8; budget > 0 && this.nextChunk < this.chunkOrder.size(); budget--) {
                  int[] c = this.chunkOrder.get(this.nextChunk++);
                  LevelChunk chunk = mc.level.getChunkSource().getChunk(c[0], c[1], ChunkStatus.FULL, false);
                  if (chunk != null) {
                     int centerX = chunk.getPos().getMinBlockX() + 8;
                     int centerZ = chunk.getPos().getMinBlockZ() + 8;
                     if (AiOracle.chunkHasTargets(chunk, mask, centerX, centerZ)) {
                        double dx = centerX + 0.5 - eye.x;
                        double dz = centerZ + 0.5 - eye.z;
                        this.found.add(new AiOracle.BlockHit(centerX, 0, centerZ, dx * dx + dz * dz));
                     }
                  }
               }

               if (this.nextChunk < this.chunkOrder.size()) {
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Scanning loaded chunks for villages (" + this.nextChunk + "/" + this.chunkOrder.size() + ")", this.statusData()
                  );
               }

               List<AiOracle.BlockHit> ordered = AiOracle.sortByDistance(this.found);
               JsonArray centers = new JsonArray();

               for (int i = 0; i < ordered.size() && i < cap; i++) {
                  AiOracle.BlockHit f = ordered.get(i);
                  centers.add(AiJson.objectOf("x", f.x(), "z", f.z(), "distance", AiOracle.round1(Math.sqrt(f.distanceSq())), "observed", true));
               }

               JsonObject nearest = ordered.isEmpty()
                  ? null
                  : AiJson.objectOf(
                     "x", ordered.get(0).x(), "z", ordered.get(0).z(), "distance", AiOracle.round1(Math.sqrt(ordered.get(0).distanceSq())), "observed", true
                  );
               return this.finish(
                  AiOperation.Status.SUCCEEDED,
                  this.found.isEmpty()
                     ? "No village-palette chunks in loaded terrain; game:find_village verifies village evidence clusters"
                     : this.found.size()
                        + " village-palette chunk(s) in loaded terrain; nearest at "
                        + ordered.get(0).x()
                        + ", "
                        + ordered.get(0).z()
                        + " ("
                        + AiOracle.round1(Math.sqrt(ordered.get(0).distanceSq()))
                        + " blocks); game:find_village verifies village evidence clusters",
                  AiJson.objectOf(
                     "query",
                     "village",
                     "observed",
                     true,
                     "predicted",
                     false,
                     "note",
                     "palette heuristic on loaded chunks; game:find_village verifies village evidence clusters",
                     "matches",
                     this.found.size(),
                     "scannedChunks",
                     this.nextChunk,
                     "nearest",
                     nearest,
                     "chunks",
                     centers
                  )
               );
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Oracle scan cancelled", new JsonObject());
            }
         }

         JsonObject statusData() {
            return AiJson.objectOf(
               "query", "village", "scannedChunks", this.nextChunk, "totalChunks", this.chunkOrder == null ? 0 : this.chunkOrder.size(), "matches", this.found.size()
            );
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
            this.cancelled = true;
         }
      };
   }

   private static boolean chunkHasTargets(LevelChunk chunk, long mask, int centerX, int centerZ) {
      Map<Integer, Long> sectionCache = new HashMap<>();
      LevelChunkSection[] sections = chunk.getSections();

      for (int i = 0; i < sections.length; i++) {
         LevelChunkSection section = sections[i];
         if (section != null
            && !section.hasOnlyAir()
            && autismclient.modules.StructureStorageFilter.isInHiddenStructure(chunk, new BlockPos(centerX, chunk.getSectionYFromSectionIndex(i) << 4, centerZ), mask, sectionCache)) {
            return true;
         }
      }

      return false;
   }

   private static AiOperation scanThreats(final int cap) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         boolean done;

         @Override
         public AiOperation.Result tick() {
            if (!this.done && mc.player != null && mc.level != null) {
               List<Entity> observed = new ArrayList<>();
               mc.level.entitiesForRendering().forEach(observed::add);
               List<LivingEntity> close = AiSurvival.livingWithin(observed, mc.player.position(), 24.0);
               close.sort(Comparator.comparingDouble(ex -> ex.distanceToSqr(mc.player)));
               JsonArray threats = new JsonArray();

               for (int i = 0; i < close.size() && i < cap; i++) {
                  LivingEntity e = close.get(i);
                  threats.add(
                     AiJson.objectOf(
                        "id",
                        e.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(),
                        "x",
                        AiOracle.round1(e.getX()),
                        "y",
                        AiOracle.round1(e.getY()),
                        "z",
                        AiOracle.round1(e.getZ()),
                        "distance",
                        AiOracle.round1(Math.sqrt(e.distanceToSqr(mc.player))),
                        "observed",
                        true
                     )
                  );
               }

               return this.finish(
                  AiOperation.Status.SUCCEEDED,
                  threats.size() + " observed hostile mob(s) within 24 blocks",
                  AiJson.objectOf(
                     "query", "threats", "observed", true, "predicted", false, "radius", 24.0, "matches", close.size(), "threats", threats
                  )
               );
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
            }
         }

         AiOperation.Result finish(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return AiGameOperations.result(status, message, data);
         }

         @Override
         public void cancel() {
            this.done = true;
         }
      };
   }

   private static double round1(double value) {
      return Math.round(value * 10.0) / 10.0;
   }

   private record StructureSearchOutcome(List<AiOracle.StructureHit> hits, String error) {
   }

   record BlockHit(int x, int y, int z, double distanceSq) {
   }

   record OracleQuery(AiOracle.QuerySubject subject, autismclient.util.oresim.OreBlockCatalog.OreKind ore, String blockId, AiOracle.StructureSpec structure) {
   }

   private record BiomeHit(int x, int y, int z, String biomeId, double distanceSq) {
   }

   private record StructureHit(int x, int y, int z, String structureId, double distanceSq) {
   }

   record ResolvedSeed(long value, String source) {
   }

   record StructureSpec(String label, List<String> ids, Set<String> dimensions) {
   }

   private record BiomeSearchOutcome(AiOracle.BiomeHit hit, String error) {
   }

   enum QuerySubject {
      LAVA,
      WATER,
      VILLAGE,
      THREATS,
      ORE,
      BLOCK,
      STRUCTURE;
   }

   interface SeedProbe {
      Long integratedSeed();

      String storedSeed();
   }
}
