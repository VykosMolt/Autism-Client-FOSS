package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroPresetRegistry;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.gui.f.b.SearchFilterList;
import autismclient.gui.f.b.TooltipRenderer;
import autismclient.gui.f.b.UiLayoutMath;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.DynamicBindingManager;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class AutismMacroEditorOverlay extends DraggableOverlayWindow {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static AutismMacroEditorOverlay init_AutismMacroEditorO_5;
   private static final int flush_I = 30;
   private static final int close_I = 12;
   private static final int open_I = 10;
   private static final int poll_I = 3;
   private static final float init_F_2 = 3.0F;
   private static final int push_I = 3;
   private static final float load_F = 0.6F;
   private static final int pop_I = 110;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.c.CollapsibleWindowNode init_CharEncoder_2 = new autismclient.gui.f.c.CollapsibleWindowNode("Macro Editor");
   private final autismclient.gui.f.c.LayoutTreeHost init_TextFormat = new autismclient.gui.f.c.LayoutTreeHost(this.init_CharEncoder, this.init_CharEncoder_2);
   private final autismclient.gui.f.c.VerticalSpacerNode init_BitSetUtil = new autismclient.gui.f.c.VerticalSpacerNode();
   private boolean write_Z = false;
   private AutismMacro init_aM;
   private AutismMacro load_aM;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private AutismMacroEditorOverlay.Inner_DataUtil init_AutismMacroEditorO = AutismMacroEditorOverlay.Inner_DataUtil.NORMAL;
   private Consumer<AutismMacro> init_Consumer = null;
   private int tick_I;
   private int run_I;
   private int get_I = 258;
   private int set_I = 308;
   private List<String> init_List = new ArrayList<>();
   private List<int[]> load_List = new ArrayList<>();
   private List<Runnable> read_List = new ArrayList<>();
   private List<Runnable> write_List = new ArrayList<>();
   private List<Boolean> update_List = new ArrayList<>();
   private int put_I = -1;
   private final List<autismclient.gui.f.b.DropdownWidget> sync_List = new ArrayList<>();
   private ThemedTextInputBox init_k;
   private ThemedTextInputBox load_k;
   private boolean flush_Z = false;
   private int add_I = 0;
   private int clear_I = 0;
   private static final float read_F = 3.0F;
   private static final long init_J = 350L;
   private static final int reset_I = 18;
   private static final int start = 12;
   private int stop = -1;
   private int next = -1;
   private int prev = -1;
   private int valid = 0;
   private int size = 0;
   private double init_D = 0.0;
   private double load_D = 0.0;
   private boolean close_Z = false;
   private boolean open_Z = false;
   private int data = -1;
   private long load_J = 0L;
   private boolean poll_Z = false;
   private double read_D = 0.0;
   private double write_D = 0.0;
   private double update_D = 0.0;
   private double sync_D = 0.0;
   private int value = 0;
   private int cache = 0;
   private boolean push_Z = false;
   private boolean pop_Z = false;
   private static final int state = 8;
   private static final int queue = 14;
   private String init_Str_2 = "";
   private List<String> flush_List = null;
   private AutismMacroEditorOverlay.Inner_JsonUtil init_AutismMacroEditorO_3 = null;
   private List<AutismMacroEditorOverlay.Inner_TaskQueue> close_List = Collections.emptyList();
   private List<AutismMacroEditorOverlay.Inner_NetHelper> open_List = Collections.emptyList();
   private Map<String, List<AutismMacroEditorOverlay.Inner_NetHelper>> init_Map = Collections.emptyMap();
   private final SearchFilterList<AutismMacroEditorOverlay.Inner_NetHelper> init_a = new SearchFilterList<>(entry -> entry.init_Str + "\n" + entry.load + "\n" + entry.read);
   private ThemedTextInputBox read_k;
   private final AutismMacroEditorOverlay.Inner_StateCache init_AutismMacroEditorO_4 = new AutismMacroEditorOverlay.Inner_StateCache();
   private int buffer = 0;
   private final HashMap<String, Integer> init_HashMap = new HashMap<>();
   private long read_J = 0L;
   private long write_J = -1L;
   private int entry = -1;
   private int node = -1;
   private int task = -1;
   private int pool = 0;
   private int index = 0;
   private final OverlayContextMenu<Integer> init_F_3;
   private static final int count = 0;
   private static final int flags = 1;
   private static final int level = 2;
   private static final float write_F = 0.14F;
   private static final float update_F = 0.14F;
   private static final float sync_F = 0.12F;
   private static final int frame = 18;
   private static final int block = 20;
   private String load_Str = null;
   private int chunk;
   private int table;
   private final Deque<AutismMacroEditorOverlay.Inner_CachePool> init_Deque = new ArrayDeque<>();
   private final Deque<AutismMacroEditorOverlay.Inner_CachePool> load_Deque = new ArrayDeque<>();
   private AutismMacro read_aM;
   private boolean tick_Z = false;
   private boolean run_Z = false;
   private CompoundTag init_CompoundTag = null;
   private boolean get_Z = false;
   private boolean set_Z = false;
   private long update_J = -1L;
   private long sync_J = 0L;
   private static final long flush_J = 100L;
   private long close_J = -9223372036854775808L;
   private long open_J = -9223372036854775808L;
   private boolean put_Z = false;
   private boolean add_Z = false;
   private final IdentityHashMap<MacroAction, Integer> init_IdentityHashMap = new IdentityHashMap<>();
   private final AutismMacroEditorOverlay.Inner_FileCache init_AutismMacroEditorO_2 = new AutismMacroEditorOverlay.Inner_FileCache();
   private Integer init_Integer;
   private final List<AutismMacroEditorOverlay.Inner_EventBus> poll_List = new ArrayList<>();
   private final List<AutismMacroEditorOverlay.Inner_UiHelper> push_List = new ArrayList<>();
   private int store = 1;
   private long poll_J = System.nanoTime();
   private static final int drain = -5609729;
   private static final int fill = 88;

   public static AutismMacroEditorOverlay init_AutismMacroEditorO_2() {
      if (init_AutismMacroEditorO_5 == null) {
         init_AutismMacroEditorO_5 = new AutismMacroEditorOverlay(Minecraft.getInstance().font);
         init_AutismMacroEditorO_5.b__V();
         OverlayManager.init_bk().init_V(init_AutismMacroEditorO_5);
      }

      init_AutismMacroEditorO_5.sync_V_9();
      return init_AutismMacroEditorO_5;
   }

   public void load_V_Z_3(boolean configurationOnly) {
      if (this.set_Z != configurationOnly) {
         this.set_Z = configurationOnly;
         ActionEditorOverlay actionEditor = ActionEditorOverlay.load_ActionEditorOverla();
         if (actionEditor != null) {
            actionEditor.read_V(configurationOnly);
            actionEditor.load_V_Z(this.cache_Z());
         }

         if (this.write_Z) {
            this.bind_V();
         }
      }
   }

   private autismclient.gui.f.b.OverlayActionButton init(
      int x, int y, int w, int h, String label, autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant, boolean enabled, Runnable action
   ) {
      return this.init(x, y, w, h, label, variant, enabled, action, null);
   }

   private autismclient.gui.f.b.OverlayActionButton init(
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant,
      boolean enabled,
      Runnable action,
      Runnable secondaryAction
   ) {
      autismclient.gui.f.b.OverlayActionButton button = autismclient.gui.f.b.OverlayActionButton.init(x, y, w, h, Component.literal(label), ignored -> {
         if (action != null) {
            action.run();
         }
      }, secondaryAction == null ? null : ignored -> secondaryAction.run());
      button.read(w);
      button.init_ByteReader_ByteReader_StreamU(variant);
      button.init_Z = enabled;
      return button;
   }

   private void init(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      autismclient.gui.f.b.ButtonDrawHelper.init(context, this.init_Font, x, y, w, h, label, variant, enabled, mouseX, mouseY);
   }

   private void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean toggleState, String animationKey, boolean enabled, int mouseX, int mouseY
   ) {
      autismclient.gui.f.b.ButtonDrawHelper.init(context, this.init_Font, x, y, w, h, label, toggleState, animationKey, enabled, mouseX, mouseY);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int w, int h, boolean running, boolean enabled, int mouseX, int mouseY) {
      boolean hovered = enabled && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
      autismclient.gui.f.b.CachePool.init(
         autismclient.gui.f.UiHelper.init(context, this.init_Font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(x, y, w, h),
         "",
         running ? autismclient.gui.f.b.CachePool.Inner_DataUtil.update : autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
         hovered,
         true
      );
      int icon = 8;
      GuiIconAtlas.init(context, running ? GuiIconAtlas.close : GuiIconAtlas.flush, x + (w - icon) / 2, y + (h - icon) / 2, icon, enabled ? -218103809 : 1728053247);
   }

   private void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      Identifier iconId,
      autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      autismclient.gui.f.b.ButtonDrawHelper.init(context, this.init_Font, x, y, w, h, "", variant, enabled, mouseX, mouseY);
      int icon = 8;
      int gap = 3;
      int textW = autismclient.gui.f.b.k.init(
         this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
      );
      int groupW = icon + gap + textW;
      int startX = x + Math.max(2, (w - groupW) / 2);
      GuiIconAtlas.init(context, iconId, startX, y + (h - icon) / 2, icon, enabled ? -218103809 : 1728053247);
      autismclient.gui.f.b.k.init(
         context,
         this.init_Font,
         label,
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         enabled ? this.init_CharEncoder.load(autismclient.gui.f.b.l.read) : this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
         startX + icon + gap,
         UiLayoutMath.init(y, h, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush()),
         false
      );
   }

   public AutismMacroEditorOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.init_F_3 = new OverlayContextMenu<>(this.init_CharEncoder, textRenderer, this::init_StrArr_Integer, 12);
      this.init_V_F(this.init_F_3);
      this.get_I = this.buffer_I();
      this.set_I = this.entry_I();
      this.init_CharEncoder_2.flush(false);
      this.init_CharEncoder_2.init(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_2.init_CharEncoder_Z_Z(true, true);
      this.init_CharEncoder_2.init_CharEncoder_I_I(this.task_I(), this.pool_I());
      this.init_CharEncoder_2.init_PacketUtil().init(0).init(autismclient.gui.f.c.EdgeInsets.init_IoWorker);
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil);
   }

   private int push_I_14() {
      return this.o() + this.index_I();
   }

   private int pop_I_14() {
      return this.push_I_14() + this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1) + this.count_I();
   }

   private int tick_I_12() {
      return this.pop_I_14() + this.bind_I() + this.flags_I();
   }

   private int run_I_12() {
      return this.tick_I_12() + this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1) + 1;
   }

   private int get_I_10() {
      return this.tick_I + this.store_I();
   }

   private int set_I_10() {
      return this.get_I - this.store_I() * 2;
   }

   private int put_I_4() {
      return this.get_I_10() + 1;
   }

   private int init_I_MacroAction_2(MacroAction action) {
      return this.init_I_MacroAction(action, true, true, true);
   }

   private int init_I_MacroAction_Z(MacroAction action, boolean allowDelete) {
      return this.init_I_MacroAction(action, true, true, allowDelete);
   }

   private int init_I_MacroAction(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int count = 0;
      if (allowMove) {
         count += 2;
      }

      if (allowDuplicate) {
         count++;
      }

      if (action != null && this.read_Z(action)) {
         count++;
      }

      if (allowDelete) {
         count++;
      }

      return count <= 0 ? 0 : count * this.level_I() + (count - 1) * this.frame_I();
   }

   private int load_I_MacroAction(MacroAction action) {
      return this.chunk_I() - this.init_I_MacroAction_2(action) - 1;
   }

   private int load_I_MacroAction_Z(MacroAction action, boolean allowDelete) {
      return this.chunk_I() - this.init_I_MacroAction_Z(action, allowDelete) - 1;
   }

   private int load(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int width = this.init_I_MacroAction(action, allowMove, allowDuplicate, allowDelete);
      return width <= 0 ? this.chunk_I() : this.chunk_I() - width - 1;
   }

   private int add_I_10() {
      return this.run_I_12() + this.clear_I_4() + this.fill_I();
   }

   private int clear_I_4() {
      return Math.max(0, this.reset_I_3() + 2);
   }

   private int reset_I_3() {
      int rowH = this.mark_I();
      int rows = this.init_aM == null ? 0 : this.init_aM.init_List.size();
      int autoList = rowH * Math.max(3, Math.min(rows, this.next_I_3()));
      if (!this.set_Z()) {
         return autoList;
      }

      int height = this.load_I_I_I(this.start_I_9() + autoList, this.a__I());
      return Math.max(rowH * 3, height - this.start_I_9());
   }

   private int start_I_9() {
      return this.run_I_12() - this.run_I + 2 + this.fill_I() + this.bind_I() + this.drain_I();
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected int a__I() {
      return this.start_I_9() + this.mark_I() * 3;
   }

   private int stop_I_3() {
      int content = this.valid_I();
      return this.init_I_I_I(content, content);
   }

   private int next_I_3() {
      int screenH = init_Minecraft.getWindow() != null ? cA.load_I() : 360;
      int chrome = this.start_I_9();
      int cap = Math.min((int)(screenH * 0.6F), screenH - 8 - chrome);
      return Math.max(3, cap / Math.max(1, this.mark_I()));
   }

   private int prev_I_2() {
      return this.add_I_10() + this.bind_I() + this.drain_I() - this.run_I;
   }

   private int valid_I() {
      if (this.init_Font == null) {
         return 240;
      }

      int gap = this.a_I();
      int topRow = this.load_I_Str_3("Press Key/Mouse...") + gap + this.f_I() + gap + this.size_I();
      if (this.add_I == 1) {
         topRow += this.h_I() + gap;
      }

      if (!this.set_Z && !this.update_Z && this.init_aM != null && LanSyncManager.init_aE().write_Z()) {
         topRow += this.g_I() + gap;
      }

      int builderRow = this.init_I_Str_4("Add Action") + this.init_I_Str_4("Add Conditional") + this.read_I_2("Presets") + this.data_I() * 2;
      int footerRow = this.read_I_2("Save")
         + this.read_I_2("Cancel")
         + this.read_I_2("Undo")
         + this.read_I_2("Redo")
         + this.value_I() * 3
         + 4
         + autismclient.gui.f.b.k.init(this.init_Font, "Name Required", this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0);
      int stepRow = 140 + this.init_I_MacroAction(null, true, true, true) + this.u() + 1 + this.block_I() + 2;
      int title = autismclient.gui.f.b.k.init(
            this.init_Font, "Create Macro [RUN] (000 steps)", this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load), 0
         )
         + this.task_I()
         + this.pool_I()
         - this.store_I() * 2;
      int content = Math.max(Math.max(topRow, builderRow), Math.max(Math.max(footerRow, stepRow), title));
      return content + this.store_I() * 2;
   }

   private int init_I_Str_4(String label) {
      String shown = label.startsWith("Add ") ? label.substring(4) : label;
      return autismclient.gui.f.UiDensityMetrics.load(
         autismclient.gui.f.b.k.init(this.init_Font, shown, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0) + 11
      );
   }

   private int load_I_Str_3(String label) {
      return autismclient.gui.f.UiDensityMetrics.load(
         autismclient.gui.f.b.k.init(
            this.init_Font,
            label,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
         )
      );
   }

   private int size_I() {
      int loopTextW = Math.max(
         autismclient.gui.f.b.k.init(this.init_Font, "Loop Inf", this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0),
         autismclient.gui.f.b.k.init(this.init_Font, "Loop #", this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
      return Math.max(40, loopTextW + 22);
   }

   private int data_I() {
      return 2;
   }

   private int value_I() {
      return 2;
   }

   private int cache_I() {
      return this.run_I_12() + 1;
   }

   private int state_I() {
      return this.cache_I() + this.reset_I_3();
   }

   private int queue_I() {
      return this.init_aM == null ? 0 : Math.max(0, this.init_aM.init_List.size() * this.mark_I() - this.reset_I_3());
   }

   private boolean init_Z_I_2(int rowY) {
      return rowY >= this.run_I_12() && rowY + this.mark_I() <= this.state_I();
   }

   private int buffer_I() {
      return this.valid_I();
   }

   private int entry_I() {
      return this.prev_I_2();
   }

   private int node_I() {
      return this.prev_I_2();
   }

   private int task_I() {
      return 10;
   }

   private int pool_I() {
      return 39;
   }

   private int index_I() {
      return 4;
   }

   private int count_I() {
      return 1;
   }

   private int flags_I() {
      return 4;
   }

   private int level_I() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int frame_I() {
      return 2;
   }

   private int block_I() {
      return 6;
   }

   private int chunk_I() {
      return this.get_I_10() + this.set_I_10() - this.block_I() - 1;
   }

   private int read_I(MacroAction action) {
      int buttonsX = this.load_I_MacroAction(action);
      return buttonsX - 1;
   }

   private int read_I_MacroAction_Z(MacroAction action, boolean allowDelete) {
      int buttonsX = this.load_I_MacroAction_Z(action, allowDelete);
      return buttonsX - 1;
   }

   private int read(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int width = this.init_I_MacroAction(action, allowMove, allowDuplicate, allowDelete);
      if (width <= 0) {
         return this.chunk_I();
      }

      int buttonsX = this.load(action, allowMove, allowDuplicate, allowDelete);
      return buttonsX - 1;
   }

   private int table_I() {
      return this.get_I_10() + this.set_I_10() - 4;
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_4() {
      this.clear_V_4();
      int listHeight = this.clear_I_4();
      int viewHeight = Math.max(1, this.reset_I_3());
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
         this.init_aM.init_List.size() * this.mark_I(), viewHeight, this.table_I(), this.cache_I(), 3, Math.max(1, listHeight - 2), this.clear_I
      );
   }

   private int store_I() {
      return 4;
   }

   private int drain_I() {
      return 4;
   }

   private int fill_I() {
      return 4;
   }

   private int mark_I() {
      return autismclient.gui.f.UiDensityMetrics.init(14) + 2;
   }

   private int scan_I() {
      return 16;
   }

   private int parse_I() {
      return 30;
   }

   private int bind_I() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int a_I() {
      return 2;
   }

   private int b_I() {
      return this.load_I_Str_3("Delete");
   }

   private int c_I() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int d_I() {
      return this.c_I() + 2;
   }

   private int e_I() {
      return 10;
   }

   private int read_I_2(String label) {
      return autismclient.gui.f.UiDensityMetrics.load(
         autismclient.gui.f.b.k.init(this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
   }

   private int f_I() {
      return this.bind_I();
   }

   private String init_Str_Z_3(boolean localRunning) {
      return localRunning ? "Stop" : "Run";
   }

   private int g_I() {
      return 28;
   }

   private int h_I() {
      return 36;
   }

   private int i_I() {
      return this.bind_I();
   }

   private int j_I() {
      return 2;
   }

   private int k() {
      return 49;
   }

   private int l() {
      return 44;
   }

   private int m() {
      return 3;
   }

   private int n() {
      return this.init_CharEncoder.load() + 4;
   }

   private int o() {
      return this.run_I + this.n() + this.d_I() + this.bind_I();
   }

   private int p() {
      return 32;
   }

   private int q() {
      return 3;
   }

   private int r() {
      return 9;
   }

   private int s() {
      return 7;
   }

   private int t() {
      return 5;
   }

   private int u() {
      return this.level_I() + this.frame_I();
   }

   private int init_I_I_6(int rowY) {
      return UiLayoutMath.init(rowY, this.mark_I(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
   }

   private int load_I_I_3(int rowY) {
      return UiLayoutMath.load(rowY, this.mark_I(), this.level_I());
   }

   private int write_I(MacroAction action) {
      if (action == null) {
         return -1;
      }

      Integer existing = this.init_IdentityHashMap.get(action);
      if (existing != null) {
         return existing;
      }

      int created = this.store++;
      this.init_IdentityHashMap.put(action, created);
      return created;
   }

   private AutismMacroEditorOverlay.Inner_TextFormat init_AutismMacroEditorO_I_2(int index) {
      if (this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()) {
         MacroAction action = this.init_aM.init_List.get(index);
         AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(index, action);
         if (group.write && !group.init_Z) {
            return new AutismMacroEditorOverlay.Inner_TextFormat(index, index + 1);
         }

         int start = group.init() ? group.init_I : index;
         int end = group.init() ? group.init_I + group.load_I + 1 : index + 1;
         start = Math.max(0, Math.min(start, this.init_aM.init_List.size()));
         end = Math.max(start, Math.min(end, this.init_aM.init_List.size()));
         return new AutismMacroEditorOverlay.Inner_TextFormat(start, end);
      } else {
         return AutismMacroEditorOverlay.Inner_TextFormat.init_AutismMacroEditorO;
      }
   }

   private void init_V_I_6(int index) {
      this.init_AutismMacroEditorO_2.init_V();
      this.read_V_2(index);
   }

   private void load_V_I_2(int index) {
      AutismMacroEditorOverlay.Inner_TextFormat unit = this.init_AutismMacroEditorO_I_2(index);
      if (!unit.init()) {
         boolean allSelected = true;

         for (int i = unit.init_I; i < unit.load; i++) {
            if (!this.init_AutismMacroEditorO_2.init_Z(this.write_I(this.init_aM.init_List.get(i)))) {
               allSelected = false;
               break;
            }
         }

         if (allSelected) {
            for (int i = unit.init_I; i < unit.load; i++) {
               this.init_AutismMacroEditorO_2.load(this.write_I(this.init_aM.init_List.get(i)));
            }
         } else {
            for (int i = unit.init_I; i < unit.load; i++) {
               this.init_AutismMacroEditorO_2.init_V(this.write_I(this.init_aM.init_List.get(i)));
            }
         }
      }
   }

   private boolean load_Z_I(int index) {
      if (this.init_aM != null && this.init_Integer != null) {
         int anchorIndex = -1;

         for (int i = 0; i < this.init_aM.init_List.size(); i++) {
            if (this.write_I(this.init_aM.init_List.get(i)) == this.init_Integer) {
               anchorIndex = i;
               break;
            }
         }

         if (anchorIndex < 0) {
            return false;
         }

         this.init_AutismMacroEditorO_2.init_V();

         for (int i = Math.min(anchorIndex, index); i <= Math.max(anchorIndex, index); i++) {
            this.read_V_2(i);
         }

         return true;
      } else {
         return false;
      }
   }

   private void init_V_I_Z_2(int index, boolean wasSelectedAtPress) {
      if (!wasSelectedAtPress && !this.read_Z(index)) {
         this.init_V_I_6(index);
      } else {
         this.load_V_I_2(index);
      }
   }

   private void read_V_2(int index) {
      AutismMacroEditorOverlay.Inner_TextFormat unit = this.init_AutismMacroEditorO_I_2(index);
      if (!unit.init() && this.init_aM != null) {
         for (int i = unit.init_I; i < unit.load; i++) {
            this.init_AutismMacroEditorO_2.init_V(this.write_I(this.init_aM.init_List.get(i)));
         }
      }
   }

   private boolean read_Z(int index) {
      return this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()
         ? this.init_AutismMacroEditorO_2.init_Z(this.write_I(this.init_aM.init_List.get(index)))
         : false;
   }

   private void set_V_5() {
      this.init_AutismMacroEditorO_2.init_V();
   }

   private List<AutismMacroEditorOverlay.Inner_TextFormat> init_List_6() {
      List<AutismMacroEditorOverlay.Inner_TextFormat> ranges = new ArrayList<>();
      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         for (int i = 0; i < this.init_aM.init_List.size(); i++) {
            if (this.read_Z(i)) {
               AutismMacroEditorOverlay.Inner_TextFormat unit = this.init_AutismMacroEditorO_I_2(i);
               if (!unit.init()) {
                  ranges.add(unit);
               }
            }
         }

         return init_List_List_2(ranges);
      } else {
         return ranges;
      }
   }

   private static List<AutismMacroEditorOverlay.Inner_TextFormat> init_List_List_2(List<AutismMacroEditorOverlay.Inner_TextFormat> units) {
      if (units != null && !units.isEmpty()) {
         List<AutismMacroEditorOverlay.Inner_TextFormat> sorted = new ArrayList<>(units);
         sorted.sort((a, b) -> Integer.compare(a.init_I, b.init_I));
         List<AutismMacroEditorOverlay.Inner_TextFormat> merged = new ArrayList<>();

         for (AutismMacroEditorOverlay.Inner_TextFormat unit : sorted) {
            if (unit != null && !unit.init()) {
               if (merged.isEmpty()) {
                  merged.add(unit);
               } else {
                  AutismMacroEditorOverlay.Inner_TextFormat previous = merged.get(merged.size() - 1);
                  if (unit.init_I <= previous.load) {
                     merged.set(merged.size() - 1, new AutismMacroEditorOverlay.Inner_TextFormat(previous.init_I, Math.max(previous.load, unit.load)));
                  } else {
                     merged.add(unit);
                  }
               }
            }
         }

         return merged;
      } else {
         return List.of();
      }
   }

   private List<MacroAction> load_List_3() {
      List<MacroAction> out = new ArrayList<>();
      if (this.init_aM == null) {
         return out;
      }

      for (AutismMacroEditorOverlay.Inner_TextFormat unit : this.init_List_6()) {
         for (int i = unit.init_I; i < unit.load && i < this.init_aM.init_List.size(); i++) {
            out.add(this.init_aM.init_List.get(i));
         }
      }

      return out;
   }

   private List<MacroAction> read_List_2() {
      List<MacroAction> out = new ArrayList<>();
      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         for (MacroAction action : this.init_aM.init_List) {
            if (this.init_AutismMacroEditorO_2.init_Z(this.write_I(action))) {
               out.add(action);
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private CompoundTag init_CompoundTag_MacroAction(MacroAction action) {
      if (action == null) {
         return null;
      }

      try {
         return AutismMacro.init_CompoundTag_MacroAction(action);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private boolean init_Z_MacroAction_CompoundTag(MacroAction action, CompoundTag expected) {
      CompoundTag actual = this.init_CompoundTag_MacroAction(action);
      return actual != null && expected != null && actual.equals(expected);
   }

   private void write_V_2(int index) {
      if (this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()) {
         CompoundTag expected = this.init_CompoundTag_MacroAction(this.init_aM.init_List.get(index));
         if (expected != null) {
            this.init_AutismMacroEditorO_2.init_V();

            for (MacroAction candidate : this.init_aM.init_List) {
               if (this.init_Z_MacroAction_CompoundTag(candidate, expected)) {
                  this.init_AutismMacroEditorO_2.init_V(this.write_I(candidate));
               }
            }
         }
      }
   }

   private void init_V_I(int index, int mouseX, int mouseY) {
      if (this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()) {
         int stableId = this.write_I(this.init_aM.init_List.get(index));
         long now = System.currentTimeMillis();
         boolean doubleClick = stableId == this.data && now - this.load_J >= 0L && now - this.load_J <= 350L;
         if (doubleClick) {
            this.init_F_3.init_V();
            this.write_V_2(index);
            this.data = -1;
            this.load_J = 0L;
         } else {
            if (!this.read_Z(index)) {
               this.init_V_I_6(index);
            }

            this.data = stableId;
            this.load_J = now;
            this.init_F_3.init(mouseX, mouseY, index);
         }
      }
   }

   private String[] init_StrArr_Integer(Integer index) {
      return index != null && this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()
         ? new String[]{
            "Play From Here",
            "Play This Step",
            "Select Similar",
            "Delete Similar",
            "Copy\tCtrl+C",
            "Paste\tCtrl+V",
            "Duplicate\tCtrl+D",
            "Delete\tDel",
            "Undo\tCtrl+Z",
            "Redo\tCtrl+R"
         }
         : new String[0];
   }

   private void read_V_I_I_2(int stepIndex, int itemIndex) {
      switch (itemIndex) {
         case 0:
            this.load_V_I_Z(stepIndex, false);
            break;
         case 1:
            this.load_V_I_Z(stepIndex, true);
            break;
         case 2:
            this.write_V_2(stepIndex);
            break;
         case 3:
            this.write_V_2(stepIndex);
            this.prev_Z_2();
            break;
         case 4:
            this.clear_Z_2();
            break;
         case 5:
            this.reset_Z_2();
            break;
         case 6:
            this.next_Z_2();
            break;
         case 7:
            this.prev_Z_2();
            break;
         case 8:
            this.flags_V();
            break;
         case 9:
            this.level_V();
      }
   }

   private void load_V_I_Z(int startIndex, boolean singleStep) {
      if (this.init_aM != null && startIndex >= 0 && startIndex < this.init_aM.init_List.size()) {
         int endExclusive = this.init_aM.init_List.size();
         if (singleStep) {
            AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(startIndex, this.init_aM.init_List.get(startIndex));
            endExclusive = group.init() && group.init_Z ? Math.min(this.init_aM.init_List.size(), startIndex + 1 + group.load_I) : startIndex + 1;
         }

         for (int i = startIndex; i < endExclusive; i++) {
            if (this.init_aM.init_List.get(i) instanceof autismclient.util.macro.InventoryAuditAction auditAction && !auditAction.init_Z_2()) {
               AutismClientMessaging.sendPrefixed("Inventory Audit needs at least one target.");
               return;
            }
         }

         this.f_V();
         AutismMacro oneShot = new AutismMacro();
         oneShot.init_Str = this.init_aM.init_Str != null && !this.init_aM.init_Str.isBlank() ? this.init_aM.init_Str : "(Unsaved)";
         oneShot.init_Z = false;
         oneShot.init_I = -1;

         for (int i = startIndex; i < endExclusive; i++) {
            MacroAction copy = AutismMacro.init_MacroAction_CompoundTag(this.init_aM.init_List.get(i).toTag());
            if (copy != null) {
               oneShot.init_List.add(copy);
            }
         }

         if (!oneShot.init_List.isEmpty()) {
            this.store_V();
            this.update_J = oneShot.init_J();
            this.chunk_V();
            if (this.update_J >= 0L) {
               this.sync_J = System.currentTimeMillis();
            }

            this.bind_V();
         }
      }
   }

   private boolean init_Z_MacroAction_2(MacroAction action) {
      return !(action instanceof autismclient.util.macro.RaceAction)
         && !(action instanceof autismclient.util.macro.ReportAction)
         && !(action instanceof autismclient.util.macro.PacketGateAction)
         && !(action instanceof autismclient.util.macro.EndPacketGateAction);
   }

   private List<MacroAction> init_List_MacroAction(MacroAction primary) {
      if (primary == null) {
         return List.of();
      }

      if (this.load_Z_MacroAction_2(primary) && this.init_Z_MacroAction_2(primary)) {
         if (!this.init_AutismMacroEditorO_2.init_Z(this.write_I(primary))) {
            return List.of(primary);
         }

         List<MacroAction> selected = this.read_List_2();
         if (selected.size() <= 1) {
            return List.of(primary);
         }

         CompoundTag expected = this.init_CompoundTag_MacroAction(primary);
         if (expected == null) {
            return List.of(primary);
         }

         for (MacroAction action : selected) {
            if (!this.init_Z_MacroAction_2(action) || !this.init_Z_MacroAction_CompoundTag(action, expected)) {
               return List.of(primary);
            }
         }

         return List.copyOf(selected);
      } else {
         return List.of(primary);
      }
   }

   private boolean load_Z_MacroAction_2(MacroAction action) {
      if (this.init_aM != null && action != null) {
         for (MacroAction candidate : this.init_aM.init_List) {
            if (candidate == action) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void init_V_I_MacroAction(int index, MacroAction primary) {
      if (primary != null && this.read_Z(primary)) {
         List<MacroAction> targets = this.init_List_MacroAction(primary);
         ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
         actionEditor.load_V_Z(this.cache_Z());
         actionEditor.init_V_aM_I(this.init_aM, index);
         actionEditor.init_V_MacroAction(primary, this::count_V, () -> this.init_V_MacroAction_List(primary, targets), targets.size());
      }
   }

   private void init_V_MacroAction_List(MacroAction primary, List<MacroAction> targets) {
      if (primary != null) {
         CompoundTag edited = this.init_CompoundTag_MacroAction(primary);
         if (edited != null && targets != null && targets.size() > 1) {
            for (MacroAction target : targets) {
               if (target != primary && this.load_Z_MacroAction_2(target)) {
                  CompoundTag copy = edited.copy();
                  target.fromTag(copy);
                  DynamicBindingManager.init(target, copy);
               }
            }
         }

         this.load_V_MacroAction_2(primary);
      }
   }

   private List<MacroAction> load_List_List(List<MacroAction> actions) {
      List<MacroAction> clones = new ArrayList<>();
      if (actions == null) {
         return clones;
      }

      for (MacroAction action : actions) {
         if (action != null) {
            MacroAction copy = AutismMacro.init_MacroAction_CompoundTag(action.toTag());
            if (copy != null) {
               clones.add(copy);
            }
         }
      }

      this.init_V_List_3(clones);
      return clones;
   }

   private void init_V_List_3(List<MacroAction> actions) {
      if (actions != null && !actions.isEmpty()) {
         String pendingGateId = null;

         for (MacroAction action : actions) {
            if (action instanceof autismclient.util.macro.PacketGateAction gate) {
               gate.gateId = UUID.randomUUID().toString();
               pendingGateId = gate.scope == autismclient.util.macro.PacketGateAction.Inner_BufferHelper.MACRO_PASS ? null : gate.gateId;
            } else if (action instanceof autismclient.util.macro.EndPacketGateAction endGate && pendingGateId != null) {
               endGate.gateId = pendingGateId;
               pendingGateId = null;
            }
         }
      }
   }

   private int v() {
      List<AutismMacroEditorOverlay.Inner_TextFormat> units = this.init_List_6();
      if (units.isEmpty()) {
         return this.init_aM == null ? 0 : this.init_aM.init_List.size();
      } else {
         return Math.max(0, Math.min(this.init_aM.init_List.size(), units.get(units.size() - 1).load));
      }
   }

   private void load_V_List_2(List<MacroAction> pasted) {
      this.init_AutismMacroEditorO_2.init_V();
      if (pasted != null) {
         for (MacroAction action : pasted) {
            if (action != null) {
               this.init_AutismMacroEditorO_2.init_V(this.write_I(action));
            }
         }
      }
   }

   private void put_V_4() {
      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         Set<Integer> liveIds = new LinkedHashSet<>();

         for (MacroAction action : this.init_aM.init_List) {
            liveIds.add(this.write_I(action));
         }

         this.init_AutismMacroEditorO_2.init(liveIds);
      } else {
         this.init_AutismMacroEditorO_2.init_V();
      }
   }

   private boolean tick_Z_4() {
      return this.init_k != null && this.init_k.init_Z()
         || this.add_I == 1 && this.load_k != null && this.load_k.init_Z()
         || this.read_k != null && this.read_k.init_Z();
   }

   private boolean run_Z_3() {
      if (init_Minecraft != null && init_Minecraft.getWindow() != null) {
         long handle = init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 341) == 1 || CompatGLFW.glfwGetKey(handle, 345) == 1;
      } else {
         return false;
      }
   }

   private boolean write_Z(int modifiers) {
      return (modifiers & 2) != 0 || this.run_Z_3();
   }

   private boolean put_Z_2() {
      if (init_Minecraft != null && init_Minecraft.getWindow() != null) {
         long handle = init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
      } else {
         return false;
      }
   }

   private boolean update_Z(int modifiers) {
      return (modifiers & 1) != 0 || this.put_Z_2();
   }

   private static String init_Str_I_4(int count) {
      return count == 1 ? "step" : "steps";
   }

   private boolean clear_Z_2() {
      if (this.queue_Z()) {
         return true;
      }

      List<MacroAction> selected = this.load_List_3();
      if (selected.isEmpty()) {
         bi.read("Select macro steps first.");
         return true;
      }

      if (MacroClipboardCodec.init_Z(selected)) {
         bi.init("Copied " + selected.size() + " macro " + init_Str_I_4(selected.size()) + ".");
      } else {
         bi.write("Failed to copy macro steps.");
      }

      return true;
   }

   private boolean reset_Z_2() {
      if (this.queue_Z()) {
         return true;
      }

      if (this.init_aM == null) {
         return false;
      }

      List<MacroAction> pasted = MacroClipboardCodec.load_List();
      if (pasted != null && !pasted.isEmpty()) {
         this.init_V_List_3(pasted);
      } else {
         pasted = this.load_List_List(this.load_List_3());
      }

      if (pasted != null && !pasted.isEmpty()) {
         int insertIndex = this.v();
         this.count_V();
         this.init_aM.init_List.addAll(insertIndex, pasted);
         this.fill_V();
         this.load_V_List_2(pasted);
         bi.init("Pasted " + pasted.size() + " macro " + init_Str_I_4(pasted.size()) + ".", -13248397);
         return true;
      } else {
         bi.read("Clipboard has no macro steps.");
         return true;
      }
   }

   private boolean start_Z_2() {
      if (this.queue_Z()) {
         return true;
      }

      List<MacroAction> selected = this.load_List_3();
      if (selected.isEmpty()) {
         bi.read("Select macro steps first.");
         return true;
      }

      if (!MacroClipboardCodec.init_Z(selected)) {
         bi.write("Failed to copy macro steps.");
         return true;
      }

      int count = selected.size();
      if (this.load_Z_Z(false)) {
         bi.init("Cut " + count + " macro " + init_Str_I_4(count) + ".");
      }

      return true;
   }

   private boolean stop_Z_2() {
      if (this.init_aM != null && !this.init_aM.init_List.isEmpty()) {
         this.load_V_List_2(this.init_aM.init_List);
         return true;
      } else {
         return false;
      }
   }

   private boolean next_Z_2() {
      if (this.queue_Z()) {
         return true;
      }

      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         List<MacroAction> copies = this.load_List_List(this.load_List_3());
         if (copies.isEmpty()) {
            return false;
         }

         int insertIndex = this.v();
         this.count_V();
         this.init_aM.init_List.addAll(insertIndex, copies);
         this.fill_V();
         this.load_V_List_2(copies);
         bi.init("Duplicated " + copies.size() + " macro " + init_Str_I_4(copies.size()) + ".", -13248397);
         return true;
      } else {
         return false;
      }
   }

   private boolean prev_Z_2() {
      return this.load_Z_Z(true);
   }

   private boolean load_Z_Z(boolean announce) {
      if (this.queue_Z()) {
         return true;
      }

      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         List<AutismMacroEditorOverlay.Inner_TextFormat> units = new ArrayList<>(this.init_List_6());
         if (units.isEmpty()) {
            this.init_AutismMacroEditorO_2.init_V();
            return false;
         }

         ActionEditorOverlay editor = ActionEditorOverlay.load_ActionEditorOverla();
         this.count_V();
         TreeSet<Integer> deleteIndices = new TreeSet<>();

         for (AutismMacroEditorOverlay.Inner_TextFormat unit : units) {
            for (int i = unit.init_I; i < unit.load && i < this.init_aM.init_List.size(); i++) {
               deleteIndices.add(i);
            }
         }

         for (int i : new ArrayList<>(deleteIndices)) {
            if (this.init_aM.init_List.get(i) instanceof autismclient.util.macro.MultiAction multi) {
               int count = multi.init(this.init_aM.init_List, i);

               for (int j = i + 1; j <= i + count; j++) {
                  deleteIndices.remove(j);
               }
            }
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         int removed = 0;

         for (int i : deleteIndices.descendingSet()) {
            if (i >= 0 && i < this.init_aM.init_List.size()) {
               if (editor != null) {
                  editor.init_V_MacroAction(this.init_aM.init_List.get(i));
               }

               this.init_aM.init_List.remove(i);
               removed++;
            }
         }

         this.init_V_Map_Set(multiCoverage, null);
         this.init_AutismMacroEditorO_2.init_V();
         this.fill_V();
         if (removed > 0 && announce) {
            bi.init("Deleted " + removed + " macro " + init_Str_I_4(removed) + ".", -42149);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean sync_Z(int direction) {
      if (this.queue_Z()) {
         return true;
      }

      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z() && direction != 0) {
         List<AutismMacroEditorOverlay.Inner_TextFormat> orderedUnits = new ArrayList<>();
         int i = 0;

         while (i < this.init_aM.init_List.size()) {
            AutismMacroEditorOverlay.Inner_TextFormat unit = this.init_AutismMacroEditorO_I_2(i);
            if (unit.init()) {
               i++;
            } else {
               orderedUnits.add(unit);
               i = unit.load;
            }
         }

         if (orderedUnits.size() < 2) {
            return false;
         }

         boolean moved = false;
         if (direction < 0) {
            for (int ix = 1; ix < orderedUnits.size(); ix++) {
               if (this.init_Z_AutismMacroEditorO(orderedUnits.get(ix)) && !this.init_Z_AutismMacroEditorO(orderedUnits.get(ix - 1))) {
                  AutismMacroEditorOverlay.Inner_TextFormat unit = orderedUnits.remove(ix);
                  orderedUnits.add(ix - 1, unit);
                  moved = true;
               }
            }
         } else {
            for (int ix = orderedUnits.size() - 2; ix >= 0; ix--) {
               if (this.init_Z_AutismMacroEditorO(orderedUnits.get(ix)) && !this.init_Z_AutismMacroEditorO(orderedUnits.get(ix + 1))) {
                  AutismMacroEditorOverlay.Inner_TextFormat unit = orderedUnits.remove(ix);
                  orderedUnits.add(ix + 1, unit);
                  moved = true;
               }
            }
         }

         if (!moved) {
            return false;
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         Set<MacroAction> movedRefs = Collections.newSetFromMap(new IdentityHashMap<>());
         movedRefs.addAll(this.load_List_3());
         List<MacroAction> rebuilt = new ArrayList<>(this.init_aM.init_List.size());

         for (AutismMacroEditorOverlay.Inner_TextFormat unit : orderedUnits) {
            for (int ix = unit.init_I; ix < unit.load && ix < this.init_aM.init_List.size(); ix++) {
               rebuilt.add(this.init_aM.init_List.get(ix));
            }
         }

         this.count_V();
         this.init_aM.init_List.clear();
         this.init_aM.init_List.addAll(rebuilt);
         this.init_V_Map_Set(multiCoverage, movedRefs);
         this.fill_V();
         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_AutismMacroEditorO(AutismMacroEditorOverlay.Inner_TextFormat unit) {
      if (unit != null && !unit.init() && this.init_aM != null) {
         for (int i = unit.init_I; i < unit.load && i < this.init_aM.init_List.size(); i++) {
            if (this.init_AutismMacroEditorO_2.init_Z(this.write_I(this.init_aM.init_List.get(i)))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private List<AutismMacroEditorOverlay.Inner_EventBus> write_List_2() {
      List<AutismMacroEditorOverlay.Inner_EventBus> snapshots = new ArrayList<>();
      if (this.init_aM == null) {
         return snapshots;
      }

      for (int index = 0; index < this.init_aM.init_List.size(); index++) {
         MacroAction action = this.init_aM.init_List.get(index);
         snapshots.add(new AutismMacroEditorOverlay.Inner_EventBus(action, this.write_I(action), action.getDisplayName(), this.write_Z(action), index));
      }

      return snapshots;
   }

   private AutismMacroEditorOverlay.Inner_EventBus init_AutismMacroEditorO_List_I(List<AutismMacroEditorOverlay.Inner_EventBus> snapshots, int stableId) {
      for (AutismMacroEditorOverlay.Inner_EventBus snapshot : snapshots) {
         if (snapshot.init_I == stableId) {
            return snapshot;
         }
      }

      return null;
   }

   private AutismMacroEditorOverlay.Inner_UiHelper init_AutismMacroEditorO_I_3(int stableId) {
      for (AutismMacroEditorOverlay.Inner_UiHelper motion : this.push_List) {
         if (motion.init_I == stableId) {
            return motion;
         }
      }

      return null;
   }

   private void update_V_2(int stableId) {
      this.push_List.removeIf(motion -> motion.init_I == stableId);
   }

   private void read(boolean animateChanges) {
      List<AutismMacroEditorOverlay.Inner_EventBus> current = this.write_List_2();
      if (!animateChanges) {
         this.push_List.clear();
         this.poll_List.clear();
         this.poll_List.addAll(current);
         this.poll_J = System.nanoTime();
      } else {
         this.push_List.clear();

         for (AutismMacroEditorOverlay.Inner_EventBus snapshot : current) {
            AutismMacroEditorOverlay.Inner_EventBus previous = this.init_AutismMacroEditorO_List_I(this.poll_List, snapshot.init_I);
            if (previous == null) {
               this.update_V_2(snapshot.init_I);
               this.push_List
                  .add(
                     new AutismMacroEditorOverlay.Inner_UiHelper(
                        snapshot.init_I, snapshot.init_Str, snapshot.init_Z, snapshot.load, snapshot.load, AutismMacroEditorOverlay.Inner_RenderState.ENTER
                     )
                  );
            } else if (previous.load != snapshot.load) {
               this.update_V_2(snapshot.init_I);
               this.push_List
                  .add(
                     new AutismMacroEditorOverlay.Inner_UiHelper(
                        snapshot.init_I, snapshot.init_Str, snapshot.init_Z, previous.load, snapshot.load, AutismMacroEditorOverlay.Inner_RenderState.MOVE
                     )
                  );
            }
         }

         for (AutismMacroEditorOverlay.Inner_EventBus snapshot : this.poll_List) {
            if (this.init_AutismMacroEditorO_List_I(current, snapshot.init_I) == null) {
               this.update_V_2(snapshot.init_I);
               this.push_List
                  .add(
                     new AutismMacroEditorOverlay.Inner_UiHelper(
                        snapshot.init_I, snapshot.init_Str, snapshot.init_Z, snapshot.load, snapshot.load, AutismMacroEditorOverlay.Inner_RenderState.EXIT
                     )
                  );
            }
         }

         this.poll_List.clear();
         this.poll_List.addAll(current);
      }
   }

   private void add_V_5() {
      this.init_AutismMacroEditorO_2.init_V();
      this.init_IdentityHashMap.clear();
      this.poll_List.clear();
      this.push_List.clear();
      this.store = 1;
      this.poll_J = System.nanoTime();
      if (this.init_aM != null) {
         this.poll_List.addAll(this.write_List_2());
      }
   }

   private float init_F() {
      long now = System.nanoTime();
      float delta = Math.max(0.0F, Math.min(0.05F, (float)(now - this.poll_J) / 1.0E9F));
      this.poll_J = now;
      if (this.push_List.isEmpty()) {
         return delta;
      }

      this.push_List.removeIf(motion -> {
         float var10000;
         switch (motion.init_AutismMacroEditorO) {
            case ENTER:
               var10000 = 0.14F;
               break;
            case MOVE:
               var10000 = 0.14F;
               break;
            case EXIT:
               var10000 = 0.12F;
               break;
            default:
               throw new MatchException(null, null);
         }

         float duration = var10000;
         motion.init_F = Math.min(1.0F, motion.init_F + (duration <= 0.0F ? 1.0F : delta / duration));
         return motion.init_F >= 0.999F;
      });
      return delta;
   }

   private float init_F_I(int stableId) {
      AutismMacroEditorOverlay.Inner_UiHelper motion = this.init_AutismMacroEditorO_I_3(stableId);
      return motion != null && motion.init_AutismMacroEditorO == AutismMacroEditorOverlay.Inner_RenderState.ENTER ? this.init_F_F(motion.init_F) : 1.0F;
   }

   private int init_I_AutismMacroEditorO_I(AutismMacroEditorOverlay.Inner_UiHelper motion, int baseListY) {
      float eased = this.init_F_F(motion.init_F);
      int fromY = baseListY + motion.load * this.mark_I();
      int toY = baseListY + motion.read * this.mark_I();
      return Math.round(fromY + (toY - fromY) * eased);
   }

   private int init_I_AutismMacroEditorO(AutismMacroEditorOverlay.Inner_UiHelper motion) {
      if (motion == null) {
         return 0;
      }

      float eased = this.init_F_F(motion.init_F);
      int var10000;
      switch (motion.init_AutismMacroEditorO) {
         case ENTER:
            var10000 = Math.round((1.0F - eased) * -18.0F);
            break;
         case MOVE:
            var10000 = 0;
            break;
         case EXIT:
            var10000 = Math.round(eased * 20.0F);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private float init_F_AutismMacroEditorO(AutismMacroEditorOverlay.Inner_UiHelper motion) {
      if (motion == null) {
         return 1.0F;
      }

      float eased = this.init_F_F(motion.init_F);
      float var10000;
      switch (motion.init_AutismMacroEditorO) {
         case ENTER:
            var10000 = 0.45F + 0.55F * eased;
            break;
         case MOVE:
            var10000 = 1.0F;
            break;
         case EXIT:
            var10000 = Math.max(0.0F, 1.0F - eased);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private float init_F_F(float value) {
      float clamped = Math.max(0.0F, Math.min(1.0F, value));
      return 1.0F - (float)Math.pow(1.0F - clamped, 3.0);
   }

   private void clear_V_4() {
      this.clear_I = this.init_I_I_3(this.clear_I, this.mark_I(), this.queue_I());
   }

   private int w() {
      return this.size - this.valid;
   }

   private int flush(int sourceIndex, int targetIndex) {
      if (sourceIndex >= 0 && targetIndex >= 0) {
         return targetIndex > sourceIndex ? targetIndex + 1 : targetIndex;
      } else {
         return -1;
      }
   }

   private int load_I_I_2(int index, int sourceIndex, int targetIndex) {
      if (sourceIndex < 0 || targetIndex < 0 || sourceIndex == targetIndex) {
         return index;
      } else if (sourceIndex < targetIndex) {
         return index > sourceIndex && index <= targetIndex ? index - 1 : index;
      } else {
         return index >= targetIndex && index < sourceIndex ? index + 1 : index;
      }
   }

   private void init_V_I(int index, double mouseX, double mouseY, boolean canDrag) {
      if (!this.queue_Z()) {
         this.next = index;
         this.prev = index;
         this.valid = (int)Math.round(mouseY) - (this.run_I_12() - this.clear_I + index * this.mark_I());
         this.size = (int)Math.round(mouseY);
         this.init_D = mouseX;
         this.load_D = mouseY;
         this.close_Z = this.read_Z(index);
         this.open_Z = canDrag;
      }
   }

   private void init_V_D_D(double mouseX, double mouseY) {
      if (this.queue_Z()) {
         this.reset_V_3();
      } else if (this.stop < 0 && this.next >= 0 && this.open_Z) {
         boolean movedEnough = Math.abs(mouseX - this.init_D) >= 3.0 || Math.abs(mouseY - this.load_D) >= 3.0;
         if (movedEnough) {
            this.stop = this.next;
            this.prev = this.next;
            this.size = (int)Math.round(mouseY);
         }
      }
   }

   private void init_V_D(double mouseY) {
      if (this.stop >= 0 && this.init_aM != null && !this.init_aM.init_List.isEmpty()) {
         this.size = (int)Math.round(mouseY);
         this.load_V_D(mouseY);
         double draggedRowTop = mouseY - this.valid;
         double draggedRowCenter = draggedRowTop - this.run_I_12() + this.clear_I + this.mark_I() / 2.0;
         this.prev = this.init_I_D_2(draggedRowCenter);
      }
   }

   private int init_I_D_2(double draggedRowCenter) {
      if (this.stop >= 0 && this.init_aM != null && !this.init_aM.init_List.isEmpty()) {
         int rowHeight = this.mark_I();
         int targetIndex = this.stop;
         double sourceCenter = this.stop * rowHeight + rowHeight / 2.0;
         if (draggedRowCenter < sourceCenter) {
            while (targetIndex > 0) {
               double previousCenter = (targetIndex - 1) * rowHeight + rowHeight / 2.0;
               if (!(draggedRowCenter < previousCenter)) {
                  break;
               }

               targetIndex--;
            }
         } else if (draggedRowCenter > sourceCenter) {
            for (int maxIndex = this.init_aM.init_List.size() - 1; targetIndex < maxIndex; targetIndex++) {
               double nextCenter = (targetIndex + 1) * rowHeight + rowHeight / 2.0;
               if (!(draggedRowCenter > nextCenter)) {
                  break;
               }
            }
         }

         return targetIndex;
      } else {
         return -1;
      }
   }

   private void load_V_D(double mouseY) {
      int maxScroll = this.queue_I();
      if (maxScroll > 0) {
         int listTop = this.run_I_12();
         int listBottom = listTop + this.clear_I_4();
         int delta = 0;
         if (mouseY < listTop + 18) {
            double depth = listTop + 18 - mouseY;
            delta = -Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0)));
         } else if (mouseY > listBottom - 18) {
            double depth = mouseY - (listBottom - 18);
            delta = Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0)));
         }

         if (delta != 0) {
            this.clear_I = Math.max(0, Math.min(maxScroll, this.clear_I + delta));
         }
      }
   }

   private void reset_V_3() {
      this.stop = -1;
      this.next = -1;
      this.prev = -1;
      this.valid = 0;
      this.size = 0;
      this.init_D = 0.0;
      this.load_D = 0.0;
      this.close_Z = false;
      this.open_Z = false;
   }

   private void write_V_I_I_2(int fromIndex, int toIndex) {
      if (!this.queue_Z()
         && this.init_aM != null
         && fromIndex >= 0
         && toIndex >= 0
         && fromIndex < this.init_aM.init_List.size()
         && toIndex < this.init_aM.init_List.size()
         && fromIndex != toIndex) {
         MacroAction source = this.init_aM.init_List.get(fromIndex);
         if (this.flush_Z(fromIndex)) {
            return;
         }

         int moveCount = 1;
         if (source instanceof autismclient.util.macro.RaceAction race) {
            moveCount = race.init_I_List_I(this.init_aM.init_List, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.ReportAction report) {
            moveCount = report.init(this.init_aM.init_List, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.MultiAction multi) {
            moveCount = multi.init(this.init_aM.init_List, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.PacketGateAction) {
            moveCount = this.sync_I(fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.EndPacketGateAction) {
            this.sync_V_2(fromIndex, toIndex);
            return;
         }

         AutismMacroEditorOverlay.Inner_StreamUtil targetGroup = this.init_AutismMacroEditorO_I_MacroAction(toIndex, this.init_aM.init_List.get(toIndex));
         if (targetGroup.init() && targetGroup.init_I == fromIndex) {
            return;
         }

         if (targetGroup.write) {
            targetGroup = AutismMacroEditorOverlay.Inner_StreamUtil.init_AutismMacroEditorO;
         }

         int insertIndex;
         if (targetGroup.init()) {
            insertIndex = toIndex > fromIndex ? targetGroup.init_I + targetGroup.load_I + 1 : targetGroup.init_I;
         } else if (toIndex > fromIndex) {
            insertIndex = toIndex + 1;
         } else {
            insertIndex = toIndex;
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         List<MacroAction> moved = new ArrayList<>(this.init_aM.init_List.subList(fromIndex, fromIndex + moveCount));
         this.init_aM.init_List.subList(fromIndex, fromIndex + moveCount).clear();
         if (insertIndex > fromIndex) {
            insertIndex -= moveCount;
         }

         insertIndex = Math.max(0, Math.min(insertIndex, this.init_aM.init_List.size()));
         this.init_aM.init_List.addAll(insertIndex, moved);
         Set<MacroAction> movedSet = Collections.newSetFromMap(new IdentityHashMap<>());
         movedSet.addAll(moved);
         this.init_V_Map_Set(multiCoverage, movedSet);
         this.fill_V();
      }
   }

   private void stop_V_3() {
      if (this.queue_Z()) {
         this.reset_V_3();
      } else {
         int sourceIndex = this.stop;
         int pressIndex = this.next;
         int targetIndex = this.prev;
         boolean wasSelectedAtPress = this.close_Z;
         boolean dragged = sourceIndex >= 0;
         this.reset_V_3();
         if (dragged) {
            if (this.init_aM != null && targetIndex >= 0 && sourceIndex != targetIndex) {
               this.count_V();
               if (this.read_Z(sourceIndex) && this.load_List_3().size() > 1) {
                  this.update_V_I_I(sourceIndex, targetIndex);
               } else {
                  this.write_V_I_I_2(sourceIndex, targetIndex);
               }
            }
         } else if (pressIndex >= 0 && !this.run_Z_3() && !this.put_Z_2()) {
            this.init_V_I_Z_2(pressIndex, wasSelectedAtPress);
         }
      }
   }

   private void update_V_I_I(int sourceIndex, int targetIndex) {
      if (this.init_aM != null) {
         List<MacroAction> refs = this.load_List_3();
         if (refs.size() > 1 && !this.init_Z_List(refs)) {
            List<MacroAction> actions = this.init_aM.init_List;
            Set<MacroAction> selectedSet = Collections.newSetFromMap(new IdentityHashMap<>());
            selectedSet.addAll(refs);
            int insertPos = this.init_I_Set(selectedSet, sourceIndex, targetIndex);
            if (insertPos >= 0) {
               Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
               List<MacroAction> remaining = new ArrayList<>(actions.size());

               for (MacroAction action : actions) {
                  if (!selectedSet.contains(action)) {
                     remaining.add(action);
                  }
               }

               insertPos = Math.max(0, Math.min(remaining.size(), insertPos));
               remaining.addAll(insertPos, refs);
               actions.clear();
               actions.addAll(remaining);
               this.init_V_Map_Set(multiCoverage, selectedSet);
            }
         } else {
            this.write_V_I_I_2(sourceIndex, targetIndex);
         }
      }
   }

   private boolean init_Z_List(List<MacroAction> refs) {
      if (refs == null) {
         return false;
      }

      for (MacroAction ref : refs) {
         if (ref instanceof autismclient.util.macro.PacketGateAction || ref instanceof autismclient.util.macro.EndPacketGateAction) {
            return true;
         }
      }

      return false;
   }

   private int init_I_Set(Set<MacroAction> selectedSet, int sourceIndex, int targetIndex) {
      if (this.init_aM == null) {
         return -1;
      }

      List<MacroAction> actions = this.init_aM.init_List;
      if (targetIndex >= 0 && targetIndex < actions.size()) {
         MacroAction anchor = actions.get(targetIndex);
         if (selectedSet.contains(anchor)) {
            return -1;
         }

         boolean movingDown = targetIndex > sourceIndex;
         AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(targetIndex, anchor);
         if (group.write) {
            group = AutismMacroEditorOverlay.Inner_StreamUtil.init_AutismMacroEditorO;
         }

         int anchorReal;
         boolean insertAfter;
         if (group.init()) {
            int lastChild = Math.min(actions.size() - 1, group.init_I + group.load_I);
            anchorReal = movingDown ? lastChild : group.init_I;
            insertAfter = movingDown;
         } else {
            anchorReal = targetIndex;
            insertAfter = movingDown;
         }

         if (anchorReal >= 0 && anchorReal < actions.size() && !selectedSet.contains(actions.get(anchorReal))) {
            int selectedBefore = 0;

            for (int j = 0; j < anchorReal; j++) {
               if (selectedSet.contains(actions.get(j))) {
                  selectedBefore++;
               }
            }

            int compactedAnchorPos = anchorReal - selectedBefore;
            return insertAfter ? compactedAnchorPos + 1 : compactedAnchorPos;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private int init_I_I_2(int realIndex, Set<MacroAction> selectedSet, int insertCompact, int blockSize) {
      List<MacroAction> actions = this.init_aM.init_List;
      int selectedBefore = 0;

      for (int j = 0; j < realIndex; j++) {
         if (selectedSet.contains(actions.get(j))) {
            selectedBefore++;
         }
      }

      int compactedPos = realIndex - selectedBefore;
      if (insertCompact >= 0 && compactedPos >= insertCompact) {
         compactedPos += blockSize;
      }

      return compactedPos;
   }

   private void init_V_GuiGraphicsExtract_6(GuiGraphicsExtractor context, MacroAction action, int top, int number) {
      autismclient.gui.f.b.PanelSurfaceHelper.init(
         context, this.get_I_10() + 1, top + 1, this.chunk_I() - this.get_I_10() - 1, this.mark_I() - 2, this.init_CharEncoder.read(4607322)
      );
      int badgeColor = this.write_Z(action) ? -30720 : -12268476;
      autismclient.gui.f.b.PanelSurfaceHelper.read(context, this.get_I_10() + 2, top + 3, 3, this.mark_I() - 6, badgeColor);
      autismclient.gui.f.b.k.init(
         context,
         this.init_Font,
         String.valueOf(number),
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         UiColorPalette.bind(),
         this.get_I_10() + 8,
         this.init_I_I_6(top),
         false
      );
      int nameX = this.get_I_10() + 22;
      int controlsX = this.load_I_MacroAction(action);
      int maxNameW = controlsX - nameX - 8;
      String displayName = autismclient.gui.f.b.k.init(
         this.init_Font, action.getDisplayName(), maxNameW, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), -1
      );
      autismclient.gui.f.b.k.init(
         context, this.init_Font, displayName, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), -1, nameX, this.init_I_I_6(top), false
      );
   }

   private int x() {
      return this.y() + 19;
   }

   private int write(String label) {
      Integer cached = this.init_HashMap.get(label);
      if (cached != null) {
         return cached;
      }

      int w = autismclient.gui.f.b.k.init(
         this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
      );
      w = Math.min(88, w);
      this.init_HashMap.put(label, w);
      return w;
   }

   private int y() {
      int screenW = init_Minecraft.getWindow() != null ? cA.init_I() : 320;
      int screenH = init_Minecraft.getWindow() != null ? cA.load_I() : 320;
      if (this.task >= 0 && this.write_J == this.read_J && this.entry == screenW && this.node == screenH) {
         return this.task;
      }

      int result = this.close(screenW, screenH);
      this.write_J = this.read_J;
      this.entry = screenW;
      this.node = screenH;
      this.task = result;
      return result;
   }

   private int close(int screenW, int screenH) {
      int maxViewportWidth = Math.max(1, Math.min(456, screenW - 20));
      int minViewportWidth = Math.min(132, maxViewportWidth);
      int maxContentHeight = Math.max(1, screenH - 36);
      List<AutismMacroEditorOverlay.Inner_SyncManager> fallbackLayouts = null;
      int fallbackOverflow = 2147483647;
      int fallbackWidth = maxViewportWidth;

      for (int candidateWidth = minViewportWidth; candidateWidth <= maxViewportWidth; candidateWidth += 6) {
         List<AutismMacroEditorOverlay.Inner_SyncManager> layouts = this.init_List_I(0, 0, candidateWidth);
         int contentHeight = this.init_I_List_3(layouts);
         if (contentHeight <= maxContentHeight) {
            return this.read_I_I(this.load_I_List(layouts), minViewportWidth, maxViewportWidth);
         }

         int overflow = contentHeight - maxContentHeight;
         if (overflow < fallbackOverflow) {
            fallbackOverflow = overflow;
            fallbackLayouts = layouts;
            fallbackWidth = candidateWidth;
         }
      }

      return fallbackLayouts != null ? this.read_I_I(this.load_I_List(fallbackLayouts), minViewportWidth, fallbackWidth) : maxViewportWidth;
   }

   private int read_I_I(int width, int minViewportWidth, int maxViewportWidth) {
      int widenedWidth = width + 8;
      return Math.max(minViewportWidth, Math.min(maxViewportWidth, widenedWidth));
   }

   private int z() {
      List<AutismMacroEditorOverlay.Inner_SyncManager> layouts = this.init_List_I(0, 0, this.y());
      return this.init_I_List_3(layouts);
   }

   private int A() {
      int screenH = init_Minecraft.getWindow() != null ? cA.load_I() : 320;
      int desiredHeight = 48 + this.z();
      return Math.max(1, Math.min(desiredHeight, screenH - 8));
   }

   private int B() {
      int pickerW = this.x();
      int screenW = init_Minecraft.getWindow() != null ? cA.init_I() : this.tick_I + this.get_I + pickerW + 8;
      if (this.tick_I + this.get_I + pickerW + 5 <= screenW) {
         return this.tick_I + this.get_I + 5;
      } else {
         return this.tick_I - pickerW - 5 >= 0
            ? this.tick_I - pickerW - 5
            : Math.max(4, Math.min(screenW - pickerW - 4, this.tick_I + (this.get_I - pickerW) / 2));
      }
   }

   private int C() {
      int pickerH = this.A();
      int screenH = init_Minecraft.getWindow() != null ? cA.load_I() : this.run_I + pickerH + 8;
      int preferredY = this.run_I + 42;
      return Math.max(4, Math.min(screenH - pickerH - 4, preferredY));
   }

   private int D() {
      return this.init_NetHelper_4().read();
   }

   private int E() {
      return this.init_NetHelper_4().write();
   }

   private autismclient.gui.f.NetHelper init_NetHelper_4() {
      return autismclient.gui.f.b.WindowHeaderHelper.load(autismclient.gui.f.NetHelper.init(this.B(), this.C(), this.x(), this.A()), 16);
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil load_PoolEntry_DataUtil_2() {
      int pickerX = this.B();
      int pickerY = this.C();
      int pickerW = this.x();
      int pickerH = this.A();
      int gridX = pickerX + 6;
      int gridY = pickerY + 42;
      int gridW = Math.max(1, pickerW - 19);
      int gridH = Math.max(1, pickerH - 48);
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(this.z(), gridH, pickerX + pickerW - 9, gridY, 3, gridH, this.buffer);
   }

   private boolean init_Z_I_I_2(int mouseX, int mouseY) {
      if (!this.valid_Z()) {
         return false;
      }

      int pickerX = this.B();
      int pickerY = this.C();
      int pickerW = this.x();
      int pickerH = this.A();
      return mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + pickerH;
   }

   private boolean valid_Z() {
      return this.init_AutismMacroEditorO_3 != null;
   }

   private void next_V_3() {
      OverlayManager.init_bk().read_V(this.init_AutismMacroEditorO_4);
      this.init_AutismMacroEditorO_3 = null;
      this.close_List = Collections.emptyList();
      this.open_List = Collections.emptyList();
      this.init_Map = Collections.emptyMap();
      this.init_a.init_V_List(Collections.emptyList());
      if (this.read_k != null) {
         this.read_k.push(false);
      }

      this.read_k = null;
      this.buffer = 0;
   }

   private void init_V_List(List<AutismMacroEditorOverlay.Inner_NetHelper> entries, String categoryId, String label, String description, Runnable action) {
      entries.add(new AutismMacroEditorOverlay.Inner_NetHelper(categoryId, label, description, action));
   }

   private void prev_V_3() {
      if (init_Minecraft.player != null) {
         this.init_V_MacroAction_2(new autismclient.util.macro.RotateAction(init_Minecraft.player.getYRot(), init_Minecraft.player.getXRot(), false, 6));
      } else {
         this.init_V_MacroAction_2(new autismclient.util.macro.RotateAction(0.0F, 0.0F, false, 6));
      }
   }

   private void valid_V() {
      autismclient.util.macro.LookAtBlockAction action = new autismclient.util.macro.LookAtBlockAction();
      if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.ENTITY && init_Minecraft.crosshairPickEntity != null) {
         action.targetMode = autismclient.util.macro.LookAtBlockAction.Inner_StreamUtil.ENTITY;
         action.entityIds.add(autismclient.util.macro.LookAtBlockAction.init_Str_Entity(init_Minecraft.crosshairPickEntity));
      } else if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)init_Minecraft.hitResult;
         action.targetMode = autismclient.util.macro.LookAtBlockAction.Inner_StreamUtil.SPECIFIC;
         action.blockX = hit.getBlockPos().getX();
         action.blockY = hit.getBlockPos().getY();
         action.blockZ = hit.getBlockPos().getZ();
      }

      this.init_V_MacroAction_2(action);
   }

   private boolean read_Z(MacroAction action) {
      return ActionEditorOverlay.init_Z_MacroAction(action);
   }

   private boolean write_Z(MacroAction action) {
      return action instanceof autismclient.util.macro.WaitForHealthAction
         || action instanceof autismclient.util.macro.WaitForBlockAction
         || action instanceof autismclient.util.macro.WaitForPacketAction
         || action instanceof autismclient.util.macro.WaitForGuiAction
         || action instanceof autismclient.util.macro.TickSyncAction
         || action instanceof autismclient.util.macro.RevisionSyncAction
         || action instanceof autismclient.util.macro.ServerTickSyncAction
         || action instanceof autismclient.util.macro.WaitForCooldownAction
         || action instanceof autismclient.util.macro.WaitPosAction
         || action instanceof autismclient.util.macro.WaitForChatAction
         || action instanceof autismclient.util.macro.WaitForEntityAction
         || action instanceof autismclient.util.macro.WaitForSlotChangeAction
         || action instanceof autismclient.util.macro.WaitForSoundAction
         || action instanceof autismclient.util.macro.WaitForWorldChangeAction
         || action instanceof autismclient.util.macro.WaitForPositionDeltaAction
         || action instanceof autismclient.util.macro.WaitForTeleportAction
         || MacroActionRegistry.isCondition(action.getTypeId());
   }

   private int read_I_2(int bodyIndex) {
      if (this.init_aM != null && bodyIndex > 0 && bodyIndex < this.init_aM.init_List.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.init_aM.init_List.get(i);
            if (action instanceof autismclient.util.macro.RaceAction race) {
               int end = i + race.init_I_List_I(this.init_aM.init_List, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private int write_I_2(int bodyIndex) {
      if (this.init_aM != null && bodyIndex > 0 && bodyIndex < this.init_aM.init_List.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.init_aM.init_List.get(i);
            if (action instanceof autismclient.util.macro.ReportAction report) {
               int end = i + report.init(this.init_aM.init_List, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.RaceAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean flush_Z(int index) {
      return this.read_I_2(index) >= 0 || this.write_I_2(index) >= 0;
   }

   private int update_I_2(int bodyIndex) {
      if (this.init_aM != null && bodyIndex > 0 && bodyIndex < this.init_aM.init_List.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.init_aM.init_List.get(i);
            if (action instanceof autismclient.util.macro.MultiAction multi) {
               int end = i + multi.init(this.init_aM.init_List, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.RaceAction || action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private Map<autismclient.util.macro.MultiAction, List<MacroAction>> init_Map() {
      Map<autismclient.util.macro.MultiAction, List<MacroAction>> coverage = new IdentityHashMap<>();
      if (this.init_aM == null) {
         return coverage;
      }

      List<MacroAction> actions = this.init_aM.init_List;

      for (int i = 0; i < actions.size(); i++) {
         if (actions.get(i) instanceof autismclient.util.macro.MultiAction multi) {
            int count = multi.init(actions, i);
            coverage.put(multi, new ArrayList<>(actions.subList(i + 1, i + 1 + count)));
         }
      }

      return coverage;
   }

   private void init_V_Map_Set(Map<autismclient.util.macro.MultiAction, List<MacroAction>> before, Set<MacroAction> moved) {
      if (this.init_aM != null && before != null && !before.isEmpty()) {
         List<MacroAction> actions = this.init_aM.init_List;

         for (Entry<autismclient.util.macro.MultiAction, List<MacroAction>> entry : before.entrySet()) {
            autismclient.util.macro.MultiAction multi = entry.getKey();
            int headerIndex = actions.indexOf(multi);
            if (headerIndex >= 0) {
               List<MacroAction> members = entry.getValue();
               int windowEnd = headerIndex + multi.init(actions, headerIndex);
               int lost = 0;

               for (MacroAction member : members) {
                  int pos = actions.indexOf(member);
                  if (pos <= headerIndex || pos > windowEnd) {
                     lost++;
                  }
               }

               int gained = 0;
               if (moved != null) {
                  for (MacroAction action : moved) {
                     if (!members.contains(action)
                        && !(action instanceof autismclient.util.macro.RaceAction)
                        && !(action instanceof autismclient.util.macro.ReportAction)
                        && !(action instanceof autismclient.util.macro.MultiAction)) {
                        int pos = actions.indexOf(action);
                        if (pos > headerIndex && pos <= windowEnd) {
                           gained++;
                        }
                     }
                  }
               }

               multi.stepCount = Math.max(0, multi.stepCount - lost + gained);
            }
         }
      }
   }

   private int sync_I(int headerIndex) {
      if (this.init_aM != null && headerIndex >= 0 && headerIndex < this.init_aM.init_List.size()) {
         if (!(this.init_aM.init_List.get(headerIndex) instanceof autismclient.util.macro.PacketGateAction gate)) {
            return 0;
         } else {
            if (gate.scope == autismclient.util.macro.PacketGateAction.Inner_BufferHelper.MACRO_PASS) {
               return 0;
            }

            for (int i = headerIndex + 1; i < this.init_aM.init_List.size(); i++) {
               MacroAction action = this.init_aM.init_List.get(i);
               if (action instanceof autismclient.util.macro.EndPacketGateAction) {
                  return i - headerIndex;
               }

               if (action instanceof autismclient.util.macro.PacketGateAction || action instanceof autismclient.util.macro.RaceAction || action instanceof autismclient.util.macro.ReportAction) {
                  break;
               }
            }

            return 0;
         }
      } else {
         return 0;
      }
   }

   private int flush_I(int bodyIndex) {
      if (this.init_aM != null && bodyIndex > 0 && bodyIndex < this.init_aM.init_List.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.init_aM.init_List.get(i);
            if (action instanceof autismclient.util.macro.PacketGateAction) {
               int end = i + this.sync_I(i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.EndPacketGateAction
               || action instanceof autismclient.util.macro.RaceAction
               || action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean close_Z(int index) {
      return this.init_aM != null
         && index >= 0
         && index < this.init_aM.init_List.size()
         && this.init_aM.init_List.get(index) instanceof autismclient.util.macro.EndPacketGateAction
         && this.flush_I(index) >= 0;
   }

   private void sync_V_2(int fromIndex, int toIndex) {
      if (this.init_aM != null && fromIndex >= 0 && fromIndex < this.init_aM.init_List.size()) {
         int header = this.flush_I(fromIndex);
         if (header >= 0) {
            int target = Math.max(header + 1, Math.min(toIndex, this.init_aM.init_List.size() - 1));
            if (target != fromIndex) {
               MacroAction end = this.init_aM.init_List.remove(fromIndex);
               target = Math.max(header + 1, Math.min(target, this.init_aM.init_List.size()));
               this.init_aM.init_List.add(target, end);
               this.size_V();
               this.fill_V();
            }
         }
      }
   }

   private void init_V_B(autismclient.util.macro.PacketGateAction gate) {
      if (this.init_aM != null && gate != null) {
         int index = this.init_aM.init_List.indexOf(gate);
         if (index >= 0) {
            int end = index + this.sync_I(index);
            if (gate.scope == autismclient.util.macro.PacketGateAction.Inner_BufferHelper.MACRO_PASS) {
               if (end > index && end < this.init_aM.init_List.size() && this.init_aM.init_List.get(end) instanceof autismclient.util.macro.EndPacketGateAction) {
                  this.init_aM.init_List.remove(end);
               }
            } else {
               if (end <= index || end >= this.init_aM.init_List.size() || !(this.init_aM.init_List.get(end) instanceof autismclient.util.macro.EndPacketGateAction)) {
                  this.init_aM.init_List.add(index + 1, new autismclient.util.macro.EndPacketGateAction(gate.gateId));
               }

               this.size_V();
            }
         }
      }
   }

   private void size_V() {
      if (this.init_aM != null) {
         for (int i = 0; i < this.init_aM.init_List.size(); i++) {
            if (this.init_aM.init_List.get(i) instanceof autismclient.util.macro.PacketGateAction gate) {
               int end = i + this.sync_I(i);
               if (end > i && end < this.init_aM.init_List.size() && this.init_aM.init_List.get(end) instanceof autismclient.util.macro.EndPacketGateAction endGate) {
                  endGate.gateId = gate.gateId != null && !gate.gateId.isBlank() ? gate.gateId : "auto";
                  endGate.flushOnDisable = gate.flushOnDisable;
               }
            }
         }
      }
   }

   private AutismMacroEditorOverlay.Inner_StreamUtil init_AutismMacroEditorO_I_MacroAction(int index, MacroAction action) {
      if (this.init_aM == null || action == null || index < 0 || index >= this.init_aM.init_List.size()) {
         return AutismMacroEditorOverlay.Inner_StreamUtil.init_AutismMacroEditorO;
      } else if (action instanceof autismclient.util.macro.RaceAction race) {
         return new AutismMacroEditorOverlay.Inner_StreamUtil(true, false, false, index, race.init_I_List_I(this.init_aM.init_List, index), 0, "RACE");
      } else if (action instanceof autismclient.util.macro.ReportAction report) {
         return new AutismMacroEditorOverlay.Inner_StreamUtil(true, true, false, index, report.init(this.init_aM.init_List, index), 0, "REPORT");
      } else if (action instanceof autismclient.util.macro.PacketGateAction) {
         return new AutismMacroEditorOverlay.Inner_StreamUtil(true, false, true, index, this.sync_I(index), 0, "GATE START");
      } else if (action instanceof autismclient.util.macro.MultiAction multi) {
         return new AutismMacroEditorOverlay.Inner_StreamUtil(true, false, false, true, index, multi.init(this.init_aM.init_List, index), 0, "MULTI");
      } else {
         int raceHeader = this.read_I_2(index);
         if (raceHeader >= 0 && this.init_aM.init_List.get(raceHeader) instanceof autismclient.util.macro.RaceAction race) {
            int childCount = race.init_I_List_I(this.init_aM.init_List, raceHeader);
            int conditionCount = race.load(this.init_aM.init_List, raceHeader);
            int offset = index - raceHeader;
            return new AutismMacroEditorOverlay.Inner_StreamUtil(
               false, false, false, raceHeader, childCount, offset, offset <= conditionCount ? "CONDITION" : "ACTION"
            );
         } else {
            int reportHeader = this.write_I_2(index);
            if (reportHeader >= 0 && this.init_aM.init_List.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
               int childCount = report.init(this.init_aM.init_List, reportHeader);
               int offset = index - reportHeader;
               return new AutismMacroEditorOverlay.Inner_StreamUtil(false, true, false, reportHeader, childCount, offset, offset == 1 ? "START" : "END");
            } else {
               int gateHeader = this.flush_I(index);
               if (gateHeader >= 0) {
                  int childCount = this.sync_I(gateHeader);
                  int offset = index - gateHeader;
                  String role = action instanceof autismclient.util.macro.EndPacketGateAction ? "GATE END" : "GATED";
                  return new AutismMacroEditorOverlay.Inner_StreamUtil(false, false, true, gateHeader, childCount, offset, role);
               } else {
                  int multiHeader = this.update_I_2(index);
                  if (multiHeader >= 0 && this.init_aM.init_List.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
                     int childCount = multi.init(this.init_aM.init_List, multiHeader);
                     int offset = index - multiHeader;
                     return new AutismMacroEditorOverlay.Inner_StreamUtil(false, false, false, true, multiHeader, childCount, offset, "SKIP");
                  } else {
                     return AutismMacroEditorOverlay.Inner_StreamUtil.init_AutismMacroEditorO;
                  }
               }
            }
         }
      }
   }

   private boolean update(MacroAction action) {
      if (action == null || !action.isEnabled()) {
         return false;
      } else if (action instanceof autismclient.util.macro.FpsAction fps) {
         return fps.init_RingBuffer_DataUti() != autismclient.util.macro.FpsAction.Inner_DataUtil.TIMEOUT;
      } else {
         return action instanceof autismclient.util.macro.SPingAction sping ? sping.init_T_DataUtil() != autismclient.util.macro.SPingAction.Inner_DataUtil.TIMEOUT : false;
      }
   }

   private int close_I(int headerIndex) {
      if (this.init_aM != null && headerIndex >= 0 && headerIndex < this.init_aM.init_List.size()) {
         MacroAction action = this.init_aM.init_List.get(headerIndex);
         int last = this.init_aM.init_List.size() - 1;
         int covered;
         if (action instanceof autismclient.util.macro.FpsAction fps) {
            covered = fps.init_RingBuffer_DataUti() == autismclient.util.macro.FpsAction.Inner_DataUtil.END ? last - headerIndex : fps.init_I_3();
         } else {
            if (!(action instanceof autismclient.util.macro.SPingAction sping)) {
               return headerIndex;
            }

            covered = sping.init_T_DataUtil() == autismclient.util.macro.SPingAction.Inner_DataUtil.END ? last - headerIndex : sping.init_I_4();
         }

         long end = (long)headerIndex + Math.max(0, covered);
         return (int)Math.max(headerIndex, Math.min(last, end));
      } else {
         return headerIndex;
      }
   }

   private int open(int index) {
      if (this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size()) {
         for (int h = index; h >= 0; h--) {
            MacroAction a = this.init_aM.init_List.get(h);
            if (this.update(a) && (h == index || index <= this.close_I(h))) {
               return h;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private AutismMacroEditorOverlay.Inner_BufferHelper init_AutismMacroEditorO_I(int index) {
      int header = this.open(index);
      if (header < 0) {
         return AutismMacroEditorOverlay.Inner_BufferHelper.init_AutismMacroEditorO;
      }

      boolean ping = this.init_aM.init_List.get(header) instanceof autismclient.util.macro.SPingAction;
      boolean isHeaderRow = header == index;
      boolean first = index == 0 || this.open(index - 1) < 0;
      boolean last = index >= this.init_aM.init_List.size() - 1 || this.open(index + 1) < 0;
      return new AutismMacroEditorOverlay.Inner_BufferHelper(true, isHeaderRow, ping, first, last);
   }

   private void data_V() {
      autismclient.util.macro.OpenContainerAction action = new autismclient.util.macro.OpenContainerAction();
      if (init_Minecraft.hitResult instanceof EntityHitResult) {
         action.init_V_Minecraft_9(init_Minecraft);
      } else if (init_Minecraft.hitResult instanceof BlockHitResult hit) {
         action.blockPos = hit.getBlockPos();
      }

      this.init_V_MacroAction_2(action);
   }

   private void value_V() {
      autismclient.util.macro.InteractEntityAction action = new autismclient.util.macro.InteractEntityAction();
      action.init_V_Minecraft_3(init_Minecraft);
      this.init_V_MacroAction_2(action);
   }

   private void cache_V() {
      autismclient.util.macro.InventoryAction action = new autismclient.util.macro.InventoryAction();
      action.mode = autismclient.util.macro.InventoryAction.Inner_DataUtil.OPEN;
      this.init_V_MacroAction_2(action);
   }

   private void state_V() {
      autismclient.util.macro.DelayPacketsAction action = new autismclient.util.macro.DelayPacketsAction();
      if (AutismSessionState.init_ck().reset_Z()) {
         action.write();
      } else {
         action.read();
      }

      this.init_V_MacroAction_2(action);
   }

   private void queue_V() {
      autismclient.util.macro.WaitForBlockAction action = new autismclient.util.macro.WaitForBlockAction();
      if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)init_Minecraft.hitResult;
         action.blockPos = hit.getBlockPos();
      }

      this.init_V_MacroAction_2(action);
   }

   private void buffer_V() {
      autismclient.util.macro.WaitForEntityAction action = new autismclient.util.macro.WaitForEntityAction();
      if (init_Minecraft.player != null) {
         action.f_x_D = Math.round(init_Minecraft.player.getX());
         action.f_y_D = Math.round(init_Minecraft.player.getY());
         action.f_z_D = Math.round(init_Minecraft.player.getZ());
      }

      this.init_V_MacroAction_2(action);
   }

   private void entry_V() {
      autismclient.util.macro.InstaBreakAction action = new autismclient.util.macro.InstaBreakAction();
      if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)init_Minecraft.hitResult;
         action.blockPos = hit.getBlockPos();
         action.direction = hit.getDirection();
      }

      this.init_V_MacroAction_2(action);
   }

   private void node_V() {
      autismclient.util.macro.BreakAction action = new autismclient.util.macro.BreakAction();
      if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)init_Minecraft.hitResult;
         action.blockPos = hit.getBlockPos();
         action.direction = hit.getDirection();
      }

      this.init_V_MacroAction_2(action);
   }

   private List<MacroAction> update_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.Inner_DataUtil.HAS_BUNDLE;
      assertBundle.message = "Must have a bundle";
      actions.add(assertBundle);
      autismclient.util.macro.DisconnectAction dupe = new autismclient.util.macro.DisconnectAction();
      dupe.mode = autismclient.util.macro.DisconnectAction.Inner_StreamUtil.KICK_DUPE;
      dupe.lagMethod = autismclient.util.macro.DisconnectAction.Inner_BufferHelper.CLICK_SLOT;
      dupe.kickMethod = autismclient.util.macro.DisconnectAction.Inner_CachePool.HURT;
      dupe.packetCount = 200;
      actions.add(dupe);
      return actions;
   }

   private List<MacroAction> sync_List_2() {
      return new ArrayList<>(List.of(new autismclient.util.macro.RollbackAction()));
   }

   private List<MacroAction> flush_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SelectSlotAction selectSlotOne = new autismclient.util.macro.SelectSlotAction();
      selectSlotOne.slot = 0;
      actions.add(selectSlotOne);
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.Inner_DataUtil.BUNDLE_V2_READY;
      assertBundle.message = "Slot 1 must contain a one-item bundle and no container GUI can be open";
      actions.add(assertBundle);
      actions.add(new autismclient.util.macro.BundleDupeV2Action());
      return actions;
   }

   private List<MacroAction> close_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.NbtBookAction book = new autismclient.util.macro.NbtBookAction();
      book.pages = 1;
      book.customComponent = "DupersUnited?";
      book.title = "NeedToHave42CharsInHereHAHA123456789012345";
      book.delayTicks = 0;
      book.bookCount = 1;
      book.requireHeldWritableBook = true;
      book.dropInventoryBefore = true;
      book.disconnectAfter = true;
      actions.add(book);
      return actions;
   }

   private List<MacroAction> open_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.WaitForGuiAction waitGui = new autismclient.util.macro.WaitForGuiAction();
      waitGui.guiType = "CONTAINER";
      actions.add(waitGui);
      autismclient.util.macro.InstaBreakAction mine = new autismclient.util.macro.InstaBreakAction();
      if (init_Minecraft.hitResult != null && init_Minecraft.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)init_Minecraft.hitResult;
         mine.blockPos = hit.getBlockPos();
         mine.direction = hit.getDirection();
      }

      mine.autoPickaxe = true;
      mine.times = 1;
      mine.delayTicks = 0;
      actions.add(mine);
      autismclient.util.macro.ContainerClickSequenceAction clicks = new autismclient.util.macro.ContainerClickSequenceAction();
      clicks.slotSource = autismclient.util.macro.ContainerClickSequenceAction.Inner_StreamUtil.RANGE;
      clicks.startSlot = 100;
      clicks.endSlot = 126;
      clicks.containerInput = "QUICK_MOVE";
      actions.add(clicks);
      return actions;
   }

   private List<MacroAction> poll_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SelectSlotAction select = new autismclient.util.macro.SelectSlotAction();
      select.itemName = "Trident or bow";
      select.strategy = autismclient.util.macro.SelectSlotAction.Inner_DataUtil.BEST_DURABILITY;
      actions.add(select);
      autismclient.util.macro.UseItemPhaseAction phase = new autismclient.util.macro.UseItemPhaseAction();
      phase.phase = autismclient.util.macro.UseItemPhaseAction.Inner_DataUtil.START_USE;
      phase.holdTicks = 100;
      phase.gateDuringHold = true;
      phase.gatePlayerActions = true;
      phase.gateContainerClicks = true;
      phase.useCustomSlotMapping = true;
      phase.swapSlotBeforeRelease = 103;
      phase.swapButton = 0;
      phase.releaseAfterHold = true;
      phase.dropSlotAfterRelease = true;
      phase.dropSlot = 8;
      phase.gateId = "trident-dupe";
      actions.add(phase);
      return actions;
   }

   private List<MacroAction> push_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SignEditAction sign = new autismclient.util.macro.SignEditAction();
      sign.guiName = "SIGN";
      sign.waitForGuiBefore = true;
      sign.line1 = "1";
      sign.sendCommandAfter = true;
      sign.commandAfter = "a";
      actions.add(sign);
      return actions;
   }

   private List<MacroAction> pop_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SignEditAction sign = new autismclient.util.macro.SignEditAction();
      sign.guiName = "SIGN";
      sign.waitForGuiBefore = true;
      sign.line1 = "e";
      sign.sendClosePacketAfter = true;
      sign.closePacketContainerId = 0;
      actions.add(sign);
      return actions;
   }

   private List<MacroAction> tick_List() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.Inner_DataUtil.HELD_ITEM;
      assertBundle.itemName = "Bundle";
      assertBundle.message = "Must hold a bundle";
      actions.add(assertBundle);
      autismclient.util.macro.PacketBurstAction burst = new autismclient.util.macro.PacketBurstAction();
      burst.mode = autismclient.util.macro.PacketBurstAction.Inner_DataUtil.BUNDLE_SELECT;
      burst.count = 1;
      burst.slot = -1;
      burst.bundleIndex = -1337;
      actions.add(burst);
      autismclient.util.macro.UseItemPhaseAction use = new autismclient.util.macro.UseItemPhaseAction();
      use.phase = autismclient.util.macro.UseItemPhaseAction.Inner_DataUtil.USE_ONCE;
      use.itemName = "Bundle";
      actions.add(use);
      return actions;
   }

   private List<MacroAction> run_List() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.VillagerDupeAction dupe = new autismclient.util.macro.VillagerDupeAction();
      dupe.count = 0;
      actions.add(dupe);
      return actions;
   }

   private void init_V_List_Str_2(List<MacroAction> actions, String gateId) {
      autismclient.util.macro.FinallyAction finallyAction = new autismclient.util.macro.FinallyAction();
      finallyAction.bodyCount = 1;
      actions.add(finallyAction);
      autismclient.util.macro.EndPacketGateAction disable = new autismclient.util.macro.EndPacketGateAction(gateId);
      actions.add(disable);
   }

   private List<AutismMacroEditorOverlay.Inner_NetHelper> init_List_AutismMacroEditorO(AutismMacroEditorOverlay.Inner_JsonUtil mode) {
      List<AutismMacroEditorOverlay.Inner_NetHelper> entries = new ArrayList<>();
      if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.ACTION) {
         this.init_V_List(entries, "flow", "Chat", "Send a chat message or slash command.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SendChatAction()));
         this.init_V_List(
            entries, "flow", "Delay", "Pause for a time in ms or ticks.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.DelayAction(1000))
         );
         this.init_V_List(
            entries,
            "flow",
            "FPS",
            "Cap max FPS (0 = freeze). End on a timer, after N following steps, or at the end of the macro.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.FpsAction())
         );
         this.init_V_List(
            entries,
            "flow",
            "Ping",
            "Spoof your ping by adding latency (ms). End on a timer, after N following steps, or at the end of the macro.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.SPingAction())
         );
         this.init_V_List(
            entries, "flow", "Repeat", "Repeat the next steps a chosen number of times.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.RepeatAction())
         );
         this.init_V_List(
            entries,
            "flow",
            "If",
            "Run THEN steps when a condition holds, otherwise the ELSE steps.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.IfAction())
         );
         this.init_V_List(
            entries,
            "flow",
            "Goto",
            "Jump: skip ahead, loop back, restart at top, go to a Label, or stop.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.FlowAction())
         );
         this.init_V_List(
            entries,
            "flow",
            "Label",
            "Named anchor that Goto can jump to (build loops with Label + Goto).",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.LabelAction())
         );
         this.init_V_List(entries, "flow", "Stop", "Stop the macro immediately.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.StopMacroAction()));
         this.init_V_List(entries, "network", "Send Toggle", "Turn packet sending on or off.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SendToggleAction()));
         this.init_V_List(entries, "network", "Delay Packets", "Queue packets until you flush them.", this::state_V);
         this.init_V_List(
            entries, "flow", "Branch", "Run or skip compact step groups.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.BranchAction())
         );
         this.init_V_List(entries, "flow", "Finally", "Register cleanup rows.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.FinallyAction()));
         this.init_V_List(entries, "flow", "Variables", "Set macro variables.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.MacroVariablesAction()));
         this.init_V_List(entries, "flow", "Capture Value", "Reuse game text later.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.CaptureValueAction()));
         this.init_V_List(
            entries,
            "flow",
            "GM",
            "Change game mode for real, or fake it client-side.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.FakeGamemodeAction())
         );
         this.init_V_List(
            entries, "flow", "Save GUI", "Remember the current GUI for later restore.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SaveGuiAction())
         );
         this.init_V_List(entries, "flow", "Restore GUI", "Reopen the last saved GUI state.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.RestoreGuiAction()));
         this.init_V_List(entries, "movement", "Rotate", "Set player yaw and pitch.", this::prev_V_3);
         this.init_V_List(entries, "movement", "Look At", "Face a specific position, nearest block, or nearest entity.", this::valid_V);
         this.init_V_List(entries, "movement", "Sneak", "Hold or release sneak.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SneakAction()));
         this.init_V_List(
            entries, "movement", "Jump", "Press jump for a chosen duration.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.JumpAction())
         );
         this.init_V_List(entries, "movement", "Sprint", "Toggle sprint state.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SprintAction()));
         this.init_V_List(entries, "movement", "Move", "Walk in a direction for N ticks.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.MoveAction()));
         this.init_V_List(
            entries, "movement", "VClip", "Vertical clip with configurable packet behavior.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.VClipAction())
         );
         this.init_V_List(
            entries,
            "movement",
            "HClip",
            "Horizontal clip forward with configurable packet behavior.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.HClipAction())
         );
         this.init_V_List(entries, "movement", "TP", "Ground-first HClip teleport.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.PacedTpAction()));
         this.init_V_List(entries, "movement", "Go To", "Pathfind to a target.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.GoToAction()));
         if (!AutismLiteVariant.enabled()) {
            this.init_V_List(
               entries, "movement", "Tools", "Run a tool, wait for it, or control it.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.ToolsAction())
            );
         }

         this.init_V_List(entries, "movement", "Mine", "Mine target blocks autonomously.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.MineAction()));
         this.init_V_List(
            entries,
            "interaction",
            "Item Click",
            "Click items or slots in open inventories.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.ItemAction())
         );
         this.init_V_List(
            entries,
            "interaction",
            "Pick Up All",
            "Pick up matching stacks into the carried item.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.PickUpAllAction())
         );
         this.init_V_List(
            entries, "interaction", "Use Item", "Use the held item or select one by name.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.UseItemAction())
         );
         this.init_V_List(
            entries, "interaction", "Use Phase", "Start, release, swing, or use once.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.UseItemPhaseAction())
         );
         this.init_V_List(entries, "inventory", "Open Inventory", "Open the player inventory screen.", this::cache_V);
         this.init_V_List(
            entries, "inventory", "Slot", "Select a hotbar slot or item by name.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SelectSlotAction())
         );
         this.init_V_List(
            entries,
            "interaction",
            "Click Sequence",
            "Click slots, ranges, or lists.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.ContainerClickSequenceAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "XCarry",
            "Store four grid items plus one cursor item, or cursor-only when selected.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.XCarryAction())
         );
         this.init_V_List(
            entries, "inventory", "Drop", "Drop items from chosen slots.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.DropAction())
         );
         this.init_V_List(entries, "inventory", "Swap", "Swap two inventory slots.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SwapSlotsAction()));
         this.init_V_List(entries, "interaction", "Open Container", "Open a targeted container block.", this::data_V);
         this.init_V_List(entries, "interaction", "Interact Entity", "Right-click a captured entity or block.", this::value_V);
         this.init_V_List(
            entries, "interaction", "Villager Dupe", "Shear villager trade items", () -> this.init_V_MacroAction_2(new autismclient.util.macro.VillagerDupeAction())
         );
         this.init_V_List(
            entries, "interaction", "Wait Villager Item", "Wait for a shearable item", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitVillagerItemAction())
         );
         this.init_V_List(
            entries,
            "interaction",
            "Place",
            "Place a selected item at a captured block position and face.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.PlaceAction())
         );
         this.init_V_List(
            entries, "inventory", "Store", "Move items between player and containers.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.StoreItemAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "Inventory Audit",
            "Snapshot slots, compare after reopen, or run automated dupe tests.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.InventoryAuditAction())
         );
         this.init_V_List(
            entries, "inventory", "Rollback", "Duplicate selected local container items.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.RollbackAction())
         );
         this.init_V_List(
            entries, "inventory", "Trade", "Trade in the open villager screen.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.TradeAction())
         );
         this.init_V_List(
            entries, "inventory", "Craft", "Craft one or more recipes.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.CraftAction())
         );
         this.init_V_List(
            entries,
            "interaction",
            "Mouse Click",
            "Simulate mouse clicks in world.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.ClickAction(autismclient.util.macro.ClickAction.Inner_DataUtil.RIGHT))
         );
         this.init_V_List(
            entries, "network", "Packet", "Send captured packets from the queue.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SendPacketAction())
         );
         this.init_V_List(entries, "network", "Packet Gate", "Cancel, delay, or allow packet groups.", () -> {
            autismclient.util.macro.PacketGateAction g = new autismclient.util.macro.PacketGateAction();
            g.gateId = UUID.randomUUID().toString();
            boolean wholePassDefault = this.init_aM != null && this.init_aM.init_Z && this.init_aM.init_List.isEmpty();
            if (wholePassDefault) {
               g.scope = autismclient.util.macro.PacketGateAction.Inner_BufferHelper.MACRO_PASS;
            }

            this.init_V_MacroAction_2(g);
            if (!wholePassDefault) {
               this.init_V_MacroAction_2(new autismclient.util.macro.EndPacketGateAction(g.gateId));
            }
         });
         this.init_V_List(entries, "network", "Packet Burst", "Generate packet bursts.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.PacketBurstAction()));
         this.init_V_List(
            entries,
            "network",
            "Custom Menu",
            "Fill and submit a server-defined dialog or form.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.CustomMenuAction())
         );
         this.init_V_List(
            entries,
            "network",
            "Payload",
            "Send and edit custom payload packets with raw bytes or runtime Java.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.PayloadAction())
         );
         this.init_V_List(
            entries,
            "network",
            "Close GUI",
            "Close the current screen with optional no-packet mode.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.CloseGuiAction())
         );
         this.init_V_List(
            entries,
            "network",
            "Desync",
            "Create GUI desync without closing locally.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.DesyncAction())
         );
         this.init_V_List(entries, "network", "NBT Book", "Write large custom books.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.NbtBookAction()));
         this.init_V_List(entries, "network", "Sign Edit", "Send sign update lines.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.SignEditAction()));
         this.init_V_List(
            entries,
            "network",
            "Disconnect",
            "Disconnect or kick using selected mode.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.DisconnectAction())
         );
         this.init_V_List(
            entries,
            "network",
            "Join",
            "Join a server (blank = last) and continue once in (Playable by default).",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.JoinAction())
         );
         this.init_V_List(entries, "flow", "Module", "Toggle AUTISM modules.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.ak()));
         this.init_V_List(entries, "flow", "Assert", "Validate preconditions.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.AssertAction()));
         this.init_V_List(entries, "flow", "Pay", "Send payments to a player list.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.PayAction()));
         this.init_V_List(entries, "interaction", "InstaBreak", "Instant rebreak for one captured block.", this::entry_V);
         this.init_V_List(entries, "interaction", "Break", "Legit vanilla break with break-progress tracking + optional GUI-race interact.", this::node_V);
         this.init_V_List(
            entries, "flow", "Race", "Run grouped actions when a condition fires.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.RaceAction())
         );
         this.init_V_List(
            entries,
            "flow",
            "Report",
            "Measure time between macro, packet, GUI, teleport, or position events.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.ReportAction())
         );
         this.init_V_List(
            entries, "flow", "Start Macro", "Start another macro from the macro library.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.StartMacroAction())
         );
         this.init_V_List(
            entries, "flow", "Stop Macro", "Stop this, all, or a selected macro.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.StopMacroAction())
         );
      } else if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.PRESET) {
         this.init_V_List(entries, "singleplayer", "Rollback", "Local container duplicate", () -> this.write(this.sync_List_2()));
         this.init_V_List(entries, "dupe", "Bundle Dupe V3", "Paper / Spigot", () -> this.write(this.update_List_2()));
         this.init_V_List(entries, "dupe", "Bundle Dupe V2", "Paper / Spigot", () -> this.write(this.flush_List_2()));
         this.init_V_List(entries, "dupe", "Book Dupe", "Paper < 1.21.1", () -> this.write(this.close_List_2()));
         this.init_V_List(entries, "dupe", "Shulker Dupe", "Vanilla 1.19 & below", () -> this.write(this.open_List_2()));
         this.init_V_List(entries, "dupe", "Trident Dupe", "Vanilla < 1.19", () -> this.write(this.poll_List_2()));
         this.init_V_List(entries, "dupe", "Villager Dupe", "Saddle / armor / carpet", () -> this.write(this.run_List()));
         this.init_V_List(entries, "trade", "AxTrade Sign", "Patched", () -> this.write(this.push_List_2()));
         this.init_V_List(entries, "trade", "LoverFella Sign", "Patched", () -> this.write(this.pop_List_2()));
         this.init_V_List(entries, "crash", "Bundle Crash", "1.21.4", () -> this.write(this.tick_List()));
      } else {
         this.init_V_List(
            entries,
            "player",
            "Health",
            "Pause until your health drops below or rises above a target.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForHealthAction(20.0F, true))
         );
         this.init_V_List(
            entries, "player", "Cooldown", "Pause until an item cooldown is ready.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForCooldownAction())
         );
         this.init_V_List(
            entries,
            "player",
            "Gamemode",
            "Pause until real or optionally fake gamemode changes.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitGamemodeChangeAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "Slot or Item",
            "Pause until slots or inventory contents change.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForSlotChangeAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "Durability",
            "Pause until a held, selected, or slotted item reaches durability.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitDurabilityAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "Free Slots",
            "Pause until free or filled inventory slots match.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitFreeSlotsAction())
         );
         this.init_V_List(
            entries,
            "world",
            "Movement",
            "Pause until a position, move-distance, world change, or teleport — pick the type inside.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitMovementAction())
         );
         this.init_V_List(entries, "world", "Block", "Pause until a block is placed or broken.", this::queue_V);
         this.init_V_List(entries, "world", "Entity", "Pause until a matching entity appears.", this::buffer_V);
         this.init_V_List(entries, "world", "Sound", "Pause until a matching sound plays.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForSoundAction()));
         this.init_V_List(entries, "gui_chat", "GUI", "Pause until a GUI opens or closes.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForGuiAction()));
         this.init_V_List(entries, "gui_chat", "Chat", "Pause until chat matches a pattern.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForChatAction()));
         this.init_V_List(entries, "sync", "LAN", "Pause until a LAN peer reaches a step.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForLanStepAction()));
         this.init_V_List(
            entries,
            "sync",
            "Macro",
            "Pause until another macro starts, completes a step, or finishes.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForMacroStepAction())
         );
         this.init_V_List(
            entries,
            "packets",
            "Packet",
            "Pause until a chosen C2S or S2C packet appears.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitForPacketAction(""))
         );
         this.init_V_List(
            entries, "packets", "Packet Match", "Pause for packet plus field predicate.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitPacketMatchAction())
         );
         this.init_V_List(
            entries,
            "inventory",
            "Inventory",
            "Pause for item, count, slot, cursor, or fullness.",
            () -> this.init_V_MacroAction_2(new autismclient.util.macro.WaitInventoryPredicateAction())
         );
         this.init_V_List(entries, "sync", "Tick Sync", "Wait for the next client tick.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.TickSyncAction()));
         this.init_V_List(entries, "sync", "Revision Sync", "Wait for handler revision sync.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.RevisionSyncAction()));
         this.init_V_List(
            entries, "sync", "Server Sync", "Wait for the server tick tracker.", () -> this.init_V_MacroAction_2(new autismclient.util.macro.ServerTickSyncAction())
         );
      }

      if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.ACTION) {
         this.init_V_List_Z_2(entries, false);
      } else if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.PRESET) {
         this.read_V(entries);
      } else {
         this.init_V_List_Z_2(entries, true);
      }

      return entries;
   }

   private void init_V_List_Z_2(List<AutismMacroEditorOverlay.Inner_NetHelper> entries, boolean conditions) {
      for (MacroActionEntry entry : MacroActionRegistry.entries()) {
         if (entry.hasPicker() && entry.isCondition() == conditions) {
            Supplier<MacroAction> factory = entry.factory();
            this.init_V_List(entries, entry.pickerCategory(), entry.pickerLabel(), entry.pickerTip(), () -> {
               MacroAction action;
               try {
                  action = factory.get();
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("[MacroActions] Addon step '{}' factory failed", entry.pickerLabel(), t);
                  return;
               }

               if (action != null) {
                  this.init_V_MacroAction_2(action);
               }
            });
         }
      }
   }

   private void read_V(List<AutismMacroEditorOverlay.Inner_NetHelper> entries) {
      for (MacroPresetRegistry.PresetEntry preset : MacroPresetRegistry.entries()) {
         Supplier<List<MacroAction>> builder = preset.builder();
         this.init_V_List(entries, preset.categoryId(), preset.label(), preset.tip(), () -> {
            List<MacroAction> stack;
            try {
               stack = builder.get();
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[MacroPresets] Addon preset '{}' failed to build", preset.label(), t);
               AutismClientMessaging.sendPrefixed("§cAddon preset '" + preset.label() + "' failed.");
               return;
            }

            if (stack != null && !stack.isEmpty()) {
               this.write(stack);
            }
         });
      }
   }

   private List<AutismMacroEditorOverlay.Inner_TaskQueue> init_List_AutismMacroEditorO_List(
      AutismMacroEditorOverlay.Inner_JsonUtil mode, List<AutismMacroEditorOverlay.Inner_NetHelper> entries
   ) {
      List<AutismMacroEditorOverlay.Inner_TaskQueue> categories = new ArrayList<>();
      Consumer<AutismMacroEditorOverlay.Inner_TaskQueue> addCategory = category -> {
         for (AutismMacroEditorOverlay.Inner_NetHelper entry : entries) {
            if (entry.init_Str.equals(category.init_Str)) {
               categories.add(category);
               return;
            }
         }
      };
      if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.ACTION) {
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("flow", "Flow", -11352981));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("movement", "Movement", -42406));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("interaction", "Interaction", -12268476));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("inventory", "Inventory", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("network", "Packets", -10835457));
      } else if (mode == AutismMacroEditorOverlay.Inner_JsonUtil.PRESET) {
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("singleplayer", "Singleplayer", -12268476));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("dupe", "Duplication", -10835457));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("trade", "Trade", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("crash", "Crash", -4555521));
      } else {
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("player", "Player", -42406));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("inventory", "Inventory", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("world", "World", -11352981));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("gui_chat", "GUI / Chat", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("packets", "Packets", -10835457));
         addCategory.accept(new AutismMacroEditorOverlay.Inner_TaskQueue("sync", "Sync", -10835457));
      }

      Set<String> known = new HashSet<>();

      for (AutismMacroEditorOverlay.Inner_TaskQueue c : categories) {
         known.add(c.init_Str);
      }

      LinkedHashSet<String> extra = new LinkedHashSet<>();

      for (AutismMacroEditorOverlay.Inner_NetHelper entry : entries) {
         if (!known.contains(entry.init_Str)) {
            extra.add(entry.init_Str);
         }
      }

      for (String catId : extra) {
         MacroActionRegistry.ActionCategory actionCat = MacroActionRegistry.category(catId);
         String label;
         int color;
         if (actionCat != null) {
            label = actionCat.label();
            color = actionCat.color();
         } else {
            String preset = MacroPresetRegistry.categoryLabel(catId);
            label = preset != null ? preset : load_Str_Str_2(catId);
            color = -5609729;
         }

         categories.add(new AutismMacroEditorOverlay.Inner_TaskQueue(catId, label, color));
      }

      return categories;
   }

   private static String load_Str_Str_2(String id) {
      if (id != null && !id.isBlank()) {
         if (id.startsWith("addon:")) {
            String addonId = id.substring("addon:".length());
            int slash = addonId.indexOf(47);
            if (slash >= 0) {
               addonId = addonId.substring(0, slash);
            }

            autismclient.b.DataUtil.StreamUtil report = autismclient.b.DataUtil.init_DataUtil_StreamUti(addonId);
            return report != null && report.load_Str() != null && !report.load_Str().isBlank()
               ? autismclient.b.DataUtil.load_Str(report.load_Str(), addonId)
               : autismclient.b.DataUtil.load_Str(addonId, "Addon");
         } else {
            String[] parts = id.replace('_', ' ').replace('-', ' ').trim().split("\\s+");
            StringBuilder sb = new StringBuilder();

            for (String part : parts) {
               if (!part.isEmpty()) {
                  if (sb.length() > 0) {
                     sb.append(' ');
                  }

                  sb.append(Character.toUpperCase(part.charAt(0)));
                  if (part.length() > 1) {
                     sb.append(part.substring(1));
                  }
               }
            }

            return sb.length() == 0 ? "Addon" : sb.toString();
         }
      } else {
         return "Addon";
      }
   }

   private void init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil mode) {
      if (!this.queue_Z() && mode != null) {
         this.reset_V_3();
         this.init_AutismMacroEditorO_3 = mode;
         List<AutismMacroEditorOverlay.Inner_NetHelper> entries = this.init_List_AutismMacroEditorO(mode);
         this.init_a.init_V_List(entries);
         this.init_a.init_V_Str("");
         this.read_k = new ThemedTextInputBox(init_Minecraft, this.init_Font, 0, 0, 1, 16, false);
         this.read_k.init_V_Component(Component.literal("Search..."));
         this.read_k.init_V_Consumer(ignored -> this.task_V());
         this.task_V();
         this.buffer = 0;
         this.flush_Z = false;
         if (this.init_k != null) {
            this.init_k.push(false);
         }

         if (this.load_k != null) {
            this.load_k.push(false);
         }

         OverlayManager manager = OverlayManager.init_bk();
         manager.init_V(this.init_AutismMacroEditorO_4);
         manager.update_V(this.init_AutismMacroEditorO_4);
      }
   }

   private List<AutismMacroEditorOverlay.Inner_NetHelper> get_List() {
      return !this.valid_Z() ? Collections.emptyList() : this.open_List;
   }

   private List<AutismMacroEditorOverlay.Inner_NetHelper> init_List_Str_2(String categoryId) {
      return categoryId != null && !categoryId.isEmpty() ? this.init_Map.getOrDefault(categoryId, Collections.emptyList()) : Collections.emptyList();
   }

   private void task_V() {
      String query = this.read_k == null ? "" : this.read_k.load_Str();
      this.init_a.init_V_Str(query);
      this.open_List = this.init_a.init_List();
      this.close_List = this.init_List_AutismMacroEditorO_List(this.init_AutismMacroEditorO_3, this.open_List);
      Map<String, List<AutismMacroEditorOverlay.Inner_NetHelper>> grouped = new LinkedHashMap<>();

      for (AutismMacroEditorOverlay.Inner_NetHelper entry : this.open_List) {
         grouped.computeIfAbsent(entry.init_Str, ignored -> new ArrayList<>()).add(entry);
      }

      Map<String, List<AutismMacroEditorOverlay.Inner_NetHelper>> frozen = new LinkedHashMap<>();

      for (Entry<String, List<AutismMacroEditorOverlay.Inner_NetHelper>> entry : grouped.entrySet()) {
         frozen.put(entry.getKey(), List.copyOf(entry.getValue()));
      }

      this.init_Map = Map.copyOf(frozen);
      this.read_J++;
      this.buffer = 0;
   }

   private void pool_V() {
      if (this.read_k != null && this.valid_Z()) {
         int pickerX = this.B();
         int pickerY = this.C();
         int pickerW = this.x();
         this.read_k.read(pickerX + 6);
         this.read_k.write(pickerY + 21);
         this.read_k.update(Math.max(1, pickerW - 12));
         this.read_k.sync(16);
      }
   }

   private int poll(int gridW) {
      int columnGap = 6;
      int minColumnW = 128;
      int maxColumns = 1;
      return Math.max(1, Math.min(maxColumns, (gridW + columnGap) / (minColumnW + columnGap)));
   }

   private int init_I_List_I_2(List<AutismMacroEditorOverlay.Inner_NetHelper> entries, int columnW) {
      int cardGap = 3;
      int rowGap = 1;
      int cardH = 14;
      int sectionLabelH = this.G();
      int buttonW = Math.max(1, (columnW - cardGap) / 2);
      int buttonX = 0;
      int buttonY = sectionLabelH + 2;

      for (AutismMacroEditorOverlay.Inner_NetHelper entry : entries) {
         if (buttonX > 0 && buttonX + buttonW > columnW) {
            buttonX = 0;
            buttonY += cardH + rowGap;
         }

         buttonX += buttonW + cardGap;
      }

      return entries.isEmpty() ? sectionLabelH : buttonY + cardH;
   }

   private List<AutismMacroEditorOverlay.Inner_SyncManager> init_List_I(int gridX, int startY, int gridW) {
      int cardGap = 3;
      int rowGap = 1;
      int cardH = 14;
      int sectionLabelH = this.G();
      int sectionGap = 3;
      int columnGap = 6;
      int columnCount = this.poll(gridW);
      int columnW = Math.max(1, (gridW - columnGap * (columnCount - 1)) / columnCount);
      int targetColumnHeight = Math.max(96, (init_Minecraft.getWindow() != null ? cA.load_I() : 320) - 96);
      List<AutismMacroEditorOverlay.Inner_SyncManager> layouts = new ArrayList<>();
      int column = 0;
      int[] columnY = new int[columnCount];
      Arrays.fill(columnY, startY);

      for (AutismMacroEditorOverlay.Inner_TaskQueue category : this.close_List) {
         List<AutismMacroEditorOverlay.Inner_NetHelper> categoryEntries = this.init_List_Str_2(category.init_Str);
         if (!categoryEntries.isEmpty()) {
            int blockHeight = this.init_I_List_I_2(categoryEntries, columnW);
            if (column < columnCount - 1 && columnY[column] > startY && columnY[column] + blockHeight > startY + targetColumnHeight) {
               column++;
            }

            int sectionX = gridX + column * (columnW + columnGap);
            int sectionY = columnY[column];
            int firstRowY = sectionY + sectionLabelH + 2;
            List<AutismMacroEditorOverlay.Inner_ConfigStore> buttons = this.init(categoryEntries, sectionX, firstRowY, columnW, cardGap, cardH, rowGap);
            int bottomY = buttons.isEmpty() ? sectionY + sectionLabelH : buttons.get(buttons.size() - 1).load + cardH;
            layouts.add(new AutismMacroEditorOverlay.Inner_SyncManager(category, sectionX, sectionY, columnW, buttons, bottomY));
            columnY[column] = bottomY + sectionGap;
         }
      }

      return layouts;
   }

   private List<AutismMacroEditorOverlay.Inner_ConfigStore> init(
      List<AutismMacroEditorOverlay.Inner_NetHelper> entries, int sectionX, int firstRowY, int columnW, int cardGap, int cardH, int rowGap
   ) {
      List<AutismMacroEditorOverlay.Inner_ConfigStore> buttons = new ArrayList<>();
      if (entries != null && !entries.isEmpty()) {
         List<AutismMacroEditorOverlay.Inner_NetHelper> ordered = this.read_List(entries);
         int minBtn = Math.min(34, Math.max(1, (columnW - cardGap) / 2));
         int y = firstRowY;

         for (int i = 0; i < ordered.size(); i += 2) {
            if (i + 1 >= ordered.size()) {
               buttons.add(new AutismMacroEditorOverlay.Inner_ConfigStore(ordered.get(i), sectionX, y, columnW, cardH));
            } else {
               int avail = Math.max(2 * minBtn, columnW - cardGap);
               int n0 = this.write(ordered.get(i).load) + 10;
               int n1 = this.write(ordered.get(i + 1).load) + 10;
               int total = Math.max(1, n0 + n1);
               int w0 = Math.round((float)avail * n0 / total);
               w0 = Math.max(minBtn, Math.min(avail - minBtn, w0));
               int w1 = avail - w0;
               buttons.add(new AutismMacroEditorOverlay.Inner_ConfigStore(ordered.get(i), sectionX, y, w0, cardH));
               buttons.add(new AutismMacroEditorOverlay.Inner_ConfigStore(ordered.get(i + 1), sectionX + w0 + cardGap, y, w1, cardH));
            }

            y += cardH + rowGap;
         }

         return buttons;
      } else {
         return buttons;
      }
   }

   private List<AutismMacroEditorOverlay.Inner_NetHelper> read_List(List<AutismMacroEditorOverlay.Inner_NetHelper> entries) {
      if (entries.size() <= 2) {
         return new ArrayList<>(entries);
      }

      List<AutismMacroEditorOverlay.Inner_NetHelper> sorted = new ArrayList<>(entries);
      sorted.sort(Comparator.comparingInt(e -> this.write(e.load)));
      List<AutismMacroEditorOverlay.Inner_NetHelper> ordered = new ArrayList<>(entries.size());
      int lo = 0;
      int hi = sorted.size() - 1;

      while (lo <= hi) {
         if (lo == hi) {
            ordered.add(sorted.get(lo));
            break;
         }

         ordered.add(sorted.get(lo++));
         ordered.add(sorted.get(hi--));
      }

      return ordered;
   }

   private int init_I_List_3(List<AutismMacroEditorOverlay.Inner_SyncManager> layouts) {
      int maxBottom = 0;
      if (layouts != null) {
         for (AutismMacroEditorOverlay.Inner_SyncManager layout : layouts) {
            maxBottom = Math.max(maxBottom, layout.write);
         }
      }

      return Math.max(0, maxBottom);
   }

   private int load_I_List(List<AutismMacroEditorOverlay.Inner_SyncManager> layouts) {
      if (layouts != null && !layouts.isEmpty()) {
         int maxRight = 0;
         int minLeft = 2147483647;
         Identifier pickerFont = this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load);
         int pickerColor = this.init_CharEncoder.load(autismclient.gui.f.b.l.read);

         for (AutismMacroEditorOverlay.Inner_SyncManager section : layouts) {
            minLeft = Math.min(minLeft, section.init_I);
            maxRight = Math.max(
               maxRight,
               section.init_I
                  + Math.min(section.read, autismclient.gui.f.b.k.init(this.init_Font, section.init_AutismMacroEditorO.load, pickerFont, pickerColor))
            );

            for (AutismMacroEditorOverlay.Inner_ConfigStore button : section.init_List) {
               minLeft = Math.min(minLeft, button.init_I);
               maxRight = Math.max(maxRight, button.init_I + button.read);
            }
         }

         if (minLeft == 2147483647) {
            minLeft = 0;
         }

         return Math.max(118, maxRight - minLeft);
      } else {
         return 118;
      }
   }

   @Override
   public int init_I() {
      return this.buffer_I();
   }

   @Override
   public int load_I() {
      return this.node_I();
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.tick_I, this.run_I, this.get_I, this.set_I, this.write_Z, this.pop_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.init_cH_cX_cH(this, new OverlayBounds(bounds.init_I, bounds.load_I, this.stop_I_3(), this.prev_I_2(), bounds.init_Z, bounds.load_Z));
         this.tick_I = clamped.init_I;
         this.run_I = clamped.load_I;
         this.get_I = clamped.read;
         this.set_I = clamped.write;
         this.write_Z = clamped.init_Z;
         this.pop_Z = clamped.load_Z;
         if (this.init_aM != null) {
            this.bind_V();
         }
      }
   }

   private CompoundTag init_CompoundTag() {
      return this.init_aM == null ? null : this.init_aM.load_CompoundTag();
   }

   private static boolean init_Z_CompoundTag_CompoundTag(CompoundTag a, CompoundTag b) {
      if (a == b) {
         return true;
      } else {
         return a != null && b != null ? a.equals(b) : false;
      }
   }

   private AutismMacroEditorOverlay.Inner_CachePool init_AutismMacroEditorO() {
      CompoundTag snapshot = this.init_CompoundTag();
      if (snapshot == null) {
         return null;
      }

      List<Integer> selectedIndices = new ArrayList<>();
      if (this.init_aM != null && !this.init_AutismMacroEditorO_2.init_Z()) {
         for (int i = 0; i < this.init_aM.init_List.size(); i++) {
            if (this.read_Z(i)) {
               selectedIndices.add(i);
            }
         }
      }

      return new AutismMacroEditorOverlay.Inner_CachePool(snapshot, List.copyOf(selectedIndices));
   }

   private void init_V_Deque_AutismMacroEditorO(Deque<AutismMacroEditorOverlay.Inner_CachePool> history, AutismMacroEditorOverlay.Inner_CachePool state) {
      if (history != null && state != null && state.init_CompoundTag != null) {
         AutismMacroEditorOverlay.Inner_CachePool head = history.peekFirst();
         if (head != null && init_Z_CompoundTag_CompoundTag(head.init_CompoundTag, state.init_CompoundTag)) {
            history.removeFirst();
         }

         history.addFirst(state);

         while (history.size() > 30) {
            history.removeLast();
         }
      }
   }

   private void index_V() {
      this.init_Deque.clear();
      this.load_Deque.clear();
   }

   private void count_V() {
      this.init_V_Deque_AutismMacroEditorO(this.init_Deque, this.init_AutismMacroEditorO());
      this.load_Deque.clear();
   }

   private boolean size_Z() {
      return this.init_aM != null && !this.init_Deque.isEmpty();
   }

   private boolean data_Z() {
      return this.init_aM != null && !this.load_Deque.isEmpty();
   }

   private void init_V_AutismMacroEditorO_2(AutismMacroEditorOverlay.Inner_CachePool state) {
      if (this.init_aM != null && state != null && state.init_CompoundTag != null) {
         this.init_aM.init_aM_CompoundTag(state.init_CompoundTag);
         this.fill_V();
         this.init_AutismMacroEditorO_2.init_V();

         for (int index : state.init_List) {
            if (index >= 0 && index < this.init_aM.init_List.size()) {
               this.init_AutismMacroEditorO_2.init_V(this.write_I(this.init_aM.init_List.get(index)));
            }
         }

         this.put_V_4();
      }
   }

   private void flags_V() {
      if (!this.queue_Z() && this.size_Z()) {
         AutismMacroEditorOverlay.Inner_CachePool previous = this.init_Deque.pollFirst();
         if (previous != null) {
            this.init_V_Deque_AutismMacroEditorO(this.load_Deque, this.init_AutismMacroEditorO());
            this.init_V_AutismMacroEditorO_2(previous);
         }
      }
   }

   private void level_V() {
      if (!this.queue_Z() && this.data_Z()) {
         AutismMacroEditorOverlay.Inner_CachePool next = this.load_Deque.pollFirst();
         if (next != null) {
            this.init_V_Deque_AutismMacroEditorO(this.init_Deque, this.init_AutismMacroEditorO());
            this.init_V_AutismMacroEditorO_2(next);
         }
      }
   }

   public void init_V_aM(AutismMacro macroToEdit) {
      this.init_V_aM_Z(macroToEdit, false);
   }

   public void init_V_aM_Z(AutismMacro macroToEdit, boolean reopenMacroListOnClose) {
      this.init_V_aM(macroToEdit, reopenMacroListOnClose, AutismMacroEditorOverlay.Inner_DataUtil.NORMAL, null, false);
   }

   public void load_V_aM(AutismMacro sharedMacro) {
      this.init_V_aM(sharedMacro, false, AutismMacroEditorOverlay.Inner_DataUtil.NORMAL, null, true);
   }

   public void init_V_aM_Consumer_2(AutismMacro macroToEdit, Consumer<AutismMacro> onSaveComplete) {
      this.init_V_aM(macroToEdit, false, AutismMacroEditorOverlay.Inner_DataUtil.JOIN_MACRO_MENU, onSaveComplete, false);
   }

   public void load_V_aM_Consumer(AutismMacro macroToEdit, Consumer<AutismMacro> onSaveComplete) {
      this.init_V_aM(macroToEdit, false, AutismMacroEditorOverlay.Inner_DataUtil.NORMAL, onSaveComplete, false);
   }

   private void init_V_aM(
      AutismMacro macroToEdit, boolean reopenMacroListOnClose, AutismMacroEditorOverlay.Inner_DataUtil context, Consumer<AutismMacro> onSaveComplete, boolean forceNew
   ) {
      this.load_aM = null;
      if (this.write_Z) {
         this.next_V_3();
         this.flush_Z = false;
      }

      for (AutismOverlay overlay : OverlayManager.init_bk().init_List()) {
         if (overlay instanceof AutismMacroListOverlay macroListOverlay) {
            macroListOverlay.init_V_Z(false);
         }
      }

      this.read_aM = null;
      this.init_aM = null;
      this.sync_Z = reopenMacroListOnClose;
      this.init_AutismMacroEditorO = context == null ? AutismMacroEditorOverlay.Inner_DataUtil.NORMAL : context;
      this.init_Consumer = onSaveComplete;
      this.write_Z = true;
      AutismSessionState.init_ck().init_V_Z(true);
      ActionEditorOverlay.init_ActionEditorOverla_2().load_V_Z(this.cache_Z());
      if (macroToEdit == null) {
         this.init_aM = new AutismMacro("");
         this.read_aM = null;
         this.update_Z = true;
      } else if (forceNew) {
         this.read_aM = null;
         this.init_aM = new AutismMacro();
         this.init_aM.init_aM_CompoundTag(macroToEdit.load_CompoundTag());
         this.init_aM.init_Str = "";
         this.init_aM.load_I = -1;
         this.update_Z = true;
      } else {
         this.read_aM = macroToEdit;
         this.init_aM = new AutismMacro();
         this.init_aM.init_aM_CompoundTag(macroToEdit.load_CompoundTag());
         this.update_Z = false;
      }

      AutismSessionState.init_ck().init_V_aM(this.init_aM);
      this.update_J = -1L;
      this.chunk_V();
      this.tick_Z = false;
      this.run_Z = false;
      this.init_CompoundTag = null;
      this.get_Z = false;
      this.index_V();
      this.reset_V_3();
      this.set_V_5();
      this.clear_I = 0;
      if (!this.init_aM.init_Z) {
         this.add_I = 0;
      } else if (this.init_aM.init_I == -1) {
         this.add_I = 2;
      } else {
         this.add_I = 1;
      }

      this.add_V_5();
      this.parse_V();
      this.init_CharEncoder_2.read_CharEncoder(!this.pop_Z);
      OverlayManager.init_bk().update_V(this);
   }

   public void write_V_12() {
      this.block_V();
      this.frame_V();
      this.update_V_10();
   }

   private boolean value_Z() {
      return this.init_AutismMacroEditorO == AutismMacroEditorOverlay.Inner_DataUtil.JOIN_MACRO_MENU;
   }

   private boolean cache_Z() {
      boolean inWorld = init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.level != null;
      return (inWorld || !this.set_Z) && !this.value_Z();
   }

   private void frame_V() {
      if (this.tick_Z) {
         if (this.run_Z) {
            String name = this.init_aM == null ? "" : this.init_aM.init_Str;
            AutismMacro savedMacro = name != null && !name.isBlank() ? MacroLibraryManager.init_aN().init_aM_Str(name) : null;
            if (savedMacro == null && this.read_aM != null) {
               savedMacro = this.read_aM;
            }

            if (savedMacro != null) {
               MacroLibraryManager.init_aN().init_List().remove(savedMacro);
               MacroLibraryManager.init_aN().load_V();
            }
         } else if (this.read_aM != null && this.init_CompoundTag != null) {
            this.read_aM.init_aM_CompoundTag(this.init_CompoundTag);
            MacroLibraryManager.init_aN().load_V();
         }

         this.tick_Z = false;
         this.run_Z = false;
         this.init_CompoundTag = null;
         this.get_Z = false;
      }
   }

   private void block_V() {
      AutismMacro runningMacro = this.init_aM();
      if (runningMacro == null || !this.tick_Z && this.load_Z_aM(runningMacro)) {
         if (this.init_aM != null && this.init_aM.init_Str != null && !this.init_aM.init_Str.isBlank()) {
            AutismMacro persisted = MacroLibraryManager.init_aN().init_aM_Str(this.init_aM.init_Str);
            if (this.tick_Z || persisted == null) {
               MacroExecutor.init_V_Str(this.init_aM.init_Str);
            }
         }

         if (this.read_aM != null && this.read_aM.init_Str != null && !this.read_aM.init_Str.isBlank()) {
            AutismMacro persisted = MacroLibraryManager.init_aN().init_aM_Str(this.read_aM.init_Str);
            if (this.tick_Z || persisted == null) {
               MacroExecutor.init_V_Str(this.read_aM.init_Str);
            }
         }
      } else {
         MacroExecutor.load_V_aM(runningMacro);
      }
   }

   private boolean load_Z_aM(AutismMacro candidate) {
      if (candidate != null && candidate.init_Str != null && !candidate.init_Str.isBlank()) {
         AutismMacro persisted = MacroLibraryManager.init_aN().init_aM_Str(candidate.init_Str);
         return persisted == candidate;
      } else {
         return false;
      }
   }

   public void update_V_10() {
      this.block_V();
      if (this.tick_Z) {
         this.frame_V();
      }

      boolean shouldReopenMacroList = this.sync_Z;
      this.scan_V();
      this.load_aM = this.write_Z ? this.init_aM : null;
      this.write_Z = false;
      AutismSessionState.init_ck().init_V_Z(false);
      AutismSessionState.init_ck().init_V_aM(null);
      this.init_aM = null;
      this.read_aM = null;
      this.update_J = -1L;
      this.chunk_V();
      this.tick_Z = false;
      this.run_Z = false;
      this.init_CompoundTag = null;
      this.sync_Z = false;
      this.init_AutismMacroEditorO = AutismMacroEditorOverlay.Inner_DataUtil.NORMAL;
      this.init_Consumer = null;
      ActionEditorOverlay.init_ActionEditorOverla_2().load_V_Z(true);
      this.next_V_3();
      this.push_V();
      this.flush_Z = false;
      this.index_V();
      this.reset_V_3();
      this.set_V_5();
      this.add_V_5();
      if (shouldReopenMacroList) {
         for (AutismOverlay overlay : OverlayManager.init_bk().init_List()) {
            if (overlay instanceof AutismMacroListOverlay macroListOverlay) {
               macroListOverlay.init_V_Z(true);
               OverlayManager.init_bk().update_V(macroListOverlay);
               break;
            }
         }
      }
   }

   private boolean state_Z() {
      this.table_V();
      return this.put_Z;
   }

   private boolean queue_Z() {
      this.table_V();
      return !this.put_Z ? false : System.currentTimeMillis() - this.sync_J >= 100L;
   }

   private void chunk_V() {
      this.close_J = -9223372036854775808L;
      this.open_J = -9223372036854775808L;
   }

   private void table_V() {
      long revision = MacroExecutor.init_J();
      long runId = this.update_J;
      if (this.close_J != revision || this.open_J != runId) {
         boolean running = runId >= 0L && MacroExecutor.init_Z_J(runId);
         if (!running && runId >= 0L) {
            this.update_J = -1L;
            runId = -1L;
         }

         if (!running && this.init_aM() != null) {
            running = true;
         }

         this.close_J = revision;
         this.open_J = runId;
         this.put_Z = running;
      }
   }

   private void store_V() {
      this.next_V_3();
      this.mark_V();
      this.init_F_3.init_V();
      this.set_V_5();
      this.reset_V_3();
      if (this.init_k != null) {
         this.init_k.push(false);
      }

      if (this.load_k != null) {
         this.load_k.push(false);
      }

      this.flush_Z = false;
   }

   private void drain_V() {
      boolean locked = this.queue_Z();
      if (locked != this.get_Z) {
         this.get_Z = locked;
         if (locked) {
            this.store_V();
         } else {
            this.reset_V_3();
         }

         this.bind_V();
      }
   }

   private AutismMacro init_aM() {
      if (this.read_aM != null && MacroExecutor.init_Z_aM(this.read_aM)) {
         return this.read_aM;
      } else if (this.init_aM != null && MacroExecutor.init_Z_aM(this.init_aM)) {
         return this.init_aM;
      } else if (this.init_aM != null && this.init_aM.init_Str != null && !this.init_aM.init_Str.isBlank() && MacroExecutor.init_Z_Str(this.init_aM.init_Str)) {
         return this.init_aM;
      } else {
         return this.read_aM != null && this.read_aM.init_Str != null && !this.read_aM.init_Str.isBlank() && MacroExecutor.init_Z_Str(this.read_aM.init_Str)
            ? this.read_aM
            : null;
      }
   }

   private void fill_V() {
      if (this.init_aM != null) {
         this.mark_V();
         if (this.init_aM.init_List.isEmpty() && this.state_Z()) {
            MacroExecutor.init_V_Str(this.init_aM.init_Str);
         }

         this.read(true);
         this.put_V_4();
         this.clear_V_4();
         this.reset_V_3();
         this.bind_V();
      }
   }

   private void mark_V() {
      ActionEditorOverlay overlay = ActionEditorOverlay.load_ActionEditorOverla();
      if (overlay != null && this.init_aM != null) {
         overlay.init_V_List(this.init_aM.init_List);
      }
   }

   private void scan_V() {
      ActionEditorOverlay overlay = ActionEditorOverlay.load_ActionEditorOverla();
      if (overlay != null && this.init_aM != null) {
         overlay.init_V_List(List.of());
      }
   }

   private static String init_Str_PlayerInfo(PlayerInfo entry) {
      if (entry != null && entry.getProfile() != null) {
         String name = entry.getProfile().name();
         return name == null ? "" : name.trim();
      } else {
         return "";
      }
   }

   private static <E extends Enum<E>> E init_Enum_Enum_I(E current, int step) {
      E[] values = (E[])current.getDeclaringClass().getEnumConstants();
      int index = (current.ordinal() + step) % values.length;
      if (index < 0) {
         index += values.length;
      }

      return values[index];
   }

   private static double init_D_D_I(double current, int step) {
      double[] speeds = new double[]{1.0, 2.0, 3.0, 5.0, 10.0};
      int index = 0;

      for (int i = 0; i < speeds.length; i++) {
         if (current <= speeds[i] + 1.0E-4) {
            index = i;
            break;
         }

         index = speeds.length - 1;
      }

      index = (index + step) % speeds.length;
      if (index < 0) {
         index += speeds.length;
      }

      return speeds[index];
   }

   public boolean init_Z_aM(AutismMacro macroToCheck) {
      return macroToCheck != null && this.read_aM != null
         ? this.read_aM == macroToCheck || this.read_aM.init_Str != null && this.read_aM.init_Str.equals(macroToCheck.init_Str)
         : false;
   }

   public void sync_V_9() {
      AutismSessionState shared = AutismSessionState.init_ck();
      this.tick_I = shared.init_I();
      this.run_I = shared.load_I();
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public void init_V_Z(boolean visible) {
      this.write_Z = visible;
      AutismSessionState.init_ck().init_V_Z(visible);
      if (visible) {
         this.init_CharEncoder_2.read_CharEncoder(!this.pop_Z);
         OverlayManager.init_bk().update_V(this);
      } else {
         this.push_V();
      }
   }

   public static void flush_V_9() {
      OverlayScreenController.init_V(init_Minecraft);
   }

   public static void close_V_8() {
   }

   @Override
   public boolean flush_Z() {
      return this.pop_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.pop_Z != collapsed) {
         this.pop_Z = collapsed;
         this.poll_Z = false;
         this.push_Z = false;
         this.reset_V_3();
         if (this.init_k != null) {
            this.init_k.push(false);
         }

         if (this.load_k != null) {
            this.load_k.push(false);
         }

         if (collapsed) {
            this.next_V_3();
            this.get_V();
         }

         this.init_CharEncoder_2.write(!collapsed);
         this.open_V_7();
      }
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      if (!this.sync_List.isEmpty() && autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List)) {
         return true;
      }

      int height = this.F();
      if (mouseX >= this.tick_I && mouseX <= this.tick_I + this.get_I && mouseY >= this.run_I && mouseY <= this.run_I + height) {
         return true;
      }

      if (this.valid_Z()) {
         int pickerX = this.B();
         int pickerY = this.C();
         int pickerW = this.x();
         int pickerH = this.A();
         if (mouseX >= pickerX && mouseX <= pickerX + pickerW && mouseY >= pickerY && mouseY <= pickerY + pickerH) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      return !this.write_Z
         ? false
         : mouseX >= this.tick_I
            && mouseX <= this.tick_I + this.get_I
            && mouseY >= this.run_I
            && mouseY <= this.run_I + this.init_CharEncoder.load()
            && !this.update_Z_D_D_2(mouseX, mouseY);
   }

   @Override
   public boolean write_Z() {
      return !this.write_Z
         ? false
         : this.init_k != null && this.init_k.init_Z() || this.load_k != null && this.load_k.init_Z() || this.read_k != null && this.read_k.init_Z();
   }

   @Override
   public void push_V() {
      if (this.init_k != null) {
         this.init_k.push(false);
      }

      if (this.load_k != null) {
         this.load_k.push(false);
      }

      if (this.read_k != null) {
         this.read_k.push(false);
      }
   }

   @Override
   public boolean e_() {
      return this.write_Z && this.flush_Z;
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      boolean dropdownReleased = autismclient.gui.f.b.DropdownWidget.write(this.sync_List);
      if (this.read_k != null && this.read_k.load(mouseX, mouseY, button)) {
         return true;
      }

      if (this.init_k != null && this.init_k.load(mouseX, mouseY, button)) {
         return true;
      }

      if (this.load_k != null && this.load_k.load(mouseX, mouseY, button)) {
         return true;
      }

      if (button != 0 || this.stop < 0 && this.next < 0) {
         if (button == 0 && this.pool != 0) {
            this.pool = 0;
            this.open_V_7();
            return true;
         }

         if (dropdownReleased) {
            return true;
         }

         if (button != 0) {
            return false;
         }

         if (!this.poll_Z) {
            return false;
         }

         boolean headerPressed = this.poll_Z;
         boolean moved = this.push_Z
            || Math.abs(mouseX - this.update_D) >= 3.0
            || Math.abs(mouseY - this.sync_D) >= 3.0
            || this.tick_I != this.value
            || this.run_I != this.cache;
         this.poll_Z = false;
         if (headerPressed && !moved && this.write_Z_D_D_2(mouseX, mouseY) && !this.update_Z_D_D_2(mouseX, mouseY)) {
            this.write_V_Z(!this.pop_Z);
         }

         this.open_V_7();
         this.push_Z = false;
         return true;
      } else {
         this.stop_V_3();
         return true;
      }
   }

   public void open_V_7() {
      AutismSessionState shared = AutismSessionState.init_ck();
      shared.init_V_Z(this.write_Z);
      shared.init_V_I(this.tick_I);
      shared.load_V_I(this.run_I);
      shared.init_V_aM(this.init_aM);
      this.a__V();
   }

   public void poll_V_5() {
      AutismSessionState shared = AutismSessionState.init_ck();
      this.b__V();
      this.write_Z = shared.init_Z();
      this.tick_I = shared.init_I();
      this.run_I = shared.load_I();
      this.init_aM = shared.init_aM();
      this.init_CharEncoder_2.read_CharEncoder(!this.pop_Z);
      if (this.write_Z && this.init_aM != null) {
         this.add_V_5();
         this.bind_V();
      }
   }

   private void parse_V() {
      if (init_Minecraft.getWindow() != null) {
         int screenWidth = cA.init_I();
         int screenHeight = cA.load_I();
         if (this.tick_I == 0 && this.run_I == 0) {
            this.tick_I = screenWidth - this.get_I - this.scan_I();
            this.run_I = this.parse_I();
         }

         this.bind_V();
      }
   }

   private boolean init_Z_Str_3(String name) {
      if (name != null && !name.trim().isEmpty()) {
         return !this.update_Z && name.equals(MacroLibraryManager.init_aN().init_aM_Str(name) != null ? name : "") ? true : MacroLibraryManager.init_aN().init_aM_Str(name) == null;
      } else {
         return false;
      }
   }

   private void bind_V() {
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      this.write_List.clear();
      this.update_List.clear();
      this.sync_List.clear();
      this.put_I = -1;
      boolean stepListLocked = this.queue_Z();
      this.get_Z = stepListLocked;
      int bh = this.bind_I();
      int gap = this.a_I();
      int row1Y = this.run_I + this.n();
      int deleteTopW = !this.update_Z ? this.b_I() : 0;
      int nameFieldW = this.get_I - this.store_I() * 2 - (deleteTopW > 0 ? deleteTopW + gap : 0);
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(init_Minecraft, this.init_Font, this.tick_I + this.store_I(), row1Y, nameFieldW, this.c_I(), false);
         this.init_k.init_V_Component(Component.literal("Macro Name"));
         this.init_k.init_V_Consumer(text -> {
            this.init_aM.init_Str = text;
            this.h_V();
         });
      }

      this.init_k.read(this.tick_I + this.store_I());
      this.init_k.write(row1Y);
      this.init_k.update(nameFieldW);
      this.init_k.init_V_Str(this.init_aM.init_Str);
      if (!this.update_Z) {
         this.init("Delete", this.tick_I + this.get_I - this.store_I() - deleteTopW, row1Y, deleteTopW, bh, () -> {
            if (this.value_Z() && this.init_aM != null && this.init_aM.init_Str != null && this.init_aM.init_Str.equalsIgnoreCase(JoinMacroController.init_Str())) {
               JoinMacroController.init_V("");
            }

            MacroLibraryManager.init_aN().read(this.init_aM);
            this.update_V_10();
         }, !stepListLocked);
      }

      int row2Y = row1Y + this.d_I();
      int bx = this.tick_I + this.store_I();
      String keyText = this.init_aM.load_I == -1 ? "Bind Key" : "Key: " + ValueSink.init_Str(this.init_aM.load_I);
      if (this.flush_Z) {
         keyText = "Press Key/Mouse...";
      }

      int keyW = this.load_I_Str_3(keyText);
      this.init(keyText, bx, row2Y, keyW, bh, () -> {
         this.flush_Z = !this.flush_Z;
         this.bind_V();
      }, !stepListLocked);
      bx += keyW + gap;
      if (!this.value_Z() && !this.set_Z) {
         this.init(this.init_Str_Z_3(this.state_Z()), bx, row2Y, this.f_I(), bh, this::e_V, this.state_Z() || this.node_Z());
         this.put_I = this.init_List.size() - 1;
         bx += this.f_I() + gap;
      }

      if (!this.set_Z && LanSyncManager.init_aE().write_Z() && !this.update_Z && this.init_aM != null) {
         this.init("LAN", bx, row2Y, this.g_I(), bh, () -> {
            if (this.init_aM != null) {
               LanSyncManager.init_aE().load_V(this.init_aM.init_Str);
            }
         }, !stepListLocked);
         bx += this.g_I() + gap;
      }

      List<String> loopOptions = List.of("Once", "Loop #", "Loop Inf");
      int loopAvailable = this.tick_I + this.get_I - this.store_I() - bx - (this.add_I == 1 ? this.h_I() + gap : 0);
      int loopW = Math.max(40, Math.min(this.size_I(), loopAvailable));
      autismclient.gui.f.b.DropdownWidget loopDropdown = new autismclient.gui.f.b.DropdownWidget(
         bx, row2Y, loopW, bh, loopOptions, Math.max(0, Math.min(this.add_I, loopOptions.size() - 1)), idx -> {
            this.add_I = Math.max(0, Math.min(idx, loopOptions.size() - 1));
            if (this.add_I != 1 && this.load_k != null) {
               this.load_k.push(false);
            }

            this.i_V();
            this.bind_V();
         }
      );
      loopDropdown.init_Z = !stepListLocked;
      this.sync_List.add(loopDropdown);
      bx += loopW + gap;
      if (this.add_I == 1) {
         if (this.load_k == null) {
            this.load_k = new ThemedTextInputBox(init_Minecraft, this.init_Font, bx, row2Y, this.h_I(), this.i_I(), false);
            this.load_k.init_V_Consumer(text -> {
               try {
                  this.init_aM.init_I = Integer.parseInt(text.replaceAll("[^0-9-]", ""));
               } catch (NumberFormatException var3x) {
               }
            });
         }

         this.load_k.read(Math.min(bx, this.tick_I + this.get_I - this.store_I() - this.h_I()));
         this.load_k.write(row2Y);
         this.load_k.update(this.h_I());
         this.load_k.init_V_Str(String.valueOf(this.init_aM.init_I == -1 ? 1 : this.init_aM.init_I));
      }

      int builderButtonY = this.pop_I_14();
      int builderGap = this.j_I();
      int builderX = this.tick_I + this.store_I();
      int builderAvailableWidth = this.get_I - this.store_I() * 2;
      int addActionW = this.init_I_Str_4("Add Action");
      int addConditionalW = this.init_I_Str_4("Add Conditional");
      int presetsW = this.read_I_2("Presets");
      int builderGroupGap = this.data_I();
      int builderContentWidth = addActionW + addConditionalW + presetsW + builderGroupGap * 2;
      if (builderContentWidth <= builderAvailableWidth) {
         int groupX = builderX;
         this.init(
            "Add Action",
            groupX,
            builderButtonY,
            addActionW,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.ACTION),
            !stepListLocked
         );
         groupX += addActionW + builderGroupGap;
         this.init(
            "Add Conditional",
            groupX,
            builderButtonY,
            addConditionalW,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.CONDITIONAL),
            !stepListLocked
         );
         groupX += addConditionalW + builderGroupGap;
         this.init(
            "Presets",
            groupX,
            builderButtonY,
            presetsW,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.PRESET),
            !stepListLocked
         );
      } else {
         int builderTotalWidth = builderAvailableWidth - builderGap * 2;
         int conditionalExtra = Math.min(18, Math.max(0, builderTotalWidth / 14));
         int sideBuilderWidth = Math.max(1, (builderTotalWidth - conditionalExtra) / 3);
         int conditionalBuilderWidth = Math.max(1, builderTotalWidth - sideBuilderWidth * 2);
         this.init(
            "Add Action",
            builderX,
            builderButtonY,
            sideBuilderWidth,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.ACTION),
            !stepListLocked
         );
         this.init(
            "Add Conditional",
            builderX + sideBuilderWidth + builderGap,
            builderButtonY,
            conditionalBuilderWidth,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.CONDITIONAL),
            !stepListLocked
         );
         this.init(
            "Presets",
            builderX + sideBuilderWidth + builderGap + conditionalBuilderWidth + builderGap,
            builderButtonY,
            sideBuilderWidth,
            bh,
            () -> this.init_V_AutismMacroEditorO_3(AutismMacroEditorOverlay.Inner_JsonUtil.PRESET),
            !stepListLocked
         );
      }

      int bottomY = this.add_I_10();
      bx = this.tick_I + this.store_I();
      boolean nameEntered = this.init_aM.init_Str != null && !this.init_aM.init_Str.trim().isEmpty();
      int footerAvailableWidth = this.get_I - this.store_I() * 2;
      int saveW = this.read_I_2("Save");
      int cancelW = this.read_I_2("Cancel");
      int undoW = this.read_I_2("Undo");
      int redoW = this.read_I_2("Redo");
      int footerGroupGap = this.value_I();
      if (saveW + cancelW + undoW + redoW + footerGroupGap * 3 <= footerAvailableWidth) {
         this.init("Save", bx, bottomY, saveW, bh, this::j_V, !stepListLocked && nameEntered);
         bx += saveW + footerGroupGap;
         this.init("Cancel", bx, bottomY, cancelW, bh, this::write_V_12);
         bx += cancelW + footerGroupGap;
         this.init("Undo", bx, bottomY, undoW, bh, this::flags_V, !stepListLocked && this.size_Z());
         bx += undoW + footerGroupGap;
         this.init("Redo", bx, bottomY, redoW, bh, this::level_V, !stepListLocked && this.data_Z());
      } else {
         this.init("Save", bx, bottomY, this.k(), bh, this::j_V, !stepListLocked && nameEntered);
         bx += this.k() + this.m();
         this.init("Cancel", bx, bottomY, this.k(), bh, this::write_V_12);
         bx += this.k() + this.m();
         this.init("Undo", bx, bottomY, this.l(), bh, this::flags_V, !stepListLocked && this.size_Z());
         bx += this.l() + this.m();
         this.init("Redo", bx, bottomY, this.l(), bh, this::level_V, !stepListLocked && this.data_Z());
      }
   }

   private void init(String label, int x, int y, int w, int h, Runnable action) {
      this.init(label, x, y, w, h, action, true);
   }

   private void init(String label, int x, int y, int w, int h, Runnable action, boolean enabled) {
      this.init(label, x, y, w, h, action, null, enabled);
   }

   private void init(String label, int x, int y, int w, int h, Runnable action, Runnable secondaryAction) {
      this.init(label, x, y, w, h, action, secondaryAction, true);
   }

   private void init(String label, int x, int y, int w, int h, Runnable action, Runnable secondaryAction, boolean enabled) {
      this.init_List.add(label);
      this.load_List.add(new int[]{x, y, w, h});
      this.read_List.add(action);
      this.write_List.add(secondaryAction);
      this.update_List.add(enabled);
   }

   private void a_V() {
      for (int i = 0; i < this.init_List.size(); i++) {
         String label = this.init_List.get(i);
         if ("Undo".equals(label)) {
            this.update_List.set(i, !this.queue_Z() && this.size_Z());
         } else if ("Redo".equals(label)) {
            this.update_List.set(i, !this.queue_Z() && this.data_Z());
         }
      }
   }

   private void b_V() {
      if (this.put_I >= 0 && this.put_I < this.update_List.size()) {
         this.update_List.set(this.put_I, this.state_Z() || this.node_Z());
      }
   }

   private void e_V() {
      if (this.init_aM == null) {
         AutismClientMessaging.sendPrefixed("No macro to run!");
      } else if (this.entry_Z()) {
         this.f_V();
         this.bind_V();
      } else if (this.init_aM.init_List.isEmpty()) {
         AutismClientMessaging.sendPrefixed("Macro has no actions!");
         this.bind_V();
      } else if (!this.node_Z()) {
         AutismClientMessaging.sendPrefixed(this.write_Str_3());
         this.bind_V();
      } else {
         this.g_V();
      }
   }

   private boolean entry_Z() {
      return this.state_Z();
   }

   private void f_V() {
      if (this.update_J >= 0L) {
         MacroExecutor.init_V_J(this.update_J);
      }

      AutismMacro runningMacro = this.init_aM();
      if (runningMacro != null) {
         MacroExecutor.load_V_aM(runningMacro);
      } else {
         String name = this.init_aM != null ? this.init_aM.init_Str : "";
         if (name != null && !name.isBlank()) {
            MacroExecutor.init_V_Str(name);
         }
      }

      this.update_J = -1L;
      this.chunk_V();
   }

   private void g_V() {
      boolean hasValidName = this.init_aM.init_Str != null && !this.init_aM.init_Str.trim().isEmpty();
      if (hasValidName) {
         AutismMacro existingWithName = MacroLibraryManager.init_aN().init_aM_Str(this.init_aM.init_Str);
         boolean isNameConflict = this.update_Z && existingWithName != null;
         if (isNameConflict) {
            AutismClientMessaging.sendPrefixed("Name already exists: " + this.init_aM.init_Str);
            return;
         }

         if (this.update_Z) {
            MacroLibraryManager.init_aN().init_V_aM(this.init_aM);
            this.read_aM = MacroLibraryManager.init_aN().init_aM_Str(this.init_aM.init_Str);
            this.update_Z = false;
            this.tick_Z = true;
            this.run_Z = true;
         } else {
            if (!this.tick_Z && this.read_aM != null) {
               this.init_CompoundTag = this.read_aM.load_CompoundTag();
            }

            if (this.read_aM != null) {
               String liveFolder = this.read_aM.read;
               this.read_aM.init_aM_CompoundTag(this.init_aM.load_CompoundTag());
               this.read_aM.read = liveFolder;
            }

            MacroLibraryManager.init_aN().load_V();
            this.tick_Z = true;
            this.run_Z = false;
         }

         AutismMacro savedMacro = MacroLibraryManager.init_aN().init_aM_Str(this.init_aM.init_Str);
         this.store_V();
         this.update_J = savedMacro != null ? savedMacro.init_J() : this.init_aM.init_J();
         this.chunk_V();
      } else {
         String originalName = this.init_aM.init_Str;
         this.init_aM.init_Str = "(Unsaved)";
         this.store_V();
         this.update_J = this.init_aM.init_J();
         this.chunk_V();
         this.init_aM.init_Str = originalName;
      }

      if (this.update_J >= 0L) {
         this.sync_J = System.currentTimeMillis();
      }

      this.bind_V();
   }

   private boolean node_Z() {
      return this.write_Str_3() == null;
   }

   private String write_Str_3() {
      if (this.init_aM == null) {
         return "No macro to run!";
      }

      for (MacroAction action : this.init_aM.init_List) {
         if (action instanceof autismclient.util.macro.InventoryAuditAction auditAction && !auditAction.init_Z_2()) {
            return "Inventory Audit needs at least one target.";
         }
      }

      return null;
   }

   private void h_V() {
      this.add_Z = true;
   }

   private void i_V() {
      if (this.add_I == 0) {
         this.init_aM.init_Z = false;
      } else if (this.add_I == 1) {
         this.init_aM.init_Z = true;

         try {
            if (this.load_k != null) {
               this.init_aM.init_I = Integer.parseInt(this.load_k.load_Str());
            }
         } catch (Exception var2) {
            this.init_aM.init_I = 1;
         }
      } else {
         this.init_aM.init_Z = true;
         this.init_aM.init_I = -1;
      }
   }

   public void init_V_MacroAction_2(MacroAction action) {
      if (!this.queue_Z()) {
         this.count_V();
         this.init_aM.init_List.add(action);
         this.fill_V();
         if (this.read_Z(action)) {
            ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
            actionEditor.load_V_Z(this.cache_Z());
            actionEditor.init_V_aM_I(this.init_aM, this.init_aM.init_List.indexOf(action));
            actionEditor.init_V_MacroAction(action, this::count_V, () -> this.load_V_MacroAction_2(action));
         }
      }
   }

   private void write(List<MacroAction> actions) {
      if (!this.queue_Z() && actions != null && !actions.isEmpty()) {
         this.count_V();

         for (MacroAction action : actions) {
            if (action != null) {
               this.init_aM.init_List.add(action);
            }
         }

         this.fill_V();
      }
   }

   private void load_V_MacroAction_2(MacroAction action) {
      if (!this.queue_Z()) {
         if (action instanceof autismclient.util.macro.RaceAction race) {
            this.init_V_L(race);
         } else if (action instanceof autismclient.util.macro.ReportAction report) {
            this.init_V_O(report);
         } else {
            if (action instanceof autismclient.util.macro.PacketGateAction) {
               this.init_V_B((autismclient.util.macro.PacketGateAction)action);
            }

            this.fill_V();
         }
      }
   }

   private void init_V_L(autismclient.util.macro.RaceAction race) {
      if (this.init_aM != null && race != null) {
         int index = this.init_aM.init_List.indexOf(race);
         if (index < 0) {
            this.fill_V();
         } else {
            int oldCount = race.init_I_List_I(this.init_aM.init_List, index);
            List<MacroAction> replacement = new ArrayList<>();
            boolean[] reused = new boolean[Math.max(0, oldCount)];

            for (String step : race.read()) {
               boolean conditionStep = autismclient.util.macro.RaceAction.init_Z_2(step);
               String typeName = autismclient.util.macro.RaceAction.init_Str_2(step);
               MacroAction kept = null;

               for (int offset = 1; offset <= oldCount && index + offset < this.init_aM.init_List.size(); offset++) {
                  int reuseIndex = offset - 1;
                  if (reuseIndex < reused.length && !reused[reuseIndex]) {
                     MacroAction oldAction = this.init_aM.init_List.get(index + offset);
                     if (oldAction != null) {
                        boolean sameType = conditionStep
                           ? autismclient.util.macro.RaceAction.init_Z(oldAction)
                              && autismclient.util.macro.RaceAction.init_L_DataUtil(oldAction).name().equalsIgnoreCase(typeName)
                           : autismclient.util.macro.RaceAction.load(oldAction) && oldAction.getTypeId().equalsIgnoreCase(typeName);
                        if (sameType) {
                           kept = oldAction;
                           reused[reuseIndex] = true;
                           break;
                        }
                     }
                  }
               }

               replacement.add(kept != null ? kept : autismclient.util.macro.RaceAction.init_MacroAction(step));
            }

            if (oldCount > 0) {
               this.init_aM.init_List.subList(index + 1, index + 1 + oldCount).clear();
            }

            this.init_aM.init_List.addAll(index + 1, replacement);
            race.raceSteps = new ArrayList<>(race.read());
            race.load_V();
            race.conditionType = race.conditionTypes.isEmpty() ? "" : race.conditionTypes.get(0);
            race.triggerAction = race.conditionTypes.isEmpty() ? new autismclient.util.macro.DelayAction(0) : race.init_MacroAction();
            race.actionType = race.actionTypes.isEmpty() ? autismclient.util.macro.MacroActionType.PACKET_CLICK.name() : race.actionTypes.get(0);
            race.bodyCount = replacement.size();
            this.fill_V();
         }
      }
   }

   private void init_V_O(autismclient.util.macro.ReportAction report) {
      if (this.init_aM != null && report != null) {
         int index = this.init_aM.init_List.indexOf(report);
         if (index < 0) {
            this.fill_V();
         } else {
            int oldCount = report.init(this.init_aM.init_List, index);
            MacroAction oldStart = oldCount >= 1 && index + 1 < this.init_aM.init_List.size() ? this.init_aM.init_List.get(index + 1) : null;
            MacroAction oldEnd = oldCount >= 2 && index + 2 < this.init_aM.init_List.size() ? this.init_aM.init_List.get(index + 2) : null;
            MacroAction newStart = report.load_MacroAction_2();
            if (oldStart != null && autismclient.util.macro.RaceAction.load(oldStart) && oldStart.getTypeId().equalsIgnoreCase(report.startActionType)) {
               newStart = oldStart;
            }

            MacroAction newEnd = report.read_MacroAction();
            if (oldEnd != null
               && autismclient.util.macro.RaceAction.init_Z(oldEnd)
               && autismclient.util.macro.RaceAction.init_L_DataUtil(oldEnd).name().equalsIgnoreCase(report.endConditionType)) {
               newEnd = oldEnd;
            }

            if (oldCount > 0) {
               this.init_aM.init_List.subList(index + 1, index + 1 + oldCount).clear();
            }

            this.init_aM.init_List.add(index + 1, newStart);
            this.init_aM.init_List.add(index + 2, newEnd);
            report.conditionCount = 2;
            this.fill_V();
         }
      }
   }

   private void j_V() {
      if (!this.queue_Z()) {
         if (this.init_aM != null && this.init_aM.init_List.isEmpty() && this.read_aM != null && !this.read_aM.init_List.isEmpty()) {
            AutismClientMessaging.sendPrefixed("Refusing to save 0 steps over \"" + this.init_aM.init_Str + "\".");
         } else {
            String trimmedName = this.init_aM != null && this.init_aM.init_Str != null ? this.init_aM.init_Str.trim() : "";
            if (trimmedName.isEmpty()) {
               bi.init("Name your macro before saving.", -2075846);
            } else {
               AutismMacro nameOwner = MacroLibraryManager.init_aN().init_aM_Str(trimmedName);
               if (nameOwner != null && nameOwner != this.read_aM) {
                  bi.init("A macro named \"" + trimmedName + "\" already exists.", -2075846);
               } else if (this.init_Z_Str_3(this.init_aM.init_Str)) {
                  String previousName = !this.update_Z && this.read_aM != null && this.read_aM.init_Str != null ? this.read_aM.init_Str : "";
                  AutismMacro savedOrUpdated = this.init_aM;
                  if (this.update_Z) {
                     MacroLibraryManager.init_aN().init_V_aM(this.init_aM);
                     savedOrUpdated = this.init_aM;
                  } else {
                     if (this.read_aM != null) {
                        this.read_aM.init_Str = this.init_aM.init_Str;
                        this.read_aM.load_Str = this.init_aM.load_Str;
                        this.read_aM.init_Z = this.init_aM.init_Z;
                        this.read_aM.init_I = this.init_aM.init_I;
                        this.read_aM.load_I = this.init_aM.load_I;
                        this.read_aM.init_List = new ArrayList<>(this.init_aM.init_List);
                        savedOrUpdated = this.read_aM;
                     }

                     MacroLibraryManager.init_aN().load_V();
                  }

                  if (!previousName.isBlank() && MacroNameNormalizer.load(previousName, savedOrUpdated.init_Str)) {
                     if (!MacroNameNormalizer.init(previousName, savedOrUpdated.init_Str) && LanSyncManager.init_aE().write_Z()) {
                        LanSyncManager.init_aE().read_V_Str(previousName);
                     }
                  }

                  this.tick_Z = false;
                  this.run_Z = false;
                  this.init_CompoundTag = null;
                  Consumer<AutismMacro> callback = this.init_Consumer;
                  if (callback != null) {
                     callback.accept(savedOrUpdated);
                  }

                  this.update_V_10();
               }
            }
         }
      }
   }

   private MacroAction init_MacroAction_MacroAction(MacroAction original) {
      if (original == null) {
         return null;
      }

      try {
         return AutismMacro.init_MacroAction_CompoundTag(original.toTag());
      } catch (Exception var3) {
         return null;
      }
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      boolean ghost = !this.write_Z && this.init_aM == null && this.load_aM != null && this.a__Z();
      if (!ghost && !this.write_Z) {
         this.load_aM = null;
      }

      if (ghost) {
         this.init_aM = this.load_aM;
      }

      try {
         this.load(context, mouseX, mouseY, delta);
      } finally {
         if (ghost) {
            this.init_aM = null;
         }
      }
   }

   private void load(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.add_Z) {
         this.add_Z = false;
         this.bind_V();
      }

      this.drain_V();
      this.a_V();
      this.b_V();
      if (this.write_Z || this.a__Z()) {
         boolean stepListLocked = this.queue_Z();
         int panelMouseX = mouseX;
         int panelMouseY = mouseY;
         if (this.init_Z_I_I_2(mouseX, mouseY)
            || this.init_F_3.init(mouseX, mouseY)
            || !this.sync_List.isEmpty() && autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List)) {
            panelMouseX = -10000;
            panelMouseY = -10000;
         }

         String editorTitle = this.update_Z ? "Create Macro" : "Edit Macro";
         boolean isRunning = this.state_Z();
         if (isRunning) {
            editorTitle = editorTitle + " [RUN]";
         }

         if (this.init_aM != null) {
            editorTitle = editorTitle + " (" + this.init_aM.init_List.size() + " steps)";
         }

         autismclient.gui.f.c.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
         OverlayBounds clamped = this.init_cH_cX_cH(this, new OverlayBounds(this.tick_I, this.run_I, this.stop_I_3(), this.prev_I_2(), this.write_Z, this.pop_Z));
         if (clamped.init_I != this.tick_I || clamped.load_I != this.run_I || clamped.read != this.get_I || clamped.write != this.set_I) {
            this.tick_I = clamped.init_I;
            this.run_I = clamped.load_I;
            this.get_I = clamped.read;
            this.set_I = clamped.write;
            this.bind_V();
         }

         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         boolean active = OverlayManager.init_bk().update_Z(this) || OverlayManager.init_bk().write_Z(this);
         boolean headerHovered = uiMouseX >= this.tick_I
            && uiMouseX < this.tick_I + this.get_I
            && uiMouseY >= this.run_I
            && uiMouseY < this.run_I + this.init_CharEncoder.load();
         this.init_CharEncoder_2.init(editorTitle);
         this.init_CharEncoder_2.load_CharEncoder(!this.pop_Z);
         this.init_CharEncoder_2.update(active);
         this.init_CharEncoder_2.sync(headerHovered);
         int frameHeight = this.F();
         this.init_BitSetUtil.init(Math.max(0, frameHeight - this.init_CharEncoder.load()));
         this.init_CharEncoder_2.init_TableIndex_F(this.tick_I, this.run_I, this.get_I, frameHeight);
         this.init_TextFormat.init(context, mouseX, mouseY, delta);
         this.init_V_GuiGraphicsExtract_13(context, mouseX, mouseY, this.load_cH());
         int bodyClipTop = this.run_I + this.init_CharEncoder.load();
         int bodyClipBottom = this.init_CharEncoder_2.init_I_2();
         if (bodyClipBottom > bodyClipTop + 1) {
            viewport.init(context, this.tick_I, bodyClipTop, this.tick_I + this.get_I, bodyClipBottom);

            try {
               if (this.init_k != null) {
                  this.init_k.init_V(context, panelMouseX, panelMouseY, delta);
               }

               if (this.add_I == 1 && this.load_k != null) {
                  this.load_k.init_V(context, panelMouseX, panelMouseY, delta);
               }

               if (!this.init_Z_Str_3(this.init_aM.init_Str)) {
                  String warning = this.init_aM.init_Str.isEmpty() ? "Name Required" : "Name Taken";
                  int warningW = autismclient.gui.f.b.k.init(
                     this.init_Font, warning, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), -43691
                  );
                  autismclient.gui.f.b.k.init(
                     context,
                     this.init_Font,
                     warning,
                     this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     -43691,
                     this.tick_I + this.get_I - this.store_I() - warningW,
                     UiLayoutMath.init(this.add_I_10(), this.bind_I(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush()),
                     false
                  );
               }

               int builderLabelY = this.push_I_14();
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  "Step Builder",
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
                  UiColorPalette.parse(),
                  this.tick_I + this.store_I() + 2,
                  UiLayoutMath.init(
                     builderLabelY,
                     this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                     this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load),
                     this.init_CharEncoder.flush()
                  ),
                  false
               );
               int listLabelY = this.tick_I_12();
               int listY = this.run_I_12();
               int listHeight = this.clear_I_4();
               int listClipTop = this.cache_I();
               int listClipBottom = this.state_I();
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  "Steps (" + this.init_aM.init_List.size() + ")",
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
                  UiColorPalette.parse(),
                  this.tick_I + this.store_I() + 2,
                  UiLayoutMath.init(
                     listLabelY,
                     this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                     this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load),
                     this.init_CharEncoder.flush()
                  ),
                  false
               );
               if (stepListLocked) {
                  String lockText = "Running - list locked";
                  int lockColor = autismclient.gui.f.c.LayoutContext.init(-24416, 0.86F);
                  int lockW = autismclient.gui.f.b.k.init(
                     this.init_Font, lockText, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load), lockColor
                  );
                  autismclient.gui.f.b.k.init(
                     context,
                     this.init_Font,
                     lockText,
                     this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
                     lockColor,
                     Math.max(this.tick_I + this.store_I() + 2, this.chunk_I() - lockW),
                     UiLayoutMath.init(
                        listLabelY,
                        this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                        this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load),
                        this.init_CharEncoder.flush()
                     ),
                     false
                  );
               }

               this.clear_V_4();
               UiColorPalette.init(context, this.get_I_10(), listY, this.set_I_10(), listHeight, false);
               autismclient.gui.f.UiClipStack.init_HashUtil()
                  .init(
                     context,
                     autismclient.gui.f.NetHelper.init(
                        this.put_I_4(), listClipTop, Math.max(0, this.chunk_I() - this.put_I_4()), Math.max(0, listClipBottom - listClipTop)
                     )
                  );

               try {
                  this.init_F();
                  int y = listY - this.clear_I;
                  int activeDragIndex = stepListLocked ? -1 : this.stop;
                  int activeDragTarget = stepListLocked ? -1 : this.prev;
                  List<MacroAction> dragBlock = null;
                  List<Integer> dragBlockIndices = null;
                  Set<MacroAction> dragSet = null;
                  int dragInsertCompact = -1;
                  int dragPrimaryPos = 0;
                  if (activeDragIndex >= 0 && activeDragIndex < this.init_aM.init_List.size() && this.read_Z(activeDragIndex)) {
                     List<MacroAction> refs = this.load_List_3();
                     if (refs.size() > 1 && !this.init_Z_List(refs)) {
                        dragBlock = new ArrayList<>();
                        dragBlockIndices = new ArrayList<>();
                        dragSet = Collections.newSetFromMap(new IdentityHashMap<>());
                        dragSet.addAll(refs);

                        for (int i = 0; i < this.init_aM.init_List.size(); i++) {
                           MacroAction a = this.init_aM.init_List.get(i);
                           if (dragSet.contains(a)) {
                              dragBlock.add(a);
                              dragBlockIndices.add(i);
                           }
                        }

                        dragInsertCompact = this.init_I_Set(dragSet, activeDragIndex, activeDragTarget);

                        for (int p = 0; p < dragBlockIndices.size(); p++) {
                           if (dragBlockIndices.get(p) == activeDragIndex) {
                              dragPrimaryPos = p;
                              break;
                           }
                        }
                     }
                  }

                  boolean multiDrag = dragBlock != null;

                  for (int i = 0; i < this.init_aM.init_List.size(); i++) {
                     MacroAction action = this.init_aM.init_List.get(i);
                     if (multiDrag ? !dragSet.contains(action) : i != activeDragIndex) {
                        int visualIndex = multiDrag
                           ? this.init_I_I_2(i, dragSet, dragInsertCompact, dragBlock.size())
                           : this.load_I_I_2(i, activeDragIndex, activeDragTarget);
                        int actionY = y + visualIndex * this.mark_I();
                        if (this.init_Z_I_2(actionY)) {
                           AutismMacroEditorOverlay.Inner_UiHelper motion = this.init_AutismMacroEditorO_I_3(this.write_I(action));
                           int animatedY = motion != null && motion.init_AutismMacroEditorO == AutismMacroEditorOverlay.Inner_RenderState.MOVE
                              ? this.init_I_AutismMacroEditorO_I(motion, y)
                              : actionY;
                           int enterOffset = this.init_I_AutismMacroEditorO(motion);
                           float enterAlpha = this.init_F_AutismMacroEditorO(motion);
                           this.init_V_GuiGraphicsExtract_5(
                              context,
                              action,
                              action.getDisplayName(),
                              this.write_Z(action),
                              i,
                              animatedY,
                              panelMouseX,
                              panelMouseY,
                              enterOffset,
                              stepListLocked ? enterAlpha * 0.54F : enterAlpha,
                              !stepListLocked
                           );
                        }
                     }
                  }

                  for (AutismMacroEditorOverlay.Inner_UiHelper motion : this.push_List) {
                     if (motion.init_AutismMacroEditorO == AutismMacroEditorOverlay.Inner_RenderState.EXIT) {
                        int actionY = this.init_I_AutismMacroEditorO_I(motion, y);
                        if (this.init_Z_I_2(actionY)) {
                           this.init_V_GuiGraphicsExtract_5(
                              context,
                              null,
                              motion.init_Str,
                              motion.init_Z,
                              motion.load,
                              actionY,
                              -10000,
                              -10000,
                              this.init_I_AutismMacroEditorO(motion),
                              this.init_F_AutismMacroEditorO(motion),
                              false
                           );
                        }
                     }
                  }

                  if (!stepListLocked && activeDragIndex >= 0 && activeDragIndex < this.init_aM.init_List.size()) {
                     int insertSlot = multiDrag ? dragInsertCompact : this.flush(activeDragIndex, activeDragTarget);
                     if (insertSlot >= 0) {
                        int insertY = y + insertSlot * this.mark_I();
                        if (insertY >= listClipTop - 2 && insertY <= listClipBottom + 2) {
                           autismclient.gui.f.b.PanelSurfaceHelper.init(
                              context, this.get_I_10() + 2, insertY - 1, this.chunk_I() - this.get_I_10() - 3, 2, this.init_CharEncoder.run()
                           );
                        }
                     }

                     int dragRowTop = this.w();
                     if (multiDrag) {
                        for (int j = 0; j < dragBlock.size(); j++) {
                           int top = dragRowTop + (j - dragPrimaryPos) * this.mark_I();
                           if (this.init_Z_I_2(top)) {
                              this.init_V_GuiGraphicsExtract_6(context, dragBlock.get(j), top, dragBlockIndices.get(j) + 1);
                           }
                        }
                     } else if (this.init_Z_I_2(dragRowTop)) {
                        this.init_V_GuiGraphicsExtract_6(context, this.init_aM.init_List.get(activeDragIndex), dragRowTop, activeDragIndex + 1);
                     }
                  }
               } finally {
                  autismclient.gui.f.UiClipStack.init_HashUtil().init(context);
               }

               autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil var531 = this.init_PoolEntry_DataUtil_4();
               autismclient.gui.f.b.ScrollbarMetricsHelper.init(context, var531, var531.init(panelMouseX, panelMouseY), this.pool == 1);
               this.load_Str = null;
               boolean macroCurrentlyRunning = this.state_Z();

               for (int ix = 0; ix < this.init_List.size(); ix++) {
                  int[] b = this.load_List.get(ix);
                  boolean enabled = this.update_List.get(ix);
                  String lbl = ix == this.put_I ? this.init_Str_Z_3(macroCurrentlyRunning) : this.init_List.get(ix);
                  autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant = autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY;
                  if (ix == this.put_I) {
                     variant = macroCurrentlyRunning
                        ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER
                        : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS;
                  } else if ("Save".equals(lbl)) {
                     variant = autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS;
                  }

                  if (ix == this.put_I) {
                     this.init(context, b[0], b[1], b[2], b[3], macroCurrentlyRunning, enabled, panelMouseX, panelMouseY);
                  } else if ("Multi".equals(lbl)) {
                     this.init_V_GuiGraphicsExtract_3(context, b[0], b[1], b[2], b[3], lbl, GuiIconAtlas.flush, variant, enabled, panelMouseX, panelMouseY);
                  } else if (!"Add Action".equals(lbl) && !"Add Conditional".equals(lbl)) {
                     this.init(context, b[0], b[1], b[2], b[3], lbl, variant, enabled, panelMouseX, panelMouseY);
                  } else {
                     this.init_V_GuiGraphicsExtract_3(context, b[0], b[1], b[2], b[3], lbl.substring(4), GuiIconAtlas.pop, variant, enabled, panelMouseX, panelMouseY);
                  }

                  if (panelMouseX >= b[0] && panelMouseX < b[0] + b[2] && panelMouseY >= b[1] && panelMouseY < b[1] + b[3]) {
                     String tip = this.read_Str_2(lbl);
                     if (tip != null) {
                        this.load_Str = tip;
                        this.chunk = panelMouseX + 8;
                        this.table = panelMouseY + 12;
                     }
                  }
               }

               if (!this.sync_List.isEmpty()) {
                  autismclient.gui.f.b.DropdownWidget.load(context, this.init_Font, this.sync_List, mouseX, mouseY);
               }
            } finally {
               viewport.read(context);
            }

            if (!this.pop_Z && !this.sync_List.isEmpty() && autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List)) {
               context.nextStratum();
               autismclient.gui.f.b.DropdownWidget.read(context, this.init_Font, this.sync_List, mouseX, mouseY);
            }

            if (!this.pop_Z && this.load_Str != null) {
               context.nextStratum();
               TooltipRenderer.init(autismclient.gui.f.UiHelper.init(context, this.init_Font, this.chunk, this.table), this.load_Str, this.chunk, this.table, 220);
            }

            if (!this.pop_Z && this.init_F_3.init_Z()) {
               context.nextStratum();
               this.init_F_3.init(context, mouseX, mouseY);
            }
         }
      }
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean hovered, boolean danger, String label) {
      autismclient.gui.f.b.IconButtonRenderer.init(
         context, this.init_Font, autismclient.gui.f.NetHelper.init(x, y, width, height), label == null ? "" : label, hovered, true, danger
      );
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean hovered, boolean danger, Identifier icon) {
      autismclient.gui.f.b.IconButtonRenderer.init(context, this.init_Font, autismclient.gui.f.NetHelper.init(x, y, width, height), icon, hovered, true, danger);
   }

   private boolean load_Z_Str_2(String label) {
      return "Add Action".equals(label) || "Add Conditional".equals(label) || "Presets".equals(label);
   }

   private void init(
      GuiGraphicsExtractor context,
      AutismMacroEditorOverlay.Inner_TaskQueue category,
      AutismMacroEditorOverlay.Inner_NetHelper entry,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      int mouseX,
      int mouseY
   ) {
      float hover = hovered ? 1.0F : 0.0F;
      float emphasis = hover * 0.85F;
      int categoryRgb = category.init_I & 16777215;
      int fillColor = UiLayoutMath.init_I_I(134217728, 570425344 | categoryRgb, emphasis);
      int borderColor = UiLayoutMath.init_I_I(category.init_I, -1, Math.min(0.22F, hover * 0.1F));
      int textColor = UiLayoutMath.init_I_I(this.init_CharEncoder.load(autismclient.gui.f.b.l.read), -1, Math.min(0.32F, hover * 0.12F));
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height), fillColor, borderColor);
      if (emphasis > 0.0F) {
         int hoverTint = (int)Math.min(30.0F, hover * 18.0F) << 24 | categoryRgb;
         autismclient.gui.f.b.PanelSurfaceHelper.init(context, x + 1, y + 1, width - 2, height - 2, hoverTint);
      }

      String label = entry.load == null ? "" : entry.load;
      int maxLabelW = Math.max(1, width - 6);
      int labelY = UiLayoutMath.init(y, height, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), 0);
      this.init(context, label, x + 3, labelY, maxLabelW, textColor);
   }

   private void init(GuiGraphicsExtractor context, String label, int x, int y, int maxWidth, int color) {
      String safe = label == null ? "" : label;
      if (!safe.isBlank() && maxWidth > 0) {
         Identifier font = this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
         int measured = autismclient.gui.f.b.k.init(this.init_Font, safe, font, color);
         if (measured <= maxWidth) {
            int labelX = x + Math.max(0, (maxWidth - measured) / 2);
            autismclient.gui.f.b.k.init(context, this.init_Font, safe, font, color, labelX, y, false);
         } else {
            float scale = Math.max(0.72F, Math.min(1.0F, (float)maxWidth / Math.max(1, measured)));
            if (scale > 0.72F) {
               int scaledWidth = Math.round(measured * scale);
               int labelX = x + Math.max(0, (maxWidth - scaledWidth) / 2);
               context.pose().pushMatrix();

               try {
                  context.pose().scale(scale, scale);
                  autismclient.gui.f.b.k.init(context, this.init_Font, safe, font, color, Math.round(labelX / scale), Math.round(y / scale), false);
               } finally {
                  context.pose().popMatrix();
               }
            } else {
               String displayLabel = autismclient.gui.f.b.k.load(this.init_Font, safe, Math.round(maxWidth / scale), font, color);
               int scaledWidth = Math.round(autismclient.gui.f.b.k.init(this.init_Font, displayLabel, font, color) * scale);
               int labelX = x + Math.max(0, (maxWidth - scaledWidth) / 2);
               context.pose().pushMatrix();

               try {
                  context.pose().scale(scale, scale);
                  autismclient.gui.f.b.k.init(context, this.init_Font, displayLabel, font, color, Math.round(labelX / scale), Math.round(y / scale), false);
               } finally {
                  context.pose().popMatrix();
               }
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_5(
      GuiGraphicsExtractor context,
      MacroAction action,
      String label,
      boolean conditional,
      int actionIndex,
      int actionY,
      int mouseX,
      int mouseY,
      int xOffset,
      float alpha,
      boolean interactive
   ) {
      int rowLeft = this.get_I_10() + 1 + xOffset;
      int rowRight = this.chunk_I() + xOffset;
      AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(actionIndex, action);
      boolean groupBody = group.init() && !group.init_Z;
      boolean multiBody = group.write && !group.init_Z;
      boolean groupHeader = group.init() && group.init_Z;
      boolean gateEnd = group.read_Z && action instanceof autismclient.util.macro.EndPacketGateAction;
      boolean allowMove = !groupBody || multiBody || gateEnd;
      boolean allowDuplicate = (!group.init() || multiBody)
         && !(action instanceof autismclient.util.macro.PacketGateAction)
         && !(action instanceof autismclient.util.macro.EndPacketGateAction);
      boolean allowDelete = (!groupBody || multiBody) && !(action instanceof autismclient.util.macro.EndPacketGateAction);
      int bodyIndent = groupBody ? 10 : 0;
      boolean hoverRow = interactive && mouseX >= rowLeft - 1 && mouseX <= rowRight + 1 && mouseY >= actionY && mouseY < actionY + this.mark_I();
      boolean selectedRow = interactive && this.read_Z(actionIndex);
      int bgColor = selectedRow ? ThemePaletteManager.init_I_I_ct_DataUtil(1715082778, ThemePaletteManager.Inner_DataUtil.ACCENT) : (hoverRow ? UiColorPalette.level() : UiColorPalette.flags());
      autismclient.gui.f.b.PanelSurfaceHelper.init(
         context, rowLeft, actionY + 1, rowRight - rowLeft, this.mark_I() - 2, autismclient.gui.f.c.LayoutContext.init(bgColor, alpha)
      );
      AutismMacroEditorOverlay.Inner_BufferHelper impact = this.init_AutismMacroEditorO_I(actionIndex);
      if (impact.init_Z) {
         int rh = this.mark_I();
         int rgb = (impact.read ? UiColorPalette.buffer() : UiColorPalette.queue()) & 16777215;
         int tint = autismclient.gui.f.c.LayoutContext.init((impact.load ? 77 : 51) << 24 | rgb, alpha);
         int edge = autismclient.gui.f.c.LayoutContext.init(-805306368 | rgb, alpha);
         autismclient.gui.f.b.PanelSurfaceHelper.init(context, rowLeft, actionY + 1, rowRight - rowLeft, rh - 2, tint);
         autismclient.gui.f.b.PanelSurfaceHelper.read(context, rowLeft, actionY + 1, 1, rh - 2, edge);
         if (impact.write) {
            autismclient.gui.f.b.PanelSurfaceHelper.read(context, rowLeft, actionY + 1, rowRight - rowLeft, 1, edge);
         }

         if (impact.update) {
            autismclient.gui.f.b.PanelSurfaceHelper.read(context, rowLeft, actionY + rh - 2, rowRight - rowLeft, 1, edge);
         }
      }

      if (selectedRow) {
         autismclient.gui.f.PacketUtil.load(
            context,
            autismclient.gui.f.NetHelper.init(rowLeft, actionY + 1, rowRight - rowLeft, this.mark_I() - 2),
            autismclient.gui.f.c.LayoutContext.init(ThemePaletteManager.init_I_I_ct_DataUtil(-42149, ThemePaletteManager.Inner_DataUtil.OUTLINE), alpha)
         );
      }

      if (group.init()) {
         int railX = this.get_I_10() + 2 + xOffset;
         int railTop = group.load() ? actionY + 3 : actionY;
         int railBottom = group.read() ? actionY + this.mark_I() - 3 : actionY + this.mark_I();
         int railColor = autismclient.gui.f.c.LayoutContext.init(group.read_Z ? UiColorPalette.node() : this.init_CharEncoder.run(), alpha);
         autismclient.gui.f.b.PanelSurfaceHelper.read(context, railX, railTop, 2, railBottom - railTop, railColor);
      }

      int badgeColor = group.read_Z ? UiColorPalette.node() : (groupHeader ? this.init_CharEncoder.run() : (conditional ? -30720 : -12268476));
      if (!groupHeader) {
         autismclient.gui.f.b.PanelSurfaceHelper.read(
            context, this.get_I_10() + 2 + xOffset + bodyIndent, actionY + 3, 3, this.mark_I() - 6, autismclient.gui.f.c.LayoutContext.init(badgeColor, alpha)
         );
      }

      autismclient.gui.f.b.k.init(
         context,
         this.init_Font,
         String.valueOf(actionIndex + 1),
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.f.c.LayoutContext.init(UiColorPalette.bind(), alpha),
         this.get_I_10() + 8 + xOffset + bodyIndent,
         this.init_I_I_6(actionY),
         false
      );
      int nameX = this.get_I_10() + 22 + xOffset + bodyIndent;
      int controlsX = this.load(action, allowMove, allowDuplicate, allowDelete) + xOffset;
      int contentRight = this.read(action, allowMove, allowDuplicate, allowDelete) + xOffset;
      int maxNameW = Math.max(1, contentRight - nameX - 8);
      if (group.init() && !group.init_Str.isEmpty()) {
         int roleColor = autismclient.gui.f.c.LayoutContext.init(group.read_Z ? UiColorPalette.node() : (groupHeader ? -10281 : UiColorPalette.parse()), alpha);
         String role = group.init_Str;
         int roleW = autismclient.gui.f.b.k.init(this.init_Font, role, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load), roleColor);
         autismclient.gui.f.b.k.init(
            context,
            this.init_Font,
            role,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
            roleColor,
            nameX,
            this.init_I_I_6(actionY),
            false
         );
         nameX += roleW + 6;
         maxNameW = Math.max(1, contentRight - nameX - 8);
      }

      int nameColor = action instanceof autismclient.util.macro.MissingAddonAction ? -6645094 : -1;
      String displayName = autismclient.gui.f.b.k.init(
         this.init_Font,
         label,
         maxNameW,
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.f.c.LayoutContext.init(nameColor, alpha)
      );
      autismclient.gui.f.b.k.init(
         context,
         this.init_Font,
         displayName,
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.f.c.LayoutContext.init(nameColor, alpha),
         nameX,
         this.init_I_I_6(actionY),
         false
      );
      if (interactive && action != null) {
         int controlY = this.load_I_I_3(actionY);
         int cx = controlsX;
         if (allowMove) {
            this.init(
               context,
               cx,
               controlY,
               this.level_I(),
               this.level_I(),
               mouseX >= cx && mouseX < cx + this.level_I() && mouseY >= controlY && mouseY < controlY + this.level_I(),
               false,
               GuiIconAtlas.update
            );
            cx += this.u();
            this.init(
               context,
               cx,
               controlY,
               this.level_I(),
               this.level_I(),
               mouseX >= cx && mouseX < cx + this.level_I() && mouseY >= controlY && mouseY < controlY + this.level_I(),
               false,
               GuiIconAtlas.write
            );
            cx += this.u();
         }

         if (allowDuplicate) {
            this.init(
               context,
               cx,
               controlY,
               this.level_I(),
               this.level_I(),
               mouseX >= cx && mouseX < cx + this.level_I() && mouseY >= controlY && mouseY < controlY + this.level_I(),
               false,
               GuiIconAtlas.push
            );
            cx += this.u();
         }

         boolean hasEditor = this.read_Z(action);
         if (hasEditor) {
            this.init(
               context,
               cx,
               controlY,
               this.level_I(),
               this.level_I(),
               mouseX >= cx && mouseX < cx + this.level_I() && mouseY >= controlY && mouseY < controlY + this.level_I(),
               false,
               GuiIconAtlas.poll
            );
            cx += this.u();
         }

         if (allowDelete) {
            this.init(
               context,
               cx,
               controlY,
               this.level_I(),
               this.level_I(),
               mouseX >= cx && mouseX < cx + this.level_I() && mouseY >= controlY && mouseY < controlY + this.level_I(),
               true,
               GuiIconAtlas.open
            );
         }
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor context, String text, int x, int y, int color) {
      autismclient.gui.f.b.k.init(context, this.init_Font, text, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), color, x, y, false);
   }

   private void load(GuiGraphicsExtractor context, String text, int x, int y, int color) {
      autismclient.gui.f.b.k.init(context, this.init_Font, text, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load), color, x, y, false);
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor context, int x, int y, boolean hovered) {
      this.init(context, x, y, 14, 12, hovered, true, GuiIconAtlas.open);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, String label, boolean hovered, boolean selected) {
      int bg = selected ? UiColorPalette.frame() : (hovered ? UiColorPalette.level() : UiColorPalette.flags());
      autismclient.gui.f.b.PanelSurfaceHelper.init(context, x, y, width, 12, bg);
      int textX = x + 3;
      this.init_V_GuiGraphicsExtract_4(context, label, textX, y + 2, selected ? UiColorPalette.table() : UiColorPalette.b());
   }

   private boolean write_Z_D_D_2(double mouseX, double mouseY) {
      return mouseX >= this.tick_I && mouseX <= this.tick_I + this.get_I && mouseY >= this.run_I && mouseY <= this.run_I + this.init_CharEncoder.load();
   }

   private boolean update_Z_D_D_2(double mouseX, double mouseY) {
      return autismclient.gui.f.b.WindowHeaderHelper.init(
         autismclient.gui.f.NetHelper.init(this.tick_I, this.run_I, this.get_I, this.F()), this.init_CharEncoder.load(), mouseX, mouseY
      );
   }

   @Override
   protected boolean d_() {
      return true;
   }

   @Override
   protected autismclient.gui.f.NetHelper init_NetHelper_cH(OverlayBounds bounds) {
      autismclient.gui.f.NetHelper window = autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, this.init_CharEncoder.load());
      autismclient.gui.f.NetHelper bar = autismclient.gui.f.b.WindowHeaderHelper.init(window, this.init_CharEncoder.load());
      autismclient.gui.f.NetHelper close = autismclient.gui.f.b.WindowHeaderHelper.load(window, this.init_CharEncoder.load());
      int size = Math.min(12, Math.max(9, this.init_CharEncoder.load() - 4));
      int x = close.read() - 3 - size;
      int y = bar.write() + Math.max(1, (bar.sync() - size) / 2);
      return autismclient.gui.f.NetHelper.init(x, y, size, size);
   }

   private OverlayBounds load_cH() {
      return new OverlayBounds(this.tick_I, this.run_I, this.get_I, this.F(), this.write_Z, this.pop_Z);
   }

   private int F() {
      this.init_BitSetUtil.init(Math.max(0, this.set_I - this.init_CharEncoder.load()));
      this.init_CharEncoder_2.load_CharEncoder(!this.pop_Z);
      autismclient.gui.f.c.LayoutContext metrics = this.init_TextFormat.init_SyncManager();
      if (metrics == null) {
         return this.pop_Z ? this.init_CharEncoder.load() : this.set_I;
      } else {
         return Math.max(this.init_CharEncoder.load(), Math.round(this.init_CharEncoder_2.init_F_SyncManager_F(metrics, this.get_I)));
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      int pickerX = this.B();
      int pickerY = this.C();
      int pickerW = this.x();
      int pickerH = this.A();
      String title = this.init_AutismMacroEditorO_3 == AutismMacroEditorOverlay.Inner_JsonUtil.ACTION
         ? "Add Action"
         : (this.init_AutismMacroEditorO_3 == AutismMacroEditorOverlay.Inner_JsonUtil.PRESET ? "Presets" : "Add Conditional");
      autismclient.gui.f.b.WindowFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(context, this.init_Font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(pickerX, pickerY, pickerW, pickerH),
         title,
         false,
         false,
         true,
         mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + 16,
         true,
         4,
         4,
         16
      );
      this.pool_V();
      if (this.read_k != null) {
         this.read_k.init_V(context, mouseX, mouseY, 0.0F);
      }

      int contentX = pickerX + 6;
      int contentY = pickerY + 42;
      int scrollbarGutter = 7;
      int contentW = Math.max(1, pickerW - 12 - scrollbarGutter);
      int contentH = Math.max(1, pickerH - 48);
      int gridY = contentY;
      int gridH = contentH;
      List<AutismMacroEditorOverlay.Inner_SyncManager> layouts = this.init_List_I(contentX, contentY - this.buffer, contentW);
      int contentHeight = this.z();
      int maxScroll = Math.max(0, contentHeight - contentH);
      this.buffer = Math.max(0, Math.min(this.buffer, maxScroll));
      autismclient.gui.f.UiClipStack.init_HashUtil()
         .init(context, autismclient.gui.f.NetHelper.init(contentX, contentY, Math.max(0, contentW), Math.max(0, contentH)));

      try {
         for (AutismMacroEditorOverlay.Inner_SyncManager section : layouts) {
            int sectionTop = section.load;
            int sectionBottom = section.write;
            if (sectionBottom >= gridY && sectionTop <= gridY + gridH) {
               String headerLabel = autismclient.gui.f.b.k.load(
                  this.init_Font,
                  section.init_AutismMacroEditorO.load,
                  Math.max(1, section.read - 4),
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
                  section.init_AutismMacroEditorO.init_I
               );
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  headerLabel,
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
                  section.init_AutismMacroEditorO.init_I,
                  section.init_I,
                  UiLayoutMath.init(sectionTop, this.G(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load), 0),
                  false
               );
            }

            for (AutismMacroEditorOverlay.Inner_ConfigStore button : section.init_List) {
               int cardX = button.init_I;
               int cardY = button.load;
               int cardW = button.read;
               int cardH = button.write;
               if (cardY + cardH >= gridY && cardY <= gridY + gridH) {
                  boolean hovered = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH;
                  this.init(context, section.init_AutismMacroEditorO, button.init_AutismMacroEditorO, cardX, cardY, cardW, cardH, hovered, mouseX, mouseY);
                  if (hovered) {
                     this.load_Str = button.init_AutismMacroEditorO.read;
                     this.chunk = mouseX + 8;
                     this.table = mouseY + 12;
                  }
               }
            }
         }
      } finally {
         autismclient.gui.f.UiClipStack.init_HashUtil().init(context);
      }

      autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil var33 = this.load_PoolEntry_DataUtil_2();
      autismclient.gui.f.b.ScrollbarMetricsHelper.init(context, var33, var33.init(mouseX, mouseY), this.pool == 2);
   }

   private int G() {
      return Math.max(11, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load));
   }

   private boolean read_Z_D(double mouseX, double mouseY, int button) {
      int pickerX = this.B();
      int pickerY = this.C();
      int pickerW = this.x();
      int pickerH = this.A();
      int closeX = this.D();
      int closeY = this.E();
      boolean inside = mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + pickerH;
      if (!inside) {
         this.next_V_3();
         return true;
      }

      if (button != 0) {
         return true;
      }

      if (mouseX >= closeX && mouseX < closeX + 12 && mouseY >= closeY && mouseY < closeY + 12) {
         this.next_V_3();
         return true;
      }

      int contentX = pickerX + 6;
      int contentY = pickerY + 42;
      int contentW = pickerW - 19;
      int contentH = pickerH - 48;
      this.pool_V();
      if (this.read_k != null && this.read_k.init_Z_D_2(mouseX, mouseY, button)) {
         return true;
      }

      if (this.read_k != null) {
         this.read_k.push(false);
      }

      autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil stepPickerScrollbar = this.load_PoolEntry_DataUtil_2();
      if (stepPickerScrollbar.init_Z() && stepPickerScrollbar.init((int)mouseX, (int)mouseY)) {
         this.pool = 2;
         boolean onThumb = mouseY >= stepPickerScrollbar.update() && mouseY < stepPickerScrollbar.update() + stepPickerScrollbar.sync();
         this.index = onThumb ? Math.max(0, (int)mouseY - stepPickerScrollbar.update()) : stepPickerScrollbar.sync() / 2;
         this.buffer = autismclient.gui.f.b.ScrollbarMetricsHelper.init(stepPickerScrollbar, (int)mouseY, this.index);
         return true;
      }

      if (!(mouseX < contentX) && !(mouseX >= contentX + contentW) && !(mouseY < contentY) && !(mouseY >= contentY + contentH)) {
         for (AutismMacroEditorOverlay.Inner_SyncManager section : this.init_List_I(contentX, contentY - this.buffer, contentW)) {
            for (AutismMacroEditorOverlay.Inner_ConfigStore buttonLayout : section.init_List) {
               int cardX = buttonLayout.init_I;
               int cardY = buttonLayout.load;
               int cardW = buttonLayout.read;
               int cardH = buttonLayout.write;
               if (mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH) {
                  this.next_V_3();
                  buttonLayout.init_AutismMacroEditorO.init_Runnable.run();
                  return true;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      if (this.init_F_3.init_Z()) {
         if (button == 0) {
            if (this.init_F_3.init(mouseX, mouseY, button, (idx, label, i) -> this.read_V_I_I_2(idx, i))) {
               return true;
            }
         } else if (button == 1 && !this.init_F_3.init(mouseX, mouseY)) {
            this.init_F_3.init_V();
         }
      }

      if (!this.sync_List.isEmpty()) {
         if (autismclient.gui.f.b.DropdownWidget.init_Z_List_2(this.sync_List, mouseX, mouseY, button)) {
            return true;
         }

         if (autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List)) {
            return true;
         }
      }

      if (this.flush_Z && ValueSink.load_Z(button)) {
         this.init_aM.load_I = ValueSink.init_I(button);
         this.flush_Z = false;
         this.bind_V();
         this.set_V_5();
         return true;
      }

      if (button == 0 && this.write_Z_D_D_2(mouseX, mouseY)) {
         if (this.load_Z_D_2(mouseX, mouseY, this.load_cH())) {
            this.pop_V();
            return true;
         } else if (this.update_Z_D_D_2(mouseX, mouseY)) {
            this.update_V_10();
            return true;
         } else {
            this.set_V_5();
            this.poll_Z = true;
            this.push_Z = false;
            this.read_D = mouseX - this.tick_I;
            this.write_D = mouseY - this.run_I;
            this.update_D = mouseX;
            this.sync_D = mouseY;
            this.value = this.tick_I;
            this.cache = this.run_I;
            return true;
         }
      } else {
         if (this.pop_Z) {
            return false;
         }

         if (this.valid_Z()) {
            return this.read_Z_D(mouseX, mouseY, button);
         }

         MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, Compat263.newGlfwMouseButtonInfo(button, 0));
         boolean fieldClicked = false;
         boolean stepListLocked = this.queue_Z();
         if (!stepListLocked && this.init_k.init(click, false)) {
            this.init_k.push(true);
            if (this.load_k != null) {
               this.load_k.push(false);
            }

            fieldClicked = true;
         } else if (!stepListLocked && this.add_I == 1 && this.load_k != null && this.load_k.init(click, false)) {
            this.load_k.push(true);
            this.init_k.push(false);
            fieldClicked = true;
         }

         if (fieldClicked) {
            this.set_V_5();
            return true;
         }

         this.init_k.push(false);
         if (this.load_k != null) {
            this.load_k.push(false);
         }

         this.a_V();

         for (int i = 0; i < this.init_List.size(); i++) {
            int[] b = this.load_List.get(i);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
               this.set_V_5();
               boolean enabled = this.update_List.get(i);
               boolean macroCurrentlyRunning = this.state_Z();
               String label = i == this.put_I ? this.init_Str_Z_3(macroCurrentlyRunning) : this.init_List.get(i);
               autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil variant = autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY;
               if (i == this.put_I) {
                  variant = macroCurrentlyRunning
                     ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER
                     : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS;
               }

               Runnable secondary = i < this.write_List.size() ? this.write_List.get(i) : null;
               autismclient.gui.f.b.OverlayActionButton buttonView = this.init(b[0], b[1], b[2], b[3], label, variant, enabled, this.read_List.get(i), secondary);
               autismclient.gui.f.b.OverlayActionButton.init(buttonView, mouseX, mouseY, button);
               return true;
            }
         }

         int listY = this.run_I_12();
         int listHeight = this.clear_I_4();
         if (mouseY >= listY && mouseY <= listY + listHeight && mouseX >= this.get_I_10() && mouseX <= this.get_I_10() + this.set_I_10()) {
            autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil stepListScrollbar = this.init_PoolEntry_DataUtil_4();
            if (button == 0 && stepListScrollbar.init_Z() && stepListScrollbar.init((int)mouseX, (int)mouseY)) {
               this.set_V_5();
               this.pool = 1;
               boolean onThumb = mouseY >= stepListScrollbar.update() && mouseY < stepListScrollbar.update() + stepListScrollbar.sync();
               this.index = onThumb ? Math.max(0, (int)mouseY - stepListScrollbar.update()) : stepListScrollbar.sync() / 2;
               this.clear_I = this.init_I_I_3(
                  autismclient.gui.f.b.ScrollbarMetricsHelper.init(stepListScrollbar, (int)mouseY, this.index), this.mark_I(), stepListScrollbar.flush()
               );
               return true;
            }

            int y = listY - this.clear_I;
            int controlSize = this.level_I();
            int controlStride = this.u();
            int index = (int)((mouseY - y) / this.mark_I());
            if (this.queue_Z()) {
               this.set_V_5();
               this.reset_V_3();
               if (button == 1 && index >= 0 && index < this.init_aM.init_List.size()) {
                  int lockedRowY = y + index * this.mark_I();
                  if (this.init_Z_I_2(lockedRowY) && mouseX < this.chunk_I()) {
                     this.init_F_3.init((int)mouseX, (int)mouseY, index);
                  }
               }

               return true;
            }

            if (index >= 0 && index < this.init_aM.init_List.size()) {
               MacroAction action = this.init_aM.init_List.get(index);
               AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(index, action);
               boolean groupBody = group.init() && !group.init_Z;
               boolean multiBody = group.write && !group.init_Z;
               boolean gateEnd = group.read_Z && action instanceof autismclient.util.macro.EndPacketGateAction;
               boolean allowMove = !groupBody || multiBody || gateEnd;
               boolean allowDuplicate = (!group.init() || multiBody)
                  && !(action instanceof autismclient.util.macro.PacketGateAction)
                  && !(action instanceof autismclient.util.macro.EndPacketGateAction);
               boolean allowDelete = (!groupBody || multiBody) && !(action instanceof autismclient.util.macro.EndPacketGateAction);
               int actionY = y + index * this.mark_I();
               if (!this.init_Z_I_2(actionY)) {
                  return true;
               }

               int controlY = this.load_I_I_3(actionY);
               int cx = this.load(action, allowMove, allowDuplicate, allowDelete);
               if (allowMove) {
                  if (mouseX >= cx && mouseX < cx + controlSize) {
                     if (this.read_Z(index) && this.sync_Z(-1)) {
                        return true;
                     }

                     if (index > 0) {
                        this.count_V();
                        this.write_V_I_I_2(index, index - 1);
                     }

                     return true;
                  }

                  cx += controlStride;
                  if (mouseX >= cx && mouseX < cx + controlSize) {
                     if (this.read_Z(index) && this.sync_Z(1)) {
                        return true;
                     }

                     if (index < this.init_aM.init_List.size() - 1) {
                        this.count_V();
                        int target = index + 1;
                        if (action instanceof autismclient.util.macro.RaceAction race) {
                           target = Math.min(this.init_aM.init_List.size() - 1, index + race.init_I_List_I(this.init_aM.init_List, index) + 1);
                        } else if (action instanceof autismclient.util.macro.ReportAction report) {
                           target = Math.min(this.init_aM.init_List.size() - 1, index + report.init(this.init_aM.init_List, index) + 1);
                        } else if (action instanceof autismclient.util.macro.MultiAction multi) {
                           target = Math.min(this.init_aM.init_List.size() - 1, index + multi.init(this.init_aM.init_List, index) + 1);
                        } else if (action instanceof autismclient.util.macro.PacketGateAction) {
                           target = Math.min(this.init_aM.init_List.size() - 1, index + this.sync_I(index) + 1);
                        }

                        this.write_V_I_I_2(index, target);
                     }

                     return true;
                  }

                  cx += controlStride;
               }

               if (allowDuplicate && mouseX >= cx && mouseX < cx + controlSize) {
                  this.sync_V(index);
                  this.set_V_5();
                  return true;
               }

               if (allowDuplicate) {
                  cx += controlStride;
               }

               if (mouseX >= cx && mouseX < cx + controlSize && this.read_Z(action)) {
                  this.init_V_I_MacroAction(index, action);
                  return true;
               }

               if (this.read_Z(action)) {
                  cx += controlStride;
               }

               if (allowDelete && mouseX >= cx && mouseX < cx + controlSize) {
                  if (this.read_Z(index)) {
                     this.prev_Z_2();
                  } else {
                     this.count_V();
                     this.flush_V(index);
                  }

                  return true;
               }

               if (button == 0 && mouseX < this.chunk_I()) {
                  boolean shiftDown = this.put_Z_2();
                  boolean controlDown = this.run_Z_3();
                  if (shiftDown && this.load_Z_I(index)) {
                     return true;
                  }

                  this.init_Integer = this.write_I(this.init_aM.init_List.get(index));
                  if (!controlDown && !shiftDown) {
                     this.init_V_I(index, mouseX, mouseY, allowMove);
                  } else {
                     this.load_V_I_2(index);
                  }

                  return true;
               }

               if (button == 1 && mouseX < this.chunk_I()) {
                  this.init_V_I(index, (int)mouseX, (int)mouseY);
                  return true;
               }
            }

            if (button == 0) {
               this.set_V_5();
               return true;
            }
         }

         if (button == 0) {
            this.set_V_5();
         }

         return false;
      }
   }

   private void sync_V(int index) {
      if (!this.queue_Z() && index >= 0 && index < this.init_aM.init_List.size()) {
         AutismMacroEditorOverlay.Inner_StreamUtil group = this.init_AutismMacroEditorO_I_MacroAction(index, this.init_aM.init_List.get(index));
         if (group.init() && !group.write) {
            return;
         }

         if (group.write && group.init_Z) {
            return;
         }

         MacroAction original = this.init_aM.init_List.get(index);
         this.count_V();
         CompoundTag tag = original.toTag();
         MacroAction copy = AutismMacro.init_MacroAction_CompoundTag(tag);
         if (copy != null) {
            this.init_aM.init_List.add(index + 1, copy);
            int header = this.read_I_2(index);
            if (header >= 0 && this.init_aM.init_List.get(header) instanceof autismclient.util.macro.RaceAction race) {
               race.bodyCount = Math.max(0, race.bodyCount + 1);
            } else {
               int reportHeader = this.write_I_2(index);
               if (reportHeader >= 0 && this.init_aM.init_List.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
                  report.conditionCount = Math.max(0, report.conditionCount + 1);
               } else {
                  int multiHeader = this.update_I_2(index);
                  if (multiHeader >= 0 && this.init_aM.init_List.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
                     multi.stepCount = Math.max(0, multi.stepCount + 1);
                  }
               }
            }

            this.fill_V();
         }
      }
   }

   private void flush_V(int index) {
      if (!this.queue_Z() && this.init_aM != null && index >= 0 && index < this.init_aM.init_List.size() && !this.flush_Z(index)) {
         MacroAction action = this.init_aM.init_List.get(index);
         ActionEditorOverlay editor = ActionEditorOverlay.load_ActionEditorOverla();
         if (action instanceof autismclient.util.macro.RaceAction race) {
            int count = race.init_I_List_I(this.init_aM.init_List, index);
            int end = Math.min(this.init_aM.init_List.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.init_V_MacroAction(this.init_aM.init_List.get(i));
               }
            }

            this.init_aM.init_List.subList(index, end).clear();
            this.fill_V();
         } else if (action instanceof autismclient.util.macro.ReportAction report) {
            int count = report.init(this.init_aM.init_List, index);
            int end = Math.min(this.init_aM.init_List.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.init_V_MacroAction(this.init_aM.init_List.get(i));
               }
            }

            this.init_aM.init_List.subList(index, end).clear();
            this.fill_V();
         } else if (action instanceof autismclient.util.macro.PacketGateAction) {
            int count = this.sync_I(index);
            int end = Math.min(this.init_aM.init_List.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.init_V_MacroAction(this.init_aM.init_List.get(i));
               }
            }

            this.init_aM.init_List.subList(index, end).clear();
            this.fill_V();
         } else if (action instanceof autismclient.util.macro.MultiAction) {
            if (editor != null) {
               editor.init_V_MacroAction(action);
            }

            this.init_aM.init_List.remove(index);
            this.fill_V();
         } else {
            if (editor != null) {
               editor.init_V_MacroAction(action);
            }

            int header = this.read_I_2(index);
            int reportHeader = header < 0 ? this.write_I_2(index) : -1;
            int multiHeader = header < 0 && reportHeader < 0 ? this.update_I_2(index) : -1;
            this.init_aM.init_List.remove(index);
            if (header >= 0 && header < this.init_aM.init_List.size() && this.init_aM.init_List.get(header) instanceof autismclient.util.macro.RaceAction race) {
               race.bodyCount = Math.max(0, race.bodyCount - 1);
            } else if (reportHeader >= 0
               && reportHeader < this.init_aM.init_List.size()
               && this.init_aM.init_List.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
               report.conditionCount = Math.max(0, report.conditionCount - 1);
            } else if (multiHeader >= 0
               && multiHeader < this.init_aM.init_List.size()
               && this.init_aM.init_List.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
               multi.stepCount = Math.max(0, multi.stepCount - 1);
            }

            this.fill_V();
         }
      }
   }

   private String read_Str_2(String label) {
      String clean = label.replaceAll("§.", "");
      String var10000;
      switch (clean) {
         case "Save":
            if (this.init_aM == null) {
               var10000 = "Save this macro";
            } else {
               String name = this.init_aM.init_Str == null ? "" : this.init_aM.init_Str.trim();
               var10000 = name.isEmpty()
                  ? "Give the macro a name first"
                  : (!this.init_Z_Str_3(this.init_aM.init_Str) ? "Pick a unique macro name before saving" : "Save this macro");
            }
            break;
         case "Run":
            String blockedReason = this.write_Str_3();
            var10000 = blockedReason != null ? blockedReason : "Run this macro";
            break;
         case "Stop":
            var10000 = "Stop the running macro";
            break;
         case "Add Action":
            var10000 = "Open categorized action picker";
            break;
         case "Add Conditional":
            var10000 = "Open categorized wait/condition picker";
            break;
         case "Presets":
            var10000 = "Append editable preset steps";
            break;
         case "Undo":
            var10000 = "Undo the last macro edit";
            break;
         case "Redo":
            var10000 = "Redo the last undone macro edit";
            break;
         case "Chat":
            var10000 = "Send a chat message or /command";
            break;
         case "Delay":
            var10000 = "Wait N milliseconds or ticks";
            break;
         case "Packet":
            var10000 = "Send a captured network packet";
            break;
         case "Item Click":
            var10000 = "Click inventory items or slots";
            break;
         case "Pick Up All":
            var10000 = "Pick up matching stacks into the carried item";
            break;
         case "Mouse Clk":
            var10000 = "Simulate L/R/M click in world";
            break;
         case "Rotate":
            var10000 = "Set player yaw & pitch";
            break;
         case "Use Item":
            var10000 = "Use held item (right-click action)";
            break;
         case "Open Inv":
            var10000 = "Open player inventory screen";
            break;
         case "Slot":
            var10000 = "Select a hotbar slot (1-9)";
            break;
         case "Drop":
            var10000 = "Drop items from slot";
            break;
         case "Close GUI":
            var10000 = "Close the current screen/GUI";
            break;
         case "Swap":
            var10000 = "Swap two inventory slots";
            break;
         case "Wait HP":
            var10000 = "Pause until health crosses the selected threshold";
            break;
         case "Wait Block":
            var10000 = "Pause until block changes";
            break;
         case "Wait Pkt":
            var10000 = "Pause until packet received";
            break;
         case "Wait Item":
            var10000 = "Pause until item in inventory";
            break;
         case "WaitGUI":
         case "Wait GUI":
            var10000 = "Pause until screen opens";
            break;
         case "Tick Sync":
            var10000 = "Wait for next client tick";
            break;
         case "Rev Sync":
            var10000 = "Sync handler revision";
            break;
         case "Srv Sync":
            var10000 = "Wait for server tick timing";
            break;
         case "Go To":
            var10000 = "Pathfind to a target position";
            break;
         case "Disconn":
            var10000 = "Disconnect / Kick (lag+kick) / Kick Dupe (lag+bundle/action+kick)";
            break;
         case "NBT Book":
            var10000 = "Sign large-data books (random/custom text, multi-book, delay)";
            break;
         case "Wait LAN":
            var10000 = "Wait for LAN sync peer to reach a macro step";
            break;
         case "Module":
            var10000 = "Toggle AUTISM modules";
            break;
         case "Sneak":
            var10000 = "Hold or release sneak key";
            break;
         case "Jump":
            var10000 = "Press jump key for N ticks";
            break;
         case "Sprint":
            var10000 = "Toggle sprint on/off";
            break;
         case "Move":
            var10000 = "Walk in direction for N ticks";
            break;
         case "Look At":
            var10000 = "Look at a block position";
            break;
         case "Repeat":
            var10000 = "Repeat next N steps M times";
            break;
         case "Open Cont":
            var10000 = "Open a container at a block position";
            break;
         case "Desync":
            var10000 = "Send close packet without closing GUI (desync server state)";
            break;
         case "Cls w/o Pkt":
            var10000 = "Close GUI locally without sending close packet to server";
            break;
         case "Restore GUI":
            var10000 = "Restore a previously saved GUI screen and handler";
            break;
         case "Save GUI":
            var10000 = "Save the current GUI screen and handler for later restore";
            break;
         case "Send Toggle":
            var10000 = "Toggle whether packets are sent to server (on/off)";
            break;
         case "Delay Pkts":
            var10000 = "Toggle packet delay mode (queues outgoing packets)";
            break;
         case "Store":
            var10000 = "Loot/store items between container and player, or move slot->slot";
            break;
         case "Wait CD":
            var10000 = "Pause until cooldown expires";
            break;
         case "Wait Pos":
            var10000 = "Pause until position reached";
            break;
         case "Wait Chat":
            var10000 = "Pause until chat message received";
            break;
         case "Wait Ent":
            var10000 = "Pause until entity appears nearby";
            break;
         case "Wait Slot":
            var10000 = "Pause until slot content changes";
            break;
         case "Wait Snd":
            var10000 = "Pause until a matching sound plays";
            break;
         case "Mine":
            var10000 = "Mine target blocks autonomously until stop conditions are met";
            break;
         case "Pay":
            var10000 = "Pay a list of players with a configurable command, amount, and delay";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      }

      if (this.pop_Z) {
         return false;
      }

      if (this.flush_Z) {
         if (keyCode == 256) {
            this.init_aM.load_I = -1;
         } else {
            this.init_aM.load_I = keyCode;
         }

         this.flush_Z = false;
         this.bind_V();
         return true;
      } else {
         KeyEvent keyInput = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);
         if (autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List)) {
            if (keyCode == 256 || keyCode == 257) {
               autismclient.gui.f.b.DropdownWidget.read(this.sync_List);
            }

            return true;
         } else if (this.valid_Z()) {
            if (this.read_k != null && this.read_k.init_Z()) {
               if (keyCode == 256) {
                  this.read_k.push(false);
                  return true;
               }

               if (this.read_k.init_Z_KeyEvent(keyInput)) {
                  return true;
               }
            }

            if (keyCode == 256 || keyCode == 257) {
               this.next_V_3();
            }

            return true;
         } else if (this.tick_Z_4()
            || !this.queue_Z()
            || (!this.write_Z(modifiers) || keyCode != 67 && keyCode != 86 && keyCode != 88 && keyCode != 68 && keyCode != 90 && keyCode != 82)
               && keyCode != 261
               && keyCode != 259) {
            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 90) {
               this.flags_V();
               return true;
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 82) {
               this.level_V();
               return true;
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 88) {
               return this.start_Z_2();
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 65) {
               return this.stop_Z_2();
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 68) {
               this.next_Z_2();
               return true;
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 67) {
               return this.clear_Z_2();
            }

            if (!this.tick_Z_4() && this.write_Z(modifiers) && keyCode == 86) {
               return this.reset_Z_2();
            }

            if (!this.tick_Z_4() && (keyCode == 261 || keyCode == 259) && this.prev_Z_2()) {
               return true;
            }

            if (keyCode == 256) {
               boolean anyFocused = this.init_k.init_Z() || this.add_I == 1 && this.load_k != null && this.load_k.init_Z();
               if (anyFocused) {
                  this.init_k.push(false);
                  if (this.load_k != null) {
                     this.load_k.push(false);
                  }

                  return true;
               } else {
                  if (this.set_Z) {
                     this.update_V_10();
                     return true;
                  }

                  if (this.value_Z()) {
                     this.update_V_10();
                     return true;
                  }

                  if (init_Minecraft.gui.screen() != null) {
                     init_Minecraft.execute(() -> init_Minecraft.gui.setScreen(null));
                  } else {
                     this.update_V_10();
                  }

                  return true;
               }
            } else {
               if (keyCode == 257) {
                  if (this.init_k.init_Z()) {
                     this.init_k.push(false);
                     return true;
                  }

                  if (this.add_I == 1 && this.load_k != null && this.load_k.init_Z()) {
                     this.load_k.push(false);
                     return true;
                  }
               }

               if (this.init_k.init_Z_KeyEvent(keyInput)) {
                  return true;
               } else {
                  return this.add_I == 1 && this.load_k != null && this.load_k.init_Z_KeyEvent(keyInput)
                     ? true
                     : this.init_k.init_Z() || this.add_I == 1 && this.load_k != null && this.load_k.init_Z();
               }
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      if (!this.write_Z) {
         return false;
      }

      if (this.pop_Z) {
         return false;
      }

      if (this.valid_Z()) {
         if (this.read_k != null && this.read_k.init_Z()) {
            CharacterEvent charInput = new CharacterEvent(chr);
            if (this.read_k.init_Z_CharacterEvent(charInput)) {
               return true;
            }

            this.read_k.load(String.valueOf(chr));
         }

         return true;
      } else {
         CharacterEvent charInput = new CharacterEvent(chr);
         if (this.init_k.init_Z_CharacterEvent(charInput)) {
            return true;
         }

         if (this.init_k.init_Z()) {
            this.init_k.load(String.valueOf(chr));
            return true;
         }

         if (this.add_I == 1 && this.load_k != null) {
            if (this.load_k.init_Z_CharacterEvent(charInput)) {
               return true;
            }

            if (this.load_k.init_Z()) {
               this.load_k.load(String.valueOf(chr));
               return true;
            }
         }

         return true;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (!this.write_Z) {
         return false;
      }

      if (!this.sync_List.isEmpty()
         && (autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List) || autismclient.gui.f.b.DropdownWidget.init(this.sync_List, mouseX, mouseY))
         && autismclient.gui.f.b.DropdownWidget.init_Z_List(this.sync_List, mouseX, mouseY, amount)) {
         return true;
      }

      if (this.valid_Z()) {
         int pickerX = this.B();
         int pickerY = this.C();
         int pickerW = this.x();
         int pickerH = this.A();
         int contentX = pickerX + 6;
         int contentY = pickerY + 42;
         int contentW = pickerW - 19;
         int contentH = pickerH - 48;
         if (mouseX >= contentX && mouseX < contentX + contentW && mouseY >= contentY && mouseY < contentY + contentH) {
            int contentHeight = this.z();
            int maxScroll = Math.max(0, contentHeight - contentH);
            this.buffer = Math.max(0, Math.min(maxScroll, this.buffer - (int)(Math.signum(amount) * 36.0)));
            return true;
         } else {
            return true;
         }
      } else {
         int listY = this.run_I_12();
         int listHeight = this.clear_I_4();
         if (mouseY >= listY && mouseY <= listY + listHeight && mouseX >= this.get_I_10() + 2 && mouseX <= this.get_I_10() + this.set_I_10() - 2) {
            int maxScroll = this.queue_I();
            this.clear_I = this.init_I_I_3(this.clear_I - (int)(Math.signum(amount) * this.mark_I()), this.mark_I(), maxScroll);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.gui.f.b.DropdownWidget.load(this.sync_List, mouseX, mouseY, button)) {
         return true;
      }

      if (this.read_k != null && this.read_k.init(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.init_k != null && this.init_k.init(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.load_k != null && this.load_k.init(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.pool == 2) {
         this.buffer = autismclient.gui.f.b.ScrollbarMetricsHelper.init(this.load_PoolEntry_DataUtil_2(), (int)mouseY, this.index);
         return true;
      }

      if (this.pool == 1) {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_4();
         this.clear_I = this.init_I_I_3(autismclient.gui.f.b.ScrollbarMetricsHelper.init(metrics, (int)mouseY, this.index), this.mark_I(), metrics.flush());
         return true;
      }

      if (!this.queue_Z() || this.stop < 0 && this.next < 0) {
         if (button != 0 || this.stop < 0 && this.next < 0) {
            if (!this.poll_Z || button != 0) {
               return false;
            }

            if (!this.push_Z && Math.abs(mouseX - this.update_D) < 3.0 && Math.abs(mouseY - this.sync_D) < 3.0) {
               return true;
            }

            this.push_Z = true;
            this.tick_I = (int)(mouseX - this.read_D);
            this.run_I = (int)(mouseY - this.write_D);
            int screenW = cA.init_I();
            int screenH = cA.load_I();
            int minVisibleWidth = Math.min(this.get_I, 96);
            this.tick_I = Math.max(Math.min(0, screenW - this.get_I), Math.min(this.tick_I, Math.max(0, screenW - minVisibleWidth)));
            this.run_I = Math.max(0, Math.min(screenH - 14, this.run_I));
            if (this.tick_I != this.value || this.run_I != this.cache || Math.abs(mouseX - this.update_D) >= 3.0 || Math.abs(mouseY - this.sync_D) >= 3.0) {
               this.push_Z = true;
            }

            this.bind_V();
            return true;
         } else {
            this.init_V_D_D(mouseX, mouseY);
            if (this.stop >= 0) {
               this.init_V_D(mouseY);
            }

            return true;
         }
      } else {
         this.reset_V_3();
         return true;
      }
   }

   public boolean poll_Z_8() {
      return false;
   }

   public String load_Str_7() {
      return "";
   }

   public String read_Str_5() {
      return "";
   }

   public String init_Str_Slot_3(Slot slot, String itemName, String registryId) {
      return "";
   }

   public boolean push_Z_7() {
      return false;
   }

   public boolean init_Z_Slot_2(Slot slot, String itemName, String registryId) {
      return false;
   }

   private static final class Inner_BufferHelper {
      private static final AutismMacroEditorOverlay.Inner_BufferHelper init_AutismMacroEditorO = new AutismMacroEditorOverlay.Inner_BufferHelper(
         false, false, false, false, false
      );
      private final boolean init_Z;
      private final boolean load;
      private final boolean read;
      private final boolean write;
      private final boolean update;

      private Inner_BufferHelper(boolean impacted, boolean header, boolean ping, boolean first, boolean last) {
         this.init_Z = impacted;
         this.load = header;
         this.read = ping;
         this.write = first;
         this.update = last;
      }
   }

   private record Inner_CachePool(CompoundTag init_CompoundTag, List<Integer> init_List) {
   }

   private static final class Inner_ConfigStore {
      private final AutismMacroEditorOverlay.Inner_NetHelper init_AutismMacroEditorO;
      private final int init_I;
      private final int load;
      private final int read;
      private final int write;

      private Inner_ConfigStore(AutismMacroEditorOverlay.Inner_NetHelper entry, int x, int y, int width, int height) {
         this.init_AutismMacroEditorO = entry;
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
      }
   }

   public enum Inner_DataUtil {
      NORMAL,
      JOIN_MACRO_MENU;
   }

   private static final class Inner_EventBus {
      private final MacroAction init_MacroAction;
      private final int init_I;
      private final String init_Str;
      private final boolean init_Z;
      private final int load;

      private Inner_EventBus(MacroAction action, int stableId, String label, boolean conditional, int index) {
         this.init_MacroAction = action;
         this.init_I = stableId;
         this.init_Str = label;
         this.init_Z = conditional;
         this.load = index;
      }
   }

   private static final class Inner_FileCache {
      private final LinkedHashSet<Integer> init = new LinkedHashSet<>();

      private void init_V() {
         this.init.clear();
      }

      private boolean init_Z() {
         return this.init.isEmpty();
      }

      private boolean init_Z(int stableId) {
         return stableId >= 0 && this.init.contains(stableId);
      }

      private void init_V(int stableId) {
         if (stableId >= 0) {
            this.init.add(stableId);
         }
      }

      private void load(int stableId) {
         if (stableId >= 0) {
            this.init.remove(stableId);
         }
      }

      private void init(Set<Integer> liveStableIds) {
         if (liveStableIds != null && !liveStableIds.isEmpty()) {
            this.init.removeIf(stableId -> !liveStableIds.contains(stableId));
         } else {
            this.init.clear();
         }
      }
   }

   private enum Inner_JsonUtil {
      ACTION,
      CONDITIONAL,
      PRESET;
   }

   private static final class Inner_NetHelper {
      private final String init_Str;
      private final String load;
      private final String read;
      private final Runnable init_Runnable;

      private Inner_NetHelper(String categoryId, String label, String description, Runnable action) {
         this.init_Str = categoryId;
         this.load = label;
         this.read = description;
         this.init_Runnable = action;
      }
   }

   private enum Inner_RenderState {
      ENTER,
      MOVE,
      EXIT;
   }

   private final class Inner_StateCache implements AutismOverlay {
      @Override
      public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
         if (this.sync_Z()) {
            context.nextStratum();
            AutismMacroEditorOverlay.this.init_V_GuiGraphicsExtract_4(context, mouseX, mouseY);
         }
      }

      @Override
      public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
         return AutismMacroEditorOverlay.this.read_Z_D(mouseX, mouseY, button);
      }

      @Override
      public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
         return AutismMacroEditorOverlay.this.load_Z_D_3(mouseX, mouseY, button);
      }

      @Override
      public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
         return AutismMacroEditorOverlay.this.init_Z_D(mouseX, mouseY, button, deltaX, deltaY);
      }

      @Override
      public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
         return AutismMacroEditorOverlay.this.init_Z_D_2(mouseX, mouseY, amount);
      }

      @Override
      public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
         return AutismMacroEditorOverlay.this.init_Z_I(keyCode, scanCode, modifiers);
      }

      @Override
      public boolean init_Z_C_I(char chr, int modifiers) {
         return AutismMacroEditorOverlay.this.init_Z_C_I(chr, modifiers);
      }

      @Override
      public boolean sync_Z() {
         return AutismMacroEditorOverlay.this.write_Z && AutismMacroEditorOverlay.this.valid_Z();
      }

      @Override
      public void init_V_Z(boolean visible) {
         if (!visible) {
            AutismMacroEditorOverlay.this.next_V_3();
         }
      }

      @Override
      public boolean init_Z_D_D(double mouseX, double mouseY) {
         return this.sync_Z();
      }

      @Override
      public boolean load_Z_D_D(double mouseX, double mouseY) {
         return false;
      }

      @Override
      public String init_Str() {
         return "macro-step-picker";
      }

      @Override
      public OverlayBounds init_cH() {
         int width = AutismMacroEditorOverlay.init_Minecraft.getWindow() == null ? 640 : cA.init_I();
         int height = AutismMacroEditorOverlay.init_Minecraft.getWindow() == null ? 360 : cA.load_I();
         return new OverlayBounds(0, 0, width, height, this.sync_Z(), false);
      }

      @Override
      public int init_I() {
         return 1;
      }

      @Override
      public int load_I() {
         return 1;
      }
   }

   private static final class Inner_StreamUtil {
      private static final AutismMacroEditorOverlay.Inner_StreamUtil init_AutismMacroEditorO = new AutismMacroEditorOverlay.Inner_StreamUtil(
         false, false, false, -1, 0, 0, ""
      );
      private final boolean init_Z;
      private final boolean load_Z;
      private final boolean read_Z;
      private final boolean write;
      private final int init_I;
      private final int load_I;
      private final int read_I;
      private final String init_Str;

      private Inner_StreamUtil(boolean header, boolean report, boolean gate, int headerIndex, int childCount, int offsetFromHeader, String roleLabel) {
         this(header, report, gate, false, headerIndex, childCount, offsetFromHeader, roleLabel);
      }

      private Inner_StreamUtil(
         boolean header, boolean report, boolean gate, boolean multi, int headerIndex, int childCount, int offsetFromHeader, String roleLabel
      ) {
         this.init_Z = header;
         this.load_Z = report;
         this.read_Z = gate;
         this.write = multi;
         this.init_I = headerIndex;
         this.load_I = Math.max(0, childCount);
         this.read_I = Math.max(0, offsetFromHeader);
         this.init_Str = roleLabel == null ? "" : roleLabel;
      }

      private boolean init() {
         return this.init_I >= 0;
      }

      private boolean load() {
         return this.read_I == 0;
      }

      private boolean read() {
         return this.read_I >= this.load_I;
      }
   }

   private static final class Inner_SyncManager {
      private final AutismMacroEditorOverlay.Inner_TaskQueue init_AutismMacroEditorO;
      private final int init_I;
      private final int load;
      private final int read;
      private final List<AutismMacroEditorOverlay.Inner_ConfigStore> init_List;
      private final int write;

      private Inner_SyncManager(
         AutismMacroEditorOverlay.Inner_TaskQueue category,
         int x,
         int headerY,
         int width,
         List<AutismMacroEditorOverlay.Inner_ConfigStore> buttons,
         int bottomY
      ) {
         this.init_AutismMacroEditorO = category;
         this.init_I = x;
         this.load = headerY;
         this.read = width;
         this.init_List = buttons;
         this.write = bottomY;
      }
   }

   private static final class Inner_TaskQueue {
      private final String init_Str;
      private final String load;
      private final int init_I;

      private Inner_TaskQueue(String id, String label, int color) {
         this.init_Str = id;
         this.load = label;
         this.init_I = color;
      }
   }

   private static final class Inner_TextFormat {
      private static final AutismMacroEditorOverlay.Inner_TextFormat init_AutismMacroEditorO = new AutismMacroEditorOverlay.Inner_TextFormat(-1, -1);
      private final int init_I;
      private final int load;

      private Inner_TextFormat(int start, int endExclusive) {
         this.init_I = start;
         this.load = endExclusive;
      }

      private boolean init() {
         return this.init_I < 0 || this.load <= this.init_I;
      }
   }

   private static final class Inner_UiHelper {
      private final int init_I;
      private final String init_Str;
      private final boolean init_Z;
      private final int load;
      private final int read;
      private final AutismMacroEditorOverlay.Inner_RenderState init_AutismMacroEditorO;
      private float init_F;

      private Inner_UiHelper(int stableId, String label, boolean conditional, int fromRowIndex, int toRowIndex, AutismMacroEditorOverlay.Inner_RenderState kind) {
         this.init_I = stableId;
         this.init_Str = label;
         this.init_Z = conditional;
         this.load = fromRowIndex;
         this.read = toRowIndex;
         this.init_AutismMacroEditorO = kind;
      }
   }
}
