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
   private static final Minecraft MC = Minecraft.getInstance();
   private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
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
   private static final Set<String> DEFAULT_BLOCKED_S2C_PACKETS = defaultBlockedS2cPackets();
   private static final Set<String> DEFAULT_BLOCKED_PACKETS = defaultBlockedPackets();
   private static final Set<String> INVENTORY_PACKET_KEYWORDS = new HashSet<>(
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
   private static final Set<String> MOVEMENT_PACKET_KEYWORDS = new HashSet<>(
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
   private static final Set<String> PAYLOAD_PACKET_KEYWORDS = new HashSet<>(
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
   private final Font font;
   private final AutismPacketInspectOverlay inspectOverlay;
   private final AutismPacketLoggerOverlay.BlockedPacketListOverlay blockedListOverlay;
   private final AutismPacketLoggerOverlay.PayloadChannelListenerOverlay channelListenerOverlay;
   private final PayloadChannelFilterManager channelFilters;
   private final PayloadRegistrationManager payloadRegistrations;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final OverlayContextMenu<AutismPacketLoggerOverlay.LoggedPacketEntry> contextMenu;
   private int panelWidth = 360;
   private int expandedHeight = 250;
   private int panelHeight = 250;
   private volatile boolean paused = true;
   private boolean configurationOnly = false;
   private boolean dragging;
   private double dragOffsetX;
   private double dragOffsetY;
   private boolean flush_Z;
   private int entryScroll;
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroller = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private boolean scrollbarDragging;
   private int entryScrollbarGrabOffset;
   private String searchQuery = "";
   private final ThemedTextInputBox searchField;
   private AutismPacketLoggerOverlay.LogCategory category = AutismPacketLoggerOverlay.LogCategory.ALL;
   private boolean open_Z = true;
   private int directionFilter = 0;
   private boolean payloadFilteredOnly;
   private final Set<String> blockedPackets = new LinkedHashSet<>();
   private final Set<String> blockedKeys = new HashSet<>();
   private final Map<Class<?>, Boolean> blockedClassCache = new HashMap<>();
   private boolean push_Z;
   private final Deque<AutismPacketLoggerOverlay.LoggedPacketEntry> allBuffer = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.LoggedPacketEntry> inventoryBuffer = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.LoggedPacketEntry> movementBuffer = new ArrayDeque<>();
   private final Deque<AutismPacketLoggerOverlay.LoggedPacketEntry> payloadBuffer = new ArrayDeque<>();
   private final List<AutismPacketLoggerOverlay.LoggedPacketEntry> pendingEntries = new ArrayList<>();
   private final PacketStateDeltaTracker stateDeltaTracker = new PacketStateDeltaTracker();
   private volatile int currentTick;
   private long lastFlushTime;
   private List<AutismPacketLoggerOverlay.DisplayRow> displayRows = new ArrayList<>();
   private volatile boolean rowsDirty = true;
   private final Set<String> expandedGroups = new HashSet<>();
   private static long nextEntryId = 0L;

   public AutismPacketLoggerOverlay(Font textRenderer) {
      super("AutismPacketLoggerOverlay", 360, 250);
      this.font = textRenderer;
      this.panelWidth = this.preferredWidth();
      this.expandedHeight = this.set_I_15();
      this.panelHeight = this.expandedHeight;
      this.windowX = 200;
      this.windowY = 40;
      this.searchField = new ThemedTextInputBox(MC, textRenderer, 0, 0, this.searchFieldWidth(), this.reset_I_5(), false);
      this.searchField.setPlaceholder(Component.literal("Search..."));
      this.searchField.setMaxLength(160);
      this.searchField.setChangedListener(value -> {
         this.searchQuery = value == null ? "" : value;
         this.rowsDirty = true;
      });
      this.inspectOverlay = new AutismPacketInspectOverlay(textRenderer);
      this.blockedListOverlay = new AutismPacketLoggerOverlay.BlockedPacketListOverlay();
      this.channelFilters = new PayloadChannelFilterManager();
      this.payloadRegistrations = new PayloadRegistrationManager();
      this.channelListenerOverlay = new AutismPacketLoggerOverlay.PayloadChannelListenerOverlay();
      this.contextMenu = new OverlayContextMenu<>(this.styleTheme, textRenderer, this::contextMenuItems, this.rowHeight());
      this.setActiveContextMenu(this.contextMenu);
      OverlayManager.getInstance().register(this.inspectOverlay);
      OverlayManager.getInstance().register(this.blockedListOverlay);
      OverlayManager.getInstance().register(this.channelListenerOverlay);
      this.loadBlockedFromConfig();
      this.paused = !AutismConfig.getGlobal().packetLoggerCapturing;
   }

   @Override
   protected boolean supportsPinning() {
      return true;
   }

   public void saveState() {
      this.saveBounds();
   }

   public synchronized String debugSummary() {
      return "capturing="
         + !this.paused
         + " all="
         + this.allBuffer.size()
         + " inv="
         + this.inventoryBuffer.size()
         + " move="
         + this.movementBuffer.size()
         + " payload="
         + this.payloadBuffer.size()
         + " pending="
         + this.pendingEntries.size();
   }

   public synchronized List<AutismPacketLoggerOverlay.LoggedPacketEntry> queryEntries(String category, int offset, int limit) {
      String key = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
      Deque var10002;
      switch (key) {
         case "INVENTORY":
         case "INV":
            var10002 = this.inventoryBuffer;
            break;
         case "MOVEMENT":
         case "MOVE":
            var10002 = this.movementBuffer;
            break;
         case "PAYLOAD":
            var10002 = this.payloadBuffer;
            break;
         default:
            var10002 = this.allBuffer;
      }

      List<AutismPacketLoggerOverlay.LoggedPacketEntry> newestFirst = new ArrayList<>(var10002);
      Collections.reverse(newestFirst);
      int from = Math.max(0, Math.min(offset, newestFirst.size()));
      int to = limit < 0 ? newestFirst.size() : Math.min(newestFirst.size(), from + limit);
      return new ArrayList<>(newestFirst.subList(from, to));
   }

   public synchronized void applyFilters(String searchText, String direction, String category) {
      if (searchText != null) {
         this.searchField.setValue(searchText);
         this.searchQuery = this.searchField.getValue();
      }

      if (direction != null) {
         switch (direction.trim().toUpperCase(Locale.ROOT)) {
            case "C2S":
               this.directionFilter = 1;
               break;
            case "S2C":
               this.directionFilter = 2;
               break;
            case "BOTH":
            default:
               this.directionFilter = 0;
         }
      }

      if (category != null) {
         switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "INVENTORY":
            case "INV":
               this.category = AutismPacketLoggerOverlay.LogCategory.INVENTORY;
               break;
            case "MOVEMENT":
            case "MOVE":
               this.category = AutismPacketLoggerOverlay.LogCategory.MOVEMENT;
               break;
            case "PAYLOAD":
               this.category = AutismPacketLoggerOverlay.LogCategory.PAYLOAD;
               break;
            case "ALL":
            default:
               this.category = AutismPacketLoggerOverlay.LogCategory.ALL;
         }
      }

      this.rowsDirty = true;
   }

   public synchronized void resetFilters() {
      this.searchField.setValue("");
      this.searchQuery = "";
      this.directionFilter = 0;
      this.category = AutismPacketLoggerOverlay.LogCategory.ALL;
      this.payloadFilteredOnly = false;
      this.rowsDirty = true;
   }

   public synchronized JsonObject stateJson() {
      JsonObject state = new JsonObject();
      state.addProperty("paused", this.paused);
      state.addProperty("configurationOnly", this.configurationOnly);
      JsonObject buffers = new JsonObject();
      buffers.addProperty("all", this.allBuffer.size());
      buffers.addProperty("inventory", this.inventoryBuffer.size());
      buffers.addProperty("movement", this.movementBuffer.size());
      buffers.addProperty("payload", this.payloadBuffer.size());
      buffers.addProperty("pending", this.pendingEntries.size());
      state.add("buffers", buffers);
      JsonObject filters = new JsonObject();
      filters.addProperty("search", this.searchQuery);
      filters.addProperty("direction", this.directionFilter == 1 ? "C2S" : (this.directionFilter == 2 ? "S2C" : "Both"));
      filters.addProperty("category", this.category.name());
      filters.addProperty("payloadFilteredOnly", this.payloadFilteredOnly);
      state.add("filters", filters);
      state.addProperty("blockedCount", this.blockedPackets.size());
      return state;
   }

   public synchronized void unblock(String name) {
      this.removeBlocked(name);
   }

   public synchronized void clearEntries() {
      this.clearEntriesLocked();
   }

   public void restoreState() {
      this.loadBounds();
      this.channelFilters.reloadFromConfig();
      this.payloadRegistrations.reloadFromConfig();
      this.rowsDirty = true;
   }

   public static boolean isSavedVisible() {
      OverlayBounds layout = AutismSessionState.getInstance().getOverlayBounds("AutismPacketLoggerOverlay");
      return layout != null && layout.visible;
   }

   @Override
   public int getMinWidth() {
      return this.preferredWidth();
   }

   @Override
   public int getMinHeight() {
      return 14 + this.clear_I_6() + this.toolbarHeight() + 12 + this.rowHeight() * 2;
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.getMinHeight() + this.rowHeight();
   }

   @Override
   protected void onSizeChanged() {
      if (!this.hasCustomStoredSize()) {
         this.panelWidth = this.preferredWidth();
         this.expandedHeight = this.set_I_15();
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.windowX, this.windowY, this.panelWidth, this.panelHeight, this.windowVisible, this.windowCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.panelWidth = clamped.width;
         this.expandedHeight = clamped.height;
         this.windowVisible = clamped.visible;
         this.windowCollapsed = clamped.collapsed;
      }
   }

   public void setCurrentTick(int t) {
      this.currentTick = t;
   }

   public boolean isPaused() {
      return this.paused;
   }

   public void load(boolean configurationOnly) {
      boolean leavingOfflineSetup = this.configurationOnly && !configurationOnly;
      this.configurationOnly = configurationOnly;
      if (configurationOnly) {
         this.contextMenu.close();
      }

      if (leavingOfflineSetup) {
         this.inspectOverlay.clearInspection();
         this.blockedListOverlay.setVisible(false);
         this.channelListenerOverlay.setVisible(false);
      }
   }

   public synchronized void read(boolean paused) {
      if (this.paused != paused) {
         if (paused) {
            this.drainPending();
         } else {
            this.lastFlushTime = System.currentTimeMillis();
         }

         this.paused = paused;
         this.rowsDirty = true;
         PacketCaptureState.write();
         AutismConfig config = AutismConfig.getGlobal();
         if (config.packetLoggerCapturing != !paused) {
            config.packetLoggerCapturing = !paused;
            config.save();
         }
      }
   }

   public void logPacket(Packet<?> packet, String direction) {
      this.logPacket(packet, direction, false, false);
   }

   public void logPayloadPacket(Packet<?> packet, String direction) {
      this.logPacket(packet, direction, true, true);
   }

   public void logPayload(long timestampMs, int tick, String direction, Class<?> packetClass, AutismPayloadSupport.PayloadEntry payloadSnapshot) {
      if (payloadSnapshot != null) {
         synchronized (this) {
            String name = packetClass != null && Packet.class.isAssignableFrom(packetClass) ? packetName(packetClass) : "Custom Payload";
            boolean isInventory = containsAny(name, INVENTORY_PACKET_KEYWORDS);
            boolean isMovement = containsAny(name, MOVEMENT_PACKET_KEYWORDS);
            AutismPacketLoggerOverlay.LoggedPacketEntry e = new AutismPacketLoggerOverlay.LoggedPacketEntry(
               timestampMs, tick, direction, name, packetClass, null, isInventory, isMovement, true, null, null, payloadSnapshot, PacketStateDeltaTracker.EMPTY_CAPTURE
            );
            this.pendingEntries.add(e);
            this.flushPendingBatch(true);
         }
      }
   }

   private static String packetName(Class<?> packetClass) {
      try {
         return PacketNameResolver.nameOfClass((Class<? extends Packet<?>>)packetClass);
      } catch (Throwable ignored) {
         return packetClass == null ? "Custom Payload" : packetClass.getSimpleName();
      }
   }

   private void logPacket(Packet<?> packet, String direction, boolean ignorePaused, boolean payloadOnly) {
      if (packet != null && (ignorePaused || !this.paused)) {
         synchronized (this) {
            if (ignorePaused || !this.paused) {
               Class<?> cls = packet.getClass();
               String name = PacketNameResolver.directionalName(packet, direction);
               boolean isInventory = containsAny(name, INVENTORY_PACKET_KEYWORDS);
               boolean isMovement = containsAny(name, MOVEMENT_PACKET_KEYWORDS);
               boolean isPayload = isPayloadPacket(cls, name);
               if ((payloadOnly && isPayload || !this.isBlocked(cls, name)) && (!payloadOnly || isPayload)) {
                  PacketStateDeltaTracker.Inner_DataUtil packetContext = this.stateDeltaTracker.capture(packet, direction);
                  AutismPacketLoggerOverlay.CaptureContext captureContext = captureContext(packet);
                  AutismPayloadSupport.PayloadEntry payloadSnapshot = isPayload ? AutismPayloadSupport.inspectPacket(packet, direction) : null;
                  if (payloadSnapshot != null) {
                     PayloadChannelFilterManager.Inner_StreamUtil match = this.channelFilters.findMatchingRuleForEntry(payloadSnapshot, direction);
                     if (match != null) {
                        PayloadNotificationTracker.init(payloadSnapshot.channel(), direction, match);
                     }
                  }

                  AutismPacketLoggerOverlay.LoggedPacketEntry e = new AutismPacketLoggerOverlay.LoggedPacketEntry(
                     System.currentTimeMillis(),
                     this.currentTick,
                     direction,
                     name,
                     cls,
                     packet,
                     isInventory,
                     isMovement,
                     isPayload,
                     captureContext.blockState(),
                     captureContext.load(),
                     payloadSnapshot,
                     packetContext
                  );
                  this.pendingEntries.add(e);
                  this.flushPendingBatch(false);
               }
            }
         }
      }
   }

   private static AutismPacketLoggerOverlay.CaptureContext captureContext(Packet<?> packet) {
      if (packet != null && MC != null && MC.isSameThread()) {
         try {
            if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
               return new AutismPacketLoggerOverlay.CaptureContext(blockName(actionPacket.getPos()), null);
            }

            if (packet instanceof ServerboundUseItemOnPacket interactBlockPacket) {
               return new AutismPacketLoggerOverlay.CaptureContext(blockName(interactBlockPacket.hitResult().getBlockPos()), null);
            }

            if (packet instanceof ServerboundContainerClosePacket) {
               return new AutismPacketLoggerOverlay.CaptureContext(null, screenTitle());
            }
         } catch (Throwable var2) {
         }

         return AutismPacketLoggerOverlay.CaptureContext.EMPTY;
      } else {
         return AutismPacketLoggerOverlay.CaptureContext.EMPTY;
      }
   }

   private static String blockName(BlockPos pos) {
      if (pos != null && MC.level != null) {
         try {
            BlockState state = MC.level.getBlockState(pos);
            return state == null ? null : BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + state;
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String screenTitle() {
      if (MC.gui.screen() == null) {
         return null;
      }

      try {
         String title = MC.gui.screen().getTitle() == null ? "" : MC.gui.screen().getTitle().getString().trim();
         String className = MC.gui.screen().getClass().getSimpleName();
         if (title.isEmpty()) {
            return className;
         } else {
            return className != null && !className.isBlank() ? title + " [" + className + "]" : title;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   public boolean isBlocked(Class<?> cls) {
      return this.isBlockedCached(cls);
   }

   private static boolean containsAny(String name, Set<String> set) {
      for (String s : set) {
         if (name.contains(s)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isPayloadPacket(Class<?> cls, String name) {
      if (containsAny(name, PAYLOAD_PACKET_KEYWORDS)) {
         return true;
      }

      if (cls == null) {
         return false;
      }

      String simpleName = cls.getSimpleName();
      if (containsAny(simpleName, PAYLOAD_PACKET_KEYWORDS)) {
         return true;
      }

      String className = cls.getName();
      return containsAny(className, PAYLOAD_PACKET_KEYWORDS);
   }

   private static Set<String> defaultBlockedPackets() {
      Set<String> keys = new LinkedHashSet<>();
      addKeys(
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
      keys.addAll(DEFAULT_BLOCKED_S2C_PACKETS);
      return Collections.unmodifiableSet(keys);
   }

   private static Set<String> defaultBlockedS2cPackets() {
      Set<String> keys = new LinkedHashSet<>();
      addKeys(
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

   private static void addKeys(Set<String> keys, String... names) {
      for (String name : names) {
         addKey(keys, name);
      }
   }

   private static void addKey(Set<String> keys, String name) {
      if (name != null && !name.isBlank()) {
         keys.add(name.trim());
      }
   }

   private static String normalizeKey(String value) {
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

   private void rebuildBlockedKeys() {
      this.blockedKeys.clear();
      this.blockedClassCache.clear();

      for (String name : this.blockedPackets) {
         if (name != null && !name.isBlank()) {
            this.blockedKeys.add(normalizeKey(name));
            if (name.endsWith("Packet")) {
               this.blockedKeys.add(normalizeKey(name.substring(0, name.length() - 6)));
            }
         }
      }
   }

   private boolean isBlocked(Class<?> cls, String friendlyName) {
      if (this.blockedKeys.isEmpty()) {
         return false;
      } else {
         return this.blockedKeys.contains(normalizeKey(friendlyName)) ? true : this.isBlockedCached(cls);
      }
   }

   private boolean isBlockedCached(Class<?> cls) {
      if (cls != null && !this.blockedKeys.isEmpty()) {
         Boolean cached = this.blockedClassCache.get(cls);
         if (cached != null) {
            return cached;
         }

         boolean blocked = this.computeBlocked(cls);
         this.blockedClassCache.put(cls, blocked);
         return blocked;
      } else {
         return false;
      }
   }

   private boolean computeBlocked(Class<?> cls) {
      if (this.blockedKeys.contains(normalizeKey(cls.getSimpleName()))) {
         return true;
      }

      if (this.blockedKeys.contains(normalizeKey(cls.getName()))) {
         return true;
      }

      if (Packet.class.isAssignableFrom(cls)) {
         Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)cls;
         if (this.blockedKeys.contains(normalizeKey(PacketNameResolver.nameOfClass(packetClass)))) {
            return true;
         }
      }

      return false;
   }

   private void loadBlockedFromConfig() {
      AutismConfig config = AutismConfig.getGlobal();
      this.blockedPackets.clear();
      if (config.packetLoggerBlockedInit) {
         if (config.packetLoggerBlocked != null) {
            this.blockedPackets.addAll(config.packetLoggerBlocked);
         }

         if (config.packetLoggerBlockedDefaultsVersion < 2) {
            this.blockedPackets.addAll(DEFAULT_BLOCKED_S2C_PACKETS);
            config.packetLoggerBlocked = new ArrayList<>(this.blockedPackets);
            config.packetLoggerBlockedDefaultsVersion = 2;
            config.save();
         }
      } else {
         this.blockedPackets.addAll(DEFAULT_BLOCKED_PACKETS);
         config.packetLoggerBlocked = new ArrayList<>(this.blockedPackets);
         config.packetLoggerBlockedInit = true;
         config.packetLoggerBlockedDefaultsVersion = 2;
         config.save();
      }

      this.rebuildBlockedKeys();
   }

   private void saveBlockedToConfig() {
      AutismConfig config = AutismConfig.getGlobal();
      config.packetLoggerBlocked = new ArrayList<>(this.blockedPackets);
      config.packetLoggerBlockedInit = true;
      config.packetLoggerBlockedDefaultsVersion = 2;
      config.save();
   }

   public synchronized void load(String name) {
      if (name != null && !name.isBlank()) {
         if (this.blockedPackets.add(name.trim())) {
            this.rebuildBlockedKeys();
            this.saveBlockedToConfig();
         }

         this.purgeBlockedEntries();
      }
   }

   private synchronized void removeBlocked(String name) {
      if (this.blockedPackets.remove(name)) {
         this.rebuildBlockedKeys();
         this.saveBlockedToConfig();
      }
   }

   private synchronized void init_V_Class_Str(Class<?> cls, String friendlyName) {
      Set<String> targets = new HashSet<>();
      if (friendlyName != null && !friendlyName.isBlank()) {
         targets.add(normalizeKey(friendlyName));
      }

      if (cls != null) {
         targets.add(normalizeKey(cls.getSimpleName()));
         targets.add(normalizeKey(cls.getName()));
      }

      for (String t : new ArrayList<>(targets)) {
         if (t.endsWith("packet")) {
            targets.add(t.substring(0, t.length() - 6));
         }
      }

      targets.remove("");
      if (!targets.isEmpty()) {
         boolean changed = this.blockedPackets
            .removeIf(
               raw -> {
                  if (raw == null) {
                     return false;
                  } else {
                     return targets.contains(normalizeKey(raw))
                        ? true
                        : raw.endsWith("Packet") && targets.contains(normalizeKey(raw.substring(0, raw.length() - 6)));
                  }
               }
            );
         if (changed) {
            this.rebuildBlockedKeys();
            this.saveBlockedToConfig();
         }
      }
   }

   private synchronized void resetBlockedToDefaults() {
      this.blockedPackets.clear();
      this.blockedPackets.addAll(DEFAULT_BLOCKED_PACKETS);
      this.rebuildBlockedKeys();
      this.saveBlockedToConfig();
      this.purgeBlockedEntries();
   }

   private synchronized void clearBlocked() {
      this.blockedPackets.clear();
      this.rebuildBlockedKeys();
      this.saveBlockedToConfig();
   }

   private synchronized void purgeBlockedEntries() {
      this.allBuffer.removeIf(e -> this.isBlocked(e.packetClass, e.name));
      this.inventoryBuffer.removeIf(e -> this.isBlocked(e.packetClass, e.name));
      this.movementBuffer.removeIf(e -> this.isBlocked(e.packetClass, e.name));
      this.payloadBuffer.removeIf(e -> this.isBlocked(e.packetClass, e.name));
      this.rowsDirty = true;
   }

   public static boolean isPayloadPacket(Packet<?> packet) {
      if (packet == null) {
         return false;
      }

      if (AutismPayloadSupport.extractPayload(packet) != null) {
         return true;
      }

      Class<?> cls = packet.getClass();
      return isPayloadPacket(cls, PacketNameResolver.directionalName(packet, ""));
   }

   private static void appendCapped(Deque<AutismPacketLoggerOverlay.LoggedPacketEntry> buf, AutismPacketLoggerOverlay.LoggedPacketEntry e, int cap) {
      buf.addLast(e);

      while (buf.size() > cap) {
         buf.removeFirst();
      }
   }

   private void reset_V_5() {
      synchronized (this) {
         this.flushPendingBatch(false);
      }
   }

   private void flushPendingBatch(boolean force) {
      long now = System.currentTimeMillis();
      if (!force && this.pendingEntries.size() < 1600) {
         if (this.pendingEntries.isEmpty()) {
            return;
         }

         if (this.lastFlushTime != 0L && now - this.lastFlushTime < 500L) {
            return;
         }
      }

      this.drainPending();
      this.lastFlushTime = now;
   }

   private void flushPending() {
      synchronized (this) {
         this.drainPending();
         this.lastFlushTime = System.currentTimeMillis();
      }
   }

   private void drainPending() {
      if (!this.pendingEntries.isEmpty()) {
         for (AutismPacketLoggerOverlay.LoggedPacketEntry e : this.pendingEntries) {
            appendCapped(this.allBuffer, e, 800);
            if (e.inventoryPacket) {
               appendCapped(this.inventoryBuffer, e, 400);
            }

            if (e.movementPacket) {
               appendCapped(this.movementBuffer, e, 200);
            }

            if (e.payloadPacket) {
               appendCapped(this.payloadBuffer, e, 400);
            }
         }

         this.pendingEntries.clear();
         this.rowsDirty = true;
      }
   }

   @Override
   public void setVisible(boolean v) {
      this.windowVisible = v;
      if (v) {
         this.entryScroll = 0;
         this.scroller.jumpTo(0, 0);
         this.rowsDirty = true;
         this.contextMenu.close();
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.inspectOverlay.clearInspection();
      }

      this.saveBounds();
   }

   public void toggleVisible() {
      this.setVisible(!this.windowVisible);
   }

   @Override
   public boolean isVisible() {
      return this.windowVisible;
   }

   @Override
   public boolean isCollapsed() {
      return this.windowCollapsed;
   }

   @Override
   public void setCollapsed(boolean c) {
      if (this.windowCollapsed != c) {
         this.windowCollapsed = c;
         this.dragging = false;
         this.flush_Z = false;
         this.scrollbarDragging = false;
         if (c) {
            this.clearFocusAndDismissMenu();
            this.contextMenu.close();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.searchField != null && this.searchField.isFocused();
   }

   @Override
   public void clearInputFocus() {
      if (this.searchField != null) {
         this.searchField.setFocused(false);
      }
   }

   @Override
   public int getZPriority() {
      return 10;
   }

   private void drawText(GuiGraphicsExtractor context, String text, autismclient.gui.f.b.l tone, int color, int x, int y) {
      autismclient.gui.f.b.k.init(context, this.font, text, this.styleTheme.init_Identifier_l(tone), color, x, y, false);
   }

   @Override
   public boolean containsPoint(double mx, double my) {
      if (!this.windowVisible) {
         return false;
      }

      int panelHeight = this.windowCollapsed ? 14 : this.panelHeight;
      OverlayBounds bounds = new OverlayBounds(this.windowX, this.windowY, this.panelWidth, panelHeight, this.windowVisible, this.windowCollapsed);
      int h = this.renderedHeight(bounds, this.windowCollapsed);
      boolean overPanel = mx >= this.windowX && mx <= this.windowX + this.panelWidth && my >= this.windowY && my <= this.windowY + h;
      return overPanel | this.contextMenu.init(mx, my);
   }

   @Override
   public boolean isOverHeader(double mx, double my) {
      return !this.windowVisible
         ? false
         : mx >= this.windowX && mx <= this.windowX + this.panelWidth && my >= this.windowY && my <= this.windowY + 14 && !this.isOverHeaderControl(mx, my, this.getBounds());
   }

   @Override
   public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.reset_V_5();
         if (this.rowsDirty) {
            this.rebuildDisplayRows();
            this.rowsDirty = false;
         }

         OverlayBounds clamped = this.clampToScreen(
            this, new OverlayBounds(this.windowX, this.windowY, this.resolveWindowWidth(this.panelWidth, this.getMinWidth()), this.preferredHeight(), this.windowVisible, this.windowCollapsed)
         );
         this.windowX = clamped.x;
         this.windowY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         int ph = this.panelHeight;
         int total = 0;

         for (AutismPacketLoggerOverlay.DisplayRow r : this.displayRows) {
            total += r.kind == AutismPacketLoggerOverlay.DisplayRowKind.GROUP ? r.count : 1;
         }

         int bodyMx = mx;
         int bodyMy = my;
         if (this.contextMenu.init(mx, my)) {
            bodyMx = -10000;
            bodyMy = -10000;
         }

         String title = "Packet Logger";
         OverlayBounds bounds = new OverlayBounds(this.windowX, this.windowY, this.panelWidth, ph, this.windowVisible, this.windowCollapsed);
         this.renderWindowFrame(ctx, bodyMx, bodyMy, bounds, title, this.windowCollapsed, this.dragging);
         this.renderPinButton(ctx, bodyMx, bodyMy, bounds);
         boolean clipBody = this.beginBodyClip(ctx, bounds, this.windowCollapsed);
         if (!clipBody) {
            this.renderInactiveDim(ctx, bounds, this.windowCollapsed, this.dragging);
         } else {
            try {
               int tabY = this.windowY + 14 + 2;
               this.renderCategoryTabs(ctx, bodyMx, bodyMy, tabY, total);
               int filterY = tabY + this.clear_I_6() + 2;
               this.renderToolbar(ctx, bodyMx, bodyMy, filterY);
               int contentY = filterY + this.toolbarHeight() + 2;
               int contentEndY = contentY + this.listViewHeight();
               if (this.displayRows.isEmpty()) {
                  this.drawText(
                     ctx, "No packets matching filters", autismclient.gui.f.b.l.write, UiColorPalette.textSecondary(), this.windowX + 10, this.rowTextY(contentY)
                  );
               } else {
                  int contentHeight = this.displayRows.size() * this.rowHeight();
                  int viewHeight = this.listViewHeight();
                  int maxScroll = Math.max(0, contentHeight - viewHeight);
                  this.entryScroll = this.quantizeScrollOffset(this.entryScroll, this.rowHeight(), maxScroll);
                  this.scroller.setOffset(this.entryScroll, maxScroll);
                  int drawScroll = this.scroller.tickAndClamp(delta, maxScroll);
                  autismclient.gui.ui.UiClipStack.getInstance()
                     .push(ctx, autismclient.gui.f.NetHelper.init(this.windowX, contentY, this.panelWidth, contentEndY - contentY));
                  int drawBase = contentY - drawScroll;

                  for (int i = 0; i < this.displayRows.size(); i++) {
                     int ey = drawBase + i * this.rowHeight();
                     if (ey + this.rowHeight() > contentY && ey < contentEndY) {
                        AutismPacketLoggerOverlay.DisplayRow row = this.displayRows.get(i);
                        if (row.kind == AutismPacketLoggerOverlay.DisplayRowKind.GROUP) {
                           this.renderGroupRow(ctx, row, ey, bodyMx, bodyMy);
                        } else {
                           this.renderEntryRow(ctx, row.entry, ey, bodyMx, bodyMy);
                        }
                     }
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, scrollbarMetrics, scrollbarMetrics.containsTrack(bodyMx, bodyMy), this.scrollbarDragging);
               }
            } finally {
               this.endBodyClip(ctx, clipBody);
               this.renderInactiveDim(ctx, bounds, this.windowCollapsed, this.dragging);
            }

            if (this.contextMenu.isOpen()) {
               ctx.nextStratum();
               this.contextMenu.init(ctx, mx, my);
            }
         }
      }
   }

   private void renderCategoryTabs(GuiGraphicsExtractor ctx, int mx, int my, int y, int total) {
      int x = this.windowX + 4;

      for (AutismPacketLoggerOverlay.LogCategory cat : AutismPacketLoggerOverlay.LogCategory.values()) {
         int w = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, cat.init_Str, 5, 32, 64);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.font, x, y, w, this.clear_I_6(), cat.init_Str, this.category == cat, mx, my);
         x += w + 2;
      }

      String summary = this.paused ? "Paused  " + total : Integer.toString(total);
      if (this.category == AutismPacketLoggerOverlay.LogCategory.PAYLOAD) {
         PayloadSubscriptionManager.Inner_BufferHelper subscriptionStatus = PayloadSubscriptionManager.status();
         String channelStatus = subscriptionStatus == null ? "" : subscriptionStatus.describe();
         if (!channelStatus.isBlank()) {
            summary = summary + "  " + channelStatus;
         }
      }

      int summaryColor = this.paused ? this.styleTheme.load(autismclient.gui.f.b.l.write) : UiColorPalette.textBody();
      int summaryWidth = autismclient.gui.f.b.k.init(
         this.font, summary, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write), summaryColor
      );
      int summaryX = this.windowX + this.panelWidth - 6 - summaryWidth;
      int minSummaryX = x + 4;
      if (summaryX >= minSummaryX) {
         int summaryY = UiLayoutMath.centerTextY(y, this.clear_I_6(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.write), this.styleTheme.flush());
         this.drawText(ctx, summary, autismclient.gui.f.b.l.write, summaryColor, summaryX, summaryY);
      }
   }

   private int listTopY() {
      return this.windowY + 14 + 2 + this.clear_I_6() + 2 + this.toolbarHeight() + 2;
   }

   private int listViewHeight() {
      int rawHeight = Math.max(0, this.panelHeight - 14 - this.clear_I_6() - this.toolbarHeight() - 8 - this.run_I_17());
      return this.alignHeightToStep(rawHeight, this.rowHeight());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      int contentHeight = this.displayRows.size() * this.rowHeight();
      int viewHeight = this.listViewHeight();
      int maxScroll = Math.max(0, contentHeight - viewHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight, viewHeight, this.windowX + this.panelWidth - 5, this.listTopY(), 3, viewHeight, this.scroller.tickAndClamp(0.0F, maxScroll)
      );
   }

   private String captureButtonLabel() {
      return this.configurationOnly ? (this.paused ? "Auto OFF" : "Auto ON") : (this.paused ? "Start" : "Stop");
   }

   private String[] toolbarLabels() {
      return new String[]{this.captureButtonLabel(), "Clear", "Copy", this.open_Z ? "Group" : "Ungrp", "Blocked", "Channels", "Payload"};
   }

   private AutismPacketLoggerOverlay.FilterColumnLayout toolbarLayout() {
      String[] labels = this.toolbarLabels();
      int[] widths = new int[labels.length];
      int total = 0;

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.buttonWidth(labels[i]);
         total += widths[i];
      }

      if (total + 2 * (labels.length - 1) <= this.panelWidth - 8) {
         return new AutismPacketLoggerOverlay.FilterColumnLayout(labels, widths, 2);
      }

      int[] legacy = new int[labels.length];
      legacy[0] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(
         this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[0], 5, 38, this.configurationOnly ? 66 : 54
      );
      legacy[1] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[1], 5, 34, 54);
      legacy[2] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[2], 5, 34, 52);
      legacy[3] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[3], 5, 38, 58);
      legacy[4] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[4], 5, 54, 72);
      legacy[5] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[5], 5, 58, 78);
      legacy[6] = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, labels[6], 5, 44, 64);
      return new AutismPacketLoggerOverlay.FilterColumnLayout(labels, legacy, 2);
   }

   private void renderToolbar(GuiGraphicsExtractor ctx, int mx, int my, int y) {
      int gap = 2;
      int row1Y = y;
      int row2Y = y + this.reset_I_5() + this.start_I_12();
      int x = this.windowX + 4;
      AutismPacketLoggerOverlay.FilterColumnLayout row1 = this.toolbarLayout();
      this.renderToggleButton(ctx, x, row1Y, row1.widths[0], this.reset_I_5(), row1.labels[0], !this.paused, "packet-logger:capture", mx, my);
      x += row1.widths[0] + row1.gap;
      this.renderButton(
         ctx, x, row1Y, row1.widths[1], this.reset_I_5(), row1.labels[1], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.widths[1] + row1.gap;
      this.renderButton(
         ctx, x, row1Y, row1.widths[2], this.reset_I_5(), row1.labels[2], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.widths[2] + row1.gap;
      this.renderToggleButton(ctx, x, row1Y, row1.widths[3], this.reset_I_5(), row1.labels[3], this.open_Z, "packet-logger:grouping", mx, my);
      x += row1.widths[3] + row1.gap;
      this.renderButton(
         ctx, x, row1Y, row1.widths[4], this.reset_I_5(), row1.labels[4], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.widths[4] + row1.gap;
      this.renderButton(
         ctx, x, row1Y, row1.widths[5], this.reset_I_5(), row1.labels[5], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x += row1.widths[5] + row1.gap;
      this.renderButton(
         ctx, x, row1Y, row1.widths[6], this.reset_I_5(), row1.labels[6], autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mx, my
      );
      x = this.windowX + 4;
      int sw = this.prev_I_4();
      this.searchField.setX(x);
      this.searchField.setY(row2Y);
      this.searchField.setWidth(sw);
      this.searchField.setHeight(this.reset_I_5());
      if (!Objects.equals(this.searchField.getValue(), this.searchQuery)) {
         this.searchField.setValue(this.searchQuery);
      }

      this.searchField.render(ctx, mx, my, 0.0F);
      x += sw + 3;
      String[] dirLabels = new String[]{"Both", "C2S", "S2C"};

      for (int d = 0; d < 3; d++) {
         int bw = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, dirLabels[d], 4, 30, 48);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.font, x, row2Y, bw, this.reset_I_5(), dirLabels[d], this.directionFilter == d, mx, my);
         x += bw + gap;
      }

      if (this.category == AutismPacketLoggerOverlay.LogCategory.PAYLOAD) {
         String listenLabel = this.payloadFilteredOnly ? "Filtered" : "All";
         int listenW = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, listenLabel, 4, 42, 62);
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.font, x, row2Y, listenW, this.reset_I_5(), listenLabel, this.payloadFilteredOnly, mx, my);
      }
   }

   private void renderButton(
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
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.font, x, y, w, h, label, variant, active, mx, my);
   }

   private void renderToggleButton(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean enabled, String animationKey, int mx, int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(ctx, this.font, x, y, w, h, label, enabled, animationKey, mx, my);
   }

   private void renderGroupRow(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.DisplayRow row, int y, int mx, int my) {
      int x = this.windowX + 4;
      boolean exp = this.expandedGroups.contains(row.groupKey);
      boolean hov = mx >= this.windowX && mx <= this.windowX + this.panelWidth && my >= y && my < y + this.rowHeight();
      PayloadChannelFilterManager.Inner_StreamUtil listenerMatch = row.payload == null
         ? null
         : this.channelFilters.findMatchingRuleForEntry(row.payload, row.direction);
      if (listenerMatch != null) {
         ctx.fill(this.windowX + 2, y, this.windowX + this.panelWidth - 4, y + this.rowHeight(), UiColorPalette.filterRowFill(hov));
      } else if (hov) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, this.windowX + 2, y, this.panelWidth - 4, this.rowHeight(), true, false);
      }

      autismclient.gui.ui.widgets.GlyphIndicatorRenderer.init(
         ctx,
         x,
         y + (this.rowHeight() - 8) / 2,
         8,
         exp ? autismclient.gui.ui.widgets.GlyphIndicatorRenderer.Inner_DataUtil.DOWN : autismclient.gui.ui.widgets.GlyphIndicatorRenderer.Inner_DataUtil.RIGHT,
         hov ? -659730 : -1582374,
         -1204153320,
         1.0F
      );
      x += 10;
      String arrow = row.direction.equals("C2S") ? ">" : "<";
      int color = row.direction.equals("C2S") ? -12268289 : -21948;
      this.drawText(ctx, arrow, autismclient.gui.f.b.l.read, color, x, this.rowTextY(y));
      x += 12;
      String displayName = row.groupKey.contains(":") ? row.groupKey.substring(row.groupKey.indexOf(58) + 1) : row.groupKey;
      String summary = row.payload == null ? "" : AutismPayloadSupport.formatSummary(row.payload, true);
      String listenText = listenerMatch == null ? "" : " [" + listenerMatch.load() + "]";
      String line = displayName + " (x" + row.count + ")" + listenText + (summary.isBlank() ? "" : " " + summary);
      int maxW = this.panelWidth - (x - this.windowX) - 32;
      if (autismclient.gui.f.b.k.init(this.font, line, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), color) > maxW) {
         line = autismclient.gui.f.b.k.init(
            this.font, line, Math.max(1, maxW), this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), color
         );
      }

      this.drawText(ctx, line, autismclient.gui.f.b.l.read, color, x, this.rowTextY(y));
      int bx = this.windowX + this.panelWidth - 28;
      boolean hb = mx >= bx && mx <= bx + 24 && my >= y && my < y + this.rowHeight();
      this.drawText(ctx, "BLK", autismclient.gui.f.b.l.write, hb ? -48060 : UiColorPalette.textSecondary(), bx, this.rowTextY(y));
   }

   private void renderEntryRow(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.LoggedPacketEntry e, int y, int mx, int my) {
      int x = this.windowX + 4;
      boolean hov = mx >= this.windowX && mx <= this.windowX + this.panelWidth && my >= y && my < y + this.rowHeight();
      PayloadChannelFilterManager.Inner_StreamUtil listenerMatch = this.matchingChannelRule(e);
      if (listenerMatch != null) {
         ctx.fill(this.windowX + 2, y, this.windowX + this.panelWidth - 4, y + this.rowHeight(), UiColorPalette.filterRowFill(hov));
      } else if (hov) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, this.windowX + 2, y, this.panelWidth - 4, this.rowHeight(), true, false);
      }

      int color = e.direction.equals("C2S") ? -12268289 : -21948;
      this.drawText(ctx, e.direction.equals("C2S") ? ">" : "<", autismclient.gui.f.b.l.read, color, x, this.rowTextY(y));
      x += 12;
      String time = TIME_FORMAT.format(Instant.ofEpochMilli(e.timestampMs));
      this.drawText(ctx, time, autismclient.gui.f.b.l.write, -4809052, x, this.rowTextY(y));
      x += autismclient.gui.f.b.k.init(this.font, time, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write), -4809052) + 4;
      String tick = "T" + e.tick;
      this.drawText(ctx, tick, autismclient.gui.f.b.l.write, -6114880, x, this.rowTextY(y));
      x += autismclient.gui.f.b.k.init(this.font, tick, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write), -6114880) + 4;
      int maxW = this.panelWidth - (x - this.windowX) - 30;
      String name = e.name;
      if (e.payloadSnapshot != null) {
         String summary = AutismPayloadSupport.formatSummary(e.payloadSnapshot, true);
         if (!summary.isBlank()) {
            name = name + " " + summary;
         }
      }

      if (listenerMatch != null) {
         name = name + " [" + listenerMatch.load() + "]";
      }

      if (autismclient.gui.f.b.k.init(this.font, name, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.textBright()) > maxW) {
         name = autismclient.gui.f.b.k.init(
            this.font, name, Math.max(1, maxW), this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.textBright()
         );
      }

      this.drawText(ctx, name, autismclient.gui.f.b.l.read, color, x, this.rowTextY(y));
      int bx = this.windowX + this.panelWidth - 16;
      boolean hb = mx >= bx && mx <= bx + 12 && my >= y && my < y + this.rowHeight();
      this.drawText(ctx, "=", autismclient.gui.f.b.l.write, hb ? -8875 : UiColorPalette.textSecondary(), bx, this.rowTextY(y));
   }

   private void load(GuiGraphicsExtractor ctx, int mx, int my, int startY, int endY) {
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDivider(ctx, this.windowX + 4, startY, this.panelWidth - 8);
      int y = startY + 3;
      boolean hh = mx >= this.windowX + 4 && mx <= this.windowX + 150 && my >= y && my < y + this.rowHeight();
      int blockedColor = hh ? ThemePaletteManager.recolor(-30584, ThemePaletteManager.ColorRole.DANGER) : ThemePaletteManager.recolor(-39322, ThemePaletteManager.ColorRole.DANGER);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(this.windowX + 6, y + 1, 9, 9), this.push_Z, blockedColor);
      this.drawText(ctx, "Blocked (" + this.blockedPackets.size() + ")", autismclient.gui.f.b.l.load, blockedColor, this.windowX + 17, y + 1);
      int ubX = this.windowX + this.panelWidth - 58;
      boolean hua = mx >= ubX && mx <= ubX + 54 && my >= y && my < y + this.rowHeight();
      this.drawText(ctx, "CLR ALL", autismclient.gui.f.b.l.write, hua ? -12255420 : UiColorPalette.textBody(), ubX, y + 1);
      int rstX = ubX - 44;
      boolean hRst = mx >= rstX && mx <= rstX + 40 && my >= y && my < y + this.rowHeight();
      this.drawText(ctx, "RESET", autismclient.gui.f.b.l.write, hRst ? -10923 : UiColorPalette.textBody(), rstX, y + 1);
      if (this.push_Z) {
         y += this.rowHeight();

         for (String name : this.blockedPackets) {
            if (y + this.rowHeight() > endY) {
               break;
            }

            this.drawText(
               ctx,
               "  "
                  + autismclient.gui.f.b.k.init(
                     this.font, name, Math.max(1, this.panelWidth - 50), this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), -5609882
                  ),
               autismclient.gui.f.b.l.read,
               -5609882,
               this.windowX + 8,
               y + 1
            );
            int ux = this.windowX + this.panelWidth - 28;
            boolean hu = mx >= ux && mx <= ux + 24 && my >= y && my < y + this.rowHeight();
            this.drawText(ctx, "UB", autismclient.gui.f.b.l.write, hu ? -12255420 : UiColorPalette.textBody(), ux, y + 1);
            y += this.rowHeight();
         }
      }
   }

   private String[] contextMenuItems(AutismPacketLoggerOverlay.LoggedPacketEntry e) {
      boolean isC2S = e != null && "C2S".equalsIgnoreCase(e.direction);
      boolean isPayload = e != null && e.payloadPacket;
      String[] base;
      if (this.configurationOnly) {
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

   private void openContextMenu(AutismPacketLoggerOverlay.LoggedPacketEntry entry, int mouseX, int mouseY) {
      this.contextMenu.init(mouseX, mouseY, entry);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.windowVisible) {
         return false;
      }

      if (this.contextMenu.init(mouseX, mouseY, button, (entry, action, index) -> this.onContextAction(action, entry))) {
         return true;
      }

      if (button != 0 && button != 1) {
         return false;
      }

      if (mouseY >= this.windowY && mouseY <= this.windowY + 14 && mouseX >= this.windowX && mouseX <= this.windowX + this.panelWidth) {
         OverlayBounds bounds = new OverlayBounds(this.windowX, this.windowY, this.panelWidth, this.preferredHeight(), this.windowVisible, this.windowCollapsed);
         if (this.isOverPinButton(mouseX, mouseY, bounds)) {
            this.togglePinned();
            return true;
         }

         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         }

         if (button == 0) {
            this.dragging = true;
            this.flush_Z = false;
            this.dragOffsetX = mouseX - this.windowX;
            this.dragOffsetY = mouseY - this.windowY;
         }

         return true;
      } else {
         if (this.windowCollapsed) {
            return false;
         }

         int tabY = this.windowY + 14 + 2;
         int filterY = tabY + this.clear_I_6() + 2;
         int contentY = filterY + this.toolbarHeight() + 2;
         if (mouseY >= tabY && mouseY < tabY + this.clear_I_6() && button == 0) {
            int x = this.windowX + 4;

            for (AutismPacketLoggerOverlay.LogCategory cat : AutismPacketLoggerOverlay.LogCategory.values()) {
               int w = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, cat.init_Str, 5, 32, 64);
               if (mouseX >= x && mouseX < x + w) {
                  this.category = cat;
                  this.rowsDirty = true;
                  this.entryScroll = 0;
                  return true;
               }

               x += w + 2;
            }

            return true;
         } else {
            if (mouseY >= filterY && mouseY < filterY + this.toolbarHeight() && button == 0) {
               return this.handleToolbarClick(mouseX, mouseY, filterY);
            }

            if (this.searchField.isFocused()) {
               this.searchField.setFocused(false);
            }

            int contentEndY = contentY + this.listViewHeight();
            if (mouseY >= contentY && mouseY < contentEndY) {
               if (button == 0) {
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics();
                  if (scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack((int)mouseX, (int)mouseY)) {
                     this.scrollbarDragging = true;
                     this.entryScrollbarGrabOffset = Math.max(0, (int)mouseY - scrollbarMetrics.thumbY());
                     this.entryScroll = this.quantizeScrollOffset(
                        autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, (int)mouseY, this.entryScrollbarGrabOffset), this.rowHeight(), scrollbarMetrics.maxScroll()
                     );
                     this.scroller.jumpTo(this.entryScroll, scrollbarMetrics.maxScroll());
                     return true;
                  }
               }

               return this.handleListClick(mouseX, mouseY, contentY, button);
            } else {
               return false;
            }
         }
      }
   }

   private boolean handleToolbarClick(double mouseX, double mouseY, int y) {
      int gap = 2;
      int row1Y = y;
      int row2Y = y + this.reset_I_5() + this.start_I_12();
      int x = this.windowX + 4;
      AutismPacketLoggerOverlay.FilterColumnLayout row1 = this.toolbarLayout();
      if (mouseY >= row1Y && mouseY < row1Y + this.reset_I_5()) {
         for (int i = 0; i < row1.widths.length; i++) {
            int w = row1.widths[i];
            if (mouseX >= x && mouseX < x + w) {
               switch (i) {
                  case 0:
                     this.read(!this.paused);
                     break;
                  case 1:
                     this.clearEntriesLocked();
                     break;
                  case 2:
                     this.copyLogToClipboard();
                     break;
                  case 3:
                     this.open_Z = !this.open_Z;
                     this.rowsDirty = true;
                     break;
                  case 4:
                     this.openBlockedList();
                     break;
                  case 5:
                     this.openChannelListener();
                     break;
                  case 6:
                     this.openPayloadEditor();
               }

               return true;
            }

            x += w + row1.gap;
         }
      }

      x = this.windowX + 4;
      int sw = this.prev_I_4();
      if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + sw) {
         this.searchField.mouseClicked(mouseX, mouseY, 0);
         return true;
      }

      x += sw + 3;
      String[] dirLabels = new String[]{"Both", "C2S", "S2C"};

      for (int d = 0; d < 3; d++) {
         int dirW = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, dirLabels[d], 4, 30, 48);
         if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + dirW) {
            this.directionFilter = d;
            this.rowsDirty = true;
            return true;
         }

         x += dirW + gap;
      }

      if (this.category == AutismPacketLoggerOverlay.LogCategory.PAYLOAD) {
         String listenLabel = this.payloadFilteredOnly ? "Filtered" : "All";
         int listenW = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, listenLabel, 4, 42, 62);
         if (mouseY >= row2Y && mouseY < row2Y + this.reset_I_5() && mouseX >= x && mouseX < x + listenW) {
            this.payloadFilteredOnly = !this.payloadFilteredOnly;
            this.rowsDirty = true;
            return true;
         }
      }

      this.searchField.setFocused(false);
      return true;
   }

   private boolean handleListClick(double mouseX, double mouseY, int contentY, int button) {
      int idx = (int)(
         (mouseY - contentY + this.scroller.tickAndClamp(0.0F, Math.max(0, this.displayRows.size() * this.rowHeight() - this.listViewHeight()))) / this.rowHeight()
      );
      if (idx >= 0 && idx < this.displayRows.size()) {
         AutismPacketLoggerOverlay.DisplayRow row = this.displayRows.get(idx);
         if (row.kind == AutismPacketLoggerOverlay.DisplayRowKind.GROUP) {
            int bx = this.windowX + this.panelWidth - 28;
            if (button == 0 && mouseX >= bx && mouseX <= bx + 24 && row.packetClass != null) {
               String n = PacketNameResolver.nameOfClass((Class<? extends Packet<?>>)row.packetClass);
               this.load(n);
               return true;
            }

            if (button == 0) {
               if (this.expandedGroups.contains(row.groupKey)) {
                  this.expandedGroups.remove(row.groupKey);
               } else {
                  this.expandedGroups.add(row.groupKey);
               }

               this.rowsDirty = true;
               return true;
            }
         }

         if (row.kind == AutismPacketLoggerOverlay.DisplayRowKind.ENTRY && row.entry != null) {
            int menuIconX = this.windowX + this.panelWidth - 16;
            if (button == 1 || button == 0 && mouseX >= menuIconX) {
               this.openContextMenu(row.entry, (int)mouseX, (int)mouseY);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void onContextAction(String action, AutismPacketLoggerOverlay.LoggedPacketEntry e) {
      if (!this.configurationOnly || !"Send".equals(action) && !"Replay".equals(action)) {
         switch (action) {
            case "Block":
               this.load(e.name);
               AutismClientMessaging.sendPrefixed("Blocked + purged from logger: " + e.name);
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
               AutismSessionState shared = AutismSessionState.getInstance();
               Class<? extends Packet<?>> pktCls = (Class<? extends Packet<?>>)e.packetClass;
               boolean added;
               if (e.direction.equals("C2S")) {
                  added = shared.getCustomC2sPackets().add(pktCls);
               } else {
                  added = shared.getCustomS2cPackets().add(pktCls);
               }

               shared.setUseCustomPackets(true);
               AutismClientMessaging.sendPrefixed(
                  added ? "Added to custom " + e.direction + " filter: " + e.name : "Already present in custom " + e.direction + " filter: " + e.name
               );
               break;
            case "- Filter":
               AutismSessionState sharedRem = AutismSessionState.getInstance();
               Class<? extends Packet<?>> pktClsRem = (Class<? extends Packet<?>>)e.packetClass;
               boolean removed;
               if (e.direction.equals("C2S")) {
                  removed = sharedRem.getCustomC2sPackets().remove(pktClsRem);
               } else {
                  removed = sharedRem.getCustomS2cPackets().remove(pktClsRem);
               }

               AutismClientMessaging.sendPrefixed(
                  removed ? "Removed from custom " + e.direction + " filter: " + e.name : "Not present in custom " + e.direction + " filter: " + e.name
               );
               break;
            case "Replay":
               if (e.packet != null && e.direction.equals("C2S")) {
                  LoggedPacketActions.read(e);
               }
               break;
            case "Copy":
               String line = e.direction + " " + TIME_FORMAT.format(Instant.ofEpochMilli(e.timestampMs)) + " T" + e.tick + " " + e.name;
               MC.keyboardHandler.setClipboard(line);
               bi.init("Copied packet info.");
               break;
            case "Inspect":
               this.inspectOverlay.inspect(e, this.windowX + this.panelWidth + 10, this.windowY + 8);
         }
      }
   }

   private boolean write(double mouseX, double mouseY, int contentEndY) {
      int y = contentEndY + 3;
      if (mouseY >= y && mouseY < y + this.rowHeight()) {
         int ubX = this.windowX + this.panelWidth - 58;
         if (mouseX >= ubX && mouseX <= ubX + 54) {
            this.clearBlocked();
            return true;
         } else {
            int rstX = ubX - 44;
            if (mouseX >= rstX && mouseX <= rstX + 40) {
               this.resetBlockedToDefaults();
               return true;
            } else {
               this.push_Z = !this.push_Z;
               return true;
            }
         }
      } else {
         if (this.push_Z) {
            int ey = y + this.rowHeight();
            int ux = this.windowX + this.panelWidth - 28;

            for (String name : new ArrayList<>(this.blockedPackets)) {
               if (mouseX >= ux && mouseX <= ux + 24 && mouseY >= ey && mouseY < ey + this.rowHeight()) {
                  this.removeBlocked(name);
                  return true;
               }

               ey += this.rowHeight();
            }
         }

         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
      if (this.scrollbarDragging && b == 0) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics();
         this.entryScroll = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, (int)my, this.entryScrollbarGrabOffset), this.rowHeight(), scrollbarMetrics.maxScroll());
         this.scroller.jumpTo(this.entryScroll, scrollbarMetrics.maxScroll());
         return true;
      }

      if (this.dragging && b == 0) {
         OverlayBounds nextBounds = this.clampToScreen(this, new OverlayBounds((int)(mx - this.dragOffsetX), (int)(my - this.dragOffsetY), this.panelWidth, this.expandedHeight, this.windowVisible, this.windowCollapsed));
         if (nextBounds.x != this.windowX || nextBounds.y != this.windowY) {
            this.flush_Z = true;
         }

         this.windowX = nextBounds.x;
         this.windowY = nextBounds.y;
         return true;
      } else {
         return this.searchField.mouseDragged(mx, my, b, dx, dy);
      }
   }

   @Override
   public boolean mouseReleased(double mx, double my, int b) {
      if (b == 0 && this.scrollbarDragging) {
         this.scrollbarDragging = false;
         return true;
      } else if (b == 0 && this.dragging) {
         this.dragging = false;
         this.flush_Z = false;
         this.saveBounds();
         return true;
      } else {
         return this.searchField.mouseReleased(mx, my, b);
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amt) {
      if (this.windowVisible && !this.windowCollapsed) {
         int totalH = this.displayRows.size() * this.rowHeight();
         int visH = this.listViewHeight();
         int maxScroll = Math.max(0, totalH - visH);
         this.entryScroll = this.quantizeScrollOffset(this.entryScroll - (int)Math.round(amt * this.rowHeight() * 3.0), this.rowHeight(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int key, int scan, int mods) {
      return this.windowVisible && !this.windowCollapsed ? this.searchField.isFocused() && this.searchField.keyPressed(Compat263.newGlfwKeyEvent(key, scan, mods)) : false;
   }

   @Override
   public boolean charTyped(char c, int mods) {
      return this.windowVisible && !this.windowCollapsed ? this.searchField.isFocused() && this.searchField.charTyped(new CharacterEvent(c)) : false;
   }

   private synchronized List<AutismPacketLoggerOverlay.LoggedPacketEntry> categoryEntries() {
      switch (this.category) {
         case ALL:
         default:
            return new ArrayList<>(this.allBuffer);
         case INVENTORY:
            return new ArrayList<>(this.inventoryBuffer);
         case MOVEMENT:
            return new ArrayList<>(this.movementBuffer);
         case PAYLOAD:
            return new ArrayList<>(this.payloadBuffer);
      }
   }

   private PayloadChannelFilterManager.Inner_StreamUtil matchingChannelRule(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return entry != null && entry.payloadPacket && entry.payloadSnapshot != null
         ? this.channelFilters.findMatchingRuleForEntry(entry.payloadSnapshot, entry.direction)
         : null;
   }

   private String init_Str_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null) {
         return "";
      }

      PayloadChannelFilterManager.Inner_StreamUtil match = this.matchingChannelRule(entry);
      return match == null ? entry.searchKey() : entry.searchKey() + " " + match.init_Str();
   }

   private void rebuildDisplayRows() {
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> source = this.categoryEntries();
      if (source == null) {
         source = new ArrayList<>();
      }

      String ls = this.searchQuery.toLowerCase(Locale.ROOT);
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> filtered = new ArrayList<>();

      for (AutismPacketLoggerOverlay.LoggedPacketEntry e : source) {
         if (!this.isBlockedCached(e.packetClass)
            && (
               this.category != AutismPacketLoggerOverlay.LogCategory.PAYLOAD
                  || !this.payloadFilteredOnly
                  || this.matchingChannelRule(e) != null
            )
            && (ls.isEmpty() || this.init_Str_AutismPacketLogger(e).contains(ls))
            && (this.directionFilter != 1 || e.direction.equals("C2S"))
            && (this.directionFilter != 2 || e.direction.equals("S2C"))) {
            filtered.add(e);
         }
      }

      Map<String, Integer> counts = new LinkedHashMap<>();
      Map<String, String> dirs = new LinkedHashMap<>();
      Map<String, Class<?>> classes = new LinkedHashMap<>();

      for (AutismPacketLoggerOverlay.LoggedPacketEntry e : filtered) {
         String key = e.key;
         counts.merge(key, 1, Integer::sum);
         dirs.put(key, e.direction);
         classes.put(key, e.packetClass);
      }

      Set<String> groupedKeys = new HashSet<>();
      if (this.open_Z) {
         for (Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 10) {
               groupedKeys.add(entry.getKey());
            }
         }
      }

      List<AutismPacketLoggerOverlay.LoggedPacketEntry> reversed = new ArrayList<>(filtered);
      Collections.reverse(reversed);
      List<AutismPacketLoggerOverlay.DisplayRow> rows = new ArrayList<>();
      Set<String> added = new LinkedHashSet<>();

      for (AutismPacketLoggerOverlay.LoggedPacketEntry e : reversed) {
         String key = e.key;
         if (groupedKeys.contains(key) && added.add(key)) {
            AutismPacketLoggerOverlay.DisplayRow h = new AutismPacketLoggerOverlay.DisplayRow();
            h.kind = AutismPacketLoggerOverlay.DisplayRowKind.GROUP;
            h.groupKey = key;
            h.count = counts.get(key);
            h.direction = dirs.get(key);
            h.packetClass = classes.get(key);
            h.payload = e.payloadSnapshot;
            rows.add(h);
            if (this.expandedGroups.contains(key)) {
               for (AutismPacketLoggerOverlay.LoggedPacketEntry child : reversed) {
                  if (child.key.equals(key)) {
                     AutismPacketLoggerOverlay.DisplayRow r = new AutismPacketLoggerOverlay.DisplayRow();
                     r.kind = AutismPacketLoggerOverlay.DisplayRowKind.ENTRY;
                     r.entry = child;
                     rows.add(r);
                  }
               }
            }
         }
      }

      for (AutismPacketLoggerOverlay.LoggedPacketEntry e : reversed) {
         if (!groupedKeys.contains(e.key)) {
            AutismPacketLoggerOverlay.DisplayRow r = new AutismPacketLoggerOverlay.DisplayRow();
            r.kind = AutismPacketLoggerOverlay.DisplayRowKind.ENTRY;
            r.entry = e;
            rows.add(r);
         }
      }

      this.displayRows = rows;
      this.clampScroll();
   }

   private void clampScroll() {
      int totalH = this.displayRows.size() * this.rowHeight();
      int visH = this.panelHeight - 14 - this.clear_I_6() - this.toolbarHeight() - 8 - this.run_I_17();
      int maxScroll = Math.max(0, totalH - visH);
      this.entryScroll = this.quantizeScrollOffset(this.entryScroll, this.rowHeight(), maxScroll);
   }

   private int preferredHeight() {
      int rc = Math.min(this.displayRows.size(), this.maxVisibleRows());
      int chrome = 14 + this.clear_I_6() + this.toolbarHeight() + 12 + this.run_I_17();
      int content = Math.max(rc * this.rowHeight(), this.rowHeight() * 2);
      return this.resolveWindowHeight(Math.max(this.getMinHeight(), Math.min(chrome + content, Math.max(this.expandedHeight, this.set_I_15()))), this.getResizeMinHeight());
   }

   private int run_I_17() {
      return 0;
   }

   private void openBlockedList() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      this.blockedListOverlay.windowX = Math.max(4, (sw - this.blockedListOverlay.windowWidth) / 2);
      this.blockedListOverlay.windowY = Math.max(4, (sh - this.blockedListOverlay.windowHeight) / 2);
      this.blockedListOverlay.setVisible(true);
      this.blockedListOverlay.refilter();
      OverlayManager.getInstance().bringToFront(this.blockedListOverlay);
   }

   private void openChannelListener() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      this.channelFilters.reloadFromConfig();
      this.payloadRegistrations.reloadFromConfig();
      this.channelListenerOverlay.windowX = Math.max(4, (sw - this.channelListenerOverlay.windowWidth) / 2);
      this.channelListenerOverlay.windowY = Math.max(4, (sh - this.channelListenerOverlay.windowHeight) / 2);
      this.channelListenerOverlay.setVisible(true);
      this.channelListenerOverlay.rebuildRows();
      OverlayManager.getInstance().bringToFront(this.channelListenerOverlay);
   }

   private void openPayloadEditor() {
      ActionEditorOverlay editor = ActionEditorOverlay.getInstance();
      editor.openPayload(new autismclient.util.macro.PayloadAction());
      OverlayManager.getInstance().bringToFront(editor);
   }

   private synchronized void clearEntriesLocked() {
      this.pendingEntries.clear();
      this.allBuffer.clear();
      this.inventoryBuffer.clear();
      this.movementBuffer.clear();
      this.payloadBuffer.clear();
      this.stateDeltaTracker.reset();
      this.displayRows.clear();
      this.entryScroll = 0;
      this.scroller.jumpTo(0, 0);
      this.expandedGroups.clear();
      this.rowsDirty = true;
   }

   private void copyLogToClipboard() {
      this.flushPending();
      StringBuilder sb = new StringBuilder();
      sb.append("=== Packet Logger [").append(this.category.init_Str).append("] ===\n");
      if (!this.searchQuery.isEmpty()) {
         sb.append("Search: \"").append(this.searchQuery).append("\"\n");
      }

      sb.append(String.format("%-5s %-14s %-8s %s%n", "DIR", "TIME", "TICK", "PACKET"));
      sb.append("----------------------------------------------\n");
      List<AutismPacketLoggerOverlay.LoggedPacketEntry> source = this.categoryEntries();
      if (source == null) {
         source = new ArrayList<>();
      }

      String ls = this.searchQuery.toLowerCase(Locale.ROOT);
      int count = 0;

      for (AutismPacketLoggerOverlay.LoggedPacketEntry e : source) {
         if (!this.isBlockedCached(e.packetClass)
            && (
               this.category != AutismPacketLoggerOverlay.LogCategory.PAYLOAD
                  || !this.payloadFilteredOnly
                  || this.matchingChannelRule(e) != null
            )
            && (ls.isEmpty() || this.init_Str_AutismPacketLogger(e).contains(ls))
            && (this.directionFilter != 1 || e.direction.equals("C2S"))
            && (this.directionFilter != 2 || e.direction.equals("S2C"))) {
            String copiedName = e.name;
            if (e.payloadSnapshot != null) {
               String summary = AutismPayloadSupport.formatSummary(e.payloadSnapshot, true);
               if (!summary.isBlank()) {
                  copiedName = copiedName + " " + summary;
               }
            }

            PayloadChannelFilterManager.Inner_StreamUtil match = this.matchingChannelRule(e);
            if (match != null) {
               copiedName = copiedName + " [" + match.load() + "]";
            }

            sb.append(
               String.format(
                  "%-5s %-14s %-8s %s%n",
                  e.direction.equals("C2S") ? "->" : "<-",
                  TIME_FORMAT.format(Instant.ofEpochMilli(e.timestampMs)),
                  "T" + e.tick,
                  copiedName
               )
            );
            count++;
         }
      }

      sb.append("----------------------------------------------\n");
      sb.append("Total: ").append(count).append(" packets\n");
      MC.keyboardHandler.setClipboard(sb.toString());
      bi.init("Copied " + count + " packets.");
   }

   private int preferredWidth() {
      String[] labels = this.toolbarLabels();
      int row1 = 0;

      for (String label : labels) {
         row1 += this.buttonWidth(label);
      }

      row1 += 2 * (labels.length - 1);
      int row2 = this.searchFieldWidth() + 3;

      for (String label : new String[]{"Both", "C2S", "S2C"}) {
         row2 += autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.styleTheme, autismclient.gui.f.b.l.read, label, 4, 30, 48) + 2;
      }

      return Math.max(row1, row2) + 8;
   }

   private int set_I_15() {
      return 14 + this.clear_I_6() + this.toolbarHeight() + 12 + this.rowHeight() * 14;
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.font, label, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
   }

   private int rowHeight() {
      return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(12), this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.read, 2));
   }

   private int rowTextY(int y) {
      return UiLayoutMath.centerTextY(y, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush());
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int clear_I_6() {
      return this.controlHeight();
   }

   private int reset_I_5() {
      return this.controlHeight();
   }

   private int start_I_12() {
      return 2;
   }

   private int toolbarHeight() {
      return this.reset_I_5() * 2 + this.start_I_12();
   }

   private int searchFieldWidth() {
      return 148;
   }

   private int prev_I_4() {
      return this.category == AutismPacketLoggerOverlay.LogCategory.PAYLOAD ? 108 : this.searchFieldWidth();
   }

   private int maxVisibleRows() {
      return 22;
   }

   private final class BlockedPacketListOverlay extends DraggableOverlayWindow {
      private final ThemedTextInputBox searchField = new ThemedTextInputBox(AutismPacketLoggerOverlay.MC, AutismPacketLoggerOverlay.this.font, 0, 0, 120, 16, false);
      private final autismclient.gui.ui.widgets.ScrollOffsetController scroller = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private final List<Class<? extends Packet<?>>> allPackets = new ArrayList<>();
      private final Set<Class<? extends Packet<?>>> init_Set = new HashSet<>();
      private final List<Class<? extends Packet<?>>> filteredPackets = new ArrayList<>();
      private int scrollOffset;
      private boolean write_Z;
      private int thumbGrabOffset;
      private boolean update_Z;
      private boolean dragging;
      private double dragOffsetX;
      private double dragOffsetY;
      private boolean flush_Z;

      private int blockedButtonWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth("Blocked");
      }

      private int clearButtonWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth("Clear");
      }

      private int resetButtonWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth("Reset");
      }

      BlockedPacketListOverlay() {
         super("PacketLoggerBlockedList", 250, 248);
         this.windowX = 260;
         this.windowY = 56;
         this.windowVisible = false;
         this.searchField.setPlaceholder(Component.literal("Search packets..."));
         this.searchField.setMaxLength(120);
         this.searchField.setChangedListener(value -> this.flush_Z = true);
         List<Class<? extends Packet<?>>> c2s = new ArrayList<>(PacketTypeRegistry.getC2sPackets());
         List<Class<? extends Packet<?>>> s2c = new ArrayList<>(PacketTypeRegistry.getS2cPackets());
         Comparator<Class<? extends Packet<?>>> byName = Comparator.comparing(PacketNameResolver::nameOfClass, String.CASE_INSENSITIVE_ORDER);
         c2s.sort(byName);
         s2c.sort(byName);
         this.init_Set.addAll(c2s);
         this.allPackets.addAll(c2s);
         this.allPackets.addAll(s2c);
      }

      private int firstRowY() {
         return this.windowY + 14 + 4;
      }

      private int get_I_16() {
         return this.firstRowY() + AutismPacketLoggerOverlay.this.controlHeight() + 2;
      }

      private int set_I_16() {
         return this.get_I_16() + AutismPacketLoggerOverlay.this.controlHeight() + 4;
      }

      private String packetName(Class<? extends Packet<?>> cls) {
         String name = PacketTypeRegistry.getPacketName(cls);
         return name != null ? name : PacketNameResolver.nameOfClass(cls);
      }

      @Override
      public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
         if (this.windowVisible || this.isClosing()) {
            if (this.flush_Z) {
               this.flush_Z = false;
               this.refilter();
            }

            OverlayBounds bounds = this.clampToScreen(this, new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
            this.windowX = bounds.x;
            this.windowY = bounds.y;
            this.renderWindowFrame(ctx, mx, my, bounds, "Block List", this.windowCollapsed, false);
            boolean clip = this.beginBodyClip(ctx, bounds, this.windowCollapsed);
            if (!clip) {
               this.renderInactiveDim(ctx, bounds, this.windowCollapsed, false);
            } else {
               try {
                  int x = this.windowX + 6;
                  int y = this.firstRowY();
                  int rowH = AutismPacketLoggerOverlay.this.controlHeight();
                  this.searchField.setX(x);
                  this.searchField.setY(y);
                  this.searchField.setWidth(Math.max(80, this.windowWidth - 12));
                  this.searchField.setHeight(rowH);
                  this.searchField.render(ctx, mx, my, delta);
                  y = this.get_I_16();
                  int bx = x;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx, AutismPacketLoggerOverlay.this.font, bx, y, this.blockedButtonWidth(), rowH, "Blocked", this.update_Z, mx, my
                  );
                  int clearX = bx + this.blockedButtonWidth() + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     clearX,
                     y,
                     this.clearButtonWidth(),
                     rowH,
                     "Clear",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
                     true,
                     mx,
                     my
                  );
                  int resetX = clearX + this.clearButtonWidth() + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     resetX,
                     y,
                     this.resetButtonWidth(),
                     rowH,
                     "Reset",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  int countX = resetX + this.resetButtonWidth() + 6;
                  AutismPacketLoggerOverlay.this.drawText(
                     ctx,
                     AutismPacketLoggerOverlay.this.blockedPackets.size() + " blocked",
                     autismclient.gui.f.b.l.write,
                     UiColorPalette.textBody(),
                     countX,
                     UiLayoutMath.centerTextY(
                        y,
                        rowH,
                        AutismPacketLoggerOverlay.this.styleTheme.init_I_l(autismclient.gui.f.b.l.write),
                        AutismPacketLoggerOverlay.this.styleTheme.flush()
                     )
                  );
                  int listTop = this.set_I_16();
                  int listH = Math.max(20, this.windowY + this.windowHeight - listTop - 6);
                  int contentH = this.filteredPackets.size() * AutismPacketLoggerOverlay.this.rowHeight();
                  int maxScroll = Math.max(0, contentH - listH);
                  this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset, AutismPacketLoggerOverlay.this.rowHeight(), maxScroll);
                  this.scroller.setOffset(this.scrollOffset, maxScroll);
                  int drawScroll = this.scroller.tickAndClamp(delta, maxScroll);
                  autismclient.gui.ui.UiClipStack.getInstance().push(ctx, autismclient.gui.f.NetHelper.init(this.windowX + 4, listTop, this.windowWidth - 8, listH));
                  int base = listTop - drawScroll;
                  int rowHeight = AutismPacketLoggerOverlay.this.rowHeight();
                  int firstVisible = Math.max(0, drawScroll / rowHeight);
                  int lastVisible = Math.min(this.filteredPackets.size() - 1, (drawScroll + listH - 1) / rowHeight);

                  for (int i = firstVisible; i <= lastVisible; i++) {
                     int ry = base + i * rowHeight;
                     Class<? extends Packet<?>> cls = this.filteredPackets.get(i);
                     boolean c2s = this.init_Set.contains(cls);
                     boolean blocked = AutismPacketLoggerOverlay.this.isBlocked(cls, PacketNameResolver.nameOfClass(cls));
                     boolean hover = mx >= this.windowX + 4
                        && mx < this.windowX + this.windowWidth - 8
                        && my >= ry
                        && my < ry + AutismPacketLoggerOverlay.this.rowHeight();
                     int fill = blocked ? UiColorPalette.blockedRowFill(hover) : UiColorPalette.packetRowFill(c2s, i, hover);
                     ctx.fill(this.windowX + 4, ry, this.windowX + this.windowWidth - 8, ry + AutismPacketLoggerOverlay.this.rowHeight(), fill);
                     if (blocked) {
                        ctx.fill(this.windowX + 4, ry, this.windowX + 6, ry + AutismPacketLoggerOverlay.this.rowHeight(), UiColorPalette.blockedRowBar());
                     }

                     int color = UiColorPalette.packetRowAccent(c2s, i);
                     String name = PacketNameResolver.nameOfClass(cls);
                     String label = autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.font,
                        name,
                        Math.max(1, this.windowWidth - 24),
                        AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                        color
                     );
                     AutismPacketLoggerOverlay.this.drawText(
                        ctx, label, autismclient.gui.f.b.l.read, color, this.windowX + 9, AutismPacketLoggerOverlay.this.rowTextY(ry)
                     );
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
                     contentH, listH, this.windowX + this.windowWidth - 5, listTop, 3, listH, drawScroll
                  );
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, metrics, metrics.containsTrack(mx, my), this.write_Z);
               } finally {
                  this.endBodyClip(ctx, clip);
                  this.renderInactiveDim(ctx, bounds, this.windowCollapsed, false);
               }
            }
         }
      }

      private void refilter() {
         String query = this.searchField.getValue() == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
         this.filteredPackets.clear();

         for (Class<? extends Packet<?>> cls : this.allPackets) {
            if (!this.update_Z || AutismPacketLoggerOverlay.this.isBlocked(cls, PacketNameResolver.nameOfClass(cls))) {
               if (!query.isEmpty()) {
                  String hay = (PacketNameResolver.nameOfClass(cls) + " " + cls.getSimpleName()).toLowerCase(Locale.ROOT);
                  if (!hay.contains(query)) {
                     continue;
                  }
               }

               this.filteredPackets.add(cls);
            }
         }

         this.scrollOffset = Math.min(this.scrollOffset, Math.max(0, this.filteredPackets.size() * AutismPacketLoggerOverlay.this.rowHeight()));
      }

      @Override
      public boolean supportsHeaderCollapseClick() {
         return true;
      }

      @Override
      public boolean mouseClicked(double mx, double my, int button) {
         if (!this.windowVisible) {
            return false;
         }

         OverlayBounds bounds = this.getBounds();
         if (button == 0 && this.isOverHeaderCloseButton(mx, my, bounds)) {
            this.setVisible(false);
            return true;
         }

         if (button == 0 && this.isOverHeader(mx, my)) {
            this.dragging = true;
            this.dragOffsetX = mx - this.windowX;
            this.dragOffsetY = my - this.windowY;
            return true;
         }

         if (this.windowCollapsed) {
            return true;
         }

         int x = this.windowX + 6;
         if (this.searchField.mouseClicked(mx, my, button)) {
            return true;
         }

         int y = this.get_I_16();
         if (button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.controlHeight()) {
            int clearX = x + this.blockedButtonWidth() + 2;
            int resetX = clearX + this.clearButtonWidth() + 2;
            if (mx >= x && mx < x + this.blockedButtonWidth()) {
               this.update_Z = !this.update_Z;
               this.scrollOffset = 0;
               this.refilter();
               return true;
            }

            if (mx >= clearX && mx < clearX + this.clearButtonWidth()) {
               AutismPacketLoggerOverlay.this.clearBlocked();
               this.refilter();
               return true;
            }

            if (mx >= resetX && mx < resetX + this.resetButtonWidth()) {
               AutismPacketLoggerOverlay.this.resetBlockedToDefaults();
               this.refilter();
               return true;
            }
         }

         int listTop = this.set_I_16();
         int listH = Math.max(20, this.windowY + this.windowHeight - listTop - 6);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
            this.filteredPackets.size() * AutismPacketLoggerOverlay.this.rowHeight(), listH, this.windowX + this.windowWidth - 5, listTop, 3, listH, this.scrollOffset
         );
         if (button == 0 && metrics.isScrollable() && metrics.containsTrack((int)mx, (int)my)) {
            this.write_Z = true;
            this.thumbGrabOffset = (int)my - metrics.thumbY();
            return true;
         }

         if (button == 0 && my >= listTop) {
            int index = (int)((my - listTop + this.scrollOffset) / AutismPacketLoggerOverlay.this.rowHeight());
            if (index >= 0 && index < this.filteredPackets.size()) {
               Class<? extends Packet<?>> cls = this.filteredPackets.get(index);
               String key = this.packetName(cls);
               if (AutismPacketLoggerOverlay.this.isBlocked(cls, PacketNameResolver.nameOfClass(cls))) {
                  AutismPacketLoggerOverlay.this.init_V_Class_Str(cls, PacketNameResolver.nameOfClass(cls));
               } else {
                  AutismPacketLoggerOverlay.this.load(key);
               }

               if (this.update_Z) {
                  this.refilter();
               }

               return true;
            }
         }

         return this.containsPoint(mx, my);
      }

      @Override
      public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
         if (this.dragging && button == 0) {
            this.setBounds(new OverlayBounds((int)Math.round(mx - this.dragOffsetX), (int)Math.round(my - this.dragOffsetY), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
            return true;
         } else if (this.write_Z && button == 0) {
            int listTop = this.set_I_16();
            int listH = Math.max(20, this.windowY + this.windowHeight - listTop - 6);
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
               this.filteredPackets.size() * AutismPacketLoggerOverlay.this.rowHeight(), listH, this.windowX + this.windowWidth - 5, listTop, 3, listH, this.scrollOffset
            );
            this.scrollOffset = this.quantizeScrollOffset(
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, (int)my, this.thumbGrabOffset), AutismPacketLoggerOverlay.this.rowHeight(), metrics.maxScroll()
            );
            return true;
         } else {
            return this.searchField.mouseDragged(mx, my, button, dx, dy);
         }
      }

      @Override
      public boolean mouseReleased(double mx, double my, int button) {
         if (button == 0 && this.dragging) {
            this.dragging = false;
            this.saveBounds();
            return true;
         } else if (button == 0 && this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.searchField.mouseReleased(mx, my, button);
         }
      }

      @Override
      public boolean mouseScrolled(double mx, double my, double amount) {
         if (this.windowVisible && !this.windowCollapsed && this.containsPoint(mx, my)) {
            int listTop = this.set_I_16();
            int listH = Math.max(20, this.windowY + this.windowHeight - listTop - 6);
            int maxScroll = Math.max(0, this.filteredPackets.size() * AutismPacketLoggerOverlay.this.rowHeight() - listH);
            this.scrollOffset = this.quantizeScrollOffset(
               this.scrollOffset - (int)Math.signum(amount) * AutismPacketLoggerOverlay.this.rowHeight(), AutismPacketLoggerOverlay.this.rowHeight(), maxScroll
            );
            this.scroller.jumpTo(this.scrollOffset, maxScroll);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         return this.windowVisible && this.searchField.isFocused() && this.searchField.keyPressed(Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers));
      }

      @Override
      public boolean charTyped(char chr, int modifiers) {
         return this.windowVisible && this.searchField.isFocused() && this.searchField.charTyped(new CharacterEvent(chr));
      }

      @Override
      public boolean hasFocusedTextInput() {
         return this.windowVisible && this.searchField.isFocused();
      }

      @Override
      public void clearInputFocus() {
         this.searchField.setFocused(false);
      }
   }

   private record FilterColumnLayout(String[] labels, int[] widths, int gap) {
   }

   static class DisplayRow {
      AutismPacketLoggerOverlay.DisplayRowKind kind;
      AutismPacketLoggerOverlay.LoggedPacketEntry entry;
      String groupKey;
      int count;
      String direction;
      Class<?> packetClass;
      AutismPayloadSupport.PayloadEntry payload;
   }

   private static final class ChannelListRow {
      final AutismPacketLoggerOverlay.ChannelRowKind kind;
      final String header;
      final int ruleIndex;
      final AutismConfig.PayloadChannelFilterRule rule;
      final int registrationIndex;
      final AutismConfig.PayloadChannelRegistrationRule registration;
      final String learnedChannel;
      final AutismPacketLoggerOverlay.ChannelFamilyBucket family;
      final PayloadChannelFilterManager.Inner_CachePool preset;

      private ChannelListRow(
         AutismPacketLoggerOverlay.ChannelRowKind kind,
         String header,
         int ruleIndex,
         AutismConfig.PayloadChannelFilterRule rule,
         int registrationIndex,
         AutismConfig.PayloadChannelRegistrationRule registration,
         String learnedChannel,
         AutismPacketLoggerOverlay.ChannelFamilyBucket family,
         PayloadChannelFilterManager.Inner_CachePool preset
      ) {
         this.kind = kind;
         this.header = header;
         this.ruleIndex = ruleIndex;
         this.rule = rule;
         this.registrationIndex = registrationIndex;
         this.registration = registration;
         this.learnedChannel = learnedChannel;
         this.family = family;
         this.preset = preset;
      }

      static AutismPacketLoggerOverlay.ChannelListRow headerRow(String label) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.HEADER, label, -1, null, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow messageRow(String label) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.EMPTY, label, -1, null, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow registrationRow(int index, AutismConfig.PayloadChannelRegistrationRule rule) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.REGISTRATION, null, -1, null, index, rule, null, null, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow learnedRow(String channel) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.LEARNED, null, -1, null, -1, null, channel, null, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow filterRuleRow(int index, AutismConfig.PayloadChannelFilterRule rule) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.ACTIVE, null, index, rule, -1, null, null, null, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow familyRow(AutismPacketLoggerOverlay.ChannelFamilyBucket family) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.FAMILY, null, -1, null, -1, null, null, family, null);
      }

      static AutismPacketLoggerOverlay.ChannelListRow presetRow(PayloadChannelFilterManager.Inner_CachePool preset) {
         return new AutismPacketLoggerOverlay.ChannelListRow(AutismPacketLoggerOverlay.ChannelRowKind.PRESET, null, -1, null, -1, null, null, null, preset);
      }
   }

   public enum LogCategory {
      ALL("All"),
      INVENTORY("INV"),
      MOVEMENT("Move"),
      PAYLOAD("Payload");

      public final String init_Str;

      LogCategory(String l) {
         this.init_Str = l;
      }
   }

   public static class LoggedPacketEntry {
      public final long id = AutismPacketLoggerOverlay.nextEntryId++;
      public final long timestampMs;
      public final int tick;
      public final String direction;
      public final String name;
      private volatile String cachedSearchKey;
      public final String key;
      public final Class<?> packetClass;
      public final Packet<?> packet;
      public final boolean inventoryPacket;
      public final boolean movementPacket;
      public final boolean payloadPacket;
      public final String write;
      public final String update;
      public final AutismPayloadSupport.PayloadEntry payloadSnapshot;
      public final PacketStateDeltaTracker.Inner_DataUtil packetContext;

      public LoggedPacketEntry(
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
         this.timestampMs = ts;
         this.tick = tick;
         this.direction = dir;
         this.name = name;
         this.payloadSnapshot = payloadSnapshot;
         this.key = dir + ":" + name;
         this.packetClass = cls;
         this.packet = ref;
         this.inventoryPacket = inv;
         this.movementPacket = mov;
         this.payloadPacket = payload;
         this.write = capturedBlockState;
         this.update = capturedScreen;
         this.packetContext = packetContext == null ? PacketStateDeltaTracker.EMPTY_CAPTURE : packetContext;
      }

      public String searchKey() {
         String key = this.cachedSearchKey;
         if (key == null) {
            String payloadSearch = this.payloadSnapshot == null
               ? ""
               : " " + this.payloadSnapshot.channel() + " " + this.payloadSnapshot.jsonDump();
            key = (this.name == null ? "" : this.name).concat(payloadSearch).toLowerCase(Locale.ROOT);
            this.cachedSearchKey = key;
         }

         return key;
      }
   }

   private record CaptureContext(String blockState, String load) {
      private static final AutismPacketLoggerOverlay.CaptureContext EMPTY = new AutismPacketLoggerOverlay.CaptureContext(null, null);
   }

   enum DisplayRowKind {
      ENTRY,
      GROUP;
   }

   private static final class ChannelGroup {
      final String group;
      final Map<String, AutismPacketLoggerOverlay.ChannelFamilyBucket> families = new LinkedHashMap<>();
      final List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();
      List<AutismPacketLoggerOverlay.ChannelFamilyBucket> familyList = List.of();
      List<PayloadChannelFilterManager.Inner_CachePool> presetList = List.of();

      ChannelGroup(String group) {
         this.group = group;
      }

      AutismPacketLoggerOverlay.ChannelFamilyBucket familyBucket(AutismPacketLoggerOverlay.ChannelFamilyKey familyKey) {
         return this.families.computeIfAbsent(familyKey.key(), unused -> new AutismPacketLoggerOverlay.ChannelFamilyBucket(familyKey));
      }
   }

   private record PacketColumnLayout(String[] labels, int[] widths, int gap) {
   }

   private record ChannelFamilyKey(String init, String load, String read) {
      String key() {
         return this.init + "|" + this.load;
      }

      public String load() {
         return this.init;
      }

      public String read() {
         return this.load;
      }

      public String familyLabel() {
         return this.read;
      }
   }

   private enum ChannelRowKind {
      HEADER,
      REGISTRATION,
      LEARNED,
      ACTIVE,
      FAMILY,
      PRESET,
      EMPTY;
   }

   private static final class ChannelFamilyBucket {
      final String init_Str;
      final String load_Str;
      final String label;
      final List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();
      List<PayloadChannelFilterManager.Inner_CachePool> displayPresets = List.of();
      private Set<String> cachedChannels;

      ChannelFamilyBucket(AutismPacketLoggerOverlay.ChannelFamilyKey familyKey) {
         this.init_Str = familyKey.load();
         this.load_Str = familyKey.read();
         this.label = familyKey.familyLabel();
      }

      String key() {
         return this.init_Str + "|" + this.load_Str;
      }

      int channelCount() {
         return this.channels().size();
      }

      Set<String> channels() {
         if (this.cachedChannels != null) {
            return this.cachedChannels;
         }

         LinkedHashSet<String> channels = new LinkedHashSet<>();

         for (PayloadChannelFilterManager.Inner_CachePool preset : this.presets) {
            if (preset != null) {
               String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
               if (PayloadRegistrationManager.isExactChannel(pattern)) {
                  channels.add(pattern);
               }
            }
         }

         this.cachedChannels = Collections.unmodifiableSet(channels);
         return this.cachedChannels;
      }

      List<PayloadChannelFilterManager.Inner_CachePool> effectivePresets() {
         return this.displayPresets != null && !this.displayPresets.isEmpty() ? this.displayPresets : this.presets;
      }
   }

   private final class PayloadChannelListenerOverlay extends DraggableOverlayWindow {
      private final ThemedTextInputBox searchField = new ThemedTextInputBox(AutismPacketLoggerOverlay.MC, AutismPacketLoggerOverlay.this.font, 0, 0, 120, 16, false);
      private final ThemedTextInputBox patternField = new ThemedTextInputBox(AutismPacketLoggerOverlay.MC, AutismPacketLoggerOverlay.this.font, 0, 0, 112, 16, false);
      private final autismclient.gui.ui.widgets.ScrollOffsetController scroller = new autismclient.gui.ui.widgets.ScrollOffsetController();
      private final List<AutismPacketLoggerOverlay.ChannelListRow> rows = new ArrayList<>();
      private int scrollOffset;
      private boolean write_Z;
      private int thumbGrabOffset;
      private boolean update_Z;
      private double dragOffsetX;
      private double dragOffsetY;
      private boolean showEnabledOnly;
      private boolean configDirty;
      private final Set<String> expandedFamilies = new HashSet<>();
      private int editingFilterIndex = -1;
      private int editingRegistrationIndex = -1;
      private Map<String, AutismPacketLoggerOverlay.ChannelGroup> cachedGroups;
      private List<AutismPacketLoggerOverlay.ChannelGroup> sortedGroups;
      private final Set<String> load_Set = new HashSet<>();
      private final Set<String> read_Set = new HashSet<>();
      private final Set<String> enabledRegistrations = new LinkedHashSet<>();
      private final Map<String, Integer> load_Map = new HashMap<>();
      private final Map<String, Integer> exactCountByGroup = new HashMap<>();
      private final Map<String, PayloadChannelFilterManager.Inner_CachePool> presetByPattern = new HashMap<>();
      private final Map<PayloadChannelFilterManager.Inner_CachePool, String> presetSearchText = new IdentityHashMap<>();
      private final Map<String, PayloadSubscriptionManager.Inner_CachePool> impactCache = new HashMap<>();
      private int highlightCount;
      private String subscriptionStatus = "";
      private int channelCap = 96;
      private boolean lockNoticeVisible;
      private long lockNoticeShownAt;
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
         this.windowX = 270;
         this.windowY = 58;
         this.windowVisible = false;
         this.searchField.setPlaceholder(Component.literal("Search presets/channels..."));
         this.searchField.setMaxLength(120);
         this.searchField.setChangedListener(value -> this.open_Z = true);
         this.patternField.setPlaceholder(Component.literal("channel or wildcard: shopgui:main / oraxen:*"));
         this.patternField.setMaxLength(96);
      }

      private int push_I_21() {
         return AutismPacketLoggerOverlay.this.controlHeight() + 2;
      }

      private int pop_I_21() {
         return this.windowY + 14 + 4;
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

      private int contentLeft() {
         return this.windowX + 6;
      }

      private int contentRight() {
         return this.windowX + this.windowWidth - 6 - 7;
      }

      private int contentWidth() {
         return Math.max(20, this.contentRight() - this.contentLeft());
      }

      private int start_I_13() {
         return this.contentLeft() - 2;
      }

      private int stop_I_6() {
         return this.windowX + this.windowWidth - 5;
      }

      private int listHeight() {
         int raw = Math.max(14, this.windowY + this.windowHeight - this.put_I_7() - 8);
         return Math.max(14, this.alignHeightToStep(raw, 14));
      }

      private boolean isEditing() {
         return this.editingFilterIndex >= 0 || this.editingRegistrationIndex >= 0;
      }

      private int patternFieldWidth() {
         int buttons = this.addButtonWidth() + (this.isEditing() ? 2 + this.cancelEditButtonWidth() : 0);
         return Math.max(120, this.windowWidth - 12 - buttons - 2);
      }

      private int addButtonWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth(this.isEditing() ? "Save" : "Add");
      }

      private int cancelEditButtonWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth("Cancel");
      }

      private int registrationButtonWidth() {
         String label = PayloadSubscriptionManager.isRegistrationUnlocked() ? "Registration : On" : "Registration : Off";
         return AutismPacketLoggerOverlay.this.buttonWidth(label);
      }

      private String acceptButtonLabel() {
         int remaining = this.lockNoticeRemainingMs();
         return remaining <= 0 ? "Accept" : "Accept " + remaining + "s";
      }

      private int warningCancelWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth("Cancel");
      }

      private int warningAcceptWidth() {
         return AutismPacketLoggerOverlay.this.buttonWidth(this.acceptButtonLabel());
      }

      private AutismPacketLoggerOverlay.PacketColumnLayout toolbarLayout() {
         String[] labels = new String[]{"Recommended", "Known On", "Capture", this.showEnabledOnly ? "Enabled" : "All", "Apply", "Revert", "All Off"};
         int[] widths = new int[labels.length];
         int total = 0;

         for (int i = 0; i < labels.length; i++) {
            widths[i] = AutismPacketLoggerOverlay.this.buttonWidth(labels[i]);
            total += widths[i];
         }

         return total + 2 * (labels.length - 1) <= this.windowWidth - 12
            ? new AutismPacketLoggerOverlay.PacketColumnLayout(labels, widths, 2)
            : new AutismPacketLoggerOverlay.PacketColumnLayout(labels, new int[]{88, 62, 56, 62, 46, 50, 50}, 2);
      }

      private void cancelEdit() {
         this.editingFilterIndex = -1;
         this.editingRegistrationIndex = -1;
         this.patternField.setValue("");
         this.patternField.setPlaceholder(Component.literal("exact channel or highlight wildcard"));
         this.patternField.setFocused(false);
      }

      private void syncManagersToConfig() {
         AutismPacketLoggerOverlay.this.channelFilters.syncToConfig(false);
         AutismPacketLoggerOverlay.this.payloadRegistrations.save(false);
      }

      private void markDirty() {
         this.configDirty = true;
         AutismPacketLoggerOverlay.this.rowsDirty = true;
      }

      private void saveIfDirty() {
         if (this.configDirty) {
            this.syncManagersToConfig();
            AutismConfig.getGlobal().save();
            this.configDirty = false;
         }
      }

      @Override
      public void setVisible(boolean v) {
         if (!v) {
            this.saveIfDirty();
            this.cancelEdit();
            this.lockNoticeVisible = false;
         }

         super.setVisible(v);
      }

      @Override
      public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
         if (this.windowVisible || this.isClosing()) {
            if (this.open_Z) {
               this.open_Z = false;
               this.rebuildRows();
            }

            OverlayBounds bounds = this.clampToScreen(this, new OverlayBounds(this.windowX, this.windowY, this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
            this.windowX = bounds.x;
            this.windowY = bounds.y;
            this.renderWindowFrame(ctx, mx, my, bounds, "Payload Channels", this.windowCollapsed, false);
            boolean clip = this.beginBodyClip(ctx, bounds, this.windowCollapsed);
            if (!clip) {
               this.renderInactiveDim(ctx, bounds, this.windowCollapsed, false);
            } else {
               try {
                  int x = this.windowX + 6;
                  int y = this.pop_I_21();
                  this.searchField.setX(x);
                  this.searchField.setY(y);
                  this.searchField.setWidth(Math.max(80, this.windowWidth - 12));
                  this.searchField.setHeight(AutismPacketLoggerOverlay.this.controlHeight());
                  this.searchField.render(ctx, mx, my, delta);
                  y = this.tick_I_19();
                  int customW = this.patternFieldWidth();
                  this.patternField.setX(x);
                  this.patternField.setY(y);
                  this.patternField.setWidth(customW);
                  this.patternField.setHeight(AutismPacketLoggerOverlay.this.controlHeight());
                  this.patternField.render(ctx, mx, my, delta);
                  int addW = this.addButtonWidth();
                  int addX = x + customW + 2;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     addX,
                     y,
                     addW,
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     this.isEditing() ? "Save" : "Add",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  if (this.isEditing()) {
                     int cancelX = addX + addW + 2;
                     autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                        ctx,
                        AutismPacketLoggerOverlay.this.font,
                        cancelX,
                        y,
                        this.cancelEditButtonWidth(),
                        AutismPacketLoggerOverlay.this.controlHeight(),
                        "Cancel",
                        autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                        true,
                        mx,
                        my
                     );
                  }

                  y = this.run_I_19();
                  AutismPacketLoggerOverlay.PacketColumnLayout controls = this.toolbarLayout();
                  int cx = x;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[0],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[0],
                     AutismPacketLoggerOverlay.this.channelFilters.isOnlyDefaultsEnabled()
                        ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST
                        : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[0] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[1],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[1],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[1] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[2],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[2],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[2] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[3],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[3],
                     this.showEnabledOnly ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[3] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[4],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[4],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[4] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[5],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[5],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  cx += controls.widths[5] + controls.gap;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     cx,
                     y,
                     controls.widths[6],
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     controls.labels[6],
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  PayloadSubscriptionManager.Inner_BufferHelper subscriptionStatus = PayloadSubscriptionManager.status();
                  String sub = subscriptionStatus == null ? "Registered 0/96" : subscriptionStatus.describe();
                  if (subscriptionStatus != null && !subscriptionStatus.locked() && subscriptionStatus.registered() > 0 && !subscriptionStatus.sent()) {
                     sub = sub + " pending";
                  }

                  boolean registrationUnlocked = PayloadSubscriptionManager.isRegistrationUnlocked();
                  int lockW = this.registrationButtonWidth();
                  int lockX = this.windowX + this.windowWidth - 8 - lockW;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     ctx,
                     AutismPacketLoggerOverlay.this.font,
                     lockX,
                     this.set_I_17(),
                     lockW,
                     AutismPacketLoggerOverlay.this.controlHeight(),
                     registrationUnlocked ? "Registration : On" : "Registration : Off",
                     registrationUnlocked ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     true,
                     mx,
                     my
                  );
                  String count = this.highlightCount + " highlight  " + sub + this.subscriptionStatus;
                  int countX = x;
                  int countMaxW = Math.max(1, lockX - countX - 5);
                  AutismPacketLoggerOverlay.this.drawText(
                     ctx,
                     autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.font,
                        count,
                        countMaxW,
                        AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
                        UiColorPalette.textBody()
                     ),
                     autismclient.gui.f.b.l.write,
                     UiColorPalette.textBody(),
                     countX,
                     UiLayoutMath.centerTextY(
                        this.get_I_17(),
                        AutismPacketLoggerOverlay.this.controlHeight(),
                        AutismPacketLoggerOverlay.this.styleTheme.init_I_l(autismclient.gui.f.b.l.write),
                        AutismPacketLoggerOverlay.this.styleTheme.flush()
                     )
                  );
                  int listTop = this.put_I_7();
                  int listH = this.listHeight();
                  int contentH = this.rows.size() * 14;
                  int maxScroll = Math.max(0, contentH - listH);
                  this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset, 14, maxScroll);
                  this.scroller.setOffset(this.scrollOffset, maxScroll);
                  int drawScroll = this.scroller.tickAndClamp(delta, maxScroll);
                  autismclient.gui.f.PacketUtil.init(
                     ctx,
                     autismclient.gui.f.NetHelper.init(this.start_I_13(), listTop - 2, Math.max(1, this.stop_I_6() - this.start_I_13()), listH + 4),
                     UiColorPalette.listBoxSurface(),
                     UiColorPalette.divider()
                  );
                  autismclient.gui.ui.UiClipStack.getInstance().push(ctx, autismclient.gui.f.NetHelper.init(this.contentLeft(), listTop, this.contentWidth(), listH));
                  int base = listTop - drawScroll;
                  int firstVisible = Math.max(0, drawScroll / 14);
                  int lastVisible = Math.min(this.rows.size() - 1, (drawScroll + listH - 1) / 14);

                  for (int i = firstVisible; i <= lastVisible; i++) {
                     int ry = base + i * 14;
                     this.renderRow(ctx, this.rows.get(i), ry, mx, my, i);
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
                     contentH, listH, this.windowX + this.windowWidth - 7, listTop, 3, listH, drawScroll
                  );
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, metrics, metrics.containsTrack(mx, my), this.write_Z);
                  if (this.lockNoticeVisible) {
                     this.renderBody(ctx, mx, my);
                  }
               } finally {
                  this.endBodyClip(ctx, clip);
                  this.renderInactiveDim(ctx, bounds, this.windowCollapsed, false);
               }
            }
         }
      }

      private void renderBody(GuiGraphicsExtractor ctx, int mx, int my) {
         int bodyTop = this.windowY + 14;
         ctx.fill(this.windowX + 4, bodyTop + 2, this.windowX + this.windowWidth - 4, this.windowY + this.windowHeight - 5, -872086265);
         AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.ColumnMetrics bounds = this.columnMetrics();
         int x = bounds.init;
         int y = bounds.load;
         int w = bounds.read;
         int h = bounds.write;
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_3(
            ctx,
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            -15594225,
            ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE),
            ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE)
         );
         this.drawFittedText(ctx, "Payload Registration Warning", autismclient.gui.f.b.l.load, ThemePaletteManager.recolor(-19276, ThemePaletteManager.ColorRole.DANGER), x, w, y + 10);
         this.drawFittedText(ctx, "Servers can see every registered channel.", autismclient.gui.f.b.l.read, UiColorPalette.textBright(), x, w, y + 30);
         this.drawFittedText(ctx, "This can identify this client or selected plugins.", autismclient.gui.f.b.l.read, UiColorPalette.textBright(), x, w, y + 43);
         this.drawFittedText(ctx, "Passive logging and decoding still work while locked.", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), x, w, y + 60);
         int cancelW = this.warningCancelWidth();
         int acceptW = this.warningAcceptWidth();
         int buttonY = bounds.update;
         int buttonsW = cancelW + 6 + acceptW;
         int cancelX = x + (w - buttonsW) / 2;
         int acceptX = cancelX + cancelW + 6;
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            ctx,
            AutismPacketLoggerOverlay.this.font,
            cancelX,
            buttonY,
            cancelW,
            AutismPacketLoggerOverlay.this.controlHeight(),
            "Cancel",
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
            true,
            mx,
            my
         );
         boolean ready = this.lockNoticeRemainingMs() <= 0;
         autismclient.gui.ui.widgets.ButtonDrawHelper.init(
            ctx,
            AutismPacketLoggerOverlay.this.font,
            acceptX,
            buttonY,
            acceptW,
            AutismPacketLoggerOverlay.this.controlHeight(),
            this.acceptButtonLabel(),
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
            ready,
            mx,
            my
         );
      }

      private int lockNoticeRemainingMs() {
         long elapsed = Math.max(0L, System.currentTimeMillis() - this.lockNoticeShownAt);
         long remaining = Math.max(0L, 5000L - elapsed);
         return (int)Math.ceil(remaining / 1000.0);
      }

      private boolean handleLockNoticeClick(double mx, double my, int button) {
         if (button != 0) {
            return true;
         }

         AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.ColumnMetrics bounds = this.columnMetrics();
         int cancelW = this.warningCancelWidth();
         int acceptW = this.warningAcceptWidth();
         int buttonsW = cancelW + 6 + acceptW;
         int cancelX = bounds.init + (bounds.read - buttonsW) / 2;
         int acceptX = cancelX + cancelW + 6;
         int buttonY = bounds.update;
         if (my >= buttonY && my < buttonY + AutismPacketLoggerOverlay.this.controlHeight() && mx >= cancelX && mx < cancelX + cancelW) {
            this.lockNoticeVisible = false;
            return true;
         }

         if (!(my >= buttonY) || !(my < buttonY + AutismPacketLoggerOverlay.this.controlHeight()) || !(mx >= acceptX) || !(mx < acceptX + acceptW)) {
            return true;
         }

         if (this.lockNoticeRemainingMs() > 0) {
            return true;
         }

         this.lockNoticeVisible = false;
         PayloadSubscriptionManager.read();
         this.rebuildRows();
         bi.init("Payload registration unlocked. Press Apply.");
         return true;
      }

      private AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.ColumnMetrics columnMetrics() {
         int bodyTop = this.windowY + 14;
         int contentW = 0;
         contentW = Math.max(
            contentW, this.textWidth("Payload Registration Warning", autismclient.gui.f.b.l.load, ThemePaletteManager.recolor(-19276, ThemePaletteManager.ColorRole.DANGER))
         );
         contentW = Math.max(contentW, this.textWidth("Servers can see every registered channel.", autismclient.gui.f.b.l.read, UiColorPalette.textBright()));
         contentW = Math.max(contentW, this.textWidth("This can identify this client or selected plugins.", autismclient.gui.f.b.l.read, UiColorPalette.textBright()));
         contentW = Math.max(contentW, this.textWidth("Passive logging and decoding still work while locked.", autismclient.gui.f.b.l.write, UiColorPalette.textBody()));
         int buttonsW = this.warningCancelWidth() + 6 + this.warningAcceptWidth();
         int maxW = Math.max(180, this.windowWidth - 24);
         int w = Math.min(maxW, Math.max(buttonsW + 28, contentW + 28));
         int h = 75 + AutismPacketLoggerOverlay.this.controlHeight() + 10;
         int x = this.windowX + (this.windowWidth - w) / 2;
         int y = bodyTop + Math.max(8, (this.windowHeight - 14 - h) / 2);
         return new AutismPacketLoggerOverlay.PayloadChannelListenerOverlay.ColumnMetrics(x, y, w, h, y + h - 10 - AutismPacketLoggerOverlay.this.controlHeight());
      }

      private int textWidth(String text, autismclient.gui.f.b.l tone, int color) {
         return autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.font, text, AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(tone), color
         );
      }

      private void drawFittedText(GuiGraphicsExtractor ctx, String text, autismclient.gui.f.b.l tone, int color, int x, int w, int y) {
         String fitted = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.font, text, Math.max(1, w - 28), AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(tone), color
         );
         int textW = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.font, fitted, AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(tone), color
         );
         AutismPacketLoggerOverlay.this.drawText(ctx, fitted, tone, color, x + Math.max(0, (w - textW) / 2), y);
      }

      private void showLockNotice() {
         this.lockNoticeVisible = true;
         this.lockNoticeShownAt = System.currentTimeMillis();
      }

      private void lockRegistration() {
         this.saveIfDirty();
         PayloadSubscriptionManager.lockRegistration(AutismPacketLoggerOverlay.MC);
         this.rebuildRows();
         bi.read("Payload registration locked.");
      }

      private boolean ensureRegistrationUnlocked() {
         if (PayloadSubscriptionManager.isRegistrationUnlocked()) {
            return true;
         }

         this.showLockNotice();
         return false;
      }

      private void renderRow(GuiGraphicsExtractor ctx, AutismPacketLoggerOverlay.ChannelListRow row, int y, int mx, int my, int index) {
         int left = this.contentLeft();
         int right = this.contentRight();
         int width = Math.max(1, right - left);
         int x = left + 4;
         if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.HEADER) {
            boolean groupHeader = this.load_Map.getOrDefault(this.cleanGroup(row.header), 0) > 0;
            boolean groupOn = groupHeader && this.isGroupFullyEnabled(row.header);
            ctx.fill(left, y, right, y + 14, UiColorPalette.sectionBarFill());
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDivider(ctx, left + 2, y + 14 - 1, Math.max(1, width - 4));
            int statusW = groupHeader ? 30 : 0;
            int statusX = right - statusW - 4;
            int channelX = groupHeader ? statusX - 58 - 3 : right;
            int studyX = groupHeader ? channelX - 36 - 3 : right;
            int maxHeaderW = groupHeader ? Math.max(1, studyX - x - 6) : Math.max(1, width - 10);
            String header = autismclient.gui.f.b.k.init(
               AutismPacketLoggerOverlay.this.font,
               row.header,
               maxHeaderW,
               AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
               UiColorPalette.textBody()
            );
            AutismPacketLoggerOverlay.this.drawText(
               ctx, header, autismclient.gui.f.b.l.write, UiColorPalette.textBody(), x, this.rowTextY(y, autismclient.gui.f.b.l.write)
            );
            if (groupHeader) {
               this.renderStudyButton(ctx, studyX, y, mx, my, "Study");
               this.renderGroupCount(ctx, row.header, channelX, y);
               this.renderStatusPill(ctx, statusX, y + 1, statusW, 12, groupOn ? "ON" : "OFF", groupOn);
            }
         } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.EMPTY) {
            ctx.fill(left, y, right, y + 14, UiColorPalette.rowSurface());
            AutismPacketLoggerOverlay.this.drawText(
               ctx,
               autismclient.gui.f.b.k.init(
                  AutismPacketLoggerOverlay.this.font,
                  row.header == null ? "" : row.header,
                  Math.max(1, width - 12),
                  AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
                  UiColorPalette.textSecondary()
               ),
               autismclient.gui.f.b.l.write,
               UiColorPalette.textSecondary(),
               x + 4,
               this.rowTextY(y, autismclient.gui.f.b.l.write)
            );
         } else {
            boolean hover = mx >= left && mx < right && my >= y && my < y + 14;
            if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.FAMILY) {
               AutismPacketLoggerOverlay.ChannelFamilyBucket family = row.family;
               if (family != null) {
                  int total = family.channelCount();
                  int enabled = this.enabledCount(family);
                  boolean allEnabled = total > 0 && enabled == total;
                  int fill = enabled > 0 ? UiColorPalette.filterRowFill(hover) : (hover ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
                  ctx.fill(left, y, right, y + 14, fill);
                  if (enabled > 0) {
                     ctx.fill(left, y, left + 2, y + 14, UiColorPalette.filterActiveBar());
                  }

                  int statusX = right - 30 - 4;
                  int channelX = statusX - 58 - 3;
                  int studyX = channelX - 36 - 3;
                  boolean familyExpanded = this.expandedFamilies.contains(family.key());
                  String text = family.label + "  " + enabled + "/" + total;
                  int textColor = enabled > 0 ? UiColorPalette.filterActiveText() : UiColorPalette.textBright();
                  autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 4, y + 2, 9, 9), familyExpanded, textColor);
                  AutismPacketLoggerOverlay.this.drawText(
                     ctx,
                     autismclient.gui.f.b.k.init(
                        AutismPacketLoggerOverlay.this.font,
                        text,
                        Math.max(1, studyX - (x + 15) - 5),
                        AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                        textColor
                     ),
                     autismclient.gui.f.b.l.read,
                     textColor,
                     x + 15,
                     this.rowTextY(y, autismclient.gui.f.b.l.read)
                  );
                  this.renderStudyButton(ctx, studyX, y, mx, my, "Study");
                  this.renderBadge(ctx, total + "ch", total > 0 ? -7346000 : UiColorPalette.textSecondary(), channelX, y);
                  this.renderStatusPill(ctx, statusX, y + 1, 30, 12, allEnabled ? "ON" : "OFF", allEnabled);
               }
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.REGISTRATION) {
               AutismConfig.PayloadChannelRegistrationRule rule = row.registration;
               boolean enabled = rule != null && rule.enabled;
               int fill = enabled ? UiColorPalette.filterRowFill(hover) : (hover ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
               ctx.fill(left, y, right, y + 14, fill);
               if (enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.filterActiveBar());
               }

               String label = rule != null && rule.label != null && !rule.label.isBlank() ? rule.label.trim() : "";
               String channel = rule == null ? "" : PayloadRegistrationManager.normalize(rule.channel);
               String text = !label.isBlank() && !label.equals(channel) ? label + "  " + channel : channel;
               boolean editable = this.isEditableRegistration(rule);
               int removeX = editable ? right - 14 : right;
               int editX = editable ? removeX - 14 : right;
               int statusRight = editable ? editX - 3 : right - 4;
               int statusX = statusRight - 30;
               int sourceW = 52;
               int sourceX = statusX - sourceW - 3;
               int maxTextW = Math.max(1, sourceX - (x + 4) - 5);
               int color = enabled ? UiColorPalette.filterActiveText() : UiColorPalette.textBright();
               AutismPacketLoggerOverlay.this.drawText(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     color
                  ),
                  autismclient.gui.f.b.l.read,
                  color,
                  x + 4,
                  this.rowTextY(y, autismclient.gui.f.b.l.read)
               );
               AutismPacketLoggerOverlay.this.drawText(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.font,
                     PayloadRegistrationManager.sourceLabel(rule == null ? "" : rule.source),
                     sourceW,
                     AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
                     UiColorPalette.textBody()
                  ),
                  autismclient.gui.f.b.l.write,
                  UiColorPalette.textBody(),
                  sourceX,
                  this.rowTextY(y, autismclient.gui.f.b.l.write)
               );
               this.renderStatusPill(ctx, statusX, y + 1, 30, 12, enabled ? "ON" : "OFF", enabled);
               if (editable) {
                  boolean eu = mx >= editX && mx <= editX + 14 && my >= y && my < y + 14;
                  boolean hu = mx >= removeX && mx <= removeX + 14 && my >= y && my < y + 14;
                  AutismPacketLoggerOverlay.this.drawText(
                     ctx, "E", autismclient.gui.f.b.l.write, eu ? -7346000 : UiColorPalette.textBody(), editX + 3, this.rowTextY(y, autismclient.gui.f.b.l.write)
                  );
                  autismclient.gui.f.PacketUtil.read(
                     ctx,
                     autismclient.gui.f.NetHelper.init(removeX + 3, y + 2, 9, 9),
                     hu ? ThemePaletteManager.recolor(-39322, ThemePaletteManager.ColorRole.DANGER) : UiColorPalette.textBody()
                  );
               }
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.LEARNED) {
               ctx.fill(left, y, right, y + 14, hover ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
               int statusX = right - 30 - 4;
               int sourceX = statusX - 56;
               int maxTextW = Math.max(1, sourceX - (x + 4) - 5);
               AutismPacketLoggerOverlay.this.drawText(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.font,
                     row.learnedChannel == null ? "" : row.learnedChannel,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     UiColorPalette.textBright()
                  ),
                  autismclient.gui.f.b.l.read,
                  UiColorPalette.textBright(),
                  x + 4,
                  this.rowTextY(y, autismclient.gui.f.b.l.read)
               );
               AutismPacketLoggerOverlay.this.drawText(
                  ctx, "Learned", autismclient.gui.f.b.l.write, UiColorPalette.textBody(), sourceX, this.rowTextY(y, autismclient.gui.f.b.l.write)
               );
               this.renderStatusPill(ctx, statusX, y + 1, 30, 12, "OFF", false);
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.ACTIVE) {
               AutismConfig.PayloadChannelFilterRule rule = row.rule;
               int fill = rule.enabled ? UiColorPalette.filterRowFill(hover) : (hover ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
               ctx.fill(left, y, right, y + 14, fill);
               if (rule.enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.filterActiveBar());
               }

               String label = rule.label != null && !rule.label.isBlank() ? rule.label : rule.pattern;
               String text = label + "  " + rule.pattern;
               int color = rule.enabled ? UiColorPalette.filterActiveText() : UiColorPalette.textBody();
               int removeX = right - 14;
               int editX = removeX - 14;
               int statusRight = editX - 3;
               int statusW = 30;
               int statusX = statusRight - statusW;
               int channelX = statusX - 58 - 3;
               int studyX = channelX - 36 - 3;
               int maxTextW = Math.max(1, studyX - (x + 4) - 4);
               AutismPacketLoggerOverlay.this.drawText(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     color
                  ),
                  autismclient.gui.f.b.l.read,
                  color,
                  x + 4,
                  this.rowTextY(y, autismclient.gui.f.b.l.read)
               );
               this.renderStudyButton(ctx, studyX, y, mx, my, "Study");
               this.renderImpactBadge(ctx, rule.pattern, channelX, y);
               this.renderStatusPill(ctx, statusX, y + 1, statusW, 12, rule.enabled ? "ON" : "OFF", rule.enabled);
               boolean eu = mx >= editX && mx <= editX + 14 && my >= y && my < y + 14;
               boolean hu = mx >= removeX && mx <= removeX + 14 && my >= y && my < y + 14;
               AutismPacketLoggerOverlay.this.drawText(
                  ctx, "E", autismclient.gui.f.b.l.write, eu ? -7346000 : UiColorPalette.textBody(), editX + 3, this.rowTextY(y, autismclient.gui.f.b.l.write)
               );
               autismclient.gui.f.PacketUtil.read(
                  ctx, autismclient.gui.f.NetHelper.init(removeX + 3, y + 2, 9, 9), hu ? ThemePaletteManager.recolor(-39322, ThemePaletteManager.ColorRole.DANGER) : UiColorPalette.textBody()
               );
            } else {
               PayloadChannelFilterManager.Inner_CachePool preset = row.preset;
               boolean enabled = this.isPresetEnabled(preset);
               int fill = enabled ? UiColorPalette.filterRowFill(hover) : (hover ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
               ctx.fill(left, y, right, y + 14, fill);
               if (enabled) {
                  ctx.fill(left, y, left + 2, y + 14, UiColorPalette.filterActiveBar());
               }

               String status = enabled ? "ON" : "OFF";
               int statusW = 30;
               int statusX = right - statusW - 4;
               String text = preset.load() + "  " + preset.read();
               int textColor = enabled ? UiColorPalette.filterActiveText() : UiColorPalette.textBright();
               int channelX = statusX - 58 - 3;
               int studyX = channelX - 36 - 3;
               int maxTextW = Math.max(1, studyX - (x + 4) - 5);
               AutismPacketLoggerOverlay.this.drawText(
                  ctx,
                  autismclient.gui.f.b.k.init(
                     AutismPacketLoggerOverlay.this.font,
                     text,
                     maxTextW,
                     AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     textColor
                  ),
                  autismclient.gui.f.b.l.read,
                  textColor,
                  x + 4,
                  this.rowTextY(y, autismclient.gui.f.b.l.read)
               );
               this.renderStudyButton(ctx, studyX, y, mx, my, this.presetActionLabel(preset));
               this.renderPresetImpact(ctx, preset, channelX, y);
               this.renderStatusPill(ctx, statusX, y + 1, statusW, 12, status, enabled);
            }
         }
      }

      private String presetActionLabel(PayloadChannelFilterManager.Inner_CachePool preset) {
         return preset != null && preset.kind().isEditableTemplate() ? "Use" : "Study";
      }

      private void renderStudyButton(GuiGraphicsExtractor ctx, int x, int y, int mx, int my, String label) {
         int h = 12;
         int py = y + (14 - h) / 2;
         boolean disabled = AutismPacketLoggerOverlay.this.configurationOnly && !"Use".equals(label);
         boolean hover = !disabled && mx >= x && mx < x + 36 && my >= py && my < py + h;
         if (disabled) {
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.font, mx, my), autismclient.gui.f.NetHelper.init(x, py, 36, h), label
            );
         } else {
            autismclient.gui.ui.widgets.PillBadgeRenderer.init(
               autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.font, mx, my),
               autismclient.gui.f.NetHelper.init(x, py, 36, h),
               label,
               false,
               hover
            );
         }
      }

      private boolean isOverStudyButton(double mx, double my, int rowY) {
         int right = this.contentRight();
         int statusX = right - 30 - 4;
         int channelX = statusX - 58 - 3;
         int studyX = channelX - 36 - 3;
         int studyY = rowY + 1;
         return mx >= studyX && mx < studyX + 36 && my >= studyY && my < studyY + 12;
      }

      private boolean isOverRowCheckbox(double mx, double my, int rowY) {
         int x = this.contentLeft() + 4;
         return mx >= x && mx < x + 16 && my >= rowY && my < rowY + 14;
      }

      private void renderImpactBadge(GuiGraphicsExtractor ctx, String pattern, int x, int y) {
         PayloadSubscriptionManager.Inner_CachePool impact = this.impactOf(pattern);
         boolean core = impact.channelCount() <= 0 && this.isCoreChannel(pattern);
         String label = impact.channelCount() > 0 ? impact.label() : (core ? "Core" : "Highlight");
         int color = impact.channelCount() <= 0 && !core ? UiColorPalette.textSecondary() : -7346000;
         this.renderBadge(ctx, label, color, x, y);
      }

      private void renderPresetImpact(GuiGraphicsExtractor ctx, PayloadChannelFilterManager.Inner_CachePool preset, int x, int y) {
         if (preset == null) {
            this.renderImpactBadge(ctx, "", x, y);
         } else {
            PayloadSubscriptionManager.Inner_CachePool impact = this.impactOf(preset.read());
            String label;
            int color;
            if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
               label = impact.channelCount() > 0 ? impact.label() : "Exact";
               color = -7346000;
            } else if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.CORE) {
               label = preset.kind().badge();
               color = -7346000;
            } else if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.UNVERIFIED) {
               label = preset.kind().badge();
               color = UiColorPalette.warningYellow();
            } else {
               label = preset.kind().badge();
               color = UiColorPalette.textSecondary();
            }

            this.renderBadge(ctx, label, color, x, y);
         }
      }

      private void renderBadge(GuiGraphicsExtractor ctx, String label, int color, int x, int y) {
         label = autismclient.gui.f.b.k.init(
            AutismPacketLoggerOverlay.this.font,
            label,
            Math.max(1, 56),
            AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
            color
         );
         AutismPacketLoggerOverlay.this.drawText(
            ctx,
            label,
            autismclient.gui.f.b.l.write,
            color,
            x
               + Math.max(
                     0,
                     58
                        - autismclient.gui.f.b.k.init(
                           AutismPacketLoggerOverlay.this.font,
                           label,
                           AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
                           color
                        )
                  )
                  / 2,
            this.rowTextY(y, autismclient.gui.f.b.l.write)
         );
      }

      private boolean isCoreChannel(String pattern) {
         String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
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

      private void renderGroupCount(GuiGraphicsExtractor ctx, String group, int x, int y) {
         int count = this.exactCount(group);
         String label = count > 0 ? count + "ch" : "Tpl";
         int color = count > 0 ? -7346000 : UiColorPalette.textSecondary();
         AutismPacketLoggerOverlay.this.drawText(
            ctx,
            label,
            autismclient.gui.f.b.l.write,
            color,
            x
               + Math.max(
                     0,
                     58
                        - autismclient.gui.f.b.k.init(
                           AutismPacketLoggerOverlay.this.font,
                           label,
                           AutismPacketLoggerOverlay.this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.write),
                           color
                        )
                  )
                  / 2,
            this.rowTextY(y, autismclient.gui.f.b.l.write)
         );
      }

      private int exactCount(String group) {
         return group == null ? 0 : this.exactCountByGroup.getOrDefault(this.cleanGroup(group), 0);
      }

      private boolean isPresetEnabled(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset == null) {
            return false;
         }

         String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
         return this.read_Set.contains(preset.init_Str()) || PayloadRegistrationManager.isExactChannel(pattern) && this.enabledRegistrations.contains(pattern);
      }

      private int enabledCount(AutismPacketLoggerOverlay.ChannelFamilyBucket family) {
         if (family == null) {
            return 0;
         }

         int enabled = 0;

         for (String channel : family.channels()) {
            if (this.enabledRegistrations.contains(channel)) {
               enabled++;
            }
         }

         return enabled;
      }

      private boolean isFamilyFullyEnabled(AutismPacketLoggerOverlay.ChannelFamilyBucket family) {
         return family != null && family.channelCount() > 0 && this.enabledCount(family) == family.channelCount();
      }

      private boolean isGroupFullyEnabled(String group) {
         if (group != null && !group.isBlank()) {
            String cleanGroup = this.cleanGroup(group);
            int total = this.load_Map.getOrDefault(cleanGroup, 0);
            if (total <= 0) {
               return false;
            }

            int enabled = 0;
            AutismPacketLoggerOverlay.ChannelGroup category = this.groups().get(cleanGroup);
            if (category == null) {
               return false;
            }

            for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.families.values()) {
               for (PayloadChannelFilterManager.Inner_CachePool preset : family.presets) {
                  if (this.isPresetEnabled(preset)) {
                     enabled++;
                  }
               }
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.presets) {
               if (this.isPresetEnabled(preset)) {
                  enabled++;
               }
            }

            return enabled == total;
         } else {
            return false;
         }
      }

      private void renderStatusPill(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean enabled) {
         autismclient.gui.ui.widgets.PillBadgeRenderer.init(
            autismclient.gui.f.UiHelper.init(ctx, AutismPacketLoggerOverlay.this.font, -1, -1),
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            label,
            enabled,
            false
         );
      }

      private int rowTextY(int y, autismclient.gui.f.b.l tone) {
         return UiLayoutMath.centerTextY(y, 14, AutismPacketLoggerOverlay.this.styleTheme.init_I_l(tone), AutismPacketLoggerOverlay.this.styleTheme.flush());
      }

      private void reloadEnabledRegistrations() {
         this.enabledRegistrations.clear();

         for (AutismConfig.PayloadChannelRegistrationRule rule : AutismPacketLoggerOverlay.this.payloadRegistrations.rules()) {
            if (rule != null && rule.enabled) {
               String channel = PayloadRegistrationManager.normalize(rule.channel);
               if (PayloadRegistrationManager.isExactChannel(channel)) {
                  this.enabledRegistrations.add(channel);
               }
            }
         }

         if (this.load_Set.isEmpty()) {
            for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.channelFilters.getPresets()) {
               if (preset != null) {
                  this.load_Set.add(preset.init_Str());
               }
            }
         }

         this.read_Set.clear();
         this.highlightCount = 0;

         for (AutismConfig.PayloadChannelFilterRule rule : AutismPacketLoggerOverlay.this.channelFilters.getRules()) {
            if (rule != null) {
               if (rule.enabled) {
                  this.highlightCount++;
               }

               if (rule.enabled) {
                  this.read_Set.add(PayloadChannelFilterManager.normalizePattern(rule.pattern));
               }
            }
         }

         if (this.load_Map.isEmpty() && this.exactCountByGroup.isEmpty()) {
            this.recountGroups();
         }

         this.channelCap = this.fetchChannelCap();
         this.subscriptionStatus = this.subscriptionStatusText();
      }

      private void recountGroups() {
         for (AutismPacketLoggerOverlay.ChannelGroup category : this.groups().values()) {
            int presetCount = category.presets.size();
            LinkedHashSet<String> exact = new LinkedHashSet<>();

            for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.families.values()) {
               presetCount += family.presets.size();
               exact.addAll(family.channels());
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.presets) {
               if (preset != null) {
                  String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
                  if (PayloadRegistrationManager.isExactChannel(pattern)) {
                     exact.add(pattern);
                  }
               }
            }

            this.load_Map.put(category.group, presetCount);
            this.exactCountByGroup.put(category.group, exact.size());
         }
      }

      private void rebuildRows() {
         this.reloadEnabledRegistrations();
         String query = this.searchField.getValue() == null ? "" : this.searchField.getValue().trim().toLowerCase(Locale.ROOT);
         this.rows.clear();
         boolean registrationUnlocked = PayloadSubscriptionManager.isRegistrationUnlocked();
         String registeredHeader = registrationUnlocked ? "Registered Channels" : "Saved Channels (Locked)";
         List<AutismConfig.PayloadChannelRegistrationRule> registrations = AutismPacketLoggerOverlay.this.payloadRegistrations.rules();
         Map<String, List<AutismPacketLoggerOverlay.ChannelListRow>> registeredGroups = new LinkedHashMap<>();

         for (int i = 0; i < registrations.size(); i++) {
            AutismConfig.PayloadChannelRegistrationRule rule = registrations.get(i);
            if (rule != null && rule.enabled && this.registrationMatches(rule, query)) {
               String group = this.registrationGroup(rule);
               registeredGroups.computeIfAbsent(group, unused -> new ArrayList<>()).add(AutismPacketLoggerOverlay.ChannelListRow.registrationRow(i, rule));
            }
         }

         int registeredCount = registeredGroups.values().stream().mapToInt(List::size).sum();
         if (!registeredGroups.isEmpty()) {
            this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow(registeredHeader));

            for (Entry<String, List<AutismPacketLoggerOverlay.ChannelListRow>> group : this.sortedGroups(registeredGroups)) {
               this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow(group.getKey()));
               group.getValue().sort(this::compareRegistrationRows);
               this.rows.addAll(group.getValue());
            }
         } else if (query.isEmpty()) {
            this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow(registeredHeader));
            this.rows
               .add(
                  AutismPacketLoggerOverlay.ChannelListRow.messageRow(
                     registrationUnlocked
                        ? "No channels registered. Enable a preset or add an exact channel."
                        : "No channels saved. Enable a preset or add an exact channel."
                  )
               );
         } else if (registeredCount == 0) {
            this.rows
               .add(AutismPacketLoggerOverlay.ChannelListRow.messageRow(registrationUnlocked ? "No matching registered channels." : "No matching saved channels."));
         }

         Set<String> knownRegistrationChannels = new LinkedHashSet<>();

         for (AutismConfig.PayloadChannelRegistrationRule rule : registrations) {
            if (rule != null) {
               knownRegistrationChannels.add(PayloadRegistrationManager.normalize(rule.channel));
            }
         }

         boolean customHeaderAdded = false;
         List<AutismConfig.PayloadChannelFilterRule> active = AutismPacketLoggerOverlay.this.channelFilters.getRules();

         for (int i = 0; i < active.size(); i++) {
            AutismConfig.PayloadChannelFilterRule rule = active.get(i);
            if (rule != null && !rule.preset && !this.load_Set.contains(PayloadChannelFilterManager.normalizePattern(rule.pattern)) && (!this.showEnabledOnly || rule.enabled)) {
               String normalizedPattern = PayloadChannelFilterManager.normalizePattern(rule.pattern);
               if (!PayloadRegistrationManager.isExactChannel(normalizedPattern) || !knownRegistrationChannels.contains(normalizedPattern)) {
                  String hay = ((rule.label == null ? "" : rule.label) + " " + rule.pattern).toLowerCase(Locale.ROOT);
                  if (query.isEmpty() || hay.contains(query)) {
                     if (!customHeaderAdded) {
                        this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow("Custom Highlights"));
                        customHeaderAdded = true;
                     }

                     this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.filterRuleRow(i, rule));
                  }
               }
            }
         }

         boolean availableHeader = false;
         if (!this.showEnabledOnly) {
            for (String learned : PayloadSubscriptionManager.learnedChannels()) {
               if (learned != null
                  && !learned.isBlank()
                  && !knownRegistrationChannels.contains(learned)
                  && (query.isEmpty() || learned.toLowerCase(Locale.ROOT).contains(query))) {
                  if (!availableHeader) {
                     this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow("Learned Exact Channels"));
                     availableHeader = true;
                  }

                  this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.learnedRow(learned));
               }
            }
         }

         for (int i = 0; i < registrations.size(); i++) {
            AutismConfig.PayloadChannelRegistrationRule rule = registrations.get(i);
            if (rule != null && !rule.enabled && !this.showEnabledOnly && !"preset".equals(rule.source) && this.registrationMatches(rule, query)) {
               if (!availableHeader) {
                  this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow("Available Exact Channels"));
                  availableHeader = true;
               }

               this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.registrationRow(i, rule));
            }
         }

         for (AutismPacketLoggerOverlay.ChannelGroup category : this.sortedGroupList()) {
            List<AutismPacketLoggerOverlay.ChannelFamilyBucket> visibleFamilies = new ArrayList<>();

            for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.familyList) {
               if (this.familyMatches(family, query) && (!this.showEnabledOnly || this.enabledCount(family) > 0)) {
                  visibleFamilies.add(family);
               }
            }

            List<PayloadChannelFilterManager.Inner_CachePool> standalone = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : category.presetList) {
               if (this.presetMatches(preset, query) && (!this.showEnabledOnly || this.isPresetEnabled(preset))) {
                  standalone.add(preset);
               }
            }

            if (!visibleFamilies.isEmpty() || !standalone.isEmpty()) {
               this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.headerRow(category.group));

               for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : visibleFamilies) {
                  this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.familyRow(family));
                  if (this.isFamilyExpanded(family, query)) {
                     for (PayloadChannelFilterManager.Inner_CachePool preset : family.effectivePresets()) {
                        if (this.presetMatchesInFamily(family, preset, query) && (!this.showEnabledOnly || this.isPresetEnabled(preset))) {
                           this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.presetRow(preset));
                        }
                     }
                  }
               }

               for (PayloadChannelFilterManager.Inner_CachePool preset : standalone) {
                  this.rows.add(AutismPacketLoggerOverlay.ChannelListRow.presetRow(preset));
               }
            }
         }

         if (this.rows.isEmpty()) {
            this.rows
               .add(AutismPacketLoggerOverlay.ChannelListRow.messageRow(this.showEnabledOnly ? "No enabled payload channels or highlights." : "No payload channels match."));
         }

         this.clampScroll();
      }

      private Map<String, AutismPacketLoggerOverlay.ChannelGroup> groups() {
         if (this.cachedGroups != null) {
            return this.cachedGroups;
         }

         Map<String, AutismPacketLoggerOverlay.ChannelGroup> categories = new LinkedHashMap<>();
         this.presetByPattern.clear();
         this.presetSearchText.clear();

         for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.channelFilters.getPresets()) {
            if (preset != null) {
               this.presetSearchText.put(preset, (preset.write() + " " + preset.load() + " " + preset.read()).toLowerCase(Locale.ROOT));
               if (PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
                  String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
                  if (PayloadRegistrationManager.isExactChannel(pattern)) {
                     PayloadChannelFilterManager.Inner_CachePool existing = this.presetByPattern.get(pattern);
                     if (existing == null || this.presetKindOrder(preset) < this.presetKindOrder(existing)) {
                        this.presetByPattern.put(pattern, preset);
                     }
                  }
               }

               String group = this.cleanGroup(preset.write());
               AutismPacketLoggerOverlay.ChannelGroup category = categories.computeIfAbsent(group, AutismPacketLoggerOverlay.ChannelGroup::new);
               AutismPacketLoggerOverlay.ChannelFamilyKey familyKey = this.familyKeyFor(preset);
               if (familyKey != null) {
                  category.familyBucket(familyKey).presets.add(preset);
               } else {
                  category.presets.add(preset);
               }
            }
         }

         this.cachedGroups = categories;
         this.sortedGroups = new ArrayList<>(categories.values());
         this.sortedGroups.sort((a, b) -> {
            int priority = Integer.compare(this.groupRank(a.group), this.groupRank(b.group));
            return priority != 0 ? priority : String.CASE_INSENSITIVE_ORDER.compare(a.group, b.group);
         });

         for (AutismPacketLoggerOverlay.ChannelGroup category : this.sortedGroups) {
            category.familyList = new ArrayList<>(category.families.values());
            category.familyList.sort(this::compareFamilies);
            category.presetList = new ArrayList<>(category.presets);
            this.sortPresets(category.presetList);

            for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.familyList) {
               family.displayPresets = new ArrayList<>(family.presets);
               this.sortPresets(family.displayPresets);
            }
         }

         return this.cachedGroups;
      }

      private List<AutismPacketLoggerOverlay.ChannelGroup> sortedGroupList() {
         this.groups();
         return this.sortedGroups == null ? List.of() : this.sortedGroups;
      }

      private String cleanGroup(String group) {
         return group != null && !group.isBlank() ? group.trim() : "Other";
      }

      private AutismPacketLoggerOverlay.ChannelFamilyKey familyKeyFor(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null && PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
            String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
            if (!PayloadRegistrationManager.isExactChannel(pattern)) {
               return null;
            }

            String family = this.load_Str(pattern);
            return family.isBlank() ? null : new AutismPacketLoggerOverlay.ChannelFamilyKey(this.cleanGroup(preset.write()), family, family);
         } else {
            return null;
         }
      }

      private String load_Str(String channel) {
         String normalized = PayloadRegistrationManager.normalize(channel);
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

      private boolean familyMatches(AutismPacketLoggerOverlay.ChannelFamilyBucket family, String query) {
         if (family == null) {
            return false;
         }

         if (query != null && !query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            if ((family.init_Str + " " + family.label).toLowerCase(Locale.ROOT).contains(needle)) {
               return true;
            }

            for (PayloadChannelFilterManager.Inner_CachePool preset : family.presets) {
               if (this.presetMatches(preset, needle)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      }

      private boolean presetMatchesInFamily(AutismPacketLoggerOverlay.ChannelFamilyBucket family, PayloadChannelFilterManager.Inner_CachePool preset, String query) {
         if (query != null && !query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            return family != null && (family.init_Str + " " + family.label).toLowerCase(Locale.ROOT).contains(needle)
               ? true
               : this.presetMatches(preset, needle);
         } else {
            return true;
         }
      }

      private boolean isFamilyExpanded(AutismPacketLoggerOverlay.ChannelFamilyBucket family, String query) {
         return family != null && (this.expandedFamilies.contains(family.key()) || query != null && !query.isBlank());
      }

      private int compareFamilies(AutismPacketLoggerOverlay.ChannelFamilyBucket a, AutismPacketLoggerOverlay.ChannelFamilyBucket b) {
         int exact = Integer.compare(b.channelCount(), a.channelCount());
         return exact != 0 ? exact : String.CASE_INSENSITIVE_ORDER.compare(a.label, b.label);
      }

      private List<Entry<String, List<AutismPacketLoggerOverlay.ChannelListRow>>> sortedGroups(Map<String, List<AutismPacketLoggerOverlay.ChannelListRow>> groups) {
         List<Entry<String, List<AutismPacketLoggerOverlay.ChannelListRow>>> out = new ArrayList<>(groups.entrySet());
         out.sort((a, b) -> {
            int priority = Integer.compare(this.load_I(a.getKey()), this.load_I(b.getKey()));
            return priority != 0 ? priority : String.CASE_INSENSITIVE_ORDER.compare(a.getKey(), b.getKey());
         });
         return out;
      }

      private int compareRegistrationRows(AutismPacketLoggerOverlay.ChannelListRow a, AutismPacketLoggerOverlay.ChannelListRow b) {
         String ac = a != null && a.registration != null ? PayloadRegistrationManager.normalize(a.registration.channel) : "";
         String bc = b != null && b.registration != null ? PayloadRegistrationManager.normalize(b.registration.channel) : "";
         return String.CASE_INSENSITIVE_ORDER.compare(ac, bc);
      }

      private String registrationGroup(AutismConfig.PayloadChannelRegistrationRule rule) {
         if (rule == null) {
            return "Registered: Other";
         } else {
            String source = rule.source == null ? "" : rule.source.trim().toLowerCase(Locale.ROOT);
            if ("custom".equals(source)) {
               return "Registered: Custom";
            } else if ("learned".equals(source)) {
               return "Registered: Learned";
            } else {
               PayloadChannelFilterManager.Inner_CachePool preset = this.presetForChannel(rule.channel);
               if (preset != null && preset.write() != null && !preset.write().isBlank()) {
                  AutismPacketLoggerOverlay.ChannelFamilyKey family = this.familyKeyFor(preset);
                  return family != null ? "Registered: " + preset.write().trim() + " - " + family.familyLabel() : "Registered: " + preset.write().trim();
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
            return "Other".equalsIgnoreCase(clean) ? 102 : this.groupRank(clean);
         }
      }

      private PayloadChannelFilterManager.Inner_CachePool presetForChannel(String channel) {
         String normalized = PayloadRegistrationManager.normalize(channel);
         if (normalized.isBlank()) {
            return null;
         }

         this.groups();
         return this.presetByPattern.get(normalized);
      }

      private boolean presetMatches(PayloadChannelFilterManager.Inner_CachePool preset, String query) {
         if (preset == null) {
            return false;
         }

         if (query != null && !query.isBlank()) {
            String hay = this.presetSearchText.get(preset);
            if (hay == null) {
               hay = (preset.write() + " " + preset.load() + " " + preset.read()).toLowerCase(Locale.ROOT);
               this.presetSearchText.put(preset, hay);
            }

            return hay.contains(query);
         } else {
            return true;
         }
      }

      private void sortPresets(List<PayloadChannelFilterManager.Inner_CachePool> presets) {
         presets.sort((a, b) -> {
            int kind = Integer.compare(this.presetKindOrder(a), this.presetKindOrder(b));
            if (kind != 0) {
               return kind;
            }

            int exact = Integer.compare(this.init_I(b), this.init_I(a));
            if (exact != 0) {
               return exact;
            }

            boolean aDefault = PayloadChannelFilterManager.isDefaultCorePreset(a);
            boolean bDefault = PayloadChannelFilterManager.isDefaultCorePreset(b);
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

         String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
         return PayloadRegistrationManager.isExactChannel(pattern) ? 1 : 0;
      }

      private PayloadSubscriptionManager.Inner_CachePool impactOf(String pattern) {
         String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
         return normalized.isBlank() ? new PayloadSubscriptionManager.Inner_CachePool(false, false, 0, List.of()) : this.impactCache.computeIfAbsent(normalized, PayloadSubscriptionManager::impactOf);
      }

      private int presetKindOrder(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null && preset.kind() != null) {
            byte var10000;
            switch (preset.kind()) {
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

      private int groupRank(String group) {
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

      private boolean registrationMatches(AutismConfig.PayloadChannelRegistrationRule rule, String query) {
         if (query != null && !query.isBlank()) {
            String hay = ((rule.label == null ? "" : rule.label) + " " + (rule.channel == null ? "" : rule.channel) + " " + PayloadRegistrationManager.sourceLabel(rule.source))
               .toLowerCase(Locale.ROOT);
            return hay.contains(query);
         } else {
            return true;
         }
      }

      private void mutateKeepingAnchor(int clickedIndex, Runnable mutation) {
         String anchor = this.rowAnchorKey(clickedIndex);
         int oldIndex = clickedIndex;
         mutation.run();
         this.rebuildRows();
         if (anchor != null) {
            int newIndex = this.rowIndexOf(anchor);
            if (newIndex >= 0) {
               this.scrollOffset += (newIndex - oldIndex) * 14;
            }
         }

         this.clampScroll();
         int listH = this.listHeight();
         int maxScroll = Math.max(0, this.rows.size() * 14 - listH);
         this.scroller.jumpTo(this.scrollOffset, maxScroll);
      }

      private String rowAnchorKey(int index) {
         if (index >= 0 && index < this.rows.size()) {
            AutismPacketLoggerOverlay.ChannelListRow row = this.rows.get(index);
            if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.ACTIVE && row.rule != null) {
               return "custom:" + PayloadChannelFilterManager.normalizePattern(row.rule.pattern);
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.REGISTRATION && row.registration != null) {
               return "reg:" + PayloadRegistrationManager.normalize(row.registration.channel);
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.LEARNED && row.learnedChannel != null) {
               return "learned:" + PayloadRegistrationManager.normalize(row.learnedChannel);
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.PRESET && row.preset != null) {
               return "preset:" + row.preset.init_Str();
            } else if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.FAMILY && row.family != null) {
               return "family:" + row.family.key();
            } else {
               return row.kind == AutismPacketLoggerOverlay.ChannelRowKind.HEADER && row.header != null ? "header:" + row.header : null;
            }
         } else {
            return null;
         }
      }

      private int rowIndexOf(String key) {
         if (key == null) {
            return -1;
         }

         for (int i = 0; i < this.rows.size(); i++) {
            if (key.equals(this.rowAnchorKey(i))) {
               return i;
            }
         }

         return -1;
      }

      private void clampScroll() {
         int listH = this.listHeight();
         int maxScroll = Math.max(0, this.rows.size() * 14 - listH);
         this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset, 14, maxScroll);
      }

      @Override
      public boolean supportsHeaderCollapseClick() {
         return true;
      }

      @Override
      public boolean mouseClicked(double mx, double my, int button) {
         if (!this.windowVisible) {
            return false;
         }

         OverlayBounds bounds = this.getBounds();
         if (button == 0 && this.isOverHeaderCloseButton(mx, my, bounds)) {
            this.setVisible(false);
            return true;
         }

         if (this.lockNoticeVisible) {
            return this.handleLockNoticeClick(mx, my, button);
         }

         if (button == 0 && this.isOverHeader(mx, my)) {
            this.update_Z = true;
            this.dragOffsetX = mx - this.windowX;
            this.dragOffsetY = my - this.windowY;
            return true;
         }

         if (this.windowCollapsed) {
            return true;
         }

         if (this.searchField.mouseClicked(mx, my, button)) {
            return true;
         }

         if (this.patternField.mouseClicked(mx, my, button)) {
            return true;
         }

         int y = this.tick_I_19();
         int customW = this.patternFieldWidth();
         int addW = this.addButtonWidth();
         int addX = this.windowX + 6 + customW + 2;
         if (button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.controlHeight()) {
            if (mx >= addX && mx < addX + addW) {
               this.submitPattern();
               return true;
            }

            if (this.isEditing()) {
               int cancelX = addX + addW + 2;
               if (mx >= cancelX && mx < cancelX + this.cancelEditButtonWidth()) {
                  this.cancelEdit();
                  return true;
               }
            }
         }

         y = this.run_I_19();
         AutismPacketLoggerOverlay.PacketColumnLayout controls = this.toolbarLayout();
         int cx = this.windowX + 6;
         boolean inControlsRow = button == 0 && my >= y && my < y + AutismPacketLoggerOverlay.this.controlHeight();
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[0]) {
            AutismPacketLoggerOverlay.this.channelFilters.resetToDefaultsInMemory();
            AutismPacketLoggerOverlay.this.payloadRegistrations.update();
            this.markDirty();
            this.rebuildRows();
            AutismPacketLoggerOverlay.this.rowsDirty = true;
            return true;
         }

         cx += controls.widths[0] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[1]) {
            this.reset_V_6();
            return true;
         }

         cx += controls.widths[1] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[2]) {
            this.importLearnedChannels(false);
            return true;
         }

         cx += controls.widths[2] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[3]) {
            this.showEnabledOnly = !this.showEnabledOnly;
            this.rebuildRows();
            return true;
         }

         cx += controls.widths[3] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[4]) {
            this.next_V_6();
            return true;
         }

         cx += controls.widths[4] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[5]) {
            this.prev_V_6();
            return true;
         }

         cx += controls.widths[5] + controls.gap;
         if (inControlsRow && mx >= cx && mx < cx + controls.widths[6]) {
            AutismPacketLoggerOverlay.this.channelFilters.disableAllInMemory();
            AutismPacketLoggerOverlay.this.payloadRegistrations.disableAll();
            this.markDirty();
            this.rebuildRows();
            AutismPacketLoggerOverlay.this.rowsDirty = true;
            return true;
         }

         int lockW = this.registrationButtonWidth();
         int lockX = this.windowX + this.windowWidth - 8 - lockW;
         int lockY = this.set_I_17();
         if (button == 0 && my >= lockY && my < lockY + AutismPacketLoggerOverlay.this.controlHeight() && mx >= lockX && mx < lockX + lockW) {
            if (PayloadSubscriptionManager.isRegistrationUnlocked()) {
               this.lockRegistration();
            } else {
               this.showLockNotice();
            }

            return true;
         } else {
            int listTop = this.put_I_7();
            int listH = this.listHeight();
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
               this.rows.size() * 14, listH, this.windowX + this.windowWidth - 7, listTop, 3, listH, this.scrollOffset
            );
            if (button == 0 && metrics.isScrollable() && metrics.containsTrack((int)mx, (int)my)) {
               this.write_Z = true;
               this.thumbGrabOffset = (int)my - metrics.thumbY();
               return true;
            }

            if (button == 0 && my >= listTop && my < listTop + listH && mx >= this.contentLeft() && mx < this.contentRight()) {
               int index = (int)((my - listTop + this.scrollOffset) / 14.0);
               if (index >= 0 && index < this.rows.size()) {
                  AutismPacketLoggerOverlay.ChannelListRow row = this.rows.get(index);
                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.REGISTRATION) {
                     boolean editable = this.isEditableRegistration(row.registration);
                     int removeX = editable ? this.contentRight() - 14 : this.contentRight();
                     int editX = editable ? removeX - 14 : this.contentRight();
                     if (editable && mx >= removeX && mx <= removeX + 14) {
                        this.mutateKeepingAnchor(index, () -> {
                           this.load_V_I_AutismConfig_Paylo_2(row.registrationIndex, row.registration);
                           this.markDirty();
                        });
                     } else if (editable && mx >= editX && mx <= editX + 14) {
                        this.editRegistration(row.registrationIndex, row.registration);
                     } else {
                        this.mutateKeepingAnchor(index, () -> {
                           AutismPacketLoggerOverlay.this.payloadRegistrations.toggle(row.registrationIndex);
                           this.markDirty();
                        });
                     }

                     AutismPacketLoggerOverlay.this.rowsDirty = true;
                     return true;
                  }

                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.LEARNED) {
                     if (!this.fitsChannelCap(List.of(row.learnedChannel))) {
                        this.reportChannelCap();
                        return true;
                     }

                     this.mutateKeepingAnchor(index, () -> {
                        AutismPacketLoggerOverlay.this.payloadRegistrations.load(row.learnedChannel, row.learnedChannel, "learned");
                        this.markDirty();
                     });
                     AutismPacketLoggerOverlay.this.rowsDirty = true;
                     return true;
                  }

                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.ACTIVE) {
                     if (this.isOverStudyButton(mx, my, listTop + index * 14 - this.scrollOffset)) {
                        this.toggleFilterRule(row.ruleIndex, row.rule);
                        return true;
                     }

                     int ux = this.contentRight() - 14;
                     int editX = ux - 14;
                     if (mx >= ux && mx <= ux + 14) {
                        this.mutateKeepingAnchor(index, () -> this.read_V_I_AutismConfig_Paylo(row.ruleIndex, row.rule));
                     } else if (mx >= editX && mx <= editX + 14) {
                        this.editFilterRule(row.ruleIndex, row.rule);
                     } else {
                        this.mutateKeepingAnchor(index, () -> this.load_V_I_AutismConfig_Paylo(row.ruleIndex, row.rule));
                     }

                     AutismPacketLoggerOverlay.this.rowsDirty = true;
                     return true;
                  }

                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.FAMILY) {
                     int rowY = listTop + index * 14 - this.scrollOffset;
                     if (this.isOverStudyButton(mx, my, rowY)) {
                        this.init_V(row.family);
                        return true;
                     }

                     if (this.isOverRowCheckbox(mx, my, rowY)) {
                        if (row.family != null) {
                           String key = row.family.key();
                           if (!this.expandedFamilies.remove(key)) {
                              this.expandedFamilies.add(key);
                           }

                           this.rebuildRows();
                        }

                        return true;
                     }

                     this.mutateKeepingAnchor(index, () -> this.toggleFamily(row.family));
                     AutismPacketLoggerOverlay.this.rowsDirty = true;
                     return true;
                  }

                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.PRESET) {
                     if (!this.isOverStudyButton(mx, my, listTop + index * 14 - this.scrollOffset)) {
                        if (row.preset != null
                           && row.preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT
                           && !this.isPresetEnabled(row.preset)
                           && !this.fitsChannelCap(List.of(row.preset.read()))) {
                           this.reportChannelCap();
                           return true;
                        }

                        this.mutateKeepingAnchor(index, () -> this.init_V(row.preset));
                        AutismPacketLoggerOverlay.this.rowsDirty = true;
                        return true;
                     }

                     if (row.preset != null && row.preset.kind().isEditableTemplate()) {
                        this.load_V(row.preset);
                     } else {
                        this.togglePreset(row.preset);
                     }

                     return true;
                  }

                  if (row.kind == AutismPacketLoggerOverlay.ChannelRowKind.HEADER
                     && this.load_Map.getOrDefault(this.cleanGroup(row.header), 0) > 0) {
                     if (this.isOverStudyButton(mx, my, listTop + index * 14 - this.scrollOffset)) {
                        this.write_V_2(row.header);
                        return true;
                     }

                     this.mutateKeepingAnchor(index, () -> this.toggleGroup(row.header));
                     AutismPacketLoggerOverlay.this.rowsDirty = true;
                     return true;
                  }
               }
            }

            return this.containsPoint(mx, my);
         }
      }

      private boolean isEditableRegistration(AutismConfig.PayloadChannelRegistrationRule rule) {
         if (rule == null) {
            return false;
         }

         String source = rule.source == null ? "" : rule.source.trim().toLowerCase(Locale.ROOT);
         return "custom".equals(source) || "learned".equals(source);
      }

      private void editRegistration(int index, AutismConfig.PayloadChannelRegistrationRule rule) {
         if (!this.isEditableRegistration(rule)) {
            bi.read("Presets can be toggled, not edited.");
         } else {
            this.editingRegistrationIndex = index;
            this.editingFilterIndex = -1;
            this.patternField.setValue(PayloadRegistrationManager.normalize(rule.channel));
            this.patternField.setPlaceholder(Component.literal("edit exact channel"));
            this.patternField.setFocused(true);
         }
      }

      private void editFilterRule(int index, AutismConfig.PayloadChannelFilterRule rule) {
         if (rule != null && !rule.preset) {
            this.editingFilterIndex = index;
            this.editingRegistrationIndex = -1;
            this.patternField.setValue(PayloadChannelFilterManager.normalizePattern(rule.pattern));
            this.patternField.setPlaceholder(Component.literal("edit highlight pattern"));
            this.patternField.setFocused(true);
         }
      }

      private void submitPattern() {
         String pattern = this.patternField.getValue() == null ? "" : this.patternField.getValue().trim();
         if (pattern.isBlank()) {
            bi.read("Enter a payload channel first.");
         } else if (!this.isEditing() && !this.fitsChannelCap(List.of(pattern))) {
            this.reportChannelCap();
         } else {
            String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
            String label = normalized;
            if (this.editingRegistrationIndex >= 0) {
               this.init_V_3(normalized);
            } else if (this.editingFilterIndex >= 0) {
               this.load_V_2(normalized);
            } else {
               boolean filterChanged = AutismPacketLoggerOverlay.this.channelFilters.addRuleInMemory(label, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
               boolean registrationChanged = false;
               if (PayloadRegistrationManager.isExactChannel(normalized)) {
                  registrationChanged = AutismPacketLoggerOverlay.this.payloadRegistrations.load(label, normalized, "custom");
               }

               if (!filterChanged && !registrationChanged) {
                  bi.read("Payload channel already exists.");
               } else {
                  this.patternField.setValue("");
                  this.markDirty();
                  this.rebuildRows();
                  this.scrollOffset = 0;
                  this.scroller.jumpTo(0, Math.max(0, this.rows.size() * 14 - this.listHeight()));
                  AutismPacketLoggerOverlay.this.rowsDirty = true;
                  if (PayloadRegistrationManager.isExactChannel(normalized)) {
                     bi.init("Payload channel added. Press Apply to register.");
                  } else {
                     bi.init("Payload highlight added.");
                  }
               }
            }
         }
      }

      private void init_V_3(String normalized) {
         if (!PayloadRegistrationManager.isExactChannel(normalized)) {
            bi.read("Exact registered channels need namespace:path.");
         } else {
            List<AutismConfig.PayloadChannelRegistrationRule> regs = AutismPacketLoggerOverlay.this.payloadRegistrations.mutableRules();
            if (this.editingRegistrationIndex >= 0 && this.editingRegistrationIndex < regs.size()) {
               int existing = AutismPacketLoggerOverlay.this.payloadRegistrations.indexOf(normalized);
               String oldChannel = "";
               AutismConfig.PayloadChannelRegistrationRule currentRule = this.editingRegistrationIndex >= 0 && this.editingRegistrationIndex < regs.size() ? regs.get(this.editingRegistrationIndex) : null;
               if (currentRule != null) {
                  oldChannel = PayloadRegistrationManager.normalize(currentRule.channel);
               }

               if (existing >= 0 && existing != this.editingRegistrationIndex) {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.write(this.editingRegistrationIndex);
                  AutismPacketLoggerOverlay.this.payloadRegistrations.load(normalized, normalized, "custom");
               } else {
                  AutismConfig.PayloadChannelRegistrationRule rule = regs.get(this.editingRegistrationIndex);
                  if (rule != null && this.isEditableRegistration(rule)) {
                     rule.channel = normalized;
                     rule.label = normalized;
                     rule.source = "custom";
                     rule.enabled = true;
                  }
               }

               if (!oldChannel.isBlank() && !oldChannel.equals(normalized)) {
                  this.read_V_2(oldChannel);
               }

               AutismPacketLoggerOverlay.this.channelFilters.addRuleInMemory(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
               this.cancelEdit();
               this.markDirty();
               this.rebuildRows();
               bi.init("Payload channel edited. Press Apply to register.");
            } else {
               this.cancelEdit();
               this.rebuildRows();
            }
         }
      }

      private void load_V_2(String normalized) {
         List<AutismConfig.PayloadChannelFilterRule> filters = AutismPacketLoggerOverlay.this.channelFilters.getMutableRules();
         if (this.editingFilterIndex >= 0 && this.editingFilterIndex < filters.size()) {
            AutismConfig.PayloadChannelFilterRule rule = filters.get(this.editingFilterIndex);
            if (rule != null && !rule.preset) {
               String oldPattern = PayloadChannelFilterManager.normalizePattern(rule.pattern);
               rule.pattern = normalized;
               rule.label = normalized;
               rule.enabled = true;
               if (PayloadRegistrationManager.isExactChannel(oldPattern)) {
                  this.removeCustomRegistration(oldPattern);
               }

               if (PayloadRegistrationManager.isExactChannel(normalized)) {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.load(normalized, normalized, "custom");
               }
            }

            this.cancelEdit();
            this.markDirty();
            this.rebuildRows();
            bi.init("Payload highlight edited.");
         } else {
            this.cancelEdit();
            this.rebuildRows();
         }
      }

      private void init_V(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null) {
            String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
            boolean enabled = this.isPresetEnabled(preset);
            if (!enabled) {
               AutismPacketLoggerOverlay.this.channelFilters.enablePresetInMemory(preset);
               if (PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.load(preset.load(), pattern, "preset");
               }

               this.markDirty();
            } else {
               if (AutismPacketLoggerOverlay.this.channelFilters.isPresetEnabled(preset)) {
                  AutismPacketLoggerOverlay.this.channelFilters.togglePresetInMemory(preset);
               }

               if (PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
                  this.removePresetRegistration(pattern);
               }

               this.markDirty();
            }
         }
      }

      private void load_V_I_AutismConfig_Paylo(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         if (rule != null) {
            String pattern = PayloadChannelFilterManager.normalizePattern(rule.pattern);
            boolean enabling = !rule.enabled;
            AutismPacketLoggerOverlay.this.channelFilters.toggleRuleInMemory(ruleIndex);
            if (PayloadRegistrationManager.isExactChannel(pattern)) {
               if (enabling) {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.load(rule.label, pattern, "custom");
               } else {
                  this.removeCustomRegistration(pattern);
               }
            }

            this.markDirty();
         }
      }

      private void read_V_I_AutismConfig_Paylo(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         String pattern = rule == null ? "" : PayloadChannelFilterManager.normalizePattern(rule.pattern);
         AutismPacketLoggerOverlay.this.channelFilters.removeRuleInMemory(ruleIndex);
         if (PayloadRegistrationManager.isExactChannel(pattern)) {
            this.removeCustomRegistration(pattern);
         }

         this.markDirty();
      }

      private void load_V_I_AutismConfig_Paylo_2(int registrationIndex, AutismConfig.PayloadChannelRegistrationRule rule) {
         String channel = rule == null ? "" : PayloadRegistrationManager.normalize(rule.channel);
         AutismPacketLoggerOverlay.this.payloadRegistrations.write(registrationIndex);
         this.read_V_2(channel);
      }

      private void read_V_2(String pattern) {
         String normalized = PayloadChannelFilterManager.normalizePattern(pattern);
         if (!normalized.isBlank()) {
            List<AutismConfig.PayloadChannelFilterRule> filters = AutismPacketLoggerOverlay.this.channelFilters.getMutableRules();

            for (int i = filters.size() - 1; i >= 0; i--) {
               AutismConfig.PayloadChannelFilterRule rule = filters.get(i);
               if (rule != null && !rule.preset && normalized.equals(PayloadChannelFilterManager.normalizePattern(rule.pattern))) {
                  filters.remove(i);
               }
            }
         }
      }

      private void load_V(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (preset != null) {
            this.editingFilterIndex = -1;
            this.editingRegistrationIndex = -1;
            String prefix = PayloadChannelFilterManager.editablePrefixFor(preset);
            this.patternField.setValue(prefix);
            this.patternField.setPlaceholder(Component.literal("finish exact channel from " + preset.load()));
            this.patternField.setFocused(true);
            bi.init("Finish the exact payload channel, then Add.");
         }
      }

      private void togglePreset(PayloadChannelFilterManager.Inner_CachePool preset) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked() && preset != null) {
            this.init_V_Str_7("Preset: " + preset.load(), List.of(preset), List.of(preset.read()));
         }
      }

      private void write_V_2(String group) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked() && group != null && !group.isBlank()) {
            List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();
            List<String> patterns = new ArrayList<>();
            AutismPacketLoggerOverlay.ChannelGroup category = this.groups().get(this.cleanGroup(group));
            if (category != null) {
               for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.familyList) {
                  for (PayloadChannelFilterManager.Inner_CachePool preset : family.effectivePresets()) {
                     presets.add(preset);
                     patterns.add(preset.read());
                  }
               }

               for (PayloadChannelFilterManager.Inner_CachePool preset : category.presetList) {
                  presets.add(preset);
                  patterns.add(preset.read());
               }

               this.init_V_Str_7("Category: " + group, presets, patterns);
            }
         }
      }

      private void init_V(AutismPacketLoggerOverlay.ChannelFamilyBucket family) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked() && family != null && !family.presets.isEmpty()) {
            List<String> patterns = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : family.presets) {
               patterns.add(preset.read());
            }

            this.init_V_Str_7("Family: " + family.label, new ArrayList<>(family.presets), patterns);
         }
      }

      private void toggleFilterRule(int ruleIndex, AutismConfig.PayloadChannelFilterRule rule) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked() && rule != null) {
            if (!rule.enabled) {
               AutismPacketLoggerOverlay.this.channelFilters.toggleRuleInMemory(ruleIndex);
               this.markDirty();
            }

            String pattern = PayloadChannelFilterManager.normalizePattern(rule.pattern);
            String label = rule.label != null && !rule.label.isBlank() ? rule.label.trim() : pattern;
            int skipped = this.init_I_Str_4(label, pattern, "custom");
            this.markDirty();
            this.rebuildRows();
            this.init_V_Str_6(label, List.of(pattern), skipped);
         }
      }

      private void init_V_Str_7(String label, List<PayloadChannelFilterManager.Inner_CachePool> presets, List<String> patterns) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked() && presets != null && !presets.isEmpty()) {
            int skipped = 0;

            for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
               if (preset != null) {
                  AutismPacketLoggerOverlay.this.channelFilters.enablePresetInMemory(preset);
                  skipped += this.init_I_Str_4(preset.load(), preset.read(), "preset");
               }
            }

            this.markDirty();
            this.rebuildRows();
            this.init_V_Str_6(label, patterns, skipped);
         }
      }

      private int init_I_Str_4(String label, String pattern, String source) {
         PayloadSubscriptionManager.Inner_CachePool impact = this.impactOf(pattern);
         int skipped = 0;

         for (String channel : impact.channels()) {
            String normalized = PayloadRegistrationManager.normalize(channel);
            if (PayloadRegistrationManager.isExactChannel(normalized)) {
               if (!AutismPacketLoggerOverlay.this.payloadRegistrations.isEnabled(normalized) && !this.fitsChannelCap(List.of(normalized))) {
                  skipped++;
               } else {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.load(label, normalized, source);
               }
            }
         }

         return skipped;
      }

      private void init_V_Str_6(String label, List<String> patterns, int skipped) {
         if (!AutismPacketLoggerOverlay.this.configurationOnly && this.ensureRegistrationUnlocked()) {
            this.saveIfDirty();
            PayloadSubscriptionManager.requestSync();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.MC, false);
            PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.status();
            if (skipped > 0 || status != null && status.read() > 0) {
               int count = status == null ? 0 : status.registered();
               int limit = status == null ? 96 : status.limit();
               bi.write("Payload channel cap: registered " + count + "/" + limit + ".");
            }

            List<String> applied = status == null ? List.of() : status.applied();
            PayloadStudyTracker.init(label, patterns == null ? List.of() : patterns, applied);
            this.setVisible(false);
            if (AutismPacketLoggerOverlay.MC != null) {
               AutismPacketLoggerOverlay.MC.gui.setScreen(null);
            }
         }
      }

      private void toggleGroup(String group) {
         if (group != null && !group.isBlank()) {
            boolean enable = !this.isGroupFullyEnabled(group);
            boolean capHit = false;
            AutismPacketLoggerOverlay.ChannelGroup category = this.groups().get(this.cleanGroup(group));
            if (category != null) {
               List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>();

               for (AutismPacketLoggerOverlay.ChannelFamilyBucket family : category.familyList) {
                  presets.addAll(family.effectivePresets());
               }

               presets.addAll(category.presetList);

               for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
                  String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
                  if (enable) {
                     if (PayloadChannelFilterManager.isRegisterableExactPreset(preset) && !this.fitsChannelCap(List.of(pattern))) {
                        capHit = true;
                     } else {
                        AutismPacketLoggerOverlay.this.channelFilters.enablePresetInMemory(preset);
                        if (PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
                           AutismPacketLoggerOverlay.this.payloadRegistrations.load(preset.load(), pattern, "preset");
                        }
                     }
                  } else {
                     if (AutismPacketLoggerOverlay.this.channelFilters.isPresetEnabled(preset)) {
                        AutismPacketLoggerOverlay.this.channelFilters.togglePresetInMemory(preset);
                     }

                     if (PayloadChannelFilterManager.isRegisterableExactPreset(preset)) {
                        this.removePresetRegistration(pattern);
                     }
                  }
               }

               if (capHit) {
                  this.reportChannelCap();
               }

               this.markDirty();
            }
         }
      }

      private void toggleFamily(AutismPacketLoggerOverlay.ChannelFamilyBucket family) {
         if (family != null && !family.presets.isEmpty()) {
            boolean enable = !this.isFamilyFullyEnabled(family);
            boolean capHit = false;
            List<PayloadChannelFilterManager.Inner_CachePool> presets = new ArrayList<>(family.presets);
            this.sortPresets(presets);

            for (PayloadChannelFilterManager.Inner_CachePool preset : presets) {
               String pattern = PayloadChannelFilterManager.normalizePattern(preset.read());
               if (PayloadRegistrationManager.isExactChannel(pattern)) {
                  if (enable) {
                     if (!AutismPacketLoggerOverlay.this.payloadRegistrations.isEnabled(pattern) && !this.fitsChannelCap(List.of(pattern))) {
                        capHit = true;
                     } else {
                        AutismPacketLoggerOverlay.this.channelFilters.enablePresetInMemory(preset);
                        AutismPacketLoggerOverlay.this.payloadRegistrations.load(preset.load(), pattern, "preset");
                     }
                  } else {
                     if (AutismPacketLoggerOverlay.this.channelFilters.isPresetEnabled(preset)) {
                        AutismPacketLoggerOverlay.this.channelFilters.togglePresetInMemory(preset);
                     }

                     this.removePresetRegistration(pattern);
                  }
               }
            }

            if (capHit) {
               this.reportChannelCap();
            }

            this.markDirty();
         }
      }

      private void removePresetRegistration(String channel) {
         int index = AutismPacketLoggerOverlay.this.payloadRegistrations.indexOf(channel);
         if (index >= 0) {
            List<AutismConfig.PayloadChannelRegistrationRule> rules = AutismPacketLoggerOverlay.this.payloadRegistrations.rules();
            if (index < rules.size()) {
               AutismConfig.PayloadChannelRegistrationRule rule = rules.get(index);
               if (rule != null && "preset".equals(rule.source)) {
                  AutismPacketLoggerOverlay.this.payloadRegistrations.write(index);
               }
            }
         }
      }

      private void removeCustomRegistration(String channel) {
         int index = AutismPacketLoggerOverlay.this.payloadRegistrations.indexOf(channel);
         if (index >= 0) {
            List<AutismConfig.PayloadChannelRegistrationRule> rules = AutismPacketLoggerOverlay.this.payloadRegistrations.rules();
            if (index < rules.size()) {
               AutismConfig.PayloadChannelRegistrationRule rule = rules.get(index);
               if (rule != null) {
                  String source = rule.source == null ? "" : rule.source;
                  if ("custom".equals(source) || "learned".equals(source)) {
                     AutismPacketLoggerOverlay.this.payloadRegistrations.write(index);
                  }
               }
            }
         }
      }

      private void importLearnedChannels(boolean replaceExisting) {
         List<String> learned = PayloadSubscriptionManager.learnedChannels();
         if (learned.isEmpty()) {
            bi.read("No captured payload channels yet.");
         } else {
            if (replaceExisting) {
               AutismPacketLoggerOverlay.this.payloadRegistrations.disableAll();
            }

            int added = 0;
            int skipped = 0;

            for (String channel : learned) {
               String normalized = PayloadRegistrationManager.normalize(channel);
               if (PayloadRegistrationManager.isExactChannel(normalized)) {
                  if (!AutismPacketLoggerOverlay.this.payloadRegistrations.isEnabled(normalized) && !this.fitsChannelCap(List.of(normalized))) {
                     skipped++;
                  } else {
                     boolean regChanged = AutismPacketLoggerOverlay.this.payloadRegistrations.load(normalized, normalized, "learned");
                     AutismPacketLoggerOverlay.this.channelFilters.addRuleInMemory(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
                     if (regChanged) {
                        added++;
                     }
                  }
               }
            }

            this.markDirty();
            this.rebuildRows();
            AutismPacketLoggerOverlay.this.rowsDirty = true;
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

         for (String channel : PayloadSubscriptionManager.learnedChannels()) {
            String normalized = PayloadRegistrationManager.normalize(channel);
            if (PayloadRegistrationManager.isExactChannel(normalized)) {
               if (!AutismPacketLoggerOverlay.this.payloadRegistrations.isEnabled(normalized) && !this.fitsChannelCap(List.of(normalized))) {
                  skipped++;
               } else {
                  boolean regChanged = AutismPacketLoggerOverlay.this.payloadRegistrations.load(normalized, normalized, "learned");
                  AutismPacketLoggerOverlay.this.channelFilters.addRuleInMemory(normalized, normalized, PayloadChannelFilterManager.Inner_DataUtil.ANY);
                  if (regChanged) {
                     added++;
                  }
               }
            }
         }

         this.groups();

         for (PayloadChannelFilterManager.Inner_CachePool preset : this.presetByPattern.values()) {
            if (preset.kind() == PayloadChannelFilterManager.Inner_BufferHelper.EXACT) {
               AutismPacketLoggerOverlay.this.channelFilters.enablePresetInMemory(preset);
               String normalized = PayloadRegistrationManager.normalize(preset.read());
               if (PayloadRegistrationManager.isExactChannel(normalized)) {
                  if (!AutismPacketLoggerOverlay.this.payloadRegistrations.isEnabled(normalized) && !this.fitsChannelCap(List.of(normalized))) {
                     skipped++;
                  } else {
                     boolean regChanged = AutismPacketLoggerOverlay.this.payloadRegistrations.load(preset.load(), normalized, "preset");
                     if (regChanged) {
                        added++;
                     }
                  }
               }
            }
         }

         this.markDirty();
         this.rebuildRows();
         this.scrollOffset = 0;
         this.scroller.jumpTo(0, Math.max(0, this.rows.size() * 14 - this.listHeight()));
         AutismPacketLoggerOverlay.this.rowsDirty = true;
         if (skipped > 0) {
            bi.write("Known channels enabled with " + skipped + " skipped at cap.");
         } else {
            bi.init("Known exact channels enabled. Press Apply.");
         }
      }

      private boolean fitsChannelCap(Collection<String> patterns) {
         return this.projectedChannels(patterns).size() <= this.channelCap;
      }

      private LinkedHashSet<String> projectedChannels(Collection<String> patterns) {
         LinkedHashSet<String> projected = new LinkedHashSet<>();

         for (AutismConfig.PayloadChannelRegistrationRule rule : AutismPacketLoggerOverlay.this.payloadRegistrations.rules()) {
            if (rule != null && rule.enabled) {
               String channel = PayloadRegistrationManager.normalize(rule.channel);
               if (PayloadRegistrationManager.isExactChannel(channel)) {
                  projected.add(channel);
               }
            }
         }

         if (patterns != null) {
            for (String pattern : patterns) {
               PayloadSubscriptionManager.Inner_CachePool impact = this.impactOf(pattern);

               for (String channel : impact.channels()) {
                  String normalized = PayloadRegistrationManager.normalize(channel);
                  if (PayloadRegistrationManager.isExactChannel(normalized)) {
                     projected.add(normalized);
                  }
               }
            }
         }

         return projected;
      }

      private int channelCapLimit() {
         return this.channelCap;
      }

      private int fetchChannelCap() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.status();
         return status != null && status.limit() > 0 ? status.limit() : 96;
      }

      private List<String> groupPatterns(String group) {
         if (group != null && !group.isBlank()) {
            List<String> patterns = new ArrayList<>();

            for (PayloadChannelFilterManager.Inner_CachePool preset : AutismPacketLoggerOverlay.this.channelFilters.getPresets()) {
               if (group.equals(preset.write())) {
                  patterns.add(preset.read());
               }
            }

            return patterns;
         } else {
            return List.of();
         }
      }

      private void reportChannelCap() {
         int limit = this.channelCapLimit();
         int count = Math.min(this.enabledRegistrations.size(), limit);
         bi.write("Payload channel cap reached: " + count + "/" + limit);
      }

      private String subscriptionStatusText() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.status();
         Set<String> applied = new LinkedHashSet<>(status == null ? List.of() : status.applied());
         Set<String> wanted = this.enabledRegistrations;
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
         this.saveIfDirty();
         if (!PayloadSubscriptionManager.isRegistrationUnlocked()) {
            PayloadSubscriptionManager.requestSync();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.MC, false);
            bi.read("Registration locked. Saved, nothing sent.");
            this.rebuildRows();
         } else {
            PayloadSubscriptionManager.requestSync();
            PayloadSubscriptionManager.init(AutismPacketLoggerOverlay.MC, false);
            PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.status();
            if (status != null && status.read() > 0) {
               bi.write("Payload channel cap reached: " + status.registered() + "/" + status.limit());
            } else {
               bi.init("Payload channels applied.");
            }

            this.rebuildRows();
         }
      }

      private void prev_V_6() {
         PayloadSubscriptionManager.Inner_BufferHelper status = PayloadSubscriptionManager.status();
         AutismPacketLoggerOverlay.this.payloadRegistrations.enableApplied(status == null ? List.of() : status.applied());
         AutismPacketLoggerOverlay.this.payloadRegistrations.reloadFromConfig();
         this.rebuildRows();
         bi.init("Payload channels reverted.");
      }

      @Override
      public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
         if (this.update_Z && button == 0) {
            this.setBounds(new OverlayBounds((int)Math.round(mx - this.dragOffsetX), (int)Math.round(my - this.dragOffsetY), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed));
            return true;
         } else if (this.write_Z && button == 0) {
            int listTop = this.put_I_7();
            int listH = this.listHeight();
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
               this.rows.size() * 14, listH, this.windowX + this.windowWidth - 7, listTop, 3, listH, this.scrollOffset
            );
            this.scrollOffset = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, (int)my, this.thumbGrabOffset), 14, metrics.maxScroll());
            return true;
         } else {
            return this.searchField.mouseDragged(mx, my, button, dx, dy) ? true : this.patternField.mouseDragged(mx, my, button, dx, dy);
         }
      }

      @Override
      public boolean mouseReleased(double mx, double my, int button) {
         if (button == 0 && this.update_Z) {
            this.update_Z = false;
            this.saveBounds();
            return true;
         } else if (button == 0 && this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return this.searchField.mouseReleased(mx, my, button) ? true : this.patternField.mouseReleased(mx, my, button);
         }
      }

      @Override
      public boolean mouseScrolled(double mx, double my, double amount) {
         if (this.windowVisible && !this.windowCollapsed && this.containsPoint(mx, my)) {
            int listTop = this.put_I_7();
            int listH = this.listHeight();
            int maxScroll = Math.max(0, this.rows.size() * 14 - listH);
            this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset - (int)Math.signum(amount) * 14, 14, maxScroll);
            this.scroller.jumpTo(this.scrollOffset, maxScroll);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         if (!this.windowVisible) {
            return false;
         } else {
            KeyEvent event = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);
            if (this.searchField.isFocused()) {
               return this.searchField.keyPressed(event);
            } else {
               return this.patternField.isFocused() ? this.patternField.keyPressed(event) : false;
            }
         }
      }

      @Override
      public boolean charTyped(char chr, int modifiers) {
         if (!this.windowVisible) {
            return false;
         } else {
            CharacterEvent event = new CharacterEvent(chr);
            if (this.searchField.isFocused()) {
               return this.searchField.charTyped(event);
            } else {
               return this.patternField.isFocused() ? this.patternField.charTyped(event) : false;
            }
         }
      }

      @Override
      public boolean hasFocusedTextInput() {
         return this.windowVisible && (this.searchField.isFocused() || this.patternField.isFocused());
      }

      @Override
      public void clearInputFocus() {
         this.searchField.setFocused(false);
         this.patternField.setFocused(false);
      }

      private record ColumnMetrics(int init, int load, int read, int write, int update) {
      }
   }
}
