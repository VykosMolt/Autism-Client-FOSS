package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.api.macro.AddonCondition;
import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroExecutionContext;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.OnlinePlayerCollector;
import autismclient.util.CommandTabNameProber;
import autismclient.util.bi;
import autismclient.util.PacedTpJobManager;
import autismclient.util.PacketNameResolver;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.RotationMath;
import autismclient.util.AutismSessionState;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MacroExecutor {
   private static final Gson GSON = new Gson();
   private static final long init_J = 500000L;
   private static final long load_J = 300L;
   private static final AtomicBoolean ANY_RUN_ACTIVE = new AtomicBoolean(false);
   public static final int MAX_CONCURRENT_RUNS = 4;
   private static final Object RUNS_LOCK = new Object();
   private static final EnumMap<MacroResourceClassifier.MacroResource, ReentrantLock> RESOURCE_LOCKS = new EnumMap<>(MacroResourceClassifier.MacroResource.class);
   private static final MacroResourceLock EXTERNAL_RESOURCE_LOCK = new MacroResourceLock();
   private static final AtomicLong NEXT_RUN_ID;
   private static final LinkedHashMap<Long, MacroExecutor.MacroRun> ACTIVE_RUNS;
   private static final ConcurrentHashMap<Long, MacroExecutor.MacroRun> RUNS_BY_ID;
   private static final AtomicLong RUN_EPOCH;
   private static final ThreadLocal<MacroExecutor.MacroRun> CURRENT_RUN;
   private static final ThreadLocal<Boolean> SUPPRESS_POST_GUI_WAIT;
   private static volatile MacroExecutor.MacroRun visibleRun;
   private static volatile boolean visibleRunActive;
   private static autismclient.util.AutismMacro visibleMacro;
   private static Thread lastMacroThread;
   private static final AtomicReference<String> GLOBAL_MATCHED_PACKET;
   private static String statusText;
   private static volatile int currentStep;
   private static volatile int totalSteps;
   private static final AtomicBoolean GLOBAL_WAITING_FOR_PACKET;
   private static final AtomicReference<String> GLOBAL_PACKET_WAIT_TARGET;
   private static final Object GLOBAL_PACKET_WAIT_LOCK;
   private static final AtomicBoolean GLOBAL_WAITING_FOR_CHAT;
   private static final AtomicReference<String> GLOBAL_CHAT_WAIT_PATTERN;
   private static final AtomicBoolean GLOBAL_CHAT_WAIT_USE_REGEX;
   private static final AtomicReference<String> GLOBAL_CHAT_WAIT_PATTERN_JSON;
   private static final AtomicInteger GLOBAL_CHAT_WAIT_FUZZY_PERCENT;
   private static final AtomicReference<String> GLOBAL_CHAT_WAIT_MATCH;
   private static final Object CHAT_LOG_LOCK;
   private static final Deque<MacroExecutor.LoggedChatMessage> CHAT_LOG;
   private static final int write_I = 256;
   private static final Object GUI_TITLE_HISTORY_LOCK;
   private static final Deque<MacroExecutor.ChatHistoryEntry> GUI_TITLE_HISTORY;
   private static final int update_I = 128;
   private static String chatLogServerKey;
   private static volatile CompletableFuture<Void> globalPacketWaitFuture;
   private static volatile CompletableFuture<Void> globalChatWaitFuture;
   private static final AtomicReference<MacroExecutor.MacroRun> NAVIGATION_OWNER;
   private static final CopyOnWriteArrayList<AiOperation> TRACKED_AI_OPERATIONS;
   private static final AtomicBoolean SMOOTH_LOOK_ACTIVE;
   private static final AtomicReference<MacroExecutor.MacroRun> SMOOTH_LOOK_OWNER;
   private static final Object SMOOTH_LOOK_LOCK;
   private static volatile float smoothLookYaw;
   private static volatile float smoothLookPitch;
   private static volatile double smoothLookSpeed;
   private static volatile int smoothLookSettledFrames;
   private static final CopyOnWriteArrayList<MacroAction> GLOBAL_HELD_ACTIONS;
   private static final ConcurrentHashMap<MoveAction, int[]> GLOBAL_HELD_MOVES;
   private static final ThreadLocal<MacroExecutor.ActiveStepSlice> ACTIVE_STEP_SLICE;
   private static final ThreadLocal<Integer> SLICE_STEPS_CONSUMED;
   private static final int flush_I = 100000;
   private static final int close = 5000000;
   private static final AtomicInteger PENDING_AI_CANCELLATIONS;
   private static volatile List<MacroExecutor.MacroRun> runsView;
   private static final LinkedHashMap<Long, MacroExecutor.MacroRunOutcome> RUN_OUTCOMES;
   private static final ConcurrentHashMap<String, MacroExecutor.ServerPacketCacheEntry> PACKET_TARGET_CACHE;
   private static final MacroExecutor.ServerPacketCacheEntry EMPTY_PACKET_TARGET;
   private static final ConcurrentHashMap<String, MacroExecutor.CompletionCacheEntry> COMPLETION_CACHE;
   private static final ConcurrentHashMap<String, MacroExecutor.SuggestionFutureEntry> PENDING_SUGGESTIONS;
   private static final int open = 256;
   private static final long read_J = 3000L;
   private static final int poll = 1000;
   private static final int push = 8;
   private static final int pop = 60000;
   private static final String read_Str = "abcdefghijklmnopqrstuvwxyz0123456789_";
   private static volatile long lastAllRunsEndedMs;
   private static final ConcurrentHashMap<String, MacroExecutor.CommandProbeQueue> COMMAND_PROBE_QUEUES;
   private static final int tick = 256;
   private static final ThreadLocal<CompletableFuture<Void>> MAIN_THREAD_BARRIER;
   private static final ThreadLocal<int[]> MAIN_THREAD_BURST_COUNT;
   private static final ConcurrentLinkedQueue<Runnable> TICK_ALIGNED_TASKS;
   private static volatile long manualLeaveAtMs;
   private static int suppressedLeaveMarks;
   private static final long sync_J = 10000L;
   private static final long WAITER_TIMEOUT_NANOS;
   private static final List<MacroExecutor.PacketWaiter> INCOMING_PACKET_WAITERS;
   private static final List<MacroExecutor.PacketWaiter> OUTGOING_PACKET_WAITERS;
   private static final List<MacroExecutor.PacketMatchWaiter> INCOMING_EVENT_WAITERS;
   private static final List<MacroExecutor.PacketMatchWaiter> OUTGOING_EVENT_WAITERS;
   private static volatile String lastSeenServerKey;

   private static Map<String, Integer> buildLabelIndex(MacroExecutor.MacroRun run, autismclient.util.AutismMacro macro) {
      Map<String, Integer> cached = run.labelIndex;
      if (cached != null) {
         return cached;
      }

      LinkedHashMap<String, Integer> built = new LinkedHashMap<>();

      for (int k = 0; k < macro.actions.size(); k++) {
         if (macro.actions.get(k) instanceof LabelAction la) {
            String key = la.normalizedName();
            if (!key.isEmpty() && !built.containsKey(key)) {
               built.put(key, k);
            }
         }
      }

      run.labelIndex = built;
      return built;
   }

   private static Integer resolveFlowTarget(MacroExecutor.MacroRun run, autismclient.util.AutismMacro macro, int i, FlowAction flow, Minecraft mc) {
      Integer var10000;
      switch (flow.target) {
         case STOP:
            var10000 = null;
            break;
         case TOP:
            var10000 = 0;
            break;
         case END:
            var10000 = macro.actions.size();
            break;
         case FORWARD:
            var10000 = i + 1 + Math.max(0, flow.amount);
            break;
         case BACK:
            var10000 = i - Math.max(0, flow.amount);
            break;
         case STEP:
            var10000 = Math.max(1, flow.amount) - 1;
            break;
         case LABEL:
            String name = LabelAction.normalize(MacroVariableContext.resolveString(flow.labelName, mc));
            Integer idx = buildLabelIndex(run, macro).get(name);
            if (idx == null) {
               AutismClientMessaging.sendPrefixed("§eFlow: label '" + flow.labelName + "' not found.");
               var10000 = flow.onMissingLabel == FlowAction.MissingLabelPolicy.STOP ? null : i + 1;
            } else {
               var10000 = idx;
            }
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int applyFlowJump(MacroExecutor.MacroRun run, autismclient.util.AutismMacro macro, int currentI, FlowAction flow, Minecraft mc) {
      run.elseBranchIndex = -1;
      run.elseSkipSteps = 0;
      Integer dest = resolveFlowTarget(run, macro, currentI, flow, mc);
      if (dest == null) {
         return -2147483648;
      }

      int size = macro.actions.size();
      int target = Math.max(0, Math.min(size, dest));
      if (target <= currentI) {
         run.backwardJumps++;
         run.backwardJumpBurst++;
         if (run.backwardJumps > 5000000) {
            AutismClientMessaging.sendPrefixed("§cMacro aborted: jump limit exceeded (runaway loop).");
            return -2147483648;
         }

         if (run.backwardJumpBurst >= 100000) {
            run.backwardJumpBurst = 0;

            try {
               awaitWhileRunning(MacroWaitRegistry.awaitTickSync());
            } catch (Exception var9) {
            }
         }
      } else {
         run.backwardJumpBurst = 0;
      }

      return target - 1;
   }

   private static boolean isStillRunning() {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run != null ? run.running.get() : ANY_RUN_ACTIVE.get();
   }

   public static boolean isCurrentRunActive() {
      return isStillRunning();
   }

   private static CopyOnWriteArrayList<MacroAction> heldActions() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run != null ? run.heldActions : GLOBAL_HELD_ACTIONS;
   }

   private static ConcurrentHashMap<MoveAction, int[]> heldMoves() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run != null ? run.heldMoves : GLOBAL_HELD_MOVES;
   }

   private static void setRunStatus(String status) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         run.statusText = status == null ? "" : status;
      }

      statusText = status == null ? "" : status;
   }

   private static void setRunProgress(int step, int total) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         run.currentStep = step;
         run.totalSteps = total;
      }

      currentStep = step;
      totalSteps = total;
   }

   private static void markStepCompleted(int stepOneBased) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         run.lastCompletedStep = Math.max(run.lastCompletedStep, stepOneBased);
      }
   }

   private static int coveredEndIndex(autismclient.util.AutismMacro macro, int headerIndex, int coveredRows) {
      int last = macro == null ? headerIndex : macro.actions.size() - 1;
      long end = (long)headerIndex + Math.max(0, coveredRows);
      return (int)Math.max(headerIndex, Math.min(last, end));
   }

   private static void startFpsOverride(MacroExecutor.MacroRun run, int headerIndex, int endIndexInclusive, int fpsLimit) {
      if (run != null) {
         run.fpsOverrideStart = headerIndex;
         run.fpsOverrideEnd = endIndexInclusive;
         MacroFpsOverrideManager.setOverride(run.id, fpsLimit);
      }
   }

   private static void startPingSpoofOverride(
      MacroExecutor.MacroRun run, int headerIndex, int endIndexInclusive, int delayMs, boolean realIncoming, boolean realOutgoing
   ) {
      if (run != null) {
         run.pingSpoofOverrideStart = headerIndex;
         run.pingSpoofOverrideEnd = endIndexInclusive;
         PingSpoofScheduler.setRunOverride(run.id, delayMs, realIncoming, realOutgoing);
      }
   }

   private static void endFpsOverride(MacroExecutor.MacroRun run) {
      if (run != null && run.fpsOverrideStart >= 0) {
         run.fpsOverrideStart = -1;
         run.fpsOverrideEnd = -1;
         MacroFpsOverrideManager.clearOverride(run.id);
      }
   }

   private static void endPingSpoofOverride(MacroExecutor.MacroRun run) {
      if (run != null && run.pingSpoofOverrideStart >= 0) {
         run.pingSpoofOverrideStart = -1;
         run.pingSpoofOverrideEnd = -1;
         PingSpoofScheduler.stop(run.id);
      }
   }

   private static void sleep(long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   private static <T> T await(CompletableFuture<T> f) {
      try {
         return f.get();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeException(e);
      } catch (ExecutionException e) {
         throw new RuntimeException(e.getCause());
      }
   }

   private static <T> T await(CompletableFuture<T> f, long ms) {
      try {
         return f.get(ms, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeException(e);
      } catch (ExecutionException | TimeoutException e) {
         throw new RuntimeException(e);
      }
   }

   private static boolean awaitLatch(CountDownLatch l, long ms) {
      try {
         return l.await(ms, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return false;
      }
   }

   private static void lock(Lock l) {
      try {
         l.lockInterruptibly();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         l.lock();
      }
   }

   private static void waitForOverrideTimeouts(MacroExecutor.MacroRun run) {
      if (run != null) {
         long deadline = Math.max(run.fpsTimeoutDeadlineNanos, run.spingTimeoutDeadlineNanos);

         long remaining;
         while (isStillRunning() && (remaining = deadline - System.nanoTime()) > 0L) {
            sleep(Math.min(25L, remaining / 1000000L + 1L));
         }
      }
   }

   private static void endOverridesOutside(MacroExecutor.MacroRun run, int index) {
      if (run != null) {
         if (run.fpsOverrideStart >= 0 && (index > run.fpsOverrideEnd || index <= run.fpsOverrideStart)) {
            endFpsOverride(run);
         }

         if (run.pingSpoofOverrideStart >= 0 && (index > run.pingSpoofOverrideEnd || index <= run.pingSpoofOverrideStart)) {
            endPingSpoofOverride(run);
         }
      }
   }

   private static void stopCurrentRunOrAll() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         run.running.set(false);
      } else {
         stopAll();
      }
   }

   static void stopCurrentRun() {
      stopCurrentRunOrAll();
   }

   private static EnumSet<MacroResourceClassifier.MacroResource> resourcesOfAction(MacroAction action) {
      return MacroResourceClassifier.classify(action);
   }

   public static AutoCloseable acquireExternalResources(Collection<String> names) {
      EnumSet<MacroResourceClassifier.MacroResource> requested = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);

      for (MacroResourceClassifier.MacroResource resource : MacroResourceClassifier.MacroResource.values()) {
         if (names.contains(resource.name())) {
            requested.add(resource);
         }
      }

      if (requested.contains(MacroResourceClassifier.MacroResource.INPUT)) {
         requested.addAll(EnumSet.of(MacroResourceClassifier.MacroResource.INVENTORY, MacroResourceClassifier.MacroResource.GUI));
      }

      if (areResourcesBusy(requested)) {
         return null;
      }

      AutoCloseable controller = EXTERNAL_RESOURCE_LOCK.acquire(requested);
      if (controller == null) {
         return null;
      }

      EnumSet<MacroResourceClassifier.MacroResource> acquired = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);

      for (MacroResourceClassifier.MacroResource resource : MacroResourceClassifier.MacroResource.values()) {
         if (requested.contains(resource)) {
            ReentrantLock lock = RESOURCE_LOCKS.get(resource);
            if (lock.hasQueuedThreads() || !lock.tryLock()) {
               releaseResourceLocks(acquired);

               try {
                  controller.close();
               } catch (Exception var10) {
               }

               return null;
            }

            acquired.add(resource);
         }
      }

      Thread owner = Thread.currentThread();
      AtomicBoolean closed = new AtomicBoolean();
      return () -> {
         if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Controller resources must be released on their owning thread");
         }

         if (closed.compareAndSet(false, true)) {
            releaseResourceLocks(acquired);
            controller.close();
            if (requested.contains(MacroResourceClassifier.MacroResource.INPUT)) {
               syncHeldKeys(Minecraft.getInstance());
            }
         }
      };
   }

   public static boolean isResourceLocked(MacroResourceClassifier.MacroResource resource) {
      return EXTERNAL_RESOURCE_LOCK.isLocked(resource);
   }

   private static boolean areResourcesBusy(EnumSet<MacroResourceClassifier.MacroResource> requested) {
      boolean input = requested.contains(MacroResourceClassifier.MacroResource.INPUT);
      boolean rotation = requested.contains(MacroResourceClassifier.MacroResource.ROTATION);
      boolean navigation = requested.contains(MacroResourceClassifier.MacroResource.NAVIGATION);
      if (!input && !rotation && !navigation || PENDING_AI_CANCELLATIONS.get() <= 0 && TRACKED_AI_OPERATIONS.isEmpty()) {
         if (input && !GLOBAL_HELD_MOVES.isEmpty()) {
            return true;
         }

         if (rotation && SMOOTH_LOOK_OWNER.get() != null) {
            return true;
         }

         for (MacroExecutor.MacroRun run : runsSnapshot()) {
            if ((input || rotation || navigation) && run.aiOperation != null) {
               return true;
            }

            if (input && !run.heldMoves.isEmpty()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static EnumSet<MacroResourceClassifier.MacroResource> acquireResourceLocksFor(MacroAction action) {
      return acquireResourceLocks(resourcesOfAction(action));
   }

   private static EnumSet<MacroResourceClassifier.MacroResource> acquireResourceLocks(EnumSet<MacroResourceClassifier.MacroResource> resources) {
      EnumSet<MacroResourceClassifier.MacroResource> acquired = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);

      for (MacroResourceClassifier.MacroResource resource : MacroResourceClassifier.MacroResource.values()) {
         if (resources.contains(resource)) {
            if (!isStillRunning()) {
               throw new RuntimeException("Macro stopped");
            }

            lock(RESOURCE_LOCKS.get(resource));
            acquired.add(resource);
            if (!isStillRunning()) {
               throw new RuntimeException("Macro stopped");
            }
         }
      }

      return acquired;
   }

   private static EnumSet<MacroResourceClassifier.MacroResource> resourcesOfActions(List<MacroAction> actions) {
      return MacroResourceClassifier.classifyAll(actions);
   }

   private static void releaseResourceLocks(EnumSet<MacroResourceClassifier.MacroResource> resources) {
      if (resources != null && !resources.isEmpty()) {
         MacroResourceClassifier.MacroResource[] ordered = MacroResourceClassifier.MacroResource.values();

         for (int i = ordered.length - 1; i >= 0; i--) {
            MacroResourceClassifier.MacroResource resource = ordered[i];
            if (resources.contains(resource)) {
               ReentrantLock lock = RESOURCE_LOCKS.get(resource);
               if (lock.isHeldByCurrentThread()) {
                  lock.unlock();
               }
            }
         }
      }
   }

   static MacroVariableStore getCurrentVariableStore() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run == null ? null : run.variables;
   }

   private static void runWithCurrentRun(MacroExecutor.MacroRun run, Runnable task) {
      MacroExecutor.MacroRun previous = CURRENT_RUN.get();
      if (run != null) {
         CURRENT_RUN.set(run);
      }

      try {
         task.run();
      } finally {
         if (previous != null) {
            CURRENT_RUN.set(previous);
         } else {
            CURRENT_RUN.remove();
         }
      }
   }

   private static MacroExecutor.ActionSliceResult init_MacroExecutor_Stat_List_I(List<MacroAction> actions, int currentIndex) {
      if (actions != null && currentIndex >= 0 && currentIndex < actions.size() - 1) {
         ArrayList<MacroAction> collected = new ArrayList<>();
         int lastIndex = -1;

         for (int j = currentIndex + 1; j < actions.size(); j++) {
            MacroAction candidate = actions.get(j);
            MacroAction boundary = originalMacroStep(candidate);
            if (boundary instanceof DisconnectAction
               || boundary instanceof JoinAction
               || boundary instanceof RaceAction
               || boundary instanceof ReportAction
               || boundary instanceof RepeatAction
               || boundary instanceof BranchAction
               || boundary instanceof FinallyAction) {
               break;
            }

            if (candidate != null && candidate.isEnabled() && RaceAction.load(candidate)) {
               collected.add(candidate);
               lastIndex = j;
            }
         }

         return collected.isEmpty()
            ? MacroExecutor.ActionSliceResult.emptySlice()
            : new MacroExecutor.ActionSliceResult(List.copyOf(collected), lastIndex);
      } else {
         return MacroExecutor.ActionSliceResult.emptySlice();
      }
   }

   private static void refreshRunsSnapshot() {
      runsView = List.copyOf(ACTIVE_RUNS.values());
   }

   private static List<MacroExecutor.MacroRun> runsSnapshot() {
      return runsView;
   }

   public static MacroExecutor.MacroRunOutcome getRunOutcome(long id) {
      synchronized (RUNS_LOCK) {
         return RUN_OUTCOMES.get(id);
      }
   }

   private static void finishCurrentRun() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && run.finished.compareAndSet(false, true)) {
         MacroToolBridge.endSession(run.id, !"COMPLETED".equals(run.outcome));
         stopMacroRun(run, false);
         endFpsOverride(run);
         endPingSpoofOverride(run);
         synchronized (RUNS_LOCK) {
            RUN_OUTCOMES.put(run.id, new MacroExecutor.MacroRunOutcome(run.id, run.outcome, run.statusText, run.lastCompletedStep));

            while (RUN_OUTCOMES.size() > 128) {
               RUN_OUTCOMES.remove(RUN_OUTCOMES.keySet().iterator().next());
            }

            ACTIVE_RUNS.remove(run.id);
            RUNS_BY_ID.remove(run.id);
            refreshRunsSnapshot();
            if (ACTIVE_RUNS.isEmpty()) {
               lastAllRunsEndedMs = System.currentTimeMillis();
            }

            RUN_EPOCH.incrementAndGet();
            refreshVisibleState();
         }

         if (!ANY_RUN_ACTIVE.get()) {
            autismclient.util.BlockPreviewRenderer.clear();
            MacroFpsOverrideManager.clearAll();
            PingSpoofScheduler.clearRunOverrides();
         }

         CURRENT_RUN.remove();
      }
   }

   private static boolean anyRunActive() {
      for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
         if (run.running.get()) {
            return true;
         }
      }

      return false;
   }

   private static MacroExecutor.MacroRun findVisibleRun() {
      for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
         if (run.running.get() && !run.flags.hiddenFromHud()) {
            return run;
         }
      }

      return null;
   }

   private static int countActiveRuns() {
      int count = 0;

      for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
         if (run.running.get()) {
            count++;
         }
      }

      return count;
   }

   static boolean hasRunCapacity(int activeCount) {
      return Math.max(0, activeCount) < 4;
   }

   private static void refreshVisibleState() {
      visibleRun = findVisibleRun();
      visibleRunActive = visibleRun != null;
      visibleMacro = visibleRun != null ? visibleRun.macro : null;
      statusText = visibleRun != null ? visibleRun.statusText : "";
      currentStep = visibleRun != null ? visibleRun.currentStep : -1;
      totalSteps = visibleRun != null ? visibleRun.totalSteps : 0;
      ANY_RUN_ACTIVE.set(anyRunActive());
   }

   private static boolean anyRunActiveLocked() {
      synchronized (RUNS_LOCK) {
         return anyRunActive();
      }
   }

   public static boolean hasVisibleActiveRun() {
      return visibleRunActive;
   }

   public static boolean isHoldingMovementInput() {
      ReentrantLock inputLock = RESOURCE_LOCKS.get(MacroResourceClassifier.MacroResource.INPUT);
      if (inputLock != null && inputLock.isLocked()) {
         return true;
      }

      if (!GLOBAL_HELD_MOVES.isEmpty()) {
         return true;
      }

      for (MacroAction action : GLOBAL_HELD_ACTIONS) {
         if (action instanceof MoveAction || action instanceof JumpAction || action instanceof SneakAction || action instanceof SprintAction) {
            return true;
         }
      }

      synchronized (RUNS_LOCK) {
         for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
            if (run != null && run.running.get()) {
               if (!run.heldMoves.isEmpty()) {
                  return true;
               }

               for (MacroAction action : run.heldActions) {
                  if (action instanceof MoveAction || action instanceof JumpAction || action instanceof SneakAction || action instanceof SprintAction) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private static void reportLanProgress(MacroExecutor.MacroRun run, int step, int total, String macroName) {
      if (run == null || !run.flags.suppressLanSync()) {
         try {
            autismclient.util.LanSyncManager sync = autismclient.util.LanSyncManager.getInstance();
            if (sync.isInSession()) {
               sync.reportMacroStepProgress(step, total, macroName);
            }
         } catch (Exception var5) {
         }
      }
   }

   private static void stopMacroRun(MacroExecutor.MacroRun run, boolean interruptThread) {
      if (run != null) {
         MacroToolBridge.endSession(run.id, true);
         if (run.running.getAndSet(false)) {
            RUN_EPOCH.incrementAndGet();
         }

         run.heldActions.clear();

         for (MacroExecutor.AsyncActionHandle wait : new ArrayList<>(run.asyncWaits.values())) {
            wait.runCleanup();
         }

         run.asyncWaits.clear();
         releaseMoves(run.heldMoves, Minecraft.getInstance(), run);
         run.heldMoves.clear();
         syncHeldKeys(Minecraft.getInstance());
         synchronized (run.packetWaitLock) {
            if (run.packetWaitFuture != null) {
               run.packetWaitFuture.cancel(true);
            }

            run.packetWaitFuture = null;
            run.waitingForPacket.set(false);
            run.packetWaitTarget.set("");
         }

         CompletableFuture<Void> chatFuture = run.chatWaitFuture;
         if (chatFuture != null) {
            chatFuture.cancel(true);
         }

         run.chatWaitFuture = null;
         run.waitingForChat.set(false);
         run.chatWaitAction = null;
         run.aiOperationResult = null;
         run.mineTracker = null;
         Minecraft mc = Minecraft.getInstance();
         PacketGateManager.closeRun(run.id, mc == null ? null : mc.getConnection());
         MacroFpsOverrideManager.clearOverride(run.id);
         PingSpoofScheduler.stop(run.id);
         releaseSmoothLook(run);
         releaseNavigation(run, mc);
         Thread thread = run.thread;
         if (interruptThread && thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
         }
      }
   }

   private static void releaseMoves(ConcurrentHashMap<MoveAction, int[]> moves, Minecraft mc, MacroExecutor.MacroRun owner) {
      if (moves != null && !moves.isEmpty() && mc != null) {
         List<MoveAction> toRelease = new ArrayList<>(moves.keySet());
         mc.execute(() -> {
            for (MoveAction move : toRelease) {
               try {
                  releaseMoveIfUnowned(owner, move, mc);
               } catch (Exception var6) {
               }
            }
         });
      }
   }

   private static void releaseMoveIfUnowned(MacroExecutor.MacroRun owner, MoveAction move, Minecraft mc) {
      if (move != null && mc != null && !isResourceLocked(MacroResourceClassifier.MacroResource.INPUT)) {
         boolean stillOwned = false;
         synchronized (RUNS_LOCK) {
            for (MacroExecutor.MacroRun active : ACTIVE_RUNS.values()) {
               if (active != owner && active.running.get()) {
                  for (MoveAction other : active.heldMoves.keySet()) {
                     if (other.direction == move.direction) {
                        stillOwned = true;
                        break;
                     }
                  }

                  if (stillOwned) {
                     break;
                  }
               }
            }
         }

         if (!stillOwned && owner != null) {
            for (MoveAction other : GLOBAL_HELD_MOVES.keySet()) {
               if (other.direction == move.direction) {
                  stillOwned = true;
                  break;
               }
            }
         }

         if (!stillOwned) {
            move.releaseKey(mc);
         }
      }
   }

   private static void syncHeldKeys(Minecraft mc) {
      if (!isResourceLocked(MacroResourceClassifier.MacroResource.INPUT) && mc != null) {
         mc.execute(() -> {
            syncSneakKey(mc);
            syncSprintKey(mc);
         });
      }
   }

   private static void syncSneakKey(Minecraft mc) {
      if (!isResourceLocked(MacroResourceClassifier.MacroResource.INPUT) && mc != null && mc.options != null && mc.options.keyShift != null) {
         boolean held = isActionTypeHeld(SneakAction.class);
         mc.options.keyShift.setDown(held);
      }
   }

   private static void syncSprintKey(Minecraft mc) {
      if (!isResourceLocked(MacroResourceClassifier.MacroResource.INPUT) && mc != null) {
         boolean held = isActionTypeHeld(SprintAction.class);
         if (mc.options != null && mc.options.keySprint != null) {
            mc.options.keySprint.setDown(held);
         }

         if (mc.player != null) {
            mc.player.setSprinting(held);
         }
      }
   }

   private static boolean isActionTypeHeld(Class<? extends MacroAction> type) {
      for (MacroAction action : GLOBAL_HELD_ACTIONS) {
         if (type.isInstance(action)) {
            return true;
         }
      }

      synchronized (RUNS_LOCK) {
         for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
            if (run.running.get()) {
               for (MacroAction action : run.heldActions) {
                  if (type.isInstance(action)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private static String macroNameKey(String macroName) {
      return macroName == null ? "" : macroName.trim().toLowerCase(Locale.ROOT);
   }

   private static String macroNameOf(autismclient.util.AutismMacro macro) {
      return macro != null && macro.name != null ? macro.name : "";
   }

   private static boolean matchesMacro(MacroExecutor.MacroRun run, autismclient.util.AutismMacro macro, String macroName) {
      if (run != null && run.running.get()) {
         if (macro == null || run.sourceMacro != macro && run.macro != macro) {
            String targetKey = macroNameKey(macroName);
            if (targetKey.isEmpty() && macro != null) {
               targetKey = macroNameKey(macro.name);
            }

            return !targetKey.isEmpty() && macroNameKey(macroNameOf(run.macro)).equals(targetKey);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static void executeMacro(autismclient.util.AutismMacro macro) {
      startMacro(macro);
   }

   public static long startMacro(autismclient.util.AutismMacro macro) {
      return startMacro(macro, MacroExecutor.ExecutionFlags.DEFAULT);
   }

   static boolean suppressLegacyMultiSteps(List<MacroAction> actions) {
      boolean hasLegacyMulti = false;
      for (int index = 0; index < actions.size(); index++) {
         if (actions.get(index) instanceof MultiAction multi) {
            hasLegacyMulti = true;
            int lastCovered = index + multi.bodySize(actions, index);
            for (int row = index + 1; row <= lastCovered; row++) {
               MacroAction step = actions.get(row);
               // Keep label identity for jumps while suppressing it in action collectors.
               if (step instanceof LabelAction label) {
                  label.setEnabled(false);
               } else {
                  actions.set(row, new SkippedLegacyMultiStep(step));
               }
            }
            index = lastCovered;
         }
      }
      return hasLegacyMulti;
   }

   private static MacroAction originalMacroStep(MacroAction action) {
      return action instanceof SkippedLegacyMultiStep skipped ? skipped.original() : action;
   }

   public static long startMacro(autismclient.util.AutismMacro macro, MacroExecutor.ExecutionFlags options) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.ByteReader.init()) {
         return -1L;
      }

      if (autismclient.modules.aD.read_Z()) {
         return -1L;
      }

      MacroExecutor.ExecutionFlags runOptions = options == null ? MacroExecutor.ExecutionFlags.DEFAULT : options;
      if (macro != null && macro.actions != null && !macro.actions.isEmpty()) {
         autismclient.util.AutismMacro executionMacro;
         try {
            executionMacro = macro.copy();
         } catch (Throwable copyError) {
            if (!runOptions.silent()) {
               AutismClientMessaging.sendPrefixed("§cCould not prepare macro run.");
            }

            return -1L;
         }

         if (executionMacro != null && executionMacro.actions != null && !executionMacro.actions.isEmpty()) {
            // Keep skipped rows in the execution copy so grouped steps and jumps retain their indices.
            boolean hasLegacyMulti = suppressLegacyMultiSteps(executionMacro.actions);
            MacroExecutor.MacroRun runState;
            synchronized (RUNS_LOCK) {
               String name = macro.name == null ? "" : macro.name;
               String nameKey = macroNameKey(name);
               if (runOptions.allowDuplicateName()) {
                  if (!hasRunCapacity(countActiveRuns())) {
                     return -1L;
                  }
               } else {
                  for (MacroExecutor.MacroRun active : ACTIVE_RUNS.values()) {
                     if (!active.flags.allowDuplicateName()) {
                        String activeName = active.macro != null && active.macro.name != null ? active.macro.name : "";
                        if (active.running.get() && macroNameKey(activeName).equals(nameKey)) {
                           if (!runOptions.silent()) {
                              AutismClientMessaging.sendPrefixed("§eMacro already running: " + name);
                           }

                           return -1L;
                        }
                     }
                  }

                  if (!hasRunCapacity(countActiveRuns())) {
                     if (!runOptions.silent()) {
                        AutismClientMessaging.sendPrefixed("§cOnly 4 macros can run at the same time.");
                     }

                     return -1L;
                  }
               }

               runState = new MacroExecutor.MacroRun(NEXT_RUN_ID.getAndIncrement(), macro, executionMacro, runOptions);
               if (ACTIVE_RUNS.isEmpty()) {
                  clearStaleCompletionCaches();
               }

               ACTIVE_RUNS.put(runState.id, runState);
               RUNS_BY_ID.put(runState.id, runState);
               refreshRunsSnapshot();
               RUN_EPOCH.incrementAndGet();
               refreshVisibleState();
            }

            if (!runOptions.silent()) {
               AutismClientMessaging.sendPrefixed("§aStarted macro: " + macro.name);
            }

            reportLanProgress(runState, 0, executionMacro.actions.size(), executionMacro.name);
            Thread thread = new Thread(() -> runGuarded(runState), "Autism-Macro-" + runState.id);
            thread.setDaemon(true);
            lastMacroThread = thread;
            runState.thread = thread;

            try {
               thread.start();
            } catch (Throwable startError) {
               stopMacroRun(runState, false);
               synchronized (RUNS_LOCK) {
                  ACTIVE_RUNS.remove(runState.id);
                  RUNS_BY_ID.remove(runState.id);
                  refreshRunsSnapshot();
                  if (ACTIVE_RUNS.isEmpty()) {
                     lastAllRunsEndedMs = System.currentTimeMillis();
                  }

                  RUN_EPOCH.incrementAndGet();
                  refreshVisibleState();
                  return -1L;
               }
            }

            if (hasLegacyMulti) {
               String warning = "Legacy Multi blocks are unsupported and will be skipped in " + macro.name + ".";
               if (runOptions.silent()) {
                  AutismClientAddon.LOG.info(warning);
               } else {
                  AutismClientMessaging.sendPrefixed("§e" + warning);
               }
            }
            return runState.id;
         } else {
            return -1L;
         }
      } else {
         if (runOptions.silent()) {
            return -1L;
         }

         AutismClientMessaging.sendPrefixed("§cMacro has no actions!");
         return -1L;
      }
   }

   public static boolean isRunActive(long id) {
      if (id < 0L) {
         return false;
      }

      MacroExecutor.MacroRun run = RUNS_BY_ID.get(id);
      return run != null && run.running.get();
   }

   public static long getRunEpoch() {
      return RUN_EPOCH.get();
   }

   public static long getCurrentRunId() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run == null ? -1L : run.id;
   }

   public static void stopRun(long id) {
      if (id >= 0L) {
         MacroExecutor.MacroRun run;
         synchronized (RUNS_LOCK) {
            run = ACTIVE_RUNS.get(id);
         }

         if (run != null) {
            stopMacroRun(run, true);
            synchronized (RUNS_LOCK) {
               refreshVisibleState();
            }
         }
      }
   }

   private static void runGuarded(MacroExecutor.MacroRun runState) {
      CURRENT_RUN.set(runState);

      try {
         executeRun(runState);
      } catch (Throwable var8) {
         Throwable fatal = var8;
         if (!runState.flags.silent()) {
            try {
               AutismClientMessaging.sendPrefixed("§cMacro crashed: " + fatal.getMessage());
            } catch (Throwable var8x) {
            }
         }
      } finally {
         finishCurrentRun();
         CURRENT_RUN.remove();
      }
   }

   private static void executeRun(MacroExecutor.MacroRun runState) {
      CURRENT_RUN.set(runState);
      MacroVariableContext.clear();
      autismclient.util.AutismMacro macro = runState.macro;
      Minecraft mc = Minecraft.getInstance();
      int loopCount = 0;
      int maxLoops = macro.loop ? (macro.loopCount == -1 ? 2147483647 : macro.loopCount) : 1;
      heldActions().clear();
      heldMoves().clear();
      Thread persistentThread = new Thread(() -> {
         CURRENT_RUN.set(runState);

         try {
            while (isStillRunning() && (AutismLiteVariant.enabled() || autismclient.e.BitSetUtil.init())) {
               if (!heldActions().isEmpty() || !heldMoves().isEmpty()) {
                  MacroExecutor.MacroRun scheduledRunx = CURRENT_RUN.get();
                  mc.execute(() -> runWithCurrentRun(scheduledRunx, () -> {
                     if (isStillRunning() && !isResourceLocked(MacroResourceClassifier.MacroResource.INPUT)) {
                        ReentrantLock inputLock = RESOURCE_LOCKS.get(MacroResourceClassifier.MacroResource.INPUT);
                        if (inputLock.tryLock()) {
                           try {
                              for (MacroAction pa : heldActions()) {
                                 try {
                                    pa.execute(mc);
                                 } catch (Exception var10x) {
                                 }
                              }

                              for (Entry<MoveAction, int[]> entry : heldMoves().entrySet()) {
                                 MoveAction bma = entry.getKey();
                                 int[] ticks = entry.getValue();
                                 if (ticks[0] > 0) {
                                    bma.execute(mc);
                                    ticks[0]--;
                                 } else {
                                    releaseMoveIfUnowned(scheduledRunx, bma, mc);
                                    heldMoves().remove(bma);
                                 }
                              }
                           } finally {
                              inputLock.unlock();
                           }
                        }
                     }
                  }));
               }

               try {
                  CompletableFuture<Void> tf = MacroWaitRegistry.awaitTickSync();
                  tf.get(100L, TimeUnit.MILLISECONDS);
               } catch (Exception e) {
                  if (!isStillRunning()) {
                     break;
                  }
               }
            }
         } finally {
            releaseMoves(heldMoves(), mc, runState);
            heldMoves().clear();
            CURRENT_RUN.remove();
         }
      }, "Autism-Macro-Persistent-" + runState.id);
      persistentThread.setDaemon(true);
      persistentThread.start();

      try {
         for (; isStillRunning() && loopCount < maxLoops; loopCount++) {
            if (macro.actions.isEmpty()) {
               if (!runState.flags.silent()) {
                  AutismClientMessaging.sendPrefixed("§cMacro stopped: no actions left.");
               }

               stopCurrentRunOrAll();
               break;
            }

            setRunStatus("Loop " + (loopCount + 1) + (maxLoops == 2147483647 ? "" : "/" + maxLoops));
            autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
            boolean burstMode = module.isPacketBurstMode();
            totalSteps = macro.actions.size();
            runState.elseBranchIndex = -1;
            runState.elseSkipSteps = 0;
            runState.backwardJumpBurst = 0;
            runState.lastCompletedStep = 0;

            for (int i = 0; i < macro.actions.size() && isStillRunning(); i++) {
               if (!AutismLiteVariant.enabled() && !autismclient.e.RingBuffer.init()) {
                  stopCurrentRunOrAll();
                  break;
               }

               setRunProgress(i, macro.actions.size());
               endOverridesOutside(runState, i);
               if (runState.elseBranchIndex == i && runState.elseSkipSteps > 0) {
                  i += runState.elseSkipSteps - 1;
                  runState.elseBranchIndex = -1;
                  runState.elseSkipSteps = 0;
               } else {
                  MacroAction action = macro.actions.get(i);
                  if (action instanceof MultiAction multi) {
                     i += multi.bodySize(macro.actions, i);
                  } else if (!action.isEnabled()) {
                     if (action instanceof RaceAction disabledRace) {
                        i += disabledRace.countBodyActions(macro.actions, i);
                     } else if (action instanceof ReportAction disabledReport) {
                        i += disabledReport.coveredCount(macro.actions, i);
                     }
                  } else if (DynamicBindingManager.apply(action, mc) && canResolveTemplates(action, mc)) {
                     if (!hasAsyncWait(action)) {
                        resetCaptureOutputs(action);
                     }

                     if (action instanceof BranchAction branch) {
                        boolean branchMatches = branch.matches(mc);
                        if (branchMatches) {
                           runState.elseBranchIndex = i + 1 + Math.max(0, branch.thenSteps);
                           runState.elseSkipSteps = Math.max(0, branch.elseSteps);
                        } else {
                           i += Math.max(0, branch.thenSteps);
                        }
                     } else if (action instanceof FinallyAction cleanup) {
                        int bodyCount = Math.max(0, cleanup.bodyCount);
                        runState.finallyActions.clear();

                        for (int c = 1; c <= bodyCount && i + c < macro.actions.size(); c++) {
                           MacroAction body = macro.actions.get(i + c);
                           if (body != null && body.isEnabled()) {
                              runState.finallyActions.add(body);
                           }
                        }

                        i += bodyCount;
                     } else if (action instanceof IfAction ifA) {
                        runState.elseBranchIndex = -1;
                        runState.elseSkipSteps = 0;
                        if (ifA.init(mc)) {
                           runState.elseBranchIndex = i + 1 + Math.max(0, ifA.thenSteps);
                           runState.elseSkipSteps = Math.max(0, ifA.elseSteps);
                        } else {
                           i += Math.max(0, ifA.thenSteps);
                        }
                     } else if (!(action instanceof LabelAction)) {
                        if (action instanceof FlowAction flow) {
                           if (flow.shouldRun(mc)) {
                              int newI = applyFlowJump(runState, macro, i, flow, mc);
                              if (newI == -2147483648) {
                                 reportLanProgress(runState, i + 1, macro.actions.size(), macro.name);
                                 stopCurrentRunOrAll();
                                 break;
                              }

                              i = newI;
                           }
                        } else {
                           armNextListeningAction(mc, macro.actions, i);
                           EnumSet<MacroResourceClassifier.MacroResource> heldResources = acquireResourceLocksFor(action);

                           try {
                              if (guiPreconditionMet(mc, action)) {
                                 applyRaycastAim(mc, action);
                                 CompletableFuture<Void> postGuiFuture = !(action instanceof ItemAction) && !(action instanceof DropAction) && !isScreenOpeningAction(action)
                                    ? postGuiWaitFuture(mc, action)
                                    : null;
                                 if (burstMode && action instanceof SendPacketAction) {
                                    List<SendPacketAction> packetBatch = new ArrayList<>();

                                    int batchEnd;
                                    for (batchEnd = i; batchEnd < macro.actions.size() && macro.actions.get(batchEnd) instanceof SendPacketAction; batchEnd++) {
                                       packetBatch.add((SendPacketAction)macro.actions.get(batchEnd));
                                    }

                                    sendPacketBatch(mc, packetBatch);
                                    i = batchEnd - 1;
                                 } else {
                                    if (action instanceof ToolsAction tools) {
                                       runToolsAction(mc, tools);
                                    } else if (action instanceof DelayAction da) {
                                       if (da.useTicks) {
                                          for (int t = 0; t < da.delayTicks && isStillRunning(); t++) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.awaitTickSync();
                                             awaitWhileRunning(future);
                                          }
                                       } else {
                                          sleep(da.delayMs);
                                       }
                                    } else if (action instanceof CustomMenuAction customMenu) {
                                       setRunStatus(customMenu.getDisplayName());
                                       customMenu.execute(mc);
                                    } else if (action instanceof FpsAction fps) {
                                       setRunStatus(fps.getDisplayName());
                                       if (fps.getEndMode() == FpsAction.EndMode.TIMEOUT) {
                                          endFpsOverride(runState);
                                          long fpsDurationNanos = fps.getDurationMs() * 1000000L;
                                          long fpsDeadline = System.nanoTime() + fpsDurationNanos;
                                          MacroFpsOverrideManager.setOverride(runState.id, Math.max(0, fps.maxFps), fpsDurationNanos);
                                          if (fps.continueNextActions) {
                                             runState.fpsTimeoutDeadlineNanos = fpsDeadline;
                                          } else {
                                             long fpsRemaining;
                                             while (isStillRunning() && (fpsRemaining = fpsDeadline - System.nanoTime()) > 0L) {
                                                sleep(Math.min(25L, fpsRemaining / 1000000L + 1L));
                                             }

                                             MacroFpsOverrideManager.clearOverride(runState.id);
                                             runState.fpsTimeoutDeadlineNanos = 0L;
                                          }
                                       } else {
                                          int covered = fps.getEndMode() == FpsAction.EndMode.END
                                             ? macro.actions.size() - 1 - i
                                             : fps.getStepCount();
                                          int endIndex = coveredEndIndex(macro, i, covered);
                                          startFpsOverride(runState, i, endIndex, Math.max(1, fps.maxFps));
                                          runState.fpsTimeoutDeadlineNanos = 0L;
                                       }
                                    } else if (action instanceof SPingAction sping) {
                                       setRunStatus(sping.getDisplayName());
                                       if (sping.getEndMode() == SPingAction.EndMode.TIMEOUT) {
                                          endPingSpoofOverride(runState);
                                          long spingDurationNanos = sping.getDurationMs() * 1000000L;
                                          long spingDeadline = System.nanoTime() + spingDurationNanos;
                                          PingSpoofScheduler.start(runState.id, Math.max(0, sping.pingDelayMs), sping.realIncoming, sping.realOutgoing, spingDurationNanos);
                                          if (sping.continueNextActions) {
                                             runState.spingTimeoutDeadlineNanos = spingDeadline;
                                          } else {
                                             long spingRemaining;
                                             while (isStillRunning() && (spingRemaining = spingDeadline - System.nanoTime()) > 0L) {
                                                sleep(Math.min(25L, spingRemaining / 1000000L + 1L));
                                             }

                                             PingSpoofScheduler.stop(runState.id);
                                             runState.spingTimeoutDeadlineNanos = 0L;
                                          }
                                       } else {
                                          int covered = sping.getEndMode() == SPingAction.EndMode.END ? macro.actions.size() - 1 - i : sping.getStepCount();
                                          int endIndex = coveredEndIndex(macro, i, covered);
                                          startPingSpoofOverride(
                                             runState, i, endIndex, Math.max(0, sping.pingDelayMs), sping.realIncoming, sping.realOutgoing
                                          );
                                          runState.spingTimeoutDeadlineNanos = 0L;
                                       }
                                    } else if (action instanceof WaitForPacketAction packetAction) {
                                       packetAction.matchedPacket = null;
                                       packetAction.matchedDirection = "";
                                       MacroExecutor.MacroRun packetRun = CURRENT_RUN.get();
                                       if (packetRun != null) {
                                          packetRun.matchedPacket = null;
                                          packetRun.matchedPacketDirection = "";
                                       }

                                       List<String> targets = packetAction.targets();
                                       MacroExecutor.AsyncActionHandle prearmed = takeAsyncWait(action);
                                       int startTarget = 0;
                                       if (prearmed != null) {
                                          try {
                                             awaitWithTimeout(prearmed.future, 0L);
                                             startTarget = prearmed.completedPacketTargets;
                                          } finally {
                                             prearmed.runCleanup();
                                          }
                                       }

                                       if (targets.isEmpty()) {
                                          if (prearmed == null) {
                                             waitForPacketTarget("");
                                          }
                                       } else {
                                          for (int targetIndex = Math.min(startTarget, targets.size());
                                             targetIndex < targets.size() && isStillRunning();
                                             targetIndex++
                                          ) {
                                             waitForPacketTarget(targets.get(targetIndex));
                                          }
                                       }

                                       applyMatchedPacket(packetAction);
                                    } else if (action instanceof WaitPacketMatchAction wpma) {
                                       setRunStatus(wpma.getDisplayName());
                                       if (!awaitPrearmedWait(wpma, wpma.timeoutMs)) {
                                          waitForPacketMatch(wpma);
                                       }
                                    } else if (action instanceof WaitInventoryPredicateAction wipa) {
                                       setRunStatus(wipa.getDisplayName());
                                       if (!awaitPrearmedWait(wipa, wipa.timeoutMs)) {
                                          waitUntilOrTimeout(() -> wipa.isSatisfied(mc), wipa.timeoutMs);
                                       }
                                    } else if (action instanceof WaitDurabilityAction wda) {
                                       setRunStatus(wda.getDisplayName());
                                       if (wda.useNext) {
                                          waitForDurability(mc, wda);
                                       } else if (!awaitPrearmedWait(wda, wda.timeoutMs)) {
                                          waitUntilOrTimeout(() -> wda.isSatisfied(mc), wda.timeoutMs);
                                       }
                                    } else if (action instanceof WaitFreeSlotsAction wfsa) {
                                       setRunStatus(wfsa.getDisplayName());
                                       if (!awaitPrearmedWait(wfsa, wfsa.timeoutMs)) {
                                          waitUntilOrTimeout(() -> wfsa.test(mc), wfsa.timeoutMs);
                                       }
                                    } else if (action instanceof WaitEntityTargetAction weta) {
                                       setRunStatus(weta.getDisplayName());
                                       if (!awaitPrearmedWait(weta, weta.timeoutMs)) {
                                          waitUntilOrTimeout(() -> weta.isSatisfied(mc), weta.timeoutMs);
                                       }
                                    } else if (action instanceof WaitGuiTypeAction wgta) {
                                       setRunStatus(wgta.getDisplayName());
                                       if (!hasAsyncWait(wgta)) {
                                          clearCaptures(wgta);
                                       }

                                       if (!awaitPrearmedWait(wgta, wgta.timeoutMs)) {
                                          if (wgta.waitMode == WaitGuiTypeAction.WaitMode.CLOSE) {
                                             waitUntilOrTimeout(guiTypeWaitCondition(wgta, mc), wgta.timeoutMs);
                                             applyStashedCaptures(wgta);
                                          } else {
                                             waitUntilOrTimeout(() -> {
                                                Optional<Map<String, MacroVarValue>> captured = wgta.capture(mc);
                                                captured.ifPresent(values -> stashCaptures(wgta, (Map<String, MacroVarValue>)values));
                                                return captured.isPresent();
                                             }, wgta.timeoutMs);
                                             applyStashedCaptures(wgta);
                                          }
                                       }
                                    } else if (action instanceof WaitForHealthAction wh) {
                                       setRunStatus(wh.statusText());

                                       try {
                                          if (!awaitPrearmedWait(wh, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.awaitHealth(wh.healthThreshold, wh.below);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var248) {
                                       }
                                    } else if (action instanceof WaitForBlockAction wba) {
                                       setRunStatus(wba.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wba, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForBlock(wba);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var247) {
                                       }
                                    } else if (action instanceof WaitForGuiAction wga) {
                                       if (!hasAsyncWait(wga)) {
                                          clearCaptures(wga);
                                       }

                                       if (!awaitPrearmedWait(wga, 0L)) {
                                          if (wga.waitMode == WaitForGuiAction.WaitMode.CLOSE) {
                                             setRunStatus("Waiting for GUI close: " + wga.guiTitle);
                                             waitUntilOrTimeout(guiWaitCondition(wga, mc), wga.timeoutMs);
                                             applyStashedCaptures(wga);
                                          } else {
                                             setRunStatus("Waiting for GUI: " + wga.guiTitle);
                                             waitUntilOrTimeout(() -> {
                                                Optional<Map<String, MacroVarValue>> captured = wga.capture(mc.gui.screen());
                                                captured.ifPresent(values -> stashCaptures(wga, (Map<String, MacroVarValue>)values));
                                                return captured.isPresent();
                                             }, wga.timeoutMs);
                                             applyStashedCaptures(wga);
                                          }
                                       }
                                    } else if (action instanceof WaitForCooldownAction wca) {
                                       MacroItemTarget cooldownTarget = targetOrLegacy(wca.itemTarget, wca.itemName).resolveTemplates(mc);
                                       if (cooldownTarget == null) {
                                          continue;
                                       }

                                       String cooldownLabel = describeTarget(cooldownTarget);
                                       setRunStatus(
                                          "Waiting for cooldown: "
                                             + (!cooldownLabel.isEmpty() ? cooldownLabel : (wca.checkMainInteractionHand ? "Main Hand" : "Off Hand"))
                                       );

                                       try {
                                          if (!awaitPrearmedWait(wca, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.awaitCooldown(cooldownTarget, wca.checkMainInteractionHand);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var246) {
                                       }
                                    } else if (action instanceof WaitPosAction wpa) {
                                       setRunStatus("Waiting for Pos: " + String.format("%.0f, %.0f, %.0f", wpa.f_x_D, wpa.f_y_D, wpa.f_z_D));

                                       try {
                                          if (!awaitPrearmedWait(wpa, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForPosition(
                                                wpa.f_x_D, wpa.f_y_D, wpa.f_z_D, wpa.leeway, wpa.checkRotation, wpa.yaw, wpa.pitch, wpa.rotLeeway
                                             );
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var245) {
                                       }
                                    } else if (action instanceof JumpAction ja) {
                                       mc.execute(() -> ja.execute(mc));
                                       if (ja.tap) {
                                          CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                                          awaitWhileRunning(tickFuture);
                                       } else {
                                          for (int t = 0; t < ja.durationTicks && isStillRunning(); t++) {
                                             CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                                             awaitWhileRunning(tickFuture);
                                          }
                                       }

                                       mc.execute(() -> ja.releaseJump(mc));
                                    } else if (action instanceof SneakAction sna) {
                                       if (sna.persistent && sna.sneak) {
                                          heldActions().removeIf(p -> p instanceof SneakAction);
                                          heldActions().add(sna);
                                       } else if (!sna.sneak) {
                                          heldActions().removeIf(p -> p instanceof SneakAction);
                                       }

                                       if (!sna.persistent && sna.sneak) {
                                          postToClientThread(mc, () -> sna.execute(mc), sna.h_());
                                       } else {
                                          postToClientThread(mc, () -> syncSneakKey(mc), sna.h_());
                                       }
                                    } else if (action instanceof SprintAction spa) {
                                       if (spa.persistent && spa.sprint) {
                                          heldActions().removeIf(p -> p instanceof SprintAction);
                                          heldActions().add(spa);
                                       } else if (!spa.sprint) {
                                          heldActions().removeIf(p -> p instanceof SprintAction);
                                       }

                                       if (!spa.persistent && spa.sprint) {
                                          postToClientThread(mc, () -> spa.execute(mc), spa.h_());
                                       } else {
                                          postToClientThread(mc, () -> syncSprintKey(mc), spa.h_());
                                       }
                                    } else if (action instanceof DisconnectAction da2) {
                                       reportLanProgress(runState, i + 1, macro.actions.size(), macro.name);
                                       if (da2.mode == DisconnectAction.DisconnectMode.DISCONNECT) {
                                          if (da2.delayMs > 0) {
                                             sleep(da2.delayMs);
                                          }

                                          mc.execute(() -> da2.execute(mc));
                                       } else if (da2.mode == DisconnectAction.DisconnectMode.AUTO_DISCONNECT) {
                                          setRunStatus("Auto Disconnect: waiting for " + da2.trigger.name() + "...");
                                          da2.execute(mc);
                                          setRunStatus("Auto Disconnect: executed");
                                       } else if (da2.mode == DisconnectAction.DisconnectMode.KICK_DUPE && da2.useNextAction) {
                                          List<MacroAction> nextActs = new ArrayList<>();
                                          int lastSkip = -1;

                                          for (int j = i + 1; j < macro.actions.size(); j++) {
                                             MacroAction candidate = macro.actions.get(j);
                                             MacroAction boundary = originalMacroStep(candidate);
                                             if (boundary instanceof DisconnectAction || boundary instanceof JoinAction) {
                                                break;
                                             }

                                             if (candidate.isEnabled()
                                                && !(candidate instanceof MultiAction)
                                                && !(candidate instanceof DelayAction)
                                                && !(candidate instanceof WaitForHealthAction)
                                                && !(candidate instanceof WaitForBlockAction)
                                                && !(candidate instanceof WaitForGuiAction)
                                                && !(candidate instanceof WaitForCooldownAction)
                                                && !(candidate instanceof WaitPosAction)
                                                && !(candidate instanceof WaitMovementAction)
                                                && !(candidate instanceof WaitForEntityAction)
                                                && !(candidate instanceof WaitForSoundAction)
                                                && !(candidate instanceof WaitForSlotChangeAction)
                                                && !(candidate instanceof WaitForPacketAction)
                                                && !(candidate instanceof WaitForChatAction)
                                                && !(candidate instanceof GoToAction)) {
                                                nextActs.add(candidate);
                                                lastSkip = j;
                                             }
                                          }

                                          da2.setFollowingActions(nextActs);
                                          mc.execute(() -> da2.execute(mc));
                                          if (lastSkip > i) {
                                             i = lastSkip;
                                          }
                                       } else {
                                          mc.execute(() -> da2.execute(mc));
                                       }

                                       if (!JoinAction.hasJoinAhead(macro.actions, i + 1, macro.loop)) {
                                          stopCurrentRunOrAll();
                                          break;
                                       }

                                       runState.rejoining = true;
                                       String statusBeforeLeave = runState.statusText;
                                       setRunStatus("Leaving the server");
                                       JoinAction.awaitDisconnect(mc, 10000L);
                                       setRunStatus(statusBeforeLeave);
                                    } else if (action instanceof JoinAction join) {
                                       String statusBefore = runState.statusText;
                                       runState.rejoining = true;

                                       boolean joined;
                                       try {
                                          joined = join.join(mc, MacroExecutor::setRunStatus);
                                       } finally {
                                          runState.rejoining = false;
                                       }

                                       if (!joined) {
                                          stopCurrentRunOrAll();
                                          break;
                                       }

                                       setRunStatus(statusBefore);
                                       reportLanProgress(runState, i + 1, macro.actions.size(), macro.name);
                                    } else if (action instanceof RaceAction race) {
                                       int bodyCount = runRaceAction(mc, module, macro.actions, i, race);
                                       if (bodyCount > 0) {
                                          i += bodyCount;
                                       }
                                    } else if (action instanceof ReportAction reportAction) {
                                       int conditionCount = runReportAction(mc, macro.actions, i, reportAction);
                                       if (conditionCount > 0) {
                                          i += conditionCount;
                                       }
                                    } else if (action instanceof NbtBookAction nba) {
                                       int totalBooks = Math.max(1, nba.bookCount);
                                       long delayMs = Math.max(1, nba.delayTicks) * 50L;
                                       boolean signedAny = false;

                                       for (int b = 0; b < totalBooks && isStillRunning(); b++) {
                                          int bookIdx = b;
                                          CompletableFuture<Boolean> result = new CompletableFuture<>();
                                          mc.execute(() -> result.complete(nba.signBook(mc, bookIdx, totalBooks)));
                                          Boolean success = await(result);
                                          if (!success) {
                                             break;
                                          }

                                          signedAny = true;
                                          if (b < totalBooks - 1) {
                                             sleep(delayMs);
                                          }
                                       }

                                       if (signedAny && nba.disconnectAfter) {
                                          mc.execute(() -> nba.disconnectIfRequested(mc));
                                       }
                                    } else if (action instanceof GoToAction ga) {
                                       runGoToAction(mc, ga);
                                    } else if (action instanceof MoveAction ma) {
                                       if (ma.nonBlocking) {
                                          mc.execute(() -> ma.execute(mc));
                                          heldMoves().put(ma, new int[]{ma.durationTicks - 1});
                                       } else {
                                          for (int t = 0; t < ma.durationTicks && isStillRunning(); t++) {
                                             mc.execute(() -> ma.execute(mc));
                                             CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                                             awaitWhileRunning(tickFuture);
                                          }

                                          MacroExecutor.MacroRun movementRun = CURRENT_RUN.get();
                                          mc.execute(() -> releaseMoveIfUnowned(movementRun, ma, mc));
                                       }
                                    } else if (action instanceof LookAtBlockAction la) {
                                       LookAtBlockAction.LookTarget rotationTarget = resolveLookTarget(mc, la);
                                       if (rotationTarget == null) {
                                          continue;
                                       }

                                       if (la.smooth) {
                                          startSmoothLook(rotationTarget.yaw(), rotationTarget.pitch(), la.getRotationStep());
                                          if (la.waitForCompletion) {
                                             awaitSmoothLookDone();
                                          }
                                       } else {
                                          mc.execute(() -> {
                                             if (mc.player != null) {
                                                mc.player.setYRot(rotationTarget.yaw());
                                                mc.player.setXRot(rotationTarget.pitch());
                                             }
                                          });
                                       }
                                    } else if (action instanceof RepeatAction ra) {
                                       int startIdx = i + 1;
                                       int endIdx = Math.min(startIdx + ra.stepCount, macro.actions.size());

                                       for (int rep = 0; rep < ra.repeatCount && isStillRunning(); rep++) {
                                          setRunStatus("Repeat " + (rep + 1) + "/" + ra.repeatCount);

                                          for (int j = startIdx; j < endIdx && isStillRunning(); j++) {
                                             MacroAction subAction = macro.actions.get(j);
                                             if (subAction.isEnabled()) {
                                                ACTIVE_STEP_SLICE.set(new MacroExecutor.ActiveStepSlice(macro, j + 1, endIdx));
                                                SLICE_STEPS_CONSUMED.set(0);

                                                try {
                                                   executeMacroAction(mc, subAction, module);
                                                   j += Math.max(0, SLICE_STEPS_CONSUMED.get());
                                                } finally {
                                                   SLICE_STEPS_CONSUMED.set(0);
                                                   ACTIVE_STEP_SLICE.remove();
                                                }
                                             }
                                          }
                                       }

                                       i = endIdx - 1;
                                    } else if (action instanceof CaptureValueAction captureValue) {
                                       executeCapture(mc, captureValue);
                                    } else if (action instanceof WaitForChatAction wca) {
                                       setRunStatus("Waiting for chat: " + wca.pattern);
                                       if (!awaitPrearmedWait(wca, wca.timeoutMs)) {
                                          waitForChatMatch(wca);
                                       }
                                    } else if (action instanceof WaitForEntityAction wea) {
                                       setRunStatus(wea.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wea, 0L)) {
                                             waitUntilOrTimeout(() -> wea.isSatisfied(mc), wea.timeoutMs);
                                          }
                                       } catch (CancellationException var242) {
                                       }
                                    } else if (action instanceof WaitForSoundAction wsa) {
                                       String sndDesc = wsa.soundIds.isEmpty() ? "any" : wsa.soundIds.get(0);
                                       setRunStatus("Waiting for sound: " + sndDesc);

                                       try {
                                          if (!awaitPrearmedWait(wsa, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForSound(wsa);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var241) {
                                       }
                                    } else if (action instanceof MineAction mna) {
                                       runMineAction(mc, mna);
                                    } else if (action instanceof InstaBreakAction) {
                                       runInstaBreakAction(mc, (InstaBreakAction)action);
                                    } else if (action instanceof BreakAction breakAction) {
                                       MacroExecutor.ActionSliceResult nextActions = MacroExecutor.ActionSliceResult.emptySlice();
                                       if (breakAction.interact && breakAction.runNextSteps) {
                                          nextActions = init_MacroExecutor_Stat_List_I(macro.actions, i);
                                          breakAction.setNextActions(nextActions.init_List());
                                       }

                                       runBreakAction(mc, breakAction);
                                       if (breakAction.load() && nextActions.lastIndex() > i) {
                                          i = nextActions.lastIndex();
                                       }
                                    } else if (action instanceof WaitForSlotChangeAction wsca) {
                                       setRunStatus(wsca.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wsca, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForSlotChange(wsca);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var240) {
                                       }
                                    } else if (action instanceof WaitForWorldChangeAction world) {
                                       setRunStatus(world.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(world, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForWorldChange(world);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var239) {
                                       }
                                    } else if (action instanceof WaitForPositionDeltaAction wpda) {
                                       setRunStatus(wpda.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wpda, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.awaitPositionDelta(wpda.distance, wpda.horizontalOnly);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var238) {
                                       }
                                    } else if (action instanceof WaitForTeleportAction teleport) {
                                       setRunStatus(teleport.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(teleport, 0L)) {
                                             CompletableFuture<?> future = triggerWaitFuture(mc, teleport);
                                             awaitWithTimeout(future, 0L);
                                          }
                                       } catch (CancellationException var237) {
                                       }
                                    } else if (action instanceof WaitGamemodeChangeAction gm) {
                                       setRunStatus(gm.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(gm, gm.timeoutMs)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForGamemode(gm);
                                             awaitWithTimeout(future, gm.timeoutMs);
                                          }
                                       } catch (CancellationException var236) {
                                       }
                                    } else if (action instanceof WaitMovementAction wm) {
                                       setRunStatus(wm.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wm, 0L)) {
                                             CompletableFuture<?> future = triggerWaitFuture(mc, wm.toWaitAction());
                                             awaitWithTimeout(future, 0L);
                                          }
                                       } catch (CancellationException var235) {
                                       }
                                    } else if (action instanceof WaitForLanStepAction wla) {
                                       setRunStatus(wla.getDisplayName());

                                       try {
                                          if (!awaitPrearmedWait(wla, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.waitForLanStep(wla);
                                             awaitWhileRunning(future);
                                          }
                                       } catch (CancellationException var234) {
                                       }
                                    } else if (action instanceof WaitForMacroStepAction wma) {
                                       setRunStatus(wma.getDisplayName());
                                       if (!awaitPrearmedWait(wma, wma.timeoutMs)) {
                                          waitForMacroStep(wma);
                                       }
                                    } else {
                                       if (action instanceof StopMacroAction) {
                                          setRunStatus("Stopping Macro");
                                          StopMacroAction stopAction = (StopMacroAction)action;
                                          if (stopAction.target != StopMacroAction.StopTarget.SELF) {
                                             stopAction.execute(mc);
                                          } else {
                                             reportLanProgress(runState, i + 1, macro.actions.size(), macro.name);
                                             stopCurrentRunOrAll();
                                          }
                                          break;
                                       }

                                       if (action instanceof TickSyncAction tsa) {
                                          if (mc.level == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.DelayedPacket> preGenerated = collectQueuedPackets(macro, i + 1, tsa.preGenCount);
                                          long targetTick = mc.level.getGameTime() + tsa.tickOffset;
                                          setRunStatus("Tick Sync -> " + targetTick + " (" + preGenerated.size() + " pkts)");

                                          while (isStillRunning() && mc.level.getGameTime() < targetTick) {
                                             LockSupport.parkNanos(1000000L);
                                          }

                                          if (isStillRunning()) {
                                             sendQueuedPackets(mc, preGenerated);
                                             i += countActionsForPackets(macro, i + 1, preGenerated.size());
                                             AutismClientMessaging.sendPrefixed("Tick sync: " + preGenerated.size() + " pkts sent");
                                          }
                                       } else if (action instanceof RevisionSyncAction rsa) {
                                          if (mc.player == null || mc.player.containerMenu == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.DelayedPacket> preGenerated = collectQueuedPackets(macro, i + 1, rsa.preGenCount);
                                          int baseRevision = mc.player.containerMenu.getStateId();
                                          int targetRevision = baseRevision + rsa.revisionOffset;
                                          setRunStatus("Rev Sync -> " + targetRevision + " (" + preGenerated.size() + " pkts)");

                                          while (
                                             isStillRunning()
                                                && mc.player != null
                                                && mc.player.containerMenu != null
                                                && mc.player.containerMenu.getStateId() < targetRevision
                                          ) {
                                             LockSupport.parkNanos(500000L);
                                          }

                                          if (isStillRunning() && mc.player != null && mc.player.containerMenu != null) {
                                             sendQueuedPackets(mc, preGenerated);
                                             i += countActionsForPackets(macro, i + 1, preGenerated.size());
                                             AutismClientMessaging.sendPrefixed("Rev sync: " + preGenerated.size() + " pkts sent");
                                          }
                                       } else if (action instanceof ServerTickSyncAction stsa) {
                                          if (mc.getConnection() == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.DelayedPacket> preGenerated = collectQueuedPackets(macro, i + 1, stsa.preGenCount);

                                          for (int sampleWaitMs = 0; isStillRunning() && !ServerTickPhaseTracker.isWarmedUp() && sampleWaitMs < stsa.maxWaitMs; sampleWaitMs += 50) {
                                             float progress = ServerTickPhaseTracker.getWarmupProgress();
                                             setRunStatus(
                                                String.format("ServerSync warmup %.0f%% (%d samples, %dms)", progress * 100.0F, ServerTickPhaseTracker.getSampleCount(), ServerTickPhaseTracker.warmupMillis())
                                             );
                                             sleep(50L);
                                          }

                                          if (!isStillRunning()) {
                                             continue;
                                          }

                                          long optimalTime = ServerTickPhaseTracker.optimalSendTime(stsa.bufferMs, stsa.ignorePing);
                                          long msUntil = (optimalTime - System.nanoTime()) / 1000000L;
                                          int ping = ServerTickPhaseTracker.getLatencyMs();
                                          String pingStr = stsa.ignorePing ? " (no ping)" : " ping:" + ping + "ms";
                                          setRunStatus("ServerSync in " + Math.max(0L, msUntil) + "ms" + pingStr + " (" + preGenerated.size() + " pkts)");
                                          long startWait = System.nanoTime();
                                          long maxWaitNanos = stsa.maxWaitMs * 1000000L;

                                          while (isStillRunning()) {
                                             long now = System.nanoTime();
                                             if (now - startWait >= maxWaitNanos || now >= optimalTime) {
                                                break;
                                             }

                                             long remaining = optimalTime - now;
                                             if (remaining > 2000000L) {
                                                LockSupport.parkNanos(remaining - 2000000L);
                                             } else {
                                                Thread.onSpinWait();
                                             }
                                          }

                                          if (isStillRunning()) {
                                             sendQueuedPackets(mc, preGenerated);
                                             i += countActionsForPackets(macro, i + 1, preGenerated.size());
                                             long actualDelay = (System.nanoTime() - optimalTime) / 1000000L;
                                             AutismClientMessaging.sendPrefixed("ServerSync: " + preGenerated.size() + " pkts (+" + actualDelay + "ms)");
                                          }
                                       } else if (action instanceof RotateAction ra) {
                                          if (ra.smooth) {
                                             startSmoothLook(ra.yaw, ra.pitch, ra.step());
                                             if (ra.waitForCompletion) {
                                                awaitSmoothLookDone();
                                             }
                                          } else {
                                             mc.execute(() -> action.execute(mc));
                                          }
                                       } else if (action instanceof PacedTpAction pacedTp) {
                                          runPacedTpAction(mc, pacedTp);
                                       } else if (action instanceof LookAtBlockAction la) {
                                          LookAtBlockAction.LookTarget rotationTarget = resolveLookTarget(mc, la);
                                          if (rotationTarget == null) {
                                             continue;
                                          }

                                          if (la.smooth) {
                                             startSmoothLook(rotationTarget.yaw(), rotationTarget.pitch(), la.getRotationStep());
                                             if (la.waitForCompletion) {
                                                awaitSmoothLookDone();
                                             }
                                          } else {
                                             mc.execute(() -> {
                                                if (mc.player != null) {
                                                   mc.player.setYRot(rotationTarget.yaw());
                                                   mc.player.setXRot(rotationTarget.pitch());
                                                }
                                             });
                                          }
                                       } else if (action instanceof UseItemAction) {
                                          runUseItemAction(mc, (UseItemAction)action);
                                       } else if (action instanceof ItemAction ia) {
                                          if (ia.waitForItem || !ia.itemNames.isEmpty()) {
                                             try {
                                                waitForItemTargets(mc, ia);
                                             } catch (CancellationException var233) {
                                             }
                                          }

                                          postGuiFuture = postGuiWaitFuture(mc, ia);
                                          if (ia.useClickDelay && ia.clickDelayMs > 0) {
                                             runItemClickPlan(mc, ia);
                                          } else {
                                             executeGuarded(mc, action, "ItemAction");
                                          }
                                       } else if (!(action instanceof DropAction) && !(action instanceof SwapSlotsAction)) {
                                          if (action instanceof CraftAction craftAction) {
                                             runCraftAction(mc, craftAction);
                                          } else if (action instanceof SendPacketAction) {
                                             if (module != null && module.isInstantExecutionMode()) {
                                                try {
                                                   action.execute(mc);
                                                } catch (Exception e) {
                                                   AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + e.getMessage());
                                                }
                                             } else {
                                                runOnClientThread(mc, () -> {
                                                   try {
                                                      action.execute(mc);
                                                   } catch (Exception ex) {
                                                      AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + ex.getMessage());
                                                   }
                                                });
                                             }
                                          } else if (action instanceof StoreItemAction sia) {
                                             setRunStatus((sia.mode == StoreItemAction.TransferMode.LOOT ? "Looting" : "Storing") + (sia.persistent ? " ∞" : ""));
                                             if (sia.persistent) {
                                                while (isStillRunning()) {
                                                   try {
                                                      runStoreItemAction(mc, sia);
                                                   } catch (Exception e) {
                                                      AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                                                   }

                                                   CompletableFuture<Void> siTick = MacroWaitRegistry.awaitTickSync();
                                                   awaitWhileRunning(siTick);
                                                }
                                             } else {
                                                try {
                                                   runStoreItemAction(mc, sia);
                                                } catch (Exception e) {
                                                   AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                                                }
                                             }
                                          } else if (action instanceof ClickAction ca) {
                                             runClickAction(mc, ca);
                                          } else if (action instanceof PayAction) {
                                             runPayAction(mc, (PayAction)action);
                                          } else if (action instanceof XCarryAction xsa) {
                                             if (xsa.mode == XCarryAction.CarryMode.PUT_IN
                                                && (xsa.transferMode == XCarryAction.TransferMode.CLICK || xsa.transferMode == XCarryAction.TransferMode.SAFE_CLICK)) {
                                                if (xsa.transferMode == XCarryAction.TransferMode.SAFE_CLICK) {
                                                   setRunStatus("XCarry Safe");
                                                   runXCarrySafeAction(mc, xsa);
                                                } else {
                                                   setRunStatus("XCarry Click");
                                                   runXCarryAction(mc, xsa);
                                                }
                                             } else {
                                                String var10000;
                                                switch (xsa.mode) {
                                                   case PUT_IN:
                                                      var10000 = "XCarry";
                                                      break;
                                                   case TAKE_OUT:
                                                      var10000 = "XCarry Out";
                                                      break;
                                                   case DROP:
                                                      var10000 = "XCarry Drop";
                                                      break;
                                                   default:
                                                      throw new MatchException(null, null);
                                                }

                                                setRunStatus(var10000);
                                                runOnClientThread(mc, () -> {
                                                   try {
                                                      xsa.execute(mc);
                                                   } catch (Exception ex) {
                                                      AutismClientMessaging.sendPrefixed("§cXCarry: " + ex.getMessage());
                                                   }
                                                });
                                             }
                                          } else if (action instanceof InventoryAuditAction auditAction
                                             && (auditAction.mode == InventoryAuditAction.AuditMode.DUPE || auditAction.mode == InventoryAuditAction.AuditMode.DUPE_SPAM)) {
                                             setRunStatus("Dupe: " + auditAction.dupeVector.name() + " " + auditAction.openMode.name());

                                             try {
                                                auditAction.runAudit(mc);
                                             } catch (Exception e) {
                                                AutismClientMessaging.sendPrefixed("§cDupe error: " + e.getMessage());
                                             }
                                          } else if (action instanceof ContainerClickSequenceAction sequence) {
                                             runContainerClickSequence(mc, sequence);
                                          } else if (action instanceof PacketBurstAction burst) {
                                             runPacketBurstAction(mc, burst);
                                          } else if (action instanceof BundleDupeV2Action bundleV2) {
                                             setRunStatus("Bundle Dupe V2");
                                             bundleV2.execute(mc);
                                          } else if (action instanceof RollbackAction rollback) {
                                             setRunStatus("Rollback");

                                             try {
                                                rollback.rollback(mc);
                                             } catch (Exception e) {
                                                AutismClientMessaging.sendPrefixed("§cRollback failed: " + describeError(e));
                                             }
                                          } else if (action instanceof PacketGateAction pga) {
                                             runPacketGateAction(mc, pga);
                                          } else if (isScreenOpeningAction(action)) {
                                             postGuiFuture = waitForTargetFuture(mc, action);
                                          } else if (action instanceof MacroVariablesAction || action instanceof AssertAction) {
                                             action.execute(mc);
                                          } else if (action instanceof UseItemPhaseAction phaseAction) {
                                             runUsePhaseAction(mc, phaseAction);
                                          } else if (action instanceof ContextMacroAction contextAction) {
                                             runContextAction(contextAction);
                                          } else if (action instanceof PlaceAction placeInteract && placeInteract.interact) {
                                             runPlaceAction(mc, placeInteract);
                                          } else if (action instanceof PlaceAction place && place.waitForItem) {
                                             MacroItemTarget placeTarget = place.resolveItemTarget().resolveTemplates(mc);
                                             if (placeTarget == null) {
                                                postGuiFuture = null;
                                             } else {
                                                if (placeTarget.hasItemFilter() || placeTarget.hasSlot()) {
                                                   setRunStatus("Waiting for " + describeTarget(placeTarget));

                                                   try {
                                                      waitForItemTarget(mc, placeTarget);
                                                   } catch (CancellationException var227) {
                                                   }
                                                }

                                                if (isStillRunning()) {
                                                   PlaceAction fPlace = place;
                                                   postToClientThread(mc, () -> {
                                                      if (!autismclient.modules.aD.read_Z()) {
                                                         try {
                                                            fPlace.execute(mc);
                                                         } catch (Exception ex) {
                                                            AutismClientMessaging.sendPrefixed("§cError in Place: " + ex.getMessage());
                                                         }
                                                      }
                                                   }, fPlace.h_());
                                                }
                                             }
                                          } else if (isPacketOrdered(action)) {
                                             executeGuarded(mc, action, action.getDisplayName());
                                          } else if (module != null && module.isInstantExecutionMode()) {
                                             CountDownLatch latch = new CountDownLatch(1);
                                             MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
                                             mc.execute(() -> runWithCurrentRun(scheduledRun, () -> {
                                                try {
                                                   if (!autismclient.modules.aD.read_Z()) {
                                                      action.execute(mc);
                                                      return;
                                                   }
                                                } catch (Exception ex) {
                                                   AutismClientMessaging.sendPrefixed("§cError in action: " + ex.getMessage());
                                                   return;
                                                } finally {
                                                   latch.countDown();
                                                }
                                             }));
                                             awaitLatch(latch, 100L);
                                          } else {
                                             executeGuarded(mc, action, action.getDisplayName());
                                          }
                                       } else {
                                          if (action instanceof DropAction dropAction) {
                                             postGuiFuture = postGuiWaitFuture(mc, dropAction);
                                          }

                                          executeGuarded(mc, action, action.getDisplayName());
                                       }
                                    }

                                    storeCaptureOutput(action, mc);
                                    if (postGuiFuture != null && isStillRunning()) {
                                       try {
                                          awaitWhileRunning(postGuiFuture);
                                       } catch (CancellationException var226) {
                                       }
                                    }

                                    if (isStillRunning()) {
                                       markStepCompleted(i + 1);
                                       reportLanProgress(runState, i + 1, macro.actions.size(), macro.name);
                                    }

                                    if (isStillRunning() && !isNonInventoryAction(action) && module != null && module.isInstantExecutionMode()) {
                                       int delayUs = module.getActionDelayUs();
                                       if (delayUs > 0) {
                                          LockSupport.parkNanos(delayUs * 1000L);
                                       } else {
                                          Thread.onSpinWait();
                                       }

                                       throttleMainThreadBurst(mc);
                                    }
                                 }
                              }
                           } finally {
                              MacroAimController.clear();
                              releaseResourceLocks(heldResources);
                           }
                        }
                     }
                  }
               }
            }

            if (macro.actions.isEmpty()) {
               if (!runState.flags.silent()) {
                  AutismClientMessaging.sendPrefixed("§cMacro stopped: no actions left.");
               }

               stopCurrentRunOrAll();
               break;
            }

            if (isStillRunning()) {
               PacketGateManager.closeRun(runState.id, mc == null ? null : mc.getConnection());
            }

            if (macro.loop && loopCount < maxLoops - 1 && isStillRunning()) {
               if (module != null && module.isInstantExecutionMode()) {
                  int delayUs = module.getActionDelayUs();
                  if (delayUs > 0) {
                     LockSupport.parkNanos(delayUs * 1000L);
                  } else {
                     Thread.onSpinWait();
                  }

                  throttleMainThreadBurst(mc);
               } else if (module != null && module.isUseMsSleepMode()) {
                  int sleepMs = module.getMsSleepInterval();
                  sleep(sleepMs);
               } else {
                  try {
                     CompletableFuture<Void> syncFuture = MacroWaitRegistry.awaitTickSync();
                     awaitWhileRunning(syncFuture);
                  } catch (CancellationException var225) {
                  }
               }
            }
         }

         waitForOverrideTimeouts(runState);
         if (isStillRunning()) {
            runState.outcome = "COMPLETED";
            if (!runState.flags.silent()) {
               AutismClientMessaging.sendPrefixed("§aMacro finished: " + macro.name);
            }
         }
      } catch (Exception e) {
         runState.outcome = "FAILED";
         if (!runState.flags.silent()) {
            AutismClientMessaging.sendPrefixed("§cMacro crashed: " + e.getMessage());
         }

         AutismClientAddon.LOG.warn("[Autism] Macro crashed: {}", macro.name, e);
      } finally {
         if (!autismclient.modules.aD.read_Z()) {
            if (!"COMPLETED".equals(runState.outcome)) {
               MacroToolBridge.endSession(runState.id, true);
            }

            runFinallyActions(runState, mc);
         }

         PacketGateManager.closeRun(runState.id, mc == null ? null : mc.getConnection());
         finishCurrentRun();
         releaseSmoothLook(runState);
         if (!ANY_RUN_ACTIVE.get()) {
            MacroWaitRegistry.cancelAll();
         }

         reportLanProgress(runState, -1, 0, "");
      }
   }

   private static String resolvePacketName(String value) {
      return PacketNameResolver.normalizeCached(value);
   }

   private static boolean packetNameMatches(String normalizedExpected, String candidate) {
      if (!normalizedExpected.isEmpty() && candidate != null && !candidate.isEmpty()) {
         String normalizedCandidate = resolvePacketName(candidate);
         if (normalizedCandidate.isEmpty()) {
            return false;
         }

         if (normalizedExpected.equals(normalizedCandidate)) {
            return true;
         }

         if (normalizedCandidate.endsWith("packet")) {
            String strippedCandidate = normalizedCandidate.substring(0, normalizedCandidate.length() - "packet".length());
            if (normalizedExpected.equals(strippedCandidate)) {
               return true;
            }
         }

         return normalizedExpected.endsWith(normalizedCandidate) || normalizedCandidate.endsWith(normalizedExpected);
      } else {
         return false;
      }
   }

   private static boolean packetNameMatchesRaw(String expected, String candidate) {
      return expected != null && !expected.isEmpty() ? packetNameMatches(resolvePacketName(expected), candidate) : false;
   }

   private static MacroExecutor.ServerPacketCacheEntry parsePacketTarget(String target) {
      if (target == null) {
         return EMPTY_PACKET_TARGET;
      }

      if (PACKET_TARGET_CACHE.size() > 256) {
         PACKET_TARGET_CACHE.clear();
      }

      return PACKET_TARGET_CACHE.computeIfAbsent(target, t -> {
         String expectedName = WaitForPacketAction.packetOf(t);
         return new MacroExecutor.ServerPacketCacheEntry(WaitForPacketAction.directionOf(t), expectedName, resolvePacketName(expectedName));
      });
   }

   private static boolean matchesPacketTarget(String target, Packet<?> packet, String direction) {
      if (packet == null) {
         return false;
      }

      MacroExecutor.ServerPacketCacheEntry parsed = parsePacketTarget(target);
      if (!parsed.direction().isEmpty() && !parsed.direction().equalsIgnoreCase(direction)) {
         return false;
      }

      if (parsed.expectedName().isEmpty()) {
         return true;
      }

      String normalizedExpected = parsed.normalizedName();
      if (packetNameMatches(normalizedExpected, PacketNameResolver.directionalName(packet, direction))) {
         return true;
      }

      if (packetNameMatches(normalizedExpected, PacketNameResolver.nameOf(packet))) {
         return true;
      }

      if (packetNameMatches(normalizedExpected, packet.getClass().getSimpleName())) {
         return true;
      }

      String fullName = PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass());
      return packetNameMatches(normalizedExpected, fullName);
   }

   private static void waitForPacketTarget(String target) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         CompletableFuture<Void> future = new CompletableFuture<>();
         String normalizedTarget = WaitForPacketAction.normalizeTarget(target);
         synchronized (run.packetWaitLock) {
            run.packetWaitTarget.set(normalizedTarget);
            run.packetWaitFuture = future;
            run.waitingForPacket.set(true);
         }

         try {
            setRunStatus(normalizedTarget.isEmpty() ? "Waiting for packet: Any" : "Waiting for packet: " + WaitForPacketAction.describe(normalizedTarget));
            awaitWhileRunning(future);
         } finally {
            synchronized (run.packetWaitLock) {
               if (run.packetWaitFuture == future) {
                  run.waitingForPacket.set(false);
                  run.packetWaitTarget.set("");
                  run.packetWaitFuture = null;
               }
            }
         }
      } else {
         CompletableFuture<Void> future = new CompletableFuture<>();
         String normalizedTarget = WaitForPacketAction.normalizeTarget(target);
         synchronized (GLOBAL_PACKET_WAIT_LOCK) {
            GLOBAL_PACKET_WAIT_TARGET.set(normalizedTarget);
            GLOBAL_MATCHED_PACKET.set("");
            globalPacketWaitFuture = future;
            GLOBAL_WAITING_FOR_PACKET.set(true);
         }

         try {
            setRunStatus(normalizedTarget.isEmpty() ? "Waiting for packet: Any" : "Waiting for packet: " + WaitForPacketAction.describe(normalizedTarget));
            awaitWhileRunning(future);
         } finally {
            synchronized (GLOBAL_PACKET_WAIT_LOCK) {
               if (globalPacketWaitFuture == future) {
                  GLOBAL_WAITING_FOR_PACKET.set(false);
                  GLOBAL_PACKET_WAIT_TARGET.set("");
                  globalPacketWaitFuture = null;
               }
            }
         }
      }
   }

   private static void waitForChatMatch(WaitForChatAction wca) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null) {
         clearCaptures(wca);
         CompletableFuture<Void> future = new CompletableFuture<>();
         run.chatWaitFuture = future;
         run.chatWaitAction = wca;
         run.waitingForChat.set(true);

         try {
            if (wca.timeoutMs > 0) {
               future.completeOnTimeout(null, wca.timeoutMs, TimeUnit.MILLISECONDS);
            }

            awaitWhileRunning(future);
            applyStashedCaptures(wca);
         } finally {
            if (run.chatWaitFuture == future) {
               run.waitingForChat.set(false);
               run.chatWaitAction = null;
               run.chatWaitFuture = null;
            }
         }
      } else {
         CompletableFuture<Void> future = new CompletableFuture<>();
         globalChatWaitFuture = future;
         GLOBAL_WAITING_FOR_CHAT.set(true);
         GLOBAL_CHAT_WAIT_PATTERN.set(cleanVisibleChatText(wca.pattern));
         GLOBAL_CHAT_WAIT_PATTERN_JSON.set(wca.patternJson == null ? "" : wca.patternJson);
         GLOBAL_CHAT_WAIT_USE_REGEX.set(wca.useRegex);
         GLOBAL_CHAT_WAIT_FUZZY_PERCENT.set(WaitForChatAction.snapFuzzyPercent(wca.fuzzyPercent));
         GLOBAL_CHAT_WAIT_MATCH.set("");

         try {
            if (wca.timeoutMs > 0) {
               future.completeOnTimeout(null, wca.timeoutMs, TimeUnit.MILLISECONDS);
            }

            awaitWhileRunning(future);
         } finally {
            GLOBAL_WAITING_FOR_CHAT.set(false);
            GLOBAL_CHAT_WAIT_PATTERN.set("");
            GLOBAL_CHAT_WAIT_PATTERN_JSON.set("");
            GLOBAL_CHAT_WAIT_USE_REGEX.set(false);
            GLOBAL_CHAT_WAIT_FUZZY_PERCENT.set(100);
            globalChatWaitFuture = null;
         }
      }
   }

   private static void captureChatValue(CaptureValueAction action) {
      if (action != null) {
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         clearCaptures(action);
         MacroExecutor.PacketWaiter listener = registerIncomingPacketWaiter(
            packet -> {
               MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
               if (capture == null) {
                  return false;
               }

               MacroExecutor.LoggedChatMessage message = new MacroExecutor.LoggedChatMessage(
                  capture.sender() == null ? "" : capture.sender(),
                  capture.message() == null ? "" : capture.message(),
                  capture.displayText() == null ? "" : capture.displayText(),
                  capture.displayComponent(),
                  capture.senderKind(),
                  System.currentTimeMillis()
               );
               CaptureValueAction.CaptureResult preview = action.previewChat(message);
               if (!preview.success()) {
                  return false;
               }

               if (run != null) {
                  run.capturedValues.put(action, preview.variables());
               } else {
                  MacroVariableContext.setAll(preview.variables());
               }

               return true;
            }
         );

         try {
            if (awaitWithTimeout(listener.future, 0L) && run != null) {
               applyStashedCaptures(action);
            }
         } finally {
            listener.cancel();
         }
      }
   }

   static CandidatePoolSelector.SelectionState getCandidatePoolState(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null) {
         synchronized (run.candidatePools) {
            return run.candidatePools.computeIfAbsent(action, a -> new CandidatePoolSelector.SelectionState());
         }
      } else {
         return null;
      }
   }

   private static void clearStaleCompletionCaches() {
      long stoppedAt = lastAllRunsEndedMs;
      if (stoppedAt > 0L && System.currentTimeMillis() - stoppedAt > 3000L) {
         COMPLETION_CACHE.clear();
         PENDING_SUGGESTIONS.clear();
         COMMAND_PROBE_QUEUES.clear();
      }
   }

   static String buildSuggestionQuery(String resolvedCommand, CandidatePoolSelector.FilterMode filter, String filterText) {
      String query = resolvedCommand.startsWith("/") ? resolvedCommand : "/" + resolvedCommand;
      if (!query.contains(" ")) {
         query = query + " ";
      }

      String prefix = filter == CandidatePoolSelector.FilterMode.PREFIX && filterText != null ? filterText.trim() : "";
      if (!prefix.isEmpty() && !prefix.contains(" ") && query.endsWith(" ")) {
         int end = query.length();

         while (end > 0 && query.charAt(end - 1) == ' ') {
            end--;
         }

         query = query.substring(0, end) + " " + prefix;
      }

      return query;
   }

   private static List<String> collectProbedSuggestions(ClientPacketListener connection, CaptureValueAction action, String query, List<String> base) {
      if (!action.autofillCacheList && base.size() < 1000) {
         return base;
      }

      MacroExecutor.CommandProbeQueue union = COMMAND_PROBE_QUEUES.compute(
         query,
         (key, existing) -> (MacroExecutor.CommandProbeQueue)(existing != null && existing.connection == connection
            ? existing
            : new MacroExecutor.CommandProbeQueue(connection, query))
      );
      synchronized (union) {
         union.collected.addAll(base);
         union.largestSuggestionBatch = Math.max(union.largestSuggestionBatch, base.size());
         advanceProbeQueue(union, connection, action.autofillTimeoutMs);
         List<String> pool = new ArrayList<>(union.collected);
         boolean ordered = action.listSelection == CandidatePoolSelector.SelectionMode.SEQUENTIAL
            || action.listSelection == CandidatePoolSelector.SelectionMode.FIRST
            || action.listSelection == CandidatePoolSelector.SelectionMode.LAST
            || action.listSelection == CandidatePoolSelector.SelectionMode.POSITION;
         if (ordered) {
            pool.sort(String.CASE_INSENSITIVE_ORDER);
         }

         return pool;
      }
   }

   private static void advanceProbeQueue(MacroExecutor.CommandProbeQueue union, ClientPacketListener connection, int timeoutMs) {
      if (union.collected.size() >= 60000) {
         union.pendingPrefixes.clear();
      } else {
         while (!union.pendingPrefixes.isEmpty()) {
            if (union.nextCharIndex < "abcdefghijklmnopqrstuvwxyz0123456789_".length()) {
               String target = union.pendingPrefixes.peek() + "abcdefghijklmnopqrstuvwxyz0123456789_".charAt(union.nextCharIndex);
               MacroExecutor.CompletionCacheEntry cached = COMPLETION_CACHE.get(target);
               if (cached == null || cached.connection() != connection) {
                  requestSuggestions(connection, target, timeoutMs);
                  return;
               }

               union.collected.addAll(cached.suggestions());
               int size = cached.suggestions().size();
               boolean truncated = size >= union.largestSuggestionBatch && union.largestSuggestionBatch >= 8;
               union.largestSuggestionBatch = Math.max(union.largestSuggestionBatch, size);
               if (truncated) {
                  union.pendingPrefixes.add(target);
               }

               union.nextCharIndex++;
            } else {
               union.pendingPrefixes.poll();
               union.nextCharIndex = 0;
            }
         }
      }
   }

   private static void storeSuggestionCapture(MacroExecutor.MacroRun run, CaptureValueAction action, List<String> suggestions, String query) {
      CaptureValueAction.CaptureResult preview = action.previewSuggestions(suggestions, query);
      if (preview.success()) {
         if (run != null) {
            run.variables.putAll(preview.variables());
         } else {
            MacroVariableContext.setAll(preview.variables());
         }
      }
   }

   private static MacroExecutor.SuggestionFutureEntry requestSuggestions(ClientPacketListener connection, String query, int timeoutMs) {
      MacroExecutor.SuggestionFutureEntry existing = PENDING_SUGGESTIONS.get(query);
      if (existing != null && existing.connection() == connection && !existing.future().isDone()) {
         return existing;
      }

      if (PENDING_SUGGESTIONS.size() > 256) {
         return existing;
      }

      int requestId = autismclient.util.CommandProbeIdAllocator.next();
      CompletableFuture<List<String>> future = new CompletableFuture<>();
      MacroExecutor.PacketWaiter listener = registerIncomingPacketWaiter(packet -> {
         if (packet instanceof ClientboundCommandSuggestionsPacket reply && reply.id() == requestId) {
            List<String> texts = new ArrayList<>();
            if (reply.suggestions() != null) {
               for (net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry entry : reply.suggestions()) {
                  if (entry != null && entry.text() != null && !entry.text().isBlank()) {
                     texts.add(entry.text());
                  }
               }
            }

            future.complete(texts);
            return true;
         } else {
            return false;
         }
      });
      MacroExecutor.SuggestionFutureEntry pending = new MacroExecutor.SuggestionFutureEntry(future, connection);
      PENDING_SUGGESTIONS.put(query, pending);
      future.completeOnTimeout(null, timeoutMs > 0 ? Math.max(1000, timeoutMs) : 30000L, TimeUnit.MILLISECONDS);
      future.whenComplete((texts, error) -> {
         listener.cancel();
         PENDING_SUGGESTIONS.remove(query, pending);
         if (texts != null) {
            if (COMPLETION_CACHE.size() >= 256) {
               COMPLETION_CACHE.clear();
            }

            COMPLETION_CACHE.put(query, new MacroExecutor.CompletionCacheEntry(List.copyOf((Collection<? extends String>)texts), connection));
         }
      });

      try {
         connection.send(new ServerboundCommandSuggestionPacket(requestId, query));
      } catch (Throwable error) {
         future.complete(null);
      }

      return pending;
   }

   private static void captureCommandSuggestions(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      clearCaptures(action);
      MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(action.autofillCommand, mc);
      if (resolved.success() && !resolved.value().isBlank()) {
         String query = buildSuggestionQuery(resolved.value(), action.listFilter, action.listFilterText);
         ClientPacketListener connection = mc.getConnection();
         if (connection != null) {
            if (action.autofillCacheList) {
               MacroExecutor.CompletionCacheEntry listCache = COMPLETION_CACHE.get(query);
               if (listCache != null && listCache.connection() == connection && !listCache.suggestions().isEmpty()) {
                  setRunStatus("Using cached suggestions");
                  storeSuggestionCapture(run, action, listCache.suggestions(), query);
               } else {
                  setRunStatus("Sweeping command suggestions");
                  List<String> all = probeTabNames(connection, query);
                  if (!all.isEmpty()) {
                     if (COMPLETION_CACHE.size() >= 256) {
                        COMPLETION_CACHE.clear();
                     }

                     COMPLETION_CACHE.put(query, new MacroExecutor.CompletionCacheEntry(List.copyOf(all), connection));
                     storeSuggestionCapture(run, action, all, query);
                  }
               }
            } else {
               MacroExecutor.CompletionCacheEntry cached = COMPLETION_CACHE.get(query);
               if (cached != null && (cached.connection() != connection || cached.suggestions().isEmpty())) {
                  cached = null;
               }

               if (cached != null) {
                  setRunStatus("Using cached suggestions");
                  storeSuggestionCapture(run, action, collectProbedSuggestions(connection, action, query, cached.suggestions()), query);
                  requestSuggestions(connection, query, action.autofillTimeoutMs);
               } else {
                  setRunStatus("Requesting command suggestions");
                  MacroExecutor.SuggestionFutureEntry pending = requestSuggestions(connection, query, action.autofillTimeoutMs);
                  if (pending != null && awaitWithTimeout(pending.future(), 0L)) {
                     List<String> suggestions = pending.future().getNow(null);
                     if (suggestions != null && !suggestions.isEmpty()) {
                        storeSuggestionCapture(run, action, collectProbedSuggestions(connection, action, query, suggestions), query);
                     }
                  }
               }
            }
         }
      }
   }

   private static List<String> probeTabNames(final ClientPacketListener connection, String query) {
      CommandTabNameProber.ProbeControl control = new CommandTabNameProber.ProbeControl() {
         @Override
         public boolean isCancelled() {
            Minecraft mc = Minecraft.getInstance();
            return !MacroExecutor.isStillRunning() || mc == null || mc.getConnection() != connection;
         }

         @Override
         public boolean isPaused() {
            return false;
         }

         @Override
         public int maxNames() {
            return 60000;
         }

         @Override
         public int maxQueries() {
            return 50000;
         }

         @Override
         public boolean usernamesOnly() {
            return false;
         }
      };
      List<String> all = CommandTabNameProber.probe(connection, query, control);
      all.sort(String.CASE_INSENSITIVE_ORDER);
      return all;
   }

   private static void captureScrapedNames(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      clearCaptures(action);
      ClientPacketListener connection = mc.getConnection();
      if (connection != null) {
         String cacheKey = "\u0000namescrape";
         MacroExecutor.CompletionCacheEntry cached = COMPLETION_CACHE.get(cacheKey);
         if (cached != null && cached.connection() == connection && !cached.suggestions().isEmpty()) {
            setRunStatus("Using cached names");
            storeSuggestionCapture(run, action, cached.suggestions(), "");
         } else {
            setRunStatus("Scraping player names");
            List<String> names = scrapePlayerNames(mc, connection);
            if (!names.isEmpty()) {
               if (COMPLETION_CACHE.size() >= 256) {
                  COMPLETION_CACHE.clear();
               }

               COMPLETION_CACHE.put(cacheKey, new MacroExecutor.CompletionCacheEntry(List.copyOf(names), connection));
               storeSuggestionCapture(run, action, names, "");
            }
         }
      }
   }

   private static List<String> scrapePlayerNames(Minecraft mc, final ClientPacketListener connection) {
      String self = mc.player == null ? "" : mc.player.getName().getString();
      LinkedHashMap<String, String> names = CommandTabNameProber.probe(mc, self);
      List<CommandTabNameProber.ProbeVector> vectors = CommandTabNameProber.discoverVectors(connection);
      CommandTabNameProber.ProbeControl control = new CommandTabNameProber.ProbeControl() {
         @Override
         public boolean isCancelled() {
            Minecraft m = Minecraft.getInstance();
            return !MacroExecutor.isStillRunning() || m == null || m.getConnection() != connection;
         }

         @Override
         public boolean isPaused() {
            return false;
         }

         @Override
         public int maxNames() {
            return 60000;
         }

         @Override
         public int maxQueries() {
            return 100000;
         }
      };
      CommandTabNameProber.probe(connection, names, self, vectors, control);
      List<String> out = new ArrayList<>(names.values());
      out.sort(String.CASE_INSENSITIVE_ORDER);
      return out;
   }

   private static void executeCapture(Minecraft mc, CaptureValueAction action) {
      if (action != null) {
         String modifierError = action.numberModifierError();
         if (!modifierError.isBlank()) {
            setRunStatus("Capture failed: " + modifierError);
            bi.write("Capture Value: " + modifierError);
         } else if (action.source == CaptureValueAction.CaptureSource.COMMAND_AUTOFILL) {
            setRunStatus("Requesting command suggestions");
            captureCommandSuggestions(mc, action);
            reportEmptyCapture(action);
         } else if (action.source == CaptureValueAction.CaptureSource.NAMESCRAPE) {
            setRunStatus("Scraping player names");
            captureScrapedNames(mc, action);
            reportEmptyCapture(action);
         } else if (action.waitForTrigger) {
            CaptureValueAction.CaptureSource source = action.source == null ? CaptureValueAction.CaptureSource.GUI_TITLE : action.source;
            setRunStatus("Waiting for capture: " + source.name().toLowerCase(Locale.ROOT).replace('_', ' '));
            if (source == CaptureValueAction.CaptureSource.RECENT_CHAT) {
               captureChatValue(action);
            } else if (source == CaptureValueAction.CaptureSource.SCOREBOARD) {
               captureScoreboardValue(mc, action);
            } else {
               capturePreviewValue(mc, action);
            }
         } else {
            MacroExecutor.MacroRun run = CURRENT_RUN.get();
            AtomicReference<CaptureValueAction.CaptureResult> result = new AtomicReference<>(CaptureValueAction.CaptureResult.failure("Not available"));
            long deadline = System.nanoTime() + 300000000L;

            while (isStillRunning()) {
               CompletableFuture<Void> done = new CompletableFuture<>();
               mc.execute(() -> runWithCurrentRun(run, () -> {
                  try {
                     result.set(action.capture(mc));
                     done.complete(null);
                  } catch (Throwable error) {
                     done.completeExceptionally(error);
                  }
               }));
               awaitWithTimeout(done, 0L);
               if (result.get().success() || System.nanoTime() >= deadline) {
                  break;
               }

               awaitNextTick();
            }

            CaptureValueAction.CaptureResult preview = result.get();
            if (!preview.success()) {
               setRunStatus("Capture failed: " + preview.status());
               bi.write("Capture " + captureLabel(action) + ": " + preview.status());
            }
         }
      }
   }

   private static void reportEmptyCapture(CaptureValueAction action) {
      String name = MacroVariableStore.rootKey(action.saveAs);
      if (!name.isBlank() && !MacroVariableContext.get(name).isPresent()) {
         setRunStatus("Capture failed: nothing captured");
         bi.write("Capture " + captureLabel(action) + ": nothing captured");
      }
   }

   private static String captureLabel(CaptureValueAction action) {
      String name = MacroVariableStore.rootKey(action.saveAs);
      return name.isBlank() ? "value" : "{" + name + "}";
   }

   private static void capturePreviewValue(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      clearCaptures(action);
      String lastMessage = "";

      while (isStillRunning()) {
         CaptureValueAction.CaptureResult preview = callOnClientThread(
            mc, () -> action.previewCurrent(mc), CaptureValueAction.CaptureResult.failure("Not available")
         );
         if (preview.success()) {
            if (run != null) {
               run.variables.putAll(preview.variables());
            } else {
               MacroVariableContext.setAll(preview.variables());
            }

            return;
         }

         if (!preview.status().isBlank() && !preview.status().equals(lastMessage)) {
            lastMessage = preview.status();
            setRunStatus("Capture waiting: " + lastMessage);
         }

         awaitNextTick();
      }
   }

   private static void captureScoreboardValue(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      clearCaptures(action);
      String trackedKey = action.scoreboardRow;
      int trackedRow = action.scoreboardRowIndex;
      String trackedObjective = action.scoreboardObjective;
      String previousText = null;
      String lastMessage = "";
      boolean baselineReady = false;

      while (isStillRunning()) {
         final String keySnapshot0 = trackedKey;
         final int rowSnapshot = trackedRow;
         final String objectiveSnapshot = trackedObjective;
         CaptureValueAction.SidebarLine line = callOnClientThread(mc, () -> action.sidebarLine(mc, keySnapshot0, rowSnapshot, objectiveSnapshot), null);
         if (line != null) {
            String keySnapshot = line.text() == null ? "" : line.text();
            if (!baselineReady) {
               trackedKey = line.key();
               trackedRow = line.row();
               trackedObjective = line.objective();
               previousText = keySnapshot;
               baselineReady = true;
            } else if (!keySnapshot.equals(previousText)) {
               CaptureValueAction.CaptureResult preview = action.previewSidebarLine(line);
               if (preview.success()) {
                  if (run != null) {
                     run.variables.putAll(preview.variables());
                  } else {
                     MacroVariableContext.setAll(preview.variables());
                  }

                  return;
               }

               if (!preview.status().isBlank() && !preview.status().equals(lastMessage)) {
                  lastMessage = preview.status();
                  setRunStatus("Capture waiting: " + lastMessage);
               }

               previousText = keySnapshot;
            }
         }

         awaitNextTick();
      }
   }

   private static void awaitWhileRunning(CompletableFuture<Void> future) {
      Thread t = Thread.currentThread();
      future.whenComplete((v, e) -> LockSupport.unpark(t));

      while (!future.isDone() && isStillRunning()) {
         LockSupport.parkNanos(500000L);
         if (Thread.interrupted()) {
            break;
         }
      }

      if (!isStillRunning()) {
         future.cancel(true);
      }
   }

   private static void runPacedTpAction(Minecraft mc, PacedTpAction action) {
      AtomicReference<PacedTpJobManager.JobHandle> handleRef = new AtomicReference<>();
      runOnClientThread(mc, () -> handleRef.set(PacedTpJobManager.init(action, "macro")));
      PacedTpJobManager.JobHandle handle = handleRef.get();
      if (handle != null) {
         setRunStatus("TP " + action.init());

         try {
            while (isStillRunning() && PacedTpJobManager.status(handle.id()).state() == PacedTpJobManager.JobState.PENDING) {
               awaitNextTick();
            }
         } finally {
            if (PacedTpJobManager.status(handle.id()).state() == PacedTpJobManager.JobState.PENDING) {
               mc.execute(() -> PacedTpJobManager.cancel(handle.id(), "macro stopped"));
            }
         }
      }
   }

   private static void throttleMainThreadBurst(Minecraft mc) {
      if (mc != null && !mc.isSameThread()) {
         int[] burst = MAIN_THREAD_BURST_COUNT.get();
         if (++burst[0] >= 256) {
            burst[0] = 0;
            CompletableFuture<Void> prev = MAIN_THREAD_BARRIER.get();
            if (prev != null && !prev.isDone()) {
               awaitWhileRunning(prev);
            }

            if (isStillRunning()) {
               CompletableFuture<Void> next = new CompletableFuture<>();
               MAIN_THREAD_BARRIER.set(next);

               try {
                  mc.execute(() -> next.complete(null));
               } catch (Throwable ignored) {
                  next.complete(null);
               }
            }
         }
      }
   }

   private static void waitUntilOrTimeout(BooleanSupplier predicate, int timeoutMs) {
      long start = System.currentTimeMillis();

      while (isStillRunning()) {
         try {
            if (predicate.getAsBoolean()) {
               return;
            }
         } catch (Throwable var5) {
         }

         if (timeoutMs > 0 && System.currentTimeMillis() - start >= timeoutMs) {
            return;
         }

         CompletableFuture<Void> tick = MacroWaitRegistry.awaitTickSync();
         awaitWhileRunning(tick);
      }
   }

   private static BooleanSupplier guiWaitCondition(WaitForGuiAction action, Minecraft mc) {
      AtomicBoolean sawMatchingGui = new AtomicBoolean(action.matchesCurrent(mc));
      return () -> {
         Optional<Map<String, MacroVarValue>> capture = mc == null ? Optional.empty() : action.capture(mc.gui.screen());
         boolean matches = capture.isPresent();
         if (matches) {
            capture.ifPresent(values -> stashCaptures(action, (Map<String, MacroVarValue>)values));
            sawMatchingGui.set(true);
            return false;
         } else {
            return sawMatchingGui.get();
         }
      };
   }

   private static BooleanSupplier guiTypeWaitCondition(WaitGuiTypeAction action, Minecraft mc) {
      AtomicBoolean sawMatchingGui = new AtomicBoolean(action.matches(mc));
      return () -> {
         Optional<Map<String, MacroVarValue>> capture = action.capture(mc);
         boolean matches = capture.isPresent();
         if (matches) {
            capture.ifPresent(values -> stashCaptures(action, (Map<String, MacroVarValue>)values));
            sawMatchingGui.set(true);
            return false;
         } else {
            return sawMatchingGui.get();
         }
      };
   }

   private static void waitForDurability(Minecraft mc, WaitDurabilityAction action) {
      AtomicReference<MacroExecutor.CaptureSessionState> sessionRef = new AtomicReference<>();
      long start = System.currentTimeMillis();

      while (isStillRunning()) {
         if (Boolean.TRUE.equals(callOnClientThread(mc, () -> pollDurability(mc, action, sessionRef), Boolean.FALSE))) {
            return;
         }

         if (action.timeoutMs > 0 && System.currentTimeMillis() - start >= action.timeoutMs) {
            return;
         }

         awaitNextTick();
      }
   }

   private static boolean checkDurabilityOnce(Minecraft mc, WaitDurabilityAction action) {
      if (action != null && action.useNext) {
         MacroExecutor.CaptureSessionState session = startDurabilitySession(mc, action);
         if (session == null || session.completed) {
            return action.isSatisfied(mc);
         }

         if (session.slotLocked) {
            ItemStack stack = stackInVisibleSlot(mc, session.slot);
            if (session.lockedItemId.isBlank() && matchesItemTarget(action, session, stack)) {
               session.lockedItemId = registryIdOf(stack);
            }

            return session.lockedItemId.isBlank()
               ? false
               : isDamageableItemOf(stack, session.lockedItemId)
                  && action.matches(stack)
                  && findReplacement(mc, action, session, session.slot) == null;
         } else {
            return session.lockedItemId.isBlank() ? action.isSatisfied(mc) : findReplacement(mc, action, session, -1) == null;
         }
      } else {
         return action != null && action.isSatisfied(mc);
      }
   }

   private static boolean pollDurability(Minecraft mc, WaitDurabilityAction action, AtomicReference<MacroExecutor.CaptureSessionState> sessionRef) {
      if (mc != null && mc.player != null && action != null) {
         MacroExecutor.CaptureSessionState session = sessionRef.get();
         if (session == null) {
            session = startDurabilitySession(mc, action);
            sessionRef.set(session);
         }

         if (session == null || session.completed) {
            return action.isSatisfied(mc);
         }

         if (session.slotLocked) {
            return pollLockedSlot(mc, action, session);
         }

         followHeldItem(mc, session);
         ItemStack active = stackInVisibleSlot(mc, session.slot);
         return isDamageableItemOf(active, session.lockedItemId) && !action.matches(active) ? false : trySwapReplacement(mc, action, session, session.slot);
      } else {
         return false;
      }
   }

   private static boolean pollLockedSlot(Minecraft mc, WaitDurabilityAction action, MacroExecutor.CaptureSessionState session) {
      ItemStack stack = stackInVisibleSlot(mc, session.slot);
      if (session.lockedItemId.isBlank() && matchesItemTarget(action, session, stack)) {
         session.lockedItemId = registryIdOf(stack);
      }

      if (!session.lockedItemId.isBlank() && isDamageableItemOf(stack, session.lockedItemId)) {
         session.itemSeen = true;
         if (!action.matches(stack)) {
            return false;
         }

         session.slotLocked = false;
         return trySwapReplacement(mc, action, session, session.slot);
      } else if (session.itemSeen) {
         session.slotLocked = false;
         return trySwapReplacement(mc, action, session, session.slot);
      } else {
         return false;
      }
   }

   private static boolean trySwapReplacement(Minecraft mc, WaitDurabilityAction action, MacroExecutor.CaptureSessionState session, int excludeSlot) {
      MacroExecutor.SlotRangePair replacement = findReplacement(mc, action, session, excludeSlot);
      if (replacement == null) {
         return true;
      } else {
         return !equipReplacement(mc, session, replacement) ? false : false;
      }
   }

   private static MacroExecutor.CaptureSessionState startDurabilitySession(Minecraft mc, WaitDurabilityAction action) {
      if (mc != null && mc.player != null && action != null) {
         MacroExecutor.CaptureSessionState session = new MacroExecutor.CaptureSessionState();
         session.originalHotbarSlot = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         MacroItemTarget target = action.targetMode == WaitDurabilityAction.TargetMode.ITEM ? MacroItemTarget.parse(action.itemName) : new MacroItemTarget();
         session.itemTarget = target;
         switch (action.targetMode) {
            case SLOT:
               session.slot = Math.max(0, Math.min(40, action.slot));
               session.slotLocked = true;
               ItemStack stack = stackInVisibleSlot(mc, session.slot);
               if (matchesItemTarget(action, session, stack)) {
                  session.lockedItemId = registryIdOf(stack);
                  session.itemSeen = true;
               }
               break;
            case HELD:
               lockHeldItem(mc, action, session);
               break;
            case ITEM:
               lockTargetItem(mc, action, session, target);
         }

         if (session.lockedItemId == null) {
            session.lockedItemId = "";
         }

         if (session.lockedItemId.isBlank() && !session.slotLocked) {
            session.completed = true;
         }

         return session;
      } else {
         return null;
      }
   }

   private static void lockHeldItem(Minecraft mc, WaitDurabilityAction action, MacroExecutor.CaptureSessionState session) {
      int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
      ItemStack main = mc.player.getInventory().getItem(selected);
      if (main != null && !main.isEmpty() && main.isDamageableItem()) {
         session.slot = selected;
         session.lockedItemId = registryIdOf(main);
      } else {
         ItemStack offhand = mc.player.getOffhandItem();
         if (offhand != null && !offhand.isEmpty() && offhand.isDamageableItem()) {
            session.slot = 40;
            session.lockedItemId = registryIdOf(offhand);
         } else {
            session.completed = true;
         }
      }
   }

   private static void lockTargetItem(Minecraft mc, WaitDurabilityAction action, MacroExecutor.CaptureSessionState session, MacroItemTarget target) {
      if (target != null && target.hasSlot()) {
         session.slot = Math.max(0, Math.min(40, target.slot));
         session.slotLocked = true;
         String explicitId = target.hasRegistryId() ? normalizeItemId(target.registryId) : "";
         if (!explicitId.isBlank()) {
            session.lockedItemId = explicitId;
         }

         ItemStack stack = stackInVisibleSlot(mc, session.slot);
         if (matchesItemTarget(action, session, stack)) {
            session.lockedItemId = registryIdOf(stack);
            session.itemSeen = true;
         }
      } else if (target != null && target.hasRegistryId()) {
         session.lockedItemId = normalizeItemId(target.registryId);
         session.slot = findTargetSlot(mc, action, session.lockedItemId, target);
      } else if (target != null && target.hasItemFilter()) {
         int slot = findDurabilitySlot(mc, action, "", target);
         if (slot >= 0) {
            session.slot = slot;
            session.lockedItemId = registryIdOf(stackInVisibleSlot(mc, slot));
         } else {
            session.completed = true;
         }
      } else {
         lockHeldItem(mc, action, session);
      }
   }

   private static int findTargetSlot(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target) {
      if (mc != null && mc.player != null) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (slotHoldsTarget(mc, action, lockedRegistryId, target, selected)) {
            return selected;
         } else {
            return slotHoldsTarget(mc, action, lockedRegistryId, target, 40) ? 40 : -1;
         }
      } else {
         return -1;
      }
   }

   private static boolean matchesItemTarget(WaitDurabilityAction action, MacroExecutor.CaptureSessionState session, ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
         return action != null
               && action.targetMode == WaitDurabilityAction.TargetMode.ITEM
               && session != null
               && session.itemTarget != null
               && session.itemTarget.hasItemFilter()
            ? session.itemTarget.matchScore(stack, session.slot) >= 0
            : true;
      } else {
         return false;
      }
   }

   private static int findDurabilitySlot(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target) {
      if (mc != null && mc.player != null) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (slotHoldsTarget(mc, action, lockedRegistryId, target, selected)) {
            return selected;
         }

         for (int slot = 0; slot < 9; slot++) {
            if (slot != selected && slotHoldsTarget(mc, action, lockedRegistryId, target, slot)) {
               return slot;
            }
         }

         for (int slot = 9; slot < Math.min(36, mc.player.getInventory().getContainerSize()); slot++) {
            if (slotHoldsTarget(mc, action, lockedRegistryId, target, slot)) {
               return slot;
            }
         }

         return slotHoldsTarget(mc, action, lockedRegistryId, target, 40) ? 40 : -1;
      } else {
         return -1;
      }
   }

   private static boolean slotHoldsTarget(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target, int slot) {
      ItemStack stack = stackInVisibleSlot(mc, slot);
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
         return lockedRegistryId != null && !lockedRegistryId.isBlank() && !isDamageableItemOf(stack, lockedRegistryId)
            ? false
            : target == null || !target.hasItemFilter() || target.matchScore(stack, slot) >= 0;
      } else {
         return false;
      }
   }

   private static void followHeldItem(Minecraft mc, MacroExecutor.CaptureSessionState session) {
      if (mc != null && mc.player != null && session != null && !session.lockedItemId.isBlank()) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (isDamageableItemOf(mc.player.getInventory().getItem(selected), session.lockedItemId)) {
            session.slot = selected;
            session.originalHotbarSlot = selected;
         } else if (isDamageableItemOf(mc.player.getOffhandItem(), session.lockedItemId)) {
            ItemStack active = stackInVisibleSlot(mc, session.slot);
            if (!isDamageableItemOf(active, session.lockedItemId)) {
               session.slot = 40;
            }
         }
      }
   }

   private static MacroExecutor.SlotRangePair findReplacement(
      Minecraft mc, WaitDurabilityAction action, MacroExecutor.CaptureSessionState session, int excludeSlot
   ) {
      if (mc != null && mc.player != null && action != null && session != null && !session.lockedItemId.isBlank()) {
         MacroExecutor.SlotRangePair hotbar = null;
         MacroExecutor.SlotRangePair inventory = null;
         MacroExecutor.SlotRangePair offhand = null;

         for (int slot = 0; slot < 9; slot++) {
            MacroExecutor.SlotRangePair candidate = replacementAt(mc, action, session.lockedItemId, slot, excludeSlot);
            if (candidate != null && (hotbar == null || candidate.remainingDurability() > hotbar.remainingDurability())) {
               hotbar = candidate;
            }
         }

         int size = Math.min(36, mc.player.getInventory().getContainerSize());

         for (int slot = 9; slot < size; slot++) {
            MacroExecutor.SlotRangePair candidate = replacementAt(mc, action, session.lockedItemId, slot, excludeSlot);
            if (candidate != null && (inventory == null || candidate.remainingDurability() > inventory.remainingDurability())) {
               inventory = candidate;
            }
         }

         offhand = replacementAt(mc, action, session.lockedItemId, 40, excludeSlot);
         if (hotbar != null) {
            return hotbar;
         } else {
            return inventory != null ? inventory : offhand;
         }
      } else {
         return null;
      }
   }

   private static MacroExecutor.SlotRangePair replacementAt(Minecraft mc, WaitDurabilityAction action, String registryId, int slot, int excludeSlot) {
      if (slot == excludeSlot) {
         return null;
      } else {
         ItemStack stack = stackInVisibleSlot(mc, slot);
         if (!isDamageableItemOf(stack, registryId)) {
            return null;
         } else {
            return action.matches(stack) ? null : new MacroExecutor.SlotRangePair(slot, remainingDurability(stack));
         }
      }
   }

   private static boolean equipReplacement(Minecraft mc, MacroExecutor.CaptureSessionState session, MacroExecutor.SlotRangePair replacement) {
      if (mc != null && mc.player != null && replacement != null) {
         int slot = replacement.slot();
         if (slot >= 0 && slot < 9) {
            autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
            session.slot = slot;
            session.originalHotbarSlot = slot;
            return true;
         }

         if (slot >= 9 && slot < 36) {
            int target = Math.max(0, Math.min(8, session.originalHotbarSlot));
            if (!autismclient.util.InventorySlotHelper.swapPlayerSlots(mc, slot, target)) {
               return false;
            }

            autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, target);
            session.slot = target;
            session.originalHotbarSlot = target;
            return true;
         } else if (slot == 40) {
            session.slot = 40;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static ItemStack stackInVisibleSlot(Minecraft mc, int visibleSlot) {
      if (mc == null || mc.player == null) {
         return ItemStack.EMPTY;
      } else if (visibleSlot == 40) {
         return mc.player.getOffhandItem();
      } else {
         return visibleSlot >= 0 && visibleSlot < mc.player.getInventory().getContainerSize() ? mc.player.getInventory().getItem(visibleSlot) : ItemStack.EMPTY;
      }
   }

   private static boolean isDamageableItemOf(ItemStack stack, String registryId) {
      return stack != null && !stack.isEmpty() && stack.isDamageableItem() && registryIdOf(stack).equals(registryId);
   }

   private static int remainingDurability(ItemStack stack) {
      return stack != null && stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 0;
   }

   private static String registryIdOf(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return id == null ? "" : id.toString();
      } else {
         return "";
      }
   }

   private static String normalizeItemId(String raw) {
      if (raw != null && !raw.isBlank()) {
         String trimmed = raw.trim().toLowerCase(Locale.ROOT);
         Identifier parsed = Identifier.tryParse(trimmed.contains(":") ? trimmed : "minecraft:" + trimmed);
         return parsed == null ? "" : BuiltInRegistries.ITEM.getOptional(parsed).map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).orElse("");
      } else {
         return "";
      }
   }

   private static CompletableFuture<Void> pollUntilAsync(BooleanSupplier predicate, int timeoutMs) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      Thread worker = new Thread(() -> runWithCurrentRun(run, () -> {
         long start = System.currentTimeMillis();

         while (!future.isDone() && isStillRunning()) {
            try {
               if (predicate.getAsBoolean()) {
                  future.complete(null);
                  return;
               }
            } catch (Throwable var6) {
            }

            if (timeoutMs > 0 && System.currentTimeMillis() - start >= timeoutMs) {
               future.complete(null);
               return;
            }

            LockSupport.parkNanos(500000L);
            if (Thread.interrupted()) {
               return;
            }
         }
      }), "MacroPredicateWait");
      worker.setDaemon(true);
      worker.start();
      future.whenComplete((v, e) -> worker.interrupt());
      return future;
   }

   private static void armNextListeningAction(Minecraft mc, List<MacroAction> actions, int currentIndex) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && actions != null) {
         int nextIndex = currentIndex + 1;
         if (nextIndex >= 0 && nextIndex < actions.size()) {
            MacroAction next = actions.get(nextIndex);
            if (next != null
               && next.isEnabled()
               && next.listensDuringPreviousAction()
               && DynamicBindingManager.apply(next, mc)
               && canResolveTemplates(next, mc)
               && isPrearmableWait(next)) {
               synchronized (run.asyncWaits) {
                  if (!run.asyncWaits.containsKey(next)) {
                     clearCaptures(next);
                     resetCaptureOutputs(next);
                     MacroExecutor.AsyncActionHandle wait = armAsyncWait(mc, next);
                     if (wait != null) {
                        run.asyncWaits.put(next, wait);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isPrearmableWait(MacroAction action) {
      return action instanceof WaitForPacketAction
         || action instanceof WaitPacketMatchAction
         || action instanceof WaitForChatAction
         || action instanceof WaitForGuiAction
         || action instanceof WaitGuiTypeAction
         || action instanceof WaitForSoundAction
         || action instanceof WaitForSlotChangeAction
         || action instanceof WaitInventoryPredicateAction
         || action instanceof WaitDurabilityAction
         || action instanceof WaitFreeSlotsAction
         || action instanceof WaitForHealthAction
         || action instanceof WaitForBlockAction
         || action instanceof WaitForEntityAction
         || action instanceof WaitEntityTargetAction
         || action instanceof WaitForCooldownAction
         || action instanceof WaitPosAction
         || action instanceof WaitForPositionDeltaAction
         || action instanceof WaitForTeleportAction
         || action instanceof WaitGamemodeChangeAction
         || action instanceof WaitForWorldChangeAction
         || action instanceof WaitMovementAction
         || action instanceof WaitForLanStepAction
         || action instanceof WaitForMacroStepAction;
   }

   private static MacroExecutor.AsyncActionHandle takeAsyncWait(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null) {
         synchronized (run.asyncWaits) {
            return run.asyncWaits.remove(action);
         }
      } else {
         return null;
      }
   }

   private static boolean awaitPrearmedWait(MacroAction action, long timeoutMs) {
      MacroExecutor.AsyncActionHandle wait = takeAsyncWait(action);
      if (wait == null) {
         return false;
      }

      try {
         awaitWithTimeout(wait.future, timeoutMs);
         applyStashedCaptures(action);
         return true;
      } finally {
         wait.runCleanup();
      }
   }

   private static MacroExecutor.AsyncActionHandle armAsyncWait(Minecraft mc, MacroAction action) {
      if (action instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.targets();
         String firstTarget = targets.isEmpty() ? "" : targets.get(0);
         return armPacketWait(packetAction, firstTarget, 1);
      } else if (action instanceof WaitPacketMatchAction packetMatch) {
         return armPacketMatchWait(packetMatch);
      } else if (action instanceof WaitInventoryPredicateAction inv) {
         inv.resetBaseline();
         return armPollingWait(action, () -> inv.isSatisfied(mc), inv.timeoutMs);
      } else if (action instanceof WaitDurabilityAction durability) {
         return durability.useNext ? null : armPollingWait(action, () -> checkDurabilityOnce(mc, durability), durability.timeoutMs);
      } else if (action instanceof WaitFreeSlotsAction freeSlots) {
         return armPollingWait(action, () -> freeSlots.test(mc), freeSlots.timeoutMs);
      } else if (action instanceof WaitEntityTargetAction entityTarget) {
         return armPollingWait(action, () -> entityTarget.isSatisfied(mc), entityTarget.timeoutMs);
      } else if (action instanceof WaitGuiTypeAction guiType) {
         return armPollingWait(
            action, guiType.waitMode == WaitGuiTypeAction.WaitMode.CLOSE ? guiTypeWaitCondition(guiType, mc) : () -> {
               Optional<Map<String, MacroVarValue>> capture = guiType.capture(mc);
               capture.ifPresent(values -> stashCaptures(guiType, (Map<String, MacroVarValue>)values));
               return capture.isPresent();
            }, guiType.timeoutMs
         );
      } else if (action instanceof WaitForChatAction chat) {
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         MacroExecutor.PacketWaiter listener = registerIncomingPacketWaiter(packet -> {
            MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
            Optional<Map<String, MacroVarValue>> matched = matchChatCapture(chat, capture, run == null ? null : run.variables);
            if (matched.isEmpty()) {
               return false;
            }

            if (run != null) {
               run.capturedValues.put(chat, matched.get());
            }

            return true;
         });
         CompletableFuture<?> future = listener.future;
         if (chat.timeoutMs > 0) {
            future.completeOnTimeout(null, chat.timeoutMs, TimeUnit.MILLISECONDS);
         }

         return new MacroExecutor.AsyncActionHandle(action, future, listener::cancel, 0);
      } else if (action instanceof WaitForMacroStepAction macroStep) {
         return armPollingWait(action, () -> isMacroStepReached(macroStep), macroStep.timeoutMs);
      } else if (action instanceof WaitForLanStepAction lanStep) {
         CompletableFuture<Void> future = MacroWaitRegistry.waitForLanStep(lanStep);
         return new MacroExecutor.AsyncActionHandle(action, future, () -> {
            if (!future.isDone()) {
               future.cancel(true);
            }
         }, 0);
      } else {
         CompletableFuture<?> future = triggerWaitFuture(mc, action);
         if (future == null) {
            return null;
         }

         int timeout = action instanceof WaitForChatAction chat ? chat.timeoutMs : 0;
         if (timeout > 0) {
            future.completeOnTimeout(null, timeout, TimeUnit.MILLISECONDS);
         }

         return new MacroExecutor.AsyncActionHandle(action, future, () -> {
            if (!future.isDone()) {
               future.cancel(true);
            }
         }, 0);
      }
   }

   private static boolean hasAsyncWait(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null) {
         synchronized (run.asyncWaits) {
            return run.asyncWaits.containsKey(action);
         }
      } else {
         return false;
      }
   }

   private static void storeMatchedPacket(MacroAction action, Packet<?> packet, String direction) {
      if (action instanceof WaitForPacketAction wait) {
         wait.matchedPacket = packet;
         wait.matchedDirection = direction == null ? "" : direction;
      } else if (action instanceof WaitPacketMatchAction wait) {
         wait.matchedPacket = packet;
         wait.matchedDirection = direction == null ? "" : direction;
      }
   }

   private static void applyMatchedPacket(WaitForPacketAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (action != null && run != null && run.matchedPacket != null) {
         action.matchedPacket = run.matchedPacket;
         action.matchedDirection = run.matchedPacketDirection;
      }
   }

   private static void clearCaptures(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null) {
         run.capturedValues.remove(action);
         if (action instanceof WaitForChatAction chat) {
            for (String name : PatternCaptureMatcher.captureNames(chat.effectiveMatchMode(), chat.pattern)) {
               run.variables.remove(name);
            }

            if (chat.saveAs != null && !chat.saveAs.isBlank()) {
               run.variables.remove(chat.saveAs);
            }
         } else if (action instanceof WaitForGuiAction gui) {
            for (String name : PatternCaptureMatcher.captureNames(gui.matchMode, gui.guiTitle)) {
               run.variables.remove(name);
            }

            if (gui.saveAs != null && !gui.saveAs.isBlank()) {
               run.variables.remove(gui.saveAs);
            }
         } else if (action instanceof WaitGuiTypeAction gui) {
            for (String name : PatternCaptureMatcher.captureNames(gui.matchMode, gui.title)) {
               run.variables.remove(name);
            }

            if (gui.saveAs != null && !gui.saveAs.isBlank()) {
               run.variables.remove(gui.saveAs);
            }
         } else if (action instanceof CaptureValueAction capture) {
            for (String name : PatternCaptureMatcher.captureNames(capture.matchMode, capture.pattern)) {
               run.variables.remove(name);
            }

            if (capture.saveAs != null && !capture.saveAs.isBlank()) {
               run.variables.remove(capture.saveAs);
            }
         }
      }
   }

   private static void resetCaptureOutputs(MacroAction action) {
      if (action instanceof CaptureOutputAction output) {
         String name = output.getSaveAs();
         if (name != null && !name.isBlank()) {
            MacroVariableContext.remove(name);
         }

         if (action instanceof WaitForSoundAction sound) {
            sound.matchedSoundId = "";
         }

         if (action instanceof WaitForPacketAction packet) {
            packet.matchedPacket = null;
            packet.matchedDirection = "";
         }

         if (action instanceof WaitPacketMatchAction packet) {
            packet.matchedPacket = null;
            packet.matchedDirection = "";
         }
      }
   }

   private static void storeCaptureOutput(MacroAction action, Minecraft mc) {
      if (action instanceof CaptureOutputAction output) {
         String name = MacroVariableStore.rootKey(output.getSaveAs());
         if (!name.isEmpty()) {
            MacroVarValue value = callOnClientThread(mc, () -> captureValueOf(action, mc), null);
            if (value != null) {
               MacroVariableContext.setValue(name, value);
            }
         }
      }
   }

   private static MacroVarValue captureValueOf(MacroAction action, Minecraft mc) {
      if (action instanceof WaitForHealthAction healthAction && mc != null && mc.player != null) {
         float health = mc.player.getHealth();
         return (healthAction.below ? health >= healthAction.healthThreshold : health <= healthAction.healthThreshold)
            ? null
            : MacroVarValue.of(
               MacroVarValue.ValueType.NUMBER,
               Float.toString(health),
               Map.of(
                  "health",
                  MacroVarValue.ofNumber(health),
                  "max",
                  MacroVarValue.ofNumber(mc.player.getMaxHealth()),
                  "absorption",
                  MacroVarValue.ofNumber(mc.player.getAbsorptionAmount())
               )
            );
      } else if (action instanceof WaitForEntityAction entityAction) {
         Entity entity = entityAction.findEntity(mc);
         if (entity == null) {
            return null;
         }

         String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
         double distance = mc != null && mc.player != null ? mc.player.distanceTo(entity) : 0.0;
         return MacroVarValue.of(
            MacroVarValue.ValueType.ENTITY,
            entity.getName().getString(),
            Map.of(
               "name",
               MacroVarValue.ofText(entity.getName().getString()),
               "id",
               MacroVarValue.ofIdentifier(id),
               "uuid",
               MacroVarValue.ofText(entity.getStringUUID()),
               "x",
               MacroVarValue.ofNumber(entity.getX()),
               "y",
               MacroVarValue.ofNumber(entity.getY()),
               "z",
               MacroVarValue.ofNumber(entity.getZ()),
               "distance",
               MacroVarValue.ofNumber(distance)
            )
         );
      } else if (action instanceof WaitForSoundAction sound) {
         return sound.matchedSoundId != null && !sound.matchedSoundId.isBlank()
            ? MacroVarValue.of(
               MacroVarValue.ValueType.IDENTIFIER,
               sound.matchedSoundId,
               Map.of(
                  "id",
                  MacroVarValue.ofIdentifier(sound.matchedSoundId),
                  "x",
                  MacroVarValue.ofNumber(sound.matchedX),
                  "y",
                  MacroVarValue.ofNumber(sound.matchedY),
                  "z",
                  MacroVarValue.ofNumber(sound.matchedZ)
               )
            )
            : null;
      } else if (action instanceof WaitForPacketAction packet) {
         return packetValue(packet.matchedPacket, packet.matchedDirection);
      } else if (action instanceof WaitPacketMatchAction packet) {
         return packetValue(packet.matchedPacket, packet.matchedDirection);
      } else if (action instanceof WaitInventoryPredicateAction inventory) {
         return !inventory.isSatisfied(mc) ? null : inventoryItemValue(mc, inventory.itemName, inventory.slot, inventory.condition.name());
      } else if (action instanceof WaitForSlotChangeAction slotWait) {
         if (slotWait.entries.isEmpty()) {
            return inventoryItemValue(mc, "", -1, "slot_change");
         }

         MacroItemTarget target = slotWait.entries.get(0).resolveItemTarget().resolveTemplates(mc);
         return target == null ? null : findItemValue(mc, target);
      } else if (action instanceof WaitPosAction || action instanceof WaitForTeleportAction || action instanceof WaitForPositionDeltaAction) {
         return positionValue(mc);
      } else if (action instanceof WaitForWorldChangeAction && mc != null && mc.level != null) {
         String dimension = mc.level.dimension().identifier().toString();
         return MacroVarValue.of(MacroVarValue.ValueType.IDENTIFIER, dimension, Map.of("id", MacroVarValue.ofIdentifier(dimension), "name", MacroVarValue.ofText(dimension)));
      } else if (action instanceof WaitGamemodeChangeAction && mc != null && mc.gameMode != null) {
         String mode = mc.gameMode.getPlayerMode().getName();
         WaitGamemodeChangeAction wait = (WaitGamemodeChangeAction)action;
         return wait.match == WaitGamemodeChangeAction.MatchMode.TO_MODE && !wait.matches(mc.gameMode.getPlayerMode())
            ? null
            : MacroVarValue.of(MacroVarValue.ValueType.TEXT, mode, Map.of("name", MacroVarValue.ofText(mode)));
      } else {
         return null;
      }
   }

   private static MacroVarValue positionValue(Minecraft mc) {
      if (mc != null && mc.player != null) {
         String display = String.format(Locale.ROOT, "%.3f, %.3f, %.3f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
         return MacroVarValue.of(
            MacroVarValue.ValueType.POSITION,
            display,
            Map.of(
               "x",
               MacroVarValue.ofNumber(mc.player.getX()),
               "y",
               MacroVarValue.ofNumber(mc.player.getY()),
               "z",
               MacroVarValue.ofNumber(mc.player.getZ()),
               "yaw",
               MacroVarValue.ofNumber(mc.player.getYRot()),
               "pitch",
               MacroVarValue.ofNumber(mc.player.getXRot())
            )
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue inventoryItemValue(Minecraft mc, String itemTemplate, int slot, String condition) {
      if (mc != null && mc.player != null) {
         MacroTemplateResolver.ResolveResult item = MacroVariableContext.resolveTemplate(itemTemplate, mc);
         if (!item.success()) {
            return null;
         }

         MacroItemTarget target = MacroItemTarget.parse(item.value());
         if (slot >= 0) {
            int safeSlot = Math.max(0, Math.min(mc.player.getInventory().getContainerSize() - 1, slot));
            MacroVarValue stack = itemStackValue(mc.player.getInventory().getItem(safeSlot), safeSlot);
            return stack == null
               ? MacroVarValue.of(
                  MacroVarValue.ValueType.SLOT,
                  Integer.toString(slot),
                  Map.of("slot", MacroVarValue.ofSlot(slot), "empty", MacroVarValue.ofText("true"), "condition", MacroVarValue.ofText(condition))
               )
               : stack;
         }

         if (target.hasItemFilter()) {
            MacroVarValue matched = findItemValue(mc, target);
            if (matched != null) {
               return matched.withProperty("condition", MacroVarValue.ofText(condition));
            }
         }

         return MacroVarValue.of(
            MacroVarValue.ValueType.TEXT,
            condition,
            Map.of("condition", MacroVarValue.ofText(condition), "selected_slot", MacroVarValue.ofSlot(mc.player.getInventory().getSelectedSlot()))
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue findItemValue(Minecraft mc, MacroItemTarget target) {
      if (mc != null && mc.player != null && target != null) {
         if (mc.player.containerMenu != null) {
            for (Slot slot : mc.player.containerMenu.slots) {
               if (slot != null && !slot.getItem().isEmpty()) {
                  int visible = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                  if (target.matchScore(slot.getItem(), visible) >= 0) {
                     return itemStackValue(slot.getItem(), visible);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static MacroVarValue itemStackValue(ItemStack stack, int slot) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String itemId = id == null ? "" : id.toString();
         String name = stack.getHoverName().getString();
         return MacroVarValue.of(
            MacroVarValue.ValueType.ITEM,
            name,
            Map.of("name", MacroVarValue.ofText(name), "id", MacroVarValue.ofIdentifier(itemId), "count", MacroVarValue.ofNumber(stack.getCount()), "slot", MacroVarValue.ofSlot(slot))
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue packetValue(Packet<?> packet, String direction) {
      if (packet == null) {
         return null;
      }

      LinkedHashMap<String, MacroVarValue> properties = new LinkedHashMap<>();
      String packetName = PacketNameResolver.directionalName(packet, direction);
      properties.put("name", MacroVarValue.ofText(packetName));
      properties.put("class", MacroVarValue.ofText(packet.getClass().getName()));
      properties.put("direction", MacroVarValue.ofText(direction));
      RecordComponent[] components = packet.getClass().getRecordComponents();
      if (components != null) {
         for (RecordComponent component : components) {
            if (properties.size() >= 64) {
               break;
            }

            try {
               Object value = component.getAccessor().invoke(packet);
               properties.put(component.getName().toLowerCase(Locale.ROOT), MacroVarValue.ofText(value));
            } catch (ReflectiveOperationException var10) {
            }
         }
      }

      return MacroVarValue.of(MacroVarValue.ValueType.PACKET, packetName, properties);
   }

   private static void stashCaptures(MacroAction action, Map<String, MacroVarValue> values) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null && values != null) {
         run.capturedValues.put(action, Map.copyOf(values));
      }
   }

   private static void applyStashedCaptures(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      if (run != null && action != null) {
         Map<String, MacroVarValue> values = run.capturedValues.remove(action);
         if (values != null) {
            run.variables.putAll(values);
         }
      }
   }

   private static MacroExecutor.AsyncActionHandle armPollingWait(MacroAction action, BooleanSupplier predicate, int timeoutMs) {
      AtomicBoolean active = new AtomicBoolean(true);
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      Thread worker = new Thread(() -> runWithCurrentRun(run, () -> {
         long start = System.currentTimeMillis();

         while (active.get() && isStillRunning() && !future.isDone()) {
            try {
               if (predicate.getAsBoolean()) {
                  future.complete(null);
                  return;
               }
            } catch (Throwable var7) {
            }

            if (timeoutMs > 0 && System.currentTimeMillis() - start >= timeoutMs) {
               future.complete(null);
               return;
            }

            LockSupport.parkNanos(500000L);
            if (Thread.interrupted()) {
               return;
            }
         }
      }), "MacroPrearmedWait");
      worker.setDaemon(true);
      worker.start();
      return new MacroExecutor.AsyncActionHandle(action, future, () -> {
         active.set(false);
         if (!future.isDone()) {
            future.cancel(true);
         }

         worker.interrupt();
      }, 0);
   }

   private static MacroExecutor.AsyncActionHandle armPacketWait(MacroAction action, String target, int completedPacketTargets) {
      String normalizedTarget = WaitForPacketAction.normalizeTarget(target);
      String direction = WaitForPacketAction.directionOf(normalizedTarget);
      CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
      MacroExecutor.PacketWaiter send = null;
      MacroExecutor.PacketWaiter recv = null;
      if (!"S2C".equalsIgnoreCase(direction)) {
         send = registerOutgoingPacketWaiter(packet -> matchesPacketTarget(normalizedTarget, packet, "C2S"));
         send.future.thenAccept(packet -> {
            storeMatchedPacket(action, (Packet<?>)packet, "C2S");
            matched.complete((Packet<?>)packet);
         });
      }

      if (!"C2S".equalsIgnoreCase(direction)) {
         recv = registerIncomingPacketWaiter(packet -> matchesPacketTarget(normalizedTarget, packet, "S2C"));
         recv.future.thenAccept(packet -> {
            storeMatchedPacket(action, (Packet<?>)packet, "S2C");
            matched.complete((Packet<?>)packet);
         });
      }

      MacroExecutor.PacketWaiter finalSend = send;
      MacroExecutor.PacketWaiter finalRecv = recv;
      return new MacroExecutor.AsyncActionHandle(action, matched, () -> {
         if (finalSend != null) {
            finalSend.cancel();
         }

         if (finalRecv != null) {
            finalRecv.cancel();
         }

         if (!matched.isDone()) {
            matched.cancel(true);
         }
      }, completedPacketTargets);
   }

   private static MacroExecutor.AsyncActionHandle armPacketMatchWait(WaitPacketMatchAction action) {
      MacroExecutor.PacketWaiter send = null;
      MacroExecutor.PacketWaiter recv = null;
      CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
      if (action.direction != WaitPacketMatchAction.PacketDirection.S2C) {
         send = registerOutgoingPacketWaiter(packet -> action.matches(packet, "C2S"));
         send.future.thenAccept(packet -> {
            storeMatchedPacket(action, (Packet<?>)packet, "C2S");
            matched.complete((Packet<?>)packet);
         });
      }

      if (action.direction != WaitPacketMatchAction.PacketDirection.C2S) {
         recv = registerIncomingPacketWaiter(packet -> action.matches(packet, "S2C"));
         recv.future.thenAccept(packet -> {
            storeMatchedPacket(action, (Packet<?>)packet, "S2C");
            matched.complete((Packet<?>)packet);
         });
      }

      if (send == null && recv == null) {
         return null;
      }

      if (action.timeoutMs > 0) {
         matched.completeOnTimeout(null, action.timeoutMs, TimeUnit.MILLISECONDS);
      }

      MacroExecutor.PacketWaiter finalSend = send;
      MacroExecutor.PacketWaiter finalRecv = recv;
      return new MacroExecutor.AsyncActionHandle(action, matched, () -> {
         if (finalSend != null) {
            finalSend.cancel();
         }

         if (finalRecv != null) {
            finalRecv.cancel();
         }

         if (!matched.isDone()) {
            matched.cancel(true);
         }
      }, 0);
   }

   private static void waitForPacketMatch(WaitPacketMatchAction action) {
      if (action != null) {
         MacroExecutor.PacketWaiter send = null;
         MacroExecutor.PacketWaiter recv = null;

         try {
            if (action.direction != WaitPacketMatchAction.PacketDirection.S2C) {
               send = registerOutgoingPacketWaiter(packet -> {
                  boolean matches = action.matches(packet, "C2S");
                  if (matches) {
                     storeMatchedPacket(action, packet, "C2S");
                  }

                  return matches;
               });
            }

            if (action.direction != WaitPacketMatchAction.PacketDirection.C2S) {
               recv = registerIncomingPacketWaiter(packet -> {
                  boolean matches = action.matches(packet, "S2C");
                  if (matches) {
                     storeMatchedPacket(action, packet, "S2C");
                  }

                  return matches;
               });
            }

            CompletableFuture<?> future;
            if (send != null && recv != null) {
               future = CompletableFuture.anyOf(send.future, recv.future);
            } else if (send != null) {
               future = send.future;
            } else {
               if (recv == null) {
                  return;
               }

               future = recv.future;
            }

            if (action.timeoutMs > 0) {
               future.completeOnTimeout(null, action.timeoutMs, TimeUnit.MILLISECONDS);
            }

            awaitWithTimeout(future, action.timeoutMs);
         } finally {
            if (send != null) {
               send.cancel();
            }

            if (recv != null) {
               recv.cancel();
            }
         }
      }
   }

   private static void runPacketGateAction(Minecraft mc, PacketGateAction action) {
      if (action != null) {
         action.execute(mc);
         if (action.mode != PacketGateAction.GateMode.DISABLE_GATE) {
            switch (action.durationMode) {
               case UNTIL_PACKET:
                  WaitPacketMatchAction wait = new WaitPacketMatchAction();
                  WaitPacketMatchAction.PacketDirection var10001;
                  switch (action.direction) {
                     case C2S:
                        var10001 = WaitPacketMatchAction.PacketDirection.C2S;
                        break;
                     case S2C:
                        var10001 = WaitPacketMatchAction.PacketDirection.S2C;
                        break;
                     case ANY:
                        var10001 = WaitPacketMatchAction.PacketDirection.ANY;
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  wait.direction = var10001;
                  wait.packetName = action.untilPacketName != null && !action.untilPacketName.isBlank()
                     ? action.untilPacketName
                     : (action.packetNames.isEmpty() ? "" : action.packetNames.get(0));
                  wait.fieldName = action.untilPacketField;
                  wait.operator = MacroNbtCodecHelper.parseEnum(WaitPacketMatchAction.MatchOperator.class, action.untilPacketOperator, WaitPacketMatchAction.MatchOperator.EXISTS);
                  wait.value = action.untilPacketValue;
                  wait.timeoutMs = Math.max(0, action.durationValue);
                  waitForPacketMatch(wait);
                  releasePacketGate(mc, action);
                  break;
               case UNTIL_GUI:
                  waitUntilOrTimeout(() -> GuiScreenMatcher.matches(mc.gui.screen(), action.untilGuiType, action.untilGuiTitle), Math.max(0, action.durationValue));
                  releasePacketGate(mc, action);
                  break;
               case UNTIL_INVENTORY:
                  WaitInventoryPredicateAction wait2 = new WaitInventoryPredicateAction();
                  wait2.condition = MacroNbtCodecHelper.parseEnum(WaitInventoryPredicateAction.InventoryCondition.class, action.untilInventoryCondition, WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS);
                  wait2.itemName = action.untilInventoryItem;
                  wait2.count = action.untilInventoryCount;
                  wait2.slot = action.untilInventorySlot;
                  wait2.timeoutMs = Math.max(0, action.durationValue);
                  wait2.resetBaseline();
                  waitUntilOrTimeout(() -> wait2.isSatisfied(mc), wait2.timeoutMs);
                  releasePacketGate(mc, action);
            }
         }
      }
   }

   private static void releasePacketGate(Minecraft mc, PacketGateAction action) {
      PacketGateManager.close(
         action.gateId != null && !action.gateId.isBlank() ? action.gateId : "auto", getCurrentRunId(), mc == null ? null : mc.getConnection(), action.flushOnDisable
      );
   }

   private static void runFinallyActions(MacroExecutor.MacroRun run, Minecraft mc) {
      if (run != null && !run.finallyActions.isEmpty()) {
         ArrayList<MacroAction> actions = new ArrayList<>(run.finallyActions);
         run.finallyActions.clear();

         for (MacroAction action : actions) {
            if (action != null && action.isEnabled()) {
               try {
                  executeMacroAction(mc, action, autismclient.modules.ModuleRuntimeManager.getInstance());
               } catch (Throwable t) {
                  AutismClientMessaging.sendPrefixed("§cCleanup failed: " + describeError(t));
               }
            }
         }
      }
   }

   private static void waitForMacroStep(WaitForMacroStepAction action) {
      long startMs = System.currentTimeMillis();

      while (isStillRunning()) {
         if (isMacroStepReached(action)) {
            return;
         }

         if (action.timeoutMs > 0 && System.currentTimeMillis() - startMs >= action.timeoutMs) {
            return;
         }

         CompletableFuture<Void> tick = MacroWaitRegistry.awaitTickSync();
         awaitWhileRunning(tick);
      }
   }

   private static boolean isMacroStepReached(WaitForMacroStepAction action) {
      String target = action.macroName == null ? "" : action.macroName.trim();
      if (target.isEmpty()) {
         return true;
      }

      synchronized (RUNS_LOCK) {
         for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
            String name = run.macro != null && run.macro.name != null ? run.macro.name : "";
            if (name.equalsIgnoreCase(target)) {
               boolean var10000;
               switch (action.mode) {
                  case STARTED_STEP:
                     var10000 = run.currentStep + 1 >= Math.max(1, action.step);
                     break;
                  case COMPLETED_STEP:
                     var10000 = run.lastCompletedStep >= Math.max(1, action.step);
                     break;
                  case FINISHED:
                     var10000 = !run.running.get();
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return var10000;
            }
         }
      }

      return action.mode == WaitForMacroStepAction.StepEvent.FINISHED;
   }

   private static int runRaceAction(Minecraft mc, autismclient.modules.ModuleRuntimeManager module, List<MacroAction> actions, int headerIndex, RaceAction race) {
      int bodyCount = race.countBodyActions(actions, headerIndex);
      List<MacroAction> conditions = new ArrayList<>();
      List<MacroAction> bodyActions = new ArrayList<>();

      for (int offset = 1; offset <= bodyCount && headerIndex + offset < actions.size(); offset++) {
         MacroAction child = actions.get(headerIndex + offset);
         if (child != null && child.isEnabled()) {
            if (RaceAction.isRaceCondition(child)) {
               conditions.add(child);
            } else if (RaceAction.load(child)) {
               bodyActions.add(child);
            }
         }
      }

      setRunStatus(
         conditions.isEmpty()
            ? "Race: dispatching"
            : "Race: waiting for " + (conditions.size() == 1 ? conditions.get(0).getDisplayName() : conditions.size() + " conditions")
      );
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      CompletableFuture<Void> bodyFuture = new CompletableFuture<>();
      AtomicBoolean dispatched = new AtomicBoolean(false);
      CompletableFuture<?> triggerFuture = raceConditionFuture(mc, conditions);
      triggerFuture.thenRun(() -> {
         if (dispatched.compareAndSet(false, true)) {
            if (run != null && !run.running.get()) {
               bodyFuture.cancel(true);
            } else {
               runRaceBody(mc, module, bodyActions, run, bodyFuture);
            }
         }
      }).exceptionally(error -> {
         if (!bodyFuture.isDone()) {
            bodyFuture.completeExceptionally(error);
         }

         return null;
      });
      boolean fired = awaitCompletion(triggerFuture, Math.max(0, race.timeoutMs));
      if (fired) {
         setRunStatus("Race: fired");
         awaitCompletion(bodyFuture, 250L);
         return bodyCount;
      } else {
         triggerFuture.cancel(true);
         bodyFuture.cancel(true);
         AutismClientMessaging.sendPrefixed("§eRace timed out: " + (race.label != null && !race.label.isBlank() ? race.label : "Race"));
         setRunStatus("Race: timeout");
         return bodyCount;
      }
   }

   private static CompletableFuture<?> raceConditionFuture(Minecraft mc, List<MacroAction> conditions) {
      if (conditions != null && !conditions.isEmpty()) {
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         List<CompletableFuture<MacroExecutor.PacketEvent>> baseFutures = new ArrayList<>();
         List<CompletableFuture<MacroExecutor.PacketEvent>> futures = new ArrayList<>();

         for (MacroAction condition : conditions) {
            resetCaptureOutputs(condition);
            CompletableFuture<MacroExecutor.PacketEvent> base = triggerEventFuture(mc, condition);
            baseFutures.add(base);
            futures.add(base.thenApply(event -> {
               if (event.packet() != null) {
                  storeMatchedPacket(condition, event.packet(), event.direction());
               }

               if (condition instanceof CaptureOutputAction output) {
                  String name = MacroVariableStore.rootKey(output.getSaveAs());
                  if (name.isEmpty()) {
                     return (MacroExecutor.PacketEvent)event;
                  }

                  MacroVarValue value = captureValueOf(condition, mc);
                  if (value == null) {
                     return (MacroExecutor.PacketEvent)event;
                  }

                  LinkedHashMap<String, MacroVarValue> captures = new LinkedHashMap<>(event.captures());
                  captures.put(name, value);
                  return new MacroExecutor.PacketEvent(event.packet(), event.direction(), event.nanoTime(), event.chatCapture(), captures);
               } else {
                  return (MacroExecutor.PacketEvent)event;
               }
            }));
         }

         CompletableFuture<Object> first = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
         CompletableFuture<Object> out = new CompletableFuture<>();
         first.whenComplete((value, e) -> {
            if (e != null) {
               out.completeExceptionally(e);
            } else {
               if (value instanceof MacroExecutor.PacketEvent event && run != null && !event.captures().isEmpty()) {
                  run.variables.putAll(event.captures());
               }

               out.complete(value);
            }
         });
         out.whenComplete((v, e) -> {
            for (CompletableFuture<MacroExecutor.PacketEvent> future : baseFutures) {
               if (!future.isDone()) {
                  future.cancel(true);
               }
            }
         });
         return out;
      } else {
         return CompletableFuture.completedFuture(null);
      }
   }

   private static int runReportAction(Minecraft mc, List<MacroAction> actions, int headerIndex, ReportAction report) {
      int conditionCount = report.coveredCount(actions, headerIndex);
      if (conditionCount >= 2 && headerIndex + 2 < actions.size()) {
         MacroAction startCondition = actions.get(headerIndex + 1);
         MacroAction endCondition = actions.get(headerIndex + 2);
         if (RaceAction.load(startCondition) && RaceAction.isRaceCondition(endCondition)) {
            int timeoutMs = Math.max(100, report.timeoutMs);
            String label = report.reportLabel != null && !report.reportLabel.isBlank() ? report.reportLabel.trim() : "Report";
            setRunStatus("Report: start " + startCondition.getDisplayName());
            CompletableFuture<MacroExecutor.PacketEvent> startAnchor = outgoingPacketAnchor(startCondition);
            executeMacroAction(mc, startCondition, null);
            if (!isStillRunning()) {
               return conditionCount;
            }

            MacroExecutor.PacketEvent startEvent = awaitAnchorEvent(
               startAnchor, 250, startCondition.getDisplayName(), "client action"
            );
            setRunStatus("Report: waiting end " + endCondition.getDisplayName());
            CompletableFuture<MacroExecutor.PacketEvent> endFuture = triggerEventFuture(mc, endCondition);
            MacroExecutor.PacketEvent endEvent = awaitEvent(endFuture, timeoutMs);
            if (endEvent == null) {
               endFuture.cancel(true);
               AutismClientMessaging.sendPrefixed("§eReport \"" + label + "\": timeout waiting for end condition");
               return conditionCount;
            }

            long deltaNanos = Math.max(0L, endEvent.nanoTime() - startEvent.nanoTime());
            long deltaMs = deltaNanos / 1000000L;
            String start = startEvent.describeEvent();
            String end = endEvent.describeEvent();
            AutismClientMessaging.sendPrefixed("§b" + label + ": " + formatNanosAsMs(deltaNanos) + " (" + start + " -> " + end + ")");
            if (report.stashToSharedState) {
               AutismSessionState.getInstance().recordTimingMeasurement(new AutismSessionState.TimingMeasurement(label, deltaMs, start, end, System.currentTimeMillis()));
            }

            return conditionCount;
         } else if (RaceAction.isRaceCondition(startCondition) && RaceAction.isRaceCondition(endCondition)) {
            int timeoutMs = Math.max(100, report.timeoutMs);
            long startWaitNanos = System.nanoTime();
            setRunStatus("Report: waiting start " + startCondition.getDisplayName());
            boolean startFired = executeTriggerAction(mc, startCondition, timeoutMs);
            if (!startFired) {
               AutismClientMessaging.sendPrefixed("§eReport \"" + report.reportLabel + "\": timeout waiting for start condition");
               return conditionCount;
            }

            MacroExecutor.PacketEvent startEvent = new MacroExecutor.PacketEvent(
               null, "condition " + startCondition.getDisplayName(), System.nanoTime(), null
            );
            long spentMs = (startEvent.nanoTime() - startWaitNanos) / 1000000L;
            int remainingMs = (int)Math.max(1L, timeoutMs - spentMs);
            setRunStatus("Report: waiting end " + endCondition.getDisplayName());
            CompletableFuture<MacroExecutor.PacketEvent> endFuture = triggerEventFuture(mc, endCondition);
            MacroExecutor.PacketEvent endEvent = awaitEvent(endFuture, remainingMs);
            if (endEvent == null) {
               endFuture.cancel(true);
               AutismClientMessaging.sendPrefixed("§eReport \"" + report.reportLabel + "\": timeout waiting for end condition");
               return conditionCount;
            }

            long deltaNanos = Math.max(0L, endEvent.nanoTime() - startEvent.nanoTime());
            long deltaMs = deltaNanos / 1000000L;
            String label = report.reportLabel != null && !report.reportLabel.isBlank() ? report.reportLabel.trim() : "Report";
            String start = startEvent.describeEvent();
            String end = endEvent.describeEvent();
            AutismClientMessaging.sendPrefixed("§b" + label + ": " + formatNanosAsMs(deltaNanos) + " (" + start + " -> " + end + ")");
            if (report.stashToSharedState) {
               AutismSessionState.getInstance().recordTimingMeasurement(new AutismSessionState.TimingMeasurement(label, deltaMs, start, end, System.currentTimeMillis()));
            }

            return conditionCount;
         } else {
            report.execute(mc);
            return conditionCount;
         }
      } else {
         setRunStatus(report.getDisplayName());
         report.execute(mc);
         return conditionCount;
      }
   }

   private static boolean runRaceTrigger(Minecraft mc, RaceAction race) {
      MacroAction trigger = race.triggerAction == null ? new DelayAction(0) : race.triggerAction;
      int timeoutMs = Math.max(0, race.timeoutMs);
      return executeTriggerAction(mc, trigger, timeoutMs);
   }

   public static boolean executeTriggerAction(Minecraft mc, MacroAction trigger, int timeoutMs) {
      if (trigger == null) {
         return true;
      }

      CompletableFuture<?> future = triggerWaitFuture(mc, trigger);
      if (future == null) {
         executeMacroAction(mc, trigger, null);
         return true;
      }

      boolean fired = awaitWithTimeout(future, timeoutMs);
      if (!fired) {
         future.cancel(true);
      }

      return fired;
   }

   private static void runRaceBody(
      Minecraft mc,
      autismclient.modules.ModuleRuntimeManager module,
      List<MacroAction> bodyActions,
      MacroExecutor.MacroRun run,
      CompletableFuture<Void> bodyFuture
   ) {
      if (bodyActions != null && !bodyActions.isEmpty()) {
         Runnable task = () -> {
            MacroExecutor.MacroRun previous = CURRENT_RUN.get();
            if (run != null) {
               CURRENT_RUN.set(run);
            }

            EnumSet<MacroResourceClassifier.MacroResource> heldResources = EnumSet.noneOf(MacroResourceClassifier.MacroResource.class);

            try {
               if (bodyActions.stream().noneMatch(ToolsAction.class::isInstance)) {
                  heldResources = acquireResourceLocks(resourcesOfActions(bodyActions));
               }

               Throwable firstFailure = null;

               for (MacroAction body : bodyActions) {
                  if (body != null && body.isEnabled() && RaceAction.load(body)) {
                     if (run != null && !run.running.get()) {
                        bodyFuture.cancel(true);
                        return;
                     }

                     try {
                        executeRaceBodyAction(mc, body);
                     } catch (Throwable t) {
                        if (firstFailure == null) {
                           firstFailure = t;
                        }

                        AutismClientMessaging.sendPrefixed("§cRace action failed (" + body.getDisplayName() + "): " + describeError(t));
                     }
                  }
               }

               if (firstFailure == null) {
                  bodyFuture.complete(null);
               } else {
                  bodyFuture.completeExceptionally(firstFailure);
               }
            } catch (Throwable t) {
               AutismClientMessaging.sendPrefixed("§cRace action failed: " + describeError(t));
               bodyFuture.completeExceptionally(t);
            } finally {
               releaseResourceLocks(heldResources);
               if (previous != null) {
                  CURRENT_RUN.set(previous);
               } else {
                  CURRENT_RUN.remove();
               }
            }
         };
         if (bodyActions.stream().allMatch(MacroExecutor::emitsAnchorPacket)) {
            task.run();
         } else {
            mc.execute(task);
         }
      } else {
         bodyFuture.complete(null);
      }
   }

   private static void executeRaceBodyAction(Minecraft mc, MacroAction body) {
      if (body != null && body.isEnabled() && RaceAction.load(body) && DynamicBindingManager.apply(body, mc)) {
         body.execute(mc);
      }
   }

   private static boolean emitsAnchorPacket(MacroAction action) {
      if (action instanceof SendChatAction chat) {
         return !chat.waitsForGuiBefore() && !chat.waitsForGuiAfter();
      } else {
         return action instanceof SendPacketAction sendPacket ? !sendPacket.waitsForGuiBefore() && !sendPacket.waitsForGuiAfter() : action instanceof PacketClickAction;
      }
   }

   private static CompletableFuture<MacroExecutor.PacketEvent> outgoingPacketAnchor(MacroAction action) {
      if (action instanceof PacketClickAction) {
         return registerOutgoingPacketMatch(packet -> packet instanceof ServerboundContainerClickPacket).future;
      } else if (action instanceof ItemAction) {
         return registerOutgoingPacketMatch(packet -> packet instanceof ServerboundContainerClickPacket).future;
      } else if (action instanceof CloseGuiAction) {
         return registerOutgoingPacketMatch(packet -> packet instanceof ServerboundContainerClosePacket).future;
      } else if (action instanceof SendChatAction) {
         return registerOutgoingPacketMatch(
               packet -> packet instanceof ServerboundChatPacket
                  || packet instanceof ServerboundChatCommandPacket
                  || packet instanceof ServerboundChatCommandSignedPacket
            )
            .future;
      } else {
         return !(action instanceof SendPacketAction) && !(action instanceof PayloadAction) ? null : registerOutgoingPacketMatch(packet -> true).future;
      }
   }

   private static MacroExecutor.PacketEvent awaitAnchorEvent(
      CompletableFuture<MacroExecutor.PacketEvent> anchor, int timeoutMs, String label, String fallbackDirection
   ) {
      if (anchor != null) {
         MacroExecutor.PacketEvent event = awaitEvent(anchor, timeoutMs);
         if (event != null) {
            return event;
         }

         anchor.cancel(true);
      }

      return new MacroExecutor.PacketEvent(null, fallbackDirection + " " + (label == null ? "" : label), System.nanoTime(), null);
   }

   private static MacroExecutor.PacketEvent awaitEvent(
      CompletableFuture<MacroExecutor.PacketEvent> future, long timeoutMs
   ) {
      if (future == null) {
         return null;
      }

      try {
         return timeoutMs <= 0L ? await(future) : await(future, timeoutMs);
      } catch (CancellationException e) {
         return null;
      }
   }

   private static boolean awaitCompletion(CompletableFuture<?> future, long timeoutMs) {
      if (future == null) {
         return true;
      }

      try {
         if (timeoutMs <= 0L) {
            await(future);
         } else {
            await(future, timeoutMs);
         }

         return !future.isCancelled() && isStillRunning();
      } catch (CancellationException e) {
         return false;
      }
   }

   private static String formatNanosAsMs(long nanos) {
      return String.format(Locale.ROOT, "%.3f ms", nanos / 1000000.0);
   }

   private static String describeError(Throwable throwable) {
      if (throwable == null) {
         return "unknown error";
      }

      String message = throwable.getMessage();
      return message != null && !message.isBlank() ? message : throwable.getClass().getSimpleName();
   }

   private static boolean isJoinPacket(Packet<?> packet) {
      return packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket;
   }

   private static CompletableFuture<MacroExecutor.PacketEvent> triggerEventFuture(Minecraft mc, MacroAction trigger) {
      if (trigger == null) {
         return CompletableFuture.completedFuture(new MacroExecutor.PacketEvent(null, "condition", System.nanoTime(), null));
      } else if (trigger instanceof WaitForChatAction chat) {
         MacroVariableStore context = getCurrentVariableStore();
         AtomicReference<Map<String, MacroVarValue>> captures = new AtomicReference<>(Map.of());
         MacroExecutor.PacketMatchWaiter listener = registerIncomingEventWaiter((packet, direction, now) -> {
            MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
            Optional<Map<String, MacroVarValue>> matched = matchChatCapture(chat, capture, context);
            matched.ifPresent(captures::set);
            return matched.isPresent();
         });
         CompletableFuture<MacroExecutor.PacketEvent> out = new CompletableFuture<>();
         listener.future
            .whenComplete(
               (event, e) -> {
                  if (e != null) {
                     out.completeExceptionally(e);
                  } else {
                     out.complete(
                        new MacroExecutor.PacketEvent(
                           event.packet(), event.direction(), event.nanoTime(), chatCaptureOf(event.packet()), captures.get()
                        )
                     );
                  }
               }
            );
         out.whenComplete((v, e) -> listener.dispose());
         return out;
      } else if (trigger instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.targets();
         CompletableFuture<MacroExecutor.PacketEvent> matched = new CompletableFuture<>();
         MacroExecutor.PacketMatchWaiter recv = registerIncomingEventWaiter((packet, direction, now) -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (matchesPacketTarget(target, packet, "S2C")) {
                  return true;
               }
            }

            return false;
         });
         MacroExecutor.PacketMatchWaiter send = registerOutgoingEventWaiter((packet, direction, now) -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (matchesPacketTarget(target, packet, "C2S")) {
                  return true;
               }
            }

            return false;
         });
         recv.future.thenAccept(matched::complete);
         send.future.thenAccept(matched::complete);
         matched.whenComplete((v, e) -> {
            recv.dispose();
            send.dispose();
         });
         return matched;
      } else if (trigger instanceof WaitForWorldChangeAction world) {
         CompletableFuture<MacroExecutor.PacketEvent> done = new CompletableFuture<>();
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.waitForWorldChange(world);
         MacroExecutor.PacketMatchWaiter worldListener = registerIncomingEventWaiter((packet, direction, now) -> isJoinPacket(packet));
         tickFuture.whenComplete((v, e) -> {
            if (e != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else {
               done.complete(new MacroExecutor.PacketEvent(null, "world change", System.nanoTime(), null));
            }
         });
         worldListener.future.whenComplete((event, error) -> {
            if (error != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else if (world.targetDimension == null || world.targetDimension.isBlank()) {
               done.complete(event);
            }
         });
         done.whenComplete((v, e) -> {
            worldListener.dispose();
            if (!tickFuture.isDone()) {
               tickFuture.cancel(true);
            }
         });
         return done;
      } else if (trigger instanceof WaitForTeleportAction teleport) {
         Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
         CompletableFuture<MacroExecutor.PacketEvent> done = new CompletableFuture<>();
         MacroExecutor.PacketMatchWaiter teleListener = registerIncomingEventWaiter(
            (packet, direction, now) -> packet instanceof ClientboundPlayerPositionPacket || isJoinPacket(packet)
         );
         teleListener.future.whenComplete((event, error) -> {
            teleListener.dispose();
            if (error != null) {
               done.cancel(true);
            } else if (teleport.minDistance <= 0.0) {
               done.complete(event);
            } else {
               MacroWaitRegistry.awaitPositionDeltaFrom(origin, teleport.minDistance, teleport.horizontalOnly).whenComplete((v, e) -> {
                  if (e != null) {
                     done.cancel(true);
                  } else {
                     done.complete(new MacroExecutor.PacketEvent(event.packet(), event.direction(), System.nanoTime(), event.chatCapture()));
                  }
               });
            }
         });
         done.whenComplete((v, e) -> teleListener.dispose());
         return done;
      } else {
         CompletableFuture<?> base = triggerWaitFuture(mc, trigger);
         if (base == null) {
            CompletableFuture<MacroExecutor.PacketEvent> done = new CompletableFuture<>();
            MacroExecutor.MacroRun run = CURRENT_RUN.get();
            mc.execute(() -> runWithCurrentRun(run, () -> {
               try {
                  executeMacroAction(mc, trigger, null);
                  done.complete(new MacroExecutor.PacketEvent(null, trigger.getDisplayName(), System.nanoTime(), null));
               } catch (Throwable t) {
                  done.completeExceptionally(t);
               }
            }));
            return done;
         } else {
            CompletableFuture<MacroExecutor.PacketEvent> timed = new CompletableFuture<>();
            base.whenComplete((v, e) -> {
               if (e != null) {
                  timed.cancel(true);
               } else {
                  timed.complete(new MacroExecutor.PacketEvent(null, trigger.getDisplayName(), System.nanoTime(), null));
               }
            });
            timed.whenComplete((v, e) -> {
               if (!base.isDone()) {
                  base.cancel(true);
               }
            });
            return timed;
         }
      }
   }

   private static CompletableFuture<?> triggerWaitFuture(Minecraft mc, MacroAction trigger) {
      if (trigger instanceof WaitMovementAction wm) {
         return triggerWaitFuture(mc, wm.toWaitAction());
      } else if (trigger instanceof WaitGamemodeChangeAction gm) {
         return MacroWaitRegistry.waitForGamemode(gm);
      } else if (trigger instanceof DelayAction delay) {
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         return CompletableFuture.runAsync(() -> runWithCurrentRun(run, () -> {
            if (delay.useTicks) {
               for (int i = 0; i < delay.delayTicks && isStillRunning(); i++) {
                  awaitWhileRunning(MacroWaitRegistry.awaitTickSync());
               }
            } else {
               sleep(Math.max(0, delay.delayMs));
            }
         }));
      } else if (trigger instanceof WaitForGuiAction gui) {
         CompletableFuture<MacroExecutor.PacketEvent> done = new CompletableFuture<>();
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         Thread worker = new Thread(() -> runWithCurrentRun(run, () -> {
            long start = System.currentTimeMillis();
            boolean saw = false;
            Map<String, MacroVarValue> observed = Map.of();

            while (!done.isDone() && isStillRunning()) {
               Optional<Map<String, MacroVarValue>> captured = gui.capture(Minecraft.getInstance().gui.screen());
               if (captured.isPresent()) {
                  saw = true;
                  observed = captured.get();
                  if (gui.waitMode == WaitForGuiAction.WaitMode.OPEN) {
                     done.complete(new MacroExecutor.PacketEvent(null, "GUI", System.nanoTime(), null, observed));
                     return;
                  }
               } else if (saw && gui.waitMode == WaitForGuiAction.WaitMode.CLOSE) {
                  done.complete(new MacroExecutor.PacketEvent(null, "GUI close", System.nanoTime(), null, observed));
                  return;
               }

               if (gui.timeoutMs > 0 && System.currentTimeMillis() - start >= gui.timeoutMs) {
                  done.cancel(false);
                  return;
               }

               LockSupport.parkNanos(500000L);
            }
         }), "MacroGuiCaptureWait");
         worker.setDaemon(true);
         worker.start();
         done.whenComplete((v, e) -> worker.interrupt());
         return done;
      } else if (trigger instanceof WaitForChatAction chat) {
         MacroVariableStore context = getCurrentVariableStore();
         MacroExecutor.PacketWaiter listener = registerIncomingPacketWaiter(packet -> {
            MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
            return capture != null && matchChatCapture(chat, capture, context).isPresent();
         });
         listener.future.whenComplete((v, e) -> listener.cancel());
         return listener.future;
      } else if (trigger instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.targets();
         CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
         MacroExecutor.PacketWaiter recv = registerIncomingPacketWaiter(packet -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (matchesPacketTarget(target, packet, "S2C")) {
                  return true;
               }
            }

            return false;
         });
         MacroExecutor.PacketWaiter send = registerOutgoingPacketWaiter(packet -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (matchesPacketTarget(target, packet, "C2S")) {
                  return true;
               }
            }

            return false;
         });
         recv.future.thenAccept(matched::complete);
         send.future.thenAccept(matched::complete);
         matched.whenComplete((v, e) -> {
            recv.cancel();
            send.cancel();
         });
         return matched;
      } else if (trigger instanceof WaitForHealthAction health) {
         return MacroWaitRegistry.awaitHealth(health.healthThreshold, health.below);
      } else if (trigger instanceof WaitForBlockAction block) {
         return MacroWaitRegistry.waitForBlock(block);
      } else if (trigger instanceof WaitForCooldownAction cooldown) {
         MacroItemTarget target = targetOrLegacy(cooldown.itemTarget, cooldown.itemName);
         return MacroWaitRegistry.awaitCooldown(target, cooldown.checkMainInteractionHand);
      } else if (trigger instanceof WaitPosAction pos) {
         return MacroWaitRegistry.waitForPosition(pos.f_x_D, pos.f_y_D, pos.f_z_D, pos.leeway, pos.checkRotation, pos.yaw, pos.pitch, pos.rotLeeway);
      } else if (trigger instanceof WaitForEntityAction entity) {
         return pollUntilAsync(() -> entity.isSatisfied(Minecraft.getInstance()), entity.timeoutMs);
      } else if (trigger instanceof WaitForSoundAction sound) {
         return MacroWaitRegistry.waitForSound(sound);
      } else if (trigger instanceof WaitForSlotChangeAction slot) {
         return MacroWaitRegistry.waitForSlotChange(slot);
      } else if (trigger instanceof WaitDurabilityAction durability) {
         return pollUntilAsync(() -> checkDurabilityOnce(Minecraft.getInstance(), durability), durability.timeoutMs);
      } else if (trigger instanceof WaitFreeSlotsAction freeSlots) {
         return pollUntilAsync(() -> freeSlots.test(Minecraft.getInstance()), freeSlots.timeoutMs);
      } else if (trigger instanceof TickSyncAction) {
         return MacroWaitRegistry.awaitTickSync();
      } else if (trigger instanceof ServerTickSyncAction serverTick) {
         return MacroWaitRegistry.awaitServerTickSync(serverTick.bufferMs, serverTick.maxWaitMs, serverTick.ignorePing);
      } else if (trigger instanceof WaitForWorldChangeAction world) {
         CompletableFuture<Void> done = new CompletableFuture<>();
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.waitForWorldChange(world);
         MacroExecutor.PacketWaiter worldListener = registerIncomingPacketWaiter(MacroExecutor::isJoinPacket);
         tickFuture.whenComplete((v, e) -> {
            if (e != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else {
               done.complete(null);
            }
         });
         worldListener.future.whenComplete((packet, error) -> {
            if (error != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else if (world.targetDimension == null || world.targetDimension.isBlank()) {
               done.complete(null);
            }
         });
         done.whenComplete((v, e) -> {
            worldListener.cancel();
            if (!tickFuture.isDone()) {
               tickFuture.cancel(true);
            }
         });
         return done;
      } else if (trigger instanceof WaitForPositionDeltaAction delta) {
         return MacroWaitRegistry.awaitPositionDelta(delta.distance, delta.horizontalOnly);
      } else if (trigger instanceof WaitForTeleportAction teleport) {
         Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
         CompletableFuture<Void> done = new CompletableFuture<>();
         MacroExecutor.PacketWaiter teleListener = registerIncomingPacketWaiter(
            packet -> packet instanceof ClientboundPlayerPositionPacket || isJoinPacket(packet)
         );
         teleListener.future.whenComplete((packet, error) -> {
            teleListener.cancel();
            if (error != null) {
               done.cancel(true);
            } else if (teleport.minDistance <= 0.0) {
               done.complete(null);
            } else {
               MacroWaitRegistry.awaitPositionDeltaFrom(origin, teleport.minDistance, teleport.horizontalOnly).whenComplete((v, e) -> {
                  if (e != null) {
                     done.cancel(true);
                  } else {
                     done.complete(null);
                  }
               });
            }
         });
         return done;
      } else if (trigger instanceof RevisionSyncAction) {
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         return CompletableFuture.runAsync(() -> runWithCurrentRun(run, () -> executeMacroAction(mc, trigger, null)));
      } else {
         return null;
      }
   }

   private static boolean awaitWithTimeout(CompletableFuture<?> future, long timeoutMs) {
      Thread t = Thread.currentThread();
      long start = System.nanoTime();
      long timeoutNanos = timeoutMs <= 0L ? 9223372036854775807L : timeoutMs * 1000000L;
      future.whenComplete((v, e) -> LockSupport.unpark(t));

      while (!future.isDone() && isStillRunning() && System.nanoTime() - start < timeoutNanos) {
         LockSupport.parkNanos(500000L);
         if (Thread.interrupted()) {
            break;
         }
      }

      return future.isDone() && !future.isCancelled() && isStillRunning();
   }

   private static void runToolsAction(Minecraft mc, ToolsAction action) {
      if (!MacroToolBridge.isAvailable()) {
         throw new IllegalStateException("Tools requires the full client");
      }

      long owner = getCurrentRunId();
      setRunStatus("Tools: " + ("RUN".equals(action.operation) ? MacroToolBridge.toolLabel(action.toolId) : action.operation.toLowerCase()));
      CompletableFuture<MacroToolBridge.ToolJob> start = new CompletableFuture<>();
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      mc.execute(() -> runWithCurrentRun(run, () -> {
         try {
            if (run != null && !run.running.get()) {
               start.cancel(false);
               return;
            }

            MacroToolBridge.ToolJob jobx = "RUN".equals(action.operation) ? action.startJob(mc, owner) : MacroToolBridge.findJob(owner, action.scope, "");
            if (run != null && !run.running.get() && "RUN".equals(action.operation) && jobx != null) {
               jobx.cancel();
            }

            start.complete(jobx);
         } catch (Throwable error) {
            start.completeExceptionally(error);
         }
      }));
      awaitWhileRunning(start.thenApply(ignored -> null));
      if (!isStillRunning()) {
         throw new RuntimeException("Macro stopped");
      }

      MacroToolBridge.ToolJob job = start.join();
      if (job == null) {
         throw new IllegalStateException("No matching tool has started");
      }

      CompletableFuture<MacroToolBridge.JobSnapshot> result;
      if ("RUN".equals(action.operation)) {
         awaitWhileRunning(job.startedFuture().thenApply(ignored -> null));
         if (!isStillRunning()) {
            throw new RuntimeException("Macro stopped");
         }

         result = job.future();
         if (!"WAIT".equals(action.completion) && !result.isDone()) {
            result.whenComplete((outcome, failure) -> {
               if (run != null && run.running.get()) {
                  runWithCurrentRun(run, () -> {
                     if (failure == null && (!job.isCancelRequested() || outcome.state() != MacroToolBridge.JobState.CANCELLED)) {
                        applyToolsOutcome(action, outcome, run);
                     }
                  });
               }
            });
            return;
         }
      } else {
         result = MacroToolBridge.control(job, action.operation);
      }

      awaitWhileRunning(result.thenApply(ignored -> null));
      if (!isStillRunning()) {
         throw new RuntimeException("Macro stopped");
      }

      applyToolsOutcome(action, result.join(), run);
   }

   private static void applyToolsOutcome(ToolsAction action, MacroToolBridge.JobSnapshot outcome, MacroExecutor.MacroRun run) {
      if (action.saveAs != null && !action.saveAs.isBlank()) {
         MacroVariableContext.set(action.saveAs, outcome.data());
      }

      if (action.stopOnFailure
         && outcome.state().isTerminal()
         && !outcome.isSucceeded()
         && (!"STOP".equals(action.operation) || outcome.state() != MacroToolBridge.JobState.CANCELLED)) {
         if (run != null) {
            run.outcome = "FAILED";
         }

         setRunStatus("Tools: " + outcome.message());
         AutismClientMessaging.sendPrefixed("§cTools: " + outcome.message());
         stopCurrentRunOrAll();
      }
   }

   private static void executeMacroAction(Minecraft mc, MacroAction action, autismclient.modules.ModuleRuntimeManager module) {
      if (action instanceof MultiAction || action instanceof SkippedLegacyMultiStep) {
         return;
      }
      if (DynamicBindingManager.apply(action, mc) && canResolveTemplates(action, mc)) {
         resetCaptureOutputs(action);
         EnumSet<MacroResourceClassifier.MacroResource> heldResources = acquireResourceLocksFor(action);

         try {
            if (guiPreconditionMet(mc, action)) {
               applyRaycastAim(mc, action);
               CompletableFuture<Void> postGuiFuture = !(action instanceof ItemAction) && !(action instanceof DropAction) && !isScreenOpeningAction(action)
                  ? postGuiWaitFuture(mc, action)
                  : null;
               if (action instanceof ToolsAction tools) {
                  runToolsAction(mc, tools);
               } else if (action instanceof DelayAction da) {
                  if (da.useTicks) {
                     for (int t = 0; t < da.delayTicks && isStillRunning(); t++) {
                        CompletableFuture<Void> tf = MacroWaitRegistry.awaitTickSync();
                        awaitWhileRunning(tf);
                     }
                  } else {
                     sleep(da.delayMs);
                  }
               } else if (action instanceof CustomMenuAction customMenu) {
                  setRunStatus(customMenu.getDisplayName());
                  customMenu.execute(mc);
               } else if (action instanceof WaitForPacketAction packetAction) {
                  packetAction.matchedPacket = null;
                  packetAction.matchedDirection = "";
                  MacroExecutor.MacroRun packetRun = CURRENT_RUN.get();
                  if (packetRun != null) {
                     packetRun.matchedPacket = null;
                     packetRun.matchedPacketDirection = "";
                  }

                  List<String> targets = packetAction.targets();
                  if (targets.isEmpty()) {
                     waitForPacketTarget("");
                  } else {
                     for (String target : targets) {
                        if (!isStillRunning()) {
                           break;
                        }

                        waitForPacketTarget(target);
                     }
                  }

                  applyMatchedPacket(packetAction);
               } else if (action instanceof WaitPacketMatchAction wpma) {
                  setRunStatus(wpma.getDisplayName());
                  waitForPacketMatch(wpma);
               } else if (action instanceof WaitInventoryPredicateAction wipa) {
                  setRunStatus(wipa.getDisplayName());
                  waitUntilOrTimeout(() -> wipa.isSatisfied(mc), wipa.timeoutMs);
               } else if (action instanceof WaitDurabilityAction wda) {
                  setRunStatus(wda.getDisplayName());
                  if (wda.useNext) {
                     waitForDurability(mc, wda);
                  } else {
                     waitUntilOrTimeout(() -> wda.isSatisfied(mc), wda.timeoutMs);
                  }
               } else if (action instanceof WaitFreeSlotsAction wfsa) {
                  setRunStatus(wfsa.getDisplayName());
                  waitUntilOrTimeout(() -> wfsa.test(mc), wfsa.timeoutMs);
               } else if (action instanceof WaitEntityTargetAction weta) {
                  setRunStatus(weta.getDisplayName());
                  waitUntilOrTimeout(() -> weta.isSatisfied(mc), weta.timeoutMs);
               } else if (action instanceof WaitGuiTypeAction wgta) {
                  setRunStatus(wgta.getDisplayName());
                  clearCaptures(wgta);
                  if (wgta.waitMode == WaitGuiTypeAction.WaitMode.CLOSE) {
                     waitUntilOrTimeout(guiTypeWaitCondition(wgta, mc), wgta.timeoutMs);
                  } else {
                     waitUntilOrTimeout(() -> {
                        Optional<Map<String, MacroVarValue>> captured = wgta.capture(mc);
                        captured.ifPresent(values -> stashCaptures(wgta, (Map<String, MacroVarValue>)values));
                        return captured.isPresent();
                     }, wgta.timeoutMs);
                  }

                  applyStashedCaptures(wgta);
               } else if (action instanceof PacketGateAction pga) {
                  runPacketGateAction(mc, pga);
               } else if (isScreenOpeningAction(action)) {
                  postGuiFuture = waitForTargetFuture(mc, action);
               } else if (action instanceof WaitForHealthAction wh) {
                  setRunStatus(wh.statusText());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.awaitHealth(wh.healthThreshold, wh.below);
                     awaitWhileRunning(future);
                  } catch (CancellationException var113) {
                  }
               } else if (action instanceof WaitForBlockAction wba) {
                  setRunStatus(wba.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForBlock(wba);
                     awaitWhileRunning(future);
                  } catch (CancellationException var112) {
                  }
               } else if (action instanceof WaitForGuiAction wga) {
                  clearCaptures(wga);
                  if (wga.waitMode == WaitForGuiAction.WaitMode.CLOSE) {
                     setRunStatus("Waiting for GUI close: " + wga.guiTitle);
                     waitUntilOrTimeout(guiWaitCondition(wga, mc), wga.timeoutMs);
                  } else {
                     setRunStatus("Waiting for GUI: " + wga.guiTitle);
                     waitUntilOrTimeout(() -> {
                        Optional<Map<String, MacroVarValue>> captured = wga.capture(mc.gui.screen());
                        captured.ifPresent(values -> stashCaptures(wga, (Map<String, MacroVarValue>)values));
                        return captured.isPresent();
                     }, wga.timeoutMs);
                  }

                  applyStashedCaptures(wga);
               } else if (action instanceof WaitForCooldownAction wca) {
                  MacroItemTarget cooldownTarget = targetOrLegacy(wca.itemTarget, wca.itemName).resolveTemplates(mc);
                  if (cooldownTarget == null) {
                     return;
                  }

                  String cooldownLabel = describeTarget(cooldownTarget);
                  setRunStatus("Waiting for cooldown: " + (!cooldownLabel.isEmpty() ? cooldownLabel : (wca.checkMainInteractionHand ? "Main Hand" : "Off Hand")));

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.awaitCooldown(cooldownTarget, wca.checkMainInteractionHand);
                     awaitWhileRunning(future);
                  } catch (CancellationException var111) {
                  }
               } else if (action instanceof WaitPosAction wpa) {
                  setRunStatus("Waiting for Pos: " + String.format("%.0f, %.0f, %.0f", wpa.f_x_D, wpa.f_y_D, wpa.f_z_D));

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForPosition(wpa.f_x_D, wpa.f_y_D, wpa.f_z_D, wpa.leeway, wpa.checkRotation, wpa.yaw, wpa.pitch, wpa.rotLeeway);
                     awaitWhileRunning(future);
                  } catch (CancellationException var110) {
                  }
               } else if (action instanceof CaptureValueAction captureValue) {
                  executeCapture(mc, captureValue);
               } else if (action instanceof WaitForChatAction wca) {
                  setRunStatus("Waiting for chat: " + wca.pattern);
                  waitForChatMatch(wca);
               } else if (action instanceof WaitForEntityAction wea) {
                  setRunStatus(wea.getDisplayName());

                  try {
                     waitUntilOrTimeout(() -> wea.isSatisfied(mc), wea.timeoutMs);
                  } catch (CancellationException var109) {
                  }
               } else if (action instanceof WaitForSoundAction wsa) {
                  String sndDesc = wsa.soundIds.isEmpty() ? "any" : wsa.soundIds.get(0);
                  setRunStatus("Waiting for sound: " + sndDesc);

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForSound(wsa);
                     awaitWhileRunning(future);
                  } catch (CancellationException var108) {
                  }
               } else if (action instanceof WaitForSlotChangeAction wsca) {
                  setRunStatus(wsca.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForSlotChange(wsca);
                     awaitWhileRunning(future);
                  } catch (CancellationException var107) {
                  }
               } else if (action instanceof WaitForWorldChangeAction world) {
                  setRunStatus(world.getDisplayName());

                  try {
                     CompletableFuture<?> future = triggerWaitFuture(mc, world);
                     awaitWithTimeout(future, 0L);
                  } catch (CancellationException var106) {
                  }
               } else if (action instanceof WaitForPositionDeltaAction wpda) {
                  setRunStatus(wpda.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.awaitPositionDelta(wpda.distance, wpda.horizontalOnly);
                     awaitWhileRunning(future);
                  } catch (CancellationException var105) {
                  }
               } else if (action instanceof WaitForTeleportAction teleport) {
                  setRunStatus(teleport.getDisplayName());

                  try {
                     CompletableFuture<?> future = triggerWaitFuture(mc, teleport);
                     awaitWithTimeout(future, 0L);
                  } catch (CancellationException var104) {
                  }
               } else if (action instanceof WaitGamemodeChangeAction gm) {
                  setRunStatus(gm.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForGamemode(gm);
                     awaitWithTimeout(future, gm.timeoutMs);
                  } catch (CancellationException var103) {
                  }
               } else if (action instanceof WaitMovementAction wm) {
                  setRunStatus(wm.getDisplayName());

                  try {
                     CompletableFuture<?> future = triggerWaitFuture(mc, wm.toWaitAction());
                     awaitWithTimeout(future, 0L);
                  } catch (CancellationException var102) {
                  }
               } else if (action instanceof WaitForLanStepAction wla) {
                  setRunStatus(wla.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.waitForLanStep(wla);
                     awaitWhileRunning(future);
                  } catch (CancellationException var101) {
                  }
               } else if (action instanceof WaitForMacroStepAction wma) {
                  setRunStatus(wma.getDisplayName());
                  waitForMacroStep(wma);
               } else if (action instanceof GoToAction ga) {
                  runGoToAction(mc, ga);
               } else if (action instanceof MoveAction ma) {
                  if (ma.nonBlocking) {
                     mc.execute(() -> ma.execute(mc));
                     heldMoves().put(ma, new int[]{ma.durationTicks - 1});
                  } else {
                     for (int t = 0; t < ma.durationTicks && isStillRunning(); t++) {
                        mc.execute(() -> ma.execute(mc));
                        CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                        awaitWhileRunning(tickFuture);
                     }

                     MacroExecutor.MacroRun movementRun = CURRENT_RUN.get();
                     mc.execute(() -> releaseMoveIfUnowned(movementRun, ma, mc));
                  }
               } else if (action instanceof LookAtBlockAction la) {
                  LookAtBlockAction.LookTarget rotationTarget = resolveLookTarget(mc, la);
                  if (rotationTarget == null) {
                     return;
                  }

                  if (la.smooth) {
                     startSmoothLook(rotationTarget.yaw(), rotationTarget.pitch(), la.getRotationStep());
                     if (la.waitForCompletion) {
                        awaitSmoothLookDone();
                     }
                  } else {
                     mc.execute(() -> {
                        if (mc.player != null) {
                           mc.player.setYRot(rotationTarget.yaw());
                           mc.player.setXRot(rotationTarget.pitch());
                        }
                     });
                  }
               } else if (action instanceof RotateAction ra) {
                  if (ra.smooth) {
                     startSmoothLook(ra.yaw, ra.pitch, ra.step());
                     if (ra.waitForCompletion) {
                        awaitSmoothLookDone();
                     }
                  } else {
                     mc.execute(() -> action.execute(mc));
                  }
               } else if (action instanceof MineAction mna) {
                  runMineAction(mc, mna);
               } else if (action instanceof InstaBreakAction) {
                  runInstaBreakAction(mc, (InstaBreakAction)action);
               } else if (action instanceof BreakAction) {
                  runBreakAction(mc, (BreakAction)action);
               } else if (action instanceof PlaceAction) {
                  runPlaceAction(mc, (PlaceAction)action);
               } else if (action instanceof UseItemAction) {
                  runUseItemAction(mc, (UseItemAction)action);
               } else if (action instanceof JumpAction ja) {
                  mc.execute(() -> ja.execute(mc));
                  if (ja.tap) {
                     CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                     awaitWhileRunning(tickFuture);
                  } else {
                     for (int t = 0; t < ja.durationTicks && isStillRunning(); t++) {
                        CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                        awaitWhileRunning(tickFuture);
                     }
                  }

                  mc.execute(() -> ja.releaseJump(mc));
               } else if (action instanceof SneakAction sna) {
                  if (sna.persistent && sna.sneak) {
                     heldActions().removeIf(p -> p instanceof SneakAction);
                     heldActions().add(sna);
                  } else if (!sna.sneak) {
                     heldActions().removeIf(p -> p instanceof SneakAction);
                  }

                  if (!sna.persistent && sna.sneak) {
                     postToClientThread(mc, () -> sna.execute(mc), sna.h_());
                  } else {
                     postToClientThread(mc, () -> syncSneakKey(mc), sna.h_());
                  }
               } else if (action instanceof SprintAction spa) {
                  if (spa.persistent && spa.sprint) {
                     heldActions().removeIf(p -> p instanceof SprintAction);
                     heldActions().add(spa);
                  } else if (!spa.sprint) {
                     heldActions().removeIf(p -> p instanceof SprintAction);
                  }

                  if (!spa.persistent && spa.sprint) {
                     postToClientThread(mc, () -> spa.execute(mc), spa.h_());
                  } else {
                     postToClientThread(mc, () -> syncSprintKey(mc), spa.h_());
                  }
               } else if (action instanceof NbtBookAction nba) {
                  int totalBooks = Math.max(1, nba.bookCount);
                  long delayMs = Math.max(1, nba.delayTicks) * 50L;
                  boolean signedAny = false;

                  for (int b = 0; b < totalBooks && isStillRunning(); b++) {
                     int bookIdx = b;
                     CompletableFuture<Boolean> result = new CompletableFuture<>();
                     mc.execute(() -> result.complete(nba.signBook(mc, bookIdx, totalBooks)));

                     try {
                        Boolean success = await(result);
                        if (!success) {
                           break;
                        }

                        signedAny = true;
                     } catch (RuntimeException e) {
                        break;
                     }

                     if (b < totalBooks - 1) {
                        sleep(delayMs);
                     }
                  }

                  if (signedAny && nba.disconnectAfter) {
                     mc.execute(() -> nba.disconnectIfRequested(mc));
                  }
               } else if (action instanceof ItemAction ia) {
                  if (ia.waitForItem || !ia.itemNames.isEmpty()) {
                     try {
                        waitForItemTargets(mc, ia);
                     } catch (CancellationException var100) {
                     }
                  }

                  postGuiFuture = postGuiWaitFuture(mc, ia);
                  executeGuarded(mc, action, "ItemAction");
               } else if (!(action instanceof DropAction) && !(action instanceof SwapSlotsAction)) {
                  if (action instanceof SendPacketAction) {
                     if (module != null && module.isInstantExecutionMode()) {
                        try {
                           action.execute(mc);
                        } catch (Exception e) {
                           AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + e.getMessage());
                        }
                     } else {
                        mc.execute(() -> {
                           try {
                              action.execute(mc);
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + e.getMessage());
                           }
                        });
                     }
                  } else if (action instanceof ClickAction ca) {
                     runClickAction(mc, ca);
                  } else if (action instanceof PayAction) {
                     runPayAction(mc, (PayAction)action);
                  } else if (action instanceof XCarryAction xsa) {
                     if (xsa.mode == XCarryAction.CarryMode.PUT_IN
                        && (xsa.transferMode == XCarryAction.TransferMode.CLICK || xsa.transferMode == XCarryAction.TransferMode.SAFE_CLICK)) {
                        if (xsa.transferMode == XCarryAction.TransferMode.SAFE_CLICK) {
                           setRunStatus("XCarry Safe");
                           runXCarrySafeAction(mc, xsa);
                        } else {
                           setRunStatus("XCarry Click");
                           runXCarryAction(mc, xsa);
                        }
                     } else {
                        String var10000;
                        switch (xsa.mode) {
                           case PUT_IN:
                              var10000 = "XCarry";
                              break;
                           case TAKE_OUT:
                              var10000 = "XCarry Out";
                              break;
                           case DROP:
                              var10000 = "XCarry Drop";
                              break;
                           default:
                              throw new MatchException(null, null);
                        }

                        setRunStatus(var10000);
                        runOnClientThread(mc, () -> {
                           try {
                              xsa.execute(mc);
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("§cXCarry: " + e.getMessage());
                           }
                        });
                     }
                  } else if (action instanceof CraftAction) {
                     runCraftAction(mc, (CraftAction)action);
                  } else if (action instanceof StoreItemAction sia) {
                     setRunStatus((sia.mode == StoreItemAction.TransferMode.LOOT ? "Looting" : "Storing") + (sia.persistent ? " ∞" : ""));
                     if (sia.persistent) {
                        while (isStillRunning()) {
                           try {
                              runStoreItemAction(mc, sia);
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                           }

                           CompletableFuture<Void> siTick = MacroWaitRegistry.awaitTickSync();
                           awaitWhileRunning(siTick);
                        }
                     } else {
                        try {
                           runStoreItemAction(mc, sia);
                        } catch (Exception e) {
                           AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                        }
                     }
                  } else if (action instanceof InventoryAuditAction auditAction
                     && (auditAction.mode == InventoryAuditAction.AuditMode.DUPE || auditAction.mode == InventoryAuditAction.AuditMode.DUPE_SPAM)) {
                     setRunStatus("Dupe: " + auditAction.dupeVector.name() + " " + auditAction.openMode.name());

                     try {
                        auditAction.runAudit(mc);
                     } catch (Throwable t) {
                        AutismClientMessaging.sendPrefixed("§cDupe error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
                     }
                  } else if (action instanceof ContainerClickSequenceAction sequence) {
                     runContainerClickSequence(mc, sequence);
                  } else if (action instanceof PacketBurstAction burst) {
                     runPacketBurstAction(mc, burst);
                  } else if (action instanceof TickSyncAction tsa) {
                     if (mc.level == null) {
                        return;
                     }

                     List<AutismSessionState.DelayedPacket> preGenerated = collectSlicePackets(tsa.preGenCount, mc);
                     long targetTick = mc.level.getGameTime() + tsa.tickOffset;
                     setRunStatus("Tick Sync -> " + targetTick + " (" + preGenerated.size() + " pkts)");

                     while (isStillRunning() && mc.level.getGameTime() < targetTick) {
                        LockSupport.parkNanos(1000000L);
                     }

                     if (isStillRunning()) {
                        sendQueuedPackets(mc, preGenerated);
                        MacroExecutor.ActiveStepSlice context = ACTIVE_STEP_SLICE.get();
                        if (context != null) {
                           SLICE_STEPS_CONSUMED.set(countActionsForPackets(context.macro(), context.nextIndex(), preGenerated.size(), context.endIndex()));
                        }

                        AutismClientMessaging.sendPrefixed("Tick sync: " + preGenerated.size() + " pkts sent");
                     }
                  } else if (action instanceof RevisionSyncAction rsa) {
                     if (mc.player == null || mc.player.containerMenu == null) {
                        return;
                     }

                     List<AutismSessionState.DelayedPacket> preGenerated = collectSlicePackets(rsa.preGenCount, mc);
                     int baseRevision = mc.player.containerMenu.getStateId();
                     int targetRevision = baseRevision + rsa.revisionOffset;
                     setRunStatus("Rev Sync -> " + targetRevision + " (" + preGenerated.size() + " pkts)");

                     while (isStillRunning() && mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu.getStateId() < targetRevision) {
                        LockSupport.parkNanos(500000L);
                     }

                     if (isStillRunning() && mc.player != null && mc.player.containerMenu != null) {
                        sendQueuedPackets(mc, preGenerated);
                        MacroExecutor.ActiveStepSlice context = ACTIVE_STEP_SLICE.get();
                        if (context != null) {
                           SLICE_STEPS_CONSUMED.set(countActionsForPackets(context.macro(), context.nextIndex(), preGenerated.size(), context.endIndex()));
                        }

                        AutismClientMessaging.sendPrefixed("Rev sync: " + preGenerated.size() + " pkts sent");
                     }
                  } else if (action instanceof ServerTickSyncAction stsa) {
                     if (mc.getConnection() == null) {
                        return;
                     }

                     List<AutismSessionState.DelayedPacket> preGenerated = collectSlicePackets(stsa.preGenCount, mc);

                     for (int sampleWaitMs = 0; isStillRunning() && !ServerTickPhaseTracker.isWarmedUp() && sampleWaitMs < stsa.maxWaitMs; sampleWaitMs += 50) {
                        float progress = ServerTickPhaseTracker.getWarmupProgress();
                        setRunStatus(String.format("ServerSync warmup %.0f%% (%d samples, %dms)", progress * 100.0F, ServerTickPhaseTracker.getSampleCount(), ServerTickPhaseTracker.warmupMillis()));
                        sleep(50L);
                     }

                     if (!isStillRunning()) {
                        return;
                     }

                     long optimalTime = ServerTickPhaseTracker.optimalSendTime(stsa.bufferMs, stsa.ignorePing);
                     long msUntil = (optimalTime - System.nanoTime()) / 1000000L;
                     int ping = ServerTickPhaseTracker.getLatencyMs();
                     String pingStr = stsa.ignorePing ? " (no ping)" : " ping:" + ping + "ms";
                     setRunStatus("ServerSync in " + Math.max(0L, msUntil) + "ms" + pingStr + " (" + preGenerated.size() + " pkts)");
                     long startWait = System.nanoTime();
                     long maxWaitNanos = stsa.maxWaitMs * 1000000L;

                     while (isStillRunning()) {
                        long now = System.nanoTime();
                        if (now - startWait >= maxWaitNanos || now >= optimalTime) {
                           break;
                        }

                        long remaining = optimalTime - now;
                        if (remaining > 2000000L) {
                           LockSupport.parkNanos(remaining - 2000000L);
                        } else {
                           Thread.onSpinWait();
                        }
                     }

                     if (isStillRunning()) {
                        sendQueuedPackets(mc, preGenerated);
                        MacroExecutor.ActiveStepSlice context = ACTIVE_STEP_SLICE.get();
                        if (context != null) {
                           SLICE_STEPS_CONSUMED.set(countActionsForPackets(context.macro(), context.nextIndex(), preGenerated.size(), context.endIndex()));
                        }

                        long actualDelay = (System.nanoTime() - optimalTime) / 1000000L;
                        AutismClientMessaging.sendPrefixed("ServerSync: " + preGenerated.size() + " pkts (+" + actualDelay + "ms)");
                     }
                  } else if (action instanceof BundleDupeV2Action bundleV2) {
                     setRunStatus("Bundle Dupe V2");
                     bundleV2.execute(mc);
                  } else if (action instanceof RollbackAction rollback) {
                     setRunStatus("Rollback");

                     try {
                        rollback.rollback(mc);
                     } catch (Exception e) {
                        AutismClientMessaging.sendPrefixed("§cRollback failed: " + describeError(e));
                     }
                  } else if (action instanceof StopMacroAction stopAction) {
                     if (stopAction.target == StopMacroAction.StopTarget.SELF) {
                        stopCurrentRunOrAll();
                     } else {
                        stopAction.execute(mc);
                     }
                  } else if (action instanceof UseItemPhaseAction phaseAction) {
                     runUsePhaseAction(mc, phaseAction);
                  } else if (action instanceof ContextMacroAction contextAction) {
                     runContextAction(contextAction);
                  } else if (isPacketOrdered(action)) {
                     executeGuarded(mc, action, action.getDisplayName());
                  } else if (module != null && module.isInstantExecutionMode()) {
                     CountDownLatch latch = new CountDownLatch(1);
                     MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
                     mc.execute(() -> runWithCurrentRun(scheduledRun, () -> {
                        try {
                           action.execute(mc);
                        } catch (Exception e) {
                           AutismClientMessaging.sendPrefixed("§cError in action: " + e.getMessage());
                        } finally {
                           latch.countDown();
                        }
                     }));
                     awaitLatch(latch, 100L);
                  } else {
                     executeGuarded(mc, action, action.getDisplayName());
                  }
               } else {
                  if (action instanceof DropAction dropAction) {
                     postGuiFuture = postGuiWaitFuture(mc, dropAction);
                  }

                  executeGuarded(mc, action, action.getDisplayName());
               }

               storeCaptureOutput(action, mc);
               if (postGuiFuture != null && isStillRunning()) {
                  try {
                     awaitWhileRunning(postGuiFuture);
                  } catch (CancellationException var95) {
                  }
               }

               if (isStillRunning() && !isNonInventoryAction(action) && module != null && module.isInstantExecutionMode()) {
                  int delayUs = module.getActionDelayUs();
                  if (delayUs > 0) {
                     LockSupport.parkNanos(delayUs * 1000L);
                  } else {
                     Thread.onSpinWait();
                  }
               }
            }
         } finally {
            MacroAimController.clear();
            releaseResourceLocks(heldResources);
         }
      }
   }

   private static void applyRaycastAim(Minecraft mc, MacroAction action) {
      if (action instanceof AimTargetAction aim && aim.isRaycast()) {
         RotationMath.Rotation rotation = callOnClientThread(mc, () -> MacroAimController.rotationTo(mc, aim.getAimTarget(mc)), null);
         if (rotation != null) {
            MacroAimController.setRotation(rotation);
            awaitNextTick();
         }
      }
   }

   private static boolean canResolveTemplates(MacroAction action, Minecraft mc) {
      if (action instanceof WaitForChatAction chat && chat.effectiveMatchMode() == PatternCaptureMatcher.MatchMode.MATCH) {
         return MacroVariableContext.resolveTemplate(chat.pattern, mc).success();
      } else if (action instanceof WaitForGuiAction gui && gui.matchMode == PatternCaptureMatcher.MatchMode.MATCH) {
         return MacroVariableContext.resolveTemplate(gui.guiTitle, mc).success();
      } else if (action instanceof WaitGuiTypeAction gui && gui.matchMode == PatternCaptureMatcher.MatchMode.MATCH) {
         return MacroVariableContext.resolveTemplate(gui.title, mc).success();
      } else if (action instanceof WaitInventoryPredicateAction inventory) {
         return MacroVariableContext.resolveTemplate(inventory.itemName, mc).success();
      } else if (action instanceof WaitDurabilityAction durability && durability.targetMode == WaitDurabilityAction.TargetMode.ITEM) {
         return MacroVariableContext.resolveTemplate(durability.itemName, mc).success();
      } else {
         return action instanceof WaitEntityTargetAction entity ? MacroVariableContext.resolveTemplate(entity.entityId, mc).success() : true;
      }
   }

   private static List<AutismSessionState.DelayedPacket> collectSlicePackets(int count, Minecraft mc) {
      MacroExecutor.ActiveStepSlice context = ACTIVE_STEP_SLICE.get();
      return context != null && context.macro() != null ? collectQueuedPackets(context.macro(), context.nextIndex(), count, context.endIndex()) : new ArrayList<>();
   }

   private static boolean isScreenOpeningAction(MacroAction action) {
      return action instanceof OpenContainerAction || action instanceof InteractEntityAction;
   }

   private static CompletableFuture<Void> waitForTargetFuture(Minecraft mc, MacroAction action) {
      boolean var10000;
      switch (action) {
         case OpenContainerAction open:
            var10000 = open.waitForTarget;
            break;
         case InteractEntityAction interact:
            var10000 = interact.waitForTarget;
            break;
         default:
            var10000 = false;
      }

      boolean waitForTarget = var10000;
      if (!waitForTarget) {
         CompletableFuture<Void> postGuiFuture = postGuiWaitFuture(mc, action);
         performInteraction(mc, action);
         return postGuiFuture;
      }

      setRunStatus("Waiting target: " + action.getDisplayName());

      while (isStillRunning()) {
         if (!isTargetAvailable(mc, action)) {
            LockSupport.parkNanos(500000L);
            if (Thread.interrupted()) {
               throw new RuntimeException();
            }
         } else {
            CompletableFuture<Void> postGuiFuture = postGuiWaitFuture(mc, action);
            if (performInteraction(mc, action)) {
               return postGuiFuture;
            }

            if (postGuiFuture != null) {
               postGuiFuture.cancel(true);
            }

            LockSupport.parkNanos(500000L);
            if (Thread.interrupted()) {
               throw new RuntimeException();
            }
         }
      }

      return null;
   }

   private static boolean isTargetAvailable(Minecraft mc, MacroAction action) {
      return callOnClientThread(mc, () -> {
         if (action instanceof OpenContainerAction open) {
            return open.open(mc);
         } else {
            return action instanceof InteractEntityAction interact ? interact.hasTargets(mc) : true;
         }
      }, Boolean.FALSE);
   }

   private static boolean performInteraction(Minecraft mc, MacroAction action) {
      return callOnClientThread(mc, () -> {
         try {
            if (action instanceof OpenContainerAction open) {
               return open.openTarget(mc);
            } else if (action instanceof InteractEntityAction interact) {
               return interact.interact(mc);
            } else {
               action.execute(mc);
               return true;
            }
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cError in " + action.getDisplayName() + ": " + e.getMessage());
            return true;
         }
      }, Boolean.FALSE, isPacketOrdered(action));
   }

   private static CompletableFuture<Void> postGuiWaitFuture(Minecraft mc, MacroAction action) {
      if (Boolean.TRUE.equals(SUPPRESS_POST_GUI_WAIT.get())) {
         return null;
      } else {
         return action instanceof GuiWaitAware wfg && wfg.waitsForGuiAfter() ? guiAfterWaitFuture(mc, wfg) : null;
      }
   }

   private static CompletableFuture<Void> guiAfterWaitFuture(Minecraft mc, GuiWaitAware wfg) {
      MacroTemplateResolver.ResolveResult guiName = MacroVariableContext.resolveTemplate(wfg.getGuiName(), mc);
      if (!guiName.success()) {
         return CompletableFuture.completedFuture(null);
      } else {
         return wfg.g_() ? MacroWaitRegistry.awaitScreenChange(mc.gui.screen()) : MacroWaitRegistry.awaitGuiTitle(guiName.value());
      }
   }

   private static boolean guiPreconditionMet(Minecraft mc, MacroAction action) {
      return action instanceof GuiWaitAware wfg ? checkGuiBefore(mc, wfg) : true;
   }

   private static boolean checkGuiBefore(Minecraft mc, GuiWaitAware action) {
      if (mc != null && action != null && action.waitsForGuiBefore()) {
         MacroTemplateResolver.ResolveResult guiName = MacroVariableContext.resolveTemplate(action.getGuiName(), mc);
         if (!guiName.success()) {
            return false;
         } else if (isGuiNamedOpen(mc, guiName.value())) {
            waitForScreenReady(mc);
            return true;
         } else {
            CompletableFuture<Void> future = MacroWaitRegistry.awaitGuiTitle(guiName.value());
            awaitWhileRunning(future);
            waitForScreenReady(mc);
            return true;
         }
      } else {
         return true;
      }
   }

   private static boolean isGuiNamedOpen(Minecraft mc, String guiName) {
      if (mc == null || mc.gui.screen() == null) {
         return false;
      }

      if (GuiScreenMatcher.isClientScreen(mc.gui.screen())) {
         return false;
      }

      String expected = guiName == null ? "" : guiName.trim();
      if (expected.isEmpty()) {
         return true;
      }

      String actual = mc.gui.screen().getTitle() == null ? "" : mc.gui.screen().getTitle().getString();
      return looseTextMatch(expected, actual);
   }

   private static boolean looseTextMatch(String expected, String actual) {
      if (expected != null && !expected.isBlank() && actual != null && !actual.isBlank()) {
         String expectedLower = expected.toLowerCase(Locale.ROOT);
         String actualLower = actual.toLowerCase(Locale.ROOT);
         if (!expectedLower.equals(actualLower) && !actualLower.contains(expectedLower)) {
            for (String word : expectedLower.split("\\s+")) {
               if (!word.isBlank() && !actualLower.contains(word)) {
                  return false;
               }
            }

            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static void waitForScreenReady(Minecraft mc) {
      long deadline = System.nanoTime() + 500000000L;

      while (isStillRunning()) {
         Boolean ready = callOnClientThread(
            mc,
            () -> mc.player != null && mc.gui.screen() != null
               ? mc.player.containerMenu != null && mc.player.containerMenu != mc.player.inventoryMenu ? mc.player.containerMenu.getStateId() > 0 : true
               : false,
            Boolean.FALSE
         );
         if (Boolean.TRUE.equals(ready)) {
            return;
         }

         if (System.nanoTime() >= deadline) {
            return;
         }

         LockSupport.parkNanos(500000L);
         if (Thread.interrupted()) {
            return;
         }
      }
   }

   private static void executeGuarded(Minecraft mc, MacroAction action, String label) {
      Runnable body = () -> {
         try {
            action.execute(mc);
         } catch (Throwable t) {
            reportAddonFailure(action, "Action threw " + t.getClass().getSimpleName());
            AutismClientMessaging.sendPrefixed("§cError in " + label + ": " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
         }
      };
      if (isPacketOrdered(action)) {
         runOnNextClientTick(mc, body);
      } else {
         runOnClientThread(mc, body);
      }
   }

   private static boolean isPacketOrdered(MacroAction action) {
      return action instanceof PacketOrderAware ordered && ordered.h_();
   }

   private static void runItemClickPlan(Minecraft mc, ItemAction ia) {
      setRunStatus(ia.getDisplayName());
      List<Runnable> plan = callOnClientThread(mc, () -> ia.buildClickPlan(mc), Collections.emptyList());
      if (plan != null && !plan.isEmpty()) {
         long delayMs = Math.max(0, ia.clickDelayMs);
         MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
         boolean first = true;

         for (Runnable op : plan) {
            if (!isStillRunning()) {
               break;
            }

            if (!first && delayMs > 0L) {
               sleep(delayMs);
            }

            first = false;
            mc.execute(() -> runWithCurrentRun(scheduledRun, () -> {
               if (!autismclient.modules.aD.read_Z()) {
                  try {
                     op.run();
                  } catch (Throwable t) {
                     AutismClientMessaging.sendPrefixed("§cError in ItemAction: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
                  }
               }
            }));
         }
      }
   }

   private static void runContextAction(ContextMacroAction action) {
      if (!autismclient.modules.aD.read_Z() && action != null) {
         try {
            action.run(new MacroExecutor.RunExecutionContext(CURRENT_RUN.get()));
         } catch (Throwable t) {
            reportAddonFailure(action, "Context action threw " + t.getClass().getSimpleName());
            AutismClientMessaging.sendPrefixed("§cAddon action error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
         }
      }
   }

   private static void reportAddonFailure(MacroAction action, String detail) {
      if (action != null) {
         String owner = MacroActionRegistry.ownerOf(action.getTypeId());
         if (owner != null && !owner.isBlank()) {
            autismclient.b.DataUtil.load_V(owner, detail);
         }
      }
   }

   private static void runUsePhaseAction(Minecraft mc, UseItemPhaseAction action) {
      int times = action.getRepeatCount();

      for (int i = 0; i < times && isStillRunning(); i++) {
         runOnClientThread(mc, () -> {
            try {
               action.usePhase(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Use phase: " + e.getMessage());
            }
         });
         boolean[] gateInstalled = new boolean[]{false};
         if (action.gatesDuringHold()) {
            runOnClientThread(mc, () -> {
               try {
                  gateInstalled[0] = action.installHoldGate(mc);
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cError in Use gate: " + e.getMessage());
               }
            });
         }

         try {
            for (int t = 0; t < action.holdTicks && isStillRunning(); t++) {
               CompletableFuture<Void> tf = MacroWaitRegistry.awaitTickSync();
               awaitWhileRunning(tf);
            }
         } finally {
            if (gateInstalled[0]) {
               boolean wasOnThread = mc.isSameThread();
               if (wasOnThread) {
                  action.removeHoldGate();
               } else {
                  runOnClientThread(mc, action::removeHoldGate);
               }
            }
         }

         if (!isStillRunning()) {
            break;
         }

         runOnClientThread(mc, () -> {
            try {
               action.finishRelease(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Use release: " + e.getMessage());
            }
         });
      }
   }

   private static void runClickAction(Minecraft mc, ClickAction action) {
      int times = Math.max(1, action.clickCount);
      runOnClientThread(mc, () -> {
         for (int c = 0; c < times && isStillRunning(); c++) {
            try {
               action.execute(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Click: " + e.getMessage());
               break;
            }
         }
      }, action.h_());
   }

   private static boolean isNonInventoryAction(MacroAction action) {
      return !(action instanceof ItemAction)
            && !(action instanceof DropAction)
            && !(action instanceof SwapSlotsAction)
            && !(action instanceof ClickAction)
            && !(action instanceof XCarryAction)
         ? action instanceof StoreItemAction storeItemAction && !storeItemAction.persistent
         : true;
   }

   private static void runStoreItemAction(Minecraft mc, StoreItemAction action) {
      if (action != null) {
         int delayTicks = StoreItemAction.clampDelayTicks(action.delayTicks);
         if (delayTicks <= 0) {
            runOnClientThread(mc, () -> action.storeItems(mc));
         } else {
            List<Integer> slots = callOnClientThread(mc, () -> action.slotsToStore(mc), List.of());

            for (int i = 0; i < slots.size() && isStillRunning(); i++) {
               autismclient.util.ContainerClickExecutor.click(mc, slots.get(i), 0, ContainerInput.QUICK_MOVE);
               if (i + 1 < slots.size()) {
                  waitClientTicks(delayTicks);
               }
            }

            if (action.closeAfter && !action.persistent) {
               runOnClientThread(mc, () -> autismclient.util.GuiDesyncHelper.init(mc, action.closeSendPkt, false));
            }
         }
      }
   }

   private static void waitClientTicks(int ticks) {
      for (int i = 0; i < ticks && isStillRunning(); i++) {
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
         awaitWhileRunning(tickFuture);
      }
   }

   private static void runUseItemAction(Minecraft mc, UseItemAction action) {
      if (action != null) {
         boolean sneaking = action.sneak;

         try {
            if (action.useMode == UseItemAction.UseMode.CUSTOM_HOLD) {
               runOnClientThread(mc, () -> {
                  try {
                     if (sneaking) {
                        autismclient.util.SneakPacketController.setSneak(mc, true);
                     }

                     action.execute(mc);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cError in UseItem: " + e.getMessage());
                  }
               }, action.h_());
               if (action.holdTicks <= 0) {
                  return;
               }

               waitClientTicks(action.holdTicks);
               if (isStillRunning()) {
                  runOnClientThread(mc, () -> {
                     try {
                        action.releaseUseItem(mc);
                     } catch (Exception e) {
                        AutismClientMessaging.sendPrefixed("§cError in UseItem release: " + e.getMessage());
                     }
                  }, action.h_());
                  if (action.waitForFinish) {
                     waitForItemUse(mc, action.holdTicks + 8);
                  }

                  return;
               }

               return;
            }

            int times = Math.max(1, action.useCount);

            for (int i = 0; i < times && isStillRunning(); i++) {
               runOnClientThread(mc, () -> {
                  try {
                     if (sneaking) {
                        autismclient.util.SneakPacketController.setSneak(mc, true);
                     }

                     action.execute(mc);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cError in UseItem: " + e.getMessage());
                  }
               }, action.h_());
               if (action.waitForFinish) {
                  waitForItemUse(mc, 0);
               }

               if (i + 1 < times && isStillRunning()) {
                  awaitNextTick();
               }
            }
         } finally {
            if (sneaking) {
               mc.execute(() -> autismclient.util.SneakPacketController.setSneak(mc, false));
            }
         }
      }
   }

   private static void runContainerClickSequence(Minecraft mc, ContainerClickSequenceAction action) {
      if (action != null) {
         List<Integer> slots = action.slots();
         int repeats = Math.max(1, action.repeatCount);
         int delay = Math.max(0, action.delayTicks);
         int total = repeats * slots.size();
         int sent = 0;

         for (int repeat = 0; repeat < repeats && isStillRunning(); repeat++) {
            for (int slot : slots) {
               if (!isStillRunning()) {
                  return;
               }

               runOnClientThread(mc, () -> action.clickSlot(mc, slot));
               if (delay > 0) {
                  if (++sent < total) {
                     waitClientTicks(delay);
                  }
               }
            }
         }
      }
   }

   private static void runPacketBurstAction(Minecraft mc, PacketBurstAction action) {
      if (action != null && mc != null && mc.getConnection() != null) {
         if (action.flushBefore) {
            AutismSessionState.getInstance().flushDelayedPackets(mc.getConnection());
         }

         int total = action.getClampedCount();
         int delay = Math.max(0, action.delayTicks);

         for (int i = 0; i < total && isStillRunning(); i++) {
            action.sendOnce(mc);
            if (delay > 0 && i + 1 < total) {
               waitClientTicks(delay);
            }
         }
      }
   }

   private static void waitForItemUse(Minecraft mc, int fallbackTicks) {
      if (mc != null && isStillRunning() && Boolean.TRUE.equals(callOnClientThread(mc, () -> mc.player != null && mc.player.isUsingItem(), Boolean.FALSE))) {
         int remaining = callOnClientThread(mc, () -> mc.player == null ? 0 : Math.max(0, mc.player.getUseItemRemainingTicks()), 0);
         int maxTicks = Math.max(8, remaining > 0 ? remaining + 8 : fallbackTicks + 8);
         maxTicks = Math.min(maxTicks, 72000);

         for (int i = 0; i < maxTicks && isStillRunning(); i++) {
            if (!Boolean.TRUE.equals(callOnClientThread(mc, () -> mc.player != null && mc.player.isUsingItem(), Boolean.FALSE))) {
               return;
            }

            CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
            awaitWhileRunning(tickFuture);
         }
      }
   }

   private static void runXCarryAction(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         if (xsa.usesCursor()) {
            setRunStatus("XCarry Cursor");
            runOnClientThread(mc, () -> xsa.execute(mc));
         } else {
            AutismSessionState sharedState = AutismSessionState.getInstance();
            boolean prevBypass = sharedState.isArmorSlotBypassEnabled();
            sharedState.setArmorSlotBypassEnabled(true);

            try {
               AtomicReference<AbstractContainerMenu> containerHandlerRef = new AtomicReference<>();
               AtomicReference<autismclient.util.InteractionTargetRef> containerTargetRef = new AtomicReference<>();
               AtomicReference<List<Integer>> containerSlotIdsRef = new AtomicReference<>(List.of());
               AtomicBoolean usingContainer = new AtomicBoolean(false);
               runOnClientThread(mc, () -> {
                  if (mc.player != null) {
                     AbstractContainerMenu current = mc.player.containerMenu;
                     if (current != null && current != mc.player.inventoryMenu) {
                        usingContainer.set(true);
                        containerHandlerRef.set(current);
                        containerTargetRef.set(AutismSessionState.getInstance().getLastInteractionTarget());
                        containerSlotIdsRef.set(new ArrayList<>(xsa.containerSlotIds(current, mc.player.inventoryMenu)));
                     }
                  }
               });
               if (usingContainer.get() && containerTargetRef.get() == null) {
                  AutismClientMessaging.sendPrefixed("XCarry click mode: missing container target, using fast mode.");
                  runOnClientThread(mc, () -> xsa.execute(mc));
                  return;
               }

               if (usingContainer.get()) {
                  for (int slotId : containerSlotIdsRef.get()) {
                     if (!isStillRunning()) {
                        return;
                     }

                     setRunStatus("XCarry Click: collect");
                     runOnClientThread(
                        mc,
                        () -> {
                           if (mc.player != null && mc.gameMode != null) {
                              AbstractContainerMenu handler = containerHandlerRef.get();
                              if (handler != null
                                 && mc.player.containerMenu == handler
                                 && slotId >= 0
                                 && slotId < handler.slots.size()
                                 && !handler.slots.get(slotId).getItem().isEmpty()) {
                                 mc.gameMode.handleContainerInput(handler.containerId, slotId, 0, ContainerInput.QUICK_MOVE, mc.player);
                              }
                           }
                        }
                     );
                     awaitNextTick();
                  }

                  runOnClientThread(mc, () -> {
                     if (mc.player != null && mc.getConnection() != null) {
                        AbstractContainerMenu handler = containerHandlerRef.get();
                        if (handler != null) {
                           mc.getConnection().send(new ServerboundContainerClosePacket(handler.containerId));
                        }
                     }
                  });
                  awaitNextTick();
                  runOnClientThread(mc, () -> {
                     if (mc.player != null) {
                        mc.player.containerMenu = mc.player.inventoryMenu;
                     }
                  });
                  awaitNextTick();
               }

               runOnClientThread(mc, xsa::enableCarry);
               int xCarryAttempts = 0;

               while (isStillRunning() && xCarryAttempts++ < 512) {
                  XCarryAction.SlotMove move = callOnClientThread(
                     mc, () -> mc.player == null ? null : xsa.nextMove(mc.player.inventoryMenu), null
                  );
                  if (move == null) {
                     break;
                  }

                  setRunStatus("XCarry Click -> " + move.targetSlot + " x" + move.count);
                  int targetBefore = callOnClientThread(mc, () -> {
                     if (mc.player == null) {
                        return 0;
                     }

                     InventoryMenu handler = mc.player.inventoryMenu;
                     return move.targetSlot >= 0 && move.targetSlot < handler.slots.size() ? handler.slots.get(move.targetSlot).getItem().getCount() : 0;
                  }, 0);
                  boolean moved = callOnClientThread(mc, () -> {
                     if (mc.player != null && mc.gameMode != null) {
                        InventoryMenu handler = mc.player.inventoryMenu;
                        mc.player.containerMenu = handler;
                        return XCarryAction.moveItems(handler, mc, move.sourceSlot, move.targetSlot, move.count);
                     } else {
                        return false;
                     }
                  }, Boolean.FALSE);
                  if (!moved) {
                     break;
                  }

                  boolean placed = waitUntilTrue(
                     mc,
                     500L,
                     () -> {
                        if (mc.player == null) {
                           return false;
                        }

                        InventoryMenu handler = mc.player.inventoryMenu;
                        return move.targetSlot >= 0 && move.targetSlot < handler.slots.size()
                           ? handler.getCarried().isEmpty() && handler.slots.get(move.targetSlot).getItem().getCount() >= targetBefore + move.count
                           : false;
                     }
                  );
                  if (!placed) {
                     putBackCarried(mc, move.sourceSlot);
                     break;
                  }

                  awaitNextTick();
               }

               runOnClientThread(mc, () -> {
                  if (mc.player != null) {
                     boolean active = XCarryAction.hasCarriedItemsIn(mc.player.inventoryMenu, xsa.carryCursor, xsa.carrySlotIds());
                     AutismSessionState shared = AutismSessionState.getInstance();
                     if (active) {
                        shared.mergeXCarrySlotMask(xsa.carrySlotIds(), xsa.carryCursor);
                     }

                     shared.setXCarryForced(active);
                     shared.setXCarryActive(active);
                  }
               });
               if (usingContainer.get()) {
                  runOnClientThread(mc, () -> {
                     if (mc.player != null) {
                        AbstractContainerMenu containerHandler = containerHandlerRef.get();
                        if (containerHandler != null) {
                           mc.player.containerMenu = containerHandler;
                        }

                        autismclient.util.InteractionTargetRef containerTarget = containerTargetRef.get();
                        if (containerTarget != null) {
                           XCarryAction.interactTarget(mc, containerTarget);
                        }
                     }
                  });
                  awaitNextTick();
               }

               return;
            } finally {
               sharedState.setArmorSlotBypassEnabled(prevBypass);
            }
         }
      }
   }

   private static void runXCarrySafeAction(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         if (xsa.usesCursor()) {
            setRunStatus("XCarry Safe: Cursor");
            runOnClientThread(mc, () -> xsa.execute(mc));
         } else {
            AutismSessionState sharedState = AutismSessionState.getInstance();
            boolean prevBypass = sharedState.isArmorSlotBypassEnabled();
            sharedState.setArmorSlotBypassEnabled(true);

            try {
               AtomicReference<AbstractContainerMenu> containerHandlerRef = new AtomicReference<>();
               AtomicReference<autismclient.util.InteractionTargetRef> containerTargetRef = new AtomicReference<>();
               AtomicReference<List<Integer>> containerSlotIdsRef = new AtomicReference<>(List.of());
               AtomicBoolean usingContainer = new AtomicBoolean(false);
               runOnClientThread(mc, () -> {
                  if (mc.player != null) {
                     AbstractContainerMenu current = mc.player.containerMenu;
                     if (current != null && current != mc.player.inventoryMenu) {
                        usingContainer.set(true);
                        containerHandlerRef.set(current);
                        containerTargetRef.set(AutismSessionState.getInstance().getLastInteractionTarget());
                        containerSlotIdsRef.set(new ArrayList<>(xsa.containerSlotIds(current, mc.player.inventoryMenu)));
                     }
                  }
               });
               if (usingContainer.get() && containerTargetRef.get() == null) {
                  AutismClientMessaging.sendPrefixed("XCarry safe mode: missing container target, using fast mode.");
                  runOnClientThread(mc, () -> xsa.execute(mc));
                  return;
               }

               if (usingContainer.get()) {
                  for (int slotId : containerSlotIdsRef.get()) {
                     if (!isStillRunning()) {
                        return;
                     }

                     setRunStatus("XCarry Safe: collect");
                     clickContainerSlot(mc, containerHandlerRef.get(), slotId, 0, ContainerInput.QUICK_MOVE);
                  }

                  runOnClientThread(mc, () -> {
                     if (mc.player != null && mc.getConnection() != null) {
                        AbstractContainerMenu handler = containerHandlerRef.get();
                        if (handler != null) {
                           mc.getConnection().send(new ServerboundContainerClosePacket(handler.containerId));
                        }
                     }
                  });
                  awaitNextTick();
                  runOnClientThread(mc, () -> {
                     if (mc.player != null) {
                        mc.player.containerMenu = mc.player.inventoryMenu;
                     }
                  });
                  awaitNextTick();
               }

               runOnClientThread(mc, xsa::enableCarry);
               int safeDelayTicks = XCarryAction.clampDelay(xsa.safeClickDelayTicks);
               boolean delayAfterPickup = xsa.safeClickDelayAfterPickup;
               boolean delayBeforeReturn = xsa.safeClickDelayBeforeReturn;
               int attempts = 0;

               while (isStillRunning() && attempts++ < 512) {
                  List<XCarryAction.SlotMove> group = callOnClientThread(mc, () -> planXCarryMoves(mc, xsa), List.of());
                  if (group.isEmpty()) {
                     break;
                  }

                  XCarryAction.SlotMove first = group.get(0);
                  int total = 0;

                  for (XCarryAction.SlotMove move : group) {
                     total += Math.max(0, move.amount());
                  }

                  setRunStatus("XCarry Safe -> " + group.size() + " slots x" + total);
                  if (!transferGroup(mc, group, safeDelayTicks, delayAfterPickup, delayBeforeReturn)) {
                     returnCarried(mc, first.source(), safeDelayTicks, delayBeforeReturn);
                     break;
                  }
               }

               runOnClientThread(mc, () -> {
                  if (mc.player != null) {
                     boolean active = XCarryAction.hasCarriedItemsIn(mc.player.inventoryMenu, xsa.carryCursor, xsa.carrySlotIds());
                     AutismSessionState shared = AutismSessionState.getInstance();
                     if (active) {
                        shared.mergeXCarrySlotMask(xsa.carrySlotIds(), xsa.carryCursor);
                     }

                     shared.setXCarryForced(active);
                     shared.setXCarryActive(active);
                  }
               });
               if (usingContainer.get()) {
                  runOnClientThread(mc, () -> {
                     if (mc.player != null) {
                        AbstractContainerMenu containerHandler = containerHandlerRef.get();
                        if (containerHandler != null) {
                           mc.player.containerMenu = containerHandler;
                        }

                        autismclient.util.InteractionTargetRef containerTarget = containerTargetRef.get();
                        if (containerTarget != null) {
                           XCarryAction.interactTarget(mc, containerTarget);
                        }
                     }
                  });
                  awaitNextTick();
               }

               return;
            } finally {
               sharedState.setArmorSlotBypassEnabled(prevBypass);
            }
         }
      }
   }

   private static List<XCarryAction.SlotMove> planXCarryMoves(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && xsa != null) {
         InventoryMenu handler = mc.player.inventoryMenu;
         List<XCarryAction.SlotMove> planned = xsa.planMoves(handler);
         if (planned.isEmpty()) {
            return List.of();
         }

         XCarryAction.SlotMove first = planned.get(0);
         if (first == null) {
            return List.of();
         }

         if (first.source() >= 0 && first.source() < handler.slots.size()) {
            ItemStack source = handler.slots.get(first.source()).getItem();
            if (source.isEmpty()) {
               return List.of();
            }

            ArrayList<XCarryAction.SlotMove> group = new ArrayList<>();
            int remainingSource = source.getCount();

            for (XCarryAction.SlotMove move : planned) {
               if (move == null
                  || move.source() != first.source()
                  || move.amount() <= 0
                  || move.amount() > remainingSource
                  || !slotMoveFits(handler, source, move)) {
                  break;
               }

               group.add(move);
               remainingSource -= move.amount();
            }

            return group;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static boolean transferGroup(Minecraft mc, List<XCarryAction.SlotMove> group, int delayTicks, boolean delayAfterPickup, boolean delayBeforeReturn) {
      if (mc != null && mc.player != null && mc.gameMode != null && group != null && !group.isEmpty()) {
         MacroExecutor.TransferPlan snapshot = callOnClientThread(
            mc, () -> captureTransferPlan(mc, group), MacroExecutor.TransferPlan.invalid()
         );
         if (!snapshot.valid) {
            return false;
         }

         MacroExecutor.TransferPlan expected = expectedTransfer(mc, group, snapshot, delayTicks, delayAfterPickup, delayBeforeReturn);
         return expected.valid;
      } else {
         return false;
      }
   }

   private static MacroExecutor.TransferPlan expectedTransfer(
      Minecraft mc,
      List<XCarryAction.SlotMove> group,
      MacroExecutor.TransferPlan snapshot,
      int delayTicks,
      boolean delayAfterPickup,
      boolean delayBeforeReturn
   ) {
      if (mc != null && mc.player != null && mc.gameMode != null && group != null && !group.isEmpty() && snapshot != null && snapshot.valid) {
         XCarryAction.SlotMove first = group.get(0);
         if (!Boolean.TRUE.equals(callOnClientThread(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && handler.getCarried().isEmpty() && matchesSnapshot(handler, group, snapshot);
         }, Boolean.FALSE))) {
            return MacroExecutor.TransferPlan.invalid();
         }

         MacroExecutor.QuickCraftPlan quickCraftPlan = callOnClientThread(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return planQuickCraft(handler, group, snapshot);
         }, MacroExecutor.QuickCraftPlan.invalid());
         MacroExecutor.TransferPlan expected = snapshot;
         if (quickCraftPlan.valid) {
            setRunStatus("XCarry Safe drag -> " + group.size() + " slots x" + quickCraftPlan.placeCount);
            int start = AbstractContainerMenu.getQuickcraftMask(0, quickCraftPlan.quickCraftType);
            int add = AbstractContainerMenu.getQuickcraftMask(1, quickCraftPlan.quickCraftType);
            int end = AbstractContainerMenu.getQuickcraftMask(2, quickCraftPlan.quickCraftType);
            int nextDragIndex = 0;
            if (delayAfterPickup) {
               if (!clickInventorySlot(mc, first.source(), 0, ContainerInput.PICKUP)) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               delayTicksIf(delayTicks, true);
               if (!Boolean.TRUE.equals(callOnClientThread(mc, () -> {
                  InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
                  return isCarrying(handler, snapshot.sourceStack, snapshot.sourceCount);
               }, Boolean.FALSE))) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               if (!clickInventorySlot(mc, -999, start, ContainerInput.QUICK_CRAFT)) {
                  return MacroExecutor.TransferPlan.invalid();
               }
            } else {
               ArrayList<MacroExecutor.DragClickStep> dragClicks = new ArrayList<>(group.size() + 3);
               dragClicks.add(new MacroExecutor.DragClickStep(first.source(), 0, ContainerInput.PICKUP));
               dragClicks.add(new MacroExecutor.DragClickStep(-999, start, ContainerInput.QUICK_CRAFT));

               for (XCarryAction.SlotMove move : group) {
                  if (move != null) {
                     dragClicks.add(new MacroExecutor.DragClickStep(move.target(), add, ContainerInput.QUICK_CRAFT));
                  }
               }

               dragClicks.add(new MacroExecutor.DragClickStep(-999, end, ContainerInput.QUICK_CRAFT));
               if (!clickInventorySlots(mc, dragClicks.toArray(MacroExecutor.DragClickStep[]::new))) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               nextDragIndex = group.size();
            }

            for (int i = nextDragIndex; i < group.size(); i++) {
               XCarryAction.SlotMove move = group.get(i);
               if (i > nextDragIndex || nextDragIndex > 0) {
                  waitXCarryTicks(delayTicks);
               }

               if (!clickInventorySlot(mc, move.target(), add, ContainerInput.QUICK_CRAFT)) {
                  return MacroExecutor.TransferPlan.invalid();
               }
            }

            if (nextDragIndex < group.size() && !clickInventorySlot(mc, -999, end, ContainerInput.QUICK_CRAFT)) {
               return MacroExecutor.TransferPlan.invalid();
            }

            boolean firstResidual = true;

            for (Entry<Integer, Integer> residual : quickCraftPlan.residualCounts.entrySet()) {
               int targetSlotId = residual.getKey();
               int count = residual.getValue() == null ? 0 : residual.getValue();
               if (count > 0) {
                  if (!firstResidual) {
                     waitXCarryTicks(delayTicks);
                  }

                  firstResidual = false;
                  if (!dropCarriedInto(mc, targetSlotId, count, delayTicks)) {
                     return MacroExecutor.TransferPlan.invalid();
                  }
               }
            }

            expected = snapshot.withExpectedTargetCounts(quickCraftPlan.expectedTargetCounts);
         } else {
            int nextMoveIndex = 0;
            if (delayAfterPickup) {
               if (!clickInventorySlot(mc, first.source(), 0, ContainerInput.PICKUP)) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               delayTicksIf(delayTicks, true);
            } else {
               XCarryAction.SlotMove firstMove = group.get(0);
               int firstButton = firstMove.amount() >= snapshot.sourceCount ? 0 : 1;
               if (!clickInventorySlots(
                  mc,
                  new MacroExecutor.DragClickStep(first.source(), 0, ContainerInput.PICKUP),
                  new MacroExecutor.DragClickStep(firstMove.target(), firstButton, ContainerInput.PICKUP)
               )) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               int remainingFirst = firstButton == 0 ? 0 : Math.max(0, firstMove.amount() - 1);
               if (remainingFirst > 0) {
                  waitXCarryTicks(delayTicks);
                  if (!dropCarriedInto(mc, firstMove.target(), remainingFirst, delayTicks)) {
                     return MacroExecutor.TransferPlan.invalid();
                  }
               }

               nextMoveIndex = 1;
            }

            for (int i = nextMoveIndex; i < group.size(); i++) {
               XCarryAction.SlotMove move = group.get(i);
               if (move != null && move.amount() > 0 && !dropCarriedInto(mc, move.target(), move.amount(), delayTicks)) {
                  return MacroExecutor.TransferPlan.invalid();
               }
            }
         }

         boolean hasLeftover = Boolean.TRUE.equals(callOnClientThread(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && !handler.getCarried().isEmpty();
         }, Boolean.FALSE));
         if (hasLeftover) {
            if (delayBeforeReturn) {
               waitXCarryTicks(delayTicks);
            }

            int returnSlot = callOnClientThread(mc, () -> findReturnSlot(mc, first.source()), -1);
            if (returnSlot < 0) {
               return MacroExecutor.TransferPlan.invalid();
            }

            if (!clickInventorySlot(mc, returnSlot, 0, ContainerInput.PICKUP)) {
               return MacroExecutor.TransferPlan.invalid();
            }
         }

         boolean cursorEmpty = Boolean.TRUE.equals(callOnClientThread(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && handler.getCarried().isEmpty();
         }, Boolean.FALSE));
         return cursorEmpty ? expected : MacroExecutor.TransferPlan.invalid();
      } else {
         return MacroExecutor.TransferPlan.invalid();
      }
   }

   private static boolean dropCarriedInto(Minecraft mc, int targetSlotId, int count, int delayTicks) {
      if (mc != null && count > 0) {
         int remaining = count;

         while (remaining > 0 && isStillRunning()) {
            int carriedCount = callOnClientThread(mc, () -> {
               InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
               return handler == null ? 0 : handler.getCarried().getCount();
            }, 0);
            if (carriedCount <= 0) {
               return false;
            }

            int button = remaining >= carriedCount ? 0 : 1;
            if (!clickInventorySlot(mc, targetSlotId, button, ContainerInput.PICKUP)) {
               return false;
            }

            if (button == 0) {
               remaining = 0;
            } else {
               remaining--;
            }

            if (remaining > 0) {
               waitXCarryTicks(delayTicks);
            }
         }

         return remaining <= 0;
      } else {
         return false;
      }
   }

   private static MacroExecutor.TransferPlan captureTransferPlan(Minecraft mc, List<XCarryAction.SlotMove> group) {
      if (mc != null && mc.player != null && group != null && !group.isEmpty()) {
         InventoryMenu handler = mc.player.inventoryMenu;
         if (handler != null && handler.getCarried().isEmpty()) {
            XCarryAction.SlotMove first = group.get(0);
            if (first.source() >= 0 && first.source() < handler.slots.size()) {
               Slot sourceSlot = handler.slots.get(first.source());
               ItemStack source = sourceSlot.getItem();
               if (source.isEmpty()) {
                  return MacroExecutor.TransferPlan.invalid();
               }

               int total = 0;
               LinkedHashMap<Integer, Integer> startCounts = new LinkedHashMap<>();
               LinkedHashMap<Integer, Integer> expectedCounts = new LinkedHashMap<>();

               for (XCarryAction.SlotMove move : group) {
                  if (move == null || move.source() != first.source()) {
                     return MacroExecutor.TransferPlan.invalid();
                  }

                  if (!slotMoveFits(handler, source, move)) {
                     return MacroExecutor.TransferPlan.invalid();
                  }

                  int start = startCounts.computeIfAbsent(move.target(), slotId -> menuSlotCount(handler, slotId));
                  if (start < 0) {
                     return MacroExecutor.TransferPlan.invalid();
                  }

                  expectedCounts.put(move.target(), expectedCounts.getOrDefault(move.target(), start) + move.amount());
                  total += move.amount();
               }

               return total > 0 && total <= source.getCount()
                  ? new MacroExecutor.TransferPlan(true, source.copy(), source.getCount(), startCounts, expectedCounts)
                  : MacroExecutor.TransferPlan.invalid();
            } else {
               return MacroExecutor.TransferPlan.invalid();
            }
         } else {
            return MacroExecutor.TransferPlan.invalid();
         }
      } else {
         return MacroExecutor.TransferPlan.invalid();
      }
   }

   private static boolean slotMoveFits(InventoryMenu handler, ItemStack source, XCarryAction.SlotMove move) {
      if (handler != null && source != null && !source.isEmpty() && move != null && move.amount() > 0) {
         if (move.target() < 0 || move.target() >= handler.slots.size()) {
            return false;
         }

         if (move.source() == move.target()) {
            return false;
         }

         Slot targetSlot = handler.slots.get(move.target());
         if (targetSlot != null && targetSlot.mayPlace(source)) {
            ItemStack target = targetSlot.getItem();
            if (!target.isEmpty() && !ItemStack.isSameItemSameComponents(target, source)) {
               return false;
            }

            int current = target.isEmpty() ? 0 : target.getCount();
            int max = Math.min(source.getMaxStackSize(), targetSlot.getMaxStackSize(source));
            return current + move.amount() <= max;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isCarrying(InventoryMenu handler, ItemStack expected, int count) {
      if (handler != null && expected != null && !expected.isEmpty()) {
         ItemStack carried = handler.getCarried();
         return !carried.isEmpty() && carried.getCount() == count && ItemStack.isSameItemSameComponents(carried, expected);
      } else {
         return false;
      }
   }

   private static boolean matchesSnapshot(InventoryMenu handler, List<XCarryAction.SlotMove> group, MacroExecutor.TransferPlan snapshot) {
      if (handler != null && group != null && snapshot != null && snapshot.valid) {
         XCarryAction.SlotMove first = group.isEmpty() ? null : group.get(0);
         if (first != null && first.source() >= 0 && first.source() < handler.slots.size()) {
            ItemStack source = handler.slots.get(first.source()).getItem();
            if (!source.isEmpty() && source.getCount() == snapshot.sourceCount && ItemStack.isSameItemSameComponents(source, snapshot.sourceStack)) {
               for (XCarryAction.SlotMove move : group) {
                  if (move != null && move.source() == first.source()) {
                     int current = menuSlotCount(handler, move.target());
                     Integer expectedStart = snapshot.startTargetCounts.get(move.target());
                     if (expectedStart != null && current == expectedStart) {
                        if (!slotMoveFits(handler, source, move)) {
                           return false;
                        }
                        continue;
                     }

                     return false;
                  }

                  return false;
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static MacroExecutor.QuickCraftPlan planQuickCraft(
      InventoryMenu handler, List<XCarryAction.SlotMove> group, MacroExecutor.TransferPlan snapshot
   ) {
      if (handler != null && group != null && group.size() >= 2 && snapshot != null && snapshot.valid) {
         LinkedHashMap<Integer, Integer> desiredAdds = new LinkedHashMap<>();

         for (XCarryAction.SlotMove move : group) {
            if (move != null && move.amount() > 0) {
               if (move.target() >= 0 && move.target() < handler.slots.size()) {
                  Slot targetSlot = handler.slots.get(move.target());
                  if (!canQuickCraftInto(handler, targetSlot, snapshot.sourceStack)) {
                     return MacroExecutor.QuickCraftPlan.invalid();
                  }

                  int start = snapshot.startTargetCounts.getOrDefault(move.target(), menuSlotCount(handler, move.target()));
                  int finalForMove = snapshot.expectedTargetCounts.getOrDefault(move.target(), start);
                  int desiredAdd = finalForMove - start;
                  if (desiredAdd <= 0) {
                     return MacroExecutor.QuickCraftPlan.invalid();
                  }

                  int max = Math.min(snapshot.sourceStack.getMaxStackSize(), targetSlot.getMaxStackSize(snapshot.sourceStack));
                  if (start + desiredAdd > max) {
                     return MacroExecutor.QuickCraftPlan.invalid();
                  }

                  desiredAdds.put(move.target(), desiredAdd);
                  continue;
               }

               return MacroExecutor.QuickCraftPlan.invalid();
            }

            return MacroExecutor.QuickCraftPlan.invalid();
         }

         MacroExecutor.QuickCraftPlan best = MacroExecutor.QuickCraftPlan.invalid();
         MacroExecutor.QuickCraftPlan even = planQuickCraft(handler, snapshot, desiredAdds, 0);
         MacroExecutor.QuickCraftPlan rightDrag = planQuickCraft(handler, snapshot, desiredAdds, 1);
         if (even.valid) {
            best = even;
         }

         if (rightDrag.valid && (!best.valid || rightDrag.cost < best.cost)) {
            best = rightDrag;
         }

         return best;
      } else {
         return MacroExecutor.QuickCraftPlan.invalid();
      }
   }

   private static MacroExecutor.QuickCraftPlan planQuickCraft(
      InventoryMenu handler, MacroExecutor.TransferPlan snapshot, LinkedHashMap<Integer, Integer> desiredAdds, int quickCraftType
   ) {
      if (handler != null && snapshot != null && snapshot.valid && desiredAdds != null && desiredAdds.size() >= 2) {
         int slotCount = desiredAdds.size();
         int var10000;
         switch (quickCraftType) {
            case 0:
               var10000 = snapshot.sourceCount / slotCount;
               break;
            case 1:
               var10000 = 1;
               break;
            default:
               var10000 = 0;
         }

         int placeCount = var10000;
         if (placeCount > 0 && snapshot.sourceCount >= placeCount * slotCount) {
            LinkedHashMap<Integer, Integer> residual = new LinkedHashMap<>();
            LinkedHashMap<Integer, Integer> expected = new LinkedHashMap<>();
            int residualTotal = 0;

            for (Entry<Integer, Integer> entry : desiredAdds.entrySet()) {
               int targetSlotId = entry.getKey();
               int desiredAdd = entry.getValue();
               if (desiredAdd < placeCount) {
                  return MacroExecutor.QuickCraftPlan.invalid();
               }

               Slot targetSlot = targetSlotId >= 0 && targetSlotId < handler.slots.size() ? handler.slots.get(targetSlotId) : null;
               if (!canQuickCraftInto(handler, targetSlot, snapshot.sourceStack)) {
                  return MacroExecutor.QuickCraftPlan.invalid();
               }

               int start = snapshot.startTargetCounts.getOrDefault(targetSlotId, menuSlotCount(handler, targetSlotId));
               int max = Math.min(snapshot.sourceStack.getMaxStackSize(), targetSlot.getMaxStackSize(snapshot.sourceStack));
               if (start + placeCount > max) {
                  return MacroExecutor.QuickCraftPlan.invalid();
               }

               int leftover = desiredAdd - placeCount;
               if (leftover > 0) {
                  residual.put(targetSlotId, leftover);
                  residualTotal += leftover;
               }

               expected.put(targetSlotId, start + desiredAdd);
            }

            int carriedAfterQuickCraft = snapshot.sourceCount - placeCount * slotCount;
            if (residualTotal > carriedAfterQuickCraft) {
               return MacroExecutor.QuickCraftPlan.invalid();
            }

            int cost = slotCount + 2 + residualTotal;
            return new MacroExecutor.QuickCraftPlan(true, quickCraftType, placeCount, cost, residual, expected);
         } else {
            return MacroExecutor.QuickCraftPlan.invalid();
         }
      } else {
         return MacroExecutor.QuickCraftPlan.invalid();
      }
   }

   private static boolean canQuickCraftInto(InventoryMenu handler, Slot targetSlot, ItemStack sourceStack) {
      if (handler == null || targetSlot == null || sourceStack == null || sourceStack.isEmpty()) {
         return false;
      } else if (targetSlot.index == 0) {
         return false;
      } else {
         return targetSlot.mayPlace(sourceStack) && handler.canDragTo(targetSlot)
            ? AbstractContainerMenu.canItemQuickReplace(targetSlot, sourceStack, true)
            : false;
      }
   }

   private static boolean clickInventorySlot(Minecraft mc, int slotId, int button, ContainerInput input) {
      return clickInventorySlots(mc, new MacroExecutor.DragClickStep(slotId, button, input));
   }

   private static boolean clickInventorySlots(Minecraft mc, MacroExecutor.DragClickStep... clicks) {
      return mc != null && clicks != null && clicks.length != 0 ? Boolean.TRUE.equals(callOnClientThread(mc, () -> {
         if (mc.player != null && mc.gameMode != null) {
            InventoryMenu handler = mc.player.inventoryMenu;
            if (handler == null) {
               return false;
            }

            mc.player.containerMenu = handler;

            for (MacroExecutor.DragClickStep click : clicks) {
               if (click == null || click.input == null) {
                  return false;
               }

               if (click.slotId != -999 && (click.slotId < 0 || click.slotId >= handler.slots.size())) {
                  return false;
               }

               mc.gameMode.handleContainerInput(handler.containerId, click.slotId, click.button, click.input, mc.player);
            }

            return true;
         } else {
            return false;
         }
      }, Boolean.FALSE)) : false;
   }

   private static void waitXCarryTicks(int delayTicks) {
      int ticks = XCarryAction.clampDelay(delayTicks);

      for (int i = 0; i < ticks && isStillRunning(); i++) {
         awaitNextTick();
      }
   }

   private static void delayTicksIf(int delayTicks, boolean enabled) {
      if (enabled) {
         waitXCarryTicks(delayTicks);
      }
   }

   private static boolean clickSlot(Minecraft mc, int slotId, int button, ContainerInput input) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         boolean clicked = callOnClientThread(mc, () -> {
            if (mc.player != null && mc.gameMode != null) {
               InventoryMenu handler = mc.player.inventoryMenu;
               mc.player.containerMenu = handler;
               if (slotId >= 0 && slotId < handler.slots.size()) {
                  mc.gameMode.handleContainerInput(handler.containerId, slotId, button, input, mc.player);
                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }, Boolean.FALSE);
         if (!clicked) {
            return false;
         }

         awaitNextTick();
         return true;
      } else {
         return false;
      }
   }

   private static boolean clickContainerSlot(Minecraft mc, AbstractContainerMenu handler, int slotId, int button, ContainerInput input) {
      if (mc != null && handler != null && input != null) {
         boolean clicked = callOnClientThread(mc, () -> {
            if (mc.player == null || mc.gameMode == null || mc.player.containerMenu != handler) {
               return false;
            }

            if (slotId < 0 || slotId >= handler.slots.size()) {
               return false;
            }

            if (handler.slots.get(slotId).getItem().isEmpty()) {
               return false;
            }

            mc.gameMode.handleContainerInput(handler.containerId, slotId, button, input, mc.player);
            return true;
         }, Boolean.FALSE);
         if (!clicked) {
            return false;
         }

         awaitNextTick();
         return true;
      } else {
         return false;
      }
   }

   private static boolean waitTicksFor(Minecraft mc, int ticks, BooleanSupplier accepted) {
      int maxTicks = Math.max(1, ticks);

      for (int i = 0; i < maxTicks && isStillRunning(); i++) {
         if (Boolean.TRUE.equals(callOnClientThread(mc, () -> accepted != null && accepted.getAsBoolean(), Boolean.FALSE))) {
            return true;
         }

         awaitNextTick();
      }

      return Boolean.TRUE.equals(callOnClientThread(mc, () -> accepted != null && accepted.getAsBoolean(), Boolean.FALSE));
   }

   private static void waitForInventoryRevision(Minecraft mc, long startRevision, int minTicks, long maxMs) {
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(50L, maxMs));
      int ticks = Math.max(1, minTicks);

      for (int i = 0; i < ticks && isStillRunning(); i++) {
         awaitNextTick();
      }

      while (isStillRunning() && System.nanoTime() < deadline) {
         long revision = MacroWaitRegistry.getInventoryRevision();
         if (revision != startRevision) {
            awaitNextTick();
            return;
         }

         awaitNextTick();
      }
   }

   private static boolean awaitTransferApplied(Minecraft mc, MacroExecutor.TransferPlan snapshot, long startRevision) {
      waitForInventoryRevision(mc, startRevision, 2, 250L);
      if (Boolean.TRUE.equals(callOnClientThread(mc, () -> isTransferApplied(mc, snapshot), Boolean.FALSE))) {
         return true;
      }

      waitForInventoryRevision(mc, MacroWaitRegistry.getInventoryRevision(), 3, 350L);
      return Boolean.TRUE.equals(callOnClientThread(mc, () -> isTransferApplied(mc, snapshot), Boolean.FALSE));
   }

   private static boolean isTransferApplied(Minecraft mc, MacroExecutor.TransferPlan snapshot) {
      if (mc != null && mc.player != null && snapshot != null && snapshot.valid) {
         InventoryMenu handler = mc.player.inventoryMenu;
         if (handler != null && handler.getCarried().isEmpty()) {
            for (Entry<Integer, Integer> entry : snapshot.expectedTargetCounts.entrySet()) {
               int slotId = entry.getKey();
               if (slotId >= 0 && slotId < handler.slots.size()) {
                  ItemStack stack = handler.slots.get(slotId).getItem();
                  if (entry.getValue() <= 0) {
                     if (!stack.isEmpty()) {
                        return false;
                     }
                     continue;
                  }

                  if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, snapshot.sourceStack)) {
                     if (stack.getCount() != entry.getValue()) {
                        return false;
                     }
                     continue;
                  }

                  return false;
               }

               return false;
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean unusedReturnCarried(Minecraft mc, int preferredSlotId, ItemStack expected, int delayTicks, boolean delayBeforeReturn) {
      if (mc != null && mc.player != null && expected != null && !expected.isEmpty()) {
         if (callOnClientThread(mc, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty(), Boolean.FALSE)) {
            return true;
         }

         int returnSlot = callOnClientThread(mc, () -> findReturnSlot(mc, preferredSlotId), -1);
         if (returnSlot < 0) {
            return false;
         }

         if (delayBeforeReturn) {
            waitXCarryTicks(delayTicks);
         }

         return clickSlot(mc, returnSlot, 0, ContainerInput.PICKUP)
            && waitTicksFor(mc, 3, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
      } else {
         return false;
      }
   }

   private static int findReturnSlot(Minecraft mc, int preferredSlotId) {
      if (mc != null && mc.player != null) {
         InventoryMenu handler = mc.player.inventoryMenu;
         ItemStack carried = handler.getCarried();
         if (carried.isEmpty()) {
            return -1;
         }

         if (canAcceptCarried(handler, preferredSlotId, carried)) {
            return preferredSlotId;
         }

         for (Slot slot : handler.slots) {
            if (slot != null && slot.index >= 9 && slot.index != 45 && canAcceptCarried(handler, slot.index, carried)) {
               return slot.index;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static boolean canAcceptCarried(InventoryMenu handler, int slotId, ItemStack carried) {
      if (handler == null || carried == null || carried.isEmpty()) {
         return false;
      }

      if (slotId >= 0 && slotId < handler.slots.size()) {
         Slot slot = handler.slots.get(slotId);
         if (slot != null && slot.mayPlace(carried)) {
            ItemStack stack = slot.getItem();
            return stack.isEmpty()
               ? carried.getCount() <= slot.getMaxStackSize(carried)
               : ItemStack.isSameItemSameComponents(stack, carried)
                  && stack.getCount() + carried.getCount() <= Math.min(carried.getMaxStackSize(), slot.getMaxStackSize(carried));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int playerSlotCount(Minecraft mc, int slotId) {
      return mc != null && mc.player != null ? menuSlotCount(mc.player.inventoryMenu, slotId) : -1;
   }

   private static int menuSlotCount(InventoryMenu handler, int slotId) {
      if (handler == null) {
         return -1;
      } else if (slotId >= 0 && slotId < handler.slots.size()) {
         ItemStack stack = handler.slots.get(slotId).getItem();
         return stack.isEmpty() ? 0 : stack.getCount();
      } else {
         return -1;
      }
   }

   private static void returnCarried(Minecraft mc, int sourceSlotId, int delayTicks, boolean delayBeforeReturn) {
      if (mc != null
         && mc.player != null
         && mc.gameMode != null
         && !callOnClientThread(mc, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty(), Boolean.FALSE)) {
         int returnSlot = callOnClientThread(mc, () -> findReturnSlot(mc, sourceSlotId), -1);
         if (returnSlot >= 0) {
            if (delayBeforeReturn) {
               waitXCarryTicks(delayTicks);
            }

            clickSlot(mc, returnSlot, 0, ContainerInput.PICKUP);
            waitTicksFor(mc, 3, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
         } else {
            putBackCarried(mc, sourceSlotId);
         }
      }
   }

   private static void putBackCarried(Minecraft mc, int sourceSlotId) {
      runOnClientThread(mc, () -> {
         if (mc.player != null && mc.gameMode != null) {
            InventoryMenu handler = mc.player.inventoryMenu;
            mc.player.containerMenu = handler;
            if (!handler.getCarried().isEmpty() && sourceSlotId >= 0 && sourceSlotId < handler.slots.size()) {
               mc.gameMode.handleContainerInput(handler.containerId, sourceSlotId, 0, ContainerInput.PICKUP, mc.player);
            }
         }
      });
      waitUntilTrue(mc, 300L, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
   }

   private static boolean waitUntilTrue(Minecraft mc, long timeoutMs, Supplier<Boolean> condition) {
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);

      while (isStillRunning()) {
         if (Boolean.TRUE.equals(callOnClientThread(mc, condition, Boolean.FALSE))) {
            return true;
         }

         if (System.nanoTime() >= deadline) {
            break;
         }

         awaitNextTick();
      }

      return Boolean.TRUE.equals(callOnClientThread(mc, condition, Boolean.FALSE));
   }

   private static void awaitNextTick() {
      CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
      awaitWhileRunning(tickFuture);
   }

   private static void runInstaBreakAction(Minecraft mc, InstaBreakAction action) {
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null && mc.gameMode != null) {
         BlockPos target = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
         Direction direction = action.direction == null ? Direction.UP : action.direction;
         int delayTicks = Math.max(0, action.delayTicks);
         int targetTimes = Math.max(0, action.times);
         int completed = 0;
         boolean wasSolid = !isBlockGone(mc, target);
         if (ensurePickaxe(mc, action)) {
            autismclient.util.BlockPreviewRenderer.show(target);

            try {
               long lastAttemptTick = AutismSessionState.getInstance().getClientTick() - (delayTicks + 1L);
               long lastSneakTick = -9223372036854775808L;

               while (isStillRunning() && isTopLevelAction(action)) {
                  if (action.sneak) {
                     long sneakTick = AutismSessionState.getInstance().getClientTick();
                     if (sneakTick != lastSneakTick) {
                        lastSneakTick = sneakTick;
                        String sneakMode = action.sneakMode;
                        runOnClientThread(mc, () -> startSneak(mc, sneakMode), action.h_());
                     }
                  }

                  boolean isAir = isBlockGone(mc, target);
                  if (wasSolid && isAir) {
                     setRunStatus("InstaBreak " + ++completed + "/" + (targetTimes == 0 ? "∞" : targetTimes));
                     if (targetTimes > 0 && completed >= targetTimes) {
                        return;
                     }

                     wasSolid = false;
                  } else if (!isAir) {
                     wasSolid = true;
                  }

                  if (!isAir) {
                     long currentTick = AutismSessionState.getInstance().getClientTick();
                     if (currentTick != lastAttemptTick && currentTick - lastAttemptTick >= delayTicks) {
                        lastAttemptTick = currentTick;
                        if (!ensurePickaxe(mc, action)) {
                           return;
                        }

                        if (action.interact) {
                           InteractTimingMode t = action.interactTiming;
                           boolean interactFirst = t == InteractTimingMode.BEFORE || t == InteractTimingMode.WITH || t == InteractTimingMode.CUSTOM && action.interactCustomMs < 0;
                           long ns = t == InteractTimingMode.CUSTOM ? Math.abs((long)action.interactCustomMs) * 1000000L : 0L;
                           if (interactFirst) {
                              runOnClientThread(mc, () -> useItemOnBlock(mc, target, direction), action.h_());
                              if (ns > 0L) {
                                 LockSupport.parkNanos(ns);
                              }

                              sendBreakPackets(mc, target, direction, action.h_());
                           } else if (t == InteractTimingMode.AFTER_PLUS) {
                              sendBreakPackets(mc, target, direction, action.h_());
                              awaitNextTick();
                              runOnClientThread(mc, () -> useItemOnBlock(mc, target, direction), action.h_());
                           } else {
                              sendBreakPackets(mc, target, direction, action.h_());
                              if (ns > 0L) {
                                 LockSupport.parkNanos(ns);
                              }

                              runOnClientThread(mc, () -> useItemOnBlock(mc, target, direction), action.h_());
                           }
                        } else {
                           sendBreakPackets(mc, target, direction, action.h_());
                        }
                     }
                  }

                  setRunStatus(
                     "InstaBreak " + target.getX() + "," + target.getY() + "," + target.getZ() + " " + completed + "/" + (targetTimes == 0 ? "∞" : targetTimes)
                  );
                  sleep(1L);
               }

               return;
            } finally {
               if (action.sneak) {
                  String sneakMode = action.sneakMode;
                  runOnClientThread(mc, () -> stopSneak(mc, sneakMode));
               }

               autismclient.util.BlockPreviewRenderer.clearIfAt(target);
            }
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cInstaBreak: missing world or connection");
      }
   }

   private static boolean isBlockGone(Minecraft mc, BlockPos pos) {
      return mc.level == null || mc.level.isOutsideBuildHeight(pos) || mc.level.getBlockState(pos).isAir();
   }

   private static boolean isTopLevelAction(MacroAction action) {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();
      return run != null && run.macro != null && run.macro.actions != null ? run.macro.actions.contains(action) : true;
   }

   private static boolean ensurePickaxe(Minecraft mc, InstaBreakAction action) {
      return callOnClientThread(mc, () -> {
         if (mc.player == null) {
            return false;
         }

         if (mc.player.getMainHandItem().is(ItemTags.PICKAXES)) {
            return true;
         }

         if (!action.autoPickaxe) {
            return false;
         }

         int preferred = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));

         for (int slot = 0; slot < 9; slot++) {
            if (mc.player.getInventory().getItem(slot).is(ItemTags.PICKAXES)) {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
               return true;
            }
         }

         for (int slot = 9; slot < 36; slot++) {
            if (mc.player.getInventory().getItem(slot).is(ItemTags.PICKAXES)) {
               if (!autismclient.util.InventorySlotHelper.swapPlayerSlots(mc, slot, preferred)) {
                  return false;
               }

               autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, preferred);
               return true;
            }
         }

         return false;
      }, false, action.h_());
   }

   private static void sendBreakPackets(Minecraft mc, BlockPos pos, Direction direction, boolean tickAligned) {
      runOnClientThread(mc, () -> {
         if (mc.player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null) {
            BlockPos blockPos = pos.immutable();
            Direction dir = direction == null ? Direction.UP : direction;
            if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
               accessor.autism$startPrediction(mc.level, sequence -> new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, blockPos, dir, sequence));
               accessor.autism$startPrediction(mc.level, sequence -> new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, dir, sequence));
            } else {
               mc.getConnection().send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, blockPos, dir));
               mc.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, dir));
            }

            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
         }
      }, tickAligned);
   }

   private static void runBreakAction(Minecraft mc, BreakAction action) {
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null && mc.gameMode != null) {
         BlockPos target = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
         Direction direction = action.direction == null ? Direction.UP : action.direction;
         int startDelay = Math.max(0, action.delayTicks);
         int targetTimes = Math.max(0, action.times);
         int completed = 0;
         if (startDelay > 0) {
            sleepClientTicks(startDelay);
         }

         boolean[] started = new boolean[]{false};
         boolean useNextSteps = action.interact && action.runNextSteps;
         boolean[] nextStepsFired = new boolean[]{false};
         boolean[] missingNextStepsWarned = new boolean[]{false};
         autismclient.util.BlockPreviewRenderer.show(target);
         long lastTick = -1L;

         while (true) {
            try {
               if (!isStillRunning() || !isTopLevelAction(action)) {
                  return;
               }

               long now = AutismSessionState.getInstance().getClientTick();
               if (now != lastTick) {
                  lastTick = now;
                  int result;
                  if (!started[0]) {
                     result = useNextSteps
                        ? breakStartStep(mc, action, target, direction, started, nextStepsFired, missingNextStepsWarned)
                        : callOnClientThread(mc, () -> breakStartTick(mc, action, target, direction, started), -1, action.h_());
                  } else if (action.interact && action.interactTiming == InteractTimingMode.CUSTOM) {
                     result = useNextSteps ? customTimingBreakStep(mc, action, target, direction, nextStepsFired, missingNextStepsWarned) : customTimingBreakTick(mc, action, target, direction);
                  } else {
                     result = useNextSteps
                        ? breakProgressStep(mc, action, target, direction, nextStepsFired, missingNextStepsWarned)
                        : callOnClientThread(mc, () -> breakProgressTick(mc, action, target, direction), -1, action.h_());
                  }

                  if (result < 0) {
                     return;
                  }

                  if (result != 1) {
                     setRunStatus(
                        "Break " + target.getX() + "," + target.getY() + "," + target.getZ() + " " + completed + "/" + (targetTimes == 0 ? "∞" : targetTimes)
                     );
                  } else {
                     boolean completedStartedBreak = started[0];
                     if (useNextSteps && completedStartedBreak) {
                        fireInteractNextSteps(mc, action, nextStepsFired, missingNextStepsWarned);
                     } else if (action.interact && action.interactTiming == InteractTimingMode.AFTER_PLUS) {
                        awaitNextTick();
                        runOnClientThread(mc, () -> useItemOnBlock(mc, target, direction), action.h_());
                     }

                     setRunStatus("Break " + ++completed + "/" + (targetTimes == 0 ? "∞" : targetTimes));
                     if (targetTimes > 0 && completed >= targetTimes) {
                        return;
                     }

                     started[0] = false;
                     nextStepsFired[0] = false;
                     waitForBlockPresent(mc, target);
                  }
               } else {
                  sleep(1L);
               }
            } finally {
               boolean wasMining = started[0];
               runOnClientThread(mc, () -> {
                  if (wasMining && mc.gameMode != null && mc.gameMode.isDestroying()) {
                     mc.gameMode.stopDestroyBlock();
                  }

                  if (action.sneak) {
                     stopSneak(mc, action.sneakMode);
                  }
               }, action.h_());
               autismclient.util.BlockPreviewRenderer.clearIfAt(target);
            }
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cBreak: missing world or connection");
      }
   }

   private static int breakStartStep(
      Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started, boolean[] nextStepsFired, boolean[] missingNextStepsWarned
   ) {
      MacroExecutor.SlotMatchFlags plan = callOnClientThread(
         mc, () -> planBreakStart(mc, action, pos), new MacroExecutor.SlotMatchFlags(-1, false, false)
      );
      if (plan.status() != 0) {
         return plan.status();
      }

      if (plan.instant() && plan.load()) {
         fireNextActions(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!isStillRunning()) {
            return -1;
         }
      }

      return callOnClientThread(mc, () -> startBreaking(mc, action, pos, dir, started), -1, action.h_());
   }

   private static MacroExecutor.SlotMatchFlags planBreakStart(Minecraft mc, BreakAction action, BlockPos pos) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return new MacroExecutor.SlotMatchFlags(-1, false, false);
      }

      if (mc.level.isOutsideBuildHeight(pos)) {
         return new MacroExecutor.SlotMatchFlags(1, false, false);
      }

      BlockState state = mc.level.getBlockState(pos);
      if (state.isAir()) {
         return new MacroExecutor.SlotMatchFlags(1, false, false);
      }

      if (action.sneak) {
         startSneak(mc, action.sneakMode);
      }

      if (action.autoTool) {
         autismclient.util.BlockBreakSpeedHelper.switchToBestTool(mc, state, action.considerInventory, true);
         state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return new MacroExecutor.SlotMatchFlags(1, false, false);
         }
      }

      float startDelta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
      boolean instant = startDelta >= 1.0F;
      boolean interactFirst = action.interact
         && (
            action.interactTiming == InteractTimingMode.BEFORE
               || action.interactTiming == InteractTimingMode.WITH
               || action.interactTiming == InteractTimingMode.CUSTOM && action.interactCustomMs < 0
         );
      return new MacroExecutor.SlotMatchFlags(0, instant, interactFirst);
   }

   private static int startBreaking(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return -1;
      }

      if (!mc.level.isOutsideBuildHeight(pos) && !mc.level.getBlockState(pos).isAir()) {
         if (action.sneak) {
            startSneak(mc, action.sneakMode);
         }

         mc.gameMode.startDestroyBlock(pos, dir);
         autismclient.util.BlockAttackGraceWindow.note(mc.player);
         Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
         started[0] = true;
         return mc.level.getBlockState(pos).isAir() ? 1 : 0;
      } else {
         return 1;
      }
   }

   private static int breakStartTick(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (!state.isAir() && !mc.level.isOutsideBuildHeight(pos)) {
            if (action.sneak) {
               startSneak(mc, action.sneakMode);
            }

            if (action.autoTool) {
               autismclient.util.BlockBreakSpeedHelper.switchToBestTool(mc, state, action.considerInventory, true);
               state = mc.level.getBlockState(pos);
            }

            float startDelta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
            boolean instant = startDelta >= 1.0F;
            boolean interactFirst = action.interact
               && (
                  action.interactTiming == InteractTimingMode.BEFORE
                     || action.interactTiming == InteractTimingMode.WITH
                     || action.interactTiming == InteractTimingMode.CUSTOM && action.interactCustomMs < 0
               );
            if (instant && interactFirst) {
               useItemOnBlock(mc, pos, dir);
            }

            mc.gameMode.startDestroyBlock(pos, dir);
            autismclient.util.BlockAttackGraceWindow.note(mc.player);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            started[0] = true;
            if (instant && action.interact && !interactFirst && action.interactTiming != InteractTimingMode.AFTER_PLUS) {
               useItemOnBlock(mc, pos, dir);
            }

            return mc.level.getBlockState(pos).isAir() ? 1 : 0;
         } else {
            return 1;
         }
      } else {
         return -1;
      }
   }

   private static int breakProgressStep(
      Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] nextStepsFired, boolean[] missingNextStepsWarned
   ) {
      MacroExecutor.StepMatchFlags plan = callOnClientThread(
         mc, () -> planBreakProgress(mc, action, pos), new MacroExecutor.StepMatchFlags(-1, false, false)
      );
      if (plan.status() != 0) {
         return plan.status();
      }

      InteractTimingMode timing = action.interactTiming;
      if (timing == InteractTimingMode.BEFORE && plan.load()) {
         fireNextActions(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!isStillRunning()) {
            return -1;
         }
      }

      if (timing == InteractTimingMode.WITH && plan.breaksThisTick()) {
         fireNextActions(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!isStillRunning()) {
            return -1;
         }
      }

      return callOnClientThread(mc, () -> progressBreaking(mc, action, pos, dir), -1, action.h_());
   }

   private static MacroExecutor.StepMatchFlags planBreakProgress(Minecraft mc, BreakAction action, BlockPos pos) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return new MacroExecutor.StepMatchFlags(1, false, false);
         }

         if (action.sneak) {
            startSneak(mc, action.sneakMode);
         }

         boolean breaksThisTick = completesThisTick(mc, pos, state);
         boolean breaksNextTick = !breaksThisTick && completesNextTick(mc, pos, state);
         return new MacroExecutor.StepMatchFlags(0, breaksThisTick, breaksNextTick);
      } else {
         return new MacroExecutor.StepMatchFlags(-1, false, false);
      }
   }

   private static int progressBreaking(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return -1;
      }

      if (mc.level.getBlockState(pos).isAir()) {
         return 1;
      }

      if (action.sneak) {
         startSneak(mc, action.sneakMode);
      }

      mc.gameMode.continueDestroyBlock(pos, dir);
      autismclient.util.BlockAttackGraceWindow.note(mc.player);
      Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
      return mc.level.getBlockState(pos).isAir() ? 1 : 0;
   }

   private static int breakProgressTick(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return 1;
         }

         if (action.sneak) {
            startSneak(mc, action.sneakMode);
         }

         boolean breaksThisTick = completesThisTick(mc, pos, state);
         boolean breaksNextTick = !breaksThisTick && completesNextTick(mc, pos, state);
         InteractTimingMode t = action.interactTiming;
         if (action.interact && t == InteractTimingMode.BEFORE && breaksNextTick) {
            useItemOnBlock(mc, pos, dir);
         }

         if (action.interact && t == InteractTimingMode.WITH && breaksThisTick) {
            useItemOnBlock(mc, pos, dir);
         }

         mc.gameMode.continueDestroyBlock(pos, dir);
         autismclient.util.BlockAttackGraceWindow.note(mc.player);
         Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
         if (action.interact && t == InteractTimingMode.AFTER && breaksThisTick) {
            useItemOnBlock(mc, pos, dir);
         }

         return mc.level.getBlockState(pos).isAir() ? 1 : 0;
      } else {
         return -1;
      }
   }

   private static int customTimingBreakStep(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] nextStepsFired, boolean[] missingNextStepsWarned) {
      boolean willBreak = Boolean.TRUE.equals(callOnClientThread(mc, () -> {
         if (mc.level != null && mc.player != null) {
            BlockState state = mc.level.getBlockState(pos);
            return state.isAir() ? Boolean.FALSE : completesThisTick(mc, pos, state);
         } else {
            return Boolean.FALSE;
         }
      }, Boolean.FALSE));
      if (!willBreak) {
         return callOnClientThread(mc, () -> progressBreaking(mc, action, pos, dir), -1, action.h_());
      }

      long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
      int broke;
      if (action.interactCustomMs < 0) {
         fireNextActions(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!isStillRunning()) {
            return -1;
         }

         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         broke = callOnClientThread(mc, () -> continueBreaking(mc, pos, dir), -1, action.h_());
      } else {
         broke = callOnClientThread(mc, () -> continueBreaking(mc, pos, dir), -1, action.h_());
      }

      return broke;
   }

   private static int customTimingBreakTick(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      boolean willBreak = Boolean.TRUE.equals(callOnClientThread(mc, () -> {
         if (mc.level != null && mc.player != null) {
            BlockState s = mc.level.getBlockState(pos);
            return s.isAir() ? Boolean.FALSE : completesThisTick(mc, pos, s);
         } else {
            return Boolean.FALSE;
         }
      }, Boolean.FALSE));
      if (!willBreak) {
         return callOnClientThread(mc, () -> {
            if (mc.level == null || mc.gameMode == null || mc.player == null) {
               return -1;
            }

            if (mc.level.getBlockState(pos).isAir()) {
               return 1;
            }

            if (action.sneak) {
               startSneak(mc, action.sneakMode);
            }

            mc.gameMode.continueDestroyBlock(pos, dir);
            autismclient.util.BlockAttackGraceWindow.note(mc.player);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            return mc.level.getBlockState(pos).isAir() ? 1 : 0;
         }, -1, action.h_());
      }

      long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
      int broke;
      if (action.interactCustomMs < 0) {
         runOnClientThread(mc, () -> useItemOnBlock(mc, pos, dir), action.h_());
         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         broke = callOnClientThread(mc, () -> continueBreaking(mc, pos, dir), -1, action.h_());
      } else {
         broke = callOnClientThread(mc, () -> continueBreaking(mc, pos, dir), -1, action.h_());
         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         runOnClientThread(mc, () -> useItemOnBlock(mc, pos, dir), action.h_());
      }

      return broke;
   }

   private static int continueBreaking(Minecraft mc, BlockPos pos, Direction dir) {
      if (mc.level == null || mc.gameMode == null || mc.player == null) {
         return -1;
      }

      if (mc.level.getBlockState(pos).isAir()) {
         return 1;
      }

      mc.gameMode.continueDestroyBlock(pos, dir);
      autismclient.util.BlockAttackGraceWindow.note(mc.player);
      Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
      return mc.level.getBlockState(pos).isAir() ? 1 : 0;
   }

   private static boolean completesThisTick(Minecraft mc, BlockPos pos, BlockState state) {
      float progress = mc.gameMode instanceof AutismMultiPlayerGameModeAccessor acc ? acc.autism$getDestroyProgress() : 0.0F;
      float delta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
      return progress > 0.0F && progress + delta >= 1.0F;
   }

   private static boolean completesNextTick(Minecraft mc, BlockPos pos, BlockState state) {
      float progress = mc.gameMode instanceof AutismMultiPlayerGameModeAccessor acc ? acc.autism$getDestroyProgress() : 0.0F;
      float delta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
      return progress > 0.0F && progress + 2.0F * delta >= 1.0F;
   }

   private static void fireNextActions(Minecraft mc, BreakAction action, boolean[] fired, boolean[] missingWarned) {
      if (fired == null || !fired[0]) {
         List<MacroAction> actions = action.getNextActions();
         if (actions != null && !actions.isEmpty()) {
            if (fired != null) {
               fired[0] = true;
            }

            action.markNextActionsFired();
            String names = actions.size() == 1 ? actions.get(0).getDisplayName() : actions.size() + " steps";
            setRunStatus("Break: next " + names);
            autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
            boolean previousSuppressPostGui = Boolean.TRUE.equals(SUPPRESS_POST_GUI_WAIT.get());

            try {
               SUPPRESS_POST_GUI_WAIT.set(true);

               for (MacroAction next : actions) {
                  if (next != null && next.isEnabled() && isStillRunning()) {
                     try {
                        executeMacroAction(mc, next, module);
                     } catch (Throwable t) {
                        AutismClientMessaging.sendPrefixed("§cBreak next step failed (" + next.getDisplayName() + "): " + describeError(t));
                     }
                  }
               }
            } finally {
               SUPPRESS_POST_GUI_WAIT.set(previousSuppressPostGui);
            }
         } else if (missingWarned != null && !missingWarned[0]) {
            missingWarned[0] = true;
            AutismClientMessaging.sendPrefixed("§cBreak: no next steps found. Add action(s) after this Break step.");
         }
      }
   }

   private static void fireInteractNextSteps(Minecraft mc, BreakAction action, boolean[] fired, boolean[] missingWarned) {
      if (action.interact && action.runNextSteps && action.interactTiming != null) {
         switch (action.interactTiming) {
            case AFTER:
               fireNextActions(mc, action, fired, missingWarned);
               break;
            case AFTER_PLUS:
               awaitNextTick();
               fireNextActions(mc, action, fired, missingWarned);
               break;
            case CUSTOM:
               if (action.interactCustomMs >= 0) {
                  long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  fireNextActions(mc, action, fired, missingWarned);
               }
         }
      }
   }

   private static void useItemOnBlock(Minecraft mc, BlockPos pos, Direction dir) {
      if (mc.gameMode != null && mc.player != null) {
         Vec3 hit = Vec3.atCenterOf(pos).add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
         BlockHitResult blockHit = new BlockHitResult(hit, dir, pos, false);
         mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
      }
   }

   private static void waitForBlockPresent(Minecraft mc, BlockPos pos) {
      while (isStillRunning()) {
         if (!isBlockGone(mc, pos)) {
            return;
         }

         sleep(1L);
      }
   }

   private static void sleepClientTicks(int ticks) {
      long start = AutismSessionState.getInstance().getClientTick();

      while (isStillRunning() && AutismSessionState.getInstance().getClientTick() - start < ticks) {
         sleep(1L);
      }
   }

   private static void startSneak(Minecraft mc, String mode) {
      autismclient.util.SneakPacketController.press(mc, mode);
   }

   private static void stopSneak(Minecraft mc, String mode) {
      autismclient.util.SneakPacketController.release(mc, mode);
      if (!autismclient.util.SneakPacketController.isPacketMode(mode)) {
         syncSneakKey(mc);
      }
   }

   private static void runOnClientThread(Minecraft mc, Runnable runnable) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
         Runnable contextual = () -> runWithCurrentRun(scheduledRun, runnable);
         if (mc.isSameThread()) {
            if (!autismclient.modules.aD.read_Z()) {
               contextual.run();
            }
         } else {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean pending = new AtomicBoolean(true);
            mc.execute(() -> {
               try {
                  if (!pending.compareAndSet(true, false)) {
                     return;
                  }

                  if (!autismclient.modules.aD.read_Z()) {
                     contextual.run();
                     return;
                  }
               } finally {
                  latch.countDown();
               }
            });
            if (!awaitLatch(latch, 2000L)) {
               pending.set(false);
            }
         }
      }
   }

   private static void runOnClientThread(Minecraft mc, Runnable runnable, boolean tickAligned) {
      if (tickAligned) {
         runOnNextClientTick(mc, runnable);
      } else {
         runOnClientThread(mc, runnable);
      }
   }

   private static void postToClientThread(Minecraft mc, Runnable runnable, boolean tickAligned) {
      if (tickAligned) {
         runOnNextClientTick(mc, runnable);
      } else if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
         mc.execute(() -> {
            if (!autismclient.modules.aD.read_Z()) {
               runWithCurrentRun(scheduledRun, runnable);
            }
         });
      }
   }

   private static void runOnNextClientTick(Minecraft mc, Runnable runnable) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
         Runnable contextual = () -> runWithCurrentRun(scheduledRun, runnable);
         if (mc.isSameThread()) {
            if (!autismclient.modules.aD.read_Z()) {
               contextual.run();
            }
         } else {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean pending = new AtomicBoolean(true);
            TICK_ALIGNED_TASKS.add(() -> {
               try {
                  if (!pending.compareAndSet(true, false)) {
                     return;
                  }

                  if (!autismclient.modules.aD.read_Z()) {
                     contextual.run();
                     return;
                  }
               } finally {
                  latch.countDown();
               }
            });
            if (!awaitLatch(latch, 2000L)) {
               pending.set(false);
            }
         }
      }
   }

   public static void onClientTick() {
      if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
         Runnable task;
         while ((task = TICK_ALIGNED_TASKS.poll()) != null) {
            try {
               task.run();
            } catch (Throwable t) {
               AutismClientAddon.LOG.debug("[Autism] Tick-aligned macro task failed", t);
            }
         }

         tickNavigationOwner();
      }
   }

   private static <T> T callOnClientThread(Minecraft mc, Supplier<T> supplier, T fallback) {
      if (autismclient.modules.aD.read_Z()) {
         return fallback;
      }

      MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
      Supplier<T> contextual = () -> {
         AtomicReference<T> value = new AtomicReference<>(fallback);
         runWithCurrentRun(scheduledRun, () -> value.set(supplier.get()));
         return value.get();
      };
      if (mc.isSameThread()) {
         return autismclient.modules.aD.read_Z() ? fallback : contextual.get();
      }

      AtomicReference<T> result = new AtomicReference<>(fallback);
      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean pending = new AtomicBoolean(true);
      mc.execute(() -> {
         try {
            if (!pending.compareAndSet(true, false)) {
               return;
            }

            if (!autismclient.modules.aD.read_Z()) {
               result.set(contextual.get());
               return;
            }
         } finally {
            latch.countDown();
         }
      });
      if (!awaitLatch(latch, 2000L)) {
         pending.set(false);
      }

      return result.get();
   }

   private static <T> T callOnClientThread(Minecraft mc, Supplier<T> supplier, T fallback, boolean tickAligned) {
      return tickAligned ? callOnNextClientTick(mc, supplier, fallback) : callOnClientThread(mc, supplier, fallback);
   }

   static <T> T callOnNextClientTick(Minecraft mc, Supplier<T> supplier, T fallback) {
      if (autismclient.modules.aD.read_Z()) {
         return fallback;
      }

      MacroExecutor.MacroRun scheduledRun = CURRENT_RUN.get();
      Supplier<T> contextual = () -> {
         AtomicReference<T> value = new AtomicReference<>(fallback);
         runWithCurrentRun(scheduledRun, () -> value.set(supplier.get()));
         return value.get();
      };
      if (mc.isSameThread()) {
         return autismclient.modules.aD.read_Z() ? fallback : contextual.get();
      }

      AtomicReference<T> result = new AtomicReference<>(fallback);
      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean pending = new AtomicBoolean(true);
      TICK_ALIGNED_TASKS.add(() -> {
         try {
            if (!pending.compareAndSet(true, false)) {
               return;
            }

            if (!autismclient.modules.aD.read_Z()) {
               result.set(contextual.get());
               return;
            }
         } finally {
            latch.countDown();
         }
      });
      if (!awaitLatch(latch, 2000L)) {
         pending.set(false);
      }

      return result.get();
   }

   private static void runPlaceAction(Minecraft mc, PlaceAction action) {
      if (mc != null && mc.player != null && mc.level != null) {
         MacroItemTarget target = action.resolveItemTarget().resolveTemplates(mc);
         if (target != null) {
            if (action.waitForItem && target != null && (target.hasItemFilter() || target.hasSlot())) {
               setRunStatus("Waiting for " + describeTarget(target));
               waitForItemTarget(mc, target);
            }

            if (!action.interact) {
               executeGuarded(mc, action, "Place");
            } else {
               BlockPos placePos = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
               Direction face = action.manualDirection && action.direction != null ? action.direction : Direction.UP;
               InteractTimingMode t = action.interactTiming;
               boolean interactFirst = t == InteractTimingMode.BEFORE || t == InteractTimingMode.CUSTOM && action.interactCustomMs < 0;
               long ns = t == InteractTimingMode.CUSTOM ? Math.abs((long)action.interactCustomMs) * 1000000L : 0L;
               if (interactFirst) {
                  runOnClientThread(mc, () -> useItemOnBlock(mc, placePos, face), action.h_());
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  executeGuarded(mc, action, "Place");
               } else if (t == InteractTimingMode.WITH) {
                  runOnClientThread(mc, () -> {
                     try {
                        action.execute(mc);
                     } catch (Exception e) {
                        AutismClientAddon.LOG.debug("[Autism] Macro WITH-interact action failed", e);
                     }

                     useItemOnBlock(mc, placePos, face);
                  }, action.h_());
               } else if (t == InteractTimingMode.AFTER_PLUS) {
                  executeGuarded(mc, action, "Place");
                  awaitNextTick();
                  runOnClientThread(mc, () -> useItemOnBlock(mc, placePos, face), action.h_());
               } else {
                  executeGuarded(mc, action, "Place");
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  runOnClientThread(mc, () -> useItemOnBlock(mc, placePos, face), action.h_());
               }
            }
         }
      }
   }

   private static void waitForSlotItem(Minecraft mc, int slotId, MacroItemTarget target) {
      while (isStillRunning()) {
         if (Boolean.TRUE.equals(callOnClientThread(mc, () -> slotMatchesTarget(mc, slotId, target), Boolean.FALSE))) {
            return;
         }

         awaitNextTick();
      }
   }

   private static void waitForSlotOccupied(Minecraft mc, int slotId) {
      while (isStillRunning()) {
         if (Boolean.TRUE.equals(callOnClientThread(mc, () -> isSlotOccupied(mc, slotId), Boolean.FALSE))) {
            return;
         }

         awaitNextTick();
      }
   }

   private static boolean slotMatchesTarget(Minecraft mc, int slotId, MacroItemTarget target) {
      if (mc.player != null && mc.player.containerMenu != null) {
         int handlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, slotId);
         if (handlerSlot >= 0 && handlerSlot < mc.player.containerMenu.slots.size()) {
            Slot slot = mc.player.containerMenu.slots.get(handlerSlot);
            if (slot != null && !slot.getItem().isEmpty()) {
               MacroItemTarget resolvedTarget = target == null ? MacroItemTarget.ofSlot(slotId) : target.copy();
               if (!resolvedTarget.hasSlot()) {
                  resolvedTarget.slot = slotId;
               }

               return resolvedTarget.matches(slot.getItem(), slotId);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isSlotOccupied(Minecraft mc, int slotId) {
      if (mc.player != null && mc.player.containerMenu != null) {
         int handlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, slotId);
         if (handlerSlot >= 0 && handlerSlot < mc.player.containerMenu.slots.size()) {
            Slot slot = mc.player.containerMenu.slots.get(handlerSlot);
            return slot != null && !slot.getItem().isEmpty();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void waitForItemTargets(Minecraft mc, ItemAction ia) {
      if (ia != null) {
         List<MacroItemTarget> targets = resolveItemTargets(mc, ia.itemTargets, ia.itemNames);
         if (targets.isEmpty()) {
            if (ia.useSlot && ia.targetSlot >= 0) {
               setRunStatus("Waiting for slot " + ia.targetSlot);
               waitForSlotOccupied(mc, ia.targetSlot);
            }
         } else {
            for (MacroItemTarget target : targets) {
               waitForItemTarget(mc, target);
            }
         }
      }
   }

   private static void waitForItemTarget(Minecraft mc, MacroItemTarget target) {
      MacroItemTarget resolvedTarget = target == null ? null : target.resolveTemplates(mc);
      if (resolvedTarget != null) {
         String itemLabel = describeTarget(resolvedTarget);
         int slot = resolvedTarget.hasSlot() ? resolvedTarget.slot : -1;
         if (slot >= 0 && resolvedTarget.hasItemFilter()) {
            setRunStatus("Waiting for " + itemLabel);
            waitForSlotItem(mc, slot, resolvedTarget);
         } else if (slot >= 0) {
            setRunStatus("Waiting for slot " + slot);
            waitForSlotOccupied(mc, slot);
         } else if (resolvedTarget.hasItemFilter()) {
            setRunStatus("Waiting for item in GUI: " + itemLabel);
            CompletableFuture<Void> waitFuture = MacroWaitRegistry.itemWait(resolvedTarget);
            awaitWhileRunning(waitFuture);
         }
      }
   }

   private static MacroItemTarget targetOrLegacy(MacroItemTarget target, String legacyEntry) {
      return target != null && (target.hasSlot() || target.hasItemFilter()) ? target.copy() : MacroItemTarget.parse(legacyEntry);
   }

   private static List<MacroItemTarget> resolveItemTargets(Minecraft mc, List<MacroItemTarget> targets, List<String> legacyEntries) {
      List<MacroItemTarget> resolved = new ArrayList<>();
      if (targets != null) {
         for (MacroItemTarget target : targets) {
            if (target != null && (target.hasSlot() || target.hasItemFilter())) {
               MacroItemTarget runtimeTarget = target.resolveTemplates(mc);
               if (runtimeTarget != null) {
                  resolved.add(runtimeTarget);
               }
            }
         }
      }

      if (!resolved.isEmpty()) {
         return resolved;
      }

      if (legacyEntries == null) {
         return resolved;
      }

      for (String entry : legacyEntries) {
         MacroItemTarget parsed = MacroItemTarget.parse(entry);
         MacroItemTarget runtimeTarget = parsed.resolveTemplates(mc);
         if (runtimeTarget != null && (runtimeTarget.hasSlot() || runtimeTarget.hasItemFilter())) {
            resolved.add(runtimeTarget);
         }
      }

      return resolved;
   }

   private static String describeTarget(MacroItemTarget target) {
      if (target == null) {
         return "";
      }

      String summary = target.formatSummary();
      if (summary != null && !summary.isBlank()) {
         return summary;
      }

      String legacy = target.toSpecString();
      return legacy == null ? "" : legacy;
   }

   private static void runCraftAction(Minecraft mc, CraftAction craftAction) {
      if (craftAction != null && craftAction.hasValidEntry()) {
         for (int index = 0; index < craftAction.entries.size(); index++) {
            CraftAction.CraftEntry entry = craftAction.entries.get(index);
            if (entry != null && entry.isValid()) {
               setRunStatus(
                  entry.useMaxAmount
                     ? "Crafting " + entry.resultName + " [Max] (" + (index + 1) + "/" + craftAction.entries.size() + ")"
                     : "Crafting " + entry.resultName + " x" + Math.max(1, entry.amount) + " (" + (index + 1) + "/" + craftAction.entries.size() + ")"
               );
               autismclient.util.AutoCraftingEngine.CraftOption option = autismclient.util.AutoCraftingEngine.findOptionByKey(mc, entry.recipeKey);
               if (option == null) {
                  option = autismclient.util.AutoCraftingEngine.findOptionById(mc, entry.recipeId);
               }

               if (option == null) {
                  setRunStatus("Recipe not found");
                  AutismClientMessaging.sendPrefixed(
                     "§cRecipe not found: " + (entry.resultName != null && !entry.resultName.isBlank() ? entry.resultName : "unknown")
                  );
                  return;
               }

               int desiredAmount = autismclient.util.AutoCraftingEngine.resolveTargetAmount(option, entry.amount, entry.useMaxAmount);
               if (desiredAmount <= 0) {
                  setRunStatus("No space or materials");
                  AutismClientMessaging.sendPrefixed("§cNo space or materials for " + entry.resultName + ".");
                  return;
               }

               autismclient.util.AutoCraftingEngine.CraftResult result = autismclient.util.AutoCraftingEngine.craft(mc, entry.recipeKey, entry.recipeId, desiredAmount);
               setRunStatus(result.message);
               if (!result.success) {
                  AutismClientMessaging.sendPrefixed("§c" + result.message);
                  return;
               }

               if (isStillRunning()) {
                  CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                  awaitWhileRunning(tickFuture);
               }
            }
         }
      } else {
         setRunStatus("Crafting");
         AutismClientMessaging.sendPrefixed("§cNo craft recipes selected.");
      }
   }

   private static void runGoToAction(Minecraft mc, GoToAction goToAction) {
      setRunStatus(String.format("Going to %.0f,%.0f,%.0f", goToAction.f_x_D, goToAction.f_y_D, goToAction.f_z_D));
      if (mc.player != null && mc.level != null) {
         BlockPos target = new BlockPos((int)Math.floor(goToAction.f_x_D), (int)Math.floor(goToAction.f_y_D), (int)Math.floor(goToAction.f_z_D));
         double radius = goToAction.arrivalRadius;
         int timeoutSeconds = Math.max(1, goToAction.timeoutMs / 1000);
         MacroExecutor.MacroRun run = CURRENT_RUN.get();
         AiOperation op = startAiOperation(
            mc, run, () -> autismclient.ai.engine.AiGoto.travelToCompat(target, radius, false, true, true, timeoutSeconds), false
         );
         if (op == null) {
            AutismClientMessaging.sendPrefixed("Failed to start navigation.");
         } else {
            try {
               if (!goToAction.waitForArrival) {
                  setRunStatus("Goto started");
                  if (run != null && run.aiOperation == op) {
                     run.aiOperation = null;
                     TRACKED_AI_OPERATIONS.add(op);
                     clearNavigationOwner(run);
                  }

                  return;
               }

               while (isStillRunning()) {
                  AiOperation.Result result = run != null ? run.aiOperationResult : null;
                  if (result != null && result.terminal()) {
                     if (result.status() == AiOperation.Status.SUCCEEDED) {
                        setRunStatus("Goto finished");
                     } else {
                        setRunStatus("Goto failed");
                        AutismClientMessaging.sendPrefixed("§cGoto failed: " + result.message());
                     }

                     return;
                  }

                  CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                  awaitWhileRunning(tickFuture);
               }
            } finally {
               if (!isStillRunning()) {
                  releaseNavigation(run, mc);
               } else if (run == null || run.aiOperation == null) {
                  clearNavigationOwner(run);
               }
            }

            return;
         }
      } else {
         AutismClientMessaging.sendPrefixed("No active world; skipping goto action.");
      }
   }

   private static void runMineAction(Minecraft mc, MineAction mineAction) {
      String mineDesc = mineAction.targetBlocks.isEmpty() ? "none" : mineAction.targetBlocks.get(0);
      setRunStatus("Mining: " + mineDesc);
      if (!mineAction.targetBlocks.isEmpty()) {
         if (mc.player != null && mc.level != null) {
            String blockArg = null;

            for (String id : mineAction.targetBlocks) {
               if (id != null && !id.isBlank()) {
                  blockArg = id.trim();
                  break;
               }
            }

            if (blockArg != null) {
               MacroExecutor.MacroRun run = CURRENT_RUN.get();
               MacroExecutor.TargetBlockScanner mineTracker = mineAction.stopMinedCount
                  ? MacroExecutor.TargetBlockScanner.fromRawTargets(mc, mineAction.targetBlocks)
                  : null;
               if (mineTracker != null) {
                  mineTracker.scanAroundPlayer(mc);
                  if (run != null) {
                     run.mineTracker = mineTracker;
                  }
               }

               try {
                  int count = mineAction.stopMinedCount ? Math.max(1, mineAction.minedCountTarget) : 640;
                  int opTimeoutSeconds = mineAction.stopAfterTime ? Math.max(1, mineAction.timeoutSeconds) : 3600;
                  JsonObject mineArgs = new JsonObject();
                  mineArgs.addProperty("block", blockArg);
                  mineArgs.addProperty("count", count);
                  mineArgs.addProperty("timeoutSeconds", opTimeoutSeconds);
                  AiOperation op = startAiOperation(mc, run, () -> autismclient.ai.engine.AiMine.mine(mineArgs), mineAction.h_());
                  if (op == null) {
                     AutismClientMessaging.sendPrefixed("Failed to start mining.");
                     return;
                  }

                  long mineStartMs = System.currentTimeMillis();

                  while (isStillRunning()) {
                     String stopReason = null;
                     int minedDelta = 0;
                     if (mineTracker != null) {
                        mineTracker.scanAroundPlayer(mc);
                        minedDelta = mineTracker.brokenCount();
                        setRunStatus("Mining: " + mineDesc + " (" + minedDelta + "/" + mineAction.minedCountTarget + ")");
                        if (minedDelta >= mineAction.minedCountTarget) {
                           stopReason = "Mine target reached";
                        }
                     }

                     if (stopReason == null && mineAction.stopInventoryFull && isMainInventoryFull(mc)) {
                        stopReason = "Mine stop: inventory full";
                     }

                     if (stopReason == null && mineAction.stopSlotsUsed) {
                        int used = countUsedMainSlots(mc);
                        if (used >= mineAction.slotsUsedThreshold) {
                           stopReason = "Mine stop: slots used";
                        }
                     }

                     if (stopReason == null && mineAction.stopAfterTime && System.currentTimeMillis() - mineStartMs >= mineAction.timeoutSeconds * 1000L) {
                        stopReason = "Mine timeout";
                     }

                     if (stopReason != null) {
                        setRunStatus(stopReason);
                        releaseNavigation(run, mc);
                        return;
                     }

                     AiOperation.Result result = run != null ? run.aiOperationResult : null;
                     if (result != null && result.terminal()) {
                        if (result.status() == AiOperation.Status.SUCCEEDED) {
                           setRunStatus("Mine finished");
                        } else {
                           setRunStatus("Mine stopped");
                           AutismClientMessaging.sendPrefixed("§cMine stopped: " + result.message());
                        }

                        return;
                     }

                     CompletableFuture<Void> tickFuture = MacroWaitRegistry.awaitTickSync();
                     awaitWhileRunning(tickFuture);
                  }

                  return;
               } finally {
                  if (run != null && run.mineTracker == mineTracker) {
                     run.mineTracker = null;
                  }

                  if (!isStillRunning()) {
                     releaseNavigation(run, mc);
                  } else if (run == null || run.aiOperation == null) {
                     clearNavigationOwner(run);
                  }
               }
            }
         } else {
            AutismClientMessaging.sendPrefixed("No active world; skipping mine action.");
         }
      }
   }

   private static AiOperation startAiOperation(Minecraft mc, MacroExecutor.MacroRun run, Supplier<AiOperation> factory, boolean tickAligned) {
      if (run != null) {
         MacroExecutor.MacroRun previousOwner = NAVIGATION_OWNER.getAndSet(run);
         if (previousOwner != null && previousOwner != run) {
            previousOwner.aiOperationResult = new AiOperation.Result(
               AiOperation.Status.CANCELLED, "Navigation superseded by another macro run", "{}", List.of()
            );
            cancelRunAiOperation(previousOwner, mc);
         }

         run.aiOperationResult = null;
      }

      AiOperation op = callOnClientThread(mc, () -> {
         if (run != null) {
            AiOperation previous = run.aiOperation;
            if (previous != null) {
               try {
                  previous.cancel();
               } catch (Throwable var5x) {
               }

               run.aiOperation = null;
            }
         }

         try {
            return factory.get();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[Autism] Failed to start engine operation.", t);
            return null;
         }
      }, (AiOperation)null, tickAligned);
      if (op == null) {
         if (run != null) {
            NAVIGATION_OWNER.compareAndSet(run, null);
         }

         return null;
      } else {
         if (run != null) {
            run.aiOperation = op;
         } else {
            TRACKED_AI_OPERATIONS.add(op);
         }

         cancelTrackedAiOperations(mc, op);
         return op;
      }
   }

   private static void cancelRunAiOperation(MacroExecutor.MacroRun run, Minecraft mc) {
      if (run != null) {
         AiOperation op = run.aiOperation;
         if (op != null && mc != null) {
            PENDING_AI_CANCELLATIONS.incrementAndGet();
            run.aiOperation = null;
            Runnable cancelTask = () -> {
               try {
                  try {
                     op.cancel();
                  } catch (Throwable var5) {
                  }

                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
               } finally {
                  PENDING_AI_CANCELLATIONS.decrementAndGet();
               }
            };
            if (mc.isSameThread()) {
               cancelTask.run();
            } else {
               mc.execute(cancelTask);
            }
         }
      }
   }

   private static void releaseNavigation(MacroExecutor.MacroRun run, Minecraft mc) {
      if (run != null) {
         NAVIGATION_OWNER.compareAndSet(run, null);
         cancelRunAiOperation(run, mc);
      }
   }

   private static void clearNavigationOwner(MacroExecutor.MacroRun run) {
      if (run != null) {
         NAVIGATION_OWNER.compareAndSet(run, null);
      }
   }

   static void trackAiOperation(AiOperation op) {
      if (op != null) {
         TRACKED_AI_OPERATIONS.add(op);
      }
   }

   private static void cancelAllNavigation(Minecraft mc) {
      MacroExecutor.MacroRun owner = NAVIGATION_OWNER.getAndSet(null);
      if (owner != null) {
         cancelRunAiOperation(owner, mc);
      }

      cancelTrackedAiOperations(mc, null);
   }

   private static void cancelTrackedAiOperations(Minecraft mc, AiOperation keep) {
      if (!TRACKED_AI_OPERATIONS.isEmpty()) {
         List<AiOperation> toCancel = new ArrayList<>(TRACKED_AI_OPERATIONS);
         toCancel.remove(keep);
         if (!toCancel.isEmpty() && mc != null) {
            PENDING_AI_CANCELLATIONS.incrementAndGet();
            TRACKED_AI_OPERATIONS.removeAll(toCancel);
            Runnable cancelTask = () -> {
               try {
                  for (AiOperation op : toCancel) {
                     try {
                        op.cancel();
                     } catch (Throwable var7) {
                     }
                  }

                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
               } finally {
                  PENDING_AI_CANCELLATIONS.decrementAndGet();
               }
            };
            if (mc.isSameThread()) {
               cancelTask.run();
            } else {
               mc.execute(cancelTask);
            }
         }
      }
   }

   private static void tickNavigationOwner() {
      MacroExecutor.MacroRun owner = NAVIGATION_OWNER.get();
      if (owner != null) {
         AiOperation op = owner.aiOperation;
         if (op != null) {
            try {
               AiOperation.Result result = op.tick();
               if (result != null) {
                  owner.aiOperationResult = result;
                  if (result.terminal()) {
                     owner.aiOperation = null;
                     NAVIGATION_OWNER.compareAndSet(owner, null);
                  }
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Macro engine op tick failed.", t);

               try {
                  op.cancel();
               } catch (Throwable var7) {
               }

               owner.aiOperation = null;
               owner.aiOperationResult = new AiOperation.Result(AiOperation.Status.FAILED, "Engine op error: " + t.getMessage(), "{}", List.of());
               NAVIGATION_OWNER.compareAndSet(owner, null);
            }
         }
      }

      if (!TRACKED_AI_OPERATIONS.isEmpty()) {
         for (AiOperation op : TRACKED_AI_OPERATIONS) {
            try {
               AiOperation.Result result = op.tick();
               if (result != null && result.terminal()) {
                  TRACKED_AI_OPERATIONS.remove(op);
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Background engine op tick failed.", t);

               try {
                  op.cancel();
               } catch (Throwable var5) {
               }

               TRACKED_AI_OPERATIONS.remove(op);
            }
         }
      }
   }

   private static boolean isMainInventoryFull(Minecraft mc) {
      if (mc.player == null) {
         return false;
      }

      for (int slot = 9; slot < 36; slot++) {
         if (mc.player.getInventory().getItem(slot).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private static int countUsedMainSlots(Minecraft mc) {
      if (mc.player == null) {
         return 0;
      }

      int used = 0;

      for (int slot = 9; slot < 36; slot++) {
         if (!mc.player.getInventory().getItem(slot).isEmpty()) {
            used++;
         }
      }

      return used;
   }

   private static void runPayAction(Minecraft mc, PayAction payAction) {
      payAction.delayMs = PayAction.clampDelay(payAction.delayMs);
      List<String> targets = new ArrayList<>();

      for (String player : payAction.players) {
         if (player != null) {
            MacroTemplateResolver.ResolveResult playerResolution = MacroVariableContext.resolveTemplate(player, mc);
            if (playerResolution.success()) {
               String trimmed = playerResolution.value().trim();
               if (!trimmed.isEmpty() && !targets.contains(trimmed)) {
                  targets.add(trimmed);
               }
            }
         }
      }

      if (targets.isEmpty()) {
         targets.addAll(OnlinePlayerCollector.collect(mc, payAction.probeHidden));
      }

      if (!targets.isEmpty() && mc.getConnection() != null) {
         MacroTemplateResolver.ResolveResult amountResolution = MacroVariableContext.resolveTemplate(payAction.amountInput, mc);
         if (amountResolution.success()) {
            long resolvedAmount = Math.max(0L, PayAction.parseAmount(amountResolution.value()));
            long[] dividedAmounts = payAction.divideEnabled ? PayAction.split(resolvedAmount, targets.size()) : null;
            int sent = 0;

            for (int i = 0; i < targets.size() && isStillRunning(); i++) {
               String player = targets.get(i);
               long perAmount = dividedAmounts != null ? dividedAmounts[i] : resolvedAmount;
               if (perAmount > 0L) {
                  String amountValue = String.valueOf(perAmount);
                  String amountLabel = PayAction.formatAmount(perAmount);
                  String template = payAction.commandTemplate != null && !payAction.commandTemplate.isBlank()
                     ? payAction.commandTemplate
                     : "/pay <player> <amount>";
                  String command = template.replace("<player>", player)
                     .replace("{player}", player)
                     .replace("<amount>", amountValue)
                     .replace("{amount}", amountValue)
                     .trim();
                  MacroTemplateResolver.ResolveResult commandResolution = MacroVariableContext.resolveTemplate(command, mc);
                  if (commandResolution.success()) {
                     command = commandResolution.value().trim();
                     if (!command.isEmpty()) {
                        String commandToSend = command;
                        if (sent > 0 && payAction.delayEnabled && payAction.delayMs > 0) {
                           sleep(payAction.delayMs);
                        }

                        setRunStatus("Paying " + player + " " + amountLabel + " (" + (sent + 1) + "/" + targets.size() + ")");
                        runOnClientThread(mc, () -> {
                           try {
                              if (autismclient.modules.aD.read_Z() || mc.getConnection() == null) {
                                 return;
                              }

                              if (commandToSend.startsWith("/") && commandToSend.length() > 1) {
                                 mc.getConnection().sendCommand(commandToSend.substring(1));
                              } else {
                                 mc.getConnection().sendChat(commandToSend);
                              }
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("§cPay failed: " + e.getMessage());
                           }
                        });
                        sent++;
                        if (payAction.confirmMacro != null && !payAction.confirmMacro.isBlank()) {
                           setRunStatus("Confirming " + player);
                           PayAction.init(payAction.confirmMacro, () -> !isStillRunning());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void stopAll() {
      String macroName = visibleMacro != null && visibleMacro.name != null ? visibleMacro.name : "";

      for (MacroExecutor.MacroRun run : runsSnapshot()) {
         stopMacroRun(run, true);
      }

      synchronized (RUNS_LOCK) {
         ACTIVE_RUNS.clear();
         RUNS_BY_ID.clear();
         refreshRunsSnapshot();
         visibleRun = null;
         visibleRunActive = false;
      }

      ANY_RUN_ACTIVE.set(false);

      try {
         Minecraft mcInst = Minecraft.getInstance();
         autismclient.util.ContainerCloseGuard.flushDeferredCloses(mcInst == null ? null : mcInst.getConnection());
      } catch (Throwable var8) {
      }

      GLOBAL_HELD_ACTIONS.clear();
      releaseMoves(GLOBAL_HELD_MOVES, Minecraft.getInstance(), null);
      GLOBAL_HELD_MOVES.clear();
      syncHeldKeys(Minecraft.getInstance());
      MacroWaitRegistry.cancelAll();
      cancelAllPacketWaiters();
      GLOBAL_WAITING_FOR_CHAT.set(false);
      GLOBAL_WAITING_FOR_PACKET.set(false);
      synchronized (GLOBAL_PACKET_WAIT_LOCK) {
         GLOBAL_PACKET_WAIT_TARGET.set("");
         CompletableFuture<Void> pf = globalPacketWaitFuture;
         if (pf != null) {
            pf.cancel(true);
            globalPacketWaitFuture = null;
         }
      }

      CompletableFuture<Void> cf = globalChatWaitFuture;
      if (cf != null) {
         cf.cancel(true);
         globalChatWaitFuture = null;
      }

      cancelAllNavigation(Minecraft.getInstance());
      autismclient.util.BlockPreviewRenderer.clear();
      Minecraft mc = Minecraft.getInstance();
      PacketGateManager.onConnectionChange(mc == null ? null : mc.getConnection());
      MacroFpsOverrideManager.clearAll();
      PingSpoofScheduler.clearRunOverrides();
      MacroVariableContext.clear();

      try {
         autismclient.util.LanSyncManager sync = autismclient.util.LanSyncManager.getInstance();
         if (sync.isInSession()) {
            sync.reportMacroStepProgress(-1, 0, "");
         }
      } catch (Exception var7) {
      }

      lastMacroThread = null;
      visibleMacro = null;
      setRunStatus("");
      synchronized (SMOOTH_LOOK_LOCK) {
         SMOOTH_LOOK_OWNER.set(null);
         SMOOTH_LOOK_ACTIVE.set(false);
         smoothLookSettledFrames = 0;
      }

      if (!macroName.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cMacro stopped: " + macroName);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro stopped.");
      }
   }

   public static boolean isRunning() {
      return ANY_RUN_ACTIVE.get();
   }

   public static int getActiveRunCount() {
      synchronized (RUNS_LOCK) {
         return countActiveRuns();
      }
   }

   public static boolean needsPacketEvents() {
      return autismclient.modules.aD.read_Z()
         ? false
         : ANY_RUN_ACTIVE.get()
            || globalPacketWaitFuture != null
            || GLOBAL_WAITING_FOR_PACKET.get()
            || !INCOMING_PACKET_WAITERS.isEmpty()
            || !OUTGOING_PACKET_WAITERS.isEmpty()
            || !INCOMING_EVENT_WAITERS.isEmpty()
            || !OUTGOING_EVENT_WAITERS.isEmpty();
   }

   public static boolean isMacroNameRunning(String macroName) {
      if (macroName == null) {
         return false;
      }

      String targetKey = macroNameKey(macroName);

      for (MacroExecutor.MacroRun run : RUNS_BY_ID.values()) {
         if (run.running.get() && macroNameKey(macroNameOf(run.macro)).equals(targetKey)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isMacroRunning(autismclient.util.AutismMacro macro) {
      if (macro == null) {
         return false;
      }

      for (MacroExecutor.MacroRun run : RUNS_BY_ID.values()) {
         if (matchesMacro(run, macro, macro.name)) {
            return true;
         }
      }

      return false;
   }

   public static List<MacroExecutor.ActiveRunSnapshot> getActiveRunSnapshots() {
      synchronized (RUNS_LOCK) {
         ArrayList<MacroExecutor.ActiveRunSnapshot> snapshots = new ArrayList<>(ACTIVE_RUNS.size());

         for (MacroExecutor.MacroRun run : ACTIVE_RUNS.values()) {
            if (run.running.get() && !run.flags.hiddenFromHud()) {
               autismclient.util.AutismMacro macro = run.macro;
               String name = macro != null && macro.name != null ? macro.name : "";
               snapshots.add(new MacroExecutor.ActiveRunSnapshot(macro, name, run.statusText == null ? "" : run.statusText, run.currentStep, run.totalSteps, run.lastCompletedStep));
            }
         }

         return snapshots;
      }
   }

   public static void markManualLevelLeave() {
      if (CURRENT_RUN.get() == null && suppressedLeaveMarks <= 0) {
         manualLeaveAtMs = System.currentTimeMillis();
      }
   }

   public static void runWithSuppressedLeaveMark(Runnable leave) {
      suppressedLeaveMarks++;

      try {
         leave.run();
      } finally {
         suppressedLeaveMarks--;
      }
   }

   public static void onServerDisconnect(boolean stopOnLeave) {
      long quitAt = manualLeaveAtMs;
      manualLeaveAtMs = 0L;
      boolean playerLeft = quitAt > 0L && System.currentTimeMillis() - quitAt < 10000L;
      boolean anyKept = false;

      for (MacroExecutor.MacroRun run : runsSnapshot()) {
         if (run.running.get()) {
            boolean rejoins = rejoinsAfterLeave(run);
            if (!JoinAction.shouldContinue(playerLeft, stopOnLeave, rejoins)) {
               stopMacroRun(run, true);
            } else {
               anyKept = true;
               if (rejoins) {
                  PacketGateManager.close("all", run.id);
                  PingSpoofScheduler.stop(run.id);
               }
            }
         }
      }

      if (!anyKept && stopOnLeave) {
         stopAll();
      } else {
         synchronized (RUNS_LOCK) {
            refreshVisibleState();
         }
      }
   }

   public static boolean hasRejoiningMacro() {
      for (MacroExecutor.MacroRun run : runsView) {
         if (run.running.get() && run.rejoining) {
            return true;
         }
      }

      return false;
   }

   private static boolean rejoinsAfterLeave(MacroExecutor.MacroRun run) {
      return run != null && run.macro != null && JoinAction.hasJoinAhead(run.macro.actions, run.currentStep, run.macro.loop);
   }

   public static void stopMacroByName(String macroName) {
      if (macroName != null && !macroName.isBlank()) {
         String targetKey = macroNameKey(macroName);
         boolean stopped = false;

         for (MacroExecutor.MacroRun run : runsSnapshot()) {
            if (run.running.get() && macroNameKey(macroNameOf(run.macro)).equals(targetKey)) {
               stopMacroRun(run, true);
               stopped = true;
            }
         }

         if (stopped) {
            synchronized (RUNS_LOCK) {
               refreshVisibleState();
            }

            AutismClientMessaging.sendPrefixed("§cMacro stopped: " + macroName);
         }
      }
   }

   public static void stopMacro(autismclient.util.AutismMacro macro) {
      if (macro != null) {
         boolean stopped = false;
         String displayName = macroNameOf(macro);

         for (MacroExecutor.MacroRun run : runsSnapshot()) {
            if (matchesMacro(run, macro, displayName)) {
               stopMacroRun(run, true);
               stopped = true;
            }
         }

         if (stopped) {
            synchronized (RUNS_LOCK) {
               refreshVisibleState();
            }

            AutismClientMessaging.sendPrefixed("§cMacro stopped: " + (displayName.isBlank() ? "macro" : displayName));
         }
      }
   }

   public static String getPrimaryMacroName() {
      autismclient.util.AutismMacro m = visibleMacro;
      return m != null && m.name != null ? m.name : "";
   }

   public static autismclient.util.AutismMacro getPrimaryMacro() {
      return visibleMacro;
   }

   public static String getPrimaryStatusText() {
      MacroExecutor.MacroRun run = visibleRun;
      return run != null ? run.statusText : statusText;
   }

   public static int getPrimaryCurrentStep() {
      MacroExecutor.MacroRun run = visibleRun;
      return run != null ? run.currentStep : currentStep;
   }

   public static int getPrimaryTotalSteps() {
      MacroExecutor.MacroRun run = visibleRun;
      return run != null ? run.totalSteps : totalSteps;
   }

   public static List<MacroExecutor.LoggedChatMessage> getRecentChatMessages() {
      synchronized (CHAT_LOG_LOCK) {
         return new ArrayList<>(CHAT_LOG);
      }
   }

   public static List<MacroExecutor.ChatHistoryEntry> getRecentGuiTitles() {
      synchronized (GUI_TITLE_HISTORY_LOCK) {
         return new ArrayList<>(GUI_TITLE_HISTORY);
      }
   }

   public static void onScreenOpened(Screen screen) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.level == null) {
         clearHistoryBuffers();
      } else if (screen != null && !GuiScreenMatcher.isClientScreen(screen)) {
         String title = screen.getTitle() == null ? "" : screen.getTitle().getString().trim();
         if (!title.isEmpty()) {
            String type = GuiScreenMatcher.screenTypeName(screen);
            MacroExecutor.ChatHistoryEntry entry = new MacroExecutor.ChatHistoryEntry(title, type == null ? "" : type, System.currentTimeMillis());
            synchronized (GUI_TITLE_HISTORY_LOCK) {
               GUI_TITLE_HISTORY.removeIf(existing -> existing.title().equals(entry.title()) && existing.screenType().equals(entry.screenType()));
               GUI_TITLE_HISTORY.addFirst(entry);

               while (GUI_TITLE_HISTORY.size() > 128) {
                  GUI_TITLE_HISTORY.removeLast();
               }
            }
         }
      }
   }

   public static void clearHistoryBuffers() {
      synchronized (CHAT_LOG_LOCK) {
         CHAT_LOG.clear();
         chatLogServerKey = "";
         lastSeenServerKey = "";
      }

      synchronized (GUI_TITLE_HISTORY_LOCK) {
         GUI_TITLE_HISTORY.clear();
      }
   }

   public static MacroExecutor.PacketWaiter registerIncomingPacketWaiter(Predicate<Packet<?>> match) {
      return new MacroExecutor.PacketWaiter(INCOMING_PACKET_WAITERS, match);
   }

   public static MacroExecutor.PacketWaiter registerOutgoingPacketWaiter(Predicate<Packet<?>> match) {
      return new MacroExecutor.PacketWaiter(OUTGOING_PACKET_WAITERS, match);
   }

   private static MacroExecutor.PacketMatchWaiter registerIncomingEventWaiter(MacroExecutor.PacketEventMatcher match) {
      return new MacroExecutor.PacketMatchWaiter(INCOMING_EVENT_WAITERS, match);
   }

   private static MacroExecutor.PacketMatchWaiter registerOutgoingPacketMatch(Predicate<Packet<?>> match) {
      return registerOutgoingEventWaiter((packet, direction, nanoTime) -> match.test(packet));
   }

   private static MacroExecutor.PacketMatchWaiter registerOutgoingEventWaiter(MacroExecutor.PacketEventMatcher match) {
      return new MacroExecutor.PacketMatchWaiter(OUTGOING_EVENT_WAITERS, match);
   }

   private static void dispatchPacketToWaiters(List<MacroExecutor.PacketWaiter> list, Packet<?> packet) {
      if (!list.isEmpty()) {
         long now = System.nanoTime();
         ArrayList<MacroExecutor.PacketWaiter> matched = null;

         for (MacroExecutor.PacketWaiter listener : list) {
            boolean drop;
            try {
               drop = listener.tryMatch(packet, now);
            } catch (Throwable ignored) {
               drop = true;
            }

            if (drop) {
               if (matched == null) {
                  matched = new ArrayList<>(4);
               }

               matched.add(listener);
            }
         }

         if (matched != null) {
            list.removeAll(matched);
         }
      }
   }

   private static void dispatchEventToWaiters(List<MacroExecutor.PacketMatchWaiter> list, MacroExecutor.PacketEvent event) {
      if (!list.isEmpty()) {
         ArrayList<MacroExecutor.PacketMatchWaiter> matched = null;

         for (MacroExecutor.PacketMatchWaiter listener : list) {
            boolean drop;
            try {
               drop = listener.offer(event);
            } catch (Throwable ignored) {
               drop = true;
            }

            if (drop) {
               if (matched == null) {
                  matched = new ArrayList<>(4);
               }

               matched.add(listener);
            }
         }

         if (matched != null) {
            list.removeAll(matched);
         }
      }
   }

   private static void cancelAllPacketWaiters() {
      for (MacroExecutor.PacketWaiter listener : INCOMING_PACKET_WAITERS) {
         listener.cancel();
      }

      for (MacroExecutor.PacketWaiter listener : OUTGOING_PACKET_WAITERS) {
         listener.cancel();
      }

      for (MacroExecutor.PacketMatchWaiter listener : INCOMING_EVENT_WAITERS) {
         listener.dispose();
      }

      for (MacroExecutor.PacketMatchWaiter listener : OUTGOING_EVENT_WAITERS) {
         listener.dispose();
      }
   }

   public static String formatDebugSummary() {
      return "runs="
         + getActiveRunCount()
         + " recv="
         + INCOMING_PACKET_WAITERS.size()
         + " send="
         + OUTGOING_PACKET_WAITERS.size()
         + " timedRecv="
         + INCOMING_EVENT_WAITERS.size()
         + " timedSend="
         + OUTGOING_EVENT_WAITERS.size();
   }

   public static void onOutgoingPacket(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         if (!OUTGOING_EVENT_WAITERS.isEmpty()) {
            dispatchEventToWaiters(OUTGOING_EVENT_WAITERS, new MacroExecutor.PacketEvent(packet, "C2S", System.nanoTime(), null));
         }

         dispatchPacketToWaiters(OUTGOING_PACKET_WAITERS, packet);
         dispatchPacketToRuns(packet, "C2S");
      }
   }

   public static void onIncomingPacket(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         if (!INCOMING_EVENT_WAITERS.isEmpty()) {
            dispatchEventToWaiters(INCOMING_EVENT_WAITERS, new MacroExecutor.PacketEvent(packet, "S2C", System.nanoTime(), null));
         }

         dispatchPacketToWaiters(INCOMING_PACKET_WAITERS, packet);
         if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
            MacroWaitRegistry.markWorldChange();
         }

         if (packet instanceof ClientboundPlayerPositionPacket) {
            MacroWaitRegistry.markWrite();
         }

         dispatchPacketToRuns(packet, "S2C");
      }
   }

   public static void dispatchPacketToRuns(Packet<?> packet, String direction) {
      if (!autismclient.modules.aD.read_Z()) {
         trackServerChange();
         List<MacroExecutor.MacroRun> runs = runsSnapshot();
         boolean s2c = "S2C".equals(direction);
         boolean chatWork = false;
         boolean mineWork = false;
         boolean packetWork = false;

         for (int i = 0; i < runs.size(); i++) {
            MacroExecutor.MacroRun run = runs.get(i);
            if (run.running.get()) {
               if (run.waitingForChat.get()) {
                  chatWork = true;
               }

               if (run.mineTracker != null) {
                  mineWork = true;
               }

               if (run.waitingForPacket.get()) {
                  packetWork = true;
               }
            }
         }

         if (s2c && (chatWork || GLOBAL_WAITING_FOR_CHAT.get())) {
            MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
            if (capture != null && capture.message() != null && !capture.message().isBlank()) {
               for (MacroExecutor.MacroRun run : runs) {
                  Optional<Map<String, MacroVarValue>> matched = run.running.get() && run.waitingForChat.get()
                     ? matchChatCapture(run.chatWaitAction, capture, run.variables)
                     : Optional.empty();
                  if (matched.isPresent()) {
                     run.capturedValues.put(run.chatWaitAction, matched.get());
                     CompletableFuture<Void> f = run.chatWaitFuture;
                     if (f != null) {
                        f.complete(null);
                     }
                  }
               }

               if (isStillRunning() && GLOBAL_WAITING_FOR_CHAT.get() && matchesGlobalChatWait(capture)) {
                  GLOBAL_CHAT_WAIT_MATCH.set(capture.displayText());
                  CompletableFuture<Void> f = globalChatWaitFuture;
                  if (f != null) {
                     f.complete(null);
                  }
               }
            }
         }

         if (s2c && mineWork) {
            Minecraft mc = Minecraft.getInstance();

            for (MacroExecutor.MacroRun run : runs) {
               MacroExecutor.TargetBlockScanner tracker = run.running.get() ? run.mineTracker : null;
               if (tracker != null) {
                  tracker.onBlockPacket(mc, packet);
               }
            }
         }

         if (isStillRunning()) {
            if (packetWork) {
               for (MacroExecutor.MacroRun run : runs) {
                  if (run.running.get() && run.waitingForPacket.get()) {
                     CompletableFuture<Void> f = null;
                     synchronized (run.packetWaitLock) {
                        if (run.waitingForPacket.get() && matchesPacketTarget(run.packetWaitTarget.get(), packet, direction)) {
                           run.matchedPacket = packet;
                           run.matchedPacketDirection = direction;
                           f = run.packetWaitFuture;
                        }
                     }

                     if (f != null) {
                        f.complete(null);
                     }
                  }
               }
            }

            if (GLOBAL_WAITING_FOR_PACKET.get()) {
               CompletableFuture<Void> f = null;
               synchronized (GLOBAL_PACKET_WAIT_LOCK) {
                  if (!GLOBAL_WAITING_FOR_PACKET.get()) {
                     return;
                  }

                  String target = GLOBAL_PACKET_WAIT_TARGET.get();
                  if (matchesPacketTarget(target, packet, direction)) {
                     GLOBAL_MATCHED_PACKET.set(direction + " " + PacketNameResolver.directionalName(packet, direction));
                     f = globalPacketWaitFuture;
                  }
               }

               if (f != null) {
                  f.complete(null);
               }
            }
         }
      }
   }

   public static void recordIncomingChatPacket(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.ChatCapture capture = chatCaptureOf(packet);
         if (capture != null && capture.message() != null && !capture.message().isBlank()) {
            trackServerChange();
            logChatMessage(capture);
         }
      }
   }

   private static void logChatMessage(MacroExecutor.ChatCapture capture) {
      MacroExecutor.LoggedChatMessage message = new MacroExecutor.LoggedChatMessage(
         capture.sender() == null ? "" : capture.sender(),
         capture.message(),
         capture.displayText(),
         capture.displayComponent(),
         capture.senderKind(),
         System.currentTimeMillis()
      );
      synchronized (CHAT_LOG_LOCK) {
         CHAT_LOG.removeIf(existing -> existing.displayText().equals(message.displayText()));
         CHAT_LOG.addFirst(message);

         while (CHAT_LOG.size() > 256) {
            CHAT_LOG.removeLast();
         }
      }
   }

   private static void trackServerChange() {
      String currentServerKey = resolveServerKey();
      if (!currentServerKey.isEmpty() && !currentServerKey.equals(lastSeenServerKey)) {
         synchronized (CHAT_LOG_LOCK) {
            if (chatLogServerKey.isEmpty()) {
               chatLogServerKey = currentServerKey;
               lastSeenServerKey = currentServerKey;
            } else if (!chatLogServerKey.equals(currentServerKey)) {
               CHAT_LOG.clear();
               chatLogServerKey = currentServerKey;
               lastSeenServerKey = currentServerKey;
               synchronized (GUI_TITLE_HISTORY_LOCK) {
                  GUI_TITLE_HISTORY.clear();
               }
            } else {
               lastSeenServerKey = currentServerKey;
            }
         }
      }
   }

   private static String resolveServerKey() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && !mc.isLocalServer()) {
         ServerData entry = mc.getCurrentServer();
         if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
            return normalizeAddress(entry.ip);
         }

         if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
            SocketAddress address = mc.getConnection().getConnection().getRemoteAddress();
            if (address instanceof InetSocketAddress inet) {
               String host = inet.getHostString();
               if ((host == null || host.isBlank()) && inet.getAddress() != null) {
                  host = inet.getAddress().getHostAddress();
               }

               if (host != null && !host.isBlank()) {
                  return normalizeAddress(host + ":" + inet.getPort());
               }
            } else if (address != null) {
               String raw = address.toString();
               if (raw != null && !raw.isBlank()) {
                  return normalizeAddress(raw);
               }
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static String normalizeAddress(String address) {
      if (address == null) {
         return "";
      }

      String stripped = address.startsWith("/") ? address.substring(1) : address;
      return stripped.trim().toLowerCase(Locale.ROOT);
   }

   private static boolean matchesGlobalChatWait(MacroExecutor.ChatCapture capture) {
      String patternJson = GLOBAL_CHAT_WAIT_PATTERN_JSON.get();
      if (!GLOBAL_CHAT_WAIT_USE_REGEX.get() && patternJson != null && !patternJson.isBlank() && capture.displayComponent() != null) {
         String captureJson = serializeComponentJson(capture.displayComponent());
         if (patternJson.equals(captureJson)) {
            return true;
         }
      }

      String pattern = cleanVisibleChatText(GLOBAL_CHAT_WAIT_PATTERN.get());
      if (pattern == null || pattern.isBlank()) {
         return true;
      }

      if (GLOBAL_CHAT_WAIT_USE_REGEX.get()) {
         try {
            Pattern compiled = PatternCaptureMatcher.compileCached(pattern);
            return compiled.matcher(capture.message()).matches() || compiled.matcher(capture.displayText()).matches();
         } catch (Exception ignored) {
            return false;
         }
      } else {
         int threshold = GLOBAL_CHAT_WAIT_FUZZY_PERCENT.get();
         return fuzzyMatchesChat(pattern, capture.message(), threshold) || fuzzyMatchesChat(pattern, capture.displayText(), threshold);
      }
   }

   private static boolean chatCaptureMatches(WaitForChatAction action, MacroExecutor.ChatCapture capture) {
      return matchChatCapture(action, capture, getCurrentVariableStore()).isPresent();
   }

   private static Optional<Map<String, MacroVarValue>> matchChatCapture(WaitForChatAction action, MacroExecutor.ChatCapture capture) {
      return matchChatCapture(action, capture, getCurrentVariableStore());
   }

   private static Optional<Map<String, MacroVarValue>> matchChatCapture(WaitForChatAction action, MacroExecutor.ChatCapture capture, MacroVariableStore context) {
      if (action != null && capture != null) {
         PatternCaptureMatcher.MatchMode mode = action.effectiveMatchMode();
         if (mode == PatternCaptureMatcher.MatchMode.MATCH && action.patternJson != null && !action.patternJson.isBlank() && capture.displayComponent() != null) {
            String captureJson = serializeComponentJson(capture.displayComponent());
            if (action.patternJson.equals(captureJson)) {
               return Optional.of(withChatProperties(action, capture, Map.of()));
            }
         }

         String pattern = cleanVisibleChatText(action.pattern);
         if (mode == PatternCaptureMatcher.MatchMode.MATCH) {
            MacroTemplateResolver.ResolveResult patternResolution = MacroTemplateResolver.resolve(pattern, context, Minecraft.getInstance());
            if (!patternResolution.success()) {
               return Optional.empty();
            }

            pattern = patternResolution.value();
         }

         if (pattern == null || pattern.isBlank()) {
            return Optional.of(withChatProperties(action, capture, Map.of()));
         } else if (mode != PatternCaptureMatcher.MatchMode.MATCH) {
            Optional<PatternCaptureMatcher.CaptureResult> messageResult = PatternCaptureMatcher.match(mode, pattern, capture.message());
            Optional<PatternCaptureMatcher.CaptureResult> result = messageResult.isPresent() ? messageResult : PatternCaptureMatcher.match(mode, pattern, capture.displayText());
            return result.map(value -> withChatProperties(action, capture, value.variables()));
         } else {
            int threshold = WaitForChatAction.snapFuzzyPercent(action.fuzzyPercent);
            boolean matched = fuzzyMatchesChat(pattern, capture.message(), threshold) || fuzzyMatchesChat(pattern, capture.displayText(), threshold);
            return matched ? Optional.of(withChatProperties(action, capture, Map.of())) : Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static Map<String, MacroVarValue> withChatProperties(WaitForChatAction action, MacroExecutor.ChatCapture capture, Map<String, MacroVarValue> captures) {
      Map<String, MacroVarValue> values = new LinkedHashMap<>(captures);
      if (action.saveAs != null && !action.saveAs.isBlank()) {
         Map<String, MacroVarValue> properties = new LinkedHashMap<>();
         properties.put("sender", MacroVarValue.ofText(capture.sender()));
         properties.put("message", MacroVarValue.ofText(capture.message()));
         properties.put("display", MacroVarValue.ofText(capture.displayText()));
         properties.put("source", MacroVarValue.ofText(capture.senderKind().name()));
         values.put(action.saveAs, MacroVarValue.of(MacroVarValue.ValueType.TEXT, capture.displayText(), properties));
      }

      return Map.copyOf(values);
   }

   private static MacroExecutor.ChatCapture chatCaptureOf(Packet<?> packet) {
      if (packet instanceof ClientboundSystemChatPacket systemChat) {
         Component content = systemChat.content();
         if (content == null) {
            return null;
         }

         String raw = content.getString();
         return raw != null && !raw.isBlank() ? createChatCapture("", raw, raw, content, MacroExecutor.ChatSenderKind.PLAYER) : null;
      } else if (packet instanceof ClientboundDisguisedChatPacket disguisedChat) {
         Component content = disguisedChat.message();
         String text = content == null ? null : content.getString();
         if (text != null && !text.isBlank()) {
            Component decorated = disguisedChat.chatType().decorate(content);
            String sender = disguisedChat.chatType().name().getString();
            return createChatCapture(sender, text, null, decorated, MacroExecutor.ChatSenderKind.PLAYER);
         } else {
            return null;
         }
      } else if (!(packet instanceof ClientboundPlayerChatPacket playerChat)) {
         return null;
      } else {
         Component content = playerChat.unsignedContent().orElse(Component.literal(playerChat.body().content()));
         String message = content.getString();
         if (message == null || message.isBlank()) {
            message = playerChat.body().content();
         }

         if (message != null && !message.isBlank()) {
            Component decorated = playerChat.chatType().decorate(content);
            String sender = playerChat.chatType().name().getString();
            if (sender == null || sender.isBlank()) {
               String byUuid = playerNameOf(playerChat.sender());
               if (byUuid != null && !byUuid.isBlank()) {
                  sender = byUuid;
               }
            }

            return createChatCapture(sender, message, null, decorated, MacroExecutor.ChatSenderKind.PLAYER);
         } else {
            return null;
         }
      }
   }

   private static MacroExecutor.ChatCapture createChatCapture(
      String sender, String message, String displayText, Component displayComponent, MacroExecutor.ChatSenderKind source
   ) {
      String cleanMessage = cleanChatText(message);
      if (cleanMessage.isBlank()) {
         return null;
      }

      String cleanSender = cleanChatText(sender);
      String cleanDisplay = cleanChatText(displayText);
      Component rendered = flattenComponent(displayComponent);
      String display = cleanDisplay;
      if (rendered == null || rendered.getString().isBlank()) {
         rendered = cleanSender.isBlank()
            ? Component.literal(cleanMessage)
            : Component.literal("<" + cleanSender + "> ").append(Component.literal(cleanMessage));
      }

      if (display == null || display.isBlank()) {
         display = cleanChatText(rendered.getString());
      }

      MacroExecutor.ChatSenderKind resolvedSource = source != null ? source : MacroExecutor.ChatSenderKind.PLAYER;
      return new MacroExecutor.ChatCapture(cleanSender, cleanMessage, cleanChatText(display), rendered, resolvedSource);
   }

   private static String cleanChatText(String text) {
      if (text == null) {
         return "";
      }

      String stripped = stripFormattingCodes(text).replace('\n', ' ').replace('\r', ' ');
      StringBuilder out = new StringBuilder(stripped.length());
      int offset = 0;

      while (offset < stripped.length()) {
         int cp = stripped.codePointAt(offset);
         offset += Character.charCount(cp);
         int type = Character.getType(cp);
         if (type != 16 && type != 15 && cp != 8203 && cp != 8204 && cp != 8205 && cp != 8288 && cp != 65038 && cp != 65039) {
            out.appendCodePoint(cp);
         }
      }

      return out.toString().trim();
   }

   public static String cleanVisibleChatText(String text) {
      if (text != null && !text.isBlank()) {
         String stripped = stripFormattingCodes(text).trim();
         String flattened = jsonComponentText(stripped);
         return cleanChatText(flattened != null ? flattened : stripped);
      } else {
         return "";
      }
   }

   private static String jsonComponentText(String text) {
      if (text != null && !text.isBlank()) {
         String trimmed = text.trim();
         if (!trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("\"")) {
            return null;
         }

         try {
            JsonElement element = JsonParser.parseString(trimmed);
            if (element != null && !element.isJsonNull()) {
               if (element.isJsonArray()) {
                  StringBuilder out = new StringBuilder();

                  for (JsonElement child : element.getAsJsonArray()) {
                     Component component = parseChatComponent(child);
                     if (component != null) {
                        out.append(component.getString());
                     }
                  }

                  return out.toString();
               } else {
                  Component component = parseChatComponent(element);
                  return component == null ? null : component.getString();
               }
            } else {
               return "";
            }
         } catch (RuntimeException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Component parseChatComponent(JsonElement element) {
      try {
         Component component = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
         return flattenComponent(component);
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   private static String stripFormattingCodes(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder out = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ((ch == 167 || ch == '&') && i + 1 < text.length()) {
               char next = Character.toLowerCase(text.charAt(i + 1));
               if (next >= '0' && next <= '9' || next >= 'a' && next <= 'f' || next >= 'k' && next <= 'o' || next == 'r' || next == 'x') {
                  i++;
                  continue;
               }
            }

            out.append(ch);
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private static boolean isOnlinePlayer(String sender) {
      String cleanSender = cleanChatText(sender);
      if (cleanSender.isBlank()) {
         return false;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() != null) {
            for (PlayerInfo entry : mc.getConnection().getListedOnlinePlayers()) {
               String playerName = entry != null && entry.getProfile() != null ? extractText(entry.getProfile()) : null;
               if (playerName != null && cleanSender.equalsIgnoreCase(playerName)) {
                  return true;
               }
            }
         }
      } catch (Throwable var7) {
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            for (AbstractClientPlayer player : mc.level.players()) {
               String playerName = player != null && player.getGameProfile() != null ? extractText(player.getGameProfile()) : null;
               if (playerName != null && cleanSender.equalsIgnoreCase(playerName)) {
                  return true;
               }
            }
         }
      } catch (Throwable var6) {
      }

      return false;
   }

   public static boolean fuzzyMatchesChat(String pattern, String candidate, int thresholdPercent) {
      String normalizedPattern = normalizeChatForMatch(pattern);
      String normalizedCandidate = normalizeChatForMatch(candidate);
      if (normalizedPattern.isBlank()) {
         return true;
      }

      if (normalizedCandidate.isBlank()) {
         return false;
      }

      if (normalizedCandidate.contains(normalizedPattern)) {
         return true;
      }

      List<String> patternTokens = splitTokens(normalizedPattern);
      List<String> candidateTokens = splitTokens(normalizedCandidate);
      if (patternTokens.isEmpty()) {
         return true;
      }

      if (thresholdPercent >= 100) {
         return containsAllTokens(patternTokens, normalizedCandidate);
      }

      double tokenScore = tokenSimilarity(patternTokens, candidateTokens);
      double charScore = editSimilarity(normalizedPattern.replace(" ", ""), normalizedCandidate.replace(" ", ""));
      double score = Math.max(tokenScore, tokenScore * 0.7 + charScore * 0.3);
      return score + 1.0E-9 >= Math.max(40, Math.min(100, thresholdPercent)) / 100.0;
   }

   private static String normalizeForMatch(String text) {
      if (text == null) {
         return "";
      }

      String normalized = Normalizer.normalize(text, Form.NFKC).toLowerCase(Locale.ROOT);
      StringBuilder out = new StringBuilder(normalized.length());

      for (int i = 0; i < normalized.length(); i++) {
         char ch = normalized.charAt(i);
         if (Character.isLetterOrDigit(ch)) {
            out.append(ch);
         } else if (Character.isWhitespace(ch)) {
            out.append(' ');
         } else if (ch != '\'' && ch != '"' && ch != '`' && ch != '-' && ch != '_' && ch != 8217) {
            out.append(' ');
         }
      }

      return out.toString().trim().replaceAll("\\s+", " ");
   }

   public static String normalizeChatForMatch(String text) {
      if (text == null) {
         return "";
      }

      String normalized = Normalizer.normalize(text, Form.NFKD).toLowerCase(Locale.ROOT);
      StringBuilder out = new StringBuilder(normalized.length());
      boolean lastWasSpace = true;
      int offset = 0;

      while (offset < normalized.length()) {
         int cp = normalized.codePointAt(offset);
         offset += Character.charCount(cp);
         cp = foldLookalike(cp);
         int type = Character.getType(cp);
         if (type != 6 && type != 8 && type != 7 && type != 16 && type != 15 && type != 18 && type != 19) {
            if (Character.isLetterOrDigit(cp)) {
               out.appendCodePoint(cp);
               lastWasSpace = false;
            } else if (Character.isWhitespace(cp)) {
               if (!lastWasSpace) {
                  out.append(' ');
               }

               lastWasSpace = true;
            } else if (!isQuoteOrDash(cp)) {
               if (!lastWasSpace) {
                  out.append(' ');
               }

               lastWasSpace = true;
            }
         }
      }

      offset = out.length();
      if (offset > 0 && out.charAt(offset - 1) == ' ') {
         out.setLength(offset - 1);
      }

      return out.toString();
   }

   private static boolean isQuoteOrDash(int cp) {
      boolean var10000;
      switch (cp) {
         case 34:
         case 39:
         case 45:
         case 95:
         case 96:
         case 700:
         case 8208:
         case 8209:
         case 8210:
         case 8211:
         case 8212:
         case 8213:
         case 8216:
         case 8217:
         case 8219:
         case 8242:
         case 8722:
         case 65287:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private static int foldLookalike(int cp) {
      int var10000;
      switch (cp) {
         case 223:
            var10000 = 115;
            break;
         case 230:
            var10000 = 97;
            break;
         case 339:
            var10000 = 111;
            break;
         case 491:
            var10000 = 113;
            break;
         case 610:
            var10000 = 103;
            break;
         case 618:
            var10000 = 105;
            break;
         case 628:
            var10000 = 110;
            break;
         case 640:
            var10000 = 114;
            break;
         case 655:
            var10000 = 121;
            break;
         case 665:
            var10000 = 98;
            break;
         case 668:
            var10000 = 104;
            break;
         case 671:
            var10000 = 108;
            break;
         case 739:
            var10000 = 120;
            break;
         case 7424:
            var10000 = 97;
            break;
         case 7428:
            var10000 = 99;
            break;
         case 7429:
            var10000 = 100;
            break;
         case 7431:
            var10000 = 101;
            break;
         case 7434:
            var10000 = 106;
            break;
         case 7435:
            var10000 = 107;
            break;
         case 7437:
            var10000 = 109;
            break;
         case 7439:
            var10000 = 111;
            break;
         case 7448:
            var10000 = 112;
            break;
         case 7451:
            var10000 = 116;
            break;
         case 7452:
            var10000 = 117;
            break;
         case 7456:
            var10000 = 118;
            break;
         case 7457:
            var10000 = 119;
            break;
         case 7458:
            var10000 = 122;
            break;
         case 42800:
            var10000 = 102;
            break;
         case 42801:
            var10000 = 115;
            break;
         default:
            var10000 = cp;
      }

      return var10000;
   }

   private static List<String> splitTokens(String normalized) {
      return normalized != null && !normalized.isBlank() ? List.of(normalized.split(" ")) : List.of();
   }

   private static boolean containsAllTokens(List<String> patternTokens, String normalizedCandidate) {
      for (String token : patternTokens) {
         if (!normalizedCandidate.contains(token)) {
            return false;
         }
      }

      return true;
   }

   private static double tokenSimilarity(List<String> patternTokens, List<String> candidateTokens) {
      if (patternTokens.isEmpty()) {
         return 1.0;
      }

      if (candidateTokens.isEmpty()) {
         return 0.0;
      }

      double total = 0.0;

      for (String patternToken : patternTokens) {
         double best = 0.0;

         for (String candidateToken : candidateTokens) {
            best = Math.max(best, wordSimilarity(patternToken, candidateToken));
            if (best >= 1.0) {
               break;
            }
         }

         total += best;
      }

      return total / patternTokens.size();
   }

   private static double wordSimilarity(String left, String right) {
      if (left.equals(right)) {
         return 1.0;
      } else if (!left.isBlank() && !right.isBlank()) {
         return !left.contains(right) && !right.contains(left)
            ? editSimilarity(left, right)
            : (double)Math.min(left.length(), right.length()) / Math.max(left.length(), right.length());
      } else {
         return 0.0;
      }
   }

   private static double editSimilarity(String left, String right) {
      if (left.equals(right)) {
         return 1.0;
      }

      int maxLen = Math.max(left.length(), right.length());
      if (maxLen == 0) {
         return 1.0;
      }

      int distance = levenshteinDistance(left, right);
      return Math.max(0.0, 1.0 - (double)distance / maxLen);
   }

   private static int levenshteinDistance(String left, String right) {
      int[] previous = new int[right.length() + 1];
      int[] current = new int[right.length() + 1];
      int j = 0;

      while (j <= right.length()) {
         previous[j] = j++;
      }

      for (int i = 1; i <= left.length(); i++) {
         current[0] = i;

         for (int jx = 1; jx <= right.length(); jx++) {
            int cost = left.charAt(i - 1) == right.charAt(jx - 1) ? 0 : 1;
            current[jx] = Math.min(Math.min(current[jx - 1] + 1, previous[jx] + 1), previous[jx - 1] + cost);
         }

         int[] temp = previous;
         previous = current;
         current = temp;
      }

      return previous[right.length()];
   }

   private static String extractNameValue(Object value) {
      Object direct = firstNonNullValue(
         invokeFirstGetter(value, "name", "getName", "targetName", "getTargetName"),
         recordComponentAt(value, 0),
         invokeOrComponent(value, 2, "name", "targetName", "type")
      );
      return cleanChatText(extractText(direct));
   }

   private static String playerNameOf(UUID uuid) {
      if (uuid == null) {
         return "";
      }

      try {
         if (Minecraft.getInstance().getConnection() != null) {
            PlayerInfo entry = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
            if (entry != null && entry.getProfile() != null) {
               String profileName = extractText(entry.getProfile());
               if (profileName != null && !profileName.isBlank()) {
                  return profileName;
               }
            }
         }
      } catch (Throwable var4) {
      }

      try {
         if (Minecraft.getInstance().level != null) {
            for (AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
               if (uuid.equals(player.getUUID()) && player.getGameProfile() != null) {
                  String profileName = extractText(player.getGameProfile());
                  if (profileName != null && !profileName.isBlank()) {
                     return profileName;
                  }
               }
            }
         }
      } catch (Throwable var5) {
      }

      return "";
   }

   private static Object firstNonNullValue(Object... values) {
      if (values == null) {
         return null;
      }

      for (Object value : values) {
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static Object invokeFirstGetter(Object target, String... names) {
      if (target != null && names != null) {
         Class<?> type = target.getClass();

         for (String name : names) {
            try {
               Method method = type.getMethod(name);
               method.setAccessible(true);
               return method.invoke(target);
            } catch (Throwable var8) {
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object recordComponentAt(Object target, int index) {
      if (target == null) {
         return null;
      }

      try {
         RecordComponent[] components = target.getClass().getRecordComponents();
         if (components != null && index >= 0 && index < components.length) {
            Method accessor = components[index].getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(target);
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static Object recordComponentOfType(Object target, Class<?> type) {
      if (target != null && type != null) {
         try {
            RecordComponent[] components = target.getClass().getRecordComponents();
            if (components == null) {
               return null;
            }

            for (RecordComponent component : components) {
               if (type.isAssignableFrom(component.getType())) {
                  Method accessor = component.getAccessor();
                  accessor.setAccessible(true);
                  return accessor.invoke(target);
               }
            }
         } catch (Throwable var8) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object invokeOrComponent(Object target, int fallbackIndex, String... names) {
      Object direct = invokeFirstGetter(target, names);
      return direct != null ? direct : recordComponentAt(target, fallbackIndex);
   }

   private static Object firstComponentExcept(Object target, Object... excludedValues) {
      if (target == null) {
         return null;
      }

      try {
         RecordComponent[] components = target.getClass().getRecordComponents();
         if (components == null) {
            return null;
         }

         label51:
         for (RecordComponent component : components) {
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            Object value = accessor.invoke(target);
            if (value != null) {
               for (Object excluded : excludedValues) {
                  if (excluded == value) {
                     continue label51;
                  }
               }

               if (value instanceof Component || value instanceof CharSequence || value instanceof Optional) {
                  return value;
               }
            }
         }
      } catch (Throwable var13) {
      }

      return null;
   }

   private static Component extractComponent(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof Optional<?> optional) {
         return optional.isPresent() ? extractComponent(optional.get()) : null;
      } else if (value instanceof Component text) {
         return flattenComponent(text);
      } else if (value instanceof CharSequence chars) {
         return Component.literal(chars.toString());
      } else if (value instanceof UUID uuid) {
         return Component.literal(playerNameOf(uuid));
      } else {
         try {
            Method getComponent = value.getClass().getMethod("getText");
            getComponent.setAccessible(true);
            Object result = getComponent.invoke(value);
            Component text = extractComponent(result);
            if (text != null) {
               return text;
            }
         } catch (Throwable var9) {
         }

         try {
            Method getString = value.getClass().getMethod("getString");
            getString.setAccessible(true);
            Object result = getString.invoke(value);
            if (result != null) {
               return Component.literal(String.valueOf(result));
            }
         } catch (Throwable var8) {
         }

         return null;
      }
   }

   public static Component flattenComponent(Component text) {
      if (text == null) {
         return null;
      }

      MutableComponent literalized = Component.empty();

      try {
         text.visit((style, string) -> {
            if (string != null && !string.isEmpty()) {
               literalized.append(Component.literal(string).setStyle(sanitizeChatStyle(style)));
            }

            return Optional.empty();
         }, Style.EMPTY);
      } catch (Throwable var3) {
      }

      return !literalized.getString().isEmpty() ? literalized : Component.literal(text.getString());
   }

   private static Style sanitizeChatStyle(Style style) {
      if (style != null && !style.isEmpty()) {
         Style safe = Style.EMPTY;
         if (style.getFont() != null) {
            safe = safe.withFont(style.getFont());
         }

         if (style.getColor() != null) {
            safe = safe.withColor(style.getColor());
         }

         if (style.isBold()) {
            safe = safe.withBold(true);
         }

         if (style.isItalic()) {
            safe = safe.withItalic(true);
         }

         if (style.isUnderlined()) {
            safe = safe.withUnderlined(true);
         }

         if (style.isStrikethrough()) {
            safe = safe.withStrikethrough(true);
         }

         if (style.isObfuscated()) {
            safe = safe.withObfuscated(true);
         }

         return safe;
      } else {
         return Style.EMPTY;
      }
   }

   public static String serializeComponentJson(Component text) {
      if (text == null) {
         return "";
      }

      try {
         Component safeComponent = flattenComponent(text);
         return safeComponent == null ? "" : GSON.toJson(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, safeComponent).getOrThrow());
      } catch (Throwable ignored) {
         return "";
      }
   }

   public static Component deserializeComponentJson(String json) {
      if (json != null && !json.isBlank()) {
         try {
            return flattenComponent(ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().copy());
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String extractText(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof Optional<?> optional) {
         return optional.isPresent() ? extractText(optional.get()) : null;
      } else if (value instanceof Component text) {
         return text.getString();
      } else if (value instanceof CharSequence chars) {
         return chars.toString();
      } else if (value instanceof UUID uuid) {
         return playerNameOf(uuid);
      } else {
         try {
            Method getString = value.getClass().getMethod("getString");
            getString.setAccessible(true);
            Object result = getString.invoke(value);
            if (result != null) {
               return String.valueOf(result);
            }
         } catch (Throwable var9) {
         }

         try {
            Method getName = value.getClass().getMethod("getName");
            getName.setAccessible(true);
            if (getName.invoke(value) instanceof CharSequence chars) {
               return chars.toString();
            }
         } catch (Throwable var8) {
         }

         return null;
      }
   }

   public static void tickSmoothLook(float tickDelta) {
      if (SMOOTH_LOOK_ACTIVE.get()) {
         MacroExecutor.MacroRun owner = SMOOTH_LOOK_OWNER.get();
         if (owner != null && !owner.running.get()) {
            releaseSmoothLook(owner);
         } else if (owner == null && !ANY_RUN_ACTIVE.get()) {
            synchronized (SMOOTH_LOOK_LOCK) {
               if (SMOOTH_LOOK_OWNER.get() == null) {
                  SMOOTH_LOOK_ACTIVE.set(false);
               }
            }
         } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
               float currentYaw = mc.player.getYRot();
               float currentPitch = mc.player.getXRot();
               double deltaYaw = Mth.wrapDegrees(smoothLookYaw - currentYaw);
               double deltaPitch = smoothLookPitch - currentPitch;
               double diagonalDistance = Math.hypot(deltaYaw, deltaPitch);
               if (diagonalDistance <= 0.35) {
                  mc.player.setYRot(smoothLookYaw);
                  mc.player.setXRot(smoothLookPitch);
                  smoothLookSettledFrames++;
                  if (smoothLookSettledFrames >= 2) {
                     synchronized (SMOOTH_LOOK_LOCK) {
                        if (SMOOTH_LOOK_OWNER.get() == owner) {
                           SMOOTH_LOOK_ACTIVE.set(false);
                           SMOOTH_LOOK_OWNER.set(null);
                        }
                     }
                  }
               } else {
                  smoothLookSettledFrames = 0;
                  double frameStep = Math.min(diagonalDistance, smoothLookSpeed * Math.max(tickDelta, 1.0F));
                  double stepYaw = deltaYaw / diagonalDistance * frameStep;
                  double stepPitch = deltaPitch / diagonalDistance * frameStep;
                  mc.player.setYRot(currentYaw + (float)stepYaw);
                  mc.player.setXRot(currentPitch + (float)stepPitch);
               }
            }
         }
      }
   }

   public static boolean isSmoothLookActive() {
      return SMOOTH_LOOK_ACTIVE.get();
   }

   private static void startSmoothLook(float yaw, float pitch, double speed) {
      MacroExecutor.MacroRun run;
      for (run = CURRENT_RUN.get(); run != null && run.running.get(); LockSupport.parkNanos(500000L)) {
         synchronized (SMOOTH_LOOK_LOCK) {
            MacroExecutor.MacroRun owner = SMOOTH_LOOK_OWNER.get();
            if (owner == null || owner == run || !owner.running.get()) {
               SMOOTH_LOOK_OWNER.set(run);
               smoothLookYaw = yaw;
               smoothLookPitch = pitch;
               smoothLookSpeed = speed;
               smoothLookSettledFrames = 0;
               SMOOTH_LOOK_ACTIVE.set(true);
               return;
            }
         }
      }

      if (run == null) {
         synchronized (SMOOTH_LOOK_LOCK) {
            if (SMOOTH_LOOK_OWNER.get() != null) {
               return;
            }

            smoothLookYaw = yaw;
            smoothLookPitch = pitch;
            smoothLookSpeed = speed;
            smoothLookSettledFrames = 0;
            SMOOTH_LOOK_ACTIVE.set(true);
         }
      }
   }

   private static void awaitSmoothLookDone() {
      MacroExecutor.MacroRun run = CURRENT_RUN.get();

      while (isStillRunning() && SMOOTH_LOOK_ACTIVE.get() && (run == null || SMOOTH_LOOK_OWNER.get() == run)) {
         LockSupport.parkNanos(1000000L);
      }
   }

   private static void releaseSmoothLook(MacroExecutor.MacroRun run) {
      if (run != null) {
         synchronized (SMOOTH_LOOK_LOCK) {
            if (SMOOTH_LOOK_OWNER.get() == run) {
               SMOOTH_LOOK_ACTIVE.set(false);
               smoothLookSettledFrames = 0;
               SMOOTH_LOOK_OWNER.set(null);
            }
         }
      }
   }

   private static LookAtBlockAction.LookTarget resolveLookTarget(Minecraft mc, LookAtBlockAction action) {
      if (mc != null && action != null) {
         AtomicReference<LookAtBlockAction.LookTarget> targetRef = new AtomicReference<>();
         CountDownLatch latch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               targetRef.set(action.resolveTarget(mc));
            } finally {
               latch.countDown();
            }
         });

         while (isStillRunning() && !awaitLatch(latch, 10L)) {
         }

         return targetRef.get();
      } else {
         return null;
      }
   }

   public static void stopAllFromAi() {
      stopAll();
   }

   private static void sendPacketBatch(Minecraft mc, List<SendPacketAction> batch) {
      if (mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("§cNo network connection for burst!");
      } else {
         List<AutismSessionState.DelayedPacket> allQueuedPackets = new ArrayList<>();

         for (SendPacketAction action : batch) {
            int repeats = SendPacketAction.clampSendCount(action.sendCount);

            for (int k = 0; k < repeats; k++) {
               allQueuedPackets.addAll(action.getPackets());
            }
         }

         if (!allQueuedPackets.isEmpty()) {
            List<Packet<?>> regeneratedPackets = new ArrayList<>(allQueuedPackets.size());

            for (AutismSessionState.DelayedPacket qp : allQueuedPackets) {
               Packet<?> resolved = SendPacketAction.resolvePacket(qp);
               if (resolved != null) {
                  regeneratedPackets.add(resolved);
               }
            }

            if (regeneratedPackets.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cBurst: All packets failed to regenerate!");
            } else {
               ClientPacketListener handler = mc.getConnection();

               for (Packet<?> packet : regeneratedPackets) {
                  handler.send(packet);
               }

               if (regeneratedPackets.size() > 1) {
                  AutismClientMessaging.sendPrefixed("§a Burst sent " + regeneratedPackets.size() + " packets");
               }
            }
         }
      }
   }

   private static List<AutismSessionState.DelayedPacket> collectQueuedPackets(autismclient.util.AutismMacro macro, int startIdx, int count) {
      new ArrayList();
      int maxIdx = count < 0 ? macro.actions.size() : Math.min(startIdx + count, macro.actions.size());
      return collectQueuedPackets(macro, startIdx, count, maxIdx);
   }

   private static List<AutismSessionState.DelayedPacket> collectQueuedPackets(autismclient.util.AutismMacro macro, int startIdx, int count, int endIdx) {
      List<AutismSessionState.DelayedPacket> result = new ArrayList<>();
      int maxIdx = Math.min(endIdx, count < 0 ? macro.actions.size() : Math.min(startIdx + count, macro.actions.size()));

      for (int j = startIdx; j < maxIdx; j++) {
         MacroAction act = macro.actions.get(j);
         if (!(act instanceof SendPacketAction spa)) {
            break;
         }

         int repeats = SendPacketAction.clampSendCount(spa.sendCount);

         for (AutismSessionState.DelayedPacket qp : spa.getPackets()) {
            if (qp.packet != null) {
               for (int k = 0; k < repeats; k++) {
                  result.add(qp);
               }
            }
         }
      }

      return result;
   }

   private static void sendQueuedPackets(Minecraft mc, List<AutismSessionState.DelayedPacket> packets) {
      if (!packets.isEmpty() && mc.getConnection() != null) {
         ClientPacketListener handler = mc.getConnection();

         for (AutismSessionState.DelayedPacket qp : packets) {
            Packet<?> toSend = SendPacketAction.resolvePacket(qp);
            if (toSend != null) {
               handler.send(toSend);
            }
         }

         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (module.isForceChannelFlush()) {
            try {
               Connection connection = handler.getConnection();
               if (connection != null) {
                  connection.flushChannel();
               }
            } catch (Exception var6) {
            }
         }
      }
   }

   private static int countActionsForPackets(autismclient.util.AutismMacro macro, int startIdx, int packetCount) {
      return countActionsForPackets(macro, startIdx, packetCount, macro.actions.size());
   }

   private static int countActionsForPackets(autismclient.util.AutismMacro macro, int startIdx, int packetCount, int endIdx) {
      int actionsToSkip = 0;
      int packetsRemaining = packetCount;

      for (int j = startIdx; j < endIdx && packetsRemaining > 0; j++) {
         MacroAction act = macro.actions.get(j);
         if (!(act instanceof SendPacketAction spa)) {
            break;
         }

         int actionPackets = spa.getPackets().size() * SendPacketAction.clampSendCount(spa.sendCount);
         packetsRemaining -= actionPackets;
         actionsToSkip++;
      }

      return actionsToSkip;
   }

   static {
      for (MacroResourceClassifier.MacroResource resource : MacroResourceClassifier.MacroResource.values()) {
         RESOURCE_LOCKS.put(resource, new ReentrantLock(true));
      }

      NEXT_RUN_ID = new AtomicLong(1L);
      ACTIVE_RUNS = new LinkedHashMap<>();
      RUNS_BY_ID = new ConcurrentHashMap<>();
      RUN_EPOCH = new AtomicLong();
      CURRENT_RUN = new ThreadLocal<>();
      SUPPRESS_POST_GUI_WAIT = ThreadLocal.withInitial(() -> false);
      visibleRun = null;
      visibleMacro = null;
      lastMacroThread = null;
      GLOBAL_MATCHED_PACKET = new AtomicReference<>("");
      statusText = "";
      currentStep = -1;
      totalSteps = 0;
      GLOBAL_WAITING_FOR_PACKET = new AtomicBoolean(false);
      GLOBAL_PACKET_WAIT_TARGET = new AtomicReference<>("");
      GLOBAL_PACKET_WAIT_LOCK = new Object();
      GLOBAL_WAITING_FOR_CHAT = new AtomicBoolean(false);
      GLOBAL_CHAT_WAIT_PATTERN = new AtomicReference<>("");
      GLOBAL_CHAT_WAIT_USE_REGEX = new AtomicBoolean(false);
      GLOBAL_CHAT_WAIT_PATTERN_JSON = new AtomicReference<>("");
      GLOBAL_CHAT_WAIT_FUZZY_PERCENT = new AtomicInteger(100);
      GLOBAL_CHAT_WAIT_MATCH = new AtomicReference<>("");
      CHAT_LOG_LOCK = new Object();
      CHAT_LOG = new ArrayDeque<>();
      GUI_TITLE_HISTORY_LOCK = new Object();
      GUI_TITLE_HISTORY = new ArrayDeque<>();
      chatLogServerKey = "";
      globalPacketWaitFuture = null;
      globalChatWaitFuture = null;
      NAVIGATION_OWNER = new AtomicReference<>();
      TRACKED_AI_OPERATIONS = new CopyOnWriteArrayList<>();
      SMOOTH_LOOK_ACTIVE = new AtomicBoolean(false);
      SMOOTH_LOOK_OWNER = new AtomicReference<>();
      SMOOTH_LOOK_LOCK = new Object();
      GLOBAL_HELD_ACTIONS = new CopyOnWriteArrayList<>();
      GLOBAL_HELD_MOVES = new ConcurrentHashMap<>();
      ACTIVE_STEP_SLICE = new ThreadLocal<>();
      SLICE_STEPS_CONSUMED = ThreadLocal.withInitial(() -> 0);
      PENDING_AI_CANCELLATIONS = new AtomicInteger();
      runsView = List.of();
      RUN_OUTCOMES = new LinkedHashMap<>();
      PACKET_TARGET_CACHE = new ConcurrentHashMap<>();
      EMPTY_PACKET_TARGET = new MacroExecutor.ServerPacketCacheEntry("", "", "");
      COMPLETION_CACHE = new ConcurrentHashMap<>();
      PENDING_SUGGESTIONS = new ConcurrentHashMap<>();
      COMMAND_PROBE_QUEUES = new ConcurrentHashMap<>();
      MAIN_THREAD_BARRIER = new ThreadLocal<>();
      MAIN_THREAD_BURST_COUNT = ThreadLocal.withInitial(() -> new int[1]);
      TICK_ALIGNED_TASKS = new ConcurrentLinkedQueue<>();
      WAITER_TIMEOUT_NANOS = TimeUnit.MINUTES.toNanos(10L);
      INCOMING_PACKET_WAITERS = new CopyOnWriteArrayList<>();
      OUTGOING_PACKET_WAITERS = new CopyOnWriteArrayList<>();
      INCOMING_EVENT_WAITERS = new CopyOnWriteArrayList<>();
      OUTGOING_EVENT_WAITERS = new CopyOnWriteArrayList<>();
      lastSeenServerKey = "";
   }

   private record DragClickStep(int slotId, int button, ContainerInput input) {
   }

   private record CompletionCacheEntry(List<String> suggestions, Object connection) {
   }

   private static final class AsyncActionHandle {
      final MacroAction action;
      final CompletableFuture<?> future;
      final Runnable cleanup;
      final int completedPacketTargets;

      AsyncActionHandle(MacroAction action, CompletableFuture<?> future, Runnable cleanup, int completedPacketTargets) {
         this.action = action;
         this.future = future;
         this.cleanup = cleanup == null ? () -> {} : cleanup;
         this.completedPacketTargets = Math.max(0, completedPacketTargets);
      }

      void runCleanup() {
         try {
            this.cleanup.run();
         } catch (Throwable var2) {
         }
      }
   }

   private record SlotMatchFlags(int status, boolean instant, boolean load) {
   }

   private static final class QuickCraftPlan {
      final boolean valid;
      final int quickCraftType;
      final int placeCount;
      final int cost;
      final Map<Integer, Integer> residualCounts;
      final Map<Integer, Integer> expectedTargetCounts;

      QuickCraftPlan(
         boolean valid, int quickCraftType, int placeCount, int cost, Map<Integer, Integer> residualCounts, Map<Integer, Integer> expectedTargetCounts
      ) {
         this.valid = valid;
         this.quickCraftType = quickCraftType;
         this.placeCount = placeCount;
         this.cost = cost;
         this.residualCounts = residualCounts == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(residualCounts));
         this.expectedTargetCounts = expectedTargetCounts == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(expectedTargetCounts));
      }

      static MacroExecutor.QuickCraftPlan invalid() {
         return new MacroExecutor.QuickCraftPlan(false, 0, 0, 2147483647, Map.of(), Map.of());
      }
   }

   private record ChatCapture(String sender, String message, String displayText, Component displayComponent, MacroExecutor.ChatSenderKind senderKind) {
   }

   private static final class RunExecutionContext implements MacroExecutionContext {
      private final MacroExecutor.MacroRun run;

      private RunExecutionContext(MacroExecutor.MacroRun run) {
         this.run = run;
      }

      @Override
      public Minecraft mc() {
         return Minecraft.getInstance();
      }

      @Override
      public void runOnClientThread(Runnable r) {
         if (r != null) {
            Runnable safe = () -> MacroExecutor.runWithCurrentRun(
               this.run,
               () -> {
                  try {
                     r.run();
                  } catch (Throwable t) {
                     AutismClientMessaging.sendPrefixed(
                        "§cAddon action (client task) error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage())
                     );
                  }
               }
            );
            MacroExecutor.runOnClientThread(Minecraft.getInstance(), safe);
         }
      }

      @Override
      public <T> T callOnClientThread(Supplier<T> s) {
         if (s == null) {
            return null;
         }

         Supplier<T> safe = () -> {
            AtomicReference<T> value = new AtomicReference<>();
            MacroExecutor.runWithCurrentRun(
               this.run,
               () -> {
                  try {
                     value.set(s.get());
                  } catch (Throwable t) {
                     AutismClientMessaging.sendPrefixed(
                        "§cAddon action (client task) error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage())
                     );
                  }
               }
            );
            return value.get();
         };
         return MacroExecutor.callOnClientThread(Minecraft.getInstance(), safe, null);
      }

      @Override
      public void waitTicks(int ticks) {
         MacroExecutor.runWithCurrentRun(this.run, () -> {
            for (int i = 0; i < ticks && MacroExecutor.isStillRunning(); i++) {
               MacroExecutor.awaitWhileRunning(MacroWaitRegistry.awaitTickSync());
            }
         });
      }

      @Override
      public void awaitCondition(CompletableFuture<Void> future) {
         if (future != null) {
            MacroExecutor.runWithCurrentRun(this.run, () -> MacroExecutor.awaitWhileRunning(future));
         }
      }

      @Override
      public void awaitCondition(AddonCondition condition) {
         if (condition != null) {
            MacroExecutor.runWithCurrentRun(this.run, () -> MacroExecutor.awaitWhileRunning(MacroWaitRegistry.waitForAddonCondition(condition)));
         }
      }

      @Override
      public boolean isActive() {
         return this.run != null && this.run.running.get() && !autismclient.modules.aD.read_Z();
      }

      @Override
      public void setStatus(String status) {
         MacroExecutor.runWithCurrentRun(this.run, () -> MacroExecutor.setRunStatus(status));
      }

      @Override
      public void sendPacket(Packet<?> packet) {
         if (packet != null) {
            this.runOnClientThread(() -> {
               Minecraft mc = Minecraft.getInstance();
               if (mc.getConnection() != null) {
                  mc.getConnection().send(packet);
               }
            });
         }
      }

      @Override
      public Optional<MacroVarValue> variable(String name) {
         return this.run == null ? Optional.empty() : this.run.variables.lookup(name);
      }

      @Override
      public void setVariable(String name, MacroVarValue value) {
         if (this.run != null) {
            this.run.variables.set(name, value);
         }
      }

      @Override
      public void removeVariable(String name) {
         if (this.run != null) {
            this.run.variables.remove(name);
         }
      }

      @Override
      public Map<String, MacroVarValue> variables() {
         return this.run == null ? Map.of() : this.run.variables.snapshot();
      }

      @Override
      public MacroTemplateResolver.ResolveResult resolveTemplate(String template) {
         return MacroTemplateResolver.resolve(template, this.run == null ? null : this.run.variables, Minecraft.getInstance());
      }
   }

   public static final class PacketWaiter {
      public final CompletableFuture<Packet<?>> future = new CompletableFuture<>();
      private final List<MacroExecutor.PacketWaiter> registry;
      private final Predicate<Packet<?>> matcher;
      private final long deadlineNanos;

      PacketWaiter(List<MacroExecutor.PacketWaiter> registry, Predicate<Packet<?>> match) {
         this.registry = registry;
         this.matcher = match;
         this.deadlineNanos = System.nanoTime() + MacroExecutor.WAITER_TIMEOUT_NANOS;
         registry.add(this);
      }

      boolean tryMatch(Packet<?> packet, long nowNanos) {
         if (this.future.isDone()) {
            return true;
         } else if (nowNanos - this.deadlineNanos >= 0L) {
            this.future.cancel(true);
            return true;
         } else if (this.matcher.test(packet)) {
            this.future.complete(packet);
            return true;
         } else {
            return false;
         }
      }

      public boolean isDone() {
         return this.future.isDone();
      }

      public Packet<?> getPacketNow() {
         if (!this.future.isDone()) {
            return null;
         }

         try {
            return this.future.getNow(null);
         } catch (Throwable ignored) {
            return null;
         }
      }

      public void cancel() {
         this.registry.remove(this);
         if (!this.future.isDone()) {
            this.future.cancel(true);
         }
      }
   }

   private static final class PacketMatchWaiter {
      public final CompletableFuture<MacroExecutor.PacketEvent> future = new CompletableFuture<>();
      private final List<MacroExecutor.PacketMatchWaiter> registry;
      private final MacroExecutor.PacketEventMatcher matcher;
      private final long deadlineNanos;

      PacketMatchWaiter(List<MacroExecutor.PacketMatchWaiter> registry, MacroExecutor.PacketEventMatcher match) {
         this.registry = registry;
         this.matcher = match;
         this.deadlineNanos = System.nanoTime() + MacroExecutor.WAITER_TIMEOUT_NANOS;
         registry.add(this);
      }

      boolean offer(MacroExecutor.PacketEvent event) {
         if (this.future.isDone()) {
            return true;
         } else if (event.nanoTime() - this.deadlineNanos >= 0L) {
            this.future.cancel(true);
            return true;
         } else if (this.matcher.test(event.packet(), event.direction(), event.nanoTime())) {
            this.future.complete(event);
            return true;
         } else {
            return false;
         }
      }

      public void dispose() {
         this.registry.remove(this);
         if (!this.future.isDone()) {
            this.future.cancel(true);
         }
      }
   }

   private record ServerPacketCacheEntry(String direction, String expectedName, String normalizedName) {
   }

   public record ChatHistoryEntry(String title, String screenType, long timestampMs) {
   }

   private record ActiveStepSlice(autismclient.util.AutismMacro macro, int nextIndex, int endIndex) {
   }

   private static final class CaptureSessionState {
      private String lockedItemId = "";
      private int slot = -1;
      private int originalHotbarSlot = 0;
      private boolean slotLocked = false;
      private boolean itemSeen = false;
      private boolean completed = false;
      private MacroItemTarget itemTarget = new MacroItemTarget();
   }

   private interface PacketEventMatcher {
      boolean test(Packet<?> var1, String var2, long var3);
   }

   private record SlotRangePair(int slot, int remainingDurability) {
   }

   public record LoggedChatMessage(
      String sender, String message, String displayText, Component displayComponent, MacroExecutor.ChatSenderKind source, long timestampMs
   ) {
   }

   public record ExecutionFlags(boolean silent, boolean hiddenFromHud, boolean suppressLanSync, boolean allowDuplicateName) {
      public static final MacroExecutor.ExecutionFlags DEFAULT = new MacroExecutor.ExecutionFlags(false, false, false, false);

      public static MacroExecutor.ExecutionFlags background() {
         return new MacroExecutor.ExecutionFlags(true, true, true, true);
      }
   }

   private static final class TargetBlockScanner {
      private static final int SCAN_RADIUS = 8;
      private final Set<Block> targetBlocks;
      private final HashMap<Long, Boolean> trackedTargets = new HashMap<>();
      private final HashSet<Long> brokenPositions = new HashSet<>();
      private int brokenTargetCount;

      private TargetBlockScanner(Set<Block> targetBlocks) {
         this.targetBlocks = targetBlocks;
      }

      static MacroExecutor.TargetBlockScanner fromRawTargets(Minecraft mc, List<String> rawTargets) {
         Set<Block> blocks = new LinkedHashSet<>();
         if (rawTargets != null) {
            for (String raw : rawTargets) {
               Block block = parseTargetBlock(raw);
               if (block != null && block != Blocks.AIR) {
                  blocks.add(block);
               }
            }
         }

         return blocks.isEmpty() ? null : new MacroExecutor.TargetBlockScanner(blocks);
      }

      private static Block parseTargetBlock(String raw) {
         if (raw != null && !raw.isBlank()) {
            String value = raw.trim();

            try {
               Identifier id = value.contains(":") ? Identifier.parse(value) : Identifier.fromNamespaceAndPath("minecraft", value);
               return !BuiltInRegistries.BLOCK.containsKey(id) ? null : BuiltInRegistries.BLOCK.getValue(id);
            } catch (Throwable ignored) {
               return null;
            }
         } else {
            return null;
         }
      }

      synchronized int brokenCount() {
         return this.brokenTargetCount;
      }

      synchronized void scanAroundPlayer(Minecraft mc) {
         if (mc != null && mc.player != null && mc.level != null) {
            BlockPos center = mc.player.blockPosition();
            int minY = Math.max(mc.level.getMinY(), center.getY() - 8);
            int maxY = Math.min(mc.level.getMaxY() - 1, center.getY() + 8);

            for (int x = center.getX() - 8; x <= center.getX() + 8; x++) {
               for (int y = minY; y <= maxY; y++) {
                  for (int z = center.getZ() - 8; z <= center.getZ() + 8; z++) {
                     BlockPos pos = new BlockPos(x, y, z);
                     this.updateTrackedTarget(pos, mc.level.getBlockState(pos));
                  }
               }
            }
         }
      }

      synchronized void onBlockPacket(Minecraft mc, Packet<?> packet) {
         if (mc != null && mc.level != null && packet != null) {
            if (packet instanceof ClientboundBlockUpdatePacket blockUpdate) {
               this.onBlockUpdate(mc, blockUpdate.getPos(), blockUpdate.getBlockState());
            } else if (packet instanceof ClientboundSectionBlocksUpdatePacket sectionUpdate) {
               try {
                  sectionUpdate.runUpdates((pos, state) -> {
                     if (pos != null && state != null) {
                        this.onBlockUpdate(mc, pos, state);
                     }
                  });
               } catch (Throwable var6) {
               }
            }
         }
      }

      private void onBlockUpdate(Minecraft mc, BlockPos pos, BlockState newState) {
         if (mc != null && mc.level != null && pos != null && newState != null && this.isWithinTrackingRadius(mc, pos)) {
            BlockPos stablePos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            BlockState oldState = mc.level.getBlockState(stablePos);
            if (this.isTargetBlock(oldState) && !this.isTargetBlock(newState)) {
               this.recordBrokenTarget(stablePos.asLong());
            }

            this.updateTrackedTarget(stablePos, newState);
         }
      }

      private boolean isWithinTrackingRadius(Minecraft mc, BlockPos pos) {
         if (mc != null && mc.player != null && pos != null) {
            BlockPos playerPos = mc.player.blockPosition();
            int radius = 12;
            return Math.abs(pos.getX() - playerPos.getX()) <= radius
               && Math.abs(pos.getY() - playerPos.getY()) <= radius
               && Math.abs(pos.getZ() - playerPos.getZ()) <= radius;
         } else {
            return false;
         }
      }

      private void updateTrackedTarget(BlockPos pos, BlockState state) {
         if (pos != null && state != null) {
            long key = pos.asLong();
            boolean nowTarget = this.isTargetBlock(state);
            boolean wasTarget = Boolean.TRUE.equals(this.trackedTargets.get(key));
            if (nowTarget) {
               this.trackedTargets.put(key, Boolean.TRUE);
            } else {
               if (wasTarget) {
                  this.recordBrokenTarget(key);
               }

               this.trackedTargets.remove(key);
            }
         }
      }

      private boolean isTargetBlock(BlockState state) {
         return state != null && this.targetBlocks.contains(state.getBlock());
      }

      private void recordBrokenTarget(long posKey) {
         if (this.brokenPositions.add(posKey)) {
            this.brokenTargetCount++;
         }
      }
   }

   private static final class TransferPlan {
      final boolean valid;
      final ItemStack sourceStack;
      final int sourceCount;
      final Map<Integer, Integer> startTargetCounts;
      final Map<Integer, Integer> expectedTargetCounts;

      TransferPlan(
         boolean valid, ItemStack sourceStack, int sourceCount, Map<Integer, Integer> startTargetCounts, Map<Integer, Integer> expectedTargetCounts
      ) {
         this.valid = valid;
         this.sourceStack = sourceStack == null ? ItemStack.EMPTY : sourceStack;
         this.sourceCount = sourceCount;
         this.startTargetCounts = startTargetCounts == null ? Map.of() : Map.copyOf(startTargetCounts);
         this.expectedTargetCounts = expectedTargetCounts == null ? Map.of() : Map.copyOf(expectedTargetCounts);
      }

      MacroExecutor.TransferPlan withExpectedTargetCounts(Map<Integer, Integer> expectedTargetCounts) {
         return new MacroExecutor.TransferPlan(this.valid, this.sourceStack, this.sourceCount, this.startTargetCounts, expectedTargetCounts);
      }

      static MacroExecutor.TransferPlan invalid() {
         return new MacroExecutor.TransferPlan(false, ItemStack.EMPTY, 0, Map.of(), Map.of());
      }
   }

   private record ActionSliceResult(List<MacroAction> init_List, int lastIndex) {
      static MacroExecutor.ActionSliceResult emptySlice() {
         return new MacroExecutor.ActionSliceResult(List.of(), -1);
      }
   }

   private record StepMatchFlags(int status, boolean breaksThisTick, boolean load) {
   }

   private record PacketEvent(
      Packet<?> packet, String init_Str, long nanoTime, MacroExecutor.ChatCapture chatCapture, Map<String, MacroVarValue> captures
   ) {
      PacketEvent(Packet<?> packet, String direction, long nanoTime, MacroExecutor.ChatCapture chatCapture) {
         this(packet, direction, nanoTime, chatCapture, Map.of());
      }

      PacketEvent {
         captures = captures == null ? Map.of() : Map.copyOf(captures);
      }

      String describeEvent() {
         if (this.packet == null) {
            return this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str : "client event";
         }

         String dir = this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str + " " : "";
         return dir + PacketNameResolver.directionalName(this.packet, this.init_Str == null ? "" : this.init_Str);
      }

      public String direction() {
         return this.init_Str;
      }
   }

   private record SkippedLegacyMultiStep(MacroAction original) implements MacroAction {
      @Override
      public void execute(Minecraft mc) {
      }

      @Override
      public CompoundTag toTag() {
         return this.original.toTag();
      }

      @Override
      public void fromTag(CompoundTag tag) {
         this.original.fromTag(tag);
      }

      @Override
      public String getDisplayName() {
         return this.original.getDisplayName() + " (skipped)";
      }

      @Override
      public String getIcon() {
         return this.original.getIcon();
      }

      @Override
      public boolean isEnabled() {
         return false;
      }
   }

   private static final class MacroRun {
      final long id;
      final autismclient.util.AutismMacro sourceMacro;
      final autismclient.util.AutismMacro macro;
      final AtomicBoolean running = new AtomicBoolean(true);
      final AtomicBoolean finished = new AtomicBoolean(false);
      volatile String outcome = "CANCELLED";
      final CopyOnWriteArrayList<MacroAction> heldActions = new CopyOnWriteArrayList<>();
      final ConcurrentHashMap<MoveAction, int[]> heldMoves = new ConcurrentHashMap<>();
      final CopyOnWriteArrayList<MacroAction> finallyActions = new CopyOnWriteArrayList<>();
      final Object packetWaitLock = new Object();
      final AtomicBoolean waitingForPacket = new AtomicBoolean(false);
      final AtomicReference<String> packetWaitTarget = new AtomicReference<>("");
      volatile CompletableFuture<Void> packetWaitFuture = null;
      volatile Packet<?> matchedPacket = null;
      volatile String matchedPacketDirection = "";
      final AtomicBoolean waitingForChat = new AtomicBoolean(false);
      volatile WaitForChatAction chatWaitAction = null;
      volatile CompletableFuture<Void> chatWaitFuture = null;
      volatile AiOperation aiOperation = null;
      volatile AiOperation.Result aiOperationResult = null;
      volatile MacroExecutor.TargetBlockScanner mineTracker = null;
      volatile Thread thread = null;
      volatile String statusText = "";
      volatile int currentStep = -1;
      volatile int totalSteps = 0;
      volatile boolean rejoining = false;
      volatile int lastCompletedStep = 0;
      volatile int elseBranchIndex = -1;
      volatile int elseSkipSteps = 0;
      volatile int fpsOverrideStart = -1;
      volatile int fpsOverrideEnd = -1;
      volatile int pingSpoofOverrideStart = -1;
      volatile int pingSpoofOverrideEnd = -1;
      volatile long fpsTimeoutDeadlineNanos = 0L;
      volatile long spingTimeoutDeadlineNanos = 0L;
      volatile Map<String, Integer> labelIndex = null;
      volatile int backwardJumps = 0;
      volatile int backwardJumpBurst = 0;
      final IdentityHashMap<MacroAction, MacroExecutor.AsyncActionHandle> asyncWaits = new IdentityHashMap<>();
      final Map<MacroAction, Map<String, MacroVarValue>> capturedValues = Collections.synchronizedMap(new IdentityHashMap<>());
      final Map<MacroAction, CandidatePoolSelector.SelectionState> candidatePools = Collections.synchronizedMap(new IdentityHashMap<>());
      final MacroExecutor.ExecutionFlags flags;
      final MacroVariableStore variables = new MacroVariableStore();

      MacroRun(long id, autismclient.util.AutismMacro sourceMacro, autismclient.util.AutismMacro macro, MacroExecutor.ExecutionFlags options) {
         this.id = id;
         this.sourceMacro = sourceMacro;
         this.macro = macro;
         this.flags = options == null ? MacroExecutor.ExecutionFlags.DEFAULT : options;
         this.totalSteps = macro != null ? macro.actions.size() : 0;
      }
   }

   public enum ChatSenderKind {
      PLAYER,
      SERVER;
   }

   private record SuggestionFutureEntry(CompletableFuture<List<String>> future, Object connection) {
   }

   private static final class CommandProbeQueue {
      final Object connection;
      final LinkedHashSet<String> collected = new LinkedHashSet<>();
      final ArrayDeque<String> pendingPrefixes = new ArrayDeque<>();
      int nextCharIndex;
      int largestSuggestionBatch;

      CommandProbeQueue(Object connection, String baseQuery) {
         this.connection = connection;
         this.pendingPrefixes.add(baseQuery);
      }
   }

   public record ActiveRunSnapshot(autismclient.util.AutismMacro macro, String macroName, String statusText, int currentStep, int totalSteps, int lastCompletedStep) {
   }

   public record MacroRunOutcome(long id, String outcome, String status, int lastCompletedStep) {
   }
}
