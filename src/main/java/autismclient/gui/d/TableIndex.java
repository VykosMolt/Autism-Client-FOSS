package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.cA;
import autismclient.util.ct;
import autismclient.util.k;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class TableIndex extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read_I = 4;
   private static final int write_I = 300;
   private static final int update_I = 2;
   private static final int sync_I = 4;
   private static final int flush_I = 12;
   private static final int close_I = 12;
   private static final int open_I = 13;
   private static final String init_Str = "Delay between bots";
   private static final int poll_I = 66;
   private static final Identifier init_Identifier = Identifier.withDefaultNamespace("hud/heart/full");
   private static final Identifier load_Identifier = Identifier.withDefaultNamespace("hud/food_full");
   private static final int push_I = -11013497;
   private static final int pop_I = -866997;
   private static final int tick_I = -42406;
   private static final int run_I = 12;
   private static final DateTimeFormatter init_DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
   private final Screen init_Screen;
   private final List<TableIndex.Inner_StreamUtil> init_List = new ArrayList<>();
   private final List<int[]> load_List = new ArrayList<>();
   private final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private String load_Str;
   private final List<TableIndex.Inner_DataUtil> read_List = new ArrayList<>();
   private List<autismclient.gui.b.CachePool.Inner_DataUtil> write_List = List.of();
   private long init_J = -9223372036854775808L;
   private String read_Str = null;
   private int get = -1;
   private int set = -1;
   private int put;
   private int add;
   private int clear;
   private int reset;
   private int start;
   private int stop;
   private final autismclient.gui.b.BufferHelper init_BufferHelper = new autismclient.gui.b.BufferHelper();
   private boolean init_Z;
   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil;
   private boolean load_Z;
   private int next;
   private int prev;
   private boolean read_Z;
   private String write_Str;
   private boolean write_Z;
   private String update_Str = "";
   private List<autismclient.util.g.NodeWalker.Inner_DataUtil> update_List = List.of();
   private final List<int[]> sync_List = new ArrayList<>();
   private int valid = -1;
   private int[] init_IArr;
   private int[] load_IArr;
   private int[] read_IArr;
   private String sync_Str;
   private int[] write_IArr;
   private int[] update_IArr;
   private int[] sync_IArr;
   private boolean update_Z;
   private int size;
   private autismclient.util.g.ClassCache.Inner_ByteReader init_ClassCache_ByteRea;
   private String flush_Str;
   private long load_J = -9223372036854775808L;
   private final List<int[]> flush_List = new ArrayList<>();
   private final List<autismclient.gui.b.JsonUtil.Inner_TaskQueue> close_List = new ArrayList<>();
   private final autismclient.gui.b.NetHelper init_NetHelper = new autismclient.gui.b.NetHelper();
   private final Map<String, autismclient.util.g.ClassCache.Inner_FieldCache> init_Map = new HashMap<>();
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int data = -1;
   private int value;
   private int cache;
   private int state;
   private int queue;
   private int buffer;
   private int entry;
   private int node;
   private EditBox init_EditBox;
   private k init_k;
   private boolean sync_Z;
   private boolean flush_Z;
   private int task;
   private long read_J = -9223372036854775808L;
   private int pool = -1;
   private String close_Str = "";
   private final List<String> open_List = new ArrayList<>();
   private int index;
   private int count;
   private int flags;
   private String open_Str;
   private String poll_Str;
   private boolean close_Z;
   private boolean open_Z;
   private String push_Str;
   private String pop_Str;
   private long write_J;
   private final List<int[]> poll_List = new ArrayList<>();
   private long update_J = -9223372036854775808L;
   private long sync_J = -9223372036854775808L;
   private String tick_Str = "";
   private int level = ValueSink.close;
   private String run_Str = "";

   public TableIndex(Screen parent) {
      super(Component.literal("Multi Console"));
      this.init_Screen = parent;
      this.run_Str = autismclient.util.g.PacketUtil.init_PacketUtil().write_Str();
   }

   @Override
   protected void init() {
      this.load_V_21();
   }

   private void load_V_21() {
      String chatValue = this.init_EditBox == null ? "" : this.init_EditBox.getValue();
      boolean restoreChatFocus = this.init_EditBox != null && this.init_EditBox.isFocused();
      int chatCursor = this.init_EditBox == null ? chatValue.length() : this.init_EditBox.getCursorPosition();
      this.clearWidgets();
      int sessionW = this.start_I_2();
      int consoleX = this.stop_I_2();
      int consoleW = this.next_I_2();
      int sendW = this.init_I_Str_17("Send");
      int ctl = update_I_11();
      int footerY = this.pop_I_4();
      this.init_EditBox = new StateCache(this.font, consoleX, footerY, Math.max(10, consoleW - sendW - 2), ctl, Component.literal("Chat or command"));
      this.init_EditBox.setMaxLength(256);
      this.init_EditBox.setHint(Component.literal("Chat or /command"));
      this.init_EditBox.setResponder(value -> this.start_V_2());
      this.init_EditBox.setValue(chatValue);
      this.init_EditBox.setCursorPosition(Math.max(0, Math.min(chatCursor, chatValue.length())));
      this.addRenderableWidget(this.init_EditBox);
      if (restoreChatFocus) {
         this.init_EditBox.setFocused(true);
         this.setFocused(this.init_EditBox);
      }

      this.init_StreamGate_I(
         consoleX + consoleW - sendW, footerY, sendW, ctl, "Send", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, button -> this.reset_V_2()
      );
      int innerX = consoleX + 4;
      int innerW = consoleW - 8;
      this.init_V_I(innerX, this.init_I_I_14(0), innerW);
      this.load(innerX, this.init_I_I_14(1), innerW);
      this.read(innerX, this.init_I_I_14(2), innerW);
      if (!this.init_Z_11()) {
         this.write(innerX, this.init_I_I_14(3), innerW);
      }

      int headerBtnY = this.close_I_6() + (sync_I_9() - ctl) / 2;
      if (this.init_Z_11()) {
         this.init_StreamGate_I(
            this.set_I_4(),
            headerBtnY,
            this.init_I_Str_17("Close GUI"),
            ctl,
            "Close GUI",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            button -> this.push_V_4()
         );
      }

      int detailsW = Math.min(sessionW - 8, this.init_I_Str_17(this.read_Z ? "Details On" : "Details"));
      this.load_IArr = new int[]{6 + sessionW - detailsW - 4, headerBtnY, detailsW, ctl};
      this.init_StreamGate_I(
         6 + sessionW - detailsW - 4,
         headerBtnY,
         detailsW,
         ctl,
         this.read_Z ? "Details On" : "Details",
         this.read_Z ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         button -> this.run_V_4()
      );
      String sharedLabel = this.write_Z ? "Shared GUI: On" : "Shared GUI";
      int sharedW = Math.min(sessionW - 8, this.init_I_Str_17(sharedLabel));
      this.read_IArr = new int[]{10, this.poll_I_4(), sharedW, ctl};
      this.init_StreamGate_I(
         10,
         this.poll_I_4(),
         sharedW,
         ctl,
         sharedLabel,
         this.write_Z ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
         button -> this.pop_V_4()
      );
      int footerGap = 2;
      int disconnectW = this.init_I_Str_17("Disconnect");
      int retryW = this.init_I_Str_17("Retry All");
      int profilesW = this.init_I_Str_17("Profiles");
      if (disconnectW + retryW + profilesW + footerGap * 2 > sessionW) {
         disconnectW = retryW = Math.max(1, (sessionW - footerGap * 2) / 3);
         profilesW = sessionW - (disconnectW + footerGap) * 2;
      }

      this.init_StreamGate_I(6, footerY, disconnectW, ctl, "Disconnect", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, button -> {
         autismclient.util.g.PacketUtil.init_PacketUtil().load_V_Str("Disconnected by user");
         this.add_V_3();
      });
      this.init_StreamGate_I(
         6 + disconnectW + footerGap, footerY, retryW, ctl, "Retry All", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, button -> this.put_V_3()
      );
      this.init_StreamGate_I(
         6 + disconnectW + footerGap + retryW + footerGap,
         footerY,
         profilesW,
         ctl,
         "Profiles",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         button -> this.add_V_3()
      );
      this.get_V_4();
      this.update_J = autismclient.util.g.PacketUtil.init_PacketUtil().load_J();
      this.sync_J = autismclient.util.g.PacketUtil.init_PacketUtil().init_J();
   }

   private void init_V_I(int x, int y, int width) {
      autismclient.util.g.FieldCache profile = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
      if (profile != null) {
         this.load_List.clear();
         int gap = 2;
         int ctl = update_I_11();
         int count = 5;
         String[] labels = new String[count];
         labels[0] = "Move";

         for (int i = 0; i < 4; i++) {
            autismclient.util.g.ListPool action = profile.init_ListPool_I(i);
            labels[i + 1] = action.init_Z() ? "Empty" : action.init_Str_I(i);
         }

         int avail = width - gap * (count - 1);
         int each = Math.max(1, avail / count);
         int[] contentW = new int[count];
         int contentTotal = gap * (count - 1);

         for (int i = 0; i < count; i++) {
            contentTotal += contentW[i] = this.init_I_Str_17(labels[i]);
         }

         boolean fits = contentTotal <= width;
         int cx = x;
         int moveW = fits ? contentW[0] : each;
         this.init_StreamGate_I(cx, y, moveW, ctl, "Move", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, button -> this.stop_V_2());
         cx += moveW + gap;

         for (int i = 0; i < 4; i++) {
            int index = i;
            autismclient.util.g.ListPool action = profile.init_ListPool_I(i);
            autismclient.gui.f.b.CachePool.Inner_DataUtil tone = action.init_Z()
               ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init
               : autismclient.gui.f.b.CachePool.Inner_DataUtil.read;
            int cw = fits ? contentW[i + 1] : (i == 3 ? x + width - cx : each);
            this.init_StreamGate_I(cx, y, cw, ctl, labels[i + 1], tone, button -> this.load_V_I(index));
            this.load_List.add(new int[]{cx, y, cw, ctl, index});
            cx += cw + gap;
         }
      }
   }

   private void load(int x, int y, int width) {
      int gap = 2;
      int half = Math.max(1, (width - gap) / 2);
      int resetW = Math.min(half, this.init_I_Str_17("Reset presets"));
      int advancedW = Math.min(Math.max(1, width - resetW - gap), this.init_I_Str_17("Advanced"));
      this.init_StreamGate_I(x, y, resetW, update_I_11(), "Reset presets", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, button -> this.next_V_2());
      this.init_StreamGate_I(
         x + resetW + gap, y, advancedW, update_I_11(), "Advanced", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, button -> this.prev_V_2()
      );
   }

   private void read(int x, int y, int width) {
      String[] labels = new String[]{"Use", "GUI", "Close", "Close W/O Pkt"};
      int gap = 2;
      int ctl = update_I_11();
      int count = labels.length;
      int avail = width - gap * (count - 1);
      int each = Math.max(1, avail / count);
      int cx = x;
      Runnable[] actions = new Runnable[]{this::flush_V_8, this::poll_V_4, this::close_V_6, this::open_V_5};

      for (int i = 0; i < count; i++) {
         Runnable action = actions[i];
         int cw = Math.min(i == count - 1 ? x + width - cx : each, this.init_I_Str_17(labels[i]));
         this.init_StreamGate_I(cx, y, cw, ctl, labels[i], autismclient.gui.f.b.CachePool.Inner_DataUtil.init, button -> action.run());
         if (i == count - 1) {
            this.init_IArr = new int[]{cx, y, cw, ctl};
         }

         cx += cw + gap;
      }
   }

   private void write(int x, int y, int width) {
      int gap = 2;
      int ctl = update_I_11();
      int sideMax = Math.max(24, Math.min(60, Math.max(1, (width - 3 * gap) / 5)));
      int runW = Math.min(sideMax, this.init_I_Str_17("Run"));
      int stopW = Math.min(sideMax, this.init_I_Str_17("Stop"));
      int assignW = Math.min(sideMax, this.init_I_Str_17("Assign"));
      int macroX = x + runW + gap + stopW + gap + assignW + gap;
      int macroW = Math.max(1, x + width - macroX);
      this.init_StreamGate_I(x, y, runW, ctl, "Run", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, button -> this.update_V_11());
      this.init_StreamGate_I(x + runW + gap, y, stopW, ctl, "Stop", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, button -> this.sync_V_9());
      this.init_StreamGate_I(
         x + runW + gap + stopW + gap, y, assignW, ctl, "Assign", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, button -> this.write_V_12()
      );
      StreamGate picker = new StreamGate(
         macroX,
         y,
         macroW,
         ctl,
         Component.literal("Macro (" + this.init_Str_9() + ")"),
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         () -> this.init_Str_Str_I_2("Macro (" + this.init_Str_9() + ")", macroW - 8),
         button -> this.read_V_14()
      );
      this.addRenderableWidget(picker);
   }

   private String init_Str_9() {
      return load_Str_Str_7(this.run_Str);
   }

   private static String load_Str_Str_7(String name) {
      return name != null && !name.isBlank() ? name : "none";
   }

   private void read_V_14() {
      this.minecraft.gui.setScreen(new BitSetUtil(this, this.run_Str, name -> {
         this.run_Str = name == null ? "" : name;
         this.tick_Str = this.run_Str.isBlank() ? "No macro chosen" : "Chose \"" + this.run_Str + "\"";
         this.level = ValueSink.open;
      }));
   }

   private void write_V_12() {
      if (this.run_Str.isBlank()) {
         this.tick_Str = "Choose a macro first (Macro button)";
         this.level = ValueSink.close;
      } else {
         this.minecraft.gui.setScreen(new IoWorker(this, this.run_Str, this.init_LinkedHashSet));
      }
   }

   private void update_V_11() {
      autismclient.util.g.PacketUtil m = autismclient.util.g.PacketUtil.init_PacketUtil();
      if (m.init_Z_Set(this.load_Set())) {
         this.init_V_PacketUtil_CachePo(m.read_PacketUtil_CachePo_Set(this.load_Set()));
      } else if (!this.run_Str.isBlank()) {
         aM macro = aN.init_aN().init_aM_Str(this.run_Str);
         if (macro != null) {
            this.init_V_PacketUtil_CachePo(m.load(macro, this.load_Set()));
         } else {
            this.tick_Str = "Macro not found";
            this.level = ValueSink.poll;
         }
      } else {
         this.tick_Str = "Choose or assign a macro first";
         this.level = ValueSink.close;
      }
   }

   private void sync_V_9() {
      this.init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.init_PacketUtil().write_PacketUtil_CachePo_Set(this.load_Set()));
   }

   private void flush_V_8() {
      this.init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.init_PacketUtil().sync_PacketUtil_CachePo_Set(this.load_Set()));
   }

   private void close_V_6() {
      this.init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.init_PacketUtil().flush_PacketUtil_CachePo_Set(this.load_Set()));
   }

   private void open_V_5() {
      this.init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.init_PacketUtil().close_PacketUtil_CachePo_Set(this.load_Set()));
   }

   private void poll_V_4() {
      if (this.init_LinkedHashSet.size() != 1) {
         this.tick_Str = "Select one bot";
         this.level = ValueSink.close;
      } else {
         this.init_V_Str_8(this.init_LinkedHashSet.iterator().next());
      }
   }

   private void init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      this.tick_Str = init_Str_PacketUtil_CachePo_2(result);
      this.level = result.read() > 0 ? ValueSink.poll : (result.load() > 0 ? ValueSink.close : ValueSink.open);
   }

   private void init_V_Str_8(String accountId) {
      this.write_Str = accountId;
      this.write_Z = false;
      this.size = 0;
      this.put = 0;
      this.flush_Str = null;
      this.load_V_21();
   }

   private void push_V_4() {
      this.write_Str = null;
      this.write_Z = false;
      this.put = 0;
      this.init_ClassCache_ByteRea = null;
      this.flush_Str = null;
      this.load_V_21();
   }

   private void pop_V_4() {
      this.write_Z = !this.write_Z;
      this.write_Str = null;
      this.size = 0;
      this.put = 0;
      this.init_ClassCache_ByteRea = null;
      this.flush_Str = null;
      this.load_V_21();
   }

   private boolean init_Z_11() {
      return this.write_Str != null || this.write_Z;
   }

   private String load_Str_6() {
      if (!this.write_Z) {
         return this.write_Str;
      }

      autismclient.util.g.NodeWalker.Inner_DataUtil group = autismclient.util.g.NodeWalker.init(this.update_List, this.update_Str);
      return group == null ? null : group.init_Str();
   }

   private List<String> init_List_11() {
      if (!this.write_Z) {
         return List.of();
      }

      autismclient.util.g.NodeWalker.Inner_DataUtil group = autismclient.util.g.NodeWalker.init(this.update_List, this.update_Str);
      return group == null ? List.of() : group.init_List();
   }

   private void load_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      if (result != null) {
         int missed = result.read() + result.load();
         if (result.init_I() <= 0 || missed != 0) {
            this.tick_Str = result.init_I() == 0 ? "No bot took the click" : "Sent " + result.init_I() + ", missed " + missed;
            this.level = result.init_I() == 0 ? ValueSink.poll : ValueSink.close;
         }
      }
   }

   private void run_V_4() {
      this.read_Z = !this.read_Z;
      this.task = 0;
      this.load_V_21();
   }

   private void get_V_4() {
      this.init_List.clear();
      List<autismclient.util.g.ClassCache.Inner_FieldCache> sessions = autismclient.util.g.PacketUtil.init_PacketUtil().load_List();
      this.read_J = init_J_List(sessions);
      Set<String> present = new HashSet<>();

      for (autismclient.util.g.ClassCache.Inner_FieldCache s : sessions) {
         present.add(s.init_Str());
      }

      this.init_LinkedHashSet.retainAll(present);
      if (this.load_Str != null && !present.contains(this.load_Str)) {
         this.load_Str = null;
      }

      int top = this.push_I_4();
      int bottom = this.run_I_4();
      int rowPitch = this.read_I_14() + 2 + 12;
      int viewH = Math.max(1, bottom - top);
      int maxScroll = 0;
      int tail = 0;

      for (int i = sessions.size() - 1; i >= 0; i--) {
         tail += rowPitch + init_I_ClassCache_FieldCa(sessions.get(i));
         if (tail >= viewH) {
            maxScroll = i;
            break;
         }
      }

      this.task = Math.max(0, Math.min(this.task, maxScroll));
      int y = top;

      for (int i = this.task; i < sessions.size(); i++) {
         int extra = init_I_ClassCache_FieldCa(sessions.get(i));
         if (!this.init_List.isEmpty() && y + rowPitch + extra > bottom) {
            break;
         }

         this.init_List.add(new TableIndex.Inner_StreamUtil(sessions.get(i).init_Str(), y, extra));
         y += rowPitch + extra;
      }
   }

   private static long init_J_List(List<autismclient.util.g.ClassCache.Inner_FieldCache> sessions) {
      long hash = sessions.size();

      for (autismclient.util.g.ClassCache.Inner_FieldCache snapshot : sessions) {
         hash = 31L * hash + snapshot.init_Str().hashCode();
         hash = 31L * hash + (init_I_ClassCache_FieldCa(snapshot) > 0 ? 1 : 0);
      }

      return hash;
   }

   private void set_V_4() {
      List<autismclient.util.g.ClassCache.Inner_FieldCache> snapshots = autismclient.util.g.PacketUtil.init_PacketUtil().load_List();
      if (init_J_List(snapshots) != this.read_J) {
         this.get_V_4();
      }
   }

   private static int init_I_ClassCache_FieldCa(autismclient.util.g.ClassCache.Inner_FieldCache snapshot) {
      String gui = snapshot.sync();
      return snapshot.read_Z() || gui != null && !gui.isBlank() ? 12 : 0;
   }

   private int read_I_14() {
      return this.read_Z ? write_I_12() + 4 * flush_I_8() + 2 : write_I_12();
   }

   private static int write_I_12() {
      return update_I_11() + 2;
   }

   private static int update_I_11() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int sync_I_9() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int flush_I_8() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int init_I_I_I_9(int y, int h) {
      return j.init(y, h, autismclient.gui.f.b.k.init(init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private int close_I_6() {
      return 10;
   }

   private int open_I_5() {
      return this.close_I_6() + sync_I_9();
   }

   private int poll_I_4() {
      return this.open_I_5() + flush_I_8() + 2;
   }

   private int push_I_4() {
      return this.poll_I_4() + update_I_11() + 4;
   }

   private int pop_I_4() {
      return this.load_I() - 6 - update_I_11();
   }

   private int tick_I_14() {
      return this.pop_I_4() - 4;
   }

   private int run_I_4() {
      return this.tick_I_14() - 4;
   }

   private int init_I_I_14(int row) {
      return this.close_I_6() + sync_I_9() + 2 + row * (update_I_11() + 2);
   }

   private int get_I_4() {
      return this.tick_I_14() - 4 - flush_I_8();
   }

   private int set_I_4() {
      return this.stop_I_2() + this.next_I_2() - 4 - this.init_I_Str_17("Close GUI");
   }

   private int put_I_3() {
      return Math.max(this.init_I_Str_17("GUI"), this.init_I_Str_17("POV"));
   }

   private void load_V_Str_2(String id) {
      autismclient.util.g.ClassCache.Inner_FieldCache current = this.init_ClassCache_FieldCa_Str(id);
      if (current != null && !autismclient.util.g.PacketUtil.init_Z_ClassCache_PathHel(current.init_ClassCache_PathHel())) {
         autismclient.util.g.PacketUtil.init_PacketUtil().init_V_Str(id);
         this.tick_Str = "Stopped";
         this.level = ValueSink.poll;
      } else {
         autismclient.util.g.PacketUtil.Inner_EventBus result = autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_EventBu_Str(id);
         this.tick_Str = autismclient.util.g.PacketUtil.update(result.init_Str(), 40);
         this.level = result.init_Z() ? ValueSink.open : ValueSink.poll;
      }
   }

   private void put_V_3() {
      autismclient.util.g.PacketUtil.Inner_EventBus result = autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_EventBu();
      this.tick_Str = autismclient.util.g.PacketUtil.update(result.init_Str(), 40);
      this.level = result.init_Z() ? ValueSink.open : ValueSink.close;
   }

   private void add_V_3() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new PathHelper(this.init_Screen, "", true));
      }
   }

   private int add_I_3() {
      return 6 + this.start_I_2() - this.clear_I_3() - 4;
   }

   private int clear_I_3() {
      return Math.max(this.init_I_Str_17("Retry"), this.init_I_Str_17("Stop"));
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      this.sync_Str = null;
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      int sessionW = this.start_I_2();
      int actionW = this.clear_I_3();

      for (TableIndex.Inner_StreamUtil row : this.init_List) {
         autismclient.util.g.ClassCache.Inner_FieldCache snapshot = this.init_ClassCache_FieldCa_Str(row.init_Str());
         if (snapshot != null) {
            int color = load_I_ClassCache_FieldCa(snapshot);
            boolean selected = this.init_LinkedHashSet.contains(row.init_Str());
            int selectionColor = -39220;
            int frameH = this.read_I_14() + 12 + row.load();
            int frameColor = selected ? selectionColor : color;
            int fill = frameColor & 16777215 | (selected ? 1140850688 : 520093696);
            autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(10, row.init_I(), sessionW - 8, frameH), 4, fill);
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(10, row.init_I(), 3, frameH), frameColor);
            int actionX = this.add_I_3();
            int btnY = row.init_I() + 1;
            int btnH = update_I_11();
            int lineTextY = this.init_I_I_I_9(row.init_I(), write_I_12());
            int toggleW = this.put_I_3();
            boolean showView = sessionW >= 220;
            int viewX = showView ? actionX - 4 - toggleW : actionX;
            boolean viewingThis = row.init_Str().equals(this.write_Str);
            int viewBorder = ct.init_I_I_ct_DataUtil(-1721357268, ct.Inner_DataUtil.load);
            int viewFill = viewingThis ? 1432015210 : 855638016;
            if (showView) {
               autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(viewX, btnY, toggleW, btnH), 4, viewFill, viewBorder);
               if (viewingThis) {
                  autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(viewX + 1, btnY + btnH - 2, toggleW - 2, 1), -10825366);
               }

               int labelColor = viewingThis ? -10825366 : -6641998;
               String viewLabelFit = autismclient.gui.f.b.k.load(this.font, "GUI", toggleW - 4, fontId, labelColor);
               int viewLabelW = autismclient.gui.f.b.k.init(this.font, viewLabelFit, fontId, labelColor);
               autismclient.gui.f.b.k.init(
                  graphics, this.font, viewLabelFit, fontId, labelColor, viewX + Math.max(2, (toggleW - viewLabelW) / 2), lineTextY, false
               );
            }

            boolean povThis = autismclient.util.g.ValueSink.init_Z(row.init_Str());
            boolean povOk = povThis || autismclient.util.g.ValueSink.load_Z(row.init_Str());
            boolean showPov = showView && povOk;
            int povX = showPov ? viewX - 4 - toggleW : viewX;
            if (showPov) {
               int povFill = povThis ? 1432015210 : 855638016;
               autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(povX, btnY, toggleW, btnH), 4, povFill, viewBorder);
               if (povThis) {
                  autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(povX + 1, btnY + btnH - 2, toggleW - 2, 1), -10825366);
               }

               int labelColor = povThis ? -10825366 : -6641998;
               String povFit = autismclient.gui.f.b.k.load(this.font, "POV", toggleW - 4, fontId, labelColor);
               int povLabelW = autismclient.gui.f.b.k.init(this.font, povFit, fontId, labelColor);
               autismclient.gui.f.b.k.init(graphics, this.font, povFit, fontId, labelColor, povX + Math.max(2, (toggleW - povLabelW) / 2), lineTextY, false);
            }

            String ping = snapshot.init_I() >= 0 ? snapshot.init_I() + "ms" : "--";
            int pingWidth = autismclient.gui.f.b.k.init(this.font, ping, fontId, color);
            int pingX = (showView ? povX : actionX) - 4 - pingWidth;
            autismclient.gui.f.b.k.init(graphics, this.font, ping, fontId, color, pingX, lineTextY, false);
            int rightEdge = pingX;
            int macroY = row.init_I() + write_I_12();
            this.init(graphics, fontId, snapshot, 18, this.init_I_I_I_9(macroY, 12), sessionW - 24);
            if (mouseX >= 10 && mouseX < 6 + sessionW - 4 && mouseY >= macroY && mouseY < macroY + 12) {
               this.sync_Str = autismclient.gui.b.ConfigStore.load(autismclient.util.g.PacketUtil.init_PacketUtil(), snapshot);
            }

            if (row.load() > 0) {
               String gui = snapshot.sync();
               String guiLabel = snapshot.read_Z() ? "GUI: CustomScreen" : "GUI: " + autismclient.util.g.PacketUtil.update(gui == null ? "" : gui, 48);
               autismclient.gui.f.b.k.init(
                  graphics,
                  this.font,
                  autismclient.gui.f.b.k.load(this.font, guiLabel, sessionW - 24, fontId, cache_I()),
                  fontId,
                  cache_I(),
                  18,
                  this.init_I_I_I_9(macroY + 12, 12),
                  false
               );
            }

            int nameX = 18;
            int nameMax = Math.max(1, rightEdge - nameX - 4);
            int nameColor = color == -42406 ? -42406 : value_I();
            String name = autismclient.gui.f.b.k.load(this.font, autismclient.util.g.PacketUtil.update(snapshot.load_Str(), 48), nameMax, fontId, nameColor);
            autismclient.gui.f.b.k.init(graphics, this.font, name, fontId, nameColor, nameX, lineTextY, false);
            String actionLabel = autismclient.util.g.PacketUtil.init_Z_ClassCache_PathHel(snapshot.init_ClassCache_PathHel()) ? "Retry" : "Stop";
            int actionFill = color & 16777215 | 1493172224;
            autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(actionX, btnY, actionW, btnH), 4, actionFill, viewBorder);
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(actionX + 1, btnY + btnH - 2, actionW - 2, 1), color);
            int labelWidth = autismclient.gui.f.b.k.init(this.font, actionLabel, fontId, color);
            autismclient.gui.f.b.k.init(graphics, this.font, actionLabel, fontId, color, actionX + Math.max(2, (actionW - labelWidth) / 2), lineTextY, false);
            if (this.read_Z) {
               int dx = 18;
               int dw = sessionW - 24;
               int line = flush_I_8();
               int dy = row.init_I() + write_I_12() + 12 + row.load();
               this.init(graphics, fontId, snapshot, dx, this.init_I_I_I_9(dy, line));
               String held = snapshot.flush() != null && !snapshot.flush().isBlank() ? snapshot.flush() : "empty";
               this.init(graphics, fontId, "Held: " + held + "   Slot " + snapshot.load_I(), dx, this.init_I_I_I_9(dy + line, line), dw);
               String dim = snapshot.close() != null && !snapshot.close().isBlank() ? snapshot.close() : "?";
               this.init(graphics, fontId, "World: " + dim, dx, this.init_I_I_I_9(dy + line * 2, line), dw);
               String pos = snapshot.write_Z()
                  ? String.format(Locale.ROOT, "Pos: %.1f  %.1f  %.1f", snapshot.init_D(), snapshot.load_D(), snapshot.read_D())
                  : "Pos: --";
               this.init(graphics, fontId, pos, dx, this.init_I_I_I_9(dy + line * 3, line), dw);
            }
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, Identifier fontId, autismclient.util.g.ClassCache.Inner_FieldCache snapshot, int x, int y, int width) {
      autismclient.util.g.PacketUtil manager = autismclient.util.g.PacketUtil.init_PacketUtil();
      String assignedName = autismclient.gui.b.ConfigStore.init(manager, snapshot);
      String assigned = autismclient.gui.b.ConfigStore.init(assignedName);
      long now = System.currentTimeMillis();
      String trailing = autismclient.gui.b.ConfigStore.init_Str(snapshot, now);
      int trailingColor = -1526710;
      if (trailing.isBlank()) {
         trailing = autismclient.gui.b.ConfigStore.load(autismclient.gui.b.ConfigStore.init_Str(snapshot));
         trailingColor = -11013497;
      }

      if (trailing.isBlank()) {
         autismclient.gui.f.b.k.init(
            graphics, this.font, autismclient.gui.f.b.k.load(this.font, assigned, width, fontId, -6641998), fontId, -6641998, x, y, false
         );
      } else {
         int trailingW = Math.min(width, autismclient.gui.f.b.k.init(this.font, trailing, fontId, trailingColor));
         int gap = 7;
         int assignedW = Math.max(0, width - trailingW - gap);
         int cx = x;
         if (assignedW > 12) {
            String fit = autismclient.gui.f.b.k.load(this.font, assigned, assignedW, fontId, -6641998);
            autismclient.gui.f.b.k.init(graphics, this.font, fit, fontId, -6641998, cx, y, false);
            cx += assignedW + gap;
         }

         String fitTrailing = autismclient.gui.f.b.k.load(this.font, trailing, Math.max(1, x + width - cx), fontId, trailingColor);
         autismclient.gui.f.b.k.init(graphics, this.font, fitTrailing, fontId, trailingColor, cx, y, false);
      }
   }

   private static boolean init_Z_IArr(int[] rect, int mouseX, int mouseY) {
      return rect != null && autismclient.gui.b.UiHelper.init(rect[0], rect[1], rect[2], rect[3], mouseX, mouseY);
   }

   private int[] init_IArr_I_I(int x, int width) {
      int gap = 4;
      int unitW = autismclient.gui.f.b.k.init(this.font, "s", init_CharEncoder.init_Identifier_l(l.read), cache_I()) + 2;
      int labelW = Math.min(width / 2, autismclient.gui.f.b.k.init(this.font, "Delay between bots", init_CharEncoder.init_Identifier_l(l.read), cache_I()) + 2);
      int boxW = Math.min(34, width - labelW - gap * 2 - unitW - 24);
      int trackX = x + labelW + gap;
      int trackW = (boxW >= 18 ? x + width - unitW - boxW - gap : x + width) - trackX;
      if (trackW < 12) {
         return new int[]{x, Math.max(1, width), 0, 0};
      } else {
         return boxW >= 18 ? new int[]{trackX, trackW, x + width - unitW - boxW, boxW} : new int[]{trackX, trackW, 0, 0};
      }
   }

   private k init_k() {
      if (this.init_k == null) {
         this.init_k = new k(this.minecraft, this.font, 0, 0, 10, 13, false);
         this.init_k.load(4);
         this.init_k.load(autismclient.util.g.RenderState::init_Z_Str);
         this.init_k.init_V_Component(Component.literal("0"));
         this.init_k.init_V_Consumer(this::read_V_2);
      }

      return this.init_k;
   }

   private void read_V_2(String text) {
      if (!this.flush_Z) {
         autismclient.util.g.RenderState.init_V_I(autismclient.util.g.RenderState.init(text, autismclient.util.g.RenderState.init_I()));
      }
   }

   private void init_V_I_9(int direction) {
      autismclient.util.g.RenderState.load_V(direction);
      this.write(autismclient.util.g.RenderState.init_Str_I(autismclient.util.g.RenderState.init_I()));
   }

   private void write(String want) {
      if (this.init_k != null && !this.init_k.load_Str().equals(want)) {
         this.flush_Z = true;

         try {
            this.init_k.init_V_Str(want);
         } finally {
            this.flush_Z = false;
         }
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, int x, int y, int width, int mouseX, int mouseY, float delta) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      int delayMs = autismclient.util.g.RenderState.init_I();
      int[] layout = this.init_IArr_I_I(x, width);
      int trackX = layout[0];
      int trackW = layout[1];
      int boxX = layout[2];
      int boxW = layout[3];
      if (trackX > x) {
         autismclient.gui.f.b.k.init(graphics, this.font, "Delay between bots", fontId, cache_I(), x, this.init_I_I_I_9(y, 13), false);
      }

      boolean hovered = this.update_Z || mouseX >= trackX && mouseX < trackX + trackW && mouseY >= y && mouseY < y + 13;
      autismclient.gui.f.b.c.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(trackX, y, trackW, 13),
         autismclient.util.g.RenderState.init_D_I(delayMs),
         hovered
      );
      this.write_IArr = new int[]{trackX, y, trackW, 13};
      this.update_IArr = new int[]{x, y, width, 13};
      if (boxW <= 0) {
         this.sync_IArr = null;
         this.clear_V_3();
      } else {
         k box = this.init_k();
         box.read(boxX);
         box.write(y);
         box.update(boxW);
         box.sync(13);
         this.sync_IArr = new int[]{boxX, y, boxW, 13};
         if (!box.init_Z()) {
            this.write(autismclient.util.g.RenderState.init_Str_I(delayMs));
         }

         box.init_V(graphics, mouseX, mouseY, delta);
         autismclient.gui.f.b.k.init(graphics, this.font, "s", fontId, cache_I(), boxX + boxW + 2, this.init_I_I_I_9(y, 13), false);
         boolean focused = box.init_Z();
         if (this.sync_Z && !focused) {
            autismclient.util.g.RenderState.init_V();
         }

         this.sync_Z = focused;
      }
   }

   private void clear_V_3() {
      if (this.init_k != null && this.init_k.init_Z()) {
         this.init_k.push(false);
      }

      if (this.sync_Z) {
         autismclient.util.g.RenderState.init_V();
      }

      this.sync_Z = false;
   }

   private boolean init_Z_D(double mouseX, double mouseY, boolean pressing) {
      int[] track = this.write_IArr;
      if (track == null) {
         return false;
      }

      if (pressing && (mouseX < track[0] || mouseX >= track[0] + track[2] || mouseY < track[1] || mouseY >= track[1] + track[3])) {
         return false;
      }

      autismclient.util.g.RenderState.init_V_I(autismclient.util.g.RenderState.init_I_D(mouseX, track[0], track[2]));
      return true;
   }

   private void init(GuiGraphicsExtractor graphics, Identifier fontId, String text, int x, int y, int width) {
      autismclient.gui.f.b.k.init(graphics, this.font, autismclient.gui.f.b.k.load(this.font, text, width, fontId, cache_I()), fontId, cache_I(), x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, Identifier fontId, autismclient.util.g.ClassCache.Inner_FieldCache snapshot, int x, int y) {
      int cx = x;
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, init_Identifier, cx, y - 1, 9, 9);
      cx += 11;
      String hp = init_Str_F(snapshot.init_F()) + "/" + init_Str_F(snapshot.load_F());
      autismclient.gui.f.b.k.init(graphics, this.font, hp, fontId, value_I(), cx, y, false);
      cx += autismclient.gui.f.b.k.init(this.font, hp, fontId, value_I()) + 10;
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, load_Identifier, cx, y - 1, 9, 9);
      cx += 11;
      autismclient.gui.f.b.k.init(graphics, this.font, snapshot.read_I() + "/20", fontId, value_I(), cx, y, false);
   }

   private static String init_Str_F(float value) {
      return value == Math.rint(value) ? Integer.toString((int)value) : String.format(Locale.ROOT, "%.1f", value);
   }

   private static int load_I_ClassCache_FieldCa(autismclient.util.g.ClassCache.Inner_FieldCache snapshot) {
      int var10000;
      switch (snapshot.init_ClassCache_JsonUti(System.currentTimeMillis())) {
         case init:
            var10000 = -11013497;
            break;
         case read:
            var10000 = -42406;
            break;
         case load:
            var10000 = -866997;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void reset_V_2() {
      String value = this.init_EditBox.getValue();
      if (value == null || value.isBlank()) {
         String last = autismclient.util.g.PacketUtil.init_PacketUtil().load_Str();
         if (last == null || last.isBlank()) {
            this.tick_Str = "Empty input";
            this.level = ValueSink.close;
            return;
         }

         value = last;
      }

      Set<String> targets = !this.init_LinkedHashSet.isEmpty()
         ? this.init_LinkedHashSet
         : (this.write_Str != null ? Set.of(this.write_Str) : this.init_LinkedHashSet);
      autismclient.util.g.PacketUtil.Inner_CachePool result = autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_Str_Set(value, targets);
      this.tick_Str = init_Str_PacketUtil_CachePo_2(result);
      this.level = result.read() > 0 ? ValueSink.poll : (result.load() > 0 ? ValueSink.close : ValueSink.open);
      autismclient.util.g.PacketUtil.init_PacketUtil().read_V_Str(value);
      if (result.init_I() > 0) {
         this.init_EditBox.setValue("");
      }

      this.pool = -1;
      this.start_V_2();
      this.valid_V_2();
   }

   private void start_V_2() {
      if (this.init_EditBox != null) {
         String last = this.init_EditBox.getValue().isEmpty() ? autismclient.util.g.PacketUtil.init_PacketUtil().load_Str() : null;
         this.init_EditBox.setSuggestion(last);
      }
   }

   private void stop_V_2() {
      autismclient.util.g.PacketUtil.Inner_CachePool result = autismclient.util.g.PacketUtil.init_PacketUtil().update_PacketUtil_CachePo_Set(this.load_Set());
      this.tick_Str = init_Str_PacketUtil_CachePo_2(result);
      this.level = result.read() > 0 ? ValueSink.poll : (result.load() > 0 ? ValueSink.close : ValueSink.open);
   }

   private void load_V_I(int index) {
      autismclient.util.g.FieldCache profile = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
      if (profile != null) {
         autismclient.util.g.ListPool action = profile.init_ListPool_I(index);
         if (action.init_Z()) {
            this.read(index);
         } else {
            autismclient.util.g.PacketUtil.Inner_CachePool result = autismclient.util.g.PacketUtil.init_PacketUtil()
               .init_PacketUtil_CachePo_ListPool_Set(action, this.load_Set());
            this.tick_Str = init_Str_PacketUtil_CachePo_2(result);
            this.level = result.read() > 0 ? ValueSink.poll : (result.load() > 0 ? ValueSink.close : ValueSink.open);
         }
      }
   }

   private void read(int index) {
      autismclient.util.g.FieldCache profile = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
      if (profile != null && this.minecraft != null) {
         this.minecraft
            .gui
            .setScreen(
               new MethodUtil(
                  this,
                  index,
                  profile.init_ListPool_I(index),
                  action -> {
                     autismclient.util.g.PacketUtil.init_PacketUtil().init_V_I_ListPool(index, action);
                     this.tick_Str = "Saved";
                     this.level = ValueSink.open;
                  },
                  action -> {
                     autismclient.util.g.PacketUtil.Inner_CachePool result = autismclient.util.g.PacketUtil.init_PacketUtil()
                        .init_PacketUtil_CachePo_ListPool_Set(action, this.load_Set());
                     this.tick_Str = init_Str_PacketUtil_CachePo_2(result);
                     this.level = result.read() > 0 ? ValueSink.poll : (result.load() > 0 ? ValueSink.close : ValueSink.open);
                     return result;
                  }
               )
            );
      }
   }

   private void next_V_2() {
      autismclient.util.g.PacketUtil.init_PacketUtil().read_V();
      this.tick_Str = "Reset";
      this.level = ValueSink.open;
      this.load_V_21();
   }

   private void prev_V_2() {
      autismclient.util.g.FieldCache profile = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
      if (profile != null) {
         this.minecraft
            .gui
            .setScreen(
               new CharEncoder(
                  this,
                  profile.init_IoWorker,
                  autismclient.util.g.PacketUtil.init_PacketUtil()::init_V_IoWorker,
                  profile.init_CachePool,
                  autismclient.util.g.PacketUtil.init_PacketUtil()::init_V_CachePool,
                  true
               )
            );
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
         if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.init_Screen);
         }
      } else {
         if (this.write_Str != null && this.init_ClassCache_FieldCa_Str(this.write_Str) == null) {
            this.push_V_4();
         }

         long uiRevision = autismclient.util.g.PacketUtil.init_PacketUtil().load_J();
         if (uiRevision != this.update_J) {
            this.load_V_21();
         } else {
            long sessionRevision = autismclient.util.g.PacketUtil.init_PacketUtil().init_J();
            if (sessionRevision != this.sync_J) {
               this.sync_J = sessionRevision;
               this.get_V_4();
            }
         }

         this.set_V_4();
         this.size_V();
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.init_NetHelper.init_SyncManager.init_Z()) {
         if (this.init_NetHelper.init_SyncManager.init(Compat263.keyEventGlfwKey(event))) {
            this.data_V();
            return true;
         } else {
            return true;
         }
      } else if (Compat263.keyEventGlfwKey(event) != 67
         || !event.hasControlDown()
         || !this.init_BufferHelper.load_Z()
         || this.init_EditBox != null && this.init_EditBox.isFocused()
         || this.init_k != null && this.init_k.init_Z()) {
         if (Compat263.keyEventGlfwKey(event) == 256 && this.init_BufferHelper.load_Z()) {
            this.init_BufferHelper.load_V();
            return true;
         }

         if (this.init_Z_11() && Compat263.keyEventGlfwKey(event) == 256) {
            this.push_V_4();
            return true;
         }

         boolean chatFocused = this.init_EditBox != null && this.init_EditBox.isFocused();
         if (this.init_Z_11() && !chatFocused && this.data >= 0 && this.minecraft != null && this.minecraft.options.keyDrop.matches(event)) {
            if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
               autismclient.util.g.BufferHelper.Inner_DataUtil spec = autismclient.util.g.BufferHelper.init(event.hasControlDown());
               if (this.write_Z) {
                  this.load_V_PacketUtil_CachePo(
                     autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List(this.init_List_11(), this.data, spec)
                  );
               } else {
                  autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_2(this.write_Str, this.data, spec);
               }

               return true;
            } else {
               this.tick_Str = this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.load_Z()
                  ? "Inventory is waiting for a server update"
                  : "Inventory is synchronizing";
               this.level = ValueSink.close;
               return true;
            }
         } else if (this.init_Z_11() && !chatFocused && this.valid >= 0 && (Compat263.keyEventGlfwKey(event) == 85 || Compat263.keyEventGlfwKey(event) == 73)) {
            if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
               boolean use = Compat263.keyEventGlfwKey(event) == 73;
               if (this.write_Z) {
                  this.load_V_PacketUtil_CachePo(
                     use
                        ? autismclient.util.g.PacketUtil.init_PacketUtil().load(this.init_List_11(), this.valid)
                        : autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List_I(this.init_List_11(), this.valid)
                  );
               } else {
                  String result = use
                     ? autismclient.util.g.PacketUtil.init_PacketUtil().load_Str(this.write_Str, this.valid)
                     : autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_I(this.write_Str, this.valid);
                  if (!"Sent".equals(result)) {
                     this.tick_Str = result;
                     this.level = ValueSink.close;
                  }
               }

               return true;
            } else {
               this.tick_Str = this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.load_Z()
                  ? "Inventory is waiting for a server update"
                  : "Inventory is synchronizing";
               this.level = ValueSink.close;
               return true;
            }
         } else {
            if (this.init_k != null && this.init_k.init_Z_KeyEvent(event)) {
               return true;
            }

            if (this.init_EditBox != null && this.init_EditBox.isFocused()) {
               switch (Compat263.keyEventGlfwKey(event)) {
                  case 256:
                     if (!this.open_List.isEmpty()) {
                        this.valid_V_2();
                        return true;
                     }
                     break;
                  case 257:
                  case 335:
                     this.reset_V_2();
                     return true;
                  case 258:
                     this.init_Z_Z((Compat263.keyEventGlfwModifiers(event) & 1) != 0);
                     return true;
                  case 264:
                     this.write(1);
                     return true;
                  case 265:
                     this.write(-1);
                     return true;
               }
            }

            return super.keyPressed(event);
         }
      } else {
         this.value_V();
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_NetHelper.init_SyncManager.init_Z()) {
         if (this.init_NetHelper.init_SyncManager.init((char)input.codepoint())) {
            this.data_V();
         }

         return true;
      } else {
         return this.init_k != null && this.init_k.init_Z_CharacterEvent(input) ? true : super.charTyped(input);
      }
   }

   private void write(int direction) {
      List<String> history = autismclient.util.g.PacketUtil.init_PacketUtil().init_List();
      if (!history.isEmpty() && this.init_EditBox != null) {
         if (direction < 0) {
            if (this.pool == -1) {
               this.close_Str = this.init_EditBox.getValue();
               this.pool = history.size() - 1;
            } else if (this.pool > 0) {
               this.pool--;
            }
         } else {
            if (this.pool == -1) {
               return;
            }

            this.pool++;
            if (this.pool >= history.size()) {
               this.pool = -1;
               this.update(this.close_Str);
               return;
            }
         }

         this.update(history.get(this.pool));
      }
   }

   private void update(String value) {
      this.init_EditBox.setValue(value);
      this.init_EditBox.moveCursorToEnd(false);
      this.valid_V_2();
   }

   private boolean init_Z_Z(boolean backwards) {
      if (!this.load_Z_5()) {
         return false;
      }

      if (this.close_Z) {
         this.index = Math.floorMod(this.index + (backwards ? -1 : 1), this.open_List.size());
      }

      this.close_Z = true;
      this.open_Z = true;
      this.update(this.index);
      return true;
   }

   private boolean load_Z_5() {
      if (!this.open_List.isEmpty() && this.init_EditBox != null) {
         String value = this.init_EditBox.getValue();
         return value.equals(this.poll_Str) || value.equals(this.push_Str);
      } else {
         return false;
      }
   }

   private void update(int index) {
      if (this.init_EditBox != null && this.poll_Str != null && index >= 0 && index < this.open_List.size()) {
         int start = Math.max(0, Math.min(this.count, this.poll_Str.length()));
         int end = Math.max(start, Math.min(this.count + this.flags, this.poll_Str.length()));
         String entry = this.open_List.get(index);
         String full = this.poll_Str.substring(0, start) + entry + this.poll_Str.substring(end);
         this.init_EditBox.setValue(full);
         int caret = Math.min(start + entry.length(), full.length());
         this.init_EditBox.setCursorPosition(caret);
         this.init_EditBox.setHighlightPos(caret);
         this.push_Str = full;
      }
   }

   private void valid_V_2() {
      this.open_List.clear();
      this.poll_List.clear();
      this.index = 0;
      this.open_Str = null;
      this.poll_Str = null;
      this.push_Str = null;
      this.close_Z = false;
      this.open_Z = false;
   }

   private void size_V() {
      if (this.init_EditBox != null) {
         String value = this.init_EditBox.getValue();
         if (!this.init_EditBox.isFocused()) {
            if (!this.open_List.isEmpty() || this.open_Str != null) {
               this.valid_V_2();
            }

            this.pop_Str = null;
         } else {
            if (this.open_Z) {
               if (value.equals(this.push_Str)) {
                  return;
               }

               this.open_Z = false;
            }

            if (!value.equals(this.open_Str)) {
               if (value.startsWith("/")) {
                  this.sync(value);
               } else if (autismclient.commands.DataUtil.init_Z(value)) {
                  this.flush(value);
               } else {
                  if (!this.open_List.isEmpty() || this.open_Str != null) {
                     this.valid_V_2();
                  }

                  this.pop_Str = null;
               }
            }
         }
      }
   }

   private void sync(String value) {
      String stripped = value.substring(1);
      long now = System.currentTimeMillis();
      if (!value.equals(this.pop_Str) && now - this.write_J >= 60L) {
         autismclient.util.g.PacketUtil.init_PacketUtil().init_V_Str_Set(stripped, this.load_Set());
         this.pop_Str = value;
         this.write_J = now;
      }

      autismclient.util.g.PacketUtil.Inner_ByteReader result = autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_ByteRea_Str(stripped);
      if (result != null) {
         this.init(value, 1 + result.init_I(), result.load(), result.init_List());
      }
   }

   private void flush(String value) {
      this.pop_Str = null;
      autismclient.gui.b.DataUtil.Inner_StreamUtil r = autismclient.gui.b.DataUtil.init_DataUtil_StreamUti(value);
      if (r != null) {
         this.init(value, r.init_I(), r.load(), r.init_List());
      }
   }

   private void init(String value, int absStart, int length, List<String> entries) {
      if (!value.equals(this.open_Str)) {
         this.index = 0;
         this.close_Z = false;
      }

      this.open_Str = value;
      this.poll_Str = value;
      this.count = absStart;
      this.flags = length;
      this.open_List.clear();
      this.open_List.addAll(entries);
      if (this.index >= this.open_List.size()) {
         this.index = Math.max(0, this.open_List.size() - 1);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0 && this.load_Z_5()) {
         for (int[] rect : this.poll_List) {
            if (virtualEvent.x() >= rect[0] && virtualEvent.x() < rect[0] + rect[2] && virtualEvent.y() >= rect[1] && virtualEvent.y() < rect[1] + rect[3]) {
               this.index = rect[4];
               this.close_Z = true;
               this.open_Z = true;
               this.update(this.index);
               if (this.init_EditBox != null) {
                  this.init_EditBox.setFocused(true);
               }

               return true;
            }
         }
      }

      if (this.write_Z && Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
         for (int[] rect : this.sync_List) {
            if (virtualEvent.x() >= rect[0]
               && virtualEvent.x() < rect[0] + rect[2]
               && virtualEvent.y() >= rect[1]
               && virtualEvent.y() < rect[1] + rect[3]
               && rect[4] < this.update_List.size()) {
               this.update_Str = this.update_List.get(rect[4]).load();
               this.size = 0;
               this.flush_Str = null;
               return true;
            }
         }
      }

      if (this.init_Z_11()) {
         if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
            for (autismclient.gui.b.JsonUtil.Inner_TaskQueue hit : this.close_List) {
               if (!(virtualEvent.x() < hit.init_I())
                  && !(virtualEvent.x() >= hit.init_I() + hit.read())
                  && !(virtualEvent.y() < hit.load())
                  && !(virtualEvent.y() >= hit.load() + hit.write())) {
                  this.cache_V();
                  this.init_V_JsonUtil_ConfigSto(hit.init_JsonUtil_ConfigSto());
                  return true;
               }
            }
         }

         for (int[] rect : this.flush_List) {
            if (virtualEvent.x() >= rect[0] && virtualEvent.x() < rect[0] + 18 && virtualEvent.y() >= rect[1] && virtualEvent.y() < rect[1] + 18) {
               this.cache_V();
               this.init(rect[2], Compat263.mouseEventGlfwButton(virtualEvent), virtualEvent.hasShiftDown(), virtualEvent.hasControlDown());
               return true;
            }
         }

         if (virtualEvent.x() >= this.state
            && virtualEvent.x() < this.state + this.buffer
            && virtualEvent.y() >= this.queue
            && virtualEvent.y() < this.queue + this.entry) {
            this.cache_V();
            return true;
         }
      }

      if (Compat263.mouseEventGlfwButton(virtualEvent) == 1) {
         for (int[] rect : this.load_List) {
            if (virtualEvent.x() >= rect[0] && virtualEvent.x() < rect[0] + rect[2] && virtualEvent.y() >= rect[1] && virtualEvent.y() < rect[1] + rect[3]) {
               this.read(rect[4]);
               return true;
            }
         }
      }

      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0
         && this.sync_IArr != null
         && this.init_k != null
         && this.init_k.init_Z_D_2(virtualEvent.x(), virtualEvent.y(), 0)) {
         if (this.init_EditBox != null) {
            this.init_EditBox.setFocused(false);
         }

         this.setFocused(null);
         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(virtualEvent) == 0 && this.init_Z_D(virtualEvent.x(), virtualEvent.y(), true)) {
            this.update_Z = true;
            return true;
         }

         if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
            for (TableIndex.Inner_StreamUtil row : this.init_List) {
               int ax = this.add_I_3();
               int ay = row.init_I() + 1;
               int ah = update_I_11();
               int toggleW = this.put_I_3();
               if (virtualEvent.x() >= ax && virtualEvent.x() < ax + this.clear_I_3() && virtualEvent.y() >= ay && virtualEvent.y() < ay + ah) {
                  this.load_V_Str_2(row.init_Str());
                  return true;
               }

               int viewX = ax - 4 - toggleW;
               if (this.start_I_2() >= 220
                  && virtualEvent.x() >= viewX
                  && virtualEvent.x() < viewX + toggleW
                  && virtualEvent.y() >= ay
                  && virtualEvent.y() < ay + ah) {
                  if (row.init_Str().equals(this.write_Str)) {
                     this.push_V_4();
                  } else {
                     this.init_V_Str_8(row.init_Str());
                  }

                  return true;
               }

               boolean povShown = this.start_I_2() >= 220
                  && (autismclient.util.g.ValueSink.init_Z(row.init_Str()) || autismclient.util.g.ValueSink.load_Z(row.init_Str()));
               int povX = viewX - 4 - toggleW;
               if (povShown && virtualEvent.x() >= povX && virtualEvent.x() < povX + toggleW && virtualEvent.y() >= ay && virtualEvent.y() < ay + ah) {
                  autismclient.util.g.ValueSink.init(row.init_Str(), this);
                  return true;
               }

               if (virtualEvent.x() >= 10.0
                  && virtualEvent.x() < 6 + this.start_I_2() - 4
                  && virtualEvent.y() >= row.init_I()
                  && virtualEvent.y() < row.init_I() + this.read_I_14() + 12 + row.load()) {
                  this.init_V_Str(row.init_Str(), virtualEvent.hasControlDown(), virtualEvent.hasShiftDown());
                  return true;
               }
            }

            if (this.init_PoolEntry_DataUtil != null
               && this.init_PoolEntry_DataUtil.init_Z()
               && this.init_PoolEntry_DataUtil.init(virtualEvent.x(), virtualEvent.y())) {
               this.load_Z = true;
               this.next = this.init_PoolEntry_DataUtil.load(virtualEvent.x(), virtualEvent.y())
                  ? (int)Math.round(virtualEvent.y()) - this.init_PoolEntry_DataUtil.update()
                  : this.init_PoolEntry_DataUtil.sync() / 2;
               this.init_V_D(virtualEvent.y());
               return true;
            }

            if (this.load(virtualEvent.x(), virtualEvent.y())) {
               return true;
            }

            if (this.init_Z_D_D_2(virtualEvent.x(), virtualEvent.y())) {
               return true;
            }
         }

         boolean handled = super.mouseClicked(virtualEvent, doubled);
         if (!handled && Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
            this.cache_V();
            if (!this.init_LinkedHashSet.isEmpty()) {
               this.put = 0;
            }

            this.init_LinkedHashSet.clear();
            this.load_Str = null;
            return true;
         } else {
            return handled;
         }
      }
   }

   private void init_V_Str(String id, boolean ctrl, boolean shift) {
      LinkedHashSet<String> before = new LinkedHashSet<>(this.init_LinkedHashSet);
      List<String> ordered = this.load_List_6();
      if (shift && this.load_Str != null && ordered.contains(this.load_Str) && ordered.contains(id)) {
         int a = ordered.indexOf(this.load_Str);
         int b = ordered.indexOf(id);
         this.init_LinkedHashSet.clear();

         for (int i = Math.min(a, b); i <= Math.max(a, b); i++) {
            this.init_LinkedHashSet.add(ordered.get(i));
         }
      } else if (ctrl) {
         if (!this.init_LinkedHashSet.remove(id)) {
            this.init_LinkedHashSet.add(id);
         }

         this.load_Str = id;
      } else {
         boolean only = this.init_LinkedHashSet.size() == 1 && this.init_LinkedHashSet.contains(id);
         this.init_LinkedHashSet.clear();
         if (!only) {
            this.init_LinkedHashSet.add(id);
            this.load_Str = id;
         } else {
            this.load_Str = null;
         }
      }

      if (!before.equals(this.init_LinkedHashSet)) {
         this.put = 0;
      }
   }

   private List<String> load_List_6() {
      List<String> ids = new ArrayList<>();

      for (autismclient.util.g.ClassCache.Inner_FieldCache s : autismclient.util.g.PacketUtil.init_PacketUtil().load_List()) {
         ids.add(s.init_Str());
      }

      return ids;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_k != null && this.init_k.init_Z() && this.init_k.load(virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         return true;
      } else if (this.update_Z) {
         this.update_Z = false;
         autismclient.util.g.RenderState.init_V();
         return true;
      } else if (this.load_Z) {
         this.load_Z = false;
         return true;
      } else if (this.init_Z) {
         this.init_Z = false;
         this.init_BufferHelper.init_V();
         return true;
      } else {
         return super.mouseReleased(virtualEvent);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.update_Z) {
         this.init_Z_D(virtualEvent.x(), virtualEvent.y(), false);
         return true;
      } else if (this.init_k != null
         && this.init_k.init_Z()
         && this.init_k.init(virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent), dragX, dragY)) {
         return true;
      } else if (this.load_Z) {
         this.init_V_D(virtualEvent.y());
         return true;
      } else if (this.init_Z) {
         this.init_V_D(virtualEvent.x(), virtualEvent.y(), false);
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dragX), cA.init_D_D(dragY));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      double vx = cA.init_D_D(mouseX);
      double vy = cA.init_D_D(mouseY);
      if (vx < 6 + this.start_I_2()) {
         this.task = Math.max(0, this.task + (vertical < 0.0 ? 1 : -1));
         this.get_V_4();
         return true;
      } else {
         int[] delayRow = this.update_IArr;
         if (delayRow != null && vx >= delayRow[0] && vx < delayRow[0] + delayRow[2] && vy >= delayRow[1] && vy < delayRow[1] + delayRow[3]) {
            this.init_V_I_9(vertical > 0.0 ? 1 : -1);
            return true;
         } else if (this.init_Z_11() && vy < this.reset) {
            this.size = Math.max(0, this.size + (vertical < 0.0 ? 18 : -18));
            return true;
         } else {
            this.put = Math.max(0, this.put + (vertical > 0.0 ? 1 : -1));
            return true;
         }
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), prev_I_2());
         int sessionW = this.start_I_2();
         int panelH = Math.max(1, this.tick_I_14() - 6);
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(6, 6, sessionW, panelH), 6, size_I(), data_I());
         int consoleX = this.stop_I_2();
         int consoleW = this.next_I_2();
         int innerX = consoleX + 4;
         int innerW = consoleW - 8;
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(consoleX, 6, consoleW, panelH), 6, valid_I_2(), data_I());
         List<autismclient.util.g.ClassCache.Inner_FieldCache> snapshots = autismclient.util.g.PacketUtil.init_PacketUtil().load_List();
         this.init_Map.clear();
         int ready = 0;

         for (autismclient.util.g.ClassCache.Inner_FieldCache s : snapshots) {
            this.init_Map.put(s.init_Str(), s);
            if (s.load_Z()) {
               ready++;
            }
         }

         int headerTextY = this.init_I_I_I_9(this.close_I_6(), sync_I_9());
         int titleRight = this.load_IArr != null ? this.load_IArr[0] - 4 : 6 + sessionW - 4;
         this.init(graphics, "Sessions  " + ready + "/" + snapshots.size() + " ready", 14, headerTextY, Math.max(1, titleRight - 14), value_I());
         int subTextY = this.init_I_I_I_9(this.open_I_5(), flush_I_8());
         autismclient.util.g.ClassCache.Inner_FieldCache viewed = this.init_LinkedHashSet.size() == 1
            ? this.init_ClassCache_FieldCa_Str(this.init_LinkedHashSet.iterator().next())
            : null;
         if (viewed != null) {
            String detail = autismclient.gui.f.b.k.load(
               this.font, init_Str_ClassCache_FieldCa(viewed), Math.max(1, sessionW - 16), init_CharEncoder.init_Identifier_l(l.read), cache_I()
            );
            boolean up = viewed.init_ClassCache_JsonUti(System.currentTimeMillis()) == autismclient.util.g.ClassCache.Inner_JsonUtil.init;
            this.init(graphics, detail, 14, subTextY, up ? cache_I() : load_I_ClassCache_FieldCa(viewed));
         } else if (!this.init_LinkedHashSet.isEmpty()) {
            this.init(graphics, this.init_LinkedHashSet.size() + " selected", 14, subTextY, cache_I());
         } else if (snapshots.isEmpty()) {
            this.init(graphics, "Starting sessions...", 14, this.init_I_I_I_9(this.push_I_4(), flush_I_8()), cache_I());
         }

         if (this.write_Z) {
            this.update_List = autismclient.util.g.NodeWalker.init();
         }

         String viewTarget = this.load_Str_6();
         boolean viewing = viewTarget != null && this.init_ClassCache_FieldCa_Str(viewTarget) != null;
         if (viewing) {
            this.close(viewTarget);
         }

         if (viewing && this.init_ClassCache_ByteRea != null) {
            cA.init(graphics, innerX, this.close_I_6(), this.set_I_4() - 4, this.close_I_6() + sync_I_9());
            Component viewTitle = this.init_ClassCache_ByteRea.init_Component();
            graphics.text(this.font, viewTitle.getVisualOrderText(), innerX, j.init(this.close_I_6(), sync_I_9(), 9, 0), value_I(), false);
            graphics.disableScissor();
         } else {
            this.init(graphics, "Console", innerX, headerTextY, innerW, value_I());
         }

         if (viewing) {
            this.write_IArr = this.update_IArr = this.sync_IArr = null;
            this.clear_V_3();
            int gridTop = this.init_I_I_14(3);
            if (this.write_Z) {
               this.load(graphics, innerX, gridTop, innerW, virtualMouseX, virtualMouseY);
               gridTop += update_I_11() + 4;
            }

            int bottomLimit = this.get_I_4();
            int available = bottomLimit - gridTop;
            int gridMaxH = Math.max(36, available - 66 - 12);
            this.init_V_GuiGraphicsExtract_4(graphics, innerX, gridTop, innerW, gridMaxH, virtualMouseX, virtualMouseY);
            this.node = gridTop + 2;
            int chatTop = gridTop + Math.max(12, this.entry) + 12;
            this.init(graphics, innerX, chatTop, innerW, Math.max(0, bottomLimit - chatTop));
         } else {
            this.sync_List.clear();
            if (this.write_Z) {
               this.init(graphics, "No bot has a GUI open yet.", innerX, this.init_I_I_I_9(this.init_I_I_14(3), update_I_11()), innerW, cache_I());
            }

            int delayY = this.init_I_I_14(4);
            this.init_V_GuiGraphicsExtract_3(graphics, innerX, delayY, innerW, virtualMouseX, virtualMouseY, delta);
            int chatTop = delayY + 13 + 4;
            this.init(graphics, innerX, chatTop, innerW, Math.max(0, this.get_I_4() - chatTop));
         }

         if (!this.tick_Str.isBlank()) {
            this.init(graphics, this.tick_Str, innerX, this.init_I_I_I_9(this.get_I_4(), flush_I_8()), innerW, load_I_I_5(this.level));
         }

         this.init_V_GuiGraphicsExtract_5(graphics, virtualMouseX, virtualMouseY);
         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         this.load(graphics, virtualMouseX, virtualMouseY);
         if (viewing) {
            ItemStack carried = this.init_ClassCache_ByteRea != null ? this.init_ClassCache_ByteRea.init_ItemStack() : ItemStack.EMPTY;
            if (carried != null && !carried.isEmpty()) {
               graphics.nextStratum();
               graphics.item(carried, virtualMouseX - 8, virtualMouseY - 8);
               graphics.itemDecorations(this.font, carried, virtualMouseX - 8, virtualMouseY - 8);
            } else if (!this.init_ItemStack.isEmpty()) {
               this.init(graphics, this.init_ItemStack, this.value, this.cache);
            }

            if (this.init_ClassCache_ByteRea != null && this.buffer > 0) {
               graphics.nextStratum();
               int colW = this.font.width("SyncID: 000000") + 6;
               int infoX = consoleX + consoleW - 4 - colW;
               if (infoX >= innerX + this.buffer + 8) {
                  this.init_V_GuiGraphicsExtract_5(
                     graphics,
                     infoX,
                     this.node,
                     colW,
                     "Rev: ",
                     Integer.toString(this.init_ClassCache_ByteRea.load_I()),
                     ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.init)
                  );
                  this.init_V_GuiGraphicsExtract_5(
                     graphics,
                     infoX,
                     this.node + 11,
                     colW,
                     "SyncID: ",
                     Integer.toString(this.init_ClassCache_ByteRea.init_I()),
                     ct.init_I_I_ct_DataUtil(-791321, ct.Inner_DataUtil.read)
                  );
                  int visibleSlot = this.data >= 0 ? autismclient.util.g.PacketUtil.init_PacketUtil().load_I(this.load_Str_6(), this.data) : -1;
                  this.init_V_GuiGraphicsExtract_5(
                     graphics,
                     infoX,
                     this.node + 22,
                     colW,
                     "Slot: ",
                     visibleSlot >= 0 ? Integer.toString(visibleSlot) : "--",
                     ct.init_I_I_ct_DataUtil(-7350273, ct.Inner_DataUtil.init)
                  );
               }
            }
         }

         if (AutismConfig.getGlobal().multiShowTooltips) {
            String tip = null;
            if (init_Z_IArr(this.init_IArr, virtualMouseX, virtualMouseY)) {
               tip = "Hide GUI locally, keep it open";
            } else if (init_Z_IArr(this.read_IArr, virtualMouseX, virtualMouseY)) {
               tip = "Control every bot's GUI at once";
            } else if (init_Z_IArr(this.load_IArr, virtualMouseX, virtualMouseY)) {
               tip = "Show HP, food, and position";
            } else if (this.sync_Str != null) {
               tip = this.sync_Str;
            }

            if (tip != null) {
               autismclient.gui.b.UiHelper.init(graphics, this.font, tip, virtualMouseX, virtualMouseY);
            }
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void load(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      this.poll_List.clear();
      if (!this.open_List.isEmpty() && this.init_EditBox != null && this.init_EditBox.isFocused()) {
         Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
         int rowH = 12;
         int visible = Math.min(8, this.open_List.size());
         int start = Math.max(0, Math.min(this.index - visible + 1, this.open_List.size() - visible));
         int textW = 40;

         for (int i = 0; i < visible; i++) {
            textW = Math.max(textW, autismclient.gui.f.b.k.init(this.font, this.open_List.get(start + i), fontId, cache_I()));
         }

         int popupW = Math.min(240, textW + 10);
         int popupH = visible * rowH + 2;
         int caretIndex = Math.min(this.count, this.init_EditBox.getValue().length());
         int anchorX = this.init_EditBox.getScreenX(caretIndex) - 3;
         int minX = this.stop_I_2() + 4;
         int maxX = Math.max(minX, this.init_I() - 6 - popupW);
         int popupX = Math.max(minX, Math.min(anchorX, maxX));
         int popupY = Math.max(20, this.init_EditBox.getY() - popupH - 2);
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(popupX, popupY, popupW, popupH), 6, -301266161, data_I());

         for (int i = 0; i < visible; i++) {
            int idx = start + i;
            int ry = popupY + 1 + i * rowH;
            boolean hover = mouseX >= popupX && mouseX < popupX + popupW && mouseY >= ry && mouseY < ry + rowH;
            boolean sel = idx == this.index;
            if (sel || hover) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(popupX + 1, ry, popupW - 2, rowH), sel ? 1714322943 : 872415231);
            }

            int color = sel ? -1 : cache_I();
            String label = autismclient.gui.f.b.k.load(this.font, this.open_List.get(idx), popupW - 8, fontId, color);
            autismclient.gui.f.b.k.init(graphics, this.font, label, fontId, color, popupX + 4, ry + 2, false);
            this.poll_List.add(new int[]{popupX + 1, ry, popupW - 2, rowH, idx});
         }
      }
   }

   private void close(String target) {
      autismclient.util.g.ClassCache.Inner_FieldCache snap = this.init_Map.get(target);
      long rev = snap != null ? snap.load_J() : -1L;
      if (!target.equals(this.flush_Str) || rev != this.load_J) {
         this.init_ClassCache_ByteRea = autismclient.util.g.PacketUtil.init_PacketUtil().init_ClassCache_ByteRea_Str(target);
         this.flush_Str = target;
         this.load_J = rev;
      }
   }

   private void load(GuiGraphicsExtractor graphics, int x, int y, int w, int mouseX, int mouseY) {
      this.sync_List.clear();
      autismclient.util.g.NodeWalker.Inner_DataUtil current = autismclient.util.g.NodeWalker.init(this.update_List, this.update_Str);
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      int cx = x;

      for (int i = 0; i < this.update_List.size(); i++) {
         autismclient.util.g.NodeWalker.Inner_DataUtil group = this.update_List.get(i);
         int room = x + w - cx;
         int tabH = update_I_11();
         int tabTextY = this.init_I_I_I_9(y, tabH);
         if (room < 40) {
            autismclient.gui.f.b.k.init(graphics, this.font, "+" + (this.update_List.size() - i), fontId, cache_I(), cx, tabTextY, false);
            break;
         }

         boolean selected = current != null && group.load().equals(current.load());
         String label = autismclient.gui.f.b.k.load(this.font, group.read(), Math.min(150, room - 10), fontId, value_I());
         int bw = autismclient.gui.f.b.k.init(this.font, label, fontId, value_I()) + 10;
         boolean hover = mouseX >= cx && mouseX < cx + bw && mouseY >= y && mouseY < y + tabH;
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(cx, y, bw, tabH),
            4,
            selected ? 859166835 : (hover ? 872415231 : 705827348),
            selected ? state() : data_I()
         );
         autismclient.gui.f.b.k.init(graphics, this.font, label, fontId, selected ? value_I() : cache_I(), cx + 5, tabTextY, false);
         this.sync_List.add(new int[]{cx, y, bw, tabH, i});
         cx += bw + 2;
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
      this.flush_List.clear();
      autismclient.util.g.ClassCache.Inner_ByteReader view = this.init_ClassCache_ByteRea;
      if (view == null) {
         this.state = x;
         this.queue = y;
         this.buffer = 0;
         this.entry = 0;
         this.init_ItemStack = ItemStack.EMPTY;
         this.data = -1;
         this.valid = -1;
         this.init(graphics, "Loading...", x, y, w, cache_I());
      } else {
         this.close_List.clear();
         this.init_NetHelper.init(view.init_ClassCache_FileCac() == null ? "" : view.init_ClassCache_FileCac().init_Str());
         int[] cs = autismclient.gui.b.JsonUtil.init_IArr_ClassCache_ByteRea(view);
         int contentW = Math.max(18, cs[0]);
         int contentH = Math.max(18, cs[1]);
         int panelW = Math.min(w, contentW);
         int panelH = Math.min(h, contentH);
         this.size = Math.max(0, Math.min(this.size, Math.max(0, contentH - panelH)));
         this.state = x;
         this.queue = y;
         this.buffer = panelW;
         this.entry = panelH;
         cA.init(graphics, x - 3, y - 3, x + panelW + 3, y + panelH + 3);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x - 3, y - 3, panelW + 6, panelH + 6), -3750202);
         int selectedHandler = autismclient.util.g.PacketUtil.init_PacketUtil().init_I_Str(this.load_Str_6());
         autismclient.util.g.ClassCache.Inner_MapStore hovered = null;

         for (autismclient.util.g.ClassCache.Inner_MapStore slot : view.init_List()) {
            int sx = x + slot.init_I();
            int sy = y + slot.load() - this.size;
            if (sy + 18 >= y && sy <= y + panelH) {
               this.read(graphics, sx, sy);
               if (selectedHandler >= 0 && slot.read() == selectedHandler) {
                  autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(sx, sy, 18, 18), 452935744, -1325449152);
               }

               ItemStack stack = slot.init_ItemStack();
               if (stack != null && !stack.isEmpty()) {
                  try {
                     graphics.item(stack, sx + 1, sy + 1);
                     graphics.itemDecorations(this.font, stack, sx + 1, sy + 1);
                  } catch (Throwable var22) {
                  }
               }

               this.flush_List.add(new int[]{sx, sy, slot.read()});
               if (mouseX >= sx
                  && mouseX < sx + 18
                  && mouseY >= sy
                  && mouseY < sy + 18
                  && mouseX >= x
                  && mouseX < x + panelW
                  && mouseY >= y
                  && mouseY < y + panelH) {
                  hovered = slot;
               }
            }
         }

         autismclient.gui.b.JsonUtil.init(graphics, this.font, view, x, y, this.size, mouseX, mouseY, this.close_List, this.init_NetHelper);
         graphics.disableScissor();
         this.init_ItemStack = hovered != null && hovered.init_ItemStack() != null ? hovered.init_ItemStack() : ItemStack.EMPTY;
         this.data = hovered != null ? hovered.read() : -1;
         this.valid = hovered != null ? autismclient.util.g.PacketUtil.init_PacketUtil().init_I_Str_I(this.load_Str_6(), hovered.read()) : -1;
         this.value = mouseX;
         this.cache = mouseY;
      }
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor graphics, int x, int y, int w, String key, String value, int valueColor) {
      int keyColor = ct.init_I_I_ct_DataUtil(-4743522, ct.Inner_DataUtil.read);
      graphics.text(this.font, Component.literal(key).getVisualOrderText(), x, y, keyColor, false);
      int vx = x + this.font.width(key);
      String fit = autismclient.gui.f.b.k.load(this.font, value, Math.max(1, x + w - vx), init_CharEncoder.init_Identifier_l(l.read), valueColor);
      graphics.text(this.font, Component.literal(fit).getVisualOrderText(), vx, y, valueColor, false);
   }

   private void read(GuiGraphicsExtractor graphics, int cx, int cy) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cx, cy, 18, 18), -7631989);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cx, cy, 18, 1), -13158601);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cx, cy, 1, 18), -13158601);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cx, cy + 17, 18, 1), -1);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(cx + 17, cy, 1, 18), -1);
   }

   private void init(GuiGraphicsExtractor graphics, ItemStack stack, int mx, int my) {
      try {
         List<Component> base = Screen.getTooltipFromItem(this.minecraft, stack);
         if (base == null || base.isEmpty()) {
            return;
         }

         List<Component> lines = base;
         if (this.valid >= 0) {
            lines = new ArrayList<>(base);
            lines.add(Component.literal("[U] switch to slot"));
            lines.add(Component.literal("[I] switch + use item"));
         }

         int tw = 0;

         for (Component c : lines) {
            tw = Math.max(tw, this.font.width(c));
         }

         int th = lines.size() == 1 ? 8 : lines.size() * 10 - 2;
         int tx = mx + 12;
         int ty = my - 12;
         if (tx + tw + 4 > this.init_I()) {
            tx = Math.max(4, mx - tw - 16);
         }

         if (ty + th + 4 > this.load_I()) {
            ty = this.load_I() - th - 4;
         }

         if (ty < 4) {
            ty = 4;
         }

         graphics.nextStratum();
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(tx - 3, ty - 3, tw + 6, th + 6), 6, -267386864, 1347420320);
         int yy = ty;

         for (Component c : lines) {
            graphics.text(this.font, c.getVisualOrderText(), tx, yy, -1, true);
            yy += 10;
         }
      } catch (Throwable var14) {
      }
   }

   private ItemStack init_ItemStack_I(int handler) {
      if (this.init_ClassCache_ByteRea == null) {
         return ItemStack.EMPTY;
      }

      for (autismclient.util.g.ClassCache.Inner_MapStore slot : this.init_ClassCache_ByteRea.init_List()) {
         if (slot.read() == handler) {
            return slot.init_ItemStack();
         }
      }

      return ItemStack.EMPTY;
   }

   private void init(int handler, int button, boolean shift, boolean ctrl) {
      if (handler >= 0) {
         ItemStack stack = this.init_ItemStack_I(handler);
         if (button == 1 && ctrl && shift) {
            if (stack != null && !stack.isEmpty()) {
               this.init_V_ItemStack(stack);
            }
         } else if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
            autismclient.util.g.BufferHelper.Inner_DataUtil spec = autismclient.util.g.BufferHelper.init(button, shift, ctrl);
            if (this.write_Z) {
               this.load_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List(this.init_List_11(), handler, spec));
            } else {
               String result = autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_2(this.write_Str, handler, spec);
               if (!"Sent".equals(result)) {
                  this.tick_Str = result;
                  this.level = ValueSink.close;
               }
            }
         } else {
            this.tick_Str = this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.load_Z()
               ? "Inventory is waiting for a server update"
               : "Inventory is synchronizing";
            this.level = ValueSink.close;
         }
      }
   }

   private void init_V_JsonUtil_ConfigSto(autismclient.gui.b.JsonUtil.Inner_ConfigStore action) {
      if (action instanceof autismclient.gui.b.JsonUtil.Inner_JsonUtil) {
         this.init_NetHelper.init_SyncManager.init_V();
         this.init_NetHelper.init_SyncManager.init("");
      } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_StreamUtil p) {
         if (p.init_Z()) {
            this.init_NetHelper.load = p.init_I();
         } else {
            this.init_NetHelper.init_I = p.init_I();
         }
      } else if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
         autismclient.util.g.PacketUtil mgr = autismclient.util.g.PacketUtil.init_PacketUtil();
         String type = this.read_Str_4();
         if (action instanceof autismclient.gui.b.JsonUtil.Inner_CachePool b) {
            if (this.write_Z) {
               this.load_V_PacketUtil_CachePo(mgr.init_PacketUtil_CachePo_List_2(this.init_List_11(), b.init(), type));
            } else {
               this.open(mgr.read_Str_Str_I(this.write_Str, b.init()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_StateCache t) {
            if (this.write_Z) {
               this.load_V_PacketUtil_CachePo(mgr.load(this.init_List_11(), t.init(), type));
            } else {
               this.open(mgr.write(this.write_Str, t.init()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_DataUtil be) {
            if (this.write_Z) {
               this.load_V_PacketUtil_CachePo(mgr.init_PacketUtil_CachePo_List(this.init_List_11(), be.init(), be.load(), type));
            } else {
               this.open(mgr.init_Str_Str(this.write_Str, be.init(), be.load()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_NetHelper rs) {
            this.init_NetHelper.read = Math.max(0, this.init_NetHelper.read + rs.init());
            if (this.write_Z) {
               this.load_V_PacketUtil_CachePo(mgr.init_PacketUtil_CachePo_List_2(this.init_List_11(), this.init_NetHelper.read, type));
            } else {
               this.open(mgr.read_Str_Str_I(this.write_Str, this.init_NetHelper.read));
            }
         }
      } else {
         this.tick_Str = "Inventory is synchronizing";
         this.level = ValueSink.close;
      }
   }

   private void open(String result) {
      if (!"Sent".equals(result)) {
         this.tick_Str = result;
         this.level = ValueSink.close;
      }
   }

   private String read_Str_4() {
      return this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_ClassCache_FileCac() != null
         ? this.init_ClassCache_ByteRea.init_ClassCache_FileCac().init_Str()
         : "";
   }

   private void data_V() {
      if (this.write_Z) {
         this.load_V_PacketUtil_CachePo(
            autismclient.util.g.PacketUtil.init_PacketUtil()
               .init_PacketUtil_CachePo_List_4(this.init_List_11(), this.init_NetHelper.init_SyncManager.init_Str(), this.read_Str_4())
         );
      } else {
         autismclient.util.g.PacketUtil.init_PacketUtil().load_Str(this.write_Str, this.init_NetHelper.init_SyncManager.init_Str());
      }
   }

   private void init_V_ItemStack(ItemStack stack) {
      if (this.minecraft != null && stack != null && !stack.isEmpty() && AutismItemNbtInspectOverlay.init_Z_ItemStack(stack)) {
         AutismItemNbtInspectOverlay overlay = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(this.font);
         if (overlay != null) {
            this.minecraft.gui.setScreen(new TaskTimer(overlay, this, false, true));
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
      this.read_List.clear();
      this.add = x;
      this.clear = x + w;
      this.reset = y;
      this.start = y + h;
      int gutter = 6;
      int tsWidth = this.font.width("[00:00:00] ");
      int x0 = x + tsWidth;
      int avail = Math.max(1, x + w - gutter - x0);
      this.stop = avail;
      this.sync(avail);
      List<autismclient.gui.b.CachePool.Inner_DataUtil> rows = this.write_List;
      List<autismclient.gui.b.BufferHelper.Inner_DataUtil> selRows = new ArrayList<>(rows.size());

      for (autismclient.gui.b.CachePool.Inner_DataUtil row : rows) {
         selRows.add(
            new autismclient.gui.b.BufferHelper.Inner_DataUtil(
               row.init_PacketUtil_BufferH().init_J(), row.init_I(), autismclient.gui.b.BufferHelper.init_Str_FormattedText(row.init_FormattedText())
            )
         );
      }

      this.init_BufferHelper.init_V_List(selRows);
      int visible = Math.max(1, h / 12);
      int maxScroll = Math.max(0, rows.size() - visible);
      this.put = Math.max(0, Math.min(this.put, maxScroll));
      int end = rows.size() - this.put;
      int start = Math.max(0, end - visible);
      int yy = y;

      for (int i = start; i < end; i++) {
         autismclient.gui.b.CachePool.Inner_DataUtil row = rows.get(i);
         String rowText = autismclient.gui.b.BufferHelper.init_Str_FormattedText(row.init_FormattedText());
         int[] range = this.init_BufferHelper.init_IArr_J(row.init_PacketUtil_BufferH().init_J(), row.init_I(), rowText.length());
         if (range != null && range[1] > range[0]) {
            int sx = x0 + autismclient.gui.b.BufferHelper.init_I_Font(this.font, row.init_FormattedText(), range[0]);
            int ex = x0 + autismclient.gui.b.BufferHelper.init_I_Font(this.font, row.init_FormattedText(), range[1]);
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(sx, yy - 1, Math.max(1, ex - sx), 12), 1430023925);
         }

         if (row.init_I() == 0) {
            graphics.text(this.font, Component.literal(this.init_Str_J_2(row.init_PacketUtil_BufferH().load())), x, yy, cache_I(), false);
         }

         graphics.text(this.font, row.init_FormattedCharSeque(), x0, yy, value_I(), false);
         this.init(graphics, row.init_FormattedText(), x0, yy);
         this.read_List.add(new TableIndex.Inner_DataUtil(row.init_PacketUtil_BufferH(), row.init_I(), yy, x0, row.init_FormattedText(), rowText));
         yy += 12;
      }

      this.prev = maxScroll;
      int lineH = 12;
      int offsetFromTop = Math.max(0, maxScroll - this.put) * lineH;
      this.init_PoolEntry_DataUtil = autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         rows.size() * lineH, visible * lineH, x + w - 4, y, 3, Math.max(1, h), offsetFromTop
      );
      autismclient.gui.f.b.PoolEntry.init(graphics, this.init_PoolEntry_DataUtil, false, this.load_Z);
   }

   private void init_V_D(double vy) {
      if (this.init_PoolEntry_DataUtil != null) {
         int px = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil, vy, this.next);
         int rowsFromTop = Math.round(px / 12.0F);
         this.put = Math.max(0, Math.min(this.prev, this.prev - rowsFromTop));
      }
   }

   private void init_V_D(double mx, double my, boolean begin) {
      if (!this.read_List.isEmpty()) {
         TableIndex.Inner_DataUtil target = this.read_List.getFirst();

         for (TableIndex.Inner_DataUtil row : this.read_List) {
            if (my >= row.load()) {
               target = row;
            }
         }

         int ch = autismclient.gui.b.BufferHelper.load_I_Font(this.font, target.init_FormattedText(), (int)Math.round(mx - target.read()));
         if (begin) {
            this.init_BufferHelper.init_V_J(target.init_PacketUtil_BufferH().init_J(), target.init_I(), ch);
         } else {
            this.init_BufferHelper.load_V_J(target.init_PacketUtil_BufferH().init_J(), target.init_I(), ch);
         }
      }
   }

   private boolean init_Z_D_D_2(double mx, double my) {
      if (!(mx < this.add) && !(mx > this.clear) && !(my < this.reset) && !(my > this.start) && !this.read_List.isEmpty()) {
         this.init_Z = true;
         this.init_V_D(mx, my, true);
         return true;
      } else {
         this.init_BufferHelper.load_V();
         return false;
      }
   }

   private void value_V() {
      String text = this.init_BufferHelper.init_Str();
      if (!text.isBlank() && this.minecraft != null && this.minecraft.keyboardHandler != null) {
         this.minecraft.keyboardHandler.setClipboard(text);
         this.tick_Str = "Copied " + text.length() + " chars";
         this.level = ValueSink.open;
      }
   }

   private Set<String> init_Set_3() {
      return this.read_Set();
   }

   private Set<String> load_Set() {
      return this.read_Set();
   }

   private Set<String> read_Set() {
      if (!this.init_LinkedHashSet.isEmpty()) {
         return this.init_LinkedHashSet;
      }

      if (this.write_Str != null) {
         return Set.of(this.write_Str);
      }

      if (this.write_Z) {
         List<String> ids = this.init_List_11();
         if (!ids.isEmpty()) {
            return new LinkedHashSet<>(ids);
         }
      }

      return this.init_LinkedHashSet;
   }

   private void sync(int avail) {
      Set<String> scope = this.init_Set_3();
      String scopeKey = String.join(",", scope);
      long rev = autismclient.util.g.PacketUtil.init_PacketUtil().read_J();
      int total = this.reset_I_2();
      if (rev != this.init_J || !scopeKey.equals(this.read_Str) || avail != this.get || total != this.set) {
         int previousRows = this.write_List.size();
         boolean preserveScrolledPosition = this.put > 0 && scopeKey.equals(this.read_Str) && avail == this.get && total == this.set;
         this.init_J = rev;
         this.read_Str = scopeKey;
         this.get = avail;
         this.set = total;
         List<autismclient.gui.b.CachePool.Inner_DataUtil> out = autismclient.gui.b.CachePool.init(
            this.font, autismclient.util.g.PacketUtil.init_PacketUtil().init_List_Set(scope), avail, total, this::read_Str_2, cache_I()
         );
         this.write_List = out;
         if (preserveScrolledPosition && out.size() > previousRows) {
            this.put = this.put + (out.size() - previousRows);
         }
      }
   }

   private int reset_I_2() {
      Set<String> scope = this.init_Set_3();
      return scope.isEmpty() ? Math.max(1, this.init_Map.size()) : scope.size();
   }

   private String read_Str_2(String id) {
      autismclient.util.g.ClassCache.Inner_FieldCache snapshot = this.init_ClassCache_FieldCa_Str(id);
      return snapshot == null ? id : snapshot.load_Str();
   }

   private void init(GuiGraphicsExtractor graphics, FormattedText line, int x0, int y) {
      autismclient.gui.b.CachePool.init(graphics, this.font, line, x0, y);
   }

   private boolean load(double mx, double my) {
      if (!(mx < this.add) && !(mx > this.clear)) {
         for (TableIndex.Inner_DataUtil row : this.read_List) {
            if (my >= row.load() && my < row.load() + 12) {
               int relativeX = (int)(mx - row.read());
               ClickEvent kind = relativeX >= 0 ? this.init_ClickEvent_FormattedText_I(row.init_FormattedText(), relativeX) : null;
               if (kind == null) {
                  return false;
               }

               this.init(row.init_PacketUtil_BufferH(), row.init_I(), relativeX, kind);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private ClickEvent init_ClickEvent_FormattedText_I(FormattedText line, int relativeX) {
      return autismclient.gui.b.CachePool.init(this.font, line, relativeX);
   }

   private void init(autismclient.util.g.PacketUtil.Inner_BufferHelper line, int lineIndex, int relativeX, ClickEvent kind) {
      switch (kind) {
         case RunCommand command:
            int ran = autismclient.gui.b.CachePool.init(
               this.font, line, lineIndex, relativeX, this.stop, this.reset_I_2(), this::read_Str_2, cache_I(), command
            );
            this.tick_Str = ran > 0 ? "Clicked " + ran : "Clicked";
            this.level = ValueSink.open;
            break;
         case SuggestCommand suggest:
            if (this.init_EditBox != null) {
               this.init_EditBox.setValue(suggest.command());
            }
            break;
         case CopyToClipboard copy:
            if (this.minecraft != null) {
               this.minecraft.keyboardHandler.setClipboard(copy.value());
            }

            this.tick_Str = "Copied";
            this.level = ValueSink.close;
            break;
         case OpenUrl url:
            autismclient.gui.b.CachePool.init(url.uri());
            this.tick_Str = "Opening link";
            this.level = ValueSink.open;
            break;
         default:
      }
   }

   private String init_Str_J_2(long time) {
      return "[" + LocalTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(init_DateTimeFormatter) + "] ";
   }

   @Override
   public void onClose() {
      if (this.init_k != null && this.init_k.init_Z()) {
         autismclient.util.g.RenderState.init_V();
      }

      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private StreamGate init_StreamGate_I(int x, int y, int w, int h, String text, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      if (w <= 0) {
         w = this.init_I_Str_17(text);
      }

      String label = this.init_Str_Str_I_2(text, w - 8);
      autismclient.gui.f.b.CachePool.Inner_DataUtil interactiveTone = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.load
         : tone;
      StreamGate button = new StreamGate(x, y, Math.max(1, w), h, Component.literal(label), interactiveTone, press);
      this.addRenderableWidget(button);
      return button;
   }

   private int init_I_Str_17(String label) {
      return autismclient.gui.f.RenderState.load(
         autismclient.gui.f.b.k.init(this.font, autismclient.util.g.PacketUtil.update(label, 64), init_CharEncoder.init_Identifier_l(l.read), value_I())
      );
   }

   private String init_Str_Str_I_2(String text, int width) {
      return autismclient.gui.f.b.k.load(
         this.font, autismclient.util.g.PacketUtil.update(text, 64), Math.max(1, width), init_CharEncoder.init_Identifier_l(l.read), value_I()
      );
   }

   private int start_I_2() {
      int room = this.init_I() - 12 - 4 - 160;
      return Math.max(120, Math.min(300, room));
   }

   private int stop_I_2() {
      return 6 + this.start_I_2() + 4;
   }

   private int next_I_2() {
      return Math.max(1, this.init_I() - this.stop_I_2() - 6);
   }

   private void cache_V() {
      if (this.init_EditBox != null) {
         this.init_EditBox.setFocused(false);
      }

      if (this.init_k != null) {
         this.init_k.push(false);
      }

      this.setFocused(null);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(graphics, this.font, autismclient.util.g.PacketUtil.update(text, 180), fontId, color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int width, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      String line = autismclient.gui.f.b.k.load(this.font, autismclient.util.g.PacketUtil.update(text, 160), Math.max(1, width), fontId, color);
      autismclient.gui.f.b.k.init(graphics, this.font, line, fontId, color, x, y, false);
   }

   private static String init_Str_ClassCache_FieldCa(autismclient.util.g.ClassCache.Inner_FieldCache snapshot) {
      if (snapshot == null) {
         return "";
      }

      String proxy = autismclient.util.g.PacketUtil.update(snapshot.read_Str(), 24);
      String status = load_Str_ClassCache_FieldCa(snapshot);
      return proxy.isBlank() ? status : proxy + " - " + status;
   }

   private static String load_Str_ClassCache_FieldCa(autismclient.util.g.ClassCache.Inner_FieldCache snapshot) {
      String var10000;
      switch (snapshot.init_ClassCache_PathHel()) {
         case init:
            var10000 = "Queued";
            break;
         case load:
            var10000 = "Auth";
            break;
         case read:
            var10000 = "Connecting";
            break;
         case write:
            var10000 = "Login";
            break;
         case update:
            var10000 = "Configuring";
            break;
         case sync:
            var10000 = "Joining";
            break;
         case flush:
            var10000 = "Ready";
            break;
         case close:
            var10000 = "Disconnected";
            break;
         case open:
            var10000 = "Failed";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_PacketUtil_CachePo_2(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      if (result == null) {
         return "Failed";
      } else if (result.read() > 0) {
         return "Failed";
      } else if (result.init_I() > 0 && result.load() == 0) {
         return "Sent";
      } else if (result.init_I() > 0) {
         return "Sent " + result.init_I();
      } else {
         return result.load() > 0 ? "Skipped" : "Failed";
      }
   }

   private static int prev_I_2() {
      return ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update);
   }

   private static int valid_I_2() {
      return ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close);
   }

   private static int size_I() {
      return ct.init_I_I_ct_DataUtil(-1206840035, ct.Inner_DataUtil.close);
   }

   private static int data_I() {
      return ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
   }

   private static int value_I() {
      return ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
   }

   private static int cache_I() {
      return ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
   }

   private static int state() {
      return ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync);
   }

   private static int queue() {
      return ct.init_I_I_ct_DataUtil(ValueSink.poll, ct.Inner_DataUtil.flush);
   }

   private static int load_I_I_5(int color) {
      if (color == ValueSink.open) {
         return state();
      } else if (color == ValueSink.poll) {
         return queue();
      } else {
         return color == ValueSink.flush ? value_I() : cache_I();
      }
   }

   private autismclient.util.g.ClassCache.Inner_FieldCache init_ClassCache_FieldCa_Str(String accountId) {
      autismclient.util.g.ClassCache.Inner_FieldCache cached = this.init_Map.get(accountId);
      if (cached != null) {
         return cached;
      }

      for (autismclient.util.g.ClassCache.Inner_FieldCache snapshot : autismclient.util.g.PacketUtil.init_PacketUtil().load_List()) {
         if (snapshot.init_Str().equals(accountId)) {
            return snapshot;
         }
      }

      return null;
   }

   private record Inner_DataUtil(
      autismclient.util.g.PacketUtil.Inner_BufferHelper init_PacketUtil_BufferH,
      int init_I,
      int load,
      int read,
      FormattedText init_FormattedText,
      String init_Str
   ) {
   }

   private record Inner_StreamUtil(String init_Str, int init_I, int load) {
   }
}
