package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.api.event.AddonEvents;
import autismclient.compat263.CompatSwingPacket;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.AutismProfilesOverlay;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.PayloadChannelFilterManager;
import autismclient.util.PayloadSubscriptionManager;
import autismclient.util.PayloadNotificationTracker;
import autismclient.util.PayloadStudyTracker;
import autismclient.util.bK;
import autismclient.util.PluginSignatureCatalog;
import autismclient.util.AutismProfileManager;
import autismclient.util.OverlayManager;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.AutismOverlay;
import autismclient.util.cf;
import autismclient.util.AutismSessionState;
import autismclient.util.ThemePaletteManager;
import autismclient.util.OverlayScreenController;
import autismclient.util.SodiumRegionResourceProbe;
import autismclient.util.macro.MacroExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class ModuleRuntimeManager {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final ModuleRuntimeManager INSTANCE = new ModuleRuntimeManager();
   private static final long JOIN_PROBE_WINDOW_MS = 20000L;
   private static final int MAX_EARLY_PAYLOAD_BUFFER = 96;
   private static final int MAX_SEEN_FINGERPRINTS = 256;
   private static final boolean TRACE_PAYLOADS = Boolean.getBoolean("autism.payload.trace");
   private static final Set<Class<?>> IGNORED_DEFAULT_C2S_PACKETS = Set.of(
      ServerboundKeepAlivePacket.class,
      ServerboundPongPacket.class,
      ServerboundClientTickEndPacket.class,
      ServerboundChunkBatchReceivedPacket.class,
      ServerboundClientCommandPacket.class,
      ServerboundContainerClosePacket.class,
      CompatSwingPacket.class,
      ServerboundPunchPacket.class,
      ServerboundPlayerInputPacket.class,
      ServerboundMovePlayerPacket.class,
      PosRot.class,
      Rot.class,
      StatusOnly.class,
      Pos.class
   );
   private AutismConfig config;
   private boolean initialized;
   private boolean wasLoadGuiPressed;
   private boolean wasFlushQueuePressed;
   private boolean wasClearQueuePressed;
   private boolean wasToggleLoggerPressed;
   private boolean wasToggleSendPressed;
   private boolean wasToggleDelayPressed;
   private boolean wasModuleMenuPressed;
   private final Map<String, Boolean> macroKeyPressStates = new HashMap<>();
   private List<autismclient.util.AutismMacro> keyboundMacros = List.of();
   private long cachedMacroRevision = -1L;
   private int delayToggleTickCounter;
   private int packetLoggerTick;
   private AutismPacketLoggerOverlay packetLoggerOverlay;
   private PayloadChannelFilterManager payloadFilterManager;
   private volatile boolean payloadFilterRuleCacheValid;
   private volatile boolean hasEnabledPayloadFilterRule;
   private AutismServerInfoOverlay serverInfoOverlay;
   private AutismOverlay profilesOverlay;
   private final Deque<ModuleRuntimeManager.Inner_StreamUtil> earlyPayloadCaptures = new ArrayDeque<>(96);
   private final Set<Packet<?>> seenPayloadPackets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
   private final Deque<String> seenFingerprintOrder = new ArrayDeque<>(256);
   private final Set<String> seenFingerprints = new HashSet<>();
   private volatile boolean joinedPlayConnection;
   private volatile boolean spawnedInWorld;
   private long lastTickErrorLogMs;
   private int watchdogMissingJoinTicks;
   private long lastWatchdogLogMs;
   private static final int WATCHDOG_THRESHOLD_TICKS = 20;
   private volatile long joinProbeWindowUntilMs;
   private volatile boolean autoProbePending;
   private volatile long autoProbeStartedMs;
   private static final long AUTO_PROBE_MIN_WAIT_MS = 2000L;
   private static final long AUTO_PROBE_TIMEOUT_MS = 25000L;
   private Module cachedXCarryModule;
   private int cachedXCarryRevision = -1;

   private ModuleRuntimeManager() {
   }

   public static ModuleRuntimeManager getInstance() {
      return INSTANCE;
   }

   public void initialize() {
      if (!this.initialized) {
         this.config = AutismConfig.load();
         this.runInitStepSafely("applyRuntimeDefaults", () -> this.config.applyRuntimeDefaults());
         AutismConfig.setGlobal(this.config);
         if (!AutismLiteVariant.enabled()) {
            this.runInitStepSafely("themeWarm", () -> ThemePaletteManager.getPalette());
         }

         this.runInitStepSafely("moduleRegistry", () -> al.init_V_AutismConfig(this.config));
         this.runInitStepSafely("c2sPackets", () -> {
            if (this.config.c2sPackets.isEmpty()) {
               this.config.c2sPackets = this.serializePacketClassNames(this.getDefaultC2sPackets());
            }
         });
         this.runInitStepSafely("sharedState", this::syncConfigToSessionState);
         this.runInitStepSafely("packHide", () -> {
            if (aD.init_Z()) {
               aD.read_V();
            }
         });
         this.runInitStepSafely("lanSync", () -> {
            if (this.config.lanSyncEnabled && !aD.init_Z()) {
               autismclient.util.LanSyncManager.getInstance().startService();
            }
         });
         if (!AutismLiteVariant.enabled()) {
            this.runInitStepSafely("profiles", () -> AutismProfileManager.init_bV());
         }

         this.initialized = true;
         autismclient.util.PacketCaptureState.load(this);
      }
   }

   private void runInitStepSafely(String where, Runnable action) {
      try {
         action.run();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Autism] init step '{}' failed; isolated so the mod still activates", where, t);
      }
   }

   public void applyConfig(AutismConfig next) {
      if (next != null) {
         next.applyRuntimeDefaults();
         AutismConfig prev = this.config != null ? this.config : AutismConfig.getGlobal();
         if (prev != null) {
            next.serverPluginScans = prev.serverPluginScans;
            next.lanSyncEnabled = prev.lanSyncEnabled;
         }

         boolean themeChanged = !AutismConfig.sameThemeColors(prev, next);
         boolean filtersChanged = !AutismConfig.samePayloadRules(prev, next);
         Map<String, Boolean> wasEnabled = new LinkedHashMap<>();

         for (Module m : al.init_Collection()) {
            wasEnabled.put(m.id(), m.isEnabled());
         }

         boolean panicBefore = aD.init_Z();
         boolean inWorld = MC != null && MC.level != null;
         this.config = next;
         AutismConfig.setGlobal(next);
         next.save();

         for (Module m : al.init_Collection()) {
            boolean now = m.isEnabled();
            boolean before = Boolean.TRUE.equals(wasEnabled.get(m.id()));
            if (now != before && inWorld && (!now || !aD.load_Z(m))) {
               al.init_V_Module_Z(m, now);
            }
         }

         if (!inWorld) {
            al.read_V();
         }

         al.write_V();
         al.load_V();
         this.syncConfigToSessionState();
         this.invalidatePayloadFilterCache(filtersChanged);
         autismclient.util.PacketCaptureState.load(this);
         autismclient.util.AutismHudManager.init_V();
         if (themeChanged) {
            ThemePaletteManager.reload();
         }

         boolean panicNow = aD.init_Z();
         if (panicNow && !panicBefore) {
            aD.read_V();
         }

         if (!panicNow) {
            if (next.lanSyncEnabled && !autismclient.util.LanSyncManager.getInstance().isRunning()) {
               autismclient.util.LanSyncManager.getInstance().startService();
            } else if (!next.lanSyncEnabled && autismclient.util.LanSyncManager.getInstance().isRunning()) {
               autismclient.util.LanSyncManager.getInstance().stopService();
            }
         }
      }
   }

   public void onClientTick() {
      if (this.initialized && MC != null) {
         try {
            this.updateWorldSpawnState();
         } catch (Throwable t) {
            this.logTickErrorThrottled("worldSpawnState", t);
         }

         try {
            this.runTickWork();
         } catch (Throwable t) {
            this.logTickErrorThrottled("tickWork", t);
         }
      }
   }

   private void logTickErrorThrottled(String where, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - this.lastTickErrorLogMs >= 5000L) {
         this.lastTickErrorLogMs = now;
         AutismClientAddon.LOG.warn("[Autism] tick '{}' failed; isolated to protect the client", where, t);
      }
   }

   private void logWatchdogRecoveryThrottled() {
      long now = System.currentTimeMillis();
      if (now - this.lastWatchdogLogMs >= 5000L) {
         this.lastWatchdogLogMs = now;
         AutismClientAddon.LOG
            .warn(
               "[Autism] active-state watchdog: a live world was present but joinedPlayConnection was false; force-restored isActive() (HUD/overlay would otherwise stay hidden until reconnect)"
            );
      }
   }

   private void runTickWork() {
      if (AutismLiteVariant.enabled() || autismclient.e.CharEncoder.init()) {
         autismclient.util.PacketCaptureState.init_V_PoolEntry(this);
         if (!AutismLiteVariant.enabled()) {
            AutismProfileManager.init_bV().load();
         }

         autismclient.util.macro.PingSpoofScheduler.update();
         BlinkPacketManager.tick();
         autismclient.util.BedSleepDesyncHelper.init(MC);
         boolean hidden = aD.init_Z();
         if (hidden) {
            this.autoProbePending = false;
            this.joinProbeWindowUntilMs = 0L;
            PayloadStudyTracker.init_V();
            PayloadNotificationTracker.load();
            AutismServerInfoOverlay hiddenServerInfo = this.getServerInfoOverlay();
            if (hiddenServerInfo != null) {
               hiddenServerInfo.poll_V_9();
            }
         }

         bK.read_V();
         PayloadNotificationTracker.init();
         if (!hidden) {
            PayloadSubscriptionManager.init(MC, this.isPacketLoggerCapturing());
         }

         autismclient.util.LanSyncManager lanSync = autismclient.util.LanSyncManager.getInstance();
         if (!hidden && lanSync.isActiveOrConnecting()) {
            long perf = bK.read_J();
            lanSync.tick();
            bK.load("join.lanSync.tick", perf);
         }

         if (!hidden && autismclient.util.macro.MacroWaitRegistry.hasPendingWaits()) {
            long perf = bK.read_J();
            autismclient.util.macro.MacroWaitRegistry.load(MC);
            bK.load("join.macroConditions.tick", perf);
         }

         AutismSessionState shared = AutismSessionState.getInstance();
         NoFallPauseTimer.read();
         if (!hidden && shared.isStaggeredSendActive()) {
            shared.tickStaggeredSend();
         }

         if (!hidden && cf.init_Z_J(1L)) {
            long perf = bK.read_J();
            al.sync_V();
            bK.init("join.modules.tick", perf, 8000000L);
         }

         if (!hidden) {
            AutoReconnectController.tick();
         }

         this.refreshXCarryActiveState();
         AutismServerInfoOverlay serverInfo = this.getServerInfoOverlay();
         if (!hidden && serverInfo != null && (serverInfo.isVisible() || serverInfo.run_Z_5())) {
            serverInfo.close_V_15();
         }

         if (shared.isDelayPacketsEnabled() && MC.getConnection() != null) {
            if (this.delayToggleTickCounter++ >= 1) {
               this.delayToggleTickCounter = 0;
            }
         } else {
            this.delayToggleTickCounter = 0;
         }

         if (autismclient.util.at.init()) {
            this.pollKeybinds();
         } else {
            this.wasLoadGuiPressed = false;
            this.wasFlushQueuePressed = false;
            this.wasClearQueuePressed = false;
            this.wasToggleLoggerPressed = false;
            this.wasToggleSendPressed = false;
            this.wasToggleDelayPressed = false;
            this.macroKeyPressStates.clear();
         }

         this.packetLoggerTick++;
         AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
         if (logger != null) {
            logger.init_V_I_8(this.packetLoggerTick);
         }

         if (!hidden && AddonEvents.hasTickListeners()) {
            AddonEvents.fireTick(MC);
         }

         this.updateActivityBitmask(hidden, shared, serverInfo);
      }
   }

   private void updateActivityBitmask(boolean hidden, AutismSessionState shared, AutismServerInfoOverlay serverInfo) {
      boolean active = !hidden
         && (
            MacroExecutor.hasVisibleActiveRun()
               || shared.isDelayPacketsEnabled()
               || shared.hasDelayedPackets()
               || shared.hasSendingPackets()
               || shared.isTimedCaptureMode()
               || shared.isGbreakCapturing()
               || shared.hasCaptureCancelCallback()
               || shared.hasAttackCaptureCallback()
               || shared.hasBlockCaptureCallback()
               || shared.hasEntityCaptureCallback()
               || PayloadStudyTracker.init_Z()
               || am.add()
               || AntiVanishModule.isHudVisible()
               || autismclient.util.bi.init_Z()
               || serverInfo != null && serverInfo.run_Z_5()
         );
      cf.init_V_J_Z(2048L, active);
   }

   public void onGameJoin() {
      SodiumRegionResourceProbe.init_V();
      bK.load_V();
      long perf = bK.read_J();
      this.joinedPlayConnection = true;
      this.spawnedInWorld = false;
      if (aD.init_Z()) {
         this.autoProbePending = false;
         this.joinProbeWindowUntilMs = 0L;
      } else {
         this.autoProbePending = this.config != null && this.config.autoProbePlugins;
         if (this.autoProbePending) {
            this.autoProbeStartedMs = System.currentTimeMillis();
         }

         this.extendJoinProbeWindow();
         PayloadSubscriptionManager.init_V();
      }

      this.resetTransientSessionFlags();
      if (aD.init_Z()) {
         aD.read_V();
      }

      if (!aD.init_Z() && this.config != null && this.config.packetLoggerCapturing) {
         this.getOrCreatePacketLoggerOverlay();
      }

      if (this.config.lanSyncEnabled && !aD.init_Z() && !autismclient.util.LanSyncManager.getInstance().isRunning()) {
         autismclient.util.LanSyncManager.getInstance().startService();
      }

      if (!aD.init_Z()) {
         autismclient.util.LanSyncManager.getInstance().refreshClientList();
      }

      if (!aD.init_Z()) {
         al.push_V();
      }

      if (!aD.init_Z()) {
         AddonEvents.fireGameJoin();
      }

      bK.init("join.onGameJoin", perf, 8000000L);
   }

   public void onGameLeave() {
      SodiumRegionResourceProbe.init_V();
      ar.load_V();
      autismclient.util.oresim.OreSimEngine.init_V();
      autismclient.util.oresim.OreSimEngine.write_V();
      this.joinedPlayConnection = false;
      this.spawnedInWorld = false;
      this.autoProbePending = false;
      PluginSignatureCatalog.init();
      PayloadNotificationTracker.load();
      PayloadSubscriptionManager.load();
      AutismServerInfoOverlay leftOverlay = this.getServerInfoOverlay();
      if (leftOverlay != null) {
         leftOverlay.flush_V_17();
      }

      this.joinProbeWindowUntilMs = 0L;
      autismclient.util.PacketCaptureState.load(this);
      AutismSessionState.getInstance().clearDetectedServerVersion();
      this.wasLoadGuiPressed = false;
      this.wasFlushQueuePressed = false;
      this.wasClearQueuePressed = false;
      this.wasToggleLoggerPressed = false;
      this.wasToggleSendPressed = false;
      this.wasToggleDelayPressed = false;
      this.wasModuleMenuPressed = false;
      if (!aD.init_Z()) {
         al.pop_V();
      }

      if (!aD.init_Z()) {
         AddonEvents.fireGameLeft();
      }
   }

   public boolean isActive() {
      return this.initialized && this.spawnedInWorld && this.isInLiveWorld();
   }

   public boolean isInitializedAndVisible() {
      return this.initialized && !aD.init_Z();
   }

   public boolean needsPacketHooks() {
      if (this.isActive() && !aD.init_Z()) {
         AutismSessionState shared = AutismSessionState.getInstance();
         return shared.needsPacketInterception()
            || this.isPacketLoggerCapturing()
            || this.needsServerInfoObservation()
            || al.load_Z()
            || AddonEvents.hasPacketListeners()
            || autismclient.util.macro.PacketGateManager.hasActiveGates()
            || MacroExecutor.needsPacketEvents();
      } else {
         return false;
      }
   }

   public ModuleRuntimeManager.Inner_DataUtil getPacketCapturePlan(boolean playConnection) {
      if (aD.init_Z()) {
         return ModuleRuntimeManager.Inner_DataUtil.none();
      }

      long captureState = autismclient.util.PacketCaptureState.init_J();
      boolean loggerCapture = autismclient.util.PacketCaptureState.init_Z_J(captureState);
      boolean passivePayloadCapture = autismclient.util.PacketCaptureState.load(captureState);
      if (!this.isActive()) {
         return !loggerCapture && !passivePayloadCapture
            ? ModuleRuntimeManager.Inner_DataUtil.none()
            : new ModuleRuntimeManager.Inner_DataUtil(false, passivePayloadCapture, loggerCapture, false);
      }

      AutismSessionState shared = AutismSessionState.getInstance();
      boolean serverInfoObservation = passivePayloadCapture && this.needsServerInfoObservation();
      boolean normalPath = playConnection
         && (
            shared.needsPacketInterception()
               || loggerCapture
               || serverInfoObservation
               || al.load_Z()
               || AddonEvents.hasPacketListeners()
               || autismclient.util.macro.PacketGateManager.hasActiveGates()
               || MacroExecutor.needsPacketEvents()
         );
      return !normalPath && !passivePayloadCapture && !loggerCapture && !serverInfoObservation
         ? ModuleRuntimeManager.Inner_DataUtil.none()
         : new ModuleRuntimeManager.Inner_DataUtil(normalPath, passivePayloadCapture, loggerCapture, serverInfoObservation);
   }

   public boolean hasPassivePayloadWork() {
      return !aD.init_Z() && (this.isJoinProbeWindowActive() || this.hasActivePayloadFilterRule() || PayloadStudyTracker.init_Z());
   }

   public boolean isLoggerCaptureEnabled() {
      return aD.init_Z() ? false : this.isPacketLoggerCapturing();
   }

   public boolean needsAnyPayloadHook() {
      return aD.init_Z() ? false : this.isPacketLoggerCapturing() || this.hasPassivePayloadWork() || this.needsServerInfoObservation() || PayloadStudyTracker.init_Z();
   }

   private boolean needsServerInfoObservation() {
      if (this.config != null && this.config.autoProbePlugins && this.autoProbePending) {
         return true;
      }

      AutismServerInfoOverlay overlay = this.getServerInfoOverlay();
      return overlay != null && overlay.put_Z_4();
   }

   public boolean shouldObserveServerInfoPackets() {
      return this.isActive() && !aD.init_Z() ? this.needsServerInfoObservation() : false;
   }

   private AutismServerInfoOverlay getOrCreateAutoProbeServerInfo() {
      AutismServerInfoOverlay overlay = this.getServerInfoOverlay();
      if (overlay != null) {
         return overlay;
      } else {
         return this.config != null && this.config.autoProbePlugins && this.autoProbePending ? this.getOrCreateServerInfoOverlay() : null;
      }
   }

   public void observeOutgoingServerInfoPacket(Packet<?> packet) {
      if (this.shouldObserveServerInfoPackets() && packet != null) {
         AutismServerInfoOverlay overlay = this.getOrCreateAutoProbeServerInfo();
         if (overlay != null) {
            overlay.init_V_Packet(packet);
            overlay.load(packet);
         }
      }
   }

   public void observeIncomingServerInfoPacket(Packet<?> packet) {
      if (this.shouldObserveServerInfoPackets() && packet != null) {
         AutismServerInfoOverlay overlay = this.getOrCreateAutoProbeServerInfo();
         if (overlay != null) {
            if (packet instanceof ClientboundCommandSuggestionsPacket suggestions) {
               overlay.init_V_I_ClientboundCommand(suggestions.id(), suggestions);
            }

            if (packet instanceof ClientboundOpenScreenPacket) {
               overlay.read(packet);
            }

            if (packet instanceof ClientboundCommandsPacket
               && this.spawnedInWorld
               && !this.autoProbePending
               && this.config != null
               && this.config.autoProbePlugins) {
               this.autoProbePending = true;
               this.autoProbeStartedMs = System.currentTimeMillis();
            }
         }
      }
   }

   private boolean shouldStudyPayloads() {
      return this.isJoinProbeWindowActive() || this.needsServerInfoObservation() || this.isPacketLoggerCapturing() || PayloadStudyTracker.init_Z();
   }

   public void invalidatePayloadFilterCache() {
      this.invalidatePayloadFilterCache(false);
   }

   public void invalidatePayloadFilterCache(boolean refreshSubscriptions) {
      this.payloadFilterRuleCacheValid = false;
      if (this.payloadFilterManager != null) {
         this.payloadFilterManager.reloadFromConfig();
      }

      if (refreshSubscriptions) {
         PayloadSubscriptionManager.init_V();
      }

      autismclient.util.PacketCaptureState.load(this);
   }

   private boolean hasActivePayloadFilterRule() {
      if (this.payloadFilterRuleCacheValid) {
         return this.hasEnabledPayloadFilterRule;
      }

      boolean enabled = this.computeActivePayloadFilterRule();
      this.hasEnabledPayloadFilterRule = enabled;
      this.payloadFilterRuleCacheValid = true;
      return enabled;
   }

   private boolean computeActivePayloadFilterRule() {
      AutismConfig cfg = AutismConfig.getGlobal();
      if (cfg != null && cfg.packetLoggerPayloadFilters != null) {
         for (AutismConfig.PayloadChannelFilterRule rule : cfg.packetLoggerPayloadFilters) {
            if (rule != null && rule.enabled) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void extendJoinProbeWindowAndRefresh() {
      this.extendJoinProbeWindow();
      PayloadSubscriptionManager.init_V();
   }

   public boolean isJoinProbeWindowActive() {
      return System.currentTimeMillis() <= this.joinProbeWindowUntilMs;
   }

   public long getJoinProbeWindowUntilMs() {
      return this.joinProbeWindowUntilMs;
   }

   public boolean isPacketLoggerCapturing() {
      if (aD.init_Z()) {
         return false;
      }

      AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
      return logger != null ? !logger.push_Z_10() : this.config != null && this.config.packetLoggerCapturing;
   }

   public boolean captureCustomPayloadPacket(Packet<?> packet, String direction, String protocolPhase) {
      return this.captureCustomPayloadInternal(packet, direction, protocolPhase, "connection");
   }

   public boolean captureCustomPayloadFromSource(Packet<?> packet, String direction, String protocolPhase, String source) {
      return this.captureCustomPayloadInternal(packet, direction, protocolPhase, source != null && !source.isBlank() ? source : "codec");
   }

   public boolean captureCustomPayloadBytes(byte[] frameBytes, String direction, String protocolPhase, String source) {
      if (!aD.init_Z() && frameBytes != null && frameBytes.length != 0) {
         AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.inspectWireFrame(frameBytes, direction, protocolPhase);
         if (snapshot == null) {
            return false;
         }

         String fingerprint = this.fingerprintPayloadSnapshot(snapshot);
         if (this.hasSeenPayloadFingerprint(fingerprint)) {
            return true;
         }

         if (this.shouldStudyPayloads()) {
            this.studyPayloadSnapshot(snapshot);
         }

         boolean loggerCapture = this.isPacketLoggerCapturing();
         boolean passiveWork = this.hasPassivePayloadWork();
         if (!loggerCapture && !passiveWork) {
            return false;
         }

         AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
         boolean captured = false;
         if (loggerCapture) {
            if (logger == null) {
               this.bufferEarlyPayloadSnapshot(snapshot, direction, payloadClassForDirection(direction));
            } else {
               logger.init_V_J(System.currentTimeMillis(), this.packetLoggerTick, direction == null ? "" : direction, payloadClassForDirection(direction), snapshot);
            }

            captured = true;
         } else if (passiveWork) {
            PayloadChannelFilterManager.Inner_StreamUtil filterMatch = this.matchPayloadFilter(snapshot.channel(), direction);
            if (filterMatch != null) {
               if (logger != null) {
                  logger.init_V_J(System.currentTimeMillis(), this.packetLoggerTick, direction == null ? "" : direction, payloadClassForDirection(direction), snapshot);
               } else {
                  PayloadNotificationTracker.init(snapshot.channel(), direction, filterMatch);
                  this.bufferEarlyPayloadSnapshot(snapshot, direction, payloadClassForDirection(direction));
               }

               captured = true;
            }
         }

         if (captured) {
            this.rememberPayloadFingerprint(fingerprint);
            this.traceCapturedSnapshot(snapshot, source);
         }

         return captured;
      } else {
         return false;
      }
   }

   private boolean captureCustomPayloadInternal(Packet<?> packet, String direction, String protocolPhase, String source) {
      if (!aD.init_Z() && packet != null) {
         if (packet instanceof BundlePacket<?> bundle) {
            boolean captured = false;

            for (Packet<?> child : unwrapBundlePackets(bundle)) {
               if (this.captureCustomPayloadInternal(child, direction, protocolPhase, source)) {
                  captured = true;
               }
            }

            return captured;
         } else {
            CustomPacketPayload payload = AutismPayloadSupport.extractPayload(packet);
            if (payload == null) {
               return false;
            }

            if (this.wasPayloadPacketSeen(packet)) {
               return true;
            }

            String channel = AutismPayloadSupport.extractChannelId(payload);
            AutismPayloadSupport.rememberProtocolPhase(packet, protocolPhase);
            if (this.shouldStudyPayloads()) {
               this.studyPayloadPacket(packet, direction);
            }

            boolean loggerCapture = this.isPacketLoggerCapturing();
            boolean passiveWork = this.hasPassivePayloadWork();
            if (!loggerCapture && !passiveWork) {
               return false;
            }

            AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
            boolean captured = false;
            if (loggerCapture) {
               if (logger == null) {
                  this.bufferEarlyPayloadPacket(packet, direction);
               } else {
                  logger.load_V_Packet_Str(packet, direction);
               }

               captured = true;
            } else if (passiveWork) {
               PayloadChannelFilterManager.Inner_StreamUtil filterMatch = this.matchPayloadFilter(channel, direction);
               if (filterMatch != null) {
                  if (logger != null) {
                     logger.load_V_Packet_Str(packet, direction);
                  } else {
                     PayloadNotificationTracker.init(channel, direction, filterMatch);
                     this.bufferEarlyPayloadPacket(packet, direction);
                  }

                  captured = true;
               }
            }

            if (captured) {
               this.markPayloadPacketSeen(packet);
               this.traceCapturedPacket(packet, channel, direction, protocolPhase, source);
            }

            return captured;
         }
      } else {
         return false;
      }
   }

   private boolean wasPayloadPacketSeen(Packet<?> packet) {
      synchronized (this.seenPayloadPackets) {
         return this.seenPayloadPackets.contains(packet);
      }
   }

   private void markPayloadPacketSeen(Packet<?> packet) {
      if (packet != null) {
         synchronized (this.seenPayloadPackets) {
            this.seenPayloadPackets.add(packet);
         }
      }
   }

   private void traceCapturedPacket(Packet<?> packet, String channel, String direction, String protocolPhase, String source) {
      if (TRACE_PAYLOADS) {
         int size = -1;

         try {
            CustomPacketPayload payload = AutismPayloadSupport.extractPayload(packet);
            size = AutismPayloadSupport.encodePayloadBytes(payload).length;
         } catch (Throwable var8) {
         }

         AutismClientAddon.LOG
            .info(
               "[Autism Payload] {} {} {} {}B via {}",
               direction == null ? "" : direction,
               protocolPhase == null ? "" : protocolPhase,
               channel != null && !channel.isBlank() ? channel : "<unknown>",
               size,
               source == null ? "" : source
            );
      }
   }

   private void traceCapturedSnapshot(AutismPayloadSupport.PayloadEntry snapshot, String source) {
      if (TRACE_PAYLOADS && snapshot != null) {
         AutismClientAddon.LOG
            .info(
               "[Autism Payload] {} {} {} {}B via {}",
               snapshot.direction() == null ? "" : snapshot.direction(),
               snapshot.protocolPhase() == null ? "" : snapshot.protocolPhase(),
               snapshot.channel() != null && !snapshot.channel().isBlank() ? snapshot.channel() : "<unknown>",
               snapshot.byteLength(),
               source == null ? "" : source
            );
      }
   }

   public void capturePassivePayload(Packet<?> packet, String direction) {
      this.capturePassivePayload(packet, direction, "");
   }

   public void capturePassivePayload(Packet<?> packet, String direction, String protocolPhase) {
      if (!aD.init_Z() && this.hasPassivePayloadWork()) {
         this.captureCustomPayloadInternal(packet, direction, protocolPhase, "passive");
      }
   }

   private void bufferEarlyPayloadPacket(Packet<?> packet, String direction) {
      long perf = bK.read_J();
      AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.inspectPacket(packet, direction);
      if (snapshot != null) {
         this.bufferEarlyPayloadSnapshot(snapshot, direction, packet.getClass());
         bK.load("join.passivePayload.snapshot", perf);
      }
   }

   private void bufferEarlyPayloadSnapshot(AutismPayloadSupport.PayloadEntry snapshot, String direction, Class<?> packetClass) {
      if (snapshot != null) {
         synchronized (this.earlyPayloadCaptures) {
            while (this.earlyPayloadCaptures.size() >= 96) {
               this.earlyPayloadCaptures.removeFirst();
            }

            this.earlyPayloadCaptures
               .addLast(new ModuleRuntimeManager.Inner_StreamUtil(System.currentTimeMillis(), this.packetLoggerTick, direction == null ? "" : direction, packetClass, snapshot));
         }
      }
   }

   private PayloadChannelFilterManager.Inner_StreamUtil matchPayloadFilter(String channel, String direction) {
      if (channel != null && !channel.isBlank()) {
         PayloadChannelFilterManager listeners = this.getPayloadFilterManager();
         return listeners.hasAnyEnabledRule() ? listeners.findMatchingRule(channel, direction) : null;
      } else {
         return null;
      }
   }

   private PayloadChannelFilterManager getPayloadFilterManager() {
      if (this.payloadFilterManager == null) {
         this.payloadFilterManager = new PayloadChannelFilterManager();
      }

      return this.payloadFilterManager;
   }

   private static List<Packet<?>> unwrapBundlePackets(BundlePacket<?> bundle) {
      if (bundle == null) {
         return List.of();
      }

      List<Packet<?>> packets = new ArrayList<>();

      try {
         for (Object child : bundle.subPackets()) {
            if (child instanceof Packet<?> packet) {
               packets.add(packet);
            }
         }
      } catch (Throwable var5) {
      }

      return packets;
   }

   private static Class<?> payloadClassForDirection(String direction) {
      return direction != null && direction.equalsIgnoreCase("C2S") ? ServerboundCustomPayloadPacket.class : ClientboundCustomPayloadPacket.class;
   }

   private String fingerprintPayloadSnapshot(AutismPayloadSupport.PayloadEntry snapshot) {
      return snapshot == null
         ? ""
         : (snapshot.direction() == null ? "" : snapshot.direction())
            + "|"
            + (snapshot.protocolPhase() == null ? "" : snapshot.protocolPhase())
            + "|"
            + snapshot.packetId()
            + "|"
            + (snapshot.channel() == null ? "" : snapshot.channel())
            + "|"
            + snapshot.byteLength()
            + "|"
            + Arrays.hashCode(snapshot.rawBytes());
   }

   private boolean hasSeenPayloadFingerprint(String fingerprint) {
      if (fingerprint != null && !fingerprint.isBlank()) {
         synchronized (this.seenFingerprints) {
            return this.seenFingerprints.contains(fingerprint);
         }
      } else {
         return false;
      }
   }

   private void rememberPayloadFingerprint(String fingerprint) {
      if (fingerprint != null && !fingerprint.isBlank()) {
         synchronized (this.seenFingerprints) {
            if (this.seenFingerprints.add(fingerprint)) {
               this.seenFingerprintOrder.addLast(fingerprint);

               while (this.seenFingerprintOrder.size() > 256) {
                  String removed = this.seenFingerprintOrder.removeFirst();
                  this.seenFingerprints.remove(removed);
               }
            }
         }
      }
   }

   public void notifyStandaloneAlwaysEnabled() {
      AutismClientMessaging.sendPrefixed("Autism is always enabled in standalone mode.");
   }

   private void extendJoinProbeWindow() {
      if (!aD.init_Z()) {
         this.joinProbeWindowUntilMs = Math.max(this.joinProbeWindowUntilMs, System.currentTimeMillis() + 20000L);
         autismclient.util.PacketCaptureState.load(this);
      }
   }

   public void appendItemTooltipLines(ItemStack stack, List<?> lines) {
      al.init_V_ItemStack_List(stack, lines);
   }

   public boolean onPacketSend(Packet<?> packet) {
      return this.onPacketSend(packet, false);
   }

   public boolean onPacketSend(Packet<?> packet, boolean payloadLoggedEarly) {
      long perf = bK.read_J();

      try {
         if (aD.init_Z()) {
            return false;
         }

         this.observeOutgoingServerInfoPacket(packet);
         if (this.shouldStudyPayloads()) {
            this.studyPayloadPacket(packet, "C2S");
         }

         AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
         if (al.init_Z_Packet(packet)) {
            return true;
         }

         if (AddonEvents.firePacketSend(packet)) {
            return true;
         }

         if (logger == null) {
            return false;
         }

         if (!payloadLoggedEarly && !logger.init_Z_Class(packet.getClass())) {
            logger.init_V_Packet_Str(packet, "C2S");
         }

         return false;
      } finally {
         bK.load("join.packetSendHook", perf);
      }
   }

   public boolean onPacketReceive(Packet<?> packet) {
      return this.onPacketReceive(packet, false);
   }

   public boolean onPacketReceive(Packet<?> packet, boolean payloadLoggedEarly) {
      long perf = bK.read_J();

      try {
         if (aD.init_Z()) {
            return false;
         }

         this.observeIncomingServerInfoPacket(packet);
         if (this.shouldStudyPayloads()) {
            this.studyPayloadPacket(packet, "S2C");
         }

         AutismPacketLoggerOverlay logger = this.getPacketLoggerOverlay();
         if (al.load_Z_Packet(packet)) {
            return true;
         }

         AddonEvents.firePacketReceive(packet);
         if (logger == null) {
            return false;
         }

         if (!payloadLoggedEarly && !logger.init_Z_Class(packet.getClass())) {
            logger.init_V_Packet_Str(packet, "S2C");
         }

         return false;
      } finally {
         bK.load("join.packetReceiveHook", perf);
      }
   }

   private void studyPayloadPacket(Packet<?> packet, String direction) {
      if (packet != null && AutismPacketLoggerOverlay.init_Z_Packet(packet)) {
         CustomPacketPayload payload = AutismPayloadSupport.extractPayload(packet);
         if (payload != null) {
            String channel = AutismPayloadSupport.extractChannelId(payload);
            boolean inbound = "S2C".equalsIgnoreCase(direction);
            if (inbound) {
               PayloadSubscriptionManager.load_V(channel);
            }

            try {
               AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.inspectPacket(packet, direction);
               PayloadStudyTracker.init_Z(snapshot);
               if (inbound) {
                  PayloadSubscriptionManager.init(snapshot);
               }
            } catch (Throwable var10) {
            }

            if (inbound && PluginSignatureCatalog.init_Z(channel)) {
               try {
                  AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.inspectPacket(packet, direction);
                  if (snapshot == null) {
                     return;
                  }

                  boolean changed = PluginSignatureCatalog.init(this.getCurrentServerAddress(), this.getCurrentServerBrand(), snapshot);
                  if (changed) {
                     AutismServerInfoOverlay overlay = this.getServerInfoOverlay();
                     if (overlay != null) {
                        overlay.put_V_8();
                     }
                  }
               } catch (Throwable var9) {
               }
            }
         }
      }
   }

   private void studyPayloadSnapshot(AutismPayloadSupport.PayloadEntry snapshot) {
      if (snapshot != null) {
         String channel = snapshot.channel();
         PayloadStudyTracker.init_Z(snapshot);
         boolean inbound = "S2C".equalsIgnoreCase(snapshot.direction());
         if (inbound) {
            PayloadSubscriptionManager.load_V(channel);
            PayloadSubscriptionManager.init(snapshot);
         }

         if (inbound && PluginSignatureCatalog.init_Z(channel)) {
            try {
               boolean changed = PluginSignatureCatalog.init(this.getCurrentServerAddress(), this.getCurrentServerBrand(), snapshot);
               if (changed) {
                  AutismServerInfoOverlay overlay = this.getServerInfoOverlay();
                  if (overlay != null) {
                     overlay.put_V_8();
                  }
               }
            } catch (Throwable var6) {
            }
         }
      }
   }

   private String getCurrentServerBrand() {
      if (MC != null && MC.getConnection() != null) {
         String brand = MC.getConnection().serverBrand();
         return brand == null ? "" : brand;
      } else {
         return "";
      }
   }

   private String getCurrentServerAddress() {
      if (MC == null) {
         return "";
      }

      ServerData entry = MC.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim().toLowerCase(Locale.ROOT);
      }

      if (MC.getConnection() != null && MC.getConnection().getConnection() != null) {
         SocketAddress address = MC.getConnection().getConnection().getRemoteAddress();
         if (address instanceof InetSocketAddress inet) {
            String host = inet.getHostString();
            if ((host == null || host.isBlank()) && inet.getAddress() != null) {
               host = inet.getAddress().getHostAddress();
            }

            if (host != null && !host.isBlank()) {
               return (host + ":" + inet.getPort()).trim().toLowerCase(Locale.ROOT);
            }
         } else if (address != null) {
            String raw = address.toString();
            if (raw != null && !raw.isBlank()) {
               return raw.replaceFirst("^/", "").trim().toLowerCase(Locale.ROOT);
            }
         }
      }

      return "";
   }

   public AutismPacketLoggerOverlay getOrCreatePacketLoggerOverlay() {
      if (this.packetLoggerOverlay == null && MC != null && MC.font != null) {
         this.packetLoggerOverlay = new AutismPacketLoggerOverlay(MC.font);
         this.packetLoggerOverlay.flush_V_13();
         this.drainEarlyPayloadsToLogger(this.packetLoggerOverlay);
      }

      return this.packetLoggerOverlay;
   }

   public AutismPacketLoggerOverlay getPacketLoggerOverlay() {
      return this.packetLoggerOverlay;
   }

   private void drainEarlyPayloadsToLogger(AutismPacketLoggerOverlay logger) {
      if (logger != null) {
         List<ModuleRuntimeManager.Inner_StreamUtil> captures;
         synchronized (this.earlyPayloadCaptures) {
            if (this.earlyPayloadCaptures.isEmpty()) {
               return;
            }

            captures = new ArrayList<>(this.earlyPayloadCaptures);
            this.earlyPayloadCaptures.clear();
         }

         for (ModuleRuntimeManager.Inner_StreamUtil capture : captures) {
            logger.init_V_J(capture.timestampMs(), capture.tick(), capture.direction(), capture.packetClass(), capture.payload());
         }
      }
   }

   public AutismServerInfoOverlay getOrCreateServerInfoOverlay() {
      if (this.serverInfoOverlay == null && MC != null && MC.font != null) {
         this.serverInfoOverlay = new AutismServerInfoOverlay(MC.font);
         this.serverInfoOverlay.update_V_21();
      }

      return this.serverInfoOverlay;
   }

   public AutismServerInfoOverlay getServerInfoOverlay() {
      return this.serverInfoOverlay;
   }

   public boolean isNoPauseOnLostFocus() {
      return this.config.noPauseOnLostFocus;
   }

   public boolean isLanSyncEnabled() {
      return this.config.lanSyncEnabled;
   }

   public boolean isPretendPackAccepted() {
      return this.config != null ? this.config.pretendPackAccepted : AutismConfig.getGlobal().pretendPackAccepted;
   }

   public boolean isSpoofClientVanilla() {
      return this.config != null ? this.config.spoofClientVanilla : AutismConfig.getGlobal().spoofClientVanilla;
   }

   public boolean isInventoryMoveEnabled() {
      Module module = al.init_Module_Str("inv-move");
      return module != null ? module.isEnabled() : this.config != null && this.config.inventoryMove;
   }

   public void setInventoryMoveEnabled(boolean value) {
      if (this.config != null) {
         this.config.inventoryMove = value;
         Module module = al.init_Module_Str("inv-move");
         if (module != null && module.isEnabled() != value) {
            module.setEnabledSilently(value);
         }

         this.saveConfig();
      }
   }

   public boolean isXCarryEnabled() {
      Module module = this.getXCarryModule();
      return module != null ? module.isEnabled() : this.config != null && this.config.xCarry;
   }

   public void setXCarryEnabled(boolean value) {
      if (this.config != null) {
         this.config.xCarry = value;
         Module module = al.init_Module_Str("xcarry");
         if (module != null && module.isEnabled() != value) {
            module.setEnabledSilently(value);
         }

         this.saveConfig();
      }
   }

   public void setPretendPackAccepted(boolean value) {
      this.config.pretendPackAccepted = value;
      AutismSessionState.getInstance().setPretendPackAccepted(value);
      this.saveConfig();
   }

   public void setSpoofClientVanilla(boolean value) {
      if (this.config != null) {
         this.config.spoofClientVanilla = value;
         this.saveConfig();
      }
   }

   public boolean isAutoDenyResourcePack() {
      return this.config != null ? this.config.autoDenyResourcePack : AutismConfig.getGlobal().autoDenyResourcePack;
   }

   public void setAutoDenyResourcePack(boolean value) {
      this.config.autoDenyResourcePack = value;
      AutismSessionState.getInstance().setAutoDenyResourcePack(value);
      this.saveConfig();
   }

   public boolean isUseMsSleepMode() {
      return this.config.useMsSleepMode;
   }

   public int getMsSleepInterval() {
      return this.config.msSleepInterval;
   }

   public boolean isInstantExecutionMode() {
      return this.config.instantExecutionMode;
   }

   public int getActionDelayUs() {
      return this.config.actionDelayUs;
   }

   public boolean isPacketBurstMode() {
      return this.config.packetBurstMode;
   }

   public boolean isUseDirectFlush() {
      return this.config.useDirectFlush;
   }

   public boolean isForceChannelFlush() {
      return this.config.forceChannelFlush;
   }

   public boolean isFlushQueueOnDelayDisable() {
      return this.config != null && this.config.flushQueueOnDelayDisable;
   }

   public void setFlushQueueOnDelayDisable(boolean value) {
      if (this.config != null) {
         this.config.flushQueueOnDelayDisable = value;
         this.saveConfig();
      }
   }

   public boolean isCaptureAsExact() {
      return this.config != null && this.config.captureAsExact;
   }

   public void setCaptureAsExact(boolean value) {
      if (this.config != null) {
         this.config.captureAsExact = value;
         this.saveConfig();
      }
   }

   public boolean isUseCustomPackets() {
      return this.config.useCustomPackets;
   }

   public void setUseCustomPackets(boolean value) {
      this.config.useCustomPackets = value;
      AutismSessionState.getInstance().setUseCustomPackets(value);
      this.saveConfig();
   }

   public void setSendGuiPackets(boolean value) {
      this.config.sendGuiPackets = value;
      AutismSessionState.getInstance().setSendPacketsEnabled(value);
   }

   public boolean setAndSaveSendGuiPackets(boolean value) {
      this.setSendGuiPackets(value);
      this.saveConfig();
      return value;
   }

   public void setDelayGuiPackets(boolean value) {
      this.config.delayGuiPackets = value;
      AutismSessionState.getInstance().setDelayPacketsEnabled(value);
   }

   public String getCommandPrefix() {
      return this.config == null ? "" : (this.config.commandPrefix == null ? "" : this.config.commandPrefix);
   }

   public void setCommandPrefix(String prefix) {
      if (this.config != null) {
         this.config.commandPrefix = autismclient.util.CommandPrefixHelper.init_Str(prefix);
         this.saveConfig();
      }
   }

   public Map<Integer, String> getCommandBinds() {
      if (this.config == null) {
         return Collections.emptyMap();
      }

      if (this.config.commandBinds == null) {
         this.config.commandBinds = new LinkedHashMap<>();
      }

      return this.config.commandBinds;
   }

   public boolean hasCommandBinds() {
      return this.config != null && this.config.commandBinds != null && !this.config.commandBinds.isEmpty();
   }

   public void setCommandBind(int key, String command) {
      if (this.config != null) {
         if (this.config.commandBinds == null) {
            this.config.commandBinds = new LinkedHashMap<>();
         }

         if (command != null && !command.isBlank()) {
            this.config.commandBinds.put(key, command);
         } else {
            this.config.commandBinds.remove(key);
         }

         this.saveConfig();
      }
   }

   public void removeCommandBind(int key) {
      if (this.config != null && this.config.commandBinds != null) {
         this.config.commandBinds.remove(key);
         this.saveConfig();
      }
   }

   private Module getXCarryModule() {
      int revision = al.init_I();
      if (revision != this.cachedXCarryRevision) {
         this.cachedXCarryModule = al.init_Module_Str("xcarry");
         this.cachedXCarryRevision = revision;
      }

      return this.cachedXCarryModule;
   }

   public boolean isXCarryUseCrafting() {
      Module m = this.getXCarryModule();
      if (m == null) {
         return true;
      }

      String v = m.value("use-crafting");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public boolean isXCarryUseArmor() {
      Module m = this.getXCarryModule();
      if (m == null) {
         return true;
      }

      String v = m.value("use-armor");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public boolean isXCarryUseOffhand() {
      Module m = this.getXCarryModule();
      if (m == null) {
         return true;
      }

      String v = m.value("use-offhand");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public void setXCarryUseCrafting(boolean v) {
      Module m = this.getXCarryModule();
      if (m != null) {
         m.setValue("use-crafting", Boolean.toString(v));
      }
   }

   public void setXCarryUseArmor(boolean v) {
      Module m = this.getXCarryModule();
      if (m != null) {
         m.setValue("use-armor", Boolean.toString(v));
      }
   }

   public void setXCarryUseOffhand(boolean v) {
      Module m = this.getXCarryModule();
      if (m != null) {
         m.setValue("use-offhand", Boolean.toString(v));
      }
   }

   public boolean isXCarryCarryCursor() {
      Module m = this.getXCarryModule();
      if (m == null) {
         return true;
      }

      String v = m.value("carry-cursor");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public void setXCarryCarryCursor(boolean v) {
      Module m = this.getXCarryModule();
      if (m != null) {
         m.setValue("carry-cursor", Boolean.toString(v));
      }
   }

   public Set<Integer> getXCarrySlotSet() {
      LinkedHashSet<Integer> s = new LinkedHashSet<>();
      if (this.isXCarryUseCrafting()) {
         s.add(1);
         s.add(2);
         s.add(3);
         s.add(4);
         s.add(0);
      }

      if (this.isXCarryUseArmor()) {
         s.add(5);
         s.add(6);
         s.add(7);
         s.add(8);
      }

      if (this.isXCarryUseOffhand()) {
         s.add(45);
      }

      if (s.isEmpty()) {
         s.add(1);
         s.add(2);
         s.add(3);
         s.add(4);
         s.add(0);
      }

      return s;
   }

   public int toggleDelayPackets(boolean value) {
      this.setDelayGuiPackets(value);
      this.saveConfig();
      return !value && this.isFlushQueueOnDelayDisable() && MC != null && MC.getConnection() != null
         ? AutismSessionState.getInstance().flushDelayedPackets(MC.getConnection())
         : 0;
   }

   public int flushPacketQueue() {
      return MC != null && MC.getConnection() != null ? AutismSessionState.getInstance().flushDelayedPackets(MC.getConnection()) : 0;
   }

   public int clearPacketQueue() {
      AutismSessionState shared = AutismSessionState.getInstance();
      return shared.cancelAndClearPacketQueues();
   }

   public void notifyDelayToggle(boolean enabled, int flushed) {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (enabled) {
         autismclient.util.bi.init("Delay Packets on", -13248397);
      } else if (flushed > 0) {
         if (shared.isSendingQueuedPackets()) {
            shared.setStaggeredCompletionMessage("Sent " + flushed + " packet" + (flushed == 1 ? "" : "s") + ".");
            autismclient.util.bi.init("Delay Packets off - sending " + flushed, -42149);
         } else {
            autismclient.util.bi.init("Delay Packets off - sent " + flushed, -42149);
         }
      } else if (!this.isFlushQueueOnDelayDisable() && (!shared.getDelayedPackets().isEmpty() || !shared.getSendingPackets().isEmpty())) {
         autismclient.util.bi.init("Delay Packets off - queue kept", -42149);
      } else {
         autismclient.util.bi.init("Delay Packets off", -42149);
      }
   }

   public void notifyQueueFlushed(int count) {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (count > 0) {
         if (shared.isSendingQueuedPackets()) {
            shared.setStaggeredCompletionMessage("Sent " + count + " packet" + (count == 1 ? "" : "s") + ".");
            autismclient.util.bi.init("Sending " + count + " packet" + (count == 1 ? "" : "s"), -14249);
         } else {
            autismclient.util.bi.init("Sent " + count + " packet" + (count == 1 ? "" : "s"), -13248397);
         }
      } else {
         autismclient.util.bi.init("Queue empty", -42149);
      }
   }

   public void notifyQueueCleared(int count) {
      autismclient.util.bi.init(count > 0 ? "Cleared " + count + " packet" + (count == 1 ? "" : "s") : "Queue empty", count > 0 ? -14249 : -42149);
   }

   public boolean togglePacketLoggerOverlay() {
      AutismPacketLoggerOverlay overlay = this.getOrCreatePacketLoggerOverlay();
      if (overlay == null) {
         return false;
      }

      OverlayManager.getInstance().register(overlay);
      overlay.close_V_12();
      return true;
   }

   public AutismOverlay getOrCreateProfilesOverlay() {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      if (this.profilesOverlay == null && MC != null && MC.font != null) {
         this.profilesOverlay = new AutismProfilesOverlay(MC.font);
         this.profilesOverlay.loadBounds();
      }

      return this.profilesOverlay;
   }

   public AutismOverlay getProfilesOverlay() {
      return this.profilesOverlay;
   }

   public void hideProfilesOverlayIfUnretained() {
      if (!OverlayScreenController.hasRetainedForeground() && !AutismLiteVariant.enabled()) {
         try {
            if (this.profilesOverlay != null && this.profilesOverlay.isVisible()) {
               this.profilesOverlay.setVisible(false);
            }
         } catch (Throwable var2) {
         }
      }
   }

   public boolean toggleProfilesOverlay() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (MC == null) {
         return false;
      }

      AutismOverlay overlay = this.getOrCreateProfilesOverlay();
      if (overlay == null) {
         return false;
      }

      if (overlay.isVisible()) {
         overlay.setVisible(false);
         return true;
      }

      autismclient.gui.screens.AutismOverlaysScreen host = new autismclient.gui.screens.AutismOverlaysScreen(overlay, null, false, true);
      OverlayScreenController.setScreenExplicit(MC, host);
      if (MC.gui.screen() != host) {
         return false;
      }

      ((AutismProfilesOverlay)overlay).update_V_19();
      OverlayManager manager = OverlayManager.getInstance();
      manager.register(overlay);
      manager.bringToFront(overlay);
      manager.markClosing(overlay);
      return true;
   }

   public boolean restoreStoredGuiAsync() {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (MC == null) {
         return false;
      } else if (shared.getStoredGuiScreen() != null && shared.getStoredContainerMenu() != null) {
         MC.execute(() -> {
            MC.gui.setScreen(shared.getStoredGuiScreen());
            AbstractContainerMenu handler = shared.getStoredContainerMenu();
            if (MC.player != null) {
               MC.player.containerMenu = handler;
            }
         });
         return true;
      } else {
         return false;
      }
   }

   public Set<Class<? extends Packet<?>>> getCustomC2sPacketsCopy() {
      return new LinkedHashSet<>(AutismSessionState.getInstance().getCustomC2sPackets());
   }

   public Set<Class<? extends Packet<?>>> getCustomS2cPacketsCopy() {
      return new LinkedHashSet<>(AutismSessionState.getInstance().getCustomS2cPackets());
   }

   public void setCustomC2sPackets(Set<Class<? extends Packet<?>>> packets) {
      Set<Class<? extends Packet<?>>> safe = packets == null ? this.getDefaultC2sPackets() : new LinkedHashSet<>(packets);
      AutismSessionState.getInstance().setCustomC2sPackets(safe);
      this.config.c2sPackets = this.serializePacketClassNames(safe);
      this.saveConfig();
   }

   public void setCustomS2cPackets(Set<Class<? extends Packet<?>>> packets) {
      Set<Class<? extends Packet<?>>> safe = packets == null ? this.getDefaultS2cPackets() : new LinkedHashSet<>(packets);
      AutismSessionState.getInstance().setCustomS2cPackets(safe);
      this.config.s2cPackets = this.serializePacketClassNames(safe);
      this.saveConfig();
   }

   public void resetCustomC2sPackets() {
      this.setCustomC2sPackets(this.getDefaultC2sPackets());
   }

   public void resetCustomS2cPackets() {
      this.setCustomS2cPackets(this.getDefaultS2cPackets());
   }

   public Set<Class<? extends Packet<?>>> getDefaultC2sPackets() {
      Set<Class<? extends Packet<?>>> defaults = new LinkedHashSet<>();

      for (Class<? extends Packet<?>> packetClass : PacketTypeRegistry.getC2sPackets()) {
         if (!IGNORED_DEFAULT_C2S_PACKETS.contains(packetClass)) {
            defaults.add(packetClass);
         }
      }

      defaults.add(ServerboundChatPacket.class);
      defaults.add(ServerboundChatCommandPacket.class);
      defaults.add(ServerboundChatCommandSignedPacket.class);
      defaults.add(ServerboundSignUpdatePacket.class);
      return defaults;
   }

   public Set<Class<? extends Packet<?>>> getDefaultS2cPackets() {
      return new LinkedHashSet<>();
   }

   private void syncConfigToSessionState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.resetTransientSessionFlags();
      shared.setUseCustomPackets(this.config.useCustomPackets);
      shared.setCustomC2sPackets(this.resolvePacketClasses(this.config.c2sPackets, true));
      shared.setCustomS2cPackets(this.resolvePacketClasses(this.config.s2cPackets, false));
      shared.setAllowSignEditing(this.config.allowSignEditing);
      shared.setAutoDenyResourcePack(this.config.autoDenyResourcePack);
      shared.setPretendPackAccepted(this.config.pretendPackAccepted);
      shared.setStaggeredPacketSend(this.config.staggeredPacketSend);
      shared.setStaggeredSendDelay(this.config.staggeredSendDelay);
   }

   private void resetTransientSessionFlags() {
      if (this.config != null) {
         this.config.applyRuntimeDefaults();
      }

      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setSendPacketsEnabled(true);
      shared.setDelayPacketsEnabled(false);
      shared.setStaggeredPacketSend(false);
      shared.setTimedCaptureMode(false);
   }

   private void refreshXCarryActiveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (!shared.isXCarryForced() && (shared.isXCarryActive() || this.isXCarryEnabled())) {
         if (aD.init_Z()) {
            shared.setXCarryActive(false);
         } else {
            boolean active = false;
            if (this.isXCarryEnabled() && MC.player != null && MC.player.inventoryMenu != null) {
               active = autismclient.util.macro.XCarryAction.hasCarriedItems(MC.player.inventoryMenu, true);
            }

            shared.setXCarryActive(active);
         }
      }
   }

   private void updateWorldSpawnState() {
      if (!this.joinedPlayConnection && this.isInLiveWorld()) {
         if (++this.watchdogMissingJoinTicks >= 20) {
            this.joinedPlayConnection = true;
            this.spawnedInWorld = true;
            this.watchdogMissingJoinTicks = 0;
            this.logWatchdogRecoveryThrottled();
            return;
         }
      } else {
         this.watchdogMissingJoinTicks = 0;
      }

      if (!this.joinedPlayConnection) {
         this.spawnedInWorld = false;
         this.autoProbePending = false;
      } else {
         boolean wasSpawned = this.spawnedInWorld;
         this.spawnedInWorld = this.isInLiveWorld();
         if (!wasSpawned && this.spawnedInWorld && !aD.init_Z()) {
            autismclient.util.JoinMacroController.read_V();
            this.autoProbePending = true;
            this.autoProbeStartedMs = System.currentTimeMillis();
         }

         this.tickAutoProbePlugins();
      }
   }

   private void tickAutoProbePlugins() {
      if ((AutismLiteVariant.enabled() || autismclient.e.FileCache.init()) && this.autoProbePending) {
         long waited = System.currentTimeMillis() - this.autoProbeStartedMs;
         if (waited > 25000L) {
            this.autoProbePending = false;
         } else if (aD.init_Z() || this.config == null || !this.config.autoProbePlugins) {
            this.autoProbePending = false;
         } else if (this.spawnedInWorld) {
            AutismServerInfoOverlay overlay = this.getOrCreateServerInfoOverlay();
            if (overlay != null && overlay.push_Z_12() && (overlay.pop_Z_7() || waited >= 2000L) && overlay.tick_Z_6()) {
               this.autoProbePending = false;
            }
         }
      }
   }

   private boolean isInLiveWorld() {
      return MC != null && MC.getConnection() != null && MC.player != null && MC.level != null;
   }

   private void saveConfig() {
      if (this.config != null) {
         this.config.save();
      }
   }

   private Set<Class<? extends Packet<?>>> resolvePacketClasses(List<String> names, boolean c2s) {
      Set<Class<? extends Packet<?>>> resolved = new LinkedHashSet<>();
      if (names != null) {
         for (String name : names) {
            if (name != null && !name.isBlank()) {
               Class<? extends Packet<?>> packetClass = PacketTypeRegistry.resolvePacketClass(name);
               if (packetClass == null) {
                  try {
                     Class<?> direct = Class.forName(name);
                     if (Packet.class.isAssignableFrom(direct)) {
                        Class<? extends Packet<?>> typed = (Class<? extends Packet<?>>)direct;
                        packetClass = typed;
                     }
                  } catch (ClassNotFoundException var9) {
                  }
               }

               if (packetClass != null) {
                  resolved.add(packetClass);
               }
            }
         }
      }

      if (resolved.isEmpty()) {
         return c2s ? this.getDefaultC2sPackets() : this.getDefaultS2cPackets();
      } else {
         return resolved;
      }
   }

   private List<String> serializePacketClassNames(Set<Class<? extends Packet<?>>> packets) {
      List<String> names = new ArrayList<>();

      for (Class<? extends Packet<?>> packetClass : packets) {
         String name = PacketTypeRegistry.getPacketName(packetClass);
         names.add(name != null ? name : packetClass.getName());
      }

      return names;
   }

   private void pollKeybinds() {
      AutismConfig cfg = this.config;
      if (cfg != null) {
         AutismSessionState shared = AutismSessionState.getInstance();
         this.refreshKeyboundMacros();
         if (cfg.keybindModuleMenu != -1) {
            boolean pressed = this.isBindPressed(cfg.keybindModuleMenu);
            if (pressed && !this.wasModuleMenuPressed && MC.gui.screen() == null && !AutismLiteVariant.enabled()) {
               MC.gui.setScreen(new autismclient.gui.screens.AutismModulesScreen(null));
            }

            this.wasModuleMenuPressed = pressed;
         }

         if (aD.init_Z()) {
            this.wasLoadGuiPressed = false;
            this.wasFlushQueuePressed = false;
            this.wasClearQueuePressed = false;
            this.wasToggleLoggerPressed = false;
            this.wasToggleSendPressed = false;
            this.wasToggleDelayPressed = false;
            this.macroKeyPressStates.clear();
         } else {
            if (cfg.keybindLoadGui != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindLoadGui);
               if (pressed && !this.wasLoadGuiPressed) {
                  if (this.restoreStoredGuiAsync()) {
                     autismclient.util.bi.init("GUI restored.", -13248397);
                  } else {
                     autismclient.util.bi.write("No stored GUI.");
                  }
               }

               this.wasLoadGuiPressed = pressed;
            }

            if (cfg.keybindFlushQueue != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindFlushQueue);
               if (pressed && !this.wasFlushQueuePressed) {
                  int count = this.flushPacketQueue();
                  this.notifyQueueFlushed(count);
               }

               this.wasFlushQueuePressed = pressed;
            }

            if (cfg.keybindClearQueue != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindClearQueue);
               if (pressed && !this.wasClearQueuePressed) {
                  int count = this.clearPacketQueue();
                  this.notifyQueueCleared(count);
               }

               this.wasClearQueuePressed = pressed;
            }

            if (cfg.keybindToggleLogger != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindToggleLogger);
               if (pressed && !this.wasToggleLoggerPressed) {
                  this.togglePacketLoggerOverlay();
               }

               this.wasToggleLoggerPressed = pressed;
            }

            if (cfg.keybindToggleSend != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindToggleSend);
               if (pressed && !this.wasToggleSendPressed) {
                  boolean newValue = !shared.isSendPacketsEnabled();
                  this.setAndSaveSendGuiPackets(newValue);
                  autismclient.util.bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
               }

               this.wasToggleSendPressed = pressed;
            }

            if (cfg.keybindToggleDelay != -1) {
               boolean pressed = this.isBindPressed(cfg.keybindToggleDelay);
               if (pressed && !this.wasToggleDelayPressed) {
                  boolean newValue = !shared.isDelayPacketsEnabled();
                  int sent = this.toggleDelayPackets(newValue);
                  this.notifyDelayToggle(newValue, sent);
               }

               this.wasToggleDelayPressed = pressed;
            }

            for (autismclient.util.AutismMacro macro : this.keyboundMacros) {
               boolean pressed = this.isBindPressed(macro.keyCode);
               boolean wasPressed = this.macroKeyPressStates.getOrDefault(macro.name, false);
               if (pressed && !wasPressed) {
                  if (MacroExecutor.isMacroRunning(macro)) {
                     MacroExecutor.stopMacro(macro);
                  } else {
                     macro.execute();
                  }
               }

               this.macroKeyPressStates.put(macro.name, pressed);
            }
         }
      }
   }

   private void refreshKeyboundMacros() {
      autismclient.util.MacroLibraryManager macroManager = autismclient.util.MacroLibraryManager.getInstance();
      long revision = macroManager.getRevision();
      if (revision != this.cachedMacroRevision) {
         List<autismclient.util.AutismMacro> keyboundMacros = new ArrayList<>();
         Set<String> activeMacroNames = new HashSet<>();

         for (autismclient.util.AutismMacro macro : macroManager.getMacros()) {
            if (macro != null && macro.keyCode != -1) {
               keyboundMacros.add(macro);
               activeMacroNames.add(macro.name);
            }
         }

         this.macroKeyPressStates.keySet().removeIf(name -> !activeMacroNames.contains(name));
         this.keyboundMacros = keyboundMacros;
         this.cachedMacroRevision = revision;
      }
   }

   private boolean hasFocusedOverlayTextInput() {
      return OverlayManager.getInstance().hasFocusedTextInput();
   }

   private boolean isBindPressed(int bindCode) {
      return autismclient.util.ValueSink.init(MC, bindCode);
   }

   private void restoreStoredGuiImmediate() {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (MC != null) {
         if (shared.getStoredGuiScreen() != null && shared.getStoredContainerMenu() != null) {
            MC.gui.setScreen(shared.getStoredGuiScreen());
            AbstractContainerMenu handler = shared.getStoredContainerMenu();
            if (MC.player != null) {
               MC.player.containerMenu = handler;
            }

            autismclient.util.bi.init("GUI restored.", -13248397);
         } else {
            autismclient.util.bi.write("No stored GUI.");
         }
      }
   }

   public record Inner_DataUtil(boolean normalPath, boolean passivePayloadCapture, boolean loggerCapture, boolean serverInfoObservation) {
      private static final ModuleRuntimeManager.Inner_DataUtil NONE = new ModuleRuntimeManager.Inner_DataUtil(false, false, false, false);

      public static ModuleRuntimeManager.Inner_DataUtil none() {
         return NONE;
      }
   }

   private record Inner_StreamUtil(long timestampMs, int tick, String direction, Class<?> packetClass, AutismPayloadSupport.PayloadEntry payload) {
   }
}
