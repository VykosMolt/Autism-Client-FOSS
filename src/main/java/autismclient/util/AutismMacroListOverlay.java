package autismclient.util;

import autismclient.compat263.CompatGLFW;
import autismclient.gui.f.b.j;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class AutismMacroListOverlay extends bj {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final long init_J = 2000L;
   private static final int flush_I = 1;
   private static final int close_I = 60;
   private static final int open_I = 150;
   private static final int poll_I = 10;
   private static final float init_F_2 = 3.0F;
   private static final int push_I = 18;
   private static final int pop_I = 12;
   private final Font init_Font;
   private final AutismMacroEditorOverlay init_AutismMacroEditorO;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.c.CharEncoder init_CharEncoder_2 = new autismclient.gui.f.c.CharEncoder("Macro Library");
   private final autismclient.gui.f.c.TextFormat init_TextFormat = new autismclient.gui.f.c.TextFormat(this.init_CharEncoder, this.init_CharEncoder_2);
   private final autismclient.gui.f.b.BitSetUtil init_BitSetUtil = new autismclient.gui.f.b.BitSetUtil();
   private final autismclient.gui.f.b.BitSetUtil load_BitSetUtil = new autismclient.gui.f.b.BitSetUtil();
   private final autismclient.gui.f.b.BitSetUtil read_BitSetUtil = new autismclient.gui.f.b.BitSetUtil();
   private boolean write_Z;
   private String init_Str_2;
   private autismclient.gui.f.b.RefQueue init_RefQueue;
   private final F<AutismMacroListOverlay.Inner_StreamUtil> init_F_3;
   private autismclient.gui.f.c.ByteReader init_ByteReader;
   private autismclient.gui.f.c.ByteReader load_ByteReader;
   private final Set<String> init_Set = new HashSet<>();
   private long load_J = -9223372036854775808L;
   private final autismclient.gui.f.c.BitSetUtil init_BitSetUtil_2 = new autismclient.gui.f.c.BitSetUtil();
   private int tick_I = 500;
   private int run_I = 250;
   private int get_I = 240;
   private int set_I = 240;
   private int put_I = 220;
   private int add_I;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private boolean flush_Z = false;
   private float load_F;
   private float read_F;
   private final autismclient.gui.f.b.CacheEntry init_CacheEntry = new autismclient.gui.f.b.CacheEntry();
   private boolean close_Z = false;
   private boolean open_Z = false;
   private int clear_I = 0;
   private final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private String load_Str;
   private String read_Str;
   private long read_J;
   private int reset_I;
   private int start;
   private long write_J;
   private float write_F;
   private final autismclient.gui.f.c.PoolEntry init_PoolEntry = new autismclient.gui.f.c.PoolEntry("", autismclient.gui.f.b.l.write);
   private String write_Str;
   private double init_D;
   private double load_D;
   private float update_F;
   private boolean poll_Z;
   private boolean push_Z;
   private boolean pop_Z;
   private String update_Str;
   private String sync_Str;
   private int stop = -2147483648;
   private String flush_Str;
   private boolean tick_Z;
   private double read_D;
   private double write_D;
   private float sync_F;
   private boolean run_Z;
   private List<aM> init_List = List.of();
   private final Set<String> load_Set = new HashSet<>();
   private int next;
   private boolean get_Z;
   private boolean set_Z;
   private boolean put_Z;
   private String close_Str = "";
   private String open_Str;
   private List<String> load_List = List.of();
   private int prev = -2147483648;
   private int valid = -1;
   private long update_J = -9223372036854775808L;
   private int size = -2147483648;
   private String poll_Str = "";
   private boolean add_Z = false;
   private boolean clear_Z = true;
   private boolean reset_Z;
   private final List<AutismMacroListOverlay.Inner_DataUtil> read_List = new ArrayList<>();
   private final List<AutismMacroListOverlay.Inner_ConfigStore> write_List = new ArrayList<>(4);
   private List<AutismMacroListOverlay.Inner_StreamUtil> update_List = List.of();
   private long flush_J = -9223372036854775808L;
   private int data;

   public AutismMacroListOverlay(Font textRenderer) {
      this(textRenderer, null);
   }

   public AutismMacroListOverlay(Font textRenderer, AutismMacroEditorOverlay activeEditor) {
      this.init_Font = textRenderer;
      this.init_AutismMacroEditorO = activeEditor;
      this.init_F_3 = new F<>(this.init_CharEncoder, textRenderer, ignored -> this.init_StrArr(), 12);
      this.init_V_F(this.init_F_3);
      this.flush_V_10();
   }

   private void flush_V_10() {
      this.get_I = this.run_I_13();
      this.init_CharEncoder_2.flush(false);
      this.init_CharEncoder_2.init(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_2.init_CharEncoder_Z_Z(true, true);
      this.init_CharEncoder_2.init_CharEncoder_I_I(this.add_I_11() + 2, this.add_I_11() + this.next_I_4() + this.prev_I_3() + this.valid_I_2() + 12);
      this.init_CharEncoder_2.init_PacketUtil().init(this.clear_I_5()).init(autismclient.gui.f.c.IoWorker.init(this.add_I_11()));
      this.init_BitSetUtil.load_BitSetUtil_Str("Search macros...").read_BitSetUtil_I(this.reset_I_4()).close(true).init_BitSetUtil_Consumer(text -> {
         this.init_CacheEntry.load(0, 0);
         this.clear_Z = true;
      });
      this.load_BitSetUtil
         .load_BitSetUtil_Str("New macro name...")
         .read_BitSetUtil_I(this.reset_I_4())
         .close(true)
         .load_BitSetUtil_Consumer(text -> this.poll_Z_9());
      this.read_BitSetUtil
         .load_BitSetUtil_Str("Folder name...")
         .init_BitSetUtil_I(64)
         .read_BitSetUtil_I(this.reset_I_4())
         .close(true)
         .load_BitSetUtil_Consumer(text -> this.push_Z_8());
      this.open_V_8();
   }

   private void close_V_9() {
      aE sync = aE.init_aE();
      this.update_List = this.init_List_List(aN.init_aN().init_List(), sync.read_Map(), sync);
      if (!this.init_LinkedHashSet.isEmpty()) {
         Set<String> visibleKeys = new HashSet<>();

         for (AutismMacroListOverlay.Inner_StreamUtil item : this.update_List) {
            if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO && item.init_aM != null) {
               visibleKeys.add(cZ.init(item.init_aM.init_Str));
            }
         }

         this.init_LinkedHashSet.retainAll(visibleKeys);
      }
   }

   private void open_V_8() {
      this.init_CharEncoder_2.init_PacketUtil().init_V_2();
      this.init_ByteReader = null;
      this.load_ByteReader = null;
      this.close_V_9();
      this.set_I = this.push_I_15();
      this.get_I = this.run_I_13();
      this.init_CharEncoder_2.init("Macro Library [" + this.tick_I_13() + "]");
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil);
      if (this.update_List.isEmpty()) {
         this.init_CharEncoder_2
            .init_PacketUtil()
            .init_TableIndex_TableIndex(
               new autismclient.gui.f.c.PoolEntry("No macros match the current filter.", autismclient.gui.f.b.l.write).load_PoolEntry(true)
            );
      } else {
         this.init_BitSetUtil_2.init(this.init_I_I_7(this.update_List.size()));
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil_2);
      }

      if (this.write_Z) {
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.read_BitSetUtil);
         String confirmLabel = this.init_Str_2 == null ? "Create Folder" : "Rename";
         boolean grow = !this.init_Z_StrArr(confirmLabel, "Cancel");
         autismclient.gui.f.c.UiHelper actions = new autismclient.gui.f.c.UiHelper().init(this.start_I_10());
         actions.init_TableIndex_TableIndex(
            this.init_ByteReader_Str(confirmLabel, autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SUCCESS, this::push_Z_8, grow)
         );
         actions.init_TableIndex_TableIndex(this.init_ByteReader_Str("Cancel", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, this::add_V_6, grow));
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(actions);
      } else if (this.close_Z) {
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.load_BitSetUtil);
         boolean grow = !this.init_Z_StrArr("Create", "Cancel");
         autismclient.gui.f.c.UiHelper actions = new autismclient.gui.f.c.UiHelper().init(this.start_I_10());
         actions.init_TableIndex_TableIndex(this.init_ByteReader_Str("Create", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SUCCESS, this::poll_Z_9, grow));
         actions.init_TableIndex_TableIndex(this.init_ByteReader_Str("Cancel", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, this::put_V_5, grow));
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(actions);
         this.init_CharEncoder_2
            .init_PacketUtil()
            .init_TableIndex_TableIndex(
               new autismclient.gui.f.c.PoolEntry("Name the new macro made from the copied steps.", autismclient.gui.f.b.l.write).load_PoolEntry(true)
            );
      } else {
         boolean grow = !this.init_Z_StrArr("Create New", "Paste", "Folder", "Move", "Copy");
         autismclient.gui.f.c.UiHelper actions = new autismclient.gui.f.c.UiHelper().init(this.start_I_10());
         actions.init_TableIndex_TableIndex(
            this.init_ByteReader_Str("Create New", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SUCCESS, this::poll_V_6, grow)
         );
         actions.init_TableIndex_TableIndex(this.init_ByteReader_Str("Paste", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, this::set_V_6, grow));
         actions.init_TableIndex_TableIndex(
            this.init_ByteReader_Str("Folder", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, () -> this.init_V_Str_7(null), grow)
         );
         this.init_ByteReader = this.init_ByteReader_Str("Move", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, this::clear_V_5, grow);
         this.load_ByteReader = this.init_ByteReader_Str("Copy", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, this::run_Z_4, grow);
         actions.init_TableIndex_TableIndex(this.init_ByteReader);
         actions.init_TableIndex_TableIndex(this.load_ByteReader);
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(actions);
         if (!this.update_List.isEmpty()) {
            this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_PoolEntry.load_PoolEntry(true));
         }

         this.next_V_4();
      }
   }

   private autismclient.gui.f.c.ByteReader init_ByteReader_Str(
      String label, autismclient.gui.f.c.ByteReader.Inner_StreamUtil variant, Runnable onPress, boolean grow
   ) {
      autismclient.gui.f.c.ByteReader button = new autismclient.gui.f.c.ByteReader(label, variant, onPress).load_ByteReader_I(this.stop_I_4());
      button.init_ByteReader_I(5);
      if (grow) {
         button.read_ByteReader_Z(true);
      } else {
         button.init_ByteReader_F(24.0F);
      }

      return button;
   }

   private int init_I_StrArr(String... labels) {
      int required = this.start_I_10() * (labels.length - 1);

      for (String label : labels) {
         required += this.init_I_Str_5(label);
      }

      return required;
   }

   private boolean init_Z_StrArr(String... labels) {
      return this.init_Font == null ? false : this.init_I_StrArr(labels) <= this.get_I - this.add_I_11() * 2;
   }

   private int init_I_Str_5(String label) {
      return this.init_Font == null
         ? 24
         : autismclient.gui.f.RenderState.load(
            autismclient.gui.f.b.k.init(
               this.init_Font,
               label,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private int init_I_I_7(int itemCount) {
      int visibleRows = Math.max(3, Math.min(this.put_I_5(), itemCount));
      return visibleRows * this.set_I_11() + 2;
   }

   private int push_I_15() {
      if (this.init_Font == null) {
         return 240;
      }

      Identifier font = this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
      int color = this.init_CharEncoder.load(autismclient.gui.f.b.l.read);
      int nameWidth = 0;
      int stepsWidth = autismclient.gui.f.b.k.init(this.init_Font, init_Str_I_5(0), font, color);

      for (aM macro : aN.init_aN().init_List()) {
         if (!cL.load(macro)) {
            nameWidth = Math.max(nameWidth, autismclient.gui.f.b.k.init(this.init_Font, macro.init_Str, font, color));
            stepsWidth = Math.max(stepsWidth, autismclient.gui.f.b.k.init(this.init_Font, init_Str_I_5(macro.init_List.size()), font, color));
         }
      }

      nameWidth = Math.max(60, Math.min(150, nameWidth));
      int icon = this.size_I_2();
      int buttons = (this.reset_Z ? 0 : icon + this.value())
         + icon
         + this.value()
         + this.init_I_Str_5("Move")
         + this.value()
         + this.init_I_Str_5("COPY")
         + this.value()
         + icon;
      int row = this.state() + 10 + this.entry() + this.queue() + nameWidth + 8 + stepsWidth + 2 + this.value() + buttons + this.state();
      int list = row + 2 + 6;
      int actions = this.init_I_StrArr("Create New", "Paste", "Folder", "Move", "Copy");
      int title = autismclient.gui.f.b.k.init(
            this.init_Font,
            "Macro Library [" + this.tick_I_13() + "]",
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.load)
         )
         + this.add_I_11() * 2
         + 2
         + this.next_I_4()
         + this.prev_I_3()
         + this.valid_I_2()
         + 12;
      return Math.max(title, Math.max(list, actions) + this.add_I_11() * 2);
   }

   public AutismMacroEditorOverlay init_AutismMacroEditorO_3() {
      return this.init_AutismMacroEditorO;
   }

   public void load_V_Z_4(boolean configurationOnly) {
      if (this.reset_Z != configurationOnly) {
         this.reset_Z = configurationOnly;
         this.clear_Z = true;
      }
   }

   public void write_V_13() {
      ck shared = ck.init_ck();
      shared.reset(this.update_Z);
      shared.set(this.tick_I);
      shared.put(this.run_I);
      shared.add(this.init_CacheEntry.init());
      shared.tick(this.init_BitSetUtil.init_Str());
      this.a__V();
   }

   public void update_V_11() {
      ck shared = ck.init_ck();
      this.b__V();
      this.update_Z = shared.data_Z();
      this.tick_I = shared.add_I();
      this.run_I = shared.clear_I();
      this.init_CacheEntry.init(shared.reset_I());
      this.init_BitSetUtil.init_BitSetUtil_Str(shared.push_Str());
      this.clear_Z = true;
      this.init_CharEncoder_2.read_CharEncoder(!this.sync_Z);
   }

   @Override
   public String init_Str() {
      return "autism-macrolist";
   }

   @Override
   public int init_I() {
      return this.run_I_13();
   }

   @Override
   public int load_I() {
      return this.init_CharEncoder.load() + this.reset_I_4() + this.stop_I_4() + this.add_I_11() * 2 + this.clear_I_5();
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected int a__I() {
      return this.add_I > 0 ? this.add_I + this.init_I_I_7(3) : this.load_I();
   }

   @Override
   public cH init_cH() {
      return new cH(this.tick_I, this.run_I, this.get_I, this.put_I, this.update_Z, this.sync_Z);
   }

   @Override
   public void init_V_cH(cH bounds) {
      if (bounds != null) {
         cH clamped = this.init_cH_cH_3(
            new cH(bounds.init_I, bounds.load_I, Math.max(this.run_I_13(), bounds.read), bounds.write, bounds.init_Z, bounds.load_Z)
         );
         this.tick_I = clamped.init_I;
         this.run_I = clamped.load_I;
         this.get_I = clamped.read;
         this.put_I = clamped.write;
         this.update_Z = clamped.init_Z;
         this.sync_Z = clamped.load_Z;
      }
   }

   @Override
   public void init_V_Z(boolean visible) {
      if (visible && this.init_AutismMacroEditorO != null && this.init_AutismMacroEditorO.sync_Z()) {
         this.update_Z = false;
         this.write_V_13();
         bk.init_bk().update_V(this.init_AutismMacroEditorO);
      } else {
         this.update_Z = visible;
         if (!visible) {
            this.init_TextFormat.load();
            this.flush_Z = false;
            this.init_RefQueue = null;
            this.init_F_3.init_V();
            this.prev_V_4();
            this.size_V_2();
         } else {
            this.clear_Z = true;
            this.init_CharEncoder_2.read_CharEncoder(!this.sync_Z);
            bk.init_bk().update_V(this);
         }

         this.write_V_13();
      }
   }

   public void sync_V_10() {
      this.init_V_Z(!this.update_Z);
   }

   @Override
   public boolean sync_Z() {
      return this.update_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.sync_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      this.sync_Z = collapsed;
      this.flush_Z = false;
      if (collapsed) {
         this.init_TextFormat.load();
         this.init_RefQueue = null;
         this.init_F_3.init_V();
         this.prev_V_4();
         this.size_V_2();
      }

      this.write_V_13();
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean write_Z() {
      return this.init_TextFormat.init_Z();
   }

   @Override
   public void push_V() {
      this.init_TextFormat.load();
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.update_Z) {
         return false;
      } else {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         float uiX = viewport.init_F_D(mouseX);
         float uiY = viewport.load(mouseY);
         if (this.init_F_3.init(uiX, uiY)) {
            return true;
         } else {
            return this.init_RefQueue != null && this.init_RefQueue.init_Z_2() && this.init_RefQueue.init_Z(Math.round(uiX), Math.round(uiY))
               ? true
               : uiX >= this.tick_I && uiX <= this.tick_I + this.get_I && uiY >= this.run_I && uiY <= this.run_I + this.put_I;
         }
      }
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.update_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiX = viewport.init_F_D(mouseX);
      float uiY = viewport.load(mouseY);
      return this.init_Z_F_F_4(uiX, uiY) && !this.load_Z_F_F_3(uiX, uiY);
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if ((this.update_Z || this.a__Z()) && init_Minecraft != null && init_Minecraft.font != null) {
         long remoteRevision = aE.init_aE().init_J();
         if (remoteRevision != this.load_J) {
            this.load_J = remoteRevision;
            this.clear_Z = true;
         }

         long currentMacroRevision = aN.init_aN().init_J();
         int currentMacroCount = this.tick_I_13();
         if (currentMacroRevision != this.update_J) {
            this.clear_Z = true;
         }

         if (!this.clear_Z && currentMacroCount == this.valid && this.close_Z == this.add_Z) {
            if (currentMacroCount != this.size) {
               this.size = currentMacroCount;
               this.poll_Str = "Macro Library [" + currentMacroCount + "]";
            }

            this.init_CharEncoder_2.init(this.poll_Str);
         } else {
            this.open_V_8();
            this.valid = currentMacroCount;
            this.update_J = currentMacroRevision;
            this.add_Z = this.close_Z;
            this.clear_Z = false;
         }

         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         boolean active = bk.init_bk().update_Z(this) || bk.init_bk().write_Z(this);
         boolean headerHovered = this.init_Z_F_F_4(uiMouseX, uiMouseY);
         autismclient.gui.f.c.SyncManager metrics = new autismclient.gui.f.c.SyncManager(
            context, init_Minecraft.font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta
         );
         this.init_CharEncoder_2.load_CharEncoder(!this.sync_Z);
         this.init_CharEncoder_2.update(active);
         this.init_CharEncoder_2.sync(headerHovered);
         this.get_I = this.init_I_I_I(this.run_I_13(), this.run_I_13());
         int autoList = this.init_I_I_7(this.update_List.size());
         if (!this.update_List.isEmpty()) {
            this.init_BitSetUtil_2.init(autoList);
         }

         this.put_I = Math.round(this.init_CharEncoder_2.init_F_SyncManager_F(metrics, this.get_I));
         if (!this.update_List.isEmpty() && !this.sync_Z) {
            this.add_I = this.put_I - autoList;
            if (this.set_Z()) {
               int want = this.load_I_I_I(this.put_I, this.a__I());
               this.init_BitSetUtil_2.init(Math.max(this.init_I_I_7(3), want - this.add_I));
               this.put_I = Math.round(this.init_CharEncoder_2.init_F_SyncManager_F(metrics, this.get_I));
            }
         }

         this.put_I = Math.max(this.init_CharEncoder.load(), this.put_I);
         cH clamped = this.init_cH_cH_3(new cH(this.tick_I, this.run_I, this.get_I, this.put_I, this.update_Z, this.sync_Z));
         this.tick_I = clamped.init_I;
         this.run_I = clamped.load_I;
         this.get_I = clamped.read;
         this.put_I = clamped.write;
         this.init_CharEncoder_2.init_TableIndex_F(this.tick_I, this.run_I, this.get_I, this.put_I);
         this.init_PoolEntry
            .init(
               this.init_LinkedHashSet.isEmpty()
                  ? "Click to select, drag to move. Right-click for more."
                  : this.init_LinkedHashSet.size() + " selected. Drag to move, right-click for more."
            );
         this.init_TextFormat.init(context, mouseX, mouseY, delta);
         if (!this.update_List.isEmpty() && this.init_CharEncoder_2.init(context)) {
            try {
               this.init_V_GuiGraphicsExtract_4(context, viewport, uiMouseX, uiMouseY, delta, active);
            } finally {
               autismclient.gui.f.HashUtil.init_HashUtil().init(context);
            }
         } else {
            this.read_List.clear();
         }

         if (this.init_RefQueue != null && !this.sync_Z) {
            this.init_RefQueue.init_V_SyncManager(autismclient.gui.f.UiHelper.init(context, this.init_Font, Math.round(uiMouseX), Math.round(uiMouseY)));
         }

         if (this.init_F_3.init_Z() && !this.sync_Z) {
            context.nextStratum();
            this.init_F_3.init(context, Math.round(uiMouseX), Math.round(uiMouseY));
         }
      }
   }

   private void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor context, autismclient.gui.f.c.RingBuffer viewport, float uiMouseX, float uiMouseY, float delta, boolean active
   ) {
      this.read_List.clear();
      int viewX = Math.round(this.init_BitSetUtil_2.load_F());
      int viewY = Math.round(this.init_BitSetUtil_2.read_F());
      int viewW = Math.round(this.init_BitSetUtil_2.write_F());
      int viewH = Math.round(this.init_BitSetUtil_2.update_F());
      if (viewW > 2 && viewH > 2) {
         if (this.run_Z || this.poll_Z) {
            this.init_V_F(uiMouseY, viewX, viewY, viewW, viewH);
         }

         autismclient.gui.f.b.TextFormat.Inner_DataUtil baseLayout = this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, 0);
         int drawScroll = this.init_CacheEntry.init_I_F_I(delta, baseLayout.open());
         autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout = this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, drawScroll);
         if (this.run_Z) {
            this.init_V_TextFormat_DataUti(listLayout, uiMouseX, uiMouseY);
         }

         if (this.poll_Z) {
            this.load_V_TextFormat_DataUti(listLayout, uiMouseX, uiMouseY);
         }

         float alpha = active ? 1.0F : 0.56F;
         autismclient.gui.f.PacketUtil.init(
            context,
            autismclient.gui.f.NetHelper.init(viewX, viewY, viewW, viewH),
            autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.reset(), alpha),
            autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.set(), alpha)
         );
         listLayout.init(context);

         try {
            autismclient.gui.f.c.SyncManager rowContext = new autismclient.gui.f.c.SyncManager(
               context, this.init_Font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta, alpha
            );
            listLayout.init(
               this.update_List.size(),
               (i, rowY) -> {
                  AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
                  if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER) {
                     this.read(context, rowContext, item, viewX + 1, rowY, listLayout.push());
                  } else if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.SECTION_HEADER) {
                     this.init_V_GuiGraphicsExtract_6(context, rowContext, item, viewX + 1, rowY, listLayout.push());
                  } else {
                     this.load(context, rowContext, item, viewX + 1, rowY, listLayout.push());
                     if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO
                        && item.init_aM != null
                        && item.init_aM.read != null
                        && !item.init_aM.read.isEmpty()) {
                        boolean continues = i + 1 < this.update_List.size()
                           && this.update_List.get(i + 1).init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO
                           && this.update_List.get(i + 1).init_aM != null
                           && cY.load_Str_Str(this.update_List.get(i + 1).init_aM.read).equals(cY.load_Str_Str(item.init_aM.read));
                        int railTop = rowY
                           - (
                              i > 0 && this.update_List.get(i - 1).init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER
                                 ? this.set_I_11() - this.get_I_11()
                                 : 0
                           );
                        int railBottom = rowY + (continues ? this.set_I_11() : this.get_I_11());
                        autismclient.gui.f.b.TableIndex.read(
                           context,
                           viewX + 1 + this.state() + 2,
                           railTop,
                           2,
                           railBottom - railTop,
                           autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.run() & 16777215 | -1728053248, alpha)
                        );
                     }
                  }
               }
            );
            if (this.run_Z) {
               if (this.get_Z && !this.put_Z && !this.set_Z && this.prev != -2147483648) {
                  autismclient.gui.f.b.TableIndex.init(
                     context, viewX + 1 + 2, this.prev - 1, listLayout.push() - 3, 2, autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.run(), alpha)
                  );
               }

               int ghostTop = Math.round(uiMouseY - this.sync_F);

               for (int j = 0; j < this.init_List.size(); j++) {
                  this.init_V_GuiGraphicsExtract_7(
                     context, this.init_List.get(j), viewX + 1, ghostTop + (j - this.next) * this.set_I_11(), listLayout.push(), alpha
                  );
               }
            }

            if (this.poll_Z) {
               if (this.push_Z && !this.pop_Z && this.stop != -2147483648) {
                  autismclient.gui.f.b.TableIndex.init(
                     context, viewX + 1 + 2, this.stop - 1, listLayout.push() - 3, 2, autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.run(), alpha)
                  );
               }

               this.init_V_GuiGraphicsExtract_7(context, viewX + 1, Math.round(uiMouseY - this.update_F), listLayout.push());
            }
         } finally {
            listLayout.load(context);
         }

         listLayout.init(context, uiMouseX, uiMouseY, this.open_Z);
      }
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_I(int viewX, int viewY, int viewW, int viewH) {
      autismclient.gui.f.b.TextFormat.Inner_DataUtil baseLayout = this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, 0);
      return this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, this.init_CacheEntry.init_I_F_I(0.0F, baseLayout.open())).init_PoolEntry_DataUtil();
   }

   private autismclient.gui.f.b.TextFormat.Inner_DataUtil init_TextFormat_DataUti_I(int viewX, int viewY, int viewW, int viewH, int scrollOffset) {
      return autismclient.gui.f.b.TextFormat.init(
         viewX, viewY, viewW, viewH, this.update_List.size(), this.get_I_11(), this.set_I_11(), scrollOffset, 6, 6, 1, 7, true
      );
   }

   private void init_V_GuiGraphicsExtract_6(
      GuiGraphicsExtractor context, autismclient.gui.f.c.SyncManager rowContext, AutismMacroListOverlay.Inner_StreamUtil item, int x, int y, int width
   ) {
      int textY = j.init(y, this.get_I_11(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.load), this.init_CharEncoder.flush());
      autismclient.gui.f.b.TableIndex.init(context, x + this.buffer(), y + this.get_I_11() - 2, width - this.buffer() * 2, rowContext.init_I_I(1124073471));
      autismclient.gui.f.b.k.init(
         context,
         this.init_Font,
         item.init_Str,
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load),
         rowContext.init_I_I(item.init_I),
         x + this.buffer(),
         textY,
         false
      );
   }

   private void load(
      GuiGraphicsExtractor context, autismclient.gui.f.c.SyncManager rowContext, AutismMacroListOverlay.Inner_StreamUtil item, int x, int y, int width
   ) {
      boolean localRow = item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO && item.init_aM != null;
      boolean draggedRow = localRow
         && (this.run_Z && this.load_Set.contains(cZ.init(item.init_aM.init_Str)) || this.poll_Z && this.load_Z_Str_3(item.init_aM.read));
      if (draggedRow) {
         autismclient.gui.f.TaskQueue.init_V_F(0.35F);

         try {
            this.init_V_GuiGraphicsExtract_9(context, rowContext, item, x, y, width, localRow, true);
         } finally {
            autismclient.gui.f.TaskQueue.init_V();
         }
      } else {
         this.init_V_GuiGraphicsExtract_9(context, rowContext, item, x, y, width, localRow, false);
      }
   }

   private void init_V_GuiGraphicsExtract_9(
      GuiGraphicsExtractor context,
      autismclient.gui.f.c.SyncManager rowContext,
      AutismMacroListOverlay.Inner_StreamUtil item,
      int x,
      int y,
      int width,
      boolean localRow,
      boolean draggedRow
   ) {
      boolean hovered = !this.run_Z && !this.poll_Z && this.init_Z_F(x, y, width, this.get_I_11(), rowContext.init_F(), rowContext.load());
      boolean selectedRow = localRow && this.init_LinkedHashSet.contains(cZ.init(item.init_aM.init_Str));
      if (selectedRow && !draggedRow) {
         autismclient.gui.f.b.TableIndex.init(
            context, x, y, width, this.get_I_11(), rowContext.init_I_I(ct.init_I_I_ct_DataUtil(1715082778, ct.Inner_DataUtil.ACCENT))
         );
         autismclient.gui.f.PacketUtil.load(
            context,
            autismclient.gui.f.NetHelper.init(x, y, width, this.get_I_11()),
            rowContext.init_I_I(ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.OUTLINE))
         );
      } else if (hovered && !draggedRow) {
         autismclient.gui.f.b.TableIndex.init(
            context, x, y, width, this.get_I_11(), rowContext.init_I_I(ct.init_I_I_ct_DataUtil(452938314, ct.Inner_DataUtil.ACCENT))
         );
      }

      List<AutismMacroListOverlay.Inner_ConfigStore> buttons = this.init_List_AutismMacroListOve(item);
      int buttonCursor = x + width - this.state();
      int buttonY = y + this.cache();

      for (int i = buttons.size() - 1; i >= 0; i--) {
         AutismMacroListOverlay.Inner_ConfigStore button = buttons.get(i);
         buttonCursor -= button.init_I;
         this.init_V_GuiGraphicsExtract_5(context, rowContext, buttonCursor, buttonY, button.init_I, button);
         this.read_List
            .add(new AutismMacroListOverlay.Inner_DataUtil(buttonCursor, buttonY, button.init_I, this.size_I_2(), item, button.init_AutismMacroListOve));
         buttonCursor -= this.value();
      }

      int textLeft = x + this.state() + (item.init_aM != null && !item.init_aM.read.isEmpty() ? 10 : 0);
      if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO) {
         boolean running = init_Z_Str_4(item.init_aM.init_Str);
         int dotColor = running ? rowContext.init_I_I(-13379224) : rowContext.init_I_I(-10132123);
         int dotTop = y + j.load(0, this.get_I_11(), this.entry());
         autismclient.gui.f.b.TableIndex.read(context, textLeft, dotTop, this.entry(), this.entry(), dotColor);
         textLeft += this.entry() + this.queue();
      }

      int rightLimit = Math.max(textLeft + 20, buttonCursor - 2);
      int labelWidth = Math.max(20, rightLimit - textLeft);
      String var10000;
      switch (item.init_AutismMacroListOve) {
         case LOCAL_MACRO:
            var10000 = item.init_aM.init_Str;
            break;
         case REMOTE_MACRO:
            var10000 = item.init_Str;
            break;
         default:
            var10000 = item.init_Str;
      }

      String primaryText = var10000;
      switch (item.init_AutismMacroListOve) {
         case LOCAL_MACRO:
            var10000 = init_Str_I_5(item.init_aM.init_List.size());
            break;
         case REMOTE_MACRO:
            var10000 = item.load != null && !item.load.isBlank() ? item.load : "";
            break;
         default:
            var10000 = "";
      }

      String secondaryText = var10000;
      int var37;
      switch (item.init_AutismMacroListOve) {
         case LOCAL_MACRO:
            var37 = rowContext.init_I_I(this.init_CharEncoder.load(autismclient.gui.f.b.l.read));
            break;
         case REMOTE_MACRO:
            var37 = rowContext.init_I_I(-12122);
            break;
         default:
            var37 = rowContext.init_I_I(this.init_CharEncoder.load(autismclient.gui.f.b.l.read));
      }

      int primaryColor = var37;
      int secondaryColor = rowContext.init_I_I(this.init_CharEncoder.load(autismclient.gui.f.b.l.write));
      int textY = j.init(y, this.get_I_11(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
      if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO) {
         int secondaryWidth = autismclient.gui.f.b.k.init(
            this.init_Font, secondaryText, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), secondaryColor
         );
         int secondaryX = Math.max(textLeft + 10, rightLimit - secondaryWidth);
         int primaryMaxWidth = Math.max(20, secondaryX - textLeft - 8);
         String trimmedPrimary = autismclient.gui.f.b.k.init(
            this.init_Font, primaryText, primaryMaxWidth, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
         );
         autismclient.gui.f.b.k.init(
            context, this.init_Font, trimmedPrimary, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor, textLeft, textY, false
         );
         autismclient.gui.f.b.k.init(
            context,
            this.init_Font,
            secondaryText,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            secondaryColor,
            secondaryX,
            textY,
            false
         );
      } else {
         String trimmedPrimary = autismclient.gui.f.b.k.init(
            this.init_Font, primaryText, labelWidth, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
         );
         autismclient.gui.f.b.k.init(
            context, this.init_Font, trimmedPrimary, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor, textLeft, textY, false
         );
         if (!secondaryText.isEmpty()) {
            int secondaryX = textLeft
               + Math.min(
                  labelWidth - 10,
                  Math.max(
                     autismclient.gui.f.b.k.init(
                           this.init_Font, trimmedPrimary, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
                        )
                        + 10,
                     labelWidth - 90
                  )
               );
            int secondaryWidth = Math.max(0, rightLimit - secondaryX);
            if (secondaryWidth > 18) {
               String trimmedSecondary = autismclient.gui.f.b.k.init(
                  this.init_Font, secondaryText, secondaryWidth, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), secondaryColor
               );
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  trimmedSecondary,
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                  secondaryColor,
                  secondaryX,
                  textY,
                  false
               );
            }
         }
      }
   }

   private static String init_Str_I_5(int count) {
      return count + (count == 1 ? " step" : " steps");
   }

   private void init_V_GuiGraphicsExtract_5(
      GuiGraphicsExtractor context, autismclient.gui.f.c.SyncManager rowContext, int x, int y, int width, AutismMacroListOverlay.Inner_ConfigStore button
   ) {
      if (button.init_AutismMacroListOve == AutismMacroListOverlay.Inner_BufferHelper.DELETE) {
         int size = this.size_I_2();
         autismclient.gui.f.b.FileCache.init(
            context, x + Math.max(0, (width - size) / 2), y, size, this.init_Z_F(x, y, width, this.size_I_2(), rowContext.init_F(), rowContext.load())
         );
      } else {
         int h = this.size_I_2();
         boolean hovered = this.init_Z_F(x, y, width, h, rowContext.init_F(), rowContext.load());
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
            rowContext.init_GuiGraphicsExtract(), rowContext.init_Font(), Math.round(rowContext.init_F()), Math.round(rowContext.load())
         );
         autismclient.gui.f.b.CachePool.init(
            ui,
            autismclient.gui.f.NetHelper.init(x, y, width, h),
            button.init_Str,
            autismclient.gui.f.c.ByteReader.init_CachePool_DataUtil_ByteReader_StreamU(button.init_ByteReader_StreamU),
            hovered,
            false
         );
         if (button.init_Identifier != null) {
            int iconSize = 8;
            cz.init(context, button.init_Identifier, x + (width - iconSize) / 2, y + (h - iconSize) / 2, iconSize, -218103809);
         }
      }
   }

   private int init_I_Str_I_4(String label, int maxWidth) {
      return Math.min(maxWidth, this.init_I_Str_5(label));
   }

   private List<AutismMacroListOverlay.Inner_ConfigStore> init_List_AutismMacroListOve(AutismMacroListOverlay.Inner_StreamUtil item) {
      this.write_List.clear();
      switch (item.init_AutismMacroListOve) {
         case LOCAL_MACRO:
            boolean running = init_Z_Str_4(item.init_aM.init_Str);
            if (!this.reset_Z) {
               this.write_List
                  .add(
                     new AutismMacroListOverlay.Inner_ConfigStore(
                        "",
                        running ? autismclient.gui.f.c.ByteReader.Inner_StreamUtil.DANGER : autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SUCCESS,
                        this.data_I_2(),
                        AutismMacroListOverlay.Inner_BufferHelper.RUN_TOGGLE,
                        running ? cz.close : cz.flush
                     )
                  );
            }

            this.write_List
               .add(
                  new AutismMacroListOverlay.Inner_ConfigStore(
                     "", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.DANGER, this.data_I_2(), AutismMacroListOverlay.Inner_BufferHelper.EDIT, cz.poll
                  )
               );
            this.write_List
               .add(
                  new AutismMacroListOverlay.Inner_ConfigStore(
                     "Move",
                     autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY,
                     this.init_I_Str_I_4("Move", 44),
                     AutismMacroListOverlay.Inner_BufferHelper.MOVE
                  )
               );
            this.write_List
               .add(
                  new AutismMacroListOverlay.Inner_ConfigStore(
                     "Copy",
                     autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY,
                     this.init_I_Str_I_4("Copy", 44),
                     AutismMacroListOverlay.Inner_BufferHelper.COPY
                  )
               );
            this.write_List
               .add(
                  new AutismMacroListOverlay.Inner_ConfigStore(
                     "X", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.DANGER, this.data_I_2(), AutismMacroListOverlay.Inner_BufferHelper.DELETE
                  )
               );
            break;
         case REMOTE_MACRO:
            this.write_List
               .add(
                  new AutismMacroListOverlay.Inner_ConfigStore(
                     "IMPORT",
                     autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY,
                     this.init_I_Str_I_4("IMPORT", 58),
                     AutismMacroListOverlay.Inner_BufferHelper.IMPORT_REMOTE
                  )
               );
            break;
      }

      return this.write_List;
   }

   private void init_V_AutismMacroListOve_AutismMacroListOve(AutismMacroListOverlay.Inner_StreamUtil item, AutismMacroListOverlay.Inner_BufferHelper action) {
      if (item != null && action != null) {
         switch (action) {
            case TOGGLE_FOLDER:
               if (item.load == null) {
                  aN.init_aN().init_V_Str(item.init_Str);
               } else {
                  String key = item.load + "\u0000" + cY.load_Str_Str(item.init_Str);
                  if (!this.init_Set.remove(key)) {
                     this.init_Set.add(key);
                  }
               }

               this.open_Z = false;
               this.clear_Z = true;
               break;
            case RENAME_FOLDER:
               this.init_V_Str_7(item.init_Str);
               break;
            case REMOVE_FOLDER:
               aN.init_aN().load_Z(item.init_Str);
               bi.load("Folder removed; macros kept.");
               this.clear_Z = true;
               break;
            case MOVE:
               this.read(item);
               break;
            case RUN_TOGGLE:
               if (this.reset_Z) {
                  return;
               }

               if (item.init_aM == null) {
                  return;
               }

               if (init_Z_Str_4(item.init_aM.init_Str)) {
                  MacroExecutor.init_V_Str(item.init_aM.init_Str);
               } else {
                  item.init_aM.init_V();
               }
               break;
            case EDIT:
               if (item.init_aM == null) {
                  return;
               }

               this.init_V_Z(false);
               AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_V_aM_Z(item.init_aM, true);
               break;
            case COPY:
               if (item.init_aM == null) {
                  return;
               }

               if (o.init_Z(List.of(item.init_aM), false)) {
                  bi.init("Copied macro: " + item.init_aM.init_Str);
               } else {
                  bi.write("Failed to copy macro.");
               }
               break;
            case DELETE:
               if (item.init_aM != null) {
                  aN.init_aN().read(item.init_aM);
               }
               break;
            case IMPORT_REMOTE:
               this.init_V_AutismMacroListOve(item);
               break;
         }
      }
   }

   private static boolean init_Z_Str_4(String name) {
      return MacroExecutor.init_Z_Str(name);
   }

   public void init_V_I_7(int count) {
      List<aM> picked = new ArrayList<>();
      AutismMacroListOverlay.Inner_StreamUtil first = null;

      for (AutismMacroListOverlay.Inner_StreamUtil item : this.update_List) {
         if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO && item.init_aM != null) {
            if (first == null) {
               first = item;
            }

            if (picked.size() < count) {
               picked.add(item.init_aM);
            }
         }
      }

      if (first != null) {
         this.init_V_List_4(picked);
         int menuX = Math.round(this.init_BitSetUtil_2.load_F()) + 40;
         int menuY = Math.round(this.init_BitSetUtil_2.read_F()) + this.set_I_11() * Math.max(1, picked.size());
         this.reset_I = menuX;
         this.start = menuY;
         this.init_F_3.init(menuX, menuY, first);
      }
   }

   private void init_V_AutismMacroListOve(AutismMacroListOverlay.Inner_StreamUtil item) {
      if (item.load != null && !item.load.isBlank()) {
         aE sync = aE.init_aE();
         Map<String, Map<String, aM>> allRemote = sync.read_Map();
         Map<String, aM> remoteMacros = allRemote.get(item.load);
         aM source = remoteMacros != null ? remoteMacros.get(cZ.init(item.init_Str)) : null;
         if (source == null) {
            AutismClientMessaging.sendPrefixed("§cRemote macro is no longer available: " + item.init_Str);
         } else {
            aM imported = aN.init_aN().init_aM_aM_Str(source, source.init_Str);
            if (imported == null) {
               AutismClientMessaging.sendPrefixed("§cFailed to import macro: " + item.init_Str);
            } else {
               AutismClientMessaging.sendPrefixed("§aImported macro as: " + imported.init_Str);
               this.clear_Z = true;
            }
         }
      }
   }

   private void poll_V_6() {
      this.init_V_Z(false);
      AutismMacroEditorOverlay.init_AutismMacroEditorO_2().init_V_aM_Z(null, true);
   }

   private void set_V_6() {
      switch (o.init_o_DataUtil()) {
         case MACROS:
            this.clear_Z_3();
            break;
         case STEPS:
            this.close_Z = true;
            this.load_BitSetUtil.init_BitSetUtil_Str("");
            this.init_CacheEntry.load(0, 0);
            this.clear_Z = true;
            this.load_BitSetUtil.init_V_Z(true);
            break;
         default:
            bi.read("Clipboard has no macros or steps.");
      }
   }

   private void put_V_5() {
      this.close_Z = false;
      this.load_BitSetUtil.init_BitSetUtil_Str("");
      this.load_BitSetUtil.init_V_Z(false);
      this.clear_Z = true;
   }

   private boolean poll_Z_9() {
      String name = this.load_BitSetUtil.init_Str().trim();
      if (name.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cEnter a name for the pasted macro.");
         return true;
      } else if (aN.init_aN().init_aM_Str(name) != null) {
         AutismClientMessaging.sendPrefixed("§cA macro with that name already exists.");
         return true;
      } else {
         aM pasted = o.init_aM();
         if (pasted == null) {
            bi.write("Clipboard has no macro steps.");
            return true;
         } else {
            pasted.init_Str = name;
            aN.init_aN().init_V_aM(pasted);
            bi.load("Created macro: " + pasted.init_Str);
            this.init_BitSetUtil.init_BitSetUtil_Str("");
            this.init_V_List_4(List.of(pasted));
            this.put_V_5();
            return true;
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.update_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (this.init_F_3.init_Z()) {
         if (button == 0) {
            if (this.init_F_3.init(uiMouseX, uiMouseY, button, (item, label, index) -> this.init_V_AutismMacroListOve_I(item, index))) {
               return true;
            }
         } else if (button == 1 && !this.init_F_3.init(uiMouseX, uiMouseY)) {
            this.init_F_3.init_V();
         } else if (button == 1) {
            return true;
         }
      }

      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         this.init_RefQueue.init_EventBus_I_2(Math.round(uiMouseX), Math.round(uiMouseY), button);
         return true;
      }

      if (button == 0 && this.load_Z_F_F_3(uiMouseX, uiMouseY)) {
         this.init_V_Z(false);
         return true;
      }

      if (button == 0 && this.init_Z_F_F_4(uiMouseX, uiMouseY)) {
         this.flush_Z = true;
         this.load_F = uiMouseX - this.tick_I;
         this.read_F = uiMouseY - this.run_I;
         return true;
      }

      if (!this.sync_Z && this.init_TextFormat.init_Z_D_2(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.sync_Z
         && button == 0
         && this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_I(
            Math.round(this.init_BitSetUtil_2.load_F()),
            Math.round(this.init_BitSetUtil_2.read_F()),
            Math.round(this.init_BitSetUtil_2.write_F()),
            Math.round(this.init_BitSetUtil_2.update_F())
         );
         if (metrics.init_Z() && metrics.init(uiMouseX, uiMouseY)) {
            this.open_Z = true;
            this.clear_I = metrics.load(uiMouseX, uiMouseY) ? Math.round(uiMouseY) - metrics.update() : metrics.sync() / 2;
            this.init_CacheEntry.init_V_PoolEntry_DataUtil(metrics, uiMouseY, this.clear_I);
            return true;
         }
      }

      if (!this.sync_Z
         && (button == 0 || button == 1)
         && this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         boolean overRegion = false;

         for (int i = this.read_List.size() - 1; i >= 0; i--) {
            if (this.read_List.get(i).init(uiMouseX, uiMouseY)) {
               overRegion = true;
               break;
            }
         }

         if (!overRegion) {
            autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout = this.init_TextFormat_DataUti();
            int rowIndex = this.init_I_F_TextFormat_DataUti(uiMouseY, listLayout);
            AutismMacroListOverlay.Inner_StreamUtil rowItem = rowIndex >= 0 ? this.update_List.get(rowIndex) : null;
            if (rowItem != null && rowItem.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO && rowItem.init_aM != null) {
               if (button == 1) {
                  this.init_V_AutismMacroListOve(rowItem, Math.round(uiMouseX), Math.round(uiMouseY));
                  return true;
               }

               if (this.tick_Z_5()) {
                  this.write(rowItem.init_aM);
               } else if (this.pop_Z_5()) {
                  this.read(rowItem.init_aM);
               } else {
                  this.init_V_AutismMacroListOve(rowItem, uiMouseX, uiMouseY, listLayout, rowIndex);
               }

               return true;
            }

            if (button == 0) {
               if (rowIndex < 0) {
                  this.reset_V_4();
               }

               return true;
            }
         }
      }

      if (!this.sync_Z
         && button == 0
         && this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         for (int i = this.read_List.size() - 1; i >= 0; i--) {
            AutismMacroListOverlay.Inner_DataUtil region = this.read_List.get(i);
            if (region.init(uiMouseX, uiMouseY)) {
               if (region.init_AutismMacroListOve == AutismMacroListOverlay.Inner_BufferHelper.TOGGLE_FOLDER && region.init_AutismMacroListOve_2.load == null) {
                  this.write_Str = region.init_AutismMacroListOve_2.init_Str;
                  this.init_D = uiMouseX;
                  this.load_D = uiMouseY;
                  this.update_F = uiMouseY - region.load;
                  return true;
               }

               this.init_V_AutismMacroListOve_AutismMacroListOve(region.init_AutismMacroListOve_2, region.init_AutismMacroListOve);
               return true;
            }
         }
      }

      return this.init_Z_D_D(mouseX, mouseY);
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         this.init_RefQueue.load(Math.round(uiMouseX), Math.round(uiMouseY), button);
         return true;
      }

      if (button == 0 && this.flush_Z) {
         this.flush_Z = false;
         this.write_V_13();
         return true;
      }

      if (button == 0 && this.open_Z) {
         this.open_Z = false;
         this.write_V_13();
         return true;
      }

      if (button == 0 && this.write_Str != null) {
         this.data_V_2();
         return true;
      }

      if (button != 0 || !this.run_Z && this.flush_Str == null) {
         return !this.sync_Z && this.init_TextFormat.load(mouseX, mouseY, button) ? true : this.init_Z_D_D(mouseX, mouseY);
      }

      this.valid_V_2();
      return true;
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.update_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         this.init_RefQueue.init_EventBus_I(Math.round(uiMouseX), Math.round(uiMouseY), button, deltaX, deltaY);
         return true;
      }

      if (this.flush_Z && button == 0) {
         int nextX = Math.round(uiMouseX - this.load_F);
         int nextY = Math.round(uiMouseY - this.read_F);
         cH clamped = this.init_cH_cH_3(new cH(nextX, nextY, this.get_I, this.put_I, this.update_Z, this.sync_Z));
         this.tick_I = clamped.init_I;
         this.run_I = clamped.load_I;
         return true;
      }

      if (this.open_Z && button == 0) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_I(
            Math.round(this.init_BitSetUtil_2.load_F()),
            Math.round(this.init_BitSetUtil_2.read_F()),
            Math.round(this.init_BitSetUtil_2.write_F()),
            Math.round(this.init_BitSetUtil_2.update_F())
         );
         this.init_CacheEntry.init_V_PoolEntry_DataUtil(metrics, uiMouseY, this.clear_I);
         return true;
      }

      if (button == 0 && this.write_Str != null) {
         if (!this.poll_Z && (Math.abs(uiMouseX - this.init_D) >= 3.0 || Math.abs(uiMouseY - this.load_D) >= 3.0)) {
            this.poll_Z = true;
            this.write_J = 0L;
            this.write_F = 0.0F;
         }

         return true;
      } else if (button == 0 && this.flush_Str != null) {
         this.init_V_F_F(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.sync_Z && this.init_TextFormat.init(mouseX, mouseY, button, deltaX, deltaY) ? true : this.init_Z_D_D(mouseX, mouseY);
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.update_Z && !this.sync_Z) {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
            this.init_RefQueue.init_EventBus_I(Math.round(uiMouseX), Math.round(uiMouseY), amount);
            return true;
         } else if (this.init_TextFormat.init_Z_D(mouseX, mouseY, amount)) {
            return true;
         } else if (this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
            autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout = this.init_TextFormat_DataUti_I(
               Math.round(this.init_BitSetUtil_2.load_F()),
               Math.round(this.init_BitSetUtil_2.read_F()),
               Math.round(this.init_BitSetUtil_2.write_F()),
               Math.round(this.init_BitSetUtil_2.update_F()),
               0
            );
            this.init_CacheEntry.init_V_D(amount, this.set_I_11(), listLayout.open());
            return true;
         } else {
            return this.init_Z_D_D(mouseX, mouseY);
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.update_Z) {
         return false;
      }

      if (this.sync_Z) {
         return false;
      }

      if (keyCode == 256) {
         if (this.run_Z) {
            this.prev_V_4();
            return true;
         }

         if (this.write_Str != null) {
            this.size_V_2();
            return true;
         }

         if (this.init_F_3.init_Z()) {
            this.init_F_3.init_V();
            return true;
         }

         if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
            this.init_RefQueue.load_V_2();
            return true;
         }

         if (this.write_Z) {
            this.add_V_6();
            return true;
         }
      }

      if (!this.init_TextFormat.init_Z()) {
         boolean control = this.init_Z_I_3(modifiers);
         if (control && keyCode == 65) {
            this.stop_V_4();
            return true;
         }

         if (control && keyCode == 67) {
            return this.run_Z_4();
         }

         if (control && keyCode == 88) {
            return this.put_Z_3();
         }

         if (control && keyCode == 86) {
            return this.clear_Z_3();
         }

         if (control && keyCode == 68) {
            return this.reset_Z_3();
         }

         if (keyCode == 261 || keyCode == 259) {
            return this.load_Z_Z_2(false);
         }
      }

      if (this.init_TextFormat.init_Z_I(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 256 && this.close_Z) {
         this.put_V_5();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.update_Z && !this.sync_Z && this.init_TextFormat.init(chr, modifiers);
   }

   private List<AutismMacroListOverlay.Inner_StreamUtil> init_List_List(
      List<aM> localMacros, Map<String, Map<String, aM>> remoteMacros, aE sync
   ) {
      String filter = this.init_BitSetUtil.init_Str().trim().toLowerCase(Locale.ROOT);
      List<AutismMacroListOverlay.Inner_StreamUtil> items = new ArrayList<>();

      for (cY.Inner_DataUtil row : aN.init_aN().load(filter, true, macro -> !cL.load(macro))) {
         items.add(row.init_Z() ? AutismMacroListOverlay.Inner_StreamUtil.init(row, null) : AutismMacroListOverlay.Inner_StreamUtil.init(row.init_aM()));
      }

      if (sync.write_Z()) {
         for (Entry<String, Map<String, aM>> entry : remoteMacros.entrySet()) {
            List<aM> available = entry.getValue().values().stream().filter(m -> !cL.load(m)).filter(m -> aN.init_aN().init_aM_Str(m.init_Str) == null).toList();
            List<cY.Inner_DataUtil> remoteRows = cY.init(
               available, List.of(), folder -> this.init_Set.contains(entry.getKey() + "\u0000" + cY.load_Str_Str(folder)), filter, false
            );
            if (!remoteRows.isEmpty()) {
               items.add(AutismMacroListOverlay.Inner_StreamUtil.init(entry.getKey(), -12122));
            }

            for (cY.Inner_DataUtil row : remoteRows) {
               items.add(
                  row.init_Z()
                     ? AutismMacroListOverlay.Inner_StreamUtil.init(row, entry.getKey())
                     : AutismMacroListOverlay.Inner_StreamUtil.init(row.init_aM().init_Str, entry.getKey())
               );
            }
         }
      }

      return items;
   }

   private void read(
      GuiGraphicsExtractor graphics, autismclient.gui.f.c.SyncManager context, AutismMacroListOverlay.Inner_StreamUtil item, int x, int y, int width
   ) {
      if (this.poll_Z && item.load == null && this.load_Z_Str_3(item.init_Str)) {
         autismclient.gui.f.TaskQueue.init_V_F(0.35F);

         try {
            this.write(graphics, context, item, x, y, width);
         } finally {
            autismclient.gui.f.TaskQueue.init_V();
         }
      } else {
         this.write(graphics, context, item, x, y, width);
      }
   }

   private void write(
      GuiGraphicsExtractor graphics, autismclient.gui.f.c.SyncManager context, AutismMacroListOverlay.Inner_StreamUtil item, int x, int y, int width
   ) {
      int renameWidth = this.init_I_Str_I_4("Rename", 56);
      int removeWidth = this.init_I_Str_I_4("Remove", 56);
      int buttonY = y + this.cache();
      int controls = item.load == null ? renameWidth + this.value() + removeWidth + this.state() : 0;
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, Math.max(1, width - controls), this.get_I_11());
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.init_Font, Math.round(context.init_F()), Math.round(context.load()));
      boolean anyDrag = this.run_Z || this.poll_Z;
      autismclient.gui.f.b.TaskQueue.load(
         ui,
         bounds,
         item.init_cY_DataUtil.init_Str(),
         item.init_cY_DataUtil.load_Z(),
         !anyDrag && bounds.init_Z(Math.round(context.init_F()), Math.round(context.load()))
      );
      this.read_List
         .add(new AutismMacroListOverlay.Inner_DataUtil(x, y, bounds.update(), this.get_I_11(), item, AutismMacroListOverlay.Inner_BufferHelper.TOGGLE_FOLDER));
      if (this.run_Z && this.get_Z && this.set_Z && item.load == null && cY.load_Str_Str(item.init_Str).equals(cY.load_Str_Str(this.close_Str))) {
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, y, bounds.update(), this.get_I_11()),
            context.init_I_I(ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.OUTLINE))
         );
      }

      if (item.load == null) {
         AutismMacroListOverlay.Inner_ConfigStore rename = new AutismMacroListOverlay.Inner_ConfigStore(
            "Rename", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, renameWidth, AutismMacroListOverlay.Inner_BufferHelper.RENAME_FOLDER
         );
         AutismMacroListOverlay.Inner_ConfigStore remove = new AutismMacroListOverlay.Inner_ConfigStore(
            "Remove", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, removeWidth, AutismMacroListOverlay.Inner_BufferHelper.REMOVE_FOLDER
         );
         int renameX = x + width - controls;
         int removeX = renameX + renameWidth + this.value();
         this.init_V_GuiGraphicsExtract_5(graphics, context, renameX, buttonY, renameWidth, rename);
         this.init_V_GuiGraphicsExtract_5(graphics, context, removeX, buttonY, removeWidth, remove);
         this.read_List
            .add(
               new AutismMacroListOverlay.Inner_DataUtil(renameX, buttonY, renameWidth, this.size_I_2(), item, AutismMacroListOverlay.Inner_BufferHelper.RENAME_FOLDER)
            );
         this.read_List
            .add(
               new AutismMacroListOverlay.Inner_DataUtil(removeX, buttonY, removeWidth, this.size_I_2(), item, AutismMacroListOverlay.Inner_BufferHelper.REMOVE_FOLDER)
            );
      }
   }

   private void init_V_Str_7(String folder) {
      this.write_Z = true;
      this.close_Z = false;
      this.init_Str_2 = folder;
      this.read_BitSetUtil.init_BitSetUtil_Str(folder == null ? "" : folder);
      this.clear_Z = true;
      this.read_BitSetUtil.init_V_Z(true);
   }

   private void add_V_6() {
      this.write_Z = false;
      this.init_Str_2 = null;
      this.read_BitSetUtil.init_V_Z(false);
      this.clear_Z = true;
   }

   private boolean push_Z_8() {
      aN manager = aN.init_aN();
      boolean saved = this.init_Str_2 == null
         ? manager.init_Z_Str(this.read_BitSetUtil.init_Str())
         : manager.init_Z_Str_Str(this.init_Str_2, this.read_BitSetUtil.init_Str());
      if (!saved) {
         bi.read("Use a unique, nonempty folder name.");
         return true;
      } else {
         this.init_BitSetUtil.init_BitSetUtil_Str("");
         this.add_V_6();
         return true;
      }
   }

   private void read(AutismMacroListOverlay.Inner_StreamUtil item) {
      if (item.init_aM != null) {
         List<String> folders = new ArrayList<>();
         folders.add("");
         folders.addAll(aN.init_aN().load_List());
         List<String> labels = folders.stream().map(f -> f.isEmpty() ? "Unfiled" : "Folder: " + f).toList();
         AutismMacroListOverlay.Inner_DataUtil anchor = this.read_List
            .stream()
            .filter(r -> r.init_AutismMacroListOve_2 == item && r.init_AutismMacroListOve == AutismMacroListOverlay.Inner_BufferHelper.MOVE)
            .findFirst()
            .orElse(null);
         if (anchor != null) {
            int menuWidth = Math.max(
               56,
               j.init(
                     this.init_Font,
                     this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                     this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
                     labels
                  )
                  + 26
            );
            this.init_RefQueue = new autismclient.gui.f.b.RefQueue(
               autismclient.gui.f.NetHelper.init(anchor.init_I, anchor.load, menuWidth, this.size_I_2()),
               labels,
               item.init_aM.read.isEmpty() ? "Unfiled" : "Folder: " + item.init_aM.read,
               choice -> {
                  int index = labels.indexOf(choice);
                  if (index >= 0) {
                     aN.init_aN().init_Z_aM_Str(item.init_aM, folders.get(index));
                  }

                  this.clear_Z = true;
               }
            );
            this.init_RefQueue.init_V_2();
         }
      }
   }

   private void clear_V_5() {
      List<aM> selected = this.load_List_4();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
      } else {
         List<String> folders = new ArrayList<>();
         folders.add("");
         folders.addAll(aN.init_aN().load_List());
         List<String> labels = folders.stream().map(f -> f.isEmpty() ? "Unfiled" : "Folder: " + f).toList();
         int menuWidth = Math.max(
            56,
            j.init(
                  this.init_Font,
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                  this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
                  labels
               )
               + 26
         );
         String common = null;

         for (aM macro : selected) {
            String folder = macro.read == null ? "" : macro.read;
            if (common == null) {
               common = folder;
            } else if (!cY.load_Str_Str(common).equals(cY.load_Str_Str(folder))) {
               common = null;
               break;
            }
         }

         String current = common == null ? "" : (common.isEmpty() ? "Unfiled" : "Folder: " + common);
         autismclient.gui.f.NetHelper anchor = this.init_ByteReader != null
            ? autismclient.gui.f.NetHelper.init(
               Math.round(this.init_ByteReader.load_F()), Math.round(this.init_ByteReader.read_F()), menuWidth, this.size_I_2()
            )
            : autismclient.gui.f.NetHelper.init(this.reset_I, this.start, menuWidth, this.size_I_2());
         this.init_RefQueue = new autismclient.gui.f.b.RefQueue(anchor, labels, current, choice -> {
            int index = labels.indexOf(choice);
            if (index >= 0) {
               String target = folders.get(index);
               List<aM> moving = new ArrayList<>();

               for (aM macro : this.load_List_4()) {
                  if (!cY.load_Str_Str(macro.read).equals(cY.load_Str_Str(target))) {
                     moving.add(macro);
                  }
               }

               if (moving.isEmpty()) {
                  bi.init("Already in " + (target.isEmpty() ? "Unfiled" : target) + ".", -5195580);
               } else if (aN.init_aN().init(moving, target, null)) {
                  bi.load("Moved " + load_Str_I_2(moving.size()) + " to " + (target.isEmpty() ? "Unfiled" : target) + ".");
               }
            }

            this.clear_Z = true;
         });
         this.init_RefQueue.init_V_2();
      }
   }

   private boolean init_Z_aM_2(aM macro) {
      return macro != null && this.init_LinkedHashSet.contains(cZ.init(macro.init_Str));
   }

   private void init_V_aM_2(aM macro) {
      if (macro != null && macro.init_Str != null && !macro.init_Str.isBlank()) {
         this.init_LinkedHashSet.add(cZ.init(macro.init_Str));
      }
   }

   private void load_V_aM_2(aM macro) {
      this.init_LinkedHashSet.clear();
      this.init_V_aM_2(macro);
      this.load_Str = macro != null && macro.init_Str != null ? cZ.init(macro.init_Str) : null;
      this.next_V_4();
   }

   private void init_V_List_4(List<aM> macros) {
      this.init_LinkedHashSet.clear();

      for (aM macro : macros) {
         this.init_V_aM_2(macro);
      }

      this.load_Str = !macros.isEmpty() && macros.get(macros.size() - 1).init_Str != null ? cZ.init(macros.get(macros.size() - 1).init_Str) : null;
      this.next_V_4();
   }

   private void read(aM macro) {
      if (macro != null && macro.init_Str != null && !macro.init_Str.isBlank()) {
         String key = cZ.init(macro.init_Str);
         if (!this.init_LinkedHashSet.remove(key)) {
            this.init_LinkedHashSet.add(key);
         }

         this.load_Str = key;
         this.next_V_4();
      }
   }

   private void write(aM macro) {
      if (macro != null && macro.init_Str != null) {
         String key = cZ.init(macro.init_Str);
         int from = -1;
         int to = -1;

         for (int i = 0; i < this.update_List.size(); i++) {
            AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
            if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO && item.init_aM != null) {
               String itemKey = cZ.init(item.init_aM.init_Str);
               if (itemKey.equals(this.load_Str)) {
                  from = i;
               }

               if (itemKey.equals(key)) {
                  to = i;
               }
            }
         }

         if (from >= 0 && to >= 0) {
            this.init_LinkedHashSet.clear();

            for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
               AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
               if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO) {
                  this.init_V_aM_2(item.init_aM);
               }
            }

            this.next_V_4();
         } else {
            this.load_V_aM_2(macro);
         }
      }
   }

   private void reset_V_4() {
      if (!this.init_LinkedHashSet.isEmpty()) {
         this.init_LinkedHashSet.clear();
         this.next_V_4();
      }
   }

   private void stop_V_4() {
      for (AutismMacroListOverlay.Inner_StreamUtil item : this.update_List) {
         if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO) {
            this.init_V_aM_2(item.init_aM);
         }
      }

      this.next_V_4();
   }

   private List<aM> load_List_4() {
      List<aM> out = new ArrayList<>();
      if (this.init_LinkedHashSet.isEmpty()) {
         return out;
      }

      for (aM macro : aN.init_aN().init_List()) {
         if (macro != null && this.init_LinkedHashSet.contains(cZ.init(macro.init_Str))) {
            out.add(macro);
         }
      }

      return out;
   }

   private void next_V_4() {
      boolean hasSelection = !this.init_LinkedHashSet.isEmpty();
      if (this.init_ByteReader != null) {
         this.init_ByteReader.read_TableIndex_Z(hasSelection);
      }

      if (this.load_ByteReader != null) {
         this.load_ByteReader.read_TableIndex_Z(hasSelection);
      }
   }

   private boolean pop_Z_5() {
      if (init_Minecraft != null && init_Minecraft.getWindow() != null) {
         long handle = init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 341) == 1 || CompatGLFW.glfwGetKey(handle, 345) == 1;
      } else {
         return false;
      }
   }

   private boolean init_Z_I_3(int modifiers) {
      return (modifiers & 2) != 0 || this.pop_Z_5();
   }

   private boolean tick_Z_5() {
      if (init_Minecraft != null && init_Minecraft.getWindow() != null) {
         long handle = init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
      } else {
         return false;
      }
   }

   private boolean run_Z_4() {
      List<aM> selected = this.load_List_4();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      }

      if (o.init_Z(selected, false)) {
         bi.init("Copied " + load_Str_I_2(selected.size()) + ".");
      } else {
         bi.write("Failed to copy macros.");
      }

      return true;
   }

   private boolean put_Z_3() {
      List<aM> selected = this.load_List_4();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      } else if (!o.init_Z(selected, true)) {
         bi.write("Failed to copy macros.");
         return true;
      } else {
         int removed = aN.init_aN().init_I_Collection(selected);
         this.reset_V_4();
         bi.init("Cut " + load_Str_I_2(removed) + ". Paste to put them back.");
         return true;
      }
   }

   private boolean clear_Z_3() {
      o.Inner_StreamUtil paste = o.init_o_StreamUtil();
      if (paste.init_List().isEmpty()) {
         bi.read("Clipboard has no macros.");
         return true;
      } else {
         List<aM> selected = this.load_List_4();
         aM anchor = selected.isEmpty() ? null : selected.get(selected.size() - 1);
         List<aM> added = aN.init_aN().init(paste.init_List(), !paste.init_Z(), anchor);
         if (added.isEmpty()) {
            bi.write("Failed to paste macros.");
            return true;
         } else {
            this.init_BitSetUtil.init_BitSetUtil_Str("");
            this.init_V_List_4(added);
            this.clear_Z = true;
            bi.load("Pasted " + load_Str_I_2(added.size()) + ".");
            return true;
         }
      }
   }

   private boolean reset_Z_3() {
      List<aM> selected = this.load_List_4();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      }

      List<aM> copies = aN.init_aN().init_List_Collection(selected);
      if (!copies.isEmpty()) {
         this.init_V_List_4(copies);
         this.clear_Z = true;
         bi.load("Duplicated " + load_Str_I_2(copies.size()) + ".");
      }

      return true;
   }

   private boolean load_Z_Z_2(boolean confirmed) {
      List<aM> selected = this.load_List_4();
      if (selected.isEmpty()) {
         return false;
      }

      String signature = String.join("\u0000", this.init_LinkedHashSet);
      long now = System.currentTimeMillis();
      if (confirmed || signature.equals(this.read_Str) && now < this.read_J) {
         this.read_Str = null;
         int removed = aN.init_aN().init_I_Collection(selected);
         this.reset_V_4();
         if (removed > 0) {
            bi.load("Deleted " + load_Str_I_2(removed) + ".");
         }

         return true;
      } else {
         this.read_Str = signature;
         this.read_J = now + 3000L;
         bi.read("Press Delete again to delete " + load_Str_I_2(selected.size()) + ".");
         return true;
      }
   }

   private static String load_Str_I_2(int count) {
      return count + (count == 1 ? " macro" : " macros");
   }

   private String[] init_StrArr() {
      return new String[]{"Cut\tCtrl+X", "Copy\tCtrl+C", "Paste\tCtrl+V", "Duplicate\tCtrl+D", "Move to Folder...", "Delete\tDel", "Select All\tCtrl+A"};
   }

   private void init_V_AutismMacroListOve_I(AutismMacroListOverlay.Inner_StreamUtil item, int itemIndex) {
      switch (itemIndex) {
         case 0:
            this.put_Z_3();
            break;
         case 1:
            this.run_Z_4();
            break;
         case 2:
            this.clear_Z_3();
            break;
         case 3:
            this.reset_Z_3();
            break;
         case 4:
            this.clear_V_5();
            break;
         case 5:
            this.load_Z_Z_2(true);
            break;
         case 6:
            this.stop_V_4();
      }
   }

   private void init_V_AutismMacroListOve(AutismMacroListOverlay.Inner_StreamUtil item, int mouseX, int mouseY) {
      if (item.init_aM != null) {
         if (!this.init_Z_aM_2(item.init_aM)) {
            this.load_V_aM_2(item.init_aM);
         }

         this.init_RefQueue = null;
         this.reset_I = mouseX;
         this.start = mouseY;
         this.init_F_3.init(mouseX, mouseY, item);
      }
   }

   private autismclient.gui.f.b.TextFormat.Inner_DataUtil init_TextFormat_DataUti() {
      int viewX = Math.round(this.init_BitSetUtil_2.load_F());
      int viewY = Math.round(this.init_BitSetUtil_2.read_F());
      int viewW = Math.round(this.init_BitSetUtil_2.write_F());
      int viewH = Math.round(this.init_BitSetUtil_2.update_F());
      autismclient.gui.f.b.TextFormat.Inner_DataUtil baseLayout = this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, 0);
      return this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, this.init_CacheEntry.init_I_F_I(0.0F, baseLayout.open()));
   }

   private int init_I_F_TextFormat_DataUti(float uiMouseY, autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout) {
      if (this.update_List.isEmpty()) {
         return -1;
      }

      int index = (int)Math.floor((double)(uiMouseY - (listLayout.load() + listLayout.pop()) + listLayout.poll()) / listLayout.sync());
      return index >= 0 && index < this.update_List.size() ? index : -1;
   }

   private int init_I_TextFormat_DataUti_I(autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout, int index) {
      return listLayout.load() + listLayout.pop() - listLayout.poll() + index * listLayout.sync();
   }

   private void init_V_AutismMacroListOve(
      AutismMacroListOverlay.Inner_StreamUtil item, float uiMouseX, float uiMouseY, autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout, int rowIndex
   ) {
      this.flush_Str = cZ.init(item.init_aM.init_Str);
      this.tick_Z = this.init_LinkedHashSet.contains(this.flush_Str);
      this.read_D = uiMouseX;
      this.write_D = uiMouseY;
      this.sync_F = uiMouseY - this.init_I_TextFormat_DataUti_I(listLayout, rowIndex);
   }

   private void init_V_F_F(float uiMouseX, float uiMouseY) {
      if (!this.run_Z && this.flush_Str != null && (!(Math.abs(uiMouseX - this.read_D) < 3.0) || !(Math.abs(uiMouseY - this.write_D) < 3.0))) {
         aM pressed = aN.init_aN().init_aM_Str(this.flush_Str);
         if (pressed == null) {
            this.prev_V_4();
         } else {
            List<aM> dragged;
            if (this.init_LinkedHashSet.contains(this.flush_Str)) {
               dragged = this.load_List_4();
            } else {
               this.load_V_aM_2(pressed);
               dragged = List.of(pressed);
            }

            if (dragged.isEmpty()) {
               this.prev_V_4();
            } else {
               this.run_Z = true;
               this.init_List = dragged;
               this.load_Set.clear();

               for (aM macro : dragged) {
                  this.load_Set.add(cZ.init(macro.init_Str));
               }

               this.next = Math.max(0, dragged.indexOf(pressed));
               this.get_Z = false;
               this.set_Z = false;
               this.put_Z = false;
               this.open_Str = null;
            }
         }
      }
   }

   private void prev_V_4() {
      this.flush_Str = null;
      this.tick_Z = false;
      this.run_Z = false;
      this.init_List = List.of();
      this.load_Set.clear();
      this.next = 0;
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.open_Str = null;
      this.load_List = List.of();
      this.prev = -2147483648;
      this.write_J = 0L;
      this.write_F = 0.0F;
   }

   private void valid_V_2() {
      if (this.run_Z) {
         List<aM> dragged = this.init_List;
         boolean apply = this.get_Z && !this.put_Z;
         String targetFolder = this.close_Str;
         String insertBefore = this.open_Str;
         List<String> foldersAbove = this.load_List;
         this.prev_V_4();
         if (apply && !dragged.isEmpty()) {
            aN.init_aN().init(dragged, targetFolder, insertBefore, foldersAbove);
         }
      } else {
         String pressedKey = this.flush_Str;
         boolean wasSelected = this.tick_Z;
         this.prev_V_4();
         if (pressedKey != null) {
            aM pressed = aN.init_aN().init_aM_Str(pressedKey);
            if (pressed != null) {
               if (wasSelected && this.init_LinkedHashSet.size() == 1) {
                  this.reset_V_4();
               } else {
                  this.load_V_aM_2(pressed);
               }
            }
         }
      }
   }

   private void init_V_F(float uiMouseY, int viewX, int viewY, int viewW, int viewH) {
      int maxScroll = this.init_TextFormat_DataUti_I(viewX, viewY, viewW, viewH, 0).open();
      if (maxScroll > 0) {
         int listTop = viewY + 1;
         int listBottom = viewY + viewH - 1;
         int delta = 0;
         if (uiMouseY < listTop + 18) {
            float depth = listTop + 18 - uiMouseY;
            delta = -Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0F)));
         } else if (uiMouseY > listBottom - 18) {
            float depth = uiMouseY - (listBottom - 18);
            delta = Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0F)));
         }

         long now = System.nanoTime();
         float frames = this.write_J == 0L ? 1.0F : Math.min(6.0F, (float)(now - this.write_J) / 1.6666667E7F);
         this.write_J = now;
         if (delta == 0) {
            this.write_F = 0.0F;
         } else {
            float px = delta * frames + this.write_F;
            int whole = (int)px;
            this.write_F = px - whole;
            if (whole != 0) {
               this.init_CacheEntry.init_V_D(-whole, 1.0F, maxScroll);
            }
         }
      }
   }

   private void init_V_TextFormat_DataUti(autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout, float uiMouseX, float uiMouseY) {
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.close_Str = "";
      this.open_Str = null;
      this.load_List = List.of();
      this.prev = -2147483648;
      if (!this.update_List.isEmpty()
         && this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         int lastLocal = this.pop_I_15();
         if (lastLocal >= 0) {
            int index = Math.max(0, (int)Math.floor((double)(uiMouseY - (listLayout.load() + listLayout.pop()) + listLayout.poll()) / listLayout.sync()));
            if (index > lastLocal) {
               this.init_V_TextFormat_DataUti_2(listLayout, lastLocal + 1, lastLocal);
            } else {
               AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(index);
               int rowTop = this.init_I_TextFormat_DataUti_I(listLayout, index);
               if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER) {
                  if (uiMouseY < rowTop + listLayout.update() / 3.0F) {
                     this.init_V_TextFormat_DataUti_2(listLayout, index, lastLocal);
                  } else {
                     this.close_Str = item.init_Str;
                     this.set_Z = true;
                     this.get_Z = true;
                  }
               } else {
                  boolean before = uiMouseY < rowTop + listLayout.update() / 2.0F;
                  String folder = item.init_aM.read == null ? "" : item.init_aM.read;
                  if (folder.isEmpty()) {
                     this.init_V_TextFormat_DataUti_2(listLayout, before ? index : index + 1, lastLocal);
                  } else {
                     this.close_Str = folder;
                     this.prev = before ? rowTop : rowTop + listLayout.sync();
                     this.open_Str = this.init_Str_I_Str(before ? index : index + 1, folder);
                     this.get_Z = true;
                     this.put_Z = this.init_Z_Str_Str_2(folder, this.open_Str);
                  }
               }
            }
         }
      }
   }

   private void init_V_TextFormat_DataUti_2(autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout, int gap, int lastLocal) {
      this.close_Str = "";
      this.prev = gap <= lastLocal
         ? this.init_I_TextFormat_DataUti_I(listLayout, gap)
         : this.init_I_TextFormat_DataUti_I(listLayout, lastLocal) + listLayout.sync();
      String next = null;

      for (int i = gap; i <= lastLocal; i++) {
         AutismMacroListOverlay.Inner_StreamUtil row = this.update_List.get(i);
         if (init_Z_AutismMacroListOve(row) && !this.load_Set.contains(cZ.init(row.init_aM.init_Str))) {
            next = row.init_aM.init_Str;
            break;
         }
      }

      this.open_Str = next;
      List<String> above = new ArrayList<>();

      for (int i = gap - 1; i >= 0; i--) {
         AutismMacroListOverlay.Inner_StreamUtil row = this.update_List.get(i);
         if (init_Z_AutismMacroListOve(row)) {
            if (!this.load_Set.contains(cZ.init(row.init_aM.init_Str))) {
               break;
            }
         } else if (row.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER && row.load == null) {
            above.add(0, row.init_Str);
         }
      }

      this.load_List = List.copyOf(above);
      this.get_Z = true;
      boolean foldersAlreadyThere = true;
      String first = this.init_List.isEmpty() ? null : cZ.init(this.init_List.get(0).init_Str);

      for (String folder : above) {
         if (first == null || !first.equals(aN.init_aN().load_Str(folder))) {
            foldersAlreadyThere = false;
            break;
         }
      }

      this.put_Z = foldersAlreadyThere && this.init_Z_Str_Str_2("", next);
   }

   private static boolean init_Z_AutismMacroListOve(AutismMacroListOverlay.Inner_StreamUtil row) {
      return row.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO
         && row.init_aM != null
         && (row.init_aM.read == null || row.init_aM.read.isEmpty());
   }

   private int pop_I_15() {
      int last = -1;

      for (int i = 0; i < this.update_List.size(); last = i++) {
         AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
         if (item.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO
            && (item.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.FOLDER || item.load != null)) {
            break;
         }
      }

      return last;
   }

   private String init_Str_I_Str(int from, String folder) {
      String folderKey = cY.load_Str_Str(folder);

      for (int i = Math.max(0, from); i < this.update_List.size(); i++) {
         AutismMacroListOverlay.Inner_StreamUtil candidate = this.update_List.get(i);
         if (candidate.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO || candidate.init_aM == null) {
            return null;
         }

         if (!cY.load_Str_Str(candidate.init_aM.read).equals(folderKey)) {
            return null;
         }

         if (!this.load_Set.contains(cZ.init(candidate.init_aM.init_Str))) {
            return candidate.init_aM.init_Str;
         }
      }

      return null;
   }

   private boolean init_Z_Str_Str_2(String folder, String insertBefore) {
      String folderKey = cY.load_Str_Str(folder);
      List<aM> run = new ArrayList<>();

      for (aM macro : aN.init_aN().init_List()) {
         if (macro != null && cY.load_Str_Str(macro.read).equals(folderKey)) {
            run.add(macro);
         }
      }

      int first = -1;
      int last = -1;
      int count = 0;

      for (int i = 0; i < run.size(); i++) {
         if (this.load_Set.contains(cZ.init(run.get(i).init_Str))) {
            if (first < 0) {
               first = i;
            }

            last = i;
            count++;
         }
      }

      if (count == this.load_Set.size() && last - first + 1 == count) {
         String follower = last + 1 < run.size() ? run.get(last + 1).init_Str : null;
         return insertBefore == null ? follower == null : follower != null && cZ.init(follower, insertBefore);
      } else {
         return false;
      }
   }

   private boolean load_Z_Str_3(String folder) {
      return this.write_Str != null && folder != null && !folder.isEmpty() && cY.load_Str_Str(folder).equals(cY.load_Str_Str(this.write_Str));
   }

   private void size_V_2() {
      this.write_Str = null;
      this.poll_Z = false;
      this.push_Z = false;
      this.pop_Z = false;
      this.update_Str = null;
      this.sync_Str = null;
      this.stop = -2147483648;
      this.write_J = 0L;
      this.write_F = 0.0F;
   }

   private void data_V_2() {
      String folder = this.write_Str;
      boolean dragged = this.poll_Z;
      boolean apply = this.push_Z && !this.pop_Z;
      String before = this.update_Str;
      String anchor = this.sync_Str;
      this.size_V_2();
      if (folder != null) {
         if (!dragged) {
            aN.init_aN().init_V_Str(folder);
            this.open_Z = false;
            this.clear_Z = true;
         } else if (apply && aN.init_aN().init(folder, before, anchor)) {
            this.clear_Z = true;
         }
      }
   }

   private void load_V_TextFormat_DataUti(autismclient.gui.f.b.TextFormat.Inner_DataUtil listLayout, float uiMouseX, float uiMouseY) {
      this.push_Z = false;
      this.pop_Z = false;
      this.update_Str = null;
      this.sync_Str = null;
      this.stop = -2147483648;
      if (!this.update_List.isEmpty()
         && this.init_Z_F(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         int lastLocal = this.pop_I_15();
         if (lastLocal >= 0) {
            int index = Math.max(0, (int)Math.floor((double)(uiMouseY - (listLayout.load() + listLayout.pop()) + listLayout.poll()) / listLayout.sync()));
            int gap;
            if (index > lastLocal) {
               gap = lastLocal + 1;
            } else {
               AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(index);
               boolean topHalf = uiMouseY < this.init_I_TextFormat_DataUti_I(listLayout, index) + listLayout.update() / 2.0F;
               if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER) {
                  gap = topHalf ? index : this.read(index) + 1;
               } else if (init_Z_AutismMacroListOve(item)) {
                  gap = topHalf ? index : index + 1;
               } else {
                  gap = this.read(this.load_I_I_4(index)) + 1;
               }
            }

            int own = -1;

            for (int i = 0; i <= lastLocal; i++) {
               AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
               if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER && item.load == null && this.load_Z_Str_3(item.init_Str)) {
                  own = i;
                  break;
               }
            }

            this.push_Z = true;
            this.stop = gap <= lastLocal
               ? this.init_I_TextFormat_DataUti_I(listLayout, gap)
               : this.init_I_TextFormat_DataUti_I(listLayout, lastLocal) + listLayout.sync();
            if (own < 0 || gap != own && gap != this.read(own) + 1) {
               for (int i = gap; i <= lastLocal; i++) {
                  AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(i);
                  if (this.sync_Str == null && init_Z_AutismMacroListOve(item)) {
                     this.sync_Str = item.init_aM.init_Str;
                  }

                  if (this.update_Str == null
                     && item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER
                     && item.load == null
                     && !this.load_Z_Str_3(item.init_Str)) {
                     this.update_Str = item.init_Str;
                  }
               }
            } else {
               this.pop_Z = true;
            }
         }
      }
   }

   private int load_I_I_4(int index) {
      AutismMacroListOverlay.Inner_StreamUtil item = this.update_List.get(index);
      if (item.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.FOLDER && item.init_aM != null) {
         String key = cY.load_Str_Str(item.init_aM.read);

         for (int i = index - 1; i >= 0; i--) {
            AutismMacroListOverlay.Inner_StreamUtil row = this.update_List.get(i);
            if (row.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER && row.load == null && cY.load_Str_Str(row.init_Str).equals(key)) {
               return i;
            }
         }

         return index;
      } else {
         return index;
      }
   }

   private int read(int header) {
      AutismMacroListOverlay.Inner_StreamUtil head = this.update_List.get(header);
      if (head.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.FOLDER) {
         return header;
      }

      String key = cY.load_Str_Str(head.init_Str);
      int end = header;

      for (int i = header + 1; i < this.update_List.size(); end = i++) {
         AutismMacroListOverlay.Inner_StreamUtil row = this.update_List.get(i);
         if (row.init_AutismMacroListOve != AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO
            || row.init_aM == null
            || !cY.load_Str_Str(row.init_aM.read).equals(key)) {
            break;
         }
      }

      return end;
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor context, int x, int y, int width) {
      AutismMacroListOverlay.Inner_StreamUtil folder = null;

      for (AutismMacroListOverlay.Inner_StreamUtil item : this.update_List) {
         if (item.init_AutismMacroListOve == AutismMacroListOverlay.Inner_CachePool.FOLDER && item.load == null && this.load_Z_Str_3(item.init_Str)) {
            folder = item;
            break;
         }
      }

      if (folder != null) {
         autismclient.gui.f.TaskQueue.init_V_F(0.9F);

         try {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, this.get_I_11());
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, this.init_Font, 0, 0);
            autismclient.gui.f.b.TaskQueue.load(ui, bounds, folder.init_cY_DataUtil.init_Str(), folder.init_cY_DataUtil.load_Z(), true);
            autismclient.gui.f.PacketUtil.load(context, bounds, ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.OUTLINE));
         } finally {
            autismclient.gui.f.TaskQueue.init_V();
         }
      }
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor context, aM macro, int x, int y, int width, float alpha) {
      int fill = autismclient.gui.f.c.SyncManager.init(ct.init_I_I_ct_DataUtil(1715082778, ct.Inner_DataUtil.ACCENT), alpha * 0.85F);
      int edge = autismclient.gui.f.c.SyncManager.init(ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.OUTLINE), alpha * 0.85F);
      autismclient.gui.f.b.TableIndex.init(context, x, y, width, this.get_I_11(), fill);
      autismclient.gui.f.PacketUtil.load(context, autismclient.gui.f.NetHelper.init(x, y, width, this.get_I_11()), edge);
      int textLeft = x + this.state() + (macro.read != null && !macro.read.isEmpty() ? 10 : 0) + this.entry() + this.queue();
      int textY = j.init(y, this.get_I_11(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
      int color = autismclient.gui.f.c.SyncManager.init(this.init_CharEncoder.load(autismclient.gui.f.b.l.read), alpha * 0.85F);
      String label = autismclient.gui.f.b.k.init(
         this.init_Font,
         macro.init_Str,
         Math.max(20, x + width - this.state() - textLeft),
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
         color
      );
      autismclient.gui.f.b.k.init(
         context, this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), color, textLeft, textY, false
      );
   }

   private int tick_I_13() {
      long revision = aN.init_aN().init_J();
      if (revision == this.flush_J) {
         return this.data;
      }

      int count = 0;

      for (aM macro : aN.init_aN().init_List()) {
         if (!cL.load(macro)) {
            count++;
         }
      }

      this.flush_J = revision;
      this.data = count;
      return this.data;
   }

   private boolean load_Z_Str_Str(String name, String filter) {
      return filter.isEmpty() || name != null && name.toLowerCase(Locale.ROOT).contains(filter);
   }

   private boolean init_Z_F_F_4(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.tick_I && uiMouseX < this.tick_I + this.get_I && uiMouseY >= this.run_I && uiMouseY < this.run_I + this.init_CharEncoder.load();
   }

   private boolean load_Z_F_F_3(float uiMouseX, float uiMouseY) {
      return autismclient.gui.f.b.ClassCache.init(
         autismclient.gui.f.NetHelper.init(this.tick_I, this.run_I, this.get_I, Math.max(this.init_CharEncoder.load(), this.put_I)),
         this.init_CharEncoder.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private cH init_cH_cH_3(cH bounds) {
      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      int margin = 4;
      int viewportW = Math.round(viewport.init());
      int viewportH = Math.round(viewport.load());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.init_CharEncoder.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.init_I(), availableW), Math.min(bounds.read, availableW));
      int minHeight = bounds.load_Z ? this.init_CharEncoder.load() : this.load_I();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.write, availableH));
      int renderedHeight = bounds.load_Z ? this.init_CharEncoder.load() : height;
      int x = Math.max(margin, Math.min(bounds.init_I, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.load_I, Math.max(margin, viewportH - margin - renderedHeight)));
      return new cH(x, y, width, height, bounds.init_Z, bounds.load_Z);
   }

   private boolean init_Z_F(float x, float y, float width, float height, float mouseX, float mouseY) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private int run_I_13() {
      return this.set_I;
   }

   private int get_I_11() {
      return autismclient.gui.f.RenderState.init(14) + 2;
   }

   private int set_I_11() {
      return this.get_I_11() + 2;
   }

   private int put_I_5() {
      int screenH = Math.round(this.init_TextFormat.init_RingBuffer().load());
      return Math.max(4, (int)(screenH * 0.6F) / this.set_I_11());
   }

   private int add_I_11() {
      return 4;
   }

   private int clear_I_5() {
      return 2;
   }

   private int reset_I_4() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int start_I_10() {
      return 2;
   }

   private int stop_I_4() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int next_I_4() {
      return 12;
   }

   private int prev_I_3() {
      return 10;
   }

   private int valid_I_2() {
      return 3;
   }

   private int size_I_2() {
      return autismclient.gui.f.RenderState.init(13) - 1;
   }

   private int data_I_2() {
      return this.size_I_2();
   }

   private int value() {
      return 2;
   }

   private int cache() {
      return Math.max(0, (this.get_I_11() - this.size_I_2()) / 2);
   }

   private int state() {
      return 4;
   }

   private int queue() {
      return 4;
   }

   private int buffer() {
      return 4;
   }

   private int entry() {
      return 4;
   }

   private enum Inner_BufferHelper {
      TOGGLE_FOLDER,
      RENAME_FOLDER,
      REMOVE_FOLDER,
      MOVE,
      RUN_TOGGLE,
      EDIT,
      COPY,
      DELETE,
      IMPORT_REMOTE;
   }

   private enum Inner_CachePool {
      FOLDER,
      SECTION_HEADER,
      LOCAL_MACRO,
      REMOTE_MACRO;
   }

   private static final class Inner_ConfigStore {
      private final String init_Str;
      private final autismclient.gui.f.c.ByteReader.Inner_StreamUtil init_ByteReader_StreamU;
      private final int init_I;
      private final AutismMacroListOverlay.Inner_BufferHelper init_AutismMacroListOve;
      private final Identifier init_Identifier;

      private Inner_ConfigStore(
         String label, autismclient.gui.f.c.ByteReader.Inner_StreamUtil variant, int width, AutismMacroListOverlay.Inner_BufferHelper action
      ) {
         this(label, variant, width, action, null);
      }

      private Inner_ConfigStore(
         String label, autismclient.gui.f.c.ByteReader.Inner_StreamUtil variant, int width, AutismMacroListOverlay.Inner_BufferHelper action, Identifier icon
      ) {
         this.init_Str = label;
         this.init_ByteReader_StreamU = variant;
         this.init_I = width;
         this.init_AutismMacroListOve = action;
         this.init_Identifier = icon;
      }
   }

   private static final class Inner_DataUtil {
      private final int init_I;
      private final int load;
      private final int read;
      private final int write;
      private final AutismMacroListOverlay.Inner_StreamUtil init_AutismMacroListOve_2;
      private final AutismMacroListOverlay.Inner_BufferHelper init_AutismMacroListOve;

      private Inner_DataUtil(
         int x, int y, int width, int height, AutismMacroListOverlay.Inner_StreamUtil item, AutismMacroListOverlay.Inner_BufferHelper action
      ) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_AutismMacroListOve_2 = item;
         this.init_AutismMacroListOve = action;
      }

      private boolean init(float mouseX, float mouseY) {
         return mouseX >= this.init_I && mouseX <= this.init_I + this.read && mouseY >= this.load && mouseY <= this.load + this.write;
      }
   }

   private static final class Inner_StreamUtil {
      private AutismMacroListOverlay.Inner_CachePool init_AutismMacroListOve;
      private String init_Str;
      private int init_I;
      private aM init_aM;
      private String load;
      private cY.Inner_DataUtil init_cY_DataUtil;

      private static AutismMacroListOverlay.Inner_StreamUtil init(cY.Inner_DataUtil row, String source) {
         AutismMacroListOverlay.Inner_StreamUtil item = new AutismMacroListOverlay.Inner_StreamUtil();
         item.init_AutismMacroListOve = AutismMacroListOverlay.Inner_CachePool.FOLDER;
         item.init_Str = row.load_Str();
         item.init_cY_DataUtil = row;
         item.load = source;
         return item;
      }

      private static AutismMacroListOverlay.Inner_StreamUtil init(String label, int color) {
         AutismMacroListOverlay.Inner_StreamUtil item = new AutismMacroListOverlay.Inner_StreamUtil();
         item.init_AutismMacroListOve = AutismMacroListOverlay.Inner_CachePool.SECTION_HEADER;
         item.init_Str = label;
         item.init_I = color;
         return item;
      }

      private static AutismMacroListOverlay.Inner_StreamUtil init(aM macro) {
         AutismMacroListOverlay.Inner_StreamUtil item = new AutismMacroListOverlay.Inner_StreamUtil();
         item.init_AutismMacroListOve = AutismMacroListOverlay.Inner_CachePool.LOCAL_MACRO;
         item.init_aM = macro;
         return item;
      }

      private static AutismMacroListOverlay.Inner_StreamUtil init(String label, String source) {
         AutismMacroListOverlay.Inner_StreamUtil item = new AutismMacroListOverlay.Inner_StreamUtil();
         item.init_AutismMacroListOve = AutismMacroListOverlay.Inner_CachePool.REMOTE_MACRO;
         item.init_Str = label;
         item.load = source;
         return item;
      }
   }
}
