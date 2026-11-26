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

final class aG {
   static final int init_I = 8;
   static final int load_I = 32;
   static final int read = 2;
   static final int write = 16;
   private static final int update = 8;
   private static final int sync = 4096;
   private static final long init_J = 60000000000L;
   private static final int flush = 400;
   private static final double init_D = 24.0;
   private static final int close = 25;
   private static final int open = 3;
   private static final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "ai-oracle-structures");
      thread.setDaemon(true);
      return thread;
   });
   private static final Object init_Obj = new Object();
   private static final LinkedHashMap<String, autismclient.util.j.a.ConfigStore> init_LinkedHashMap = new LinkedHashMap<String, autismclient.util.j.a.ConfigStore>(
      4, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<String, autismclient.util.j.a.ConfigStore> eldest) {
         return this.size() > 3;
      }
   };
   private static long load_J = -1L;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   static final Map<String, aG.Inner_StateCache> init_Map = init_Map();
   private static final int poll = 64;

   private aG() {
   }

   static void init_V_Z(boolean on) {
      if (on) {
         init_AtomicInteger.incrementAndGet();
      } else {
         init_AtomicInteger.updateAndGet(n -> Math.max(0, n - 1));
      }
   }

   static boolean init_Z() {
      return init_AtomicInteger.get() > 0;
   }

   static AiOperation init_AiOperation_JsonObject(JsonObject args) {
      aG.Inner_ConfigStore q = init_aG_ConfigStore_Str(ag.init_Str_JsonObject(args, "query", ""));
      int cap = init_I_I(ag.init_I_JsonObject(args, "cap", 8));
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (!init_Z() || q.subject() != aG.Inner_SyncManager.update && q.subject() != aG.Inner_SyncManager.flush) {
            AiOperation var10000;
            switch (q.subject()) {
               case init:
                  var10000 = init("lava", state -> state.getBlock() == Blocks.LAVA && state.getFluidState().isSource(), cap, init_I_JsonObject(args));
                  break;
               case load:
                  var10000 = init("water", state -> state.getBlock() == Blocks.WATER, cap, init_I_JsonObject(args));
                  break;
               case read:
                  var10000 = init_AiOperation_I_I(cap, init_I_JsonObject(args));
                  break;
               case write:
                  var10000 = init_AiOperation_I(cap);
                  break;
               case update:
                  var10000 = init(q.ore(), cap, ag.init_Str_JsonObject(args, "seed", ""));
                  break;
               case sync:
                  var10000 = init(q.blockId(), init_Predicate_Str(q.blockId()), cap, init_I_JsonObject(args));
                  break;
               case flush:
                  var10000 = init(q.structure(), cap, ag.init_Str_JsonObject(args, "seed", ""));
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

   static aG.Inner_ConfigStore init_aG_ConfigStore_Str(String raw) {
      String q = raw == null ? "" : raw.strip().toLowerCase(Locale.ROOT);
      if (q.isEmpty()) {
         throw new IllegalArgumentException(
            "query is required: lava, water, village, threats, an ore kind (coal, copper, diamond, debris, emerald, gold, iron, lapis, quartz, redstone or ancient_debris), a structure id (stronghold, fortress, ... or structure:<id>), or a registry block id (minecraft:xxx)"
         );
      }

      switch (q) {
         case "lava":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.init, null, null, null);
         case "water":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.load, null, null, null);
         case "village":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.read, null, null, null);
         case "threats":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.write, null, null, null);
         case "structure":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.flush, null, null, init_aG_StateCache_Str("stronghold"));
         case "ancient_debris":
            return new aG.Inner_ConfigStore(aG.Inner_SyncManager.update, autismclient.util.oresim.CachePool.Inner_DataUtil.DEBRIS, null, null);
         default:
            if (q.startsWith("structure:")) {
               return load(q.substring("structure:".length()));
            } else {
               for (autismclient.util.oresim.CachePool.Inner_DataUtil kind : autismclient.util.oresim.CachePool.Inner_DataUtil.values()) {
                  if (kind.init_Str.equals(q)) {
                     return new aG.Inner_ConfigStore(aG.Inner_SyncManager.update, kind, null, null);
                  }
               }

               aG.Inner_StateCache direct = init_aG_StateCache_Str(q);
               if (direct != null) {
                  return new aG.Inner_ConfigStore(aG.Inner_SyncManager.flush, null, null, direct);
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
                     return new aG.Inner_ConfigStore(aG.Inner_SyncManager.sync, null, id.toString(), null);
                  }
               }
            }
      }
   }

   private static aG.Inner_ConfigStore load(String name) {
      aG.Inner_StateCache spec = init_aG_StateCache_Str(name);
      if (spec == null) {
         throw new IllegalArgumentException("Unknown structure \"" + name + "\"; known structures: " + String.join(", ", init_Map.keySet()));
      } else {
         return new aG.Inner_ConfigStore(aG.Inner_SyncManager.flush, null, null, spec);
      }
   }

   static aG.Inner_StateCache init_aG_StateCache_Str(String name) {
      if (name == null) {
         return null;
      }

      String key = name.strip().toLowerCase(Locale.ROOT);
      int colon = key.indexOf(58);
      if (colon >= 0) {
         key = key.substring(colon + 1);
      }

      return init_Map.get(key);
   }

   private static Map<String, aG.Inner_StateCache> init_Map() {
      Set<String> ow = Set.of("overworld");
      Set<String> ne = Set.of("nether");
      Set<String> en = Set.of("end");
      Set<String> owNe = Set.of("overworld", "nether");
      Map<String, aG.Inner_StateCache> m = new LinkedHashMap<>();
      m.put("stronghold", new aG.Inner_StateCache("stronghold", List.of("minecraft:stronghold"), ow));
      m.put("fortress", new aG.Inner_StateCache("fortress", List.of("minecraft:fortress"), ne));
      m.put("bastion_remnant", new aG.Inner_StateCache("bastion_remnant", List.of("minecraft:bastion_remnant"), ne));
      m.put("bastion", m.get("bastion_remnant"));
      m.put("nether_fossil", new aG.Inner_StateCache("nether_fossil", List.of("minecraft:nether_fossil"), ne));
      m.put("end_city", new aG.Inner_StateCache("end_city", List.of("minecraft:end_city"), en));
      m.put("endcity", m.get("end_city"));
      m.put(
         "village",
         new aG.Inner_StateCache(
            "village",
            List.of("minecraft:village_plains", "minecraft:village_desert", "minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga"),
            ow
         )
      );
      m.put("village_plains", new aG.Inner_StateCache("village_plains", List.of("minecraft:village_plains"), ow));
      m.put("village_desert", new aG.Inner_StateCache("village_desert", List.of("minecraft:village_desert"), ow));
      m.put("village_savanna", new aG.Inner_StateCache("village_savanna", List.of("minecraft:village_savanna"), ow));
      m.put("village_snowy", new aG.Inner_StateCache("village_snowy", List.of("minecraft:village_snowy"), ow));
      m.put("village_taiga", new aG.Inner_StateCache("village_taiga", List.of("minecraft:village_taiga"), ow));
      m.put("mansion", new aG.Inner_StateCache("mansion", List.of("minecraft:mansion"), ow));
      m.put("monument", new aG.Inner_StateCache("monument", List.of("minecraft:monument"), ow));
      m.put("pillager_outpost", new aG.Inner_StateCache("pillager_outpost", List.of("minecraft:pillager_outpost"), ow));
      m.put("outpost", m.get("pillager_outpost"));
      m.put(
         "ruined_portal",
         new aG.Inner_StateCache(
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
      m.put("ruined_portal_desert", new aG.Inner_StateCache("ruined_portal_desert", List.of("minecraft:ruined_portal_desert"), ow));
      m.put("ruined_portal_jungle", new aG.Inner_StateCache("ruined_portal_jungle", List.of("minecraft:ruined_portal_jungle"), ow));
      m.put("ruined_portal_mountain", new aG.Inner_StateCache("ruined_portal_mountain", List.of("minecraft:ruined_portal_mountain"), ow));
      m.put("ruined_portal_ocean", new aG.Inner_StateCache("ruined_portal_ocean", List.of("minecraft:ruined_portal_ocean"), ow));
      m.put("ruined_portal_swamp", new aG.Inner_StateCache("ruined_portal_swamp", List.of("minecraft:ruined_portal_swamp"), ow));
      m.put("ruined_portal_nether", new aG.Inner_StateCache("ruined_portal_nether", List.of("minecraft:ruined_portal_nether"), ne));
      m.put("mineshaft", new aG.Inner_StateCache("mineshaft", List.of("minecraft:mineshaft", "minecraft:mineshaft_mesa"), ow));
      m.put("mineshaft_mesa", new aG.Inner_StateCache("mineshaft_mesa", List.of("minecraft:mineshaft_mesa"), ow));
      m.put("ancient_city", new aG.Inner_StateCache("ancient_city", List.of("minecraft:ancient_city"), ow));
      m.put("buried_treasure", new aG.Inner_StateCache("buried_treasure", List.of("minecraft:buried_treasure"), ow));
      m.put("desert_pyramid", new aG.Inner_StateCache("desert_pyramid", List.of("minecraft:desert_pyramid"), ow));
      m.put("jungle_pyramid", new aG.Inner_StateCache("jungle_pyramid", List.of("minecraft:jungle_pyramid"), ow));
      m.put("igloo", new aG.Inner_StateCache("igloo", List.of("minecraft:igloo"), ow));
      m.put("ocean_ruin", new aG.Inner_StateCache("ocean_ruin", List.of("minecraft:ocean_ruin_cold", "minecraft:ocean_ruin_warm"), ow));
      m.put("ocean_ruin_cold", new aG.Inner_StateCache("ocean_ruin_cold", List.of("minecraft:ocean_ruin_cold"), ow));
      m.put("ocean_ruin_warm", new aG.Inner_StateCache("ocean_ruin_warm", List.of("minecraft:ocean_ruin_warm"), ow));
      m.put("shipwreck", new aG.Inner_StateCache("shipwreck", List.of("minecraft:shipwreck", "minecraft:shipwreck_beached"), ow));
      m.put("shipwreck_beached", new aG.Inner_StateCache("shipwreck_beached", List.of("minecraft:shipwreck_beached"), ow));
      m.put("swamp_hut", new aG.Inner_StateCache("swamp_hut", List.of("minecraft:swamp_hut"), ow));
      m.put("trail_ruins", new aG.Inner_StateCache("trail_ruins", List.of("minecraft:trail_ruins"), ow));
      m.put("trial_chambers", new aG.Inner_StateCache("trial_chambers", List.of("minecraft:trial_chambers"), ow));
      return Collections.unmodifiableMap(m);
   }

   static int init_I_CachePool_DataUtil(autismclient.util.oresim.CachePool.Inner_DataUtil kind) {
      return 1 << kind.ordinal();
   }

   static int init_I_I(int cap) {
      return Math.max(1, Math.min(32, cap));
   }

   static int load(int radiusChunks) {
      return Math.max(2, Math.min(16, radiusChunks));
   }

   static List<aG.Inner_CachePool> init_List_List(List<aG.Inner_CachePool> found) {
      List<aG.Inner_CachePool> ordered = new ArrayList<>(found);
      ordered.sort(Comparator.comparingDouble(aG.Inner_CachePool::distanceSq));
      return ordered;
   }

   static List<int[]> init(int centerX, int centerZ, int radiusChunks) {
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

   static aG.Inner_NetHelper init_aG_NetHelper_Str_aG_TaskQueue(String seedArg, aG.Inner_TaskQueue probe) {
      String raw = seedArg == null ? "" : seedArg.strip();
      if (!raw.isEmpty()) {
         autismclient.util.oresim.BufferHelper.Inner_DataUtil parsed = autismclient.util.oresim.BufferHelper.init(raw);
         if (!parsed.init_Z()) {
            throw new IllegalArgumentException("seed must be a signed decimal long, got \"" + raw + "\"");
         } else {
            return new aG.Inner_NetHelper(parsed.init_Long(), "argument");
         }
      } else {
         Long local = probe.init_Long();
         if (local != null) {
            return new aG.Inner_NetHelper(local, "singleplayer");
         } else {
            autismclient.util.oresim.BufferHelper.Inner_DataUtil stored = autismclient.util.oresim.BufferHelper.init(probe.init_Str());
            if (stored.init_Z()) {
               return new aG.Inner_NetHelper(stored.init_Long(), "stored");
            } else if (stored.init_BufferHelper_Strea() == autismclient.util.oresim.BufferHelper.Inner_StreamUtil.INVALID) {
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

   private static aG.Inner_TaskQueue init_aG_TaskQueue_Minecraft(final Minecraft mc) {
      return new aG.Inner_TaskQueue() {
         @Override
         public Long init_Long() {
            IntegratedServer server = mc.getSingleplayerServer();
            return server == null ? null : server.getWorldGenSettings().options().seed();
         }

         @Override
         public String init_Str() {
            return AutismOreSimSeedStore.init_AutismOreSimSeedSt().init_Str(AutismWaypoints.init_Str_Minecraft(mc), "");
         }
      };
   }

   private static int init_I_JsonObject(JsonObject args) {
      return load(ag.init_I_JsonObject(args, "radiusChunks", autismclient.modules.am.init_I()));
   }

   private static Predicate<BlockState> init_Predicate_Str(String blockId) {
      Block block = BuiltInRegistries.BLOCK
         .getOptional(Identifier.parse(blockId))
         .orElseThrow(() -> new IllegalArgumentException("Unknown block \"" + blockId + "\""));
      return state -> state.getBlock() == block;
   }

   private static long init_J() {
      if (load_J < 0L) {
         for (int i = 0; i < autismclient.modules.aq.init.size(); i++) {
            if ("hide-villages".equals(autismclient.modules.aq.init.get(i).init_Str())) {
               load_J = 1L << i;
               break;
            }
         }

         if (load_J < 0L) {
            throw new IllegalStateException("ModuleStorageStructures.ALL has no village entry");
         }
      }

      return load_J;
   }

   private static AiOperation init(final autismclient.util.oresim.CachePool.Inner_DataUtil kind, final int cap, String seedArg) {
      final Minecraft mc = Minecraft.getInstance();
      final aG.Inner_NetHelper seed = init_aG_NetHelper_Str_aG_TaskQueue(seedArg, init_aG_TaskQueue_Minecraft(mc));
      final int mask = init_I_CachePool_DataUtil(kind);
      return new AiOperation() {
         final long init_J = System.nanoTime() + 60000000000L;
         int init_I;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               autismclient.util.oresim.StreamUtil.init(mc.level, mc.player.blockPosition(), seed.value(), 8, mask);
               if (autismclient.util.oresim.StreamUtil.read_Z()) {
                  return this.init(
                     AiOperation.Status.FAILED,
                     "Ore simulation failed (status "
                        + autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH()
                        + "): "
                        + autismclient.util.oresim.StreamUtil.init_Str(),
                     new JsonObject()
                  );
               }

               if (autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH() == autismclient.util.oresim.StreamUtil.Inner_BufferHelper.UNSUPPORTED_DIMENSION) {
                  return this.init(
                     AiOperation.Status.FAILED,
                     "Ore simulation supports only the Overworld, Nether and End; the current dimension "
                        + mc.level.dimension().identifier()
                        + " is unsupported",
                     new JsonObject()
                  );
               }

               if (System.nanoTime() > this.init_J) {
                  return this.init(
                     AiOperation.Status.TIMED_OUT,
                     "Ore simulation did not finish within 60s (status " + autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH() + ")",
                     this.init()
                  );
               }

               if (!autismclient.util.oresim.StreamUtil.init_Z()) {
                  return this.init("Loading vanilla worldgen context for seed simulation");
               }

               autismclient.util.oresim.StreamUtil.Inner_ConfigStore progress = autismclient.util.oresim.StreamUtil.init_StreamUtil_ConfigS();
               if (!progress.init_Z() && ++this.init_I < 400) {
                  return this.init("Simulating ore generation (" + progress.init_I() + "/" + progress.load() + " chunks)");
               }

               Vec3 eye = mc.player.getEyePosition();
               long[] outPos = new long[cap];
               int[] outState = new int[cap];
               int n = autismclient.util.oresim.StreamUtil.init(
                  eye.x,
                  eye.y,
                  eye.z,
                  8,
                  stateId -> autismclient.util.oresim.CachePool.Inner_StreamUtil.init_CachePool_DataUtil(stateId) == kind,
                  cap,
                  outPos,
                  outState
               );
               JsonArray positions = new JsonArray();

               for (int i = 0; i < n; i++) {
                  BlockPos p = BlockPos.of(outPos[i]);
                  positions.add(
                     ag.init_JsonObject_ObjArr(
                        "x",
                        p.getX(),
                        "y",
                        p.getY(),
                        "z",
                        p.getZ(),
                        "distance",
                        aG.init_D_D(Math.sqrt(eye.distanceToSqr(Vec3.atCenterOf(p)))),
                        "kind",
                        kind.init_Str,
                        "predicted",
                        true
                     )
                  );
               }

               return this.init(
                  AiOperation.Status.SUCCEEDED,
                  n
                     + " predicted "
                     + kind.init_Str
                     + " position(s), nearest-first; predicted from world seed simulation (seed from "
                     + seed.source()
                     + "); confirm by observation",
                  ag.init_JsonObject_ObjArr(
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
                     progress.init_I(),
                     "totalChunks",
                     progress.load(),
                     "positions",
                     positions
                  )
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
            }
         }

         AiOperation.Result init(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init());
         }

         JsonObject init() {
            autismclient.util.oresim.StreamUtil.Inner_ConfigStore p = autismclient.util.oresim.StreamUtil.init_StreamUtil_ConfigS();
            return ag.init_JsonObject_ObjArr(
               "engineStatus", autismclient.util.oresim.StreamUtil.init_StreamUtil_BufferH().name(), "simulatedChunks", p.init_I(), "totalChunks", p.load()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.init_Z = true;
               autismclient.util.oresim.StreamUtil.load_V();
            }
         }
      };
   }

   private static AiOperation init(final aG.Inner_StateCache spec, final int cap, String seedArg) {
      final Minecraft mc = Minecraft.getInstance();
      final aG.Inner_NetHelper seed = init_aG_NetHelper_Str_aG_TaskQueue(seedArg, init_aG_TaskQueue_Minecraft(mc));
      final ResourceKey<Level> dimension = mc.level.dimension();
      final String dim = init_Str_ResourceKey(dimension);
      if (dim == null) {
         throw new IllegalArgumentException("Structure locating supports only the Overworld, Nether and End");
      } else {
         return !spec.dimensions().contains(dim)
            ? R.init_AiOperation(
               AiOperation.Status.FAILED,
               spec.label() + " does not generate in the " + dim + " (generates in: " + String.join(", ", spec.dimensions()) + ")",
               ag.init_JsonObject_ObjArr("query", spec.label(), "dimension", dim, "predicted", true, "observed", false)
            )
            : new AiOperation() {
               final long init_J = System.nanoTime() + 60000000000L;
               final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
               volatile CompletableFuture<aG.Inner_BufferHelper> init_CompletableFuture;

               @Override
               public AiOperation.Result tick() {
                  if (!this.init_AtomicBoolean.get() && mc.player != null && mc.level != null) {
                     if (this.init_CompletableFuture == null) {
                        BlockPos origin = mc.player.blockPosition();
                        this.init_CompletableFuture = CompletableFuture.supplyAsync(
                           () -> aG.init_aG_BufferHelper_J(seed.value(), dimension, spec, origin, cap, this.init_AtomicBoolean), aG.init_ExecutorService
                        );
                     }

                     if (!this.init_CompletableFuture.isDone()) {
                        if (System.nanoTime() > this.init_J) {
                           this.cancel();
                           return R.init_AiOperation_Result(AiOperation.Status.TIMED_OUT, "Structure location did not finish within 60s", this.init());
                        } else {
                           return R.init_AiOperation_Result(
                              AiOperation.Status.RUNNING, "Locating " + spec.label() + " from world seed simulation (off-thread vanilla worldgen)", this.init()
                           );
                        }
                     } else {
                        aG.Inner_BufferHelper outcome;
                        try {
                           outcome = this.init_CompletableFuture.get();
                        } catch (Exception error) {
                           return this.init(AiOperation.Status.FAILED, "Structure location failed: " + error, new JsonObject());
                        }

                        if (this.init_AtomicBoolean.get()) {
                           return this.init(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
                        }

                        if (outcome.error() != null) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              outcome.error(),
                              ag.init_JsonObject_ObjArr("query", spec.label(), "dimension", dim, "predicted", true, "observed", false)
                           );
                        }

                        JsonArray positions = new JsonArray();

                        for (aG.Inner_JsonUtil hit : outcome.hits()) {
                           positions.add(
                              ag.init_JsonObject_ObjArr(
                                 "x",
                                 hit.x(),
                                 "y",
                                 hit.y(),
                                 "z",
                                 hit.z(),
                                 "distance",
                                 aG.init_D_D(Math.sqrt(hit.distanceSq())),
                                 "structure",
                                 hit.structureId(),
                                 "predicted",
                                 true
                              )
                           );
                        }

                        return this.init(
                           AiOperation.Status.SUCCEEDED,
                           positions.size()
                              + " predicted "
                              + spec.label()
                              + " position(s), nearest-first; predicted from world seed simulation (seed from "
                              + seed.source()
                              + "); confirm by observation",
                           ag.init_JsonObject_ObjArr(
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
                     return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
                  }
               }

               JsonObject init() {
                  return ag.init_JsonObject_ObjArr("query", spec.label(), "dimension", dim, "worker", "ai-oracle-structures");
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
                  this.cancel();
                  return R.init_AiOperation_Result(status, message, data);
               }

               @Override
               public void cancel() {
                  this.init_AtomicBoolean.set(true);
                  CompletableFuture<aG.Inner_BufferHelper> running = this.init_CompletableFuture;
                  if (running != null) {
                     running.cancel(true);
                  }
               }
            };
      }
   }

   private static aG.Inner_BufferHelper init_aG_BufferHelper_J(
      long seed, ResourceKey<Level> dimension, aG.Inner_StateCache spec, BlockPos origin, int cap, AtomicBoolean cancelled
   ) {
      try {
         autismclient.util.j.a.ConfigStore context = init_ConfigStore_J_ResourceKey(seed, dimension);
         if (cancelled.get()) {
            return new aG.Inner_BufferHelper(List.of(), null);
         }

         RegistryLookup<Structure> registry = context.init_HolderLookup_Provi().lookupOrThrow(Registries.STRUCTURE);
         List<Holder<Structure>> wanted = new ArrayList<>();

         for (String id : spec.ids()) {
            registry.get(ResourceKey.create(Registries.STRUCTURE, Identifier.parse(id))).ifPresent(wanted::add);
         }

         if (wanted.isEmpty()) {
            return new aG.Inner_BufferHelper(List.of(), "None of " + spec.ids() + " exists in the vanilla 26.2 structure registry");
         } else {
            List<aG.Inner_JsonUtil> hits = init(context, wanted, origin, cap, cancelled);
            if (cancelled.get()) {
               return new aG.Inner_BufferHelper(List.of(), null);
            } else {
               return hits.isEmpty()
                  ? new aG.Inner_BufferHelper(
                     List.of(),
                     "No " + spec.label() + " within 25 structure sectors of " + origin.toShortString() + "; seed simulation cannot reliably search further"
                  )
                  : new aG.Inner_BufferHelper(hits, null);
            }
         }
      } catch (Throwable error) {
         return new aG.Inner_BufferHelper(
            List.of(), "Structure location failed: " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage())
         );
      }
   }

   private static autismclient.util.j.a.ConfigStore init_ConfigStore_J_ResourceKey(long seed, ResourceKey<Level> dimension) {
      String key = seed + "@" + dimension.identifier();
      synchronized (init_Obj) {
         autismclient.util.j.a.ConfigStore existing = init_LinkedHashMap.get(key);
         if (existing != null) {
            return existing;
         }

         autismclient.util.j.a.ConfigStore created = autismclient.util.j.a.ConfigStore.init(seed, dimension);
         init_LinkedHashMap.put(key, created);
         return created;
      }
   }

   private static List<aG.Inner_JsonUtil> init(
      autismclient.util.j.a.ConfigStore context, List<Holder<Structure>> wanted, BlockPos origin, int cap, AtomicBoolean cancelled
   ) {
      ChunkGeneratorStructureState state = context.init_ChunkGeneratorStru();
      long seed = state.getLevelSeed();
      Map<StructurePlacement, Set<Holder<Structure>>> scans = new LinkedHashMap<>();

      for (Holder<Structure> holder : wanted) {
         for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
            scans.computeIfAbsent(placement, p -> new LinkedHashSet<>()).add(holder);
         }
      }

      List<aG.Inner_JsonUtil> hits = new ArrayList<>();

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

                  init_V_ConfigStore(context, entry.getValue(), candidate, origin, seed, hits);
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
                           init_V_ConfigStore(context, entry.getValue(), candidate, origin, seed, hits);
                        }
                     }
                  }
               }
            }
         }
      }

      hits.sort(Comparator.comparingDouble(aG.Inner_JsonUtil::distanceSq));
      return hits.size() > cap ? new ArrayList<>(hits.subList(0, cap)) : hits;
   }

   private static void init_V_ConfigStore(
      autismclient.util.j.a.ConfigStore context,
      Set<Holder<Structure>> structures,
      ChunkPos candidate,
      BlockPos origin,
      long seed,
      List<aG.Inner_JsonUtil> hits
   ) {
      for (Holder<Structure> holder : structures) {
         Structure structure = holder.value();
         GenerationContext generation = new GenerationContext(
            context.init_RegistryAccess(),
            context.init_ChunkGenerator(),
            context.init_BiomeSource(),
            context.init_RandomState().createClimateSampler(null),
            context.init_RandomState(),
            context.init_StructureTemplateM(),
            seed,
            candidate,
            context.init_LevelHeightAccesso(),
            structure.biomes()::contains
         );
         Optional<GenerationStub> stub = structure.findValidGenerationPoint(generation);
         if (!stub.isEmpty()) {
            BlockPos pos = stub.get().position();
            hits.add(new aG.Inner_JsonUtil(pos.getX(), pos.getY(), pos.getZ(), init_Str_Holder(holder), origin.distSqr(pos)));
            return;
         }
      }
   }

   private static String init_Str_Holder(Holder<Structure> holder) {
      return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
   }

   private static String init_Str_ResourceKey(ResourceKey<Level> dimension) {
      if (Level.OVERWORLD.equals(dimension)) {
         return "overworld";
      } else if (Level.NETHER.equals(dimension)) {
         return "nether";
      } else {
         return Level.END.equals(dimension) ? "end" : null;
      }
   }

   static AiOperation load(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         throw new IllegalArgumentException("Biome location requires a connected living player");
      }

      if (init_Z()) {
         throw new IllegalArgumentException("No seed knowledge in a seed-free run: find the biome by exploration");
      }

      final String biomeId = init_Str_Str_Collection(ag.init_Str_JsonObject(args, "biome", ""), init_List_Minecraft(mc));
      final int radiusBlocks = Math.max(64, Math.min(16384, ag.init_I_JsonObject(args, "radiusBlocks", 4096)));
      final aG.Inner_NetHelper seed = init_aG_NetHelper_Str_aG_TaskQueue(ag.init_Str_JsonObject(args, "seed", ""), init_aG_TaskQueue_Minecraft(mc));
      ResourceKey<Level> dimension = args.has("dimension") ? init_ResourceKey_Str(ag.init_Str_JsonObject(args, "dimension", "")) : mc.level.dimension();
      if (dimension == null) {
         throw new IllegalArgumentException("dimension must be overworld, nether or end");
      }

      final ResourceKey<Level> targetDimension = dimension;
      final int timeoutSeconds = Math.max(5, Math.min(300, ag.init_I_JsonObject(args, "timeoutSeconds", 60)));
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
         volatile CompletableFuture<aG.Inner_StreamUtil> init_CompletableFuture;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_AtomicBoolean.get() && mc.player != null && mc.level != null) {
               if (this.init_CompletableFuture == null) {
                  BlockPos origin = mc.player.blockPosition();
                  this.init_CompletableFuture = CompletableFuture.supplyAsync(
                     () -> aG.init_aG_StreamUtil_J(seed.value(), targetDimension, biomeId, origin, radiusBlocks, this.init_AtomicBoolean),
                     aG.init_ExecutorService
                  );
               }

               if (!this.init_CompletableFuture.isDone()) {
                  if (System.nanoTime() > this.init_J) {
                     this.cancel();
                     return R.init_AiOperation_Result(AiOperation.Status.TIMED_OUT, "Biome location did not finish within " + timeoutSeconds + "s", this.init());
                  } else {
                     return R.init_AiOperation_Result(
                        AiOperation.Status.RUNNING,
                        "Sampling vanilla biome generation for " + biomeId + " (off-thread, seed from " + seed.source() + ")",
                        this.init()
                     );
                  }
               } else {
                  aG.Inner_StreamUtil outcome;
                  try {
                     outcome = this.init_CompletableFuture.get();
                  } catch (Exception error) {
                     return this.init(AiOperation.Status.FAILED, "Biome location failed: " + error, new JsonObject());
                  }

                  if (this.init_AtomicBoolean.get()) {
                     return this.init(AiOperation.Status.CANCELLED, "Biome location cancelled", new JsonObject());
                  } else if (outcome.error() != null) {
                     this.cancel();
                     return AiOperation.Result.failed(
                        A.close, outcome.error(), ag.init_JsonObject_ObjArr("biome", biomeId, "predicted", true, "observed", false).toString()
                     );
                  } else {
                     aG.Inner_DataUtil hit = outcome.hit();
                     return this.init(
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
                           + aG.init_D_D(Math.sqrt(hit.distanceSq()))
                           + " blocks); predicted from world seed simulation (seed from "
                           + seed.source()
                           + "); confirm by observation",
                        ag.init_JsonObject_ObjArr(
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
                           ag.init_JsonObject_ObjArr("x", hit.x(), "y", hit.y(), "z", hit.z()),
                           "distance",
                           aG.init_D_D(Math.sqrt(hit.distanceSq()))
                        )
                     );
                  }
               }
            } else {
               this.cancel();
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Biome location cancelled", new JsonObject());
            }
         }

         JsonObject init() {
            return ag.init_JsonObject_ObjArr("biome", biomeId, "worker", "ai-oracle-structures");
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
            this.init_AtomicBoolean.set(true);
            CompletableFuture<aG.Inner_StreamUtil> running = this.init_CompletableFuture;
            if (running != null) {
               running.cancel(true);
            }
         }
      };
   }

   private static aG.Inner_StreamUtil init_aG_StreamUtil_J(
      long seed, ResourceKey<Level> dimension, String biomeId, BlockPos origin, int radiusBlocks, AtomicBoolean cancelled
   ) {
      try {
         autismclient.util.j.a.ConfigStore context = init_ConfigStore_J_ResourceKey(seed, dimension);
         if (cancelled.get()) {
            return new aG.Inner_StreamUtil(null, null);
         }

         BiomeSource biomeSource = context.init_BiomeSource();
         int quartY = origin.getY() >> 2;
         int centerCellX = Math.floorDiv(origin.getX(), 64);
         int centerCellZ = Math.floorDiv(origin.getZ(), 64);
         int radiusCells = Math.max(1, radiusBlocks / 64);

         for (int[] cell : load(centerCellX, centerCellZ, radiusCells)) {
            if (cancelled.get()) {
               return new aG.Inner_StreamUtil(null, null);
            }

            int x = cell[0] * 64 + 32;
            int z = cell[1] * 64 + 32;
            Holder<Biome> biome = biomeSource.createCachingResolver(context.init_RandomState()).getNoiseBiome(x >> 2, quartY, z >> 2);
            String sampled = load(biome);
            if (biomeId.equals(sampled)) {
               double dx = x - origin.getX();
               double dz = z - origin.getZ();
               return new aG.Inner_StreamUtil(new aG.Inner_DataUtil(x, origin.getY(), z, sampled, dx * dx + dz * dz), null);
            }
         }

         return new aG.Inner_StreamUtil(
            null, "No " + biomeId + " within " + radiusBlocks + " blocks of " + origin.toShortString() + " (seed-simulated biome sampling at 64-block cells)"
         );
      } catch (Throwable error) {
         return new aG.Inner_StreamUtil(
            null, "Biome location failed: " + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : " - " + error.getMessage())
         );
      }
   }

   static String init_Str_Str_Collection(String spec, Collection<String> biomeIds) {
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

   static List<int[]> load(int centerCellX, int centerCellZ, int radiusCells) {
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

   private static List<String> init_List_Minecraft(Minecraft mc) {
      return mc.level.registryAccess().lookupOrThrow(Registries.BIOME).keySet().stream().map(Identifier::toString).toList();
   }

   private static String load(Holder<Biome> holder) {
      return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("unknown");
   }

   private static ResourceKey<Level> init_ResourceKey_Str(String name) {
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

   private static AiOperation init(final String label, final Predicate<BlockState> target, final int cap, final int radiusChunks) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final long init_J = System.nanoTime() + 60000000000L;
         final List<aG.Inner_CachePool> init_List = new ArrayList<>();
         List<int[]> load;
         int init_I;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Block scan deadline reached", this.init());
               }

               if (this.load == null) {
                  this.load = aG.init(mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4, radiusChunks);
               }

               Vec3 eye = mc.player.getEyePosition();

               for (int budget = 8; budget > 0 && this.init_I < this.load.size() && this.init_List.size() < 4096; budget--) {
                  int[] c = this.load.get(this.init_I++);
                  LevelChunk chunk = mc.level.getChunkSource().getChunk(c[0], c[1], ChunkStatus.FULL, false);
                  if (chunk != null) {
                     aG.init(mc.level, chunk, target, eye, this.init_List);
                  }
               }

               if (this.init_I < this.load.size() && this.init_List.size() < 4096) {
                  return R.init_AiOperation_Result(
                     AiOperation.Status.RUNNING, "Scanning loaded chunks for " + label + " (" + this.init_I + "/" + this.load.size() + ")", this.init()
                  );
               }

               List<aG.Inner_CachePool> ordered = aG.init_List_List(this.init_List);
               JsonArray positions = new JsonArray();

               for (int i = 0; i < ordered.size() && i < cap; i++) {
                  aG.Inner_CachePool f = ordered.get(i);
                  positions.add(
                     ag.init_JsonObject_ObjArr(
                        "x", f.x(), "y", f.y(), "z", f.z(), "distance", aG.init_D_D(Math.sqrt(f.distanceSq())), "block", label, "observed", true
                     )
                  );
               }

               return this.init(
                  AiOperation.Status.SUCCEEDED,
                  positions.size() + " observed " + label + " position(s) in loaded terrain, nearest-first",
                  ag.init_JsonObject_ObjArr(
                     "query",
                     label,
                     "observed",
                     true,
                     "predicted",
                     false,
                     "matches",
                     this.init_List.size(),
                     "scannedChunks",
                     this.init_I,
                     "positions",
                     positions
                  )
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Oracle scan cancelled", new JsonObject());
            }
         }

         JsonObject init() {
            return ag.init_JsonObject_ObjArr(
               "query", label, "scannedChunks", this.init_I, "totalChunks", this.load == null ? 0 : this.load.size(), "matches", this.init_List.size()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
            this.init_Z = true;
         }
      };
   }

   private static void init(ClientLevel level, LevelChunk chunk, Predicate<BlockState> target, Vec3 origin, List<aG.Inner_CachePool> out) {
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
                           out.add(new aG.Inner_CachePool(x, y, z, origin.distanceToSqr(x + 0.5, y + 0.5, z + 0.5)));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static AiOperation init_AiOperation_I_I(final int cap, final int radiusChunks) {
      final Minecraft mc = Minecraft.getInstance();
      final long mask = init_J();
      return new AiOperation() {
         final long init_J = System.nanoTime() + 60000000000L;
         final List<aG.Inner_CachePool> init_List = new ArrayList<>();
         List<int[]> load;
         int init_I;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Village scan deadline reached", this.init());
               }

               if (this.load == null) {
                  this.load = aG.init(mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4, radiusChunks);
               }

               Vec3 eye = mc.player.getEyePosition();

               for (int budget = 8; budget > 0 && this.init_I < this.load.size(); budget--) {
                  int[] c = this.load.get(this.init_I++);
                  LevelChunk chunk = mc.level.getChunkSource().getChunk(c[0], c[1], ChunkStatus.FULL, false);
                  if (chunk != null) {
                     int centerX = chunk.getPos().getMinBlockX() + 8;
                     int centerZ = chunk.getPos().getMinBlockZ() + 8;
                     if (aG.init(chunk, mask, centerX, centerZ)) {
                        double dx = centerX + 0.5 - eye.x;
                        double dz = centerZ + 0.5 - eye.z;
                        this.init_List.add(new aG.Inner_CachePool(centerX, 0, centerZ, dx * dx + dz * dz));
                     }
                  }
               }

               if (this.init_I < this.load.size()) {
                  return R.init_AiOperation_Result(
                     AiOperation.Status.RUNNING, "Scanning loaded chunks for villages (" + this.init_I + "/" + this.load.size() + ")", this.init()
                  );
               }

               List<aG.Inner_CachePool> ordered = aG.init_List_List(this.init_List);
               JsonArray centers = new JsonArray();

               for (int i = 0; i < ordered.size() && i < cap; i++) {
                  aG.Inner_CachePool f = ordered.get(i);
                  centers.add(ag.init_JsonObject_ObjArr("x", f.x(), "z", f.z(), "distance", aG.init_D_D(Math.sqrt(f.distanceSq())), "observed", true));
               }

               JsonObject nearest = ordered.isEmpty()
                  ? null
                  : ag.init_JsonObject_ObjArr(
                     "x", ordered.get(0).x(), "z", ordered.get(0).z(), "distance", aG.init_D_D(Math.sqrt(ordered.get(0).distanceSq())), "observed", true
                  );
               return this.init(
                  AiOperation.Status.SUCCEEDED,
                  this.init_List.isEmpty()
                     ? "No village-palette chunks in loaded terrain; game:find_village verifies village evidence clusters"
                     : this.init_List.size()
                        + " village-palette chunk(s) in loaded terrain; nearest at "
                        + ordered.get(0).x()
                        + ", "
                        + ordered.get(0).z()
                        + " ("
                        + aG.init_D_D(Math.sqrt(ordered.get(0).distanceSq()))
                        + " blocks); game:find_village verifies village evidence clusters",
                  ag.init_JsonObject_ObjArr(
                     "query",
                     "village",
                     "observed",
                     true,
                     "predicted",
                     false,
                     "note",
                     "palette heuristic on loaded chunks; game:find_village verifies village evidence clusters",
                     "matches",
                     this.init_List.size(),
                     "scannedChunks",
                     this.init_I,
                     "nearest",
                     nearest,
                     "chunks",
                     centers
                  )
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Oracle scan cancelled", new JsonObject());
            }
         }

         JsonObject init() {
            return ag.init_JsonObject_ObjArr(
               "query", "village", "scannedChunks", this.init_I, "totalChunks", this.load == null ? 0 : this.load.size(), "matches", this.init_List.size()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
            this.init_Z = true;
         }
      };
   }

   private static boolean init(LevelChunk chunk, long mask, int centerX, int centerZ) {
      Map<Integer, Long> sectionCache = new HashMap<>();
      LevelChunkSection[] sections = chunk.getSections();

      for (int i = 0; i < sections.length; i++) {
         LevelChunkSection section = sections[i];
         if (section != null
            && !section.hasOnlyAir()
            && autismclient.modules.aq.init_Z_LevelChunk(chunk, new BlockPos(centerX, chunk.getSectionYFromSectionIndex(i) << 4, centerZ), mask, sectionCache)) {
            return true;
         }
      }

      return false;
   }

   private static AiOperation init_AiOperation_I(final int cap) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         boolean init;

         @Override
         public AiOperation.Result tick() {
            if (!this.init && mc.player != null && mc.level != null) {
               List<Entity> observed = new ArrayList<>();
               mc.level.entitiesForRendering().forEach(observed::add);
               List<LivingEntity> close = cq.init_List_List(observed, mc.player.position(), 24.0);
               close.sort(Comparator.comparingDouble(ex -> ex.distanceToSqr(mc.player)));
               JsonArray threats = new JsonArray();

               for (int i = 0; i < close.size() && i < cap; i++) {
                  LivingEntity e = close.get(i);
                  threats.add(
                     ag.init_JsonObject_ObjArr(
                        "id",
                        e.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString(),
                        "x",
                        aG.init_D_D(e.getX()),
                        "y",
                        aG.init_D_D(e.getY()),
                        "z",
                        aG.init_D_D(e.getZ()),
                        "distance",
                        aG.init_D_D(Math.sqrt(e.distanceToSqr(mc.player))),
                        "observed",
                        true
                     )
                  );
               }

               return this.init(
                  AiOperation.Status.SUCCEEDED,
                  threats.size() + " observed hostile mob(s) within 24 blocks",
                  ag.init_JsonObject_ObjArr(
                     "query", "threats", "observed", true, "predicted", false, "radius", 24.0, "matches", close.size(), "threats", threats
                  )
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Oracle query cancelled", new JsonObject());
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, data);
         }

         @Override
         public void cancel() {
            this.init = true;
         }
      };
   }

   private static double init_D_D(double value) {
      return Math.round(value * 10.0) / 10.0;
   }

   private record Inner_BufferHelper(List<aG.Inner_JsonUtil> hits, String error) {
   }

   record Inner_CachePool(int x, int y, int z, double distanceSq) {
   }

   record Inner_ConfigStore(aG.Inner_SyncManager subject, autismclient.util.oresim.CachePool.Inner_DataUtil ore, String blockId, aG.Inner_StateCache structure) {
   }

   private record Inner_DataUtil(int x, int y, int z, String biomeId, double distanceSq) {
   }

   private record Inner_JsonUtil(int x, int y, int z, String structureId, double distanceSq) {
   }

   record Inner_NetHelper(long value, String source) {
   }

   record Inner_StateCache(String label, List<String> ids, Set<String> dimensions) {
   }

   private record Inner_StreamUtil(aG.Inner_DataUtil hit, String error) {
   }

   enum Inner_SyncManager {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   interface Inner_TaskQueue {
      Long init_Long();

      String init_Str();
   }
}
