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
import autismclient.util.bB;
import autismclient.util.bD;
import autismclient.util.bF;
import autismclient.util.bI;
import autismclient.util.bK;
import autismclient.util.bQ;
import autismclient.util.bV;
import autismclient.util.bk;
import autismclient.util.bw;
import autismclient.util.cX;
import autismclient.util.cf;
import autismclient.util.ck;
import autismclient.util.ct;
import autismclient.util.cx;
import autismclient.util.dm;
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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final ModuleRuntimeManager init_PoolEntry = new ModuleRuntimeManager();
   private static final long init_J = 20000L;
   private static final int init_I = 96;
   private static final int load_I = 256;
   private static final boolean init_Z = Boolean.getBoolean("autism.payload.trace");
   private static final Set<Class<?>> init_Set = Set.of(
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
   private AutismConfig init_AutismConfig;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private boolean close_Z;
   private boolean open;
   private final Map<String, Boolean> init_Map = new HashMap<>();
   private List<autismclient.util.AutismMacro> init_List = List.of();
   private long load_J = -1L;
   private int read_I;
   private int write_I;
   private AutismPacketLoggerOverlay init_AutismPacketLogger;
   private bB init_bB;
   private volatile boolean poll;
   private volatile boolean push;
   private AutismServerInfoOverlay init_AutismServerInfoOv;
   private cX load_cX;
   private final Deque<ModuleRuntimeManager.Inner_StreamUtil> init_Deque = new ArrayDeque<>(96);
   private final Set<Packet<?>> load_Set = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
   private final Deque<String> load_Deque = new ArrayDeque<>(256);
   private final Set<String> read_Set = new HashSet<>();
   private volatile boolean pop;
   private volatile boolean tick;
   private long read_J;
   private int update_I;
   private long write_J;
   private static final int sync_I = 20;
   private volatile long update_J;
   private volatile boolean run;
   private volatile long sync_J;
   private static final long flush_J = 2000L;
   private static final long close_J = 25000L;
   private Module init_Module;
   private int flush_I = -1;

   private ModuleRuntimeManager() {
   }

   public static ModuleRuntimeManager init_PoolEntry() {
      return init_PoolEntry;
   }

   public void init_V() {
      if (!this.load_Z) {
         this.init_AutismConfig = AutismConfig.load();
         this.init("applyRuntimeDefaults", () -> this.init_AutismConfig.applyRuntimeDefaults());
         AutismConfig.setGlobal(this.init_AutismConfig);
         if (!AutismLiteVariant.enabled()) {
            this.init("themeWarm", () -> ct.init_ct_StreamUtil());
         }

         this.init("moduleRegistry", () -> al.init_V_AutismConfig(this.init_AutismConfig));
         this.init("c2sPackets", () -> {
            if (this.init_AutismConfig.c2sPackets.isEmpty()) {
               this.init_AutismConfig.c2sPackets = this.init_List_Set(this.write_Set());
            }
         });
         this.init("sharedState", this::get_V);
         this.init("packHide", () -> {
            if (aD.init_Z()) {
               aD.read_V();
            }
         });
         this.init("lanSync", () -> {
            if (this.init_AutismConfig.lanSyncEnabled && !aD.init_Z()) {
               autismclient.util.aE.init_aE().init_V();
            }
         });
         if (!AutismLiteVariant.enabled()) {
            this.init("profiles", () -> bV.init_bV());
         }

         this.load_Z = true;
         autismclient.util.bg.load(this);
      }
   }

   private void init(String where, Runnable action) {
      try {
         action.run();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Autism] init step '{}' failed; isolated so the mod still activates", where, t);
      }
   }

   public void init_V_AutismConfig(AutismConfig next) {
      if (next != null) {
         next.applyRuntimeDefaults();
         AutismConfig prev = this.init_AutismConfig != null ? this.init_AutismConfig : AutismConfig.getGlobal();
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
         boolean inWorld = init_Minecraft != null && init_Minecraft.level != null;
         this.init_AutismConfig = next;
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
         this.get_V();
         this.init_V_Z(filtersChanged);
         autismclient.util.bg.load(this);
         autismclient.util.al.init_V();
         if (themeChanged) {
            ct.init_V();
         }

         boolean panicNow = aD.init_Z();
         if (panicNow && !panicBefore) {
            aD.read_V();
         }

         if (!panicNow) {
            if (next.lanSyncEnabled && !autismclient.util.aE.init_aE().init_Z()) {
               autismclient.util.aE.init_aE().init_V();
            } else if (!next.lanSyncEnabled && autismclient.util.aE.init_aE().init_Z()) {
               autismclient.util.aE.init_aE().load_V();
            }
         }
      }
   }

   public void load_V() {
      if (this.load_Z && init_Minecraft != null) {
         try {
            this.add_V();
         } catch (Throwable t) {
            this.init("worldSpawnState", t);
         }

         try {
            this.pop_V();
         } catch (Throwable t) {
            this.init("tickWork", t);
         }
      }
   }

   private void init(String where, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - this.read_J >= 5000L) {
         this.read_J = now;
         AutismClientAddon.LOG.warn("[Autism] tick '{}' failed; isolated to protect the client", where, t);
      }
   }

   private void push_V() {
      long now = System.currentTimeMillis();
      if (now - this.write_J >= 5000L) {
         this.write_J = now;
         AutismClientAddon.LOG
            .warn(
               "[Autism] active-state watchdog: a live world was present but joinedPlayConnection was false; force-restored isActive() (HUD/overlay would otherwise stay hidden until reconnect)"
            );
      }
   }

   private void pop_V() {
      if (AutismLiteVariant.enabled() || autismclient.e.CharEncoder.init()) {
         autismclient.util.bg.init_V_PoolEntry(this);
         if (!AutismLiteVariant.enabled()) {
            bV.init_bV().load();
         }

         autismclient.util.macro.PingSpoofScheduler.update();
         BlinkPacketManager.update();
         autismclient.util.m.init(init_Minecraft);
         boolean hidden = aD.init_Z();
         if (hidden) {
            this.run = false;
            this.update_J = 0L;
            bI.init_V();
            bF.load();
            AutismServerInfoOverlay hiddenServerInfo = this.load_AutismServerInfoOv();
            if (hiddenServerInfo != null) {
               hiddenServerInfo.poll_V_9();
            }
         }

         bK.read_V();
         bF.init();
         if (!hidden) {
            bD.init(init_Minecraft, this.open_Z());
         }

         autismclient.util.aE lanSync = autismclient.util.aE.init_aE();
         if (!hidden && lanSync.read_Z()) {
            long perf = bK.read_J();
            lanSync.poll();
            bK.load("join.lanSync.tick", perf);
         }

         if (!hidden && autismclient.util.macro.MacroWaitRegistry.init_Z()) {
            long perf = bK.read_J();
            autismclient.util.macro.MacroWaitRegistry.load(init_Minecraft);
            bK.load("join.macroConditions.tick", perf);
         }

         ck shared = ck.init_ck();
         NoFallPauseTimer.read();
         if (!hidden && shared.push_Z()) {
            shared.read_V();
         }

         if (!hidden && cf.init_Z_J(1L)) {
            long perf = bK.read_J();
            al.sync_V();
            bK.init("join.modules.tick", perf, 8000000L);
         }

         if (!hidden) {
            AutoReconnectController.load_V();
         }

         this.put_V();
         AutismServerInfoOverlay serverInfo = this.load_AutismServerInfoOv();
         if (!hidden && serverInfo != null && (serverInfo.sync_Z() || serverInfo.run_Z_5())) {
            serverInfo.close_V_15();
         }

         if (shared.write_Z() && init_Minecraft.getConnection() != null) {
            if (this.read_I++ >= 1) {
               this.read_I = 0;
            }
         } else {
            this.read_I = 0;
         }

         if (autismclient.util.at.init()) {
            this.start_V();
         } else {
            this.read_Z = false;
            this.write_Z = false;
            this.update_Z = false;
            this.sync_Z = false;
            this.flush_Z = false;
            this.close_Z = false;
            this.init_Map.clear();
         }

         this.write_I++;
         AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
         if (logger != null) {
            logger.init_V_I_8(this.write_I);
         }

         if (!hidden && AddonEvents.hasTickListeners()) {
            AddonEvents.fireTick(init_Minecraft);
         }

         this.init(hidden, shared, serverInfo);
      }
   }

   private void init(boolean hidden, ck shared, AutismServerInfoOverlay serverInfo) {
      boolean active = !hidden
         && (
            MacroExecutor.load_Z()
               || shared.write_Z()
               || shared.open_Z()
               || shared.run_Z()
               || shared.chunk()
               || shared.cache_Z()
               || shared.level()
               || shared.count()
               || shared.pool()
               || shared.frame()
               || bI.init_Z()
               || am.add()
               || AntiVanishModule.init_Z_19()
               || autismclient.util.bi.init_Z()
               || serverInfo != null && serverInfo.run_Z_5()
         );
      cf.init_V_J_Z(2048L, active);
   }

   public void read_V() {
      dm.init_V();
      bK.load_V();
      long perf = bK.read_J();
      this.pop = true;
      this.tick = false;
      if (aD.init_Z()) {
         this.run = false;
         this.update_J = 0L;
      } else {
         this.run = this.init_AutismConfig != null && this.init_AutismConfig.autoProbePlugins;
         if (this.run) {
            this.sync_J = System.currentTimeMillis();
         }

         this.tick_V();
         bD.init_V();
      }

      this.set_V();
      if (aD.init_Z()) {
         aD.read_V();
      }

      if (!aD.init_Z() && this.init_AutismConfig != null && this.init_AutismConfig.packetLoggerCapturing) {
         this.init_AutismPacketLogger();
      }

      if (this.init_AutismConfig.lanSyncEnabled && !aD.init_Z() && !autismclient.util.aE.init_aE().init_Z()) {
         autismclient.util.aE.init_aE().init_V();
      }

      if (!aD.init_Z()) {
         autismclient.util.aE.init_aE().tick();
      }

      if (!aD.init_Z()) {
         al.push_V();
      }

      if (!aD.init_Z()) {
         AddonEvents.fireGameJoin();
      }

      bK.init("join.onGameJoin", perf, 8000000L);
   }

   public void write_V() {
      dm.init_V();
      ar.load_V();
      autismclient.util.oresim.OreSimEngine.init_V();
      autismclient.util.oresim.OreSimEngine.write_V();
      this.pop = false;
      this.tick = false;
      this.run = false;
      bQ.init();
      bF.load();
      bD.load();
      AutismServerInfoOverlay leftOverlay = this.load_AutismServerInfoOv();
      if (leftOverlay != null) {
         leftOverlay.flush_V_17();
      }

      this.update_J = 0L;
      autismclient.util.bg.load(this);
      ck.init_ck().close_V();
      this.read_Z = false;
      this.write_Z = false;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.close_Z = false;
      this.open = false;
      if (!aD.init_Z()) {
         al.pop_V();
      }

      if (!aD.init_Z()) {
         AddonEvents.fireGameLeft();
      }
   }

   public boolean init_Z() {
      return this.load_Z && this.tick && this.level();
   }

   public boolean load_Z() {
      return this.load_Z && !aD.init_Z();
   }

   public boolean read_Z() {
      if (this.init_Z() && !aD.init_Z()) {
         ck shared = ck.init_ck();
         return shared.pop_Z()
            || this.open_Z()
            || this.pool()
            || al.load_Z()
            || AddonEvents.hasPacketListeners()
            || autismclient.util.macro.PacketGateManager.init_Z()
            || MacroExecutor.update_Z();
      } else {
         return false;
      }
   }

   public ModuleRuntimeManager.Inner_DataUtil init_PoolEntry_DataUtil_Z(boolean playConnection) {
      if (aD.init_Z()) {
         return ModuleRuntimeManager.Inner_DataUtil.init_PoolEntry_DataUtil();
      }

      long captureState = autismclient.util.bg.init_J();
      boolean loggerCapture = autismclient.util.bg.init_Z_J(captureState);
      boolean passivePayloadCapture = autismclient.util.bg.load(captureState);
      if (!this.init_Z()) {
         return !loggerCapture && !passivePayloadCapture
            ? ModuleRuntimeManager.Inner_DataUtil.init_PoolEntry_DataUtil()
            : new ModuleRuntimeManager.Inner_DataUtil(false, passivePayloadCapture, loggerCapture, false);
      }

      ck shared = ck.init_ck();
      boolean serverInfoObservation = passivePayloadCapture && this.pool();
      boolean normalPath = playConnection
         && (
            shared.pop_Z()
               || loggerCapture
               || serverInfoObservation
               || al.load_Z()
               || AddonEvents.hasPacketListeners()
               || autismclient.util.macro.PacketGateManager.init_Z()
               || MacroExecutor.update_Z()
         );
      return !normalPath && !passivePayloadCapture && !loggerCapture && !serverInfoObservation
         ? ModuleRuntimeManager.Inner_DataUtil.init_PoolEntry_DataUtil()
         : new ModuleRuntimeManager.Inner_DataUtil(normalPath, passivePayloadCapture, loggerCapture, serverInfoObservation);
   }

   public boolean write_Z() {
      return !aD.init_Z() && (this.close_Z() || this.count() || bI.init_Z());
   }

   public boolean update_Z() {
      return aD.init_Z() ? false : this.open_Z();
   }

   public boolean sync_Z() {
      return aD.init_Z() ? false : this.open_Z() || this.write_Z() || this.pool() || bI.init_Z();
   }

   private boolean pool() {
      if (this.init_AutismConfig != null && this.init_AutismConfig.autoProbePlugins && this.run) {
         return true;
      }

      AutismServerInfoOverlay overlay = this.load_AutismServerInfoOv();
      return overlay != null && overlay.put_Z_4();
   }

   public boolean flush_Z() {
      return this.init_Z() && !aD.init_Z() ? this.pool() : false;
   }

   private AutismServerInfoOverlay read_AutismServerInfoOv() {
      AutismServerInfoOverlay overlay = this.load_AutismServerInfoOv();
      if (overlay != null) {
         return overlay;
      } else {
         return this.init_AutismConfig != null && this.init_AutismConfig.autoProbePlugins && this.run ? this.init_AutismServerInfoOv() : null;
      }
   }

   public void init_V_Packet(Packet<?> packet) {
      if (this.flush_Z() && packet != null) {
         AutismServerInfoOverlay overlay = this.read_AutismServerInfoOv();
         if (overlay != null) {
            overlay.init_V_Packet(packet);
            overlay.load(packet);
         }
      }
   }

   public void load_V(Packet<?> packet) {
      if (this.flush_Z() && packet != null) {
         AutismServerInfoOverlay overlay = this.read_AutismServerInfoOv();
         if (overlay != null) {
            if (packet instanceof ClientboundCommandSuggestionsPacket suggestions) {
               overlay.init_V_I_ClientboundCommand(suggestions.id(), suggestions);
            }

            if (packet instanceof ClientboundOpenScreenPacket) {
               overlay.read(packet);
            }

            if (packet instanceof ClientboundCommandsPacket
               && this.tick
               && !this.run
               && this.init_AutismConfig != null
               && this.init_AutismConfig.autoProbePlugins) {
               this.run = true;
               this.sync_J = System.currentTimeMillis();
            }
         }
      }
   }

   private boolean index() {
      return this.close_Z() || this.pool() || this.open_Z() || bI.init_Z();
   }

   public void update_V() {
      this.init_V_Z(false);
   }

   public void init_V_Z(boolean refreshSubscriptions) {
      this.poll = false;
      if (this.init_bB != null) {
         this.init_bB.init_V();
      }

      if (refreshSubscriptions) {
         bD.init_V();
      }

      autismclient.util.bg.load(this);
   }

   private boolean count() {
      if (this.poll) {
         return this.push;
      }

      boolean enabled = this.flags();
      this.push = enabled;
      this.poll = true;
      return enabled;
   }

   private boolean flags() {
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

   public void sync_V() {
      this.tick_V();
      bD.init_V();
   }

   public boolean close_Z() {
      return System.currentTimeMillis() <= this.update_J;
   }

   public long init_J() {
      return this.update_J;
   }

   public boolean open_Z() {
      if (aD.init_Z()) {
         return false;
      }

      AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
      return logger != null ? !logger.push_Z_10() : this.init_AutismConfig != null && this.init_AutismConfig.packetLoggerCapturing;
   }

   public boolean init_Z(Packet<?> packet, String direction, String protocolPhase) {
      return this.load(packet, direction, protocolPhase, "connection");
   }

   public boolean init(Packet<?> packet, String direction, String protocolPhase, String source) {
      return this.load(packet, direction, protocolPhase, source != null && !source.isBlank() ? source : "codec");
   }

   public boolean init(byte[] frameBytes, String direction, String protocolPhase, String source) {
      if (!aD.init_Z() && frameBytes != null && frameBytes.length != 0) {
         AutismPayloadSupport.Inner_StreamUtil snapshot = AutismPayloadSupport.init(frameBytes, direction, protocolPhase);
         if (snapshot == null) {
            return false;
         }

         String fingerprint = this.init_Str_AutismPayloadSuppo(snapshot);
         if (this.init_Z_Str(fingerprint)) {
            return true;
         }

         if (this.index()) {
            this.init_V_AutismPayloadSuppo(snapshot);
         }

         boolean loggerCapture = this.open_Z();
         boolean passiveWork = this.write_Z();
         if (!loggerCapture && !passiveWork) {
            return false;
         }

         AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
         boolean captured = false;
         if (loggerCapture) {
            if (logger == null) {
               this.init(snapshot, direction, init_Class_Str(direction));
            } else {
               logger.init_V_J(System.currentTimeMillis(), this.write_I, direction == null ? "" : direction, init_Class_Str(direction), snapshot);
            }

            captured = true;
         } else if (passiveWork) {
            bB.Inner_StreamUtil filterMatch = this.init(snapshot.load_Str(), direction);
            if (filterMatch != null) {
               if (logger != null) {
                  logger.init_V_J(System.currentTimeMillis(), this.write_I, direction == null ? "" : direction, init_Class_Str(direction), snapshot);
               } else {
                  bF.init(snapshot.load_Str(), direction, filterMatch);
                  this.init(snapshot, direction, init_Class_Str(direction));
               }

               captured = true;
            }
         }

         if (captured) {
            this.load(fingerprint);
            this.init(snapshot, source);
         }

         return captured;
      } else {
         return false;
      }
   }

   private boolean load(Packet<?> packet, String direction, String protocolPhase, String source) {
      if (!aD.init_Z() && packet != null) {
         if (packet instanceof BundlePacket<?> bundle) {
            boolean captured = false;

            for (Packet<?> child : init_List_BundlePacket(bundle)) {
               if (this.load(child, direction, protocolPhase, source)) {
                  captured = true;
               }
            }

            return captured;
         } else {
            CustomPacketPayload payload = AutismPayloadSupport.init(packet);
            if (payload == null) {
               return false;
            }

            if (this.read_Z(packet)) {
               return true;
            }

            String channel = AutismPayloadSupport.init_Str(payload);
            AutismPayloadSupport.init_V(packet, protocolPhase);
            if (this.index()) {
               this.read(packet, direction);
            }

            boolean loggerCapture = this.open_Z();
            boolean passiveWork = this.write_Z();
            if (!loggerCapture && !passiveWork) {
               return false;
            }

            AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
            boolean captured = false;
            if (loggerCapture) {
               if (logger == null) {
                  this.load(packet, direction);
               } else {
                  logger.load_V_Packet_Str(packet, direction);
               }

               captured = true;
            } else if (passiveWork) {
               bB.Inner_StreamUtil filterMatch = this.init(channel, direction);
               if (filterMatch != null) {
                  if (logger != null) {
                     logger.load_V_Packet_Str(packet, direction);
                  } else {
                     bF.init(channel, direction, filterMatch);
                     this.load(packet, direction);
                  }

                  captured = true;
               }
            }

            if (captured) {
               this.read_V(packet);
               this.init(packet, channel, direction, protocolPhase, source);
            }

            return captured;
         }
      } else {
         return false;
      }
   }

   private boolean read_Z(Packet<?> packet) {
      synchronized (this.load_Set) {
         return this.load_Set.contains(packet);
      }
   }

   private void read_V(Packet<?> packet) {
      if (packet != null) {
         synchronized (this.load_Set) {
            this.load_Set.add(packet);
         }
      }
   }

   private void init(Packet<?> packet, String channel, String direction, String protocolPhase, String source) {
      if (init_Z) {
         int size = -1;

         try {
            CustomPacketPayload payload = AutismPayloadSupport.init(packet);
            size = AutismPayloadSupport.load(payload).length;
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

   private void init(AutismPayloadSupport.Inner_StreamUtil snapshot, String source) {
      if (init_Z && snapshot != null) {
         AutismClientAddon.LOG
            .info(
               "[Autism Payload] {} {} {} {}B via {}",
               snapshot.init_Str() == null ? "" : snapshot.init_Str(),
               snapshot.update() == null ? "" : snapshot.update(),
               snapshot.load_Str() != null && !snapshot.load_Str().isBlank() ? snapshot.load_Str() : "<unknown>",
               snapshot.init_I(),
               source == null ? "" : source
            );
      }
   }

   public void init(Packet<?> packet, String direction) {
      this.init_V(packet, direction, "");
   }

   public void init_V(Packet<?> packet, String direction, String protocolPhase) {
      if (!aD.init_Z() && this.write_Z()) {
         this.load(packet, direction, protocolPhase, "passive");
      }
   }

   private void load(Packet<?> packet, String direction) {
      long perf = bK.read_J();
      AutismPayloadSupport.Inner_StreamUtil snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(packet, direction);
      if (snapshot != null) {
         this.init(snapshot, direction, packet.getClass());
         bK.load("join.passivePayload.snapshot", perf);
      }
   }

   private void init(AutismPayloadSupport.Inner_StreamUtil snapshot, String direction, Class<?> packetClass) {
      if (snapshot != null) {
         synchronized (this.init_Deque) {
            while (this.init_Deque.size() >= 96) {
               this.init_Deque.removeFirst();
            }

            this.init_Deque
               .addLast(new ModuleRuntimeManager.Inner_StreamUtil(System.currentTimeMillis(), this.write_I, direction == null ? "" : direction, packetClass, snapshot));
         }
      }
   }

   private bB.Inner_StreamUtil init(String channel, String direction) {
      if (channel != null && !channel.isBlank()) {
         bB listeners = this.init_bB();
         return listeners.init_Z() ? listeners.init_bB_StreamUtil_Str_Str(channel, direction) : null;
      } else {
         return null;
      }
   }

   private bB init_bB() {
      if (this.init_bB == null) {
         this.init_bB = new bB();
      }

      return this.init_bB;
   }

   private static List<Packet<?>> init_List_BundlePacket(BundlePacket<?> bundle) {
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

   private static Class<?> init_Class_Str(String direction) {
      return direction != null && direction.equalsIgnoreCase("C2S") ? ServerboundCustomPayloadPacket.class : ClientboundCustomPayloadPacket.class;
   }

   private String init_Str_AutismPayloadSuppo(AutismPayloadSupport.Inner_StreamUtil snapshot) {
      return snapshot == null
         ? ""
         : (snapshot.init_Str() == null ? "" : snapshot.init_Str())
            + "|"
            + (snapshot.update() == null ? "" : snapshot.update())
            + "|"
            + snapshot.load_I()
            + "|"
            + (snapshot.load_Str() == null ? "" : snapshot.load_Str())
            + "|"
            + snapshot.init_I()
            + "|"
            + Arrays.hashCode(snapshot.init_BArr());
   }

   private boolean init_Z_Str(String fingerprint) {
      if (fingerprint != null && !fingerprint.isBlank()) {
         synchronized (this.read_Set) {
            return this.read_Set.contains(fingerprint);
         }
      } else {
         return false;
      }
   }

   private void load(String fingerprint) {
      if (fingerprint != null && !fingerprint.isBlank()) {
         synchronized (this.read_Set) {
            if (this.read_Set.add(fingerprint)) {
               this.load_Deque.addLast(fingerprint);

               while (this.load_Deque.size() > 256) {
                  String removed = this.load_Deque.removeFirst();
                  this.read_Set.remove(removed);
               }
            }
         }
      }
   }

   public void flush_V() {
      AutismClientMessaging.sendPrefixed("Autism is always enabled in standalone mode.");
   }

   private void tick_V() {
      if (!aD.init_Z()) {
         this.update_J = Math.max(this.update_J, System.currentTimeMillis() + 20000L);
         autismclient.util.bg.load(this);
      }
   }

   public void init(ItemStack stack, List<?> lines) {
      al.init_V_ItemStack_List(stack, lines);
   }

   public boolean init_Z_Packet(Packet<?> packet) {
      return this.init(packet, false);
   }

   public boolean init(Packet<?> packet, boolean payloadLoggedEarly) {
      long perf = bK.read_J();

      try {
         if (aD.init_Z()) {
            return false;
         }

         this.init_V_Packet(packet);
         if (this.index()) {
            this.read(packet, "C2S");
         }

         AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
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

   public boolean load_Z(Packet<?> packet) {
      return this.load(packet, false);
   }

   public boolean load(Packet<?> packet, boolean payloadLoggedEarly) {
      long perf = bK.read_J();

      try {
         if (aD.init_Z()) {
            return false;
         }

         this.load_V(packet);
         if (this.index()) {
            this.read(packet, "S2C");
         }

         AutismPacketLoggerOverlay logger = this.load_AutismPacketLogger();
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

   private void read(Packet<?> packet, String direction) {
      if (packet != null && AutismPacketLoggerOverlay.init_Z_Packet(packet)) {
         CustomPacketPayload payload = AutismPayloadSupport.init(packet);
         if (payload != null) {
            String channel = AutismPayloadSupport.init_Str(payload);
            boolean inbound = "S2C".equalsIgnoreCase(direction);
            if (inbound) {
               bD.load_V(channel);
            }

            try {
               AutismPayloadSupport.Inner_StreamUtil snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(packet, direction);
               bI.init_Z(snapshot);
               if (inbound) {
                  bD.init(snapshot);
               }
            } catch (Throwable var10) {
            }

            if (inbound && bQ.init_Z(channel)) {
               try {
                  AutismPayloadSupport.Inner_StreamUtil snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(packet, direction);
                  if (snapshot == null) {
                     return;
                  }

                  boolean changed = bQ.init(this.read_Str(), this.load_Str(), snapshot);
                  if (changed) {
                     AutismServerInfoOverlay overlay = this.load_AutismServerInfoOv();
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

   private void init_V_AutismPayloadSuppo(AutismPayloadSupport.Inner_StreamUtil snapshot) {
      if (snapshot != null) {
         String channel = snapshot.load_Str();
         bI.init_Z(snapshot);
         boolean inbound = "S2C".equalsIgnoreCase(snapshot.init_Str());
         if (inbound) {
            bD.load_V(channel);
            bD.init(snapshot);
         }

         if (inbound && bQ.init_Z(channel)) {
            try {
               boolean changed = bQ.init(this.read_Str(), this.load_Str(), snapshot);
               if (changed) {
                  AutismServerInfoOverlay overlay = this.load_AutismServerInfoOv();
                  if (overlay != null) {
                     overlay.put_V_8();
                  }
               }
            } catch (Throwable var6) {
            }
         }
      }
   }

   private String load_Str() {
      if (init_Minecraft != null && init_Minecraft.getConnection() != null) {
         String brand = init_Minecraft.getConnection().serverBrand();
         return brand == null ? "" : brand;
      } else {
         return "";
      }
   }

   private String read_Str() {
      if (init_Minecraft == null) {
         return "";
      }

      ServerData entry = init_Minecraft.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim().toLowerCase(Locale.ROOT);
      }

      if (init_Minecraft.getConnection() != null && init_Minecraft.getConnection().getConnection() != null) {
         SocketAddress address = init_Minecraft.getConnection().getConnection().getRemoteAddress();
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

   public AutismPacketLoggerOverlay init_AutismPacketLogger() {
      if (this.init_AutismPacketLogger == null && init_Minecraft != null && init_Minecraft.font != null) {
         this.init_AutismPacketLogger = new AutismPacketLoggerOverlay(init_Minecraft.font);
         this.init_AutismPacketLogger.flush_V_13();
         this.init_V_AutismPacketLogger(this.init_AutismPacketLogger);
      }

      return this.init_AutismPacketLogger;
   }

   public AutismPacketLoggerOverlay load_AutismPacketLogger() {
      return this.init_AutismPacketLogger;
   }

   private void init_V_AutismPacketLogger(AutismPacketLoggerOverlay logger) {
      if (logger != null) {
         List<ModuleRuntimeManager.Inner_StreamUtil> captures;
         synchronized (this.init_Deque) {
            if (this.init_Deque.isEmpty()) {
               return;
            }

            captures = new ArrayList<>(this.init_Deque);
            this.init_Deque.clear();
         }

         for (ModuleRuntimeManager.Inner_StreamUtil capture : captures) {
            logger.init_V_J(capture.init_J(), capture.init_I(), capture.init_Str(), capture.init_Class(), capture.init_AutismPayloadSuppo());
         }
      }
   }

   public AutismServerInfoOverlay init_AutismServerInfoOv() {
      if (this.init_AutismServerInfoOv == null && init_Minecraft != null && init_Minecraft.font != null) {
         this.init_AutismServerInfoOv = new AutismServerInfoOverlay(init_Minecraft.font);
         this.init_AutismServerInfoOv.update_V_21();
      }

      return this.init_AutismServerInfoOv;
   }

   public AutismServerInfoOverlay load_AutismServerInfoOv() {
      return this.init_AutismServerInfoOv;
   }

   public boolean poll_Z() {
      return this.init_AutismConfig.noPauseOnLostFocus;
   }

   public boolean push_Z() {
      return this.init_AutismConfig.lanSyncEnabled;
   }

   public boolean pop_Z() {
      return this.init_AutismConfig != null ? this.init_AutismConfig.pretendPackAccepted : AutismConfig.getGlobal().pretendPackAccepted;
   }

   public boolean tick_Z() {
      return this.init_AutismConfig != null ? this.init_AutismConfig.spoofClientVanilla : AutismConfig.getGlobal().spoofClientVanilla;
   }

   public boolean run_Z() {
      Module module = al.init_Module_Str("inv-move");
      return module != null ? module.isEnabled() : this.init_AutismConfig != null && this.init_AutismConfig.inventoryMove;
   }

   public void load(boolean value) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.inventoryMove = value;
         Module module = al.init_Module_Str("inv-move");
         if (module != null && module.isEnabled() != value) {
            module.setEnabledSilently(value);
         }

         this.reset_V();
      }
   }

   public boolean get_Z() {
      Module module = this.init_Module();
      return module != null ? module.isEnabled() : this.init_AutismConfig != null && this.init_AutismConfig.xCarry;
   }

   public void read(boolean value) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.xCarry = value;
         Module module = al.init_Module_Str("xcarry");
         if (module != null && module.isEnabled() != value) {
            module.setEnabledSilently(value);
         }

         this.reset_V();
      }
   }

   public void write(boolean value) {
      this.init_AutismConfig.pretendPackAccepted = value;
      ck.init_ck().push(value);
      this.reset_V();
   }

   public void update(boolean value) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.spoofClientVanilla = value;
         this.reset_V();
      }
   }

   public boolean set_Z() {
      return this.init_AutismConfig != null ? this.init_AutismConfig.autoDenyResourcePack : AutismConfig.getGlobal().autoDenyResourcePack;
   }

   public void sync(boolean value) {
      this.init_AutismConfig.autoDenyResourcePack = value;
      ck.init_ck().pop(value);
      this.reset_V();
   }

   public boolean put_Z() {
      return this.init_AutismConfig.useMsSleepMode;
   }

   public int init_I() {
      return this.init_AutismConfig.msSleepInterval;
   }

   public boolean add_Z() {
      return this.init_AutismConfig.instantExecutionMode;
   }

   public int load_I() {
      return this.init_AutismConfig.actionDelayUs;
   }

   public boolean clear_Z() {
      return this.init_AutismConfig.packetBurstMode;
   }

   public boolean reset_Z() {
      return this.init_AutismConfig.useDirectFlush;
   }

   public boolean start_Z() {
      return this.init_AutismConfig.forceChannelFlush;
   }

   public boolean stop_Z() {
      return this.init_AutismConfig != null && this.init_AutismConfig.flushQueueOnDelayDisable;
   }

   public void flush(boolean value) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.flushQueueOnDelayDisable = value;
         this.reset_V();
      }
   }

   public boolean next_Z() {
      return this.init_AutismConfig != null && this.init_AutismConfig.captureAsExact;
   }

   public void close(boolean value) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.captureAsExact = value;
         this.reset_V();
      }
   }

   public boolean prev() {
      return this.init_AutismConfig.useCustomPackets;
   }

   public void open(boolean value) {
      this.init_AutismConfig.useCustomPackets = value;
      ck.init_ck().tick(value);
      this.reset_V();
   }

   public void poll(boolean value) {
      this.init_AutismConfig.sendGuiPackets = value;
      ck.init_ck().read_V_Z(value);
   }

   public boolean init_Z_Z(boolean value) {
      this.poll(value);
      this.reset_V();
      return value;
   }

   public void push(boolean value) {
      this.init_AutismConfig.delayGuiPackets = value;
      ck.init_ck().write(value);
   }

   public String init_Str() {
      return this.init_AutismConfig == null ? "" : (this.init_AutismConfig.commandPrefix == null ? "" : this.init_AutismConfig.commandPrefix);
   }

   public void init_V_Str(String prefix) {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.commandPrefix = autismclient.util.z.init_Str(prefix);
         this.reset_V();
      }
   }

   public Map<Integer, String> init_Map() {
      if (this.init_AutismConfig == null) {
         return Collections.emptyMap();
      }

      if (this.init_AutismConfig.commandBinds == null) {
         this.init_AutismConfig.commandBinds = new LinkedHashMap<>();
      }

      return this.init_AutismConfig.commandBinds;
   }

   public boolean valid() {
      return this.init_AutismConfig != null && this.init_AutismConfig.commandBinds != null && !this.init_AutismConfig.commandBinds.isEmpty();
   }

   public void init(int key, String command) {
      if (this.init_AutismConfig != null) {
         if (this.init_AutismConfig.commandBinds == null) {
            this.init_AutismConfig.commandBinds = new LinkedHashMap<>();
         }

         if (command != null && !command.isBlank()) {
            this.init_AutismConfig.commandBinds.put(key, command);
         } else {
            this.init_AutismConfig.commandBinds.remove(key);
         }

         this.reset_V();
      }
   }

   public void init_V_I(int key) {
      if (this.init_AutismConfig != null && this.init_AutismConfig.commandBinds != null) {
         this.init_AutismConfig.commandBinds.remove(key);
         this.reset_V();
      }
   }

   private Module init_Module() {
      int revision = al.init_I();
      if (revision != this.flush_I) {
         this.init_Module = al.init_Module_Str("xcarry");
         this.flush_I = revision;
      }

      return this.init_Module;
   }

   public boolean size() {
      Module m = this.init_Module();
      if (m == null) {
         return true;
      }

      String v = m.value("use-crafting");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public boolean data() {
      Module m = this.init_Module();
      if (m == null) {
         return true;
      }

      String v = m.value("use-armor");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public boolean value() {
      Module m = this.init_Module();
      if (m == null) {
         return true;
      }

      String v = m.value("use-offhand");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public void pop(boolean v) {
      Module m = this.init_Module();
      if (m != null) {
         m.setValue("use-crafting", Boolean.toString(v));
      }
   }

   public void tick(boolean v) {
      Module m = this.init_Module();
      if (m != null) {
         m.setValue("use-armor", Boolean.toString(v));
      }
   }

   public void run(boolean v) {
      Module m = this.init_Module();
      if (m != null) {
         m.setValue("use-offhand", Boolean.toString(v));
      }
   }

   public boolean cache() {
      Module m = this.init_Module();
      if (m == null) {
         return true;
      }

      String v = m.value("carry-cursor");
      return v == null || v.isBlank() || Boolean.parseBoolean(v);
   }

   public void get(boolean v) {
      Module m = this.init_Module();
      if (m != null) {
         m.setValue("carry-cursor", Boolean.toString(v));
      }
   }

   public Set<Integer> init_Set() {
      LinkedHashSet<Integer> s = new LinkedHashSet<>();
      if (this.size()) {
         s.add(1);
         s.add(2);
         s.add(3);
         s.add(4);
         s.add(0);
      }

      if (this.data()) {
         s.add(5);
         s.add(6);
         s.add(7);
         s.add(8);
      }

      if (this.value()) {
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

   public int init_I_Z(boolean value) {
      this.push(value);
      this.reset_V();
      return !value && this.stop_Z() && init_Minecraft != null && init_Minecraft.getConnection() != null
         ? ck.init_ck().init_I_ClientPacketListen(init_Minecraft.getConnection())
         : 0;
   }

   public int read_I() {
      return init_Minecraft != null && init_Minecraft.getConnection() != null ? ck.init_ck().init_I_ClientPacketListen(init_Minecraft.getConnection()) : 0;
   }

   public int write_I() {
      ck shared = ck.init_ck();
      return shared.write_I();
   }

   public void init(boolean enabled, int flushed) {
      ck shared = ck.init_ck();
      if (enabled) {
         autismclient.util.bi.init("Delay Packets on", -13248397);
      } else if (flushed > 0) {
         if (shared.tick_Z()) {
            shared.write_V("Sent " + flushed + " packet" + (flushed == 1 ? "" : "s") + ".");
            autismclient.util.bi.init("Delay Packets off - sending " + flushed, -42149);
         } else {
            autismclient.util.bi.init("Delay Packets off - sent " + flushed, -42149);
         }
      } else if (!this.stop_Z() && (!shared.load_List().isEmpty() || !shared.read_List().isEmpty())) {
         autismclient.util.bi.init("Delay Packets off - queue kept", -42149);
      } else {
         autismclient.util.bi.init("Delay Packets off", -42149);
      }
   }

   public void load(int count) {
      ck shared = ck.init_ck();
      if (count > 0) {
         if (shared.tick_Z()) {
            shared.write_V("Sent " + count + " packet" + (count == 1 ? "" : "s") + ".");
            autismclient.util.bi.init("Sending " + count + " packet" + (count == 1 ? "" : "s"), -14249);
         } else {
            autismclient.util.bi.init("Sent " + count + " packet" + (count == 1 ? "" : "s"), -13248397);
         }
      } else {
         autismclient.util.bi.init("Queue empty", -42149);
      }
   }

   public void read(int count) {
      autismclient.util.bi.init(count > 0 ? "Cleared " + count + " packet" + (count == 1 ? "" : "s") : "Queue empty", count > 0 ? -14249 : -42149);
   }

   public boolean state() {
      AutismPacketLoggerOverlay overlay = this.init_AutismPacketLogger();
      if (overlay == null) {
         return false;
      }

      bk.init_bk().init_V(overlay);
      overlay.close_V_12();
      return true;
   }

   public cX load_cX() {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      if (this.load_cX == null && init_Minecraft != null && init_Minecraft.font != null) {
         this.load_cX = new AutismProfilesOverlay(init_Minecraft.font);
         this.load_cX.b__V();
      }

      return this.load_cX;
   }

   public cX update_cX() {
      return this.load_cX;
   }

   public void close_V() {
      if (!cx.load_Z() && !AutismLiteVariant.enabled()) {
         try {
            if (this.load_cX != null && this.load_cX.sync_Z()) {
               this.load_cX.init_V_Z(false);
            }
         } catch (Throwable var2) {
         }
      }
   }

   public boolean node() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (init_Minecraft == null) {
         return false;
      }

      cX overlay = this.load_cX();
      if (overlay == null) {
         return false;
      }

      if (overlay.sync_Z()) {
         overlay.init_V_Z(false);
         return true;
      }

      autismclient.gui.d.AutismOverlaysScreen host = new autismclient.gui.d.AutismOverlaysScreen(overlay, null, false, true);
      cx.init_V(init_Minecraft, host);
      if (init_Minecraft.gui.screen() != host) {
         return false;
      }

      ((AutismProfilesOverlay)overlay).update_V_19();
      bk manager = bk.init_bk();
      manager.init_V(overlay);
      manager.update_V(overlay);
      manager.write_V(overlay);
      return true;
   }

   public boolean task() {
      ck shared = ck.init_ck();
      if (init_Minecraft == null) {
         return false;
      } else if (shared.init_Screen() != null && shared.init_AbstractContainerM() != null) {
         init_Minecraft.execute(() -> {
            init_Minecraft.gui.setScreen(shared.init_Screen());
            AbstractContainerMenu handler = shared.init_AbstractContainerM();
            if (init_Minecraft.player != null) {
               init_Minecraft.player.containerMenu = handler;
            }
         });
         return true;
      } else {
         return false;
      }
   }

   public Set<Class<? extends Packet<?>>> load_Set() {
      return new LinkedHashSet<>(ck.init_ck().load_Set());
   }

   public Set<Class<? extends Packet<?>>> read_Set() {
      return new LinkedHashSet<>(ck.init_ck().read_Set());
   }

   public void init_V_Set(Set<Class<? extends Packet<?>>> packets) {
      Set<Class<? extends Packet<?>>> safe = packets == null ? this.write_Set() : new LinkedHashSet<>(packets);
      ck.init_ck().init_V_Set(safe);
      this.init_AutismConfig.c2sPackets = this.init_List_Set(safe);
      this.reset_V();
   }

   public void load(Set<Class<? extends Packet<?>>> packets) {
      Set<Class<? extends Packet<?>>> safe = packets == null ? this.update_Set() : new LinkedHashSet<>(packets);
      ck.init_ck().load_V_Set(safe);
      this.init_AutismConfig.s2cPackets = this.init_List_Set(safe);
      this.reset_V();
   }

   public void open_V() {
      this.init_V_Set(this.write_Set());
   }

   public void poll_V() {
      this.load(this.update_Set());
   }

   public Set<Class<? extends Packet<?>>> write_Set() {
      Set<Class<? extends Packet<?>>> defaults = new LinkedHashSet<>();

      for (Class<? extends Packet<?>> packetClass : bw.load()) {
         if (!init_Set.contains(packetClass)) {
            defaults.add(packetClass);
         }
      }

      defaults.add(ServerboundChatPacket.class);
      defaults.add(ServerboundChatCommandPacket.class);
      defaults.add(ServerboundChatCommandSignedPacket.class);
      defaults.add(ServerboundSignUpdatePacket.class);
      return defaults;
   }

   public Set<Class<? extends Packet<?>>> update_Set() {
      return new LinkedHashSet<>();
   }

   private void get_V() {
      ck shared = ck.init_ck();
      this.set_V();
      shared.tick(this.init_AutismConfig.useCustomPackets);
      shared.init_V_Set(this.init(this.init_AutismConfig.c2sPackets, true));
      shared.load_V_Set(this.init(this.init_AutismConfig.s2cPackets, false));
      shared.open(this.init_AutismConfig.allowSignEditing);
      shared.pop(this.init_AutismConfig.autoDenyResourcePack);
      shared.push(this.init_AutismConfig.pretendPackAccepted);
      shared.run(this.init_AutismConfig.staggeredPacketSend);
      shared.read_V_I(this.init_AutismConfig.staggeredSendDelay);
   }

   private void set_V() {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.applyRuntimeDefaults();
      }

      ck shared = ck.init_ck();
      shared.read_V_Z(true);
      shared.write(false);
      shared.run(false);
      shared.cache(false);
   }

   private void put_V() {
      ck shared = ck.init_ck();
      if (!shared.sync_Z() && (shared.update_Z() || this.get_Z())) {
         if (aD.init_Z()) {
            shared.update(false);
         } else {
            boolean active = false;
            if (this.get_Z() && init_Minecraft.player != null && init_Minecraft.player.inventoryMenu != null) {
               active = autismclient.util.macro.XCarryAction.init_Z_InventoryMenu_Z(init_Minecraft.player.inventoryMenu, true);
            }

            shared.update(active);
         }
      }
   }

   private void add_V() {
      if (!this.pop && this.level()) {
         if (++this.update_I >= 20) {
            this.pop = true;
            this.tick = true;
            this.update_I = 0;
            this.push_V();
            return;
         }
      } else {
         this.update_I = 0;
      }

      if (!this.pop) {
         this.tick = false;
         this.run = false;
      } else {
         boolean wasSpawned = this.tick;
         this.tick = this.level();
         if (!wasSpawned && this.tick && !aD.init_Z()) {
            autismclient.util.aA.read_V();
            this.run = true;
            this.sync_J = System.currentTimeMillis();
         }

         this.clear_V();
      }
   }

   private void clear_V() {
      if ((AutismLiteVariant.enabled() || autismclient.e.FileCache.init()) && this.run) {
         long waited = System.currentTimeMillis() - this.sync_J;
         if (waited > 25000L) {
            this.run = false;
         } else if (aD.init_Z() || this.init_AutismConfig == null || !this.init_AutismConfig.autoProbePlugins) {
            this.run = false;
         } else if (this.tick) {
            AutismServerInfoOverlay overlay = this.init_AutismServerInfoOv();
            if (overlay != null && overlay.push_Z_12() && (overlay.pop_Z_7() || waited >= 2000L) && overlay.tick_Z_6()) {
               this.run = false;
            }
         }
      }
   }

   private boolean level() {
      return init_Minecraft != null && init_Minecraft.getConnection() != null && init_Minecraft.player != null && init_Minecraft.level != null;
   }

   private void reset_V() {
      if (this.init_AutismConfig != null) {
         this.init_AutismConfig.save();
      }
   }

   private Set<Class<? extends Packet<?>>> init(List<String> names, boolean c2s) {
      Set<Class<? extends Packet<?>>> resolved = new LinkedHashSet<>();
      if (names != null) {
         for (String name : names) {
            if (name != null && !name.isBlank()) {
               Class<? extends Packet<?>> packetClass = bw.init_Class_Str(name);
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
         return c2s ? this.write_Set() : this.update_Set();
      } else {
         return resolved;
      }
   }

   private List<String> init_List_Set(Set<Class<? extends Packet<?>>> packets) {
      List<String> names = new ArrayList<>();

      for (Class<? extends Packet<?>> packetClass : packets) {
         String name = bw.init_Str_Class(packetClass);
         names.add(name != null ? name : packetClass.getName());
      }

      return names;
   }

   private void start_V() {
      AutismConfig cfg = this.init_AutismConfig;
      if (cfg != null) {
         ck shared = ck.init_ck();
         this.stop_V();
         if (cfg.keybindModuleMenu != -1) {
            boolean pressed = this.init_Z_I(cfg.keybindModuleMenu);
            if (pressed && !this.open && init_Minecraft.gui.screen() == null && !AutismLiteVariant.enabled()) {
               init_Minecraft.gui.setScreen(new autismclient.gui.d.AutismModulesScreen(null));
            }

            this.open = pressed;
         }

         if (aD.init_Z()) {
            this.read_Z = false;
            this.write_Z = false;
            this.update_Z = false;
            this.sync_Z = false;
            this.flush_Z = false;
            this.close_Z = false;
            this.init_Map.clear();
         } else {
            if (cfg.keybindLoadGui != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindLoadGui);
               if (pressed && !this.read_Z) {
                  if (this.task()) {
                     autismclient.util.bi.init("GUI restored.", -13248397);
                  } else {
                     autismclient.util.bi.write("No stored GUI.");
                  }
               }

               this.read_Z = pressed;
            }

            if (cfg.keybindFlushQueue != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindFlushQueue);
               if (pressed && !this.write_Z) {
                  int count = this.read_I();
                  this.load(count);
               }

               this.write_Z = pressed;
            }

            if (cfg.keybindClearQueue != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindClearQueue);
               if (pressed && !this.update_Z) {
                  int count = this.write_I();
                  this.read(count);
               }

               this.update_Z = pressed;
            }

            if (cfg.keybindToggleLogger != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindToggleLogger);
               if (pressed && !this.sync_Z) {
                  this.state();
               }

               this.sync_Z = pressed;
            }

            if (cfg.keybindToggleSend != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindToggleSend);
               if (pressed && !this.flush_Z) {
                  boolean newValue = !shared.read_Z();
                  this.init_Z_Z(newValue);
                  autismclient.util.bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
               }

               this.flush_Z = pressed;
            }

            if (cfg.keybindToggleDelay != -1) {
               boolean pressed = this.init_Z_I(cfg.keybindToggleDelay);
               if (pressed && !this.close_Z) {
                  boolean newValue = !shared.write_Z();
                  int sent = this.init_I_Z(newValue);
                  this.init(newValue, sent);
               }

               this.close_Z = pressed;
            }

            for (autismclient.util.AutismMacro macro : this.init_List) {
               boolean pressed = this.init_Z_I(macro.load_I);
               boolean wasPressed = this.init_Map.getOrDefault(macro.init_Str, false);
               if (pressed && !wasPressed) {
                  if (MacroExecutor.init_Z_aM(macro)) {
                     MacroExecutor.load_V_aM(macro);
                  } else {
                     macro.init_V();
                  }
               }

               this.init_Map.put(macro.init_Str, pressed);
            }
         }
      }
   }

   private void stop_V() {
      autismclient.util.aN macroManager = autismclient.util.aN.init_aN();
      long revision = macroManager.init_J();
      if (revision != this.load_J) {
         List<autismclient.util.AutismMacro> keyboundMacros = new ArrayList<>();
         Set<String> activeMacroNames = new HashSet<>();

         for (autismclient.util.AutismMacro macro : macroManager.init_List()) {
            if (macro != null && macro.load_I != -1) {
               keyboundMacros.add(macro);
               activeMacroNames.add(macro.init_Str);
            }
         }

         this.init_Map.keySet().removeIf(name -> !activeMacroNames.contains(name));
         this.init_List = keyboundMacros;
         this.load_J = revision;
      }
   }

   private boolean frame() {
      return bk.init_bk().update_Z();
   }

   private boolean init_Z_I(int bindCode) {
      return autismclient.util.ValueSink.init(init_Minecraft, bindCode);
   }

   private void next_V() {
      ck shared = ck.init_ck();
      if (init_Minecraft != null) {
         if (shared.init_Screen() != null && shared.init_AbstractContainerM() != null) {
            init_Minecraft.gui.setScreen(shared.init_Screen());
            AbstractContainerMenu handler = shared.init_AbstractContainerM();
            if (init_Minecraft.player != null) {
               init_Minecraft.player.containerMenu = handler;
            }

            autismclient.util.bi.init("GUI restored.", -13248397);
         } else {
            autismclient.util.bi.write("No stored GUI.");
         }
      }
   }

   public record Inner_DataUtil(boolean init_Z, boolean load, boolean read, boolean write) {
      private static final ModuleRuntimeManager.Inner_DataUtil init_PoolEntry_DataUtil = new ModuleRuntimeManager.Inner_DataUtil(false, false, false, false);

      public static ModuleRuntimeManager.Inner_DataUtil init_PoolEntry_DataUtil() {
         return init_PoolEntry_DataUtil;
      }
   }

   private record Inner_StreamUtil(long init_J, int init_I, String init_Str, Class<?> init_Class, AutismPayloadSupport.Inner_StreamUtil init_AutismPayloadSuppo) {
   }
}
