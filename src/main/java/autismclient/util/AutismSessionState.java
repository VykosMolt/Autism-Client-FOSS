package autismclient.util;

import autismclient.AutismClientAddon;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class AutismSessionState {
   private static final AutismSessionState init_ck = new AutismSessionState();
   private volatile boolean init_Z = true;
   private volatile boolean load_Z;
   private volatile boolean read_Z = true;
   private volatile boolean write_Z = true;
   private volatile boolean update_Z;
   private volatile boolean sync_Z;
   private volatile boolean flush_Z;
   private volatile boolean close_Z;
   private volatile boolean open_Z;
   private volatile Set<Integer> init_Set = Collections.emptySet();
   private volatile boolean poll_Z;
   private volatile BlockPos init_BlockPos = null;
   private volatile String init_Str = "";
   private volatile InteractionTargetRef init_E = null;
   private volatile boolean push_Z = false;
   private final Set<Class<? extends Packet<?>>> load_Set = ConcurrentHashMap.newKeySet();
   private final Set<Class<? extends Packet<?>>> read_Set = ConcurrentHashMap.newKeySet();
   private final List<AutismSessionState.DelayedPacket> init_List = new CopyOnWriteArrayList<>();
   private volatile boolean pop_Z = false;
   private volatile int init_I = 1;
   private final List<AutismSessionState.DelayedPacket> load_List = new CopyOnWriteArrayList<>();
   private final List<AutismSessionState.DelayedPacket> read_List = new CopyOnWriteArrayList<>();
   private final List<AutismSessionState.DelayedPacket> write_List = new CopyOnWriteArrayList<>();
   private final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private int load_I = 0;
   private volatile ClientPacketListener init_ClientPacketListen = null;
   private int read_I = 0;
   private boolean tick_Z = false;
   private static final int write_I = 32;
   private final Deque<AutismSessionState.TimingMeasurement> init_Deque = new ArrayDeque<>(32);
   private volatile AutismSessionState.DelayUnit init_ck_DataUtil = AutismSessionState.DelayUnit.TICKS;
   private AutismSessionState.DelayUnit load_ck_DataUtil = AutismSessionState.DelayUnit.TICKS;
   private long init_J = 0L;
   private int update_I = 0;
   private long load_J = -1L;
   private int sync_I = -1;
   private int flush_I = 0;
   private String load_Str = null;
   private Screen init_Screen;
   private AbstractContainerMenu init_AbstractContainerM;
   private boolean run_Z = false;
   private boolean get_Z = false;
   private int close_I = 500;
   private int open_I = 5;
   private String read_Str = "0";
   private String write_Str = "";
   private String update_Str = "1";
   private int poll_I = 0;
   private int push_I = 0;
   private boolean set_Z = false;
   private boolean put_Z = false;
   private String sync_Str = "";
   private int pop_I = -1;
   private String flush_Str = "";
   private int tick_I = 0;
   private final List<autismclient.util.macro.CraftAction.Inner_DataUtil> update_List = new ArrayList<>();
   private int run_I = -1;
   private int get_I = 0;
   private boolean add_Z = false;
   private int set_I = 500;
   private int put_I = 5;
   private int add_I = 0;
   private int clear_I = 0;
   private boolean clear_Z = false;
   private String close_Str = "";
   private String open_Str = "";
   private String poll_Str = "";
   private boolean reset_Z = false;
   private int reset_I = 500;
   private int start_I = 250;
   private int stop_I = 0;
   private String push_Str = "";
   private boolean start_Z = false;
   private int next_I = 100;
   private int prev_I = 100;
   private int valid_I = 0;
   private int size_I = 0;
   private String pop_Str = "";
   private String tick_Str = "";
   private volatile String run_Str = "";
   private volatile String get_Str = "";
   private int data_I = 50;
   private int value_I = 252;
   private int cache_I = 258;
   private int state_I = 280;
   private int queue_I = 280;
   private boolean stop_Z = false;
   private AutismMacro init_aM = null;
   private int buffer_I = 400;
   private int entry_I = 100;
   private boolean next_Z = false;
   private volatile boolean prev_Z = false;
   private volatile int node = 0;
   private Runnable init_Runnable = null;
   private final List<AutismSessionState.DelayedPacket> sync_List = new ArrayList<>();
   private volatile boolean valid_Z = false;
   private volatile long read_J = -1L;
   private volatile int task = -1;
   private volatile int pool = 0;
   private volatile double init_D = 50.0;
   private volatile long write_J = -1L;
   private final Map<String, OverlayBounds> init_Map = new HashMap<>();
   private final Set<String> write_Set = ConcurrentHashMap.newKeySet();
   private final Map<String, AutismSessionState.PluginScanResult> load_Map = new HashMap<>();
   private final List<String> flush_List = new ArrayList<>();
   private String set_Str = "";
   private static final boolean size_Z = Boolean.getBoolean("autism.payload.trace");
   private volatile boolean data_Z = false;
   private volatile boolean value_Z = false;
   private volatile boolean cache_Z = false;
   private volatile boolean state_Z = false;
   private volatile boolean queue_Z = false;
   private volatile Consumer<BlockPos> init_Consumer = null;
   private volatile BiConsumer<BlockPos, Direction> init_BiConsumer = null;
   private volatile Runnable load_Runnable = null;
   private volatile Runnable read_Runnable = null;
   private volatile Consumer<String> load_Consumer = null;
   private volatile boolean buffer_Z = false;
   private volatile boolean entry_Z = false;

   public synchronized void init_V_ck_ConfigStore(AutismSessionState.TimingMeasurement sample) {
      if (sample != null) {
         this.init_Deque.addFirst(sample);

         while (this.init_Deque.size() > 32) {
            this.init_Deque.removeLast();
         }
      }
   }

   public synchronized AutismSessionState.TimingMeasurement init_ck_ConfigStore_Str(String label) {
      if (label == null) {
         return this.init_Deque.isEmpty() ? null : this.init_Deque.peekFirst();
      }

      for (AutismSessionState.TimingMeasurement s : this.init_Deque) {
         if (label.equals(s.init_Str)) {
            return s;
         }
      }

      return null;
   }

   public synchronized List<AutismSessionState.TimingMeasurement> init_List_I(int n) {
      List<AutismSessionState.TimingMeasurement> out = new ArrayList<>(Math.min(n, this.init_Deque.size()));
      int i = 0;

      for (AutismSessionState.TimingMeasurement s : this.init_Deque) {
         if (i++ >= n) {
            break;
         }

         out.add(s);
      }

      return out;
   }

   private AutismSessionState() {
   }

   public static AutismSessionState init_ck() {
      return init_ck;
   }

   public boolean init_Z() {
      return this.stop_Z;
   }

   public void init_V_Z(boolean visible) {
      this.stop_Z = visible;
   }

   public AutismMacro init_aM() {
      return this.init_aM;
   }

   public void init_V_aM(AutismMacro macro) {
      this.init_aM = macro;
   }

   public int init_I() {
      return this.buffer_I;
   }

   public void init_V_I(int x) {
      this.buffer_I = x;
   }

   public int load_I() {
      return this.entry_I;
   }

   public void load_V_I(int y) {
      this.entry_I = y;
   }

   public synchronized OverlayBounds init_cH_Str(String id) {
      OverlayBounds layout = this.init_Map.get(id);
      return layout == null ? null : new OverlayBounds(layout.init_I, layout.load_I, layout.read, layout.write, layout.init_Z, layout.load_Z);
   }

   public boolean init_Z_Str(String id) {
      return id != null && this.write_Set.contains(id);
   }

   public void init_V_Str_Z(String id, boolean pinned) {
      if (id != null && !id.isEmpty()) {
         if (pinned) {
            this.write_Set.add(id);
         } else {
            this.write_Set.remove(id);
         }
      }
   }

   public synchronized void init_V_Str_cH(String id, OverlayBounds layout) {
      if (id != null && !id.isEmpty() && layout != null) {
         OverlayBounds trueGeometry = OverlayManager.init_bk().init(id);
         if (trueGeometry != null) {
            layout = new OverlayBounds(trueGeometry.init_I, trueGeometry.load_I, trueGeometry.read, trueGeometry.write, layout.init_Z, layout.load_Z);
         }

         this.init_Map.put(id, new OverlayBounds(layout.init_I, layout.load_I, layout.read, layout.write, layout.init_Z, layout.load_Z));
      }
   }

   public synchronized List<String> init_List() {
      return new ArrayList<>(this.flush_List);
   }

   public synchronized void init_V_List(List<String> order) {
      this.flush_List.clear();
      if (order != null) {
         for (String id : order) {
            if (id != null && !id.isEmpty() && !this.flush_List.contains(id)) {
               this.flush_List.add(id);
            }
         }
      }
   }

   public synchronized String init_Str() {
      return this.set_Str == null ? "" : this.set_Str;
   }

   public synchronized void init_V_Str(String overlayId) {
      this.set_Str = overlayId == null ? "" : overlayId.trim();
   }

   public synchronized AutismSessionState.PluginScanResult init_ck_TaskQueue_Str(String address) {
      return this.init_ck_TaskQueue_Str_Str(address, "");
   }

   public synchronized AutismSessionState.PluginScanResult init_ck_TaskQueue_Str_Str(String address, String contextSignature) {
      String key = init_Str_Str_Str(address, contextSignature);
      if (key.isEmpty()) {
         return null;
      }

      AutismSessionState.PluginScanResult scan = this.load_Map.get(key);
      return scan == null ? null : scan.init_ck_TaskQueue();
   }

   public synchronized void init(String address, List<String> plugins, Map<String, List<String>> pluginCommands) {
      this.init(address, "", plugins, pluginCommands, Map.of());
   }

   public synchronized void init(String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands) {
      this.init(address, contextSignature, plugins, pluginCommands, Map.of());
   }

   public synchronized void init(
      String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands, Map<String, String> pluginEvidence
   ) {
      String normalizedAddress = load_Str_Str(address);
      String normalizedContext = read_Str_Str(contextSignature);
      String key = init_Str_Str_Str(normalizedAddress, normalizedContext);
      if (!key.isEmpty()) {
         this.load_Map.put(key, new AutismSessionState.PluginScanResult(normalizedAddress, normalizedContext, plugins, pluginCommands, pluginEvidence));
      }
   }

   public synchronized void load_V_Str(String address) {
      String normalized = load_Str_Str(address);
      if (!normalized.isEmpty()) {
         this.load_Map.keySet().removeIf(key -> key.equals(normalized) || key.startsWith(normalized + "|"));
      }
   }

   private static String load_Str_Str(String address) {
      return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
   }

   private static String read_Str_Str(String contextSignature) {
      return contextSignature == null ? "" : contextSignature.trim().toLowerCase(Locale.ROOT);
   }

   private static String init_Str_Str_Str(String address, String contextSignature) {
      String normalizedAddress = load_Str_Str(address);
      if (normalizedAddress.isEmpty()) {
         return "";
      }

      String normalizedContext = read_Str_Str(contextSignature);
      return normalizedContext.isEmpty() ? normalizedAddress + "|default" : normalizedAddress + "|" + normalizedContext;
   }

   public boolean load_Z() {
      return this.next_Z;
   }

   public void load_V_Z(boolean visible) {
      this.next_Z = visible;
   }

   public boolean read_Z() {
      return this.init_Z;
   }

   public void read_V_Z(boolean value) {
      this.init_Z = value;
   }

   public boolean write_Z() {
      return this.load_Z;
   }

   public void write(boolean value) {
      if (!value || !autismclient.modules.aD.read_Z()) {
         this.load_Z = value;
      }
   }

   public boolean update_Z() {
      return this.flush_Z;
   }

   public void update(boolean value) {
      this.flush_Z = value;
   }

   public boolean sync_Z() {
      return this.close_Z;
   }

   public void sync(boolean value) {
      if (!value || this.init_Set != null && !this.init_Set.isEmpty() || this.poll_Z) {
         this.close_Z = value;
         if (!value) {
            this.init_Set = Collections.emptySet();
            this.poll_Z = false;
         }
      }
   }

   public Set<Integer> init_Set() {
      return this.init_Set;
   }

   public boolean flush_Z() {
      return this.poll_Z;
   }

   public void init_V_Set_Z(Set<Integer> slotMask, boolean carryCursor) {
      LinkedHashSet<Integer> copy = new LinkedHashSet<>();
      if (slotMask != null) {
         for (Integer slot : slotMask) {
            if (slot != null && slot >= 0) {
               copy.add(slot);
            }
         }
      }

      this.init_Set = Collections.unmodifiableSet(copy);
      this.poll_Z = carryCursor;
   }

   public void load(Set<Integer> slotMask, boolean carryCursor) {
      LinkedHashSet<Integer> merged = new LinkedHashSet<>();
      if (this.close_Z && this.init_Set != null) {
         for (Integer slot : this.init_Set) {
            if (slot != null && slot >= 0) {
               merged.add(slot);
            }
         }
      }

      if (slotMask != null) {
         for (Integer slot : slotMask) {
            if (slot != null && slot >= 0) {
               merged.add(slot);
            }
         }
      }

      this.init_Set = Collections.unmodifiableSet(merged);
      this.poll_Z = this.close_Z && this.poll_Z || carryCursor;
   }

   public boolean close_Z() {
      return this.open_Z;
   }

   public void flush(boolean value) {
      this.open_Z = value;
   }

   public String load_Str() {
      return this.init_Str == null ? "" : this.init_Str;
   }

   public void read_V_Str(String message) {
      this.init_Str = message == null ? "" : message;
   }

   public BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   public void init_V_BlockPos(BlockPos pos) {
      this.init_BlockPos = pos;
   }

   public InteractionTargetRef init_E() {
      return this.init_E;
   }

   public void init_V_E(InteractionTargetRef target) {
      this.init_E = target;
      if (target != null && target.init_Z()) {
         this.init_BlockPos = target.init_BlockPos();
      }
   }

   public synchronized List<AutismSessionState.DelayedPacket> load_List() {
      return new ArrayList<>(this.init_List);
   }

   public int read_I() {
      return this.init_AtomicInteger.get();
   }

   public synchronized AutismSessionState.DelayQueueSnapshot init_ck_StreamUtil_Z_I(boolean sending, int maxLines) {
      List<AutismSessionState.DelayedPacket> source = sending ? this.load_List : this.init_List;
      int totalCount = source.size();
      if (totalCount != 0 && maxLines > 0) {
         int limit = Math.min(totalCount, maxLines);
         ArrayList<AutismSessionState.DelayedPacket> visiblePackets = new ArrayList<>(limit);

         for (AutismSessionState.DelayedPacket packet : source) {
            if (packet != null && packet.init_Packet != null) {
               int insertAt = visiblePackets.size();

               for (int i = 0; i < visiblePackets.size(); i++) {
                  AutismSessionState.DelayedPacket existing = visiblePackets.get(i);
                  int byDelay = Integer.compare(existing.load_I(), packet.load_I());
                  if (byDelay > 0 || byDelay == 0 && existing.init_I() > packet.init_I()) {
                     insertAt = i;
                     break;
                  }
               }

               if (insertAt < limit) {
                  visiblePackets.add(insertAt, packet);
                  if (visiblePackets.size() > limit) {
                     visiblePackets.remove(limit);
                  }
               } else if (visiblePackets.size() < limit) {
                  visiblePackets.add(packet);
               }
            }
         }

         return new AutismSessionState.DelayQueueSnapshot(visiblePackets, totalCount);
      } else {
         return new AutismSessionState.DelayQueueSnapshot(List.of(), totalCount);
      }
   }

   public boolean open_Z() {
      return !this.init_List.isEmpty();
   }

   public synchronized void load_V_List(List<AutismSessionState.DelayedPacket> packets) {
      this.init_List.clear();
      if (packets != null && !packets.isEmpty()) {
         this.init_List.addAll(this.init_List_List(packets));
      }

      this.pop_V();
      this.set_V();
   }

   public synchronized void init_V() {
      if (!this.init_List.isEmpty()) {
         this.run_V();
      }

      this.init_List.clear();
      this.pop_V();
      this.set_V();
   }

   private void pop_V() {
      if (this.init_List.isEmpty() && this.load_List.isEmpty()) {
         this.tick_V();
         AutismSessionState.DelayedPacket.load_V();
      }
   }

   private void tick_V() {
      this.read_J = -1L;
      this.task = -1;
   }

   private void run_V() {
      this.write_List.clear();
      if (!this.init_List.isEmpty()) {
         this.write_List.addAll(this.init_List_List(this.init_List));
      }
   }

   private void write(List<AutismSessionState.DelayedPacket> queue) {
      this.write_List.clear();
      if (queue != null && !queue.isEmpty()) {
         this.write_List.addAll(this.init_List_List(queue));
      }
   }

   private void get_V() {
      this.load_List.clear();
      this.read_List.clear();
      this.init_ClientPacketListen = null;
      this.load_I = 0;
      this.init_J = 0L;
      this.update_I = 0;
      this.read_I = 0;
      this.tick_Z = false;
      this.load_J = -1L;
      this.sync_I = -1;
      this.flush_I = 0;
      this.load_ck_DataUtil = this.init_ck_DataUtil;
      this.set_V();
   }

   private void set_V() {
      this.init_AtomicInteger.incrementAndGet();
   }

   private boolean store() {
      return this.init_ClientPacketListen != null && !this.load_List.isEmpty();
   }

   private int init_I_ck_DataUtil(AutismSessionState.DelayUnit mode) {
      if (mode == AutismSessionState.DelayUnit.MS) {
         return this.init_J <= 0L ? 0 : (int)Math.max(0L, (System.nanoTime() - this.init_J) / 1000000L);
      } else {
         return Math.max(0, this.pool - this.update_I);
      }
   }

   private int init_I_List(List<AutismSessionState.DelayedPacket> queue) {
      int maxDelay = 0;
      if (queue != null && !queue.isEmpty()) {
         for (AutismSessionState.DelayedPacket qp : queue) {
            if (qp != null && !qp.load_Z()) {
               maxDelay = Math.max(maxDelay, qp.load_I());
            }
         }

         return maxDelay;
      } else {
         return maxDelay;
      }
   }

   private void init_V_List_ck_CachePool(List<AutismSessionState.DelayedPacket> queue, AutismSessionState.DelayedPacket packet) {
      if (queue != null && packet != null) {
         int insertAt = queue.size();

         for (int i = 0; i < queue.size(); i++) {
            AutismSessionState.DelayedPacket existing = queue.get(i);
            if (existing != null) {
               int byDelay = Integer.compare(existing.load_I(), packet.load_I());
               if (byDelay > 0 || byDelay == 0 && existing.init_I() > packet.init_I()) {
                  insertAt = i;
                  break;
               }
            }
         }

         queue.add(insertAt, packet);
      }
   }

   private int queue_I() {
      int elapsed = this.init_I_ck_DataUtil(this.load_ck_DataUtil);
      if (this.tick_Z || this.valid_Z) {
         return elapsed;
      } else if (this.pop_Z) {
         int tailDelay = this.init_I_List(this.load_List);
         return Math.max(elapsed, tailDelay) + this.init_I;
      } else {
         return elapsed;
      }
   }

   private int buffer_I() {
      if (this.load_ck_DataUtil == AutismSessionState.DelayUnit.MS) {
         long now = System.nanoTime();
         long reference = this.load_J > 0L ? this.load_J : this.init_J;
         int delta = reference > 0L ? (int)Math.max(0L, (now - reference) / 1000000L) : 0;
         this.load_J = now;
         this.sync_I = this.pool;
         this.flush_I += delta;
         return this.flush_I;
      } else {
         int tick = this.pool;
         int reference = this.sync_I >= 0 ? this.sync_I : this.update_I;
         int delta = Math.max(0, tick - reference);
         this.sync_I = tick;
         this.load_J = System.nanoTime();
         this.flush_I += delta;
         return this.flush_I;
      }
   }

   public synchronized boolean poll_Z() {
      if (this.write_List.isEmpty()) {
         return false;
      }

      this.init_List.clear();
      this.tick_V();
      if (!this.write_List.isEmpty()) {
         this.init_List.addAll(this.init_List_List(this.write_List));
      }

      this.set_V();
      return true;
   }

   public synchronized void init_V_Packet(Packet<?> packet) {
      this.init_V_Packet_ck_BufferHelper(packet, AutismSessionState.ReplayMode.REGENERATE);
   }

   public synchronized void load_V_Packet(Packet<?> packet) {
      this.init_V_Packet_ck_BufferHelper(packet, AutismSessionState.ReplayMode.EXACT);
   }

   public synchronized void init_V_Packet_ck_BufferHelper(Packet<?> packet, AutismSessionState.ReplayMode replayMode) {
      if (!autismclient.modules.aD.read_Z() && packet != null) {
         AutismSessionState.ReplayMode mode = this.init_ck_BufferHelper_Packet_ck_BufferHelper(packet, replayMode);
         if (this.store()) {
            int activeDelay = this.valid_Z ? this.buffer_I() : this.queue_I();
            AutismSessionState.DelayedPacket queuedPacket = new AutismSessionState.DelayedPacket(packet, activeDelay, mode);
            this.init_V_List_ck_CachePool(this.load_List, queuedPacket);
            this.init_V_List_ck_CachePool(this.read_List, queuedPacket);
            this.read_I++;
            this.set_V();
            return;
         }

         int delay = 0;
         if (this.valid_Z) {
            if (this.init_List.isEmpty() && this.load_List.isEmpty()) {
               this.tick_V();
            }

            if (this.init_ck_DataUtil == AutismSessionState.DelayUnit.TICKS) {
               int tick = this.pool;
               if (this.task < 0) {
                  this.task = tick;
                  delay = 0;
               } else {
                  delay = tick - this.task;
               }
            } else {
               long now = System.nanoTime();
               if (this.read_J < 0L) {
                  this.read_J = now;
                  delay = 0;
               } else {
                  delay = (int)((now - this.read_J) / 1000000L);
               }
            }
         } else if (this.pop_Z) {
            delay = this.init_List.size() * this.init_I;
         }

         this.init_List.add(new AutismSessionState.DelayedPacket(packet, delay, mode));
         this.set_V();
      }
   }

   private AutismSessionState.ReplayMode init_ck_BufferHelper_Packet_ck_BufferHelper(Packet<?> packet, AutismSessionState.ReplayMode replayMode) {
      AutismSessionState.ReplayMode mode = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      return mode == AutismSessionState.ReplayMode.REGENERATE
            && packet instanceof ServerboundContainerClosePacket closePacket
            && this.init_Z_I(this.init_I_ServerboundContain(closePacket))
         ? AutismSessionState.ReplayMode.EXACT
         : mode;
   }

   private int init_I_ServerboundContain(ServerboundContainerClosePacket packet) {
      if (packet == null) {
         return -1;
      }

      for (String methodName : new String[]{"containerId", "syncId", "getSyncId"}) {
         try {
            Method method = packet.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            if (method.invoke(packet) instanceof Number number) {
               return number.intValue();
            }
         } catch (ReflectiveOperationException var10) {
         }
      }

      if (packet.getClass().isRecord()) {
         for (RecordComponent component : packet.getClass().getRecordComponents()) {
            try {
               if (component.getAccessor().invoke(packet) instanceof Number number) {
                  return number.intValue();
               }
            } catch (ReflectiveOperationException var9) {
            }
         }
      }

      for (Field field : packet.getClass().getDeclaredFields()) {
         if (field.getType() == int.class) {
            try {
               field.setAccessible(true);
               return field.getInt(packet);
            } catch (IllegalAccessException var11) {
            }
         }
      }

      return -1;
   }

   private boolean init_Z_I(int containerId) {
      for (AutismSessionState.DelayedPacket queuedPacket : this.init_List) {
         if (this.init_Z_ck_CachePool_I(queuedPacket, containerId)) {
            return true;
         }
      }

      for (AutismSessionState.DelayedPacket queuedPacket : this.load_List) {
         if (this.init_Z_ck_CachePool_I(queuedPacket, containerId)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_ck_CachePool_I(AutismSessionState.DelayedPacket queuedPacket, int containerId) {
      return queuedPacket != null
         && queuedPacket.init_Z()
         && queuedPacket.init_Packet instanceof ServerboundContainerClickPacket clickPacket
         && clickPacket.containerId() == containerId;
   }

   public void init_V_Screen_AbstractContainerM(Screen screen, AbstractContainerMenu handler) {
      this.init_Screen = screen;
      this.init_AbstractContainerM = handler;
   }

   public Screen init_Screen() {
      return this.init_Screen;
   }

   public AbstractContainerMenu init_AbstractContainerM() {
      return this.init_AbstractContainerM;
   }

   public synchronized int init_I_ClientPacketListen(ClientPacketListener networkHandler) {
      if (autismclient.modules.aD.read_Z()) {
         return 0;
      }

      if (!this.load_List.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cAlready sending - wait for current queue to finish");
         return 0;
      }

      this.load_Str = null;
      int count = this.init_List.size();
      if (count == 0) {
         return 0;
      }

      this.tick_V();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      boolean hasDelays = this.init_List.stream().anyMatch(qpx -> qpx.load > 0);
      if (module.reset_Z() && !hasDelays) {
         return this.load_I_ClientPacketListen(networkHandler);
      }

      this.run_V();
      if (networkHandler != null) {
         boolean shouldStagger = this.pop_Z || hasDelays;
         if (shouldStagger) {
            this.read_List.clear();
            this.load_List.addAll(this.init_List);
            this.read_List.addAll(this.init_List);
            this.init_ClientPacketListen = networkHandler;
            this.load_I = 0;
            this.read_I = count;
            this.tick_Z = hasDelays;
            this.load_ck_DataUtil = this.init_ck_DataUtil;
            this.init_J = System.nanoTime();
            this.update_I = this.pool;
            this.load_J = this.init_J;
            this.sync_I = this.update_I;
            this.flush_I = this.init_I_List(this.load_List);
         } else {
            this.run_Z = true;

            try {
               for (AutismSessionState.DelayedPacket qp : this.init_List) {
                  if (PacketTypeRegistry.load().contains(qp.init_Packet.getClass())) {
                     Packet<?> packetToSend = this.init_Packet_ck_CachePool(qp);
                     if (packetToSend != null) {
                        DirectPacketSender.init(packetToSend);
                     }
                  } else {
                     AutismClientAddon.LOG.warn("[Autism] Skipped sending invalid C2S packet during flush: {}", qp.init_Packet.getClass().getName());
                  }
               }
            } finally {
               this.run_Z = false;
            }

            AutismSessionState.DelayedPacket.load_V();
         }
      }

      this.init_List.clear();
      this.set_V();
      return count;
   }

   public void init_V_ClientPacketListen_Packet(ClientPacketListener networkHandler, Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z() && networkHandler != null && packet != null) {
         this.run_Z = true;

         try {
            networkHandler.getConnection().send(packet, null);
         } finally {
            this.run_Z = false;
         }
      }
   }

   private int load_I_ClientPacketListen(ClientPacketListener networkHandler) {
      if (autismclient.modules.aD.read_Z()) {
         return 0;
      }

      int count = this.init_List.size();
      if (count == 0) {
         return 0;
      }

      this.run_V();
      if (networkHandler != null) {
         this.run_Z = true;

         try {
            for (AutismSessionState.DelayedPacket qp : this.init_List) {
               if (PacketTypeRegistry.load().contains(qp.init_Packet.getClass())) {
                  Packet<?> packetToSend = this.init_Packet_ck_CachePool(qp);
                  if (packetToSend != null) {
                     networkHandler.getConnection().send(packetToSend, null);
                  }
               } else {
                  AutismClientAddon.LOG.warn("[Autism] Skipped invalid C2S packet during bypass flush: {}", qp.init_Packet.getClass().getName());
               }
            }
         } finally {
            this.run_Z = false;
         }
      }

      this.init_List.clear();
      this.pop_V();
      this.set_V();
      return count;
   }

   public synchronized void load_V() {
      if (!this.init_List.isEmpty()) {
         List<AutismSessionState.DelayedPacket> newPackets = new ArrayList<>(this.init_List.size());
         int skipped = 0;
         int deferredClickSlot = 0;

         for (AutismSessionState.DelayedPacket qp : this.init_List) {
            if (qp.init_Packet instanceof ServerboundContainerClickPacket) {
               newPackets.add(new AutismSessionState.DelayedPacket(qp.init_Packet, qp.load, qp.init_I, qp.init_ck_BufferHelper));
               deferredClickSlot++;
            } else if (qp.init_Z()) {
               newPackets.add(new AutismSessionState.DelayedPacket(qp.init_Packet, qp.load, qp.init_I, qp.init_ck_BufferHelper));
            } else {
               Packet<?> regenerated = PacketRegenerator.init(qp.init_Packet);
               if (regenerated != null) {
                  newPackets.add(new AutismSessionState.DelayedPacket(regenerated, qp.load, qp.init_I, qp.init_ck_BufferHelper));
               } else {
                  skipped++;
               }
            }
         }

         this.init_List.clear();
         this.init_List.addAll(this.init_List_List(newPackets));
         this.set_V();
         if (skipped <= 0 && deferredClickSlot <= 0) {
            AutismClientAddon.LOG.info("[Autism] Regenerated {} queued packets (IDs preserved).", newPackets.size());
         } else {
            AutismClientAddon.LOG
               .warn(
                  "[Autism] Regenerated {} packets, skipped {}, deferred {} ClickSlot (IDs preserved).",
                  newPackets.size() - deferredClickSlot,
                  skipped,
                  deferredClickSlot
               );
         }
      }
   }

   public synchronized void read_V() {
      if (autismclient.modules.aD.read_Z()) {
         this.load_List.clear();
         this.read_List.clear();
         this.init_ClientPacketListen = null;
         this.set_V();
      } else if (!this.load_List.isEmpty() && this.init_ClientPacketListen != null) {
         List<AutismSessionState.DelayedPacket> toRemove = new ArrayList<>();

         for (AutismSessionState.DelayedPacket qp : this.load_List) {
            boolean shouldSend;
            if (this.load_ck_DataUtil == AutismSessionState.DelayUnit.MS) {
               long elapsedMs = (System.nanoTime() - this.init_J) / 1000000L;
               shouldSend = elapsedMs >= qp.load_I() && !qp.load_Z();
            } else {
               int elapsedTicks = this.pool - this.update_I;
               shouldSend = elapsedTicks >= qp.load_I() && !qp.load_Z();
            }

            if (shouldSend) {
               this.run_Z = true;

               try {
                  if (PacketTypeRegistry.load().contains(qp.init_Packet.getClass())) {
                     Packet<?> packetToSend = this.init_Packet_ck_CachePool(qp);
                     if (packetToSend == null) {
                        qp.init_V();
                        toRemove.add(qp);
                     } else {
                        this.init_ClientPacketListen.getConnection().send(packetToSend, null);
                        qp.init_V();
                        toRemove.add(qp);
                     }
                  } else {
                     AutismClientAddon.LOG.warn("[Autism] Skipped sending invalid C2S packet: {}", qp.init_Packet.getClass().getName());
                     qp.init_V();
                     toRemove.add(qp);
                  }
               } catch (Exception e) {
                  qp.init_V();
                  toRemove.add(qp);
                  AutismClientAddon.LOG.error("[Autism] Failed to send queued packet: {}", qp.init_Packet.getClass().getName(), e);
               } finally {
                  this.run_Z = false;
               }
            }
         }

         this.load_List.removeAll(toRemove);
         if (!toRemove.isEmpty()) {
            this.set_V();
         }

         this.load_I++;
         if (this.load_List.isEmpty()) {
            int total = this.read_I;
            String completionMessage = this.run_Str();
            this.get_V();
            if (this.init_List.isEmpty()) {
               this.tick_V();
               AutismSessionState.DelayedPacket.load_V();
            }

            if (completionMessage == null || completionMessage.isBlank()) {
               completionMessage = "Finished sending " + total + " packets.";
            }

            bi.init(completionMessage, -13248397);
         }
      }
   }

   public boolean push_Z() {
      return !this.load_List.isEmpty() && this.init_ClientPacketListen != null;
   }

   public boolean pop_Z() {
      return this.load_Z
         || !this.init_Z
         || this.push_Z
         || this.run_Z
         || this.data_Z
         || this.value_Z
         || this.cache_Z
         || this.state_Z
         || this.queue_Z
         || !this.read_Z
         || !this.write_Z
         || this.prev_Z
         || this.valid_Z
         || this.init_Consumer != null
         || this.init_BiConsumer != null
         || this.load_Runnable != null
         || this.load_Consumer != null
         || this.read_Runnable != null;
   }

   public boolean tick_Z() {
      return !this.load_List.isEmpty() && this.init_ClientPacketListen != null;
   }

   public synchronized void write_V(String message) {
      this.load_Str = message != null && !message.isBlank() ? message : null;
   }

   private String run_Str() {
      String message = this.load_Str;
      this.load_Str = null;
      return message;
   }

   public synchronized List<AutismSessionState.DelayedPacket> read_List() {
      return new ArrayList<>(this.load_List);
   }

   public boolean run_Z() {
      return !this.load_List.isEmpty();
   }

   public synchronized List<AutismSessionState.DelayedPacket> write_List() {
      return new ArrayList<>(this.read_List);
   }

   public synchronized int write_I() {
      if (!this.load_List.isEmpty()) {
         int count = this.load_List.size() + this.init_List.size();
         List<AutismSessionState.DelayedPacket> combined = new ArrayList<>(this.load_List.size() + this.init_List.size());
         combined.addAll(this.load_List);
         combined.addAll(this.init_List);
         this.write(combined);
         this.load_Str = null;
         this.get_V();
         this.init_List.clear();
         this.pop_V();
         return count;
      } else {
         int count = this.init_List.size();
         this.init_V();
         return count;
      }
   }

   public synchronized void init_V_ck_CachePool(AutismSessionState.DelayedPacket packet) {
      if (packet != null) {
         this.read_List.remove(packet);
         boolean removedFromStaggered = this.load_List.remove(packet);
         if (removedFromStaggered && this.load_List.isEmpty()) {
            this.get_V();
         }

         if (!removedFromStaggered) {
            this.init_List.remove(packet);
            if (!this.init_List.isEmpty()) {
               List<AutismSessionState.DelayedPacket> rebuilt = this.init_List_List(this.init_List);
               this.init_List.clear();
               this.init_List.addAll(rebuilt);
            }
         }

         this.pop_V();
         this.set_V();
      }
   }

   public synchronized void init_V_ck_CachePool_I(AutismSessionState.DelayedPacket packet, int newDelay) {
      if (packet != null) {
         packet.init(newDelay);
         this.set_V();
      }
   }

   public synchronized void write_V() {
      if (this.init_List.size() >= 2) {
         List<AutismSessionState.DelayedPacket> ordered = new ArrayList<>(this.init_List);
         ordered.sort((a, b) -> {
            int byDelay = Integer.compare(a.load_I(), b.load_I());
            return byDelay != 0 ? byDelay : Integer.compare(a.init_I(), b.init_I());
         });
         this.init_List.clear();
         this.init_List.addAll(ordered);
         this.set_V();
      }
   }

   private List<AutismSessionState.DelayedPacket> init_List_List(List<AutismSessionState.DelayedPacket> source) {
      List<AutismSessionState.DelayedPacket> rebuilt = new ArrayList<>();
      if (source != null && !source.isEmpty()) {
         List<AutismSessionState.DelayedPacket> ordered = new ArrayList<>();

         for (AutismSessionState.DelayedPacket qp : source) {
            if (qp != null && qp.init_Packet != null) {
               ordered.add(qp);
            }
         }

         ordered.sort((a, b) -> {
            int byDelay = Integer.compare(a.load_I(), b.load_I());
            return byDelay != 0 ? byDelay : Integer.compare(a.init_I(), b.init_I());
         });
         AutismSessionState.DelayedPacket.load_V();

         for (AutismSessionState.DelayedPacket qp : ordered) {
            rebuilt.add(new AutismSessionState.DelayedPacket(qp.init_Packet, qp.load_I(), qp.init_ck_BufferHelper()));
         }

         return rebuilt;
      } else {
         return rebuilt;
      }
   }

   private Packet<?> init_Packet_ck_CachePool(AutismSessionState.DelayedPacket queuedPacket) {
      if (queuedPacket != null && queuedPacket.init_Packet != null) {
         return queuedPacket.init_Z() ? queuedPacket.init_Packet : PacketRegenerator.init(queuedPacket.init_Packet);
      } else {
         return null;
      }
   }

   public boolean get_Z() {
      return this.run_Z;
   }

   public void close(boolean value) {
      this.run_Z = value;
   }

   public void update_V() {
      this.init_Screen = null;
      this.init_AbstractContainerM = null;
   }

   public synchronized void sync_V() {
      this.init_Z = true;
      this.load_Z = false;
      this.read_Z = true;
      this.write_Z = true;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.close_Z = false;
      this.init_BlockPos = null;
      this.init_E = null;
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      this.init_ClientPacketListen = null;
      this.load_I = 0;
      this.read_I = 0;
      AutismSessionState.DelayedPacket.load_V();
      this.tick_Z = false;
      this.init_J = 0L;
      this.update_I = 0;
      this.load_J = -1L;
      this.sync_I = -1;
      this.flush_I = 0;
      this.load_ck_DataUtil = AutismSessionState.DelayUnit.TICKS;
      this.read_J = -1L;
      this.task = -1;
      this.set_V();
      this.init_Screen = null;
      this.init_AbstractContainerM = null;
      this.load_Map.clear();
      this.valid_I = 0;
      this.size_I = 0;
      this.pop_Str = "";
      this.tick_Str = "";
      this.run_Str = "";
      this.get_Str = "";
      this.data_I = 50;
   }

   public boolean set_Z() {
      return this.read_Z;
   }

   public void open(boolean value) {
      this.read_Z = value;
   }

   public boolean put_Z() {
      return this.write_Z;
   }

   public void poll(boolean value) {
      this.write_Z = value;
   }

   public boolean add_Z() {
      return this.update_Z;
   }

   public void push(boolean value) {
      this.update_Z = value;
   }

   public boolean clear_Z() {
      return this.sync_Z;
   }

   public void pop(boolean value) {
      this.sync_Z = value;
   }

   public boolean reset_Z() {
      return this.push_Z;
   }

   public void tick(boolean value) {
      this.push_Z = value;
   }

   public Set<Class<? extends Packet<?>>> load_Set() {
      return this.load_Set;
   }

   public void init_V_Set(Set<Class<? extends Packet<?>>> packets) {
      this.load_Set.clear();
      this.load_Set.addAll(packets);
   }

   public Set<Class<? extends Packet<?>>> read_Set() {
      return this.read_Set;
   }

   public void load_V_Set(Set<Class<? extends Packet<?>>> packets) {
      this.read_Set.clear();
      this.read_Set.addAll(packets);
   }

   public boolean start_Z() {
      return this.pop_Z;
   }

   public void run(boolean value) {
      if (!value || !autismclient.modules.aD.read_Z()) {
         this.pop_Z = value;
      }
   }

   public int update_I() {
      return this.init_I;
   }

   public void read_V_I(int delay) {
      this.init_I = Math.max(1, Math.min(40, delay));
   }

   public AutismSessionState.DelayUnit init_ck_DataUtil() {
      return this.init_ck_DataUtil;
   }

   public synchronized AutismSessionState.DelayUnit load_ck_DataUtil() {
      return this.store() ? this.load_ck_DataUtil : this.init_ck_DataUtil;
   }

   public void init_V_ck_DataUtil(AutismSessionState.DelayUnit mode) {
      this.init_ck_DataUtil = mode;
   }

   public void flush_V() {
      this.init_ck_DataUtil = this.init_ck_DataUtil == AutismSessionState.DelayUnit.TICKS ? AutismSessionState.DelayUnit.MS : AutismSessionState.DelayUnit.TICKS;
   }

   public boolean stop_Z() {
      return this.get_Z;
   }

   public void get(boolean visible) {
      this.get_Z = visible;
   }

   public int sync_I() {
      return this.close_I;
   }

   public void write(int x) {
      this.close_I = x;
   }

   public int flush_I() {
      return this.open_I;
   }

   public void update(int y) {
      this.open_I = y;
   }

   public String read_Str() {
      return this.read_Str;
   }

   public void update(String value) {
      this.read_Str = value != null ? value : "0";
   }

   public String write_Str() {
      return this.write_Str;
   }

   public void sync(String value) {
      this.write_Str = value != null ? value : "";
   }

   public String update_Str() {
      return this.update_Str;
   }

   public void flush(String value) {
      this.update_Str = value != null ? value : "1";
   }

   public int close_I() {
      return this.poll_I;
   }

   public void sync(int index) {
      this.poll_I = Math.max(0, index);
   }

   public boolean next_Z() {
      return this.put_Z;
   }

   public void set(boolean useMaxAmount) {
      this.put_Z = useMaxAmount;
   }

   public int open_I() {
      return this.push_I;
   }

   public void flush(int index) {
      this.push_I = Math.max(0, index);
   }

   public boolean prev_Z() {
      return this.set_Z;
   }

   public void put(boolean value) {
      this.set_Z = value;
   }

   public String sync_Str() {
      return this.sync_Str;
   }

   public void close(String value) {
      this.sync_Str = value != null ? value : "";
   }

   public int poll_I() {
      return this.pop_I;
   }

   public void close(int recipeId) {
      this.pop_I = recipeId;
   }

   public String flush_Str() {
      return this.flush_Str;
   }

   public void open(String recipeKey) {
      this.flush_Str = recipeKey != null ? recipeKey : "";
   }

   public int push_I() {
      return this.tick_I;
   }

   public void open(int offset) {
      this.tick_I = Math.max(0, offset);
   }

   public synchronized List<autismclient.util.macro.CraftAction.Inner_DataUtil> update_List() {
      List<autismclient.util.macro.CraftAction.Inner_DataUtil> copies = new ArrayList<>(this.update_List.size());

      for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : this.update_List) {
         if (entry != null) {
            copies.add(entry.init_SyncManager_DataUt());
         }
      }

      return copies;
   }

   public synchronized void read_V_List(List<autismclient.util.macro.CraftAction.Inner_DataUtil> entries) {
      this.update_List.clear();
      if (entries != null) {
         for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : entries) {
            if (entry != null) {
               this.update_List.add(entry.init_SyncManager_DataUt());
            }
         }
      }
   }

   public int pop_I() {
      return this.run_I;
   }

   public void poll(int index) {
      this.run_I = index;
   }

   public int tick_I() {
      return this.get_I;
   }

   public void push(int offset) {
      this.get_I = Math.max(0, offset);
   }

   public boolean valid_Z() {
      return this.add_Z;
   }

   public void add(boolean visible) {
      this.add_Z = visible;
   }

   public int run_I() {
      return this.set_I;
   }

   public void pop(int x) {
      this.set_I = x;
   }

   public int get_I() {
      return this.put_I;
   }

   public void tick(int y) {
      this.put_I = y;
   }

   public int set_I() {
      return this.add_I;
   }

   public void run(int activeTab) {
      this.add_I = Math.max(0, activeTab);
   }

   public int put_I() {
      return this.clear_I;
   }

   public void get(int scrollOffset) {
      this.clear_I = Math.max(0, scrollOffset);
   }

   public boolean size_Z() {
      return this.clear_Z;
   }

   public void clear(boolean perUserMode) {
      this.clear_Z = perUserMode;
   }

   public String close_Str() {
      return this.close_Str;
   }

   public void poll(String selectedMacroName) {
      this.close_Str = selectedMacroName == null ? "" : selectedMacroName;
   }

   public String open_Str() {
      return this.open_Str;
   }

   public void push(String expandedExecutePeer) {
      this.open_Str = expandedExecutePeer == null ? "" : expandedExecutePeer;
   }

   public String poll_Str() {
      return this.poll_Str;
   }

   public void pop(String selectedPeer) {
      this.poll_Str = selectedPeer == null ? "" : selectedPeer;
   }

   public boolean data_Z() {
      return this.reset_Z;
   }

   public void reset(boolean visible) {
      this.reset_Z = visible;
   }

   public int add_I() {
      return this.reset_I;
   }

   public void set(int x) {
      this.reset_I = x;
   }

   public int clear_I() {
      return this.start_I;
   }

   public void put(int y) {
      this.start_I = y;
   }

   public int reset_I() {
      return this.stop_I;
   }

   public void add(int scrollOffset) {
      this.stop_I = Math.max(0, scrollOffset);
   }

   public String push_Str() {
      return this.push_Str;
   }

   public void tick(String search) {
      this.push_Str = search == null ? "" : search;
   }

   public boolean value_Z() {
      return this.start_Z;
   }

   public void start(boolean visible) {
      this.start_Z = visible;
   }

   public int start_I() {
      return this.next_I;
   }

   public void clear(int x) {
      this.next_I = x;
   }

   public int stop_I() {
      return this.prev_I;
   }

   public void reset(int y) {
      this.prev_I = y;
   }

   public int next_I() {
      return this.valid_I;
   }

   public void start(int tab) {
      this.valid_I = Math.max(0, tab);
   }

   public int prev_I() {
      return this.size_I;
   }

   public void stop(int offset) {
      this.size_I = Math.max(0, offset);
   }

   public String pop_Str() {
      return this.pop_Str;
   }

   public void run(String plugin) {
      this.pop_Str = plugin == null ? "" : plugin;
   }

   public String tick_Str() {
      return this.tick_Str;
   }

   public void get(String address) {
      this.tick_Str = load_Str_Str(address);
   }

   public String init_Str_Str(String address) {
      String normalizedAddress = load_Str_Str(address);
      if (this.run_Str == null || this.run_Str.isBlank()) {
         return "";
      } else if (normalizedAddress.isEmpty()) {
         return this.run_Str;
      } else {
         String normalizedStored = load_Str_Str(this.get_Str);
         if (normalizedStored.isEmpty()) {
            return this.run_Str;
         } else {
            String addrNoPort = this.write_Str(normalizedAddress);
            String storedNoPort = this.write_Str(normalizedStored);
            if (addrNoPort.equals(storedNoPort)) {
               return this.run_Str;
            } else {
               return normalizedAddress.equals(normalizedStored) ? this.run_Str : "";
            }
         }
      }
   }

   private String write_Str(String address) {
      if (address != null && !address.isBlank()) {
         int colonIdx = address.lastIndexOf(58);
         if (colonIdx >= 0) {
            String potentialPort = address.substring(colonIdx + 1);
            if (potentialPort.matches("\\d+")) {
               return address.substring(0, colonIdx);
            }
         }

         return address;
      } else {
         return "";
      }
   }

   public void init_V_Str_Str(String address, String version) {
      String normalizedAddress = load_Str_Str(address);
      this.get_Str = normalizedAddress;
      this.run_Str = version == null ? "" : version.trim();
   }

   public void close_V() {
      this.run_Str = "";
      this.get_Str = "";
   }

   public int valid_I() {
      return this.data_I;
   }

   public void next(int delayMs) {
      this.data_I = Math.max(10, Math.min(500, delayMs));
   }

   public int size_I() {
      return this.value_I;
   }

   public void prev(int width) {
      this.value_I = Math.max(252, width);
   }

   public int data_I() {
      return this.cache_I;
   }

   public void valid(int height) {
      this.cache_I = Math.max(258, height);
   }

   public int value_I() {
      return this.state_I;
   }

   public void size(int width) {
      this.state_I = Math.max(280, width);
   }

   public int cache_I() {
      return this.queue_I;
   }

   public void data(int height) {
      this.queue_I = Math.max(280, height);
   }

   public void init_V_Runnable(Runnable onCaptureComplete) {
      this.prev_Z = true;
      this.node = 0;
      this.sync_List.clear();
      this.init_Runnable = onCaptureComplete;
   }

   public boolean cache_Z() {
      return this.prev_Z;
   }

   public void read_V_Packet(Packet<?> packet) {
      if (this.prev_Z) {
         this.node++;
         if (size_Z) {
            AutismClientAddon.LOG.info("[GBreak] Packet #{}: {}", this.node, packet.getClass().getSimpleName());
         }

         if (this.node == 2) {
            this.sync_List.clear();
            this.sync_List.add(new AutismSessionState.DelayedPacket(packet, 0));
            this.put_V();
         }
      }
   }

   public List<AutismSessionState.DelayedPacket> sync_List() {
      return new ArrayList<>(this.sync_List);
   }

   private void put_V() {
      this.prev_Z = false;
      if (this.init_Runnable != null) {
         this.init_Runnable.run();
         this.init_Runnable = null;
      }
   }

   public void open_V() {
      this.prev_Z = false;
      this.node = 0;
      this.sync_List.clear();
      this.init_Runnable = null;
   }

   public void stop(boolean v) {
      this.next(v);
   }

   public void next(boolean v) {
      this.data_Z = v;
   }

   public void prev(boolean v) {
      this.value_Z = v;
   }

   public void valid(boolean v) {
      this.cache_Z = v;
   }

   public void size(boolean v) {
      this.state_Z = v;
   }

   public void data(boolean v) {
      this.queue_Z = v;
   }

   public boolean state_Z() {
      boolean v = this.data_Z;
      this.data_Z = false;
      return v;
   }

   public boolean queue_Z() {
      return this.state_Z();
   }

   public boolean buffer_Z() {
      boolean v = this.value_Z;
      this.value_Z = false;
      return v;
   }

   public boolean entry() {
      boolean v = this.cache_Z;
      this.cache_Z = false;
      return v;
   }

   public boolean node() {
      boolean v = this.state_Z;
      this.state_Z = false;
      return v;
   }

   public boolean task() {
      boolean v = this.queue_Z;
      this.queue_Z = false;
      return v;
   }

   public void init_V_Consumer(Consumer<BlockPos> cb) {
      this.init_Consumer = cb;
   }

   public void init_V_BiConsumer(BiConsumer<BlockPos, Direction> cb) {
      this.init_BiConsumer = cb;
   }

   public boolean init_Z_BlockPos(BlockPos pos) {
      return this.init_Z_BlockPos_Direction(pos, Direction.UP);
   }

   public boolean init_Z_BlockPos_Direction(BlockPos pos, Direction direction) {
      BiConsumer<BlockPos, Direction> dcb = this.init_BiConsumer;
      if (dcb != null) {
         this.init_BiConsumer = null;
         dcb.accept(pos, direction == null ? Direction.UP : direction);
         return true;
      } else {
         Consumer<BlockPos> cb = this.init_Consumer;
         if (cb != null) {
            this.init_Consumer = null;
            cb.accept(pos);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean pool() {
      return this.init_Consumer != null || this.init_BiConsumer != null;
   }

   public void load_V_Runnable(Runnable cb) {
      this.load_Runnable = cb;
   }

   public boolean index() {
      Runnable cb = this.load_Runnable;
      if (cb != null) {
         this.load_Runnable = null;
         cb.run();
         return true;
      } else {
         return false;
      }
   }

   public boolean count() {
      return this.load_Runnable != null;
   }

   public void read_V_Runnable(Runnable cb) {
      this.read_Runnable = cb;
   }

   public boolean flags() {
      Runnable cb = this.read_Runnable;
      if (cb != null) {
         this.read_Runnable = null;
         cb.run();
         return true;
      } else {
         return false;
      }
   }

   public boolean level() {
      return this.read_Runnable != null;
   }

   public void load_V_Consumer(Consumer<String> cb) {
      this.load_Consumer = cb;
   }

   public boolean load_Z_Str(String payload) {
      Consumer<String> cb = this.load_Consumer;
      if (cb != null) {
         this.load_Consumer = null;
         cb.accept(payload);
         return true;
      } else {
         return false;
      }
   }

   public boolean init_Z_Entity(Entity targeted) {
      if (targeted != null && this.load_Consumer != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && targeted == mc.player) {
            return false;
         }

         String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(targeted.getType()).toString();
         String payload;
         if (this.buffer_Z) {
            String name = targeted.getDisplayName().getString().replaceAll("§.", "").trim();
            payload = "~" + targeted.getStringUUID() + "~" + typeId + "~" + name;
            this.buffer_Z = false;
         } else {
            payload = typeId;
         }

         return this.load_Z_Str(payload);
      } else {
         return false;
      }
   }

   public boolean frame() {
      return this.load_Consumer != null;
   }

   public void value(boolean v) {
      this.buffer_Z = v;
   }

   public boolean block() {
      return this.buffer_Z;
   }

   public boolean chunk() {
      return this.valid_Z;
   }

   public void cache(boolean v) {
      if (!v || !autismclient.modules.aD.read_Z()) {
         this.valid_Z = v;
         if (v) {
            this.read_J = -1L;
            this.task = -1;
         }
      }
   }

   public void poll_V() {
      long now = System.currentTimeMillis();
      if (this.write_J > 0L) {
         long wallDeltaMs = now - this.write_J;
         if (wallDeltaMs > 0L) {
            double measured = wallDeltaMs / 20.0;
            measured = Math.max(50.0, Math.min(200.0, measured));
            this.init_D = this.init_D * 0.3 + measured * 0.7;
         }
      }

      this.write_J = now;
   }

   public double init_D() {
      return this.init_D;
   }

   public double load_D() {
      return this.write_J <= 0L ? -1.0 : 1000.0 / this.init_D;
   }

   public void push_V() {
      this.pool++;
   }

   public int state_I() {
      return this.pool;
   }

   public boolean table() {
      return this.entry_Z;
   }

   public void state(boolean active) {
      this.entry_Z = active;
   }

   public enum ReplayMode {
      REGENERATE,
      EXACT;
   }

   public static class DelayedPacket {
      private static final AtomicInteger init_AtomicInteger = new AtomicInteger(1);
      public final Packet<?> init_Packet;
      public final int init_I;
      private volatile AutismSessionState.ReplayMode init_ck_BufferHelper;
      private volatile int load;
      private volatile boolean init_Z;

      public DelayedPacket(Packet<?> packet, int delay) {
         this(packet, delay, AutismSessionState.ReplayMode.REGENERATE);
      }

      public DelayedPacket(Packet<?> packet, int delay, AutismSessionState.ReplayMode replayMode) {
         this.init_Packet = packet;
         this.init_I = init_AtomicInteger.getAndIncrement();
         this.load = delay;
         this.init_Z = false;
         this.init_ck_BufferHelper = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      }

      public DelayedPacket(Packet<?> packet, int delay, int existingId) {
         this(packet, delay, existingId, AutismSessionState.ReplayMode.REGENERATE);
      }

      public DelayedPacket(Packet<?> packet, int delay, int existingId, AutismSessionState.ReplayMode replayMode) {
         this.init_Packet = packet;
         this.init_I = existingId;
         this.load = delay;
         this.init_Z = false;
         this.init_ck_BufferHelper = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      }

      public int init_I() {
         return this.init_I;
      }

      public int load_I() {
         return this.load;
      }

      public AutismSessionState.ReplayMode init_ck_BufferHelper() {
         return this.init_ck_BufferHelper;
      }

      public boolean init_Z() {
         return this.init_ck_BufferHelper == AutismSessionState.ReplayMode.EXACT;
      }

      public synchronized void init(int newDelay) {
         if (!this.init_Z) {
            this.load = newDelay;
         }
      }

      public synchronized void init(AutismSessionState.ReplayMode mode) {
         if (!this.init_Z) {
            this.init_ck_BufferHelper = mode == null ? AutismSessionState.ReplayMode.REGENERATE : mode;
         }
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      synchronized void init_V() {
         this.init_Z = true;
      }

      public static void load_V() {
         init_AtomicInteger.set(1);
      }
   }

   public static final class TimingMeasurement {
      public final String init_Str;
      public final long init_J;
      public final String load_Str;
      public final String read;
      public final long load_J;

      public TimingMeasurement(String label, long deltaMs, String startEvent, String endEvent, long capturedAtMs) {
         this.init_Str = label == null ? "" : label;
         this.init_J = deltaMs;
         this.load_Str = startEvent == null ? "" : startEvent;
         this.read = endEvent == null ? "" : endEvent;
         this.load_J = capturedAtMs;
      }
   }

   public enum DelayUnit {
      TICKS,
      MS;
   }

   public record DelayQueueSnapshot(List<AutismSessionState.DelayedPacket> init_List, int init_I) {
   }

   public static final class PluginScanResult {
      private final String init_Str;
      private final String load_Str;
      private final List<String> init_List;
      private final Map<String, List<String>> init_Map;
      private final Map<String, String> load_Map;

      private PluginScanResult(
         String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands, Map<String, String> pluginEvidence
      ) {
         this.init_Str = address;
         this.load_Str = contextSignature == null ? "" : contextSignature;
         this.init_List = plugins == null ? List.of() : new ArrayList<>(plugins);
         this.init_Map = init(pluginCommands);
         this.load_Map = load(pluginEvidence);
      }

      public String init_Str() {
         return this.init_Str;
      }

      public List<String> init_List() {
         return new ArrayList<>(this.init_List);
      }

      public String load_Str() {
         return this.load_Str;
      }

      public Map<String, List<String>> init_Map() {
         return init(this.init_Map);
      }

      public Map<String, String> load_Map() {
         return load(this.load_Map);
      }

      private AutismSessionState.PluginScanResult init_ck_TaskQueue() {
         return new AutismSessionState.PluginScanResult(this.init_Str, this.load_Str, this.init_List, this.init_Map, this.load_Map);
      }

      private static Map<String, List<String>> init(Map<String, List<String>> source) {
         if (source != null && !source.isEmpty()) {
            Map<String, List<String>> copy = new LinkedHashMap<>();

            for (Entry<String, List<String>> entry : source.entrySet()) {
               if (entry.getKey() != null) {
                  copy.put(entry.getKey(), entry.getValue() == null ? List.of() : new ArrayList<>(entry.getValue()));
               }
            }

            return copy;
         } else {
            return Collections.emptyMap();
         }
      }

      private static Map<String, String> load(Map<String, String> source) {
         if (source != null && !source.isEmpty()) {
            Map<String, String> copy = new LinkedHashMap<>();

            for (Entry<String, String> entry : source.entrySet()) {
               if (entry.getKey() != null && !entry.getKey().isBlank()) {
                  copy.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
               }
            }

            return copy;
         } else {
            return Collections.emptyMap();
         }
      }
   }
}
