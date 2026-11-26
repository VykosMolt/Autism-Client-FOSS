package autismclient.gui.macro.editor;

import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.compat263.Compat263;
import autismclient.gui.f.HashUtil;
import autismclient.gui.f.RenderState;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.ByteReader;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.FileCache;
import autismclient.gui.f.b.PoolEntry;
import autismclient.gui.f.b.TableIndex;
import autismclient.gui.f.b.f;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import autismclient.gui.f.c.EventBus;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismPacketSelectorOverlay;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.BackgroundTaskExecutor;
import autismclient.util.D;
import autismclient.util.J;
import autismclient.util.N;
import autismclient.util.AutismMacro;
import autismclient.util.PayloadEditorModel;
import autismclient.util.PayloadJsonCodec;
import autismclient.util.PayloadFieldCodec;
import autismclient.util.bN;
import autismclient.util.bO;
import autismclient.util.bi;
import autismclient.util.DraggableOverlayWindow;
import autismclient.util.OverlayManager;
import autismclient.util.ContainerClickSnapshot;
import autismclient.util.PacketNameResolver;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.cA;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import autismclient.util.cY;
import autismclient.util.cc;
import autismclient.util.ck;
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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
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
   private static final Pattern init_Pattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_-]{0,63}");
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
   private static final Gson init_Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final int mark = -10035062;
   private static final int scan = -38037;
   private static final int parse = 5;
   private static List<String> init_List;
   private static List<String> load_List;
   private static List<String> read_List;
   private static List<String> write_List;
   private final Font init_Font;
   private final CharEncoder init_CharEncoder = new CharEncoder();
   private final AutismPacketSelectorOverlay init_AutismPacketSelect;
   private final RaceStepSelectorOverlay init_RaceStepSelectorOv;
   private int bind = 320;
   private int f_a_I = 60;
   private int f_b_I = 280;
   private int f_c_I = 64;
   private boolean write_Z = false;
   private boolean update_Z = false;
   private double load_D;
   private double read_D;
   private boolean sync_Z = false;
   private String init_Str_2 = "";
   // Set by input and edit entry points; the missing-variable warning is recomputed on the next frame only then.
   private boolean variableWarningStale = true;
   private boolean flush_Z = true;
   private boolean close_Z = false;
   private Screen init_Screen;
   private Screen load_Screen;
   private boolean open_Z = false;
   private boolean poll_Z = false;
   private boolean push_Z = false;
   private String load_Str = "";
   private String read_Str = "";
   private boolean pop_Z;
   private MacroAction init_MacroAction;
   private int f_d_I = 1;
   private int auditEditSession = 0;
   private CompoundTag init_CompoundTag;
   private ActionFieldSchema init_ActionFieldSchema;
   private Runnable init_Runnable;
   private final Map<String, ThemedTextInputBox> init_Map = new LinkedHashMap<>();
   private final Map<String, Boolean> load_Map = new LinkedHashMap<>();
   private final Map<String, Integer> read_Map = new LinkedHashMap<>();
   private final Map<String, List<String>> write_Map = new LinkedHashMap<>();
   private final Map<String, MacroItemTarget> update_Map = new HashMap<>();
   private final Map<String, List<MacroItemTarget>> sync_Map = new HashMap<>();
   private final Map<String, ThemedTextInputBox> flush_Map = new LinkedHashMap<>();
   private final Map<String, EventBus> close_Map = new HashMap<>();
   private final Map<String, Integer> open_Map = new HashMap<>();
   private final Map<String, String> poll_Map = new HashMap<>();
   private final Map<String, List<Integer>> push_Map = new HashMap<>();
   private final Map<String, List<String>> pop_Map = new HashMap<>();
   private final Map<String, EventBus> tick_Map = new HashMap<>();
   private final List<MacroExecutor.Inner_PacketUtil> update_List = new ArrayList<>();
   private int f_e_I = 0;
   private ItemAction init_MapStore;
   private final autismclient.gui.macro.editor.a.StreamUtil init_StreamUtil = new autismclient.gui.macro.editor.a.StreamUtil();
   private String write_Str;
   private final List<autismclient.gui.f.b.SyncManager> sync_List = new ArrayList<>();
   private final Map<String, autismclient.gui.f.b.SyncManager> run_Map = new HashMap<>();
   private final List<String> flush_List = new ArrayList<>();
   private AutismMacro init_aM;
   private int f_f_I = -1;
   private static final List<String> close_List = List.of(
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
   private static final List<String> open_List = List.of(
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
   private List<AutismOverlay> poll_List;
   private final f init_f = new f(700L, 140.0F, 180.0F, 4, 4, 18);
   private List<autismclient.util.macro.CraftAction.Inner_DataUtil> push_List;
   private List<J.Inner_TaskQueue> pop_List;
   private List<J.Inner_TaskQueue> tick_List;
   private J.Inner_TaskQueue init_J_TaskQueue;
   private int f_g_I;
   private boolean tick_Z;
   private String update_Str;
   private int[] init_IArr;
   private DropAction init_PacketUtil;
   private PayloadAction init_H;
   private PayloadEditorModel init_bE;
   private boolean run_Z = false;
   private ActionEditorOverlay.TaskQueue init_ActionEditorOverla_3 = ActionEditorOverlay.TaskQueue.BINARY_REPLAY;
   private int f_h_I = 0;
   private int f_i_I = 0;
   private boolean get_Z = false;
   private boolean set_Z = false;
   private boolean put_Z = false;
   private boolean add_Z = false;
   private boolean clear_Z = false;
   private boolean reset_Z = false;
   private boolean start_Z = false;
   private ActionEditorOverlay.StateCache init_ActionEditorOverla_2;
   private long read_J;
   private long write_J = -9223372036854775808L;
   private ActionEditorOverlay.ConfigStore init_ActionEditorOverla;
   private int f_j_I = -1;
   private int f_k_I = -1;
   private int f_l_I = -1;
   private List<WaitForSlotChangeAction.Inner_DataUtil> run_List;
   private WaitForSlotChangeAction.Inner_StreamUtil init_aD_StreamUtil = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
   private int f_m_I = 1;
   private boolean stop_Z = false;
   private boolean next_Z = false;
   private boolean prev_Z = false;
   private boolean valid_Z = false;
   private boolean size_Z = false;
   private int f_n_I = -1;
   private int f_o_I = -1;
   private int f_p_I = 0;
   private int f_q_I = 0;
   private boolean data_Z = false;
   private int f_r_I = -1;
   private int f_s_I = -1;
   private int f_t_I = 0;
   private int f_u_I = 0;
   private boolean value_Z = false;
   private int f_v_I = -1;
   private int f_w_I = -1;
   private int f_x_I = 0;
   private int f_y_I = 0;
   private boolean cache_Z = false;
   private static final List<PayloadFieldCodec.Inner_BufferHelper> get_List = List.of(
      PayloadFieldCodec.Inner_BufferHelper.BYTE,
      PayloadFieldCodec.Inner_BufferHelper.UNSIGNED_BYTE,
      PayloadFieldCodec.Inner_BufferHelper.BOOLEAN,
      PayloadFieldCodec.Inner_BufferHelper.SHORT,
      PayloadFieldCodec.Inner_BufferHelper.UNSIGNED_SHORT,
      PayloadFieldCodec.Inner_BufferHelper.CHAR,
      PayloadFieldCodec.Inner_BufferHelper.INT,
      PayloadFieldCodec.Inner_BufferHelper.LONG,
      PayloadFieldCodec.Inner_BufferHelper.FLOAT,
      PayloadFieldCodec.Inner_BufferHelper.DOUBLE,
      PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF,
      PayloadFieldCodec.Inner_BufferHelper.RAW_BYTES,
      PayloadFieldCodec.Inner_BufferHelper.BYTE_ARRAY,
      PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES,
      PayloadFieldCodec.Inner_BufferHelper.VAR_INT,
      PayloadFieldCodec.Inner_BufferHelper.VAR_LONG,
      PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING,
      PayloadFieldCodec.Inner_BufferHelper.IDENTIFIER,
      PayloadFieldCodec.Inner_BufferHelper.UUID_FIELD,
      PayloadFieldCodec.Inner_BufferHelper.BLOCK_POS,
      PayloadFieldCodec.Inner_BufferHelper.ENUM_VAR_INT,
      PayloadFieldCodec.Inner_BufferHelper.OPTIONAL_VALUE,
      PayloadFieldCodec.Inner_BufferHelper.NBT,
      PayloadFieldCodec.Inner_BufferHelper.ITEM_STACK,
      PayloadFieldCodec.Inner_BufferHelper.TEXT_COMPONENT
   );
   private static final List<String> set_List = init_List_2();
   private static final List<String> put_List = List.of("Raw UTF-8", "Hex", "DataOutput", "ByteBuf", "JSON", "Mixed");
   private static final List<PayloadFieldCodec.Inner_StreamUtil> add_List = List.of(
      PayloadFieldCodec.Inner_StreamUtil.RAW_UTF8,
      PayloadFieldCodec.Inner_StreamUtil.MANUAL_HEX,
      PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT,
      PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF,
      PayloadFieldCodec.Inner_StreamUtil.JSON_TEXT,
      PayloadFieldCodec.Inner_StreamUtil.ADVANCED_MIXED
   );
   private List<WaitForLanStepAction.Inner_DataUtil> clear_List;
   private List<autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool> reset_List;
   private int f_z_I = -1;
   private final List<String> start_List = new ArrayList<>();
   private final Map<String, String> get_Map = new HashMap<>();
   private final Map<String, Component> set_Map = new HashMap<>();
   private boolean state_Z = false;
   private final List<String> stop_List = new ArrayList<>();
   private List<ak.Inner_DataUtil> prev_List;
   private Runnable load_Runnable;
   private final List<ActionEditorOverlay.Inner_StreamUtil> valid_List = new ArrayList<>();
   private autismclient.gui.f.NetHelper init_NetHelper;
   private float init_F_2 = 0.0F;
   private final List<ActionEditorOverlay.SyncManager> size_List = new ArrayList<>();
   private IntConsumer init_IntConsumer = null;
   private static ActionEditorOverlay init_ActionEditorOverla_4;
   private static final List<String> data_List = List.of(
      "{user}", "{pos}", "{rot}", "{x}", "{y}", "{z}", "{uuid}", "{dimension}", "{facing}", "{server}", "{selected_slot}", "{timestamp}"
   );
   private List<String> value_List;
   private long update_J;

   private static List<String> init_List_2() {
      List<String> labels = new ArrayList<>();

      for (PayloadFieldCodec.Inner_BufferHelper type : get_List) {
         labels.add(type.init());
      }

      return List.copyOf(labels);
   }

   public static ActionEditorOverlay init_ActionEditorOverla_2() {
      if (init_ActionEditorOverla_4 == null) {
         init_ActionEditorOverla_4 = new ActionEditorOverlay(init_Minecraft.font);
         OverlayManager.init_bk().init_V(init_ActionEditorOverla_4);
      }

      return init_ActionEditorOverla_4;
   }

   private static boolean init_Z_StreamGate(MacroActionType type) {
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

   private int init_I_MacroAction(MacroAction action) {
      return init_Z_StreamGate(action == null ? null : action.getType()) ? 184 : 280;
   }

   private int push_I_2() {
      return init_Z_StreamGate(this.init_MacroAction == null ? null : this.init_MacroAction.getType()) ? 56 : 64;
   }

   public static boolean init_Z_MacroAction(MacroAction action) {
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

   public static ActionEditorOverlay load_ActionEditorOverla() {
      return init_ActionEditorOverla_4;
   }

   public boolean load_Z_MacroAction(MacroAction action) {
      return this.write_Z && !this.run_Z && this.init_MacroAction == action;
   }

   public void load_V_Z(boolean allowed) {
      this.flush_Z = allowed;
      if (!allowed) {
         this.poll_Z_2();
      }
   }

   public void read_V(boolean configurationOnly) {
      this.close_Z = configurationOnly;
      if (configurationOnly) {
         this.load_V_Z(false);
      }
   }

   private boolean put_Z() {
      if (this.clear_Z()) {
         return true;
      }

      bi.read("Pick/Capture only works in-game.");
      return false;
   }

   private boolean clear_Z() {
      if (this.flush_Z) {
         return true;
      }

      boolean inWorld = init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.level != null;
      if (!inWorld) {
         return false;
      }

      this.close_Z = false;
      this.flush_Z = true;
      return true;
   }

   private boolean init_Z_StreamUtil(StreamUtil mode) {
      return mode == StreamUtil.CUSTOM_MENU_BUTTON ? true : mode == StreamUtil.PACKET_NAME || this.clear_Z();
   }

   public void init_V_List(List<MacroAction> actions) {
      if (this.write_Z && !this.run_Z && this.init_MacroAction != null && actions != null && !actions.contains(this.init_MacroAction)) {
         this.set(false);
      }
   }

   public void init_V_MacroAction(MacroAction action) {
      if (this.load_Z_MacroAction(action)) {
         this.set(false);
      }
   }

   public ActionEditorOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(textRenderer);
      this.init_RaceStepSelectorOv = new RaceStepSelectorOverlay(textRenderer);
   }

   public void init_V_aM_I(AutismMacro macro, int actionIndex) {
      this.init_aM = macro;
      this.f_f_I = actionIndex;
      this.task();
   }

   public void init_V_MacroAction(MacroAction action, Runnable onPreSave, Runnable onSave) {
      this.init_V_MacroAction(action, onPreSave, onSave, 1);
   }

   public void init_V_MacroAction(MacroAction action, Runnable onPreSave, Runnable onSave, int editCount) {
      this.variableWarningStale = true;
      this.flags();
      this.get(true);
      this.auditEditSession++;
      this.run_Z = false;
      this.init_MacroAction = action;
      this.f_d_I = Math.max(1, editCount);
      this.load_Runnable = onPreSave;
      this.init_Runnable = onSave;
      this.init_CompoundTag = AutismMacro.init_CompoundTag_MacroAction(action);
      this.init_ActionFieldSchema = ActionFieldRegistry.init(action);
      this.f_e_I = 0;
      this.task();
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.write_Map.clear();
      this.update_Map.clear();
      this.sync_Map.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      this.open_Map.clear();
      this.poll_Map.clear();
      this.push_Map.clear();
      this.pop_Map.clear();
      this.tick_Map.clear();
      this.init_AutismPacketSelect.write_V_21();
      this.read(action);
      this.load_V_MacroAction(action);
      if (action instanceof UseItemAction useItemAction) {
         ThemedTextInputBox slotField = this.init_Map.get("slot");
         if (slotField != null && useItemAction.slot < 0 && !this.init_CompoundTag.contains("slot")) {
            slotField.init_V_Str("");
         }
      }

      this.init_MapStore = null;
      this.init_StreamUtil.init_V();
      this.f_j_I = -1;
      this.f_k_I = -1;
      if (action instanceof ItemAction ia) {
         this.init_MapStore = new ItemAction();
         this.init_MapStore.itemNames = new ArrayList<>(ia.itemNames);
         this.init_MapStore.itemTargets = this.init_List_List_List(ia.itemTargets, ia.itemNames);
         this.init_MapStore.itemTimes = new ArrayList<>(ia.itemTimes);
         this.init_MapStore.itemActionIdx = new ArrayList<>(ia.itemActionIdx);
         this.init_MapStore.itemButtons = new ArrayList<>(ia.itemButtons);
         this.init_MapStore.itemSearchScopes = new ArrayList<>(ia.itemSearchScopes);
         this.init_MapStore.stackAmountModes = new ArrayList<>(ia.stackAmountModes);

         while (this.init_MapStore.itemTimes.size() < this.init_MapStore.itemNames.size()) {
            this.init_MapStore.itemTimes.add(1);
         }

         while (this.init_MapStore.itemActionIdx.size() < this.init_MapStore.itemNames.size()) {
            this.init_MapStore.itemActionIdx.add(0);
         }

         while (this.init_MapStore.itemButtons.size() < this.init_MapStore.itemNames.size()) {
            this.init_MapStore.itemButtons.add(0);
         }

         while (this.init_MapStore.itemSearchScopes.size() < this.init_MapStore.itemNames.size()) {
            int index = this.init_MapStore.itemSearchScopes.size();
            this.init_MapStore.itemSearchScopes.add(ia.init_MapStore_DataUtil_I(index).ordinal());
         }

         while (this.init_MapStore.stackAmountModes.size() < this.init_MapStore.itemNames.size()) {
            this.init_MapStore.stackAmountModes.add(0);
         }

         this.init_MapStore.targetSlot = ia.targetSlot;
         this.init_MapStore.useSlot = ia.useSlot;
         this.init_MapStore.actionIndex = ia.actionIndex;
         this.init_MapStore.button = ia.button;
         this.init_MapStore.times = ia.times;
         this.init_MapStore.waitForGuiBefore = ia.waitForGuiBefore;
         this.init_MapStore.waitForGuiAfter = ia.waitForGuiAfter;
         this.init_MapStore.guiName = ia.guiName != null ? ia.guiName : "";
         this.init_MapStore.waitForItem = ia.waitForItem;
         this.init_MapStore.useCursorItemForPickupAll = ia.useCursorItemForPickupAll;

         for (int i = 0; i < this.init_MapStore.itemNames.size(); i++) {
            ThemedTextInputBox f = this.init_k_I(28);
            f.poll(true);
            f.init_V_Str(String.valueOf(this.init_MapStore.init_I_I_3(i)));
            this.init_Map.put("item_times_" + i, f);
         }

         ThemedTextInputBox addF = this.init_k_I(120);
         addF.init_V_Component(Component.literal("Item name"));
         addF.init_V_Consumer(text -> this.close_V_2());
         this.flush_Map.put("_item_add", addF);
         ThemedTextInputBox addSlotF = this.init_k_I(52);
         addSlotF.poll(true);
         addSlotF.init_V_Component(Component.literal("Slot"));
         addSlotF.init_V_Consumer(text -> this.close_V_2());
         this.init_Map.put("item_entrySlot", addSlotF);
         this.load_Map.put("item_waitForGuiBefore", ia.waitForGuiBefore);
         this.load_Map.put("item_waitForGuiAfter", ia.waitForGuiAfter);
         this.load_Map.put("item_waitForItem", ia.waitForItem);
         this.load_Map.put("item_useCursorItemForPickupAll", ia.useCursorItemForPickupAll);
         this.load_Map.put("item_useClickDelay", ia.useClickDelay);
         ThemedTextInputBox guiF = this.init_k_I(80);
         guiF.init_V_Str(ia.guiName != null ? ia.guiName : "");
         this.init_Map.put("item_guiName", guiF);
         ThemedTextInputBox delayF = this.init_k_I(60);
         delayF.poll(true);
         delayF.init_V_Str(String.valueOf(Math.max(0, ia.clickDelayMs)));
         this.init_Map.put("item_clickDelayMs", delayF);
      }

      this.f_l_I = -1;
      this.run_List = new ArrayList<>();
      this.init_aD_StreamUtil = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
      this.f_m_I = 1;
      if (action instanceof WaitForSlotChangeAction wsc) {
         for (WaitForSlotChangeAction.Inner_DataUtil e : wsc.entries) {
            this.run_List.add(e.init_aD_DataUtil());
         }
      }

      if (action.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
         this.load_Map.put("listenDuringPreviousAction", action.listensDuringPreviousAction());
         ThemedTextInputBox wscAddF = this.init_k_I(120);
         wscAddF.init_V_Component(Component.literal("Item name (optional)"));
         wscAddF.init_V_Consumer(text -> this.scan());
         this.flush_Map.put("_wsc_add", wscAddF);
         ThemedTextInputBox wscSlotF = this.init_k_I(52);
         wscSlotF.poll(true);
         wscSlotF.init_V_Component(Component.literal("Slot #"));
         wscSlotF.init_V_Consumer(text -> this.scan());
         this.init_Map.put("wsc_slot", wscSlotF);
         ThemedTextInputBox wscCountF = this.init_k_I(36);
         wscCountF.poll(true);
         wscCountF.init_V_Component(Component.literal("Count"));
         wscCountF.init_V_Consumer(text -> this.parse());
         this.init_Map.put("wsc_count", wscCountF);
      }

      this.push_List = null;
      this.pop_List = null;
      this.tick_List = null;
      this.init_J_TaskQueue = null;
      this.f_g_I = 0;
      this.tick_Z = false;
      this.update_Str = null;
      this.init_IArr = null;
      if (action instanceof autismclient.util.macro.CraftAction craftAction) {
         this.push_List = craftAction.init_List_6();

         for (int i = 0; i < this.push_List.size(); i++) {
            autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.push_List.get(i);
            ThemedTextInputBox f = this.init_k_I(44);
            f.poll(true);
            f.init_V_Str(String.valueOf(entry.read_I));
            this.init_Map.put("craft_amount_" + i, f);
            this.load_Map.put("craft_useMax_" + i, entry.init_Z);
         }

         ThemedTextInputBox amtF = this.init_k_I(44);
         amtF.poll(true);
         amtF.init_V_Str("1");
         this.init_Map.put("_craft_amount", amtF);
         ThemedTextInputBox srchF = this.init_k_I(180);
         srchF.init_V_Component(Component.literal("Search recipes..."));
         this.flush_Map.put("_craft_search", srchF);
         this.pop_List = J.init_List(init_Minecraft);
         this.tick_List = J.init(this.pop_List, "");
      }

      if (action instanceof WaitForPacketAction waitForPacketAction) {
         this.write_Map.put("packetNames", this.init_List_List(waitForPacketAction.init()));
         this.load_Map.put("listenDuringPreviousAction", waitForPacketAction.listenDuringPreviousAction);
      }

      this.reset_List = null;
      this.f_z_I = -1;
      if (action instanceof autismclient.util.macro.WaitPacketMatchAction packetMatchAction) {
         this.reset_List = new ArrayList<>();

         for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : packetMatchAction.init_List_9()) {
            this.reset_List.add(rule.init_aM_CachePool());
         }

         if (this.reset_List.isEmpty()) {
            this.reset_List.add(new autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool());
         }

         this.f_z_I = 0;
         this.load_Map.put("listenDuringPreviousAction", packetMatchAction.listenDuringPreviousAction);
         ThemedTextInputBox valueField = this.init_k_I(120);
         valueField.init_V_Component(Component.literal("Value"));
         this.init_Map.put("_wpm_value", valueField);
         this.z();
      }

      this.init_PacketUtil = null;
      if (action instanceof DropAction da) {
         this.init_PacketUtil = new DropAction();
         this.init_PacketUtil.mode = da.mode;
         this.init_PacketUtil.dropCount = da.dropCount;
         this.init_PacketUtil.itemNames = new ArrayList<>(da.itemNames);
         this.init_PacketUtil.itemTargets = this.init_List_List_List(da.itemTargets, da.itemNames);
         this.init_PacketUtil.itemCounts = new ArrayList<>(da.itemCounts);
         this.init_PacketUtil.waitForGuiBefore = da.waitForGuiBefore;
         this.init_PacketUtil.waitForGuiAfter = da.waitForGuiAfter;
         this.init_PacketUtil.guiName = da.guiName != null ? da.guiName : "";
         this.init_PacketUtil.useHandlerSlots = da.useHandlerSlots;

         while (this.init_PacketUtil.itemCounts.size() < this.init_PacketUtil.itemNames.size()) {
            this.init_PacketUtil.itemCounts.add(this.init_PacketUtil.init_I_I_4(this.init_PacketUtil.itemCounts.size()));
         }

         for (int i = 0; i < this.init_PacketUtil.itemNames.size(); i++) {
            ThemedTextInputBox f = this.init_k_I(32);
            f.poll(true);
            f.init_V_Str(String.valueOf(this.init_PacketUtil.itemCounts.get(i)));
            this.init_Map.put("drop_count_" + i, f);
         }

         ThemedTextInputBox cntF = this.init_k_I(60);
         cntF.poll(true);
         cntF.init_V_Str(String.valueOf(da.dropCount));
         cntF.init_V_Consumer(text -> this.prev_V());
         this.init_Map.put("drop_globalCount", cntF);
         ThemedTextInputBox addDropF = this.init_k_I(120);
         addDropF.init_V_Component(Component.literal("Item name..."));
         addDropF.init_V_Consumer(text -> this.stop_V());
         this.flush_Map.put("_drop_add", addDropF);
         ThemedTextInputBox addDropSlotF = this.init_k_I(52);
         addDropSlotF.poll(true);
         addDropSlotF.init_V_Component(Component.literal("Slot"));
         addDropSlotF.init_V_Consumer(text -> this.stop_V());
         this.init_Map.put("drop_entrySlot", addDropSlotF);
         ThemedTextInputBox dropGuiF = this.init_k_I(80);
         dropGuiF.init_V_Str(da.guiName != null ? da.guiName : "");
         this.init_Map.put("drop_guiName", dropGuiF);
         this.load_Map.put("drop_waitForGuiBefore", da.waitForGuiBefore);
         this.load_Map.put("drop_waitForGuiAfter", da.waitForGuiAfter);
         this.load_Map.put("drop_useHandlerSlots", da.useHandlerSlots);
         DropAction.Inner_DataUtil[] modes = DropAction.Inner_DataUtil.values();
         int mi = 0;

         for (int i = 0; i < modes.length; i++) {
            if (modes[i] == da.mode) {
               mi = i;
               break;
            }
         }

         this.read_Map.put("drop_mode", mi);
         this.next_V();
      }

      this.init_H = null;
      this.init_bE = null;
      if (action instanceof PayloadAction pa) {
         this.init_H = new PayloadAction();
         this.init_H.fromTag(pa.toTag());
         this.load_V_H(this.init_H);
      }

      this.clear_List = null;
      if (action instanceof WaitForLanStepAction wls) {
         this.clear_List = new ArrayList<>();

         for (WaitForLanStepAction.Inner_DataUtil e : wls.entries) {
            this.clear_List.add(new WaitForLanStepAction.Inner_DataUtil(e.init_Str, e.init_I));
         }

         for (int i = 0; i < this.clear_List.size(); i++) {
            WaitForLanStepAction.Inner_DataUtil e = this.clear_List.get(i);
            ThemedTextInputBox uf = this.init_k_I(80);
            uf.init_V_Str(e.init_Str);
            this.init_Map.put("lan_user_" + i, uf);
            ThemedTextInputBox sf = this.init_k_I(40);
            sf.poll(true);
            sf.init_V_Str(String.valueOf(e.init_I));
            this.init_Map.put("lan_step_" + i, sf);
         }

         ThemedTextInputBox newUF = this.init_k_I(80);
         newUF.init_V_Component(Component.literal("Peer name..."));
         this.flush_Map.put("_lan_user_add", newUF);
         ThemedTextInputBox newSF = this.init_k_I(40);
         newSF.poll(true);
         newSF.init_V_Str("1");
         this.flush_Map.put("_lan_step_add", newSF);
         ThemedTextInputBox dsF = this.init_k_I(60);
         dsF.poll(true);
         dsF.init_V_Str(String.valueOf(wls.defaultStep));
         this.init_Map.put("lan_defaultStep", dsF);
         this.load_Map.put("lan_filterByUser", wls.filterByUser);
         this.load_Map.put("listenDuringPreviousAction", wls.listenDuringPreviousAction);
      }

      this.poll_Z = false;
      if (action instanceof WaitForSoundAction) {
         ThemedTextInputBox search = this.flush_Map.get("soundIds");
         if (search != null) {
            search.init_V_Component(Component.literal("Search sound id..."));
         }
      }

      if (action instanceof WaitForEntityAction) {
         ThemedTextInputBox search = this.flush_Map.get("entityIds");
         if (search != null) {
            search.init_V_Component(Component.literal("Search entity type..."));
         }
      }

      if (action instanceof LookAtBlockAction) {
         ThemedTextInputBox search = this.flush_Map.get("entityIds");
         if (search != null) {
            search.init_V_Component(Component.literal("Search entity type..."));
         }
      }

      if (action instanceof StoreItemAction) {
         ThemedTextInputBox search = this.flush_Map.get("targetItems");
         if (search != null) {
            search.init_V_Component(Component.literal("Search item id..."));
         }
      }

      if (action instanceof InventoryAuditAction) {
         ThemedTextInputBox search = this.flush_Map.get("targetItems");
         if (search != null) {
            search.init_V_Component(Component.literal("Item name"));
         }
      }

      this.start_List.clear();
      this.get_Map.clear();
      this.set_Map.clear();
      this.state_Z = false;
      if (action instanceof PayAction) {
         ThemedTextInputBox search = this.flush_Map.get("players");
         if (search != null) {
            search.init_V_Component(Component.literal("Search or add player..."));
         }
      }

      this.stop_List.clear();
      this.prev_List = null;
      if (action instanceof ak) {
         ThemedTextInputBox search = this.init_k_I(180);
         search.init_V_Component(Component.literal("Search module..."));
         this.flush_Map.put("_toggle_module_search", search);
         this.c();
         ak toggleModuleAction = (ak)action;
         this.prev_List = new ArrayList<>();

         for (ak.Inner_DataUtil entry : toggleModuleAction.entries) {
            if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
               this.prev_List.add(new ak.Inner_DataUtil(entry.init_Str, entry.init_ak_StreamUtil));
            }
         }
      }

      if (action instanceof WaitForChatAction) {
         ThemedTextInputBox search = this.init_k_I(180);
         search.init_V_Component(Component.literal("Search recent chat..."));
         this.flush_Map.put("_wait_chat_search", search);
         ThemedTextInputBox captureName = this.init_k_I(100);
         captureName.init_V_Component(Component.literal("Variable name"));
         this.flush_Map.put("_capture_name", captureName);
         this.size_Z = false;
         this.h();
         ThemedTextInputBox patternField = this.init_Map.get("pattern");
         if (patternField != null) {
            patternField.load(true);
            patternField.sync(40);
            String existingPatternJson = this.init_CompoundTag.getStringOr("patternJson", "");
            Component initialPattern = existingPatternJson != null && !existingPatternJson.isBlank()
               ? this.update_Component(this.init_CompoundTag.getStringOr("pattern", ""))
               : Component.literal(MacroExecutor.init_Str_Str(this.init_CompoundTag.getStringOr("pattern", "")));
            String visiblePattern = initialPattern.getString();
            this.init_CompoundTag.putString("pattern", visiblePattern);
            if (existingPatternJson != null && !existingPatternJson.isBlank()) {
               this.init_CompoundTag.putString("patternJson", MacroExecutor.init_Str_Component(initialPattern));
            } else {
               this.init_CompoundTag.putString("patternJson", "");
            }

            patternField.init_V_Function(this::update_Component);
            this.cache_Z = true;
            patternField.init_V_Str(visiblePattern);
            this.cache_Z = false;
            patternField.init_V_Consumer(value -> {
               if (!this.cache_Z) {
                  String visibleValue = MacroExecutor.init_Str_Str(value);
                  this.init_CompoundTag.putString("pattern", visibleValue);
                  this.init_CompoundTag.putString("patternJson", "");
               }
            });
         }
      } else {
         this.cache_Z = false;
         this.size_Z = false;
         this.h();
      }

      if (action instanceof WaitForGuiAction) {
         ThemedTextInputBox captureName = this.init_k_I(100);
         captureName.init_V_Component(Component.literal("Variable name"));
         this.flush_Map.put("_capture_name", captureName);
      }

      if (action instanceof autismclient.util.macro.CaptureValueAction) {
         ThemedTextInputBox token = this.init_k_I(180);
         token.open(false);
         token.update(false);
         token.init_V_Component(Component.literal("Set a variable name"));
         this.flush_Map.put("_capture_token", token);
         ThemedTextInputBox preview = this.init_k_I(180);
         preview.open(false);
         preview.update(false);
         preview.init_V_Component(Component.literal("Nothing available now"));
         this.flush_Map.put("_capture_preview", preview);
         this.load_Str = autismclient.util.macro.CaptureValueAction.init(this.update("exampleText", ""), this.update("selectedText", ""), this.update("saveAs", "value"));
         this.read_Str = "";
         this.pop_Z = false;

         for (String key : List.of("saveAs", "exampleText", "selectedText")) {
            ThemedTextInputBox field = this.init_Map.get(key);
            if (field != null) {
               field.init_V_Consumer(ignored -> this.e());
            }
         }
      } else {
         this.push_Z = false;
         this.load_Str = "";
      }

      if (action instanceof RotateAction) {
         this.data_Z = false;
         this.i();
         RotateAction rotateAction = (RotateAction)action;
         ThemedTextInputBox smoothnessField = this.init_Map.get("smoothness");
         int smoothness = RotateAction.init_I(rotateAction.smoothness);
         this.init_CompoundTag.putInt("smoothness", smoothness);
         if (smoothnessField != null) {
            smoothnessField.init_V_Str(String.valueOf(smoothness));
         }
      } else if (action instanceof LookAtBlockAction lookAtAction) {
         this.data_Z = false;
         this.i();
         ThemedTextInputBox smoothnessField = this.init_Map.get("smoothness");
         int smoothness = RotateAction.init_I(lookAtAction.smoothness);
         this.init_CompoundTag.putInt("smoothness", smoothness);
         if (smoothnessField != null) {
            smoothnessField.init_V_Str(String.valueOf(smoothness));
         }
      } else {
         this.data_Z = false;
         this.i();
      }

      if (action instanceof XCarryAction xCarryAction) {
         this.value_Z = false;
         this.j();
         ThemedTextInputBox delayField = this.init_Map.get("safeClickDelayTicks");
         int safeDelay = XCarryAction.write(xCarryAction.safeClickDelayTicks);
         this.init_CompoundTag.putInt("safeClickDelayTicks", safeDelay);
         this.init_CompoundTag.putBoolean("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
         this.init_CompoundTag.putBoolean("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
         this.load_Map.put("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
         this.load_Map.put("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
         if (delayField != null) {
            delayField.init_V_Str(String.valueOf(safeDelay));
         }
      } else {
         this.value_Z = false;
         this.j();
      }

      if (action.getType() == MacroActionType.INVENTORY || action.getType() == MacroActionType.CLOSE_GUI || action.getType() == MacroActionType.SAVE_GUI) {
         this.load_Map.put("sendPacket", !this.init_CompoundTag.getBooleanOr("sendPacket", true));
      }

      if (action.getType() == MacroActionType.STORE_ITEM) {
         this.load_Map.put("closeSendPkt", !this.init_CompoundTag.getBooleanOr("closeSendPkt", true));
      }

      this.store();
      this.node();
      if (action.getType() == MacroActionType.PLACE) {
         ThemedTextInputBox slotF = this.init_k_I(52);
         slotF.poll(true);
         slotF.init_V_Component(Component.literal("Slot"));
         slotF.init_V_Consumer(text -> this.table());
         this.init_Map.put("place_itemSlot", slotF);
         if (action instanceof PlaceAction placeAction) {
            this.load_Map.put("place_waitForItem", placeAction.waitForItem);
            this.load_Map.put("place_silentSwitch", placeAction.silentSwitch);
         }

         MacroItemTarget placeTarget = this.update_Map.get("itemName");
         if (placeTarget == null && action instanceof PlaceAction placeAction) {
            placeTarget = this.init_ArrayUtil_ArrayUtil_Str(placeAction.itemTarget, placeAction.itemName);
         }

         if (placeTarget != null) {
            slotF.init_V_Str(placeTarget.init_Z() ? String.valueOf(placeTarget.init_I) : "");
         }

         ThemedTextInputBox nameF = this.init_Map.get("itemName");
         if (nameF != null) {
            nameF.init_V_Consumer(text -> this.table());
         }

         if (action instanceof PlaceAction placeAction && placeAction.blockPos != null) {
            autismclient.util.BlockPreviewRenderer.init(placeAction.blockPos, -50373);
         }
      }

      this.f_b_I = this.init_I_MacroAction(action);
      this.pool();
      int contentH = this.stop_I();
      int desiredH = 18 + contentH + this.run_I() + 4;
      int minH = this.push_I_2();
      int maxH = Math.max(minH, cA.load_I() * 4 / 5);
      this.f_c_I = Math.max(minH, Math.min(maxH, desiredH));
      this.write_Z = true;
      OverlayManager.init_bk().update_V(this);
   }

   public void init_V_H(PayloadAction action) {
      this.init_V_MacroAction(action, null, null);
      this.run_Z = true;
   }

   public boolean init_Z_Str(String channel) {
      String target = channel == null ? "" : channel.trim();
      if (!this.write_Z || target.isBlank()) {
         return false;
      }

      if (!this.run_Z && this.init_H == null && !(this.init_MacroAction instanceof PayloadAction)) {
         return false;
      }

      ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
      if (channelField == null) {
         return false;
      }

      this.start_Z = true;

      try {
         channelField.init_V_Str(target);
         if (this.init_bE != null) {
            this.init_bE.read_Str = target;
            this.init_bE.init_V();
            this.init_ActionEditorOverla = null;
         }

         if (this.init_H != null) {
            this.init_H.channel = target;
         }

         ThemedTextInputBox contentField = this.init_Map.get("payload_content");
         if (contentField != null) {
            contentField.init_V_Str(this.init_Str_Str_Str(contentField.load_Str(), target));
         }

         this.put_Z = true;
      } finally {
         this.start_Z = false;
      }

      this.fill();
      OverlayManager.init_bk().update_V(this);
      return true;
   }

   private String init_Str_Str_Str(String script, String channel) {
      String safeScript = script == null ? "" : script.replace("\r\n", "\n").replace('\r', '\n');
      String safeChannel = channel != null && !channel.isBlank() ? channel.trim() : "minecraft:brand";
      String[] lines = safeScript.split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         String raw = lines[i] == null ? "" : lines[i];
         String stripped = raw.stripLeading();
         if (!stripped.startsWith("#")) {
            int split = this.init_I_Str(raw);
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
   public String init_Str() {
      return "autism-action-editor";
   }

   @Override
   public int init_I() {
      return 168;
   }

   @Override
   public int load_I() {
      return this.push_I_2();
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public void init_V_Z(boolean v) {
      this.write_Z = v;
      if (v) {
         this.valid_List.clear();
         this.size_List.clear();
      } else {
         this.push(true);
         autismclient.gui.f.b.SyncManager.read(this.sync_List);
         this.init_AutismPacketSelect.init_V_Z(false);
         this.init_RaceStepSelectorOv.init_V_Z(false);
         this.push_V();
      }
   }

   @Override
   public boolean flush_Z() {
      return this.load_Z;
   }

   @Override
   public void write_V_Z(boolean value) {
      if (this.load_Z != value) {
         super.write_V_Z(value);
         if (value) {
            autismclient.gui.f.b.SyncManager.read(this.sync_List);
            this.init_AutismPacketSelect.init_V_Z(false);
            this.init_RaceStepSelectorOv.init_V_Z(false);
            this.valid_List.clear();
            this.size_List.clear();
         }
      }
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.bind, this.f_a_I, this.f_b_I, this.f_c_I, this.write_Z, this.load_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds b) {
      OverlayBounds c = this.init_cH_cX_cH(this, b);
      this.bind = c.init_I;
      this.f_a_I = c.load_I;
      this.f_b_I = c.read;
      this.f_c_I = c.write;
      this.write_Z = c.init_Z;
      this.load_Z = c.load_Z;
   }

   @Override
   public boolean init_Z_D_D(double mx, double my) {
      return this.write_Z
         && (
            this.init_AutismPacketSelect.sync_Z()
               || this.init_RaceStepSelectorOv.sync_Z()
               || autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List)
               || mx >= this.bind && mx < this.bind + this.f_b_I && my >= this.f_a_I && my < this.f_a_I + (this.load_Z ? 14 : this.f_c_I)
         );
   }

   @Override
   public boolean load_Z_D_D(double mx, double my) {
      return this.write_Z
         && mx >= this.bind
         && mx < this.bind + this.f_b_I
         && my >= this.f_a_I
         && my < this.f_a_I + 14
         && !this.read_Z_D_2(mx, my, this.init_cH());
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean write_Z() {
      if (this.init_AutismPacketSelect.write_Z()) {
         return true;
      }

      if (this.init_RaceStepSelectorOv.write_Z()) {
         return true;
      }

      for (ThemedTextInputBox f : this.init_Map.values()) {
         if (f.init_Z()) {
            return true;
         }
      }

      for (ThemedTextInputBox f : this.flush_Map.values()) {
         if (f.init_Z()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void push_V() {
      this.init_Map.values().forEach(f -> f.push(false));
      this.flush_Map.values().forEach(f -> f.push(false));
   }

   public boolean close_Z() {
      return this.init_StreamUtil.init_Z();
   }

   public boolean open_Z() {
      return this.init_StreamUtil.init_Z();
   }

   public String load_Str_2() {
      return "Capturing " + this.clear_Str() + " - " + this.reset_Str();
   }

   public String read_Str() {
      String activeCaptureKey = this.init_StreamUtil.init_Str();
      if ("_item_entries".equals(activeCaptureKey) || "_drop_entries".equals(activeCaptureKey)) {
         return "Right-click a slot to set the item. Esc = cancel";
      } else if ("_wsc_entries".equals(activeCaptureKey)) {
         return "Right-click slots to add items or exact slots. Esc = done";
      } else if (!this.write_Map.containsKey(activeCaptureKey)) {
         return "Right-click a slot in this screen. Esc = cancel";
      } else if (this.put_Z(activeCaptureKey)) {
         return "Right-click slots to add items, or empty slots for exact slots. Esc = done";
      } else {
         return this.tick_Z(activeCaptureKey)
            ? "Right-click items anywhere to add by name. Ctrl+right-click for exact slots. Esc = done"
            : "Right-click slots to add item names. Esc = done";
      }
   }

   public String init_Str_Slot(Slot slot, String itemName, String registryId) {
      if (slot == null) {
         return "";
      }

      int handlerSlot = autismclient.util.aw.load(init_Minecraft, slot);
      int visibleSlot = handlerSlot >= 0 ? autismclient.util.aw.init_I(init_Minecraft, handlerSlot) : -1;
      String slotText = visibleSlot >= 0 ? String.valueOf(visibleSlot) : "Handler " + handlerSlot;
      String slotDetail = "Handler " + handlerSlot;
      String itemText = !registryId.isEmpty() ? registryId : (!itemName.isEmpty() ? itemName : "Empty slot");
      return slotText.equals(slotDetail) ? "Hover: " + slotText + " | " + itemText : "Hover: " + slotText + " | " + slotDetail + " | " + itemText;
   }

   public boolean poll_Z_2() {
      return this.tick_Z() ? true : this.init_StreamUtil.init(() -> this.init_V_Z_Z(false, false));
   }

   public boolean push_Z_2() {
      ck state = ck.init_ck();
      return this.init_StreamUtil.init_Z()
         || this.write_Str != null
         || this.poll_List != null
         || this.sync_Z
         || this.load_Screen != null
         || this.init_Screen != null
         || state.level()
         || state.pool()
         || state.frame()
         || state.count()
         || state.cache_Z();
   }

   public boolean init_Z_Slot(Slot slot, String itemName, String registryId) {
      return this.init_Z_Slot(slot, itemName, registryId, false);
   }

   public boolean init_Z_Slot(Slot slot, String itemName, String registryId, boolean slotMode) {
      this.variableWarningStale = true;
      int visibleSlot = slot != null ? autismclient.util.aw.init_I(init_Minecraft, slot) : -1;
      MacroItemTarget capturedTarget = this.init_ArrayUtil_Slot(slot, itemName, registryId, visibleSlot);
      if (this.init_StreamUtil.init_Z() && (!itemName.isEmpty() || visibleSlot >= 0)) {
         boolean useSlot = slotMode && visibleSlot >= 0;
         if (useSlot) {
            capturedTarget = MacroItemTarget.init_ArrayUtil_I(Math.max(0, visibleSlot));
         }

         String key = this.init_StreamUtil.init_Str();
         if ("_item_entries".equals(key) && this.init_MapStore != null) {
            this.load_V_ArrayUtil(capturedTarget);
            this.init_StreamUtil.init_V();
            this.init_V_Z_Z(false, false);
            return true;
         }

         if ("_drop_entries".equals(key) && this.init_PacketUtil != null) {
            this.read(capturedTarget);
            this.init_StreamUtil.init_V();
            this.init_V_Z_Z(false, false);
            return true;
         }

         if ("_wsc_entries".equals(key)) {
            String rawTarget = capturedTarget.write_Str();
            if (rawTarget == null) {
               this.init_V_Str_I("Nothing to add from that slot", -38037);
               return true;
            }

            String norm = StoreItemAction.init_Str_Str_8(rawTarget);
            if (norm != null && !norm.isBlank()) {
               String disp = StoreItemAction.load_Str_Str_3(norm);
               if (this.f_l_I >= 0 && this.f_l_I < this.run_List.size()) {
                  if (!this.init_Z_Str_I(norm, this.f_l_I)) {
                     WaitForSlotChangeAction.Inner_DataUtil entry = this.run_List.get(this.f_l_I);
                     entry.init_Str = norm;
                     entry.init_ArrayUtil = capturedTarget.init_ArrayUtil();
                     this.init_V_aD_DataUtil(entry);
                     this.init_V_Str_I("Updated: " + disp, -10035062);
                  } else {
                     this.init_V_Str_I("Already in list: " + disp, -38037);
                  }
               } else if (this.push_Z(norm)) {
                  this.init_V_Str_I("Already added: " + disp, -38037);
               } else {
                  WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil(norm, this.init_aD_StreamUtil, this.f_m_I);
                  entry.init_ArrayUtil = capturedTarget.init_ArrayUtil();
                  entry.init_Str = norm;
                  this.run_List.add(entry);
                  this.init_V_Str_I("Added: " + disp, -10035062);
               }

               return true;
            } else {
               this.init_V_Str_I("Nothing to add from that slot", -38037);
               return true;
            }
         } else {
            if ("wait_inventory_slot".equals(key)) {
               this.poll(Math.max(0, visibleSlot));
               this.init_StreamUtil.init_V();
               this.init_V_Z_Z(false, false);
               this.init_V_Str_I("Slot set: " + Math.max(0, visibleSlot), -10035062);
               return true;
            }

            if ("itemName".equals(key) && this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.USE_ITEM) {
               ThemedTextInputBox itemField = this.init_Map.get("itemName");
               ThemedTextInputBox slotField = this.init_Map.get("slot");
               if (capturedTarget.update()) {
                  MacroItemTarget nameOnly = init_ArrayUtil_ArrayUtil(capturedTarget);
                  if (itemField != null) {
                     itemField.init_V_Str(nameOnly.init_Str());
                  }

                  this.update_Map.put("itemName", nameOnly.init_ArrayUtil());
                  this.init_V_Str_I("Item set: " + nameOnly.read_Str(), -10035062);
               } else if (capturedTarget.init_Z()) {
                  if (itemField != null) {
                     itemField.init_V_Str("");
                  }

                  this.update_Map.remove("itemName");
                  if (slotField != null) {
                     slotField.init_V_Str(String.valueOf(Math.max(0, capturedTarget.init_I)));
                  }

                  this.init_V_Str_I("Slot set: " + Math.max(0, capturedTarget.init_I), -10035062);
               } else {
                  this.init_V_Str_I("Nothing to capture from that slot", -38037);
               }

               this.init_StreamUtil.init_V();
               this.init_V_Z_Z(false, false);
               return true;
            } else if (this.write_Map.containsKey(key)) {
               List<String> list = this.write_Map.get(key);
               if (list == null) {
                  return false;
               }

               ActionEditorOverlay.Inner_DataUtil result = this.tick_Z(key)
                  ? this.init_ActionEditorOverla_Slot(slot, itemName, registryId, visibleSlot, useSlot, list)
                  : this.init_ActionEditorOverla_FieldDef(
                     this.read_FieldDef(key),
                     key,
                     list,
                     useSlot
                        ? capturedTarget.write_Str()
                        : (
                           this.put_Z(key)
                              ? capturedTarget.write_Str()
                              : (
                                 this.run_Z(key)
                                    ? init_ArrayUtil_ArrayUtil(capturedTarget).write_Str()
                                    : (this.get_Z(key) ? capturedTarget.write_Str() : itemName)
                              )
                        )
                  );
               if (result != null && result.init_Z() && (this.get_Z(key) || this.put_Z(key) || this.run_Z(key))) {
                  MacroItemTarget preservedTarget = this.tick_Z(key)
                     ? load_ArrayUtil_ArrayUtil(capturedTarget)
                     : (this.run_Z(key) ? (useSlot ? capturedTarget : init_ArrayUtil_ArrayUtil(capturedTarget)) : capturedTarget);
                  this.init_V_Str_2(key, list, preservedTarget);
               }

               if (result != null && result.init_Str() != null && !result.init_Str().isBlank()) {
                  this.init_V_Str_I(result.init_Str(), result.init_I());
               }

               return true;
            } else {
               ThemedTextInputBox tf = this.init_Map.get(key);
               if (tf == null) {
                  return false;
               }

               FieldDef field = this.read_FieldDef(key);
               if (field != null && field.type() == CachePool.SLOT) {
                  tf.init_V_Str(String.valueOf(Math.max(0, visibleSlot)));
               } else if (useSlot) {
                  tf.init_V_Str(capturedTarget.write_Str());
                  if ("itemName".equals(key) || "fromItemName".equals(key) || "toItemName".equals(key) || this.update_Map.containsKey(key)) {
                     this.update_Map.put(key, capturedTarget.init_ArrayUtil());
                  }
               } else {
                  MacroItemTarget nameOnly = init_ArrayUtil_ArrayUtil(capturedTarget);
                  tf.init_V_Str(nameOnly.init_Str());
                  if ("itemName".equals(key) || "fromItemName".equals(key) || "toItemName".equals(key) || this.update_Map.containsKey(key)) {
                     this.update_Map.put(key, nameOnly.init_ArrayUtil());
                  }
               }

               this.init_StreamUtil.init_V();
               this.init_V_Z_Z(false, false);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.a__Z()) {
         this.push(false);
         this.init_F_2 = delta;
         this.valid_List.clear();
         this.size_List.clear();
         int rawMouseX = mouseX;
         int rawMouseY = mouseY;
         if (!this.sync_List.isEmpty() && autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List)) {
            mouseX = -2147483648;
            mouseY = -2147483648;
         }

         this.sync_List.clear();
         this.init_NetHelper = null;
         if (this.variableWarningStale) {
            this.variableWarningStale = false;
            this.pool();
         }

         if (!this.load_Z) {
            int neededH = 18 + this.stop_I() + this.run_I() + 4;
            int minH = this.push_I_2();
            int maxH = Math.max(minH, cA.load_I() * 4 / 5);
            this.f_c_I = Math.max(minH, Math.min(maxH, neededH));
         }

         OverlayBounds clamped = this.init_cH_cX_cH(this, new OverlayBounds(this.bind, this.f_a_I, this.f_b_I, this.f_c_I, this.write_Z, this.load_Z));
         this.bind = clamped.init_I;
         this.f_a_I = clamped.load_I;
         this.f_b_I = clamped.read;
         this.f_c_I = clamped.write;
         String title = this.start_Str();
         OverlayBounds bounds = this.init_cH();
         this.init_V_GuiGraphicsExtract_6(context, mouseX, mouseY, bounds, title, this.load_Z, this.update_Z);
         boolean clipBody = this.init_Z_GuiGraphicsExtract(context, bounds, this.load_Z);
         if (clipBody) {
            try {
               int frameH = this.load_I_cH_Z(bounds, false);
               int bodyTop = this.f_a_I + 14;
               int bodyBtm = this.f_a_I + frameH;
               if (bodyBtm > bodyTop + 1) {
                  this.init_V_GuiGraphicsExtract(context, mouseX, mouseY, delta, bodyTop, bodyBtm);
               }

               if (!this.sync_List.isEmpty()) {
                  autismclient.gui.f.b.SyncManager.load(context, this.init_Font, this.sync_List, rawMouseX, rawMouseY);
               }
            } finally {
               this.init_V_GuiGraphicsExtract_Z(context, true);
            }
         }

         this.init_V_GuiGraphicsExtract_14(context, bounds, this.load_Z, this.update_Z);
         if (!this.sync_List.isEmpty() && autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List)) {
            context.nextStratum();
            autismclient.gui.f.b.SyncManager.read(context, this.init_Font, this.sync_List, rawMouseX, rawMouseY);
         }

         if (this.init_AutismPacketSelect.sync_Z()) {
            this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(context, mouseX, mouseY, delta);
         }

         if (this.init_RaceStepSelectorOv.sync_Z()) {
            this.init_RaceStepSelectorOv.init_V_GuiGraphicsExtract_15(context, mouseX, mouseY, delta);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int bodyTop, int bodyBtm) {
      if (this.isLegacyMulti()) {
         FileCache.init(
            context, this.init_Font, "Legacy Multi is unsupported. This block and its steps are skipped when the macro runs.",
            this.bind + 6, bodyTop + 4, this.f_b_I - 12
         );
      } else if (this.init_MapStore != null) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.init_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_H != null) {
         int contentBtm = bodyBtm - this.run_I();
         int contentAreaH = contentBtm - bodyTop;
         int totalContentH = this.stop_I();
         int maxScroll = Math.max(0, totalContentH - contentAreaH);
         this.f_e_I = Math.max(0, Math.min(this.f_e_I, maxScroll));
         boolean needsScroll = maxScroll > 0;
         int sbReserve = needsScroll ? 6 : 0;
         int x = this.bind + 4;
         int w = this.f_b_I - 8 - sbReserve;
         HashUtil.init_HashUtil()
            .init(context, autismclient.gui.f.NetHelper.init(this.bind + 1, bodyTop, Math.max(0, this.f_b_I - 2), Math.max(0, contentBtm - bodyTop)));

         try {
            this.open(context, x, bodyTop - this.f_e_I, w, mouseX, mouseY, delta);
         } finally {
            HashUtil.init_HashUtil().init(context);
         }

         if (needsScroll) {
            int sbX = this.bind + this.f_b_I - 5 - 1;
            this.init_V_GuiGraphicsExtract(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.f_e_I, mouseX, mouseY);
            int capSbY = bodyTop;
            int capSbH = contentAreaH;
            this.size_List.add(new ActionEditorOverlay.SyncManager(sbX, bodyTop, 5, contentAreaH, my -> {
               int rel = Math.max(0, Math.min(capSbH, my - capSbY));
               this.f_e_I = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
            }));
         }
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SEND_PACKET) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.flush(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.update_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.sync(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SOUND) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.push(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_ENTITY) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.pop(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.USE_ITEM) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.clear(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.LOOK_AT_BLOCK) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.start(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.ROTATE) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.reset(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.GO_TO) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.stop(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SWAP_SLOTS) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.next(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CLICK) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.prev(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DISCONNECT) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.valid(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_GUI) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.size(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_CHAT) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.init_V_GuiGraphicsExtract(context, x, bodyTop, bodyBtm - this.run_I(), w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SELECT_SLOT) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.data(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_COOLDOWN) {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.value(context, x, bodyTop, w, mouseX, mouseY, delta);
      } else if (this.init_MacroAction == null || this.init_MacroAction.getType() != MacroActionType.OPEN_CONTAINER && this.init_MacroAction.getType() != MacroActionType.INTERACT_ENTITY) {
         if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.add(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_INVENTORY_PREDICATE) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.cache(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CAPTURE_VALUE) {
            int contentBtm = bodyBtm - this.run_I();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.stop_I();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.f_e_I = Math.max(0, Math.min(this.f_e_I, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.bind + 4;
            int w = this.f_b_I - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.bind + 1, bodyTop, Math.max(0, this.f_b_I - 2), contentAreaH);
            this.init_NetHelper = clip;
            int firstBodyHit = this.valid_List.size();
            HashUtil.init_HashUtil().init(context, clip);

            try {
               this.state(context, x, bodyTop - this.f_e_I, w, mouseX, mouseY, delta);
            } finally {
               HashUtil.init_HashUtil().init(context);
            }

            this.init_V_I_NetHelper(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.bind + this.f_b_I - 5 - 1;
               this.init_V_GuiGraphicsExtract(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.f_e_I, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.size_List.add(new ActionEditorOverlay.SyncManager(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.f_e_I = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DELAY_PACKETS) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.queue(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.MINE) {
            int contentBtm = bodyBtm - this.run_I();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.stop_I();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.f_e_I = Math.max(0, Math.min(this.f_e_I, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.bind + 4;
            int w = this.f_b_I - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.bind + 1, bodyTop, Math.max(0, this.f_b_I - 2), contentAreaH);
            this.init_NetHelper = clip;
            int firstBodyHit = this.valid_List.size();
            HashUtil.init_HashUtil().init(context, clip);

            try {
               this.buffer(context, x, bodyTop - this.f_e_I, w, mouseX, mouseY, delta);
            } finally {
               HashUtil.init_HashUtil().init(context);
            }

            this.init_V_I_NetHelper(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.bind + this.f_b_I - 5 - 1;
               this.init_V_GuiGraphicsExtract(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.f_e_I, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.size_List.add(new ActionEditorOverlay.SyncManager(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.f_e_I = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PAY) {
            int contentBtm = bodyBtm - this.run_I();
            int contentAreaH = Math.max(0, contentBtm - bodyTop);
            int totalContentH = this.stop_I();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.f_e_I = Math.max(0, Math.min(this.f_e_I, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.bind + 4;
            int w = this.f_b_I - 8 - sbReserve;
            autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(this.bind + 1, bodyTop, Math.max(0, this.f_b_I - 2), contentAreaH);
            this.init_NetHelper = clip;
            int firstBodyHit = this.valid_List.size();
            HashUtil.init_HashUtil().init(context, clip);

            try {
               this.set(context, x, bodyTop - this.f_e_I, w, mouseX, mouseY, delta);
            } finally {
               HashUtil.init_HashUtil().init(context);
            }

            this.init_V_I_NetHelper(firstBodyHit, clip);
            if (needsScroll) {
               int sbX = this.bind + this.f_b_I - 5 - 1;
               this.init_V_GuiGraphicsExtract(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.f_e_I, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.size_List.add(new ActionEditorOverlay.SyncManager(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.f_e_I = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY_AUDIT) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.get(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.STORE_ITEM) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.run(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.TOGGLE_MODULE) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.put(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.push_List != null) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.load_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_PacketUtil != null) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.read_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.clear_List != null) {
            int x = this.bind + 4;
            int w = this.f_b_I - 8;
            this.write_V_GuiGraphicsExtract(context, x, bodyTop, w, mouseX, mouseY, delta);
         } else if (this.init_ActionFieldSchema != null && !this.init_ActionFieldSchema.fields().isEmpty()) {
            int contentBtm = bodyBtm - this.run_I();
            int contentAreaH = contentBtm - bodyTop;
            int totalContentH = this.stop_I();
            int maxScroll = Math.max(0, totalContentH - contentAreaH);
            this.f_e_I = Math.max(0, Math.min(this.f_e_I, maxScroll));
            boolean needsScroll = maxScroll > 0;
            int sbReserve = needsScroll ? 6 : 0;
            int x = this.bind + 4;
            int w = this.f_b_I - 8 - sbReserve;
            HashUtil.init_HashUtil()
               .init(context, autismclient.gui.f.NetHelper.init(this.bind + 1, bodyTop, Math.max(0, this.f_b_I - 2), Math.max(0, contentBtm - bodyTop)));

            try {
               int y = bodyTop + 4 - this.f_e_I;
               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
                  y = this.init_I_GuiGraphicsExtract_5(context, "packetOrder", x, y, w, mouseX, mouseY, delta);
                  BlockPos previewPos = null;

                  try {
                     ThemedTextInputBox fx = this.init_Map.get("blockPos_0");
                     ThemedTextInputBox fy = this.init_Map.get("blockPos_1");
                     ThemedTextInputBox fz = this.init_Map.get("blockPos_2");
                     if (fx != null && fy != null && fz != null) {
                        String sx = fx.load_Str().strip();
                        String sy = fy.load_Str().strip();
                        String sz = fz.load_Str().strip();
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

                  Identifier placeFont = this.init_CharEncoder.init_Identifier_l(l.read);
                  autismclient.gui.f.b.k.init(context, this.init_Font, "Item", placeFont, UiColorPalette.parse(), x, y + 2, false);
                  y += 13;
                  this.poll(context, x, y, w, mouseX, mouseY, delta);
                  y += 20;
                  y = this.init_I_GuiGraphicsExtract(context, x, y, w, "Name:any  Slot:exact  None:held");
                  y += 2;
                  this.init_V_GuiGraphicsExtract_2(context, placeFont, "place_waitForItem", "Wait for Item", x, y, w, mouseX, mouseY);
                  y += 18;
                  this.init_V_GuiGraphicsExtract_2(context, placeFont, "place_silentSwitch", "Silent Switch", x, y, w, mouseX, mouseY);
                  y += 18;
               }

               for (FieldDef field : this.init_ActionFieldSchema.fields()) {
                  if (field.type() != CachePool.STRING_LIST
                     && !this.flush_Z(field.key())
                     && this.write(field)
                     && (
                        this.init_MacroAction == null
                           || this.init_MacroAction.getType() != MacroActionType.PLACE
                           || !"itemName".equals(field.key()) && !"packetOrder".equals(field.key())
                     )) {
                     this.init_V_GuiGraphicsExtract_5(context, field, x, y, w, mouseX, mouseY, delta);
                     y += this.init_I_FieldDef(field) + 2;
                  }
               }

               for (FieldDef field : this.init_ActionFieldSchema.fields()) {
                  if (field.type() == CachePool.STRING_LIST
                     && !this.flush_Z(field.key())
                     && this.write(field)
                     && (
                        this.init_MacroAction == null
                           || this.init_MacroAction.getType() != MacroActionType.PLACE
                           || !"itemName".equals(field.key()) && !"packetOrder".equals(field.key())
                     )) {
                     this.init_V_GuiGraphicsExtract_5(context, field, x, y, w, mouseX, mouseY, delta);
                     y += this.init_I_FieldDef(field) + 2;
                  }
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PACKET) {
                  this.load_V(context, x, y, w, mouseX, mouseY);
                  y += 52;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SEND_PACKET) {
                  this.write_V(context, x, y, w, mouseX, mouseY);
                  y += 52;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DELAY_PACKETS) {
                  this.update_V(context, x, y, w, mouseX, mouseY);
               }
            } finally {
               HashUtil.init_HashUtil().init(context);
            }

            if (needsScroll) {
               int sbX = this.bind + this.f_b_I - 5 - 1;
               this.init_V_GuiGraphicsExtract(context, sbX, bodyTop, contentAreaH, totalContentH, contentAreaH, 1, this.f_e_I, mouseX, mouseY);
               int capSbY = bodyTop;
               int capSbH = contentAreaH;
               this.size_List.add(new ActionEditorOverlay.SyncManager(sbX, bodyTop, 5, contentAreaH, my -> {
                  int rel = Math.max(0, Math.min(capSbH, my - capSbY));
                  this.f_e_I = Math.max(0, Math.min(maxScroll, rel * maxScroll / Math.max(1, capSbH)));
               }));
            }
         }
      } else {
         int x = this.bind + 4;
         int w = this.f_b_I - 8;
         this.tick(context, x, bodyTop, w, mouseX, mouseY, delta);
      }

      this.load(context, bodyBtm - this.run_I(), mouseX, mouseY);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      int headerBtnW = this.write_I("Clear");
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Items / Exact Slots (" + this.init_MapStore.itemNames.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      boolean canClearAll = !this.init_MapStore.itemNames.isEmpty();
      int clearX = x + w - headerBtnW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, clearX, cy, headerBtnW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClearAll, mx, my, () -> {
         this.init_MapStore.itemNames.clear();
         this.init_MapStore.itemTimes.clear();
         this.init_MapStore.itemActionIdx.clear();
         this.init_MapStore.itemButtons.clear();
         this.init_MapStore.itemSearchScopes.clear();
         this.init_MapStore.stackAmountModes.clear();
         this.update_V_3();
         this.write_V_3();
      });
      cy += 14;
      int selAreaH = 56;
      int sbX = x + w - 5;
      EventBus itemViewport = this.init_EventBus_Map(this.tick_Map, "_item_entries", x, cy, w, selAreaH, 14, 5);
      itemViewport.init_V(this.init_MapStore.itemNames.size() * 14);
      itemViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      N[] ACTIONS = N.values();
      int sbGap = 7;
      int delW = 13;
      int itemW = w - sbGap - delW - 2;
      if (!this.init_MapStore.itemNames.isEmpty()) {
         itemViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstVis = itemViewport.pop();
         int iy0 = cy - itemViewport.open() % 14;

         for (int i = firstVis; i < this.init_MapStore.itemNames.size() && i <= itemViewport.tick(); i++) {
            int iy = itemViewport.init_I(i);
            if (iy != -2147483648) {
               String entry = this.init_MapStore.itemNames.get(i);
               int eiActIdx = this.init_MapStore.load_I(i);
               N eiAct = ACTIONS[eiActIdx];
               int eiBtn = this.init_MapStore.read_I(i);
               boolean selected = i == this.f_j_I;
               MacroItemTarget entryTarget = this.init_ArrayUtil_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, i);
               Component displayName = this.load_Component_ArrayUtil_Str(entryTarget, entry);
               boolean rowHovered = mx >= x && mx < x + itemW && my >= iy && my < iy + 13;
               String summaryAct = eiAct.load_Str;
               String var10000;
               if (eiAct == N.SWAP) {
                  var10000 = "H" + Math.max(1, Math.min(9, eiBtn + 1));
               } else if (eiAct == N.PICKUP_ALL) {
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
               int times = this.init_MapStore.init_I_I_3(i);
               String summary = " • " + summaryAct + (summaryBtn.isBlank() ? "" : " " + summaryBtn) + (times != 1 ? " ×" + times : "");
               Component rowLabel = Component.empty().append(displayName).append(Component.literal(summary).withStyle(s -> s.withColor(-7829368)));
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx, this.init_Font, rowLabel, autismclient.gui.f.NetHelper.init(x, iy, itemW, 13), rowHovered, selected, FileCache.Inner_DataUtil.NORMAL, true
               );
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int fi = i;
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.Inner_StreamUtil(
                           x, Math.max(cy, iy), itemW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.init_V_I(fi)
                        )
                     );
               }

               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int delX = x + itemW + 2;
                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                     this.init_MapStore.itemNames.remove(fi);
                     if (fi < this.init_MapStore.itemTargets.size()) {
                        this.init_MapStore.itemTargets.remove(fi);
                     }

                     if (fi < this.init_MapStore.itemTimes.size()) {
                        this.init_MapStore.itemTimes.remove(fi);
                     }

                     if (fi < this.init_MapStore.itemActionIdx.size()) {
                        this.init_MapStore.itemActionIdx.remove(fi);
                     }

                     if (fi < this.init_MapStore.itemButtons.size()) {
                        this.init_MapStore.itemButtons.remove(fi);
                     }

                     if (fi < this.init_MapStore.itemSearchScopes.size()) {
                        this.init_MapStore.itemSearchScopes.remove(fi);
                     }

                     if (fi < this.init_MapStore.stackAmountModes.size()) {
                        this.init_MapStore.stackAmountModes.remove(fi);
                     }

                     if (this.f_j_I == fi) {
                        this.update_V_3();
                     } else if (this.f_j_I > fi) {
                        this.f_j_I--;
                     }

                     this.write_V_3();
                     EventBus vp = this.tick_Map.get("_item_entries");
                     if (vp != null) {
                        vp.read_V(-1);
                     }
                  });
               }
            }
         }

         itemViewport.init(ctx);
      } else {
         FileCache.init(ctx, this.init_Font, "No rows yet. Add an item or exact slot.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      int addPickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
      String addLbl = this.f_j_I >= 0 ? "New" : "+Add";
      int addBtnW = this.write_I(addLbl);
      int slotW = 26;
      ThemedTextInputBox addF = this.flush_Map.get("_item_add");
      ThemedTextInputBox entrySlotF = this.init_Map.get("item_entrySlot");
      if (addF != null && entrySlotF != null) {
         int pickX = x + w - addBtnW - 3 - addPickW;
         int plusX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         addF.read(x);
         addF.write(cy);
         addF.update(slotX - x - 2);
         addF.init_V(ctx, mx, my, delta);
         entrySlotF.read(slotX);
         entrySlotF.write(cy);
         entrySlotF.update(slotW);
         entrySlotF.init_V(ctx, mx, my, delta);
         boolean capturing = this.init_StreamUtil.init("_item_entries");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, cy, addPickW, 14, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("_item_entries"));
         this.init_ByteReader_GuiGraphicsExtract_2(ctx, plusX, cy, addBtnW, 13, addLbl, ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
            if (this.f_j_I >= 0) {
               this.update_V_3();
            } else {
               this.flush_V_2();
            }
         });
      }

      cy += 16;
      cy += 4;
      if (this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
         int eiActIdx = this.init_MapStore.load_I(this.f_j_I);
         N eiAct = ACTIONS[eiActIdx];
         int eiBtn = this.init_MapStore.read_I(this.f_j_I);
         boolean btnActive = eiAct == N.PICKUP || eiAct == N.QUICK_MOVE || eiAct == N.SWAP;
         int editIdx = this.f_j_I;
         int editTimesW = 28;
         int editGap = 3;
         List<String> actOptions = new ArrayList<>();

         for (N a : ItemAction.init_List_4()) {
            actOptions.add(a.load_Str);
         }

         String var81;
         if (eiAct == N.SWAP) {
            var81 = "H" + Math.max(1, Math.min(9, eiBtn + 1));
         } else if (eiAct == N.PICKUP_ALL) {
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
         boolean btnIsDropdown = this.init_MapStore.update_I(editIdx) > 2;
         List<String> btnOptions = btnIsDropdown ? this.init_MapStore.init_List_I(editIdx) : List.of();
         int editBtnW = btnIsDropdown ? this.init_I_List_I(btnOptions, w) : this.write_I(btnLbl);
         int editActW = this.init_I_List_I(actOptions, w - editGap - editBtnW - editGap - editTimesW);
         this.init_V_Str(
            "item_action", actOptions, this.init_MapStore.write_I(editIdx), x, cy, editActW, 13, idx -> this.init_MapStore.init_V_I_I(editIdx, idx)
         );
         if (btnIsDropdown) {
            this.init_V_Str(
               "item_button",
               btnOptions,
               this.init_MapStore.read_I(editIdx),
               x + editActW + editGap,
               cy,
               editBtnW,
               13,
               idx -> this.init_MapStore.load_V_I_I(editIdx, idx)
            );
         } else {
            this.init_ByteReader_GuiGraphicsExtract(
               ctx,
               x + editActW + editGap,
               cy,
               editBtnW,
               13,
               btnLbl,
               eiAct == N.SWAP ? ByteReader.Inner_StreamUtil.PRIMARY : ByteReader.Inner_StreamUtil.GHOST,
               btnActive,
               mx,
               my,
               () -> this.init_MapStore.read_V(editIdx),
               () -> this.init_MapStore.write_V(editIdx)
            );
         }

         ThemedTextInputBox tf = this.init_Map.get("item_times_" + this.f_j_I);
         if (tf != null) {
            tf.read(x + editActW + editGap + editBtnW + editGap);
            tf.write(cy);
            tf.update(editTimesW);
            tf.init_V(ctx, mx, my, delta);
         }

         cy += 16;
         if (this.reset_Z()) {
            this.init_V_GuiGraphicsExtract_2(ctx, x, cy, w, mx, my);
            cy += 16;
         }
      }

      this.load(ctx, font, "item_waitForGuiBefore", "item_waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.load_Map.getOrDefault("item_waitForGuiBefore", false) && !this.load_Map.getOrDefault("item_waitForGuiAfter", false)) {
         ThemedTextInputBox guiF = this.init_Map.get("item_guiName");
         if (guiF != null) {
            guiF.read(-1000);
         }
      } else {
         ThemedTextInputBox guiF = this.init_Map.get("item_guiName");
         if (guiF != null) {
            int lw = this.init_I_I(w, "GUI Name", font);
            this.init_V_GuiGraphicsExtract_3(ctx, "GUI Name", x, cy, lw, font);
            guiF.read(this.flush_I(x, lw));
            guiF.write(cy + 2);
            guiF.update(this.close(w, lw));
            guiF.init_V(ctx, mx, my, delta);
         }

         cy += 16;
      }

      this.init_V_GuiGraphicsExtract_2(ctx, font, "item_waitForItem", "Wait for Item", x, cy, w, mx, my);
      cy += 16;
      this.init_V_GuiGraphicsExtract_2(ctx, font, "item_useClickDelay", "Delay Clicks", x, cy, w, mx, my);
      cy += 16;
      if (this.load_Map.getOrDefault("item_useClickDelay", false)) {
         ThemedTextInputBox delayF = this.init_Map.get("item_clickDelayMs");
         if (delayF != null) {
            int lw = this.init_I_I(w, "Delay (ms)", font);
            this.init_V_GuiGraphicsExtract_3(ctx, "Delay (ms)", x, cy, lw, font);
            delayF.read(this.flush_I(x, lw));
            delayF.write(cy + 2);
            delayF.update(this.close(w, lw));
            delayF.init_V(ctx, mx, my, delta);
         }

         cy += 16;
      } else {
         ThemedTextInputBox delayF = this.init_Map.get("item_clickDelayMs");
         if (delayF != null) {
            delayF.read(-1000);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      if (this.init_MapStore != null && this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
         int gap = 4;
         ItemAction.Inner_DataUtil scope = this.init_MapStore.init_MapStore_DataUtil_I(this.f_j_I);
         ItemAction.Inner_DataUtil[] scopes = ItemAction.Inner_DataUtil.values();
         List<String> scopeOptions = new ArrayList<>();

         for (ItemAction.Inner_DataUtil s : scopes) {
            scopeOptions.add(s.init_Str);
         }

         int invW = this.init_I_List_I(scopeOptions, Math.max(68, (w - gap) / 2));
         this.init_V_Str(
            "item_scope",
            scopeOptions,
            scope.ordinal(),
            x,
            y + 2,
            invW,
            13,
            idx -> this.init_MapStore.init_V_I_MapStore_DataUtil(this.f_j_I, scopes[Math.max(0, Math.min(idx, scopes.length - 1))])
         );
         ItemAction.Inner_CachePool mode = this.init_MapStore.init_MapStore_CachePool_I(this.f_j_I);
         ItemAction.Inner_CachePool[] stackModes = ItemAction.Inner_CachePool.values();
         List<String> stackOptions = new ArrayList<>();

         for (ItemAction.Inner_CachePool m : stackModes) {
            stackOptions.add("Stack: " + m.init_Str);
         }

         int stackW = this.init_I_List_I(stackOptions, Math.max(72, w - invW - gap));
         int stackX = x + invW + gap;
         this.init_V_Str("item_stack", stackOptions, mode.ordinal(), stackX, y + 2, stackW, 13, idx -> {
            while (this.init_MapStore.stackAmountModes.size() <= this.f_j_I) {
               this.init_MapStore.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
            }

            this.init_MapStore.stackAmountModes.set(this.f_j_I, stackModes[Math.max(0, Math.min(idx, stackModes.length - 1))].ordinal());
         });
      }
   }

   private boolean reset_Z() {
      if (this.init_MapStore != null && this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
         MacroItemTarget target = this.init_ArrayUtil_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, this.f_j_I);
         return target.update() && !target.init_Z()
            ? this.init_MapStore.init_N_I(this.f_j_I) != N.PICKUP_ALL
               || !this.load_Map.getOrDefault("item_useCursorItemForPickupAll", this.init_MapStore.useCursorItemForPickupAll)
            : false;
      } else {
         return false;
      }
   }

   private void init_V_ArrayUtil(MacroItemTarget target) {
      this.init_V_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, target);

      while (this.init_MapStore.itemTimes.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemTimes.add(1);
      }

      while (this.init_MapStore.itemActionIdx.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemActionIdx.add(0);
      }

      while (this.init_MapStore.itemButtons.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemButtons.add(0);
      }

      while (this.init_MapStore.itemSearchScopes.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemSearchScopes.add(0);
      }

      while (this.init_MapStore.stackAmountModes.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.stackAmountModes.add(0);
      }

      int idx = this.init_MapStore.itemNames.size() - 1;
      ThemedTextInputBox f = this.init_k_I(28);
      f.poll(true);
      f.init_V_Str("1");
      this.init_Map.put("item_times_" + idx, f);
   }

   private void write_V_3() {
      this.init_Map.keySet().removeIf(k -> k.startsWith("item_times_"));
      this.init_V_List_I(this.init_MapStore.itemTargets, this.init_MapStore.itemNames.size());

      while (this.init_MapStore.itemTimes.size() > this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemTimes.remove(this.init_MapStore.itemTimes.size() - 1);
      }

      while (this.init_MapStore.itemActionIdx.size() > this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemActionIdx.remove(this.init_MapStore.itemActionIdx.size() - 1);
      }

      while (this.init_MapStore.itemButtons.size() > this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemButtons.remove(this.init_MapStore.itemButtons.size() - 1);
      }

      while (this.init_MapStore.itemSearchScopes.size() > this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemSearchScopes.remove(this.init_MapStore.itemSearchScopes.size() - 1);
      }

      while (this.init_MapStore.stackAmountModes.size() > this.init_MapStore.itemNames.size()) {
         this.init_MapStore.stackAmountModes.remove(this.init_MapStore.stackAmountModes.size() - 1);
      }

      while (this.init_MapStore.itemSearchScopes.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.itemSearchScopes.add(0);
      }

      while (this.init_MapStore.stackAmountModes.size() < this.init_MapStore.itemNames.size()) {
         this.init_MapStore.stackAmountModes.add(0);
      }

      for (int i = 0; i < this.init_MapStore.itemNames.size(); i++) {
         ThemedTextInputBox f = this.init_k_I(28);
         f.poll(true);
         f.init_V_Str(String.valueOf(this.init_MapStore.init_I_I_3(i)));
         this.init_Map.put("item_times_" + i, f);
      }
   }

   private void init_V_I(int index) {
      if (this.f_j_I == index) {
         this.update_V_3();
      } else {
         this.f_j_I = index;
         this.sync_V_2();
      }
   }

   private void update_V_3() {
      this.f_j_I = -1;
      ThemedTextInputBox addF = this.flush_Map.get("_item_add");
      this.stop_Z = true;
      if (addF != null) {
         addF.init_V_Str("");
      }

      ThemedTextInputBox slotF = this.init_Map.get("item_entrySlot");
      if (slotF != null) {
         slotF.init_V_Str("");
      }

      this.stop_Z = false;
   }

   private void sync_V_2() {
      ThemedTextInputBox addF = this.flush_Map.get("_item_add");
      ThemedTextInputBox slotF = this.init_Map.get("item_entrySlot");
      if (addF != null && slotF != null) {
         this.stop_Z = true;
         if (this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
            MacroItemTarget target = this.init_ArrayUtil_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, this.f_j_I);
            addF.init_V_Str(target.init_Str());
            slotF.init_V_Str(target.init_Z() ? String.valueOf(target.init_I) : "");
            this.stop_Z = false;
         } else {
            addF.init_V_Str("");
            slotF.init_V_Str("");
            this.stop_Z = false;
         }
      }
   }

   private void flush_V_2() {
      this.flush(false);
   }

   private void close_V_2() {
      if (!this.stop_Z && this.init_MapStore != null && this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
         this.flush(true);
      }
   }

   private void flush(boolean preserveSelection) {
      if (this.init_MapStore != null) {
         ThemedTextInputBox addF = this.flush_Map.get("_item_add");
         ThemedTextInputBox slotF = this.init_Map.get("item_entrySlot");
         if (addF != null && slotF != null) {
            String nameText = addF.load_Str().strip();
            String slotText = slotF.load_Str().strip();
            MacroItemTarget target = this.init_ArrayUtil_Str_2(
               nameText, slotText, this.init_ArrayUtil_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, this.f_j_I)
            );
            String entry = target.write_Str();
            if (entry != null && !entry.isBlank()) {
               if (this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
                  if (!this.init_Z_List_2(this.init_MapStore.itemNames, entry, this.f_j_I)) {
                     this.init_V_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, this.f_j_I, target);
                  }
               } else if (!this.init_MapStore.itemNames.contains(entry)) {
                  this.init_V_ArrayUtil(target);
               }

               if (!preserveSelection) {
                  this.update_V_3();
               }
            }
         }
      }
   }

   private void load_V_ArrayUtil(MacroItemTarget rawTarget) {
      if (this.init_MapStore != null) {
         MacroItemTarget target = load_ArrayUtil_ArrayUtil(rawTarget);
         String entry = target == null ? "" : target.write_Str();
         if (!entry.isBlank()) {
            int targetIndex = this.f_j_I;
            if (targetIndex < 0 || targetIndex >= this.init_MapStore.itemNames.size()) {
               targetIndex = this.init_MapStore.itemNames.indexOf(entry);
               if (targetIndex < 0) {
                  this.init_V_ArrayUtil(target.init_ArrayUtil());
                  targetIndex = this.init_MapStore.itemNames.size() - 1;
               }
            } else if (!this.init_Z_List_2(this.init_MapStore.itemNames, entry, targetIndex)) {
               this.init_V_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, targetIndex, target.init_ArrayUtil());
            }

            this.f_j_I = targetIndex;
            ThemedTextInputBox addF = this.flush_Map.get("_item_add");
            if (addF != null) {
               addF.init_V_Str(target.init_Str());
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, Identifier font, String stateKey, String label, int x, int y, int w, int mx, int my) {
      boolean val = this.load_Map.getOrDefault(stateKey, false);
      int lw = this.init_I_I(w, label, font, 34);
      this.init_V_GuiGraphicsExtract_3(ctx, label, x, y, lw, font);
      int btnW = 34;
      int btnH = 13;
      int btnX = x + w - btnW;
      this.init_ByteReader_GuiGraphicsExtract_3(
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
         () -> this.load_Map.put(stateKey, !this.load_Map.getOrDefault(stateKey, false))
      );
   }

   private void load(GuiGraphicsExtractor ctx, Identifier font, String beforeKey, String afterKey, int x, int y, int w, int mx, int my) {
      this.init_V_GuiGraphicsExtract_3(ctx, "GUI Wait", x, y, this.init_I_I(w, "GUI Wait", font, 90), font);
      int btnW = 42;
      int gap = 4;
      int afterX = x + w - btnW;
      int beforeX = afterX - gap - btnW;
      this.init_V_GuiGraphicsExtract_7(ctx, beforeKey, "Before", beforeX, y + 2, btnW, mx, my);
      this.init_V_GuiGraphicsExtract_7(ctx, afterKey, "After", afterX, y + 2, btnW, mx, my);
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor ctx, String key, String label, int x, int y, int w, int mx, int my) {
      boolean val = this.load_Map.getOrDefault(key, false);
      this.init_ByteReader_GuiGraphicsExtract_3(
         ctx, x, y, w, 14, label, val, "macro-gui-wait:" + key, mx, my, () -> this.load_Map.put(key, !this.load_Map.getOrDefault(key, false))
      );
   }

   private void load_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int removeW = 13;
      int maxTogW = 42;
      int amountW = 44;
      int headerBtnW = this.write_I("Clear");
      int cy = bodyTop + 4;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Craft Entries (" + this.push_List.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      boolean canClearEntries = !this.push_List.isEmpty();
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, x + w - headerBtnW, cy, headerBtnW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClearEntries, mx, my, () -> {
            this.push_List.clear();
            this.set_V_2();
            EventBus craftVp = this.tick_Map.get("_craft_entries");
            if (craftVp != null) {
               craftVp.load_V(0);
            }
         }
      );
      cy += 14;
      int selAreaH = 56;
      EventBus craftViewport = this.init_EventBus_Map(this.tick_Map, "_craft_entries", x, cy, w, selAreaH, 14, 5);
      craftViewport.init_V(this.push_List.size() * 14);
      craftViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      int sbGap = 7;
      if (!this.push_List.isEmpty()) {
         craftViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstSel = craftViewport.pop();

         for (int i = firstSel; i < this.push_List.size() && i <= craftViewport.tick(); i++) {
            int siy = craftViewport.init_I(i);
            if (siy != -2147483648) {
               autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.push_List.get(i);
               boolean useMax = this.load_Map.getOrDefault("craft_useMax_" + i, false);
               int removeX = x + w - sbGap - removeW;
               int fi = i;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, siy + 1, removeW, mx, my, () -> {
                  this.push_List.remove(fi);
                  this.set_V_2();
                  EventBus vp = this.tick_Map.get("_craft_entries");
                  if (vp != null) {
                     vp.read_V(-1);
                  }
               });
               int maxX = removeX - 3 - maxTogW;
               String mLbl = useMax ? "Max: On" : "Max: Off";
               int fii = i;
               this.init_ByteReader_GuiGraphicsExtract_3(
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
                  () -> this.load_Map.put("craft_useMax_" + fii, !this.load_Map.getOrDefault("craft_useMax_" + fii, false))
               );
               if (!useMax) {
                  int amtX = maxX - 3 - amountW;
                  ThemedTextInputBox af = this.init_Map.get("craft_amount_" + i);
                  if (af != null) {
                     af.read(amtX);
                     af.write(siy + 1);
                     af.update(amountW);
                     af.init_V(ctx, mx, my, delta);
                  }
               }

               int nameW = useMax ? maxX - 3 - x : maxX - 3 - amountW - 3 - x;
               boolean rowHovered = mx >= x && mx < x + nameW && my >= siy && my < siy + 13;
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  entry.init_Component(),
                  autismclient.gui.f.NetHelper.init(x, siy, Math.max(1, nameW), 13),
                  rowHovered,
                  false,
                  useMax ? FileCache.Inner_DataUtil.READY : FileCache.Inner_DataUtil.NORMAL,
                  true
               );
            }
         }

         craftViewport.init(ctx);
      } else {
         FileCache.init(ctx, this.init_Font, "No craft entries yet.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      int refreshW = this.write_I("Reload");
      ThemedTextInputBox srchF = this.flush_Map.get("_craft_search");
      if (srchF != null) {
         srchF.read(x);
         srchF.write(cy);
         srchF.update(w - refreshW - 3);
         srchF.init_V(ctx, mx, my, delta);
      }

      int rfX = x + w - refreshW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, rfX, cy, refreshW, 13, "Reload", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, this::open_V_2);
      cy += 16;
      String query = srchF != null ? srchF.load_Str() : "";
      if (!query.equals(this.update_Str)) {
         this.tick_List = J.init(this.pop_List != null ? this.pop_List : List.of(), query);
         this.update_Str = query;
         this.f_g_I = 0;
         if (this.init_J_TaskQueue != null && (this.tick_List == null || !this.tick_List.contains(this.init_J_TaskQueue))) {
            this.init_J_TaskQueue = null;
         }
      }

      List<J.Inner_TaskQueue> filtered = this.tick_List != null ? this.tick_List : List.of();
      int recipeListH = 52;
      EventBus recipeViewport = this.init_EventBus_Map(this.close_Map, "_craft_recipe_browser", x, cy, w, recipeListH, 13, 5);
      recipeViewport.init_V(filtered.size() * 13);
      this.init_IArr = new int[]{cy, recipeListH};
      recipeViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      int recW = w - 5 - 1;
      if (filtered.isEmpty()) {
         FileCache.init(
            ctx,
            this.init_Font,
            this.pop_List != null && !this.pop_List.isEmpty() ? "No recipes match the search." : "No crafting recipes loaded. Use Reload.",
            x,
            cy,
            recW
         );
      } else {
         recipeViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstRec = recipeViewport.pop();

         for (int i = firstRec; i < filtered.size() && i <= recipeViewport.tick(); i++) {
            int ry = recipeViewport.init_I(i);
            if (ry != -2147483648) {
               J.Inner_TaskQueue opt = filtered.get(i);
               boolean inList = this.push_List
                  .stream()
                  .anyMatch(e -> opt.init_Str != null && opt.init_Str.equals(e.init_Str) || opt.init_I >= 0 && opt.init_I == e.init_I);
               boolean hov = !inList && mx >= x && mx < x + recW && my >= ry && my < ry + 13;
               FileCache.Inner_DataUtil tone = inList
                  ? FileCache.Inner_DataUtil.READY
                  : (opt.read_Z ? FileCache.Inner_DataUtil.NORMAL : FileCache.Inner_DataUtil.MISSING);
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx, this.init_Font, opt.init_Component, autismclient.gui.f.NetHelper.init(x, ry, recW, 13), hov, inList, tone, true
               );
               if (opt.init_ItemStack != null && opt.init_ItemStack.getCount() > 1) {
                  String cnt = "×" + opt.init_ItemStack.getCount();
                  autismclient.gui.f.b.k.init(ctx, this.init_Font, cnt, font, UiColorPalette.bind(), x + recW - this.init_I_Identifier_Str(font, cnt) - 2, ry + 2, false);
               }

               J.Inner_TaskQueue fOpt = opt;
               boolean fInList = inList;
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, ry, recW, 13, () -> {
                  if (fInList) {
                     this.push_List.removeIf(e -> fOpt.init_Str != null && fOpt.init_Str.equals(e.init_Str) || fOpt.init_I >= 0 && fOpt.init_I == e.init_I);
                     this.set_V_2();
                  } else {
                     autismclient.util.macro.CraftAction.Inner_DataUtil newEntry = autismclient.util.macro.CraftAction.Inner_DataUtil.init(fOpt, 1, false);
                     this.push_List.add(newEntry);
                     int idx = this.push_List.size() - 1;
                     ThemedTextInputBox f = this.init_k_I(44);
                     f.poll(true);
                     f.init_V_Str("1");
                     this.init_Map.put("craft_amount_" + idx, f);
                     this.load_Map.put("craft_useMax_" + idx, false);
                  }
               }));
            }
         }

         recipeViewport.init(ctx);
      }
   }

   private void open_V_2() {
      this.pop_List = J.init_List(init_Minecraft);
      this.update_Str = null;
      this.init_J_TaskQueue = null;
      this.f_g_I = 0;
   }

   private void poll_V_2() {
      if (this.init_J_TaskQueue != null) {
         ThemedTextInputBox amtF = this.init_Map.get("_craft_amount");
         int amount = 1;
         if (amtF != null && !this.tick_Z) {
            try {
               amount = Math.max(1, Integer.parseInt(amtF.load_Str().strip()));
            } catch (NumberFormatException var7) {
            }
         }

         autismclient.util.macro.CraftAction.Inner_DataUtil newEntry = autismclient.util.macro.CraftAction.Inner_DataUtil.init(
            this.init_J_TaskQueue, amount, this.tick_Z
         );

         for (int i = 0; i < this.push_List.size(); i++) {
            autismclient.util.macro.CraftAction.Inner_DataUtil existing = this.push_List.get(i);
            if (newEntry.init_Str != null && newEntry.init_Str.equals(existing.init_Str) || newEntry.init_I >= 0 && newEntry.init_I == existing.init_I) {
               existing.read_I = newEntry.read_I;
               existing.init_Z = newEntry.init_Z;
               this.load_Map.put("craft_useMax_" + i, newEntry.init_Z);
               ThemedTextInputBox f = this.init_Map.get("craft_amount_" + i);
               if (f != null) {
                  f.init_V_Str(String.valueOf(newEntry.read_I));
               }

               return;
            }
         }

         this.push_List.add(newEntry);
         int idx = this.push_List.size() - 1;
         ThemedTextInputBox f = this.init_k_I(44);
         f.poll(true);
         f.init_V_Str(String.valueOf(newEntry.read_I));
         this.init_Map.put("craft_amount_" + idx, f);
         this.load_Map.put("craft_useMax_" + idx, newEntry.init_Z);
      }
   }

   private void set_V_2() {
      this.init_Map.keySet().removeIf(k -> k.startsWith("craft_amount_"));

      for (int i = 0; i < this.push_List.size(); i++) {
         autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.push_List.get(i);
         ThemedTextInputBox f = this.init_k_I(44);
         f.poll(true);
         f.init_V_Str(String.valueOf(entry.read_I));
         this.init_Map.put("craft_amount_" + i, f);
         if (!this.load_Map.containsKey("craft_useMax_" + i)) {
            this.load_Map.put("craft_useMax_" + i, entry.init_Z);
         }
      }

      this.load_Map.keySet().removeIf(k -> {
         if (!k.startsWith("craft_useMax_")) {
            return false;
         }

         try {
            return Integer.parseInt(k.substring("craft_useMax_".length())) >= this.push_List.size();
         } catch (NumberFormatException e) {
            return true;
         }
      });
   }

   private void read_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      int headerBtnW = this.write_I("Clear");
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Drop Entries (" + this.init_PacketUtil.itemNames.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      boolean canClearAll = !this.init_PacketUtil.itemNames.isEmpty();
      int clearX = x + w - headerBtnW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, clearX, cy, headerBtnW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClearAll, mx, my, () -> {
         this.init_PacketUtil.itemNames.clear();
         this.init_PacketUtil.itemTargets.clear();
         this.init_PacketUtil.itemCounts.clear();
         this.add_V_2();
         this.put_V();
      });
      cy += 14;
      int selAreaH = 56;
      EventBus dropViewport = this.init_EventBus_Map(this.tick_Map, "_drop_entries", x, cy, w, selAreaH, 14, 5);
      dropViewport.init_V(this.init_PacketUtil.itemNames.size() * 14);
      dropViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      int sbGap = 7;
      int delW = 13;
      int dropItemW = w - sbGap - delW - 2;
      if (!this.init_PacketUtil.itemNames.isEmpty()) {
         dropViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstVis = dropViewport.pop();

         for (int i = firstVis; i < this.init_PacketUtil.itemNames.size() && i <= dropViewport.tick(); i++) {
            int iy = dropViewport.init_I(i);
            if (iy != -2147483648) {
               String entry = this.init_PacketUtil.itemNames.get(i);
               boolean selected = i == this.f_k_I;
               MacroItemTarget entryTarget = this.init_ArrayUtil_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, i);
               Component displayName = this.load_Component_ArrayUtil_Str(entryTarget, entry);
               int cnt = i < this.init_PacketUtil.itemCounts.size() ? this.init_PacketUtil.itemCounts.get(i) : 0;
               String summary = " • " + (cnt == 0 ? "all" : "×" + cnt);
               Component rowLabel = Component.empty().append(displayName).append(Component.literal(summary).withStyle(s -> s.withColor(-7829368)));
               boolean rowHovered = mx >= x && mx < x + dropItemW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  rowLabel,
                  autismclient.gui.f.NetHelper.init(x, iy, dropItemW, 13),
                  rowHovered,
                  selected,
                  FileCache.Inner_DataUtil.NORMAL,
                  true
               );
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int fi = i;
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.Inner_StreamUtil(
                           x, Math.max(cy, iy), dropItemW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.load_V_I(fi)
                        )
                     );
               }

               if (iy + 13 > cy && iy < cy + selAreaH) {
                  int delX = x + dropItemW + 2;
                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                     this.init_PacketUtil.itemNames.remove(fi);
                     if (fi < this.init_PacketUtil.itemTargets.size()) {
                        this.init_PacketUtil.itemTargets.remove(fi);
                     }

                     if (fi < this.init_PacketUtil.itemCounts.size()) {
                        this.init_PacketUtil.itemCounts.remove(fi);
                     }

                     if (this.f_k_I == fi) {
                        this.add_V_2();
                     } else if (this.f_k_I > fi) {
                        this.f_k_I--;
                     }

                     this.put_V();
                     EventBus vp = this.tick_Map.get("_drop_entries");
                     if (vp != null) {
                        vp.read_V(-1);
                     }
                  });
               }
            }
         }

         dropViewport.init(ctx);
      } else {
         FileCache.init(ctx, this.init_Font, "Optional: add items/slots, or use the fallback below.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      int addPickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
      String addLbl = this.f_k_I >= 0 ? "New" : "+Add";
      int addBtnW = this.write_I(addLbl);
      int slotW = 44;
      ThemedTextInputBox addF = this.flush_Map.get("_drop_add");
      ThemedTextInputBox entrySlotF = this.init_Map.get("drop_entrySlot");
      if (addF != null && entrySlotF != null) {
         int pickX = x + w - addBtnW - 3 - addPickW;
         int plusX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         addF.read(x);
         addF.write(cy);
         addF.update(slotX - x - 2);
         addF.init_V(ctx, mx, my, delta);
         entrySlotF.read(slotX);
         entrySlotF.write(cy);
         entrySlotF.update(slotW);
         entrySlotF.init_V(ctx, mx, my, delta);
         boolean capturing = this.init_StreamUtil.init("_drop_entries");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, cy, addPickW, 14, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("_drop_entries"));
         this.init_ByteReader_GuiGraphicsExtract_2(ctx, plusX, cy, addBtnW, 13, addLbl, ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
            if (this.f_k_I >= 0) {
               this.add_V_2();
            } else {
               this.reset_V();
            }
         });
      }

      cy += 16;
      cy += 4;
      boolean editingSelectedDrop = this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size();
      boolean noDropTargets = this.init_PacketUtil.itemNames.isEmpty();
      String dropHint = editingSelectedDrop
         ? "Editing this row. 0 means drop the full stack from that slot or item."
         : (
            noDropTargets
               ? (
                  this.init_PacketUtil.mode == DropAction.Inner_DataUtil.ALL
                     ? "No items selected: Drop All drops the entire main inventory and hotbar."
                     : "No items selected: Times drops the currently held item by the count below."
               )
               : "No row selected. These controls set defaults for newly added rows."
         );
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, dropHint);
      boolean dropAllSelected = editingSelectedDrop ? this.init_I_I_2(this.f_k_I) == 0 : this.init_PacketUtil.mode == DropAction.Inner_DataUtil.ALL;
      String modeLabel = editingSelectedDrop ? "Selected Row" : (noDropTargets ? "No Items" : "Default Mode");
      int lw = this.init_I_I(w, modeLabel, font);
      this.init_V_GuiGraphicsExtract_3(ctx, modeLabel, x, cy, lw, font);
      int ctrlX = this.flush_I(x, lw);
      int ctrlW = this.close(w, lw);
      int[] modeBtns = this.init_IArr_I(ctrlX, ctrlW, false, "Drop All", "Times");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx,
         modeBtns[0],
         cy + 2,
         modeBtns[1],
         13,
         "Drop All",
         dropAllSelected ? ByteReader.Inner_StreamUtil.PRIMARY : ByteReader.Inner_StreamUtil.GHOST,
         true,
         mx,
         my,
         () -> {
            if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
               this.read_V_I_I(this.f_k_I, 0);
            } else {
               this.read_Map.put("drop_mode", 0);
               this.init_PacketUtil.mode = DropAction.Inner_DataUtil.ALL;
            }

            this.next_V();
         }
      );
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx,
         modeBtns[2],
         cy + 2,
         modeBtns[3],
         13,
         "Times",
         !dropAllSelected ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.GHOST,
         true,
         mx,
         my,
         () -> {
            if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
               this.read_V_I_I(this.f_k_I, Math.max(1, this.init_I_I_2(this.f_k_I)));
            } else {
               this.read_Map.put("drop_mode", 1);
               this.init_PacketUtil.mode = DropAction.Inner_DataUtil.TIMES;
            }

            this.next_V();
         }
      );
      cy += 16;
      ThemedTextInputBox cntF = this.init_Map.get("drop_globalCount");
      this.next_V();
      if (cntF != null) {
         modeLabel = editingSelectedDrop ? "Selected Count" : (noDropTargets ? "Held Count" : "Default Count");
         lw = this.init_I_I(w, modeLabel, font);
         this.init_V_GuiGraphicsExtract_3(ctx, modeLabel, x, cy, lw, font);
         cntF.read(this.flush_I(x, lw));
         cntF.write(cy + 2);
         cntF.update(this.close(w, lw));
         cntF.init_V(ctx, mx, my, delta);
         cy += 16;
      }

      this.load(ctx, font, "drop_waitForGuiBefore", "drop_waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.load_Map.getOrDefault("drop_waitForGuiBefore", false) && !this.load_Map.getOrDefault("drop_waitForGuiAfter", false)) {
         ThemedTextInputBox guiF = this.init_Map.get("drop_guiName");
         if (guiF != null) {
            guiF.read(-1000);
         }
      } else {
         ThemedTextInputBox guiF = this.init_Map.get("drop_guiName");
         if (guiF != null) {
            lw = this.init_I_I(w, "GUI Name", font);
            this.init_V_GuiGraphicsExtract_3(ctx, "GUI Name", x, cy, lw, font);
            guiF.read(this.flush_I(x, lw));
            guiF.write(cy + 2);
            guiF.update(this.close(w, lw));
            guiF.init_V(ctx, mx, my, delta);
         }

         cy += 16;
      }
   }

   private void init_V_ArrayUtil_I(MacroItemTarget target, int count) {
      if (target != null) {
         String entry = target.write_Str();
         if (!entry.isEmpty()) {
            this.init_V_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, target);
            int safeCount = Math.max(0, count);
            this.init_PacketUtil.itemCounts.add(safeCount);
            int idx = this.init_PacketUtil.itemNames.size() - 1;
            ThemedTextInputBox f = this.init_k_I(32);
            f.poll(true);
            f.init_V_Str(String.valueOf(safeCount));
            int fieldIndex = idx;
            f.init_V_Consumer(text -> {
               try {
                  this.read_V_I_I(fieldIndex, Math.max(0, Integer.parseInt(text.strip())));
                  if (fieldIndex == this.f_k_I) {
                     this.next_V();
                  }
               } catch (NumberFormatException var4x) {
               }
            });
            this.init_Map.put("drop_count_" + idx, f);
         }
      }
   }

   private void put_V() {
      this.init_Map.keySet().removeIf(k -> k.startsWith("drop_count_"));
      this.init_V_List_I(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames.size());

      while (this.init_PacketUtil.itemCounts.size() > this.init_PacketUtil.itemNames.size()) {
         this.init_PacketUtil.itemCounts.remove(this.init_PacketUtil.itemCounts.size() - 1);
      }

      for (int i = 0; i < this.init_PacketUtil.itemNames.size(); i++) {
         ThemedTextInputBox f = this.init_k_I(32);
         f.poll(true);
         f.init_V_Str(String.valueOf(this.init_PacketUtil.itemCounts.get(i)));
         int fieldIndex = i;
         f.init_V_Consumer(text -> {
            try {
               this.read_V_I_I(fieldIndex, Math.max(0, Integer.parseInt(text.strip())));
               if (fieldIndex == this.f_k_I) {
                  this.next_V();
               }
            } catch (NumberFormatException var4) {
            }
         });
         this.init_Map.put("drop_count_" + i, f);
      }
   }

   private void load_V_I(int index) {
      if (this.f_k_I == index) {
         this.add_V_2();
      } else {
         this.f_k_I = index;
         this.clear_V();
         this.next_V();
      }
   }

   private void add_V_2() {
      this.f_k_I = -1;
      ThemedTextInputBox addF = this.flush_Map.get("_drop_add");
      this.next_Z = true;
      if (addF != null) {
         addF.init_V_Str("");
      }

      ThemedTextInputBox slotF = this.init_Map.get("drop_entrySlot");
      if (slotF != null) {
         slotF.init_V_Str("");
      }

      this.next_Z = false;
      this.next_V();
   }

   private void clear_V() {
      ThemedTextInputBox addF = this.flush_Map.get("_drop_add");
      ThemedTextInputBox slotF = this.init_Map.get("drop_entrySlot");
      if (addF != null && slotF != null) {
         this.next_Z = true;
         if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
            MacroItemTarget target = this.init_ArrayUtil_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, this.f_k_I);
            addF.init_V_Str(target.init_Str());
            slotF.init_V_Str(target.init_Z() ? String.valueOf(target.init_I) : "");
            this.next_Z = false;
            this.next_V();
         } else {
            addF.init_V_Str("");
            slotF.init_V_Str("");
            this.next_Z = false;
         }
      }
   }

   private void reset_V() {
      this.close(false);
   }

   private void stop_V() {
      if (!this.next_Z && this.init_PacketUtil != null && this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
         this.close(true);
      }
   }

   private void close(boolean preserveSelection) {
      if (this.init_PacketUtil != null) {
         ThemedTextInputBox addF = this.flush_Map.get("_drop_add");
         ThemedTextInputBox slotF = this.init_Map.get("drop_entrySlot");
         if (addF != null && slotF != null) {
            String nameText = addF.load_Str().strip();
            String slotText = slotF.load_Str().strip();
            MacroItemTarget target = this.init_ArrayUtil_Str_2(
               nameText, slotText, this.init_ArrayUtil_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, this.f_k_I)
            );
            String entry = target.write_Str();
            if (!entry.isBlank()) {
               if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
                  if (!this.init_Z_List_2(this.init_PacketUtil.itemNames, entry, this.f_k_I)) {
                     this.init_V_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, this.f_k_I, target);
                  }
               } else if (!this.init_PacketUtil.itemNames.contains(entry)) {
                  this.init_V_ArrayUtil_I(target, this.pop_I_2());
               }

               if (!preserveSelection) {
                  this.add_V_2();
               }
            }
         }
      }
   }

   private void read(MacroItemTarget rawTarget) {
      if (this.init_PacketUtil != null) {
         MacroItemTarget target = load_ArrayUtil_ArrayUtil(rawTarget);
         String entry = target == null ? "" : target.write_Str();
         if (!entry.isBlank()) {
            int targetIndex = this.f_k_I;
            if (targetIndex < 0 || targetIndex >= this.init_PacketUtil.itemNames.size()) {
               targetIndex = this.init_PacketUtil.itemNames.indexOf(entry);
               if (targetIndex < 0) {
                  this.init_V_ArrayUtil_I(target.init_ArrayUtil(), this.pop_I_2());
                  targetIndex = this.init_PacketUtil.itemNames.size() - 1;
               }
            } else if (!this.init_Z_List_2(this.init_PacketUtil.itemNames, entry, targetIndex)) {
               this.init_V_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, targetIndex, target.init_ArrayUtil());
            }

            this.f_k_I = targetIndex;
            this.clear_V();
            this.next_V();
         }
      }
   }

   private int pop_I_2() {
      ThemedTextInputBox globalCountField = this.init_Map.get("drop_globalCount");
      if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemCounts.size()) {
         return Math.max(0, this.init_PacketUtil.itemCounts.get(this.f_k_I));
      }

      if (this.read_Map.getOrDefault("drop_mode", 0) == 0) {
         return 0;
      }

      if (globalCountField != null) {
         try {
            return Math.max(0, Integer.parseInt(globalCountField.load_Str().strip()));
         } catch (NumberFormatException var3) {
         }
      }

      return Math.max(0, this.init_PacketUtil.dropCount);
   }

   private int init_I_I_2(int index) {
      if (this.init_PacketUtil != null && index >= 0 && index < this.init_PacketUtil.itemNames.size()) {
         while (this.init_PacketUtil.itemCounts.size() <= index) {
            this.init_PacketUtil.itemCounts.add(1);
         }

         return Math.max(0, this.init_PacketUtil.itemCounts.get(index));
      } else {
         return 1;
      }
   }

   private void read_V_I_I(int index, int count) {
      if (this.init_PacketUtil != null && index >= 0 && index < this.init_PacketUtil.itemNames.size()) {
         while (this.init_PacketUtil.itemCounts.size() <= index) {
            this.init_PacketUtil.itemCounts.add(1);
         }

         int safeCount = Math.max(0, count);
         this.init_PacketUtil.itemCounts.set(index, safeCount);
         ThemedTextInputBox rowField = this.init_Map.get("drop_count_" + index);
         if (rowField != null) {
            rowField.init_V_Str(String.valueOf(safeCount));
         }
      }
   }

   private void next_V() {
      if (this.init_PacketUtil != null) {
         ThemedTextInputBox countField = this.init_Map.get("drop_globalCount");
         if (countField != null) {
            boolean editingSelectedDrop = this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size();
            boolean dropAll = editingSelectedDrop ? this.init_I_I_2(this.f_k_I) == 0 : this.init_PacketUtil.mode == DropAction.Inner_DataUtil.ALL;
            int displayCount = editingSelectedDrop ? Math.max(1, this.init_I_I_2(this.f_k_I)) : Math.max(1, this.init_PacketUtil.dropCount);
            this.valid_Z = true;
            countField.init_V_Str(String.valueOf(displayCount));
            countField.open(!dropAll);
            this.valid_Z = false;
         }
      }
   }

   private void prev_V() {
      if (!this.valid_Z && this.init_PacketUtil != null) {
         ThemedTextInputBox countField = this.init_Map.get("drop_globalCount");
         if (countField != null) {
            boolean editingSelectedDrop = this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size();
            boolean dropAll = editingSelectedDrop ? this.init_I_I_2(this.f_k_I) == 0 : this.init_PacketUtil.mode == DropAction.Inner_DataUtil.ALL;
            if (!dropAll) {
               try {
                  int value = Math.max(1, Integer.parseInt(countField.load_Str().strip()));
                  if (editingSelectedDrop) {
                     this.read_V_I_I(this.f_k_I, value);
                  } else {
                     this.init_PacketUtil.dropCount = value;
                  }
               } catch (NumberFormatException var5) {
               }
            }
         }
      }
   }

   private void write_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      boolean filterByUser = this.load_Map.getOrDefault("lan_filterByUser", false);
      this.init_V_GuiGraphicsExtract_2(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 16;
      this.init_V_GuiGraphicsExtract_2(ctx, font, "lan_filterByUser", "Specific Peers", x, cy, w, mx, my);
      cy += 16;
      ThemedTextInputBox dsF = this.init_Map.get("lan_defaultStep");
      if (dsF != null && (!filterByUser || this.clear_List.isEmpty())) {
         int lw = this.init_I_I(w, filterByUser ? "Fallback Step" : "Any Peer Step", font);
         this.init_V_GuiGraphicsExtract_3(ctx, filterByUser ? "Fallback Step" : "Any Peer Step", x, cy, lw, font);
         dsF.read(this.flush_I(x, lw));
         dsF.write(cy + 2);
         dsF.update(this.close(w, lw));
         dsF.init_V(ctx, mx, my, delta);
         cy += 16;
      }

      String summary = !filterByUser
         ? "Continue when any LAN peer reaches the target step."
         : (
            this.clear_List.isEmpty()
               ? "Add peers below to narrow it down. Until then, it waits for any peer at the fallback step."
               : "Each peer below must reach its step before this continues."
         );
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, summary);
      if (filterByUser) {
         if (!this.clear_List.isEmpty()) {
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "Peers (" + this.clear_List.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
            cy += 13;
         }

         boolean hasEntries = !this.clear_List.isEmpty();
         int visibleRows = hasEntries ? Math.min(3, Math.max(1, this.clear_List.size())) : 1;
         int selAreaH = hasEntries ? visibleRows * 14 : 12;
         EventBus lanViewport = null;
         if (hasEntries) {
            lanViewport = this.init_EventBus_Map(this.tick_Map, "_lan_entries", x, cy, w, selAreaH, 14, 5);
            lanViewport.init_V(this.clear_List.size() * 14);
         }

         if (hasEntries && lanViewport != null) {
            lanViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
         }

         int sbGap = 7;
         int removeW = 13;
         int stepW = 40;
         int gapPx = 2;
         int removeX = x + w - sbGap - removeW;
         int stepX = removeX - gapPx - stepW;
         int userW = stepX - x - gapPx;
         if (hasEntries && lanViewport != null) {
            lanViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
            int firstVis = lanViewport.pop();

            for (int i = firstVis; i < this.clear_List.size() && i <= lanViewport.tick(); i++) {
               int iy = lanViewport.init_I(i);
               if (iy != -2147483648) {
                  TableIndex.read(ctx, x, iy, stepX - gapPx - x, 13);
                  ThemedTextInputBox uf = this.init_Map.get("lan_user_" + i);
                  if (uf != null) {
                     uf.read(x);
                     uf.write(iy + 1);
                     uf.update(userW);
                     uf.init_V(ctx, mx, my, delta);
                  }

                  ThemedTextInputBox sf = this.init_Map.get("lan_step_" + i);
                  if (sf != null) {
                     sf.read(stepX);
                     sf.write(iy + 1);
                     sf.update(stepW);
                     sf.init_V(ctx, mx, my, delta);
                  }

                  int fi = i;
                  this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy + 1, removeW, mx, my, () -> {
                     this.clear_List.remove(fi);
                     this.valid();
                     EventBus vp = this.tick_Map.get("_lan_entries");
                     if (vp != null) {
                        vp.read_V(-1);
                     }
                  });
               }
            }

            lanViewport.init(ctx);
         } else {
            FileCache.init(ctx, this.init_Font, "No peer filters yet.", x, cy, w - 5 - 1);
         }

         cy += selAreaH;
         TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
         cy += 6;
         int addBtnW = this.write_I("+Add");
         ThemedTextInputBox newUF = this.flush_Map.get("_lan_user_add");
         ThemedTextInputBox newSF = this.flush_Map.get("_lan_step_add");
         if (newUF != null && newSF != null) {
            int plusX = x + w - addBtnW;
            int stepAX = plusX - 3 - stepW;
            int userAW = stepAX - x - 2;
            newUF.read(x);
            newUF.write(cy);
            newUF.update(userAW);
            newUF.init_V(ctx, mx, my, delta);
            newSF.read(stepAX);
            newSF.write(cy);
            newSF.update(stepW);
            newSF.init_V(ctx, mx, my, delta);
            this.init_ByteReader_GuiGraphicsExtract_2(ctx, plusX, cy, addBtnW, 13, "+Add", ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
               String uname = newUF.load_Str().strip();
               int step = 1;

               try {
                  step = Math.max(1, Integer.parseInt(newSF.load_Str().strip()));
               } catch (NumberFormatException var8x) {
               }

               this.clear_List.add(new WaitForLanStepAction.Inner_DataUtil(uname, step));
               int idx = this.clear_List.size() - 1;
               ThemedTextInputBox uf2 = this.init_k_I(80);
               uf2.init_V_Str(uname);
               this.init_Map.put("lan_user_" + idx, uf2);
               ThemedTextInputBox sf2 = this.init_k_I(40);
               sf2.poll(true);
               sf2.init_V_Str(String.valueOf(step));
               this.init_Map.put("lan_step_" + idx, sf2);
               newUF.init_V_Str("");
               newSF.init_V_Str("1");
            });
         }
      }
   }

   private void valid() {
      this.init_Map.keySet().removeIf(k -> k.startsWith("lan_user_") || k.startsWith("lan_step_"));

      for (int i = 0; i < this.clear_List.size(); i++) {
         WaitForLanStepAction.Inner_DataUtil e = this.clear_List.get(i);
         ThemedTextInputBox uf = this.init_k_I(80);
         uf.init_V_Str(e.init_Str);
         this.init_Map.put("lan_user_" + i, uf);
         ThemedTextInputBox sf = this.init_k_I(40);
         sf.poll(true);
         sf.init_V_Str(String.valueOf(e.init_I));
         this.init_Map.put("lan_step_" + i, sf);
      }
   }

   private void load_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int btnH = 13;
      int topY = y + 2;
      int bottomY = y + 20;
      String info = this.pop_Str();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, info, w, font, -1), font, UiColorPalette.bind(), x, y - 10, false);
      int[] topBtns = this.init_IArr_I(x, w, false, "Queue First", "Paste Base64");
      this.init_V_GuiGraphicsExtract(ctx, topBtns[0], topY, topBtns[1], btnH, "Queue First", mx, my, () -> {
         List<ck.Inner_CachePool> queue = ck.init_ck().load_List();
         if (queue != null && !queue.isEmpty()) {
            this.init_V_ck_CachePool(queue.get(0));
            AutismClientMessaging.sendPrefixed("Loaded first queued packet");
         } else {
            AutismClientMessaging.sendPrefixed("Queue is empty");
         }
      });
      this.init_V_GuiGraphicsExtract(ctx, topBtns[2], topY, topBtns[3], btnH, "Paste Base64", mx, my, () -> {
         List<ck.Inner_CachePool> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.init_V_ck_CachePool(pasted.get(0));
            AutismClientMessaging.sendPrefixed("Loaded first pasted packet");
         } else {
            AutismClientMessaging.sendPrefixed("Failed to paste packet data");
         }
      });
      this.init_V_GuiGraphicsExtract(ctx, x, bottomY, Math.min(w, this.write_I("Clear Raw Packet")), btnH, "Clear Raw Packet", mx, my, () -> {
         this.init_CompoundTag.putString("packetData", "");
         AutismClientMessaging.sendPrefixed("Cleared raw packet data");
      });
   }

   private void update_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<String> c2sTargets = this.read_List("C2S");
      List<String> s2cTargets = this.read_List("S2C");
      String summary = c2sTargets.isEmpty() && s2cTargets.isEmpty()
         ? "No packets selected. This step will continue on the next packet in either direction."
         : "This step continues as soon as any selected C2S or S2C packet arrives.";
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, summary);
      this.init_V_GuiGraphicsExtract_2(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 18;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      int[] addBtns = this.init_IArr_I(x, w, false, "Add C2S", "Add S2C");
      this.init_V_GuiGraphicsExtract(ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S", mx, my, () -> this.clear_V_Z(true));
      this.init_V_GuiGraphicsExtract(ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C", mx, my, () -> this.clear_V_Z(false));
      cy += 18;
      int[] loadBtns = this.init_IArr_I(x, w, false, "Load Queue", "Clear All");
      this.init_V_GuiGraphicsExtract(ctx, loadBtns[0], cy, loadBtns[1], 13, "Load Queue", mx, my, this::A);
      this.init_V_GuiGraphicsExtract(ctx, loadBtns[2], cy, loadBtns[3], 13, "Clear All", mx, my, () -> {
         this.close_List().clear();
         EventBus vpC2s = this.tick_Map.get("wait_packet_c2s");
         if (vpC2s != null) {
            vpC2s.load_V(0);
         }

         EventBus vpS2c = this.tick_Map.get("wait_packet_s2c");
         if (vpS2c != null) {
            vpS2c.load_V(0);
         }
      });
      cy += 20;
      cy = this.init_I_GuiGraphicsExtract(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "wait_packet_c2s",
         "C2S Packets",
         c2sTargets,
         this::set_V,
         this::valid,
         ignored -> null,
         () -> this.get_V("C2S"),
         "No C2S packets selected"
      );
      cy += 4;
      this.init_I_GuiGraphicsExtract(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "wait_packet_s2c",
         "S2C Packets",
         s2cTargets,
         this::set_V,
         this::valid,
         ignored -> null,
         () -> this.get_V("S2C"),
         "No S2C packets selected"
      );
   }

   private void sync(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      if (this.reset_List == null) {
         this.reset_List = new ArrayList<>();
      }

      if (this.f_z_I >= this.reset_List.size()) {
         this.f_z_I = this.reset_List.isEmpty() ? -1 : 0;
      }

      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Add a packet, then set field + rule.");
      this.init_V_GuiGraphicsExtract_2(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 18;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      int[] addBtns = this.init_IArr_I(x, w, false, "Add C2S", "Add S2C");
      this.init_V_GuiGraphicsExtract(ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S", mx, my, () -> this.add(true));
      this.init_V_GuiGraphicsExtract(ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C", mx, my, () -> this.add(false));
      cy += 18;
      this.read_V(ctx, x, cy, w, mx, my);
      cy += 13 + Math.max(1, Math.min(4, Math.max(1, this.reset_List.size()))) * 14 + 5;
      if (this.reset_List.isEmpty()) {
         this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "No packet rules yet. Add C2S or S2C.");
      } else {
         autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.reset_List.get(Math.max(0, this.f_z_I));
         List<String> dirOpts = new ArrayList<>();

         for (autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil d : autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.values()) {
            dirOpts.add(d.name());
         }

         cy = this.init_I_GuiGraphicsExtract_2(
            ctx,
            font,
            "Direction",
            "_wpm_dir",
            dirOpts,
            rule.init_aM_DataUtil.ordinal(),
            x,
            cy,
            w,
            i -> rule.init_aM_DataUtil = autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.values()[i]
         );
         Class<? extends Packet<?>> packetClass = this.init_Class_aM_CachePool(rule);
         List<String> fieldNames = new ArrayList<>(autismclient.util.macro.WaitPacketMatchAction.init_List(packetClass));
         List<String> fieldDisplay = new ArrayList<>();
         fieldDisplay.add("Packet Only");
         fieldDisplay.addAll(fieldNames);
         int fieldIdx = rule.load != null && !rule.load.isBlank() ? Math.max(0, fieldNames.indexOf(rule.load) + 1) : 0;
         cy = this.init_I_GuiGraphicsExtract_2(ctx, font, "Field", "_wpm_field", fieldDisplay, fieldIdx, x, cy, w, i -> {
            rule.load = i <= 0 ? "" : fieldNames.get(i - 1);
            if (rule.load.isBlank()) {
               rule.init_aM_StreamUtil = autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS;
            }

            List<String> vo = autismclient.util.macro.WaitPacketMatchAction.init_List_Class_Str(packetClass, rule.load);
            if (!vo.isEmpty() && (rule.read == null || rule.read.isBlank() || !vo.contains(rule.read))) {
               rule.read = vo.get(0);
            }

            this.z();
         });
         if (rule.load != null && !rule.load.isBlank()) {
            List<String> opOpts = new ArrayList<>();

            for (autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil o : autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.values()) {
               opOpts.add(o.name());
            }

            cy = this.init_I_GuiGraphicsExtract_2(
               ctx,
               font,
               "Operator",
               "_wpm_op",
               opOpts,
               rule.init_aM_StreamUtil.ordinal(),
               x,
               cy,
               w,
               i -> rule.init_aM_StreamUtil = autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.values()[i]
            );
            if (rule.init_aM_StreamUtil != autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS) {
               List<String> valueOptions = autismclient.util.macro.WaitPacketMatchAction.init_List_Class_Str(packetClass, rule.load);
               if (!valueOptions.isEmpty()) {
                  int valIdx = Math.max(0, valueOptions.indexOf(rule.read));
                  cy = this.init_I_GuiGraphicsExtract_2(ctx, font, "Value", "_wpm_value_dd", valueOptions, valIdx, x, cy, w, i -> {
                     rule.read = valueOptions.get(i);
                     this.z();
                  });
               } else {
                  ThemedTextInputBox valueField = this.init_Map.get("_wpm_value");
                  if (valueField != null) {
                     int lw = this.init_I_I(w, "Value", font, 72);
                     this.init_V_GuiGraphicsExtract_3(ctx, "Value", x, cy, lw, font);
                     valueField.read(this.flush_I(x, lw));
                     valueField.write(cy + 1);
                     valueField.update(this.close(w, lw));
                     valueField.init_V(ctx, mx, my, delta);
                     cy += 16;
                  }
               }
            }
         }
      }
   }

   private int init_I_GuiGraphicsExtract_2(
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

      int lw = this.init_I_I(w, label, font, 72);
      this.init_V_GuiGraphicsExtract_3(ctx, label, x, cy, lw, font);
      int ctrlX = this.flush_I(x, lw);
      int ctrlW = this.init_I_List_I(options, this.close(w, lw));
      int idx = Math.max(0, Math.min(selectedIdx, options.size() - 1));
      List<String> opts = options;
      IntConsumer cb = onSelect;
      autismclient.gui.f.b.SyncManager dd = this.run_Map
         .computeIfAbsent(cacheKey, k -> new autismclient.gui.f.b.SyncManager(ctrlX, cy + 2, ctrlW, 13, opts, idx, cb));
      dd.init_SyncManager_I(ctrlX, cy + 2, ctrlW, 13).init_SyncManager_List(opts).init_SyncManager_I(idx).init_SyncManager_IntConsumer(cb);
      this.sync_List.add(dd);
      return cy + 14 + 2;
   }

   private void init_V_Str(String cacheKey, List<String> options, int selectedIdx, int x, int y, int w, int h, IntConsumer onSelect) {
      if (!options.isEmpty()) {
         int idx = Math.max(0, Math.min(selectedIdx, options.size() - 1));
         List<String> opts = options;
         IntConsumer cb = onSelect;
         autismclient.gui.f.b.SyncManager dd = this.run_Map.computeIfAbsent(cacheKey, k -> new autismclient.gui.f.b.SyncManager(x, y, w, h, opts, idx, cb));
         dd.init_SyncManager_I(x, y, w, h).init_SyncManager_List(opts).init_SyncManager_I(idx).init_SyncManager_IntConsumer(cb);
         this.sync_List.add(dd);
      }
   }

   private void read_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Packet Rules (" + this.reset_List.size() + ")", font, UiColorPalette.parse(), x, y + 2, false);
      int clearW = this.write_I("Clear");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, x + w - clearW, y, clearW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, !this.reset_List.isEmpty(), mx, my, () -> {
            this.reset_List.clear();
            this.f_z_I = -1;
         }
      );
      y += 14;
      int rows = Math.max(1, Math.min(4, Math.max(1, this.reset_List.size())));
      int listH = rows * 14;
      int delW = 13;
      if (this.reset_List.isEmpty()) {
         FileCache.init(ctx, this.init_Font, "No packet rules selected.", x, y, w - 5 - 1);
      } else {
         EventBus viewport = this.init_EventBus_Map(this.tick_Map, "_wpm_rules", x, y, w, listH, 14, 5);
         viewport.init_V(this.reset_List.size() * 14);
         viewport.init_V_GuiGraphicsExtract(ctx, mx, my);
         int rowW = w - 5 - 1 - delW - 2;
         viewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);

         for (int i = viewport.pop(); i < this.reset_List.size() && i <= viewport.tick(); i++) {
            int iy = viewport.init_I(i);
            if (iy != -2147483648) {
               autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.reset_List.get(i);
               boolean selected = i == this.f_z_I;
               boolean hovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  this.init_Component_aM_CachePool(rule),
                  autismclient.gui.f.NetHelper.init(x, iy, rowW, 13),
                  hovered,
                  selected,
                  FileCache.Inner_DataUtil.NORMAL,
                  true
               );
               int fi = i;
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, rowW, 13, () -> {
                  this.y();
                  this.f_z_I = fi;
                  this.z();
               }));
               this.init_V_GuiGraphicsExtract_2(ctx, x + rowW + 2, iy, delW, mx, my, () -> {
                  this.reset_List.remove(fi);
                  if (this.f_z_I >= this.reset_List.size()) {
                     this.f_z_I = this.reset_List.size() - 1;
                  }

                  this.z();
               });
            }
         }

         viewport.init(ctx);
      }
   }

   private void flush(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<ck.Inner_CachePool> actionPackets = this.read_List();
      List<ck.Inner_CachePool> queuePackets = ck.init_ck().load_List();
      if (queuePackets == null) {
         queuePackets = Collections.emptyList();
      }

      List<ck.Inner_CachePool> finalQueuePackets = queuePackets;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Action Packets (" + actionPackets.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      int apClearW = this.write_I("Clear");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx,
         x + w - apClearW,
         cy,
         apClearW,
         13,
         "Clear",
         ByteReader.Inner_StreamUtil.DANGER,
         !actionPackets.isEmpty(),
         mx,
         my,
         () -> this.write(Collections.emptyList())
      );
      cy += 14;
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, mx, my, delta, "_send_packet_action", actionPackets, true, packetIndex -> {
         List<ck.Inner_CachePool> updated = this.read_List();
         if (packetIndex >= 0 && packetIndex < updated.size()) {
            updated.remove(packetIndex);
            this.write(updated);
         }
      });
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      ThemedTextInputBox nameField = this.init_Map.get("customName");
      if (nameField != null) {
         int lw = this.init_I_I(w, "Name", font);
         this.init_V_GuiGraphicsExtract_3(ctx, "Name", x, cy, lw, font);
         nameField.read(this.flush_I(x, lw));
         nameField.write(cy + 2);
         nameField.update(this.close(w, lw));
         nameField.init_V(ctx, mx, my, delta);
         cy += 16;
      }

      ThemedTextInputBox countField = this.init_Map.get("sendCount");
      if (countField != null) {
         int lw = this.init_I_I(w, "Send Count", font);
         this.init_V_GuiGraphicsExtract_3(ctx, "Send Count", x, cy, lw, font);
         countField.read(this.flush_I(x, lw));
         countField.write(cy + 2);
         countField.update(this.close(w, lw));
         countField.init_V(ctx, mx, my, delta);
         cy += 16;
      }

      this.load(ctx, font, "waitForGuiBefore", "waitForGuiAfter", x, cy, w, mx, my);
      cy += 16;
      if (!this.load_Map.getOrDefault("waitForGuiBefore", false) && !this.load_Map.getOrDefault("waitForGuiAfter", false)) {
         ThemedTextInputBox guiField = this.init_Map.get("guiName");
         if (guiField != null) {
            guiField.read(-1000);
         }
      } else {
         ThemedTextInputBox guiField = this.init_Map.get("guiName");
         if (guiField != null) {
            int lw = this.init_I_I(w, "GUI Name", font);
            this.init_V_GuiGraphicsExtract_3(ctx, "GUI Name", x, cy, lw, font);
            guiField.read(this.flush_I(x, lw));
            guiField.write(cy + 2);
            guiField.update(this.close(w, lw));
            guiField.init_V(ctx, mx, my, delta);
            cy += 16;
         }
      }

      cy += 2;
      int[] fromBtns = this.init_IArr_I(x, w, false, "From Queue", "Paste Base64");
      this.init_V_GuiGraphicsExtract(ctx, fromBtns[0], cy, fromBtns[1], 13, "From Queue", mx, my, () -> {
         this.write(finalQueuePackets);
         AutismClientMessaging.sendPrefixed("Loaded " + finalQueuePackets.size() + " packets from queue");
      });
      this.init_V_GuiGraphicsExtract(ctx, fromBtns[2], cy, fromBtns[3], 13, "Paste Base64", mx, my, () -> {
         List<ck.Inner_CachePool> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.write(pasted);
            AutismClientMessaging.sendPrefixed("Pasted " + pasted.size() + " packets");
         } else {
            bi.write("Clipboard does not contain packets.");
         }
      });
      cy += 18;
      int[] clearBtns = this.init_IArr_I(x, w, false, "Clear", "GBreak");
      this.init_V_GuiGraphicsExtract(ctx, clearBtns[0], cy, clearBtns[1], 13, "Clear", mx, my, () -> {
         this.write(Collections.emptyList());
         AutismClientMessaging.sendPrefixed("Cleared packet list");
      });
      this.init_V_GuiGraphicsExtract(ctx, clearBtns[2], cy, clearBtns[3], 13, "GBreak", mx, my, this::bind);
      cy += 20;
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Current Queue (" + finalQueuePackets.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 14;
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, mx, my, delta, "_send_packet_queue", finalQueuePackets, false, packetIndex -> {
         if (packetIndex >= 0 && packetIndex < finalQueuePackets.size()) {
            List<ck.Inner_CachePool> updated = this.read_List();
            updated.add(finalQueuePackets.get(packetIndex));
            this.write(updated);
         }
      });
   }

   private int init_I_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int mx,
      int my,
      float delta,
      String listKey,
      List<ck.Inner_CachePool> packets,
      boolean removable,
      IntConsumer rowAction
   ) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int listH = 56;
      EventBus packetViewport = this.init_EventBus_Map(this.tick_Map, listKey, x, y, w, listH, 14, 5);
      packetViewport.init_V(packets.size() * 14);
      packetViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      int removeW = removable ? 13 : 0;
      int modeW = removable ? 14 : 0;
      int textW = w - 5 - 2 - (removable ? removeW + 2 + modeW + 2 : 0);
      if (packets.isEmpty()) {
         autismclient.gui.f.b.k.init(ctx, this.init_Font, removable ? "(none - use GBreak, queue, or Paste)" : "(queue empty)", font, UiColorPalette.bind(), x, y + 2, false);
         return y + listH;
      }

      packetViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
      int firstVis = packetViewport.pop();

      for (int i = firstVis; i < packets.size() && i <= packetViewport.tick(); i++) {
         int iy = packetViewport.init_I(i);
         if (iy != -2147483648) {
            ck.Inner_CachePool qp = packets.get(i);
            String pname = qp != null && qp.init_Packet != null ? PacketNameResolver.init_Str_Packet(qp.init_Packet) : "???";
            String rowText = i + 1 + ". " + pname + " d=" + (qp != null ? qp.load_I() : 0);
            boolean hovered = mx >= x && mx < x + textW && my >= iy && my < iy + 13;
            autismclient.gui.macro.editor.a.CachePool.init(
               ctx,
               this.init_Font,
               Component.literal(rowText),
               autismclient.gui.f.NetHelper.init(x, iy, textW, 13),
               hovered,
               false,
               FileCache.Inner_DataUtil.NORMAL,
               false
            );
            int rowIndex = i;
            if (!removable) {
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, textW, 13, () -> rowAction.accept(rowIndex)));
            }

            if (removable) {
               int modeX = x + textW + 2;
               boolean exact = qp != null && qp.init_Z();
               this.init_ByteReader_GuiGraphicsExtract_2(
                  ctx,
                  modeX,
                  iy,
                  modeW,
                  13,
                  exact ? "E" : "R",
                  exact ? ByteReader.Inner_StreamUtil.DANGER : ByteReader.Inner_StreamUtil.SUCCESS,
                  qp != null && qp.init_Packet != null,
                  mx,
                  my,
                  () -> {
                     List<ck.Inner_CachePool> updated = this.read_List();
                     if (rowIndex >= 0 && rowIndex < updated.size()) {
                        ck.Inner_CachePool target = updated.get(rowIndex);
                        if (target != null && target.init_Packet != null) {
                           target.init(target.init_Z() ? ck.Inner_BufferHelper.REGENERATE : ck.Inner_BufferHelper.EXACT);
                           this.write(updated);
                        }
                     }
                  }
               );
               int removeX = modeX + modeW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy, removeW, mx, my, () -> rowAction.accept(rowIndex));
            }
         }
      }

      packetViewport.init(ctx);
      return y + listH;
   }

   private void write_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int btnH = 13;
      int topY = y + 2;
      int bottomY = y + 20;
      String info = this.push_Str();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, info, w, font, -1), font, UiColorPalette.bind(), x, y - 10, false);
      int[] topBtns = this.init_IArr_I(x, w, false, "From Queue", "Paste Base64");
      this.init_V_GuiGraphicsExtract(ctx, topBtns[0], topY, topBtns[1], btnH, "From Queue", mx, my, () -> {
         List<ck.Inner_CachePool> queue = ck.init_ck().load_List();
         this.write(queue);
         AutismClientMessaging.sendPrefixed("Loaded " + this.read_List().size() + " packets from queue");
      });
      this.init_V_GuiGraphicsExtract(ctx, topBtns[2], topY, topBtns[3], btnH, "Paste Base64", mx, my, () -> {
         List<ck.Inner_CachePool> pasted = MacroClipboardCodec.read();
         if (pasted != null && !pasted.isEmpty()) {
            this.write(pasted);
            AutismClientMessaging.sendPrefixed("Pasted " + pasted.size() + " packets");
         } else {
            bi.write("Clipboard does not contain packets.");
         }
      });
      String gbreakLbl = this.close_Z ? "Capture: In Game" : "GBreak";
      int[] bottomBtns = this.init_IArr_I(x, w, false, "Clear", gbreakLbl);
      this.init_V_GuiGraphicsExtract(ctx, bottomBtns[0], bottomY, bottomBtns[1], btnH, "Clear", mx, my, () -> {
         this.write(Collections.emptyList());
         AutismClientMessaging.sendPrefixed("Cleared packet list");
      });
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, bottomBtns[2], bottomY, bottomBtns[3], btnH, gbreakLbl, ByteReader.Inner_StreamUtil.SECONDARY, !this.close_Z, mx, my, () -> {
            if (!this.close_Z) {
               final int session = this.auditEditSession;
               ck.init_ck().init_V_Runnable(() -> init_Minecraft.execute(() -> {
                  if (this.auditEditSession != session || this.init_CompoundTag == null) {
                     return;
                  }
                  List<ck.Inner_CachePool> captured = ck.init_ck().sync_List();
                  this.write(captured);
                  AutismClientMessaging.sendPrefixed(captured.isEmpty() ? "GBreak capture finished with no packet" : "GBreak packet captured");
               }));
               AutismClientMessaging.sendPrefixed("Break a block now to capture the GBreak packet");
            }
         }
      );
   }

   private void load_V_H(PayloadAction seedAction) {
      this.init_bE = PayloadEditorModel.init_bE_H(seedAction);
      this.init_ActionEditorOverla_2 = null;
      this.write_J = -9223372036854775808L;
      this.init_ActionEditorOverla = null;
      ActionEditorOverlay.JsonUtil state = this.init_ActionEditorOverla_H(seedAction);
      this.init_ActionEditorOverla_3 = state.init_ActionEditorOverla();
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.add_Z = false;
      this.clear_Z = false;
      this.reset_Z = false;
      this.start_Z = false;
      ThemedTextInputBox channelField = this.init_k_I(120);
      channelField.init_V_Str(state.init_Str());
      channelField.init_V_Component(Component.literal("namespace:channel"));
      channelField.init_V_Consumer(text -> {
         if (!this.start_Z) {
            this.put_Z = true;
            if (this.init_bE != null) {
               this.init_bE.read_Str = text != null && !text.isBlank() ? text.strip() : "minecraft:brand";
               this.init_bE.init_V();
               this.init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper.PREVIEW_ONLY, "");
            }

            this.data();
         }
      });
      this.init_Map.put("payload_channel", channelField);
      if (seedAction != null) {
         seedAction.payloadPhase = this.init_bE.load_Str;
      }

      ThemedTextInputBox contentField = this.init_k_I(126);
      contentField.load(true);
      contentField.read(true);
      contentField.sync(true);
      contentField.write(false);
      contentField.update(false);
      contentField.init_V_I(1376389388);
      contentField.load(32767);
      contentField.init_V_Component(Component.literal("direction = C2S\nphase = PLAY\nchannel = minecraft:brand\n\nwriteMcString = vanilla"));
      contentField.init_V_Function(this::init_Component_Str);
      contentField.init_V_LongSupplier(this::init_J);
      contentField.init_V_Consumer(text -> {
         if (!this.start_Z) {
            this.get_Z = true;
            this.init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper.SCRIPT, text);
         }
      });
      this.init_Map.put("payload_content", contentField);
      ThemedTextInputBox hexView = this.init_k_I(138);
      hexView.load(true);
      hexView.read(true);
      hexView.sync(true);
      hexView.write(false);
      hexView.update(false);
      hexView.init_V_I(1376389388);
      hexView.open(true);
      hexView.load(32767);
      hexView.init_V_Component(Component.literal("00 01 02 FF"));
      hexView.init_V_Function(this::load_Component_Str);
      hexView.init_V_LongSupplier(this::init_J);
      hexView.init_V_Consumer(text -> {
         if (!this.start_Z) {
            this.init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper.HEX, text);
         }
      });
      this.init_Map.put("payload_hex_view", hexView);
      ThemedTextInputBox utf8View = this.init_k_I(138);
      utf8View.load(true);
      utf8View.read(true);
      utf8View.sync(true);
      utf8View.write(false);
      utf8View.update(false);
      utf8View.init_V_I(1376389388);
      utf8View.open(true);
      utf8View.load(32767);
      utf8View.init_V_Component(Component.literal("Raw UTF-8 text"));
      utf8View.init_V_Function(this::read_Component);
      utf8View.init_V_LongSupplier(this::init_J);
      utf8View.init_V_Consumer(text -> {
         if (!this.start_Z) {
            this.init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper.UTF8, text);
         }
      });
      this.init_Map.put("payload_utf8_view", utf8View);
      ThemedTextInputBox logicalView = this.init_k_I(138);
      logicalView.load(true);
      logicalView.read(true);
      logicalView.sync(true);
      logicalView.write(false);
      logicalView.update(false);
      logicalView.init_V_I(1376389388);
      logicalView.open(true);
      logicalView.load(32767);
      logicalView.init_V_Component(Component.literal("direction = C2S\nphase = PLAY\nchannel = minecraft:brand\nbodyHex = ..."));
      logicalView.init_V_Function(this::write_Component);
      logicalView.init_V_LongSupplier(this::init_J);
      logicalView.init_V_Consumer(text -> {
         if (!this.start_Z) {
            this.init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper.LOGICAL, text);
         }
      });
      this.init_Map.put("payload_logical_view", logicalView);
      ThemedTextInputBox commandField = this.init_k_I(66);
      commandField.poll(true);
      commandField.init_V_Str(String.valueOf(this.init_bE.load_I));
      commandField.init_V_Component(Component.literal("value"));
      commandField.init_V_Consumer(text -> {
         if (!this.start_Z && this.init_bE != null) {
            try {
               this.init_bE.load_I = Integer.parseInt((text == null ? "" : text).strip());
               this.init_bE.init_Z = true;
               this.init_bE.load_Z = true;
               this.init_bE.init_V();
               this.init_ActionEditorOverla = null;
               this.load_Map.put("payload_command_api", true);
               this.get_Z = true;
            } catch (NumberFormatException var3x) {
            }
         }
      });
      this.init_Map.put("payload_command_value", commandField);
      this.load_Map.put("payload_command_api", this.init_bE.load_Z);
      this.pop(true);
      PayloadFieldCodec.Inner_JsonUtil template = this.load_bJ_JsonUtil();
      PayloadFieldCodec.Inner_StreamUtil suggestedMode = this.init_bJ_StreamUtil_Str(template.init_Str(), template.init_List(), template.init_bJ_StreamUtil());
      this.read_Map.put("payload_mode", this.init_I_bJ_StreamUtil(suggestedMode));
      this.read_Map
         .put("payload_add_type", this.init_I_bJ_BufferHelper(this.init_bJ_BufferHelper_Str(template.init_Str(), suggestedMode, template.init_List())));
   }

   private ActionEditorOverlay.JsonUtil init_ActionEditorOverla_H(PayloadAction seedAction) {
      PayloadAction action = new PayloadAction();
      if (seedAction != null) {
         action.fromTag(seedAction.toTag());
      }

      String channel = action.channel == null ? "" : action.channel.strip();
      byte[] bytes = new byte[0];
      boolean haveBytes = false;
      if (action.payloadData != null && !action.payloadData.isBlank()) {
         try {
            bytes = AutismPayloadSupport.init_BArr(action.payloadData);
            haveBytes = true;
         } catch (Exception ignored) {
            bytes = new byte[0];
         }
      }

      if (action.payloadJson != null && !action.payloadJson.isBlank()) {
         try {
            action.payloadJson = PayloadJsonCodec.init_Str(action.payloadJson);
            PayloadJsonCodec.Inner_DataUtil encoded = PayloadJsonCodec.init_bG_DataUtil(action);
            if (channel.isBlank() && encoded.init_Str() != null && !encoded.init_Str().isBlank()) {
               channel = encoded.init_Str();
            }

            if (!haveBytes) {
               bytes = encoded.init_BArr();
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

      if (AutismPayloadSupport.init_Z(channel)) {
         String brandText = AutismPayloadSupport.read(bytes);
         if (brandText != null || bytes.length == 0) {
            return new ActionEditorOverlay.JsonUtil(
               channel,
               bytes,
               false,
               action.commandApiValue,
               ActionEditorOverlay.TaskQueue.BRAND_STRING,
               brandText != null ? brandText : AutismPayloadSupport.init_Str()
            );
         }
      }

      Integer commandValue = AutismPayloadSupport.init_Integer(null, channel, bytes);
      boolean commandRecognized = action.commandApiRecognized || commandValue != null;
      int resolvedCommandValue = commandValue != null ? commandValue : action.commandApiValue;
      ActionEditorOverlay.TaskQueue mode = ActionEditorOverlay.TaskQueue.BINARY_REPLAY;
      String contentText = "";
      if (commandRecognized) {
         mode = ActionEditorOverlay.TaskQueue.COMMAND_INT;
         contentText = String.valueOf(resolvedCommandValue);
      } else {
         String readableText = AutismPayloadSupport.write(bytes);
         if (readableText.isBlank() && bytes.length != 0) {
            contentText = "Binary payload (" + bytes.length + " bytes). Safe replay keeps the captured bytes.";
         } else {
            mode = ActionEditorOverlay.TaskQueue.UTF8_TEXT;
            contentText = this.init_Str_Str(readableText);
         }
      }

      return new ActionEditorOverlay.JsonUtil(channel, bytes, commandRecognized, resolvedCommandValue, mode, contentText);
   }

   private String init_Str_Str(String text) {
      if (text != null && !text.isBlank()) {
         String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
         String stripped = normalized.strip();
         if (!stripped.startsWith("{") && !stripped.startsWith("[")) {
            return normalized;
         }

         try {
            return init_Gson.toJson(JsonParser.parseString(stripped));
         } catch (Throwable ignored) {
            return normalized;
         }
      } else {
         return "";
      }
   }

   private void open(boolean syncPreview) {
      ThemedTextInputBox jsonField = this.init_Map.get("payload_json");
      if (jsonField != null) {
         jsonField.init_V_Str(PayloadJsonCodec.init_Str(jsonField.load_Str()));
         if (syncPreview) {
            this.poll(true);
         }
      }
   }

   private void poll(boolean notifyOnError) {
      ThemedTextInputBox jsonField = this.init_Map.get("payload_json");
      if (jsonField != null) {
         PayloadAction preview = new PayloadAction();
         if (this.init_H != null) {
            preview.fromTag(this.init_H.toTag());
         }

         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         ThemedTextInputBox rawField = this.init_Map.get("payload_data");
         ThemedTextInputBox textField = this.init_Map.get("payload_text");
         ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
         preview.payloadJson = jsonField.load_Str();
         preview.channel = channelField == null ? preview.channel : channelField.load_Str().strip();
         preview.payloadData = rawField == null ? preview.payloadData : rawField.load_Str();
         preview.commandApiRecognized = this.load_Map.getOrDefault("payload_command_api", preview.commandApiRecognized);
         if (commandField != null) {
            try {
               preview.commandApiValue = Integer.parseInt(commandField.load_Str().strip());
            } catch (NumberFormatException var13) {
            }
         }

         try {
            preview.payloadJson = PayloadJsonCodec.init_Str(preview.payloadJson);
            PayloadJsonCodec.Inner_DataUtil encoded = PayloadJsonCodec.init_bG_DataUtil(preview);
            this.start_Z = true;

            try {
               jsonField.init_V_Str(preview.payloadJson);
               if (channelField != null) {
                  channelField.init_V_Str(encoded.init_Str());
               }

               if (rawField != null) {
                  rawField.init_V_Str(AutismPayloadSupport.init_Str(encoded.init_BArr()));
               }

               if (textField != null) {
                  textField.init_V_Str(AutismPayloadSupport.write(encoded.init_BArr()));
               }

               this.put_Z = true;
               this.set_Z = true;
               this.add_Z = false;
            } finally {
               this.start_Z = false;
            }

            Integer commandValue = AutismPayloadSupport.init_Integer(null, encoded.init_Str(), encoded.init_BArr());
            if (commandValue != null) {
               this.load_Map.put("payload_command_api", true);
               if (commandField != null) {
                  commandField.init_V_Str(String.valueOf(commandValue));
               }
            }
         } catch (Exception e) {
            if (notifyOnError) {
               AutismClientMessaging.sendPrefixed("§cCould not rebuild payload from JSON: " + AutismPayloadSupport.init(e));
            }
         }
      }
   }

   private void load_V_List(List<PayloadFieldCodec.Inner_CachePool> fields) {
      this.size();
      List<PayloadFieldCodec.Inner_CachePool> safeFields = fields == null ? List.of() : fields;
      this.f_h_I = safeFields.size();
      if (this.init_bE != null) {
         this.init_bE.init_List = new ArrayList<>(safeFields);
         this.init_bE.init_V();
         this.init_bE.init_Z_Str(this.init_bE.init_Str());
         this.init_ActionEditorOverla = null;
         this.pop(true);
      } else {
         ThemedTextInputBox contentField = this.init_Map.get("payload_content");
         if (contentField != null) {
            contentField.init_V_Str(PayloadFieldCodec.init(safeFields));
         }
      }
   }

   private void size() {
      this.init_Map.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_value_"));
      this.read_Map.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_type_"));
      this.run_Map.entrySet().removeIf(entry -> entry.getKey().startsWith("payload_type_"));
      this.f_h_I = 0;
   }

   private List<PayloadFieldCodec.Inner_CachePool> load_List() {
      if (this.init_bE != null) {
         return this.init_bE.init_List == null ? List.of() : this.init_bE.init_List;
      }

      ThemedTextInputBox contentField = this.init_Map.get("payload_content");
      return PayloadFieldCodec.init_List(contentField == null ? "" : contentField.load_Str());
   }

   private void data() {
      if (!this.start_Z) {
         String channel = this.write_Str();
         List<PayloadFieldCodec.Inner_CachePool> fields = this.load_List();
         if (!this.clear_Z) {
            PayloadFieldCodec.Inner_StreamUtil current = add_List.get(
               Math.max(0, Math.min(this.read_Map.getOrDefault("payload_mode", this.init_I_bJ_StreamUtil(PayloadFieldCodec.Inner_StreamUtil.ADVANCED_MIXED)), add_List.size() - 1))
            );
            this.read_Map.put("payload_mode", this.init_I_bJ_StreamUtil(this.init_bJ_StreamUtil_Str(channel, fields, current)));
         }

         if (!this.reset_Z) {
            PayloadFieldCodec.Inner_StreamUtil mode = this.init_bJ_StreamUtil_Str(channel);
            this.read_Map.put("payload_add_type", this.init_I_bJ_BufferHelper(this.init_bJ_BufferHelper_Str(channel, mode, fields)));
         }
      }
   }

   private String write_Str() {
      if (this.init_bE != null && this.init_bE.read_Str != null && !this.init_bE.read_Str.isBlank()) {
         return this.init_bE.read_Str.strip();
      } else {
         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         if (channelField != null && !channelField.load_Str().isBlank()) {
            return channelField.load_Str().strip();
         } else {
            return this.init_H != null && this.init_H.channel != null && !this.init_H.channel.isBlank() ? this.init_H.channel.strip() : "minecraft:brand";
         }
      }
   }

   private PayloadFieldCodec.Inner_StreamUtil init_bJ_StreamUtil_Str(String channel, List<PayloadFieldCodec.Inner_CachePool> fields, PayloadFieldCodec.Inner_StreamUtil fallback) {
      List<PayloadFieldCodec.Inner_CachePool> safeFields = fields == null ? List.of() : fields;
      if (AutismPayloadSupport.init_Z(channel)) {
         return PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF;
      }

      if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip())) {
         return PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT;
      }

      for (PayloadFieldCodec.Inner_CachePool field : safeFields) {
         if (field != null) {
            switch (field.init_bJ_BufferHelper()) {
               case HEX_BYTES:
               case RAW_BYTES:
               case BYTE_ARRAY:
                  return PayloadFieldCodec.Inner_StreamUtil.MANUAL_HEX;
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
                  return PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT;
               case VAR_INT:
               case VAR_LONG:
               case MINECRAFT_STRING:
               case IDENTIFIER:
               case UUID_FIELD:
               case BLOCK_POS:
               case ENUM_VAR_INT:
               case OPTIONAL_VALUE:
                  return PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF;
               case JSON_STRING:
               case TEXT_COMPONENT:
               case NBT:
               case ITEM_STACK:
                  return PayloadFieldCodec.Inner_StreamUtil.JSON_TEXT;
               case STRING_BYTES:
               case RAW_UTF8_STRING:
                  return PayloadFieldCodec.Inner_StreamUtil.RAW_UTF8;
            }
         }
      }

      return fallback == null ? PayloadFieldCodec.Inner_StreamUtil.ADVANCED_MIXED : fallback;
   }

   private PayloadFieldCodec.Inner_BufferHelper init_bJ_BufferHelper_Str(String channel, PayloadFieldCodec.Inner_StreamUtil mode, List<PayloadFieldCodec.Inner_CachePool> fields) {
      if (AutismPayloadSupport.init_Z(channel)) {
         return PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING;
      } else if ("bungeecord:main".equalsIgnoreCase(channel == null ? "" : channel.strip())) {
         return PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF;
      } else if (mode == PayloadFieldCodec.Inner_StreamUtil.MANUAL_HEX) {
         return PayloadFieldCodec.Inner_BufferHelper.HEX_BYTES;
      } else if (mode == PayloadFieldCodec.Inner_StreamUtil.JAVA_DATA_OUTPUT) {
         return PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF;
      } else if (mode == PayloadFieldCodec.Inner_StreamUtil.MINECRAFT_BYTEBUF) {
         return PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING;
      } else {
         return mode == PayloadFieldCodec.Inner_StreamUtil.JSON_TEXT ? PayloadFieldCodec.Inner_BufferHelper.TEXT_COMPONENT : PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES;
      }
   }

   private void init_V_ActionEditorOverla_Str(ActionEditorOverlay.NetHelper kind, String text) {
      if (kind != null) {
         if (this.init_ActionEditorOverla_2 != null && this.init_ActionEditorOverla_2.init_ActionEditorOverla() != kind) {
            this.push(true);
         }

         this.init_ActionEditorOverla_2 = new ActionEditorOverlay.StateCache(kind, text == null ? "" : text);
         this.read_J = System.nanoTime();
      }
   }

   private void push(boolean force) {
      ActionEditorOverlay.StateCache edit = this.init_ActionEditorOverla_2;
      if (edit != null && (force || System.nanoTime() - this.read_J >= 125000000L)) {
         this.init_ActionEditorOverla_2 = null;
         if (this.init_bE == null) {
            this.data();
         } else {
            switch (edit.init_ActionEditorOverla()) {
               case SCRIPT:
                  if (this.init_bE.init_Z_Str(edit.init_Str())) {
                     this.tick(false);
                     this.run(false);
                  }

                  this.data();
                  break;
               case HEX:
                  this.init_V_Str_2(edit.init_Str());
                  break;
               case UTF8:
                  this.load_V_Str(edit.init_Str());
                  break;
               case LOGICAL:
                  this.read_V(edit.init_Str());
                  break;
               case PREVIEW_ONLY:
                  this.run(false);
            }

            this.init_ActionEditorOverla = null;
         }
      }
   }

   private void init_V_Str_2(String text) {
      if (this.init_bE != null && this.init_bE.load_Z_Str(text)) {
         this.get_Z = true;
         this.pop(false);
      }
   }

   private void load_V_Str(String text) {
      if (this.init_bE != null) {
         this.init_bE.read(text);
         this.get_Z = true;
         this.pop(false);
      }
   }

   private void read_V(String text) {
      if (this.init_bE != null && this.init_bE.write(text)) {
         this.get_Z = true;
         this.pop(false);
      }
   }

   private void pop(boolean force) {
      this.tick(force);
      this.run(force);
   }

   private void tick(boolean force) {
      if (this.init_bE != null) {
         this.start_Z = true;

         try {
            ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
            if (channelField != null && (force || !channelField.init_Z())) {
               channelField.init_V_Str(this.init_bE.read_Str == null ? "" : this.init_bE.read_Str);
            }

            this.read_Map.put("payload_mode", this.init_I_bJ_StreamUtil(PayloadFieldCodec.Inner_StreamUtil.init(this.init_bE.sync, this.init_bE.read_Str)));
            ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
            if (commandField != null && (force || !commandField.init_Z())) {
               commandField.init_V_Str(String.valueOf(this.init_bE.load_I));
            }

            this.load_Map.put("payload_command_api", this.init_bE.load_Z);
         } finally {
            this.start_Z = false;
         }
      }
   }

   private void run(boolean force) {
      if (this.init_bE != null) {
         long revision = this.init_bE.init_J();
         if (force || this.write_J != revision) {
            this.start_Z = true;

            try {
               ThemedTextInputBox contentField = this.init_Map.get("payload_content");
               if (contentField != null && (force || !contentField.init_Z())) {
                  contentField.init_V_Str(this.init_bE.init_Str());
               }

               ThemedTextInputBox hexView = this.init_Map.get("payload_hex_view");
               if (hexView != null && (force || !hexView.init_Z())) {
                  hexView.init_V_Str(this.init_bE.load_Str());
               }

               ThemedTextInputBox utf8View = this.init_Map.get("payload_utf8_view");
               if (utf8View != null && (force || !utf8View.init_Z())) {
                  utf8View.init_V_Str(this.init_bE.read());
               }

               ThemedTextInputBox logicalView = this.init_Map.get("payload_logical_view");
               if (logicalView != null && (force || !logicalView.init_Z())) {
                  logicalView.init_V_Str(this.init_bE.write());
               }
            } finally {
               this.start_Z = false;
            }

            this.write_J = revision;
         }
      }
   }

   private String load_Str_Str(String text) {
      return text != null && !text.isEmpty() ? text.replace("\\", "\\\\").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n") : "";
   }

   private int init_I_bJ_BufferHelper(PayloadFieldCodec.Inner_BufferHelper type) {
      int index = get_List.indexOf(type);
      return index < 0 ? get_List.indexOf(PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES) : index;
   }

   private PayloadFieldCodec.Inner_BufferHelper init_bJ_BufferHelper_I(int row) {
      int index = Math.max(
         0, Math.min(this.read_Map.getOrDefault(this.init_Str_I(row), this.init_I_bJ_BufferHelper(PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES)), get_List.size() - 1)
      );
      return get_List.get(index);
   }

   private String init_Str_I(int row) {
      return "payload_type_" + row;
   }

   private String load_Str_I(int row) {
      return "payload_value_" + row;
   }

   private int init_I_bJ_StreamUtil(PayloadFieldCodec.Inner_StreamUtil mode) {
      int index = add_List.indexOf(mode);
      return index < 0 ? add_List.indexOf(PayloadFieldCodec.Inner_StreamUtil.ADVANCED_MIXED) : index;
   }

   private PayloadFieldCodec.Inner_StreamUtil init_bJ_StreamUtil_Str(String channel) {
      int fallback = this.init_I_bJ_StreamUtil(PayloadFieldCodec.Inner_StreamUtil.init(this.init_H == null ? "" : this.init_H.payloadEncodingMode, channel));
      int index = Math.max(0, Math.min(this.read_Map.getOrDefault("payload_mode", fallback), add_List.size() - 1));
      return add_List.get(index);
   }

   private void close(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      ActionEditorOverlay.ConfigStore preview = this.init_ActionEditorOverla();
      PayloadFieldCodec.Inner_JsonUtil template = preview.init_bJ_JsonUtil();
      PayloadFieldCodec.Inner_DataUtil built = preview.init_bJ_DataUtil();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Custom Payload", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 14;
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w);
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, mx, my);
      int dirLabelW = 18;
      int dirW = this.write_I(template.init_bJ_ConfigStore().name());
      int modeLabelW = 28;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Dir", font, UiColorPalette.bind(), x, cy + 3, false);
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, x + dirLabelW, cy, dirW, 13, template.init_bJ_ConfigStore().name(), ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> {
            this.push(true);
            if (this.init_bE != null) {
               this.init_bE.init_Str = PayloadFieldCodec.init_Str(this.init_bE.init_Str);
               this.init_bE.init_V();
               this.init_ActionEditorOverla = null;
               this.pop(false);
            } else if (this.init_H != null) {
               this.init_H.payloadDirection = PayloadFieldCodec.init_Str(this.init_H.payloadDirection);
            }

            this.get_Z = true;
            this.fill();
         }
      );
      int modeLabelX = x + dirLabelW + dirW + 5;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Mode", font, UiColorPalette.bind(), modeLabelX, cy + 3, false);
      int modeX = modeLabelX + modeLabelW;
      this.init_V_I_2(modeX, cy, this.init_I_List_I(put_List, Math.max(54, x + w - modeX)), mx, my, template);
      cy += 18;
      ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
      if (channelField != null) {
         int lw = this.init_I_I(w, "Channel", font, 64);
         this.init_V_GuiGraphicsExtract_3(ctx, "Channel", x, cy, lw, font);
         int ctrlX = this.flush_I(x, lw);
         int ctrlW = this.close(w, lw);
         channelField.read(ctrlX);
         channelField.write(cy + 2);
         channelField.update(ctrlW);
         channelField.init_V(ctx, mx, my, delta);
         cy += 16;
      }

      if (this.init_bE != null && this.init_bE.init_Z) {
         ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
         if (commandField != null) {
            int lw = this.init_I_I(w, "CommandApi", font, 64);
            this.init_V_GuiGraphicsExtract_3(ctx, "CommandApi", x, cy, lw, font);
            int ctrlX = this.flush_I(x, lw);
            int ctrlW = this.close(w, lw);
            int btnW = Math.max(this.write_I("Override"), this.write_I("Exact"));
            int fieldW = Math.max(38, ctrlW - btnW - 3);
            commandField.read(ctrlX);
            commandField.write(cy + 2);
            commandField.update(fieldW);
            commandField.init_V(ctx, mx, my, delta);
            boolean override = this.load_Map.getOrDefault("payload_command_api", this.init_bE.load_Z);
            this.init_ByteReader_GuiGraphicsExtract_2(
               ctx,
               ctrlX + fieldW + 3,
               cy + 2,
               btnW,
               13,
               override ? "Override" : "Exact",
               override ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.SECONDARY,
               true,
               mx,
               my,
               () -> {
                  if (this.init_bE != null) {
                     this.init_bE.load_Z = !this.init_bE.load_Z;
                     this.init_bE.init_V();
                     this.init_ActionEditorOverla = null;
                     this.load_Map.put("payload_command_api", this.init_bE.load_Z);
                     this.get_Z = true;
                  }

                  this.fill();
               }
            );
            cy += 16;
         }
      }

      if (this.f_i_I == 0) {
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "Packet Script", font, UiColorPalette.parse(), x, cy + 2, false);
         int psClearW = this.write_I("Clear");
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, x + w - psClearW, cy, psClearW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, !this.load_List().isEmpty(), mx, my, () -> {
               ThemedTextInputBox contentField = this.init_Map.get("payload_content");
               if (contentField != null) {
                  contentField.init_V_Str("");
               }

               this.get_Z = true;
               this.fill();
            }
         );
         cy += 15;
         int addButtonW = this.write_I("Add");
         this.init_V_I(x, cy, this.init_I_List_I(set_List, Math.max(54, w - addButtonW - 4)), mx, my);
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx,
            x + w - addButtonW,
            cy,
            addButtonW,
            13,
            "Add",
            ByteReader.Inner_StreamUtil.SUCCESS,
            true,
            mx,
            my,
            () -> this.read_V(this.read_Map.getOrDefault("payload_add_type", this.init_I_bJ_BufferHelper(PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES)))
         );
         cy += 17;
         ThemedTextInputBox contentField = this.init_Map.get("payload_content");
         if (contentField != null) {
            contentField.read(x);
            contentField.write(cy);
            contentField.update(w);
            contentField.sync(126);
            contentField.init_V(ctx, mx, my, delta);
         }

         cy += 130;
      } else if (this.f_i_I == 1) {
         this.run(false);
         ThemedTextInputBox hexView = this.init_Map.get("payload_hex_view");
         if (hexView != null) {
            int bytes = this.init_bE == null ? built.init_BArr().length : this.init_bE.init_BArr.length;
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "Body Hex - payload body only (" + bytes + " bytes)", font, UiColorPalette.parse(), x, cy + 2, false);
            cy += 14;
            hexView.read(x);
            hexView.write(cy);
            hexView.update(w);
            hexView.sync(138);
            hexView.init_V(ctx, mx, my, delta);
            cy += 142;
         }
      } else if (this.f_i_I == 2) {
         this.run(false);
         ThemedTextInputBox utf8View = this.init_Map.get("payload_utf8_view");
         if (utf8View != null) {
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "UTF-8 / Raw Text Body", font, UiColorPalette.parse(), x, cy + 2, false);
            cy += 14;
            utf8View.read(x);
            utf8View.write(cy);
            utf8View.update(w);
            utf8View.sync(138);
            utf8View.init_V(ctx, mx, my, delta);
            cy += 142;
         }
      } else {
         this.run(false);
         ThemedTextInputBox logicalView = this.init_Map.get("payload_logical_view");
         if (logicalView != null) {
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "Logical Packet - envelope + body", font, UiColorPalette.parse(), x, cy + 2, false);
            cy += 14;
            logicalView.read(x);
            logicalView.write(cy);
            logicalView.update(w);
            logicalView.sync(138);
            logicalView.init_V(ctx, mx, my, delta);
            cy += 142;
         }
      }

      String notice = this.load_Str_bJ_DataUtil(built);
      boolean modelBad = this.init_bE != null && this.init_bE.flush != null && !this.init_bE.flush.isBlank();
      int noticeColor = !modelBad && built.load().isEmpty() ? (!built.init_List().isEmpty() ? -14249 : UiColorPalette.bind()) : UiColorPalette.valid();
      autismclient.gui.f.b.k.init(
         ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, notice, Math.max(10, w), font, noticeColor), font, noticeColor, x, cy + 2, false
      );
      cy += 14;
      String sendLbl = this.close_Z ? "Send: In Game" : "Send";
      int[] sendBtns = this.init_IArr_I(x, w, false, sendLbl, "Reset");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, sendBtns[0], cy, sendBtns[1], 13, sendLbl, ByteReader.Inner_StreamUtil.SUCCESS, !this.close_Z && built.init_Z() && !modelBad, mx, my, () -> {
            if (!this.close_Z) {
               try {
                  PayloadAction action = this.load_H();
                  action.execute(init_Minecraft);
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cPayload send failed: " + AutismPayloadSupport.init(e));
               }
            }
         }
      );
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, sendBtns[2], cy, sendBtns[3], 13, "Reset", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> {
         this.cache();
         this.fill();
      });
      cy += 20;
      if (this.start_Z()) {
         cy = this.init_I_GuiGraphicsExtract_2(ctx, x, cy, w, "Config-phase payload — sending may disconnect.", -1657776);
      }

      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.close_Str());
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      String[] tabs = new String[]{"Packet", "Body Hex", "UTF-8", "Logical"};
      int gap = 3;
      int tabW = Math.max(42, (w - gap * (tabs.length - 1)) / tabs.length);

      for (int i = 0; i < tabs.length; i++) {
         int tx = x + i * (tabW + gap);
         int tw = i == tabs.length - 1 ? Math.max(30, x + w - tx) : tabW;
         ByteReader.Inner_StreamUtil variant = this.f_i_I == i ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.SECONDARY;
         int index = i;
         this.init_ByteReader_GuiGraphicsExtract_2(ctx, tx, y, tw, 13, tabs[i], variant, true, mx, my, () -> {
            this.push(true);
            this.f_i_I = index;
            this.value();
            this.fill();
         });
      }

      return y + 18;
   }

   private void value() {
      for (String key : List.of("payload_content", "payload_hex_view", "payload_utf8_view", "payload_logical_view")) {
         ThemedTextInputBox field = this.init_Map.get(key);
         if (field != null) {
            field.push(false);
         }
      }
   }

   private void init_V_I(int x, int y, int w, int mx, int my) {
      int idx = Math.max(
         0,
         Math.min(
            this.read_Map
               .getOrDefault(
                  "payload_add_type",
                  this.init_I_bJ_BufferHelper(this.init_bJ_BufferHelper_Str(this.write_Str(), this.init_bJ_StreamUtil_Str(this.write_Str()), this.load_List()))
               ),
            set_List.size() - 1
         )
      );
      autismclient.gui.f.b.SyncManager dd = this.run_Map
         .computeIfAbsent("payload_add_type", key -> new autismclient.gui.f.b.SyncManager(x, y, w, 13, set_List, idx, newIdx -> {
            this.reset_Z = true;
            this.read_Map.put("payload_add_type", newIdx);
         }));
      dd.init_SyncManager_I(x, y, w, 13).init_SyncManager_List(set_List).init_SyncManager_I(idx).init_SyncManager_IntConsumer(newIdx -> {
         this.reset_Z = true;
         this.read_Map.put("payload_add_type", newIdx);
      }).init_SyncManager_Str("");
      this.sync_List.add(dd);
   }

   private void read_V(int index) {
      if (index >= 0 && index < get_List.size()) {
         PayloadFieldCodec.Inner_BufferHelper type = get_List.get(index);
         this.write_V(type.init() + " = " + type.load());
         if (!this.clear_Z) {
            this.read_Map
               .put(
                  "payload_mode",
                  this.init_I_bJ_StreamUtil(
                     this.init_bJ_StreamUtil_Str(
                        this.write_Str(), List.of(new PayloadFieldCodec.Inner_CachePool(type, type.load(), true)), this.init_bJ_StreamUtil_Str(this.write_Str())
                     )
                  )
               );
         }

         this.fill();
      }
   }

   private void init_V_bJ_DataUtil(PayloadFieldCodec.Inner_DataUtil built) {
      ThemedTextInputBox hexView = this.init_Map.get("payload_hex_view");
      if (hexView != null && !hexView.init_Z()) {
         this.start_Z = true;

         try {
            hexView.init_V_Str(this.init_Str_BArr(built.init_BArr()));
         } finally {
            this.start_Z = false;
         }
      }

      ThemedTextInputBox utf8View = this.init_Map.get("payload_utf8_view");
      if (utf8View != null && !utf8View.init_Z()) {
         this.start_Z = true;

         try {
            utf8View.init_V_Str(this.init_Str_bJ_DataUtil(built));
         } finally {
            this.start_Z = false;
         }
      }
   }

   private String init_Str_BArr(byte[] bytes) {
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

   private String init_Str_bJ_DataUtil(PayloadFieldCodec.Inner_DataUtil built) {
      PayloadFieldCodec.Inner_NetHelper preview = built.init_bJ_NetHelper();
      String fullUtf8 = AutismPayloadSupport.write(built.init_BArr());
      if (fullUtf8 != null && !fullUtf8.isBlank()) {
         return fullUtf8;
      } else if (preview.load() != null && !preview.load().isBlank()) {
         return preview.load();
      } else if (preview.read() != null && !preview.read().isBlank()) {
         return preview.read();
      } else {
         return preview.write() != null && !preview.write().isBlank() ? preview.write() : "";
      }
   }

   private String load_Str_bJ_DataUtil(PayloadFieldCodec.Inner_DataUtil built) {
      if (this.init_bE != null && this.init_bE.flush != null && !this.init_bE.flush.isBlank()) {
         return "Fix: " + this.read_Str(this.init_bE.flush);
      }

      String scriptIssue = this.update_Str();
      if (scriptIssue != null) {
         return "Fix: " + this.read_Str(scriptIssue);
      }

      if (this.f_i_I == 1) {
         ThemedTextInputBox hexView = this.init_Map.get("payload_hex_view");
         if (hexView != null && hexView.init_Z()) {
            try {
               this.init_BArr_Str(hexView.load_Str());
            } catch (Exception e) {
               return "Bad hex: " + this.read_Str(AutismPayloadSupport.init(e));
            }
         }
      }

      if (!built.load().isEmpty()) {
         return "Fix: " + this.read_Str(built.load().get(0));
      } else {
         return !built.init_List().isEmpty() ? "Warn: " + this.read_Str(built.init_List().get(0)) : "Ready: " + built.init_BArr().length + " bytes";
      }
   }

   private String read_Str(String message) {
      String text = message == null ? "invalid payload" : message.replace('\n', ' ').strip();
      return text.length() <= 34 ? text : text.substring(0, 31) + "...";
   }

   private byte[] init_BArr_Str(String text) {
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
      String text = AutismPayloadSupport.write(bytes);
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

   private String update_Str() {
      ThemedTextInputBox contentField = this.init_Map.get("payload_content");
      if (contentField == null) {
         return null;
      }

      String text = contentField.load_Str();
      if (text != null && !text.isBlank()) {
         String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

         for (int i = 0; i < lines.length; i++) {
            String raw = lines[i] == null ? "" : lines[i];
            String line = raw.strip();
            if (!line.isEmpty() && !line.startsWith("#")) {
               int split = this.init_I_Str(raw);
               if (split < 0) {
                  return "Line " + (i + 1) + " needs writer = value";
               }

               String typeText = raw.substring(0, split).strip();
               if (!this.load_Z_Str(typeText) && this.init_bJ_BufferHelper_Str(typeText) == null) {
                  return "Line " + (i + 1) + " unknown writer";
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private long init_J() {
      return this.init_bE == null ? 0L : this.init_bE.init_J();
   }

   private Component init_Component_Str(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      String[] lines = safe.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         if (i > 0) {
            this.init_V_MutableComponent(out, "\n", 7829367);
         }

         String line = lines[i];
         if (!line.isEmpty()) {
            String stripped = line.stripLeading();
            if (stripped.startsWith("#")) {
               this.init_V_MutableComponent(out, line, 7829367);
            } else {
               int split = this.init_I_Str(line);
               if (split < 0) {
                  this.init_V_MutableComponent(out, line, 16742263);
               } else {
                  String typePart = line.substring(0, split);
                  String sep = line.substring(split, split + 1);
                  String valuePart = line.substring(split + 1);
                  PayloadFieldCodec.Inner_BufferHelper type = this.init_bJ_BufferHelper_Str(typePart.strip());
                  this.init_V_MutableComponent(out, typePart, this.load_Z_Str(typePart.strip()) ? 16328306 : (type == null ? 16742263 : 6740463));
                  this.init_V_MutableComponent(out, sep, 11184810);
                  this.init_V_MutableComponent_Str(out, valuePart);
               }
            }
         }
      }

      return out;
   }

   private boolean load_Z_Str(String key) {
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

   private Component load_Component_Str(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      int hexCount = 0;

      for (int i = 0; i < safe.length(); i++) {
         char chr = safe.charAt(i);
         if (chr == '\n' || Character.isWhitespace(chr)) {
            this.init_V_MutableComponent(out, Character.toString(chr), 7829367);
         } else if (this.init_Z_C(chr)) {
            int color = hexCount / 2 % 2 == 0 ? 10936878 : 6740463;
            this.init_V_MutableComponent(out, Character.toString(chr), color);
            hexCount++;
         } else {
            this.init_V_MutableComponent(out, Character.toString(chr), 16742263);
         }
      }

      return out;
   }

   private Component read_Component(String source) {
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

         this.init_V_MutableComponent(out, Character.toString(chr), color);
      }

      return out;
   }

   private Component write_Component(String source) {
      MutableComponent out = Component.empty();
      String safe = source == null ? "" : source;
      String[] lines = safe.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (int i = 0; i < lines.length; i++) {
         if (i > 0) {
            this.init_V_MutableComponent(out, "\n", 7829367);
         }

         String line = lines[i];
         int split = this.init_I_Str(line);
         if (split < 0) {
            this.init_V_MutableComponent(out, line, line.strip().startsWith("#") ? 7829367 : 16316658);
         } else {
            String key = line.substring(0, split);
            String sep = line.substring(split, split + 1);
            String value = line.substring(split + 1);
            this.init_V_MutableComponent(out, key, !this.load_Z_Str(key) && !key.toLowerCase(Locale.ROOT).contains("hex") ? 10936878 : 6740463);
            this.init_V_MutableComponent(out, sep, 11184810);
            if (key.toLowerCase(Locale.ROOT).contains("hex")) {
               this.init_V_MutableComponent(out, value, 15129460);
            } else {
               this.init_V_MutableComponent_Str(out, value);
            }
         }
      }

      return out;
   }

   private void init_V_MutableComponent_Str(MutableComponent out, String valuePart) {
      if (valuePart != null && !valuePart.isEmpty()) {
         int leading = 0;

         while (leading < valuePart.length() && Character.isWhitespace(valuePart.charAt(leading))) {
            leading++;
         }

         if (leading > 0) {
            this.init_V_MutableComponent(out, valuePart.substring(0, leading), 11184810);
         }

         String value = valuePart.substring(leading);
         int color = this.read_Z(value) ? 11436543 : (this.write_Z(value) ? 16328306 : 15129460);
         this.init_V_MutableComponent(out, value, color);
      }
   }

   private void init_V_MutableComponent(MutableComponent out, String text, int color) {
      if (text != null && !text.isEmpty()) {
         out.append(autismclient.gui.f.b.k.init(text, this.init_CharEncoder.init_Identifier_l(l.read), color));
      }
   }

   private int init_I_Str(String line) {
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

   private PayloadFieldCodec.Inner_BufferHelper init_bJ_BufferHelper_Str(String token) {
      if (token != null && !token.isBlank()) {
         String normalized = token.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

         for (PayloadFieldCodec.Inner_BufferHelper type : PayloadFieldCodec.Inner_BufferHelper.values()) {
            if (type.name().equals(normalized) || type.init().equalsIgnoreCase(token.strip())) {
               return type;
            }
         }

         if ("UUID".equals(normalized)) {
            return PayloadFieldCodec.Inner_BufferHelper.UUID_FIELD;
         } else if ("UTF".equals(normalized) || "WRITEUTF".equals(normalized)) {
            return PayloadFieldCodec.Inner_BufferHelper.JAVA_WRITE_UTF;
         } else if (!"MC_STRING".equals(normalized) && !"MINECRAFT_STRING".equals(normalized)) {
            return !"COMPONENT".equals(normalized) && !"TEXT_COMPONENT".equals(normalized) ? null : PayloadFieldCodec.Inner_BufferHelper.TEXT_COMPONENT;
         } else {
            return PayloadFieldCodec.Inner_BufferHelper.MINECRAFT_STRING;
         }
      } else {
         return null;
      }
   }

   private boolean init_Z_C(char chr) {
      return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
   }

   private boolean read_Z(String value) {
      String text = value == null ? "" : value.strip();
      return text.matches("-?(0x[0-9a-fA-F]+|\\d+)(\\.\\d+)?");
   }

   private boolean write_Z(String value) {
      String text = value == null ? "" : value.strip();
      return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
   }

   private void init_V_I_2(int x, int y, int w, int mx, int my, PayloadFieldCodec.Inner_JsonUtil template) {
      int idx = this.init_I_bJ_StreamUtil(template == null ? PayloadFieldCodec.Inner_StreamUtil.ADVANCED_MIXED : template.init_bJ_StreamUtil());
      autismclient.gui.f.b.SyncManager dd = this.run_Map
         .computeIfAbsent("payload_mode", key -> new autismclient.gui.f.b.SyncManager(x, y, w, 13, put_List, idx, newIdx -> {
            if (newIdx >= 0 && newIdx < add_List.size()) {
               this.push(true);
               this.clear_Z = true;
               this.read_Map.put("payload_mode", newIdx);
               if (this.init_H != null) {
                  this.init_H.payloadEncodingMode = add_List.get(newIdx).name();
               }

               if (this.init_bE != null) {
                  this.init_bE.sync = add_List.get(newIdx).name();
                  this.init_bE.init_V();
                  this.init_ActionEditorOverla = null;
                  this.run(false);
               }

               this.get_Z = true;
            }
         }));
      dd.init_SyncManager_I(x, y, w, 13).init_SyncManager_List(put_List).init_SyncManager_I(idx).init_SyncManager_IntConsumer(newIdx -> {
         if (newIdx >= 0 && newIdx < add_List.size()) {
            this.push(true);
            this.clear_Z = true;
            this.read_Map.put("payload_mode", newIdx);
            if (this.init_H != null) {
               this.init_H.payloadEncodingMode = add_List.get(newIdx).name();
            }

            if (this.init_bE != null) {
               this.init_bE.sync = add_List.get(newIdx).name();
               this.init_bE.init_V();
               this.init_ActionEditorOverla = null;
               this.run(false);
            }

            this.get_Z = true;
         }
      });
      this.sync_List.add(dd);
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int x, int y, int w, int row, int mx, int my, float delta) {
      int btnH = 13;
      int typeW = this.init_I_List_I(set_List, Math.min(116, Math.max(88, (w - 70) / 2)));
      int gap = 3;
      int actionW = 19;
      int actionsW = actionW * 3 + gap * 2;
      int valueX = x + typeW + gap;
      int valueW = Math.max(36, w - typeW - gap - actionsW - gap);
      this.init_V_I(row, x, y + 2, typeW, mx, my);
      ThemedTextInputBox valueField = this.init_Map.get(this.load_Str_I(row));
      if (valueField != null) {
         PayloadFieldCodec.Inner_BufferHelper type = this.init_bJ_BufferHelper_I(row);
         valueField.init_V_Component(Component.literal(type.load()));
         valueField.read(valueX);
         valueField.write(y + 3);
         valueField.update(valueW);
         valueField.init_V(ctx, mx, my, delta);
      }

      int ax = valueX + valueW + gap;
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, ax, y + 2, actionW, btnH, "Up", ByteReader.Inner_StreamUtil.SECONDARY, row > 0, mx, my, () -> this.update_V(row, -1)
      );
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, ax + actionW + gap, y + 2, actionW, btnH, "Dn", ByteReader.Inner_StreamUtil.SECONDARY, row < this.f_h_I - 1, mx, my, () -> this.update_V(row, 1)
      );
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, ax + (actionW + gap) * 2, y + 2, actionW, btnH, "X", ByteReader.Inner_StreamUtil.DANGER, this.f_h_I > 1, mx, my, () -> this.write_V(row)
      );
      return y + 18;
   }

   private void init_V_I(int row, int x, int y, int w, int mx, int my) {
      String key = this.init_Str_I(row);
      int idx = Math.max(0, Math.min(this.read_Map.getOrDefault(key, this.init_I_bJ_BufferHelper(PayloadFieldCodec.Inner_BufferHelper.STRING_BYTES)), set_List.size() - 1));
      autismclient.gui.f.b.SyncManager dd = this.run_Map
         .computeIfAbsent(key, cacheKey -> new autismclient.gui.f.b.SyncManager(x, y, w, 13, set_List, idx, newIdx -> this.write_V_I_I(row, newIdx)));
      dd.init_SyncManager_I(x, y, w, 13)
         .init_SyncManager_List(set_List)
         .init_SyncManager_I(idx)
         .init_SyncManager_IntConsumer(newIdx -> this.write_V_I_I(row, newIdx));
      this.sync_List.add(dd);
   }

   private void write_V_I_I(int row, int newIdx) {
      if (newIdx >= 0 && newIdx < get_List.size()) {
         String key = this.init_Str_I(row);
         int previous = this.read_Map.getOrDefault(key, -1);
         this.read_Map.put(key, newIdx);
         if (previous != newIdx) {
            ThemedTextInputBox valueField = this.init_Map.get(this.load_Str_I(row));
            if (valueField != null && valueField.load_Str().isBlank()) {
               valueField.init_V_Str(get_List.get(newIdx).load());
            }

            this.get_Z = true;
         }
      }
   }

   private void update_V(int row, int direction) {
      List<PayloadFieldCodec.Inner_CachePool> fields = new ArrayList<>(this.load_List());
      int target = row + direction;
      if (row >= 0 && row < fields.size() && target >= 0 && target < fields.size()) {
         Collections.swap(fields, row, target);
         this.load_V_List(fields);
         this.get_Z = true;
         this.fill();
      }
   }

   private void write_V(int row) {
      List<PayloadFieldCodec.Inner_CachePool> fields = new ArrayList<>(this.load_List());
      if (fields.size() > 1 && row >= 0 && row < fields.size()) {
         fields.remove(row);
         this.load_V_List(fields);
         this.get_Z = true;
         this.fill();
      }
   }

   private PayloadFieldCodec.Inner_JsonUtil init_bJ_JsonUtil() {
      if (this.init_bE != null) {
         return this.load_bJ_JsonUtil();
      }

      PayloadAction action = new PayloadAction();
      if (this.init_H != null) {
         action.fromTag(this.init_H.toTag());
      }

      ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
      if (channelField != null && !channelField.load_Str().isBlank()) {
         action.channel = channelField.load_Str().strip();
      }

      action.payloadPhase = "PLAY";
      action.payloadEncodingMode = this.init_bJ_StreamUtil_Str(action.channel).name();
      action.payloadFields = PayloadFieldCodec.init(this.load_List());
      return PayloadFieldCodec.init(action);
   }

   private ActionEditorOverlay.ConfigStore init_ActionEditorOverla() {
      if (this.init_bE == null) {
         PayloadFieldCodec.Inner_JsonUtil template = this.init_bJ_JsonUtil();
         return new ActionEditorOverlay.ConfigStore(-9223372036854775808L, template, template.init_bJ_DataUtil());
      }

      long revision = this.init_bE.init_J();
      ActionEditorOverlay.ConfigStore hit = this.init_ActionEditorOverla;
      if (hit != null && hit.init_J() == revision) {
         return hit;
      }

      PayloadFieldCodec.Inner_JsonUtil template = this.load_bJ_JsonUtil();
      hit = new ActionEditorOverlay.ConfigStore(revision, template, template.init_bJ_DataUtil());
      this.init_ActionEditorOverla = hit;
      return hit;
   }

   private PayloadFieldCodec.Inner_JsonUtil load_bJ_JsonUtil() {
      return this.init_bE == null ? PayloadFieldCodec.init(new PayloadAction()) : this.init_bE.init_bJ_JsonUtil();
   }

   private void write_V(String line) {
      ThemedTextInputBox contentField = this.init_Map.get("payload_content");
      if (contentField != null && line != null && !line.isBlank()) {
         String current = contentField.load_Str() == null ? "" : contentField.load_Str().stripTrailing();
         contentField.init_V_Str(current.isBlank() ? line : current + "\n" + line);
         this.get_Z = true;
      }
   }

   private void init_V_bJ_JsonUtil(PayloadFieldCodec.Inner_JsonUtil template) {
      if (template != null) {
         if (this.init_H != null) {
            this.init_H.channel = template.init_Str();
            this.init_H.payloadDirection = template.init_bJ_ConfigStore().name();
            this.init_H.payloadPhase = template.init_bJ_TaskQueue().name();
            this.init_H.payloadEncodingMode = template.init_bJ_StreamUtil().name();
            this.init_H.payloadFields = PayloadFieldCodec.init(template.init_List());
         }

         this.start_Z = true;

         try {
            if (this.init_bE != null) {
               this.init_bE.read_Str = template.init_Str();
               this.init_bE.init_Str = template.init_bJ_ConfigStore().name();
               this.init_bE.load_Str = template.init_bJ_TaskQueue().name();
               this.init_bE.sync = template.init_bJ_StreamUtil().name();
               this.init_bE.init_List = new ArrayList<>(template.init_List());
               this.init_bE.init_V();
               this.init_bE.init_Z_Str(this.init_bE.init_Str());
               this.init_ActionEditorOverla = null;
            }

            ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
            if (channelField != null) {
               channelField.init_V_Str(template.init_Str());
            }

            if (this.init_bE == null) {
               this.load_V_List(template.init_List());
            } else {
               this.pop(true);
            }
         } finally {
            this.start_Z = false;
         }

         this.put_Z = true;
         this.get_Z = true;
         this.fill();
      }
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      String status = this.sync_Str();
      String transport = this.init_bE == null
         ? "Encoder: vanilla"
         : this.init_bE.init_Str + " / " + this.init_bE.load_Str + (this.init_bE.init_I >= 0 ? " / id " + this.init_bE.init_I : " / id ?");
      autismclient.gui.f.b.k.init(
         ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, status, Math.max(10, w), font, UiColorPalette.scan()), font, UiColorPalette.scan(), x, y + 2, false
      );
      y += 11;
      autismclient.gui.f.b.k.init(
         ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, transport, Math.max(10, w), font, UiColorPalette.bind()), font, UiColorPalette.bind(), x, y + 2, false
      );
      return y + 13;
   }

   private String sync_Str() {
      if (this.init_bE != null) {
         String decoded = this.init_bE.close == null ? "payload" : this.init_bE.close;
         int bytes = this.init_bE.init_BArr == null ? 0 : this.init_bE.init_BArr.length;
         return !this.get_Z && !this.put_Z ? "Ready: " + bytes + "B " + decoded : "Edited: " + bytes + "B " + decoded;
      } else if (this.get_Z) {
         return "Edited: script rebuilds bytes";
      } else {
         return this.put_Z ? "Edited: channel changed" : "Payload builder ready";
      }
   }

   private String flush_Str() {
      String var10000;
      switch (this.init_ActionEditorOverla_3) {
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

   private String close_Str() {
      return "Rows run top to bottom.";
   }

   private boolean start_Z() {
      return this.init_bE != null && this.init_bE.load_Str != null
         ? this.init_bE.load_Str.toLowerCase(Locale.ROOT).contains("config")
         : this.init_H != null && this.init_H.payloadPhase != null && this.init_H.payloadPhase.toLowerCase(Locale.ROOT).contains("config");
   }

   private void cache() {
      this.init_ActionEditorOverla_2 = null;
      this.init_bE = PayloadEditorModel.init_bE_H(this.init_H);
      this.write_J = -9223372036854775808L;
      this.init_ActionEditorOverla = null;
      ActionEditorOverlay.JsonUtil state = this.init_ActionEditorOverla_H(this.init_H);
      this.init_ActionEditorOverla_3 = state.init_ActionEditorOverla();
      this.start_Z = true;

      try {
         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         if (channelField != null) {
            channelField.init_V_Str(state.init_Str());
         }

         this.pop(true);
         this.put_Z = false;
         this.get_Z = false;
         this.set_Z = false;
         this.add_Z = false;
         this.clear_Z = false;
         this.reset_Z = false;
      } finally {
         this.start_Z = false;
      }

      this.data();
   }

   private PayloadAction init_H() {
      this.push(true);
      if (this.init_bE != null) {
         if (this.init_bE.flush != null && !this.init_bE.flush.isBlank()) {
            throw new IllegalArgumentException(this.init_bE.flush);
         }

         PayloadAction action = this.init_bE.init_H_H(this.init_H);
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = "minecraft:brand";
         }

         PayloadFieldCodec.Inner_DataUtil built = this.init_bE.init_bJ_JsonUtil().init_bJ_DataUtil();
         if (!built.init_Z()) {
            throw new IllegalArgumentException(String.join("; ", built.load()));
         }

         action.payloadData = AutismPayloadSupport.init_Str(built.init_BArr());
         return action;
      } else {
         PayloadAction action = new PayloadAction();
         if (this.init_H != null) {
            action.fromTag(this.init_H.toTag());
         }

         action.payloadClassName = "";
         action.commandApiOverride = false;
         action.javaSource = "";
         action.payloadScriptEnabled = false;
         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         if (channelField != null) {
            String editorChannel = channelField.load_Str().strip();
            if (!editorChannel.isBlank()) {
               action.channel = editorChannel;
            }
         }

         action.payloadFields = PayloadFieldCodec.init(this.load_List());
         if (action.payloadDirection == null || action.payloadDirection.isBlank()) {
            action.payloadDirection = "C2S";
         }

         action.payloadPhase = "PLAY";
         action.payloadEncodingMode = this.init_bJ_StreamUtil_Str(action.channel).name();
         PayloadFieldCodec.Inner_JsonUtil template = PayloadFieldCodec.init(action);
         PayloadFieldCodec.Inner_DataUtil built = template.init_bJ_DataUtil();
         if (!built.init_Z()) {
            throw new IllegalArgumentException(String.join("; ", built.load()));
         }

         action.channel = template.init_Str();
         action.payloadDirection = template.init_bJ_ConfigStore().name();
         action.payloadPhase = "PLAY";
         action.payloadEncodingMode = template.init_bJ_StreamUtil().name();
         action.payloadFields = PayloadFieldCodec.init(template.init_List());
         action.sourceDirection = action.payloadDirection;
         action.sourceProtocol = "";
         action.payloadData = AutismPayloadSupport.init_Str(built.init_BArr());
         action.payloadJson = "";
         action.commandApiRecognized = false;
         action.commandApiOverride = false;
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = this.init_H != null && this.init_H.channel != null && !this.init_H.channel.isBlank() ? this.init_H.channel : "minecraft:brand";
         }

         return action;
      }
   }

   private void open(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      if (this.init_H == null && this.init_MacroAction == null) {
         Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
         int cy = bodyTop + 4;
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "Custom Payload Editor", font, UiColorPalette.parse(), x, cy + 2, false);
         cy += 13;
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "Packet JSON", font, UiColorPalette.parse(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox jsonField = this.init_Map.get("payload_json");
         if (jsonField != null) {
            jsonField.read(x);
            jsonField.write(cy);
            jsonField.update(w);
            jsonField.sync(74);
            jsonField.init_V(ctx, mx, my, delta);
            cy += 78;
         }

         int[] jsonBtns = this.init_IArr_I(x, w, false, "Pretty JSON", "Apply JSON");
         this.init_V_GuiGraphicsExtract(ctx, jsonBtns[0], cy, jsonBtns[1], 13, "Pretty JSON", mx, my, () -> {
            this.open(false);
            this.fill();
         });
         this.init_V_GuiGraphicsExtract(ctx, jsonBtns[2], cy, jsonBtns[3], 13, "Apply JSON", mx, my, () -> {
            this.poll(true);
            this.fill();
         });
         cy += 18;
         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         if (channelField != null) {
            int lw = this.init_I_I(w, "Channel", font, 92);
            this.init_V_GuiGraphicsExtract_3(ctx, "Channel", x, cy, lw, font);
            int ctrlX = this.flush_I(x, lw);
            int ctrlW = this.close(w, lw);
            channelField.read(ctrlX);
            channelField.write(cy + 2);
            channelField.update(ctrlW);
            channelField.init_V(ctx, mx, my, delta);
            cy += 16;
         }

         if (this.load_Map.getOrDefault("payload_command_api", false)) {
            ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
            if (commandField != null) {
               int lw = this.init_I_I(w, "CommandApi", font, 92);
               this.init_V_GuiGraphicsExtract_3(ctx, "CommandApi", x, cy, lw, font);
               int ctrlX = this.flush_I(x, lw);
               int ctrlW = this.close(w, lw);
               int btnW = this.write_I("Sync");
               int fieldW = Math.max(32, ctrlW - btnW - 2);
               commandField.read(ctrlX);
               commandField.write(cy + 2);
               commandField.update(fieldW);
               commandField.init_V(ctx, mx, my, delta);
               this.init_ByteReader_GuiGraphicsExtract_2(
                  ctx, ctrlX + fieldW + 2, cy + 2, btnW, 13, "Sync", ByteReader.Inner_StreamUtil.GHOST, true, mx, my, () -> {
                     this.queue();
                     this.fill();
                  }
               );
               cy += 16;
            }
         }

         autismclient.gui.f.b.k.init(ctx, this.init_Font, "UTF-8 Component", font, UiColorPalette.parse(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox textField = this.init_Map.get("payload_text");
         if (textField != null) {
            textField.read(x);
            textField.write(cy);
            textField.update(w);
            textField.sync(30);
            textField.init_V(ctx, mx, my, delta);
            cy += 34;
         }

         int[] legacyBtns = this.init_IArr_I(x, w, false, "Component -> Hex", "Hex -> Component");
         this.init_V_GuiGraphicsExtract(ctx, legacyBtns[0], cy, legacyBtns[1], 13, "Component -> Hex", mx, my, () -> {
            this.entry();
            this.fill();
         });
         this.init_V_GuiGraphicsExtract(ctx, legacyBtns[2], cy, legacyBtns[3], 13, "Hex -> Component", mx, my, () -> {
            this.buffer();
            this.fill();
         });
         cy += 18;
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "Raw Payload", font, UiColorPalette.parse(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox rawField = this.init_Map.get("payload_data");
         if (rawField != null) {
            rawField.read(x);
            rawField.write(cy);
            rawField.update(w);
            rawField.sync(42);
            rawField.init_V(ctx, mx, my, delta);
            cy += 46;
         }

         int[] parseBtns = this.init_IArr_I(x, w, false, "Parse Int", "Normalize");
         this.init_V_GuiGraphicsExtract(ctx, parseBtns[0], cy, parseBtns[1], 13, "Parse Int", mx, my, () -> {
            this.state();
            this.fill();
         });
         this.init_V_GuiGraphicsExtract(ctx, parseBtns[2], cy, parseBtns[3], 13, "Normalize", mx, my, () -> {
            ThemedTextInputBox field = this.init_Map.get("payload_data");
            if (field != null) {
               try {
                  field.init_V_Str(AutismPayloadSupport.init_Str(AutismPayloadSupport.init_BArr(field.load_Str())));
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.init(e));
               }
            }
         });
         cy += 18;
         String sendNowLbl = this.close_Z ? "Send: In Game" : "Send Now";
         int[] seedBtns = this.init_IArr_I(x, w, false, "Seed From Int", sendNowLbl);
         this.init_V_GuiGraphicsExtract(ctx, seedBtns[0], cy, seedBtns[1], 13, "Seed From Int", mx, my, () -> {
            this.queue();
            this.fill();
         });
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, seedBtns[2], cy, seedBtns[3], 13, sendNowLbl, ByteReader.Inner_StreamUtil.SUCCESS, !this.close_Z, mx, my, () -> {
               if (!this.close_Z) {
                  try {
                     PayloadAction action = this.load_H();
                     action.execute(init_Minecraft);
                  } catch (Exception e) {
                     AutismClientMessaging.sendPrefixed("§cPayload send failed: " + AutismPayloadSupport.init(e));
                  }
               }
            }
         );
         cy += 20;
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "Runtime Java", font, UiColorPalette.parse(), x, cy + 2, false);
         cy += 13;
         ThemedTextInputBox javaField = this.init_Map.get("payload_java");
         if (javaField != null) {
            javaField.read(x);
            javaField.write(cy);
            javaField.update(w);
            javaField.sync(48);
            javaField.init_V(ctx, mx, my, delta);
            cy += 52;
         }

         this.init_I_GuiGraphicsExtract(
            ctx, x, cy, w, "Raw replay is exact by default. Use Apply JSON only when you want the JSON view to rebuild the raw bytes."
         );
      } else {
         this.close(ctx, x, bodyTop, w, mx, my, delta);
      }
   }

   private PayloadAction load_H() {
      if (this.init_H == null && this.init_MacroAction == null) {
         PayloadAction action = new PayloadAction();
         if (this.init_H != null) {
            action.fromTag(this.init_H.toTag());
         }

         action.payloadClassName = "";
         ThemedTextInputBox jsonField = this.init_Map.get("payload_json");
         if (jsonField != null) {
            action.payloadJson = jsonField.load_Str();
         }

         boolean jsonUsable = false;
         PayloadJsonCodec.Inner_DataUtil jsonEncoded = null;
         if (action.payloadJson != null && !action.payloadJson.isBlank()) {
            action.payloadJson = PayloadJsonCodec.init_Str(action.payloadJson);

            try {
               jsonEncoded = PayloadJsonCodec.init_bG_DataUtil(action);
               jsonUsable = true;
            } catch (Exception var13) {
            }
         }

         ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
         if (channelField != null) {
            String editorChannel = channelField.load_Str().strip();
            if (!editorChannel.isBlank()) {
               action.channel = editorChannel;
            }
         }

         ThemedTextInputBox rawField = this.init_Map.get("payload_data");
         if (rawField != null) {
            action.payloadData = rawField.load_Str();
         }

         if (action.payloadData == null || action.payloadData.isBlank()) {
            ThemedTextInputBox textField = this.init_Map.get("payload_text");
            if (textField != null && !textField.load_Str().isBlank()) {
               action.payloadData = AutismPayloadSupport.init_Str(textField.load_Str().getBytes(StandardCharsets.UTF_8));
            }
         }

         ThemedTextInputBox javaField = this.init_Map.get("payload_java");
         if (javaField != null) {
            action.javaSource = javaField.load_Str();
         }

         action.commandApiRecognized = this.load_Map.getOrDefault("payload_command_api", false);
         ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
         if (commandField != null) {
            try {
               action.commandApiValue = Integer.parseInt(commandField.load_Str().strip());
            } catch (NumberFormatException var12) {
            }
         }

         if (this.add_Z && jsonUsable && jsonEncoded != null && !this.set_Z && !this.put_Z) {
            try {
               jsonEncoded = PayloadJsonCodec.init_bG_DataUtil(action);
               action.channel = jsonEncoded.init_Str();
               action.payloadData = AutismPayloadSupport.init_Str(jsonEncoded.init_BArr());
            } catch (Exception var11) {
            }
         }

         if (action.commandApiRecognized) {
            try {
               byte[] bytes = AutismPayloadSupport.init_BArr(action.payloadData);
               action.payloadData = AutismPayloadSupport.init_Str(AutismPayloadSupport.init_BArr(bytes, action.commandApiValue));
            } catch (Exception var10) {
            }
         }

         action.payloadClassName = "";
         if (action.channel == null || action.channel.isBlank()) {
            action.channel = this.init_H != null && this.init_H.channel != null && !this.init_H.channel.isBlank() ? this.init_H.channel : "minecraft:brand";
         }

         return action;
      } else {
         return this.init_H();
      }
   }

   private void state() {
      ThemedTextInputBox rawField = this.init_Map.get("payload_data");
      ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
      ThemedTextInputBox channelField = this.init_Map.get("payload_channel");
      if (rawField != null && commandField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.init_BArr(rawField.load_Str());
            Integer value = AutismPayloadSupport.init_Integer(null, channelField == null ? "" : channelField.load_Str(), bytes);
            if (value == null) {
               AutismClientMessaging.sendPrefixed("§cCould not parse a CommandApi integer from the current payload bytes.");
               return;
            }

            this.load_Map.put("payload_command_api", true);
            commandField.init_V_Str(String.valueOf(value));
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.init(e));
         }
      }
   }

   private void queue() {
      ThemedTextInputBox rawField = this.init_Map.get("payload_data");
      ThemedTextInputBox commandField = this.init_Map.get("payload_command_value");
      if (rawField != null && commandField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.init_BArr(rawField.load_Str());
            int value = Integer.parseInt(commandField.load_Str().strip());
            this.load_Map.put("payload_command_api", true);
            rawField.init_V_Str(AutismPayloadSupport.init_Str(AutismPayloadSupport.init_BArr(bytes, value)));
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cFailed to sync CommandApi value: " + AutismPayloadSupport.init(e));
         }
      }
   }

   private void buffer() {
      ThemedTextInputBox rawField = this.init_Map.get("payload_data");
      ThemedTextInputBox textField = this.init_Map.get("payload_text");
      if (rawField != null && textField != null) {
         try {
            byte[] bytes = AutismPayloadSupport.init_BArr(rawField.load_Str());
            String text = AutismPayloadSupport.write(bytes);
            if (text.isBlank()) {
               AutismClientMessaging.sendPrefixed("§cThe current payload bytes do not look like readable UTF-8 text.");
               return;
            }

            textField.init_V_Str(text);
         } catch (Exception e) {
            AutismClientMessaging.sendPrefixed("§cInvalid payload data: " + AutismPayloadSupport.init(e));
         }
      }
   }

   private void entry() {
      ThemedTextInputBox rawField = this.init_Map.get("payload_data");
      ThemedTextInputBox textField = this.init_Map.get("payload_text");
      if (rawField != null && textField != null) {
         rawField.init_V_Str(AutismPayloadSupport.init_Str(textField.load_Str().getBytes(StandardCharsets.UTF_8)));
      }
   }

   private void update_V(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      int btnH = 13;
      int[] presetBtns = this.init_IArr_I(x, w, false, "Default", "Module");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, presetBtns[0], y + 2, presetBtns[1], btnH, "Default", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> {
            autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
            tmp.read();
            this.write_Map.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
            this.write_Map.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
         }
      );
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, presetBtns[2], y + 2, presetBtns[3], btnH, "Module", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, () -> {
            autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
            tmp.write();
            this.write_Map.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
            this.write_Map.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
         }
      );
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract_2(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, ByteReader.Inner_StreamUtil variant, boolean active, int mx, int my, Runnable action
   ) {
      return this.init_ByteReader_GuiGraphicsExtract(ctx, x, y, w, h, label, variant, active, mx, my, action, null, null, null);
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      ByteReader.Inner_StreamUtil variant,
      boolean active,
      int mx,
      int my,
      Runnable action,
      Runnable secondaryAction
   ) {
      return this.init_ByteReader_GuiGraphicsExtract(ctx, x, y, w, h, label, variant, active, mx, my, action, secondaryAction, null, null);
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract_3(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, boolean toggleState, String animationKey, int mx, int my, Runnable action
   ) {
      return this.init_ByteReader_GuiGraphicsExtract(
         ctx,
         x,
         y,
         w,
         h,
         label,
         toggleState ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.DANGER,
         true,
         mx,
         my,
         action,
         null,
         toggleState,
         animationKey
      );
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      String label,
      ByteReader.Inner_StreamUtil variant,
      boolean active,
      int mx,
      int my,
      Runnable action,
      Runnable secondaryAction,
      Boolean toggleState,
      String animationKey
   ) {
      ByteReader button = ByteReader.init(
         x, y, w, h, Component.literal(label), ignored -> action.run(), secondaryAction == null ? null : ignored -> secondaryAction.run()
      );
      button.init_ByteReader_ByteReader_StreamU(variant);
      if (toggleState != null) {
         button.init_ByteReader_Z(toggleState).init_ByteReader_Str(animationKey);
      }

      button.init_Z = active;
      ByteReader.init(ctx, this.init_Font, button, mx, my);
      this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(button, action));
      return button;
   }

   private static int init_I_ByteReader_StreamU_Z(ByteReader.Inner_StreamUtil variant, boolean active) {
      if (!active) {
         return -2009060816;
      }

      int var10000;
      switch (variant == null ? ByteReader.Inner_StreamUtil.SECONDARY : variant) {
         case SUCCESS:
            var10000 = ThemePaletteManager.init_I_I_ct_DataUtil(-13248397, ThemePaletteManager.Inner_DataUtil.SUCCESS);
            break;
         case DANGER:
            var10000 = ThemePaletteManager.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.DANGER);
            break;
         case PRIMARY:
         case FILTER_ON:
            var10000 = ThemePaletteManager.init_I_I_ct_DataUtil(-42149, ThemePaletteManager.Inner_DataUtil.BUTTON);
            break;
         case GHOST:
         case FILTER_OFF:
         case SECONDARY:
            var10000 = ThemePaletteManager.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.Inner_DataUtil.OUTLINE);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx, int x, int y, int w, int h, StreamUtil mode, boolean capturing, boolean active, int mx, int my, Runnable action
   ) {
      return this.init_ByteReader_GuiGraphicsExtract(ctx, x, y, w, h, mode, capturing, active, mx, my, null, null, action);
   }

   private ByteReader init_ByteReader_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int h,
      StreamUtil mode,
      boolean capturing,
      boolean active,
      int mx,
      int my,
      String idleLabel,
      String capturingLabel,
      Runnable action
   ) {
      if (!this.init_Z_StreamUtil(mode)) {
         return null;
      }

      ByteReader button = autismclient.gui.macro.editor.a.DataUtil.init(
         ctx, this.init_Font, autismclient.gui.f.NetHelper.init(x, y, w, h), mode, capturing, active, mx, my, idleLabel, capturingLabel, action
      );
      this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(button, action));
      return button;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int x, int y, int size, int mx, int my, Runnable action) {
      boolean hovered = mx >= x && mx < x + size && my >= y && my < y + size;
      autismclient.gui.macro.editor.a.CachePool.init(ctx, autismclient.gui.f.NetHelper.init(x, y, size, size), hovered);
      this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, y, size, size, (mouseX, mouseY, mouseButton) -> {
         if (mouseButton != 0) {
            return false;
         }

         action.run();
         return true;
      }));
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, int mx, int my, Runnable action) {
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, x, y, w, h, label, ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, action);
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, String text) {
      return this.init_I_GuiGraphicsExtract_2(ctx, x, y, w, text, UiColorPalette.bind());
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int x, int y, int w, String text, int color) {
      String hint = this.write_Str(text);
      if (hint.isEmpty()) {
         return y;
      }

      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      List<String> lines = this.init_List_Str(hint, Math.max(10, w), font, color);
      int cy = y;

      for (String line : lines) {
         autismclient.gui.f.b.k.init(ctx, this.init_Font, line, font, color, x, cy + 2, false);
         cy += 12;
      }

      return cy;
   }

   private List<String> init_List_Str(String hint, int maxWidth, Identifier font, int color) {
      if (hint != null && !hint.isBlank()) {
         String safe = hint.trim().replaceAll("\\s+", " ");
         if (autismclient.gui.f.b.k.init(this.init_Font, safe, font, color) <= maxWidth) {
            return List.of(safe);
         }

         String[] words = safe.split(" ");
         StringBuilder first = new StringBuilder();
         StringBuilder second = new StringBuilder();

         for (String word : words) {
            String candidate = first.length() == 0 ? word : first + " " + word;
            if (second.length() == 0 && autismclient.gui.f.b.k.init(this.init_Font, candidate, font, color) <= maxWidth) {
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
            String secondLine = autismclient.gui.f.b.k.init(this.init_Font, second.toString(), font, color) <= maxWidth
               ? second.toString()
               : autismclient.gui.f.b.k.init(this.init_Font, second.toString(), maxWidth, font, color);
            return List.of(first.toString(), secondLine);
         } else {
            return List.of(autismclient.gui.f.b.k.init(this.init_Font, safe, maxWidth, font, color));
         }
      } else {
         return List.of();
      }
   }

   private String write_Str(String text) {
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
         return this.sync_Str(exact);
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
      return this.sync_Str(normalized);
   }

   private void node() {
      if (this.init_ActionFieldSchema != null) {
         for (FieldDef field : this.init_ActionFieldSchema.fields()) {
            switch (field.type()) {
               case TEXT:
               case MACRO_SELECT:
               case NUMBER:
               case DECIMAL:
               case SLOT:
                  this.init_V_Str_Str_2(field.key(), this.init_Str_FieldDef(field));
                  break;
               case BLOCK_POS:
                  this.update_V(field.key());
            }
         }
      }

      this.init_V_Str_Str_2("item_guiName", "Any GUI");
      this.init_V_Str_Str_2("drop_guiName", "Any GUI");
      this.init_V_Str_Str_2("lan_defaultStep", "Any step");
      this.init_V_Str_Str_2("item_entrySlot", "Slot");
      this.init_V_Str_Str_2("place_itemSlot", "Slot");
      this.init_V_Str_Str_2("drop_entrySlot", "Slot #");
      this.init_V_Str_Str_2("_craft_amount", "1");
      this.init_V_Str_Str_2("_lan_step_add", "1");
      this.init_V_Str_Str_2("drop_globalCount", "1");
      this.init_V_Str_Str_2("amountInput", "1k");
      this.load_V_Str_Str("_item_add", "Any item");
      this.load_V_Str_Str("_drop_add", "Any item");
      this.load_V_Str_Str("_craft_search", "Find recipe");
      this.load_V_Str_Str("_lan_user_add", "Peer name");
      this.load_V_Str_Str("soundIds", "Find sound");
      this.load_V_Str_Str("entityIds", "Find entity");
      this.load_V_Str_Str("targetItems", "Item name");
      this.load_V_Str_Str("players", "Find player");
      this.load_V_Str_Str("_toggle_module_search", "Find module");
      this.load_V_Str_Str("_wait_chat_search", "Find chat");
   }

   private void init_V_Str_Str_2(String key, String placeholder) {
      ThemedTextInputBox field = this.init_Map.get(key);
      this.init_V_k_Str(field, placeholder);
   }

   private void load_V_Str_Str(String key, String placeholder) {
      ThemedTextInputBox field = this.flush_Map.get(key);
      this.init_V_k_Str(field, placeholder);
   }

   private void update_V(String key) {
      this.init_V_Str_Str_2(key + "_0", "X");
      this.init_V_Str_Str_2(key + "_1", "Y");
      this.init_V_Str_Str_2(key + "_2", "Z");
   }

   private void init_V_k_Str(ThemedTextInputBox field, String placeholder) {
      if (field != null && placeholder != null && !placeholder.isBlank()) {
         field.init_V_Component(Component.literal(this.update_Str(placeholder)));
      }
   }

   private String init_Str_FieldDef(FieldDef field) {
      if (field == null) {
         return "";
      }

      if (this.init_MacroAction instanceof ToolsAction && field.key().startsWith("toolArg.")) {
         String name = field.key().substring("toolArg.".length());
         autismclient.util.macro.MacroToolBridge.Inner_TaskQueue tool = autismclient.util.macro.MacroToolBridge.init_k_TaskQueue(this.tick_Str("toolId"));
         if (tool != null) {
            JsonObject definition = JsonParser.parseString(tool.read()).getAsJsonObject();
            if (definition.has("required")) {
               for (JsonElement required : definition.getAsJsonArray("required")) {
                  if (name.equals(required.getAsString())) {
                     return BufferHelper.init(name);
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
               var10000 = this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.NBT_BOOK ? "Book title" : "Optional title";
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
                     var10000 = this.update_Str(field.label());
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

   private String update_Str(String text) {
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

   private String sync_Str(String text) {
      return text == null ? "" : text.trim().replaceAll("\\s+", " ");
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      if (this.sync_Z(field.key())) {
         this.load(ctx, this.init_CharEncoder.init_Identifier_l(l.read), field.key(), "waitForGuiAfter", x, y, w, mx, my);
      } else if ("safeClickDelayTicks".equals(field.key())) {
         this.close(ctx, x, y, w, mx, my);
      } else {
         switch (field.type()) {
            case TEXT:
            case MACRO_SELECT:
            case NUMBER:
            case DECIMAL:
            case SLOT:
               this.load(ctx, field, x, y, w, mx, my, delta);
               break;
            case BLOCK_POS:
               this.read(ctx, field, x, y, w, mx, my, delta);
               break;
            case TOGGLE:
               this.write_V_GuiGraphicsExtract_2(ctx, field, x, y, w, mx, my);
               break;
            case ENUM:
               this.update_V_GuiGraphicsExtract_2(ctx, field, x, y, w, mx, my);
               break;
            case STRING_LIST:
               if (this.init_Z_FieldDef(field)) {
                  this.load_V_GuiGraphicsExtract_2(ctx, field, x, y, w, mx, my);
               } else if (field.captureMode() == StreamUtil.BLOCK_CATALOG) {
                  this.update(ctx, field, x, y, w, mx, my, delta);
               } else {
                  this.write(ctx, field, x, y, w, mx, my, delta);
               }
               break;
            case TARGET_SUMMARY:
               this.load(ctx, field, x, y, w);
               break;
            case CAPTURE_BUTTON:
               this.init_V_GuiGraphicsExtract_4(ctx, field, x, y, w, mx, my);
               break;
            case CONDITION:
               this.init_V_GuiGraphicsExtract(ctx, field, x, y, w);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int lw = this.init_I_I(w, field.label(), font, 56);
      this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, lw, font);
      String summary = this.flush_Str(field.key());
      int valX = this.flush_I(x, lw);
      int valW = this.close(w, lw);
      int color = UiColorPalette.scan();
      String trimmed = autismclient.gui.f.b.k.init(this.init_Font, summary, font, color) > valW
         ? autismclient.gui.f.b.k.init(this.init_Font, summary, Math.max(8, valW - 4), font, color)
         : summary;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, trimmed, font, color, valX, y + 2, false);
   }

   private String flush_Str(String conditionKey) {
      CompoundTag stored = this.init_CompoundTag != null && this.init_CompoundTag.get(conditionKey) instanceof CompoundTag c ? c : null;
      boolean hasFlatEditor = this.load_FieldDef_Str("cond_kind") != null;
      boolean flatEditable = hasFlatEditor;
      if (hasFlatEditor && stored != null) {
         try {
            flatEditable = MacroCondition.init_a_CompoundTag(stored).read_a() != null;
         } catch (Throwable ignored) {
            flatEditable = true;
         }
      }

      if (flatEditable && hasFlatEditor) {
         CompoundTag flat = new CompoundTag();
         flat.putString("cond_kind", this.tick_Str("cond_kind"));
         flat.putBoolean("cond_negate", this.load_Map.getOrDefault("cond_negate", false));
         flat.putString("cond_item", this.close_Str("cond_item"));
         flat.putString("cond_op", this.tick_Str("cond_op"));
         flat.putString("cond_value", this.close_Str("cond_value"));
         flat.putString("cond_cmp", this.tick_Str("cond_cmp"));
         flat.putInt("cond_amount", this.init_I_Str_I("cond_amount", 0));
         flat.putInt("cond_slot", this.init_I_Str_I("cond_slot", 0));
         flat.putString("cond_toolScope", this.tick_Str("cond_toolScope"));
         flat.putString("cond_toolState", this.tick_Str("cond_toolState"));
         flat.putString("cond_toolFilter", this.tick_Str("cond_toolFilter"));

         try {
            return MacroCondition.init_a_CompoundTag_Str(flat, "cond_").init_Str();
         } catch (Throwable var9) {
         }
      }

      if (stored != null) {
         try {
            return MacroCondition.init_a_CompoundTag(stored).init_Str();
         } catch (Throwable var8) {
         }
      }

      return "always";
   }

   private String close_Str(String key) {
      ThemedTextInputBox f = this.init_Map.get(key);
      return f == null ? "" : f.load_Str();
   }

   private int init_I_Str_I(String key, int fallback) {
      ThemedTextInputBox f = this.init_Map.get(key);
      if (f == null) {
         return fallback;
      }

      try {
         return Integer.parseInt(f.load_Str().strip());
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private void poll(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      ThemedTextInputBox nameF = this.init_Map.get("itemName");
      ThemedTextInputBox slotF = this.init_Map.get("place_itemSlot");
      if (nameF != null && slotF != null) {
         int pickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
         int slotW = 26;
         int gap = 3;
         int pickX = x + w - pickW;
         int slotX = pickX - gap - slotW;
         int nameW = Math.max(1, slotX - x - 2);
         nameF.read(x);
         nameF.write(y + 1);
         nameF.update(nameW);
         nameF.init_V(ctx, mx, my, delta);
         slotF.read(slotX);
         slotF.write(y + 1);
         slotF.update(slotW);
         slotF.init_V(ctx, mx, my, delta);
         boolean capt = this.init_StreamUtil.init("itemName");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, y, pickW, 14, StreamUtil.ITEM_SLOT, capt, true, mx, my, () -> this.close_V("itemName"));
      }
   }

   private void load(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int lw = this.init_I_I(w, field.label(), font, 56);
      this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, lw, font);
      String summary = this.open_Str();
      int valX = this.flush_I(x, lw);
      int valW = this.close(w, lw);
      int color = summary != null && !summary.isEmpty() ? UiColorPalette.scan() : UiColorPalette.parse();
      String display = summary != null && !summary.isEmpty() ? summary : "Not captured";
      String trimmed = autismclient.gui.f.b.k.init(this.init_Font, display, font, color) > valW
         ? autismclient.gui.f.b.k.init(this.init_Font, display, Math.max(8, valW - 4), font, color)
         : display;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, trimmed, font, color, valX, y + 2, false);
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int btnW = this.init_I_StreamUtil(StreamUtil.PACKET_CLICK_TARGET, "Re-capture", "Click slot...");
      int btnH = 13;
      int lw = this.init_I_I(w, field.label(), font, btnW);
      this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, lw, font);
      boolean capturing = field.key().equals(this.write_Str);
      int btnX = x + w - btnW;
      this.init_ByteReader_GuiGraphicsExtract(
         ctx, btnX, y + 2, btnW, btnH, StreamUtil.PACKET_CLICK_TARGET, capturing, true, mx, my, "Re-capture", "Click slot...", () -> this.sync_V(field.key())
      );
   }

   private String open_Str() {
      if (this.init_MacroAction instanceof PacketClickAction pca) {
         return pca.target == null ? null : pca.target.init_bo_StreamUtil_bo_DataUtil(this.init_bo_DataUtil()).init_Str();
      } else {
         return null;
      }
   }

   private boolean init_Z_FieldDef(FieldDef field) {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.RACE && field != null && "raceSteps".equals(field.key());
   }

   private void load_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      List<String> values = this.write_Map.computeIfAbsent(key, ignored -> new ArrayList<>());
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, field.label() + " (" + values.size() + ")", font, UiColorPalette.parse(), x, y + 2, false);
      int addY = y + 13;
      int[] addBtns = this.init_IArr_I(x, w, false, "+ Condition", "+ Action");
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, addBtns[0], addY, addBtns[1], 13, "+ Condition", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, () -> this.init_V_List_Z(values, true)
      );
      this.init_ByteReader_GuiGraphicsExtract_2(
         ctx, addBtns[2], addY, addBtns[3], 13, "+ Action", ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> this.init_V_List_Z(values, false)
      );
      int listY = addY + 13 + 2;
      int rowH = 14;
      int visibleRows = 4;
      EventBus viewport = this.init_EventBus_Map(this.tick_Map, key + "_race", x, listY, w, visibleRows * rowH, rowH, 5);
      viewport.init_V(values.size() * rowH);
      viewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (values.isEmpty()) {
         String hint = "Use +Cond for waits or +Act for batch actions.";
         FileCache.init(ctx, this.init_Font, hint, x, listY, w - 5 - 1);
      } else {
         int buttonW = 12;
         int rowGap = 2;
         int buttonArea = buttonW * 3 + rowGap * 2;
         int rowTextW = Math.max(20, w - 5 - buttonArea - 7);
         viewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int first = viewport.pop();

         for (int vi = first; vi < values.size() && vi <= viewport.tick(); vi++) {
            int rowY = viewport.init_I(vi);
            if (rowY != -2147483648) {
               String value = values.get(vi);
               boolean hovered = mx >= x && mx < x + rowTextW && my >= rowY && my < rowY + rowH;
               this.init_V_GuiGraphicsExtract_4(ctx, x, rowY, rowTextW, rowH, vi, value, hovered);
               int bx = x + rowTextW + 3;
               int rowIndex = vi;
               this.init_V_GuiGraphicsExtract_2(
                  ctx, bx, rowY + (rowH - buttonW) / 2, buttonW, mx, my, "^", false, rowIndex > 0, () -> Collections.swap(values, rowIndex, rowIndex - 1)
               );
               bx += buttonW + rowGap;
               this.init_V_GuiGraphicsExtract_2(
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
               boolean canDelete = this.init_I_List(values) > 1 || !this.update_Z(value);
               this.init_V_GuiGraphicsExtract_2(ctx, bx, rowY + (rowH - buttonW) / 2, buttonW, mx, my, "X", true, canDelete, () -> {
                  if (rowIndex >= 0 && rowIndex < values.size() && (this.init_I_List(values) > 1 || !this.update_Z(values.get(rowIndex)))) {
                     values.remove(rowIndex);
                  }
               });
            }
         }

         viewport.init(ctx);
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int index, String step, boolean hovered) {
      String canonical = RaceAction.load_Str(step);
      boolean condition = RaceAction.init_Z_2(canonical);
      int bgColor = hovered ? UiColorPalette.level() : UiColorPalette.flags();
      TableIndex.init(ctx, x, y + 1, w, h - 2, bgColor);
      int badgeColor = condition ? -30720 : -12268476;
      TableIndex.read(ctx, x + 1, y + 3, 3, h - 6, badgeColor);
      int numberColor = UiColorPalette.bind();
      autismclient.gui.f.b.k.init(
         ctx,
         this.init_Font,
         String.valueOf(index + 1),
         this.init_CharEncoder.init_Identifier_l(l.read),
         numberColor,
         x + 8,
         j.init(y, h, this.init_CharEncoder.init_I_l(l.read), this.init_CharEncoder.flush()),
         false
      );
      String role = condition ? "COND" : "ACT";
      int roleColor = condition ? UiColorPalette.node() : UiColorPalette.parse();
      int roleX = x + 24;
      autismclient.gui.f.b.k.init(
         ctx,
         this.init_Font,
         role,
         this.init_CharEncoder.init_Identifier_l(l.load),
         roleColor,
         roleX,
         j.init(y, h, this.init_CharEncoder.init_I_l(l.load), this.init_CharEncoder.flush()),
         false
      );
      String label = this.open_Str(step).replaceFirst("^(COND|ACT)\\s+", "");
      int labelX = roleX + 34;
      String trimmed = autismclient.gui.f.b.k.init(this.init_Font, label, Math.max(1, x + w - labelX - 4), this.init_CharEncoder.init_Identifier_l(l.read), -1);
      autismclient.gui.f.b.k.init(
         ctx,
         this.init_Font,
         trimmed,
         this.init_CharEncoder.init_Identifier_l(l.read),
         -1,
         labelX,
         j.init(y, h, this.init_CharEncoder.init_I_l(l.read), this.init_CharEncoder.flush()),
         false
      );
   }

   private void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor ctx, int x, int y, int size, int mx, int my, String symbol, boolean danger, boolean active, Runnable action
   ) {
      boolean hovered = active && mx >= x && mx < x + size && my >= y && my < y + size;
      autismclient.gui.macro.editor.a.CachePool.init(ctx, autismclient.gui.f.NetHelper.init(x, y, size, size), symbol, hovered, danger);
      this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, y, size, size, (mouseX, mouseY, mouseButton) -> {
         if (active && mouseButton == 0) {
            action.run();
            return true;
         } else {
            return false;
         }
      }));
   }

   private void init_V_List_Z(List<String> values, boolean condition) {
      ArrayList<String> actions = new ArrayList<>(open_List);
      if (!AutismLiteVariant.enabled()) {
         actions.add("Tools / TOOLS");
      }

      List<RaceStepSelectorOverlay.Inner_DataUtil> options = condition
         ? this.init_List_List_Str(close_List, "CONDITION")
         : this.init_List_List_Str(actions, "ACTION");
      this.init_RaceStepSelectorOv.init_V_Str_4(condition ? "Condition Selector" : "Action Selector", options, option -> {
         if (option != null && option.load() != null && !option.load().isBlank()) {
            values.add((condition ? "CONDITION:" : "ACTION:") + option.load());
            this.fill();
         }
      });
   }

   private List<RaceStepSelectorOverlay.Inner_DataUtil> init_List_List_Str(List<String> choices, String role) {
      ArrayList<RaceStepSelectorOverlay.Inner_DataUtil> out = new ArrayList<>();

      for (String choice : choices) {
         String[] parts = choice == null ? new String[0] : choice.split("/", 2);
         String category = parts.length > 1 ? parts[0].trim() : role;
         String id = this.poll_Str(choice);
         if (!id.isBlank()) {
            String label = id.replace('_', ' ');
            out.add(new RaceStepSelectorOverlay.Inner_DataUtil(category, id, label, role));
         }
      }

      return out;
   }

   private String open_Str(String step) {
      String canonical = RaceAction.load_Str(step);
      String type = RaceAction.init_Str_2(canonical);
      return (RaceAction.init_Z_2(canonical) ? "COND  " : "ACT   ") + type;
   }

   private boolean update_Z(String step) {
      return RaceAction.load_Z(RaceAction.load_Str(step));
   }

   private int init_I_List(List<String> values) {
      int count = 0;
      if (values != null) {
         for (String value : values) {
            if (this.update_Z(value)) {
               count++;
            }
         }
      }

      return count;
   }

   private String poll_Str(String choice) {
      if (choice == null) {
         return "";
      }

      int slash = choice.lastIndexOf(47);
      return (slash >= 0 ? choice.substring(slash + 1) : choice).trim();
   }

   private void sync_V(String fieldKey) {
      if (this.init_MacroAction instanceof PacketClickAction) {
         this.write_Str = fieldKey;
         D.init(target -> this.init_V_bo_StreamUtil(target));
         this.index();
      }
   }

   private void init_V_bo_StreamUtil(ContainerClickSnapshot.Inner_StreamUtil target) {
      if (target != null) {
         if (this.init_MacroAction instanceof PacketClickAction pca) {
            ContainerClickSnapshot.Inner_DataUtil mode = this.init_bo_DataUtil();
            pca.mode = mode.name();
            pca.init(target.init_bo_StreamUtil_bo_DataUtil(mode));
            if (this.init_CompoundTag != null) {
               this.init_CompoundTag.putString("mode", mode.name());
               this.init_CompoundTag.put("target", target.init_bo_StreamUtil_bo_DataUtil(mode).init_CompoundTag());
            }
         }

         this.write_Str = null;
         this.init_V_Z_Z(false, false);
      }
   }

   private ContainerClickSnapshot.Inner_DataUtil init_bo_DataUtil() {
      FieldDef modeField = this.init_FieldDef_Str("mode");
      if (modeField != null && modeField.type() == CachePool.ENUM && !modeField.enumOptions().isEmpty()) {
         int idx = Math.max(0, Math.min(this.read_Map.getOrDefault("mode", 0), modeField.enumOptions().size() - 1));
         return ContainerClickSnapshot.Inner_DataUtil.init(modeField.enumOptions().get(idx));
      } else {
         return ContainerClickSnapshot.Inner_DataUtil.init(this.init_CompoundTag == null ? "" : this.init_CompoundTag.getStringOr("mode", ""));
      }
   }

   private FieldDef init_FieldDef_Str(String key) {
      if (this.init_ActionFieldSchema != null && key != null) {
         for (FieldDef field : this.init_ActionFieldSchema.fields()) {
            if (key.equals(field.key())) {
               return field;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean pop_Z() {
      return this.write_Str != null && D.load_Z();
   }

   public boolean tick_Z() {
      if (this.write_Str == null) {
         return false;
      }

      D.read();
      this.write_Str = null;
      this.init_V_Z_Z(false, false);
      return true;
   }

   private boolean sync_Z(String key) {
      return "waitForGuiBefore".equals(key);
   }

   private boolean flush_Z(String key) {
      return "waitForGuiAfter".equals(key);
   }

   private void write_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      boolean val = this.load_Map.getOrDefault(key, false);
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int btnW = 34;
      int btnH = 13;
      String label = this.load_Str_FieldDef(field);
      int lw = this.init_I_I(w, label, font, btnW);
      this.init_V_GuiGraphicsExtract_3(ctx, label, x, y, lw, font);
      int btnX = x + w - btnW;
      this.init_ByteReader_GuiGraphicsExtract_3(ctx, btnX, y + 2, btnW, btnH, val ? "ON" : "OFF", val, "macro-field:" + key, mx, my, () -> {
         boolean nowOn = !this.load_Map.getOrDefault(key, false);
         this.load_Map.put(key, nowOn);
         if ("waitForTrigger".equals(key)) {
            this.pop_Z = true;
         }

         if (this.init_CompoundTag != null) {
            this.init_CompoundTag.putBoolean(key, nowOn);
         }

         if (nowOn && field.hasMutualExclusion()) {
            for (String other : field.mutuallyExclusiveWith()) {
               this.load_Map.put(other, false);
               if (this.init_CompoundTag != null) {
                  this.init_CompoundTag.putBoolean(other, false);
               }
            }
         }
      });
   }

   private void load(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      ThemedTextInputBox tf = this.init_Map.get(key);
      if (tf != null) {
         Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
         if (field.type() == CachePool.MACRO_SELECT) {
            this.init_V_GuiGraphicsExtract_6(ctx, field, tf, x, y, w, mx, my);
         } else if (this.read(field)) {
            autismclient.gui.f.b.k.init(ctx, this.init_Font, field.label(), font, UiColorPalette.parse(), x, y + 2, false);
            tf.read(x);
            tf.write(y + 13);
            int variableW = this.close_Z(field.key()) ? 24 : 0;
            tf.update(Math.max(1, w - (variableW > 0 ? variableW + 2 : 0)));
            tf.sync(40);
            tf.init_V(ctx, mx, my, delta);
            if (variableW > 0) {
               this.init_V_FieldDef(field, tf, x + w - variableW, y + 13, variableW);
            }
         } else {
            boolean packetPick = field.captureMode() == StreamUtil.PACKET_NAME;
            boolean slotPick = field.type() == CachePool.SLOT && this.clear_Z();
            boolean itemSlotPick = field.captureMode() == StreamUtil.ITEM_SLOT && this.clear_Z();
            boolean menuButtonPick = field.captureMode() == StreamUtil.CUSTOM_MENU_BUTTON;
            boolean nbtBookFilePick = this.init_MacroAction != null
               && this.init_MacroAction.getType() == MacroActionType.NBT_BOOK
               && "customFilePath".equals(key);
            int lw = this.init_I_I(w, field.label(), font, !itemSlotPick && !packetPick && !slotPick && !menuButtonPick ? 56 : 88);
            this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, lw, font);
            int tfX = this.flush_I(x, lw);
            int variableW = this.close_Z(key) ? 24 : 0;
            if (menuButtonPick) {
               int pickW = this.init_I_StreamUtil(StreamUtil.CUSTOM_MENU_BUTTON, null, null);
               int availableW = Math.max(1, this.close(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = Math.max(1, availableW - pickW - 2);
               tf.read(tfX);
               tf.write(y + 2);
               tf.update(tfW);
               tf.init_V(ctx, mx, my, delta);
               String fKey = key;
               this.init_ByteReader_GuiGraphicsExtract(ctx, tfX + tfW + 2, y + 2, pickW, 13, StreamUtil.CUSTOM_MENU_BUTTON, false, true, mx, my, () -> this.flush_V(fKey));
               if (variableW > 0) {
                  this.init_V_FieldDef(field, tf, tfX + tfW + pickW + 4, y + 2, variableW);
               }
            } else if (nbtBookFilePick) {
               int pickW = this.write_I("Pick");
               int availableW = Math.max(1, this.close(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = Math.max(1, availableW - pickW - 2);
               tf.read(tfX);
               tf.write(y + 2);
               tf.update(tfW);
               tf.init_V(ctx, mx, my, delta);
               this.init_ByteReader_GuiGraphicsExtract_2(
                  ctx, tfX + tfW + 2, y + 2, pickW, 13, "Pick", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> this.init_V_k(tf)
               );
               if (variableW > 0) {
                  this.init_V_FieldDef(field, tf, tfX + tfW + pickW + 4, y + 2, variableW);
               }
            } else if (!itemSlotPick && !packetPick && !slotPick) {
               int tfW = Math.max(1, this.close(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               tf.read(tfX);
               tf.write(y + 2);
               tf.update(tfW);
               tf.init_V(ctx, mx, my, delta);
               if (variableW > 0) {
                  this.init_V_FieldDef(field, tf, tfX + tfW + 2, y + 2, variableW);
               }
            } else {
               int pickW = this.init_I_StreamUtil(packetPick ? StreamUtil.PACKET_NAME : StreamUtil.ITEM_SLOT, null, null);
               int availableW = Math.max(1, this.close(w, lw) - (variableW > 0 ? variableW + 2 : 0));
               int tfW = slotPick ? Math.max(26, availableW - pickW - 2) : Math.max(1, availableW - pickW - 2);
               tf.read(tfX);
               tf.write(y + 2);
               tf.update(tfW);
               tf.init_V(ctx, mx, my, delta);
               int pkX = tfX + tfW + 2;
               boolean capt = !packetPick && this.init_StreamUtil.init(key);
               String fKey = key;
               this.init_ByteReader_GuiGraphicsExtract(
                  ctx, pkX, y + 2, pickW, 14, packetPick ? StreamUtil.PACKET_NAME : StreamUtil.ITEM_SLOT, capt, true, mx, my, () -> {
                     if (packetPick) {
                        this.run_V(fKey);
                     } else {
                        this.close_V(fKey);
                     }
                  }
               );
               if (variableW > 0) {
                  this.init_V_FieldDef(field, tf, pkX + pickW + 2, y + 2, variableW);
               }
            }
         }
      }
   }

   private void flush_V(String key) {
      CustomMenuSnapshot menu = autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot();
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
            ThemedTextInputBox tf = this.init_Map.get(key);
            String current = tf == null ? "" : tf.load_Str().trim();
            int next = 0;

            for (int i = 0; i < buttons.size(); i++) {
               if (buttons.get(i).label().equalsIgnoreCase(current)) {
                  next = (i + 1) % buttons.size();
                  break;
               }
            }

            String label = buttons.get(next).label();
            if (tf != null) {
               tf.init_V_Str(label);
            }

            bi.init("Button set to \"" + label + "\"" + (buttons.size() > 1 ? " (Pick again to cycle)" : ""), -11013497);
         }
      }
   }

   private void init_V_FieldDef(FieldDef field, ThemedTextInputBox textField, int x, int y, int width) {
      if (field != null) {
         this.init_V_Str_2(field.key(), textField, x, y, width);
      }
   }

   private void init_V_Str_2(String key, ThemedTextInputBox textField, int x, int y, int width) {
      if (this.close_Z(key) && textField != null) {
         String cacheKey = "__vars_" + key;
         autismclient.gui.f.b.SyncManager dropdown = this.run_Map
            .computeIfAbsent(cacheKey, ignored -> new autismclient.gui.f.b.SyncManager(x, y, width, 13, this.flush_List, 0, index -> {}));
         dropdown.init_SyncManager_I(x, y, width, 13).init_SyncManager_List(this.flush_List).init_SyncManager_Str("{}").init_SyncManager_IntConsumer(index -> {
            if (index >= 0 && index < this.flush_List.size()) {
               textField.read(this.flush_List.get(index));
            }

            dropdown.init_V();
         });
         this.sync_List.add(dropdown);
      }
   }

   private boolean close_Z(String key) {
      return this.init_MacroAction != null
         && this.init_MacroAction.getType() == MacroActionType.SEND_CHAT
         && "message".equals(key)
         && !this.flush_List.isEmpty();
   }

   private void task() {
      LinkedHashSet<String> tokens = new LinkedHashSet<>();
      if (this.init_aM != null && this.init_aM.init_List != null) {
         int limit = this.f_f_I < 0 ? this.init_aM.init_List.size() : Math.min(this.f_f_I, this.init_aM.init_List.size());

         for (int i = limit - 1; i >= 0 && tokens.size() < 4; i--) {
            init_V_Set(tokens, this.init_aM.init_List.get(i), 4);
         }
      }

      tokens.addAll(data_List);
      this.flush_List.clear();
      this.flush_List.addAll(tokens);
   }

   private static void init_V_Set(Set<String> tokens, MacroAction action, int limit) {
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

   private static void init_V_Set_MacroAction(Set<String> tokens, MacroAction action) {
      if (action instanceof WaitForChatAction chat) {
         for (String name : PatternCaptureMatcher.init(chat.init_UtilCore_DataUtil(), chat.pattern)) {
            tokens.add("{" + name + "}");
         }

         init_V_Set_2(tokens, chat.saveAs, "sender", "message", "display", "source");
      } else if (action instanceof WaitForGuiAction gui) {
         for (String name : PatternCaptureMatcher.init(gui.matchMode, gui.guiTitle)) {
            tokens.add("{" + name + "}");
         }

         init_V_Set_2(tokens, gui.saveAs, "title", "type");
      } else if (action instanceof autismclient.util.macro.CaptureValueAction capture) {
         for (String name : PatternCaptureMatcher.init(capture.matchMode, capture.pattern)) {
            tokens.add("{" + name + "}");
         }

         switch (capture.source) {
            case GUI_TITLE:
               init_V_Set_2(tokens, capture.saveAs, "title", "type");
               break;
            case RECENT_CHAT:
               init_V_Set_2(tokens, capture.saveAs, "sender", "message", "display", "source");
               break;
            case SCOREBOARD:
               init_V_Set_2(tokens, capture.saveAs, "line", "name", "score", "row", "title", "objective");
               break;
            case GUI_ITEM:
            case PLAYER_ITEM:
            case CURSOR_ITEM:
            case HELD_ITEM:
               init_V_Set_2(tokens, capture.saveAs, "name", "id", "count", "slot", "lore", "lore1");
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
            init_V_Set_2(tokens, root, "health", "max", "absorption");
         } else if (action instanceof WaitForEntityAction) {
            init_V_Set_2(tokens, root, "name", "id", "uuid", "x", "y", "z", "distance");
         } else if (action instanceof WaitForSoundAction) {
            init_V_Set_2(tokens, root, "id", "x", "y", "z");
         } else if (action instanceof WaitForPacketAction || action instanceof autismclient.util.macro.WaitPacketMatchAction) {
            init_V_Set_2(tokens, root, "name", "class", "direction");
            if (action instanceof autismclient.util.macro.WaitPacketMatchAction packetMatch) {
               String cleanRoot = MacroVariableStore.load(root);
               if (!cleanRoot.isBlank()) {
                  for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : packetMatch.init_List_9()) {
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

            init_V_Set_2(tokens, root, "title", "type");
         } else if (action instanceof WaitPosAction || action instanceof WaitForPositionDeltaAction || action instanceof WaitForTeleportAction) {
            init_V_Set_2(tokens, root, "x", "y", "z", "yaw", "pitch");
         } else if (action instanceof WaitForWorldChangeAction) {
            init_V_Set_2(tokens, root, "id", "name");
         } else if (action instanceof WaitGamemodeChangeAction) {
            init_V_Set_2(tokens, root, "name");
         } else {
            init_V_Set_2(tokens, root, "name", "id", "count", "slot", "condition");
         }
      }
   }

   private static void init_V_Set_2(Set<String> tokens, String root, String... properties) {
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

   private void init_V_GuiGraphicsExtract_6(GuiGraphicsExtractor ctx, FieldDef field, ThemedTextInputBox backingField, int x, int y, int w, int mx, int my) {
      backingField.read(-10000);
      backingField.write(-10000);
      List<cY.Inner_DataUtil> macros = new ArrayList<>(autismclient.util.aN.init_aN().init_List_Str(""));
      if (backingField.load_Str() != null && !backingField.load_Str().isBlank() && autismclient.util.aN.init_aN().init_aM_Str(backingField.load_Str()) == null) {
         macros.add(0, new cY.Inner_DataUtil("", new AutismMacro(backingField.load_Str()), 0, false));
      }

      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      String current = backingField.load_Str() == null ? "" : backingField.load_Str();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, field.label(), font, UiColorPalette.parse(), x, y + 2, false);
      int badgeW = Math.min(150, Math.max(60, w / 2));
      int badgeX = x + w - badgeW;
      TableIndex.read(ctx, badgeX, y + 1, badgeW, 14);
      String selectedLabel = current.isBlank() ? "No macro selected" : current;
      String selectedTrimmed = autismclient.gui.f.b.k.init(this.init_Font, selectedLabel, badgeW - 6, font, -1);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, selectedTrimmed, font, current.isBlank() ? UiColorPalette.a() : UiColorPalette.scan(), badgeX + 3, y + 3, false);
      int listY = y + 17;
      int listH = 70;
      int itemW = w - 5 - 1;
      String viewportKey = "macro_select_" + field.key();
      EventBus viewport = this.init_EventBus_Map(this.tick_Map, viewportKey, x, listY, w, listH, 14, 5);
      viewport.init_V(macros.size() * 14);
      viewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (macros.isEmpty()) {
         TableIndex.init(ctx, x, listY, itemW, 13, false, false);
         FileCache.init(ctx, this.init_Font, "No saved macros", x, listY, itemW);
      } else {
         viewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int first = viewport.pop();

         for (int i = first; i < macros.size() && i <= viewport.tick(); i++) {
            cY.Inner_DataUtil folderRow = macros.get(i);
            String macroName = folderRow.init_Str();
            int iy = viewport.init_I(i);
            if (iy != -2147483648) {
               if (folderRow.init_Z()) {
                  autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, iy, itemW, 14);
                  autismclient.gui.f.b.TaskQueue.load(
                     UiHelper.init(ctx, this.init_Font, mx, my), bounds, folderRow.init_Str(), folderRow.load_Z(), bounds.init_Z(mx, my)
                  );
                  int hitTop = Math.max(iy, listY);
                  int hitBottom = Math.min(iy + 14, listY + listH);
                  if (hitBottom > hitTop) {
                     this.valid_List
                        .add(
                           new ActionEditorOverlay.Inner_StreamUtil(
                              x, hitTop, itemW, hitBottom - hitTop, () -> autismclient.util.aN.init_aN().init_V_Str(folderRow.load_Str())
                           )
                        );
                  }
               } else {
                  boolean selected = macroName.equals(current);
                  boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + 14;
                  autismclient.gui.macro.editor.a.CachePool.init(
                     ctx,
                     this.init_Font,
                     Component.literal(macroName),
                     autismclient.gui.f.NetHelper.init(x, iy, itemW, 14),
                     hovered,
                     selected,
                     FileCache.Inner_DataUtil.NORMAL,
                     false
                  );
                  String selectedMacro = macroName;
                  this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, itemW, 14, () -> backingField.init_V_Str(selectedMacro)));
               }
            }
         }

         viewport.init(ctx);
      }
   }

   private boolean read(FieldDef field) {
      return field != null && "pattern".equals(field.key()) && this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_CHAT;
   }

   private void update_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my) {
      String key = field.key();
      List<String> opts = field.enumOptions();
      if (!opts.isEmpty()) {
         int idx = Math.min(this.read_Map.getOrDefault(key, 0), opts.size() - 1);
         Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
         int lw = this.init_I_I(w, field.label(), font, 72);
         this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, lw, font);
         int ctrlX = this.flush_I(x, lw);
         int ctrlW = this.close(w, lw);
         int ddH = 13;
         int variableW = this.close_Z(key) ? 20 : 0;
         int valueW = Math.max(1, ctrlW - (variableW > 0 ? variableW + 2 : 0));
         ThemedTextInputBox bindingField = field.dynamic() ? this.init_Map.get(key) : null;
         if (bindingField != null && !bindingField.load_Str().isBlank()) {
            bindingField.read(ctrlX);
            bindingField.write(y + 2);
            bindingField.update(valueW);
            bindingField.init_V(ctx, mx, my, 0.0F);
            if (variableW > 0) {
               this.load(field, bindingField, ctrlX + valueW + 2, y + 2, variableW);
            }
         } else {
            boolean toolsLabels = this.init_MacroAction instanceof ToolsAction || key.startsWith("cond_tool");
            List<String> displayOpts = toolsLabels ? opts.stream().map(value -> BufferHelper.init(key, value)).toList() : opts;
            int ddW = this.init_I_List_I(displayOpts, valueW);
            autismclient.gui.f.b.SyncManager dd = this.run_Map
               .computeIfAbsent(
                  key, k -> new autismclient.gui.f.b.SyncManager(ctrlX, y + 2, ddW, ddH, displayOpts, idx, newIdx -> this.init_V_FieldDef_I(field, newIdx))
               );
            dd.init_SyncManager_I(ctrlX, y + 2, ddW, ddH)
               .init_SyncManager_List(displayOpts)
               .init_SyncManager_I(idx)
               .init_SyncManager_IntConsumer(newIdx -> this.init_V_FieldDef_I(field, newIdx));
            this.sync_List.add(dd);
            if (variableW > 0 && bindingField != null) {
               this.load(field, bindingField, ctrlX + ddW + 2, y + 2, variableW);
            }
         }
      }
   }

   private void load(FieldDef field, ThemedTextInputBox bindingField, int x, int y, int width) {
      List<String> options = new ArrayList<>();
      options.add("Static");
      options.addAll(this.flush_List);
      String cacheKey = "__enum_vars_" + field.key();
      autismclient.gui.f.b.SyncManager dropdown = this.run_Map
         .computeIfAbsent(cacheKey, ignored -> new autismclient.gui.f.b.SyncManager(x, y, width, 13, options, 0, index -> {}));
      dropdown.init_SyncManager_I(x, y, width, 13).init_SyncManager_List(options).init_SyncManager_Str("{}").init_SyncManager_IntConsumer(index -> {
         if (index <= 0) {
            bindingField.init_V_Str("");
         } else if (index < options.size()) {
            bindingField.init_V_Str(options.get(index));
         }

         dropdown.init_V();
      });
      this.sync_List.add(dropdown);
   }

   private void read(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      String[] axis = new String[]{"X", "Y", "Z"};
      boolean hasCapture = field.captureMode() != StreamUtil.NONE && this.init_Z_StreamUtil(field.captureMode());
      int capBtnW = this.init_I_StreamUtil(field.captureMode(), null, null);
      int capBtnH = 13;
      int labelW = hasCapture ? w - capBtnW - 4 : w;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, field.label(), font, UiColorPalette.parse(), x, y + 3, false);
      if (this.init_MacroAction instanceof ToolsAction && "position".equals(key) && !"OFFSET".equals(this.tick_Str("destination"))) {
         int currentW = autismclient.gui.f.b.k.init(this.init_Font, "Use current", font, UiColorPalette.scan()) + 12;
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, x + w - currentW, y, currentW, 13, "Use current", ByteReader.Inner_StreamUtil.SECONDARY, init_Minecraft.player != null, mx, my, () -> {
               if (init_Minecraft.player != null) {
                  BlockPos position = init_Minecraft.player.blockPosition();
                  int[] values = new int[]{position.getX(), position.getY(), position.getZ()};

                  for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
                     ThemedTextInputBox input = this.init_Map.get(key + "_" + axisIndex);
                     if (input != null) {
                        input.init_V_Str(Integer.toString(values[axisIndex]));
                     }
                  }
               }
            }
         );
      }

      if (hasCapture) {
         int cbX = x + labelW + 4;
         this.init_ByteReader_GuiGraphicsExtract(ctx, cbX, y, capBtnW, capBtnH, field.captureMode(), false, true, mx, my, () -> this.init_V_FieldDef(field));
      }

      int fieldW = (w - 4) / 3;

      for (int i = 0; i < 3; i++) {
         ThemedTextInputBox tf = this.init_Map.get(key + "_" + i);
         if (tf != null) {
            int fx = x + i * (fieldW + 2);
            int variableW = this.close_Z(field.key()) ? 18 : 0;
            autismclient.gui.f.b.k.init(ctx, this.init_Font, axis[i], font, UiColorPalette.bind(), fx, y + 14 + 3, false);
            tf.read(fx + 9);
            tf.write(y + 14 + 1);
            tf.update(Math.max(1, fieldW - 9 - (variableW > 0 ? variableW + 2 : 0)));
            tf.init_V(ctx, mx, my, delta);
            if (variableW > 0) {
               this.init_V_Str_2(field.key() + "_" + axis[i].toLowerCase(Locale.ROOT), tf, fx + fieldW - variableW, y + 14 + 2, variableW);
            }
         }
      }
   }

   private void init_V_k(ThemedTextInputBox field) {
      if (field != null) {
         String current = field.load_Str() == null ? "" : field.load_Str().trim();
         String selected = TinyFileDialogs.tinyfd_openFileDialog("NBT Book Text File", current.isBlank() ? null : current, null, null, false);
         if (selected != null && !selected.isBlank()) {
            field.init_V_Str(selected);
         }
      }
   }

   private void write(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      List<String> lst = this.write_Map.getOrDefault(key, Collections.emptyList());
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      ThemedTextInputBox af = this.flush_Map.get(key);
      boolean editable = this.set_Z(key);
      boolean xcarryList = this.put_Z(key);
      int editIdx = editable ? this.open_Map.getOrDefault(key, -1) : -1;
      if (editIdx >= lst.size()) {
         editIdx = -1;
         this.open_Map.put(key, -1);
      }

      String filter = !editable && af != null ? af.load_Str().toLowerCase() : "";
      String emptyHint = this.pop_Str(key);
      List<Integer> filtered = autismclient.gui.macro.editor.a.CachePool.init(
         lst, this.push_Map.computeIfAbsent(key, ignored -> new ArrayList<>()), value -> filter.isEmpty() || value.toLowerCase().contains(filter)
      );
      String sectionLabel = filter.isEmpty() ? field.label() + " (" + lst.size() + ")" : field.label() + " (" + filtered.size() + "/" + lst.size() + ")";
      autismclient.gui.f.b.k.init(ctx, this.init_Font, sectionLabel, font, UiColorPalette.parse(), x, y + 2, false);
      boolean hasCapture = field.captureMode() != StreamUtil.NONE && this.init_Z_StreamUtil(field.captureMode());
      boolean hasSlotField = editable && this.reset_Z(key);
      int btnH = 13;
      int addY = y + 13;
      autismclient.gui.macro.editor.a.CachePool.Inner_DataUtil toolbar = autismclient.gui.macro.editor.a.CachePool.init(
         autismclient.gui.f.NetHelper.init(x, addY, w, 13), hasSlotField, hasCapture
      );
      int slotW = toolbar.load() == null ? 0 : toolbar.load().update();
      ThemedTextInputBox slotField = hasSlotField ? this.init_Map.get(key + "_slot") : null;
      if (hasSlotField && slotField == null) {
         slotField = this.init_k_I(slotW);
         slotField.poll(true);
         slotField.init_V_Component(Component.literal("Slot#"));
         this.init_Map.put(key + "_slot", slotField);
      }

      if (af != null) {
         af.read(toolbar.init().read());
         af.write(toolbar.init().write());
         af.update(toolbar.init().update());
         af.init_V(ctx, mx, my, delta);
      }

      if (hasSlotField && slotField != null) {
         slotField.read(toolbar.load().read());
         slotField.write(toolbar.load().write());
         slotField.update(toolbar.load().update());
         slotField.init_V(ctx, mx, my, delta);
      }

      if (editable && editIdx >= 0 && editIdx < lst.size() && af != null) {
         String nameText = af.load_Str().strip();
         String slotText = hasSlotField && slotField != null ? slotField.load_Str().strip() : "";
         String entry = write_Str_Str_Str(nameText, slotText);
         if (!entry.isEmpty()) {
            String normalized = this.put_Z(key) ? this.run_Str(entry) : (this.get_Z(key) ? StoreItemAction.init_Str_Str_8(entry) : entry);
            if (normalized != null && !normalized.isBlank() && !normalized.equals(lst.get(editIdx))) {
               lst.set(editIdx, normalized);
               if (this.reset_Z(key)) {
                  this.sync_Map.put(key, this.init_List_Str(key));
               }
            }
         }
      }

      ThemedTextInputBox fSlotField = slotField;
      if (af != null) {
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, toolbar.read().read(), toolbar.read().write(), toolbar.read().update(), btnH, "+Add", ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
               String nameText = af.load_Str().strip();
               String slotText = hasSlotField && fSlotField != null ? fSlotField.load_Str().strip() : "";
               String entry = write_Str_Str_Str(nameText, slotText);
               if (!entry.isEmpty() && this.init_Z_FieldDef(field, lst, entry)) {
                  af.init_V_Str("");
                  if (fSlotField != null) {
                     fSlotField.init_V_Str("");
                  }

                  this.open_Map.put(key, -1);
               }
            }
         );
      }

      if (hasCapture) {
         boolean isItemSlot = field.captureMode() == StreamUtil.ITEM_SLOT;
         boolean capturing = isItemSlot && this.init_StreamUtil.init(key);
         String fKey = key;
         FieldDef fField = field;
         List<String> fLst = lst;
         this.init_ByteReader_GuiGraphicsExtract(
            ctx, toolbar.write().read(), toolbar.write().write(), toolbar.write().update(), btnH, field.captureMode(), capturing, true, mx, my, () -> {
               if (isItemSlot) {
                  this.close_V(fKey);
               } else {
                  this.init_V_FieldDef_List(fField, fLst);
               }
            }
         );
      }

      int itemsY = addY + btnH + 2;
      int selAreaH = 56;
      int delW = 13;
      int destW = xcarryList ? this.init_I_List_I(XCarryAction.init_List_10(), 62) : 0;
      int amountW = xcarryList ? Math.max(this.write_I("FullStk"), this.write_I("x64")) : 0;
      if (xcarryList) {
         this.sync(lst.size());
      }

      if (xcarryList) {
         this.flush(lst.size());
         if (editIdx >= 0 && editIdx < lst.size()) {
            int selectedXCarryIndex = editIdx;
            ThemedTextInputBox countField = this.init_Map.get(key + "_xcarry_count");
            if (countField == null) {
               countField = this.init_k_I(38);
               countField.poll(true);
               countField.init_V_Component(Component.literal("Count"));
               this.init_Map.put(key + "_xcarry_count", countField);
            }

            int amountY = itemsY;
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "Amount", this.init_CharEncoder.init_Identifier_l(l.read), UiColorPalette.parse(), x, amountY + 3, false);
            int modeX = x + 52;
            boolean custom = this.init_aQ_DataUtil_I(editIdx) == XCarryAction.Inner_DataUtil.CUSTOM;
            int amountBtnW = Math.max(this.write_I("Count"), this.write_I("FullStk"));
            this.init_ByteReader_GuiGraphicsExtract_2(
               ctx, modeX, amountY, amountBtnW, btnH, custom ? "Count" : "FullStk", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> {
                  boolean nowCustom = this.init_aQ_DataUtil_I(selectedXCarryIndex) == XCarryAction.Inner_DataUtil.CUSTOM;
                  this.init_V_I_aQ_DataUtil(selectedXCarryIndex, nowCustom ? XCarryAction.Inner_DataUtil.FULL_STK : XCarryAction.Inner_DataUtil.CUSTOM);
                  ThemedTextInputBox cf = this.init_Map.get(key + "_xcarry_count");
                  if (cf != null) {
                     cf.init_V_Str(String.valueOf(this.write_I(selectedXCarryIndex)));
                  }
               }
            );
            if (custom) {
               countField.read(modeX + amountBtnW + 4);
               countField.write(amountY);
               countField.update(42);
               if (countField.load_Str().isBlank()) {
                  countField.init_V_Str(String.valueOf(this.write_I(editIdx)));
               }

               countField.init_V(ctx, mx, my, delta);

               try {
                  int parsed = Integer.parseInt(countField.load_Str().strip());
                  this.flush_V(selectedXCarryIndex, Math.max(1, parsed));
               } catch (NumberFormatException var55) {
               }
            }

            itemsY = amountY + btnH + 2;
         }
      }

      EventBus strViewport = this.init_EventBus_Map(this.tick_Map, key, x, itemsY, w, selAreaH, 14, 5);
      strViewport.init_V(filtered.size() * 14);
      strViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (!filtered.isEmpty()) {
         int rowAreaW = Math.max(24, w - 5 - 1 - delW - 2 - (destW > 0 ? destW + 2 : 0) - (amountW > 0 ? amountW + 2 : 0));
         strViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstVi = strViewport.pop();

         for (int vi = firstVi; vi < filtered.size() && vi <= strViewport.tick(); vi++) {
            int ri = filtered.get(vi);
            int iy = strViewport.init_I(vi);
            if (iy != -2147483648) {
               Component displayValue = this.init_Component_Str(key, lst.get(ri), ri);
               boolean selected = editable && ri == editIdx;
               boolean hovered = mx >= x && mx < x + rowAreaW && my >= iy && my < iy + 14;
               if (field.captureMode() == StreamUtil.PACKET_NAME) {
                  int textColor = this.init_I_Str(lst.get(ri), ri, selected);
                  autismclient.gui.macro.editor.a.CachePool.init(
                     ctx, this.init_Font, displayValue, autismclient.gui.f.NetHelper.init(x, iy, rowAreaW, 14), hovered, selected, textColor, this.reset_Z(key)
                  );
               } else {
                  autismclient.gui.macro.editor.a.CachePool.init(
                     ctx,
                     this.init_Font,
                     displayValue,
                     autismclient.gui.f.NetHelper.init(x, iy, rowAreaW, 14),
                     hovered,
                     selected,
                     FileCache.Inner_DataUtil.NORMAL,
                     this.reset_Z(key)
                  );
               }

               if (editable) {
                  int fri = ri;
                  ThemedTextInputBox fSlotF2 = hasSlotField ? this.init_Map.get(key + "_slot") : null;
                  this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, rowAreaW, 14, () -> {
                     int curIdx = this.open_Map.getOrDefault(key, -1);
                     if (curIdx == fri) {
                        this.open_Map.put(key, -1);
                        if (af != null) {
                           af.init_V_Str("");
                        }

                        if (fSlotF2 != null) {
                           fSlotF2.init_V_Str("");
                        }

                        ThemedTextInputBox countField = this.init_Map.get(key + "_xcarry_count");
                        if (countField != null) {
                           countField.init_V_Str("");
                        }
                     } else {
                        this.open_Map.put(key, fri);
                        String raw = lst.get(fri);
                        if (af != null) {
                           af.init_V_Str(this.get_Str(raw));
                        }

                        if (fSlotF2 != null) {
                           MacroItemTarget parsed = MacroItemTarget.update(raw);
                           fSlotF2.init_V_Str(parsed.init_Z() ? String.valueOf(parsed.init_I) : "");
                        }

                        ThemedTextInputBox countField = this.init_Map.get(key + "_xcarry_count");
                        if (countField != null) {
                           countField.init_V_Str(String.valueOf(this.write_I(fri)));
                        }
                     }
                  }));
               }

               int actionX = x + rowAreaW + 2;
               if (xcarryList) {
                  int fDestIndex = ri;
                  int dest = this.read_I(fDestIndex);
                  int[] destValues = XCarryAction.init_IArr();
                  List<String> destOptions = XCarryAction.init_List_10();
                  int selectedDestIndex = XCarryAction.init_I_I_6(dest);
                  String ddKey = key + "_xcarry_dest_" + fDestIndex;
                  int ddX = actionX;
                  int ddY = iy + 1;
                  autismclient.gui.f.b.SyncManager dd = this.run_Map
                     .computeIfAbsent(ddKey, k -> new autismclient.gui.f.b.SyncManager(ddX, ddY, destW, 13, destOptions, selectedDestIndex, newIdx -> {
                        if (newIdx >= 0 && newIdx < destValues.length) {
                           this.sync_V(fDestIndex, destValues[newIdx]);
                        }
                     }));
                  dd.init_SyncManager_I(ddX, ddY, destW, 13)
                     .init_SyncManager_List(destOptions)
                     .init_SyncManager_I(selectedDestIndex)
                     .init_SyncManager_IntConsumer(newIdx -> {
                        if (newIdx >= 0 && newIdx < destValues.length) {
                           this.sync_V(fDestIndex, destValues[newIdx]);
                        }
                     });
                  this.sync_List.add(dd);
                  actionX += destW + 2;
                  int fAmountIndex = ri;
                  boolean custom = this.init_aQ_DataUtil_I(fAmountIndex) == XCarryAction.Inner_DataUtil.CUSTOM;
                  String amountLabel = custom ? "x" + this.write_I(fAmountIndex) : "FullStk";
                  this.init_ByteReader_GuiGraphicsExtract_2(
                     ctx,
                     actionX,
                     iy + 1,
                     amountW,
                     13,
                     amountLabel,
                     custom ? ByteReader.Inner_StreamUtil.SUCCESS : ByteReader.Inner_StreamUtil.SECONDARY,
                     true,
                     mx,
                     my,
                     () -> {
                        boolean nowCustom = this.init_aQ_DataUtil_I(fAmountIndex) == XCarryAction.Inner_DataUtil.CUSTOM;
                        this.init_V_I_aQ_DataUtil(fAmountIndex, nowCustom ? XCarryAction.Inner_DataUtil.FULL_STK : XCarryAction.Inner_DataUtil.CUSTOM);
                        this.open_Map.put(key, fAmountIndex);
                        ThemedTextInputBox cf = this.init_Map.get(key + "_xcarry_count");
                        if (cf != null) {
                           cf.init_V_Str(String.valueOf(this.write_I(fAmountIndex)));
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
                     this.close(fRi);
                     this.open(fRi);
                  }

                  if (editable && this.open_Map.getOrDefault(key, -1) == fRi) {
                     this.open_Map.put(key, -1);
                     if (af != null) {
                        af.init_V_Str("");
                     }
                  } else if (editable && this.open_Map.getOrDefault(key, -1) > fRi) {
                     this.open_Map.put(key, this.open_Map.get(key) - 1);
                  }

                  EventBus vp = this.tick_Map.get(key);
                  if (vp != null) {
                     vp.read_V(-1);
                  }

                  if (this.reset_Z(key)) {
                     this.sync_Map.put(key, this.init_List_Str(key));
                  }
               });
            }
         }

         strViewport.init(ctx);
      } else if (emptyHint != null && !emptyHint.isBlank()) {
         int itemW = w - 5 - 1;
         TableIndex.init(ctx, x, itemsY, itemW, 12, false, false);
         FileCache.init(ctx, this.init_Font, emptyHint, x, itemsY, itemW);
      }
   }

   private String pop_Str(String key) {
      if (this.run_Z(key)) {
         List<String> selected = this.write_Map.getOrDefault(key, Collections.emptyList());
         if (selected.isEmpty()) {
            return "Add at least one target before running this audit.";
         }
      }

      return null;
   }

   private void update(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      String key = field.key();
      List<String> sel = this.write_Map.getOrDefault(key, Collections.emptyList());
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      ThemedTextInputBox af = this.flush_Map.get(key);
      String filter = af != null ? af.load_Str().toLowerCase() : "";
      int cy = y;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, field.label() + " (" + sel.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 13;
      int delW = 13;
      int selAreaH = 56;
      EventBus catSelViewport = this.init_EventBus_Map(this.tick_Map, key, x, cy, w, selAreaH, 14, 5);
      catSelViewport.init_V(sel.size() * 14);
      catSelViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (!sel.isEmpty()) {
         int delX = x + w - 5 - 2 - delW;
         catSelViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstSel = catSelViewport.pop();

         for (int i = firstSel; i < sel.size() && i <= catSelViewport.tick(); i++) {
            int siy = catSelViewport.init_I(i);
            if (siy != -2147483648) {
               String display = cc.load(sel.get(i));
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  Component.literal(display),
                  autismclient.gui.f.NetHelper.init(x, siy, delX - x - 1, 13),
                  mx >= x && mx < delX - 1 && my >= siy && my < siy + 13,
                  false,
                  FileCache.Inner_DataUtil.NORMAL,
                  false
               );
               int fi = i;
               this.init_V_GuiGraphicsExtract_2(ctx, delX, siy, delW, mx, my, () -> {
                  sel.remove(fi);
                  EventBus vp = this.tick_Map.get(key);
                  if (vp != null) {
                     vp.read_V(-1);
                  }
               });
            }
         }

         catSelViewport.init(ctx);
      } else {
         FileCache.init(ctx, this.init_Font, "(none selected)", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      TableIndex.init(ctx, x, cy + 1, w, UiColorPalette.c());
      cy += 5;
      if (af != null) {
         af.read(x);
         af.write(cy);
         af.update(w);
         af.init_V(ctx, mx, my, delta);
      }

      cy += 14;
      int headerH = 15;
      int capBtnH2 = 13;
      int capBtnW2 = this.init_I_StreamUtil(field.captureMode(), null, null);
      int cbX = x + w - capBtnW2;
      int headerTextW = Math.max(24, cbX - x - 4);
      String headerLabel = autismclient.gui.f.b.k.init(this.init_Font, "Available Blocks", headerTextW, font, UiColorPalette.parse());
      int headerTextY = j.init(cy, headerH, this.init_CharEncoder.init_I_l(l.read), this.init_CharEncoder.flush());
      this.init_ByteReader_GuiGraphicsExtract(
         ctx, cbX, cy, capBtnW2, capBtnH2, field.captureMode(), false, true, mx, my, () -> this.init_V_FieldDef_List(field, sel)
      );
      autismclient.gui.f.b.k.init(ctx, this.init_Font, headerLabel, font, UiColorPalette.parse(), x, headerTextY, false);
      cy += headerH + 2;
      List<String> filtered = autismclient.gui.macro.editor.a.CachePool.load(
         pop_List(),
         this.pop_Map.computeIfAbsent(key + "_catalog", ignored -> new ArrayList<>()),
         idx -> init_Z_Str_StrArr(filter, idx, clear_Str_Str(idx), cc.load(idx))
      );
      int catItemW = w - 5 - 1;
      EventBus catViewport = this.init_EventBus_Map(this.close_Map, key, x, cy, w, 60, 13, 5);
      catViewport.init_V(filtered.size() * 13);
      catViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      catViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
      int firstItem = catViewport.pop();

      for (int i = firstItem; i < filtered.size() && i <= catViewport.tick(); i++) {
         int iy = catViewport.init_I(i);
         if (iy != -2147483648) {
            String id = filtered.get(i);
            String display = cc.load(id);
            boolean already = sel.contains(id);
            boolean hov = mx >= x && mx < x + catItemW && my >= iy && my < iy + 13;
            autismclient.gui.macro.editor.a.CachePool.init(
               ctx,
               this.init_Font,
               Component.literal(display),
               autismclient.gui.f.NetHelper.init(x, iy, catItemW, 13),
               hov,
               already,
               already ? FileCache.Inner_DataUtil.READY : FileCache.Inner_DataUtil.NORMAL,
               false
            );
            if (!already) {
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, catItemW, 13, () -> {
                  if (!sel.contains(id)) {
                     sel.add(id);
                  }
               }));
            } else {
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, catItemW, 13, () -> sel.remove(id)));
            }
         }
      }

      catViewport.init(ctx);
   }

   private void pool() {
      this.init_Str_2 = this.run_Z ? "" : this.poll_Str();
   }

   private int tick_I() {
      return this.init_Str_2.isBlank() ? 0 : RenderState.init(12);
   }

   private int run_I() {
      return 4 + this.tick_I() + 13 + 4;
   }

   private void load(GuiGraphicsExtractor ctx, int footerY, int mx, int my) {
      int btnH = 13;
      String variableWarning = this.init_Str_2;
      int warnH = this.tick_I();
      int btnY = footerY + 4 + warnH;
      if (this.run_Z || this.isLegacyMulti()) {
         int closeW = this.write_I("Close");
         int closeX = this.bind + (this.f_b_I - closeW) / 2;
         TableIndex.init(ctx, this.bind + 4, footerY, this.f_b_I - 8, UiColorPalette.c());
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, closeX, btnY, closeW, btnH, "Close", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> this.set(false)
         );
      } else {
         String saveLbl = this.init_H != null ? "Save Macro" : "Save";
         int cancelW = this.write_I("Cancel");
         int saveW = this.write_I(saveLbl);
         int gap = 2;
         if (cancelW + saveW + gap > this.f_b_I - 8) {
            cancelW = saveW = this.init_H != null ? 72 : 58;
         }

         int total = cancelW + saveW + gap;
         int sx = this.bind + (this.f_b_I - total) / 2;
         TableIndex.init(ctx, this.bind + 4, footerY, this.f_b_I - 8, UiColorPalette.c());
         if (!variableWarning.isBlank()) {
            Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
            String text = autismclient.gui.f.b.k.init(this.init_Font, variableWarning, this.f_b_I - 8, font, -1);
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               text,
               font,
               -38037,
               this.bind + Math.max(4, (this.f_b_I - this.init_I_Identifier_Str(font, text)) / 2),
               j.init(footerY + 2, warnH, this.init_I_Identifier(font), this.init_CharEncoder.flush()),
               false
            );
         }

         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, sx, btnY, cancelW, btnH, "Cancel", ByteReader.Inner_StreamUtil.SECONDARY, true, mx, my, () -> this.set(false)
         );
         int saveX = sx + cancelW + gap;
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, saveX, btnY, saveW, btnH, saveLbl, ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, () -> this.set(true)
         );
      }
   }

   private String poll_Str() {
      if (this.init_aM != null && this.init_aM.init_List != null && this.f_f_I >= 0) {
         Set<String> used = new LinkedHashSet<>();

         for (ThemedTextInputBox field : this.init_Map.values()) {
            init_V_Set_Str(used, field == null ? "" : field.load_Str());
         }

         for (List<String> values : this.write_Map.values()) {
            for (String value : values) {
               init_V_Set_Str(used, value);
            }
         }

         used.removeAll(this.init_Set());
         if (used.isEmpty()) {
            return "";
         }

         used.removeAll(autismclient.util.macro.MacroTemplateResolver.init_Set);
         if (used.isEmpty()) {
            return "";
         }

         Map<String, Integer> providerIndex = new LinkedHashMap<>();

         for (int i = 0; i < this.init_aM.init_List.size(); i++) {
            LinkedHashSet<String> supplied = new LinkedHashSet<>();
            init_V_Set_MacroAction(supplied, this.init_aM.init_List.get(i));

            for (String token : supplied) {
               LinkedHashSet<String> roots = new LinkedHashSet<>();
               init_V_Set_Str(roots, token);

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

            if (provider >= this.f_f_I) {
               return "Captured later";
            }

            if (this.init_Z_I(provider) && !this.init_Z_I_I(provider, this.f_f_I)) {
               return "Race-only value";
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private Set<String> init_Set() {
      LinkedHashSet<String> outputs = new LinkedHashSet<>();
      if (this.init_MacroAction instanceof CaptureOutputAction captureOutput) {
         String root = MacroVariableStore.load(captureOutput.load());
         if (!root.isBlank()) {
            outputs.add(root);
         }
      }

      String pattern = "";
      PatternCaptureMatcher.Inner_DataUtil mode = PatternCaptureMatcher.Inner_DataUtil.MATCH;
      if (this.init_MacroAction instanceof WaitForChatAction) {
         ThemedTextInputBox field = this.init_Map.get("pattern");
         pattern = field == null ? "" : field.load_Str();
         mode = this.init_UtilCore_DataUtil();
      } else if (this.init_MacroAction instanceof WaitForGuiAction) {
         ThemedTextInputBox field = this.init_Map.get("guiTitle");
         pattern = field == null ? "" : field.load_Str();
         mode = this.init_UtilCore_DataUtil();
      } else if (this.init_MacroAction instanceof WaitGuiTypeAction) {
         ThemedTextInputBox field = this.init_Map.get("title");
         pattern = field == null ? "" : field.load_Str();
         mode = this.init_UtilCore_DataUtil();
      } else if (this.init_MacroAction instanceof autismclient.util.macro.CaptureValueAction) {
         ThemedTextInputBox field = this.init_Map.get("pattern");
         pattern = field == null ? "" : field.load_Str();
         mode = this.init_UtilCore_DataUtil();
      }

      if (mode != PatternCaptureMatcher.Inner_DataUtil.MATCH) {
         outputs.addAll(PatternCaptureMatcher.init(mode, pattern));
      }

      return outputs;
   }

   private PatternCaptureMatcher.Inner_DataUtil init_UtilCore_DataUtil() {
      try {
         return PatternCaptureMatcher.Inner_DataUtil.valueOf(this.tick_Str("matchMode"));
      } catch (IllegalArgumentException | NullPointerException ignored) {
         return PatternCaptureMatcher.Inner_DataUtil.MATCH;
      }
   }

   private static void init_V_Set_Str(Set<String> out, String text) {
      if (out != null && text != null && !text.isBlank()) {
         Matcher matcher = VARIABLE_REF.matcher(text);

         while (matcher.find()) {
            String name = matcher.group(1).toLowerCase(Locale.ROOT);
            int dot = name.indexOf(46);
            out.add(dot < 0 ? name : name.substring(0, dot));
         }
      }
   }

   private boolean init_Z_I(int index) {
      return this.load_I_I(index) >= 0;
   }

   private boolean init_Z_I_I(int left, int right) {
      int leftHeader = this.load_I_I(left);
      return leftHeader >= 0 && leftHeader == this.load_I_I(right);
   }

   private int load_I_I(int index) {
      if (this.init_aM != null && this.init_aM.init_List != null && index >= 0) {
         for (int i = 0; i < Math.min(index, this.init_aM.init_List.size()); i++) {
            MacroAction action = this.init_aM.init_List.get(i);
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

   private void index() {
      this.level();
      this.poll_List = new ArrayList<>();

      for (AutismOverlay o : OverlayManager.init_bk().init_List()) {
         if (o != this && o.sync_Z()) {
            o.a__V();
            this.poll_List.add(o);
            o.init_V_Z(false);
         }
      }

      this.a__V();
      this.push_V();
      this.sync_Z = this.write_Z;
      this.write_Z = false;
      ck.init_ck().cache(true);
   }

   private void close_V(String key) {
      if (this.put_Z()) {
         this.init_StreamUtil.init(key, this::count, () -> this.init_V_Z_Z(false, false));
      }
   }

   private void count() {
      Screen current = init_Minecraft.gui.screen();
      boolean hasSlotScreen = current instanceof AbstractContainerScreen;
      this.open_Z = false;
      this.index();
      if (!hasSlotScreen) {
         this.load_Screen = current;
         this.open_Z = true;
         init_Minecraft.execute(() -> {
            if (init_Minecraft.player != null) {
               OverlayScreenController.init_V(init_Minecraft, new InventoryScreen(init_Minecraft.player));
            }
         });
      }
   }

   private void flags() {
      if (this.poll_List != null) {
         OverlayManager manager = OverlayManager.init_bk();

         for (AutismOverlay overlay : this.poll_List) {
            manager.init_V(overlay);
            overlay.init_V_Z(true);
            overlay.a__V();
         }

         this.poll_List = null;
      }
   }

   private void get(boolean cancelGBreak) {
      ck state = ck.init_ck();
      state.read_V_Runnable(null);
      state.init_V_Consumer(null);
      state.init_V_BiConsumer(null);
      state.load_V_Consumer(null);
      state.load_V_Runnable(null);
      state.value(false);
      if (cancelGBreak && state.cache_Z()) {
         state.open_V();
      }
   }

   private void init_V_Z_Z(boolean reopenInventory, boolean closeCurrentScreen) {
      this.flags();
      this.get(false);
      ck.init_ck().state(false);
      ck.init_ck().cache(false);
      this.write_Z = this.sync_Z;
      this.sync_Z = false;
      this.valid_List.clear();
      this.size_List.clear();
      OverlayManager.init_bk().update_V(this);
      if (this.open_Z) {
         Screen restore = this.load_Screen;
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() instanceof InventoryScreen) {
               if (restore != null) {
                  OverlayScreenController.init_V(init_Minecraft, restore);
               }
            } else if (restore == null && init_Minecraft.gui.screen() == null && init_Minecraft.player != null) {
               OverlayScreenController.init_V(init_Minecraft, new InventoryScreen(init_Minecraft.player));
            } else if (restore != null && init_Minecraft.gui.screen() == null) {
               OverlayScreenController.init_V(init_Minecraft, restore);
            }
         });
         this.open_Z = false;
      } else if (reopenInventory && this.load_Screen != null) {
         final Screen restore = this.load_Screen;
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() == null) {
               OverlayScreenController.init_V(init_Minecraft, restore);
            }
         });
      } else if (reopenInventory && init_Minecraft.player != null) {
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() == null && init_Minecraft.player != null) {
               OverlayScreenController.init_V(init_Minecraft, new InventoryScreen(init_Minecraft.player));
            }
         });
      }

      if (closeCurrentScreen) {
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
      }

      this.load_Screen = null;
      this.fill();
   }

   private void set(boolean save) {
      this.auditEditSession++;
      this.flags();
      this.sync_Z = false;
      this.get(true);
      ck.init_ck().cache(false);
      this.level();
      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
         autismclient.util.BlockPreviewRenderer.load();
      }

      if (save && this.init_MacroAction != null && !this.isLegacyMulti()) {
         if (this.load_Runnable != null) {
            this.load_Runnable.run();
         }

         this.frame();
         this.init_MacroAction.fromTag(this.init_CompoundTag);
         DynamicBindingManager.init(this.init_MacroAction, this.init_CompoundTag);
         if (this.init_Runnable != null) {
            this.init_Runnable.run();
         }
      }

      this.init_AutismPacketSelect.write_V_21();
      this.init_RaceStepSelectorOv.load_V_3();
      this.write_Z = false;
      this.init_MacroAction = null;
      this.f_d_I = 1;
      this.run_Z = false;
      this.init_H = null;
      this.init_ActionEditorOverla_2 = null;
      this.init_ActionEditorOverla = null;
      this.init_CompoundTag = null;
      this.init_ActionFieldSchema = null;
      this.init_MapStore = null;
      this.init_StreamUtil.init_V();
      if (this.write_Str != null) {
         D.read();
         this.write_Str = null;
      }

      this.sync_List.clear();
      this.run_Map.clear();
      this.push_List = null;
      this.pop_List = null;
      this.tick_List = null;
      this.init_J_TaskQueue = null;
      this.init_IArr = null;
      this.init_PacketUtil = null;
      this.update_Map.clear();
      this.sync_Map.clear();
      this.clear_List = null;
      this.prev_List = null;
      this.load_Runnable = null;
      this.init_Runnable = null;
      this.init_Screen = null;
      this.load_Screen = null;
   }

   private void level() {
      this.init_f.init_V();
   }

   private void init_V_Str_I(String message, int accentColor) {
      this.init_f.init(message, accentColor);
   }

   public boolean run_Z() {
      return this.init_f.init_Z();
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int anchorX, int anchorY, int anchorWidth) {
      this.init_f.init(UiHelper.init(context, this.init_Font, anchorX, anchorY), anchorX, anchorY, anchorWidth);
   }

   private void load_V_MacroAction(MacroAction action) {
      for (FieldDef field : this.init_ActionFieldSchema.fields()) {
         String key = field.key();
         switch (field.type()) {
            case TEXT:
            case MACRO_SELECT: {
               String v = this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getStringOr(key, "") : "";
               ThemedTextInputBox f = this.init_k_I(80);
               f.init_V_Str(v);
               if ("saveAs".equals(key) && (v.isEmpty() || init_Pattern.matcher(v).matches())) {
                  f.load(sx -> sx.isEmpty() || init_Pattern.matcher(sx).matches());
               }

               this.init_Map.put(key, f);
               break;
            }
            case NUMBER: {
               int v = this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getIntOr(key, 0) : 0;
               ThemedTextInputBox f = this.init_k_I(80);
               if (!field.dynamic()) {
                  f.poll(true);
               }

               String binding = DynamicBindingManager.init(action, key);
               f.init_V_Str(binding.isBlank() ? String.valueOf(v) : binding);
               this.init_Map.put(key, f);
               break;
            }
            case DECIMAL: {
               double v = this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getDoubleOr(key, 0.0) : 0.0;
               ThemedTextInputBox f = this.init_k_I(80);
               if (!field.dynamic()) {
                  f.load(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*\\.?\\d*"));
               }

               String binding = DynamicBindingManager.init(action, key);
               f.init_V_Str(binding.isBlank() ? init_Str_D(v) : binding);
               this.init_Map.put(key, f);
               break;
            }
            case SLOT: {
               int v = this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getIntOr(key, 0) : 0;
               ThemedTextInputBox f = this.init_k_I(50);
               if (!field.dynamic()) {
                  f.load(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*"));
               }

               String binding = DynamicBindingManager.init(action, key);
               f.init_V_Str(binding.isBlank() ? String.valueOf(v) : binding);
               this.init_Map.put(key, f);
               break;
            }
            case BLOCK_POS: {
               String[] xyzKeys = field.xyzKeys();
               boolean dbl = field.xyzDouble();

               for (int i = 0; i < 3; i++) {
                  ThemedTextInputBox f = this.init_k_I(50);
                  if (dbl) {
                     if (!field.dynamic()) {
                        f.load(sx -> sx.isEmpty() || sx.equals("-") || sx.matches("-?\\d*\\.?\\d*"));
                     }

                     double v = this.init_CompoundTag.contains(xyzKeys[i]) ? this.init_CompoundTag.getDoubleOr(xyzKeys[i], 0.0) : 0.0;
                     String binding = DynamicBindingManager.init(action, xyzKeys[i]);
                     f.init_V_Str(binding.isBlank() ? init_Str_D(v) : binding);
                  } else {
                     if (!field.dynamic()) {
                        f.poll(true);
                     }

                     int v = this.init_CompoundTag.contains(xyzKeys[i]) ? this.init_CompoundTag.getIntOr(xyzKeys[i], 0) : 0;
                     String binding = DynamicBindingManager.init(action, xyzKeys[i]);
                     f.init_V_Str(binding.isBlank() ? String.valueOf(v) : binding);
                  }

                  this.init_Map.put(key + "_" + i, f);
               }
               break;
            }
            case TOGGLE:
               this.load_Map.put(key, this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getBooleanOr(key, false) : false);
               break;
            case ENUM: {
               String v = this.init_CompoundTag.contains(key) ? this.init_CompoundTag.getStringOr(key, "") : "";
               List<String> opts = field.enumOptions();
               int idx = opts.indexOf(v);
               if (idx < 0) {
                  idx = opts.indexOf(v.toUpperCase());
               }

               this.read_Map.put(key, Math.max(0, idx));
               if (field.dynamic()) {
                  ThemedTextInputBox bindingField = this.init_k_I(80);
                  bindingField.init_V_Str(DynamicBindingManager.init(action, key));
                  this.init_Map.put(key, bindingField);
               }
               break;
            }
            case STRING_LIST:
               List<String> list = new ArrayList<>();
               if (this.init_CompoundTag.contains(key)) {
                  for (Tag el : this.init_CompoundTag.getList(key).orElse(new ListTag())) {
                     String s = el.asString().orElse("");
                     if (!s.isEmpty()) {
                        list.add(s);
                     }
                  }
               }

               this.write_Map.put(key, list);
               ThemedTextInputBox af = this.init_k_I(80);
               if (field.captureMode() == StreamUtil.BLOCK_CATALOG) {
                  af.init_V_Component(Component.literal("Search blocks..."));
               } else {
                  af.init_V_Component(Component.literal("Search / " + field.addLabel()));
                  af.init_V_Predicate(text -> {
                     if (!text.isBlank()) {
                        List<String> entries = this.write_Map.get(key);
                        if (entries != null && this.init_Z_FieldDef(field, entries, text.strip())) {
                           af.init_V_Str("");
                        }
                     }

                     return true;
                  });
               }

               this.flush_Map.put(key, af);
         }
      }

      if (action instanceof ToolsAction) {
         for (FieldDef field : this.init_ActionFieldSchema.fields()) {
            if (field.key().startsWith("toolArg.")) {
               ThemedTextInputBox input = this.init_Map.get(field.key());
               if (input != null && !this.init_CompoundTag.contains(field.key())) {
                  input.init_V_Str("");
               }

               if (input != null) {
                  input.init_V_Component(Component.literal("Default"));
               }
            }
         }
      }
   }

   private void init_V_FieldDef_I(FieldDef field, int index) {
      if (index >= 0 && index < field.enumOptions().size()) {
         boolean changed = index != this.read_Map.getOrDefault(field.key(), 0);
         this.read_Map.put(field.key(), index);
         if (changed && this.init_MacroAction instanceof ToolsAction && "toolId".equals(field.key())) {
            this.frame();
            this.init_CompoundTag.putString("argumentsJson", "{}");

            for (FieldDef oldField : this.init_ActionFieldSchema.fields()) {
               if (oldField.key().startsWith("toolArg.")) {
                  this.open_V(oldField.key());
               }
            }

            for (String key : new ArrayList<>(this.init_CompoundTag.keySet())) {
               if (key.startsWith("toolArg.")) {
                  this.init_CompoundTag.remove(key);
               }
            }

            ToolsAction draft = new ToolsAction();
            draft.fromTag(this.init_CompoundTag);
            DynamicBindingManager.init(draft, this.init_CompoundTag);
            this.init_ActionFieldSchema = BufferHelper.init(draft);
            this.init_Map.clear();
            this.load_Map.clear();
            this.read_Map.clear();
            this.run_Map.clear();
            this.load_V_MacroAction(draft);
            this.node();
            this.f_e_I = 0;
         }
      }
   }

   private void frame() {
      if (this.init_CompoundTag != null) {
         if (this.init_MapStore != null) {
            if (this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()) {
               this.flush_V_2();
            }

            for (int i = 0; i < this.init_MapStore.itemNames.size(); i++) {
               ThemedTextInputBox tf = this.init_Map.get("item_times_" + i);
               if (tf != null) {
                  try {
                     while (this.init_MapStore.itemTimes.size() <= i) {
                        this.init_MapStore.itemTimes.add(1);
                     }

                     this.init_MapStore.itemTimes.set(i, Math.max(1, Integer.parseInt(tf.load_Str().strip())));
                  } catch (NumberFormatException var23) {
                  }
               }
            }

            this.init_MapStore.waitForGuiBefore = this.load_Map.getOrDefault("item_waitForGuiBefore", false);
            this.init_MapStore.waitForGuiAfter = this.load_Map.getOrDefault("item_waitForGuiAfter", false);
            this.init_MapStore.waitForItem = this.load_Map.getOrDefault("item_waitForItem", false);
            this.init_MapStore.useCursorItemForPickupAll = this.load_Map.getOrDefault("item_useCursorItemForPickupAll", true);
            this.init_MapStore.useClickDelay = this.load_Map.getOrDefault("item_useClickDelay", false);
            ThemedTextInputBox delayF = this.init_Map.get("item_clickDelayMs");
            if (delayF != null) {
               try {
                  this.init_MapStore.clickDelayMs = Math.max(0, Integer.parseInt(delayF.load_Str().strip()));
               } catch (NumberFormatException var22) {
               }
            }

            ThemedTextInputBox guiF = this.init_Map.get("item_guiName");
            if (guiF != null) {
               this.init_MapStore.guiName = guiF.load_Str();
            }

            if (!this.init_MapStore.waitForGuiBefore && !this.init_MapStore.waitForGuiAfter) {
               this.init_MapStore.guiName = "";
            }

            if (!this.init_MapStore.itemNames.isEmpty()) {
               this.init_MapStore.useSlot = false;
               this.init_MapStore.targetSlot = -1;
               this.init_MapStore.actionIndex = 0;
               this.init_MapStore.button = 0;
               this.init_MapStore.times = 1;
            }

            this.init_CompoundTag = this.init_MapStore.toTag();
         } else if (this.push_List != null) {
            for (int i = 0; i < this.push_List.size(); i++) {
               autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.push_List.get(i);
               ThemedTextInputBox af = this.init_Map.get("craft_amount_" + i);
               if (af != null) {
                  try {
                     entry.read_I = Math.max(1, Integer.parseInt(af.load_Str().strip()));
                  } catch (NumberFormatException var21) {
                  }
               }

               entry.init_Z = this.load_Map.getOrDefault("craft_useMax_" + i, false);
            }

            ListTag entryTags = new ListTag();

            for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : this.push_List) {
               if (entry.init_Z()) {
                  entryTags.add(entry.init_CompoundTag());
               }
            }

            this.init_CompoundTag.put("entries", entryTags);
         } else if (this.init_PacketUtil != null) {
            if (this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()) {
               this.reset_V();
            }

            while (this.init_PacketUtil.itemCounts.size() < this.init_PacketUtil.itemNames.size()) {
               this.init_PacketUtil.itemCounts.add(this.init_PacketUtil.init_I_I_4(this.init_PacketUtil.itemCounts.size()));
            }

            for (int i = 0; i < this.init_PacketUtil.itemNames.size(); i++) {
               ThemedTextInputBox f = this.init_Map.get("drop_count_" + i);
               if (f != null) {
                  try {
                     this.init_PacketUtil.itemCounts.set(i, Math.max(0, Integer.parseInt(f.load_Str().strip())));
                  } catch (NumberFormatException var20) {
                  }
               }
            }

            ThemedTextInputBox cntF = this.init_Map.get("drop_globalCount");
            if (cntF != null) {
               try {
                  this.init_PacketUtil.dropCount = Math.max(1, Integer.parseInt(cntF.load_Str().strip()));
               } catch (NumberFormatException var19) {
               }
            }

            DropAction.Inner_DataUtil[] modes = DropAction.Inner_DataUtil.values();
            this.init_PacketUtil.mode = modes[Math.min(this.read_Map.getOrDefault("drop_mode", 0), modes.length - 1)];
            this.init_PacketUtil.waitForGuiBefore = this.load_Map.getOrDefault("drop_waitForGuiBefore", false);
            this.init_PacketUtil.waitForGuiAfter = this.load_Map.getOrDefault("drop_waitForGuiAfter", false);
            this.init_PacketUtil.useHandlerSlots = this.load_Map.getOrDefault("drop_useHandlerSlots", true);
            ThemedTextInputBox guiF = this.init_Map.get("drop_guiName");
            if (guiF != null) {
               this.init_PacketUtil.guiName = guiF.load_Str();
            }

            if (!this.init_PacketUtil.waitForGuiBefore && !this.init_PacketUtil.waitForGuiAfter) {
               this.init_PacketUtil.guiName = "";
            }

            this.init_CompoundTag = this.init_PacketUtil.toTag();
            this.init_CompoundTag.putString("packetOrder", this.tick_Str("packetOrder"));
         } else if (this.init_H != null) {
            this.init_H = this.load_H();
            this.init_CompoundTag = this.init_H.toTag();
         } else {
            if (this.clear_List != null) {
               for (int i = 0; i < this.clear_List.size(); i++) {
                  WaitForLanStepAction.Inner_DataUtil e = this.clear_List.get(i);
                  ThemedTextInputBox uf = this.init_Map.get("lan_user_" + i);
                  if (uf != null) {
                     e.init_Str = uf.load_Str();
                  }

                  ThemedTextInputBox sf = this.init_Map.get("lan_step_" + i);
                  if (sf != null) {
                     try {
                        e.init_I = Math.max(1, Integer.parseInt(sf.load_Str().strip()));
                     } catch (NumberFormatException var18) {
                     }
                  }
               }

               ListTag entryList = new ListTag();

               for (WaitForLanStepAction.Inner_DataUtil e : this.clear_List) {
                  entryList.add(e.init_CompoundTag());
               }

               this.init_CompoundTag.put("entries", entryList);
               this.init_CompoundTag.putBoolean("filterByUser", this.load_Map.getOrDefault("lan_filterByUser", false));
               this.init_CompoundTag.putBoolean("listenDuringPreviousAction", this.load_Map.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox dsF = this.init_Map.get("lan_defaultStep");
               if (dsF != null) {
                  try {
                     this.init_CompoundTag.putInt("defaultStep", Math.max(1, Integer.parseInt(dsF.load_Str().strip())));
                  } catch (NumberFormatException var17) {
                  }
               }
            }

            if (this.prev_List != null) {
               ListTag entriesTag = new ListTag();

               for (ak.Inner_DataUtil entry : this.prev_List) {
                  if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
                     entriesTag.add(entry.init());
                  }
               }

               this.init_CompoundTag.put("entries", entriesTag);
               this.init_CompoundTag.putString("moduleName", "");
               this.init_CompoundTag.putString("toggleMode", ak.Inner_StreamUtil.init.name());
            } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET) {
               List<String> targets = this.init_List_List(this.close_List());
               ListTag packetList = new ListTag();

               for (String target : targets) {
                  packetList.add(StringTag.valueOf(target));
               }

               this.init_CompoundTag.put("packetNames", packetList);
               this.init_CompoundTag.putString("packetName", targets.isEmpty() ? "" : targets.get(0));
               this.init_CompoundTag.putBoolean("listenDuringPreviousAction", this.load_Map.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox saveAs = this.init_Map.get("saveAs");
               if (saveAs != null) {
                  this.init_CompoundTag.putString("saveAs", saveAs.load_Str());
               }
            } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
               this.y();
               ListTag rules = new ListTag();
               if (this.reset_List != null) {
                  for (autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule : this.reset_List) {
                     if (rule != null && (rule.init_Str != null && !rule.init_Str.isBlank() || rule.load != null && !rule.load.isBlank())) {
                        rules.add(rule.init_CompoundTag());
                     }
                  }
               }

               this.init_CompoundTag.put("rules", rules);
               this.init_CompoundTag.putBoolean("listenDuringPreviousAction", this.load_Map.getOrDefault("listenDuringPreviousAction", false));
               ThemedTextInputBox saveAs = this.init_Map.get("saveAs");
               if (saveAs != null) {
                  this.init_CompoundTag.putString("saveAs", saveAs.load_Str());
               }
            } else if (this.init_ActionFieldSchema != null) {
               for (FieldDef field : this.init_ActionFieldSchema.fields()) {
                  String key = field.key();
                  switch (field.type()) {
                     case TEXT:
                     case MACRO_SELECT:
                        ThemedTextInputBox f = this.init_Map.get(key);
                        if (f != null) {
                           this.init_CompoundTag.putString(key, f.load_Str());
                        }
                        break;
                     case NUMBER:
                     case SLOT:
                        ThemedTextInputBox fx = this.init_Map.get(key);
                        if (fx == null) {
                           break;
                        }

                        String raw = fx.load_Str().strip();
                        if (field.dynamic() && raw.contains("{")) {
                           this.read_V_Str_Str(key, raw);
                        } else {
                           this.open_V(key);
                           if (this.init_MacroAction instanceof ToolsAction && key.startsWith("toolArg.") && raw.isBlank()) {
                              this.init_CompoundTag.putString(key, "");
                           } else {
                              try {
                                 this.init_CompoundTag.putInt(key, Integer.parseInt(raw));
                              } catch (NumberFormatException var16) {
                              }
                           }
                        }
                        break;
                     case DECIMAL:
                        ThemedTextInputBox fxDbl = this.init_Map.get(key);
                        if (fxDbl == null) {
                           break;
                        }

                        String rawDbl = fxDbl.load_Str().strip();
                        if (field.dynamic() && rawDbl.contains("{")) {
                           this.read_V_Str_Str(key, rawDbl);
                        } else {
                           this.open_V(key);
                           if (this.init_MacroAction instanceof ToolsAction && key.startsWith("toolArg.") && rawDbl.isBlank()) {
                              this.init_CompoundTag.putString(key, "");
                           } else {
                              try {
                                 this.init_CompoundTag.putDouble(key, Double.parseDouble(rawDbl));
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
                           ThemedTextInputBox fXyz = this.init_Map.get(key + "_" + i);
                           if (fXyz != null) {
                              String t = fXyz.load_Str().strip();
                              if (field.dynamic() && t.contains("{")) {
                                 this.read_V_Str_Str(xyzKeys[i], t);
                              } else {
                                 this.open_V(xyzKeys[i]);
                                 if (dbl) {
                                    try {
                                       this.init_CompoundTag.putDouble(xyzKeys[i], Double.parseDouble(t));
                                    } catch (NumberFormatException var14) {
                                    }
                                 } else {
                                    try {
                                       this.init_CompoundTag.putInt(xyzKeys[i], Integer.parseInt(t));
                                    } catch (NumberFormatException var13) {
                                    }
                                 }
                              }
                           }
                        }
                        break;
                     case TOGGLE:
                        this.init_CompoundTag.putBoolean(key, this.load_Map.getOrDefault(key, false));
                        break;
                     case ENUM:
                        ThemedTextInputBox bindingField = field.dynamic() ? this.init_Map.get(key) : null;
                        String binding = bindingField == null ? "" : bindingField.load_Str().strip();
                        if (field.dynamic() && !binding.isBlank()) {
                           this.read_V_Str_Str(key, binding);
                        } else {
                           this.open_V(key);
                           List<String> opts = field.enumOptions();
                           if (!opts.isEmpty()) {
                              int idx = Math.min(this.read_Map.getOrDefault(key, 0), opts.size() - 1);
                              this.init_CompoundTag.putString(key, opts.get(idx));
                           }
                        }
                        break;
                     case STRING_LIST:
                        List<String> items = this.write_Map.getOrDefault(key, Collections.emptyList());
                        ListTag nbt = new ListTag();

                        for (String s : items) {
                           nbt.add(StringTag.valueOf(s));
                        }

                        this.init_CompoundTag.put(key, nbt);
                  }
               }

               this.drain();
               if (this.init_MacroAction instanceof GuiWaitAware && !this.next_Z()) {
                  this.init_CompoundTag.putString("guiName", "");
                  this.init_CompoundTag.putString("waitGuiName", "");
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY) {
                  String mode = this.tick_Str("mode");
                  if ("CLOSE".equals(mode)) {
                     this.init_CompoundTag.putBoolean("waitForGuiBefore", false);
                     this.init_CompoundTag.putBoolean("waitForGuiAfter", false);
                  }

                  this.init_CompoundTag.putString("guiName", "");
                  this.init_CompoundTag.putBoolean("sendPacket", !this.load_Map.getOrDefault("sendPacket", false));
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
                  ListTag nbtEntries = new ListTag();
                  if (this.run_List != null) {
                     for (WaitForSlotChangeAction.Inner_DataUtil e : this.run_List) {
                        CompoundTag ec = new CompoundTag();
                        MacroItemTarget target = e.init_ArrayUtil();
                        if (target.init_Z() || target.update()) {
                           ec.put("target", target.init_CompoundTag());
                        }

                        ec.putString("mode", e.init_aD_StreamUtil.name());
                        ec.putInt("count", Math.max(1, e.init_I));
                        nbtEntries.add(ec);
                     }
                  }

                  this.init_CompoundTag.put("entries", nbtEntries);
                  this.init_CompoundTag.putBoolean("listenDuringPreviousAction", this.load_Map.getOrDefault("listenDuringPreviousAction", false));
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.USE_ITEM) {
                  ThemedTextInputBox slotField = this.init_Map.get("slot");
                  if (slotField == null || slotField.load_Str() == null || slotField.load_Str().strip().isEmpty()) {
                     this.init_CompoundTag.remove("slot");
                  }

                  String mode = this.init_CompoundTag.getStringOr("useMode", "AUTOMATIC");
                  if ("CUSTOM_HOLD".equals(mode)) {
                     this.init_CompoundTag.putInt("useCount", 1);
                     if (this.init_CompoundTag.getIntOr("holdTicks", 0) <= 0) {
                        this.init_CompoundTag.putInt("holdTicks", 20);
                     }
                  } else if (this.init_CompoundTag.getIntOr("useCount", 0) <= 0) {
                     this.init_CompoundTag.putInt("useCount", 1);
                  }
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.NBT_BOOK) {
                  this.block();
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SWAP_SLOTS) {
                  boolean fromUseItemName = this.init_CompoundTag.getBooleanOr("fromUseItemName", false);
                  boolean toUseItemName = this.init_CompoundTag.getBooleanOr("toUseItemName", false);
                  if (fromUseItemName) {
                     this.init_CompoundTag.putInt("fromSlot", -1);
                  } else {
                     this.init_CompoundTag.putString("fromItemName", "");
                  }

                  if (toUseItemName) {
                     this.init_CompoundTag.putInt("toSlot", -1);
                  } else {
                     this.init_CompoundTag.putString("toItemName", "");
                  }
               }

               if (this.init_MacroAction != null
                  && this.init_MacroAction.getType() == MacroActionType.GO_TO
                  && !this.init_CompoundTag.getBooleanOr("waitForArrival", false)) {
                  this.init_CompoundTag.putDouble("arrivalRadius", 2.0);
                  this.init_CompoundTag.putInt("timeoutMs", 60000);
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CLICK) {
                  if (!this.next_Z()) {
                     this.init_CompoundTag.putString("guiName", "");
                  }

                  if ("MIDDLE".equals(this.init_CompoundTag.getStringOr("clickType", "RIGHT"))) {
                     this.init_CompoundTag.putString("clickType", "RIGHT");
                  }

                  if (this.init_CompoundTag.getIntOr("clickCount", 0) <= 0) {
                     this.init_CompoundTag.putInt("clickCount", 1);
                  }
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.XCARRY) {
                  this.init_CompoundTag.putInt("safeClickDelayTicks", XCarryAction.write(this.init_CompoundTag.getIntOr("safeClickDelayTicks", 1)));
                  this.init_CompoundTag
                     .putBoolean(
                        "safeClickDelayAfterPickup",
                        this.load_Map.getOrDefault("safeClickDelayAfterPickup", this.init_CompoundTag.getBooleanOr("safeClickDelayAfterPickup", true))
                     );
                  this.init_CompoundTag
                     .putBoolean(
                        "safeClickDelayBeforeReturn",
                        this.load_Map.getOrDefault("safeClickDelayBeforeReturn", this.init_CompoundTag.getBooleanOr("safeClickDelayBeforeReturn", true))
                     );
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DISCONNECT) {
                  String mode = this.init_CompoundTag.getStringOr("mode", "DISCONNECT");
                  if ("DISCONNECT".equals(mode)) {
                     this.init_CompoundTag.putBoolean("useNextAction", false);
                  } else if (this.init_CompoundTag.getIntOr("packetCount", 0) <= 0) {
                     this.init_CompoundTag.putInt("packetCount", 200);
                  }

                  if (!"KICK_DUPE".equals(mode)) {
                     this.init_CompoundTag.putBoolean("useNextAction", false);
                  }
               }

               if (this.init_MacroAction != null
                  && this.init_MacroAction.getType() == MacroActionType.BREAK
                  && !this.init_CompoundTag.getBooleanOr("interact", false)) {
                  this.init_CompoundTag.putBoolean("runNextSteps", false);
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CLOSE_GUI) {
                  this.init_CompoundTag.putBoolean("sendPacket", !this.load_Map.getOrDefault("sendPacket", false));
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SAVE_GUI) {
                  this.init_CompoundTag.putBoolean("sendPacket", !this.load_Map.getOrDefault("sendPacket", false));
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.STORE_ITEM) {
                  this.init_CompoundTag.putBoolean("closeSendPkt", !this.load_Map.getOrDefault("closeSendPkt", false));
               }

               if (this.init_MacroAction != null
                  && (this.init_MacroAction.getType() == MacroActionType.OPEN_CONTAINER || this.init_MacroAction.getType() == MacroActionType.INTERACT_ENTITY)) {
                  List<String> selectedTargets = this.write_Map.getOrDefault("entityTargets", Collections.emptyList());
                  String entityTarget = selectedTargets.isEmpty() ? "" : selectedTargets.get(0);
                  this.init_CompoundTag.putString("entityTarget", entityTarget);
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_CHAT) {
                  this.init_CompoundTag.putInt("fuzzyPercent", WaitForChatAction.init_I_I_8(this.put_I()));
                  if (!this.next_Z()) {
                     this.init_CompoundTag.putString("waitGuiName", "");
                  }
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
                  if (!this.init_CompoundTag.getBooleanOr("manualDirection", false)) {
                     this.init_CompoundTag.remove("direction");
                  }

                  this.init_CompoundTag.putBoolean("waitForItem", this.load_Map.getOrDefault("place_waitForItem", true));
                  this.init_CompoundTag.putBoolean("silentSwitch", this.load_Map.getOrDefault("place_silentSwitch", false));
               }
            }
         }
      }
   }

   private void read_V_Str_Str(String key, String template) {
      if (this.init_CompoundTag != null && key != null && !key.isBlank()) {
         CompoundTag bindings = this.init_CompoundTag.get("_dynamicBindings") instanceof CompoundTag existing ? existing : new CompoundTag();
         bindings.putString(key, template == null ? "" : template);
         this.init_CompoundTag.put("_dynamicBindings", bindings);
      }
   }

   private void open_V(String key) {
      if (this.init_CompoundTag != null && key != null && this.init_CompoundTag.get("_dynamicBindings") instanceof CompoundTag bindings) {
         bindings.remove(key);
         if (bindings.isEmpty()) {
            this.init_CompoundTag.remove("_dynamicBindings");
         }
      }
   }

   private void block() {
      if (this.init_CompoundTag != null) {
         int pages = this.init_CompoundTag.getIntOr("pages", 100);
         if (pages <= 0) {
            this.init_CompoundTag.putInt("pages", 100);
         }

         int chars = this.init_CompoundTag.getIntOr("characters", 1024);
         if (chars <= 0) {
            this.init_CompoundTag.putInt("characters", 1024);
         }

         if (!this.init_CompoundTag.contains("sign")) {
            this.init_CompoundTag.putBoolean("sign", true);
         }

         if (!this.init_CompoundTag.contains("appendCount")) {
            this.init_CompoundTag.putBoolean("appendCount", true);
         }

         String source = this.tick_Str("dataSource");
         if (source == null || source.isBlank()) {
            source = "Random";
         }

         this.init_CompoundTag.putString("dataSource", source);
         if ("Random".equals(source)) {
            this.init_CompoundTag.putString("customText", "");
            this.init_CompoundTag.putString("customFilePath", "");
         } else if ("Pasted".equals(source)) {
            this.init_CompoundTag.putString("customFilePath", "");
         } else if ("File".equals(source)) {
            this.init_CompoundTag.putString("customText", "");
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mx, double my, int button) {
      this.variableWarningStale = true;
      if (!this.write_Z) {
         return false;
      }

      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_D_3(mx, my, button);
      }

      if (this.init_RaceStepSelectorOv.sync_Z()) {
         return this.init_RaceStepSelectorOv.init_Z_D_3(mx, my, button);
      }

      int imx = (int)mx;
      int imy = (int)my;
      if (!this.sync_List.isEmpty()) {
         if (autismclient.gui.f.b.SyncManager.init_Z_List_2(this.sync_List, mx, my, button)) {
            OverlayManager.init_bk().update_V(this);
            return true;
         }

         if (autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List)) {
            return true;
         }
      }

      if (this.write(mx, my, this.init_cH())) {
         this.set(false);
         return true;
      }

      if (this.load_Z_D_D(mx, my)) {
         this.update_Z = true;
         this.load_D = mx - this.bind;
         this.read_D = my - this.f_a_I;
         OverlayManager.init_bk().update_V(this);
         return true;
      }

      if (!this.init_Z_D_D(mx, my)) {
         return false;
      }

      if (this.load_Z) {
         return true;
      }

      OverlayManager.init_bk().update_V(this);
      MouseButtonEvent click = new MouseButtonEvent(imx, imy, Compat263.newGlfwMouseButtonInfo(button, 0));
      boolean bodyInputAllowed = this.init_NetHelper == null || this.init_NetHelper.init_Z(imx, imy);
      ThemedTextInputBox focused = null;
      if (bodyInputAllowed) {
         for (Entry<String, ThemedTextInputBox> entry : this.init_Map.entrySet()) {
            if (this.open_Z(entry.getKey())) {
               String baseKey = entry.getKey().replaceAll("_\\d+$", "");
               FieldDef fld = this.load_FieldDef_Str(baseKey);
               if ((fld == null || this.write(fld)) && entry.getValue().init(click, false)) {
                  focused = entry.getValue();
                  break;
               }
            }
         }
      }

      if (bodyInputAllowed && focused == null) {
         for (Entry<String, ThemedTextInputBox> entry : this.flush_Map.entrySet()) {
            FieldDef fld = this.load_FieldDef_Str(entry.getKey());
            if ((fld == null || this.write(fld)) && entry.getValue().init(click, false)) {
               focused = entry.getValue();
               break;
            }
         }
      }

      if (focused != null) {
         ThemedTextInputBox ff = focused;
         this.init_Map.values().forEach(f -> f.push(f == ff));
         this.flush_Map.values().forEach(f -> f.push(f == ff));
         return true;
      }

      this.push_V();
      if (button == 0 && this.load_Z_I_I(imx, imy)) {
         this.size_Z = true;
         this.pop(imx);
         return true;
      }

      if (button == 0 && this.read_Z_I_I(imx, imy)) {
         this.data_Z = true;
         this.run(imx);
         return true;
      }

      if (button == 0 && this.write_Z_I_I(imx, imy)) {
         this.value_Z = true;
         this.set(imx);
         return true;
      }

      if (bodyInputAllowed) {
         for (EventBus vp : this.tick_Map.values()) {
            if (vp.load(mx, my)) {
               vp.init_Z_D_2(mx, my, button);
               return true;
            }
         }

         for (EventBus vp : this.close_Map.values()) {
            if (vp.load(mx, my)) {
               vp.init_Z_D_2(mx, my, button);
               return true;
            }
         }
      }

      for (ActionEditorOverlay.SyncManager r : this.size_List) {
         if (r.init(imx, imy)) {
            this.init_IntConsumer = r.init_IntConsumer();
            this.init_IntConsumer.accept(imy);
            return true;
         }
      }

      for (ActionEditorOverlay.Inner_StreamUtil r : this.valid_List) {
         if (r.init(imx, imy) && r.init(mx, my, button)) {
            this.fill();
            return true;
         }
      }

      return true;
   }

   private boolean open_Z(String key) {
      if ("payload_content".equals(key)) {
         return this.f_i_I == 0;
      } else if ("payload_hex_view".equals(key)) {
         return this.f_i_I == 1;
      } else if ("payload_utf8_view".equals(key)) {
         return this.f_i_I == 2;
      } else {
         return "payload_logical_view".equals(key) ? this.f_i_I == 3 : true;
      }
   }

   @Override
   public boolean load_Z_D_3(double mx, double my, int button) {
      this.variableWarningStale = true;
      if (this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.load_Z_D_3(mx, my, button)) {
         return true;
      }

      if (this.init_RaceStepSelectorOv.sync_Z() && this.init_RaceStepSelectorOv.load_Z_D_3(mx, my, button)) {
         return true;
      }

      boolean consumed = false;

      for (EventBus vp : this.tick_Map.values()) {
         if (vp.init_Z()) {
            vp.read_V();
            consumed = true;
         }
      }

      for (EventBus vp : this.close_Map.values()) {
         if (vp.init_Z()) {
            vp.read_V();
            consumed = true;
         }
      }

      if (this.init_IntConsumer != null) {
         this.init_IntConsumer = null;
         consumed = true;
      }

      if (this.size_Z) {
         this.size_Z = false;
         consumed = true;
      }

      if (this.data_Z) {
         this.data_Z = false;
         consumed = true;
      }

      if (this.value_Z) {
         this.value_Z = false;
         consumed = true;
      }

      if (this.update_Z) {
         this.update_Z = false;
         consumed = true;
      }

      if (autismclient.gui.f.b.SyncManager.write(this.sync_List)) {
         return true;
      }

      for (Entry<String, ThemedTextInputBox> entry : this.init_Map.entrySet()) {
         if (this.open_Z(entry.getKey()) && entry.getValue().load(mx, my, button)) {
            consumed = true;
         }
      }

      for (ThemedTextInputBox field : this.flush_Map.values()) {
         if (field.load(mx, my, button)) {
            consumed = true;
         }
      }

      return consumed;
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      this.variableWarningStale = true;
      if (this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.init_Z_D(mx, my, button, dx, dy)) {
         return true;
      }

      if (this.init_RaceStepSelectorOv.sync_Z() && this.init_RaceStepSelectorOv.init_Z_D(mx, my, button, dx, dy)) {
         return true;
      }

      if (autismclient.gui.f.b.SyncManager.load(this.sync_List, mx, my, button)) {
         return true;
      }

      if (this.size_Z) {
         this.pop((int)mx);
         return true;
      }

      if (this.data_Z) {
         this.run((int)mx);
         return true;
      }

      if (this.value_Z) {
         this.set((int)mx);
         return true;
      }

      if (this.update_Z) {
         this.bind = (int)(mx - this.load_D);
         this.f_a_I = (int)(my - this.read_D);
         OverlayBounds c = this.init_cH_cX(this);
         this.bind = c.init_I;
         this.f_a_I = c.load_I;
         return true;
      }

      for (Entry<String, ThemedTextInputBox> entry : this.init_Map.entrySet()) {
         if (this.open_Z(entry.getKey()) && entry.getValue().init(mx, my, button, dx, dy)) {
            return true;
         }
      }

      for (ThemedTextInputBox field : this.flush_Map.values()) {
         if (field.init(mx, my, button, dx, dy)) {
            return true;
         }
      }

      for (EventBus vp : this.tick_Map.values()) {
         if (vp.init_Z()) {
            vp.init_V_D_D(mx, my);
            return true;
         }
      }

      for (EventBus vp : this.close_Map.values()) {
         if (vp.init_Z()) {
            vp.init_V_D_D(mx, my);
            return true;
         }
      }

      if (this.init_IntConsumer != null) {
         this.init_IntConsumer.accept((int)my);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      this.variableWarningStale = true;
      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_D_2(mx, my, amount);
      }

      if (this.init_RaceStepSelectorOv.sync_Z()) {
         return this.init_RaceStepSelectorOv.init_Z_D_2(mx, my, amount);
      }

      if (this.write_Z && this.init_Z_D_D(mx, my)) {
         if (!this.sync_List.isEmpty()
            && (autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List) || autismclient.gui.f.b.SyncManager.init(this.sync_List, mx, my))
            && autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List, mx, my, amount)) {
            return true;
         }

         boolean bodyScrollAllowed = this.init_NetHelper == null || this.init_NetHelper.init_Z((int)mx, (int)my);
         if (bodyScrollAllowed) {
            for (Entry<String, ThemedTextInputBox> entry : this.init_Map.entrySet()) {
               if (this.open_Z(entry.getKey()) && entry.getValue().init_Z_D(mx, my, amount)) {
                  return true;
               }
            }

            for (ThemedTextInputBox field : this.flush_Map.values()) {
               if (field.init_Z_D(mx, my, amount)) {
                  return true;
               }
            }

            for (Entry<String, EventBus> entry : this.tick_Map.entrySet()) {
               EventBus vp = entry.getValue();
               if (vp.init_Z_D_D(mx, my)) {
                  vp.init_Z_D(mx, my, amount);
                  return true;
               }
            }

            for (Entry<String, EventBus> entry : this.close_Map.entrySet()) {
               EventBus vp = entry.getValue();
               if (vp.init_Z_D_D(mx, my)) {
                  vp.init_Z_D(mx, my, amount);
                  return true;
               }
            }
         }

         if (this.init_IArr != null && my >= this.init_IArr[0] && my < this.init_IArr[0] + this.init_IArr[1]) {
            this.f_g_I = Math.max(0, this.f_g_I - (int)(amount * 13.0));
            return true;
         } else {
            this.f_e_I = Math.max(0, this.f_e_I - (int)(amount * 12.0));
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      this.variableWarningStale = true;
      if (!this.write_Z) {
         return false;
      }

      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_I(keyCode, scanCode, modifiers);
      }

      if (this.init_RaceStepSelectorOv.sync_Z()) {
         return this.init_RaceStepSelectorOv.init_Z_I(keyCode, scanCode, modifiers);
      }

      if (!autismclient.gui.f.b.SyncManager.init_Z_List(this.sync_List)) {
         if (keyCode == 256) {
            if (this.poll_Z_2()) {
               return true;
            } else if (this.write_Z()) {
               this.push_V();
               return true;
            } else {
               this.set(false);
               return true;
            }
         } else {
            KeyEvent ki = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);

            for (ThemedTextInputBox f : this.init_Map.values()) {
               if (f.init_Z()) {
                  return f.init_Z_KeyEvent(ki);
               }
            }

            for (ThemedTextInputBox f : this.flush_Map.values()) {
               if (f.init_Z()) {
                  return f.init_Z_KeyEvent(ki);
               }
            }

            return false;
         }
      } else {
         if (keyCode == 256 || keyCode == 257) {
            autismclient.gui.f.b.SyncManager.read(this.sync_List);
         }

         return true;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      this.variableWarningStale = true;
      if (!this.write_Z) {
         return false;
      }

      if (this.init_AutismPacketSelect.sync_Z()) {
         return this.init_AutismPacketSelect.init_Z_C_I(chr, modifiers);
      }

      if (this.init_RaceStepSelectorOv.sync_Z()) {
         return this.init_RaceStepSelectorOv.init_Z_C_I(chr, modifiers);
      }

      CharacterEvent ci = new CharacterEvent(chr);

      for (ThemedTextInputBox f : this.init_Map.values()) {
         if (f.init_Z() && f.init_Z_CharacterEvent(ci)) {
            return true;
         }
      }

      for (ThemedTextInputBox f : this.flush_Map.values()) {
         if (f.init_Z() && f.init_Z_CharacterEvent(ci)) {
            return true;
         }
      }

      return false;
   }

   private boolean write(FieldDef field) {
      if (field == null) {
         return false;
      }

      if (this.init_MacroAction instanceof ToolsAction) {
         String key = field.key();
         boolean run = "RUN".equals(this.tick_Str("operation"));
         if ((key.startsWith("toolArg.") || "position".equals(key) || "waypoint".equals(key)) && !run) {
            return false;
         }

         if ("position".equals(key)) {
            return "CUSTOM".equals(this.tick_Str("destination")) || "OFFSET".equals(this.tick_Str("destination"));
         }

         if ("waypoint".equals(key)) {
            return "WAYPOINT".equals(this.tick_Str("destination"));
         }
      }

      if ("safeClickDelayTicks".equals(field.key())) {
         return this.stop_Z();
      }

      if ("safeClickDelayAfterPickup".equals(field.key()) || "safeClickDelayBeforeReturn".equals(field.key())) {
         return this.stop_Z();
      }

      if (this.poll_Z(field.key()) && this.init_MacroAction instanceof GuiWaitAware) {
         return this.next_Z();
      }

      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY) {
         String mode = this.tick_Str("mode");
         if ("waitForGuiBefore".equals(field.key()) || "waitForGuiAfter".equals(field.key())) {
            return "OPEN".equals(mode);
         }

         if ("sendPacket".equals(field.key())) {
            return "CLOSE".equals(mode);
         }
      }

      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_ENTITY) {
         String checkMode = this.tick_Str("checkMode");
         if ("centerOnPlayer".equals(field.key()) || "radius".equals(field.key()) || "mustBeLookingAt".equals(field.key())) {
            return "RADIUS".equals(checkMode) || "NEARBY".equals(checkMode) && !"mustBeLookingAt".equals(field.key());
         }

         if ("pos".equals(field.key())) {
            return ("RADIUS".equals(checkMode) || "NEARBY".equals(checkMode)) && !this.load_Map.getOrDefault("centerOnPlayer", false);
         }
      }

      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY_AUDIT) {
         String mode = this.tick_Str("mode");
         String openMode = this.tick_Str("openMode");
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

      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PACKET_GATE && "DISABLE_GATE".equals(this.tick_Str("mode"))) {
         String key = field.key();
         return "mode".equals(key) || "gateId".equals(key) || "flushOnDisable".equals(key);
      }

      if (this.init_MacroAction != null
         && (this.init_MacroAction.getType() == MacroActionType.FPS || this.init_MacroAction.getType() == MacroActionType.SPING)) {
         String endMode = this.tick_Str("endMode");
         boolean timeout = endMode.isEmpty() || "TIMEOUT".equals(endMode);
         boolean steps = "STEPS".equals(endMode);
         boolean useTicks = this.load_Map.getOrDefault("useTicks", false);
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

      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CAPTURE_VALUE) {
         String key = field.key();
         if ("listFilterText".equals(key) || "listStripPrefix".equals(key) || "listPickPosition".equals(key)) {
            String source = this.tick_Str("source");
            if (!"COMMAND_AUTOFILL".equals(source) && !"TABLIST".equals(source)) {
               return false;
            }

            String filter = this.tick_Str("listFilter");
            if ("listFilterText".equals(key)) {
               return !"NONE".equals(filter);
            }

            if (!"listStripPrefix".equals(key)) {
               return "POSITION".equals(this.tick_Str("listSelection"));
            }

            return "PREFIX".equals(filter) || "SUFFIX".equals(filter);
         }
      }

      if (!field.hasShowWhen()) {
         return true;
      }

      boolean cond;
      if (field.showWhenValue() != null && !field.showWhenValue().isEmpty()) {
         String currentValue = this.tick_Str(field.showWhenKey());
         cond = false;

         for (String allowed : field.showWhenValue().split("\\|")) {
            if (allowed.equals(currentValue)) {
               cond = true;
               break;
            }
         }
      } else {
         cond = this.load_Map.getOrDefault(field.showWhenKey(), false);
      }

      boolean visible = field.showWhenInverted() ? !cond : cond;
      if (visible && !field.showWhenInverted()) {
         FieldDef dep = this.load_FieldDef_Str(field.showWhenKey());
         if (dep != null && !this.write(dep)) {
            visible = false;
         }
      }

      return visible;
   }

   private boolean stop_Z() {
      return this.init_MacroAction != null
         && this.init_MacroAction.getType() == MacroActionType.XCARRY
         && "PUT_IN".equals(this.tick_Str("mode"))
         && "SAFE_CLICK".equals(this.tick_Str("transferMode"));
   }

   private boolean poll_Z(String key) {
      return "guiName".equals(key) || "waitGuiName".equals(key);
   }

   private boolean next_Z() {
      return this.load_Map.getOrDefault("waitForGuiBefore", false)
         || this.load_Map.getOrDefault("waitForGuiAfter", false)
         || this.load_Map.getOrDefault("waitForGui", false);
   }

   private String tick_Str(String key) {
      if (this.init_ActionFieldSchema == null) {
         return "";
      }

      for (FieldDef field : this.init_ActionFieldSchema.fields()) {
         if (field.key().equals(key) && field.type() == CachePool.ENUM) {
            List<String> opts = field.enumOptions();
            if (opts.isEmpty()) {
               return "";
            }

            int idx = Math.min(this.read_Map.getOrDefault(key, 0), opts.size() - 1);
            return opts.get(idx);
         }
      }

      return "";
   }

   private void read(MacroAction action) {
      if (action != null && this.init_CompoundTag != null) {
         if (action instanceof SelectSlotAction selectSlotAction) {
            this.init_V_Str("itemName", selectSlotAction.itemTarget, selectSlotAction.itemName, null);
         }

         if (action instanceof UseItemAction useItemAction) {
            this.init_V_Str("itemName", useItemAction.itemTarget, useItemAction.itemName, "slot");
            if (useItemAction.slot >= 0) {
               this.init_CompoundTag.putInt("slot", useItemAction.slot);
            }
         }

         if (action instanceof WaitForCooldownAction waitForCooldownAction) {
            this.init_V_Str("itemName", waitForCooldownAction.itemTarget, waitForCooldownAction.itemName, null);
         }

         if (action instanceof autismclient.util.macro.CloseGuiAction closeGuiAction) {
            this.init_V_Str("itemName", closeGuiAction.itemTarget, closeGuiAction.itemName, "targetSlot");
         }

         if (action instanceof SwapSlotsAction swapSlotsAction) {
            this.init_V_Str("fromItemName", swapSlotsAction.fromItemTarget, swapSlotsAction.fromItemName, null);
            this.init_V_Str("toItemName", swapSlotsAction.toItemTarget, swapSlotsAction.toItemName, null);
         }

         if (action instanceof StoreItemAction storeItemAction) {
            this.init_V_Str_3("targetItems", storeItemAction.itemTargets, storeItemAction.targetItems);
         }

         if (action instanceof XCarryAction xCarryAction) {
            this.init_V_Str_3("entries", xCarryAction.entryTargets, xCarryAction.entries);
            this.init_CompoundTag.putInt("safeClickDelayTicks", XCarryAction.write(xCarryAction.safeClickDelayTicks));
            this.init_CompoundTag.putBoolean("safeClickDelayAfterPickup", xCarryAction.safeClickDelayAfterPickup);
            this.init_CompoundTag.putBoolean("safeClickDelayBeforeReturn", xCarryAction.safeClickDelayBeforeReturn);
            ListTag destinations = new ListTag();
            xCarryAction.init_V_I_2(xCarryAction.entries.size());
            xCarryAction.load_V_I(xCarryAction.entries.size());
            ListTag amountModes = new ListTag();
            ListTag amounts = new ListTag();

            for (int i = 0; i < xCarryAction.entries.size(); i++) {
               destinations.add(StringTag.valueOf(String.valueOf(xCarryAction.load_I_I(i))));
               amountModes.add(StringTag.valueOf(xCarryAction.init_aQ_DataUtil_I(i).name()));
               amounts.add(StringTag.valueOf(String.valueOf(xCarryAction.read_I_I(i))));
            }

            this.init_CompoundTag.put("entryDestinations", destinations);
            this.init_CompoundTag.put("entryAmountModes", amountModes);
            this.init_CompoundTag.put("entryAmounts", amounts);
         }

         if (action instanceof InventoryAuditAction inventoryAuditAction) {
            this.init_V_Str_3("targetItems", inventoryAuditAction.itemTargets, inventoryAuditAction.targetItems);
         }

         if (action instanceof PlaceAction placeAction) {
            this.init_V_Str("itemName", placeAction.itemTarget, placeAction.itemName, null);
            this.chunk();
         }
      }
   }

   private void chunk() {
      if (this.init_CompoundTag != null) {
         this.init_CompoundTag.remove("slot");
         if (!this.init_CompoundTag.getBooleanOr("manualDirection", false)) {
            this.init_CompoundTag.remove("direction");
         }
      }
   }

   private MacroItemTarget init_ArrayUtil() {
      ThemedTextInputBox nameF = this.init_Map.get("itemName");
      ThemedTextInputBox slotF = this.init_Map.get("place_itemSlot");
      return this.init_ArrayUtil_Str_2(nameF == null ? "" : nameF.load_Str(), slotF == null ? "" : slotF.load_Str(), this.update_Map.get("itemName"));
   }

   private void table() {
      if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
         MacroItemTarget target = this.init_ArrayUtil();
         if (!target.init_Z() && !target.update()) {
            this.update_Map.remove("itemName");
         } else {
            this.update_Map.put("itemName", target.init_ArrayUtil());
         }
      }
   }

   private void init_V_Str(String key, MacroItemTarget source, String legacyValue, String slotKey) {
      MacroItemTarget resolved = this.init_ArrayUtil_ArrayUtil_Str(source, legacyValue);
      if (resolved.init_Z() || resolved.update()) {
         this.update_Map.put(key, resolved.init_ArrayUtil());
         this.init_CompoundTag.putString(key, resolved.init_Str());
         if (slotKey != null && resolved.init_Z()) {
            this.init_CompoundTag.putInt(slotKey, resolved.init_I);
         }
      }
   }

   private void store() {
      this.poll_V("itemName");
      this.poll_V("fromItemName");
      this.poll_V("toItemName");
      this.push_V("_item_add");
      this.push_V("_drop_add");
      this.push_V("_wsc_add");
      this.push_V("targetItems");
   }

   private void poll_V(String key) {
      ThemedTextInputBox field = this.init_Map.get(key);
      if (field != null) {
         field.init_V_Function(value -> this.init_Component_Str_Str(key, value));
      }
   }

   private void push_V(String key) {
      ThemedTextInputBox field = this.flush_Map.get(key);
      if (field != null) {
         field.init_V_Function(value -> this.load_Component_Str_Str(key, value));
      }
   }

   private Component init_Component_Str_Str(String key, String value) {
      return this.init_Component_ArrayUtil_Str(this.update_Map.get(key), value);
   }

   private Component load_Component_Str_Str(String key, String value) {
      Component var10000;
      switch (key) {
         case "_item_add":
            var10000 = this.init_Component_ArrayUtil_Str(
               this.init_MapStore != null && this.f_j_I >= 0 && this.f_j_I < this.init_MapStore.itemNames.size()
                  ? this.init_ArrayUtil_List(this.init_MapStore.itemTargets, this.init_MapStore.itemNames, this.f_j_I)
                  : null,
               value
            );
            break;
         case "_drop_add":
            var10000 = this.init_Component_ArrayUtil_Str(
               this.init_PacketUtil != null && this.f_k_I >= 0 && this.f_k_I < this.init_PacketUtil.itemNames.size()
                  ? this.init_ArrayUtil_List(this.init_PacketUtil.itemTargets, this.init_PacketUtil.itemNames, this.f_k_I)
                  : null,
               value
            );
            break;
         case "_wsc_add":
            var10000 = this.init_Component_ArrayUtil_Str(
               this.run_List != null && this.f_l_I >= 0 && this.f_l_I < this.run_List.size() ? this.run_List.get(this.f_l_I).init_ArrayUtil() : null, value
            );
            break;
         case "targetItems":
            var10000 = this.init_Component_ArrayUtil_Str(this.load_ArrayUtil(), value);
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   private MacroItemTarget load_ArrayUtil() {
      int storeIndex = this.open_Map.getOrDefault("store_items_selected", -1);
      int auditIndex = this.open_Map.getOrDefault("audit_items_selected", -1);
      int index = storeIndex >= 0 ? storeIndex : auditIndex;
      if (index < 0) {
         return null;
      }

      List<MacroItemTarget> targets = this.init_List_Str("targetItems");
      if (index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.init_Z() || target.update())) {
            return target;
         }
      }

      return null;
   }

   private Component init_Component_ArrayUtil_Str(MacroItemTarget target, String value) {
      if (target == null) {
         return null;
      }

      Component rich = target.init_Component_Str(value);
      if (rich == null) {
         return null;
      }

      String safeValue = value == null ? "" : value;
      return safeValue.equals(rich.getString()) ? rich.copy() : null;
   }

   private void init_V_Str_3(String key, List<MacroItemTarget> source, List<String> legacyValues) {
      List<MacroItemTarget> resolved = this.init_List_List_List(source, legacyValues);
      if (!resolved.isEmpty()) {
         this.sync_Map.put(key, MacroItemTarget.init_List_List(resolved));
         ListTag listTag = new ListTag();

         for (MacroItemTarget target : resolved) {
            if (target != null) {
               String entry = target.write_Str();
               if (!entry.isBlank()) {
                  listTag.add(StringTag.valueOf(entry));
               }
            }
         }

         this.init_CompoundTag.put(key, listTag);
      }
   }

   private MacroItemTarget init_ArrayUtil_ArrayUtil_Str(MacroItemTarget source, String legacyValue) {
      return source != null && (source.init_Z() || source.update()) ? source.init_ArrayUtil() : MacroItemTarget.update(legacyValue);
   }

   private List<MacroItemTarget> init_List_List_List(List<MacroItemTarget> source, List<String> legacyValues) {
      List<MacroItemTarget> resolved = MacroItemTarget.init_List_List(source);
      if (!resolved.isEmpty()) {
         return resolved;
      }

      List<MacroItemTarget> parsed = new ArrayList<>();
      if (legacyValues == null) {
         return parsed;
      }

      for (String legacyValue : legacyValues) {
         MacroItemTarget target = MacroItemTarget.update(legacyValue);
         if (target.init_Z() || target.update()) {
            parsed.add(target);
         }
      }

      return parsed;
   }

   private MacroItemTarget init_ArrayUtil_Str_Str(String key, String slotKey) {
      ThemedTextInputBox valueField = this.init_Map.get(key);
      String text = valueField == null ? "" : valueField.load_Str().strip();
      MacroItemTarget previous = this.update_Map.get(key);
      int slot = -1;
      boolean slotDriven = false;
      if (slotKey != null) {
         ThemedTextInputBox slotField = this.init_Map.get(slotKey);
         slot = slotField == null ? -1 : this.load_I_Str_I(slotField.load_Str(), -1);
         slotDriven = true;
      } else if (previous != null && previous.init_Z()) {
         slot = previous.init_I;
      }

      if (!text.isEmpty() || slotDriven && slot >= 0) {
         if (previous != null && text.equals(previous.init_Str())) {
            MacroItemTarget preserved = previous.init_ArrayUtil();
            if (slotDriven) {
               preserved.init_I = slot;
            }

            return preserved;
         } else if (previous != null && previous.write_Z() && !text.isEmpty()) {
            MacroItemTarget preserved = previous.load_ArrayUtil_Str(text);
            if (slot >= 0) {
               preserved.init_I = slot;
            } else if (slotDriven) {
               preserved.init_I = -1;
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

            return MacroItemTarget.update(raw);
         }
      } else {
         return new MacroItemTarget();
      }
   }

   private List<MacroItemTarget> init_List_Str(String key) {
      List<String> values = this.write_Map.getOrDefault(key, Collections.emptyList());
      List<MacroItemTarget> previous = this.sync_Map.getOrDefault(key, Collections.emptyList());
      boolean[] used = new boolean[previous.size()];
      List<MacroItemTarget> rebuilt = new ArrayList<>();

      for (String value : values) {
         String entry = value == null ? "" : value.strip();
         if (!entry.isEmpty()) {
            MacroItemTarget preserved = null;

            for (int i = 0; i < previous.size(); i++) {
               MacroItemTarget candidate = previous.get(i);
               if (!used[i] && candidate != null && entry.equals(candidate.write_Str())) {
                  preserved = candidate.init_ArrayUtil();
                  used[i] = true;
                  break;
               }
            }

            if (preserved == null) {
               preserved = MacroItemTarget.update(entry);
            }

            if (this.put_Z(key) && preserved.update()) {
               preserved.init_I = -1;
            }

            if (preserved.init_Z() || preserved.update()) {
               rebuilt.add(preserved);
            }
         }
      }

      this.sync_Map.put(key, MacroItemTarget.init_List_List(rebuilt));
      return rebuilt;
   }

   private void drain() {
      if (this.init_MacroAction != null && this.init_CompoundTag != null) {
         if (this.init_MacroAction instanceof SelectSlotAction) {
            this.init_V_Str_ArrayUtil("itemName", this.init_ArrayUtil_Str_Str("itemName", null));
         }

         if (this.init_MacroAction instanceof UseItemAction) {
            this.init_V_Str_ArrayUtil("itemName", this.init_ArrayUtil_Str_Str("itemName", "slot"));
         }

         if (this.init_MacroAction instanceof WaitForCooldownAction) {
            this.init_V_Str_ArrayUtil("itemName", this.init_ArrayUtil_Str_Str("itemName", null));
         }

         if (this.init_MacroAction instanceof autismclient.util.macro.CloseGuiAction) {
            this.init_V_Str_ArrayUtil("itemName", this.init_ArrayUtil_Str_Str("itemName", "targetSlot"));
         }

         if (this.init_MacroAction instanceof SwapSlotsAction) {
            this.init_V_Str_ArrayUtil("fromItemName", this.init_ArrayUtil_Str_Str("fromItemName", null));
            this.init_V_Str_ArrayUtil("toItemName", this.init_ArrayUtil_Str_Str("toItemName", null));
         }

         if (this.init_MacroAction instanceof PlaceAction) {
            this.init_V_Str_ArrayUtil("itemName", this.init_ArrayUtil());
            this.init_CompoundTag.remove("slot");
         }

         if (this.init_MacroAction instanceof StoreItemAction) {
            this.init_V_Str_List("targetItems", this.init_List_Str("targetItems"));
         }

         if (this.init_MacroAction instanceof XCarryAction) {
            this.init_V_Str_List("entries", this.init_List_Str("entries"));
            int count = this.write_Map.getOrDefault("entries", Collections.emptyList()).size();
            this.sync(count);
            this.flush(count);
         }

         if (this.init_MacroAction instanceof InventoryAuditAction) {
            this.init_V_Str_List("targetItems", this.init_List_Str("targetItems"));
         }
      }
   }

   private void init_V_Str_ArrayUtil(String key, MacroItemTarget target) {
      if (target == null || !target.init_Z() && !target.update()) {
         this.init_CompoundTag.remove(key);
         this.update_Map.remove(key);
      } else {
         this.init_CompoundTag.put(key, target.init_CompoundTag());
         this.update_Map.put(key, target.init_ArrayUtil());
      }
   }

   private void init_V_Str_List(String key, List<MacroItemTarget> targets) {
      this.init_CompoundTag.put(key, MacroItemTarget.init_ListTag_List(targets));
   }

   private void fill() {
      this.valid_List.clear();
      this.size_List.clear();
      this.pool();
      int neededH = 18 + this.stop_I() + this.run_I() + 4;
      int minH = this.push_I_2();
      int maxH = Math.max(minH, cA.load_I() * 4 / 5);
      this.f_c_I = Math.max(minH, Math.min(maxH, neededH));
      OverlayBounds clamped = this.init_cH_cX_cH(this, new OverlayBounds(this.bind, this.f_a_I, this.f_b_I, this.f_c_I, this.write_Z, this.load_Z));
      this.bind = clamped.init_I;
      this.f_a_I = clamped.load_I;
      this.f_b_I = clamped.read;
      this.f_c_I = clamped.write;
   }

   private void update_V(int index) {
      if (this.f_l_I == index) {
         this.f_l_I = -1;
         this.mark();
      } else {
         this.f_l_I = index;
         if (this.run_List != null && index < this.run_List.size()) {
            this.init_V_aD_DataUtil(this.run_List.get(index));
         }
      }
   }

   private void init_V_aD_DataUtil(WaitForSlotChangeAction.Inner_DataUtil e) {
      ThemedTextInputBox addF = this.flush_Map.get("_wsc_add");
      ThemedTextInputBox slotF = this.init_Map.get("wsc_slot");
      ThemedTextInputBox countF = this.init_Map.get("wsc_count");
      if (addF != null && slotF != null) {
         this.prev_Z = true;
         MacroItemTarget target = e.init_ArrayUtil();
         addF.init_V_Str(target.init_Str());
         slotF.init_V_Str(target.init_Z() ? String.valueOf(target.init_I) : "");
         if (countF != null) {
            countF.init_V_Str(String.valueOf(Math.max(1, e.init_I)));
         }

         this.prev_Z = false;
      }
   }

   private void mark() {
      ThemedTextInputBox addF = this.flush_Map.get("_wsc_add");
      ThemedTextInputBox slotF = this.init_Map.get("wsc_slot");
      ThemedTextInputBox countF = this.init_Map.get("wsc_count");
      this.prev_Z = true;
      if (addF != null) {
         addF.init_V_Str("");
      }

      if (slotF != null) {
         slotF.init_V_Str("");
      }

      if (countF != null) {
         countF.init_V_Str("");
      }

      this.prev_Z = false;
   }

   private void scan() {
      if (!this.prev_Z && this.run_List != null && this.f_l_I >= 0 && this.f_l_I < this.run_List.size()) {
         ThemedTextInputBox addF = this.flush_Map.get("_wsc_add");
         ThemedTextInputBox slotF = this.init_Map.get("wsc_slot");
         if (addF != null && slotF != null) {
            String nameText = addF.load_Str().strip();
            String slotText = slotF.load_Str().strip();
            WaitForSlotChangeAction.Inner_DataUtil entry = this.run_List.get(this.f_l_I);
            MacroItemTarget target = this.init_ArrayUtil_Str_2(nameText, slotText, entry.init_ArrayUtil());
            String rawTarget = target.write_Str();
            String norm = rawTarget.isEmpty() ? "" : StoreItemAction.init_Str_Str_8(rawTarget);
            if (norm == null) {
               norm = "";
            }

            if (!this.init_Z_Str_I(norm, this.f_l_I)) {
               entry.init_Str = norm;
               entry.init_ArrayUtil = target;
            }
         }
      }
   }

   private void parse() {
      if (!this.prev_Z && this.run_List != null) {
         ThemedTextInputBox countF = this.init_Map.get("wsc_count");
         if (countF != null) {
            int count;
            try {
               count = Integer.parseInt(countF.load_Str().strip());
            } catch (NumberFormatException ignored) {
               return;
            }

            count = Math.max(1, count);
            if (this.f_l_I >= 0 && this.f_l_I < this.run_List.size()) {
               this.run_List.get(this.f_l_I).init_I = count;
            } else {
               this.f_m_I = count;
            }
         }
      }
   }

   private void init_V_k_k(ThemedTextInputBox addF, ThemedTextInputBox slotF) {
      if (this.run_List != null) {
         String nameText = addF.load_Str().strip();
         String slotText = slotF.load_Str().strip();
         MacroItemTarget target = this.init_ArrayUtil_Str_2(nameText, slotText, null);
         String rawTarget = target.write_Str();
         String norm = rawTarget.isEmpty() ? "" : StoreItemAction.init_Str_Str_8(rawTarget);
         if (norm == null) {
            norm = "";
         }

         if (!this.push_Z(norm)) {
            WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil(norm, this.init_aD_StreamUtil, this.f_m_I);
            entry.init_ArrayUtil = target;
            entry.init_Str = norm;
            this.run_List.add(entry);
            addF.init_V_Str("");
            slotF.init_V_Str("");
         }
      }
   }

   private boolean push_Z(String normTarget) {
      if (this.run_List == null) {
         return false;
      }

      for (WaitForSlotChangeAction.Inner_DataUtil e : this.run_List) {
         if (e.init_Str.equals(normTarget)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_Str_I(String normTarget, int ignoreIdx) {
      if (this.run_List == null) {
         return false;
      }

      for (int i = 0; i < this.run_List.size(); i++) {
         if (i != ignoreIdx && this.run_List.get(i).init_Str.equals(normTarget)) {
            return true;
         }
      }

      return false;
   }

   private boolean pop_Z(String key) {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SLOT_CHANGE && "targetEntries".equals(key);
   }

   private boolean tick_Z(String key) {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.STORE_ITEM && "targetItems".equals(key);
   }

   private boolean run_Z(String key) {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY_AUDIT && "targetItems".equals(key);
   }

   private boolean get_Z(String key) {
      return this.tick_Z(key) || this.run_Z(key);
   }

   private String load_Str_Str_Str(String nameText, String slotText) {
      String trimmedName = nameText == null ? "" : nameText.trim();
      int slot = this.load_I_Str_I(slotText, -1);
      if (!trimmedName.isEmpty() && slot >= 0) {
         return "#" + slot + "|" + trimmedName;
      } else if (!trimmedName.isEmpty()) {
         return trimmedName;
      } else {
         return slot >= 0 ? "#" + slot : null;
      }
   }

   private MacroItemTarget init_ArrayUtil_Slot(Slot slot, String itemName, String registryId, int visibleSlot) {
      if (slot != null && slot.getItem() != null && !slot.getItem().isEmpty()) {
         return MacroItemTarget.init_ArrayUtil_ItemStack_I(slot.getItem(), visibleSlot);
      }

      if (visibleSlot >= 0) {
         return MacroItemTarget.init_ArrayUtil_I(visibleSlot);
      }

      String fallback = registryId != null && !registryId.isBlank() ? registryId.trim() : (itemName == null ? "" : itemName.trim());
      return MacroItemTarget.update(fallback);
   }

   private static MacroItemTarget init_ArrayUtil_ArrayUtil(MacroItemTarget target) {
      if (target == null) {
         return new MacroItemTarget();
      }

      if (!target.init_Z()) {
         return target;
      }

      MacroItemTarget copy = target.init_ArrayUtil();
      copy.init_I = -1;
      return copy;
   }

   private static MacroItemTarget load_ArrayUtil_ArrayUtil(MacroItemTarget target) {
      if (target == null) {
         return new MacroItemTarget();
      } else {
         return target.update() ? init_ArrayUtil_ArrayUtil(target) : target.init_ArrayUtil();
      }
   }

   private MacroItemTarget init_ArrayUtil_Str_2(String nameText, String slotText, MacroItemTarget previous) {
      String trimmedName = nameText == null ? "" : nameText.strip();
      int slot = this.load_I_Str_I(slotText, -1);
      if (trimmedName.isEmpty() && slot < 0) {
         return new MacroItemTarget();
      }

      if (previous != null && trimmedName.equals(previous.init_Str())) {
         MacroItemTarget kept = previous.init_ArrayUtil();
         kept.init_I = slot;
         return kept;
      }

      if (previous != null && previous.write_Z() && !trimmedName.isEmpty()) {
         MacroItemTarget preserved = previous.load_ArrayUtil_Str(trimmedName);
         preserved.init_I = slot;
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

      return MacroItemTarget.update(raw);
   }

   private MacroItemTarget init_ArrayUtil_List(List<MacroItemTarget> targets, List<String> legacyEntries, int index) {
      if (targets != null && index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.init_Z() || target.update())) {
            return target;
         }
      }

      return legacyEntries != null && index >= 0 && index < legacyEntries.size() ? MacroItemTarget.update(legacyEntries.get(index)) : new MacroItemTarget();
   }

   private void init_V_Str_2(String key, List<String> entries, MacroItemTarget capturedTarget) {
      if (key != null && entries != null && capturedTarget != null) {
         String normalized = capturedTarget.write_Str();
         if (this.get_Z(key) || this.pop_Z(key)) {
            normalized = StoreItemAction.init_Str_Str_8(normalized);
         } else if (this.put_Z(key)) {
            normalized = this.run_Str(normalized);
         }

         if (normalized != null && !normalized.isBlank()) {
            int index = entries.indexOf(normalized);
            if (index >= 0) {
               List<MacroItemTarget> targets = this.init_List_Str(key);

               while (targets.size() < entries.size()) {
                  targets.add(new MacroItemTarget());
               }

               MacroItemTarget stored = this.put_Z(key) ? init_ArrayUtil_ArrayUtil(capturedTarget) : capturedTarget.init_ArrayUtil();
               if (!normalized.equals(stored.write_Str())) {
                  MacroItemTarget normalizedTarget = MacroItemTarget.update(normalized);
                  stored.init_I = normalizedTarget.init_I;
                  if (!normalizedTarget.init_Z() && !stored.update()) {
                     stored = normalizedTarget;
                  }
               }

               targets.set(index, stored);
               this.sync_Map.put(key, MacroItemTarget.init_List_List(targets));
            }
         }
      }
   }

   private void init_V_List(List<MacroItemTarget> targets, List<String> legacyEntries, int index, MacroItemTarget target) {
      if (targets != null && legacyEntries != null && index >= 0) {
         while (targets.size() <= index) {
            targets.add(new MacroItemTarget());
         }

         while (legacyEntries.size() <= index) {
            legacyEntries.add("");
         }

         MacroItemTarget stored = target == null ? new MacroItemTarget() : target;
         targets.set(index, stored);
         legacyEntries.set(index, stored.write_Str());
      }
   }

   private void init_V_List(List<MacroItemTarget> targets, List<String> legacyEntries, MacroItemTarget target) {
      if (targets != null && legacyEntries != null && target != null) {
         targets.add(target);
         legacyEntries.add(target.write_Str());
      }
   }

   private void init_V_List_I(List<MacroItemTarget> targets, int size) {
      if (targets != null) {
         while (targets.size() > size) {
            targets.remove(targets.size() - 1);
         }

         while (targets.size() < size) {
            targets.add(new MacroItemTarget());
         }
      }
   }

   private boolean set_Z(String key) {
      return this.put_Z(key) || this.tick_Z(key) || this.run_Z(key) || this.pop_Z(key);
   }

   private boolean put_Z(String key) {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.XCARRY && "entries".equals(key);
   }

   private boolean add_Z(String key) {
      return this.init_MacroAction != null
         && (this.init_MacroAction.getType() == MacroActionType.OPEN_CONTAINER || this.init_MacroAction.getType() == MacroActionType.INTERACT_ENTITY)
         && "entityTargets".equals(key);
   }

   private boolean clear_Z_Str(String key) {
      return false;
   }

   private ActionEditorOverlay.Inner_DataUtil init_ActionEditorOverla_Slot(
      Slot slot, String itemName, String registryId, int visibleSlot, boolean slotMode, List<String> entries
   ) {
      if (slot != null && visibleSlot >= 0) {
         MacroItemTarget captured = slotMode
            ? MacroItemTarget.init_ArrayUtil_I(visibleSlot)
            : load_ArrayUtil_ArrayUtil(this.init_ArrayUtil_Slot(slot, itemName, registryId, visibleSlot));
         if (!captured.update()) {
            String mode = this.tick_Str("mode");
            boolean playerInventorySlot = autismclient.util.aw.init_Z(init_Minecraft, slot);
            if ("LOOT".equals(mode) && playerInventorySlot) {
               return new ActionEditorOverlay.Inner_DataUtil(false, "Loot slot numbers must be chest/GUI slots - pick an item to add it by name", -38037);
            }

            if ("STORE".equals(mode) && !playerInventorySlot) {
               return new ActionEditorOverlay.Inner_DataUtil(false, "Store slot numbers must be inventory slots - pick an item to add it by name", -38037);
            }
         }

         return this.init_ActionEditorOverla_FieldDef(this.read_FieldDef("targetItems"), "targetItems", entries, captured.write_Str());
      } else {
         return new ActionEditorOverlay.Inner_DataUtil(false, "Could not read that slot", -38037);
      }
   }

   private String run_Str(String raw) {
      if (raw == null) {
         return null;
      }

      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      MacroItemTarget parsed = MacroItemTarget.update(trimmed);
      if (!parsed.update()) {
         if (!trimmed.startsWith("#") && trimmed.matches("\\d+")) {
            trimmed = "#" + trimmed;
         }

         return XCarryAction.init_Str_Str_7(trimmed);
      } else {
         parsed.init_I = -1;
         String entry = parsed.write_Str();
         return entry != null && !entry.isBlank() ? entry : null;
      }
   }

   private boolean init_Z_FieldDef(FieldDef field, List<String> entries, String rawEntry) {
      if (entries != null && rawEntry != null) {
         String entry = rawEntry.strip();
         if (entry.isEmpty()) {
            return false;
         }

         if (field != null && this.put_Z(field.key())) {
            entry = this.run_Str(entry);
            if (entry == null) {
               return false;
            } else {
               int limit = this.get_I();
               if (entries.size() >= limit) {
                  AutismClientMessaging.sendPrefixed(this.read_Str(limit));
                  return false;
               } else {
                  entries.add(entry);
                  this.sync(entries.size());
                  this.flush(entries.size());
                  return true;
               }
            }
         } else if (field != null && this.add_Z(field.key())) {
            if (entry.isBlank()) {
               return false;
            }

            if (!entries.contains(entry)) {
               entries.add(entry);
            }

            return true;
         } else if (field == null || !this.get_Z(field.key()) && !this.pop_Z(field.key())) {
            if (field == null || !"players".equals(field.key())) {
               entries.add(entry);
               return true;
            }

            if (this.init_Z_List_Str(entries, entry)) {
               return false;
            }

            entries.add(entry);
            return true;
         } else {
            entry = StoreItemAction.init_Str_Str_8(entry);
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

   private ActionEditorOverlay.Inner_DataUtil init_ActionEditorOverla_FieldDef(FieldDef field, String key, List<String> entries, String rawEntry) {
      if (entries == null) {
         return null;
      }

      String entry = rawEntry == null ? "" : rawEntry.strip();
      if (entry.isEmpty()) {
         return new ActionEditorOverlay.Inner_DataUtil(false, "Nothing to add from that slot", -38037);
      }

      if (field != null && this.put_Z(field.key())) {
         entry = this.run_Str(entry);
         if (entry != null && !entry.isBlank()) {
            String formatted = this.read_Str_Str_Str(key, entry);
            int limit = this.get_I();
            if (entries.size() >= limit) {
               return new ActionEditorOverlay.Inner_DataUtil(false, this.read_Str(limit), -38037);
            }

            entries.add(entry);
            this.sync(entries.size());
            this.flush(entries.size());
            return new ActionEditorOverlay.Inner_DataUtil(true, "Added " + formatted, -10035062);
         } else {
            return new ActionEditorOverlay.Inner_DataUtil(false, "Could not read that XCarry entry", -38037);
         }
      } else {
         if (field != null && (this.get_Z(field.key()) || this.pop_Z(field.key()))) {
            entry = StoreItemAction.init_Str_Str_8(entry);
            if (entry == null || entry.isBlank()) {
               return new ActionEditorOverlay.Inner_DataUtil(false, "Could not read that slot target", -38037);
            }
         }

         String formatted = this.read_Str_Str_Str(key, entry);
         if (entries.contains(entry)) {
            return new ActionEditorOverlay.Inner_DataUtil(false, "Already added " + formatted, -38037);
         }

         entries.add(entry);
         return new ActionEditorOverlay.Inner_DataUtil(true, "Added " + formatted, -10035062);
      }
   }

   private String read_Str_Str_Str(String key, String entry) {
      if (this.put_Z(key)) {
         String formatted = XCarryAction.load_Str_Str_2(entry);
         if (!formatted.isEmpty()) {
            return formatted;
         }
      }

      if (this.get_Z(key) || this.pop_Z(key)) {
         return StoreItemAction.load_Str_Str_3(entry);
      } else if (this.add_Z(key)) {
         return this.reset_Str(entry);
      } else {
         return entry == null ? "" : entry;
      }
   }

   private int get_I() {
      return 11;
   }

   private String read_Str(int limit) {
      return "XCarry limit reached: " + limit + " entries max (5 craft + 4 armor + offhand + cursor)";
   }

   private void sync(int count) {
      if (this.init_CompoundTag != null) {
         ListTag list = this.init_CompoundTag.getList("entryDestinations").orElse(new ListTag());

         while (list.size() < count) {
            list.add(StringTag.valueOf(String.valueOf(-2147483648)));
         }

         while (list.size() > count) {
            list.remove(list.size() - 1);
         }

         this.init_CompoundTag.put("entryDestinations", list);
      }
   }

   private void flush(int count) {
      if (this.init_CompoundTag != null) {
         ListTag modes = this.init_CompoundTag.getList("entryAmountModes").orElse(new ListTag());

         while (modes.size() < count) {
            modes.add(StringTag.valueOf(XCarryAction.Inner_DataUtil.FULL_STK.name()));
         }

         while (modes.size() > count) {
            modes.remove(modes.size() - 1);
         }

         this.init_CompoundTag.put("entryAmountModes", modes);
         ListTag amounts = this.init_CompoundTag.getList("entryAmounts").orElse(new ListTag());

         while (amounts.size() < count) {
            amounts.add(StringTag.valueOf("1"));
         }

         while (amounts.size() > count) {
            amounts.remove(amounts.size() - 1);
         }

         this.init_CompoundTag.put("entryAmounts", amounts);
      }
   }

   private int read_I(int index) {
      if (this.init_CompoundTag != null && index >= 0) {
         ListTag list = this.init_CompoundTag.getList("entryDestinations").orElse(new ListTag());
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

   private void sync_V(int index, int destination) {
      if (this.init_CompoundTag != null && index >= 0) {
         this.sync(Math.max(index + 1, this.write_Map.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.init_CompoundTag.getList("entryDestinations").orElse(new ListTag());
         if (index < list.size()) {
            list.set(index, StringTag.valueOf(String.valueOf(destination)));
            this.init_CompoundTag.put("entryDestinations", list);
         }
      }
   }

   private void close(int index) {
      if (this.init_CompoundTag != null && index >= 0) {
         ListTag list = this.init_CompoundTag.getList("entryDestinations").orElse(new ListTag());
         if (index >= 0 && index < list.size()) {
            list.remove(index);
         }

         this.init_CompoundTag.put("entryDestinations", list);
      }
   }

   private XCarryAction.Inner_DataUtil init_aQ_DataUtil_I(int index) {
      if (this.init_CompoundTag != null && index >= 0) {
         ListTag list = this.init_CompoundTag.getList("entryAmountModes").orElse(new ListTag());
         if (index >= list.size()) {
            return XCarryAction.Inner_DataUtil.FULL_STK;
         }

         try {
            return XCarryAction.Inner_DataUtil.valueOf(list.get(index).asString().orElse(XCarryAction.Inner_DataUtil.FULL_STK.name()));
         } catch (IllegalArgumentException ignored) {
            return XCarryAction.Inner_DataUtil.FULL_STK;
         }
      } else {
         return XCarryAction.Inner_DataUtil.FULL_STK;
      }
   }

   private void init_V_I_aQ_DataUtil(int index, XCarryAction.Inner_DataUtil mode) {
      if (this.init_CompoundTag != null && index >= 0) {
         this.flush(Math.max(index + 1, this.write_Map.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.init_CompoundTag.getList("entryAmountModes").orElse(new ListTag());
         list.set(index, StringTag.valueOf((mode == null ? XCarryAction.Inner_DataUtil.FULL_STK : mode).name()));
         this.init_CompoundTag.put("entryAmountModes", list);
      }
   }

   private int write_I(int index) {
      if (this.init_CompoundTag != null && index >= 0) {
         ListTag list = this.init_CompoundTag.getList("entryAmounts").orElse(new ListTag());
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

   private void flush_V(int index, int amount) {
      if (this.init_CompoundTag != null && index >= 0) {
         this.flush(Math.max(index + 1, this.write_Map.getOrDefault("entries", Collections.emptyList()).size()));
         ListTag list = this.init_CompoundTag.getList("entryAmounts").orElse(new ListTag());
         list.set(index, StringTag.valueOf(String.valueOf(Math.max(1, amount))));
         this.init_CompoundTag.put("entryAmounts", list);
      }
   }

   private void open(int index) {
      if (this.init_CompoundTag != null && index >= 0) {
         ListTag modes = this.init_CompoundTag.getList("entryAmountModes").orElse(new ListTag());
         if (index >= 0 && index < modes.size()) {
            modes.remove(index);
         }

         this.init_CompoundTag.put("entryAmountModes", modes);
         ListTag amounts = this.init_CompoundTag.getList("entryAmounts").orElse(new ListTag());
         if (index >= 0 && index < amounts.size()) {
            amounts.remove(index);
         }

         this.init_CompoundTag.put("entryAmounts", amounts);
      }
   }

   private Component init_Component_Str(String key, String entry, int index) {
      if (this.reset_Z(key)) {
         MacroItemTarget target = this.init_ArrayUtil_Str(key, index, entry);
         return this.load_Component_ArrayUtil_Str(target, this.read_Str_Str_Str(key, entry));
      } else {
         return Component.literal(this.read_Str_Str_Str(key, entry));
      }
   }

   private int init_I_Str(String entry, int index, boolean selected) {
      if (selected) {
         return UiColorPalette.store();
      }

      Class<? extends Packet<?>> packetClass = PacketTypeRegistry.init_Class_Str(entry);
      if (packetClass == null) {
         return this.init_CharEncoder.load(l.read);
      }

      boolean c2s = PacketTypeRegistry.load().contains(packetClass);
      return UiColorPalette.init(c2s, index);
   }

   private MacroItemTarget init_ArrayUtil_Str(String key, int index, String entry) {
      List<MacroItemTarget> targets = this.init_List_Str(key);
      if (index >= 0 && index < targets.size()) {
         MacroItemTarget target = targets.get(index);
         if (target != null && (target.init_Z() || target.update())) {
            return target.init_ArrayUtil();
         }
      }

      MacroItemTarget parsed = MacroItemTarget.update(entry);
      return !parsed.init_Z() && !parsed.update() ? null : parsed;
   }

   private boolean reset_Z(String key) {
      return this.get_Z(key) || this.put_Z(key);
   }

   private Component load_Component_ArrayUtil_Str(MacroItemTarget target, String fallback) {
      String safeFallback = fallback == null ? "" : fallback;
      if (target == null) {
         return Component.literal(safeFallback);
      }

      boolean hasSlot = target.init_Z();
      boolean hasIdentity = target.update();
      if (hasSlot && hasIdentity) {
         return Component.literal(target.init_I + ": ").append(target.load_Component().copy());
      }

      if (hasSlot) {
         return Component.literal("#" + target.init_I);
      }

      if (hasIdentity) {
         Component display = target.load_Component();
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

   private String get_Str(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry;
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private int load_I_Str_I(String text, int fallback) {
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

   private int load_I_Str(String entry) {
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

   private boolean init_Z_List_2(List<String> entries, String entry, int ignoreIndex) {
      for (int i = 0; i < entries.size(); i++) {
         if (i != ignoreIndex && entries.get(i).equals(entry)) {
            return true;
         }
      }

      return false;
   }

   private List<ck.Inner_CachePool> read_List() {
      if (this.init_CompoundTag == null) {
         return new ArrayList<>();
      }

      SendPacketAction action = new SendPacketAction();
      action.fromTag(this.init_CompoundTag);
      return new ArrayList<>(action.packets);
   }

   private void write(List<ck.Inner_CachePool> packets) {
      if (this.init_CompoundTag != null) {
         ListTag packetList = new ListTag();
         if (packets != null) {
            for (ck.Inner_CachePool qp : packets) {
               CompoundTag packetTag = MacroClipboardCodec.init(qp);
               if (packetTag != null) {
                  packetList.add(packetTag);
               }
            }
         }

         this.init_CompoundTag.put("packets", packetList);
      }
   }

   private String push_Str() {
      List<ck.Inner_CachePool> packets = this.read_List();
      if (packets.isEmpty()) {
         return "Packets: 0";
      }

      String autoName = "";

      for (ck.Inner_CachePool qp : packets) {
         if (qp != null && qp.init_Packet != null) {
            autoName = PacketNameResolver.init_Str_Packet(qp.init_Packet);
            break;
         }
      }

      if (autoName.isEmpty()) {
         return "Packets: " + packets.size();
      } else {
         return packets.size() == 1 ? "Packets: 1 - " + autoName : "Packets: " + packets.size() + " - " + autoName;
      }
   }

   private void init_V_ck_CachePool(ck.Inner_CachePool packet) {
      if (this.init_CompoundTag != null && packet != null) {
         List<ck.Inner_CachePool> single = Collections.singletonList(packet);
         String base64 = MacroClipboardCodec.init_Str(single);
         if (base64 != null && !base64.isBlank()) {
            this.init_CompoundTag.putString("packetData", base64);
            if (!this.init_CompoundTag.contains("description") || this.init_CompoundTag.getStringOr("description", "").isBlank()) {
               String name = packet.init_Packet != null ? PacketNameResolver.init_Str_Packet(packet.init_Packet) : "Packet";
               this.init_CompoundTag.putString("description", name);
            }
         }
      }
   }

   private String pop_Str() {
      if (this.init_CompoundTag == null) {
         return "Raw Packet: empty";
      } else {
         String packetData = this.init_CompoundTag.getStringOr("packetData", "");
         if (packetData != null && !packetData.isBlank()) {
            String description = this.init_CompoundTag.getStringOr("description", "");
            return description != null && !description.isBlank() ? "Raw Packet: " + description : "Raw Packet: loaded";
         } else {
            return "Raw Packet: empty";
         }
      }
   }

   private void bind() {
      if (this.put_Z()) {
         ck state = ck.init_ck();
         this.init_Screen = init_Minecraft.gui.screen();
         this.index();
         state.read_V_Runnable(() -> {
            state.open_V();
            if (this.init_Screen != null) {
               OverlayScreenController.init_V(init_Minecraft, this.init_Screen);
               this.init_Screen = null;
            }

            this.init_V_Z_Z(false, false);
         });
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
         AutismClientMessaging.sendPrefixed("GBreak: Break a block to capture the insta-break packet");
         final int session = this.auditEditSession;
         state.init_V_Runnable(() -> init_Minecraft.execute(() -> {
            if (this.auditEditSession != session || this.init_CompoundTag == null) {
               return;
            }
            List<ck.Inner_CachePool> captured = ck.init_ck().sync_List();
            if (!captured.isEmpty()) {
               this.write(Collections.singletonList(captured.get(0)));
               this.init_CompoundTag.putString("customName", "GBreak");
               ThemedTextInputBox customNameField = this.init_Map.get("customName");
               if (customNameField != null) {
                  customNameField.init_V_Str("GBreak");
               }

               AutismClientMessaging.sendPrefixed("GBreak packet captured");
            } else {
               AutismClientMessaging.sendPrefixed("GBreak capture finished with no packet");
            }

            if (this.init_Screen != null) {
               OverlayScreenController.init_V(init_Minecraft, this.init_Screen);
               this.init_Screen = null;
            }

            this.init_V_Z_Z(false, false);
            OverlayManager.init_bk().update_V(this);
         }));
      }
   }

   private void a() {
      if (this.put_Z() && this.init_MacroAction instanceof LookAtBlockAction) {
         Screen previousScreen = init_Minecraft.gui.screen();
         this.index();
         ck state = ck.init_ck();
         state.read_V_Runnable(() -> {
            if (previousScreen != null) {
               OverlayScreenController.init_V(init_Minecraft, previousScreen);
            }

            state.load_V_Consumer(null);
            this.init_V_Z_Z(false, false);
         });
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
         state.value(this.poll_Z);
         state.load_V_Consumer(payload -> init_Minecraft.execute(() -> {
            List<String> selected = this.write_Map.get("entityIds");
            if (selected != null && payload != null && !payload.isBlank()) {
               if (this.poll_Z) {
                  selected.removeIf(existing -> existing.startsWith(this.stop(payload)));
               }

               if (!selected.contains(payload)) {
                  selected.add(payload);
               }
            }

            if (previousScreen != null) {
               OverlayScreenController.init_V(init_Minecraft, previousScreen);
            }

            this.init_V_Z_Z(false, false);
            OverlayManager.init_bk().update_V(this);
         }));
      }
   }

   private void push(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitGuiName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "checkDistance", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "maxDistance", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<String> selected = this.write_Map.getOrDefault("soundIds", Collections.emptyList());
      cy = this.init_I_GuiGraphicsExtract_2(ctx, x, cy, w, mx, my, "wait_sound_selected", "Sounds", selected, value -> {
         selected.remove(value);
         EventBus vp = this.tick_Map.get("wait_sound_selected");
         if (vp != null) {
            vp.read_V(-1);
         }
      }, cc::write, "(any sound - add to filter)");
      ThemedTextInputBox search = this.flush_Map.get("soundIds");
      if (search != null) {
         search.read(x);
         search.write(cy + 1);
         search.update(w);
         search.init_V(ctx, mx, my, delta);
      }

      cy += 18;
      String filter = search != null ? search.load_Str().trim().toLowerCase() : "";
      List<String> filtered = this.init_List_Str_2("wait_sound_search", open_List(), filter, cc::write);
      this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "wait_sound_search", filtered, 6, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, cc::write);
   }

   private void pop(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "checkMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "radius", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "centerOnPlayer", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "pos", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "mustBeLookingAt", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<String> selected = this.write_Map.getOrDefault("entityIds", Collections.emptyList());
      cy = this.init_I_GuiGraphicsExtract_2(
         ctx, x, cy, w, mx, my, "wait_entity_selected", "Entities", selected, selected::remove, this::reset_Str, "(any entity - add type or specific)"
      );
      ThemedTextInputBox search = this.flush_Map.get("entityIds");
      String modeLabel = this.poll_Z ? "[Spec]" : "[Type]";
      int modeW = Math.max(this.write_I("[Spec]"), this.write_I("[Type]"));
      int capW = this.write_I("CAP");
      int searchW = w - modeW - 2 - capW - 2;
      if (search != null) {
         search.read(x);
         search.write(cy);
         search.update(searchW);
         search.init_V(ctx, mx, my, delta);
      }

      int modeX = x + searchW + 2;
      this.init_V_GuiGraphicsExtract(ctx, modeX, cy, modeW, 13, modeLabel, mx, my, () -> this.poll_Z = !this.poll_Z);
      this.init_V_GuiGraphicsExtract(ctx, modeX + modeW + 2, cy, capW, 13, "CAP", mx, my, this::x);
      cy += 18;
      String filter = search != null ? search.load_Str().trim().toLowerCase() : "";
      List<String> filtered = this.init_List_Str_2("wait_entity_search", poll_List(), filter, cc::read);
      this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "wait_entity_search", filtered, 4, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, cc::read);
      cy += 66;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Nearby Entities", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 13;
      List<String> nearbyEntries = this.sync_List();
      this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "wait_entity_nearby", nearbyEntries, 3, value -> {
         String stored = this.poll_Z ? value : this.start(value);
         if (!stored.isBlank()) {
            if (this.poll_Z) {
               boolean removed = selected.removeIf(existing -> existing.startsWith(this.stop(stored)));
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
         String stored = this.poll_Z ? value : this.start(value);
         return this.poll_Z ? selected.stream().anyMatch(existing -> existing.startsWith(this.stop(stored))) : selected.contains(stored);
      }, this::next);
   }

   private void tick(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "targetMode", x, cy, w, mx, my, delta);
      String targetMode = this.tick_Str("targetMode");
      if ("ENTITY".equals(targetMode)) {
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "entityTargets", x, cy, w, mx, my, delta);
      } else if ("BLOCK".equals(targetMode)) {
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "pos", x, cy, w, mx, my, delta);
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "guiName", x, cy, w, mx, my, delta);
      if ("ENTITY".equals(targetMode)) {
         String hint = "Pick captures exact entities. Last Target reuses the most recent interaction target.";
         cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
         List<String> selected = this.write_Map.getOrDefault("entityTargets", Collections.emptyList());
         cy += 13;
         List<String> nearbyEntries = this.sync_List();
         this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "open_container_entity_nearby", nearbyEntries, 3, value -> {
            if (selected.contains(value)) {
               selected.remove(value);
            } else {
               selected.add(value);
            }
         }, selected::contains, this::next);
      } else if ("LAST_TARGET".equals(targetMode)) {
         this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Uses the last block or entity container you actually opened.");
      } else {
         cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Capture only marks the block. It will not open it.");
         this.init_V_GuiGraphicsExtract_3(ctx, x, cy, w, mx, my, "open_container_nearby", "pos");
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, String listKey, String fieldKey) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Nearby Containers", font, UiColorPalette.parse(), x, y + 2, false);
      y += 14;
      List<BlockPos> nearbyContainers = this.flush_List();
      this.init_V_GuiGraphicsExtract(
         ctx,
         x,
         y,
         w,
         mx,
         my,
         listKey,
         nearbyContainers,
         2,
         pos -> this.init_V_Str_BlockPos(fieldKey, pos),
         pos -> this.init_Z_Str_BlockPos(fieldKey, pos),
         this::init_Str_BlockPos
      );
   }

   private void run(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "allItems", x, cy, w, mx, my, delta);
      List<String> selected = this.write_Map.getOrDefault("targetItems", Collections.emptyList());
      if (!this.load_Map.getOrDefault("allItems", false)) {
         cy = this.init_I_GuiGraphicsExtract(
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
            StoreItemAction::load_Str_Str_3,
            value -> this.init_Component_Str("targetItems", value, selected.indexOf(value)),
            selected::clear,
            "(nothing selected - search, type #slot, or capture)",
            true
         );
         int storeEditIdx = this.open_Map.getOrDefault("store_items_selected", -1);
         boolean storeEditing = storeEditIdx >= 0 && storeEditIdx < selected.size();
         ThemedTextInputBox search = this.flush_Map.get("targetItems");
         int pickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
         int addBtnW = this.write_I("+Add");
         int slotW = 44;
         int capBtnX = x + w - pickW;
         int addBtnX = capBtnX - 3 - addBtnW;
         int slotX = addBtnX - 2 - slotW;
         int nameW = slotX - x - 2;
         ThemedTextInputBox storeSlotF = this.init_Map.get("store_slot");
         if (storeSlotF == null) {
            storeSlotF = this.init_k_I(slotW);
            storeSlotF.poll(true);
            storeSlotF.init_V_Component(Component.literal("Slot#"));
            this.init_Map.put("store_slot", storeSlotF);
         }

         String pendingText = this.poll_Map.remove("store_items_selected");
         if (pendingText != null) {
            if (search != null) {
               search.init_V_Str(this.get_Str(pendingText));
            }

            MacroItemTarget parsed = MacroItemTarget.update(pendingText);
            storeSlotF.init_V_Str(parsed.init_Z() ? String.valueOf(parsed.init_I) : "");
         }

         if (search != null) {
            search.read(x);
            search.write(cy);
            search.update(nameW);
            search.init_V(ctx, mx, my, delta);
         }

         storeSlotF.read(slotX);
         storeSlotF.write(cy);
         storeSlotF.update(slotW);
         storeSlotF.init_V(ctx, mx, my, delta);
         if (storeEditing && search != null) {
            String nameText = search.load_Str().strip();
            String slotText = storeSlotF.load_Str().strip();
            String entry = write_Str_Str_Str(nameText, slotText);
            if (!entry.isEmpty()) {
               String normalized = StoreItemAction.init_Str_Str_8(entry);
               if (normalized != null && !normalized.isBlank() && !normalized.equals(selected.get(storeEditIdx))) {
                  selected.set(storeEditIdx, normalized);
                  this.sync_Map.put("targetItems", this.init_List_Str("targetItems"));
               }
            }
         }

         ThemedTextInputBox fStoreSlotF = storeSlotF;
         this.init_ByteReader_GuiGraphicsExtract_2(ctx, addBtnX, cy, addBtnW, 13, "+Add", ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
            if (search != null) {
               String nameText = search.load_Str().strip();
               String slotText = fStoreSlotF.load_Str().strip();
               String entry = write_Str_Str_Str(nameText, slotText);
               if (!entry.isEmpty()) {
                  String normalized = StoreItemAction.init_Str_Str_8(entry);
                  if (normalized != null && !normalized.isBlank() && !selected.contains(normalized)) {
                     selected.add(normalized);
                     this.sync_Map.put("targetItems", this.init_List_Str("targetItems"));
                  }

                  search.init_V_Str("");
                  fStoreSlotF.init_V_Str("");
                  this.open_Map.put("store_items_selected", -1);
               }
            }
         });
         boolean capturing = this.init_StreamUtil.init("targetItems");
         this.init_ByteReader_GuiGraphicsExtract(ctx, capBtnX, cy, pickW, 13, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("targetItems"));
         cy += 18;
         String filter = search != null ? search.load_Str().trim().toLowerCase() : "";
         List<String> filtered = this.init_List_Str_2("store_items_search", push_List(), filter, cc::init);
         this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "store_items_search", filtered, 6, value -> {
            if (selected.contains(value)) {
               selected.remove(value);
            } else {
               selected.add(value);
            }

            this.open_Map.put("store_items_selected", -1);
            this.sync_Map.put("targetItems", this.init_List_Str("targetItems"));
         }, selected::contains, cc::init);
         cy += 82;
         cy = this.init_I_GuiGraphicsExtract(
            ctx, x, cy, w, "Pick on an item adds it by name, from any slot on the screen. Ctrl+pick or an empty slot adds that exact slot."
         );
         String modeHint = "LOOT".equals(this.tick_Str("mode")) ? "Exact slots must be chest/custom GUI slots." : "Exact slots must be player inventory slots.";
         cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, modeHint);
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "persistent", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "delayTicks", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "closeAfter", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract_5(ctx, "closeSendPkt", x, cy, w, mx, my, delta);
   }

   private void get(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "mode", x, cy, w, mx, my, delta);
      List<String> selected = this.write_Map.getOrDefault("targetItems", Collections.emptyList());
      FieldDef targetItemsField = this.load_FieldDef_Str("targetItems");
      if (targetItemsField != null && this.write(targetItemsField)) {
         String emptyLabel = this.pop_Str("targetItems");
         if (emptyLabel == null || emptyLabel.isBlank()) {
            emptyLabel = "(nothing selected)";
         }

         boolean multipleStacks = this.load_Map.getOrDefault("multipleStacks", false);
         int listTop = cy;
         cy = this.init_I_GuiGraphicsExtract(
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
            StoreItemAction::load_Str_Str_3,
            value -> this.init_Component_Str("targetItems", value, selected.indexOf(value)),
            selected::clear,
            emptyLabel,
            true
         );
         int multiBtnW = 96;
         int clearBtnW = this.write_I("Clear");
         int multiBtnX = x + w - clearBtnW - 3 - multiBtnW;
         this.init_ByteReader_GuiGraphicsExtract_3(
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
            () -> this.load_Map.put("multipleStacks", !this.load_Map.getOrDefault("multipleStacks", false))
         );
         int auditEditIdx = this.open_Map.getOrDefault("audit_items_selected", -1);
         boolean auditEditing = auditEditIdx >= 0 && auditEditIdx < selected.size();
         ThemedTextInputBox search = this.flush_Map.get("targetItems");
         String addLabel = auditEditing ? "New" : "+Add";
         int pickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
         int addBtnW = this.write_I(addLabel);
         int slotW = 26;
         int pickX = x + w - addBtnW - 3 - pickW;
         int addBtnX = x + w - addBtnW;
         int slotX = pickX - 3 - slotW;
         int nameW = slotX - x - 2;
         ThemedTextInputBox auditSlotF = this.init_Map.get("audit_slot");
         if (auditSlotF == null) {
            auditSlotF = this.init_k_I(slotW);
            auditSlotF.poll(true);
            auditSlotF.init_V_Component(Component.literal("Slot"));
            this.init_Map.put("audit_slot", auditSlotF);
         }

         String pendingText = this.poll_Map.remove("audit_items_selected");
         if (pendingText != null) {
            if (search != null) {
               search.init_V_Str(this.get_Str(pendingText));
            }

            MacroItemTarget parsed = MacroItemTarget.update(pendingText);
            auditSlotF.init_V_Str(parsed.init_Z() ? String.valueOf(parsed.init_I) : "");
         }

         if (search != null) {
            search.read(x);
            search.write(cy);
            search.update(nameW);
            search.init_V(ctx, mx, my, delta);
         }

         auditSlotF.read(slotX);
         auditSlotF.write(cy);
         auditSlotF.update(slotW);
         auditSlotF.init_V(ctx, mx, my, delta);
         if (auditEditing && search != null) {
            String nameText = search.load_Str().strip();
            String slotText = auditSlotF.load_Str().strip();
            String entry = write_Str_Str_Str(nameText, slotText);
            if (!entry.isEmpty()) {
               String normalized = StoreItemAction.init_Str_Str_8(entry);
               if (normalized != null && !normalized.isBlank() && !normalized.equals(selected.get(auditEditIdx))) {
                  selected.set(auditEditIdx, normalized);
                  this.sync_Map.put("targetItems", this.init_List_Str("targetItems"));
               }
            }
         }

         ThemedTextInputBox fAuditSlotF = auditSlotF;
         boolean capturing = this.init_StreamUtil.init("targetItems");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, cy, pickW, 13, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("targetItems"));
         this.init_ByteReader_GuiGraphicsExtract_2(ctx, addBtnX, cy, addBtnW, 13, addLabel, ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
            if (search != null) {
               if (this.open_Map.getOrDefault("audit_items_selected", -1) >= 0) {
                  this.open_Map.put("audit_items_selected", -1);
                  this.poll_Map.put("audit_items_selected", "");
                  search.init_V_Str("");
                  fAuditSlotF.init_V_Str("");
               } else {
                  String nameText = search.load_Str().strip();
                  String slotText = fAuditSlotF.load_Str().strip();
                  String entryx = write_Str_Str_Str(nameText, slotText);
                  if (!entryx.isEmpty()) {
                     String normalizedx = StoreItemAction.init_Str_Str_8(entryx);
                     if (normalizedx != null && !normalizedx.isBlank() && !selected.contains(normalizedx)) {
                        selected.add(normalizedx);
                        this.sync_Map.put("targetItems", this.init_List_Str("targetItems"));
                     }

                     search.init_V_Str("");
                     fAuditSlotF.init_V_Str("");
                     this.open_Map.put("audit_items_selected", -1);
                     this.poll_Map.put("audit_items_selected", "");
                  }
               }
            }
         });
         cy += 18;
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "openMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "openCommand", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "containerPos", x, cy, w, mx, my, delta);
      if ("CONTAINER".equals(this.tick_Str("openMode"))) {
         cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Capture stores the clicked block position. Nearby also includes likely server-handled GUI blocks.");
         this.init_V_GuiGraphicsExtract_3(ctx, x, cy, w, mx, my, "inventory_audit_nearby", "containerPos");
         cy += 43;
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "dupeVector", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "iterations", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "maxTransferAttempts", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "transferRetryDelayMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "spamCount", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract_5(ctx, "spamDelayMs", x, cy, w, mx, my, delta);
   }

   private void set(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "commandTemplate", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "amountInput", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "divideEnabled", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "probeHidden", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "delayEnabled", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "delayMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "confirmMacro", x, cy, w, mx, my, delta);
      List<String> selected = this.write_Map.getOrDefault("players", Collections.emptyList());
      cy = this.init_I_GuiGraphicsExtract(
         ctx,
         x,
         cy,
         w,
         mx,
         my,
         "pay_players_selected",
         "Players",
         selected,
         value -> this.read_V_List_Str(selected, value),
         value -> value,
         ignored -> null,
         selected::clear,
         "(scan the server or add one manually)"
      );
      ThemedTextInputBox search = this.flush_Map.get("players");
      int scanW = this.write_I("Scan");
      int allW = this.write_I("All");
      int searchW = Math.max(1, w - scanW - allW - 4);
      if (search != null) {
         search.read(x);
         search.write(cy);
         search.update(searchW);
         search.init_V(ctx, mx, my, delta);
      }

      int scanX = x + searchW + 2;
      int allX = scanX + scanW + 2;
      this.init_V_GuiGraphicsExtract(ctx, scanX, cy, scanW, 13, "Scan", mx, my, this::b);
      this.init_V_GuiGraphicsExtract(ctx, allX, cy, allW, 13, "All", mx, my, () -> this.init_V_List_Str(selected, search != null ? search.load_Str() : ""));
      cy += 18;
      String query = search != null ? search.load_Str() : "";
      if (!this.state_Z) {
         this.init_V_GuiGraphicsExtract_3(ctx, x, cy, w, "pay_players_scan", 1, "Press Scan to load the current server players.");
         cy += 17;
      } else {
         cy = this.init_I_GuiGraphicsExtract_3(ctx, x, cy, w, mx, my, selected, query) + 4;
      }

      ThemedTextInputBox amountField = this.init_Map.get("amountInput");
      long amount = PayAction.init_J_Str(amountField != null ? amountField.load_Str() : "");
      boolean divide = this.load_Map.getOrDefault("divideEnabled", false);
      int count = selected.size();
      String summary;
      if (count == 0) {
         summary = "Empty list = pays EVERYONE (tab + probed hidden).";
      } else if (divide) {
         long[] dist = PayAction.init(amount, count);
         String per = dist[0] == dist[count - 1] ? PayAction.init_Str_J(dist[0]) : PayAction.init_Str_J(dist[count - 1]) + "-" + PayAction.init_Str_J(dist[0]);
         summary = "Splits " + PayAction.init_Str_J(amount) + " among " + count + " (" + per + " each).";
      } else {
         summary = "Pays " + count + " x " + PayAction.init_Str_J(amount) + " (" + PayAction.init_Str_J(amount * count) + " total).";
      }

      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, summary);
      String hint = this.state_Z
         ? "Click a name to add it. Click a rank header to add/remove everyone in that rank; All adds all."
         : "Scan to list players by rank. Type a name then Enter to add it manually.";
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
   }

   private List<ActionEditorOverlay.Inner_BufferHelper> load_List_Str(String query) {
      Map<String, List<String>> byPrefix = new LinkedHashMap<>();

      for (String name : this.load_List_List_Str(this.start_List, query)) {
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
      List<ActionEditorOverlay.Inner_BufferHelper> rows = new ArrayList<>();

      for (String prefix : order) {
         List<String> names = byPrefix.get(prefix);
         names.sort(String.CASE_INSENSITIVE_ORDER);
         rows.add(ActionEditorOverlay.Inner_BufferHelper.init(prefix, names));

         for (String name : names) {
            rows.add(ActionEditorOverlay.Inner_BufferHelper.init(prefix, name));
         }
      }

      return rows;
   }

   private int init_I_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, List<String> selected, String query) {
      int rowH = 13;
      List<ActionEditorOverlay.Inner_BufferHelper> entries = this.load_List_Str(query);
      if (entries.isEmpty()) {
         TableIndex.init(ctx, x, y, w, 12, false, false);
         FileCache.init(ctx, this.init_Font, "No players found", x, y, w);
         return y + rowH;
      }

      int clipTop = this.init_NetHelper != null ? this.init_NetHelper.write() : -2147483648;
      int clipBottom = this.init_NetHelper != null ? this.init_NetHelper.load() : 2147483647;
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int iy = y;

      for (ActionEditorOverlay.Inner_BufferHelper row : entries) {
         if (iy + rowH > clipTop && iy < clipBottom) {
            if (row.init_Z()) {
               List<String> members = row.init_List();
               int selCount = (int)members.stream().filter(m -> this.init_Z_List_Str(selected, m)).count();
               boolean allSel = selCount == members.size() && selCount > 0;
               autismclient.gui.f.b.k.init(ctx, x, iy, x + w, iy + rowH - 1, 1140850688);
               autismclient.gui.f.b.k.init(ctx, x, iy, x + 2, iy + rowH - 1, allSel ? -11628966 : -7459271);
               int headerColor = allSel ? -6496088 : -1593160;
               int baseY = iy + Math.max(1, (rowH - 8) / 2);
               String marker = allSel ? "✓ " : "+ ";
               String counter = "  (" + selCount + "/" + members.size() + ")";
               int markerW = this.init_Font.width(marker);
               int counterW = this.init_Font.width(counter);
               int rankMaxW = Math.max(8, w - 12 - markerW - counterW);
               int cx = x + 6;
               autismclient.gui.f.b.k.init(ctx, this.init_Font, marker, font, headerColor, cx, baseY, false);
               cx += markerW;
               Component styled = row.init_Str().isBlank() ? null : this.set_Map.get(row.init_Str());
               if (styled != null && this.init_Font.width(styled) <= rankMaxW) {
                  ctx.text(this.init_Font, styled, cx, baseY, -1, false);
                  cx += this.init_Font.width(styled);
               } else {
                  String fallback = row.init_Str().isBlank() ? "No rank" : row.init_Str();
                  int fallbackColor = row.init_Str().isBlank() ? headerColor : -1;
                  fallback = autismclient.gui.f.b.k.init(this.init_Font, fallback, rankMaxW, font, fallbackColor);
                  autismclient.gui.f.b.k.init(ctx, this.init_Font, fallback, font, fallbackColor, cx, baseY, false);
                  cx += this.init_Font.width(fallback);
               }

               autismclient.gui.f.b.k.init(ctx, this.init_Font, counter, font, headerColor, cx, baseY, false);
               List<String> targetMembers = members;
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, w, rowH, () -> this.init_V_List_List(selected, targetMembers)));
            } else {
               boolean sel = this.init_Z_List_Str(selected, row.load());
               boolean hovered = mx >= x && mx < x + w && my >= iy && my < iy + rowH;
               String label = autismclient.gui.f.b.k.init(this.init_Font, "  " + row.load(), w - 6, font, UiColorPalette.parse());
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  Component.literal(label),
                  autismclient.gui.f.NetHelper.init(x, iy, w, rowH),
                  hovered,
                  sel,
                  sel ? FileCache.Inner_DataUtil.READY : FileCache.Inner_DataUtil.NORMAL,
                  false
               );
               String name = row.load();
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, w, rowH, () -> this.load_V_List_Str(selected, name)));
            }
         }

         iy += rowH;
      }

      return iy;
   }

   private void put(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      List<ak.Inner_DataUtil> selected = this.prev_List != null ? this.prev_List : Collections.emptyList();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Module Actions (" + selected.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      boolean canClear = !selected.isEmpty();
      int tmClearW = this.write_I("Clear");
      int clearX = x + w - tmClearW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, clearX, cy, tmClearW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClear, mx, my, () -> {
         selected.clear();
         EventBus vp = this.tick_Map.get("toggle_module_selected");
         if (vp != null) {
            vp.load_V(0);
         }
      });
      cy += 14;
      int listH = 56;
      int itemW = w - 5 - 1;
      EventBus toggleViewport = this.init_EventBus_Map(this.tick_Map, "toggle_module_selected", x, cy, w, listH, 14, 5);
      toggleViewport.init_V(selected.size() * 14);
      toggleViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (selected.isEmpty()) {
         FileCache.init(ctx, this.init_Font, "Pick modules below.", x, cy, itemW);
      } else {
         toggleViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int first = toggleViewport.pop();

         for (int i = first; i < selected.size() && i <= toggleViewport.tick(); i++) {
            int iy = toggleViewport.init_I(i);
            if (iy != -2147483648) {
               ak.Inner_DataUtil entry = selected.get(i);
               int removeW = 13;
               ak.Inner_StreamUtil[] tmModes = ak.Inner_StreamUtil.values();
               List<String> tmOptions = new ArrayList<>();

               for (ak.Inner_StreamUtil m : tmModes) {
                  tmOptions.add(this.init_Str_ak_StreamUtil(m));
               }

               int modeW = this.init_I_List_I(tmOptions, 52);
               int rowW = itemW - removeW - modeW - 4;
               boolean hovered = mx >= x && mx < x + rowW && my >= iy + 1 && my < iy + 14;
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  Component.literal(entry.init_Str),
                  autismclient.gui.f.NetHelper.init(x, iy + 1, rowW, 13),
                  hovered,
                  false,
                  FileCache.Inner_DataUtil.NORMAL,
                  false
               );
               int modeX = x + rowW + 2;
               int entryIndex = i;
               this.init_V_Str("toggle_mode_" + entryIndex, tmOptions, entry.init_ak_StreamUtil.ordinal(), modeX, iy + 1, modeW, 13, idx -> {
                  if (entryIndex >= 0 && entryIndex < selected.size()) {
                     selected.get(entryIndex).init_ak_StreamUtil = tmModes[Math.max(0, Math.min(idx, tmModes.length - 1))];
                  }
               });
               int removeX = modeX + modeW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy + 1, removeW, mx, my, () -> {
                  if (entryIndex >= 0 && entryIndex < selected.size()) {
                     selected.remove(entryIndex);
                     EventBus vp = this.tick_Map.get("toggle_module_selected");
                     if (vp != null) {
                        vp.read_V(-1);
                     }
                  }
               });
            }
         }

         toggleViewport.init(ctx);
      }

      cy += listH + 4;
      ThemedTextInputBox search = this.flush_Map.get("_toggle_module_search");
      int refreshW = this.write_I("Refresh");
      int addW = this.write_I("+Add");
      int searchW = Math.max(1, w - refreshW - addW - 4);
      if (search != null) {
         search.read(x);
         search.write(cy);
         search.update(searchW);
         search.init_V(ctx, mx, my, delta);
      }

      int refreshX = x + searchW + 2;
      int addX = refreshX + refreshW + 2;
      this.init_V_GuiGraphicsExtract(ctx, refreshX, cy, refreshW, 13, "Refresh", mx, my, this::c);
      this.init_V_GuiGraphicsExtract(ctx, addX, cy, addW, 13, "+Add", mx, my, () -> this.pop_V(search != null ? search.load_Str() : ""));
      cy += 18;
      String query = search != null ? search.load_Str() : "";
      List<String> filteredAutism = this.init_List_Str_2("toggle_module_autism_registry", this.stop_List, query);
      if (this.stop_List.isEmpty()) {
         this.init_V_GuiGraphicsExtract_3(ctx, x, cy, w, "toggle_module_registry_empty", 6, "No modules found right now.");
         cy += 82;
      } else {
         cy = this.init_I_GuiGraphicsExtract(
            ctx, x, cy, w, mx, my, "AUTISM Modules", "toggle_module_autism_registry", this.stop_List, filteredAutism, 6, selected
         );
      }

      String hint = selected.isEmpty()
         ? "Pick modules below, then click the mode chip on each row."
         : "Each row runs with its own Toggle / Enable / Disable setting.";
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
   }

   private int init_I_GuiGraphicsExtract(
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
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, title, font, UiColorPalette.parse(), x, y + 1, false);
      y += 12;
      if (source != null && !source.isEmpty()) {
         this.init_V_GuiGraphicsExtract(
            ctx, x, y, w, mx, my, listKey, filtered, visibleRows, this::tick_V, value -> this.load_Z_List_Str(selected, value), value -> value
         );
      } else {
         this.init_V_GuiGraphicsExtract_3(ctx, x, y, w, listKey, visibleRows, "No modules found right now.");
      }

      return y + visibleRows * 13 + 4;
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, int x, int y, int w, String listKey, int visibleRows, String message) {
      int listH = visibleRows * 13;
      int itemW = w - 5 - 1;
      TableIndex.init(ctx, x, y, itemW, 12, false, false);
      FileCache.init(ctx, this.init_Font, this.write_Str(message), x, y, itemW);
   }

   private void b() {
      this.start_List.clear();
      this.get_Map.clear();
      this.set_Map.clear();
      this.state_Z = true;
      EventBus vp = this.close_Map.get("pay_players_scan");
      if (vp != null) {
         vp.load_V(0);
      }

      for (bO.Inner_StreamUtil player : bO.init(init_Minecraft)) {
         if (player != null && player.init_Str() != null && !player.init_Str().isBlank() && !this.init_Z_List_Str(this.start_List, player.init_Str())) {
            this.start_List.add(player.init_Str());
            if (player.init_Z()) {
               this.get_Map.put(player.init_Str().toLowerCase(Locale.ROOT), player.load());
               if (player.init_Component() != null) {
                  this.set_Map.putIfAbsent(player.load(), player.init_Component());
               }
            }
         }
      }

      BackgroundTaskExecutor.init_Future_Str_Runnable("pay-scan-probe", () -> {
         List<String> probed = bN.init(init_Minecraft, true);
         if (!probed.isEmpty()) {
            init_Minecraft.execute(() -> {
               boolean changed = false;

               for (String name : probed) {
                  if (!this.init_Z_List_Str(this.start_List, name)) {
                     this.start_List.add(name);
                     this.get_Map.putIfAbsent(name.toLowerCase(Locale.ROOT), "unknown");
                     changed = true;
                  }
               }

               if (changed) {
                  this.start_List.sort(String::compareToIgnoreCase);
               }
            });
         }
      });
      this.start_List.sort(String::compareToIgnoreCase);
   }

   private String set_Str(String name) {
      return name == null ? "" : this.get_Map.getOrDefault(name.toLowerCase(Locale.ROOT), "");
   }

   private void init_V_List_List(List<String> selected, List<String> members) {
      if (selected != null && members != null && !members.isEmpty()) {
         boolean allSelected = members.stream().allMatch(mx -> this.init_Z_List_Str(selected, mx));
         if (allSelected) {
            for (String m : members) {
               this.read_V_List_Str(selected, m);
            }
         } else {
            for (String m : members) {
               if (!this.init_Z_List_Str(selected, m)) {
                  selected.add(m);
               }
            }
         }
      }
   }

   private void c() {
      this.stop_List.clear();
      EventBus autismVp = this.close_Map.get("toggle_module_autism_registry");
      if (autismVp != null) {
         autismVp.load_V(0);
      }

      EventBus emptyVp = this.close_Map.get("toggle_module_registry_empty");
      if (emptyVp != null) {
         emptyVp.load_V(0);
      }

      for (String moduleName : autismclient.util.CommandPrefixHelper.read_List()) {
         if (moduleName != null && !moduleName.isBlank() && !this.init_Z_List_Str(this.stop_List, moduleName)) {
            this.stop_List.add(moduleName);
         }
      }

      this.stop_List.sort(String::compareToIgnoreCase);
   }

   private List<String> load_List_List_Str(List<String> source, String query) {
      return this.init_List_Str_2(null, source, query);
   }

   private List<String> init_List_Str_2(String cacheKey, List<String> source, String query, Function<String, String> labeler) {
      String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      List<String> target = this.pop_Map.computeIfAbsent(cacheKey, ignored -> new ArrayList<>());
      return autismclient.gui.macro.editor.a.CachePool.load(
         source, target, id -> id != null && init_Z_Str_StrArr(filter, id, clear_Str_Str(id), labeler.apply(id))
      );
   }

   private List<String> init_List_Str_2(String cacheKey, List<String> source, String query) {
      if (source != null && !source.isEmpty()) {
         String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
         List<String> target = cacheKey == null ? new ArrayList<>() : this.pop_Map.computeIfAbsent(cacheKey, ignored -> new ArrayList<>());
         return autismclient.gui.macro.editor.a.CachePool.load(
            source, target, value -> value != null && (filter.isEmpty() || value.toLowerCase(Locale.ROOT).contains(filter))
         );
      } else {
         return Collections.emptyList();
      }
   }

   private void init_V_List_Str(List<String> selected, String query) {
      if (selected != null) {
         for (String player : this.load_List_List_Str(this.start_List, query)) {
            if (!this.init_Z_List_Str(selected, player)) {
               selected.add(player);
            }
         }
      }
   }

   private void load_V_List_Str(List<String> selected, String player) {
      if (selected != null && player != null && !player.isBlank()) {
         if (this.init_Z_List_Str(selected, player)) {
            this.read_V_List_Str(selected, player);
         } else {
            selected.add(player);
         }
      }
   }

   private boolean init_Z_List_Str(List<String> values, String target) {
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

   private void read_V_List_Str(List<String> values, String target) {
      if (values != null && target != null) {
         values.removeIf(value -> value != null && value.equalsIgnoreCase(target));
      }
   }

   private void pop_V(String query) {
      List<String> filtered = this.load_List_List_Str(this.stop_List, query);
      if (!filtered.isEmpty()) {
         this.tick_V(filtered.get(0));
      }
   }

   private void tick_V(String moduleName) {
      if (this.prev_List != null && moduleName != null && !moduleName.isBlank() && !this.load_Z_List_Str(this.prev_List, moduleName)) {
         this.prev_List.add(new ak.Inner_DataUtil(moduleName, ak.Inner_StreamUtil.init));
      }
   }

   private boolean load_Z_List_Str(List<ak.Inner_DataUtil> entries, String moduleName) {
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

   private String init_Str_ak_StreamUtil(ak.Inner_StreamUtil mode) {
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

   private void add(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      int headerBtnW = this.write_I("Clear");
      this.init_V_GuiGraphicsExtract_2(ctx, font, "listenDuringPreviousAction", "Listen During Previous", x, cy, w, mx, my);
      cy += 16;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      List<WaitForSlotChangeAction.Inner_DataUtil> entries = this.run_List != null ? this.run_List : new ArrayList<>();
      if (this.f_l_I >= entries.size()) {
         this.f_l_I = -1;
      }

      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Items / Slots (" + entries.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      boolean canClear = !entries.isEmpty();
      int clearX = x + w - headerBtnW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, clearX, cy, headerBtnW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClear, mx, my, () -> {
         entries.clear();
         this.f_l_I = -1;
         this.mark();
      });
      cy += 14;
      int delW = 13;
      int rowW = w - 5 - 1 - delW - 2;
      int selAreaH = 56;
      EventBus wscViewport = this.init_EventBus_Map(this.tick_Map, "_wsc_entries", x, cy, w, selAreaH, 14, 5);
      wscViewport.init_V(entries.size() * 14);
      wscViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (!entries.isEmpty()) {
         wscViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int firstVis = wscViewport.pop();

         for (int i = firstVis; i < entries.size() && i <= wscViewport.tick(); i++) {
            int iy = wscViewport.init_I(i);
            if (iy != -2147483648) {
               boolean selected = i == this.f_l_I;
               WaitForSlotChangeAction.Inner_DataUtil e = entries.get(i);
               Component targetDisp = this.load_Component_ArrayUtil_Str(e.init_ArrayUtil(), "(any slot)");
               String modeSummary = " • " + e.init_Str();
               Component rowLabel = Component.empty().append(targetDisp).append(Component.literal(modeSummary).withStyle(s -> s.withColor(-7829368)));
               boolean rowHovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx, this.init_Font, rowLabel, autismclient.gui.f.NetHelper.init(x, iy, rowW, 13), rowHovered, selected, FileCache.Inner_DataUtil.NORMAL, true
               );
               int fi = i;
               if (iy + 13 > cy && iy < cy + selAreaH) {
                  this.valid_List
                     .add(
                        new ActionEditorOverlay.Inner_StreamUtil(
                           x, Math.max(cy, iy), rowW, Math.min(iy + 13, cy + selAreaH) - Math.max(cy, iy), () -> this.update_V(fi)
                        )
                     );
               }

               int delX = x + rowW + 2;
               this.init_V_GuiGraphicsExtract_2(ctx, delX, iy, delW, mx, my, () -> {
                  if (this.f_l_I == fi) {
                     this.f_l_I = -1;
                     this.mark();
                  } else if (this.f_l_I > fi) {
                     this.f_l_I--;
                  }

                  entries.remove(fi);
                  EventBus vp = this.tick_Map.get("_wsc_entries");
                  if (vp != null) {
                     vp.read_V(-1);
                  }
               });
            }
         }

         wscViewport.init(ctx);
      } else {
         FileCache.init(ctx, this.init_Font, "No entries yet. Add an item or slot below.", x, cy, w - 5 - 1);
      }

      cy += selAreaH;
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 6;
      ThemedTextInputBox wscAddF = this.flush_Map.get("_wsc_add");
      ThemedTextInputBox wscSlotF = this.init_Map.get("wsc_slot");
      if (wscAddF != null && wscSlotF != null) {
         int pickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
         int slotW = 44;
         int pickX = x + w - pickW;
         int slotX = pickX - 2 - slotW;
         wscAddF.read(x);
         wscAddF.write(cy);
         wscAddF.update(slotX - x - 2);
         wscSlotF.read(slotX);
         wscSlotF.write(cy);
         wscSlotF.update(slotW);
         wscAddF.init_V(ctx, mx, my, delta);
         wscSlotF.init_V(ctx, mx, my, delta);
         boolean capturing = this.init_StreamUtil.init("_wsc_entries");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, cy, pickW, 13, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("_wsc_entries"));
      }

      cy += 16;
      boolean editing = this.f_l_I >= 0 && this.f_l_I < entries.size();
      WaitForSlotChangeAction.Inner_StreamUtil curMode = editing ? entries.get(this.f_l_I).init_aD_StreamUtil : this.init_aD_StreamUtil;
      int curCount = editing ? entries.get(this.f_l_I).init_I : this.f_m_I;
      int addBtnW = this.write_I("+Add");
      int cntW = 36;
      WaitForSlotChangeAction.Inner_StreamUtil[] modes = WaitForSlotChangeAction.Inner_StreamUtil.values();
      List<String> modeOptions = new ArrayList<>();

      for (WaitForSlotChangeAction.Inner_StreamUtil m : modes) {
         modeOptions.add(m.name().replace("_", " "));
      }

      int modBtnW = this.init_I_List_I(modeOptions, w - addBtnW - 2 - cntW - 2);
      this.init_V_Str("wsc_mode", modeOptions, curMode.ordinal(), x, cy, modBtnW, 13, idx -> {
         WaitForSlotChangeAction.Inner_StreamUtil chosen = modes[Math.max(0, Math.min(idx, modes.length - 1))];
         if (this.f_l_I >= 0 && this.f_l_I < entries.size()) {
            entries.get(this.f_l_I).init_aD_StreamUtil = chosen;
         } else {
            this.init_aD_StreamUtil = chosen;
         }
      });
      int cntX = x + modBtnW + 2;
      ThemedTextInputBox countF = this.init_Map.get("wsc_count");
      boolean countRelevant = curMode == WaitForSlotChangeAction.Inner_StreamUtil.COUNT_AT_LEAST || curMode == WaitForSlotChangeAction.Inner_StreamUtil.COUNT_BELOW;
      if (countF != null) {
         if (countRelevant) {
            countF.read(cntX);
            countF.write(cy);
            countF.update(cntW);
            if (!countF.init_Z() && !this.prev_Z) {
               this.prev_Z = true;
               countF.init_V_Str(String.valueOf(curCount));
               this.prev_Z = false;
            }

            countF.init_V(ctx, mx, my, delta);
         } else {
            TableIndex.read(ctx, cntX, cy, cntW, 13);
            this.sync_V(ctx, cntX, cy, cntW, 13, -14018022);
            autismclient.gui.f.b.k.init(ctx, this.init_Font, "-", font, UiColorPalette.bind(), cntX + (cntW - this.init_I_Identifier_Str(font, "-")) / 2, cy + 3, false);
         }
      }

      int plusX = cntX + cntW + 2;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, plusX, cy, addBtnW, 13, "+Add", ByteReader.Inner_StreamUtil.SUCCESS, true, mx, my, () -> {
         if (wscAddF != null && wscSlotF != null) {
            this.init_V_k_k(wscAddF, wscSlotF);
            this.f_l_I = -1;
            this.mark();
         }
      });
      cy += 16;
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.f_l_I >= 0 ? "Editing row - all entries must match." : "Click a row to edit - all must match.");
   }

   private void clear(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, mx, my, delta);
      int[] useBtns = this.init_IArr_I(x, w, false, "Use Held", "Use Current");
      this.init_V_GuiGraphicsExtract(ctx, useBtns[0], cy, useBtns[1], 13, "Use Held", mx, my, this::o);
      this.init_V_GuiGraphicsExtract(ctx, useBtns[2], cy, useBtns[3], 13, "Use Current", mx, my, () -> {
         ThemedTextInputBox field = this.init_Map.get("itemName");
         if (field != null) {
            field.init_V_Str("");
         }

         ThemedTextInputBox slotField = this.init_Map.get("slot");
         if (slotField != null) {
            slotField.init_V_Str("");
         }

         this.update_Map.remove("itemName");
      });
      cy += 18;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "useMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForFinish", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "sneak", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "holdTicks", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "useCount", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGui", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "guiName", x, cy, w, mx, my, delta);
      String itemTarget = this.run_Str();
      String mode = this.tick_Str("useMode");
      String hint = "CUSTOM_HOLD".equals(mode) ? "Hold-uses " + itemTarget + " for the set ticks." : "Uses " + itemTarget + " for the set count.";
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      FieldDef itemFieldDef = this.load_FieldDef_Str("itemName");
      ThemedTextInputBox itemField = this.init_Map.get("itemName");
      ThemedTextInputBox slotField = this.init_Map.get("slot");
      if (itemFieldDef != null && itemField != null && slotField != null) {
         Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
         int labelW = this.init_I_I(w, itemFieldDef.label(), font, 88);
         this.init_V_GuiGraphicsExtract_3(ctx, itemFieldDef.label(), x, y, labelW, font);
         int controlX = this.flush_I(x, labelW);
         int controlW = Math.max(1, this.close(w, labelW));
         int pickW = this.init_I_StreamUtil(StreamUtil.ITEM_SLOT, null, null);
         int slotW = 34;
         int gap = 2;
         int itemW = Math.max(1, controlW - pickW - slotW - gap * 2);
         itemField.read(controlX);
         itemField.write(y + 2);
         itemField.update(itemW);
         itemField.init_V(ctx, mx, my, delta);
         int slotX = controlX + itemW + gap;
         slotField.read(slotX);
         slotField.write(y + 2);
         slotField.update(slotW);
         slotField.init_V(ctx, mx, my, delta);
         int pickX = slotX + slotW + gap;
         boolean capturing = this.init_StreamUtil.init("itemName");
         this.init_ByteReader_GuiGraphicsExtract(ctx, pickX, y + 2, pickW, 14, StreamUtil.ITEM_SLOT, capturing, true, mx, my, () -> this.close_V("itemName"));
         return y + 14 + 2;
      } else {
         int cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemName", x, y, w, mx, my, delta);
         return this.init_I_GuiGraphicsExtract_5(ctx, "slot", x, cy, w, mx, my, delta);
      }
   }

   private void reset(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "yaw", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "pitch", x, cy, w, mx, my, delta);
      this.init_V_GuiGraphicsExtract(ctx, x, cy, Math.min(w, this.write_I("Capture View")), 13, "Capture View", mx, my, this::u);
      cy += 18;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "smooth", x, cy, w, mx, my, delta);
      if (this.load_Map.getOrDefault("smooth", false)) {
         cy = this.flush(ctx, x, cy, w, mx, my);
      } else {
         this.data_Z = false;
         this.i();
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForCompletion", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(
         ctx,
         x,
         cy,
         w,
         this.load_Map.getOrDefault("smooth", false)
            ? "Capture View fills in your current yaw and pitch. Smoothness 1 is faster, 10 is slower."
            : "Capture View fills in your current yaw and pitch."
      );
   }

   private void start(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "targetMode", x, cy, w, mx, my, delta);
      String mode = this.tick_Str("targetMode");
      if ("BLOCK".equals(mode)) {
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "searchRadius", x, cy, w, mx, my, delta);
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "blockIds", x, cy, w, mx, my, delta);
      } else if ("ENTITY".equals(mode)) {
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "searchRadius", x, cy, w, mx, my, delta);
         cy = this.load_I_GuiGraphicsExtract(ctx, x, cy, w, mx, my, delta);
      } else {
         cy = this.init_I_GuiGraphicsExtract_5(ctx, "blockPos", x, cy, w, mx, my, delta);
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "smooth", x, cy, w, mx, my, delta);
      if (this.load_Map.getOrDefault("smooth", false)) {
         cy = this.flush(ctx, x, cy, w, mx, my);
      } else {
         this.data_Z = false;
         this.i();
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForCompletion", x, cy, w, mx, my, delta);
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
      if (this.load_Map.getOrDefault("smooth", false)) {
         hint = hint + " Smoothness 1 is faster, 10 is slower.";
      }

      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
   }

   private int load_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = y;
      List<String> selected = this.write_Map.getOrDefault("entityIds", Collections.emptyList());
      cy = this.init_I_GuiGraphicsExtract_2(
         ctx, x, cy, w, mx, my, "look_at_entity_selected", "Entities", selected, selected::remove, this::reset_Str, "(add entity types or specific captures)"
      );
      ThemedTextInputBox search = this.flush_Map.get("entityIds");
      String modeLabel = this.poll_Z ? "[Spec]" : "[Type]";
      int modeW = Math.max(this.write_I("[Spec]"), this.write_I("[Type]"));
      int capW = this.write_I("CAP");
      int searchW = w - modeW - 2 - capW - 2;
      if (search != null) {
         search.read(x);
         search.write(cy);
         search.update(searchW);
         search.init_V(ctx, mx, my, delta);
      }

      int modeX = x + searchW + 2;
      this.init_V_GuiGraphicsExtract(ctx, modeX, cy, modeW, 13, modeLabel, mx, my, () -> this.poll_Z = !this.poll_Z);
      this.init_V_GuiGraphicsExtract(ctx, modeX + modeW + 2, cy, capW, 13, "CAP", mx, my, this::a);
      cy += 18;
      String filter = search != null ? search.load_Str().trim().toLowerCase() : "";
      List<String> filtered = this.init_List_Str_2("look_at_entity_search", poll_List(), filter, cc::read);
      this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "look_at_entity_search", filtered, 4, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, cc::read);
      cy += 66;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Nearby Entities", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 13;
      List<String> nearbyEntries = this.sync_List();
      this.init_V_GuiGraphicsExtract(ctx, x, cy, w, mx, my, "look_at_entity_nearby", nearbyEntries, 3, value -> {
         String stored = this.poll_Z ? value : this.start(value);
         if (!stored.isBlank()) {
            if (this.poll_Z) {
               boolean removed = selected.removeIf(existing -> existing.startsWith(this.stop(stored)));
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
         String stored = this.poll_Z ? value : this.start(value);
         return this.poll_Z ? selected.stream().anyMatch(existing -> existing.startsWith(this.stop(stored))) : selected.contains(stored);
      }, this::next);
      return cy + 39 + 4;
   }

   private void stop(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      FieldDef posField = this.load_FieldDef_Str("pos");
      if (posField != null) {
         cy = this.init_I_GuiGraphicsExtract_4(ctx, posField, x, cy, w, mx, my, delta);
      }

      this.init_V_GuiGraphicsExtract(ctx, x, cy, Math.min(w, this.write_I("Capture Here")), 13, "Capture Here", mx, my, this::v);
      cy += 18;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForArrival", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Wait for Arrival continues only after navigation finishes.");
   }

   private void next(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "From", font, UiColorPalette.parse(), x, cy + 2, false);
      int flipW = this.write_I("Flip Ends");
      this.init_V_GuiGraphicsExtract(ctx, x + w - flipW, cy, flipW, 13, "Flip Ends", mx, my, this::w);
      cy += 16;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "fromUseItemName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "fromItemName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "fromSlot", x, cy, w, mx, my, delta);
      TableIndex.init(ctx, x, cy + 2, w, UiColorPalette.c());
      cy += 8;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "To", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 16;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "toUseItemName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "toItemName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "toSlot", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.put_Str());
   }

   private void prev(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "clickType", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "clickCount", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "guiName", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.add_Str());
   }

   private void valid(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "delayMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "lagMethod", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "kickMethod", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetCount", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "useNextAction", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "trigger", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "tolerance", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "bufferMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "timeoutSec", x, cy, w, mx, my, delta);
      String mode = this.tick_Str("mode");
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
            var10000 = this.load_Map.getOrDefault("useNextAction", false)
               ? "Kick Dupe will run the next eligible macro actions inside the lag sandwich, then kick."
               : "Kick Dupe will try to use a bundle, then kick.";
            break;
         default:
            var10000 = "Unknown mode";
      }

      String hint = var10000;
      this.init_I_GuiGraphicsExtract_2(ctx, x, cy, w, hint, "DISCONNECT".equals(mode) ? UiColorPalette.bind() : -19605);
   }

   private void size(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "guiType", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "matchMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "guiTitle", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_2(ctx, this.init_Map.get("guiTitle"), x, cy, w, mx, my);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      String mode = this.tick_Str("waitMode");
      String guiType = this.tick_Str("guiType");
      String hint = "CLOSE".equals(mode) ? "Waits until the matching " + guiType + " GUI closes." : "Waits until a matching " + guiType + " GUI opens.";
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
   }

   private void data(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemName", x, cy, w, mx, my, delta);
      int[] slotBtns = this.init_IArr_I(x, w, false, "Held", "Use Slot Only");
      this.init_V_GuiGraphicsExtract(ctx, slotBtns[0], cy, slotBtns[1], 13, "Held", mx, my, this::p);
      this.init_V_GuiGraphicsExtract(ctx, slotBtns[2], cy, slotBtns[3], 13, "Use Slot Only", mx, my, this::q);
      cy += 18;
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Fallback Hotbar Slot", font, UiColorPalette.parse(), x, cy + 2, false);
      cy += 12;
      cy = this.load_I(ctx, x, cy, w, mx, my);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "strategy", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "outputVariable", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.tick_Str());
   }

   private int load_I(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int selectedSlot = this.start_I();
      int slotGap = 2;
      int cellW = Math.max(16, Math.min(24, (w - slotGap * 8) / 9));
      int totalW = cellW * 9 + slotGap * 8;
      int startX = x + Math.max(0, (w - totalW) / 2);

      for (int slot = 0; slot < 9; slot++) {
         int sx = startX + slot * (cellW + slotGap);
         boolean selected = selectedSlot == slot;
         String label = String.valueOf(slot + 1);
         int clickedSlot = slot;
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx,
            sx,
            y,
            cellW,
            13,
            label,
            selected ? ByteReader.Inner_StreamUtil.PRIMARY : ByteReader.Inner_StreamUtil.GHOST,
            true,
            mx,
            my,
            () -> this.put(clickedSlot)
         );
      }

      return y + 18;
   }

   private void value(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemName", x, cy, w, mx, my, delta);
      String handLbl = this.get_Str();
      int[] handBtns = this.init_IArr_I(x, w, false, handLbl, "Capture Held");
      this.init_V_GuiGraphicsExtract(ctx, handBtns[0], cy, handBtns[1], 13, handLbl, mx, my, this::r);
      this.init_V_GuiGraphicsExtract(ctx, handBtns[2], cy, handBtns[3], 13, "Capture Held", mx, my, this::s);
      cy += 18;
      this.init_V_GuiGraphicsExtract(ctx, x, cy, Math.min(w, this.write_I("Use InteractionHand Item")), 13, "Use InteractionHand Item", mx, my, this::t);
      cy += 18;
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, this.set_Str());
   }

   private void cache(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listenDuringPreviousAction", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "condition", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemName", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "count", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "slot", x, cy, w, mx, my, delta);
      if (this.write(this.load_FieldDef_Str("slot"))) {
         int[] slotBtns = this.init_IArr_I(x, w, false, "Selected", "Pick Slot", "Slot 0");
         this.init_V_GuiGraphicsExtract(ctx, slotBtns[0], cy, slotBtns[1], 13, "Selected", mx, my, this::f);
         boolean capturing = this.init_StreamUtil.init("wait_inventory_slot");
         this.init_ByteReader_GuiGraphicsExtract(
            ctx, slotBtns[2], cy, slotBtns[3], 13, StreamUtil.ITEM_SLOT, capturing, true, mx, my, "Pick Slot", "Done", this::g
         );
         this.init_V_GuiGraphicsExtract(ctx, slotBtns[4], cy, slotBtns[5], 13, "Slot 0", mx, my, () -> this.poll(0));
         cy += 18;
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
   }

   private void state(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      this.d();
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "source", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "autofillCommand", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "autofillTimeoutMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "autofillCacheList", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemFilter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "slot", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "itemText", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listSelection", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listPickPosition", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listFilter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listFilterText", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listExcludeText", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "listStripPrefix", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "excludeSelf", x, cy, w, mx, my, delta);
      if ("RECENT_CHAT".equals(this.tick_Str("source"))) {
         cy = this.read_I(ctx, x, cy, w, mx, my);
      } else if ("SCOREBOARD".equals(this.tick_Str("source"))) {
         cy = this.write_I(ctx, x, cy, w, mx, my);
      } else if ("TABLIST".equals(this.tick_Str("source"))) {
         cy = this.update_I(ctx, x, cy, w, mx, my);
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "exampleText", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "selectedText", x, cy, w, mx, my, delta);
      autismclient.util.macro.CaptureValueAction draft = this.init_TaskQueue();
      String primaryName = this.init_Str_TaskQueue(draft);
      String token = primaryName.isBlank() ? "" : "{" + primaryName + "}";
      boolean exampleCapture = draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.RECENT_CHAT
         || draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.GUI_TITLE
         || draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.SCOREBOARD;
      autismclient.util.macro.CaptureValueAction.Inner_NetHelper preview = exampleCapture && !draft.exampleText.isBlank()
         ? draft.init_TaskQueue_NetHelpe()
         : draft.load_TaskQueue_NetHelpe_Minecraft(init_Minecraft);
      String previewText = preview.init_Z() ? preview.init_Str_Str(primaryName) : preview.load();
      if (preview.init_Z() && previewText.isBlank()) {
         previewText = preview.init_Str();
      }

      boolean derivingPattern = exampleCapture && !draft.exampleText.isBlank() && draft.matchMode != PatternCaptureMatcher.Inner_DataUtil.MATCH;
      if (derivingPattern && draft.selectedText.isBlank()) {
         previewText = "Set Capture This";
      } else if (derivingPattern && draft.pattern.isBlank()) {
         previewText = "Not in example";
      }

      cy = this.init_I_GuiGraphicsExtract(ctx, "Use Later", "_capture_token", token, x, cy, w, mx, my, "Variable copied.");
      cy = this.init_I_GuiGraphicsExtract(ctx, "Preview Now", "_capture_preview", previewText, x, cy, w, mx, my, "Preview copied.");
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "numberMode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "numberModifier", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "numberModifierAmount", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForTrigger", x, cy, w, mx, my, delta);
      String behaviorHint;
      if (draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL) {
         behaviorHint = "Asks server when run";
      } else if (draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.TABLIST) {
         behaviorHint = draft.waitForTrigger ? "Waits for player" : "Picks online player";
      } else {
         behaviorHint = draft.waitForTrigger
            ? (draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.SCOREBOARD ? "Waits for row change" : "Waits for next match")
            : (
               draft.source != autismclient.util.macro.CaptureValueAction.Inner_StateCache.RECENT_CHAT
                     && draft.source != autismclient.util.macro.CaptureValueAction.Inner_StateCache.GUI_TITLE
                  ? "Reads value now"
                  : "Uses newest match"
            );
      }

      if (!draft.saveAs.isBlank() && MacroVariableStore.load(draft.saveAs).isBlank()) {
         behaviorHint = "Bad variable name";
      }

      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, behaviorHint);
   }

   private int read_I(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      List<MacroExecutor.Inner_PacketUtil> history = MacroExecutor.load_List().stream().limit(6L).toList();
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Recent Chat", font, UiColorPalette.parse(), x, y + 2, false);
      y += 13;
      int listH = this.update_I(history.size());
      this.init_V_GuiGraphicsExtract(ctx, x, y, w, listH, mx, my, history, "capture_value_recent", 3, this::init_V_MacroExecutor_Pack);
      return y + listH + 2;
   }

   private void init_V_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil entry) {
      if (entry != null) {
         String text = this.init_Str_MacroExecutor_Pack(entry);
         String suggested = autismclient.util.macro.CaptureValueAction.init_Str_Str_5(text);
         ThemedTextInputBox exampleField = this.init_Map.get("exampleText");
         ThemedTextInputBox selectedField = this.init_Map.get("selectedText");
         this.push_Z = true;
         if (exampleField != null) {
            exampleField.init_V_Str(text);
         }

         if (selectedField != null) {
            selectedField.init_V_Str(suggested);
         }

         this.push_Z = false;
         this.load_Str = this.update("pattern", "");
         this.e();
      }
   }

   private int write_I(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      List<autismclient.util.macro.CaptureValueAction.Inner_JsonUtil> lines = autismclient.util.macro.CaptureValueAction.init_List_Minecraft_2(init_Minecraft);
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      String title = !lines.isEmpty() && !lines.get(0).read().isBlank() ? "Scoreboard - " + lines.get(0).read() : "Scoreboard";
      autismclient.gui.f.b.k.init(ctx, this.init_Font, autismclient.gui.f.b.k.init(this.init_Font, title, w, font, -1), font, UiColorPalette.parse(), x, y + 2, false);
      y += 13;
      int listH = lines.isEmpty() ? 24 : Math.min(5, lines.size()) * 22;
      int itemW = Math.max(1, w - 5 - 1);
      EventBus viewport = this.init_EventBus_Map(this.tick_Map, "capture_value_scoreboard", x, y, w, listH, 22, 5);
      viewport.init_V(lines.size() * 22);
      viewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (lines.isEmpty()) {
         TableIndex.init(ctx, x, y, itemW, 24, false, false);
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "No scoreboard", font, UiColorPalette.bind(), x + 3, y + 7, false);
         return y + 24 + 2;
      }

      String selectedKey = this.update("scoreboardRow", "");
      viewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
      int first = viewport.pop();

      for (int i = first; i < lines.size() && i <= viewport.tick(); i++) {
         int rowY = viewport.init_I(i);
         if (rowY != -2147483648) {
            autismclient.util.macro.CaptureValueAction.Inner_JsonUtil line = lines.get(i);
            boolean selected = line.init_Str().equals(selectedKey);
            boolean hovered = mx >= x && mx < x + itemW && my >= rowY && my < rowY + 22;
            TableIndex.init(ctx, x, rowY, itemW, 22, hovered, selected);
            TableIndex.read(ctx, x, rowY + 2, 3, 18, selected ? -13248397 : -3376606);
            String rowLabel = "Row " + (line.init_I() + 1);
            autismclient.gui.f.b.k.init(ctx, this.init_Font, rowLabel, font, UiColorPalette.bind(), x + 6, rowY + 2, false);
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               autismclient.gui.f.b.k.init(this.init_Font, line.flush(), Math.max(20, itemW - 10), font, -1),
               font,
               UiColorPalette.scan(),
               x + 6,
               rowY + 11,
               false
            );
            autismclient.util.macro.CaptureValueAction.Inner_JsonUtil selectedLine = line;
            this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, rowY, itemW, 22, () -> this.init_V_TaskQueue_JsonUtil(selectedLine)));
         }
      }

      viewport.init(ctx);
      return y + listH + 2;
   }

   private List<String> write_List() {
      long now = System.currentTimeMillis();
      if (this.value_List == null || now - this.update_J > 500L) {
         List<bO.Inner_StreamUtil> players = bO.init(init_Minecraft);
         List<String> names = new ArrayList<>(players.size());

         for (bO.Inner_StreamUtil player : players) {
            names.add(player.init_Str());
         }

         this.value_List = names;
         this.update_J = now;
      }

      return this.value_List;
   }

   private List<String> init_List_TaskQueue(autismclient.util.macro.CaptureValueAction draft) {
      List<String> names = new ArrayList<>(this.write_List());
      if (!draft.excludeSelf && init_Minecraft.player != null && init_Minecraft.player.getGameProfile() != null) {
         names.add(init_Minecraft.player.getGameProfile().name());
         names.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return names;
   }

   private int set_I() {
      autismclient.util.macro.CaptureValueAction draft = this.init_TaskQueue();
      return draft.init_List_List(this.init_List_TaskQueue(draft)).size();
   }

   private int update_I(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      autismclient.util.macro.CaptureValueAction draft = this.init_TaskQueue();
      List<String> names = this.init_List_TaskQueue(draft);
      List<String> pool = draft.init_List_List(names);
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Matches (" + pool.size() + " of " + names.size() + ")", font, UiColorPalette.parse(), x, y + 2, false);
      y += 13;
      int listH = pool.isEmpty() ? 24 : Math.min(5, pool.size()) * 14;
      int itemW = Math.max(1, w - 5 - 1);
      EventBus viewport = this.init_EventBus_Map(this.tick_Map, "capture_value_tablist", x, y, w, listH, 14, 5);
      viewport.init_V(pool.size() * 14);
      viewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (pool.isEmpty()) {
         TableIndex.init(ctx, x, y, itemW, 24, false, false);
         autismclient.gui.f.b.k.init(ctx, this.init_Font, "No matches", font, UiColorPalette.bind(), x + 3, y + 7, false);
         return y + 24 + 2;
      }

      viewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
      int first = viewport.pop();

      for (int i = first; i < pool.size() && i <= viewport.tick(); i++) {
         int rowY = viewport.init_I(i);
         if (rowY != -2147483648) {
            String name = pool.get(i);
            boolean hovered = mx >= x && mx < x + itemW && my >= rowY && my < rowY + 14;
            TableIndex.init(ctx, x, rowY, itemW, 14, hovered, false);
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               autismclient.gui.f.b.k.init(this.init_Font, name, Math.max(20, itemW - 10), font, -1),
               font,
               UiColorPalette.scan(),
               x + 6,
               rowY + 3,
               false
            );
            this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, rowY, itemW, 14, () -> this.write_V_Str_Str(name, "Name copied.")));
         }
      }

      viewport.init(ctx);
      return y + listH + 2;
   }

   private void init_V_TaskQueue_JsonUtil(autismclient.util.macro.CaptureValueAction.Inner_JsonUtil line) {
      if (line != null) {
         ThemedTextInputBox rowField = this.init_Map.get("scoreboardRow");
         ThemedTextInputBox rowIndexField = this.init_Map.get("scoreboardRowIndex");
         ThemedTextInputBox objectiveField = this.init_Map.get("scoreboardObjective");
         ThemedTextInputBox exampleField = this.init_Map.get("exampleText");
         ThemedTextInputBox selectedField = this.init_Map.get("selectedText");
         String suggested = autismclient.util.macro.CaptureValueAction.init_Str_Str_5(line.flush());
         this.push_Z = true;
         if (rowField != null) {
            rowField.init_V_Str(line.init_Str());
         }

         if (rowIndexField != null) {
            rowIndexField.init_V_Str(Integer.toString(line.init_I()));
         }

         if (objectiveField != null) {
            objectiveField.init_V_Str(line.load());
         }

         if (exampleField != null) {
            exampleField.init_V_Str(line.flush());
         }

         if (selectedField != null) {
            selectedField.init_V_Str(suggested);
         }

         this.push_Z = false;
         this.load_Str = this.update("pattern", "");
         this.e();
      }
   }

   private String load_Str_FieldDef(FieldDef field) {
      if ("waitForTrigger".equals(field.key()) && this.init_MacroAction instanceof autismclient.util.macro.CaptureValueAction) {
         String var10000;
         switch (this.tick_Str("source") == null ? "" : this.tick_Str("source")) {
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

   private void d() {
      String source = this.tick_Str("source");
      if (source != null && !source.equals(this.read_Str)) {
         boolean firstFrame = this.read_Str.isEmpty();
         this.read_Str = source;
         if (!firstFrame && !this.pop_Z) {
            autismclient.util.macro.CaptureValueAction.Inner_StateCache parsed;
            try {
               parsed = autismclient.util.macro.CaptureValueAction.Inner_StateCache.valueOf(source);
            } catch (IllegalArgumentException ignored) {
               return;
            }

            boolean waits = autismclient.util.macro.CaptureValueAction.init_Z_TaskQueue_StateCac(parsed);
            this.load_Map.put("waitForTrigger", waits);
            if (this.init_CompoundTag != null) {
               this.init_CompoundTag.putBoolean("waitForTrigger", waits);
            }
         }
      }
   }

   private void e() {
      if (!this.push_Z && this.init_MacroAction instanceof autismclient.util.macro.CaptureValueAction) {
         String source = this.tick_Str("source");
         if ("GUI_TITLE".equals(source) || "RECENT_CHAT".equals(source) || "SCOREBOARD".equals(source)) {
            String example = this.update("exampleText", "");
            String selected = this.update("selectedText", "");
            String variable = this.update("saveAs", "value");
            if (this.init_CompoundTag != null) {
               this.init_CompoundTag.putString("exampleText", example);
               this.init_CompoundTag.putString("selectedText", selected);
            }

            ThemedTextInputBox patternField = this.init_Map.get("pattern");
            String current = patternField != null
               ? patternField.load_Str()
               : (this.init_CompoundTag == null ? "" : this.init_CompoundTag.getStringOr("pattern", ""));
            if (current.isBlank() || current.equals(this.load_Str)) {
               if (!example.isBlank() && !selected.isBlank()) {
                  String generated = autismclient.util.macro.CaptureValueAction.init(example, selected, variable);
                  if (!generated.isBlank()) {
                     if (patternField != null) {
                        patternField.init_V_Str(generated);
                     }

                     if (this.init_CompoundTag != null) {
                        this.init_CompoundTag.putString("pattern", generated);
                     }

                     this.load_Str = generated;
                     if (!generated.isBlank()) {
                        this.read_Map.put("matchMode", 1);
                        if (this.init_CompoundTag != null) {
                           this.init_CompoundTag.putString("matchMode", "CAPTURE");
                        }
                     }
                  }
               } else {
                  if (patternField != null) {
                     patternField.init_V_Str("");
                  }

                  if (this.init_CompoundTag != null) {
                     this.init_CompoundTag.putString("pattern", "");
                  }

                  this.load_Str = "";
               }
            }
         }
      }
   }

   private int update_I(int count) {
      return count <= 0 ? 24 : Math.min(3, Math.min(6, count)) * 34;
   }

   private void init_V_I_NetHelper(int fromIndex, autismclient.gui.f.NetHelper clip) {
      if (clip != null) {
         for (int i = this.valid_List.size() - 1; i >= Math.max(0, fromIndex); i--) {
            ActionEditorOverlay.Inner_StreamUtil region = this.valid_List.get(i);
            int left = Math.max(region.init_I, clip.read());
            int top = Math.max(region.load, clip.write());
            int right = Math.min(region.init_I + region.read, clip.init());
            int bottom = Math.min(region.load + region.write, clip.load());
            if (right <= left || bottom <= top) {
               this.valid_List.remove(i);
            } else if (left != region.init_I || top != region.load || right != region.init_I + region.read || bottom != region.load + region.write) {
               this.valid_List.set(i, new ActionEditorOverlay.Inner_StreamUtil(left, top, right - left, bottom - top, region.init_ActionEditorOverla));
            }
         }
      }
   }

   private int init_I_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx, String label, String fieldKey, String value, int x, int y, int w, int mx, int my, String copiedMessage
   ) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(ctx, this.init_Font, label, font, UiColorPalette.parse(), x, y + 2, false);
      y += 12;
      int buttonW = this.write_I("Copy");
      ThemedTextInputBox field = this.flush_Map.get(fieldKey);
      if (field != null) {
         String safe = value == null ? "" : value;
         if (!safe.equals(field.load_Str())) {
            field.init_V_Str(safe);
         }

         field.read(x);
         field.write(y);
         field.update(Math.max(1, w - buttonW - 4));
         field.sync(13);
         field.init_V(ctx, mx, my, this.init_F_2);
      }

      String copied = value == null ? "" : value;
      this.init_V_GuiGraphicsExtract(ctx, x + w - buttonW, y, buttonW, 13, "Copy", mx, my, () -> this.write_V_Str_Str(copied, copiedMessage));
      return y + 18;
   }

   private void write_V_Str_Str(String value, String message) {
      if (value != null && !value.isBlank()) {
         init_Minecraft.keyboardHandler.setClipboard(value);
         bi.init(message);
      } else {
         bi.read("Nothing to copy.");
      }
   }

   private autismclient.util.macro.CaptureValueAction init_TaskQueue() {
      autismclient.util.macro.CaptureValueAction draft = new autismclient.util.macro.CaptureValueAction();

      try {
         draft.source = autismclient.util.macro.CaptureValueAction.Inner_StateCache.valueOf(this.tick_Str("source"));
      } catch (IllegalArgumentException | NullPointerException var13) {
      }

      try {
         draft.matchMode = PatternCaptureMatcher.Inner_DataUtil.valueOf(this.tick_Str("matchMode"));
      } catch (IllegalArgumentException | NullPointerException var12) {
      }

      try {
         draft.itemText = autismclient.util.macro.CaptureValueAction.Inner_CachePool.valueOf(this.tick_Str("itemText"));
      } catch (IllegalArgumentException | NullPointerException var11) {
      }

      draft.saveAs = this.update("saveAs", "value");
      draft.pattern = this.update("pattern", "");
      draft.exampleText = this.update("exampleText", "");
      draft.selectedText = this.update("selectedText", "");
      draft.itemFilter = this.update("itemFilter", "");
      draft.scoreboardRow = this.update("scoreboardRow", "");
      draft.scoreboardObjective = this.update("scoreboardObjective", "");

      try {
         draft.scoreboardRowIndex = Integer.parseInt(this.update("scoreboardRowIndex", "-1").trim());
      } catch (NumberFormatException ignored) {
         draft.scoreboardRowIndex = -1;
      }

      try {
         draft.numberMode = autismclient.util.macro.CaptureValueAction.Inner_BufferHelper.valueOf(this.tick_Str("numberMode"));
      } catch (IllegalArgumentException | NullPointerException var9) {
      }

      try {
         draft.numberModifier = autismclient.util.macro.CaptureValueAction.Inner_TaskQueue.valueOf(this.tick_Str("numberModifier"));
      } catch (IllegalArgumentException | NullPointerException var8) {
      }

      try {
         draft.numberModifierAmount = Double.parseDouble(this.update("numberModifierAmount", "1").trim());
      } catch (NumberFormatException ignored) {
         draft.numberModifierAmount = this.init_CompoundTag == null ? 1.0 : this.init_CompoundTag.getDoubleOr("numberModifierAmount", 1.0);
      }

      draft.waitForTrigger = this.load_Map
         .getOrDefault("waitForTrigger", this.init_CompoundTag == null || this.init_CompoundTag.getBooleanOr("waitForTrigger", true));

      try {
         draft.slot = Integer.parseInt(this.update("slot", "-1").trim());
      } catch (NumberFormatException ignored) {
         draft.slot = -1;
      }

      draft.autofillCommand = this.update("autofillCommand", "");

      try {
         draft.autofillTimeoutMs = Integer.parseInt(this.update("autofillTimeoutMs", "5000").trim());
      } catch (NumberFormatException ignored) {
         draft.autofillTimeoutMs = 5000;
      }

      draft.autofillCacheList = this.load_Map
         .getOrDefault("autofillCacheList", this.init_CompoundTag != null && this.init_CompoundTag.getBooleanOr("autofillCacheList", false));

      try {
         draft.listSelection = autismclient.util.macro.CandidatePoolSelector.Inner_CachePool.valueOf(this.tick_Str("listSelection"));
      } catch (IllegalArgumentException | NullPointerException var5) {
      }

      try {
         draft.listFilter = autismclient.util.macro.CandidatePoolSelector.Inner_DataUtil.valueOf(this.tick_Str("listFilter"));
      } catch (IllegalArgumentException | NullPointerException var4) {
      }

      draft.listFilterText = this.update("listFilterText", "");
      draft.listExcludeText = this.update("listExcludeText", "");

      try {
         draft.listPickPosition = Integer.parseInt(this.update("listPickPosition", "1").trim());
      } catch (NumberFormatException ignored) {
         draft.listPickPosition = 1;
      }

      draft.listStripPrefix = this.load_Map
         .getOrDefault("listStripPrefix", this.init_CompoundTag != null && this.init_CompoundTag.getBooleanOr("listStripPrefix", false));
      draft.excludeSelf = this.load_Map.getOrDefault("excludeSelf", this.init_CompoundTag == null || this.init_CompoundTag.getBooleanOr("excludeSelf", true));
      return draft;
   }

   private String init_Str_TaskQueue(autismclient.util.macro.CaptureValueAction draft) {
      if (draft == null) {
         return "";
      }

      boolean listSource = draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL
         || draft.source == autismclient.util.macro.CaptureValueAction.Inner_StateCache.TABLIST;
      if (!listSource) {
         Set<String> captured = PatternCaptureMatcher.init(draft.matchMode, draft.pattern);
         if (!captured.isEmpty()) {
            return captured.iterator().next();
         }
      }

      return MacroVariableStore.load(draft.saveAs);
   }

   private String update(String key, String fallback) {
      ThemedTextInputBox field = this.init_Map.get(key);
      return field == null ? fallback : field.load_Str();
   }

   private void f() {
      if (init_Minecraft.player != null) {
         this.poll(init_Minecraft.player.getInventory().getSelectedSlot());
      }
   }

   private void poll(int slot) {
      ThemedTextInputBox field = this.init_Map.get("slot");
      if (field != null) {
         field.init_V_Str(String.valueOf(Math.max(0, slot)));
      }
   }

   private void g() {
      this.close_V("wait_inventory_slot");
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int bodyTop, int contentBottom, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "pattern", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_2(ctx, this.init_Map.get("pattern"), x, cy, w, mx, my);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "matchMode", x, cy, w, mx, my, delta);
      String matchMode = this.tick_Str("matchMode");
      if ("MATCH".equals(matchMode)) {
         cy = this.sync_I(ctx, x, cy, w, mx, my);
      } else {
         this.size_Z = false;
         this.h();
      }

      cy = this.init_I_GuiGraphicsExtract_5(ctx, "saveAs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "timeoutMs", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiBefore", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitForGuiAfter", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "waitGuiName", x, cy, w, mx, my, delta);
      String pattern = this.init_Map.containsKey("pattern") ? this.init_Map.get("pattern").load_Str().trim() : "";
      int fuzzyPercent = this.put_I();
      String hint = pattern.isEmpty()
         ? "Blank pattern = any chat line."
         : (
            "REGEX".equals(matchMode)
               ? "Named groups save values."
               : ("CAPTURE".equals(matchMode) ? "Use {name} to capture." : "Fuzzy match: " + fuzzyPercent + "%.")
         );
      cy = this.init_I_GuiGraphicsExtract(ctx, x, cy, w, hint);
      ThemedTextInputBox searchField = this.flush_Map.get("_wait_chat_search");
      if (searchField != null) {
         searchField.read(x);
         searchField.write(cy);
         searchField.update(w);
         searchField.init_V(ctx, mx, my, delta);
         cy += 18;
      }

      List<MacroExecutor.Inner_PacketUtil> history = this.update_List();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Recent Messages (" + history.size() + ")", font, UiColorPalette.parse(), x, cy + 2, false);
      autismclient.gui.f.b.k.init(
         ctx,
         this.init_Font,
         autismclient.gui.f.b.k.init(this.init_Font, this.write_Str("Click a row to use that message."), Math.max(20, w - 122), font, -1),
         font,
         UiColorPalette.bind(),
         x + 118,
         cy + 2,
         false
      );
      cy += 13;
      int availableListHeight = Math.max(24, contentBottom - cy - 4);
      this.init_V_GuiGraphicsExtract_2(ctx, x, cy, w, availableListHeight, mx, my, history);
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, ThemedTextInputBox patternField, int x, int y, int w, int mx, int my) {
      ThemedTextInputBox nameField = this.flush_Map.get("_capture_name");
      if (nameField != null && patternField != null) {
         int buttonW = this.write_I("Capture");
         nameField.read(x);
         nameField.write(y + 1);
         nameField.update(Math.max(1, w - buttonW - 3));
         nameField.sync(13);
         nameField.init_V(ctx, mx, my, 0.0F);
         this.init_V_GuiGraphicsExtract(ctx, x + w - buttonW, y + 1, buttonW, 13, "Capture", mx, my, () -> {
            String name = MacroVariableStore.load(nameField.load_Str());
            if (!name.isBlank()) {
               patternField.read("{" + name + "}");
               this.read_Map.put("matchMode", 1);
               this.init_CompoundTag.putString("matchMode", "CAPTURE");
               this.init_CompoundTag.putString("patternJson", "");
            }
         });
         return y + 18;
      } else {
         return y;
      }
   }

   private int sync_I(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int percent = this.put_I();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Match Strength", font, UiColorPalette.parse(), x, y + 2, false);
      String label = percent + "%";
      autismclient.gui.f.b.k.init(ctx, this.init_Font, label, font, UiColorPalette.bind(), x + w - this.init_I_Identifier_Str(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.f_n_I = x;
      this.f_o_I = y;
      this.f_p_I = sliderW;
      this.f_q_I = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.c());
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
      boolean hovered = this.load_Z_I_I(mx, my);
      int knobFill = this.size_Z ? -14202259 : (hovered ? -14007198 : -14533802);
      int knobBorder = this.size_Z ? -5908993 : -7816193;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int put_I() {
      ThemedTextInputBox field = this.init_Map.get("fuzzyPercent");
      if (field == null) {
         return 100;
      }

      try {
         return WaitForChatAction.init_I_I_8(Integer.parseInt(field.load_Str().trim()));
      } catch (NumberFormatException ignored) {
         return 100;
      }
   }

   private void push(int percent) {
      ThemedTextInputBox field = this.init_Map.get("fuzzyPercent");
      if (field != null) {
         field.init_V_Str(String.valueOf(WaitForChatAction.init_I_I_8(percent)));
      }
   }

   private void h() {
      this.f_n_I = -1;
      this.f_o_I = -1;
      this.f_p_I = 0;
      this.f_q_I = 0;
   }

   private boolean load_Z_I_I(int mx, int my) {
      return this.f_p_I > 0 && this.f_q_I > 0 && mx >= this.f_n_I && mx < this.f_n_I + this.f_p_I && my >= this.f_o_I && my < this.f_o_I + this.f_q_I;
   }

   private void pop(int mouseX) {
      if (this.f_p_I > 1) {
         float normalized = (float)(mouseX - this.f_n_I) / (this.f_p_I - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 6.0F);
         this.push(40 + stepIndex * 10);
      }
   }

   private int flush(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int smoothness = this.add_I();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Smoothness", font, UiColorPalette.parse(), x, y + 2, false);
      String label = smoothness + " / 10";
      autismclient.gui.f.b.k.init(ctx, this.init_Font, label, font, UiColorPalette.bind(), x + w - this.init_I_Identifier_Str(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.f_r_I = x;
      this.f_s_I = y;
      this.f_t_I = sliderW;
      this.f_u_I = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.c());
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
      boolean hovered = this.read_Z_I_I(mx, my);
      int knobFill = this.data_Z ? -14202259 : (hovered ? -14007198 : -14533802);
      int knobBorder = this.data_Z ? -5908993 : -7816193;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int add_I() {
      ThemedTextInputBox field = this.init_Map.get("smoothness");
      if (field == null) {
         return 9;
      }

      try {
         return RotateAction.init_I(Integer.parseInt(field.load_Str().trim()));
      } catch (NumberFormatException ignored) {
         return 9;
      }
   }

   private void tick(int smoothness) {
      ThemedTextInputBox field = this.init_Map.get("smoothness");
      if (field != null) {
         field.init_V_Str(String.valueOf(RotateAction.init_I(smoothness)));
      }
   }

   private void i() {
      this.f_r_I = -1;
      this.f_s_I = -1;
      this.f_t_I = 0;
      this.f_u_I = 0;
   }

   private boolean read_Z_I_I(int mx, int my) {
      return this.f_t_I > 0 && this.f_u_I > 0 && mx >= this.f_r_I && mx < this.f_r_I + this.f_t_I && my >= this.f_s_I && my < this.f_s_I + this.f_u_I;
   }

   private void run(int mouseX) {
      if (this.f_t_I > 1) {
         float normalized = (float)(mouseX - this.f_r_I) / (this.f_t_I - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 9.0F);
         this.tick(1 + stepIndex);
      }
   }

   private int close(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int delay = this.clear_I();
      autismclient.gui.f.b.k.init(ctx, this.init_Font, "Safe Delay", font, UiColorPalette.parse(), x, y + 2, false);
      String label = delay + "t";
      autismclient.gui.f.b.k.init(ctx, this.init_Font, label, font, UiColorPalette.bind(), x + w - this.init_I_Identifier_Str(font, label), y + 2, false);
      y += 13;
      int sliderW = w;
      int sliderH = 14;
      int trackH = 4;
      int trackY = y + (sliderH - trackH) / 2;
      this.f_v_I = x;
      this.f_w_I = y;
      this.f_x_I = sliderW;
      this.f_y_I = sliderH;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x, y, sliderW, sliderH), -15921896, UiColorPalette.c());
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
      boolean hovered = this.write_Z_I_I(mx, my);
      int knobFill = this.value_Z ? -10738652 : (hovered ? -11787228 : -13296869);
      int knobBorder = this.value_Z ? -20304 : -34953;
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(knobX, y, knobW, sliderH), knobFill, knobBorder);
      return y + sliderH + 2;
   }

   private int clear_I() {
      ThemedTextInputBox field = this.init_Map.get("safeClickDelayTicks");
      if (field == null) {
         return 1;
      }

      try {
         return XCarryAction.write(Integer.parseInt(field.load_Str().trim()));
      } catch (NumberFormatException ignored) {
         return 1;
      }
   }

   private void get(int ticks) {
      ThemedTextInputBox field = this.init_Map.get("safeClickDelayTicks");
      if (field != null) {
         field.init_V_Str(String.valueOf(XCarryAction.write(ticks)));
      }
   }

   private void j() {
      this.f_v_I = -1;
      this.f_w_I = -1;
      this.f_x_I = 0;
      this.f_y_I = 0;
   }

   private boolean write_Z_I_I(int mx, int my) {
      return this.f_x_I > 0 && this.f_y_I > 0 && mx >= this.f_v_I && mx < this.f_v_I + this.f_x_I && my >= this.f_w_I && my < this.f_w_I + this.f_y_I;
   }

   private void set(int mouseX) {
      if (this.f_x_I > 1) {
         float normalized = (float)(mouseX - this.f_v_I) / (this.f_x_I - 1);
         normalized = Math.max(0.0F, Math.min(1.0F, normalized));
         int stepIndex = Math.round(normalized * 10.0F);
         this.get(stepIndex);
      }
   }

   private void load_V_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil entry) {
      ThemedTextInputBox field = this.init_Map.get("pattern");
      if (field != null && entry != null) {
         Component patternComponent = entry.init_Component() != null
            ? entry.init_Component().copy()
            : Component.literal(this.init_Str_MacroExecutor_Pack(entry));
         this.init_V_k_Component(field, patternComponent);
         this.read_Map.put("matchMode", 0);
         this.init_CompoundTag.putString("matchMode", "MATCH");
      }
   }

   private void init_V_k_Component(ThemedTextInputBox field, Component component) {
      if (field != null) {
         Component safeComponent = component != null ? component.copy() : Component.empty();
         String visibleText = safeComponent.getString();
         this.init_CompoundTag.putString("pattern", visibleText);
         this.init_CompoundTag.putString("patternJson", MacroExecutor.init_Str_Component(safeComponent));
         this.cache_Z = true;
         field.init_V_Str(visibleText);
         this.cache_Z = false;
      }
   }

   private Component update_Component(String fallbackValue) {
      Component exact = MacroExecutor.init_Component_Str(this.init_CompoundTag != null ? this.init_CompoundTag.getStringOr("patternJson", "") : "");
      return exact != null ? exact.copy() : Component.literal(fallbackValue == null ? "" : fallbackValue);
   }

   private Component init_Component_Component_Str(Component previousComponent, String editedValue) {
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

      List<Style> previousStyles = this.init_List_Component_I(previousComponent, previousValue.length());
      if (previousStyles.isEmpty()) {
         return Component.literal(safeValue);
      }

      int prefix = this.init_I_Str_Str(previousValue, safeValue);
      int suffix = this.init_I_Str_2(previousValue, safeValue, prefix);
      int previousLength = previousValue.length();
      int nextLength = safeValue.length();
      MutableComponent rebuilt = Component.empty();
      StringBuilder segment = new StringBuilder();
      Style segmentStyle = null;

      for (int i = 0; i < nextLength; i++) {
         Style style = this.init_Style_List(previousStyles, previousLength, nextLength, prefix, suffix, i);
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

   private List<Style> init_List_Component_I(Component text, int expectedLength) {
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

   private int init_I_Str_Str(String left, String right) {
      int max = Math.min(left.length(), right.length());
      int index = 0;

      while (index < max && left.charAt(index) == right.charAt(index)) {
         index++;
      }

      return index;
   }

   private int init_I_Str_2(String previous, String next, int prefixLength) {
      int previousRemaining = previous.length() - prefixLength;
      int nextRemaining = next.length() - prefixLength;
      int max = Math.min(previousRemaining, nextRemaining);
      int suffix = 0;

      while (suffix < max && previous.charAt(previous.length() - 1 - suffix) == next.charAt(next.length() - 1 - suffix)) {
         suffix++;
      }

      return suffix;
   }

   private Style init_Style_List(List<Style> previousStyles, int previousLength, int nextLength, int prefixLength, int suffixLength, int nextIndex) {
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

   private List<MacroExecutor.Inner_PacketUtil> update_List() {
      List<MacroExecutor.Inner_PacketUtil> history = MacroExecutor.load_List();
      ThemedTextInputBox searchField = this.flush_Map.get("_wait_chat_search");
      String query = searchField != null ? searchField.load_Str().trim() : "";
      this.update_List.clear();
      String normalizedQuery = query.isEmpty() ? "" : this.put_Str(query);

      for (MacroExecutor.Inner_PacketUtil entry : history) {
         if (normalizedQuery.isEmpty()) {
            this.update_List.add(entry);
         } else {
            String haystack = this.put_Str(this.init_Str_MacroExecutor_Pack(entry));
            if (haystack.contains(normalizedQuery)) {
               this.update_List.add(entry);
            }
         }
      }

      return this.update_List;
   }

   private String put_Str(String text) {
      return MacroExecutor.load_Str_Str(text);
   }

   private String init_Str_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil entry) {
      if (entry == null) {
         return "";
      }

      if (entry.init_Component() != null) {
         String rendered = entry.init_Component().getString();
         if (rendered != null && !rendered.isBlank()) {
            return rendered;
         }
      }

      String sender = entry.init_Str() == null ? "" : entry.init_Str().trim();
      String message = entry.load() == null ? "" : entry.load().trim();
      if (!sender.isEmpty() && !message.isEmpty()) {
         return sender + ": " + message;
      } else if (!message.isEmpty()) {
         return (entry.init_MacroExecutor_Task() == MacroExecutor.Inner_TaskQueue.SERVER ? "[Server] " : "[Player] ") + message;
      } else {
         return entry.read() == null ? "" : entry.read();
      }
   }

   private void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor ctx, int x, int y, int w, int listH, int mx, int my, List<MacroExecutor.Inner_PacketUtil> values
   ) {
      this.init_V_GuiGraphicsExtract(ctx, x, y, w, listH, mx, my, values, "wait_chat_recent", 4, this::load_V_MacroExecutor_Pack);
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx,
      int x,
      int y,
      int w,
      int listH,
      int mx,
      int my,
      List<MacroExecutor.Inner_PacketUtil> values,
      String viewportKey,
      int visibleRows,
      Consumer<MacroExecutor.Inner_PacketUtil> onSelect
   ) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int desiredListH = values.isEmpty() ? 24 : Math.min(Math.max(1, visibleRows), values.size()) * 34;
      listH = Math.max(24, Math.min(listH, desiredListH));
      int itemW = w - 5 - 1;
      EventBus chatViewport = this.init_EventBus_Map(this.tick_Map, viewportKey, x, y, w, listH, 34, 5);
      chatViewport.init_V(values.size() * 34);
      chatViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (values.isEmpty()) {
         TableIndex.init(ctx, x, y, itemW, 24, false, false);
         String firstLine = "capture_value_recent".equals(viewportKey) ? "No recent chat yet." : "No recent messages matched your search.";
         String secondLine = "capture_value_recent".equals(viewportKey) ? "Send or receive a message first." : "Send or receive chat first, then pick it here.";
         autismclient.gui.f.b.k.init(ctx, this.init_Font, firstLine, font, UiColorPalette.bind(), x + 3, y + 3, false);
         autismclient.gui.f.b.k.init(ctx, this.init_Font, secondLine, font, UiColorPalette.bind(), x + 3, y + 13, false);
      } else {
         chatViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int first = chatViewport.pop();

         for (int i = first; i < values.size() && i <= chatViewport.tick(); i++) {
            int iy = chatViewport.init_I(i);
            if (iy != -2147483648) {
               int itemH = 34;
               if (iy + itemH > y + listH) {
                  itemH = Math.max(0, y + listH - iy);
               }

               if (itemH > 0) {
                  MacroExecutor.Inner_PacketUtil value = values.get(i);
                  boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + itemH;
                  TableIndex.init(ctx, x, iy, itemW, itemH, hovered, false);
                  int border = value.init_MacroExecutor_Task() == MacroExecutor.Inner_TaskQueue.SERVER
                     ? (hovered ? -20134 : -3376606)
                     : (hovered ? -8597761 : -11627064);
                  this.sync_V(ctx, x, iy, itemW, itemH, border);
                  List<FormattedCharSequence> wrapped = this.init_List_MacroExecutor_Pack(value, Math.max(20, itemW - 6), 3);
                  int textY = iy + 3;
                  if (wrapped.isEmpty()) {
                     autismclient.gui.f.b.k.init(ctx, this.init_Font, "(empty)", font, UiColorPalette.bind(), x + 3, textY, false);
                  } else {
                     for (FormattedCharSequence line : wrapped) {
                        if (textY + 8 > iy + itemH) {
                           break;
                        }

                        ctx.text(this.init_Font, line, x + 3, textY, -1, false);
                        textY += 9;
                     }
                  }

                  MacroExecutor.Inner_PacketUtil selected = value;
                  this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, itemW, itemH, () -> {
                     if (onSelect != null) {
                        onSelect.accept(selected);
                     }
                  }));
               }
            }
         }

         chatViewport.init(ctx);
      }
   }

   private int reset_I() {
      int count = this.update_List().size();
      return count <= 0 ? 24 : Math.min(4, count) * 34;
   }

   private List<FormattedCharSequence> init_List_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil entry, int maxWidth, int maxLines) {
      Component display = entry != null && entry.init_Component() != null ? entry.init_Component() : Component.literal(this.init_Str_MacroExecutor_Pack(entry));
      List<FormattedCharSequence> wrapped = this.init_Font.split(display, Math.max(20, maxWidth));
      return wrapped.size() <= maxLines ? wrapped : new ArrayList<>(wrapped.subList(0, Math.max(0, maxLines)));
   }

   private List<String> init_List_Str(String text, int maxWidth, int maxLines) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isBlank() && maxLines > 0) {
         String[] words = text.trim().split("\\s+");
         StringBuilder current = new StringBuilder();

         for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
            String word = words[wordIndex];
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.read_I(candidate) <= maxWidth) {
               current.setLength(0);
               current.append(candidate);
            } else if (!current.isEmpty()) {
               if (lines.size() == maxLines - 1) {
                  lines.add(this.init_Str_Str_I(current + " " + this.init_Str_StrArr_I(words, wordIndex), maxWidth));
                  return lines;
               }

               lines.add(current.toString());
               current.setLength(0);
               current.append(word);
            } else {
               if (lines.size() == maxLines - 1) {
                  lines.add(this.init_Str_Str_I(this.init_Str_StrArr_I(words, wordIndex), maxWidth));
                  return lines;
               }

               lines.add(this.init_Str_Str_I(word, maxWidth));
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

   private String init_Str_StrArr_I(String[] words, int startIndex) {
      StringBuilder out = new StringBuilder();

      for (int i = startIndex; i < words.length; i++) {
         if (out.length() > 0) {
            out.append(' ');
         }

         out.append(words[i]);
      }

      return out.toString();
   }

   private String init_Str_Str_I(String text, int maxWidth) {
      String trimmed = text == null ? "" : text;

      while (!trimmed.isEmpty() && this.read_I(trimmed + "...") > maxWidth) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }

      return trimmed.length() < (text == null ? 0 : text.length()) ? trimmed + "..." : trimmed;
   }

   private void queue(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      int cy = bodyTop + 4;
      List<String> c2sTargets = this.init_List_Z(true);
      List<String> s2cTargets = this.init_List_Z(false);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "mode", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "flushOnDisable", x, cy, w, mx, my, delta);
      String mode = this.tick_Str("mode");
      if ("ENABLE".equals(mode)) {
         int[] addBtns = this.init_IArr_I(x, w, false, "Add C2S (" + c2sTargets.size() + ")", "Add S2C (" + s2cTargets.size() + ")");
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, addBtns[0], cy, addBtns[1], 13, "Add C2S (" + c2sTargets.size() + ")", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, () -> this.put(true)
         );
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, addBtns[2], cy, addBtns[3], 13, "Add S2C (" + s2cTargets.size() + ")", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, () -> this.put(false)
         );
         cy += 18;
         int[] loadBtns = this.init_IArr_I(x, w, false, "Load Queue", "Clear All");
         this.init_V_GuiGraphicsExtract(ctx, loadBtns[0], cy, loadBtns[1], 13, "Load Queue", mx, my, this::k);
         this.init_V_GuiGraphicsExtract(ctx, loadBtns[2], cy, loadBtns[3], 13, "Clear All", mx, my, this::l);
         cy += 18;
         int[] presetBtns = this.init_IArr_I(x, w, false, "Default Preset", "Module Preset");
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, presetBtns[0], cy, presetBtns[1], 13, "Default Preset", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, this::m
         );
         this.init_ByteReader_GuiGraphicsExtract_2(
            ctx, presetBtns[2], cy, presetBtns[3], 13, "Module Preset", ByteReader.Inner_StreamUtil.PRIMARY, true, mx, my, this::n
         );
      }
   }

   private List<String> init_List_Z(boolean c2s) {
      return this.write_Map.computeIfAbsent(c2s ? "c2sPackets" : "s2cPackets", ignored -> new ArrayList<>());
   }

   private void put(boolean c2s) {
      if (c2s) {
         this.init_AutismPacketSelect
            .init_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Z(true, packetClass, selected), this.load_List_Z(true));
      } else {
         this.init_AutismPacketSelect
            .load_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Z(false, packetClass, selected), this.load_List_Z(false));
      }
   }

   private List<Class<? extends Packet<?>>> load_List_Z(boolean c2s) {
      List<Class<? extends Packet<?>>> selected = new ArrayList<>();

      for (String target : this.init_List_Z(c2s)) {
         Class<? extends Packet<?>> packetClass = this.init_Class_Str_Z(target, c2s);
         if (packetClass != null && !selected.contains(packetClass)) {
            selected.add(packetClass);
         }
      }

      return selected;
   }

   private Class<? extends Packet<?>> init_Class_Str_Z(String target, boolean c2s) {
      if (target != null && !target.isBlank()) {
         for (Class<? extends Packet<?>> candidate : c2s ? new ArrayList<>(PacketTypeRegistry.load()) : new ArrayList<>(PacketTypeRegistry.init_Set())) {
            String registryName = PacketTypeRegistry.init_Str_Class(candidate);
            if (this.init_Z_Str_Str(target, registryName)) {
               return candidate;
            }

            if (this.init_Z_Str_Str(target, PacketNameResolver.init_Str_Class(candidate))) {
               return candidate;
            }

            if (this.init_Z_Str_Str(target, candidate.getSimpleName())) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private void init_V_Z(boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      if (packetClass != null) {
         List<String> targets = this.init_List_Z(c2s);
         if (selected) {
            if (!this.init_Z_List(targets, packetClass, c2s)) {
               targets.add(PacketNameResolver.init_Str_Class(packetClass));
            }
         } else {
            targets.removeIf(existing -> packetClass.equals(this.init_Class_Str_Z(existing, c2s)));
         }
      }
   }

   private boolean init_Z_List(List<String> targets, Class<? extends Packet<?>> packetClass, boolean c2s) {
      if (targets != null && packetClass != null) {
         for (String target : targets) {
            if (packetClass.equals(this.init_Class_Str_Z(target, c2s))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void k() {
      List<ck.Inner_CachePool> queue = ck.init_ck().load_List();
      if (queue != null && !queue.isEmpty()) {
         int before = this.init_List_Z(true).size() + this.init_List_Z(false).size();

         for (ck.Inner_CachePool queuedPacket : queue) {
            if (queuedPacket != null && queuedPacket.init_Packet != null) {
               Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)queuedPacket.init_Packet.getClass();
               if (PacketTypeRegistry.load().contains(packetClass)) {
                  this.init_V_Z(true, packetClass, true);
               } else if (PacketTypeRegistry.init_Set().contains(packetClass)) {
                  this.init_V_Z(false, packetClass, true);
               }
            }
         }

         int after = this.init_List_Z(true).size() + this.init_List_Z(false).size();
         int added = Math.max(0, after - before);
         AutismClientMessaging.sendPrefixed(
            added == 0 ? "Queue did not add any new packet filters" : "Added " + added + " packet filter" + (added == 1 ? "" : "s") + " from queue"
         );
      } else {
         AutismClientMessaging.sendPrefixed("Queue is empty");
      }
   }

   private void l() {
      this.init_List_Z(true).clear();
      this.init_List_Z(false).clear();
   }

   private void m() {
      autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
      tmp.read();
      this.write_Map.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
      this.write_Map.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
   }

   private void n() {
      autismclient.util.macro.DelayPacketsAction tmp = new autismclient.util.macro.DelayPacketsAction();
      tmp.write();
      this.write_Map.put("c2sPackets", new ArrayList<>(tmp.c2sPacketNames));
      this.write_Map.put("s2cPackets", new ArrayList<>(tmp.s2cPacketNames));
   }

   private String init_Str_Z(boolean c2s) {
      String direction = c2s ? "C2S" : "S2C";
      List<String> targets = this.init_List_Z(c2s);
      if (targets.isEmpty()) {
         return direction + ": none";
      }

      List<String> labels = new ArrayList<>();

      for (int i = 0; i < targets.size() && i < 3; i++) {
         String target = targets.get(i);
         Class<? extends Packet<?>> packetClass = this.init_Class_Str_Z(target, c2s);
         labels.add(packetClass != null ? PacketNameResolver.init_Str_Class(packetClass) : target);
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

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int x, int y, int w, int mx, int my, float delta, boolean c2s) {
      String key = c2s ? "c2sPackets" : "s2cPackets";
      String searchKey = c2s ? "_delay_packets_c2s_search" : "_delay_packets_s2c_search";
      String title = c2s ? "C2S Packets" : "S2C Packets";
      List<String> selected = this.write_Map.getOrDefault(key, Collections.emptyList());
      y = this.init_I_GuiGraphicsExtract_2(ctx, x, y, w, mx, my, key + "_selected", title, selected, selected::remove, value -> value, "(none selected)");
      ThemedTextInputBox search = this.flush_Map.get(searchKey);
      if (search != null) {
         search.read(x);
         search.write(y + 1);
         search.update(w);
         search.init_V(ctx, mx, my, delta);
      }

      y += 18;
      String filter = search != null ? search.load_Str().trim().toLowerCase(Locale.ROOT) : "";
      List<String> options = new ArrayList<>();

      for (String packetName : this.read_List(c2s)) {
         if (filter.isEmpty() || packetName.toLowerCase(Locale.ROOT).contains(filter)) {
            options.add(packetName);
         }
      }

      this.init_V_GuiGraphicsExtract(ctx, x, y, w, mx, my, key + "_search", options, 5, value -> {
         if (selected.contains(value)) {
            selected.remove(value);
         } else {
            selected.add(value);
         }
      }, selected::contains, value -> value);
      return y + 65 + 4;
   }

   private void buffer(GuiGraphicsExtractor ctx, int x, int bodyTop, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int cy = bodyTop + 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "packetOrder", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "targetBlocks", x, cy, w, mx, my, delta);
      cy += 4;
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "stopInventoryFull", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "stopSlotsUsed", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "slotsUsedThreshold", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "stopMinedCount", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "minedCountTarget", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "stopAfterTime", x, cy, w, mx, my, delta);
      cy = this.init_I_GuiGraphicsExtract_5(ctx, "timeoutSeconds", x, cy, w, mx, my, delta);
      this.init_I_GuiGraphicsExtract(ctx, x, cy, w, "Pick target blocks, then choose the stop rule you want.");
   }

   private void o() {
      if (init_Minecraft.player != null) {
         ThemedTextInputBox field = this.init_Map.get("itemName");
         if (field != null) {
            if (init_Minecraft.player.getMainHandItem().isEmpty()) {
               field.init_V_Str("");
               this.update_Map.remove("itemName");
            } else {
               MacroItemTarget target = init_ArrayUtil_ArrayUtil(MacroItemTarget.init_ArrayUtil_ItemStack_I(init_Minecraft.player.getMainHandItem(), -1));
               this.update_Map.put("itemName", target);
               field.init_V_Str(target.init_Str());
            }
         }
      }
   }

   private void p() {
      if (init_Minecraft.player != null) {
         ThemedTextInputBox field = this.init_Map.get("itemName");
         if (field != null) {
            if (init_Minecraft.player.getMainHandItem().isEmpty()) {
               field.init_V_Str("");
               this.update_Map.remove("itemName");
            } else {
               MacroItemTarget target = MacroItemTarget.init_ArrayUtil_ItemStack_I(
                  init_Minecraft.player.getMainHandItem(), init_Minecraft.player.getInventory().getSelectedSlot()
               );
               this.update_Map.put("itemName", target);
               field.init_V_Str(target.init_Str());
            }
         }
      }
   }

   private void q() {
      ThemedTextInputBox field = this.init_Map.get("itemName");
      if (field != null) {
         field.init_V_Str("");
      }

      this.update_Map.remove("itemName");
   }

   private int start_I() {
      ThemedTextInputBox field = this.init_Map.get("slot");
      if (field == null) {
         return 0;
      }

      try {
         return Math.max(0, Math.min(8, Integer.parseInt(field.load_Str().trim())));
      } catch (NumberFormatException ignored) {
         return 0;
      }
   }

   private void put(int slot) {
      ThemedTextInputBox field = this.init_Map.get("slot");
      if (field != null) {
         field.init_V_Str(String.valueOf(Math.max(0, Math.min(8, slot))));
      }
   }

   private String tick_Str() {
      int fallbackSlot = this.start_I() + 1;
      ThemedTextInputBox itemField = this.init_Map.get("itemName");
      String itemName = itemField != null ? itemField.load_Str().trim() : "";
      return itemName.isEmpty() ? "Uses hotbar slot " + fallbackSlot + "." : "Uses the named item, else slot " + fallbackSlot + ".";
   }

   private String run_Str() {
      ThemedTextInputBox field = this.init_Map.get("itemName");
      String item = field != null ? field.load_Str().trim() : "";
      ThemedTextInputBox slotField = this.init_Map.get("slot");
      String slot = slotField != null ? slotField.load_Str().trim() : "";
      if (!slot.isEmpty() && !item.isEmpty()) {
         return "slot #" + slot + " / named item";
      } else if (!slot.isEmpty()) {
         return "slot #" + slot;
      } else {
         return item.isEmpty() ? "the held item" : "the named item";
      }
   }

   private void r() {
      this.load_Map.put("checkMainHand", !this.load_Map.getOrDefault("checkMainHand", true));
   }

   private String get_Str() {
      return this.load_Map.getOrDefault("checkMainHand", true) ? "Hand: Main" : "Hand: Off";
   }

   private void s() {
      if (init_Minecraft.player != null) {
         ThemedTextInputBox field = this.init_Map.get("itemName");
         if (field != null) {
            if (init_Minecraft.player.getMainHandItem().isEmpty()) {
               field.init_V_Str("");
               this.update_Map.remove("itemName");
            } else {
               MacroItemTarget target = MacroItemTarget.init_ArrayUtil_ItemStack_I(
                  init_Minecraft.player.getMainHandItem(), init_Minecraft.player.getInventory().getSelectedSlot()
               );
               this.update_Map.put("itemName", target);
               field.init_V_Str(target.init_Str());
            }
         }
      }
   }

   private void t() {
      ThemedTextInputBox field = this.init_Map.get("itemName");
      if (field != null) {
         field.init_V_Str("");
      }

      this.update_Map.remove("itemName");
   }

   private void u() {
      if (this.put_Z() && init_Minecraft.player != null) {
         ThemedTextInputBox yawField = this.init_Map.get("yaw");
         ThemedTextInputBox pitchField = this.init_Map.get("pitch");
         if (yawField != null) {
            yawField.init_V_Str(init_Str_D(init_Minecraft.player.getYRot()));
         }

         if (pitchField != null) {
            pitchField.init_V_Str(init_Str_D(init_Minecraft.player.getXRot()));
         }
      }
   }

   private void v() {
      if (this.put_Z() && init_Minecraft.player != null) {
         ThemedTextInputBox xField = this.init_Map.get("pos_0");
         ThemedTextInputBox yField = this.init_Map.get("pos_1");
         ThemedTextInputBox zField = this.init_Map.get("pos_2");
         if (xField != null) {
            xField.init_V_Str(init_Str_D(init_Minecraft.player.getX()));
         }

         if (yField != null) {
            yField.init_V_Str(init_Str_D(init_Minecraft.player.getY()));
         }

         if (zField != null) {
            zField.init_V_Str(init_Str_D(init_Minecraft.player.getZ()));
         }
      }
   }

   private String set_Str() {
      ThemedTextInputBox itemField = this.init_Map.get("itemName");
      String itemName = itemField != null ? itemField.load_Str().trim() : "";
      String handLabel = this.load_Map.getOrDefault("checkMainHand", true) ? "main hand" : "off hand";
      return itemName.isEmpty() ? "Waits for the " + handLabel + " cooldown." : "Waits for the named item cooldown.";
   }

   private void w() {
      boolean fromUseName = this.load_Map.getOrDefault("fromUseItemName", false);
      boolean toUseName = this.load_Map.getOrDefault("toUseItemName", false);
      String fromItem = this.init_Map.containsKey("fromItemName") ? this.init_Map.get("fromItemName").load_Str() : "";
      String toItem = this.init_Map.containsKey("toItemName") ? this.init_Map.get("toItemName").load_Str() : "";
      String fromSlot = this.init_Map.containsKey("fromSlot") ? this.init_Map.get("fromSlot").load_Str() : "";
      String toSlot = this.init_Map.containsKey("toSlot") ? this.init_Map.get("toSlot").load_Str() : "";
      this.load_Map.put("fromUseItemName", toUseName);
      this.load_Map.put("toUseItemName", fromUseName);
      ThemedTextInputBox fromItemField = this.init_Map.get("fromItemName");
      ThemedTextInputBox toItemField = this.init_Map.get("toItemName");
      ThemedTextInputBox fromSlotField = this.init_Map.get("fromSlot");
      ThemedTextInputBox toSlotField = this.init_Map.get("toSlot");
      if (fromItemField != null) {
         fromItemField.init_V_Str(toItem);
      }

      if (toItemField != null) {
         toItemField.init_V_Str(fromItem);
      }

      if (fromSlotField != null) {
         fromSlotField.init_V_Str(toSlot);
      }

      if (toSlotField != null) {
         toSlotField.init_V_Str(fromSlot);
      }
   }

   private String put_Str() {
      return "Swaps the two targets. Name mode uses the first visible match.";
   }

   private String add_Str() {
      String clickType = this.tick_Str("clickType");
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

   private String add_Str(String waitMode) {
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

   private int init_I_GuiGraphicsExtract_5(GuiGraphicsExtractor ctx, String key, int x, int y, int w, int mx, int my, float delta) {
      if (this.flush_Z(key)) {
         return y;
      }

      FieldDef field = this.load_FieldDef_Str(key);
      if (field != null && this.write(field)) {
         this.init_V_GuiGraphicsExtract_5(ctx, field, x, y, w, mx, my, delta);
         return y + this.init_I_FieldDef(field) + 2;
      }

      if ("safeClickDelayTicks".equals(key)) {
         this.j();
      }

      return y;
   }

   private FieldDef load_FieldDef_Str(String key) {
      if (this.init_ActionFieldSchema == null) {
         return null;
      }

      for (FieldDef field : this.init_ActionFieldSchema.fields()) {
         if (field.key().equals(key)) {
            return field;
         }
      }

      return null;
   }

   private <T> int init_I_GuiGraphicsExtract_2(
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
      return this.init_I_GuiGraphicsExtract(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, ignored -> null, values::clear, emptyLabel, false
      );
   }

   private <T> int init_I_GuiGraphicsExtract(
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
      return this.init_I_GuiGraphicsExtract(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, richFormatter, values::clear, emptyLabel, false
      );
   }

   private <T> int init_I_GuiGraphicsExtract(
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
      return this.init_I_GuiGraphicsExtract(
         ctx, x, y, w, mx, my, listKey, label, values, removeAction, formatter, richFormatter, clearAction, emptyLabel, false
      );
   }

   private <T> int init_I_GuiGraphicsExtract(
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
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      int headerBtnW = this.write_I("Clear");
      int editIdx = editable ? this.open_Map.getOrDefault(listKey, -1) : -1;
      if (editIdx >= values.size()) {
         editIdx = -1;
         this.open_Map.put(listKey, -1);
      }

      autismclient.gui.f.b.k.init(ctx, this.init_Font, label + " (" + values.size() + ")", font, UiColorPalette.parse(), x, y + 2, false);
      boolean canClear = !values.isEmpty();
      int clearX = x + w - headerBtnW;
      this.init_ByteReader_GuiGraphicsExtract_2(ctx, clearX, y, headerBtnW, 13, "Clear", ByteReader.Inner_StreamUtil.DANGER, canClear, mx, my, () -> {
         clearAction.run();
         this.open_Map.put(listKey, -1);
         EventBus vp = this.tick_Map.get(listKey);
         if (vp != null) {
            vp.load_V(0);
         }
      });
      y += 14;
      int listH = 56;
      int itemW = w - 5 - 1;
      EventBus simpleViewport = this.init_EventBus_Map(this.tick_Map, listKey, x, y, w, listH, 14, 5);
      simpleViewport.init_V(values.size() * 14);
      simpleViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (values.isEmpty()) {
         TableIndex.init(ctx, x, y, itemW, 12, false, false);
         FileCache.init(ctx, this.init_Font, emptyLabel, x, y, itemW);
         return y + listH + 4;
      }

      simpleViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
      int first = simpleViewport.pop();

      for (int i = first; i < values.size() && i <= simpleViewport.tick(); i++) {
         int iy = simpleViewport.init_I(i);
         if (iy != -2147483648) {
            T value = values.get(i);
            int removeW = 13;
            int rowW = itemW - removeW - 2;
            boolean selected = editable && i == editIdx;
            boolean hovered = mx >= x && mx < x + rowW && my >= iy && my < iy + 13;
            Component richLabel = richFormatter == null ? null : richFormatter.apply(value);
            if (richLabel != null) {
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx, this.init_Font, richLabel, autismclient.gui.f.NetHelper.init(x, iy, rowW, 13), hovered, selected, FileCache.Inner_DataUtil.NORMAL, true
               );
            } else {
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  Component.literal(formatter.apply(value)),
                  autismclient.gui.f.NetHelper.init(x, iy, rowW, 13),
                  hovered,
                  selected,
                  FileCache.Inner_DataUtil.NORMAL,
                  false
               );
            }

            if (editable) {
               int fi = i;
               String rawText = value instanceof String rawValue ? rawValue : formatter.apply(value);
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, rowW, 13, () -> {
                  int curIdx = this.open_Map.getOrDefault(listKey, -1);
                  if (curIdx == fi) {
                     this.open_Map.put(listKey, -1);
                     this.poll_Map.put(listKey, "");
                  } else {
                     this.open_Map.put(listKey, fi);
                     this.poll_Map.put(listKey, rawText != null ? rawText : "");
                  }
               }));
            }

            int removeX = x + rowW + 2;
            int fi2 = i;
            this.init_V_GuiGraphicsExtract_2(ctx, removeX, iy, removeW, mx, my, () -> {
               removeAction.accept(values.get(fi2));
               if (editable) {
                  int curSel = this.open_Map.getOrDefault(listKey, -1);
                  if (curSel == fi2) {
                     this.open_Map.put(listKey, -1);
                     this.poll_Map.put(listKey, "");
                  } else if (curSel > fi2) {
                     this.open_Map.put(listKey, curSel - 1);
                  }
               }

               EventBus vp = this.tick_Map.get(listKey);
               if (vp != null) {
                  vp.read_V(-1);
               }
            });
         }
      }

      simpleViewport.init(ctx);
      return y + listH + 4;
   }

   private <T> void init_V_GuiGraphicsExtract_2(
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
      this.init_V_GuiGraphicsExtract(ctx, x, y, w, mx, my, listKey, values, visibleRows, clickAction, ignored -> false, formatter);
   }

   private <T> void init_V_GuiGraphicsExtract(
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
      EventBus regViewport = this.init_EventBus_Map(this.close_Map, listKey, x, y, w, listH, rowH, 5);
      regViewport.init_V(values.size() * rowH);
      regViewport.init_V_GuiGraphicsExtract(ctx, mx, my);
      if (values.isEmpty()) {
         TableIndex.init(ctx, x, y, itemW, 12, false, false);
         FileCache.init(ctx, this.init_Font, "No matches", x, y, itemW);
      } else {
         regViewport.init_V_GuiGraphicsExtract_2(ctx, this.init_CharEncoder.set(), 905969664);
         int first = regViewport.pop();

         for (int i = first; i < values.size() && i <= regViewport.tick(); i++) {
            int iy = regViewport.init_I(i);
            if (iy != -2147483648) {
               T value = values.get(i);
               String display = formatter.apply(value);
               boolean selected = selectedPredicate != null && selectedPredicate.test(value);
               boolean hovered = mx >= x && mx < x + itemW && my >= iy && my < iy + rowH;
               ItemStack icon = this.init_ItemStack_Str_Obj(listKey, value);
               autismclient.gui.macro.editor.a.CachePool.init(
                  ctx,
                  this.init_Font,
                  Component.literal(display),
                  icon,
                  autismclient.gui.f.NetHelper.init(x, iy, itemW, rowH),
                  hovered,
                  selected,
                  selected ? FileCache.Inner_DataUtil.READY : FileCache.Inner_DataUtil.NORMAL,
                  false
               );
               T clickedValue = value;
               this.valid_List.add(new ActionEditorOverlay.Inner_StreamUtil(x, iy, itemW, rowH, () -> clickAction.accept(clickedValue)));
            }
         }

         regViewport.init(ctx);
      }
   }

   private ItemStack init_ItemStack_Str_Obj(String listKey, Object value) {
      if (value instanceof String raw && !raw.isBlank()) {
         String id = raw;
         if (raw.startsWith("~")) {
            id = this.start(raw);
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
                  return this.init_ItemStack_Identifier(parsed);
               } else {
                  return key.contains("sound") ? this.load_ItemStack_Identifier(parsed) : ItemStack.EMPTY;
               }
            }
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_Identifier(Identifier entityId) {
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

   private ItemStack load_ItemStack_Identifier(Identifier soundId) {
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

   private void x() {
      if (this.put_Z() && this.init_MacroAction instanceof WaitForEntityAction) {
         Screen previousScreen = init_Minecraft.gui.screen();
         this.index();
         ck state = ck.init_ck();
         state.read_V_Runnable(() -> {
            if (previousScreen != null) {
               OverlayScreenController.init_V(init_Minecraft, previousScreen);
            }

            state.load_V_Consumer(null);
            this.init_V_Z_Z(false, false);
         });
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
         state.value(this.poll_Z);
         state.load_V_Consumer(payload -> init_Minecraft.execute(() -> {
            List<String> selected = this.write_Map.get("entityIds");
            if (selected != null && payload != null && !payload.isBlank()) {
               if (this.poll_Z) {
                  selected.removeIf(existing -> existing.startsWith(this.stop(payload)));
               }

               if (!selected.contains(payload)) {
                  selected.add(payload);
               }
            }

            if (previousScreen != null) {
               OverlayScreenController.init_V(init_Minecraft, previousScreen);
            }

            this.init_V_Z_Z(false, false);
            OverlayManager.init_bk().update_V(this);
         }));
      }
   }

   private static String clear_Str_Str(String value) {
      return cc.sync(value);
   }

   private String reset_Str(String entry) {
      if (entry == null || entry.isBlank()) {
         return "(unknown)";
      } else if (entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         String rawName = parts.length >= 4 ? parts[3] : "";
         String type = parts.length >= 3 ? cc.read(parts[2]) : "?";
         String uuid = parts.length >= 2 ? parts[1] : "";
         String suffix = uuid.length() >= 4 ? uuid.substring(uuid.length() - 4) : uuid;
         String name = rawName == null ? "" : rawName.trim();
         return !name.isEmpty() && !name.equalsIgnoreCase(type) && !name.equalsIgnoreCase(clear_Str_Str(type))
            ? name + " (" + type + " #" + suffix + ")"
            : type + " #" + suffix;
      } else {
         return cc.read(entry);
      }
   }

   private List<String> sync_List() {
      boolean supportedAction = this.init_MacroAction instanceof WaitForEntityAction
         || this.init_MacroAction instanceof LookAtBlockAction
         || this.init_MacroAction instanceof OpenContainerAction
         || this.init_MacroAction instanceof InteractEntityAction;
      if (supportedAction && init_Minecraft.player != null && init_Minecraft.level != null) {
         List<String> result = new ArrayList<>();

         for (Entity entity : init_Minecraft.level.entitiesForRendering()) {
            if (entity != init_Minecraft.player && !(init_Minecraft.player.distanceTo(entity) > 16.0)) {
               String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
               String uuid = entity.getStringUUID();
               String displayName = entity.getDisplayName().getString().replaceAll("§.", "").trim();
               result.add("~" + uuid + "~" + typeId + "~" + displayName);
            }
         }

         result.sort(Comparator.comparingDouble(this::init_D_Str));
         return result;
      } else {
         return Collections.emptyList();
      }
   }

   private boolean init_Z_Entity(Entity entity) {
      return entity == null ? false : entity instanceof AbstractMinecartContainer || entity instanceof ChestBoat || entity instanceof AbstractHorse;
   }

   private double init_D_Str(String entry) {
      if (init_Minecraft.player != null && entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         if (parts.length >= 2 && init_Minecraft.level != null) {
            try {
               UUID uuid = UUID.fromString(parts[1]);
               Entity entity = null;

               for (Entity candidate : init_Minecraft.level.entitiesForRendering()) {
                  if (uuid.equals(candidate.getUUID())) {
                     entity = candidate;
                     break;
                  }
               }

               return entity != null ? init_Minecraft.player.distanceTo(entity) : 1.7976931348623157E308;
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

   private String start(String entry) {
      if (entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         return parts.length >= 3 ? parts[2] : "";
      } else {
         return "";
      }
   }

   private String stop(String entry) {
      if (entry != null && entry.startsWith("~")) {
         String[] parts = entry.split("~", 4);
         return parts.length >= 2 ? "~" + parts[1] + "~" : "";
      } else {
         return "";
      }
   }

   private String next(String entry) {
      String label = this.reset_Str(entry);
      double dist = this.init_D_Str(entry);
      return dist == 1.7976931348623157E308 ? label : label + " (" + String.format(Locale.ROOT, "%.1fm", dist) + ")";
   }

   private List<BlockPos> flush_List() {
      if (init_Minecraft.player != null && init_Minecraft.level != null) {
         BlockPos center = init_Minecraft.player.blockPosition();
         List<BlockPos> found = new ArrayList<>();
         int range = 10;

         for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
               for (int dz = -range; dz <= range; dz++) {
                  BlockPos pos = center.offset(dx, dy, dz);
                  if (this.init_Z_BlockPos(pos)) {
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

   private boolean init_Z_BlockPos(BlockPos pos) {
      if (init_Minecraft.level != null && pos != null) {
         BlockState state = init_Minecraft.level.getBlockState(pos);
         if (state == null || state.isAir()) {
            return false;
         }

         if (state.getMenuProvider(init_Minecraft.level, pos) != null) {
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

   private String init_Str_BlockPos(BlockPos pos) {
      if (pos == null) {
         return "(unknown)";
      }

      String blockName = init_Minecraft.level != null ? init_Minecraft.level.getBlockState(pos).getBlock().getName().getString() : "Container";
      return blockName + " (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
   }

   private void init_V_Str_BlockPos(String fieldKey, BlockPos pos) {
      if (fieldKey != null && pos != null) {
         ThemedTextInputBox fx = this.init_Map.get(fieldKey + "_0");
         ThemedTextInputBox fy = this.init_Map.get(fieldKey + "_1");
         ThemedTextInputBox fz = this.init_Map.get(fieldKey + "_2");
         if (fx != null) {
            fx.init_V_Str(String.valueOf(pos.getX()));
         }

         if (fy != null) {
            fy.init_V_Str(String.valueOf(pos.getY()));
         }

         if (fz != null) {
            fz.init_V_Str(String.valueOf(pos.getZ()));
         }
      }
   }

   private boolean init_Z_Str_BlockPos(String fieldKey, BlockPos pos) {
      if (fieldKey != null && pos != null) {
         ThemedTextInputBox fx = this.init_Map.get(fieldKey + "_0");
         ThemedTextInputBox fy = this.init_Map.get(fieldKey + "_1");
         ThemedTextInputBox fz = this.init_Map.get(fieldKey + "_2");
         if (fx != null && fy != null && fz != null) {
            try {
               int x = (int)Math.round(Double.parseDouble(fx.load_Str().trim()));
               int y = (int)Math.round(Double.parseDouble(fy.load_Str().trim()));
               int z = (int)Math.round(Double.parseDouble(fz.load_Str().trim()));
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

   private List<String> close_List() {
      return this.write_Map.computeIfAbsent("packetNames", ignored -> new ArrayList<>());
   }

   private void add(boolean c2s) {
      Consumer<Class<? extends Packet<?>>> add = packetClass -> {
         if (packetClass != null) {
            if (this.reset_List == null) {
               this.reset_List = new ArrayList<>();
            }

            autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = new autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool();
            rule.init_aM_DataUtil = c2s ? autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.C2S : autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.S2C;
            String name = PacketTypeRegistry.init_Str_Class(packetClass);
            rule.init_Str = name != null && !name.isBlank() ? name : PacketNameResolver.init_Str_Class(packetClass);
            this.reset_List.add(rule);
            this.f_z_I = this.reset_List.size() - 1;
            this.z();
         }
      };
      if (c2s) {
         this.init_AutismPacketSelect.load(add);
      } else {
         this.init_AutismPacketSelect.read(add);
      }
   }

   private void run_V(String fieldKey) {
      ThemedTextInputBox field = this.init_Map.get(fieldKey);
      Consumer<Class<? extends Packet<?>>> set = packetClass -> {
         if (packetClass != null && field != null) {
            String name = PacketTypeRegistry.init_Str_Class(packetClass);
            field.init_V_Str(name != null && !name.isBlank() ? name : PacketNameResolver.init_Str_Class(packetClass));
         }
      };
      String direction = this.tick_Str("direction");
      if ("S2C".equals(direction)) {
         this.init_AutismPacketSelect.read(set);
      } else if ("ANY".equals(direction)) {
         this.init_AutismPacketSelect.init_V_Consumer(set);
      } else {
         this.init_AutismPacketSelect.load(set);
      }
   }

   private void y() {
      if (this.reset_List != null && this.f_z_I >= 0 && this.f_z_I < this.reset_List.size()) {
         ThemedTextInputBox valueField = this.init_Map.get("_wpm_value");
         if (valueField != null) {
            this.reset_List.get(this.f_z_I).read = valueField.load_Str();
         }
      }
   }

   private void z() {
      ThemedTextInputBox valueField = this.init_Map.get("_wpm_value");
      if (valueField != null) {
         if (this.reset_List != null && this.f_z_I >= 0 && this.f_z_I < this.reset_List.size()) {
            valueField.init_V_Str(this.reset_List.get(this.f_z_I).read == null ? "" : this.reset_List.get(this.f_z_I).read);
         } else {
            valueField.init_V_Str("");
         }
      }
   }

   private String init_Str_aM_CachePool(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      return rule != null && rule.load != null && !rule.load.isBlank() ? rule.load : "Packet Only";
   }

   private Component init_Component_aM_CachePool(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule == null) {
         return Component.literal("(empty)");
      }

      String label = rule.init_aM_DataUtil + " " + (rule.init_Str != null && !rule.init_Str.isBlank() ? PacketNameResolver.init_Str_Str(rule.init_Str) : "Any Packet");
      if (rule.load != null && !rule.load.isBlank()) {
         label = label + " / " + rule.load;
      }

      return Component.literal(label);
   }

   private void init_V_aM_CachePool(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule != null) {
         Class<? extends Packet<?>> packetClass = this.init_Class_aM_CachePool(rule);
         List<String> fields = new ArrayList<>(autismclient.util.macro.WaitPacketMatchAction.init_List(packetClass));
         fields.add(0, "");
         int idx = fields.indexOf(rule.load == null ? "" : rule.load);
         rule.load = fields.get((idx + 1 + fields.size()) % fields.size());
         rule.init_aM_StreamUtil = rule.load.isBlank() ? autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS : rule.init_aM_StreamUtil;
         List<String> options = autismclient.util.macro.WaitPacketMatchAction.init_List_Class_Str(packetClass, rule.load);
         if (!options.isEmpty() && (rule.read == null || rule.read.isBlank() || !options.contains(rule.read))) {
            rule.read = options.get(0);
         }

         this.z();
      }
   }

   private Class<? extends Packet<?>> init_Class_aM_CachePool(autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule) {
      if (rule == null) {
         return null;
      }

      String direction = rule.init_aM_DataUtil == autismclient.util.macro.WaitPacketMatchAction.Inner_DataUtil.ANY ? "" : rule.init_aM_DataUtil.name();
      return this.init_Class_Str_Str(direction, rule.init_Str == null ? "" : rule.init_Str);
   }

   private static <E extends Enum<E>> E init_Enum_Enum_EnumArr(E current, E[] values) {
      if (values != null && values.length != 0) {
         int idx = current == null ? -1 : current.ordinal();
         return values[(idx + 1 + values.length) % values.length];
      } else {
         return current;
      }
   }

   private List<String> init_List_List(List<String> rawTargets) {
      List<String> sanitized = new ArrayList<>();

      for (String rawTarget : rawTargets == null ? Collections.<String>emptyList() : rawTargets) {
         String normalized = this.prev(rawTarget);
         if (!normalized.isEmpty() && !sanitized.contains(normalized)) {
            sanitized.add(normalized);
         }
      }

      return sanitized;
   }

   private String prev(String target) {
      String normalized = WaitForPacketAction.read(target);
      if (normalized.isEmpty()) {
         return "";
      } else if (!WaitForPacketAction.init(normalized).isEmpty()) {
         return normalized;
      } else {
         Class<? extends Packet<?>> packetClass = this.init_Class_Str_Str("", normalized);
         if (packetClass == null) {
            return normalized;
         } else if (PacketTypeRegistry.load().contains(packetClass)) {
            return this.init_Str_Str_Class("C2S", packetClass);
         } else {
            return PacketTypeRegistry.init_Set().contains(packetClass) ? this.init_Str_Str_Class("S2C", packetClass) : normalized;
         }
      }
   }

   private List<String> read_List(String direction) {
      List<String> filtered = this.pop_Map.computeIfAbsent("wait_packet_targets_" + direction.toUpperCase(Locale.ROOT), ignored -> new ArrayList<>());
      filtered.clear();

      for (String target : this.close_List()) {
         String normalized = this.prev(target);
         if (!normalized.isEmpty() && direction.equalsIgnoreCase(WaitForPacketAction.init(normalized))) {
            filtered.add(normalized);
         }
      }

      return filtered;
   }

   private String valid(String target) {
      return WaitForPacketAction.write(target);
   }

   private void get_V(String direction) {
      this.close_List().removeIf(target -> direction.equalsIgnoreCase(WaitForPacketAction.init(this.prev(target))));
      EventBus vpC2s = this.tick_Map.get("wait_packet_c2s");
      if (vpC2s != null) {
         vpC2s.load_V(0);
      }

      EventBus vpS2c = this.tick_Map.get("wait_packet_s2c");
      if (vpS2c != null) {
         vpS2c.load_V(0);
      }
   }

   private void set_V(String target) {
      String normalized = this.prev(target);
      if (!normalized.isEmpty()) {
         this.close_List().removeIf(existing -> normalized.equals(this.prev(existing)));
      }
   }

   private void clear_V_Z(boolean c2s) {
      if (c2s) {
         this.init_AutismPacketSelect
            .init_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Str("C2S", packetClass, selected), this.write_List("C2S"));
      } else {
         this.init_AutismPacketSelect
            .load_V_BiConsumer_Collection((packetClass, selected) -> this.init_V_Str("S2C", packetClass, selected), this.write_List("S2C"));
      }
   }

   private List<Class<? extends Packet<?>>> write_List(String direction) {
      List<Class<? extends Packet<?>>> selected = new ArrayList<>();

      for (String target : this.read_List(direction)) {
         Class<? extends Packet<?>> packetClass = this.init_Class_Str_Str(direction, target);
         if (packetClass != null && !selected.contains(packetClass)) {
            selected.add(packetClass);
         }
      }

      return selected;
   }

   private void init_V_Str(String direction, Class<? extends Packet<?>> packetClass, boolean selected) {
      String target = this.init_Str_Str_Class(direction, packetClass);
      if (!target.isEmpty()) {
         List<String> targets = this.close_List();
         if (selected) {
            if (!targets.stream().map(this::prev).anyMatch(target::equals)) {
               targets.add(target);
            }
         } else {
            targets.removeIf(existing -> target.equals(this.prev(existing)));
         }
      }
   }

   private void A() {
      List<ck.Inner_CachePool> queue = ck.init_ck().load_List();
      if (queue != null && !queue.isEmpty()) {
         List<String> targets = this.close_List();
         List<String> merged = this.init_List_List(targets);
         int before = merged.size();

         for (ck.Inner_CachePool queuedPacket : queue) {
            String target = this.init_Str_ck_CachePool(queuedPacket);
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

   private String init_Str_ck_CachePool(ck.Inner_CachePool queuedPacket) {
      if (queuedPacket != null && queuedPacket.init_Packet != null) {
         Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)queuedPacket.init_Packet.getClass();
         if (PacketTypeRegistry.load().contains(packetClass)) {
            return this.init_Str_Str_Class("C2S", packetClass);
         } else {
            return PacketTypeRegistry.init_Set().contains(packetClass) ? this.init_Str_Str_Class("S2C", packetClass) : "";
         }
      } else {
         return "";
      }
   }

   private String init_Str_Str_Class(String direction, Class<? extends Packet<?>> packetClass) {
      if (packetClass == null) {
         return "";
      }

      String name = PacketTypeRegistry.init_Str_Class(packetClass);
      if (name == null || name.isBlank()) {
         name = PacketNameResolver.init_Str_Class(packetClass);
      }

      return WaitForPacketAction.init(direction, name);
   }

   private Class<? extends Packet<?>> init_Class_Str_Str(String direction, String target) {
      String packetName = WaitForPacketAction.load(target);
      if (packetName.isBlank()) {
         return null;
      }

      Class<? extends Packet<?>> direct = PacketTypeRegistry.init_Class_Str(packetName);
      if (direct != null) {
         if (direction.isBlank()) {
            return direct;
         }

         if ("C2S".equalsIgnoreCase(direction) && PacketTypeRegistry.load().contains(direct)) {
            return direct;
         }

         if ("S2C".equalsIgnoreCase(direction) && PacketTypeRegistry.init_Set().contains(direct)) {
            return direct;
         }
      }

      List<Class<? extends Packet<?>>> pool = new ArrayList<>();
      if ("C2S".equalsIgnoreCase(direction)) {
         pool.addAll(PacketTypeRegistry.load());
      } else if ("S2C".equalsIgnoreCase(direction)) {
         pool.addAll(PacketTypeRegistry.init_Set());
      } else {
         pool.addAll(PacketTypeRegistry.load());
         pool.addAll(PacketTypeRegistry.init_Set());
      }

      for (Class<? extends Packet<?>> candidate : pool) {
         String registryName = PacketTypeRegistry.init_Str_Class(candidate);
         if (this.init_Z_Str_Str(packetName, registryName)) {
            return candidate;
         }

         if (this.init_Z_Str_Str(packetName, PacketNameResolver.init_Str_Class(candidate))) {
            return candidate;
         }

         if (this.init_Z_Str_Str(packetName, candidate.getSimpleName())) {
            return candidate;
         }
      }

      return null;
   }

   private boolean init_Z_Str_Str(String expected, String candidate) {
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

   private List<String> read_List(boolean c2s) {
      List<String> names = new ArrayList<>();

      for (Class<? extends Packet<?>> packetClass : c2s ? PacketTypeRegistry.load() : PacketTypeRegistry.init_Set()) {
         String name = PacketTypeRegistry.init_Str_Class(packetClass);
         if (name != null && !name.isBlank()) {
            names.add(name);
         }
      }

      Collections.sort(names);
      return names;
   }

   private int init_I_GuiGraphicsExtract_4(GuiGraphicsExtractor ctx, FieldDef field, int x, int y, int w, int mx, int my, float delta) {
      Identifier font = this.init_CharEncoder.init_Identifier_l(l.read);
      this.init_V_GuiGraphicsExtract_3(ctx, field.label(), x, y, w, font);
      y += 13;
      int bw = (w - 4) / 3;

      for (int i = 0; i < 3; i++) {
         ThemedTextInputBox f = this.init_Map.get(field.key() + "_" + i);
         if (f != null) {
            int fx = x + i * (bw + 2);
            f.read(fx);
            f.write(y + 1);
            f.update(bw);
            f.init_V(ctx, mx, my, delta);
         }
      }

      return y + 14 + 2;
   }

   private static List<String> open_List() {
      if (load_List == null) {
         load_List = new ArrayList<>();

         for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
            if (id != null) {
               load_List.add(id.toString());
            }
         }

         Collections.sort(load_List);
      }

      return load_List;
   }

   private static List<String> poll_List() {
      if (read_List == null) {
         read_List = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            read_List.add(id.toString());
         }

         Collections.sort(read_List);
      }

      return read_List;
   }

   private static List<String> push_List() {
      if (write_List == null) {
         write_List = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
            write_List.add(id.toString());
         }

         Collections.sort(write_List);
      }

      return write_List;
   }

   private int init_I_FieldDef(FieldDef field) {
      if (field != null && "safeClickDelayTicks".equals(field.key())) {
         return 29;
      }

      if (this.read(field)) {
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
            if (this.init_Z_FieldDef(field)) {
               var10000 = 86;
            } else if (field.captureMode() == StreamUtil.BLOCK_CATALOG) {
               var10000 = 161;
            } else {
               int extraEditorRow = this.put_Z(field.key()) && this.open_Map.getOrDefault(field.key(), -1) >= 0 ? 15 : 0;
               var10000 = 84 + extraEditorRow;
            }
            break;
         default:
            var10000 = 14;
      }

      return var10000;
   }

   private int stop_I() {
      if (this.init_MapStore != null) {
         int h = 100;
         h += 16;
         if (this.reset_Z()) {
            h += 16;
         }

         h += 16;
         if (this.load_Map.getOrDefault("item_waitForGuiBefore", false) || this.load_Map.getOrDefault("item_waitForGuiAfter", false)) {
            h += 16;
         }

         h += 16;
         h += 16;
         if (this.load_Map.getOrDefault("item_useClickDelay", false)) {
            h += 16;
         }

         return h;
      } else if (this.init_H != null) {
         int h = 4;
         h += 13;
         h += 24;
         h += 18;
         h += 18;
         h += 16;
         h += 13;
         h += (this.f_i_I == 0 ? 143 : 152) + 4;
         h += 14;
         h += 20;
         h += 24;
         if (this.start_Z()) {
            h += 24;
         }

         return h;
      } else if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SEND_PACKET) {
         int h = 80;
         h += 16;
         h += 16;
         h += 16;
         if (this.next_Z()) {
            h += 16;
         }

         return h + 116;
      } else {
         if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SOUND) {
            int h = 4;
            h += this.write(this.load_FieldDef_Str("waitForGuiBefore")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("waitForGuiAfter")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("waitGuiName")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("checkDistance")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("maxDistance")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0;
            h += 74;
            h += 18;
            return h + 78;
         }

         if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_ENTITY) {
            int h = 4;
            h += this.write(this.load_FieldDef_Str("checkMode")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("radius")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("centerOnPlayer")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("pos")) ? this.init_I_FieldDef(this.load_FieldDef_Str("pos")) + 2 : 0;
            h += this.write(this.load_FieldDef_Str("mustBeLookingAt")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0;
            h += 74;
            h += 18;
            h += 62;
            return h + 52;
         }

         if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.LOOK_AT_BLOCK) {
            int h = 4;
            h += this.write(this.load_FieldDef_Str("targetMode")) ? 16 : 0;
            String mode = this.tick_Str("targetMode");
            if ("BLOCK".equals(mode)) {
               h += this.write(this.load_FieldDef_Str("searchRadius")) ? 16 : 0;
               h += this.init_I_FieldDef(this.load_FieldDef_Str("blockIds")) + 2;
            } else if ("ENTITY".equals(mode)) {
               h += this.write(this.load_FieldDef_Str("searchRadius")) ? 16 : 0;
               h += 74;
               h += 18;
               h += 66;
               h += 56;
            } else {
               h += this.init_I_FieldDef(this.load_FieldDef_Str("blockPos")) + 2;
            }

            h += this.write(this.load_FieldDef_Str("smooth")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("smoothness")) ? 16 : 0;
            h += this.write(this.load_FieldDef_Str("waitForCompletion")) ? 16 : 0;
            return h + 14;
         } else {
            if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.ROTATE) {
               int h = 4;
               h += this.write(this.load_FieldDef_Str("yaw")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("pitch")) ? 16 : 0;
               h += 18;
               h += this.write(this.load_FieldDef_Str("smooth")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("smoothness")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("waitForCompletion")) ? 16 : 0;
               return h + 14;
            }

            if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.GO_TO) {
               int h = 4;
               h += this.init_I_FieldDef(this.load_FieldDef_Str("pos")) + 2;
               h += 18;
               h += this.write(this.load_FieldDef_Str("waitForArrival")) ? 16 : 0;
               return h + 14;
            }

            if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.USE_ITEM) {
               int h = 4;
               h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("itemName")) ? 16 : 0;
               h += 18;
               h += this.write(this.load_FieldDef_Str("useMode")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("waitForFinish")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("sneak")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("holdTicks")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("useCount")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("waitForGui")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("guiName")) ? 16 : 0;
               return h + 16;
            }

            if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.INVENTORY_AUDIT) {
               int h = 4;
               h += this.write(this.load_FieldDef_Str("mode")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("targetItems")) ? 92 : 0;
               h += this.write(this.load_FieldDef_Str("openMode")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("openCommand")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("containerPos")) ? this.init_I_FieldDef(this.load_FieldDef_Str("containerPos")) + 2 : 0;
               if ("CONTAINER".equals(this.tick_Str("openMode"))) {
                  h += 56;
               }

               h += this.write(this.load_FieldDef_Str("dupeVector")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("iterations")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("maxTransferAttempts")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("transferRetryDelayMs")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("spamCount")) ? 16 : 0;
               h += this.write(this.load_FieldDef_Str("spamDelayMs")) ? 16 : 0;
               return h + 16;
            } else {
               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SWAP_SLOTS) {
                  int h = 20;
                  h += this.write(this.load_FieldDef_Str("fromUseItemName")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("fromItemName")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("fromSlot")) ? 16 : 0;
                  h += 24;
                  h += this.write(this.load_FieldDef_Str("toUseItemName")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("toItemName")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("toSlot")) ? 16 : 0;
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CLICK) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("clickType")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("clickCount")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForGuiBefore")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("guiName")) ? 16 : 0;
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DISCONNECT) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("mode")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("delayMs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("lagMethod")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("kickMethod")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("packetCount")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("useNextAction")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("trigger")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("tolerance")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("bufferMs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("timeoutSec")) ? 16 : 0;
                  return h + 12;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_GUI) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("waitMode")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("guiType")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("matchMode")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("guiTitle")) ? 16 : 0;
                  h += 18;
                  h += this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("timeoutMs")) ? 16 : 0;
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.SELECT_SLOT) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("itemName")) ? 16 : 0;
                  h += 18;
                  h += 30;
                  h += this.write(this.load_FieldDef_Str("strategy")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("outputVariable")) ? 16 : 0;
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_COOLDOWN) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("itemName")) ? 16 : 0;
                  h += 18;
                  h += 18;
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_CHAT) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("pattern")) ? this.init_I_FieldDef(this.load_FieldDef_Str("pattern")) + 2 : 0;
                  h += 18;
                  h += this.write(this.load_FieldDef_Str("matchMode")) ? 16 : 0;
                  h += "MATCH".equals(this.tick_Str("matchMode")) ? 29 : 0;
                  h += this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("timeoutMs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForGuiBefore")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForGuiAfter")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitGuiName")) ? 16 : 0;
                  h += 18;
                  h += 18;
                  h += 13 + this.reset_I();
                  return h + 16;
               }

               if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.CAPTURE_VALUE) {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("source")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("autofillCommand")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("autofillTimeoutMs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("autofillCacheList")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listSelection")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listPickPosition")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listFilter")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listFilterText")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listExcludeText")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("listStripPrefix")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("excludeSelf")) ? 16 : 0;
                  if ("RECENT_CHAT".equals(this.tick_Str("source"))) {
                     h += 13 + this.update_I(MacroExecutor.load_List().size()) + 2;
                  } else if ("SCOREBOARD".equals(this.tick_Str("source"))) {
                     int lineCount = autismclient.util.macro.CaptureValueAction.init_List_Minecraft_2(init_Minecraft).size();
                     h += 13 + (lineCount <= 0 ? 24 : Math.min(5, lineCount) * 22) + 2;
                  } else if ("TABLIST".equals(this.tick_Str("source"))) {
                     int matchCount = this.set_I();
                     h += 13 + (matchCount <= 0 ? 24 : Math.min(5, matchCount) * 14) + 2;
                  }

                  h += this.write(this.load_FieldDef_Str("exampleText")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("selectedText")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("itemFilter")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("slot")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("itemText")) ? 16 : 0;
                  h += 60;
                  h += this.write(this.load_FieldDef_Str("numberMode")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("numberModifier")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("numberModifierAmount")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForTrigger")) ? 16 : 0;
                  return h + 24;
               } else if (this.init_MacroAction == null
                  || this.init_MacroAction.getType() != MacroActionType.OPEN_CONTAINER && this.init_MacroAction.getType() != MacroActionType.INTERACT_ENTITY) {
                  if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
                     int h = 4;
                     h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                     h += 47;
                     h += this.write(this.load_FieldDef_Str("blockPos")) ? this.init_I_FieldDef(this.load_FieldDef_Str("blockPos")) + 2 : 0;
                     h += this.write(this.load_FieldDef_Str("manualDirection")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("direction")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("sneak")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("sneakMode")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("interact")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("interactTiming")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("interactCustomMs")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("waitForGuiBefore")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("waitForGuiAfter")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("guiName")) ? 16 : 0;
                     return h + 36;
                  }

                  if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_SLOT_CHANGE) {
                     int h = 4;
                     h += 16;
                     h += 16;
                     h += 76;
                     h += 16;
                     h += 16;
                     return h + 12;
                  }

                  if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_INVENTORY_PREDICATE) {
                     int h = 4;
                     h += this.write(this.load_FieldDef_Str("listenDuringPreviousAction")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("condition")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("itemName")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("count")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("slot")) ? 34 : 0;
                     h += this.write(this.load_FieldDef_Str("timeoutMs")) ? 16 : 0;
                     return h + (this.write(this.load_FieldDef_Str("saveAs")) ? 16 : 0);
                  }

                  if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DELAY_PACKETS) {
                     int h = 4;
                     h += this.write(this.load_FieldDef_Str("mode")) ? 16 : 0;
                     h += this.write(this.load_FieldDef_Str("flushOnDisable")) ? 16 : 0;
                     if ("ENABLE".equals(this.tick_Str("mode"))) {
                        h += 54;
                     }

                     return h + 8;
                  } else {
                     if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.MINE) {
                        int h = 4;
                        h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                        h += this.init_I_FieldDef(this.load_FieldDef_Str("targetBlocks")) + 2 + 4;
                        h += this.write(this.load_FieldDef_Str("stopInventoryFull")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("stopSlotsUsed")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("slotsUsedThreshold")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("stopMinedCount")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("minedCountTarget")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("stopAfterTime")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("timeoutSeconds")) ? 16 : 0;
                        return h + 14;
                     }

                     if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PAY) {
                        int h = 4;
                        h += this.write(this.load_FieldDef_Str("commandTemplate")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("amountInput")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("divideEnabled")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("probeHidden")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("delayEnabled")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("delayMs")) ? 16 : 0;
                        h += 74;
                        h += 18;
                        ThemedTextInputBox paySearch = this.flush_Map.get("players");
                        int payRows = this.state_Z ? Math.max(1, this.load_List_Str(paySearch != null ? paySearch.load_Str() : "").size()) : 1;
                        h += payRows * 13 + 4;
                        return h + 48;
                     }

                     if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.STORE_ITEM) {
                        int h = 4;
                        h += this.write(this.load_FieldDef_Str("mode")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("allItems")) ? 16 : 0;
                        if (!this.load_Map.getOrDefault("allItems", false)) {
                           h += 74;
                           h += 18;
                           h += 82;
                           h += 24;
                        }

                        h += this.write(this.load_FieldDef_Str("persistent")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("delayTicks")) ? 16 : 0;
                        h += this.write(this.load_FieldDef_Str("closeAfter")) ? 16 : 0;
                        return h + (this.write(this.load_FieldDef_Str("closeSendPkt")) ? 16 : 0);
                     } else {
                        if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.TOGGLE_MODULE) {
                           int h = 4;
                           h += 74;
                           h += 18;
                           h += 94;
                           return h + 14;
                        }

                        if (this.push_List != null) {
                           return 148;
                        }

                        if (this.init_PacketUtil != null) {
                           int h = 100;
                           h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                           h += 24;
                           h += 16;
                           h += 16;
                           h += 16;
                           if (this.load_Map.getOrDefault("drop_waitForGuiBefore", false) || this.load_Map.getOrDefault("drop_waitForGuiAfter", false)) {
                              h += 16;
                           }

                           return h;
                        } else if (this.clear_List != null) {
                           boolean filterByUser = this.load_Map.getOrDefault("lan_filterByUser", false);
                           int h = 36;
                           if (!filterByUser || this.clear_List.isEmpty()) {
                              h += 16;
                           }

                           h += 12;
                           if (filterByUser) {
                              if (!this.clear_List.isEmpty()) {
                                 int visibleRows = Math.min(3, Math.max(1, this.clear_List.size()));
                                 h += 13 + visibleRows * 14;
                              } else {
                                 h += 12;
                              }

                              h += 22;
                           }

                           return h;
                        } else {
                           if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET) {
                              return 242;
                           }

                           if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.WAIT_PACKET_MATCH) {
                              int rows = this.reset_List == null ? 1 : Math.max(1, Math.min(4, Math.max(1, this.reset_List.size())));
                              int h = 82 + rows * 14 + 5;
                              if (this.reset_List != null && !this.reset_List.isEmpty()) {
                                 int idx = Math.max(0, Math.min(this.f_z_I, this.reset_List.size() - 1));
                                 autismclient.util.macro.WaitPacketMatchAction.Inner_CachePool rule = this.reset_List.get(idx);
                                 h += 32;
                                 if (rule.load != null && !rule.load.isBlank()) {
                                    h += 16;
                                    if (rule.init_aM_StreamUtil != autismclient.util.macro.WaitPacketMatchAction.Inner_StreamUtil.EXISTS) {
                                       h += 16;
                                    }
                                 }
                              } else {
                                 h += 12;
                              }

                              return h;
                           } else {
                              if (this.init_ActionFieldSchema == null) {
                                 return 0;
                              }

                              int h = 4;

                              for (FieldDef field : this.init_ActionFieldSchema.fields()) {
                                 if (field.type() != CachePool.STRING_LIST && !this.flush_Z(field.key()) && this.write(field)) {
                                    h += this.init_I_FieldDef(field) + 2;
                                 }
                              }

                              for (FieldDef field : this.init_ActionFieldSchema.fields()) {
                                 if (field.type() == CachePool.STRING_LIST && !this.flush_Z(field.key()) && this.write(field)) {
                                    h += this.init_I_FieldDef(field) + 2;
                                 }
                              }

                              if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PACKET) {
                                 h += 52;
                              }

                              if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.DELAY_PACKETS) {
                                 h += 18;
                              }

                              return h;
                           }
                        }
                     }
                  }
               } else {
                  int h = 4;
                  h += this.write(this.load_FieldDef_Str("packetOrder")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("targetMode")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("pos")) ? this.init_I_FieldDef(this.load_FieldDef_Str("pos")) + 2 : 0;
                  h += this.write(this.load_FieldDef_Str("entityTargets")) ? this.init_I_FieldDef(this.load_FieldDef_Str("entityTargets")) + 2 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForGuiBefore")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("waitForGuiAfter")) ? 16 : 0;
                  h += this.write(this.load_FieldDef_Str("guiName")) ? 16 : 0;
                  String targetMode = this.tick_Str("targetMode");
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

   private boolean prev_Z() {
      return this.init_MapStore != null
         && this.f_j_I >= 0
         && this.f_j_I < this.init_MapStore.itemNames.size()
         && this.init_MapStore.init_N_I(this.f_j_I) == N.PICKUP_ALL;
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

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor ctx, String text, int x, int y, int maxW, Identifier font) {
      String trimmed = autismclient.gui.f.b.k.init(this.init_Font, text, maxW - 4, font, -1);
      autismclient.gui.f.b.k.init(
         ctx, this.init_Font, trimmed, font, UiColorPalette.scan(), x, j.init(y, 14, this.init_I_Identifier(font), this.init_CharEncoder.flush()), false
      );
   }

   private int init_I_Identifier(Identifier font) {
      if (autismclient.gui.f.a.DataUtil.read.equals(font)) {
         return this.init_CharEncoder.init_I_l(l.init);
      } else {
         return autismclient.gui.f.a.DataUtil.load.equals(font) ? this.init_CharEncoder.init_I_l(l.load) : this.init_CharEncoder.init_I_l(l.read);
      }
   }

   private int init_I_Identifier_Str(Identifier font, String text) {
      return autismclient.gui.f.b.k.init(this.init_Font, text == null ? "" : text, font, UiColorPalette.scan());
   }

   private int read_I(String text) {
      return this.init_I_Identifier_Str(this.init_CharEncoder.init_Identifier_l(l.read), text);
   }

   private int write_I(String label) {
      return RenderState.load(this.read_I(label));
   }

   private int init_I_StreamUtil(StreamUtil mode, String idleLabel, String capturingLabel) {
      return Math.max(
         this.write_I(autismclient.gui.macro.editor.a.DataUtil.init(mode, false, idleLabel, capturingLabel)),
         this.write_I(autismclient.gui.macro.editor.a.DataUtil.init(mode, true, idleLabel, capturingLabel))
      );
   }

   private int init_I_List_I(List<String> options, int available) {
      int longest = 0;
      if (options != null) {
         for (String option : options) {
            longest = Math.max(longest, this.read_I(option));
         }
      }

      return Math.min(Math.max(available, 0), Math.max(56, longest + 26));
   }

   private int[] init_IArr_I(int x, int totalW, boolean alignRight, String... labels) {
      int n = labels.length;
      int[] out = new int[n * 2];
      int[] cws = new int[n];
      int sum = 0;

      for (int i = 0; i < n; i++) {
         cws[i] = this.write_I(labels[i]);
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

   private int init_I_I(int totalWidth, String label, Identifier font) {
      return this.init_I_I(totalWidth, label, font, 56);
   }

   private int init_I_I(int totalWidth, String label, Identifier font, int minControlWidth) {
      int measured = this.init_I_Identifier_Str(font, label) + 8;
      int availableMax = Math.max(44, totalWidth - Math.max(28, minControlWidth) - 2);
      return Math.max(44, Math.min(measured, availableMax));
   }

   private int flush_I(int x, int labelWidth) {
      return x + labelWidth + 2;
   }

   private int close(int totalWidth, int labelWidth) {
      return Math.max(28, totalWidth - labelWidth - 2);
   }

   private void sync_V(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int color) {
      autismclient.gui.f.PacketUtil.load(ctx, autismclient.gui.f.NetHelper.init(x, y, w, h), color);
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx, int trackX, int trackY, int trackH, int totalCount, int visibleCount, int itemH, int scrollOffset, int mouseX, int mouseY
   ) {
      if (totalCount > visibleCount) {
         int viewPixels = Math.max(1, Math.min(trackH, visibleCount * itemH));
         int contentPixels = Math.max(0, totalCount * itemH);
         PoolEntry.Inner_DataUtil metrics = PoolEntry.init_PoolEntry_DataUtil_I_2(contentPixels, viewPixels, trackX, trackY, 5, trackH, scrollOffset);
         boolean sbDragging = this.init_IntConsumer != null;
         PoolEntry.init(ctx, metrics, metrics.init(mouseX, mouseY), sbDragging);
      }
   }

   private EventBus init_EventBus_Map(Map<String, EventBus> viewports, String key, int x, int y, int width, int height, int rowHeight, int scrollbarWidth) {
      EventBus vp = viewports.get(key);
      if (vp == null || vp.init_I() != x || vp.load_I() != y || vp.read_I() != width || vp.write_I() != height) {
         vp = new EventBus(x, y, width, height, rowHeight, scrollbarWidth);
         viewports.put(key, vp);
      }

      return vp;
   }

   private ThemedTextInputBox init_k_I(int w) {
      return new ThemedTextInputBox(init_Minecraft, this.init_Font, 0, 0, w, 13, false);
   }

   private static String init_Str_D(double v) {
      long lv = (long)v;
      return v == lv ? String.valueOf(lv) : String.valueOf(v);
   }

   private static boolean init_Z_Str_StrArr(String filter, String... candidates) {
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
      if (field.captureMode() == StreamUtil.PACKET_NAME) {
         boolean both = this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PACKET_GATE;
         if (!both) {
            Consumer<Class<? extends Packet<?>>> add = packetClass -> {
               if (packetClass != null && lst != null) {
                  String namex = PacketTypeRegistry.init_Str_Class(packetClass);
                  if (namex == null || namex.isBlank()) {
                     namex = PacketNameResolver.init_Str_Class(packetClass);
                  }

                  if (!lst.contains(namex)) {
                     lst.add(namex);
                  }
               }
            };
            String direction = this.tick_Str("direction");
            if ("ANY".equals(direction)) {
               this.init_AutismPacketSelect.init_V_Consumer(add);
            } else if ("S2C".equals(direction)) {
               this.init_AutismPacketSelect.read(add);
            } else {
               this.init_AutismPacketSelect.load(add);
            }
         } else {
            BiConsumer<Class<? extends Packet<?>>, Boolean> toggle = (packetClass, selected) -> {
               if (packetClass != null && lst != null) {
                  String namex = PacketTypeRegistry.init_Str_Class(packetClass);
                  if (namex == null || namex.isBlank()) {
                     namex = PacketNameResolver.init_Str_Class(packetClass);
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
                  Class<? extends Packet<?>> c = PacketTypeRegistry.init_Class_Str(name);
                  if (c != null && !selected.contains(c)) {
                     selected.add(c);
                  }
               }
            }

            this.init_AutismPacketSelect.read_V_BiConsumer_Collection(toggle, selected);
         }
      } else if (this.put_Z()) {
         this.load_Screen = init_Minecraft.gui.screen();
         this.index();
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
         ck state = ck.init_ck();
         state.read_V_Runnable(() -> this.init_V_Z_Z(true, false));
         if (field.captureMode() == StreamUtil.BLOCK_ID || field.captureMode() == StreamUtil.BLOCK_CATALOG) {
            state.init_V_Consumer(pos -> {
               if (init_Minecraft.level != null) {
                  String id = BuiltInRegistries.BLOCK.getKey(init_Minecraft.level.getBlockState(pos).getBlock()).toString();
                  if (!lst.contains(id)) {
                     lst.add(id);
                  }
               }

               this.init_V_Z_Z(true, false);
            });
         } else if (field.captureMode() == StreamUtil.ENTITY_ID) {
            boolean specificCapture = this.add_Z(field.key());
            boolean replaceOnCapture = this.clear_Z_Str(field.key());
            state.value(specificCapture);
            state.load_V_Consumer(payload -> {
               String value = payload == null ? "" : payload.strip();
               if (!value.isBlank()) {
                  if (replaceOnCapture) {
                     lst.clear();
                  }

                  if (!lst.contains(value)) {
                     lst.add(value);
                  }
               }

               this.init_V_Z_Z(true, false);
            });
         }
      }
   }

   private void init_V_FieldDef(FieldDef field) {
      if (this.put_Z()) {
         if (this.init_MacroAction != null
            && (this.init_MacroAction.getType() == MacroActionType.INSTA_BREAK || this.init_MacroAction.getType() == MacroActionType.BREAK)
            && "blockPos".equals(field.key())) {
            this.B();
         } else {
            this.load_Screen = init_Minecraft.gui.screen();
            this.index();
            String key = field.key();
            boolean dbl = field.xyzDouble();
            init_Minecraft.execute(() -> {
               if (init_Minecraft.gui.screen() != null) {
                  OverlayScreenController.init_V(init_Minecraft, null);
               }
            });
            ck state = ck.init_ck();
            state.state(this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE);
            state.read_V_Runnable(() -> {
               state.state(false);
               this.init_V_Z_Z(true, false);
            });
            if (this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.PLACE) {
               state.init_V_BiConsumer((pos, face) -> {
                  Direction useFace = face == null ? Direction.UP : face;
                  BlockPos targetPos = pos.relative(useFace);
                  ThemedTextInputBox fx = this.init_Map.get(key + "_0");
                  ThemedTextInputBox fy = this.init_Map.get(key + "_1");
                  ThemedTextInputBox fz = this.init_Map.get(key + "_2");
                  if (fx != null) {
                     fx.init_V_Str(dbl ? init_Str_D(targetPos.getX()) : String.valueOf(targetPos.getX()));
                  }

                  if (fy != null) {
                     fy.init_V_Str(dbl ? init_Str_D(targetPos.getY()) : String.valueOf(targetPos.getY()));
                  }

                  if (fz != null) {
                     fz.init_V_Str(dbl ? init_Str_D(targetPos.getZ()) : String.valueOf(targetPos.getZ()));
                  }

                  state.state(false);
                  this.init_V_Z_Z(true, false);
               });
            } else {
               state.init_V_Consumer(pos -> {
                  ThemedTextInputBox fx = this.init_Map.get(key + "_0");
                  ThemedTextInputBox fy = this.init_Map.get(key + "_1");
                  ThemedTextInputBox fz = this.init_Map.get(key + "_2");
                  if (fx != null) {
                     fx.init_V_Str(dbl ? init_Str_D(pos.getX()) : String.valueOf(pos.getX()));
                  }

                  if (fy != null) {
                     fy.init_V_Str(dbl ? init_Str_D(pos.getY()) : String.valueOf(pos.getY()));
                  }

                  if (fz != null) {
                     fz.init_V_Str(dbl ? init_Str_D(pos.getZ()) : String.valueOf(pos.getZ()));
                  }

                  this.init_V_Z_Z(true, false);
               });
            }
         }
      }
   }

   private void B() {
      if (this.put_Z()) {
         this.load_Screen = init_Minecraft.gui.screen();
         this.index();
         init_Minecraft.execute(() -> {
            if (init_Minecraft.gui.screen() != null) {
               OverlayScreenController.init_V(init_Minecraft, null);
            }
         });
         ck state = ck.init_ck();
         state.read_V_Runnable(() -> this.init_V_Z_Z(true, false));
         state.init_V_BiConsumer((pos, direction) -> {
            this.init_V_Str_BlockPos("blockPos", pos);
            this.read_Map.put("direction", direction == null ? 1 : direction.ordinal());
            AutismClientMessaging.sendPrefixed("InstaBreak target captured");
            this.init_V_Z_Z(true, false);
         });
         AutismClientMessaging.sendPrefixed("InstaBreak: right-click the target block to capture it");
      }
   }

   private static List<String> pop_List() {
      if (init_List == null) {
         init_List = new ArrayList<>();

         for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            String path = id.getPath();
            if (!path.endsWith("_wall_head")
               && !path.endsWith("_wall_sign")
               && !path.endsWith("_wall_banner")
               && !path.endsWith("_wall_torch")
               && !path.endsWith("_wall_skull")) {
               init_List.add(id.toString());
            }
         }

         Collections.sort(init_List);
      }

      return init_List;
   }

   private static String init_Str_StreamGate(MacroActionType type) {
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

   private String clear_Str() {
      String activeCaptureKey = this.init_StreamUtil.init_Str();
      if (!"_item_entries".equals(activeCaptureKey) && !"_drop_entries".equals(activeCaptureKey) && !"_wsc_entries".equals(activeCaptureKey)) {
         FieldDef field = this.read_FieldDef(activeCaptureKey);
         return field != null ? field.label() : "Slot";
      } else {
         return "Slot + Item";
      }
   }

   private String reset_Str() {
      return this.init_MacroAction != null ? this.start_Str() : "Action";
   }

   private boolean isLegacyMulti() {
      return this.init_MacroAction != null && this.init_MacroAction.getType() == MacroActionType.MULTI;
   }

   private String start_Str() {
      if (this.init_MacroAction == null) {
         return "Edit Action";
      }

      String title = this.init_MacroAction.getType() != null ? init_Str_StreamGate(this.init_MacroAction.getType()) : this.init_MacroAction.getDisplayName();
      return this.f_d_I > 1 ? title + " x" + this.f_d_I : title;
   }

   private FieldDef read_FieldDef(String key) {
      if (this.init_ActionFieldSchema != null && key != null) {
         for (FieldDef field : this.init_ActionFieldSchema.fields()) {
            if (key.equals(field.key())) {
               return field;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private record ConfigStore(long init_J, PayloadFieldCodec.Inner_JsonUtil init_bJ_JsonUtil, PayloadFieldCodec.Inner_DataUtil init_bJ_DataUtil) {
   }

   private record Inner_BufferHelper(boolean init_Z, String init_Str, String load, List<String> init_List) {
      static ActionEditorOverlay.Inner_BufferHelper init(String prefix, List<String> members) {
         return new ActionEditorOverlay.Inner_BufferHelper(true, prefix, null, members);
      }

      static ActionEditorOverlay.Inner_BufferHelper init(String prefix, String name) {
         return new ActionEditorOverlay.Inner_BufferHelper(false, prefix, name, null);
      }
   }

   @FunctionalInterface
   private interface Inner_CachePool {
      boolean fire(double var1, double var3, int var5);
   }

   private record Inner_DataUtil(boolean init_Z, String init_Str, int init_I) {
   }

   private static final class Inner_StreamUtil {
      private final int init_I;
      private final int load;
      private final int read;
      private final int write;
      private final ActionEditorOverlay.Inner_CachePool init_ActionEditorOverla;

      private Inner_StreamUtil(int x, int y, int w, int h, Runnable action) {
         this(x, y, w, h, (mx, my, mouseButton) -> {
            if (mouseButton != 0) {
               return false;
            }

            action.run();
            return true;
         });
      }

      private Inner_StreamUtil(ByteReader button, Runnable action) {
         this(button.init_I(), button.load(), button.read(), button.write(), (mx, my, mouseButton) -> ByteReader.init(button, mx, my, mouseButton));
      }

      private Inner_StreamUtil(int x, int y, int w, int h, ActionEditorOverlay.Inner_CachePool action) {
         this.init_I = x;
         this.load = y;
         this.read = w;
         this.write = h;
         this.init_ActionEditorOverla = action;
      }

      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }

      boolean init(double mx, double my, int mouseButton) {
         return this.init_ActionEditorOverla != null && this.init_ActionEditorOverla.fire(mx, my, mouseButton);
      }
   }

   private record JsonUtil(String init_Str, byte[] init_BArr, boolean init_Z, int init_I, ActionEditorOverlay.TaskQueue init_ActionEditorOverla, String load) {
      private JsonUtil {
         init_BArr = init_BArr == null ? new byte[0] : (byte[])init_BArr.clone();
         init_ActionEditorOverla = init_ActionEditorOverla == null ? ActionEditorOverlay.TaskQueue.BINARY_REPLAY : init_ActionEditorOverla;
         load = load == null ? "" : load;
      }

      public byte[] init_BArr() {
         return (byte[])this.init_BArr.clone();
      }
   }

   private enum NetHelper {
      SCRIPT,
      HEX,
      UTF8,
      LOGICAL,
      PREVIEW_ONLY;
   }

   private record StateCache(ActionEditorOverlay.NetHelper init_ActionEditorOverla, String init_Str) {
   }

   private record SyncManager(int init_I, int load, int read, int write, IntConsumer init_IntConsumer) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum TaskQueue {
      UTF8_TEXT,
      BRAND_STRING,
      COMMAND_INT,
      BINARY_REPLAY;
   }
}
