package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.state.BlockState;

public class AutismPacketLoggerOverlay extends DraggableOverlayWindow {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final DateTimeFormatter init_DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
   private static final int flush_I = 14;
   private static final int close_I = 10;
   private static final int open_I = 2;
   private static final int poll_I = 2;
   private static final int push_I = 360;
   private static final int pop_I = 250;
   private static final int tick = 800;
   private static final int run = 400;
   private static final int get = 200;
   private static final int set = 400;
   private static final String init_Str_2 = "AutismPacketLoggerOverlay";
   private static final long init_J = 500L;
   private static final int put = 2;
   private static final int add = -4809052;
   private static final int clear = -6114880;
   private static final Set<String> init_Set = load_Set();
   private static final Set<String> load_Set = init_Set_2();
   private static final Set<String> read_Set = new HashSet<>(
      Arrays.asList(
         "ClickSlot",
         "CloseAbstractContainerScreen",
         "OpenScreen",
         "AbstractContainerMenu",
         "CreativeInventoryAction",
         "PickFromInventory",
         "PlayerAction",
         "InventoryS2C",
         "AbstractContainerMenuSlotUpdate",
         "AbstractContainerMenuProperty",
         "SetTradeOffers",
         "OpenHorseScreen",
         "CraftRequest",
         "ButtonClick",
         "RecipeBookData",
         "UpdateSelectedSlot",
         "HandSwing",
         "PlayerInteractBlock",
         "PlayerInteractItem",
         "PlayerInteractEntity",
         "ItemPickupAnimation",
         "ContainerClick",
         "ContainerClose",
         "ContainerSetContent",
         "ContainerSetData",
         "ContainerSetSlot",
         "ContainerButtonClick",
         "SetCreativeModeSlot",
         "SetCarriedItem",
         "Swing",
         "UseItem",
         "UseItemOn",
         "Interact",
         "SetCursorItem",
         "SetPlayerInventory",
         "TakeItemEntity",
         "MerchantOffers"
      )
   );
   private static final Set<String> write_Set = new HashSet<>(
      Arrays.asList(
         "PlayerMove",
         "PlayerMoveFull",
         "PlayerMovePositionAndOnGround",
         "PlayerMoveLookAndOnGround",
         "PlayerMoveOnGroundOnly",
         "EntityPosition",
         "EntityPositionSync",
         "EntitySetHead",
         "EntityVelocityUpdate",
         "VehicleMove",
         "MoveRelative",
         "PacketMoveRelative",
         "RotateRelative",
         "PacketRotateRelative",
         "EntityPacketRotate",
         "EntityMoveRelative",
         "EntityRotate",
         "TeleportConfirm",
         "ClientTickEnd",
         "ServerboundMovePlayer",
         "ServerboundMoveVehicle",
         "ServerboundPlayerInput",
         "ServerboundAcceptTeleportation",
         "ClientboundMoveEntity",
         "ClientboundMoveVehicle",
         "ClientboundMoveMinecart",
         "ClientboundPlayerPosition",
         "ClientboundPlayerRotation",
         "ClientboundEntityPositionSync",
         "ClientboundSetEntityMotion",
         "ClientboundRotateHead",
         "ClientboundTeleportEntity"
      )
   );
   private static final Set<String> update_Set = new HashSet<>(
      Arrays.asList(
         "CustomPacketPayload",
         "CustomPacketPayloadC2S",
         "CustomPacketPayloadS2C",
         "BrandPayload",
         "PluginMessage",
         "CustomPayload",
         "S2CPayload",
         "DiscardedPayload"
      )
   );
   private final Font init_Font;
   private final AutismPacketInspectOverlay init_AutismPacketInspec;
   private final AutismPacketLoggerOverlay.BlockedPacketListOverlay init_AutismPacketLogger;
   private final AutismPacketLoggerOverlay.PayloadChannelListenerOverlay init_AutismPacketLogger_3;
   private final PayloadChannelFilterManager init_bB;
   private final PayloadRegistrationManager init_bC;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final OverlayContextMenu<AutismPacketLoggerOverlay.Inner_JsonUtil> init_F_2;
   private int reset = 360;
   private int start = 250;
   private int stop = 250;
   private volatile boolean write_Z = true;
   private boolean update_Z = false;
   private boolean sync_Z;
   private double init_D;
   private double load_D;
   private boolean flush_Z;
   private int next;
   private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private boolean close_Z;
   private int prev;
   private String load_Str = "";
   private final ThemedTextInputBox init_k;
   private AutismPacketLoggerOverlay.Inner_DataUtil init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.ALL;
   private boolean open_Z = true;
   private int valid = 0;
   private boolean poll_Z;
   private final Set<String> sync_Set = new LinkedHashSet<>();
   private final Set<String> flush_Set = new HashSet<>();
   private final Map<Class<?>, Boolean> init_Map = new HashMap<>();
   private boolean push_Z;
   private final Deque<AutismPacketLoggerOverlay.Inner_JsonUtil> init_Deque = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.Inner_JsonUtil> load_Deque = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.Inner_JsonUtil> read_Deque = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.Inner_JsonUtil> write_Deque = new ArrayDeque<>();
   private final List<AutismPacketLoggerOverlay.Inner_JsonUtil> init_List = new ArrayList<>();
   private final PacketStateDeltaTracker init_bq = new PacketStateDeltaTracker();
   private volatile int size;
   private long load_J;
   private List<AutismPacketLoggerOverlay.Inner_CachePool> load_List = new ArrayList<>();
   private volatile boolean pop_Z = true;
   private final Set<String> close_Set = new HashSet<>();
   private static long read_J = 0L;

   public AutismPacketLoggerOverlay(Font textRenderer) {
      super("AutismPacketLoggerOverlay", 360, 250);
      this.init_Font = textRenderer;
      this.reset = this.get_I_15();
      this.start = this.set_I_15();
      this.stop = this.start;
      this.init_I_2 = 200;
      this.load_I = 40;
      this.init_k = new ThemedTextInputBox(init_Minecraft, textRenderer, 0, 0, this.next_I_5(), this.reset_I_5(), false);
      this.init_k.init_V_Component(Component.literal("Search..."));
      this.init_k.load(160);
      this.init_k.init_V_Consumer(value -> {
         this.load_Str = value == null ? "" : value;
         this.pop_Z = true;
      });
      this.init_AutismPacketInspec = new AutismPacketInspectOverlay(textRenderer);
      this.init_AutismPacketLogger = new AutismPacketLoggerOverlay.BlockedPacketListOverlay();
      this.init_bB = new PayloadChannelFilterManager();
      this.init_bC = new PayloadRegistrationManager();
      this.init_AutismPacketLogger_3 = new AutismPacketLoggerOverlay.PayloadChannelListenerOverlay();
      this.init_F_2 = new OverlayContextMenu<>(this.init_CharEncoder, textRenderer, this::init_StrArr_AutismPacketLogger, this.put_I_6());
      this.init_V_F(this.init_F_2);
      OverlayManager.init_bk().init_V(this.init_AutismPacketInspec);
      OverlayManager.init_bk().init_V(this.init_AutismPacketLogger);
      OverlayManager.init_bk().init_V(this.init_AutismPacketLogger_3);
      this.poll_V_7();
      this.write_Z = !AutismConfig.getGlobal().packetLoggerCapturing;
   }

   @Override
   protected boolean d_() {
      return true;
   }

   public void write_V_18() {
      this.a__V();
   }

   public synchronized String load_Str_9() {
      return "capturing="
         + !this.write_Z
         + " all="
         + this.init_Deque.size()
         + " inv="
         + this.load_Deque.size()
         + " move="
         + this.read_Deque.size()
         + " payload="
         + this.write_Deque.size()
         + " pending="
         + this.init_List.size();
   }

   public synchronized List<AutismPacketLoggerOverlay.Inner_JsonUtil> init_List_Str_3(String category, int offset, int limit) {
      String key = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
      Deque var10002;
      switch (key) {
         case "INVENTORY":
         case "INV":
            var10002 = this.load_Deque;
            break;
         case "MOVEMENT":
         case "MOVE":
            var10002 = this.read_Deque;
            break;
         case "PAYLOAD":
            var10002 = this.write_Deque;
            break;
         default:
            var10002 = this.init_Deque;
      }

      List<AutismPacketLoggerOverlay.Inner_JsonUtil> newestFirst = new ArrayList<>(var10002);
      Collections.reverse(newestFirst);
      int from = Math.max(0, Math.min(offset, newestFirst.size()));
      int to = limit < 0 ? newestFirst.size() : Math.min(newestFirst.size(), from + limit);
      return new ArrayList<>(newestFirst.subList(from, to));
   }

   public synchronized void init_V_Str_5(String searchText, String direction, String category) {
      if (searchText != null) {
         this.init_k.init_V_Str(searchText);
         this.load_Str = this.init_k.load_Str();
      }

      if (direction != null) {
         switch (direction.trim().toUpperCase(Locale.ROOT)) {
            case "C2S":
               this.valid = 1;
               break;
            case "S2C":
               this.valid = 2;
               break;
            case "BOTH":
            default:
               this.valid = 0;
         }
      }

      if (category != null) {
         switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "INVENTORY":
            case "INV":
               this.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.INVENTORY;
               break;
            case "MOVEMENT":
            case "MOVE":
               this.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.MOVEMENT;
               break;
            case "PAYLOAD":
               this.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD;
               break;
            case "ALL":
            default:
               this.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.ALL;
         }
      }

      this.pop_Z = true;
   }

   public synchronized void update_V_16() {
      this.init_k.init_V_Str("");
      this.load_Str = "";
      this.valid = 0;
      this.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_DataUtil.ALL;
      this.poll_Z = false;
      this.pop_Z = true;
   }

   public synchronized JsonObject init_JsonObject_2() {
      JsonObject state = new JsonObject();
      state.addProperty("paused", this.write_Z);
      state.addProperty("configurationOnly", this.update_Z);
      JsonObject buffers = new JsonObject();
      buffers.addProperty("all", this.init_Deque.size());
      buffers.addProperty("inventory", this.load_Deque.size());
      buffers.addProperty("movement", this.read_Deque.size());
      buffers.addProperty("payload", this.write_Deque.size());
      buffers.addProperty("pending", this.init_List.size());
      state.add("buffers", buffers);
      JsonObject filters = new JsonObject();
      filters.addProperty("search", this.load_Str);
      filters.addProperty("direction", this.valid == 1 ? "C2S" : (this.valid == 2 ? "S2C" : "Both"));
      filters.addProperty("category", this.init_AutismPacketLogger_2.name());
      filters.addProperty("payloadFilteredOnly", this.poll_Z);
      state.add("filters", filters);
      state.addProperty("blockedCount", this.sync_Set.size());
      return state;
   }

   public synchronized void init_V_Str_9(String name) {
      this.read(name);
   }

   public synchronized void sync_V_14() {
      this.cache();
   }

   public void flush_V_13() {
      this.b__V();
      this.init_bB.init_V();
      this.init_bC.init_V();
      this.pop_Z = true;
   }

   public static boolean poll_Z_13() {
      OverlayBounds layout = AutismSessionState.init_ck().init_cH_Str("AutismPacketLoggerOverlay");
      return layout != null && layout.init_Z;
   }

   @Override
   public int init_I() {
      return this.get_I_15();
   }

   @Override
   public int load_I() {
      return 14 + this.clear_I_6() + this.stop_I_5() + 12 + this.put_I_6() * 2;
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected int a__I() {
      return this.load_I() + this.put_I_6();
   }

   @Override
   protected void c_() {
      if (!this.set_Z()) {
         this.reset = this.get_I_15();
         this.start = this.set_I_15();
      }
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.init_I_2, this.load_I, this.reset, this.stop, this.init_Z, this.load_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.init_cH_cX_cH(this, bounds);
         this.init_I_2 = clamped.init_I;
         this.load_I = clamped.load_I;
         this.reset = clamped.read;
         this.start = clamped.write;
         this.init_Z = clamped.init_Z;
         this.load_Z = clamped.load_Z;
      }
   }

   public void init_V_I_8(int t) {
      this.size = t;
   }

   public boolean push_Z_10() {
      return this.write_Z;
   }

   public void load(boolean configurationOnly) {
      boolean leavingOfflineSetup = this.update_Z && !configurationOnly;
      this.update_Z = configurationOnly;
      if (configurationOnly) {
         this.init_F_2.init_V();
      }

      if (leavingOfflineSetup) {
         this.init_AutismPacketInspec.write_V_17();
         this.init_AutismPacketLogger.init_V_Z(false);
         this.init_AutismPacketLogger_3.init_V_Z(false);
      }
   }

   public synchronized void read(boolean paused) {
      if (this.write_Z != paused) {
         if (paused) {
            this.next_V_5();
         } else {
            this.load_J = System.currentTimeMillis();
         }

         this.write_Z = paused;
         this.pop_Z = true;
         PacketCaptureState.write();
         AutismConfig config = AutismConfig.getGlobal();
         if (config.packetLoggerCapturing != !paused) {
            config.packetLoggerCapturing = !paused;
            config.save();
         }
      }
   }

   public void init_V_Packet_Str(Packet<?> packet, String direction) {
      this.init_V_Packet(packet, direction, false, false);
   }

   public void load_V_Packet_Str(Packet<?> packet, String direction) {
      this.init_V_Packet(packet, direction, true, true);
   }

   public void init_V_J(long timestampMs, int tick, String direction, Class<?> packetClass, AutismPayloadSupport.PayloadEntry payloadSnapshot) {
      if (payloadSnapshot != null) {
         synchronized (this) {
            String name = packetClass != null && Packet.class.isAssignableFrom(packetClass) ? init_Str_Class(packetClass) : "Custom Payload";
            boolean isInventory = init_Z_Str_Set(name, read_Set);
            boolean isMovement = init_Z_Str_Set(name, write_Set);
            AutismPacketLoggerOverlay.Inner_JsonUtil e = new AutismPacketLoggerOverlay.Inner_JsonUtil(
               timestampMs, tick, direction, name, packetClass, null, isInventory, isMovement, true, null, null, payloadSnapshot, PacketStateDeltaTracker.init_bq_DataUtil
            );
            this.init_List.add(e);
            this.flush(true);
         }
      }
   }

   private static String init_Str_Class(Class<?> packetClass) {
      try {
         return PacketNameResolver.init_Str_Class((Class<? extends Packet<?>>)packetClass);
      } catch (Throwable ignored) {
         return packetClass == null ? "Custom Payload" : packetClass.getSimpleName();
      }
   }

   private void init_V_Packet(Packet<?> packet, String direction, boolean ignorePaused, boolean payloadOnly) {
      if (packet != null && (ignorePaused || !this.write_Z)) {
         synchronized (this) {
            if (ignorePaused || !this.write_Z) {
               Class<?> cls = packet.getClass();
               String name = PacketNameResolver.init_Str_Packet_Str(packet, direction);
               boolean isInventory = init_Z_Str_Set(name, read_Set);
               boolean isMovement = init_Z_Str_Set(name, write_Set);
               boolean isPayload = init_Z_Class_Str(cls, name);
               if ((payloadOnly && isPayload || !this.load_Z_Class_Str(cls, name)) && (!payloadOnly || isPayload)) {
                  PacketStateDeltaTracker.Inner_DataUtil packetContext = this.init_bq.init_bq_DataUtil_Packet_Str(packet, direction);
                  AutismPacketLoggerOverlay.Inner_NetHelper captureContext = init_AutismPacketLogger_Packet(packet);
                  AutismPayloadSupport.PayloadEntry payloadSnapshot = isPayload ? AutismPayloadSupport.init_AutismPayloadSuppo(packet, direction) : null;
                  if (payloadSnapshot != null) {
                     PayloadChannelFilterManager.Inner_StreamUtil match = this.init_bB.init_bB_StreamUtil_AutismPayloadSuppo_Str(payloadSnapshot, direction);
                     if (match != null) {
                        PayloadNotificationTracker.init(payloadSnapshot.load_Str(), direction, match);
                     }
                  }

                  AutismPacketLoggerOverlay.Inner_JsonUtil e = new AutismPacketLoggerOverlay.Inner_JsonUtil(
                     System.currentTimeMillis(),
                     this.size,
                     direction,
                     name,
                     cls,
                     packet,
                     isInventory,
                     isMovement,
                     isPayload,
                     captureContext.init_Str(),
                     captureContext.load(),
                     payloadSnapshot,
                     packetContext
                  );
                  this.init_List.add(e);
                  this.flush(false);
               }
            }
         }
      }
   }

   private static AutismPacketLoggerOverlay.Inner_NetHelper init_AutismPacketLogger_Packet(Packet<?> packet) {
      if (packet != null && init_Minecraft != null && init_Minecraft.isSameThread()) {
         try {
            if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
               return new AutismPacketLoggerOverlay.Inner_NetHelper(init_Str_BlockPos_2(actionPacket.getPos()), null);
            }

            if (packet instanceof ServerboundUseItemOnPacket interactBlockPacket) {
               return new AutismPacketLoggerOverlay.Inner_NetHelper(init_Str_BlockPos_2(interactBlockPacket.hitResult().getBlockPos()), null);
            }

            if (packet instanceof ServerboundContainerClosePacket) {
               return new AutismPacketLoggerOverlay.Inner_NetHelper(null, read_Str_6());
            }
         } catch (Throwable var2) {
         }

         return AutismPacketLoggerOverlay.Inner_NetHelper.init_AutismPacketLogger;
      } else {
         return AutismPacketLoggerOverlay.Inner_NetHelper.init_AutismPacketLogger;
      }
   }

   private static String init_Str_BlockPos_2(BlockPos pos) {
      if (pos != null && init_Minecraft.level != null) {
         try {
            BlockState state = init_Minecraft.level.getBlockState(pos);
            return state == null ? null : BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + state;
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String read_Str_6() {
      if (init_Minecraft.gui.screen() == null) {
         return null;
      }

      try {
         String title = init_Minecraft.gui.screen().getTitle() == null ? "" : init_Minecraft.gui.screen().getTitle().getString().trim();
         String className = init_Minecraft.gui.screen().getClass().getSimpleName();
         if (title.isEmpty()) {
            return className;
         } else {
            return className != null && !className.isBlank() ? title + " [" + className + "]" : title;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   public boolean init_Z_Class(Class<?> cls) {
      return this.load(cls);
   }

   private static boolean init_Z_Str_Set(String name, Set<String> set) {
      for (String s : set) {
         if (name.contains(s)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init_Z_Class_Str(Class<?> cls, String name) {
      if (init_Z_Str_Set(name, update_Set)) {
         return true;
      }

      if (cls == null) {
         return false;
      }

      String simpleName = cls.getSimpleName();
      if (init_Z_Str_Set(simpleName, update_Set)) {
         return true;
      }

      String className = cls.getName();
      return init_Z_Str_Set(className, update_Set);
   }

   private static Set<String> init_Set_2() {
      Set<String> keys = new LinkedHashSet<>();
      init_V_Set_StrArr(
         keys,
         "ServerboundMovePlayerPacket.Pos",
         "ServerboundMovePlayerPacket.PosRot",
         "ServerboundMovePlayerPacket.Rot",
         "ServerboundMovePlayerPacket.StatusOnly",
         "ServerboundClientTickEndPacket",
         "ServerboundPlayerInputPacket",
         "ServerboundAcceptTeleportationPacket",
         "ServerboundChunkBatchReceivedPacket",
         "ServerboundPlayerCommandPacket",
         "ServerboundKeepAlivePacket",
         "ServerboundPongPacket",
         "ClientboundBundlePacket",
         "ClientboundBundleDelimiterPacket",
         "ClientboundKeepAlivePacket",
         "ClientboundPingPacket",
         "ClientboundMoveEntityPacket.Pos",
         "ClientboundMoveEntityPacket.PosRot",
         "ClientboundMoveEntityPacket.Rot",
         "ClientboundEntityPositionSyncPacket",
         "ClientboundSetEntityMotionPacket",
         "ClientboundRotateHeadPacket",
         "ClientboundTeleportEntityPacket",
         "ClientboundMoveMinecartPacket",
         "ClientboundSetEntityDataPacket",
         "ClientboundUpdateAttributesPacket",
         "ClientboundEntityEventPacket",
         "ClientboundLightUpdatePacket",
         "ClientboundLevelChunkWithLightPacket",
         "ClientboundForgetLevelChunkPacket",
         "ClientboundChunkBatchStartPacket",
         "ClientboundChunkBatchFinishedPacket",
         "ClientboundSetChunkCacheCenterPacket",
         "ClientboundChunksBiomesPacket",
         "ClientboundSectionBlocksUpdatePacket",
         "ClientboundBlockUpdatePacket",
         "ClientboundBlockEntityDataPacket",
         "ClientboundSetTimePacket",
         "ClientboundLevelParticlesPacket",
         "ClientboundLevelEventPacket",
         "ClientboundSoundPacket",
         "ClientboundSoundEntityPacket",
         "ClientboundPlayerInfoUpdatePacket",
         "ClientboundSetObjectivePacket",
         "ClientboundTickingStatePacket"
      );
      keys.addAll(init_Set);
      return Collections.unmodifiableSet(keys);
   }

   private static Set<String> load_Set() {
      Set<String> keys = new LinkedHashSet<>();
      init_V_Set_StrArr(
         keys,
         "ClientboundSetScorePacket",
         "ClientboundSetEquipmentPacket",
         "ClientboundAnimatePacket",
         "ClientboundTabListPacket",
         "ClientboundBlockDestructionPacket",
         "ClientboundPlayerPositionPacket",
         "ClientboundExplodePacket",
         "ClientboundRemoveEntitiesPacket",
         "ClientboundBlockEventPacket",
         "ClientboundSetPlayerTeamPacket",
         "ClientboundSystemChatPacket"
      );
      return Collections.unmodifiableSet(keys);
   }

   private static void init_V_Set_StrArr(Set<String> keys, String... names) {
      for (String name : names) {
         init_V_Set_Str_2(keys, name);
      }
   }

   private static void init_V_Set_Str_2(Set<String> keys, String name) {
      if (name != null && !name.isBlank()) {
         keys.add(name.trim());
      }
   }

   private static String init_Str_Str_5(String value) {
      if (value != null && !value.isEmpty()) {
         String lower = value.toLowerCase(Locale.ROOT);
         StringBuilder normalized = new StringBuilder(lower.length());

         for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
               normalized.append(c);
            }
         }

         return normalized.toString();
      } else {
         return "";
      }
   }

   private void open_V_10() {
      this.flush_Set.clear();
      this.init_Map.clear();

      for (String name : this.sync_Set) {
         if (name != null && !name.isBlank()) {
            this.flush_Set.add(init_Str_Str_5(name));
            if (name.endsWith("Packet")) {
               this.flush_Set.add(init_Str_Str_5(name.substring(0, name.length() - 6)));
            }
         }
      }
   }

   private boolean load_Z_Class_Str(Class<?> cls, String friendlyName) {
      if (this.flush_Set.isEmpty()) {
         return false;
      } else {
         return this.flush_Set.contains(init_Str_Str_5(friendlyName)) ? true : this.load(cls);
      }
   }

   private boolean load(Class<?> cls) {
      if (cls != null && !this.flush_Set.isEmpty()) {
         Boolean cached = this.init_Map.get(cls);
         if (cached != null) {
            return cached;
         }

         boolean blocked = this.read(cls);
         this.init_Map.put(cls, blocked);
         return blocked;
      } else {
         return false;
      }
   }

   private boolean read(Class<?> cls) {
      if (this.flush_Set.contains(init_Str_Str_5(cls.getSimpleName()))) {
         return true;
      }

      if (this.flush_Set.contains(init_Str_Str_5(cls.getName()))) {
         return true;
      }

      if (Packet.class.isAssignableFrom(cls)) {
         Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)cls;
         if (this.flush_Set.contains(init_Str_Str_5(PacketNameResolver.init_Str_Class(packetClass)))) {
            return true;
         }
      }

      return false;
   }

   private void poll_V_7() {
      AutismConfig config = AutismConfig.getGlobal();
      this.sync_Set.clear();
      if (config.packetLoggerBlockedInit) {
         if (config.packetLoggerBlocked != null) {
            this.sync_Set.addAll(config.packetLoggerBlocked);
         }

         if (config.packetLoggerBlockedDefaultsVersion < 2) {
            this.sync_Set.addAll(init_Set);
            config.packetLoggerBlocked = new ArrayList<>(this.sync_Set);
            config.packetLoggerBlockedDefaultsVersion = 2;
            config.save();
         }
      } else {
         this.sync_Set.addAll(load_Set);
         config.packetLoggerBlocked = new ArrayList<>(this.sync_Set);
         config.packetLoggerBlockedInit = true;
         config.packetLoggerBlockedDefaultsVersion = 2;
         config.save();
      }

      this.open_V_10();
   }

   private void set_V_7() {
      AutismConfig config = AutismConfig.getGlobal();
      config.packetLoggerBlocked = new ArrayList<>(this.sync_Set);
      config.packetLoggerBlockedInit = true;
      config.packetLoggerBlockedDefaultsVersion = 2;
      config.save();
   }

   public synchronized void load(String name) {
      if (name != null && !name.isBlank()) {
         if (this.sync_Set.add(name.trim())) {
            this.open_V_10();
            this.set_V_7();
         }

         this.clear_V_6();
      }
   }

   private synchronized void read(String name) {
      if (this.sync_Set.remove(name)) {
         this.open_V_10();
         this.set_V_7();
      }
   }

   private synchronized void init_V_Class_Str(Class<?> cls, String friendlyName) {
      Set<String> targets = new HashSet<>();
      if (friendlyName != null && !friendlyName.isBlank()) {
         targets.add(init_Str_Str_5(friendlyName));
      }

      if (cls != null) {
         targets.add(init_Str_Str_5(cls.getSimpleName()));
         targets.add(init_Str_Str_5(cls.getName()));
      }

      for (String t : new ArrayList<>(targets)) {
         if (t.endsWith("packet")) {
            targets.add(t.substring(0, t.length() - 6));
         }
      }

      targets.remove("");
      if (!targets.isEmpty()) {
         boolean changed = this.sync_Set
            .removeIf(
               raw -> {
                  if (raw == null) {
                     return false;
                  } else {
                     return targets.contains(init_Str_Str_5(raw))
                        ? true
                        : raw.endsWith("Packet") && targets.contains(init_Str_Str_5(raw.substring(0, raw.length() - 6)));
                  }
               }
            );
         if (changed) {
            this.open_V_10();
            this.set_V_7();
         }
      }
   }

   private synchronized void put_V_6() {
      this.sync_Set.clear();
      this.sync_Set.addAll(load_Set);
      this.open_V_10();
      this.set_V_7();
      this.clear_V_6();
   }

   private synchronized void add_V_7() {
      this.sync_Set.clear();
      this.open_V_10();
      this.set_V_7();
   }

   private synchronized void clear_V_6() {
      this.init_Deque.removeIf(e -> this.load_Z_Class_Str(e.init_Class, e.load_Str));
      this.load_Deque.removeIf(e -> this.load_Z_Class_Str(e.init_Class, e.load_Str));
      this.read_Deque.removeIf(e -> this.load_Z_Class_Str(e.init_Class, e.load_Str));
      this.write_Deque.removeIf(e -> this.load_Z_Class_Str(e.init_Class, e.load_Str));
      this.pop_Z = true;
   }

   public static boolean init_Z_Packet(Packet<?> packet) {
      if (packet == null) {
         return false;
      }

      if (AutismPayloadSupport.init(packet) != null) {
         return true;
      }

      Class<?> cls = packet.getClass();
      return init_Z_Class_Str(cls, PacketNameResolver.init_Str_Packet_Str(packet, ""));
   }

   private static void init_V_Deque(Deque<AutismPacketLoggerOverlay.Inner_JsonUtil> buf, AutismPacketLoggerOverlay.Inner_JsonUtil e, int cap) {
      buf.addLast(e);

      while (buf.size() > cap) {
         buf.removeFirst();
      }
   }

   private void reset_V_5() {
      synchronized (this) {
         this.flush(false);
      }
   }

   private void flush(boolean force) {
      long now = System.currentTimeMillis();
      if (!force && this.init_List.size() < 1600) {
         if (this.init_List.isEmpty()) {
            return;
         }

         if (this.load_J != 0L && now - this.load_J < 500L) {
            return;
         }
      }

      this.next_V_5();
      this.load_J = now;
   }

   private void stop_V_5() {
      synchronized (this) {
         this.next_V_5();
         this.load_J = System.currentTimeMillis();
      }
   }

   private void next_V_5() {
      if (!this.init_List.isEmpty()) {
         for (AutismPacketLoggerOverlay.Inner_JsonUtil e : this.init_List) {
            init_V_Deque(this.init_Deque, e, 800);
            if (e.init_Z) {
               init_V_Deque(this.load_Deque, e, 400);
            }

            if (e.load_Z) {
               init_V_Deque(this.read_Deque, e, 200);
            }

            if (e.read_Z) {
               init_V_Deque(this.write_Deque, e, 400);
            }
         }

         this.init_List.clear();
         this.pop_Z = true;
      }
   }

   @Override
   public void init_V_Z(boolean v) {
      this.init_Z = v;
      if (v) {
         this.next = 0;
         this.init_CacheEntry.load(0, 0);
         this.pop_Z = true;
         this.init_F_2.init_V();
         OverlayManager.init_bk().update_V(this);
      } else {
         this.init_AutismPacketInspec.write_V_17();
      }

      this.a__V();
   }

   public void close_V_12() {
      this.init_V_Z(!this.init_Z);
   }

   @Override
   public boolean sync_Z() {
      return this.init_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.load_Z;
   }

   @Override
   public void write_V_Z(boolean c) {
      if (this.load_Z != c) {
         this.load_Z = c;
         this.sync_Z = false;
         this.flush_Z = false;
         this.close_Z = false;
         if (c) {
            this.get_V();
            this.init_F_2.init_V();
         }

         this.a__V();
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean write_Z() {
      return this.init_k != null && this.init_k.init_Z();
   }

   @Override
   public void push_V() {
      if (this.init_k != null) {
         this.init_k.push(false);
      }
   }

   @Override
   public int b__I() {
      return 10;
   }

   private void init_V_GuiGraphicsExtract_8(GuiGraphicsExtractor context, String text, autismclient.gui.f.b.l tone, int color, int x, int y) {
      autismclient.gui.f.b.k.init(context, this.init_Font, text, this.init_CharEncoder.init_Identifier_l(tone), color, x, y, false);
   }

   @Override
   public boolean init_Z_D_D(double mx, double my) {
      if (!this.init_Z) {
         return false;
      }

      int panelHeight = this.load_Z ? 14 : this.stop;
      OverlayBounds bounds = new OverlayBounds(this.init_I_2, this.load_I, this.reset, panelHeight, this.init_Z, this.load_Z);
      int h = this.load_I_cH_Z(bounds, this.load_Z);
      boolean overPanel = mx >= this.init_I_2 && mx <= this.init_I_2 + this.reset && my >= this.load_I && my <= this.load_I + h;
      return overPanel | this.init_F_2.init(mx, my);
   }

   @Override
   public boolean load_Z_D_D(double mx, double my) {
      return !this.init_Z
         ? false
         : mx >= this.init_I_2 && mx <= this.init_I_2 + this.reset && my >= this.load_I && my <= this.load_I + 14 && !this.read_Z_D_2(mx, my, this.init_cH());
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         this.reset_V_5();
         if (this.pop_Z) {
            this.prev_V_5();
            this.pop_Z = false;
         }

         OverlayBounds clamped = this.init_cH_cX_cH(
            this, new OverlayBounds(this.init_I_2, this.load_I, this.init_I_I_I(this.reset, this.init_I()), this.tick_I_17(), this.init_Z, this.load_Z)
         );
         this.init_I_2 = clamped.init_I;
         this.load_I = clamped.load_I;
         this.reset = clamped.read;
         this.stop = clamped.write;
         int ph = this.stop;
         int total = 0;

         for (AutismPacketLoggerOverlay.Inner_CachePool r : this.load_List) {
            total += r.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_RenderState.GROUP ? r.init_I : 1;
         }

         int bodyMx = mx;
         int bodyMy = my;
         if (this.init_F_2.init(mx, my)) {
            bodyMx = -10000;
            bodyMy = -10000;
         }

         String title = "Packet Logger";
         OverlayBounds bounds = new OverlayBounds(this.init_I_2, this.load_I, this.reset, ph, this.init_Z, this.load_Z);
         this.init_V_GuiGraphicsExtract_6(ctx, bodyMx, bodyMy, bounds, title, this.load_Z, this.sync_Z);
         this.init_V_GuiGraphicsExtract_13(ctx, bodyMx, bodyMy, bounds);
         boolean clipBody = this.init_Z_GuiGraphicsExtract(ctx, bounds, this.load_Z);
         if (!clipBody) {
            this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, this.sync_Z);
         } else {
            try {
               int tabY = this.load_I + 14 + 2;
               this.init_V_GuiGraphicsExtract_6(ctx, bodyMx, bodyMy, tabY, total);
               int filterY = tabY + this.clear_I_6() + 2;
               this.init_V_GuiGraphicsExtract_9(ctx, bodyMx, bodyMy, filterY);
               int contentY = filterY + this.stop_I_5() + 2;
               int contentEndY = contentY + this.pop_I_19();
               if (this.load_List.isEmpty()) {
                  this.init_V_GuiGraphicsExtract_8(
                     ctx, "No packets matching filters", autismclient.gui.f.b.l.write, UiColorPalette.bind(), this.init_I_2 + 10, this.init_I_I_8(contentY)
                  );
               } else {
                  int contentHeight = this.load_List.size() * this.put_I_6();
                  int viewHeight = this.pop_I_19();
                  int maxScroll = Math.max(0, contentHeight - viewHeight);
                  this.next = this.init_I_I_3(this.next, this.put_I_6(), maxScroll);
                  this.init_CacheEntry.init_V_I_I(this.next, maxScroll);
                  int drawScroll = this.init_CacheEntry.init_I_F_I(delta, maxScroll);
                  autismclient.gui.ui.UiClipStack.init_HashUtil()
                     .init(ctx, autismclient.gui.f.NetHelper.init(this.init_I_2, contentY, this.reset, contentEndY - contentY));
                  int drawBase = contentY - drawScroll;

                  for (int i = 0; i < this.load_List.size(); i++) {
                     int ey = drawBase + i * this.put_I_6();
                     if (ey + this.put_I_6() > contentY && ey < contentEndY) {
                        AutismPacketLoggerOverlay.Inner_CachePool row = this.load_List.get(i);
                        if (row.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_RenderState.GROUP) {
                           this.init_V_GuiGraphicsExtract_7(ctx, row, ey, bodyMx, bodyMy);
                        } else {
                           this.init_V_GuiGraphicsExtract_8(ctx, row.init_AutismPacketLogger, ey, bodyMx, bodyMy);
                        }
                     }
                  }

                  autismclient.gui.ui.UiClipStack.init_HashUtil().init(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_7();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(ctx, scrollbarMetrics, scrollbarMetrics.init(bodyMx, bodyMy), this.close_Z);
               }
            } finally {
               this.init_V_GuiGraphicsExtract_Z(ctx, clipBody);
               this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, this.sync_Z);
            }

            if (this.init_F_2.init_Z()) {
               ctx.nextStratum();
               this.init_F_2.init(ctx, mx, my);
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_6(GuiGraphicsExtractor ctx, int mx, int my, int y, int total) {
      int x = this.init_I_2 + 4;

      for (AutismPacketLoggerOverlay.Inner_DataUtil cat : AutismPacketLoggerOverlay.Inner_DataUtil.values()) {
         int w = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, cat.init_Str, 5, 32, 64);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.init_Font, x, y, w, this.clear_I_6(), cat.init_Str, this.init_AutismPacketLogger_2 == cat, mx, my);
         x += w + 2;
      }

      String summary = this.write_Z ? "Paused  " + total : Integer.toString(total);
      if (this.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD) {
         PayloadSubscriptionManager.Inner_BufferHelper subscriptionStatus = PayloadSubscriptionManager.init_bD_BufferHelper();
         String channelStatus = subscriptionStatus == null ? "" : subscriptionStatus.init_Str();
         if (!channelStatus.isBlank()) {
            summary = summary + "  " + channelStatus;
         }
      }

      int summaryColor = this.write_Z ? this.init_CharEncoder.load(autismclient.gui.f.b.l.write) : UiColorPalette.parse();
      int summaryWidth = autismclient.gui.f.b.k.init(
         this.init_Font, summary, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write), summaryColor
      );
      int summaryX = this.init_I_2 + this.reset - 6 - summaryWidth;
      int minSummaryX = x + 4;
      if (summaryX >= minSummaryX) {
         int summaryY = UiLayoutMath.init(y, this.clear_I_6(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush());
         this.init_V_GuiGraphicsExtract_8(ctx, summary, autismclient.gui.f.b.l.write, summaryColor, summaryX, summaryY);
      }
   }

   private int push_I_19() {
      return this.load_I + 14 + 2 + this.clear_I_6() + 2 + this.stop_I_5() + 2;
   }

   private int pop_I_19() {
      int rawHeight = Math.max(0, this.stop - 14 - this.clear_I_6() - this.stop_I_5() - 8 - this.run_I_17());
      return this.sync_I(rawHeight, this.put_I_6());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_7() {
      int contentHeight = this.load_List.size() * this.put_I_6();
      int viewHeight = this.pop_I_19();
      int maxScroll = Math.max(0, contentHeight - viewHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
         contentHeight, viewHeight, this.init_I_2 + this.reset - 5, this.push_I_19(), 3, viewHeight, this.init_CacheEntry.init_I_F_I(0.0F, maxScroll)
      );
   }

   private String write_Str_4() {
      return this.update_Z ? (this.write_Z ? "Auto OFF" : "Auto ON") : (this.write_Z ? "Start" : "Stop");
   }

   private String[] init_StrArr_2() {
      return new String[]{this.write_Str_4(), "Clear", "Copy", this.open_Z ? "Group" : "Ungrp", "Blocked", "Channels", "Payload"};
   }

   private AutismPacketLoggerOverlay.Inner_BufferHelper init_AutismPacketLogger_2() {
      String[] labels = this.init_StrArr_2();
      int[] widths = new int[labels.length];
      int total = 0;

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.init_I_Str_6(labels[i]);
         total += widths[i];
      }

      if (total + 2 * (labels.length - 1) <= this.reset - 8) {
         return new AutismPacketLoggerOverlay.Inner_BufferHelper(labels, widths, 2);
      }

      int[] legacy = new int[labels.length];
      legacy[0] = autismclient.gui.ui.layout.PanelLayoutMath.init(
         this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[0], 5, 38, this.update_Z ? 66 : 54
      );
      legacy[1] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[1], 5, 34, 54);
      legacy[2] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[2], 5, 34, 52);
      legacy[3] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[3], 5, 38, 58);
      legacy[4] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[4], 5, 54, 72);
      legacy[5] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[5], 5, 58, 78);
      legacy[6] = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, labels[6], 5, 44, 64);
      return new AutismPacketLoggerOverlay.Inner_BufferHelper(labels, legacy, 2);
   }

   private void init_V_GuiGraphicsExtract_9(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int gap = 2;
      int row1Y = y;
      int row2Y = y + this.reset_I_5() + this.start_I_12();
      int x = this.init_I_2 + 4;
      AutismPacketLoggerOverlay.Inner_BufferHelper row1 = this.init_AutismPacketLogger_2();
      this.init_V_GuiGraphicsExtract_4(ctx, x, row1Y, row1.init_IArr[0], this.reset_I_5(), row1.init_StrArr[0], !this.write_Z, "packet-logger:capture", mx, my);
      x += row1.init_IArr[0] + row1.init_I;
      this.init_V_GuiGraphicsExtract_3(
         ctx, x, row1Y, row1.init_IArr[1], this.reset_I_5(), row1.init_StrArr[1], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.init_IArr[1] + row1.init_I;
      this.init_V_GuiGraphicsExtract_3(
         ctx, x, row1Y, row1.init_IArr[2], this.reset_I_5(), row1.init_StrArr[2], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.init_IArr[2] + row1.init_I;
      this.init_V_GuiGraphicsExtract_4(ctx, x, row1Y, row1.init_IArr[3], this.reset_I_5(), row1.init_StrArr[3], this.open_Z, "packet-logger:grouping", mx, my);
      x += row1.init_IArr[3] + row1.init_I;
      this.init_V_GuiGraphicsExtract_3(
         ctx, x, row1Y, row1.init_IArr[4], this.reset_I_5(), row1.init_StrArr[4], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.init_IArr[4] + row1.init_I;
      this.init_V_GuiGraphicsExtract_3(
         ctx, x, row1Y, row1.init_IArr[5], this.reset_I_5(), row1.init_StrArr[5], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.init_IArr[5] + row1.init_I;
      this.init_V_GuiGraphicsExtract_3(
         ctx, x, row1Y, row1.init_IArr[6], this.reset_I_5(), row1.init_StrArr[6], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x = this.init_I_2 + 4;
      int sw = this.prev_I_4();
      this.init_k.read(x);
      this.init_k.write(row2Y);
      this.init_k.update(sw);
      this.init_k.sync(this.reset_I_5());
      if (!Objects.equals(this.init_k.load_Str(), this.load_Str)) {
         this.init_k.init_V_Str(this.load_Str);
      }

      this.init_k.init_V(ctx, mx, my, 0.0F);
      x += sw + 3;
      String[] dirLabels = new String[]{"Both", "C2S", "S2C"};

      for (int d = 0; d < 3; d++) {
         int bw = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, dirLabels[d], 4, 30, 48);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.init_Font, x, row2Y, bw, this.reset_I_5(), dirLabels[d], this.valid == d, mx, my);
         x += bw + gap;
      }

      if (this.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD) {
         String listenLabel = this.poll_Z ? "Filtered" : "All";
         int listenW = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, listenLabel, 4, 42, 62);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.init_Font, x, row2Y, listenW, this.reset_I_5(), listenLabel, this.poll_Z, mx, my);
      }
   }

   private void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean active,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.init_Font, x, y, w, h, label, variant, active, mx, my);
   }

   private void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean enabled, String animationKey, int mx, int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.init_Font, x, y, w, h, label, enabled, animationKey, mx, my);
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.Inner_CachePool row, int y, int mx, int my) {
      int x = this.init_I_2 + 4;
      boolean exp = this.close_Set.contains(row.init_Str);
      boolean hov = mx >= this.init_I_2 && mx <= this.init_I_2 + this.reset && my >= y && my < y + this.put_I_6();
      PayloadChannelFilterManager.Inner_StreamUtil listenerMatch = row.init_AutismPayloadSuppo == null
         ? null
         : this.init_bB.init_bB_StreamUtil_AutismPayloadSuppo_Str(row.init_AutismPayloadSuppo, row.load);
      if (listenerMatch != null) {
         ctx.fill(this.init_I_2 + 2, y, this.init_I_2 + this.reset - 4, y + this.put_I_6(), UiColorPalette.init(hov));
      } else if (hov) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.init(ctx, this.init_I_2 + 2, y, this.reset - 4, this.put_I_6(), true, false);
      }

      autismclient.gui.ui.widgets.GlyphIndicatorRenderer.init(
         ctx,
         x,
         y + (this.put_I_6() - 8) / 2,
         8,
         exp ? autismclient.gui.ui.widgets.GlyphIndicatorRenderer.Inner_DataUtil.DOWN : autismclient.gui.ui.widgets.GlyphIndicatorRenderer.Inner_DataUtil.RIGHT,
         hov ? -659730 : -1582374,
         -1204153320,
         1.0F
      );
      x += 10;
      String arrow = row.load.equals("C2S") ? ">" : "<";
      int color = row.load.equals("C2S") ? -12268289 : -21948;
      this.init_V_GuiGraphicsExtract_8(ctx, arrow, autismclient.gui.f.b.l.read, color, x, this.init_I_I_8(y));
      x += 12;
      String displayName = row.init_Str.contains(":") ? row.init_Str.substring(row.init_Str.indexOf(58) + 1) : row.init_Str;
      String summary = row.init_AutismPayloadSuppo == null ? "" : AutismPayloadSupport.init(row.init_AutismPayloadSuppo, true);
      String listenText = listenerMatch == null ? "" : " [" + listenerMatch.load() + "]";
      String line = displayName + " (x" + row.init_I + ")" + listenText + (summary.isBlank() ? "" : " " + summary);
      int maxW = this.reset - (x - this.init_I_2) - 32;
      if (autismclient.gui.f.b.k.init(this.init_Font, line, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), color) > maxW) {
         line = autismclient.gui.f.b.k.init(
            this.init_Font, line, Math.max(1, maxW), this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), color
         );
      }

      this.init_V_GuiGraphicsExtract_8(ctx, line, autismclient.gui.f.b.l.read, color, x, this.init_I_I_8(y));
      int bx = this.init_I_2 + this.reset - 28;
      boolean hb = mx >= bx && mx <= bx + 24 && my >= y && my < y + this.put_I_6();
      this.init_V_GuiGraphicsExtract_8(ctx, "BLK", autismclient.gui.f.b.l.write, hb ? -48060 : UiColorPalette.bind(), bx, this.init_I_I_8(y));
   }

   private void init_V_GuiGraphicsExtract_8(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.Inner_JsonUtil e, int y, int mx, int my) {
      int x = this.init_I_2 + 4;
      boolean hov = mx >= this.init_I_2 && mx <= this.init_I_2 + this.reset && my >= y && my < y + this.put_I_6();
      PayloadChannelFilterManager.Inner_StreamUtil listenerMatch = this.init_bB_StreamUtil_AutismPacketLogger(e);
      if (listenerMatch != null) {
         ctx.fill(this.init_I_2 + 2, y, this.init_I_2 + this.reset - 4, y + this.put_I_6(), UiColorPalette.init(hov));
      } else if (hov) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.init(ctx, this.init_I_2 + 2, y, this.reset - 4, this.put_I_6(), true, false);
      }

      int color = e.init_Str.equals("C2S") ? -12268289 : -21948;
      this.init_V_GuiGraphicsExtract_8(ctx, e.init_Str.equals("C2S") ? ">" : "<", autismclient.gui.f.b.l.read, color, x, this.init_I_I_8(y));
      x += 12;
      String time = init_DateTimeFormatter.format(Instant.ofEpochMilli(e.load_J));
      this.init_V_GuiGraphicsExtract_8(ctx, time, autismclient.gui.f.b.l.write, -4809052, x, this.init_I_I_8(y));
      x += autismclient.gui.f.b.k.init(this.init_Font, time, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write), -4809052) + 4;
      String tick = "T" + e.init_I;
      this.init_V_GuiGraphicsExtract_8(ctx, tick, autismclient.gui.f.b.l.write, -6114880, x, this.init_I_I_8(y));
      x += autismclient.gui.f.b.k.init(this.init_Font, tick, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write), -6114880) + 4;
      int maxW = this.reset - (x - this.init_I_2) - 30;
      String name = e.load_Str;
      if (e.init_AutismPayloadSuppo != null) {
         String summary = AutismPayloadSupport.init(e.init_AutismPayloadSuppo, true);
         if (!summary.isBlank()) {
            name = name + " " + summary;
         }
      }

      if (listenerMatch != null) {
         name = name + " [" + listenerMatch.load() + "]";
      }

      if (autismclient.gui.f.b.k.init(this.init_Font, name, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.scan()) > maxW) {
         name = autismclient.gui.f.b.k.init(
            this.init_Font, name, Math.max(1, maxW), this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.scan()
         );
      }

      this.init_V_GuiGraphicsExtract_8(ctx, name, autismclient.gui.f.b.l.read, color, x, this.init_I_I_8(y));
      int bx = this.init_I_2 + this.reset - 16;
      boolean hb = mx >= bx && mx <= bx + 12 && my >= y && my < y + this.put_I_6();
      this.init_V_GuiGraphicsExtract_8(ctx, "=", autismclient.gui.f.b.l.write, hb ? -8875 : UiColorPalette.bind(), bx, this.init_I_I_8(y));
   }

   private void load(GuiGraphicsExtractor ctx, int mx, int my, int startY, int endY) {
      autismclient.gui.ui.widgets.PanelSurfaceHelper.init(ctx, this.init_I_2 + 4, startY, this.reset - 8);
      int y = startY + 3;
      boolean hh = mx >= this.init_I_2 + 4 && mx <= this.init_I_2 + 150 && my >= y && my < y + this.put_I_6();
      int blockedColor = hh ? ThemePaletteManager.init_I_I_ct_DataUtil(-30584, ThemePaletteManager.ColorRole.DANGER) : ThemePaletteManager.init_I_I_ct_DataUtil(-39322, ThemePaletteManager.ColorRole.DANGER);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(this.init_I_2 + 6, y + 1, 9, 9), this.push_Z, blockedColor);
      this.init_V_GuiGraphicsExtract_8(ctx, "Blocked (" + this.sync_Set.size() + ")", autismclient.gui.f.b.l.load, blockedColor, this.init_I_2 + 17, y + 1);
      int ubX = this.init_I_2 + this.reset - 58;
      boolean hua = mx >= ubX && mx <= ubX + 54 && my >= y && my < y + this.put_I_6();
      this.init_V_GuiGraphicsExtract_8(ctx, "CLR ALL", autismclient.gui.f.b.l.write, hua ? -12255420 : UiColorPalette.parse(), ubX, y + 1);
      int rstX = ubX - 44;
      boolean hRst = mx >= rstX && mx <= rstX + 40 && my >= y && my < y + this.put_I_6();
      this.init_V_GuiGraphicsExtract_8(ctx, "RESET", autismclient.gui.f.b.l.write, hRst ? -10923 : UiColorPalette.parse(), rstX, y + 1);
      if (this.push_Z) {
         y += this.put_I_6();

         for (String name : this.sync_Set) {
            if (y + this.put_I_6() > endY) {
               break;
            }

            this.init_V_GuiGraphicsExtract_8(
               ctx,
               "  "
                  + autismclient.gui.f.b.k.init(
                     this.init_Font, name, Math.max(1, this.reset - 50), this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), -5609882
                  ),
               autismclient.gui.f.b.l.read,
               -5609882,
               this.init_I_2 + 8,
               y + 1
            );
            int ux = this.init_I_2 + this.reset - 28;
            boolean hu = mx >= ux && mx <= ux + 24 && my >= y && my < y + this.put_I_6();
            this.init_V_GuiGraphicsExtract_8(ctx, "UB", autismclient.gui.f.b.l.write, hu ? -12255420 : UiColorPalette.parse(), ux, y + 1);
            y += this.put_I_6();
         }
      }
   }

   private String[] init_StrArr_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_JsonUtil e) {
      boolean isC2S = e != null && "C2S".equalsIgnoreCase(e.init_Str);
      boolean isPayload = e != null && e.read_Z;
      String[] base;
      if (this.update_Z) {
         if (isC2S && isPayload) {
            base = new String[]{"Block", "Queue", "Edit Payload", "+PAYLOAD", "+SEND", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"};
         } else {
            base = isC2S
               ? new String[]{"Block", "Queue", "+SEND", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"}
               : new String[]{"Block", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"};
         }
      } else if (isC2S && isPayload) {
         base = new String[]{"Block", "Queue", "Replay", "Send", "Edit Payload", "+PAYLOAD", "+SEND", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"};
      } else {
         base = isC2S
            ? new String[]{"Block", "Queue", "Replay", "Send", "+SEND", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"}
            : new String[]{"Block", "+WAIT", "+ Filter", "- Filter", "Copy", "Inspect"};
      }

      return base;
   }

   private void init_V_AutismPacketLogger_2(AutismPacketLoggerOverlay.Inner_JsonUtil entry, int mouseX, int mouseY) {
      this.init_F_2.init(mouseX, mouseY, entry);
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      if (this.init_F_2.init(mouseX, mouseY, button, (entry, action, index) -> this.init_V_Str_AutismPacketLogger(action, entry))) {
         return true;
      }

      if (button != 0 && button != 1) {
         return false;
      }

      if (mouseY >= this.load_I && mouseY <= this.load_I + 14 && mouseX >= this.init_I_2 && mouseX <= this.init_I_2 + this.reset) {
         OverlayBounds bounds = new OverlayBounds(this.init_I_2, this.load_I, this.reset, this.tick_I_17(), this.init_Z, this.load_Z);
         if (this.load_Z_D_2(mouseX, mouseY, bounds)) {
            this.pop_V();
            return true;
         }

         if (this.write(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            return true;
         }

         if (button == 0) {
            this.sync_Z = true;
            this.flush_Z = false;
            this.init_D = mouseX - this.init_I_2;
            this.load_D = mouseY - this.load_I;
         }

         return true;
      } else {
         if (this.load_Z) {
            return false;
         }

         int tabY = this.load_I + 14 + 2;
         int filterY = tabY + this.clear_I_6() + 2;
         int contentY = filterY + this.stop_I_5() + 2;
         if (mouseY >= tabY && mouseY < tabY + this.clear_I_6() && button == 0) {
            int x = this.init_I_2 + 4;

            for (AutismPacketLoggerOverlay.Inner_DataUtil cat : AutismPacketLoggerOverlay.Inner_DataUtil.values()) {
               int w = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, cat.init_Str, 5, 32, 64);
               if (mouseX >= x && mouseX < x + w) {
                  this.init_AutismPacketLogger_2 = cat;
                  this.pop_Z = true;
                  this.next = 0;
                  return true;
               }

               x += w + 2;
            }

            return true;
         } else {
            if (mouseY >= filterY && mouseY < filterY + this.stop_I_5() && button == 0) {
               return this.read(mouseX, mouseY, filterY);
            }

            if (this.init_k.init_Z()) {
               this.init_k.push(false);
            }

            int contentEndY = contentY + this.pop_I_19();
            if (mouseY >= contentY && mouseY < contentEndY) {
               if (button == 0) {
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_7();
                  if (scrollbarMetrics.init_Z() && scrollbarMetrics.init((int)mouseX, (int)mouseY)) {
                     this.close_Z = true;
                     this.prev = Math.max(0, (int)mouseY - scrollbarMetrics.update());
                     this.next = this.init_I_I_3(
                        autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(scrollbarMetrics, (int)mouseY, this.prev), this.put_I_6(), scrollbarMetrics.flush()
                     );
                     this.init_CacheEntry.load(this.next, scrollbarMetrics.flush());
                     return true;
                  }
               }

               return this.init_Z_D(mouseX, mouseY, contentY, button);
            } else {
               return false;
            }
         }
      }
   }

   private boolean read(double mouseX, double mouseY, int y) {
      int gap = 2;
      int row1Y = y;
      int row2Y = y + this.reset_I_5() + this.start_I_12();
      int x = this.init_I_2 + 4;
      AutismPacketLoggerOverlay.Inner_BufferHelper row1 = this.init_AutismPacketLogger_2();
      if (mouseY >= row1Y && mouseY < row1Y + this.reset_I_5()) {
         for (int i = 0; i < row1.init_IArr.length; i++) {
            int w = row1.init_IArr[i];
            if (mouseX >= x && mouseX < x + w) {
               switch (i) {
                  case 0:
                     this.read(!this.write_Z);
                     break;
                  case 1:
                     this.cache();
                     break;
                  case 2:
                     this.state();
                     break;
                  case 3:
                     this.open_Z = !this.open_Z;
                     this.pop_Z = true;
                     break;
                  case 4:
                     this.size();
                     break;
                  case 5:
                     this.data();
                     break;
                  case 6:
                     this.value();
               }

               return true;
            }

            x += w + row1.init_I;
         }
      }

      x = this.init_I_2 + 4;
      int sw = this.prev_I_4();
      if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + sw) {
         this.init_k.init_Z_D_2(mouseX, mouseY, 0);
         return true;
      }

      x += sw + 3;
      String[] dirLabels = new String[]{"Both", "C2S", "S2C"};

      for (int d = 0; d < 3; d++) {
         int dirW = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, dirLabels[d], 4, 30, 48);
         if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + dirW) {
            this.valid = d;
            this.pop_Z = true;
            return true;
         }

         x += dirW + gap;
      }

      if (this.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD) {
         String listenLabel = this.poll_Z ? "Filtered" : "All";
         int listenW = autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, listenLabel, 4, 42, 62);
         if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + listenW) {
            this.poll_Z = !this.poll_Z;
            this.pop_Z = true;
            return true;
         }
      }

      this.init_k.push(false);
      return true;
   }

   private boolean init_Z_D(double mouseX, double mouseY, int contentY, int button) {
      int idx = (int)(
         (mouseY - contentY + this.init_CacheEntry.init_I_F_I(0.0F, Math.max(0, this.load_List.size() * this.put_I_6() - this.pop_I_19()))) / this.put_I_6()
      );
      if (idx >= 0 && idx < this.load_List.size()) {
         AutismPacketLoggerOverlay.Inner_CachePool row = this.load_List.get(idx);
         if (row.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_RenderState.GROUP) {
            int bx = this.init_I_2 + this.reset - 28;
            if (button == 0 && mouseX >= bx && mouseX <= bx + 24 && row.init_Class != null) {
               String n = PacketNameResolver.init_Str_Class((Class<? extends Packet<?>>)row.init_Class);
               this.load(n);
               return true;
            }

            if (button == 0) {
               if (this.close_Set.contains(row.init_Str)) {
                  this.close_Set.remove(row.init_Str);
               } else {
                  this.close_Set.add(row.init_Str);
               }

               this.pop_Z = true;
               return true;
            }
         }

         if (row.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_RenderState.ENTRY && row.init_AutismPacketLogger != null) {
            int menuIconX = this.init_I_2 + this.reset - 16;
            if (button == 1 || button == 0 && mouseX >= menuIconX) {
               this.init_V_AutismPacketLogger_2(row.init_AutismPacketLogger, (int)mouseX, (int)mouseY);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void init_V_Str_AutismPacketLogger(String action, AutismPacketLoggerOverlay.Inner_JsonUtil e) {
      if (!this.update_Z || !"Send".equals(action) && !"Replay".equals(action)) {
         switch (action) {
            case "Block":
               this.load(e.load_Str);
               AutismClientMessaging.sendPrefixed("Blocked + purged from logger: " + e.load_Str);
               break;
            case "Queue":
               LoggedPacketActions.close(e);
               break;
            case "Send":
               LoggedPacketActions.read(e);
               break;
            case "+SEND":
               LoggedPacketActions.open(e);
               break;
            case "+WAIT":
               LoggedPacketActions.poll(e);
               break;
            case "Edit Payload":
               LoggedPacketActions.push(e);
               break;
            case "+PAYLOAD":
               LoggedPacketActions.pop(e);
               break;
            case "+ Filter":
               AutismSessionState shared = AutismSessionState.init_ck();
               Class<? extends Packet<?>> pktCls = (Class<? extends Packet<?>>)e.init_Class;
               boolean added;
               if (e.init_Str.equals("C2S")) {
                  added = shared.load_Set().add(pktCls);
               } else {
                  added = shared.read_Set().add(pktCls);
               }

               shared.tick(true);
               AutismClientMessaging.sendPrefixed(
                  added ? "Added to custom " + e.init_Str + " filter: " + e.load_Str : "Already present in custom " + e.init_Str + " filter: " + e.load_Str
               );
               break;
            case "- Filter":
               AutismSessionState sharedRem = AutismSessionState.init_ck();
               Class<? extends Packet<?>> pktClsRem = (Class<? extends Packet<?>>)e.init_Class;
               boolean removed;
               if (e.init_Str.equals("C2S")) {
                  removed = sharedRem.load_Set().remove(pktClsRem);
               } else {
                  removed = sharedRem.read_Set().remove(pktClsRem);
               }

               AutismClientMessaging.sendPrefixed(
                  removed ? "Removed from custom " + e.init_Str + " filter: " + e.load_Str : "Not present in custom " + e.init_Str + " filter: " + e.load_Str
               );
               break;
            case "Replay":
               if (e.init_Packet != null && e.init_Str.equals("C2S")) {
                  LoggedPacketActions.read(e);
               }
               break;
            case "Copy":
               String line = e.init_Str + " " + init_DateTimeFormatter.format(Instant.ofEpochMilli(e.load_J)) + " T" + e.init_I + " " + e.load_Str;
               init_Minecraft.keyboardHandler.setClipboard(line);
               bi.init("Copied packet info.");
               break;
            case "Inspect":
               this.init_AutismPacketInspec.init_V_AutismPacketLogger(e, this.init_I_2 + this.reset + 10, this.load_I + 8);
         }
      }
   }

   private boolean write(double mouseX, double mouseY, int contentEndY) {
      int y = contentEndY + 3;
      if (mouseY >= y && mouseY < y + this.put_I_6()) {
         int ubX = this.init_I_2 + this.reset - 58;
         if (mouseX >= ubX && mouseX <= ubX + 54) {
            this.add_V_7();
            return true;
         } else {
            int rstX = ubX - 44;
            if (mouseX >= rstX && mouseX <= rstX + 40) {
               this.put_V_6();
               return true;
            } else {
               this.push_Z = !this.push_Z;
               return true;
            }
         }
      } else {
         if (this.push_Z) {
            int ey = y + this.put_I_6();
            int ux = this.init_I_2 + this.reset - 28;

            for (String name : new ArrayList<>(this.sync_Set)) {
               if (mouseX >= ux && mouseX <= ux + 24 && mouseY >= ey && mouseY < ey + this.put_I_6()) {
                  this.read(name);
                  return true;
               }

               ey += this.put_I_6();
            }
         }

         return false;
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int b, double dx, double dy) {
      if (this.close_Z && b == 0) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.init_PoolEntry_DataUtil_7();
         this.next = this.init_I_I_3(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(scrollbarMetrics, (int)my, this.prev), this.put_I_6(), scrollbarMetrics.flush());
         this.init_CacheEntry.load(this.next, scrollbarMetrics.flush());
         return true;
      }

      if (this.sync_Z && b == 0) {
         OverlayBounds nextBounds = this.init_cH_cX_cH(this, new OverlayBounds((int)(mx - this.init_D), (int)(my - this.load_D), this.reset, this.start, this.init_Z, this.load_Z));
         if (nextBounds.init_I != this.init_I_2 || nextBounds.load_I != this.load_I) {
            this.flush_Z = true;
         }

         this.init_I_2 = nextBounds.init_I;
         this.load_I = nextBounds.load_I;
         return true;
      } else {
         return this.init_k.init(mx, my, b, dx, dy);
      }
   }

   @Override
   public boolean load_Z_D_3(double mx, double my, int b) {
      if (b == 0 && this.close_Z) {
         this.close_Z = false;
         return true;
      } else if (b == 0 && this.sync_Z) {
         this.sync_Z = false;
         this.flush_Z = false;
         this.a__V();
         return true;
      } else {
         return this.init_k.load(mx, my, b);
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amt) {
      if (this.init_Z && !this.load_Z) {
         int totalH = this.load_List.size() * this.put_I_6();
         int visH = this.pop_I_19();
         int maxScroll = Math.max(0, totalH - visH);
         this.next = this.init_I_I_3(this.next - (int)Math.round(amt * this.put_I_6() * 3.0), this.put_I_6(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int key, int scan, int mods) {
      return this.init_Z && !this.load_Z ? this.init_k.init_Z() && this.init_k.init_Z_KeyEvent(Compat263.newGlfwKeyEvent(key, scan, mods)) : false;
   }

   @Override
   public boolean init_Z_C_I(char c, int mods) {
      return this.init_Z && !this.load_Z ? this.init_k.init_Z() && this.init_k.init_Z_CharacterEvent(new CharacterEvent(c)) : false;
   }

   private synchronized List<AutismPacketLoggerOverlay.Inner_JsonUtil> init_List_10() {
      switch (this.init_AutismPacketLogger_2) {
         case ALL:
         default:
            return new ArrayList<>(this.init_Deque);
         case INVENTORY:
            return new ArrayList<>(this.load_Deque);
         case MOVEMENT:
            return new ArrayList<>(this.read_Deque);
         case PAYLOAD:
            return new ArrayList<>(this.write_Deque);
      }
   }

   private PayloadChannelFilterManager.Inner_StreamUtil init_bB_StreamUtil_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      return entry != null && entry.read_Z && entry.init_AutismPayloadSuppo != null
         ? this.init_bB.init_bB_StreamUtil_AutismPayloadSuppo_Str(entry.init_AutismPayloadSuppo, entry.init_Str)
         : null;
   }

   private String init_Str_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry == null) {
         return "";
      }

      PayloadChannelFilterManager.Inner_StreamUtil match = this.init_bB_StreamUtil_AutismPacketLogger(entry);
      return match == null ? entry.init() : entry.init() + " " + match.init_Str();
   }

   private void prev_V_5() {
      List<AutismPacketLoggerOverlay.Inner_JsonUtil> source = this.init_List_10();
      if (source == null) {
         source = new ArrayList<>();
      }

      String ls = this.load_Str.toLowerCase(Locale.ROOT);
      List<AutismPacketLoggerOverlay.Inner_JsonUtil> filtered = new ArrayList<>();

      for (AutismPacketLoggerOverlay.Inner_JsonUtil e : source) {
         if (!this.load(e.init_Class)
            && (
               this.init_AutismPacketLogger_2 != AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD
                  || !this.poll_Z
                  || this.init_bB_StreamUtil_AutismPacketLogger(e) != null
            )
            && (ls.isEmpty() || this.init_Str_AutismPacketLogger(e).contains(ls))
            && (this.valid != 1 || e.init_Str.equals("C2S"))
            && (this.valid != 2 || e.init_Str.equals("S2C"))) {
            filtered.add(e);
         }
      }

      Map<String, Integer> counts = new LinkedHashMap<>();
      Map<String, String> dirs = new LinkedHashMap<>();
      Map<String, Class<?>> classes = new LinkedHashMap<>();

      for (AutismPacketLoggerOverlay.Inner_JsonUtil e : filtered) {
         String key = e.read_Str;
         counts.merge(key, 1, Integer::sum);
         dirs.put(key, e.init_Str);
         classes.put(key, e.init_Class);
      }

      Set<String> groupedKeys = new HashSet<>();
      if (this.open_Z) {
         for (Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 10) {
               groupedKeys.add(entry.getKey());
            }
         }
      }

      List<AutismPacketLoggerOverlay.Inner_JsonUtil> reversed = new ArrayList<>(filtered);
      Collections.reverse(reversed);
      List<AutismPacketLoggerOverlay.Inner_CachePool> rows = new ArrayList<>();
      Set<String> added = new LinkedHashSet<>();

      for (AutismPacketLoggerOverlay.Inner_JsonUtil e : reversed) {
         String key = e.read_Str;
         if (groupedKeys.contains(key) && added.add(key)) {
            AutismPacketLoggerOverlay.Inner_CachePool h = new AutismPacketLoggerOverlay.Inner_CachePool();
            h.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_RenderState.GROUP;
            h.init_Str = key;
            h.init_I = counts.get(key);
            h.load = dirs.get(key);
            h.init_Class = classes.get(key);
            h.init_AutismPayloadSuppo = e.init_AutismPayloadSuppo;
            rows.add(h);
            if (this.close_Set.contains(key)) {
               for (AutismPacketLoggerOverlay.Inner_JsonUtil child : reversed) {
                  if (child.read_Str.equals(key)) {
                     AutismPacketLoggerOverlay.Inner_CachePool r = new AutismPacketLoggerOverlay.Inner_CachePool();
                     r.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_RenderState.ENTRY;
                     r.init_AutismPacketLogger = child;
                     rows.add(r);
                  }
               }
            }
         }
      }

      for (AutismPacketLoggerOverlay.Inner_JsonUtil e : reversed) {
         if (!groupedKeys.contains(e.read_Str)) {
            AutismPacketLoggerOverlay.Inner_CachePool r = new AutismPacketLoggerOverlay.Inner_CachePool();
            r.init_AutismPacketLogger_2 = AutismPacketLoggerOverlay.Inner_RenderState.ENTRY;
            r.init_AutismPacketLogger = e;
            rows.add(r);
         }
      }

      this.load_List = rows;
      this.valid_V_3();
   }

   private void valid_V_3() {
      int totalH = this.load_List.size() * this.put_I_6();
      int visH = this.stop - 14 - this.clear_I_6() - this.stop_I_5() - 8 - this.run_I_17();
      int maxScroll = Math.max(0, totalH - visH);
      this.next = this.init_I_I_3(this.next, this.put_I_6(), maxScroll);
   }

   private int tick_I_17() {
      int rc = Math.min(this.load_List.size(), this.valid_I_3());
      int chrome = 14 + this.clear_I_6() + this.stop_I_5() + 12 + this.run_I_17();
      int content = Math.max(rc * this.put_I_6(), this.put_I_6() * 2);
      return this.load_I_I_I(Math.max(this.load_I(), Math.min(chrome + content, Math.max(this.start, this.set_I_15()))), this.a__I());
   }

   private int run_I_17() {
      return 0;
   }

   private void size() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      this.init_AutismPacketLogger.init_I_2 = Math.max(4, (sw - this.init_AutismPacketLogger.read_I) / 2);
      this.init_AutismPacketLogger.load_I = Math.max(4, (sh - this.init_AutismPacketLogger.write) / 2);
      this.init_AutismPacketLogger.init_V_Z(true);
      this.init_AutismPacketLogger.write_V_19();
      OverlayManager.init_bk().update_V(this.init_AutismPacketLogger);
   }

   private void data() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      this.init_bB.init_V();
      this.init_bC.init_V();
      this.init_AutismPacketLogger_3.init_I_2 = Math.max(4, (sw - this.init_AutismPacketLogger_3.read_I) / 2);
      this.init_AutismPacketLogger_3.load_I = Math.max(4, (sh - this.init_AutismPacketLogger_3.write) / 2);
      this.init_AutismPacketLogger_3.init_V_Z(true);
      this.init_AutismPacketLogger_3.put_V_7();
      OverlayManager.init_bk().update_V(this.init_AutismPacketLogger_3);
   }

   private void value() {
      ActionEditorOverlay editor = ActionEditorOverlay.init_ActionEditorOverla_2();
      editor.init_V_H(new autismclient.util.macro.PayloadAction());
      OverlayManager.init_bk().update_V(editor);
   }

   private synchronized void cache() {
      this.init_List.clear();
      this.init_Deque.clear();
      this.load_Deque.clear();
      this.read_Deque.clear();
      this.write_Deque.clear();
      this.init_bq.init_V();
      this.load_List.clear();
      this.next = 0;
      this.init_CacheEntry.load(0, 0);
      this.close_Set.clear();
      this.pop_Z = true;
   }

   private void state() {
      this.stop_V_5();
      StringBuilder sb = new StringBuilder();
      sb.append("=== Packet Logger [").append(this.init_AutismPacketLogger_2.init_Str).append("] ===\n");
      if (!this.load_Str.isEmpty()) {
         sb.append("Search: \"").append(this.load_Str).append("\"\n");
      }

      sb.append(String.format("%-5s %-14s %-8s %s%n", "DIR", "TIME", "TICK", "PACKET"));
      sb.append("----------------------------------------------\n");
      List<AutismPacketLoggerOverlay.Inner_JsonUtil> source = this.init_List_10();
      if (source == null) {
         source = new ArrayList<>();
      }

      String ls = this.load_Str.toLowerCase(Locale.ROOT);
      int count = 0;

      for (AutismPacketLoggerOverlay.Inner_JsonUtil e : source) {
         if (!this.load(e.init_Class)
            && (
               this.init_AutismPacketLogger_2 != AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD
                  || !this.poll_Z
                  || this.init_bB_StreamUtil_AutismPacketLogger(e) != null
            )
            && (ls.isEmpty() || this.init_Str_AutismPacketLogger(e).contains(ls))
            && (this.valid != 1 || e.init_Str.equals("C2S"))
            && (this.valid != 2 || e.init_Str.equals("S2C"))) {
            String copiedName = e.load_Str;
            if (e.init_AutismPayloadSuppo != null) {
               String summary = AutismPayloadSupport.init(e.init_AutismPayloadSuppo, true);
               if (!summary.isBlank()) {
                  copiedName = copiedName + " " + summary;
               }
            }

            PayloadChannelFilterManager.Inner_StreamUtil match = this.init_bB_StreamUtil_AutismPacketLogger(e);
            if (match != null) {
               copiedName = copiedName + " [" + match.load() + "]";
            }

            sb.append(
               String.format(
                  "%-5s %-14s %-8s %s%n",
                  e.init_Str.equals("C2S") ? "->" : "<-",
                  init_DateTimeFormatter.format(Instant.ofEpochMilli(e.load_J)),
                  "T" + e.init_I,
                  copiedName
               )
            );
            count++;
         }
      }

      sb.append("----------------------------------------------\n");
      sb.append("Total: ").append(count).append(" packets\n");
      init_Minecraft.keyboardHandler.setClipboard(sb.toString());
      bi.init("Copied " + count + " packets.");
   }

   private int get_I_15() {
      String[] labels = this.init_StrArr_2();
      int row1 = 0;

      for (String label : labels) {
         row1 += this.init_I_Str_6(label);
      }

      row1 += 2 * (labels.length - 1);
      int row2 = this.next_I_5() + 3;

      for (String label : new String[]{"Both", "C2S", "S2C"}) {
         row2 += autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 4, 30, 48) + 2;
      }

      return Math.max(row1, row2) + 8;
   }

   private int set_I_15() {
      return 14 + this.clear_I_6() + this.stop_I_5() + 12 + this.put_I_6() * 14;
   }

   private int init_I_Str_6(String label) {
      return autismclient.gui.ui.UiDensityMetrics.load(
         autismclient.gui.f.b.k.init(this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
   }

   private int put_I_6() {
      return Math.max(autismclient.gui.ui.UiDensityMetrics.init(12), this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.read, 2));
   }

   private int init_I_I_8(int y) {
      return UiLayoutMath.init(y, this.put_I_6(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
   }

   private int add_I_13() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   private int clear_I_6() {
      return this.add_I_13();
   }

   private int reset_I_5() {
      return this.add_I_13();
   }

   private int start_I_12() {
      return 2;
   }

   private int stop_I_5() {
      return this.reset_I_5() * 2 + this.start_I_12();
   }

   private int next_I_5() {
      return 148;
   }

   private int prev_I_4() {
      return this.init_AutismPacketLogger_2 == AutismPacketLoggerOverlay.Inner_DataUtil.PAYLOAD ? 108 : this.next_I_5();
   }

   private int valid_I_3() {
      return 22;
   }

   private final class BlockedPacketListOverlay extends DraggableOverlayWindow {
      private final ThemedTextInputBox init_k = new ThemedTextInputBox(AutismPacketLoggerOverlay.init_Minecraft, AutismPacketLoggerOverlay.this.init_Font, 0, 0, 120, 16, false);
      private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private final List<Class<? extends Packet<?>>> init_List = new ArrayList<>();
      private final Set<Class<? extends Packet<?>>> init_Set = new HashSet<>();
      private final List<Class<? extends Packet<?>>> load_List = new ArrayList<>();
      private int flush_I;
      private boolean write_Z;
      private int close_I;
      private boolean update_Z;
      private boolean sync_Z;
      private double init_D;
      private double load_D;
      private boolean flush_Z;

      private int push_I_20() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6("Blocked");
      }

      private int pop_I_20() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6("Clear");
      }

      private int tick_I_18() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6("Reset");
      }

      BlockedPacketListOverlay() {
         super("PacketLoggerBlockedList", 250, 248);
         this.init_I_2 = 260;
         this.load_I = 56;
         this.init_Z = false;
         this.init_k.init_V_Component(Component.literal("Search packets..."));
         this.init_k.load(120);
         this.init_k.init_V_Consumer(value -> this.flush_Z = true);
         List<Class<? extends Packet<?>>> c2s = new ArrayList<>(PacketTypeRegistry.load());
         List<Class<? extends Packet<?>>> s2c = new ArrayList<>(PacketTypeRegistry.init_Set());
         Comparator<Class<? extends Packet<?>>> byName = Comparator.comparing(PacketNameResolver::init_Str_Class, String.CASE_INSENSITIVE_ORDER);
         c2s.sort(byName);
         s2c.sort(byName);
         this.init_Set.addAll(c2s);
         this.init_List.addAll(c2s);
         this.init_List.addAll(s2c);
      }

      private int run_I_18() {
         return this.load_I + 14 + 4;
      }

      private int get_I_16() {
         return this.run_I_18() + AutismPacketLoggerOverlay.this.add_I_13() + 2;
      }

      private int set_I_16() {
         return this.get_I_16() + AutismPacketLoggerOverlay.this.add_I_13() + 4;
      }

      private String init(Class<? extends Packet<?>> cls) {
         String name = PacketTypeRegistry.init_Str_Class(cls);
         return name != null ? name : PacketNameResolver.init_Str_Class(cls);
      }

      @Override
      public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
         if (this.init_Z || this.a__Z()) {
            if (this.flush_Z) {
               this.flush_Z = false;
               this.write_V_19();
            }

            OverlayBounds bounds = this.init_cH_cX_cH(this, new OverlayBounds(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z));
            this.init_I_2 = bounds.init_I;
            this.load_I = bounds.load_I;
            this.init_V_GuiGraphicsExtract_6(ctx, mx, my, bounds, "Block List", this.load_Z, false);
            boolean clip = this.init_Z_GuiGraphicsExtract(ctx, bounds, this.load_Z);
            if (!clip) {
               this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, false);
            } else {
               try {
                  int x = this.init_I_2 + 6;
                  int y = this.run_I_18();
                  int rowH = AutismPacketLoggerOverlay.this.add_I_13();
                  this.init_k.read(x);
                  this.init_k.write(y);
                  this.init_k.update(Math.max(80, this.read_I - 12));
                  this.init_k.sync(rowH);
                  this.init_k.init_V(ctx, mx, my, delta);
                  y = this.get_I_16();
                  int bx = x;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx, AutismPacketLoggerOverlay.this.init_Font, bx, y, this.push_I_20(), rowH, "Blocked", this.update_Z, mx, my
                  );
                  int clearX = bx + this.push_I_20() + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     clearX,
                     y,
                     this.pop_I_20(),
                     rowH,
                     "Clear",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
                     true,
                     mx,
                     my
                  );
                  int resetX = clearX + this.pop_I_20() + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     resetX,
                     y,
                     this.tick_I_18(),
                     rowH,
                     "Reset",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  int countX = resetX + this.tick_I_18() + 6;
                  AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                     ctx,
                     AutismPacketLoggerOverlay.this.sync_Set.size() + " blocked",
                     autismclient.gui.f.b.l.write,
                     UiColorPalette.parse(),
                     countX,
                     UiLayoutMath.init(
                        y,
                        rowH,
                        AutismPacketLoggerOverlay.this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write),
                        AutismPacketLoggerOverlay.this.init_CharEncoder.flush()
                     )
                  );
                  int listTop = this.set_I_16();
                  int listH = Math.max(20, this.load_I + this.write - listTop - 6);
                  int contentH = this.load_List.size() * AutismPacketLoggerOverlay.this.put_I_6();
                  int maxScroll = Math.max(0, contentH - listH);
                  this.flush_I = this.init_I_I_3(this.flush_I, AutismPacketLoggerOverlay.this.put_I_6(), maxScroll);
                  this.init_CacheEntry.init_V_I_I(this.flush_I, maxScroll);
                  int drawScroll = this.init_CacheEntry.init_I_F_I(delta, maxScroll);
                  autismclient.gui.ui.UiClipStack.init_HashUtil().init(ctx, autismclient.gui.f.NetHelper.init(this.init_I_2 + 4, listTop, this.read_I - 8, listH));
                  int base = listTop - drawScroll;
                  int rowHeight = AutismPacketLoggerOverlay.this.put_I_6();
                  int firstVisible = Math.max(0, drawScroll / rowHeight);
                  int lastVisible = Math.min(this.load_List.size() - 1, (drawScroll + listH - 1) / rowHeight);

                  for (int i = firstVisible; i <= lastVisible; i++) {
                     int ry = base + i * rowHeight;
                     Class<? extends Packet<?>> cls = this.load_List.get(i);
                     boolean c2s = this.init_Set.contains(cls);
                     boolean blocked = AutismPacketLoggerOverlay.this.load_Z_Class_Str(cls, PacketNameResolver.init_Str_Class(cls));
                     boolean hover = mx >= this.init_I_2 + 4
                        && mx < this.init_I_2 + this.read_I - 8
                        && my >= ry
                        && my < ry + AutismPacketLoggerOverlay.this.put_I_6();
                     int fill = blocked ? UiColorPalette.load(hover) : UiColorPalette.init(c2s, i, hover);
                     ctx.fill(this.init_I_2 + 4, ry, this.init_I_2 + this.read_I - 8, ry + AutismPacketLoggerOverlay.this.put_I_6(), fill);
                     if (blocked) {
                        ctx.fill(this.init_I_2 + 4, ry, this.init_I_2 + 6, ry + AutismPacketLoggerOverlay.this.put_I_6(), UiColorPalette.fill());
                     }

                     int color = UiColorPalette.init(c2s, i);
                     String name = PacketNameResolver.init_Str_Class(cls);
                     String label = autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.init_Font,
                        name,
                        Math.max(1, this.read_I - 24),
                        AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                        color
                     );
                     AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                        ctx, label, autismclient.gui.f.b.l.read, color, this.init_I_2 + 9, AutismPacketLoggerOverlay.this.init_I_I_8(ry)
                     );
                  }

                  autismclient.gui.ui.UiClipStack.init_HashUtil().init(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
                     contentH, listH, this.init_I_2 + this.read_I - 5, listTop, 3, listH, drawScroll
                  );
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(ctx, metrics, metrics.init(mx, my), this.write_Z);
               } finally {
                  this.init_V_GuiGraphicsExtract_Z(ctx, clip);
                  this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, false);
               }
            }
         }
      }

      private void write_V_19() {
         String query = this.init_k.load_Str() == null ? "" : this.init_k.load_Str().trim().toLowerCase(Locale.ROOT);
         this.load_List.clear();

         for (Class<? extends Packet<?>> cls : this.init_List) {
            if (!this.update_Z || AutismPacketLoggerOverlay.this.load_Z_Class_Str(cls, PacketNameResolver.init_Str_Class(cls))) {
               if (!query.isEmpty()) {
                  String hay = (PacketNameResolver.init_Str_Class(cls) + " " + cls.getSimpleName()).toLowerCase(Locale.ROOT);
                  if (!hay.contains(query)) {
                     continue;
                  }
               }

               this.load_List.add(cls);
            }
         }

         this.flush_I = Math.min(this.flush_I, Math.max(0, this.load_List.size() * AutismPacketLoggerOverlay.this.put_I_6()));
      }

      @Override
      public boolean load_Z() {
         return true;
      }

      @Override
      public boolean init_Z_D_3(double mx, double my, int button) {
         if (!this.init_Z) {
            return false;
         }

         OverlayBounds bounds = this.init_cH();
         if (button == 0 && this.write(mx, my, bounds)) {
            this.init_V_Z(false);
            return true;
         }

         if (button == 0 && this.load_Z_D_D(mx, my)) {
            this.sync_Z = true;
            this.init_D = mx - this.init_I_2;
            this.load_D = my - this.load_I;
            return true;
         }

         if (this.load_Z) {
            return true;
         }

         int x = this.init_I_2 + 6;
         if (this.init_k.init_Z_D_2(mx, my, button)) {
            return true;
         }

         int y = this.get_I_16();
         if (button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.add_I_13()) {
            int clearX = x + this.push_I_20() + 2;
            int resetX = clearX + this.pop_I_20() + 2;
            if (mx >= x && mx < x + this.push_I_20()) {
               this.update_Z = !this.update_Z;
               this.flush_I = 0;
               this.write_V_19();
               return true;
            }

            if (mx >= clearX && mx < clearX + this.pop_I_20()) {
               AutismPacketLoggerOverlay.this.add_V_7();
               this.write_V_19();
               return true;
            }

            if (mx >= resetX && mx < resetX + this.tick_I_18()) {
               AutismPacketLoggerOverlay.this.put_V_6();
               this.write_V_19();
               return true;
            }
         }

         int listTop = this.set_I_16();
         int listH = Math.max(20, this.load_I + this.write - listTop - 6);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            this.load_List.size() * AutismPacketLoggerOverlay.this.put_I_6(), listH, this.init_I_2 + this.read_I - 5, listTop, 3, listH, this.flush_I
         );
         if (button == 0 && metrics.init_Z() && metrics.init((int)mx, (int)my)) {
            this.write_Z = true;
            this.close_I = (int)my - metrics.update();
            return true;
         }

         if (button == 0 && my >= listTop) {
            int index = (int)((my - listTop + this.flush_I) / AutismPacketLoggerOverlay.this.put_I_6());
            if (index >= 0 && index < this.load_List.size()) {
               Class<? extends Packet<?>> cls = this.load_List.get(index);
               String key = this.init(cls);
               if (AutismPacketLoggerOverlay.this.load_Z_Class_Str(cls, PacketNameResolver.init_Str_Class(cls))) {
                  AutismPacketLoggerOverlay.this.init_V_Class_Str(cls, PacketNameResolver.init_Str_Class(cls));
               } else {
                  AutismPacketLoggerOverlay.this.load(key);
               }

               if (this.update_Z) {
                  this.write_V_19();
               }

               return true;
            }
         }

         return this.init_Z_D_D(mx, my);
      }

      @Override
      public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
         if (this.sync_Z && button == 0) {
            this.init_V_cH(new OverlayBounds((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z));
            return true;
         } else if (this.write_Z && button == 0) {
            int listTop = this.set_I_16();
            int listH = Math.max(20, this.load_I + this.write - listTop - 6);
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
               this.load_List.size() * AutismPacketLoggerOverlay.this.put_I_6(), listH, this.init_I_2 + this.read_I - 5, listTop, 3, listH, this.flush_I
            );
            this.flush_I = this.init_I_I_3(
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(metrics, (int)my, this.close_I), AutismPacketLoggerOverlay.this.put_I_6(), metrics.flush()
            );
            return true;
         } else {
            return this.init_k.init(mx, my, button, dx, dy);
         }
      }

      @Override
      public boolean load_Z_D_3(double mx, double my, int button) {
         if (button == 0 && this.sync_Z) {
            this.sync_Z = false;
            this.a__V();
            return true;
         } else if (button == 0 && this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.init_k.load(mx, my, button);
         }
      }

      @Override
      public boolean init_Z_D_2(double mx, double my, double amount) {
         if (this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my)) {
            int listTop = this.set_I_16();
            int listH = Math.max(20, this.load_I + this.write - listTop - 6);
            int maxScroll = Math.max(0, this.load_List.size() * AutismPacketLoggerOverlay.this.put_I_6() - listH);
            this.flush_I = this.init_I_I_3(
               this.flush_I - (int)Math.signum(amount) * AutismPacketLoggerOverlay.this.put_I_6(), AutismPacketLoggerOverlay.this.put_I_6(), maxScroll
            );
            this.init_CacheEntry.load(this.flush_I, maxScroll);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
         return this.init_Z && this.init_k.init_Z() && this.init_k.init_Z_KeyEvent(Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers));
      }

      @Override
      public boolean init_Z_C_I(char chr, int modifiers) {
         return this.init_Z && this.init_k.init_Z() && this.init_k.init_Z_CharacterEvent(new CharacterEvent(chr));
      }

      @Override
      public boolean write_Z() {
         return this.init_Z && this.init_k.init_Z();
      }

      @Override
      public void push_V() {
         this.init_k.push(false);
      }
   }

   private record Inner_BufferHelper(String[] init_StrArr, int[] init_IArr, int init_I) {
   }

   static class Inner_CachePool {
      AutismPacketLoggerOverlay.Inner_RenderState init_AutismPacketLogger_2;
      AutismPacketLoggerOverlay.Inner_JsonUtil init_AutismPacketLogger;
      String init_Str;
      int init_I;
      String load;
      Class<?> init_Class;
      AutismPayloadSupport.PayloadEntry init_AutismPayloadSuppo;
   }

   private static final class Inner_ConfigStore {
      final AutismPacketLoggerOverlay.Inner_TaskQueue init_AutismPacketLogger;
      final String init_Str;
      final int init_I;
      final AutismConfig.PayloadChannelFilterRule init_AutismConfig_Paylo;
      final int load_I;
      final AutismConfig.PayloadChannelRegistrationRule init_AutismConfig_Paylo_2;
      final String load_Str;
      final AutismPacketLoggerOverlay.Inner_UiHelper init_AutismPacketLogger_2;
      final PayloadChannelFilterManager.Inner_CachePool init_bB_CachePool;

      private Inner_ConfigStore(
         AutismPacketLoggerOverlay.Inner_TaskQueue kind,
         String header,
         int ruleIndex,
         AutismConfig.PayloadChannelFilterRule rule,
         int registrationIndex,
         AutismConfig.PayloadChannelRegistrationRule registration,
         String learnedChannel,
         AutismPacketLoggerOverlay.Inner_UiHelper family,
         PayloadChannelFilterManager.Inner_CachePool preset
      ) {
         this.init_AutismPacketLogger = kind;
         this.init_Str = header;
         this.init_I = ruleIndex;
         this.init_AutismConfig_Paylo = rule;
         this.load_I = registrationIndex;
         this.init_AutismConfig_Paylo_2 = registration;
         this.load_Str = learnedChannel;
         this.init_AutismPacketLogger_2 = family;
         this.init_bB_CachePool = preset;
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore init(String label) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.HEADER, label, -1, null, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore load(String label) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.EMPTY, label, -1, null, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore init(int index, AutismConfig.PayloadChannelRegistrationRule rule) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.REGISTRATION, null, -1, null, index, rule, null, null, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore read(String channel) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.LEARNED, null, -1, null, -1, null, channel, null, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore init(int index, AutismConfig.PayloadChannelFilterRule rule) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.ACTIVE, null, index, rule, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore init(AutismPacketLoggerOverlay.Inner_UiHelper family) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.FAMILY, null, -1, null, -1, null, null, family, null);
      }

      static AutismPacketLoggerOverlay.Inner_ConfigStore init(PayloadChannelFilterManager.Inner_CachePool preset) {
         return new AutismPacketLoggerOverlay.Inner_ConfigStore(AutismPacketLoggerOverlay.Inner_TaskQueue.PRESET, null, -1, null, -1, null, null, null, preset);
      }
   }

   public enum Inner_DataUtil {
      ALL("All"),
      INVENTORY("INV"),
      MOVEMENT("Move"),
      PAYLOAD("Payload");

      public final String init_Str;

      Inner_DataUtil(String l) {
         this.init_Str = l;
      }
   }

   public static class Inner_JsonUtil {
      public final long init_J = AutismPacketLoggerOverlay.read_J++;
      public final long load_J;
      public final int init_I;
      public final String init_Str;
      public final String load_Str;
      private volatile String sync;
      public final String read_Str;
      public final Class<?> init_Class;
      public final Packet<?> init_Packet;
      public final boolean init_Z;
      public final boolean load_Z;
      public final boolean read_Z;
      public final String write;
      public final String update;
      public final AutismPayloadSupport.PayloadEntry init_AutismPayloadSuppo;
      public final PacketStateDeltaTracker.Inner_DataUtil init_bq_DataUtil;

      public Inner_JsonUtil(
         long ts,
         int tick,
         String dir,
         String name,
         Class<?> cls,
         Packet<?> ref,
         boolean inv,
         boolean mov,
         boolean payload,
         String capturedBlockState,
         String capturedScreen,
         AutismPayloadSupport.PayloadEntry payloadSnapshot,
         PacketStateDeltaTracker.Inner_DataUtil packetContext
      ) {
         this.load_J = ts;
         this.init_I = tick;
         this.init_Str = dir;
         this.load_Str = name;
         this.init_AutismPayloadSuppo = payloadSnapshot;
         this.read_Str = dir + ":" + name;
         this.init_Class = cls;
         this.init_Packet = ref;
         this.init_Z = inv;
         this.load_Z = mov;
         this.read_Z = payload;
         this.write = capturedBlockState;
         this.update = capturedScreen;
         this.init_bq_DataUtil = packetContext == null ? PacketStateDeltaTracker.init_bq_DataUtil : packetContext;
      }

      public String init() {
         String key = this.sync;
         if (key == null) {
            String payloadSearch = this.init_AutismPayloadSuppo == null
               ? ""
               : " " + this.init_AutismPayloadSuppo.load_Str() + " " + this.init_AutismPayloadSuppo.read();
            key = (this.load_Str == null ? "" : this.load_Str).concat(payloadSearch).toLowerCase(Locale.ROOT);
            this.sync = key;
         }

         return key;
      }
   }

   private record Inner_NetHelper(String init_Str, String load) {
      private static final AutismPacketLoggerOverlay.Inner_NetHelper init_AutismPacketLogger = new AutismPacketLoggerOverlay.Inner_NetHelper(null, null);
   }

   enum Inner_RenderState {
      ENTRY,
      GROUP;
   }

   private static final class Inner_StateCache {
      final String init_Str;
      final Map<String, AutismPacketLoggerOverlay.Inner_UiHelper> init_Map = new LinkedHashMap<>();
      final List<PayloadChannelFilterManager.Inner_CachePool> init_List = new ArrayList<>();
      List<AutismPacketLoggerOverlay.Inner_UiHelper> load = List.of();
      List<PayloadChannelFilterManager.Inner_CachePool> read = List.of();

      Inner_StateCache(String group) {
         this.init_Str = group;
      }

      AutismPacketLoggerOverlay.Inner_UiHelper init(AutismPacketLoggerOverlay.Inner_SyncManager familyKey) {
         return this.init_Map.computeIfAbsent(familyKey.init_Str(), unused -> new AutismPacketLoggerOverlay.Inner_UiHelper(familyKey));
      }
   }

   private record Inner_StreamUtil(String[] init_StrArr, int[] init_IArr, int init_I) {
   }

   private record Inner_SyncManager(String init, String load, String read) {
      String init_Str() {
         return this.init + "|" + this.load;
      }

      public String load() {
         return this.init;
      }

      public String read() {
         return this.load;
      }

      public String write() {
         return this.read;
      }
   }

   private enum Inner_TaskQueue {
      HEADER,
      REGISTRATION,
      LEARNED,
      ACTIVE,
      FAMILY,
      PRESET,
      EMPTY;
   }

   private static final class Inner_UiHelper {
      final String init_Str;
      final String load_Str;
      final String read;
      final List<PayloadChannelFilterManager.Inner_CachePool> init_List = new ArrayList<>();
      List<PayloadChannelFilterManager.Inner_CachePool> load_List = List.of();
      private Set<String> init_Set;

      Inner_UiHelper(AutismPacketLoggerOverlay.Inner_SyncManager familyKey) {
         this.init_Str = familyKey.load();
         this.load_Str = familyKey.read();
         this.read = familyKey.write();
      }

      String init_Str() {
         return this.init_Str + "|" + this.load_Str;
      }

      int init_I() {
         return this.init_Set().size();
      }

      Set<String> init_Set() {
         if (this.init_Set != null) {
            return this.init_Set;
         }

         LinkedHashSet<String> channels = new LinkedHashSet<>();

         for (PayloadChannelFilterManager.Inner_CachePool preset : this.init_List) {
            if (preset != null) {
               String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
               if (PayloadRegistrationManager.load_Z_Str(pattern)) {
                  channels.add(pattern);
               }
            }
         }

         this.init_Set = Collections.unmodifiableSet(channels);
         return this.init_Set;
      }

      List<PayloadChannelFilterManager.Inner_CachePool> init_List() {
         return this.load_List != null && !this.load_List.isEmpty() ? this.load_List : this.init_List;
      }
   }

   private final class PayloadChannelListenerOverlay extends DraggableOverlayWindow {
      private final ThemedTextInputBox init_k = new ThemedTextInputBox(AutismPacketLoggerOverlay.init_Minecraft, AutismPacketLoggerOverlay.this.init_Font, 0, 0, 120, 16, false);
      private final ThemedTextInputBox load_k = new ThemedTextInputBox(AutismPacketLoggerOverlay.init_Minecraft, AutismPacketLoggerOverlay.this.init_Font, 0, 0, 112, 16, false);
      private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private final List<AutismPacketLoggerOverlay.Inner_ConfigStore> init_List = new ArrayList<>();
      private int flush_I;
      private boolean write_Z;
      private int close_I;
      private boolean update_Z;
      private double init_D;
      private double load_D;
      private boolean sync_Z;
      private boolean flush_Z;
      private final Set<String> init_Set = new HashSet<>();
      private int open_I = -1;
      private int poll = -1;
      private Map<String, AutismPacketLoggerOverlay.Inner_StateCache> init_Map;
      private List<AutismPacketLoggerOverlay.Inner_StateCache> load_List;
      private final Set<String> load_Set = new HashSet<>();
      private final Set<String> read_Set = new HashSet<>();
      private final Set<String> write_Set = new LinkedHashSet<>();
      private final Map<String, Integer> load_Map = new HashMap<>();
      private final Map<String, Integer> read_Map = new HashMap<>();
      private final Map<String, PayloadChannelFilterManager.Inner_CachePool> write_Map = new HashMap<>();
      private final Map<PayloadChannelFilterManager.Inner_CachePool, String> update_Map = new IdentityHashMap<>();
      private final Map<String, PayloadSubscriptionManager.Inner_CachePool> sync_Map = new HashMap<>();
      private int push;
      private String init_Str_2 = "";
      private int pop = 96;
      private boolean close_Z;
      private long init_J;
      private boolean open_Z;
      private static final int tick = 14;
      private static final int run = 2;
      private static final int get = 6;
      private static final int set = 2;
      private static final int put = 7;
      private static final int add = 58;
      private static final int clear = 36;
      private static final int reset = 30;
      private static final int start = 12;
      private static final int stop = 14;
      private static final int next = 88;
      private static final int prev = 62;
      private static final int valid = 56;
      private static final int size = 62;
      private static final int data = 46;
      private static final int value = 50;
      private static final int cache = 50;
      private static final int state = 5000;
      private static final int queue = 14;
      private static final int buffer = 10;
      private static final int entry = 2;

      PayloadChannelListenerOverlay() {
         super("PacketLoggerPayloadChannels", 492, 336);
         this.init_I_2 = 270;
         this.load_I = 58;
         this.init_Z = false;
         this.init_k.init_V_Component(Component.literal("Search presets/channels..."));
         this.init_k.load(120);
         this.init_k.init_V_Consumer(value -> this.open_Z = true);
         this.load_k.init_V_Component(Component.literal("channel or wildcard: shopgui:main / oraxen:*"));
         this.load_k.load(96);
      }

      private int push_I_21() {
         return AutismPacketLoggerOverlay.this.add_I_13() + 2;
      }

      private int pop_I_21() {
         return this.load_I + 14 + 4;
      }

      private int tick_I_19() {
         return this.pop_I_21() + this.push_I_21();
      }

      private int run_I_19() {
         return this.tick_I_19() + this.push_I_21();
      }

      private int get_I_17() {
         return this.run_I_19() + this.push_I_21();
      }

      private int set_I_17() {
         return this.get_I_17();
      }

      private int put_I_7() {
         return this.get_I_17() + this.push_I_21() + 2;
      }

      private int add_I_14() {
         return this.init_I_2 + 6;
      }

      private int clear_I_7() {
         return this.init_I_2 + this.read_I - 6 - 7;
      }

      private int reset_I_6() {
         return Math.max(20, this.clear_I_7() - this.add_I_14());
      }

      private int start_I_13() {
         return this.add_I_14() - 2;
      }

      private int stop_I_6() {
         return this.init_I_2 + this.read_I - 5;
      }

      private int next_I_6() {
         int raw = Math.max(14, this.load_I + this.write - this.put_I_7() - 8);
         return Math.max(14, this.sync_I(raw, 14));
      }

      private boolean poll_Z_14() {
         return this.open_I >= 0 || this.poll >= 0;
      }

      private int prev_I_5() {
         int buttons = this.valid() + (this.poll_Z_14() ? 2 + this.size() : 0);
         return Math.max(120, this.read_I - 12 - buttons - 2);
      }

      private int valid() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6(this.poll_Z_14() ? "Save" : "Add");
      }

      private int size() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6("Cancel");
      }

      private int data() {
         String label = PayloadSubscriptionManager.init_Z() ? "Registration : On" : "Registration : Off";
         return AutismPacketLoggerOverlay.this.init_I_Str_6(label);
      }

      private String load_Str_10() {
         int remaining = this.state();
         return remaining <= 0 ? "Accept" : "Accept " + remaining + "s";
      }

      private int value() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6("Cancel");
      }

      private int cache() {
         return AutismPacketLoggerOverlay.this.init_I_Str_6(this.load_Str_10());
      }

      private AutismPacketLoggerOverlay.Inner_StreamUtil init_AutismPacketLogger_4() {
         String[] labels = new String[]{"Recommended", "Known On", "Capture", this.sync_Z ? "Enabled" : "All", "Apply", "Revert", "All Off"};
         int[] widths = new int[labels.length];
         int total = 0;

         for (int i = 0; i < labels.length; i++) {
            widths[i] = AutismPacketLoggerOverlay.this.init_I_Str_6(labels[i]);
            total += widths[i];
         }

         return total + 2 * (labels.length - 1) <= this.read_I - 12
            ? new AutismPacketLoggerOverlay.Inner_StreamUtil(labels, widths, 2)
            : new AutismPacketLoggerOverlay.Inner_StreamUtil(labels, new int[]{88, 62, 56, 62, 46, 50, 50}, 2);
      }

      private void write_V_20() {
         this.open_I = -1;
         this.poll = -1;
         this.load_k.init_V_Str("");
         this.load_k.init_V_Component(Component.literal("exact channel or highlight wildcard"));
         this.load_k.push(false);
      }

      private void update_V_17() {
         AutismPacketLoggerOverlay.this.init_bB.init_V_Z(false);
         AutismPacketLoggerOverlay.this.init_bC.init_V_Z(false);
      }

      private void sync_V_15() {
         this.flush_Z = true;
         AutismPacketLoggerOverlay.this.pop_Z = true;
      }

      private void flush_V_14() {
         if (this.flush_Z) {
            this.update_V_17();
            AutismConfig.getGlobal().save();
            this.flush_Z = false;
         }
      }

      @Override
      public void init_V_Z(boolean v) {
         if (!v) {
            this.flush_V_14();
            this.write_V_20();
            this.close_Z = false;
         }

         super.init_V_Z(v);
      }

      @Override
      public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
         if (this.init_Z || this.a__Z()) {
            if (this.open_Z) {
               this.open_Z = false;
               this.put_V_7();
            }

            OverlayBounds bounds = this.init_cH_cX_cH(this, new OverlayBounds(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z));
            this.init_I_2 = bounds.init_I;
            this.load_I = bounds.load_I;
            this.init_V_GuiGraphicsExtract_6(ctx, mx, my, bounds, "Payload Channels", this.load_Z, false);
            boolean clip = this.init_Z_GuiGraphicsExtract(ctx, bounds, this.load_Z);
            if (!clip) {
               this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, false);
            } else {
               try {
                  int x = this.init_I_2 + 6;
                  int y = this.pop_I_21();
                  this.init_k.read(x);
                  this.init_k.write(y);
                  this.init_k.update(Math.max(80, this.read_I - 12));
                  this.init_k.sync(AutismPacketLoggerOverlay.this.add_I_13());
                  this.init_k.init_V(ctx, mx, my, delta);
                  y = this.tick_I_19();
                  int customW = this.prev_I_5();
                  this.load_k.read(x);
                  this.load_k.write(y);
                  this.load_k.update(customW);
                  this.load_k.sync(AutismPacketLoggerOverlay.this.add_I_13());
                  this.load_k.init_V(ctx, mx, my, delta);
                  int addW = this.valid();
                  int addX = x + customW + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     addX,
                     y,
                     addW,
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     this.poll_Z_14() ? "Save" : "Add",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  if (this.poll_Z_14()) {
                     int cancelX = addX + addW + 2;
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        ctx,
                        AutismPacketLoggerOverlay.this.init_Font,
                        cancelX,
                        y,
                        this.size(),
                        AutismPacketLoggerOverlay.this.add_I_13(),
                        "Cancel",
                        autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                        true,
                        mx,
                        my
                     );
                  }

                  y = this.run_I_19();
                  AutismPacketLoggerOverlay.Inner_StreamUtil controls = this.init_AutismPacketLogger_4();
                  int cx = x;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[0],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[0],
                     AutismPacketLoggerOverlay.this.init_bB.read_Z()
                        ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST
                        : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[0] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[1],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[1],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[1] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[2],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[2],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[2] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[3],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[3],
                     this.sync_Z ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[3] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[4],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[4],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[4] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[5],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[5],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  cx += controls.init_IArr[5] + controls.init_I;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     cx,
                     y,
                     controls.init_IArr[6],
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     controls.init_StrArr[6],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  PayloadSubscriptionManager.Inner_BufferHelper subscriptionStatus = PayloadSubscriptionManager.init_bD_BufferHelper();
                  String sub = subscriptionStatus == null ? "Registered 0/96" : subscriptionStatus.init_Str();
                  if (subscriptionStatus != null && !subscriptionStatus.load_Z() && subscriptionStatus.init_I() > 0 && !subscriptionStatus.init_Z()) {
                     sub = sub + " pending";
                  }

                  boolean registrationUnlocked = PayloadSubscriptionManager.init_Z();
                  int lockW = this.data();
                  int lockX = this.init_I_2 + this.read_I - 8 - lockW;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.init_Font,
                     lockX,
                     this.set_I_17(),
                     lockW,
                     AutismPacketLoggerOverlay.this.add_I_13(),
                     registrationUnlocked ? "Registration : On" : "Registration : Off",
                     registrationUnlocked ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  String count = this.push + " highlight  " + sub + this.init_Str_2;
                  int countX = x;
                  int countMaxW = Math.max(1, lockX - countX - 5);
                  AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                     ctx,
                     autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.init_Font,
                        count,
                        countMaxW,
                        AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                        UiColorPalette.parse()
                     ),
                     autismclient.gui.f.b.l.write,
                     UiColorPalette.parse(),
                     countX,
                     UiLayoutMath.init(
                        this.get_I_17(),
                        AutismPacketLoggerOverlay.this.add_I_13(),
                        AutismPacketLoggerOverlay.this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write),
                        AutismPacketLoggerOverlay.this.init_CharEncoder.flush()
                     )
                  );
                  int listTop = this.put_I_7();
                  int listH = this.next_I_6();
                  int contentH = this.init_List.size() * 14;
                  int maxScroll = Math.max(0, contentH - listH);
                  this.flush_I = this.init_I_I_3(this.flush_I, 14, maxScroll);
                  this.init_CacheEntry.init_V_I_I(this.flush_I, maxScroll);
                  int drawScroll = this.init_CacheEntry.init_I_F_I(delta, maxScroll);
                  autismclient.gui.f.PacketUtil.init(
                     ctx,
                     autismclient.gui.f.NetHelper.init(this.start_I_13(), listTop - 2, Math.max(1, this.stop_I_6() - this.start_I_13()), listH + 4),
                     UiColorPalette.count(),
                     UiColorPalette.c()
                  );
                  autismclient.gui.ui.UiClipStack.init_HashUtil().init(ctx, autismclient.gui.f.NetHelper.init(this.add_I_14(), listTop, this.reset_I_6(), listH));
                  int base = listTop - drawScroll;
                  int firstVisible = Math.max(0, drawScroll / 14);
                  int lastVisible = Math.min(this.init_List.size() - 1, (drawScroll + listH - 1) / 14);

                  for (int i = firstVisible; i <= lastVisible; i++) {
                     int ry = base + i * 14;
                     this.init(ctx, this.init_List.get(i), ry, mx, my, i);
                  }

                  autismclient.gui.ui.UiClipStack.init_HashUtil().init(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
                     contentH, listH, this.init_I_2 + this.read_I - 7, listTop, 3, listH, drawScroll
                  );
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(ctx, metrics, metrics.init(mx, my), this.write_Z);
                  if (this.close_Z) {
                     this.init_V_GuiGraphicsExtract_6(ctx, mx, my);
                  }
               } finally {
                  this.init_V_GuiGraphicsExtract_Z(ctx, clip);
                  this.init_V_GuiGraphicsExtract_14(ctx, bounds, this.load_Z, false);
               }
            }
         }
      }

      private void init_V_GuiGraphicsExtract_6(GuiGraphicsExtractor ctx, int mx, int my) {
         int bodyTop = this.load_I + 14;
         ctx.fill(this.init_I_2 + 4, bodyTop + 2, this.init_I_2 + this.read_I - 4, this.load_I + this.write - 5, -872086265);
         AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.Inner_DataUtil bounds = this.init_AutismPacketLogger_3();
         int x = bounds.init;
         int y = bounds.load;
         int w = bounds.read;
         int h = bounds.write;
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_3(
            ctx,
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            -15594225,
            ThemePaletteManager.init_I_I_ct_DataUtil(-42149, ThemePaletteManager.ColorRole.OUTLINE),
            ThemePaletteManager.init_I_I_ct_DataUtil(-42149, ThemePaletteManager.ColorRole.OUTLINE)
         );
         this.init(ctx, "Payload Registration Warning", autismclient.gui.f.b.l.load, ThemePaletteManager.init_I_I_ct_DataUtil(-19276, ThemePaletteManager.ColorRole.DANGER), x, w, y + 10);
         this.init(ctx, "Servers can see every registered channel.", autismclient.gui.f.b.l.read, UiColorPalette.scan(), x, w, y + 30);
         this.init(ctx, "This can identify this client or selected plugins.", autismclient.gui.f.b.l.read, UiColorPalette.scan(), x, w, y + 43);
         this.init(ctx, "Passive logging and decoding still work while locked.", autismclient.gui.f.b.l.write, UiColorPalette.parse(), x, w, y + 60);
         int cancelW = this.value();
         int acceptW = this.cache();
         int buttonY = bounds.update;
         int buttonsW = cancelW + 6 + acceptW;
         int cancelX = x + (w - buttonsW) / 2;
         int acceptX = cancelX + cancelW + 6;
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            ctx,
            AutismPacketLoggerOverlay.this.init_Font,
            cancelX,
            buttonY,
            cancelW,
            AutismPacketLoggerOverlay.this.add_I_13(),
            "Cancel",
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
            true,
            mx,
            my
         );
         boolean ready = this.state() <= 0;
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            ctx,
            AutismPacketLoggerOverlay.this.init_Font,
            acceptX,
            buttonY,
            acceptW,
            AutismPacketLoggerOverlay.this.add_I_13(),
            this.load_Str_10(),
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
            ready,
            mx,
            my
         );
      }

      private int state() {
         long elapsed = Math.max(0L, System.currentTimeMillis() - this.init_J);
         long remaining = Math.max(0L, 5000L - elapsed);
         return (int)Math.ceil(remaining / 1000.0);
      }

      private boolean read(double mx, double my, int button) {
         if (button != 0) {
            return true;
         }

         AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.Inner_DataUtil bounds = this.init_AutismPacketLogger_3();
         int cancelW = this.value();
         int acceptW = this.cache();
         int buttonsW = cancelW + 6 + acceptW;
         int cancelX = bounds.init + (bounds.read - buttonsW) / 2;
         int acceptX = cancelX + cancelW + 6;
         int buttonY = bounds.update;
         if (my >= buttonY && my < buttonY + AutismPacketLoggerOverlay.this.add_I_13() && mx >= cancelX && mx < cancelX + cancelW) {
            this.close_Z = false;
            return true;
         }

         if (!(my >= buttonY) || !(my < buttonY + AutismPacketLoggerOverlay.this.add_I_13()) || !(mx >= acceptX) || !(mx < acceptX + acceptW)) {
            return true;
         }

         if (this.state() > 0) {
            return true;
         }

         this.close_Z = false;
         PayloadSubscriptionManager.read();
         this.put_V_7();
         bi.init("Payload registration unlocked. Press Apply.");
         return true;
      }

      private AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.Inner_DataUtil init_AutismPacketLogger_3() {
         int bodyTop = this.load_I + 14;
         int contentW = 0;
         contentW = Math.max(
            contentW, this.init_I_Str_3("Payload Registration Warning", autismclient.gui.f.b.l.load, ThemePaletteManager.init_I_I_ct_DataUtil(-19276, ThemePaletteManager.ColorRole.DANGER))
         );
         contentW = Math.max(contentW, this.init_I_Str_3("Servers can see every registered channel.", autismclient.gui.f.b.l.read, UiColorPalette.scan()));
         contentW = Math.max(contentW, this.init_I_Str_3("This can identify this client or selected plugins.", autismclient.gui.f.b.l.read, UiColorPalette.scan()));
         contentW = Math.max(contentW, this.init_I_Str_3("Passive logging and decoding still work while locked.", autismclient.gui.f.b.l.write, UiColorPalette.parse()));
         int buttonsW = this.value() + 6 + this.cache();
         int maxW = Math.max(180, this.read_I - 24);
         int w = Math.min(maxW, Math.max(buttonsW + 28, contentW + 28));
         int h = 75 + AutismPacketLoggerOverlay.this.add_I_13() + 10;
         int x = this.init_I_2 + (this.read_I - w) / 2;
         int y = bodyTop + Math.max(8, (this.write - 14 - h) / 2);
         return new AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.Inner_DataUtil(x, y, w, h, y + h - 10 - AutismPacketLoggerOverlay.this.add_I_13());
      }

      private int init_I_Str_3(String text, autismclient.gui.f.b.l tone, int color) {
         return autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.init_Font, text, AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(tone), color
         );
      }

      private void init(GuiGraphicsExtractor ctx, String text, autismclient.gui.f.b.l tone, int color, int x, int w, int y) {
         String fitted = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.init_Font, text, Math.max(1, w - 28), AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(tone), color
         );
         int textW = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.init_Font, fitted, AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(tone), color
         );
         AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(ctx, fitted, tone, color, x + Math.max(0, (w - textW) / 2), y);
      }

      private void close_V_13() {
         this.close_Z = true;
         this.init_J = System.currentTimeMillis();
      }

      private void open_V_11() {
         this.flush_V_14();
         PayloadSubscriptionManager.init_V(AutismPacketLoggerOverlay.init_Minecraft);
         this.put_V_7();
         bi.read("Payload registration locked.");
      }

      private boolean push_Z_11() {
         if (PayloadSubscriptionManager.init_Z()) {
            return true;
         }

         this.close_V_13();
         return false;
      }

      private void init(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.Inner_ConfigStore row, int y, int mx, int my, int index) {
         int left = this.add_I_14();
         int right = this.clear_I_7();
         int width = Math.max(1, right - left);
         int x = left + 4;
         if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.HEADER) {
            boolean groupHeader = this.load_Map.getOrDefault(this.init_Str_2(row.init_Str), 0) > 0;
            boolean groupOn = groupHeader && this.load_Z_2(row.init_Str);
            ctx.fill(left, y, right, y + 14, UiColorPalette.clear());
            autismclient.gui.ui.widgets.PanelSurfaceHelper.init(ctx, left + 2, y + 14 - 1, Math.max(1, width - 4));
            int statusW = groupHeader ? 30 : 0;
            int statusX = right - statusW - 4;
            int channelX = groupHeader ? statusX - 58 - 3 : right;
            int studyX = groupHeader ? channelX - 36 - 3 : right;
            int maxHeaderW = groupHeader ? Math.max(1, studyX - x - 6) : Math.max(1, width - 10);
            String header = autismclient.gui.f.b.k.init(
               AutismPacketLoggerOverlay.this.init_Font,
               row.init_Str,
               maxHeaderW,
               AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
               UiColorPalette.parse()
            );
            AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
               ctx, header, autismclient.gui.f.b.l.write, UiColorPalette.parse(), x, this.init_I_I_l(y, autismclient.gui.f.b.l.write)
            );
            if (groupHeader) {
               this.init(ctx, studyX, y, mx, my, "Study");
               this.load(ctx, row.init_Str, channelX, y);
               this.init(ctx, statusX, y + 1, statusW, 12, groupOn ? "ON" : "OFF", groupOn);
            }
         } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.EMPTY) {
            ctx.fill(left, y, right, y + 14, UiColorPalette.flags());
            AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
               ctx,
               autismclient.gui.f.b.k.init(
                  AutismPacketLoggerOverlay.this.init_Font,
                  row.init_Str == null ? "" : row.init_Str,
                  Math.max(1, width - 12),
                  AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                  UiColorPalette.bind()
               ),
               autismclient.gui.f.b.l.write,
               UiColorPalette.bind(),
               x + 4,
               this.init_I_I_l(y, autismclient.gui.f.b.l.write)
            );
         } else {
            boolean hover = mx >= left && mx < right && my >= y && my < y + 14;
            if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.FAMILY) {
               AutismPacketLoggerOverlay.Inner_UiHelper family = row.init_AutismPacketLogger_2;
               if (family != null) {
                  int total = family.init_I();
                  int enabled = this.init_I(family);
                  boolean allEnabled = total > 0 && enabled == total;
                  int fill = enabled > 0 ? UiColorPalette.init(hover) : (hover ? UiColorPalette.level() : UiColorPalette.flags());
                  ctx.fill(left, y, right, y + 14, fill);
                  if (enabled > 0) {
                     ctx.fill(left, y, left + 2, y + 14, UiColorPalette.drain());
                  }

                  int statusX = right - 30 - 4;
                  int channelX = statusX - 58 - 3;
                  int studyX = channelX - 36 - 3;
                  boolean familyExpanded = this.init_Set.contains(family.init_Str());
                  String text = family.read + "  " + enabled + "/" + total;
                  int textColor = enabled > 0 ? UiColorPalette.store() : UiColorPalette.scan();
                  autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 4, y + 2, 9, 9), familyExpanded, textColor);
                  AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                     ctx,
                     autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.init_Font,
                        text,
                        Math.max(1, studyX - (x + 15) - 5),
                        AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                        textColor
                     ),
                     autismclient.gui.f.b.l.read,
                     textColor,
                     x + 15,
                     this.init_I_I_l(y, autismclient.gui.f.b.l.read)
                  );
                  this.init(ctx, studyX, y, mx, my, "Study");
                  this.init_V_GuiGraphicsExtract_9(ctx, total + "ch", total > 0 ? -7346000 : UiColorPalette.bind(), channelX, y);
                  this.init(ctx, statusX, y + 1, 30, 12, allEnabled ? "ON" : "OFF", allEnabled);
               }
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.REGISTRATION) {
               AutismConfig.PayloadChannelRegistrationRule rule = row.init_AutismConfig_Paylo_2;
               boolean enabled = rule != null && rule.enabled;
               int fill = enabled ? UiColorPalette.init(hover) : (hover ? UiColorPalette.level() : UiColorPalette.flags());
               ctx.fill(left, y, right, y + 14, fill);
               if (enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.drain());
               }

               String label = rule != null && rule.label != null && !rule.label.isBlank() ? rule.label.trim() : "";
               String channel = rule == null ? "" : PayloadRegistrationManager.init_Str_Str(rule.channel);
               String text = !label.isBlank() && !label.equals(channel) ? label + "  " + channel : channel;
               boolean editable = this.init_Z(rule);
               int removeX = editable ? right - 14 : right;
               int editX = editable ? removeX - 14 : right;
               int statusRight = editable ? editX - 3 : right - 4;
               int statusX = statusRight - 30;
               int sourceW = 52;
               int sourceX = statusX - sourceW - 3;
               int maxTextW = Math.max(1, sourceX - (x + 4) - 5);
               int color = enabled ? UiColorPalette.store() : UiColorPalette.scan();
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.init_Font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     color
                  ),
                  autismclient.gui.f.b.l.read,
                  color,
                  x + 4,
                  this.init_I_I_l(y, autismclient.gui.f.b.l.read)
               );
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.init_Font,
                     PayloadRegistrationManager.load_Str_Str(rule == null ? "" : rule.source),
                     sourceW,
                     AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                     UiColorPalette.parse()
                  ),
                  autismclient.gui.f.b.l.write,
                  UiColorPalette.parse(),
                  sourceX,
                  this.init_I_I_l(y, autismclient.gui.f.b.l.write)
               );
               this.init(ctx, statusX, y + 1, 30, 12, enabled ? "ON" : "OFF", enabled);
               if (editable) {
                  boolean eu = mx >= editX && mx <= editX + 14 && my >= y && my < y + 14;
                  boolean hu = mx >= removeX && mx <= removeX + 14 && my >= y && my < y + 14;
                  AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                     ctx, "E", autismclient.gui.f.b.l.write, eu ? -7346000 : UiColorPalette.parse(), editX + 3, this.init_I_I_l(y, autismclient.gui.f.b.l.write)
                  );
                  autismclient.gui.f.PacketUtil.read(
                     ctx,
                     autismclient.gui.f.NetHelper.init(removeX + 3, y + 2, 9, 9),
                     hu ? ThemePaletteManager.init_I_I_ct_DataUtil(-39322, ThemePaletteManager.ColorRole.DANGER) : UiColorPalette.parse()
                  );
               }
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.LEARNED) {
               ctx.fill(left, y, right, y + 14, hover ? UiColorPalette.level() : UiColorPalette.flags());
               int statusX = right - 30 - 4;
               int sourceX = statusX - 56;
               int maxTextW = Math.max(1, sourceX - (x + 4) - 5);
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.init_Font,
                     row.load_Str == null ? "" : row.load_Str,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     UiColorPalette.scan()
                  ),
                  autismclient.gui.f.b.l.read,
                  UiColorPalette.scan(),
                  x + 4,
                  this.init_I_I_l(y, autismclient.gui.f.b.l.read)
               );
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx, "Learned", autismclient.gui.f.b.l.write, UiColorPalette.parse(), sourceX, this.init_I_I_l(y, autismclient.gui.f.b.l.write)
               );
               this.init(ctx, statusX, y + 1, 30, 12, "OFF", false);
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.ACTIVE) {
               AutismConfig.PayloadChannelFilterRule rule = row.init_AutismConfig_Paylo;
               int fill = rule.enabled ? UiColorPalette.init(hover) : (hover ? UiColorPalette.level() : UiColorPalette.flags());
               ctx.fill(left, y, right, y + 14, fill);
               if (rule.enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.drain());
               }

               String label = rule.label != null && !rule.label.isBlank() ? rule.label : rule.pattern;
               String text = label + "  " + rule.pattern;
               int color = rule.enabled ? UiColorPalette.store() : UiColorPalette.parse();
               int removeX = right - 14;
               int editX = removeX - 14;
               int statusRight = editX - 3;
               int statusW = 30;
               int statusX = statusRight - statusW;
               int channelX = statusX - 58 - 3;
               int studyX = channelX - 36 - 3;
               int maxTextW = Math.max(1, studyX - (x + 4) - 4);
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.init_Font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     color
                  ),
                  autismclient.gui.f.b.l.read,
                  color,
                  x + 4,
                  this.init_I_I_l(y, autismclient.gui.f.b.l.read)
               );
               this.init(ctx, studyX, y, mx, my, "Study");
               this.init_V_GuiGraphicsExtract_11(ctx, rule.pattern, channelX, y);
               this.init(ctx, statusX, y + 1, statusW, 12, rule.enabled ? "ON" : "OFF", rule.enabled);
               boolean eu = mx >= editX && mx <= editX + 14 && my >= y && my < y + 14;
               boolean hu = mx >= removeX && mx <= removeX + 14 && my >= y && my < y + 14;
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx, "E", autismclient.gui.f.b.l.write, eu ? -7346000 : UiColorPalette.parse(), editX + 3, this.init_I_I_l(y, autismclient.gui.f.b.l.write)
               );
               autismclient.gui.f.PacketUtil.read(
                  ctx, autismclient.gui.f.NetHelper.init(removeX + 3, y + 2, 9, 9), hu ? ThemePaletteManager.init_I_I_ct_DataUtil(-39322, ThemePaletteManager.ColorRole.DANGER) : UiColorPalette.parse()
               );
            } else {
               PayloadChannelFilterManager.Inner_CachePool preset = row.init_bB_CachePool;
               boolean enabled = this.init_Z(preset);
               int fill = enabled ? UiColorPalette.init(hover) : (hover ? UiColorPalette.level() : UiColorPalette.flags());
               ctx.fill(left, y, right, y + 14, fill);
               if (enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.drain());
               }

               String status = enabled ? "ON" : "OFF";
               int statusW = 30;
               int statusX = right - statusW - 4;
               String text = preset.load() + "  " + preset.read();
               int textColor = enabled ? UiColorPalette.store() : UiColorPalette.scan();
               int channelX = statusX - 58 - 3;
               int studyX = channelX - 36 - 3;
               int maxTextW = Math.max(1, studyX - (x + 4) - 5);
               AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.init_Font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     textColor
                  ),
                  autismclient.gui.f.b.l.read,
                  textColor,
                  x + 4,
                  this.init_I_I_l(y, autismclient.gui.f.b.l.read)
               );
               this.init(ctx, studyX, y, mx, my, this.init_Str(preset));
               this.init_V_GuiGraphicsExtract_10(ctx, preset, channelX, y);
               this.init(ctx, statusX, y + 1, statusW, 12, status, enabled);
            }
         }
      }

      private String init_Str(PayloadChannelFilterManager.Inner_CachePool preset) {
         return preset != null && preset.init_bB_BufferHelper().init_Z() ? "Use" : "Study";
      }

      private void init(GuiGraphicsExtractor ctx, int x, int y, int mx, int my, String label) {
         int h = 12;
         int py = y + (14 - h) / 2;
         boolean disabled = AutismPacketLoggerOverlay.this.update_Z && !"Use".equals(label);
         boolean hover = !disabled && mx >= x && mx < x + 36 && my >= py && my < py + h;
         if (disabled) {
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, py, 36, h), label
            );
         } else {
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.init_Font, mx, my),
               autismclient.gui.f.NetHelper.init(x, py, 36, h),
               label,
               false,
               hover
            );
         }
      }

      private boolean write(double mx, double my, int rowY) {
         int right = this.clear_I_7();
         int statusX = right - 30 - 4;
         int channelX = statusX - 58 - 3;
         int studyX = channelX - 36 - 3;
         int studyY = rowY + 1;
         return mx >= studyX && mx < studyX + 36 && my >= studyY && my < studyY + 12;
      }

      private boolean update(double mx, double my, int rowY) {
         int x = this.add_I_14() + 4;
         return mx >= x && mx < x + 16 && my >= rowY && my < rowY + 14;
      }

      private void init_V_GuiGraphicsExtract_11(GuiGraphicsExtractor ctx, String pattern, int x, int y) {
         PayloadSubscriptionManager.Inner_CachePool impact = this.init_bD_CachePool(pattern);
         boolean core = impact.init_I() <= 0 && this.init_Z_3(pattern);
         String label = impact.init_I() > 0 ? impact.init_Str() : (core ? "Core" : "Highlight");
         int color = impact.init_I() <= 0 && !core ? UiColorPalette.bind() : -7346000;
         this.init_V_GuiGraphicsExtract_9(ctx, label, color, x, y);
      }

      private void init_V_GuiGraphicsExtract_10(GuiGraphicsExtractor ctx, PayloadChannelFilterManager.Inner_CachePool preset, int x, int y) {
         if (preset == null) {
            this.init_V_GuiGraphicsExtract_11(ctx, "", x, y);
         } else {
            PayloadSubscriptionManager.Inner_CachePool impact = this.init_bD_CachePool(preset.read());
            String label;
            int color;
            if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
               label = impact.init_I() > 0 ? impact.init_Str() : "Exact";
               color = -7346000;
            } else if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.CORE) {
               label = preset.init_bB_BufferHelper().init_Str();
               color = -7346000;
            } else if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED) {
               label = preset.init_bB_BufferHelper().init_Str();
               color = UiColorPalette.entry();
            } else {
               label = preset.init_bB_BufferHelper().init_Str();
               color = UiColorPalette.bind();
            }

            this.init_V_GuiGraphicsExtract_9(ctx, label, color, x, y);
         }
      }

      private void init_V_GuiGraphicsExtract_9(GuiGraphicsExtractor ctx, String label, int color, int x, int y) {
         label = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.init_Font,
            label,
            Math.max(1, 56),
            AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
            color
         );
         AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
            ctx,
            label,
            autismclient.gui.f.b.l.write,
            color,
            x
               + Math.max(
                     0,
                     58
                        - autismclient.gui.f.b.k.init(
                           AutismPacketLoggerOverlay.this.init_Font,
                           label,
                           AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                           color
                        )
                  )
                  / 2,
            this.init_I_I_l(y, autismclient.gui.f.b.l.write)
         );
      }

      private boolean init_Z_3(String pattern) {
         String normalized = PayloadChannelFilterManager.init_Str_Str(pattern);
         boolean var10000;
         switch (normalized) {
            case "minecraft:register":
            case "minecraft:unregister":
            case "register":
            case "unregister":
            case "bungeecord":
               var10000 = true;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      }

      private void load(GuiGraphicsExtractor ctx, String group, int x, int y) {
         int count = this.init_I_4(group);
         String label = count > 0 ? count + "ch" : "Tpl";
         int color = count > 0 ? -7346000 : UiColorPalette.bind();
         AutismPacketLoggerOverlay.this.init_V_GuiGraphicsExtract_8(
            ctx,
            label,
            autismclient.gui.f.b.l.write,
            color,
            x
               + Math.max(
                     0,
                     58
                        - autismclient.gui.f.b.k.init(
                           AutismPacketLoggerOverlay.this.init_Font,
                           label,
                           AutismPacketLoggerOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                           color
                        )
                  )
                  / 2,
            this.init_I_I_l(y, autismclient.gui.f.b.l.write)
         );
      }

      private int init_I_4(String group) {
         return group == null ? 0 : this.read_Map.getOrDefault(this.init_Str_2(group), 0);
      }

      private boolean init_Z(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset == null) {
            return false;
         }

         String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
         return this.read_Set.contains(preset.init_Str()) || PayloadRegistrationManager.load_Z_Str(pattern) && this.write_Set.contains(pattern);
      }

      private int init_I(AutismPacketLoggerOverlay.Inner_UiHelper family) {
         if (family == null) {
            return 0;
         }

         int enabled = 0;

         for (String channel : family.init_Set()) {
            if (this.write_Set.contains(channel)) {
               enabled++;
            }
         }

         return enabled;
      }

      private boolean init_Z(AutismPacketLoggerOverlay.Inner_UiHelper family) {
         return family != null && family.init_I() > 0 && this.init_I(family) == family.init_I();
      }

      private boolean load_Z_2(String group) {
         if (group != null && !group.isBlank()) {
            String cleanGroup = this.init_Str_2(group);
            int total = this.load_Map.getOrDefault(cleanGroup, 0);
            if (total <= 0) {
               return false;
            }

            int enabled = 0;
            AutismPacketLoggerOverlay.Inner_StateCache category = this.init_Map_2().get(cleanGroup);
            if (category == null) {
               return false;
            }

            for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.init_Map.values()) {
               for (PayloadChannelFilterManager.Inner_CachePool preset : family.init_List) {
                  if (this.init_Z(preset)) {
                     enabled++;
                  }
               }
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.init_List) {
               if (this.init_Z(preset)) {
                  enabled++;
               }
            }

            return enabled == total;
         } else {
            return false;
         }
      }

      private void init(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean enabled) {
         autismclient.gui.ui.widgets.PillBadgeRenderer.init(
            autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.init_Font, -1, -1),
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            label,
            enabled,
            false
         );
      }

      private int init_I_I_l(int y, autismclient.gui.f.b.l tone) {
         return UiLayoutMath.init(y, 14, AutismPacketLoggerOverlay.this.init_CharEncoder.init_I_l(tone), AutismPacketLoggerOverlay.this.init_CharEncoder.flush());
      }

      private void poll_V_8() {
         this.write_Set.clear();

         for (AutismConfig.PayloadChannelRegistrationRule rule : AutismPacketLoggerOverlay.this.init_bC.init_List()) {
            if (rule != null && rule.enabled) {
               String channel = PayloadRegistrationManager.init_Str_Str(rule.channel);
               if (PayloadRegistrationManager.load_Z_Str(channel)) {
                  this.write_Set.add(channel);
               }
            }
         }

         if (this.load_Set.isEmpty()) {
            for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.init_bB.read_List()) {
               if (preset != null) {
                  this.load_Set.add(preset.init_Str());
               }
            }
         }

         this.read_Set.clear();
         this.push = 0;

         for (AutismConfig.PayloadChannelFilterRule rule : AutismPacketLoggerOverlay.this.init_bB.init_List()) {
            if (rule != null) {
               if (rule.enabled) {
                  this.push++;
               }

               if (rule.enabled) {
                  this.read_Set.add(PayloadChannelFilterManager.init_Str_Str(rule.pattern));
               }
            }
         }

         if (this.load_Map.isEmpty() && this.read_Map.isEmpty()) {
            this.set_V_8();
         }

         this.pop = this.buffer();
         this.init_Str_2 = this.read_Str_7();
      }

      private void set_V_8() {
         for (AutismPacketLoggerOverlay.Inner_StateCache category : this.init_Map_2().values()) {
            int presetCount = category.init_List.size();
            LinkedHashSet<String> exact = new LinkedHashSet<>();

            for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.init_Map.values()) {
               presetCount += family.init_List.size();
               exact.addAll(family.init_Set());
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.init_List) {
               if (preset != null) {
                  String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
                  if (PayloadRegistrationManager.load_Z_Str(pattern)) {
                     exact.add(pattern);
                  }
               }
            }

            this.load_Map.put(category.init_Str, presetCount);
            this.read_Map.put(category.init_Str, exact.size());
         }
      }

      private void put_V_7() {
         this.poll_V_8();
         String query = this.init_k.load_Str() == null ? "" : this.init_k.load_Str().trim().toLowerCase(Locale.ROOT);
         this.init_List.clear();
         boolean registrationUnlocked = PayloadSubscriptionManager.init_Z();
         String registeredHeader = registrationUnlocked ? "Registered Channels" : "Saved Channels (Locked)";
         List<AutismConfig.PayloadChannelRegistrationRule> registrations = AutismPacketLoggerOverlay.this.init_bC.init_List();
         Map<String, List<AutismPacketLoggerOverlay.Inner_ConfigStore>> registeredGroups = new LinkedHashMap<>();

         for (int i = 0; i < registrations.size(); i++) {
            AutismConfig.PayloadChannelRegistrationRule rule = registrations.get(i);
            if (rule != null && rule.enabled && this.init_Z_AutismConfig_Paylo_Str(rule, query)) {
               String group = this.init_Str(rule);
               registeredGroups.computeIfAbsent(group, unused -> new ArrayList<>()).add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(i, rule));
            }
         }

         int registeredCount = registeredGroups.values().stream().mapToInt(List::size).sum();
         if (!registeredGroups.isEmpty()) {
            this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(registeredHeader));

            for (Entry<String, List<AutismPacketLoggerOverlay.Inner_ConfigStore>> group : this.init(registeredGroups)) {
               this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(group.getKey()));
               group.getValue().sort(this::init_I_AutismPacketLogger_AutismPacketLogger);
               this.init_List.addAll(group.getValue());
            }
         } else if (query.isEmpty()) {
            this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(registeredHeader));
            this.init_List
               .add(
                  AutismPacketLoggerOverlay.Inner_ConfigStore.load(
                     registrationUnlocked
                        ? "No channels registered. Enable a preset or add an exact channel."
                        : "No channels saved. Enable a preset or add an exact channel."
                  )
               );
         } else if (registeredCount == 0) {
            this.init_List
               .add(AutismPacketLoggerOverlay.Inner_ConfigStore.load(registrationUnlocked ? "No matching registered channels." : "No matching saved channels."));
         }

         Set<String> knownRegistrationChannels = new LinkedHashSet<>();

         for (AutismConfig.PayloadChannelRegistrationRule rule : registrations) {
            if (rule != null) {
               knownRegistrationChannels.add(PayloadRegistrationManager.init_Str_Str(rule.channel));
            }
         }

         boolean customHeaderAdded = false;
         List<AutismConfig.PayloadChannelFilterRule> active = AutismPacketLoggerOverlay.this.init_bB.init_List();

         for (int i = 0; i < active.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = active.get(i);
            if (rule != null && !rule.preset && !this.load_Set.contains(PayloadChannelFilterManager.init_Str_Str(rule.pattern)) && (!this.sync_Z || rule.enabled)) {
               String normalizedPattern = PayloadChannelFilterManager.init_Str_Str(rule.pattern);
               if (!PayloadRegistrationManager.load_Z_Str(normalizedPattern) || !knownRegistrationChannels.contains(normalizedPattern)) {
                  String hay = ((rule.label == null ? "" : rule.label) + " " + rule.pattern).toLowerCase(Locale.ROOT);
                  if (query.isEmpty() || hay.contains(query)) {
                     if (!customHeaderAdded) {
                        this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init("Custom Highlights"));
                        customHeaderAdded = true;
                     }

                     this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(i, rule));
                  }
               }
            }
         }

         boolean availableHeader = false;
         if (!this.sync_Z) {
            for (String learned : PayloadSubscriptionManager.init_List()) {
               if (learned != null
                  && !learned.isBlank()
                  && !knownRegistrationChannels.contains(learned)
                  && (query.isEmpty() || learned.toLowerCase(Locale.ROOT).contains(query))) {
                  if (!availableHeader) {
                     this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init("Learned Exact Channels"));
                     availableHeader = true;
                  }

                  this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.read(learned));
               }
            }
         }

         for (int i = 0; i < registrations.size(); i++) {
            AutismConfig.PayloadChannelRegistrationRule rule = registrations.get(i);
            if (rule != null && !rule.enabled && !this.sync_Z && !"preset".equals(rule.source) && this.init_Z_AutismConfig_Paylo_Str(rule, query)) {
               if (!availableHeader) {
                  this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init("Available Exact Channels"));
                  availableHeader = true;
               }

               this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(i, rule));
            }
         }

         for (AutismPacketLoggerOverlay.Inner_StateCache category : this.init_List_11()) {
            List<AutismPacketLoggerOverlay.Inner_UiHelper> visibleFamilies = new ArrayList<>();

            for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.load) {
               if (this.init_Z_AutismPacketLogger_Str(family, query) && (!this.sync_Z || this.init_I(family) > 0)) {
                  visibleFamilies.add(family);
               }
            }

            List<PayloadChannelFilterManager.Inner_CachePool> standalone = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.read) {
               if (this.init_Z_bB_CachePool_Str(preset, query) && (!this.sync_Z || this.init_Z(preset))) {
                  standalone.add(preset);
               }
            }

            if (!visibleFamilies.isEmpty() || !standalone.isEmpty()) {
               this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(category.init_Str));

               for (AutismPacketLoggerOverlay.Inner_UiHelper family : visibleFamilies) {
                  this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(family));
                  if (this.load_Z_AutismPacketLogger_Str(family, query)) {
                     for (PayloadChannelFilterManager.Inner_CachePool preset : family.init_List()) {
                        if (this.init_Z_AutismPacketLogger(family, preset, query) && (!this.sync_Z || this.init_Z(preset))) {
                           this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(preset));
                        }
                     }
                  }
               }

               for (PayloadChannelFilterManager.Inner_CachePool preset : standalone) {
                  this.init_List.add(AutismPacketLoggerOverlay.Inner_ConfigStore.init(preset));
               }
            }
         }

         if (this.init_List.isEmpty()) {
            this.init_List
               .add(AutismPacketLoggerOverlay.Inner_ConfigStore.load(this.sync_Z ? "No enabled payload channels or highlights." : "No payload channels match."));
         }

         this.add_V_8();
      }

      private Map<String, AutismPacketLoggerOverlay.Inner_StateCache> init_Map_2() {
         if (this.init_Map != null) {
            return this.init_Map;
         }

         Map<String, AutismPacketLoggerOverlay.Inner_StateCache> categories = new LinkedHashMap<>();
         this.write_Map.clear();
         this.update_Map.clear();

         for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.init_bB.read_List()) {
            if (preset != null) {
               this.update_Map.put(preset, (preset.write() + " " + preset.load() + " " + preset.read()).toLowerCase(Locale.ROOT));
               if (PayloadChannelFilterManager.close(preset)) {
                  String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
                  if (PayloadRegistrationManager.load_Z_Str(pattern)) {
                     PayloadChannelFilterManager.Inner_CachePool existing = this.write_Map.get(pattern);
                     if (existing == null || this.load_I(preset) < this.load_I(existing)) {
                        this.write_Map.put(pattern, preset);
                     }
                  }
               }

               String group = this.init_Str_2(preset.write());
               AutismPacketLoggerOverlay.Inner_StateCache category = categories.computeIfAbsent(group, AutismPacketLoggerOverlay.Inner_StateCache::new);
               AutismPacketLoggerOverlay.Inner_SyncManager familyKey = this.init_AutismPacketLogger(preset);
               if (familyKey != null) {
                  category.init(familyKey).init_List.add(preset);
               } else {
                  category.init_List.add(preset);
               }
            }
         }

         this.init_Map = categories;
         this.load_List = new ArrayList<>(categories.values());
         this.load_List.sort((a, b) -> {
            int priority = Integer.compare(this.read_I_3(a.init_Str), this.read_I_3(b.init_Str));
            return priority != 0 ? priority : String.CASE_INSENSITIVE_ORDER.compare(a.init_Str, b.init_Str);
         });

         for (AutismPacketLoggerOverlay.Inner_StateCache category : this.load_List) {
            category.load = new ArrayList<>(category.init_Map.values());
            category.load.sort(this::init_I_AutismPacketLogger_AutismPacketLogger_2);
            category.read = new ArrayList<>(category.init_List);
            this.init(category.read);

            for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.load) {
               family.load_List = new ArrayList<>(family.init_List);
               this.init(family.load_List);
            }
         }

         return this.init_Map;
      }

      private List<AutismPacketLoggerOverlay.Inner_StateCache> init_List_11() {
         this.init_Map_2();
         return this.load_List == null ? List.of() : this.load_List;
      }

      private String init_Str_2(String group) {
         return group != null && !group.isBlank() ? group.trim() : "Other";
      }

      private AutismPacketLoggerOverlay.Inner_SyncManager init_AutismPacketLogger(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null && PayloadChannelFilterManager.close(preset)) {
            String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
            if (!PayloadRegistrationManager.load_Z_Str(pattern)) {
               return null;
            }

            String family = this.load_Str(pattern);
            return family.isBlank() ? null : new AutismPacketLoggerOverlay.Inner_SyncManager(this.init_Str_2(preset.write()), family, family);
         } else {
            return null;
         }
      }

      private String load_Str(String channel) {
         String normalized = PayloadRegistrationManager.init_Str_Str(channel);
         int colon = normalized.indexOf(58);
         if (colon <= 0) {
            return "";
         } else {
            String namespace = normalized.substring(0, colon);
            String path = normalized.substring(colon + 1);
            if (namespace.equals("plasmo") && path.startsWith("voice/v2/")) {
               return "plasmo:voice/v2/*";
            } else if (namespace.equals("ftbchunks")) {
               return "ftbchunks:*";
            } else {
               return namespace.equals("ftbteams") ? "ftbteams:*" : namespace + ":*";
            }
         }
      }

      private boolean init_Z_AutismPacketLogger_Str(AutismPacketLoggerOverlay.Inner_UiHelper family, String query) {
         if (family == null) {
            return false;
         }

         if (query != null && !query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            if ((family.init_Str + " " + family.read).toLowerCase(Locale.ROOT).contains(needle)) {
               return true;
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : family.init_List) {
               if (this.init_Z_bB_CachePool_Str(preset, needle)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      }

      private boolean init_Z_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_UiHelper family, PayloadChannelFilterManager.Inner_CachePool preset, String query) {
         if (query != null && !query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            return family != null && (family.init_Str + " " + family.read).toLowerCase(Locale.ROOT).contains(needle)
               ? true
               : this.init_Z_bB_CachePool_Str(preset, needle);
         } else {
            return true;
         }
      }

      private boolean load_Z_AutismPacketLogger_Str(AutismPacketLoggerOverlay.Inner_UiHelper family, String query) {
         return family != null && (this.init_Set.contains(family.init_Str()) || query != null && !query.isBlank());
      }

      private int init_I_AutismPacketLogger_AutismPacketLogger_2(AutismPacketLoggerOverlay.Inner_UiHelper a, AutismPacketLoggerOverlay.Inner_UiHelper b) {
         int exact = Integer.compare(b.init_I(), a.init_I());
         return exact != 0 ? exact : String.CASE_INSENSITIVE_ORDER.compare(a.read, b.read);
      }

      private List<Entry<String, List<AutismPacketLoggerOverlay.Inner_ConfigStore>>> init(Map<String, List<AutismPacketLoggerOverlay.Inner_ConfigStore>> groups) {
         List<Entry<String, List<AutismPacketLoggerOverlay.Inner_ConfigStore>>> out = new ArrayList<>(groups.entrySet());
         out.sort((a, b) -> {
            int priority = Integer.compare(this.load_I(a.getKey()), this.load_I(b.getKey()));
            return priority != 0 ? priority : String.CASE_INSENSITIVE_ORDER.compare(a.getKey(), b.getKey());
         });
         return out;
      }

      private int init_I_AutismPacketLogger_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_ConfigStore a, AutismPacketLoggerOverlay.Inner_ConfigStore b) {
         String ac = a != null && a.init_AutismConfig_Paylo_2 != null ? PayloadRegistrationManager.init_Str_Str(a.init_AutismConfig_Paylo_2.channel) : "";
         String bc = b != null && b.init_AutismConfig_Paylo_2 != null ? PayloadRegistrationManager.init_Str_Str(b.init_AutismConfig_Paylo_2.channel) : "";
         return String.CASE_INSENSITIVE_ORDER.compare(ac, bc);
      }

      private String init_Str(AutismConfig.PayloadChannelRegistrationRule rule) {
         if (rule == null) {
            return "Registered: Other";
         } else {
            String source = rule.source == null ? "" : rule.source.trim().toLowerCase(Locale.ROOT);
            if ("custom".equals(source)) {
               return "Registered: Custom";
            } else if ("learned".equals(source)) {
               return "Registered: Learned";
            } else {
               PayloadChannelFilterManager.Inner_CachePool preset = this.init_bB_CachePool(rule.channel);
               if (preset != null && preset.write() != null && !preset.write().isBlank()) {
                  AutismPacketLoggerOverlay.Inner_SyncManager family = this.init_AutismPacketLogger(preset);
                  return family != null ? "Registered: " + preset.write().trim() + " - " + family.write() : "Registered: " + preset.write().trim();
               } else {
                  return "Registered: Other";
               }
            }
         }
      }

      private int load_I(String group) {
         String clean = group == null ? "" : group.trim();
         if (clean.startsWith("Registered: ")) {
            clean = clean.substring("Registered: ".length()).trim();
         }

         int familySep = clean.indexOf(" - ");
         if (familySep > 0) {
            clean = clean.substring(0, familySep).trim();
         }

         if ("Custom".equalsIgnoreCase(clean)) {
            return 100;
         } else if ("Learned".equalsIgnoreCase(clean)) {
            return 101;
         } else {
            return "Other".equalsIgnoreCase(clean) ? 102 : this.read_I_3(clean);
         }
      }

      private PayloadChannelFilterManager.Inner_CachePool init_bB_CachePool(String channel) {
         String normalized = PayloadRegistrationManager.init_Str_Str(channel);
         if (normalized.isBlank()) {
            return null;
         }

         this.init_Map_2();
         return this.write_Map.get(normalized);
      }

      private boolean init_Z_bB_CachePool_Str(PayloadChannelFilterManager.Inner_CachePool preset, String query) {
         if (preset == null) {
            return false;
         }

         if (query != null && !query.isBlank()) {
            String hay = this.update_Map.get(preset);
            if (hay == null) {
               hay = (preset.write() + " " + preset.load() + " " + preset.read()).toLowerCase(Locale.ROOT);
               this.update_Map.put(preset, hay);
            }

            return hay.contains(query);
         } else {
            return true;
         }
      }

      private void init(List<PayloadChannelFilterManager.Inner_CachePool> presets) {
         presets.sort((a, b) -> {
            int kind = Integer.compare(this.load_I(a), this.load_I(b));
            if (kind != 0) {
               return kind;
            }

            int exact = Integer.compare(this.init_I(b), this.init_I(a));
            if (exact != 0) {
               return exact;
            }

            boolean aDefault = PayloadChannelFilterManager.flush(a);
            boolean bDefault = PayloadChannelFilterManager.flush(b);
            if (aDefault != bDefault) {
               return aDefault ? -1 : 1;
            }

            int labelCompare = String.CASE_INSENSITIVE_ORDER.compare(a.load(), b.load());
            return labelCompare != 0 ? labelCompare : String.CASE_INSENSITIVE_ORDER.compare(a.read(), b.read());
         });
      }

      private int init_I(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset == null) {
            return 0;
         }

         String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
         return PayloadRegistrationManager.load_Z_Str(pattern) ? 1 : 0;
      }

      private PayloadSubscriptionManager.Inner_CachePool init_bD_CachePool(String pattern) {
         String normalized = PayloadChannelFilterManager.init_Str_Str(pattern);
         return normalized.isBlank() ? new PayloadSubscriptionManager.Inner_CachePool(false, false, 0, List.of()) : this.sync_Map.computeIfAbsent(normalized, PayloadSubscriptionManager::init_bD_CachePool);
      }

      private int load_I(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null && preset.init_bB_BufferHelper() != null) {
            byte var10000;
            switch (preset.init_bB_BufferHelper()) {
               case EXACT:
                  var10000 = 0;
                  break;
               case CORE:
                  var10000 = 1;
                  break;
               case TEMPLATE:
                  var10000 = 2;
                  break;
               case UNVERIFIED:
                  var10000 = 3;
                  break;
               default:
                  throw new MatchException(null, null);
            }

            return var10000;
         } else {
            return 4;
         }
      }

      private int read_I_3(String group) {
         byte var10000;
         switch (group == null ? "" : group.toLowerCase(Locale.ROOT)) {
            case "minecraft":
               var10000 = 0;
               break;
            case "proxy":
               var10000 = 1;
               break;
            case "mod api":
               var10000 = 2;
               break;
            case "voice":
               var10000 = 3;
               break;
            case "protection":
               var10000 = 4;
               break;
            case "shops":
               var10000 = 5;
               break;
            case "auctions":
               var10000 = 6;
               break;
            case "crates":
               var10000 = 7;
               break;
            case "gambling":
               var10000 = 8;
               break;
            case "containers":
               var10000 = 9;
               break;
            case "storage":
               var10000 = 10;
               break;
            case "backpacks":
               var10000 = 11;
               break;
            case "economy":
               var10000 = 12;
               break;
            case "items":
               var10000 = 13;
               break;
            default:
               var10000 = 50;
         }

         return var10000;
      }

      private boolean init_Z_AutismConfig_Paylo_Str(AutismConfig.PayloadChannelRegistrationRule rule, String query) {
         if (query != null && !query.isBlank()) {
            String hay = ((rule.label == null ? "" : rule.label) + " " + (rule.channel == null ? "" : rule.channel) + " " + PayloadRegistrationManager.load_Str_Str(rule.source))
               .toLowerCase(Locale.ROOT);
            return hay.contains(query);
         } else {
            return true;
         }
      }

      private void init_V_I_Runnable(int clickedIndex, Runnable mutation) {
         String anchor = this.init(clickedIndex);
         int oldIndex = clickedIndex;
         mutation.run();
         this.put_V_7();
         if (anchor != null) {
            int newIndex = this.write_I_2(anchor);
            if (newIndex >= 0) {
               this.flush_I += (newIndex - oldIndex) * 14;
            }
         }

         this.add_V_8();
         int listH = this.next_I_6();
         int maxScroll = Math.max(0, this.init_List.size() * 14 - listH);
         this.init_CacheEntry.load(this.flush_I, maxScroll);
      }

      private String init(int index) {
         if (index >= 0 && index < this.init_List.size()) {
            AutismPacketLoggerOverlay.Inner_ConfigStore row = this.init_List.get(index);
            if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.ACTIVE && row.init_AutismConfig_Paylo != null) {
               return "custom:" + PayloadChannelFilterManager.init_Str_Str(row.init_AutismConfig_Paylo.pattern);
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.REGISTRATION && row.init_AutismConfig_Paylo_2 != null) {
               return "reg:" + PayloadRegistrationManager.init_Str_Str(row.init_AutismConfig_Paylo_2.channel);
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.LEARNED && row.load_Str != null) {
               return "learned:" + PayloadRegistrationManager.init_Str_Str(row.load_Str);
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.PRESET && row.init_bB_CachePool != null) {
               return "preset:" + row.init_bB_CachePool.init_Str();
            } else if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.FAMILY && row.init_AutismPacketLogger_2 != null) {
               return "family:" + row.init_AutismPacketLogger_2.init_Str();
            } else {
               return row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.HEADER && row.init_Str != null ? "header:" + row.init_Str : null;
            }
         } else {
            return null;
         }
      }

      private int write_I_2(String key) {
         if (key == null) {
            return -1;
         }

         for (int i = 0; i < this.init_List.size(); i++) {
            if (key.equals(this.init(i))) {
               return i;
            }
         }

         return -1;
      }

      private void add_V_8() {
         int listH = this.next_I_6();
         int maxScroll = Math.max(0, this.init_List.size() * 14 - listH);
         this.flush_I = this.init_I_I_3(this.flush_I, 14, maxScroll);
      }

      @Override
      public boolean load_Z() {
         return true;
      }

      @Override
      public boolean init_Z_D_3(double mx, double my, int button) {
         if (!this.init_Z) {
            return false;
         }

         OverlayBounds bounds = this.init_cH();
         if (button == 0 && this.write(mx, my, bounds)) {
            this.init_V_Z(false);
            return true;
         }

         if (this.close_Z) {
            return this.read(mx, my, button);
         }

         if (button == 0 && this.load_Z_D_D(mx, my)) {
            this.update_Z = true;
            this.init_D = mx - this.init_I_2;
            this.load_D = my - this.load_I;
            return true;
         }

         if (this.load_Z) {
            return true;
         }

         if (this.init_k.init_Z_D_2(mx, my, button)) {
            return true;
         }

         if (this.load_k.init_Z_D_2(mx, my, button)) {
            return true;
         }

         int y = this.tick_I_19();
         int customW = this.prev_I_5();
         int addW = this.valid();
         int addX = this.init_I_2 + 6 + customW + 2;
         if (button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.add_I_13()) {
            if (mx >= addX && mx < addX + addW) {
               this.clear_V_7();
               return true;
            }

            if (this.poll_Z_14()) {
               int cancelX = addX + addW + 2;
               if (mx >= cancelX && mx < cancelX + this.size()) {
                  this.write_V_20();
                  return true;
               }
            }
         }

         y = this.run_I_19();
         AutismPacketLoggerOverlay.Inner_StreamUtil controls = this.init_AutismPacketLogger_4();
         int cx = this.init_I_2 + 6;
         boolean inControlsRow = button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.add_I_13();
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[0]) {
            AutismPacketLoggerOverlay.this.init_bB.sync_V();
            AutismPacketLoggerOverlay.this.init_bC.update();
            this.sync_V_15();
            this.put_V_7();
            AutismPacketLoggerOverlay.this.pop_Z = true;
            return true;
         }

         cx += controls.init_IArr[0] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[1]) {
            this.reset_V_6();
            return true;
         }

         cx += controls.init_IArr[1] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[2]) {
            this.load(false);
            return true;
         }

         cx += controls.init_IArr[2] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[3]) {
            this.sync_Z = !this.sync_Z;
            this.put_V_7();
            return true;
         }

         cx += controls.init_IArr[3] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[4]) {
            this.next_V_6();
            return true;
         }

         cx += controls.init_IArr[4] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[5]) {
            this.prev_V_6();
            return true;
         }

         cx += controls.init_IArr[5] + controls.init_I;
         if (inControlsRow && mx >= cx && mx < cx + controls.init_IArr[6]) {
            AutismPacketLoggerOverlay.this.init_bB.flush_V();
            AutismPacketLoggerOverlay.this.init_bC.read_V();
            this.sync_V_15();
            this.put_V_7();
            AutismPacketLoggerOverlay.this.pop_Z = true;
            return true;
         }

         int lockW = this.data();
         int lockX = this.init_I_2 + this.read_I - 8 - lockW;
         int lockY = this.set_I_17();
         if (button == 0 && my >= lockY && my < lockY + AutismPacketLoggerOverlay.this.add_I_13() && mx >= lockX && mx < lockX + lockW) {
            if (PayloadSubscriptionManager.init_Z()) {
               this.open_V_11();
            } else {
               this.close_V_13();
            }

            return true;
         } else {
            int listTop = this.put_I_7();
            int listH = this.next_I_6();
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
               this.init_List.size() * 14, listH, this.init_I_2 + this.read_I - 7, listTop, 3, listH, this.flush_I
            );
            if (button == 0 && metrics.init_Z() && metrics.init((int)mx, (int)my)) {
               this.write_Z = true;
               this.close_I = (int)my - metrics.update();
               return true;
            }

            if (button == 0 && my >= listTop && my < listTop + listH && mx >= this.add_I_14() && mx < this.clear_I_7()) {
               int index = (int)((my - listTop + this.flush_I) / 14.0);
               if (index >= 0 && index < this.init_List.size()) {
                  AutismPacketLoggerOverlay.Inner_ConfigStore row = this.init_List.get(index);
                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.REGISTRATION) {
                     boolean editable = this.init_Z(row.init_AutismConfig_Paylo_2);
                     int removeX = editable ? this.clear_I_7() - 14 : this.clear_I_7();
                     int editX = editable ? removeX - 14 : this.clear_I_7();
                     if (editable && mx >= removeX && mx <= removeX + 14) {
                        this.init_V_I_Runnable(index, () -> {
                           this.load_V_I_AutismConfig_Paylo_2(row.load_I, row.init_AutismConfig_Paylo_2);
                           this.sync_V_15();
                        });
                     } else if (editable && mx >= editX && mx <= editX + 14) {
                        this.init_V_I_AutismConfig_Paylo_2(row.load_I, row.init_AutismConfig_Paylo_2);
                     } else {
                        this.init_V_I_Runnable(index, () -> {
                           AutismPacketLoggerOverlay.this.init_bC.load_V_I(row.load_I);
                           this.sync_V_15();
                        });
                     }

                     AutismPacketLoggerOverlay.this.pop_Z = true;
                     return true;
                  }

                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.LEARNED) {
                     if (!this.init_Z(List.of(row.load_Str))) {
                        this.stop_V_6();
                        return true;
                     }

                     this.init_V_I_Runnable(index, () -> {
                        AutismPacketLoggerOverlay.this.init_bC.load(row.load_Str, row.load_Str, "learned");
                        this.sync_V_15();
                     });
                     AutismPacketLoggerOverlay.this.pop_Z = true;
                     return true;
                  }

                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.ACTIVE) {
                     if (this.write(mx, my, listTop + index * 14 - this.flush_I)) {
                        this.write(row.init_I, row.init_AutismConfig_Paylo);
                        return true;
                     }

                     int ux = this.clear_I_7() - 14;
                     int editX = ux - 14;
                     if (mx >= ux && mx <= ux + 14) {
                        this.init_V_I_Runnable(index, () -> this.read_V_I_AutismConfig_Paylo(row.init_I, row.init_AutismConfig_Paylo));
                     } else if (mx >= editX && mx <= editX + 14) {
                        this.init_V_I_AutismConfig_Paylo(row.init_I, row.init_AutismConfig_Paylo);
                     } else {
                        this.init_V_I_Runnable(index, () -> this.load_V_I_AutismConfig_Paylo(row.init_I, row.init_AutismConfig_Paylo));
                     }

                     AutismPacketLoggerOverlay.this.pop_Z = true;
                     return true;
                  }

                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.FAMILY) {
                     int rowY = listTop + index * 14 - this.flush_I;
                     if (this.write(mx, my, rowY)) {
                        this.init_V(row.init_AutismPacketLogger_2);
                        return true;
                     }

                     if (this.update(mx, my, rowY)) {
                        if (row.init_AutismPacketLogger_2 != null) {
                           String key = row.init_AutismPacketLogger_2.init_Str();
                           if (!this.init_Set.remove(key)) {
                              this.init_Set.add(key);
                           }

                           this.put_V_7();
                        }

                        return true;
                     }

                     this.init_V_I_Runnable(index, () -> this.load(row.init_AutismPacketLogger_2));
                     AutismPacketLoggerOverlay.this.pop_Z = true;
                     return true;
                  }

                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.PRESET) {
                     if (!this.write(mx, my, listTop + index * 14 - this.flush_I)) {
                        if (row.init_bB_CachePool != null
                           && row.init_bB_CachePool.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT
                           && !this.init_Z(row.init_bB_CachePool)
                           && !this.init_Z(List.of(row.init_bB_CachePool.read()))) {
                           this.stop_V_6();
                           return true;
                        }

                        this.init_V_I_Runnable(index, () -> this.init_V(row.init_bB_CachePool));
                        AutismPacketLoggerOverlay.this.pop_Z = true;
                        return true;
                     }

                     if (row.init_bB_CachePool != null && row.init_bB_CachePool.init_bB_BufferHelper().init_Z()) {
                        this.load_V(row.init_bB_CachePool);
                     } else {
                        this.read(row.init_bB_CachePool);
                     }

                     return true;
                  }

                  if (row.init_AutismPacketLogger == AutismPacketLoggerOverlay.Inner_TaskQueue.HEADER
                     && this.load_Map.getOrDefault(this.init_Str_2(row.init_Str), 0) > 0) {
                     if (this.write(mx, my, listTop + index * 14 - this.flush_I)) {
                        this.write_V_2(row.init_Str);
                        return true;
                     }

                     this.init_V_I_Runnable(index, () -> this.update(row.init_Str));
                     AutismPacketLoggerOverlay.this.pop_Z = true;
                     return true;
                  }
               }
            }

            return this.init_Z_D_D(mx, my);
         }
      }

      private boolean init_Z(AutismConfig.PayloadChannelRegistrationRule rule) {
         if (rule == null) {
            return false;
         }

         String source = rule.source == null ? "" : rule.source.trim().toLowerCase(Locale.ROOT);
         return "custom".equals(source) || "learned".equals(source);
      }

      private void init_V_I_AutismConfig_Paylo_2(int index, AutismConfig.PayloadChannelRegistrationRule rule) {
         if (!this.init_Z(rule)) {
            bi.read("Presets can be toggled, not edited.");
         } else {
            this.poll = index;
            this.open_I = -1;
            this.load_k.init_V_Str(PayloadRegistrationManager.init_Str_Str(rule.channel));
            this.load_k.init_V_Component(Component.literal("edit exact channel"));
            this.load_k.push(true);
         }
      }

      private void init_V_I_AutismConfig_Paylo(int index, AutismConfig.PayloadChannelFilterRule rule) {
         if (rule != null && !rule.preset) {
            this.open_I = index;
            this.poll = -1;
            this.load_k.init_V_Str(PayloadChannelFilterManager.init_Str_Str(rule.pattern));
            this.load_k.init_V_Component(Component.literal("edit highlight pattern"));
            this.load_k.push(true);
         }
      }

      private void clear_V_7() {
         String pattern = this.load_k.load_Str() == null ? "" : this.load_k.load_Str().trim();
         if (pattern.isBlank()) {
            bi.read("Enter a payload channel first.");
         } else if (!this.poll_Z_14() && !this.init_Z(List.of(pattern))) {
            this.stop_V_6();
         } else {
            String normalized = PayloadChannelFilterManager.init_Str_Str(pattern);
            String label = normalized;
            if (this.poll >= 0) {
               this.init_V_3(normalized);
            } else if (this.open_I >= 0) {
               this.load_V_2(normalized);
            } else {
               boolean filterChanged = AutismPacketLoggerOverlay.this.init_bB.load(label, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
               boolean registrationChanged = false;
               if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                  registrationChanged = AutismPacketLoggerOverlay.this.init_bC.load(label, normalized, "custom");
               }

               if (!filterChanged && !registrationChanged) {
                  bi.read("Payload channel already exists.");
               } else {
                  this.load_k.init_V_Str("");
                  this.sync_V_15();
                  this.put_V_7();
                  this.flush_I = 0;
                  this.init_CacheEntry.load(0, Math.max(0, this.init_List.size() * 14 - this.next_I_6()));
                  AutismPacketLoggerOverlay.this.pop_Z = true;
                  if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                     bi.init("Payload channel added. Press Apply to register.");
                  } else {
                     bi.init("Payload highlight added.");
                  }
               }
            }
         }
      }

      private void init_V_3(String normalized) {
         if (!PayloadRegistrationManager.load_Z_Str(normalized)) {
            bi.read("Exact registered channels need namespace:path.");
         } else {
            List<AutismConfig.PayloadChannelRegistrationRule> regs = AutismPacketLoggerOverlay.this.init_bC.load_List();
            if (this.poll >= 0 && this.poll < regs.size()) {
               int existing = AutismPacketLoggerOverlay.this.init_bC.init_I_Str(normalized);
               String oldChannel = "";
               AutismConfig.PayloadChannelRegistrationRule currentRule = this.poll >= 0 && this.poll < regs.size() ? regs.get(this.poll) : null;
               if (currentRule != null) {
                  oldChannel = PayloadRegistrationManager.init_Str_Str(currentRule.channel);
               }

               if (existing >= 0 && existing != this.poll) {
                  AutismPacketLoggerOverlay.this.init_bC.write(this.poll);
                  AutismPacketLoggerOverlay.this.init_bC.load(normalized, normalized, "custom");
               } else {
                  AutismConfig.PayloadChannelRegistrationRule rule = regs.get(this.poll);
                  if (rule != null && this.init_Z(rule)) {
                     rule.channel = normalized;
                     rule.label = normalized;
                     rule.source = "custom";
                     rule.enabled = true;
                  }
               }

               if (!oldChannel.isBlank() && !oldChannel.equals(normalized)) {
                  this.read_V_2(oldChannel);
               }

               AutismPacketLoggerOverlay.this.init_bB.load(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
               this.write_V_20();
               this.sync_V_15();
               this.put_V_7();
               bi.init("Payload channel edited. Press Apply to register.");
            } else {
               this.write_V_20();
               this.put_V_7();
            }
         }
      }

      private void load_V_2(String normalized) {
         List<AutismConfig.PayloadChannelFilterRule> filters = AutismPacketLoggerOverlay.this.init_bB.load_List();
         if (this.open_I >= 0 && this.open_I < filters.size()) {
            AutismConfig.PayloadChannelFilterRule rule = filters.get(this.open_I);
            if (rule != null && !rule.preset) {
               String oldPattern = PayloadChannelFilterManager.init_Str_Str(rule.pattern);
               rule.pattern = normalized;
               rule.label = normalized;
               rule.enabled = true;
               if (PayloadRegistrationManager.load_Z_Str(oldPattern)) {
                  this.flush(oldPattern);
               }

               if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                  AutismPacketLoggerOverlay.this.init_bC.load(normalized, normalized, "custom");
               }
            }

            this.write_V_20();
            this.sync_V_15();
            this.put_V_7();
            bi.init("Payload highlight edited.");
         } else {
            this.write_V_20();
            this.put_V_7();
         }
      }

      private void init_V(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null) {
            String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
            boolean enabled = this.init_Z(preset);
            if (!enabled) {
               AutismPacketLoggerOverlay.this.init_bB.write_Z_bB_CachePool(preset);
               if (PayloadChannelFilterManager.close(preset)) {
                  AutismPacketLoggerOverlay.this.init_bC.load(preset.load(), pattern, "preset");
               }

               this.sync_V_15();
            } else {
               if (AutismPacketLoggerOverlay.this.init_bB.sync(preset)) {
                  AutismPacketLoggerOverlay.this.init_bB.load_V_bB_CachePool(preset);
               }

               if (PayloadChannelFilterManager.close(preset)) {
                  this.sync(pattern);
               }

               this.sync_V_15();
            }
         }
      }

      private void load_V_I_AutismConfig_Paylo(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         if (rule != null) {
            String pattern = PayloadChannelFilterManager.init_Str_Str(rule.pattern);
            boolean enabling = !rule.enabled;
            AutismPacketLoggerOverlay.this.init_bB.load_V_I(ruleIndex);
            if (PayloadRegistrationManager.load_Z_Str(pattern)) {
               if (enabling) {
                  AutismPacketLoggerOverlay.this.init_bC.load(rule.label, pattern, "custom");
               } else {
                  this.flush(pattern);
               }
            }

            this.sync_V_15();
         }
      }

      private void read_V_I_AutismConfig_Paylo(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         String pattern = rule == null ? "" : PayloadChannelFilterManager.init_Str_Str(rule.pattern);
         AutismPacketLoggerOverlay.this.init_bB.write_V_I(ruleIndex);
         if (PayloadRegistrationManager.load_Z_Str(pattern)) {
            this.flush(pattern);
         }

         this.sync_V_15();
      }

      private void load_V_I_AutismConfig_Paylo_2(int registrationIndex, AutismConfig.PayloadChannelRegistrationRule rule) {
         String channel = rule == null ? "" : PayloadRegistrationManager.init_Str_Str(rule.channel);
         AutismPacketLoggerOverlay.this.init_bC.write(registrationIndex);
         this.read_V_2(channel);
      }

      private void read_V_2(String pattern) {
         String normalized = PayloadChannelFilterManager.init_Str_Str(pattern);
         if (!normalized.isBlank()) {
            List<AutismConfig.PayloadChannelFilterRule> filters = AutismPacketLoggerOverlay.this.init_bB.load_List();

            for (int i = filters.size() - 1; i >= 0; i--) {
               AutismConfig.PayloadChannelFilterRule rule = filters.get(i);
               if (rule != null && !rule.preset && normalized.equals(PayloadChannelFilterManager.init_Str_Str(rule.pattern))) {
                  filters.remove(i);
               }
            }
         }
      }

      private void load_V(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null) {
            this.open_I = -1;
            this.poll = -1;
            String prefix = PayloadChannelFilterManager.init_Str_bB_CachePool(preset);
            this.load_k.init_V_Str(prefix);
            this.load_k.init_V_Component(Component.literal("finish exact channel from " + preset.load()));
            this.load_k.push(true);
            bi.init("Finish the exact payload channel, then Add.");
         }
      }

      private void read(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11() && preset != null) {
            this.init_V_Str_7("Preset: " + preset.load(), List.of(preset), List.of(preset.read()));
         }
      }

      private void write_V_2(String group) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11() && group != null && !group.isBlank()) {
            List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();
            List<String> patterns = new ArrayList<>();
            AutismPacketLoggerOverlay.Inner_StateCache category = this.init_Map_2().get(this.init_Str_2(group));
            if (category != null) {
               for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.load) {
                  for (PayloadChannelFilterManager.Inner_CachePool preset : family.init_List()) {
                     presets.add(preset);
                     patterns.add(preset.read());
                  }
               }

               for (PayloadChannelFilterManager.Inner_CachePool preset : category.read) {
                  presets.add(preset);
                  patterns.add(preset.read());
               }

               this.init_V_Str_7("Category: " + group, presets, patterns);
            }
         }
      }

      private void init_V(AutismPacketLoggerOverlay.Inner_UiHelper family) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11() && family != null && !family.init_List.isEmpty()) {
            List<String> patterns = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : family.init_List) {
               patterns.add(preset.read());
            }

            this.init_V_Str_7("Family: " + family.read, new ArrayList<>(family.init_List), patterns);
         }
      }

      private void write(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11() && rule != null) {
            if (!rule.enabled) {
               AutismPacketLoggerOverlay.this.init_bB.load_V_I(ruleIndex);
               this.sync_V_15();
            }

            String pattern = PayloadChannelFilterManager.init_Str_Str(rule.pattern);
            String label = rule.label != null && !rule.label.isBlank() ? rule.label.trim() : pattern;
            int skipped = this.init_I_Str_4(label, pattern, "custom");
            this.sync_V_15();
            this.put_V_7();
            this.init_V_Str_6(label, List.of(pattern), skipped);
         }
      }

      private void init_V_Str_7(String label, List<PayloadChannelFilterManager.Inner_CachePool> presets, List<String> patterns) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11() && presets != null && !presets.isEmpty()) {
            int skipped = 0;

            for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
               if (preset != null) {
                  AutismPacketLoggerOverlay.this.init_bB.write_Z_bB_CachePool(preset);
                  skipped += this.init_I_Str_4(preset.load(), preset.read(), "preset");
               }
            }

            this.sync_V_15();
            this.put_V_7();
            this.init_V_Str_6(label, patterns, skipped);
         }
      }

      private int init_I_Str_4(String label, String pattern, String source) {
         PayloadSubscriptionManager.Inner_CachePool impact = this.init_bD_CachePool(pattern);
         int skipped = 0;

         for (String channel : impact.init_List()) {
            String normalized = PayloadRegistrationManager.init_Str_Str(channel);
            if (PayloadRegistrationManager.load_Z_Str(normalized)) {
               if (!AutismPacketLoggerOverlay.this.init_bC.init_Z_Str(normalized) && !this.init_Z(List.of(normalized))) {
                  skipped++;
               } else {
                  AutismPacketLoggerOverlay.this.init_bC.load(label, normalized, source);
               }
            }
         }

         return skipped;
      }

      private void init_V_Str_6(String label, List<String> patterns, int skipped) {
         if (!AutismPacketLoggerOverlay.this.update_Z && this.push_Z_11()) {
            this.flush_V_14();
            PayloadSubscriptionManager.init_V();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.init_Minecraft, false);
            PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.init_bD_BufferHelper();
            if (skipped > 0 || status != null && status.read() > 0) {
               int count = status == null ? 0 : status.init_I();
               int limit = status == null ? 96 : status.load_I();
               bi.write("Payload channel cap: registered " + count + "/" + limit + ".");
            }

            List<String> applied = status == null ? List.of() : status.init_List();
            PayloadStudyTracker.init(label, patterns == null ? List.of() : patterns, applied);
            this.init_V_Z(false);
            if (AutismPacketLoggerOverlay.init_Minecraft != null) {
               AutismPacketLoggerOverlay.init_Minecraft.gui.setScreen(null);
            }
         }
      }

      private void update(String group) {
         if (group != null && !group.isBlank()) {
            boolean enable = !this.load_Z_2(group);
            boolean capHit = false;
            AutismPacketLoggerOverlay.Inner_StateCache category = this.init_Map_2().get(this.init_Str_2(group));
            if (category != null) {
               List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();

               for (AutismPacketLoggerOverlay.Inner_UiHelper family : category.load) {
                  presets.addAll(family.init_List());
               }

               presets.addAll(category.read);

               for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
                  String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
                  if (enable) {
                     if (PayloadChannelFilterManager.close(preset) && !this.init_Z(List.of(pattern))) {
                        capHit = true;
                     } else {
                        AutismPacketLoggerOverlay.this.init_bB.write_Z_bB_CachePool(preset);
                        if (PayloadChannelFilterManager.close(preset)) {
                           AutismPacketLoggerOverlay.this.init_bC.load(preset.load(), pattern, "preset");
                        }
                     }
                  } else {
                     if (AutismPacketLoggerOverlay.this.init_bB.sync(preset)) {
                        AutismPacketLoggerOverlay.this.init_bB.load_V_bB_CachePool(preset);
                     }

                     if (PayloadChannelFilterManager.close(preset)) {
                        this.sync(pattern);
                     }
                  }
               }

               if (capHit) {
                  this.stop_V_6();
               }

               this.sync_V_15();
            }
         }
      }

      private void load(AutismPacketLoggerOverlay.Inner_UiHelper family) {
         if (family != null && !family.init_List.isEmpty()) {
            boolean enable = !this.init_Z(family);
            boolean capHit = false;
            List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>(family.init_List);
            this.init(presets);

            for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
               String pattern = PayloadChannelFilterManager.init_Str_Str(preset.read());
               if (PayloadRegistrationManager.load_Z_Str(pattern)) {
                  if (enable) {
                     if (!AutismPacketLoggerOverlay.this.init_bC.init_Z_Str(pattern) && !this.init_Z(List.of(pattern))) {
                        capHit = true;
                     } else {
                        AutismPacketLoggerOverlay.this.init_bB.write_Z_bB_CachePool(preset);
                        AutismPacketLoggerOverlay.this.init_bC.load(preset.load(), pattern, "preset");
                     }
                  } else {
                     if (AutismPacketLoggerOverlay.this.init_bB.sync(preset)) {
                        AutismPacketLoggerOverlay.this.init_bB.load_V_bB_CachePool(preset);
                     }

                     this.sync(pattern);
                  }
               }
            }

            if (capHit) {
               this.stop_V_6();
            }

            this.sync_V_15();
         }
      }

      private void sync(String channel) {
         int index = AutismPacketLoggerOverlay.this.init_bC.init_I_Str(channel);
         if (index >= 0) {
            List<AutismConfig.PayloadChannelRegistrationRule> rules = AutismPacketLoggerOverlay.this.init_bC.init_List();
            if (index < rules.size()) {
               AutismConfig.PayloadChannelRegistrationRule rule = rules.get(index);
               if (rule != null && "preset".equals(rule.source)) {
                  AutismPacketLoggerOverlay.this.init_bC.write(index);
               }
            }
         }
      }

      private void flush(String channel) {
         int index = AutismPacketLoggerOverlay.this.init_bC.init_I_Str(channel);
         if (index >= 0) {
            List<AutismConfig.PayloadChannelRegistrationRule> rules = AutismPacketLoggerOverlay.this.init_bC.init_List();
            if (index < rules.size()) {
               AutismConfig.PayloadChannelRegistrationRule rule = rules.get(index);
               if (rule != null) {
                  String source = rule.source == null ? "" : rule.source;
                  if ("custom".equals(source) || "learned".equals(source)) {
                     AutismPacketLoggerOverlay.this.init_bC.write(index);
                  }
               }
            }
         }
      }

      private void load(boolean replaceExisting) {
         List<String> learned = PayloadSubscriptionManager.init_List();
         if (learned.isEmpty()) {
            bi.read("No captured payload channels yet.");
         } else {
            if (replaceExisting) {
               AutismPacketLoggerOverlay.this.init_bC.read_V();
            }

            int added = 0;
            int skipped = 0;

            for (String channel : learned) {
               String normalized = PayloadRegistrationManager.init_Str_Str(channel);
               if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                  if (!AutismPacketLoggerOverlay.this.init_bC.init_Z_Str(normalized) && !this.init_Z(List.of(normalized))) {
                     skipped++;
                  } else {
                     boolean regChanged = AutismPacketLoggerOverlay.this.init_bC.load(normalized, normalized, "learned");
                     AutismPacketLoggerOverlay.this.init_bB.load(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
                     if (regChanged) {
                        added++;
                     }
                  }
               }
            }

            this.sync_V_15();
            this.put_V_7();
            AutismPacketLoggerOverlay.this.pop_Z = true;
            if (skipped > 0) {
               bi.write("Captured " + added + ", skipped " + skipped + " at channel cap.");
            } else {
               bi.init((replaceExisting ? "Reset to " : "Captured ") + added + " payload channels.");
            }
         }
      }

      private void reset_V_6() {
         int added = 0;
         int skipped = 0;

         for (String channel : PayloadSubscriptionManager.init_List()) {
            String normalized = PayloadRegistrationManager.init_Str_Str(channel);
            if (PayloadRegistrationManager.load_Z_Str(normalized)) {
               if (!AutismPacketLoggerOverlay.this.init_bC.init_Z_Str(normalized) && !this.init_Z(List.of(normalized))) {
                  skipped++;
               } else {
                  boolean regChanged = AutismPacketLoggerOverlay.this.init_bC.load(normalized, normalized, "learned");
                  AutismPacketLoggerOverlay.this.init_bB.load(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
                  if (regChanged) {
                     added++;
                  }
               }
            }
         }

         this.init_Map_2();

         for (PayloadChannelFilterManager.Inner_CachePool preset : this.write_Map.values()) {
            if (preset.init_bB_BufferHelper() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
               AutismPacketLoggerOverlay.this.init_bB.write_Z_bB_CachePool(preset);
               String normalized = PayloadRegistrationManager.init_Str_Str(preset.read());
               if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                  if (!AutismPacketLoggerOverlay.this.init_bC.init_Z_Str(normalized) && !this.init_Z(List.of(normalized))) {
                     skipped++;
                  } else {
                     boolean regChanged = AutismPacketLoggerOverlay.this.init_bC.load(preset.load(), normalized, "preset");
                     if (regChanged) {
                        added++;
                     }
                  }
               }
            }
         }

         this.sync_V_15();
         this.put_V_7();
         this.flush_I = 0;
         this.init_CacheEntry.load(0, Math.max(0, this.init_List.size() * 14 - this.next_I_6()));
         AutismPacketLoggerOverlay.this.pop_Z = true;
         if (skipped > 0) {
            bi.write("Known channels enabled with " + skipped + " skipped at cap.");
         } else {
            bi.init("Known exact channels enabled. Press Apply.");
         }
      }

      private boolean init_Z(Collection<String> patterns) {
         return this.init_LinkedHashSet(patterns).size() <= this.pop;
      }

      private LinkedHashSet<String> init_LinkedHashSet(Collection<String> patterns) {
         LinkedHashSet<String> projected = new LinkedHashSet<>();

         for (AutismConfig.PayloadChannelRegistrationRule rule : AutismPacketLoggerOverlay.this.init_bC.init_List()) {
            if (rule != null && rule.enabled) {
               String channel = PayloadRegistrationManager.init_Str_Str(rule.channel);
               if (PayloadRegistrationManager.load_Z_Str(channel)) {
                  projected.add(channel);
               }
            }
         }

         if (patterns != null) {
            for (String pattern : patterns) {
               PayloadSubscriptionManager.Inner_CachePool impact = this.init_bD_CachePool(pattern);

               for (String channel : impact.init_List()) {
                  String normalized = PayloadRegistrationManager.init_Str_Str(channel);
                  if (PayloadRegistrationManager.load_Z_Str(normalized)) {
                     projected.add(normalized);
                  }
               }
            }
         }

         return projected;
      }

      private int queue() {
         return this.pop;
      }

      private int buffer() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.init_bD_BufferHelper();
         return status != null && status.load_I() > 0 ? status.load_I() : 96;
      }

      private List<String> init_List(String group) {
         if (group != null && !group.isBlank()) {
            List<String> patterns = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.init_bB.read_List()) {
               if (group.equals(preset.write())) {
                  patterns.add(preset.read());
               }
            }

            return patterns;
         } else {
            return List.of();
         }
      }

      private void stop_V_6() {
         int limit = this.queue();
         int count = Math.min(this.write_Set.size(), limit);
         bi.write("Payload channel cap reached: " + count + "/" + limit);
      }

      private String read_Str_7() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.init_bD_BufferHelper();
         Set<String> applied = new LinkedHashSet<>(status == null ? List.of() : status.init_List());
         Set<String> wanted = this.write_Set;
         int added = 0;

         for (String channel : wanted) {
            if (!applied.contains(channel)) {
               added++;
            }
         }

         int removed = 0;

         for (String channel : applied) {
            if (!wanted.contains(channel)) {
               removed++;
            }
         }

         return added == 0 && removed == 0 ? "" : "  Pending +" + added + " / -" + removed;
      }

      private void next_V_6() {
         this.flush_V_14();
         if (!PayloadSubscriptionManager.init_Z()) {
            PayloadSubscriptionManager.init_V();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.init_Minecraft, false);
            bi.read("Registration locked. Saved, nothing sent.");
            this.put_V_7();
         } else {
            PayloadSubscriptionManager.init_V();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.init_Minecraft, false);
            PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.init_bD_BufferHelper();
            if (status != null && status.read() > 0) {
               bi.write("Payload channel cap reached: " + status.init_I() + "/" + status.load_I());
            } else {
               bi.init("Payload channels applied.");
            }

            this.put_V_7();
         }
      }

      private void prev_V_6() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.init_bD_BufferHelper();
         AutismPacketLoggerOverlay.this.init_bC.init_V_Collection(status == null ? List.of() : status.init_List());
         AutismPacketLoggerOverlay.this.init_bC.init_V();
         this.put_V_7();
         bi.init("Payload channels reverted.");
      }

      @Override
      public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
         if (this.update_Z && button == 0) {
            this.init_V_cH(new OverlayBounds((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z));
            return true;
         } else if (this.write_Z && button == 0) {
            int listTop = this.put_I_7();
            int listH = this.next_I_6();
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
               this.init_List.size() * 14, listH, this.init_I_2 + this.read_I - 7, listTop, 3, listH, this.flush_I
            );
            this.flush_I = this.init_I_I_3(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(metrics, (int)my, this.close_I), 14, metrics.flush());
            return true;
         } else {
            return this.init_k.init(mx, my, button, dx, dy) ? true : this.load_k.init(mx, my, button, dx, dy);
         }
      }

      @Override
      public boolean load_Z_D_3(double mx, double my, int button) {
         if (button == 0 && this.update_Z) {
            this.update_Z = false;
            this.a__V();
            return true;
         } else if (button == 0 && this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.init_k.load(mx, my, button) ? true : this.load_k.load(mx, my, button);
         }
      }

      @Override
      public boolean init_Z_D_2(double mx, double my, double amount) {
         if (this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my)) {
            int listTop = this.put_I_7();
            int listH = this.next_I_6();
            int maxScroll = Math.max(0, this.init_List.size() * 14 - listH);
            this.flush_I = this.init_I_I_3(this.flush_I - (int)Math.signum(amount) * 14, 14, maxScroll);
            this.init_CacheEntry.load(this.flush_I, maxScroll);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
         if (!this.init_Z) {
            return false;
         } else {
            KeyEvent event = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);
            if (this.init_k.init_Z()) {
               return this.init_k.init_Z_KeyEvent(event);
            } else {
               return this.load_k.init_Z() ? this.load_k.init_Z_KeyEvent(event) : false;
            }
         }
      }

      @Override
      public boolean init_Z_C_I(char chr, int modifiers) {
         if (!this.init_Z) {
            return false;
         } else {
            CharacterEvent event = new CharacterEvent(chr);
            if (this.init_k.init_Z()) {
               return this.init_k.init_Z_CharacterEvent(event);
            } else {
               return this.load_k.init_Z() ? this.load_k.init_Z_CharacterEvent(event) : false;
            }
         }
      }

      @Override
      public boolean write_Z() {
         return this.init_Z && (this.init_k.init_Z() || this.load_k.init_Z());
      }

      @Override
      public void push_V() {
         this.init_k.push(false);
         this.load_k.push(false);
      }

      private record Inner_DataUtil(int init, int load, int read, int write, int update) {
      }
   }
}
