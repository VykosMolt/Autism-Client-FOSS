package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiScaffold;
import autismclient.api.ai.AiCapabilityDescriptor;
import autismclient.api.ai.AiObservation;
import autismclient.api.ai.AiOperation;
import autismclient.api.module.Setting;
import autismclient.compat263.CompatGLFW;
import autismclient.e.PacketUtil;
import autismclient.modules.Module;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.macro.MacroExecutor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiService {
   public static final Set<String> init_Set = Set.of(
      "observe",
      "screenshot",
      "catalog_search",
      "catalog_inspect",
      "memory_search",
      "memory_remember",
      "web_search",
      "web_fetch",
      "task_update",
      "ask_user",
      "finish"
   );
   private static AiService init_cc;
   private final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-agent");
      t.setDaemon(true);
      return t;
   });
   private final ExecutorService load_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-storage");
      t.setDaemon(true);
      return t;
   });
   private final ExecutorService read_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-chat");
      t.setDaemon(true);
      return t;
   });
   private static final String read_Str = "hostChatMode";
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private final AiCatalog init_AiCatalog = new AiCatalog();
   private final AiMemoryStore init_AiMemoryStore = new AiMemoryStore(AutismClientAddon.FOLDER.toPath().resolve("ai"));
   private final AiTools init_cz = new AiTools(this);
   private final AiRegions init_AiRegions = new AiRegions();
   private final AiPerfMetrics init_aM = new AiPerfMetrics();
   private final Map<String, AiService.SessionState> init_Map = new ConcurrentHashMap<>();
   private final Set<String> load_Set = ConcurrentHashMap.newKeySet();
   private volatile String write_Str;
   private volatile AiSessions init_cd;
   private final List<AiService.SettingDelta> init_List = new ArrayList<>();
   private final Map<String, AiObservation> load_Map = new ConcurrentHashMap<>();
   private final Map<String, String> read_Map = new ConcurrentHashMap<>();
   private volatile String update_Str;
   private volatile boolean init_Z;
   private final Map<AiSettings.Provider, AiService.ProviderTestResult> write_Map = new ConcurrentHashMap<>();
   private final AtomicLong load_AtomicLong = new AtomicLong();
   private volatile AiSettings init_AiSettings = new AiSettings();
   private volatile AiService.RunState init_cc_FileCache = AiService.RunState.IDLE;
   private volatile AiChatMode init_FieldCache;
   private volatile AiService.RunState load_cc_FileCache = AiService.RunState.IDLE;
   private volatile JsonObject init_JsonObject = new JsonObject();
   private volatile String sync_Str = "";
   private volatile String flush_Str = UUID.randomUUID().toString();
   private volatile String close_Str = "offline";
   private volatile long write_J;
   private volatile long update_J;
   private volatile JsonObject load_JsonObject = new JsonObject();
   private volatile List<AiService.AttachmentRef> load_List = List.of();
   private volatile String open_Str = "";
   private volatile AiService.ActiveOperation init_cc_SyncManager;
   private volatile String poll_Str = "{}";
   private volatile AiProvider init_bA;
   private volatile AiScriptHost init_bZ;
   private volatile AiObservation init_AiObservation;
   private volatile AiObservation load_AiObservation;
   private volatile boolean load_Z;
   private volatile String push_Str = "";
   private Object init_Obj;
   private volatile String pop_Str = "offline";
   static final long init_J = 20000L;
   private int update_I;
   private long sync_J;
   private volatile boolean read_Z;
   private volatile long flush_J;
   private String tick_Str;
   private long close_J = -1L;
   private long open_J;
   private long poll_J = -1L;
   private long push_J;
   private float init_F = -1.0F;
   private long pop_J;
   private long tick_J;
   private long run_J;
   private long get_J;
   private final Deque<Long> init_Deque = new ArrayDeque<>();
   private final Set<Integer> read_Set = new HashSet<>();
   private Map<String, Integer> update_Map;
   private boolean write_Z = true;
   private volatile String run_Str;
   private int sync_I;
   private boolean update_Z;
   static final int init_I = 1800;
   static final long load_J = 660000000000L;
   static final double init_D = 0.7;
   static final double load_D = 0.35;
   static final double read_D = 0.6;
   static final String init_Str = "The run resumed after a pause; continue the objective from the latest HOST STATE.";
   private static final Pattern init_Pattern = Pattern.compile(
      "\\n?(Current user statement evidence|Trusted current user-statement evidence ID): [0-9a-fA-F-]{36}"
   );
   static final double write_D = 0.55;
   static final double update_D = 0.15;
   static final double sync_D = 0.6;
   static final long read_J = 24000L;
   static final int load_I = 16000;
   private static final Pattern load_Pattern = Pattern.compile("<\\|[A-Za-z0-9_:.-]{1,40}\\|>");
   static final int read_I = 6;
   static final int write_I = 3;
   private long set_J;
   private volatile String get_Str;
   private final Object load_Obj = new Object();
   private volatile AiService.UserPromptQuestion init_cc_UiHelper;
   private String set_Str;
   static final String load_Str = "minecraft.killed:minecraft.ender_dragon";
   private static final String put = "hostPlanningPhase";
   private static final String add = "hostPlanAwaitingApproval";

   static boolean init_Z_cc_RenderState(AiService.SessionState s) {
      return s.update_Str.isBlank() && s.init_Deque.isEmpty() && s.load_List.stream().noneMatch(m -> "user".equals(m.role()));
   }

   static String init_Str_List_2(List<AiSessions.Inner_StreamUtil> newestFirst, String deleted, Predicate<String> empty) {
      for (AiSessions.Inner_StreamUtil m : newestFirst) {
         if (!m.id().equals(deleted) && !empty.test(m.id())) {
            return m.id();
         }
      }

      return null;
   }

   static boolean init_Z_cc_FileCache(AiService.RunState s) {
      return s == AiService.RunState.RUNNING || s == AiService.RunState.PLANNING;
   }

   static boolean load_Z_cc_FileCache(AiService.RunState s) {
      return init_Z_cc_FileCache(s) || s == AiService.RunState.CHATTING;
   }

   public static synchronized AiService init_cc() {
      if (init_cc == null) {
         init_cc = new AiService();
      }

      return init_cc;
   }

   public static AiService load_cc() {
      return init_cc;
   }

   static boolean auditIsSchematicBuildRunning() {
      AiService assistant = init_cc;
      if (assistant == null) {
         return false;
      }

      AiService.ActiveOperation current = assistant.init_cc_SyncManager;
      if (current == null || !"game:build".equals(current.id())) {
         return false;
      }

      if (current.result() != null && current.result().isDone()) {
         return false;
      }

      AiService.RunState state = assistant.init_cc_FileCache;
      if (state == AiService.RunState.RUNNING) {
         return true;
      }

      return state == AiService.RunState.CHATTING && current.hostParked();
   }

   JsonObject auditRunStep(long g, AiService.SessionState s, AiIntents.Inner_BufferHelper step, String occurrence)
      throws InterruptedException, ExecutionException {
      return this.init_JsonObject_J(g, s, step, occurrence);
   }

   boolean init_Z() {
      return this.init_Z;
   }

   private static boolean load_Z_Player(Player player) {
      AABB box = player.getBoundingBox().expandTowards(0.0, 0.6, 0.0);

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         if (player.level().getFluidState(p).is(FluidTags.LAVA)) {
            return true;
         }
      }

      return false;
   }

   static boolean init_Z_Z(boolean onGround, boolean inWater, boolean onClimbable) {
      return onGround || inWater || onClimbable;
   }

   static boolean init_Z_F(float worstSource) {
      return worstSource > 0.0F && autismclient.util.ExplosionDamageCalculator.load(worstSource);
   }

   static boolean init_Z_Str(String id) {
      return Set.of(
            "game:navigate",
            "game:goto",
            "game:follow",
            "game:surface",
            "game:cave",
            "game:mine",
            "game:mine_area",
            "game:dig",
            "game:fill",
            "game:replace",
            "game:copy",
            "game:paste",
            "game:brew",
            "game:smith",
            "game:anvil",
            "game:harvest_wheat",
            "game:explore",
            "game:find_village",
            "game:build",
            "game:collect_drops",
            "game:break",
            "game:place",
            "game:place_near",
            "game:interact",
            "game:interact_entity",
            "game:survey",
            "game:telly",
            "game:scaffold",
            "game:chop",
            "game:collect",
            "game:obtain",
            "game:container",
            "game:equip",
            "game:smelt_auto",
            "game:trade",
            "game:farm",
            "game:dimension",
            "game:prospect",
            "game:recover",
            "game:plan",
            "game:sleep",
            "game:fish",
            "game:bucket",
            "game:ride",
            "game:redstone",
            "game:tidy",
            "game:eat",
            "game:barter",
            "game:clear_crystals",
            "game:portal_cast"
         )
         .contains(id);
   }

   static boolean init_Z_I(int hostilesWithin6, float health, long millisSinceDamage) {
      return hostilesWithin6 > 0 && (health <= 8.0F || millisSinceDamage < 3000L);
   }

   static AiOperation.Result init_AiOperation_Result_AiOperation(AiOperation operation, AiFailure cause, String message) {
      boolean pending = operation.requiresReconciliationAfterEmergency();
      return new AiOperation.Result(
         pending ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
         message,
         AiJson.objectOf("interruptedActionNeedsReconciliation", pending, "reobserveBeforeResuming", true).toString(),
         List.of(),
         cause
      );
   }

   static boolean init_Z_cc_FileCache(AiService.RunState state, long threatEscalatedAt, long now, boolean planMode) {
      return state == AiService.RunState.PAUSED && threatEscalatedAt != 0L && now - threatEscalatedAt >= 20000L && !planMode;
   }

   static boolean init_Z_I(int hostilesNear, boolean sameDimension, boolean executing, boolean planMode) {
      return hostilesNear > 0 && sameDimension && executing && !planMode;
   }

   static String init_Str_F(float health, boolean armed, boolean planMode, boolean reflexSource) {
      return !planMode && !reflexSource ? (health > 8.0F && armed ? "game:fight" : "game:flee") : "";
   }

   static boolean load_Z_Str(String itemId) {
      return (itemId.contains("sword") || itemId.contains("_axe")) && !itemId.contains("wooden_") && !itemId.contains("golden_");
   }

   static boolean init_Z_J(long dayTime) {
      return dayTime >= 12500L && dayTime <= 13100L;
   }

   static boolean init_Z_Z(boolean canSleep, boolean overworld, boolean pendingIsModelOp, boolean executing, boolean planMode) {
      return canSleep && executing && !planMode && overworld && !pendingIsModelOp;
   }

   private static boolean init_Z_Minecraft(Minecraft mc) {
      LocalPlayer p = mc.player;
      if (p == null
         || p.getAbilities().invulnerable
         || p.onGround()
         || p.isInWater()
         || p.onClimbable()
         || p.getAbilities().flying
         || p.isFallFlying()
         || p.isPassenger()) {
         return false;
      } else if (p.tickCount < 40) {
         return false;
      } else if (!(p.getDeltaMovement().y > -0.5) && !(p.fallDistance < 3.0)) {
         boolean water = AiGameState.countInventoryItem("minecraft:water_bucket") > 0
            && !mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES);
         return !water && AiGameState.countInventoryItem("minecraft:cobweb") <= 0 ? false : AiDanger.init_i_DataUtil_Minecraft(mc).fallDamage() >= 4.0F;
      } else {
         return false;
      }
   }

   static boolean init_Z_J(long dayTime, boolean overworld, boolean hasBedOrShelter, boolean pendingIsModelOp, boolean executing, boolean planMode) {
      return executing && !planMode && overworld && !hasBedOrShelter && !pendingIsModelOp && init_Z_J(dayTime);
   }

   static boolean read_Z_Str(String id) {
      return Set.of(
            "game:navigate",
            "game:goto",
            "game:follow",
            "game:surface",
            "game:cave",
            "game:explore",
            "game:find_village",
            "game:telly",
            "game:scaffold",
            "game:flee",
            "game:burrow",
            "game:mine",
            "game:mine_area",
            "game:dig",
            "game:chop",
            "game:prospect",
            "game:collect",
            "game:collect_drops",
            "game:fight",
            "game:shoot",
            "game:dodge",
            "game:obtain",
            "game:recover",
            "game:plan",
            "game:build",
            "game:fill",
            "game:replace",
            "game:paste",
            "game:harvest_wheat",
            "game:farm",
            "game:container",
            "game:trade",
            "game:craft",
            "game:smelt_auto",
            "game:brew",
            "game:smith",
            "game:anvil",
            "game:sleep",
            "game:dimension",
            "game:bucket",
            "game:interact_entity",
            "game:break",
            "game:place",
            "game:interact",
            "game:barter",
            "game:clear_crystals",
            "game:portal_cast",
            "game:dragon_fight",
            "game:nether_entry",
            "game:speedrun",
            "game:nether_run",
            "game:stronghold",
            "game:end_run"
         )
         .contains(id);
   }

   static String init_Str_Player(Player player) {
      if (!init_Z_Player(player)) {
         return null;
      } else if (player.isOnFire() && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
         return "on fire";
      } else if (player.isInWall()) {
         return "suffocating inside a block";
      } else if (player.isFullyFrozen()) {
         return "freezing in powder snow";
      } else {
         return AiBreathe.init(player) ? "running out of air under water" : null;
      }
   }

   static boolean init_Z_Player(Player player) {
      return player != null && !player.getAbilities().invulnerable;
   }

   boolean load_Z() {
      return this.init_cc_SyncManager != null;
   }

   private AiService() {
      AiService.SessionState initial = new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis());
      this.init_Map.put(initial.init_Str, initial);
      this.write_Str = initial.init_Str;
      this.init_ExecutorService
         .execute(
            () -> {
               try {
                  // a bad settings file blocks tasks until settings are saved, but must not stop the session store from loading
                  try {
                     this.init_AiSettings = AiSettings.load(this.init_AiMemoryStore.init_Path());
                  } catch (Exception e) {
                     this.push_Str = "AI settings: " + e.getMessage();
                     this.init_V_Str_Str("error", this.push_Str);
                  }

                  this.init_AiMemoryStore.init_V();
                  this.init_AiRegions.init(this.init_AiMemoryStore.init_Path());
                  AiSourceKnowledge.init_V();
                  AiPlaybooks.init_V();
                  autismclient.util.PacketSchemaRegistry.init_I();
                  AiMemoryStore.init_V_Path_I(this.init_AiMemoryStore.init_Path().resolve("images"), 32);
                  synchronized (this) {
                     this.init_cd = new AiSessions(this.init_AiMemoryStore.init_Path());
                     this.init_cd.init_V();

                     try {
                        this.init_cd.init_List();
                     } catch (Exception e) {
                        this.init_V_Str_Str("error", "Could not remove empty sessions: " + e.getMessage());
                     }

                     Path task = this.init_AiMemoryStore.init_Path().resolve("task.json");
                     if (Files.exists(task)) {
                        try {
                           AiSessions.Inner_DataUtil migrated = AiSessions.init(
                              AiJson.parseObject(Files.readString(task)),
                              UUID.randomUUID().toString(),
                              System.currentTimeMillis(),
                              this.init_AiSettings.maxMinutes
                           );
                           this.init_cd.init_V_cd_DataUtil(migrated);
                           Files.delete(task);
                           this.init_V_Str_Str("system", "Previous task migrated into session '" + migrated.load_Str + "'");
                        } catch (Exception e) {
                           this.init_V_Str_Str("error", "Could not migrate previous task.json: " + e.getMessage());
                        }
                     }

                     AiService.SessionState preliminary = this.init_cc_RenderState();
                     List<AiSessions.Inner_StreamUtil> metas = this.init_cd.load();
                     if (!metas.isEmpty()) {
                        boolean keepPreliminary = !init_Z_cc_RenderState(preliminary);
                        if (keepPreliminary) {
                           this.init_cd.init_V_cd_DataUtil(this.init_cd_DataUtil_cc_RenderState(preliminary));
                        }

                        AiService.SessionState restored = this.init_cc_RenderState_cd_DataUtil(this.init_cd.init_cd_DataUtil_Str(metas.get(0).id()));
                        if (!keepPreliminary) {
                           restored.load_List.addAll(preliminary.load_List);
                           this.init_Map.remove(preliminary.init_Str);
                        }

                        this.init_Map.put(restored.init_Str, restored);
                        this.write_Str = restored.init_Str;
                        this.init_cc_FileCache = init_cc_FileCache_cc_RenderState(restored);
                        if (this.init_cc_FileCache == AiService.RunState.PAUSED) {
                           this.init_V_Str_Str("system", "Previous objective restored paused. Resume after joining the correct world.");
                        }
                     }
                  }

                  if (!this.init_AiMemoryStore.init_Str().isBlank()) {
                     this.init_V_Str_Str("system", this.init_AiMemoryStore.init_Str());
                  }
               } catch (Exception e) {
                  this.push_Str = e.getMessage();
                  this.init_V_Str_Str("error", "AI persistence: " + e.getMessage());
               } finally {
                  this.load_Z = true;
               }
            }
         );
   }

   private AiService.SessionState init_cc_RenderState() {
      return this.init_Map.get(this.write_Str);
   }

   public AiService.RunState init_cc_FileCache() {
      return this.init_cc_FileCache;
   }

   public boolean read_Z() {
      AiChatMode lane = this.init_FieldCache;
      return lane != null && lane.init_Z();
   }

   public String init_Str() {
      AiChatMode lane = this.init_FieldCache;
      return lane == null ? "Chat mode off" : lane.init_Str();
   }

   public JsonObject init_JsonObject() {
      AiChatMode lane = this.init_FieldCache;
      if (lane != null) {
         return lane.init_JsonObject();
      }

      JsonObject saved = this.init_JsonObject;
      return saved != null && !saved.isEmpty() ? saved.deepCopy() : AiJson.objectOf("active", false, "summary", "Chat mode off");
   }

   boolean write_Z() {
      return this.load_Z && this.push_Str.isEmpty() && !this.init_AiCatalog_Snapshot().entries().isEmpty();
   }

   void init_V() {
      AiObservation snapshot = AiGameState.observe(this.flush_Str, this.close_Str, "qualification-smoke");
      AiCatalog.Snapshot contracts = this.init_AiCatalog_Snapshot();
      this.init_ExecutorService
         .execute(
            () -> {
               try {
                  String rebuilt = AiContext.init(contracts, snapshot, "Inspect the installed client", "Read-only qualification smoke", List.of(), 96000);
                  if (!rebuilt.contains(contracts.revision())) {
                     throw new IllegalStateException("Missing catalog revision in mandatory context");
                  }

                  AiJson.writeAtomicJson(
                     this.init_AiMemoryStore.init_Path().resolve("qualification-smoke.json"),
                     AiJson.objectOf(
                        "at",
                        System.currentTimeMillis(),
                        "catalogEntries",
                        contracts.entries().size(),
                        "families",
                        contracts.families(),
                        "contextCharacters",
                        rebuilt.length(),
                        "observation",
                        snapshot,
                        "result",
                        "PASS",
                        "scope",
                        "catalog/context/world observation and window opening only; no gameplay or model qualification"
                     )
                  );
                  System.out
                     .println(
                        "[AI qualification] PASS: catalog=" + contracts.entries().size() + ", mandatory context reconstructed, world observed, window opened"
                     );
               } catch (Exception e) {
                  System.out.println("[AI qualification] FAIL: " + e.getMessage());
               }
            }
         );
   }

   public String load_Str() {
      return this.init_cc_RenderState().update_Str;
   }

   String read_Str() {
      return this.init_cc_RenderState().close_Str;
   }

   public String write_Str() {
      return this.init_cc_RenderState().sync_Str;
   }

   public String update_Str() {
      return this.init_cc_RenderState().init_JsonObject.toString();
   }

   String sync_Str() {
      return this.init_cc_SyncManager == null ? "{}" : this.poll_Str;
   }

   String flush_Str() {
      return this.poll_Str;
   }

   public String close_Str() {
      return this.load_JsonObject.toString();
   }

   public List<String> init_List() {
      return List.copyOf(this.init_cc_RenderState().init_Deque);
   }

   public void init_V_Str(String text) {
      if (text != null && !text.isBlank()) {
         AiService.SessionState s = this.init_cc_RenderState();
         if (text.length() <= 16000 && s.init_Deque.size() < 20) {
            s.init_Deque.add(text);
            this.init_V_Str_Str("system", "Objective queued: " + text);
            this.put_V();
         } else {
            this.init_V_Str_Str("error", "Objective queue limit reached");
         }
      }
   }

   public String open_Str() {
      return this.sync_Str;
   }

   public long init_J() {
      return this.init_cc_RenderState().read_J;
   }

   public long load_J() {
      return this.init_cc_RenderState().write_J;
   }

   public int init_I() {
      return this.init_cc_RenderState().init_I;
   }

   public long read_J() {
      return this.write_J() / 1000000L;
   }

   public AiSettings init_AiSettings() {
      return this.init_AiSettings.copy();
   }

   List<AiRegions.Region> load_List() {
      return this.init_AiRegions.init(this.close_Str);
   }

   void init(BlockPos position, int beds, int bells, int paths, int farmland, int villagers, String from) {
      String evidenceId = UUID.randomUUID().toString();
      AiRegions.Region region = new AiRegions.Region(
         this.close_Str,
         position.getX(),
         position.getY(),
         position.getZ(),
         System.currentTimeMillis(),
         this.flush_Str,
         evidenceId,
         beds,
         bells,
         paths,
         farmland,
         villagers,
         from.isBlank() ? List.of() : List.of(from)
      );
      this.init_AiRegions.init(region);
      AiObservation proof = new AiObservation(evidenceId, this.flush_Str, this.close_Str, System.currentTimeMillis(), "terrain-survey", AiJson.GSON.toJson(region));
      this.load_ExecutorService.execute(() -> {
         try {
            this.init_AiMemoryStore.init(proof);
         } catch (Exception e) {
            this.init_V_Str_Str("error", e.getMessage());
         }
      });
   }

   public AiMemoryStore init_AiMemoryStore() {
      return this.init_AiMemoryStore;
   }

   public AiCatalog.Snapshot init_AiCatalog_Snapshot() {
      return this.init_AiCatalog.init_AiCatalog_Snapshot();
   }

   public String poll_Str() {
      return this.close_Str;
   }

   public AiObservation init_AiObservation() {
      return this.init_AiObservation;
   }

   String push_Str() {
      AiObservation u = this.load_AiObservation;
      return u == null ? null : u.id();
   }

   public List<AiService.TranscriptEntry> read_List() {
      AiService.SessionState s = this.init_cc_RenderState();
      return s == null ? List.of() : List.copyOf(s.load_List);
   }

   public void init_V_Str_Str(String role, String text) {
      this.init_V_Str_3(role, text, "");
   }

   public void init_V_Str_3(String role, String text, String detail) {
      this.init_V_cc_RenderState(this.init_cc_RenderState(), role, text, detail);
   }

   private void init_V_cc_RenderState(AiService.SessionState s, String role, String text) {
      this.init_V_cc_RenderState(s, role, text, "");
   }

   private void init_V_cc_RenderState(AiService.SessionState s, String role, String text, String detail) {
      if (s != null && text != null && !text.isBlank()) {
         s.load_List
            .add(
               new AiService.TranscriptEntry(
                  System.currentTimeMillis(),
                  role,
                  text.length() > 32000 ? text.substring(0, 32000) : text,
                  detail.length() > 16000 ? detail.substring(0, 16000) + " [truncated]" : detail
               )
            );

         while (s.load_List.size() > 300) {
            s.load_List.remove(0);
         }
      }
   }

   public List<AiSessions.Inner_StreamUtil> write_List() {
      AiSessions storeRef = this.init_cd;
      List<AiSessions.Inner_StreamUtil> metas = new ArrayList<>();
      if (storeRef != null) {
         for (AiSessions.Inner_StreamUtil m : storeRef.load()) {
            if (!storeRef.init_Z_Str(m.id()) || m.id().equals(this.write_Str)) {
               metas.add(m);
            }
         }
      }

      AiService.SessionState s = this.init_cc_RenderState();
      if (s != null) {
         AiSessions.Inner_StreamUtil live = new AiSessions.Inner_StreamUtil(
            s.init_Str, s.load_Str, s.init_J, s.load_J, s.read_Str, this.init_cc_FileCache.name(), s.load_List.size()
         );
         boolean replaced = false;

         for (int i = 0; i < metas.size(); i++) {
            if (metas.get(i).id().equals(s.init_Str)) {
               metas.set(i, live);
               replaced = true;
               break;
            }
         }

         if (!replaced) {
            metas.add(0, live);
         }
      }

      return List.copyOf(metas);
   }

   public String pop_Str() {
      return this.write_Str;
   }

   public String tick_Str() {
      AiService.SessionState s = this.init_cc_RenderState();
      return s == null ? "" : s.load_Str;
   }

   private static AiService.RunState init_cc_FileCache_cc_RenderState(AiService.SessionState s) {
      if (s.update_Str.isBlank()) {
         return AiService.RunState.IDLE;
      }

      AiService.RunState var10000;
      switch (s.write_Str) {
         case "COMPLETED":
            var10000 = AiService.RunState.COMPLETED;
            break;
         case "ANSWERED":
            var10000 = AiService.RunState.ANSWERED;
            break;
         case "FAILED":
            var10000 = AiService.RunState.FAILED;
            break;
         case "IDLE":
            var10000 = AiService.RunState.IDLE;
            break;
         case "CHATTING":
            var10000 = s.update_Str.isBlank() ? AiService.RunState.IDLE : AiService.RunState.PAUSED;
            break;
         default:
            var10000 = AiService.RunState.PAUSED;
      }

      return var10000;
   }

   public synchronized String run_Str() {
      if (this.load_Z && this.init_cd != null) {
         AiService.SessionState current = this.init_cc_RenderState();
         if (current != null && init_Z_cc_RenderState(current) && !init_Z_cc_FileCache(this.init_cc_FileCache)) {
            this.sync_Str = "";
            return current.init_Str;
         }

         this.sync_V_Str("Chat mode stopped: session switched");
         if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
            this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Session switched; task paused");
         }

         this.read_V_cc_RenderState(current);
         this.init_V_cc_RenderState(new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis()));
         return this.write_Str;
      } else {
         this.init_V_Str_Str("system", "AI storage is still loading");
         return null;
      }
   }

   private void init_V_cc_RenderState(AiService.SessionState target) {
      this.run_Str = null;
      AiService.SessionState previous = this.init_cc_RenderState();
      if (previous != null && previous != target && init_Z_cc_RenderState(previous)) {
         this.init_Map.remove(previous.init_Str);
      }

      this.init_Map.put(target.init_Str, target);
      this.write_Str = target.init_Str;
      target.load_J = System.currentTimeMillis();
      this.init_cc_FileCache = init_cc_FileCache_cc_RenderState(target);
      this.sync_Str = "";
      this.load_Map.clear();
      this.read_Map.clear();
      this.update_Str = null;
      this.init_cz.init_V();
      this.read_V_cc_RenderState(target);
   }

   public synchronized void load_V_Str(String id) {
      if (id != null && !id.equals(this.write_Str)) {
         if (this.load_Z && this.init_cd != null) {
            AiService.SessionState loaded = this.init_Map.get(id);
            if (loaded != null) {
               this.load_V_cc_RenderState(loaded);
            } else {
               this.run_Str = id;
               AiSessions storeRef = this.init_cd;
               this.load_ExecutorService.execute(() -> {
                  AiService.SessionState target;
                  try {
                     target = this.init_cc_RenderState_cd_DataUtil(storeRef.init_cd_DataUtil_Str(id));
                  } catch (Exception e) {
                     this.init_V_Str_Str("error", "Could not load session: " + e.getMessage());
                     return;
                  }

                  Minecraft.getInstance().execute(() -> {
                     synchronized (this) {
                        if (id.equals(this.run_Str) && !this.load_Set.contains(id)) {
                           this.init_Map.putIfAbsent(id, target);
                           this.load_V_cc_RenderState(this.init_Map.get(id));
                        }
                     }
                  });
               });
            }
         } else {
            this.init_V_Str_Str("system", "AI storage is still loading");
         }
      }
   }

   private void load_V_cc_RenderState(AiService.SessionState target) {
      this.sync_V_Str("Chat mode stopped: session switched");
      if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
         this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Switched to another session; task paused");
      }

      this.read_V_cc_RenderState(this.init_cc_RenderState());
      this.init_V_cc_RenderState(target);
   }

   public synchronized void load_V_Str_Str(String id, String title) {
      if (id == null || title == null || title.isBlank()) {
         this.init_V_Str_Str("error", "Session title is blank");
      } else if (this.load_Z && this.init_cd != null) {
         String clean = title.trim();
         if (clean.length() > 96) {
            clean = clean.substring(0, 96).trim();
         }

         AiService.SessionState s = this.init_Map.get(id);
         if (s != null) {
            s.load_Str = clean;
            this.read_V_cc_RenderState(s);
         } else {
            AiSessions storeRef = this.init_cd;
            String name = clean;
            this.load_ExecutorService.execute(() -> {
               if (!this.load_Set.contains(id)) {
                  try {
                     storeRef.init_V_Str_Str(id, name);
                  } catch (Exception e) {
                     this.init_V_Str_Str("error", "Rename session: " + e.getMessage());
                  }
               }
            });
         }
      } else {
         this.init_V_Str_Str("system", "AI storage is still loading");
      }
   }

   public synchronized void read_V_Str(String id) {
      if (id != null) {
         if (this.load_Z && this.init_cd != null) {
            this.load_Set.add(id);
            if (id.equals(this.write_Str)) {
               this.sync_V_Str("Chat mode stopped: session deleted");
               if (init_Z_cc_FileCache(this.init_cc_FileCache) || this.init_cc_FileCache == AiService.RunState.PAUSED) {
                  this.init_V_cc_FileCache_Str(AiService.RunState.IDLE, "Session deleted");
               }

               String next = init_Str_List_2(this.init_cd.load(), id, this.init_cd::init_Z_Str);
               AiService.SessionState target = next == null ? null : this.init_Map.get(next);
               this.init_V_cc_RenderState(target != null ? target : new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis()));
               if (next != null && target == null) {
                  this.load_V_Str(next);
               }
            }

            this.init_Map.remove(id);
            AiSessions storeRef = this.init_cd;
            this.load_ExecutorService.execute(() -> {
               try {
                  storeRef.init_V_Str(id);
               } catch (Exception e) {
                  this.init_V_Str_Str("error", "Delete session: " + e.getMessage());
               }
            });
         } else {
            this.init_V_Str_Str("system", "AI storage is still loading");
         }
      }
   }

   public synchronized boolean write_Z_Str(String id) {
      if (id == null) {
         return false;
      }

      if (this.load_Z && this.init_cd != null) {
         AiService.SessionState source = this.init_Map.get(id);
         if (source != null && init_Z_cc_RenderState(source)) {
            return false;
         }

         if (id.equals(this.write_Str)) {
            this.sync_V_Str("Chat mode stopped: session branched");
         }

         if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
            this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Session branched; task paused");
         }

         if (source != null) {
            this.read_V_cc_RenderState(source);
         }

         AiSessions storeRef = this.init_cd;
         this.load_ExecutorService.execute(() -> {
            AiService.SessionState fork;
            try {
               fork = this.init_cc_RenderState_cd_DataUtil(storeRef.load_cd_DataUtil(id));
            } catch (Exception e) {
               this.init_V_Str_Str("error", "Branch session: " + e.getMessage());
               return;
            }

            Minecraft.getInstance().execute(() -> {
               synchronized (this) {
                  this.init_Map.put(fork.init_Str, fork);
                  this.load_V_cc_RenderState(fork);
               }
            });
         });
         return true;
      } else {
         this.init_V_Str_Str("system", "AI storage is still loading");
         return false;
      }
   }

   private void push_V() {
      CountDownLatch drained = new CountDownLatch(1);
      this.load_ExecutorService.execute(drained::countDown);

      try {
         if (!drained.await(15L, TimeUnit.SECONDS)) {
            this.init_V_Str_Str("error", "Storage flush timed out");
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   void load_V() {
      this.init_cc_RenderState().init_Z = true;
   }

   public void write_V_Str(String prompt) {
      this.load_V_Str_JsonObject(prompt, null);
   }

   public void init_V_Str_2(String label, String capability, JsonObject arguments) {
      if (!this.init_AiSettings.builtIn) {
         throw new IllegalStateException("Select the Built-In provider first");
      }

      if (this.init_cc_UiHelper != null) {
         throw new IllegalStateException("Answer the current question first");
      }

      if (!this.load_Z) {
         throw new IllegalStateException("AI storage is still loading");
      }

      if (!this.push_Str.isEmpty()) {
         throw new IllegalStateException("Resolve AI settings/memory error first: " + this.push_Str);
      }

      JsonObject local = AiLocalRequest.init(label, capability, arguments);
      if (capability.equals("game:build") || capability.equals("game:goto") || capability.equals("game:farm") && arguments.has("from")) {
         local.addProperty("placementWorld", AiWorldIdentity.init(Minecraft.getInstance()));
         local.addProperty("placementDimension", AiWorldIdentity.load(Minecraft.getInstance()));
      }

      if (capability.equals("game:build") && arguments.has("schematic")) {
         try {
            AiLocalRequest.init_V(local, AiSchematic.init_Path_Path_Str(AiSchematic.init(), AiJson.getString(arguments, "schematic", "")));
         } catch (IOException e) {
            throw new IllegalArgumentException("Schematic is no longer available: " + e.getMessage(), e);
         }
      }

      this.load_V_Str_JsonObject(label, local);
   }

   private void load_V_Str_JsonObject(String prompt, JsonObject localAction) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.SyncManager.init()) {
         this.init_V_Str_Str("error", "Autism is locked: early access requires the donator role.");
      } else if (prompt != null && !prompt.isBlank()) {
         if (prompt.length() > 16000) {
            this.init_V_Str_Str("error", "Request exceeds 16,000 characters");
         } else if (!this.load_Z) {
            this.init_V_Str_Str("system", "AI storage is still loading");
         } else if (!this.push_Str.isEmpty()) {
            this.init_V_Str_Str("error", "Resolve AI settings/memory error first: " + this.push_Str);
         } else {
            AiService.SessionState s = this.init_cc_RenderState();
            if (this.init_cc_UiHelper != null) {
               this.open(prompt);
            } else if (AiChatPolicy.read(prompt) && this.read_Z() && !AiChatPolicy.write(prompt)) {
               this.init_V_Str_Str("user", prompt);
               this.sync_V_Str("Chat mode stopped by user");
            } else {
               if (init_Z_cc_FileCache(this.init_cc_FileCache) && this.init_AiSettings.builtIn) {
                  this.init_V_cc_FileCache_Str(AiService.RunState.IDLE, "Replaced by a new request");
               }

               if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
                  if (!AiChatPolicy.load_Z(prompt) || AiChatPolicy.read(prompt) || this.update_Z() || this.init_AiSettings.planMode || this.init_AiSettings.builtIn
                     )
                   {
                     s.init_I = AiTaskDuration.init(prompt, s.init_I);
                     s.init_Queue.add(prompt);
                     s.init(prompt);
                     this.init_V_Str_Str("user", prompt);
                     this.sync_Str = "Objective amended; releasing the previous operation before replanning";
                     Minecraft.getInstance().execute(this::run_V);
                     AiScriptHost script = this.init_bZ;
                     if (script != null) {
                        Thread.ofVirtual().start(script::init);
                     }
                  } else if (AiChatPolicy.write(prompt)) {
                     this.init_V_Str_Str("user", prompt);
                     this.close_Z(prompt);
                     s.init_I = AiTaskDuration.init(prompt, s.init_I);
                     s.init_Queue.add(prompt);
                     s.init(prompt);
                     this.sync_Str = "Chat mode updated; the work request will be planned at the next action boundary";
                  } else {
                     this.init_V_Str_Str("user", prompt);
                     if (this.close_Z(prompt)) {
                        this.sync_Str = "Chat mode updated; the running operation continues";
                     }
                  }
               } else {
                  if (AiChatPolicy.load_Z(prompt)
                     && !AiChatPolicy.read(prompt)
                     && !this.update_Z()
                     && !this.init_AiSettings.planMode
                     && !this.init_AiSettings.builtIn) {
                     if (this.init_cc_FileCache == AiService.RunState.CHATTING && this.read_Z() && !AiChatPolicy.write(prompt)) {
                        this.init_V_Str_Str("user", prompt);
                        this.close_Z(prompt);
                        return;
                     }

                     this.close_Z(prompt);
                  }

                  s.close_Str = UUID.randomUUID().toString();
                  s.update_Str = prompt;
                  s.init_I = AiTaskDuration.init(prompt, this.init_AiSettings.maxMinutes);
                  s.sync_Str = "Not started";
                  this.load_Map.clear();
                  this.read_Map.clear();
                  this.update_Str = null;
                  this.init_cz.init_V();
                  s.read_J = s.write_J = s.update_J = 0L;
                  s.sync_J = 0L;
                  s.load_I = -1;
                  s.init_Z = false;
                  // Built aside and swapped in under the session lock, like every other writer: readers copy this object.
                  JsonObject fresh = new JsonObject();
                  if (localAction != null) {
                     fresh.add("builtInAction", localAction.deepCopy());
                  }

                  synchronized (s) {
                     JsonObject old = s.init_JsonObject;
                     if (old.has("hostChatMode") && old.get("hostChatMode").isJsonObject()) {
                        fresh.add("hostChatMode", old.getAsJsonObject("hostChatMode").deepCopy());
                     }

                     s.init_JsonObject = fresh;
                  }

                  s.flush_Str = "";
                  s.init_List.clear();
                  s.init(this.init_AiSettings.planMode && !this.init_AiSettings.builtIn);
                  AiRunTrace.init_bL_Path_Str(this.init_AiMemoryStore.init_Path(), s.init_Str).init_V_Str_I(prompt, s.init_I);
                  if (s.load_Str.isBlank() || s.load_Str.equals("New session")) {
                     s.load_Str = AiSessions.init_Str_Str(prompt);
                  }

                  this.init_V_Str_Str("user", prompt);
                  this.load_V_Z(false);
               }
            }
         }
      }
   }

   public JsonObject init_JsonObject_Str(String instructions) {
      String text = instructions == null ? "" : instructions.trim();
      if (text.isEmpty()) {
         return this.load_JsonObject_Str(this.init_Str());
      }

      this.init_V_Str_Str("user", "Keep chatting: " + text);

      try {
         AiChatPolicy.Inner_DataUtil base = AiChatPolicy.Inner_DataUtil.defaults(text);
         AiChatPolicy.Inner_DataUtil config = new AiChatPolicy.Inner_DataUtil(
            base.instructions(),
            AiChatPolicy.init_List(text),
            base.onlyMentions(),
            base.includeServerChat(),
            base.maxRepliesPerMinute(),
            base.minGapMs(),
            base.debounceMs(),
            base.maxReplyChars()
         );
         return this.init_JsonObject_Str_JsonObject("Chat mode is active", this.init_JsonObject_PathHelper_DataUti(config, null, false));
      } catch (Exception error) {
         this.init_V_Str_Str("error", "Chat mode: " + error.getMessage());
         return AiJson.objectOf("status", "FAILED", "error", Objects.toString(error.getMessage(), "Chat mode could not start"), "active", false);
      }
   }

   private boolean close_Z(String objective) {
      if (this.init_AiSettings.builtIn) {
         this.init_V_Str_Str("warning", "Chat mode needs an API provider: select one in AI settings (Built-In has no model to chat with).");
         return false;
      }

      try {
         AiChatPolicy.Inner_DataUtil base = AiChatPolicy.Inner_DataUtil.defaults(objective);
         AiChatPolicy.Inner_DataUtil config = new AiChatPolicy.Inner_DataUtil(
            base.instructions(),
            AiChatPolicy.init_List(objective),
            base.onlyMentions(),
            base.includeServerChat(),
            base.maxRepliesPerMinute(),
            base.minGapMs(),
            base.debounceMs(),
            base.maxReplyChars()
         );
         this.init_JsonObject_PathHelper_DataUti(config, null, true);
         return true;
      } catch (Exception error) {
         this.init_V_Str_Str("error", "Chat mode: " + error.getMessage());
         return false;
      }
   }

   private JsonObject init_JsonObject_PathHelper_DataUti(AiChatPolicy.Inner_DataUtil config, String note, boolean auto) {
      if (config == null || config.instructions().isBlank()) {
         throw new IllegalArgumentException("chat mode needs instructions describing the persona and what to keep talking about");
      }

      if (!AutismLiteVariant.enabled() && !PacketUtil.init()) {
         throw new IllegalStateException("Autism is locked: early access requires the donator role");
      }

      if (!this.load_Z) {
         throw new IllegalStateException("AI storage is still loading");
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null) {
         AiSettings live = this.init_AiSettings;
         if (live.builtIn) {
            throw new IllegalStateException("Chat mode needs an API provider; Built-In has no model (select a provider in AI settings)");
         }

         if (!live.hasKey(live.provider.name())) {
            throw new IllegalStateException("No " + live.provider.name() + " API key is set; add one in AI settings first");
         }

         AiChatMode lane = this.init_FieldCache;
         if (lane != null && lane.init_Z()) {
            lane.init_V_PathHelper_DataUti(config);
            this.init_V_Str_Str(
               "system",
               note != null && !note.isBlank() ? note : "Chat mode updated; it keeps answering player chat in the background without touching movement."
            );
            this.put_V();
            return lane.init_JsonObject();
         }

         AiService.ChatModeHost host = new AiService.ChatModeHost();
         AiSettings chatSettings = live.copy();
         AiChatMode fresh = new AiChatMode(config, host, () -> new AiChatMode.Inner_ConfigStore(chatSettings), this.read_ExecutorService);
         host.init_FieldCache = fresh;
         this.init_FieldCache = fresh;

         try {
            fresh.init_V();
         } catch (RuntimeException | Error failure) {
            this.init_FieldCache = null;
            if (this.init_cc_FileCache == AiService.RunState.CHATTING) {
               this.init_cc_FileCache = this.load_cc_FileCache == null ? AiService.RunState.IDLE : this.load_cc_FileCache;
               this.sync_Str = "Chat mode could not start";
            }

            throw failure;
         }

         this.init_V_Str_Str(
            "system",
            note != null && !note.isBlank()
               ? note
               : (
                  auto
                     ? "Chat mode started automatically: it answers player chat in the background, never owns movement, and keeps running after the objective ends. Stop it with chat_keep stop or .ai chat stop."
                     : "Chat mode started: it answers player chat in the background, never owns movement, and keeps running after the objective ends. Stop it with chat_keep stop or .ai chat stop."
               )
         );
         this.put_V();
         return fresh.init_JsonObject();
      } else {
         throw new IllegalStateException("Join a server with a player before starting chat mode");
      }
   }

   JsonObject init_JsonObject_JsonObject_J(JsonObject args, long g) throws InterruptedException, ExecutionException {
      String action = AiJson.getString(args, "action", "status").trim().toLowerCase(Locale.ROOT);
      if (action.isEmpty()) {
         action = "status";
      }

      if (action.equals("status")) {
         return this.load_JsonObject_Str(this.init_Str());
      } else if (action.equals("stop")) {
         this.sync_V_Str("Chat mode stopped by the assistant");
         return this.load_JsonObject_Str(this.init_Str());
      } else if (!action.equals("start")) {
         throw new IllegalArgumentException("chat_keep action must be start, stop or status (got '" + action + "')");
      } else {
         AiChatPolicy.Inner_DataUtil config = AiChatPolicy.Inner_DataUtil.parse(args);
         if (config.instructions().isBlank()) {
            throw new IllegalArgumentException("chat_keep start needs instructions: the persona and what to keep talking about");
         } else {
            return this.init_Obj_J_Supplier(
               g, () -> this.init_JsonObject_Str_JsonObject("Chat mode is active", this.init_JsonObject_PathHelper_DataUti(config, null, false))
            );
         }
      }
   }

   JsonObject load_JsonObject_JsonObject_J(JsonObject args, long g) throws InterruptedException, ExecutionException {
      String raw = AiJson.getString(args, "text", "");
      if (raw.strip().startsWith("/")) {
         throw new IllegalArgumentException("chat_send is plain chat; a leading '/' would be a server command. Use game:command for commands.");
      } else {
         String text = AiChatPolicy.init(raw, 256);
         if (text.isEmpty()) {
            throw new IllegalArgumentException("chat_send needs one non-blank chat line");
         } else {
            return this.init_Obj_J_Supplier(g, () -> {
               Minecraft mc = Minecraft.getInstance();
               if (mc.getConnection() == null) {
                  throw new IllegalStateException("chat_send requires a connected server session");
               }

               mc.getConnection().sendChat(text);
               this.update_V_Str(text);
               return AiJson.objectOf("status", "SUCCEEDED", "message", "Chat line sent", "text", text, "backgroundChat", this.read_Z());
            });
         }
      }
   }

   private JsonObject load_JsonObject_Str(String message) {
      return this.init_JsonObject_Str_JsonObject(message, this.init_JsonObject());
   }

   private JsonObject init_JsonObject_Str_JsonObject(String message, JsonObject chat) {
      return AiJson.objectOf("status", "SUCCEEDED", "message", message, "chat", chat);
   }

   void update_V_Str(String text) {
      AiChatMode lane = this.init_FieldCache;
      if (lane != null && lane.init_Z()) {
         lane.init_V_Str(text);
      }
   }

   public void sync_V_Str(String reason) {
      AiChatMode lane = this.init_FieldCache;
      if (lane != null) {
         lane.load(reason != null && !reason.isBlank() ? reason : "Chat mode stopped");
      }
   }

   private void write_V_JsonObject(JsonObject status) {
      JsonObject copy = status == null ? new JsonObject() : status.deepCopy();
      this.init_JsonObject = copy;
      AiService.SessionState s = this.init_cc_RenderState();
      if (s != null) {
         synchronized (s) {
            JsonObject next = s.init_JsonObject.deepCopy();
            if (!copy.isEmpty() && AiJson.getBoolean(copy, "active", false)) {
               next.add("hostChatMode", copy.deepCopy());
            } else {
               next.remove("hostChatMode");
            }

            s.init_JsonObject = next;
         }
      }

      if (this.init_cc_FileCache == AiService.RunState.CHATTING) {
         this.sync_Str = copy.isEmpty() ? "Chat mode off" : AiJson.getString(copy, "summary", "Chatting");
      }
   }

   private synchronized void pop_V() {
      if (this.read_Z()
         && (
            this.init_cc_FileCache == AiService.RunState.COMPLETED
               || this.init_cc_FileCache == AiService.RunState.ANSWERED
               || this.init_cc_FileCache == AiService.RunState.IDLE
         )) {
         this.load_cc_FileCache = this.init_cc_FileCache;
         this.init_cc_FileCache = AiService.RunState.CHATTING;
         this.sync_Str = this.init_Str();
      }
   }

   private synchronized void tick_V() {
      this.pop_V();
   }

   private synchronized void get(String reason) {
      String line = reason != null && !reason.isBlank() ? reason : "Chat mode stopped";
      this.write_V_JsonObject(null);
      if (this.init_cc_FileCache == AiService.RunState.CHATTING) {
         this.init_cc_FileCache = this.load_cc_FileCache == null ? AiService.RunState.IDLE : this.load_cc_FileCache;
         this.sync_Str = line;
      }

      this.init_V_Str_Str("system", line);
      this.put_V();
   }

   private void load_V_Z(boolean resume) {
      this.init_V_Z_Str(resume, null);
   }

   private void init_V_Z_Str(boolean resume, String note) {
      this.flush_J = 0L;
      long g = this.init_AtomicLong.incrementAndGet();
      AiService.SessionState s = this.init_cc_RenderState();
      this.init_cc_FileCache = s != null && s.load_Z ? AiService.RunState.PLANNING : AiService.RunState.RUNNING;
      this.write_J = System.nanoTime();
      this.sync_Str = resume ? "Reobserving before resuming" : "Preparing client context";
      if (resume) {
         this.init_cz.init_V();
      }

      this.init_ExecutorService.execute(() -> this.init_V_J(g, resume, note));
   }

   public void read_V() {
      if (AutismLiteVariant.enabled() || autismclient.e.SyncManager.init()) {
         AiService.SessionState s = this.init_cc_RenderState();
         if (s != null && s.read_Z) {
            this.init_V_Str_Str("system", "A plan awaits approval; approve or reject it instead of resuming");
         } else if ((
               this.init_cc_FileCache == AiService.RunState.PAUSED
                  || this.init_cc_FileCache == AiService.RunState.BLOCKED
                  || this.init_cc_FileCache == AiService.RunState.FAILED
                  || this.init_cc_FileCache == AiService.RunState.CHATTING && !this.init_cc_RenderState().update_Str.isBlank()
            )
            && !this.init_cc_RenderState().update_Str.isBlank()) {
            this.init_V_Z_Str(true, "The run resumed after a pause; continue the objective from the latest HOST STATE.");
         }
      }
   }

   public boolean update_Z() {
      AiService.SessionState s = this.init_cc_RenderState();
      return this.init_cc_FileCache == AiService.RunState.PLANNING || this.init_cc_FileCache == AiService.RunState.PAUSED && s != null && s.read_Z;
   }

   public boolean sync_Z() {
      AiService.SessionState s = this.init_cc_RenderState();
      return s != null && s.read_Z;
   }

   public synchronized boolean flush_Z() {
      AiService.SessionState s = this.init_cc_RenderState();
      if (s != null && s.read_Z) {
         s.init();
         this.init_V_Str_Str("system", "Plan approved; execution started");
         this.put_V();
         this.init_V_Z_Str(true, "The user approved the plan above. Execute it faithfully and report any deviation.");
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean update_Z_Str(String feedback) {
      AiService.SessionState s = this.init_cc_RenderState();
      if (s == null || !s.read_Z) {
         return false;
      } else if (feedback != null && !feedback.isBlank()) {
         s.read(feedback.trim());
         this.init_V_Str_Str("user", feedback.trim());
         this.put_V();
         this.load_V_Z(true);
         return true;
      } else {
         this.init_V_Str_Str("error", "Plan rejection needs feedback for the planner");
         return false;
      }
   }

   public void write_V() {
      this.sync_V_Str("Chat mode stopped by user");
      this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Paused by user");
   }

   public void update_V() {
      this.sync_V_Str("Chat mode stopped by user");
      this.init_V_cc_FileCache_Str(AiService.RunState.IDLE, "Stopped by user");
   }

   private void init_V_cc_FileCache_Str(AiService.RunState next, String reason) {
      this.set_V();
      this.read_Z = false;
      this.flush_J = 0L;
      this.init_AtomicLong.incrementAndGet();
      this.init_cc_FileCache = next;
      this.sync_Str = reason;
      if (next == AiService.RunState.PAUSED) {
         this.init_V_Str_Str("system", reason);
         System.out.println("[AI task] PAUSED: " + reason);
      }

      AiProvider p = this.init_bA;
      if (p != null) {
         Thread.ofVirtual().start(p::close);
      }

      AiScriptHost h = this.init_bZ;
      if (h != null) {
         Thread.ofVirtual().start(h::init);
      }

      Minecraft.getInstance().execute(this::get_V);
      this.put_V();
   }

   public void sync_V() {
      this.sync_V_Str("Chat mode stopped: left the world");
      if (init_Z_cc_FileCache(this.init_cc_FileCache) && !this.init_AiSettings.autoRejoin) {
         this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "World changed; reobserve before resuming");
      }
   }

   void flush_V() {
      long start = System.nanoTime();

      try {
         if (autismclient.modules.aD.init_Z()) {
            if (this.read_Z()) {
               this.sync_V_Str("Chat mode stopped: client panic mode");
            }

            if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
               this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Client panic mode; task paused");
            }
         }

         if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
            this.close_V();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[AI] tick failed", t);
      } finally {
         this.init_aM.load(System.nanoTime() - start);
      }
   }

   private boolean auditAfloat(Minecraft mc) {
      AiService.ActiveOperation driving = this.init_cc_SyncManager;
      return mc.player != null
         && mc.player.isAlive()
         && load_Z_cc_FileCache(this.init_cc_FileCache)
         && mc.player.isInWater()
         && mc.gui.screen() == null
         && (driving == null || !driving.resources().contains("INPUT"))
         && (!mc.player.onGround() || mc.player.isUnderWater())
         && !load_Z_Player(mc.player);
   }

   private void auditReleaseSwimJump(Minecraft mc) {
      if (this.update_Z) {
         this.update_Z = false;
         mc.options.keyJump.setDown(autismclient.util.KeyMappingControl.of(mc.options.keyJump).autism$physicalForBody());
      }
   }

   public void close_V() {
      Minecraft mc = Minecraft.getInstance();
      // Early returns below skip the auto-swim block, so release a stale jump here.
      if (this.update_Z && !this.auditAfloat(mc)) {
         this.auditReleaseSwimJump(mc);
      }

      if (this.load_Z) {
         if (mc.level != this.init_Obj) {
            String previousDimension = this.init_Obj instanceof Level previous ? previous.dimension().identifier().toString() : "offline";
            if (this.read_Z()) {
               this.sync_V_Str("Chat mode stopped: world/session changed");
            }

            AiServerEvidence.clearAll();
            AiChatLedger.init_V();
            autismclient.ai.engine.AiGridRevisions.init_V();
            AiScaffold.init_V();
            this.init_Obj = mc.level;
            this.flush_Str = UUID.randomUUID().toString();
            this.update_Map = null;
            AiEventLog.init_x().auditReset(mc.getConnection());
            AiEventLog.init_x().init("dimension_changed", AiJson.objectOf("dimension", AiWorldIdentity.load(mc)));
            AiRunTrace trace = AiRunTrace.init_bL();
            if (trace != null && (init_Z_cc_FileCache(this.init_cc_FileCache) || this.init_cc_FileCache == AiService.RunState.PAUSED)) {
               String nowDimension = AiWorldIdentity.load(mc);
               trace.load(previousDimension, nowDimension);
               if ("minecraft:the_nether".equals(nowDimension)) {
                  trace.read("nether entered", "");
               } else if ("minecraft:the_end".equals(nowDimension)) {
                  trace.read("end entered", "");
               }
            }

            if (init_Z_cc_FileCache(this.init_cc_FileCache) && this.init_AiSettings.autoRejoin) {
               AiService.ActiveOperation current = this.init_cc_SyncManager;
               if (current != null && !AiDirectOps.init_Z(current.id())) {
                  this.init_V_cc_SyncManager_Z(current, true);
                  current.result()
                     .complete(AiGameOperations.result(AiOperation.Status.CANCELLED, "World/session changed during operation", new JsonObject()));
                  this.init_cc_SyncManager = null;
               }

               AiScriptHost active_scripts = this.init_bZ;
               if (active_scripts != null) {
                  Thread.ofVirtual().start(active_scripts::init);
               }

               this.init_cz.init_V();
               this.init_cc_RenderState().flush_Str = "";
               this.init_V_Str_Str("system", "World/session changed; the objective continues and the scope re-bases on the next observation.");
               String note = this.init_cc_RenderState().sync_Str
                  + "\nThe world or session changed (kick, rejoin, or world switch); the scope re-bases automatically - re-observe before continuing.";
               this.close_V(note.length() > 11900 ? note.substring(note.length() - 11900) : note);
            } else if (init_Z_cc_FileCache(this.init_cc_FileCache) && (this.init_cc_SyncManager == null || !AiDirectOps.init_Z(this.init_cc_SyncManager.id()))) {
               this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "World/session changed");
            }

            this.pop_Str = mc.player == null ? "offline" : this.init_AiMemoryStore.init_Str(AiWorldIdentity.init(mc));
            this.close_Str = this.pop_Str + "/dimension/" + AiWorldIdentity.load(mc);
         }

         if (this.load_Z) {
            AiChatMode chatLane = this.init_FieldCache;
            if (chatLane != null && chatLane.init_Z()) {
               if (!AutismLiteVariant.enabled() && !autismclient.e.CharEncoder.init()) {
                  this.sync_V_Str("Chat mode stopped: Autism is locked");
               } else if (this.init_AiSettings.builtIn) {
                  this.sync_V_Str("Chat mode stopped: the provider switched to Built-In");
               } else {
                  if (this.init_cc_FileCache == AiService.RunState.COMPLETED
                     || this.init_cc_FileCache == AiService.RunState.ANSWERED
                     || this.init_cc_FileCache == AiService.RunState.IDLE) {
                     this.pop_V();
                  }

                  chatLane.load_V();
               }
            }

            if (++this.update_I % 20 == 0 || this.init_AiCatalog.init_AiCatalog_Snapshot().entries().isEmpty()) {
               try {
                  this.init_AiCatalog.init_V();
               } catch (Exception e) {
                  this.init_V_Str_Str("error", "Catalog: " + e.getMessage());
               }
            }

            if (mc.player != null && !mc.player.isAlive()) {
               AiDeathLog.init(mc);
            }

            this.init_V_Minecraft(mc);
            if (load_Z_cc_FileCache(this.init_cc_FileCache) && mc.player != null && !mc.player.isAlive()) {
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.read();
               }

               if (this.init_cc_FileCache != AiService.RunState.CHATTING) {
                  AiService.ActiveOperation dying = this.init_cc_SyncManager;
                  if (sync_Z_Str(dying == null ? "" : dying.id())) {
                     String reason = "The deathless speedrun ended: player death was observed";

                     try {
                        AiOperation.Result terminal = dying.operation().tick();
                        this.poll_Str = terminal.dataJson();
                        this.init_cc_SyncManager = null;
                        this.init_V_cc_SyncManager_Z(dying, !terminal.terminal());
                        dying.result().complete(terminal.terminal() ? terminal : AiOperation.Result.failed(AiFailure.THREAT_INTERRUPT, reason, terminal.dataJson()));
                     } catch (Throwable error) {
                        this.init_V_cc_SyncManager_Z(dying, true);
                        this.init_cc_SyncManager = null;
                        dying.result().completeExceptionally(error);
                     }

                     this.sync_J = 0L;
                     this.init_V_cc_FileCache_Str(AiService.RunState.FAILED, reason);
                     return;
                  }

                  this.init_V_cc_FileCache_Str(
                     AiService.RunState.PAUSED,
                     this.init_AiSettings.autoRespawn ? "Player died; respawning and resuming automatically" : "Player died; task paused"
                  );
                  this.read_Z = true;
                  this.tick_Str = AiWorldIdentity.load(mc);
                  if (this.init_AiSettings.autoRespawn) {
                     this.sync_J = System.currentTimeMillis() + 1500L;
                  }

                  return;
               }

               AiService.ActiveOperation dying = this.init_cc_SyncManager;
               if (dying != null) {
                  this.init_V_cc_SyncManager_Z(dying, true);
                  dying.result().complete(AiOperation.Result.failed(AiFailure.THREAT_INTERRUPT, "Player died while chatting", "{}"));
                  this.init_cc_SyncManager = null;
               }

               if (this.init_AiSettings.autoRespawn && this.sync_J == 0L) {
                  this.sync_J = System.currentTimeMillis() + 1500L;
                  this.read_Z = true;
                  this.tick_Str = AiWorldIdentity.load(mc);
                  this.init_V_Str_Str("system", "Player died while chatting; respawning automatically and keeping the conversation.");
               }
            }

            if ((this.init_cc_FileCache == AiService.RunState.PAUSED || this.init_cc_FileCache == AiService.RunState.CHATTING)
               && this.sync_J != 0L
               && System.currentTimeMillis() >= this.sync_J) {
               this.sync_J = 0L;
               if (mc.player != null && !mc.player.isAlive() && mc.getConnection() != null) {
                  mc.getConnection().send(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
                  this.sync_J = System.currentTimeMillis() + 3000L;
               } else if (mc.player == null || !mc.player.isAlive()) {
                  this.sync_J = System.currentTimeMillis() + 1000L;
               } else if (this.read_Z) {
                  String diedIn = this.tick_Str;
                  this.read_Z = false;
                  this.tick_Str = null;
                  AiRunTrace trace = AiRunTrace.init_bL();
                  if (trace != null) {
                     trace.write();
                  }

                  if (this.init_cc_FileCache == AiService.RunState.CHATTING) {
                     this.init_V_Str_Str("system", "Respawned after dying; chat continues.");
                     this.sync_Str = this.init_Str();
                  } else {
                     int hostilesNear = 0;

                     for (Entity e : mc.level.entitiesForRendering()) {
                        if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 144.0) {
                           hostilesNear++;
                        }
                     }

                     boolean stabilize = init_Z_I(hostilesNear, AiWorldIdentity.load(mc).equals(diedIn), true, this.init_AiSettings.planMode);
                     this.init_V_Str_Str(
                        "system",
                        "Respawned after dying; the objective resumes with fresh observations"
                           + (stabilize ? " (moved to safety first)" : "")
                           + ". The grave was recorded - run game:recover within five minutes to get the dropped items back."
                     );
                     String note = this.init_cc_RenderState().sync_Str
                        + "\nThe player died and respawned"
                        + (stabilize ? " near hostile mobs; a host-parked flee moved the player to safety before anything else ran" : "")
                        + "; the death position and carried items were recorded; game:recover walks back and collects them, but the drops despawn five minutes after the death.";
                     this.close_V(note.length() > 11900 ? note.substring(note.length() - 11900) : note);
                     this.read_V();
                     if (stabilize) {
                        this.load_V_Minecraft(mc);
                     }
                  }
               }
            }

            if (mc.player != null && init_Z_cc_FileCache(this.init_cc_FileCache, this.flush_J, System.currentTimeMillis(), this.init_AiSettings.planMode)) {
               this.flush_J = 0L;
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.load("threat_escalation_resume");
               }

               this.init_V_Str_Str("system", "Auto-resuming after the threat pause.");
               this.read_V();
            }

            if (init_Z_cc_FileCache(this.init_cc_FileCache) && this.write_J() > this.init_cc_RenderState().init_I * 60000000000L) {
               this.init_V_cc_FileCache_Str(AiService.RunState.FAILED, "Task time limit reached");
            } else {
               if (this.update_I % 100 == 0) {
                  this.load_ExecutorService.execute(() -> {
                     this.load_JsonObject = this.init_aM.init();
                     if (Boolean.getBoolean("autism.ai.smoke")) {
                        try {
                           AiJson.writeAtomicJson(this.init_AiMemoryStore.init_Path().resolve("qualification-performance.json"), this.load_JsonObject);
                        } catch (Exception ex) {
                           this.init_V_Str_Str("error", ex.getMessage());
                        }
                     }
                  });
                  this.load_ExecutorService.execute(() -> {
                     try {
                        this.init_AiRegions.load(this.init_AiMemoryStore.init_Path());
                     } catch (Exception ex) {
                        this.init_V_Str_Str("error", "Region persistence: " + ex.getMessage());
                     }
                  });
                  if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
                     this.put_V();
                  }

                  if (init_Z_cc_FileCache(this.init_cc_FileCache) && mc.player != null && mc.player.isAlive() && mc.getConnection() != null) {
                     mc.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
                  }
               }

               if (load_Z_cc_FileCache(this.init_cc_FileCache) && CompatGLFW.glfwGetKey(mc.getWindow().handle(), this.init_AiSettings.emergencyKey) == 1) {
                  this.update_V();
               } else {
                  if (load_Z_cc_FileCache(this.init_cc_FileCache) && mc.player != null && mc.player.isAlive()) {
                     float health = mc.player.getHealth();
                     if (this.init_F >= 0.0F && health < this.init_F - 0.01F) {
                        this.pop_J = System.currentTimeMillis();
                        AiEventLog.init_x()
                           .init(
                              "player_damaged",
                              AiJson.objectOf(
                                 "amount", Math.round((this.init_F - health) * 10.0F) / 10.0, "health", Math.round(health * 10.0F) / 10.0
                              )
                           );
                     }

                     this.init_F = health;
                     if (load_Z_cc_FileCache(this.init_cc_FileCache) && init_Z_Minecraft(mc)) {
                        AiService.ActiveOperation falling = this.init_cc_SyncManager;
                        if (falling == null || !falling.id().equals("game:mlg")) {
                           if (falling != null) {
                              AiOperation.Result interruption = init_AiOperation_Result_AiOperation(
                                 falling.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Falling: the landing is being clutched"
                              );
                              this.init_V_cc_SyncManager_Z(falling, true);
                              falling.result().complete(interruption);
                              this.init_cc_SyncManager = null;
                           }

                           this.init_Z_Minecraft(
                              mc,
                              "game:mlg",
                              AiJson.objectOf("method", "auto", "collect", true, "timeoutSeconds", 15),
                              "fall_save",
                              "Falling - clutching the landing."
                           );
                           return;
                        }
                     }

                     AiService.ActiveOperation hazardPending = this.init_cc_SyncManager;
                     String urgentHazard = AiHazardEscape.init(mc);
                     if (!this.init_AiSettings.planMode
                        && urgentHazard != null
                        && !AiGuard.isSuppressed()
                        && (hazardPending == null || !hazardPending.id().equals("game:escape_hazard"))
                        && (hazardPending == null || hazardPending.resources().contains("INPUT"))) {
                        if (hazardPending != null) {
                           AiOperation.Result interruption = init_AiOperation_Result_AiOperation(
                              hazardPending.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Environmental emergency: " + urgentHazard
                           );
                           this.init_V_cc_SyncManager_Z(hazardPending, true);
                           hazardPending.result().complete(interruption);
                           this.init_cc_SyncManager = null;
                        }

                        AiEventLog.init_x().init("hazard_reflex", AiJson.objectOf("hazard", urgentHazard, "priority", "before-combat"));
                        if (this.init_Z_Minecraft(
                           mc,
                           "game:escape_hazard",
                           AiJson.objectOf("timeoutSeconds", 25),
                           "environmental_escape",
                           "Environmental danger - " + urgentHazard + "; escaping before resuming work."
                        )) {
                           return;
                        }
                     }

                     AiService.ActiveOperation threatPending = this.init_cc_SyncManager;
                     if (threatPending != null
                        && init_Z_Str(threatPending.id())
                        && !AiGuard.isSuppressed()
                        && init_Z_Z(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                        long now = System.currentTimeMillis();
                        String hazard = init_Str_Player(mc.player);
                        if (hazard != null) {
                           boolean announce = now - this.run_J > 10000L;
                           this.run_J = now;
                           AiOperation.Result interrupted = init_AiOperation_Result_AiOperation(
                              threatPending.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Environmental hazard interrupted the operation: the player is " + hazard
                           );
                           this.init_V_cc_SyncManager_Z(threatPending, true);
                           threatPending.result().complete(interrupted);
                           this.init_cc_SyncManager = null;
                           AiEventLog.init_x().init("hazard_interrupt", AiJson.objectOf("hazard", hazard));
                           AiRunTrace trace = AiRunTrace.init_bL();
                           if (trace != null) {
                              trace.load("hazard");
                           }

                           if (announce) {
                              this.init_V_Str_Str(
                                 "system",
                                 "Environmental hazard: the player is "
                                    + hazard
                                    + ". Deal with it first (move out, game:bucket water, dig free), then resume the interrupted step."
                              );
                           }

                           if (AiBreathe.init(mc.player)) {
                              this.init_Z_Minecraft(
                                 mc, "game:breathe", AiJson.objectOf("timeoutSeconds", 25), "breathe", "Out of air - swimming up to breathe."
                              );
                           }

                           return;
                        }

                        int hostiles = 0;
                        int ranged = 0;

                        for (Entity e : mc.level.entitiesForRendering()) {
                           if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 36.0 && AiThreats.init_Z(mc, e)) {
                              hostiles++;
                           }

                           if (e instanceof Mob m && m.isAlive() && autismclient.ai.engine.AiSight.init(e) && e.distanceToSqr(mc.player) < 256.0 && AiThreats.init_Z(mc, e)) {
                              ranged++;
                           }
                        }

                        AiDanger.Inner_DataUtil danger = AiDanger.init_i_DataUtil_Minecraft(mc);
                        boolean lethalImminent = init_Z_F(danger.worstSource());
                        boolean projectileThreat = AiProjectiles.init(mc).stream().anyMatch(AiProjectiles::init);
                        boolean unsafeMeal = threatPending.id().equals("game:eat") && (AiCombatPriority.init(hostiles, ranged) || AiSurvival.init_Entity(mc) != null);
                        if ((
                              init_Z_I(hostiles, health, now - this.pop_J)
                                 || init_Z_I(ranged, health, now - this.pop_J)
                                 || unsafeMeal
                                 || lethalImminent
                                 || projectileThreat
                           )
                           && (lethalImminent || projectileThreat || health <= 8.0F || threatPending.operation().canInterruptNow())) {
                           boolean announce = now - this.tick_J > 10000L;
                           this.tick_J = now;
                           if (!projectileThreat) {
                              this.init_Deque.addLast(now);
                           }

                           while (!this.init_Deque.isEmpty() && now - this.init_Deque.peekFirst() > 60000L) {
                              this.init_Deque.pollFirst();
                           }

                           AiOperation.Result interrupted = init_AiOperation_Result_AiOperation(
                              threatPending.operation(), AiFailure.THREAT_INTERRUPT, "Threat interrupted the operation; controls released for combat or evasion"
                           );
                           this.init_V_cc_SyncManager_Z(threatPending, true);
                           threatPending.result().complete(interrupted);
                           this.init_cc_SyncManager = null;
                           AiEventLog.init_x()
                              .init(
                                 "threat_interrupt",
                                 AiJson.objectOf("hostiles", hostiles, "ranged", ranged, "lethal", lethalImminent, "projectile", projectileThreat)
                              );
                           AiRunTrace trace = AiRunTrace.init_bL();
                           if (trace != null) {
                              trace.load("threat");
                           }

                           if (!projectileThreat && this.init_Deque.size() >= 3) {
                              int interruptionCount = this.init_Deque.size();
                              this.init_Deque.clear();
                              if (this.init_Z_Minecraft(
                                 mc,
                                 "game:flee",
                                 AiJson.objectOf("radius", 24, "distance", 48, "timeoutSeconds", 30),
                                 "threat_escalation_flee",
                                 "Repeated threats - leaving the area."
                              )) {
                                 return;
                              }

                              this.init_V_cc_FileCache_Str(
                                 AiService.RunState.PAUSED,
                                 "Repeated threat interruptions ("
                                    + interruptionCount
                                    + " within a minute): the area is too dangerous for this objective. Clear the threat or move elsewhere, then resume."
                              );
                              this.init_Deque.clear();
                              this.flush_J = System.currentTimeMillis();
                              if (trace != null) {
                                 trace.load("threat_escalation_pause");
                              }

                              return;
                           }

                           if (projectileThreat) {
                              this.init_Z_Minecraft(
                                 mc,
                                 "game:dodge",
                                 AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4),
                                 "projectile_reflex",
                                 "Incoming projectile - reacting immediately, then resuming the objective."
                              );
                           } else {
                              this.init_V_Minecraft(mc, threatPending, health, hostiles);
                           }

                           if (announce) {
                              this.init_V_Str_Str(
                                 "system",
                                 lethalImminent
                                    ? "Threat interruption: predicted lethal damage ("
                                       + danger.totalPredicted()
                                       + " from "
                                       + (danger.sources().isEmpty() ? "unknown source" : danger.sources().get(0).kind())
                                       + ") exceeds what even a totem saves. React first (game:fight, retreat, blocks, game:eat), then resume the interrupted step."
                                    : "Threat interruption: "
                                       + (
                                          hostiles > 0
                                             ? hostiles + " hostile mob(s) within 6 blocks"
                                             : ranged + " hostile mob(s) targeting the player within 16 blocks"
                                       )
                                       + (projectileThreat ? " and an inbound projectile" : "")
                                       + " with the player under pressure. React first (game:fight, blocks, game:eat, game:flee), then resume the interrupted step."
                              );
                           }

                           return;
                        }

                        int food = mc.player.getFoodData().getFoodLevel();
                        if (food <= 6
                           && !threatPending.id().equals("game:eat")
                           && hostiles == 0
                           && ranged == 0
                           && threatPending.operation().canInterruptNow()
                           && AiSurvival.init(mc.player.getInventory()) >= 0) {
                           boolean announce = now - this.get_J > 10000L;
                           this.get_J = now;
                           this.init_V_cc_SyncManager_Z(threatPending, true);
                           threatPending.result()
                              .complete(
                                 AiOperation.Result.failed(
                                    AiFailure.HUNGER, "Hunger interrupted the operation; food level " + food + "/20 with edible food in inventory", "{}"
                                 )
                              );
                           this.init_cc_SyncManager = null;
                           AiEventLog.init_x().init("hunger_interrupt", AiJson.objectOf("food", food));
                           this.init_Z_Minecraft(
                              mc,
                              "game:eat",
                              AiJson.objectOf("timeoutSeconds", 15),
                              "food_reflex",
                              "Critical hunger - eating safely before resuming work."
                           );
                           AiRunTrace trace = AiRunTrace.init_bL();
                           if (trace != null) {
                              trace.load("hunger");
                           }

                           if (announce) {
                              this.init_V_Str_Str(
                                 "system",
                                 "Hunger interruption: food level is "
                                    + food
                                    + "/20 and edible food is in the inventory. Eat first (game:eat), then resume the interrupted step."
                              );
                           }

                           return;
                        }
                     }

                     long idleNow = System.currentTimeMillis();
                     if (threatPending == null
                        && !this.init_AiSettings.planMode
                        && !AiGuard.isSuppressed()
                        && idleNow - this.set_J > 1000L
                        && init_Z_Z(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                        int hostiles = 0;
                        int ranged = 0;

                        for (Entity e : mc.level.entitiesForRendering()) {
                           if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 36.0 && AiThreats.init_Z(mc, e)) {
                              hostiles++;
                           }

                           if (e instanceof Mob m && m.isAlive() && autismclient.ai.engine.AiSight.init(e) && e.distanceToSqr(mc.player) < 256.0 && AiThreats.init_Z(mc, e)) {
                              ranged++;
                           }
                        }

                        boolean lethalImminent = init_Z_F(AiDanger.init_i_DataUtil_Minecraft(mc).worstSource());
                        boolean projectileThreat = AiProjectiles.init(mc).stream().anyMatch(AiProjectiles::init);
                        if (init_Z_I(hostiles, health, idleNow - this.pop_J)
                           || init_Z_I(ranged, health, idleNow - this.pop_J)
                           || lethalImminent
                           || projectileThreat) {
                           this.set_J = idleNow;
                           AiEventLog.init_x()
                              .init(
                                 "idle_threat",
                                 AiJson.objectOf("hostiles", hostiles, "ranged", ranged, "lethal", lethalImminent, "projectile", projectileThreat)
                              );
                           if (projectileThreat) {
                              this.init_Z_Minecraft(
                                 mc,
                                 "game:dodge",
                                 AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4),
                                 "projectile_reflex",
                                 "Incoming projectile - reacting while the objective thinks."
                              );
                           } else {
                              this.init_V_Minecraft(mc, null, health, Math.max(hostiles, ranged));
                           }

                           return;
                        }
                     }
                  }

                  AiService.ActiveOperation duskPending = this.init_cc_SyncManager;
                  if (duskPending == null
                     && mc.player != null
                     && mc.player.isAlive()
                     && init_Z_J(AiSleep.init_J())
                     && init_Z_J(
                        AiSleep.init_J(),
                        read_Z_Minecraft(mc),
                        write_Z_Minecraft(mc),
                        duskPending != null && !duskPending.hostParked(),
                        load_Z_cc_FileCache(this.init_cc_FileCache),
                        this.init_AiSettings.planMode
                     )
                     && update_Z_Minecraft(mc)) {
                     long dayIndex = mc.level.getOverworldClockTime() / 24000L;
                     long nowMs = System.currentTimeMillis();
                     if (dayIndex != this.close_J
                        && nowMs - this.open_J >= 30000L
                        && this.init_Z_Minecraft(
                           mc, "game:burrow", AiJson.objectOf("timeoutSeconds", 600), "night_burrow", "Night falls - burrowing until dawn."
                        )) {
                        this.close_J = dayIndex;
                        this.open_J = nowMs;
                        AiRunTrace trace = AiRunTrace.init_bL();
                        if (trace != null) {
                           trace.read("night_burrow", "");
                        }
                     }
                  }

                  if (mc.player != null
                     && mc.player.isAlive()
                     && load_Z_cc_FileCache(this.init_cc_FileCache)
                     && AiBreathe.init(mc.player)
                     && (this.init_cc_SyncManager == null || this.init_cc_SyncManager.hostParked() || !AiGuard.isSuppressed())) {
                     AiService.ActiveOperation reflex = this.init_cc_SyncManager;
                     if (reflex != null && reflex.hostParked() && !reflex.id().equals("game:breathe") && !reflex.id().equals("game:escape_hazard")) {
                        this.init_V_cc_SyncManager_Z(reflex, true);
                        reflex.result().complete(AiOperation.Result.failed(AiFailure.ENVIRONMENTAL_HAZARD, "Out of air", "{}"));
                        this.init_cc_SyncManager = null;
                     } else if (reflex != null && !reflex.hostParked() && !init_Z_Str(reflex.id())) {
                        AiOperation.Result interrupted = init_AiOperation_Result_AiOperation(
                           reflex.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Out of air under water: stopped to swim up; run it again from the surface"
                        );
                        this.init_V_cc_SyncManager_Z(reflex, true);
                        reflex.result().complete(interrupted);
                        this.init_cc_SyncManager = null;
                     }

                     if (this.init_cc_SyncManager == null) {
                        this.init_Z_Minecraft(
                           mc, "game:breathe", AiJson.objectOf("timeoutSeconds", 25), "breathe", "Out of air - swimming up to breathe."
                        );
                     }
                  }

                  if (this.auditAfloat(mc)) {
                     mc.options.keyJump.setDown(true);
                     this.update_Z = true;
                  } else {
                     this.auditReleaseSwimJump(mc);
                  }

                  if (this.init_cc_SyncManager == null
                     && this.update_I % 20 == 0
                     && mc.player != null
                     && mc.player.isAlive()
                     && init_Z_Z(AiSleep.load(), read_Z_Minecraft(mc), false, load_Z_cc_FileCache(this.init_cc_FileCache), this.init_AiSettings.planMode)) {
                     long dayIndex = mc.level.getOverworldClockTime() / 24000L;
                     long nowMs = System.currentTimeMillis();
                     if (dayIndex != this.poll_J
                        && nowMs - this.push_J >= 30000L
                        && write_Z_Minecraft(mc)
                        && this.init_Z_Minecraft(
                           mc, "game:sleep", AiJson.objectOf("timeoutSeconds", 60), "night_sleep", "Night falls - sleeping in the bed nearby."
                        )) {
                        this.poll_J = dayIndex;
                        this.push_J = nowMs;
                        AiRunTrace trace = AiRunTrace.init_bL();
                        if (trace != null) {
                           trace.read("night_sleep", "");
                        }
                     }
                  }

                  AiService.ActiveOperation current = this.init_cc_SyncManager;
                  if (current != null) {
                     boolean liveOperation = this.init_cc_FileCache == AiService.RunState.RUNNING
                        || this.init_cc_FileCache == AiService.RunState.CHATTING && current.hostParked();
                     if (current.generation() == this.init_AtomicLong.get() && liveOperation) {
                        AiCatalog.Entry contract = this.init_AiCatalog.init_AiCatalog_Snapshot().get(current.id());
                        if (contract != null && contract.fingerprint().equals(current.fingerprint())) {
                           try {
                              if (this.init_AiSettings.stopOnInput && !Boolean.getBoolean("autism.ai.livetask") && this.load_Z_Minecraft(mc)) {
                                 this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, "Manual game input; AI controls released");
                                 return;
                              }

                              AiOperation.Result result = current.operation().tick();
                              this.sync_Str = result.message();
                              this.poll_Str = result.dataJson();
                              autismclient.ai.engine.AiMotionAudit.init(mc, current.id(), result.message());
                              if (this.update_I % 20 == 0 && !result.terminal()) {
                                 AiService.SessionState s = this.init_cc_RenderState();
                                 synchronized (s) {
                                    JsonObject next = s.init_JsonObject.deepCopy();
                                    JsonObject old = next.has("activeOperation") && next.get("activeOperation").isJsonObject()
                                       ? next.getAsJsonObject("activeOperation")
                                       : null;
                                    JsonObject active = old != null && current.id().equals(AiJson.getString(old, "id", "")) ? old : new JsonObject();
                                    active.addProperty("id", current.id());
                                    active.addProperty("progress", result.message());
                                    JsonObject data = AiJson.parseObject(result.dataJson());

                                    for (Entry<String, JsonElement> entry : data.entrySet()) {
                                       active.add(entry.getKey(), entry.getValue());
                                    }

                                    if (active.toString().length() >= 16000) {
                                       active = AiJson.objectOf("id", current.id(), "progress", result.message());
                                    }

                                    next.add("activeOperation", active);
                                    s.init_JsonObject = next;
                                 }
                              }

                              if (result.terminal()) {
                                 autismclient.ai.engine.AiMotionAudit.init_V();
                                 this.init_cc_SyncManager = null;
                                 this.init_V_cc_SyncManager_Z(current, false);
                                 AiService.SessionState s = this.init_cc_RenderState();
                                 if (s != null && s.init_JsonObject.has("activeOperation")) {
                                    synchronized (s) {
                                       JsonObject next = s.init_JsonObject.deepCopy();
                                       next.remove("activeOperation");
                                       s.init_JsonObject = next;
                                    }
                                 }

                                 if (this.init_cc_FileCache == AiService.RunState.CHATTING && current.hostParked()) {
                                    this.sync_Str = this.init_Str();
                                 }

                                 current.result().complete(result);
                              }
                           } catch (Throwable e) {
                              AutismClientAddon.LOG.warn("[AI] operation {} threw", current.id(), e);
                              this.init_V_cc_SyncManager_Z(current, true);
                              this.init_cc_SyncManager = null;
                              current.result().completeExceptionally(e);
                           }
                        } else {
                           this.init_V_cc_SyncManager_Z(current, true);
                           current.result()
                              .complete(AiGameOperations.result(AiOperation.Status.CANCELLED, "Capability changed during execution", new JsonObject()));
                           this.init_cc_SyncManager = null;
                        }
                     } else {
                        this.get_V();
                     }
                  }
               }
            }
         }
      }
   }

   static boolean sync_Z_Str(String operationId) {
      return "game:speedrun".equals(operationId);
   }

   public void flush_V(String reason) {
      if (!Boolean.getBoolean("autism.ai.livetask")
         && this.init_AiSettings.stopOnInput
         && !autismclient.modules.FreecamController.isActive()
         && load_Z_cc_FileCache(this.init_cc_FileCache)
         && this.init_cc_SyncManager != null) {
         this.init_V_cc_FileCache_Str(AiService.RunState.PAUSED, reason);
      }
   }

   public boolean close_Z() {
      AiService.ActiveOperation p = this.init_cc_SyncManager;
      return !this.init_AiSettings.stopOnInput && p != null && load_Z_cc_FileCache(this.init_cc_FileCache) && p.resources().contains("INPUT");
   }

   public boolean open_Z() {
      return init_Z_cc_FileCache(this.init_cc_FileCache)
         || this.sync_I > 0
         || this.init_cc_SyncManager != null && this.init_cc_SyncManager.resources().contains("INPUT");
   }

   private boolean load_Z_Minecraft(Minecraft mc) {
      if (mc.gui.screen() == null && !autismclient.modules.FreecamController.isActive()) {
         for (KeyMapping key : List.of(
            mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keyAttack, mc.options.keyUse
         )) {
            if (autismclient.util.KeyMappingControl.of(key).autism$isActuallyDown()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void init_V_Minecraft(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         if (!mc.player.isAlive() && this.write_Z) {
            this.write_Z = false;
            BlockPos p = mc.player.blockPosition();
            AiEventLog.init_x()
               .init("death", AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ(), "dimension", mc.level.dimension().identifier().toString()));
         } else if (mc.player.isAlive() && !this.write_Z) {
            this.write_Z = true;
            BlockPos p = mc.player.blockPosition();
            AiEventLog.init_x().init("respawn", AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ()));
         }

         if (this.update_I % 5 == 0) {
            Set<Integer> now = new HashSet<>();

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Monster m && e.isAlive() && m.getTarget() == mc.player) {
                  now.add(e.getId());
                  if (this.read_Set.add(e.getId())) {
                     AiEventLog.init_x()
                        .init(
                           "entity_attacking", AiJson.objectOf("id", e.getId(), "type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString())
                        );
                  }
               }
            }

            this.read_Set.retainAll(now);
         }

         if (this.update_I % 20 == 0) {
            Map<String, Integer> counts = new LinkedHashMap<>();

            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty()) {
                  counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
               }
            }

            if (this.update_Map != null) {
               TreeMap<String, Integer> merged = new TreeMap<>(counts);
               this.update_Map.forEach((item, count) -> merged.merge(item, -count, Integer::sum));
               merged.forEach((item, delta) -> {
                  if (delta > 0) {
                     AiEventLog.init_x().init("item_obtained", AiJson.objectOf("item", item, "count", delta));
                  } else if (delta < 0) {
                     AiEventLog.init_x().init("item_lost", AiJson.objectOf("item", item, "count", -delta));
                  }
               });
            }

            this.update_Map = counts;
         }
      }
   }

   private void run_V() {
      if (!AiClientTick.init()) {
         AiClientTick.init_V_Runnable(this::run_V);
      } else {
         AiService.ActiveOperation current = this.init_cc_SyncManager;
         this.init_cc_SyncManager = null;
         if (current != null) {
            this.init_V_cc_SyncManager_Z(current, true);
            current.result().complete(AiOperation.Result.failed(AiFailure.CANCELLED, "Operation cancelled", "{}"));
         }
      }
   }

   private void get_V() {
      if (!AiClientTick.init()) {
         AiClientTick.init_V_Runnable(this::get_V);
      } else {
         this.run_V();

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AiService.SettingDelta c = this.init_List.get(i);
            Module m = autismclient.modules.al.init_Module_Str(c.module());
            if (m != null && m.controlRevision(c.key()) == c.revision()) {
               if (c.key().equals("enabled")) {
                  if (Boolean.toString(m.isEnabled()).equals(c.after())) {
                     m.setEnabled(Boolean.parseBoolean(c.before()));
                  }
               } else {
                  Setting<?, ? extends Setting<?, ?>> s = (Setting<?, ? extends Setting<?, ?>>)m.setting(c.key());
                  if (s != null && s.serialize().equals(c.after())) {
                     m.putSettingValue(c.key(), c.before());
                  }
               }
            }
         }

         this.init_List.clear();
      }
   }

   private void init_V_cc_SyncManager_Z(AiService.ActiveOperation current, boolean cancel) {
      if (!AiClientTick.init()) {
         AiClientTick.init_V_Runnable(() -> this.init_V_cc_SyncManager_Z(current, cancel));
      } else {
         boolean input = current.resources().contains("INPUT");
         if (input) {
            this.sync_I++;
         }

         try {
            if (cancel) {
               current.operation().cancel();
            }
         } catch (Throwable e) {
            this.init_V_Str_Str("error", "Operation cleanup: " + e.getMessage());
         } finally {
            if (current.resources().contains("INPUT")) {
               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
               Minecraft mc = Minecraft.getInstance();
               if (mc.options != null) {
                  for (KeyMapping key : List.of(mc.options.keyAttack, mc.options.keyUse)) {
                     key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
                  }
               }
            }

            try {
               current.lease().close();
            } catch (Exception e) {
               this.init_V_Str_Str("error", "Resource release: " + e.getMessage());
            } finally {
               if (input) {
                  this.sync_I--;
               }
            }
         }
      }
   }

   private void load_V_Minecraft(Minecraft mc) {
      this.init_Z_Minecraft(
         mc,
         "game:flee",
         AiJson.objectOf("radius", 16, "distance", 48, "timeoutSeconds", 25),
         "respawn_stabilize",
         "Respawned into danger; moving to safety first."
      );
   }

   private static boolean read_Z_Minecraft(Minecraft mc) {
      return mc.level != null && "minecraft:overworld".equals(mc.level.dimension().identifier().toString());
   }

   private static boolean write_Z_Minecraft(Minecraft mc) {
      for (Item item : AiItems.resolve("bed").items()) {
         if (AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(item).toString()) > 0) {
            return true;
         }
      }

      BlockPos origin = mc.player.blockPosition();

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -4, -6), origin.offset(6, 4, 6))) {
         if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
            return true;
         }
      }

      return false;
   }

   private static boolean update_Z_Minecraft(Minecraft mc) {
      BlockPos pos = mc.player.blockPosition();
      return mc.level.getHeight(Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
   }

   private void init_V_Minecraft(Minecraft mc, AiService.ActiveOperation interrupted, float health, int hostiles) {
      int weaponSlot = AiItems.bestHotbarWeaponSlot();
      boolean armed = weaponSlot >= 0 && load_Z_Str(BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(weaponSlot).getItem()).toString());
      String reflex = init_Str_F(
         health, armed, this.init_AiSettings.planMode, interrupted != null && interrupted.hostParked() && !interrupted.id().equals("game:eat")
      );
      Entity mealThreat = interrupted != null && interrupted.id().equals("game:eat") ? AiSurvival.init_Entity(mc) : null;
      if (mealThreat != null && AiThreats.init_cv_DataUtil(mealThreat) == AiThreats.Inner_DataUtil.AVOID && !reflex.isEmpty()) {
         reflex = "game:flee";
      }

      if (reflex.equals("game:flee") && health >= 8.0F && AiGuard.isOnlySmallFastMeleeThreat()) {
         reflex = "game:fight";
      }

      if (reflex.equals("game:fight") && mealThreat != null) {
         this.init_Z_Minecraft(
            mc,
            "game:fight",
            AiJson.objectOf(
               "target",
               BuiltInRegistries.ENTITY_TYPE.getKey(mealThreat.getType()).toString(),
               "entityId",
               mealThreat.getId(),
               "count",
               1,
               "radius",
               12,
               "timeoutSeconds",
               15
            ),
            "meal_threat_fight",
            "Enemy arrived during a meal - defending before resuming work."
         );
      } else if (reflex.equals("game:fight")) {
         this.init_Z_Minecraft(
            mc,
            "game:fight",
            AiJson.objectOf("target", "hostiles", "count", Math.max(1, hostiles), "radius", 12, "timeoutSeconds", 15),
            "threat_reflex_fight",
            "Under attack - defending while the objective thinks."
         );
      } else if (reflex.equals("game:flee")) {
         this.init_Z_Minecraft(
            mc,
            "game:flee",
            AiJson.objectOf("radius", 16, "distance", 40, "timeoutSeconds", 20),
            "threat_reflex_flee",
            "Under attack - moving away while the objective thinks."
         );
      }
   }

   private boolean init_Z_Minecraft(Minecraft mc, String id, JsonObject args, String traceKind, String chatLine) {
      AiCatalog.Entry entry = this.init_AiCatalog_Snapshot().get(id);
      if (entry != null && this.init_cc_SyncManager == null) {
         LocalPlayer rider = mc.player;
         if (rider != null && !rider.isPassenger() && !rider.isFallFlying()) {
            AiAssistant.init_ConfigStore().pop();
            AutoCloseable lease = MacroExecutor.acquireExternalResources(entry.contract().resources());
            if (lease == null) {
               return false;
            }

            AiOperation inner;
            try {
               inner = AiModuleScope.load(() -> {
                  AiModuleScope.init();
                  return AiGuard.allowDuringLowHealth(Objects.requireNonNull(AiTools.init_AiOperation_Str_JsonObject(id, args)));
               });
            } catch (Exception e) {
               try {
                  lease.close();
               } catch (Exception var12) {
               }

               return false;
            }

            if (inner == null) {
               try {
                  lease.close();
               } catch (Exception var13) {
               }

               return false;
            } else {
               AiOperation op = inner;
               this.poll_Str = "{}";
               this.init_cc_SyncManager = new AiService.ActiveOperation(
                  this.init_AtomicLong.get(), id, entry.fingerprint(), op, new CompletableFuture<>(), lease, entry.contract().resources(), true
               );
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.load(traceKind);
               }

               this.init_V_Str_Str("system", chatLine);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private synchronized void set_V() {
      if (this.write_J != 0L) {
         AiService.SessionState s = this.init_cc_RenderState();
         if (s != null) {
            s.update_J = s.update_J + (System.nanoTime() - this.write_J);
         }

         this.write_J = 0L;
      }
   }

   private long write_J() {
      return this.init_J_cc_RenderState(this.init_cc_RenderState());
   }

   private long init_J_cc_RenderState(AiService.SessionState s) {
      return s == null ? 0L : s.update_J + (s.init_Str.equals(this.write_Str) && this.write_J != 0L ? System.nanoTime() - this.write_J : 0L);
   }

   void init_V_J(long g) {
      if (g == this.init_AtomicLong.get() && init_Z_cc_FileCache(this.init_cc_FileCache)) {
         if (this.update_J != 0L && System.nanoTime() > this.update_J) {
            throw new IllegalStateException("Script deadline reached");
         } else {
            AiService.SessionState s = this.init_cc_RenderState();
            if (s.write_J >= this.init_AiSettings.maxTokens || this.init_J_cc_RenderState(s) > s.init_I * 60000000000L) {
               throw new IllegalStateException("Task usage/time limit reached; adjust limits before resuming");
            }
         }
      } else {
         throw new CancellationException();
      }
   }

   <T> T init_Obj_J_Supplier(long g, Supplier<T> action) throws InterruptedException, ExecutionException {
      this.init_V_J(g);
      CompletableFuture<T> result = AiClientTick.init_CompletableFuture_Supplier(() -> {
         long start = System.nanoTime();

         try {
            this.init_V_J(g);
            return action.get();
         } finally {
            this.init_aM.init(System.nanoTime() - start);
         }
      });

      try {
         while (true) {
            this.init_V_J(g);

            try {
               return result.get(200L, TimeUnit.MILLISECONDS);
            } catch (TimeoutException var9) {
            }
         }
      } finally {
         result.cancel(false);
      }
   }

   AiObservation init_AiObservation_J_Str(long g, String source) throws InterruptedException, ExecutionException {
      AiService.SessionState s = this.init_cc_RenderState();
      AiObservation o = this.init_Obj_J_Supplier(g, () -> {
         if (s.flush_Str.isBlank() || s.flush_Str.equals("offline")) {
            s.flush_Str = this.pop_Str;
         }

         if (!s.flush_Str.equals(this.pop_Str)) {
            throw new IllegalStateException("This objective belongs to another world/epoch. Start a new objective for the current world.");
         }

         if (s.load_I < 0) {
            s.load_I = AiGameState.countInventoryItem("minecraft:wheat");
         }

         return AiGameState.observe(this.flush_Str, this.close_Str, source, this.write_J() / 1000000000L, s.init_I);
      });
      this.init_AiObservation = o;

      try {
         this.init_AiMemoryStore.init(o);
      } catch (IOException e) {
         this.init_V_Str_Str("error", "Observation retained in this session; journal write failed: " + e.getMessage());
      }

      return o;
   }

   private void load_V_J(long g) throws InterruptedException {
      long until = System.nanoTime() + 660000000000L;
      String shown = null;

      while (true) {
         AiService.ActiveOperation p = this.init_cc_SyncManager;
         if (this.init_cc_SyncManager == null || !p.hostParked() || System.nanoTime() >= until) {
            return;
         }

         this.init_V_J(g);
         if (!p.id().equals(shown)) {
            shown = p.id();
            this.sync_Str = "Waiting for the survival reflex (" + p.id() + ") to finish";
         }

         Thread.sleep(250L);
      }
   }

   JsonObject init_JsonObject_J_2(long g, String id, String fingerprint, Supplier<AiOperation> factory) throws InterruptedException, ExecutionException {
      if (!AutismLiteVariant.enabled() && !PacketUtil.init()) {
         throw new IllegalStateException("Autism is locked: early access requires the donator role.");
      }

      this.load_V_J(g);
      CompletableFuture<AiOperation.Result> future = this.init_Obj_J_Supplier(
         g,
         () -> {
            AiCatalog.Entry fresh = this.init_AiCatalog_Snapshot().get(id);
            if (fresh == null || !fresh.fingerprint().equals(fingerprint)) {
               throw new IllegalArgumentException("Capability changed before dispatch; inspect it again");
            }

            if (this.init_cc_SyncManager != null) {
               throw new IllegalStateException("Another operation owns client controls");
            }

            if (read_Z_Str(id)) {
               LocalPlayer rider = Minecraft.getInstance().player;
               if (rider != null && rider.isPassenger()) {
                  throw new IllegalStateException("Dismount first (game:ride action:dismount) before " + id + "; movement keys would steer the mount");
               }

               if (rider != null && rider.isFallFlying()) {
                  throw new IllegalStateException("Land first; movement keys are meaningless while elytra-flying (" + id + ")");
               }
            }

            if (fresh.contract().classification() != AiCapabilityDescriptor.Classification.OBSERVE
               && (id.startsWith("game:") || id.startsWith("macro:") || id.equals("client:run_macro"))) {
               AiAssistant.init_ConfigStore().pop();
            }

            AutoCloseable lease = () -> {};
            if (!id.startsWith("macro:") && !id.equals("client:run_macro")) {
               lease = MacroExecutor.acquireExternalResources(this.init_AiCatalog_Snapshot().get(id).contract().resources());
               if (lease == null) {
                  throw new IllegalStateException("Another controller owns required macro resources");
               }
            }

            CompletableFuture<AiOperation.Result> f = new CompletableFuture<>();

            try {
               this.poll_Str = "{}";
               AiOperation operation = AiModuleScope.load(() -> {
                  if (id.startsWith("game:") && fresh.contract().classification() != AiCapabilityDescriptor.Classification.OBSERVE) {
                     AiModuleScope.init();
                  }

                  return factory.get();
               });
               this.init_cc_SyncManager = new AiService.ActiveOperation(
                  g,
                  id,
                  fingerprint,
                  operation,
                  f,
                  lease,
                  !id.startsWith("macro:") && !id.equals("client:run_macro") ? fresh.contract().resources() : List.of(),
                  false
               );
               return f;
            } catch (Throwable ex) {
               try {
                  lease.close();
               } catch (Exception var11x) {
               }

               throw ex;
            }
         }
      );
      AiEventLog.init_x().init("op_started", AiJson.objectOf("id", id));

      AiOperation.Result result;
      while (true) {
         this.init_V_J(g);

         try {
            result = future.get(200L, TimeUnit.MILLISECONDS);
            break;
         } catch (TimeoutException var15) {
         }
      }

      if (result.status() == AiOperation.Status.SUCCEEDED) {
         AiEventLog.init_x().init("op_completed", AiJson.objectOf("id", id, "status", result.status().name()));
      } else {
         AiEventLog.init_x().init("op_failed", AiJson.objectOf("id", id, "status", result.status().name(), "failure", result.failure().name()));
      }

      AiObservation full = this.init_AiObservation_J_Str(g, result.status() == AiOperation.Status.SUCCEEDED ? "verified-operation" : "operation-result");
      AiObservation evidence = new AiObservation(
         full.id(),
         full.session(),
         full.scope(),
         full.observedAt(),
         full.source(),
         AiGameState.compactState(AiJson.parseObject(full.dataJson()).getAsJsonObject()).toString()
      );
      if (result.status() == AiOperation.Status.SUCCEEDED) {
         this.load_Map.put(evidence.id(), evidence);
         this.read_Map.put(evidence.id(), id);
         this.update_Str = evidence.id();

         try {
            this.init_AiMemoryStore
               .init(
                  AiMemoryStore.Inner_DataUtil.WORLD,
                  this.close_Str,
                  "Observed successful capability " + id + ": " + result.message(),
                  evidence.id(),
                  this.init_AiCatalog_Snapshot().revision(),
                  result.dataJson()
               );
         } catch (Exception e) {
            this.init_V_Str_Str("error", "Verified result retained; memory could not be saved: " + e.getMessage());
         }
      }

      JsonObject output = AiJson.objectOf(
         "status",
         result.status().name(),
         "message",
         result.message(),
         "data",
         AiJson.parseObject(result.dataJson()),
         "evidence",
         AiJson.objectOf("id", evidence.id(), "observedAt", evidence.observedAt())
      );
      if (result.status() == AiOperation.Status.FAILED || result.status() == AiOperation.Status.TIMED_OUT) {
         AiFailure failure = result.status() == AiOperation.Status.TIMED_OUT && result.failure() == AiFailure.UNKNOWN ? AiFailure.TIMEOUT : result.failure();
         output.add(
            "failure",
            AiJson.objectOf("code", failure.name(), "recoverable", failure.init_Z(), "suggestedActions", AiJson.GSON.toJsonTree(failure.init_List()))
         );
      }

      try {
         JsonObject journaled = output.deepCopy();
         journaled.add("evidence", AiJson.GSON.toJsonTree(evidence));
         JsonObject entry = AiJson.objectOf("capability", id, "result", journaled);
         entry.addProperty("sessionId", this.write_Str);
         this.init_AiMemoryStore.init_V_Str_JsonElement("operation", entry);
      } catch (IOException e) {
         this.init_V_Str_Str("error", "Operation journal write failed: " + e.getMessage());
      }

      return output;
   }

   AiOperation init_AiOperation_Str_JsonObject(String id, final JsonObject args) {
      final Module m = autismclient.modules.al.init_Module_Str(id);
      if (m != null && m.settingsShareable() && !id.equals("ai-assistant")) {
         if (AiJson.getBoolean(args, "enabled", false)) {
            String restricted = init_Str_Str_AiSettings(id, this.init_AiSettings);
            if (restricted != null) {
               throw new IllegalArgumentException(restricted);
            }
         }

         for (String key : args.keySet()) {
            if (!Set.of("enabled", "settings", "persistent", "keybind", "actions").contains(key)) {
               throw new IllegalArgumentException("Unknown module control field " + key);
            }
         }

         boolean persistent = AiJson.getBoolean(args, "persistent", false);
         List<Runnable> actions = new ArrayList<>();
         if (args.has("actions") && args.get("actions").isJsonArray()) {
            for (JsonElement a : args.getAsJsonArray("actions")) {
               Setting<?, ? extends Setting<?, ?>> s = (Setting<?, ? extends Setting<?, ?>>)m.setting(a.getAsString());
               if (s == null || s.action() == null) {
                  throw new IllegalArgumentException("Unknown action setting " + a.getAsString());
               }

               if (!s.isAvailable(
                  Minecraft.getInstance().level != null,
                  Minecraft.getInstance().player != null && Minecraft.getInstance().player.containerMenu != Minecraft.getInstance().player.inventoryMenu
               )) {
                  throw new IllegalArgumentException("Unavailable action " + s.id());
               }

               actions.add(s.action());
            }
         }

         JsonObject requested = args.has("settings") ? args.getAsJsonObject("settings") : new JsonObject();
         final Map<String, String> values = new LinkedHashMap<>();

         for (Entry<String, JsonElement> e : requested.entrySet()) {
            Setting<?, ? extends Setting<?, ?>> s = (Setting<?, ? extends Setting<?, ?>>)m.setting(e.getKey());
            if (s == null) {
               throw new IllegalArgumentException("Unknown setting " + e.getKey());
            }

            if (!s.isAvailable(
               Minecraft.getInstance().level != null, Minecraft.getInstance().player.containerMenu != Minecraft.getInstance().player.inventoryMenu
            )) {
               throw new IllegalArgumentException("Unavailable setting " + s.id());
            }

            if (s.action() != null) {
               throw new IllegalArgumentException("Action settings require a dedicated capability contract");
            }

            String raw = e.getValue().isJsonPrimitive() ? e.getValue().getAsString() : e.getValue().toString();
            if (!s.choices().isEmpty() && !s.choices().contains(raw)) {
               throw new IllegalArgumentException("Invalid option " + raw);
            }

            if (s.kind().name().equals("BOOLEAN") && !Set.of("true", "false").contains(raw)) {
               throw new IllegalArgumentException("Expected boolean " + s.id());
            }

            if (Set.of("INTEGER", "DOUBLE").contains(s.kind().name())) {
               double number = Double.parseDouble(raw);
               if (!Double.isFinite(number) || number < s.min() || number > s.max() || s.kind().name().equals("INTEGER") && number != Math.rint(number)) {
                  throw new IllegalArgumentException("Out-of-range " + s.id());
               }
            }

            values.put(s.id(), s.deserialize(raw));
         }

         values.forEach((key, value) -> {
            String before = m.setting(key).serialize();
            long revision = m.controlRevision(key);
            m.putSettingValue(key, value);
            if (!persistent) {
               this.init_V_Module(m, key, before, value, revision);
            }
         });
         if (args.has("keybind")) {
            int code = AiJson.getInt(args, "keybind", -1);
            if (code < -1) {
               throw new IllegalArgumentException("keybind must be a GLFW key code, or -1 to unbind");
            }

            m.setKeybind(code);
         }

         if (args.has("enabled")) {
            boolean desired = AiJson.getBoolean(args, "enabled", false);
            if (persistent) {
               m.setEnabled(desired);
            } else {
               AiModuleScope.init(m, desired);
            }
         }

         actions.forEach(Runnable::run);
         return new AiOperation() {
            int init_I;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (this.init_Z) {
                  return AiGameOperations.result(AiOperation.Status.CANCELLED, "Module verification cancelled", new JsonObject());
               }

               JsonObject actual = new JsonObject();
               boolean match = !args.has("enabled") || m.isEnabled() == AiJson.getBoolean(args, "enabled", false);

               for (Entry<String, String> entry : values.entrySet()) {
                  String value = m.setting(entry.getKey()).serialize();
                  actual.addProperty(entry.getKey(), value);
                  if (!value.equals(entry.getValue())) {
                     match = false;
                  }
               }

               JsonObject data = AiJson.objectOf("enabled", m.isEnabled(), "settings", actual);
               return ++this.init_I < 2
                  ? AiGameOperations.result(AiOperation.Status.RUNNING, "Checking settings after owning module validation", data)
                  : AiGameOperations.result(
                     match ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                     match
                        ? "Module state verified after client ticks; gameplay outcome requires separate verification"
                        : "Owning module rules adjusted or rejected the requested combination; inspect actual settings",
                     data
                  );
            }

            @Override
            public void cancel() {
               this.init_Z = true;
            }
         };
      } else {
         throw new IllegalArgumentException("Module settings are private or unavailable");
      }
   }

   static String init_Str_Str_AiSettings(String moduleId, AiSettings s) {
      if (Set.of("flight", "boat-fly").contains(moduleId) && !s.allowFly) {
         return "'" + moduleId + "' is a detectable flight feature and is disabled; enable 'Detectable: flight' in AI Advanced settings to allow it";
      } else {
         return "no-clip".equals(moduleId) && !s.allowClips
            ? "'no-clip' is a detectable clip feature and is disabled; enable 'Detectable: clips' in AI Advanced settings to allow it"
            : null;
      }
   }

   private void init_V_Module(Module m, String key, String before, String after, long previousRevision) {
      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AiService.SettingDelta old = this.init_List.get(i);
         if (old.module().equals(m.id()) && old.key().equals(key)) {
            if (old.revision() == previousRevision) {
               before = old.before();
            }

            this.init_List.remove(i);
            break;
         }
      }

      this.init_List.add(new AiService.SettingDelta(m.id(), key, before, after, m.controlRevision(key)));
   }

   private void set(String text) {
      this.load_AiObservation = new AiObservation(
         UUID.randomUUID().toString(), this.flush_Str, "user", System.currentTimeMillis(), "user", AiJson.objectOf("request", text).toString()
      );

      try {
         this.init_AiMemoryStore.init(this.load_AiObservation);
         if (text.toLowerCase(Locale.ROOT).matches("(?s).*\\b(i prefer|remember that|from now on|always use|never use)\\b.*")) {
            this.init_AiMemoryStore
               .init(
                  AiMemoryStore.Inner_DataUtil.USER,
                  "user",
                  text,
                  this.load_AiObservation.id(),
                  this.init_AiCatalog_Snapshot().revision(),
                  "Explicit user statement; newer corrections take precedence"
               );
         }
      } catch (Exception e) {
         this.init_V_Str_Str("error", "User statement is available for this task; memory write failed: " + e.getMessage());
      }
   }

   void close_V(String value) {
      if (value.length() > 12000) {
         throw new IllegalArgumentException("Task state too long");
      }

      this.init_cc_RenderState().sync_Str = value;
      this.put_V();
   }

   void init_V_JsonObject(JsonObject value) {
      if (value.toString().length() > 12000) {
         throw new IllegalArgumentException("Task state too long");
      }

      JsonObject next = value.deepCopy();
      next.remove("hostPlanningPhase");
      next.remove("hostPlanAwaitingApproval");
      if (next.has("subgoals")) {
         for (JsonElement element : next.getAsJsonArray("subgoals")) {
            JsonObject goal = element.getAsJsonObject();
            String status = AiJson.getString(goal, "status", "pending");
            if (!Set.of("pending", "running", "blocked", "verified").contains(status)) {
               throw new IllegalArgumentException("Unknown subgoal status");
            }

            if (this.init_Z && status.equals("verified") && !this.load_Map.containsKey(AiJson.getString(goal, "evidenceId", ""))) {
               throw new IllegalArgumentException("Verified subgoals need the evidence id of a SUCCEEDED perform; mark it running or pending until one exists");
            }
         }
      }

      AiService.SessionState s = this.init_cc_RenderState();
      synchronized (s) {
         s.init_JsonObject = AiJson.auditKeepHostKeys(s.init_JsonObject, next);
      }

      this.close_V(AiJson.getString(value, "state", s.sync_Str));
   }

   private AiService.PromptContextBundle init_cc_NetHelper_cc_RenderState(AiService.SessionState s, boolean planning, int promptBudget) {
      AiCatalog.Snapshot contracts = this.init_AiCatalog_Snapshot();
      List<AiMemoryStore.Memory> memories = this.init_AiMemoryStore.init_List_Str_Str(this.close_Str, s.update_Str + " " + String.join(" ", s.init_List));
      int systemChars = (int)Math.min(2147483647L, (long)(promptBudget * 3.5 * 0.6));
      AiContext.Inner_StreamUtil prompt = AiContext.init(
         contracts,
         s.update_Str,
         List.copyOf(s.init_List),
         s.sync_Str,
         planning,
         this.init_AiSettings.vision,
         this.init_Z,
         memories,
         System.currentTimeMillis(),
         systemChars
      );
      return new AiService.PromptContextBundle(prompt, new AiContext.Inner_DataUtil(), contracts.revision());
   }

   static long init_J_J_I(long systemChars, int promptBudget) {
      double perToken = 3.5;
      long room = (long)(0.55 * promptBudget * perToken) - systemChars - 16000L;
      return Math.max((long)(0.15 * promptBudget * perToken), room);
   }

   private static long init_J_cc_NetHelper_I(AiService.PromptContextBundle epoch, int promptBudget) {
      return init_J_J_I(epoch.prompt().stable().length() + epoch.prompt().objective().length(), promptBudget);
   }

   private static long init_J_cc_NetHelper_List(AiService.PromptContextBundle epoch, List<JsonObject> history) {
      return AiContext.init_J(epoch.prompt().stable().length() + epoch.prompt().objective().length() + AiContext.init(AiJson.GSON.toJsonTree(history)));
   }

   private boolean init_Z_cc_RenderState(AiService.SessionState s, String sid, List<JsonObject> history, AiProvider transport, long budgetTokens, boolean forced) {
      long total = AiContext.init(AiJson.GSON.toJsonTree(history));
      boolean keepLastThinking = transport.init_AiSettings_Provide() == AiSettings.Provider.ANTHROPIC
         && AiSettings.anthropicThinking(transport.init_Str()) == AiSettings.Thinking.BUDGET;
      AiHistory.Inner_StreamUtil cut = AiHistory.init(
         transport.init_AiSettings_Provide(), history, (long)(0.35 * budgetTokens * 3.5), (long)(0.6 * budgetTokens * 3.5), keepLastThinking
      );
      if (cut == null) {
         return false;
      }

      if (!forced && cut.dropped().stream().noneMatch(AiHistory::init_Z)) {
         return false;
      }

      if (!forced && AiContext.init(AiJson.GSON.toJsonTree(cut.dropped())) < total / 10L) {
         return false;
      }

      String archived = AiJson.GSON.toJsonTree(cut.dropped()).toString();

      try {
         JsonObject entry = AiJson.objectOf(
            "truncated", archived.length() > 200000, "history", archived.length() > 200000 ? archived.substring(0, 200000) + " [truncated]" : archived
         );
         entry.addProperty("sessionId", sid);
         this.init_AiMemoryStore.init_V_Str_JsonElement("archived-provider-history", entry);
      } catch (IOException e) {
         this.init_V_cc_RenderState(s, "error", "Archived history retained in session; journal write failed: " + e.getMessage());
      }

      history.clear();
      history.add(transport.init_JsonObject_Str(AiHistory.init(s.update_Str, List.copyOf(s.init_List), s.sync_Str, cut.digest())));
      history.addAll(cut.kept());
      this.init_V_cc_RenderState(s, "system", "Conversation compacted to fit the context window (" + cut.dropped().size() + " older messages summarized)");
      return true;
   }

   private void init_V_J(long g, boolean resume, String note) {
      AiService.SessionState s = this.init_cc_RenderState();
      String sid = s.init_Str;
      AiSettings pinned = this.init_AiSettings.copy();
      this.init_Z = pinned.verifyOutcomes;
      this.get_Str = null;
      String hostNote = null;
      if (pinned.builtIn) {
         if (this.init_cc_FileCache == AiService.RunState.PLANNING) {
            this.init_cc_FileCache = AiService.RunState.RUNNING;
         }

         if (!this.init_cc_TaskQueue_J(g, s, true).finished()) {
            this.init_cc_FileCache = AiService.RunState.BLOCKED;
            this.sync_Str = "Not something the built-in assistant understands";
            this.init_V_cc_RenderState(s, "warning", load_Str_Str(s.update_Str));
            this.init_V_J_cc_RenderState(g, s);
         }
      } else {
         if (!resume && this.init_cc_FileCache == AiService.RunState.RUNNING && pinned.hostIntents) {
            AiService.CompletionNote desk = this.init_cc_TaskQueue_J(g, s, false);
            if (desk.finished()) {
               return;
            }

            hostNote = desk.note();
         }

         List<JsonObject> history = new ArrayList<>();
         AiService.SessionTurnTracker transcript = new AiService.SessionTurnTracker(sid);
         AiService.PendingToolBatch[] open = new AiService.PendingToolBatch[]{null};
         AiProvider transport = new AiProvider(pinned);

         try {
            this.init_bA = transport;
            pinned.validate();
            this.set(s.update_Str);
            s.sync_Str = init_Pattern.matcher(s.sync_Str).replaceAll("");
            int promptBudget = Math.max(12000, pinned.contextTokens - pinned.outputTokens);
            AiSessions storeRef = this.init_cd;
            boolean sameWire = s.open_Str == null || s.open_Str.equals(pinned.provider.name());
            AiService.PromptContextBundle epoch = this.init_cc_NetHelper_cc_RenderState(s, this.init_cc_FileCache == AiService.RunState.PLANNING, promptBudget);
            if (storeRef != null && sameWire) {
               this.push_V();
               long replay = init_J_cc_NetHelper_I(epoch, promptBudget);
               if (!resume) {
                  replay = Math.min(replay, 24000L);
               }

               try {
                  history.addAll(AiHistory.init_List(pinned.provider, storeRef.init(sid, replay, false)));
               } catch (Exception e) {
                  this.init_V_cc_RenderState(s, "error", "Transcript could not be loaded; continuing from the saved task state: " + e.getMessage());
               }
            } else if (!sameWire) {
               this.init_V_cc_RenderState(s, "system", "The provider changed since this conversation last ran; continuing from the saved task state.");
            }

            s.open_Str = pinned.provider.name();
            transcript.init();
            if (!history.isEmpty()) {
               if (!resume) {
                  transport.init_V_List_Str(history, "New objective: " + s.update_Str + (hostNote == null ? "" : "\n\n" + hostNote));
               } else if (note != null) {
                  transport.init_V_List_Str(history, note);
               }
            } else {
               boolean continuation = resume && !s.sync_Str.isBlank() && !s.sync_Str.equals("Not started");
               history.add(
                  transport.init_JsonObject_Str(
                     continuation
                        ? s.update_Str + "\nContinue from this factual task state and current observations: " + s.sync_Str
                        : (hostNote == null ? s.update_Str : s.update_Str + "\n\n" + hostNote)
                  )
               );
            }

            s.pop = init_J_cc_NetHelper_List(epoch, history);
            int outputLimitRecoveries = 0;
            int contextRecoveries = 0;
            int streamRecoveries = 0;
            int textOnlyNudges = 0;
            boolean forceCompact = false;
            String toolChoice = null;

            while (init_Z_cc_FileCache(this.init_cc_FileCache)) {
               this.init_V_J(g);
               AiObservation current = this.init_AiObservation_J_Str(g, "game");

               String amendment;
               while ((amendment = s.init_Queue.poll()) != null) {
                  transport.init_V_List_Str(history, "User update to active objective: " + amendment);
                  this.set(amendment);
               }

               long budget = Math.max(1, this.init_AiSettings.contextTokens - this.init_AiSettings.outputTokens);
               if (forceCompact || s.pop > 0.7 * budget) {
                  if (this.init_Z_cc_RenderState(s, sid, history, transport, budget, forceCompact)) {
                     epoch = this.init_cc_NetHelper_cc_RenderState(s, this.init_cc_FileCache == AiService.RunState.PLANNING, promptBudget);
                     transcript.init();
                     s.pop = init_J_cc_NetHelper_List(epoch, history);
                  }

                  forceCompact = false;
               }

               if (s.read_J >= this.init_AiSettings.maxRequests) {
                  throw new IllegalStateException("Task request limit reached");
               }

               String revision = this.init_AiCatalog_Snapshot().revision();
               transport.init_V_List_Str(
                  history,
                  AiContext.init(
                     epoch.host(),
                     s.sync_Str,
                     s.init_JsonObject,
                     s.load_I,
                     s.init_I,
                     this.init_J_cc_RenderState(s) / 1000000000L,
                     this.load_AiObservation == null ? null : this.load_AiObservation.id(),
                     revision.equals(epoch.catalogRevision()) ? null : revision,
                     current
                  )
               );
               transcript.init(history);
               transport.init_V_AiSettings(this.init_AiSettings);
               this.sync_Str = "Thinking";
               s.read_J++;
               s.push++;
               StringBuilder live = new StringBuilder();
               AiService.ToolCallGate gate = new AiService.ToolCallGate();
               boolean[] finishedByTool = new boolean[]{false};
               List<String> executedCalls = new ArrayList<>();
               long providerStarted = System.nanoTime();

               AiProvider.Inner_BufferHelper reply;
               try {
                  reply = transport.init_bA_BufferHelper_c_StreamUtil(epoch.prompt(), history, AiTools.init_JsonArray_Z(this.init_Z), toolChoice, delta -> {
                     live.append(delta);
                     this.sync_Str = live.length() > 240 ? live.substring(live.length() - 240) : live.toString();
                  }, () -> g != this.init_AtomicLong.get(), ready -> {
                     gate.init_V(ready.index());
                     if (init_Z_cc_FileCache(this.init_cc_FileCache) && !"ask_user".equals(ready.call().name())) {
                        JsonObject callResult = this.init_JsonObject_cc_RenderState(s, sid, ready.call(), g);
                        gate.init_V(ready.index(), callResult);
                        executedCalls.add(init_Str_bA_DataUtil_JsonObject(ready.call(), callResult));
                        if (!init_Z_cc_FileCache(this.init_cc_FileCache)) {
                           finishedByTool[0] = true;
                        }
                     }
                  });
               } catch (IOException e) {
                  if (finishedByTool[0]) {
                     break;
                  }

                  String error = Objects.toString(e.getMessage(), "");
                  if ((error.startsWith("Model output limit reached") || error.startsWith("Provider output exceeded limit")) && outputLimitRecoveries++ < 3) {
                     transport.init_V_List_Str(history, init_Str_List(executedCalls));
                     this.init_V_J(g);
                     continue;
                  }

                  if (AiProvider.init_Z_Str(error) && executedCalls.isEmpty() && contextRecoveries++ < 2) {
                     forceCompact = true;
                     this.init_V_cc_RenderState(s, "system", "The conversation no longer fits the context window; compacting and retrying");
                     this.init_V_J(g);
                     continue;
                  }

                  if (!executedCalls.isEmpty() && init_Z_cc_FileCache(this.init_cc_FileCache) && streamRecoveries++ < 3) {
                     this.init_V_cc_RenderState(s, "system", "The provider connection dropped after the step ran; continuing from its result");
                     transport.init_V_List_Str(history, load_Str_List(executedCalls));
                     this.init_V_J(g);
                     continue;
                  }

                  throw e;
               }

               outputLimitRecoveries = 0;
               contextRecoveries = 0;
               streamRecoveries = 0;
               AiProvider.Inner_TaskQueue usage = reply.usage();
               long sent = epoch.prompt().stable().length() + epoch.prompt().objective().length() + AiContext.init(AiJson.GSON.toJsonTree(history));
               long transcriptChars = sent + reply.wireMessage().toString().length();
               long estTokens = reply.tokens() > 0L ? reply.tokens() : Math.max(1L, (transcriptChars - s.sync_J) / 2L);
               s.write_J += estTokens;
               s.sync_J = Math.max(s.sync_J, transcriptChars);
               s.flush_J = s.flush_J + usage.inputTokens();
               s.close_J = s.close_J + usage.cachedInputTokens();
               s.open_J = s.open_J + usage.cacheWriteTokens();
               s.poll = s.poll + usage.outputTokens();
               s.pop = usage.promptTokens() > 0L ? usage.promptTokens() : AiContext.init_J(sent);
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.init_V_J_J((System.nanoTime() - providerStarted) / 1000000L, estTokens);
               }

               if (!finishedByTool[0]) {
                  this.init_V_J(g);
               }

               history.add(reply.wireMessage());
               transcript.init(history);
               String shown = init_Str_Str(reply.text());
               if (!shown.isBlank()) {
                  this.init_V_cc_RenderState(s, "assistant", shown);
               }

               toolChoice = null;
               boolean normalStop = reply.finish().isBlank() || Set.of("stop", "STOP", "end_turn", "tool_calls", "tool_use").contains(reply.finish());
               if (reply.calls().isEmpty() && normalStop && textOnlyNudges++ < 2) {
                  this.init_V_cc_RenderState(s, "system", "The reply had no action; asking the model to act instead of describing steps");
                  transport.init_V_List_Str(history, init_Str_Z(this.init_cc_FileCache == AiService.RunState.PLANNING));
                  toolChoice = "required";
                  this.init_V_J(g);
               } else {
                  if (!reply.calls().isEmpty()) {
                     textOnlyNudges = 0;
                  }

                  if (reply.calls().isEmpty()) {
                     this.init_cc_FileCache = AiService.RunState.BLOCKED;
                     this.sync_Str = "Model ended its response without a tool call; objective remains unverified";
                     if (!reply.finish().isBlank() && !Set.of("stop", "STOP", "end_turn", "tool_calls", "tool_use").contains(reply.finish())) {
                        boolean refused = Set.of("SAFETY", "content_filter", "refusal", "blocklist", "PROHIBITED_CONTENT").contains(reply.finish());
                        this.sync_Str = this.sync_Str + " (provider finish: " + reply.finish() + (refused ? " - refused by content policy" : "") + ")";
                     }

                     this.init_V_cc_RenderState(s, "warning", this.sync_Str);
                     break;
                  }

                  open[0] = new AiService.PendingToolBatch(reply.calls(), gate);
                  int k = 0;

                  while (true) {
                     if (k < reply.calls().size()) {
                        int streamIndex = gate.init_I(k);
                        label527:
                        if (streamIndex >= 0) {
                           if (gate.init_JsonObject(streamIndex) == null) {
                              if (!init_Z_cc_FileCache(this.init_cc_FileCache)) {
                                 break label527;
                              }

                              gate.init_V(streamIndex, this.init_JsonObject_cc_RenderState(s, sid, reply.calls().get(k), g));
                           }

                           if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
                              k++;
                              continue;
                           }
                        }
                     }

                     List<JsonObject> aligned = gate.init_List(
                        reply.calls().size(),
                        AiJson.objectOf("status", "CANCELLED", "message", "Turn ended by an earlier finish call; not dispatched")
                     );
                     history.addAll(transport.init_List_List_List(reply.calls(), aligned));
                     open[0] = null;
                     this.read_V_cc_RenderState(s);
                     break;
                  }
               }
            }
         } catch (CancellationException var62) {
         } catch (Exception e) {
            if (g == this.init_AtomicLong.get()) {
               this.init_cc_FileCache = AiService.RunState.FAILED;
               this.sync_Str = Objects.toString(e.getMessage(), "AI task failed");
               this.init_V_cc_RenderState(s, "error", this.sync_Str);
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.read("failed");
               }
            }
         } finally {
            AiService.PendingToolBatch round = open[0];
            if (round != null) {
               try {
                  history.addAll(
                     transport.init_List_List_List(
                        round.calls(),
                        round.gate()
                           .init_List(
                              round.calls().size(),
                              AiJson.parseObject(
                                 "{\"status\":\"INTERRUPTED\",\"message\":\"The run stopped before this call's result was recorded; re-observe before assuming its effect\"}"
                              )
                           )
                     )
                  );
               } catch (RuntimeException var59) {
               }
            }

            if (!history.isEmpty()) {
               transcript.init(history);
            }

            transport.close();
            if (g == this.init_AtomicLong.get()) {
               this.set_V();
               this.init_bA = null;
               Minecraft.getInstance().execute(this::get_V);
               if (this.init_cc_FileCache == AiService.RunState.COMPLETED && !s.init_Deque.isEmpty()) {
                  String next = s.init_Deque.poll();
                  Minecraft.getInstance().execute(() -> {
                     if (s.init_Str.equals(this.write_Str) && !init_Z_cc_FileCache(this.init_cc_FileCache)) {
                        this.write_V_Str(next);
                     } else {
                        s.init_Deque.offerFirst(next);
                     }
                  });
               } else {
                  this.tick_V();
               }

               this.read_V_cc_RenderState(s);
            } else {
               this.read_V_cc_RenderState(s);
            }
         }
      }
   }

   static String init_Str_List(List<String> executedCalls) {
      String note = "Your previous response was discarded for exceeding output limits. Reply concisely; prefer short tool calls over long reasoning.";
      if (!executedCalls.isEmpty()) {
         note = note
            + " These calls from the discarded response ALREADY EXECUTED and their effects stand - do not re-issue them: "
            + String.join("; ", executedCalls)
            + ".";
      }

      return note;
   }

   static String init_Str_bA_DataUtil_JsonObject(AiProvider.Inner_DataUtil call, JsonObject result) {
      String what = call.name();
      if ("perform".equals(call.name()) && call.arguments() != null) {
         what = what + " " + AiJson.getString(call.arguments(), "id", "");
      }

      String status = AiJson.getString(result, "status", AiJson.getString(result, "outcome", "returned"));
      String message = AiJson.getString(result, "message", AiJson.getString(result, "error", ""));
      if (message.length() > 300) {
         message = message.substring(0, 300) + "...";
      }

      return what + " -> " + status + (message.isBlank() ? "" : " (" + message + ")");
   }

   static String load_Str_List(List<String> executedCalls) {
      return "The connection to you dropped while a step from your last response was running (long operations can outlive it). These calls from that response ALREADY EXECUTED and their effects stand - do not re-issue them: "
         + String.join("; ", executedCalls)
         + ". Continue from the current state.";
   }

   static String init_Str_Str(String text) {
      if (text == null) {
         return "";
      }

      if (!text.contains("<|")) {
         return text;
      }

      String out = load_Pattern.matcher(text).replaceAll(" ").strip();
      return out.matches("(?i)(?:response|assistant|answer|tool_calls?|functions?)?") ? "" : out;
   }

   static String init_Str_Z(boolean planning) {
      return planning
         ? "Your last reply had no tool call, and a reply without one ends the run. Keep researching with the allowed tools, or present the plan by calling finish with outcome answered and the full plan as the message."
         : "Your last reply had no tool call, and a reply without one ends the run. Do not tell the user how to do it or list steps for them: you operate the game, so carry out the next step now with a tool call. If the objective is already done, call finish; if the user only asked a question, call finish with outcome answered and the answer as the message; if you need a decision, call ask_user.";
   }

   private JsonObject init_JsonObject_cc_RenderState(AiService.SessionState s, String sid, AiProvider.Inner_DataUtil call, long g) {
      this.init_V_J(g);
      AiRunTrace trace = AiRunTrace.init_bL();
      long toolStarted = System.nanoTime();
      if (trace != null) {
         trace.init(call.name());
      }

      JsonObject result;
      if (call.argumentsError() != null) {
         result = AiJson.objectOf(
            "status", "FAILED", "error", "Model produced malformed tool-call arguments; resend valid JSON", "rawArgumentsExcerpt", call.argumentsError()
         );
      } else {
         try {
            result = s.init_Queue.isEmpty()
               ? this.init_cz.init_JsonObject_Str(call.name(), call.arguments(), g)
               : AiJson.objectOf("status", "CANCELLED", "message", "The user amended the objective; this earlier planned call was not dispatched");
         } catch (CancellationException e) {
            throw e;
         } catch (Exception e) {
            String error = Objects.toString(e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), "Operation failed");
            result = AiJson.objectOf("status", "FAILED", "error", error);
         }
      }

      if ("perform".equals(call.name())) {
         this.get_Str = AiJson.getString(result, "status", "FAILED");
      }

      JsonObject detail = result.deepCopy();
      detail.remove("imagePngBase64");
      this.init_V_cc_RenderState(
         s,
         "tool",
         call.name() + ": " + AiJson.getString(result, "message", AiJson.getString(result, "status", "returned result")),
         detail.toString()
      );

      try {
         JsonObject entry = AiJson.objectOf("name", call.name(), "result", detail);
         entry.addProperty("sessionId", sid);
         this.init_AiMemoryStore.init_V_Str_JsonElement("tool", entry);
      } catch (IOException e) {
         this.init_V_cc_RenderState(s, "error", "Tool result retained in session; journal write failed: " + e.getMessage());
      }

      if (trace != null) {
         trace.init_V_Str(
            call.name(),
            AiJson.getString(result, "status", AiJson.getString(result, "outcome", "returned")),
            result.has("failure") && result.get("failure").isJsonObject() ? AiJson.getString(result.getAsJsonObject("failure"), "code", "") : "",
            (System.nanoTime() - toolStarted) / 1000000L
         );
      }

      return result;
   }

   private AiService.CompletionNote init_cc_TaskQueue_J(long g, AiService.SessionState s, boolean builtIn) {
      if (builtIn && s.init_JsonObject.has("builtInAction")) {
         JsonObject local = s.init_JsonObject.getAsJsonObject("builtInAction");
         if (!AiLocalRequest.init(local, AiWorldIdentity.init(Minecraft.getInstance()), AiWorldIdentity.load(Minecraft.getInstance()))) {
            return this.init_cc_TaskQueue_J(
               g, s, AiService.RunState.BLOCKED, "warning", "This placement belongs to another world or dimension. Select the destination again."
            );
         }

         if (local.has("sourceRevision")) {
            JsonObject args = local.getAsJsonObject("arguments");

            boolean unchanged;
            try {
               unchanged = AiLocalRequest.init_Z(local, AiSchematic.init_Path_Path_Str(AiSchematic.init(), AiJson.getString(args, "schematic", "")));
            } catch (IllegalArgumentException missing) {
               unchanged = false;
            }

            if (!unchanged) {
               return this.init_cc_TaskQueue_J(
                  g, s, AiService.RunState.BLOCKED, "warning", "The schematic changed or was removed. Select it again before building."
               );
            }
         }
      }

      AiIntents.Inner_CachePool plan = builtIn && s.init_JsonObject.has("builtInAction")
         ? AiLocalRequest.init(s.init_JsonObject.getAsJsonObject("builtInAction"))
         : AiIntents.init_ad_CachePool(s.update_Str, init_ad_StreamUtil());
      if (plan.steps().isEmpty()) {
         return new AiService.CompletionNote(false, null);
      }

      List<AiIntents.Inner_BufferHelper> steps = new ArrayList<>(plan.steps());
      List<String> done = new ArrayList<>();

      try {
         s.init_Z = true;

         for (int i = 0; i < steps.size(); i++) {
            AiIntents.Inner_BufferHelper step = steps.get(i);
            if (!step.asks().isEmpty()) {
               JsonObject args = step.arguments().deepCopy();

               for (AiIntents.Inner_DataUtil ask : step.asks()) {
                  JsonObject reply = this.write(AiJson.objectOf("question", ask.question(), "options", ask.options()), g);
                  String said = AiJson.getString(reply, "answer", "");
                  if (!AiIntents.init(ask, said, args)) {
                     if (builtIn) {
                        return this.init_cc_TaskQueue_J(
                           g,
                           s,
                           AiService.RunState.BLOCKED,
                           "warning",
                           "The built-in assistant did not understand the answer \"" + said + "\"; pick one of the options"
                        );
                     }

                     this.init_V_cc_RenderState(s, "system", "That answer needs the AI; handing the request over");
                     return new AiService.CompletionNote(
                        false, init_Str_List(done, s.update_Str + " (asked \"" + ask.question() + "\", the user answered \"" + said + "\")", null)
                     );
                  }
               }

               steps.set(i, new AiIntents.Inner_BufferHelper(step.clause(), step.capability(), args, init_Str_JsonObject(args)));
            }
         }

         for (int i = 0; i < steps.size(); i++) {
            this.init_V_J(g);
            AiIntents.Inner_BufferHelper step = steps.get(i);
            String failure = null;
            int prerequisiteIndex = 0;

            for (AiIntents.Inner_BufferHelper before : this.init_List_J_ad_BufferHelper(g, step)) {
               JsonObject r = this.init_JsonObject_J(g, s, before, i + "/prerequisite/" + prerequisiteIndex++);
               done.add(init_Str_ad_BufferHelper_JsonObject(before, r));
               if (!load_Z_JsonObject(r) && !this.load_Z_J_ad_BufferHelper(g, before)) {
                  failure = before.describe() + " (needed to " + step.describe() + "): " + load_Str_JsonObject(r);
                  break;
               }
            }

            if (failure == null) {
               if ("game:craft".equals(step.capability()) && !this.init_Z_J_ad_BufferHelper(g, step)) {
                  AiIntents.Inner_BufferHelper gather = init_ad_BufferHelper_ad_BufferHelper_Z(step, true);
                  if (gather != null) {
                     step = gather;
                  }
               }

               if (AiJson.getBoolean(step.arguments(), "cookHeld", false)) {
                  step = this.init_ad_BufferHelper_J_ad_BufferHelper(g, step);
               }

               JsonObject r = this.init_JsonObject_J(g, s, step, i + "/requested");
               done.add(init_Str_ad_BufferHelper_JsonObject(step, r));
               AiIntents.Inner_BufferHelper gather = load_Z_JsonObject(r) ? null : init_ad_BufferHelper_ad_BufferHelper_JsonObject(step, r);
               if (gather != null) {
                  this.init_V_J(g);
                  r = this.init_JsonObject_J(g, s, gather, i + "/fallback");
                  done.add(init_Str_ad_BufferHelper_JsonObject(gather, r));
               }

               for (int retool = 0; retool < 3; retool++) {
                  AiIntents.Inner_BufferHelper tool = load_Z_JsonObject(r) ? null : load_ad_BufferHelper_ad_BufferHelper_JsonObject(step, r);
                  if (tool == null) {
                     break;
                  }

                  this.init_V_J(g);
                  JsonObject got = this.init_JsonObject_J(g, s, tool, i + "/tool/" + retool);
                  done.add(init_Str_ad_BufferHelper_JsonObject(tool, got));
                  if (!load_Z_JsonObject(got)) {
                     break;
                  }

                  step = read_ad_BufferHelper_ad_BufferHelper_JsonObject(step, r);
                  this.init_V_J(g);
                  r = this.init_JsonObject_J(g, s, step, i + "/retool/" + retool);
                  done.add(init_Str_ad_BufferHelper_JsonObject(step, r));
               }

               if (!load_Z_JsonObject(r)) {
                  failure = step.describe() + ": " + load_Str_JsonObject(r);
               }
            }

            if (failure != null) {
               if (builtIn) {
                  return this.init_cc_TaskQueue_J(g, s, AiService.RunState.FAILED, "error", "Could not finish: " + failure);
               }

               List<String> rest = new ArrayList<>();

               for (AiIntents.Inner_BufferHelper left : steps.subList(i, steps.size())) {
                  rest.add(left.clause().trim());
               }

               if (!plan.remainder().isBlank()) {
                  rest.add(plan.remainder());
               }

               this.init_V_cc_RenderState(s, "warning", "Quick action failed (" + failure + "); the AI takes over");
               return new AiService.CompletionNote(false, init_Str_List(done, String.join(", then ", rest), failure));
            }
         }

         if (plan.handledAll()) {
            this.set(s.update_Str);
            this.init_cc_FileCache = AiService.RunState.COMPLETED;
            this.sync_Str = "Done without an AI turn";
            List<String> what = new ArrayList<>();

            for (AiIntents.Inner_BufferHelper step : steps) {
               what.add(step.describe());
            }

            this.init_V_cc_RenderState(s, "success", "Done without an AI turn: " + String.join("; ", what));
            AiRunTrace trace = AiRunTrace.init_bL();
            if (trace != null) {
               trace.read("completed");
            }

            this.init_V_J_cc_RenderState(g, s);
            return new AiService.CompletionNote(true, null);
         } else {
            if (!builtIn) {
               this.init_V_cc_RenderState(s, "system", "Quick actions done; the AI takes over: " + plan.remainder());
               return new AiService.CompletionNote(false, init_Str_List(done, plan.remainder(), null));
            }

            List<String> what = new ArrayList<>();

            for (AiIntents.Inner_BufferHelper step : steps) {
               what.add(step.describe());
            }

            return this.init_cc_TaskQueue_J(
               g, s, AiService.RunState.BLOCKED, "warning", "Done: " + String.join("; ", what) + ". " + load_Str_Str(plan.remainder())
            );
         }
      } catch (CancellationException e) {
         this.init_V_J_cc_RenderState(g, s);
         return new AiService.CompletionNote(true, null);
      } catch (Exception e) {
         if (builtIn) {
            return this.init_cc_TaskQueue_J(g, s, AiService.RunState.FAILED, "error", "Stopped: " + e.getMessage());
         }

         this.init_V_cc_RenderState(s, "warning", "Quick actions stopped (" + e.getMessage() + "); the AI takes over");
         return new AiService.CompletionNote(false, init_Str_List(done, s.update_Str, null));
      }
   }

   private JsonObject init_JsonObject_J(long g, AiService.SessionState s, AiIntents.Inner_BufferHelper step, String occurrence) throws InterruptedException, ExecutionException {
      JsonObject stocked = AuditBuilderShulkerStock.before(this, g, s, step, occurrence);
      if (stocked != null) {
         return stocked;
      }

      JsonObject arguments = step.arguments();
      if (step.capability().equals("game:obtain") && AiJson.getBoolean(arguments, "additional", false)) {
         String key = occurrence + ":" + step.clause();
         JsonObject current = s.init_JsonObject;
         JsonObject targets = current.has("hostGetTargets") ? current.getAsJsonObject("hostGetTargets") : new JsonObject();
         if (!targets.has(key)) {
            JsonObject requested = arguments;
            JsonObject target = this.init_Obj_J_Supplier(
               g, () -> AiLocalRequest.init(requested, AiItems.countInInventory(AiItems.resolveItem(AiJson.getString(requested, "item", ""))), 2304)
            );
            synchronized (s) {
               JsonObject next = s.init_JsonObject.deepCopy();
               targets = next.has("hostGetTargets") ? next.getAsJsonObject("hostGetTargets") : new JsonObject();
               targets.add(key, target);
               next.add("hostGetTargets", targets);
               s.init_JsonObject = next;
            }

            this.read_V_cc_RenderState(s);
         }

         arguments = targets.getAsJsonObject(key);
      }

      this.init_V_cc_RenderState(s, "system", "Quick action: " + step.describe() + " (no AI turn)");
      JsonObject r = null;

      for (int attempt = 0; attempt < 6; attempt++) {
         r = this.init_JsonObject_cc_RenderState(
            s,
            s.init_Str,
            new AiProvider.Inner_DataUtil("host-" + UUID.randomUUID(), "perform", AiJson.objectOf("id", step.capability(), "arguments", arguments)),
            g
         );
         if (load_Z_JsonObject(r) || !init_Z_JsonObject(r)) {
            return r;
         }

         this.init_V_cc_RenderState(s, "system", "Quick action paused (" + load_Str_JsonObject(r) + "); it resumes when the player is safe");
         this.init_V_J_I(g, 180);
      }

      return r;
   }

   static boolean init_Z_JsonObject(JsonObject r) {
      if ("UNCERTAIN".equals(AiJson.getString(r, "status", ""))) {
         return false;
      }

      String code = r.has("failure") && r.get("failure").isJsonObject() ? AiJson.getString(r.getAsJsonObject("failure"), "code", "") : "";
      return Set.of("THREAT_INTERRUPT", "HUNGER", "LOW_HEALTH", "ENVIRONMENTAL_HAZARD").contains(code)
         ? true
         : AiJson.getString(r, "error", "").contains("Another operation owns client controls");
   }

   private void init_V_J_I(long g, int seconds) throws InterruptedException {
      long until = System.nanoTime() + seconds * 1000000000L;
      Thread.sleep(500L);

      while (this.init_cc_SyncManager != null && System.nanoTime() < until) {
         this.init_V_J(g);
         Thread.sleep(250L);
      }
   }

   private static boolean load_Z_JsonObject(JsonObject r) {
      return "SUCCEEDED".equals(AiJson.getString(r, "status", ""));
   }

   static AiIntents.Inner_BufferHelper init_ad_BufferHelper_ad_BufferHelper_JsonObject(AiIntents.Inner_BufferHelper step, JsonObject result) {
      if (!"game:craft".equals(step.capability())) {
         return null;
      }

      String reason = AiJson.getString(result, "message", AiJson.getString(result, "error", "")).toLowerCase(Locale.ROOT);
      return !reason.contains("missing") ? null : init_ad_BufferHelper_ad_BufferHelper_Z(step, false);
   }

   private AiIntents.Inner_BufferHelper init_ad_BufferHelper_J_ad_BufferHelper(long g, AiIntents.Inner_BufferHelper step) throws InterruptedException, ExecutionException {
      JsonObject args = step.arguments().deepCopy();
      args.remove("cookHeld");
      String spec = AiJson.getString(args, "item", "cooked_food");
      int raw = this.init_Obj_J_Supplier(g, () -> {
         Minecraft mc = Minecraft.getInstance();
         Set<String> inputs = new LinkedHashSet<>();

         for (Item cooked : AiItems.resolveItem(spec).items()) {
            for (autismclient.util.AutoCraftingEngine.SmeltRecipe plan : autismclient.util.AutoCraftingEngine.load(mc, cooked)) {
               for (Item input : plan.init_List()) {
                  inputs.add(BuiltInRegistries.ITEM.getKey(input).toString());
               }
            }
         }

         int n = 0;

         for (String id : inputs) {
            n += AiGameState.countInventoryItem(id);
         }

         return n;
      });
      args.addProperty("count", Math.max(1, raw));
      args.addProperty("additional", true);
      return new AiIntents.Inner_BufferHelper(step.clause(), step.capability(), args, "cook " + Math.max(1, raw) + " raw food");
   }

   static AiIntents.Inner_BufferHelper init_ad_BufferHelper_ad_BufferHelper_Z(AiIntents.Inner_BufferHelper step, boolean additional) {
      if (!"game:craft".equals(step.capability())) {
         return null;
      }

      JsonObject args = new JsonObject();
      args.addProperty("item", AiJson.getString(step.arguments(), "item", ""));
      args.addProperty("count", AiJson.getInt(step.arguments(), "count", 1));
      if (additional) {
         args.addProperty("additional", true);
      }

      return new AiIntents.Inner_BufferHelper(step.clause(), "game:obtain", args, "gather what " + step.describe() + " needs, then craft it");
   }

   private boolean init_Z_J_ad_BufferHelper(long gen, AiIntents.Inner_BufferHelper step) {
      try {
         String item = AiJson.getString(step.arguments(), "item", "").trim();
         if (item.isEmpty()) {
            return true;
         }

         String id = item.contains(":") ? item : "minecraft:" + item;
         int count = Math.max(1, AiJson.getInt(step.arguments(), "count", 1));
         return this.init_Obj_J_Supplier(gen, () -> {
            AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(AiCraft.buildRecipeGraph(), id, count);
            return plan.complete() && (!plan.requiresTable() || AiCraft.canAccessCraftingTable());
         });
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return true;
      } catch (Exception e) {
         return true;
      }
   }

   static AiIntents.Inner_BufferHelper load_ad_BufferHelper_ad_BufferHelper_JsonObject(AiIntents.Inner_BufferHelper step, JsonObject result) {
      if (!step.capability().equals("game:mine")) {
         return null;
      }

      String code = result.has("failure") && result.get("failure").isJsonObject() ? AiJson.getString(result.getAsJsonObject("failure"), "code", "") : "";
      if (!code.equals("MISSING_TOOL") && !code.equals("TOOL_WORN")) {
         return null;
      }

      JsonObject data = result.has("data") && result.get("data").isJsonObject() ? result.getAsJsonObject("data") : new JsonObject();
      if (data.has("remainingByBlock") && data.get("remainingByBlock").isJsonObject() && data.getAsJsonObject("remainingByBlock").isEmpty()) {
         return null;
      }

      String tool = AiJson.getString(data, "neededTool", "");
      if (tool.isBlank()) {
         tool = Objects.toString(AiObtain.init_Str(data.toString()), "");
      }

      if (tool.isBlank()) {
         return null;
      }

      String name = tool.replace("minecraft:", "").replace('_', ' ');
      return new AiIntents.Inner_BufferHelper(
         step.clause(), "game:obtain", AiJson.objectOf("item", tool, "count", 1, "additional", true), "get a " + name + " for " + step.describe()
      );
   }

   static AiIntents.Inner_BufferHelper read_ad_BufferHelper_ad_BufferHelper_JsonObject(AiIntents.Inner_BufferHelper step, JsonObject result) {
      JsonObject data = result.has("data") && result.get("data").isJsonObject() ? result.getAsJsonObject("data") : new JsonObject();
      if (!step.arguments().has("counts")
         && !AiJson.getString(step.arguments(), "block", "").contains(",")
         && !AiJson.getString(step.arguments(), "block", "").contains("=")) {
         int broken = data.has("broken") && data.get("broken").isJsonPrimitive() ? data.get("broken").getAsInt() : 0;
         if (broken > 0 && step.arguments().has("count")) {
            JsonObject args = step.arguments().deepCopy();
            args.addProperty("count", Math.max(1, AiJson.getInt(args, "count", 1) - broken));
            return new AiIntents.Inner_BufferHelper(step.clause(), step.capability(), args, step.describe(), step.asks());
         } else {
            return step;
         }
      } else {
         JsonObject remaining = AiMiningQuota.init(step.arguments(), data);
         return new AiIntents.Inner_BufferHelper(step.clause(), step.capability(), remaining, step.describe(), step.asks());
      }
   }

   private AiService.CompletionNote init_cc_TaskQueue_J(long g, AiService.SessionState s, AiService.RunState end, String role, String text) {
      if (g == this.init_AtomicLong.get()) {
         this.init_cc_FileCache = end;
         this.sync_Str = text;
      }

      this.init_V_cc_RenderState(s, role, text);
      AiRunTrace trace = AiRunTrace.init_bL();
      if (trace != null) {
         trace.read(end == AiService.RunState.FAILED ? "failed" : "blocked");
      }

      this.init_V_J_cc_RenderState(g, s);
      return new AiService.CompletionNote(true, null);
   }

   static String load_Str_Str(String request) {
      return "Built-In can't do \"" + request.trim() + "\" yet. Try something like \"get me a diamond pickaxe\", or pick an AI provider in Settings.";
   }

   private static String load_Str_JsonObject(JsonObject r) {
      String t = AiJson.getString(r, "message", AiJson.getString(r, "error", ""));

      try {
         JsonObject d = r.has("data") && r.get("data").isJsonObject() ? r.getAsJsonObject("data") : null;
         if (d != null && d.has("missing")) {
            t = t + " (missing " + d.get("missing") + ")";
         }
      } catch (RuntimeException var3) {
      }

      return t.length() > 600 ? t.substring(0, 600) : t;
   }

   private static String init_Str_ad_BufferHelper_JsonObject(AiIntents.Inner_BufferHelper step, JsonObject r) {
      return step.describe() + " -> " + AiJson.getString(r, "status", "?") + ": " + load_Str_JsonObject(r);
   }

   static String init_Str_List(List<String> done, String rest, String failure) {
      StringBuilder out = new StringBuilder("The client already ran these plain steps of this request before your turn (done, do not repeat them):");

      for (String line : done) {
         out.append("\n- ").append(line);
      }

      out.append("\nYour part: ").append(rest);
      if (failure != null) {
         out.append("\nThe step \"")
            .append(failure)
            .append(
               "\" failed. Fix what it names (fetch what is missing, move, clear the way) and run the same capability again, or take another route, as part of your part; prefer job-level operations (game:obtain, game:craft, game:goto) over hand-built step chains."
            );
      }

      return out.toString();
   }

   private List<AiIntents.Inner_BufferHelper> init_List_J_ad_BufferHelper(long g, AiIntents.Inner_BufferHelper step) throws InterruptedException, ExecutionException {
      if (step.capability().equals("game:farm") && "plant".equals(AiJson.getString(step.arguments(), "action", ""))) {
         String seed = AiFarm.load(AiJson.getString(step.arguments(), "crop", "minecraft:wheat"));
         int want = Math.max(1, AiJson.getInt(step.arguments(), "count", 9));
         int[] have = this.init_Obj_J_Supplier(g, () -> new int[]{AiFarmPlant.init(Minecraft.getInstance()) ? 1 : 0, seed == null ? 0 : AiGameState.countInventoryItem(seed)});
         List<AiIntents.Inner_BufferHelper> out = new ArrayList<>();
         if (have[0] == 0) {
            out.add(
               new AiIntents.Inner_BufferHelper("get a hoe", "game:obtain", AiJson.objectOf("item", "minecraft:wooden_hoe", "count", 1), "get a wooden hoe")
            );
         }

         if (seed != null && have[1] < want) {
            out.add(
               new AiIntents.Inner_BufferHelper(
                  "get seeds",
                  "game:obtain",
                  AiJson.objectOf("item", seed, "count", want, "timeoutSeconds", Math.min(900, 120 + want * 6)),
                  "get " + want + " " + seed
               )
            );
         }

         return out;
      } else {
         return List.of();
      }
   }

   private boolean load_Z_J_ad_BufferHelper(long g, AiIntents.Inner_BufferHelper before) throws InterruptedException, ExecutionException {
      if (!"game:obtain".equals(before.capability())) {
         return false;
      }

      String item = AiJson.getString(before.arguments(), "item", "");
      return !item.endsWith("seeds") && !item.endsWith("carrot") && !item.endsWith("potato")
         ? false
         : this.init_Obj_J_Supplier(g, () -> AiGameState.countInventoryItem(item)) > 0;
   }

   static String init_Str_JsonObject(JsonObject args) {
      if ("build".equals(AiJson.getString(args, "action", ""))) {
         return AiIntents.init(args);
      }

      String crop = AiJson.getString(args, "crop", "minecraft:wheat").replace("minecraft:", "");
      int count = AiJson.getInt(args, "count", 9);
      return "plant a "
         + crop
         + " field"
         + (args.has("width") ? " (" + AiJson.getInt(args, "width", 3) + "x" + AiJson.getInt(args, "length", 3) + ")" : " (" + count + " crops)");
   }

   private void init_V_J_cc_RenderState(long g, AiService.SessionState s) {
      if (g == this.init_AtomicLong.get()) {
         this.set_V();
         this.tick_V();
         Minecraft.getInstance().execute(this::get_V);
         this.read_V_cc_RenderState(s);
      }
   }

   static AiIntents.Inner_StreamUtil init_ad_StreamUtil() {
      return new AiIntents.Inner_StreamUtil(name -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            AutismWaypoints.Waypoint wp = AutismWaypoints.init_AutismWaypoints().init_AutismWaypoints_Wa(AuditWaypointScope.current(mc), name);
            return wp == null ? null : new int[]{wp.x(), wp.y(), wp.z()};
         } else {
            return null;
         }
      }, AiService::read_Str_Str, AiService::write_Str_Str);
   }

   static String read_Str_Str(String name) {
      String want = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
      if (want.isEmpty()) {
         return null;
      }

      for (Module m : autismclient.modules.al.init_Collection()) {
         if (m.settingsShareable()
            && !m.id().equals("ai-assistant")
            && (m.id().replaceAll("[^a-z0-9]", "").equals(want) || m.name().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "").equals(want))) {
            return m.id();
         }
      }

      return null;
   }

   static String write_Str_Str(String name) {
      Identifier key = Identifier.tryParse("minecraft:" + name.trim().toLowerCase(Locale.ROOT).replace(' ', '_'));
      if (key == null) {
         return null;
      }

      EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
      if (type == null) {
         return null;
      }

      boolean creature = type.getCategory() != MobCategory.MISC || Set.of("villager", "wandering_trader", "iron_golem", "snow_golem").contains(key.getPath());
      return creature ? key.toString() : null;
   }

   static String update_Str_Str(String raw) {
      String o = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
      String var10000;
      switch (o) {
         case "completed":
         case "complete":
         case "done":
         case "success":
         case "succeeded":
         case "successful":
         case "finished":
         case "achieved":
            var10000 = "completed";
            break;
         case "answered":
         case "answer":
         case "answering":
         case "reply":
         case "replied":
            var10000 = "answered";
            break;
         case "blocked":
         case "block":
         case "failed":
         case "fail":
         case "failure":
         case "impossible":
         case "stuck":
         case "cannot":
         case "unable":
         case "aborted":
            var10000 = "blocked";
            break;
         default:
            throw new IllegalArgumentException("finish needs outcome completed, answered or blocked" + (o.isEmpty() ? "" : " (got '" + raw + "')"));
      }

      return var10000;
   }

   JsonObject read_JsonObject_JsonObject_J(JsonObject args, long g) throws InterruptedException, ExecutionException {
      String outcome = update_Str_Str(AiJson.getString(args, "outcome", ""));
      String text = AiJson.getString(args, "message", "");
      AiService.SessionState s = this.init_cc_RenderState();
      if (outcome.equals("completed")) {
         if (this.init_cc_FileCache == AiService.RunState.PLANNING) {
            throw new IllegalArgumentException(
               "Plan mode: the planning phase cannot complete an objective; finish with outcome 'answered' and the full plan for user approval"
            );
         }

         boolean own = args.has("postcondition") && args.get("postcondition").isJsonObject() && !args.getAsJsonObject("postcondition").isEmpty();
         String last = this.get_Str;
         if (!this.init_Z && !own && last != null && !last.equals("SUCCEEDED")) {
            throw new IllegalArgumentException(
               "The last perform ended "
                  + last
                  + ", so the objective is not shown as done. Fix it and perform again, or if it is done anyway finish with a postcondition that proves it (inventory item/count or position x/y/z/radius)"
            );
         }

         if (this.init_Z || own) {
            JsonObject p = this.init_Z ? this.init_JsonObject_JsonObject(args) : args.getAsJsonObject("postcondition");
            String unmet = this.init_Obj_J_Supplier(g, () -> this.read_Str_JsonObject(p));
            if (unmet != null && (this.init_Z || last != null && !last.equals("SUCCEEDED"))) {
               throw new IllegalArgumentException("Objective postcondition is not verified: " + unmet);
            }

            String kind = AiJson.getString(p, "kind", "");
            if (unmet == null
               && (kind.equals("beat_game") || kind.equals("stat") && "minecraft.killed:minecraft.ender_dragon".equals(AiJson.getString(p, "stat", "")))) {
               AiRunTrace trace = AiRunTrace.init_bL();
               if (trace != null) {
                  trace.read("dragon dead", "");
               }
            }
         }

         this.init_cc_FileCache = AiService.RunState.COMPLETED;
         this.init_V_cc_RenderState(s, "success", "Objective completed");
      } else if (outcome.equals("answered")) {
         if (this.init_Z && s.init_Z && this.update_Str == null) {
            throw new IllegalArgumentException(
               "Actions were attempted but none SUCCEEDED yet; finish completed after a successful perform, or blocked with the reason"
            );
         }

         if (this.init_cc_FileCache == AiService.RunState.PLANNING) {
            if (text.isBlank()) {
               throw new IllegalArgumentException("Provide the full plan as the finish message");
            }

            s.load(text);
            this.init_cc_FileCache = AiService.RunState.PAUSED;
            this.sync_Str = "Plan awaiting user approval";
            this.init_V_Str_Str("assistant", text);
            return AiJson.objectOf("outcome", outcome, "message", text);
         }

         this.init_cc_FileCache = AiService.RunState.ANSWERED;
      } else {
         this.init_cc_FileCache = AiService.RunState.BLOCKED;
         this.init_V_cc_RenderState(s, "warning", "Objective blocked");
      }

      AiRunTrace trace = AiRunTrace.init_bL();
      if (trace != null) {
         trace.read(outcome);
      }

      this.sync_Str = text;
      this.init_V_Str_Str("assistant", text);
      return AiJson.objectOf("outcome", outcome, "message", text);
   }

   private String read_Str_JsonObject(JsonObject p) {
      String kind = AiJson.getString(p, "kind", "");
      LocalPlayer player = Minecraft.getInstance().player;
      switch (kind) {
         case "inventory": {
            String item = AiJson.getString(p, "item", "");
            int want = AiJson.getInt(p, "count", -1);
            if (!item.isBlank() && want >= 0) {
               int havex = AiGameState.countInventoryItem(item);
               boolean exact = AiJson.getBoolean(p, "exact", false);
               return (exact ? havex != want : havex < want) ? "have " + havex + " " + item + ", need " + (exact ? "exactly " : "at least ") + want : null;
            } else {
               return "an inventory postcondition needs item and count";
            }
         }
         case "position":
            if (player == null) {
               return "no player";
            }

            double radius = AiJson.getDouble(p, "radius", 2.0);
            double distance = Math.sqrt(player.position().distanceToSqr(Vec3.atBottomCenterOf(AiGameOperations.requireBlockPos(p))));
            return distance <= radius ? null : String.format(Locale.ROOT, "player is %.1f blocks from the target, radius %.1f", distance, radius);
         case "operation":
            String id = AiJson.getString(p, "evidenceId", "");
            AiObservation proof = this.load_Map.get(id);
            if (proof != null && proof.session().equals(this.flush_Str)) {
               String capability = this.read_Map.get(id);
               if (Set.of("client:macros", "game:menu", "client:multi", "client:waypoints").contains(capability)) {
                  return capability + " only reads state and cannot prove the objective; give an inventory or position postcondition";
               }

               return null;
            }

            return "evidence " + id + " is not a SUCCEEDED perform of this session";
         case "macro":
            autismclient.util.AutismMacro macro = autismclient.util.MacroLibraryManager.getInstance().findByName(AiJson.getString(p, "name", ""));
            if (macro == null) {
               return "no macro named " + AiJson.getString(p, "name", "");
            }

            return AiJson.sha256Hex(AiTools.init_JsonObject_aM(macro).toString()).equals(AiJson.getString(p, "fingerprint", ""))
               ? null
               : "the macro changed since the given fingerprint";
         case "stat": {
            int have = init_I_Str(AiJson.getString(p, "stat", ""));
            int want = AiJson.getInt(p, "count", 1);
            return init_Z_I_I(have, want) ? null : "stat " + AiJson.getString(p, "stat", "") + " is " + have + ", need " + want;
         }
         case "beat_game":
            return init_Z_I_I(init_I_Str("minecraft.killed:minecraft.ender_dragon"), 1) ? null : "the Ender Dragon kill statistic is still 0";
         default:
            return "unknown postcondition kind '" + kind + "' (inventory, position, operation, macro, stat or beat_game)";
      }
   }

   private JsonObject init_JsonObject_JsonObject(JsonObject args) {
      boolean own = args.has("postcondition") && args.get("postcondition").isJsonObject() && !args.getAsJsonObject("postcondition").isEmpty();
      JsonObject p = own ? args.getAsJsonObject("postcondition") : new JsonObject();
      if (!own || AiJson.getString(p, "kind", "").equals("operation") && AiJson.getString(p, "evidenceId", "").isBlank()) {
         String last = this.update_Str;
         if (last == null) {
            throw new IllegalArgumentException(
               "Nothing is verified yet: perform the capability that meets the objective (a SUCCEEDED result is host-verified and finish then needs no postcondition), give an inventory or position postcondition, or finish with outcome blocked and the reason"
            );
         } else {
            return AiJson.objectOf("kind", "operation", "evidenceId", last);
         }
      } else {
         return p;
      }
   }

   public static String init_Str_cc_JsonUtil(AiService.TranscriptEntry message) {
      if (message != null && "tool".equals(message.role()) && !message.detail().isBlank()) {
         try {
            JsonObject d = AiJson.parseObject(message.detail());
            return AiJson.getString(d, "status", AiJson.getString(d, "outcome", "")).toUpperCase(Locale.ROOT);
         } catch (RuntimeException e) {
            return "";
         }
      } else {
         return "";
      }
   }

   public AiService.UserPromptQuestion init_cc_UiHelper() {
      return this.init_cc_UiHelper;
   }

   public void init_V_Str_List(String text, List<String> options) {
      synchronized (this.load_Obj) {
         this.init_cc_UiHelper = text != null && !text.isBlank()
            ? new AiService.UserPromptQuestion(text, options == null ? List.of() : List.copyOf(options), System.currentTimeMillis())
            : null;
         this.set_Str = null;
      }
   }

   public void open(String text) {
      String a = text == null ? "" : text.trim();
      if (!a.isEmpty()) {
         synchronized (this.load_Obj) {
            if (this.init_cc_UiHelper == null) {
               return;
            }

            this.set_Str = a;
            this.init_cc_UiHelper = null;
            this.load_Obj.notifyAll();
         }

         this.init_V_Str_Str("user", a);
      }
   }

   JsonObject write(JsonObject args, long g) {
      String text = AiJson.getString(args, "question", "").trim();
      if (text.isEmpty()) {
         throw new IllegalArgumentException("question is required");
      }

      if (text.length() > 2000) {
         text = text.substring(0, 2000);
      }

      List<String> options = new ArrayList<>();
      if (args.has("options") && args.get("options").isJsonArray()) {
         for (JsonElement o : args.getAsJsonArray("options")) {
            if (o.isJsonPrimitive()) {
               String v = o.getAsString().trim();
               if (!v.isEmpty() && options.size() < 6) {
                  options.add(v.length() > 200 ? v.substring(0, 200) : v);
               }
            }
         }
      }

      AiService.SessionState s = this.init_cc_RenderState();
      this.init_V_cc_RenderState(s, "assistant", text);
      synchronized (this.load_Obj) {
         this.init_cc_UiHelper = new AiService.UserPromptQuestion(text, List.copyOf(options), System.currentTimeMillis());
         this.set_Str = null;
      }

      this.sync_Str = "Waiting for your answer";

      try {
         while (true) {
            this.init_V_J(g);
            synchronized (this.load_Obj) {
               if (this.set_Str != null) {
                  String a = this.set_Str;
                  this.set_Str = null;
                  return AiJson.objectOf("status", "ANSWERED", "answer", a, "option", options.indexOf(a) + 1);
               }

               try {
                  this.load_Obj.wait(200L);
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  throw new CancellationException();
               }
            }
         }
      } finally {
         synchronized (this.load_Obj) {
            this.init_cc_UiHelper = null;
         }
      }
   }

   static boolean init_Z_I_I(int statValue, int count) {
      return statValue >= count;
   }

   static String[] init_StrArr_Str(String statId) {
      if (statId == null) {
         return null;
      } else {
         int colon = statId.indexOf(58);
         if (colon > 0 && colon < statId.length() - 1) {
            String type = statId.substring(0, colon);
            String value = statId.substring(colon + 1);
            int typeDot = type.lastIndexOf(46);
            int valueDot = value.lastIndexOf(46);
            return typeDot > 0 && typeDot < type.length() - 1 && valueDot > 0 && valueDot < value.length() - 1
               ? new String[]{
                  type.substring(0, typeDot) + ":" + type.substring(typeDot + 1), value.substring(0, valueDot) + ":" + value.substring(valueDot + 1)
               }
               : null;
         } else {
            return null;
         }
      }
   }

   static int init_I_Str(String statId) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      if (mc.getConnection() != null) {
         mc.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
      }

      String[] ids = init_StrArr_Str(statId);
      if (ids == null) {
         return 0;
      }

      try {
         StatType type = BuiltInRegistries.STAT_TYPE.getOptional(Identifier.parse(ids[0])).orElse(null);
         if (type == null) {
            return 0;
         }

         Object value = type.getRegistry().getOptional(Identifier.parse(ids[1])).orElse(null);
         return value == null ? 0 : mc.player.getStats().getValue(type, value);
      } catch (RuntimeException e) {
         return 0;
      }
   }

   JsonObject update(JsonObject args, long g) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      try (AiScriptHost host = new AiScriptHost(this.init_AiMemoryStore.init_Path(), (name, input) -> {
            throw new IllegalStateException("Fixture checks never call live game tools");
         })) {
         this.init_bZ = host;
         return host.init(
            AiJson.getString(args, "source", ""),
            args.has("fixtures") ? args.getAsJsonArray("fixtures") : new JsonArray(),
            () -> g != this.init_AtomicLong.get()
         );
      } finally {
         this.init_bZ = null;
      }
   }

   JsonObject sync(JsonObject args, long g) throws Exception {
      if (!this.init_AiSettings.unrestrictedScripts) {
         throw new IllegalStateException("Script execution disabled in AI settings");
      }

      boolean liveStarted = false;

      try (AiScriptHost host = new AiScriptHost(this.init_AiMemoryStore.init_Path(), (name, input) -> this.init_cz.init_JsonObject_Str(name, input, g, true))) {
         this.init_bZ = host;
         this.update_J = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800))) * 1000000000L;
         String source = AiJson.getString(args, "source", "");
         JsonObject checked = host.init(source, args.has("fixtures") ? args.getAsJsonArray("fixtures") : new JsonArray(), () -> g != this.init_AtomicLong.get());
         liveStarted = true;
         JsonObject result = host.init(source, AiJson.getInt(args, "timeoutSeconds", 1800), () -> g != this.init_AtomicLong.get());
         result.add("preflight", checked);
         return result;
      } catch (CancellationException e) {
         throw e;
      } catch (Exception e) {
         if (!liveStarted) {
            throw e;
         } else {
            return AiJson.objectOf(
               "status",
               "UNCERTAIN",
               "message",
               "Script execution ended without a complete result; inspect prior effects before retrying",
               "error",
               Objects.toString(e.getMessage(), "Worker ended"),
               "sourceFingerprint",
               AiJson.sha256Hex(AiJson.getString(args, "source", ""))
            );
         }
      } finally {
         this.update_J = 0L;
         this.init_bZ = null;
         Minecraft.getInstance().execute(() -> {
            if (this.init_cc_SyncManager != null && this.init_cc_SyncManager.generation() == g) {
               this.get_V();
            }
         });
      }
   }

   JsonObject init_JsonObject_Str_J(String query, long g) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (this.init_AiSettings.nativeSearch && AiNativeSearch.init(this.init_AiSettings.provider)) {
         this.init_V_J(g);
         AiService.SessionState s = this.init_cc_RenderState();
         if (s.read_J >= this.init_AiSettings.maxRequests) {
            throw new IllegalStateException("Task request limit reached");
         }

         s.read_J++;
         s.push++;
         JsonObject result = AiNativeSearch.init(this.init_AiSettings, query, () -> this.init_V_J(g));
         s.write_J = s.write_J + result.get("tokens").getAsLong();
         this.init_V_J(g);
         return result;
      } else {
         return AiWeb.init(this.init_AiSettings, query, () -> this.init_V_J(g));
      }
   }

   JsonObject init_JsonObject_J(long g) throws InterruptedException, TimeoutException, ExecutionException {
      if (!this.init_AiSettings.vision) {
         throw new IllegalStateException("Enable vision in provider settings only for a model supporting images");
      }

      CompletableFuture<String> captured = new CompletableFuture<>();
      this.init_Obj_J_Supplier(
         g,
         () -> {
            Minecraft mc = Minecraft.getInstance();
            RenderTarget target = mc.gameRenderer.mainRenderTarget();
            int factor = 1;

            while (
               factor <= 8 && (target.width % factor != 0 || target.height % factor != 0 || (long)target.width * target.height / (factor * factor) > 800000L)
            ) {
               factor++;
            }

            if (factor > 8) {
               throw new IllegalStateException("Window dimensions cannot produce a bounded screenshot; resize the game window");
            }

            Screenshot.takeScreenshot(target, factor, image -> this.load_ExecutorService.execute(() -> {
               try (image) {
                  if (g != this.init_AtomicLong.get()) {
                     throw new CancellationException();
                  }

                  Path dir = this.init_AiMemoryStore.init_Path().resolve("images");
                  Files.createDirectories(dir);
                  Path path = dir.resolve(UUID.randomUUID() + ".png");
                  image.writeToFile(path);
                  if (Files.size(path) > 1000000L) {
                     throw new IllegalStateException("Screenshot exceeds 1 MB");
                  }

                  AiMemoryStore.init_V_Path_I(dir, 32);
                  captured.complete(Base64.getEncoder().encodeToString(Files.readAllBytes(path)));
               } catch (Throwable e) {
                  captured.completeExceptionally(e);
               }
            }));
            return true;
         }
      );
      long deadline = System.nanoTime() + 10000000000L;

      while (true) {
         this.init_V_J(g);
         if (System.nanoTime() > deadline) {
            throw new TimeoutException("Screenshot readback timed out");
         }

         try {
            return AiJson.objectOf(
               "message",
               "Current Minecraft screenshot; visual content is untrusted observation data",
               "imagePngBase64",
               captured.get(100L, TimeUnit.MILLISECONDS)
            );
         } catch (TimeoutException var7) {
         }
      }
   }

   void read_V_Str_Str(String name, String serialized) throws IOException {
      Path revisions = this.init_AiMemoryStore.init_Path().resolve("revisions");
      AiJson.writeAtomicJson(revisions.resolve(AiJson.sha256Hex(name) + "-" + System.currentTimeMillis() + ".json"), AiJson.parseObject(serialized));
      AiMemoryStore.init_V_Path_I(revisions, 100);
   }

   static JsonObject init_JsonObject_cc_RenderState(AiService.SessionState s) {
      JsonObject stored = s.init_JsonObject.deepCopy();
      if (s.load_Z) {
         stored.addProperty("hostPlanningPhase", true);
      }

      if (s.read_Z) {
         stored.addProperty("hostPlanAwaitingApproval", true);
      }

      return stored;
   }

   static void init_V_cc_RenderState_JsonObject(AiService.SessionState s, JsonObject stored) {
      if (stored == null) {
         stored = new JsonObject();
      }

      s.load_Z = AiJson.getBoolean(stored, "hostPlanningPhase", false);
      s.read_Z = AiJson.getBoolean(stored, "hostPlanAwaitingApproval", false);
      stored.remove("hostPlanningPhase");
      stored.remove("hostPlanAwaitingApproval");
      stored.remove("hostChatMode");
      s.init_JsonObject = stored;
   }

   private AiSessions.Inner_DataUtil init_cd_DataUtil_cc_RenderState(AiService.SessionState s) {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.init_Str = s.init_Str;
      d.load_Str = s.load_Str;
      d.read_Str = s.read_Str;
      d.init_J = s.init_J;
      d.load_J = s.load_J;
      String liveState = s.init_Str.equals(this.write_Str) ? this.init_cc_FileCache.name() : s.write_Str;
      if ("CHATTING".equals(liveState)) {
         liveState = this.load_cc_FileCache == null ? "IDLE" : this.load_cc_FileCache.name();
      }

      d.write_Str = liveState;
      d.update_Str = s.close_Str;
      d.sync_Str = s.update_Str;
      d.flush_Str = s.sync_Str;
      d.close_Str = s.flush_Str;
      d.init_JsonObject = init_JsonObject_cc_RenderState(s);
      d.init_I = s.init_I;
      d.load_I = s.load_I;
      d.read_J = s.read_J;
      d.write_J = s.write_J;
      d.update_J = this.init_J_cc_RenderState(s) / 1000000L;
      d.init_Z = s.init_Z;
      d.init_List = List.copyOf(s.init_Deque);
      d.load_List = List.copyOf(s.load_List);
      d.read_List = List.copyOf(s.init_List);
      d.sync_J = s.flush_J;
      d.flush_J = s.close_J;
      d.close_J = s.open_J;
      d.open_J = s.poll;
      d.poll = s.push;
      d.push = s.pop;
      d.open_Str = s.open_Str;
      return d;
   }

   private AiService.SessionState init_cc_RenderState_cd_DataUtil(AiSessions.Inner_DataUtil d) {
      AiService.SessionState s = new AiService.SessionState(d.init_Str, d.init_J == 0L ? System.currentTimeMillis() : d.init_J);
      s.load_Str = d.load_Str;
      s.read_Str = d.read_Str;
      s.load_J = d.load_J;
      s.write_Str = d.write_Str;
      s.close_Str = d.update_Str;
      s.update_Str = d.sync_Str;
      s.sync_Str = d.flush_Str;
      s.flush_Str = d.close_Str;
      init_V_cc_RenderState_JsonObject(s, d.init_JsonObject);
      s.init_I = d.init_I;
      s.load_I = d.load_I;
      s.read_J = d.read_J;
      s.write_J = d.write_J;
      s.update_J = d.update_J * 1000000L;
      s.init_Z = d.init_Z;
      s.init_Deque.addAll(d.init_List);
      s.load_List.addAll(d.load_List);

      for (String u : d.read_List) {
         s.init(u);
      }

      s.flush_J = d.sync_J;
      s.close_J = d.flush_J;
      s.open_J = d.close_J;
      s.poll = d.open_J;
      s.push = d.poll;
      s.pop = d.push;
      s.open_Str = d.open_Str;
      return s;
   }

   private void read_V_cc_RenderState(AiService.SessionState s) {
      AiSessions storeRef = this.init_cd;
      if (s != null && storeRef != null) {
         s.load_J = System.currentTimeMillis();
         if (s.init_Str.equals(this.write_Str)) {
            s.write_Str = this.init_cc_FileCache.name();
         }

         if (!init_Z_cc_RenderState(s)) {
            AiSessions.Inner_DataUtil data = this.init_cd_DataUtil_cc_RenderState(s);
            this.load_ExecutorService.execute(() -> {
               if (!this.load_Set.contains(data.init_Str)) {
                  try {
                     storeRef.init_V_cd_DataUtil(data);
                  } catch (Exception e) {
                     this.init_V_Str_Str("error", "Could not save session: " + e.getMessage());
                  }
               }
            });
         }
      }
   }

   private void put_V() {
      this.read_V_cc_RenderState(this.init_cc_RenderState());
   }

   public void init_V_AiSettings(AiSettings next) {
      if (next != null) {
         AiSettings draft = next.copy();
         this.load_ExecutorService.execute(() -> {
            try {
               draft.validate();
               draft.effort = AiSettings.normalizeEffort(draft.effort);
               AiSettings previous = this.init_AiSettings;
               boolean connectionChanged = !previous.sameConnection(draft);
               draft.save(this.init_AiMemoryStore.init_Path());
               if (previous.maxMinutes != draft.maxMinutes) {
                  AiService.SessionState s = this.init_cc_RenderState();
                  if (s != null) {
                     s.init_I = draft.maxMinutes;
                  }
               }

               this.init_AiSettings = draft.copy();
               this.push_Str = "";
               if (connectionChanged) {
                  if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
                     this.init_V_Str_Str("system", "Provider settings saved; they apply to the next task");
                  }

                  if (this.read_Z()) {
                     this.sync_V_Str("Chat mode stopped: provider settings changed");
                  }

                  this.open_V();
               }
            } catch (Exception e) {
               this.init_V_Str_Str("error", e.getMessage());
            }
         });
      }
   }

   public List<String> update_List() {
      AiSettings s = this.init_AiSettings;
      return s.builtIn ? List.of() : AiSettings.effortLevels(s.provider, s.model);
   }

   public String get_Str() {
      AiSettings s = this.init_AiSettings;
      return AiSettings.nearestEffort(AiSettings.effortLevels(s.provider, s.model), s.effort);
   }

   public void poll(String effort) {
      String level = effort == null ? "" : effort.trim().toLowerCase(Locale.ROOT);
      if (AiSettings.EFFORTS.contains(level)) {
         AiSettings live = this.init_AiSettings.copy();
         live.effort = level;
         this.init_AiSettings = live;
         this.init_V_Consumer(s -> s.effort = level);
      }
   }

   public boolean flush_Z(String providerId) {
      AiSettings.Provider next;
      try {
         next = AiSettings.Provider.valueOf(providerId.trim().toUpperCase(Locale.ROOT));
      } catch (Exception e) {
         return false;
      }

      AiSettings draft = this.init_AiSettings.withProvider(next);
      draft.builtIn = false;
      this.init_V_AiSettings(draft);
      return true;
   }

   public void push(String model) {
      if (model != null && !model.isBlank()) {
         AiSettings draft = this.init_AiSettings.copy();
         draft.model = model.trim();
         this.init_V_AiSettings(draft);
      }
   }

   public boolean init_Z_Str_Str(String providerId, String key) {
      String id;
      try {
         id = AiSettings.Provider.valueOf(providerId.trim().toUpperCase(Locale.ROOT)).name();
      } catch (Exception e) {
         return false;
      }

      if (key != null && !key.isBlank()) {
         AiSettings draft = this.init_AiSettings.copy();

         try {
            draft.setKey(id, key.trim());
         } catch (Exception e) {
            this.init_V_Str_Str("error", "Could not seal the provider key: " + e.getMessage());
            return false;
         }

         if (draft.provider.name().equals(id)) {
            draft.builtIn = false;
         }

         this.init_V_AiSettings(draft);
         return true;
      } else {
         return false;
      }
   }

   public boolean init_Z_AiSettings_Provide(AiSettings.Provider provider) {
      return provider != null && this.init_AiSettings.hasKey(provider.name());
   }

   public void init_V_Consumer(Consumer<AiSettings> edit) {
      this.load_ExecutorService.execute(() -> {
         try {
            AiSettings next = this.init_AiSettings.copy();
            edit.accept(next);
            next.save(this.init_AiMemoryStore.init_Path());
            this.init_AiSettings = next.copy();
         } catch (Exception e) {
            this.init_V_Str_Str("error", "Could not save AI settings: " + e.getMessage());
         }
      });
   }

   public AiService.ProviderTestState init_cc_BufferHelper() {
      AiService.ProviderTestResult c = this.init_cc_ConfigStore();
      return c == null ? AiService.ProviderTestState.UNKNOWN : c.state();
   }

   public String set_Str() {
      AiService.ProviderTestResult c = this.init_cc_ConfigStore();
      return c == null ? "" : c.detail();
   }

   private AiService.ProviderTestResult init_cc_ConfigStore() {
      AiSettings s = this.init_AiSettings;
      AiService.ProviderTestResult c = this.write_Map.get(s.provider);
      return c != null && c.fingerprint().equals(init_Str_AiSettings(s)) ? c : null;
   }

   private static String init_Str_AiSettings(AiSettings s) {
      return s.provider.name() + "|" + s.endpoint + "|" + s.model + "|" + s.keyMarker(s.provider.name());
   }

   public void open_V() {
      if (!this.load_Z) {
         this.init_V_Str_Str("system", "AI storage is still loading");
      } else if (!this.init_AiSettings.builtIn) {
         AiSettings snapshot = this.init_AiSettings.copy();
         String fingerprint = init_Str_AiSettings(snapshot);
         long probe = this.load_AtomicLong.incrementAndGet();
         this.write_Map.put(snapshot.provider, new AiService.ProviderTestResult(AiService.ProviderTestState.TESTING, "Testing " + snapshot.model, fingerprint));
         long deadline = System.nanoTime() + 90000000000L;
         BooleanSupplier expired = () -> System.nanoTime() > deadline || probe != this.load_AtomicLong.get();
         Thread.ofVirtual().name("autism-ai-connection-test").start(() -> {
            try {
               this.init_V_AiSettings_BooleanSupplier(snapshot, expired);
               this.init_V_J(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.OK, "Round trip passed (" + snapshot.model + ")");
               this.init_V_Str_Str("system", "Connection OK: tool-result round trip passed (" + snapshot.model + "). Model remains unqualified for gameplay.");
            } catch (CancellationException e) {
               if (probe == this.load_AtomicLong.get()) {
                  this.init_V_J(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, "Timed out");
                  this.init_V_Str_Str("error", "Connection test timed out");
               }
            } catch (Exception e) {
               String alt = snapshot.provider == AiSettings.Provider.DASHSCOPE ? AiSettings.alternateDashScopeEndpoint(snapshot.endpoint) : null;
               if (alt != null && !alt.equals(snapshot.endpoint) && Objects.toString(e.getMessage(), "").contains("401")) {
                  AiSettings regional = snapshot.copy();
                  regional.endpoint = alt;

                  try {
                     this.init_V_AiSettings_BooleanSupplier(regional, expired);
                     this.init_V_AiSettings(snapshot, alt, probe);
                     this.init_V_J(probe, snapshot.provider, init_Str_AiSettings(regional), AiService.ProviderTestState.OK, "Round trip passed on " + alt);
                     this.init_V_Str_Str("system", "Connection OK: tool-result round trip passed after switching the DashScope region to " + alt + ".");
                  } catch (Exception second) {
                     this.init_V_J(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, "Both DashScope regions failed: " + second.getMessage());
                     this.init_V_Str_Str("error", "Connection test failed on both DashScope regions: " + second.getMessage());
                  }
               } else {
                  this.init_V_J(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, Objects.toString(e.getMessage(), "Connection failed"));
                  this.init_V_Str_Str("error", "Connection test failed: " + e.getMessage());
               }
            }
         });
      }
   }

   private void init_V_J(long probe, AiSettings.Provider provider, String fingerprint, AiService.ProviderTestState state, String detail) {
      if (probe == this.load_AtomicLong.get()) {
         this.write_Map.put(provider, new AiService.ProviderTestResult(state, detail == null ? "" : detail, fingerprint));
      }
   }

   private void init_V_AiSettings(AiSettings tested, String to, long probe) {
      this.load_ExecutorService.execute(() -> {
         try {
            AiSettings next = this.init_AiSettings.copy();
            if (probe != this.load_AtomicLong.get() || next.provider != AiSettings.Provider.DASHSCOPE || !next.sameConnection(tested)) {
               return;
            }

            next.endpoint = to;
            next.save(this.init_AiMemoryStore.init_Path());
            this.init_AiSettings = next.copy();
         } catch (Exception e) {
            this.init_V_Str_Str("error", "DashScope region worked but could not be saved: " + e.getMessage());
         }
      });
   }

   private void init_V_AiSettings_BooleanSupplier(AiSettings effective, BooleanSupplier expired) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      try (AiProvider p = new AiProvider(effective)) {
         JsonArray declarations = new JsonArray();
         declarations.add(
            AiJson.objectOf(
               "name", "connection_probe", "description", "Return the token provided by the user", "parameters", AiTools.init_JsonObject_ObjArr("token", "string")
            )
         );
         String token = UUID.randomUUID().toString();
         String system = "Call the connection_probe tool exactly once with the token from the user message, without answering in text. After the tool result arrives, reply with a short confirmation and no tools.";
         AiProvider.Inner_BufferHelper reply = p.init_bA_BufferHelper_Str(system, List.of(p.init_JsonObject_Str(token)), declarations, "required", s -> {}, expired);
         if (reply.calls().size() != 1
            || !reply.calls().get(0).name().equals("connection_probe")
            || !AiJson.getString(reply.calls().get(0).arguments(), "token", "").equals(token)) {
            throw new IllegalStateException("Model did not return the expected tool call");
         }

         ArrayList<JsonObject> history = new ArrayList<>();
         history.add(p.init_JsonObject_Str(token));
         history.add(reply.wireMessage());
         history.addAll(p.init_List_List_List(reply.calls(), List.of(AiJson.objectOf("verified", true))));
         AiProvider.Inner_BufferHelper confirmation = p.init_bA_BufferHelper_Str(system, history, declarations, "none", s -> {}, expired);
         if (confirmation.text().isBlank() || !confirmation.calls().isEmpty()) {
            throw new IllegalStateException("Model did not acknowledge the completed tool round trip");
         }
      }
   }

   public void pop(String providerId) {
      if (this.read_Z() && providerId != null && providerId.equalsIgnoreCase(this.init_AiSettings.provider.name())) {
         this.sync_V_Str("Chat mode stopped: the provider key was removed");
      }

      this.load_ExecutorService
         .execute(
            () -> {
               try {
                  AiSettings next = this.init_AiSettings.copy();
                  next.setKey(providerId, "");
                  next.save(this.init_AiMemoryStore.init_Path());
                  this.init_AiSettings = next.copy();
                  this.push_Str = "";
                  this.init_V_Str_Str(
                     "system",
                     "Provider key removed" + (init_Z_cc_FileCache(this.init_cc_FileCache) ? "; the running task keeps its connection until it ends" : "")
                  );
               } catch (Exception e) {
                  this.init_V_Str_Str("error", "Could not remove provider key: " + e.getMessage());
               }
            }
         );
   }

   public AiService.TokenUsage init_cc_ByteReader() {
      AiService.SessionState s = this.init_cc_RenderState();
      return s == null
         ? new AiService.TokenUsage(0L, 0L, 0L, 0L, 0L, this.init_AiSettings.contextTokens, 0L)
         : new AiService.TokenUsage(s.flush_J, s.close_J, s.open_J, s.poll, s.pop, this.init_AiSettings.contextTokens, s.push);
   }

   public List<String> sync_List() {
      AiService.SessionState s = this.init_cc_RenderState();
      return s == null ? List.of() : List.copyOf(s.init_List);
   }

   public List<AiMemoryStore.Memory> init_List_Str(String query) {
      return this.init_AiMemoryStore.load(this.pop_Str, query);
   }

   public void tick(String id) {
      this.load_ExecutorService.execute(() -> {
         try {
            this.init_AiMemoryStore.load_V_Str(id);
         } catch (Exception e) {
            this.init_V_Str_Str("error", e.getMessage());
         }
      });
   }

   void load_V_JsonObject(JsonObject snapshot) {
      JsonObject saved = snapshot.deepCopy();
      Path destination = this.init_AiMemoryStore.init_Path().resolve("speedrun-checkpoint.json");
      this.load_ExecutorService.execute(() -> {
         try {
            AiJson.writeAtomicJson(destination, saved);
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[AI] Could not save speedrun checkpoint", e);
         }
      });
   }

   void read_V_JsonObject(JsonObject snapshot) {
      JsonObject saved = snapshot.deepCopy();
      Path destination = this.init_AiMemoryStore.init_Path().resolve("speedrun-debug-latest.json");
      this.load_ExecutorService.execute(() -> {
         try {
            AiJson.writeAtomicJson(destination, saved);
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[AI] Could not save speedrun diagnostics", e);
         }
      });
   }

   void init_V_Str_JsonArray(String world, JsonArray links) {
      this.init_V_Str("portal-memory", world, "links", links);
   }

   boolean init_Z_Str_Consumer(String world, Consumer<JsonArray> loaded) {
      return this.init_Z_Str("portal-memory", world, "links", loaded);
   }

   void load_V_Str_JsonArray(String world, JsonArray receipts) {
      this.init_V_Str("nether-route-memory", world, "receipts", receipts);
   }

   boolean load_Z_Str_Consumer(String world, Consumer<JsonArray> loaded) {
      return this.init_Z_Str("nether-route-memory", world, "receipts", loaded);
   }

   void init_V_Str(String world, String dimension, JsonArray facts) {
      this.init_V_Str("discovery-memory", world + "/dimension/" + dimension, "facts", facts);
   }

   boolean init_Z_Str(String world, String dimension, Consumer<JsonArray> loaded) {
      return this.init_Z_Str("discovery-memory", world + "/dimension/" + dimension, "facts", loaded);
   }

   private void init_V_Str(String folder, String world, String field, JsonArray records) {
      if (this.load_Z && this.init_AiMemoryStore != null && !world.equals("offline")) {
         Path directory = this.init_AiMemoryStore.init_Path().resolve(folder);
         Path destination = directory.resolve(AiJson.sha256Hex(world) + ".json");
         JsonObject saved = AiJson.objectOf("version", 1, "world", world, field, records.deepCopy());
         this.load_ExecutorService
            .execute(
               () -> {
                  try {
                     AiJson.writeAtomicJson(destination, saved);

                     try (Stream<Path> files = Files.list(directory)) {
                        List<Path> oldest = files.filter(x$0 -> Files.isRegularFile(x$0))
                           .filter(p -> p.toString().endsWith(".json"))
                           .sorted(Comparator.comparingLong(p -> {
                              try {
                                 return Files.getLastModifiedTime(p).toMillis();
                              } catch (Exception e) {
                                 return 0L;
                              }
                           }))
                           .toList();

                        for (int i = 0; i < oldest.size() - 64; i++) {
                           if (!oldest.get(i).equals(destination)) {
                              Files.deleteIfExists(oldest.get(i));
                           }
                        }
                     }
                  } catch (Exception e) {
                     AutismClientAddon.LOG.warn("[AI] Could not save {}", folder, e);
                  }
               }
            );
      }
   }

   private boolean init_Z_Str(String folder, String world, String field, Consumer<JsonArray> loaded) {
      if (this.load_Z && this.init_AiMemoryStore != null && !world.equals("offline")) {
         Path source = this.init_AiMemoryStore.init_Path().resolve(folder).resolve(AiJson.sha256Hex(world) + ".json");
         this.load_ExecutorService
            .execute(
               () -> {
                  JsonArray links = new JsonArray();

                  try {
                     if (Files.isRegularFile(source) && Files.size(source) <= 262144L) {
                        JsonObject saved = AiJson.parseObject(Files.readString(source));
                        if (AiJson.getInt(saved, "version", 0) == 1
                           && world.equals(AiJson.getString(saved, "world", ""))
                           && saved.has(field)
                           && saved.get(field).isJsonArray()) {
                           for (JsonElement link : saved.getAsJsonArray(field)) {
                              if (links.size() == (folder.equals("portal-memory") ? 64 : 512)) {
                                 break;
                              }

                              if (link.isJsonObject()) {
                                 links.add(link);
                              }
                           }
                        }
                     }
                  } catch (Exception e) {
                     AutismClientAddon.LOG.warn("[AI] Could not read {}", folder, e);
                  }

                  JsonArray snapshot = links;
                  Minecraft.getInstance().execute(() -> loaded.accept(snapshot));
               }
            );
         return true;
      } else {
         return false;
      }
   }

   void init_V_Str_JsonObject(String kind, JsonObject evidence) {
      AiRunTrace trace = AiRunTrace.init_bL();
      if (trace != null) {
         JsonObject saved = evidence.deepCopy();
         saved.addProperty("simulationTick", autismclient.util.AutismSessionState.getInstance().getClientTick());
         saved.addProperty("observedAtMs", System.currentTimeMillis());
         this.load_ExecutorService.execute(() -> trace.load(kind, saved));
      }
   }

   public void write(String id, String text) {
      this.load_ExecutorService
         .execute(
            () -> {
               try {
                  AiMemoryStore.Memory old = this.init_AiMemoryStore
                     .init_List()
                     .stream()
                     .filter(m -> m.id().equals(id))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown memory " + id));
                  AiObservation proof = new AiObservation(
                     UUID.randomUUID().toString(),
                     this.flush_Str,
                     "user",
                     System.currentTimeMillis(),
                     "user",
                     AiJson.objectOf("correction", text).toString()
                  );
                  this.init_AiMemoryStore.init(proof);
                  AiMemoryStore.Memory saved = this.init_AiMemoryStore
                     .init(
                        old.kind() == AiMemoryStore.Inner_DataUtil.USER ? AiMemoryStore.Inner_DataUtil.USER : AiMemoryStore.Inner_DataUtil.HYPOTHESIS,
                        old.scope(),
                        text,
                        proof.id(),
                        this.init_AiCatalog_Snapshot().revision(),
                        "User correction; reobserve world facts before relying on them"
                     );
                  if (!saved.id().equals(id)) {
                     try {
                        this.init_AiMemoryStore.load_V_Str(id);
                     } catch (Exception e) {
                        this.init_V_Str_Str(
                           "error",
                           "Correction saved as a new memory but the original could not be removed; both may appear until it is forgotten manually: "
                              + e.getMessage()
                        );
                     }
                  }

                  this.init_V_Str_Str("system", "Memory correction saved");
               } catch (Exception e) {
                  this.init_V_Str_Str("error", "Memory correction: " + e.getMessage());
               }
            }
         );
   }

   public List<AiService.AttachmentRef> flush_List() {
      return this.load_List;
   }

   public String put_Str() {
      return this.open_Str;
   }

   public void poll_V() {
      this.load_ExecutorService
         .execute(
            () -> {
               try {
                  List<AiService.AttachmentRef> found = new ArrayList<>();

                  for (String folder : List.of("scripts", "revisions")) {
                     Path path = this.init_AiMemoryStore.init_Path().resolve(folder);
                     if (Files.isDirectory(path)) {
                        try (Stream<Path> entries = Files.list(path)) {
                           entries.filter(x$0 -> Files.isRegularFile(x$0))
                              .filter(p -> p.toString().endsWith(".js") || p.toString().endsWith(".json"))
                              .sorted(Comparator.comparing(Path::toString).reversed())
                              .limit(100L)
                              .forEach(p -> found.add(new AiService.AttachmentRef(folder + " / " + p.getFileName(), folder + "/" + p.getFileName())));
                        }
                     }
                  }

                  this.load_List = List.copyOf(found);
                  this.open_Str = "Select generated source or a preserved macro revision.";
               } catch (Exception e) {
                  this.init_V_Str_Str("error", e.getMessage());
               }
            }
         );
   }

   public void run(String relative) {
      this.load_ExecutorService.execute(() -> {
         try {
            Path root = this.init_AiMemoryStore.init_Path().toAbsolutePath().normalize();
            Path path = root.resolve(relative).normalize();
            if (!path.startsWith(root) || this.load_List.stream().noneMatch(a -> a.relativePath().equals(relative))) {
               throw new IllegalArgumentException("Unknown artifact");
            }

            try (BufferedReader reader = Files.newBufferedReader(path)) {
               char[] buffer = new char[24000];
               int n = reader.read(buffer);
               this.open_Str = relative + "\n" + (n < 0 ? "" : new String(buffer, 0, n)) + (n == 24000 ? "\n[Preview limited to 24,000 characters]" : "");
            }
         } catch (Exception e) {
            this.open_Str = e.getMessage();
         }
      });
   }

   public void init_V_Z(boolean worldOnly) {
      if (init_Z_cc_FileCache(this.init_cc_FileCache)) {
         this.write_V();
      }

      String worldKey = Minecraft.getInstance().player == null ? "offline" : AiWorldIdentity.init(Minecraft.getInstance());
      String dimension = AiWorldIdentity.load(Minecraft.getInstance());
      AiPortalLinks.init_V(worldOnly ? worldKey : null);
      AiWorldMemory.init(worldOnly ? worldKey : null);
      this.load_ExecutorService.execute(() -> {
         try {
            Path portalDirectory = this.init_AiMemoryStore.init_Path().resolve("portal-memory");
            if (worldOnly) {
               Files.deleteIfExists(portalDirectory.resolve(AiJson.sha256Hex(worldKey) + ".json"));
            } else if (Files.isDirectory(portalDirectory)) {
               try (Stream<Path> files = Files.list(portalDirectory)) {
                  for (Path file : files.filter(x$0 -> Files.isRegularFile(x$0)).filter(p -> p.toString().endsWith(".json")).toList()) {
                     Files.deleteIfExists(file);
                  }
               }
            }

            Path discoveryDirectory = this.init_AiMemoryStore.init_Path().resolve("discovery-memory");
            if (Files.isDirectory(discoveryDirectory)) {
               try (Stream<Path> files = Files.list(discoveryDirectory)) {
                  for (Path file : files.filter(x$0 -> Files.isRegularFile(x$0)).filter(p -> p.toString().endsWith(".json")).toList()) {
                     if (!worldOnly) {
                        Files.deleteIfExists(file);
                     } else if (Files.size(file) <= 262144L) {
                        try {
                           if (AiJson.getString(AiJson.parseObject(Files.readString(file)), "world", "").startsWith(worldKey + "/dimension/")) {
                              Files.deleteIfExists(file);
                           }
                        } catch (RuntimeException var12) {
                        }
                     }
                  }
               }
            }

            if (worldOnly) {
               this.init_AiMemoryStore.init_V(worldKey);
               Minecraft.getInstance().execute(() -> {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.player != null && AiWorldIdentity.init(mc).equals(worldKey) && AiWorldIdentity.load(mc).equals(dimension)) {
                     this.pop_Str = this.init_AiMemoryStore.init_Str(worldKey);
                     this.close_Str = this.pop_Str + "/dimension/" + dimension;
                  }
               });
            } else {
               this.init_AiMemoryStore.load();
            }

            this.init_V_Str_Str("system", "Memory reset");
         } catch (Exception e) {
            this.init_V_Str_Str("error", e.getMessage());
         }
      });
   }

   public enum ProviderTestState {
      UNKNOWN,
      TESTING,
      OK,
      FAILED;
   }

   public record TokenUsage(
      long inputTokens, long cachedInputTokens, long cacheWriteTokens, long outputTokens, long lastPromptTokens, int contextWindow, long requests
   ) {
   }

   private final class ChatModeHost implements AiChatMode.Inner_BufferHelper {
      private final AiService.SessionState init_cc_RenderState = AiService.this.init_cc_RenderState();
      private final ClientLevel init_ClientLevel = Minecraft.getInstance().level;
      private final ClientPacketListener init_ClientPacketListen = Minecraft.getInstance().getConnection();
      private AiChatMode init_FieldCache;

      private boolean load_Z() {
         Minecraft mc = Minecraft.getInstance();
         return AiService.this.init_FieldCache == this.init_FieldCache
            && AiService.this.init_cc_RenderState() == this.init_cc_RenderState
            && mc.level == this.init_ClientLevel
            && mc.getConnection() == this.init_ClientPacketListen;
      }

      @Override
      public long init_J() {
         return System.currentTimeMillis();
      }

      @Override
      public String init_Str() {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null) {
            return "";
         }

         GameProfile profile = player.getGameProfile();
         return profile != null && profile.name() != null ? profile.name() : player.getName().getString();
      }

      @Override
      public boolean init_Z() {
         Minecraft mc = Minecraft.getInstance();
         return this.load_Z()
            && this.init_FieldCache.init_Z()
            && mc.player != null
            && this.init_ClientPacketListen != null
            && !autismclient.modules.aD.init_Z();
      }

      @Override
      public long load_J() {
         return AiChatLedger.init_J();
      }

      @Override
      public List<AiChatLedger.ChatEntry> init(int max) {
         return AiChatLedger.init_List(max);
      }

      @Override
      public void init(String text) {
         this.init(text, () -> true);
      }

      @Override
      public void init(String text, BooleanSupplier valid) {
         Minecraft mc = Minecraft.getInstance();
         mc.execute(() -> {
            if (valid.getAsBoolean() && this.init_Z()) {
               ClientPacketListener connection = mc.getConnection();
               if (connection != null) {
                  connection.sendChat(text);
               }
            }
         });
      }

      @Override
      public void init(String player, String text) {
         this.init(player, text, () -> true);
      }

      @Override
      public void init(String player, String text, BooleanSupplier valid) {
         if (AiChatPolicy.init_Z(player)) {
            String prefix = "msg " + player + " ";
            String body = text.length() + prefix.length() > 256 ? text.substring(0, 256 - prefix.length()).strip() : text;
            if (!body.isEmpty()) {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (valid.getAsBoolean() && this.init_Z()) {
                     ClientPacketListener connection = mc.getConnection();
                     if (connection != null) {
                        connection.sendCommand(prefix + body);
                     }
                  }
               });
            }
         }
      }

      @Override
      public void load(String role, String text) {
         if (this.load_Z()) {
            AiService.this.init_V_cc_RenderState(this.init_cc_RenderState, role, text);
         }
      }

      @Override
      public void init(JsonObject status) {
         if (this.load_Z()) {
            AiService.this.write_V_JsonObject(status);
         }
      }

      @Override
      public void init_V() {
         if (this.load_Z()) {
            AiService.this.pop_V();
         }
      }

      @Override
      public void load(String reason) {
         if (AiService.this.init_FieldCache == this.init_FieldCache && AiService.this.init_cc_RenderState() == this.init_cc_RenderState) {
            AiService.this.get(reason);
         }
      }

      @Override
      public void init(long inputTokens, long outputTokens, long requests) {
         AiService.SessionState s = this.init_cc_RenderState;
         if (s != null) {
            synchronized (s) {
               s.flush_J = s.flush_J + Math.max(0L, inputTokens);
               s.poll = s.poll + Math.max(0L, outputTokens);
               s.push = s.push + Math.max(0L, requests);
            }
         }
      }
   }

   private record ProviderTestResult(AiService.ProviderTestState state, String detail, String fingerprint) {
   }

   public record AttachmentRef(String name, String relativePath) {
   }

   static final class ToolCallGate {
      private final List<Integer> init_List = new ArrayList<>();
      private final TreeMap<Integer, JsonObject> init_TreeMap = new TreeMap<>();
      private List<Integer> load;

      void init_V(int index) {
         this.init_List.add(index);
         this.load = null;
      }

      void init_V(int index, JsonObject result) {
         this.init_TreeMap.put(index, result);
      }

      void init(JsonObject result) {
         for (int index : this.init_List) {
            this.init_TreeMap.putIfAbsent(index, result);
         }
      }

      JsonObject init_JsonObject(int index) {
         return this.init_TreeMap.get(index);
      }

      private List<Integer> init() {
         if (this.load == null) {
            this.load = this.init_List.stream().sorted().toList();
         }

         return this.load;
      }

      int init_I(int position) {
         return position < this.init().size() ? this.init().get(position) : -1;
      }

      List<JsonObject> init_List(int callCount, JsonObject filler) {
         List<JsonObject> out = new ArrayList<>(callCount);

         for (int k = 0; k < callCount; k++) {
            int index = this.init_I(k);
            JsonObject r = index < 0 ? null : this.init_TreeMap.get(index);
            out.add(r == null ? filler : r);
         }

         return out;
      }

      List<JsonObject> init_List(int callCount) {
         if (this.init_List.size() != callCount) {
            return null;
         }

         List<JsonObject> aligned = new ArrayList<>(callCount);

         for (int k = 0; k < callCount; k++) {
            JsonObject result = this.init_TreeMap.get(this.init().get(k));
            if (result == null) {
               return null;
            }

            aligned.add(result);
         }

         return aligned;
      }
   }

   public enum RunState {
      IDLE,
      RUNNING,
      PLANNING,
      PAUSED,
      COMPLETED,
      ANSWERED,
      BLOCKED,
      FAILED,
      CHATTING;
   }

   public record TranscriptEntry(long at, String role, String text, String detail) {
   }

   private record PromptContextBundle(AiContext.Inner_StreamUtil prompt, AiContext.Inner_DataUtil host, String catalogRevision) {
   }

   static final class SessionState {
      final String init_Str;
      final long init_J;
      volatile String load_Str = "New session";
      volatile String read_Str;
      volatile long load_J;
      volatile String write_Str = "IDLE";
      volatile String update_Str = "";
      volatile String sync_Str = "";
      volatile String flush_Str = "";
      volatile String close_Str = UUID.randomUUID().toString();
      volatile JsonObject init_JsonObject = new JsonObject();
      volatile int init_I = 60;
      volatile int load_I = -1;
      volatile long read_J;
      volatile long write_J;
      volatile long update_J;
      volatile long sync_J;
      volatile long flush_J;
      volatile long close_J;
      volatile long open_J;
      volatile long poll;
      volatile long push;
      volatile long pop;
      volatile String open_Str;
      volatile boolean init_Z;
      volatile boolean load_Z;
      volatile boolean read_Z;
      final Queue<String> init_Queue = new ConcurrentLinkedQueue<>();
      final List<String> init_List = new CopyOnWriteArrayList<>();
      final Deque<String> init_Deque = new ConcurrentLinkedDeque<>();
      final List<AiService.TranscriptEntry> load_List = new CopyOnWriteArrayList<>();

      SessionState(String id, long createdAt) {
         this.init_Str = id;
         this.init_J = createdAt;
         this.load_J = createdAt;
      }

      void init(String text) {
         String t = text.trim();
         if (!t.isEmpty()) {
            this.init_List.add(t.length() > 2000 ? t.substring(0, 2000) : t);

            while (this.init_List.size() > 8) {
               this.init_List.remove(0);
            }
         }
      }

      void init(boolean planModeEnabled) {
         this.load_Z = planModeEnabled;
         this.read_Z = false;
      }

      void load(String planText) {
         this.read_Z = true;
         String note = this.sync_Str + "\nPLAN AWAITING APPROVAL:\n" + planText;
         this.sync_Str = note.length() > 11900 ? note.substring(note.length() - 11900) : note;
      }

      void init() {
         this.read_Z = false;
         this.load_Z = false;
         String note = this.sync_Str + "\nPlan approved by the user; execute it faithfully and report deviations.";
         this.sync_Str = note.length() > 11900 ? note.substring(note.length() - 11900) : note;
      }

      void read(String feedback) {
         this.read_Z = false;
         this.init_Queue.add(feedback);
         this.init(feedback);
      }
   }

   private record PendingToolBatch(List<AiProvider.Inner_DataUtil> calls, AiService.ToolCallGate gate) {
   }

   private record SettingDelta(String module, String key, String before, String after, long revision) {
   }

   private record ActiveOperation(
      long generation,
      String id,
      String fingerprint,
      AiOperation operation,
      CompletableFuture<AiOperation.Result> result,
      AutoCloseable lease,
      List<String> resources,
      boolean hostParked
   ) {
   }

   private record CompletionNote(boolean finished, String note) {
   }

   private final class SessionTurnTracker {
      private final String init_Str;
      private int init_I;
      private String load;
      private boolean init_Z;

      SessionTurnTracker(String sid) {
         this.init_Str = sid;
      }

      void init() {
         this.init_Z = true;
      }

      void init(List<JsonObject> history) {
         AiSessions storeRef = AiService.this.init_cd;
         if (storeRef != null) {
            if (!this.init_Z && (this.init_I > history.size() || this.init_I > 0 && !history.get(this.init_I - 1).toString().equals(this.load))) {
               this.init_Z = true;
            }

            boolean whole = this.init_Z;
            List<JsonObject> lines = new ArrayList<>();

            for (int i = whole ? 0 : this.init_I; i < history.size(); i++) {
               lines.add(history.get(i).deepCopy());
            }

            this.init_I = history.size();
            this.load = this.init_I == 0 ? null : history.get(this.init_I - 1).toString();
            this.init_Z = false;
            if (whole || !lines.isEmpty()) {
               AiService.this.load_ExecutorService.execute(() -> {
                  if (!AiService.this.load_Set.contains(this.init_Str)) {
                     try {
                        if (whole) {
                           storeRef.init_V_Str_List(this.init_Str, lines);
                        } else {
                           for (JsonObject line : lines) {
                              storeRef.init_V_Str_JsonObject(this.init_Str, line);
                           }
                        }
                     } catch (Exception e) {
                        AiService.this.init_V_Str_Str("error", "Transcript write failed: " + e.getMessage());
                     }
                  }
               });
            }
         }
      }
   }

   public record UserPromptQuestion(String text, List<String> options, long at) {
   }
}
