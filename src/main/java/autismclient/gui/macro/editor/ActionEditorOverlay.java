package autismclient.gui.macro.editor;

import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.UiClipStack;
import autismclient.gui.ui.UiDensityMetrics;
import autismclient.gui.f.UiHelper;
import autismclient.gui.ui.widgets.OverlayActionButton;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.ui.widgets.ListRowRenderer;
import autismclient.gui.ui.widgets.ScrollbarMetricsHelper;
import autismclient.gui.ui.widgets.PanelSurfaceHelper;
import autismclient.gui.ui.widgets.ToastNotificationStack;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.gui.ui.layout.ScrollableListView;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismPacketSelectorOverlay;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.BackgroundTaskExecutor;
import autismclient.util.ContainerCloseGuard;
import autismclient.util.AutoCraftingEngine;
import autismclient.util.ContainerClickType;
import autismclient.util.AutismMacro;
import autismclient.util.PayloadEditorModel;
import autismclient.util.PayloadJsonCodec;
import autismclient.util.PayloadFieldCodec;
import autismclient.util.OnlinePlayerCollector;
import autismclient.util.TabPlayerListParser;
import autismclient.util.bi;
import autismclient.util.DraggableOverlayWindow;
import autismclient.util.OverlayManager;
import autismclient.util.ContainerClickSnapshot;
import autismclient.util.PacketNameResolver;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.cA;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import autismclient.util.MacroFolderLayout;
import autismclient.util.RegistryDisplayNameCache;
import autismclient.util.AutismSessionState;
import autismclient.util.ThemePaletteManager;
import autismclient.util.OverlayScreenController;
import autismclient.util.ThemedTextInputBox;
import autismclient.util.MacroClipboardCodec;
import autismclient.util.UiColorPalette;
import autismclient.util.macro.MacroItemTarget;
import autismclient.util.macro.CaptureOutputAction;
import autismclient.util.macro.PayAction;
import autismclient.util.macro.PayloadAction;
import autismclient.util.macro.PlaceAction;
import autismclient.util.macro.RaceAction;
import autismclient.util.macro.InventoryAuditAction;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.ItemAction;
import autismclient.util.macro.DropAction;
import autismclient.util.macro.InteractEntityAction;
import autismclient.util.macro.RotateAction;
import autismclient.util.macro.MacroActionType;
import autismclient.util.macro.PatternCaptureMatcher;
import autismclient.util.macro.SelectSlotAction;
import autismclient.util.macro.LookAtBlockAction;
import autismclient.util.macro.SendPacketAction;
import autismclient.util.macro.MacroCondition;
import autismclient.util.macro.WaitForPacketAction;
import autismclient.util.macro.WaitForPositionDeltaAction;
import autismclient.util.macro.WaitForSlotChangeAction;
import autismclient.util.macro.WaitForSoundAction;
import autismclient.util.macro.WaitForTeleportAction;
import autismclient.util.macro.WaitForWorldChangeAction;
import autismclient.util.macro.WaitGamemodeChangeAction;
import autismclient.util.macro.WaitGuiTypeAction;
import autismclient.util.macro.WaitPosAction;
import autismclient.util.macro.GuiWaitAware;
import autismclient.util.macro.XCarryAction;
import autismclient.util.macro.StoreItemAction;
import autismclient.util.macro.SwapSlotsAction;
import autismclient.util.macro.ak;
import autismclient.util.macro.ToolsAction;
import autismclient.util.macro.UseItemAction;
import autismclient.util.macro.WaitForChatAction;
import autismclient.util.macro.WaitForCooldownAction;
import autismclient.util.macro.WaitForEntityAction;
import autismclient.util.macro.WaitForGuiAction;
import autismclient.util.macro.WaitForHealthAction;
import autismclient.util.macro.WaitForLanStepAction;
import autismclient.util.macro.DynamicBindingManager;
import autismclient.util.macro.MacroVariableStore;
import autismclient.util.macro.MissingAddonAction;
import autismclient.util.macro.OpenContainerAction;
import autismclient.util.macro.PacketClickAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class ActionEditorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final int flush_I = 280;
   private static final int close_I = 184;
   private static final int open_I = 168;
   private static final int poll_I = 64;
   private static final int push_I = 56;
   private static final int pop_I = 4;
   private static final int tick_I = 14;
   private static final int run_I = 13;
   private static final int get_I = 2;
   private static final int set_I = 44;
   private static final int put_I = 68;
   private static final int add_I = 2;
   private static final int clear_I = 60;
   private static final int reset_I = 13;
   private static final int start_I = 4;
   private static final int stop_I = 14;
   private static final int next_I = 14;
   private static final int prev_I = 12;
   private static final int valid_I = 2;
   private static final int size_I = 4;
   private static final int data_I = 52;
   private static final int value_I = 5;
   private static final int cache_I = 34;
   private static final int state_I = 4;
   private static final int queue = 3;
   private static final int buffer = 6;
   private static final int entry = 5;
   private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]{0,63}");
   private static final Pattern VARIABLE_REF = Pattern.compile("(?<!\\{)\\{([A-Za-z_][A-Za-z0-9_.-]{0,63})(?:\\|[^{}]*)?}(?!})");
   private static final int node = 22;
   private static final int task = 5;
   private static final int pool = 14;
   private static final long init_J = 500L;
   private static final int index = 40;
   private static final double init_D = 16.0;
   private static final int count = 12;
   private static final int flags = 16;
   private static final int level = 74;
   private static final int frame = 74;
   private static final int block = 30;
   private static final int chunk = 42;
   private static final int table = 48;
   private static final int store = 18;
   private static final int drain = 126;
   private static final int fill = 138;
   private static final long load_J = 125000000L;
   private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final int mark = -10035062;
   private static final int scan = -38037;
   private static final int parse = 5;
   private static List<String> cachedBlockIds;
   private static List<String> cachedSoundIds;
   private static List<String> cachedEntityTypeIds;
   private static List<String> cachedItemIds;
   private final Font font;
   private final CharEncoder styleTheme = new CharEncoder();
   private final AutismPacketSelectorOverlay packetSelector;
   private final RaceStepSelectorOverlay raceStepSelector;
   private int editorX = 320;
   private int editorY = 60;
   private int editorWidth = 280;
   private int editorHeight = 64;
   private boolean visible = false;
   private boolean dragging = false;
   private double dragOffsetX;
   private double dragOffsetY;
   private boolean visibleBeforeCapture = false;
   private String variableWarning = "";
   // Set by input and edit entry points; the missing-variable warning is recomputed on the next frame only then.
   private boolean variableWarningStale = true;
   private boolean captureAllowed = true;
   private boolean configurationOnly = false;
   private Screen screenBeforeGbreakCapture;
   private Screen screenBeforeCapture;
   private boolean restoreScreenAfterCapture = false;
   private boolean captureSpecificEntity = false;
   private boolean suppressFieldListeners = false;
   private String capturePattern = "";
   private String lastCaptureSource = "";
   private boolean waitForTriggerTouched;
   private MacroAction editingAction;
   private int editCount = 1;
   private int auditEditSession = 0;
   private CompoundTag actionTag;
   private ActionFieldSchema fieldSchema;
   private Runnable onSave;
   private final Map<String, ThemedTextInputBox> textFields = new LinkedHashMap<>();
   private final Map<String, Boolean> toggleStates = new LinkedHashMap<>();
   private final Map<String, Integer> modeIndices = new LinkedHashMap<>();
   private final Map<String, List<String>> listFields = new LinkedHashMap<>();
   private final Map<String, MacroItemTarget> itemTargets = new HashMap<>();
   private final Map<String, List<MacroItemTarget>> itemTargetLists = new HashMap<>();
   private final Map<String, ThemedTextInputBox> internalTextFields = new LinkedHashMap<>();
   private final Map<String, ScrollableListView> listViewports = new HashMap<>();
   private final Map<String, Integer> listEditIndices = new HashMap<>();
   private final Map<String, String> pendingSelections = new HashMap<>();
   private final Map<String, List<Integer>> filteredIndexCache = new HashMap<>();
   private final Map<String, List<String>> filteredOptionCache = new HashMap<>();
   private final Map<String, ScrollableListView> entryViewports = new HashMap<>();
   private final List<MacroExecutor.LoggedChatMessage> chatMessageBuffer = new ArrayList<>();
   private int scrollOffset = 0;
   private ItemAction itemActionDraft;
   private final autismclient.gui.macro.editor.capture.ActiveCaptureSession captureSession = new autismclient.gui.macro.editor.capture.ActiveCaptureSession();
   private String capturingFieldKey;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private final Map<String, autismclient.gui.ui.widgets.DropdownWidget> dropdownCache = new HashMap<>();
   private final List<String> insertTokenOptions = new ArrayList<>();
   private AutismMacro macro;
   private int actionIndex = -1;
   private static final List<String> CONDITION_TYPE_OPTIONS = List.of(
      "Timing / DELAY",
      "GUI / WAIT_GUI",
      "Chat / WAIT_CHAT",
      "Packets / WAIT_PACKET",
      "Packets / WAIT_PACKET_MATCH",
      "Inventory / WAIT_SLOT_CHANGE",
      "Inventory / WAIT_INVENTORY_PREDICATE",
      "Inventory / WAIT_DURABILITY",
      "Inventory / WAIT_FREE_SLOTS",
      "Player / WAIT_HEALTH",
      "Player / WAIT_GAMEMODE_CHANGE",
      "World / WAIT_BLOCK",
      "World / WAIT_ENTITY",
      "World / WAIT_MOVEMENT",
      "World / WAIT_SOUND",
      "State / WAIT_COOLDOWN",
      "Sync / WAIT_LAN_STEP",
      "Sync / WAIT_MACRO_STEP",
      "Sync / TICK_SYNC",
      "Sync / REVISION_SYNC",
      "Sync / SERVER_TICK_SYNC"
   );
   private static final List<String> ACTION_TYPE_OPTIONS = List.of(
      "Inventory / PACKET_CLICK",
      "Inventory / ITEM",
      "Inventory / CLICK",
      "Inventory / CLOSE_GUI",
      "Inventory / INVENTORY",
      "Inventory / SELECT_SLOT",
      "Inventory / SWAP_SLOTS",
      "Inventory / DROP",
      "Inventory / XCARRY",
      "Inventory / CONTAINER_CLICK_SEQUENCE",
      "Packets / SEND_PACKET",
      "Packets / PACKET_GATE",
      "Packets / PACKET_BURST",
      "Packets / PAYLOAD",
      "Chat / SEND_CHAT",
      "Chat / SEND_COMMAND_PACKET",
      "Items / USE_ITEM",
      "Items / USE_ITEM_PHASE",
      "Items / NBT_BOOK",
      "World / MINE",
      "World / INSTA_BREAK",
      "World / BREAK",
      "Movement / VCLIP",
      "Movement / HCLIP",
      "Movement / TP",
      "Movement / SNEAK",
      "Movement / JUMP",
      "Movement / SPRINT",
      "Macro / MACRO_VARIABLES",
      "Macro / CAPTURE_VALUE",
      "Macro / FAKE_GAMEMODE",
      "Macro / ASSERT",
      "Macro / TOGGLE_MODULE",
      "Macro / START_MACRO",
      "Macro / STOP_MACRO",
      "Misc / PAY",
      "Misc / SIGN_EDIT",
      "Misc / RESTORE_GUI",
      "Misc / SAVE_GUI",
      "Misc / DESYNC"
   );
   private List<AutismOverlay> hiddenOverlays;
   private final ToastNotificationStack toasts = new ToastNotificationStack(700L, 140.0F, 180.0F, 4, 4, 18);
   private List<autismclient.util.macro.CraftAction.CraftEntry> craftEntries;
   private List<AutoCraftingEngine.CraftOption> craftOptions;
   private List<AutoCraftingEngine.CraftOption> filteredCraftOptions;
   private AutoCraftingEngine.CraftOption selectedCraftOption;
   private int recipeListScroll;
   private boolean tick_Z;
   private String lastCraftQuery;
   private int[] recipeListBounds;
   private DropAction dropActionDraft;
   private PayloadAction payloadActionDraft;
   private PayloadEditorModel payloadEditor;
   private boolean standalonePayloadMode = false;
   private ActionEditorOverlay.PayloadTemplate payloadTemplate = ActionEditorOverlay.PayloadTemplate.BINARY_REPLAY;
   private int payloadFieldCount = 0;
   private int payloadTabIndex = 0;
   private boolean payloadContentDirty = false;
   private boolean set_Z = false;
   private boolean put_Z = false;
   private boolean add_Z = false;
   private boolean payloadModeUserSelected = false;
   private boolean payloadAddTypeUserSelected = false;
   private boolean suppressPayloadListeners = false;
   private ActionEditorOverlay.PendingPayloadEdit pendingPayloadEdit;
   private long pendingPayloadEditTime;
   private long lastPayloadRevision = -9223372036854775808L;
   private ActionEditorOverlay.PayloadSnapshot cachedPayloadSnapshot;
   private int selectedItemEntry = -1;
   private int selectedDropEntry = -1;
   private int selectedSlotChangeEntry = -1;
   private List<WaitForSlotChangeAction.Inner_DataUtil> slotChangeEntries;
   private WaitForSlotChangeAction.Inner_StreamUtil slotChangeCondition = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
   private int slotChangeCount = 1;
   private boolean suppressItemAddListener = false;
   private boolean suppressDropAddListener = false;
   private boolean suppressSlotChangeListener = false;
   private boolean suppressDropCountListener = false;
   private boolean draggingMatchStrength = false;
   private int matchStrengthSliderX = -1;
   private int matchStrengthSliderY = -1;
   private int matchStrengthSliderWidth = 0;
   private int matchStrengthSliderHeight = 0;
   private boolean draggingSmoothness = false;
   private int smoothnessSliderX = -1;
   private int smoothnessSliderY = -1;
   private int smoothnessSliderWidth = 0;
   private int smoothnessSliderHeight = 0;
   private boolean draggingSafeDelay = false;
   private int safeDelaySliderX = -1;
   private int safeDelaySliderY = -1;
   private int safeDelaySliderWidth = 0;
   private int safeDelaySliderHeight = 0;
   private boolean suppressPatternListener = false;
   private static final List<PayloadFieldCodec.FieldType> PAYLOAD_FIELD_TYPES = List.of(
      PayloadFieldCodec.FieldType.BYTE,
      PayloadFieldCodec.FieldType.UNSIGNED_BYTE,
      PayloadFieldCodec.FieldType.BOOLEAN,
      PayloadFieldCodec.FieldType.SHORT,
      PayloadFieldCodec.FieldType.UNSIGNED_SHORT,
      PayloadFieldCodec.FieldType.CHAR,
      PayloadFieldCodec.FieldType.INT,
      PayloadFieldCodec.FieldType.LONG,
      PayloadFieldCodec.FieldType.FLOAT,
      PayloadFieldCodec.FieldType.DOUBLE,
      PayloadFieldCodec.FieldType.JAVA_WRITE_UTF,
      PayloadFieldCodec.FieldType.RAW_BYTES,
      PayloadFieldCodec.FieldType.BYTE_ARRAY,
      PayloadFieldCodec.FieldType.STRING_BYTES,
      PayloadFieldCodec.FieldType.VAR_INT,
      PayloadFieldCodec.FieldType.VAR_LONG,
      PayloadFieldCodec.FieldType.MINECRAFT_STRING,
      PayloadFieldCodec.FieldType.IDENTIFIER,
      PayloadFieldCodec.FieldType.UUID_FIELD,
      PayloadFieldCodec.FieldType.BLOCK_POS,
      PayloadFieldCodec.FieldType.ENUM_VAR_INT,
      PayloadFieldCodec.FieldType.OPTIONAL_VALUE,
      PayloadFieldCodec.FieldType.NBT,
      PayloadFieldCodec.FieldType.ITEM_STACK,
      PayloadFieldCodec.FieldType.TEXT_COMPONENT
   );
   private static final List<String> PAYLOAD_FIELD_TYPE_LABELS = buildPayloadFieldTypeLabels();
   private static final List<String> PAYLOAD_MODE_LABELS = List.of("Raw UTF-8", "Hex", "DataOutput", "ByteBuf", "JSON", "Mixed");
   private static final List<PayloadFieldCodec.EncodingMode> PAYLOAD_MODES = List.of(
      PayloadFieldCodec.EncodingMode.RAW_UTF8,
      PayloadFieldCodec.EncodingMode.MANUAL_HEX,
      PayloadFieldCodec.EncodingMode.JAVA_DATA_OUTPUT,
      PayloadFieldCodec.EncodingMode.MINECRAFT_BYTEBUF,
      PayloadFieldCodec.EncodingMode.JSON_TEXT,
      PayloadFieldCodec.EncodingMode.ADVANCED_MIXED
   );
   private List<WaitForLanStepAction.StepEntry> lanStepEntries;
   private List<autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool> packetMatchRules;
   private int selectedPacketRule = -1;
   private final List<String> playerNameOptions = new ArrayList<>();
   private final Map<String, String> get_Map = new HashMap<>();
   private final Map<String, Component> set_Map = new HashMap<>();
   private boolean playerOptionsLoaded = false;
   private final List<String> autismModuleNames = new ArrayList<>();
   private List<ak.Inner_DataUtil> toggleModuleEntries;
   private Runnable onPreSave;
   private final List<ActionEditorOverlay.ClickRegion> valid_List = new ArrayList<>();
   private autismclient.gui.f.NetHelper bodyClip;
   private float frameDelta = 0.0F;
   private final List<ActionEditorOverlay.ScrollbarRegion> scrollbarRegions = new ArrayList<>();
   private IntConsumer activeScrollbarDrag = null;
   private static ActionEditorOverlay instance;
   private static final List<String> BUILTIN_VARIABLE_TOKENS = List.of(
      "{user}", "{pos}", "{rot}", "{x}", "{y}", "{z}", "{uuid}", "{dimension}", "{facing}", "{server}", "{selected_slot}", "{timestamp}"
   );
   private List<String> cachedTabPlayerNames;
   private long tabPlayerNamesFetchedAt;

   private static List<String> buildPayloadFieldTypeLabels() {
      List<String> labels = new ArrayList<>();

      for (PayloadFieldCodec.FieldType type : PAYLOAD_FIELD_TYPES) {
         labels.add(type.label());
      }

      return List.copyOf(labels);
   }

   public static ActionEditorOverlay getInstance() {
      if (instance == null) {
         instance = new ActionEditorOverlay(MC.font);
         OverlayManager.getInstance().register(instance);
      }

      return instance;
   }

   private static boolean usesCompactLayout(MacroActionType type) {
      if (type == null) {
         return false;
      }

      boolean var10000;
      switch (type) {
         case INVENTORY:
         case RESTORE_GUI:
         case SAVE_GUI:
         case SEND_TOGGLE:
         case DESYNC:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private int windowWidthFor(MacroAction action) {
      return usesCompactLayout(action == null ? null : action.getType()) ? 184 : 280;
   }

   private int defaultWindowHeight() {
      return usesCompactLayout(this.editingAction == null ? null : this.editingAction.getType()) ? 56 : 64;
   }

   public static boolean isEditable(MacroAction action) {
      if (action == null) {
         return false;
      }

      if (action instanceof MissingAddonAction) {
         return false;
      }

      MacroActionType type = action.getType();
      if (type == null) {
         return !ActionFieldRegistry.init(action).fields().isEmpty();
      }

      boolean var10000;
      switch (type) {
         case CRAFT:
         case DROP:
         case ITEM:
         case PAYLOAD:
         case WAIT_LAN_STEP:
         case WAIT_PACKET:
         case WAIT_PACKET_MATCH:
         case MULTI:
         case WAIT_SLOT_CHANGE:
            var10000 = true;
            break;
         default:
            var10000 = !ActionFieldRegistry.init(type).fields().isEmpty();
      }

      return var10000;
   }

   public static ActionEditorOverlay getExisting() {
      return instance;
   }

   public boolean isEditing(MacroAction action) {
      return this.visible && !this.standalonePayloadMode && this.editingAction == action;
   }

   public void setCaptureAllowed(boolean allowed) {
      this.captureAllowed = allowed;
      if (!allowed) {
         this.poll_Z_2();
      }
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      this.configurationOnly = configurationOnly;
      if (configurationOnly) {
         this.setCaptureAllowed(false);
      }
   }

   private boolean ensureCaptureAvailable() {
      if (this.isCaptureAvailable()) {
         return true;
      }

      bi.read("Pick/Capture only works in-game.");
      return false;
   }

   private boolean isCaptureAvailable() {
      if (this.captureAllowed) {
         return true;
      }

      boolean inWorld = MC != null && MC.player != null && MC.level != null;
      if (!inWorld) {
         return false;
      }

      this.configurationOnly = false;
      this.captureAllowed = true;
      return true;
   }

   private boolean canCapture(CaptureMode mode) {
      return mode == CaptureMode.CUSTOM_MENU_BUTTON ? true : mode == CaptureMode.PACKET_NAME || this.isCaptureAvailable();
   }

   public void onActionsChanged(List<MacroAction> actions) {
      if (this.visible && !this.standalonePayloadMode && this.editingAction != null && actions != null && !actions.contains(this.editingAction)) {
         this.finishEditing(false);
      }
   }

   public void closeIfEditing(MacroAction action) {
      if (this.isEditing(action)) {
         this.finishEditing(false);
      }
   }

   public ActionEditorOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.packetSelector = new AutismPacketSelectorOverlay(textRenderer);
      this.raceStepSelector = new RaceStepSelectorOverlay(textRenderer);
   }

   public void setContext(AutismMacro macro, int actionIndex) {
      this.macro = macro;
      this.actionIndex = actionIndex;
      this.rebuildInsertTokenOptions();
   }

   public void openAction(MacroAction action, Runnable onPreSave, Runnable onSave) {
      this.openAction(action, onPreSave, onSave, 1);
   }

   public void openAction(MacroAction action, Runnable onPreSave, Runnable onSave, int editCount) {
      this.variableWarningStale = true;
      this.restoreHiddenOverlays();
      this.clearCaptureCallbacks(true);
      this.auditEditSession++;
      this.standalonePayloadMode = false;
      this.editingAction = action;
      this.editCount = Math.max(1, editCount);
      this.onPreSave = onPreSave;
      this.onSave = onSave;
      this.actionTag = AutismMacro.serializeAction(action);
      this.fieldSchema = ActionFieldRegistry.init(action);
      this.scrollOffset = 0;
      this.rebuildInsertTokenOptions();
      this.textFields.clear();
      this.toggleStates.clear();
      this.modeIndices.clear();
      this.listFields.clear();
      this.itemTargets.clear();
      this.itemTargetLists.clear();
      this.internalTextFields.clear();
      this.listViewports.clear();
      this.listEditIndices.clear();
      this.pendingSelections.clear();
      this.filteredIndexCache.clear();
      this.filteredOptionCache.clear();
      this.entryViewports.clear();
      this.packetSelector.resetSelection();
      this.loadItemTargetFields(action);
      this.applyFieldsToAction(action);
      if (action instanceof UseItemAction useItemAction) {
         ThemedTextInputBox slotField = this.textFields.get("slot");
         if (slotField != null && useItemAction.slot < 0 && !this.actionTag.contains("slot")) {
            slotField.setValue("");
         }
      }

      this.itemActionDraft = null;
      this.captureSession.clear();
      this.selectedItemEntry = -1;
      this.selectedDropEntry = -1;
      if (action instanceof ItemAction ia) {
         this.itemActionDraft = new ItemAction();
         this.itemActionDraft.itemNames = new ArrayList<>(ia.itemNames);
         this.itemActionDraft.itemTargets = this.resolveItemTargets(ia.itemTargets, ia.itemNames);
         this.itemActionDraft.itemTimes = new ArrayList<>(ia.itemTimes);
         this.itemActionDraft.itemActionIdx = new ArrayList<>(ia.itemActionIdx);
         this.itemActionDraft.itemButtons = new ArrayList<>(ia.itemButtons);
         this.itemActionDraft.itemSearchScopes = new ArrayList<>(ia.itemSearchScopes);
         this.itemActionDraft.stackAmountModes = new ArrayList<>(ia.stackAmountModes);

         while (this.itemActionDraft.itemTimes.size() < this.itemActionDraft.itemNames.size()) {
            this.itemActionDraft.itemTimes.add(1);
         }

         while (this.itemActionDraft.itemActionIdx.size() < this.itemActionDraft.itemNames.size()) {
            this.itemActionDraft.itemActionIdx.add(0);
         }

         while (this.itemActionDraft.itemButtons.size() < this.itemActionDraft.itemNames.size()) {
            this.itemActionDraft.itemButtons.add(0);
         }

         while (this.itemActionDraft.itemSearchScopes.size() < this.itemActionDraft.itemNames.size()) {
            int index = this.itemActionDraft.itemSearchScopes.size();
            this.itemActionDraft.itemSearchScopes.add(ia.getItemSearchScope(index).ordinal());
         }

         while (this.itemActionDraft.stackAmountModes.size() < this.itemActionDraft.itemNames.size()) {
            this.itemActionDraft.stackAmountModes.add(0);
         }

         this.itemActionDraft.targetSlot = ia.targetSlot;
         this.itemActionDraft.useSlot = ia.useSlot;
         this.itemActionDraft.actionIndex = ia.actionIndex;
         this.itemActionDraft.button = ia.button;
         this.itemActionDraft.times = ia.times;
         this.itemActionDraft.waitForGuiBefore = ia.waitForGuiBefore;
         this.itemActionDraft.waitForGuiAfter = ia.waitForGuiAfter;
         this.itemActionDraft.guiName = ia.guiName != null ? ia.guiName : "";
         this.itemActionDraft.waitForItem = ia.waitForItem;
         this.itemActionDraft.useCursorItemForPickupAll = ia.useCursorItemForPickupAll;

         for (int i = 0; i < this.itemActionDraft.itemNames.size(); i++) {
            ThemedTextInputBox f = this.newTextField(28);
            f.setIntegerOnlyFilter(true);
            f.setValue(String.valueOf(this.itemActionDraft.getItemTimes(i)));
            this.textFields.put("item_times_" + i, f);
         }

         ThemedTextInputBox addF = this.newTextField(120);
         addF.setPlaceholder(Component.literal("Item name"));
         addF.setChangedListener(text -> this.onItemEntryFieldChanged());
         this.internalTextFields.put("_item_add", addF);
         ThemedTextInputBox addSlotF = this.newTextField(52);
         addSlotF.setIntegerOnlyFilter(true);
         addSlotF.setPlaceholder(Component.literal("Slot"));
         addSlotF.setChangedListener(text -> this.onItemEntryFieldChanged());
         this.textFields.put("item_entrySlot", addSlotF);
         this.toggleStates.put("item_waitForGuiBefore", ia.waitForGuiBefore);
         this.toggleStates.put("item_waitForGuiAfter", ia.waitForGuiAfter);
         this.toggleStates.put("item_waitForItem", ia.waitForItem);
         this.toggleStates.put("item_useCursorItemForPickupAll", ia.useCursorItemForPickupAll);
         this.toggleStates.put("item_useClickDelay", ia.useClickDelay);
         ThemedTextInputBox guiF = this.newTextField(80);
         guiF.setValue(ia.guiName != null ? ia.guiName : "");
         this.textFields.put("item_guiName", guiF);
         ThemedTextInputBox delayF = this.newTextField(60);
         delayF.setIntegerOnlyFilter(true);
         delayF.setValue(String.valueOf(Math.max(0, ia.clickDelayMs)));
         this.textFields.put("item_clickDelayMs", delayF);
      }

      this.selectedSlotChangeEntry = -1;
      this.slotChangeEntries = new ArrayList<>();
      this.slotChangeCondition = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
      this.slotChangeCount = 1;
      if (action instanceof WaitForSlotChangeAction wsc) {
         for (WaitForSlotChangeAction.Inner_DataUtil e : wsc.entries) {
            this.slotChangeEntries.add(e.copy());
         }
      }

      if (action.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
         this.toggleStates.put("listenDuringPreviousAction", action.listensDuringPreviousAction());
         ThemedTextInputBox wscAddF = this.newTextField(120);
         wscAddF.setPlaceholder(Component.literal("Item name (optional)"));
         wscAddF.setChangedListener(text -> this.applySlotChangeEntryEdit());
         this.internalTextFields.put("_wsc_add", wscAddF);
         ThemedTextInputBox wscSlotF = this.newTextField(52);
         wscSlotF.setIntegerOnlyFilter(true);
         wscSlotF.setPlaceholder(Component.literal("Slot #"));
         wscSlotF.setChangedListener(text -> this.applySlotChangeEntryEdit());
         this.textFields.put("wsc_slot", wscSlotF);
         ThemedTextInputBox wscCountF = this.newTextField(36);
         wscCountF.setIntegerOnlyFilter(true);
         wscCountF.setPlaceholder(Component.literal("Count"));
         wscCountF.setChangedListener(text -> this.applySlotChangeCount());
         this.textFields.put("wsc_count", wscCountF);
      }

      this.craftEntries = null;
      this.craftOptions = null;
      this.filteredCraftOptions = null;
      this.selectedCraftOption = null;
      this.recipeListScroll = 0;
      this.tick_Z = false;
      this.lastCraftQuery = null;
      this.recipeListBounds = null;
      if (action instanceof autismclient.util.macro.CraftAction craftAction) {
         this.craftEntries = craftAction.copyEntries();

         for (int i = 0; i < this.craftEntries.size(); i++) {
            autismclient.util.macro.CraftAction.CraftEntry entry = this.craftEntries.get(i);
            ThemedTextInputBox f = this.newTextField(44);
            f.setIntegerOnlyFilter(true);
            f.setValue(String.valueOf(entry.amount));
            this.textFields.put("craft_amount_" + i, f);
            this.toggleStates.put("craft_useMax_" + i, entry.useMaxAmount);
         }

         ThemedTextInputBox amtF = this.newTextField(44);
         amtF.setIntegerOnlyFilter(true);
         amtF.setValue("1");
         this.textFields.put("_craft_amount", amtF);
         ThemedTextInputBox srchF = this.newTextField(180);
         srchF.setPlaceholder(Component.literal("Search recipes..."));
         this.internalTextFields.put("_craft_search", srchF);
         this.craftOptions = AutoCraftingEngine.scanOptionsForClient(MC);
         this.filteredCraftOptions = AutoCraftingEngine.filterOptions(this.craftOptions, "");
      }

      if (action instanceof WaitForPacketAction waitForPacketAction) {
         this.listFields.put("packetNames", this.sanitizePacketTargets(waitForPacketAction.init()));
         this.toggleStates.put("listenDuringPreviousAction", waitForPacketAction.listenDuringPreviousAction);
      }

      this.packetMatchRules = null;
      this.selectedPacketRule = -1;
      if (action instanceof autismclient.util.macro.WaitPacketMatchAction packetMatchAction) {
         this.packetMatchRules = new ArrayList<>();

         for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : packetMatchAction.effectiveRules()) {
            this.packetMatchRules.add(rule.copy());
         }

         if (this.packetMatchRules.isEmpty()) {
            this.packetMatchRules.add(new autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool());
         }

         this.selectedPacketRule = 0;
         this.toggleStates.put("listenDuringPreviousAction", packetMatchAction.listenDuringPreviousAction);
         ThemedTextInputBox valueField = this.newTextField(120);
         valueField.setPlaceholder(Component.literal("Value"));
         this.textFields.put("_wpm_value", valueField);
         this.loadPacketRuleValue();
      }

      this.dropActionDraft = null;
      if (action instanceof DropAction da) {
         this.dropActionDraft = new DropAction();
         this.dropActionDraft.mode = da.mode;
         this.dropActionDraft.dropCount = da.dropCount;
         this.dropActionDraft.itemNames = new ArrayList<>(da.itemNames);
         this.dropActionDraft.itemTargets = this.resolveItemTargets(da.itemTargets, da.itemNames);
         this.dropActionDraft.itemCounts = new ArrayList<>(da.itemCounts);
         this.dropActionDraft.waitForGuiBefore = da.waitForGuiBefore;
         this.dropActionDraft.waitForGuiAfter = da.waitForGuiAfter;
         this.dropActionDraft.guiName = da.guiName != null ? da.guiName : "";
         this.dropActionDraft.useHandlerSlots = da.useHandlerSlots;

         while (this.dropActionDraft.itemCounts.size() < this.dropActionDraft.itemNames.size()) {
            this.dropActionDraft.itemCounts.add(this.dropActionDraft.getItemCount(this.dropActionDraft.itemCounts.size()));
         }

         for (int i = 0; i < this.dropActionDraft.itemNames.size(); i++) {
            ThemedTextInputBox f = this.newTextField(32);
            f.setIntegerOnlyFilter(true);
            f.setValue(String.valueOf(this.dropActionDraft.itemCounts.get(i)));
            this.textFields.put("drop_count_" + i, f);
         }

         ThemedTextInputBox cntF = this.newTextField(60);
         cntF.setIntegerOnlyFilter(true);
         cntF.setValue(String.valueOf(da.dropCount));
         cntF.setChangedListener(text -> this.onDropCountFieldChanged());
         this.textFields.put("drop_globalCount", cntF);
         ThemedTextInputBox addDropF = this.newTextField(120);
         addDropF.setPlaceholder(Component.literal("Item name..."));
         addDropF.setChangedListener(text -> this.onDropEntryFieldChanged());
         this.internalTextFields.put("_drop_add", addDropF);
         ThemedTextInputBox addDropSlotF = this.newTextField(52);
         addDropSlotF.setIntegerOnlyFilter(true);
         addDropSlotF.setPlaceholder(Component.literal("Slot"));
         addDropSlotF.setChangedListener(text -> this.onDropEntryFieldChanged());
         this.textFields.put("drop_entrySlot", addDropSlotF);
         ThemedTextInputBox dropGuiF = this.newTextField(80);
         dropGuiF.setValue(da.guiName != null ? da.guiName : "");
         this.textFields.put("drop_guiName", dropGuiF);
         this.toggleStates.put("drop_waitForGuiBefore", da.waitForGuiBefore);
         this.toggleStates.put("drop_waitForGuiAfter", da.waitForGuiAfter);
         this.toggleStates.put("drop_useHandlerSlots", da.useHandlerSlots);
         DropAction.Inner_DataUtil[] modes = DropAction.Inner_DataUtil.values();
         int mi = 0;

         for (int i = 0; i < modes.length; i++) {
            if (modes[i] == da.mode) {
               mi = i;
               break;
            }
         }

         this.modeIndices.put("drop_mode", mi);
         this.syncDropCountField();
      }

      this.payloadActionDraft = null;
      this.payloadEditor = null;
      if (action instanceof PayloadAction pa) {
         this.payloadActionDraft = new PayloadAction();
         this.payloadActionDraft.fromTag(pa.toTag());
         this.initPayloadEditor(this.payloadActionDraft);
      }

      this.lanStepEntries = null;
      if (action instanceof WaitForLanStepAction wls) {
         this.lanStepEntries = new ArrayList<>();

         for (WaitForLanStepAction.StepEntry e : wls.entries) {
            this.lanStepEntries.add(new WaitForLanStepAction.StepEntry(e.username, e.step));
         }

         for (int i = 0; i < this.lanStepEntries.size(); i++) {
            WaitForLanStepAction.StepEntry e = this.lanStepEntries.get(i);
            ThemedTextInputBox uf = this.newTextField(80);
            uf.setValue(e.username);
            this.textFields.put("lan_user_" + i, uf);
            ThemedTextInputBox sf = this.newTextField(40);
            sf.setIntegerOnlyFilter(true);
            sf.setValue(String.valueOf(e.step));
            this.textFields.put("lan_step_" + i, sf);
         }

         ThemedTextInputBox newUF = this.newTextField(80);
         newUF.setPlaceholder(Component.literal("Peer name..."));
         this.internalTextFields.put("_lan_user_add", newUF);
         ThemedTextInputBox newSF = this.newTextField(40);
         newSF.setIntegerOnlyFilter(true);
         newSF.setValue("1");
         this.internalTextFields.put("_lan_step_add", newSF);
         ThemedTextInputBox dsF = this.newTextField(60);
         dsF.setIntegerOnlyFilter(true);
         dsF.setValue(String.valueOf(wls.defaultStep));
         this.textFields.put("lan_defaultStep", dsF);
         this.toggleStates.put("lan_filterByUser", wls.filterByUser);
         this.toggleStates.put("listenDuringPreviousAction", wls.listenDuringPreviousAction);
      }

      this.captureSpecificEntity = false;
      if (action instanceof WaitForSoundAction) {
         ThemedTextInputBox search = this.internalTextFields.get("soundIds");
         if (search != null) {
            search.setPlaceholder(Component.literal("Search sound id..."));
         }
      }

      if (action instanceof WaitForEntityAction) {
         ThemedTextInputBox search = this.internalTextFields.get("entityIds");
         if (search != null) {
            search.setPlaceholder(Component.literal("Search entity type..."));
         }
      }

      if (action instanceof LookAtBlockAction) {
         ThemedTextInputBox search = this.internalTextFields.get("entityIds");
         if (search != null) {
            search.setPlaceholder(Component.literal("Search entity type..."));
         }
      }

      if (action instanceof StoreItemAction) {
         ThemedTextInputBox search = this.internalTextFields.get("targetItems");
         if (search != null) {
            search.setPlaceholder(Component.literal("Search item id..."));
         }
      }

      if (action instanceof InventoryAuditAction) {
         ThemedTextInputBox search = this.internalTextFields.get("targetItems");
         if (search != null) {
            search.setPlaceholder(Component.literal("Item name"));
         }
      }

      this.playerNameOptions.clear();
      this.get_Map.clear();
      this.set_Map.clear();
      this.playerOptionsLoaded = false;
      if (action instanceof PayAction) {
         ThemedTextInputBox search = this.internalTextFields.get("players");
         if (search != null) {
            search.setPlaceholder(Component.literal("Search or add player..."));
         }
      }

      this.autismModuleNames.clear();
      this.toggleModuleEntries = null;
      if (action instanceof ak) {
         ThemedTextInputBox search = this.newTextField(180);
         search.setPlaceholder(Component.literal("Search module..."));
         this.internalTextFields.put("_toggle_module_search", search);
         this.refreshModuleRegistry();
         ak toggleModuleAction = (ak)action;
         this.toggleModuleEntries = new ArrayList<>();

         for (ak.Inner_DataUtil entry : toggleModuleAction.entries) {
            if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
               this.toggleModuleEntries.add(new ak.Inner_DataUtil(entry.init_Str, entry.init_ak_StreamUtil));
            }
         }
      }

      if (action instanceof WaitForChatAction) {
         ThemedTextInputBox search = this.newTextField(180);
         search.setPlaceholder(Component.literal("Search recent chat..."));
         this.internalTextFields.put("_wait_chat_search", search);
         ThemedTextInputBox captureName = this.newTextField(100);
         captureName.setPlaceholder(Component.literal("Variable name"));
         this.internalTextFields.put("_capture_name", captureName);
         this.draggingMatchStrength = false;
         this.resetMatchStrengthSlider();
         ThemedTextInputBox patternField = this.textFields.get("pattern");
         if (patternField != null) {
            patternField.setMultiline(true);
            patternField.setHeight(40);
            String existingPatternJson = this.actionTag.getStringOr("patternJson", "");
            Component initialPattern = existingPatternJson != null && !existingPatternJson.isBlank()
               ? this.patternDisplayComponent(this.actionTag.getStringOr("pattern", ""))
               : Component.literal(MacroExecutor.cleanVisibleChatText(this.actionTag.getStringOr("pattern", "")));
            String visiblePattern = initialPattern.getString();
            this.actionTag.putString("pattern", visiblePattern);
            if (existingPatternJson != null && !existingPatternJson.isBlank()) {
               this.actionTag.putString("patternJson", MacroExecutor.serializeComponentJson(initialPattern));
            } else {
               this.actionTag.putString("patternJson", "");
            }

            patternField.setDisplayTextProvider(this::patternDisplayComponent);
            this.suppressPatternListener = true;
            patternField.setValue(visiblePattern);
            this.suppressPatternListener = false;
            patternField.setChangedListener(value -> {
               if (!this.suppressPatternListener) {
                  String visibleValue = MacroExecutor.cleanVisibleChatText(value);
                  this.actionTag.putString("pattern", visibleValue);
                  this.actionTag.putString("patternJson", "");
               }
            });
         }
      } else {
         this.suppressPatternListener = false;
         this.draggingMatchStrength = false;
         this.resetMatchStrengthSlider();
      }

      if (action instanceof WaitForGuiAction) {
         ThemedTextInputBox captureName = this.newTextField(100);
         captureName.setPlaceholder(Component.literal("Variable name"));
         this.internalTextFields.put("_capture_name", captureName);
      }

      if (action instanceof autismclient.util.macro.CaptureValueAction) {
         ThemedTextInputBox token = this.newTextField(180);
         token.setEditable(false);
         token.setFocusEffectsEnabled(false);
         token.setPlaceholder(Component.literal("Set a variable name"));
         this.internalTextFields.put("_capture_token", token);
         ThemedTextInputBox preview = this.newTextField(180);
         preview.setEditable(false);
         preview.setFocusEffectsEnabled(false);
         preview.setPlaceholder(Component.literal("Nothing available now"));
         this.internalTextFields.put("_capture_preview", preview);
         this.capturePattern = autismclient.util.macro.CaptureValueAction.init(this.textFieldValueOr("exampleText", ""), this.textFieldValueOr("selectedText", ""), this.textFieldValueOr("saveAs", "value"));
         this.lastCaptureSource = "";
         this.waitForTriggerTouched = false;

         for (String key : List.of("saveAs", "exampleText", "selectedText")) {
            ThemedTextInputBox field = this.textFields.get(key);
            if (field != null) {
               field.setChangedListener(ignored -> this.updateCapturePatternFromExample());
            }
         }
      } else {
         this.suppressFieldListeners = false;
         this.capturePattern = "";
      }

      if (action instanceof RotateAction) {
         this.draggingSmoothness = false;
         this.resetSmoothnessSlider();
         RotateAction rotateAction = (RotateAction)action;
         ThemedTextInputBox smoothnessField = this.textFields.get("smoothness");
         int smoothness = RotateAction.clampSmoothness(rotateAction.smoothness);
         this.actionTag.putInt("smoothness", smoothness);
         if (smoothnessField != null) {
            smoothnessField.setValue(String.valueOf(smoothness));
         }
      } else if (action instanceof LookAtBlockAction lookAtAction) {
         this.draggingSmoothness = false;
         this.resetSmoothnessSlider();
         ThemedTextInputBox smoothnessField = this.textFields.get("smoothness");
         int smoothness = RotateAction.clampSmoothness(lookAtAction.smoothness);
         this.actionTag.putInt("smoothness", smoothness);
         if (smoothnessField != null) {
            smoothnessField.setValue(String.valueOf(smoothness));
         }
      } else {
         this.draggingSmoothness = false;
         this.resetSmoothnessSlider();
      }

      if (action instanceof XCarryAction xCarryAction) {
         this.draggingSafeDelay = false;
         this.resetSafeDelaySlider();
         ThemedTextInputBox delayField = this.textFields.get("safeClickDelayTicks");
         int safeDelay = XCarryAction.write(xCarryAction.safeClickDelayTicks);
         this.actionTag.putInt("safeClickDelayTicks", safeDelay);
         this.actionTag.putBoolean("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
         this.actionTag.putBoolean("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
         this.toggleStates.put("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
         this.toggleStates.put("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
         if (delayField != null) {
            delayField.setValue(String.valueOf(safeDelay));
         }
      } else {
         this.draggingSafeDelay = false;
         this.resetSafeDelaySlider();
      }

      if (action.getType() == MacroActionType.INVENTORY || action.getType() == MacroActionType.CLOSE_GUI || action.getType() == MacroActionType.SAVE_GUI) {
         this.toggleStates.put("sendPacket", !this.actionTag.getBooleanOr("sendPacket", true));
      }

      if (action.getType() == MacroActionType.STORE_ITEM) {
         this.toggleStates.put("closeSendPkt", !this.actionTag.getBooleanOr("closeSendPkt", true));
      }

      this.attachAllItemTargetStyling();
      this.applyFieldPlaceholders();
      if (action.getType() == MacroActionType.PLACE) {
         ThemedTextInputBox slotF = this.newTextField(52);
         slotF.setIntegerOnlyFilter(true);
         slotF.setPlaceholder(Component.literal("Slot"));
         slotF.setChangedListener(text -> this.syncPlaceItemTarget());
         this.textFields.put("place_itemSlot", slotF);
         if (action instanceof PlaceAction placeAction) {
            this.toggleStates.put("place_waitForItem", placeAction.waitForItem);
            this.toggleStates.put("place_silentSwitch", placeAction.silentSwitch);
         }

         MacroItemTarget placeTarget = this.itemTargets.get("itemName");
         if (placeTarget == null && action instanceof PlaceAction placeAction) {
            placeTarget = this.resolveItemTarget(placeAction.itemTarget, placeAction.itemName);
         }

         if (placeTarget != null) {
            slotF.setValue(placeTarget.hasSlot() ? String.valueOf(placeTarget.slot) : "");
         }

         ThemedTextInputBox nameF = this.textFields.get("itemName");
         if (nameF != null) {
            nameF.setChangedListener(text -> this.syncPlaceItemTarget());
         }

         if (action instanceof PlaceAction placeAction && placeAction.blockPos != null) {
            autismclient.util.BlockPreviewRenderer.init(placeAction.blockPos, -50373);
         }
      }

      this.editorWidth = this.windowWidthFor(action);
      this.refreshVariableWarning();
      int contentH = this.itemEditorHeight();
      int desiredH = 18 + contentH + this.footerHeight() + 4;
      int minH = this.defaultWindowHeight();
      int maxH = Math.max(minH, cA.load_I() * 4 / 5);
      this.editorHeight = Math.max(minH, Math.min(maxH, desiredH));
      this.visible = true;
      OverlayManager.getInstance().bringToFront(this);
   }

   public void openPayload(PayloadAction action) {
      this.openAction(action, null, null);
      this.standalonePayloadMode = true;
   }

   public boolean init_Z_Str(String channel) {
      String target = channel == null ? "" : channel.trim();
      if (!this.visible || target.isBlank()) {
         return false;
      }

      if (!this.standalonePayloadMode && this.payloadActionDraft == null && !(this.editingAction instanceof PayloadAction)) {
         return false;
      }

      ThemedTextInputBox channelField = this.textFields.get("payload_channel");
      if (channelField == null) {
         return false;
      }

      this.suppressPayloadListeners = true;

      try {
         channelField.setValue(target);
         if (this.payloadEditor != null) {
            this.payloadEditor.channel = target;
            this.payloadEditor.bumpRevision();
            this.cachedPayloadSnapshot = null;
         }

         if (this.payloadActionDraft != null) {
            this.payloadActionDraft.channel = target;
         }

         ThemedTextInputBox contentField = this.textFields.get("payload_content");
         if (contentField != null) {
            contentField.setValue(this.replaceScriptChannel(contentField.getValue(), target));
         }

         this.put_Z = true;
      } finally {
         this.suppressPayloadListeners = false;
      }

      this.resizeEditorToContent();
      OverlayManager.getInstance().bringToFront(this);
      return true;
   }

   private String replaceScriptChannel(String script, String channel) {
      String safeScript = script == null ? "" : script.replace("\r\n", "\n").replace('\r', '\n');
      String safeChannel = channel != null && !channel.isBlank() ? channel.trim() : "minecraft:brand";
      String[] lines = safeScript.split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         String raw = lines[i] == null ? "" : lines[i];
         String stripped = raw.stripLeading();
         if (!stripped.startsWith("#")) {
            int split = this.keyValueSeparatorIndex(raw);
            if (split >= 0) {
               String key = raw.substring(0, split).strip();
               if ("channel".equalsIgnoreCase(key)) {
                  String indent = raw.substring(0, raw.length() - stripped.length());
                  lines[i] = indent + "channel = " + safeChannel;
                  return String.join("\n", lines);
               }
            }
         }
      }

      return safeScript.isBlank() ? "channel = " + safeChannel : "channel = " + safeChannel + "\n" + safeScript;
   }

   @Override
   public String getOverlayId() {
      return "autism-action-editor";
   }

   @Override
   public int getMinWidth() {
      return 168;
   }

   @Override
   public int getMinHeight() {
      return this.defaultWindowHeight();
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean v) {
      this.visible = v;
      if (v) {
         this.valid_List.clear();
         this.scrollbarRegions.clear();
      } else {
         this.flushPendingPayloadEdit(true);
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         this.packetSelector.setVisible(false);
         this.raceStepSelector.setVisible(false);
         this.clearInputFocus();
      }
   }

   @Override
   public boolean isCollapsed() {
      return this.windowCollapsed;
   }

   @Override
   public void setCollapsed(boolean value) {
      if (this.windowCollapsed != value) {
         super.setCollapsed(value);
         if (value) {
            autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
            this.packetSelector.setVisible(false);
            this.raceStepSelector.setVisible(false);
            this.valid_List.clear();
            this.scrollbarRegions.clear();
         }
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.editorX, this.editorY, this.editorWidth, this.editorHeight, this.visible, this.windowCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds b) {
      OverlayBounds c = this.clampToScreen(this, b);
      this.editorX = c.x;
      this.editorY = c.y;
      this.editorWidth = c.width;
      this.editorHeight = c.height;
      this.visible = c.visible;
      this.windowCollapsed = c.collapsed;
   }

   @Override
   public boolean containsPoint(double mx, double my) {
      return this.visible
         && (
            this.packetSelector.isVisible()
               || this.raceStepSelector.isVisible()
               || autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)
               || mx >= this.editorX && mx < this.editorX + this.editorWidth && my >= this.editorY && my < this.editorY + (this.windowCollapsed ? 14 : this.editorHeight)
         );
   }

   @Override
   public boolean isOverHeader(double mx, double my) {
      return this.visible
         && mx >= this.editorX
         && mx < this.editorX + this.editorWidth
         && my >= this.editorY
         && my < this.editorY + 14
         && !this.isOverHeaderControl(mx, my, this.getBounds());
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean hasFocusedTextInput() {
      if (this.packetSelector.hasFocusedTextInput()) {
         return true;
      }

      if (this.raceStepSelector.hasFocusedTextInput()) {
         return true;
      }

      for (ThemedTextInputBox f : this.textFields.values()) {
         if (f.isFocused()) {
            return true;
         }
      }

      for (ThemedTextInputBox f : this.internalTextFields.values()) {
         if (f.isFocused()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void clearInputFocus() {
      this.textFields.values().forEach(f -> f.setFocused(false));
      this.internalTextFields.values().forEach(f -> f.setFocused(false));
   }

   public boolean unusedIsCaptureActive() {
      return this.captureSession.isActive();
   }

   public boolean unusedIsCaptureSessionActive() {
      return this.captureSession.isActive();
   }

   public String captureStatusText() {
      return "Capturing " + this.captureTargetLabel() + " - " + this.actionTitleOrDefault();
   }

   public String captureHintText() {
      String activeCaptureKey = this.captureSession.key();
      if ("_item_entries".equals(activeCaptureKey) || "_drop_entries".equals(activeCaptureKey)) {
         return "Right-click a slot to set the item. Esc = cancel";
      } else if ("_wsc_entries".equals(activeCaptureKey)) {
         return "Right-click slots to add items or exact slots. Esc = done";
      } else if (!this.listFields.containsKey(activeCaptureKey)) {
         return "Right-click a slot in this screen. Esc = cancel";
      } else if (this.isXCarryEntriesKey(activeCaptureKey)) {
         return "Right-click slots to add items, or empty slots for exact slots. Esc = done";
      } else {
         return this.isStoreItemTargetsKey(activeCaptureKey)
            ? "Right-click items anywhere to add by name. Ctrl+right-click for exact slots. Esc = done"
            : "Right-click slots to add item names. Esc = done";
      }
   }

   public String init_Str_Slot(Slot slot, String itemName, String registryId) {
      if (slot == null) {
         return "";
      }

      int handlerSlot = autismclient.util.InventorySlotHelper.findSlotIndex(MC, slot);
      int visibleSlot = handlerSlot >= 0 ? autismclient.util.InventorySlotHelper.toVisibleSlot(MC, handlerSlot) : -1;
      String slotText = visibleSlot >= 0 ? String.valueOf(visibleSlot) : "Handler " + handlerSlot;
      String slotDetail = "Handler " + handlerSlot;
      String itemText = !registryId.isEmpty() ? registryId : (!itemName.isEmpty() ? itemName : "Empty slot");
      return slotText.equals(slotDetail) ? "Hover: " + slotText + " | " + itemText : "Hover: " + slotText + " | " + slotDetail + " | " + itemText;
   }

   public boolean poll_Z_2() {
      return this.cancelPacketClickTargetCapture() ? true : this.captureSession.init(() -> this.finishCapture(false, false));
   }

   public boolean push_Z_2() {
      AutismSessionState state = AutismSessionState.getInstance();
      return this.captureSession.isActive()
         || this.capturingFieldKey != null
         || this.hiddenOverlays != null
         || this.visibleBeforeCapture
         || this.screenBeforeCapture != null
         || this.screenBeforeGbreakCapture != null
         || state.hasCaptureCancelCallback()
         || state.hasBlockCaptureCallback()
         || state.hasEntityCaptureCallback()
         || state.hasAttackCaptureCallback()
         || state.isGbreakCapturing();
   }

   public boolean captureSlot(Slot slot, String itemName, String registryId) {
      return this.captureSlot(slot, itemName, registryId, false);
   }

   public boolean captureSlot(Slot slot, String itemName, String registryId, boolean slotMode) {
      this.variableWarningStale = true;
      int visibleSlot = slot != null ? autismclient.util.InventorySlotHelper.toVisibleSlot(MC, slot) : -1;
      MacroItemTarget capturedTarget = this.targetFromSlot(slot, itemName, registryId, visibleSlot);
      if (this.captureSession.isActive() && (!itemName.isEmpty() || visibleSlot >= 0)) {
         boolean useSlot = slotMode && visibleSlot >= 0;
         if (useSlot) {
            capturedTarget = MacroItemTarget.ofSlot(Math.max(0, visibleSlot));
         }

         String key = this.captureSession.key();
         if ("_item_entries".equals(key) && this.itemActionDraft != null) {
            this.load_V_ArrayUtil(capturedTarget);
            this.captureSession.clear();
            this.finishCapture(false, false);
            return true;
         }

         if ("_drop_entries".equals(key) && this.dropActionDraft != null) {
            this.applyCapturedDropTarget(capturedTarget);
            this.captureSession.clear();
            this.finishCapture(false, false);
            return true;
         }

         if ("_wsc_entries".equals(key)) {
            String rawTarget = capturedTarget.toSpecString();
            if (rawTarget == null) {
               this.showToast("Nothing to add from that slot", -38037);
               return true;
            }

            String norm = StoreItemAction.normalizeTarget(rawTarget);
            if (norm != null && !norm.isBlank()) {
               String disp = StoreItemAction.formatTarget(norm);
               if (this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < this.slotChangeEntries.size()) {
                  if (!this.hasSlotChangeTargetExcept(norm, this.selectedSlotChangeEntry)) {
                     WaitForSlotChangeAction.Inner_DataUtil entry = this.slotChangeEntries.get(this.selectedSlotChangeEntry);
                     entry.target = norm;
                     entry.itemTarget = capturedTarget.copy();
                     this.loadSlotChangeEntry(entry);
                     this.showToast("Updated: " + disp, -10035062);
                  } else {
                     this.showToast("Already in list: " + disp, -38037);
                  }
               } else if (this.hasSlotChangeTarget(norm)) {
                  this.showToast("Already added: " + disp, -38037);
               } else {
                  WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil(norm, this.slotChangeCondition, this.slotChangeCount);
                  entry.itemTarget = capturedTarget.copy();
                  entry.target = norm;
                  this.slotChangeEntries.add(entry);
                  this.showToast("Added: " + disp, -10035062);
               }

               return true;
            } else {
               this.showToast("Nothing to add from that slot", -38037);
               return true;
            }
         } else {
            if ("wait_inventory_slot".equals(key)) {
               this.setWaitSlotField(Math.max(0, visibleSlot));
               this.captureSession.clear();
               this.finishCapture(false, false);
               this.showToast("Slot set: " + Math.max(0, visibleSlot), -10035062);
               return true;
            }

            if ("itemName".equals(key) && this.editingAction != null && this.editingAction.getType() == MacroActionType.USE_ITEM) {
               ThemedTextInputBox itemField = this.textFields.get("itemName");
               ThemedTextInputBox slotField = this.textFields.get("slot");
               if (capturedTarget.hasItemFilter()) {
                  MacroItemTarget nameOnly = withoutSlot(capturedTarget);
                  if (itemField != null) {
                     itemField.setValue(nameOnly.rawLabel());
                  }

                  this.itemTargets.put("itemName", nameOnly.copy());
                  this.showToast("Item set: " + nameOnly.resolvedDisplayName(), -10035062);
               } else if (capturedTarget.hasSlot()) {
                  if (itemField != null) {
                     itemField.setValue("");
                  }

                  this.itemTargets.remove("itemName");
                  if (slotField != null) {
                     slotField.setValue(String.valueOf(Math.max(0, capturedTarget.slot)));
                  }

                  this.showToast("Slot set: " + Math.max(0, capturedTarget.slot), -10035062);
               } else {
                  this.showToast("Nothing to capture from that slot", -38037);
               }

               this.captureSession.clear();
               this.finishCapture(false, false);
               return true;
            } else if (this.listFields.containsKey(key)) {
               List<String> list = this.listFields.get(key);
               if (list == null) {
                  return false;
               }

               ActionEditorOverlay.EntryResult result = this.isStoreItemTargetsKey(key)
                  ? this.captureSlotEntry(slot, itemName, registryId, visibleSlot, useSlot, list)
                  : this.init_ActionEditorOverla_FieldDef(
                     this.findField(key),
                     key,
                     list,
                     useSlot
                        ? capturedTarget.toSpecString()
                        : (
                           this.isXCarryEntriesKey(key)
                              ? capturedTarget.toSpecString()
                              : (
                                 this.isAuditTargetsKey(key)
                                    ? withoutSlot(capturedTarget).toSpecString()
                                    : (this.isItemTargetsKey(key) ? capturedTarget.toSpecString() : itemName)
                              )
                        )
                  );
               if (result != null && result.accepted() && (this.isItemTargetsKey(key) || this.isXCarryEntriesKey(key) || this.isAuditTargetsKey(key))) {
                  MacroItemTarget preservedTarget = this.isStoreItemTargetsKey(key)
                     ? stripSlotIfFiltered(capturedTarget)
                     : (this.isAuditTargetsKey(key) ? (useSlot ? capturedTarget : withoutSlot(capturedTarget)) : capturedTarget);
                  this.addCapturedTarget(key, list, preservedTarget);
               }

               if (result != null && result.message() != null && !result.message().isBlank()) {
                  this.showToast(result.message(), result.toastColor());
               }

               return true;
            } else {
               ThemedTextInputBox tf = this.textFields.get(key);
               if (tf == null) {
                  return false;
               }

               FieldDef field = this.findField(key);
               if (field != null && field.type() == FieldType.SLOT) {
                  tf.setValue(String.valueOf(Math.max(0, visibleSlot)));
               } else if (useSlot) {
                  tf.setValue(capturedTarget.toSpecString());
                  if ("itemName".equals(key) || "fromItemName".equals(key) || "toItemName".equals(key) || this.itemTargets.containsKey(key)) {
                     this.itemTargets.put(key, capturedTarget.copy());
                  }
               } else {
                  MacroItemTarget nameOnly = withoutSlot(capturedTarget);
                  tf.setValue(nameOnly.rawLabel());
                  if ("itemName".equals(key) || "fromItemName".equals(key) || "toItemName".equals(key) || this.itemTargets.containsKey(key)) {
                     this.itemTargets.put(key, nameOnly.copy());
                  }
               }

               this.captureSession.clear();
               this.finishCapture(false, false);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         this.flushPendingPayloadEdit(false);
         this.frameDelta = delta;
         this.valid_List.clear();
         this.scrollbarRegions.clear();
         int rawMouseX = mouseX;
         int rawMouseY = mouseY;
         if (!this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            mouseX = -2147483648;
            mouseY = -2147483648;
         }

         this.dropdowns.clear();
         this.bodyClip = null;
         if (this.variableWarningStale) {
            this.variableWarningStale = false;
            this.refreshVariableWarning();
         }

         if (!this.windowCollapsed) {
            int neededH = 18 + this.itemEditorHeight() + this.footerHeight() + 4;
            int minH = this.defaultWindowHeight();
            int maxH = Math.max(minH, cA.load_I() * 4 / 5);
            this.editorHeight = Math.max(minH, Math.min(maxH, neededH));
         }

         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.editorX, this.editorY, this.editorWidth, this.editorHeight, this.visible, this.windowCollapsed));
         this.editorX = clamped.x;
         this.editorY = clamped.y;
         this.editorWidth = clamped.width;
         this.editorHeight = clamped.height;
         String title = this.windowTitle();
         OverlayBounds bounds = this.getBounds();
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.windowCollapsed, this.dragging);
         boolean clipBody = this.beginBodyClip(context, bounds, this.windowCollapsed);
         if (clipBody) {
            try {
               int frameH = this.renderedHeight(bounds, false);
               int bodyTop = this.editorY + 14;
               int bodyBtm = this.editorY + frameH;
               if (bodyBtm > bodyTop + 1) {
                  this.renderBody(context, mouseX, mouseY, delta, bodyTop, bodyBtm);
               }

               if (!this.dropdowns.isEmpty()) {
                  autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(context, this.font, this.dropdowns, rawMouseX, rawMouseY);
               }
            } finally {
               this.endBodyClip(context, true);
            }
         }

         this.renderInactiveDim(context, bounds, this.windowCollapsed, this.dragging);
         if (!this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            context.nextStratum();
            autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(context, this.font, this.dropdowns, rawMouseX, rawMouseY);
         }

         if (this.packetSelector.isVisible()) {
            this.packetSelector.render(context, mouseX, mouseY, delta);
         }

         if (this.raceStepSelector.isVisible()) {
            this.raceStepSelector.render(context, mouseX, mouseY, delta);
         }
      }
   }

   private void renderBody(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int bodyTop, int bodyBtm) {
      if (this.isLegacyMulti()) {
         ListRowRenderer.drawEmptyPlaceholder(
            context, this.font, "Legacy Multi is unsupported. This block and its steps are skipped when the macro runs.",
            this.editorX + 6, bodyTop + 4, this.editorWidth - 12
         );
      } else if (this.itemActionDraft != null) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderItemActionEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.payloadActionDraft != null) {
         int contentBtm = bodyBtm - this.footerHeight();
         int contentAreaH = contentBtm - bodyTop;
         int totalContentH = this.itemEditorHeight();
         int maxScroll = Math.max(0, totalContentH - contentAreaH);
         this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
         boolean needsScroll = maxScroll > 0;
         int sbReserve = needsScroll ? 6 : 0;
         int x = this.editorX + 4;
         int w = this.editorWidth - 8 - sbReserve;
         UiClipStack.getInstance()
            .push(context, autismclient.gui.f.NetHelper.init(this.editorX + 1, bodyTop, Math.max(0, this.editorWidth - 2), Math.max(0, contentBtm - bodyTop)));

         try {
            this.renderPayloadActionEditor(context, x, bodyTop - this.scrollOffset, w, mouseX, mouseY, delta);
         } finally {
            UiClipStack.getInstance().pop(context);
         }

         if (needsScroll) {
            int sbX = this.editorX + this.editorWidth - 5 - 1;
            this.renderScrollbar(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.scrollOffset, mouseX, mouseY);
            int capSbY = bodyTop;
            int capSbH = contentAreaH;
            this.scrollbarRegions.add(new ActionEditorOverlay.ScrollbarRegion(sbX, bodyTop, 5, contentAreaH, my -> {
               int rel = Math.max(0, Math.min(capSbH, my - capSbY));
               this.scrollOffset = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
            }));
         }
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SEND_PACKET) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderSendPacketEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitPacketEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitPacketMatchEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SOUND) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitSoundEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_ENTITY) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitEntityEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.USE_ITEM) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderUseItemEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.LOOK_AT_BLOCK) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderLookAtBlockEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.ROTATE) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderRotateEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.GO_TO) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderGoToEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SWAP_SLOTS) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderSwapSlotsEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CLICK) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderClickEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DISCONNECT) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderDisconnectEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_GUI) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitGuiEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_CHAT) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.init_V_GuiGraphicsExtract(context, x, bodyTop, bodyBtm - this.footerHeight(), w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SELECT_SLOT) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderSelectSlotEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_COOLDOWN) {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderWaitCooldownEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.editingAction == null || this.editingAction.getType() != MacroActionType.OPEN_CONTAINER && this.editingAction.getType() != MacroActionType.INTERACT_ENTITY) {
         if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderWaitSlotChangeEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_INVENTORY_PREDICATE) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderWaitInventoryPredicateEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CAPTURE_VALUE) {
            int contentBtm = bodyBtm - this.footerHeight();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.itemEditorHeight();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.editorX + 4;
            int w = this.editorWidth - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.editorX + 1, bodyTop, Math.max(0, this.editorWidth - 2), contentAreaH);
            this.bodyClip = clip;
            int firstBodyHit = this.valid_List.size();
            UiClipStack.getInstance().push(context, clip);

            try {
               this.renderCaptureValueEditor(context, x, bodyTop - this.scrollOffset, w, mouseX, mouseY, delta);
            } finally {
               UiClipStack.getInstance().pop(context);
            }

            this.clipHitRegions(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.editorX + this.editorWidth - 5 - 1;
               this.renderScrollbar(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.scrollOffset, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.scrollbarRegions.add(new ActionEditorOverlay.ScrollbarRegion(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.scrollOffset = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DELAY_PACKETS) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderDelayPacketsEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.MINE) {
            int contentBtm = bodyBtm - this.footerHeight();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.itemEditorHeight();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.editorX + 4;
            int w = this.editorWidth - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.editorX + 1, bodyTop, Math.max(0, this.editorWidth - 2), contentAreaH);
            this.bodyClip = clip;
            int firstBodyHit = this.valid_List.size();
            UiClipStack.getInstance().push(context, clip);

            try {
               this.renderMineEditor(context, x, bodyTop - this.scrollOffset, w, mouseX, mouseY, delta);
            } finally {
               UiClipStack.getInstance().pop(context);
            }

            this.clipHitRegions(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.editorX + this.editorWidth - 5 - 1;
               this.renderScrollbar(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.scrollOffset, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.scrollbarRegions.add(new ActionEditorOverlay.ScrollbarRegion(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.scrollOffset = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PAY) {
            int contentBtm = bodyBtm - this.footerHeight();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.itemEditorHeight();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.editorX + 4;
            int w = this.editorWidth - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.editorX + 1, bodyTop, Math.max(0, this.editorWidth - 2), contentAreaH);
            this.bodyClip = clip;
            int firstBodyHit = this.valid_List.size();
            UiClipStack.getInstance().push(context, clip);

            try {
               this.renderPayEditor(context, x, bodyTop - this.scrollOffset, w, mouseX, mouseY, delta);
            } finally {
               UiClipStack.getInstance().pop(context);
            }

            this.clipHitRegions(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.editorX + this.editorWidth - 5 - 1;
               this.renderScrollbar(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.scrollOffset, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.scrollbarRegions.add(new ActionEditorOverlay.ScrollbarRegion(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.scrollOffset = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY_AUDIT) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderInventoryAuditEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.STORE_ITEM) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderStoreItemEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.TOGGLE_MODULE) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderToggleModuleEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.craftEntries != null) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderCraftActionEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.dropActionDraft != null) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.renderDropActionEditor(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.lanStepEntries != null) {
            int x = this.editorX + 4;
            int w = this.editorWidth - 8;
            this.write_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.fieldSchema != null && !this.fieldSchema.fields().isEmpty()) {
            int contentBtm = bodyBtm - this.footerHeight();
            int contentAreaH = contentBtm - bodyTop;
            int totalContentH = this.itemEditorHeight();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.editorX + 4;
            int w = this.editorWidth - 8 - sbReserve;
            UiClipStack.getInstance()
               .push(context, autismclient.gui.f.NetHelper.init(this.editorX + 1, bodyTop, Math.max(0, this.editorWidth - 2), Math.max(0, contentBtm - bodyTop)));

            try {
               int y = bodyTop + 4 - this.scrollOffset;
               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
                  y = this.renderFieldByKey(context, "packetOrder", x, y, w, mouseX, mouseY, delta);
                  BlockPos previewPos = null;

                  try {
                     ThemedTextInputBox fx = this.textFields.get("blockPos_0");
                     ThemedTextInputBox fy = this.textFields.get("blockPos_1");
                     ThemedTextInputBox fz = this.textFields.get("blockPos_2");
                     if (fx != null && fy != null && fz != null) {
                        String sx = fx.getValue().strip();
                        String sy = fy.getValue().strip();
                        String sz = fz.getValue().strip();
                        if (!sx.isEmpty() && !sy.isEmpty() && !sz.isEmpty()) {
                           int xPos = Integer.parseInt(sx);
                           int yPos = Integer.parseInt(sy);
                           int zPos = Integer.parseInt(sz);
                           previewPos = new BlockPos(xPos, yPos, zPos);
                        }
                     }
                  } catch (NumberFormatException ignored) {
                     previewPos = null;
                  }

                  if (previewPos != null) {
                     autismclient.util.BlockPreviewRenderer.init(previewPos, -50373);
                  }

                  Identifier placeFont = this.styleTheme.init_Identifier_l(l.read);
                  autismclient.gui.f.b.k.init(context, this.font, "Item", placeFont, UiColorPalette.textBody(), x, y + 2, false);
                  y += 13;
                  this.renderPlaceItemPicker(context, x, y, w, mouseX, mouseY, delta);
                  y += 20;
                  y = this.renderHint(context, x, y, w, "Name:any  Slot:exact  None:held");
                  y += 2;
                  this.renderToggleRow(context, placeFont, "place_waitForItem", "Wait for Item", x, y, w, mouseX, mouseY);
                  y += 18;
                  this.renderToggleRow(context, placeFont, "place_silentSwitch", "Silent Switch", x, y, w, mouseX, mouseY);
                  y += 18;
               }

               for (FieldDef field : this.fieldSchema.fields()) {
                  if (field.type() != FieldType.STRING_LIST
                     && !this.isWaitForGuiAfterKey(field.key())
                     && this.isFieldApplicable(field)
                     && (
                        this.editingAction == null
                           || this.editingAction.getType() != MacroActionType.PLACE
                           || !"itemName".equals(field.key()) && !"packetOrder".equals(field.key())
                     )) {
                     this.init_V_GuiGraphicsExtract_5(context, field, x, y, w, mouseX, mouseY, delta);
                     y += this.fieldHeight(field) + 2;
                  }
               }

               for (FieldDef field : this.fieldSchema.fields()) {
                  if (field.type() == FieldType.STRING_LIST
                     && !this.isWaitForGuiAfterKey(field.key())
                     && this.isFieldApplicable(field)
                     && (
                        this.editingAction == null
                           || this.editingAction.getType() != MacroActionType.PLACE
                           || !"itemName".equals(field.key()) && !"packetOrder".equals(field.key())
                     )) {
                     this.init_V_GuiGraphicsExtract_5(context, field, x, y, w, mouseX, mouseY, delta);
                     y += this.fieldHeight(field) + 2;
                  }
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PACKET) {
                  this.load_V(context, x, y, w, mouseX, mouseY);
                  y += 52;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SEND_PACKET) {
                  this.write_V(context, x, y, w, mouseX, mouseY);
                  y += 52;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DELAY_PACKETS) {
                  this.renderPresetButtons(context, x, y, w, mouseX, mouseY);
               }
            } finally {
               UiClipStack.getInstance().pop(context);
            }

            if (needsScroll) {
               int sbX = this.editorX + this.editorWidth - 5 - 1;
               this.renderScrollbar(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.scrollOffset, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.scrollbarRegions.add(new ActionEditorOverlay.ScrollbarRegion(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.scrollOffset = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         }
      } else {
         int x = this.editorX + 4;
         int w = this.editorWidth - 8;
         this.renderGenericActionFields(context, x, bodyTop, w, mouseX, mouseY, delta);
      }

      this.renderFooter(context, bodyBtm - this.footerHeight(), mouseX, mouseY);
   }

   private void renderItemActionEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      int headerBtnW = this.buttonWidth("Clear");
      autismclient.gui.f.b.k.init(ctx, this.font, "Items / Exact Slots (" + this.itemActionDraft.itemNames.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      boolean canClearAll = !this.itemActionDraft.itemNames.isEmpty();
      int clearX = x + w - headerBtnW;
      this.renderActionButton(ctx, clearX, cy, headerBtnW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClearAll, mx, my, () -> {
         this.itemActionDraft.itemNames.clear();
         this.itemActionDraft.itemTimes.clear();
         this.itemActionDraft.itemActionIdx.clear();
         this.itemActionDraft.itemButtons.clear();
         this.itemActionDraft.itemSearchScopes.clear();
         this.itemActionDraft.stackAmountModes.clear();
         this.clearItemEntrySelection();
         this.rebuildItemTimeFields();
      });
      cy += 14;
      int selAreaH = 56;
      int sbX = x + w - 5;
      ScrollableListView itemViewport = this.viewportFor(this.entryViewports, "_item_entries", x, cy, w, selAreaH, 14, 5);
      itemViewport.setContentHeight(this.itemActionDraft.itemNames.size() * 14);
      itemViewport.renderScrollbar(ctx, mx, my);
      ContainerClickType[] ACTIONS = ContainerClickType.values();
      int sbGap = 7;
      int delW = 13;
      int itemW = w - sbGap - delW - 2;
      if (!this.itemActionDraft.itemNames.isEmpty()) {
         itemViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstVis = itemViewport.getFirstVisibleRow();
         int iy0 = cy - itemViewport.getScrollOffset() % 14;

         for (int i = firstVis; i < this.itemActionDraft.itemNames.size() && i <= itemViewport.getLastVisibleRow(); i++) {
            int iy = itemViewport.getRowY(i);
            if (iy != -2147483648) {
               String entry = this.itemActionDraft.itemNames.get(i);
               int eiActIdx = this.itemActionDraft.getItemActionIndex(i);
               ContainerClickType eiAct = ACTIONS[eiActIdx];
               int eiBtn = this.itemActionDraft.getItemButton(i);
               boolean selected = i == this.selectedItemEntry;
               MacroItemTarget entryTarget = this.itemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, i);
               Component displayName = this.describeItemTarget(entryTarget, entry);
               boolean rowHovered = mx >= x && mx < x + itemW && my >= iy && my < iy + 13;
               String summaryAct = eiAct.load_Str;
               String var10000;
               if (eiAct == ContainerClickType.SWAP) {
                  var10000 = "H" + Math.max(1, Math.min(9, eiBtn + 1));
               } else if (eiAct == ContainerClickType.PICKUP_ALL) {
                  var10000 = "";
               } else {
                  switch (eiBtn) {
                     case 1:
                        var10000 = "R";
                        break;
                     case 2:
                        var10000 = "M";
                        break;
                     default:
                        var10000 = "L";
                  }
               }

               String summaryBtn = var10000;
               int times = this.itemActionDraft.getItemTimes(i);
               String summary = " • " + summaryAct + (summaryBtn.isBlank() ? "" : " " + summaryBtn) + (times != 1 ? " ×" + times : "");
               Component rowLabel = Component.empty().append(displayName).append(Component.literal(summary).withStyle(s -> s.withColor(-7829368)));
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx, this.font, rowLabel, autismclient.gui.f.NetHelper.init(x, iy, itemW, 13), rowHovered, selected, ListRowRenderer.RowTone.NORMAL, true
               );
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int fi = i;
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.ClickRegion(
                           x, Math.max(cy, iy), itemW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.toggleItemEntrySelection(fi)
                        )
                     );
               }

               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int delX = x + itemW + 2;
                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                     this.itemActionDraft.itemNames.remove(fi);
                     if (fi < this.itemActionDraft.itemTargets.size()) {
                        this.itemActionDraft.itemTargets.remove(fi);
                     }

                     if (fi < this.itemActionDraft.itemTimes.size()) {
                        this.itemActionDraft.itemTimes.remove(fi);
                     }

                     if (fi < this.itemActionDraft.itemActionIdx.size()) {
                        this.itemActionDraft.itemActionIdx.remove(fi);
                     }

                     if (fi < this.itemActionDraft.itemButtons.size()) {
                        this.itemActionDraft.itemButtons.remove(fi);
                     }

                     if (fi < this.itemActionDraft.itemSearchScopes.size()) {
                        this.itemActionDraft.itemSearchScopes.remove(fi);
                     }

                     if (fi < this.itemActionDraft.stackAmountModes.size()) {
                        this.itemActionDraft.stackAmountModes.remove(fi);
                     }

                     if (this.selectedItemEntry == fi) {
                        this.clearItemEntrySelection();
                     } else if (this.selectedItemEntry > fi) {
                        this.selectedItemEntry--;
                     }

                     this.rebuildItemTimeFields();
                     ScrollableListView vp = this.entryViewports.get("_item_entries");
                     if (vp != null) {
                        vp.scrollByRows(-1);
                     }
                  });
               }
            }
         }

         itemViewport.endFrame(ctx);
      } else {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No rows yet. Add an item or exact slot.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      int addPickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
      String addLbl = this.selectedItemEntry >= 0 ? "New" : "+Add";
      int addBtnW = this.buttonWidth(addLbl);
      int slotW = 26;
      ThemedTextInputBox addF = this.internalTextFields.get("_item_add");
      ThemedTextInputBox entrySlotF = this.textFields.get("item_entrySlot");
      if (addF != null && entrySlotF != null) {
         int pickX = x + w - addBtnW - 3 - addPickW;
         int plusX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         addF.setX(x);
         addF.setY(cy);
         addF.setWidth(slotX - x - 2);
         addF.render(ctx, mx, my, delta);
         entrySlotF.setX(slotX);
         entrySlotF.setY(cy);
         entrySlotF.setWidth(slotW);
         entrySlotF.render(ctx, mx, my, delta);
         boolean capturing = this.captureSession.init("_item_entries");
         this.renderCaptureButton(ctx, pickX, cy, addPickW, 14, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("_item_entries"));
         this.renderActionButton(ctx, plusX, cy, addBtnW, 13, addLbl, OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
            if (this.selectedItemEntry >= 0) {
               this.clearItemEntrySelection();
            } else {
               this.flush_V_2();
            }
         });
      }

      cy += 16;
      cy += 4;
      if (this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
         int eiActIdx = this.itemActionDraft.getItemActionIndex(this.selectedItemEntry);
         ContainerClickType eiAct = ACTIONS[eiActIdx];
         int eiBtn = this.itemActionDraft.getItemButton(this.selectedItemEntry);
         boolean btnActive = eiAct == ContainerClickType.PICKUP || eiAct == ContainerClickType.QUICK_MOVE || eiAct == ContainerClickType.SWAP;
         int editIdx = this.selectedItemEntry;
         int editTimesW = 28;
         int editGap = 3;
         List<String> actOptions = new ArrayList<>();

         for (ContainerClickType a : ItemAction.selectableClickTypes()) {
            actOptions.add(a.load_Str);
         }

         String var81;
         if (eiAct == ContainerClickType.SWAP) {
            var81 = "H" + Math.max(1, Math.min(9, eiBtn + 1));
         } else if (eiAct == ContainerClickType.PICKUP_ALL) {
            var81 = "All";
         } else {
            switch (eiBtn) {
               case 1:
                  var81 = "R";
                  break;
               case 2:
                  var81 = "M";
                  break;
               default:
                  var81 = "L";
            }
         }

         String btnLbl = var81;
         boolean btnIsDropdown = this.itemActionDraft.getItemButtonCount(editIdx) > 2;
         List<String> btnOptions = btnIsDropdown ? this.itemActionDraft.getItemButtonLabels(editIdx) : List.of();
         int editBtnW = btnIsDropdown ? this.dropdownWidth(btnOptions, w) : this.buttonWidth(btnLbl);
         int editActW = this.dropdownWidth(actOptions, w - editGap - editBtnW - editGap - editTimesW);
         this.renderDropdown(
            "item_action", actOptions, this.itemActionDraft.getItemClickTypeOption(editIdx), x, cy, editActW, 13, idx -> this.itemActionDraft.setItemClickTypeOption(editIdx, idx)
         );
         if (btnIsDropdown) {
            this.renderDropdown(
               "item_button",
               btnOptions,
               this.itemActionDraft.getItemButton(editIdx),
               x + editActW + editGap,
               cy,
               editBtnW,
               13,
               idx -> this.itemActionDraft.setItemButton(editIdx, idx)
            );
         } else {
            this.renderButton(
               ctx,
               x + editActW + editGap,
               cy,
               editBtnW,
               13,
               btnLbl,
               eiAct == ContainerClickType.SWAP ? OverlayActionButton.ButtonTone.PRIMARY : OverlayActionButton.ButtonTone.GHOST,
               btnActive,
               mx,
               my,
               () -> this.itemActionDraft.nextItemButton(editIdx),
               () -> this.itemActionDraft.previousItemButton(editIdx)
            );
         }

         ThemedTextInputBox tf = this.textFields.get("item_times_" + this.selectedItemEntry);
         if (tf != null) {
            tf.setX(x + editActW + editGap + editBtnW + editGap);
            tf.setY(cy);
            tf.setWidth(editTimesW);
            tf.render(ctx, mx, my, delta);
         }

         cy += 16;
         if (this.reset_Z()) {
            this.renderSelectedItemScope(ctx, x, cy, w, mx, my);
            cy += 16;
         }
      }

      this.renderGuiWaitRow(ctx, font, "item_waitForGuiBefore", "item_waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.toggleStates.getOrDefault("item_waitForGuiBefore", false) && !this.toggleStates.getOrDefault("item_waitForGuiAfter", false)) {
         ThemedTextInputBox guiF = this.textFields.get("item_guiName");
         if (guiF != null) {
            guiF.setX(-1000);
         }
      } else {
         ThemedTextInputBox guiF = this.textFields.get("item_guiName");
         if (guiF != null) {
            int lw = this.labelWidth(w, "GUI Name", font);
            this.drawTrimmedText(ctx, "GUI Name", x, cy, lw, font);
            guiF.setX(this.controlX(x, lw));
            guiF.setY(cy + 2);
            guiF.setWidth(this.valueFieldWidth(w, lw));
            guiF.render(ctx, mx, my, delta);
         }

         cy += 16;
      }

      this.renderToggleRow(ctx, font, "item_waitForItem", "Wait for Item", x, cy, w, mx, my);
      cy += 16;
      this.renderToggleRow(ctx, font, "item_useClickDelay", "Delay Clicks", x, cy, w, mx, my);
      cy += 16;
      if (this.toggleStates.getOrDefault("item_useClickDelay", false)) {
         ThemedTextInputBox delayF = this.textFields.get("item_clickDelayMs");
         if (delayF != null) {
            int lw = this.labelWidth(w, "Delay (ms)", font);
            this.drawTrimmedText(ctx, "Delay (ms)", x, cy, lw, font);
            delayF.setX(this.controlX(x, lw));
            delayF.setY(cy + 2);
            delayF.setWidth(this.valueFieldWidth(w, lw));
            delayF.render(ctx, mx, my, delta);
         }

         cy += 16;
      } else {
         ThemedTextInputBox delayF = this.textFields.get("item_clickDelayMs");
         if (delayF != null) {
            delayF.setX(-1000);
         }
      }
   }

   private void renderSelectedItemScope(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      if (this.itemActionDraft != null && this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
         int gap = 4;
         ItemAction.Inner_DataUtil scope = this.itemActionDraft.getItemSearchScope(this.selectedItemEntry);
         ItemAction.Inner_DataUtil[] scopes = ItemAction.Inner_DataUtil.values();
         List<String> scopeOptions = new ArrayList<>();

         for (ItemAction.Inner_DataUtil s : scopes) {
            scopeOptions.add(s.init_Str);
         }

         int invW = this.dropdownWidth(scopeOptions, Math.max(68, (w - gap) / 2));
         this.renderDropdown(
            "item_scope",
            scopeOptions,
            scope.ordinal(),
            x,
            y + 2,
            invW,
            13,
            idx -> this.itemActionDraft.setItemSearchScope(this.selectedItemEntry, scopes[Math.max(0, Math.min(idx, scopes.length - 1))])
         );
         ItemAction.Inner_CachePool mode = this.itemActionDraft.getItemStackAmountMode(this.selectedItemEntry);
         ItemAction.Inner_CachePool[] stackModes = ItemAction.Inner_CachePool.values();
         List<String> stackOptions = new ArrayList<>();

         for (ItemAction.Inner_CachePool m : stackModes) {
            stackOptions.add("Stack: " + m.init_Str);
         }

         int stackW = this.dropdownWidth(stackOptions, Math.max(72, w - invW - gap));
         int stackX = x + invW + gap;
         this.renderDropdown("item_stack", stackOptions, mode.ordinal(), stackX, y + 2, stackW, 13, idx -> {
            while (this.itemActionDraft.stackAmountModes.size() <= this.selectedItemEntry) {
               this.itemActionDraft.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
            }

            this.itemActionDraft.stackAmountModes.set(this.selectedItemEntry, stackModes[Math.max(0, Math.min(idx, stackModes.length - 1))].ordinal());
         });
      }
   }

   private boolean reset_Z() {
      if (this.itemActionDraft != null && this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
         MacroItemTarget target = this.itemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, this.selectedItemEntry);
         return target.hasItemFilter() && !target.hasSlot()
            ? this.itemActionDraft.getItemClickType(this.selectedItemEntry) != ContainerClickType.PICKUP_ALL
               || !this.toggleStates.getOrDefault("item_useCursorItemForPickupAll", this.itemActionDraft.useCursorItemForPickupAll)
            : false;
      } else {
         return false;
      }
   }

   private void addItemTarget(MacroItemTarget target) {
      this.appendItemTarget(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, target);

      while (this.itemActionDraft.itemTimes.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemTimes.add(1);
      }

      while (this.itemActionDraft.itemActionIdx.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemActionIdx.add(0);
      }

      while (this.itemActionDraft.itemButtons.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemButtons.add(0);
      }

      while (this.itemActionDraft.itemSearchScopes.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemSearchScopes.add(0);
      }

      while (this.itemActionDraft.stackAmountModes.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.stackAmountModes.add(0);
      }

      int idx = this.itemActionDraft.itemNames.size() - 1;
      ThemedTextInputBox f = this.newTextField(28);
      f.setIntegerOnlyFilter(true);
      f.setValue("1");
      this.textFields.put("item_times_" + idx, f);
   }

   private void rebuildItemTimeFields() {
      this.textFields.keySet().removeIf(k -> k.startsWith("item_times_"));
      this.resizeItemTargets(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames.size());

      while (this.itemActionDraft.itemTimes.size() > this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemTimes.remove(this.itemActionDraft.itemTimes.size() - 1);
      }

      while (this.itemActionDraft.itemActionIdx.size() > this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemActionIdx.remove(this.itemActionDraft.itemActionIdx.size() - 1);
      }

      while (this.itemActionDraft.itemButtons.size() > this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemButtons.remove(this.itemActionDraft.itemButtons.size() - 1);
      }

      while (this.itemActionDraft.itemSearchScopes.size() > this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemSearchScopes.remove(this.itemActionDraft.itemSearchScopes.size() - 1);
      }

      while (this.itemActionDraft.stackAmountModes.size() > this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.stackAmountModes.remove(this.itemActionDraft.stackAmountModes.size() - 1);
      }

      while (this.itemActionDraft.itemSearchScopes.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.itemSearchScopes.add(0);
      }

      while (this.itemActionDraft.stackAmountModes.size() < this.itemActionDraft.itemNames.size()) {
         this.itemActionDraft.stackAmountModes.add(0);
      }

      for (int i = 0; i < this.itemActionDraft.itemNames.size(); i++) {
         ThemedTextInputBox f = this.newTextField(28);
         f.setIntegerOnlyFilter(true);
         f.setValue(String.valueOf(this.itemActionDraft.getItemTimes(i)));
         this.textFields.put("item_times_" + i, f);
      }
   }

   private void toggleItemEntrySelection(int index) {
      if (this.selectedItemEntry == index) {
         this.clearItemEntrySelection();
      } else {
         this.selectedItemEntry = index;
         this.loadSelectedItemEntry();
      }
   }

   private void clearItemEntrySelection() {
      this.selectedItemEntry = -1;
      ThemedTextInputBox addF = this.internalTextFields.get("_item_add");
      this.suppressItemAddListener = true;
      if (addF != null) {
         addF.setValue("");
      }

      ThemedTextInputBox slotF = this.textFields.get("item_entrySlot");
      if (slotF != null) {
         slotF.setValue("");
      }

      this.suppressItemAddListener = false;
   }

   private void loadSelectedItemEntry() {
      ThemedTextInputBox addF = this.internalTextFields.get("_item_add");
      ThemedTextInputBox slotF = this.textFields.get("item_entrySlot");
      if (addF != null && slotF != null) {
         this.suppressItemAddListener = true;
         if (this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
            MacroItemTarget target = this.itemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, this.selectedItemEntry);
            addF.setValue(target.rawLabel());
            slotF.setValue(target.hasSlot() ? String.valueOf(target.slot) : "");
            this.suppressItemAddListener = false;
         } else {
            addF.setValue("");
            slotF.setValue("");
            this.suppressItemAddListener = false;
         }
      }
   }

   private void flush_V_2() {
      this.commitItemEntryEdit(false);
   }

   private void onItemEntryFieldChanged() {
      if (!this.suppressItemAddListener && this.itemActionDraft != null && this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
         this.commitItemEntryEdit(true);
      }
   }

   private void commitItemEntryEdit(boolean preserveSelection) {
      if (this.itemActionDraft != null) {
         ThemedTextInputBox addF = this.internalTextFields.get("_item_add");
         ThemedTextInputBox slotF = this.textFields.get("item_entrySlot");
         if (addF != null && slotF != null) {
            String nameText = addF.getValue().strip();
            String slotText = slotF.getValue().strip();
            MacroItemTarget target = this.parseItemTarget(
               nameText, slotText, this.itemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, this.selectedItemEntry)
            );
            String entry = target.toSpecString();
            if (entry != null && !entry.isBlank()) {
               if (this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
                  if (!this.containsExcept(this.itemActionDraft.itemNames, entry, this.selectedItemEntry)) {
                     this.setItemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, this.selectedItemEntry, target);
                  }
               } else if (!this.itemActionDraft.itemNames.contains(entry)) {
                  this.addItemTarget(target);
               }

               if (!preserveSelection) {
                  this.clearItemEntrySelection();
               }
            }
         }
      }
   }

   private void load_V_ArrayUtil(MacroItemTarget rawTarget) {
      if (this.itemActionDraft != null) {
         MacroItemTarget target = stripSlotIfFiltered(rawTarget);
         String entry = target == null ? "" : target.toSpecString();
         if (!entry.isBlank()) {
            int targetIndex = this.selectedItemEntry;
            if (targetIndex < 0 || targetIndex >= this.itemActionDraft.itemNames.size()) {
               targetIndex = this.itemActionDraft.itemNames.indexOf(entry);
               if (targetIndex < 0) {
                  this.addItemTarget(target.copy());
                  targetIndex = this.itemActionDraft.itemNames.size() - 1;
               }
            } else if (!this.containsExcept(this.itemActionDraft.itemNames, entry, targetIndex)) {
               this.setItemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, targetIndex, target.copy());
            }

            this.selectedItemEntry = targetIndex;
            ThemedTextInputBox addF = this.internalTextFields.get("_item_add");
            if (addF != null) {
               addF.setValue(target.rawLabel());
            }
         }
      }
   }

   private void renderToggleRow(GuiGraphicsExtractor ctx, Identifier font, String stateKey, String label, int x, int y, int w, int mx, int my) {
      boolean val = this.toggleStates.getOrDefault(stateKey, false);
      int lw = this.labelWidth(w, label, font, 34);
      this.drawTrimmedText(ctx, label, x, y, lw, font);
      int btnW = 34;
      int btnH = 13;
      int btnX = x + w - btnW;
      this.renderToggleButton(
         ctx,
         btnX,
         y + 2,
         btnW,
         btnH,
         val ? "ON" : "OFF",
         val,
         "macro-inline:" + stateKey,
         mx,
         my,
         () -> this.toggleStates.put(stateKey, !this.toggleStates.getOrDefault(stateKey, false))
      );
   }

   private void renderGuiWaitRow(GuiGraphicsExtractor ctx, Identifier font, String beforeKey, String afterKey, int x, int y, int w, int mx, int my) {
      this.drawTrimmedText(ctx, "GUI Wait", x, y, this.labelWidth(w, "GUI Wait", font, 90), font);
      int btnW = 42;
      int gap = 4;
      int afterX = x + w - btnW;
      int beforeX = afterX - gap - btnW;
      this.renderToggleField(ctx, beforeKey, "Before", beforeX, y + 2, btnW, mx, my);
      this.renderToggleField(ctx, afterKey, "After", afterX, y + 2, btnW, mx, my);
   }

   private void renderToggleField(GuiGraphicsExtractor ctx, String key, String label, int x, int y, int w, int mx, int my) {
      boolean val = this.toggleStates.getOrDefault(key, false);
      this.renderToggleButton(
         ctx, x, y, w, 14, label, val, "macro-gui-wait:" + key, mx, my, () -> this.toggleStates.put(key, !this.toggleStates.getOrDefault(key, false))
      );
   }

   private void renderCraftActionEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int removeW = 13;
      int maxTogW = 42;
      int amountW = 44;
      int headerBtnW = this.buttonWidth("Clear");
      int cy = bodyTop + 4;
      autismclient.gui.f.b.k.init(ctx, this.font, "Craft Entries (" + this.craftEntries.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      boolean canClearEntries = !this.craftEntries.isEmpty();
      this.renderActionButton(
         ctx, x + w - headerBtnW, cy, headerBtnW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClearEntries, mx, my, () -> {
            this.craftEntries.clear();
            this.rebuildCraftAmountFields();
            ScrollableListView craftVp = this.entryViewports.get("_craft_entries");
            if (craftVp != null) {
               craftVp.setScrollOffset(0);
            }
         }
      );
      cy += 14;
      int selAreaH = 56;
      ScrollableListView craftViewport = this.viewportFor(this.entryViewports, "_craft_entries", x, cy, w, selAreaH, 14, 5);
      craftViewport.setContentHeight(this.craftEntries.size() * 14);
      craftViewport.renderScrollbar(ctx, mx, my);
      int sbGap = 7;
      if (!this.craftEntries.isEmpty()) {
         craftViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstSel = craftViewport.getFirstVisibleRow();

         for (int i = firstSel; i < this.craftEntries.size() && i <= craftViewport.getLastVisibleRow(); i++) {
            int siy = craftViewport.getRowY(i);
            if (siy != -2147483648) {
               autismclient.util.macro.CraftAction.CraftEntry entry = this.craftEntries.get(i);
               boolean useMax = this.toggleStates.getOrDefault("craft_useMax_" + i, false);
               int removeX = x + w - sbGap - removeW;
               int fi = i;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, siy + 1, removeW, mx, my, () -> {
                  this.craftEntries.remove(fi);
                  this.rebuildCraftAmountFields();
                  ScrollableListView vp = this.entryViewports.get("_craft_entries");
                  if (vp != null) {
                     vp.scrollByRows(-1);
                  }
               });
               int maxX = removeX - 3 - maxTogW;
               String mLbl = useMax ? "Max: On" : "Max: Off";
               int fii = i;
               this.renderToggleButton(
                  ctx,
                  maxX,
                  siy + 1,
                  maxTogW,
                  13,
                  mLbl,
                  useMax,
                  "macro-craft-use-max:" + fii,
                  mx,
                  my,
                  () -> this.toggleStates.put("craft_useMax_" + fii, !this.toggleStates.getOrDefault("craft_useMax_" + fii, false))
               );
               if (!useMax) {
                  int amtX = maxX - 3 - amountW;
                  ThemedTextInputBox af = this.textFields.get("craft_amount_" + i);
                  if (af != null) {
                     af.setX(amtX);
                     af.setY(siy + 1);
                     af.setWidth(amountW);
                     af.render(ctx, mx, my, delta);
                  }
               }

               int nameW = useMax ? maxX - 3 - x : maxX - 3 - amountW - 3 - x;
               boolean rowHovered = mx >= x && mx < x + nameW && my >= siy && my < siy + 13;
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  entry.displayName(),
                  autismclient.gui.f.NetHelper.init(x, siy, Math.max(1, nameW), 13),
                  rowHovered,
                  false,
                  useMax ? ListRowRenderer.RowTone.READY : ListRowRenderer.RowTone.NORMAL,
                  true
               );
            }
         }

         craftViewport.endFrame(ctx);
      } else {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No craft entries yet.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      int refreshW = this.buttonWidth("Reload");
      ThemedTextInputBox srchF = this.internalTextFields.get("_craft_search");
      if (srchF != null) {
         srchF.setX(x);
         srchF.setY(cy);
         srchF.setWidth(w - refreshW - 3);
         srchF.render(ctx, mx, my, delta);
      }

      int rfX = x + w - refreshW;
      this.renderActionButton(ctx, rfX, cy, refreshW, 13, "Reload", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, this::rescanCraftOptions);
      cy += 16;
      String query = srchF != null ? srchF.getValue() : "";
      if (!query.equals(this.lastCraftQuery)) {
         this.filteredCraftOptions = AutoCraftingEngine.filterOptions(this.craftOptions != null ? this.craftOptions : List.of(), query);
         this.lastCraftQuery = query;
         this.recipeListScroll = 0;
         if (this.selectedCraftOption != null && (this.filteredCraftOptions == null || !this.filteredCraftOptions.contains(this.selectedCraftOption))) {
            this.selectedCraftOption = null;
         }
      }

      List<AutoCraftingEngine.CraftOption> filtered = this.filteredCraftOptions != null ? this.filteredCraftOptions : List.of();
      int recipeListH = 52;
      ScrollableListView recipeViewport = this.viewportFor(this.listViewports, "_craft_recipe_browser", x, cy, w, recipeListH, 13, 5);
      recipeViewport.setContentHeight(filtered.size() * 13);
      this.recipeListBounds = new int[]{cy, recipeListH};
      recipeViewport.renderScrollbar(ctx, mx, my);
      int recW = w - 5 - 1;
      if (filtered.isEmpty()) {
         ListRowRenderer.drawEmptyPlaceholder(
            ctx,
            this.font,
            this.craftOptions != null && !this.craftOptions.isEmpty() ? "No recipes match the search." : "No crafting recipes loaded. Use Reload.",
            x,
            cy,
            recW
         );
      } else {
         recipeViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstRec = recipeViewport.getFirstVisibleRow();

         for (int i = firstRec; i < filtered.size() && i <= recipeViewport.getLastVisibleRow(); i++) {
            int ry = recipeViewport.getRowY(i);
            if (ry != -2147483648) {
               AutoCraftingEngine.CraftOption opt = filtered.get(i);
               boolean inList = this.craftEntries
                  .stream()
                  .anyMatch(e -> opt.recipeKey != null && opt.recipeKey.equals(e.recipeKey) || opt.recipeKeyHash >= 0 && opt.recipeKeyHash == e.recipeId);
               boolean hov = !inList && mx >= x && mx < x + recW && my >= ry && my < ry + 13;
               ListRowRenderer.RowTone tone = inList
                  ? ListRowRenderer.RowTone.READY
                  : (opt.craftable ? ListRowRenderer.RowTone.NORMAL : ListRowRenderer.RowTone.MISSING);
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx, this.font, opt.outputHoverName, autismclient.gui.f.NetHelper.init(x, ry, recW, 13), hov, inList, tone, true
               );
               if (opt.outputStack != null && opt.outputStack.getCount() > 1) {
                  String cnt = "×" + opt.outputStack.getCount();
                  autismclient.gui.f.b.k.init(ctx, this.font, cnt, font, UiColorPalette.textSecondary(), x + recW - this.textWidth(font, cnt) - 2, ry + 2, false);
               }

               AutoCraftingEngine.CraftOption fOpt = opt;
               boolean fInList = inList;
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, ry, recW, 13, () -> {
                  if (fInList) {
                     this.craftEntries.removeIf(e -> fOpt.recipeKey != null && fOpt.recipeKey.equals(e.recipeKey) || fOpt.recipeKeyHash >= 0 && fOpt.recipeKeyHash == e.recipeId);
                     this.rebuildCraftAmountFields();
                  } else {
                     autismclient.util.macro.CraftAction.CraftEntry newEntry = autismclient.util.macro.CraftAction.CraftEntry.init(fOpt, 1, false);
                     this.craftEntries.add(newEntry);
                     int idx = this.craftEntries.size() - 1;
                     ThemedTextInputBox f = this.newTextField(44);
                     f.setIntegerOnlyFilter(true);
                     f.setValue("1");
                     this.textFields.put("craft_amount_" + idx, f);
                     this.toggleStates.put("craft_useMax_" + idx, false);
                  }
               }));
            }
         }

         recipeViewport.endFrame(ctx);
      }
   }

   private void rescanCraftOptions() {
      this.craftOptions = AutoCraftingEngine.scanOptionsForClient(MC);
      this.lastCraftQuery = null;
      this.selectedCraftOption = null;
      this.recipeListScroll = 0;
   }

   private void addSelectedCraftOption() {
      if (this.selectedCraftOption != null) {
         ThemedTextInputBox amtF = this.textFields.get("_craft_amount");
         int amount = 1;
         if (amtF != null && !this.tick_Z) {
            try {
               amount = Math.max(1, Integer.parseInt(amtF.getValue().strip()));
            } catch (NumberFormatException var7) {
            }
         }

         autismclient.util.macro.CraftAction.CraftEntry newEntry = autismclient.util.macro.CraftAction.CraftEntry.init(
            this.selectedCraftOption, amount, this.tick_Z
         );

         for (int i = 0; i < this.craftEntries.size(); i++) {
            autismclient.util.macro.CraftAction.CraftEntry existing = this.craftEntries.get(i);
            if (newEntry.recipeKey != null && newEntry.recipeKey.equals(existing.recipeKey) || newEntry.recipeId >= 0 && newEntry.recipeId == existing.recipeId) {
               existing.amount = newEntry.amount;
               existing.useMaxAmount = newEntry.useMaxAmount;
               this.toggleStates.put("craft_useMax_" + i, newEntry.useMaxAmount);
               ThemedTextInputBox f = this.textFields.get("craft_amount_" + i);
               if (f != null) {
                  f.setValue(String.valueOf(newEntry.amount));
               }

               return;
            }
         }

         this.craftEntries.add(newEntry);
         int idx = this.craftEntries.size() - 1;
         ThemedTextInputBox f = this.newTextField(44);
         f.setIntegerOnlyFilter(true);
         f.setValue(String.valueOf(newEntry.amount));
         this.textFields.put("craft_amount_" + idx, f);
         this.toggleStates.put("craft_useMax_" + idx, newEntry.useMaxAmount);
      }
   }

   private void rebuildCraftAmountFields() {
      this.textFields.keySet().removeIf(k -> k.startsWith("craft_amount_"));

      for (int i = 0; i < this.craftEntries.size(); i++) {
         autismclient.util.macro.CraftAction.CraftEntry entry = this.craftEntries.get(i);
         ThemedTextInputBox f = this.newTextField(44);
         f.setIntegerOnlyFilter(true);
         f.setValue(String.valueOf(entry.amount));
         this.textFields.put("craft_amount_" + i, f);
         if (!this.toggleStates.containsKey("craft_useMax_" + i)) {
            this.toggleStates.put("craft_useMax_" + i, entry.useMaxAmount);
         }
      }

      this.toggleStates.keySet().removeIf(k -> {
         if (!k.startsWith("craft_useMax_")) {
            return false;
         }

         try {
            return Integer.parseInt(k.substring("craft_useMax_".length())) >= this.craftEntries.size();
         } catch (NumberFormatException e) {
            return true;
         }
      });
   }

   private void renderDropActionEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      int headerBtnW = this.buttonWidth("Clear");
      autismclient.gui.f.b.k.init(ctx, this.font, "Drop Entries (" + this.dropActionDraft.itemNames.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      boolean canClearAll = !this.dropActionDraft.itemNames.isEmpty();
      int clearX = x + w - headerBtnW;
      this.renderActionButton(ctx, clearX, cy, headerBtnW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClearAll, mx, my, () -> {
         this.dropActionDraft.itemNames.clear();
         this.dropActionDraft.itemTargets.clear();
         this.dropActionDraft.itemCounts.clear();
         this.clearDropEntrySelection();
         this.rebuildDropCountFields();
      });
      cy += 14;
      int selAreaH = 56;
      ScrollableListView dropViewport = this.viewportFor(this.entryViewports, "_drop_entries", x, cy, w, selAreaH, 14, 5);
      dropViewport.setContentHeight(this.dropActionDraft.itemNames.size() * 14);
      dropViewport.renderScrollbar(ctx, mx, my);
      int sbGap = 7;
      int delW = 13;
      int dropItemW = w - sbGap - delW - 2;
      if (!this.dropActionDraft.itemNames.isEmpty()) {
         dropViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstVis = dropViewport.getFirstVisibleRow();

         for (int i = firstVis; i < this.dropActionDraft.itemNames.size() && i <= dropViewport.getLastVisibleRow(); i++) {
            int iy = dropViewport.getRowY(i);
            if (iy != -2147483648) {
               String entry = this.dropActionDraft.itemNames.get(i);
               boolean selected = i == this.selectedDropEntry;
               MacroItemTarget entryTarget = this.itemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, i);
               Component displayName = this.describeItemTarget(entryTarget, entry);
               int cnt = i < this.dropActionDraft.itemCounts.size() ? this.dropActionDraft.itemCounts.get(i) : 0;
               String summary = " • " + (cnt == 0 ? "all" : "×" + cnt);
               Component rowLabel = Component.empty().append(displayName).append(Component.literal(summary).withStyle(s -> s.withColor(-7829368)));
               boolean rowHovered = mx >= x && mx < x + dropItemW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  rowLabel,
                  autismclient.gui.f.NetHelper.init(x, iy, dropItemW, 13),
                  rowHovered,
                  selected,
                  ListRowRenderer.RowTone.NORMAL,
                  true
               );
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int fi = i;
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.ClickRegion(
                           x, Math.max(cy, iy), dropItemW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.toggleDropEntrySelection(fi)
                        )
                     );
               }

               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int delX = x + dropItemW + 2;
                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                     this.dropActionDraft.itemNames.remove(fi);
                     if (fi < this.dropActionDraft.itemTargets.size()) {
                        this.dropActionDraft.itemTargets.remove(fi);
                     }

                     if (fi < this.dropActionDraft.itemCounts.size()) {
                        this.dropActionDraft.itemCounts.remove(fi);
                     }

                     if (this.selectedDropEntry == fi) {
                        this.clearDropEntrySelection();
                     } else if (this.selectedDropEntry > fi) {
                        this.selectedDropEntry--;
                     }

                     this.rebuildDropCountFields();
                     ScrollableListView vp = this.entryViewports.get("_drop_entries");
                     if (vp != null) {
                        vp.scrollByRows(-1);
                     }
                  });
               }
            }
         }

         dropViewport.endFrame(ctx);
      } else {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "Optional: add items/slots, or use the fallback below.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      int addPickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
      String addLbl = this.selectedDropEntry >= 0 ? "New" : "+Add";
      int addBtnW = this.buttonWidth(addLbl);
      int slotW = 44;
      ThemedTextInputBox addF = this.internalTextFields.get("_drop_add");
      ThemedTextInputBox entrySlotF = this.textFields.get("drop_entrySlot");
      if (addF != null && entrySlotF != null) {
         int pickX = x + w - addBtnW - 3 - addPickW;
         int plusX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         addF.setX(x);
         addF.setY(cy);
         addF.setWidth(slotX - x - 2);
         addF.render(ctx, mx, my, delta);
         entrySlotF.setX(slotX);
         entrySlotF.setY(cy);
         entrySlotF.setWidth(slotW);
         entrySlotF.render(ctx, mx, my, delta);
         boolean capturing = this.captureSession.init("_drop_entries");
         this.renderCaptureButton(ctx, pickX, cy, addPickW, 14, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("_drop_entries"));
         this.renderActionButton(ctx, plusX, cy, addBtnW, 13, addLbl, OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
            if (this.selectedDropEntry >= 0) {
               this.clearDropEntrySelection();
            } else {
               this.reset_V();
            }
         });
      }

      cy += 16;
      cy += 4;
      boolean editingSelectedDrop = this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size();
      boolean noDropTargets = this.dropActionDraft.itemNames.isEmpty();
      String dropHint = editingSelectedDrop
         ? "Editing this row. 0 means drop the full stack from that slot or item."
         : (
            noDropTargets
               ? (
                  this.dropActionDraft.mode == DropAction.Inner_DataUtil.ALL
                     ? "No items selected: Drop All drops the entire main inventory and hotbar."
                     : "No items selected: Times drops the currently held item by the count below."
               )
               : "No row selected. These controls set defaults for newly added rows."
         );
      cy = this.renderHint(ctx, x, cy, w, dropHint);
      boolean dropAllSelected = editingSelectedDrop ? this.dropCountAt(this.selectedDropEntry) == 0 : this.dropActionDraft.mode == DropAction.Inner_DataUtil.ALL;
      String modeLabel = editingSelectedDrop ? "Selected Row" : (noDropTargets ? "No Items" : "Default Mode");
      int lw = this.labelWidth(w, modeLabel, font);
      this.drawTrimmedText(ctx, modeLabel, x, cy, lw, font);
      int ctrlX = this.controlX(x, lw);
      int ctrlW = this.valueFieldWidth(w, lw);
      int[] modeBtns = this.layoutButtons(ctrlX, ctrlW, false, "Drop All", "Times");
      this.renderActionButton(
         ctx,
         modeBtns[0],
         cy + 2,
         modeBtns[1],
         13,
         "Drop All",
         dropAllSelected ? OverlayActionButton.ButtonTone.PRIMARY : OverlayActionButton.ButtonTone.GHOST,
         true,
         mx,
         my,
         () -> {
            if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
               this.setDropCountAt(this.selectedDropEntry, 0);
            } else {
               this.modeIndices.put("drop_mode", 0);
               this.dropActionDraft.mode = DropAction.Inner_DataUtil.ALL;
            }

            this.syncDropCountField();
         }
      );
      this.renderActionButton(
         ctx,
         modeBtns[2],
         cy + 2,
         modeBtns[3],
         13,
         "Times",
         !dropAllSelected ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.GHOST,
         true,
         mx,
         my,
         () -> {
            if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
               this.setDropCountAt(this.selectedDropEntry, Math.max(1, this.dropCountAt(this.selectedDropEntry)));
            } else {
               this.modeIndices.put("drop_mode", 1);
               this.dropActionDraft.mode = DropAction.Inner_DataUtil.TIMES;
            }

            this.syncDropCountField();
         }
      );
      cy += 16;
      ThemedTextInputBox cntF = this.textFields.get("drop_globalCount");
      this.syncDropCountField();
      if (cntF != null) {
         modeLabel = editingSelectedDrop ? "Selected Count" : (noDropTargets ? "Held Count" : "Default Count");
         lw = this.labelWidth(w, modeLabel, font);
         this.drawTrimmedText(ctx, modeLabel, x, cy, lw, font);
         cntF.setX(this.controlX(x, lw));
         cntF.setY(cy + 2);
         cntF.setWidth(this.valueFieldWidth(w, lw));
         cntF.render(ctx, mx, my, delta);
         cy += 16;
      }

      this.renderGuiWaitRow(ctx, font, "drop_waitForGuiBefore", "drop_waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.toggleStates.getOrDefault("drop_waitForGuiBefore", false) && !this.toggleStates.getOrDefault("drop_waitForGuiAfter", false)) {
         ThemedTextInputBox guiF = this.textFields.get("drop_guiName");
         if (guiF != null) {
            guiF.setX(-1000);
         }
      } else {
         ThemedTextInputBox guiF = this.textFields.get("drop_guiName");
         if (guiF != null) {
            lw = this.labelWidth(w, "GUI Name", font);
            this.drawTrimmedText(ctx, "GUI Name", x, cy, lw, font);
            guiF.setX(this.controlX(x, lw));
            guiF.setY(cy + 2);
            guiF.setWidth(this.valueFieldWidth(w, lw));
            guiF.render(ctx, mx, my, delta);
         }

         cy += 16;
      }
   }

   private void addDropTarget(MacroItemTarget target, int count) {
      if (target != null) {
         String entry = target.toSpecString();
         if (!entry.isEmpty()) {
            this.appendItemTarget(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, target);
            int safeCount = Math.max(0, count);
            this.dropActionDraft.itemCounts.add(safeCount);
            int idx = this.dropActionDraft.itemNames.size() - 1;
            ThemedTextInputBox f = this.newTextField(32);
            f.setIntegerOnlyFilter(true);
            f.setValue(String.valueOf(safeCount));
            int fieldIndex = idx;
            f.setChangedListener(text -> {
               try {
                  this.setDropCountAt(fieldIndex, Math.max(0, Integer.parseInt(text.strip())));
                  if (fieldIndex == this.selectedDropEntry) {
                     this.syncDropCountField();
                  }
               } catch (NumberFormatException var4x) {
               }
            });
            this.textFields.put("drop_count_" + idx, f);
         }
      }
   }

   private void rebuildDropCountFields() {
      this.textFields.keySet().removeIf(k -> k.startsWith("drop_count_"));
      this.resizeItemTargets(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames.size());

      while (this.dropActionDraft.itemCounts.size() > this.dropActionDraft.itemNames.size()) {
         this.dropActionDraft.itemCounts.remove(this.dropActionDraft.itemCounts.size() - 1);
      }

      for (int i = 0; i < this.dropActionDraft.itemNames.size(); i++) {
         ThemedTextInputBox f = this.newTextField(32);
         f.setIntegerOnlyFilter(true);
         f.setValue(String.valueOf(this.dropActionDraft.itemCounts.get(i)));
         int fieldIndex = i;
         f.setChangedListener(text -> {
            try {
               this.setDropCountAt(fieldIndex, Math.max(0, Integer.parseInt(text.strip())));
               if (fieldIndex == this.selectedDropEntry) {
                  this.syncDropCountField();
               }
            } catch (NumberFormatException var4) {
            }
         });
         this.textFields.put("drop_count_" + i, f);
      }
   }

   private void toggleDropEntrySelection(int index) {
      if (this.selectedDropEntry == index) {
         this.clearDropEntrySelection();
      } else {
         this.selectedDropEntry = index;
         this.loadSelectedDropEntry();
         this.syncDropCountField();
      }
   }

   private void clearDropEntrySelection() {
      this.selectedDropEntry = -1;
      ThemedTextInputBox addF = this.internalTextFields.get("_drop_add");
      this.suppressDropAddListener = true;
      if (addF != null) {
         addF.setValue("");
      }

      ThemedTextInputBox slotF = this.textFields.get("drop_entrySlot");
      if (slotF != null) {
         slotF.setValue("");
      }

      this.suppressDropAddListener = false;
      this.syncDropCountField();
   }

   private void loadSelectedDropEntry() {
      ThemedTextInputBox addF = this.internalTextFields.get("_drop_add");
      ThemedTextInputBox slotF = this.textFields.get("drop_entrySlot");
      if (addF != null && slotF != null) {
         this.suppressDropAddListener = true;
         if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
            MacroItemTarget target = this.itemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, this.selectedDropEntry);
            addF.setValue(target.rawLabel());
            slotF.setValue(target.hasSlot() ? String.valueOf(target.slot) : "");
            this.suppressDropAddListener = false;
            this.syncDropCountField();
         } else {
            addF.setValue("");
            slotF.setValue("");
            this.suppressDropAddListener = false;
         }
      }
   }

   private void reset_V() {
      this.commitDropEntryEdit(false);
   }

   private void onDropEntryFieldChanged() {
      if (!this.suppressDropAddListener && this.dropActionDraft != null && this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
         this.commitDropEntryEdit(true);
      }
   }

   private void commitDropEntryEdit(boolean preserveSelection) {
      if (this.dropActionDraft != null) {
         ThemedTextInputBox addF = this.internalTextFields.get("_drop_add");
         ThemedTextInputBox slotF = this.textFields.get("drop_entrySlot");
         if (addF != null && slotF != null) {
            String nameText = addF.getValue().strip();
            String slotText = slotF.getValue().strip();
            MacroItemTarget target = this.parseItemTarget(
               nameText, slotText, this.itemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, this.selectedDropEntry)
            );
            String entry = target.toSpecString();
            if (!entry.isBlank()) {
               if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
                  if (!this.containsExcept(this.dropActionDraft.itemNames, entry, this.selectedDropEntry)) {
                     this.setItemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, this.selectedDropEntry, target);
                  }
               } else if (!this.dropActionDraft.itemNames.contains(entry)) {
                  this.addDropTarget(target, this.currentDropCount());
               }

               if (!preserveSelection) {
                  this.clearDropEntrySelection();
               }
            }
         }
      }
   }

   private void applyCapturedDropTarget(MacroItemTarget rawTarget) {
      if (this.dropActionDraft != null) {
         MacroItemTarget target = stripSlotIfFiltered(rawTarget);
         String entry = target == null ? "" : target.toSpecString();
         if (!entry.isBlank()) {
            int targetIndex = this.selectedDropEntry;
            if (targetIndex < 0 || targetIndex >= this.dropActionDraft.itemNames.size()) {
               targetIndex = this.dropActionDraft.itemNames.indexOf(entry);
               if (targetIndex < 0) {
                  this.addDropTarget(target.copy(), this.currentDropCount());
                  targetIndex = this.dropActionDraft.itemNames.size() - 1;
               }
            } else if (!this.containsExcept(this.dropActionDraft.itemNames, entry, targetIndex)) {
               this.setItemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, targetIndex, target.copy());
            }

            this.selectedDropEntry = targetIndex;
            this.loadSelectedDropEntry();
            this.syncDropCountField();
         }
      }
   }

   private int currentDropCount() {
      ThemedTextInputBox globalCountField = this.textFields.get("drop_globalCount");
      if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemCounts.size()) {
         return Math.max(0, this.dropActionDraft.itemCounts.get(this.selectedDropEntry));
      }

      if (this.modeIndices.getOrDefault("drop_mode", 0) == 0) {
         return 0;
      }

      if (globalCountField != null) {
         try {
            return Math.max(0, Integer.parseInt(globalCountField.getValue().strip()));
         } catch (NumberFormatException var3) {
         }
      }

      return Math.max(0, this.dropActionDraft.dropCount);
   }

   private int dropCountAt(int index) {
      if (this.dropActionDraft != null && index >= 0 && index < this.dropActionDraft.itemNames.size()) {
         while (this.dropActionDraft.itemCounts.size() <= index) {
            this.dropActionDraft.itemCounts.add(1);
         }

         return Math.max(0, this.dropActionDraft.itemCounts.get(index));
      } else {
         return 1;
      }
   }

   private void setDropCountAt(int index, int count) {
      if (this.dropActionDraft != null && index >= 0 && index < this.dropActionDraft.itemNames.size()) {
         while (this.dropActionDraft.itemCounts.size() <= index) {
            this.dropActionDraft.itemCounts.add(1);
         }

         int safeCount = Math.max(0, count);
         this.dropActionDraft.itemCounts.set(index, safeCount);
         ThemedTextInputBox rowField = this.textFields.get("drop_count_" + index);
         if (rowField != null) {
            rowField.setValue(String.valueOf(safeCount));
         }
      }
   }

   private void syncDropCountField() {
      if (this.dropActionDraft != null) {
         ThemedTextInputBox countField = this.textFields.get("drop_globalCount");
         if (countField != null) {
            boolean editingSelectedDrop = this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size();
            boolean dropAll = editingSelectedDrop ? this.dropCountAt(this.selectedDropEntry) == 0 : this.dropActionDraft.mode == DropAction.Inner_DataUtil.ALL;
            int displayCount = editingSelectedDrop ? Math.max(1, this.dropCountAt(this.selectedDropEntry)) : Math.max(1, this.dropActionDraft.dropCount);
            this.suppressDropCountListener = true;
            countField.setValue(String.valueOf(displayCount));
            countField.setEditable(!dropAll);
            this.suppressDropCountListener = false;
         }
      }
   }

   private void onDropCountFieldChanged() {
      if (!this.suppressDropCountListener && this.dropActionDraft != null) {
         ThemedTextInputBox countField = this.textFields.get("drop_globalCount");
         if (countField != null) {
            boolean editingSelectedDrop = this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size();
            boolean dropAll = editingSelectedDrop ? this.dropCountAt(this.selectedDropEntry) == 0 : this.dropActionDraft.mode == DropAction.Inner_DataUtil.ALL;
            if (!dropAll) {
               try {
                  int value = Math.max(1, Integer.parseInt(countField.getValue().strip()));
                  if (editingSelectedDrop) {
                     this.setDropCountAt(this.selectedDropEntry, value);
                  } else {
                     this.dropActionDraft.dropCount = value;
                  }
               } catch (NumberFormatException var5) {
               }
            }
         }
      }
   }

   private void write_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      boolean filterByUser = this.toggleStates.getOrDefault("lan_filterByUser", false);
      this.renderToggleRow(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 16;
      this.renderToggleRow(ctx, font, "lan_filterByUser", "Specific Peers", x, cy, w, mx, my);
      cy += 16;
      ThemedTextInputBox dsF = this.textFields.get("lan_defaultStep");
      if (dsF != null && (!filterByUser || this.lanStepEntries.isEmpty())) {
         int lw = this.labelWidth(w, filterByUser ? "Fallback Step" : "Any Peer Step", font);
         this.drawTrimmedText(ctx, filterByUser ? "Fallback Step" : "Any Peer Step", x, cy, lw, font);
         dsF.setX(this.controlX(x, lw));
         dsF.setY(cy + 2);
         dsF.setWidth(this.valueFieldWidth(w, lw));
         dsF.render(ctx, mx, my, delta);
         cy += 16;
      }

      String summary = !filterByUser
         ? "Continue when any LAN peer reaches the target step."
         : (
            this.lanStepEntries.isEmpty()
               ? "Add peers below to narrow it down. Until then, it waits for any peer at the fallback step."
               : "Each peer below must reach its step before this continues."
         );
      cy = this.renderHint(ctx, x, cy, w, summary);
      if (filterByUser) {
         if (!this.lanStepEntries.isEmpty()) {
            autismclient.gui.f.b.k.init(ctx, this.font, "Peers (" + this.lanStepEntries.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
            cy += 13;
         }

         boolean hasEntries = !this.lanStepEntries.isEmpty();
         int visibleRows = hasEntries ? Math.min(3, Math.max(1, this.lanStepEntries.size())) : 1;
         int selAreaH = hasEntries ? visibleRows * 14 : 12;
         ScrollableListView lanViewport = null;
         if (hasEntries) {
            lanViewport = this.viewportFor(this.entryViewports, "_lan_entries", x, cy, w, selAreaH, 14, 5);
            lanViewport.setContentHeight(this.lanStepEntries.size() * 14);
         }

         if (hasEntries && lanViewport != null) {
            lanViewport.renderScrollbar(ctx, mx, my);
         }

         int sbGap = 7;
         int removeW = 13;
         int stepW = 40;
         int gapPx = 2;
         int removeX = x + w - sbGap - removeW;
         int stepX = removeX - gapPx - stepW;
         int userW = stepX - x - gapPx;
         if (hasEntries && lanViewport != null) {
            lanViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
            int firstVis = lanViewport.getFirstVisibleRow();

            for (int i = firstVis; i < this.lanStepEntries.size() && i <= lanViewport.getLastVisibleRow(); i++) {
               int iy = lanViewport.getRowY(i);
               if (iy != -2147483648) {
                  PanelSurfaceHelper.drawInputPill(ctx, x, iy, stepX - gapPx - x, 13);
                  ThemedTextInputBox uf = this.textFields.get("lan_user_" + i);
                  if (uf != null) {
                     uf.setX(x);
                     uf.setY(iy + 1);
                     uf.setWidth(userW);
                     uf.render(ctx, mx, my, delta);
                  }

                  ThemedTextInputBox sf = this.textFields.get("lan_step_" + i);
                  if (sf != null) {
                     sf.setX(stepX);
                     sf.setY(iy + 1);
                     sf.setWidth(stepW);
                     sf.render(ctx, mx, my, delta);
                  }

                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy + 1, removeW, mx, my, () -> {
                     this.lanStepEntries.remove(fi);
                     this.rebuildLanStepFields();
                     ScrollableListView vp = this.entryViewports.get("_lan_entries");
                     if (vp != null) {
                        vp.scrollByRows(-1);
                     }
                  });
               }
            }

            lanViewport.endFrame(ctx);
         } else {
            ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No peer filters yet.", x, cy, w - 5 - 1);
         }

         cy += selAreaH;
         PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
         cy += 6;
         int addBtnW = this.buttonWidth("+Add");
         ThemedTextInputBox newUF = this.internalTextFields.get("_lan_user_add");
         ThemedTextInputBox newSF = this.internalTextFields.get("_lan_step_add");
         if (newUF != null && newSF != null) {
            int plusX = x + w - addBtnW;
            int stepAX = plusX - 3 - stepW;
            int userAW = stepAX - x - 2;
            newUF.setX(x);
            newUF.setY(cy);
            newUF.setWidth(userAW);
            newUF.render(ctx, mx, my, delta);
            newSF.setX(stepAX);
            newSF.setY(cy);
            newSF.setWidth(stepW);
            newSF.render(ctx, mx, my, delta);
            this.renderActionButton(ctx, plusX, cy, addBtnW, 13, "+Add", OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
               String uname = newUF.getValue().strip();
               int step = 1;

               try {
                  step = Math.max(1, Integer.parseInt(newSF.getValue().strip()));
               } catch (NumberFormatException var8x) {
               }

               this.lanStepEntries.add(new WaitForLanStepAction.StepEntry(uname, step));
               int idx = this.lanStepEntries.size() - 1;
               ThemedTextInputBox uf2 = this.newTextField(80);
               uf2.setValue(uname);
               this.textFields.put("lan_user_" + idx, uf2);
               ThemedTextInputBox sf2 = this.newTextField(40);
               sf2.setIntegerOnlyFilter(true);
               sf2.setValue(String.valueOf(step));
               this.textFields.put("lan_step_" + idx, sf2);
               newUF.setValue("");
               newSF.setValue("1");
            });
         }
      }
   }

   private void rebuildLanStepFields() {
      this.textFields.keySet().removeIf(k -> k.startsWith("lan_user_") || k.startsWith("lan_step_"));

      for (int i = 0; i < this.lanStepEntries.size(); i++) {
         WaitForLanStepAction.StepEntry e = this.lanStepEntries.get(i);
         ThemedTextInputBox uf = this.newTextField(80);
         uf.setValue(e.username);
         this.textFields.put("lan_user_" + i, uf);
         ThemedTextInputBox sf = this.newTextField(40);
         sf.setIntegerOnlyFilter(true);
         sf.setValue(String.valueOf(e.step));
         this.textFields.put("lan_step_" + i, sf);
      }
   }

   private void load_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int btnH = 13;
      int topY = y + 2;
      int bottomY = y + 20;
      String info = this.rawPacketSummary();
      autismclient.gui.f.b.k.init(ctx, this.font, autismclient.gui.f.b.k.init(this.font, info, w, font, -1), font, UiColorPalette.textSecondary(), x, y - 10, false);
      int[] topBtns = this.layoutButtons(x, w, false, "Queue First", "Paste Base64");
      this.renderSecondaryButton(ctx, topBtns[0], topY, topBtns[1], btnH, "Queue First", mx, my, () -> {
         List<AutismSessionState.DelayedPacket> queue = AutismSessionState.getInstance().getDelayedPackets();
         if (queue != null && !queue.isEmpty()) {
            this.init_V_ck_CachePool(queue.get(0));
            AutismClientMessaging.sendPrefixed("Loaded first queued packet");
         } else {
            AutismClientMessaging.sendPrefixed("Queue is empty");
         }
      });
      this.renderSecondaryButton(ctx, topBtns[2], topY, topBtns[3], btnH, "Paste Base64", mx, my, () -> {
         List<AutismSessionState.DelayedPacket> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.init_V_ck_CachePool(pasted.get(0));
            AutismClientMessaging.sendPrefixed("Loaded first pasted packet");
         } else {
            AutismClientMessaging.sendPrefixed("Failed to paste packet data");
         }
      });
      this.renderSecondaryButton(ctx, x, bottomY, Math.min(w, this.buttonWidth("Clear Raw Packet")), btnH, "Clear Raw Packet", mx, my, () -> {
         this.actionTag.putString("packetData", "");
         AutismClientMessaging.sendPrefixed("Cleared raw packet data");
      });
   }

   private void renderWaitPacketEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<String> c2sTargets = this.packetTargetsFor("C2S");
      List<String> s2cTargets = this.packetTargetsFor("S2C");
      String summary = c2sTargets.isEmpty() && s2cTargets.isEmpty()
         ? "No packets selected. This step will continue on the next packet in either direction."
         : "This step continues as soon as any selected C2S or S2C packet arrives.";
      cy = this.renderHint(ctx, x, cy, w, summary);
      this.renderToggleRow(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 18;
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      int[] addBtns = this.layoutButtons(x, w, false, "Add C2S", "Add S2C");
      this.renderSecondaryButton(ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S", mx, my, () -> this.openWaitPacketSelector(true));
      this.renderSecondaryButton(ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C", mx, my, () -> this.openWaitPacketSelector(false));
      cy += 18;
      int[] loadBtns = this.layoutButtons(x, w, false, "Load Queue", "Clear All");
      this.renderSecondaryButton(ctx, loadBtns[0], cy, loadBtns[1], 13, "Load Queue", mx, my, this::loadWaitPacketsFromQueue);
      this.renderSecondaryButton(ctx, loadBtns[2], cy, loadBtns[3], 13, "Clear All", mx, my, () -> {
         this.waitPacketNames().clear();
         ScrollableListView vpC2s = this.entryViewports.get("wait_packet_c2s");
         if (vpC2s != null) {
            vpC2s.setScrollOffset(0);
         }

         ScrollableListView vpS2c = this.entryViewports.get("wait_packet_s2c");
         if (vpS2c != null) {
            vpS2c.setScrollOffset(0);
         }
      });
      cy += 20;
      cy = this.renderEntryRows(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "wait_packet_c2s",
         "C2S Packets",
         c2sTargets,
         this::removePacketTarget,
         this::waitPacketDisplayName,
         ignored -> null,
         () -> this.clearPacketTargets("C2S"),
         "No C2S packets selected"
      );
      cy += 4;
      this.renderEntryRows(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "wait_packet_s2c",
         "S2C Packets",
         s2cTargets,
         this::removePacketTarget,
         this::waitPacketDisplayName,
         ignored -> null,
         () -> this.clearPacketTargets("S2C"),
         "No S2C packets selected"
      );
   }

   private void renderWaitPacketMatchEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      if (this.packetMatchRules == null) {
         this.packetMatchRules = new ArrayList<>();
      }

      if (this.selectedPacketRule >= this.packetMatchRules.size()) {
         this.selectedPacketRule = this.packetMatchRules.isEmpty() ? -1 : 0;
      }

      cy = this.renderHint(ctx, x, cy, w, "Add a packet, then set field + rule.");
      this.renderToggleRow(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 18;
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      int[] addBtns = this.layoutButtons(x, w, false, "Add C2S", "Add S2C");
      this.renderSecondaryButton(ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S", mx, my, () -> this.openPacketMatchSelector(true));
      this.renderSecondaryButton(ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C", mx, my, () -> this.openPacketMatchSelector(false));
      cy += 18;
      this.renderPacketRules(ctx, x, cy, w, mx, my);
      cy += 13 + Math.max(1, Math.min(4, Math.max(1, this.packetMatchRules.size()))) * 14 + 5;
      if (this.packetMatchRules.isEmpty()) {
         this.renderHint(ctx, x, cy, w, "No packet rules yet. Add C2S or S2C.");
      } else {
         autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.packetMatchRules.get(Math.max(0, this.selectedPacketRule));
         List<String> dirOpts = new ArrayList<>();

         for (autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil d : autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.values()) {
            dirOpts.add(d.name());
         }

         cy = this.renderLabeledDropdown(
            ctx,
            font,
            "Direction",
            "_wpm_dir",
            dirOpts,
            rule.direction.ordinal(),
            x,
            cy,
            w,
            i -> rule.direction = autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.values()[i]
         );
         Class<? extends Packet<?>> packetClass = this.rulePacketClass(rule);
         List<String> fieldNames = new ArrayList<>(autismclient.util.macro.WaitPacketMatchAction.fieldNames(packetClass));
         List<String> fieldDisplay = new ArrayList<>();
         fieldDisplay.add("Packet Only");
         fieldDisplay.addAll(fieldNames);
         int fieldIdx = rule.load != null && !rule.load.isBlank() ? Math.max(0, fieldNames.indexOf(rule.load) + 1) : 0;
         cy = this.renderLabeledDropdown(ctx, font, "Field", "_wpm_field", fieldDisplay, fieldIdx, x, cy, w, i -> {
            rule.load = i <= 0 ? "" : fieldNames.get(i - 1);
            if (rule.load.isBlank()) {
               rule.operator = autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS;
            }

            List<String> vo = autismclient.util.macro.WaitPacketMatchAction.valueOptions(packetClass, rule.load);
            if (!vo.isEmpty() && (rule.read == null || rule.read.isBlank() || !vo.contains(rule.read))) {
               rule.read = vo.get(0);
            }

            this.loadPacketRuleValue();
         });
         if (rule.load != null && !rule.load.isBlank()) {
            List<String> opOpts = new ArrayList<>();

            for (autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil o : autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.values()) {
               opOpts.add(o.name());
            }

            cy = this.renderLabeledDropdown(
               ctx,
               font,
               "Operator",
               "_wpm_op",
               opOpts,
               rule.operator.ordinal(),
               x,
               cy,
               w,
               i -> rule.operator = autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.values()[i]
            );
            if (rule.operator != autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS) {
               List<String> valueOptions = autismclient.util.macro.WaitPacketMatchAction.valueOptions(packetClass, rule.load);
               if (!valueOptions.isEmpty()) {
                  int valIdx = Math.max(0, valueOptions.indexOf(rule.read));
                  cy = this.renderLabeledDropdown(ctx, font, "Value", "_wpm_value_dd", valueOptions, valIdx, x, cy, w, i -> {
                     rule.read = valueOptions.get(i);
                     this.loadPacketRuleValue();
                  });
               } else {
                  ThemedTextInputBox valueField = this.textFields.get("_wpm_value");
                  if (valueField != null) {
                     int lw = this.labelWidth(w, "Value", font, 72);
                     this.drawTrimmedText(ctx, "Value", x, cy, lw, font);
                     valueField.setX(this.controlX(x, lw));
                     valueField.setY(cy + 1);
                     valueField.setWidth(this.valueFieldWidth(w, lw));
                     valueField.render(ctx, mx, my, delta);
                     cy += 16;
                  }
               }
            }
         }
      }
   }

   private int renderLabeledDropdown(
      GuiGraphicsExtractor ctx,
      Identifier font,
      String label,
      String cacheKey,
      List<String> options,
      int selectedIdx,
      int x,
      int cy,
      int w,
      IntConsumer onSelect
   ) {
      if (options.isEmpty()) {
         return cy;
      }

      int lw = this.labelWidth(w, label, font, 72);
      this.drawTrimmedText(ctx, label, x, cy, lw, font);
      int ctrlX = this.controlX(x, lw);
      int ctrlW = this.dropdownWidth(options, this.valueFieldWidth(w, lw));
      int idx = Math.max(0, Math.min(selectedIdx, options.size() - 1));
      List<String> opts = options;
      IntConsumer cb = onSelect;
      autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
         .computeIfAbsent(cacheKey, k -> new autismclient.gui.ui.widgets.DropdownWidget(ctrlX, cy + 2, ctrlW, 13, opts, idx, cb));
      dd.setBounds(ctrlX, cy + 2, ctrlW, 13).setOptions(opts).setSelectedIndex(idx).setOnSelect(cb);
      this.dropdowns.add(dd);
      return cy + 14 + 2;
   }

   private void renderDropdown(String cacheKey, List<String> options, int selectedIdx, int x, int y, int w, int h, IntConsumer onSelect) {
      if (!options.isEmpty()) {
         int idx = Math.max(0, Math.min(selectedIdx, options.size() - 1));
         List<String> opts = options;
         IntConsumer cb = onSelect;
         autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache.computeIfAbsent(cacheKey, k -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, w, h, opts, idx, cb));
         dd.setBounds(x, y, w, h).setOptions(opts).setSelectedIndex(idx).setOnSelect(cb);
         this.dropdowns.add(dd);
      }
   }

   private void renderPacketRules(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, "Packet Rules (" + this.packetMatchRules.size() + ")", font, UiColorPalette.textBody(), x, y + 2, false);
      int clearW = this.buttonWidth("Clear");
      this.renderActionButton(
         ctx, x + w - clearW, y, clearW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, !this.packetMatchRules.isEmpty(), mx, my, () -> {
            this.packetMatchRules.clear();
            this.selectedPacketRule = -1;
         }
      );
      y += 14;
      int rows = Math.max(1, Math.min(4, Math.max(1, this.packetMatchRules.size())));
      int listH = rows * 14;
      int delW = 13;
      if (this.packetMatchRules.isEmpty()) {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No packet rules selected.", x, y, w - 5 - 1);
      } else {
         ScrollableListView viewport = this.viewportFor(this.entryViewports, "_wpm_rules", x, y, w, listH, 14, 5);
         viewport.setContentHeight(this.packetMatchRules.size() * 14);
         viewport.renderScrollbar(ctx, mx, my);
         int rowW = w - 5 - 1 - delW - 2;
         viewport.beginFrame(ctx, this.styleTheme.set(), 905969664);

         for (int i = viewport.getFirstVisibleRow(); i < this.packetMatchRules.size() && i <= viewport.getLastVisibleRow(); i++) {
            int iy = viewport.getRowY(i);
            if (iy != -2147483648) {
               autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.packetMatchRules.get(i);
               boolean selected = i == this.selectedPacketRule;
               boolean hovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  this.ruleLabel(rule),
                  autismclient.gui.f.NetHelper.init(x, iy, rowW, 13),
                  hovered,
                  selected,
                  ListRowRenderer.RowTone.NORMAL,
                  true
               );
               int fi = i;
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, rowW, 13, () -> {
                  this.applyPacketRuleValue();
                  this.selectedPacketRule = fi;
                  this.loadPacketRuleValue();
               }));
               this.init_V_GuiGraphicsExtract_2(ctx, x + rowW + 2, iy, delW, mx, my, () -> {
                  this.packetMatchRules.remove(fi);
                  if (this.selectedPacketRule >= this.packetMatchRules.size()) {
                     this.selectedPacketRule = this.packetMatchRules.size() - 1;
                  }

                  this.loadPacketRuleValue();
               });
            }
         }

         viewport.endFrame(ctx);
      }
   }

   private void renderSendPacketEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<AutismSessionState.DelayedPacket> actionPackets = this.queuedPackets();
      List<AutismSessionState.DelayedPacket> queuePackets = AutismSessionState.getInstance().getDelayedPackets();
      if (queuePackets == null) {
         queuePackets = Collections.emptyList();
      }

      List<AutismSessionState.DelayedPacket> finalQueuePackets = queuePackets;
      autismclient.gui.f.b.k.init(ctx, this.font, "Action Packets (" + actionPackets.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      int apClearW = this.buttonWidth("Clear");
      this.renderActionButton(
         ctx,
         x + w - apClearW,
         cy,
         apClearW,
         13,
         "Clear",
         OverlayActionButton.ButtonTone.DANGER,
         !actionPackets.isEmpty(),
         mx,
         my,
         () -> this.storeCapturedPackets(Collections.emptyList())
      );
      cy += 14;
      cy = this.renderDelayedPacketList(ctx, x, cy, w, mx, my, delta, "_send_packet_action", actionPackets, true, packetIndex -> {
         List<AutismSessionState.DelayedPacket> updated = this.queuedPackets();
         if (packetIndex >= 0 && packetIndex < updated.size()) {
            updated.remove(packetIndex);
            this.storeCapturedPackets(updated);
         }
      });
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      ThemedTextInputBox nameField = this.textFields.get("customName");
      if (nameField != null) {
         int lw = this.labelWidth(w, "Name", font);
         this.drawTrimmedText(ctx, "Name", x, cy, lw, font);
         nameField.setX(this.controlX(x, lw));
         nameField.setY(cy + 2);
         nameField.setWidth(this.valueFieldWidth(w, lw));
         nameField.render(ctx, mx, my, delta);
         cy += 16;
      }

      ThemedTextInputBox countField = this.textFields.get("sendCount");
      if (countField != null) {
         int lw = this.labelWidth(w, "Send Count", font);
         this.drawTrimmedText(ctx, "Send Count", x, cy, lw, font);
         countField.setX(this.controlX(x, lw));
         countField.setY(cy + 2);
         countField.setWidth(this.valueFieldWidth(w, lw));
         countField.render(ctx, mx, my, delta);
         cy += 16;
      }

      this.renderGuiWaitRow(ctx, font, "waitForGuiBefore", "waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.toggleStates.getOrDefault("waitForGuiBefore", false) && !this.toggleStates.getOrDefault("waitForGuiAfter", false)) {
         ThemedTextInputBox guiField = this.textFields.get("guiName");
         if (guiField != null) {
            guiField.setX(-1000);
         }
      } else {
         ThemedTextInputBox guiField = this.textFields.get("guiName");
         if (guiField != null) {
            int lw = this.labelWidth(w, "GUI Name", font);
            this.drawTrimmedText(ctx, "GUI Name", x, cy, lw, font);
            guiField.setX(this.controlX(x, lw));
            guiField.setY(cy + 2);
            guiField.setWidth(this.valueFieldWidth(w, lw));
            guiField.render(ctx, mx, my, delta);
            cy += 16;
         }
      }

      cy += 2;
      int[] fromBtns = this.layoutButtons(x, w, false, "From Queue", "Paste Base64");
      this.renderSecondaryButton(ctx, fromBtns[0], cy, fromBtns[1], 13, "From Queue", mx, my, () -> {
         this.storeCapturedPackets(finalQueuePackets);
         AutismClientMessaging.sendPrefixed("Loaded " + finalQueuePackets.size() + " packets from queue");
      });
      this.renderSecondaryButton(ctx, fromBtns[2], cy, fromBtns[3], 13, "Paste Base64", mx, my, () -> {
         List<AutismSessionState.DelayedPacket> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.storeCapturedPackets(pasted);
            AutismClientMessaging.sendPrefixed("Pasted " + pasted.size() + " packets");
         } else {
            bi.write("Clipboard does not contain packets.");
         }
      });
      cy += 18;
      int[] clearBtns = this.layoutButtons(x, w, false, "Clear", "GBreak");
      this.renderSecondaryButton(ctx, clearBtns[0], cy, clearBtns[1], 13, "Clear", mx, my, () -> {
         this.storeCapturedPackets(Collections.emptyList());
         AutismClientMessaging.sendPrefixed("Cleared packet list");
      });
      this.renderSecondaryButton(ctx, clearBtns[2], cy, clearBtns[3], 13, "GBreak", mx, my, this::startGBreakCapture);
      cy += 20;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      autismclient.gui.f.b.k.init(ctx, this.font, "Current Queue (" + finalQueuePackets.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 14;
      this.renderDelayedPacketList(ctx, x, cy, w, mx, my, delta, "_send_packet_queue", finalQueuePackets, false, packetIndex -> {
         if (packetIndex >= 0 && packetIndex < finalQueuePackets.size()) {
            List<AutismSessionState.DelayedPacket> updated = this.queuedPackets();
            updated.add(finalQueuePackets.get(packetIndex));
            this.storeCapturedPackets(updated);
         }
      });
   }

   private int renderDelayedPacketList(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      float delta,
      String listKey,
      List<AutismSessionState.DelayedPacket> packets,
      boolean removable,
      IntConsumer rowAction
   ) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int listH = 56;
      ScrollableListView packetViewport = this.viewportFor(this.entryViewports, listKey, x, y, w, listH, 14, 5);
      packetViewport.setContentHeight(packets.size() * 14);
      packetViewport.renderScrollbar(ctx, mx, my);
      int removeW = removable ? 13 : 0;
      int modeW = removable ? 14 : 0;
      int textW = w - 5 - 2 - (removable ? removeW + 2 + modeW + 2 : 0);
      if (packets.isEmpty()) {
         autismclient.gui.f.b.k.init(ctx, this.font, removable ? "(none - use GBreak, queue, or Paste)" : "(queue empty)", font, UiColorPalette.textSecondary(), x, y + 2, false);
         return y + listH;
      }

      packetViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
      int firstVis = packetViewport.getFirstVisibleRow();

      for (int i = firstVis; i < packets.size() && i <= packetViewport.getLastVisibleRow(); i++) {
         int iy = packetViewport.getRowY(i);
         if (iy != -2147483648) {
            AutismSessionState.DelayedPacket qp = packets.get(i);
            String pname = qp != null && qp.packet != null ? PacketNameResolver.nameOf(qp.packet) : "???";
            String rowText = i + 1 + ". " + pname + " d=" + (qp != null ? qp.getDelay() : 0);
            boolean hovered = mx >= x && mx < x + textW && my >= iy && my < iy + 13;
            autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
               ctx,
               this.font,
               Component.literal(rowText),
               autismclient.gui.f.NetHelper.init(x, iy, textW, 13),
               hovered,
               false,
               ListRowRenderer.RowTone.NORMAL,
               false
            );
            int rowIndex = i;
            if (!removable) {
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, textW, 13, () -> rowAction.accept(rowIndex)));
            }

            if (removable) {
               int modeX = x + textW + 2;
               boolean exact = qp != null && qp.isExactReplay();
               this.renderActionButton(
                  ctx,
                  modeX,
                  iy,
                  modeW,
                  13,
                  exact ? "E" : "R",
                  exact ? OverlayActionButton.ButtonTone.DANGER : OverlayActionButton.ButtonTone.SUCCESS,
                  qp != null && qp.packet != null,
                  mx,
                  my,
                  () -> {
                     List<AutismSessionState.DelayedPacket> updated = this.queuedPackets();
                     if (rowIndex >= 0 && rowIndex < updated.size()) {
                        AutismSessionState.DelayedPacket target = updated.get(rowIndex);
                        if (target != null && target.packet != null) {
                           target.setReplayMode(target.isExactReplay() ? AutismSessionState.ReplayMode.REGENERATE : AutismSessionState.ReplayMode.EXACT);
                           this.storeCapturedPackets(updated);
                        }
                     }
                  }
               );
               int removeX = modeX + modeW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy, removeW, mx, my, () -> rowAction.accept(rowIndex));
            }
         }
      }

      packetViewport.endFrame(ctx);
      return y + listH;
   }

   private void write_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int btnH = 13;
      int topY = y + 2;
      int bottomY = y + 20;
      String info = this.queuedPacketsSummary();
      autismclient.gui.f.b.k.init(ctx, this.font, autismclient.gui.f.b.k.init(this.font, info, w, font, -1), font, UiColorPalette.textSecondary(), x, y - 10, false);
      int[] topBtns = this.layoutButtons(x, w, false, "From Queue", "Paste Base64");
      this.renderSecondaryButton(ctx, topBtns[0], topY, topBtns[1], btnH, "From Queue", mx, my, () -> {
         List<AutismSessionState.DelayedPacket> queue = AutismSessionState.getInstance().getDelayedPackets();
         this.storeCapturedPackets(queue);
         AutismClientMessaging.sendPrefixed("Loaded " + this.queuedPackets().size() + " packets from queue");
      });
      this.renderSecondaryButton(ctx, topBtns[2], topY, topBtns[3], btnH, "Paste Base64", mx, my, () -> {
         List<AutismSessionState.DelayedPacket> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.storeCapturedPackets(pasted);
            AutismClientMessaging.sendPrefixed("Pasted " + pasted.size() + " packets");
         } else {
            bi.write("Clipboard does not contain packets.");
         }
      });
      String gbreakLbl = this.configurationOnly ? "Capture: In Game" : "GBreak";
      int[] bottomBtns = this.layoutButtons(x, w, false, "Clear", gbreakLbl);
      this.renderSecondaryButton(ctx, bottomBtns[0], bottomY, bottomBtns[1], btnH, "Clear", mx, my, () -> {
         this.storeCapturedPackets(Collections.emptyList());
         AutismClientMessaging.sendPrefixed("Cleared packet list");
      });
      this.renderActionButton(
         ctx, bottomBtns[2], bottomY, bottomBtns[3], btnH, gbreakLbl, OverlayActionButton.ButtonTone.SECONDARY, !this.configurationOnly, mx, my, () -> {
            if (!this.configurationOnly) {
               final int session = this.auditEditSession;
               AutismSessionState.getInstance().startGbreakCapture(() -> MC.execute(() -> {
                  if (this.auditEditSession != session || this.actionTag == null) {
                     return;
                  }
                  List<AutismSessionState.DelayedPacket> captured = AutismSessionState.getInstance().getGbreakCapturedPackets();
                  this.storeCapturedPackets(captured);
                  AutismClientMessaging.sendPrefixed(captured.isEmpty() ? "GBreak capture finished with no packet" : "GBreak packet captured");
               }));
               AutismClientMessaging.sendPrefixed("Break a block now to capture the GBreak packet");
            }
         }
      );
   }

   private void initPayloadEditor(PayloadAction seedAction) {
      this.payloadEditor = PayloadEditorModel.fromAction(seedAction);
      this.pendingPayloadEdit = null;
      this.lastPayloadRevision = -9223372036854775808L;
      this.cachedPayloadSnapshot = null;
      ActionEditorOverlay.PayloadEditorState state = this.buildPayloadEditorState(seedAction);
      this.payloadTemplate = state.template();
      this.payloadContentDirty = false;
      this.set_Z = false;
      this.put_Z = false;
      this.add_Z = false;
      this.payloadModeUserSelected = false;
      this.payloadAddTypeUserSelected = false;
      this.suppressPayloadListeners = false;
      ThemedTextInputBox channelField = this.newTextField(120);
      channelField.setValue(state.channel());
      channelField.setPlaceholder(Component.literal("namespace:channel"));
      channelField.setChangedListener(text -> {
         if (!this.suppressPayloadListeners) {
            this.put_Z = true;
            if (this.payloadEditor != null) {
               this.payloadEditor.channel = text != null && !text.isBlank() ? text.strip() : "minecraft:brand";
               this.payloadEditor.bumpRevision();
               this.queuePayloadEdit(ActionEditorOverlay.PayloadView.PREVIEW_ONLY, "");
            }

            this.inferPayloadModeSelection();
         }
      });
      this.textFields.put("payload_channel", channelField);
      if (seedAction != null) {
         seedAction.payloadPhase = this.payloadEditor.phase;
      }

      ThemedTextInputBox contentField = this.newTextField(126);
      contentField.setMultiline(true);
      contentField.setEnterInsertsNewline(true);
      contentField.setSpaceKeyInsertsSpace(true);
      contentField.setHoverEffectsEnabled(false);
      contentField.setFocusEffectsEnabled(false);
      contentField.setBackgroundColorOverride(1376389388);
      contentField.setMaxLength(32767);
      contentField.setPlaceholder(Component.literal("direction = C2S\nphase = PLAY\nchannel = minecraft:brand\n\nwriteMcString = vanilla"));
      contentField.setDisplayTextProvider(this::init_Component_Str);
      contentField.setRevisionProvider(this::payloadRevision);
      contentField.setChangedListener(text -> {
         if (!this.suppressPayloadListeners) {
            this.payloadContentDirty = true;
            this.queuePayloadEdit(ActionEditorOverlay.PayloadView.SCRIPT, text);
         }
      });
      this.textFields.put("payload_content", contentField);
      ThemedTextInputBox hexView = this.newTextField(138);
      hexView.setMultiline(true);
      hexView.setEnterInsertsNewline(true);
      hexView.setSpaceKeyInsertsSpace(true);
      hexView.setHoverEffectsEnabled(false);
      hexView.setFocusEffectsEnabled(false);
      hexView.setBackgroundColorOverride(1376389388);
      hexView.setEditable(true);
      hexView.setMaxLength(32767);
      hexView.setPlaceholder(Component.literal("00 01 02 FF"));
      hexView.setDisplayTextProvider(this::highlightHex);
      hexView.setRevisionProvider(this::payloadRevision);
      hexView.setChangedListener(text -> {
         if (!this.suppressPayloadListeners) {
            this.queuePayloadEdit(ActionEditorOverlay.PayloadView.HEX, text);
         }
      });
      this.textFields.put("payload_hex_view", hexView);
      ThemedTextInputBox utf8View = this.newTextField(138);
      utf8View.setMultiline(true);
      utf8View.setEnterInsertsNewline(true);
      utf8View.setSpaceKeyInsertsSpace(true);
      utf8View.setHoverEffectsEnabled(false);
      utf8View.setFocusEffectsEnabled(false);
      utf8View.setBackgroundColorOverride(1376389388);
      utf8View.setEditable(true);
      utf8View.setMaxLength(32767);
      utf8View.setPlaceholder(Component.literal("Raw UTF-8 text"));
      utf8View.setDisplayTextProvider(this::highlightJson);
      utf8View.setRevisionProvider(this::payloadRevision);
      utf8View.setChangedListener(text -> {
         if (!this.suppressPayloadListeners) {
            this.queuePayloadEdit(ActionEditorOverlay.PayloadView.UTF8, text);
         }
      });
      this.textFields.put("payload_utf8_view", utf8View);
      ThemedTextInputBox logicalView = this.newTextField(138);
      logicalView.setMultiline(true);
      logicalView.setEnterInsertsNewline(true);
      logicalView.setSpaceKeyInsertsSpace(true);
      logicalView.setHoverEffectsEnabled(false);
      logicalView.setFocusEffectsEnabled(false);
      logicalView.setBackgroundColorOverride(1376389388);
      logicalView.setEditable(true);
      logicalView.setMaxLength(32767);
      logicalView.setPlaceholder(Component.literal("direction = C2S\nphase = PLAY\nchannel = minecraft:brand\nbodyHex = ..."));
      logicalView.setDisplayTextProvider(this::write_Component);
      logicalView.setRevisionProvider(this::payloadRevision);
      logicalView.setChangedListener(text -> {
         if (!this.suppressPayloadListeners) {
            this.queuePayloadEdit(ActionEditorOverlay.PayloadView.LOGICAL, text);
         }
      });
      this.textFields.put("payload_logical_view", logicalView);
      ThemedTextInputBox commandField = this.newTextField(66);
      commandField.setIntegerOnlyFilter(true);
      commandField.setValue(String.valueOf(this.payloadEditor.commandApiValue));
      commandField.setPlaceholder(Component.literal("value"));
      commandField.setChangedListener(text -> {
         if (!this.suppressPayloadListeners && this.payloadEditor != null) {
            try {
               this.payloadEditor.commandApiValue = Integer.parseInt((text == null ? "" : text).strip());
               this.payloadEditor.hasCommandApiValue = true;
               this.payloadEditor.commandApiOverride = true;
               this.payloadEditor.bumpRevision();
               this.cachedPayloadSnapshot = null;
               this.toggleStates.put("payload_command_api", true);
               this.payloadContentDirty = true;
            } catch (NumberFormatException var3x) {
            }
         }
      });
      this.textFields.put("payload_command_value", commandField);
      this.toggleStates.put("payload_command_api", this.payloadEditor.commandApiOverride);
      this.pushPayloadEditorToFields(true);
      PayloadFieldCodec.PayloadTemplate template = this.editorPayloadTemplate();
      PayloadFieldCodec.EncodingMode suggestedMode = this.inferPayloadMode(template.channel(), template.fields(), template.encodingMode());
      this.modeIndices.put("payload_mode", this.payloadModeIndex(suggestedMode));
      this.modeIndices
         .put("payload_add_type", this.fieldTypeIndex(this.defaultAddFieldType(template.channel(), suggestedMode, template.fields())));
   }

   private ActionEditorOverlay.PayloadEditorState buildPayloadEditorState(PayloadAction seedAction) {
      PayloadAction action = new PayloadAction();
      if (seedAction != null) {
         action.fromTag(seedAction.toTag());
      }

      String channel = action.channel == null ? "" : action.channel.strip();
      byte[] bytes = new byte[0];
      boolean haveBytes = false;
      if (action.payloadData != null && !action.payloadData.isBlank()) {
         try {
            bytes = AutismPayloadSupport.parseHexOrBase64(action.payloadData);
            haveBytes = true;
         } catch (Exception ignored) {
            bytes = new byte[0];
         }
      }

      if (action.payloadJson != null && !action.payloadJson.isBlank()) {
         try {
            action.payloadJson = PayloadJsonCodec.prettyPrint(action.payloadJson);
            PayloadJsonCodec.EncodedPayload encoded = PayloadJsonCodec.encode(action);
            if (channel.isBlank() && encoded.channel() != null && !encoded.channel().isBlank()) {
               channel = encoded.channel();
            }

            if (!haveBytes) {
               bytes = encoded.bytes();
               if (bytes.length <= 0 && action.payloadData != null && !action.payloadData.isBlank()) {
                  boolean var16 = false;
               } else {
                  boolean commandRecognized = true;
               }
            }
         } catch (Exception var13) {
         }
      }

      if (channel.isBlank()) {
         channel = "minecraft:brand";
      }

      if (AutismPayloadSupport.isBrandChannel(channel)) {
         String brandText = AutismPayloadSupport.decodeMcString(bytes);
         if (brandText != null || bytes.length == 0) {
            return new ActionEditorOverlay.PayloadEditorState(
               channel,
               bytes,
               false,
               action.commandApiValue,
               ActionEditorOverlay.PayloadTemplate.BRAND_STRING,
               brandText != null ? brandText : AutismPayloadSupport.defaultClientBrand()
            );
         }
      }

      Integer commandValue = AutismPayloadSupport.detectCommandApiValue(null, channel, bytes);
      boolean commandRecognized = action.commandApiRecognized || commandValue != null;
      int resolvedCommandValue = commandValue != null ? commandValue : action.commandApiValue;
      ActionEditorOverlay.PayloadTemplate mode = ActionEditorOverlay.PayloadTemplate.BINARY_REPLAY;
      String contentText = "";
      if (commandRecognized) {
         mode = ActionEditorOverlay.PayloadTemplate.COMMAND_INT;
         contentText = String.valueOf(resolvedCommandValue);
      } else {
         String readableText = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (readableText.isBlank() && bytes.length != 0) {
            contentText = "Binary payload (" + bytes.length + " bytes). Safe replay keeps the captured bytes.";
         } else {
            mode = ActionEditorOverlay.PayloadTemplate.UTF8_TEXT;
            contentText = this.prettyJson(readableText);
         }
      }

      return new ActionEditorOverlay.PayloadEditorState(channel, bytes, commandRecognized, resolvedCommandValue, mode, contentText);
   }

   private String prettyJson(String text) {
      if (text != null && !text.isBlank()) {
         String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
         String stripped = normalized.strip();
         if (!stripped.startsWith("{") && !stripped.startsWith("[")) {
            return normalized;
         }

         try {
            return PRETTY_GSON.toJson(JsonParser.parseString(stripped));
         } catch (Throwable ignored) {
            return normalized;
         }
      } else {
         return "";
      }
   }

   private void prettyPrintPayloadJson(boolean syncPreview) {
      ThemedTextInputBox jsonField = this.textFields.get("payload_json");
      if (jsonField != null) {
         jsonField.setValue(PayloadJsonCodec.prettyPrint(jsonField.getValue()));
         if (syncPreview) {
            this.applyPayloadJson(true);
         }
      }
   }

   private void applyPayloadJson(boolean notifyOnError) {
      ThemedTextInputBox jsonField = this.textFields.get("payload_json");
      if (jsonField != null) {
         PayloadAction preview = new PayloadAction();
         if (this.payloadActionDraft != null) {
            preview.fromTag(this.payloadActionDraft.toTag());
         }

         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         ThemedTextInputBox rawField = this.textFields.get("payload_data");
         ThemedTextInputBox textField = this.textFields.get("payload_text");
         ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
         preview.payloadJson = jsonField.getValue();
         preview.channel = channelField == null ? preview.channel : channelField.getValue().strip();
         preview.payloadData = rawField == null ? preview.payloadData : rawField.getValue();
         preview.commandApiRecognized = this.toggleStates.getOrDefault("payload_command_api", preview.commandApiRecognized);
         if (commandField != null) {
            try {
               preview.commandApiValue = Integer.parseInt(commandField.getValue().strip());
            } catch (NumberFormatException var13) {
            }
         }

         try {
            preview.payloadJson = PayloadJsonCodec.prettyPrint(preview.payloadJson);
            PayloadJsonCodec.EncodedPayload encoded = PayloadJsonCodec.encode(preview);
            this.suppressPayloadListeners = true;

            try {
               jsonField.setValue(preview.payloadJson);
               if (channelField != null) {
                  channelField.setValue(encoded.channel());
               }

               if (rawField != null) {
                  rawField.setValue(AutismPayloadSupport.toHexString(encoded.bytes()));
               }

               if (textField != null) {
                  textField.setValue(AutismPayloadSupport.decodePrintableUtf8(encoded.bytes()));
               }

               this.put_Z = true;
               this.set_Z = true;
               this.add_Z = false;
            } finally {
               this.suppressPayloadListeners = false;
            }

            Integer commandValue = AutismPayloadSupport.detectCommandApiValue(null, encoded.channel(), encoded.bytes());
            if (commandValue != null) {
               this.toggleStates.put("payload_command_api", true);
               if (commandField != null) {
                  commandField.setValue(String.valueOf(commandValue));
               }
            }
         } catch (Exception e) {
            if (notifyOnError) {
               AutismClientMessaging.sendPrefixed("§cCould not rebuild payload from JSON: " + AutismPayloadSupport.describeError(e));
            }
         }
      }
   }

   private void setPayloadFields(List<PayloadFieldCodec.PayloadField> fields) {
      this.clearPayloadValueFields();
      List<PayloadFieldCodec.PayloadField> safeFields = fields == null ? List.of() : fields;
      this.payloadFieldCount = safeFields.size();
      if (this.payloadEditor != null) {
         this.payloadEditor.fields = new ArrayList<>(safeFields);
         this.payloadEditor.bumpRevision();
         this.payloadEditor.parseScript(this.payloadEditor.toScript());
         this.cachedPayloadSnapshot = null;
         this.pushPayloadEditorToFields(true);
      } else {
         ThemedTextInputBox contentField = this.textFields.get("payload_content");
         if (contentField != null) {
            contentField.setValue(PayloadFieldCodec.formatFields(safeFields));
         }
      }
   }

   private void clearPayloadValueFields() {
      this.textFields.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_value_"));
      this.modeIndices.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_type_"));
      this.dropdownCache.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_type_"));
      this.payloadFieldCount = 0;
   }

   private List<PayloadFieldCodec.PayloadField> payloadFields() {
      if (this.payloadEditor != null) {
         return this.payloadEditor.fields == null ? List.of() : this.payloadEditor.fields;
      }

      ThemedTextInputBox contentField = this.textFields.get("payload_content");
      return PayloadFieldCodec.parseFields(contentField == null ? "" : contentField.getValue());
   }

   private void inferPayloadModeSelection() {
      if (!this.suppressPayloadListeners) {
         String channel = this.payloadChannel();
         List<PayloadFieldCodec.PayloadField> fields = this.payloadFields();
         if (!this.payloadModeUserSelected) {
            PayloadFieldCodec.EncodingMode current = PAYLOAD_MODES.get(
               Math.max(0, Math.min(this.modeIndices.getOrDefault("payload_mode", this.payloadModeIndex(PayloadFieldCodec.EncodingMode.ADVANCED_MIXED)), PAYLOAD_MODES.size() - 1))
            );
            this.modeIndices.put("payload_mode", this.payloadModeIndex(this.inferPayloadMode(channel, fields, current)));
         }

         if (!this.payloadAddTypeUserSelected) {
            PayloadFieldCodec.EncodingMode mode = this.selectedPayloadMode(channel);
            this.modeIndices.put("payload_add_type", this.fieldTypeIndex(this.defaultAddFieldType(channel, mode, fields)));
         }
      }
   }

   private String payloadChannel() {
      if (this.payloadEditor != null && this.payloadEditor.channel != null && !this.payloadEditor.channel.isBlank()) {
         return this.payloadEditor.channel.strip();
      } else {
         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         if (channelField != null && !channelField.getValue().isBlank()) {
            return channelField.getValue().strip();
         } else {
            return this.payloadActionDraft != null && this.payloadActionDraft.channel != null && !this.payloadActionDraft.channel.isBlank() ? this.payloadActionDraft.channel.strip() : "minecraft:brand";
         }
      }
   }

   private PayloadFieldCodec.EncodingMode inferPayloadMode(String channel, List<PayloadFieldCodec.PayloadField> fields, PayloadFieldCodec.EncodingMode fallback) {
      List<PayloadFieldCodec.PayloadField> safeFields = fields == null ? List.of() : fields;
      if (AutismPayloadSupport.isBrandChannel(channel)) {
         return PayloadFieldCodec.EncodingMode.MINECRAFT_BYTEBUF;
      }

      if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip())) {
         return PayloadFieldCodec.EncodingMode.JAVA_DATA_OUTPUT;
      }

      for (PayloadFieldCodec.PayloadField field : safeFields) {
         if (field != null) {
            switch (field.type()) {
               case HEX_BYTES:
               case RAW_BYTES:
               case BYTE_ARRAY:
                  return PayloadFieldCodec.EncodingMode.MANUAL_HEX;
               case JAVA_WRITE_UTF:
               case BYTE:
               case UNSIGNED_BYTE:
               case BOOLEAN:
               case SHORT:
               case UNSIGNED_SHORT:
               case CHAR:
               case INT:
               case LONG:
               case FLOAT:
               case DOUBLE:
                  return PayloadFieldCodec.EncodingMode.JAVA_DATA_OUTPUT;
               case VAR_INT:
               case VAR_LONG:
               case MINECRAFT_STRING:
               case IDENTIFIER:
               case UUID_FIELD:
               case BLOCK_POS:
               case ENUM_VAR_INT:
               case OPTIONAL_VALUE:
                  return PayloadFieldCodec.EncodingMode.MINECRAFT_BYTEBUF;
               case JSON_STRING:
               case TEXT_COMPONENT:
               case NBT:
               case ITEM_STACK:
                  return PayloadFieldCodec.EncodingMode.JSON_TEXT;
               case STRING_BYTES:
               case RAW_UTF8_STRING:
                  return PayloadFieldCodec.EncodingMode.RAW_UTF8;
            }
         }
      }

      return fallback == null ? PayloadFieldCodec.EncodingMode.ADVANCED_MIXED : fallback;
   }

   private PayloadFieldCodec.FieldType defaultAddFieldType(String channel, PayloadFieldCodec.EncodingMode mode, List<PayloadFieldCodec.PayloadField> fields) {
      if (AutismPayloadSupport.isBrandChannel(channel)) {
         return PayloadFieldCodec.FieldType.MINECRAFT_STRING;
      } else if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip())) {
         return PayloadFieldCodec.FieldType.JAVA_WRITE_UTF;
      } else if (mode == PayloadFieldCodec.EncodingMode.MANUAL_HEX) {
         return PayloadFieldCodec.FieldType.HEX_BYTES;
      } else if (mode == PayloadFieldCodec.EncodingMode.JAVA_DATA_OUTPUT) {
         return PayloadFieldCodec.FieldType.JAVA_WRITE_UTF;
      } else if (mode == PayloadFieldCodec.EncodingMode.MINECRAFT_BYTEBUF) {
         return PayloadFieldCodec.FieldType.MINECRAFT_STRING;
      } else {
         return mode == PayloadFieldCodec.EncodingMode.JSON_TEXT ? PayloadFieldCodec.FieldType.TEXT_COMPONENT : PayloadFieldCodec.FieldType.STRING_BYTES;
      }
   }

   private void queuePayloadEdit(ActionEditorOverlay.PayloadView kind, String text) {
      if (kind != null) {
         if (this.pendingPayloadEdit != null && this.pendingPayloadEdit.view() != kind) {
            this.flushPendingPayloadEdit(true);
         }

         this.pendingPayloadEdit = new ActionEditorOverlay.PendingPayloadEdit(kind, text == null ? "" : text);
         this.pendingPayloadEditTime = System.nanoTime();
      }
   }

   private void flushPendingPayloadEdit(boolean force) {
      ActionEditorOverlay.PendingPayloadEdit edit = this.pendingPayloadEdit;
      if (edit != null && (force || System.nanoTime() - this.pendingPayloadEditTime >= 125000000L)) {
         this.pendingPayloadEdit = null;
         if (this.payloadEditor == null) {
            this.inferPayloadModeSelection();
         } else {
            switch (edit.view()) {
               case SCRIPT:
                  if (this.payloadEditor.parseScript(edit.text())) {
                     this.pushPayloadHeaderToFields(false);
                     this.pushPayloadContentToFields(false);
                  }

                  this.inferPayloadModeSelection();
                  break;
               case HEX:
                  this.init_V_Str_2(edit.text());
                  break;
               case UTF8:
                  this.load_V_Str(edit.text());
                  break;
               case LOGICAL:
                  this.read_V(edit.text());
                  break;
               case PREVIEW_ONLY:
                  this.pushPayloadContentToFields(false);
            }

            this.cachedPayloadSnapshot = null;
         }
      }
   }

   private void init_V_Str_2(String text) {
      if (this.payloadEditor != null && this.payloadEditor.parseHex(text)) {
         this.payloadContentDirty = true;
         this.pushPayloadEditorToFields(false);
      }
   }

   private void load_V_Str(String text) {
      if (this.payloadEditor != null) {
         this.payloadEditor.read(text);
         this.payloadContentDirty = true;
         this.pushPayloadEditorToFields(false);
      }
   }

   private void read_V(String text) {
      if (this.payloadEditor != null && this.payloadEditor.write(text)) {
         this.payloadContentDirty = true;
         this.pushPayloadEditorToFields(false);
      }
   }

   private void pushPayloadEditorToFields(boolean force) {
      this.pushPayloadHeaderToFields(force);
      this.pushPayloadContentToFields(force);
   }

   private void pushPayloadHeaderToFields(boolean force) {
      if (this.payloadEditor != null) {
         this.suppressPayloadListeners = true;

         try {
            ThemedTextInputBox channelField = this.textFields.get("payload_channel");
            if (channelField != null && (force || !channelField.isFocused())) {
               channelField.setValue(this.payloadEditor.channel == null ? "" : this.payloadEditor.channel);
            }

            this.modeIndices.put("payload_mode", this.payloadModeIndex(PayloadFieldCodec.EncodingMode.parse(this.payloadEditor.sync, this.payloadEditor.channel)));
            ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
            if (commandField != null && (force || !commandField.isFocused())) {
               commandField.setValue(String.valueOf(this.payloadEditor.commandApiValue));
            }

            this.toggleStates.put("payload_command_api", this.payloadEditor.commandApiOverride);
         } finally {
            this.suppressPayloadListeners = false;
         }
      }
   }

   private void pushPayloadContentToFields(boolean force) {
      if (this.payloadEditor != null) {
         long revision = this.payloadEditor.revision();
         if (force || this.lastPayloadRevision != revision) {
            this.suppressPayloadListeners = true;

            try {
               ThemedTextInputBox contentField = this.textFields.get("payload_content");
               if (contentField != null && (force || !contentField.isFocused())) {
                  contentField.setValue(this.payloadEditor.toScript());
               }

               ThemedTextInputBox hexView = this.textFields.get("payload_hex_view");
               if (hexView != null && (force || !hexView.isFocused())) {
                  hexView.setValue(this.payloadEditor.toHexDump());
               }

               ThemedTextInputBox utf8View = this.textFields.get("payload_utf8_view");
               if (utf8View != null && (force || !utf8View.isFocused())) {
                  utf8View.setValue(this.payloadEditor.read());
               }

               ThemedTextInputBox logicalView = this.textFields.get("payload_logical_view");
               if (logicalView != null && (force || !logicalView.isFocused())) {
                  logicalView.setValue(this.payloadEditor.write());
               }
            } finally {
               this.suppressPayloadListeners = false;
            }

            this.lastPayloadRevision = revision;
         }
      }
   }

   private String escapeNewlines(String text) {
      return text != null && !text.isEmpty() ? text.replace("\\", "\\\\").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n") : "";
   }

   private int fieldTypeIndex(PayloadFieldCodec.FieldType type) {
      int index = PAYLOAD_FIELD_TYPES.indexOf(type);
      return index < 0 ? PAYLOAD_FIELD_TYPES.indexOf(PayloadFieldCodec.FieldType.STRING_BYTES) : index;
   }

   private PayloadFieldCodec.FieldType fieldTypeAt(int row) {
      int index = Math.max(
         0, Math.min(this.modeIndices.getOrDefault(this.fieldTypeKey(row), this.fieldTypeIndex(PayloadFieldCodec.FieldType.STRING_BYTES)), PAYLOAD_FIELD_TYPES.size() - 1)
      );
      return PAYLOAD_FIELD_TYPES.get(index);
   }

   private String fieldTypeKey(int row) {
      return "payload_type_" + row;
   }

   private String fieldValueKey(int row) {
      return "payload_value_" + row;
   }

   private int payloadModeIndex(PayloadFieldCodec.EncodingMode mode) {
      int index = PAYLOAD_MODES.indexOf(mode);
      return index < 0 ? PAYLOAD_MODES.indexOf(PayloadFieldCodec.EncodingMode.ADVANCED_MIXED) : index;
   }

   private PayloadFieldCodec.EncodingMode selectedPayloadMode(String channel) {
      int fallback = this.payloadModeIndex(PayloadFieldCodec.EncodingMode.parse(this.payloadActionDraft == null ? "" : this.payloadActionDraft.payloadEncodingMode, channel));
      int index = Math.max(0, Math.min(this.modeIndices.getOrDefault("payload_mode", fallback), PAYLOAD_MODES.size() - 1));
      return PAYLOAD_MODES.get(index);
   }

   private void renderPayloadSnapshotEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      ActionEditorOverlay.PayloadSnapshot preview = this.buildPayloadSnapshot();
      PayloadFieldCodec.PayloadTemplate template = preview.template();
      PayloadFieldCodec.EncodeResult built = preview.built();
      autismclient.gui.f.b.k.init(ctx, this.font, "Custom Payload", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 14;
      cy = this.renderPayloadStatus(ctx, x, cy, w);
      cy = this.renderPayloadTabs(ctx, x, cy, w, mx, my);
      int dirLabelW = 18;
      int dirW = this.buttonWidth(template.direction().name());
      int modeLabelW = 28;
      autismclient.gui.f.b.k.init(ctx, this.font, "Dir", font, UiColorPalette.textSecondary(), x, cy + 3, false);
      this.renderActionButton(
         ctx, x + dirLabelW, cy, dirW, 13, template.direction().name(), OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> {
            this.flushPendingPayloadEdit(true);
            if (this.payloadEditor != null) {
               this.payloadEditor.direction = PayloadFieldCodec.toggleDirection(this.payloadEditor.direction);
               this.payloadEditor.bumpRevision();
               this.cachedPayloadSnapshot = null;
               this.pushPayloadEditorToFields(false);
            } else if (this.payloadActionDraft != null) {
               this.payloadActionDraft.payloadDirection = PayloadFieldCodec.toggleDirection(this.payloadActionDraft.payloadDirection);
            }

            this.payloadContentDirty = true;
            this.resizeEditorToContent();
         }
      );
      int modeLabelX = x + dirLabelW + dirW + 5;
      autismclient.gui.f.b.k.init(ctx, this.font, "Mode", font, UiColorPalette.textSecondary(), modeLabelX, cy + 3, false);
      int modeX = modeLabelX + modeLabelW;
      this.renderPayloadModeDropdown(modeX, cy, this.dropdownWidth(PAYLOAD_MODE_LABELS, Math.max(54, x + w - modeX)), mx, my, template);
      cy += 18;
      ThemedTextInputBox channelField = this.textFields.get("payload_channel");
      if (channelField != null) {
         int lw = this.labelWidth(w, "Channel", font, 64);
         this.drawTrimmedText(ctx, "Channel", x, cy, lw, font);
         int ctrlX = this.controlX(x, lw);
         int ctrlW = this.valueFieldWidth(w, lw);
         channelField.setX(ctrlX);
         channelField.setY(cy + 2);
         channelField.setWidth(ctrlW);
         channelField.render(ctx, mx, my, delta);
         cy += 16;
      }

      if (this.payloadEditor != null && this.payloadEditor.hasCommandApiValue) {
         ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
         if (commandField != null) {
            int lw = this.labelWidth(w, "CommandApi", font, 64);
            this.drawTrimmedText(ctx, "CommandApi", x, cy, lw, font);
            int ctrlX = this.controlX(x, lw);
            int ctrlW = this.valueFieldWidth(w, lw);
            int btnW = Math.max(this.buttonWidth("Override"), this.buttonWidth("Exact"));
            int fieldW = Math.max(38, ctrlW - btnW - 3);
            commandField.setX(ctrlX);
            commandField.setY(cy + 2);
            commandField.setWidth(fieldW);
            commandField.render(ctx, mx, my, delta);
            boolean override = this.toggleStates.getOrDefault("payload_command_api", this.payloadEditor.commandApiOverride);
            this.renderActionButton(
               ctx,
               ctrlX + fieldW + 3,
               cy + 2,
               btnW,
               13,
               override ? "Override" : "Exact",
               override ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.SECONDARY,
               true,
               mx,
               my,
               () -> {
                  if (this.payloadEditor != null) {
                     this.payloadEditor.commandApiOverride = !this.payloadEditor.commandApiOverride;
                     this.payloadEditor.bumpRevision();
                     this.cachedPayloadSnapshot = null;
                     this.toggleStates.put("payload_command_api", this.payloadEditor.commandApiOverride);
                     this.payloadContentDirty = true;
                  }

                  this.resizeEditorToContent();
               }
            );
            cy += 16;
         }
      }

      if (this.payloadTabIndex == 0) {
         autismclient.gui.f.b.k.init(ctx, this.font, "Packet Script", font, UiColorPalette.textBody(), x, cy + 2, false);
         int psClearW = this.buttonWidth("Clear");
         this.renderActionButton(
            ctx, x + w - psClearW, cy, psClearW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, !this.payloadFields().isEmpty(), mx, my, () -> {
               ThemedTextInputBox contentField = this.textFields.get("payload_content");
               if (contentField != null) {
                  contentField.setValue("");
               }

               this.payloadContentDirty = true;
               this.resizeEditorToContent();
            }
         );
         cy += 15;
         int addButtonW = this.buttonWidth("Add");
         this.renderAddFieldTypeDropdown(x, cy, this.dropdownWidth(PAYLOAD_FIELD_TYPE_LABELS, Math.max(54, w - addButtonW - 4)), mx, my);
         this.renderActionButton(
            ctx,
            x + w - addButtonW,
            cy,
            addButtonW,
            13,
            "Add",
            OverlayActionButton.ButtonTone.SUCCESS,
            true,
            mx,
            my,
            () -> this.addFieldOfType(this.modeIndices.getOrDefault("payload_add_type", this.fieldTypeIndex(PayloadFieldCodec.FieldType.STRING_BYTES)))
         );
         cy += 17;
         ThemedTextInputBox contentField = this.textFields.get("payload_content");
         if (contentField != null) {
            contentField.setX(x);
            contentField.setY(cy);
            contentField.setWidth(w);
            contentField.setHeight(126);
            contentField.render(ctx, mx, my, delta);
         }

         cy += 130;
      } else if (this.payloadTabIndex == 1) {
         this.pushPayloadContentToFields(false);
         ThemedTextInputBox hexView = this.textFields.get("payload_hex_view");
         if (hexView != null) {
            int bytes = this.payloadEditor == null ? built.bytes().length : this.payloadEditor.bodyBytes.length;
            autismclient.gui.f.b.k.init(ctx, this.font, "Body Hex - payload body only (" + bytes + " bytes)", font, UiColorPalette.textBody(), x, cy + 2, false);
            cy += 14;
            hexView.setX(x);
            hexView.setY(cy);
            hexView.setWidth(w);
            hexView.setHeight(138);
            hexView.render(ctx, mx, my, delta);
            cy += 142;
         }
      } else if (this.payloadTabIndex == 2) {
         this.pushPayloadContentToFields(false);
         ThemedTextInputBox utf8View = this.textFields.get("payload_utf8_view");
         if (utf8View != null) {
            autismclient.gui.f.b.k.init(ctx, this.font, "UTF-8 / Raw Text Body", font, UiColorPalette.textBody(), x, cy + 2, false);
            cy += 14;
            utf8View.setX(x);
            utf8View.setY(cy);
            utf8View.setWidth(w);
            utf8View.setHeight(138);
            utf8View.render(ctx, mx, my, delta);
            cy += 142;
         }
      } else {
         this.pushPayloadContentToFields(false);
         ThemedTextInputBox logicalView = this.textFields.get("payload_logical_view");
         if (logicalView != null) {
            autismclient.gui.f.b.k.init(ctx, this.font, "Logical Packet - envelope + body", font, UiColorPalette.textBody(), x, cy + 2, false);
            cy += 14;
            logicalView.setX(x);
            logicalView.setY(cy);
            logicalView.setWidth(w);
            logicalView.setHeight(138);
            logicalView.render(ctx, mx, my, delta);
            cy += 142;
         }
      }

      String notice = this.payloadIssueText(built);
      boolean modelBad = this.payloadEditor != null && this.payloadEditor.flush != null && !this.payloadEditor.flush.isBlank();
      int noticeColor = !modelBad && built.errors().isEmpty() ? (!built.warnings().isEmpty() ? -14249 : UiColorPalette.textSecondary()) : UiColorPalette.errorText();
      autismclient.gui.f.b.k.init(
         ctx, this.font, autismclient.gui.f.b.k.init(this.font, notice, Math.max(10, w), font, noticeColor), font, noticeColor, x, cy + 2, false
      );
      cy += 14;
      String sendLbl = this.configurationOnly ? "Send: In Game" : "Send";
      int[] sendBtns = this.layoutButtons(x, w, false, sendLbl, "Reset");
      this.renderActionButton(
         ctx, sendBtns[0], cy, sendBtns[1], 13, sendLbl, OverlayActionButton.ButtonTone.SUCCESS, !this.configurationOnly && built.isValid() && !modelBad, mx, my, () -> {
            if (!this.configurationOnly) {
               try {
                  PayloadAction action = this.load_H();
                  action.execute(MC);
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cPayload send failed: " + AutismPayloadSupport.describeError(e));
               }
            }
         }
      );
      this.renderActionButton(ctx, sendBtns[2], cy, sendBtns[3], 13, "Reset", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> {
         this.resetPayloadEditor();
         this.resizeEditorToContent();
      });
      cy += 20;
      if (this.isConfigPhase()) {
         cy = this.renderHintColored(ctx, x, cy, w, "Config-phase payload — sending may disconnect.", -1657776);
      }

      this.renderHint(ctx, x, cy, w, this.payloadRowsHint());
   }

   private int renderPayloadTabs(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      String[] tabs = new String[]{"Packet", "Body Hex", "UTF-8", "Logical"};
      int gap = 3;
      int tabW = Math.max(42, (w - gap * (tabs.length - 1)) / tabs.length);

      for (int i = 0; i < tabs.length; i++) {
         int tx = x + i * (tabW + gap);
         int tw = i == tabs.length - 1 ? Math.max(30, x + w - tx) : tabW;
         OverlayActionButton.ButtonTone variant = this.payloadTabIndex == i ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.SECONDARY;
         int index = i;
         this.renderActionButton(ctx, tx, y, tw, 13, tabs[i], variant, true, mx, my, () -> {
            this.flushPendingPayloadEdit(true);
            this.payloadTabIndex = index;
            this.unfocusPayloadViewFields();
            this.resizeEditorToContent();
         });
      }

      return y + 18;
   }

   private void unfocusPayloadViewFields() {
      for (String key : List.of("payload_content", "payload_hex_view", "payload_utf8_view", "payload_logical_view")) {
         ThemedTextInputBox field = this.textFields.get(key);
         if (field != null) {
            field.setFocused(false);
         }
      }
   }

   private void renderAddFieldTypeDropdown(int x, int y, int w, int mx, int my) {
      int idx = Math.max(
         0,
         Math.min(
            this.modeIndices
               .getOrDefault(
                  "payload_add_type",
                  this.fieldTypeIndex(this.defaultAddFieldType(this.payloadChannel(), this.selectedPayloadMode(this.payloadChannel()), this.payloadFields()))
               ),
            PAYLOAD_FIELD_TYPE_LABELS.size() - 1
         )
      );
      autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
         .computeIfAbsent("payload_add_type", key -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, w, 13, PAYLOAD_FIELD_TYPE_LABELS, idx, newIdx -> {
            this.payloadAddTypeUserSelected = true;
            this.modeIndices.put("payload_add_type", newIdx);
         }));
      dd.setBounds(x, y, w, 13).setOptions(PAYLOAD_FIELD_TYPE_LABELS).setSelectedIndex(idx).setOnSelect(newIdx -> {
         this.payloadAddTypeUserSelected = true;
         this.modeIndices.put("payload_add_type", newIdx);
      }).setLabelOverride("");
      this.dropdowns.add(dd);
   }

   private void addFieldOfType(int index) {
      if (index >= 0 && index < PAYLOAD_FIELD_TYPES.size()) {
         PayloadFieldCodec.FieldType type = PAYLOAD_FIELD_TYPES.get(index);
         this.appendContentLine(type.label() + " = " + type.defaultValue());
         if (!this.payloadModeUserSelected) {
            this.modeIndices
               .put(
                  "payload_mode",
                  this.payloadModeIndex(
                     this.inferPayloadMode(
                        this.payloadChannel(), List.of(new PayloadFieldCodec.PayloadField(type, type.defaultValue(), true)), this.selectedPayloadMode(this.payloadChannel())
                     )
                  )
               );
         }

         this.resizeEditorToContent();
      }
   }

   private void updateHexView(PayloadFieldCodec.EncodeResult built) {
      ThemedTextInputBox hexView = this.textFields.get("payload_hex_view");
      if (hexView != null && !hexView.isFocused()) {
         this.suppressPayloadListeners = true;

         try {
            hexView.setValue(this.formatHex(built.bytes()));
         } finally {
            this.suppressPayloadListeners = false;
         }
      }

      ThemedTextInputBox utf8View = this.textFields.get("payload_utf8_view");
      if (utf8View != null && !utf8View.isFocused()) {
         this.suppressPayloadListeners = true;

         try {
            utf8View.setValue(this.payloadPreviewText(built));
         } finally {
            this.suppressPayloadListeners = false;
         }
      }
   }

   private String formatHex(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         StringBuilder sb = new StringBuilder(bytes.length * 3 + bytes.length / 16 * 10);

         for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
               sb.append((char)(i % 16 == 0 ? '\n' : ' '));
            }

            sb.append(String.format(Locale.ROOT, "%02X", bytes[i] & 255));
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private String payloadPreviewText(PayloadFieldCodec.EncodeResult built) {
      PayloadFieldCodec.PayloadPreview preview = built.preview();
      String fullUtf8 = AutismPayloadSupport.decodePrintableUtf8(built.bytes());
      if (fullUtf8 != null && !fullUtf8.isBlank()) {
         return fullUtf8;
      } else if (preview.utf8Preview() != null && !preview.utf8Preview().isBlank()) {
         return preview.utf8Preview();
      } else if (preview.mcStringPreview() != null && !preview.mcStringPreview().isBlank()) {
         return preview.mcStringPreview();
      } else {
         return preview.javaUtfPreview() != null && !preview.javaUtfPreview().isBlank() ? preview.javaUtfPreview() : "";
      }
   }

   private String payloadIssueText(PayloadFieldCodec.EncodeResult built) {
      if (this.payloadEditor != null && this.payloadEditor.flush != null && !this.payloadEditor.flush.isBlank()) {
         return "Fix: " + this.shortenIssue(this.payloadEditor.flush);
      }

      String scriptIssue = this.payloadScriptIssue();
      if (scriptIssue != null) {
         return "Fix: " + this.shortenIssue(scriptIssue);
      }

      if (this.payloadTabIndex == 1) {
         ThemedTextInputBox hexView = this.textFields.get("payload_hex_view");
         if (hexView != null && hexView.isFocused()) {
            try {
               this.parseHex(hexView.getValue());
            } catch (Exception e) {
               return "Bad hex: " + this.shortenIssue(AutismPayloadSupport.describeError(e));
            }
         }
      }

      if (!built.errors().isEmpty()) {
         return "Fix: " + this.shortenIssue(built.errors().get(0));
      } else {
         return !built.warnings().isEmpty() ? "Warn: " + this.shortenIssue(built.warnings().get(0)) : "Ready: " + built.bytes().length + " bytes";
      }
   }

   private String shortenIssue(String message) {
      String text = message == null ? "invalid payload" : message.replace('\n', ' ').strip();
      return text.length() <= 34 ? text : text.substring(0, 31) + "...";
   }

   private byte[] parseHex(String text) {
      String value = text == null ? "" : text.strip();
      if (value.isEmpty()) {
         return new byte[0];
      }

      String compact = value.replaceAll("(?i)0x", "").replaceAll("\\s+", "");
      if (compact.isEmpty()) {
         return new byte[0];
      }

      if ((compact.length() & 1) != 0) {
         throw new IllegalArgumentException("Hex needs byte pairs");
      }

      if (!compact.matches("(?i)[0-9a-f]+")) {
         throw new IllegalArgumentException("Only hex digits are allowed here");
      }

      byte[] out = new byte[compact.length() / 2];

      for (int i = 0; i < compact.length(); i += 2) {
         out[i / 2] = (byte)Integer.parseInt(compact.substring(i, i + 2), 16);
      }

      return out;
   }

   private String load_Str_BArr(byte[] bytes) {
      String text = AutismPayloadSupport.decodePrintableUtf8(bytes);
      if (text != null && !text.isBlank()) {
         for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (chr != '\n' && chr != '\r' && chr != '\t' && (Character.isISOControl(chr) || chr == '�')) {
               return null;
            }
         }

         return text;
      } else {
         return null;
      }
   }

   private String payloadScriptIssue() {
      ThemedTextInputBox contentField = this.textFields.get("payload_content");
      if (contentField == null) {
         return null;
      }

      String text = contentField.getValue();
      if (text != null && !text.isBlank()) {
         String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

         for (int i = 0; i < lines.length; i++) {
            String raw = lines[i] == null ? "" : lines[i];
            String line = raw.strip();
            if (!line.isEmpty() && !line.startsWith("#")) {
               int split = this.keyValueSeparatorIndex(raw);
               if (split < 0) {
                  return "Line " + (i + 1) + " needs writer = value";
               }

               String typeText = raw.substring(0, split).strip();
               if (!this.isPacketHeaderKey(typeText) && this.parseFieldType(typeText) == null) {
                  return "Line " + (i + 1) + " unknown writer";
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private long payloadRevision() {
      return this.payloadEditor == null ? 0L : this.payloadEditor.revision();
   }

   private Component init_Component_Str(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      String[] lines = safe.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         if (i > 0) {
            this.appendColored(out, "\n", 7829367);
         }

         String line = lines[i];
         if (!line.isEmpty()) {
            String stripped = line.stripLeading();
            if (stripped.startsWith("#")) {
               this.appendColored(out, line, 7829367);
            } else {
               int split = this.keyValueSeparatorIndex(line);
               if (split < 0) {
                  this.appendColored(out, line, 16742263);
               } else {
                  String typePart = line.substring(0, split);
                  String sep = line.substring(split, split + 1);
                  String valuePart = line.substring(split + 1);
                  PayloadFieldCodec.FieldType type = this.parseFieldType(typePart.strip());
                  this.appendColored(out, typePart, this.isPacketHeaderKey(typePart.strip()) ? 16328306 : (type == null ? 16742263 : 6740463));
                  this.appendColored(out, sep, 11184810);
                  this.appendHighlightedValue(out, valuePart);
               }
            }
         }
      }

      return out;
   }

   private boolean isPacketHeaderKey(String key) {
      if (key == null) {
         return false;
      }

      boolean var10000;
      switch (key.strip().toLowerCase(Locale.ROOT)) {
         case "direction":
         case "phase":
         case "channel":
         case "packetid":
         case "packet_id":
         case "packetclass":
         case "packet_class":
         case "sourceprotocol":
         case "source_protocol":
         case "provenance":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private Component highlightHex(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      int hexCount = 0;

      for (int i = 0; i < safe.length(); i++) {
         char chr = safe.charAt(i);
         if (chr == '\n' || Character.isWhitespace(chr)) {
            this.appendColored(out, Character.toString(chr), 7829367);
         } else if (this.isHexDigit(chr)) {
            int color = hexCount / 2 % 2 == 0 ? 10936878 : 6740463;
            this.appendColored(out, Character.toString(chr), color);
            hexCount++;
         } else {
            this.appendColored(out, Character.toString(chr), 16742263);
         }
      }

      return out;
   }

   private Component highlightJson(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      boolean inString = false;
      boolean escaped = false;

      for (int i = 0; i < safe.length(); i++) {
         char chr = safe.charAt(i);
         int color;
         if (chr == '\n') {
            color = 7829367;
         } else if (Character.isWhitespace(chr)) {
            color = 7829367;
         } else if (escaped) {
            color = 11436543;
            escaped = false;
         } else if (chr == '\\') {
            color = 11436543;
            escaped = true;
         } else if (chr == '"' || chr == '\'') {
            color = 15129460;
            inString = !inString;
         } else if (inString) {
            color = 15129460;
         } else if ("{}[],:=".indexOf(chr) >= 0) {
            color = 6740463;
         } else if (Character.isDigit(chr) || chr == '-' || chr == '+') {
            color = 11436543;
         } else if (Character.isLetter(chr)) {
            color = 10936878;
         } else {
            color = 16316658;
         }

         this.appendColored(out, Character.toString(chr), color);
      }

      return out;
   }

   private Component write_Component(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      String[] lines = safe.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         if (i > 0) {
            this.appendColored(out, "\n", 7829367);
         }

         String line = lines[i];
         int split = this.keyValueSeparatorIndex(line);
         if (split < 0) {
            this.appendColored(out, line, line.strip().startsWith("#") ? 7829367 : 16316658);
         } else {
            String key = line.substring(0, split);
            String sep = line.substring(split, split + 1);
            String value = line.substring(split + 1);
            this.appendColored(out, key, !this.isPacketHeaderKey(key) && !key.toLowerCase(Locale.ROOT).contains("hex") ? 10936878 : 6740463);
            this.appendColored(out, sep, 11184810);
            if (key.toLowerCase(Locale.ROOT).contains("hex")) {
               this.appendColored(out, value, 15129460);
            } else {
               this.appendHighlightedValue(out, value);
            }
         }
      }

      return out;
   }

   private void appendHighlightedValue(MutableComponent out, String valuePart) {
      if (valuePart != null && !valuePart.isEmpty()) {
         int leading = 0;

         while (leading < valuePart.length() && Character.isWhitespace(valuePart.charAt(leading))) {
            leading++;
         }

         if (leading > 0) {
            this.appendColored(out, valuePart.substring(0, leading), 11184810);
         }

         String value = valuePart.substring(leading);
         int color = this.isNumericLiteral(value) ? 11436543 : (this.isBooleanLiteral(value) ? 16328306 : 15129460);
         this.appendColored(out, value, color);
      }
   }

   private void appendColored(MutableComponent out, String text, int color) {
      if (text != null && !text.isEmpty()) {
         out.append(autismclient.gui.f.b.k.init(text, this.styleTheme.init_Identifier_l(l.read), color));
      }
   }

   private int keyValueSeparatorIndex(String line) {
      if (line == null) {
         return -1;
      } else {
         int eq = line.indexOf(61);
         int colon = line.indexOf(58);
         if (eq < 0) {
            return colon;
         } else {
            return colon < 0 ? eq : Math.min(eq, colon);
         }
      }
   }

   private PayloadFieldCodec.FieldType parseFieldType(String token) {
      if (token != null && !token.isBlank()) {
         String normalized = token.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

         for (PayloadFieldCodec.FieldType type : PayloadFieldCodec.FieldType.values()) {
            if (type.name().equals(normalized) || type.label().equalsIgnoreCase(token.strip())) {
               return type;
            }
         }

         if ("UUID".equals(normalized)) {
            return PayloadFieldCodec.FieldType.UUID_FIELD;
         } else if ("UTF".equals(normalized) || "WRITEUTF".equals(normalized)) {
            return PayloadFieldCodec.FieldType.JAVA_WRITE_UTF;
         } else if (!"MC_STRING".equals(normalized) && !"MINECRAFT_STRING".equals(normalized)) {
            return !"COMPONENT".equals(normalized) && !"TEXT_COMPONENT".equals(normalized) ? null : PayloadFieldCodec.FieldType.TEXT_COMPONENT;
         } else {
            return PayloadFieldCodec.FieldType.MINECRAFT_STRING;
         }
      } else {
         return null;
      }
   }

   private boolean isHexDigit(char chr) {
      return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
   }

   private boolean isNumericLiteral(String value) {
      String text = value == null ? "" : value.strip();
      return text.matches("-?(0x[0-9a-fA-F]+|\\d+)(\\.\\d+)?");
   }

   private boolean isBooleanLiteral(String value) {
      String text = value == null ? "" : value.strip();
      return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
   }

   private void renderPayloadModeDropdown(int x, int y, int w, int mx, int my, PayloadFieldCodec.PayloadTemplate template) {
      int idx = this.payloadModeIndex(template == null ? PayloadFieldCodec.EncodingMode.ADVANCED_MIXED : template.encodingMode());
      autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
         .computeIfAbsent("payload_mode", key -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, w, 13, PAYLOAD_MODE_LABELS, idx, newIdx -> {
            if (newIdx >= 0 && newIdx < PAYLOAD_MODES.size()) {
               this.flushPendingPayloadEdit(true);
               this.payloadModeUserSelected = true;
               this.modeIndices.put("payload_mode", newIdx);
               if (this.payloadActionDraft != null) {
                  this.payloadActionDraft.payloadEncodingMode = PAYLOAD_MODES.get(newIdx).name();
               }

               if (this.payloadEditor != null) {
                  this.payloadEditor.sync = PAYLOAD_MODES.get(newIdx).name();
                  this.payloadEditor.bumpRevision();
                  this.cachedPayloadSnapshot = null;
                  this.pushPayloadContentToFields(false);
               }

               this.payloadContentDirty = true;
            }
         }));
      dd.setBounds(x, y, w, 13).setOptions(PAYLOAD_MODE_LABELS).setSelectedIndex(idx).setOnSelect(newIdx -> {
         if (newIdx >= 0 && newIdx < PAYLOAD_MODES.size()) {
            this.flushPendingPayloadEdit(true);
            this.payloadModeUserSelected = true;
            this.modeIndices.put("payload_mode", newIdx);
            if (this.payloadActionDraft != null) {
               this.payloadActionDraft.payloadEncodingMode = PAYLOAD_MODES.get(newIdx).name();
            }

            if (this.payloadEditor != null) {
               this.payloadEditor.sync = PAYLOAD_MODES.get(newIdx).name();
               this.payloadEditor.bumpRevision();
               this.cachedPayloadSnapshot = null;
               this.pushPayloadContentToFields(false);
            }

            this.payloadContentDirty = true;
         }
      });
      this.dropdowns.add(dd);
   }

   private int renderPayloadFieldRow(GuiGraphicsExtractor ctx, int x, int y, int w, int row, int mx, int my, float delta) {
      int btnH = 13;
      int typeW = this.dropdownWidth(PAYLOAD_FIELD_TYPE_LABELS, Math.min(116, Math.max(88, (w - 70) / 2)));
      int gap = 3;
      int actionW = 19;
      int actionsW = actionW * 3 + gap * 2;
      int valueX = x + typeW + gap;
      int valueW = Math.max(36, w - typeW - gap - actionsW - gap);
      this.renderFieldTypeDropdown(row, x, y + 2, typeW, mx, my);
      ThemedTextInputBox valueField = this.textFields.get(this.fieldValueKey(row));
      if (valueField != null) {
         PayloadFieldCodec.FieldType type = this.fieldTypeAt(row);
         valueField.setPlaceholder(Component.literal(type.defaultValue()));
         valueField.setX(valueX);
         valueField.setY(y + 3);
         valueField.setWidth(valueW);
         valueField.render(ctx, mx, my, delta);
      }

      int ax = valueX + valueW + gap;
      this.renderActionButton(
         ctx, ax, y + 2, actionW, btnH, "Up", OverlayActionButton.ButtonTone.SECONDARY, row > 0, mx, my, () -> this.moveField(row, -1)
      );
      this.renderActionButton(
         ctx, ax + actionW + gap, y + 2, actionW, btnH, "Dn", OverlayActionButton.ButtonTone.SECONDARY, row < this.payloadFieldCount - 1, mx, my, () -> this.moveField(row, 1)
      );
      this.renderActionButton(
         ctx, ax + (actionW + gap) * 2, y + 2, actionW, btnH, "X", OverlayActionButton.ButtonTone.DANGER, this.payloadFieldCount > 1, mx, my, () -> this.removeField(row)
      );
      return y + 18;
   }

   private void renderFieldTypeDropdown(int row, int x, int y, int w, int mx, int my) {
      String key = this.fieldTypeKey(row);
      int idx = Math.max(0, Math.min(this.modeIndices.getOrDefault(key, this.fieldTypeIndex(PayloadFieldCodec.FieldType.STRING_BYTES)), PAYLOAD_FIELD_TYPE_LABELS.size() - 1));
      autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
         .computeIfAbsent(key, cacheKey -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, w, 13, PAYLOAD_FIELD_TYPE_LABELS, idx, newIdx -> this.setFieldType(row, newIdx)));
      dd.setBounds(x, y, w, 13)
         .setOptions(PAYLOAD_FIELD_TYPE_LABELS)
         .setSelectedIndex(idx)
         .setOnSelect(newIdx -> this.setFieldType(row, newIdx));
      this.dropdowns.add(dd);
   }

   private void setFieldType(int row, int newIdx) {
      if (newIdx >= 0 && newIdx < PAYLOAD_FIELD_TYPES.size()) {
         String key = this.fieldTypeKey(row);
         int previous = this.modeIndices.getOrDefault(key, -1);
         this.modeIndices.put(key, newIdx);
         if (previous != newIdx) {
            ThemedTextInputBox valueField = this.textFields.get(this.fieldValueKey(row));
            if (valueField != null && valueField.getValue().isBlank()) {
               valueField.setValue(PAYLOAD_FIELD_TYPES.get(newIdx).defaultValue());
            }

            this.payloadContentDirty = true;
         }
      }
   }

   private void moveField(int row, int direction) {
      List<PayloadFieldCodec.PayloadField> fields = new ArrayList<>(this.payloadFields());
      int target = row + direction;
      if (row >= 0 && row < fields.size() && target >= 0 && target < fields.size()) {
         Collections.swap(fields, row, target);
         this.setPayloadFields(fields);
         this.payloadContentDirty = true;
         this.resizeEditorToContent();
      }
   }

   private void removeField(int row) {
      List<PayloadFieldCodec.PayloadField> fields = new ArrayList<>(this.payloadFields());
      if (fields.size() > 1 && row >= 0 && row < fields.size()) {
         fields.remove(row);
         this.setPayloadFields(fields);
         this.payloadContentDirty = true;
         this.resizeEditorToContent();
      }
   }

   private PayloadFieldCodec.PayloadTemplate currentPayloadTemplate() {
      if (this.payloadEditor != null) {
         return this.editorPayloadTemplate();
      }

      PayloadAction action = new PayloadAction();
      if (this.payloadActionDraft != null) {
         action.fromTag(this.payloadActionDraft.toTag());
      }

      ThemedTextInputBox channelField = this.textFields.get("payload_channel");
      if (channelField != null && !channelField.getValue().isBlank()) {
         action.channel = channelField.getValue().strip();
      }

      action.payloadPhase = "PLAY";
      action.payloadEncodingMode = this.selectedPayloadMode(action.channel).name();
      action.payloadFields = PayloadFieldCodec.formatFields(this.payloadFields());
      return PayloadFieldCodec.fromAction(action);
   }

   private ActionEditorOverlay.PayloadSnapshot buildPayloadSnapshot() {
      if (this.payloadEditor == null) {
         PayloadFieldCodec.PayloadTemplate template = this.currentPayloadTemplate();
         return new ActionEditorOverlay.PayloadSnapshot(-9223372036854775808L, template, template.encode());
      }

      long revision = this.payloadEditor.revision();
      ActionEditorOverlay.PayloadSnapshot hit = this.cachedPayloadSnapshot;
      if (hit != null && hit.revision() == revision) {
         return hit;
      }

      PayloadFieldCodec.PayloadTemplate template = this.editorPayloadTemplate();
      hit = new ActionEditorOverlay.PayloadSnapshot(revision, template, template.encode());
      this.cachedPayloadSnapshot = hit;
      return hit;
   }

   private PayloadFieldCodec.PayloadTemplate editorPayloadTemplate() {
      return this.payloadEditor == null ? PayloadFieldCodec.fromAction(new PayloadAction()) : this.payloadEditor.toSpec();
   }

   private void appendContentLine(String line) {
      ThemedTextInputBox contentField = this.textFields.get("payload_content");
      if (contentField != null && line != null && !line.isBlank()) {
         String current = contentField.getValue() == null ? "" : contentField.getValue().stripTrailing();
         contentField.setValue(current.isBlank() ? line : current + "\n" + line);
         this.payloadContentDirty = true;
      }
   }

   private void applyPayloadTemplate(PayloadFieldCodec.PayloadTemplate template) {
      if (template != null) {
         if (this.payloadActionDraft != null) {
            this.payloadActionDraft.channel = template.channel();
            this.payloadActionDraft.payloadDirection = template.direction().name();
            this.payloadActionDraft.payloadPhase = template.phase().name();
            this.payloadActionDraft.payloadEncodingMode = template.encodingMode().name();
            this.payloadActionDraft.payloadFields = PayloadFieldCodec.formatFields(template.fields());
         }

         this.suppressPayloadListeners = true;

         try {
            if (this.payloadEditor != null) {
               this.payloadEditor.channel = template.channel();
               this.payloadEditor.direction = template.direction().name();
               this.payloadEditor.phase = template.phase().name();
               this.payloadEditor.sync = template.encodingMode().name();
               this.payloadEditor.fields = new ArrayList<>(template.fields());
               this.payloadEditor.bumpRevision();
               this.payloadEditor.parseScript(this.payloadEditor.toScript());
               this.cachedPayloadSnapshot = null;
            }

            ThemedTextInputBox channelField = this.textFields.get("payload_channel");
            if (channelField != null) {
               channelField.setValue(template.channel());
            }

            if (this.payloadEditor == null) {
               this.setPayloadFields(template.fields());
            } else {
               this.pushPayloadEditorToFields(true);
            }
         } finally {
            this.suppressPayloadListeners = false;
         }

         this.put_Z = true;
         this.payloadContentDirty = true;
         this.resizeEditorToContent();
      }
   }

   private int renderPayloadStatus(GuiGraphicsExtractor ctx, int x, int y, int w) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      String status = this.payloadStatusText();
      String transport = this.payloadEditor == null
         ? "Encoder: vanilla"
         : this.payloadEditor.direction + " / " + this.payloadEditor.phase + (this.payloadEditor.packetId >= 0 ? " / id " + this.payloadEditor.packetId : " / id ?");
      autismclient.gui.f.b.k.init(
         ctx, this.font, autismclient.gui.f.b.k.init(this.font, status, Math.max(10, w), font, UiColorPalette.textBright()), font, UiColorPalette.textBright(), x, y + 2, false
      );
      y += 11;
      autismclient.gui.f.b.k.init(
         ctx, this.font, autismclient.gui.f.b.k.init(this.font, transport, Math.max(10, w), font, UiColorPalette.textSecondary()), font, UiColorPalette.textSecondary(), x, y + 2, false
      );
      return y + 13;
   }

   private String payloadStatusText() {
      if (this.payloadEditor != null) {
         String decoded = this.payloadEditor.close == null ? "payload" : this.payloadEditor.close;
         int bytes = this.payloadEditor.bodyBytes == null ? 0 : this.payloadEditor.bodyBytes.length;
         return !this.payloadContentDirty && !this.put_Z ? "Ready: " + bytes + "B " + decoded : "Edited: " + bytes + "B " + decoded;
      } else if (this.payloadContentDirty) {
         return "Edited: script rebuilds bytes";
      } else {
         return this.put_Z ? "Edited: channel changed" : "Payload builder ready";
      }
   }

   private String payloadValueLabel() {
      String var10000;
      switch (this.payloadTemplate) {
         case UTF8_TEXT:
            var10000 = "Payload Component";
            break;
         case BRAND_STRING:
            var10000 = "Brand";
            break;
         case COMMAND_INT:
            var10000 = "Payload Value";
            break;
         case BINARY_REPLAY:
            var10000 = "Payload";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String payloadRowsHint() {
      return "Rows run top to bottom.";
   }

   private boolean isConfigPhase() {
      return this.payloadEditor != null && this.payloadEditor.phase != null
         ? this.payloadEditor.phase.toLowerCase(Locale.ROOT).contains("config")
         : this.payloadActionDraft != null && this.payloadActionDraft.payloadPhase != null && this.payloadActionDraft.payloadPhase.toLowerCase(Locale.ROOT).contains("config");
   }

   private void resetPayloadEditor() {
      this.pendingPayloadEdit = null;
      this.payloadEditor = PayloadEditorModel.fromAction(this.payloadActionDraft);
      this.lastPayloadRevision = -9223372036854775808L;
      this.cachedPayloadSnapshot = null;
      ActionEditorOverlay.PayloadEditorState state = this.buildPayloadEditorState(this.payloadActionDraft);
      this.payloadTemplate = state.template();
      this.suppressPayloadListeners = true;

      try {
         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         if (channelField != null) {
            channelField.setValue(state.channel());
         }

         this.pushPayloadEditorToFields(true);
         this.put_Z = false;
         this.payloadContentDirty = false;
         this.set_Z = false;
         this.add_Z = false;
         this.payloadModeUserSelected = false;
         this.payloadAddTypeUserSelected = false;
      } finally {
         this.suppressPayloadListeners = false;
      }

      this.inferPayloadModeSelection();
   }

   private PayloadAction buildPayloadAction() {
      this.flushPendingPayloadEdit(true);
      if (this.payloadEditor != null) {
         if (this.payloadEditor.flush != null && !this.payloadEditor.flush.isBlank()) {
            throw new IllegalArgumentException(this.payloadEditor.flush);
         }

         PayloadAction action = this.payloadEditor.toAction(this.payloadActionDraft);
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = "minecraft:brand";
         }

         PayloadFieldCodec.EncodeResult built = this.payloadEditor.toSpec().encode();
         if (!built.isValid()) {
            throw new IllegalArgumentException(String.join("; ", built.errors()));
         }

         action.payloadData = AutismPayloadSupport.toHexString(built.bytes());
         return action;
      } else {
         PayloadAction action = new PayloadAction();
         if (this.payloadActionDraft != null) {
            action.fromTag(this.payloadActionDraft.toTag());
         }

         action.payloadClassName = "";
         action.commandApiOverride = false;
         action.javaSource = "";
         action.payloadScriptEnabled = false;
         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         if (channelField != null) {
            String editorChannel = channelField.getValue().strip();
            if (!editorChannel.isBlank()) {
               action.channel = editorChannel;
            }
         }

         action.payloadFields = PayloadFieldCodec.formatFields(this.payloadFields());
         if (action.payloadDirection == null || action.payloadDirection.isBlank()) {
            action.payloadDirection = "C2S";
         }

         action.payloadPhase = "PLAY";
         action.payloadEncodingMode = this.selectedPayloadMode(action.channel).name();
         PayloadFieldCodec.PayloadTemplate template = PayloadFieldCodec.fromAction(action);
         PayloadFieldCodec.EncodeResult built = template.encode();
         if (!built.isValid()) {
            throw new IllegalArgumentException(String.join("; ", built.errors()));
         }

         action.channel = template.channel();
         action.payloadDirection = template.direction().name();
         action.payloadPhase = "PLAY";
         action.payloadEncodingMode = template.encodingMode().name();
         action.payloadFields = PayloadFieldCodec.formatFields(template.fields());
         action.sourceDirection = action.payloadDirection;
         action.sourceProtocol = "";
         action.payloadData = AutismPayloadSupport.toHexString(built.bytes());
         action.payloadJson = "";
         action.commandApiRecognized = false;
         action.commandApiOverride = false;
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = this.payloadActionDraft != null && this.payloadActionDraft.channel != null && !this.payloadActionDraft.channel.isBlank() ? this.payloadActionDraft.channel : "minecraft:brand";
         }

         return action;
      }
   }

   private void renderPayloadActionEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      if (this.payloadActionDraft == null && this.editingAction == null) {
         Identifier font = this.styleTheme.init_Identifier_l(l.read);
         int cy = bodyTop + 4;
         autismclient.gui.f.b.k.init(ctx, this.font, "Custom Payload Editor", font, UiColorPalette.textBody(), x, cy + 2, false);
         cy += 13;
         autismclient.gui.f.b.k.init(ctx, this.font, "Packet JSON", font, UiColorPalette.textBody(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox jsonField = this.textFields.get("payload_json");
         if (jsonField != null) {
            jsonField.setX(x);
            jsonField.setY(cy);
            jsonField.setWidth(w);
            jsonField.setHeight(74);
            jsonField.render(ctx, mx, my, delta);
            cy += 78;
         }

         int[] jsonBtns = this.layoutButtons(x, w, false, "Pretty JSON", "Apply JSON");
         this.renderSecondaryButton(ctx, jsonBtns[0], cy, jsonBtns[1], 13, "Pretty JSON", mx, my, () -> {
            this.prettyPrintPayloadJson(false);
            this.resizeEditorToContent();
         });
         this.renderSecondaryButton(ctx, jsonBtns[2], cy, jsonBtns[3], 13, "Apply JSON", mx, my, () -> {
            this.applyPayloadJson(true);
            this.resizeEditorToContent();
         });
         cy += 18;
         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         if (channelField != null) {
            int lw = this.labelWidth(w, "Channel", font, 92);
            this.drawTrimmedText(ctx, "Channel", x, cy, lw, font);
            int ctrlX = this.controlX(x, lw);
            int ctrlW = this.valueFieldWidth(w, lw);
            channelField.setX(ctrlX);
            channelField.setY(cy + 2);
            channelField.setWidth(ctrlW);
            channelField.render(ctx, mx, my, delta);
            cy += 16;
         }

         if (this.toggleStates.getOrDefault("payload_command_api", false)) {
            ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
            if (commandField != null) {
               int lw = this.labelWidth(w, "CommandApi", font, 92);
               this.drawTrimmedText(ctx, "CommandApi", x, cy, lw, font);
               int ctrlX = this.controlX(x, lw);
               int ctrlW = this.valueFieldWidth(w, lw);
               int btnW = this.buttonWidth("Sync");
               int fieldW = Math.max(32, ctrlW - btnW - 2);
               commandField.setX(ctrlX);
               commandField.setY(cy + 2);
               commandField.setWidth(fieldW);
               commandField.render(ctx, mx, my, delta);
               this.renderActionButton(
                  ctx, ctrlX + fieldW + 2, cy + 2, btnW, 13, "Sync", OverlayActionButton.ButtonTone.GHOST, true, mx, my, () -> {
                     this.seedPayloadCommandInt();
                     this.resizeEditorToContent();
                  }
               );
               cy += 16;
            }
         }

         autismclient.gui.f.b.k.init(ctx, this.font, "UTF-8 Component", font, UiColorPalette.textBody(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox textField = this.textFields.get("payload_text");
         if (textField != null) {
            textField.setX(x);
            textField.setY(cy);
            textField.setWidth(w);
            textField.setHeight(30);
            textField.render(ctx, mx, my, delta);
            cy += 34;
         }

         int[] legacyBtns = this.layoutButtons(x, w, false, "Component -> Hex", "Hex -> Component");
         this.renderSecondaryButton(ctx, legacyBtns[0], cy, legacyBtns[1], 13, "Component -> Hex", mx, my, () -> {
            this.convertPayloadTextToHex();
            this.resizeEditorToContent();
         });
         this.renderSecondaryButton(ctx, legacyBtns[2], cy, legacyBtns[3], 13, "Hex -> Component", mx, my, () -> {
            this.convertPayloadHexToText();
            this.resizeEditorToContent();
         });
         cy += 18;
         autismclient.gui.f.b.k.init(ctx, this.font, "Raw Payload", font, UiColorPalette.textBody(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox rawField = this.textFields.get("payload_data");
         if (rawField != null) {
            rawField.setX(x);
            rawField.setY(cy);
            rawField.setWidth(w);
            rawField.setHeight(42);
            rawField.render(ctx, mx, my, delta);
            cy += 46;
         }

         int[] parseBtns = this.layoutButtons(x, w, false, "Parse Int", "Normalize");
         this.renderSecondaryButton(ctx, parseBtns[0], cy, parseBtns[1], 13, "Parse Int", mx, my, () -> {
            this.parsePayloadCommandInt();
            this.resizeEditorToContent();
         });
         this.renderSecondaryButton(ctx, parseBtns[2], cy, parseBtns[3], 13, "Normalize", mx, my, () -> {
            ThemedTextInputBox field = this.textFields.get("payload_data");
            if (field != null) {
               try {
                  field.setValue(AutismPayloadSupport.toHexString(AutismPayloadSupport.parseHexOrBase64(field.getValue())));
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.describeError(e));
               }
            }
         });
         cy += 18;
         String sendNowLbl = this.configurationOnly ? "Send: In Game" : "Send Now";
         int[] seedBtns = this.layoutButtons(x, w, false, "Seed From Int", sendNowLbl);
         this.renderSecondaryButton(ctx, seedBtns[0], cy, seedBtns[1], 13, "Seed From Int", mx, my, () -> {
            this.seedPayloadCommandInt();
            this.resizeEditorToContent();
         });
         this.renderActionButton(
            ctx, seedBtns[2], cy, seedBtns[3], 13, sendNowLbl, OverlayActionButton.ButtonTone.SUCCESS, !this.configurationOnly, mx, my, () -> {
               if (!this.configurationOnly) {
                  try {
                     PayloadAction action = this.load_H();
                     action.execute(MC);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cPayload send failed: " + AutismPayloadSupport.describeError(e));
                  }
               }
            }
         );
         cy += 20;
         autismclient.gui.f.b.k.init(ctx, this.font, "Runtime Java", font, UiColorPalette.textBody(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox javaField = this.textFields.get("payload_java");
         if (javaField != null) {
            javaField.setX(x);
            javaField.setY(cy);
            javaField.setWidth(w);
            javaField.setHeight(48);
            javaField.render(ctx, mx, my, delta);
            cy += 52;
         }

         this.renderHint(
            ctx, x, cy, w, "Raw replay is exact by default. Use Apply JSON only when you want the JSON view to rebuild the raw bytes."
         );
      } else {
         this.renderPayloadSnapshotEditor(ctx, x, bodyTop, w, mx, my, delta);
      }
   }

   private PayloadAction load_H() {
      if (this.payloadActionDraft == null && this.editingAction == null) {
         PayloadAction action = new PayloadAction();
         if (this.payloadActionDraft != null) {
            action.fromTag(this.payloadActionDraft.toTag());
         }

         action.payloadClassName = "";
         ThemedTextInputBox jsonField = this.textFields.get("payload_json");
         if (jsonField != null) {
            action.payloadJson = jsonField.getValue();
         }

         boolean jsonUsable = false;
         PayloadJsonCodec.EncodedPayload jsonEncoded = null;
         if (action.payloadJson != null && !action.payloadJson.isBlank()) {
            action.payloadJson = PayloadJsonCodec.prettyPrint(action.payloadJson);

            try {
               jsonEncoded = PayloadJsonCodec.encode(action);
               jsonUsable = true;
            } catch (Exception var13) {
            }
         }

         ThemedTextInputBox channelField = this.textFields.get("payload_channel");
         if (channelField != null) {
            String editorChannel = channelField.getValue().strip();
            if (!editorChannel.isBlank()) {
               action.channel = editorChannel;
            }
         }

         ThemedTextInputBox rawField = this.textFields.get("payload_data");
         if (rawField != null) {
            action.payloadData = rawField.getValue();
         }

         if (action.payloadData == null || action.payloadData.isBlank()) {
            ThemedTextInputBox textField = this.textFields.get("payload_text");
            if (textField != null && !textField.getValue().isBlank()) {
               action.payloadData = AutismPayloadSupport.toHexString(textField.getValue().getBytes(StandardCharsets.UTF_8));
            }
         }

         ThemedTextInputBox javaField = this.textFields.get("payload_java");
         if (javaField != null) {
            action.javaSource = javaField.getValue();
         }

         action.commandApiRecognized = this.toggleStates.getOrDefault("payload_command_api", false);
         ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
         if (commandField != null) {
            try {
               action.commandApiValue = Integer.parseInt(commandField.getValue().strip());
            } catch (NumberFormatException var12) {
            }
         }

         if (this.add_Z && jsonUsable && jsonEncoded != null && !this.set_Z && !this.put_Z) {
            try {
               jsonEncoded = PayloadJsonCodec.encode(action);
               action.channel = jsonEncoded.channel();
               action.payloadData = AutismPayloadSupport.toHexString(jsonEncoded.bytes());
            } catch (Exception var11) {
            }
         }

         if (action.commandApiRecognized) {
            try {
               byte[] bytes = AutismPayloadSupport.parseHexOrBase64(action.payloadData);
               action.payloadData = AutismPayloadSupport.toHexString(AutismPayloadSupport.withLeadingBigEndianInt(bytes, action.commandApiValue));
            } catch (Exception var10) {
            }
         }

         action.payloadClassName = "";
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = this.payloadActionDraft != null && this.payloadActionDraft.channel != null && !this.payloadActionDraft.channel.isBlank() ? this.payloadActionDraft.channel : "minecraft:brand";
         }

         return action;
      } else {
         return this.buildPayloadAction();
      }
   }

   private void parsePayloadCommandInt() {
      ThemedTextInputBox rawField = this.textFields.get("payload_data");
      ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
      ThemedTextInputBox channelField = this.textFields.get("payload_channel");
      if (rawField != null && commandField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.parseHexOrBase64(rawField.getValue());
            Integer value = AutismPayloadSupport.detectCommandApiValue(null, channelField == null ? "" : channelField.getValue(), bytes);
            if (value == null) {
               AutismClientMessaging.sendPrefixed("§cCould not parse a CommandApi integer from the current payload bytes.");
               return;
            }

            this.toggleStates.put("payload_command_api", true);
            commandField.setValue(String.valueOf(value));
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.describeError(e));
         }
      }
   }

   private void seedPayloadCommandInt() {
      ThemedTextInputBox rawField = this.textFields.get("payload_data");
      ThemedTextInputBox commandField = this.textFields.get("payload_command_value");
      if (rawField != null && commandField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.parseHexOrBase64(rawField.getValue());
            int value = Integer.parseInt(commandField.getValue().strip());
            this.toggleStates.put("payload_command_api", true);
            rawField.setValue(AutismPayloadSupport.toHexString(AutismPayloadSupport.withLeadingBigEndianInt(bytes, value)));
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cFailed to sync CommandApi value: " + AutismPayloadSupport.describeError(e));
         }
      }
   }

   private void convertPayloadHexToText() {
      ThemedTextInputBox rawField = this.textFields.get("payload_data");
      ThemedTextInputBox textField = this.textFields.get("payload_text");
      if (rawField != null && textField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.parseHexOrBase64(rawField.getValue());
            String text = AutismPayloadSupport.decodePrintableUtf8(bytes);
            if (text.isBlank()) {
               AutismClientMessaging.sendPrefixed("§cThe current payload bytes do not look like readable UTF-8 text.");
               return;
            }

            textField.setValue(text);
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.describeError(e));
         }
      }
   }

   private void convertPayloadTextToHex() {
      ThemedTextInputBox rawField = this.textFields.get("payload_data");
      ThemedTextInputBox textField = this.textFields.get("payload_text");
      if (rawField != null && textField != null) {
         rawField.setValue(AutismPayloadSupport.toHexString(textField.getValue().getBytes(StandardCharsets.UTF_8)));
      }
   }

   private void renderPresetButtons(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      int btnH = 13;
      int[] presetBtns = this.layoutButtons(x, w, false, "Default", "Module");
      this.renderActionButton(
         ctx, presetBtns[0], y + 2, presetBtns[1], btnH, "Default", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> {
            autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
            tmp.read();
            this.listFields.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
            this.listFields.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
         }
      );
      this.renderActionButton(
         ctx, presetBtns[2], y + 2, presetBtns[3], btnH, "Module", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, () -> {
            autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
            tmp.write();
            this.listFields.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
            this.listFields.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
         }
      );
   }

   private OverlayActionButton renderActionButton(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, OverlayActionButton.ButtonTone variant, boolean active, int mx, int my, Runnable action
   ) {
      return this.renderButton(ctx, x, y, w, h, label, variant, active, mx, my, action, null, null, null);
   }

   private OverlayActionButton renderButton(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      OverlayActionButton.ButtonTone variant,
      boolean active,
      int mx,
      int my,
      Runnable action,
      Runnable secondaryAction
   ) {
      return this.renderButton(ctx, x, y, w, h, label, variant, active, mx, my, action, secondaryAction, null, null);
   }

   private OverlayActionButton renderToggleButton(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean toggleState, String animationKey, int mx, int my, Runnable action
   ) {
      return this.renderButton(
         ctx,
         x,
         y,
         w,
         h,
         label,
         toggleState ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.DANGER,
         true,
         mx,
         my,
         action,
         null,
         toggleState,
         animationKey
      );
   }

   private OverlayActionButton renderButton(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      OverlayActionButton.ButtonTone variant,
      boolean active,
      int mx,
      int my,
      Runnable action,
      Runnable secondaryAction,
      Boolean toggleState,
      String animationKey
   ) {
      OverlayActionButton button = OverlayActionButton.create(
         x, y, w, h, Component.literal(label), ignored -> action.run(), secondaryAction == null ? null : ignored -> secondaryAction.run()
      );
      button.setTone(variant);
      if (toggleState != null) {
         button.setToggleState(toggleState).setAnimationKey(animationKey);
      }

      button.active = active;
      OverlayActionButton.render(ctx, this.font, button, mx, my);
      this.valid_List.add(new ActionEditorOverlay.ClickRegion(button, action));
      return button;
   }

   private static int buttonColor(OverlayActionButton.ButtonTone variant, boolean active) {
      if (!active) {
         return -2009060816;
      }

      int var10000;
      switch (variant == null ? OverlayActionButton.ButtonTone.SECONDARY : variant) {
         case SUCCESS:
            var10000 = ThemePaletteManager.recolor(-13248397, ThemePaletteManager.ColorRole.SUCCESS);
            break;
         case DANGER:
            var10000 = ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.DANGER);
            break;
         case PRIMARY:
         case FILTER_ON:
            var10000 = ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.BUTTON);
            break;
         case GHOST:
         case FILTER_OFF:
         case SECONDARY:
            var10000 = ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.OUTLINE);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private OverlayActionButton renderCaptureButton(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, CaptureMode mode, boolean capturing, boolean active, int mx, int my, Runnable action
   ) {
      return this.renderCaptureButton(ctx, x, y, w, h, mode, capturing, active, mx, my, null, null, action);
   }

   private OverlayActionButton renderCaptureButton(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      CaptureMode mode,
      boolean capturing,
      boolean active,
      int mx,
      int my,
      String idleLabel,
      String capturingLabel,
      Runnable action
   ) {
      if (!this.canCapture(mode)) {
         return null;
      }

      OverlayActionButton button = autismclient.gui.macro.editor.capture.CaptureButtonRenderer.init(
         ctx, this.font, autismclient.gui.f.NetHelper.init(x, y, w, h), mode, capturing, active, mx, my, idleLabel, capturingLabel, action
      );
      this.valid_List.add(new ActionEditorOverlay.ClickRegion(button, action));
      return button;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int x, int y, int size, int mx, int my, Runnable action) {
      boolean hovered = mx >= x && mx < x + size && my >= y && my < y + size;
      autismclient.gui.macro.editor.capture.EditorListRowHelper.init(ctx, autismclient.gui.f.NetHelper.init(x, y, size, size), hovered);
      this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, y, size, size, (mouseX, mouseY, mouseButton) -> {
         if (mouseButton != 0) {
            return false;
         }

         action.run();
         return true;
      }));
   }

   private void renderSecondaryButton(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, int mx, int my, Runnable action) {
      this.renderActionButton(ctx, x, y, w, h, label, OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, action);
   }

   private int renderHint(GuiGraphicsExtractor ctx, int x, int y, int w, String text) {
      return this.renderHintColored(ctx, x, y, w, text, UiColorPalette.textSecondary());
   }

   private int renderHintColored(GuiGraphicsExtractor ctx, int x, int y, int w, String text, int color) {
      String hint = this.condenseHint(text);
      if (hint.isEmpty()) {
         return y;
      }

      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      List<String> lines = this.wrapHint(hint, Math.max(10, w), font, color);
      int cy = y;

      for (String line : lines) {
         autismclient.gui.f.b.k.init(ctx, this.font, line, font, color, x, cy + 2, false);
         cy += 12;
      }

      return cy;
   }

   private List<String> wrapHint(String hint, int maxWidth, Identifier font, int color) {
      if (hint != null && !hint.isBlank()) {
         String safe = hint.trim().replaceAll("\\s+", " ");
         if (autismclient.gui.f.b.k.init(this.font, safe, font, color) <= maxWidth) {
            return List.of(safe);
         }

         String[] words = safe.split(" ");
         StringBuilder first = new StringBuilder();
         StringBuilder second = new StringBuilder();

         for (String word : words) {
            String candidate = first.length() == 0 ? word : first + " " + word;
            if (second.length() == 0 && autismclient.gui.f.b.k.init(this.font, candidate, font, color) <= maxWidth) {
               first.setLength(0);
               first.append(candidate);
            } else {
               if (second.length() > 0) {
                  second.append(' ');
               }

               second.append(word);
            }
         }

         if (second.length() != 0 && first.length() != 0) {
            String secondLine = autismclient.gui.f.b.k.init(this.font, second.toString(), font, color) <= maxWidth
               ? second.toString()
               : autismclient.gui.f.b.k.init(this.font, second.toString(), maxWidth, font, color);
            return List.of(first.toString(), secondLine);
         } else {
            return List.of(autismclient.gui.f.b.k.init(this.font, safe, maxWidth, font, color));
         }
      } else {
         return List.of();
      }
   }

   private String condenseHint(String text) {
      if (text == null) {
         return "";
      }

      String normalized = text.trim().replaceAll("\\s+", " ");
      if (normalized.isEmpty()) {
         return "";
      }

      if (normalized.startsWith("Config-phase payload")) {
         return "Config payload may disconnect.";
      }

      String var10000;
      switch (normalized) {
         case "Editing this row. 0 means drop the full stack from that slot or item.":
            var10000 = "Editing row. 0 = full stack.";
            break;
         case "No row selected. The controls below set defaults for new rows you add.":
            var10000 = "No row picked. Controls set new-row defaults.";
            break;
         case "Continue when any LAN peer reaches the target step.":
            var10000 = "Waits for any LAN peer step.";
            break;
         case "Add peers below to narrow it down. Until then, it waits for any peer at the fallback step.":
            var10000 = "Add peers below or use fallback.";
            break;
         case "Each peer below must reach its step before this continues.":
            var10000 = "All listed peers must reach their step.";
            break;
         case "No packets selected. This step will continue on the next packet in either direction.":
            var10000 = "No filters: next packet continues.";
            break;
         case "This step continues as soon as any selected C2S or S2C packet arrives.":
            var10000 = "Waits for any chosen packet.";
            break;
         case "Capture only marks the block. It will not open it.":
            var10000 = "Capture only marks the block.";
            break;
         case "0,0,0 uses the current container when open, otherwise the closest nearby one.":
            var10000 = "0,0,0 = current or nearest container.";
            break;
         case "Pick on an item adds it by name, from any slot on the screen. Ctrl+pick or an empty slot adds that exact slot.":
            var10000 = "Item = name (any slot). Ctrl/empty = exact slot.";
            break;
         case "Exact slots must be chest/custom GUI slots.":
            var10000 = "Exact slots: GUI side only.";
            break;
         case "Exact slots must be player inventory slots.":
            var10000 = "Exact slots: your inventory only.";
            break;
         case "No payment targets selected yet.":
            var10000 = "No players selected.";
            break;
         case "Click a scanned name to toggle it, or use All to add the filtered scan results.":
            var10000 = "Click names to toggle, or use All.";
            break;
         case "The search box also lets you manually add a player by pressing Enter.":
            var10000 = "Press Enter to add a typed player.";
            break;
         case "Pick modules below, then click the mode chip on each row.":
            var10000 = "Pick modules, then set each mode.";
            break;
         case "Each row runs with its own Toggle / Enable / Disable setting.":
            var10000 = "Each row keeps its own mode.";
            break;
         case "Capture View fills in your current yaw and pitch.":
            var10000 = "Capture View fills yaw and pitch.";
            break;
         case "Wait for Arrival continues only after navigation finishes.":
            var10000 = "Waits until navigation arrives.";
            break;
         case "Simple disconnect. Delay only controls how long to wait before closing the connection.":
            var10000 = "Simple disconnect after the delay.";
            break;
         case "Sends lag packets, then the selected kick packet. Packet Count controls how hard it pushes.":
            var10000 = "Sends lag, then the kick packet.";
            break;
         case "Kick Dupe will run the next eligible macro actions inside the lag sandwich, then kick.":
            var10000 = "Runs next actions inside the dupe kick.";
            break;
         case "Kick Dupe will try to use a bundle, then kick.":
            var10000 = "Tries bundle dupe, then kicks.";
            break;
         case "Automatically detects teleport/world change and disconnects at the perfect timing for dupe exploits.":
            var10000 = "Auto-disconnects at the perfect dupe timing.";
            break;
         case "Waits until the current GUI closes.":
            var10000 = "Waits for the GUI to close.";
            break;
         case "Waits until a GUI opens.":
            var10000 = "Waits for any GUI to open.";
            break;
         case "Blank pattern waits for any incoming server or plugin message.":
            var10000 = "Blank pattern = any server message.";
            break;
         case "Blank pattern waits for any incoming chat line.":
            var10000 = "Blank pattern = any chat line.";
            break;
         case "Regex is on, so the pattern must match with Java regex rules.":
            var10000 = "Regex mode uses Java regex rules.";
            break;
         case "No modules found right now.":
            var10000 = "No modules found.";
            break;
         case "Press Scan to load the current server players.":
            var10000 = "Press Scan for players.";
            break;
         case "Click a row to use that message.":
            var10000 = "Click a row to use it.";
            break;
         case "Pick target blocks, then choose the stop rule you want.":
            var10000 = "Pick blocks, then choose a stop rule.";
            break;
         default:
            var10000 = "";
      }

      String exact = var10000;
      if (!exact.isEmpty()) {
         return this.collapseWhitespace(exact);
      }

      normalized = normalized.replace("Waits until", "Waits for")
         .replace("Continue when", "Continues when")
         .replace("This step continues as soon as", "Continues when")
         .replace("the selected", "chosen")
         .replace("the current", "this")
         .replace("current ", "")
         .replace("number of times", "times")
         .replace("the chosen number of times", "times")
         .replace("the chosen ticks", "set ticks")
         .replace("only after", "after")
         .replace("It will not", "Won't")
         .replace("selected kick packet", "kick packet")
         .replace("Packet Count controls how hard it pushes", "Packet Count controls force")
         .replace("the open handler", "the open GUI")
         .replace("player inventory", "inventory")
         .replace("inventory slot", "slot")
         .replace("the matched item into", "it into")
         .replace("first matching visible slot", "first visible match")
         .replace("contains an item that matches the optional name", "matches the optional item")
         .replace("becomes empty", "turns empty")
         .replace("reaches the target count", "hits the target count")
         .replace("drops below the target count", "drops below the target")
         .replace("on the first content or count change in that exact slot", "on the first exact slot change")
         .replace("the block under your crosshair", "your target block")
         .replace("the entity under your crosshair", "your target entity")
         .replace("whatever is under your crosshair", "your target")
         .replace("will attack or break", "acts on")
         .replace("will interact with", "uses")
         .replace("the currently selected hotbar item", "the held item");
      return this.collapseWhitespace(normalized);
   }

   private void applyFieldPlaceholders() {
      if (this.fieldSchema != null) {
         for (FieldDef field : this.fieldSchema.fields()) {
            switch (field.type()) {
               case TEXT:
               case MACRO_SELECT:
               case NUMBER:
               case DECIMAL:
               case SLOT:
                  this.setFieldPlaceholder(field.key(), this.init_Str_FieldDef(field));
                  break;
               case BLOCK_POS:
                  this.setXyzPlaceholders(field.key());
            }
         }
      }

      this.setFieldPlaceholder("item_guiName", "Any GUI");
      this.setFieldPlaceholder("drop_guiName", "Any GUI");
      this.setFieldPlaceholder("lan_defaultStep", "Any step");
      this.setFieldPlaceholder("item_entrySlot", "Slot");
      this.setFieldPlaceholder("place_itemSlot", "Slot");
      this.setFieldPlaceholder("drop_entrySlot", "Slot #");
      this.setFieldPlaceholder("_craft_amount", "1");
      this.setFieldPlaceholder("_lan_step_add", "1");
      this.setFieldPlaceholder("drop_globalCount", "1");
      this.setFieldPlaceholder("amountInput", "1k");
      this.setInternalFieldPlaceholder("_item_add", "Any item");
      this.setInternalFieldPlaceholder("_drop_add", "Any item");
      this.setInternalFieldPlaceholder("_craft_search", "Find recipe");
      this.setInternalFieldPlaceholder("_lan_user_add", "Peer name");
      this.setInternalFieldPlaceholder("soundIds", "Find sound");
      this.setInternalFieldPlaceholder("entityIds", "Find entity");
      this.setInternalFieldPlaceholder("targetItems", "Item name");
      this.setInternalFieldPlaceholder("players", "Find player");
      this.setInternalFieldPlaceholder("_toggle_module_search", "Find module");
      this.setInternalFieldPlaceholder("_wait_chat_search", "Find chat");
   }

   private void setFieldPlaceholder(String key, String placeholder) {
      ThemedTextInputBox field = this.textFields.get(key);
      this.applyPlaceholder(field, placeholder);
   }

   private void setInternalFieldPlaceholder(String key, String placeholder) {
      ThemedTextInputBox field = this.internalTextFields.get(key);
      this.applyPlaceholder(field, placeholder);
   }

   private void setXyzPlaceholders(String key) {
      this.setFieldPlaceholder(key + "_0", "X");
      this.setFieldPlaceholder(key + "_1", "Y");
      this.setFieldPlaceholder(key + "_2", "Z");
   }

   private void applyPlaceholder(ThemedTextInputBox field, String placeholder) {
      if (field != null && placeholder != null && !placeholder.isBlank()) {
         field.setPlaceholder(Component.literal(this.shortenPlaceholder(placeholder)));
      }
   }

   private String init_Str_FieldDef(FieldDef field) {
      if (field == null) {
         return "";
      }

      if (this.editingAction instanceof ToolsAction && field.key().startsWith("toolArg.")) {
         String name = field.key().substring("toolArg.".length());
         autismclient.util.macro.MacroToolBridge.ToolInfo tool = autismclient.util.macro.MacroToolBridge.findTool(this.enumValue("toolId"));
         if (tool != null) {
            JsonObject definition = JsonParser.parseString(tool.read()).getAsJsonObject();
            if (definition.has("required")) {
               for (JsonElement required : definition.getAsJsonArray("required")) {
                  if (name.equals(required.getAsString())) {
                     return ToolsActionSchemaBuilder.init(name);
                  }
               }
            }
         }

         return "Default";
      } else {
         String var10000;
         switch (field.key()) {
            case "guiName":
            case "waitGuiName":
               var10000 = "Any GUI";
               break;
            case "listFilterText":
               var10000 = "text to match";
               break;
            case "listExcludeText":
               var10000 = "name1, name2";
               break;
            case "pattern":
               var10000 = "Any chat";
               break;
            case "message":
               var10000 = "Type message";
               break;
            case "description":
               var10000 = "Quick note";
               break;
            case "customText":
               var10000 = "Optional text";
               break;
            case "customFilePath":
               var10000 = "File in book-sources/";
               break;
            case "characters":
               var10000 = "1024";
               break;
            case "title":
               var10000 = this.editingAction != null && this.editingAction.getType() == MacroActionType.NBT_BOOK ? "Book title" : "Optional title";
               break;
            case "guiTitle":
               var10000 = "Optional title";
               break;
            case "customName":
               var10000 = "Optional name";
               break;
            case "commandTemplate":
               var10000 = "/pay <p> <amt>";
               break;
            case "amountInput":
               var10000 = "1k";
               break;
            case "itemName":
            case "fromItemName":
            case "toItemName":
               var10000 = "Any item";
               break;
            case "moduleName":
               var10000 = "Pick below";
               break;
            case "slot":
            case "fromSlot":
            case "toSlot":
            case "slotNumber":
            case "targetSlot":
               var10000 = "Slot #";
               break;
            case "healthThreshold":
               var10000 = "0-20";
               break;
            case "yaw":
            case "pitch":
               var10000 = "0";
               break;
            case "delayMs":
               var10000 = "0";
               break;
            case "delayTicks":
               var10000 = "0";
               break;
            case "clickCount":
            case "useCount":
            case "holdTicks":
            case "packetCount":
            case "maxDistance":
            case "stopSlotsUsed":
            case "slotsUsedThreshold":
            case "minedCountTarget":
            case "timeoutSeconds":
            case "durationTicks":
            case "tickOffset":
            case "preGenCount":
            case "revisionOffset":
            case "maxWaitMs":
            case "bufferMs":
               var10000 = "1";
               break;
            default:
               switch (field.type()) {
                  case TEXT:
                  case MACRO_SELECT:
                     var10000 = this.shortenPlaceholder(field.label());
                     break;
                  case NUMBER:
                  case DECIMAL:
                     var10000 = "0";
                     break;
                  case SLOT:
                     var10000 = "Slot #";
                     break;
                  default:
                     var10000 = "";
               }
         }

         return var10000;
      }
   }

   private String shortenPlaceholder(String text) {
      String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
      if (normalized.length() <= 16) {
         return normalized;
      }

      int cutoff = normalized.lastIndexOf(32, 13);
      if (cutoff < 5) {
         cutoff = 13;
      }

      return normalized.substring(0, Math.max(1, cutoff)).trim() + "...";
   }

   private String collapseWhitespace(String text) {
      return text == null ? "" : text.trim().replaceAll("\\s+", " ");
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      if (this.isWaitForGuiBeforeKey(field.key())) {
         this.renderGuiWaitRow(ctx, this.styleTheme.init_Identifier_l(l.read), field.key(), "waitForGuiAfter", x, y, w, mx, my);
      } else if ("safeClickDelayTicks".equals(field.key())) {
         this.renderSafeDelaySlider(ctx, x, y, w, mx, my);
      } else {
         switch (field.type()) {
            case TEXT:
            case MACRO_SELECT:
            case NUMBER:
            case DECIMAL:
            case SLOT:
               this.renderTextField(ctx, field, x, y, w, mx, my, delta);
               break;
            case BLOCK_POS:
               this.renderBlockPosField(ctx, field, x, y, w, mx, my, delta);
               break;
            case TOGGLE:
               this.renderBooleanField(ctx, field, x, y, w, mx, my);
               break;
            case ENUM:
               this.renderEnumField(ctx, field, x, y, w, mx, my);
               break;
            case STRING_LIST:
               if (this.isRaceStepsField(field)) {
                  this.renderStringListField(ctx, field, x, y, w, mx, my);
               } else if (field.captureMode() == CaptureMode.BLOCK_CATALOG) {
                  this.renderBlockCatalogField(ctx, field, x, y, w, mx, my, delta);
               } else {
                  this.renderListField(ctx, field, x, y, w, mx, my, delta);
               }
               break;
            case TARGET_SUMMARY:
               this.renderTargetSummaryField(ctx, field, x, y, w);
               break;
            case CAPTURE_BUTTON:
               this.renderPacketClickTargetField(ctx, field, x, y, w, mx, my);
               break;
            case CONDITION:
               this.renderFieldLabel(ctx, field, x, y, w);
         }
      }
   }

   private void renderFieldLabel(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int lw = this.labelWidth(w, field.label(), font, 56);
      this.drawTrimmedText(ctx, field.label(), x, y, lw, font);
      String summary = this.flush_Str(field.key());
      int valX = this.controlX(x, lw);
      int valW = this.valueFieldWidth(w, lw);
      int color = UiColorPalette.textBright();
      String trimmed = autismclient.gui.f.b.k.init(this.font, summary, font, color) > valW
         ? autismclient.gui.f.b.k.init(this.font, summary, Math.max(8, valW - 4), font, color)
         : summary;
      autismclient.gui.f.b.k.init(ctx, this.font, trimmed, font, color, valX, y + 2, false);
   }

   private String flush_Str(String conditionKey) {
      CompoundTag stored = this.actionTag != null && this.actionTag.get(conditionKey) instanceof CompoundTag c ? c : null;
      boolean hasFlatEditor = this.fieldByKey("cond_kind") != null;
      boolean flatEditable = hasFlatEditor;
      if (hasFlatEditor && stored != null) {
         try {
            flatEditable = MacroCondition.fromTag(stored).asLeaf() != null;
         } catch (Throwable ignored) {
            flatEditable = true;
         }
      }

      if (flatEditable && hasFlatEditor) {
         CompoundTag flat = new CompoundTag();
         flat.putString("cond_kind", this.enumValue("cond_kind"));
         flat.putBoolean("cond_negate", this.toggleStates.getOrDefault("cond_negate", false));
         flat.putString("cond_item", this.fieldText("cond_item"));
         flat.putString("cond_op", this.enumValue("cond_op"));
         flat.putString("cond_value", this.fieldText("cond_value"));
         flat.putString("cond_cmp", this.enumValue("cond_cmp"));
         flat.putInt("cond_amount", this.fieldInt("cond_amount", 0));
         flat.putInt("cond_slot", this.fieldInt("cond_slot", 0));
         flat.putString("cond_toolScope", this.enumValue("cond_toolScope"));
         flat.putString("cond_toolState", this.enumValue("cond_toolState"));
         flat.putString("cond_toolFilter", this.enumValue("cond_toolFilter"));

         try {
            return MacroCondition.readFlat(flat, "cond_").describe();
         } catch (Throwable var9) {
         }
      }

      if (stored != null) {
         try {
            return MacroCondition.fromTag(stored).describe();
         } catch (Throwable var8) {
         }
      }

      return "always";
   }

   private String fieldText(String key) {
      ThemedTextInputBox f = this.textFields.get(key);
      return f == null ? "" : f.getValue();
   }

   private int fieldInt(String key, int fallback) {
      ThemedTextInputBox f = this.textFields.get(key);
      if (f == null) {
         return fallback;
      }

      try {
         return Integer.parseInt(f.getValue().strip());
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private void renderPlaceItemPicker(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      ThemedTextInputBox nameF = this.textFields.get("itemName");
      ThemedTextInputBox slotF = this.textFields.get("place_itemSlot");
      if (nameF != null && slotF != null) {
         int pickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
         int slotW = 26;
         int gap = 3;
         int pickX = x + w - pickW;
         int slotX = pickX - gap - slotW;
         int nameW = Math.max(1, slotX - x - 2);
         nameF.setX(x);
         nameF.setY(y + 1);
         nameF.setWidth(nameW);
         nameF.render(ctx, mx, my, delta);
         slotF.setX(slotX);
         slotF.setY(y + 1);
         slotF.setWidth(slotW);
         slotF.render(ctx, mx, my, delta);
         boolean capt = this.captureSession.init("itemName");
         this.renderCaptureButton(ctx, pickX, y, pickW, 14, CaptureMode.ITEM_SLOT, capt, true, mx, my, () -> this.startCapture("itemName"));
      }
   }

   private void renderTargetSummaryField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int lw = this.labelWidth(w, field.label(), font, 56);
      this.drawTrimmedText(ctx, field.label(), x, y, lw, font);
      String summary = this.packetClickTargetText();
      int valX = this.controlX(x, lw);
      int valW = this.valueFieldWidth(w, lw);
      int color = summary != null && !summary.isEmpty() ? UiColorPalette.textBright() : UiColorPalette.textBody();
      String display = summary != null && !summary.isEmpty() ? summary : "Not captured";
      String trimmed = autismclient.gui.f.b.k.init(this.font, display, font, color) > valW
         ? autismclient.gui.f.b.k.init(this.font, display, Math.max(8, valW - 4), font, color)
         : display;
      autismclient.gui.f.b.k.init(ctx, this.font, trimmed, font, color, valX, y + 2, false);
   }

   private void renderPacketClickTargetField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int btnW = this.captureButtonWidth(CaptureMode.PACKET_CLICK_TARGET, "Re-capture", "Click slot...");
      int btnH = 13;
      int lw = this.labelWidth(w, field.label(), font, btnW);
      this.drawTrimmedText(ctx, field.label(), x, y, lw, font);
      boolean capturing = field.key().equals(this.capturingFieldKey);
      int btnX = x + w - btnW;
      this.renderCaptureButton(
         ctx, btnX, y + 2, btnW, btnH, CaptureMode.PACKET_CLICK_TARGET, capturing, true, mx, my, "Re-capture", "Click slot...", () -> this.startPacketClickTargetCapture(field.key())
      );
   }

   private String packetClickTargetText() {
      if (this.editingAction instanceof PacketClickAction pca) {
         return pca.target == null ? null : pca.target.withMode(this.selectedClickMode()).init_Str();
      } else {
         return null;
      }
   }

   private boolean isRaceStepsField(FieldDef field) {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.RACE && field != null && "raceSteps".equals(field.key());
   }

   private void renderStringListField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      List<String> values = this.listFields.computeIfAbsent(key, ignored -> new ArrayList<>());
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, field.label() + " (" + values.size() + ")", font, UiColorPalette.textBody(), x, y + 2, false);
      int addY = y + 13;
      int[] addBtns = this.layoutButtons(x, w, false, "+ Condition", "+ Action");
      this.renderActionButton(
         ctx, addBtns[0], addY, addBtns[1], 13, "+ Condition", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, () -> this.openRaceStepSelector(values, true)
      );
      this.renderActionButton(
         ctx, addBtns[2], addY, addBtns[3], 13, "+ Action", OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> this.openRaceStepSelector(values, false)
      );
      int listY = addY + 13 + 2;
      int rowH = 14;
      int visibleRows = 4;
      ScrollableListView viewport = this.viewportFor(this.entryViewports, key + "_race", x, listY, w, visibleRows * rowH, rowH, 5);
      viewport.setContentHeight(values.size() * rowH);
      viewport.renderScrollbar(ctx, mx, my);
      if (values.isEmpty()) {
         String hint = "Use +Cond for waits or +Act for batch actions.";
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, hint, x, listY, w - 5 - 1);
      } else {
         int buttonW = 12;
         int rowGap = 2;
         int buttonArea = buttonW * 3 + rowGap * 2;
         int rowTextW = Math.max(20, w - 5 - buttonArea - 7);
         viewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int first = viewport.getFirstVisibleRow();

         for (int vi = first; vi < values.size() && vi <= viewport.getLastVisibleRow(); vi++) {
            int rowY = viewport.getRowY(vi);
            if (rowY != -2147483648) {
               String value = values.get(vi);
               boolean hovered = mx >= x && mx < x + rowTextW && my >= rowY && my < rowY + rowH;
               this.renderRaceStepRow(ctx, x, rowY, rowTextW, rowH, vi, value, hovered);
               int bx = x + rowTextW + 3;
               int rowIndex = vi;
               this.renderIconButton(
                  ctx, bx, rowY + (rowH - buttonW) / 2, buttonW, mx, my, "^", false, rowIndex > 0, () -> Collections.swap(values, rowIndex, rowIndex - 1)
               );
               bx += buttonW + rowGap;
               this.renderIconButton(
                  ctx,
                  bx,
                  rowY + (rowH - buttonW) / 2,
                  buttonW,
                  mx,
                  my,
                  "v",
                  false,
                  rowIndex + 1 < values.size(),
                  () -> Collections.swap(values, rowIndex, rowIndex + 1)
               );
               bx += buttonW + rowGap;
               boolean canDelete = this.countRaceActionSteps(values) > 1 || !this.isRaceActionStep(value);
               this.renderIconButton(ctx, bx, rowY + (rowH - buttonW) / 2, buttonW, mx, my, "X", true, canDelete, () -> {
                  if (rowIndex >= 0 && rowIndex < values.size() && (this.countRaceActionSteps(values) > 1 || !this.isRaceActionStep(values.get(rowIndex)))) {
                     values.remove(rowIndex);
                  }
               });
            }
         }

         viewport.endFrame(ctx);
      }
   }

   private void renderRaceStepRow(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int index, String step, boolean hovered) {
      String canonical = RaceAction.canonicalStep(step);
      boolean condition = RaceAction.isConditionStep(canonical);
      int bgColor = hovered ? UiColorPalette.rowHover() : UiColorPalette.rowSurface();
      PanelSurfaceHelper.fillRect(ctx, x, y + 1, w, h - 2, bgColor);
      int badgeColor = condition ? -30720 : -12268476;
      PanelSurfaceHelper.drawAccentBar(ctx, x + 1, y + 3, 3, h - 6, badgeColor);
      int numberColor = UiColorPalette.textSecondary();
      autismclient.gui.f.b.k.init(
         ctx,
         this.font,
         String.valueOf(index + 1),
         this.styleTheme.init_Identifier_l(l.read),
         numberColor,
         x + 8,
         UiLayoutMath.centerTextY(y, h, this.styleTheme.init_I_l(l.read), this.styleTheme.flush()),
         false
      );
      String role = condition ? "COND" : "ACT";
      int roleColor = condition ? UiColorPalette.cyan() : UiColorPalette.textBody();
      int roleX = x + 24;
      autismclient.gui.f.b.k.init(
         ctx,
         this.font,
         role,
         this.styleTheme.init_Identifier_l(l.load),
         roleColor,
         roleX,
         UiLayoutMath.centerTextY(y, h, this.styleTheme.init_I_l(l.load), this.styleTheme.flush()),
         false
      );
      String label = this.raceStepLabel(step).replaceFirst("^(COND|ACT)\\s+", "");
      int labelX = roleX + 34;
      String trimmed = autismclient.gui.f.b.k.init(this.font, label, Math.max(1, x + w - labelX - 4), this.styleTheme.init_Identifier_l(l.read), -1);
      autismclient.gui.f.b.k.init(
         ctx,
         this.font,
         trimmed,
         this.styleTheme.init_Identifier_l(l.read),
         -1,
         labelX,
         UiLayoutMath.centerTextY(y, h, this.styleTheme.init_I_l(l.read), this.styleTheme.flush()),
         false
      );
   }

   private void renderIconButton(
      GuiGraphicsExtractor ctx, int x, int y, int size, int mx, int my, String symbol, boolean danger, boolean active, Runnable action
   ) {
      boolean hovered = active && mx >= x && mx < x + size && my >= y && my < y + size;
      autismclient.gui.macro.editor.capture.EditorListRowHelper.init(ctx, autismclient.gui.f.NetHelper.init(x, y, size, size), symbol, hovered, danger);
      this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, y, size, size, (mouseX, mouseY, mouseButton) -> {
         if (active && mouseButton == 0) {
            action.run();
            return true;
         } else {
            return false;
         }
      }));
   }

   private void openRaceStepSelector(List<String> values, boolean condition) {
      ArrayList<String> actions = new ArrayList<>(ACTION_TYPE_OPTIONS);
      if (!AutismLiteVariant.enabled()) {
         actions.add("Tools / TOOLS");
      }

      List<RaceStepSelectorOverlay.Inner_DataUtil> options = condition
         ? this.raceStepOptions(CONDITION_TYPE_OPTIONS, "CONDITION")
         : this.raceStepOptions(actions, "ACTION");
      this.raceStepSelector.openSelector(condition ? "Condition Selector" : "Action Selector", options, option -> {
         if (option != null && option.load() != null && !option.load().isBlank()) {
            values.add((condition ? "CONDITION:" : "ACTION:") + option.load());
            this.resizeEditorToContent();
         }
      });
   }

   private List<RaceStepSelectorOverlay.Inner_DataUtil> raceStepOptions(List<String> choices, String role) {
      ArrayList<RaceStepSelectorOverlay.Inner_DataUtil> out = new ArrayList<>();

      for (String choice : choices) {
         String[] parts = choice == null ? new String[0] : choice.split("/", 2);
         String category = parts.length > 1 ? parts[0].trim() : role;
         String id = this.choiceName(choice);
         if (!id.isBlank()) {
            String label = id.replace('_', ' ');
            out.add(new RaceStepSelectorOverlay.Inner_DataUtil(category, id, label, role));
         }
      }

      return out;
   }

   private String raceStepLabel(String step) {
      String canonical = RaceAction.canonicalStep(step);
      String type = RaceAction.stepType(canonical);
      return (RaceAction.isConditionStep(canonical) ? "COND  " : "ACT   ") + type;
   }

   private boolean isRaceActionStep(String step) {
      return RaceAction.isActionStep(RaceAction.canonicalStep(step));
   }

   private int countRaceActionSteps(List<String> values) {
      int count = 0;
      if (values != null) {
         for (String value : values) {
            if (this.isRaceActionStep(value)) {
               count++;
            }
         }
      }

      return count;
   }

   private String choiceName(String choice) {
      if (choice == null) {
         return "";
      }

      int slash = choice.lastIndexOf(47);
      return (slash >= 0 ? choice.substring(slash + 1) : choice).trim();
   }

   private void startPacketClickTargetCapture(String fieldKey) {
      if (this.editingAction instanceof PacketClickAction) {
         this.capturingFieldKey = fieldKey;
         ContainerCloseGuard.init(target -> this.applyPacketClickTarget(target));
         this.hideOverlaysForCapture();
      }
   }

   private void applyPacketClickTarget(ContainerClickSnapshot.Inner_StreamUtil target) {
      if (target != null) {
         if (this.editingAction instanceof PacketClickAction pca) {
            ContainerClickSnapshot.Inner_DataUtil mode = this.selectedClickMode();
            pca.mode = mode.name();
            pca.init(target.withMode(mode));
            if (this.actionTag != null) {
               this.actionTag.putString("mode", mode.name());
               this.actionTag.put("target", target.withMode(mode).toTag());
            }
         }

         this.capturingFieldKey = null;
         this.finishCapture(false, false);
      }
   }

   private ContainerClickSnapshot.Inner_DataUtil selectedClickMode() {
      FieldDef modeField = this.schemaField("mode");
      if (modeField != null && modeField.type() == FieldType.ENUM && !modeField.enumOptions().isEmpty()) {
         int idx = Math.max(0, Math.min(this.modeIndices.getOrDefault("mode", 0), modeField.enumOptions().size() - 1));
         return ContainerClickSnapshot.Inner_DataUtil.init(modeField.enumOptions().get(idx));
      } else {
         return ContainerClickSnapshot.Inner_DataUtil.init(this.actionTag == null ? "" : this.actionTag.getStringOr("mode", ""));
      }
   }

   private FieldDef schemaField(String key) {
      if (this.fieldSchema != null && key != null) {
         for (FieldDef field : this.fieldSchema.fields()) {
            if (key.equals(field.key())) {
               return field;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean isCapturingPacketClickTarget() {
      return this.capturingFieldKey != null && ContainerCloseGuard.isCapturing();
   }

   public boolean cancelPacketClickTargetCapture() {
      if (this.capturingFieldKey == null) {
         return false;
      }

      ContainerCloseGuard.read();
      this.capturingFieldKey = null;
      this.finishCapture(false, false);
      return true;
   }

   private boolean isWaitForGuiBeforeKey(String key) {
      return "waitForGuiBefore".equals(key);
   }

   private boolean isWaitForGuiAfterKey(String key) {
      return "waitForGuiAfter".equals(key);
   }

   private void renderBooleanField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      boolean val = this.toggleStates.getOrDefault(key, false);
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int btnW = 34;
      int btnH = 13;
      String label = this.dynamicFieldLabel(field);
      int lw = this.labelWidth(w, label, font, btnW);
      this.drawTrimmedText(ctx, label, x, y, lw, font);
      int btnX = x + w - btnW;
      this.renderToggleButton(ctx, btnX, y + 2, btnW, btnH, val ? "ON" : "OFF", val, "macro-field:" + key, mx, my, () -> {
         boolean nowOn = !this.toggleStates.getOrDefault(key, false);
         this.toggleStates.put(key, nowOn);
         if ("waitForTrigger".equals(key)) {
            this.waitForTriggerTouched = true;
         }

         if (this.actionTag != null) {
            this.actionTag.putBoolean(key, nowOn);
         }

         if (nowOn && field.hasMutualExclusion()) {
            for (String other : field.mutuallyExclusiveWith()) {
               this.toggleStates.put(other, false);
               if (this.actionTag != null) {
                  this.actionTag.putBoolean(other, false);
               }
            }
         }
      });
   }

   private void renderTextField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      ThemedTextInputBox tf = this.textFields.get(key);
      if (tf != null) {
         Identifier font = this.styleTheme.init_Identifier_l(l.read);
         if (field.type() == FieldType.MACRO_SELECT) {
            this.renderMacroSelectField(ctx, field, tf, x, y, w, mx, my);
         } else if (this.isWaitChatPatternField(field)) {
            autismclient.gui.f.b.k.init(ctx, this.font, field.label(), font, UiColorPalette.textBody(), x, y + 2, false);
            tf.setX(x);
            tf.setY(y + 13);
            int variableW = this.hasVariableDropdown(field.key()) ? 24 : 0;
            tf.setWidth(Math.max(1, w - (variableW > 0 ? variableW + 2 : 0)));
            tf.setHeight(40);
            tf.render(ctx, mx, my, delta);
            if (variableW > 0) {
               this.renderFieldVariableDropdown(field, tf, x + w - variableW, y + 13, variableW);
            }
         } else {
            boolean packetPick = field.captureMode() == CaptureMode.PACKET_NAME;
            boolean slotPick = field.type() == FieldType.SLOT && this.isCaptureAvailable();
            boolean itemSlotPick = field.captureMode() == CaptureMode.ITEM_SLOT && this.isCaptureAvailable();
            boolean menuButtonPick = field.captureMode() == CaptureMode.CUSTOM_MENU_BUTTON;
            boolean nbtBookFilePick = this.editingAction != null
               && this.editingAction.getType() == MacroActionType.NBT_BOOK
               && "customFilePath".equals(key);
            int lw = this.labelWidth(w, field.label(), font, !itemSlotPick && !packetPick && !slotPick && !menuButtonPick ? 56 : 88);
            this.drawTrimmedText(ctx, field.label(), x, y, lw, font);
            int tfX = this.controlX(x, lw);
            int variableW = this.hasVariableDropdown(key) ? 24 : 0;
            if (menuButtonPick) {
               int pickW = this.captureButtonWidth(CaptureMode.CUSTOM_MENU_BUTTON, null, null);
               int availableW = Math.max(1, this.valueFieldWidth(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = Math.max(1, availableW - pickW - 2);
               tf.setX(tfX);
               tf.setY(y + 2);
               tf.setWidth(tfW);
               tf.render(ctx, mx, my, delta);
               String fKey = key;
               this.renderCaptureButton(ctx, tfX + tfW + 2, y + 2, pickW, 13, CaptureMode.CUSTOM_MENU_BUTTON, false, true, mx, my, () -> this.pickCustomMenuButton(fKey));
               if (variableW > 0) {
                  this.renderFieldVariableDropdown(field, tf, tfX + tfW + pickW + 4, y + 2, variableW);
               }
            } else if (nbtBookFilePick) {
               int pickW = this.buttonWidth("Pick");
               int availableW = Math.max(1, this.valueFieldWidth(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = Math.max(1, availableW - pickW - 2);
               tf.setX(tfX);
               tf.setY(y + 2);
               tf.setWidth(tfW);
               tf.render(ctx, mx, my, delta);
               this.renderActionButton(
                  ctx, tfX + tfW + 2, y + 2, pickW, 13, "Pick", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> this.chooseBookTextFile(tf)
               );
               if (variableW > 0) {
                  this.renderFieldVariableDropdown(field, tf, tfX + tfW + pickW + 4, y + 2, variableW);
               }
            } else if (!itemSlotPick && !packetPick && !slotPick) {
               int tfW = Math.max(1, this.valueFieldWidth(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               tf.setX(tfX);
               tf.setY(y + 2);
               tf.setWidth(tfW);
               tf.render(ctx, mx, my, delta);
               if (variableW > 0) {
                  this.renderFieldVariableDropdown(field, tf, tfX + tfW + 2, y + 2, variableW);
               }
            } else {
               int pickW = this.captureButtonWidth(packetPick ? CaptureMode.PACKET_NAME : CaptureMode.ITEM_SLOT, null, null);
               int availableW = Math.max(1, this.valueFieldWidth(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = slotPick ? Math.max(26, availableW - pickW - 2) : Math.max(1, availableW - pickW - 2);
               tf.setX(tfX);
               tf.setY(y + 2);
               tf.setWidth(tfW);
               tf.render(ctx, mx, my, delta);
               int pkX = tfX + tfW + 2;
               boolean capt = !packetPick && this.captureSession.init(key);
               String fKey = key;
               this.renderCaptureButton(
                  ctx, pkX, y + 2, pickW, 14, packetPick ? CaptureMode.PACKET_NAME : CaptureMode.ITEM_SLOT, capt, true, mx, my, () -> {
                     if (packetPick) {
                        this.openPacketPickerForField(fKey);
                     } else {
                        this.startCapture(fKey);
                     }
                  }
               );
               if (variableW > 0) {
                  this.renderFieldVariableDropdown(field, tf, pkX + pickW + 2, y + 2, variableW);
               }
            }
         }
      }
   }

   private void pickCustomMenuButton(String key) {
      CustomMenuSnapshot menu = autismclient.util.custommenu.CustomMenuTracker.snapshot();
      if (menu == null) {
         bi.read("No custom screen is open to pick a button from.");
      } else {
         List<CustomMenuButton> buttons = new ArrayList<>();

         for (CustomMenuButton button : menu.buttons()) {
            if (button.serverRelevant() && !button.label().isBlank()) {
               buttons.add(button);
            }
         }

         if (buttons.isEmpty()) {
            bi.read("This screen has no pressable buttons.");
         } else {
            ThemedTextInputBox tf = this.textFields.get(key);
            String current = tf == null ? "" : tf.getValue().trim();
            int next = 0;

            for (int i = 0; i < buttons.size(); i++) {
               if (buttons.get(i).label().equalsIgnoreCase(current)) {
                  next = (i + 1) % buttons.size();
                  break;
               }
            }

            String label = buttons.get(next).label();
            if (tf != null) {
               tf.setValue(label);
            }

            bi.init("Button set to \"" + label + "\"" + (buttons.size() > 1 ? " (Pick again to cycle)" : ""), -11013497);
         }
      }
   }

   private void renderFieldVariableDropdown(FieldDef field, ThemedTextInputBox textField, int x, int y, int width) {
      if (field != null) {
         this.renderVariableDropdown(field.key(), textField, x, y, width);
      }
   }

   private void renderVariableDropdown(String key, ThemedTextInputBox textField, int x, int y, int width) {
      if (this.hasVariableDropdown(key) && textField != null) {
         String cacheKey = "__vars_" + key;
         autismclient.gui.ui.widgets.DropdownWidget dropdown = this.dropdownCache
            .computeIfAbsent(cacheKey, ignored -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, width, 13, this.insertTokenOptions, 0, index -> {}));
         dropdown.setBounds(x, y, width, 13).setOptions(this.insertTokenOptions).setLabelOverride("{}").setOnSelect(index -> {
            if (index >= 0 && index < this.insertTokenOptions.size()) {
               textField.insertText(this.insertTokenOptions.get(index));
            }

            dropdown.close();
         });
         this.dropdowns.add(dropdown);
      }
   }

   private boolean hasVariableDropdown(String key) {
      return this.editingAction != null
         && this.editingAction.getType() == MacroActionType.SEND_CHAT
         && "message".equals(key)
         && !this.insertTokenOptions.isEmpty();
   }

   private void rebuildInsertTokenOptions() {
      LinkedHashSet<String> tokens = new LinkedHashSet<>();
      if (this.macro != null && this.macro.actions != null) {
         int limit = this.actionIndex < 0 ? this.macro.actions.size() : Math.min(this.actionIndex, this.macro.actions.size());

         for (int i = limit - 1; i >= 0 && tokens.size() < 4; i--) {
            collectCaptureTokens(tokens, this.macro.actions.get(i), 4);
         }
      }

      tokens.addAll(BUILTIN_VARIABLE_TOKENS);
      this.insertTokenOptions.clear();
      this.insertTokenOptions.addAll(tokens);
   }

   private static void collectCaptureTokens(Set<String> tokens, MacroAction action, int limit) {
      if (action instanceof autismclient.util.macro.CaptureValueAction capture && tokens.size() < limit) {
         for (String name : PatternCaptureMatcher.init(capture.matchMode, capture.pattern)) {
            if (tokens.size() >= limit) {
               return;
            }

            tokens.add("{" + name + "}");
         }

         if (tokens.size() < limit) {
            String root = MacroVariableStore.load(capture.saveAs);
            if (!root.isBlank()) {
               tokens.add("{" + root + "}");
            }
         }
      }
   }

   private static void collectActionOutputTokens(Set<String> tokens, MacroAction action) {
      if (action instanceof WaitForChatAction chat) {
         for (String name : PatternCaptureMatcher.init(chat.effectiveMatchMode(), chat.pattern)) {
            tokens.add("{" + name + "}");
         }

         addVariableTokens(tokens, chat.saveAs, "sender", "message", "display", "source");
      } else if (action instanceof WaitForGuiAction gui) {
         for (String name : PatternCaptureMatcher.init(gui.matchMode, gui.guiTitle)) {
            tokens.add("{" + name + "}");
         }

         addVariableTokens(tokens, gui.saveAs, "title", "type");
      } else if (action instanceof autismclient.util.macro.CaptureValueAction capture) {
         for (String name : PatternCaptureMatcher.init(capture.matchMode, capture.pattern)) {
            tokens.add("{" + name + "}");
         }

         switch (capture.source) {
            case GUI_TITLE:
               addVariableTokens(tokens, capture.saveAs, "title", "type");
               break;
            case RECENT_CHAT:
               addVariableTokens(tokens, capture.saveAs, "sender", "message", "display", "source");
               break;
            case SCOREBOARD:
               addVariableTokens(tokens, capture.saveAs, "line", "name", "score", "row", "title", "objective");
               break;
            case GUI_ITEM:
            case PLAYER_ITEM:
            case CURSOR_ITEM:
            case HELD_ITEM:
               addVariableTokens(tokens, capture.saveAs, "name", "id", "count", "slot", "lore", "lore1");
         }
      } else if (action instanceof SelectSlotAction select && select.outputVariable != null && !select.outputVariable.isBlank()) {
         tokens.add("{" + select.outputVariable.trim() + "}");
      } else if (action instanceof autismclient.util.macro.MacroVariablesAction variables) {
         for (String name : variables.names) {
            if (name != null && !name.isBlank()) {
               tokens.add("{" + name.trim() + "}");
            }
         }
      } else if (action instanceof CaptureOutputAction output) {
         String root = output.load();
         if (action instanceof WaitForHealthAction) {
            addVariableTokens(tokens, root, "health", "max", "absorption");
         } else if (action instanceof WaitForEntityAction) {
            addVariableTokens(tokens, root, "name", "id", "uuid", "x", "y", "z", "distance");
         } else if (action instanceof WaitForSoundAction) {
            addVariableTokens(tokens, root, "id", "x", "y", "z");
         } else if (action instanceof WaitForPacketAction || action instanceof autismclient.util.macro.WaitPacketMatchAction) {
            addVariableTokens(tokens, root, "name", "class", "direction");
            if (action instanceof autismclient.util.macro.WaitPacketMatchAction packetMatch) {
               String cleanRoot = MacroVariableStore.load(root);
               if (!cleanRoot.isBlank()) {
                  for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : packetMatch.effectiveRules()) {
                     if (rule.load != null && !rule.load.isBlank()) {
                        tokens.add("{" + cleanRoot + "." + rule.load.trim().toLowerCase(Locale.ROOT) + "}");
                     }
                  }
               }
            }
         } else if (action instanceof WaitGuiTypeAction gui) {
            for (String name : PatternCaptureMatcher.init(gui.matchMode, gui.title)) {
               tokens.add("{" + name + "}");
            }

            addVariableTokens(tokens, root, "title", "type");
         } else if (action instanceof WaitPosAction || action instanceof WaitForPositionDeltaAction || action instanceof WaitForTeleportAction) {
            addVariableTokens(tokens, root, "x", "y", "z", "yaw", "pitch");
         } else if (action instanceof WaitForWorldChangeAction) {
            addVariableTokens(tokens, root, "id", "name");
         } else if (action instanceof WaitGamemodeChangeAction) {
            addVariableTokens(tokens, root, "name");
         } else {
            addVariableTokens(tokens, root, "name", "id", "count", "slot", "condition");
         }
      }
   }

   private static void addVariableTokens(Set<String> tokens, String root, String... properties) {
      if (root != null && !root.isBlank()) {
         String clean = MacroVariableStore.load(root);
         if (!clean.isBlank()) {
            tokens.add("{" + clean + "}");

            for (String property : properties) {
               tokens.add("{" + clean + "." + property + "}");
            }
         }
      }
   }

   private void renderMacroSelectField(GuiGraphicsExtractor ctx, FieldDef field, ThemedTextInputBox backingField, int x, int y, int w, int mx, int my) {
      backingField.setX(-10000);
      backingField.setY(-10000);
      List<MacroFolderLayout.Inner_DataUtil> macros = new ArrayList<>(autismclient.util.MacroLibraryManager.getInstance().getFolderEntries(""));
      if (backingField.getValue() != null && !backingField.getValue().isBlank() && autismclient.util.MacroLibraryManager.getInstance().findByName(backingField.getValue()) == null) {
         macros.add(0, new MacroFolderLayout.Inner_DataUtil("", new AutismMacro(backingField.getValue()), 0, false));
      }

      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      String current = backingField.getValue() == null ? "" : backingField.getValue();
      autismclient.gui.f.b.k.init(ctx, this.font, field.label(), font, UiColorPalette.textBody(), x, y + 2, false);
      int badgeW = Math.min(150, Math.max(60, w / 2));
      int badgeX = x + w - badgeW;
      PanelSurfaceHelper.drawInputPill(ctx, badgeX, y + 1, badgeW, 14);
      String selectedLabel = current.isBlank() ? "No macro selected" : current;
      String selectedTrimmed = autismclient.gui.f.b.k.init(this.font, selectedLabel, badgeW - 6, font, -1);
      autismclient.gui.f.b.k.init(ctx, this.font, selectedTrimmed, font, current.isBlank() ? UiColorPalette.textMuted() : UiColorPalette.textBright(), badgeX + 3, y + 3, false);
      int listY = y + 17;
      int listH = 70;
      int itemW = w - 5 - 1;
      String viewportKey = "macro_select_" + field.key();
      ScrollableListView viewport = this.viewportFor(this.entryViewports, viewportKey, x, listY, w, listH, 14, 5);
      viewport.setContentHeight(macros.size() * 14);
      viewport.renderScrollbar(ctx, mx, my);
      if (macros.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, listY, itemW, 13, false, false);
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No saved macros", x, listY, itemW);
      } else {
         viewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int first = viewport.getFirstVisibleRow();

         for (int i = first; i < macros.size() && i <= viewport.getLastVisibleRow(); i++) {
            MacroFolderLayout.Inner_DataUtil folderRow = macros.get(i);
            String macroName = folderRow.init_Str();
            int iy = viewport.getRowY(i);
            if (iy != -2147483648) {
               if (folderRow.init_Z()) {
                  autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, iy, itemW, 14);
                  autismclient.gui.ui.widgets.GroupHeaderRenderer.load(
                     UiHelper.init(ctx, this.font, mx, my), bounds, folderRow.init_Str(), folderRow.isFolder(), bounds.init_Z(mx, my)
                  );
                  int hitTop = Math.max(iy, listY);
                  int hitBottom = Math.min(iy + 14, listY + listH);
                  if (hitBottom > hitTop) {
                     this.valid_List
                        .add(
                           new ActionEditorOverlay.ClickRegion(
                              x, hitTop, itemW, hitBottom - hitTop, () -> autismclient.util.MacroLibraryManager.getInstance().toggleFolderCollapsed(folderRow.folderName())
                           )
                        );
                  }
               } else {
                  boolean selected = macroName.equals(current);
                  boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + 14;
                  autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                     ctx,
                     this.font,
                     Component.literal(macroName),
                     autismclient.gui.f.NetHelper.init(x, iy, itemW, 14),
                     hovered,
                     selected,
                     ListRowRenderer.RowTone.NORMAL,
                     false
                  );
                  String selectedMacro = macroName;
                  this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, itemW, 14, () -> backingField.setValue(selectedMacro)));
               }
            }
         }

         viewport.endFrame(ctx);
      }
   }

   private boolean isWaitChatPatternField(FieldDef field) {
      return field != null && "pattern".equals(field.key()) && this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_CHAT;
   }

   private void renderEnumField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      List<String> opts = field.enumOptions();
      if (!opts.isEmpty()) {
         int idx = Math.min(this.modeIndices.getOrDefault(key, 0), opts.size() - 1);
         Identifier font = this.styleTheme.init_Identifier_l(l.read);
         int lw = this.labelWidth(w, field.label(), font, 72);
         this.drawTrimmedText(ctx, field.label(), x, y, lw, font);
         int ctrlX = this.controlX(x, lw);
         int ctrlW = this.valueFieldWidth(w, lw);
         int ddH = 13;
         int variableW = this.hasVariableDropdown(key) ? 20 : 0;
         int valueW = Math.max(1, ctrlW - (variableW > 0 ? variableW + 2 : 0));
         ThemedTextInputBox bindingField = field.dynamic() ? this.textFields.get(key) : null;
         if (bindingField != null && !bindingField.getValue().isBlank()) {
            bindingField.setX(ctrlX);
            bindingField.setY(y + 2);
            bindingField.setWidth(valueW);
            bindingField.render(ctx, mx, my, 0.0F);
            if (variableW > 0) {
               this.renderEnumVariableDropdown(field, bindingField, ctrlX + valueW + 2, y + 2, variableW);
            }
         } else {
            boolean toolsLabels = this.editingAction instanceof ToolsAction || key.startsWith("cond_tool");
            List<String> displayOpts = toolsLabels ? opts.stream().map(value -> ToolsActionSchemaBuilder.init(key, value)).toList() : opts;
            int ddW = this.dropdownWidth(displayOpts, valueW);
            autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
               .computeIfAbsent(
                  key, k -> new autismclient.gui.ui.widgets.DropdownWidget(ctrlX, y + 2, ddW, ddH, displayOpts, idx, newIdx -> this.selectEnumOption(field, newIdx))
               );
            dd.setBounds(ctrlX, y + 2, ddW, ddH)
               .setOptions(displayOpts)
               .setSelectedIndex(idx)
               .setOnSelect(newIdx -> this.selectEnumOption(field, newIdx));
            this.dropdowns.add(dd);
            if (variableW > 0 && bindingField != null) {
               this.renderEnumVariableDropdown(field, bindingField, ctrlX + ddW + 2, y + 2, variableW);
            }
         }
      }
   }

   private void renderEnumVariableDropdown(FieldDef field, ThemedTextInputBox bindingField, int x, int y, int width) {
      List<String> options = new ArrayList<>();
      options.add("Static");
      options.addAll(this.insertTokenOptions);
      String cacheKey = "__enum_vars_" + field.key();
      autismclient.gui.ui.widgets.DropdownWidget dropdown = this.dropdownCache
         .computeIfAbsent(cacheKey, ignored -> new autismclient.gui.ui.widgets.DropdownWidget(x, y, width, 13, options, 0, index -> {}));
      dropdown.setBounds(x, y, width, 13).setOptions(options).setLabelOverride("{}").setOnSelect(index -> {
         if (index <= 0) {
            bindingField.setValue("");
         } else if (index < options.size()) {
            bindingField.setValue(options.get(index));
         }

         dropdown.close();
      });
      this.dropdowns.add(dropdown);
   }

   private void renderBlockPosField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      String[] axis = new String[]{"X", "Y", "Z"};
      boolean hasCapture = field.captureMode() != CaptureMode.NONE && this.canCapture(field.captureMode());
      int capBtnW = this.captureButtonWidth(field.captureMode(), null, null);
      int capBtnH = 13;
      int labelW = hasCapture ? w - capBtnW - 4 : w;
      autismclient.gui.f.b.k.init(ctx, this.font, field.label(), font, UiColorPalette.textBody(), x, y + 3, false);
      if (this.editingAction instanceof ToolsAction && "position".equals(key) && !"OFFSET".equals(this.enumValue("destination"))) {
         int currentW = autismclient.gui.f.b.k.init(this.font, "Use current", font, UiColorPalette.textBright()) + 12;
         this.renderActionButton(
            ctx, x + w - currentW, y, currentW, 13, "Use current", OverlayActionButton.ButtonTone.SECONDARY, MC.player != null, mx, my, () -> {
               if (MC.player != null) {
                  BlockPos position = MC.player.blockPosition();
                  int[] values = new int[]{position.getX(), position.getY(), position.getZ()};

                  for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
                     ThemedTextInputBox input = this.textFields.get(key + "_" + axisIndex);
                     if (input != null) {
                        input.setValue(Integer.toString(values[axisIndex]));
                     }
                  }
               }
            }
         );
      }

      if (hasCapture) {
         int cbX = x + labelW + 4;
         this.renderCaptureButton(ctx, cbX, y, capBtnW, capBtnH, field.captureMode(), false, true, mx, my, () -> this.startFieldCapture(field));
      }

      int fieldW = (w - 4) / 3;

      for (int i = 0; i < 3; i++) {
         ThemedTextInputBox tf = this.textFields.get(key + "_" + i);
         if (tf != null) {
            int fx = x + i * (fieldW + 2);
            int variableW = this.hasVariableDropdown(field.key()) ? 18 : 0;
            autismclient.gui.f.b.k.init(ctx, this.font, axis[i], font, UiColorPalette.textSecondary(), fx, y + 14 + 3, false);
            tf.setX(fx + 9);
            tf.setY(y + 14 + 1);
            tf.setWidth(Math.max(1, fieldW - 9 - (variableW > 0 ? variableW + 2 : 0)));
            tf.render(ctx, mx, my, delta);
            if (variableW > 0) {
               this.renderVariableDropdown(field.key() + "_" + axis[i].toLowerCase(Locale.ROOT), tf, fx + fieldW - variableW, y + 14 + 2, variableW);
            }
         }
      }
   }

   private void chooseBookTextFile(ThemedTextInputBox field) {
      if (field != null) {
         String current = field.getValue() == null ? "" : field.getValue().trim();
         String selected = TinyFileDialogs.tinyfd_openFileDialog("NBT Book Text File", current.isBlank() ? null : current, null, null, false);
         if (selected != null && !selected.isBlank()) {
            field.setValue(selected);
         }
      }
   }

   private void renderListField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      List<String> lst = this.listFields.getOrDefault(key, Collections.emptyList());
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      ThemedTextInputBox af = this.internalTextFields.get(key);
      boolean editable = this.isTargetListKey(key);
      boolean xcarryList = this.isXCarryEntriesKey(key);
      int editIdx = editable ? this.listEditIndices.getOrDefault(key, -1) : -1;
      if (editIdx >= lst.size()) {
         editIdx = -1;
         this.listEditIndices.put(key, -1);
      }

      String filter = !editable && af != null ? af.getValue().toLowerCase() : "";
      String emptyHint = this.auditTargetsError(key);
      List<Integer> filtered = autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
         lst, this.filteredIndexCache.computeIfAbsent(key, ignored -> new ArrayList<>()), value -> filter.isEmpty() || value.toLowerCase().contains(filter)
      );
      String sectionLabel = filter.isEmpty() ? field.label() + " (" + lst.size() + ")" : field.label() + " (" + filtered.size() + "/" + lst.size() + ")";
      autismclient.gui.f.b.k.init(ctx, this.font, sectionLabel, font, UiColorPalette.textBody(), x, y + 2, false);
      boolean hasCapture = field.captureMode() != CaptureMode.NONE && this.canCapture(field.captureMode());
      boolean hasSlotField = editable && this.isItemEntryListKey(key);
      int btnH = 13;
      int addY = y + 13;
      autismclient.gui.macro.editor.capture.EditorListRowHelper.RowLayout toolbar = autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
         autismclient.gui.f.NetHelper.init(x, addY, w, 13), hasSlotField, hasCapture
      );
      int slotW = toolbar.load() == null ? 0 : toolbar.load().update();
      ThemedTextInputBox slotField = hasSlotField ? this.textFields.get(key + "_slot") : null;
      if (hasSlotField && slotField == null) {
         slotField = this.newTextField(slotW);
         slotField.setIntegerOnlyFilter(true);
         slotField.setPlaceholder(Component.literal("Slot#"));
         this.textFields.put(key + "_slot", slotField);
      }

      if (af != null) {
         af.setX(toolbar.init().read());
         af.setY(toolbar.init().write());
         af.setWidth(toolbar.init().update());
         af.render(ctx, mx, my, delta);
      }

      if (hasSlotField && slotField != null) {
         slotField.setX(toolbar.load().read());
         slotField.setY(toolbar.load().write());
         slotField.setWidth(toolbar.load().update());
         slotField.render(ctx, mx, my, delta);
      }

      if (editable && editIdx >= 0 && editIdx < lst.size() && af != null) {
         String nameText = af.getValue().strip();
         String slotText = hasSlotField && slotField != null ? slotField.getValue().strip() : "";
         String entry = write_Str_Str_Str(nameText, slotText);
         if (!entry.isEmpty()) {
            String normalized = this.isXCarryEntriesKey(key) ? this.normalizeItemEntry(entry) : (this.isItemTargetsKey(key) ? StoreItemAction.normalizeTarget(entry) : entry);
            if (normalized != null && !normalized.isBlank() && !normalized.equals(lst.get(editIdx))) {
               lst.set(editIdx, normalized);
               if (this.isItemEntryListKey(key)) {
                  this.itemTargetLists.put(key, this.readItemTargetList(key));
               }
            }
         }
      }

      ThemedTextInputBox fSlotField = slotField;
      if (af != null) {
         this.renderActionButton(
            ctx, toolbar.read().read(), toolbar.read().write(), toolbar.read().update(), btnH, "+Add", OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
               String nameText = af.getValue().strip();
               String slotText = hasSlotField && fSlotField != null ? fSlotField.getValue().strip() : "";
               String entry = write_Str_Str_Str(nameText, slotText);
               if (!entry.isEmpty() && this.addListEntry(field, lst, entry)) {
                  af.setValue("");
                  if (fSlotField != null) {
                     fSlotField.setValue("");
                  }

                  this.listEditIndices.put(key, -1);
               }
            }
         );
      }

      if (hasCapture) {
         boolean isItemSlot = field.captureMode() == CaptureMode.ITEM_SLOT;
         boolean capturing = isItemSlot && this.captureSession.init(key);
         String fKey = key;
         FieldDef fField = field;
         List<String> fLst = lst;
         this.renderCaptureButton(
            ctx, toolbar.write().read(), toolbar.write().write(), toolbar.write().update(), btnH, field.captureMode(), capturing, true, mx, my, () -> {
               if (isItemSlot) {
                  this.startCapture(fKey);
               } else {
                  this.init_V_FieldDef_List(fField, fLst);
               }
            }
         );
      }

      int itemsY = addY + btnH + 2;
      int selAreaH = 56;
      int delW = 13;
      int destW = xcarryList ? this.dropdownWidth(XCarryAction.destinationLabels(), 62) : 0;
      int amountW = xcarryList ? Math.max(this.buttonWidth("FullStk"), this.buttonWidth("x64")) : 0;
      if (xcarryList) {
         this.resizeXCarryDestinations(lst.size());
      }

      if (xcarryList) {
         this.resizeXCarryAmountModes(lst.size());
         if (editIdx >= 0 && editIdx < lst.size()) {
            int selectedXCarryIndex = editIdx;
            ThemedTextInputBox countField = this.textFields.get(key + "_xcarry_count");
            if (countField == null) {
               countField = this.newTextField(38);
               countField.setIntegerOnlyFilter(true);
               countField.setPlaceholder(Component.literal("Count"));
               this.textFields.put(key + "_xcarry_count", countField);
            }

            int amountY = itemsY;
            autismclient.gui.f.b.k.init(ctx, this.font, "Amount", this.styleTheme.init_Identifier_l(l.read), UiColorPalette.textBody(), x, amountY + 3, false);
            int modeX = x + 52;
            boolean custom = this.entryAmountMode(editIdx) == XCarryAction.AmountMode.CUSTOM;
            int amountBtnW = Math.max(this.buttonWidth("Count"), this.buttonWidth("FullStk"));
            this.renderActionButton(
               ctx, modeX, amountY, amountBtnW, btnH, custom ? "Count" : "FullStk", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> {
                  boolean nowCustom = this.entryAmountMode(selectedXCarryIndex) == XCarryAction.AmountMode.CUSTOM;
                  this.setEntryAmountMode(selectedXCarryIndex, nowCustom ? XCarryAction.AmountMode.FULL_STK : XCarryAction.AmountMode.CUSTOM);
                  ThemedTextInputBox cf = this.textFields.get(key + "_xcarry_count");
                  if (cf != null) {
                     cf.setValue(String.valueOf(this.entryAmount(selectedXCarryIndex)));
                  }
               }
            );
            if (custom) {
               countField.setX(modeX + amountBtnW + 4);
               countField.setY(amountY);
               countField.setWidth(42);
               if (countField.getValue().isBlank()) {
                  countField.setValue(String.valueOf(this.entryAmount(editIdx)));
               }

               countField.render(ctx, mx, my, delta);

               try {
                  int parsed = Integer.parseInt(countField.getValue().strip());
                  this.setEntryAmount(selectedXCarryIndex, Math.max(1, parsed));
               } catch (NumberFormatException var55) {
               }
            }

            itemsY = amountY + btnH + 2;
         }
      }

      ScrollableListView strViewport = this.viewportFor(this.entryViewports, key, x, itemsY, w, selAreaH, 14, 5);
      strViewport.setContentHeight(filtered.size() * 14);
      strViewport.renderScrollbar(ctx, mx, my);
      if (!filtered.isEmpty()) {
         int rowAreaW = Math.max(24, w - 5 - 1 - delW - 2 - (destW > 0 ? destW + 2 : 0) - (amountW > 0 ? amountW + 2 : 0));
         strViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstVi = strViewport.getFirstVisibleRow();

         for (int vi = firstVi; vi < filtered.size() && vi <= strViewport.getLastVisibleRow(); vi++) {
            int ri = filtered.get(vi);
            int iy = strViewport.getRowY(vi);
            if (iy != -2147483648) {
               Component displayValue = this.styledListEntry(key, lst.get(ri), ri);
               boolean selected = editable && ri == editIdx;
               boolean hovered = mx >= x && mx < x + rowAreaW && my >= iy && my < iy + 14;
               if (field.captureMode() == CaptureMode.PACKET_NAME) {
                  int textColor = this.listEntryColor(lst.get(ri), ri, selected);
                  autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                     ctx, this.font, displayValue, autismclient.gui.f.NetHelper.init(x, iy, rowAreaW, 14), hovered, selected, textColor, this.isItemEntryListKey(key)
                  );
               } else {
                  autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                     ctx,
                     this.font,
                     displayValue,
                     autismclient.gui.f.NetHelper.init(x, iy, rowAreaW, 14),
                     hovered,
                     selected,
                     ListRowRenderer.RowTone.NORMAL,
                     this.isItemEntryListKey(key)
                  );
               }

               if (editable) {
                  int fri = ri;
                  ThemedTextInputBox fSlotF2 = hasSlotField ? this.textFields.get(key + "_slot") : null;
                  this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, rowAreaW, 14, () -> {
                     int curIdx = this.listEditIndices.getOrDefault(key, -1);
                     if (curIdx == fri) {
                        this.listEditIndices.put(key, -1);
                        if (af != null) {
                           af.setValue("");
                        }

                        if (fSlotF2 != null) {
                           fSlotF2.setValue("");
                        }

                        ThemedTextInputBox countField = this.textFields.get(key + "_xcarry_count");
                        if (countField != null) {
                           countField.setValue("");
                        }
                     } else {
                        this.listEditIndices.put(key, fri);
                        String raw = lst.get(fri);
                        if (af != null) {
                           af.setValue(this.entryName(raw));
                        }

                        if (fSlotF2 != null) {
                           MacroItemTarget parsed = MacroItemTarget.parse(raw);
                           fSlotF2.setValue(parsed.hasSlot() ? String.valueOf(parsed.slot) : "");
                        }

                        ThemedTextInputBox countField = this.textFields.get(key + "_xcarry_count");
                        if (countField != null) {
                           countField.setValue(String.valueOf(this.entryAmount(fri)));
                        }
                     }
                  }));
               }

               int actionX = x + rowAreaW + 2;
               if (xcarryList) {
                  int fDestIndex = ri;
                  int dest = this.entryDestination(fDestIndex);
                  int[] destValues = XCarryAction.destinationValues();
                  List<String> destOptions = XCarryAction.destinationLabels();
                  int selectedDestIndex = XCarryAction.destinationIndex(dest);
                  String ddKey = key + "_xcarry_dest_" + fDestIndex;
                  int ddX = actionX;
                  int ddY = iy + 1;
                  autismclient.gui.ui.widgets.DropdownWidget dd = this.dropdownCache
                     .computeIfAbsent(ddKey, k -> new autismclient.gui.ui.widgets.DropdownWidget(ddX, ddY, destW, 13, destOptions, selectedDestIndex, newIdx -> {
                        if (newIdx >= 0 && newIdx < destValues.length) {
                           this.setEntryDestination(fDestIndex, destValues[newIdx]);
                        }
                     }));
                  dd.setBounds(ddX, ddY, destW, 13)
                     .setOptions(destOptions)
                     .setSelectedIndex(selectedDestIndex)
                     .setOnSelect(newIdx -> {
                        if (newIdx >= 0 && newIdx < destValues.length) {
                           this.setEntryDestination(fDestIndex, destValues[newIdx]);
                        }
                     });
                  this.dropdowns.add(dd);
                  actionX += destW + 2;
                  int fAmountIndex = ri;
                  boolean custom = this.entryAmountMode(fAmountIndex) == XCarryAction.AmountMode.CUSTOM;
                  String amountLabel = custom ? "x" + this.entryAmount(fAmountIndex) : "FullStk";
                  this.renderActionButton(
                     ctx,
                     actionX,
                     iy + 1,
                     amountW,
                     13,
                     amountLabel,
                     custom ? OverlayActionButton.ButtonTone.SUCCESS : OverlayActionButton.ButtonTone.SECONDARY,
                     true,
                     mx,
                     my,
                     () -> {
                        boolean nowCustom = this.entryAmountMode(fAmountIndex) == XCarryAction.AmountMode.CUSTOM;
                        this.setEntryAmountMode(fAmountIndex, nowCustom ? XCarryAction.AmountMode.FULL_STK : XCarryAction.AmountMode.CUSTOM);
                        this.listEditIndices.put(key, fAmountIndex);
                        ThemedTextInputBox cf = this.textFields.get(key + "_xcarry_count");
                        if (cf != null) {
                           cf.setValue(String.valueOf(this.entryAmount(fAmountIndex)));
                        }
                     }
                  );
                  actionX += amountW + 2;
               }

               int delX = actionX;
               int fRi = ri;
               this.init_V_GuiGraphicsExtract_2(ctx, delX, iy + 1, delW, mx, my, () -> {
                  lst.remove(fRi);
                  if (xcarryList) {
                     this.removeXCarryDestination(fRi);
                     this.removeXCarryAmountEntry(fRi);
                  }

                  if (editable && this.listEditIndices.getOrDefault(key, -1) == fRi) {
                     this.listEditIndices.put(key, -1);
                     if (af != null) {
                        af.setValue("");
                     }
                  } else if (editable && this.listEditIndices.getOrDefault(key, -1) > fRi) {
                     this.listEditIndices.put(key, this.listEditIndices.get(key) - 1);
                  }

                  ScrollableListView vp = this.entryViewports.get(key);
                  if (vp != null) {
                     vp.scrollByRows(-1);
                  }

                  if (this.isItemEntryListKey(key)) {
                     this.itemTargetLists.put(key, this.readItemTargetList(key));
                  }
               });
            }
         }

         strViewport.endFrame(ctx);
      } else if (emptyHint != null && !emptyHint.isBlank()) {
         int itemW = w - 5 - 1;
         PanelSurfaceHelper.drawCardRow(ctx, x, itemsY, itemW, 12, false, false);
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, emptyHint, x, itemsY, itemW);
      }
   }

   private String auditTargetsError(String key) {
      if (this.isAuditTargetsKey(key)) {
         List<String> selected = this.listFields.getOrDefault(key, Collections.emptyList());
         if (selected.isEmpty()) {
            return "Add at least one target before running this audit.";
         }
      }

      return null;
   }

   private void renderBlockCatalogField(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      List<String> sel = this.listFields.getOrDefault(key, Collections.emptyList());
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      ThemedTextInputBox af = this.internalTextFields.get(key);
      String filter = af != null ? af.getValue().toLowerCase() : "";
      int cy = y;
      autismclient.gui.f.b.k.init(ctx, this.font, field.label() + " (" + sel.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 13;
      int delW = 13;
      int selAreaH = 56;
      ScrollableListView catSelViewport = this.viewportFor(this.entryViewports, key, x, cy, w, selAreaH, 14, 5);
      catSelViewport.setContentHeight(sel.size() * 14);
      catSelViewport.renderScrollbar(ctx, mx, my);
      if (!sel.isEmpty()) {
         int delX = x + w - 5 - 2 - delW;
         catSelViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstSel = catSelViewport.getFirstVisibleRow();

         for (int i = firstSel; i < sel.size() && i <= catSelViewport.getLastVisibleRow(); i++) {
            int siy = catSelViewport.getRowY(i);
            if (siy != -2147483648) {
               String display = RegistryDisplayNameCache.load(sel.get(i));
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  Component.literal(display),
                  autismclient.gui.f.NetHelper.init(x, siy, delX - x - 1, 13),
                  mx >= x && mx < delX - 1 && my >= siy && my < siy + 13,
                  false,
                  ListRowRenderer.RowTone.NORMAL,
                  false
               );
               int fi = i;
               this.init_V_GuiGraphicsExtract_2(ctx, delX, siy, delW, mx, my, () -> {
                  sel.remove(fi);
                  ScrollableListView vp = this.entryViewports.get(key);
                  if (vp != null) {
                     vp.scrollByRows(-1);
                  }
               });
            }
         }

         catSelViewport.endFrame(ctx);
      } else {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "(none selected)", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 1, w, UiColorPalette.divider());
      cy += 5;
      if (af != null) {
         af.setX(x);
         af.setY(cy);
         af.setWidth(w);
         af.render(ctx, mx, my, delta);
      }

      cy += 14;
      int headerH = 15;
      int capBtnH2 = 13;
      int capBtnW2 = this.captureButtonWidth(field.captureMode(), null, null);
      int cbX = x + w - capBtnW2;
      int headerTextW = Math.max(24, cbX - x - 4);
      String headerLabel = autismclient.gui.f.b.k.init(this.font, "Available Blocks", headerTextW, font, UiColorPalette.textBody());
      int headerTextY = UiLayoutMath.centerTextY(cy, headerH, this.styleTheme.init_I_l(l.read), this.styleTheme.flush());
      this.renderCaptureButton(
         ctx, cbX, cy, capBtnW2, capBtnH2, field.captureMode(), false, true, mx, my, () -> this.init_V_FieldDef_List(field, sel)
      );
      autismclient.gui.f.b.k.init(ctx, this.font, headerLabel, font, UiColorPalette.textBody(), x, headerTextY, false);
      cy += headerH + 2;
      List<String> filtered = autismclient.gui.macro.editor.capture.EditorListRowHelper.load(
         blockIds(),
         this.filteredOptionCache.computeIfAbsent(key + "_catalog", ignored -> new ArrayList<>()),
         idx -> matchesFilter(filter, idx, registryDisplayName(idx), RegistryDisplayNameCache.load(idx))
      );
      int catItemW = w - 5 - 1;
      ScrollableListView catViewport = this.viewportFor(this.listViewports, key, x, cy, w, 60, 13, 5);
      catViewport.setContentHeight(filtered.size() * 13);
      catViewport.renderScrollbar(ctx, mx, my);
      catViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
      int firstItem = catViewport.getFirstVisibleRow();

      for (int i = firstItem; i < filtered.size() && i <= catViewport.getLastVisibleRow(); i++) {
         int iy = catViewport.getRowY(i);
         if (iy != -2147483648) {
            String id = filtered.get(i);
            String display = RegistryDisplayNameCache.load(id);
            boolean already = sel.contains(id);
            boolean hov = mx >= x && mx < x + catItemW && my >= iy && my < iy + 13;
            autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
               ctx,
               this.font,
               Component.literal(display),
               autismclient.gui.f.NetHelper.init(x, iy, catItemW, 13),
               hov,
               already,
               already ? ListRowRenderer.RowTone.READY : ListRowRenderer.RowTone.NORMAL,
               false
            );
            if (!already) {
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, catItemW, 13, () -> {
                  if (!sel.contains(id)) {
                     sel.add(id);
                  }
               }));
            } else {
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, catItemW, 13, () -> sel.remove(id)));
            }
         }
      }

      catViewport.endFrame(ctx);
   }

   private void refreshVariableWarning() {
      this.variableWarning = this.standalonePayloadMode ? "" : this.undefinedVariableWarning();
   }

   private int variableWarningHeight() {
      return this.variableWarning.isBlank() ? 0 : UiDensityMetrics.scaledHeight(12);
   }

   private int footerHeight() {
      return 4 + this.variableWarningHeight() + 13 + 4;
   }

   private void renderFooter(GuiGraphicsExtractor ctx, int footerY, int mx, int my) {
      int btnH = 13;
      String variableWarning = this.variableWarning;
      int warnH = this.variableWarningHeight();
      int btnY = footerY + 4 + warnH;
      if (this.standalonePayloadMode || this.isLegacyMulti()) {
         int closeW = this.buttonWidth("Close");
         int closeX = this.editorX + (this.editorWidth - closeW) / 2;
         PanelSurfaceHelper.drawDividerColored(ctx, this.editorX + 4, footerY, this.editorWidth - 8, UiColorPalette.divider());
         this.renderActionButton(
            ctx, closeX, btnY, closeW, btnH, "Close", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> this.finishEditing(false)
         );
      } else {
         String saveLbl = this.payloadActionDraft != null ? "Save Macro" : "Save";
         int cancelW = this.buttonWidth("Cancel");
         int saveW = this.buttonWidth(saveLbl);
         int gap = 2;
         if (cancelW + saveW + gap > this.editorWidth - 8) {
            cancelW = saveW = this.payloadActionDraft != null ? 72 : 58;
         }

         int total = cancelW + saveW + gap;
         int sx = this.editorX + (this.editorWidth - total) / 2;
         PanelSurfaceHelper.drawDividerColored(ctx, this.editorX + 4, footerY, this.editorWidth - 8, UiColorPalette.divider());
         if (!variableWarning.isBlank()) {
            Identifier font = this.styleTheme.init_Identifier_l(l.read);
            String text = autismclient.gui.f.b.k.init(this.font, variableWarning, this.editorWidth - 8, font, -1);
            autismclient.gui.f.b.k.init(
               ctx,
               this.font,
               text,
               font,
               -38037,
               this.editorX + Math.max(4, (this.editorWidth - this.textWidth(font, text)) / 2),
               UiLayoutMath.centerTextY(footerY + 2, warnH, this.init_I_Identifier(font), this.styleTheme.flush()),
               false
            );
         }

         this.renderActionButton(
            ctx, sx, btnY, cancelW, btnH, "Cancel", OverlayActionButton.ButtonTone.SECONDARY, true, mx, my, () -> this.finishEditing(false)
         );
         int saveX = sx + cancelW + gap;
         this.renderActionButton(
            ctx, saveX, btnY, saveW, btnH, saveLbl, OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, () -> this.finishEditing(true)
         );
      }
   }

   private String undefinedVariableWarning() {
      if (this.macro != null && this.macro.actions != null && this.actionIndex >= 0) {
         Set<String> used = new LinkedHashSet<>();

         for (ThemedTextInputBox field : this.textFields.values()) {
            collectVariableRefs(used, field == null ? "" : field.getValue());
         }

         for (List<String> values : this.listFields.values()) {
            for (String value : values) {
               collectVariableRefs(used, value);
            }
         }

         used.removeAll(this.actionOutputVariables());
         if (used.isEmpty()) {
            return "";
         }

         used.removeAll(autismclient.util.macro.MacroTemplateResolver.BUILTIN_VARIABLES);
         if (used.isEmpty()) {
            return "";
         }

         Map<String, Integer> providerIndex = new LinkedHashMap<>();

         for (int i = 0; i < this.macro.actions.size(); i++) {
            LinkedHashSet<String> supplied = new LinkedHashSet<>();
            collectActionOutputTokens(supplied, this.macro.actions.get(i));

            for (String token : supplied) {
               LinkedHashSet<String> roots = new LinkedHashSet<>();
               collectVariableRefs(roots, token);

               for (String root : roots) {
                  providerIndex.putIfAbsent(root, i);
               }
            }
         }

         for (String root : used) {
            Integer provider = providerIndex.get(root);
            if (provider == null) {
               return "Value may be missing";
            }

            if (provider >= this.actionIndex) {
               return "Captured later";
            }

            if (this.isInRace(provider) && !this.sameRace(provider, this.actionIndex)) {
               return "Race-only value";
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private Set<String> actionOutputVariables() {
      LinkedHashSet<String> outputs = new LinkedHashSet<>();
      if (this.editingAction instanceof CaptureOutputAction captureOutput) {
         String root = MacroVariableStore.load(captureOutput.load());
         if (!root.isBlank()) {
            outputs.add(root);
         }
      }

      String pattern = "";
      PatternCaptureMatcher.MatchMode mode = PatternCaptureMatcher.MatchMode.MATCH;
      if (this.editingAction instanceof WaitForChatAction) {
         ThemedTextInputBox field = this.textFields.get("pattern");
         pattern = field == null ? "" : field.getValue();
         mode = this.selectedMatchMode();
      } else if (this.editingAction instanceof WaitForGuiAction) {
         ThemedTextInputBox field = this.textFields.get("guiTitle");
         pattern = field == null ? "" : field.getValue();
         mode = this.selectedMatchMode();
      } else if (this.editingAction instanceof WaitGuiTypeAction) {
         ThemedTextInputBox field = this.textFields.get("title");
         pattern = field == null ? "" : field.getValue();
         mode = this.selectedMatchMode();
      } else if (this.editingAction instanceof autismclient.util.macro.CaptureValueAction) {
         ThemedTextInputBox field = this.textFields.get("pattern");
         pattern = field == null ? "" : field.getValue();
         mode = this.selectedMatchMode();
      }

      if (mode != PatternCaptureMatcher.MatchMode.MATCH) {
         outputs.addAll(PatternCaptureMatcher.init(mode, pattern));
      }

      return outputs;
   }

   private PatternCaptureMatcher.MatchMode selectedMatchMode() {
      try {
         return PatternCaptureMatcher.MatchMode.valueOf(this.enumValue("matchMode"));
      } catch (IllegalArgumentException | NullPointerException ignored) {
         return PatternCaptureMatcher.MatchMode.MATCH;
      }
   }

   private static void collectVariableRefs(Set<String> out, String text) {
      if (out != null && text != null && !text.isBlank()) {
         Matcher matcher = VARIABLE_REF.matcher(text);

         while (matcher.find()) {
            String name = matcher.group(1).toLowerCase(Locale.ROOT);
            int dot = name.indexOf(46);
            out.add(dot < 0 ? name : name.substring(0, dot));
         }
      }
   }

   private boolean isInRace(int index) {
      return this.enclosingRaceIndex(index) >= 0;
   }

   private boolean sameRace(int left, int right) {
      int leftHeader = this.enclosingRaceIndex(left);
      return leftHeader >= 0 && leftHeader == this.enclosingRaceIndex(right);
   }

   private int enclosingRaceIndex(int index) {
      if (this.macro != null && this.macro.actions != null && index >= 0) {
         for (int i = 0; i < Math.min(index, this.macro.actions.size()); i++) {
            MacroAction action = this.macro.actions.get(i);
            if (action instanceof RaceAction race) {
               int end = i + Math.max(0, race.bodyCount);
               if (index > i && index <= end) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private void hideOverlaysForCapture() {
      this.clearToasts();
      this.hiddenOverlays = new ArrayList<>();

      for (AutismOverlay o : OverlayManager.getInstance().getOverlays()) {
         if (o != this && o.isVisible()) {
            o.saveBounds();
            this.hiddenOverlays.add(o);
            o.setVisible(false);
         }
      }

      this.saveBounds();
      this.clearInputFocus();
      this.visibleBeforeCapture = this.visible;
      this.visible = false;
      AutismSessionState.getInstance().setTimedCaptureMode(true);
   }

   private void startCapture(String key) {
      if (this.ensureCaptureAvailable()) {
         this.captureSession.init(key, this::prepareSlotCapture, () -> this.finishCapture(false, false));
      }
   }

   private void prepareSlotCapture() {
      Screen current = MC.gui.screen();
      boolean hasSlotScreen = current instanceof AbstractContainerScreen;
      this.restoreScreenAfterCapture = false;
      this.hideOverlaysForCapture();
      if (!hasSlotScreen) {
         this.screenBeforeCapture = current;
         this.restoreScreenAfterCapture = true;
         MC.execute(() -> {
            if (MC.player != null) {
               OverlayScreenController.setScreenExplicit(MC, new InventoryScreen(MC.player));
            }
         });
      }
   }

   private void restoreHiddenOverlays() {
      if (this.hiddenOverlays != null) {
         OverlayManager manager = OverlayManager.getInstance();

         for (AutismOverlay overlay : this.hiddenOverlays) {
            manager.register(overlay);
            overlay.setVisible(true);
            overlay.saveBounds();
         }

         this.hiddenOverlays = null;
      }
   }

   private void clearCaptureCallbacks(boolean cancelGBreak) {
      AutismSessionState state = AutismSessionState.getInstance();
      state.setCaptureCancelCallback(null);
      state.setBlockCaptureCallback(null);
      state.setBlockFaceCaptureCallback(null);
      state.setEntityCaptureCallback(null);
      state.setAttackCaptureCallback(null);
      state.setCaptureSpecificEntity(false);
      if (cancelGBreak && state.isGbreakCapturing()) {
         state.cancelGbreakCapture();
      }
   }

   private void finishCapture(boolean reopenInventory, boolean closeCurrentScreen) {
      this.restoreHiddenOverlays();
      this.clearCaptureCallbacks(false);
      AutismSessionState.getInstance().setPlacePreviewActive(false);
      AutismSessionState.getInstance().setTimedCaptureMode(false);
      this.visible = this.visibleBeforeCapture;
      this.visibleBeforeCapture = false;
      this.valid_List.clear();
      this.scrollbarRegions.clear();
      OverlayManager.getInstance().bringToFront(this);
      if (this.restoreScreenAfterCapture) {
         Screen restore = this.screenBeforeCapture;
         MC.execute(() -> {
            if (MC.gui.screen() instanceof InventoryScreen) {
               if (restore != null) {
                  OverlayScreenController.setScreenExplicit(MC, restore);
               }
            } else if (restore == null && MC.gui.screen() == null && MC.player != null) {
               OverlayScreenController.setScreenExplicit(MC, new InventoryScreen(MC.player));
            } else if (restore != null && MC.gui.screen() == null) {
               OverlayScreenController.setScreenExplicit(MC, restore);
            }
         });
         this.restoreScreenAfterCapture = false;
      } else if (reopenInventory && this.screenBeforeCapture != null) {
         final Screen restore = this.screenBeforeCapture;
         MC.execute(() -> {
            if (MC.gui.screen() == null) {
               OverlayScreenController.setScreenExplicit(MC, restore);
            }
         });
      } else if (reopenInventory && MC.player != null) {
         MC.execute(() -> {
            if (MC.gui.screen() == null && MC.player != null) {
               OverlayScreenController.setScreenExplicit(MC, new InventoryScreen(MC.player));
            }
         });
      }

      if (closeCurrentScreen) {
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
      }

      this.screenBeforeCapture = null;
      this.resizeEditorToContent();
   }

   private void finishEditing(boolean save) {
      this.auditEditSession++;
      this.restoreHiddenOverlays();
      this.visibleBeforeCapture = false;
      this.clearCaptureCallbacks(true);
      AutismSessionState.getInstance().setTimedCaptureMode(false);
      this.clearToasts();
      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
         autismclient.util.BlockPreviewRenderer.load();
      }

      if (save && this.editingAction != null && !this.isLegacyMulti()) {
         if (this.onPreSave != null) {
            this.onPreSave.run();
         }

         this.writeDraftsToActionTag();
         this.editingAction.fromTag(this.actionTag);
         DynamicBindingManager.init(this.editingAction, this.actionTag);
         if (this.onSave != null) {
            this.onSave.run();
         }
      }

      this.packetSelector.resetSelection();
      this.raceStepSelector.dismiss();
      this.visible = false;
      this.editingAction = null;
      this.editCount = 1;
      this.standalonePayloadMode = false;
      this.payloadActionDraft = null;
      this.pendingPayloadEdit = null;
      this.cachedPayloadSnapshot = null;
      this.actionTag = null;
      this.fieldSchema = null;
      this.itemActionDraft = null;
      this.captureSession.clear();
      if (this.capturingFieldKey != null) {
         ContainerCloseGuard.read();
         this.capturingFieldKey = null;
      }

      this.dropdowns.clear();
      this.dropdownCache.clear();
      this.craftEntries = null;
      this.craftOptions = null;
      this.filteredCraftOptions = null;
      this.selectedCraftOption = null;
      this.recipeListBounds = null;
      this.dropActionDraft = null;
      this.itemTargets.clear();
      this.itemTargetLists.clear();
      this.lanStepEntries = null;
      this.toggleModuleEntries = null;
      this.onPreSave = null;
      this.onSave = null;
      this.screenBeforeGbreakCapture = null;
      this.screenBeforeCapture = null;
   }

   private void clearToasts() {
      this.toasts.clear();
   }

   private void showToast(String message, int accentColor) {
      this.toasts.init(message, accentColor);
   }

   public boolean hasToasts() {
      return this.toasts.hasToasts();
   }

   public void renderToasts(GuiGraphicsExtractor context, int anchorX, int anchorY, int anchorWidth) {
      this.toasts.init(UiHelper.init(context, this.font, anchorX, anchorY), anchorX, anchorY, anchorWidth);
   }

   private void applyFieldsToAction(MacroAction action) {
      for (FieldDef field : this.fieldSchema.fields()) {
         String key = field.key();
         switch (field.type()) {
            case TEXT:
            case MACRO_SELECT: {
               String v = this.actionTag.contains(key) ? this.actionTag.getStringOr(key, "") : "";
               ThemedTextInputBox f = this.newTextField(80);
               f.setValue(v);
               if ("saveAs".equals(key) && (v.isEmpty() || VARIABLE_NAME_PATTERN.matcher(v).matches())) {
                  f.setFilter(sx -> sx.isEmpty() || VARIABLE_NAME_PATTERN.matcher(sx).matches());
               }

               this.textFields.put(key, f);
               break;
            }
            case NUMBER: {
               int v = this.actionTag.contains(key) ? this.actionTag.getIntOr(key, 0) : 0;
               ThemedTextInputBox f = this.newTextField(80);
               if (!field.dynamic()) {
                  f.setIntegerOnlyFilter(true);
               }

               String binding = DynamicBindingManager.init(action, key);
               f.setValue(binding.isBlank() ? String.valueOf(v) : binding);
               this.textFields.put(key, f);
               break;
            }
            case DECIMAL: {
               double v = this.actionTag.contains(key) ? this.actionTag.getDoubleOr(key, 0.0) : 0.0;
               ThemedTextInputBox f = this.newTextField(80);
               if (!field.dynamic()) {
                  f.setFilter(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*\\.?\\d*"));
               }

               String binding = DynamicBindingManager.init(action, key);
               f.setValue(binding.isBlank() ? formatNumber(v) : binding);
               this.textFields.put(key, f);
               break;
            }
            case SLOT: {
               int v = this.actionTag.contains(key) ? this.actionTag.getIntOr(key, 0) : 0;
               ThemedTextInputBox f = this.newTextField(50);
               if (!field.dynamic()) {
                  f.setFilter(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*"));
               }

               String binding = DynamicBindingManager.init(action, key);
               f.setValue(binding.isBlank() ? String.valueOf(v) : binding);
               this.textFields.put(key, f);
               break;
            }
            case BLOCK_POS: {
               String[] xyzKeys = field.xyzKeys();
               boolean dbl = field.xyzDouble();

               for (int i = 0; i < 3; i++) {
                  ThemedTextInputBox f = this.newTextField(50);
                  if (dbl) {
                     if (!field.dynamic()) {
                        f.setFilter(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*\\.?\\d*"));
                     }

                     double v = this.actionTag.contains(xyzKeys[i]) ? this.actionTag.getDoubleOr(xyzKeys[i], 0.0) : 0.0;
                     String binding = DynamicBindingManager.init(action, xyzKeys[i]);
                     f.setValue(binding.isBlank() ? formatNumber(v) : binding);
                  } else {
                     if (!field.dynamic()) {
                        f.setIntegerOnlyFilter(true);
                     }

                     int v = this.actionTag.contains(xyzKeys[i]) ? this.actionTag.getIntOr(xyzKeys[i], 0) : 0;
                     String binding = DynamicBindingManager.init(action, xyzKeys[i]);
                     f.setValue(binding.isBlank() ? String.valueOf(v) : binding);
                  }

                  this.textFields.put(key + "_" + i, f);
               }
               break;
            }
            case TOGGLE:
               this.toggleStates.put(key, this.actionTag.contains(key) ? this.actionTag.getBooleanOr(key, false) : false);
               break;
            case ENUM: {
               String v = this.actionTag.contains(key) ? this.actionTag.getStringOr(key, "") : "";
               List<String> opts = field.enumOptions();
               int idx = opts.indexOf(v);
               if (idx < 0) {
                  idx = opts.indexOf(v.toUpperCase());
               }

               this.modeIndices.put(key, Math.max(0, idx));
               if (field.dynamic()) {
                  ThemedTextInputBox bindingField = this.newTextField(80);
                  bindingField.setValue(DynamicBindingManager.init(action, key));
                  this.textFields.put(key, bindingField);
               }
               break;
            }
            case STRING_LIST:
               List<String> list = new ArrayList<>();
               if (this.actionTag.contains(key)) {
                  for (Tag el : this.actionTag.getList(key).orElse(new ListTag())) {
                     String s = el.asString().orElse("");
                     if (!s.isEmpty()) {
                        list.add(s);
                     }
                  }
               }

               this.listFields.put(key, list);
               ThemedTextInputBox af = this.newTextField(80);
               if (field.captureMode() == CaptureMode.BLOCK_CATALOG) {
                  af.setPlaceholder(Component.literal("Search blocks..."));
               } else {
                  af.setPlaceholder(Component.literal("Search / " + field.addLabel()));
                  af.setSubmitHandler(text -> {
                     if (!text.isBlank()) {
                        List<String> entries = this.listFields.get(key);
                        if (entries != null && this.addListEntry(field, entries, text.strip())) {
                           af.setValue("");
                        }
                     }

                     return true;
                  });
               }

               this.internalTextFields.put(key, af);
         }
      }

      if (action instanceof ToolsAction) {
         for (FieldDef field : this.fieldSchema.fields()) {
            if (field.key().startsWith("toolArg.")) {
               ThemedTextInputBox input = this.textFields.get(field.key());
               if (input != null && !this.actionTag.contains(field.key())) {
                  input.setValue("");
               }

               if (input != null) {
                  input.setPlaceholder(Component.literal("Default"));
               }
            }
         }
      }
   }

   private void selectEnumOption(FieldDef field, int index) {
      if (index >= 0 && index < field.enumOptions().size()) {
         boolean changed = index != this.modeIndices.getOrDefault(field.key(), 0);
         this.modeIndices.put(field.key(), index);
         if (changed && this.editingAction instanceof ToolsAction && "toolId".equals(field.key())) {
            this.writeDraftsToActionTag();
            this.actionTag.putString("argumentsJson", "{}");

            for (FieldDef oldField : this.fieldSchema.fields()) {
               if (oldField.key().startsWith("toolArg.")) {
                  this.removeDynamicBinding(oldField.key());
               }
            }

            for (String key : new ArrayList<>(this.actionTag.keySet())) {
               if (key.startsWith("toolArg.")) {
                  this.actionTag.remove(key);
               }
            }

            ToolsAction draft = new ToolsAction();
            draft.fromTag(this.actionTag);
            DynamicBindingManager.init(draft, this.actionTag);
            this.fieldSchema = ToolsActionSchemaBuilder.init(draft);
            this.textFields.clear();
            this.toggleStates.clear();
            this.modeIndices.clear();
            this.dropdownCache.clear();
            this.applyFieldsToAction(draft);
            this.applyFieldPlaceholders();
            this.scrollOffset = 0;
         }
      }
   }

   private void writeDraftsToActionTag() {
      if (this.actionTag != null) {
         if (this.itemActionDraft != null) {
            if (this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()) {
               this.flush_V_2();
            }

            for (int i = 0; i < this.itemActionDraft.itemNames.size(); i++) {
               ThemedTextInputBox tf = this.textFields.get("item_times_" + i);
               if (tf != null) {
                  try {
                     while (this.itemActionDraft.itemTimes.size() <= i) {
                        this.itemActionDraft.itemTimes.add(1);
                     }

                     this.itemActionDraft.itemTimes.set(i, Math.max(1, Integer.parseInt(tf.getValue().strip())));
                  } catch (NumberFormatException var23) {
                  }
               }
            }

            this.itemActionDraft.waitForGuiBefore = this.toggleStates.getOrDefault("item_waitForGuiBefore", false);
            this.itemActionDraft.waitForGuiAfter = this.toggleStates.getOrDefault("item_waitForGuiAfter", false);
            this.itemActionDraft.waitForItem = this.toggleStates.getOrDefault("item_waitForItem", false);
            this.itemActionDraft.useCursorItemForPickupAll = this.toggleStates.getOrDefault("item_useCursorItemForPickupAll", true);
            this.itemActionDraft.useClickDelay = this.toggleStates.getOrDefault("item_useClickDelay", false);
            ThemedTextInputBox delayF = this.textFields.get("item_clickDelayMs");
            if (delayF != null) {
               try {
                  this.itemActionDraft.clickDelayMs = Math.max(0, Integer.parseInt(delayF.getValue().strip()));
               } catch (NumberFormatException var22) {
               }
            }

            ThemedTextInputBox guiF = this.textFields.get("item_guiName");
            if (guiF != null) {
               this.itemActionDraft.guiName = guiF.getValue();
            }

            if (!this.itemActionDraft.waitForGuiBefore && !this.itemActionDraft.waitForGuiAfter) {
               this.itemActionDraft.guiName = "";
            }

            if (!this.itemActionDraft.itemNames.isEmpty()) {
               this.itemActionDraft.useSlot = false;
               this.itemActionDraft.targetSlot = -1;
               this.itemActionDraft.actionIndex = 0;
               this.itemActionDraft.button = 0;
               this.itemActionDraft.times = 1;
            }

            this.actionTag = this.itemActionDraft.toTag();
         } else if (this.craftEntries != null) {
            for (int i = 0; i < this.craftEntries.size(); i++) {
               autismclient.util.macro.CraftAction.CraftEntry entry = this.craftEntries.get(i);
               ThemedTextInputBox af = this.textFields.get("craft_amount_" + i);
               if (af != null) {
                  try {
                     entry.amount = Math.max(1, Integer.parseInt(af.getValue().strip()));
                  } catch (NumberFormatException var21) {
                  }
               }

               entry.useMaxAmount = this.toggleStates.getOrDefault("craft_useMax_" + i, false);
            }

            ListTag entryTags = new ListTag();

            for (autismclient.util.macro.CraftAction.CraftEntry entry : this.craftEntries) {
               if (entry.isValid()) {
                  entryTags.add(entry.toTag());
               }
            }

            this.actionTag.put("entries", entryTags);
         } else if (this.dropActionDraft != null) {
            if (this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()) {
               this.reset_V();
            }

            while (this.dropActionDraft.itemCounts.size() < this.dropActionDraft.itemNames.size()) {
               this.dropActionDraft.itemCounts.add(this.dropActionDraft.getItemCount(this.dropActionDraft.itemCounts.size()));
            }

            for (int i = 0; i < this.dropActionDraft.itemNames.size(); i++) {
               ThemedTextInputBox f = this.textFields.get("drop_count_" + i);
               if (f != null) {
                  try {
                     this.dropActionDraft.itemCounts.set(i, Math.max(0, Integer.parseInt(f.getValue().strip())));
                  } catch (NumberFormatException var20) {
                  }
               }
            }

            ThemedTextInputBox cntF = this.textFields.get("drop_globalCount");
            if (cntF != null) {
               try {
                  this.dropActionDraft.dropCount = Math.max(1, Integer.parseInt(cntF.getValue().strip()));
               } catch (NumberFormatException var19) {
               }
            }

            DropAction.Inner_DataUtil[] modes = DropAction.Inner_DataUtil.values();
            this.dropActionDraft.mode = modes[Math.min(this.modeIndices.getOrDefault("drop_mode", 0), modes.length - 1)];
            this.dropActionDraft.waitForGuiBefore = this.toggleStates.getOrDefault("drop_waitForGuiBefore", false);
            this.dropActionDraft.waitForGuiAfter = this.toggleStates.getOrDefault("drop_waitForGuiAfter", false);
            this.dropActionDraft.useHandlerSlots = this.toggleStates.getOrDefault("drop_useHandlerSlots", true);
            ThemedTextInputBox guiF = this.textFields.get("drop_guiName");
            if (guiF != null) {
               this.dropActionDraft.guiName = guiF.getValue();
            }

            if (!this.dropActionDraft.waitForGuiBefore && !this.dropActionDraft.waitForGuiAfter) {
               this.dropActionDraft.guiName = "";
            }

            this.actionTag = this.dropActionDraft.toTag();
            this.actionTag.putString("packetOrder", this.enumValue("packetOrder"));
         } else if (this.payloadActionDraft != null) {
            this.payloadActionDraft = this.load_H();
            this.actionTag = this.payloadActionDraft.toTag();
         } else {
            if (this.lanStepEntries != null) {
               for (int i = 0; i < this.lanStepEntries.size(); i++) {
                  WaitForLanStepAction.StepEntry e = this.lanStepEntries.get(i);
                  ThemedTextInputBox uf = this.textFields.get("lan_user_" + i);
                  if (uf != null) {
                     e.username = uf.getValue();
                  }

                  ThemedTextInputBox sf = this.textFields.get("lan_step_" + i);
                  if (sf != null) {
                     try {
                        e.step = Math.max(1, Integer.parseInt(sf.getValue().strip()));
                     } catch (NumberFormatException var18) {
                     }
                  }
               }

               ListTag entryList = new ListTag();

               for (WaitForLanStepAction.StepEntry e : this.lanStepEntries) {
                  entryList.add(e.toTag());
               }

               this.actionTag.put("entries", entryList);
               this.actionTag.putBoolean("filterByUser", this.toggleStates.getOrDefault("lan_filterByUser", false));
               this.actionTag.putBoolean("listenDuringPreviousAction", this.toggleStates.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox dsF = this.textFields.get("lan_defaultStep");
               if (dsF != null) {
                  try {
                     this.actionTag.putInt("defaultStep", Math.max(1, Integer.parseInt(dsF.getValue().strip())));
                  } catch (NumberFormatException var17) {
                  }
               }
            }

            if (this.toggleModuleEntries != null) {
               ListTag entriesTag = new ListTag();

               for (ak.Inner_DataUtil entry : this.toggleModuleEntries) {
                  if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
                     entriesTag.add(entry.init());
                  }
               }

               this.actionTag.put("entries", entriesTag);
               this.actionTag.putString("moduleName", "");
               this.actionTag.putString("toggleMode", ak.Inner_StreamUtil.init.name());
            } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET) {
               List<String> targets = this.sanitizePacketTargets(this.waitPacketNames());
               ListTag packetList = new ListTag();

               for (String target : targets) {
                  packetList.add(StringTag.valueOf(target));
               }

               this.actionTag.put("packetNames", packetList);
               this.actionTag.putString("packetName", targets.isEmpty() ? "" : targets.get(0));
               this.actionTag.putBoolean("listenDuringPreviousAction", this.toggleStates.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox saveAs = this.textFields.get("saveAs");
               if (saveAs != null) {
                  this.actionTag.putString("saveAs", saveAs.getValue());
               }
            } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
               this.applyPacketRuleValue();
               ListTag rules = new ListTag();
               if (this.packetMatchRules != null) {
                  for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : this.packetMatchRules) {
                     if (rule != null && (rule.packetName != null && !rule.packetName.isBlank() || rule.load != null && !rule.load.isBlank())) {
                        rules.add(rule.toTag());
                     }
                  }
               }

               this.actionTag.put("rules", rules);
               this.actionTag.putBoolean("listenDuringPreviousAction", this.toggleStates.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox saveAs = this.textFields.get("saveAs");
               if (saveAs != null) {
                  this.actionTag.putString("saveAs", saveAs.getValue());
               }
            } else if (this.fieldSchema != null) {
               for (FieldDef field : this.fieldSchema.fields()) {
                  String key = field.key();
                  switch (field.type()) {
                     case TEXT:
                     case MACRO_SELECT:
                        ThemedTextInputBox f = this.textFields.get(key);
                        if (f != null) {
                           this.actionTag.putString(key, f.getValue());
                        }
                        break;
                     case NUMBER:
                     case SLOT:
                        ThemedTextInputBox fx = this.textFields.get(key);
                        if (fx == null) {
                           break;
                        }

                        String raw = fx.getValue().strip();
                        if (field.dynamic() && raw.contains("{")) {
                           this.setDynamicBinding(key, raw);
                        } else {
                           this.removeDynamicBinding(key);
                           if (this.editingAction instanceof ToolsAction && key.startsWith("toolArg.") && raw.isBlank()) {
                              this.actionTag.putString(key, "");
                           } else {
                              try {
                                 this.actionTag.putInt(key, Integer.parseInt(raw));
                              } catch (NumberFormatException var16) {
                              }
                           }
                        }
                        break;
                     case DECIMAL:
                        ThemedTextInputBox fxDbl = this.textFields.get(key);
                        if (fxDbl == null) {
                           break;
                        }

                        String rawDbl = fxDbl.getValue().strip();
                        if (field.dynamic() && rawDbl.contains("{")) {
                           this.setDynamicBinding(key, rawDbl);
                        } else {
                           this.removeDynamicBinding(key);
                           if (this.editingAction instanceof ToolsAction && key.startsWith("toolArg.") && rawDbl.isBlank()) {
                              this.actionTag.putString(key, "");
                           } else {
                              try {
                                 this.actionTag.putDouble(key, Double.parseDouble(rawDbl));
                              } catch (NumberFormatException var15) {
                              }
                           }
                        }
                        break;
                     case BLOCK_POS:
                        String[] xyzKeys = field.xyzKeys();
                        boolean dbl = field.xyzDouble();
                        int i = 0;

                        for (; i < 3; i++) {
                           ThemedTextInputBox fXyz = this.textFields.get(key + "_" + i);
                           if (fXyz != null) {
                              String t = fXyz.getValue().strip();
                              if (field.dynamic() && t.contains("{")) {
                                 this.setDynamicBinding(xyzKeys[i], t);
                              } else {
                                 this.removeDynamicBinding(xyzKeys[i]);
                                 if (dbl) {
                                    try {
                                       this.actionTag.putDouble(xyzKeys[i], Double.parseDouble(t));
                                    } catch (NumberFormatException var14) {
                                    }
                                 } else {
                                    try {
                                       this.actionTag.putInt(xyzKeys[i], Integer.parseInt(t));
                                    } catch (NumberFormatException var13) {
                                    }
                                 }
                              }
                           }
                        }
                        break;
                     case TOGGLE:
                        this.actionTag.putBoolean(key, this.toggleStates.getOrDefault(key, false));
                        break;
                     case ENUM:
                        ThemedTextInputBox bindingField = field.dynamic() ? this.textFields.get(key) : null;
                        String binding = bindingField == null ? "" : bindingField.getValue().strip();
                        if (field.dynamic() && !binding.isBlank()) {
                           this.setDynamicBinding(key, binding);
                        } else {
                           this.removeDynamicBinding(key);
                           List<String> opts = field.enumOptions();
                           if (!opts.isEmpty()) {
                              int idx = Math.min(this.modeIndices.getOrDefault(key, 0), opts.size() - 1);
                              this.actionTag.putString(key, opts.get(idx));
                           }
                        }
                        break;
                     case STRING_LIST:
                        List<String> items = this.listFields.getOrDefault(key, Collections.emptyList());
                        ListTag nbt = new ListTag();

                        for (String s : items) {
                           nbt.add(StringTag.valueOf(s));
                        }

                        this.actionTag.put(key, nbt);
                  }
               }

               this.storeItemTargetFields();
               if (this.editingAction instanceof GuiWaitAware && !this.waitsForGui()) {
                  this.actionTag.putString("guiName", "");
                  this.actionTag.putString("waitGuiName", "");
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY) {
                  String mode = this.enumValue("mode");
                  if ("CLOSE".equals(mode)) {
                     this.actionTag.putBoolean("waitForGuiBefore", false);
                     this.actionTag.putBoolean("waitForGuiAfter", false);
                  }

                  this.actionTag.putString("guiName", "");
                  this.actionTag.putBoolean("sendPacket", !this.toggleStates.getOrDefault("sendPacket", false));
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
                  ListTag nbtEntries = new ListTag();
                  if (this.slotChangeEntries != null) {
                     for (WaitForSlotChangeAction.Inner_DataUtil e : this.slotChangeEntries) {
                        CompoundTag ec = new CompoundTag();
                        MacroItemTarget target = e.resolveItemTarget();
                        if (target.hasSlot() || target.hasItemFilter()) {
                           ec.put("target", target.toTag());
                        }

                        ec.putString("mode", e.mode.name());
                        ec.putInt("count", Math.max(1, e.count));
                        nbtEntries.add(ec);
                     }
                  }

                  this.actionTag.put("entries", nbtEntries);
                  this.actionTag.putBoolean("listenDuringPreviousAction", this.toggleStates.getOrDefault("listenDuringPreviousAction", false));
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.USE_ITEM) {
                  ThemedTextInputBox slotField = this.textFields.get("slot");
                  if (slotField == null || slotField.getValue() == null || slotField.getValue().strip().isEmpty()) {
                     this.actionTag.remove("slot");
                  }

                  String mode = this.actionTag.getStringOr("useMode", "AUTOMATIC");
                  if ("CUSTOM_HOLD".equals(mode)) {
                     this.actionTag.putInt("useCount", 1);
                     if (this.actionTag.getIntOr("holdTicks", 0) <= 0) {
                        this.actionTag.putInt("holdTicks", 20);
                     }
                  } else if (this.actionTag.getIntOr("useCount", 0) <= 0) {
                     this.actionTag.putInt("useCount", 1);
                  }
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.NBT_BOOK) {
                  this.applyBookTagDefaults();
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SWAP_SLOTS) {
                  boolean fromUseItemName = this.actionTag.getBooleanOr("fromUseItemName", false);
                  boolean toUseItemName = this.actionTag.getBooleanOr("toUseItemName", false);
                  if (fromUseItemName) {
                     this.actionTag.putInt("fromSlot", -1);
                  } else {
                     this.actionTag.putString("fromItemName", "");
                  }

                  if (toUseItemName) {
                     this.actionTag.putInt("toSlot", -1);
                  } else {
                     this.actionTag.putString("toItemName", "");
                  }
               }

               if (this.editingAction != null
                  && this.editingAction.getType() == MacroActionType.GO_TO
                  && !this.actionTag.getBooleanOr("waitForArrival", false)) {
                  this.actionTag.putDouble("arrivalRadius", 2.0);
                  this.actionTag.putInt("timeoutMs", 60000);
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CLICK) {
                  if (!this.waitsForGui()) {
                     this.actionTag.putString("guiName", "");
                  }

                  if ("MIDDLE".equals(this.actionTag.getStringOr("clickType", "RIGHT"))) {
                     this.actionTag.putString("clickType", "RIGHT");
                  }

                  if (this.actionTag.getIntOr("clickCount", 0) <= 0) {
                     this.actionTag.putInt("clickCount", 1);
                  }
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.XCARRY) {
                  this.actionTag.putInt("safeClickDelayTicks", XCarryAction.write(this.actionTag.getIntOr("safeClickDelayTicks", 1)));
                  this.actionTag
                     .putBoolean(
                        "safeClickDelayAfterPickup",
                        this.toggleStates.getOrDefault("safeClickDelayAfterPickup", this.actionTag.getBooleanOr("safeClickDelayAfterPickup", true))
                     );
                  this.actionTag
                     .putBoolean(
                        "safeClickDelayBeforeReturn",
                        this.toggleStates.getOrDefault("safeClickDelayBeforeReturn", this.actionTag.getBooleanOr("safeClickDelayBeforeReturn", true))
                     );
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DISCONNECT) {
                  String mode = this.actionTag.getStringOr("mode", "DISCONNECT");
                  if ("DISCONNECT".equals(mode)) {
                     this.actionTag.putBoolean("useNextAction", false);
                  } else if (this.actionTag.getIntOr("packetCount", 0) <= 0) {
                     this.actionTag.putInt("packetCount", 200);
                  }

                  if (!"KICK_DUPE".equals(mode)) {
                     this.actionTag.putBoolean("useNextAction", false);
                  }
               }

               if (this.editingAction != null
                  && this.editingAction.getType() == MacroActionType.BREAK
                  && !this.actionTag.getBooleanOr("interact", false)) {
                  this.actionTag.putBoolean("runNextSteps", false);
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CLOSE_GUI) {
                  this.actionTag.putBoolean("sendPacket", !this.toggleStates.getOrDefault("sendPacket", false));
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SAVE_GUI) {
                  this.actionTag.putBoolean("sendPacket", !this.toggleStates.getOrDefault("sendPacket", false));
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.STORE_ITEM) {
                  this.actionTag.putBoolean("closeSendPkt", !this.toggleStates.getOrDefault("closeSendPkt", false));
               }

               if (this.editingAction != null
                  && (this.editingAction.getType() == MacroActionType.OPEN_CONTAINER || this.editingAction.getType() == MacroActionType.INTERACT_ENTITY)) {
                  List<String> selectedTargets = this.listFields.getOrDefault("entityTargets", Collections.emptyList());
                  String entityTarget = selectedTargets.isEmpty() ? "" : selectedTargets.get(0);
                  this.actionTag.putString("entityTarget", entityTarget);
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_CHAT) {
                  this.actionTag.putInt("fuzzyPercent", WaitForChatAction.snapFuzzyPercent(this.fuzzyPercent()));
                  if (!this.waitsForGui()) {
                     this.actionTag.putString("waitGuiName", "");
                  }
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
                  if (!this.actionTag.getBooleanOr("manualDirection", false)) {
                     this.actionTag.remove("direction");
                  }

                  this.actionTag.putBoolean("waitForItem", this.toggleStates.getOrDefault("place_waitForItem", true));
                  this.actionTag.putBoolean("silentSwitch", this.toggleStates.getOrDefault("place_silentSwitch", false));
               }
            }
         }
      }
   }

   private void setDynamicBinding(String key, String template) {
      if (this.actionTag != null && key != null && !key.isBlank()) {
         CompoundTag bindings = this.actionTag.get("_dynamicBindings") instanceof CompoundTag existing ? existing : new CompoundTag();
         bindings.putString(key, template == null ? "" : template);
         this.actionTag.put("_dynamicBindings", bindings);
      }
   }

   private void removeDynamicBinding(String key) {
      if (this.actionTag != null && key != null && this.actionTag.get("_dynamicBindings") instanceof CompoundTag bindings) {
         bindings.remove(key);
         if (bindings.isEmpty()) {
            this.actionTag.remove("_dynamicBindings");
         }
      }
   }

   private void applyBookTagDefaults() {
      if (this.actionTag != null) {
         int pages = this.actionTag.getIntOr("pages", 100);
         if (pages <= 0) {
            this.actionTag.putInt("pages", 100);
         }

         int chars = this.actionTag.getIntOr("characters", 1024);
         if (chars <= 0) {
            this.actionTag.putInt("characters", 1024);
         }

         if (!this.actionTag.contains("sign")) {
            this.actionTag.putBoolean("sign", true);
         }

         if (!this.actionTag.contains("appendCount")) {
            this.actionTag.putBoolean("appendCount", true);
         }

         String source = this.enumValue("dataSource");
         if (source == null || source.isBlank()) {
            source = "Random";
         }

         this.actionTag.putString("dataSource", source);
         if ("Random".equals(source)) {
            this.actionTag.putString("customText", "");
            this.actionTag.putString("customFilePath", "");
         } else if ("Pasted".equals(source)) {
            this.actionTag.putString("customFilePath", "");
         } else if ("File".equals(source)) {
            this.actionTag.putString("customText", "");
         }
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      this.variableWarningStale = true;
      if (!this.visible) {
         return false;
      }

      if (this.packetSelector.isVisible()) {
         return this.packetSelector.mouseClicked(mx, my, button);
      }

      if (this.raceStepSelector.isVisible()) {
         return this.raceStepSelector.mouseClicked(mx, my, button);
      }

      int imx = (int)mx;
      int imy = (int)my;
      if (!this.dropdowns.isEmpty()) {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, mx, my, button)) {
            OverlayManager.getInstance().bringToFront(this);
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            return true;
         }
      }

      if (this.isOverHeaderCloseButton(mx, my, this.getBounds())) {
         this.finishEditing(false);
         return true;
      }

      if (this.isOverHeader(mx, my)) {
         this.dragging = true;
         this.dragOffsetX = mx - this.editorX;
         this.dragOffsetY = my - this.editorY;
         OverlayManager.getInstance().bringToFront(this);
         return true;
      }

      if (!this.containsPoint(mx, my)) {
         return false;
      }

      if (this.windowCollapsed) {
         return true;
      }

      OverlayManager.getInstance().bringToFront(this);
      MouseButtonEvent click = new MouseButtonEvent(imx, imy, Compat263.newGlfwMouseButtonInfo(button, 0));
      boolean bodyInputAllowed = this.bodyClip == null || this.bodyClip.init_Z(imx, imy);
      ThemedTextInputBox focused = null;
      if (bodyInputAllowed) {
         for (Entry<String, ThemedTextInputBox> entry : this.textFields.entrySet()) {
            if (this.isPayloadFieldVisible(entry.getKey())) {
               String baseKey = entry.getKey().replaceAll("_\\d+$", "");
               FieldDef fld = this.fieldByKey(baseKey);
               if ((fld == null || this.isFieldApplicable(fld)) && entry.getValue().mouseClicked(click, false)) {
                  focused = entry.getValue();
                  break;
               }
            }
         }
      }

      if (bodyInputAllowed && focused == null) {
         for (Entry<String, ThemedTextInputBox> entry : this.internalTextFields.entrySet()) {
            FieldDef fld = this.fieldByKey(entry.getKey());
            if ((fld == null || this.isFieldApplicable(fld)) && entry.getValue().mouseClicked(click, false)) {
               focused = entry.getValue();
               break;
            }
         }
      }

      if (focused != null) {
         ThemedTextInputBox ff = focused;
         this.textFields.values().forEach(f -> f.setFocused(f == ff));
         this.internalTextFields.values().forEach(f -> f.setFocused(f == ff));
         return true;
      }

      this.clearInputFocus();
      if (button == 0 && this.isOverMatchStrengthSlider(imx, imy)) {
         this.draggingMatchStrength = true;
         this.dragMatchStrengthSlider(imx);
         return true;
      }

      if (button == 0 && this.isOverSmoothnessSlider(imx, imy)) {
         this.draggingSmoothness = true;
         this.dragSmoothnessSlider(imx);
         return true;
      }

      if (button == 0 && this.isOverSafeDelaySlider(imx, imy)) {
         this.draggingSafeDelay = true;
         this.dragSafeDelaySlider(imx);
         return true;
      }

      if (bodyInputAllowed) {
         for (ScrollableListView vp : this.entryViewports.values()) {
            if (vp.isOverScrollbar(mx, my)) {
               vp.mouseClicked(mx, my, button);
               return true;
            }
         }

         for (ScrollableListView vp : this.listViewports.values()) {
            if (vp.isOverScrollbar(mx, my)) {
               vp.mouseClicked(mx, my, button);
               return true;
            }
         }
      }

      for (ActionEditorOverlay.ScrollbarRegion r : this.scrollbarRegions) {
         if (r.containsPoint(imx, imy)) {
            this.activeScrollbarDrag = r.onDrag();
            this.activeScrollbarDrag.accept(imy);
            return true;
         }
      }

      for (ActionEditorOverlay.ClickRegion r : this.valid_List) {
         if (r.containsPoint(imx, imy) && r.fireClick(mx, my, button)) {
            this.resizeEditorToContent();
            return true;
         }
      }

      return true;
   }

   private boolean isPayloadFieldVisible(String key) {
      if ("payload_content".equals(key)) {
         return this.payloadTabIndex == 0;
      } else if ("payload_hex_view".equals(key)) {
         return this.payloadTabIndex == 1;
      } else if ("payload_utf8_view".equals(key)) {
         return this.payloadTabIndex == 2;
      } else {
         return "payload_logical_view".equals(key) ? this.payloadTabIndex == 3 : true;
      }
   }

   @Override
   public boolean mouseReleased(double mx, double my, int button) {
      this.variableWarningStale = true;
      if (this.packetSelector.isVisible() && this.packetSelector.mouseReleased(mx, my, button)) {
         return true;
      }

      if (this.raceStepSelector.isVisible() && this.raceStepSelector.mouseReleased(mx, my, button)) {
         return true;
      }

      boolean consumed = false;

      for (ScrollableListView vp : this.entryViewports.values()) {
         if (vp.isDraggingScrollbar()) {
            vp.mouseReleased();
            consumed = true;
         }
      }

      for (ScrollableListView vp : this.listViewports.values()) {
         if (vp.isDraggingScrollbar()) {
            vp.mouseReleased();
            consumed = true;
         }
      }

      if (this.activeScrollbarDrag != null) {
         this.activeScrollbarDrag = null;
         consumed = true;
      }

      if (this.draggingMatchStrength) {
         this.draggingMatchStrength = false;
         consumed = true;
      }

      if (this.draggingSmoothness) {
         this.draggingSmoothness = false;
         consumed = true;
      }

      if (this.draggingSafeDelay) {
         this.draggingSafeDelay = false;
         consumed = true;
      }

      if (this.dragging) {
         this.dragging = false;
         consumed = true;
      }

      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns)) {
         return true;
      }

      for (Entry<String, ThemedTextInputBox> entry : this.textFields.entrySet()) {
         if (this.isPayloadFieldVisible(entry.getKey()) && entry.getValue().mouseReleased(mx, my, button)) {
            consumed = true;
         }
      }

      for (ThemedTextInputBox field : this.internalTextFields.values()) {
         if (field.mouseReleased(mx, my, button)) {
            consumed = true;
         }
      }

      return consumed;
   }

   @Override
   public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
      this.variableWarningStale = true;
      if (this.packetSelector.isVisible() && this.packetSelector.mouseDragged(mx, my, button, dx, dy)) {
         return true;
      }

      if (this.raceStepSelector.isVisible() && this.raceStepSelector.mouseDragged(mx, my, button, dx, dy)) {
         return true;
      }

      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, mx, my, button)) {
         return true;
      }

      if (this.draggingMatchStrength) {
         this.dragMatchStrengthSlider((int)mx);
         return true;
      }

      if (this.draggingSmoothness) {
         this.dragSmoothnessSlider((int)mx);
         return true;
      }

      if (this.draggingSafeDelay) {
         this.dragSafeDelaySlider((int)mx);
         return true;
      }

      if (this.dragging) {
         this.editorX = (int)(mx - this.dragOffsetX);
         this.editorY = (int)(my - this.dragOffsetY);
         OverlayBounds c = this.clampCurrentBounds(this);
         this.editorX = c.x;
         this.editorY = c.y;
         return true;
      }

      for (Entry<String, ThemedTextInputBox> entry : this.textFields.entrySet()) {
         if (this.isPayloadFieldVisible(entry.getKey()) && entry.getValue().mouseDragged(mx, my, button, dx, dy)) {
            return true;
         }
      }

      for (ThemedTextInputBox field : this.internalTextFields.values()) {
         if (field.mouseDragged(mx, my, button, dx, dy)) {
            return true;
         }
      }

      for (ScrollableListView vp : this.entryViewports.values()) {
         if (vp.isDraggingScrollbar()) {
            vp.mouseDragged(mx, my);
            return true;
         }
      }

      for (ScrollableListView vp : this.listViewports.values()) {
         if (vp.isDraggingScrollbar()) {
            vp.mouseDragged(mx, my);
            return true;
         }
      }

      if (this.activeScrollbarDrag != null) {
         this.activeScrollbarDrag.accept((int)my);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      this.variableWarningStale = true;
      if (this.packetSelector.isVisible()) {
         return this.packetSelector.mouseScrolled(mx, my, amount);
      }

      if (this.raceStepSelector.isVisible()) {
         return this.raceStepSelector.mouseScrolled(mx, my, amount);
      }

      if (this.visible && this.containsPoint(mx, my)) {
         if (!this.dropdowns.isEmpty()
            && (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns) || autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.dropdowns, mx, my))
            && autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, mx, my, amount)) {
            return true;
         }

         boolean bodyScrollAllowed = this.bodyClip == null || this.bodyClip.init_Z((int)mx, (int)my);
         if (bodyScrollAllowed) {
            for (Entry<String, ThemedTextInputBox> entry : this.textFields.entrySet()) {
               if (this.isPayloadFieldVisible(entry.getKey()) && entry.getValue().mouseScrolled(mx, my, amount)) {
                  return true;
               }
            }

            for (ThemedTextInputBox field : this.internalTextFields.values()) {
               if (field.mouseScrolled(mx, my, amount)) {
                  return true;
               }
            }

            for (Entry<String, ScrollableListView> entry : this.entryViewports.entrySet()) {
               ScrollableListView vp = entry.getValue();
               if (vp.containsPoint(mx, my)) {
                  vp.mouseScrolled(mx, my, amount);
                  return true;
               }
            }

            for (Entry<String, ScrollableListView> entry : this.listViewports.entrySet()) {
               ScrollableListView vp = entry.getValue();
               if (vp.containsPoint(mx, my)) {
                  vp.mouseScrolled(mx, my, amount);
                  return true;
               }
            }
         }

         if (this.recipeListBounds != null && my >= this.recipeListBounds[0] && my < this.recipeListBounds[0] + this.recipeListBounds[1]) {
            this.recipeListScroll = Math.max(0, this.recipeListScroll - (int)(amount * 13.0));
            return true;
         } else {
            this.scrollOffset = Math.max(0, this.scrollOffset - (int)(amount * 12.0));
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.variableWarningStale = true;
      if (!this.visible) {
         return false;
      }

      if (this.packetSelector.isVisible()) {
         return this.packetSelector.keyPressed(keyCode, scanCode, modifiers);
      }

      if (this.raceStepSelector.isVisible()) {
         return this.raceStepSelector.keyPressed(keyCode, scanCode, modifiers);
      }

      if (!autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
         if (keyCode == 256) {
            if (this.poll_Z_2()) {
               return true;
            } else if (this.hasFocusedTextInput()) {
               this.clearInputFocus();
               return true;
            } else {
               this.finishEditing(false);
               return true;
            }
         } else {
            KeyEvent ki = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);

            for (ThemedTextInputBox f : this.textFields.values()) {
               if (f.isFocused()) {
                  return f.keyPressed(ki);
               }
            }

            for (ThemedTextInputBox f : this.internalTextFields.values()) {
               if (f.isFocused()) {
                  return f.keyPressed(ki);
               }
            }

            return false;
         }
      } else {
         if (keyCode == 256 || keyCode == 257) {
            autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      this.variableWarningStale = true;
      if (!this.visible) {
         return false;
      }

      if (this.packetSelector.isVisible()) {
         return this.packetSelector.charTyped(chr, modifiers);
      }

      if (this.raceStepSelector.isVisible()) {
         return this.raceStepSelector.charTyped(chr, modifiers);
      }

      CharacterEvent ci = new CharacterEvent(chr);

      for (ThemedTextInputBox f : this.textFields.values()) {
         if (f.isFocused() && f.charTyped(ci)) {
            return true;
         }
      }

      for (ThemedTextInputBox f : this.internalTextFields.values()) {
         if (f.isFocused() && f.charTyped(ci)) {
            return true;
         }
      }

      return false;
   }

   private boolean isFieldApplicable(FieldDef field) {
      if (field == null) {
         return false;
      }

      if (this.editingAction instanceof ToolsAction) {
         String key = field.key();
         boolean run = "RUN".equals(this.enumValue("operation"));
         if ((key.startsWith("toolArg.") || "position".equals(key) || "waypoint".equals(key)) && !run) {
            return false;
         }

         if ("position".equals(key)) {
            return "CUSTOM".equals(this.enumValue("destination")) || "OFFSET".equals(this.enumValue("destination"));
         }

         if ("waypoint".equals(key)) {
            return "WAYPOINT".equals(this.enumValue("destination"));
         }
      }

      if ("safeClickDelayTicks".equals(field.key())) {
         return this.isSafeClickXCarry();
      }

      if ("safeClickDelayAfterPickup".equals(field.key()) || "safeClickDelayBeforeReturn".equals(field.key())) {
         return this.isSafeClickXCarry();
      }

      if (this.isGuiNameKey(field.key()) && this.editingAction instanceof GuiWaitAware) {
         return this.waitsForGui();
      }

      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY) {
         String mode = this.enumValue("mode");
         if ("waitForGuiBefore".equals(field.key()) || "waitForGuiAfter".equals(field.key())) {
            return "OPEN".equals(mode);
         }

         if ("sendPacket".equals(field.key())) {
            return "CLOSE".equals(mode);
         }
      }

      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_ENTITY) {
         String checkMode = this.enumValue("checkMode");
         if ("centerOnPlayer".equals(field.key()) || "radius".equals(field.key()) || "mustBeLookingAt".equals(field.key())) {
            return "RADIUS".equals(checkMode) || "NEARBY".equals(checkMode) && !"mustBeLookingAt".equals(field.key());
         }

         if ("pos".equals(field.key())) {
            return ("RADIUS".equals(checkMode) || "NEARBY".equals(checkMode)) && !this.toggleStates.getOrDefault("centerOnPlayer", false);
         }
      }

      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY_AUDIT) {
         String mode = this.enumValue("mode");
         String openMode = this.enumValue("openMode");
         boolean isDupeMode = "DUPE".equals(mode) || "DUPE_SPAM".equals(mode);
         boolean isDupeSpam = "DUPE_SPAM".equals(mode);
         if ("targetItems".equals(field.key())) {
            return isDupeMode;
         }

         if ("openCommand".equals(field.key())) {
            return isDupeMode && "COMMAND".equals(openMode);
         }

         if ("containerPos".equals(field.key())) {
            return isDupeMode && "CONTAINER".equals(openMode);
         }

         if ("spamCount".equals(field.key()) || "spamDelayMs".equals(field.key())) {
            return isDupeSpam;
         }

         if ("openMode".equals(field.key())
            || "dupeVector".equals(field.key())
            || "iterations".equals(field.key())
            || "maxTransferAttempts".equals(field.key())
            || "transferRetryDelayMs".equals(field.key())) {
            return isDupeMode;
         }
      }

      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PACKET_GATE && "DISABLE_GATE".equals(this.enumValue("mode"))) {
         String key = field.key();
         return "mode".equals(key) || "gateId".equals(key) || "flushOnDisable".equals(key);
      }

      if (this.editingAction != null
         && (this.editingAction.getType() == MacroActionType.FPS || this.editingAction.getType() == MacroActionType.SPING)) {
         String endMode = this.enumValue("endMode");
         boolean timeout = endMode.isEmpty() || "TIMEOUT".equals(endMode);
         boolean steps = "STEPS".equals(endMode);
         boolean useTicks = this.toggleStates.getOrDefault("useTicks", false);
         String key = field.key();
         if ("stepCount".equals(key)) {
            return steps;
         }

         if ("useTicks".equals(key)) {
            return timeout;
         }

         if ("durationMs".equals(key)) {
            return timeout && !useTicks;
         }

         if ("durationTicks".equals(key)) {
            return timeout && useTicks;
         }

         if ("continueNextActions".equals(key)) {
            return timeout;
         }
      }

      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CAPTURE_VALUE) {
         String key = field.key();
         if ("listFilterText".equals(key) || "listStripPrefix".equals(key) || "listPickPosition".equals(key)) {
            String source = this.enumValue("source");
            if (!"COMMAND_AUTOFILL".equals(source) && !"TABLIST".equals(source)) {
               return false;
            }

            String filter = this.enumValue("listFilter");
            if ("listFilterText".equals(key)) {
               return !"NONE".equals(filter);
            }

            if (!"listStripPrefix".equals(key)) {
               return "POSITION".equals(this.enumValue("listSelection"));
            }

            return "PREFIX".equals(filter) || "SUFFIX".equals(filter);
         }
      }

      if (!field.hasShowWhen()) {
         return true;
      }

      boolean cond;
      if (field.showWhenValue() != null && !field.showWhenValue().isEmpty()) {
         String currentValue = this.enumValue(field.showWhenKey());
         cond = false;

         for (String allowed : field.showWhenValue().split("\\|")) {
            if (allowed.equals(currentValue)) {
               cond = true;
               break;
            }
         }
      } else {
         cond = this.toggleStates.getOrDefault(field.showWhenKey(), false);
      }

      boolean visible = field.showWhenInverted() ? !cond : cond;
      if (visible && !field.showWhenInverted()) {
         FieldDef dep = this.fieldByKey(field.showWhenKey());
         if (dep != null && !this.isFieldApplicable(dep)) {
            visible = false;
         }
      }

      return visible;
   }

   private boolean isSafeClickXCarry() {
      return this.editingAction != null
         && this.editingAction.getType() == MacroActionType.XCARRY
         && "PUT_IN".equals(this.enumValue("mode"))
         && "SAFE_CLICK".equals(this.enumValue("transferMode"));
   }

   private boolean isGuiNameKey(String key) {
      return "guiName".equals(key) || "waitGuiName".equals(key);
   }

   private boolean waitsForGui() {
      return this.toggleStates.getOrDefault("waitForGuiBefore", false)
         || this.toggleStates.getOrDefault("waitForGuiAfter", false)
         || this.toggleStates.getOrDefault("waitForGui", false);
   }

   private String enumValue(String key) {
      if (this.fieldSchema == null) {
         return "";
      }

      for (FieldDef field : this.fieldSchema.fields()) {
         if (field.key().equals(key) && field.type() == FieldType.ENUM) {
            List<String> opts = field.enumOptions();
            if (opts.isEmpty()) {
               return "";
            }

            int idx = Math.min(this.modeIndices.getOrDefault(key, 0), opts.size() - 1);
            return opts.get(idx);
         }
      }

      return "";
   }

   private void loadItemTargetFields(MacroAction action) {
      if (action != null && this.actionTag != null) {
         if (action instanceof SelectSlotAction selectSlotAction) {
            this.loadItemTargetField("itemName", selectSlotAction.itemTarget, selectSlotAction.itemName, null);
         }

         if (action instanceof UseItemAction useItemAction) {
            this.loadItemTargetField("itemName", useItemAction.itemTarget, useItemAction.itemName, "slot");
            if (useItemAction.slot >= 0) {
               this.actionTag.putInt("slot", useItemAction.slot);
            }
         }

         if (action instanceof WaitForCooldownAction waitForCooldownAction) {
            this.loadItemTargetField("itemName", waitForCooldownAction.itemTarget, waitForCooldownAction.itemName, null);
         }

         if (action instanceof autismclient.util.macro.CloseGuiAction closeGuiAction) {
            this.loadItemTargetField("itemName", closeGuiAction.itemTarget, closeGuiAction.itemName, "targetSlot");
         }

         if (action instanceof SwapSlotsAction swapSlotsAction) {
            this.loadItemTargetField("fromItemName", swapSlotsAction.fromItemTarget, swapSlotsAction.fromItemName, null);
            this.loadItemTargetField("toItemName", swapSlotsAction.toItemTarget, swapSlotsAction.toItemName, null);
         }

         if (action instanceof StoreItemAction storeItemAction) {
            this.loadItemTargetListField("targetItems", storeItemAction.itemTargets, storeItemAction.targetItems);
         }

         if (action instanceof XCarryAction xCarryAction) {
            this.loadItemTargetListField("entries", xCarryAction.entryTargets, xCarryAction.entries);
            this.actionTag.putInt("safeClickDelayTicks", XCarryAction.write(xCarryAction.safeClickDelayTicks));
            this.actionTag.putBoolean("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
            this.actionTag.putBoolean("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
            ListTag destinations = new ListTag();
            xCarryAction.resizeEntryDestinations(xCarryAction.entries.size());
            xCarryAction.resizeEntryAmounts(xCarryAction.entries.size());
            ListTag amountModes = new ListTag();
            ListTag amounts = new ListTag();

            for (int i = 0; i < xCarryAction.entries.size(); i++) {
               destinations.add(StringTag.valueOf(String.valueOf(xCarryAction.getEntryDestination(i))));
               amountModes.add(StringTag.valueOf(xCarryAction.getEntryAmountMode(i).name()));
               amounts.add(StringTag.valueOf(String.valueOf(xCarryAction.getEntryAmount(i))));
            }

            this.actionTag.put("entryDestinations", destinations);
            this.actionTag.put("entryAmountModes", amountModes);
            this.actionTag.put("entryAmounts", amounts);
         }

         if (action instanceof InventoryAuditAction inventoryAuditAction) {
            this.loadItemTargetListField("targetItems", inventoryAuditAction.itemTargets, inventoryAuditAction.targetItems);
         }

         if (action instanceof PlaceAction placeAction) {
            this.loadItemTargetField("itemName", placeAction.itemTarget, placeAction.itemName, null);
            this.clearAutoPlaceTagKeys();
         }
      }
   }

   private void clearAutoPlaceTagKeys() {
      if (this.actionTag != null) {
         this.actionTag.remove("slot");
         if (!this.actionTag.getBooleanOr("manualDirection", false)) {
            this.actionTag.remove("direction");
         }
      }
   }

   private MacroItemTarget placeItemTarget() {
      ThemedTextInputBox nameF = this.textFields.get("itemName");
      ThemedTextInputBox slotF = this.textFields.get("place_itemSlot");
      return this.parseItemTarget(nameF == null ? "" : nameF.getValue(), slotF == null ? "" : slotF.getValue(), this.itemTargets.get("itemName"));
   }

   private void syncPlaceItemTarget() {
      if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
         MacroItemTarget target = this.placeItemTarget();
         if (!target.hasSlot() && !target.hasItemFilter()) {
            this.itemTargets.remove("itemName");
         } else {
            this.itemTargets.put("itemName", target.copy());
         }
      }
   }

   private void loadItemTargetField(String key, MacroItemTarget source, String legacyValue, String slotKey) {
      MacroItemTarget resolved = this.resolveItemTarget(source, legacyValue);
      if (resolved.hasSlot() || resolved.hasItemFilter()) {
         this.itemTargets.put(key, resolved.copy());
         this.actionTag.putString(key, resolved.rawLabel());
         if (slotKey != null && resolved.hasSlot()) {
            this.actionTag.putInt(slotKey, resolved.slot);
         }
      }
   }

   private void attachAllItemTargetStyling() {
      this.attachItemTargetStyling("itemName");
      this.attachItemTargetStyling("fromItemName");
      this.attachItemTargetStyling("toItemName");
      this.attachInternalItemTargetStyling("_item_add");
      this.attachInternalItemTargetStyling("_drop_add");
      this.attachInternalItemTargetStyling("_wsc_add");
      this.attachInternalItemTargetStyling("targetItems");
   }

   private void attachItemTargetStyling(String key) {
      ThemedTextInputBox field = this.textFields.get(key);
      if (field != null) {
         field.setDisplayTextProvider(value -> this.styledItemTargetText(key, value));
      }
   }

   private void attachInternalItemTargetStyling(String key) {
      ThemedTextInputBox field = this.internalTextFields.get(key);
      if (field != null) {
         field.setDisplayTextProvider(value -> this.styledInternalItemTargetText(key, value));
      }
   }

   private Component styledItemTargetText(String key, String value) {
      return this.restyleWithTarget(this.itemTargets.get(key), value);
   }

   private Component styledInternalItemTargetText(String key, String value) {
      Component var10000;
      switch (key) {
         case "_item_add":
            var10000 = this.restyleWithTarget(
               this.itemActionDraft != null && this.selectedItemEntry >= 0 && this.selectedItemEntry < this.itemActionDraft.itemNames.size()
                  ? this.itemTargetAt(this.itemActionDraft.itemTargets, this.itemActionDraft.itemNames, this.selectedItemEntry)
                  : null,
               value
            );
            break;
         case "_drop_add":
            var10000 = this.restyleWithTarget(
               this.dropActionDraft != null && this.selectedDropEntry >= 0 && this.selectedDropEntry < this.dropActionDraft.itemNames.size()
                  ? this.itemTargetAt(this.dropActionDraft.itemTargets, this.dropActionDraft.itemNames, this.selectedDropEntry)
                  : null,
               value
            );
            break;
         case "_wsc_add":
            var10000 = this.restyleWithTarget(
               this.slotChangeEntries != null && this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < this.slotChangeEntries.size() ? this.slotChangeEntries.get(this.selectedSlotChangeEntry).resolveItemTarget() : null, value
            );
            break;
         case "targetItems":
            var10000 = this.restyleWithTarget(this.selectedStoreAuditTarget(), value);
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   private MacroItemTarget selectedStoreAuditTarget() {
      int storeIndex = this.listEditIndices.getOrDefault("store_items_selected", -1);
      int auditIndex = this.listEditIndices.getOrDefault("audit_items_selected", -1);
      int index = storeIndex >= 0 ? storeIndex : auditIndex;
      if (index < 0) {
         return null;
      }

      List<MacroItemTarget> targets = this.readItemTargetList("targetItems");
      if (index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.hasSlot() || target.hasItemFilter())) {
            return target;
         }
      }

      return null;
   }

   private Component restyleWithTarget(MacroItemTarget target, String value) {
      if (target == null) {
         return null;
      }

      Component rich = target.restyleText(value);
      if (rich == null) {
         return null;
      }

      String safeValue = value == null ? "" : value;
      return safeValue.equals(rich.getString()) ? rich.copy() : null;
   }

   private void loadItemTargetListField(String key, List<MacroItemTarget> source, List<String> legacyValues) {
      List<MacroItemTarget> resolved = this.resolveItemTargets(source, legacyValues);
      if (!resolved.isEmpty()) {
         this.itemTargetLists.put(key, MacroItemTarget.copyList(resolved));
         ListTag listTag = new ListTag();

         for (MacroItemTarget target : resolved) {
            if (target != null) {
               String entry = target.toSpecString();
               if (!entry.isBlank()) {
                  listTag.add(StringTag.valueOf(entry));
               }
            }
         }

         this.actionTag.put(key, listTag);
      }
   }

   private MacroItemTarget resolveItemTarget(MacroItemTarget source, String legacyValue) {
      return source != null && (source.hasSlot() || source.hasItemFilter()) ? source.copy() : MacroItemTarget.parse(legacyValue);
   }

   private List<MacroItemTarget> resolveItemTargets(List<MacroItemTarget> source, List<String> legacyValues) {
      List<MacroItemTarget> resolved = MacroItemTarget.copyList(source);
      if (!resolved.isEmpty()) {
         return resolved;
      }

      List<MacroItemTarget> parsed = new ArrayList<>();
      if (legacyValues == null) {
         return parsed;
      }

      for (String legacyValue : legacyValues) {
         MacroItemTarget target = MacroItemTarget.parse(legacyValue);
         if (target.hasSlot() || target.hasItemFilter()) {
            parsed.add(target);
         }
      }

      return parsed;
   }

   private MacroItemTarget readItemTargetField(String key, String slotKey) {
      ThemedTextInputBox valueField = this.textFields.get(key);
      String text = valueField == null ? "" : valueField.getValue().strip();
      MacroItemTarget previous = this.itemTargets.get(key);
      int slot = -1;
      boolean slotDriven = false;
      if (slotKey != null) {
         ThemedTextInputBox slotField = this.textFields.get(slotKey);
         slot = slotField == null ? -1 : this.parseDigits(slotField.getValue(), -1);
         slotDriven = true;
      } else if (previous != null && previous.hasSlot()) {
         slot = previous.slot;
      }

      if (!text.isEmpty() || slotDriven && slot >= 0) {
         if (previous != null && text.equals(previous.rawLabel())) {
            MacroItemTarget preserved = previous.copy();
            if (slotDriven) {
               preserved.slot = slot;
            }

            return preserved;
         } else if (previous != null && previous.hasTextJson() && !text.isEmpty()) {
            MacroItemTarget preserved = previous.withStyledDisplay(text);
            if (slot >= 0) {
               preserved.slot = slot;
            } else if (slotDriven) {
               preserved.slot = -1;
            }

            return preserved;
         } else {
            String raw;
            if (slot >= 0 && !text.isEmpty()) {
               raw = "#" + slot + "|" + text;
            } else if (slot >= 0) {
               raw = "#" + slot;
            } else {
               raw = text;
            }

            return MacroItemTarget.parse(raw);
         }
      } else {
         return new MacroItemTarget();
      }
   }

   private List<MacroItemTarget> readItemTargetList(String key) {
      List<String> values = this.listFields.getOrDefault(key, Collections.emptyList());
      List<MacroItemTarget> previous = this.itemTargetLists.getOrDefault(key, Collections.emptyList());
      boolean[] used = new boolean[previous.size()];
      List<MacroItemTarget> rebuilt = new ArrayList<>();

      for (String value : values) {
         String entry = value == null ? "" : value.strip();
         if (!entry.isEmpty()) {
            MacroItemTarget preserved = null;

            for (int i = 0; i < previous.size(); i++) {
               MacroItemTarget candidate = previous.get(i);
               if (!used[i] && candidate != null && entry.equals(candidate.toSpecString())) {
                  preserved = candidate.copy();
                  used[i] = true;
                  break;
               }
            }

            if (preserved == null) {
               preserved = MacroItemTarget.parse(entry);
            }

            if (this.isXCarryEntriesKey(key) && preserved.hasItemFilter()) {
               preserved.slot = -1;
            }

            if (preserved.hasSlot() || preserved.hasItemFilter()) {
               rebuilt.add(preserved);
            }
         }
      }

      this.itemTargetLists.put(key, MacroItemTarget.copyList(rebuilt));
      return rebuilt;
   }

   private void storeItemTargetFields() {
      if (this.editingAction != null && this.actionTag != null) {
         if (this.editingAction instanceof SelectSlotAction) {
            this.storeItemTarget("itemName", this.readItemTargetField("itemName", null));
         }

         if (this.editingAction instanceof UseItemAction) {
            this.storeItemTarget("itemName", this.readItemTargetField("itemName", "slot"));
         }

         if (this.editingAction instanceof WaitForCooldownAction) {
            this.storeItemTarget("itemName", this.readItemTargetField("itemName", null));
         }

         if (this.editingAction instanceof autismclient.util.macro.CloseGuiAction) {
            this.storeItemTarget("itemName", this.readItemTargetField("itemName", "targetSlot"));
         }

         if (this.editingAction instanceof SwapSlotsAction) {
            this.storeItemTarget("fromItemName", this.readItemTargetField("fromItemName", null));
            this.storeItemTarget("toItemName", this.readItemTargetField("toItemName", null));
         }

         if (this.editingAction instanceof PlaceAction) {
            this.storeItemTarget("itemName", this.placeItemTarget());
            this.actionTag.remove("slot");
         }

         if (this.editingAction instanceof StoreItemAction) {
            this.storeItemTargets("targetItems", this.readItemTargetList("targetItems"));
         }

         if (this.editingAction instanceof XCarryAction) {
            this.storeItemTargets("entries", this.readItemTargetList("entries"));
            int count = this.listFields.getOrDefault("entries", Collections.emptyList()).size();
            this.resizeXCarryDestinations(count);
            this.resizeXCarryAmountModes(count);
         }

         if (this.editingAction instanceof InventoryAuditAction) {
            this.storeItemTargets("targetItems", this.readItemTargetList("targetItems"));
         }
      }
   }

   private void storeItemTarget(String key, MacroItemTarget target) {
      if (target == null || !target.hasSlot() && !target.hasItemFilter()) {
         this.actionTag.remove(key);
         this.itemTargets.remove(key);
      } else {
         this.actionTag.put(key, target.toTag());
         this.itemTargets.put(key, target.copy());
      }
   }

   private void storeItemTargets(String key, List<MacroItemTarget> targets) {
      this.actionTag.put(key, MacroItemTarget.toListTag(targets));
   }

   private void resizeEditorToContent() {
      this.valid_List.clear();
      this.scrollbarRegions.clear();
      this.refreshVariableWarning();
      int neededH = 18 + this.itemEditorHeight() + this.footerHeight() + 4;
      int minH = this.defaultWindowHeight();
      int maxH = Math.max(minH, cA.load_I() * 4 / 5);
      this.editorHeight = Math.max(minH, Math.min(maxH, neededH));
      OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.editorX, this.editorY, this.editorWidth, this.editorHeight, this.visible, this.windowCollapsed));
      this.editorX = clamped.x;
      this.editorY = clamped.y;
      this.editorWidth = clamped.width;
      this.editorHeight = clamped.height;
   }

   private void toggleSlotChangeSelection(int index) {
      if (this.selectedSlotChangeEntry == index) {
         this.selectedSlotChangeEntry = -1;
         this.clearSlotChangeInputs();
      } else {
         this.selectedSlotChangeEntry = index;
         if (this.slotChangeEntries != null && index < this.slotChangeEntries.size()) {
            this.loadSlotChangeEntry(this.slotChangeEntries.get(index));
         }
      }
   }

   private void loadSlotChangeEntry(WaitForSlotChangeAction.Inner_DataUtil e) {
      ThemedTextInputBox addF = this.internalTextFields.get("_wsc_add");
      ThemedTextInputBox slotF = this.textFields.get("wsc_slot");
      ThemedTextInputBox countF = this.textFields.get("wsc_count");
      if (addF != null && slotF != null) {
         this.suppressSlotChangeListener = true;
         MacroItemTarget target = e.resolveItemTarget();
         addF.setValue(target.rawLabel());
         slotF.setValue(target.hasSlot() ? String.valueOf(target.slot) : "");
         if (countF != null) {
            countF.setValue(String.valueOf(Math.max(1, e.count)));
         }

         this.suppressSlotChangeListener = false;
      }
   }

   private void clearSlotChangeInputs() {
      ThemedTextInputBox addF = this.internalTextFields.get("_wsc_add");
      ThemedTextInputBox slotF = this.textFields.get("wsc_slot");
      ThemedTextInputBox countF = this.textFields.get("wsc_count");
      this.suppressSlotChangeListener = true;
      if (addF != null) {
         addF.setValue("");
      }

      if (slotF != null) {
         slotF.setValue("");
      }

      if (countF != null) {
         countF.setValue("");
      }

      this.suppressSlotChangeListener = false;
   }

   private void applySlotChangeEntryEdit() {
      if (!this.suppressSlotChangeListener && this.slotChangeEntries != null && this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < this.slotChangeEntries.size()) {
         ThemedTextInputBox addF = this.internalTextFields.get("_wsc_add");
         ThemedTextInputBox slotF = this.textFields.get("wsc_slot");
         if (addF != null && slotF != null) {
            String nameText = addF.getValue().strip();
            String slotText = slotF.getValue().strip();
            WaitForSlotChangeAction.Inner_DataUtil entry = this.slotChangeEntries.get(this.selectedSlotChangeEntry);
            MacroItemTarget target = this.parseItemTarget(nameText, slotText, entry.resolveItemTarget());
            String rawTarget = target.toSpecString();
            String norm = rawTarget.isEmpty() ? "" : StoreItemAction.normalizeTarget(rawTarget);
            if (norm == null) {
               norm = "";
            }

            if (!this.hasSlotChangeTargetExcept(norm, this.selectedSlotChangeEntry)) {
               entry.target = norm;
               entry.itemTarget = target;
            }
         }
      }
   }

   private void applySlotChangeCount() {
      if (!this.suppressSlotChangeListener && this.slotChangeEntries != null) {
         ThemedTextInputBox countF = this.textFields.get("wsc_count");
         if (countF != null) {
            int count;
            try {
               count = Integer.parseInt(countF.getValue().strip());
            } catch (NumberFormatException ignored) {
               return;
            }

            count = Math.max(1, count);
            if (this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < this.slotChangeEntries.size()) {
               this.slotChangeEntries.get(this.selectedSlotChangeEntry).count = count;
            } else {
               this.slotChangeCount = count;
            }
         }
      }
   }

   private void addSlotChangeEntry(ThemedTextInputBox addF, ThemedTextInputBox slotF) {
      if (this.slotChangeEntries != null) {
         String nameText = addF.getValue().strip();
         String slotText = slotF.getValue().strip();
         MacroItemTarget target = this.parseItemTarget(nameText, slotText, null);
         String rawTarget = target.toSpecString();
         String norm = rawTarget.isEmpty() ? "" : StoreItemAction.normalizeTarget(rawTarget);
         if (norm == null) {
            norm = "";
         }

         if (!this.hasSlotChangeTarget(norm)) {
            WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil(norm, this.slotChangeCondition, this.slotChangeCount);
            entry.itemTarget = target;
            entry.target = norm;
            this.slotChangeEntries.add(entry);
            addF.setValue("");
            slotF.setValue("");
         }
      }
   }

   private boolean hasSlotChangeTarget(String normTarget) {
      if (this.slotChangeEntries == null) {
         return false;
      }

      for (WaitForSlotChangeAction.Inner_DataUtil e : this.slotChangeEntries) {
         if (e.target.equals(normTarget)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasSlotChangeTargetExcept(String normTarget, int ignoreIdx) {
      if (this.slotChangeEntries == null) {
         return false;
      }

      for (int i = 0; i < this.slotChangeEntries.size(); i++) {
         if (i != ignoreIdx && this.slotChangeEntries.get(i).target.equals(normTarget)) {
            return true;
         }
      }

      return false;
   }

   private boolean isSlotChangeTargetsKey(String key) {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SLOT_CHANGE && "targetEntries".equals(key);
   }

   private boolean isStoreItemTargetsKey(String key) {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.STORE_ITEM && "targetItems".equals(key);
   }

   private boolean isAuditTargetsKey(String key) {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY_AUDIT && "targetItems".equals(key);
   }

   private boolean isItemTargetsKey(String key) {
      return this.isStoreItemTargetsKey(key) || this.isAuditTargetsKey(key);
   }

   private String formatSlotEntry(String nameText, String slotText) {
      String trimmedName = nameText == null ? "" : nameText.trim();
      int slot = this.parseDigits(slotText, -1);
      if (!trimmedName.isEmpty() && slot >= 0) {
         return "#" + slot + "|" + trimmedName;
      } else if (!trimmedName.isEmpty()) {
         return trimmedName;
      } else {
         return slot >= 0 ? "#" + slot : null;
      }
   }

   private MacroItemTarget targetFromSlot(Slot slot, String itemName, String registryId, int visibleSlot) {
      if (slot != null && slot.getItem() != null && !slot.getItem().isEmpty()) {
         return MacroItemTarget.fromItemStack(slot.getItem(), visibleSlot);
      }

      if (visibleSlot >= 0) {
         return MacroItemTarget.ofSlot(visibleSlot);
      }

      String fallback = registryId != null && !registryId.isBlank() ? registryId.trim() : (itemName == null ? "" : itemName.trim());
      return MacroItemTarget.parse(fallback);
   }

   private static MacroItemTarget withoutSlot(MacroItemTarget target) {
      if (target == null) {
         return new MacroItemTarget();
      }

      if (!target.hasSlot()) {
         return target;
      }

      MacroItemTarget copy = target.copy();
      copy.slot = -1;
      return copy;
   }

   private static MacroItemTarget stripSlotIfFiltered(MacroItemTarget target) {
      if (target == null) {
         return new MacroItemTarget();
      } else {
         return target.hasItemFilter() ? withoutSlot(target) : target.copy();
      }
   }

   private MacroItemTarget parseItemTarget(String nameText, String slotText, MacroItemTarget previous) {
      String trimmedName = nameText == null ? "" : nameText.strip();
      int slot = this.parseDigits(slotText, -1);
      if (trimmedName.isEmpty() && slot < 0) {
         return new MacroItemTarget();
      }

      if (previous != null && trimmedName.equals(previous.rawLabel())) {
         MacroItemTarget kept = previous.copy();
         kept.slot = slot;
         return kept;
      }

      if (previous != null && previous.hasTextJson() && !trimmedName.isEmpty()) {
         MacroItemTarget preserved = previous.withStyledDisplay(trimmedName);
         preserved.slot = slot;
         return preserved;
      }

      String raw;
      if (slot >= 0 && !trimmedName.isEmpty()) {
         raw = "#" + slot + "|" + trimmedName;
      } else if (slot >= 0) {
         raw = "#" + slot;
      } else {
         raw = trimmedName;
      }

      return MacroItemTarget.parse(raw);
   }

   private MacroItemTarget itemTargetAt(List<MacroItemTarget> targets, List<String> legacyEntries, int index) {
      if (targets != null && index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.hasSlot() || target.hasItemFilter())) {
            return target;
         }
      }

      return legacyEntries != null && index >= 0 && index < legacyEntries.size() ? MacroItemTarget.parse(legacyEntries.get(index)) : new MacroItemTarget();
   }

   private void addCapturedTarget(String key, List<String> entries, MacroItemTarget capturedTarget) {
      if (key != null && entries != null && capturedTarget != null) {
         String normalized = capturedTarget.toSpecString();
         if (this.isItemTargetsKey(key) || this.isSlotChangeTargetsKey(key)) {
            normalized = StoreItemAction.normalizeTarget(normalized);
         } else if (this.isXCarryEntriesKey(key)) {
            normalized = this.normalizeItemEntry(normalized);
         }

         if (normalized != null && !normalized.isBlank()) {
            int index = entries.indexOf(normalized);
            if (index >= 0) {
               List<MacroItemTarget> targets = this.readItemTargetList(key);

               while (targets.size() < entries.size()) {
                  targets.add(new MacroItemTarget());
               }

               MacroItemTarget stored = this.isXCarryEntriesKey(key) ? withoutSlot(capturedTarget) : capturedTarget.copy();
               if (!normalized.equals(stored.toSpecString())) {
                  MacroItemTarget normalizedTarget = MacroItemTarget.parse(normalized);
                  stored.slot = normalizedTarget.slot;
                  if (!normalizedTarget.hasSlot() && !stored.hasItemFilter()) {
                     stored = normalizedTarget;
                  }
               }

               targets.set(index, stored);
               this.itemTargetLists.put(key, MacroItemTarget.copyList(targets));
            }
         }
      }
   }

   private void setItemTargetAt(List<MacroItemTarget> targets, List<String> legacyEntries, int index, MacroItemTarget target) {
      if (targets != null && legacyEntries != null && index >= 0) {
         while (targets.size() <= index) {
            targets.add(new MacroItemTarget());
         }

         while (legacyEntries.size() <= index) {
            legacyEntries.add("");
         }

         MacroItemTarget stored = target == null ? new MacroItemTarget() : target;
         targets.set(index, stored);
         legacyEntries.set(index, stored.toSpecString());
      }
   }

   private void appendItemTarget(List<MacroItemTarget> targets, List<String> legacyEntries, MacroItemTarget target) {
      if (targets != null && legacyEntries != null && target != null) {
         targets.add(target);
         legacyEntries.add(target.toSpecString());
      }
   }

   private void resizeItemTargets(List<MacroItemTarget> targets, int size) {
      if (targets != null) {
         while (targets.size() > size) {
            targets.remove(targets.size() - 1);
         }

         while (targets.size() < size) {
            targets.add(new MacroItemTarget());
         }
      }
   }

   private boolean isTargetListKey(String key) {
      return this.isXCarryEntriesKey(key) || this.isStoreItemTargetsKey(key) || this.isAuditTargetsKey(key) || this.isSlotChangeTargetsKey(key);
   }

   private boolean isXCarryEntriesKey(String key) {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.XCARRY && "entries".equals(key);
   }

   private boolean isEntityTargetsKey(String key) {
      return this.editingAction != null
         && (this.editingAction.getType() == MacroActionType.OPEN_CONTAINER || this.editingAction.getType() == MacroActionType.INTERACT_ENTITY)
         && "entityTargets".equals(key);
   }

   private boolean clear_Z_Str(String key) {
      return false;
   }

   private ActionEditorOverlay.EntryResult captureSlotEntry(
      Slot slot, String itemName, String registryId, int visibleSlot, boolean slotMode, List<String> entries
   ) {
      if (slot != null && visibleSlot >= 0) {
         MacroItemTarget captured = slotMode
            ? MacroItemTarget.ofSlot(visibleSlot)
            : stripSlotIfFiltered(this.targetFromSlot(slot, itemName, registryId, visibleSlot));
         if (!captured.hasItemFilter()) {
            String mode = this.enumValue("mode");
            boolean playerInventorySlot = autismclient.util.InventorySlotHelper.isPlayerInventorySlot(MC, slot);
            if ("LOOT".equals(mode) && playerInventorySlot) {
               return new ActionEditorOverlay.EntryResult(false, "Loot slot numbers must be chest/GUI slots - pick an item to add it by name", -38037);
            }

            if ("STORE".equals(mode) && !playerInventorySlot) {
               return new ActionEditorOverlay.EntryResult(false, "Store slot numbers must be inventory slots - pick an item to add it by name", -38037);
            }
         }

         return this.init_ActionEditorOverla_FieldDef(this.findField("targetItems"), "targetItems", entries, captured.toSpecString());
      } else {
         return new ActionEditorOverlay.EntryResult(false, "Could not read that slot", -38037);
      }
   }

   private String normalizeItemEntry(String raw) {
      if (raw == null) {
         return null;
      }

      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      MacroItemTarget parsed = MacroItemTarget.parse(trimmed);
      if (!parsed.hasItemFilter()) {
         if (!trimmed.startsWith("#") && trimmed.matches("\\d+")) {
            trimmed = "#" + trimmed;
         }

         return XCarryAction.normalizeEntry(trimmed);
      } else {
         parsed.slot = -1;
         String entry = parsed.toSpecString();
         return entry != null && !entry.isBlank() ? entry : null;
      }
   }

   private boolean addListEntry(FieldDef field, List<String> entries, String rawEntry) {
      if (entries != null && rawEntry != null) {
         String entry = rawEntry.strip();
         if (entry.isEmpty()) {
            return false;
         }

         if (field != null && this.isXCarryEntriesKey(field.key())) {
            entry = this.normalizeItemEntry(entry);
            if (entry == null) {
               return false;
            } else {
               int limit = this.xCarryEntryLimit();
               if (entries.size() >= limit) {
                  AutismClientMessaging.sendPrefixed(this.xCarryLimitMessage(limit));
                  return false;
               } else {
                  entries.add(entry);
                  this.resizeXCarryDestinations(entries.size());
                  this.resizeXCarryAmountModes(entries.size());
                  return true;
               }
            }
         } else if (field != null && this.isEntityTargetsKey(field.key())) {
            if (entry.isBlank()) {
               return false;
            }

            if (!entries.contains(entry)) {
               entries.add(entry);
            }

            return true;
         } else if (field == null || !this.isItemTargetsKey(field.key()) && !this.isSlotChangeTargetsKey(field.key())) {
            if (field == null || !"players".equals(field.key())) {
               entries.add(entry);
               return true;
            }

            if (this.containsIgnoreCase(entries, entry)) {
               return false;
            }

            entries.add(entry);
            return true;
         } else {
            entry = StoreItemAction.normalizeTarget(entry);
            if (entry != null && !entry.isBlank() && !entries.contains(entry)) {
               entries.add(entry);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private ActionEditorOverlay.EntryResult init_ActionEditorOverla_FieldDef(FieldDef field, String key, List<String> entries, String rawEntry) {
      if (entries == null) {
         return null;
      }

      String entry = rawEntry == null ? "" : rawEntry.strip();
      if (entry.isEmpty()) {
         return new ActionEditorOverlay.EntryResult(false, "Nothing to add from that slot", -38037);
      }

      if (field != null && this.isXCarryEntriesKey(field.key())) {
         entry = this.normalizeItemEntry(entry);
         if (entry != null && !entry.isBlank()) {
            String formatted = this.formatListEntry(key, entry);
            int limit = this.xCarryEntryLimit();
            if (entries.size() >= limit) {
               return new ActionEditorOverlay.EntryResult(false, this.xCarryLimitMessage(limit), -38037);
            }

            entries.add(entry);
            this.resizeXCarryDestinations(entries.size());
            this.resizeXCarryAmountModes(entries.size());
            return new ActionEditorOverlay.EntryResult(true, "Added " + formatted, -10035062);
         } else {
            return new ActionEditorOverlay.EntryResult(false, "Could not read that XCarry entry", -38037);
         }
      } else {
         if (field != null && (this.isItemTargetsKey(field.key()) || this.isSlotChangeTargetsKey(field.key()))) {
            entry = StoreItemAction.normalizeTarget(entry);
            if (entry == null || entry.isBlank()) {
               return new ActionEditorOverlay.EntryResult(false, "Could not read that slot target", -38037);
            }
         }

         String formatted = this.formatListEntry(key, entry);
         if (entries.contains(entry)) {
            return new ActionEditorOverlay.EntryResult(false, "Already added " + formatted, -38037);
         }

         entries.add(entry);
         return new ActionEditorOverlay.EntryResult(true, "Added " + formatted, -10035062);
      }
   }

   private String formatListEntry(String key, String entry) {
      if (this.isXCarryEntriesKey(key)) {
         String formatted = XCarryAction.formatEntry(entry);
         if (!formatted.isEmpty()) {
            return formatted;
         }
      }

      if (this.isItemTargetsKey(key) || this.isSlotChangeTargetsKey(key)) {
         return StoreItemAction.formatTarget(entry);
      } else if (this.isEntityTargetsKey(key)) {
         return this.entityEntryLabel(entry);
      } else {
         return entry == null ? "" : entry;
      }
   }

   private int xCarryEntryLimit() {
      return 11;
   }

   private String xCarryLimitMessage(int limit) {
      return "XCarry limit reached: " + limit + " entries max (5 craft + 4 armor + offhand + cursor)";
   }

   private void resizeXCarryDestinations(int count) {
      if (this.actionTag != null) {
         ListTag list = this.actionTag.getList("entryDestinations").orElse(new ListTag());

         while (list.size() < count) {
            list.add(StringTag.valueOf(String.valueOf(-2147483648)));
         }

         while (list.size() > count) {
            list.remove(list.size() - 1);
         }

         this.actionTag.put("entryDestinations", list);
      }
   }

   private void resizeXCarryAmountModes(int count) {
      if (this.actionTag != null) {
         ListTag modes = this.actionTag.getList("entryAmountModes").orElse(new ListTag());

         while (modes.size() < count) {
            modes.add(StringTag.valueOf(XCarryAction.AmountMode.FULL_STK.name()));
         }

         while (modes.size() > count) {
            modes.remove(modes.size() - 1);
         }

         this.actionTag.put("entryAmountModes", modes);
         ListTag amounts = this.actionTag.getList("entryAmounts").orElse(new ListTag());

         while (amounts.size() < count) {
            amounts.add(StringTag.valueOf("1"));
         }

         while (amounts.size() > count) {
            amounts.remove(amounts.size() - 1);
         }

         this.actionTag.put("entryAmounts", amounts);
      }
   }

   private int entryDestination(int index) {
      if (this.actionTag != null && index >= 0) {
         ListTag list = this.actionTag.getList("entryDestinations").orElse(new ListTag());
         if (index >= list.size()) {
            return -2147483648;
         }

         try {
            return Integer.parseInt(list.get(index).asString().orElse(String.valueOf(-2147483648)));
         } catch (Exception ignored) {
            return -2147483648;
         }
      } else {
         return -2147483648;
      }
   }

   private void setEntryDestination(int index, int destination) {
      if (this.actionTag != null && index >= 0) {
         this.resizeXCarryDestinations(Math.max(index + 1, this.listFields.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.actionTag.getList("entryDestinations").orElse(new ListTag());
         if (index < list.size()) {
            list.set(index, StringTag.valueOf(String.valueOf(destination)));
            this.actionTag.put("entryDestinations", list);
         }
      }
   }

   private void removeXCarryDestination(int index) {
      if (this.actionTag != null && index >= 0) {
         ListTag list = this.actionTag.getList("entryDestinations").orElse(new ListTag());
         if (index >= 0 && index < list.size()) {
            list.remove(index);
         }

         this.actionTag.put("entryDestinations", list);
      }
   }

   private XCarryAction.AmountMode entryAmountMode(int index) {
      if (this.actionTag != null && index >= 0) {
         ListTag list = this.actionTag.getList("entryAmountModes").orElse(new ListTag());
         if (index >= list.size()) {
            return XCarryAction.AmountMode.FULL_STK;
         }

         try {
            return XCarryAction.AmountMode.valueOf(list.get(index).asString().orElse(XCarryAction.AmountMode.FULL_STK.name()));
         } catch (IllegalArgumentException ignored) {
            return XCarryAction.AmountMode.FULL_STK;
         }
      } else {
         return XCarryAction.AmountMode.FULL_STK;
      }
   }

   private void setEntryAmountMode(int index, XCarryAction.AmountMode mode) {
      if (this.actionTag != null && index >= 0) {
         this.resizeXCarryAmountModes(Math.max(index + 1, this.listFields.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.actionTag.getList("entryAmountModes").orElse(new ListTag());
         list.set(index, StringTag.valueOf((mode == null ? XCarryAction.AmountMode.FULL_STK : mode).name()));
         this.actionTag.put("entryAmountModes", list);
      }
   }

   private int entryAmount(int index) {
      if (this.actionTag != null && index >= 0) {
         ListTag list = this.actionTag.getList("entryAmounts").orElse(new ListTag());
         if (index >= list.size()) {
            return 1;
         }

         try {
            return Math.max(1, Integer.parseInt(list.get(index).asString().orElse("1")));
         } catch (NumberFormatException ignored) {
            return 1;
         }
      } else {
         return 1;
      }
   }

   private void setEntryAmount(int index, int amount) {
      if (this.actionTag != null && index >= 0) {
         this.resizeXCarryAmountModes(Math.max(index + 1, this.listFields.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.actionTag.getList("entryAmounts").orElse(new ListTag());
         list.set(index, StringTag.valueOf(String.valueOf(Math.max(1, amount))));
         this.actionTag.put("entryAmounts", list);
      }
   }

   private void removeXCarryAmountEntry(int index) {
      if (this.actionTag != null && index >= 0) {
         ListTag modes = this.actionTag.getList("entryAmountModes").orElse(new ListTag());
         if (index >= 0 && index < modes.size()) {
            modes.remove(index);
         }

         this.actionTag.put("entryAmountModes", modes);
         ListTag amounts = this.actionTag.getList("entryAmounts").orElse(new ListTag());
         if (index >= 0 && index < amounts.size()) {
            amounts.remove(index);
         }

         this.actionTag.put("entryAmounts", amounts);
      }
   }

   private Component styledListEntry(String key, String entry, int index) {
      if (this.isItemEntryListKey(key)) {
         MacroItemTarget target = this.itemTargetForEntry(key, index, entry);
         return this.describeItemTarget(target, this.formatListEntry(key, entry));
      } else {
         return Component.literal(this.formatListEntry(key, entry));
      }
   }

   private int listEntryColor(String entry, int index, boolean selected) {
      if (selected) {
         return UiColorPalette.filterActiveText();
      }

      Class<? extends Packet<?>> packetClass = PacketTypeRegistry.resolvePacketClass(entry);
      if (packetClass == null) {
         return this.styleTheme.load(l.read);
      }

      boolean c2s = PacketTypeRegistry.getC2sPackets().contains(packetClass);
      return UiColorPalette.packetRowAccent(c2s, index);
   }

   private MacroItemTarget itemTargetForEntry(String key, int index, String entry) {
      List<MacroItemTarget> targets = this.readItemTargetList(key);
      if (index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.hasSlot() || target.hasItemFilter())) {
            return target.copy();
         }
      }

      MacroItemTarget parsed = MacroItemTarget.parse(entry);
      return !parsed.hasSlot() && !parsed.hasItemFilter() ? null : parsed;
   }

   private boolean isItemEntryListKey(String key) {
      return this.isItemTargetsKey(key) || this.isXCarryEntriesKey(key);
   }

   private Component describeItemTarget(MacroItemTarget target, String fallback) {
      String safeFallback = fallback == null ? "" : fallback;
      if (target == null) {
         return Component.literal(safeFallback);
      }

      boolean hasSlot = target.hasSlot();
      boolean hasIdentity = target.hasItemFilter();
      if (hasSlot && hasIdentity) {
         return Component.literal(target.slot + ": ").append(target.toDisplayComponent().copy());
      }

      if (hasSlot) {
         return Component.literal("#" + target.slot);
      }

      if (hasIdentity) {
         Component display = target.toDisplayComponent();
         if (display != null && !display.getString().isBlank()) {
            return display.copy();
         }
      }

      return Component.literal(safeFallback);
   }

   private static String write_Str_Str_Str(String name, String slotText) {
      String n = name == null ? "" : name.strip();
      int slot = -1;
      if (slotText != null && !slotText.isBlank()) {
         try {
            slot = Integer.parseInt(slotText.replaceAll("[^0-9]", ""));
         } catch (NumberFormatException var5) {
         }
      }

      if (slot >= 0 && !n.isEmpty()) {
         return "#" + slot + "|" + n;
      } else {
         return slot >= 0 ? "#" + slot : n;
      }
   }

   private String entryName(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry;
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private int parseDigits(String text, int fallback) {
      String raw = text == null ? "" : text.trim();
      if (raw.isEmpty()) {
         return fallback;
      }

      String cleaned = raw.replaceAll("[^0-9]", "");
      if (cleaned.isEmpty()) {
         return fallback;
      }

      try {
         int slot = Integer.parseInt(cleaned);
         return slot < 0 ? fallback : slot;
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private int entrySlot(String entry) {
      if (entry != null && entry.startsWith("#")) {
         try {
            int separator = entry.indexOf(124);
            String raw = separator >= 0 ? entry.substring(1, separator) : entry.substring(1);
            return Integer.parseInt(raw);
         } catch (NumberFormatException ignored) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean containsExcept(List<String> entries, String entry, int ignoreIndex) {
      for (int i = 0; i < entries.size(); i++) {
         if (i != ignoreIndex && entries.get(i).equals(entry)) {
            return true;
         }
      }

      return false;
   }

   private List<AutismSessionState.DelayedPacket> queuedPackets() {
      if (this.actionTag == null) {
         return new ArrayList<>();
      }

      SendPacketAction action = new SendPacketAction();
      action.fromTag(this.actionTag);
      return new ArrayList<>(action.packets);
   }

   private void storeCapturedPackets(List<AutismSessionState.DelayedPacket> packets) {
      if (this.actionTag != null) {
         ListTag packetList = new ListTag();
         if (packets != null) {
            for (AutismSessionState.DelayedPacket qp : packets) {
               CompoundTag packetTag = MacroClipboardCodec.init(qp);
               if (packetTag != null) {
                  packetList.add(packetTag);
               }
            }
         }

         this.actionTag.put("packets", packetList);
      }
   }

   private String queuedPacketsSummary() {
      List<AutismSessionState.DelayedPacket> packets = this.queuedPackets();
      if (packets.isEmpty()) {
         return "Packets: 0";
      }

      String autoName = "";

      for (AutismSessionState.DelayedPacket qp : packets) {
         if (qp != null && qp.packet != null) {
            autoName = PacketNameResolver.nameOf(qp.packet);
            break;
         }
      }

      if (autoName.isEmpty()) {
         return "Packets: " + packets.size();
      } else {
         return packets.size() == 1 ? "Packets: 1 - " + autoName : "Packets: " + packets.size() + " - " + autoName;
      }
   }

   private void init_V_ck_CachePool(AutismSessionState.DelayedPacket packet) {
      if (this.actionTag != null && packet != null) {
         List<AutismSessionState.DelayedPacket> single = Collections.singletonList(packet);
         String base64 = MacroClipboardCodec.encodeQueue(single);
         if (base64 != null && !base64.isBlank()) {
            this.actionTag.putString("packetData", base64);
            if (!this.actionTag.contains("description") || this.actionTag.getStringOr("description", "").isBlank()) {
               String name = packet.packet != null ? PacketNameResolver.nameOf(packet.packet) : "Packet";
               this.actionTag.putString("description", name);
            }
         }
      }
   }

   private String rawPacketSummary() {
      if (this.actionTag == null) {
         return "Raw Packet: empty";
      } else {
         String packetData = this.actionTag.getStringOr("packetData", "");
         if (packetData != null && !packetData.isBlank()) {
            String description = this.actionTag.getStringOr("description", "");
            return description != null && !description.isBlank() ? "Raw Packet: " + description : "Raw Packet: loaded";
         } else {
            return "Raw Packet: empty";
         }
      }
   }

   private void startGBreakCapture() {
      if (this.ensureCaptureAvailable()) {
         AutismSessionState state = AutismSessionState.getInstance();
         this.screenBeforeGbreakCapture = MC.gui.screen();
         this.hideOverlaysForCapture();
         state.setCaptureCancelCallback(() -> {
            state.cancelGbreakCapture();
            if (this.screenBeforeGbreakCapture != null) {
               OverlayScreenController.setScreenExplicit(MC, this.screenBeforeGbreakCapture);
               this.screenBeforeGbreakCapture = null;
            }

            this.finishCapture(false, false);
         });
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
         AutismClientMessaging.sendPrefixed("GBreak: Break a block to capture the insta-break packet");
         final int session = this.auditEditSession;
         state.startGbreakCapture(() -> MC.execute(() -> {
            if (this.auditEditSession != session || this.actionTag == null) {
               return;
            }
            List<AutismSessionState.DelayedPacket> captured = AutismSessionState.getInstance().getGbreakCapturedPackets();
            if (!captured.isEmpty()) {
               this.storeCapturedPackets(Collections.singletonList(captured.get(0)));
               this.actionTag.putString("customName", "GBreak");
               ThemedTextInputBox customNameField = this.textFields.get("customName");
               if (customNameField != null) {
                  customNameField.setValue("GBreak");
               }

               AutismClientMessaging.sendPrefixed("GBreak packet captured");
            } else {
               AutismClientMessaging.sendPrefixed("GBreak capture finished with no packet");
            }

            if (this.screenBeforeGbreakCapture != null) {
               OverlayScreenController.setScreenExplicit(MC, this.screenBeforeGbreakCapture);
               this.screenBeforeGbreakCapture = null;
            }

            this.finishCapture(false, false);
            OverlayManager.getInstance().bringToFront(this);
         }));
      }
   }

   private void captureLookAtBlock() {
      if (this.ensureCaptureAvailable() && this.editingAction instanceof LookAtBlockAction) {
         Screen previousScreen = MC.gui.screen();
         this.hideOverlaysForCapture();
         AutismSessionState state = AutismSessionState.getInstance();
         state.setCaptureCancelCallback(() -> {
            if (previousScreen != null) {
               OverlayScreenController.setScreenExplicit(MC, previousScreen);
            }

            state.setEntityCaptureCallback(null);
            this.finishCapture(false, false);
         });
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
         state.setCaptureSpecificEntity(this.captureSpecificEntity);
         state.setEntityCaptureCallback(payload -> MC.execute(() -> {
            List<String> selected = this.listFields.get("entityIds");
            if (selected != null && payload != null && !payload.isBlank()) {
               if (this.captureSpecificEntity) {
                  selected.removeIf(existing -> existing.startsWith(this.entityEntryPrefix(payload)));
               }

               if (!selected.contains(payload)) {
                  selected.add(payload);
               }
            }

            if (previousScreen != null) {
               OverlayScreenController.setScreenExplicit(MC, previousScreen);
            }

            this.finishCapture(false, false);
            OverlayManager.getInstance().bringToFront(this);
         }));
      }
   }

   private void renderWaitSoundEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitGuiName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "checkDistance", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "maxDistance", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<String> selected = this.listFields.getOrDefault("soundIds", Collections.emptyList());
      cy = this.renderSimpleEntryRows(ctx, x, cy, w, mx, my, "wait_sound_selected", "Sounds", selected, value -> {
         selected.remove(value);
         ScrollableListView vp = this.entryViewports.get("wait_sound_selected");
         if (vp != null) {
            vp.scrollByRows(-1);
         }
      }, RegistryDisplayNameCache::write, "(any sound - add to filter)");
      ThemedTextInputBox search = this.internalTextFields.get("soundIds");
      if (search != null) {
         search.setX(x);
         search.setY(cy + 1);
         search.setWidth(w);
         search.render(ctx, mx, my, delta);
      }

      cy += 18;
      String filter = search != null ? search.getValue().trim().toLowerCase() : "";
      List<String> filtered = this.filterCached("wait_sound_search", soundIds(), filter, RegistryDisplayNameCache::write);
      this.renderSelectableList(ctx, x, cy, w, mx, my, "wait_sound_search", filtered, 6, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, RegistryDisplayNameCache::write);
   }

   private void renderWaitEntityEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "checkMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "radius", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "centerOnPlayer", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "pos", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "mustBeLookingAt", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<String> selected = this.listFields.getOrDefault("entityIds", Collections.emptyList());
      cy = this.renderSimpleEntryRows(
         ctx, x, cy, w, mx, my, "wait_entity_selected", "Entities", selected, selected::remove, this::entityEntryLabel, "(any entity - add type or specific)"
      );
      ThemedTextInputBox search = this.internalTextFields.get("entityIds");
      String modeLabel = this.captureSpecificEntity ? "[Spec]" : "[Type]";
      int modeW = Math.max(this.buttonWidth("[Spec]"), this.buttonWidth("[Type]"));
      int capW = this.buttonWidth("CAP");
      int searchW = w - modeW - 2 - capW - 2;
      if (search != null) {
         search.setX(x);
         search.setY(cy);
         search.setWidth(searchW);
         search.render(ctx, mx, my, delta);
      }

      int modeX = x + searchW + 2;
      this.renderSecondaryButton(ctx, modeX, cy, modeW, 13, modeLabel, mx, my, () -> this.captureSpecificEntity = !this.captureSpecificEntity);
      this.renderSecondaryButton(ctx, modeX + modeW + 2, cy, capW, 13, "CAP", mx, my, this::captureWaitEntity);
      cy += 18;
      String filter = search != null ? search.getValue().trim().toLowerCase() : "";
      List<String> filtered = this.filterCached("wait_entity_search", entityTypeIds(), filter, RegistryDisplayNameCache::read);
      this.renderSelectableList(ctx, x, cy, w, mx, my, "wait_entity_search", filtered, 4, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, RegistryDisplayNameCache::read);
      cy += 66;
      autismclient.gui.f.b.k.init(ctx, this.font, "Nearby Entities", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 13;
      List<String> nearbyEntries = this.nearbyEntityEntries();
      this.renderSelectableList(ctx, x, cy, w, mx, my, "wait_entity_nearby", nearbyEntries, 3, value -> {
         String stored = this.captureSpecificEntity ? value : this.entityEntryId(value);
         if (!stored.isBlank()) {
            if (this.captureSpecificEntity) {
               boolean removed = selected.removeIf(existing -> existing.startsWith(this.entityEntryPrefix(stored)));
               if (!removed) {
                  selected.add(stored);
               }
            } else if (selected.contains(stored)) {
               selected.remove(stored);
            } else {
               selected.add(stored);
            }
         }
      }, value -> {
         String stored = this.captureSpecificEntity ? value : this.entityEntryId(value);
         return this.captureSpecificEntity ? selected.stream().anyMatch(existing -> existing.startsWith(this.entityEntryPrefix(stored))) : selected.contains(stored);
      }, this::entityEntryDisplay);
   }

   private void renderGenericActionFields(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "targetMode", x, cy, w, mx, my, delta);
      String targetMode = this.enumValue("targetMode");
      if ("ENTITY".equals(targetMode)) {
         cy = this.renderFieldByKey(ctx, "entityTargets", x, cy, w, mx, my, delta);
      } else if ("BLOCK".equals(targetMode)) {
         cy = this.renderFieldByKey(ctx, "pos", x, cy, w, mx, my, delta);
      }

      cy = this.renderFieldByKey(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "guiName", x, cy, w, mx, my, delta);
      if ("ENTITY".equals(targetMode)) {
         String hint = "Pick captures exact entities. Last Target reuses the most recent interaction target.";
         cy = this.renderHint(ctx, x, cy, w, hint);
         List<String> selected = this.listFields.getOrDefault("entityTargets", Collections.emptyList());
         cy += 13;
         List<String> nearbyEntries = this.nearbyEntityEntries();
         this.renderSelectableList(ctx, x, cy, w, mx, my, "open_container_entity_nearby", nearbyEntries, 3, value -> {
            if (selected.contains(value)) {
               selected.remove(value);
            } else {
               selected.add(value);
            }
         }, selected::contains, this::entityEntryDisplay);
      } else if ("LAST_TARGET".equals(targetMode)) {
         this.renderHint(ctx, x, cy, w, "Uses the last block or entity container you actually opened.");
      } else {
         cy = this.renderHint(ctx, x, cy, w, "Capture only marks the block. It will not open it.");
         this.renderNearbyList(ctx, x, cy, w, mx, my, "open_container_nearby", "pos");
      }
   }

   private void renderNearbyList(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, String listKey, String fieldKey) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, "Nearby Containers", font, UiColorPalette.textBody(), x, y + 2, false);
      y += 14;
      List<BlockPos> nearbyContainers = this.nearbyContainers();
      this.renderSelectableList(
         ctx,
         x,
         y,
         w,
         mx,
         my,
         listKey,
         nearbyContainers,
         2,
         pos -> this.setPosFields(fieldKey, pos),
         pos -> this.posFieldsMatch(fieldKey, pos),
         this::containerLabel
      );
   }

   private void renderStoreItemEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "allItems", x, cy, w, mx, my, delta);
      List<String> selected = this.listFields.getOrDefault("targetItems", Collections.emptyList());
      if (!this.toggleStates.getOrDefault("allItems", false)) {
         cy = this.renderEntryRows(
            ctx,
            x,
            cy,
            w,
            mx,
            my,
            "store_items_selected",
            "Target Items",
            selected,
            selected::remove,
            StoreItemAction::formatTarget,
            value -> this.styledListEntry("targetItems", value, selected.indexOf(value)),
            selected::clear,
            "(nothing selected - search, type #slot, or capture)",
            true
         );
         int storeEditIdx = this.listEditIndices.getOrDefault("store_items_selected", -1);
         boolean storeEditing = storeEditIdx >= 0 && storeEditIdx < selected.size();
         ThemedTextInputBox search = this.internalTextFields.get("targetItems");
         int pickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
         int addBtnW = this.buttonWidth("+Add");
         int slotW = 44;
         int capBtnX = x + w - pickW;
         int addBtnX = capBtnX - 3 - addBtnW;
         int slotX = addBtnX - 2 - slotW;
         int nameW = slotX - x - 2;
         ThemedTextInputBox storeSlotF = this.textFields.get("store_slot");
         if (storeSlotF == null) {
            storeSlotF = this.newTextField(slotW);
            storeSlotF.setIntegerOnlyFilter(true);
            storeSlotF.setPlaceholder(Component.literal("Slot#"));
            this.textFields.put("store_slot", storeSlotF);
         }

         String pendingText = this.pendingSelections.remove("store_items_selected");
         if (pendingText != null) {
            if (search != null) {
               search.setValue(this.entryName(pendingText));
            }

            MacroItemTarget parsed = MacroItemTarget.parse(pendingText);
            storeSlotF.setValue(parsed.hasSlot() ? String.valueOf(parsed.slot) : "");
         }

         if (search != null) {
            search.setX(x);
            search.setY(cy);
            search.setWidth(nameW);
            search.render(ctx, mx, my, delta);
         }

         storeSlotF.setX(slotX);
         storeSlotF.setY(cy);
         storeSlotF.setWidth(slotW);
         storeSlotF.render(ctx, mx, my, delta);
         if (storeEditing && search != null) {
            String nameText = search.getValue().strip();
            String slotText = storeSlotF.getValue().strip();
            String entry = write_Str_Str_Str(nameText, slotText);
            if (!entry.isEmpty()) {
               String normalized = StoreItemAction.normalizeTarget(entry);
               if (normalized != null && !normalized.isBlank() && !normalized.equals(selected.get(storeEditIdx))) {
                  selected.set(storeEditIdx, normalized);
                  this.itemTargetLists.put("targetItems", this.readItemTargetList("targetItems"));
               }
            }
         }

         ThemedTextInputBox fStoreSlotF = storeSlotF;
         this.renderActionButton(ctx, addBtnX, cy, addBtnW, 13, "+Add", OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
            if (search != null) {
               String nameText = search.getValue().strip();
               String slotText = fStoreSlotF.getValue().strip();
               String entry = write_Str_Str_Str(nameText, slotText);
               if (!entry.isEmpty()) {
                  String normalized = StoreItemAction.normalizeTarget(entry);
                  if (normalized != null && !normalized.isBlank() && !selected.contains(normalized)) {
                     selected.add(normalized);
                     this.itemTargetLists.put("targetItems", this.readItemTargetList("targetItems"));
                  }

                  search.setValue("");
                  fStoreSlotF.setValue("");
                  this.listEditIndices.put("store_items_selected", -1);
               }
            }
         });
         boolean capturing = this.captureSession.init("targetItems");
         this.renderCaptureButton(ctx, capBtnX, cy, pickW, 13, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("targetItems"));
         cy += 18;
         String filter = search != null ? search.getValue().trim().toLowerCase() : "";
         List<String> filtered = this.filterCached("store_items_search", itemIds(), filter, RegistryDisplayNameCache::init);
         this.renderSelectableList(ctx, x, cy, w, mx, my, "store_items_search", filtered, 6, value -> {
            if (selected.contains(value)) {
               selected.remove(value);
            } else {
               selected.add(value);
            }

            this.listEditIndices.put("store_items_selected", -1);
            this.itemTargetLists.put("targetItems", this.readItemTargetList("targetItems"));
         }, selected::contains, RegistryDisplayNameCache::init);
         cy += 82;
         cy = this.renderHint(
            ctx, x, cy, w, "Pick on an item adds it by name, from any slot on the screen. Ctrl+pick or an empty slot adds that exact slot."
         );
         String modeHint = "LOOT".equals(this.enumValue("mode")) ? "Exact slots must be chest/custom GUI slots." : "Exact slots must be player inventory slots.";
         cy = this.renderHint(ctx, x, cy, w, modeHint);
      }

      cy = this.renderFieldByKey(ctx, "persistent", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "delayTicks", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "closeAfter", x, cy, w, mx, my, delta);
      this.renderFieldByKey(ctx, "closeSendPkt", x, cy, w, mx, my, delta);
   }

   private void renderInventoryAuditEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "mode", x, cy, w, mx, my, delta);
      List<String> selected = this.listFields.getOrDefault("targetItems", Collections.emptyList());
      FieldDef targetItemsField = this.fieldByKey("targetItems");
      if (targetItemsField != null && this.isFieldApplicable(targetItemsField)) {
         String emptyLabel = this.auditTargetsError("targetItems");
         if (emptyLabel == null || emptyLabel.isBlank()) {
            emptyLabel = "(nothing selected)";
         }

         boolean multipleStacks = this.toggleStates.getOrDefault("multipleStacks", false);
         int listTop = cy;
         cy = this.renderEntryRows(
            ctx,
            x,
            cy,
            w,
            mx,
            my,
            "audit_items_selected",
            "Targets",
            selected,
            selected::remove,
            StoreItemAction::formatTarget,
            value -> this.styledListEntry("targetItems", value, selected.indexOf(value)),
            selected::clear,
            emptyLabel,
            true
         );
         int multiBtnW = 96;
         int clearBtnW = this.buttonWidth("Clear");
         int multiBtnX = x + w - clearBtnW - 3 - multiBtnW;
         this.renderToggleButton(
            ctx,
            multiBtnX,
            listTop,
            multiBtnW,
            13,
            "Multiple Stacks",
            multipleStacks,
            "macro-audit:multiple-stacks",
            mx,
            my,
            () -> this.toggleStates.put("multipleStacks", !this.toggleStates.getOrDefault("multipleStacks", false))
         );
         int auditEditIdx = this.listEditIndices.getOrDefault("audit_items_selected", -1);
         boolean auditEditing = auditEditIdx >= 0 && auditEditIdx < selected.size();
         ThemedTextInputBox search = this.internalTextFields.get("targetItems");
         String addLabel = auditEditing ? "New" : "+Add";
         int pickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
         int addBtnW = this.buttonWidth(addLabel);
         int slotW = 26;
         int pickX = x + w - addBtnW - 3 - pickW;
         int addBtnX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         int nameW = slotX - x - 2;
         ThemedTextInputBox auditSlotF = this.textFields.get("audit_slot");
         if (auditSlotF == null) {
            auditSlotF = this.newTextField(slotW);
            auditSlotF.setIntegerOnlyFilter(true);
            auditSlotF.setPlaceholder(Component.literal("Slot"));
            this.textFields.put("audit_slot", auditSlotF);
         }

         String pendingText = this.pendingSelections.remove("audit_items_selected");
         if (pendingText != null) {
            if (search != null) {
               search.setValue(this.entryName(pendingText));
            }

            MacroItemTarget parsed = MacroItemTarget.parse(pendingText);
            auditSlotF.setValue(parsed.hasSlot() ? String.valueOf(parsed.slot) : "");
         }

         if (search != null) {
            search.setX(x);
            search.setY(cy);
            search.setWidth(nameW);
            search.render(ctx, mx, my, delta);
         }

         auditSlotF.setX(slotX);
         auditSlotF.setY(cy);
         auditSlotF.setWidth(slotW);
         auditSlotF.render(ctx, mx, my, delta);
         if (auditEditing && search != null) {
            String nameText = search.getValue().strip();
            String slotText = auditSlotF.getValue().strip();
            String entry = write_Str_Str_Str(nameText, slotText);
            if (!entry.isEmpty()) {
               String normalized = StoreItemAction.normalizeTarget(entry);
               if (normalized != null && !normalized.isBlank() && !normalized.equals(selected.get(auditEditIdx))) {
                  selected.set(auditEditIdx, normalized);
                  this.itemTargetLists.put("targetItems", this.readItemTargetList("targetItems"));
               }
            }
         }

         ThemedTextInputBox fAuditSlotF = auditSlotF;
         boolean capturing = this.captureSession.init("targetItems");
         this.renderCaptureButton(ctx, pickX, cy, pickW, 13, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("targetItems"));
         this.renderActionButton(ctx, addBtnX, cy, addBtnW, 13, addLabel, OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
            if (search != null) {
               if (this.listEditIndices.getOrDefault("audit_items_selected", -1) >= 0) {
                  this.listEditIndices.put("audit_items_selected", -1);
                  this.pendingSelections.put("audit_items_selected", "");
                  search.setValue("");
                  fAuditSlotF.setValue("");
               } else {
                  String nameText = search.getValue().strip();
                  String slotText = fAuditSlotF.getValue().strip();
                  String entryx = write_Str_Str_Str(nameText, slotText);
                  if (!entryx.isEmpty()) {
                     String normalizedx = StoreItemAction.normalizeTarget(entryx);
                     if (normalizedx != null && !normalizedx.isBlank() && !selected.contains(normalizedx)) {
                        selected.add(normalizedx);
                        this.itemTargetLists.put("targetItems", this.readItemTargetList("targetItems"));
                     }

                     search.setValue("");
                     fAuditSlotF.setValue("");
                     this.listEditIndices.put("audit_items_selected", -1);
                     this.pendingSelections.put("audit_items_selected", "");
                  }
               }
            }
         });
         cy += 18;
      }

      cy = this.renderFieldByKey(ctx, "openMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "openCommand", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "containerPos", x, cy, w, mx, my, delta);
      if ("CONTAINER".equals(this.enumValue("openMode"))) {
         cy = this.renderHint(ctx, x, cy, w, "Capture stores the clicked block position. Nearby also includes likely server-handled GUI blocks.");
         this.renderNearbyList(ctx, x, cy, w, mx, my, "inventory_audit_nearby", "containerPos");
         cy += 43;
      }

      cy = this.renderFieldByKey(ctx, "dupeVector", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "iterations", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "maxTransferAttempts", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "transferRetryDelayMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "spamCount", x, cy, w, mx, my, delta);
      this.renderFieldByKey(ctx, "spamDelayMs", x, cy, w, mx, my, delta);
   }

   private void renderPayEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "commandTemplate", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "amountInput", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "divideEnabled", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "probeHidden", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "delayEnabled", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "delayMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "confirmMacro", x, cy, w, mx, my, delta);
      List<String> selected = this.listFields.getOrDefault("players", Collections.emptyList());
      cy = this.renderEntryRows(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "pay_players_selected",
         "Players",
         selected,
         value -> this.removeIgnoreCase(selected, value),
         value -> value,
         ignored -> null,
         selected::clear,
         "(scan the server or add one manually)"
      );
      ThemedTextInputBox search = this.internalTextFields.get("players");
      int scanW = this.buttonWidth("Scan");
      int allW = this.buttonWidth("All");
      int searchW = Math.max(1, w - scanW - allW - 4);
      if (search != null) {
         search.setX(x);
         search.setY(cy);
         search.setWidth(searchW);
         search.render(ctx, mx, my, delta);
      }

      int scanX = x + searchW + 2;
      int allX = scanX + scanW + 2;
      this.renderSecondaryButton(ctx, scanX, cy, scanW, 13, "Scan", mx, my, this::scanPayPlayers);
      this.renderSecondaryButton(ctx, allX, cy, allW, 13, "All", mx, my, () -> this.selectAllMatching(selected, search != null ? search.getValue() : ""));
      cy += 18;
      String query = search != null ? search.getValue() : "";
      if (!this.playerOptionsLoaded) {
         this.renderEmptyList(ctx, x, cy, w, "pay_players_scan", 1, "Press Scan to load the current server players.");
         cy += 17;
      } else {
         cy = this.renderPlayerPicker(ctx, x, cy, w, mx, my, selected, query) + 4;
      }

      ThemedTextInputBox amountField = this.textFields.get("amountInput");
      long amount = PayAction.parseAmount(amountField != null ? amountField.getValue() : "");
      boolean divide = this.toggleStates.getOrDefault("divideEnabled", false);
      int count = selected.size();
      String summary;
      if (count == 0) {
         summary = "Empty list = pays EVERYONE (tab + probed hidden).";
      } else if (divide) {
         long[] dist = PayAction.init(amount, count);
         String per = dist[0] == dist[count - 1] ? PayAction.formatAmount(dist[0]) : PayAction.formatAmount(dist[count - 1]) + "-" + PayAction.formatAmount(dist[0]);
         summary = "Splits " + PayAction.formatAmount(amount) + " among " + count + " (" + per + " each).";
      } else {
         summary = "Pays " + count + " x " + PayAction.formatAmount(amount) + " (" + PayAction.formatAmount(amount * count) + " total).";
      }

      cy = this.renderHint(ctx, x, cy, w, summary);
      String hint = this.playerOptionsLoaded
         ? "Click a name to add it. Click a rank header to add/remove everyone in that rank; All adds all."
         : "Scan to list players by rank. Type a name then Enter to add it manually.";
      this.renderHint(ctx, x, cy, w, hint);
   }

   private List<ActionEditorOverlay.PlayerRow> groupedPlayerEntries(String query) {
      Map<String, List<String>> byPrefix = new LinkedHashMap<>();

      for (String name : this.filterList(this.playerNameOptions, query)) {
         byPrefix.computeIfAbsent(this.set_Str(name), ignored -> new ArrayList<>()).add(name);
      }

      List<String> order = new ArrayList<>(byPrefix.keySet());
      order.sort((a, b) -> {
         boolean ab = a.isBlank();
         boolean bb = b.isBlank();
         if (ab != bb) {
            return ab ? 1 : -1;
         } else {
            return a.compareToIgnoreCase(b);
         }
      });
      List<ActionEditorOverlay.PlayerRow> rows = new ArrayList<>();

      for (String prefix : order) {
         List<String> names = byPrefix.get(prefix);
         names.sort(String.CASE_INSENSITIVE_ORDER);
         rows.add(ActionEditorOverlay.PlayerRow.groupRow(prefix, names));

         for (String name : names) {
            rows.add(ActionEditorOverlay.PlayerRow.singleRow(prefix, name));
         }
      }

      return rows;
   }

   private int renderPlayerPicker(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, List<String> selected, String query) {
      int rowH = 13;
      List<ActionEditorOverlay.PlayerRow> entries = this.groupedPlayerEntries(query);
      if (entries.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, w, 12, false, false);
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No players found", x, y, w);
         return y + rowH;
      }

      int clipTop = this.bodyClip != null ? this.bodyClip.write() : -2147483648;
      int clipBottom = this.bodyClip != null ? this.bodyClip.load() : 2147483647;
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int iy = y;

      for (ActionEditorOverlay.PlayerRow row : entries) {
         if (iy + rowH > clipTop && iy < clipBottom) {
            if (row.header()) {
               List<String> members = row.members();
               int selCount = (int)members.stream().filter(m -> this.containsIgnoreCase(selected, m)).count();
               boolean allSel = selCount == members.size() && selCount > 0;
               autismclient.gui.f.b.k.init(ctx, x, iy, x + w, iy + rowH - 1, 1140850688);
               autismclient.gui.f.b.k.init(ctx, x, iy, x + 2, iy + rowH - 1, allSel ? -11628966 : -7459271);
               int headerColor = allSel ? -6496088 : -1593160;
               int baseY = iy + Math.max(1, (rowH - 8) / 2);
               String marker = allSel ? "✓ " : "+ ";
               String counter = "  (" + selCount + "/" + members.size() + ")";
               int markerW = this.font.width(marker);
               int counterW = this.font.width(counter);
               int rankMaxW = Math.max(8, w - 12 - markerW - counterW);
               int cx = x + 6;
               autismclient.gui.f.b.k.init(ctx, this.font, marker, font, headerColor, cx, baseY, false);
               cx += markerW;
               Component styled = row.prefix().isBlank() ? null : this.set_Map.get(row.prefix());
               if (styled != null && this.font.width(styled) <= rankMaxW) {
                  ctx.text(this.font, styled, cx, baseY, -1, false);
                  cx += this.font.width(styled);
               } else {
                  String fallback = row.prefix().isBlank() ? "No rank" : row.prefix();
                  int fallbackColor = row.prefix().isBlank() ? headerColor : -1;
                  fallback = autismclient.gui.f.b.k.init(this.font, fallback, rankMaxW, font, fallbackColor);
                  autismclient.gui.f.b.k.init(ctx, this.font, fallback, font, fallbackColor, cx, baseY, false);
                  cx += this.font.width(fallback);
               }

               autismclient.gui.f.b.k.init(ctx, this.font, counter, font, headerColor, cx, baseY, false);
               List<String> targetMembers = members;
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, w, rowH, () -> this.toggleGroup(selected, targetMembers)));
            } else {
               boolean sel = this.containsIgnoreCase(selected, row.load());
               boolean hovered = mx >= x && mx < x + w && my >= iy && my < iy + rowH;
               String label = autismclient.gui.f.b.k.init(this.font, "  " + row.load(), w - 6, font, UiColorPalette.textBody());
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  Component.literal(label),
                  autismclient.gui.f.NetHelper.init(x, iy, w, rowH),
                  hovered,
                  sel,
                  sel ? ListRowRenderer.RowTone.READY : ListRowRenderer.RowTone.NORMAL,
                  false
               );
               String name = row.load();
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, w, rowH, () -> this.togglePlayer(selected, name)));
            }
         }

         iy += rowH;
      }

      return iy;
   }

   private void renderToggleModuleEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<ak.Inner_DataUtil> selected = this.toggleModuleEntries != null ? this.toggleModuleEntries : Collections.emptyList();
      autismclient.gui.f.b.k.init(ctx, this.font, "Module Actions (" + selected.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      boolean canClear = !selected.isEmpty();
      int tmClearW = this.buttonWidth("Clear");
      int clearX = x + w - tmClearW;
      this.renderActionButton(ctx, clearX, cy, tmClearW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClear, mx, my, () -> {
         selected.clear();
         ScrollableListView vp = this.entryViewports.get("toggle_module_selected");
         if (vp != null) {
            vp.setScrollOffset(0);
         }
      });
      cy += 14;
      int listH = 56;
      int itemW = w - 5 - 1;
      ScrollableListView toggleViewport = this.viewportFor(this.entryViewports, "toggle_module_selected", x, cy, w, listH, 14, 5);
      toggleViewport.setContentHeight(selected.size() * 14);
      toggleViewport.renderScrollbar(ctx, mx, my);
      if (selected.isEmpty()) {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "Pick modules below.", x, cy, itemW);
      } else {
         toggleViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int first = toggleViewport.getFirstVisibleRow();

         for (int i = first; i < selected.size() && i <= toggleViewport.getLastVisibleRow(); i++) {
            int iy = toggleViewport.getRowY(i);
            if (iy != -2147483648) {
               ak.Inner_DataUtil entry = selected.get(i);
               int removeW = 13;
               ak.Inner_StreamUtil[] tmModes = ak.Inner_StreamUtil.values();
               List<String> tmOptions = new ArrayList<>();

               for (ak.Inner_StreamUtil m : tmModes) {
                  tmOptions.add(this.toggleModeLabel(m));
               }

               int modeW = this.dropdownWidth(tmOptions, 52);
               int rowW = itemW - removeW - modeW - 4;
               boolean hovered = mx >= x && mx < x + rowW && my >= iy + 1 && my < iy + 14;
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  Component.literal(entry.init_Str),
                  autismclient.gui.f.NetHelper.init(x, iy + 1, rowW, 13),
                  hovered,
                  false,
                  ListRowRenderer.RowTone.NORMAL,
                  false
               );
               int modeX = x + rowW + 2;
               int entryIndex = i;
               this.renderDropdown("toggle_mode_" + entryIndex, tmOptions, entry.init_ak_StreamUtil.ordinal(), modeX, iy + 1, modeW, 13, idx -> {
                  if (entryIndex >= 0 && entryIndex < selected.size()) {
                     selected.get(entryIndex).init_ak_StreamUtil = tmModes[Math.max(0, Math.min(idx, tmModes.length - 1))];
                  }
               });
               int removeX = modeX + modeW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy + 1, removeW, mx, my, () -> {
                  if (entryIndex >= 0 && entryIndex < selected.size()) {
                     selected.remove(entryIndex);
                     ScrollableListView vp = this.entryViewports.get("toggle_module_selected");
                     if (vp != null) {
                        vp.scrollByRows(-1);
                     }
                  }
               });
            }
         }

         toggleViewport.endFrame(ctx);
      }

      cy += listH + 4;
      ThemedTextInputBox search = this.internalTextFields.get("_toggle_module_search");
      int refreshW = this.buttonWidth("Refresh");
      int addW = this.buttonWidth("+Add");
      int searchW = Math.max(1, w - refreshW - addW - 4);
      if (search != null) {
         search.setX(x);
         search.setY(cy);
         search.setWidth(searchW);
         search.render(ctx, mx, my, delta);
      }

      int refreshX = x + searchW + 2;
      int addX = refreshX + refreshW + 2;
      this.renderSecondaryButton(ctx, refreshX, cy, refreshW, 13, "Refresh", mx, my, this::refreshModuleRegistry);
      this.renderSecondaryButton(ctx, addX, cy, addW, 13, "+Add", mx, my, () -> this.addFirstMatchingModule(search != null ? search.getValue() : ""));
      cy += 18;
      String query = search != null ? search.getValue() : "";
      List<String> filteredAutism = this.filterCached("toggle_module_autism_registry", this.autismModuleNames, query);
      if (this.autismModuleNames.isEmpty()) {
         this.renderEmptyList(ctx, x, cy, w, "toggle_module_registry_empty", 6, "No modules found right now.");
         cy += 82;
      } else {
         cy = this.renderModulePicker(
            ctx, x, cy, w, mx, my, "AUTISM Modules", "toggle_module_autism_registry", this.autismModuleNames, filteredAutism, 6, selected
         );
      }

      String hint = selected.isEmpty()
         ? "Pick modules below, then click the mode chip on each row."
         : "Each row runs with its own Toggle / Enable / Disable setting.";
      this.renderHint(ctx, x, cy, w, hint);
   }

   private int renderModulePicker(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String title,
      String listKey,
      List<String> source,
      List<String> filtered,
      int visibleRows,
      List<ak.Inner_DataUtil> selected
   ) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, title, font, UiColorPalette.textBody(), x, y + 1, false);
      y += 12;
      if (source != null && !source.isEmpty()) {
         this.renderSelectableList(
            ctx, x, y, w, mx, my, listKey, filtered, visibleRows, this::addToggleModule, value -> this.hasToggleModule(selected, value), value -> value
         );
      } else {
         this.renderEmptyList(ctx, x, y, w, listKey, visibleRows, "No modules found right now.");
      }

      return y + visibleRows * 13 + 4;
   }

   private void renderEmptyList(GuiGraphicsExtractor ctx, int x, int y, int w, String listKey, int visibleRows, String message) {
      int listH = visibleRows * 13;
      int itemW = w - 5 - 1;
      PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 12, false, false);
      ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, this.condenseHint(message), x, y, itemW);
   }

   private void scanPayPlayers() {
      this.playerNameOptions.clear();
      this.get_Map.clear();
      this.set_Map.clear();
      this.playerOptionsLoaded = true;
      ScrollableListView vp = this.listViewports.get("pay_players_scan");
      if (vp != null) {
         vp.setScrollOffset(0);
      }

      for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.init(MC)) {
         if (player != null && player.name() != null && !player.name().isBlank() && !this.containsIgnoreCase(this.playerNameOptions, player.name())) {
            this.playerNameOptions.add(player.name());
            if (player.hasPrefix()) {
               this.get_Map.put(player.name().toLowerCase(Locale.ROOT), player.load());
               if (player.prefixComponent() != null) {
                  this.set_Map.putIfAbsent(player.load(), player.prefixComponent());
               }
            }
         }
      }

      BackgroundTaskExecutor.submit("pay-scan-probe", () -> {
         List<String> probed = OnlinePlayerCollector.init(MC, true);
         if (!probed.isEmpty()) {
            MC.execute(() -> {
               boolean changed = false;

               for (String name : probed) {
                  if (!this.containsIgnoreCase(this.playerNameOptions, name)) {
                     this.playerNameOptions.add(name);
                     this.get_Map.putIfAbsent(name.toLowerCase(Locale.ROOT), "unknown");
                     changed = true;
                  }
               }

               if (changed) {
                  this.playerNameOptions.sort(String::compareToIgnoreCase);
               }
            });
         }
      });
      this.playerNameOptions.sort(String::compareToIgnoreCase);
   }

   private String set_Str(String name) {
      return name == null ? "" : this.get_Map.getOrDefault(name.toLowerCase(Locale.ROOT), "");
   }

   private void toggleGroup(List<String> selected, List<String> members) {
      if (selected != null && members != null && !members.isEmpty()) {
         boolean allSelected = members.stream().allMatch(mx -> this.containsIgnoreCase(selected, mx));
         if (allSelected) {
            for (String m : members) {
               this.removeIgnoreCase(selected, m);
            }
         } else {
            for (String m : members) {
               if (!this.containsIgnoreCase(selected, m)) {
                  selected.add(m);
               }
            }
         }
      }
   }

   private void refreshModuleRegistry() {
      this.autismModuleNames.clear();
      ScrollableListView autismVp = this.listViewports.get("toggle_module_autism_registry");
      if (autismVp != null) {
         autismVp.setScrollOffset(0);
      }

      ScrollableListView emptyVp = this.listViewports.get("toggle_module_registry_empty");
      if (emptyVp != null) {
         emptyVp.setScrollOffset(0);
      }

      for (String moduleName : autismclient.util.CommandPrefixHelper.moduleNames()) {
         if (moduleName != null && !moduleName.isBlank() && !this.containsIgnoreCase(this.autismModuleNames, moduleName)) {
            this.autismModuleNames.add(moduleName);
         }
      }

      this.autismModuleNames.sort(String::compareToIgnoreCase);
   }

   private List<String> filterList(List<String> source, String query) {
      return this.filterCached(null, source, query);
   }

   private List<String> filterCached(String cacheKey, List<String> source, String query, Function<String, String> labeler) {
      String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      List<String> target = this.filteredOptionCache.computeIfAbsent(cacheKey, ignored -> new ArrayList<>());
      return autismclient.gui.macro.editor.capture.EditorListRowHelper.load(
         source, target, id -> id != null && matchesFilter(filter, id, registryDisplayName(id), labeler.apply(id))
      );
   }

   private List<String> filterCached(String cacheKey, List<String> source, String query) {
      if (source != null && !source.isEmpty()) {
         String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
         List<String> target = cacheKey == null ? new ArrayList<>() : this.filteredOptionCache.computeIfAbsent(cacheKey, ignored -> new ArrayList<>());
         return autismclient.gui.macro.editor.capture.EditorListRowHelper.load(
            source, target, value -> value != null && (filter.isEmpty() || value.toLowerCase(Locale.ROOT).contains(filter))
         );
      } else {
         return Collections.emptyList();
      }
   }

   private void selectAllMatching(List<String> selected, String query) {
      if (selected != null) {
         for (String player : this.filterList(this.playerNameOptions, query)) {
            if (!this.containsIgnoreCase(selected, player)) {
               selected.add(player);
            }
         }
      }
   }

   private void togglePlayer(List<String> selected, String player) {
      if (selected != null && player != null && !player.isBlank()) {
         if (this.containsIgnoreCase(selected, player)) {
            this.removeIgnoreCase(selected, player);
         } else {
            selected.add(player);
         }
      }
   }

   private boolean containsIgnoreCase(List<String> values, String target) {
      if (values != null && target != null) {
         for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void removeIgnoreCase(List<String> values, String target) {
      if (values != null && target != null) {
         values.removeIf(value -> value != null && value.equalsIgnoreCase(target));
      }
   }

   private void addFirstMatchingModule(String query) {
      List<String> filtered = this.filterList(this.autismModuleNames, query);
      if (!filtered.isEmpty()) {
         this.addToggleModule(filtered.get(0));
      }
   }

   private void addToggleModule(String moduleName) {
      if (this.toggleModuleEntries != null && moduleName != null && !moduleName.isBlank() && !this.hasToggleModule(this.toggleModuleEntries, moduleName)) {
         this.toggleModuleEntries.add(new ak.Inner_DataUtil(moduleName, ak.Inner_StreamUtil.init));
      }
   }

   private boolean hasToggleModule(List<ak.Inner_DataUtil> entries, String moduleName) {
      if (entries != null && moduleName != null) {
         for (ak.Inner_DataUtil entry : entries) {
            if (entry != null && entry.init_Str != null && entry.init_Str.equalsIgnoreCase(moduleName)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String toggleModeLabel(ak.Inner_StreamUtil mode) {
      String var10000;
      switch (mode) {
         case load:
            var10000 = "Enable";
            break;
         case read:
            var10000 = "Disable";
            break;
         default:
            var10000 = "Toggle";
      }

      return var10000;
   }

   private void renderWaitSlotChangeEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      int headerBtnW = this.buttonWidth("Clear");
      this.renderToggleRow(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 16;
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<WaitForSlotChangeAction.Inner_DataUtil> entries = this.slotChangeEntries != null ? this.slotChangeEntries : new ArrayList<>();
      if (this.selectedSlotChangeEntry >= entries.size()) {
         this.selectedSlotChangeEntry = -1;
      }

      autismclient.gui.f.b.k.init(ctx, this.font, "Items / Slots (" + entries.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      boolean canClear = !entries.isEmpty();
      int clearX = x + w - headerBtnW;
      this.renderActionButton(ctx, clearX, cy, headerBtnW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClear, mx, my, () -> {
         entries.clear();
         this.selectedSlotChangeEntry = -1;
         this.clearSlotChangeInputs();
      });
      cy += 14;
      int delW = 13;
      int rowW = w - 5 - 1 - delW - 2;
      int selAreaH = 56;
      ScrollableListView wscViewport = this.viewportFor(this.entryViewports, "_wsc_entries", x, cy, w, selAreaH, 14, 5);
      wscViewport.setContentHeight(entries.size() * 14);
      wscViewport.renderScrollbar(ctx, mx, my);
      if (!entries.isEmpty()) {
         wscViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int firstVis = wscViewport.getFirstVisibleRow();

         for (int i = firstVis; i < entries.size() && i <= wscViewport.getLastVisibleRow(); i++) {
            int iy = wscViewport.getRowY(i);
            if (iy != -2147483648) {
               boolean selected = i == this.selectedSlotChangeEntry;
               WaitForSlotChangeAction.Inner_DataUtil e = entries.get(i);
               Component targetDisp = this.describeItemTarget(e.resolveItemTarget(), "(any slot)");
               String modeSummary = " • " + e.describeMode();
               Component rowLabel = Component.empty().append(targetDisp).append(Component.literal(modeSummary).withStyle(s -> s.withColor(-7829368)));
               boolean rowHovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx, this.font, rowLabel, autismclient.gui.f.NetHelper.init(x, iy, rowW, 13), rowHovered, selected, ListRowRenderer.RowTone.NORMAL, true
               );
               int fi = i;
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.ClickRegion(
                           x, Math.max(cy, iy), rowW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.toggleSlotChangeSelection(fi)
                        )
                     );
               }

               int delX = x + rowW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                  if (this.selectedSlotChangeEntry == fi) {
                     this.selectedSlotChangeEntry = -1;
                     this.clearSlotChangeInputs();
                  } else if (this.selectedSlotChangeEntry > fi) {
                     this.selectedSlotChangeEntry--;
                  }

                  entries.remove(fi);
                  ScrollableListView vp = this.entryViewports.get("_wsc_entries");
                  if (vp != null) {
                     vp.scrollByRows(-1);
                  }
               });
            }
         }

         wscViewport.endFrame(ctx);
      } else {
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No entries yet. Add an item or slot below.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 6;
      ThemedTextInputBox wscAddF = this.internalTextFields.get("_wsc_add");
      ThemedTextInputBox wscSlotF = this.textFields.get("wsc_slot");
      if (wscAddF != null && wscSlotF != null) {
         int pickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
         int slotW = 44;
         int pickX = x + w - pickW;
         int slotX = pickX - 2 - slotW;
         wscAddF.setX(x);
         wscAddF.setY(cy);
         wscAddF.setWidth(slotX - x - 2);
         wscSlotF.setX(slotX);
         wscSlotF.setY(cy);
         wscSlotF.setWidth(slotW);
         wscAddF.render(ctx, mx, my, delta);
         wscSlotF.render(ctx, mx, my, delta);
         boolean capturing = this.captureSession.init("_wsc_entries");
         this.renderCaptureButton(ctx, pickX, cy, pickW, 13, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("_wsc_entries"));
      }

      cy += 16;
      boolean editing = this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < entries.size();
      WaitForSlotChangeAction.Inner_StreamUtil curMode = editing ? entries.get(this.selectedSlotChangeEntry).mode : this.slotChangeCondition;
      int curCount = editing ? entries.get(this.selectedSlotChangeEntry).count : this.slotChangeCount;
      int addBtnW = this.buttonWidth("+Add");
      int cntW = 36;
      WaitForSlotChangeAction.Inner_StreamUtil[] modes = WaitForSlotChangeAction.Inner_StreamUtil.values();
      List<String> modeOptions = new ArrayList<>();

      for (WaitForSlotChangeAction.Inner_StreamUtil m : modes) {
         modeOptions.add(m.name().replace("_", " "));
      }

      int modBtnW = this.dropdownWidth(modeOptions, w - addBtnW - 2 - cntW - 2);
      this.renderDropdown("wsc_mode", modeOptions, curMode.ordinal(), x, cy, modBtnW, 13, idx -> {
         WaitForSlotChangeAction.Inner_StreamUtil chosen = modes[Math.max(0, Math.min(idx, modes.length - 1))];
         if (this.selectedSlotChangeEntry >= 0 && this.selectedSlotChangeEntry < entries.size()) {
            entries.get(this.selectedSlotChangeEntry).mode = chosen;
         } else {
            this.slotChangeCondition = chosen;
         }
      });
      int cntX = x + modBtnW + 2;
      ThemedTextInputBox countF = this.textFields.get("wsc_count");
      boolean countRelevant = curMode == WaitForSlotChangeAction.Inner_StreamUtil.COUNT_AT_LEAST || curMode == WaitForSlotChangeAction.Inner_StreamUtil.COUNT_BELOW;
      if (countF != null) {
         if (countRelevant) {
            countF.setX(cntX);
            countF.setY(cy);
            countF.setWidth(cntW);
            if (!countF.isFocused() && !this.suppressSlotChangeListener) {
               this.suppressSlotChangeListener = true;
               countF.setValue(String.valueOf(curCount));
               this.suppressSlotChangeListener = false;
            }

            countF.render(ctx, mx, my, delta);
         } else {
            PanelSurfaceHelper.drawInputPill(ctx, cntX, cy, cntW, 13);
            this.fillRect(ctx, cntX, cy, cntW, 13, -14018022);
            autismclient.gui.f.b.k.init(ctx, this.font, "-", font, UiColorPalette.textSecondary(), cntX + (cntW - this.textWidth(font, "-")) / 2, cy + 3, false);
         }
      }

      int plusX = cntX + cntW + 2;
      this.renderActionButton(ctx, plusX, cy, addBtnW, 13, "+Add", OverlayActionButton.ButtonTone.SUCCESS, true, mx, my, () -> {
         if (wscAddF != null && wscSlotF != null) {
            this.addSlotChangeEntry(wscAddF, wscSlotF);
            this.selectedSlotChangeEntry = -1;
            this.clearSlotChangeInputs();
         }
      });
      cy += 16;
      this.renderHint(ctx, x, cy, w, this.selectedSlotChangeEntry >= 0 ? "Editing row - all entries must match." : "Click a row to edit - all must match.");
   }

   private void renderUseItemEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.renderPlaceItemSection(ctx, x, cy, w, mx, my, delta);
      int[] useBtns = this.layoutButtons(x, w, false, "Use Held", "Use Current");
      this.renderSecondaryButton(ctx, useBtns[0], cy, useBtns[1], 13, "Use Held", mx, my, this::useHeldItemTarget);
      this.renderSecondaryButton(ctx, useBtns[2], cy, useBtns[3], 13, "Use Current", mx, my, () -> {
         ThemedTextInputBox field = this.textFields.get("itemName");
         if (field != null) {
            field.setValue("");
         }

         ThemedTextInputBox slotField = this.textFields.get("slot");
         if (slotField != null) {
            slotField.setValue("");
         }

         this.itemTargets.remove("itemName");
      });
      cy += 18;
      cy = this.renderFieldByKey(ctx, "useMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForFinish", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "sneak", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "holdTicks", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "useCount", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGui", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "guiName", x, cy, w, mx, my, delta);
      String itemTarget = this.run_Str();
      String mode = this.enumValue("useMode");
      String hint = "CUSTOM_HOLD".equals(mode) ? "Hold-uses " + itemTarget + " for the set ticks." : "Uses " + itemTarget + " for the set count.";
      this.renderHint(ctx, x, cy, w, hint);
   }

   private int renderPlaceItemSection(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      FieldDef itemFieldDef = this.fieldByKey("itemName");
      ThemedTextInputBox itemField = this.textFields.get("itemName");
      ThemedTextInputBox slotField = this.textFields.get("slot");
      if (itemFieldDef != null && itemField != null && slotField != null) {
         Identifier font = this.styleTheme.init_Identifier_l(l.read);
         int labelW = this.labelWidth(w, itemFieldDef.label(), font, 88);
         this.drawTrimmedText(ctx, itemFieldDef.label(), x, y, labelW, font);
         int controlX = this.controlX(x, labelW);
         int controlW = Math.max(1, this.valueFieldWidth(w, labelW));
         int pickW = this.captureButtonWidth(CaptureMode.ITEM_SLOT, null, null);
         int slotW = 34;
         int gap = 2;
         int itemW = Math.max(1, controlW - pickW - slotW - gap * 2);
         itemField.setX(controlX);
         itemField.setY(y + 2);
         itemField.setWidth(itemW);
         itemField.render(ctx, mx, my, delta);
         int slotX = controlX + itemW + gap;
         slotField.setX(slotX);
         slotField.setY(y + 2);
         slotField.setWidth(slotW);
         slotField.render(ctx, mx, my, delta);
         int pickX = slotX + slotW + gap;
         boolean capturing = this.captureSession.init("itemName");
         this.renderCaptureButton(ctx, pickX, y + 2, pickW, 14, CaptureMode.ITEM_SLOT, capturing, true, mx, my, () -> this.startCapture("itemName"));
         return y + 14 + 2;
      } else {
         int cy = this.renderFieldByKey(ctx, "itemName", x, y, w, mx, my, delta);
         return this.renderFieldByKey(ctx, "slot", x, cy, w, mx, my, delta);
      }
   }

   private void renderRotateEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "yaw", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "pitch", x, cy, w, mx, my, delta);
      this.renderSecondaryButton(ctx, x, cy, Math.min(w, this.buttonWidth("Capture View")), 13, "Capture View", mx, my, this::captureCurrentView);
      cy += 18;
      cy = this.renderFieldByKey(ctx, "smooth", x, cy, w, mx, my, delta);
      if (this.toggleStates.getOrDefault("smooth", false)) {
         cy = this.renderSmoothnessSlider(ctx, x, cy, w, mx, my);
      } else {
         this.draggingSmoothness = false;
         this.resetSmoothnessSlider();
      }

      cy = this.renderFieldByKey(ctx, "waitForCompletion", x, cy, w, mx, my, delta);
      this.renderHint(
         ctx,
         x,
         cy,
         w,
         this.toggleStates.getOrDefault("smooth", false)
            ? "Capture View fills in your current yaw and pitch. Smoothness 1 is faster, 10 is slower."
            : "Capture View fills in your current yaw and pitch."
      );
   }

   private void renderLookAtBlockEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "targetMode", x, cy, w, mx, my, delta);
      String mode = this.enumValue("targetMode");
      if ("BLOCK".equals(mode)) {
         cy = this.renderFieldByKey(ctx, "searchRadius", x, cy, w, mx, my, delta);
         cy = this.renderFieldByKey(ctx, "blockIds", x, cy, w, mx, my, delta);
      } else if ("ENTITY".equals(mode)) {
         cy = this.renderFieldByKey(ctx, "searchRadius", x, cy, w, mx, my, delta);
         cy = this.renderEntryList(ctx, x, cy, w, mx, my, delta);
      } else {
         cy = this.renderFieldByKey(ctx, "blockPos", x, cy, w, mx, my, delta);
      }

      cy = this.renderFieldByKey(ctx, "smooth", x, cy, w, mx, my, delta);
      if (this.toggleStates.getOrDefault("smooth", false)) {
         cy = this.renderSmoothnessSlider(ctx, x, cy, w, mx, my);
      } else {
         this.draggingSmoothness = false;
         this.resetSmoothnessSlider();
      }

      cy = this.renderFieldByKey(ctx, "waitForCompletion", x, cy, w, mx, my, delta);
      String var10000;
      switch (mode) {
         case "BLOCK":
            var10000 = "Search blocks and it faces the nearest matching block in range.";
            break;
         case "ENTITY":
            var10000 = "Pick entity types or specific captures and it faces the nearest match in range.";
            break;
         default:
            var10000 = "Pick a specific block position to face.";
      }

      String hint = var10000;
      if (this.toggleStates.getOrDefault("smooth", false)) {
         hint = hint + " Smoothness 1 is faster, 10 is slower.";
      }

      this.renderHint(ctx, x, cy, w, hint);
   }

   private int renderEntryList(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = y;
      List<String> selected = this.listFields.getOrDefault("entityIds", Collections.emptyList());
      cy = this.renderSimpleEntryRows(
         ctx, x, cy, w, mx, my, "look_at_entity_selected", "Entities", selected, selected::remove, this::entityEntryLabel, "(add entity types or specific captures)"
      );
      ThemedTextInputBox search = this.internalTextFields.get("entityIds");
      String modeLabel = this.captureSpecificEntity ? "[Spec]" : "[Type]";
      int modeW = Math.max(this.buttonWidth("[Spec]"), this.buttonWidth("[Type]"));
      int capW = this.buttonWidth("CAP");
      int searchW = w - modeW - 2 - capW - 2;
      if (search != null) {
         search.setX(x);
         search.setY(cy);
         search.setWidth(searchW);
         search.render(ctx, mx, my, delta);
      }

      int modeX = x + searchW + 2;
      this.renderSecondaryButton(ctx, modeX, cy, modeW, 13, modeLabel, mx, my, () -> this.captureSpecificEntity = !this.captureSpecificEntity);
      this.renderSecondaryButton(ctx, modeX + modeW + 2, cy, capW, 13, "CAP", mx, my, this::captureLookAtBlock);
      cy += 18;
      String filter = search != null ? search.getValue().trim().toLowerCase() : "";
      List<String> filtered = this.filterCached("look_at_entity_search", entityTypeIds(), filter, RegistryDisplayNameCache::read);
      this.renderSelectableList(ctx, x, cy, w, mx, my, "look_at_entity_search", filtered, 4, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, RegistryDisplayNameCache::read);
      cy += 66;
      autismclient.gui.f.b.k.init(ctx, this.font, "Nearby Entities", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 13;
      List<String> nearbyEntries = this.nearbyEntityEntries();
      this.renderSelectableList(ctx, x, cy, w, mx, my, "look_at_entity_nearby", nearbyEntries, 3, value -> {
         String stored = this.captureSpecificEntity ? value : this.entityEntryId(value);
         if (!stored.isBlank()) {
            if (this.captureSpecificEntity) {
               boolean removed = selected.removeIf(existing -> existing.startsWith(this.entityEntryPrefix(stored)));
               if (!removed) {
                  selected.add(stored);
               }
            } else if (selected.contains(stored)) {
               selected.remove(stored);
            } else {
               selected.add(stored);
            }
         }
      }, value -> {
         String stored = this.captureSpecificEntity ? value : this.entityEntryId(value);
         return this.captureSpecificEntity ? selected.stream().anyMatch(existing -> existing.startsWith(this.entityEntryPrefix(stored))) : selected.contains(stored);
      }, this::entityEntryDisplay);
      return cy + 39 + 4;
   }

   private void renderGoToEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      FieldDef posField = this.fieldByKey("pos");
      if (posField != null) {
         cy = this.init_I_GuiGraphicsExtract_4(ctx, posField, x, cy, w, mx, my, delta);
      }

      this.renderSecondaryButton(ctx, x, cy, Math.min(w, this.buttonWidth("Capture Here")), 13, "Capture Here", mx, my, this::capturePlayerPosition);
      cy += 18;
      cy = this.renderFieldByKey(ctx, "waitForArrival", x, cy, w, mx, my, delta);
      this.renderHint(ctx, x, cy, w, "Wait for Arrival continues only after navigation finishes.");
   }

   private void renderSwapSlotsEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      autismclient.gui.f.b.k.init(ctx, this.font, "From", font, UiColorPalette.textBody(), x, cy + 2, false);
      int flipW = this.buttonWidth("Flip Ends");
      this.renderSecondaryButton(ctx, x + w - flipW, cy, flipW, 13, "Flip Ends", mx, my, this::flipSwapEnds);
      cy += 16;
      cy = this.renderFieldByKey(ctx, "fromUseItemName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "fromItemName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "fromSlot", x, cy, w, mx, my, delta);
      PanelSurfaceHelper.drawDividerColored(ctx, x, cy + 2, w, UiColorPalette.divider());
      cy += 8;
      autismclient.gui.f.b.k.init(ctx, this.font, "To", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 16;
      cy = this.renderFieldByKey(ctx, "toUseItemName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "toItemName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "toSlot", x, cy, w, mx, my, delta);
      this.renderHint(ctx, x, cy, w, this.swapSlotsHint());
   }

   private void renderClickEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "clickType", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "clickCount", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "guiName", x, cy, w, mx, my, delta);
      this.renderHint(ctx, x, cy, w, this.clickTypeHint());
   }

   private void renderDisconnectEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "delayMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "lagMethod", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "kickMethod", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "packetCount", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "useNextAction", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "trigger", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "tolerance", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "bufferMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "timeoutSec", x, cy, w, mx, my, delta);
      String mode = this.enumValue("mode");
      String var10000;
      switch (mode) {
         case "DISCONNECT":
            var10000 = "Simple disconnect. Delay only controls how long to wait before closing the connection.";
            break;
         case "KICK":
            var10000 = "Sends lag packets, then the selected kick packet. Packet Count controls how hard it pushes.";
            break;
         case "AUTO_DISCONNECT":
            var10000 = "Auto-disconnects at the perfect dupe timing.";
            break;
         case "KICK_DUPE":
            var10000 = this.toggleStates.getOrDefault("useNextAction", false)
               ? "Kick Dupe will run the next eligible macro actions inside the lag sandwich, then kick."
               : "Kick Dupe will try to use a bundle, then kick.";
            break;
         default:
            var10000 = "Unknown mode";
      }

      String hint = var10000;
      this.renderHintColored(ctx, x, cy, w, hint, "DISCONNECT".equals(mode) ? UiColorPalette.textSecondary() : -19605);
   }

   private void renderWaitGuiEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "waitMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "guiType", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "matchMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "guiTitle", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_2(ctx, this.textFields.get("guiTitle"), x, cy, w, mx, my);
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      String mode = this.enumValue("waitMode");
      String guiType = this.enumValue("guiType");
      String hint = "CLOSE".equals(mode) ? "Waits until the matching " + guiType + " GUI closes." : "Waits until a matching " + guiType + " GUI opens.";
      this.renderHint(ctx, x, cy, w, hint);
   }

   private void renderSelectSlotEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "itemName", x, cy, w, mx, my, delta);
      int[] slotBtns = this.layoutButtons(x, w, false, "Held", "Use Slot Only");
      this.renderSecondaryButton(ctx, slotBtns[0], cy, slotBtns[1], 13, "Held", mx, my, this::selectHeldItemTarget);
      this.renderSecondaryButton(ctx, slotBtns[2], cy, slotBtns[3], 13, "Use Slot Only", mx, my, this::clearSelectSlotItemName);
      cy += 18;
      autismclient.gui.f.b.k.init(ctx, this.font, "Fallback Hotbar Slot", font, UiColorPalette.textBody(), x, cy + 2, false);
      cy += 12;
      cy = this.renderHotbarSlotPicker(ctx, x, cy, w, mx, my);
      cy = this.renderFieldByKey(ctx, "strategy", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "outputVariable", x, cy, w, mx, my, delta);
      this.renderHint(ctx, x, cy, w, this.useItemSummary());
   }

   private int renderHotbarSlotPicker(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int selectedSlot = this.selectedHotbarSlot();
      int slotGap = 2;
      int cellW = Math.max(16, Math.min(24, (w - slotGap * 8) / 9));
      int totalW = cellW * 9 + slotGap * 8;
      int startX = x + Math.max(0, (w - totalW) / 2);

      for (int slot = 0; slot < 9; slot++) {
         int sx = startX + slot * (cellW + slotGap);
         boolean selected = selectedSlot == slot;
         String label = String.valueOf(slot + 1);
         int clickedSlot = slot;
         this.renderActionButton(
            ctx,
            sx,
            y,
            cellW,
            13,
            label,
            selected ? OverlayActionButton.ButtonTone.PRIMARY : OverlayActionButton.ButtonTone.GHOST,
            true,
            mx,
            my,
            () -> this.setSelectSlotField(clickedSlot)
         );
      }

      return y + 18;
   }

   private void renderWaitCooldownEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "itemName", x, cy, w, mx, my, delta);
      String handLbl = this.handLabel();
      int[] handBtns = this.layoutButtons(x, w, false, handLbl, "Capture Held");
      this.renderSecondaryButton(ctx, handBtns[0], cy, handBtns[1], 13, handLbl, mx, my, this::toggleCheckMainHand);
      this.renderSecondaryButton(ctx, handBtns[2], cy, handBtns[3], 13, "Capture Held", mx, my, this::captureCooldownHeldItem);
      cy += 18;
      this.renderSecondaryButton(ctx, x, cy, Math.min(w, this.buttonWidth("Use InteractionHand Item")), 13, "Use InteractionHand Item", mx, my, this::clearCooldownItemName);
      cy += 18;
      this.renderHint(ctx, x, cy, w, this.set_Str());
   }

   private void renderWaitInventoryPredicateEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "listenDuringPreviousAction", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "condition", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "itemName", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "count", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "slot", x, cy, w, mx, my, delta);
      if (this.isFieldApplicable(this.fieldByKey("slot"))) {
         int[] slotBtns = this.layoutButtons(x, w, false, "Selected", "Pick Slot", "Slot 0");
         this.renderSecondaryButton(ctx, slotBtns[0], cy, slotBtns[1], 13, "Selected", mx, my, this::useSelectedHotbarSlot);
         boolean capturing = this.captureSession.init("wait_inventory_slot");
         this.renderCaptureButton(
            ctx, slotBtns[2], cy, slotBtns[3], 13, CaptureMode.ITEM_SLOT, capturing, true, mx, my, "Pick Slot", "Done", this::startWaitInventorySlotCapture
         );
         this.renderSecondaryButton(ctx, slotBtns[4], cy, slotBtns[5], 13, "Slot 0", mx, my, () -> this.setWaitSlotField(0));
         cy += 18;
      }

      cy = this.renderFieldByKey(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
   }

   private void renderCaptureValueEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      this.syncCaptureSourceDefaults();
      cy = this.renderFieldByKey(ctx, "source", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "autofillCommand", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "autofillTimeoutMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "autofillCacheList", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "itemFilter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "slot", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "itemText", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listSelection", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listPickPosition", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listFilter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listFilterText", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listExcludeText", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "listStripPrefix", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "excludeSelf", x, cy, w, mx, my, delta);
      if ("RECENT_CHAT".equals(this.enumValue("source"))) {
         cy = this.renderRecentChat(ctx, x, cy, w, mx, my);
      } else if ("SCOREBOARD".equals(this.enumValue("source"))) {
         cy = this.renderSidebarLines(ctx, x, cy, w, mx, my);
      } else if ("TABLIST".equals(this.enumValue("source"))) {
         cy = this.renderCandidatePool(ctx, x, cy, w, mx, my);
      }

      cy = this.renderFieldByKey(ctx, "exampleText", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "selectedText", x, cy, w, mx, my, delta);
      autismclient.util.macro.CaptureValueAction draft = this.buildCaptureDraft();
      String primaryName = this.init_Str_TaskQueue(draft);
      String token = primaryName.isBlank() ? "" : "{" + primaryName + "}";
      boolean exampleCapture = draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.RECENT_CHAT
         || draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.GUI_TITLE
         || draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.SCOREBOARD;
      autismclient.util.macro.CaptureValueAction.CaptureResult preview = exampleCapture && !draft.exampleText.isBlank()
         ? draft.previewExample()
         : draft.preview(MC);
      String previewText = preview.success() ? preview.textOf(primaryName) : preview.load();
      if (preview.success() && previewText.isBlank()) {
         previewText = preview.text();
      }

      boolean derivingPattern = exampleCapture && !draft.exampleText.isBlank() && draft.matchMode != PatternCaptureMatcher.MatchMode.MATCH;
      if (derivingPattern && draft.selectedText.isBlank()) {
         previewText = "Set Capture This";
      } else if (derivingPattern && draft.pattern.isBlank()) {
         previewText = "Not in example";
      }

      cy = this.renderCopyableValue(ctx, "Use Later", "_capture_token", token, x, cy, w, mx, my, "Variable copied.");
      cy = this.renderCopyableValue(ctx, "Preview Now", "_capture_preview", previewText, x, cy, w, mx, my, "Preview copied.");
      cy = this.renderFieldByKey(ctx, "numberMode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "numberModifier", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "numberModifierAmount", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForTrigger", x, cy, w, mx, my, delta);
      String behaviorHint;
      if (draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.COMMAND_AUTOFILL) {
         behaviorHint = "Asks server when run";
      } else if (draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.TABLIST) {
         behaviorHint = draft.waitForTrigger ? "Waits for player" : "Picks online player";
      } else {
         behaviorHint = draft.waitForTrigger
            ? (draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.SCOREBOARD ? "Waits for row change" : "Waits for next match")
            : (
               draft.source != autismclient.util.macro.CaptureValueAction.CaptureSource.RECENT_CHAT
                     && draft.source != autismclient.util.macro.CaptureValueAction.CaptureSource.GUI_TITLE
                  ? "Reads value now"
                  : "Uses newest match"
            );
      }

      if (!draft.saveAs.isBlank() && MacroVariableStore.load(draft.saveAs).isBlank()) {
         behaviorHint = "Bad variable name";
      }

      this.renderHint(ctx, x, cy, w, behaviorHint);
   }

   private int renderRecentChat(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      List<MacroExecutor.LoggedChatMessage> history = MacroExecutor.getRecentChatMessages().stream().limit(6L).toList();
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, "Recent Chat", font, UiColorPalette.textBody(), x, y + 2, false);
      y += 13;
      int listH = this.candidateListHeight(history.size());
      this.renderChatList(ctx, x, y, w, listH, mx, my, history, "capture_value_recent", 3, this::useChatLineAsExample);
      return y + listH + 2;
   }

   private void useChatLineAsExample(MacroExecutor.LoggedChatMessage entry) {
      if (entry != null) {
         String text = this.chatEntryText(entry);
         String suggested = autismclient.util.macro.CaptureValueAction.extractNumber(text);
         ThemedTextInputBox exampleField = this.textFields.get("exampleText");
         ThemedTextInputBox selectedField = this.textFields.get("selectedText");
         this.suppressFieldListeners = true;
         if (exampleField != null) {
            exampleField.setValue(text);
         }

         if (selectedField != null) {
            selectedField.setValue(suggested);
         }

         this.suppressFieldListeners = false;
         this.capturePattern = this.textFieldValueOr("pattern", "");
         this.updateCapturePatternFromExample();
      }
   }

   private int renderSidebarLines(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      List<autismclient.util.macro.CaptureValueAction.SidebarLine> lines = autismclient.util.macro.CaptureValueAction.sidebarLines(MC);
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      String title = !lines.isEmpty() && !lines.get(0).read().isBlank() ? "Scoreboard - " + lines.get(0).read() : "Scoreboard";
      autismclient.gui.f.b.k.init(ctx, this.font, autismclient.gui.f.b.k.init(this.font, title, w, font, -1), font, UiColorPalette.textBody(), x, y + 2, false);
      y += 13;
      int listH = lines.isEmpty() ? 24 : Math.min(5, lines.size()) * 22;
      int itemW = Math.max(1, w - 5 - 1);
      ScrollableListView viewport = this.viewportFor(this.entryViewports, "capture_value_scoreboard", x, y, w, listH, 22, 5);
      viewport.setContentHeight(lines.size() * 22);
      viewport.renderScrollbar(ctx, mx, my);
      if (lines.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 24, false, false);
         autismclient.gui.f.b.k.init(ctx, this.font, "No scoreboard", font, UiColorPalette.textSecondary(), x + 3, y + 7, false);
         return y + 24 + 2;
      }

      String selectedKey = this.textFieldValueOr("scoreboardRow", "");
      viewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
      int first = viewport.getFirstVisibleRow();

      for (int i = first; i < lines.size() && i <= viewport.getLastVisibleRow(); i++) {
         int rowY = viewport.getRowY(i);
         if (rowY != -2147483648) {
            autismclient.util.macro.CaptureValueAction.SidebarLine line = lines.get(i);
            boolean selected = line.key().equals(selectedKey);
            boolean hovered = mx >= x && mx < x + itemW && my >= rowY && my < rowY + 22;
            PanelSurfaceHelper.drawCardRow(ctx, x, rowY, itemW, 22, hovered, selected);
            PanelSurfaceHelper.drawAccentBar(ctx, x, rowY + 2, 3, 18, selected ? -13248397 : -3376606);
            String rowLabel = "Row " + (line.row() + 1);
            autismclient.gui.f.b.k.init(ctx, this.font, rowLabel, font, UiColorPalette.textSecondary(), x + 6, rowY + 2, false);
            autismclient.gui.f.b.k.init(
               ctx,
               this.font,
               autismclient.gui.f.b.k.init(this.font, line.flush(), Math.max(20, itemW - 10), font, -1),
               font,
               UiColorPalette.textBright(),
               x + 6,
               rowY + 11,
               false
            );
            autismclient.util.macro.CaptureValueAction.SidebarLine selectedLine = line;
            this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, rowY, itemW, 22, () -> this.useSidebarLine(selectedLine)));
         }
      }

      viewport.endFrame(ctx);
      return y + listH + 2;
   }

   private List<String> tabPlayerNames() {
      long now = System.currentTimeMillis();
      if (this.cachedTabPlayerNames == null || now - this.tabPlayerNamesFetchedAt > 500L) {
         List<TabPlayerListParser.ParsedPlayer> players = TabPlayerListParser.init(MC);
         List<String> names = new ArrayList<>(players.size());

         for (TabPlayerListParser.ParsedPlayer player : players) {
            names.add(player.name());
         }

         this.cachedTabPlayerNames = names;
         this.tabPlayerNamesFetchedAt = now;
      }

      return this.cachedTabPlayerNames;
   }

   private List<String> candidateNames(autismclient.util.macro.CaptureValueAction draft) {
      List<String> names = new ArrayList<>(this.tabPlayerNames());
      if (!draft.excludeSelf && MC.player != null && MC.player.getGameProfile() != null) {
         names.add(MC.player.getGameProfile().name());
         names.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return names;
   }

   private int matchingCandidateCount() {
      autismclient.util.macro.CaptureValueAction draft = this.buildCaptureDraft();
      return draft.filterCandidates(this.candidateNames(draft)).size();
   }

   private int renderCandidatePool(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      autismclient.util.macro.CaptureValueAction draft = this.buildCaptureDraft();
      List<String> names = this.candidateNames(draft);
      List<String> pool = draft.filterCandidates(names);
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, "Matches (" + pool.size() + " of " + names.size() + ")", font, UiColorPalette.textBody(), x, y + 2, false);
      y += 13;
      int listH = pool.isEmpty() ? 24 : Math.min(5, pool.size()) * 14;
      int itemW = Math.max(1, w - 5 - 1);
      ScrollableListView viewport = this.viewportFor(this.entryViewports, "capture_value_tablist", x, y, w, listH, 14, 5);
      viewport.setContentHeight(pool.size() * 14);
      viewport.renderScrollbar(ctx, mx, my);
      if (pool.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 24, false, false);
         autismclient.gui.f.b.k.init(ctx, this.font, "No matches", font, UiColorPalette.textSecondary(), x + 3, y + 7, false);
         return y + 24 + 2;
      }

      viewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
      int first = viewport.getFirstVisibleRow();

      for (int i = first; i < pool.size() && i <= viewport.getLastVisibleRow(); i++) {
         int rowY = viewport.getRowY(i);
         if (rowY != -2147483648) {
            String name = pool.get(i);
            boolean hovered = mx >= x && mx < x + itemW && my >= rowY && my < rowY + 14;
            PanelSurfaceHelper.drawCardRow(ctx, x, rowY, itemW, 14, hovered, false);
            autismclient.gui.f.b.k.init(
               ctx,
               this.font,
               autismclient.gui.f.b.k.init(this.font, name, Math.max(20, itemW - 10), font, -1),
               font,
               UiColorPalette.textBright(),
               x + 6,
               rowY + 3,
               false
            );
            this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, rowY, itemW, 14, () -> this.copyToClipboard(name, "Name copied.")));
         }
      }

      viewport.endFrame(ctx);
      return y + listH + 2;
   }

   private void useSidebarLine(autismclient.util.macro.CaptureValueAction.SidebarLine line) {
      if (line != null) {
         ThemedTextInputBox rowField = this.textFields.get("scoreboardRow");
         ThemedTextInputBox rowIndexField = this.textFields.get("scoreboardRowIndex");
         ThemedTextInputBox objectiveField = this.textFields.get("scoreboardObjective");
         ThemedTextInputBox exampleField = this.textFields.get("exampleText");
         ThemedTextInputBox selectedField = this.textFields.get("selectedText");
         String suggested = autismclient.util.macro.CaptureValueAction.extractNumber(line.flush());
         this.suppressFieldListeners = true;
         if (rowField != null) {
            rowField.setValue(line.key());
         }

         if (rowIndexField != null) {
            rowIndexField.setValue(Integer.toString(line.row()));
         }

         if (objectiveField != null) {
            objectiveField.setValue(line.load());
         }

         if (exampleField != null) {
            exampleField.setValue(line.flush());
         }

         if (selectedField != null) {
            selectedField.setValue(suggested);
         }

         this.suppressFieldListeners = false;
         this.capturePattern = this.textFieldValueOr("pattern", "");
         this.updateCapturePatternFromExample();
      }
   }

   private String dynamicFieldLabel(FieldDef field) {
      if ("waitForTrigger".equals(field.key()) && this.editingAction instanceof autismclient.util.macro.CaptureValueAction) {
         String var10000;
         switch (this.enumValue("source") == null ? "" : this.enumValue("source")) {
            case "RECENT_CHAT":
            case "GUI_TITLE":
               var10000 = "Wait for New";
               break;
            case "SCOREBOARD":
               var10000 = "Wait for Change";
               break;
            default:
               var10000 = "Wait for Match";
         }

         return var10000;
      } else {
         return field.label();
      }
   }

   private void syncCaptureSourceDefaults() {
      String source = this.enumValue("source");
      if (source != null && !source.equals(this.lastCaptureSource)) {
         boolean firstFrame = this.lastCaptureSource.isEmpty();
         this.lastCaptureSource = source;
         if (!firstFrame && !this.waitForTriggerTouched) {
            autismclient.util.macro.CaptureValueAction.CaptureSource parsed;
            try {
               parsed = autismclient.util.macro.CaptureValueAction.CaptureSource.valueOf(source);
            } catch (IllegalArgumentException ignored) {
               return;
            }

            boolean waits = autismclient.util.macro.CaptureValueAction.isRecentChatSource(parsed);
            this.toggleStates.put("waitForTrigger", waits);
            if (this.actionTag != null) {
               this.actionTag.putBoolean("waitForTrigger", waits);
            }
         }
      }
   }

   private void updateCapturePatternFromExample() {
      if (!this.suppressFieldListeners && this.editingAction instanceof autismclient.util.macro.CaptureValueAction) {
         String source = this.enumValue("source");
         if ("GUI_TITLE".equals(source) || "RECENT_CHAT".equals(source) || "SCOREBOARD".equals(source)) {
            String example = this.textFieldValueOr("exampleText", "");
            String selected = this.textFieldValueOr("selectedText", "");
            String variable = this.textFieldValueOr("saveAs", "value");
            if (this.actionTag != null) {
               this.actionTag.putString("exampleText", example);
               this.actionTag.putString("selectedText", selected);
            }

            ThemedTextInputBox patternField = this.textFields.get("pattern");
            String current = patternField != null
               ? patternField.getValue()
               : (this.actionTag == null ? "" : this.actionTag.getStringOr("pattern", ""));
            if (current.isBlank() || current.equals(this.capturePattern)) {
               if (!example.isBlank() && !selected.isBlank()) {
                  String generated = autismclient.util.macro.CaptureValueAction.init(example, selected, variable);
                  if (!generated.isBlank()) {
                     if (patternField != null) {
                        patternField.setValue(generated);
                     }

                     if (this.actionTag != null) {
                        this.actionTag.putString("pattern", generated);
                     }

                     this.capturePattern = generated;
                     if (!generated.isBlank()) {
                        this.modeIndices.put("matchMode", 1);
                        if (this.actionTag != null) {
                           this.actionTag.putString("matchMode", "CAPTURE");
                        }
                     }
                  }
               } else {
                  if (patternField != null) {
                     patternField.setValue("");
                  }

                  if (this.actionTag != null) {
                     this.actionTag.putString("pattern", "");
                  }

                  this.capturePattern = "";
               }
            }
         }
      }
   }

   private int candidateListHeight(int count) {
      return count <= 0 ? 24 : Math.min(3, Math.min(6, count)) * 34;
   }

   private void clipHitRegions(int fromIndex, autismclient.gui.f.NetHelper clip) {
      if (clip != null) {
         for (int i = this.valid_List.size() - 1; i >= Math.max(0, fromIndex); i--) {
            ActionEditorOverlay.ClickRegion region = this.valid_List.get(i);
            int left = Math.max(region.x, clip.read());
            int top = Math.max(region.y, clip.write());
            int right = Math.min(region.x + region.width, clip.init());
            int bottom = Math.min(region.y + region.height, clip.load());
            if (right <= left || bottom <= top) {
               this.valid_List.remove(i);
            } else if (left != region.x || top != region.y || right != region.x + region.width || bottom != region.y + region.height) {
               this.valid_List.set(i, new ActionEditorOverlay.ClickRegion(left, top, right - left, bottom - top, region.clickHandler));
            }
         }
      }
   }

   private int renderCopyableValue(
      GuiGraphicsExtractor ctx, String label, String fieldKey, String value, int x, int y, int w, int mx, int my, String copiedMessage
   ) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.font, label, font, UiColorPalette.textBody(), x, y + 2, false);
      y += 12;
      int buttonW = this.buttonWidth("Copy");
      ThemedTextInputBox field = this.internalTextFields.get(fieldKey);
      if (field != null) {
         String safe = value == null ? "" : value;
         if (!safe.equals(field.getValue())) {
            field.setValue(safe);
         }

         field.setX(x);
         field.setY(y);
         field.setWidth(Math.max(1, w - buttonW - 4));
         field.setHeight(13);
         field.render(ctx, mx, my, this.frameDelta);
      }

      String copied = value == null ? "" : value;
      this.renderSecondaryButton(ctx, x + w - buttonW, y, buttonW, 13, "Copy", mx, my, () -> this.copyToClipboard(copied, copiedMessage));
      return y + 18;
   }

   private void copyToClipboard(String value, String message) {
      if (value != null && !value.isBlank()) {
         MC.keyboardHandler.setClipboard(value);
         bi.init(message);
      } else {
         bi.read("Nothing to copy.");
      }
   }

   private autismclient.util.macro.CaptureValueAction buildCaptureDraft() {
      autismclient.util.macro.CaptureValueAction draft = new autismclient.util.macro.CaptureValueAction();

      try {
         draft.source = autismclient.util.macro.CaptureValueAction.CaptureSource.valueOf(this.enumValue("source"));
      } catch (IllegalArgumentException | NullPointerException var13) {
      }

      try {
         draft.matchMode = PatternCaptureMatcher.MatchMode.valueOf(this.enumValue("matchMode"));
      } catch (IllegalArgumentException | NullPointerException var12) {
      }

      try {
         draft.itemText = autismclient.util.macro.CaptureValueAction.ItemTextMode.valueOf(this.enumValue("itemText"));
      } catch (IllegalArgumentException | NullPointerException var11) {
      }

      draft.saveAs = this.textFieldValueOr("saveAs", "value");
      draft.pattern = this.textFieldValueOr("pattern", "");
      draft.exampleText = this.textFieldValueOr("exampleText", "");
      draft.selectedText = this.textFieldValueOr("selectedText", "");
      draft.itemFilter = this.textFieldValueOr("itemFilter", "");
      draft.scoreboardRow = this.textFieldValueOr("scoreboardRow", "");
      draft.scoreboardObjective = this.textFieldValueOr("scoreboardObjective", "");

      try {
         draft.scoreboardRowIndex = Integer.parseInt(this.textFieldValueOr("scoreboardRowIndex", "-1").trim());
      } catch (NumberFormatException ignored) {
         draft.scoreboardRowIndex = -1;
      }

      try {
         draft.numberMode = autismclient.util.macro.CaptureValueAction.NumberMode.valueOf(this.enumValue("numberMode"));
      } catch (IllegalArgumentException | NullPointerException var9) {
      }

      try {
         draft.numberModifier = autismclient.util.macro.CaptureValueAction.NumberModifier.valueOf(this.enumValue("numberModifier"));
      } catch (IllegalArgumentException | NullPointerException var8) {
      }

      try {
         draft.numberModifierAmount = Double.parseDouble(this.textFieldValueOr("numberModifierAmount", "1").trim());
      } catch (NumberFormatException ignored) {
         draft.numberModifierAmount = this.actionTag == null ? 1.0 : this.actionTag.getDoubleOr("numberModifierAmount", 1.0);
      }

      draft.waitForTrigger = this.toggleStates
         .getOrDefault("waitForTrigger", this.actionTag == null || this.actionTag.getBooleanOr("waitForTrigger", true));

      try {
         draft.slot = Integer.parseInt(this.textFieldValueOr("slot", "-1").trim());
      } catch (NumberFormatException ignored) {
         draft.slot = -1;
      }

      draft.autofillCommand = this.textFieldValueOr("autofillCommand", "");

      try {
         draft.autofillTimeoutMs = Integer.parseInt(this.textFieldValueOr("autofillTimeoutMs", "5000").trim());
      } catch (NumberFormatException ignored) {
         draft.autofillTimeoutMs = 5000;
      }

      draft.autofillCacheList = this.toggleStates
         .getOrDefault("autofillCacheList", this.actionTag != null && this.actionTag.getBooleanOr("autofillCacheList", false));

      try {
         draft.listSelection = autismclient.util.macro.CandidatePoolSelector.Inner_CachePool.valueOf(this.enumValue("listSelection"));
      } catch (IllegalArgumentException | NullPointerException var5) {
      }

      try {
         draft.listFilter = autismclient.util.macro.CandidatePoolSelector.Inner_DataUtil.valueOf(this.enumValue("listFilter"));
      } catch (IllegalArgumentException | NullPointerException var4) {
      }

      draft.listFilterText = this.textFieldValueOr("listFilterText", "");
      draft.listExcludeText = this.textFieldValueOr("listExcludeText", "");

      try {
         draft.listPickPosition = Integer.parseInt(this.textFieldValueOr("listPickPosition", "1").trim());
      } catch (NumberFormatException ignored) {
         draft.listPickPosition = 1;
      }

      draft.listStripPrefix = this.toggleStates
         .getOrDefault("listStripPrefix", this.actionTag != null && this.actionTag.getBooleanOr("listStripPrefix", false));
      draft.excludeSelf = this.toggleStates.getOrDefault("excludeSelf", this.actionTag == null || this.actionTag.getBooleanOr("excludeSelf", true));
      return draft;
   }

   private String init_Str_TaskQueue(autismclient.util.macro.CaptureValueAction draft) {
      if (draft == null) {
         return "";
      }

      boolean listSource = draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.COMMAND_AUTOFILL
         || draft.source == autismclient.util.macro.CaptureValueAction.CaptureSource.TABLIST;
      if (!listSource) {
         Set<String> captured = PatternCaptureMatcher.init(draft.matchMode, draft.pattern);
         if (!captured.isEmpty()) {
            return captured.iterator().next();
         }
      }

      return MacroVariableStore.load(draft.saveAs);
   }

   private String textFieldValueOr(String key, String fallback) {
      ThemedTextInputBox field = this.textFields.get(key);
      return field == null ? fallback : field.getValue();
   }

   private void useSelectedHotbarSlot() {
      if (MC.player != null) {
         this.setWaitSlotField(MC.player.getInventory().getSelectedSlot());
      }
   }

   private void setWaitSlotField(int slot) {
      ThemedTextInputBox field = this.textFields.get("slot");
      if (field != null) {
         field.setValue(String.valueOf(Math.max(0, slot)));
      }
   }

   private void startWaitInventorySlotCapture() {
      this.startCapture("wait_inventory_slot");
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int contentBottom, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "pattern", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_2(ctx, this.textFields.get("pattern"), x, cy, w, mx, my);
      cy = this.renderFieldByKey(ctx, "matchMode", x, cy, w, mx, my, delta);
      String matchMode = this.enumValue("matchMode");
      if ("MATCH".equals(matchMode)) {
         cy = this.renderMatchStrengthSlider(ctx, x, cy, w, mx, my);
      } else {
         this.draggingMatchStrength = false;
         this.resetMatchStrengthSlider();
      }

      cy = this.renderFieldByKey(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "waitGuiName", x, cy, w, mx, my, delta);
      String pattern = this.textFields.containsKey("pattern") ? this.textFields.get("pattern").getValue().trim() : "";
      int fuzzyPercent = this.fuzzyPercent();
      String hint = pattern.isEmpty()
         ? "Blank pattern = any chat line."
         : (
            "REGEX".equals(matchMode)
               ? "Named groups save values."
               : ("CAPTURE".equals(matchMode) ? "Use {name} to capture." : "Fuzzy match: " + fuzzyPercent + "%.")
         );
      cy = this.renderHint(ctx, x, cy, w, hint);
      ThemedTextInputBox searchField = this.internalTextFields.get("_wait_chat_search");
      if (searchField != null) {
         searchField.setX(x);
         searchField.setY(cy);
         searchField.setWidth(w);
         searchField.render(ctx, mx, my, delta);
         cy += 18;
      }

      List<MacroExecutor.LoggedChatMessage> history = this.filteredChatHistory();
      autismclient.gui.f.b.k.init(ctx, this.font, "Recent Messages (" + history.size() + ")", font, UiColorPalette.textBody(), x, cy + 2, false);
      autismclient.gui.f.b.k.init(
         ctx,
         this.font,
         autismclient.gui.f.b.k.init(this.font, this.condenseHint("Click a row to use that message."), Math.max(20, w - 122), font, -1),
         font,
         UiColorPalette.textSecondary(),
         x + 118,
         cy + 2,
         false
      );
      cy += 13;
      int availableListHeight = Math.max(24, contentBottom - cy - 4);
      this.renderChatResults(ctx, x, cy, w, availableListHeight, mx, my, history);
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, ThemedTextInputBox patternField, int x, int y, int w, int mx, int my) {
      ThemedTextInputBox nameField = this.internalTextFields.get("_capture_name");
      if (nameField != null && patternField != null) {
         int buttonW = this.buttonWidth("Capture");
         nameField.setX(x);
         nameField.setY(y + 1);
         nameField.setWidth(Math.max(1, w - buttonW - 3));
         nameField.setHeight(13);
         nameField.render(ctx, mx, my, 0.0F);
         this.renderSecondaryButton(ctx, x + w - buttonW, y + 1, buttonW, 13, "Capture", mx, my, () -> {
            String name = MacroVariableStore.load(nameField.getValue());
            if (!name.isBlank()) {
               patternField.insertText("{" + name + "}");
               this.modeIndices.put("matchMode", 1);
               this.actionTag.putString("matchMode", "CAPTURE");
               this.actionTag.putString("patternJson", "");
            }
         });
         return y + 18;
      } else {
         return y;
      }
   }

   private int renderMatchStrengthSlider(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int percent = this.fuzzyPercent();
      autismclient.gui.f.b.k.init(ctx, this.font, "Match Strength", font, UiColorPalette.textBody(), x, y + 2, false);
      String label = percent + "%";
      autismclient.gui.f.b.k.init(ctx, this.font, label, font, UiColorPalette.textSecondary(), x + w - this.textWidth(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.matchStrengthSliderX = x;
      this.matchStrengthSliderY = y;
      this.matchStrengthSliderWidth = sliderW;
      this.matchStrengthSliderHeight = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.divider());
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, sliderW - 2, trackH), -15397876);
      int[] steps = new int[]{40, 50, 60, 70, 80, 90, 100};
      int knobIndex = Math.max(0, Math.min(6, (percent - 40) / 10));
      int knobCenterX = x + Math.round((sliderW - 1) * (knobIndex / 6.0F));
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, knobCenterX - x - 1, trackH), -13345658);

      for (int i = 0; i < steps.length; i++) {
         int step = steps[i];
         int cx = x + Math.round((sliderW - 1) * (i / 6.0F));
         int tickColor = step <= percent ? -6435073 : -10862528;
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, y + 2, 1, sliderH - 4), tickColor);
      }

      int knobW = 16;
      int knobX = Math.max(x, Math.min(x + sliderW - knobW, knobCenterX - knobW / 2));
      boolean hovered = this.isOverMatchStrengthSlider(mx, my);
      int knobFill = this.draggingMatchStrength ? -14202259 : (hovered ? -14007198 : -14533802);
      int knobBorder = this.draggingMatchStrength ? -5908993 : -7816193;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int fuzzyPercent() {
      ThemedTextInputBox field = this.textFields.get("fuzzyPercent");
      if (field == null) {
         return 100;
      }

      try {
         return WaitForChatAction.snapFuzzyPercent(Integer.parseInt(field.getValue().trim()));
      } catch (NumberFormatException ignored) {
         return 100;
      }
   }

   private void setFuzzyPercent(int percent) {
      ThemedTextInputBox field = this.textFields.get("fuzzyPercent");
      if (field != null) {
         field.setValue(String.valueOf(WaitForChatAction.snapFuzzyPercent(percent)));
      }
   }

   private void resetMatchStrengthSlider() {
      this.matchStrengthSliderX = -1;
      this.matchStrengthSliderY = -1;
      this.matchStrengthSliderWidth = 0;
      this.matchStrengthSliderHeight = 0;
   }

   private boolean isOverMatchStrengthSlider(int mx, int my) {
      return this.matchStrengthSliderWidth > 0 && this.matchStrengthSliderHeight > 0 && mx >= this.matchStrengthSliderX && mx < this.matchStrengthSliderX + this.matchStrengthSliderWidth && my >= this.matchStrengthSliderY && my < this.matchStrengthSliderY + this.matchStrengthSliderHeight;
   }

   private void dragMatchStrengthSlider(int mouseX) {
      if (this.matchStrengthSliderWidth > 1) {
         float normalized = (float)(mouseX - this.matchStrengthSliderX) / (this.matchStrengthSliderWidth - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 6.0F);
         this.setFuzzyPercent(40 + stepIndex * 10);
      }
   }

   private int renderSmoothnessSlider(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int smoothness = this.smoothness();
      autismclient.gui.f.b.k.init(ctx, this.font, "Smoothness", font, UiColorPalette.textBody(), x, y + 2, false);
      String label = smoothness + " / 10";
      autismclient.gui.f.b.k.init(ctx, this.font, label, font, UiColorPalette.textSecondary(), x + w - this.textWidth(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.smoothnessSliderX = x;
      this.smoothnessSliderY = y;
      this.smoothnessSliderWidth = sliderW;
      this.smoothnessSliderHeight = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.divider());
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, sliderW - 2, trackH), -15397876);
      int knobIndex = Math.max(0, Math.min(9, smoothness - 1));
      int knobCenterX = x + Math.round((sliderW - 1) * (knobIndex / 9.0F));
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, knobCenterX - x - 1, trackH), -13345658);

      for (int i = 0; i < 10; i++) {
         int cx = x + Math.round((sliderW - 1) * (i / 9.0F));
         int tickColor = i <= knobIndex ? -6435073 : -10862528;
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, y + 2, 1, sliderH - 4), tickColor);
      }

      int knobW = 16;
      int knobX = Math.max(x, Math.min(x + sliderW - knobW, knobCenterX - knobW / 2));
      boolean hovered = this.isOverSmoothnessSlider(mx, my);
      int knobFill = this.draggingSmoothness ? -14202259 : (hovered ? -14007198 : -14533802);
      int knobBorder = this.draggingSmoothness ? -5908993 : -7816193;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int smoothness() {
      ThemedTextInputBox field = this.textFields.get("smoothness");
      if (field == null) {
         return 9;
      }

      try {
         return RotateAction.clampSmoothness(Integer.parseInt(field.getValue().trim()));
      } catch (NumberFormatException ignored) {
         return 9;
      }
   }

   private void setSmoothness(int smoothness) {
      ThemedTextInputBox field = this.textFields.get("smoothness");
      if (field != null) {
         field.setValue(String.valueOf(RotateAction.clampSmoothness(smoothness)));
      }
   }

   private void resetSmoothnessSlider() {
      this.smoothnessSliderX = -1;
      this.smoothnessSliderY = -1;
      this.smoothnessSliderWidth = 0;
      this.smoothnessSliderHeight = 0;
   }

   private boolean isOverSmoothnessSlider(int mx, int my) {
      return this.smoothnessSliderWidth > 0 && this.smoothnessSliderHeight > 0 && mx >= this.smoothnessSliderX && mx < this.smoothnessSliderX + this.smoothnessSliderWidth && my >= this.smoothnessSliderY && my < this.smoothnessSliderY + this.smoothnessSliderHeight;
   }

   private void dragSmoothnessSlider(int mouseX) {
      if (this.smoothnessSliderWidth > 1) {
         float normalized = (float)(mouseX - this.smoothnessSliderX) / (this.smoothnessSliderWidth - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 9.0F);
         this.setSmoothness(1 + stepIndex);
      }
   }

   private int renderSafeDelaySlider(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int delay = this.safeClickDelay();
      autismclient.gui.f.b.k.init(ctx, this.font, "Safe Delay", font, UiColorPalette.textBody(), x, y + 2, false);
      String label = delay + "t";
      autismclient.gui.f.b.k.init(ctx, this.font, label, font, UiColorPalette.textSecondary(), x + w - this.textWidth(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.safeDelaySliderX = x;
      this.safeDelaySliderY = y;
      this.safeDelaySliderWidth = sliderW;
      this.safeDelaySliderHeight = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.divider());
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, sliderW - 2, trackH), -15397876);
      int knobIndex = Math.max(0, Math.min(10, delay));
      int knobCenterX = x + Math.round((sliderW - 1) * (knobIndex / 10.0F));
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x + 1, trackY, knobCenterX - x - 1, trackH), -9818835);

      for (int i = 0; i <= 10; i++) {
         int cx = x + Math.round((sliderW - 1) * (i / 10.0F));
         int tickColor = i <= knobIndex ? -30070 : -10862528;
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, y + 2, 1, sliderH - 4), tickColor);
      }

      int knobW = 16;
      int knobX = Math.max(x, Math.min(x + sliderW - knobW, knobCenterX - knobW / 2));
      boolean hovered = this.isOverSafeDelaySlider(mx, my);
      int knobFill = this.draggingSafeDelay ? -10738652 : (hovered ? -11787228 : -13296869);
      int knobBorder = this.draggingSafeDelay ? -20304 : -34953;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int safeClickDelay() {
      ThemedTextInputBox field = this.textFields.get("safeClickDelayTicks");
      if (field == null) {
         return 1;
      }

      try {
         return XCarryAction.write(Integer.parseInt(field.getValue().trim()));
      } catch (NumberFormatException ignored) {
         return 1;
      }
   }

   private void setSafeClickDelay(int ticks) {
      ThemedTextInputBox field = this.textFields.get("safeClickDelayTicks");
      if (field != null) {
         field.setValue(String.valueOf(XCarryAction.write(ticks)));
      }
   }

   private void resetSafeDelaySlider() {
      this.safeDelaySliderX = -1;
      this.safeDelaySliderY = -1;
      this.safeDelaySliderWidth = 0;
      this.safeDelaySliderHeight = 0;
   }

   private boolean isOverSafeDelaySlider(int mx, int my) {
      return this.safeDelaySliderWidth > 0 && this.safeDelaySliderHeight > 0 && mx >= this.safeDelaySliderX && mx < this.safeDelaySliderX + this.safeDelaySliderWidth && my >= this.safeDelaySliderY && my < this.safeDelaySliderY + this.safeDelaySliderHeight;
   }

   private void dragSafeDelaySlider(int mouseX) {
      if (this.safeDelaySliderWidth > 1) {
         float normalized = (float)(mouseX - this.safeDelaySliderX) / (this.safeDelaySliderWidth - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 10.0F);
         this.setSafeClickDelay(stepIndex);
      }
   }

   private void usePatternFromChat(MacroExecutor.LoggedChatMessage entry) {
      ThemedTextInputBox field = this.textFields.get("pattern");
      if (field != null && entry != null) {
         Component patternComponent = entry.displayComponent() != null
            ? entry.displayComponent().copy()
            : Component.literal(this.chatEntryText(entry));
         this.setFieldComponent(field, patternComponent);
         this.modeIndices.put("matchMode", 0);
         this.actionTag.putString("matchMode", "MATCH");
      }
   }

   private void setFieldComponent(ThemedTextInputBox field, Component component) {
      if (field != null) {
         Component safeComponent = component != null ? component.copy() : Component.empty();
         String visibleText = safeComponent.getString();
         this.actionTag.putString("pattern", visibleText);
         this.actionTag.putString("patternJson", MacroExecutor.serializeComponentJson(safeComponent));
         this.suppressPatternListener = true;
         field.setValue(visibleText);
         this.suppressPatternListener = false;
      }
   }

   private Component patternDisplayComponent(String fallbackValue) {
      Component exact = MacroExecutor.deserializeComponentJson(this.actionTag != null ? this.actionTag.getStringOr("patternJson", "") : "");
      return exact != null ? exact.copy() : Component.literal(fallbackValue == null ? "" : fallbackValue);
   }

   private Component mergeEditedStyles(Component previousComponent, String editedValue) {
      String safeValue = editedValue == null ? "" : editedValue;
      if (previousComponent == null) {
         return Component.literal(safeValue);
      }

      String previousValue = previousComponent.getString();
      if (previousValue.equals(safeValue)) {
         return previousComponent.copy();
      }

      if (safeValue.isEmpty()) {
         return Component.empty();
      }

      if (previousValue.isEmpty()) {
         return Component.literal(safeValue);
      }

      List<Style> previousStyles = this.charStyles(previousComponent, previousValue.length());
      if (previousStyles.isEmpty()) {
         return Component.literal(safeValue);
      }

      int prefix = this.commonPrefixLength(previousValue, safeValue);
      int suffix = this.commonSuffixLength(previousValue, safeValue, prefix);
      int previousLength = previousValue.length();
      int nextLength = safeValue.length();
      MutableComponent rebuilt = Component.empty();
      StringBuilder segment = new StringBuilder();
      Style segmentStyle = null;

      for (int i = 0; i < nextLength; i++) {
         Style style = this.styleForIndex(previousStyles, previousLength, nextLength, prefix, suffix, i);
         if (segmentStyle == null) {
            segmentStyle = style;
         } else if (!segmentStyle.equals(style)) {
            rebuilt.append(Component.literal(segment.toString()).setStyle(segmentStyle));
            segment.setLength(0);
            segmentStyle = style;
         }

         segment.append(safeValue.charAt(i));
      }

      if (!segment.isEmpty()) {
         rebuilt.append(Component.literal(segment.toString()).setStyle(segmentStyle == null ? Style.EMPTY : segmentStyle));
      }

      return rebuilt;
   }

   private List<Style> charStyles(Component text, int expectedLength) {
      if (text != null && expectedLength > 0) {
         List<Style> rawStyles = new ArrayList<>(expectedLength);
         text.visit((style, part) -> {
            if (part != null && !part.isEmpty()) {
               Style safeStyle = style == null ? Style.EMPTY : style;

               for (int i = 0; i < part.length(); i++) {
                  rawStyles.add(safeStyle);
               }
            }

            return Optional.empty();
         }, Style.EMPTY);
         List<Style> styles = new ArrayList<>(rawStyles);
         if (styles.isEmpty()) {
            for (int i = 0; i < expectedLength; i++) {
               styles.add(Style.EMPTY);
            }
         } else if (styles.size() < expectedLength) {
            Style fill = styles.get(styles.size() - 1);

            while (styles.size() < expectedLength) {
               styles.add(fill);
            }
         } else if (styles.size() > expectedLength) {
            styles = new ArrayList<>(styles.subList(0, expectedLength));
         }

         return styles;
      } else {
         return Collections.emptyList();
      }
   }

   private int commonPrefixLength(String left, String right) {
      int max = Math.min(left.length(), right.length());
      int index = 0;

      while (index < max && left.charAt(index) == right.charAt(index)) {
         index++;
      }

      return index;
   }

   private int commonSuffixLength(String previous, String next, int prefixLength) {
      int previousRemaining = previous.length() - prefixLength;
      int nextRemaining = next.length() - prefixLength;
      int max = Math.min(previousRemaining, nextRemaining);
      int suffix = 0;

      while (suffix < max && previous.charAt(previous.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix)) {
         suffix++;
      }

      return suffix;
   }

   private Style styleForIndex(List<Style> previousStyles, int previousLength, int nextLength, int prefixLength, int suffixLength, int nextIndex) {
      if (previousStyles.isEmpty()) {
         return Style.EMPTY;
      }

      if (nextIndex < prefixLength) {
         return previousStyles.get(Math.min(nextIndex, previousStyles.size() - 1));
      }

      int nextSuffixStart = nextLength - suffixLength;
      int previousSuffixStart = previousLength - suffixLength;
      if (suffixLength > 0 && nextIndex >= nextSuffixStart) {
         int mappedIndex = previousSuffixStart + (nextIndex - nextSuffixStart);
         return previousStyles.get(Math.max(0, Math.min(mappedIndex, previousStyles.size() - 1)));
      }

      int anchorIndex;
      if (prefixLength > 0) {
         anchorIndex = prefixLength - 1;
      } else if (suffixLength > 0) {
         anchorIndex = previousSuffixStart;
      } else {
         anchorIndex = 0;
      }

      return previousStyles.get(Math.max(0, Math.min(anchorIndex, previousStyles.size() - 1)));
   }

   private List<MacroExecutor.LoggedChatMessage> filteredChatHistory() {
      List<MacroExecutor.LoggedChatMessage> history = MacroExecutor.getRecentChatMessages();
      ThemedTextInputBox searchField = this.internalTextFields.get("_wait_chat_search");
      String query = searchField != null ? searchField.getValue().trim() : "";
      this.chatMessageBuffer.clear();
      String normalizedQuery = query.isEmpty() ? "" : this.normalizeChat(query);

      for (MacroExecutor.LoggedChatMessage entry : history) {
         if (normalizedQuery.isEmpty()) {
            this.chatMessageBuffer.add(entry);
         } else {
            String haystack = this.normalizeChat(this.chatEntryText(entry));
            if (haystack.contains(normalizedQuery)) {
               this.chatMessageBuffer.add(entry);
            }
         }
      }

      return this.chatMessageBuffer;
   }

   private String normalizeChat(String text) {
      return MacroExecutor.normalizeChatForMatch(text);
   }

   private String chatEntryText(MacroExecutor.LoggedChatMessage entry) {
      if (entry == null) {
         return "";
      }

      if (entry.displayComponent() != null) {
         String rendered = entry.displayComponent().getString();
         if (rendered != null && !rendered.isBlank()) {
            return rendered;
         }
      }

      String sender = entry.sender() == null ? "" : entry.sender().trim();
      String message = entry.message() == null ? "" : entry.message().trim();
      if (!sender.isEmpty() && !message.isEmpty()) {
         return sender + ": " + message;
      } else if (!message.isEmpty()) {
         return (entry.source() == MacroExecutor.ChatSenderKind.SERVER ? "[Server] " : "[Player] ") + message;
      } else {
         return entry.displayText() == null ? "" : entry.displayText();
      }
   }

   private void renderChatResults(
      GuiGraphicsExtractor ctx, int x, int y, int w, int listH, int mx, int my, List<MacroExecutor.LoggedChatMessage> values
   ) {
      this.renderChatList(ctx, x, y, w, listH, mx, my, values, "wait_chat_recent", 4, this::usePatternFromChat);
   }

   private void renderChatList(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int listH,
      int mx,
      int my,
      List<MacroExecutor.LoggedChatMessage> values,
      String viewportKey,
      int visibleRows,
      Consumer<MacroExecutor.LoggedChatMessage> onSelect
   ) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int desiredListH = values.isEmpty() ? 24 : Math.min(Math.max(1, visibleRows), values.size()) * 34;
      listH = Math.max(24, Math.min(listH, desiredListH));
      int itemW = w - 5 - 1;
      ScrollableListView chatViewport = this.viewportFor(this.entryViewports, viewportKey, x, y, w, listH, 34, 5);
      chatViewport.setContentHeight(values.size() * 34);
      chatViewport.renderScrollbar(ctx, mx, my);
      if (values.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 24, false, false);
         String firstLine = "capture_value_recent".equals(viewportKey) ? "No recent chat yet." : "No recent messages matched your search.";
         String secondLine = "capture_value_recent".equals(viewportKey) ? "Send or receive a message first." : "Send or receive chat first, then pick it here.";
         autismclient.gui.f.b.k.init(ctx, this.font, firstLine, font, UiColorPalette.textSecondary(), x + 3, y + 3, false);
         autismclient.gui.f.b.k.init(ctx, this.font, secondLine, font, UiColorPalette.textSecondary(), x + 3, y + 13, false);
      } else {
         chatViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int first = chatViewport.getFirstVisibleRow();

         for (int i = first; i < values.size() && i <= chatViewport.getLastVisibleRow(); i++) {
            int iy = chatViewport.getRowY(i);
            if (iy != -2147483648) {
               int itemH = 34;
               if (iy + itemH > y + listH) {
                  itemH = Math.max(0, y + listH - iy);
               }

               if (itemH > 0) {
                  MacroExecutor.LoggedChatMessage value = values.get(i);
                  boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + itemH;
                  PanelSurfaceHelper.drawCardRow(ctx, x, iy, itemW, itemH, hovered, false);
                  int border = value.source() == MacroExecutor.ChatSenderKind.SERVER
                     ? (hovered ? -20134 : -3376606)
                     : (hovered ? -8597761 : -11627064);
                  this.fillRect(ctx, x, iy, itemW, itemH, border);
                  List<FormattedCharSequence> wrapped = this.wrapChatEntry(value, Math.max(20, itemW - 6), 3);
                  int textY = iy + 3;
                  if (wrapped.isEmpty()) {
                     autismclient.gui.f.b.k.init(ctx, this.font, "(empty)", font, UiColorPalette.textSecondary(), x + 3, textY, false);
                  } else {
                     for (FormattedCharSequence line : wrapped) {
                        if (textY + 8 > iy + itemH) {
                           break;
                        }

                        ctx.text(this.font, line, x + 3, textY, -1, false);
                        textY += 9;
                     }
                  }

                  MacroExecutor.LoggedChatMessage selected = value;
                  this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, itemW, itemH, () -> {
                     if (onSelect != null) {
                        onSelect.accept(selected);
                     }
                  }));
               }
            }
         }

         chatViewport.endFrame(ctx);
      }
   }

   private int chatResultsHeight() {
      int count = this.filteredChatHistory().size();
      return count <= 0 ? 24 : Math.min(4, count) * 34;
   }

   private List<FormattedCharSequence> wrapChatEntry(MacroExecutor.LoggedChatMessage entry, int maxWidth, int maxLines) {
      Component display = entry != null && entry.displayComponent() != null ? entry.displayComponent() : Component.literal(this.chatEntryText(entry));
      List<FormattedCharSequence> wrapped = this.font.split(display, Math.max(20, maxWidth));
      return wrapped.size() <= maxLines ? wrapped : new ArrayList<>(wrapped.subList(0, Math.max(0, maxLines)));
   }

   private List<String> wrapWords(String text, int maxWidth, int maxLines) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isBlank() && maxLines > 0) {
         String[] words = text.trim().split("\\s+");
         StringBuilder current = new StringBuilder();

         for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String word = words[wordIndex];
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.bodyTextWidth(candidate) <= maxWidth) {
               current.setLength(0);
               current.append(candidate);
            } else if (!current.isEmpty()) {
               if (lines.size() == maxLines - 1) {
                  lines.add(this.ellipsize(current + " " + this.joinFrom(words, wordIndex), maxWidth));
                  return lines;
               }

               lines.add(current.toString());
               current.setLength(0);
               current.append(word);
            } else {
               if (lines.size() == maxLines - 1) {
                  lines.add(this.ellipsize(this.joinFrom(words, wordIndex), maxWidth));
                  return lines;
               }

               lines.add(this.ellipsize(word, maxWidth));
            }
         }

         if (!current.isEmpty() && lines.size() < maxLines) {
            lines.add(current.toString());
         }

         return lines;
      } else {
         return lines;
      }
   }

   private String joinFrom(String[] words, int startIndex) {
      StringBuilder out = new StringBuilder();

      for (int i = startIndex; i < words.length; i++) {
         if (out.length() > 0) {
            out.append(' ');
         }

         out.append(words[i]);
      }

      return out.toString();
   }

   private String ellipsize(String text, int maxWidth) {
      String trimmed = text == null ? "" : text;

      while (!trimmed.isEmpty() && this.bodyTextWidth(trimmed + "...") > maxWidth) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }

      return trimmed.length() < (text == null ? 0 : text.length()) ? trimmed + "..." : trimmed;
   }

   private void renderDelayPacketsEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      List<String> c2sTargets = this.packetNameList(true);
      List<String> s2cTargets = this.packetNameList(false);
      cy = this.renderFieldByKey(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "flushOnDisable", x, cy, w, mx, my, delta);
      String mode = this.enumValue("mode");
      if ("ENABLE".equals(mode)) {
         int[] addBtns = this.layoutButtons(x, w, false, "Add C2S (" + c2sTargets.size() + ")", "Add S2C (" + s2cTargets.size() + ")");
         this.renderActionButton(
            ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S (" + c2sTargets.size() + ")", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, () -> this.openDelayPacketSelector(true)
         );
         this.renderActionButton(
            ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C (" + s2cTargets.size() + ")", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, () -> this.openDelayPacketSelector(false)
         );
         cy += 18;
         int[] loadBtns = this.layoutButtons(x, w, false, "Load Queue", "Clear All");
         this.renderSecondaryButton(ctx, loadBtns[0], cy, loadBtns[1], 13, "Load Queue", mx, my, this::loadDelayPacketsFromQueue);
         this.renderSecondaryButton(ctx, loadBtns[2], cy, loadBtns[3], 13, "Clear All", mx, my, this::clearDelayPacketLists);
         cy += 18;
         int[] presetBtns = this.layoutButtons(x, w, false, "Default Preset", "Module Preset");
         this.renderActionButton(
            ctx, presetBtns[0], cy, presetBtns[1], 13, "Default Preset", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, this::applyDefaultDelayPreset
         );
         this.renderActionButton(
            ctx, presetBtns[2], cy, presetBtns[3], 13, "Module Preset", OverlayActionButton.ButtonTone.PRIMARY, true, mx, my, this::applyModuleDelayPreset
         );
      }
   }

   private List<String> packetNameList(boolean c2s) {
      return this.listFields.computeIfAbsent(c2s ? "c2sPackets" : "s2cPackets", ignored -> new ArrayList<>());
   }

   private void openDelayPacketSelector(boolean c2s) {
      if (c2s) {
         this.packetSelector
            .openC2sMultiSelect((packetClass, selected) -> this.setPacketSelected(true, packetClass, selected), this.selectedPacketClasses(true));
      } else {
         this.packetSelector
            .openS2cMultiSelect((packetClass, selected) -> this.setPacketSelected(false, packetClass, selected), this.selectedPacketClasses(false));
      }
   }

   private List<Class<? extends Packet<?>>> selectedPacketClasses(boolean c2s) {
      List<Class<? extends Packet<?>>> selected = new ArrayList<>();

      for (String target : this.packetNameList(c2s)) {
         Class<? extends Packet<?>> packetClass = this.resolvePacketClass(target, c2s);
         if (packetClass != null && !selected.contains(packetClass)) {
            selected.add(packetClass);
         }
      }

      return selected;
   }

   private Class<? extends Packet<?>> resolvePacketClass(String target, boolean c2s) {
      if (target != null && !target.isBlank()) {
         for (Class<? extends Packet<?>> candidate : c2s ? new ArrayList<>(PacketTypeRegistry.getC2sPackets()) : new ArrayList<>(PacketTypeRegistry.getS2cPackets())) {
            String registryName = PacketTypeRegistry.getPacketName(candidate);
            if (this.looseNameMatch(target, registryName)) {
               return candidate;
            }

            if (this.looseNameMatch(target, PacketNameResolver.nameOfClass(candidate))) {
               return candidate;
            }

            if (this.looseNameMatch(target, candidate.getSimpleName())) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private void setPacketSelected(boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      if (packetClass != null) {
         List<String> targets = this.packetNameList(c2s);
         if (selected) {
            if (!this.containsPacket(targets, packetClass, c2s)) {
               targets.add(PacketNameResolver.nameOfClass(packetClass));
            }
         } else {
            targets.removeIf(existing -> packetClass.equals(this.resolvePacketClass(existing, c2s)));
         }
      }
   }

   private boolean containsPacket(List<String> targets, Class<? extends Packet<?>> packetClass, boolean c2s) {
      if (targets != null && packetClass != null) {
         for (String target : targets) {
            if (packetClass.equals(this.resolvePacketClass(target, c2s))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void loadDelayPacketsFromQueue() {
      List<AutismSessionState.DelayedPacket> queue = AutismSessionState.getInstance().getDelayedPackets();
      if (queue != null && !queue.isEmpty()) {
         int before = this.packetNameList(true).size() + this.packetNameList(false).size();

         for (AutismSessionState.DelayedPacket queuedPacket : queue) {
            if (queuedPacket != null && queuedPacket.packet != null) {
               Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)queuedPacket.packet.getClass();
               if (PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
                  this.setPacketSelected(true, packetClass, true);
               } else if (PacketTypeRegistry.getS2cPackets().contains(packetClass)) {
                  this.setPacketSelected(false, packetClass, true);
               }
            }
         }

         int after = this.packetNameList(true).size() + this.packetNameList(false).size();
         int added = Math.max(0, after - before);
         AutismClientMessaging.sendPrefixed(
            added == 0 ? "Queue did not add any new packet filters" : "Added " + added + " packet filter" + (added == 1 ? "" : "s") + " from queue"
         );
      } else {
         AutismClientMessaging.sendPrefixed("Queue is empty");
      }
   }

   private void clearDelayPacketLists() {
      this.packetNameList(true).clear();
      this.packetNameList(false).clear();
   }

   private void applyDefaultDelayPreset() {
      autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
      tmp.read();
      this.listFields.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
      this.listFields.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
   }

   private void applyModuleDelayPreset() {
      autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
      tmp.write();
      this.listFields.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
      this.listFields.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
   }

   private String packetSelectionSummary(boolean c2s) {
      String direction = c2s ? "C2S" : "S2C";
      List<String> targets = this.packetNameList(c2s);
      if (targets.isEmpty()) {
         return direction + ": none";
      }

      List<String> labels = new ArrayList<>();

      for (int i = 0; i < targets.size() && i < 3; i++) {
         String target = targets.get(i);
         Class<? extends Packet<?>> packetClass = this.resolvePacketClass(target, c2s);
         labels.add(packetClass != null ? PacketNameResolver.nameOfClass(packetClass) : target);
      }

      String summary = direction + ": " + targets.size();
      if (!labels.isEmpty()) {
         summary = summary + " - " + String.join(", ", labels);
      }

      if (targets.size() > labels.size()) {
         summary = summary + ", +" + (targets.size() - labels.size());
      }

      return summary;
   }

   private int renderDelayPacketList(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta, boolean c2s) {
      String key = c2s ? "c2sPackets" : "s2cPackets";
      String searchKey = c2s ? "_delay_packets_c2s_search" : "_delay_packets_s2c_search";
      String title = c2s ? "C2S Packets" : "S2C Packets";
      List<String> selected = this.listFields.getOrDefault(key, Collections.emptyList());
      y = this.renderSimpleEntryRows(ctx, x, y, w, mx, my, key + "_selected", title, selected, selected::remove, value -> value, "(none selected)");
      ThemedTextInputBox search = this.internalTextFields.get(searchKey);
      if (search != null) {
         search.setX(x);
         search.setY(y + 1);
         search.setWidth(w);
         search.render(ctx, mx, my, delta);
      }

      y += 18;
      String filter = search != null ? search.getValue().trim().toLowerCase(Locale.ROOT) : "";
      List<String> options = new ArrayList<>();

      for (String packetName : this.packetNames(c2s)) {
         if (filter.isEmpty() || packetName.toLowerCase(Locale.ROOT).contains(filter)) {
            options.add(packetName);
         }
      }

      this.renderSelectableList(ctx, x, y, w, mx, my, key + "_search", options, 5, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, value -> value);
      return y + 65 + 4;
   }

   private void renderMineEditor(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.renderFieldByKey(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "targetBlocks", x, cy, w, mx, my, delta);
      cy += 4;
      cy = this.renderFieldByKey(ctx, "stopInventoryFull", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "stopSlotsUsed", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "slotsUsedThreshold", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "stopMinedCount", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "minedCountTarget", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "stopAfterTime", x, cy, w, mx, my, delta);
      cy = this.renderFieldByKey(ctx, "timeoutSeconds", x, cy, w, mx, my, delta);
      this.renderHint(ctx, x, cy, w, "Pick target blocks, then choose the stop rule you want.");
   }

   private void useHeldItemTarget() {
      if (MC.player != null) {
         ThemedTextInputBox field = this.textFields.get("itemName");
         if (field != null) {
            if (MC.player.getMainHandItem().isEmpty()) {
               field.setValue("");
               this.itemTargets.remove("itemName");
            } else {
               MacroItemTarget target = withoutSlot(MacroItemTarget.fromItemStack(MC.player.getMainHandItem(), -1));
               this.itemTargets.put("itemName", target);
               field.setValue(target.rawLabel());
            }
         }
      }
   }

   private void selectHeldItemTarget() {
      if (MC.player != null) {
         ThemedTextInputBox field = this.textFields.get("itemName");
         if (field != null) {
            if (MC.player.getMainHandItem().isEmpty()) {
               field.setValue("");
               this.itemTargets.remove("itemName");
            } else {
               MacroItemTarget target = MacroItemTarget.fromItemStack(
                  MC.player.getMainHandItem(), MC.player.getInventory().getSelectedSlot()
               );
               this.itemTargets.put("itemName", target);
               field.setValue(target.rawLabel());
            }
         }
      }
   }

   private void clearSelectSlotItemName() {
      ThemedTextInputBox field = this.textFields.get("itemName");
      if (field != null) {
         field.setValue("");
      }

      this.itemTargets.remove("itemName");
   }

   private int selectedHotbarSlot() {
      ThemedTextInputBox field = this.textFields.get("slot");
      if (field == null) {
         return 0;
      }

      try {
         return Math.max(0, Math.min(8, Integer.parseInt(field.getValue().trim())));
      } catch (NumberFormatException ignored) {
         return 0;
      }
   }

   private void setSelectSlotField(int slot) {
      ThemedTextInputBox field = this.textFields.get("slot");
      if (field != null) {
         field.setValue(String.valueOf(Math.max(0, Math.min(8, slot))));
      }
   }

   private String useItemSummary() {
      int fallbackSlot = this.selectedHotbarSlot() + 1;
      ThemedTextInputBox itemField = this.textFields.get("itemName");
      String itemName = itemField != null ? itemField.getValue().trim() : "";
      return itemName.isEmpty() ? "Uses hotbar slot " + fallbackSlot + "." : "Uses the named item, else slot " + fallbackSlot + ".";
   }

   private String run_Str() {
      ThemedTextInputBox field = this.textFields.get("itemName");
      String item = field != null ? field.getValue().trim() : "";
      ThemedTextInputBox slotField = this.textFields.get("slot");
      String slot = slotField != null ? slotField.getValue().trim() : "";
      if (!slot.isEmpty() && !item.isEmpty()) {
         return "slot #" + slot + " / named item";
      } else if (!slot.isEmpty()) {
         return "slot #" + slot;
      } else {
         return item.isEmpty() ? "the held item" : "the named item";
      }
   }

   private void toggleCheckMainHand() {
      this.toggleStates.put("checkMainHand", !this.toggleStates.getOrDefault("checkMainHand", true));
   }

   private String handLabel() {
      return this.toggleStates.getOrDefault("checkMainHand", true) ? "Hand: Main" : "Hand: Off";
   }

   private void captureCooldownHeldItem() {
      if (MC.player != null) {
         ThemedTextInputBox field = this.textFields.get("itemName");
         if (field != null) {
            if (MC.player.getMainHandItem().isEmpty()) {
               field.setValue("");
               this.itemTargets.remove("itemName");
            } else {
               MacroItemTarget target = MacroItemTarget.fromItemStack(
                  MC.player.getMainHandItem(), MC.player.getInventory().getSelectedSlot()
               );
               this.itemTargets.put("itemName", target);
               field.setValue(target.rawLabel());
            }
         }
      }
   }

   private void clearCooldownItemName() {
      ThemedTextInputBox field = this.textFields.get("itemName");
      if (field != null) {
         field.setValue("");
      }

      this.itemTargets.remove("itemName");
   }

   private void captureCurrentView() {
      if (this.ensureCaptureAvailable() && MC.player != null) {
         ThemedTextInputBox yawField = this.textFields.get("yaw");
         ThemedTextInputBox pitchField = this.textFields.get("pitch");
         if (yawField != null) {
            yawField.setValue(formatNumber(MC.player.getYRot()));
         }

         if (pitchField != null) {
            pitchField.setValue(formatNumber(MC.player.getXRot()));
         }
      }
   }

   private void capturePlayerPosition() {
      if (this.ensureCaptureAvailable() && MC.player != null) {
         ThemedTextInputBox xField = this.textFields.get("pos_0");
         ThemedTextInputBox yField = this.textFields.get("pos_1");
         ThemedTextInputBox zField = this.textFields.get("pos_2");
         if (xField != null) {
            xField.setValue(formatNumber(MC.player.getX()));
         }

         if (yField != null) {
            yField.setValue(formatNumber(MC.player.getY()));
         }

         if (zField != null) {
            zField.setValue(formatNumber(MC.player.getZ()));
         }
      }
   }

   private String set_Str() {
      ThemedTextInputBox itemField = this.textFields.get("itemName");
      String itemName = itemField != null ? itemField.getValue().trim() : "";
      String handLabel = this.toggleStates.getOrDefault("checkMainHand", true) ? "main hand" : "off hand";
      return itemName.isEmpty() ? "Waits for the " + handLabel + " cooldown." : "Waits for the named item cooldown.";
   }

   private void flipSwapEnds() {
      boolean fromUseName = this.toggleStates.getOrDefault("fromUseItemName", false);
      boolean toUseName = this.toggleStates.getOrDefault("toUseItemName", false);
      String fromItem = this.textFields.containsKey("fromItemName") ? this.textFields.get("fromItemName").getValue() : "";
      String toItem = this.textFields.containsKey("toItemName") ? this.textFields.get("toItemName").getValue() : "";
      String fromSlot = this.textFields.containsKey("fromSlot") ? this.textFields.get("fromSlot").getValue() : "";
      String toSlot = this.textFields.containsKey("toSlot") ? this.textFields.get("toSlot").getValue() : "";
      this.toggleStates.put("fromUseItemName", toUseName);
      this.toggleStates.put("toUseItemName", fromUseName);
      ThemedTextInputBox fromItemField = this.textFields.get("fromItemName");
      ThemedTextInputBox toItemField = this.textFields.get("toItemName");
      ThemedTextInputBox fromSlotField = this.textFields.get("fromSlot");
      ThemedTextInputBox toSlotField = this.textFields.get("toSlot");
      if (fromItemField != null) {
         fromItemField.setValue(toItem);
      }

      if (toItemField != null) {
         toItemField.setValue(fromItem);
      }

      if (fromSlotField != null) {
         fromSlotField.setValue(toSlot);
      }

      if (toSlotField != null) {
         toSlotField.setValue(fromSlot);
      }
   }

   private String swapSlotsHint() {
      return "Swaps the two targets. Name mode uses the first visible match.";
   }

   private String clickTypeHint() {
      String clickType = this.enumValue("clickType");
      String var10000;
      switch (clickType) {
         case "LEFT":
            var10000 = "Left click acts on your target.";
            break;
         default:
            var10000 = "Right click uses your target.";
      }

      return var10000;
   }

   private String waitModeHint(String waitMode) {
      String var10000;
      switch (waitMode) {
         case "IS_EMPTY":
            var10000 = "Waits until the slot (or any slot) is empty.";
            break;
         case "COUNT_AT_LEAST":
            var10000 = "Waits until the slot/item hits the target count.";
            break;
         case "COUNT_BELOW":
            var10000 = "Waits until the slot/item drops below the count.";
            break;
         case "ANY_CHANGE":
            var10000 = "Waits for the first change in the specified slot.";
            break;
         default:
            var10000 = "Waits until the slot or item is present. -1 scans all slots.";
      }

      return var10000;
   }

   private int renderFieldByKey(GuiGraphicsExtractor ctx, String key, int x, int y, int w, int mx, int my, float delta) {
      if (this.isWaitForGuiAfterKey(key)) {
         return y;
      }

      FieldDef field = this.fieldByKey(key);
      if (field != null && this.isFieldApplicable(field)) {
         this.init_V_GuiGraphicsExtract_5(ctx, field, x, y, w, mx, my, delta);
         return y + this.fieldHeight(field) + 2;
      }

      if ("safeClickDelayTicks".equals(key)) {
         this.resetSafeDelaySlider();
      }

      return y;
   }

   private FieldDef fieldByKey(String key) {
      if (this.fieldSchema == null) {
         return null;
      }

      for (FieldDef field : this.fieldSchema.fields()) {
         if (field.key().equals(key)) {
            return field;
         }
      }

      return null;
   }

   private <T> int renderSimpleEntryRows(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      String label,
      List<T> values,
      Consumer<T> removeAction,
      Function<T, String> formatter,
      String emptyLabel
   ) {
      return this.renderEntryRows(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, ignored -> null, values::clear, emptyLabel, false
      );
   }

   private <T> int renderEntryRows(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      String label,
      List<T> values,
      Consumer<T> removeAction,
      Function<T, String> formatter,
      Function<T, Component> richFormatter,
      String emptyLabel
   ) {
      return this.renderEntryRows(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, richFormatter, values::clear, emptyLabel, false
      );
   }

   private <T> int renderEntryRows(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      String label,
      List<T> values,
      Consumer<T> removeAction,
      Function<T, String> formatter,
      Function<T, Component> richFormatter,
      Runnable clearAction,
      String emptyLabel
   ) {
      return this.renderEntryRows(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, richFormatter, clearAction, emptyLabel, false
      );
   }

   private <T> int renderEntryRows(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      String label,
      List<T> values,
      Consumer<T> removeAction,
      Function<T, String> formatter,
      Function<T, Component> richFormatter,
      Runnable clearAction,
      String emptyLabel,
      boolean editable
   ) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      int headerBtnW = this.buttonWidth("Clear");
      int editIdx = editable ? this.listEditIndices.getOrDefault(listKey, -1) : -1;
      if (editIdx >= values.size()) {
         editIdx = -1;
         this.listEditIndices.put(listKey, -1);
      }

      autismclient.gui.f.b.k.init(ctx, this.font, label + " (" + values.size() + ")", font, UiColorPalette.textBody(), x, y + 2, false);
      boolean canClear = !values.isEmpty();
      int clearX = x + w - headerBtnW;
      this.renderActionButton(ctx, clearX, y, headerBtnW, 13, "Clear", OverlayActionButton.ButtonTone.DANGER, canClear, mx, my, () -> {
         clearAction.run();
         this.listEditIndices.put(listKey, -1);
         ScrollableListView vp = this.entryViewports.get(listKey);
         if (vp != null) {
            vp.setScrollOffset(0);
         }
      });
      y += 14;
      int listH = 56;
      int itemW = w - 5 - 1;
      ScrollableListView simpleViewport = this.viewportFor(this.entryViewports, listKey, x, y, w, listH, 14, 5);
      simpleViewport.setContentHeight(values.size() * 14);
      simpleViewport.renderScrollbar(ctx, mx, my);
      if (values.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 12, false, false);
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, emptyLabel, x, y, itemW);
         return y + listH + 4;
      }

      simpleViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
      int first = simpleViewport.getFirstVisibleRow();

      for (int i = first; i < values.size() && i <= simpleViewport.getLastVisibleRow(); i++) {
         int iy = simpleViewport.getRowY(i);
         if (iy != -2147483648) {
            T value = values.get(i);
            int removeW = 13;
            int rowW = itemW - removeW - 2;
            boolean selected = editable && i == editIdx;
            boolean hovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
            Component richLabel = richFormatter == null ? null : richFormatter.apply(value);
            if (richLabel != null) {
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx, this.font, richLabel, autismclient.gui.f.NetHelper.init(x, iy, rowW, 13), hovered, selected, ListRowRenderer.RowTone.NORMAL, true
               );
            } else {
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  Component.literal(formatter.apply(value)),
                  autismclient.gui.f.NetHelper.init(x, iy, rowW, 13),
                  hovered,
                  selected,
                  ListRowRenderer.RowTone.NORMAL,
                  false
               );
            }

            if (editable) {
               int fi = i;
               String rawText = value instanceof String rawValue ? rawValue : formatter.apply(value);
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, rowW, 13, () -> {
                  int curIdx = this.listEditIndices.getOrDefault(listKey, -1);
                  if (curIdx == fi) {
                     this.listEditIndices.put(listKey, -1);
                     this.pendingSelections.put(listKey, "");
                  } else {
                     this.listEditIndices.put(listKey, fi);
                     this.pendingSelections.put(listKey, rawText != null ? rawText : "");
                  }
               }));
            }

            int removeX = x + rowW + 2;
            int fi2 = i;
            this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy, removeW, mx, my, () -> {
               removeAction.accept(values.get(fi2));
               if (editable) {
                  int curSel = this.listEditIndices.getOrDefault(listKey, -1);
                  if (curSel == fi2) {
                     this.listEditIndices.put(listKey, -1);
                     this.pendingSelections.put(listKey, "");
                  } else if (curSel > fi2) {
                     this.listEditIndices.put(listKey, curSel - 1);
                  }
               }

               ScrollableListView vp = this.entryViewports.get(listKey);
               if (vp != null) {
                  vp.scrollByRows(-1);
               }
            });
         }
      }

      simpleViewport.endFrame(ctx);
      return y + listH + 4;
   }

   private <T> void renderPickList(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      List<T> values,
      int visibleRows,
      Consumer<T> clickAction,
      Function<T, String> formatter
   ) {
      this.renderSelectableList(ctx, x, y, w, mx, my, listKey, values, visibleRows, clickAction, ignored -> false, formatter);
   }

   private <T> void renderSelectableList(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      String listKey,
      List<T> values,
      int visibleRows,
      Consumer<T> clickAction,
      Predicate<T> selectedPredicate,
      Function<T, String> formatter
   ) {
      int rowH = 13;
      int listH = visibleRows * rowH;
      int itemW = w - 5 - 1;
      ScrollableListView regViewport = this.viewportFor(this.listViewports, listKey, x, y, w, listH, rowH, 5);
      regViewport.setContentHeight(values.size() * rowH);
      regViewport.renderScrollbar(ctx, mx, my);
      if (values.isEmpty()) {
         PanelSurfaceHelper.drawCardRow(ctx, x, y, itemW, 12, false, false);
         ListRowRenderer.drawEmptyPlaceholder(ctx, this.font, "No matches", x, y, itemW);
      } else {
         regViewport.beginFrame(ctx, this.styleTheme.set(), 905969664);
         int first = regViewport.getFirstVisibleRow();

         for (int i = first; i < values.size() && i <= regViewport.getLastVisibleRow(); i++) {
            int iy = regViewport.getRowY(i);
            if (iy != -2147483648) {
               T value = values.get(i);
               String display = formatter.apply(value);
               boolean selected = selectedPredicate != null && selectedPredicate.test(value);
               boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + rowH;
               ItemStack icon = this.iconFor(listKey, value);
               autismclient.gui.macro.editor.capture.EditorListRowHelper.init(
                  ctx,
                  this.font,
                  Component.literal(display),
                  icon,
                  autismclient.gui.f.NetHelper.init(x, iy, itemW, rowH),
                  hovered,
                  selected,
                  selected ? ListRowRenderer.RowTone.READY : ListRowRenderer.RowTone.NORMAL,
                  false
               );
               T clickedValue = value;
               this.valid_List.add(new ActionEditorOverlay.ClickRegion(x, iy, itemW, rowH, () -> clickAction.accept(clickedValue)));
            }
         }

         regViewport.endFrame(ctx);
      }
   }

   private ItemStack iconFor(String listKey, Object value) {
      if (value instanceof String raw && !raw.isBlank()) {
         String id = raw;
         if (raw.startsWith("~")) {
            id = this.entityEntryId(raw);
         }

         if (id != null && !id.isBlank()) {
            Identifier parsed = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
            if (parsed == null) {
               return ItemStack.EMPTY;
            } else {
               String key = listKey == null ? "" : listKey.toLowerCase(Locale.ROOT);
               if (key.contains("item") || key.contains("store")) {
                  Item item = BuiltInRegistries.ITEM.getOptional(parsed).orElse(Items.AIR);
                  return item == Items.AIR ? ItemStack.EMPTY : item.getDefaultInstance();
               } else if (key.contains("block") || key.contains("container")) {
                  Block block = BuiltInRegistries.BLOCK.getOptional(parsed).orElse(Blocks.AIR);
                  return block != Blocks.AIR && block.asItem() != Items.AIR ? block.asItem().getDefaultInstance() : ItemStack.EMPTY;
               } else if (key.contains("entity")) {
                  return this.entityIcon(parsed);
               } else {
                  return key.contains("sound") ? this.soundIcon(parsed) : ItemStack.EMPTY;
               }
            }
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack entityIcon(Identifier entityId) {
      EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);
      if (entityType == null) {
         return ItemStack.EMPTY;
      }

      for (Item item : BuiltInRegistries.ITEM) {
         TypedEntityData<EntityType<?>> data = item.components().get(DataComponents.ENTITY_DATA);
         if (data != null && data.type() == entityType) {
            return item.getDefaultInstance();
         }
      }

      return "player".equals(entityId.getPath()) ? Items.PLAYER_HEAD.getDefaultInstance() : ItemStack.EMPTY;
   }

   private ItemStack soundIcon(Identifier soundId) {
      String path = soundId.getPath().toLowerCase(Locale.ROOT);
      if (path.contains("fishing_bobber")) {
         return Items.FISHING_ROD.getDefaultInstance();
      } else if (path.contains("lava")) {
         return Items.LAVA_BUCKET.getDefaultInstance();
      } else if (path.contains("water") || path.contains("rain") || path.contains("bubble")) {
         return Items.WATER_BUCKET.getDefaultInstance();
      } else if (path.contains("firework")) {
         return Items.FIREWORK_ROCKET.getDefaultInstance();
      } else if (path.contains("experience_orb")) {
         return Items.EXPERIENCE_BOTTLE.getDefaultInstance();
      } else if (path.contains("lightning") || path.contains("thunder")) {
         return Items.LIGHTNING_ROD.weathering().unaffected().getDefaultInstance();
      } else if (path.contains("note_block")) {
         return Items.NOTE_BLOCK.getDefaultInstance();
      } else if (!path.contains("ui_") && !path.startsWith("ui.")) {
         return path.contains("ambient_cave") ? Items.TORCH.getDefaultInstance() : Items.NOTE_BLOCK.getDefaultInstance();
      } else {
         return Items.COMPASS.getDefaultInstance();
      }
   }

   private void captureWaitEntity() {
      if (this.ensureCaptureAvailable() && this.editingAction instanceof WaitForEntityAction) {
         Screen previousScreen = MC.gui.screen();
         this.hideOverlaysForCapture();
         AutismSessionState state = AutismSessionState.getInstance();
         state.setCaptureCancelCallback(() -> {
            if (previousScreen != null) {
               OverlayScreenController.setScreenExplicit(MC, previousScreen);
            }

            state.setEntityCaptureCallback(null);
            this.finishCapture(false, false);
         });
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
         state.setCaptureSpecificEntity(this.captureSpecificEntity);
         state.setEntityCaptureCallback(payload -> MC.execute(() -> {
            List<String> selected = this.listFields.get("entityIds");
            if (selected != null && payload != null && !payload.isBlank()) {
               if (this.captureSpecificEntity) {
                  selected.removeIf(existing -> existing.startsWith(this.entityEntryPrefix(payload)));
               }

               if (!selected.contains(payload)) {
                  selected.add(payload);
               }
            }

            if (previousScreen != null) {
               OverlayScreenController.setScreenExplicit(MC, previousScreen);
            }

            this.finishCapture(false, false);
            OverlayManager.getInstance().bringToFront(this);
         }));
      }
   }

   private static String registryDisplayName(String value) {
      return RegistryDisplayNameCache.sync(value);
   }

   private String entityEntryLabel(String entry) {
      if (entry == null || entry.isBlank()) {
         return "(unknown)";
      } else if (entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         String rawName = parts.length >= 4 ? parts[3] : "";
         String type = parts.length >= 3 ? RegistryDisplayNameCache.read(parts[2]) : "?";
         String uuid = parts.length >= 2 ? parts[1] : "";
         String suffix = uuid.length() >= 4 ? uuid.substring(uuid.length() - 4) : uuid;
         String name = rawName == null ? "" : rawName.trim();
         return !name.isEmpty() && !name.equalsIgnoreCase(type) && !name.equalsIgnoreCase(registryDisplayName(type))
            ? name + " (" + type + " #" + suffix + ")"
            : type + " #" + suffix;
      } else {
         return RegistryDisplayNameCache.read(entry);
      }
   }

   private List<String> nearbyEntityEntries() {
      boolean supportedAction = this.editingAction instanceof WaitForEntityAction
         || this.editingAction instanceof LookAtBlockAction
         || this.editingAction instanceof OpenContainerAction
         || this.editingAction instanceof InteractEntityAction;
      if (supportedAction && MC.player != null && MC.level != null) {
         List<String> result = new ArrayList<>();

         for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity != MC.player && !(MC.player.distanceTo(entity) > 16.0)) {
               String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
               String uuid = entity.getStringUUID();
               String displayName = entity.getDisplayName().getString().replaceAll("§.", "").trim();
               result.add("~" + uuid + "~" + typeId + "~" + displayName);
            }
         }

         result.sort(Comparator.comparingDouble(this::entityEntryDistance));
         return result;
      } else {
         return Collections.emptyList();
      }
   }

   private boolean isEntityContainer(Entity entity) {
      return entity == null ? false : entity instanceof AbstractMinecartContainer || entity instanceof ChestBoat || entity instanceof AbstractHorse;
   }

   private double entityEntryDistance(String entry) {
      if (MC.player != null && entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         if (parts.length >= 2 && MC.level != null) {
            try {
               UUID uuid = UUID.fromString(parts[1]);
               Entity entity = null;

               for (Entity candidate : MC.level.entitiesForRendering()) {
                  if (uuid.equals(candidate.getUUID())) {
                     entity = candidate;
                     break;
                  }
               }

               return entity != null ? MC.player.distanceTo(entity) : 1.7976931348623157E308;
            } catch (Exception ignored) {
               return 1.7976931348623157E308;
            }
         } else {
            return 1.7976931348623157E308;
         }
      } else {
         return 1.7976931348623157E308;
      }
   }

   private String entityEntryId(String entry) {
      if (entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         return parts.length >= 3 ? parts[2] : "";
      } else {
         return "";
      }
   }

   private String entityEntryPrefix(String entry) {
      if (entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         return parts.length >= 2 ? "~" + parts[1] + "~" : "";
      } else {
         return "";
      }
   }

   private String entityEntryDisplay(String entry) {
      String label = this.entityEntryLabel(entry);
      double dist = this.entityEntryDistance(entry);
      return dist == 1.7976931348623157E308 ? label : label + " (" + String.format(Locale.ROOT, "%.1fm", dist) + ")";
   }

   private List<BlockPos> nearbyContainers() {
      if (MC.player != null && MC.level != null) {
         BlockPos center = MC.player.blockPosition();
         List<BlockPos> found = new ArrayList<>();
         int range = 10;

         for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
               for (int dz = -range; dz <= range; dz++) {
                  BlockPos pos = center.offset(dx, dy, dz);
                  if (this.isContainerBlock(pos)) {
                     found.add(pos);
                  }
               }
            }
         }

         found.sort(Comparator.comparingDouble(posx -> Vec3.atCenterOf(center).distanceToSqr(Vec3.atCenterOf(posx))));
         return found;
      } else {
         return Collections.emptyList();
      }
   }

   private boolean isContainerBlock(BlockPos pos) {
      if (MC.level != null && pos != null) {
         BlockState state = MC.level.getBlockState(pos);
         if (state == null || state.isAir()) {
            return false;
         }

         if (state.getMenuProvider(MC.level, pos) != null) {
            return true;
         }

         String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
         String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
         return !path.contains("chest")
               && !path.contains("barrel")
               && !path.contains("shulker")
               && !path.contains("hopper")
               && !path.contains("dispenser")
               && !path.contains("dropper")
               && !path.contains("furnace")
               && !path.contains("smoker")
               && !path.contains("blast_furnace")
               && !path.contains("brewing")
               && !path.contains("anvil")
               && !path.contains("beacon")
               && !path.contains("crafting")
               && !path.contains("loom")
               && !path.contains("smithing")
               && !path.contains("stonecutter")
               && !path.contains("cartography")
               && !path.contains("grindstone")
               && !path.contains("enchant")
               && !path.contains("ender_chest")
               && !path.contains("spawner")
            ? blockId.contains("container") || blockId.contains("gui") || blockId.contains("menu")
            : true;
      } else {
         return false;
      }
   }

   private String containerLabel(BlockPos pos) {
      if (pos == null) {
         return "(unknown)";
      }

      String blockName = MC.level != null ? MC.level.getBlockState(pos).getBlock().getName().getString() : "Container";
      return blockName + " (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
   }

   private void setPosFields(String fieldKey, BlockPos pos) {
      if (fieldKey != null && pos != null) {
         ThemedTextInputBox fx = this.textFields.get(fieldKey + "_0");
         ThemedTextInputBox fy = this.textFields.get(fieldKey + "_1");
         ThemedTextInputBox fz = this.textFields.get(fieldKey + "_2");
         if (fx != null) {
            fx.setValue(String.valueOf(pos.getX()));
         }

         if (fy != null) {
            fy.setValue(String.valueOf(pos.getY()));
         }

         if (fz != null) {
            fz.setValue(String.valueOf(pos.getZ()));
         }
      }
   }

   private boolean posFieldsMatch(String fieldKey, BlockPos pos) {
      if (fieldKey != null && pos != null) {
         ThemedTextInputBox fx = this.textFields.get(fieldKey + "_0");
         ThemedTextInputBox fy = this.textFields.get(fieldKey + "_1");
         ThemedTextInputBox fz = this.textFields.get(fieldKey + "_2");
         if (fx != null && fy != null && fz != null) {
            try {
               int x = (int)Math.round(Double.parseDouble(fx.getValue().trim()));
               int y = (int)Math.round(Double.parseDouble(fy.getValue().trim()));
               int z = (int)Math.round(Double.parseDouble(fz.getValue().trim()));
               return x == pos.getX() && y == pos.getY() && z == pos.getZ();
            } catch (NumberFormatException ignored) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private List<String> waitPacketNames() {
      return this.listFields.computeIfAbsent("packetNames", ignored -> new ArrayList<>());
   }

   private void openPacketMatchSelector(boolean c2s) {
      Consumer<Class<? extends Packet<?>>> add = packetClass -> {
         if (packetClass != null) {
            if (this.packetMatchRules == null) {
               this.packetMatchRules = new ArrayList<>();
            }

            autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = new autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool();
            rule.direction = c2s ? autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.C2S : autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.S2C;
            String name = PacketTypeRegistry.getPacketName(packetClass);
            rule.packetName = name != null && !name.isBlank() ? name : PacketNameResolver.nameOfClass(packetClass);
            this.packetMatchRules.add(rule);
            this.selectedPacketRule = this.packetMatchRules.size() - 1;
            this.loadPacketRuleValue();
         }
      };
      if (c2s) {
         this.packetSelector.load(add);
      } else {
         this.packetSelector.read(add);
      }
   }

   private void openPacketPickerForField(String fieldKey) {
      ThemedTextInputBox field = this.textFields.get(fieldKey);
      Consumer<Class<? extends Packet<?>>> set = packetClass -> {
         if (packetClass != null && field != null) {
            String name = PacketTypeRegistry.getPacketName(packetClass);
            field.setValue(name != null && !name.isBlank() ? name : PacketNameResolver.nameOfClass(packetClass));
         }
      };
      String direction = this.enumValue("direction");
      if ("S2C".equals(direction)) {
         this.packetSelector.read(set);
      } else if ("ANY".equals(direction)) {
         this.packetSelector.openSinglePicker(set);
      } else {
         this.packetSelector.load(set);
      }
   }

   private void applyPacketRuleValue() {
      if (this.packetMatchRules != null && this.selectedPacketRule >= 0 && this.selectedPacketRule < this.packetMatchRules.size()) {
         ThemedTextInputBox valueField = this.textFields.get("_wpm_value");
         if (valueField != null) {
            this.packetMatchRules.get(this.selectedPacketRule).read = valueField.getValue();
         }
      }
   }

   private void loadPacketRuleValue() {
      ThemedTextInputBox valueField = this.textFields.get("_wpm_value");
      if (valueField != null) {
         if (this.packetMatchRules != null && this.selectedPacketRule >= 0 && this.selectedPacketRule < this.packetMatchRules.size()) {
            valueField.setValue(this.packetMatchRules.get(this.selectedPacketRule).read == null ? "" : this.packetMatchRules.get(this.selectedPacketRule).read);
         } else {
            valueField.setValue("");
         }
      }
   }

   private String ruleFilterLabel(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      return rule != null && rule.load != null && !rule.load.isBlank() ? rule.load : "Packet Only";
   }

   private Component ruleLabel(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule == null) {
         return Component.literal("(empty)");
      }

      String label = rule.direction + " " + (rule.packetName != null && !rule.packetName.isBlank() ? PacketNameResolver.friendlyName(rule.packetName) : "Any Packet");
      if (rule.load != null && !rule.load.isBlank()) {
         label = label + " / " + rule.load;
      }

      return Component.literal(label);
   }

   private void init_V_aM_CachePool(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule != null) {
         Class<? extends Packet<?>> packetClass = this.rulePacketClass(rule);
         List<String> fields = new ArrayList<>(autismclient.util.macro.WaitPacketMatchAction.fieldNames(packetClass));
         fields.add(0, "");
         int idx = fields.indexOf(rule.load == null ? "" : rule.load);
         rule.load = fields.get((idx + 1 + fields.size()) % fields.size());
         rule.operator = rule.load.isBlank() ? autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS : rule.operator;
         List<String> options = autismclient.util.macro.WaitPacketMatchAction.valueOptions(packetClass, rule.load);
         if (!options.isEmpty() && (rule.read == null || rule.read.isBlank() || !options.contains(rule.read))) {
            rule.read = options.get(0);
         }

         this.loadPacketRuleValue();
      }
   }

   private Class<? extends Packet<?>> rulePacketClass(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule == null) {
         return null;
      }

      String direction = rule.direction == autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.ANY ? "" : rule.direction.name();
      return this.resolveTargetClass(direction, rule.packetName == null ? "" : rule.packetName);
   }

   private static <E extends Enum<E>> E init_Enum_Enum_EnumArr(E current, E[] values) {
      if (values != null && values.length != 0) {
         int idx = current == null ? -1 : current.ordinal();
         return values[(idx + 1 + values.length) % values.length];
      } else {
         return current;
      }
   }

   private List<String> sanitizePacketTargets(List<String> rawTargets) {
      List<String> sanitized = new ArrayList<>();

      for (String rawTarget : rawTargets == null ? Collections.<String>emptyList() : rawTargets) {
         String normalized = this.normalizeWaitPacketTarget(rawTarget);
         if (!normalized.isEmpty() && !sanitized.contains(normalized)) {
            sanitized.add(normalized);
         }
      }

      return sanitized;
   }

   private String normalizeWaitPacketTarget(String target) {
      String normalized = WaitForPacketAction.read(target);
      if (normalized.isEmpty()) {
         return "";
      } else if (!WaitForPacketAction.init(normalized).isEmpty()) {
         return normalized;
      } else {
         Class<? extends Packet<?>> packetClass = this.resolveTargetClass("", normalized);
         if (packetClass == null) {
            return normalized;
         } else if (PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
            return this.packetTarget("C2S", packetClass);
         } else {
            return PacketTypeRegistry.getS2cPackets().contains(packetClass) ? this.packetTarget("S2C", packetClass) : normalized;
         }
      }
   }

   private List<String> packetTargetsFor(String direction) {
      List<String> filtered = this.filteredOptionCache.computeIfAbsent("wait_packet_targets_" + direction.toUpperCase(Locale.ROOT), ignored -> new ArrayList<>());
      filtered.clear();

      for (String target : this.waitPacketNames()) {
         String normalized = this.normalizeWaitPacketTarget(target);
         if (!normalized.isEmpty() && direction.equalsIgnoreCase(WaitForPacketAction.init(normalized))) {
            filtered.add(normalized);
         }
      }

      return filtered;
   }

   private String waitPacketDisplayName(String target) {
      return WaitForPacketAction.write(target);
   }

   private void clearPacketTargets(String direction) {
      this.waitPacketNames().removeIf(target -> direction.equalsIgnoreCase(WaitForPacketAction.init(this.normalizeWaitPacketTarget(target))));
      ScrollableListView vpC2s = this.entryViewports.get("wait_packet_c2s");
      if (vpC2s != null) {
         vpC2s.setScrollOffset(0);
      }

      ScrollableListView vpS2c = this.entryViewports.get("wait_packet_s2c");
      if (vpS2c != null) {
         vpS2c.setScrollOffset(0);
      }
   }

   private void removePacketTarget(String target) {
      String normalized = this.normalizeWaitPacketTarget(target);
      if (!normalized.isEmpty()) {
         this.waitPacketNames().removeIf(existing -> normalized.equals(this.normalizeWaitPacketTarget(existing)));
      }
   }

   private void openWaitPacketSelector(boolean c2s) {
      if (c2s) {
         this.packetSelector
            .openC2sMultiSelect((packetClass, selected) -> this.setWaitPacketSelected("C2S", packetClass, selected), this.selectedPacketClassesFor("C2S"));
      } else {
         this.packetSelector
            .openS2cMultiSelect((packetClass, selected) -> this.setWaitPacketSelected("S2C", packetClass, selected), this.selectedPacketClassesFor("S2C"));
      }
   }

   private List<Class<? extends Packet<?>>> selectedPacketClassesFor(String direction) {
      List<Class<? extends Packet<?>>> selected = new ArrayList<>();

      for (String target : this.packetTargetsFor(direction)) {
         Class<? extends Packet<?>> packetClass = this.resolveTargetClass(direction, target);
         if (packetClass != null && !selected.contains(packetClass)) {
            selected.add(packetClass);
         }
      }

      return selected;
   }

   private void setWaitPacketSelected(String direction, Class<? extends Packet<?>> packetClass, boolean selected) {
      String target = this.packetTarget(direction, packetClass);
      if (!target.isEmpty()) {
         List<String> targets = this.waitPacketNames();
         if (selected) {
            if (!targets.stream().map(this::normalizeWaitPacketTarget).anyMatch(target::equals)) {
               targets.add(target);
            }
         } else {
            targets.removeIf(existing -> target.equals(this.normalizeWaitPacketTarget(existing)));
         }
      }
   }

   private void loadWaitPacketsFromQueue() {
      List<AutismSessionState.DelayedPacket> queue = AutismSessionState.getInstance().getDelayedPackets();
      if (queue != null && !queue.isEmpty()) {
         List<String> targets = this.waitPacketNames();
         List<String> merged = this.sanitizePacketTargets(targets);
         int before = merged.size();

         for (AutismSessionState.DelayedPacket queuedPacket : queue) {
            String target = this.queuedPacketName(queuedPacket);
            if (!target.isEmpty() && !merged.contains(target)) {
               merged.add(target);
            }
         }

         targets.clear();
         targets.addAll(merged);
         int added = Math.max(0, merged.size() - before);
         AutismClientMessaging.sendPrefixed(
            added == 0 ? "Queue did not add any new packet targets" : "Added " + added + " packet target" + (added == 1 ? "" : "s") + " from queue"
         );
      } else {
         AutismClientMessaging.sendPrefixed("Queue is empty");
      }
   }

   private String queuedPacketName(AutismSessionState.DelayedPacket queuedPacket) {
      if (queuedPacket != null && queuedPacket.packet != null) {
         Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)queuedPacket.packet.getClass();
         if (PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
            return this.packetTarget("C2S", packetClass);
         } else {
            return PacketTypeRegistry.getS2cPackets().contains(packetClass) ? this.packetTarget("S2C", packetClass) : "";
         }
      } else {
         return "";
      }
   }

   private String packetTarget(String direction, Class<? extends Packet<?>> packetClass) {
      if (packetClass == null) {
         return "";
      }

      String name = PacketTypeRegistry.getPacketName(packetClass);
      if (name == null || name.isBlank()) {
         name = PacketNameResolver.nameOfClass(packetClass);
      }

      return WaitForPacketAction.init(direction, name);
   }

   private Class<? extends Packet<?>> resolveTargetClass(String direction, String target) {
      String packetName = WaitForPacketAction.load(target);
      if (packetName.isBlank()) {
         return null;
      }

      Class<? extends Packet<?>> direct = PacketTypeRegistry.resolvePacketClass(packetName);
      if (direct != null) {
         if (direction.isBlank()) {
            return direct;
         }

         if ("C2S".equalsIgnoreCase(direction) && PacketTypeRegistry.getC2sPackets().contains(direct)) {
            return direct;
         }

         if ("S2C".equalsIgnoreCase(direction) && PacketTypeRegistry.getS2cPackets().contains(direct)) {
            return direct;
         }
      }

      List<Class<? extends Packet<?>>> pool = new ArrayList<>();
      if ("C2S".equalsIgnoreCase(direction)) {
         pool.addAll(PacketTypeRegistry.getC2sPackets());
      } else if ("S2C".equalsIgnoreCase(direction)) {
         pool.addAll(PacketTypeRegistry.getS2cPackets());
      } else {
         pool.addAll(PacketTypeRegistry.getC2sPackets());
         pool.addAll(PacketTypeRegistry.getS2cPackets());
      }

      for (Class<? extends Packet<?>> candidate : pool) {
         String registryName = PacketTypeRegistry.getPacketName(candidate);
         if (this.looseNameMatch(packetName, registryName)) {
            return candidate;
         }

         if (this.looseNameMatch(packetName, PacketNameResolver.nameOfClass(candidate))) {
            return candidate;
         }

         if (this.looseNameMatch(packetName, candidate.getSimpleName())) {
            return candidate;
         }
      }

      return null;
   }

   private boolean looseNameMatch(String expected, String candidate) {
      if (expected != null && !expected.isBlank() && candidate != null && !candidate.isBlank()) {
         String normalizedExpected = expected.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
         String normalizedCandidate = candidate.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
         if (normalizedExpected.isEmpty() || normalizedCandidate.isEmpty()) {
            return false;
         }

         if (normalizedExpected.equals(normalizedCandidate)) {
            return true;
         }

         if (normalizedCandidate.endsWith("packet")) {
            String stripped = normalizedCandidate.substring(0, normalizedCandidate.length() - "packet".length());
            if (normalizedExpected.equals(stripped)) {
               return true;
            }
         }

         return normalizedExpected.endsWith(normalizedCandidate) || normalizedCandidate.endsWith(normalizedExpected);
      } else {
         return false;
      }
   }

   private List<String> packetNames(boolean c2s) {
      List<String> names = new ArrayList<>();

      for (Class<? extends Packet<?>> packetClass : c2s ? PacketTypeRegistry.getC2sPackets() : PacketTypeRegistry.getS2cPackets()) {
         String name = PacketTypeRegistry.getPacketName(packetClass);
         if (name != null && !name.isBlank()) {
            names.add(name);
         }
      }

      Collections.sort(names);
      return names;
   }

   private int init_I_GuiGraphicsExtract_4(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      Identifier font = this.styleTheme.init_Identifier_l(l.read);
      this.drawTrimmedText(ctx, field.label(), x, y, w, font);
      y += 13;
      int bw = (w - 4) / 3;

      for (int i = 0; i < 3; i++) {
         ThemedTextInputBox f = this.textFields.get(field.key() + "_" + i);
         if (f != null) {
            int fx = x + i * (bw + 2);
            f.setX(fx);
            f.setY(y + 1);
            f.setWidth(bw);
            f.render(ctx, mx, my, delta);
         }
      }

      return y + 14 + 2;
   }

   private static List<String> soundIds() {
      if (cachedSoundIds == null) {
         cachedSoundIds = new ArrayList<>();

         for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
            if (id != null) {
               cachedSoundIds.add(id.toString());
            }
         }

         Collections.sort(cachedSoundIds);
      }

      return cachedSoundIds;
   }

   private static List<String> entityTypeIds() {
      if (cachedEntityTypeIds == null) {
         cachedEntityTypeIds = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            cachedEntityTypeIds.add(id.toString());
         }

         Collections.sort(cachedEntityTypeIds);
      }

      return cachedEntityTypeIds;
   }

   private static List<String> itemIds() {
      if (cachedItemIds == null) {
         cachedItemIds = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
            cachedItemIds.add(id.toString());
         }

         Collections.sort(cachedItemIds);
      }

      return cachedItemIds;
   }

   private int fieldHeight(FieldDef field) {
      if (field != null && "safeClickDelayTicks".equals(field.key())) {
         return 29;
      }

      if (this.isWaitChatPatternField(field)) {
         return 53;
      }

      int var10000;
      switch (field.type()) {
         case MACRO_SELECT:
            var10000 = 87;
            break;
         case BLOCK_POS:
            var10000 = 29;
            break;
         case STRING_LIST:
            if (this.isRaceStepsField(field)) {
               var10000 = 86;
            } else if (field.captureMode() == CaptureMode.BLOCK_CATALOG) {
               var10000 = 161;
            } else {
               int extraEditorRow = this.isXCarryEntriesKey(field.key()) && this.listEditIndices.getOrDefault(field.key(), -1) >= 0 ? 15 : 0;
               var10000 = 84 + extraEditorRow;
            }
            break;
         default:
            var10000 = 14;
      }

      return var10000;
   }

   private int itemEditorHeight() {
      if (this.itemActionDraft != null) {
         int h = 100;
         h += 16;
         if (this.reset_Z()) {
            h += 16;
         }

         h += 16;
         if (this.toggleStates.getOrDefault("item_waitForGuiBefore", false) || this.toggleStates.getOrDefault("item_waitForGuiAfter", false)) {
            h += 16;
         }

         h += 16;
         h += 16;
         if (this.toggleStates.getOrDefault("item_useClickDelay", false)) {
            h += 16;
         }

         return h;
      } else if (this.payloadActionDraft != null) {
         int h = 4;
         h += 13;
         h += 24;
         h += 18;
         h += 18;
         h += 16;
         h += 13;
         h += (this.payloadTabIndex == 0 ? 143 : 152) + 4;
         h += 14;
         h += 20;
         h += 24;
         if (this.isConfigPhase()) {
            h += 24;
         }

         return h;
      } else if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SEND_PACKET) {
         int h = 80;
         h += 16;
         h += 16;
         h += 16;
         if (this.waitsForGui()) {
            h += 16;
         }

         return h + 116;
      } else {
         if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SOUND) {
            int h = 4;
            h += this.isFieldApplicable(this.fieldByKey("waitForGuiBefore")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("waitForGuiAfter")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("waitGuiName")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("checkDistance")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("maxDistance")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0;
            h += 74;
            h += 18;
            return h + 78;
         }

         if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_ENTITY) {
            int h = 4;
            h += this.isFieldApplicable(this.fieldByKey("checkMode")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("radius")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("centerOnPlayer")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("pos")) ? this.fieldHeight(this.fieldByKey("pos")) + 2 : 0;
            h += this.isFieldApplicable(this.fieldByKey("mustBeLookingAt")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0;
            h += 74;
            h += 18;
            h += 62;
            return h + 52;
         }

         if (this.editingAction != null && this.editingAction.getType() == MacroActionType.LOOK_AT_BLOCK) {
            int h = 4;
            h += this.isFieldApplicable(this.fieldByKey("targetMode")) ? 16 : 0;
            String mode = this.enumValue("targetMode");
            if ("BLOCK".equals(mode)) {
               h += this.isFieldApplicable(this.fieldByKey("searchRadius")) ? 16 : 0;
               h += this.fieldHeight(this.fieldByKey("blockIds")) + 2;
            } else if ("ENTITY".equals(mode)) {
               h += this.isFieldApplicable(this.fieldByKey("searchRadius")) ? 16 : 0;
               h += 74;
               h += 18;
               h += 66;
               h += 56;
            } else {
               h += this.fieldHeight(this.fieldByKey("blockPos")) + 2;
            }

            h += this.isFieldApplicable(this.fieldByKey("smooth")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("smoothness")) ? 16 : 0;
            h += this.isFieldApplicable(this.fieldByKey("waitForCompletion")) ? 16 : 0;
            return h + 14;
         } else {
            if (this.editingAction != null && this.editingAction.getType() == MacroActionType.ROTATE) {
               int h = 4;
               h += this.isFieldApplicable(this.fieldByKey("yaw")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("pitch")) ? 16 : 0;
               h += 18;
               h += this.isFieldApplicable(this.fieldByKey("smooth")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("smoothness")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("waitForCompletion")) ? 16 : 0;
               return h + 14;
            }

            if (this.editingAction != null && this.editingAction.getType() == MacroActionType.GO_TO) {
               int h = 4;
               h += this.fieldHeight(this.fieldByKey("pos")) + 2;
               h += 18;
               h += this.isFieldApplicable(this.fieldByKey("waitForArrival")) ? 16 : 0;
               return h + 14;
            }

            if (this.editingAction != null && this.editingAction.getType() == MacroActionType.USE_ITEM) {
               int h = 4;
               h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("itemName")) ? 16 : 0;
               h += 18;
               h += this.isFieldApplicable(this.fieldByKey("useMode")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("waitForFinish")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("sneak")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("holdTicks")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("useCount")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("waitForGui")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("guiName")) ? 16 : 0;
               return h + 16;
            }

            if (this.editingAction != null && this.editingAction.getType() == MacroActionType.INVENTORY_AUDIT) {
               int h = 4;
               h += this.isFieldApplicable(this.fieldByKey("mode")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("targetItems")) ? 92 : 0;
               h += this.isFieldApplicable(this.fieldByKey("openMode")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("openCommand")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("containerPos")) ? this.fieldHeight(this.fieldByKey("containerPos")) + 2 : 0;
               if ("CONTAINER".equals(this.enumValue("openMode"))) {
                  h += 56;
               }

               h += this.isFieldApplicable(this.fieldByKey("dupeVector")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("iterations")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("maxTransferAttempts")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("transferRetryDelayMs")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("spamCount")) ? 16 : 0;
               h += this.isFieldApplicable(this.fieldByKey("spamDelayMs")) ? 16 : 0;
               return h + 16;
            } else {
               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SWAP_SLOTS) {
                  int h = 20;
                  h += this.isFieldApplicable(this.fieldByKey("fromUseItemName")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("fromItemName")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("fromSlot")) ? 16 : 0;
                  h += 24;
                  h += this.isFieldApplicable(this.fieldByKey("toUseItemName")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("toItemName")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("toSlot")) ? 16 : 0;
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CLICK) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("clickType")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("clickCount")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForGuiBefore")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("guiName")) ? 16 : 0;
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DISCONNECT) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("mode")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("delayMs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("lagMethod")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("kickMethod")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("packetCount")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("useNextAction")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("trigger")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("tolerance")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("bufferMs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("timeoutSec")) ? 16 : 0;
                  return h + 12;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_GUI) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("waitMode")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("guiType")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("matchMode")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("guiTitle")) ? 16 : 0;
                  h += 18;
                  h += this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("timeoutMs")) ? 16 : 0;
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.SELECT_SLOT) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("itemName")) ? 16 : 0;
                  h += 18;
                  h += 30;
                  h += this.isFieldApplicable(this.fieldByKey("strategy")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("outputVariable")) ? 16 : 0;
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_COOLDOWN) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("itemName")) ? 16 : 0;
                  h += 18;
                  h += 18;
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_CHAT) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("pattern")) ? this.fieldHeight(this.fieldByKey("pattern")) + 2 : 0;
                  h += 18;
                  h += this.isFieldApplicable(this.fieldByKey("matchMode")) ? 16 : 0;
                  h += "MATCH".equals(this.enumValue("matchMode")) ? 29 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("timeoutMs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForGuiBefore")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForGuiAfter")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitGuiName")) ? 16 : 0;
                  h += 18;
                  h += 18;
                  h += 13 + this.chatResultsHeight();
                  return h + 16;
               }

               if (this.editingAction != null && this.editingAction.getType() == MacroActionType.CAPTURE_VALUE) {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("source")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("autofillCommand")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("autofillTimeoutMs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("autofillCacheList")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listSelection")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listPickPosition")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listFilter")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listFilterText")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listExcludeText")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("listStripPrefix")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("excludeSelf")) ? 16 : 0;
                  if ("RECENT_CHAT".equals(this.enumValue("source"))) {
                     h += 13 + this.candidateListHeight(MacroExecutor.getRecentChatMessages().size()) + 2;
                  } else if ("SCOREBOARD".equals(this.enumValue("source"))) {
                     int lineCount = autismclient.util.macro.CaptureValueAction.sidebarLines(MC).size();
                     h += 13 + (lineCount <= 0 ? 24 : Math.min(5, lineCount) * 22) + 2;
                  } else if ("TABLIST".equals(this.enumValue("source"))) {
                     int matchCount = this.matchingCandidateCount();
                     h += 13 + (matchCount <= 0 ? 24 : Math.min(5, matchCount) * 14) + 2;
                  }

                  h += this.isFieldApplicable(this.fieldByKey("exampleText")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("selectedText")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("itemFilter")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("slot")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("itemText")) ? 16 : 0;
                  h += 60;
                  h += this.isFieldApplicable(this.fieldByKey("numberMode")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("numberModifier")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("numberModifierAmount")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForTrigger")) ? 16 : 0;
                  return h + 24;
               } else if (this.editingAction == null
                  || this.editingAction.getType() != MacroActionType.OPEN_CONTAINER && this.editingAction.getType() != MacroActionType.INTERACT_ENTITY) {
                  if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
                     int h = 4;
                     h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                     h += 47;
                     h += this.isFieldApplicable(this.fieldByKey("blockPos")) ? this.fieldHeight(this.fieldByKey("blockPos")) + 2 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("manualDirection")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("direction")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("sneak")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("sneakMode")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("interact")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("interactTiming")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("interactCustomMs")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("waitForGuiBefore")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("waitForGuiAfter")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("guiName")) ? 16 : 0;
                     return h + 36;
                  }

                  if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
                     int h = 4;
                     h += 16;
                     h += 16;
                     h += 76;
                     h += 16;
                     h += 16;
                     return h + 12;
                  }

                  if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_INVENTORY_PREDICATE) {
                     int h = 4;
                     h += this.isFieldApplicable(this.fieldByKey("listenDuringPreviousAction")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("condition")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("itemName")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("count")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("slot")) ? 34 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("timeoutMs")) ? 16 : 0;
                     return h + (this.isFieldApplicable(this.fieldByKey("saveAs")) ? 16 : 0);
                  }

                  if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DELAY_PACKETS) {
                     int h = 4;
                     h += this.isFieldApplicable(this.fieldByKey("mode")) ? 16 : 0;
                     h += this.isFieldApplicable(this.fieldByKey("flushOnDisable")) ? 16 : 0;
                     if ("ENABLE".equals(this.enumValue("mode"))) {
                        h += 54;
                     }

                     return h + 8;
                  } else {
                     if (this.editingAction != null && this.editingAction.getType() == MacroActionType.MINE) {
                        int h = 4;
                        h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                        h += this.fieldHeight(this.fieldByKey("targetBlocks")) + 2 + 4;
                        h += this.isFieldApplicable(this.fieldByKey("stopInventoryFull")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("stopSlotsUsed")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("slotsUsedThreshold")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("stopMinedCount")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("minedCountTarget")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("stopAfterTime")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("timeoutSeconds")) ? 16 : 0;
                        return h + 14;
                     }

                     if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PAY) {
                        int h = 4;
                        h += this.isFieldApplicable(this.fieldByKey("commandTemplate")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("amountInput")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("divideEnabled")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("probeHidden")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("delayEnabled")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("delayMs")) ? 16 : 0;
                        h += 74;
                        h += 18;
                        ThemedTextInputBox paySearch = this.internalTextFields.get("players");
                        int payRows = this.playerOptionsLoaded ? Math.max(1, this.groupedPlayerEntries(paySearch != null ? paySearch.getValue() : "").size()) : 1;
                        h += payRows * 13 + 4;
                        return h + 48;
                     }

                     if (this.editingAction != null && this.editingAction.getType() == MacroActionType.STORE_ITEM) {
                        int h = 4;
                        h += this.isFieldApplicable(this.fieldByKey("mode")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("allItems")) ? 16 : 0;
                        if (!this.toggleStates.getOrDefault("allItems", false)) {
                           h += 74;
                           h += 18;
                           h += 82;
                           h += 24;
                        }

                        h += this.isFieldApplicable(this.fieldByKey("persistent")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("delayTicks")) ? 16 : 0;
                        h += this.isFieldApplicable(this.fieldByKey("closeAfter")) ? 16 : 0;
                        return h + (this.isFieldApplicable(this.fieldByKey("closeSendPkt")) ? 16 : 0);
                     } else {
                        if (this.editingAction != null && this.editingAction.getType() == MacroActionType.TOGGLE_MODULE) {
                           int h = 4;
                           h += 74;
                           h += 18;
                           h += 94;
                           return h + 14;
                        }

                        if (this.craftEntries != null) {
                           return 148;
                        }

                        if (this.dropActionDraft != null) {
                           int h = 100;
                           h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                           h += 24;
                           h += 16;
                           h += 16;
                           h += 16;
                           if (this.toggleStates.getOrDefault("drop_waitForGuiBefore", false) || this.toggleStates.getOrDefault("drop_waitForGuiAfter", false)) {
                              h += 16;
                           }

                           return h;
                        } else if (this.lanStepEntries != null) {
                           boolean filterByUser = this.toggleStates.getOrDefault("lan_filterByUser", false);
                           int h = 36;
                           if (!filterByUser || this.lanStepEntries.isEmpty()) {
                              h += 16;
                           }

                           h += 12;
                           if (filterByUser) {
                              if (!this.lanStepEntries.isEmpty()) {
                                 int visibleRows = Math.min(3, Math.max(1, this.lanStepEntries.size()));
                                 h += 13 + visibleRows * 14;
                              } else {
                                 h += 12;
                              }

                              h += 22;
                           }

                           return h;
                        } else {
                           if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET) {
                              return 242;
                           }

                           if (this.editingAction != null && this.editingAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
                              int rows = this.packetMatchRules == null ? 1 : Math.max(1, Math.min(4, Math.max(1, this.packetMatchRules.size())));
                              int h = 82 + rows * 14 + 5;
                              if (this.packetMatchRules != null && !this.packetMatchRules.isEmpty()) {
                                 int idx = Math.max(0, Math.min(this.selectedPacketRule, this.packetMatchRules.size() - 1));
                                 autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.packetMatchRules.get(idx);
                                 h += 32;
                                 if (rule.load != null && !rule.load.isBlank()) {
                                    h += 16;
                                    if (rule.operator != autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS) {
                                       h += 16;
                                    }
                                 }
                              } else {
                                 h += 12;
                              }

                              return h;
                           } else {
                              if (this.fieldSchema == null) {
                                 return 0;
                              }

                              int h = 4;

                              for (FieldDef field : this.fieldSchema.fields()) {
                                 if (field.type() != FieldType.STRING_LIST && !this.isWaitForGuiAfterKey(field.key()) && this.isFieldApplicable(field)) {
                                    h += this.fieldHeight(field) + 2;
                                 }
                              }

                              for (FieldDef field : this.fieldSchema.fields()) {
                                 if (field.type() == FieldType.STRING_LIST && !this.isWaitForGuiAfterKey(field.key()) && this.isFieldApplicable(field)) {
                                    h += this.fieldHeight(field) + 2;
                                 }
                              }

                              if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PACKET) {
                                 h += 52;
                              }

                              if (this.editingAction != null && this.editingAction.getType() == MacroActionType.DELAY_PACKETS) {
                                 h += 18;
                              }

                              return h;
                           }
                        }
                     }
                  }
               } else {
                  int h = 4;
                  h += this.isFieldApplicable(this.fieldByKey("packetOrder")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("targetMode")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("pos")) ? this.fieldHeight(this.fieldByKey("pos")) + 2 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("entityTargets")) ? this.fieldHeight(this.fieldByKey("entityTargets")) + 2 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForGuiBefore")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("waitForGuiAfter")) ? 16 : 0;
                  h += this.isFieldApplicable(this.fieldByKey("guiName")) ? 16 : 0;
                  String targetMode = this.enumValue("targetMode");
                  h += 12;
                  if ("ENTITY".equals(targetMode)) {
                     h += 52;
                  } else if ("BLOCK".equals(targetMode)) {
                     h += 39;
                  }

                  return h;
               }
            }
         }
      }
   }

   private boolean isSelectedPickupAll() {
      return this.itemActionDraft != null
         && this.selectedItemEntry >= 0
         && this.selectedItemEntry < this.itemActionDraft.itemNames.size()
         && this.itemActionDraft.getItemClickType(this.selectedItemEntry) == ContainerClickType.PICKUP_ALL;
   }

   private int next_I() {
      int h = 4;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      h += 16;
      return h + 12;
   }

   private void drawTrimmedText(GuiGraphicsExtractor ctx, String text, int x, int y, int maxW, Identifier font) {
      String trimmed = autismclient.gui.f.b.k.init(this.font, text, maxW - 4, font, -1);
      autismclient.gui.f.b.k.init(
         ctx, this.font, trimmed, font, UiColorPalette.textBright(), x, UiLayoutMath.centerTextY(y, 14, this.init_I_Identifier(font), this.styleTheme.flush()), false
      );
   }

   private int init_I_Identifier(Identifier font) {
      if (autismclient.gui.f.a.DataUtil.read.equals(font)) {
         return this.styleTheme.init_I_l(l.init);
      } else {
         return autismclient.gui.f.a.DataUtil.load.equals(font) ? this.styleTheme.init_I_l(l.load) : this.styleTheme.init_I_l(l.read);
      }
   }

   private int textWidth(Identifier font, String text) {
      return autismclient.gui.f.b.k.init(this.font, text == null ? "" : text, font, UiColorPalette.textBright());
   }

   private int bodyTextWidth(String text) {
      return this.textWidth(this.styleTheme.init_Identifier_l(l.read), text);
   }

   private int buttonWidth(String label) {
      return UiDensityMetrics.buttonWidthForText(this.bodyTextWidth(label));
   }

   private int captureButtonWidth(CaptureMode mode, String idleLabel, String capturingLabel) {
      return Math.max(
         this.buttonWidth(autismclient.gui.macro.editor.capture.CaptureButtonRenderer.init(mode, false, idleLabel, capturingLabel)),
         this.buttonWidth(autismclient.gui.macro.editor.capture.CaptureButtonRenderer.init(mode, true, idleLabel, capturingLabel))
      );
   }

   private int dropdownWidth(List<String> options, int available) {
      int longest = 0;
      if (options != null) {
         for (String option : options) {
            longest = Math.max(longest, this.bodyTextWidth(option));
         }
      }

      return Math.min(Math.max(available, 0), Math.max(56, longest + 26));
   }

   private int[] layoutButtons(int x, int totalW, boolean alignRight, String... labels) {
      int n = labels.length;
      int[] out = new int[n * 2];
      int[] cws = new int[n];
      int sum = 0;

      for (int i = 0; i < n; i++) {
         cws[i] = this.buttonWidth(labels[i]);
         sum += cws[i];
      }

      int gap = 2;
      if (sum + gap * (n - 1) > totalW) {
         int each = (totalW - gap * (n - 1)) / n;

         for (int i = 0; i < n; i++) {
            out[i * 2] = x + i * (each + gap);
            out[i * 2 + 1] = i == n - 1 ? totalW - i * (each + gap) : each;
         }

         return out;
      } else {
         int cx = alignRight ? x + totalW - sum - gap * (n - 1) : x;

         for (int i = 0; i < n; i++) {
            out[i * 2] = cx;
            out[i * 2 + 1] = cws[i];
            cx += cws[i] + gap;
         }

         return out;
      }
   }

   private int labelWidth(int totalWidth, String label, Identifier font) {
      return this.labelWidth(totalWidth, label, font, 56);
   }

   private int labelWidth(int totalWidth, String label, Identifier font, int minControlWidth) {
      int measured = this.textWidth(font, label) + 8;
      int availableMax = Math.max(44, totalWidth - Math.max(28, minControlWidth) - 2);
      return Math.max(44, Math.min(measured, availableMax));
   }

   private int controlX(int x, int labelWidth) {
      return x + labelWidth + 2;
   }

   private int valueFieldWidth(int totalWidth, int labelWidth) {
      return Math.max(28, totalWidth - labelWidth - 2);
   }

   private void fillRect(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), color);
   }

   private void renderScrollbar(
      GuiGraphicsExtractor ctx, int trackX, int trackY, int trackH, int totalCount, int visibleCount, int itemH, int scrollOffset, int mouseX, int mouseY
   ) {
      if (totalCount > visibleCount) {
         int viewPixels = Math.max(1, Math.min(trackH, visibleCount * itemH));
         int contentPixels = Math.max(0, totalCount * itemH);
         ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.compute(contentPixels, viewPixels, trackX, trackY, 5, trackH, scrollOffset);
         boolean sbDragging = this.activeScrollbarDrag != null;
         ScrollbarMetricsHelper.render(ctx, metrics, metrics.containsTrack(mouseX, mouseY), sbDragging);
      }
   }

   private ScrollableListView viewportFor(Map<String, ScrollableListView> viewports, String key, int x, int y, int width, int height, int rowHeight, int scrollbarWidth) {
      ScrollableListView vp = viewports.get(key);
      if (vp == null || vp.getX() != x || vp.getY() != y || vp.getWidth() != width || vp.getHeight() != height) {
         vp = new ScrollableListView(x, y, width, height, rowHeight, scrollbarWidth);
         viewports.put(key, vp);
      }

      return vp;
   }

   private ThemedTextInputBox newTextField(int w) {
      return new ThemedTextInputBox(MC, this.font, 0, 0, w, 13, false);
   }

   private static String formatNumber(double v) {
      long lv = (long)v;
      return v == lv ? String.valueOf(lv) : String.valueOf(v);
   }

   private static boolean matchesFilter(String filter, String... candidates) {
      String normalized = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
      if (normalized.isEmpty()) {
         return true;
      }

      for (String candidate : candidates) {
         if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(normalized)) {
            return true;
         }
      }

      return false;
   }

   private void init_V_FieldDef_List(FieldDef field, List<String> lst) {
      if (field.captureMode() == CaptureMode.PACKET_NAME) {
         boolean both = this.editingAction != null && this.editingAction.getType() == MacroActionType.PACKET_GATE;
         if (!both) {
            Consumer<Class<? extends Packet<?>>> add = packetClass -> {
               if (packetClass != null && lst != null) {
                  String namex = PacketTypeRegistry.getPacketName(packetClass);
                  if (namex == null || namex.isBlank()) {
                     namex = PacketNameResolver.nameOfClass(packetClass);
                  }

                  if (!lst.contains(namex)) {
                     lst.add(namex);
                  }
               }
            };
            String direction = this.enumValue("direction");
            if ("ANY".equals(direction)) {
               this.packetSelector.openSinglePicker(add);
            } else if ("S2C".equals(direction)) {
               this.packetSelector.read(add);
            } else {
               this.packetSelector.load(add);
            }
         } else {
            BiConsumer<Class<? extends Packet<?>>, Boolean> toggle = (packetClass, selected) -> {
               if (packetClass != null && lst != null) {
                  String namex = PacketTypeRegistry.getPacketName(packetClass);
                  if (namex == null || namex.isBlank()) {
                     namex = PacketNameResolver.nameOfClass(packetClass);
                  }

                  if (selected) {
                     if (!lst.contains(namex)) {
                        lst.add(namex);
                     }
                  } else {
                     lst.remove(namex);
                  }
               }
            };
            List<Class<? extends Packet<?>>> selected = new ArrayList<>();
            if (lst != null) {
               for (String name : lst) {
                  Class<? extends Packet<?>> c = PacketTypeRegistry.resolvePacketClass(name);
                  if (c != null && !selected.contains(c)) {
                     selected.add(c);
                  }
               }
            }

            this.packetSelector.openAnyMultiSelect(toggle, selected);
         }
      } else if (this.ensureCaptureAvailable()) {
         this.screenBeforeCapture = MC.gui.screen();
         this.hideOverlaysForCapture();
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
         AutismSessionState state = AutismSessionState.getInstance();
         state.setCaptureCancelCallback(() -> this.finishCapture(true, false));
         if (field.captureMode() == CaptureMode.BLOCK_ID || field.captureMode() == CaptureMode.BLOCK_CATALOG) {
            state.setBlockCaptureCallback(pos -> {
               if (MC.level != null) {
                  String id = BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(pos).getBlock()).toString();
                  if (!lst.contains(id)) {
                     lst.add(id);
                  }
               }

               this.finishCapture(true, false);
            });
         } else if (field.captureMode() == CaptureMode.ENTITY_ID) {
            boolean specificCapture = this.isEntityTargetsKey(field.key());
            boolean replaceOnCapture = this.clear_Z_Str(field.key());
            state.setCaptureSpecificEntity(specificCapture);
            state.setEntityCaptureCallback(payload -> {
               String value = payload == null ? "" : payload.strip();
               if (!value.isBlank()) {
                  if (replaceOnCapture) {
                     lst.clear();
                  }

                  if (!lst.contains(value)) {
                     lst.add(value);
                  }
               }

               this.finishCapture(true, false);
            });
         }
      }
   }

   private void startFieldCapture(FieldDef field) {
      if (this.ensureCaptureAvailable()) {
         if (this.editingAction != null
            && (this.editingAction.getType() == MacroActionType.INSTA_BREAK || this.editingAction.getType() == MacroActionType.BREAK)
            && "blockPos".equals(field.key())) {
            this.startBlockPosCapture();
         } else {
            this.screenBeforeCapture = MC.gui.screen();
            this.hideOverlaysForCapture();
            String key = field.key();
            boolean dbl = field.xyzDouble();
            MC.execute(() -> {
               if (MC.gui.screen() != null) {
                  OverlayScreenController.setScreenExplicit(MC, null);
               }
            });
            AutismSessionState state = AutismSessionState.getInstance();
            state.setPlacePreviewActive(this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE);
            state.setCaptureCancelCallback(() -> {
               state.setPlacePreviewActive(false);
               this.finishCapture(true, false);
            });
            if (this.editingAction != null && this.editingAction.getType() == MacroActionType.PLACE) {
               state.setBlockFaceCaptureCallback((pos, face) -> {
                  Direction useFace = face == null ? Direction.UP : face;
                  BlockPos targetPos = pos.relative(useFace);
                  ThemedTextInputBox fx = this.textFields.get(key + "_0");
                  ThemedTextInputBox fy = this.textFields.get(key + "_1");
                  ThemedTextInputBox fz = this.textFields.get(key + "_2");
                  if (fx != null) {
                     fx.setValue(dbl ? formatNumber(targetPos.getX()) : String.valueOf(targetPos.getX()));
                  }

                  if (fy != null) {
                     fy.setValue(dbl ? formatNumber(targetPos.getY()) : String.valueOf(targetPos.getY()));
                  }

                  if (fz != null) {
                     fz.setValue(dbl ? formatNumber(targetPos.getZ()) : String.valueOf(targetPos.getZ()));
                  }

                  state.setPlacePreviewActive(false);
                  this.finishCapture(true, false);
               });
            } else {
               state.setBlockCaptureCallback(pos -> {
                  ThemedTextInputBox fx = this.textFields.get(key + "_0");
                  ThemedTextInputBox fy = this.textFields.get(key + "_1");
                  ThemedTextInputBox fz = this.textFields.get(key + "_2");
                  if (fx != null) {
                     fx.setValue(dbl ? formatNumber(pos.getX()) : String.valueOf(pos.getX()));
                  }

                  if (fy != null) {
                     fy.setValue(dbl ? formatNumber(pos.getY()) : String.valueOf(pos.getY()));
                  }

                  if (fz != null) {
                     fz.setValue(dbl ? formatNumber(pos.getZ()) : String.valueOf(pos.getZ()));
                  }

                  this.finishCapture(true, false);
               });
            }
         }
      }
   }

   private void startBlockPosCapture() {
      if (this.ensureCaptureAvailable()) {
         this.screenBeforeCapture = MC.gui.screen();
         this.hideOverlaysForCapture();
         MC.execute(() -> {
            if (MC.gui.screen() != null) {
               OverlayScreenController.setScreenExplicit(MC, null);
            }
         });
         AutismSessionState state = AutismSessionState.getInstance();
         state.setCaptureCancelCallback(() -> this.finishCapture(true, false));
         state.setBlockFaceCaptureCallback((pos, direction) -> {
            this.setPosFields("blockPos", pos);
            this.modeIndices.put("direction", direction == null ? 1 : direction.ordinal());
            AutismClientMessaging.sendPrefixed("InstaBreak target captured");
            this.finishCapture(true, false);
         });
         AutismClientMessaging.sendPrefixed("InstaBreak: right-click the target block to capture it");
      }
   }

   private static List<String> blockIds() {
      if (cachedBlockIds == null) {
         cachedBlockIds = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            String path = id.getPath();
            if (!path.endsWith("_wall_head")
               && !path.endsWith("_wall_sign")
               && !path.endsWith("_wall_banner")
               && !path.endsWith("_wall_torch")
               && !path.endsWith("_wall_skull")) {
               cachedBlockIds.add(id.toString());
            }
         }

         Collections.sort(cachedBlockIds);
      }

      return cachedBlockIds;
   }

   private static String actionTypeTitle(MacroActionType type) {
      if (type == null) {
         return "Action";
      }

      if (type == MacroActionType.MULTI) {
         return "Multi (unsupported)";
      }

      String[] parts = type.name().split("_");
      StringBuilder sb = new StringBuilder();

      for (String part : parts) {
         if (sb.length() > 0) {
            sb.append(' ');
         }

         sb.append(Character.toUpperCase(part.charAt(0)));
         if (part.length() > 1) {
            sb.append(part.substring(1).toLowerCase());
         }
      }

      return sb.toString();
   }

   private String captureTargetLabel() {
      String activeCaptureKey = this.captureSession.key();
      if (!"_item_entries".equals(activeCaptureKey) && !"_drop_entries".equals(activeCaptureKey) && !"_wsc_entries".equals(activeCaptureKey)) {
         FieldDef field = this.findField(activeCaptureKey);
         return field != null ? field.label() : "Slot";
      } else {
         return "Slot + Item";
      }
   }

   private String actionTitleOrDefault() {
      return this.editingAction != null ? this.windowTitle() : "Action";
   }

   private boolean isLegacyMulti() {
      return this.editingAction != null && this.editingAction.getType() == MacroActionType.MULTI;
   }

   private String windowTitle() {
      if (this.editingAction == null) {
         return "Edit Action";
      }

      String title = this.editingAction.getType() != null ? actionTypeTitle(this.editingAction.getType()) : this.editingAction.getDisplayName();
      return this.editCount > 1 ? title + " x" + this.editCount : title;
   }

   private FieldDef findField(String key) {
      if (this.fieldSchema != null && key != null) {
         for (FieldDef field : this.fieldSchema.fields()) {
            if (key.equals(field.key())) {
               return field;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private record PayloadSnapshot(long revision, PayloadFieldCodec.PayloadTemplate template, PayloadFieldCodec.EncodeResult built) {
   }

   private record PlayerRow(boolean header, String prefix, String load, List<String> members) {
      static ActionEditorOverlay.PlayerRow groupRow(String prefix, List<String> members) {
         return new ActionEditorOverlay.PlayerRow(true, prefix, null, members);
      }

      static ActionEditorOverlay.PlayerRow singleRow(String prefix, String name) {
         return new ActionEditorOverlay.PlayerRow(false, prefix, name, null);
      }
   }

   @FunctionalInterface
   private interface ClickHandler {
      boolean fire(double var1, double var3, int var5);
   }

   private record EntryResult(boolean accepted, String message, int toastColor) {
   }

   private static final class ClickRegion {
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final ActionEditorOverlay.ClickHandler clickHandler;

      private ClickRegion(int x, int y, int w, int h, Runnable action) {
         this(x, y, w, h, (mx, my, mouseButton) -> {
            if (mouseButton != 0) {
               return false;
            }

            action.run();
            return true;
         });
      }

      private ClickRegion(OverlayActionButton button, Runnable action) {
         this(button.getX(), button.getY(), button.getWidth(), button.getHeight(), (mx, my, mouseButton) -> OverlayActionButton.handleClick(button, mx, my, mouseButton));
      }

      private ClickRegion(int x, int y, int w, int h, ActionEditorOverlay.ClickHandler action) {
         this.x = x;
         this.y = y;
         this.width = w;
         this.height = h;
         this.clickHandler = action;
      }

      boolean containsPoint(int mx, int my) {
         return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
      }

      boolean fireClick(double mx, double my, int mouseButton) {
         return this.clickHandler != null && this.clickHandler.fire(mx, my, mouseButton);
      }
   }

   private record PayloadEditorState(String channel, byte[] init_BArr, boolean commandRecognized, int commandValue, ActionEditorOverlay.PayloadTemplate template, String load) {
      private PayloadEditorState {
         init_BArr = init_BArr == null ? new byte[0] : (byte[])init_BArr.clone();
         template = template == null ? ActionEditorOverlay.PayloadTemplate.BINARY_REPLAY : template;
         load = load == null ? "" : load;
      }

      public byte[] init_BArr() {
         return (byte[])this.init_BArr.clone();
      }
   }

   private enum PayloadView {
      SCRIPT,
      HEX,
      UTF8,
      LOGICAL,
      PREVIEW_ONLY;
   }

   private record PendingPayloadEdit(ActionEditorOverlay.PayloadView view, String text) {
   }

   private record ScrollbarRegion(int x, int load, int read, int write, IntConsumer onDrag) {
      boolean containsPoint(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum PayloadTemplate {
      UTF8_TEXT,
      BRAND_STRING,
      COMMAND_INT,
      BINARY_REPLAY;
   }
}
