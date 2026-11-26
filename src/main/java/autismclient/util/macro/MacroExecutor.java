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
   private static final Gson init_Gson = new Gson();
   private static final long init_J = 500000L;
   private static final long load_J = 300L;
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   public static final int init_I = 4;
   private static final Object init_Obj = new Object();
   private static final EnumMap<MacroResourceClassifier.Inner_DataUtil, ReentrantLock> init_EnumMap = new EnumMap<>(MacroResourceClassifier.Inner_DataUtil.class);
   private static final MacroResourceLock init_e = new MacroResourceLock();
   private static final AtomicLong init_AtomicLong;
   private static final LinkedHashMap<Long, MacroExecutor.Inner_TableIndex> init_LinkedHashMap;
   private static final ConcurrentHashMap<Long, MacroExecutor.Inner_TableIndex> init_ConcurrentHashMap;
   private static final AtomicLong load_AtomicLong;
   private static final ThreadLocal<MacroExecutor.Inner_TableIndex> init_ThreadLocal;
   private static final ThreadLocal<Boolean> load_ThreadLocal;
   private static volatile MacroExecutor.Inner_TableIndex init_MacroExecutor_Tabl;
   private static volatile boolean init_Z;
   private static autismclient.util.AutismMacro init_aM;
   private static Thread init_Thread;
   private static final AtomicReference<String> init_AtomicReference;
   private static String init_Str;
   private static volatile int load_I;
   private static volatile int read_I;
   private static final AtomicBoolean load_AtomicBoolean;
   private static final AtomicReference<String> load_AtomicReference;
   private static final Object load_Obj;
   private static final AtomicBoolean read_AtomicBoolean;
   private static final AtomicReference<String> read_AtomicReference;
   private static final AtomicBoolean write_AtomicBoolean;
   private static final AtomicReference<String> write_AtomicReference;
   private static final AtomicInteger init_AtomicInteger;
   private static final AtomicReference<String> update_AtomicReference;
   private static final Object read_Obj;
   private static final Deque<MacroExecutor.Inner_PacketUtil> init_Deque;
   private static final int write_I = 256;
   private static final Object write_Obj;
   private static final Deque<MacroExecutor.Inner_HashUtil> load_Deque;
   private static final int update_I = 128;
   private static String load_Str;
   private static volatile CompletableFuture<Void> init_CompletableFuture;
   private static volatile CompletableFuture<Void> load_CompletableFuture;
   private static final AtomicReference<MacroExecutor.Inner_TableIndex> sync_AtomicReference;
   private static final CopyOnWriteArrayList<AiOperation> init_CopyOnWriteArrayLi;
   private static final AtomicBoolean update_AtomicBoolean;
   private static final AtomicReference<MacroExecutor.Inner_TableIndex> flush_AtomicReference;
   private static final Object update_Obj;
   private static volatile float init_F;
   private static volatile float load_F;
   private static volatile double init_D;
   private static volatile int sync_I;
   private static final CopyOnWriteArrayList<MacroAction> load_CopyOnWriteArrayLi;
   private static final ConcurrentHashMap<MoveAction, int[]> load_ConcurrentHashMap;
   private static final ThreadLocal<MacroExecutor.Inner_IoWorker> read_ThreadLocal;
   private static final ThreadLocal<Integer> write_ThreadLocal;
   private static final int flush_I = 100000;
   private static final int close = 5000000;
   private static final AtomicInteger load_AtomicInteger;
   private static volatile List<MacroExecutor.Inner_TableIndex> init_List;
   private static final LinkedHashMap<Long, MacroExecutor.MacroRunOutcome> load_LinkedHashMap;
   private static final ConcurrentHashMap<String, MacroExecutor.Inner_FileCache> read_ConcurrentHashMap;
   private static final MacroExecutor.Inner_FileCache init_MacroExecutor_File;
   private static final ConcurrentHashMap<String, MacroExecutor.Inner_BufferHelper> write_ConcurrentHashMap;
   private static final ConcurrentHashMap<String, MacroExecutor.Inner_TextFormat> update_ConcurrentHashMap;
   private static final int open = 256;
   private static final long read_J = 3000L;
   private static final int poll = 1000;
   private static final int push = 8;
   private static final int pop = 60000;
   private static final String read_Str = "abcdefghijklmnopqrstuvwxyz0123456789_";
   private static volatile long write_J;
   private static final ConcurrentHashMap<String, MacroExecutor.Inner_TokenStore> sync_ConcurrentHashMap;
   private static final int tick = 256;
   private static final ThreadLocal<CompletableFuture<Void>> update_ThreadLocal;
   private static final ThreadLocal<int[]> sync_ThreadLocal;
   private static final ConcurrentLinkedQueue<Runnable> init_ConcurrentLinkedQu;
   private static volatile long update_J;
   private static int run;
   private static final long sync_J = 10000L;
   private static final long flush_J;
   private static final List<MacroExecutor.Inner_EventBus> load_List;
   private static final List<MacroExecutor.Inner_EventBus> read_List;
   private static final List<MacroExecutor.Inner_FieldCache> write_List;
   private static final List<MacroExecutor.Inner_FieldCache> update_List;
   private static volatile String write_Str;

   private static Map<String, Integer> init_Map_MacroExecutor_Tabl_aM(MacroExecutor.Inner_TableIndex run, autismclient.util.AutismMacro macro) {
      Map<String, Integer> cached = run.init_Map;
      if (cached != null) {
         return cached;
      }

      LinkedHashMap<String, Integer> built = new LinkedHashMap<>();

      for (int k = 0; k < macro.init_List.size(); k++) {
         if (macro.init_List.get(k) instanceof LabelAction la) {
            String key = la.init();
            if (!key.isEmpty() && !built.containsKey(key)) {
               built.put(key, k);
            }
         }
      }

      run.init_Map = built;
      return built;
   }

   private static Integer init_Integer_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run, autismclient.util.AutismMacro macro, int i, FlowAction flow, Minecraft mc) {
      Integer var10000;
      switch (flow.target) {
         case STOP:
            var10000 = null;
            break;
         case TOP:
            var10000 = 0;
            break;
         case END:
            var10000 = macro.init_List.size();
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
            String name = LabelAction.init(MacroVariableContext.load(flow.labelName, mc));
            Integer idx = init_Map_MacroExecutor_Tabl_aM(run, macro).get(name);
            if (idx == null) {
               AutismClientMessaging.sendPrefixed("§eFlow: label '" + flow.labelName + "' not found.");
               var10000 = flow.onMissingLabel == FlowAction.Inner_DataUtil.STOP ? null : i + 1;
            } else {
               var10000 = idx;
            }
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int init_I_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run, autismclient.util.AutismMacro macro, int currentI, FlowAction flow, Minecraft mc) {
      run.write_I = -1;
      run.update = 0;
      Integer dest = init_Integer_MacroExecutor_Tabl(run, macro, currentI, flow, mc);
      if (dest == null) {
         return -2147483648;
      }

      int size = macro.init_List.size();
      int target = Math.max(0, Math.min(size, dest));
      if (target <= currentI) {
         run.poll++;
         run.push++;
         if (run.poll > 5000000) {
            AutismClientMessaging.sendPrefixed("§cMacro aborted: jump limit exceeded (runaway loop).");
            return -2147483648;
         }

         if (run.push >= 100000) {
            run.push = 0;

            try {
               init_V_CompletableFuture(MacroWaitRegistry.load_CompletableFuture());
            } catch (Exception var9) {
            }
         }
      } else {
         run.push = 0;
      }

      return target - 1;
   }

   private static boolean close_Z() {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run != null ? run.init_AtomicBoolean.get() : init_AtomicBoolean.get();
   }

   public static boolean init_Z() {
      return close_Z();
   }

   private static CopyOnWriteArrayList<MacroAction> init_CopyOnWriteArrayLi() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run != null ? run.init_CopyOnWriteArrayLi : load_CopyOnWriteArrayLi;
   }

   private static ConcurrentHashMap<MoveAction, int[]> init_ConcurrentHashMap() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run != null ? run.init_ConcurrentHashMap : load_ConcurrentHashMap;
   }

   private static void load_V_Str(String status) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         run.read_Str = status == null ? "" : status;
      }

      init_Str = status == null ? "" : status;
   }

   private static void init_V_I_I(int step, int total) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         run.init_I = step;
         run.load_I = total;
      }

      load_I = step;
      read_I = total;
   }

   private static void init_V_I(int stepOneBased) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         run.read_I = Math.max(run.read_I, stepOneBased);
      }
   }

   private static int init_I_aM(autismclient.util.AutismMacro macro, int headerIndex, int coveredRows) {
      int last = macro == null ? headerIndex : macro.init_List.size() - 1;
      long end = (long)headerIndex + Math.max(0, coveredRows);
      return (int)Math.max(headerIndex, Math.min(last, end));
   }

   private static void init_V_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run, int headerIndex, int endIndexInclusive, int fpsLimit) {
      if (run != null) {
         run.sync = headerIndex;
         run.flush = endIndexInclusive;
         MacroFpsOverrideManager.init_V_J_I(run.init_J, fpsLimit);
      }
   }

   private static void init_V_MacroExecutor_Tabl(
      MacroExecutor.Inner_TableIndex run, int headerIndex, int endIndexInclusive, int delayMs, boolean realIncoming, boolean realOutgoing
   ) {
      if (run != null) {
         run.close = headerIndex;
         run.open = endIndexInclusive;
         PingSpoofScheduler.init_V_J(run.init_J, delayMs, realIncoming, realOutgoing);
      }
   }

   private static void init_V_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run) {
      if (run != null && run.sync >= 0) {
         run.sync = -1;
         run.flush = -1;
         MacroFpsOverrideManager.init_V_J(run.init_J);
      }
   }

   private static void load_V_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run) {
      if (run != null && run.close >= 0) {
         run.close = -1;
         run.open = -1;
         PingSpoofScheduler.init(run.init_J);
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

   private static void read(MacroExecutor.Inner_TableIndex run) {
      if (run != null) {
         long deadline = Math.max(run.load_J, run.read_J);

         long remaining;
         while (close_Z() && (remaining = deadline - System.nanoTime()) > 0L) {
            sleep(Math.min(25L, remaining / 1000000L + 1L));
         }
      }
   }

   private static void init_V_MacroExecutor_Tabl_I(MacroExecutor.Inner_TableIndex run, int index) {
      if (run != null) {
         if (run.sync >= 0 && (index > run.flush || index <= run.sync)) {
            init_V_MacroExecutor_Tabl(run);
         }

         if (run.close >= 0 && (index > run.open || index <= run.close)) {
            load_V_MacroExecutor_Tabl(run);
         }
      }
   }

   private static void flush_V() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         run.init_AtomicBoolean.set(false);
      } else {
         read_V();
      }
   }

   static void init_V() {
      flush_V();
   }

   private static EnumSet<MacroResourceClassifier.Inner_DataUtil> init_EnumSet_MacroAction(MacroAction action) {
      return MacroResourceClassifier.init(action);
   }

   public static AutoCloseable init_AutoCloseable_Collection(Collection<String> names) {
      EnumSet<MacroResourceClassifier.Inner_DataUtil> requested = EnumSet.noneOf(MacroResourceClassifier.Inner_DataUtil.class);

      for (MacroResourceClassifier.Inner_DataUtil resource : MacroResourceClassifier.Inner_DataUtil.values()) {
         if (names.contains(resource.name())) {
            requested.add(resource);
         }
      }

      if (requested.contains(MacroResourceClassifier.Inner_DataUtil.INPUT)) {
         requested.addAll(EnumSet.of(MacroResourceClassifier.Inner_DataUtil.INVENTORY, MacroResourceClassifier.Inner_DataUtil.GUI));
      }

      if (init_Z_EnumSet(requested)) {
         return null;
      }

      AutoCloseable controller = init_e.init(requested);
      if (controller == null) {
         return null;
      }

      EnumSet<MacroResourceClassifier.Inner_DataUtil> acquired = EnumSet.noneOf(MacroResourceClassifier.Inner_DataUtil.class);

      for (MacroResourceClassifier.Inner_DataUtil resource : MacroResourceClassifier.Inner_DataUtil.values()) {
         if (requested.contains(resource)) {
            ReentrantLock lock = init_EnumMap.get(resource);
            if (lock.hasQueuedThreads() || !lock.tryLock()) {
               init_V_EnumSet(acquired);

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
            init_V_EnumSet(acquired);
            controller.close();
            if (requested.contains(MacroResourceClassifier.Inner_DataUtil.INPUT)) {
               init_V_Minecraft(Minecraft.getInstance());
            }
         }
      };
   }

   public static boolean init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil resource) {
      return init_e.init(resource);
   }

   private static boolean init_Z_EnumSet(EnumSet<MacroResourceClassifier.Inner_DataUtil> requested) {
      boolean input = requested.contains(MacroResourceClassifier.Inner_DataUtil.INPUT);
      boolean rotation = requested.contains(MacroResourceClassifier.Inner_DataUtil.ROTATION);
      boolean navigation = requested.contains(MacroResourceClassifier.Inner_DataUtil.NAVIGATION);
      if (!input && !rotation && !navigation || load_AtomicInteger.get() <= 0 && init_CopyOnWriteArrayLi.isEmpty()) {
         if (input && !load_ConcurrentHashMap.isEmpty()) {
            return true;
         }

         if (rotation && flush_AtomicReference.get() != null) {
            return true;
         }

         for (MacroExecutor.Inner_TableIndex run : write_List()) {
            if ((input || rotation || navigation) && run.init_AiOperation != null) {
               return true;
            }

            if (input && !run.init_ConcurrentHashMap.isEmpty()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private static EnumSet<MacroResourceClassifier.Inner_DataUtil> load_EnumSet_MacroAction(MacroAction action) {
      return init_EnumSet_EnumSet(init_EnumSet_MacroAction(action));
   }

   private static EnumSet<MacroResourceClassifier.Inner_DataUtil> init_EnumSet_EnumSet(EnumSet<MacroResourceClassifier.Inner_DataUtil> resources) {
      EnumSet<MacroResourceClassifier.Inner_DataUtil> acquired = EnumSet.noneOf(MacroResourceClassifier.Inner_DataUtil.class);

      for (MacroResourceClassifier.Inner_DataUtil resource : MacroResourceClassifier.Inner_DataUtil.values()) {
         if (resources.contains(resource)) {
            if (!close_Z()) {
               throw new RuntimeException("Macro stopped");
            }

            lock(init_EnumMap.get(resource));
            acquired.add(resource);
            if (!close_Z()) {
               throw new RuntimeException("Macro stopped");
            }
         }
      }

      return acquired;
   }

   private static EnumSet<MacroResourceClassifier.Inner_DataUtil> init_EnumSet_List(List<MacroAction> actions) {
      return MacroResourceClassifier.init(actions);
   }

   private static void init_V_EnumSet(EnumSet<MacroResourceClassifier.Inner_DataUtil> resources) {
      if (resources != null && !resources.isEmpty()) {
         MacroResourceClassifier.Inner_DataUtil[] ordered = MacroResourceClassifier.Inner_DataUtil.values();

         for (int i = ordered.length - 1; i >= 0; i--) {
            MacroResourceClassifier.Inner_DataUtil resource = ordered[i];
            if (resources.contains(resource)) {
               ReentrantLock lock = init_EnumMap.get(resource);
               if (lock.isHeldByCurrentThread()) {
                  lock.unlock();
               }
            }
         }
      }
   }

   static MacroVariableStore init_m() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run == null ? null : run.init_m;
   }

   private static void init_V_MacroExecutor_Tabl_Runnable(MacroExecutor.Inner_TableIndex run, Runnable task) {
      MacroExecutor.Inner_TableIndex previous = init_ThreadLocal.get();
      if (run != null) {
         init_ThreadLocal.set(run);
      }

      try {
         task.run();
      } finally {
         if (previous != null) {
            init_ThreadLocal.set(previous);
         } else {
            init_ThreadLocal.remove();
         }
      }
   }

   private static MacroExecutor.Inner_StateCache init_MacroExecutor_Stat_List_I(List<MacroAction> actions, int currentIndex) {
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
            ? MacroExecutor.Inner_StateCache.init_MacroExecutor_Stat()
            : new MacroExecutor.Inner_StateCache(List.copyOf(collected), lastIndex);
      } else {
         return MacroExecutor.Inner_StateCache.init_MacroExecutor_Stat();
      }
   }

   private static void close_V() {
      init_List = List.copyOf(init_LinkedHashMap.values());
   }

   private static List<MacroExecutor.Inner_TableIndex> write_List() {
      return init_List;
   }

   public static MacroExecutor.MacroRunOutcome init_MacroExecutor_Macr_J(long id) {
      synchronized (init_Obj) {
         return load_LinkedHashMap.get(id);
      }
   }

   private static void open_V() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && run.load_AtomicBoolean.compareAndSet(false, true)) {
         MacroToolBridge.init(run.init_J, !"COMPLETED".equals(run.init_Str));
         init_V_MacroExecutor_Tabl_Z(run, false);
         init_V_MacroExecutor_Tabl(run);
         load_V_MacroExecutor_Tabl(run);
         synchronized (init_Obj) {
            load_LinkedHashMap.put(run.init_J, new MacroExecutor.MacroRunOutcome(run.init_J, run.init_Str, run.read_Str, run.read_I));

            while (load_LinkedHashMap.size() > 128) {
               load_LinkedHashMap.remove(load_LinkedHashMap.keySet().iterator().next());
            }

            init_LinkedHashMap.remove(run.init_J);
            init_ConcurrentHashMap.remove(run.init_J);
            close_V();
            if (init_LinkedHashMap.isEmpty()) {
               write_J = System.currentTimeMillis();
            }

            load_AtomicLong.incrementAndGet();
            poll_V();
         }

         if (!init_AtomicBoolean.get()) {
            autismclient.util.BlockPreviewRenderer.load();
            MacroFpsOverrideManager.load_V();
            PingSpoofScheduler.load_V();
         }

         init_ThreadLocal.remove();
      }
   }

   private static boolean open_Z() {
      for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
         if (run.init_AtomicBoolean.get()) {
            return true;
         }
      }

      return false;
   }

   private static MacroExecutor.Inner_TableIndex init_MacroExecutor_Tabl() {
      for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
         if (run.init_AtomicBoolean.get() && !run.init_MacroExecutor_Pool.load()) {
            return run;
         }
      }

      return null;
   }

   private static int write_I() {
      int count = 0;

      for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
         if (run.init_AtomicBoolean.get()) {
            count++;
         }
      }

      return count;
   }

   static boolean init_Z_I(int activeCount) {
      return Math.max(0, activeCount) < 4;
   }

   private static void poll_V() {
      init_MacroExecutor_Tabl = init_MacroExecutor_Tabl();
      init_Z = init_MacroExecutor_Tabl != null;
      init_aM = init_MacroExecutor_Tabl != null ? init_MacroExecutor_Tabl.load_aM : null;
      init_Str = init_MacroExecutor_Tabl != null ? init_MacroExecutor_Tabl.read_Str : "";
      load_I = init_MacroExecutor_Tabl != null ? init_MacroExecutor_Tabl.init_I : -1;
      read_I = init_MacroExecutor_Tabl != null ? init_MacroExecutor_Tabl.load_I : 0;
      init_AtomicBoolean.set(open_Z());
   }

   private static boolean poll_Z() {
      synchronized (init_Obj) {
         return open_Z();
      }
   }

   public static boolean load_Z() {
      return init_Z;
   }

   public static boolean read_Z() {
      ReentrantLock inputLock = init_EnumMap.get(MacroResourceClassifier.Inner_DataUtil.INPUT);
      if (inputLock != null && inputLock.isLocked()) {
         return true;
      }

      if (!load_ConcurrentHashMap.isEmpty()) {
         return true;
      }

      for (MacroAction action : load_CopyOnWriteArrayLi) {
         if (action instanceof MoveAction || action instanceof JumpAction || action instanceof SneakAction || action instanceof SprintAction) {
            return true;
         }
      }

      synchronized (init_Obj) {
         for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
            if (run != null && run.init_AtomicBoolean.get()) {
               if (!run.init_ConcurrentHashMap.isEmpty()) {
                  return true;
               }

               for (MacroAction action : run.init_CopyOnWriteArrayLi) {
                  if (action instanceof MoveAction || action instanceof JumpAction || action instanceof SneakAction || action instanceof SprintAction) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private static void init_V_MacroExecutor_Tabl_2(MacroExecutor.Inner_TableIndex run, int step, int total, String macroName) {
      if (run == null || !run.init_MacroExecutor_Pool.read()) {
         try {
            autismclient.util.LanSyncManager sync = autismclient.util.LanSyncManager.init_aE();
            if (sync.write_Z()) {
               sync.init_V_I(step, total, macroName);
            }
         } catch (Exception var5) {
         }
      }
   }

   private static void init_V_MacroExecutor_Tabl_Z(MacroExecutor.Inner_TableIndex run, boolean interruptThread) {
      if (run != null) {
         MacroToolBridge.init(run.init_J, true);
         if (run.init_AtomicBoolean.getAndSet(false)) {
            load_AtomicLong.incrementAndGet();
         }

         run.init_CopyOnWriteArrayLi.clear();

         for (MacroExecutor.Inner_ByteReader wait : new ArrayList<>(run.init_IdentityHashMap.values())) {
            wait.init();
         }

         run.init_IdentityHashMap.clear();
         init_V_ConcurrentHashMap(run.init_ConcurrentHashMap, Minecraft.getInstance(), run);
         run.init_ConcurrentHashMap.clear();
         init_V_Minecraft(Minecraft.getInstance());
         synchronized (run.init_Obj) {
            if (run.init_CompletableFuture != null) {
               run.init_CompletableFuture.cancel(true);
            }

            run.init_CompletableFuture = null;
            run.read_AtomicBoolean.set(false);
            run.init_AtomicReference.set("");
         }

         CompletableFuture<Void> chatFuture = run.load_CompletableFuture;
         if (chatFuture != null) {
            chatFuture.cancel(true);
         }

         run.load_CompletableFuture = null;
         run.write_AtomicBoolean.set(false);
         run.init_au = null;
         run.init_AiOperation_Result = null;
         run.init_MacroExecutor_Rend = null;
         Minecraft mc = Minecraft.getInstance();
         PacketGateManager.init(run.init_J, mc == null ? null : mc.getConnection());
         MacroFpsOverrideManager.init_V_J(run.init_J);
         PingSpoofScheduler.init(run.init_J);
         flush(run);
         read(run, mc);
         Thread thread = run.init_Thread;
         if (interruptThread && thread != null && thread != Thread.currentThread()) {
            thread.interrupt();
         }
      }
   }

   private static void init_V_ConcurrentHashMap(ConcurrentHashMap<MoveAction, int[]> moves, Minecraft mc, MacroExecutor.Inner_TableIndex owner) {
      if (moves != null && !moves.isEmpty() && mc != null) {
         List<MoveAction> toRelease = new ArrayList<>(moves.keySet());
         mc.execute(() -> {
            for (MoveAction move : toRelease) {
               try {
                  init_V_MacroExecutor_Tabl(owner, move, mc);
               } catch (Exception var6) {
               }
            }
         });
      }
   }

   private static void init_V_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex owner, MoveAction move, Minecraft mc) {
      if (move != null && mc != null && !init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil.INPUT)) {
         boolean stillOwned = false;
         synchronized (init_Obj) {
            for (MacroExecutor.Inner_TableIndex active : init_LinkedHashMap.values()) {
               if (active != owner && active.init_AtomicBoolean.get()) {
                  for (MoveAction other : active.init_ConcurrentHashMap.keySet()) {
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
            for (MoveAction other : load_ConcurrentHashMap.keySet()) {
               if (other.direction == move.direction) {
                  stillOwned = true;
                  break;
               }
            }
         }

         if (!stillOwned) {
            move.init_V_3(mc);
         }
      }
   }

   private static void init_V_Minecraft(Minecraft mc) {
      if (!init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil.INPUT) && mc != null) {
         mc.execute(() -> {
            load_V_Minecraft(mc);
            read(mc);
         });
      }
   }

   private static void load_V_Minecraft(Minecraft mc) {
      if (!init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil.INPUT) && mc != null && mc.options != null && mc.options.keyShift != null) {
         boolean held = init_Z_Class(SneakAction.class);
         mc.options.keyShift.setDown(held);
      }
   }

   private static void read(Minecraft mc) {
      if (!init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil.INPUT) && mc != null) {
         boolean held = init_Z_Class(SprintAction.class);
         if (mc.options != null && mc.options.keySprint != null) {
            mc.options.keySprint.setDown(held);
         }

         if (mc.player != null) {
            mc.player.setSprinting(held);
         }
      }
   }

   private static boolean init_Z_Class(Class<? extends MacroAction> type) {
      for (MacroAction action : load_CopyOnWriteArrayLi) {
         if (type.isInstance(action)) {
            return true;
         }
      }

      synchronized (init_Obj) {
         for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
            if (run.init_AtomicBoolean.get()) {
               for (MacroAction action : run.init_CopyOnWriteArrayLi) {
                  if (type.isInstance(action)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private static String read_Str(String macroName) {
      return macroName == null ? "" : macroName.trim().toLowerCase(Locale.ROOT);
   }

   private static String init_Str_aM(autismclient.util.AutismMacro macro) {
      return macro != null && macro.init_Str != null ? macro.init_Str : "";
   }

   private static boolean init_Z_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run, autismclient.util.AutismMacro macro, String macroName) {
      if (run != null && run.init_AtomicBoolean.get()) {
         if (macro == null || run.init_aM != macro && run.load_aM != macro) {
            String targetKey = read_Str(macroName);
            if (targetKey.isEmpty() && macro != null) {
               targetKey = read_Str(macro.init_Str);
            }

            return !targetKey.isEmpty() && read_Str(init_Str_aM(run.load_aM)).equals(targetKey);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static void init_V_aM(autismclient.util.AutismMacro macro) {
      init_J_aM(macro);
   }

   public static long init_J_aM(autismclient.util.AutismMacro macro) {
      return init_J_aM_MacroExecutor_Pool(macro, MacroExecutor.Inner_PoolEntry.init_MacroExecutor_Pool);
   }

   static boolean suppressLegacyMultiSteps(List<MacroAction> actions) {
      boolean hasLegacyMulti = false;
      for (int index = 0; index < actions.size(); index++) {
         if (actions.get(index) instanceof MultiAction multi) {
            hasLegacyMulti = true;
            int lastCovered = index + multi.init(actions, index);
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

   public static long init_J_aM_MacroExecutor_Pool(autismclient.util.AutismMacro macro, MacroExecutor.Inner_PoolEntry options) {
      if (!AutismLiteVariant.enabled() && !autismclient.e.ByteReader.init()) {
         return -1L;
      }

      if (autismclient.modules.aD.read_Z()) {
         return -1L;
      }

      MacroExecutor.Inner_PoolEntry runOptions = options == null ? MacroExecutor.Inner_PoolEntry.init_MacroExecutor_Pool : options;
      if (macro != null && macro.init_List != null && !macro.init_List.isEmpty()) {
         autismclient.util.AutismMacro executionMacro;
         try {
            executionMacro = macro.init_aM();
         } catch (Throwable copyError) {
            if (!runOptions.init_Z()) {
               AutismClientMessaging.sendPrefixed("§cCould not prepare macro run.");
            }

            return -1L;
         }

         if (executionMacro != null && executionMacro.init_List != null && !executionMacro.init_List.isEmpty()) {
            // Keep skipped rows in the execution copy so grouped steps and jumps retain their indices.
            boolean hasLegacyMulti = suppressLegacyMultiSteps(executionMacro.init_List);
            MacroExecutor.Inner_TableIndex runState;
            synchronized (init_Obj) {
               String name = macro.init_Str == null ? "" : macro.init_Str;
               String nameKey = read_Str(name);
               if (runOptions.write()) {
                  if (!init_Z_I(write_I())) {
                     return -1L;
                  }
               } else {
                  for (MacroExecutor.Inner_TableIndex active : init_LinkedHashMap.values()) {
                     if (!active.init_MacroExecutor_Pool.write()) {
                        String activeName = active.load_aM != null && active.load_aM.init_Str != null ? active.load_aM.init_Str : "";
                        if (active.init_AtomicBoolean.get() && read_Str(activeName).equals(nameKey)) {
                           if (!runOptions.init_Z()) {
                              AutismClientMessaging.sendPrefixed("§eMacro already running: " + name);
                           }

                           return -1L;
                        }
                     }
                  }

                  if (!init_Z_I(write_I())) {
                     if (!runOptions.init_Z()) {
                        AutismClientMessaging.sendPrefixed("§cOnly 4 macros can run at the same time.");
                     }

                     return -1L;
                  }
               }

               runState = new MacroExecutor.Inner_TableIndex(init_AtomicLong.getAndIncrement(), macro, executionMacro, runOptions);
               if (init_LinkedHashMap.isEmpty()) {
                  push();
               }

               init_LinkedHashMap.put(runState.init_J, runState);
               init_ConcurrentHashMap.put(runState.init_J, runState);
               close_V();
               load_AtomicLong.incrementAndGet();
               poll_V();
            }

            if (!runOptions.init_Z()) {
               AutismClientMessaging.sendPrefixed("§aStarted macro: " + macro.init_Str);
            }

            init_V_MacroExecutor_Tabl_2(runState, 0, executionMacro.init_List.size(), executionMacro.init_Str);
            Thread thread = new Thread(() -> write(runState), "Autism-Macro-" + runState.init_J);
            thread.setDaemon(true);
            init_Thread = thread;
            runState.init_Thread = thread;

            try {
               thread.start();
            } catch (Throwable startError) {
               init_V_MacroExecutor_Tabl_Z(runState, false);
               synchronized (init_Obj) {
                  init_LinkedHashMap.remove(runState.init_J);
                  init_ConcurrentHashMap.remove(runState.init_J);
                  close_V();
                  if (init_LinkedHashMap.isEmpty()) {
                     write_J = System.currentTimeMillis();
                  }

                  load_AtomicLong.incrementAndGet();
                  poll_V();
                  return -1L;
               }
            }

            if (hasLegacyMulti) {
               String warning = "Legacy Multi blocks are unsupported and will be skipped in " + macro.init_Str + ".";
               if (runOptions.init_Z()) {
                  AutismClientAddon.LOG.info(warning);
               } else {
                  AutismClientMessaging.sendPrefixed("§e" + warning);
               }
            }
            return runState.init_J;
         } else {
            return -1L;
         }
      } else {
         if (runOptions.init_Z()) {
            return -1L;
         }

         AutismClientMessaging.sendPrefixed("§cMacro has no actions!");
         return -1L;
      }
   }

   public static boolean init_Z_J(long id) {
      if (id < 0L) {
         return false;
      }

      MacroExecutor.Inner_TableIndex run = init_ConcurrentHashMap.get(id);
      return run != null && run.init_AtomicBoolean.get();
   }

   public static long init_J() {
      return load_AtomicLong.get();
   }

   public static long load_J() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run == null ? -1L : run.init_J;
   }

   public static void init_V_J(long id) {
      if (id >= 0L) {
         MacroExecutor.Inner_TableIndex run;
         synchronized (init_Obj) {
            run = init_LinkedHashMap.get(id);
         }

         if (run != null) {
            init_V_MacroExecutor_Tabl_Z(run, true);
            synchronized (init_Obj) {
               poll_V();
            }
         }
      }
   }

   private static void write(MacroExecutor.Inner_TableIndex runState) {
      init_ThreadLocal.set(runState);

      try {
         update(runState);
      } catch (Throwable var8) {
         Throwable fatal = var8;
         if (!runState.init_MacroExecutor_Pool.init_Z()) {
            try {
               AutismClientMessaging.sendPrefixed("§cMacro crashed: " + fatal.getMessage());
            } catch (Throwable var8x) {
            }
         }
      } finally {
         open_V();
         init_ThreadLocal.remove();
      }
   }

   private static void update(MacroExecutor.Inner_TableIndex runState) {
      init_ThreadLocal.set(runState);
      MacroVariableContext.init_V();
      autismclient.util.AutismMacro macro = runState.load_aM;
      Minecraft mc = Minecraft.getInstance();
      int loopCount = 0;
      int maxLoops = macro.init_Z ? (macro.init_I == -1 ? 2147483647 : macro.init_I) : 1;
      init_CopyOnWriteArrayLi().clear();
      init_ConcurrentHashMap().clear();
      Thread persistentThread = new Thread(() -> {
         init_ThreadLocal.set(runState);

         try {
            while (close_Z() && (AutismLiteVariant.enabled() || autismclient.e.BitSetUtil.init())) {
               if (!init_CopyOnWriteArrayLi().isEmpty() || !init_ConcurrentHashMap().isEmpty()) {
                  MacroExecutor.Inner_TableIndex scheduledRunx = init_ThreadLocal.get();
                  mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(scheduledRunx, () -> {
                     if (close_Z() && !init_Z_DataPipe_DataUtil(MacroResourceClassifier.Inner_DataUtil.INPUT)) {
                        ReentrantLock inputLock = init_EnumMap.get(MacroResourceClassifier.Inner_DataUtil.INPUT);
                        if (inputLock.tryLock()) {
                           try {
                              for (MacroAction pa : init_CopyOnWriteArrayLi()) {
                                 try {
                                    pa.execute(mc);
                                 } catch (Exception var10x) {
                                 }
                              }

                              for (Entry<MoveAction, int[]> entry : init_ConcurrentHashMap().entrySet()) {
                                 MoveAction bma = entry.getKey();
                                 int[] ticks = entry.getValue();
                                 if (ticks[0] > 0) {
                                    bma.execute(mc);
                                    ticks[0]--;
                                 } else {
                                    init_V_MacroExecutor_Tabl(scheduledRunx, bma, mc);
                                    init_ConcurrentHashMap().remove(bma);
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
                  CompletableFuture<Void> tf = MacroWaitRegistry.load_CompletableFuture();
                  tf.get(100L, TimeUnit.MILLISECONDS);
               } catch (Exception e) {
                  if (!close_Z()) {
                     break;
                  }
               }
            }
         } finally {
            init_V_ConcurrentHashMap(init_ConcurrentHashMap(), mc, runState);
            init_ConcurrentHashMap().clear();
            init_ThreadLocal.remove();
         }
      }, "Autism-Macro-Persistent-" + runState.init_J);
      persistentThread.setDaemon(true);
      persistentThread.start();

      try {
         for (; close_Z() && loopCount < maxLoops; loopCount++) {
            if (macro.init_List.isEmpty()) {
               if (!runState.init_MacroExecutor_Pool.init_Z()) {
                  AutismClientMessaging.sendPrefixed("§cMacro stopped: no actions left.");
               }

               flush_V();
               break;
            }

            load_V_Str("Loop " + (loopCount + 1) + (maxLoops == 2147483647 ? "" : "/" + maxLoops));
            autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
            boolean burstMode = module.clear_Z();
            read_I = macro.init_List.size();
            runState.write_I = -1;
            runState.update = 0;
            runState.push = 0;
            runState.read_I = 0;

            for (int i = 0; i < macro.init_List.size() && close_Z(); i++) {
               if (!AutismLiteVariant.enabled() && !autismclient.e.RingBuffer.init()) {
                  flush_V();
                  break;
               }

               init_V_I_I(i, macro.init_List.size());
               init_V_MacroExecutor_Tabl_I(runState, i);
               if (runState.write_I == i && runState.update > 0) {
                  i += runState.update - 1;
                  runState.write_I = -1;
                  runState.update = 0;
               } else {
                  MacroAction action = macro.init_List.get(i);
                  if (action instanceof MultiAction multi) {
                     i += multi.init(macro.init_List, i);
                  } else if (!action.isEnabled()) {
                     if (action instanceof RaceAction disabledRace) {
                        i += disabledRace.init_I_List_I(macro.init_List, i);
                     } else if (action instanceof ReportAction disabledReport) {
                        i += disabledReport.init(macro.init_List, i);
                     }
                  } else if (DynamicBindingManager.init(action, mc) && init_Z_MacroAction_Minecraft(action, mc)) {
                     if (!load_Z_MacroAction(action)) {
                        load_V_MacroAction(action);
                     }

                     if (action instanceof BranchAction branch) {
                        boolean branchMatches = branch.init(mc);
                        if (branchMatches) {
                           runState.write_I = i + 1 + Math.max(0, branch.thenSteps);
                           runState.update = Math.max(0, branch.elseSteps);
                        } else {
                           i += Math.max(0, branch.thenSteps);
                        }
                     } else if (action instanceof FinallyAction cleanup) {
                        int bodyCount = Math.max(0, cleanup.bodyCount);
                        runState.load_CopyOnWriteArrayLi.clear();

                        for (int c = 1; c <= bodyCount && i + c < macro.init_List.size(); c++) {
                           MacroAction body = macro.init_List.get(i + c);
                           if (body != null && body.isEnabled()) {
                              runState.load_CopyOnWriteArrayLi.add(body);
                           }
                        }

                        i += bodyCount;
                     } else if (action instanceof IfAction ifA) {
                        runState.write_I = -1;
                        runState.update = 0;
                        if (ifA.init(mc)) {
                           runState.write_I = i + 1 + Math.max(0, ifA.thenSteps);
                           runState.update = Math.max(0, ifA.elseSteps);
                        } else {
                           i += Math.max(0, ifA.thenSteps);
                        }
                     } else if (!(action instanceof LabelAction)) {
                        if (action instanceof FlowAction flow) {
                           if (flow.init(mc)) {
                              int newI = init_I_MacroExecutor_Tabl(runState, macro, i, flow, mc);
                              if (newI == -2147483648) {
                                 init_V_MacroExecutor_Tabl_2(runState, i + 1, macro.init_List.size(), macro.init_Str);
                                 flush_V();
                                 break;
                              }

                              i = newI;
                           }
                        } else {
                           init_V_Minecraft_6(mc, macro.init_List, i);
                           EnumSet<MacroResourceClassifier.Inner_DataUtil> heldResources = load_EnumSet_MacroAction(action);

                           try {
                              if (read_Z(mc, action)) {
                                 load_V_Minecraft_MacroAction(mc, action);
                                 CompletableFuture<Void> postGuiFuture = !(action instanceof ItemAction) && !(action instanceof DropAction) && !write(action)
                                    ? write(mc, action)
                                    : null;
                                 if (burstMode && action instanceof SendPacketAction) {
                                    List<SendPacketAction> packetBatch = new ArrayList<>();

                                    int batchEnd;
                                    for (batchEnd = i; batchEnd < macro.init_List.size() && macro.init_List.get(batchEnd) instanceof SendPacketAction; batchEnd++) {
                                       packetBatch.add((SendPacketAction)macro.init_List.get(batchEnd));
                                    }

                                    init_V_Minecraft_List(mc, packetBatch);
                                    i = batchEnd - 1;
                                 } else {
                                    if (action instanceof ToolsAction tools) {
                                       init_V_Minecraft_al(mc, tools);
                                    } else if (action instanceof DelayAction da) {
                                       if (da.useTicks) {
                                          for (int t = 0; t < da.delayTicks && close_Z(); t++) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.load_CompletableFuture();
                                             init_V_CompletableFuture(future);
                                          }
                                       } else {
                                          sleep(da.delayMs);
                                       }
                                    } else if (action instanceof CustomMenuAction customMenu) {
                                       load_V_Str(customMenu.getDisplayName());
                                       customMenu.execute(mc);
                                    } else if (action instanceof FpsAction fps) {
                                       load_V_Str(fps.getDisplayName());
                                       if (fps.init_RingBuffer_DataUti() == FpsAction.Inner_DataUtil.TIMEOUT) {
                                          init_V_MacroExecutor_Tabl(runState);
                                          long fpsDurationNanos = fps.init_J_2() * 1000000L;
                                          long fpsDeadline = System.nanoTime() + fpsDurationNanos;
                                          MacroFpsOverrideManager.init(runState.init_J, Math.max(0, fps.maxFps), fpsDurationNanos);
                                          if (fps.continueNextActions) {
                                             runState.load_J = fpsDeadline;
                                          } else {
                                             long fpsRemaining;
                                             while (close_Z() && (fpsRemaining = fpsDeadline - System.nanoTime()) > 0L) {
                                                sleep(Math.min(25L, fpsRemaining / 1000000L + 1L));
                                             }

                                             MacroFpsOverrideManager.init_V_J(runState.init_J);
                                             runState.load_J = 0L;
                                          }
                                       } else {
                                          int covered = fps.init_RingBuffer_DataUti() == FpsAction.Inner_DataUtil.END
                                             ? macro.init_List.size() - 1 - i
                                             : fps.init_I_3();
                                          int endIndex = init_I_aM(macro, i, covered);
                                          init_V_MacroExecutor_Tabl(runState, i, endIndex, Math.max(1, fps.maxFps));
                                          runState.load_J = 0L;
                                       }
                                    } else if (action instanceof SPingAction sping) {
                                       load_V_Str(sping.getDisplayName());
                                       if (sping.init_T_DataUtil() == SPingAction.Inner_DataUtil.TIMEOUT) {
                                          load_V_MacroExecutor_Tabl(runState);
                                          long spingDurationNanos = sping.init_J_3() * 1000000L;
                                          long spingDeadline = System.nanoTime() + spingDurationNanos;
                                          PingSpoofScheduler.init(runState.init_J, Math.max(0, sping.pingDelayMs), sping.realIncoming, sping.realOutgoing, spingDurationNanos);
                                          if (sping.continueNextActions) {
                                             runState.read_J = spingDeadline;
                                          } else {
                                             long spingRemaining;
                                             while (close_Z() && (spingRemaining = spingDeadline - System.nanoTime()) > 0L) {
                                                sleep(Math.min(25L, spingRemaining / 1000000L + 1L));
                                             }

                                             PingSpoofScheduler.init(runState.init_J);
                                             runState.read_J = 0L;
                                          }
                                       } else {
                                          int covered = sping.init_T_DataUtil() == SPingAction.Inner_DataUtil.END ? macro.init_List.size() - 1 - i : sping.init_I_4();
                                          int endIndex = init_I_aM(macro, i, covered);
                                          init_V_MacroExecutor_Tabl(
                                             runState, i, endIndex, Math.max(0, sping.pingDelayMs), sping.realIncoming, sping.realOutgoing
                                          );
                                          runState.read_J = 0L;
                                       }
                                    } else if (action instanceof WaitForPacketAction packetAction) {
                                       packetAction.matchedPacket = null;
                                       packetAction.matchedDirection = "";
                                       MacroExecutor.Inner_TableIndex packetRun = init_ThreadLocal.get();
                                       if (packetRun != null) {
                                          packetRun.init_Packet = null;
                                          packetRun.load_Str = "";
                                       }

                                       List<String> targets = packetAction.init();
                                       MacroExecutor.Inner_ByteReader prearmed = init_MacroExecutor_Byte_MacroAction(action);
                                       int startTarget = 0;
                                       if (prearmed != null) {
                                          try {
                                             load_Z_CompletableFuture_J(prearmed.init_CompletableFuture, 0L);
                                             startTarget = prearmed.init_I;
                                          } finally {
                                             prearmed.init();
                                          }
                                       }

                                       if (targets.isEmpty()) {
                                          if (prearmed == null) {
                                             read_V("");
                                          }
                                       } else {
                                          for (int targetIndex = Math.min(startTarget, targets.size());
                                             targetIndex < targets.size() && close_Z();
                                             targetIndex++
                                          ) {
                                             read_V(targets.get(targetIndex));
                                          }
                                       }

                                       init_V_aB(packetAction);
                                    } else if (action instanceof WaitPacketMatchAction wpma) {
                                       load_V_Str(wpma.getDisplayName());
                                       if (!init_Z_MacroAction_J(wpma, wpma.timeoutMs)) {
                                          init_V_aM_2(wpma);
                                       }
                                    } else if (action instanceof WaitInventoryPredicateAction wipa) {
                                       load_V_Str(wipa.getDisplayName());
                                       if (!init_Z_MacroAction_J(wipa, wipa.timeoutMs)) {
                                          init_V_BooleanSupplier_I(() -> wipa.init(mc), wipa.timeoutMs);
                                       }
                                    } else if (action instanceof WaitDurabilityAction wda) {
                                       load_V_Str(wda.getDisplayName());
                                       if (wda.useNext) {
                                          init_V_Minecraft_ar(mc, wda);
                                       } else if (!init_Z_MacroAction_J(wda, wda.timeoutMs)) {
                                          init_V_BooleanSupplier_I(() -> wda.init(mc), wda.timeoutMs);
                                       }
                                    } else if (action instanceof WaitFreeSlotsAction wfsa) {
                                       load_V_Str(wfsa.getDisplayName());
                                       if (!init_Z_MacroAction_J(wfsa, wfsa.timeoutMs)) {
                                          init_V_BooleanSupplier_I(() -> wfsa.init(mc), wfsa.timeoutMs);
                                       }
                                    } else if (action instanceof WaitEntityTargetAction weta) {
                                       load_V_Str(weta.getDisplayName());
                                       if (!init_Z_MacroAction_J(weta, weta.timeoutMs)) {
                                          init_V_BooleanSupplier_I(() -> weta.init(mc), weta.timeoutMs);
                                       }
                                    } else if (action instanceof WaitGuiTypeAction wgta) {
                                       load_V_Str(wgta.getDisplayName());
                                       if (!load_Z_MacroAction(wgta)) {
                                          init_V_MacroAction(wgta);
                                       }

                                       if (!init_Z_MacroAction_J(wgta, wgta.timeoutMs)) {
                                          if (wgta.waitMode == WaitGuiTypeAction.Inner_DataUtil.CLOSE) {
                                             init_V_BooleanSupplier_I(init_BooleanSupplier_aJ_Minecraft(wgta, mc), wgta.timeoutMs);
                                             read_V(wgta);
                                          } else {
                                             init_V_BooleanSupplier_I(() -> {
                                                Optional<Map<String, MacroVarValue>> captured = wgta.init_Optional(mc);
                                                captured.ifPresent(values -> init_V_MacroAction_Map(wgta, (Map<String, MacroVarValue>)values));
                                                return captured.isPresent();
                                             }, wgta.timeoutMs);
                                             read_V(wgta);
                                          }
                                       }
                                    } else if (action instanceof WaitForHealthAction wh) {
                                       load_V_Str(wh.write());

                                       try {
                                          if (!init_Z_MacroAction_J(wh, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_F_Z(wh.healthThreshold, wh.below);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var248) {
                                       }
                                    } else if (action instanceof WaitForBlockAction wba) {
                                       load_V_Str(wba.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wba, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(wba);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var247) {
                                       }
                                    } else if (action instanceof WaitForGuiAction wga) {
                                       if (!load_Z_MacroAction(wga)) {
                                          init_V_MacroAction(wga);
                                       }

                                       if (!init_Z_MacroAction_J(wga, 0L)) {
                                          if (wga.waitMode == WaitForGuiAction.Inner_DataUtil.CLOSE) {
                                             load_V_Str("Waiting for GUI close: " + wga.guiTitle);
                                             init_V_BooleanSupplier_I(init_BooleanSupplier_ax_Minecraft(wga, mc), wga.timeoutMs);
                                             read_V(wga);
                                          } else {
                                             load_V_Str("Waiting for GUI: " + wga.guiTitle);
                                             init_V_BooleanSupplier_I(() -> {
                                                Optional<Map<String, MacroVarValue>> captured = wga.init(mc.gui.screen());
                                                captured.ifPresent(values -> init_V_MacroAction_Map(wga, (Map<String, MacroVarValue>)values));
                                                return captured.isPresent();
                                             }, wga.timeoutMs);
                                             read_V(wga);
                                          }
                                       }
                                    } else if (action instanceof WaitForCooldownAction wca) {
                                       MacroItemTarget cooldownTarget = init_ArrayUtil_ArrayUtil_Str(wca.itemTarget, wca.itemName).init_ArrayUtil_Minecraft(mc);
                                       if (cooldownTarget == null) {
                                          continue;
                                       }

                                       String cooldownLabel = init_Str_ArrayUtil(cooldownTarget);
                                       load_V_Str(
                                          "Waiting for cooldown: "
                                             + (!cooldownLabel.isEmpty() ? cooldownLabel : (wca.checkMainInteractionHand ? "Main Hand" : "Off Hand"))
                                       );

                                       try {
                                          if (!init_Z_MacroAction_J(wca, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_ArrayUtil_Z(cooldownTarget, wca.checkMainInteractionHand);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var246) {
                                       }
                                    } else if (action instanceof WaitPosAction wpa) {
                                       load_V_Str("Waiting for Pos: " + String.format("%.0f, %.0f, %.0f", wpa.f_x_D, wpa.f_y_D, wpa.f_z_D));

                                       try {
                                          if (!init_Z_MacroAction_J(wpa, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(
                                                wpa.f_x_D, wpa.f_y_D, wpa.f_z_D, wpa.leeway, wpa.checkRotation, wpa.yaw, wpa.pitch, wpa.rotLeeway
                                             );
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var245) {
                                       }
                                    } else if (action instanceof JumpAction ja) {
                                       mc.execute(() -> ja.execute(mc));
                                       if (ja.tap) {
                                          CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                                          init_V_CompletableFuture(tickFuture);
                                       } else {
                                          for (int t = 0; t < ja.durationTicks && close_Z(); t++) {
                                             CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                                             init_V_CompletableFuture(tickFuture);
                                          }
                                       }

                                       mc.execute(() -> ja.init(mc));
                                    } else if (action instanceof SneakAction sna) {
                                       if (sna.persistent && sna.sneak) {
                                          init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SneakAction);
                                          init_CopyOnWriteArrayLi().add(sna);
                                       } else if (!sna.sneak) {
                                          init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SneakAction);
                                       }

                                       if (!sna.persistent && sna.sneak) {
                                          load_V_Minecraft(mc, () -> sna.execute(mc), sna.h_());
                                       } else {
                                          load_V_Minecraft(mc, () -> load_V_Minecraft(mc), sna.h_());
                                       }
                                    } else if (action instanceof SprintAction spa) {
                                       if (spa.persistent && spa.sprint) {
                                          init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SprintAction);
                                          init_CopyOnWriteArrayLi().add(spa);
                                       } else if (!spa.sprint) {
                                          init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SprintAction);
                                       }

                                       if (!spa.persistent && spa.sprint) {
                                          load_V_Minecraft(mc, () -> spa.execute(mc), spa.h_());
                                       } else {
                                          load_V_Minecraft(mc, () -> read(mc), spa.h_());
                                       }
                                    } else if (action instanceof DisconnectAction da2) {
                                       init_V_MacroExecutor_Tabl_2(runState, i + 1, macro.init_List.size(), macro.init_Str);
                                       if (da2.mode == DisconnectAction.Inner_StreamUtil.DISCONNECT) {
                                          if (da2.delayMs > 0) {
                                             sleep(da2.delayMs);
                                          }

                                          mc.execute(() -> da2.execute(mc));
                                       } else if (da2.mode == DisconnectAction.Inner_StreamUtil.AUTO_DISCONNECT) {
                                          load_V_Str("Auto Disconnect: waiting for " + da2.trigger.name() + "...");
                                          da2.execute(mc);
                                          load_V_Str("Auto Disconnect: executed");
                                       } else if (da2.mode == DisconnectAction.Inner_StreamUtil.KICK_DUPE && da2.useNextAction) {
                                          List<MacroAction> nextActs = new ArrayList<>();
                                          int lastSkip = -1;

                                          for (int j = i + 1; j < macro.init_List.size(); j++) {
                                             MacroAction candidate = macro.init_List.get(j);
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

                                          da2.init(nextActs);
                                          mc.execute(() -> da2.execute(mc));
                                          if (lastSkip > i) {
                                             i = lastSkip;
                                          }
                                       } else {
                                          mc.execute(() -> da2.execute(mc));
                                       }

                                       if (!JoinAction.init(macro.init_List, i + 1, macro.init_Z)) {
                                          flush_V();
                                          break;
                                       }

                                       runState.init_Z = true;
                                       String statusBeforeLeave = runState.read_Str;
                                       load_V_Str("Leaving the server");
                                       JoinAction.init_V_Minecraft_J(mc, 10000L);
                                       load_V_Str(statusBeforeLeave);
                                    } else if (action instanceof JoinAction join) {
                                       String statusBefore = runState.read_Str;
                                       runState.init_Z = true;

                                       boolean joined;
                                       try {
                                          joined = join.init_Z_Minecraft_Consumer(mc, MacroExecutor::load_V_Str);
                                       } finally {
                                          runState.init_Z = false;
                                       }

                                       if (!joined) {
                                          flush_V();
                                          break;
                                       }

                                       load_V_Str(statusBefore);
                                       init_V_MacroExecutor_Tabl_2(runState, i + 1, macro.init_List.size(), macro.init_Str);
                                    } else if (action instanceof RaceAction race) {
                                       int bodyCount = init_I_Minecraft(mc, module, macro.init_List, i, race);
                                       if (bodyCount > 0) {
                                          i += bodyCount;
                                       }
                                    } else if (action instanceof ReportAction reportAction) {
                                       int conditionCount = init_I_Minecraft_3(mc, macro.init_List, i, reportAction);
                                       if (conditionCount > 0) {
                                          i += conditionCount;
                                       }
                                    } else if (action instanceof NbtBookAction nba) {
                                       int totalBooks = Math.max(1, nba.bookCount);
                                       long delayMs = Math.max(1, nba.delayTicks) * 50L;
                                       boolean signedAny = false;

                                       for (int b = 0; b < totalBooks && close_Z(); b++) {
                                          int bookIdx = b;
                                          CompletableFuture<Boolean> result = new CompletableFuture<>();
                                          mc.execute(() -> result.complete(nba.init_Z_Minecraft_2(mc, bookIdx, totalBooks)));
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
                                          mc.execute(() -> nba.init_V_Minecraft_8(mc));
                                       }
                                    } else if (action instanceof GoToAction ga) {
                                       init_V_Minecraft_CharEncoder(mc, ga);
                                    } else if (action instanceof MoveAction ma) {
                                       if (ma.nonBlocking) {
                                          mc.execute(() -> ma.execute(mc));
                                          init_ConcurrentHashMap().put(ma, new int[]{ma.durationTicks - 1});
                                       } else {
                                          for (int t = 0; t < ma.durationTicks && close_Z(); t++) {
                                             mc.execute(() -> ma.execute(mc));
                                             CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                                             init_V_CompletableFuture(tickFuture);
                                          }

                                          MacroExecutor.Inner_TableIndex movementRun = init_ThreadLocal.get();
                                          mc.execute(() -> init_V_MacroExecutor_Tabl(movementRun, ma, mc));
                                       }
                                    } else if (action instanceof LookAtBlockAction la) {
                                       LookAtBlockAction.Inner_DataUtil rotationTarget = init_ValueSink_DataUtil_Minecraft_ValueSink(mc, la);
                                       if (rotationTarget == null) {
                                          continue;
                                       }

                                       if (la.smooth) {
                                          init_V_F(rotationTarget.init(), rotationTarget.load(), la.init_D_2());
                                          if (la.waitForCompletion) {
                                             set();
                                          }
                                       } else {
                                          mc.execute(() -> {
                                             if (mc.player != null) {
                                                mc.player.setYRot(rotationTarget.init());
                                                mc.player.setXRot(rotationTarget.load());
                                             }
                                          });
                                       }
                                    } else if (action instanceof RepeatAction ra) {
                                       int startIdx = i + 1;
                                       int endIdx = Math.min(startIdx + ra.stepCount, macro.init_List.size());

                                       for (int rep = 0; rep < ra.repeatCount && close_Z(); rep++) {
                                          load_V_Str("Repeat " + (rep + 1) + "/" + ra.repeatCount);

                                          for (int j = startIdx; j < endIdx && close_Z(); j++) {
                                             MacroAction subAction = macro.init_List.get(j);
                                             if (subAction.isEnabled()) {
                                                read_ThreadLocal.set(new MacroExecutor.Inner_IoWorker(macro, j + 1, endIdx));
                                                write_ThreadLocal.set(0);

                                                try {
                                                   init_V_Minecraft_2(mc, subAction, module);
                                                   j += Math.max(0, write_ThreadLocal.get());
                                                } finally {
                                                   write_ThreadLocal.set(0);
                                                   read_ThreadLocal.remove();
                                                }
                                             }
                                          }
                                       }

                                       i = endIdx - 1;
                                    } else if (action instanceof CaptureValueAction captureValue) {
                                       read(mc, captureValue);
                                    } else if (action instanceof WaitForChatAction wca) {
                                       load_V_Str("Waiting for chat: " + wca.pattern);
                                       if (!init_Z_MacroAction_J(wca, wca.timeoutMs)) {
                                          init_V_au(wca);
                                       }
                                    } else if (action instanceof WaitForEntityAction wea) {
                                       load_V_Str(wea.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wea, 0L)) {
                                             init_V_BooleanSupplier_I(() -> wea.init_Z_5(mc), wea.timeoutMs);
                                          }
                                       } catch (CancellationException var242) {
                                       }
                                    } else if (action instanceof WaitForSoundAction wsa) {
                                       String sndDesc = wsa.soundIds.isEmpty() ? "any" : wsa.soundIds.get(0);
                                       load_V_Str("Waiting for sound: " + sndDesc);

                                       try {
                                          if (!init_Z_MacroAction_J(wsa, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(wsa);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var241) {
                                       }
                                    } else if (action instanceof MineAction mna) {
                                       init_V_Minecraft_q(mc, mna);
                                    } else if (action instanceof InstaBreakAction) {
                                       init_V_Minecraft_MethodUtil(mc, (InstaBreakAction)action);
                                    } else if (action instanceof BreakAction breakAction) {
                                       MacroExecutor.Inner_StateCache nextActions = MacroExecutor.Inner_StateCache.init_MacroExecutor_Stat();
                                       if (breakAction.interact && breakAction.runNextSteps) {
                                          nextActions = init_MacroExecutor_Stat_List_I(macro.init_List, i);
                                          breakAction.init_V_List(nextActions.init_List());
                                       }

                                       init_V_Minecraft_CachePool(mc, breakAction);
                                       if (breakAction.load() && nextActions.init_I() > i) {
                                          i = nextActions.init_I();
                                       }
                                    } else if (action instanceof WaitForSlotChangeAction wsca) {
                                       load_V_Str(wsca.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wsca, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(wsca);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var240) {
                                       }
                                    } else if (action instanceof WaitForWorldChangeAction world) {
                                       load_V_Str(world.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(world, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(world);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var239) {
                                       }
                                    } else if (action instanceof WaitForPositionDeltaAction wpda) {
                                       load_V_Str(wpda.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wpda, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_D_Z(wpda.distance, wpda.horizontalOnly);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var238) {
                                       }
                                    } else if (action instanceof WaitForTeleportAction teleport) {
                                       load_V_Str(teleport.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(teleport, 0L)) {
                                             CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, teleport);
                                             load_Z_CompletableFuture_J(future, 0L);
                                          }
                                       } catch (CancellationException var237) {
                                       }
                                    } else if (action instanceof WaitGamemodeChangeAction gm) {
                                       load_V_Str(gm.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(gm, gm.timeoutMs)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(gm);
                                             load_Z_CompletableFuture_J(future, gm.timeoutMs);
                                          }
                                       } catch (CancellationException var236) {
                                       }
                                    } else if (action instanceof WaitMovementAction wm) {
                                       load_V_Str(wm.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wm, 0L)) {
                                             CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, wm.init_MacroAction_3());
                                             load_Z_CompletableFuture_J(future, 0L);
                                          }
                                       } catch (CancellationException var235) {
                                       }
                                    } else if (action instanceof WaitForLanStepAction wla) {
                                       load_V_Str(wla.getDisplayName());

                                       try {
                                          if (!init_Z_MacroAction_J(wla, 0L)) {
                                             CompletableFuture<Void> future = MacroWaitRegistry.init(wla);
                                             init_V_CompletableFuture(future);
                                          }
                                       } catch (CancellationException var234) {
                                       }
                                    } else if (action instanceof WaitForMacroStepAction wma) {
                                       load_V_Str(wma.getDisplayName());
                                       if (!init_Z_MacroAction_J(wma, wma.timeoutMs)) {
                                          init_V_aA(wma);
                                       }
                                    } else {
                                       if (action instanceof StopMacroAction) {
                                          load_V_Str("Stopping Macro");
                                          StopMacroAction stopAction = (StopMacroAction)action;
                                          if (stopAction.target != StopMacroAction.Inner_DataUtil.SELF) {
                                             stopAction.execute(mc);
                                          } else {
                                             init_V_MacroExecutor_Tabl_2(runState, i + 1, macro.init_List.size(), macro.init_Str);
                                             flush_V();
                                          }
                                          break;
                                       }

                                       if (action instanceof TickSyncAction tsa) {
                                          if (mc.level == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.Inner_CachePool> preGenerated = init_List_aM(macro, i + 1, tsa.preGenCount);
                                          long targetTick = mc.level.getGameTime() + tsa.tickOffset;
                                          load_V_Str("Tick Sync -> " + targetTick + " (" + preGenerated.size() + " pkts)");

                                          while (close_Z() && mc.level.getGameTime() < targetTick) {
                                             LockSupport.parkNanos(1000000L);
                                          }

                                          if (close_Z()) {
                                             load_V_Minecraft_List(mc, preGenerated);
                                             i += load_I_aM(macro, i + 1, preGenerated.size());
                                             AutismClientMessaging.sendPrefixed("Tick sync: " + preGenerated.size() + " pkts sent");
                                          }
                                       } else if (action instanceof RevisionSyncAction rsa) {
                                          if (mc.player == null || mc.player.containerMenu == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.Inner_CachePool> preGenerated = init_List_aM(macro, i + 1, rsa.preGenCount);
                                          int baseRevision = mc.player.containerMenu.getStateId();
                                          int targetRevision = baseRevision + rsa.revisionOffset;
                                          load_V_Str("Rev Sync -> " + targetRevision + " (" + preGenerated.size() + " pkts)");

                                          while (
                                             close_Z()
                                                && mc.player != null
                                                && mc.player.containerMenu != null
                                                && mc.player.containerMenu.getStateId() < targetRevision
                                          ) {
                                             LockSupport.parkNanos(500000L);
                                          }

                                          if (close_Z() && mc.player != null && mc.player.containerMenu != null) {
                                             load_V_Minecraft_List(mc, preGenerated);
                                             i += load_I_aM(macro, i + 1, preGenerated.size());
                                             AutismClientMessaging.sendPrefixed("Rev sync: " + preGenerated.size() + " pkts sent");
                                          }
                                       } else if (action instanceof ServerTickSyncAction stsa) {
                                          if (mc.getConnection() == null) {
                                             continue;
                                          }

                                          List<AutismSessionState.Inner_CachePool> preGenerated = init_List_aM(macro, i + 1, stsa.preGenCount);

                                          for (int sampleWaitMs = 0; close_Z() && !ServerTickPhaseTracker.init_Z() && sampleWaitMs < stsa.maxWaitMs; sampleWaitMs += 50) {
                                             float progress = ServerTickPhaseTracker.init_F();
                                             load_V_Str(
                                                String.format("ServerSync warmup %.0f%% (%d samples, %dms)", progress * 100.0F, ServerTickPhaseTracker.load_I(), ServerTickPhaseTracker.write())
                                             );
                                             sleep(50L);
                                          }

                                          if (!close_Z()) {
                                             continue;
                                          }

                                          long optimalTime = ServerTickPhaseTracker.init(stsa.bufferMs, stsa.ignorePing);
                                          long msUntil = (optimalTime - System.nanoTime()) / 1000000L;
                                          int ping = ServerTickPhaseTracker.init_I();
                                          String pingStr = stsa.ignorePing ? " (no ping)" : " ping:" + ping + "ms";
                                          load_V_Str("ServerSync in " + Math.max(0L, msUntil) + "ms" + pingStr + " (" + preGenerated.size() + " pkts)");
                                          long startWait = System.nanoTime();
                                          long maxWaitNanos = stsa.maxWaitMs * 1000000L;

                                          while (close_Z()) {
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

                                          if (close_Z()) {
                                             load_V_Minecraft_List(mc, preGenerated);
                                             i += load_I_aM(macro, i + 1, preGenerated.size());
                                             long actualDelay = (System.nanoTime() - optimalTime) / 1000000L;
                                             AutismClientMessaging.sendPrefixed("ServerSync: " + preGenerated.size() + " pkts (+" + actualDelay + "ms)");
                                          }
                                       } else if (action instanceof RotateAction ra) {
                                          if (ra.smooth) {
                                             init_V_F(ra.yaw, ra.pitch, ra.init());
                                             if (ra.waitForCompletion) {
                                                set();
                                             }
                                          } else {
                                             mc.execute(() -> action.execute(mc));
                                          }
                                       } else if (action instanceof PacedTpAction pacedTp) {
                                          init_V_Minecraft_w(mc, pacedTp);
                                       } else if (action instanceof LookAtBlockAction la) {
                                          LookAtBlockAction.Inner_DataUtil rotationTarget = init_ValueSink_DataUtil_Minecraft_ValueSink(mc, la);
                                          if (rotationTarget == null) {
                                             continue;
                                          }

                                          if (la.smooth) {
                                             init_V_F(rotationTarget.init(), rotationTarget.load(), la.init_D_2());
                                             if (la.waitForCompletion) {
                                                set();
                                             }
                                          } else {
                                             mc.execute(() -> {
                                                if (mc.player != null) {
                                                   mc.player.setYRot(rotationTarget.init());
                                                   mc.player.setXRot(rotationTarget.load());
                                                }
                                             });
                                          }
                                       } else if (action instanceof UseItemAction) {
                                          init_V_Minecraft_an(mc, (UseItemAction)action);
                                       } else if (action instanceof ItemAction ia) {
                                          if (ia.waitForItem || !ia.itemNames.isEmpty()) {
                                             try {
                                                load_V_Minecraft_MapStore(mc, ia);
                                             } catch (CancellationException var233) {
                                             }
                                          }

                                          postGuiFuture = write(mc, ia);
                                          if (ia.useClickDelay && ia.clickDelayMs > 0) {
                                             init_V_Minecraft_MapStore(mc, ia);
                                          } else {
                                             init_V_Minecraft_3(mc, action, "ItemAction");
                                          }
                                       } else if (!(action instanceof DropAction) && !(action instanceof SwapSlotsAction)) {
                                          if (action instanceof CraftAction craftAction) {
                                             init_V_Minecraft_SyncManager(mc, craftAction);
                                          } else if (action instanceof SendPacketAction) {
                                             if (module != null && module.add_Z()) {
                                                try {
                                                   action.execute(mc);
                                                } catch (Exception e) {
                                                   AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + e.getMessage());
                                                }
                                             } else {
                                                init_V_Minecraft_Runnable(mc, () -> {
                                                   try {
                                                      action.execute(mc);
                                                   } catch (Exception ex) {
                                                      AutismClientMessaging.sendPrefixed("§cError in SendPacket: " + ex.getMessage());
                                                   }
                                                });
                                             }
                                          } else if (action instanceof StoreItemAction sia) {
                                             load_V_Str((sia.mode == StoreItemAction.Inner_DataUtil.LOOT ? "Looting" : "Storing") + (sia.persistent ? " ∞" : ""));
                                             if (sia.persistent) {
                                                while (close_Z()) {
                                                   try {
                                                      init_V_Minecraft_ah(mc, sia);
                                                   } catch (Exception e) {
                                                      AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                                                   }

                                                   CompletableFuture<Void> siTick = MacroWaitRegistry.load_CompletableFuture();
                                                   init_V_CompletableFuture(siTick);
                                                }
                                             } else {
                                                try {
                                                   init_V_Minecraft_ah(mc, sia);
                                                } catch (Exception e) {
                                                   AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                                                }
                                             }
                                          } else if (action instanceof ClickAction ca) {
                                             init_V_Minecraft_NetHelper(mc, ca);
                                          } else if (action instanceof PayAction) {
                                             init_V_Minecraft_G(mc, (PayAction)action);
                                          } else if (action instanceof XCarryAction xsa) {
                                             if (xsa.mode == XCarryAction.Inner_StreamUtil.PUT_IN
                                                && (xsa.transferMode == XCarryAction.Inner_ConfigStore.CLICK || xsa.transferMode == XCarryAction.Inner_ConfigStore.SAFE_CLICK)) {
                                                if (xsa.transferMode == XCarryAction.Inner_ConfigStore.SAFE_CLICK) {
                                                   load_V_Str("XCarry Safe");
                                                   load_V_Minecraft_aQ(mc, xsa);
                                                } else {
                                                   load_V_Str("XCarry Click");
                                                   init_V_Minecraft_aQ(mc, xsa);
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

                                                load_V_Str(var10000);
                                                init_V_Minecraft_Runnable(mc, () -> {
                                                   try {
                                                      xsa.execute(mc);
                                                   } catch (Exception ex) {
                                                      AutismClientMessaging.sendPrefixed("§cXCarry: " + ex.getMessage());
                                                   }
                                                });
                                             }
                                          } else if (action instanceof InventoryAuditAction auditAction
                                             && (auditAction.mode == InventoryAuditAction.Inner_BufferHelper.DUPE || auditAction.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM)) {
                                             load_V_Str("Dupe: " + auditAction.dupeVector.name() + " " + auditAction.openMode.name());

                                             try {
                                                auditAction.init_V_Minecraft_2(mc);
                                             } catch (Exception e) {
                                                AutismClientMessaging.sendPrefixed("§cDupe error: " + e.getMessage());
                                             }
                                          } else if (action instanceof ContainerClickSequenceAction sequence) {
                                             init_V_Minecraft_StateCache(mc, sequence);
                                          } else if (action instanceof PacketBurstAction burst) {
                                             init_V_Minecraft_y(mc, burst);
                                          } else if (action instanceof BundleDupeV2Action bundleV2) {
                                             load_V_Str("Bundle Dupe V2");
                                             bundleV2.execute(mc);
                                          } else if (action instanceof RollbackAction rollback) {
                                             load_V_Str("Rollback");

                                             try {
                                                rollback.init_V_Minecraft_4(mc);
                                             } catch (Exception e) {
                                                AutismClientMessaging.sendPrefixed("§cRollback failed: " + init_Str_Throwable(e));
                                             }
                                          } else if (action instanceof PacketGateAction pga) {
                                             init_V_Minecraft_B(mc, pga);
                                          } else if (write(action)) {
                                             postGuiFuture = read_CompletableFuture(mc, action);
                                          } else if (action instanceof MacroVariablesAction || action instanceof AssertAction) {
                                             action.execute(mc);
                                          } else if (action instanceof UseItemPhaseAction phaseAction) {
                                             init_V_Minecraft_ao(mc, phaseAction);
                                          } else if (action instanceof ContextMacroAction contextAction) {
                                             init_V_ContextMacroAction(contextAction);
                                          } else if (action instanceof PlaceAction placeInteract && placeInteract.interact) {
                                             init_V_Minecraft_K(mc, placeInteract);
                                          } else if (action instanceof PlaceAction place && place.waitForItem) {
                                             MacroItemTarget placeTarget = place.init_ArrayUtil().init_ArrayUtil_Minecraft(mc);
                                             if (placeTarget == null) {
                                                postGuiFuture = null;
                                             } else {
                                                if (placeTarget.update() || placeTarget.init_Z()) {
                                                   load_V_Str("Waiting for " + init_Str_ArrayUtil(placeTarget));

                                                   try {
                                                      init_V_Minecraft_ArrayUtil(mc, placeTarget);
                                                   } catch (CancellationException var227) {
                                                   }
                                                }

                                                if (close_Z()) {
                                                   PlaceAction fPlace = place;
                                                   load_V_Minecraft(mc, () -> {
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
                                          } else if (update(action)) {
                                             init_V_Minecraft_3(mc, action, action.getDisplayName());
                                          } else if (module != null && module.add_Z()) {
                                             CountDownLatch latch = new CountDownLatch(1);
                                             MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
                                             mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(scheduledRun, () -> {
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
                                             init_V_Minecraft_3(mc, action, action.getDisplayName());
                                          }
                                       } else {
                                          if (action instanceof DropAction dropAction) {
                                             postGuiFuture = write(mc, dropAction);
                                          }

                                          init_V_Minecraft_3(mc, action, action.getDisplayName());
                                       }
                                    }

                                    init_V_MacroAction_Minecraft(action, mc);
                                    if (postGuiFuture != null && close_Z()) {
                                       try {
                                          init_V_CompletableFuture(postGuiFuture);
                                       } catch (CancellationException var226) {
                                       }
                                    }

                                    if (close_Z()) {
                                       init_V_I(i + 1);
                                       init_V_MacroExecutor_Tabl_2(runState, i + 1, macro.init_List.size(), macro.init_Str);
                                    }

                                    if (close_Z() && !sync(action) && module != null && module.add_Z()) {
                                       int delayUs = module.load_I();
                                       if (delayUs > 0) {
                                          LockSupport.parkNanos(delayUs * 1000L);
                                       } else {
                                          Thread.onSpinWait();
                                       }

                                       write(mc);
                                    }
                                 }
                              }
                           } finally {
                              MacroAimController.init_V();
                              init_V_EnumSet(heldResources);
                           }
                        }
                     }
                  }
               }
            }

            if (macro.init_List.isEmpty()) {
               if (!runState.init_MacroExecutor_Pool.init_Z()) {
                  AutismClientMessaging.sendPrefixed("§cMacro stopped: no actions left.");
               }

               flush_V();
               break;
            }

            if (close_Z()) {
               PacketGateManager.init(runState.init_J, mc == null ? null : mc.getConnection());
            }

            if (macro.init_Z && loopCount < maxLoops - 1 && close_Z()) {
               if (module != null && module.add_Z()) {
                  int delayUs = module.load_I();
                  if (delayUs > 0) {
                     LockSupport.parkNanos(delayUs * 1000L);
                  } else {
                     Thread.onSpinWait();
                  }

                  write(mc);
               } else if (module != null && module.put_Z()) {
                  int sleepMs = module.init_I();
                  sleep(sleepMs);
               } else {
                  try {
                     CompletableFuture<Void> syncFuture = MacroWaitRegistry.load_CompletableFuture();
                     init_V_CompletableFuture(syncFuture);
                  } catch (CancellationException var225) {
                  }
               }
            }
         }

         read(runState);
         if (close_Z()) {
            runState.init_Str = "COMPLETED";
            if (!runState.init_MacroExecutor_Pool.init_Z()) {
               AutismClientMessaging.sendPrefixed("§aMacro finished: " + macro.init_Str);
            }
         }
      } catch (Exception e) {
         runState.init_Str = "FAILED";
         if (!runState.init_MacroExecutor_Pool.init_Z()) {
            AutismClientMessaging.sendPrefixed("§cMacro crashed: " + e.getMessage());
         }

         AutismClientAddon.LOG.warn("[Autism] Macro crashed: {}", macro.init_Str, e);
      } finally {
         if (!autismclient.modules.aD.read_Z()) {
            if (!"COMPLETED".equals(runState.init_Str)) {
               MacroToolBridge.init(runState.init_J, true);
            }

            init_V_MacroExecutor_Tabl_Minecraft(runState, mc);
         }

         PacketGateManager.init(runState.init_J, mc == null ? null : mc.getConnection());
         open_V();
         flush(runState);
         if (!init_AtomicBoolean.get()) {
            MacroWaitRegistry.load_V();
         }

         init_V_MacroExecutor_Tabl_2(runState, -1, 0, "");
      }
   }

   private static String write(String value) {
      return PacketNameResolver.load(value);
   }

   private static boolean init_Z_Str_Str(String normalizedExpected, String candidate) {
      if (!normalizedExpected.isEmpty() && candidate != null && !candidate.isEmpty()) {
         String normalizedCandidate = write(candidate);
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

   private static boolean load_Z_Str_Str(String expected, String candidate) {
      return expected != null && !expected.isEmpty() ? init_Z_Str_Str(write(expected), candidate) : false;
   }

   private static MacroExecutor.Inner_FileCache init_MacroExecutor_File_Str(String target) {
      if (target == null) {
         return init_MacroExecutor_File;
      }

      if (read_ConcurrentHashMap.size() > 256) {
         read_ConcurrentHashMap.clear();
      }

      return read_ConcurrentHashMap.computeIfAbsent(target, t -> {
         String expectedName = WaitForPacketAction.load(t);
         return new MacroExecutor.Inner_FileCache(WaitForPacketAction.init(t), expectedName, write(expectedName));
      });
   }

   private static boolean init_Z_Str_2(String target, Packet<?> packet, String direction) {
      if (packet == null) {
         return false;
      }

      MacroExecutor.Inner_FileCache parsed = init_MacroExecutor_File_Str(target);
      if (!parsed.init().isEmpty() && !parsed.init().equalsIgnoreCase(direction)) {
         return false;
      }

      if (parsed.load().isEmpty()) {
         return true;
      }

      String normalizedExpected = parsed.read();
      if (init_Z_Str_Str(normalizedExpected, PacketNameResolver.init_Str_Packet_Str(packet, direction))) {
         return true;
      }

      if (init_Z_Str_Str(normalizedExpected, PacketNameResolver.init_Str_Packet(packet))) {
         return true;
      }

      if (init_Z_Str_Str(normalizedExpected, packet.getClass().getSimpleName())) {
         return true;
      }

      String fullName = PacketTypeRegistry.init_Str_Class((Class<? extends Packet<?>>)packet.getClass());
      return init_Z_Str_Str(normalizedExpected, fullName);
   }

   private static void read_V(String target) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         CompletableFuture<Void> future = new CompletableFuture<>();
         String normalizedTarget = WaitForPacketAction.read(target);
         synchronized (run.init_Obj) {
            run.init_AtomicReference.set(normalizedTarget);
            run.init_CompletableFuture = future;
            run.read_AtomicBoolean.set(true);
         }

         try {
            load_V_Str(normalizedTarget.isEmpty() ? "Waiting for packet: Any" : "Waiting for packet: " + WaitForPacketAction.write(normalizedTarget));
            init_V_CompletableFuture(future);
         } finally {
            synchronized (run.init_Obj) {
               if (run.init_CompletableFuture == future) {
                  run.read_AtomicBoolean.set(false);
                  run.init_AtomicReference.set("");
                  run.init_CompletableFuture = null;
               }
            }
         }
      } else {
         CompletableFuture<Void> future = new CompletableFuture<>();
         String normalizedTarget = WaitForPacketAction.read(target);
         synchronized (load_Obj) {
            load_AtomicReference.set(normalizedTarget);
            init_AtomicReference.set("");
            init_CompletableFuture = future;
            load_AtomicBoolean.set(true);
         }

         try {
            load_V_Str(normalizedTarget.isEmpty() ? "Waiting for packet: Any" : "Waiting for packet: " + WaitForPacketAction.write(normalizedTarget));
            init_V_CompletableFuture(future);
         } finally {
            synchronized (load_Obj) {
               if (init_CompletableFuture == future) {
                  load_AtomicBoolean.set(false);
                  load_AtomicReference.set("");
                  init_CompletableFuture = null;
               }
            }
         }
      }
   }

   private static void init_V_au(WaitForChatAction wca) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null) {
         init_V_MacroAction(wca);
         CompletableFuture<Void> future = new CompletableFuture<>();
         run.load_CompletableFuture = future;
         run.init_au = wca;
         run.write_AtomicBoolean.set(true);

         try {
            if (wca.timeoutMs > 0) {
               future.completeOnTimeout(null, wca.timeoutMs, TimeUnit.MILLISECONDS);
            }

            init_V_CompletableFuture(future);
            read_V(wca);
         } finally {
            if (run.load_CompletableFuture == future) {
               run.write_AtomicBoolean.set(false);
               run.init_au = null;
               run.load_CompletableFuture = null;
            }
         }
      } else {
         CompletableFuture<Void> future = new CompletableFuture<>();
         load_CompletableFuture = future;
         read_AtomicBoolean.set(true);
         read_AtomicReference.set(init_Str_Str(wca.pattern));
         write_AtomicReference.set(wca.patternJson == null ? "" : wca.patternJson);
         write_AtomicBoolean.set(wca.useRegex);
         init_AtomicInteger.set(WaitForChatAction.init_I_I_8(wca.fuzzyPercent));
         update_AtomicReference.set("");

         try {
            if (wca.timeoutMs > 0) {
               future.completeOnTimeout(null, wca.timeoutMs, TimeUnit.MILLISECONDS);
            }

            init_V_CompletableFuture(future);
         } finally {
            read_AtomicBoolean.set(false);
            read_AtomicReference.set("");
            write_AtomicReference.set("");
            write_AtomicBoolean.set(false);
            init_AtomicInteger.set(100);
            load_CompletableFuture = null;
         }
      }
   }

   private static void init_V_TaskQueue(CaptureValueAction action) {
      if (action != null) {
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         init_V_MacroAction(action);
         MacroExecutor.Inner_EventBus listener = init_MacroExecutor_Even_Predicate(
            packet -> {
               MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
               if (capture == null) {
                  return false;
               }

               MacroExecutor.Inner_PacketUtil message = new MacroExecutor.Inner_PacketUtil(
                  capture.init_Str() == null ? "" : capture.init_Str(),
                  capture.load() == null ? "" : capture.load(),
                  capture.read() == null ? "" : capture.read(),
                  capture.init_Component(),
                  capture.init_MacroExecutor_Task(),
                  System.currentTimeMillis()
               );
               CaptureValueAction.Inner_NetHelper preview = action.init_TaskQueue_NetHelpe_MacroExecutor_Pack(message);
               if (!preview.init_Z()) {
                  return false;
               }

               if (run != null) {
                  run.load_Map.put(action, preview.init_Map());
               } else {
                  MacroVariableContext.init_V_Map(preview.init_Map());
               }

               return true;
            }
         );

         try {
            if (load_Z_CompletableFuture_J(listener.init_CompletableFuture, 0L) && run != null) {
               read_V(action);
            }
         } finally {
            listener.init_V();
         }
      }
   }

   static CandidatePoolSelector.Inner_BufferHelper init_ConfigStore_Buffer_MacroAction(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null) {
         synchronized (run.read_Map) {
            return run.read_Map.computeIfAbsent(action, a -> new CandidatePoolSelector.Inner_BufferHelper());
         }
      } else {
         return null;
      }
   }

   private static void push() {
      long stoppedAt = write_J;
      if (stoppedAt > 0L && System.currentTimeMillis() - stoppedAt > 3000L) {
         write_ConcurrentHashMap.clear();
         update_ConcurrentHashMap.clear();
         sync_ConcurrentHashMap.clear();
      }
   }

   static String init_Str_Str(String resolvedCommand, CandidatePoolSelector.Inner_DataUtil filter, String filterText) {
      String query = resolvedCommand.startsWith("/") ? resolvedCommand : "/" + resolvedCommand;
      if (!query.contains(" ")) {
         query = query + " ";
      }

      String prefix = filter == CandidatePoolSelector.Inner_DataUtil.PREFIX && filterText != null ? filterText.trim() : "";
      if (!prefix.isEmpty() && !prefix.contains(" ") && query.endsWith(" ")) {
         int end = query.length();

         while (end > 0 && query.charAt(end - 1) == ' ') {
            end--;
         }

         query = query.substring(0, end) + " " + prefix;
      }

      return query;
   }

   private static List<String> init_List_ClientPacketListen(ClientPacketListener connection, CaptureValueAction action, String query, List<String> base) {
      if (!action.autofillCacheList && base.size() < 1000) {
         return base;
      }

      MacroExecutor.Inner_TokenStore union = sync_ConcurrentHashMap.compute(
         query,
         (key, existing) -> (MacroExecutor.Inner_TokenStore)(existing != null && existing.init_Obj == connection
            ? existing
            : new MacroExecutor.Inner_TokenStore(connection, query))
      );
      synchronized (union) {
         union.init_LinkedHashSet.addAll(base);
         union.load = Math.max(union.load, base.size());
         init_V_MacroExecutor_Toke(union, connection, action.autofillTimeoutMs);
         List<String> pool = new ArrayList<>(union.init_LinkedHashSet);
         boolean ordered = action.listSelection == CandidatePoolSelector.Inner_CachePool.SEQUENTIAL
            || action.listSelection == CandidatePoolSelector.Inner_CachePool.FIRST
            || action.listSelection == CandidatePoolSelector.Inner_CachePool.LAST
            || action.listSelection == CandidatePoolSelector.Inner_CachePool.POSITION;
         if (ordered) {
            pool.sort(String.CASE_INSENSITIVE_ORDER);
         }

         return pool;
      }
   }

   private static void init_V_MacroExecutor_Toke(MacroExecutor.Inner_TokenStore union, ClientPacketListener connection, int timeoutMs) {
      if (union.init_LinkedHashSet.size() >= 60000) {
         union.init_ArrayDeque.clear();
      } else {
         while (!union.init_ArrayDeque.isEmpty()) {
            if (union.init_I < "abcdefghijklmnopqrstuvwxyz0123456789_".length()) {
               String target = union.init_ArrayDeque.peek() + "abcdefghijklmnopqrstuvwxyz0123456789_".charAt(union.init_I);
               MacroExecutor.Inner_BufferHelper cached = write_ConcurrentHashMap.get(target);
               if (cached == null || cached.init_Obj() != connection) {
                  init_MacroExecutor_Text_ClientPacketListen(connection, target, timeoutMs);
                  return;
               }

               union.init_LinkedHashSet.addAll(cached.init_List());
               int size = cached.init_List().size();
               boolean truncated = size >= union.load && union.load >= 8;
               union.load = Math.max(union.load, size);
               if (truncated) {
                  union.init_ArrayDeque.add(target);
               }

               union.init_I++;
            } else {
               union.init_ArrayDeque.poll();
               union.init_I = 0;
            }
         }
      }
   }

   private static void init_V_MacroExecutor_Tabl_3(MacroExecutor.Inner_TableIndex run, CaptureValueAction action, List<String> suggestions, String query) {
      CaptureValueAction.Inner_NetHelper preview = action.init_TaskQueue_NetHelpe_List_Str(suggestions, query);
      if (preview.init_Z()) {
         if (run != null) {
            run.init_m.init_V_Map(preview.init_Map());
         } else {
            MacroVariableContext.init_V_Map(preview.init_Map());
         }
      }
   }

   private static MacroExecutor.Inner_TextFormat init_MacroExecutor_Text_ClientPacketListen(ClientPacketListener connection, String query, int timeoutMs) {
      MacroExecutor.Inner_TextFormat existing = update_ConcurrentHashMap.get(query);
      if (existing != null && existing.init_Obj() == connection && !existing.init_CompletableFuture().isDone()) {
         return existing;
      }

      if (update_ConcurrentHashMap.size() > 256) {
         return existing;
      }

      int requestId = autismclient.util.CommandProbeIdAllocator.init();
      CompletableFuture<List<String>> future = new CompletableFuture<>();
      MacroExecutor.Inner_EventBus listener = init_MacroExecutor_Even_Predicate(packet -> {
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
      MacroExecutor.Inner_TextFormat pending = new MacroExecutor.Inner_TextFormat(future, connection);
      update_ConcurrentHashMap.put(query, pending);
      future.completeOnTimeout(null, timeoutMs > 0 ? Math.max(1000, timeoutMs) : 30000L, TimeUnit.MILLISECONDS);
      future.whenComplete((texts, error) -> {
         listener.init_V();
         update_ConcurrentHashMap.remove(query, pending);
         if (texts != null) {
            if (write_ConcurrentHashMap.size() >= 256) {
               write_ConcurrentHashMap.clear();
            }

            write_ConcurrentHashMap.put(query, new MacroExecutor.Inner_BufferHelper(List.copyOf((Collection<? extends String>)texts), connection));
         }
      });

      try {
         connection.send(new ServerboundCommandSuggestionPacket(requestId, query));
      } catch (Throwable error) {
         future.complete(null);
      }

      return pending;
   }

   private static void init_V_Minecraft_TaskQueue(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      init_V_MacroAction(action);
      MacroTemplateResolver.Inner_CachePool resolved = MacroVariableContext.init_j_CachePool_Str_Minecraft(action.autofillCommand, mc);
      if (resolved.init_Z() && !resolved.init_Str().isBlank()) {
         String query = init_Str_Str(resolved.init_Str(), action.listFilter, action.listFilterText);
         ClientPacketListener connection = mc.getConnection();
         if (connection != null) {
            if (action.autofillCacheList) {
               MacroExecutor.Inner_BufferHelper listCache = write_ConcurrentHashMap.get(query);
               if (listCache != null && listCache.init_Obj() == connection && !listCache.init_List().isEmpty()) {
                  load_V_Str("Using cached suggestions");
                  init_V_MacroExecutor_Tabl_3(run, action, listCache.init_List(), query);
               } else {
                  load_V_Str("Sweeping command suggestions");
                  List<String> all = init_List_ClientPacketListen_Str(connection, query);
                  if (!all.isEmpty()) {
                     if (write_ConcurrentHashMap.size() >= 256) {
                        write_ConcurrentHashMap.clear();
                     }

                     write_ConcurrentHashMap.put(query, new MacroExecutor.Inner_BufferHelper(List.copyOf(all), connection));
                     init_V_MacroExecutor_Tabl_3(run, action, all, query);
                  }
               }
            } else {
               MacroExecutor.Inner_BufferHelper cached = write_ConcurrentHashMap.get(query);
               if (cached != null && (cached.init_Obj() != connection || cached.init_List().isEmpty())) {
                  cached = null;
               }

               if (cached != null) {
                  load_V_Str("Using cached suggestions");
                  init_V_MacroExecutor_Tabl_3(run, action, init_List_ClientPacketListen(connection, action, query, cached.init_List()), query);
                  init_MacroExecutor_Text_ClientPacketListen(connection, query, action.autofillTimeoutMs);
               } else {
                  load_V_Str("Requesting command suggestions");
                  MacroExecutor.Inner_TextFormat pending = init_MacroExecutor_Text_ClientPacketListen(connection, query, action.autofillTimeoutMs);
                  if (pending != null && load_Z_CompletableFuture_J(pending.init_CompletableFuture(), 0L)) {
                     List<String> suggestions = pending.init_CompletableFuture().getNow(null);
                     if (suggestions != null && !suggestions.isEmpty()) {
                        init_V_MacroExecutor_Tabl_3(run, action, init_List_ClientPacketListen(connection, action, query, suggestions), query);
                     }
                  }
               }
            }
         }
      }
   }

   private static List<String> init_List_ClientPacketListen_Str(final ClientPacketListener connection, String query) {
      CommandTabNameProber.Inner_DataUtil control = new CommandTabNameProber.Inner_DataUtil() {
         @Override
         public boolean init_Z() {
            Minecraft mc = Minecraft.getInstance();
            return !MacroExecutor.close_Z() || mc == null || mc.getConnection() != connection;
         }

         @Override
         public boolean load_Z() {
            return false;
         }

         @Override
         public int init_I() {
            return 60000;
         }

         @Override
         public int load_I() {
            return 50000;
         }

         @Override
         public boolean read() {
            return false;
         }
      };
      List<String> all = CommandTabNameProber.init(connection, query, control);
      all.sort(String.CASE_INSENSITIVE_ORDER);
      return all;
   }

   private static void load_V_Minecraft_TaskQueue(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      init_V_MacroAction(action);
      ClientPacketListener connection = mc.getConnection();
      if (connection != null) {
         String cacheKey = "\u0000namescrape";
         MacroExecutor.Inner_BufferHelper cached = write_ConcurrentHashMap.get(cacheKey);
         if (cached != null && cached.init_Obj() == connection && !cached.init_List().isEmpty()) {
            load_V_Str("Using cached names");
            init_V_MacroExecutor_Tabl_3(run, action, cached.init_List(), "");
         } else {
            load_V_Str("Scraping player names");
            List<String> names = init_List_Minecraft_ClientPacketListen(mc, connection);
            if (!names.isEmpty()) {
               if (write_ConcurrentHashMap.size() >= 256) {
                  write_ConcurrentHashMap.clear();
               }

               write_ConcurrentHashMap.put(cacheKey, new MacroExecutor.Inner_BufferHelper(List.copyOf(names), connection));
               init_V_MacroExecutor_Tabl_3(run, action, names, "");
            }
         }
      }
   }

   private static List<String> init_List_Minecraft_ClientPacketListen(Minecraft mc, final ClientPacketListener connection) {
      String self = mc.player == null ? "" : mc.player.getName().getString();
      LinkedHashMap<String, String> names = CommandTabNameProber.init(mc, self);
      List<CommandTabNameProber.Inner_StreamUtil> vectors = CommandTabNameProber.init_List_ClientPacketListen(connection);
      CommandTabNameProber.Inner_DataUtil control = new CommandTabNameProber.Inner_DataUtil() {
         @Override
         public boolean init_Z() {
            Minecraft m = Minecraft.getInstance();
            return !MacroExecutor.close_Z() || m == null || m.getConnection() != connection;
         }

         @Override
         public boolean load_Z() {
            return false;
         }

         @Override
         public int init_I() {
            return 60000;
         }

         @Override
         public int load_I() {
            return 100000;
         }
      };
      CommandTabNameProber.init(connection, names, self, vectors, control);
      List<String> out = new ArrayList<>(names.values());
      out.sort(String.CASE_INSENSITIVE_ORDER);
      return out;
   }

   private static void read(Minecraft mc, CaptureValueAction action) {
      if (action != null) {
         String modifierError = action.init_Str_4();
         if (!modifierError.isBlank()) {
            load_V_Str("Capture failed: " + modifierError);
            bi.write("Capture Value: " + modifierError);
         } else if (action.source == CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL) {
            load_V_Str("Requesting command suggestions");
            init_V_Minecraft_TaskQueue(mc, action);
            load_V_TaskQueue(action);
         } else if (action.source == CaptureValueAction.Inner_StateCache.NAMESCRAPE) {
            load_V_Str("Scraping player names");
            load_V_Minecraft_TaskQueue(mc, action);
            load_V_TaskQueue(action);
         } else if (action.waitForTrigger) {
            CaptureValueAction.Inner_StateCache source = action.source == null ? CaptureValueAction.Inner_StateCache.GUI_TITLE : action.source;
            load_V_Str("Waiting for capture: " + source.name().toLowerCase(Locale.ROOT).replace('_', ' '));
            if (source == CaptureValueAction.Inner_StateCache.RECENT_CHAT) {
               init_V_TaskQueue(action);
            } else if (source == CaptureValueAction.Inner_StateCache.SCOREBOARD) {
               update(mc, action);
            } else {
               write(mc, action);
            }
         } else {
            MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
            AtomicReference<CaptureValueAction.Inner_NetHelper> result = new AtomicReference<>(CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Not available"));
            long deadline = System.nanoTime() + 300000000L;

            while (close_Z()) {
               CompletableFuture<Void> done = new CompletableFuture<>();
               mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
                  try {
                     result.set(action.init_TaskQueue_NetHelpe_Minecraft(mc));
                     done.complete(null);
                  } catch (Throwable error) {
                     done.completeExceptionally(error);
                  }
               }));
               load_Z_CompletableFuture_J(done, 0L);
               if (result.get().init_Z() || System.nanoTime() >= deadline) {
                  break;
               }

               pop();
            }

            CaptureValueAction.Inner_NetHelper preview = result.get();
            if (!preview.init_Z()) {
               load_V_Str("Capture failed: " + preview.load());
               bi.write("Capture " + init_Str_TaskQueue(action) + ": " + preview.load());
            }
         }
      }
   }

   private static void load_V_TaskQueue(CaptureValueAction action) {
      String name = MacroVariableStore.load(action.saveAs);
      if (!name.isBlank() && !MacroVariableContext.init_Optional_Str(name).isPresent()) {
         load_V_Str("Capture failed: nothing captured");
         bi.write("Capture " + init_Str_TaskQueue(action) + ": nothing captured");
      }
   }

   private static String init_Str_TaskQueue(CaptureValueAction action) {
      String name = MacroVariableStore.load(action.saveAs);
      return name.isBlank() ? "value" : "{" + name + "}";
   }

   private static void write(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      init_V_MacroAction(action);
      String lastMessage = "";

      while (close_Z()) {
         CaptureValueAction.Inner_NetHelper preview = load_Obj_Minecraft(
            mc, () -> action.read_TaskQueue_NetHelpe(mc), CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Not available")
         );
         if (preview.init_Z()) {
            if (run != null) {
               run.init_m.init_V_Map(preview.init_Map());
            } else {
               MacroVariableContext.init_V_Map(preview.init_Map());
            }

            return;
         }

         if (!preview.load().isBlank() && !preview.load().equals(lastMessage)) {
            lastMessage = preview.load();
            load_V_Str("Capture waiting: " + lastMessage);
         }

         pop();
      }
   }

   private static void update(Minecraft mc, CaptureValueAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      init_V_MacroAction(action);
      String trackedKey = action.scoreboardRow;
      int trackedRow = action.scoreboardRowIndex;
      String trackedObjective = action.scoreboardObjective;
      String previousText = null;
      String lastMessage = "";
      boolean baselineReady = false;

      while (close_Z()) {
         final String keySnapshot0 = trackedKey;
         final int rowSnapshot = trackedRow;
         final String objectiveSnapshot = trackedObjective;
         CaptureValueAction.Inner_JsonUtil line = load_Obj_Minecraft(mc, () -> action.init(mc, keySnapshot0, rowSnapshot, objectiveSnapshot), null);
         if (line != null) {
            String keySnapshot = line.flush() == null ? "" : line.flush();
            if (!baselineReady) {
               trackedKey = line.init_Str();
               trackedRow = line.init_I();
               trackedObjective = line.load();
               previousText = keySnapshot;
               baselineReady = true;
            } else if (!keySnapshot.equals(previousText)) {
               CaptureValueAction.Inner_NetHelper preview = action.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(line);
               if (preview.init_Z()) {
                  if (run != null) {
                     run.init_m.init_V_Map(preview.init_Map());
                  } else {
                     MacroVariableContext.init_V_Map(preview.init_Map());
                  }

                  return;
               }

               if (!preview.load().isBlank() && !preview.load().equals(lastMessage)) {
                  lastMessage = preview.load();
                  load_V_Str("Capture waiting: " + lastMessage);
               }

               previousText = keySnapshot;
            }
         }

         pop();
      }
   }

   private static void init_V_CompletableFuture(CompletableFuture<Void> future) {
      Thread t = Thread.currentThread();
      future.whenComplete((v, e) -> LockSupport.unpark(t));

      while (!future.isDone() && close_Z()) {
         LockSupport.parkNanos(500000L);
         if (Thread.interrupted()) {
            break;
         }
      }

      if (!close_Z()) {
         future.cancel(true);
      }
   }

   private static void init_V_Minecraft_w(Minecraft mc, PacedTpAction action) {
      AtomicReference<PacedTpJobManager.Inner_StreamUtil> handleRef = new AtomicReference<>();
      init_V_Minecraft_Runnable(mc, () -> handleRef.set(PacedTpJobManager.init(action, "macro")));
      PacedTpJobManager.Inner_StreamUtil handle = handleRef.get();
      if (handle != null) {
         load_V_Str("TP " + action.init());

         try {
            while (close_Z() && PacedTpJobManager.init_bl_CachePool(handle.init_J()).init_bl_DataUtil() == PacedTpJobManager.Inner_DataUtil.PENDING) {
               pop();
            }
         } finally {
            if (PacedTpJobManager.init_bl_CachePool(handle.init_J()).init_bl_DataUtil() == PacedTpJobManager.Inner_DataUtil.PENDING) {
               mc.execute(() -> PacedTpJobManager.init(handle.init_J(), "macro stopped"));
            }
         }
      }
   }

   private static void write(Minecraft mc) {
      if (mc != null && !mc.isSameThread()) {
         int[] burst = sync_ThreadLocal.get();
         if (++burst[0] >= 256) {
            burst[0] = 0;
            CompletableFuture<Void> prev = update_ThreadLocal.get();
            if (prev != null && !prev.isDone()) {
               init_V_CompletableFuture(prev);
            }

            if (close_Z()) {
               CompletableFuture<Void> next = new CompletableFuture<>();
               update_ThreadLocal.set(next);

               try {
                  mc.execute(() -> next.complete(null));
               } catch (Throwable ignored) {
                  next.complete(null);
               }
            }
         }
      }
   }

   private static void init_V_BooleanSupplier_I(BooleanSupplier predicate, int timeoutMs) {
      long start = System.currentTimeMillis();

      while (close_Z()) {
         try {
            if (predicate.getAsBoolean()) {
               return;
            }
         } catch (Throwable var5) {
         }

         if (timeoutMs > 0 && System.currentTimeMillis() - start >= timeoutMs) {
            return;
         }

         CompletableFuture<Void> tick = MacroWaitRegistry.load_CompletableFuture();
         init_V_CompletableFuture(tick);
      }
   }

   private static BooleanSupplier init_BooleanSupplier_ax_Minecraft(WaitForGuiAction action, Minecraft mc) {
      AtomicBoolean sawMatchingGui = new AtomicBoolean(action.init(mc));
      return () -> {
         Optional<Map<String, MacroVarValue>> capture = mc == null ? Optional.empty() : action.init(mc.gui.screen());
         boolean matches = capture.isPresent();
         if (matches) {
            capture.ifPresent(values -> init_V_MacroAction_Map(action, (Map<String, MacroVarValue>)values));
            sawMatchingGui.set(true);
            return false;
         } else {
            return sawMatchingGui.get();
         }
      };
   }

   private static BooleanSupplier init_BooleanSupplier_aJ_Minecraft(WaitGuiTypeAction action, Minecraft mc) {
      AtomicBoolean sawMatchingGui = new AtomicBoolean(action.init_Z_3(mc));
      return () -> {
         Optional<Map<String, MacroVarValue>> capture = action.init_Optional(mc);
         boolean matches = capture.isPresent();
         if (matches) {
            capture.ifPresent(values -> init_V_MacroAction_Map(action, (Map<String, MacroVarValue>)values));
            sawMatchingGui.set(true);
            return false;
         } else {
            return sawMatchingGui.get();
         }
      };
   }

   private static void init_V_Minecraft_ar(Minecraft mc, WaitDurabilityAction action) {
      AtomicReference<MacroExecutor.Inner_JsonUtil> sessionRef = new AtomicReference<>();
      long start = System.currentTimeMillis();

      while (close_Z()) {
         if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> init_Z_Minecraft_8(mc, action, sessionRef), Boolean.FALSE))) {
            return;
         }

         if (action.timeoutMs > 0 && System.currentTimeMillis() - start >= action.timeoutMs) {
            return;
         }

         pop();
      }
   }

   private static boolean init_Z_Minecraft_ar(Minecraft mc, WaitDurabilityAction action) {
      if (action != null && action.useNext) {
         MacroExecutor.Inner_JsonUtil session = init_MacroExecutor_Json_Minecraft_ar(mc, action);
         if (session == null || session.read) {
            return action.init(mc);
         }

         if (session.init_Z) {
            ItemStack stack = init_ItemStack_Minecraft_I(mc, session.init_I);
            if (session.init_Str.isBlank() && init_Z_ar(action, session, stack)) {
               session.init_Str = init_Str_ItemStack(stack);
            }

            return session.init_Str.isBlank()
               ? false
               : init_Z_ItemStack_Str(stack, session.init_Str)
                  && action.init_Z(stack)
                  && init_MacroExecutor_NetH_Minecraft(mc, action, session, session.init_I) == null;
         } else {
            return session.init_Str.isBlank() ? action.init(mc) : init_MacroExecutor_NetH_Minecraft(mc, action, session, -1) == null;
         }
      } else {
         return action != null && action.init(mc);
      }
   }

   private static boolean init_Z_Minecraft_8(Minecraft mc, WaitDurabilityAction action, AtomicReference<MacroExecutor.Inner_JsonUtil> sessionRef) {
      if (mc != null && mc.player != null && action != null) {
         MacroExecutor.Inner_JsonUtil session = sessionRef.get();
         if (session == null) {
            session = init_MacroExecutor_Json_Minecraft_ar(mc, action);
            sessionRef.set(session);
         }

         if (session == null || session.read) {
            return action.init(mc);
         }

         if (session.init_Z) {
            return init_Z_Minecraft_7(mc, action, session);
         }

         init_V_Minecraft_MacroExecutor_Json(mc, session);
         ItemStack active = init_ItemStack_Minecraft_I(mc, session.init_I);
         return init_Z_ItemStack_Str(active, session.init_Str) && !action.init_Z(active) ? false : init_Z_Minecraft_3(mc, action, session, session.init_I);
      } else {
         return false;
      }
   }

   private static boolean init_Z_Minecraft_7(Minecraft mc, WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session) {
      ItemStack stack = init_ItemStack_Minecraft_I(mc, session.init_I);
      if (session.init_Str.isBlank() && init_Z_ar(action, session, stack)) {
         session.init_Str = init_Str_ItemStack(stack);
      }

      if (!session.init_Str.isBlank() && init_Z_ItemStack_Str(stack, session.init_Str)) {
         session.load_Z = true;
         if (!action.init_Z(stack)) {
            return false;
         }

         session.init_Z = false;
         return init_Z_Minecraft_3(mc, action, session, session.init_I);
      } else if (session.load_Z) {
         session.init_Z = false;
         return init_Z_Minecraft_3(mc, action, session, session.init_I);
      } else {
         return false;
      }
   }

   private static boolean init_Z_Minecraft_3(Minecraft mc, WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session, int excludeSlot) {
      MacroExecutor.Inner_NetHelper replacement = init_MacroExecutor_NetH_Minecraft(mc, action, session, excludeSlot);
      if (replacement == null) {
         return true;
      } else {
         return !init_Z_Minecraft_5(mc, session, replacement) ? false : false;
      }
   }

   private static MacroExecutor.Inner_JsonUtil init_MacroExecutor_Json_Minecraft_ar(Minecraft mc, WaitDurabilityAction action) {
      if (mc != null && mc.player != null && action != null) {
         MacroExecutor.Inner_JsonUtil session = new MacroExecutor.Inner_JsonUtil();
         session.load_I = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         MacroItemTarget target = action.targetMode == WaitDurabilityAction.Inner_CachePool.ITEM ? MacroItemTarget.update(action.itemName) : new MacroItemTarget();
         session.init_ArrayUtil = target;
         switch (action.targetMode) {
            case SLOT:
               session.init_I = Math.max(0, Math.min(40, action.slot));
               session.init_Z = true;
               ItemStack stack = init_ItemStack_Minecraft_I(mc, session.init_I);
               if (init_Z_ar(action, session, stack)) {
                  session.init_Str = init_Str_ItemStack(stack);
                  session.load_Z = true;
               }
               break;
            case HELD:
               init_V_Minecraft_4(mc, action, session);
               break;
            case ITEM:
               init_V_Minecraft_4(mc, action, session, target);
         }

         if (session.init_Str == null) {
            session.init_Str = "";
         }

         if (session.init_Str.isBlank() && !session.init_Z) {
            session.read = true;
         }

         return session;
      } else {
         return null;
      }
   }

   private static void init_V_Minecraft_4(Minecraft mc, WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session) {
      int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
      ItemStack main = mc.player.getInventory().getItem(selected);
      if (main != null && !main.isEmpty() && main.isDamageableItem()) {
         session.init_I = selected;
         session.init_Str = init_Str_ItemStack(main);
      } else {
         ItemStack offhand = mc.player.getOffhandItem();
         if (offhand != null && !offhand.isEmpty() && offhand.isDamageableItem()) {
            session.init_I = 40;
            session.init_Str = init_Str_ItemStack(offhand);
         } else {
            session.read = true;
         }
      }
   }

   private static void init_V_Minecraft_4(Minecraft mc, WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session, MacroItemTarget target) {
      if (target != null && target.init_Z()) {
         session.init_I = Math.max(0, Math.min(40, target.init_I));
         session.init_Z = true;
         String explicitId = target.load_Z() ? update(target.flush) : "";
         if (!explicitId.isBlank()) {
            session.init_Str = explicitId;
         }

         ItemStack stack = init_ItemStack_Minecraft_I(mc, session.init_I);
         if (init_Z_ar(action, session, stack)) {
            session.init_Str = init_Str_ItemStack(stack);
            session.load_Z = true;
         }
      } else if (target != null && target.load_Z()) {
         session.init_Str = update(target.flush);
         session.init_I = init_I_Minecraft_2(mc, action, session.init_Str, target);
      } else if (target != null && target.update()) {
         int slot = load(mc, action, "", target);
         if (slot >= 0) {
            session.init_I = slot;
            session.init_Str = init_Str_ItemStack(init_ItemStack_Minecraft_I(mc, slot));
         } else {
            session.read = true;
         }
      } else {
         init_V_Minecraft_4(mc, action, session);
      }
   }

   private static int init_I_Minecraft_2(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target) {
      if (mc != null && mc.player != null) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (init_Z_Minecraft_2(mc, action, lockedRegistryId, target, selected)) {
            return selected;
         } else {
            return init_Z_Minecraft_2(mc, action, lockedRegistryId, target, 40) ? 40 : -1;
         }
      } else {
         return -1;
      }
   }

   private static boolean init_Z_ar(WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session, ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
         return action != null
               && action.targetMode == WaitDurabilityAction.Inner_CachePool.ITEM
               && session != null
               && session.init_ArrayUtil != null
               && session.init_ArrayUtil.update()
            ? session.init_ArrayUtil.init_I_ItemStack_I(stack, session.init_I) >= 0
            : true;
      } else {
         return false;
      }
   }

   private static int load(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target) {
      if (mc != null && mc.player != null) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (init_Z_Minecraft_2(mc, action, lockedRegistryId, target, selected)) {
            return selected;
         }

         for (int slot = 0; slot < 9; slot++) {
            if (slot != selected && init_Z_Minecraft_2(mc, action, lockedRegistryId, target, slot)) {
               return slot;
            }
         }

         for (int slot = 9; slot < Math.min(36, mc.player.getInventory().getContainerSize()); slot++) {
            if (init_Z_Minecraft_2(mc, action, lockedRegistryId, target, slot)) {
               return slot;
            }
         }

         return init_Z_Minecraft_2(mc, action, lockedRegistryId, target, 40) ? 40 : -1;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_Minecraft_2(Minecraft mc, WaitDurabilityAction action, String lockedRegistryId, MacroItemTarget target, int slot) {
      ItemStack stack = init_ItemStack_Minecraft_I(mc, slot);
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
         return lockedRegistryId != null && !lockedRegistryId.isBlank() && !init_Z_ItemStack_Str(stack, lockedRegistryId)
            ? false
            : target == null || !target.update() || target.init_I_ItemStack_I(stack, slot) >= 0;
      } else {
         return false;
      }
   }

   private static void init_V_Minecraft_MacroExecutor_Json(Minecraft mc, MacroExecutor.Inner_JsonUtil session) {
      if (mc != null && mc.player != null && session != null && !session.init_Str.isBlank()) {
         int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
         if (init_Z_ItemStack_Str(mc.player.getInventory().getItem(selected), session.init_Str)) {
            session.init_I = selected;
            session.load_I = selected;
         } else if (init_Z_ItemStack_Str(mc.player.getOffhandItem(), session.init_Str)) {
            ItemStack active = init_ItemStack_Minecraft_I(mc, session.init_I);
            if (!init_Z_ItemStack_Str(active, session.init_Str)) {
               session.init_I = 40;
            }
         }
      }
   }

   private static MacroExecutor.Inner_NetHelper init_MacroExecutor_NetH_Minecraft(
      Minecraft mc, WaitDurabilityAction action, MacroExecutor.Inner_JsonUtil session, int excludeSlot
   ) {
      if (mc != null && mc.player != null && action != null && session != null && !session.init_Str.isBlank()) {
         MacroExecutor.Inner_NetHelper hotbar = null;
         MacroExecutor.Inner_NetHelper inventory = null;
         MacroExecutor.Inner_NetHelper offhand = null;

         for (int slot = 0; slot < 9; slot++) {
            MacroExecutor.Inner_NetHelper candidate = init_MacroExecutor_NetH_Minecraft(mc, action, session.init_Str, slot, excludeSlot);
            if (candidate != null && (hotbar == null || candidate.load() > hotbar.load())) {
               hotbar = candidate;
            }
         }

         int size = Math.min(36, mc.player.getInventory().getContainerSize());

         for (int slot = 9; slot < size; slot++) {
            MacroExecutor.Inner_NetHelper candidate = init_MacroExecutor_NetH_Minecraft(mc, action, session.init_Str, slot, excludeSlot);
            if (candidate != null && (inventory == null || candidate.load() > inventory.load())) {
               inventory = candidate;
            }
         }

         offhand = init_MacroExecutor_NetH_Minecraft(mc, action, session.init_Str, 40, excludeSlot);
         if (hotbar != null) {
            return hotbar;
         } else {
            return inventory != null ? inventory : offhand;
         }
      } else {
         return null;
      }
   }

   private static MacroExecutor.Inner_NetHelper init_MacroExecutor_NetH_Minecraft(Minecraft mc, WaitDurabilityAction action, String registryId, int slot, int excludeSlot) {
      if (slot == excludeSlot) {
         return null;
      } else {
         ItemStack stack = init_ItemStack_Minecraft_I(mc, slot);
         if (!init_Z_ItemStack_Str(stack, registryId)) {
            return null;
         } else {
            return action.init_Z(stack) ? null : new MacroExecutor.Inner_NetHelper(slot, init_I_ItemStack(stack));
         }
      }
   }

   private static boolean init_Z_Minecraft_5(Minecraft mc, MacroExecutor.Inner_JsonUtil session, MacroExecutor.Inner_NetHelper replacement) {
      if (mc != null && mc.player != null && replacement != null) {
         int slot = replacement.init();
         if (slot >= 0 && slot < 9) {
            autismclient.util.InventorySlotHelper.init_V(mc, slot);
            session.init_I = slot;
            session.load_I = slot;
            return true;
         }

         if (slot >= 9 && slot < 36) {
            int target = Math.max(0, Math.min(8, session.load_I));
            if (!autismclient.util.InventorySlotHelper.read(mc, slot, target)) {
               return false;
            }

            autismclient.util.InventorySlotHelper.init_V(mc, target);
            session.init_I = target;
            session.load_I = target;
            return true;
         } else if (slot == 40) {
            session.init_I = 40;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static ItemStack init_ItemStack_Minecraft_I(Minecraft mc, int visibleSlot) {
      if (mc == null || mc.player == null) {
         return ItemStack.EMPTY;
      } else if (visibleSlot == 40) {
         return mc.player.getOffhandItem();
      } else {
         return visibleSlot >= 0 && visibleSlot < mc.player.getInventory().getContainerSize() ? mc.player.getInventory().getItem(visibleSlot) : ItemStack.EMPTY;
      }
   }

   private static boolean init_Z_ItemStack_Str(ItemStack stack, String registryId) {
      return stack != null && !stack.isEmpty() && stack.isDamageableItem() && init_Str_ItemStack(stack).equals(registryId);
   }

   private static int init_I_ItemStack(ItemStack stack) {
      return stack != null && stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 0;
   }

   private static String init_Str_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return id == null ? "" : id.toString();
      } else {
         return "";
      }
   }

   private static String update(String raw) {
      if (raw != null && !raw.isBlank()) {
         String trimmed = raw.trim().toLowerCase(Locale.ROOT);
         Identifier parsed = Identifier.tryParse(trimmed.contains(":") ? trimmed : "minecraft:" + trimmed);
         return parsed == null ? "" : BuiltInRegistries.ITEM.getOptional(parsed).map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).orElse("");
      } else {
         return "";
      }
   }

   private static CompletableFuture<Void> init_CompletableFuture_BooleanSupplier_I(BooleanSupplier predicate, int timeoutMs) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      Thread worker = new Thread(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
         long start = System.currentTimeMillis();

         while (!future.isDone() && close_Z()) {
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

   private static void init_V_Minecraft_6(Minecraft mc, List<MacroAction> actions, int currentIndex) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && actions != null) {
         int nextIndex = currentIndex + 1;
         if (nextIndex >= 0 && nextIndex < actions.size()) {
            MacroAction next = actions.get(nextIndex);
            if (next != null
               && next.isEnabled()
               && next.listensDuringPreviousAction()
               && DynamicBindingManager.init(next, mc)
               && init_Z_MacroAction_Minecraft(next, mc)
               && init_Z_MacroAction(next)) {
               synchronized (run.init_IdentityHashMap) {
                  if (!run.init_IdentityHashMap.containsKey(next)) {
                     init_V_MacroAction(next);
                     load_V_MacroAction(next);
                     MacroExecutor.Inner_ByteReader wait = init_MacroExecutor_Byte_Minecraft_MacroAction(mc, next);
                     if (wait != null) {
                        run.init_IdentityHashMap.put(next, wait);
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean init_Z_MacroAction(MacroAction action) {
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

   private static MacroExecutor.Inner_ByteReader init_MacroExecutor_Byte_MacroAction(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null) {
         synchronized (run.init_IdentityHashMap) {
            return run.init_IdentityHashMap.remove(action);
         }
      } else {
         return null;
      }
   }

   private static boolean init_Z_MacroAction_J(MacroAction action, long timeoutMs) {
      MacroExecutor.Inner_ByteReader wait = init_MacroExecutor_Byte_MacroAction(action);
      if (wait == null) {
         return false;
      }

      try {
         load_Z_CompletableFuture_J(wait.init_CompletableFuture, timeoutMs);
         read_V(action);
         return true;
      } finally {
         wait.init();
      }
   }

   private static MacroExecutor.Inner_ByteReader init_MacroExecutor_Byte_Minecraft_MacroAction(Minecraft mc, MacroAction action) {
      if (action instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.init();
         String firstTarget = targets.isEmpty() ? "" : targets.get(0);
         return init_MacroExecutor_Byte_MacroAction(packetAction, firstTarget, 1);
      } else if (action instanceof WaitPacketMatchAction packetMatch) {
         return init_MacroExecutor_Byte_aM(packetMatch);
      } else if (action instanceof WaitInventoryPredicateAction inv) {
         inv.init();
         return init_MacroExecutor_Byte_MacroAction_2(action, () -> inv.init(mc), inv.timeoutMs);
      } else if (action instanceof WaitDurabilityAction durability) {
         return durability.useNext ? null : init_MacroExecutor_Byte_MacroAction_2(action, () -> init_Z_Minecraft_ar(mc, durability), durability.timeoutMs);
      } else if (action instanceof WaitFreeSlotsAction freeSlots) {
         return init_MacroExecutor_Byte_MacroAction_2(action, () -> freeSlots.init(mc), freeSlots.timeoutMs);
      } else if (action instanceof WaitEntityTargetAction entityTarget) {
         return init_MacroExecutor_Byte_MacroAction_2(action, () -> entityTarget.init(mc), entityTarget.timeoutMs);
      } else if (action instanceof WaitGuiTypeAction guiType) {
         return init_MacroExecutor_Byte_MacroAction_2(
            action, guiType.waitMode == WaitGuiTypeAction.Inner_DataUtil.CLOSE ? init_BooleanSupplier_aJ_Minecraft(guiType, mc) : () -> {
               Optional<Map<String, MacroVarValue>> capture = guiType.init_Optional(mc);
               capture.ifPresent(values -> init_V_MacroAction_Map(guiType, (Map<String, MacroVarValue>)values));
               return capture.isPresent();
            }, guiType.timeoutMs
         );
      } else if (action instanceof WaitForChatAction chat) {
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         MacroExecutor.Inner_EventBus listener = init_MacroExecutor_Even_Predicate(packet -> {
            MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
            Optional<Map<String, MacroVarValue>> matched = init_Optional_au(chat, capture, run == null ? null : run.init_m);
            if (matched.isEmpty()) {
               return false;
            }

            if (run != null) {
               run.load_Map.put(chat, matched.get());
            }

            return true;
         });
         CompletableFuture<?> future = listener.init_CompletableFuture;
         if (chat.timeoutMs > 0) {
            future.completeOnTimeout(null, chat.timeoutMs, TimeUnit.MILLISECONDS);
         }

         return new MacroExecutor.Inner_ByteReader(action, future, listener::init_V, 0);
      } else if (action instanceof WaitForMacroStepAction macroStep) {
         return init_MacroExecutor_Byte_MacroAction_2(action, () -> init_Z_aA(macroStep), macroStep.timeoutMs);
      } else if (action instanceof WaitForLanStepAction lanStep) {
         CompletableFuture<Void> future = MacroWaitRegistry.init(lanStep);
         return new MacroExecutor.Inner_ByteReader(action, future, () -> {
            if (!future.isDone()) {
               future.cancel(true);
            }
         }, 0);
      } else {
         CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, action);
         if (future == null) {
            return null;
         }

         int timeout = action instanceof WaitForChatAction chat ? chat.timeoutMs : 0;
         if (timeout > 0) {
            future.completeOnTimeout(null, timeout, TimeUnit.MILLISECONDS);
         }

         return new MacroExecutor.Inner_ByteReader(action, future, () -> {
            if (!future.isDone()) {
               future.cancel(true);
            }
         }, 0);
      }
   }

   private static boolean load_Z_MacroAction(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null) {
         synchronized (run.init_IdentityHashMap) {
            return run.init_IdentityHashMap.containsKey(action);
         }
      } else {
         return false;
      }
   }

   private static void init_V_MacroAction(MacroAction action, Packet<?> packet, String direction) {
      if (action instanceof WaitForPacketAction wait) {
         wait.matchedPacket = packet;
         wait.matchedDirection = direction == null ? "" : direction;
      } else if (action instanceof WaitPacketMatchAction wait) {
         wait.matchedPacket = packet;
         wait.matchedDirection = direction == null ? "" : direction;
      }
   }

   private static void init_V_aB(WaitForPacketAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (action != null && run != null && run.init_Packet != null) {
         action.matchedPacket = run.init_Packet;
         action.matchedDirection = run.load_Str;
      }
   }

   private static void init_V_MacroAction(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null) {
         run.load_Map.remove(action);
         if (action instanceof WaitForChatAction chat) {
            for (String name : PatternCaptureMatcher.init(chat.init_UtilCore_DataUtil(), chat.pattern)) {
               run.init_m.init_V_Str(name);
            }

            if (chat.saveAs != null && !chat.saveAs.isBlank()) {
               run.init_m.init_V_Str(chat.saveAs);
            }
         } else if (action instanceof WaitForGuiAction gui) {
            for (String name : PatternCaptureMatcher.init(gui.matchMode, gui.guiTitle)) {
               run.init_m.init_V_Str(name);
            }

            if (gui.saveAs != null && !gui.saveAs.isBlank()) {
               run.init_m.init_V_Str(gui.saveAs);
            }
         } else if (action instanceof WaitGuiTypeAction gui) {
            for (String name : PatternCaptureMatcher.init(gui.matchMode, gui.title)) {
               run.init_m.init_V_Str(name);
            }

            if (gui.saveAs != null && !gui.saveAs.isBlank()) {
               run.init_m.init_V_Str(gui.saveAs);
            }
         } else if (action instanceof CaptureValueAction capture) {
            for (String name : PatternCaptureMatcher.init(capture.matchMode, capture.pattern)) {
               run.init_m.init_V_Str(name);
            }

            if (capture.saveAs != null && !capture.saveAs.isBlank()) {
               run.init_m.init_V_Str(capture.saveAs);
            }
         }
      }
   }

   private static void load_V_MacroAction(MacroAction action) {
      if (action instanceof CaptureOutputAction output) {
         String name = output.load();
         if (name != null && !name.isBlank()) {
            MacroVariableContext.init_V_Str(name);
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

   private static void init_V_MacroAction_Minecraft(MacroAction action, Minecraft mc) {
      if (action instanceof CaptureOutputAction output) {
         String name = MacroVariableStore.load(output.load());
         if (!name.isEmpty()) {
            MacroVarValue value = load_Obj_Minecraft(mc, () -> init_l_MacroAction_Minecraft(action, mc), null);
            if (value != null) {
               MacroVariableContext.init_V_Str_l(name, value);
            }
         }
      }
   }

   private static MacroVarValue init_l_MacroAction_Minecraft(MacroAction action, Minecraft mc) {
      if (action instanceof WaitForHealthAction healthAction && mc != null && mc.player != null) {
         float health = mc.player.getHealth();
         return (healthAction.below ? health >= healthAction.healthThreshold : health <= healthAction.healthThreshold)
            ? null
            : MacroVarValue.init(
               MacroVarValue.Inner_DataUtil.NUMBER,
               Float.toString(health),
               Map.of(
                  "health",
                  MacroVarValue.init_l_Number(health),
                  "max",
                  MacroVarValue.init_l_Number(mc.player.getMaxHealth()),
                  "absorption",
                  MacroVarValue.init_l_Number(mc.player.getAbsorptionAmount())
               )
            );
      } else if (action instanceof WaitForEntityAction entityAction) {
         Entity entity = entityAction.init_Entity(mc);
         if (entity == null) {
            return null;
         }

         String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
         double distance = mc != null && mc.player != null ? mc.player.distanceTo(entity) : 0.0;
         return MacroVarValue.init(
            MacroVarValue.Inner_DataUtil.ENTITY,
            entity.getName().getString(),
            Map.of(
               "name",
               MacroVarValue.init_l_Obj(entity.getName().getString()),
               "id",
               MacroVarValue.init_l_Str(id),
               "uuid",
               MacroVarValue.init_l_Obj(entity.getStringUUID()),
               "x",
               MacroVarValue.init_l_Number(entity.getX()),
               "y",
               MacroVarValue.init_l_Number(entity.getY()),
               "z",
               MacroVarValue.init_l_Number(entity.getZ()),
               "distance",
               MacroVarValue.init_l_Number(distance)
            )
         );
      } else if (action instanceof WaitForSoundAction sound) {
         return sound.matchedSoundId != null && !sound.matchedSoundId.isBlank()
            ? MacroVarValue.init(
               MacroVarValue.Inner_DataUtil.IDENTIFIER,
               sound.matchedSoundId,
               Map.of(
                  "id",
                  MacroVarValue.init_l_Str(sound.matchedSoundId),
                  "x",
                  MacroVarValue.init_l_Number(sound.matchedX),
                  "y",
                  MacroVarValue.init_l_Number(sound.matchedY),
                  "z",
                  MacroVarValue.init_l_Number(sound.matchedZ)
               )
            )
            : null;
      } else if (action instanceof WaitForPacketAction packet) {
         return init_l_Packet_Str(packet.matchedPacket, packet.matchedDirection);
      } else if (action instanceof WaitPacketMatchAction packet) {
         return init_l_Packet_Str(packet.matchedPacket, packet.matchedDirection);
      } else if (action instanceof WaitInventoryPredicateAction inventory) {
         return !inventory.init(mc) ? null : init_l_Minecraft(mc, inventory.itemName, inventory.slot, inventory.condition.name());
      } else if (action instanceof WaitForSlotChangeAction slotWait) {
         if (slotWait.entries.isEmpty()) {
            return init_l_Minecraft(mc, "", -1, "slot_change");
         }

         MacroItemTarget target = slotWait.entries.get(0).init_ArrayUtil().init_ArrayUtil_Minecraft(mc);
         return target == null ? null : init_l_Minecraft_ArrayUtil(mc, target);
      } else if (action instanceof WaitPosAction || action instanceof WaitForTeleportAction || action instanceof WaitForPositionDeltaAction) {
         return init_l_Minecraft(mc);
      } else if (action instanceof WaitForWorldChangeAction && mc != null && mc.level != null) {
         String dimension = mc.level.dimension().identifier().toString();
         return MacroVarValue.init(MacroVarValue.Inner_DataUtil.IDENTIFIER, dimension, Map.of("id", MacroVarValue.init_l_Str(dimension), "name", MacroVarValue.init_l_Obj(dimension)));
      } else if (action instanceof WaitGamemodeChangeAction && mc != null && mc.gameMode != null) {
         String mode = mc.gameMode.getPlayerMode().getName();
         WaitGamemodeChangeAction wait = (WaitGamemodeChangeAction)action;
         return wait.match == WaitGamemodeChangeAction.Inner_DataUtil.TO_MODE && !wait.init(mc.gameMode.getPlayerMode())
            ? null
            : MacroVarValue.init(MacroVarValue.Inner_DataUtil.TEXT, mode, Map.of("name", MacroVarValue.init_l_Obj(mode)));
      } else {
         return null;
      }
   }

   private static MacroVarValue init_l_Minecraft(Minecraft mc) {
      if (mc != null && mc.player != null) {
         String display = String.format(Locale.ROOT, "%.3f, %.3f, %.3f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
         return MacroVarValue.init(
            MacroVarValue.Inner_DataUtil.POSITION,
            display,
            Map.of(
               "x",
               MacroVarValue.init_l_Number(mc.player.getX()),
               "y",
               MacroVarValue.init_l_Number(mc.player.getY()),
               "z",
               MacroVarValue.init_l_Number(mc.player.getZ()),
               "yaw",
               MacroVarValue.init_l_Number(mc.player.getYRot()),
               "pitch",
               MacroVarValue.init_l_Number(mc.player.getXRot())
            )
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue init_l_Minecraft(Minecraft mc, String itemTemplate, int slot, String condition) {
      if (mc != null && mc.player != null) {
         MacroTemplateResolver.Inner_CachePool item = MacroVariableContext.init_j_CachePool_Str_Minecraft(itemTemplate, mc);
         if (!item.init_Z()) {
            return null;
         }

         MacroItemTarget target = MacroItemTarget.update(item.init_Str());
         if (slot >= 0) {
            int safeSlot = Math.max(0, Math.min(mc.player.getInventory().getContainerSize() - 1, slot));
            MacroVarValue stack = init_l_ItemStack_I(mc.player.getInventory().getItem(safeSlot), safeSlot);
            return stack == null
               ? MacroVarValue.init(
                  MacroVarValue.Inner_DataUtil.SLOT,
                  Integer.toString(slot),
                  Map.of("slot", MacroVarValue.init_l_I(slot), "empty", MacroVarValue.init_l_Obj("true"), "condition", MacroVarValue.init_l_Obj(condition))
               )
               : stack;
         }

         if (target.update()) {
            MacroVarValue matched = init_l_Minecraft_ArrayUtil(mc, target);
            if (matched != null) {
               return matched.init("condition", MacroVarValue.init_l_Obj(condition));
            }
         }

         return MacroVarValue.init(
            MacroVarValue.Inner_DataUtil.TEXT,
            condition,
            Map.of("condition", MacroVarValue.init_l_Obj(condition), "selected_slot", MacroVarValue.init_l_I(mc.player.getInventory().getSelectedSlot()))
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue init_l_Minecraft_ArrayUtil(Minecraft mc, MacroItemTarget target) {
      if (mc != null && mc.player != null && target != null) {
         if (mc.player.containerMenu != null) {
            for (Slot slot : mc.player.containerMenu.slots) {
               if (slot != null && !slot.getItem().isEmpty()) {
                  int visible = autismclient.util.InventorySlotHelper.init_I(mc, slot.index);
                  if (target.init_I_ItemStack_I(slot.getItem(), visible) >= 0) {
                     return init_l_ItemStack_I(slot.getItem(), visible);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static MacroVarValue init_l_ItemStack_I(ItemStack stack, int slot) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String itemId = id == null ? "" : id.toString();
         String name = stack.getHoverName().getString();
         return MacroVarValue.init(
            MacroVarValue.Inner_DataUtil.ITEM,
            name,
            Map.of("name", MacroVarValue.init_l_Obj(name), "id", MacroVarValue.init_l_Str(itemId), "count", MacroVarValue.init_l_Number(stack.getCount()), "slot", MacroVarValue.init_l_I(slot))
         );
      } else {
         return null;
      }
   }

   private static MacroVarValue init_l_Packet_Str(Packet<?> packet, String direction) {
      if (packet == null) {
         return null;
      }

      LinkedHashMap<String, MacroVarValue> properties = new LinkedHashMap<>();
      String packetName = PacketNameResolver.init_Str_Packet_Str(packet, direction);
      properties.put("name", MacroVarValue.init_l_Obj(packetName));
      properties.put("class", MacroVarValue.init_l_Obj(packet.getClass().getName()));
      properties.put("direction", MacroVarValue.init_l_Obj(direction));
      RecordComponent[] components = packet.getClass().getRecordComponents();
      if (components != null) {
         for (RecordComponent component : components) {
            if (properties.size() >= 64) {
               break;
            }

            try {
               Object value = component.getAccessor().invoke(packet);
               properties.put(component.getName().toLowerCase(Locale.ROOT), MacroVarValue.init_l_Obj(value));
            } catch (ReflectiveOperationException var10) {
            }
         }
      }

      return MacroVarValue.init(MacroVarValue.Inner_DataUtil.PACKET, packetName, properties);
   }

   private static void init_V_MacroAction_Map(MacroAction action, Map<String, MacroVarValue> values) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null && values != null) {
         run.load_Map.put(action, Map.copyOf(values));
      }
   }

   private static void read_V(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      if (run != null && action != null) {
         Map<String, MacroVarValue> values = run.load_Map.remove(action);
         if (values != null) {
            run.init_m.init_V_Map(values);
         }
      }
   }

   private static MacroExecutor.Inner_ByteReader init_MacroExecutor_Byte_MacroAction_2(MacroAction action, BooleanSupplier predicate, int timeoutMs) {
      AtomicBoolean active = new AtomicBoolean(true);
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      Thread worker = new Thread(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
         long start = System.currentTimeMillis();

         while (active.get() && close_Z() && !future.isDone()) {
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
      return new MacroExecutor.Inner_ByteReader(action, future, () -> {
         active.set(false);
         if (!future.isDone()) {
            future.cancel(true);
         }

         worker.interrupt();
      }, 0);
   }

   private static MacroExecutor.Inner_ByteReader init_MacroExecutor_Byte_MacroAction(MacroAction action, String target, int completedPacketTargets) {
      String normalizedTarget = WaitForPacketAction.read(target);
      String direction = WaitForPacketAction.init(normalizedTarget);
      CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
      MacroExecutor.Inner_EventBus send = null;
      MacroExecutor.Inner_EventBus recv = null;
      if (!"S2C".equalsIgnoreCase(direction)) {
         send = load_MacroExecutor_Even_Predicate(packet -> init_Z_Str_2(normalizedTarget, packet, "C2S"));
         send.init_CompletableFuture.thenAccept(packet -> {
            init_V_MacroAction(action, (Packet<?>)packet, "C2S");
            matched.complete((Packet<?>)packet);
         });
      }

      if (!"C2S".equalsIgnoreCase(direction)) {
         recv = init_MacroExecutor_Even_Predicate(packet -> init_Z_Str_2(normalizedTarget, packet, "S2C"));
         recv.init_CompletableFuture.thenAccept(packet -> {
            init_V_MacroAction(action, (Packet<?>)packet, "S2C");
            matched.complete((Packet<?>)packet);
         });
      }

      MacroExecutor.Inner_EventBus finalSend = send;
      MacroExecutor.Inner_EventBus finalRecv = recv;
      return new MacroExecutor.Inner_ByteReader(action, matched, () -> {
         if (finalSend != null) {
            finalSend.init_V();
         }

         if (finalRecv != null) {
            finalRecv.init_V();
         }

         if (!matched.isDone()) {
            matched.cancel(true);
         }
      }, completedPacketTargets);
   }

   private static MacroExecutor.Inner_ByteReader init_MacroExecutor_Byte_aM(WaitPacketMatchAction action) {
      MacroExecutor.Inner_EventBus send = null;
      MacroExecutor.Inner_EventBus recv = null;
      CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
      if (action.direction != WaitPacketMatchAction.Inner_DataUtil.S2C) {
         send = load_MacroExecutor_Even_Predicate(packet -> action.init_Z_Packet_Str(packet, "C2S"));
         send.init_CompletableFuture.thenAccept(packet -> {
            init_V_MacroAction(action, (Packet<?>)packet, "C2S");
            matched.complete((Packet<?>)packet);
         });
      }

      if (action.direction != WaitPacketMatchAction.Inner_DataUtil.C2S) {
         recv = init_MacroExecutor_Even_Predicate(packet -> action.init_Z_Packet_Str(packet, "S2C"));
         recv.init_CompletableFuture.thenAccept(packet -> {
            init_V_MacroAction(action, (Packet<?>)packet, "S2C");
            matched.complete((Packet<?>)packet);
         });
      }

      if (send == null && recv == null) {
         return null;
      }

      if (action.timeoutMs > 0) {
         matched.completeOnTimeout(null, action.timeoutMs, TimeUnit.MILLISECONDS);
      }

      MacroExecutor.Inner_EventBus finalSend = send;
      MacroExecutor.Inner_EventBus finalRecv = recv;
      return new MacroExecutor.Inner_ByteReader(action, matched, () -> {
         if (finalSend != null) {
            finalSend.init_V();
         }

         if (finalRecv != null) {
            finalRecv.init_V();
         }

         if (!matched.isDone()) {
            matched.cancel(true);
         }
      }, 0);
   }

   private static void init_V_aM_2(WaitPacketMatchAction action) {
      if (action != null) {
         MacroExecutor.Inner_EventBus send = null;
         MacroExecutor.Inner_EventBus recv = null;

         try {
            if (action.direction != WaitPacketMatchAction.Inner_DataUtil.S2C) {
               send = load_MacroExecutor_Even_Predicate(packet -> {
                  boolean matches = action.init_Z_Packet_Str(packet, "C2S");
                  if (matches) {
                     init_V_MacroAction(action, packet, "C2S");
                  }

                  return matches;
               });
            }

            if (action.direction != WaitPacketMatchAction.Inner_DataUtil.C2S) {
               recv = init_MacroExecutor_Even_Predicate(packet -> {
                  boolean matches = action.init_Z_Packet_Str(packet, "S2C");
                  if (matches) {
                     init_V_MacroAction(action, packet, "S2C");
                  }

                  return matches;
               });
            }

            CompletableFuture<?> future;
            if (send != null && recv != null) {
               future = CompletableFuture.anyOf(send.init_CompletableFuture, recv.init_CompletableFuture);
            } else if (send != null) {
               future = send.init_CompletableFuture;
            } else {
               if (recv == null) {
                  return;
               }

               future = recv.init_CompletableFuture;
            }

            if (action.timeoutMs > 0) {
               future.completeOnTimeout(null, action.timeoutMs, TimeUnit.MILLISECONDS);
            }

            load_Z_CompletableFuture_J(future, action.timeoutMs);
         } finally {
            if (send != null) {
               send.init_V();
            }

            if (recv != null) {
               recv.init_V();
            }
         }
      }
   }

   private static void init_V_Minecraft_B(Minecraft mc, PacketGateAction action) {
      if (action != null) {
         action.execute(mc);
         if (action.mode != PacketGateAction.Inner_CachePool.DISABLE_GATE) {
            switch (action.durationMode) {
               case UNTIL_PACKET:
                  WaitPacketMatchAction wait = new WaitPacketMatchAction();
                  WaitPacketMatchAction.Inner_DataUtil var10001;
                  switch (action.direction) {
                     case C2S:
                        var10001 = WaitPacketMatchAction.Inner_DataUtil.C2S;
                        break;
                     case S2C:
                        var10001 = WaitPacketMatchAction.Inner_DataUtil.S2C;
                        break;
                     case ANY:
                        var10001 = WaitPacketMatchAction.Inner_DataUtil.ANY;
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  wait.direction = var10001;
                  wait.packetName = action.untilPacketName != null && !action.untilPacketName.isBlank()
                     ? action.untilPacketName
                     : (action.packetNames.isEmpty() ? "" : action.packetNames.get(0));
                  wait.fieldName = action.untilPacketField;
                  wait.operator = MacroNbtCodecHelper.init(WaitPacketMatchAction.Inner_StreamUtil.class, action.untilPacketOperator, WaitPacketMatchAction.Inner_StreamUtil.EXISTS);
                  wait.value = action.untilPacketValue;
                  wait.timeoutMs = Math.max(0, action.durationValue);
                  init_V_aM_2(wait);
                  load_V_Minecraft_B(mc, action);
                  break;
               case UNTIL_GUI:
                  init_V_BooleanSupplier_I(() -> GuiScreenMatcher.init(mc.gui.screen(), action.untilGuiType, action.untilGuiTitle), Math.max(0, action.durationValue));
                  load_V_Minecraft_B(mc, action);
                  break;
               case UNTIL_INVENTORY:
                  WaitInventoryPredicateAction wait2 = new WaitInventoryPredicateAction();
                  wait2.condition = MacroNbtCodecHelper.init(WaitInventoryPredicateAction.Inner_DataUtil.class, action.untilInventoryCondition, WaitInventoryPredicateAction.Inner_DataUtil.ITEM_EXISTS);
                  wait2.itemName = action.untilInventoryItem;
                  wait2.count = action.untilInventoryCount;
                  wait2.slot = action.untilInventorySlot;
                  wait2.timeoutMs = Math.max(0, action.durationValue);
                  wait2.init();
                  init_V_BooleanSupplier_I(() -> wait2.init(mc), wait2.timeoutMs);
                  load_V_Minecraft_B(mc, action);
            }
         }
      }
   }

   private static void load_V_Minecraft_B(Minecraft mc, PacketGateAction action) {
      PacketGateManager.init(
         action.gateId != null && !action.gateId.isBlank() ? action.gateId : "auto", load_J(), mc == null ? null : mc.getConnection(), action.flushOnDisable
      );
   }

   private static void init_V_MacroExecutor_Tabl_Minecraft(MacroExecutor.Inner_TableIndex run, Minecraft mc) {
      if (run != null && !run.load_CopyOnWriteArrayLi.isEmpty()) {
         ArrayList<MacroAction> actions = new ArrayList<>(run.load_CopyOnWriteArrayLi);
         run.load_CopyOnWriteArrayLi.clear();

         for (MacroAction action : actions) {
            if (action != null && action.isEnabled()) {
               try {
                  init_V_Minecraft_2(mc, action, autismclient.modules.ModuleRuntimeManager.init_PoolEntry());
               } catch (Throwable t) {
                  AutismClientMessaging.sendPrefixed("§cCleanup failed: " + init_Str_Throwable(t));
               }
            }
         }
      }
   }

   private static void init_V_aA(WaitForMacroStepAction action) {
      long startMs = System.currentTimeMillis();

      while (close_Z()) {
         if (init_Z_aA(action)) {
            return;
         }

         if (action.timeoutMs > 0 && System.currentTimeMillis() - startMs >= action.timeoutMs) {
            return;
         }

         CompletableFuture<Void> tick = MacroWaitRegistry.load_CompletableFuture();
         init_V_CompletableFuture(tick);
      }
   }

   private static boolean init_Z_aA(WaitForMacroStepAction action) {
      String target = action.macroName == null ? "" : action.macroName.trim();
      if (target.isEmpty()) {
         return true;
      }

      synchronized (init_Obj) {
         for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
            String name = run.load_aM != null && run.load_aM.init_Str != null ? run.load_aM.init_Str : "";
            if (name.equalsIgnoreCase(target)) {
               boolean var10000;
               switch (action.mode) {
                  case STARTED_STEP:
                     var10000 = run.init_I + 1 >= Math.max(1, action.step);
                     break;
                  case COMPLETED_STEP:
                     var10000 = run.read_I >= Math.max(1, action.step);
                     break;
                  case FINISHED:
                     var10000 = !run.init_AtomicBoolean.get();
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return var10000;
            }
         }
      }

      return action.mode == WaitForMacroStepAction.Inner_DataUtil.FINISHED;
   }

   private static int init_I_Minecraft(Minecraft mc, autismclient.modules.ModuleRuntimeManager module, List<MacroAction> actions, int headerIndex, RaceAction race) {
      int bodyCount = race.init_I_List_I(actions, headerIndex);
      List<MacroAction> conditions = new ArrayList<>();
      List<MacroAction> bodyActions = new ArrayList<>();

      for (int offset = 1; offset <= bodyCount && headerIndex + offset < actions.size(); offset++) {
         MacroAction child = actions.get(headerIndex + offset);
         if (child != null && child.isEnabled()) {
            if (RaceAction.init_Z(child)) {
               conditions.add(child);
            } else if (RaceAction.load(child)) {
               bodyActions.add(child);
            }
         }
      }

      load_V_Str(
         conditions.isEmpty()
            ? "Race: dispatching"
            : "Race: waiting for " + (conditions.size() == 1 ? conditions.get(0).getDisplayName() : conditions.size() + " conditions")
      );
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      CompletableFuture<Void> bodyFuture = new CompletableFuture<>();
      AtomicBoolean dispatched = new AtomicBoolean(false);
      CompletableFuture<?> triggerFuture = init_CompletableFuture_Minecraft_List(mc, conditions);
      triggerFuture.thenRun(() -> {
         if (dispatched.compareAndSet(false, true)) {
            if (run != null && !run.init_AtomicBoolean.get()) {
               bodyFuture.cancel(true);
            } else {
               init_V_Minecraft(mc, module, bodyActions, run, bodyFuture);
            }
         }
      }).exceptionally(error -> {
         if (!bodyFuture.isDone()) {
            bodyFuture.completeExceptionally(error);
         }

         return null;
      });
      boolean fired = init_Z_CompletableFuture_J(triggerFuture, Math.max(0, race.timeoutMs));
      if (fired) {
         load_V_Str("Race: fired");
         init_Z_CompletableFuture_J(bodyFuture, 250L);
         return bodyCount;
      } else {
         triggerFuture.cancel(true);
         bodyFuture.cancel(true);
         AutismClientMessaging.sendPrefixed("§eRace timed out: " + (race.label != null && !race.label.isBlank() ? race.label : "Race"));
         load_V_Str("Race: timeout");
         return bodyCount;
      }
   }

   private static CompletableFuture<?> init_CompletableFuture_Minecraft_List(Minecraft mc, List<MacroAction> conditions) {
      if (conditions != null && !conditions.isEmpty()) {
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         List<CompletableFuture<MacroExecutor.Inner_SyncManager>> baseFutures = new ArrayList<>();
         List<CompletableFuture<MacroExecutor.Inner_SyncManager>> futures = new ArrayList<>();

         for (MacroAction condition : conditions) {
            load_V_MacroAction(condition);
            CompletableFuture<MacroExecutor.Inner_SyncManager> base = init_CompletableFuture_Minecraft_MacroAction(mc, condition);
            baseFutures.add(base);
            futures.add(base.thenApply(event -> {
               if (event.init_Packet() != null) {
                  init_V_MacroAction(condition, event.init_Packet(), event.load());
               }

               if (condition instanceof CaptureOutputAction output) {
                  String name = MacroVariableStore.load(output.load());
                  if (name.isEmpty()) {
                     return (MacroExecutor.Inner_SyncManager)event;
                  }

                  MacroVarValue value = init_l_MacroAction_Minecraft(condition, mc);
                  if (value == null) {
                     return (MacroExecutor.Inner_SyncManager)event;
                  }

                  LinkedHashMap<String, MacroVarValue> captures = new LinkedHashMap<>(event.init_Map());
                  captures.put(name, value);
                  return new MacroExecutor.Inner_SyncManager(event.init_Packet(), event.load(), event.init_J(), event.init_MacroExecutor_Conf(), captures);
               } else {
                  return (MacroExecutor.Inner_SyncManager)event;
               }
            }));
         }

         CompletableFuture<Object> first = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
         CompletableFuture<Object> out = new CompletableFuture<>();
         first.whenComplete((value, e) -> {
            if (e != null) {
               out.completeExceptionally(e);
            } else {
               if (value instanceof MacroExecutor.Inner_SyncManager event && run != null && !event.init_Map().isEmpty()) {
                  run.init_m.init_V_Map(event.init_Map());
               }

               out.complete(value);
            }
         });
         out.whenComplete((v, e) -> {
            for (CompletableFuture<MacroExecutor.Inner_SyncManager> future : baseFutures) {
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

   private static int init_I_Minecraft_3(Minecraft mc, List<MacroAction> actions, int headerIndex, ReportAction report) {
      int conditionCount = report.init(actions, headerIndex);
      if (conditionCount >= 2 && headerIndex + 2 < actions.size()) {
         MacroAction startCondition = actions.get(headerIndex + 1);
         MacroAction endCondition = actions.get(headerIndex + 2);
         if (RaceAction.load(startCondition) && RaceAction.init_Z(endCondition)) {
            int timeoutMs = Math.max(100, report.timeoutMs);
            String label = report.reportLabel != null && !report.reportLabel.isBlank() ? report.reportLabel.trim() : "Report";
            load_V_Str("Report: start " + startCondition.getDisplayName());
            CompletableFuture<MacroExecutor.Inner_SyncManager> startAnchor = init_CompletableFuture_MacroAction(startCondition);
            init_V_Minecraft_2(mc, startCondition, null);
            if (!close_Z()) {
               return conditionCount;
            }

            MacroExecutor.Inner_SyncManager startEvent = init_MacroExecutor_Sync_CompletableFuture(
               startAnchor, 250, startCondition.getDisplayName(), "client action"
            );
            load_V_Str("Report: waiting end " + endCondition.getDisplayName());
            CompletableFuture<MacroExecutor.Inner_SyncManager> endFuture = init_CompletableFuture_Minecraft_MacroAction(mc, endCondition);
            MacroExecutor.Inner_SyncManager endEvent = init_MacroExecutor_Sync_CompletableFuture_J(endFuture, timeoutMs);
            if (endEvent == null) {
               endFuture.cancel(true);
               AutismClientMessaging.sendPrefixed("§eReport \"" + label + "\": timeout waiting for end condition");
               return conditionCount;
            }

            long deltaNanos = Math.max(0L, endEvent.init_J() - startEvent.init_J());
            long deltaMs = deltaNanos / 1000000L;
            String start = startEvent.init_Str_2();
            String end = endEvent.init_Str_2();
            AutismClientMessaging.sendPrefixed("§b" + label + ": " + init_Str_J(deltaNanos) + " (" + start + " -> " + end + ")");
            if (report.stashToSharedState) {
               AutismSessionState.init_ck().init_V_ck_ConfigStore(new AutismSessionState.Inner_ConfigStore(label, deltaMs, start, end, System.currentTimeMillis()));
            }

            return conditionCount;
         } else if (RaceAction.init_Z(startCondition) && RaceAction.init_Z(endCondition)) {
            int timeoutMs = Math.max(100, report.timeoutMs);
            long startWaitNanos = System.nanoTime();
            load_V_Str("Report: waiting start " + startCondition.getDisplayName());
            boolean startFired = init_Z_Minecraft_4(mc, startCondition, timeoutMs);
            if (!startFired) {
               AutismClientMessaging.sendPrefixed("§eReport \"" + report.reportLabel + "\": timeout waiting for start condition");
               return conditionCount;
            }

            MacroExecutor.Inner_SyncManager startEvent = new MacroExecutor.Inner_SyncManager(
               null, "condition " + startCondition.getDisplayName(), System.nanoTime(), null
            );
            long spentMs = (startEvent.init_J() - startWaitNanos) / 1000000L;
            int remainingMs = (int)Math.max(1L, timeoutMs - spentMs);
            load_V_Str("Report: waiting end " + endCondition.getDisplayName());
            CompletableFuture<MacroExecutor.Inner_SyncManager> endFuture = init_CompletableFuture_Minecraft_MacroAction(mc, endCondition);
            MacroExecutor.Inner_SyncManager endEvent = init_MacroExecutor_Sync_CompletableFuture_J(endFuture, remainingMs);
            if (endEvent == null) {
               endFuture.cancel(true);
               AutismClientMessaging.sendPrefixed("§eReport \"" + report.reportLabel + "\": timeout waiting for end condition");
               return conditionCount;
            }

            long deltaNanos = Math.max(0L, endEvent.init_J() - startEvent.init_J());
            long deltaMs = deltaNanos / 1000000L;
            String label = report.reportLabel != null && !report.reportLabel.isBlank() ? report.reportLabel.trim() : "Report";
            String start = startEvent.init_Str_2();
            String end = endEvent.init_Str_2();
            AutismClientMessaging.sendPrefixed("§b" + label + ": " + init_Str_J(deltaNanos) + " (" + start + " -> " + end + ")");
            if (report.stashToSharedState) {
               AutismSessionState.init_ck().init_V_ck_ConfigStore(new AutismSessionState.Inner_ConfigStore(label, deltaMs, start, end, System.currentTimeMillis()));
            }

            return conditionCount;
         } else {
            report.execute(mc);
            return conditionCount;
         }
      } else {
         load_V_Str(report.getDisplayName());
         report.execute(mc);
         return conditionCount;
      }
   }

   private static boolean init_Z_Minecraft_L(Minecraft mc, RaceAction race) {
      MacroAction trigger = race.triggerAction == null ? new DelayAction(0) : race.triggerAction;
      int timeoutMs = Math.max(0, race.timeoutMs);
      return init_Z_Minecraft_4(mc, trigger, timeoutMs);
   }

   public static boolean init_Z_Minecraft_4(Minecraft mc, MacroAction trigger, int timeoutMs) {
      if (trigger == null) {
         return true;
      }

      CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, trigger);
      if (future == null) {
         init_V_Minecraft_2(mc, trigger, null);
         return true;
      }

      boolean fired = load_Z_CompletableFuture_J(future, timeoutMs);
      if (!fired) {
         future.cancel(true);
      }

      return fired;
   }

   private static void init_V_Minecraft(
      Minecraft mc,
      autismclient.modules.ModuleRuntimeManager module,
      List<MacroAction> bodyActions,
      MacroExecutor.Inner_TableIndex run,
      CompletableFuture<Void> bodyFuture
   ) {
      if (bodyActions != null && !bodyActions.isEmpty()) {
         Runnable task = () -> {
            MacroExecutor.Inner_TableIndex previous = init_ThreadLocal.get();
            if (run != null) {
               init_ThreadLocal.set(run);
            }

            EnumSet<MacroResourceClassifier.Inner_DataUtil> heldResources = EnumSet.noneOf(MacroResourceClassifier.Inner_DataUtil.class);

            try {
               if (bodyActions.stream().noneMatch(ToolsAction.class::isInstance)) {
                  heldResources = init_EnumSet_EnumSet(init_EnumSet_List(bodyActions));
               }

               Throwable firstFailure = null;

               for (MacroAction body : bodyActions) {
                  if (body != null && body.isEnabled() && RaceAction.load(body)) {
                     if (run != null && !run.init_AtomicBoolean.get()) {
                        bodyFuture.cancel(true);
                        return;
                     }

                     try {
                        init_V_Minecraft_MacroAction(mc, body);
                     } catch (Throwable t) {
                        if (firstFailure == null) {
                           firstFailure = t;
                        }

                        AutismClientMessaging.sendPrefixed("§cRace action failed (" + body.getDisplayName() + "): " + init_Str_Throwable(t));
                     }
                  }
               }

               if (firstFailure == null) {
                  bodyFuture.complete(null);
               } else {
                  bodyFuture.completeExceptionally(firstFailure);
               }
            } catch (Throwable t) {
               AutismClientMessaging.sendPrefixed("§cRace action failed: " + init_Str_Throwable(t));
               bodyFuture.completeExceptionally(t);
            } finally {
               init_V_EnumSet(heldResources);
               if (previous != null) {
                  init_ThreadLocal.set(previous);
               } else {
                  init_ThreadLocal.remove();
               }
            }
         };
         if (bodyActions.stream().allMatch(MacroExecutor::read_Z)) {
            task.run();
         } else {
            mc.execute(task);
         }
      } else {
         bodyFuture.complete(null);
      }
   }

   private static void init_V_Minecraft_MacroAction(Minecraft mc, MacroAction body) {
      if (body != null && body.isEnabled() && RaceAction.load(body) && DynamicBindingManager.init(body, mc)) {
         body.execute(mc);
      }
   }

   private static boolean read_Z(MacroAction action) {
      if (action instanceof SendChatAction chat) {
         return !chat.read_Z() && !chat.write_Z();
      } else {
         return action instanceof SendPacketAction sendPacket ? !sendPacket.read_Z() && !sendPacket.write_Z() : action instanceof PacketClickAction;
      }
   }

   private static CompletableFuture<MacroExecutor.Inner_SyncManager> init_CompletableFuture_MacroAction(MacroAction action) {
      if (action instanceof PacketClickAction) {
         return init_MacroExecutor_Fiel_Predicate(packet -> packet instanceof ServerboundContainerClickPacket).init_CompletableFuture;
      } else if (action instanceof ItemAction) {
         return init_MacroExecutor_Fiel_Predicate(packet -> packet instanceof ServerboundContainerClickPacket).init_CompletableFuture;
      } else if (action instanceof CloseGuiAction) {
         return init_MacroExecutor_Fiel_Predicate(packet -> packet instanceof ServerboundContainerClosePacket).init_CompletableFuture;
      } else if (action instanceof SendChatAction) {
         return init_MacroExecutor_Fiel_Predicate(
               packet -> packet instanceof ServerboundChatPacket
                  || packet instanceof ServerboundChatCommandPacket
                  || packet instanceof ServerboundChatCommandSignedPacket
            )
            .init_CompletableFuture;
      } else {
         return !(action instanceof SendPacketAction) && !(action instanceof PayloadAction) ? null : init_MacroExecutor_Fiel_Predicate(packet -> true).init_CompletableFuture;
      }
   }

   private static MacroExecutor.Inner_SyncManager init_MacroExecutor_Sync_CompletableFuture(
      CompletableFuture<MacroExecutor.Inner_SyncManager> anchor, int timeoutMs, String label, String fallbackDirection
   ) {
      if (anchor != null) {
         MacroExecutor.Inner_SyncManager event = init_MacroExecutor_Sync_CompletableFuture_J(anchor, timeoutMs);
         if (event != null) {
            return event;
         }

         anchor.cancel(true);
      }

      return new MacroExecutor.Inner_SyncManager(null, fallbackDirection + " " + (label == null ? "" : label), System.nanoTime(), null);
   }

   private static MacroExecutor.Inner_SyncManager init_MacroExecutor_Sync_CompletableFuture_J(
      CompletableFuture<MacroExecutor.Inner_SyncManager> future, long timeoutMs
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

   private static boolean init_Z_CompletableFuture_J(CompletableFuture<?> future, long timeoutMs) {
      if (future == null) {
         return true;
      }

      try {
         if (timeoutMs <= 0L) {
            await(future);
         } else {
            await(future, timeoutMs);
         }

         return !future.isCancelled() && close_Z();
      } catch (CancellationException e) {
         return false;
      }
   }

   private static String init_Str_J(long nanos) {
      return String.format(Locale.ROOT, "%.3f ms", nanos / 1000000.0);
   }

   private static String init_Str_Throwable(Throwable throwable) {
      if (throwable == null) {
         return "unknown error";
      }

      String message = throwable.getMessage();
      return message != null && !message.isBlank() ? message : throwable.getClass().getSimpleName();
   }

   private static boolean init_Z_Packet(Packet<?> packet) {
      return packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket;
   }

   private static CompletableFuture<MacroExecutor.Inner_SyncManager> init_CompletableFuture_Minecraft_MacroAction(Minecraft mc, MacroAction trigger) {
      if (trigger == null) {
         return CompletableFuture.completedFuture(new MacroExecutor.Inner_SyncManager(null, "condition", System.nanoTime(), null));
      } else if (trigger instanceof WaitForChatAction chat) {
         MacroVariableStore context = init_m();
         AtomicReference<Map<String, MacroVarValue>> captures = new AtomicReference<>(Map.of());
         MacroExecutor.Inner_FieldCache listener = init_MacroExecutor_Fiel_MacroExecutor_Meth((packet, direction, now) -> {
            MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
            Optional<Map<String, MacroVarValue>> matched = init_Optional_au(chat, capture, context);
            matched.ifPresent(captures::set);
            return matched.isPresent();
         });
         CompletableFuture<MacroExecutor.Inner_SyncManager> out = new CompletableFuture<>();
         listener.init_CompletableFuture
            .whenComplete(
               (event, e) -> {
                  if (e != null) {
                     out.completeExceptionally(e);
                  } else {
                     out.complete(
                        new MacroExecutor.Inner_SyncManager(
                           event.init_Packet(), event.load(), event.init_J(), init_MacroExecutor_Conf_Packet(event.init_Packet()), captures.get()
                        )
                     );
                  }
               }
            );
         out.whenComplete((v, e) -> listener.init());
         return out;
      } else if (trigger instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.init();
         CompletableFuture<MacroExecutor.Inner_SyncManager> matched = new CompletableFuture<>();
         MacroExecutor.Inner_FieldCache recv = init_MacroExecutor_Fiel_MacroExecutor_Meth((packet, direction, now) -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (init_Z_Str_2(target, packet, "S2C")) {
                  return true;
               }
            }

            return false;
         });
         MacroExecutor.Inner_FieldCache send = load_MacroExecutor_Fiel_MacroExecutor_Meth((packet, direction, now) -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (init_Z_Str_2(target, packet, "C2S")) {
                  return true;
               }
            }

            return false;
         });
         recv.init_CompletableFuture.thenAccept(matched::complete);
         send.init_CompletableFuture.thenAccept(matched::complete);
         matched.whenComplete((v, e) -> {
            recv.init();
            send.init();
         });
         return matched;
      } else if (trigger instanceof WaitForWorldChangeAction world) {
         CompletableFuture<MacroExecutor.Inner_SyncManager> done = new CompletableFuture<>();
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.init(world);
         MacroExecutor.Inner_FieldCache worldListener = init_MacroExecutor_Fiel_MacroExecutor_Meth((packet, direction, now) -> init_Z_Packet(packet));
         tickFuture.whenComplete((v, e) -> {
            if (e != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else {
               done.complete(new MacroExecutor.Inner_SyncManager(null, "world change", System.nanoTime(), null));
            }
         });
         worldListener.init_CompletableFuture.whenComplete((event, error) -> {
            if (error != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else if (world.targetDimension == null || world.targetDimension.isBlank()) {
               done.complete(event);
            }
         });
         done.whenComplete((v, e) -> {
            worldListener.init();
            if (!tickFuture.isDone()) {
               tickFuture.cancel(true);
            }
         });
         return done;
      } else if (trigger instanceof WaitForTeleportAction teleport) {
         Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
         CompletableFuture<MacroExecutor.Inner_SyncManager> done = new CompletableFuture<>();
         MacroExecutor.Inner_FieldCache teleListener = init_MacroExecutor_Fiel_MacroExecutor_Meth(
            (packet, direction, now) -> packet instanceof ClientboundPlayerPositionPacket || init_Z_Packet(packet)
         );
         teleListener.init_CompletableFuture.whenComplete((event, error) -> {
            teleListener.init();
            if (error != null) {
               done.cancel(true);
            } else if (teleport.minDistance <= 0.0) {
               done.complete(event);
            } else {
               MacroWaitRegistry.init_CompletableFuture_Vec3(origin, teleport.minDistance, teleport.horizontalOnly).whenComplete((v, e) -> {
                  if (e != null) {
                     done.cancel(true);
                  } else {
                     done.complete(new MacroExecutor.Inner_SyncManager(event.init_Packet(), event.load(), System.nanoTime(), event.init_MacroExecutor_Conf()));
                  }
               });
            }
         });
         done.whenComplete((v, e) -> teleListener.init());
         return done;
      } else {
         CompletableFuture<?> base = load_CompletableFuture_Minecraft_MacroAction(mc, trigger);
         if (base == null) {
            CompletableFuture<MacroExecutor.Inner_SyncManager> done = new CompletableFuture<>();
            MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
            mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
               try {
                  init_V_Minecraft_2(mc, trigger, null);
                  done.complete(new MacroExecutor.Inner_SyncManager(null, trigger.getDisplayName(), System.nanoTime(), null));
               } catch (Throwable t) {
                  done.completeExceptionally(t);
               }
            }));
            return done;
         } else {
            CompletableFuture<MacroExecutor.Inner_SyncManager> timed = new CompletableFuture<>();
            base.whenComplete((v, e) -> {
               if (e != null) {
                  timed.cancel(true);
               } else {
                  timed.complete(new MacroExecutor.Inner_SyncManager(null, trigger.getDisplayName(), System.nanoTime(), null));
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

   private static CompletableFuture<?> load_CompletableFuture_Minecraft_MacroAction(Minecraft mc, MacroAction trigger) {
      if (trigger instanceof WaitMovementAction wm) {
         return load_CompletableFuture_Minecraft_MacroAction(mc, wm.init_MacroAction_3());
      } else if (trigger instanceof WaitGamemodeChangeAction gm) {
         return MacroWaitRegistry.init(gm);
      } else if (trigger instanceof DelayAction delay) {
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         return CompletableFuture.runAsync(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
            if (delay.useTicks) {
               for (int i = 0; i < delay.delayTicks && close_Z(); i++) {
                  init_V_CompletableFuture(MacroWaitRegistry.load_CompletableFuture());
               }
            } else {
               sleep(Math.max(0, delay.delayMs));
            }
         }));
      } else if (trigger instanceof WaitForGuiAction gui) {
         CompletableFuture<MacroExecutor.Inner_SyncManager> done = new CompletableFuture<>();
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         Thread worker = new Thread(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
            long start = System.currentTimeMillis();
            boolean saw = false;
            Map<String, MacroVarValue> observed = Map.of();

            while (!done.isDone() && close_Z()) {
               Optional<Map<String, MacroVarValue>> captured = gui.init(Minecraft.getInstance().gui.screen());
               if (captured.isPresent()) {
                  saw = true;
                  observed = captured.get();
                  if (gui.waitMode == WaitForGuiAction.Inner_DataUtil.OPEN) {
                     done.complete(new MacroExecutor.Inner_SyncManager(null, "GUI", System.nanoTime(), null, observed));
                     return;
                  }
               } else if (saw && gui.waitMode == WaitForGuiAction.Inner_DataUtil.CLOSE) {
                  done.complete(new MacroExecutor.Inner_SyncManager(null, "GUI close", System.nanoTime(), null, observed));
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
         MacroVariableStore context = init_m();
         MacroExecutor.Inner_EventBus listener = init_MacroExecutor_Even_Predicate(packet -> {
            MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
            return capture != null && init_Optional_au(chat, capture, context).isPresent();
         });
         listener.init_CompletableFuture.whenComplete((v, e) -> listener.init_V());
         return listener.init_CompletableFuture;
      } else if (trigger instanceof WaitForPacketAction packetAction) {
         List<String> targets = packetAction.init();
         CompletableFuture<Packet<?>> matched = new CompletableFuture<>();
         MacroExecutor.Inner_EventBus recv = init_MacroExecutor_Even_Predicate(packet -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (init_Z_Str_2(target, packet, "S2C")) {
                  return true;
               }
            }

            return false;
         });
         MacroExecutor.Inner_EventBus send = load_MacroExecutor_Even_Predicate(packet -> {
            if (targets.isEmpty()) {
               return true;
            }

            for (String target : targets) {
               if (init_Z_Str_2(target, packet, "C2S")) {
                  return true;
               }
            }

            return false;
         });
         recv.init_CompletableFuture.thenAccept(matched::complete);
         send.init_CompletableFuture.thenAccept(matched::complete);
         matched.whenComplete((v, e) -> {
            recv.init_V();
            send.init_V();
         });
         return matched;
      } else if (trigger instanceof WaitForHealthAction health) {
         return MacroWaitRegistry.init_CompletableFuture_F_Z(health.healthThreshold, health.below);
      } else if (trigger instanceof WaitForBlockAction block) {
         return MacroWaitRegistry.init(block);
      } else if (trigger instanceof WaitForCooldownAction cooldown) {
         MacroItemTarget target = init_ArrayUtil_ArrayUtil_Str(cooldown.itemTarget, cooldown.itemName);
         return MacroWaitRegistry.init_CompletableFuture_ArrayUtil_Z(target, cooldown.checkMainInteractionHand);
      } else if (trigger instanceof WaitPosAction pos) {
         return MacroWaitRegistry.init(pos.f_x_D, pos.f_y_D, pos.f_z_D, pos.leeway, pos.checkRotation, pos.yaw, pos.pitch, pos.rotLeeway);
      } else if (trigger instanceof WaitForEntityAction entity) {
         return init_CompletableFuture_BooleanSupplier_I(() -> entity.init_Z_5(Minecraft.getInstance()), entity.timeoutMs);
      } else if (trigger instanceof WaitForSoundAction sound) {
         return MacroWaitRegistry.init(sound);
      } else if (trigger instanceof WaitForSlotChangeAction slot) {
         return MacroWaitRegistry.init(slot);
      } else if (trigger instanceof WaitDurabilityAction durability) {
         return init_CompletableFuture_BooleanSupplier_I(() -> init_Z_Minecraft_ar(Minecraft.getInstance(), durability), durability.timeoutMs);
      } else if (trigger instanceof WaitFreeSlotsAction freeSlots) {
         return init_CompletableFuture_BooleanSupplier_I(() -> freeSlots.init(Minecraft.getInstance()), freeSlots.timeoutMs);
      } else if (trigger instanceof TickSyncAction) {
         return MacroWaitRegistry.load_CompletableFuture();
      } else if (trigger instanceof ServerTickSyncAction serverTick) {
         return MacroWaitRegistry.init_CompletableFuture_I(serverTick.bufferMs, serverTick.maxWaitMs, serverTick.ignorePing);
      } else if (trigger instanceof WaitForWorldChangeAction world) {
         CompletableFuture<Void> done = new CompletableFuture<>();
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.init(world);
         MacroExecutor.Inner_EventBus worldListener = init_MacroExecutor_Even_Predicate(MacroExecutor::init_Z_Packet);
         tickFuture.whenComplete((v, e) -> {
            if (e != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else {
               done.complete(null);
            }
         });
         worldListener.init_CompletableFuture.whenComplete((packet, error) -> {
            if (error != null) {
               if (!done.isDone()) {
                  done.cancel(true);
               }
            } else if (world.targetDimension == null || world.targetDimension.isBlank()) {
               done.complete(null);
            }
         });
         done.whenComplete((v, e) -> {
            worldListener.init_V();
            if (!tickFuture.isDone()) {
               tickFuture.cancel(true);
            }
         });
         return done;
      } else if (trigger instanceof WaitForPositionDeltaAction delta) {
         return MacroWaitRegistry.init_CompletableFuture_D_Z(delta.distance, delta.horizontalOnly);
      } else if (trigger instanceof WaitForTeleportAction teleport) {
         Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
         CompletableFuture<Void> done = new CompletableFuture<>();
         MacroExecutor.Inner_EventBus teleListener = init_MacroExecutor_Even_Predicate(
            packet -> packet instanceof ClientboundPlayerPositionPacket || init_Z_Packet(packet)
         );
         teleListener.init_CompletableFuture.whenComplete((packet, error) -> {
            teleListener.init_V();
            if (error != null) {
               done.cancel(true);
            } else if (teleport.minDistance <= 0.0) {
               done.complete(null);
            } else {
               MacroWaitRegistry.init_CompletableFuture_Vec3(origin, teleport.minDistance, teleport.horizontalOnly).whenComplete((v, e) -> {
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
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         return CompletableFuture.runAsync(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> init_V_Minecraft_2(mc, trigger, null)));
      } else {
         return null;
      }
   }

   private static boolean load_Z_CompletableFuture_J(CompletableFuture<?> future, long timeoutMs) {
      Thread t = Thread.currentThread();
      long start = System.nanoTime();
      long timeoutNanos = timeoutMs <= 0L ? 9223372036854775807L : timeoutMs * 1000000L;
      future.whenComplete((v, e) -> LockSupport.unpark(t));

      while (!future.isDone() && close_Z() && System.nanoTime() - start < timeoutNanos) {
         LockSupport.parkNanos(500000L);
         if (Thread.interrupted()) {
            break;
         }
      }

      return future.isDone() && !future.isCancelled() && close_Z();
   }

   private static void init_V_Minecraft_al(Minecraft mc, ToolsAction action) {
      if (!MacroToolBridge.init_Z()) {
         throw new IllegalStateException("Tools requires the full client");
      }

      long owner = load_J();
      load_V_Str("Tools: " + ("RUN".equals(action.operation) ? MacroToolBridge.init_Str(action.toolId) : action.operation.toLowerCase()));
      CompletableFuture<MacroToolBridge.Inner_StreamUtil> start = new CompletableFuture<>();
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(run, () -> {
         try {
            if (run != null && !run.init_AtomicBoolean.get()) {
               start.cancel(false);
               return;
            }

            MacroToolBridge.Inner_StreamUtil jobx = "RUN".equals(action.operation) ? action.init_k_StreamUtil_Minecraft_J(mc, owner) : MacroToolBridge.load(owner, action.scope, "");
            if (run != null && !run.init_AtomicBoolean.get() && "RUN".equals(action.operation) && jobx != null) {
               jobx.init_V();
            }

            start.complete(jobx);
         } catch (Throwable error) {
            start.completeExceptionally(error);
         }
      }));
      init_V_CompletableFuture(start.thenApply(ignored -> null));
      if (!close_Z()) {
         throw new RuntimeException("Macro stopped");
      }

      MacroToolBridge.Inner_StreamUtil job = start.join();
      if (job == null) {
         throw new IllegalStateException("No matching tool has started");
      }

      CompletableFuture<MacroToolBridge.Inner_CachePool> result;
      if ("RUN".equals(action.operation)) {
         init_V_CompletableFuture(job.init_CompletableFuture().thenApply(ignored -> null));
         if (!close_Z()) {
            throw new RuntimeException("Macro stopped");
         }

         result = job.load();
         if (!"WAIT".equals(action.completion) && !result.isDone()) {
            result.whenComplete((outcome, failure) -> {
               if (run != null && run.init_AtomicBoolean.get()) {
                  init_V_MacroExecutor_Tabl_Runnable(run, () -> {
                     if (failure == null && (!job.init_Z() || outcome.init_k_ConfigStore() != MacroToolBridge.Inner_ConfigStore.CANCELLED)) {
                        init_V_al(action, outcome, run);
                     }
                  });
               }
            });
            return;
         }
      } else {
         result = MacroToolBridge.init(job, action.operation);
      }

      init_V_CompletableFuture(result.thenApply(ignored -> null));
      if (!close_Z()) {
         throw new RuntimeException("Macro stopped");
      }

      init_V_al(action, result.join(), run);
   }

   private static void init_V_al(ToolsAction action, MacroToolBridge.Inner_CachePool outcome, MacroExecutor.Inner_TableIndex run) {
      if (action.saveAs != null && !action.saveAs.isBlank()) {
         MacroVariableContext.init_V_Str_Obj(action.saveAs, outcome.load());
      }

      if (action.stopOnFailure
         && outcome.init_k_ConfigStore().init()
         && !outcome.init_Z()
         && (!"STOP".equals(action.operation) || outcome.init_k_ConfigStore() != MacroToolBridge.Inner_ConfigStore.CANCELLED)) {
         if (run != null) {
            run.init_Str = "FAILED";
         }

         load_V_Str("Tools: " + outcome.init_Str());
         AutismClientMessaging.sendPrefixed("§cTools: " + outcome.init_Str());
         flush_V();
      }
   }

   private static void init_V_Minecraft_2(Minecraft mc, MacroAction action, autismclient.modules.ModuleRuntimeManager module) {
      if (action instanceof MultiAction || action instanceof SkippedLegacyMultiStep) {
         return;
      }
      if (DynamicBindingManager.init(action, mc) && init_Z_MacroAction_Minecraft(action, mc)) {
         load_V_MacroAction(action);
         EnumSet<MacroResourceClassifier.Inner_DataUtil> heldResources = load_EnumSet_MacroAction(action);

         try {
            if (read_Z(mc, action)) {
               load_V_Minecraft_MacroAction(mc, action);
               CompletableFuture<Void> postGuiFuture = !(action instanceof ItemAction) && !(action instanceof DropAction) && !write(action)
                  ? write(mc, action)
                  : null;
               if (action instanceof ToolsAction tools) {
                  init_V_Minecraft_al(mc, tools);
               } else if (action instanceof DelayAction da) {
                  if (da.useTicks) {
                     for (int t = 0; t < da.delayTicks && close_Z(); t++) {
                        CompletableFuture<Void> tf = MacroWaitRegistry.load_CompletableFuture();
                        init_V_CompletableFuture(tf);
                     }
                  } else {
                     sleep(da.delayMs);
                  }
               } else if (action instanceof CustomMenuAction customMenu) {
                  load_V_Str(customMenu.getDisplayName());
                  customMenu.execute(mc);
               } else if (action instanceof WaitForPacketAction packetAction) {
                  packetAction.matchedPacket = null;
                  packetAction.matchedDirection = "";
                  MacroExecutor.Inner_TableIndex packetRun = init_ThreadLocal.get();
                  if (packetRun != null) {
                     packetRun.init_Packet = null;
                     packetRun.load_Str = "";
                  }

                  List<String> targets = packetAction.init();
                  if (targets.isEmpty()) {
                     read_V("");
                  } else {
                     for (String target : targets) {
                        if (!close_Z()) {
                           break;
                        }

                        read_V(target);
                     }
                  }

                  init_V_aB(packetAction);
               } else if (action instanceof WaitPacketMatchAction wpma) {
                  load_V_Str(wpma.getDisplayName());
                  init_V_aM_2(wpma);
               } else if (action instanceof WaitInventoryPredicateAction wipa) {
                  load_V_Str(wipa.getDisplayName());
                  init_V_BooleanSupplier_I(() -> wipa.init(mc), wipa.timeoutMs);
               } else if (action instanceof WaitDurabilityAction wda) {
                  load_V_Str(wda.getDisplayName());
                  if (wda.useNext) {
                     init_V_Minecraft_ar(mc, wda);
                  } else {
                     init_V_BooleanSupplier_I(() -> wda.init(mc), wda.timeoutMs);
                  }
               } else if (action instanceof WaitFreeSlotsAction wfsa) {
                  load_V_Str(wfsa.getDisplayName());
                  init_V_BooleanSupplier_I(() -> wfsa.init(mc), wfsa.timeoutMs);
               } else if (action instanceof WaitEntityTargetAction weta) {
                  load_V_Str(weta.getDisplayName());
                  init_V_BooleanSupplier_I(() -> weta.init(mc), weta.timeoutMs);
               } else if (action instanceof WaitGuiTypeAction wgta) {
                  load_V_Str(wgta.getDisplayName());
                  init_V_MacroAction(wgta);
                  if (wgta.waitMode == WaitGuiTypeAction.Inner_DataUtil.CLOSE) {
                     init_V_BooleanSupplier_I(init_BooleanSupplier_aJ_Minecraft(wgta, mc), wgta.timeoutMs);
                  } else {
                     init_V_BooleanSupplier_I(() -> {
                        Optional<Map<String, MacroVarValue>> captured = wgta.init_Optional(mc);
                        captured.ifPresent(values -> init_V_MacroAction_Map(wgta, (Map<String, MacroVarValue>)values));
                        return captured.isPresent();
                     }, wgta.timeoutMs);
                  }

                  read_V(wgta);
               } else if (action instanceof PacketGateAction pga) {
                  init_V_Minecraft_B(mc, pga);
               } else if (write(action)) {
                  postGuiFuture = read_CompletableFuture(mc, action);
               } else if (action instanceof WaitForHealthAction wh) {
                  load_V_Str(wh.write());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_F_Z(wh.healthThreshold, wh.below);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var113) {
                  }
               } else if (action instanceof WaitForBlockAction wba) {
                  load_V_Str(wba.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(wba);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var112) {
                  }
               } else if (action instanceof WaitForGuiAction wga) {
                  init_V_MacroAction(wga);
                  if (wga.waitMode == WaitForGuiAction.Inner_DataUtil.CLOSE) {
                     load_V_Str("Waiting for GUI close: " + wga.guiTitle);
                     init_V_BooleanSupplier_I(init_BooleanSupplier_ax_Minecraft(wga, mc), wga.timeoutMs);
                  } else {
                     load_V_Str("Waiting for GUI: " + wga.guiTitle);
                     init_V_BooleanSupplier_I(() -> {
                        Optional<Map<String, MacroVarValue>> captured = wga.init(mc.gui.screen());
                        captured.ifPresent(values -> init_V_MacroAction_Map(wga, (Map<String, MacroVarValue>)values));
                        return captured.isPresent();
                     }, wga.timeoutMs);
                  }

                  read_V(wga);
               } else if (action instanceof WaitForCooldownAction wca) {
                  MacroItemTarget cooldownTarget = init_ArrayUtil_ArrayUtil_Str(wca.itemTarget, wca.itemName).init_ArrayUtil_Minecraft(mc);
                  if (cooldownTarget == null) {
                     return;
                  }

                  String cooldownLabel = init_Str_ArrayUtil(cooldownTarget);
                  load_V_Str("Waiting for cooldown: " + (!cooldownLabel.isEmpty() ? cooldownLabel : (wca.checkMainInteractionHand ? "Main Hand" : "Off Hand")));

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_ArrayUtil_Z(cooldownTarget, wca.checkMainInteractionHand);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var111) {
                  }
               } else if (action instanceof WaitPosAction wpa) {
                  load_V_Str("Waiting for Pos: " + String.format("%.0f, %.0f, %.0f", wpa.f_x_D, wpa.f_y_D, wpa.f_z_D));

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(wpa.f_x_D, wpa.f_y_D, wpa.f_z_D, wpa.leeway, wpa.checkRotation, wpa.yaw, wpa.pitch, wpa.rotLeeway);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var110) {
                  }
               } else if (action instanceof CaptureValueAction captureValue) {
                  read(mc, captureValue);
               } else if (action instanceof WaitForChatAction wca) {
                  load_V_Str("Waiting for chat: " + wca.pattern);
                  init_V_au(wca);
               } else if (action instanceof WaitForEntityAction wea) {
                  load_V_Str(wea.getDisplayName());

                  try {
                     init_V_BooleanSupplier_I(() -> wea.init_Z_5(mc), wea.timeoutMs);
                  } catch (CancellationException var109) {
                  }
               } else if (action instanceof WaitForSoundAction wsa) {
                  String sndDesc = wsa.soundIds.isEmpty() ? "any" : wsa.soundIds.get(0);
                  load_V_Str("Waiting for sound: " + sndDesc);

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(wsa);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var108) {
                  }
               } else if (action instanceof WaitForSlotChangeAction wsca) {
                  load_V_Str(wsca.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(wsca);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var107) {
                  }
               } else if (action instanceof WaitForWorldChangeAction world) {
                  load_V_Str(world.getDisplayName());

                  try {
                     CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, world);
                     load_Z_CompletableFuture_J(future, 0L);
                  } catch (CancellationException var106) {
                  }
               } else if (action instanceof WaitForPositionDeltaAction wpda) {
                  load_V_Str(wpda.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture_D_Z(wpda.distance, wpda.horizontalOnly);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var105) {
                  }
               } else if (action instanceof WaitForTeleportAction teleport) {
                  load_V_Str(teleport.getDisplayName());

                  try {
                     CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, teleport);
                     load_Z_CompletableFuture_J(future, 0L);
                  } catch (CancellationException var104) {
                  }
               } else if (action instanceof WaitGamemodeChangeAction gm) {
                  load_V_Str(gm.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(gm);
                     load_Z_CompletableFuture_J(future, gm.timeoutMs);
                  } catch (CancellationException var103) {
                  }
               } else if (action instanceof WaitMovementAction wm) {
                  load_V_Str(wm.getDisplayName());

                  try {
                     CompletableFuture<?> future = load_CompletableFuture_Minecraft_MacroAction(mc, wm.init_MacroAction_3());
                     load_Z_CompletableFuture_J(future, 0L);
                  } catch (CancellationException var102) {
                  }
               } else if (action instanceof WaitForLanStepAction wla) {
                  load_V_Str(wla.getDisplayName());

                  try {
                     CompletableFuture<Void> future = MacroWaitRegistry.init(wla);
                     init_V_CompletableFuture(future);
                  } catch (CancellationException var101) {
                  }
               } else if (action instanceof WaitForMacroStepAction wma) {
                  load_V_Str(wma.getDisplayName());
                  init_V_aA(wma);
               } else if (action instanceof GoToAction ga) {
                  init_V_Minecraft_CharEncoder(mc, ga);
               } else if (action instanceof MoveAction ma) {
                  if (ma.nonBlocking) {
                     mc.execute(() -> ma.execute(mc));
                     init_ConcurrentHashMap().put(ma, new int[]{ma.durationTicks - 1});
                  } else {
                     for (int t = 0; t < ma.durationTicks && close_Z(); t++) {
                        mc.execute(() -> ma.execute(mc));
                        CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                        init_V_CompletableFuture(tickFuture);
                     }

                     MacroExecutor.Inner_TableIndex movementRun = init_ThreadLocal.get();
                     mc.execute(() -> init_V_MacroExecutor_Tabl(movementRun, ma, mc));
                  }
               } else if (action instanceof LookAtBlockAction la) {
                  LookAtBlockAction.Inner_DataUtil rotationTarget = init_ValueSink_DataUtil_Minecraft_ValueSink(mc, la);
                  if (rotationTarget == null) {
                     return;
                  }

                  if (la.smooth) {
                     init_V_F(rotationTarget.init(), rotationTarget.load(), la.init_D_2());
                     if (la.waitForCompletion) {
                        set();
                     }
                  } else {
                     mc.execute(() -> {
                        if (mc.player != null) {
                           mc.player.setYRot(rotationTarget.init());
                           mc.player.setXRot(rotationTarget.load());
                        }
                     });
                  }
               } else if (action instanceof RotateAction ra) {
                  if (ra.smooth) {
                     init_V_F(ra.yaw, ra.pitch, ra.init());
                     if (ra.waitForCompletion) {
                        set();
                     }
                  } else {
                     mc.execute(() -> action.execute(mc));
                  }
               } else if (action instanceof MineAction mna) {
                  init_V_Minecraft_q(mc, mna);
               } else if (action instanceof InstaBreakAction) {
                  init_V_Minecraft_MethodUtil(mc, (InstaBreakAction)action);
               } else if (action instanceof BreakAction) {
                  init_V_Minecraft_CachePool(mc, (BreakAction)action);
               } else if (action instanceof PlaceAction) {
                  init_V_Minecraft_K(mc, (PlaceAction)action);
               } else if (action instanceof UseItemAction) {
                  init_V_Minecraft_an(mc, (UseItemAction)action);
               } else if (action instanceof JumpAction ja) {
                  mc.execute(() -> ja.execute(mc));
                  if (ja.tap) {
                     CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                     init_V_CompletableFuture(tickFuture);
                  } else {
                     for (int t = 0; t < ja.durationTicks && close_Z(); t++) {
                        CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                        init_V_CompletableFuture(tickFuture);
                     }
                  }

                  mc.execute(() -> ja.init(mc));
               } else if (action instanceof SneakAction sna) {
                  if (sna.persistent && sna.sneak) {
                     init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SneakAction);
                     init_CopyOnWriteArrayLi().add(sna);
                  } else if (!sna.sneak) {
                     init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SneakAction);
                  }

                  if (!sna.persistent && sna.sneak) {
                     load_V_Minecraft(mc, () -> sna.execute(mc), sna.h_());
                  } else {
                     load_V_Minecraft(mc, () -> load_V_Minecraft(mc), sna.h_());
                  }
               } else if (action instanceof SprintAction spa) {
                  if (spa.persistent && spa.sprint) {
                     init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SprintAction);
                     init_CopyOnWriteArrayLi().add(spa);
                  } else if (!spa.sprint) {
                     init_CopyOnWriteArrayLi().removeIf(p -> p instanceof SprintAction);
                  }

                  if (!spa.persistent && spa.sprint) {
                     load_V_Minecraft(mc, () -> spa.execute(mc), spa.h_());
                  } else {
                     load_V_Minecraft(mc, () -> read(mc), spa.h_());
                  }
               } else if (action instanceof NbtBookAction nba) {
                  int totalBooks = Math.max(1, nba.bookCount);
                  long delayMs = Math.max(1, nba.delayTicks) * 50L;
                  boolean signedAny = false;

                  for (int b = 0; b < totalBooks && close_Z(); b++) {
                     int bookIdx = b;
                     CompletableFuture<Boolean> result = new CompletableFuture<>();
                     mc.execute(() -> result.complete(nba.init_Z_Minecraft_2(mc, bookIdx, totalBooks)));

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
                     mc.execute(() -> nba.init_V_Minecraft_8(mc));
                  }
               } else if (action instanceof ItemAction ia) {
                  if (ia.waitForItem || !ia.itemNames.isEmpty()) {
                     try {
                        load_V_Minecraft_MapStore(mc, ia);
                     } catch (CancellationException var100) {
                     }
                  }

                  postGuiFuture = write(mc, ia);
                  init_V_Minecraft_3(mc, action, "ItemAction");
               } else if (!(action instanceof DropAction) && !(action instanceof SwapSlotsAction)) {
                  if (action instanceof SendPacketAction) {
                     if (module != null && module.add_Z()) {
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
                     init_V_Minecraft_NetHelper(mc, ca);
                  } else if (action instanceof PayAction) {
                     init_V_Minecraft_G(mc, (PayAction)action);
                  } else if (action instanceof XCarryAction xsa) {
                     if (xsa.mode == XCarryAction.Inner_StreamUtil.PUT_IN
                        && (xsa.transferMode == XCarryAction.Inner_ConfigStore.CLICK || xsa.transferMode == XCarryAction.Inner_ConfigStore.SAFE_CLICK)) {
                        if (xsa.transferMode == XCarryAction.Inner_ConfigStore.SAFE_CLICK) {
                           load_V_Str("XCarry Safe");
                           load_V_Minecraft_aQ(mc, xsa);
                        } else {
                           load_V_Str("XCarry Click");
                           init_V_Minecraft_aQ(mc, xsa);
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

                        load_V_Str(var10000);
                        init_V_Minecraft_Runnable(mc, () -> {
                           try {
                              xsa.execute(mc);
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("§cXCarry: " + e.getMessage());
                           }
                        });
                     }
                  } else if (action instanceof CraftAction) {
                     init_V_Minecraft_SyncManager(mc, (CraftAction)action);
                  } else if (action instanceof StoreItemAction sia) {
                     load_V_Str((sia.mode == StoreItemAction.Inner_DataUtil.LOOT ? "Looting" : "Storing") + (sia.persistent ? " ∞" : ""));
                     if (sia.persistent) {
                        while (close_Z()) {
                           try {
                              init_V_Minecraft_ah(mc, sia);
                           } catch (Exception e) {
                              AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                           }

                           CompletableFuture<Void> siTick = MacroWaitRegistry.load_CompletableFuture();
                           init_V_CompletableFuture(siTick);
                        }
                     } else {
                        try {
                           init_V_Minecraft_ah(mc, sia);
                        } catch (Exception e) {
                           AutismClientMessaging.sendPrefixed("Store error: " + e.getMessage());
                        }
                     }
                  } else if (action instanceof InventoryAuditAction auditAction
                     && (auditAction.mode == InventoryAuditAction.Inner_BufferHelper.DUPE || auditAction.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM)) {
                     load_V_Str("Dupe: " + auditAction.dupeVector.name() + " " + auditAction.openMode.name());

                     try {
                        auditAction.init_V_Minecraft_2(mc);
                     } catch (Throwable t) {
                        AutismClientMessaging.sendPrefixed("§cDupe error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
                     }
                  } else if (action instanceof ContainerClickSequenceAction sequence) {
                     init_V_Minecraft_StateCache(mc, sequence);
                  } else if (action instanceof PacketBurstAction burst) {
                     init_V_Minecraft_y(mc, burst);
                  } else if (action instanceof TickSyncAction tsa) {
                     if (mc.level == null) {
                        return;
                     }

                     List<AutismSessionState.Inner_CachePool> preGenerated = init_List_I_Minecraft(tsa.preGenCount, mc);
                     long targetTick = mc.level.getGameTime() + tsa.tickOffset;
                     load_V_Str("Tick Sync -> " + targetTick + " (" + preGenerated.size() + " pkts)");

                     while (close_Z() && mc.level.getGameTime() < targetTick) {
                        LockSupport.parkNanos(1000000L);
                     }

                     if (close_Z()) {
                        load_V_Minecraft_List(mc, preGenerated);
                        MacroExecutor.Inner_IoWorker context = read_ThreadLocal.get();
                        if (context != null) {
                           write_ThreadLocal.set(init_I_aM(context.init_aM(), context.init_I(), preGenerated.size(), context.load()));
                        }

                        AutismClientMessaging.sendPrefixed("Tick sync: " + preGenerated.size() + " pkts sent");
                     }
                  } else if (action instanceof RevisionSyncAction rsa) {
                     if (mc.player == null || mc.player.containerMenu == null) {
                        return;
                     }

                     List<AutismSessionState.Inner_CachePool> preGenerated = init_List_I_Minecraft(rsa.preGenCount, mc);
                     int baseRevision = mc.player.containerMenu.getStateId();
                     int targetRevision = baseRevision + rsa.revisionOffset;
                     load_V_Str("Rev Sync -> " + targetRevision + " (" + preGenerated.size() + " pkts)");

                     while (close_Z() && mc.player != null && mc.player.containerMenu != null && mc.player.containerMenu.getStateId() < targetRevision) {
                        LockSupport.parkNanos(500000L);
                     }

                     if (close_Z() && mc.player != null && mc.player.containerMenu != null) {
                        load_V_Minecraft_List(mc, preGenerated);
                        MacroExecutor.Inner_IoWorker context = read_ThreadLocal.get();
                        if (context != null) {
                           write_ThreadLocal.set(init_I_aM(context.init_aM(), context.init_I(), preGenerated.size(), context.load()));
                        }

                        AutismClientMessaging.sendPrefixed("Rev sync: " + preGenerated.size() + " pkts sent");
                     }
                  } else if (action instanceof ServerTickSyncAction stsa) {
                     if (mc.getConnection() == null) {
                        return;
                     }

                     List<AutismSessionState.Inner_CachePool> preGenerated = init_List_I_Minecraft(stsa.preGenCount, mc);

                     for (int sampleWaitMs = 0; close_Z() && !ServerTickPhaseTracker.init_Z() && sampleWaitMs < stsa.maxWaitMs; sampleWaitMs += 50) {
                        float progress = ServerTickPhaseTracker.init_F();
                        load_V_Str(String.format("ServerSync warmup %.0f%% (%d samples, %dms)", progress * 100.0F, ServerTickPhaseTracker.load_I(), ServerTickPhaseTracker.write()));
                        sleep(50L);
                     }

                     if (!close_Z()) {
                        return;
                     }

                     long optimalTime = ServerTickPhaseTracker.init(stsa.bufferMs, stsa.ignorePing);
                     long msUntil = (optimalTime - System.nanoTime()) / 1000000L;
                     int ping = ServerTickPhaseTracker.init_I();
                     String pingStr = stsa.ignorePing ? " (no ping)" : " ping:" + ping + "ms";
                     load_V_Str("ServerSync in " + Math.max(0L, msUntil) + "ms" + pingStr + " (" + preGenerated.size() + " pkts)");
                     long startWait = System.nanoTime();
                     long maxWaitNanos = stsa.maxWaitMs * 1000000L;

                     while (close_Z()) {
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

                     if (close_Z()) {
                        load_V_Minecraft_List(mc, preGenerated);
                        MacroExecutor.Inner_IoWorker context = read_ThreadLocal.get();
                        if (context != null) {
                           write_ThreadLocal.set(init_I_aM(context.init_aM(), context.init_I(), preGenerated.size(), context.load()));
                        }

                        long actualDelay = (System.nanoTime() - optimalTime) / 1000000L;
                        AutismClientMessaging.sendPrefixed("ServerSync: " + preGenerated.size() + " pkts (+" + actualDelay + "ms)");
                     }
                  } else if (action instanceof BundleDupeV2Action bundleV2) {
                     load_V_Str("Bundle Dupe V2");
                     bundleV2.execute(mc);
                  } else if (action instanceof RollbackAction rollback) {
                     load_V_Str("Rollback");

                     try {
                        rollback.init_V_Minecraft_4(mc);
                     } catch (Exception e) {
                        AutismClientMessaging.sendPrefixed("§cRollback failed: " + init_Str_Throwable(e));
                     }
                  } else if (action instanceof StopMacroAction stopAction) {
                     if (stopAction.target == StopMacroAction.Inner_DataUtil.SELF) {
                        flush_V();
                     } else {
                        stopAction.execute(mc);
                     }
                  } else if (action instanceof UseItemPhaseAction phaseAction) {
                     init_V_Minecraft_ao(mc, phaseAction);
                  } else if (action instanceof ContextMacroAction contextAction) {
                     init_V_ContextMacroAction(contextAction);
                  } else if (update(action)) {
                     init_V_Minecraft_3(mc, action, action.getDisplayName());
                  } else if (module != null && module.add_Z()) {
                     CountDownLatch latch = new CountDownLatch(1);
                     MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
                     mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(scheduledRun, () -> {
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
                     init_V_Minecraft_3(mc, action, action.getDisplayName());
                  }
               } else {
                  if (action instanceof DropAction dropAction) {
                     postGuiFuture = write(mc, dropAction);
                  }

                  init_V_Minecraft_3(mc, action, action.getDisplayName());
               }

               init_V_MacroAction_Minecraft(action, mc);
               if (postGuiFuture != null && close_Z()) {
                  try {
                     init_V_CompletableFuture(postGuiFuture);
                  } catch (CancellationException var95) {
                  }
               }

               if (close_Z() && !sync(action) && module != null && module.add_Z()) {
                  int delayUs = module.load_I();
                  if (delayUs > 0) {
                     LockSupport.parkNanos(delayUs * 1000L);
                  } else {
                     Thread.onSpinWait();
                  }
               }
            }
         } finally {
            MacroAimController.init_V();
            init_V_EnumSet(heldResources);
         }
      }
   }

   private static void load_V_Minecraft_MacroAction(Minecraft mc, MacroAction action) {
      if (action instanceof AimTargetAction aim && aim.init_Z_3()) {
         RotationMath.Inner_DataUtil rotation = load_Obj_Minecraft(mc, () -> MacroAimController.init(mc, aim.init_M_DataUtil_Minecraft(mc)), null);
         if (rotation != null) {
            MacroAimController.init(rotation);
            pop();
         }
      }
   }

   private static boolean init_Z_MacroAction_Minecraft(MacroAction action, Minecraft mc) {
      if (action instanceof WaitForChatAction chat && chat.init_UtilCore_DataUtil() == PatternCaptureMatcher.Inner_DataUtil.MATCH) {
         return MacroVariableContext.init_j_CachePool_Str_Minecraft(chat.pattern, mc).init_Z();
      } else if (action instanceof WaitForGuiAction gui && gui.matchMode == PatternCaptureMatcher.Inner_DataUtil.MATCH) {
         return MacroVariableContext.init_j_CachePool_Str_Minecraft(gui.guiTitle, mc).init_Z();
      } else if (action instanceof WaitGuiTypeAction gui && gui.matchMode == PatternCaptureMatcher.Inner_DataUtil.MATCH) {
         return MacroVariableContext.init_j_CachePool_Str_Minecraft(gui.title, mc).init_Z();
      } else if (action instanceof WaitInventoryPredicateAction inventory) {
         return MacroVariableContext.init_j_CachePool_Str_Minecraft(inventory.itemName, mc).init_Z();
      } else if (action instanceof WaitDurabilityAction durability && durability.targetMode == WaitDurabilityAction.Inner_CachePool.ITEM) {
         return MacroVariableContext.init_j_CachePool_Str_Minecraft(durability.itemName, mc).init_Z();
      } else {
         return action instanceof WaitEntityTargetAction entity ? MacroVariableContext.init_j_CachePool_Str_Minecraft(entity.entityId, mc).init_Z() : true;
      }
   }

   private static List<AutismSessionState.Inner_CachePool> init_List_I_Minecraft(int count, Minecraft mc) {
      MacroExecutor.Inner_IoWorker context = read_ThreadLocal.get();
      return context != null && context.init_aM() != null ? init_List_aM(context.init_aM(), context.init_I(), count, context.load()) : new ArrayList<>();
   }

   private static boolean write(MacroAction action) {
      return action instanceof OpenContainerAction || action instanceof InteractEntityAction;
   }

   private static CompletableFuture<Void> read_CompletableFuture(Minecraft mc, MacroAction action) {
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
         CompletableFuture<Void> postGuiFuture = write(mc, action);
         load_Z_Minecraft_MacroAction(mc, action);
         return postGuiFuture;
      }

      load_V_Str("Waiting target: " + action.getDisplayName());

      while (close_Z()) {
         if (!init_Z_Minecraft_MacroAction(mc, action)) {
            LockSupport.parkNanos(500000L);
            if (Thread.interrupted()) {
               throw new RuntimeException();
            }
         } else {
            CompletableFuture<Void> postGuiFuture = write(mc, action);
            if (load_Z_Minecraft_MacroAction(mc, action)) {
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

   private static boolean init_Z_Minecraft_MacroAction(Minecraft mc, MacroAction action) {
      return load_Obj_Minecraft(mc, () -> {
         if (action instanceof OpenContainerAction open) {
            return open.load(mc);
         } else {
            return action instanceof InteractEntityAction interact ? interact.load(mc) : true;
         }
      }, Boolean.FALSE);
   }

   private static boolean load_Z_Minecraft_MacroAction(Minecraft mc, MacroAction action) {
      return init_Obj_Minecraft(mc, () -> {
         try {
            if (action instanceof OpenContainerAction open) {
               return open.init_Z_Minecraft_4(mc);
            } else if (action instanceof InteractEntityAction interact) {
               return interact.init_Z_Minecraft(mc);
            } else {
               action.execute(mc);
               return true;
            }
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cError in " + action.getDisplayName() + ": " + e.getMessage());
            return true;
         }
      }, Boolean.FALSE, update(action));
   }

   private static CompletableFuture<Void> write(Minecraft mc, MacroAction action) {
      if (Boolean.TRUE.equals(load_ThreadLocal.get())) {
         return null;
      } else {
         return action instanceof GuiWaitAware wfg && wfg.write_Z() ? init_CompletableFuture_Minecraft_aP(mc, wfg) : null;
      }
   }

   private static CompletableFuture<Void> init_CompletableFuture_Minecraft_aP(Minecraft mc, GuiWaitAware wfg) {
      MacroTemplateResolver.Inner_CachePool guiName = MacroVariableContext.init_j_CachePool_Str_Minecraft(wfg.init_Str_7(), mc);
      if (!guiName.init_Z()) {
         return CompletableFuture.completedFuture(null);
      } else {
         return wfg.g_() ? MacroWaitRegistry.init_CompletableFuture(mc.gui.screen()) : MacroWaitRegistry.init_CompletableFuture(guiName.init_Str());
      }
   }

   private static boolean read_Z(Minecraft mc, MacroAction action) {
      return action instanceof GuiWaitAware wfg ? init_Z_Minecraft_aP(mc, wfg) : true;
   }

   private static boolean init_Z_Minecraft_aP(Minecraft mc, GuiWaitAware action) {
      if (mc != null && action != null && action.read_Z()) {
         MacroTemplateResolver.Inner_CachePool guiName = MacroVariableContext.init_j_CachePool_Str_Minecraft(action.init_Str_7(), mc);
         if (!guiName.init_Z()) {
            return false;
         } else if (init_Z_Minecraft_Str(mc, guiName.init_Str())) {
            update(mc);
            return true;
         } else {
            CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture(guiName.init_Str());
            init_V_CompletableFuture(future);
            update(mc);
            return true;
         }
      } else {
         return true;
      }
   }

   private static boolean init_Z_Minecraft_Str(Minecraft mc, String guiName) {
      if (mc == null || mc.gui.screen() == null) {
         return false;
      }

      if (GuiScreenMatcher.init_Z(mc.gui.screen())) {
         return false;
      }

      String expected = guiName == null ? "" : guiName.trim();
      if (expected.isEmpty()) {
         return true;
      }

      String actual = mc.gui.screen().getTitle() == null ? "" : mc.gui.screen().getTitle().getString();
      return read(expected, actual);
   }

   private static boolean read(String expected, String actual) {
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

   private static void update(Minecraft mc) {
      long deadline = System.nanoTime() + 500000000L;

      while (close_Z()) {
         Boolean ready = load_Obj_Minecraft(
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

   private static void init_V_Minecraft_3(Minecraft mc, MacroAction action, String label) {
      Runnable body = () -> {
         try {
            action.execute(mc);
         } catch (Throwable t) {
            init_V_MacroAction_Str(action, "Action threw " + t.getClass().getSimpleName());
            AutismClientMessaging.sendPrefixed("§cError in " + label + ": " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
         }
      };
      if (update(action)) {
         load_V_Minecraft_Runnable(mc, body);
      } else {
         init_V_Minecraft_Runnable(mc, body);
      }
   }

   private static boolean update(MacroAction action) {
      return action instanceof PacketOrderAware ordered && ordered.h_();
   }

   private static void init_V_Minecraft_MapStore(Minecraft mc, ItemAction ia) {
      load_V_Str(ia.getDisplayName());
      List<Runnable> plan = load_Obj_Minecraft(mc, () -> ia.init_List_Minecraft(mc), Collections.emptyList());
      if (plan != null && !plan.isEmpty()) {
         long delayMs = Math.max(0, ia.clickDelayMs);
         MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
         boolean first = true;

         for (Runnable op : plan) {
            if (!close_Z()) {
               break;
            }

            if (!first && delayMs > 0L) {
               sleep(delayMs);
            }

            first = false;
            mc.execute(() -> init_V_MacroExecutor_Tabl_Runnable(scheduledRun, () -> {
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

   private static void init_V_ContextMacroAction(ContextMacroAction action) {
      if (!autismclient.modules.aD.read_Z() && action != null) {
         try {
            action.run(new MacroExecutor.Inner_DataUtil(init_ThreadLocal.get()));
         } catch (Throwable t) {
            init_V_MacroAction_Str(action, "Context action threw " + t.getClass().getSimpleName());
            AutismClientMessaging.sendPrefixed("§cAddon action error: " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()));
         }
      }
   }

   private static void init_V_MacroAction_Str(MacroAction action, String detail) {
      if (action != null) {
         String owner = MacroActionRegistry.ownerOf(action.getTypeId());
         if (owner != null && !owner.isBlank()) {
            autismclient.b.DataUtil.load_V(owner, detail);
         }
      }
   }

   private static void init_V_Minecraft_ao(Minecraft mc, UseItemPhaseAction action) {
      int times = action.init_I_6();

      for (int i = 0; i < times && close_Z(); i++) {
         init_V_Minecraft_Runnable(mc, () -> {
            try {
               action.init_V_2(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Use phase: " + e.getMessage());
            }
         });
         boolean[] gateInstalled = new boolean[]{false};
         if (action.init_Z_8()) {
            init_V_Minecraft_Runnable(mc, () -> {
               try {
                  gateInstalled[0] = action.init_Z_4(mc);
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cError in Use gate: " + e.getMessage());
               }
            });
         }

         try {
            for (int t = 0; t < action.holdTicks && close_Z(); t++) {
               CompletableFuture<Void> tf = MacroWaitRegistry.load_CompletableFuture();
               init_V_CompletableFuture(tf);
            }
         } finally {
            if (gateInstalled[0]) {
               boolean wasOnThread = mc.isSameThread();
               if (wasOnThread) {
                  action.init_V_14();
               } else {
                  init_V_Minecraft_Runnable(mc, action::init_V_14);
               }
            }
         }

         if (!close_Z()) {
            break;
         }

         init_V_Minecraft_Runnable(mc, () -> {
            try {
               action.load(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Use release: " + e.getMessage());
            }
         });
      }
   }

   private static void init_V_Minecraft_NetHelper(Minecraft mc, ClickAction action) {
      int times = Math.max(1, action.clickCount);
      init_V_Minecraft_5(mc, () -> {
         for (int c = 0; c < times && close_Z(); c++) {
            try {
               action.execute(mc);
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cError in Click: " + e.getMessage());
               break;
            }
         }
      }, action.h_());
   }

   private static boolean sync(MacroAction action) {
      return !(action instanceof ItemAction)
            && !(action instanceof DropAction)
            && !(action instanceof SwapSlotsAction)
            && !(action instanceof ClickAction)
            && !(action instanceof XCarryAction)
         ? action instanceof StoreItemAction storeItemAction && !storeItemAction.persistent
         : true;
   }

   private static void init_V_Minecraft_ah(Minecraft mc, StoreItemAction action) {
      if (action != null) {
         int delayTicks = StoreItemAction.init_I_I_7(action.delayTicks);
         if (delayTicks <= 0) {
            init_V_Minecraft_Runnable(mc, () -> action.init_V_Minecraft_6(mc));
         } else {
            List<Integer> slots = load_Obj_Minecraft(mc, () -> action.init_List_Minecraft_3(mc), List.of());

            for (int i = 0; i < slots.size() && close_Z(); i++) {
               autismclient.util.ContainerClickExecutor.init(mc, slots.get(i), 0, ContainerInput.QUICK_MOVE);
               if (i + 1 < slots.size()) {
                  load_V_I(delayTicks);
               }
            }

            if (action.closeAfter && !action.persistent) {
               init_V_Minecraft_Runnable(mc, () -> autismclient.util.GuiDesyncHelper.init(mc, action.closeSendPkt, false));
            }
         }
      }
   }

   private static void load_V_I(int ticks) {
      for (int i = 0; i < ticks && close_Z(); i++) {
         CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
         init_V_CompletableFuture(tickFuture);
      }
   }

   private static void init_V_Minecraft_an(Minecraft mc, UseItemAction action) {
      if (action != null) {
         boolean sneaking = action.sneak;

         try {
            if (action.useMode == UseItemAction.Inner_DataUtil.CUSTOM_HOLD) {
               init_V_Minecraft_5(mc, () -> {
                  try {
                     if (sneaking) {
                        autismclient.util.SneakPacketController.init(mc, true);
                     }

                     action.execute(mc);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cError in UseItem: " + e.getMessage());
                  }
               }, action.h_());
               if (action.holdTicks <= 0) {
                  return;
               }

               load_V_I(action.holdTicks);
               if (close_Z()) {
                  init_V_Minecraft_5(mc, () -> {
                     try {
                        action.init_V_Minecraft_7(mc);
                     } catch (Exception e) {
                        AutismClientMessaging.sendPrefixed("§cError in UseItem release: " + e.getMessage());
                     }
                  }, action.h_());
                  if (action.waitForFinish) {
                     init_V_Minecraft_I(mc, action.holdTicks + 8);
                  }

                  return;
               }

               return;
            }

            int times = Math.max(1, action.useCount);

            for (int i = 0; i < times && close_Z(); i++) {
               init_V_Minecraft_5(mc, () -> {
                  try {
                     if (sneaking) {
                        autismclient.util.SneakPacketController.init(mc, true);
                     }

                     action.execute(mc);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cError in UseItem: " + e.getMessage());
                  }
               }, action.h_());
               if (action.waitForFinish) {
                  init_V_Minecraft_I(mc, 0);
               }

               if (i + 1 < times && close_Z()) {
                  pop();
               }
            }
         } finally {
            if (sneaking) {
               mc.execute(() -> autismclient.util.SneakPacketController.init(mc, false));
            }
         }
      }
   }

   private static void init_V_Minecraft_StateCache(Minecraft mc, ContainerClickSequenceAction action) {
      if (action != null) {
         List<Integer> slots = action.init();
         int repeats = Math.max(1, action.repeatCount);
         int delay = Math.max(0, action.delayTicks);
         int total = repeats * slots.size();
         int sent = 0;

         for (int repeat = 0; repeat < repeats && close_Z(); repeat++) {
            for (int slot : slots) {
               if (!close_Z()) {
                  return;
               }

               init_V_Minecraft_Runnable(mc, () -> action.init(mc, slot));
               if (delay > 0) {
                  if (++sent < total) {
                     load_V_I(delay);
                  }
               }
            }
         }
      }
   }

   private static void init_V_Minecraft_y(Minecraft mc, PacketBurstAction action) {
      if (action != null && mc != null && mc.getConnection() != null) {
         if (action.flushBefore) {
            AutismSessionState.init_ck().init_I_ClientPacketListen(mc.getConnection());
         }

         int total = action.init_I_7();
         int delay = Math.max(0, action.delayTicks);

         for (int i = 0; i < total && close_Z(); i++) {
            action.init_Z_6(mc);
            if (delay > 0 && i + 1 < total) {
               load_V_I(delay);
            }
         }
      }
   }

   private static void init_V_Minecraft_I(Minecraft mc, int fallbackTicks) {
      if (mc != null && close_Z() && Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> mc.player != null && mc.player.isUsingItem(), Boolean.FALSE))) {
         int remaining = load_Obj_Minecraft(mc, () -> mc.player == null ? 0 : Math.max(0, mc.player.getUseItemRemainingTicks()), 0);
         int maxTicks = Math.max(8, remaining > 0 ? remaining + 8 : fallbackTicks + 8);
         maxTicks = Math.min(maxTicks, 72000);

         for (int i = 0; i < maxTicks && close_Z(); i++) {
            if (!Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> mc.player != null && mc.player.isUsingItem(), Boolean.FALSE))) {
               return;
            }

            CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
            init_V_CompletableFuture(tickFuture);
         }
      }
   }

   private static void init_V_Minecraft_aQ(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         if (xsa.init_Z_7()) {
            load_V_Str("XCarry Cursor");
            init_V_Minecraft_Runnable(mc, () -> xsa.execute(mc));
         } else {
            AutismSessionState sharedState = AutismSessionState.init_ck();
            boolean prevBypass = sharedState.close_Z();
            sharedState.flush(true);

            try {
               AtomicReference<AbstractContainerMenu> containerHandlerRef = new AtomicReference<>();
               AtomicReference<autismclient.util.InteractionTargetRef> containerTargetRef = new AtomicReference<>();
               AtomicReference<List<Integer>> containerSlotIdsRef = new AtomicReference<>(List.of());
               AtomicBoolean usingContainer = new AtomicBoolean(false);
               init_V_Minecraft_Runnable(mc, () -> {
                  if (mc.player != null) {
                     AbstractContainerMenu current = mc.player.containerMenu;
                     if (current != null && current != mc.player.inventoryMenu) {
                        usingContainer.set(true);
                        containerHandlerRef.set(current);
                        containerTargetRef.set(AutismSessionState.init_ck().init_E());
                        containerSlotIdsRef.set(new ArrayList<>(xsa.init_List_AbstractContainerM_InventoryMenu(current, mc.player.inventoryMenu)));
                     }
                  }
               });
               if (usingContainer.get() && containerTargetRef.get() == null) {
                  AutismClientMessaging.sendPrefixed("XCarry click mode: missing container target, using fast mode.");
                  init_V_Minecraft_Runnable(mc, () -> xsa.execute(mc));
                  return;
               }

               if (usingContainer.get()) {
                  for (int slotId : containerSlotIdsRef.get()) {
                     if (!close_Z()) {
                        return;
                     }

                     load_V_Str("XCarry Click: collect");
                     init_V_Minecraft_Runnable(
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
                     pop();
                  }

                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null && mc.getConnection() != null) {
                        AbstractContainerMenu handler = containerHandlerRef.get();
                        if (handler != null) {
                           mc.getConnection().send(new ServerboundContainerClosePacket(handler.containerId));
                        }
                     }
                  });
                  pop();
                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null) {
                        mc.player.containerMenu = mc.player.inventoryMenu;
                     }
                  });
                  pop();
               }

               init_V_Minecraft_Runnable(mc, xsa::init_V_12);
               int xCarryAttempts = 0;

               while (close_Z() && xCarryAttempts++ < 512) {
                  XCarryAction.Inner_CachePool move = load_Obj_Minecraft(
                     mc, () -> mc.player == null ? null : xsa.init_aQ_CachePool_InventoryMenu(mc.player.inventoryMenu), null
                  );
                  if (move == null) {
                     break;
                  }

                  load_V_Str("XCarry Click -> " + move.load + " x" + move.read);
                  int targetBefore = load_Obj_Minecraft(mc, () -> {
                     if (mc.player == null) {
                        return 0;
                     }

                     InventoryMenu handler = mc.player.inventoryMenu;
                     return move.load >= 0 && move.load < handler.slots.size() ? handler.slots.get(move.load).getItem().getCount() : 0;
                  }, 0);
                  boolean moved = load_Obj_Minecraft(mc, () -> {
                     if (mc.player != null && mc.gameMode != null) {
                        InventoryMenu handler = mc.player.inventoryMenu;
                        mc.player.containerMenu = handler;
                        return XCarryAction.init(handler, mc, move.init, move.load, move.read);
                     } else {
                        return false;
                     }
                  }, Boolean.FALSE);
                  if (!moved) {
                     break;
                  }

                  boolean placed = init_Z_Minecraft_3(
                     mc,
                     500L,
                     () -> {
                        if (mc.player == null) {
                           return false;
                        }

                        InventoryMenu handler = mc.player.inventoryMenu;
                        return move.load >= 0 && move.load < handler.slots.size()
                           ? handler.getCarried().isEmpty() && handler.slots.get(move.load).getItem().getCount() >= targetBefore + move.read
                           : false;
                     }
                  );
                  if (!placed) {
                     load_V_Minecraft_I(mc, move.init);
                     break;
                  }

                  pop();
               }

               init_V_Minecraft_Runnable(mc, () -> {
                  if (mc.player != null) {
                     boolean active = XCarryAction.init_Z_InventoryMenu_3(mc.player.inventoryMenu, xsa.carryCursor, xsa.init_Set());
                     AutismSessionState shared = AutismSessionState.init_ck();
                     if (active) {
                        shared.load(xsa.init_Set(), xsa.carryCursor);
                     }

                     shared.sync(active);
                     shared.update(active);
                  }
               });
               if (usingContainer.get()) {
                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null) {
                        AbstractContainerMenu containerHandler = containerHandlerRef.get();
                        if (containerHandler != null) {
                           mc.player.containerMenu = containerHandler;
                        }

                        autismclient.util.InteractionTargetRef containerTarget = containerTargetRef.get();
                        if (containerTarget != null) {
                           XCarryAction.init_V_Minecraft_E(mc, containerTarget);
                        }
                     }
                  });
                  pop();
               }

               return;
            } finally {
               sharedState.flush(prevBypass);
            }
         }
      }
   }

   private static void load_V_Minecraft_aQ(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         if (xsa.init_Z_7()) {
            load_V_Str("XCarry Safe: Cursor");
            init_V_Minecraft_Runnable(mc, () -> xsa.execute(mc));
         } else {
            AutismSessionState sharedState = AutismSessionState.init_ck();
            boolean prevBypass = sharedState.close_Z();
            sharedState.flush(true);

            try {
               AtomicReference<AbstractContainerMenu> containerHandlerRef = new AtomicReference<>();
               AtomicReference<autismclient.util.InteractionTargetRef> containerTargetRef = new AtomicReference<>();
               AtomicReference<List<Integer>> containerSlotIdsRef = new AtomicReference<>(List.of());
               AtomicBoolean usingContainer = new AtomicBoolean(false);
               init_V_Minecraft_Runnable(mc, () -> {
                  if (mc.player != null) {
                     AbstractContainerMenu current = mc.player.containerMenu;
                     if (current != null && current != mc.player.inventoryMenu) {
                        usingContainer.set(true);
                        containerHandlerRef.set(current);
                        containerTargetRef.set(AutismSessionState.init_ck().init_E());
                        containerSlotIdsRef.set(new ArrayList<>(xsa.init_List_AbstractContainerM_InventoryMenu(current, mc.player.inventoryMenu)));
                     }
                  }
               });
               if (usingContainer.get() && containerTargetRef.get() == null) {
                  AutismClientMessaging.sendPrefixed("XCarry safe mode: missing container target, using fast mode.");
                  init_V_Minecraft_Runnable(mc, () -> xsa.execute(mc));
                  return;
               }

               if (usingContainer.get()) {
                  for (int slotId : containerSlotIdsRef.get()) {
                     if (!close_Z()) {
                        return;
                     }

                     load_V_Str("XCarry Safe: collect");
                     init_Z_Minecraft_4(mc, containerHandlerRef.get(), slotId, 0, ContainerInput.QUICK_MOVE);
                  }

                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null && mc.getConnection() != null) {
                        AbstractContainerMenu handler = containerHandlerRef.get();
                        if (handler != null) {
                           mc.getConnection().send(new ServerboundContainerClosePacket(handler.containerId));
                        }
                     }
                  });
                  pop();
                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null) {
                        mc.player.containerMenu = mc.player.inventoryMenu;
                     }
                  });
                  pop();
               }

               init_V_Minecraft_Runnable(mc, xsa::init_V_12);
               int safeDelayTicks = XCarryAction.write(xsa.safeClickDelayTicks);
               boolean delayAfterPickup = xsa.safeClickDelayAfterPickup;
               boolean delayBeforeReturn = xsa.safeClickDelayBeforeReturn;
               int attempts = 0;

               while (close_Z() && attempts++ < 512) {
                  List<XCarryAction.Inner_CachePool> group = load_Obj_Minecraft(mc, () -> init_List_Minecraft_aQ(mc, xsa), List.of());
                  if (group.isEmpty()) {
                     break;
                  }

                  XCarryAction.Inner_CachePool first = group.get(0);
                  int total = 0;

                  for (XCarryAction.Inner_CachePool move : group) {
                     total += Math.max(0, move.read());
                  }

                  load_V_Str("XCarry Safe -> " + group.size() + " slots x" + total);
                  if (!init_Z_Minecraft_3(mc, group, safeDelayTicks, delayAfterPickup, delayBeforeReturn)) {
                     init_V_Minecraft(mc, first.init(), safeDelayTicks, delayBeforeReturn);
                     break;
                  }
               }

               init_V_Minecraft_Runnable(mc, () -> {
                  if (mc.player != null) {
                     boolean active = XCarryAction.init_Z_InventoryMenu_3(mc.player.inventoryMenu, xsa.carryCursor, xsa.init_Set());
                     AutismSessionState shared = AutismSessionState.init_ck();
                     if (active) {
                        shared.load(xsa.init_Set(), xsa.carryCursor);
                     }

                     shared.sync(active);
                     shared.update(active);
                  }
               });
               if (usingContainer.get()) {
                  init_V_Minecraft_Runnable(mc, () -> {
                     if (mc.player != null) {
                        AbstractContainerMenu containerHandler = containerHandlerRef.get();
                        if (containerHandler != null) {
                           mc.player.containerMenu = containerHandler;
                        }

                        autismclient.util.InteractionTargetRef containerTarget = containerTargetRef.get();
                        if (containerTarget != null) {
                           XCarryAction.init_V_Minecraft_E(mc, containerTarget);
                        }
                     }
                  });
                  pop();
               }

               return;
            } finally {
               sharedState.flush(prevBypass);
            }
         }
      }
   }

   private static List<XCarryAction.Inner_CachePool> init_List_Minecraft_aQ(Minecraft mc, XCarryAction xsa) {
      if (mc != null && mc.player != null && xsa != null) {
         InventoryMenu handler = mc.player.inventoryMenu;
         List<XCarryAction.Inner_CachePool> planned = xsa.init_List_InventoryMenu(handler);
         if (planned.isEmpty()) {
            return List.of();
         }

         XCarryAction.Inner_CachePool first = planned.get(0);
         if (first == null) {
            return List.of();
         }

         if (first.init() >= 0 && first.init() < handler.slots.size()) {
            ItemStack source = handler.slots.get(first.init()).getItem();
            if (source.isEmpty()) {
               return List.of();
            }

            ArrayList<XCarryAction.Inner_CachePool> group = new ArrayList<>();
            int remainingSource = source.getCount();

            for (XCarryAction.Inner_CachePool move : planned) {
               if (move == null
                  || move.init() != first.init()
                  || move.read() <= 0
                  || move.read() > remainingSource
                  || !init_Z_InventoryMenu_5(handler, source, move)) {
                  break;
               }

               group.add(move);
               remainingSource -= move.read();
            }

            return group;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static boolean init_Z_Minecraft_3(Minecraft mc, List<XCarryAction.Inner_CachePool> group, int delayTicks, boolean delayAfterPickup, boolean delayBeforeReturn) {
      if (mc != null && mc.player != null && mc.gameMode != null && group != null && !group.isEmpty()) {
         MacroExecutor.Inner_RingBuffer snapshot = load_Obj_Minecraft(
            mc, () -> init_MacroExecutor_Ring_Minecraft_List(mc, group), MacroExecutor.Inner_RingBuffer.init()
         );
         if (!snapshot.init_Z) {
            return false;
         }

         MacroExecutor.Inner_RingBuffer expected = init_MacroExecutor_Ring_Minecraft(mc, group, snapshot, delayTicks, delayAfterPickup, delayBeforeReturn);
         return expected.init_Z;
      } else {
         return false;
      }
   }

   private static MacroExecutor.Inner_RingBuffer init_MacroExecutor_Ring_Minecraft(
      Minecraft mc,
      List<XCarryAction.Inner_CachePool> group,
      MacroExecutor.Inner_RingBuffer snapshot,
      int delayTicks,
      boolean delayAfterPickup,
      boolean delayBeforeReturn
   ) {
      if (mc != null && mc.player != null && mc.gameMode != null && group != null && !group.isEmpty() && snapshot != null && snapshot.init_Z) {
         XCarryAction.Inner_CachePool first = group.get(0);
         if (!Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && handler.getCarried().isEmpty() && init_Z_InventoryMenu_2(handler, group, snapshot);
         }, Boolean.FALSE))) {
            return MacroExecutor.Inner_RingBuffer.init();
         }

         MacroExecutor.Inner_CharEncoder quickCraftPlan = load_Obj_Minecraft(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return init_MacroExecutor_Char_InventoryMenu(handler, group, snapshot);
         }, MacroExecutor.Inner_CharEncoder.init());
         MacroExecutor.Inner_RingBuffer expected = snapshot;
         if (quickCraftPlan.init_Z) {
            load_V_Str("XCarry Safe drag -> " + group.size() + " slots x" + quickCraftPlan.load_I);
            int start = AbstractContainerMenu.getQuickcraftMask(0, quickCraftPlan.init_I);
            int add = AbstractContainerMenu.getQuickcraftMask(1, quickCraftPlan.init_I);
            int end = AbstractContainerMenu.getQuickcraftMask(2, quickCraftPlan.init_I);
            int nextDragIndex = 0;
            if (delayAfterPickup) {
               if (!init_Z_Minecraft_2(mc, first.init(), 0, ContainerInput.PICKUP)) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               init_V_I_Z(delayTicks, true);
               if (!Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
                  InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
                  return init_Z_InventoryMenu_4(handler, snapshot.init_ItemStack, snapshot.init_I);
               }, Boolean.FALSE))) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               if (!init_Z_Minecraft_2(mc, -999, start, ContainerInput.QUICK_CRAFT)) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }
            } else {
               ArrayList<MacroExecutor.Inner_BitSetUtil> dragClicks = new ArrayList<>(group.size() + 3);
               dragClicks.add(new MacroExecutor.Inner_BitSetUtil(first.init(), 0, ContainerInput.PICKUP));
               dragClicks.add(new MacroExecutor.Inner_BitSetUtil(-999, start, ContainerInput.QUICK_CRAFT));

               for (XCarryAction.Inner_CachePool move : group) {
                  if (move != null) {
                     dragClicks.add(new MacroExecutor.Inner_BitSetUtil(move.load(), add, ContainerInput.QUICK_CRAFT));
                  }
               }

               dragClicks.add(new MacroExecutor.Inner_BitSetUtil(-999, end, ContainerInput.QUICK_CRAFT));
               if (!init_Z_Minecraft_MacroExecutor_BitSArr(mc, dragClicks.toArray(MacroExecutor.Inner_BitSetUtil[]::new))) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               nextDragIndex = group.size();
            }

            for (int i = nextDragIndex; i < group.size(); i++) {
               XCarryAction.Inner_CachePool move = group.get(i);
               if (i > nextDragIndex || nextDragIndex > 0) {
                  read(delayTicks);
               }

               if (!init_Z_Minecraft_2(mc, move.load(), add, ContainerInput.QUICK_CRAFT)) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }
            }

            if (nextDragIndex < group.size() && !init_Z_Minecraft_2(mc, -999, end, ContainerInput.QUICK_CRAFT)) {
               return MacroExecutor.Inner_RingBuffer.init();
            }

            boolean firstResidual = true;

            for (Entry<Integer, Integer> residual : quickCraftPlan.init_Map.entrySet()) {
               int targetSlotId = residual.getKey();
               int count = residual.getValue() == null ? 0 : residual.getValue();
               if (count > 0) {
                  if (!firstResidual) {
                     read(delayTicks);
                  }

                  firstResidual = false;
                  if (!init_Z_Minecraft(mc, targetSlotId, count, delayTicks)) {
                     return MacroExecutor.Inner_RingBuffer.init();
                  }
               }
            }

            expected = snapshot.init(quickCraftPlan.load_Map);
         } else {
            int nextMoveIndex = 0;
            if (delayAfterPickup) {
               if (!init_Z_Minecraft_2(mc, first.init(), 0, ContainerInput.PICKUP)) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               init_V_I_Z(delayTicks, true);
            } else {
               XCarryAction.Inner_CachePool firstMove = group.get(0);
               int firstButton = firstMove.read() >= snapshot.init_I ? 0 : 1;
               if (!init_Z_Minecraft_MacroExecutor_BitSArr(
                  mc,
                  new MacroExecutor.Inner_BitSetUtil(first.init(), 0, ContainerInput.PICKUP),
                  new MacroExecutor.Inner_BitSetUtil(firstMove.load(), firstButton, ContainerInput.PICKUP)
               )) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               int remainingFirst = firstButton == 0 ? 0 : Math.max(0, firstMove.read() - 1);
               if (remainingFirst > 0) {
                  read(delayTicks);
                  if (!init_Z_Minecraft(mc, firstMove.load(), remainingFirst, delayTicks)) {
                     return MacroExecutor.Inner_RingBuffer.init();
                  }
               }

               nextMoveIndex = 1;
            }

            for (int i = nextMoveIndex; i < group.size(); i++) {
               XCarryAction.Inner_CachePool move = group.get(i);
               if (move != null && move.read() > 0 && !init_Z_Minecraft(mc, move.load(), move.read(), delayTicks)) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }
            }
         }

         boolean hasLeftover = Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && !handler.getCarried().isEmpty();
         }, Boolean.FALSE));
         if (hasLeftover) {
            if (delayBeforeReturn) {
               read(delayTicks);
            }

            int returnSlot = load_Obj_Minecraft(mc, () -> init_I_Minecraft_I(mc, first.init()), -1);
            if (returnSlot < 0) {
               return MacroExecutor.Inner_RingBuffer.init();
            }

            if (!init_Z_Minecraft_2(mc, returnSlot, 0, ContainerInput.PICKUP)) {
               return MacroExecutor.Inner_RingBuffer.init();
            }
         }

         boolean cursorEmpty = Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
            InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
            return handler != null && handler.getCarried().isEmpty();
         }, Boolean.FALSE));
         return cursorEmpty ? expected : MacroExecutor.Inner_RingBuffer.init();
      } else {
         return MacroExecutor.Inner_RingBuffer.init();
      }
   }

   private static boolean init_Z_Minecraft(Minecraft mc, int targetSlotId, int count, int delayTicks) {
      if (mc != null && count > 0) {
         int remaining = count;

         while (remaining > 0 && close_Z()) {
            int carriedCount = load_Obj_Minecraft(mc, () -> {
               InventoryMenu handler = mc.player == null ? null : mc.player.inventoryMenu;
               return handler == null ? 0 : handler.getCarried().getCount();
            }, 0);
            if (carriedCount <= 0) {
               return false;
            }

            int button = remaining >= carriedCount ? 0 : 1;
            if (!init_Z_Minecraft_2(mc, targetSlotId, button, ContainerInput.PICKUP)) {
               return false;
            }

            if (button == 0) {
               remaining = 0;
            } else {
               remaining--;
            }

            if (remaining > 0) {
               read(delayTicks);
            }
         }

         return remaining <= 0;
      } else {
         return false;
      }
   }

   private static MacroExecutor.Inner_RingBuffer init_MacroExecutor_Ring_Minecraft_List(Minecraft mc, List<XCarryAction.Inner_CachePool> group) {
      if (mc != null && mc.player != null && group != null && !group.isEmpty()) {
         InventoryMenu handler = mc.player.inventoryMenu;
         if (handler != null && handler.getCarried().isEmpty()) {
            XCarryAction.Inner_CachePool first = group.get(0);
            if (first.init() >= 0 && first.init() < handler.slots.size()) {
               Slot sourceSlot = handler.slots.get(first.init());
               ItemStack source = sourceSlot.getItem();
               if (source.isEmpty()) {
                  return MacroExecutor.Inner_RingBuffer.init();
               }

               int total = 0;
               LinkedHashMap<Integer, Integer> startCounts = new LinkedHashMap<>();
               LinkedHashMap<Integer, Integer> expectedCounts = new LinkedHashMap<>();

               for (XCarryAction.Inner_CachePool move : group) {
                  if (move == null || move.init() != first.init()) {
                     return MacroExecutor.Inner_RingBuffer.init();
                  }

                  if (!init_Z_InventoryMenu_5(handler, source, move)) {
                     return MacroExecutor.Inner_RingBuffer.init();
                  }

                  int start = startCounts.computeIfAbsent(move.load(), slotId -> init_I_InventoryMenu_I(handler, slotId));
                  if (start < 0) {
                     return MacroExecutor.Inner_RingBuffer.init();
                  }

                  expectedCounts.put(move.load(), expectedCounts.getOrDefault(move.load(), start) + move.read());
                  total += move.read();
               }

               return total > 0 && total <= source.getCount()
                  ? new MacroExecutor.Inner_RingBuffer(true, source.copy(), source.getCount(), startCounts, expectedCounts)
                  : MacroExecutor.Inner_RingBuffer.init();
            } else {
               return MacroExecutor.Inner_RingBuffer.init();
            }
         } else {
            return MacroExecutor.Inner_RingBuffer.init();
         }
      } else {
         return MacroExecutor.Inner_RingBuffer.init();
      }
   }

   private static boolean init_Z_InventoryMenu_5(InventoryMenu handler, ItemStack source, XCarryAction.Inner_CachePool move) {
      if (handler != null && source != null && !source.isEmpty() && move != null && move.read() > 0) {
         if (move.load() < 0 || move.load() >= handler.slots.size()) {
            return false;
         }

         if (move.init() == move.load()) {
            return false;
         }

         Slot targetSlot = handler.slots.get(move.load());
         if (targetSlot != null && targetSlot.mayPlace(source)) {
            ItemStack target = targetSlot.getItem();
            if (!target.isEmpty() && !ItemStack.isSameItemSameComponents(target, source)) {
               return false;
            }

            int current = target.isEmpty() ? 0 : target.getCount();
            int max = Math.min(source.getMaxStackSize(), targetSlot.getMaxStackSize(source));
            return current + move.read() <= max;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z_InventoryMenu_4(InventoryMenu handler, ItemStack expected, int count) {
      if (handler != null && expected != null && !expected.isEmpty()) {
         ItemStack carried = handler.getCarried();
         return !carried.isEmpty() && carried.getCount() == count && ItemStack.isSameItemSameComponents(carried, expected);
      } else {
         return false;
      }
   }

   private static boolean init_Z_InventoryMenu_2(InventoryMenu handler, List<XCarryAction.Inner_CachePool> group, MacroExecutor.Inner_RingBuffer snapshot) {
      if (handler != null && group != null && snapshot != null && snapshot.init_Z) {
         XCarryAction.Inner_CachePool first = group.isEmpty() ? null : group.get(0);
         if (first != null && first.init() >= 0 && first.init() < handler.slots.size()) {
            ItemStack source = handler.slots.get(first.init()).getItem();
            if (!source.isEmpty() && source.getCount() == snapshot.init_I && ItemStack.isSameItemSameComponents(source, snapshot.init_ItemStack)) {
               for (XCarryAction.Inner_CachePool move : group) {
                  if (move != null && move.init() == first.init()) {
                     int current = init_I_InventoryMenu_I(handler, move.load());
                     Integer expectedStart = snapshot.init_Map.get(move.load());
                     if (expectedStart != null && current == expectedStart) {
                        if (!init_Z_InventoryMenu_5(handler, source, move)) {
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

   private static MacroExecutor.Inner_CharEncoder init_MacroExecutor_Char_InventoryMenu(
      InventoryMenu handler, List<XCarryAction.Inner_CachePool> group, MacroExecutor.Inner_RingBuffer snapshot
   ) {
      if (handler != null && group != null && group.size() >= 2 && snapshot != null && snapshot.init_Z) {
         LinkedHashMap<Integer, Integer> desiredAdds = new LinkedHashMap<>();

         for (XCarryAction.Inner_CachePool move : group) {
            if (move != null && move.read() > 0) {
               if (move.load() >= 0 && move.load() < handler.slots.size()) {
                  Slot targetSlot = handler.slots.get(move.load());
                  if (!init_Z_InventoryMenu_3(handler, targetSlot, snapshot.init_ItemStack)) {
                     return MacroExecutor.Inner_CharEncoder.init();
                  }

                  int start = snapshot.init_Map.getOrDefault(move.load(), init_I_InventoryMenu_I(handler, move.load()));
                  int finalForMove = snapshot.load.getOrDefault(move.load(), start);
                  int desiredAdd = finalForMove - start;
                  if (desiredAdd <= 0) {
                     return MacroExecutor.Inner_CharEncoder.init();
                  }

                  int max = Math.min(snapshot.init_ItemStack.getMaxStackSize(), targetSlot.getMaxStackSize(snapshot.init_ItemStack));
                  if (start + desiredAdd > max) {
                     return MacroExecutor.Inner_CharEncoder.init();
                  }

                  desiredAdds.put(move.load(), desiredAdd);
                  continue;
               }

               return MacroExecutor.Inner_CharEncoder.init();
            }

            return MacroExecutor.Inner_CharEncoder.init();
         }

         MacroExecutor.Inner_CharEncoder best = MacroExecutor.Inner_CharEncoder.init();
         MacroExecutor.Inner_CharEncoder even = init_MacroExecutor_Char_InventoryMenu(handler, snapshot, desiredAdds, 0);
         MacroExecutor.Inner_CharEncoder rightDrag = init_MacroExecutor_Char_InventoryMenu(handler, snapshot, desiredAdds, 1);
         if (even.init_Z) {
            best = even;
         }

         if (rightDrag.init_Z && (!best.init_Z || rightDrag.read < best.read)) {
            best = rightDrag;
         }

         return best;
      } else {
         return MacroExecutor.Inner_CharEncoder.init();
      }
   }

   private static MacroExecutor.Inner_CharEncoder init_MacroExecutor_Char_InventoryMenu(
      InventoryMenu handler, MacroExecutor.Inner_RingBuffer snapshot, LinkedHashMap<Integer, Integer> desiredAdds, int quickCraftType
   ) {
      if (handler != null && snapshot != null && snapshot.init_Z && desiredAdds != null && desiredAdds.size() >= 2) {
         int slotCount = desiredAdds.size();
         int var10000;
         switch (quickCraftType) {
            case 0:
               var10000 = snapshot.init_I / slotCount;
               break;
            case 1:
               var10000 = 1;
               break;
            default:
               var10000 = 0;
         }

         int placeCount = var10000;
         if (placeCount > 0 && snapshot.init_I >= placeCount * slotCount) {
            LinkedHashMap<Integer, Integer> residual = new LinkedHashMap<>();
            LinkedHashMap<Integer, Integer> expected = new LinkedHashMap<>();
            int residualTotal = 0;

            for (Entry<Integer, Integer> entry : desiredAdds.entrySet()) {
               int targetSlotId = entry.getKey();
               int desiredAdd = entry.getValue();
               if (desiredAdd < placeCount) {
                  return MacroExecutor.Inner_CharEncoder.init();
               }

               Slot targetSlot = targetSlotId >= 0 && targetSlotId < handler.slots.size() ? handler.slots.get(targetSlotId) : null;
               if (!init_Z_InventoryMenu_3(handler, targetSlot, snapshot.init_ItemStack)) {
                  return MacroExecutor.Inner_CharEncoder.init();
               }

               int start = snapshot.init_Map.getOrDefault(targetSlotId, init_I_InventoryMenu_I(handler, targetSlotId));
               int max = Math.min(snapshot.init_ItemStack.getMaxStackSize(), targetSlot.getMaxStackSize(snapshot.init_ItemStack));
               if (start + placeCount > max) {
                  return MacroExecutor.Inner_CharEncoder.init();
               }

               int leftover = desiredAdd - placeCount;
               if (leftover > 0) {
                  residual.put(targetSlotId, leftover);
                  residualTotal += leftover;
               }

               expected.put(targetSlotId, start + desiredAdd);
            }

            int carriedAfterQuickCraft = snapshot.init_I - placeCount * slotCount;
            if (residualTotal > carriedAfterQuickCraft) {
               return MacroExecutor.Inner_CharEncoder.init();
            }

            int cost = slotCount + 2 + residualTotal;
            return new MacroExecutor.Inner_CharEncoder(true, quickCraftType, placeCount, cost, residual, expected);
         } else {
            return MacroExecutor.Inner_CharEncoder.init();
         }
      } else {
         return MacroExecutor.Inner_CharEncoder.init();
      }
   }

   private static boolean init_Z_InventoryMenu_3(InventoryMenu handler, Slot targetSlot, ItemStack sourceStack) {
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

   private static boolean init_Z_Minecraft_2(Minecraft mc, int slotId, int button, ContainerInput input) {
      return init_Z_Minecraft_MacroExecutor_BitSArr(mc, new MacroExecutor.Inner_BitSetUtil(slotId, button, input));
   }

   private static boolean init_Z_Minecraft_MacroExecutor_BitSArr(Minecraft mc, MacroExecutor.Inner_BitSetUtil... clicks) {
      return mc != null && clicks != null && clicks.length != 0 ? Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
         if (mc.player != null && mc.gameMode != null) {
            InventoryMenu handler = mc.player.inventoryMenu;
            if (handler == null) {
               return false;
            }

            mc.player.containerMenu = handler;

            for (MacroExecutor.Inner_BitSetUtil click : clicks) {
               if (click == null || click.init_ContainerInput == null) {
                  return false;
               }

               if (click.init_I != -999 && (click.init_I < 0 || click.init_I >= handler.slots.size())) {
                  return false;
               }

               mc.gameMode.handleContainerInput(handler.containerId, click.init_I, click.load, click.init_ContainerInput, mc.player);
            }

            return true;
         } else {
            return false;
         }
      }, Boolean.FALSE)) : false;
   }

   private static void read(int delayTicks) {
      int ticks = XCarryAction.write(delayTicks);

      for (int i = 0; i < ticks && close_Z(); i++) {
         pop();
      }
   }

   private static void init_V_I_Z(int delayTicks, boolean enabled) {
      if (enabled) {
         read(delayTicks);
      }
   }

   private static boolean load(Minecraft mc, int slotId, int button, ContainerInput input) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         boolean clicked = load_Obj_Minecraft(mc, () -> {
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

         pop();
         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_Minecraft_4(Minecraft mc, AbstractContainerMenu handler, int slotId, int button, ContainerInput input) {
      if (mc != null && handler != null && input != null) {
         boolean clicked = load_Obj_Minecraft(mc, () -> {
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

         pop();
         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_Minecraft_2(Minecraft mc, int ticks, BooleanSupplier accepted) {
      int maxTicks = Math.max(1, ticks);

      for (int i = 0; i < maxTicks && close_Z(); i++) {
         if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> accepted != null && accepted.getAsBoolean(), Boolean.FALSE))) {
            return true;
         }

         pop();
      }

      return Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> accepted != null && accepted.getAsBoolean(), Boolean.FALSE));
   }

   private static void init_V_Minecraft_2(Minecraft mc, long startRevision, int minTicks, long maxMs) {
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(Math.max(50L, maxMs));
      int ticks = Math.max(1, minTicks);

      for (int i = 0; i < ticks && close_Z(); i++) {
         pop();
      }

      while (close_Z() && System.nanoTime() < deadline) {
         long revision = MacroWaitRegistry.init_J();
         if (revision != startRevision) {
            pop();
            return;
         }

         pop();
      }
   }

   private static boolean init_Z_Minecraft_6(Minecraft mc, MacroExecutor.Inner_RingBuffer snapshot, long startRevision) {
      init_V_Minecraft_2(mc, startRevision, 2, 250L);
      if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> init_Z_Minecraft_MacroExecutor_Ring(mc, snapshot), Boolean.FALSE))) {
         return true;
      }

      init_V_Minecraft_2(mc, MacroWaitRegistry.init_J(), 3, 350L);
      return Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> init_Z_Minecraft_MacroExecutor_Ring(mc, snapshot), Boolean.FALSE));
   }

   private static boolean init_Z_Minecraft_MacroExecutor_Ring(Minecraft mc, MacroExecutor.Inner_RingBuffer snapshot) {
      if (mc != null && mc.player != null && snapshot != null && snapshot.init_Z) {
         InventoryMenu handler = mc.player.inventoryMenu;
         if (handler != null && handler.getCarried().isEmpty()) {
            for (Entry<Integer, Integer> entry : snapshot.load.entrySet()) {
               int slotId = entry.getKey();
               if (slotId >= 0 && slotId < handler.slots.size()) {
                  ItemStack stack = handler.slots.get(slotId).getItem();
                  if (entry.getValue() <= 0) {
                     if (!stack.isEmpty()) {
                        return false;
                     }
                     continue;
                  }

                  if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, snapshot.init_ItemStack)) {
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

   private static boolean init_Z_Minecraft(Minecraft mc, int preferredSlotId, ItemStack expected, int delayTicks, boolean delayBeforeReturn) {
      if (mc != null && mc.player != null && expected != null && !expected.isEmpty()) {
         if (load_Obj_Minecraft(mc, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty(), Boolean.FALSE)) {
            return true;
         }

         int returnSlot = load_Obj_Minecraft(mc, () -> init_I_Minecraft_I(mc, preferredSlotId), -1);
         if (returnSlot < 0) {
            return false;
         }

         if (delayBeforeReturn) {
            read(delayTicks);
         }

         return load(mc, returnSlot, 0, ContainerInput.PICKUP)
            && init_Z_Minecraft_2(mc, 3, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
      } else {
         return false;
      }
   }

   private static int init_I_Minecraft_I(Minecraft mc, int preferredSlotId) {
      if (mc != null && mc.player != null) {
         InventoryMenu handler = mc.player.inventoryMenu;
         ItemStack carried = handler.getCarried();
         if (carried.isEmpty()) {
            return -1;
         }

         if (init_Z_InventoryMenu(handler, preferredSlotId, carried)) {
            return preferredSlotId;
         }

         for (Slot slot : handler.slots) {
            if (slot != null && slot.index >= 9 && slot.index != 45 && init_Z_InventoryMenu(handler, slot.index, carried)) {
               return slot.index;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_InventoryMenu(InventoryMenu handler, int slotId, ItemStack carried) {
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

   private static int load_I_Minecraft_I(Minecraft mc, int slotId) {
      return mc != null && mc.player != null ? init_I_InventoryMenu_I(mc.player.inventoryMenu, slotId) : -1;
   }

   private static int init_I_InventoryMenu_I(InventoryMenu handler, int slotId) {
      if (handler == null) {
         return -1;
      } else if (slotId >= 0 && slotId < handler.slots.size()) {
         ItemStack stack = handler.slots.get(slotId).getItem();
         return stack.isEmpty() ? 0 : stack.getCount();
      } else {
         return -1;
      }
   }

   private static void init_V_Minecraft(Minecraft mc, int sourceSlotId, int delayTicks, boolean delayBeforeReturn) {
      if (mc != null
         && mc.player != null
         && mc.gameMode != null
         && !load_Obj_Minecraft(mc, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty(), Boolean.FALSE)) {
         int returnSlot = load_Obj_Minecraft(mc, () -> init_I_Minecraft_I(mc, sourceSlotId), -1);
         if (returnSlot >= 0) {
            if (delayBeforeReturn) {
               read(delayTicks);
            }

            load(mc, returnSlot, 0, ContainerInput.PICKUP);
            init_Z_Minecraft_2(mc, 3, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
         } else {
            load_V_Minecraft_I(mc, sourceSlotId);
         }
      }
   }

   private static void load_V_Minecraft_I(Minecraft mc, int sourceSlotId) {
      init_V_Minecraft_Runnable(mc, () -> {
         if (mc.player != null && mc.gameMode != null) {
            InventoryMenu handler = mc.player.inventoryMenu;
            mc.player.containerMenu = handler;
            if (!handler.getCarried().isEmpty() && sourceSlotId >= 0 && sourceSlotId < handler.slots.size()) {
               mc.gameMode.handleContainerInput(handler.containerId, sourceSlotId, 0, ContainerInput.PICKUP, mc.player);
            }
         }
      });
      init_Z_Minecraft_3(mc, 300L, () -> mc.player != null && mc.player.inventoryMenu.getCarried().isEmpty());
   }

   private static boolean init_Z_Minecraft_3(Minecraft mc, long timeoutMs, Supplier<Boolean> condition) {
      long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);

      while (close_Z()) {
         if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, condition, Boolean.FALSE))) {
            return true;
         }

         if (System.nanoTime() >= deadline) {
            break;
         }

         pop();
      }

      return Boolean.TRUE.equals(load_Obj_Minecraft(mc, condition, Boolean.FALSE));
   }

   private static void pop() {
      CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
      init_V_CompletableFuture(tickFuture);
   }

   private static void init_V_Minecraft_MethodUtil(Minecraft mc, InstaBreakAction action) {
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null && mc.gameMode != null) {
         BlockPos target = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
         Direction direction = action.direction == null ? Direction.UP : action.direction;
         int delayTicks = Math.max(0, action.delayTicks);
         int targetTimes = Math.max(0, action.times);
         int completed = 0;
         boolean wasSolid = !init_Z_Minecraft_BlockPos(mc, target);
         if (init_Z_Minecraft_MethodUtil(mc, action)) {
            autismclient.util.BlockPreviewRenderer.init(target);

            try {
               long lastAttemptTick = AutismSessionState.init_ck().state_I() - (delayTicks + 1L);
               long lastSneakTick = -9223372036854775808L;

               while (close_Z() && flush(action)) {
                  if (action.sneak) {
                     long sneakTick = AutismSessionState.init_ck().state_I();
                     if (sneakTick != lastSneakTick) {
                        lastSneakTick = sneakTick;
                        String sneakMode = action.sneakMode;
                        init_V_Minecraft_5(mc, () -> init_V_Minecraft_Str(mc, sneakMode), action.h_());
                     }
                  }

                  boolean isAir = init_Z_Minecraft_BlockPos(mc, target);
                  if (wasSolid && isAir) {
                     load_V_Str("InstaBreak " + ++completed + "/" + (targetTimes == 0 ? "∞" : targetTimes));
                     if (targetTimes > 0 && completed >= targetTimes) {
                        return;
                     }

                     wasSolid = false;
                  } else if (!isAir) {
                     wasSolid = true;
                  }

                  if (!isAir) {
                     long currentTick = AutismSessionState.init_ck().state_I();
                     if (currentTick != lastAttemptTick && currentTick - lastAttemptTick >= delayTicks) {
                        lastAttemptTick = currentTick;
                        if (!init_Z_Minecraft_MethodUtil(mc, action)) {
                           return;
                        }

                        if (action.interact) {
                           InteractTimingMode t = action.interactTiming;
                           boolean interactFirst = t == InteractTimingMode.BEFORE || t == InteractTimingMode.WITH || t == InteractTimingMode.CUSTOM && action.interactCustomMs < 0;
                           long ns = t == InteractTimingMode.CUSTOM ? Math.abs((long)action.interactCustomMs) * 1000000L : 0L;
                           if (interactFirst) {
                              init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, target, direction), action.h_());
                              if (ns > 0L) {
                                 LockSupport.parkNanos(ns);
                              }

                              init_V_Minecraft_5(mc, target, direction, action.h_());
                           } else if (t == InteractTimingMode.AFTER_PLUS) {
                              init_V_Minecraft_5(mc, target, direction, action.h_());
                              pop();
                              init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, target, direction), action.h_());
                           } else {
                              init_V_Minecraft_5(mc, target, direction, action.h_());
                              if (ns > 0L) {
                                 LockSupport.parkNanos(ns);
                              }

                              init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, target, direction), action.h_());
                           }
                        } else {
                           init_V_Minecraft_5(mc, target, direction, action.h_());
                        }
                     }
                  }

                  load_V_Str(
                     "InstaBreak " + target.getX() + "," + target.getY() + "," + target.getZ() + " " + completed + "/" + (targetTimes == 0 ? "∞" : targetTimes)
                  );
                  sleep(1L);
               }

               return;
            } finally {
               if (action.sneak) {
                  String sneakMode = action.sneakMode;
                  init_V_Minecraft_Runnable(mc, () -> load_V_Minecraft_Str(mc, sneakMode));
               }

               autismclient.util.BlockPreviewRenderer.load(target);
            }
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cInstaBreak: missing world or connection");
      }
   }

   private static boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos pos) {
      return mc.level == null || mc.level.isOutsideBuildHeight(pos) || mc.level.getBlockState(pos).isAir();
   }

   private static boolean flush(MacroAction action) {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
      return run != null && run.load_aM != null && run.load_aM.init_List != null ? run.load_aM.init_List.contains(action) : true;
   }

   private static boolean init_Z_Minecraft_MethodUtil(Minecraft mc, InstaBreakAction action) {
      return init_Obj_Minecraft(mc, () -> {
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
               autismclient.util.InventorySlotHelper.init_V(mc, slot);
               return true;
            }
         }

         for (int slot = 9; slot < 36; slot++) {
            if (mc.player.getInventory().getItem(slot).is(ItemTags.PICKAXES)) {
               if (!autismclient.util.InventorySlotHelper.read(mc, slot, preferred)) {
                  return false;
               }

               autismclient.util.InventorySlotHelper.init_V(mc, preferred);
               return true;
            }
         }

         return false;
      }, false, action.h_());
   }

   private static void init_V_Minecraft_5(Minecraft mc, BlockPos pos, Direction direction, boolean tickAligned) {
      init_V_Minecraft_5(mc, () -> {
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

   private static void init_V_Minecraft_CachePool(Minecraft mc, BreakAction action) {
      if (mc != null && mc.player != null && mc.level != null && mc.getConnection() != null && mc.gameMode != null) {
         BlockPos target = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
         Direction direction = action.direction == null ? Direction.UP : action.direction;
         int startDelay = Math.max(0, action.delayTicks);
         int targetTimes = Math.max(0, action.times);
         int completed = 0;
         if (startDelay > 0) {
            write(startDelay);
         }

         boolean[] started = new boolean[]{false};
         boolean useNextSteps = action.interact && action.runNextSteps;
         boolean[] nextStepsFired = new boolean[]{false};
         boolean[] missingNextStepsWarned = new boolean[]{false};
         autismclient.util.BlockPreviewRenderer.init(target);
         long lastTick = -1L;

         while (true) {
            try {
               if (!close_Z() || !flush(action)) {
                  return;
               }

               long now = AutismSessionState.init_ck().state_I();
               if (now != lastTick) {
                  lastTick = now;
                  int result;
                  if (!started[0]) {
                     result = useNextSteps
                        ? init_I_Minecraft(mc, action, target, direction, started, nextStepsFired, missingNextStepsWarned)
                        : init_Obj_Minecraft(mc, () -> load(mc, action, target, direction, started), -1, action.h_());
                  } else if (action.interact && action.interactTiming == InteractTimingMode.CUSTOM) {
                     result = useNextSteps ? load(mc, action, target, direction, nextStepsFired, missingNextStepsWarned) : read(mc, action, target, direction);
                  } else {
                     result = useNextSteps
                        ? init_I_Minecraft_2(mc, action, target, direction, nextStepsFired, missingNextStepsWarned)
                        : init_Obj_Minecraft(mc, () -> load(mc, action, target, direction), -1, action.h_());
                  }

                  if (result < 0) {
                     return;
                  }

                  if (result != 1) {
                     load_V_Str(
                        "Break " + target.getX() + "," + target.getY() + "," + target.getZ() + " " + completed + "/" + (targetTimes == 0 ? "∞" : targetTimes)
                     );
                  } else {
                     boolean completedStartedBreak = started[0];
                     if (useNextSteps && completedStartedBreak) {
                        load(mc, action, nextStepsFired, missingNextStepsWarned);
                     } else if (action.interact && action.interactTiming == InteractTimingMode.AFTER_PLUS) {
                        pop();
                        init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, target, direction), action.h_());
                     }

                     load_V_Str("Break " + ++completed + "/" + (targetTimes == 0 ? "∞" : targetTimes));
                     if (targetTimes > 0 && completed >= targetTimes) {
                        return;
                     }

                     started[0] = false;
                     nextStepsFired[0] = false;
                     init_V_Minecraft_BlockPos(mc, target);
                  }
               } else {
                  sleep(1L);
               }
            } finally {
               boolean wasMining = started[0];
               init_V_Minecraft_5(mc, () -> {
                  if (wasMining && mc.gameMode != null && mc.gameMode.isDestroying()) {
                     mc.gameMode.stopDestroyBlock();
                  }

                  if (action.sneak) {
                     load_V_Minecraft_Str(mc, action.sneakMode);
                  }
               }, action.h_());
               autismclient.util.BlockPreviewRenderer.load(target);
            }
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cBreak: missing world or connection");
      }
   }

   private static int init_I_Minecraft(
      Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started, boolean[] nextStepsFired, boolean[] missingNextStepsWarned
   ) {
      MacroExecutor.Inner_CachePool plan = load_Obj_Minecraft(
         mc, () -> init_MacroExecutor_Cach_Minecraft(mc, action, pos), new MacroExecutor.Inner_CachePool(-1, false, false)
      );
      if (plan.init_I() != 0) {
         return plan.init_I();
      }

      if (plan.init_Z() && plan.load()) {
         init_V_Minecraft_3(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!close_Z()) {
            return -1;
         }
      }

      return init_Obj_Minecraft(mc, () -> init_I_Minecraft_2(mc, action, pos, dir, started), -1, action.h_());
   }

   private static MacroExecutor.Inner_CachePool init_MacroExecutor_Cach_Minecraft(Minecraft mc, BreakAction action, BlockPos pos) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return new MacroExecutor.Inner_CachePool(-1, false, false);
      }

      if (mc.level.isOutsideBuildHeight(pos)) {
         return new MacroExecutor.Inner_CachePool(1, false, false);
      }

      BlockState state = mc.level.getBlockState(pos);
      if (state.isAir()) {
         return new MacroExecutor.Inner_CachePool(1, false, false);
      }

      if (action.sneak) {
         init_V_Minecraft_Str(mc, action.sneakMode);
      }

      if (action.autoTool) {
         autismclient.util.BlockBreakSpeedHelper.init(mc, state, action.considerInventory, true);
         state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return new MacroExecutor.Inner_CachePool(1, false, false);
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
      return new MacroExecutor.Inner_CachePool(0, instant, interactFirst);
   }

   private static int init_I_Minecraft_2(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return -1;
      }

      if (!mc.level.isOutsideBuildHeight(pos) && !mc.level.getBlockState(pos).isAir()) {
         if (action.sneak) {
            init_V_Minecraft_Str(mc, action.sneakMode);
         }

         mc.gameMode.startDestroyBlock(pos, dir);
         autismclient.util.BlockAttackGraceWindow.init(mc.player);
         Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
         started[0] = true;
         return mc.level.getBlockState(pos).isAir() ? 1 : 0;
      } else {
         return 1;
      }
   }

   private static int load(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] started) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (!state.isAir() && !mc.level.isOutsideBuildHeight(pos)) {
            if (action.sneak) {
               init_V_Minecraft_Str(mc, action.sneakMode);
            }

            if (action.autoTool) {
               autismclient.util.BlockBreakSpeedHelper.init(mc, state, action.considerInventory, true);
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
               init_V_Minecraft_7(mc, pos, dir);
            }

            mc.gameMode.startDestroyBlock(pos, dir);
            autismclient.util.BlockAttackGraceWindow.init(mc.player);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            started[0] = true;
            if (instant && action.interact && !interactFirst && action.interactTiming != InteractTimingMode.AFTER_PLUS) {
               init_V_Minecraft_7(mc, pos, dir);
            }

            return mc.level.getBlockState(pos).isAir() ? 1 : 0;
         } else {
            return 1;
         }
      } else {
         return -1;
      }
   }

   private static int init_I_Minecraft_2(
      Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] nextStepsFired, boolean[] missingNextStepsWarned
   ) {
      MacroExecutor.Inner_StreamUtil plan = load_Obj_Minecraft(
         mc, () -> init_MacroExecutor_Stre_Minecraft(mc, action, pos), new MacroExecutor.Inner_StreamUtil(-1, false, false)
      );
      if (plan.init_I() != 0) {
         return plan.init_I();
      }

      InteractTimingMode timing = action.interactTiming;
      if (timing == InteractTimingMode.BEFORE && plan.load()) {
         init_V_Minecraft_3(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!close_Z()) {
            return -1;
         }
      }

      if (timing == InteractTimingMode.WITH && plan.init_Z()) {
         init_V_Minecraft_3(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!close_Z()) {
            return -1;
         }
      }

      return init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, action, pos, dir), -1, action.h_());
   }

   private static MacroExecutor.Inner_StreamUtil init_MacroExecutor_Stre_Minecraft(Minecraft mc, BreakAction action, BlockPos pos) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return new MacroExecutor.Inner_StreamUtil(1, false, false);
         }

         if (action.sneak) {
            init_V_Minecraft_Str(mc, action.sneakMode);
         }

         boolean breaksThisTick = init_Z_Minecraft_9(mc, pos, state);
         boolean breaksNextTick = !breaksThisTick && load_Z_Minecraft(mc, pos, state);
         return new MacroExecutor.Inner_StreamUtil(0, breaksThisTick, breaksNextTick);
      } else {
         return new MacroExecutor.Inner_StreamUtil(-1, false, false);
      }
   }

   private static int init_I_Minecraft(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      if (mc.level == null || mc.gameMode == null || mc.player == null || mc.getConnection() == null) {
         return -1;
      }

      if (mc.level.getBlockState(pos).isAir()) {
         return 1;
      }

      if (action.sneak) {
         init_V_Minecraft_Str(mc, action.sneakMode);
      }

      mc.gameMode.continueDestroyBlock(pos, dir);
      autismclient.util.BlockAttackGraceWindow.init(mc.player);
      Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
      return mc.level.getBlockState(pos).isAir() ? 1 : 0;
   }

   private static int load(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      if (mc.level != null && mc.gameMode != null && mc.player != null && mc.getConnection() != null) {
         BlockState state = mc.level.getBlockState(pos);
         if (state.isAir()) {
            return 1;
         }

         if (action.sneak) {
            init_V_Minecraft_Str(mc, action.sneakMode);
         }

         boolean breaksThisTick = init_Z_Minecraft_9(mc, pos, state);
         boolean breaksNextTick = !breaksThisTick && load_Z_Minecraft(mc, pos, state);
         InteractTimingMode t = action.interactTiming;
         if (action.interact && t == InteractTimingMode.BEFORE && breaksNextTick) {
            init_V_Minecraft_7(mc, pos, dir);
         }

         if (action.interact && t == InteractTimingMode.WITH && breaksThisTick) {
            init_V_Minecraft_7(mc, pos, dir);
         }

         mc.gameMode.continueDestroyBlock(pos, dir);
         autismclient.util.BlockAttackGraceWindow.init(mc.player);
         Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
         if (action.interact && t == InteractTimingMode.AFTER && breaksThisTick) {
            init_V_Minecraft_7(mc, pos, dir);
         }

         return mc.level.getBlockState(pos).isAir() ? 1 : 0;
      } else {
         return -1;
      }
   }

   private static int load(Minecraft mc, BreakAction action, BlockPos pos, Direction dir, boolean[] nextStepsFired, boolean[] missingNextStepsWarned) {
      boolean willBreak = Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
         if (mc.level != null && mc.player != null) {
            BlockState state = mc.level.getBlockState(pos);
            return state.isAir() ? Boolean.FALSE : init_Z_Minecraft_9(mc, pos, state);
         } else {
            return Boolean.FALSE;
         }
      }, Boolean.FALSE));
      if (!willBreak) {
         return init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, action, pos, dir), -1, action.h_());
      }

      long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
      int broke;
      if (action.interactCustomMs < 0) {
         init_V_Minecraft_3(mc, action, nextStepsFired, missingNextStepsWarned);
         if (!close_Z()) {
            return -1;
         }

         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         broke = init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, pos, dir), -1, action.h_());
      } else {
         broke = init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, pos, dir), -1, action.h_());
      }

      return broke;
   }

   private static int read(Minecraft mc, BreakAction action, BlockPos pos, Direction dir) {
      boolean willBreak = Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> {
         if (mc.level != null && mc.player != null) {
            BlockState s = mc.level.getBlockState(pos);
            return s.isAir() ? Boolean.FALSE : init_Z_Minecraft_9(mc, pos, s);
         } else {
            return Boolean.FALSE;
         }
      }, Boolean.FALSE));
      if (!willBreak) {
         return init_Obj_Minecraft(mc, () -> {
            if (mc.level == null || mc.gameMode == null || mc.player == null) {
               return -1;
            }

            if (mc.level.getBlockState(pos).isAir()) {
               return 1;
            }

            if (action.sneak) {
               init_V_Minecraft_Str(mc, action.sneakMode);
            }

            mc.gameMode.continueDestroyBlock(pos, dir);
            autismclient.util.BlockAttackGraceWindow.init(mc.player);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            return mc.level.getBlockState(pos).isAir() ? 1 : 0;
         }, -1, action.h_());
      }

      long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
      int broke;
      if (action.interactCustomMs < 0) {
         init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, pos, dir), action.h_());
         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         broke = init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, pos, dir), -1, action.h_());
      } else {
         broke = init_Obj_Minecraft(mc, () -> init_I_Minecraft(mc, pos, dir), -1, action.h_());
         if (ns > 0L) {
            LockSupport.parkNanos(ns);
         }

         init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, pos, dir), action.h_());
      }

      return broke;
   }

   private static int init_I_Minecraft(Minecraft mc, BlockPos pos, Direction dir) {
      if (mc.level == null || mc.gameMode == null || mc.player == null) {
         return -1;
      }

      if (mc.level.getBlockState(pos).isAir()) {
         return 1;
      }

      mc.gameMode.continueDestroyBlock(pos, dir);
      autismclient.util.BlockAttackGraceWindow.init(mc.player);
      Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
      return mc.level.getBlockState(pos).isAir() ? 1 : 0;
   }

   private static boolean init_Z_Minecraft_9(Minecraft mc, BlockPos pos, BlockState state) {
      float progress = mc.gameMode instanceof AutismMultiPlayerGameModeAccessor acc ? acc.autism$getDestroyProgress() : 0.0F;
      float delta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
      return progress > 0.0F && progress + delta >= 1.0F;
   }

   private static boolean load_Z_Minecraft(Minecraft mc, BlockPos pos, BlockState state) {
      float progress = mc.gameMode instanceof AutismMultiPlayerGameModeAccessor acc ? acc.autism$getDestroyProgress() : 0.0F;
      float delta = state.getDestroyProgress(mc.player, mc.player.level(), pos);
      return progress > 0.0F && progress + 2.0F * delta >= 1.0F;
   }

   private static void init_V_Minecraft_3(Minecraft mc, BreakAction action, boolean[] fired, boolean[] missingWarned) {
      if (fired == null || !fired[0]) {
         List<MacroAction> actions = action.init_List();
         if (actions != null && !actions.isEmpty()) {
            if (fired != null) {
               fired[0] = true;
            }

            action.init_V();
            String names = actions.size() == 1 ? actions.get(0).getDisplayName() : actions.size() + " steps";
            load_V_Str("Break: next " + names);
            autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
            boolean previousSuppressPostGui = Boolean.TRUE.equals(load_ThreadLocal.get());

            try {
               load_ThreadLocal.set(true);

               for (MacroAction next : actions) {
                  if (next != null && next.isEnabled() && close_Z()) {
                     try {
                        init_V_Minecraft_2(mc, next, module);
                     } catch (Throwable t) {
                        AutismClientMessaging.sendPrefixed("§cBreak next step failed (" + next.getDisplayName() + "): " + init_Str_Throwable(t));
                     }
                  }
               }
            } finally {
               load_ThreadLocal.set(previousSuppressPostGui);
            }
         } else if (missingWarned != null && !missingWarned[0]) {
            missingWarned[0] = true;
            AutismClientMessaging.sendPrefixed("§cBreak: no next steps found. Add action(s) after this Break step.");
         }
      }
   }

   private static void load(Minecraft mc, BreakAction action, boolean[] fired, boolean[] missingWarned) {
      if (action.interact && action.runNextSteps && action.interactTiming != null) {
         switch (action.interactTiming) {
            case AFTER:
               init_V_Minecraft_3(mc, action, fired, missingWarned);
               break;
            case AFTER_PLUS:
               pop();
               init_V_Minecraft_3(mc, action, fired, missingWarned);
               break;
            case CUSTOM:
               if (action.interactCustomMs >= 0) {
                  long ns = Math.abs((long)action.interactCustomMs) * 1000000L;
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  init_V_Minecraft_3(mc, action, fired, missingWarned);
               }
         }
      }
   }

   private static void init_V_Minecraft_7(Minecraft mc, BlockPos pos, Direction dir) {
      if (mc.gameMode != null && mc.player != null) {
         Vec3 hit = Vec3.atCenterOf(pos).add(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
         BlockHitResult blockHit = new BlockHitResult(hit, dir, pos, false);
         mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
      }
   }

   private static void init_V_Minecraft_BlockPos(Minecraft mc, BlockPos pos) {
      while (close_Z()) {
         if (!init_Z_Minecraft_BlockPos(mc, pos)) {
            return;
         }

         sleep(1L);
      }
   }

   private static void write(int ticks) {
      long start = AutismSessionState.init_ck().state_I();

      while (close_Z() && AutismSessionState.init_ck().state_I() - start < ticks) {
         sleep(1L);
      }
   }

   private static void init_V_Minecraft_Str(Minecraft mc, String mode) {
      autismclient.util.SneakPacketController.init(mc, mode);
   }

   private static void load_V_Minecraft_Str(Minecraft mc, String mode) {
      autismclient.util.SneakPacketController.load(mc, mode);
      if (!autismclient.util.SneakPacketController.init(mode)) {
         load_V_Minecraft(mc);
      }
   }

   private static void init_V_Minecraft_Runnable(Minecraft mc, Runnable runnable) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
         Runnable contextual = () -> init_V_MacroExecutor_Tabl_Runnable(scheduledRun, runnable);
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

   private static void init_V_Minecraft_5(Minecraft mc, Runnable runnable, boolean tickAligned) {
      if (tickAligned) {
         load_V_Minecraft_Runnable(mc, runnable);
      } else {
         init_V_Minecraft_Runnable(mc, runnable);
      }
   }

   private static void load_V_Minecraft(Minecraft mc, Runnable runnable, boolean tickAligned) {
      if (tickAligned) {
         load_V_Minecraft_Runnable(mc, runnable);
      } else if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
         mc.execute(() -> {
            if (!autismclient.modules.aD.read_Z()) {
               init_V_MacroExecutor_Tabl_Runnable(scheduledRun, runnable);
            }
         });
      }
   }

   private static void load_V_Minecraft_Runnable(Minecraft mc, Runnable runnable) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
         Runnable contextual = () -> init_V_MacroExecutor_Tabl_Runnable(scheduledRun, runnable);
         if (mc.isSameThread()) {
            if (!autismclient.modules.aD.read_Z()) {
               contextual.run();
            }
         } else {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicBoolean pending = new AtomicBoolean(true);
            init_ConcurrentLinkedQu.add(() -> {
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

   public static void load_V() {
      if (!autismclient.modules.AutoTotemModule.init_Z_6()) {
         Runnable task;
         while ((task = init_ConcurrentLinkedQu.poll()) != null) {
            try {
               task.run();
            } catch (Throwable t) {
               AutismClientAddon.LOG.debug("[Autism] Tick-aligned macro task failed", t);
            }
         }

         tick();
      }
   }

   private static <T> T load_Obj_Minecraft(Minecraft mc, Supplier<T> supplier, T fallback) {
      if (autismclient.modules.aD.read_Z()) {
         return fallback;
      }

      MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
      Supplier<T> contextual = () -> {
         AtomicReference<T> value = new AtomicReference<>(fallback);
         init_V_MacroExecutor_Tabl_Runnable(scheduledRun, () -> value.set(supplier.get()));
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

   private static <T> T init_Obj_Minecraft(Minecraft mc, Supplier<T> supplier, T fallback, boolean tickAligned) {
      return tickAligned ? init_Obj_Minecraft(mc, supplier, fallback) : load_Obj_Minecraft(mc, supplier, fallback);
   }

   static <T> T init_Obj_Minecraft(Minecraft mc, Supplier<T> supplier, T fallback) {
      if (autismclient.modules.aD.read_Z()) {
         return fallback;
      }

      MacroExecutor.Inner_TableIndex scheduledRun = init_ThreadLocal.get();
      Supplier<T> contextual = () -> {
         AtomicReference<T> value = new AtomicReference<>(fallback);
         init_V_MacroExecutor_Tabl_Runnable(scheduledRun, () -> value.set(supplier.get()));
         return value.get();
      };
      if (mc.isSameThread()) {
         return autismclient.modules.aD.read_Z() ? fallback : contextual.get();
      }

      AtomicReference<T> result = new AtomicReference<>(fallback);
      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean pending = new AtomicBoolean(true);
      init_ConcurrentLinkedQu.add(() -> {
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

   private static void init_V_Minecraft_K(Minecraft mc, PlaceAction action) {
      if (mc != null && mc.player != null && mc.level != null) {
         MacroItemTarget target = action.init_ArrayUtil().init_ArrayUtil_Minecraft(mc);
         if (target != null) {
            if (action.waitForItem && target != null && (target.update() || target.init_Z())) {
               load_V_Str("Waiting for " + init_Str_ArrayUtil(target));
               init_V_Minecraft_ArrayUtil(mc, target);
            }

            if (!action.interact) {
               init_V_Minecraft_3(mc, action, "Place");
            } else {
               BlockPos placePos = action.blockPos == null ? BlockPos.ZERO : action.blockPos;
               Direction face = action.manualDirection && action.direction != null ? action.direction : Direction.UP;
               InteractTimingMode t = action.interactTiming;
               boolean interactFirst = t == InteractTimingMode.BEFORE || t == InteractTimingMode.CUSTOM && action.interactCustomMs < 0;
               long ns = t == InteractTimingMode.CUSTOM ? Math.abs((long)action.interactCustomMs) * 1000000L : 0L;
               if (interactFirst) {
                  init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, placePos, face), action.h_());
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  init_V_Minecraft_3(mc, action, "Place");
               } else if (t == InteractTimingMode.WITH) {
                  init_V_Minecraft_5(mc, () -> {
                     try {
                        action.execute(mc);
                     } catch (Exception e) {
                        AutismClientAddon.LOG.debug("[Autism] Macro WITH-interact action failed", e);
                     }

                     init_V_Minecraft_7(mc, placePos, face);
                  }, action.h_());
               } else if (t == InteractTimingMode.AFTER_PLUS) {
                  init_V_Minecraft_3(mc, action, "Place");
                  pop();
                  init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, placePos, face), action.h_());
               } else {
                  init_V_Minecraft_3(mc, action, "Place");
                  if (ns > 0L) {
                     LockSupport.parkNanos(ns);
                  }

                  init_V_Minecraft_5(mc, () -> init_V_Minecraft_7(mc, placePos, face), action.h_());
               }
            }
         }
      }
   }

   private static void init_V_Minecraft(Minecraft mc, int slotId, MacroItemTarget target) {
      while (close_Z()) {
         if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> init_Z_Minecraft(mc, slotId, target), Boolean.FALSE))) {
            return;
         }

         pop();
      }
   }

   private static void read(Minecraft mc, int slotId) {
      while (close_Z()) {
         if (Boolean.TRUE.equals(load_Obj_Minecraft(mc, () -> init_Z_Minecraft_I(mc, slotId), Boolean.FALSE))) {
            return;
         }

         pop();
      }
   }

   private static boolean init_Z_Minecraft(Minecraft mc, int slotId, MacroItemTarget target) {
      if (mc.player != null && mc.player.containerMenu != null) {
         int handlerSlot = autismclient.util.InventorySlotHelper.read_I(mc, slotId);
         if (handlerSlot >= 0 && handlerSlot < mc.player.containerMenu.slots.size()) {
            Slot slot = mc.player.containerMenu.slots.get(handlerSlot);
            if (slot != null && !slot.getItem().isEmpty()) {
               MacroItemTarget resolvedTarget = target == null ? MacroItemTarget.init_ArrayUtil_I(slotId) : target.init_ArrayUtil();
               if (!resolvedTarget.init_Z()) {
                  resolvedTarget.init_I = slotId;
               }

               return resolvedTarget.init_Z_ItemStack_I(slot.getItem(), slotId);
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

   private static boolean init_Z_Minecraft_I(Minecraft mc, int slotId) {
      if (mc.player != null && mc.player.containerMenu != null) {
         int handlerSlot = autismclient.util.InventorySlotHelper.read_I(mc, slotId);
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

   private static void load_V_Minecraft_MapStore(Minecraft mc, ItemAction ia) {
      if (ia != null) {
         List<MacroItemTarget> targets = init_List_Minecraft(mc, ia.itemTargets, ia.itemNames);
         if (targets.isEmpty()) {
            if (ia.useSlot && ia.targetSlot >= 0) {
               load_V_Str("Waiting for slot " + ia.targetSlot);
               read(mc, ia.targetSlot);
            }
         } else {
            for (MacroItemTarget target : targets) {
               init_V_Minecraft_ArrayUtil(mc, target);
            }
         }
      }
   }

   private static void init_V_Minecraft_ArrayUtil(Minecraft mc, MacroItemTarget target) {
      MacroItemTarget resolvedTarget = target == null ? null : target.init_ArrayUtil_Minecraft(mc);
      if (resolvedTarget != null) {
         String itemLabel = init_Str_ArrayUtil(resolvedTarget);
         int slot = resolvedTarget.init_Z() ? resolvedTarget.init_I : -1;
         if (slot >= 0 && resolvedTarget.update()) {
            load_V_Str("Waiting for " + itemLabel);
            init_V_Minecraft(mc, slot, resolvedTarget);
         } else if (slot >= 0) {
            load_V_Str("Waiting for slot " + slot);
            read(mc, slot);
         } else if (resolvedTarget.update()) {
            load_V_Str("Waiting for item in GUI: " + itemLabel);
            CompletableFuture<Void> waitFuture = MacroWaitRegistry.load(resolvedTarget);
            init_V_CompletableFuture(waitFuture);
         }
      }
   }

   private static MacroItemTarget init_ArrayUtil_ArrayUtil_Str(MacroItemTarget target, String legacyEntry) {
      return target != null && (target.init_Z() || target.update()) ? target.init_ArrayUtil() : MacroItemTarget.update(legacyEntry);
   }

   private static List<MacroItemTarget> init_List_Minecraft(Minecraft mc, List<MacroItemTarget> targets, List<String> legacyEntries) {
      List<MacroItemTarget> resolved = new ArrayList<>();
      if (targets != null) {
         for (MacroItemTarget target : targets) {
            if (target != null && (target.init_Z() || target.update())) {
               MacroItemTarget runtimeTarget = target.init_ArrayUtil_Minecraft(mc);
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
         MacroItemTarget parsed = MacroItemTarget.update(entry);
         MacroItemTarget runtimeTarget = parsed.init_ArrayUtil_Minecraft(mc);
         if (runtimeTarget != null && (runtimeTarget.init_Z() || runtimeTarget.update())) {
            resolved.add(runtimeTarget);
         }
      }

      return resolved;
   }

   private static String init_Str_ArrayUtil(MacroItemTarget target) {
      if (target == null) {
         return "";
      }

      String summary = target.load_Str();
      if (summary != null && !summary.isBlank()) {
         return summary;
      }

      String legacy = target.write_Str();
      return legacy == null ? "" : legacy;
   }

   private static void init_V_Minecraft_SyncManager(Minecraft mc, CraftAction craftAction) {
      if (craftAction != null && craftAction.init_Z_5()) {
         for (int index = 0; index < craftAction.entries.size(); index++) {
            CraftAction.Inner_DataUtil entry = craftAction.entries.get(index);
            if (entry != null && entry.init_Z()) {
               load_V_Str(
                  entry.init_Z
                     ? "Crafting " + entry.load_Str + " [Max] (" + (index + 1) + "/" + craftAction.entries.size() + ")"
                     : "Crafting " + entry.load_Str + " x" + Math.max(1, entry.read_I) + " (" + (index + 1) + "/" + craftAction.entries.size() + ")"
               );
               autismclient.util.AutoCraftingEngine.Inner_TaskQueue option = autismclient.util.AutoCraftingEngine.init(mc, entry.init_Str);
               if (option == null) {
                  option = autismclient.util.AutoCraftingEngine.init(mc, entry.init_I);
               }

               if (option == null) {
                  load_V_Str("Recipe not found");
                  AutismClientMessaging.sendPrefixed(
                     "§cRecipe not found: " + (entry.load_Str != null && !entry.load_Str.isBlank() ? entry.load_Str : "unknown")
                  );
                  return;
               }

               int desiredAmount = autismclient.util.AutoCraftingEngine.init(option, entry.read_I, entry.init_Z);
               if (desiredAmount <= 0) {
                  load_V_Str("No space or materials");
                  AutismClientMessaging.sendPrefixed("§cNo space or materials for " + entry.load_Str + ".");
                  return;
               }

               autismclient.util.AutoCraftingEngine.Inner_CachePool result = autismclient.util.AutoCraftingEngine.init_J_CachePool(mc, entry.init_Str, entry.init_I, desiredAmount);
               load_V_Str(result.init_Str);
               if (!result.init_Z) {
                  AutismClientMessaging.sendPrefixed("§c" + result.init_Str);
                  return;
               }

               if (close_Z()) {
                  CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                  init_V_CompletableFuture(tickFuture);
               }
            }
         }
      } else {
         load_V_Str("Crafting");
         AutismClientMessaging.sendPrefixed("§cNo craft recipes selected.");
      }
   }

   private static void init_V_Minecraft_CharEncoder(Minecraft mc, GoToAction goToAction) {
      load_V_Str(String.format("Going to %.0f,%.0f,%.0f", goToAction.f_x_D, goToAction.f_y_D, goToAction.f_z_D));
      if (mc.player != null && mc.level != null) {
         BlockPos target = new BlockPos((int)Math.floor(goToAction.f_x_D), (int)Math.floor(goToAction.f_y_D), (int)Math.floor(goToAction.f_z_D));
         double radius = goToAction.arrivalRadius;
         int timeoutSeconds = Math.max(1, goToAction.timeoutMs / 1000);
         MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
         AiOperation op = init_AiOperation_Minecraft(
            mc, run, () -> autismclient.ai.a.AiGoto.init_AiOperation_BlockPos(target, radius, false, true, true, timeoutSeconds), false
         );
         if (op == null) {
            AutismClientMessaging.sendPrefixed("Failed to start navigation.");
         } else {
            try {
               if (!goToAction.waitForArrival) {
                  load_V_Str("Goto started");
                  if (run != null && run.init_AiOperation == op) {
                     run.init_AiOperation = null;
                     init_CopyOnWriteArrayLi.add(op);
                     sync(run);
                  }

                  return;
               }

               while (close_Z()) {
                  AiOperation.Result result = run != null ? run.init_AiOperation_Result : null;
                  if (result != null && result.terminal()) {
                     if (result.status() == AiOperation.Status.SUCCEEDED) {
                        load_V_Str("Goto finished");
                     } else {
                        load_V_Str("Goto failed");
                        AutismClientMessaging.sendPrefixed("§cGoto failed: " + result.message());
                     }

                     return;
                  }

                  CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                  init_V_CompletableFuture(tickFuture);
               }
            } finally {
               if (!close_Z()) {
                  read(run, mc);
               } else if (run == null || run.init_AiOperation == null) {
                  sync(run);
               }
            }

            return;
         }
      } else {
         AutismClientMessaging.sendPrefixed("No active world; skipping goto action.");
      }
   }

   private static void init_V_Minecraft_q(Minecraft mc, MineAction mineAction) {
      String mineDesc = mineAction.targetBlocks.isEmpty() ? "none" : mineAction.targetBlocks.get(0);
      load_V_Str("Mining: " + mineDesc);
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
               MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();
               MacroExecutor.Inner_RenderState mineTracker = mineAction.stopMinedCount
                  ? MacroExecutor.Inner_RenderState.init_MacroExecutor_Rend_Minecraft_List(mc, mineAction.targetBlocks)
                  : null;
               if (mineTracker != null) {
                  mineTracker.init_V_Minecraft(mc);
                  if (run != null) {
                     run.init_MacroExecutor_Rend = mineTracker;
                  }
               }

               try {
                  int count = mineAction.stopMinedCount ? Math.max(1, mineAction.minedCountTarget) : 640;
                  int opTimeoutSeconds = mineAction.stopAfterTime ? Math.max(1, mineAction.timeoutSeconds) : 3600;
                  JsonObject mineArgs = new JsonObject();
                  mineArgs.addProperty("block", blockArg);
                  mineArgs.addProperty("count", count);
                  mineArgs.addProperty("timeoutSeconds", opTimeoutSeconds);
                  AiOperation op = init_AiOperation_Minecraft(mc, run, () -> autismclient.ai.a.AiMine.init(mineArgs), mineAction.h_());
                  if (op == null) {
                     AutismClientMessaging.sendPrefixed("Failed to start mining.");
                     return;
                  }

                  long mineStartMs = System.currentTimeMillis();

                  while (close_Z()) {
                     String stopReason = null;
                     int minedDelta = 0;
                     if (mineTracker != null) {
                        mineTracker.init_V_Minecraft(mc);
                        minedDelta = mineTracker.init();
                        load_V_Str("Mining: " + mineDesc + " (" + minedDelta + "/" + mineAction.minedCountTarget + ")");
                        if (minedDelta >= mineAction.minedCountTarget) {
                           stopReason = "Mine target reached";
                        }
                     }

                     if (stopReason == null && mineAction.stopInventoryFull && init_Z_Minecraft(mc)) {
                        stopReason = "Mine stop: inventory full";
                     }

                     if (stopReason == null && mineAction.stopSlotsUsed) {
                        int used = init_I_Minecraft(mc);
                        if (used >= mineAction.slotsUsedThreshold) {
                           stopReason = "Mine stop: slots used";
                        }
                     }

                     if (stopReason == null && mineAction.stopAfterTime && System.currentTimeMillis() - mineStartMs >= mineAction.timeoutSeconds * 1000L) {
                        stopReason = "Mine timeout";
                     }

                     if (stopReason != null) {
                        load_V_Str(stopReason);
                        read(run, mc);
                        return;
                     }

                     AiOperation.Result result = run != null ? run.init_AiOperation_Result : null;
                     if (result != null && result.terminal()) {
                        if (result.status() == AiOperation.Status.SUCCEEDED) {
                           load_V_Str("Mine finished");
                        } else {
                           load_V_Str("Mine stopped");
                           AutismClientMessaging.sendPrefixed("§cMine stopped: " + result.message());
                        }

                        return;
                     }

                     CompletableFuture<Void> tickFuture = MacroWaitRegistry.load_CompletableFuture();
                     init_V_CompletableFuture(tickFuture);
                  }

                  return;
               } finally {
                  if (run != null && run.init_MacroExecutor_Rend == mineTracker) {
                     run.init_MacroExecutor_Rend = null;
                  }

                  if (!close_Z()) {
                     read(run, mc);
                  } else if (run == null || run.init_AiOperation == null) {
                     sync(run);
                  }
               }
            }
         } else {
            AutismClientMessaging.sendPrefixed("No active world; skipping mine action.");
         }
      }
   }

   private static AiOperation init_AiOperation_Minecraft(Minecraft mc, MacroExecutor.Inner_TableIndex run, Supplier<AiOperation> factory, boolean tickAligned) {
      if (run != null) {
         MacroExecutor.Inner_TableIndex previousOwner = sync_AtomicReference.getAndSet(run);
         if (previousOwner != null && previousOwner != run) {
            previousOwner.init_AiOperation_Result = new AiOperation.Result(
               AiOperation.Status.CANCELLED, "Navigation superseded by another macro run", "{}", List.of()
            );
            load_V_MacroExecutor_Tabl_Minecraft(previousOwner, mc);
         }

         run.init_AiOperation_Result = null;
      }

      AiOperation op = init_Obj_Minecraft(mc, () -> {
         if (run != null) {
            AiOperation previous = run.init_AiOperation;
            if (previous != null) {
               try {
                  previous.cancel();
               } catch (Throwable var5x) {
               }

               run.init_AiOperation = null;
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
            sync_AtomicReference.compareAndSet(run, null);
         }

         return null;
      } else {
         if (run != null) {
            run.init_AiOperation = op;
         } else {
            init_CopyOnWriteArrayLi.add(op);
         }

         init_V_Minecraft_AiOperation(mc, op);
         return op;
      }
   }

   private static void load_V_MacroExecutor_Tabl_Minecraft(MacroExecutor.Inner_TableIndex run, Minecraft mc) {
      if (run != null) {
         AiOperation op = run.init_AiOperation;
         if (op != null && mc != null) {
            load_AtomicInteger.incrementAndGet();
            run.init_AiOperation = null;
            Runnable cancelTask = () -> {
               try {
                  try {
                     op.cancel();
                  } catch (Throwable var5) {
                  }

                  autismclient.ai.a.AiMotionSafety.putAt_V();
               } finally {
                  load_AtomicInteger.decrementAndGet();
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

   private static void read(MacroExecutor.Inner_TableIndex run, Minecraft mc) {
      if (run != null) {
         sync_AtomicReference.compareAndSet(run, null);
         load_V_MacroExecutor_Tabl_Minecraft(run, mc);
      }
   }

   private static void sync(MacroExecutor.Inner_TableIndex run) {
      if (run != null) {
         sync_AtomicReference.compareAndSet(run, null);
      }
   }

   static void init_V_AiOperation(AiOperation op) {
      if (op != null) {
         init_CopyOnWriteArrayLi.add(op);
      }
   }

   private static void sync(Minecraft mc) {
      MacroExecutor.Inner_TableIndex owner = sync_AtomicReference.getAndSet(null);
      if (owner != null) {
         load_V_MacroExecutor_Tabl_Minecraft(owner, mc);
      }

      init_V_Minecraft_AiOperation(mc, null);
   }

   private static void init_V_Minecraft_AiOperation(Minecraft mc, AiOperation keep) {
      if (!init_CopyOnWriteArrayLi.isEmpty()) {
         List<AiOperation> toCancel = new ArrayList<>(init_CopyOnWriteArrayLi);
         toCancel.remove(keep);
         if (!toCancel.isEmpty() && mc != null) {
            load_AtomicInteger.incrementAndGet();
            init_CopyOnWriteArrayLi.removeAll(toCancel);
            Runnable cancelTask = () -> {
               try {
                  for (AiOperation op : toCancel) {
                     try {
                        op.cancel();
                     } catch (Throwable var7) {
                     }
                  }

                  autismclient.ai.a.AiMotionSafety.putAt_V();
               } finally {
                  load_AtomicInteger.decrementAndGet();
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

   private static void tick() {
      MacroExecutor.Inner_TableIndex owner = sync_AtomicReference.get();
      if (owner != null) {
         AiOperation op = owner.init_AiOperation;
         if (op != null) {
            try {
               AiOperation.Result result = op.tick();
               if (result != null) {
                  owner.init_AiOperation_Result = result;
                  if (result.terminal()) {
                     owner.init_AiOperation = null;
                     sync_AtomicReference.compareAndSet(owner, null);
                  }
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Macro engine op tick failed.", t);

               try {
                  op.cancel();
               } catch (Throwable var7) {
               }

               owner.init_AiOperation = null;
               owner.init_AiOperation_Result = new AiOperation.Result(AiOperation.Status.FAILED, "Engine op error: " + t.getMessage(), "{}", List.of());
               sync_AtomicReference.compareAndSet(owner, null);
            }
         }
      }

      if (!init_CopyOnWriteArrayLi.isEmpty()) {
         for (AiOperation op : init_CopyOnWriteArrayLi) {
            try {
               AiOperation.Result result = op.tick();
               if (result != null && result.terminal()) {
                  init_CopyOnWriteArrayLi.remove(op);
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Background engine op tick failed.", t);

               try {
                  op.cancel();
               } catch (Throwable var5) {
               }

               init_CopyOnWriteArrayLi.remove(op);
            }
         }
      }
   }

   private static boolean init_Z_Minecraft(Minecraft mc) {
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

   private static int init_I_Minecraft(Minecraft mc) {
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

   private static void init_V_Minecraft_G(Minecraft mc, PayAction payAction) {
      payAction.delayMs = PayAction.init_I_I(payAction.delayMs);
      List<String> targets = new ArrayList<>();

      for (String player : payAction.players) {
         if (player != null) {
            MacroTemplateResolver.Inner_CachePool playerResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(player, mc);
            if (playerResolution.init_Z()) {
               String trimmed = playerResolution.init_Str().trim();
               if (!trimmed.isEmpty() && !targets.contains(trimmed)) {
                  targets.add(trimmed);
               }
            }
         }
      }

      if (targets.isEmpty()) {
         targets.addAll(OnlinePlayerCollector.init(mc, payAction.probeHidden));
      }

      if (!targets.isEmpty() && mc.getConnection() != null) {
         MacroTemplateResolver.Inner_CachePool amountResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(payAction.amountInput, mc);
         if (amountResolution.init_Z()) {
            long resolvedAmount = Math.max(0L, PayAction.init_J_Str(amountResolution.init_Str()));
            long[] dividedAmounts = payAction.divideEnabled ? PayAction.init(resolvedAmount, targets.size()) : null;
            int sent = 0;

            for (int i = 0; i < targets.size() && close_Z(); i++) {
               String player = targets.get(i);
               long perAmount = dividedAmounts != null ? dividedAmounts[i] : resolvedAmount;
               if (perAmount > 0L) {
                  String amountValue = String.valueOf(perAmount);
                  String amountLabel = PayAction.init_Str_J(perAmount);
                  String template = payAction.commandTemplate != null && !payAction.commandTemplate.isBlank()
                     ? payAction.commandTemplate
                     : "/pay <player> <amount>";
                  String command = template.replace("<player>", player)
                     .replace("{player}", player)
                     .replace("<amount>", amountValue)
                     .replace("{amount}", amountValue)
                     .trim();
                  MacroTemplateResolver.Inner_CachePool commandResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(command, mc);
                  if (commandResolution.init_Z()) {
                     command = commandResolution.init_Str().trim();
                     if (!command.isEmpty()) {
                        String commandToSend = command;
                        if (sent > 0 && payAction.delayEnabled && payAction.delayMs > 0) {
                           sleep(payAction.delayMs);
                        }

                        load_V_Str("Paying " + player + " " + amountLabel + " (" + (sent + 1) + "/" + targets.size() + ")");
                        init_V_Minecraft_Runnable(mc, () -> {
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
                           load_V_Str("Confirming " + player);
                           PayAction.init(payAction.confirmMacro, () -> !close_Z());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void read_V() {
      String macroName = init_aM != null && init_aM.init_Str != null ? init_aM.init_Str : "";

      for (MacroExecutor.Inner_TableIndex run : write_List()) {
         init_V_MacroExecutor_Tabl_Z(run, true);
      }

      synchronized (init_Obj) {
         init_LinkedHashMap.clear();
         init_ConcurrentHashMap.clear();
         close_V();
         init_MacroExecutor_Tabl = null;
         init_Z = false;
      }

      init_AtomicBoolean.set(false);

      try {
         Minecraft mcInst = Minecraft.getInstance();
         autismclient.util.ContainerCloseGuard.init(mcInst == null ? null : mcInst.getConnection());
      } catch (Throwable var8) {
      }

      load_CopyOnWriteArrayLi.clear();
      init_V_ConcurrentHashMap(load_ConcurrentHashMap, Minecraft.getInstance(), null);
      load_ConcurrentHashMap.clear();
      init_V_Minecraft(Minecraft.getInstance());
      MacroWaitRegistry.load_V();
      run();
      read_AtomicBoolean.set(false);
      load_AtomicBoolean.set(false);
      synchronized (load_Obj) {
         load_AtomicReference.set("");
         CompletableFuture<Void> pf = init_CompletableFuture;
         if (pf != null) {
            pf.cancel(true);
            init_CompletableFuture = null;
         }
      }

      CompletableFuture<Void> cf = load_CompletableFuture;
      if (cf != null) {
         cf.cancel(true);
         load_CompletableFuture = null;
      }

      sync(Minecraft.getInstance());
      autismclient.util.BlockPreviewRenderer.load();
      Minecraft mc = Minecraft.getInstance();
      PacketGateManager.init(mc == null ? null : mc.getConnection());
      MacroFpsOverrideManager.load_V();
      PingSpoofScheduler.load_V();
      MacroVariableContext.init_V();

      try {
         autismclient.util.LanSyncManager sync = autismclient.util.LanSyncManager.init_aE();
         if (sync.write_Z()) {
            sync.init_V_I(-1, 0, "");
         }
      } catch (Exception var7) {
      }

      init_Thread = null;
      init_aM = null;
      load_V_Str("");
      synchronized (update_Obj) {
         flush_AtomicReference.set(null);
         update_AtomicBoolean.set(false);
         sync_I = 0;
      }

      if (!macroName.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cMacro stopped: " + macroName);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro stopped.");
      }
   }

   public static boolean write_Z() {
      return init_AtomicBoolean.get();
   }

   public static int init_I() {
      synchronized (init_Obj) {
         return write_I();
      }
   }

   public static boolean update_Z() {
      return autismclient.modules.aD.read_Z()
         ? false
         : init_AtomicBoolean.get()
            || init_CompletableFuture != null
            || load_AtomicBoolean.get()
            || !load_List.isEmpty()
            || !read_List.isEmpty()
            || !write_List.isEmpty()
            || !update_List.isEmpty();
   }

   public static boolean init_Z_Str(String macroName) {
      if (macroName == null) {
         return false;
      }

      String targetKey = read_Str(macroName);

      for (MacroExecutor.Inner_TableIndex run : init_ConcurrentHashMap.values()) {
         if (run.init_AtomicBoolean.get() && read_Str(init_Str_aM(run.load_aM)).equals(targetKey)) {
            return true;
         }
      }

      return false;
   }

   public static boolean init_Z_aM(autismclient.util.AutismMacro macro) {
      if (macro == null) {
         return false;
      }

      for (MacroExecutor.Inner_TableIndex run : init_ConcurrentHashMap.values()) {
         if (init_Z_MacroExecutor_Tabl(run, macro, macro.init_Str)) {
            return true;
         }
      }

      return false;
   }

   public static List<MacroExecutor.Inner_UiHelper> init_List() {
      synchronized (init_Obj) {
         ArrayList<MacroExecutor.Inner_UiHelper> snapshots = new ArrayList<>(init_LinkedHashMap.size());

         for (MacroExecutor.Inner_TableIndex run : init_LinkedHashMap.values()) {
            if (run.init_AtomicBoolean.get() && !run.init_MacroExecutor_Pool.load()) {
               autismclient.util.AutismMacro macro = run.load_aM;
               String name = macro != null && macro.init_Str != null ? macro.init_Str : "";
               snapshots.add(new MacroExecutor.Inner_UiHelper(macro, name, run.read_Str == null ? "" : run.read_Str, run.init_I, run.load_I, run.read_I));
            }
         }

         return snapshots;
      }
   }

   public static void write_V() {
      if (init_ThreadLocal.get() == null && run <= 0) {
         update_J = System.currentTimeMillis();
      }
   }

   public static void init_V_Runnable(Runnable leave) {
      run++;

      try {
         leave.run();
      } finally {
         run--;
      }
   }

   public static void init_V_Z(boolean stopOnLeave) {
      long quitAt = update_J;
      update_J = 0L;
      boolean playerLeft = quitAt > 0L && System.currentTimeMillis() - quitAt < 10000L;
      boolean anyKept = false;

      for (MacroExecutor.Inner_TableIndex run : write_List()) {
         if (run.init_AtomicBoolean.get()) {
            boolean rejoins = init_Z_MacroExecutor_Tabl(run);
            if (!JoinAction.init(playerLeft, stopOnLeave, rejoins)) {
               init_V_MacroExecutor_Tabl_Z(run, true);
            } else {
               anyKept = true;
               if (rejoins) {
                  PacketGateManager.init("all", run.init_J);
                  PingSpoofScheduler.init(run.init_J);
               }
            }
         }
      }

      if (!anyKept && stopOnLeave) {
         read_V();
      } else {
         synchronized (init_Obj) {
            poll_V();
         }
      }
   }

   public static boolean sync_Z() {
      for (MacroExecutor.Inner_TableIndex run : init_List) {
         if (run.init_AtomicBoolean.get() && run.init_Z) {
            return true;
         }
      }

      return false;
   }

   private static boolean init_Z_MacroExecutor_Tabl(MacroExecutor.Inner_TableIndex run) {
      return run != null && run.load_aM != null && JoinAction.init(run.load_aM.init_List, run.init_I, run.load_aM.init_Z);
   }

   public static void init_V_Str(String macroName) {
      if (macroName != null && !macroName.isBlank()) {
         String targetKey = read_Str(macroName);
         boolean stopped = false;

         for (MacroExecutor.Inner_TableIndex run : write_List()) {
            if (run.init_AtomicBoolean.get() && read_Str(init_Str_aM(run.load_aM)).equals(targetKey)) {
               init_V_MacroExecutor_Tabl_Z(run, true);
               stopped = true;
            }
         }

         if (stopped) {
            synchronized (init_Obj) {
               poll_V();
            }

            AutismClientMessaging.sendPrefixed("§cMacro stopped: " + macroName);
         }
      }
   }

   public static void load_V_aM(autismclient.util.AutismMacro macro) {
      if (macro != null) {
         boolean stopped = false;
         String displayName = init_Str_aM(macro);

         for (MacroExecutor.Inner_TableIndex run : write_List()) {
            if (init_Z_MacroExecutor_Tabl(run, macro, displayName)) {
               init_V_MacroExecutor_Tabl_Z(run, true);
               stopped = true;
            }
         }

         if (stopped) {
            synchronized (init_Obj) {
               poll_V();
            }

            AutismClientMessaging.sendPrefixed("§cMacro stopped: " + (displayName.isBlank() ? "macro" : displayName));
         }
      }
   }

   public static String init_Str() {
      autismclient.util.AutismMacro m = init_aM;
      return m != null && m.init_Str != null ? m.init_Str : "";
   }

   public static autismclient.util.AutismMacro init_aM() {
      return init_aM;
   }

   public static String load_Str() {
      MacroExecutor.Inner_TableIndex run = init_MacroExecutor_Tabl;
      return run != null ? run.read_Str : init_Str;
   }

   public static int load_I() {
      MacroExecutor.Inner_TableIndex run = init_MacroExecutor_Tabl;
      return run != null ? run.init_I : load_I;
   }

   public static int read_I() {
      MacroExecutor.Inner_TableIndex run = init_MacroExecutor_Tabl;
      return run != null ? run.load_I : read_I;
   }

   public static List<MacroExecutor.Inner_PacketUtil> load_List() {
      synchronized (read_Obj) {
         return new ArrayList<>(init_Deque);
      }
   }

   public static List<MacroExecutor.Inner_HashUtil> read_List() {
      synchronized (write_Obj) {
         return new ArrayList<>(load_Deque);
      }
   }

   public static void init_V_Screen(Screen screen) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.level == null) {
         update_V();
      } else if (screen != null && !GuiScreenMatcher.init_Z(screen)) {
         String title = screen.getTitle() == null ? "" : screen.getTitle().getString().trim();
         if (!title.isEmpty()) {
            String type = GuiScreenMatcher.init_Str(screen);
            MacroExecutor.Inner_HashUtil entry = new MacroExecutor.Inner_HashUtil(title, type == null ? "" : type, System.currentTimeMillis());
            synchronized (write_Obj) {
               load_Deque.removeIf(existing -> existing.init_Str().equals(entry.init_Str()) && existing.load().equals(entry.load()));
               load_Deque.addFirst(entry);

               while (load_Deque.size() > 128) {
                  load_Deque.removeLast();
               }
            }
         }
      }
   }

   public static void update_V() {
      synchronized (read_Obj) {
         init_Deque.clear();
         load_Str = "";
         write_Str = "";
      }

      synchronized (write_Obj) {
         load_Deque.clear();
      }
   }

   public static MacroExecutor.Inner_EventBus init_MacroExecutor_Even_Predicate(Predicate<Packet<?>> match) {
      return new MacroExecutor.Inner_EventBus(load_List, match);
   }

   public static MacroExecutor.Inner_EventBus load_MacroExecutor_Even_Predicate(Predicate<Packet<?>> match) {
      return new MacroExecutor.Inner_EventBus(read_List, match);
   }

   private static MacroExecutor.Inner_FieldCache init_MacroExecutor_Fiel_MacroExecutor_Meth(MacroExecutor.Inner_MethodUtil match) {
      return new MacroExecutor.Inner_FieldCache(write_List, match);
   }

   private static MacroExecutor.Inner_FieldCache init_MacroExecutor_Fiel_Predicate(Predicate<Packet<?>> match) {
      return load_MacroExecutor_Fiel_MacroExecutor_Meth((packet, direction, nanoTime) -> match.test(packet));
   }

   private static MacroExecutor.Inner_FieldCache load_MacroExecutor_Fiel_MacroExecutor_Meth(MacroExecutor.Inner_MethodUtil match) {
      return new MacroExecutor.Inner_FieldCache(update_List, match);
   }

   private static void init_V_List_Packet(List<MacroExecutor.Inner_EventBus> list, Packet<?> packet) {
      if (!list.isEmpty()) {
         long now = System.nanoTime();
         ArrayList<MacroExecutor.Inner_EventBus> matched = null;

         for (MacroExecutor.Inner_EventBus listener : list) {
            boolean drop;
            try {
               drop = listener.init(packet, now);
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

   private static void init_V_List_MacroExecutor_Sync(List<MacroExecutor.Inner_FieldCache> list, MacroExecutor.Inner_SyncManager event) {
      if (!list.isEmpty()) {
         ArrayList<MacroExecutor.Inner_FieldCache> matched = null;

         for (MacroExecutor.Inner_FieldCache listener : list) {
            boolean drop;
            try {
               drop = listener.init(event);
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

   private static void run() {
      for (MacroExecutor.Inner_EventBus listener : load_List) {
         listener.init_V();
      }

      for (MacroExecutor.Inner_EventBus listener : read_List) {
         listener.init_V();
      }

      for (MacroExecutor.Inner_FieldCache listener : write_List) {
         listener.init();
      }

      for (MacroExecutor.Inner_FieldCache listener : update_List) {
         listener.init();
      }
   }

   public static String read_Str() {
      return "runs="
         + init_I()
         + " recv="
         + load_List.size()
         + " send="
         + read_List.size()
         + " timedRecv="
         + write_List.size()
         + " timedSend="
         + update_List.size();
   }

   public static void init_V_Packet(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         if (!update_List.isEmpty()) {
            init_V_List_MacroExecutor_Sync(update_List, new MacroExecutor.Inner_SyncManager(packet, "C2S", System.nanoTime(), null));
         }

         init_V_List_Packet(read_List, packet);
         init_V_Packet_Str(packet, "C2S");
      }
   }

   public static void load_V_Packet(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         if (!write_List.isEmpty()) {
            init_V_List_MacroExecutor_Sync(write_List, new MacroExecutor.Inner_SyncManager(packet, "S2C", System.nanoTime(), null));
         }

         init_V_List_Packet(load_List, packet);
         if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
            MacroWaitRegistry.read_V();
         }

         if (packet instanceof ClientboundPlayerPositionPacket) {
            MacroWaitRegistry.write();
         }

         init_V_Packet_Str(packet, "S2C");
      }
   }

   public static void init_V_Packet_Str(Packet<?> packet, String direction) {
      if (!autismclient.modules.aD.read_Z()) {
         get();
         List<MacroExecutor.Inner_TableIndex> runs = write_List();
         boolean s2c = "S2C".equals(direction);
         boolean chatWork = false;
         boolean mineWork = false;
         boolean packetWork = false;

         for (int i = 0; i < runs.size(); i++) {
            MacroExecutor.Inner_TableIndex run = runs.get(i);
            if (run.init_AtomicBoolean.get()) {
               if (run.write_AtomicBoolean.get()) {
                  chatWork = true;
               }

               if (run.init_MacroExecutor_Rend != null) {
                  mineWork = true;
               }

               if (run.read_AtomicBoolean.get()) {
                  packetWork = true;
               }
            }
         }

         if (s2c && (chatWork || read_AtomicBoolean.get())) {
            MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
            if (capture != null && capture.load() != null && !capture.load().isBlank()) {
               for (MacroExecutor.Inner_TableIndex run : runs) {
                  Optional<Map<String, MacroVarValue>> matched = run.init_AtomicBoolean.get() && run.write_AtomicBoolean.get()
                     ? init_Optional_au(run.init_au, capture, run.init_m)
                     : Optional.empty();
                  if (matched.isPresent()) {
                     run.load_Map.put(run.init_au, matched.get());
                     CompletableFuture<Void> f = run.load_CompletableFuture;
                     if (f != null) {
                        f.complete(null);
                     }
                  }
               }

               if (close_Z() && read_AtomicBoolean.get() && init_Z_MacroExecutor_Conf(capture)) {
                  update_AtomicReference.set(capture.read());
                  CompletableFuture<Void> f = load_CompletableFuture;
                  if (f != null) {
                     f.complete(null);
                  }
               }
            }
         }

         if (s2c && mineWork) {
            Minecraft mc = Minecraft.getInstance();

            for (MacroExecutor.Inner_TableIndex run : runs) {
               MacroExecutor.Inner_RenderState tracker = run.init_AtomicBoolean.get() ? run.init_MacroExecutor_Rend : null;
               if (tracker != null) {
                  tracker.init_V_Minecraft_Packet(mc, packet);
               }
            }
         }

         if (close_Z()) {
            if (packetWork) {
               for (MacroExecutor.Inner_TableIndex run : runs) {
                  if (run.init_AtomicBoolean.get() && run.read_AtomicBoolean.get()) {
                     CompletableFuture<Void> f = null;
                     synchronized (run.init_Obj) {
                        if (run.read_AtomicBoolean.get() && init_Z_Str_2(run.init_AtomicReference.get(), packet, direction)) {
                           run.init_Packet = packet;
                           run.load_Str = direction;
                           f = run.init_CompletableFuture;
                        }
                     }

                     if (f != null) {
                        f.complete(null);
                     }
                  }
               }
            }

            if (load_AtomicBoolean.get()) {
               CompletableFuture<Void> f = null;
               synchronized (load_Obj) {
                  if (!load_AtomicBoolean.get()) {
                     return;
                  }

                  String target = load_AtomicReference.get();
                  if (init_Z_Str_2(target, packet, direction)) {
                     init_AtomicReference.set(direction + " " + PacketNameResolver.init_Str_Packet_Str(packet, direction));
                     f = init_CompletableFuture;
                  }
               }

               if (f != null) {
                  f.complete(null);
               }
            }
         }
      }
   }

   public static void read(Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z()) {
         MacroExecutor.Inner_ConfigStore capture = init_MacroExecutor_Conf_Packet(packet);
         if (capture != null && capture.load() != null && !capture.load().isBlank()) {
            get();
            init_V_MacroExecutor_Conf(capture);
         }
      }
   }

   private static void init_V_MacroExecutor_Conf(MacroExecutor.Inner_ConfigStore capture) {
      MacroExecutor.Inner_PacketUtil message = new MacroExecutor.Inner_PacketUtil(
         capture.init_Str() == null ? "" : capture.init_Str(),
         capture.load(),
         capture.read(),
         capture.init_Component(),
         capture.init_MacroExecutor_Task(),
         System.currentTimeMillis()
      );
      synchronized (read_Obj) {
         init_Deque.removeIf(existing -> existing.read().equals(message.read()));
         init_Deque.addFirst(message);

         while (init_Deque.size() > 256) {
            init_Deque.removeLast();
         }
      }
   }

   private static void get() {
      String currentServerKey = write_Str();
      if (!currentServerKey.isEmpty() && !currentServerKey.equals(write_Str)) {
         synchronized (read_Obj) {
            if (load_Str.isEmpty()) {
               load_Str = currentServerKey;
               write_Str = currentServerKey;
            } else if (!load_Str.equals(currentServerKey)) {
               init_Deque.clear();
               load_Str = currentServerKey;
               write_Str = currentServerKey;
               synchronized (write_Obj) {
                  load_Deque.clear();
               }
            } else {
               write_Str = currentServerKey;
            }
         }
      }
   }

   private static String write_Str() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && !mc.isLocalServer()) {
         ServerData entry = mc.getCurrentServer();
         if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
            return sync(entry.ip);
         }

         if (mc.getConnection() != null && mc.getConnection().getConnection() != null) {
            SocketAddress address = mc.getConnection().getConnection().getRemoteAddress();
            if (address instanceof InetSocketAddress inet) {
               String host = inet.getHostString();
               if ((host == null || host.isBlank()) && inet.getAddress() != null) {
                  host = inet.getAddress().getHostAddress();
               }

               if (host != null && !host.isBlank()) {
                  return sync(host + ":" + inet.getPort());
               }
            } else if (address != null) {
               String raw = address.toString();
               if (raw != null && !raw.isBlank()) {
                  return sync(raw);
               }
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static String sync(String address) {
      if (address == null) {
         return "";
      }

      String stripped = address.startsWith("/") ? address.substring(1) : address;
      return stripped.trim().toLowerCase(Locale.ROOT);
   }

   private static boolean init_Z_MacroExecutor_Conf(MacroExecutor.Inner_ConfigStore capture) {
      String patternJson = write_AtomicReference.get();
      if (!write_AtomicBoolean.get() && patternJson != null && !patternJson.isBlank() && capture.init_Component() != null) {
         String captureJson = init_Str_Component(capture.init_Component());
         if (patternJson.equals(captureJson)) {
            return true;
         }
      }

      String pattern = init_Str_Str(read_AtomicReference.get());
      if (pattern == null || pattern.isBlank()) {
         return true;
      }

      if (write_AtomicBoolean.get()) {
         try {
            Pattern compiled = PatternCaptureMatcher.init_Pattern(pattern);
            return compiled.matcher(capture.load()).matches() || compiled.matcher(capture.read()).matches();
         } catch (Exception ignored) {
            return false;
         }
      } else {
         int threshold = init_AtomicInteger.get();
         return init_Z_Str(pattern, capture.load(), threshold) || init_Z_Str(pattern, capture.read(), threshold);
      }
   }

   private static boolean init_Z_au_MacroExecutor_Conf(WaitForChatAction action, MacroExecutor.Inner_ConfigStore capture) {
      return init_Optional_au(action, capture, init_m()).isPresent();
   }

   private static Optional<Map<String, MacroVarValue>> init_Optional_au_MacroExecutor_Conf(WaitForChatAction action, MacroExecutor.Inner_ConfigStore capture) {
      return init_Optional_au(action, capture, init_m());
   }

   private static Optional<Map<String, MacroVarValue>> init_Optional_au(WaitForChatAction action, MacroExecutor.Inner_ConfigStore capture, MacroVariableStore context) {
      if (action != null && capture != null) {
         PatternCaptureMatcher.Inner_DataUtil mode = action.init_UtilCore_DataUtil();
         if (mode == PatternCaptureMatcher.Inner_DataUtil.MATCH && action.patternJson != null && !action.patternJson.isBlank() && capture.init_Component() != null) {
            String captureJson = init_Str_Component(capture.init_Component());
            if (action.patternJson.equals(captureJson)) {
               return Optional.of(init_Map_au(action, capture, Map.of()));
            }
         }

         String pattern = init_Str_Str(action.pattern);
         if (mode == PatternCaptureMatcher.Inner_DataUtil.MATCH) {
            MacroTemplateResolver.Inner_CachePool patternResolution = MacroTemplateResolver.init_j_CachePool(pattern, context, Minecraft.getInstance());
            if (!patternResolution.init_Z()) {
               return Optional.empty();
            }

            pattern = patternResolution.init_Str();
         }

         if (pattern == null || pattern.isBlank()) {
            return Optional.of(init_Map_au(action, capture, Map.of()));
         } else if (mode != PatternCaptureMatcher.Inner_DataUtil.MATCH) {
            Optional<PatternCaptureMatcher.Inner_StreamUtil> messageResult = PatternCaptureMatcher.init(mode, pattern, capture.load());
            Optional<PatternCaptureMatcher.Inner_StreamUtil> result = messageResult.isPresent() ? messageResult : PatternCaptureMatcher.init(mode, pattern, capture.read());
            return result.map(value -> init_Map_au(action, capture, value.init_Map()));
         } else {
            int threshold = WaitForChatAction.init_I_I_8(action.fuzzyPercent);
            boolean matched = init_Z_Str(pattern, capture.load(), threshold) || init_Z_Str(pattern, capture.read(), threshold);
            return matched ? Optional.of(init_Map_au(action, capture, Map.of())) : Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static Map<String, MacroVarValue> init_Map_au(WaitForChatAction action, MacroExecutor.Inner_ConfigStore capture, Map<String, MacroVarValue> captures) {
      Map<String, MacroVarValue> values = new LinkedHashMap<>(captures);
      if (action.saveAs != null && !action.saveAs.isBlank()) {
         Map<String, MacroVarValue> properties = new LinkedHashMap<>();
         properties.put("sender", MacroVarValue.init_l_Obj(capture.init_Str()));
         properties.put("message", MacroVarValue.init_l_Obj(capture.load()));
         properties.put("display", MacroVarValue.init_l_Obj(capture.read()));
         properties.put("source", MacroVarValue.init_l_Obj(capture.init_MacroExecutor_Task().name()));
         values.put(action.saveAs, MacroVarValue.init(MacroVarValue.Inner_DataUtil.TEXT, capture.read(), properties));
      }

      return Map.copyOf(values);
   }

   private static MacroExecutor.Inner_ConfigStore init_MacroExecutor_Conf_Packet(Packet<?> packet) {
      if (packet instanceof ClientboundSystemChatPacket systemChat) {
         Component content = systemChat.content();
         if (content == null) {
            return null;
         }

         String raw = content.getString();
         return raw != null && !raw.isBlank() ? init_MacroExecutor_Conf_Str("", raw, raw, content, MacroExecutor.Inner_TaskQueue.PLAYER) : null;
      } else if (packet instanceof ClientboundDisguisedChatPacket disguisedChat) {
         Component content = disguisedChat.message();
         String text = content == null ? null : content.getString();
         if (text != null && !text.isBlank()) {
            Component decorated = disguisedChat.chatType().decorate(content);
            String sender = disguisedChat.chatType().name().getString();
            return init_MacroExecutor_Conf_Str(sender, text, null, decorated, MacroExecutor.Inner_TaskQueue.PLAYER);
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
               String byUuid = init_Str_UUID(playerChat.sender());
               if (byUuid != null && !byUuid.isBlank()) {
                  sender = byUuid;
               }
            }

            return init_MacroExecutor_Conf_Str(sender, message, null, decorated, MacroExecutor.Inner_TaskQueue.PLAYER);
         } else {
            return null;
         }
      }
   }

   private static MacroExecutor.Inner_ConfigStore init_MacroExecutor_Conf_Str(
      String sender, String message, String displayText, Component displayComponent, MacroExecutor.Inner_TaskQueue source
   ) {
      String cleanMessage = flush(message);
      if (cleanMessage.isBlank()) {
         return null;
      }

      String cleanSender = flush(sender);
      String cleanDisplay = flush(displayText);
      Component rendered = init_Component_Component(displayComponent);
      String display = cleanDisplay;
      if (rendered == null || rendered.getString().isBlank()) {
         rendered = cleanSender.isBlank()
            ? Component.literal(cleanMessage)
            : Component.literal("<" + cleanSender + "> ").append(Component.literal(cleanMessage));
      }

      if (display == null || display.isBlank()) {
         display = flush(rendered.getString());
      }

      MacroExecutor.Inner_TaskQueue resolvedSource = source != null ? source : MacroExecutor.Inner_TaskQueue.PLAYER;
      return new MacroExecutor.Inner_ConfigStore(cleanSender, cleanMessage, flush(display), rendered, resolvedSource);
   }

   private static String flush(String text) {
      if (text == null) {
         return "";
      }

      String stripped = open(text).replace('\n', ' ').replace('\r', ' ');
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

   public static String init_Str_Str(String text) {
      if (text != null && !text.isBlank()) {
         String stripped = open(text).trim();
         String flattened = close(stripped);
         return flush(flattened != null ? flattened : stripped);
      } else {
         return "";
      }
   }

   private static String close(String text) {
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
                     Component component = init_Component_JsonElement(child);
                     if (component != null) {
                        out.append(component.getString());
                     }
                  }

                  return out.toString();
               } else {
                  Component component = init_Component_JsonElement(element);
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

   private static Component init_Component_JsonElement(JsonElement element) {
      try {
         Component component = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
         return init_Component_Component(component);
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   private static String open(String text) {
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

   private static boolean load_Z_Str(String sender) {
      String cleanSender = flush(sender);
      if (cleanSender.isBlank()) {
         return false;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.getConnection() != null) {
            for (PlayerInfo entry : mc.getConnection().getListedOnlinePlayers()) {
               String playerName = entry != null && entry.getProfile() != null ? load_Str_Obj(entry.getProfile()) : null;
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
               String playerName = player != null && player.getGameProfile() != null ? load_Str_Obj(player.getGameProfile()) : null;
               if (playerName != null && cleanSender.equalsIgnoreCase(playerName)) {
                  return true;
               }
            }
         }
      } catch (Throwable var6) {
      }

      return false;
   }

   public static boolean init_Z_Str(String pattern, String candidate, int thresholdPercent) {
      String normalizedPattern = load_Str_Str(pattern);
      String normalizedCandidate = load_Str_Str(candidate);
      if (normalizedPattern.isBlank()) {
         return true;
      }

      if (normalizedCandidate.isBlank()) {
         return false;
      }

      if (normalizedCandidate.contains(normalizedPattern)) {
         return true;
      }

      List<String> patternTokens = init_List_Str(normalizedPattern);
      List<String> candidateTokens = init_List_Str(normalizedCandidate);
      if (patternTokens.isEmpty()) {
         return true;
      }

      if (thresholdPercent >= 100) {
         return init_Z_List_Str(patternTokens, normalizedCandidate);
      }

      double tokenScore = init_D_List_List(patternTokens, candidateTokens);
      double charScore = load_D_Str_Str(normalizedPattern.replace(" ", ""), normalizedCandidate.replace(" ", ""));
      double score = Math.max(tokenScore, tokenScore * 0.7 + charScore * 0.3);
      return score + 1.0E-9 >= Math.max(40, Math.min(100, thresholdPercent)) / 100.0;
   }

   private static String poll(String text) {
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

   public static String load_Str_Str(String text) {
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
         cp = init_I_I(cp);
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
            } else if (!load_Z_I(cp)) {
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

   private static boolean load_Z_I(int cp) {
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

   private static int init_I_I(int cp) {
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

   private static List<String> init_List_Str(String normalized) {
      return normalized != null && !normalized.isBlank() ? List.of(normalized.split(" ")) : List.of();
   }

   private static boolean init_Z_List_Str(List<String> patternTokens, String normalizedCandidate) {
      for (String token : patternTokens) {
         if (!normalizedCandidate.contains(token)) {
            return false;
         }
      }

      return true;
   }

   private static double init_D_List_List(List<String> patternTokens, List<String> candidateTokens) {
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
            best = Math.max(best, init_D_Str_Str(patternToken, candidateToken));
            if (best >= 1.0) {
               break;
            }
         }

         total += best;
      }

      return total / patternTokens.size();
   }

   private static double init_D_Str_Str(String left, String right) {
      if (left.equals(right)) {
         return 1.0;
      } else if (!left.isBlank() && !right.isBlank()) {
         return !left.contains(right) && !right.contains(left)
            ? load_D_Str_Str(left, right)
            : (double)Math.min(left.length(), right.length()) / Math.max(left.length(), right.length());
      } else {
         return 0.0;
      }
   }

   private static double load_D_Str_Str(String left, String right) {
      if (left.equals(right)) {
         return 1.0;
      }

      int maxLen = Math.max(left.length(), right.length());
      if (maxLen == 0) {
         return 1.0;
      }

      int distance = init_I_Str_Str(left, right);
      return Math.max(0.0, 1.0 - (double)distance / maxLen);
   }

   private static int init_I_Str_Str(String left, String right) {
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

   private static String init_Str_Obj(Object value) {
      Object direct = init_Obj_ObjArr(
         init_Obj_Obj_StrArr(value, "name", "getName", "targetName", "getTargetName"),
         init_Obj_Obj_I(value, 0),
         init_Obj_Obj(value, 2, "name", "targetName", "type")
      );
      return flush(load_Str_Obj(direct));
   }

   private static String init_Str_UUID(UUID uuid) {
      if (uuid == null) {
         return "";
      }

      try {
         if (Minecraft.getInstance().getConnection() != null) {
            PlayerInfo entry = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
            if (entry != null && entry.getProfile() != null) {
               String profileName = load_Str_Obj(entry.getProfile());
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
                  String profileName = load_Str_Obj(player.getGameProfile());
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

   private static Object init_Obj_ObjArr(Object... values) {
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

   private static Object init_Obj_Obj_StrArr(Object target, String... names) {
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

   private static Object init_Obj_Obj_I(Object target, int index) {
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

   private static Object init_Obj_Obj_Class(Object target, Class<?> type) {
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

   private static Object init_Obj_Obj(Object target, int fallbackIndex, String... names) {
      Object direct = init_Obj_Obj_StrArr(target, names);
      return direct != null ? direct : init_Obj_Obj_I(target, fallbackIndex);
   }

   private static Object init_Obj_Obj_ObjArr(Object target, Object... excludedValues) {
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

   private static Component init_Component_Obj(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof Optional<?> optional) {
         return optional.isPresent() ? init_Component_Obj(optional.get()) : null;
      } else if (value instanceof Component text) {
         return init_Component_Component(text);
      } else if (value instanceof CharSequence chars) {
         return Component.literal(chars.toString());
      } else if (value instanceof UUID uuid) {
         return Component.literal(init_Str_UUID(uuid));
      } else {
         try {
            Method getComponent = value.getClass().getMethod("getText");
            getComponent.setAccessible(true);
            Object result = getComponent.invoke(value);
            Component text = init_Component_Obj(result);
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

   public static Component init_Component_Component(Component text) {
      if (text == null) {
         return null;
      }

      MutableComponent literalized = Component.empty();

      try {
         text.visit((style, string) -> {
            if (string != null && !string.isEmpty()) {
               literalized.append(Component.literal(string).setStyle(init_Style_Style(style)));
            }

            return Optional.empty();
         }, Style.EMPTY);
      } catch (Throwable var3) {
      }

      return !literalized.getString().isEmpty() ? literalized : Component.literal(text.getString());
   }

   private static Style init_Style_Style(Style style) {
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

   public static String init_Str_Component(Component text) {
      if (text == null) {
         return "";
      }

      try {
         Component safeComponent = init_Component_Component(text);
         return safeComponent == null ? "" : init_Gson.toJson(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, safeComponent).getOrThrow());
      } catch (Throwable ignored) {
         return "";
      }
   }

   public static Component init_Component_Str(String json) {
      if (json != null && !json.isBlank()) {
         try {
            return init_Component_Component(ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().copy());
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String load_Str_Obj(Object value) {
      if (value == null) {
         return null;
      } else if (value instanceof Optional<?> optional) {
         return optional.isPresent() ? load_Str_Obj(optional.get()) : null;
      } else if (value instanceof Component text) {
         return text.getString();
      } else if (value instanceof CharSequence chars) {
         return chars.toString();
      } else if (value instanceof UUID uuid) {
         return init_Str_UUID(uuid);
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

   public static void init_V_F(float tickDelta) {
      if (update_AtomicBoolean.get()) {
         MacroExecutor.Inner_TableIndex owner = flush_AtomicReference.get();
         if (owner != null && !owner.init_AtomicBoolean.get()) {
            flush(owner);
         } else if (owner == null && !init_AtomicBoolean.get()) {
            synchronized (update_Obj) {
               if (flush_AtomicReference.get() == null) {
                  update_AtomicBoolean.set(false);
               }
            }
         } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
               float currentYaw = mc.player.getYRot();
               float currentPitch = mc.player.getXRot();
               double deltaYaw = Mth.wrapDegrees(init_F - currentYaw);
               double deltaPitch = load_F - currentPitch;
               double diagonalDistance = Math.hypot(deltaYaw, deltaPitch);
               if (diagonalDistance <= 0.35) {
                  mc.player.setYRot(init_F);
                  mc.player.setXRot(load_F);
                  sync_I++;
                  if (sync_I >= 2) {
                     synchronized (update_Obj) {
                        if (flush_AtomicReference.get() == owner) {
                           update_AtomicBoolean.set(false);
                           flush_AtomicReference.set(null);
                        }
                     }
                  }
               } else {
                  sync_I = 0;
                  double frameStep = Math.min(diagonalDistance, init_D * Math.max(tickDelta, 1.0F));
                  double stepYaw = deltaYaw / diagonalDistance * frameStep;
                  double stepPitch = deltaPitch / diagonalDistance * frameStep;
                  mc.player.setYRot(currentYaw + (float)stepYaw);
                  mc.player.setXRot(currentPitch + (float)stepPitch);
               }
            }
         }
      }
   }

   public static boolean flush_Z() {
      return update_AtomicBoolean.get();
   }

   private static void init_V_F(float yaw, float pitch, double speed) {
      MacroExecutor.Inner_TableIndex run;
      for (run = init_ThreadLocal.get(); run != null && run.init_AtomicBoolean.get(); LockSupport.parkNanos(500000L)) {
         synchronized (update_Obj) {
            MacroExecutor.Inner_TableIndex owner = flush_AtomicReference.get();
            if (owner == null || owner == run || !owner.init_AtomicBoolean.get()) {
               flush_AtomicReference.set(run);
               init_F = yaw;
               load_F = pitch;
               init_D = speed;
               sync_I = 0;
               update_AtomicBoolean.set(true);
               return;
            }
         }
      }

      if (run == null) {
         synchronized (update_Obj) {
            if (flush_AtomicReference.get() != null) {
               return;
            }

            init_F = yaw;
            load_F = pitch;
            init_D = speed;
            sync_I = 0;
            update_AtomicBoolean.set(true);
         }
      }
   }

   private static void set() {
      MacroExecutor.Inner_TableIndex run = init_ThreadLocal.get();

      while (close_Z() && update_AtomicBoolean.get() && (run == null || flush_AtomicReference.get() == run)) {
         LockSupport.parkNanos(1000000L);
      }
   }

   private static void flush(MacroExecutor.Inner_TableIndex run) {
      if (run != null) {
         synchronized (update_Obj) {
            if (flush_AtomicReference.get() == run) {
               update_AtomicBoolean.set(false);
               sync_I = 0;
               flush_AtomicReference.set(null);
            }
         }
      }
   }

   private static LookAtBlockAction.Inner_DataUtil init_ValueSink_DataUtil_Minecraft_ValueSink(Minecraft mc, LookAtBlockAction action) {
      if (mc != null && action != null) {
         AtomicReference<LookAtBlockAction.Inner_DataUtil> targetRef = new AtomicReference<>();
         CountDownLatch latch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               targetRef.set(action.init_ValueSink_DataUtil_Minecraft(mc));
            } finally {
               latch.countDown();
            }
         });

         while (close_Z() && !awaitLatch(latch, 10L)) {
         }

         return targetRef.get();
      } else {
         return null;
      }
   }

   public static void sync_V() {
      read_V();
   }

   private static void init_V_Minecraft_List(Minecraft mc, List<SendPacketAction> batch) {
      if (mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("§cNo network connection for burst!");
      } else {
         List<AutismSessionState.Inner_CachePool> allQueuedPackets = new ArrayList<>();

         for (SendPacketAction action : batch) {
            int repeats = SendPacketAction.init_I_I_5(action.sendCount);

            for (int k = 0; k < repeats; k++) {
               allQueuedPackets.addAll(action.init_List_8());
            }
         }

         if (!allQueuedPackets.isEmpty()) {
            List<Packet<?>> regeneratedPackets = new ArrayList<>(allQueuedPackets.size());

            for (AutismSessionState.Inner_CachePool qp : allQueuedPackets) {
               Packet<?> resolved = SendPacketAction.init_Packet_ck_CachePool(qp);
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

   private static List<AutismSessionState.Inner_CachePool> init_List_aM(autismclient.util.AutismMacro macro, int startIdx, int count) {
      new ArrayList();
      int maxIdx = count < 0 ? macro.init_List.size() : Math.min(startIdx + count, macro.init_List.size());
      return init_List_aM(macro, startIdx, count, maxIdx);
   }

   private static List<AutismSessionState.Inner_CachePool> init_List_aM(autismclient.util.AutismMacro macro, int startIdx, int count, int endIdx) {
      List<AutismSessionState.Inner_CachePool> result = new ArrayList<>();
      int maxIdx = Math.min(endIdx, count < 0 ? macro.init_List.size() : Math.min(startIdx + count, macro.init_List.size()));

      for (int j = startIdx; j < maxIdx; j++) {
         MacroAction act = macro.init_List.get(j);
         if (!(act instanceof SendPacketAction spa)) {
            break;
         }

         int repeats = SendPacketAction.init_I_I_5(spa.sendCount);

         for (AutismSessionState.Inner_CachePool qp : spa.init_List_8()) {
            if (qp.init_Packet != null) {
               for (int k = 0; k < repeats; k++) {
                  result.add(qp);
               }
            }
         }
      }

      return result;
   }

   private static void load_V_Minecraft_List(Minecraft mc, List<AutismSessionState.Inner_CachePool> packets) {
      if (!packets.isEmpty() && mc.getConnection() != null) {
         ClientPacketListener handler = mc.getConnection();

         for (AutismSessionState.Inner_CachePool qp : packets) {
            Packet<?> toSend = SendPacketAction.init_Packet_ck_CachePool(qp);
            if (toSend != null) {
               handler.send(toSend);
            }
         }

         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         if (module.start_Z()) {
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

   private static int load_I_aM(autismclient.util.AutismMacro macro, int startIdx, int packetCount) {
      return init_I_aM(macro, startIdx, packetCount, macro.init_List.size());
   }

   private static int init_I_aM(autismclient.util.AutismMacro macro, int startIdx, int packetCount, int endIdx) {
      int actionsToSkip = 0;
      int packetsRemaining = packetCount;

      for (int j = startIdx; j < endIdx && packetsRemaining > 0; j++) {
         MacroAction act = macro.init_List.get(j);
         if (!(act instanceof SendPacketAction spa)) {
            break;
         }

         int actionPackets = spa.init_List_8().size() * SendPacketAction.init_I_I_5(spa.sendCount);
         packetsRemaining -= actionPackets;
         actionsToSkip++;
      }

      return actionsToSkip;
   }

   static {
      for (MacroResourceClassifier.Inner_DataUtil resource : MacroResourceClassifier.Inner_DataUtil.values()) {
         init_EnumMap.put(resource, new ReentrantLock(true));
      }

      init_AtomicLong = new AtomicLong(1L);
      init_LinkedHashMap = new LinkedHashMap<>();
      init_ConcurrentHashMap = new ConcurrentHashMap<>();
      load_AtomicLong = new AtomicLong();
      init_ThreadLocal = new ThreadLocal<>();
      load_ThreadLocal = ThreadLocal.withInitial(() -> false);
      init_MacroExecutor_Tabl = null;
      init_aM = null;
      init_Thread = null;
      init_AtomicReference = new AtomicReference<>("");
      init_Str = "";
      load_I = -1;
      read_I = 0;
      load_AtomicBoolean = new AtomicBoolean(false);
      load_AtomicReference = new AtomicReference<>("");
      load_Obj = new Object();
      read_AtomicBoolean = new AtomicBoolean(false);
      read_AtomicReference = new AtomicReference<>("");
      write_AtomicBoolean = new AtomicBoolean(false);
      write_AtomicReference = new AtomicReference<>("");
      init_AtomicInteger = new AtomicInteger(100);
      update_AtomicReference = new AtomicReference<>("");
      read_Obj = new Object();
      init_Deque = new ArrayDeque<>();
      write_Obj = new Object();
      load_Deque = new ArrayDeque<>();
      load_Str = "";
      init_CompletableFuture = null;
      load_CompletableFuture = null;
      sync_AtomicReference = new AtomicReference<>();
      init_CopyOnWriteArrayLi = new CopyOnWriteArrayList<>();
      update_AtomicBoolean = new AtomicBoolean(false);
      flush_AtomicReference = new AtomicReference<>();
      update_Obj = new Object();
      load_CopyOnWriteArrayLi = new CopyOnWriteArrayList<>();
      load_ConcurrentHashMap = new ConcurrentHashMap<>();
      read_ThreadLocal = new ThreadLocal<>();
      write_ThreadLocal = ThreadLocal.withInitial(() -> 0);
      load_AtomicInteger = new AtomicInteger();
      init_List = List.of();
      load_LinkedHashMap = new LinkedHashMap<>();
      read_ConcurrentHashMap = new ConcurrentHashMap<>();
      init_MacroExecutor_File = new MacroExecutor.Inner_FileCache("", "", "");
      write_ConcurrentHashMap = new ConcurrentHashMap<>();
      update_ConcurrentHashMap = new ConcurrentHashMap<>();
      sync_ConcurrentHashMap = new ConcurrentHashMap<>();
      update_ThreadLocal = new ThreadLocal<>();
      sync_ThreadLocal = ThreadLocal.withInitial(() -> new int[1]);
      init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
      flush_J = TimeUnit.MINUTES.toNanos(10L);
      load_List = new CopyOnWriteArrayList<>();
      read_List = new CopyOnWriteArrayList<>();
      write_List = new CopyOnWriteArrayList<>();
      update_List = new CopyOnWriteArrayList<>();
      write_Str = "";
   }

   private record Inner_BitSetUtil(int init_I, int load, ContainerInput init_ContainerInput) {
   }

   private record Inner_BufferHelper(List<String> init_List, Object init_Obj) {
   }

   private static final class Inner_ByteReader {
      final MacroAction init_MacroAction;
      final CompletableFuture<?> init_CompletableFuture;
      final Runnable init_Runnable;
      final int init_I;

      Inner_ByteReader(MacroAction action, CompletableFuture<?> future, Runnable cleanup, int completedPacketTargets) {
         this.init_MacroAction = action;
         this.init_CompletableFuture = future;
         this.init_Runnable = cleanup == null ? () -> {} : cleanup;
         this.init_I = Math.max(0, completedPacketTargets);
      }

      void init() {
         try {
            this.init_Runnable.run();
         } catch (Throwable var2) {
         }
      }
   }

   private record Inner_CachePool(int init_I, boolean init_Z, boolean load) {
   }

   private static final class Inner_CharEncoder {
      final boolean init_Z;
      final int init_I;
      final int load_I;
      final int read;
      final Map<Integer, Integer> init_Map;
      final Map<Integer, Integer> load_Map;

      Inner_CharEncoder(
         boolean valid, int quickCraftType, int placeCount, int cost, Map<Integer, Integer> residualCounts, Map<Integer, Integer> expectedTargetCounts
      ) {
         this.init_Z = valid;
         this.init_I = quickCraftType;
         this.load_I = placeCount;
         this.read = cost;
         this.init_Map = residualCounts == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(residualCounts));
         this.load_Map = expectedTargetCounts == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(expectedTargetCounts));
      }

      static MacroExecutor.Inner_CharEncoder init() {
         return new MacroExecutor.Inner_CharEncoder(false, 0, 0, 2147483647, Map.of(), Map.of());
      }
   }

   private record Inner_ConfigStore(String init_Str, String load, String read, Component init_Component, MacroExecutor.Inner_TaskQueue init_MacroExecutor_Task) {
   }

   private static final class Inner_DataUtil implements MacroExecutionContext {
      private final MacroExecutor.Inner_TableIndex init;

      private Inner_DataUtil(MacroExecutor.Inner_TableIndex run) {
         this.init = run;
      }

      @Override
      public Minecraft mc() {
         return Minecraft.getInstance();
      }

      @Override
      public void runOnClientThread(Runnable r) {
         if (r != null) {
            Runnable safe = () -> MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(
               this.init,
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
            MacroExecutor.init_V_Minecraft_Runnable(Minecraft.getInstance(), safe);
         }
      }

      @Override
      public <T> T callOnClientThread(Supplier<T> s) {
         if (s == null) {
            return null;
         }

         Supplier<T> safe = () -> {
            AtomicReference<T> value = new AtomicReference<>();
            MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(
               this.init,
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
         return MacroExecutor.load_Obj_Minecraft(Minecraft.getInstance(), safe, null);
      }

      @Override
      public void waitTicks(int ticks) {
         MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(this.init, () -> {
            for (int i = 0; i < ticks && MacroExecutor.close_Z(); i++) {
               MacroExecutor.init_V_CompletableFuture(MacroWaitRegistry.load_CompletableFuture());
            }
         });
      }

      @Override
      public void awaitCondition(CompletableFuture<Void> future) {
         if (future != null) {
            MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(this.init, () -> MacroExecutor.init_V_CompletableFuture(future));
         }
      }

      @Override
      public void awaitCondition(AddonCondition condition) {
         if (condition != null) {
            MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(this.init, () -> MacroExecutor.init_V_CompletableFuture(MacroWaitRegistry.init(condition)));
         }
      }

      @Override
      public boolean isActive() {
         return this.init != null && this.init.init_AtomicBoolean.get() && !autismclient.modules.aD.read_Z();
      }

      @Override
      public void setStatus(String status) {
         MacroExecutor.init_V_MacroExecutor_Tabl_Runnable(this.init, () -> MacroExecutor.load_V_Str(status));
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
         return this.init == null ? Optional.empty() : this.init.init_m.init_Optional_Str(name);
      }

      @Override
      public void setVariable(String name, MacroVarValue value) {
         if (this.init != null) {
            this.init.init_m.init(name, value);
         }
      }

      @Override
      public void removeVariable(String name) {
         if (this.init != null) {
            this.init.init_m.init_V_Str(name);
         }
      }

      @Override
      public Map<String, MacroVarValue> variables() {
         return this.init == null ? Map.of() : this.init.init_m.init_Map();
      }

      @Override
      public MacroTemplateResolver.Inner_CachePool resolveTemplate(String template) {
         return MacroTemplateResolver.init_j_CachePool(template, this.init == null ? null : this.init.init_m, Minecraft.getInstance());
      }
   }

   public static final class Inner_EventBus {
      public final CompletableFuture<Packet<?>> init_CompletableFuture = new CompletableFuture<>();
      private final List<MacroExecutor.Inner_EventBus> init_List;
      private final Predicate<Packet<?>> init_Predicate;
      private final long init_J;

      Inner_EventBus(List<MacroExecutor.Inner_EventBus> registry, Predicate<Packet<?>> match) {
         this.init_List = registry;
         this.init_Predicate = match;
         this.init_J = System.nanoTime() + MacroExecutor.flush_J;
         registry.add(this);
      }

      boolean init(Packet<?> packet, long nowNanos) {
         if (this.init_CompletableFuture.isDone()) {
            return true;
         } else if (nowNanos - this.init_J >= 0L) {
            this.init_CompletableFuture.cancel(true);
            return true;
         } else if (this.init_Predicate.test(packet)) {
            this.init_CompletableFuture.complete(packet);
            return true;
         } else {
            return false;
         }
      }

      public boolean init_Z() {
         return this.init_CompletableFuture.isDone();
      }

      public Packet<?> init_Packet() {
         if (!this.init_CompletableFuture.isDone()) {
            return null;
         }

         try {
            return this.init_CompletableFuture.getNow(null);
         } catch (Throwable ignored) {
            return null;
         }
      }

      public void init_V() {
         this.init_List.remove(this);
         if (!this.init_CompletableFuture.isDone()) {
            this.init_CompletableFuture.cancel(true);
         }
      }
   }

   private static final class Inner_FieldCache {
      public final CompletableFuture<MacroExecutor.Inner_SyncManager> init_CompletableFuture = new CompletableFuture<>();
      private final List<MacroExecutor.Inner_FieldCache> init_List;
      private final MacroExecutor.Inner_MethodUtil init_MacroExecutor_Meth;
      private final long init_J;

      Inner_FieldCache(List<MacroExecutor.Inner_FieldCache> registry, MacroExecutor.Inner_MethodUtil match) {
         this.init_List = registry;
         this.init_MacroExecutor_Meth = match;
         this.init_J = System.nanoTime() + MacroExecutor.flush_J;
         registry.add(this);
      }

      boolean init(MacroExecutor.Inner_SyncManager event) {
         if (this.init_CompletableFuture.isDone()) {
            return true;
         } else if (event.init_J() - this.init_J >= 0L) {
            this.init_CompletableFuture.cancel(true);
            return true;
         } else if (this.init_MacroExecutor_Meth.test(event.init_Packet(), event.load(), event.init_J())) {
            this.init_CompletableFuture.complete(event);
            return true;
         } else {
            return false;
         }
      }

      public void init() {
         this.init_List.remove(this);
         if (!this.init_CompletableFuture.isDone()) {
            this.init_CompletableFuture.cancel(true);
         }
      }
   }

   private record Inner_FileCache(String init, String load, String read) {
   }

   public record Inner_HashUtil(String init_Str, String load, long init_J) {
   }

   private record Inner_IoWorker(autismclient.util.AutismMacro init_aM, int init_I, int load) {
   }

   private static final class Inner_JsonUtil {
      private String init_Str = "";
      private int init_I = -1;
      private int load_I = 0;
      private boolean init_Z = false;
      private boolean load_Z = false;
      private boolean read = false;
      private MacroItemTarget init_ArrayUtil = new MacroItemTarget();
   }

   private interface Inner_MethodUtil {
      boolean test(Packet<?> var1, String var2, long var3);
   }

   private record Inner_NetHelper(int init, int load) {
   }

   public record Inner_PacketUtil(
      String init_Str, String load, String read, Component init_Component, MacroExecutor.Inner_TaskQueue init_MacroExecutor_Task, long init_J
   ) {
   }

   public record Inner_PoolEntry(boolean init_Z, boolean load, boolean read, boolean write) {
      public static final MacroExecutor.Inner_PoolEntry init_MacroExecutor_Pool = new MacroExecutor.Inner_PoolEntry(false, false, false, false);

      public static MacroExecutor.Inner_PoolEntry init_MacroExecutor_Pool() {
         return new MacroExecutor.Inner_PoolEntry(true, true, true, true);
      }
   }

   private static final class Inner_RenderState {
      private static final int init_I = 8;
      private final Set<Block> init_Set;
      private final HashMap<Long, Boolean> init_HashMap = new HashMap<>();
      private final HashSet<Long> init_HashSet = new HashSet<>();
      private int load;

      private Inner_RenderState(Set<Block> targetBlocks) {
         this.init_Set = targetBlocks;
      }

      static MacroExecutor.Inner_RenderState init_MacroExecutor_Rend_Minecraft_List(Minecraft mc, List<String> rawTargets) {
         Set<Block> blocks = new LinkedHashSet<>();
         if (rawTargets != null) {
            for (String raw : rawTargets) {
               Block block = init_Block_Str(raw);
               if (block != null && block != Blocks.AIR) {
                  blocks.add(block);
               }
            }
         }

         return blocks.isEmpty() ? null : new MacroExecutor.Inner_RenderState(blocks);
      }

      private static Block init_Block_Str(String raw) {
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

      synchronized int init() {
         return this.load;
      }

      synchronized void init_V_Minecraft(Minecraft mc) {
         if (mc != null && mc.player != null && mc.level != null) {
            BlockPos center = mc.player.blockPosition();
            int minY = Math.max(mc.level.getMinY(), center.getY() - 8);
            int maxY = Math.min(mc.level.getMaxY() - 1, center.getY() + 8);

            for (int x = center.getX() - 8; x <= center.getX() + 8; x++) {
               for (int y = minY; y <= maxY; y++) {
                  for (int z = center.getZ() - 8; z <= center.getZ() + 8; z++) {
                     BlockPos pos = new BlockPos(x, y, z);
                     this.init_V_BlockPos_BlockState(pos, mc.level.getBlockState(pos));
                  }
               }
            }
         }
      }

      synchronized void init_V_Minecraft_Packet(Minecraft mc, Packet<?> packet) {
         if (mc != null && mc.level != null && packet != null) {
            if (packet instanceof ClientboundBlockUpdatePacket blockUpdate) {
               this.init(mc, blockUpdate.getPos(), blockUpdate.getBlockState());
            } else if (packet instanceof ClientboundSectionBlocksUpdatePacket sectionUpdate) {
               try {
                  sectionUpdate.runUpdates((pos, state) -> {
                     if (pos != null && state != null) {
                        this.init(mc, pos, state);
                     }
                  });
               } catch (Throwable var6) {
               }
            }
         }
      }

      private void init(Minecraft mc, BlockPos pos, BlockState newState) {
         if (mc != null && mc.level != null && pos != null && newState != null && this.init_Z_Minecraft_BlockPos(mc, pos)) {
            BlockPos stablePos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            BlockState oldState = mc.level.getBlockState(stablePos);
            if (this.init_Z_BlockState(oldState) && !this.init_Z_BlockState(newState)) {
               this.init_V_J(stablePos.asLong());
            }

            this.init_V_BlockPos_BlockState(stablePos, newState);
         }
      }

      private boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos pos) {
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

      private void init_V_BlockPos_BlockState(BlockPos pos, BlockState state) {
         if (pos != null && state != null) {
            long key = pos.asLong();
            boolean nowTarget = this.init_Z_BlockState(state);
            boolean wasTarget = Boolean.TRUE.equals(this.init_HashMap.get(key));
            if (nowTarget) {
               this.init_HashMap.put(key, Boolean.TRUE);
            } else {
               if (wasTarget) {
                  this.init_V_J(key);
               }

               this.init_HashMap.remove(key);
            }
         }
      }

      private boolean init_Z_BlockState(BlockState state) {
         return state != null && this.init_Set.contains(state.getBlock());
      }

      private void init_V_J(long posKey) {
         if (this.init_HashSet.add(posKey)) {
            this.load++;
         }
      }
   }

   private static final class Inner_RingBuffer {
      final boolean init_Z;
      final ItemStack init_ItemStack;
      final int init_I;
      final Map<Integer, Integer> init_Map;
      final Map<Integer, Integer> load;

      Inner_RingBuffer(
         boolean valid, ItemStack sourceStack, int sourceCount, Map<Integer, Integer> startTargetCounts, Map<Integer, Integer> expectedTargetCounts
      ) {
         this.init_Z = valid;
         this.init_ItemStack = sourceStack == null ? ItemStack.EMPTY : sourceStack;
         this.init_I = sourceCount;
         this.init_Map = startTargetCounts == null ? Map.of() : Map.copyOf(startTargetCounts);
         this.load = expectedTargetCounts == null ? Map.of() : Map.copyOf(expectedTargetCounts);
      }

      MacroExecutor.Inner_RingBuffer init(Map<Integer, Integer> expectedTargetCounts) {
         return new MacroExecutor.Inner_RingBuffer(this.init_Z, this.init_ItemStack, this.init_I, this.init_Map, expectedTargetCounts);
      }

      static MacroExecutor.Inner_RingBuffer init() {
         return new MacroExecutor.Inner_RingBuffer(false, ItemStack.EMPTY, 0, Map.of(), Map.of());
      }
   }

   private record Inner_StateCache(List<MacroAction> init_List, int init_I) {
      static MacroExecutor.Inner_StateCache init_MacroExecutor_Stat() {
         return new MacroExecutor.Inner_StateCache(List.of(), -1);
      }
   }

   private record Inner_StreamUtil(int init_I, boolean init_Z, boolean load) {
   }

   private record Inner_SyncManager(
      Packet<?> init_Packet, String init_Str, long init_J, MacroExecutor.Inner_ConfigStore init_MacroExecutor_Conf, Map<String, MacroVarValue> init_Map
   ) {
      Inner_SyncManager(Packet<?> packet, String direction, long nanoTime, MacroExecutor.Inner_ConfigStore chatCapture) {
         this(packet, direction, nanoTime, chatCapture, Map.of());
      }

      Inner_SyncManager {
         init_Map = init_Map == null ? Map.of() : Map.copyOf(init_Map);
      }

      String init_Str_2() {
         if (this.init_Packet == null) {
            return this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str : "client event";
         }

         String dir = this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str + " " : "";
         return dir + PacketNameResolver.init_Str_Packet_Str(this.init_Packet, this.init_Str == null ? "" : this.init_Str);
      }

      public String load() {
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

   private static final class Inner_TableIndex {
      final long init_J;
      final autismclient.util.AutismMacro init_aM;
      final autismclient.util.AutismMacro load_aM;
      final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(true);
      final AtomicBoolean load_AtomicBoolean = new AtomicBoolean(false);
      volatile String init_Str = "CANCELLED";
      final CopyOnWriteArrayList<MacroAction> init_CopyOnWriteArrayLi = new CopyOnWriteArrayList<>();
      final ConcurrentHashMap<MoveAction, int[]> init_ConcurrentHashMap = new ConcurrentHashMap<>();
      final CopyOnWriteArrayList<MacroAction> load_CopyOnWriteArrayLi = new CopyOnWriteArrayList<>();
      final Object init_Obj = new Object();
      final AtomicBoolean read_AtomicBoolean = new AtomicBoolean(false);
      final AtomicReference<String> init_AtomicReference = new AtomicReference<>("");
      volatile CompletableFuture<Void> init_CompletableFuture = null;
      volatile Packet<?> init_Packet = null;
      volatile String load_Str = "";
      final AtomicBoolean write_AtomicBoolean = new AtomicBoolean(false);
      volatile WaitForChatAction init_au = null;
      volatile CompletableFuture<Void> load_CompletableFuture = null;
      volatile AiOperation init_AiOperation = null;
      volatile AiOperation.Result init_AiOperation_Result = null;
      volatile MacroExecutor.Inner_RenderState init_MacroExecutor_Rend = null;
      volatile Thread init_Thread = null;
      volatile String read_Str = "";
      volatile int init_I = -1;
      volatile int load_I = 0;
      volatile boolean init_Z = false;
      volatile int read_I = 0;
      volatile int write_I = -1;
      volatile int update = 0;
      volatile int sync = -1;
      volatile int flush = -1;
      volatile int close = -1;
      volatile int open = -1;
      volatile long load_J = 0L;
      volatile long read_J = 0L;
      volatile Map<String, Integer> init_Map = null;
      volatile int poll = 0;
      volatile int push = 0;
      final IdentityHashMap<MacroAction, MacroExecutor.Inner_ByteReader> init_IdentityHashMap = new IdentityHashMap<>();
      final Map<MacroAction, Map<String, MacroVarValue>> load_Map = Collections.synchronizedMap(new IdentityHashMap<>());
      final Map<MacroAction, CandidatePoolSelector.Inner_BufferHelper> read_Map = Collections.synchronizedMap(new IdentityHashMap<>());
      final MacroExecutor.Inner_PoolEntry init_MacroExecutor_Pool;
      final MacroVariableStore init_m = new MacroVariableStore();

      Inner_TableIndex(long id, autismclient.util.AutismMacro sourceMacro, autismclient.util.AutismMacro macro, MacroExecutor.Inner_PoolEntry options) {
         this.init_J = id;
         this.init_aM = sourceMacro;
         this.load_aM = macro;
         this.init_MacroExecutor_Pool = options == null ? MacroExecutor.Inner_PoolEntry.init_MacroExecutor_Pool : options;
         this.load_I = macro != null ? macro.init_List.size() : 0;
      }
   }

   public enum Inner_TaskQueue {
      PLAYER,
      SERVER;
   }

   private record Inner_TextFormat(CompletableFuture<List<String>> init_CompletableFuture, Object init_Obj) {
   }

   private static final class Inner_TokenStore {
      final Object init_Obj;
      final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
      final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();
      int init_I;
      int load;

      Inner_TokenStore(Object connection, String baseQuery) {
         this.init_Obj = connection;
         this.init_ArrayDeque.add(baseQuery);
      }
   }

   public record Inner_UiHelper(autismclient.util.AutismMacro init_aM, String init_Str, String load_Str, int init_I, int load_I, int read) {
   }

   public record MacroRunOutcome(long id, String outcome, String status, int lastCompletedStep) {
   }
}
