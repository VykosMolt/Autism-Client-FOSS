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
   public static final Set<String> READ_ONLY_TOOLS = Set.of(
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
   private static AiService INSTANCE;
   private final ExecutorService agentExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-agent");
      t.setDaemon(true);
      return t;
   });
   private final ExecutorService storageExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-storage");
      t.setDaemon(true);
      return t;
   });
   private final ExecutorService chatExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-ai-chat");
      t.setDaemon(true);
      return t;
   });
   private static final String CHAT_MODE_STATE_KEY = "hostChatMode";
   private final AtomicLong runGeneration = new AtomicLong();
   private final AiCatalog catalog = new AiCatalog();
   private final AiMemoryStore memoryStore = new AiMemoryStore(AutismClientAddon.FOLDER.toPath().resolve("ai"));
   private final AiTools tools = new AiTools(this);
   private final AiRegions regions = new AiRegions();
   private final AiPerfMetrics perfMetrics = new AiPerfMetrics();
   private final Map<String, AiService.SessionState> sessions = new ConcurrentHashMap<>();
   private final Set<String> deletedSessionIds = ConcurrentHashMap.newKeySet();
   private volatile String currentSessionId;
   private volatile AiSessions sessionStore;
   private final List<AiService.SettingDelta> settingDeltas = new ArrayList<>();
   private final Map<String, AiObservation> evidenceById = new ConcurrentHashMap<>();
   private final Map<String, String> evidenceCapability = new ConcurrentHashMap<>();
   private volatile String lastEvidenceId;
   private volatile boolean verifyOutcomes;
   private final Map<AiSettings.Provider, AiService.ProviderTestResult> providerTests = new ConcurrentHashMap<>();
   private final AtomicLong providerProbe = new AtomicLong();
   private volatile AiSettings settings = new AiSettings();
   private volatile AiService.RunState runState = AiService.RunState.IDLE;
   private volatile AiChatMode chatMode;
   private volatile AiService.RunState runStateBeforeChat = AiService.RunState.IDLE;
   private volatile JsonObject savedChatModeStatus = new JsonObject();
   private volatile String stateReason = "";
   private volatile String observationSession = UUID.randomUUID().toString();
   private volatile String observationScope = "offline";
   private volatile long runClockStartNanos;
   private volatile long scriptDeadlineNanos;
   private volatile JsonObject qualificationPerformance = new JsonObject();
   private volatile List<AiService.AttachmentRef> attachments = List.of();
   private volatile String attachmentPreview = "";
   private volatile AiService.ActiveOperation activeOperation;
   private volatile String operationDataJson = "{}";
   private volatile AiProvider activeProvider;
   private volatile AiScriptHost scriptHost;
   private volatile AiObservation latestObservation;
   private volatile AiObservation lastUserObservation;
   private volatile boolean storageLoaded;
   private volatile String storageError = "";
   private Object lastLevel;
   private volatile String worldEpochKey = "offline";
   static final long THREAT_RESUME_DELAY_MS = 20000L;
   private int tickCounter;
   private long respawnAtMs;
   private volatile boolean diedPendingResume;
   private volatile long threatEscalatedAt;
   private String deathDimensionId;
   private long lastBurrowDay = -1L;
   private long lastBurrowMs;
   private long lastSleepDay = -1L;
   private long lastSleepMs;
   private float lastHealth = -1.0F;
   private long lastDamageMs;
   private long lastThreatAnnounceMs;
   private long lastHazardAnnounceMs;
   private long lastHungerAnnounceMs;
   private final Deque<Long> recentInterruptions = new ArrayDeque<>();
   private final Set<Integer> targetingMonsterIds = new HashSet<>();
   private Map<String, Integer> lastInventoryCounts;
   private boolean deathArmed = true;
   private volatile String pendingSwitchSessionId;
   private int inputReleasesInFlight;
   private boolean swimJumpHeld;
   static final int init_I = 1800;
   static final long PARKED_OPERATION_TIMEOUT_NANOS = 660000000000L;
   static final double init_D = 0.7;
   static final double load_D = 0.35;
   static final double read_D = 0.6;
   static final String RESUME_NOTE = "The run resumed after a pause; continue the objective from the latest HOST STATE.";
   private static final Pattern USER_EVIDENCE_LINE_PATTERN = Pattern.compile(
      "\\n?(Current user statement evidence|Trusted current user-statement evidence ID): [0-9a-fA-F-]{36}"
   );
   static final double write_D = 0.55;
   static final double update_D = 0.15;
   static final double sync_D = 0.6;
   static final long read_J = 24000L;
   static final int load_I = 16000;
   private static final Pattern SPECIAL_TOKEN_PATTERN = Pattern.compile("<\\|[A-Za-z0-9_:.-]{1,40}\\|>");
   static final int read_I = 6;
   static final int write_I = 3;
   private long lastIdleThreatMs;
   private volatile String lastPerformStatus;
   private final Object questionLock = new Object();
   private volatile AiService.UserPromptQuestion pendingQuestion;
   private String questionAnswer;
   static final String DRAGON_KILL_STAT = "minecraft.killed:minecraft.ender_dragon";
   private static final String put = "hostPlanningPhase";
   private static final String add = "hostPlanAwaitingApproval";

   static boolean isSessionEmpty(AiService.SessionState s) {
      return s.objective.isBlank() && s.queue.isEmpty() && s.messages.stream().noneMatch(m -> "user".equals(m.role()));
   }

   static String pickFallbackSessionId(List<AiSessions.Inner_StreamUtil> newestFirst, String deleted, Predicate<String> empty) {
      for (AiSessions.Inner_StreamUtil m : newestFirst) {
         if (!m.id().equals(deleted) && !empty.test(m.id())) {
            return m.id();
         }
      }

      return null;
   }

   static boolean isExecutingState(AiService.RunState s) {
      return s == AiService.RunState.RUNNING || s == AiService.RunState.PLANNING;
   }

   static boolean isBusyState(AiService.RunState s) {
      return isExecutingState(s) || s == AiService.RunState.CHATTING;
   }

   public static synchronized AiService getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AiService();
      }

      return INSTANCE;
   }

   public static AiService getExistingInstance() {
      return INSTANCE;
   }

   static boolean auditIsSchematicBuildRunning() {
      AiService assistant = INSTANCE;
      if (assistant == null) {
         return false;
      }

      AiService.ActiveOperation current = assistant.activeOperation;
      if (current == null || !"game:build".equals(current.id())) {
         return false;
      }

      if (current.result() != null && current.result().isDone()) {
         return false;
      }

      AiService.RunState state = assistant.runState;
      if (state == AiService.RunState.RUNNING) {
         return true;
      }

      return state == AiService.RunState.CHATTING && current.hostParked();
   }

   JsonObject auditRunStep(long g, AiService.SessionState s, AiIntents.Inner_BufferHelper step, String occurrence)
      throws InterruptedException, ExecutionException {
      return this.performIntentStep(g, s, step, occurrence);
   }

   boolean init_Z() {
      return this.verifyOutcomes;
   }

   private static boolean isTouchingLava(Player player) {
      AABB box = player.getBoundingBox().expandTowards(0.0, 0.6, 0.0);

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         if (player.level().getFluidState(p).is(FluidTags.LAVA)) {
            return true;
         }
      }

      return false;
   }

   static boolean isSafeStanceForReflex(boolean onGround, boolean inWater, boolean onClimbable) {
      return onGround || inWater || onClimbable;
   }

   static boolean wouldExplosionKill(float worstSource) {
      return worstSource > 0.0F && autismclient.util.ExplosionDamageCalculator.wouldSelfDamageKillWithoutTotem(worstSource);
   }

   static boolean isInterruptibleOperation(String id) {
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

   static boolean isUnderAttackPressure(int hostilesWithin6, float health, long millisSinceDamage) {
      return hostilesWithin6 > 0 && (health <= 8.0F || millisSinceDamage < 3000L);
   }

   static AiOperation.Result interruptedFailureResult(AiOperation operation, AiFailure cause, String message) {
      boolean pending = operation.requiresReconciliationAfterEmergency();
      return new AiOperation.Result(
         pending ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
         message,
         AiJson.objectOf("interruptedActionNeedsReconciliation", pending, "reobserveBeforeResuming", true).toString(),
         List.of(),
         cause
      );
   }

   static boolean threatPauseExpired(AiService.RunState state, long threatEscalatedAt, long now, boolean planMode) {
      return state == AiService.RunState.PAUSED && threatEscalatedAt != 0L && now - threatEscalatedAt >= 20000L && !planMode;
   }

   static boolean shouldReactToHostiles(int hostilesNear, boolean sameDimension, boolean executing, boolean planMode) {
      return hostilesNear > 0 && sameDimension && executing && !planMode;
   }

   static String threatReflexOperation(float health, boolean armed, boolean planMode, boolean reflexSource) {
      return !planMode && !reflexSource ? (health > 8.0F && armed ? "game:fight" : "game:flee") : "";
   }

   static boolean isCombatWeaponId(String itemId) {
      return (itemId.contains("sword") || itemId.contains("_axe")) && !itemId.contains("wooden_") && !itemId.contains("golden_");
   }

   static boolean isDuskTime(long dayTime) {
      return dayTime >= 12500L && dayTime <= 13100L;
   }

   static boolean shouldSleepNow(boolean canSleep, boolean overworld, boolean pendingIsModelOp, boolean executing, boolean planMode) {
      return canSleep && executing && !planMode && overworld && !pendingIsModelOp;
   }

   private static boolean shouldClutchFall(Minecraft mc) {
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
         return !water && AiGameState.countInventoryItem("minecraft:cobweb") <= 0 ? false : AiDanger.assess(mc).fallDamage() >= 4.0F;
      } else {
         return false;
      }
   }

   static boolean shouldBurrowAtDusk(long dayTime, boolean overworld, boolean hasBedOrShelter, boolean pendingIsModelOp, boolean executing, boolean planMode) {
      return executing && !planMode && overworld && !hasBedOrShelter && !pendingIsModelOp && isDuskTime(dayTime);
   }

   static boolean isGameplayActionOperation(String id) {
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

   static String environmentalHazardOf(Player player) {
      if (!isPlayerVulnerable(player)) {
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

   static boolean isPlayerVulnerable(Player player) {
      return player != null && !player.getAbilities().invulnerable;
   }

   boolean hasActiveOperation() {
      return this.activeOperation != null;
   }

   private AiService() {
      AiService.SessionState initial = new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis());
      this.sessions.put(initial.id, initial);
      this.currentSessionId = initial.id;
      this.agentExecutor
         .execute(
            () -> {
               try {
                  // a bad settings file blocks tasks until settings are saved, but must not stop the session store from loading
                  try {
                     this.settings = AiSettings.load(this.memoryStore.directory());
                  } catch (Exception e) {
                     this.storageError = "AI settings: " + e.getMessage();
                     this.appendTranscript("error", this.storageError);
                  }

                  this.memoryStore.reload();
                  this.regions.init(this.memoryStore.directory());
                  AiSourceKnowledge.load();
                  AiPlaybooks.load();
                  autismclient.util.PacketSchemaRegistry.schemaClassCount();
                  AiMemoryStore.prune(this.memoryStore.directory().resolve("images"), 32);
                  synchronized (this) {
                     this.sessionStore = new AiSessions(this.memoryStore.directory());
                     this.sessionStore.reloadIndex();

                     try {
                        this.sessionStore.purgeDeleted();
                     } catch (Exception e) {
                        this.appendTranscript("error", "Could not remove empty sessions: " + e.getMessage());
                     }

                     Path task = this.memoryStore.directory().resolve("task.json");
                     if (Files.exists(task)) {
                        try {
                           AiSessions.Inner_DataUtil migrated = AiSessions.init(
                              AiJson.parseObject(Files.readString(task)),
                              UUID.randomUUID().toString(),
                              System.currentTimeMillis(),
                              this.settings.maxMinutes
                           );
                           this.sessionStore.save(migrated);
                           Files.delete(task);
                           this.appendTranscript("system", "Previous task migrated into session '" + migrated.title + "'");
                        } catch (Exception e) {
                           this.appendTranscript("error", "Could not migrate previous task.json: " + e.getMessage());
                        }
                     }

                     AiService.SessionState preliminary = this.currentSession();
                     List<AiSessions.Inner_StreamUtil> metas = this.sessionStore.load();
                     if (!metas.isEmpty()) {
                        boolean keepPreliminary = !isSessionEmpty(preliminary);
                        if (keepPreliminary) {
                           this.sessionStore.save(this.toStoredSession(preliminary));
                        }

                        AiService.SessionState restored = this.fromStoredSession(this.sessionStore.readSession(metas.get(0).id()));
                        if (!keepPreliminary) {
                           restored.messages.addAll(preliminary.messages);
                           this.sessions.remove(preliminary.id);
                        }

                        this.sessions.put(restored.id, restored);
                        this.currentSessionId = restored.id;
                        this.runState = restoredRunState(restored);
                        if (this.runState == AiService.RunState.PAUSED) {
                           this.appendTranscript("system", "Previous objective restored paused. Resume after joining the correct world.");
                        }
                     }
                  }

                  if (!this.memoryStore.systemNote().isBlank()) {
                     this.appendTranscript("system", this.memoryStore.systemNote());
                  }
               } catch (Exception e) {
                  this.storageError = e.getMessage();
                  this.appendTranscript("error", "AI persistence: " + e.getMessage());
               } finally {
                  this.storageLoaded = true;
               }
            }
         );
   }

   private AiService.SessionState currentSession() {
      return this.sessions.get(this.currentSessionId);
   }

   public AiService.RunState getRunState() {
      return this.runState;
   }

   public boolean isChatModeActive() {
      AiChatMode lane = this.chatMode;
      return lane != null && lane.isActive();
   }

   public String getChatModeSummary() {
      AiChatMode lane = this.chatMode;
      return lane == null ? "Chat mode off" : lane.statusLine();
   }

   public JsonObject getChatModeStatusJson() {
      AiChatMode lane = this.chatMode;
      if (lane != null) {
         return lane.statusJson();
      }

      JsonObject saved = this.savedChatModeStatus;
      return saved != null && !saved.isEmpty() ? saved.deepCopy() : AiJson.objectOf("active", false, "summary", "Chat mode off");
   }

   boolean isStorageReady() {
      return this.storageLoaded && this.storageError.isEmpty() && !this.getCatalogSnapshot().entries().isEmpty();
   }

   void runQualificationSmoke() {
      AiObservation snapshot = AiGameState.observe(this.observationSession, this.observationScope, "qualification-smoke");
      AiCatalog.Snapshot contracts = this.getCatalogSnapshot();
      this.agentExecutor
         .execute(
            () -> {
               try {
                  String rebuilt = AiContext.init(contracts, snapshot, "Inspect the installed client", "Read-only qualification smoke", List.of(), 96000);
                  if (!rebuilt.contains(contracts.revision())) {
                     throw new IllegalStateException("Missing catalog revision in mandatory context");
                  }

                  AiJson.writeAtomicJson(
                     this.memoryStore.directory().resolve("qualification-smoke.json"),
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

   public String getObjective() {
      return this.currentSession().objective;
   }

   String read_Str() {
      return this.currentSession().taskId;
   }

   public String getPlanNotes() {
      return this.currentSession().taskState;
   }

   public String getTaskStateJson() {
      return this.currentSession().structuredTask.toString();
   }

   String activeOperationDataJson() {
      return this.activeOperation == null ? "{}" : this.operationDataJson;
   }

   String lastOperationDataJson() {
      return this.operationDataJson;
   }

   public String getPerfMetricsJson() {
      return this.qualificationPerformance.toString();
   }

   public List<String> getQueuedObjectives() {
      return List.copyOf(this.currentSession().queue);
   }

   public void enqueueObjective(String text) {
      if (text != null && !text.isBlank()) {
         AiService.SessionState s = this.currentSession();
         if (text.length() <= 16000 && s.queue.size() < 20) {
            s.queue.add(text);
            this.appendTranscript("system", "Objective queued: " + text);
            this.persistCurrentSession();
         } else {
            this.appendTranscript("error", "Objective queue limit reached");
         }
      }
   }

   public String getStatusLine() {
      return this.stateReason;
   }

   public long getRequestCount() {
      return this.currentSession().requests;
   }

   public long getTokenCount() {
      return this.currentSession().tokens;
   }

   public int getTaskMaxMinutes() {
      return this.currentSession().taskMinutes;
   }

   public long getElapsedTaskMillis() {
      return this.currentElapsedNanos() / 1000000L;
   }

   public AiSettings getSettings() {
      return this.settings.copy();
   }

   List<AiRegions.Region> getDimensionRegions() {
      return this.regions.init(this.observationScope);
   }

   void recordSurveyRegion(BlockPos position, int beds, int bells, int paths, int farmland, int villagers, String from) {
      String evidenceId = UUID.randomUUID().toString();
      AiRegions.Region region = new AiRegions.Region(
         this.observationScope,
         position.getX(),
         position.getY(),
         position.getZ(),
         System.currentTimeMillis(),
         this.observationSession,
         evidenceId,
         beds,
         bells,
         paths,
         farmland,
         villagers,
         from.isBlank() ? List.of() : List.of(from)
      );
      this.regions.init(region);
      AiObservation proof = new AiObservation(evidenceId, this.observationSession, this.observationScope, System.currentTimeMillis(), "terrain-survey", AiJson.GSON.toJson(region));
      this.storageExecutor.execute(() -> {
         try {
            this.memoryStore.init(proof);
         } catch (Exception e) {
            this.appendTranscript("error", e.getMessage());
         }
      });
   }

   public AiMemoryStore getMemoryStore() {
      return this.memoryStore;
   }

   public AiCatalog.Snapshot getCatalogSnapshot() {
      return this.catalog.snapshot();
   }

   public String getDimensionId() {
      return this.observationScope;
   }

   public AiObservation getLatestObservation() {
      return this.latestObservation;
   }

   String lastObservationId() {
      AiObservation u = this.lastUserObservation;
      return u == null ? null : u.id();
   }

   public List<AiService.TranscriptEntry> getTranscript() {
      AiService.SessionState s = this.currentSession();
      return s == null ? List.of() : List.copyOf(s.messages);
   }

   public void appendTranscript(String role, String text) {
      this.appendTranscript(role, text, "");
   }

   public void appendTranscript(String role, String text, String detail) {
      this.appendToSession(this.currentSession(), role, text, detail);
   }

   private void appendToSession(AiService.SessionState s, String role, String text) {
      this.appendToSession(s, role, text, "");
   }

   private void appendToSession(AiService.SessionState s, String role, String text, String detail) {
      if (s != null && text != null && !text.isBlank()) {
         s.messages
            .add(
               new AiService.TranscriptEntry(
                  System.currentTimeMillis(),
                  role,
                  text.length() > 32000 ? text.substring(0, 32000) : text,
                  detail.length() > 16000 ? detail.substring(0, 16000) + " [truncated]" : detail
               )
            );

         while (s.messages.size() > 300) {
            s.messages.remove(0);
         }
      }
   }

   public List<AiSessions.Inner_StreamUtil> listSessions() {
      AiSessions storeRef = this.sessionStore;
      List<AiSessions.Inner_StreamUtil> metas = new ArrayList<>();
      if (storeRef != null) {
         for (AiSessions.Inner_StreamUtil m : storeRef.load()) {
            if (!storeRef.init_Z_Str(m.id()) || m.id().equals(this.currentSessionId)) {
               metas.add(m);
            }
         }
      }

      AiService.SessionState s = this.currentSession();
      if (s != null) {
         AiSessions.Inner_StreamUtil live = new AiSessions.Inner_StreamUtil(
            s.id, s.title, s.createdAt, s.lastActiveAt, s.parentId, this.runState.name(), s.messages.size()
         );
         boolean replaced = false;

         for (int i = 0; i < metas.size(); i++) {
            if (metas.get(i).id().equals(s.id)) {
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

   public String getActiveSessionId() {
      return this.currentSessionId;
   }

   public String getActiveSessionTitle() {
      AiService.SessionState s = this.currentSession();
      return s == null ? "" : s.title;
   }

   private static AiService.RunState restoredRunState(AiService.SessionState s) {
      if (s.objective.isBlank()) {
         return AiService.RunState.IDLE;
      }

      AiService.RunState var10000;
      switch (s.state) {
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
            var10000 = s.objective.isBlank() ? AiService.RunState.IDLE : AiService.RunState.PAUSED;
            break;
         default:
            var10000 = AiService.RunState.PAUSED;
      }

      return var10000;
   }

   public synchronized String createSession() {
      if (this.storageLoaded && this.sessionStore != null) {
         AiService.SessionState current = this.currentSession();
         if (current != null && isSessionEmpty(current) && !isExecutingState(this.runState)) {
            this.stateReason = "";
            return current.id;
         }

         this.stopChatMode("Chat mode stopped: session switched");
         if (isExecutingState(this.runState)) {
            this.transitionRunState(AiService.RunState.PAUSED, "Session switched; task paused");
         }

         this.persistSession(current);
         this.activateSession(new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis()));
         return this.currentSessionId;
      } else {
         this.appendTranscript("system", "AI storage is still loading");
         return null;
      }
   }

   private void activateSession(AiService.SessionState target) {
      this.pendingSwitchSessionId = null;
      AiService.SessionState previous = this.currentSession();
      if (previous != null && previous != target && isSessionEmpty(previous)) {
         this.sessions.remove(previous.id);
      }

      this.sessions.put(target.id, target);
      this.currentSessionId = target.id;
      target.lastActiveAt = System.currentTimeMillis();
      this.runState = restoredRunState(target);
      this.stateReason = "";
      this.evidenceById.clear();
      this.evidenceCapability.clear();
      this.lastEvidenceId = null;
      this.tools.reset();
      this.persistSession(target);
   }

   public synchronized void switchSession(String id) {
      if (id != null && !id.equals(this.currentSessionId)) {
         if (this.storageLoaded && this.sessionStore != null) {
            AiService.SessionState loaded = this.sessions.get(id);
            if (loaded != null) {
               this.switchToSession(loaded);
            } else {
               this.pendingSwitchSessionId = id;
               AiSessions storeRef = this.sessionStore;
               this.storageExecutor.execute(() -> {
                  AiService.SessionState target;
                  try {
                     target = this.fromStoredSession(storeRef.readSession(id));
                  } catch (Exception e) {
                     this.appendTranscript("error", "Could not load session: " + e.getMessage());
                     return;
                  }

                  Minecraft.getInstance().execute(() -> {
                     synchronized (this) {
                        if (id.equals(this.pendingSwitchSessionId) && !this.deletedSessionIds.contains(id)) {
                           this.sessions.putIfAbsent(id, target);
                           this.switchToSession(this.sessions.get(id));
                        }
                     }
                  });
               });
            }
         } else {
            this.appendTranscript("system", "AI storage is still loading");
         }
      }
   }

   private void switchToSession(AiService.SessionState target) {
      this.stopChatMode("Chat mode stopped: session switched");
      if (isExecutingState(this.runState)) {
         this.transitionRunState(AiService.RunState.PAUSED, "Switched to another session; task paused");
      }

      this.persistSession(this.currentSession());
      this.activateSession(target);
   }

   public synchronized void renameSession(String id, String title) {
      if (id == null || title == null || title.isBlank()) {
         this.appendTranscript("error", "Session title is blank");
      } else if (this.storageLoaded && this.sessionStore != null) {
         String clean = title.trim();
         if (clean.length() > 96) {
            clean = clean.substring(0, 96).trim();
         }

         AiService.SessionState s = this.sessions.get(id);
         if (s != null) {
            s.title = clean;
            this.persistSession(s);
         } else {
            AiSessions storeRef = this.sessionStore;
            String name = clean;
            this.storageExecutor.execute(() -> {
               if (!this.deletedSessionIds.contains(id)) {
                  try {
                     storeRef.rename(id, name);
                  } catch (Exception e) {
                     this.appendTranscript("error", "Rename session: " + e.getMessage());
                  }
               }
            });
         }
      } else {
         this.appendTranscript("system", "AI storage is still loading");
      }
   }

   public synchronized void deleteSession(String id) {
      if (id != null) {
         if (this.storageLoaded && this.sessionStore != null) {
            this.deletedSessionIds.add(id);
            if (id.equals(this.currentSessionId)) {
               this.stopChatMode("Chat mode stopped: session deleted");
               if (isExecutingState(this.runState) || this.runState == AiService.RunState.PAUSED) {
                  this.transitionRunState(AiService.RunState.IDLE, "Session deleted");
               }

               String next = pickFallbackSessionId(this.sessionStore.load(), id, this.sessionStore::init_Z_Str);
               AiService.SessionState target = next == null ? null : this.sessions.get(next);
               this.activateSession(target != null ? target : new AiService.SessionState(UUID.randomUUID().toString(), System.currentTimeMillis()));
               if (next != null && target == null) {
                  this.switchSession(next);
               }
            }

            this.sessions.remove(id);
            AiSessions storeRef = this.sessionStore;
            this.storageExecutor.execute(() -> {
               try {
                  storeRef.delete(id);
               } catch (Exception e) {
                  this.appendTranscript("error", "Delete session: " + e.getMessage());
               }
            });
         } else {
            this.appendTranscript("system", "AI storage is still loading");
         }
      }
   }

   public synchronized boolean branchSession(String id) {
      if (id == null) {
         return false;
      }

      if (this.storageLoaded && this.sessionStore != null) {
         AiService.SessionState source = this.sessions.get(id);
         if (source != null && isSessionEmpty(source)) {
            return false;
         }

         if (id.equals(this.currentSessionId)) {
            this.stopChatMode("Chat mode stopped: session branched");
         }

         if (isExecutingState(this.runState)) {
            this.transitionRunState(AiService.RunState.PAUSED, "Session branched; task paused");
         }

         if (source != null) {
            this.persistSession(source);
         }

         AiSessions storeRef = this.sessionStore;
         this.storageExecutor.execute(() -> {
            AiService.SessionState fork;
            try {
               fork = this.fromStoredSession(storeRef.fork(id));
            } catch (Exception e) {
               this.appendTranscript("error", "Branch session: " + e.getMessage());
               return;
            }

            Minecraft.getInstance().execute(() -> {
               synchronized (this) {
                  this.sessions.put(fork.id, fork);
                  this.switchToSession(fork);
               }
            });
         });
         return true;
      } else {
         this.appendTranscript("system", "AI storage is still loading");
         return false;
      }
   }

   private void awaitStorageDrain() {
      CountDownLatch drained = new CountDownLatch(1);
      this.storageExecutor.execute(drained::countDown);

      try {
         if (!drained.await(15L, TimeUnit.SECONDS)) {
            this.appendTranscript("error", "Storage flush timed out");
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   void load_V() {
      this.currentSession().attemptedAction = true;
   }

   public void submitPrompt(String prompt) {
      this.startPromptTask(prompt, null);
   }

   public void submitBuiltInAction(String label, String capability, JsonObject arguments) {
      if (!this.settings.builtIn) {
         throw new IllegalStateException("Select the Built-In provider first");
      }

      if (this.pendingQuestion != null) {
         throw new IllegalStateException("Answer the current question first");
      }

      if (!this.storageLoaded) {
         throw new IllegalStateException("AI storage is still loading");
      }

      if (!this.storageError.isEmpty()) {
         throw new IllegalStateException("Resolve AI settings/memory error first: " + this.storageError);
      }

      JsonObject local = AiLocalRequest.init(label, capability, arguments);
      if (capability.equals("game:build") || capability.equals("game:goto") || capability.equals("game:farm") && arguments.has("from")) {
         local.addProperty("placementWorld", AiWorldIdentity.currentWorldKey(Minecraft.getInstance()));
         local.addProperty("placementDimension", AiWorldIdentity.currentDimensionId(Minecraft.getInstance()));
      }

      if (capability.equals("game:build") && arguments.has("schematic")) {
         try {
            AiLocalRequest.stampSource(local, AiSchematic.resolveFile(AiSchematic.init(), AiJson.getString(arguments, "schematic", "")));
         } catch (IOException e) {
            throw new IllegalArgumentException("Schematic is no longer available: " + e.getMessage(), e);
         }
      }

      this.startPromptTask(label, local);
   }

   private void startPromptTask(String prompt, JsonObject localAction) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.SyncManager.init()) {
         this.appendTranscript("error", "Autism is locked: early access requires the donator role.");
      } else if (prompt != null && !prompt.isBlank()) {
         if (prompt.length() > 16000) {
            this.appendTranscript("error", "Request exceeds 16,000 characters");
         } else if (!this.storageLoaded) {
            this.appendTranscript("system", "AI storage is still loading");
         } else if (!this.storageError.isEmpty()) {
            this.appendTranscript("error", "Resolve AI settings/memory error first: " + this.storageError);
         } else {
            AiService.SessionState s = this.currentSession();
            if (this.pendingQuestion != null) {
               this.answerPendingQuestion(prompt);
            } else if (AiChatPolicy.read(prompt) && this.isChatModeActive() && !AiChatPolicy.write(prompt)) {
               this.appendTranscript("user", prompt);
               this.stopChatMode("Chat mode stopped by user");
            } else {
               if (isExecutingState(this.runState) && this.settings.builtIn) {
                  this.transitionRunState(AiService.RunState.IDLE, "Replaced by a new request");
               }

               if (isExecutingState(this.runState)) {
                  if (!AiChatPolicy.load_Z(prompt) || AiChatPolicy.read(prompt) || this.isPlanPendingOrPlanning() || this.settings.planMode || this.settings.builtIn
                     )
                   {
                     s.taskMinutes = AiTaskDuration.init(prompt, s.taskMinutes);
                     s.amendments.add(prompt);
                     s.init(prompt);
                     this.appendTranscript("user", prompt);
                     this.stateReason = "Objective amended; releasing the previous operation before replanning";
                     Minecraft.getInstance().execute(this::cancelActiveOperation);
                     AiScriptHost script = this.scriptHost;
                     if (script != null) {
                        Thread.ofVirtual().start(script::init);
                     }
                  } else if (AiChatPolicy.write(prompt)) {
                     this.appendTranscript("user", prompt);
                     this.tryStartChatMode(prompt);
                     s.taskMinutes = AiTaskDuration.init(prompt, s.taskMinutes);
                     s.amendments.add(prompt);
                     s.init(prompt);
                     this.stateReason = "Chat mode updated; the work request will be planned at the next action boundary";
                  } else {
                     this.appendTranscript("user", prompt);
                     if (this.tryStartChatMode(prompt)) {
                        this.stateReason = "Chat mode updated; the running operation continues";
                     }
                  }
               } else {
                  if (AiChatPolicy.load_Z(prompt)
                     && !AiChatPolicy.read(prompt)
                     && !this.isPlanPendingOrPlanning()
                     && !this.settings.planMode
                     && !this.settings.builtIn) {
                     if (this.runState == AiService.RunState.CHATTING && this.isChatModeActive() && !AiChatPolicy.write(prompt)) {
                        this.appendTranscript("user", prompt);
                        this.tryStartChatMode(prompt);
                        return;
                     }

                     this.tryStartChatMode(prompt);
                  }

                  s.taskId = UUID.randomUUID().toString();
                  s.objective = prompt;
                  s.taskMinutes = AiTaskDuration.init(prompt, this.settings.maxMinutes);
                  s.taskState = "Not started";
                  this.evidenceById.clear();
                  this.evidenceCapability.clear();
                  this.lastEvidenceId = null;
                  this.tools.reset();
                  s.requests = s.tokens = s.elapsedNanos = 0L;
                  s.unusedResetTime = 0L;
                  s.wheatBaseline = -1;
                  s.attemptedAction = false;
                  // Built aside and swapped in under the session lock, like every other writer: readers copy this object.
                  JsonObject fresh = new JsonObject();
                  if (localAction != null) {
                     fresh.add("builtInAction", localAction.deepCopy());
                  }

                  synchronized (s) {
                     JsonObject old = s.structuredTask;
                     if (old.has("hostChatMode") && old.get("hostChatMode").isJsonObject()) {
                        fresh.add("hostChatMode", old.getAsJsonObject("hostChatMode").deepCopy());
                     }

                     s.structuredTask = fresh;
                  }

                  s.scope = "";
                  s.updates.clear();
                  s.init(this.settings.planMode && !this.settings.builtIn);
                  AiRunTrace.startSession(this.memoryStore.directory(), s.id).recordTaskStart(prompt, s.taskMinutes);
                  if (s.title.isBlank() || s.title.equals("New session")) {
                     s.title = AiSessions.titleFor(prompt);
                  }

                  this.appendTranscript("user", prompt);
                  this.startAgentRun(false);
               }
            }
         }
      }
   }

   public JsonObject startChatMode(String instructions) {
      String text = instructions == null ? "" : instructions.trim();
      if (text.isEmpty()) {
         return this.chatModeReply(this.getChatModeSummary());
      }

      this.appendTranscript("user", "Keep chatting: " + text);

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
         return this.chatModeReplyWith("Chat mode is active", this.launchChatMode(config, null, false));
      } catch (Exception error) {
         this.appendTranscript("error", "Chat mode: " + error.getMessage());
         return AiJson.objectOf("status", "FAILED", "error", Objects.toString(error.getMessage(), "Chat mode could not start"), "active", false);
      }
   }

   private boolean tryStartChatMode(String objective) {
      if (this.settings.builtIn) {
         this.appendTranscript("warning", "Chat mode needs an API provider: select one in AI settings (Built-In has no model to chat with).");
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
         this.launchChatMode(config, null, true);
         return true;
      } catch (Exception error) {
         this.appendTranscript("error", "Chat mode: " + error.getMessage());
         return false;
      }
   }

   private JsonObject launchChatMode(AiChatPolicy.Inner_DataUtil config, String note, boolean auto) {
      if (config == null || config.instructions().isBlank()) {
         throw new IllegalArgumentException("chat mode needs instructions describing the persona and what to keep talking about");
      }

      if (!AutismLiteVariant.enabled() && !PacketUtil.init()) {
         throw new IllegalStateException("Autism is locked: early access requires the donator role");
      }

      if (!this.storageLoaded) {
         throw new IllegalStateException("AI storage is still loading");
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null) {
         AiSettings live = this.settings;
         if (live.builtIn) {
            throw new IllegalStateException("Chat mode needs an API provider; Built-In has no model (select a provider in AI settings)");
         }

         if (!live.hasKey(live.provider.name())) {
            throw new IllegalStateException("No " + live.provider.name() + " API key is set; add one in AI settings first");
         }

         AiChatMode lane = this.chatMode;
         if (lane != null && lane.isActive()) {
            lane.applyPolicy(config);
            this.appendTranscript(
               "system",
               note != null && !note.isBlank() ? note : "Chat mode updated; it keeps answering player chat in the background without touching movement."
            );
            this.persistCurrentSession();
            return lane.statusJson();
         }

         AiService.ChatModeHost host = new AiService.ChatModeHost();
         AiSettings chatSettings = live.copy();
         AiChatMode fresh = new AiChatMode(config, host, () -> new AiChatMode.Inner_ConfigStore(chatSettings), this.chatExecutor);
         host.boundChatMode = fresh;
         this.chatMode = fresh;

         try {
            fresh.start();
         } catch (RuntimeException | Error failure) {
            this.chatMode = null;
            if (this.runState == AiService.RunState.CHATTING) {
               this.runState = this.runStateBeforeChat == null ? AiService.RunState.IDLE : this.runStateBeforeChat;
               this.stateReason = "Chat mode could not start";
            }

            throw failure;
         }

         this.appendTranscript(
            "system",
            note != null && !note.isBlank()
               ? note
               : (
                  auto
                     ? "Chat mode started automatically: it answers player chat in the background, never owns movement, and keeps running after the objective ends. Stop it with chat_keep stop or .ai chat stop."
                     : "Chat mode started: it answers player chat in the background, never owns movement, and keeps running after the objective ends. Stop it with chat_keep stop or .ai chat stop."
               )
         );
         this.persistCurrentSession();
         return fresh.statusJson();
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
         return this.chatModeReply(this.getChatModeSummary());
      } else if (action.equals("stop")) {
         this.stopChatMode("Chat mode stopped by the assistant");
         return this.chatModeReply(this.getChatModeSummary());
      } else if (!action.equals("start")) {
         throw new IllegalArgumentException("chat_keep action must be start, stop or status (got '" + action + "')");
      } else {
         AiChatPolicy.Inner_DataUtil config = AiChatPolicy.Inner_DataUtil.parse(args);
         if (config.instructions().isBlank()) {
            throw new IllegalArgumentException("chat_keep start needs instructions: the persona and what to keep talking about");
         } else {
            return this.onClientThread(
               g, () -> this.chatModeReplyWith("Chat mode is active", this.launchChatMode(config, null, false))
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
            return this.onClientThread(g, () -> {
               Minecraft mc = Minecraft.getInstance();
               if (mc.getConnection() == null) {
                  throw new IllegalStateException("chat_send requires a connected server session");
               }

               mc.getConnection().sendChat(text);
               this.forwardChatMessage(text);
               return AiJson.objectOf("status", "SUCCEEDED", "message", "Chat line sent", "text", text, "backgroundChat", this.isChatModeActive());
            });
         }
      }
   }

   private JsonObject chatModeReply(String message) {
      return this.chatModeReplyWith(message, this.getChatModeStatusJson());
   }

   private JsonObject chatModeReplyWith(String message, JsonObject chat) {
      return AiJson.objectOf("status", "SUCCEEDED", "message", message, "chat", chat);
   }

   void forwardChatMessage(String text) {
      AiChatMode lane = this.chatMode;
      if (lane != null && lane.isActive()) {
         lane.init_V_Str(text);
      }
   }

   public void stopChatMode(String reason) {
      AiChatMode lane = this.chatMode;
      if (lane != null) {
         lane.load(reason != null && !reason.isBlank() ? reason : "Chat mode stopped");
      }
   }

   private void saveChatModeStatus(JsonObject status) {
      JsonObject copy = status == null ? new JsonObject() : status.deepCopy();
      this.savedChatModeStatus = copy;
      AiService.SessionState s = this.currentSession();
      if (s != null) {
         synchronized (s) {
            JsonObject next = s.structuredTask.deepCopy();
            if (!copy.isEmpty() && AiJson.getBoolean(copy, "active", false)) {
               next.add("hostChatMode", copy.deepCopy());
            } else {
               next.remove("hostChatMode");
            }

            s.structuredTask = next;
         }
      }

      if (this.runState == AiService.RunState.CHATTING) {
         this.stateReason = copy.isEmpty() ? "Chat mode off" : AiJson.getString(copy, "summary", "Chatting");
      }
   }

   private synchronized void enterChattingState() {
      if (this.isChatModeActive()
         && (
            this.runState == AiService.RunState.COMPLETED
               || this.runState == AiService.RunState.ANSWERED
               || this.runState == AiService.RunState.IDLE
         )) {
         this.runStateBeforeChat = this.runState;
         this.runState = AiService.RunState.CHATTING;
         this.stateReason = this.getChatModeSummary();
      }
   }

   private synchronized void refreshChattingState() {
      this.enterChattingState();
   }

   private synchronized void get(String reason) {
      String line = reason != null && !reason.isBlank() ? reason : "Chat mode stopped";
      this.saveChatModeStatus(null);
      if (this.runState == AiService.RunState.CHATTING) {
         this.runState = this.runStateBeforeChat == null ? AiService.RunState.IDLE : this.runStateBeforeChat;
         this.stateReason = line;
      }

      this.appendTranscript("system", line);
      this.persistCurrentSession();
   }

   private void startAgentRun(boolean resume) {
      this.startAgentRunWithNote(resume, null);
   }

   private void startAgentRunWithNote(boolean resume, String note) {
      this.threatEscalatedAt = 0L;
      long g = this.runGeneration.incrementAndGet();
      AiService.SessionState s = this.currentSession();
      this.runState = s != null && s.planningPhase ? AiService.RunState.PLANNING : AiService.RunState.RUNNING;
      this.runClockStartNanos = System.nanoTime();
      this.stateReason = resume ? "Reobserving before resuming" : "Preparing client context";
      if (resume) {
         this.tools.reset();
      }

      this.agentExecutor.execute(() -> this.runAgentLoop(g, resume, note));
   }

   public void resumeTask() {
      if (AutismLiteVariant.enabled() || autismclient.e.SyncManager.init()) {
         AiService.SessionState s = this.currentSession();
         if (s != null && s.planAwaitingApproval) {
            this.appendTranscript("system", "A plan awaits approval; approve or reject it instead of resuming");
         } else if ((
               this.runState == AiService.RunState.PAUSED
                  || this.runState == AiService.RunState.BLOCKED
                  || this.runState == AiService.RunState.FAILED
                  || this.runState == AiService.RunState.CHATTING && !this.currentSession().objective.isBlank()
            )
            && !this.currentSession().objective.isBlank()) {
            this.startAgentRunWithNote(true, "The run resumed after a pause; continue the objective from the latest HOST STATE.");
         }
      }
   }

   public boolean isPlanPendingOrPlanning() {
      AiService.SessionState s = this.currentSession();
      return this.runState == AiService.RunState.PLANNING || this.runState == AiService.RunState.PAUSED && s != null && s.planAwaitingApproval;
   }

   public boolean isAwaitingPlanApproval() {
      AiService.SessionState s = this.currentSession();
      return s != null && s.planAwaitingApproval;
   }

   public synchronized boolean approvePlan() {
      AiService.SessionState s = this.currentSession();
      if (s != null && s.planAwaitingApproval) {
         s.init();
         this.appendTranscript("system", "Plan approved; execution started");
         this.persistCurrentSession();
         this.startAgentRunWithNote(true, "The user approved the plan above. Execute it faithfully and report any deviation.");
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean rejectPlan(String feedback) {
      AiService.SessionState s = this.currentSession();
      if (s == null || !s.planAwaitingApproval) {
         return false;
      } else if (feedback != null && !feedback.isBlank()) {
         s.read(feedback.trim());
         this.appendTranscript("user", feedback.trim());
         this.persistCurrentSession();
         this.startAgentRun(true);
         return true;
      } else {
         this.appendTranscript("error", "Plan rejection needs feedback for the planner");
         return false;
      }
   }

   public void pauseTask() {
      this.stopChatMode("Chat mode stopped by user");
      this.transitionRunState(AiService.RunState.PAUSED, "Paused by user");
   }

   public void stopTask() {
      this.stopChatMode("Chat mode stopped by user");
      this.transitionRunState(AiService.RunState.IDLE, "Stopped by user");
   }

   private void transitionRunState(AiService.RunState next, String reason) {
      this.stopRunClock();
      this.diedPendingResume = false;
      this.threatEscalatedAt = 0L;
      this.runGeneration.incrementAndGet();
      this.runState = next;
      this.stateReason = reason;
      if (next == AiService.RunState.PAUSED) {
         this.appendTranscript("system", reason);
         System.out.println("[AI task] PAUSED: " + reason);
      }

      AiProvider p = this.activeProvider;
      if (p != null) {
         Thread.ofVirtual().start(p::close);
      }

      AiScriptHost h = this.scriptHost;
      if (h != null) {
         Thread.ofVirtual().start(h::init);
      }

      Minecraft.getInstance().execute(this::revertSettingDeltas);
      this.persistCurrentSession();
   }

   public void onWorldDisconnect() {
      this.stopChatMode("Chat mode stopped: left the world");
      if (isExecutingState(this.runState) && !this.settings.autoRejoin) {
         this.transitionRunState(AiService.RunState.PAUSED, "World changed; reobserve before resuming");
      }
   }

   void onClientTick() {
      long start = System.nanoTime();

      try {
         if (autismclient.modules.aD.init_Z()) {
            if (this.isChatModeActive()) {
               this.stopChatMode("Chat mode stopped: client panic mode");
            }

            if (isExecutingState(this.runState)) {
               this.transitionRunState(AiService.RunState.PAUSED, "Client panic mode; task paused");
            }
         }

         if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
            this.tickClientState();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[AI] tick failed", t);
      } finally {
         this.perfMetrics.load(System.nanoTime() - start);
      }
   }

   private boolean auditAfloat(Minecraft mc) {
      AiService.ActiveOperation driving = this.activeOperation;
      return mc.player != null
         && mc.player.isAlive()
         && isBusyState(this.runState)
         && mc.player.isInWater()
         && mc.gui.screen() == null
         && (driving == null || !driving.resources().contains("INPUT"))
         && (!mc.player.onGround() || mc.player.isUnderWater())
         && !isTouchingLava(mc.player);
   }

   private void auditReleaseSwimJump(Minecraft mc) {
      if (this.swimJumpHeld) {
         this.swimJumpHeld = false;
         mc.options.keyJump.setDown(autismclient.util.KeyMappingControl.of(mc.options.keyJump).autism$physicalForBody());
      }
   }

   public void tickClientState() {
      Minecraft mc = Minecraft.getInstance();
      // Early returns below skip the auto-swim block, so release a stale jump here.
      if (this.swimJumpHeld && !this.auditAfloat(mc)) {
         this.auditReleaseSwimJump(mc);
      }

      if (this.storageLoaded) {
         if (mc.level != this.lastLevel) {
            String previousDimension = this.lastLevel instanceof Level previous ? previous.dimension().identifier().toString() : "offline";
            if (this.isChatModeActive()) {
               this.stopChatMode("Chat mode stopped: world/session changed");
            }

            AiServerEvidence.clearAll();
            AiChatLedger.clear();
            autismclient.ai.engine.AiGridRevisions.resetAll();
            AiScaffold.reset();
            this.lastLevel = mc.level;
            this.observationSession = UUID.randomUUID().toString();
            this.lastInventoryCounts = null;
            AiEventLog.getInstance().auditReset(mc.getConnection());
            AiEventLog.getInstance().init("dimension_changed", AiJson.objectOf("dimension", AiWorldIdentity.currentDimensionId(mc)));
            AiRunTrace trace = AiRunTrace.current();
            if (trace != null && (isExecutingState(this.runState) || this.runState == AiService.RunState.PAUSED)) {
               String nowDimension = AiWorldIdentity.currentDimensionId(mc);
               trace.recordDimensionChange(previousDimension, nowDimension);
               if ("minecraft:the_nether".equals(nowDimension)) {
                  trace.recordSplit("nether entered", "");
               } else if ("minecraft:the_end".equals(nowDimension)) {
                  trace.recordSplit("end entered", "");
               }
            }

            if (isExecutingState(this.runState) && this.settings.autoRejoin) {
               AiService.ActiveOperation current = this.activeOperation;
               if (current != null && !AiDirectOps.init_Z(current.id())) {
                  this.releaseOperationResources(current, true);
                  current.result()
                     .complete(AiGameOperations.result(AiOperation.Status.CANCELLED, "World/session changed during operation", new JsonObject()));
                  this.activeOperation = null;
               }

               AiScriptHost active_scripts = this.scriptHost;
               if (active_scripts != null) {
                  Thread.ofVirtual().start(active_scripts::init);
               }

               this.tools.reset();
               this.currentSession().scope = "";
               this.appendTranscript("system", "World/session changed; the objective continues and the scope re-bases on the next observation.");
               String note = this.currentSession().taskState
                  + "\nThe world or session changed (kick, rejoin, or world switch); the scope re-bases automatically - re-observe before continuing.";
               this.updateTaskState(note.length() > 11900 ? note.substring(note.length() - 11900) : note);
            } else if (isExecutingState(this.runState) && (this.activeOperation == null || !AiDirectOps.init_Z(this.activeOperation.id()))) {
               this.transitionRunState(AiService.RunState.PAUSED, "World/session changed");
            }

            this.worldEpochKey = mc.player == null ? "offline" : this.memoryStore.epochKey(AiWorldIdentity.currentWorldKey(mc));
            this.observationScope = this.worldEpochKey + "/dimension/" + AiWorldIdentity.currentDimensionId(mc);
         }

         if (this.storageLoaded) {
            AiChatMode chatLane = this.chatMode;
            if (chatLane != null && chatLane.isActive()) {
               if (!AutismLiteVariant.enabled() && !autismclient.e.CharEncoder.init()) {
                  this.stopChatMode("Chat mode stopped: Autism is locked");
               } else if (this.settings.builtIn) {
                  this.stopChatMode("Chat mode stopped: the provider switched to Built-In");
               } else {
                  if (this.runState == AiService.RunState.COMPLETED
                     || this.runState == AiService.RunState.ANSWERED
                     || this.runState == AiService.RunState.IDLE) {
                     this.enterChattingState();
                  }

                  chatLane.tick();
               }
            }

            if (++this.tickCounter % 20 == 0 || this.catalog.snapshot().entries().isEmpty()) {
               try {
                  this.catalog.refresh();
               } catch (Exception e) {
                  this.appendTranscript("error", "Catalog: " + e.getMessage());
               }
            }

            if (mc.player != null && !mc.player.isAlive()) {
               AiDeathLog.init(mc);
            }

            this.logWorldEvents(mc);
            if (isBusyState(this.runState) && mc.player != null && !mc.player.isAlive()) {
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordDeath();
               }

               if (this.runState != AiService.RunState.CHATTING) {
                  AiService.ActiveOperation dying = this.activeOperation;
                  if (isSpeedrunOperation(dying == null ? "" : dying.id())) {
                     String reason = "The deathless speedrun ended: player death was observed";

                     try {
                        AiOperation.Result terminal = dying.operation().tick();
                        this.operationDataJson = terminal.dataJson();
                        this.activeOperation = null;
                        this.releaseOperationResources(dying, !terminal.terminal());
                        dying.result().complete(terminal.terminal() ? terminal : AiOperation.Result.failed(AiFailure.THREAT_INTERRUPT, reason, terminal.dataJson()));
                     } catch (Throwable error) {
                        this.releaseOperationResources(dying, true);
                        this.activeOperation = null;
                        dying.result().completeExceptionally(error);
                     }

                     this.respawnAtMs = 0L;
                     this.transitionRunState(AiService.RunState.FAILED, reason);
                     return;
                  }

                  this.transitionRunState(
                     AiService.RunState.PAUSED,
                     this.settings.autoRespawn ? "Player died; respawning and resuming automatically" : "Player died; task paused"
                  );
                  this.diedPendingResume = true;
                  this.deathDimensionId = AiWorldIdentity.currentDimensionId(mc);
                  if (this.settings.autoRespawn) {
                     this.respawnAtMs = System.currentTimeMillis() + 1500L;
                  }

                  return;
               }

               AiService.ActiveOperation dying = this.activeOperation;
               if (dying != null) {
                  this.releaseOperationResources(dying, true);
                  dying.result().complete(AiOperation.Result.failed(AiFailure.THREAT_INTERRUPT, "Player died while chatting", "{}"));
                  this.activeOperation = null;
               }

               if (this.settings.autoRespawn && this.respawnAtMs == 0L) {
                  this.respawnAtMs = System.currentTimeMillis() + 1500L;
                  this.diedPendingResume = true;
                  this.deathDimensionId = AiWorldIdentity.currentDimensionId(mc);
                  this.appendTranscript("system", "Player died while chatting; respawning automatically and keeping the conversation.");
               }
            }

            if ((this.runState == AiService.RunState.PAUSED || this.runState == AiService.RunState.CHATTING)
               && this.respawnAtMs != 0L
               && System.currentTimeMillis() >= this.respawnAtMs) {
               this.respawnAtMs = 0L;
               if (mc.player != null && !mc.player.isAlive() && mc.getConnection() != null) {
                  mc.getConnection().send(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
                  this.respawnAtMs = System.currentTimeMillis() + 3000L;
               } else if (mc.player == null || !mc.player.isAlive()) {
                  this.respawnAtMs = System.currentTimeMillis() + 1000L;
               } else if (this.diedPendingResume) {
                  String diedIn = this.deathDimensionId;
                  this.diedPendingResume = false;
                  this.deathDimensionId = null;
                  AiRunTrace trace = AiRunTrace.current();
                  if (trace != null) {
                     trace.recordRespawn();
                  }

                  if (this.runState == AiService.RunState.CHATTING) {
                     this.appendTranscript("system", "Respawned after dying; chat continues.");
                     this.stateReason = this.getChatModeSummary();
                  } else {
                     int hostilesNear = 0;

                     for (Entity e : mc.level.entitiesForRendering()) {
                        if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 144.0) {
                           hostilesNear++;
                        }
                     }

                     boolean stabilize = shouldReactToHostiles(hostilesNear, AiWorldIdentity.currentDimensionId(mc).equals(diedIn), true, this.settings.planMode);
                     this.appendTranscript(
                        "system",
                        "Respawned after dying; the objective resumes with fresh observations"
                           + (stabilize ? " (moved to safety first)" : "")
                           + ". The grave was recorded - run game:recover within five minutes to get the dropped items back."
                     );
                     String note = this.currentSession().taskState
                        + "\nThe player died and respawned"
                        + (stabilize ? " near hostile mobs; a host-parked flee moved the player to safety before anything else ran" : "")
                        + "; the death position and carried items were recorded; game:recover walks back and collects them, but the drops despawn five minutes after the death.";
                     this.updateTaskState(note.length() > 11900 ? note.substring(note.length() - 11900) : note);
                     this.resumeTask();
                     if (stabilize) {
                        this.fleeAfterRespawn(mc);
                     }
                  }
               }
            }

            if (mc.player != null && threatPauseExpired(this.runState, this.threatEscalatedAt, System.currentTimeMillis(), this.settings.planMode)) {
               this.threatEscalatedAt = 0L;
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordOpInterrupt("threat_escalation_resume");
               }

               this.appendTranscript("system", "Auto-resuming after the threat pause.");
               this.resumeTask();
            }

            if (isExecutingState(this.runState) && this.currentElapsedNanos() > this.currentSession().taskMinutes * 60000000000L) {
               this.transitionRunState(AiService.RunState.FAILED, "Task time limit reached");
            } else {
               if (this.tickCounter % 100 == 0) {
                  this.storageExecutor.execute(() -> {
                     this.qualificationPerformance = this.perfMetrics.init();
                     if (Boolean.getBoolean("autism.ai.smoke")) {
                        try {
                           AiJson.writeAtomicJson(this.memoryStore.directory().resolve("qualification-performance.json"), this.qualificationPerformance);
                        } catch (Exception ex) {
                           this.appendTranscript("error", ex.getMessage());
                        }
                     }
                  });
                  this.storageExecutor.execute(() -> {
                     try {
                        this.regions.load(this.memoryStore.directory());
                     } catch (Exception ex) {
                        this.appendTranscript("error", "Region persistence: " + ex.getMessage());
                     }
                  });
                  if (isExecutingState(this.runState)) {
                     this.persistCurrentSession();
                  }

                  if (isExecutingState(this.runState) && mc.player != null && mc.player.isAlive() && mc.getConnection() != null) {
                     mc.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
                  }
               }

               if (isBusyState(this.runState) && CompatGLFW.glfwGetKey(mc.getWindow().handle(), this.settings.emergencyKey) == 1) {
                  this.stopTask();
               } else {
                  if (isBusyState(this.runState) && mc.player != null && mc.player.isAlive()) {
                     float health = mc.player.getHealth();
                     if (this.lastHealth >= 0.0F && health < this.lastHealth - 0.01F) {
                        this.lastDamageMs = System.currentTimeMillis();
                        AiEventLog.getInstance()
                           .init(
                              "player_damaged",
                              AiJson.objectOf(
                                 "amount", Math.round((this.lastHealth - health) * 10.0F) / 10.0, "health", Math.round(health * 10.0F) / 10.0
                              )
                           );
                     }

                     this.lastHealth = health;
                     if (isBusyState(this.runState) && shouldClutchFall(mc)) {
                        AiService.ActiveOperation falling = this.activeOperation;
                        if (falling == null || !falling.id().equals("game:mlg")) {
                           if (falling != null) {
                              AiOperation.Result interruption = interruptedFailureResult(
                                 falling.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Falling: the landing is being clutched"
                              );
                              this.releaseOperationResources(falling, true);
                              falling.result().complete(interruption);
                              this.activeOperation = null;
                           }

                           this.startHostOperation(
                              mc,
                              "game:mlg",
                              AiJson.objectOf("method", "auto", "collect", true, "timeoutSeconds", 15),
                              "fall_save",
                              "Falling - clutching the landing."
                           );
                           return;
                        }
                     }

                     AiService.ActiveOperation hazardPending = this.activeOperation;
                     String urgentHazard = AiHazardEscape.init(mc);
                     if (!this.settings.planMode
                        && urgentHazard != null
                        && !AiGuard.isSuppressed()
                        && (hazardPending == null || !hazardPending.id().equals("game:escape_hazard"))
                        && (hazardPending == null || hazardPending.resources().contains("INPUT"))) {
                        if (hazardPending != null) {
                           AiOperation.Result interruption = interruptedFailureResult(
                              hazardPending.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Environmental emergency: " + urgentHazard
                           );
                           this.releaseOperationResources(hazardPending, true);
                           hazardPending.result().complete(interruption);
                           this.activeOperation = null;
                        }

                        AiEventLog.getInstance().init("hazard_reflex", AiJson.objectOf("hazard", urgentHazard, "priority", "before-combat"));
                        if (this.startHostOperation(
                           mc,
                           "game:escape_hazard",
                           AiJson.objectOf("timeoutSeconds", 25),
                           "environmental_escape",
                           "Environmental danger - " + urgentHazard + "; escaping before resuming work."
                        )) {
                           return;
                        }
                     }

                     AiService.ActiveOperation threatPending = this.activeOperation;
                     if (threatPending != null
                        && isInterruptibleOperation(threatPending.id())
                        && !AiGuard.isSuppressed()
                        && isSafeStanceForReflex(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                        long now = System.currentTimeMillis();
                        String hazard = environmentalHazardOf(mc.player);
                        if (hazard != null) {
                           boolean announce = now - this.lastHazardAnnounceMs > 10000L;
                           this.lastHazardAnnounceMs = now;
                           AiOperation.Result interrupted = interruptedFailureResult(
                              threatPending.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Environmental hazard interrupted the operation: the player is " + hazard
                           );
                           this.releaseOperationResources(threatPending, true);
                           threatPending.result().complete(interrupted);
                           this.activeOperation = null;
                           AiEventLog.getInstance().init("hazard_interrupt", AiJson.objectOf("hazard", hazard));
                           AiRunTrace trace = AiRunTrace.current();
                           if (trace != null) {
                              trace.recordOpInterrupt("hazard");
                           }

                           if (announce) {
                              this.appendTranscript(
                                 "system",
                                 "Environmental hazard: the player is "
                                    + hazard
                                    + ". Deal with it first (move out, game:bucket water, dig free), then resume the interrupted step."
                              );
                           }

                           if (AiBreathe.init(mc.player)) {
                              this.startHostOperation(
                                 mc, "game:breathe", AiJson.objectOf("timeoutSeconds", 25), "breathe", "Out of air - swimming up to breathe."
                              );
                           }

                           return;
                        }

                        int hostiles = 0;
                        int ranged = 0;

                        for (Entity e : mc.level.entitiesForRendering()) {
                           if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 36.0 && AiThreats.isActiveThreat(mc, e)) {
                              hostiles++;
                           }

                           if (e instanceof Mob m && m.isAlive() && autismclient.ai.engine.AiSight.init(e) && e.distanceToSqr(mc.player) < 256.0 && AiThreats.isActiveThreat(mc, e)) {
                              ranged++;
                           }
                        }

                        AiDanger.Inner_DataUtil danger = AiDanger.assess(mc);
                        boolean lethalImminent = wouldExplosionKill(danger.worstSource());
                        boolean projectileThreat = AiProjectiles.init(mc).stream().anyMatch(AiProjectiles::init);
                        boolean unsafeMeal = threatPending.id().equals("game:eat") && (AiCombatPriority.init(hostiles, ranged) || AiSurvival.nearestThreat(mc) != null);
                        if ((
                              isUnderAttackPressure(hostiles, health, now - this.lastDamageMs)
                                 || isUnderAttackPressure(ranged, health, now - this.lastDamageMs)
                                 || unsafeMeal
                                 || lethalImminent
                                 || projectileThreat
                           )
                           && (lethalImminent || projectileThreat || health <= 8.0F || threatPending.operation().canInterruptNow())) {
                           boolean announce = now - this.lastThreatAnnounceMs > 10000L;
                           this.lastThreatAnnounceMs = now;
                           if (!projectileThreat) {
                              this.recentInterruptions.addLast(now);
                           }

                           while (!this.recentInterruptions.isEmpty() && now - this.recentInterruptions.peekFirst() > 60000L) {
                              this.recentInterruptions.pollFirst();
                           }

                           AiOperation.Result interrupted = interruptedFailureResult(
                              threatPending.operation(), AiFailure.THREAT_INTERRUPT, "Threat interrupted the operation; controls released for combat or evasion"
                           );
                           this.releaseOperationResources(threatPending, true);
                           threatPending.result().complete(interrupted);
                           this.activeOperation = null;
                           AiEventLog.getInstance()
                              .init(
                                 "threat_interrupt",
                                 AiJson.objectOf("hostiles", hostiles, "ranged", ranged, "lethal", lethalImminent, "projectile", projectileThreat)
                              );
                           AiRunTrace trace = AiRunTrace.current();
                           if (trace != null) {
                              trace.recordOpInterrupt("threat");
                           }

                           if (!projectileThreat && this.recentInterruptions.size() >= 3) {
                              int interruptionCount = this.recentInterruptions.size();
                              this.recentInterruptions.clear();
                              if (this.startHostOperation(
                                 mc,
                                 "game:flee",
                                 AiJson.objectOf("radius", 24, "distance", 48, "timeoutSeconds", 30),
                                 "threat_escalation_flee",
                                 "Repeated threats - leaving the area."
                              )) {
                                 return;
                              }

                              this.transitionRunState(
                                 AiService.RunState.PAUSED,
                                 "Repeated threat interruptions ("
                                    + interruptionCount
                                    + " within a minute): the area is too dangerous for this objective. Clear the threat or move elsewhere, then resume."
                              );
                              this.recentInterruptions.clear();
                              this.threatEscalatedAt = System.currentTimeMillis();
                              if (trace != null) {
                                 trace.recordOpInterrupt("threat_escalation_pause");
                              }

                              return;
                           }

                           if (projectileThreat) {
                              this.startHostOperation(
                                 mc,
                                 "game:dodge",
                                 AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4),
                                 "projectile_reflex",
                                 "Incoming projectile - reacting immediately, then resuming the objective."
                              );
                           } else {
                              this.startThreatReflex(mc, threatPending, health, hostiles);
                           }

                           if (announce) {
                              this.appendTranscript(
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
                           boolean announce = now - this.lastHungerAnnounceMs > 10000L;
                           this.lastHungerAnnounceMs = now;
                           this.releaseOperationResources(threatPending, true);
                           threatPending.result()
                              .complete(
                                 AiOperation.Result.failed(
                                    AiFailure.HUNGER, "Hunger interrupted the operation; food level " + food + "/20 with edible food in inventory", "{}"
                                 )
                              );
                           this.activeOperation = null;
                           AiEventLog.getInstance().init("hunger_interrupt", AiJson.objectOf("food", food));
                           this.startHostOperation(
                              mc,
                              "game:eat",
                              AiJson.objectOf("timeoutSeconds", 15),
                              "food_reflex",
                              "Critical hunger - eating safely before resuming work."
                           );
                           AiRunTrace trace = AiRunTrace.current();
                           if (trace != null) {
                              trace.recordOpInterrupt("hunger");
                           }

                           if (announce) {
                              this.appendTranscript(
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
                        && !this.settings.planMode
                        && !AiGuard.isSuppressed()
                        && idleNow - this.lastIdleThreatMs > 1000L
                        && isSafeStanceForReflex(mc.player.onGround(), mc.player.isInWater(), mc.player.onClimbable())) {
                        int hostiles = 0;
                        int ranged = 0;

                        for (Entity e : mc.level.entitiesForRendering()) {
                           if (e instanceof Enemy && e.isAlive() && e.distanceToSqr(mc.player) < 36.0 && AiThreats.isActiveThreat(mc, e)) {
                              hostiles++;
                           }

                           if (e instanceof Mob m && m.isAlive() && autismclient.ai.engine.AiSight.init(e) && e.distanceToSqr(mc.player) < 256.0 && AiThreats.isActiveThreat(mc, e)) {
                              ranged++;
                           }
                        }

                        boolean lethalImminent = wouldExplosionKill(AiDanger.assess(mc).worstSource());
                        boolean projectileThreat = AiProjectiles.init(mc).stream().anyMatch(AiProjectiles::init);
                        if (isUnderAttackPressure(hostiles, health, idleNow - this.lastDamageMs)
                           || isUnderAttackPressure(ranged, health, idleNow - this.lastDamageMs)
                           || lethalImminent
                           || projectileThreat) {
                           this.lastIdleThreatMs = idleNow;
                           AiEventLog.getInstance()
                              .init(
                                 "idle_threat",
                                 AiJson.objectOf("hostiles", hostiles, "ranged", ranged, "lethal", lethalImminent, "projectile", projectileThreat)
                              );
                           if (projectileThreat) {
                              this.startHostOperation(
                                 mc,
                                 "game:dodge",
                                 AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4),
                                 "projectile_reflex",
                                 "Incoming projectile - reacting while the objective thinks."
                              );
                           } else {
                              this.startThreatReflex(mc, null, health, Math.max(hostiles, ranged));
                           }

                           return;
                        }
                     }
                  }

                  AiService.ActiveOperation duskPending = this.activeOperation;
                  if (duskPending == null
                     && mc.player != null
                     && mc.player.isAlive()
                     && isDuskTime(AiSleep.dayTime())
                     && shouldBurrowAtDusk(
                        AiSleep.dayTime(),
                        isInOverworld(mc),
                        hasBedAvailable(mc),
                        duskPending != null && !duskPending.hostParked(),
                        isBusyState(this.runState),
                        this.settings.planMode
                     )
                     && isUnderOpenSky(mc)) {
                     long dayIndex = mc.level.getOverworldClockTime() / 24000L;
                     long nowMs = System.currentTimeMillis();
                     if (dayIndex != this.lastBurrowDay
                        && nowMs - this.lastBurrowMs >= 30000L
                        && this.startHostOperation(
                           mc, "game:burrow", AiJson.objectOf("timeoutSeconds", 600), "night_burrow", "Night falls - burrowing until dawn."
                        )) {
                        this.lastBurrowDay = dayIndex;
                        this.lastBurrowMs = nowMs;
                        AiRunTrace trace = AiRunTrace.current();
                        if (trace != null) {
                           trace.recordSplit("night_burrow", "");
                        }
                     }
                  }

                  if (mc.player != null
                     && mc.player.isAlive()
                     && isBusyState(this.runState)
                     && AiBreathe.init(mc.player)
                     && (this.activeOperation == null || this.activeOperation.hostParked() || !AiGuard.isSuppressed())) {
                     AiService.ActiveOperation reflex = this.activeOperation;
                     if (reflex != null && reflex.hostParked() && !reflex.id().equals("game:breathe") && !reflex.id().equals("game:escape_hazard")) {
                        this.releaseOperationResources(reflex, true);
                        reflex.result().complete(AiOperation.Result.failed(AiFailure.ENVIRONMENTAL_HAZARD, "Out of air", "{}"));
                        this.activeOperation = null;
                     } else if (reflex != null && !reflex.hostParked() && !isInterruptibleOperation(reflex.id())) {
                        AiOperation.Result interrupted = interruptedFailureResult(
                           reflex.operation(), AiFailure.ENVIRONMENTAL_HAZARD, "Out of air under water: stopped to swim up; run it again from the surface"
                        );
                        this.releaseOperationResources(reflex, true);
                        reflex.result().complete(interrupted);
                        this.activeOperation = null;
                     }

                     if (this.activeOperation == null) {
                        this.startHostOperation(
                           mc, "game:breathe", AiJson.objectOf("timeoutSeconds", 25), "breathe", "Out of air - swimming up to breathe."
                        );
                     }
                  }

                  if (this.auditAfloat(mc)) {
                     mc.options.keyJump.setDown(true);
                     this.swimJumpHeld = true;
                  } else {
                     this.auditReleaseSwimJump(mc);
                  }

                  if (this.activeOperation == null
                     && this.tickCounter % 20 == 0
                     && mc.player != null
                     && mc.player.isAlive()
                     && shouldSleepNow(AiSleep.load(), isInOverworld(mc), false, isBusyState(this.runState), this.settings.planMode)) {
                     long dayIndex = mc.level.getOverworldClockTime() / 24000L;
                     long nowMs = System.currentTimeMillis();
                     if (dayIndex != this.lastSleepDay
                        && nowMs - this.lastSleepMs >= 30000L
                        && hasBedAvailable(mc)
                        && this.startHostOperation(
                           mc, "game:sleep", AiJson.objectOf("timeoutSeconds", 60), "night_sleep", "Night falls - sleeping in the bed nearby."
                        )) {
                        this.lastSleepDay = dayIndex;
                        this.lastSleepMs = nowMs;
                        AiRunTrace trace = AiRunTrace.current();
                        if (trace != null) {
                           trace.recordSplit("night_sleep", "");
                        }
                     }
                  }

                  AiService.ActiveOperation current = this.activeOperation;
                  if (current != null) {
                     boolean liveOperation = this.runState == AiService.RunState.RUNNING
                        || this.runState == AiService.RunState.CHATTING && current.hostParked();
                     if (current.generation() == this.runGeneration.get() && liveOperation) {
                        AiCatalog.Entry contract = this.catalog.snapshot().get(current.id());
                        if (contract != null && contract.fingerprint().equals(current.fingerprint())) {
                           try {
                              if (this.settings.stopOnInput && !Boolean.getBoolean("autism.ai.livetask") && this.isUserPressingInput(mc)) {
                                 this.transitionRunState(AiService.RunState.PAUSED, "Manual game input; AI controls released");
                                 return;
                              }

                              AiOperation.Result result = current.operation().tick();
                              this.stateReason = result.message();
                              this.operationDataJson = result.dataJson();
                              autismclient.ai.engine.AiMotionAudit.init(mc, current.id(), result.message());
                              if (this.tickCounter % 20 == 0 && !result.terminal()) {
                                 AiService.SessionState s = this.currentSession();
                                 synchronized (s) {
                                    JsonObject next = s.structuredTask.deepCopy();
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
                                    s.structuredTask = next;
                                 }
                              }

                              if (result.terminal()) {
                                 autismclient.ai.engine.AiMotionAudit.finishOperation();
                                 this.activeOperation = null;
                                 this.releaseOperationResources(current, false);
                                 AiService.SessionState s = this.currentSession();
                                 if (s != null && s.structuredTask.has("activeOperation")) {
                                    synchronized (s) {
                                       JsonObject next = s.structuredTask.deepCopy();
                                       next.remove("activeOperation");
                                       s.structuredTask = next;
                                    }
                                 }

                                 if (this.runState == AiService.RunState.CHATTING && current.hostParked()) {
                                    this.stateReason = this.getChatModeSummary();
                                 }

                                 current.result().complete(result);
                              }
                           } catch (Throwable e) {
                              AutismClientAddon.LOG.warn("[AI] operation {} threw", current.id(), e);
                              this.releaseOperationResources(current, true);
                              this.activeOperation = null;
                              current.result().completeExceptionally(e);
                           }
                        } else {
                           this.releaseOperationResources(current, true);
                           current.result()
                              .complete(AiGameOperations.result(AiOperation.Status.CANCELLED, "Capability changed during execution", new JsonObject()));
                           this.activeOperation = null;
                        }
                     } else {
                        this.revertSettingDeltas();
                     }
                  }
               }
            }
         }
      }
   }

   static boolean isSpeedrunOperation(String operationId) {
      return "game:speedrun".equals(operationId);
   }

   public void pauseOnUserInput(String reason) {
      if (!Boolean.getBoolean("autism.ai.livetask")
         && this.settings.stopOnInput
         && !autismclient.modules.FreecamController.isActive()
         && isBusyState(this.runState)
         && this.activeOperation != null) {
         this.transitionRunState(AiService.RunState.PAUSED, reason);
      }
   }

   public boolean isBlockingManualInput() {
      AiService.ActiveOperation p = this.activeOperation;
      return !this.settings.stopOnInput && p != null && isBusyState(this.runState) && p.resources().contains("INPUT");
   }

   public boolean isActivelyDriving() {
      return isExecutingState(this.runState)
         || this.inputReleasesInFlight > 0
         || this.activeOperation != null && this.activeOperation.resources().contains("INPUT");
   }

   private boolean isUserPressingInput(Minecraft mc) {
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

   private void logWorldEvents(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         if (!mc.player.isAlive() && this.deathArmed) {
            this.deathArmed = false;
            BlockPos p = mc.player.blockPosition();
            AiEventLog.getInstance()
               .init("death", AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ(), "dimension", mc.level.dimension().identifier().toString()));
         } else if (mc.player.isAlive() && !this.deathArmed) {
            this.deathArmed = true;
            BlockPos p = mc.player.blockPosition();
            AiEventLog.getInstance().init("respawn", AiJson.objectOf("x", p.getX(), "y", p.getY(), "z", p.getZ()));
         }

         if (this.tickCounter % 5 == 0) {
            Set<Integer> now = new HashSet<>();

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Monster m && e.isAlive() && m.getTarget() == mc.player) {
                  now.add(e.getId());
                  if (this.targetingMonsterIds.add(e.getId())) {
                     AiEventLog.getInstance()
                        .init(
                           "entity_attacking", AiJson.objectOf("id", e.getId(), "type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString())
                        );
                  }
               }
            }

            this.targetingMonsterIds.retainAll(now);
         }

         if (this.tickCounter % 20 == 0) {
            Map<String, Integer> counts = new LinkedHashMap<>();

            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty()) {
                  counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
               }
            }

            if (this.lastInventoryCounts != null) {
               TreeMap<String, Integer> merged = new TreeMap<>(counts);
               this.lastInventoryCounts.forEach((item, count) -> merged.merge(item, -count, Integer::sum));
               merged.forEach((item, delta) -> {
                  if (delta > 0) {
                     AiEventLog.getInstance().init("item_obtained", AiJson.objectOf("item", item, "count", delta));
                  } else if (delta < 0) {
                     AiEventLog.getInstance().init("item_lost", AiJson.objectOf("item", item, "count", -delta));
                  }
               });
            }

            this.lastInventoryCounts = counts;
         }
      }
   }

   private void cancelActiveOperation() {
      if (!AiClientTick.init()) {
         AiClientTick.execute(this::cancelActiveOperation);
      } else {
         AiService.ActiveOperation current = this.activeOperation;
         this.activeOperation = null;
         if (current != null) {
            this.releaseOperationResources(current, true);
            current.result().complete(AiOperation.Result.failed(AiFailure.CANCELLED, "Operation cancelled", "{}"));
         }
      }
   }

   private void revertSettingDeltas() {
      if (!AiClientTick.init()) {
         AiClientTick.execute(this::revertSettingDeltas);
      } else {
         this.cancelActiveOperation();

         for (int i = this.settingDeltas.size() - 1; i >= 0; i--) {
            AiService.SettingDelta c = this.settingDeltas.get(i);
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

         this.settingDeltas.clear();
      }
   }

   private void releaseOperationResources(AiService.ActiveOperation current, boolean cancel) {
      if (!AiClientTick.init()) {
         AiClientTick.execute(() -> this.releaseOperationResources(current, cancel));
      } else {
         boolean input = current.resources().contains("INPUT");
         if (input) {
            this.inputReleasesInFlight++;
         }

         try {
            if (cancel) {
               current.operation().cancel();
            }
         } catch (Throwable e) {
            this.appendTranscript("error", "Operation cleanup: " + e.getMessage());
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
               this.appendTranscript("error", "Resource release: " + e.getMessage());
            } finally {
               if (input) {
                  this.inputReleasesInFlight--;
               }
            }
         }
      }
   }

   private void fleeAfterRespawn(Minecraft mc) {
      this.startHostOperation(
         mc,
         "game:flee",
         AiJson.objectOf("radius", 16, "distance", 48, "timeoutSeconds", 25),
         "respawn_stabilize",
         "Respawned into danger; moving to safety first."
      );
   }

   private static boolean isInOverworld(Minecraft mc) {
      return mc.level != null && "minecraft:overworld".equals(mc.level.dimension().identifier().toString());
   }

   private static boolean hasBedAvailable(Minecraft mc) {
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

   private static boolean isUnderOpenSky(Minecraft mc) {
      BlockPos pos = mc.player.blockPosition();
      return mc.level.getHeight(Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
   }

   private void startThreatReflex(Minecraft mc, AiService.ActiveOperation interrupted, float health, int hostiles) {
      int weaponSlot = AiItems.bestHotbarWeaponSlot();
      boolean armed = weaponSlot >= 0 && isCombatWeaponId(BuiltInRegistries.ITEM.getKey(mc.player.getInventory().getItem(weaponSlot).getItem()).toString());
      String reflex = threatReflexOperation(
         health, armed, this.settings.planMode, interrupted != null && interrupted.hostParked() && !interrupted.id().equals("game:eat")
      );
      Entity mealThreat = interrupted != null && interrupted.id().equals("game:eat") ? AiSurvival.nearestThreat(mc) : null;
      if (mealThreat != null && AiThreats.classify(mealThreat) == AiThreats.Inner_DataUtil.AVOID && !reflex.isEmpty()) {
         reflex = "game:flee";
      }

      if (reflex.equals("game:flee") && health >= 8.0F && AiGuard.isOnlySmallFastMeleeThreat()) {
         reflex = "game:fight";
      }

      if (reflex.equals("game:fight") && mealThreat != null) {
         this.startHostOperation(
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
         this.startHostOperation(
            mc,
            "game:fight",
            AiJson.objectOf("target", "hostiles", "count", Math.max(1, hostiles), "radius", 12, "timeoutSeconds", 15),
            "threat_reflex_fight",
            "Under attack - defending while the objective thinks."
         );
      } else if (reflex.equals("game:flee")) {
         this.startHostOperation(
            mc,
            "game:flee",
            AiJson.objectOf("radius", 16, "distance", 40, "timeoutSeconds", 20),
            "threat_reflex_flee",
            "Under attack - moving away while the objective thinks."
         );
      }
   }

   private boolean startHostOperation(Minecraft mc, String id, JsonObject args, String traceKind, String chatLine) {
      AiCatalog.Entry entry = this.getCatalogSnapshot().get(id);
      if (entry != null && this.activeOperation == null) {
         LocalPlayer rider = mc.player;
         if (rider != null && !rider.isPassenger() && !rider.isFallFlying()) {
            AiAssistant.getInstance().pop();
            AutoCloseable lease = MacroExecutor.acquireExternalResources(entry.contract().resources());
            if (lease == null) {
               return false;
            }

            AiOperation inner;
            try {
               inner = AiModuleScope.load(() -> {
                  AiModuleScope.init();
                  return AiGuard.allowDuringLowHealth(Objects.requireNonNull(AiTools.operation(id, args)));
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
               this.operationDataJson = "{}";
               this.activeOperation = new AiService.ActiveOperation(
                  this.runGeneration.get(), id, entry.fingerprint(), op, new CompletableFuture<>(), lease, entry.contract().resources(), true
               );
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordOpInterrupt(traceKind);
               }

               this.appendTranscript("system", chatLine);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private synchronized void stopRunClock() {
      if (this.runClockStartNanos != 0L) {
         AiService.SessionState s = this.currentSession();
         if (s != null) {
            s.elapsedNanos = s.elapsedNanos + (System.nanoTime() - this.runClockStartNanos);
         }

         this.runClockStartNanos = 0L;
      }
   }

   private long currentElapsedNanos() {
      return this.elapsedNanosOf(this.currentSession());
   }

   private long elapsedNanosOf(AiService.SessionState s) {
      return s == null ? 0L : s.elapsedNanos + (s.id.equals(this.currentSessionId) && this.runClockStartNanos != 0L ? System.nanoTime() - this.runClockStartNanos : 0L);
   }

   void checkGeneration(long g) {
      if (g == this.runGeneration.get() && isExecutingState(this.runState)) {
         if (this.scriptDeadlineNanos != 0L && System.nanoTime() > this.scriptDeadlineNanos) {
            throw new IllegalStateException("Script deadline reached");
         } else {
            AiService.SessionState s = this.currentSession();
            if (s.tokens >= this.settings.maxTokens || this.elapsedNanosOf(s) > s.taskMinutes * 60000000000L) {
               throw new IllegalStateException("Task usage/time limit reached; adjust limits before resuming");
            }
         }
      } else {
         throw new CancellationException();
      }
   }

   <T> T onClientThread(long g, Supplier<T> action) throws InterruptedException, ExecutionException {
      this.checkGeneration(g);
      CompletableFuture<T> result = AiClientTick.supply(() -> {
         long start = System.nanoTime();

         try {
            this.checkGeneration(g);
            return action.get();
         } finally {
            this.perfMetrics.init(System.nanoTime() - start);
         }
      });

      try {
         while (true) {
            this.checkGeneration(g);

            try {
               return result.get(200L, TimeUnit.MILLISECONDS);
            } catch (TimeoutException var9) {
            }
         }
      } finally {
         result.cancel(false);
      }
   }

   AiObservation observe(long g, String source) throws InterruptedException, ExecutionException {
      AiService.SessionState s = this.currentSession();
      AiObservation o = this.onClientThread(g, () -> {
         if (s.scope.isBlank() || s.scope.equals("offline")) {
            s.scope = this.worldEpochKey;
         }

         if (!s.scope.equals(this.worldEpochKey)) {
            throw new IllegalStateException("This objective belongs to another world/epoch. Start a new objective for the current world.");
         }

         if (s.wheatBaseline < 0) {
            s.wheatBaseline = AiGameState.countInventoryItem("minecraft:wheat");
         }

         return AiGameState.observe(this.observationSession, this.observationScope, source, this.currentElapsedNanos() / 1000000000L, s.taskMinutes);
      });
      this.latestObservation = o;

      try {
         this.memoryStore.init(o);
      } catch (IOException e) {
         this.appendTranscript("error", "Observation retained in this session; journal write failed: " + e.getMessage());
      }

      return o;
   }

   private void awaitParkedOperation(long g) throws InterruptedException {
      long until = System.nanoTime() + 660000000000L;
      String shown = null;

      while (true) {
         AiService.ActiveOperation p = this.activeOperation;
         if (this.activeOperation == null || !p.hostParked() || System.nanoTime() >= until) {
            return;
         }

         this.checkGeneration(g);
         if (!p.id().equals(shown)) {
            shown = p.id();
            this.stateReason = "Waiting for the survival reflex (" + p.id() + ") to finish";
         }

         Thread.sleep(250L);
      }
   }

   JsonObject runOperation(long g, String id, String fingerprint, Supplier<AiOperation> factory) throws InterruptedException, ExecutionException {
      if (!AutismLiteVariant.enabled() && !PacketUtil.init()) {
         throw new IllegalStateException("Autism is locked: early access requires the donator role.");
      }

      this.awaitParkedOperation(g);
      CompletableFuture<AiOperation.Result> future = this.onClientThread(
         g,
         () -> {
            AiCatalog.Entry fresh = this.getCatalogSnapshot().get(id);
            if (fresh == null || !fresh.fingerprint().equals(fingerprint)) {
               throw new IllegalArgumentException("Capability changed before dispatch; inspect it again");
            }

            if (this.activeOperation != null) {
               throw new IllegalStateException("Another operation owns client controls");
            }

            if (isGameplayActionOperation(id)) {
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
               AiAssistant.getInstance().pop();
            }

            AutoCloseable lease = () -> {};
            if (!id.startsWith("macro:") && !id.equals("client:run_macro")) {
               lease = MacroExecutor.acquireExternalResources(this.getCatalogSnapshot().get(id).contract().resources());
               if (lease == null) {
                  throw new IllegalStateException("Another controller owns required macro resources");
               }
            }

            CompletableFuture<AiOperation.Result> f = new CompletableFuture<>();

            try {
               this.operationDataJson = "{}";
               AiOperation operation = AiModuleScope.load(() -> {
                  if (id.startsWith("game:") && fresh.contract().classification() != AiCapabilityDescriptor.Classification.OBSERVE) {
                     AiModuleScope.init();
                  }

                  return factory.get();
               });
               this.activeOperation = new AiService.ActiveOperation(
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
      AiEventLog.getInstance().init("op_started", AiJson.objectOf("id", id));

      AiOperation.Result result;
      while (true) {
         this.checkGeneration(g);

         try {
            result = future.get(200L, TimeUnit.MILLISECONDS);
            break;
         } catch (TimeoutException var15) {
         }
      }

      if (result.status() == AiOperation.Status.SUCCEEDED) {
         AiEventLog.getInstance().init("op_completed", AiJson.objectOf("id", id, "status", result.status().name()));
      } else {
         AiEventLog.getInstance().init("op_failed", AiJson.objectOf("id", id, "status", result.status().name(), "failure", result.failure().name()));
      }

      AiObservation full = this.observe(g, result.status() == AiOperation.Status.SUCCEEDED ? "verified-operation" : "operation-result");
      AiObservation evidence = new AiObservation(
         full.id(),
         full.session(),
         full.scope(),
         full.observedAt(),
         full.source(),
         AiGameState.compactState(AiJson.parseObject(full.dataJson()).getAsJsonObject()).toString()
      );
      if (result.status() == AiOperation.Status.SUCCEEDED) {
         this.evidenceById.put(evidence.id(), evidence);
         this.evidenceCapability.put(evidence.id(), id);
         this.lastEvidenceId = evidence.id();

         try {
            this.memoryStore
               .init(
                  AiMemoryStore.Inner_DataUtil.WORLD,
                  this.observationScope,
                  "Observed successful capability " + id + ": " + result.message(),
                  evidence.id(),
                  this.getCatalogSnapshot().revision(),
                  result.dataJson()
               );
         } catch (Exception e) {
            this.appendTranscript("error", "Verified result retained; memory could not be saved: " + e.getMessage());
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
            AiJson.objectOf("code", failure.name(), "recoverable", failure.isRecoverable(), "suggestedActions", AiJson.GSON.toJsonTree(failure.suggestedActions()))
         );
      }

      try {
         JsonObject journaled = output.deepCopy();
         journaled.add("evidence", AiJson.GSON.toJsonTree(evidence));
         JsonObject entry = AiJson.objectOf("capability", id, "result", journaled);
         entry.addProperty("sessionId", this.currentSessionId);
         this.memoryStore.journal("operation", entry);
      } catch (IOException e) {
         this.appendTranscript("error", "Operation journal write failed: " + e.getMessage());
      }

      return output;
   }

   AiOperation moduleOperation(String id, final JsonObject args) {
      final Module m = autismclient.modules.al.init_Module_Str(id);
      if (m != null && m.settingsShareable() && !id.equals("ai-assistant")) {
         if (AiJson.getBoolean(args, "enabled", false)) {
            String restricted = moduleBlockReason(id, this.settings);
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
               this.recordSettingDelta(m, key, before, value, revision);
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
            int verifyTicks;
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               if (this.cancelled) {
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
               return ++this.verifyTicks < 2
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
               this.cancelled = true;
            }
         };
      } else {
         throw new IllegalArgumentException("Module settings are private or unavailable");
      }
   }

   static String moduleBlockReason(String moduleId, AiSettings s) {
      if (Set.of("flight", "boat-fly").contains(moduleId) && !s.allowFly) {
         return "'" + moduleId + "' is a detectable flight feature and is disabled; enable 'Detectable: flight' in AI Advanced settings to allow it";
      } else {
         return "no-clip".equals(moduleId) && !s.allowClips
            ? "'no-clip' is a detectable clip feature and is disabled; enable 'Detectable: clips' in AI Advanced settings to allow it"
            : null;
      }
   }

   private void recordSettingDelta(Module m, String key, String before, String after, long previousRevision) {
      for (int i = this.settingDeltas.size() - 1; i >= 0; i--) {
         AiService.SettingDelta old = this.settingDeltas.get(i);
         if (old.module().equals(m.id()) && old.key().equals(key)) {
            if (old.revision() == previousRevision) {
               before = old.before();
            }

            this.settingDeltas.remove(i);
            break;
         }
      }

      this.settingDeltas.add(new AiService.SettingDelta(m.id(), key, before, after, m.controlRevision(key)));
   }

   private void set(String text) {
      this.lastUserObservation = new AiObservation(
         UUID.randomUUID().toString(), this.observationSession, "user", System.currentTimeMillis(), "user", AiJson.objectOf("request", text).toString()
      );

      try {
         this.memoryStore.init(this.lastUserObservation);
         if (text.toLowerCase(Locale.ROOT).matches("(?s).*\\b(i prefer|remember that|from now on|always use|never use)\\b.*")) {
            this.memoryStore
               .init(
                  AiMemoryStore.Inner_DataUtil.USER,
                  "user",
                  text,
                  this.lastUserObservation.id(),
                  this.getCatalogSnapshot().revision(),
                  "Explicit user statement; newer corrections take precedence"
               );
         }
      } catch (Exception e) {
         this.appendTranscript("error", "User statement is available for this task; memory write failed: " + e.getMessage());
      }
   }

   void updateTaskState(String value) {
      if (value.length() > 12000) {
         throw new IllegalArgumentException("Task state too long");
      }

      this.currentSession().taskState = value;
      this.persistCurrentSession();
   }

   void setTaskState(JsonObject value) {
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

            if (this.verifyOutcomes && status.equals("verified") && !this.evidenceById.containsKey(AiJson.getString(goal, "evidenceId", ""))) {
               throw new IllegalArgumentException("Verified subgoals need the evidence id of a SUCCEEDED perform; mark it running or pending until one exists");
            }
         }
      }

      AiService.SessionState s = this.currentSession();
      synchronized (s) {
         s.structuredTask = AiJson.auditKeepHostKeys(s.structuredTask, next);
      }

      this.updateTaskState(AiJson.getString(value, "state", s.taskState));
   }

   private AiService.PromptContextBundle buildPromptContext(AiService.SessionState s, boolean planning, int promptBudget) {
      AiCatalog.Snapshot contracts = this.getCatalogSnapshot();
      List<AiMemoryStore.Memory> memories = this.memoryStore.recall(this.observationScope, s.objective + " " + String.join(" ", s.updates));
      int systemChars = (int)Math.min(2147483647L, (long)(promptBudget * 3.5 * 0.6));
      AiContext.Inner_StreamUtil prompt = AiContext.init(
         contracts,
         s.objective,
         List.copyOf(s.updates),
         s.taskState,
         planning,
         this.settings.vision,
         this.verifyOutcomes,
         memories,
         System.currentTimeMillis(),
         systemChars
      );
      return new AiService.PromptContextBundle(prompt, new AiContext.Inner_DataUtil(), contracts.revision());
   }

   static long historyCharBudget(long systemChars, int promptBudget) {
      double perToken = 3.5;
      long room = (long)(0.55 * promptBudget * perToken) - systemChars - 16000L;
      return Math.max((long)(0.15 * promptBudget * perToken), room);
   }

   private static long historyCharBudgetFor(AiService.PromptContextBundle epoch, int promptBudget) {
      return historyCharBudget(epoch.prompt().stable().length() + epoch.prompt().objective().length(), promptBudget);
   }

   private static long estimatePromptTokens(AiService.PromptContextBundle epoch, List<JsonObject> history) {
      return AiContext.estimateTokens(epoch.prompt().stable().length() + epoch.prompt().objective().length() + AiContext.init(AiJson.GSON.toJsonTree(history)));
   }

   private boolean compactHistoryIfNeeded(AiService.SessionState s, String sid, List<JsonObject> history, AiProvider transport, long budgetTokens, boolean forced) {
      long total = AiContext.init(AiJson.GSON.toJsonTree(history));
      boolean keepLastThinking = transport.provider() == AiSettings.Provider.ANTHROPIC
         && AiSettings.anthropicThinking(transport.model()) == AiSettings.Thinking.BUDGET;
      AiHistory.Inner_StreamUtil cut = AiHistory.init(
         transport.provider(), history, (long)(0.35 * budgetTokens * 3.5), (long)(0.6 * budgetTokens * 3.5), keepLastThinking
      );
      if (cut == null) {
         return false;
      }

      if (!forced && cut.dropped().stream().noneMatch(AiHistory::isAssistant)) {
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
         this.memoryStore.journal("archived-provider-history", entry);
      } catch (IOException e) {
         this.appendToSession(s, "error", "Archived history retained in session; journal write failed: " + e.getMessage());
      }

      history.clear();
      history.add(transport.userMessage(AiHistory.init(s.objective, List.copyOf(s.updates), s.taskState, cut.digest())));
      history.addAll(cut.kept());
      this.appendToSession(s, "system", "Conversation compacted to fit the context window (" + cut.dropped().size() + " older messages summarized)");
      return true;
   }

   private void runAgentLoop(long g, boolean resume, String note) {
      AiService.SessionState s = this.currentSession();
      String sid = s.id;
      AiSettings pinned = this.settings.copy();
      this.verifyOutcomes = pinned.verifyOutcomes;
      this.lastPerformStatus = null;
      String hostNote = null;
      if (pinned.builtIn) {
         if (this.runState == AiService.RunState.PLANNING) {
            this.runState = AiService.RunState.RUNNING;
         }

         if (!this.runBuiltInRequest(g, s, true).finished()) {
            this.runState = AiService.RunState.BLOCKED;
            this.stateReason = "Not something the built-in assistant understands";
            this.appendToSession(s, "warning", builtInUnsupportedMessage(s.objective));
            this.finalizeRun(g, s);
         }
      } else {
         if (!resume && this.runState == AiService.RunState.RUNNING && pinned.hostIntents) {
            AiService.CompletionNote desk = this.runBuiltInRequest(g, s, false);
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
            this.activeProvider = transport;
            pinned.validate();
            this.set(s.objective);
            s.taskState = USER_EVIDENCE_LINE_PATTERN.matcher(s.taskState).replaceAll("");
            int promptBudget = Math.max(12000, pinned.contextTokens - pinned.outputTokens);
            AiSessions storeRef = this.sessionStore;
            boolean sameWire = s.wire == null || s.wire.equals(pinned.provider.name());
            AiService.PromptContextBundle epoch = this.buildPromptContext(s, this.runState == AiService.RunState.PLANNING, promptBudget);
            if (storeRef != null && sameWire) {
               this.awaitStorageDrain();
               long replay = historyCharBudgetFor(epoch, promptBudget);
               if (!resume) {
                  replay = Math.min(replay, 24000L);
               }

               try {
                  history.addAll(AiHistory.forProvider(pinned.provider, storeRef.init(sid, replay, false)));
               } catch (Exception e) {
                  this.appendToSession(s, "error", "Transcript could not be loaded; continuing from the saved task state: " + e.getMessage());
               }
            } else if (!sameWire) {
               this.appendToSession(s, "system", "The provider changed since this conversation last ran; continuing from the saved task state.");
            }

            s.wire = pinned.provider.name();
            transcript.init();
            if (!history.isEmpty()) {
               if (!resume) {
                  transport.appendUserText(history, "New objective: " + s.objective + (hostNote == null ? "" : "\n\n" + hostNote));
               } else if (note != null) {
                  transport.appendUserText(history, note);
               }
            } else {
               boolean continuation = resume && !s.taskState.isBlank() && !s.taskState.equals("Not started");
               history.add(
                  transport.userMessage(
                     continuation
                        ? s.objective + "\nContinue from this factual task state and current observations: " + s.taskState
                        : (hostNote == null ? s.objective : s.objective + "\n\n" + hostNote)
                  )
               );
            }

            s.pop = estimatePromptTokens(epoch, history);
            int outputLimitRecoveries = 0;
            int contextRecoveries = 0;
            int streamRecoveries = 0;
            int textOnlyNudges = 0;
            boolean forceCompact = false;
            String toolChoice = null;

            while (isExecutingState(this.runState)) {
               this.checkGeneration(g);
               AiObservation current = this.observe(g, "game");

               String amendment;
               while ((amendment = s.amendments.poll()) != null) {
                  transport.appendUserText(history, "User update to active objective: " + amendment);
                  this.set(amendment);
               }

               long budget = Math.max(1, this.settings.contextTokens - this.settings.outputTokens);
               if (forceCompact || s.pop > 0.7 * budget) {
                  if (this.compactHistoryIfNeeded(s, sid, history, transport, budget, forceCompact)) {
                     epoch = this.buildPromptContext(s, this.runState == AiService.RunState.PLANNING, promptBudget);
                     transcript.init();
                     s.pop = estimatePromptTokens(epoch, history);
                  }

                  forceCompact = false;
               }

               if (s.requests >= this.settings.maxRequests) {
                  throw new IllegalStateException("Task request limit reached");
               }

               String revision = this.getCatalogSnapshot().revision();
               transport.appendUserText(
                  history,
                  AiContext.init(
                     epoch.host(),
                     s.taskState,
                     s.structuredTask,
                     s.wheatBaseline,
                     s.taskMinutes,
                     this.elapsedNanosOf(s) / 1000000000L,
                     this.lastUserObservation == null ? null : this.lastUserObservation.id(),
                     revision.equals(epoch.catalogRevision()) ? null : revision,
                     current
                  )
               );
               transcript.init(history);
               transport.applySettings(this.settings);
               this.stateReason = "Thinking";
               s.requests++;
               s.push++;
               StringBuilder live = new StringBuilder();
               AiService.ToolCallGate gate = new AiService.ToolCallGate();
               boolean[] finishedByTool = new boolean[]{false};
               List<String> executedCalls = new ArrayList<>();
               long providerStarted = System.nanoTime();

               AiProvider.Inner_BufferHelper reply;
               try {
                  reply = transport.completeWithContext(epoch.prompt(), history, AiTools.toolDefinitions(this.verifyOutcomes), toolChoice, delta -> {
                     live.append(delta);
                     this.stateReason = live.length() > 240 ? live.substring(live.length() - 240) : live.toString();
                  }, () -> g != this.runGeneration.get(), ready -> {
                     gate.markIssued(ready.index());
                     if (isExecutingState(this.runState) && !"ask_user".equals(ready.call().name())) {
                        JsonObject callResult = this.executeToolCall(s, sid, ready.call(), g);
                        gate.recordResult(ready.index(), callResult);
                        executedCalls.add(describeToolResult(ready.call(), callResult));
                        if (!isExecutingState(this.runState)) {
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
                     transport.appendUserText(history, outputLimitNote(executedCalls));
                     this.checkGeneration(g);
                     continue;
                  }

                  if (AiProvider.init_Z_Str(error) && executedCalls.isEmpty() && contextRecoveries++ < 2) {
                     forceCompact = true;
                     this.appendToSession(s, "system", "The conversation no longer fits the context window; compacting and retrying");
                     this.checkGeneration(g);
                     continue;
                  }

                  if (!executedCalls.isEmpty() && isExecutingState(this.runState) && streamRecoveries++ < 3) {
                     this.appendToSession(s, "system", "The provider connection dropped after the step ran; continuing from its result");
                     transport.appendUserText(history, droppedConnectionNote(executedCalls));
                     this.checkGeneration(g);
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
               long estTokens = reply.tokens() > 0L ? reply.tokens() : Math.max(1L, (transcriptChars - s.unusedResetTime) / 2L);
               s.tokens += estTokens;
               s.unusedResetTime = Math.max(s.unusedResetTime, transcriptChars);
               s.inputTokens = s.inputTokens + usage.inputTokens();
               s.cachedInputTokens = s.cachedInputTokens + usage.cachedInputTokens();
               s.cacheWriteTokens = s.cacheWriteTokens + usage.cacheWriteTokens();
               s.poll = s.poll + usage.outputTokens();
               s.pop = usage.promptTokens() > 0L ? usage.promptTokens() : AiContext.estimateTokens(sent);
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordProviderRequest((System.nanoTime() - providerStarted) / 1000000L, estTokens);
               }

               if (!finishedByTool[0]) {
                  this.checkGeneration(g);
               }

               history.add(reply.wireMessage());
               transcript.init(history);
               String shown = stripSpecialTokens(reply.text());
               if (!shown.isBlank()) {
                  this.appendToSession(s, "assistant", shown);
               }

               toolChoice = null;
               boolean normalStop = reply.finish().isBlank() || Set.of("stop", "STOP", "end_turn", "tool_calls", "tool_use").contains(reply.finish());
               if (reply.calls().isEmpty() && normalStop && textOnlyNudges++ < 2) {
                  this.appendToSession(s, "system", "The reply had no action; asking the model to act instead of describing steps");
                  transport.appendUserText(history, noToolCallNote(this.runState == AiService.RunState.PLANNING));
                  toolChoice = "required";
                  this.checkGeneration(g);
               } else {
                  if (!reply.calls().isEmpty()) {
                     textOnlyNudges = 0;
                  }

                  if (reply.calls().isEmpty()) {
                     this.runState = AiService.RunState.BLOCKED;
                     this.stateReason = "Model ended its response without a tool call; objective remains unverified";
                     if (!reply.finish().isBlank() && !Set.of("stop", "STOP", "end_turn", "tool_calls", "tool_use").contains(reply.finish())) {
                        boolean refused = Set.of("SAFETY", "content_filter", "refusal", "blocklist", "PROHIBITED_CONTENT").contains(reply.finish());
                        this.stateReason = this.stateReason + " (provider finish: " + reply.finish() + (refused ? " - refused by content policy" : "") + ")";
                     }

                     this.appendToSession(s, "warning", this.stateReason);
                     break;
                  }

                  open[0] = new AiService.PendingToolBatch(reply.calls(), gate);
                  int k = 0;

                  while (true) {
                     if (k < reply.calls().size()) {
                        int streamIndex = gate.indexAtPosition(k);
                        label527:
                        if (streamIndex >= 0) {
                           if (gate.resultAt(streamIndex) == null) {
                              if (!isExecutingState(this.runState)) {
                                 break label527;
                              }

                              gate.recordResult(streamIndex, this.executeToolCall(s, sid, reply.calls().get(k), g));
                           }

                           if (isExecutingState(this.runState)) {
                              k++;
                              continue;
                           }
                        }
                     }

                     List<JsonObject> aligned = gate.resultsOrFiller(
                        reply.calls().size(),
                        AiJson.objectOf("status", "CANCELLED", "message", "Turn ended by an earlier finish call; not dispatched")
                     );
                     history.addAll(transport.toolResults(reply.calls(), aligned));
                     open[0] = null;
                     this.persistSession(s);
                     break;
                  }
               }
            }
         } catch (CancellationException var62) {
         } catch (Exception e) {
            if (g == this.runGeneration.get()) {
               this.runState = AiService.RunState.FAILED;
               this.stateReason = Objects.toString(e.getMessage(), "AI task failed");
               this.appendToSession(s, "error", this.stateReason);
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordFinish("failed");
               }
            }
         } finally {
            AiService.PendingToolBatch round = open[0];
            if (round != null) {
               try {
                  history.addAll(
                     transport.toolResults(
                        round.calls(),
                        round.gate()
                           .resultsOrFiller(
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
            if (g == this.runGeneration.get()) {
               this.stopRunClock();
               this.activeProvider = null;
               Minecraft.getInstance().execute(this::revertSettingDeltas);
               if (this.runState == AiService.RunState.COMPLETED && !s.queue.isEmpty()) {
                  String next = s.queue.poll();
                  Minecraft.getInstance().execute(() -> {
                     if (s.id.equals(this.currentSessionId) && !isExecutingState(this.runState)) {
                        this.submitPrompt(next);
                     } else {
                        s.queue.offerFirst(next);
                     }
                  });
               } else {
                  this.refreshChattingState();
               }

               this.persistSession(s);
            } else {
               this.persistSession(s);
            }
         }
      }
   }

   static String outputLimitNote(List<String> executedCalls) {
      String note = "Your previous response was discarded for exceeding output limits. Reply concisely; prefer short tool calls over long reasoning.";
      if (!executedCalls.isEmpty()) {
         note = note
            + " These calls from the discarded response ALREADY EXECUTED and their effects stand - do not re-issue them: "
            + String.join("; ", executedCalls)
            + ".";
      }

      return note;
   }

   static String describeToolResult(AiProvider.Inner_DataUtil call, JsonObject result) {
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

   static String droppedConnectionNote(List<String> executedCalls) {
      return "The connection to you dropped while a step from your last response was running (long operations can outlive it). These calls from that response ALREADY EXECUTED and their effects stand - do not re-issue them: "
         + String.join("; ", executedCalls)
         + ". Continue from the current state.";
   }

   static String stripSpecialTokens(String text) {
      if (text == null) {
         return "";
      }

      if (!text.contains("<|")) {
         return text;
      }

      String out = SPECIAL_TOKEN_PATTERN.matcher(text).replaceAll(" ").strip();
      return out.matches("(?i)(?:response|assistant|answer|tool_calls?|functions?)?") ? "" : out;
   }

   static String noToolCallNote(boolean planning) {
      return planning
         ? "Your last reply had no tool call, and a reply without one ends the run. Keep researching with the allowed tools, or present the plan by calling finish with outcome answered and the full plan as the message."
         : "Your last reply had no tool call, and a reply without one ends the run. Do not tell the user how to do it or list steps for them: you operate the game, so carry out the next step now with a tool call. If the objective is already done, call finish; if the user only asked a question, call finish with outcome answered and the answer as the message; if you need a decision, call ask_user.";
   }

   private JsonObject executeToolCall(AiService.SessionState s, String sid, AiProvider.Inner_DataUtil call, long g) {
      this.checkGeneration(g);
      AiRunTrace trace = AiRunTrace.current();
      long toolStarted = System.nanoTime();
      if (trace != null) {
         trace.recordToolCall(call.name());
      }

      JsonObject result;
      if (call.argumentsError() != null) {
         result = AiJson.objectOf(
            "status", "FAILED", "error", "Model produced malformed tool-call arguments; resend valid JSON", "rawArgumentsExcerpt", call.argumentsError()
         );
      } else {
         try {
            result = s.amendments.isEmpty()
               ? this.tools.call(call.name(), call.arguments(), g)
               : AiJson.objectOf("status", "CANCELLED", "message", "The user amended the objective; this earlier planned call was not dispatched");
         } catch (CancellationException e) {
            throw e;
         } catch (Exception e) {
            String error = Objects.toString(e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), "Operation failed");
            result = AiJson.objectOf("status", "FAILED", "error", error);
         }
      }

      if ("perform".equals(call.name())) {
         this.lastPerformStatus = AiJson.getString(result, "status", "FAILED");
      }

      JsonObject detail = result.deepCopy();
      detail.remove("imagePngBase64");
      this.appendToSession(
         s,
         "tool",
         call.name() + ": " + AiJson.getString(result, "message", AiJson.getString(result, "status", "returned result")),
         detail.toString()
      );

      try {
         JsonObject entry = AiJson.objectOf("name", call.name(), "result", detail);
         entry.addProperty("sessionId", sid);
         this.memoryStore.journal("tool", entry);
      } catch (IOException e) {
         this.appendToSession(s, "error", "Tool result retained in session; journal write failed: " + e.getMessage());
      }

      if (trace != null) {
         trace.recordToolResult(
            call.name(),
            AiJson.getString(result, "status", AiJson.getString(result, "outcome", "returned")),
            result.has("failure") && result.get("failure").isJsonObject() ? AiJson.getString(result.getAsJsonObject("failure"), "code", "") : "",
            (System.nanoTime() - toolStarted) / 1000000L
         );
      }

      return result;
   }

   private AiService.CompletionNote runBuiltInRequest(long g, AiService.SessionState s, boolean builtIn) {
      if (builtIn && s.structuredTask.has("builtInAction")) {
         JsonObject local = s.structuredTask.getAsJsonObject("builtInAction");
         if (!AiLocalRequest.init(local, AiWorldIdentity.currentWorldKey(Minecraft.getInstance()), AiWorldIdentity.currentDimensionId(Minecraft.getInstance()))) {
            return this.endRunWith(
               g, s, AiService.RunState.BLOCKED, "warning", "This placement belongs to another world or dimension. Select the destination again."
            );
         }

         if (local.has("sourceRevision")) {
            JsonObject args = local.getAsJsonObject("arguments");

            boolean unchanged;
            try {
               unchanged = AiLocalRequest.isSourceUnchanged(local, AiSchematic.resolveFile(AiSchematic.init(), AiJson.getString(args, "schematic", "")));
            } catch (IllegalArgumentException missing) {
               unchanged = false;
            }

            if (!unchanged) {
               return this.endRunWith(
                  g, s, AiService.RunState.BLOCKED, "warning", "The schematic changed or was removed. Select it again before building."
               );
            }
         }
      }

      AiIntents.Inner_CachePool plan = builtIn && s.structuredTask.has("builtInAction")
         ? AiLocalRequest.init(s.structuredTask.getAsJsonObject("builtInAction"))
         : AiIntents.plan(s.objective, createWaypointResolver());
      if (plan.steps().isEmpty()) {
         return new AiService.CompletionNote(false, null);
      }

      List<AiIntents.Inner_BufferHelper> steps = new ArrayList<>(plan.steps());
      List<String> done = new ArrayList<>();

      try {
         s.attemptedAction = true;

         for (int i = 0; i < steps.size(); i++) {
            AiIntents.Inner_BufferHelper step = steps.get(i);
            if (!step.asks().isEmpty()) {
               JsonObject args = step.arguments().deepCopy();

               for (AiIntents.Inner_DataUtil ask : step.asks()) {
                  JsonObject reply = this.write(AiJson.objectOf("question", ask.question(), "options", ask.options()), g);
                  String said = AiJson.getString(reply, "answer", "");
                  if (!AiIntents.init(ask, said, args)) {
                     if (builtIn) {
                        return this.endRunWith(
                           g,
                           s,
                           AiService.RunState.BLOCKED,
                           "warning",
                           "The built-in assistant did not understand the answer \"" + said + "\"; pick one of the options"
                        );
                     }

                     this.appendToSession(s, "system", "That answer needs the AI; handing the request over");
                     return new AiService.CompletionNote(
                        false, completedStepsNote(done, s.objective + " (asked \"" + ask.question() + "\", the user answered \"" + said + "\")", null)
                     );
                  }
               }

               steps.set(i, new AiIntents.Inner_BufferHelper(step.clause(), step.capability(), args, describeFarmRequest(args)));
            }
         }

         for (int i = 0; i < steps.size(); i++) {
            this.checkGeneration(g);
            AiIntents.Inner_BufferHelper step = steps.get(i);
            String failure = null;
            int prerequisiteIndex = 0;

            for (AiIntents.Inner_BufferHelper before : this.farmPrerequisiteSteps(g, step)) {
               JsonObject r = this.performIntentStep(g, s, before, i + "/prerequisite/" + prerequisiteIndex++);
               done.add(describeStepResult(before, r));
               if (!isSucceededResult(r) && !this.hasPlantableAlready(g, before)) {
                  failure = before.describe() + " (needed to " + step.describe() + "): " + resultMessageOf(r);
                  break;
               }
            }

            if (failure == null) {
               if ("game:craft".equals(step.capability()) && !this.init_Z_J_ad_BufferHelper(g, step)) {
                  AiIntents.Inner_BufferHelper gather = gatherForCraftStep(step, true);
                  if (gather != null) {
                     step = gather;
                  }
               }

               if (AiJson.getBoolean(step.arguments(), "cookHeld", false)) {
                  step = this.cookRawFoodStep(g, step);
               }

               JsonObject r = this.performIntentStep(g, s, step, i + "/requested");
               done.add(describeStepResult(step, r));
               AiIntents.Inner_BufferHelper gather = isSucceededResult(r) ? null : craftMissingInputsStep(step, r);
               if (gather != null) {
                  this.checkGeneration(g);
                  r = this.performIntentStep(g, s, gather, i + "/fallback");
                  done.add(describeStepResult(gather, r));
               }

               for (int retool = 0; retool < 3; retool++) {
                  AiIntents.Inner_BufferHelper tool = isSucceededResult(r) ? null : missingToolStep(step, r);
                  if (tool == null) {
                     break;
                  }

                  this.checkGeneration(g);
                  JsonObject got = this.performIntentStep(g, s, tool, i + "/tool/" + retool);
                  done.add(describeStepResult(tool, got));
                  if (!isSucceededResult(got)) {
                     break;
                  }

                  step = remainingMineStep(step, r);
                  this.checkGeneration(g);
                  r = this.performIntentStep(g, s, step, i + "/retool/" + retool);
                  done.add(describeStepResult(step, r));
               }

               if (!isSucceededResult(r)) {
                  failure = step.describe() + ": " + resultMessageOf(r);
               }
            }

            if (failure != null) {
               if (builtIn) {
                  return this.endRunWith(g, s, AiService.RunState.FAILED, "error", "Could not finish: " + failure);
               }

               List<String> rest = new ArrayList<>();

               for (AiIntents.Inner_BufferHelper left : steps.subList(i, steps.size())) {
                  rest.add(left.clause().trim());
               }

               if (!plan.remainder().isBlank()) {
                  rest.add(plan.remainder());
               }

               this.appendToSession(s, "warning", "Quick action failed (" + failure + "); the AI takes over");
               return new AiService.CompletionNote(false, completedStepsNote(done, String.join(", then ", rest), failure));
            }
         }

         if (plan.handledAll()) {
            this.set(s.objective);
            this.runState = AiService.RunState.COMPLETED;
            this.stateReason = "Done without an AI turn";
            List<String> what = new ArrayList<>();

            for (AiIntents.Inner_BufferHelper step : steps) {
               what.add(step.describe());
            }

            this.appendToSession(s, "success", "Done without an AI turn: " + String.join("; ", what));
            AiRunTrace trace = AiRunTrace.current();
            if (trace != null) {
               trace.recordFinish("completed");
            }

            this.finalizeRun(g, s);
            return new AiService.CompletionNote(true, null);
         } else {
            if (!builtIn) {
               this.appendToSession(s, "system", "Quick actions done; the AI takes over: " + plan.remainder());
               return new AiService.CompletionNote(false, completedStepsNote(done, plan.remainder(), null));
            }

            List<String> what = new ArrayList<>();

            for (AiIntents.Inner_BufferHelper step : steps) {
               what.add(step.describe());
            }

            return this.endRunWith(
               g, s, AiService.RunState.BLOCKED, "warning", "Done: " + String.join("; ", what) + ". " + builtInUnsupportedMessage(plan.remainder())
            );
         }
      } catch (CancellationException e) {
         this.finalizeRun(g, s);
         return new AiService.CompletionNote(true, null);
      } catch (Exception e) {
         if (builtIn) {
            return this.endRunWith(g, s, AiService.RunState.FAILED, "error", "Stopped: " + e.getMessage());
         }

         this.appendToSession(s, "warning", "Quick actions stopped (" + e.getMessage() + "); the AI takes over");
         return new AiService.CompletionNote(false, completedStepsNote(done, s.objective, null));
      }
   }

   private JsonObject performIntentStep(long g, AiService.SessionState s, AiIntents.Inner_BufferHelper step, String occurrence) throws InterruptedException, ExecutionException {
      JsonObject stocked = AuditBuilderShulkerStock.before(this, g, s, step, occurrence);
      if (stocked != null) {
         return stocked;
      }

      JsonObject arguments = step.arguments();
      if (step.capability().equals("game:obtain") && AiJson.getBoolean(arguments, "additional", false)) {
         String key = occurrence + ":" + step.clause();
         JsonObject current = s.structuredTask;
         JsonObject targets = current.has("hostGetTargets") ? current.getAsJsonObject("hostGetTargets") : new JsonObject();
         if (!targets.has(key)) {
            JsonObject requested = arguments;
            JsonObject target = this.onClientThread(
               g, () -> AiLocalRequest.init(requested, AiItems.countInInventory(AiItems.resolveItem(AiJson.getString(requested, "item", ""))), 2304)
            );
            synchronized (s) {
               JsonObject next = s.structuredTask.deepCopy();
               targets = next.has("hostGetTargets") ? next.getAsJsonObject("hostGetTargets") : new JsonObject();
               targets.add(key, target);
               next.add("hostGetTargets", targets);
               s.structuredTask = next;
            }

            this.persistSession(s);
         }

         arguments = targets.getAsJsonObject(key);
      }

      this.appendToSession(s, "system", "Quick action: " + step.describe() + " (no AI turn)");
      JsonObject r = null;

      for (int attempt = 0; attempt < 6; attempt++) {
         r = this.executeToolCall(
            s,
            s.id,
            new AiProvider.Inner_DataUtil("host-" + UUID.randomUUID(), "perform", AiJson.objectOf("id", step.capability(), "arguments", arguments)),
            g
         );
         if (isSucceededResult(r) || !isResumableInterruption(r)) {
            return r;
         }

         this.appendToSession(s, "system", "Quick action paused (" + resultMessageOf(r) + "); it resumes when the player is safe");
         this.waitForOperationIdle(g, 180);
      }

      return r;
   }

   static boolean isResumableInterruption(JsonObject r) {
      if ("UNCERTAIN".equals(AiJson.getString(r, "status", ""))) {
         return false;
      }

      String code = r.has("failure") && r.get("failure").isJsonObject() ? AiJson.getString(r.getAsJsonObject("failure"), "code", "") : "";
      return Set.of("THREAT_INTERRUPT", "HUNGER", "LOW_HEALTH", "ENVIRONMENTAL_HAZARD").contains(code)
         ? true
         : AiJson.getString(r, "error", "").contains("Another operation owns client controls");
   }

   private void waitForOperationIdle(long g, int seconds) throws InterruptedException {
      long until = System.nanoTime() + seconds * 1000000000L;
      Thread.sleep(500L);

      while (this.activeOperation != null && System.nanoTime() < until) {
         this.checkGeneration(g);
         Thread.sleep(250L);
      }
   }

   private static boolean isSucceededResult(JsonObject r) {
      return "SUCCEEDED".equals(AiJson.getString(r, "status", ""));
   }

   static AiIntents.Inner_BufferHelper craftMissingInputsStep(AiIntents.Inner_BufferHelper step, JsonObject result) {
      if (!"game:craft".equals(step.capability())) {
         return null;
      }

      String reason = AiJson.getString(result, "message", AiJson.getString(result, "error", "")).toLowerCase(Locale.ROOT);
      return !reason.contains("missing") ? null : gatherForCraftStep(step, false);
   }

   private AiIntents.Inner_BufferHelper cookRawFoodStep(long g, AiIntents.Inner_BufferHelper step) throws InterruptedException, ExecutionException {
      JsonObject args = step.arguments().deepCopy();
      args.remove("cookHeld");
      String spec = AiJson.getString(args, "item", "cooked_food");
      int raw = this.onClientThread(g, () -> {
         Minecraft mc = Minecraft.getInstance();
         Set<String> inputs = new LinkedHashSet<>();

         for (Item cooked : AiItems.resolveItem(spec).items()) {
            for (autismclient.util.AutoCraftingEngine.SmeltRecipe plan : autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, cooked)) {
               for (Item input : plan.inputs()) {
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

   static AiIntents.Inner_BufferHelper gatherForCraftStep(AiIntents.Inner_BufferHelper step, boolean additional) {
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
         return this.onClientThread(gen, () -> {
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

   static AiIntents.Inner_BufferHelper missingToolStep(AiIntents.Inner_BufferHelper step, JsonObject result) {
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

   static AiIntents.Inner_BufferHelper remainingMineStep(AiIntents.Inner_BufferHelper step, JsonObject result) {
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

   private AiService.CompletionNote endRunWith(long g, AiService.SessionState s, AiService.RunState end, String role, String text) {
      if (g == this.runGeneration.get()) {
         this.runState = end;
         this.stateReason = text;
      }

      this.appendToSession(s, role, text);
      AiRunTrace trace = AiRunTrace.current();
      if (trace != null) {
         trace.recordFinish(end == AiService.RunState.FAILED ? "failed" : "blocked");
      }

      this.finalizeRun(g, s);
      return new AiService.CompletionNote(true, null);
   }

   static String builtInUnsupportedMessage(String request) {
      return "Built-In can't do \"" + request.trim() + "\" yet. Try something like \"get me a diamond pickaxe\", or pick an AI provider in Settings.";
   }

   private static String resultMessageOf(JsonObject r) {
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

   private static String describeStepResult(AiIntents.Inner_BufferHelper step, JsonObject r) {
      return step.describe() + " -> " + AiJson.getString(r, "status", "?") + ": " + resultMessageOf(r);
   }

   static String completedStepsNote(List<String> done, String rest, String failure) {
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

   private List<AiIntents.Inner_BufferHelper> farmPrerequisiteSteps(long g, AiIntents.Inner_BufferHelper step) throws InterruptedException, ExecutionException {
      if (step.capability().equals("game:farm") && "plant".equals(AiJson.getString(step.arguments(), "action", ""))) {
         String seed = AiFarm.load(AiJson.getString(step.arguments(), "crop", "minecraft:wheat"));
         int want = Math.max(1, AiJson.getInt(step.arguments(), "count", 9));
         int[] have = this.onClientThread(g, () -> new int[]{AiFarmPlant.init(Minecraft.getInstance()) ? 1 : 0, seed == null ? 0 : AiGameState.countInventoryItem(seed)});
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

   private boolean hasPlantableAlready(long g, AiIntents.Inner_BufferHelper before) throws InterruptedException, ExecutionException {
      if (!"game:obtain".equals(before.capability())) {
         return false;
      }

      String item = AiJson.getString(before.arguments(), "item", "");
      return !item.endsWith("seeds") && !item.endsWith("carrot") && !item.endsWith("potato")
         ? false
         : this.onClientThread(g, () -> AiGameState.countInventoryItem(item)) > 0;
   }

   static String describeFarmRequest(JsonObject args) {
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

   private void finalizeRun(long g, AiService.SessionState s) {
      if (g == this.runGeneration.get()) {
         this.stopRunClock();
         this.refreshChattingState();
         Minecraft.getInstance().execute(this::revertSettingDeltas);
         this.persistSession(s);
      }
   }

   static AiIntents.Inner_StreamUtil createWaypointResolver() {
      return new AiIntents.Inner_StreamUtil(name -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            AutismWaypoints.Waypoint wp = AutismWaypoints.getInstance().find(AuditWaypointScope.current(mc), name);
            return wp == null ? null : new int[]{wp.x(), wp.y(), wp.z()};
         } else {
            return null;
         }
      }, AiService::resolveShareableModuleId, AiService::resolveEntityTypeId);
   }

   static String resolveShareableModuleId(String name) {
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

   static String resolveEntityTypeId(String name) {
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

   static String normalizeOutcomeWord(String raw) {
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
      String outcome = normalizeOutcomeWord(AiJson.getString(args, "outcome", ""));
      String text = AiJson.getString(args, "message", "");
      AiService.SessionState s = this.currentSession();
      if (outcome.equals("completed")) {
         if (this.runState == AiService.RunState.PLANNING) {
            throw new IllegalArgumentException(
               "Plan mode: the planning phase cannot complete an objective; finish with outcome 'answered' and the full plan for user approval"
            );
         }

         boolean own = args.has("postcondition") && args.get("postcondition").isJsonObject() && !args.getAsJsonObject("postcondition").isEmpty();
         String last = this.lastPerformStatus;
         if (!this.verifyOutcomes && !own && last != null && !last.equals("SUCCEEDED")) {
            throw new IllegalArgumentException(
               "The last perform ended "
                  + last
                  + ", so the objective is not shown as done. Fix it and perform again, or if it is done anyway finish with a postcondition that proves it (inventory item/count or position x/y/z/radius)"
            );
         }

         if (this.verifyOutcomes || own) {
            JsonObject p = this.verifyOutcomes ? this.init_JsonObject_JsonObject(args) : args.getAsJsonObject("postcondition");
            String unmet = this.onClientThread(g, () -> this.postconditionFailure(p));
            if (unmet != null && (this.verifyOutcomes || last != null && !last.equals("SUCCEEDED"))) {
               throw new IllegalArgumentException("Objective postcondition is not verified: " + unmet);
            }

            String kind = AiJson.getString(p, "kind", "");
            if (unmet == null
               && (kind.equals("beat_game") || kind.equals("stat") && "minecraft.killed:minecraft.ender_dragon".equals(AiJson.getString(p, "stat", "")))) {
               AiRunTrace trace = AiRunTrace.current();
               if (trace != null) {
                  trace.recordSplit("dragon dead", "");
               }
            }
         }

         this.runState = AiService.RunState.COMPLETED;
         this.appendToSession(s, "success", "Objective completed");
      } else if (outcome.equals("answered")) {
         if (this.verifyOutcomes && s.attemptedAction && this.lastEvidenceId == null) {
            throw new IllegalArgumentException(
               "Actions were attempted but none SUCCEEDED yet; finish completed after a successful perform, or blocked with the reason"
            );
         }

         if (this.runState == AiService.RunState.PLANNING) {
            if (text.isBlank()) {
               throw new IllegalArgumentException("Provide the full plan as the finish message");
            }

            s.load(text);
            this.runState = AiService.RunState.PAUSED;
            this.stateReason = "Plan awaiting user approval";
            this.appendTranscript("assistant", text);
            return AiJson.objectOf("outcome", outcome, "message", text);
         }

         this.runState = AiService.RunState.ANSWERED;
      } else {
         this.runState = AiService.RunState.BLOCKED;
         this.appendToSession(s, "warning", "Objective blocked");
      }

      AiRunTrace trace = AiRunTrace.current();
      if (trace != null) {
         trace.recordFinish(outcome);
      }

      this.stateReason = text;
      this.appendTranscript("assistant", text);
      return AiJson.objectOf("outcome", outcome, "message", text);
   }

   private String postconditionFailure(JsonObject p) {
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
            AiObservation proof = this.evidenceById.get(id);
            if (proof != null && proof.session().equals(this.observationSession)) {
               String capability = this.evidenceCapability.get(id);
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

            return AiJson.sha256Hex(AiTools.macroJson(macro).toString()).equals(AiJson.getString(p, "fingerprint", ""))
               ? null
               : "the macro changed since the given fingerprint";
         case "stat": {
            int have = readStatValue(AiJson.getString(p, "stat", ""));
            int want = AiJson.getInt(p, "count", 1);
            return statReached(have, want) ? null : "stat " + AiJson.getString(p, "stat", "") + " is " + have + ", need " + want;
         }
         case "beat_game":
            return statReached(readStatValue("minecraft.killed:minecraft.ender_dragon"), 1) ? null : "the Ender Dragon kill statistic is still 0";
         default:
            return "unknown postcondition kind '" + kind + "' (inventory, position, operation, macro, stat or beat_game)";
      }
   }

   private JsonObject init_JsonObject_JsonObject(JsonObject args) {
      boolean own = args.has("postcondition") && args.get("postcondition").isJsonObject() && !args.getAsJsonObject("postcondition").isEmpty();
      JsonObject p = own ? args.getAsJsonObject("postcondition") : new JsonObject();
      if (!own || AiJson.getString(p, "kind", "").equals("operation") && AiJson.getString(p, "evidenceId", "").isBlank()) {
         String last = this.lastEvidenceId;
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

   public static String extractToolStatus(AiService.TranscriptEntry message) {
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

   public AiService.UserPromptQuestion getPendingQuestion() {
      return this.pendingQuestion;
   }

   public void setPendingQuestion(String text, List<String> options) {
      synchronized (this.questionLock) {
         this.pendingQuestion = text != null && !text.isBlank()
            ? new AiService.UserPromptQuestion(text, options == null ? List.of() : List.copyOf(options), System.currentTimeMillis())
            : null;
         this.questionAnswer = null;
      }
   }

   public void answerPendingQuestion(String text) {
      String a = text == null ? "" : text.trim();
      if (!a.isEmpty()) {
         synchronized (this.questionLock) {
            if (this.pendingQuestion == null) {
               return;
            }

            this.questionAnswer = a;
            this.pendingQuestion = null;
            this.questionLock.notifyAll();
         }

         this.appendTranscript("user", a);
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

      AiService.SessionState s = this.currentSession();
      this.appendToSession(s, "assistant", text);
      synchronized (this.questionLock) {
         this.pendingQuestion = new AiService.UserPromptQuestion(text, List.copyOf(options), System.currentTimeMillis());
         this.questionAnswer = null;
      }

      this.stateReason = "Waiting for your answer";

      try {
         while (true) {
            this.checkGeneration(g);
            synchronized (this.questionLock) {
               if (this.questionAnswer != null) {
                  String a = this.questionAnswer;
                  this.questionAnswer = null;
                  return AiJson.objectOf("status", "ANSWERED", "answer", a, "option", options.indexOf(a) + 1);
               }

               try {
                  this.questionLock.wait(200L);
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  throw new CancellationException();
               }
            }
         }
      } finally {
         synchronized (this.questionLock) {
            this.pendingQuestion = null;
         }
      }
   }

   static boolean statReached(int statValue, int count) {
      return statValue >= count;
   }

   static String[] parseStatId(String statId) {
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

   static int readStatValue(String statId) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      if (mc.getConnection() != null) {
         mc.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
      }

      String[] ids = parseStatId(statId);
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
      try (AiScriptHost host = new AiScriptHost(this.memoryStore.directory(), (name, input) -> {
            throw new IllegalStateException("Fixture checks never call live game tools");
         })) {
         this.scriptHost = host;
         return host.init(
            AiJson.getString(args, "source", ""),
            args.has("fixtures") ? args.getAsJsonArray("fixtures") : new JsonArray(),
            () -> g != this.runGeneration.get()
         );
      } finally {
         this.scriptHost = null;
      }
   }

   JsonObject sync(JsonObject args, long g) throws Exception {
      if (!this.settings.unrestrictedScripts) {
         throw new IllegalStateException("Script execution disabled in AI settings");
      }

      boolean liveStarted = false;

      try (AiScriptHost host = new AiScriptHost(this.memoryStore.directory(), (name, input) -> this.tools.call(name, input, g, true))) {
         this.scriptHost = host;
         this.scriptDeadlineNanos = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800))) * 1000000000L;
         String source = AiJson.getString(args, "source", "");
         JsonObject checked = host.init(source, args.has("fixtures") ? args.getAsJsonArray("fixtures") : new JsonArray(), () -> g != this.runGeneration.get());
         liveStarted = true;
         JsonObject result = host.init(source, AiJson.getInt(args, "timeoutSeconds", 1800), () -> g != this.runGeneration.get());
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
         this.scriptDeadlineNanos = 0L;
         this.scriptHost = null;
         Minecraft.getInstance().execute(() -> {
            if (this.activeOperation != null && this.activeOperation.generation() == g) {
               this.revertSettingDeltas();
            }
         });
      }
   }

   JsonObject search(String query, long g) throws IOException, InterruptedException, TimeoutException, ExecutionException {
      if (this.settings.nativeSearch && AiNativeSearch.init(this.settings.provider)) {
         this.checkGeneration(g);
         AiService.SessionState s = this.currentSession();
         if (s.requests >= this.settings.maxRequests) {
            throw new IllegalStateException("Task request limit reached");
         }

         s.requests++;
         s.push++;
         JsonObject result = AiNativeSearch.init(this.settings, query, () -> this.checkGeneration(g));
         s.tokens = s.tokens + result.get("tokens").getAsLong();
         this.checkGeneration(g);
         return result;
      } else {
         return AiWeb.init(this.settings, query, () -> this.checkGeneration(g));
      }
   }

   JsonObject screenshot(long g) throws InterruptedException, TimeoutException, ExecutionException {
      if (!this.settings.vision) {
         throw new IllegalStateException("Enable vision in provider settings only for a model supporting images");
      }

      CompletableFuture<String> captured = new CompletableFuture<>();
      this.onClientThread(
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

            Screenshot.takeScreenshot(target, factor, image -> this.storageExecutor.execute(() -> {
               try (image) {
                  if (g != this.runGeneration.get()) {
                     throw new CancellationException();
                  }

                  Path dir = this.memoryStore.directory().resolve("images");
                  Files.createDirectories(dir);
                  Path path = dir.resolve(UUID.randomUUID() + ".png");
                  image.writeToFile(path);
                  if (Files.size(path) > 1000000L) {
                     throw new IllegalStateException("Screenshot exceeds 1 MB");
                  }

                  AiMemoryStore.prune(dir, 32);
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
         this.checkGeneration(g);
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

   void saveMacroRevision(String name, String serialized) throws IOException {
      Path revisions = this.memoryStore.directory().resolve("revisions");
      AiJson.writeAtomicJson(revisions.resolve(AiJson.sha256Hex(name) + "-" + System.currentTimeMillis() + ".json"), AiJson.parseObject(serialized));
      AiMemoryStore.prune(revisions, 100);
   }

   static JsonObject structuredTaskForStorage(AiService.SessionState s) {
      JsonObject stored = s.structuredTask.deepCopy();
      if (s.planningPhase) {
         stored.addProperty("hostPlanningPhase", true);
      }

      if (s.planAwaitingApproval) {
         stored.addProperty("hostPlanAwaitingApproval", true);
      }

      return stored;
   }

   static void restoreStructuredTask(AiService.SessionState s, JsonObject stored) {
      if (stored == null) {
         stored = new JsonObject();
      }

      s.planningPhase = AiJson.getBoolean(stored, "hostPlanningPhase", false);
      s.planAwaitingApproval = AiJson.getBoolean(stored, "hostPlanAwaitingApproval", false);
      stored.remove("hostPlanningPhase");
      stored.remove("hostPlanAwaitingApproval");
      stored.remove("hostChatMode");
      s.structuredTask = stored;
   }

   private AiSessions.Inner_DataUtil toStoredSession(AiService.SessionState s) {
      AiSessions.Inner_DataUtil d = new AiSessions.Inner_DataUtil();
      d.id = s.id;
      d.title = s.title;
      d.parentId = s.parentId;
      d.createdAt = s.createdAt;
      d.lastActiveAt = s.lastActiveAt;
      String liveState = s.id.equals(this.currentSessionId) ? this.runState.name() : s.state;
      if ("CHATTING".equals(liveState)) {
         liveState = this.runStateBeforeChat == null ? "IDLE" : this.runStateBeforeChat.name();
      }

      d.state = liveState;
      d.taskId = s.taskId;
      d.objective = s.objective;
      d.taskState = s.taskState;
      d.scope = s.scope;
      d.structuredTask = structuredTaskForStorage(s);
      d.taskMinutes = s.taskMinutes;
      d.wheatBaseline = s.wheatBaseline;
      d.requests = s.requests;
      d.tokens = s.tokens;
      d.elapsedMillis = this.elapsedNanosOf(s) / 1000000L;
      d.attemptedAction = s.attemptedAction;
      d.queue = List.copyOf(s.queue);
      d.messages = List.copyOf(s.messages);
      d.updates = List.copyOf(s.updates);
      d.inputTokens = s.inputTokens;
      d.cachedInputTokens = s.cachedInputTokens;
      d.cacheWriteTokens = s.cacheWriteTokens;
      d.outputTokens = s.poll;
      d.poll = s.push;
      d.push = s.pop;
      d.wire = s.wire;
      return d;
   }

   private AiService.SessionState fromStoredSession(AiSessions.Inner_DataUtil d) {
      AiService.SessionState s = new AiService.SessionState(d.id, d.createdAt == 0L ? System.currentTimeMillis() : d.createdAt);
      s.title = d.title;
      s.parentId = d.parentId;
      s.lastActiveAt = d.lastActiveAt;
      s.state = d.state;
      s.taskId = d.taskId;
      s.objective = d.objective;
      s.taskState = d.taskState;
      s.scope = d.scope;
      restoreStructuredTask(s, d.structuredTask);
      s.taskMinutes = d.taskMinutes;
      s.wheatBaseline = d.wheatBaseline;
      s.requests = d.requests;
      s.tokens = d.tokens;
      s.elapsedNanos = d.elapsedMillis * 1000000L;
      s.attemptedAction = d.attemptedAction;
      s.queue.addAll(d.queue);
      s.messages.addAll(d.messages);

      for (String u : d.updates) {
         s.init(u);
      }

      s.inputTokens = d.inputTokens;
      s.cachedInputTokens = d.cachedInputTokens;
      s.cacheWriteTokens = d.cacheWriteTokens;
      s.poll = d.outputTokens;
      s.push = d.poll;
      s.pop = d.push;
      s.wire = d.wire;
      return s;
   }

   private void persistSession(AiService.SessionState s) {
      AiSessions storeRef = this.sessionStore;
      if (s != null && storeRef != null) {
         s.lastActiveAt = System.currentTimeMillis();
         if (s.id.equals(this.currentSessionId)) {
            s.state = this.runState.name();
         }

         if (!isSessionEmpty(s)) {
            AiSessions.Inner_DataUtil data = this.toStoredSession(s);
            this.storageExecutor.execute(() -> {
               if (!this.deletedSessionIds.contains(data.id)) {
                  try {
                     storeRef.save(data);
                  } catch (Exception e) {
                     this.appendTranscript("error", "Could not save session: " + e.getMessage());
                  }
               }
            });
         }
      }
   }

   private void persistCurrentSession() {
      this.persistSession(this.currentSession());
   }

   public void saveSettings(AiSettings next) {
      if (next != null) {
         AiSettings draft = next.copy();
         this.storageExecutor.execute(() -> {
            try {
               draft.validate();
               draft.effort = AiSettings.normalizeEffort(draft.effort);
               AiSettings previous = this.settings;
               boolean connectionChanged = !previous.sameConnection(draft);
               draft.save(this.memoryStore.directory());
               if (previous.maxMinutes != draft.maxMinutes) {
                  AiService.SessionState s = this.currentSession();
                  if (s != null) {
                     s.taskMinutes = draft.maxMinutes;
                  }
               }

               this.settings = draft.copy();
               this.storageError = "";
               if (connectionChanged) {
                  if (isExecutingState(this.runState)) {
                     this.appendTranscript("system", "Provider settings saved; they apply to the next task");
                  }

                  if (this.isChatModeActive()) {
                     this.stopChatMode("Chat mode stopped: provider settings changed");
                  }

                  this.startConnectionTest();
               }
            } catch (Exception e) {
               this.appendTranscript("error", e.getMessage());
            }
         });
      }
   }

   public List<String> getAvailableEffortLevels() {
      AiSettings s = this.settings;
      return s.builtIn ? List.of() : AiSettings.effortLevels(s.provider, s.model);
   }

   public String getEffectiveEffortLevel() {
      AiSettings s = this.settings;
      return AiSettings.nearestEffort(AiSettings.effortLevels(s.provider, s.model), s.effort);
   }

   public void setEffortLevel(String effort) {
      String level = effort == null ? "" : effort.trim().toLowerCase(Locale.ROOT);
      if (AiSettings.EFFORTS.contains(level)) {
         AiSettings live = this.settings.copy();
         live.effort = level;
         this.settings = live;
         this.editSettingsAsync(s -> s.effort = level);
      }
   }

   public boolean selectProvider(String providerId) {
      AiSettings.Provider next;
      try {
         next = AiSettings.Provider.valueOf(providerId.trim().toUpperCase(Locale.ROOT));
      } catch (Exception e) {
         return false;
      }

      AiSettings draft = this.settings.withProvider(next);
      draft.builtIn = false;
      this.saveSettings(draft);
      return true;
   }

   public void selectModel(String model) {
      if (model != null && !model.isBlank()) {
         AiSettings draft = this.settings.copy();
         draft.model = model.trim();
         this.saveSettings(draft);
      }
   }

   public boolean setProviderKey(String providerId, String key) {
      String id;
      try {
         id = AiSettings.Provider.valueOf(providerId.trim().toUpperCase(Locale.ROOT)).name();
      } catch (Exception e) {
         return false;
      }

      if (key != null && !key.isBlank()) {
         AiSettings draft = this.settings.copy();

         try {
            draft.setKey(id, key.trim());
         } catch (Exception e) {
            this.appendTranscript("error", "Could not seal the provider key: " + e.getMessage());
            return false;
         }

         if (draft.provider.name().equals(id)) {
            draft.builtIn = false;
         }

         this.saveSettings(draft);
         return true;
      } else {
         return false;
      }
   }

   public boolean hasProviderKey(AiSettings.Provider provider) {
      return provider != null && this.settings.hasKey(provider.name());
   }

   public void editSettingsAsync(Consumer<AiSettings> edit) {
      this.storageExecutor.execute(() -> {
         try {
            AiSettings next = this.settings.copy();
            edit.accept(next);
            next.save(this.memoryStore.directory());
            this.settings = next.copy();
         } catch (Exception e) {
            this.appendTranscript("error", "Could not save AI settings: " + e.getMessage());
         }
      });
   }

   public AiService.ProviderTestState getProviderTestState() {
      AiService.ProviderTestResult c = this.cachedProviderTest();
      return c == null ? AiService.ProviderTestState.UNKNOWN : c.state();
   }

   public String getProviderTestDetail() {
      AiService.ProviderTestResult c = this.cachedProviderTest();
      return c == null ? "" : c.detail();
   }

   private AiService.ProviderTestResult cachedProviderTest() {
      AiSettings s = this.settings;
      AiService.ProviderTestResult c = this.providerTests.get(s.provider);
      return c != null && c.fingerprint().equals(providerFingerprint(s)) ? c : null;
   }

   private static String providerFingerprint(AiSettings s) {
      return s.provider.name() + "|" + s.endpoint + "|" + s.model + "|" + s.keyMarker(s.provider.name());
   }

   public void startConnectionTest() {
      if (!this.storageLoaded) {
         this.appendTranscript("system", "AI storage is still loading");
      } else if (!this.settings.builtIn) {
         AiSettings snapshot = this.settings.copy();
         String fingerprint = providerFingerprint(snapshot);
         long probe = this.providerProbe.incrementAndGet();
         this.providerTests.put(snapshot.provider, new AiService.ProviderTestResult(AiService.ProviderTestState.TESTING, "Testing " + snapshot.model, fingerprint));
         long deadline = System.nanoTime() + 90000000000L;
         BooleanSupplier expired = () -> System.nanoTime() > deadline || probe != this.providerProbe.get();
         Thread.ofVirtual().name("autism-ai-connection-test").start(() -> {
            try {
               this.runProviderProbe(snapshot, expired);
               this.recordProviderTest(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.OK, "Round trip passed (" + snapshot.model + ")");
               this.appendTranscript("system", "Connection OK: tool-result round trip passed (" + snapshot.model + "). Model remains unqualified for gameplay.");
            } catch (CancellationException e) {
               if (probe == this.providerProbe.get()) {
                  this.recordProviderTest(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, "Timed out");
                  this.appendTranscript("error", "Connection test timed out");
               }
            } catch (Exception e) {
               String alt = snapshot.provider == AiSettings.Provider.DASHSCOPE ? AiSettings.alternateDashScopeEndpoint(snapshot.endpoint) : null;
               if (alt != null && !alt.equals(snapshot.endpoint) && Objects.toString(e.getMessage(), "").contains("401")) {
                  AiSettings regional = snapshot.copy();
                  regional.endpoint = alt;

                  try {
                     this.runProviderProbe(regional, expired);
                     this.saveDashScopeEndpoint(snapshot, alt, probe);
                     this.recordProviderTest(probe, snapshot.provider, providerFingerprint(regional), AiService.ProviderTestState.OK, "Round trip passed on " + alt);
                     this.appendTranscript("system", "Connection OK: tool-result round trip passed after switching the DashScope region to " + alt + ".");
                  } catch (Exception second) {
                     this.recordProviderTest(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, "Both DashScope regions failed: " + second.getMessage());
                     this.appendTranscript("error", "Connection test failed on both DashScope regions: " + second.getMessage());
                  }
               } else {
                  this.recordProviderTest(probe, snapshot.provider, fingerprint, AiService.ProviderTestState.FAILED, Objects.toString(e.getMessage(), "Connection failed"));
                  this.appendTranscript("error", "Connection test failed: " + e.getMessage());
               }
            }
         });
      }
   }

   private void recordProviderTest(long probe, AiSettings.Provider provider, String fingerprint, AiService.ProviderTestState state, String detail) {
      if (probe == this.providerProbe.get()) {
         this.providerTests.put(provider, new AiService.ProviderTestResult(state, detail == null ? "" : detail, fingerprint));
      }
   }

   private void saveDashScopeEndpoint(AiSettings tested, String to, long probe) {
      this.storageExecutor.execute(() -> {
         try {
            AiSettings next = this.settings.copy();
            if (probe != this.providerProbe.get() || next.provider != AiSettings.Provider.DASHSCOPE || !next.sameConnection(tested)) {
               return;
            }

            next.endpoint = to;
            next.save(this.memoryStore.directory());
            this.settings = next.copy();
         } catch (Exception e) {
            this.appendTranscript("error", "DashScope region worked but could not be saved: " + e.getMessage());
         }
      });
   }

   private void runProviderProbe(AiSettings effective, BooleanSupplier expired) throws IOException, InterruptedException, ExecutionException, TimeoutException {
      try (AiProvider p = new AiProvider(effective)) {
         JsonArray declarations = new JsonArray();
         declarations.add(
            AiJson.objectOf(
               "name", "connection_probe", "description", "Return the token provided by the user", "parameters", AiTools.schema("token", "string")
            )
         );
         String token = UUID.randomUUID().toString();
         String system = "Call the connection_probe tool exactly once with the token from the user message, without answering in text. After the tool result arrives, reply with a short confirmation and no tools.";
         AiProvider.Inner_BufferHelper reply = p.complete(system, List.of(p.userMessage(token)), declarations, "required", s -> {}, expired);
         if (reply.calls().size() != 1
            || !reply.calls().get(0).name().equals("connection_probe")
            || !AiJson.getString(reply.calls().get(0).arguments(), "token", "").equals(token)) {
            throw new IllegalStateException("Model did not return the expected tool call");
         }

         ArrayList<JsonObject> history = new ArrayList<>();
         history.add(p.userMessage(token));
         history.add(reply.wireMessage());
         history.addAll(p.toolResults(reply.calls(), List.of(AiJson.objectOf("verified", true))));
         AiProvider.Inner_BufferHelper confirmation = p.complete(system, history, declarations, "none", s -> {}, expired);
         if (confirmation.text().isBlank() || !confirmation.calls().isEmpty()) {
            throw new IllegalStateException("Model did not acknowledge the completed tool round trip");
         }
      }
   }

   public void removeProviderKey(String providerId) {
      if (this.isChatModeActive() && providerId != null && providerId.equalsIgnoreCase(this.settings.provider.name())) {
         this.stopChatMode("Chat mode stopped: the provider key was removed");
      }

      this.storageExecutor
         .execute(
            () -> {
               try {
                  AiSettings next = this.settings.copy();
                  next.setKey(providerId, "");
                  next.save(this.memoryStore.directory());
                  this.settings = next.copy();
                  this.storageError = "";
                  this.appendTranscript(
                     "system",
                     "Provider key removed" + (isExecutingState(this.runState) ? "; the running task keeps its connection until it ends" : "")
                  );
               } catch (Exception e) {
                  this.appendTranscript("error", "Could not remove provider key: " + e.getMessage());
               }
            }
         );
   }

   public AiService.TokenUsage getTokenUsage() {
      AiService.SessionState s = this.currentSession();
      return s == null
         ? new AiService.TokenUsage(0L, 0L, 0L, 0L, 0L, this.settings.contextTokens, 0L)
         : new AiService.TokenUsage(s.inputTokens, s.cachedInputTokens, s.cacheWriteTokens, s.poll, s.pop, this.settings.contextTokens, s.push);
   }

   public List<String> getSteeringNotes() {
      AiService.SessionState s = this.currentSession();
      return s == null ? List.of() : List.copyOf(s.updates);
   }

   public List<AiMemoryStore.Memory> searchMemories(String query) {
      return this.memoryStore.load(this.worldEpochKey, query);
   }

   public void forgetMemory(String id) {
      this.storageExecutor.execute(() -> {
         try {
            this.memoryStore.forget(id);
         } catch (Exception e) {
            this.appendTranscript("error", e.getMessage());
         }
      });
   }

   void saveSpeedrunCheckpoint(JsonObject snapshot) {
      JsonObject saved = snapshot.deepCopy();
      Path destination = this.memoryStore.directory().resolve("speedrun-checkpoint.json");
      this.storageExecutor.execute(() -> {
         try {
            AiJson.writeAtomicJson(destination, saved);
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[AI] Could not save speedrun checkpoint", e);
         }
      });
   }

   void saveSpeedrunDiagnostics(JsonObject snapshot) {
      JsonObject saved = snapshot.deepCopy();
      Path destination = this.memoryStore.directory().resolve("speedrun-debug-latest.json");
      this.storageExecutor.execute(() -> {
         try {
            AiJson.writeAtomicJson(destination, saved);
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[AI] Could not save speedrun diagnostics", e);
         }
      });
   }

   void savePortalMemory(String world, JsonArray links) {
      this.saveWorldRecords("portal-memory", world, "links", links);
   }

   boolean loadPortalMemory(String world, Consumer<JsonArray> loaded) {
      return this.loadWorldRecords("portal-memory", world, "links", loaded);
   }

   void saveNetherRouteMemory(String world, JsonArray receipts) {
      this.saveWorldRecords("nether-route-memory", world, "receipts", receipts);
   }

   boolean loadNetherRouteMemory(String world, Consumer<JsonArray> loaded) {
      return this.loadWorldRecords("nether-route-memory", world, "receipts", loaded);
   }

   void saveDiscoveryMemory(String world, String dimension, JsonArray facts) {
      this.saveWorldRecords("discovery-memory", world + "/dimension/" + dimension, "facts", facts);
   }

   boolean loadDiscoveryMemory(String world, String dimension, Consumer<JsonArray> loaded) {
      return this.loadWorldRecords("discovery-memory", world + "/dimension/" + dimension, "facts", loaded);
   }

   private void saveWorldRecords(String folder, String world, String field, JsonArray records) {
      if (this.storageLoaded && this.memoryStore != null && !world.equals("offline")) {
         Path directory = this.memoryStore.directory().resolve(folder);
         Path destination = directory.resolve(AiJson.sha256Hex(world) + ".json");
         JsonObject saved = AiJson.objectOf("version", 1, "world", world, field, records.deepCopy());
         this.storageExecutor
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

   private boolean loadWorldRecords(String folder, String world, String field, Consumer<JsonArray> loaded) {
      if (this.storageLoaded && this.memoryStore != null && !world.equals("offline")) {
         Path source = this.memoryStore.directory().resolve(folder).resolve(AiJson.sha256Hex(world) + ".json");
         this.storageExecutor
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

   void appendTraceEvent(String kind, JsonObject evidence) {
      AiRunTrace trace = AiRunTrace.current();
      if (trace != null) {
         JsonObject saved = evidence.deepCopy();
         saved.addProperty("simulationTick", autismclient.util.AutismSessionState.getInstance().getClientTick());
         saved.addProperty("observedAtMs", System.currentTimeMillis());
         this.storageExecutor.execute(() -> trace.appendEvent(kind, saved));
      }
   }

   public void correctMemory(String id, String text) {
      this.storageExecutor
         .execute(
            () -> {
               try {
                  AiMemoryStore.Memory old = this.memoryStore
                     .memories()
                     .stream()
                     .filter(m -> m.id().equals(id))
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Unknown memory " + id));
                  AiObservation proof = new AiObservation(
                     UUID.randomUUID().toString(),
                     this.observationSession,
                     "user",
                     System.currentTimeMillis(),
                     "user",
                     AiJson.objectOf("correction", text).toString()
                  );
                  this.memoryStore.init(proof);
                  AiMemoryStore.Memory saved = this.memoryStore
                     .init(
                        old.kind() == AiMemoryStore.Inner_DataUtil.USER ? AiMemoryStore.Inner_DataUtil.USER : AiMemoryStore.Inner_DataUtil.HYPOTHESIS,
                        old.scope(),
                        text,
                        proof.id(),
                        this.getCatalogSnapshot().revision(),
                        "User correction; reobserve world facts before relying on them"
                     );
                  if (!saved.id().equals(id)) {
                     try {
                        this.memoryStore.forget(id);
                     } catch (Exception e) {
                        this.appendTranscript(
                           "error",
                           "Correction saved as a new memory but the original could not be removed; both may appear until it is forgotten manually: "
                              + e.getMessage()
                        );
                     }
                  }

                  this.appendTranscript("system", "Memory correction saved");
               } catch (Exception e) {
                  this.appendTranscript("error", "Memory correction: " + e.getMessage());
               }
            }
         );
   }

   public List<AiService.AttachmentRef> getAttachments() {
      return this.attachments;
   }

   public String getAttachmentPreview() {
      return this.attachmentPreview;
   }

   public void refreshAttachments() {
      this.storageExecutor
         .execute(
            () -> {
               try {
                  List<AiService.AttachmentRef> found = new ArrayList<>();

                  for (String folder : List.of("scripts", "revisions")) {
                     Path path = this.memoryStore.directory().resolve(folder);
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

                  this.attachments = List.copyOf(found);
                  this.attachmentPreview = "Select generated source or a preserved macro revision.";
               } catch (Exception e) {
                  this.appendTranscript("error", e.getMessage());
               }
            }
         );
   }

   public void loadAttachmentPreview(String relative) {
      this.storageExecutor.execute(() -> {
         try {
            Path root = this.memoryStore.directory().toAbsolutePath().normalize();
            Path path = root.resolve(relative).normalize();
            if (!path.startsWith(root) || this.attachments.stream().noneMatch(a -> a.relativePath().equals(relative))) {
               throw new IllegalArgumentException("Unknown artifact");
            }

            try (BufferedReader reader = Files.newBufferedReader(path)) {
               char[] buffer = new char[24000];
               int n = reader.read(buffer);
               this.attachmentPreview = relative + "\n" + (n < 0 ? "" : new String(buffer, 0, n)) + (n == 24000 ? "\n[Preview limited to 24,000 characters]" : "");
            }
         } catch (Exception e) {
            this.attachmentPreview = e.getMessage();
         }
      });
   }

   public void clearWorldMemory(boolean worldOnly) {
      if (isExecutingState(this.runState)) {
         this.pauseTask();
      }

      String worldKey = Minecraft.getInstance().player == null ? "offline" : AiWorldIdentity.currentWorldKey(Minecraft.getInstance());
      String dimension = AiWorldIdentity.currentDimensionId(Minecraft.getInstance());
      AiPortalLinks.reset(worldOnly ? worldKey : null);
      AiWorldMemory.init(worldOnly ? worldKey : null);
      this.storageExecutor.execute(() -> {
         try {
            Path portalDirectory = this.memoryStore.directory().resolve("portal-memory");
            if (worldOnly) {
               Files.deleteIfExists(portalDirectory.resolve(AiJson.sha256Hex(worldKey) + ".json"));
            } else if (Files.isDirectory(portalDirectory)) {
               try (Stream<Path> files = Files.list(portalDirectory)) {
                  for (Path file : files.filter(x$0 -> Files.isRegularFile(x$0)).filter(p -> p.toString().endsWith(".json")).toList()) {
                     Files.deleteIfExists(file);
                  }
               }
            }

            Path discoveryDirectory = this.memoryStore.directory().resolve("discovery-memory");
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
               this.memoryStore.startEpoch(worldKey);
               Minecraft.getInstance().execute(() -> {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.player != null && AiWorldIdentity.currentWorldKey(mc).equals(worldKey) && AiWorldIdentity.currentDimensionId(mc).equals(dimension)) {
                     this.worldEpochKey = this.memoryStore.epochKey(worldKey);
                     this.observationScope = this.worldEpochKey + "/dimension/" + dimension;
                  }
               });
            } else {
               this.memoryStore.load();
            }

            this.appendTranscript("system", "Memory reset");
         } catch (Exception e) {
            this.appendTranscript("error", e.getMessage());
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
      private final AiService.SessionState session = AiService.this.currentSession();
      private final ClientLevel level = Minecraft.getInstance().level;
      private final ClientPacketListener connection = Minecraft.getInstance().getConnection();
      private AiChatMode boundChatMode;

      private boolean isStillCurrent() {
         Minecraft mc = Minecraft.getInstance();
         return AiService.this.chatMode == this.boundChatMode
            && AiService.this.currentSession() == this.session
            && mc.level == this.level
            && mc.getConnection() == this.connection;
      }

      @Override
      public long now() {
         return System.currentTimeMillis();
      }

      @Override
      public String playerName() {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null) {
            return "";
         }

         GameProfile profile = player.getGameProfile();
         return profile != null && profile.name() != null ? profile.name() : player.getName().getString();
      }

      @Override
      public boolean canSend() {
         Minecraft mc = Minecraft.getInstance();
         return this.isStillCurrent()
            && this.boundChatMode.isActive()
            && mc.player != null
            && this.connection != null
            && !autismclient.modules.aD.init_Z();
      }

      @Override
      public long load_J() {
         return AiChatLedger.revision();
      }

      @Override
      public List<AiChatLedger.ChatEntry> init(int max) {
         return AiChatLedger.recent(max);
      }

      @Override
      public void init(String text) {
         this.init(text, () -> true);
      }

      @Override
      public void init(String text, BooleanSupplier valid) {
         Minecraft mc = Minecraft.getInstance();
         mc.execute(() -> {
            if (valid.getAsBoolean() && this.canSend()) {
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
         if (AiChatPolicy.isPlayerName(player)) {
            String prefix = "msg " + player + " ";
            String body = text.length() + prefix.length() > 256 ? text.substring(0, 256 - prefix.length()).strip() : text;
            if (!body.isEmpty()) {
               Minecraft mc = Minecraft.getInstance();
               mc.execute(() -> {
                  if (valid.getAsBoolean() && this.canSend()) {
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
         if (this.isStillCurrent()) {
            AiService.this.appendToSession(this.session, role, text);
         }
      }

      @Override
      public void init(JsonObject status) {
         if (this.isStillCurrent()) {
            AiService.this.saveChatModeStatus(status);
         }
      }

      @Override
      public void init_V() {
         if (this.isStillCurrent()) {
            AiService.this.enterChattingState();
         }
      }

      @Override
      public void load(String reason) {
         if (AiService.this.chatMode == this.boundChatMode && AiService.this.currentSession() == this.session) {
            AiService.this.get(reason);
         }
      }

      @Override
      public void init(long inputTokens, long outputTokens, long requests) {
         AiService.SessionState s = this.session;
         if (s != null) {
            synchronized (s) {
               s.inputTokens = s.inputTokens + Math.max(0L, inputTokens);
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
      private final List<Integer> issuedIndices = new ArrayList<>();
      private final TreeMap<Integer, JsonObject> resultsByIndex = new TreeMap<>();
      private List<Integer> load;

      void markIssued(int index) {
         this.issuedIndices.add(index);
         this.load = null;
      }

      void recordResult(int index, JsonObject result) {
         this.resultsByIndex.put(index, result);
      }

      void init(JsonObject result) {
         for (int index : this.issuedIndices) {
            this.resultsByIndex.putIfAbsent(index, result);
         }
      }

      JsonObject resultAt(int index) {
         return this.resultsByIndex.get(index);
      }

      private List<Integer> init() {
         if (this.load == null) {
            this.load = this.issuedIndices.stream().sorted().toList();
         }

         return this.load;
      }

      int indexAtPosition(int position) {
         return position < this.init().size() ? this.init().get(position) : -1;
      }

      List<JsonObject> resultsOrFiller(int callCount, JsonObject filler) {
         List<JsonObject> out = new ArrayList<>(callCount);

         for (int k = 0; k < callCount; k++) {
            int index = this.indexAtPosition(k);
            JsonObject r = index < 0 ? null : this.resultsByIndex.get(index);
            out.add(r == null ? filler : r);
         }

         return out;
      }

      List<JsonObject> alignedResults(int callCount) {
         if (this.issuedIndices.size() != callCount) {
            return null;
         }

         List<JsonObject> aligned = new ArrayList<>(callCount);

         for (int k = 0; k < callCount; k++) {
            JsonObject result = this.resultsByIndex.get(this.init().get(k));
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
      final String id;
      final long createdAt;
      volatile String title = "New session";
      volatile String parentId;
      volatile long lastActiveAt;
      volatile String state = "IDLE";
      volatile String objective = "";
      volatile String taskState = "";
      volatile String scope = "";
      volatile String taskId = UUID.randomUUID().toString();
      volatile JsonObject structuredTask = new JsonObject();
      volatile int taskMinutes = 60;
      volatile int wheatBaseline = -1;
      volatile long requests;
      volatile long tokens;
      volatile long elapsedNanos;
      volatile long unusedResetTime;
      volatile long inputTokens;
      volatile long cachedInputTokens;
      volatile long cacheWriteTokens;
      volatile long poll;
      volatile long push;
      volatile long pop;
      volatile String wire;
      volatile boolean attemptedAction;
      volatile boolean planningPhase;
      volatile boolean planAwaitingApproval;
      final Queue<String> amendments = new ConcurrentLinkedQueue<>();
      final List<String> updates = new CopyOnWriteArrayList<>();
      final Deque<String> queue = new ConcurrentLinkedDeque<>();
      final List<AiService.TranscriptEntry> messages = new CopyOnWriteArrayList<>();

      SessionState(String id, long createdAt) {
         this.id = id;
         this.createdAt = createdAt;
         this.lastActiveAt = createdAt;
      }

      void init(String text) {
         String t = text.trim();
         if (!t.isEmpty()) {
            this.updates.add(t.length() > 2000 ? t.substring(0, 2000) : t);

            while (this.updates.size() > 8) {
               this.updates.remove(0);
            }
         }
      }

      void init(boolean planModeEnabled) {
         this.planningPhase = planModeEnabled;
         this.planAwaitingApproval = false;
      }

      void load(String planText) {
         this.planAwaitingApproval = true;
         String note = this.taskState + "\nPLAN AWAITING APPROVAL:\n" + planText;
         this.taskState = note.length() > 11900 ? note.substring(note.length() - 11900) : note;
      }

      void init() {
         this.planAwaitingApproval = false;
         this.planningPhase = false;
         String note = this.taskState + "\nPlan approved by the user; execute it faithfully and report deviations.";
         this.taskState = note.length() > 11900 ? note.substring(note.length() - 11900) : note;
      }

      void read(String feedback) {
         this.planAwaitingApproval = false;
         this.amendments.add(feedback);
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
      private final String sessionId;
      private int persistedCount;
      private String load;
      private boolean rewriteWhole;

      SessionTurnTracker(String sid) {
         this.sessionId = sid;
      }

      void init() {
         this.rewriteWhole = true;
      }

      void init(List<JsonObject> history) {
         AiSessions storeRef = AiService.this.sessionStore;
         if (storeRef != null) {
            if (!this.rewriteWhole && (this.persistedCount > history.size() || this.persistedCount > 0 && !history.get(this.persistedCount - 1).toString().equals(this.load))) {
               this.rewriteWhole = true;
            }

            boolean whole = this.rewriteWhole;
            List<JsonObject> lines = new ArrayList<>();

            for (int i = whole ? 0 : this.persistedCount; i < history.size(); i++) {
               lines.add(history.get(i).deepCopy());
            }

            this.persistedCount = history.size();
            this.load = this.persistedCount == 0 ? null : history.get(this.persistedCount - 1).toString();
            this.rewriteWhole = false;
            if (whole || !lines.isEmpty()) {
               AiService.this.storageExecutor.execute(() -> {
                  if (!AiService.this.deletedSessionIds.contains(this.sessionId)) {
                     try {
                        if (whole) {
                           storeRef.writeLines(this.sessionId, lines);
                        } else {
                           for (JsonObject line : lines) {
                              storeRef.appendLine(this.sessionId, line);
                           }
                        }
                     } catch (Exception e) {
                        AiService.this.appendTranscript("error", "Transcript write failed: " + e.getMessage());
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
