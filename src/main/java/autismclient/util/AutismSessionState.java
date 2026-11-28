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
   private static final AutismSessionState INSTANCE = new AutismSessionState();
   private volatile boolean sendPacketsEnabled = true;
   private volatile boolean delayPacketsEnabled;
   private volatile boolean allowSignEditing = true;
   private volatile boolean allowBookEditing = true;
   private volatile boolean pretendPackAccepted;
   private volatile boolean autoDenyResourcePack;
   private volatile boolean xCarryActive;
   private volatile boolean xCarryForced;
   private volatile boolean armorSlotBypassEnabled;
   private volatile Set<Integer> xCarrySlotMask = Collections.emptySet();
   private volatile boolean xCarryCarryCursor;
   private volatile BlockPos lastInteractedBlockPos = null;
   private volatile String lastSentMessage = "";
   private volatile InteractionTargetRef lastInteractionTarget = null;
   private volatile boolean useCustomPackets = false;
   private final Set<Class<? extends Packet<?>>> customC2sPackets = ConcurrentHashMap.newKeySet();
   private final Set<Class<? extends Packet<?>>> customS2cPackets = ConcurrentHashMap.newKeySet();
   private final List<AutismSessionState.DelayedPacket> delayedPackets = new CopyOnWriteArrayList<>();
   private volatile boolean staggeredPacketSend = false;
   private volatile int staggeredSendDelay = 1;
   private final List<AutismSessionState.DelayedPacket> sendingPackets = new CopyOnWriteArrayList<>();
   private final List<AutismSessionState.DelayedPacket> sendingDisplayPackets = new CopyOnWriteArrayList<>();
   private final List<AutismSessionState.DelayedPacket> lastFlushedPackets = new CopyOnWriteArrayList<>();
   private final AtomicInteger queueRevision = new AtomicInteger();
   private int staggeredTickCounter = 0;
   private volatile ClientPacketListener staggeredSendConnection = null;
   private int staggeredSendTotalCount = 0;
   private boolean staggeredHasExplicitDelays = false;
   private static final int MAX_TIMING_SAMPLES = 32;
   private final Deque<AutismSessionState.TimingMeasurement> timingMeasurements = new ArrayDeque<>(32);
   private volatile AutismSessionState.DelayUnit delayUnit = AutismSessionState.DelayUnit.TICKS;
   private AutismSessionState.DelayUnit activeSendDelayUnit = AutismSessionState.DelayUnit.TICKS;
   private long staggeredStartNanos = 0L;
   private int staggeredStartTick = 0;
   private long liveCaptureLastNanos = -1L;
   private int liveCaptureLastTick = -1;
   private int liveCaptureAccumulatedDelay = 0;
   private String staggeredCompletionMessage = null;
   private Screen storedGuiScreen;
   private AbstractContainerMenu storedContainerMenu;
   private boolean bypassingPacketInterception = false;
   private boolean fabricatorVisible = false;
   private int fabricatorX = 500;
   private int fabricatorY = 5;
   private String fabricatorSlotInput = "0";
   private String fabricatorItemInput = "";
   private String fabricatorCountInput = "1";
   private int fabricatorActionIndex = 0;
   private int fabricatorClickModeIndex = 0;
   private boolean fabricatorDropWholeStack = false;
   private boolean fabricatorUseMaxAmount = false;
   private String fabricatorRecipeSearch = "";
   private int fabricatorSelectedRecipeId = -1;
   private String fabricatorSelectedRecipeKey = "";
   private int fabricatorRecipeScrollOffset = 0;
   private final List<autismclient.util.macro.CraftAction.CraftEntry> fabricatorCraftEntries = new ArrayList<>();
   private int fabricatorSelectedCraftIndex = -1;
   private int fabricatorCraftScrollOffset = 0;
   private boolean lanSyncOverlayVisible = false;
   private int lanSyncOverlayX = 500;
   private int lanSyncOverlayY = 5;
   private int lanSyncActiveTab = 0;
   private int lanSyncScrollOffset = 0;
   private boolean lanSyncPerUserMode = false;
   private String lanSyncSelectedMacroName = "";
   private String lanSyncExpandedExecutePeer = "";
   private String lanSyncSelectedPeer = "";
   private boolean macroListOverlayVisible = false;
   private int macroListOverlayX = 500;
   private int macroListOverlayY = 250;
   private int macroListScrollOffset = 0;
   private String macroListSearchQuery = "";
   private boolean queueEditorVisible = false;
   private int queueEditorX = 100;
   private int queueEditorY = 100;
   private int serverInfoActiveTab = 0;
   private int serverInfoScrollOffset = 0;
   private String serverInfoSelectedPlugin = "";
   private String serverInfoStateAddress = "";
   private volatile String serverDetectedVersion = "";
   private volatile String serverDetectedVersionAddress = "";
   private int serverInfoScanDelayMs = 50;
   private int serverInfoTabWidth = 252;
   private int serverInfoTabHeight = 258;
   private int serverInfoPluginsTabWidth = 280;
   private int serverInfoPluginsTabHeight = 280;
   private boolean macroEditorVisible = false;
   private AutismMacro editingMacro = null;
   private int macroEditorX = 400;
   private int macroEditorY = 100;
   private boolean launcherOverlayVisible = false;
   private volatile boolean gbreakCapturing = false;
   private volatile int gbreakPacketCount = 0;
   private Runnable gbreakCaptureCallback = null;
   private final List<AutismSessionState.DelayedPacket> gbreakCapturedPackets = new ArrayList<>();
   private volatile boolean timedCaptureMode = false;
   private volatile long captureStartNanos = -1L;
   private volatile int captureStartTick = -1;
   private volatile int clientTick = 0;
   private volatile double msPerTickEma = 50.0;
   private volatile long lastTimeUpdateMs = -1L;
   private final Map<String, OverlayBounds> overlayBoundsById = new HashMap<>();
   private final Set<String> pinnedOverlayIds = ConcurrentHashMap.newKeySet();
   private final Map<String, AutismSessionState.PluginScanResult> pluginScanResults = new HashMap<>();
   private final List<String> overlayZOrder = new ArrayList<>();
   private String activeOverlayId = "";
   private static final boolean TRACE_PAYLOADS = Boolean.getBoolean("autism.payload.trace");
   private volatile boolean dropNextContainerClose = false;
   private volatile boolean dropNextSignUpdate = false;
   private volatile boolean dropNextBookEdit = false;
   private volatile boolean forceNextSignUpdate = false;
   private volatile boolean forceNextBookEdit = false;
   private volatile Consumer<BlockPos> blockCaptureCallback = null;
   private volatile BiConsumer<BlockPos, Direction> blockFaceCaptureCallback = null;
   private volatile Runnable attackCaptureCallback = null;
   private volatile Runnable captureCancelCallback = null;
   private volatile Consumer<String> entityCaptureCallback = null;
   private volatile boolean captureSpecificEntity = false;
   private volatile boolean placePreviewActive = false;

   public synchronized void recordTimingMeasurement(AutismSessionState.TimingMeasurement sample) {
      if (sample != null) {
         this.timingMeasurements.addFirst(sample);

         while (this.timingMeasurements.size() > 32) {
            this.timingMeasurements.removeLast();
         }
      }
   }

   public synchronized AutismSessionState.TimingMeasurement getLatestTimingMeasurement(String label) {
      if (label == null) {
         return this.timingMeasurements.isEmpty() ? null : this.timingMeasurements.peekFirst();
      }

      for (AutismSessionState.TimingMeasurement s : this.timingMeasurements) {
         if (label.equals(s.label)) {
            return s;
         }
      }

      return null;
   }

   public synchronized List<AutismSessionState.TimingMeasurement> getRecentTimingMeasurements(int n) {
      List<AutismSessionState.TimingMeasurement> out = new ArrayList<>(Math.min(n, this.timingMeasurements.size()));
      int i = 0;

      for (AutismSessionState.TimingMeasurement s : this.timingMeasurements) {
         if (i++ >= n) {
            break;
         }

         out.add(s);
      }

      return out;
   }

   private AutismSessionState() {
   }

   public static AutismSessionState getInstance() {
      return INSTANCE;
   }

   public boolean isMacroEditorVisible() {
      return this.macroEditorVisible;
   }

   public void setMacroEditorVisible(boolean visible) {
      this.macroEditorVisible = visible;
   }

   public AutismMacro getEditingMacro() {
      return this.editingMacro;
   }

   public void setEditingMacro(AutismMacro macro) {
      this.editingMacro = macro;
   }

   public int getMacroEditorX() {
      return this.macroEditorX;
   }

   public void setMacroEditorX(int x) {
      this.macroEditorX = x;
   }

   public int getMacroEditorY() {
      return this.macroEditorY;
   }

   public void setMacroEditorY(int y) {
      this.macroEditorY = y;
   }

   public synchronized OverlayBounds getOverlayBounds(String id) {
      OverlayBounds layout = this.overlayBoundsById.get(id);
      return layout == null ? null : new OverlayBounds(layout.x, layout.y, layout.width, layout.height, layout.visible, layout.collapsed);
   }

   public boolean isOverlayPinned(String id) {
      return id != null && this.pinnedOverlayIds.contains(id);
   }

   public void setOverlayPinned(String id, boolean pinned) {
      if (id != null && !id.isEmpty()) {
         if (pinned) {
            this.pinnedOverlayIds.add(id);
         } else {
            this.pinnedOverlayIds.remove(id);
         }
      }
   }

   public synchronized void setOverlayBounds(String id, OverlayBounds layout) {
      if (id != null && !id.isEmpty() && layout != null) {
         OverlayBounds trueGeometry = OverlayManager.getInstance().getStashedBounds(id);
         if (trueGeometry != null) {
            layout = new OverlayBounds(trueGeometry.x, trueGeometry.y, trueGeometry.width, trueGeometry.height, layout.visible, layout.collapsed);
         }

         this.overlayBoundsById.put(id, new OverlayBounds(layout.x, layout.y, layout.width, layout.height, layout.visible, layout.collapsed));
      }
   }

   public synchronized List<String> getOverlayZOrder() {
      return new ArrayList<>(this.overlayZOrder);
   }

   public synchronized void setOverlayZOrder(List<String> order) {
      this.overlayZOrder.clear();
      if (order != null) {
         for (String id : order) {
            if (id != null && !id.isEmpty() && !this.overlayZOrder.contains(id)) {
               this.overlayZOrder.add(id);
            }
         }
      }
   }

   public synchronized String getActiveOverlayId() {
      return this.activeOverlayId == null ? "" : this.activeOverlayId;
   }

   public synchronized void setActiveOverlayId(String overlayId) {
      this.activeOverlayId = overlayId == null ? "" : overlayId.trim();
   }

   public synchronized AutismSessionState.PluginScanResult getPluginScanResult(String address) {
      return this.getPluginScanResult(address, "");
   }

   public synchronized AutismSessionState.PluginScanResult getPluginScanResult(String address, String contextSignature) {
      String key = buildPluginScanKey(address, contextSignature);
      if (key.isEmpty()) {
         return null;
      }

      AutismSessionState.PluginScanResult scan = this.pluginScanResults.get(key);
      return scan == null ? null : scan.copy();
   }

   public synchronized void savePluginScanResult(String address, List<String> plugins, Map<String, List<String>> pluginCommands) {
      this.savePluginScanResult(address, "", plugins, pluginCommands, Map.of());
   }

   public synchronized void savePluginScanResult(String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands) {
      this.savePluginScanResult(address, contextSignature, plugins, pluginCommands, Map.of());
   }

   public synchronized void savePluginScanResult(
      String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands, Map<String, String> pluginEvidence
   ) {
      String normalizedAddress = normalizeServerAddress(address);
      String normalizedContext = normalizeContextSignature(contextSignature);
      String key = buildPluginScanKey(normalizedAddress, normalizedContext);
      if (!key.isEmpty()) {
         this.pluginScanResults.put(key, new AutismSessionState.PluginScanResult(normalizedAddress, normalizedContext, plugins, pluginCommands, pluginEvidence));
      }
   }

   public synchronized void clearPluginScanResults(String address) {
      String normalized = normalizeServerAddress(address);
      if (!normalized.isEmpty()) {
         this.pluginScanResults.keySet().removeIf(key -> key.equals(normalized) || key.startsWith(normalized + "|"));
      }
   }

   private static String normalizeServerAddress(String address) {
      return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
   }

   private static String normalizeContextSignature(String contextSignature) {
      return contextSignature == null ? "" : contextSignature.trim().toLowerCase(Locale.ROOT);
   }

   private static String buildPluginScanKey(String address, String contextSignature) {
      String normalizedAddress = normalizeServerAddress(address);
      if (normalizedAddress.isEmpty()) {
         return "";
      }

      String normalizedContext = normalizeContextSignature(contextSignature);
      return normalizedContext.isEmpty() ? normalizedAddress + "|default" : normalizedAddress + "|" + normalizedContext;
   }

   public boolean isLauncherOverlayVisible() {
      return this.launcherOverlayVisible;
   }

   public void setLauncherOverlayVisible(boolean visible) {
      this.launcherOverlayVisible = visible;
   }

   public boolean isSendPacketsEnabled() {
      return this.sendPacketsEnabled;
   }

   public void setSendPacketsEnabled(boolean value) {
      this.sendPacketsEnabled = value;
   }

   public boolean isDelayPacketsEnabled() {
      return this.delayPacketsEnabled;
   }

   public void setDelayPacketsEnabled(boolean value) {
      if (!value || !autismclient.modules.aD.read_Z()) {
         this.delayPacketsEnabled = value;
      }
   }

   public boolean isXCarryActive() {
      return this.xCarryActive;
   }

   public void setXCarryActive(boolean value) {
      this.xCarryActive = value;
   }

   public boolean isXCarryForced() {
      return this.xCarryForced;
   }

   public void setXCarryForced(boolean value) {
      if (!value || this.xCarrySlotMask != null && !this.xCarrySlotMask.isEmpty() || this.xCarryCarryCursor) {
         this.xCarryForced = value;
         if (!value) {
            this.xCarrySlotMask = Collections.emptySet();
            this.xCarryCarryCursor = false;
         }
      }
   }

   public Set<Integer> getXCarrySlotMask() {
      return this.xCarrySlotMask;
   }

   public boolean isXCarryCarryCursor() {
      return this.xCarryCarryCursor;
   }

   public void setXCarrySlotMask(Set<Integer> slotMask, boolean carryCursor) {
      LinkedHashSet<Integer> copy = new LinkedHashSet<>();
      if (slotMask != null) {
         for (Integer slot : slotMask) {
            if (slot != null && slot >= 0) {
               copy.add(slot);
            }
         }
      }

      this.xCarrySlotMask = Collections.unmodifiableSet(copy);
      this.xCarryCarryCursor = carryCursor;
   }

   public void mergeXCarrySlotMask(Set<Integer> slotMask, boolean carryCursor) {
      LinkedHashSet<Integer> merged = new LinkedHashSet<>();
      if (this.xCarryForced && this.xCarrySlotMask != null) {
         for (Integer slot : this.xCarrySlotMask) {
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

      this.xCarrySlotMask = Collections.unmodifiableSet(merged);
      this.xCarryCarryCursor = this.xCarryForced && this.xCarryCarryCursor || carryCursor;
   }

   public boolean isArmorSlotBypassEnabled() {
      return this.armorSlotBypassEnabled;
   }

   public void setArmorSlotBypassEnabled(boolean value) {
      this.armorSlotBypassEnabled = value;
   }

   public String getLastSentMessage() {
      return this.lastSentMessage == null ? "" : this.lastSentMessage;
   }

   public void setLastSentMessage(String message) {
      this.lastSentMessage = message == null ? "" : message;
   }

   public BlockPos getLastInteractedBlockPos() {
      return this.lastInteractedBlockPos;
   }

   public void setLastInteractedBlockPos(BlockPos pos) {
      this.lastInteractedBlockPos = pos;
   }

   public InteractionTargetRef getLastInteractionTarget() {
      return this.lastInteractionTarget;
   }

   public void setLastInteractionTarget(InteractionTargetRef target) {
      this.lastInteractionTarget = target;
      if (target != null && target.isBlock()) {
         this.lastInteractedBlockPos = target.blockPos();
      }
   }

   public synchronized List<AutismSessionState.DelayedPacket> getDelayedPackets() {
      return new ArrayList<>(this.delayedPackets);
   }

   public int getQueueRevision() {
      return this.queueRevision.get();
   }

   public synchronized AutismSessionState.DelayQueueSnapshot snapshotDelayQueue(boolean sending, int maxLines) {
      List<AutismSessionState.DelayedPacket> source = sending ? this.sendingPackets : this.delayedPackets;
      int totalCount = source.size();
      if (totalCount != 0 && maxLines > 0) {
         int limit = Math.min(totalCount, maxLines);
         ArrayList<AutismSessionState.DelayedPacket> visiblePackets = new ArrayList<>(limit);

         for (AutismSessionState.DelayedPacket packet : source) {
            if (packet != null && packet.packet != null) {
               int insertAt = visiblePackets.size();

               for (int i = 0; i < visiblePackets.size(); i++) {
                  AutismSessionState.DelayedPacket existing = visiblePackets.get(i);
                  int byDelay = Integer.compare(existing.getDelay(), packet.getDelay());
                  if (byDelay > 0 || byDelay == 0 && existing.getId() > packet.getId()) {
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

   public boolean hasDelayedPackets() {
      return !this.delayedPackets.isEmpty();
   }

   public synchronized void replaceDelayedPackets(List<AutismSessionState.DelayedPacket> packets) {
      this.delayedPackets.clear();
      if (packets != null && !packets.isEmpty()) {
         this.delayedPackets.addAll(this.cloneAndRenumberQueue(packets));
      }

      this.resetPacketIdsIfEmpty();
      this.bumpQueueRevision();
   }

   public synchronized void clearDelayedPackets() {
      if (!this.delayedPackets.isEmpty()) {
         this.archiveCurrentDelayedQueue();
      }

      this.delayedPackets.clear();
      this.resetPacketIdsIfEmpty();
      this.bumpQueueRevision();
   }

   private void resetPacketIdsIfEmpty() {
      if (this.delayedPackets.isEmpty() && this.sendingPackets.isEmpty()) {
         this.resetTimedCaptureOrigin();
         AutismSessionState.DelayedPacket.resetIdCounter();
      }
   }

   private void resetTimedCaptureOrigin() {
      this.captureStartNanos = -1L;
      this.captureStartTick = -1;
   }

   private void archiveCurrentDelayedQueue() {
      this.lastFlushedPackets.clear();
      if (!this.delayedPackets.isEmpty()) {
         this.lastFlushedPackets.addAll(this.cloneAndRenumberQueue(this.delayedPackets));
      }
   }

   private void archivePacketQueue(List<AutismSessionState.DelayedPacket> queue) {
      this.lastFlushedPackets.clear();
      if (queue != null && !queue.isEmpty()) {
         this.lastFlushedPackets.addAll(this.cloneAndRenumberQueue(queue));
      }
   }

   private void resetStaggeredSendState() {
      this.sendingPackets.clear();
      this.sendingDisplayPackets.clear();
      this.staggeredSendConnection = null;
      this.staggeredTickCounter = 0;
      this.staggeredStartNanos = 0L;
      this.staggeredStartTick = 0;
      this.staggeredSendTotalCount = 0;
      this.staggeredHasExplicitDelays = false;
      this.liveCaptureLastNanos = -1L;
      this.liveCaptureLastTick = -1;
      this.liveCaptureAccumulatedDelay = 0;
      this.activeSendDelayUnit = this.delayUnit;
      this.bumpQueueRevision();
   }

   private void bumpQueueRevision() {
      this.queueRevision.incrementAndGet();
   }

   private boolean isActivelySendingStaggered() {
      return this.staggeredSendConnection != null && !this.sendingPackets.isEmpty();
   }

   private int computeStaggeredElapsed(AutismSessionState.DelayUnit mode) {
      if (mode == AutismSessionState.DelayUnit.MS) {
         return this.staggeredStartNanos <= 0L ? 0 : (int)Math.max(0L, (System.nanoTime() - this.staggeredStartNanos) / 1000000L);
      } else {
         return Math.max(0, this.clientTick - this.staggeredStartTick);
      }
   }

   private int findMaxPendingDelay(List<AutismSessionState.DelayedPacket> queue) {
      int maxDelay = 0;
      if (queue != null && !queue.isEmpty()) {
         for (AutismSessionState.DelayedPacket qp : queue) {
            if (qp != null && !qp.isSent()) {
               maxDelay = Math.max(maxDelay, qp.getDelay());
            }
         }

         return maxDelay;
      } else {
         return maxDelay;
      }
   }

   private void insertSortedByDelay(List<AutismSessionState.DelayedPacket> queue, AutismSessionState.DelayedPacket packet) {
      if (queue != null && packet != null) {
         int insertAt = queue.size();

         for (int i = 0; i < queue.size(); i++) {
            AutismSessionState.DelayedPacket existing = queue.get(i);
            if (existing != null) {
               int byDelay = Integer.compare(existing.getDelay(), packet.getDelay());
               if (byDelay > 0 || byDelay == 0 && existing.getId() > packet.getId()) {
                  insertAt = i;
                  break;
               }
            }
         }

         queue.add(insertAt, packet);
      }
   }

   private int computeNextActiveSendDelay() {
      int elapsed = this.computeStaggeredElapsed(this.activeSendDelayUnit);
      if (this.staggeredHasExplicitDelays || this.timedCaptureMode) {
         return elapsed;
      } else if (this.staggeredPacketSend) {
         int tailDelay = this.findMaxPendingDelay(this.sendingPackets);
         return Math.max(elapsed, tailDelay) + this.staggeredSendDelay;
      } else {
         return elapsed;
      }
   }

   private int advanceLiveCaptureDelay() {
      if (this.activeSendDelayUnit == AutismSessionState.DelayUnit.MS) {
         long now = System.nanoTime();
         long reference = this.liveCaptureLastNanos > 0L ? this.liveCaptureLastNanos : this.staggeredStartNanos;
         int delta = reference > 0L ? (int)Math.max(0L, (now - reference) / 1000000L) : 0;
         this.liveCaptureLastNanos = now;
         this.liveCaptureLastTick = this.clientTick;
         this.liveCaptureAccumulatedDelay += delta;
         return this.liveCaptureAccumulatedDelay;
      } else {
         int tick = this.clientTick;
         int reference = this.liveCaptureLastTick >= 0 ? this.liveCaptureLastTick : this.staggeredStartTick;
         int delta = Math.max(0, tick - reference);
         this.liveCaptureLastTick = tick;
         this.liveCaptureLastNanos = System.nanoTime();
         this.liveCaptureAccumulatedDelay += delta;
         return this.liveCaptureAccumulatedDelay;
      }
   }

   public synchronized boolean restoreLastFlushedQueue() {
      if (this.lastFlushedPackets.isEmpty()) {
         return false;
      }

      this.delayedPackets.clear();
      this.resetTimedCaptureOrigin();
      if (!this.lastFlushedPackets.isEmpty()) {
         this.delayedPackets.addAll(this.cloneAndRenumberQueue(this.lastFlushedPackets));
      }

      this.bumpQueueRevision();
      return true;
   }

   public synchronized void enqueueDelayedPacket(Packet<?> packet) {
      this.enqueueDelayedPacket(packet, AutismSessionState.ReplayMode.REGENERATE);
   }

   public synchronized void enqueueExactPacket(Packet<?> packet) {
      this.enqueueDelayedPacket(packet, AutismSessionState.ReplayMode.EXACT);
   }

   public synchronized void enqueueDelayedPacket(Packet<?> packet, AutismSessionState.ReplayMode replayMode) {
      if (!autismclient.modules.aD.read_Z() && packet != null) {
         AutismSessionState.ReplayMode mode = this.resolvePacketReplayMode(packet, replayMode);
         if (this.isActivelySendingStaggered()) {
            int activeDelay = this.timedCaptureMode ? this.advanceLiveCaptureDelay() : this.computeNextActiveSendDelay();
            AutismSessionState.DelayedPacket queuedPacket = new AutismSessionState.DelayedPacket(packet, activeDelay, mode);
            this.insertSortedByDelay(this.sendingPackets, queuedPacket);
            this.insertSortedByDelay(this.sendingDisplayPackets, queuedPacket);
            this.staggeredSendTotalCount++;
            this.bumpQueueRevision();
            return;
         }

         int delay = 0;
         if (this.timedCaptureMode) {
            if (this.delayedPackets.isEmpty() && this.sendingPackets.isEmpty()) {
               this.resetTimedCaptureOrigin();
            }

            if (this.delayUnit == AutismSessionState.DelayUnit.TICKS) {
               int tick = this.clientTick;
               if (this.captureStartTick < 0) {
                  this.captureStartTick = tick;
                  delay = 0;
               } else {
                  delay = tick - this.captureStartTick;
               }
            } else {
               long now = System.nanoTime();
               if (this.captureStartNanos < 0L) {
                  this.captureStartNanos = now;
                  delay = 0;
               } else {
                  delay = (int)((now - this.captureStartNanos) / 1000000L);
               }
            }
         } else if (this.staggeredPacketSend) {
            delay = this.delayedPackets.size() * this.staggeredSendDelay;
         }

         this.delayedPackets.add(new AutismSessionState.DelayedPacket(packet, delay, mode));
         this.bumpQueueRevision();
      }
   }

   private AutismSessionState.ReplayMode resolvePacketReplayMode(Packet<?> packet, AutismSessionState.ReplayMode replayMode) {
      AutismSessionState.ReplayMode mode = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      return mode == AutismSessionState.ReplayMode.REGENERATE
            && packet instanceof ServerboundContainerClosePacket closePacket
            && this.hasQueuedExactClickForContainer(this.extractContainerId(closePacket))
         ? AutismSessionState.ReplayMode.EXACT
         : mode;
   }

   private int extractContainerId(ServerboundContainerClosePacket packet) {
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

   private boolean hasQueuedExactClickForContainer(int containerId) {
      for (AutismSessionState.DelayedPacket queuedPacket : this.delayedPackets) {
         if (this.isExactClickForContainer(queuedPacket, containerId)) {
            return true;
         }
      }

      for (AutismSessionState.DelayedPacket queuedPacket : this.sendingPackets) {
         if (this.isExactClickForContainer(queuedPacket, containerId)) {
            return true;
         }
      }

      return false;
   }

   private boolean isExactClickForContainer(AutismSessionState.DelayedPacket queuedPacket, int containerId) {
      return queuedPacket != null
         && queuedPacket.isExactReplay()
         && queuedPacket.packet instanceof ServerboundContainerClickPacket clickPacket
         && clickPacket.containerId() == containerId;
   }

   public void storeGuiScreen(Screen screen, AbstractContainerMenu handler) {
      this.storedGuiScreen = screen;
      this.storedContainerMenu = handler;
   }

   public Screen getStoredGuiScreen() {
      return this.storedGuiScreen;
   }

   public AbstractContainerMenu getStoredContainerMenu() {
      return this.storedContainerMenu;
   }

   public synchronized int flushDelayedPackets(ClientPacketListener networkHandler) {
      if (autismclient.modules.aD.read_Z()) {
         return 0;
      }

      if (!this.sendingPackets.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cAlready sending - wait for current queue to finish");
         return 0;
      }

      this.staggeredCompletionMessage = null;
      int count = this.delayedPackets.size();
      if (count == 0) {
         return 0;
      }

      this.resetTimedCaptureOrigin();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      boolean hasDelays = this.delayedPackets.stream().anyMatch(qpx -> qpx.delay > 0);
      if (module.isUseDirectFlush() && !hasDelays) {
         return this.flushDelayedPacketsDirectly(networkHandler);
      }

      this.archiveCurrentDelayedQueue();
      if (networkHandler != null) {
         boolean shouldStagger = this.staggeredPacketSend || hasDelays;
         if (shouldStagger) {
            this.sendingDisplayPackets.clear();
            this.sendingPackets.addAll(this.delayedPackets);
            this.sendingDisplayPackets.addAll(this.delayedPackets);
            this.staggeredSendConnection = networkHandler;
            this.staggeredTickCounter = 0;
            this.staggeredSendTotalCount = count;
            this.staggeredHasExplicitDelays = hasDelays;
            this.activeSendDelayUnit = this.delayUnit;
            this.staggeredStartNanos = System.nanoTime();
            this.staggeredStartTick = this.clientTick;
            this.liveCaptureLastNanos = this.staggeredStartNanos;
            this.liveCaptureLastTick = this.staggeredStartTick;
            this.liveCaptureAccumulatedDelay = this.findMaxPendingDelay(this.sendingPackets);
         } else {
            this.bypassingPacketInterception = true;

            try {
               for (AutismSessionState.DelayedPacket qp : this.delayedPackets) {
                  if (PacketTypeRegistry.getC2sPackets().contains(qp.packet.getClass())) {
                     Packet<?> packetToSend = this.preparePacketForSend(qp);
                     if (packetToSend != null) {
                        DirectPacketSender.send(packetToSend);
                     }
                  } else {
                     AutismClientAddon.LOG.warn("[Autism] Skipped sending invalid C2S packet during flush: {}", qp.packet.getClass().getName());
                  }
               }
            } finally {
               this.bypassingPacketInterception = false;
            }

            AutismSessionState.DelayedPacket.resetIdCounter();
         }
      }

      this.delayedPackets.clear();
      this.bumpQueueRevision();
      return count;
   }

   public void sendPacketBypassingInterception(ClientPacketListener networkHandler, Packet<?> packet) {
      if (!autismclient.modules.aD.read_Z() && networkHandler != null && packet != null) {
         this.bypassingPacketInterception = true;

         try {
            networkHandler.getConnection().send(packet, null);
         } finally {
            this.bypassingPacketInterception = false;
         }
      }
   }

   private int flushDelayedPacketsDirectly(ClientPacketListener networkHandler) {
      if (autismclient.modules.aD.read_Z()) {
         return 0;
      }

      int count = this.delayedPackets.size();
      if (count == 0) {
         return 0;
      }

      this.archiveCurrentDelayedQueue();
      if (networkHandler != null) {
         this.bypassingPacketInterception = true;

         try {
            for (AutismSessionState.DelayedPacket qp : this.delayedPackets) {
               if (PacketTypeRegistry.getC2sPackets().contains(qp.packet.getClass())) {
                  Packet<?> packetToSend = this.preparePacketForSend(qp);
                  if (packetToSend != null) {
                     networkHandler.getConnection().send(packetToSend, null);
                  }
               } else {
                  AutismClientAddon.LOG.warn("[Autism] Skipped invalid C2S packet during bypass flush: {}", qp.packet.getClass().getName());
               }
            }
         } finally {
            this.bypassingPacketInterception = false;
         }
      }

      this.delayedPackets.clear();
      this.resetPacketIdsIfEmpty();
      this.bumpQueueRevision();
      return count;
   }

   public synchronized void regenerateQueuedPackets() {
      if (!this.delayedPackets.isEmpty()) {
         List<AutismSessionState.DelayedPacket> newPackets = new ArrayList<>(this.delayedPackets.size());
         int skipped = 0;
         int deferredClickSlot = 0;

         for (AutismSessionState.DelayedPacket qp : this.delayedPackets) {
            if (qp.packet instanceof ServerboundContainerClickPacket) {
               newPackets.add(new AutismSessionState.DelayedPacket(qp.packet, qp.delay, qp.id, qp.replayMode));
               deferredClickSlot++;
            } else if (qp.isExactReplay()) {
               newPackets.add(new AutismSessionState.DelayedPacket(qp.packet, qp.delay, qp.id, qp.replayMode));
            } else {
               Packet<?> regenerated = PacketRegenerator.regenerate(qp.packet);
               if (regenerated != null) {
                  newPackets.add(new AutismSessionState.DelayedPacket(regenerated, qp.delay, qp.id, qp.replayMode));
               } else {
                  skipped++;
               }
            }
         }

         this.delayedPackets.clear();
         this.delayedPackets.addAll(this.cloneAndRenumberQueue(newPackets));
         this.bumpQueueRevision();
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

   public synchronized void tickStaggeredSend() {
      if (autismclient.modules.aD.read_Z()) {
         this.sendingPackets.clear();
         this.sendingDisplayPackets.clear();
         this.staggeredSendConnection = null;
         this.bumpQueueRevision();
      } else if (!this.sendingPackets.isEmpty() && this.staggeredSendConnection != null) {
         List<AutismSessionState.DelayedPacket> toRemove = new ArrayList<>();

         for (AutismSessionState.DelayedPacket qp : this.sendingPackets) {
            boolean shouldSend;
            if (this.activeSendDelayUnit == AutismSessionState.DelayUnit.MS) {
               long elapsedMs = (System.nanoTime() - this.staggeredStartNanos) / 1000000L;
               shouldSend = elapsedMs >= qp.getDelay() && !qp.isSent();
            } else {
               int elapsedTicks = this.clientTick - this.staggeredStartTick;
               shouldSend = elapsedTicks >= qp.getDelay() && !qp.isSent();
            }

            if (shouldSend) {
               this.bypassingPacketInterception = true;

               try {
                  if (PacketTypeRegistry.getC2sPackets().contains(qp.packet.getClass())) {
                     Packet<?> packetToSend = this.preparePacketForSend(qp);
                     if (packetToSend == null) {
                        qp.markSent();
                        toRemove.add(qp);
                     } else {
                        this.staggeredSendConnection.getConnection().send(packetToSend, null);
                        qp.markSent();
                        toRemove.add(qp);
                     }
                  } else {
                     AutismClientAddon.LOG.warn("[Autism] Skipped sending invalid C2S packet: {}", qp.packet.getClass().getName());
                     qp.markSent();
                     toRemove.add(qp);
                  }
               } catch (Exception e) {
                  qp.markSent();
                  toRemove.add(qp);
                  AutismClientAddon.LOG.error("[Autism] Failed to send queued packet: {}", qp.packet.getClass().getName(), e);
               } finally {
                  this.bypassingPacketInterception = false;
               }
            }
         }

         this.sendingPackets.removeAll(toRemove);
         if (!toRemove.isEmpty()) {
            this.bumpQueueRevision();
         }

         this.staggeredTickCounter++;
         if (this.sendingPackets.isEmpty()) {
            int total = this.staggeredSendTotalCount;
            String completionMessage = this.consumeStaggeredCompletionMessage();
            this.resetStaggeredSendState();
            if (this.delayedPackets.isEmpty()) {
               this.resetTimedCaptureOrigin();
               AutismSessionState.DelayedPacket.resetIdCounter();
            }

            if (completionMessage == null || completionMessage.isBlank()) {
               completionMessage = "Finished sending " + total + " packets.";
            }

            bi.init(completionMessage, -13248397);
         }
      }
   }

   public boolean isStaggeredSendActive() {
      return !this.sendingPackets.isEmpty() && this.staggeredSendConnection != null;
   }

   public boolean needsPacketInterception() {
      return this.delayPacketsEnabled
         || !this.sendPacketsEnabled
         || this.useCustomPackets
         || this.bypassingPacketInterception
         || this.dropNextContainerClose
         || this.dropNextSignUpdate
         || this.dropNextBookEdit
         || this.forceNextSignUpdate
         || this.forceNextBookEdit
         || !this.allowSignEditing
         || !this.allowBookEditing
         || this.gbreakCapturing
         || this.timedCaptureMode
         || this.blockCaptureCallback != null
         || this.blockFaceCaptureCallback != null
         || this.attackCaptureCallback != null
         || this.entityCaptureCallback != null
         || this.captureCancelCallback != null;
   }

   public boolean isSendingQueuedPackets() {
      return !this.sendingPackets.isEmpty() && this.staggeredSendConnection != null;
   }

   public synchronized void setStaggeredCompletionMessage(String message) {
      this.staggeredCompletionMessage = message != null && !message.isBlank() ? message : null;
   }

   private String consumeStaggeredCompletionMessage() {
      String message = this.staggeredCompletionMessage;
      this.staggeredCompletionMessage = null;
      return message;
   }

   public synchronized List<AutismSessionState.DelayedPacket> getSendingPackets() {
      return new ArrayList<>(this.sendingPackets);
   }

   public boolean hasSendingPackets() {
      return !this.sendingPackets.isEmpty();
   }

   public synchronized List<AutismSessionState.DelayedPacket> getSendingDisplayPackets() {
      return new ArrayList<>(this.sendingDisplayPackets);
   }

   public synchronized int cancelAndClearPacketQueues() {
      if (!this.sendingPackets.isEmpty()) {
         int count = this.sendingPackets.size() + this.delayedPackets.size();
         List<AutismSessionState.DelayedPacket> combined = new ArrayList<>(this.sendingPackets.size() + this.delayedPackets.size());
         combined.addAll(this.sendingPackets);
         combined.addAll(this.delayedPackets);
         this.archivePacketQueue(combined);
         this.staggeredCompletionMessage = null;
         this.resetStaggeredSendState();
         this.delayedPackets.clear();
         this.resetPacketIdsIfEmpty();
         return count;
      } else {
         int count = this.delayedPackets.size();
         this.clearDelayedPackets();
         return count;
      }
   }

   public synchronized void removeDelayedPacket(AutismSessionState.DelayedPacket packet) {
      if (packet != null) {
         this.sendingDisplayPackets.remove(packet);
         boolean removedFromStaggered = this.sendingPackets.remove(packet);
         if (removedFromStaggered && this.sendingPackets.isEmpty()) {
            this.resetStaggeredSendState();
         }

         if (!removedFromStaggered) {
            this.delayedPackets.remove(packet);
            if (!this.delayedPackets.isEmpty()) {
               List<AutismSessionState.DelayedPacket> rebuilt = this.cloneAndRenumberQueue(this.delayedPackets);
               this.delayedPackets.clear();
               this.delayedPackets.addAll(rebuilt);
            }
         }

         this.resetPacketIdsIfEmpty();
         this.bumpQueueRevision();
      }
   }

   public synchronized void updateDelayedPacketDelay(AutismSessionState.DelayedPacket packet, int newDelay) {
      if (packet != null) {
         packet.setDelay(newDelay);
         this.bumpQueueRevision();
      }
   }

   public synchronized void sortDelayedPacketsByDelay() {
      if (this.delayedPackets.size() >= 2) {
         List<AutismSessionState.DelayedPacket> ordered = new ArrayList<>(this.delayedPackets);
         ordered.sort((a, b) -> {
            int byDelay = Integer.compare(a.getDelay(), b.getDelay());
            return byDelay != 0 ? byDelay : Integer.compare(a.getId(), b.getId());
         });
         this.delayedPackets.clear();
         this.delayedPackets.addAll(ordered);
         this.bumpQueueRevision();
      }
   }

   private List<AutismSessionState.DelayedPacket> cloneAndRenumberQueue(List<AutismSessionState.DelayedPacket> source) {
      List<AutismSessionState.DelayedPacket> rebuilt = new ArrayList<>();
      if (source != null && !source.isEmpty()) {
         List<AutismSessionState.DelayedPacket> ordered = new ArrayList<>();

         for (AutismSessionState.DelayedPacket qp : source) {
            if (qp != null && qp.packet != null) {
               ordered.add(qp);
            }
         }

         ordered.sort((a, b) -> {
            int byDelay = Integer.compare(a.getDelay(), b.getDelay());
            return byDelay != 0 ? byDelay : Integer.compare(a.getId(), b.getId());
         });
         AutismSessionState.DelayedPacket.resetIdCounter();

         for (AutismSessionState.DelayedPacket qp : ordered) {
            rebuilt.add(new AutismSessionState.DelayedPacket(qp.packet, qp.getDelay(), qp.getReplayMode()));
         }

         return rebuilt;
      } else {
         return rebuilt;
      }
   }

   private Packet<?> preparePacketForSend(AutismSessionState.DelayedPacket queuedPacket) {
      if (queuedPacket != null && queuedPacket.packet != null) {
         return queuedPacket.isExactReplay() ? queuedPacket.packet : PacketRegenerator.regenerate(queuedPacket.packet);
      } else {
         return null;
      }
   }

   public boolean isBypassingPacketInterception() {
      return this.bypassingPacketInterception;
   }

   public void setBypassingPacketInterception(boolean value) {
      this.bypassingPacketInterception = value;
   }

   public void clearStoredGui() {
      this.storedGuiScreen = null;
      this.storedContainerMenu = null;
   }

   public synchronized void resetSessionState() {
      this.sendPacketsEnabled = true;
      this.delayPacketsEnabled = false;
      this.allowSignEditing = true;
      this.allowBookEditing = true;
      this.pretendPackAccepted = false;
      this.autoDenyResourcePack = false;
      this.xCarryActive = false;
      this.xCarryForced = false;
      this.lastInteractedBlockPos = null;
      this.lastInteractionTarget = null;
      this.delayedPackets.clear();
      this.sendingPackets.clear();
      this.sendingDisplayPackets.clear();
      this.staggeredSendConnection = null;
      this.staggeredTickCounter = 0;
      this.staggeredSendTotalCount = 0;
      AutismSessionState.DelayedPacket.resetIdCounter();
      this.staggeredHasExplicitDelays = false;
      this.staggeredStartNanos = 0L;
      this.staggeredStartTick = 0;
      this.liveCaptureLastNanos = -1L;
      this.liveCaptureLastTick = -1;
      this.liveCaptureAccumulatedDelay = 0;
      this.activeSendDelayUnit = AutismSessionState.DelayUnit.TICKS;
      this.captureStartNanos = -1L;
      this.captureStartTick = -1;
      this.bumpQueueRevision();
      this.storedGuiScreen = null;
      this.storedContainerMenu = null;
      this.pluginScanResults.clear();
      this.serverInfoActiveTab = 0;
      this.serverInfoScrollOffset = 0;
      this.serverInfoSelectedPlugin = "";
      this.serverInfoStateAddress = "";
      this.serverDetectedVersion = "";
      this.serverDetectedVersionAddress = "";
      this.serverInfoScanDelayMs = 50;
   }

   public boolean isSignEditingAllowed() {
      return this.allowSignEditing;
   }

   public void setAllowSignEditing(boolean value) {
      this.allowSignEditing = value;
   }

   public boolean isBookEditingAllowed() {
      return this.allowBookEditing;
   }

   public void setAllowBookEditing(boolean value) {
      this.allowBookEditing = value;
   }

   public boolean isPretendPackAccepted() {
      return this.pretendPackAccepted;
   }

   public void setPretendPackAccepted(boolean value) {
      this.pretendPackAccepted = value;
   }

   public boolean isAutoDenyResourcePack() {
      return this.autoDenyResourcePack;
   }

   public void setAutoDenyResourcePack(boolean value) {
      this.autoDenyResourcePack = value;
   }

   public boolean isUseCustomPackets() {
      return this.useCustomPackets;
   }

   public void setUseCustomPackets(boolean value) {
      this.useCustomPackets = value;
   }

   public Set<Class<? extends Packet<?>>> getCustomC2sPackets() {
      return this.customC2sPackets;
   }

   public void setCustomC2sPackets(Set<Class<? extends Packet<?>>> packets) {
      this.customC2sPackets.clear();
      this.customC2sPackets.addAll(packets);
   }

   public Set<Class<? extends Packet<?>>> getCustomS2cPackets() {
      return this.customS2cPackets;
   }

   public void setCustomS2cPackets(Set<Class<? extends Packet<?>>> packets) {
      this.customS2cPackets.clear();
      this.customS2cPackets.addAll(packets);
   }

   public boolean isStaggeredPacketSend() {
      return this.staggeredPacketSend;
   }

   public void setStaggeredPacketSend(boolean value) {
      if (!value || !autismclient.modules.aD.read_Z()) {
         this.staggeredPacketSend = value;
      }
   }

   public int getStaggeredSendDelay() {
      return this.staggeredSendDelay;
   }

   public void setStaggeredSendDelay(int delay) {
      this.staggeredSendDelay = Math.max(1, Math.min(40, delay));
   }

   public AutismSessionState.DelayUnit getDelayUnit() {
      return this.delayUnit;
   }

   public synchronized AutismSessionState.DelayUnit getEffectiveDelayUnit() {
      return this.isActivelySendingStaggered() ? this.activeSendDelayUnit : this.delayUnit;
   }

   public void setDelayUnit(AutismSessionState.DelayUnit mode) {
      this.delayUnit = mode;
   }

   public void toggleDelayUnit() {
      this.delayUnit = this.delayUnit == AutismSessionState.DelayUnit.TICKS ? AutismSessionState.DelayUnit.MS : AutismSessionState.DelayUnit.TICKS;
   }

   public boolean isFabricatorVisible() {
      return this.fabricatorVisible;
   }

   public void setFabricatorVisible(boolean visible) {
      this.fabricatorVisible = visible;
   }

   public int getFabricatorX() {
      return this.fabricatorX;
   }

   public void setFabricatorX(int x) {
      this.fabricatorX = x;
   }

   public int getFabricatorY() {
      return this.fabricatorY;
   }

   public void setFabricatorY(int y) {
      this.fabricatorY = y;
   }

   public String getFabricatorSlotInput() {
      return this.fabricatorSlotInput;
   }

   public void setFabricatorSlotInput(String value) {
      this.fabricatorSlotInput = value != null ? value : "0";
   }

   public String getFabricatorItemInput() {
      return this.fabricatorItemInput;
   }

   public void setFabricatorItemInput(String value) {
      this.fabricatorItemInput = value != null ? value : "";
   }

   public String getFabricatorCountInput() {
      return this.fabricatorCountInput;
   }

   public void setFabricatorCountInput(String value) {
      this.fabricatorCountInput = value != null ? value : "1";
   }

   public int getFabricatorActionIndex() {
      return this.fabricatorActionIndex;
   }

   public void setFabricatorActionIndex(int index) {
      this.fabricatorActionIndex = Math.max(0, index);
   }

   public boolean isFabricatorUseMaxAmount() {
      return this.fabricatorUseMaxAmount;
   }

   public void setFabricatorUseMaxAmount(boolean useMaxAmount) {
      this.fabricatorUseMaxAmount = useMaxAmount;
   }

   public int getFabricatorClickModeIndex() {
      return this.fabricatorClickModeIndex;
   }

   public void setFabricatorClickModeIndex(int index) {
      this.fabricatorClickModeIndex = Math.max(0, index);
   }

   public boolean isFabricatorDropWholeStack() {
      return this.fabricatorDropWholeStack;
   }

   public void setFabricatorDropWholeStack(boolean value) {
      this.fabricatorDropWholeStack = value;
   }

   public String getFabricatorRecipeSearch() {
      return this.fabricatorRecipeSearch;
   }

   public void setFabricatorRecipeSearch(String value) {
      this.fabricatorRecipeSearch = value != null ? value : "";
   }

   public int getFabricatorSelectedRecipeId() {
      return this.fabricatorSelectedRecipeId;
   }

   public void setFabricatorSelectedRecipeId(int recipeId) {
      this.fabricatorSelectedRecipeId = recipeId;
   }

   public String getFabricatorSelectedRecipeKey() {
      return this.fabricatorSelectedRecipeKey;
   }

   public void setFabricatorSelectedRecipeKey(String recipeKey) {
      this.fabricatorSelectedRecipeKey = recipeKey != null ? recipeKey : "";
   }

   public int getFabricatorRecipeScrollOffset() {
      return this.fabricatorRecipeScrollOffset;
   }

   public void setFabricatorRecipeScrollOffset(int offset) {
      this.fabricatorRecipeScrollOffset = Math.max(0, offset);
   }

   public synchronized List<autismclient.util.macro.CraftAction.CraftEntry> getFabricatorCraftEntries() {
      List<autismclient.util.macro.CraftAction.CraftEntry> copies = new ArrayList<>(this.fabricatorCraftEntries.size());

      for (autismclient.util.macro.CraftAction.CraftEntry entry : this.fabricatorCraftEntries) {
         if (entry != null) {
            copies.add(entry.copy());
         }
      }

      return copies;
   }

   public synchronized void setFabricatorCraftEntries(List<autismclient.util.macro.CraftAction.CraftEntry> entries) {
      this.fabricatorCraftEntries.clear();
      if (entries != null) {
         for (autismclient.util.macro.CraftAction.CraftEntry entry : entries) {
            if (entry != null) {
               this.fabricatorCraftEntries.add(entry.copy());
            }
         }
      }
   }

   public int getFabricatorSelectedCraftIndex() {
      return this.fabricatorSelectedCraftIndex;
   }

   public void setFabricatorSelectedCraftIndex(int index) {
      this.fabricatorSelectedCraftIndex = index;
   }

   public int getFabricatorCraftScrollOffset() {
      return this.fabricatorCraftScrollOffset;
   }

   public void setFabricatorCraftScrollOffset(int offset) {
      this.fabricatorCraftScrollOffset = Math.max(0, offset);
   }

   public boolean isLanSyncOverlayVisible() {
      return this.lanSyncOverlayVisible;
   }

   public void setLanSyncOverlayVisible(boolean visible) {
      this.lanSyncOverlayVisible = visible;
   }

   public int getLanSyncOverlayX() {
      return this.lanSyncOverlayX;
   }

   public void setLanSyncOverlayX(int x) {
      this.lanSyncOverlayX = x;
   }

   public int getLanSyncOverlayY() {
      return this.lanSyncOverlayY;
   }

   public void setLanSyncOverlayY(int y) {
      this.lanSyncOverlayY = y;
   }

   public int getLanSyncActiveTab() {
      return this.lanSyncActiveTab;
   }

   public void setLanSyncActiveTab(int activeTab) {
      this.lanSyncActiveTab = Math.max(0, activeTab);
   }

   public int getLanSyncScrollOffset() {
      return this.lanSyncScrollOffset;
   }

   public void setLanSyncScrollOffset(int scrollOffset) {
      this.lanSyncScrollOffset = Math.max(0, scrollOffset);
   }

   public boolean isLanSyncPerUserMode() {
      return this.lanSyncPerUserMode;
   }

   public void setLanSyncPerUserMode(boolean perUserMode) {
      this.lanSyncPerUserMode = perUserMode;
   }

   public String getLanSyncSelectedMacroName() {
      return this.lanSyncSelectedMacroName;
   }

   public void setLanSyncSelectedMacroName(String selectedMacroName) {
      this.lanSyncSelectedMacroName = selectedMacroName == null ? "" : selectedMacroName;
   }

   public String getLanSyncExpandedExecutePeer() {
      return this.lanSyncExpandedExecutePeer;
   }

   public void setLanSyncExpandedExecutePeer(String expandedExecutePeer) {
      this.lanSyncExpandedExecutePeer = expandedExecutePeer == null ? "" : expandedExecutePeer;
   }

   public String getLanSyncSelectedPeer() {
      return this.lanSyncSelectedPeer;
   }

   public void setLanSyncSelectedPeer(String selectedPeer) {
      this.lanSyncSelectedPeer = selectedPeer == null ? "" : selectedPeer;
   }

   public boolean isMacroListOverlayVisible() {
      return this.macroListOverlayVisible;
   }

   public void setMacroListOverlayVisible(boolean visible) {
      this.macroListOverlayVisible = visible;
   }

   public int getMacroListOverlayX() {
      return this.macroListOverlayX;
   }

   public void setMacroListOverlayX(int x) {
      this.macroListOverlayX = x;
   }

   public int getMacroListOverlayY() {
      return this.macroListOverlayY;
   }

   public void setMacroListOverlayY(int y) {
      this.macroListOverlayY = y;
   }

   public int getMacroListScrollOffset() {
      return this.macroListScrollOffset;
   }

   public void setMacroListScrollOffset(int scrollOffset) {
      this.macroListScrollOffset = Math.max(0, scrollOffset);
   }

   public String getMacroListSearchQuery() {
      return this.macroListSearchQuery;
   }

   public void setMacroListSearchQuery(String search) {
      this.macroListSearchQuery = search == null ? "" : search;
   }

   public boolean isQueueEditorVisible() {
      return this.queueEditorVisible;
   }

   public void setQueueEditorVisible(boolean visible) {
      this.queueEditorVisible = visible;
   }

   public int getQueueEditorX() {
      return this.queueEditorX;
   }

   public void setQueueEditorX(int x) {
      this.queueEditorX = x;
   }

   public int getQueueEditorY() {
      return this.queueEditorY;
   }

   public void setQueueEditorY(int y) {
      this.queueEditorY = y;
   }

   public int getServerInfoActiveTab() {
      return this.serverInfoActiveTab;
   }

   public void setServerInfoActiveTab(int tab) {
      this.serverInfoActiveTab = Math.max(0, tab);
   }

   public int getServerInfoScrollOffset() {
      return this.serverInfoScrollOffset;
   }

   public void setServerInfoScrollOffset(int offset) {
      this.serverInfoScrollOffset = Math.max(0, offset);
   }

   public String getServerInfoSelectedPlugin() {
      return this.serverInfoSelectedPlugin;
   }

   public void setServerInfoSelectedPlugin(String plugin) {
      this.serverInfoSelectedPlugin = plugin == null ? "" : plugin;
   }

   public String getServerInfoStateAddress() {
      return this.serverInfoStateAddress;
   }

   public void setServerInfoStateAddress(String address) {
      this.serverInfoStateAddress = normalizeServerAddress(address);
   }

   public String getDetectedServerVersion(String address) {
      String normalizedAddress = normalizeServerAddress(address);
      if (this.serverDetectedVersion == null || this.serverDetectedVersion.isBlank()) {
         return "";
      } else if (normalizedAddress.isEmpty()) {
         return this.serverDetectedVersion;
      } else {
         String normalizedStored = normalizeServerAddress(this.serverDetectedVersionAddress);
         if (normalizedStored.isEmpty()) {
            return this.serverDetectedVersion;
         } else {
            String addrNoPort = this.stripPort(normalizedAddress);
            String storedNoPort = this.stripPort(normalizedStored);
            if (addrNoPort.equals(storedNoPort)) {
               return this.serverDetectedVersion;
            } else {
               return normalizedAddress.equals(normalizedStored) ? this.serverDetectedVersion : "";
            }
         }
      }
   }

   private String stripPort(String address) {
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

   public void setDetectedServerVersion(String address, String version) {
      String normalizedAddress = normalizeServerAddress(address);
      this.serverDetectedVersionAddress = normalizedAddress;
      this.serverDetectedVersion = version == null ? "" : version.trim();
   }

   public void clearDetectedServerVersion() {
      this.serverDetectedVersion = "";
      this.serverDetectedVersionAddress = "";
   }

   public int getServerInfoScanDelayMs() {
      return this.serverInfoScanDelayMs;
   }

   public void setServerInfoScanDelayMs(int delayMs) {
      this.serverInfoScanDelayMs = Math.max(10, Math.min(500, delayMs));
   }

   public int getServerInfoTabWidth() {
      return this.serverInfoTabWidth;
   }

   public void setServerInfoTabWidth(int width) {
      this.serverInfoTabWidth = Math.max(252, width);
   }

   public int getServerInfoTabHeight() {
      return this.serverInfoTabHeight;
   }

   public void setServerInfoTabHeight(int height) {
      this.serverInfoTabHeight = Math.max(258, height);
   }

   public int getServerInfoPluginsTabWidth() {
      return this.serverInfoPluginsTabWidth;
   }

   public void setServerInfoPluginsTabWidth(int width) {
      this.serverInfoPluginsTabWidth = Math.max(280, width);
   }

   public int getServerInfoPluginsTabHeight() {
      return this.serverInfoPluginsTabHeight;
   }

   public void setServerInfoPluginsTabHeight(int height) {
      this.serverInfoPluginsTabHeight = Math.max(280, height);
   }

   public void startGbreakCapture(Runnable onCaptureComplete) {
      this.gbreakCapturing = true;
      this.gbreakPacketCount = 0;
      this.gbreakCapturedPackets.clear();
      this.gbreakCaptureCallback = onCaptureComplete;
   }

   public boolean isGbreakCapturing() {
      return this.gbreakCapturing;
   }

   public void onGbreakPacket(Packet<?> packet) {
      if (this.gbreakCapturing) {
         this.gbreakPacketCount++;
         if (TRACE_PAYLOADS) {
            AutismClientAddon.LOG.info("[GBreak] Packet #{}: {}", this.gbreakPacketCount, packet.getClass().getSimpleName());
         }

         if (this.gbreakPacketCount == 2) {
            this.gbreakCapturedPackets.clear();
            this.gbreakCapturedPackets.add(new AutismSessionState.DelayedPacket(packet, 0));
            this.finishGbreakCapture();
         }
      }
   }

   public List<AutismSessionState.DelayedPacket> getGbreakCapturedPackets() {
      return new ArrayList<>(this.gbreakCapturedPackets);
   }

   private void finishGbreakCapture() {
      this.gbreakCapturing = false;
      if (this.gbreakCaptureCallback != null) {
         this.gbreakCaptureCallback.run();
         this.gbreakCaptureCallback = null;
      }
   }

   public void cancelGbreakCapture() {
      this.gbreakCapturing = false;
      this.gbreakPacketCount = 0;
      this.gbreakCapturedPackets.clear();
      this.gbreakCaptureCallback = null;
   }

   public void markDropNextContainerClose(boolean v) {
      this.setDropNextContainerClose(v);
   }

   public void setDropNextContainerClose(boolean v) {
      this.dropNextContainerClose = v;
   }

   public void setDropNextSignUpdate(boolean v) {
      this.dropNextSignUpdate = v;
   }

   public void setDropNextBookEdit(boolean v) {
      this.dropNextBookEdit = v;
   }

   public void setForceNextSignUpdate(boolean v) {
      this.forceNextSignUpdate = v;
   }

   public void setForceNextBookEdit(boolean v) {
      this.forceNextBookEdit = v;
   }

   public boolean consumeDropNextContainerClose() {
      boolean v = this.dropNextContainerClose;
      this.dropNextContainerClose = false;
      return v;
   }

   public boolean pollDropNextContainerClose() {
      return this.consumeDropNextContainerClose();
   }

   public boolean consumeDropNextSignUpdate() {
      boolean v = this.dropNextSignUpdate;
      this.dropNextSignUpdate = false;
      return v;
   }

   public boolean consumeDropNextBookEdit() {
      boolean v = this.dropNextBookEdit;
      this.dropNextBookEdit = false;
      return v;
   }

   public boolean consumeForceNextSignUpdate() {
      boolean v = this.forceNextSignUpdate;
      this.forceNextSignUpdate = false;
      return v;
   }

   public boolean consumeForceNextBookEdit() {
      boolean v = this.forceNextBookEdit;
      this.forceNextBookEdit = false;
      return v;
   }

   public void setBlockCaptureCallback(Consumer<BlockPos> cb) {
      this.blockCaptureCallback = cb;
   }

   public void setBlockFaceCaptureCallback(BiConsumer<BlockPos, Direction> cb) {
      this.blockFaceCaptureCallback = cb;
   }

   public boolean dispatchBlockCapture(BlockPos pos) {
      return this.dispatchBlockCapture(pos, Direction.UP);
   }

   public boolean dispatchBlockCapture(BlockPos pos, Direction direction) {
      BiConsumer<BlockPos, Direction> dcb = this.blockFaceCaptureCallback;
      if (dcb != null) {
         this.blockFaceCaptureCallback = null;
         dcb.accept(pos, direction == null ? Direction.UP : direction);
         return true;
      } else {
         Consumer<BlockPos> cb = this.blockCaptureCallback;
         if (cb != null) {
            this.blockCaptureCallback = null;
            cb.accept(pos);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean hasBlockCaptureCallback() {
      return this.blockCaptureCallback != null || this.blockFaceCaptureCallback != null;
   }

   public void setAttackCaptureCallback(Runnable cb) {
      this.attackCaptureCallback = cb;
   }

   public boolean triggerAttackCaptureCallback() {
      Runnable cb = this.attackCaptureCallback;
      if (cb != null) {
         this.attackCaptureCallback = null;
         cb.run();
         return true;
      } else {
         return false;
      }
   }

   public boolean hasAttackCaptureCallback() {
      return this.attackCaptureCallback != null;
   }

   public void setCaptureCancelCallback(Runnable cb) {
      this.captureCancelCallback = cb;
   }

   public boolean triggerCaptureCancelCallback() {
      Runnable cb = this.captureCancelCallback;
      if (cb != null) {
         this.captureCancelCallback = null;
         cb.run();
         return true;
      } else {
         return false;
      }
   }

   public boolean hasCaptureCancelCallback() {
      return this.captureCancelCallback != null;
   }

   public void setEntityCaptureCallback(Consumer<String> cb) {
      this.entityCaptureCallback = cb;
   }

   public boolean dispatchEntityCapturePayload(String payload) {
      Consumer<String> cb = this.entityCaptureCallback;
      if (cb != null) {
         this.entityCaptureCallback = null;
         cb.accept(payload);
         return true;
      } else {
         return false;
      }
   }

   public boolean dispatchEntityCapture(Entity targeted) {
      if (targeted != null && this.entityCaptureCallback != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && targeted == mc.player) {
            return false;
         }

         String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(targeted.getType()).toString();
         String payload;
         if (this.captureSpecificEntity) {
            String name = targeted.getDisplayName().getString().replaceAll("§.", "").trim();
            payload = "~" + targeted.getStringUUID() + "~" + typeId + "~" + name;
            this.captureSpecificEntity = false;
         } else {
            payload = typeId;
         }

         return this.dispatchEntityCapturePayload(payload);
      } else {
         return false;
      }
   }

   public boolean hasEntityCaptureCallback() {
      return this.entityCaptureCallback != null;
   }

   public void setCaptureSpecificEntity(boolean v) {
      this.captureSpecificEntity = v;
   }

   public boolean isCaptureSpecificEntity() {
      return this.captureSpecificEntity;
   }

   public boolean isTimedCaptureMode() {
      return this.timedCaptureMode;
   }

   public void setTimedCaptureMode(boolean v) {
      if (!v || !autismclient.modules.aD.read_Z()) {
         this.timedCaptureMode = v;
         if (v) {
            this.captureStartNanos = -1L;
            this.captureStartTick = -1;
         }
      }
   }

   public void onServerTimeUpdate() {
      long now = System.currentTimeMillis();
      if (this.lastTimeUpdateMs > 0L) {
         long wallDeltaMs = now - this.lastTimeUpdateMs;
         if (wallDeltaMs > 0L) {
            double measured = wallDeltaMs / 20.0;
            measured = Math.max(50.0, Math.min(200.0, measured));
            this.msPerTickEma = this.msPerTickEma * 0.3 + measured * 0.7;
         }
      }

      this.lastTimeUpdateMs = now;
   }

   public double getEstimatedMsPerTick() {
      return this.msPerTickEma;
   }

   public double getEstimatedTps() {
      return this.lastTimeUpdateMs <= 0L ? -1.0 : 1000.0 / this.msPerTickEma;
   }

   public void incrementClientTick() {
      this.clientTick++;
   }

   public int getClientTick() {
      return this.clientTick;
   }

   public boolean isPlacePreviewActive() {
      return this.placePreviewActive;
   }

   public void setPlacePreviewActive(boolean active) {
      this.placePreviewActive = active;
   }

   public enum ReplayMode {
      REGENERATE,
      EXACT;
   }

   public static class DelayedPacket {
      private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
      public final Packet<?> packet;
      public final int id;
      private volatile AutismSessionState.ReplayMode replayMode;
      private volatile int delay;
      private volatile boolean sent;

      public DelayedPacket(Packet<?> packet, int delay) {
         this(packet, delay, AutismSessionState.ReplayMode.REGENERATE);
      }

      public DelayedPacket(Packet<?> packet, int delay, AutismSessionState.ReplayMode replayMode) {
         this.packet = packet;
         this.id = NEXT_ID.getAndIncrement();
         this.delay = delay;
         this.sent = false;
         this.replayMode = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      }

      public DelayedPacket(Packet<?> packet, int delay, int existingId) {
         this(packet, delay, existingId, AutismSessionState.ReplayMode.REGENERATE);
      }

      public DelayedPacket(Packet<?> packet, int delay, int existingId, AutismSessionState.ReplayMode replayMode) {
         this.packet = packet;
         this.id = existingId;
         this.delay = delay;
         this.sent = false;
         this.replayMode = replayMode == null ? AutismSessionState.ReplayMode.REGENERATE : replayMode;
      }

      public int getId() {
         return this.id;
      }

      public int getDelay() {
         return this.delay;
      }

      public AutismSessionState.ReplayMode getReplayMode() {
         return this.replayMode;
      }

      public boolean isExactReplay() {
         return this.replayMode == AutismSessionState.ReplayMode.EXACT;
      }

      public synchronized void setDelay(int newDelay) {
         if (!this.sent) {
            this.delay = newDelay;
         }
      }

      public synchronized void setReplayMode(AutismSessionState.ReplayMode mode) {
         if (!this.sent) {
            this.replayMode = mode == null ? AutismSessionState.ReplayMode.REGENERATE : mode;
         }
      }

      public boolean isSent() {
         return this.sent;
      }

      synchronized void markSent() {
         this.sent = true;
      }

      public static void resetIdCounter() {
         NEXT_ID.set(1);
      }
   }

   public static final class TimingMeasurement {
      public final String label;
      public final long deltaMs;
      public final String startEvent;
      public final String endEvent;
      public final long capturedAtMs;

      public TimingMeasurement(String label, long deltaMs, String startEvent, String endEvent, long capturedAtMs) {
         this.label = label == null ? "" : label;
         this.deltaMs = deltaMs;
         this.startEvent = startEvent == null ? "" : startEvent;
         this.endEvent = endEvent == null ? "" : endEvent;
         this.capturedAtMs = capturedAtMs;
      }
   }

   public enum DelayUnit {
      TICKS,
      MS;
   }

   public record DelayQueueSnapshot(List<AutismSessionState.DelayedPacket> visiblePackets, int totalCount) {
   }

   public static final class PluginScanResult {
      private final String address;
      private final String contextSignature;
      private final List<String> plugins;
      private final Map<String, List<String>> pluginCommands;
      private final Map<String, String> pluginEvidence;

      private PluginScanResult(
         String address, String contextSignature, List<String> plugins, Map<String, List<String>> pluginCommands, Map<String, String> pluginEvidence
      ) {
         this.address = address;
         this.contextSignature = contextSignature == null ? "" : contextSignature;
         this.plugins = plugins == null ? List.of() : new ArrayList<>(plugins);
         this.pluginCommands = copyCommandMap(pluginCommands);
         this.pluginEvidence = copyEvidenceMap(pluginEvidence);
      }

      public String getAddress() {
         return this.address;
      }

      public List<String> getPlugins() {
         return new ArrayList<>(this.plugins);
      }

      public String getContextSignature() {
         return this.contextSignature;
      }

      public Map<String, List<String>> getPluginCommands() {
         return copyCommandMap(this.pluginCommands);
      }

      public Map<String, String> getPluginEvidence() {
         return copyEvidenceMap(this.pluginEvidence);
      }

      private AutismSessionState.PluginScanResult copy() {
         return new AutismSessionState.PluginScanResult(this.address, this.contextSignature, this.plugins, this.pluginCommands, this.pluginEvidence);
      }

      private static Map<String, List<String>> copyCommandMap(Map<String, List<String>> source) {
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

      private static Map<String, String> copyEvidenceMap(Map<String, String> source) {
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
