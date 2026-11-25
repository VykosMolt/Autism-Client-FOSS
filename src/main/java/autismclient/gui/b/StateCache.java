package autismclient.gui.b;

import autismclient.compat263.CompatGLFW;
import autismclient.gui.f.HashUtil;
import autismclient.gui.f.b.BitSetUtil;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.UtilCore;
import autismclient.gui.f.b.c;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.gui.f.c.RingBuffer;
import autismclient.util.AutismConfig;
import autismclient.util.MethodUtil;
import autismclient.util.TokenStore;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.ProxyEntry;
import autismclient.util.ProxyManager;
import autismclient.util.bi;
import autismclient.util.bw;
import autismclient.util.cY;
import autismclient.util.ct;
import autismclient.util.g.ClassCache;
import autismclient.util.g.FieldCache;
import autismclient.util.g.IoWorker;
import autismclient.util.g.ListPool;
import autismclient.util.g.PacketUtil;
import autismclient.util.g.RenderState;
import autismclient.util.g.TaskTimer;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public final class StateCache {
   private static final int init_I = 705827348;
   private static final int load_I = 403771667;
   private static final int read_I = -12174784;
   private static final int write_I = -46518;
   private static final int update_I = -13248397;
   private static final int sync_I = -42149;
   private static final int flush_I = -855310;
   private static final int close_I = -6645094;
   private static final int open_I = -14249;
   private static final int poll_I = 2;
   private static final int push_I = 2;
   private static final int pop_I = 11;
   private static final int tick_I = 3;
   private static final int run_I = 13;
   private static final int get_I = -39220;
   private static final int set_I = -42406;
   private static final int put_I = -866997;
   private static final Identifier init_Identifier = Identifier.withDefaultNamespace("hud/heart/full");
   private static final Identifier load_Identifier = Identifier.withDefaultNamespace("hud/food_full");
   private static final int add = 11;
   private static final DateTimeFormatter init_DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static final int[] init_IArr = new int[]{50, 100, 200, 300, 500, 1000};
   private static final List<String> init_List = List.of("Gentle", "Balanced", "Fast", "Immediate", "Custom");
   private static final List<String> load_List = List.of("Off", "Auto", "Manual");
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final StateCache.Inner_CachePool init_StateCache_CachePo;
   private final Font init_Font;
   private final CharEncoder init_CharEncoder = new CharEncoder();
   private final List<StateCache.Inner_BufferHelper> read_List = new ArrayList<>();
   private final List<BitSetUtil> write_List = new ArrayList<>();
   private final EnumSet<MethodUtil> init_EnumSet = EnumSet.allOf(MethodUtil.class);
   private final LinkedHashSet<String> init_LinkedHashSet = new LinkedHashSet<>();
   private final List<autismclient.gui.f.b.SyncManager> update_List = new ArrayList<>();
   private final autismclient.gui.f.b.SyncManager init_SyncManager;
   private final autismclient.gui.f.b.SyncManager load_SyncManager;
   private final autismclient.gui.f.b.SyncManager read_SyncManager;
   private final BitSetUtil init_BitSetUtil = this.init_BitSetUtil_Str_I("Profile name", 64);
   private final BitSetUtil load_BitSetUtil = this.init_BitSetUtil_Str_I("server.example:25565", 160);
   private final BitSetUtil read_BitSetUtil = this.init_BitSetUtil_Str_I("Concurrency", 4);
   private final BitSetUtil write_BitSetUtil = this.init_BitSetUtil_Str_I("Delay ms", 5);
   private final BitSetUtil update_BitSetUtil = this.init_BitSetUtil_Str_I("Max ping", 4);
   private final BitSetUtil sync_BitSetUtil = this.init_BitSetUtil_Str_I("Search accounts", 48);
   private final BitSetUtil flush_BitSetUtil = this.init_BitSetUtil_Str_I("0", 4).load_BitSetUtil_I(3).init_BitSetUtil_Predicate(RenderState::init_Z_Str);
   private final BitSetUtil close_BitSetUtil = this.init_BitSetUtil_Str_I("Chat or /command", 256);
   private final List<String> sync_List = new ArrayList<>();
   private final List<int[]> flush_List = new ArrayList<>();
   private int clear;
   private String init_Str;
   private String load_Str;
   private int reset;
   private int start;
   private String read_Str;
   private long init_J;
   private final BitSetUtil open_BitSetUtil = this.init_BitSetUtil_Str_I("Search macros", 48);
   private final BitSetUtil poll_BitSetUtil = this.init_BitSetUtil_Str_I("Action name", 32);
   private final BitSetUtil push_BitSetUtil = this.init_BitSetUtil_Str_I("Packet args", 2048);
   private StateCache.Inner_JsonUtil init_StateCache_JsonUti = StateCache.Inner_JsonUtil.init;
   private FieldCache init_FieldCache;
   private long load_J;
   private String write_Str;
   private String update_Str = "";
   private String sync_Str = "";
   private String flush_Str = "";
   private String close_Str = "";
   private boolean init_Z;
   private final LinkedHashSet<String> load_LinkedHashSet = new LinkedHashSet<>();
   private int stop;
   private String open_Str = "";
   private String poll_Str;
   private int next = -6645094;
   private long read_J;
   private int prev;
   private int valid;
   private int size;
   private int data;
   private boolean load_Z;
   private int value;
   private int cache;
   private int state;
   private String push_Str;
   private int queue;
   private int buffer;
   private int entry;
   private int node;
   private IntConsumer init_IntConsumer;
   private StateCache.Inner_NetHelper init_StateCache_NetHelp;
   private Runnable init_Runnable;
   private int task;
   private int pool;
   private int index;
   private int count = -1;
   private int flags;
   private ListPool init_ListPool;
   private float init_F;
   private int level;
   private int frame;
   private int block;
   private int chunk;
   private int table;
   private int store;
   private int drain;
   private int fill;
   private StateCache.Inner_StateCache init_StateCache_StateCa;
   private StateCache.Inner_StateCache load_StateCache_StateCa;
   private StateCache.Inner_StateCache read_StateCache_StateCa;
   private StateCache.Inner_StateCache write_StateCache_StateCa;
   private StateCache.Inner_StateCache update_StateCache_StateCa;
   private StateCache.Inner_StateCache sync_StateCache_StateCa;
   private StateCache.Inner_StateCache flush_StateCache_StateCa;
   private StateCache.Inner_StateCache close_StateCache_StateCa;
   private boolean read_Z;
   private boolean write_Z;
   private StateCache.Inner_StateCache open_StateCache_StateCa;
   private final List<StateCache.Inner_StreamUtil> close_List;
   private final List<StateCache.Inner_TaskQueue> open_List;
   private String pop_Str;
   private int mark;
   private int scan;
   private final List<int[]> poll_List;
   private final BufferHelper init_BufferHelper;
   private boolean update_Z;
   private List<ClassCache.Inner_FieldCache> push_List;
   private FieldCache load_FieldCache;
   private int parse;
   private final Set<String> init_Set;
   private long write_J;
   private List<FieldCache> pop_List;
   private long update_J;
   private long sync_J;
   private FieldCache read_FieldCache;
   private long flush_J;
   private String tick_Str;
   private int bind;
   private int f_a_I;
   private List<CachePool.Inner_DataUtil> tick_List;
   private int f_b_I;
   private int f_c_I;
   private int f_d_I;
   private long close_J;
   private String run_Str;
   private List<cY.Inner_DataUtil> run_List;
   private int f_e_I;
   private long open_J;
   private List<autismclient.util.BitSetUtil> get_List;
   private String get_Str;
   private int f_f_I;
   private FieldCache write_FieldCache;
   private int f_g_I;
   private long poll_J;
   private List<StateCache.Inner_DataUtil> set_List;
   private long push_J;
   private String set_Str;
   private List<String> put_List;

   public StateCache(StateCache.Inner_CachePool host, Font font) {
      this.init_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.load_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.read_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.write_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.update_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.sync_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.flush_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.close_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.close_List = new ArrayList<>();
      this.open_List = new ArrayList<>();
      this.pop_Str = "";
      this.poll_List = new ArrayList<>();
      this.init_BufferHelper = new BufferHelper();
      this.push_List = List.of();
      this.init_Set = new HashSet<>(1000);
      this.write_J = -9223372036854775808L;
      this.pop_List = List.of();
      this.update_J = -9223372036854775808L;
      this.sync_J = -9223372036854775808L;
      this.flush_J = -9223372036854775808L;
      this.tick_Str = "";
      this.bind = -1;
      this.f_a_I = -1;
      this.tick_List = List.of();
      this.close_J = -9223372036854775808L;
      this.run_Str = "";
      this.run_List = List.of();
      this.f_e_I = -1;
      this.open_J = -9223372036854775808L;
      this.get_List = List.of();
      this.get_Str = "";
      this.f_f_I = -2147483648;
      this.f_g_I = -1;
      this.poll_J = -9223372036854775808L;
      this.set_List = List.of();
      this.push_J = -9223372036854775808L;
      this.set_Str = "";
      this.put_List = List.of();
      this.init_StateCache_CachePo = host;
      this.init_Font = font;
      this.init_SyncManager = new autismclient.gui.f.b.SyncManager(0, 0, 1, 1, init_List, 0, this::load_V_I);
      this.load_SyncManager = new autismclient.gui.f.b.SyncManager(0, 0, 1, 1, load_List, 0, this::read);
      this.read_SyncManager = new autismclient.gui.f.b.SyncManager(0, 0, 1, 1, write_List(), 1, this::write);
      this.close_BitSetUtil.load_BitSetUtil_Consumer(this::update_V_Str);
      this.flush_BitSetUtil.init_BitSetUtil_Consumer(this::close);
      this.flush_BitSetUtil.sync(true);
      this.flush_BitSetUtil.load_BitSetUtil_Consumer(text -> {
         this.close(text);
         RenderState.init_V();
         this.flush_BitSetUtil.init_V_Z(false);
      });
      this.close_BitSetUtil.flush(true);
      this.close_BitSetUtil.init_BitSetUtil_Supplier(() -> PacketUtil.init_PacketUtil().init_List());
      this.update_V();
   }

   private BitSetUtil init_BitSetUtil_Str_I(String placeholder, int maxLength) {
      return new BitSetUtil().load_BitSetUtil_Str(placeholder).init_BitSetUtil_I(maxLength).read_BitSetUtil_I(read_I());
   }

   public void init_V() {
      this.f_e_I = -1;
      this.write_FieldCache = null;
      if (!PacketUtil.init_PacketUtil().load_Z()) {
         FieldCache shared = TaskTimer.init_TaskTimer().init_FieldCache_Str(TaskTimer.init_TaskTimer().read_Str());
         if (shared != null && !shared.init_CompoundTag().toString().equals(this.init_FieldCache.init_CompoundTag().toString())) {
            this.init_V_FieldCache(shared);
         }
      }

      if (!PacketUtil.init_PacketUtil().load_Z() && this.init_FieldCache.update_Str.isBlank()) {
         this.get();
      }

      if (!PacketUtil.init_PacketUtil().load_Z() && this.init_FieldCache.init_List.isEmpty()) {
         this.add();
      }
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my, float partial) {
      this.flags();
      this.open_V();
      this.read_List.clear();
      this.write_List.clear();
      this.poll_Str = null;
      boolean dropdownOpen = autismclient.gui.f.b.SyncManager.init_Z_List(this.update_List);
      this.update_List.clear();
      this.init_F = partial;
      int contentMx = dropdownOpen ? -2147483648 : mx;
      int contentMy = dropdownOpen ? -2147483648 : my;
      this.level = contentMx;
      this.frame = contentMy;
      this.block = x;
      this.chunk = y;
      this.table = w;
      this.store = h;
      this.drain = y + 2 + read_I() + 4;
      this.fill = y + h - 2;
      this.init_StateCache_StateCa = this.load_StateCache_StateCa = this.read_StateCache_StateCa = this.write_StateCache_StateCa = this.update_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      this.sync_StateCache_StateCa = this.flush_StateCache_StateCa = this.close_StateCache_StateCa = this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
      PacketUtil manager = PacketUtil.init_PacketUtil();
      if (manager.load_Z()) {
         this.push_List = manager.load_List();
         this.load_FieldCache = this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.read ? null : this.init_FieldCache_PacketUtil(manager);
         this.init_Set.clear();
         this.parse = 0;

         for (ClassCache.Inner_FieldCache snapshot : this.push_List) {
            this.init_Set.add(snapshot.init_Str());
            if (snapshot.load_Z()) {
               this.parse++;
            }
         }

         this.init_LinkedHashSet.retainAll(this.init_Set);
      } else {
         this.push_List = List.of();
         this.load_FieldCache = null;
         this.parse = 0;
         this.init_LinkedHashSet.clear();
         this.value = 0;
      }

      this.init_V_GuiGraphicsExtract(g, x + 2, y + 2, w - 4, contentMx, contentMy);
      switch (this.init_StateCache_JsonUti) {
         case init:
            this.init_V_GuiGraphicsExtract(g, contentMx, contentMy);
            break;
         case load:
            this.load(g, contentMx, contentMy);
            break;
         case read:
            this.read(g, contentMx, contentMy);
            break;
         case write:
            this.update(g, contentMx, contentMy);
            break;
         case update:
            this.sync(g, contentMx, contentMy);
      }

      if (this.count >= 0) {
         this.flush(g, contentMx, contentMy);
      } else if (!this.close_Str.isBlank()) {
         this.close(g, contentMx, contentMy);
      } else if (this.init_Z) {
         this.open(g, contentMx, contentMy);
      }

      autismclient.gui.f.b.SyncManager.read(g, this.init_Font, this.update_List, mx, my);
      if (this.poll_Str != null && AutismConfig.getGlobal().multiShowTooltips) {
         UiHelper.init(g, this.init_Font, this.poll_Str, mx, my);
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
      StateCache.Inner_JsonUtil[] tabs = StateCache.Inner_JsonUtil.values();
      String[] labels = new String[]{"Setup", "Accounts", "Console", "Actions", "Macros"};
      int each = (w - 2 * (tabs.length - 1)) / tabs.length;
      int cx = x;

      for (int i = 0; i < tabs.length; i++) {
         int cw = i == tabs.length - 1 ? x + w - cx : each;
         StateCache.Inner_JsonUtil target = tabs[i];
         this.init_V_GuiGraphicsExtract_2(
            g, cx, y, cw, read_I(), labels[i], this.init_StateCache_JsonUti == target ? this.poll_I() : this.close_I(), this.pop_I(), mx, my, () -> {
               this.init_StateCache_JsonUti = target;
               this.read_V();
            }
         );
         cx += cw + 2;
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int mx, int my) {
      boolean active = PacketUtil.init_PacketUtil().load_Z();
      int ch = read_I();
      int lh = sync_I();
      int leftW = Math.max(105, Math.min(142, this.table / 3));
      int lx = this.block + 2;
      int rx = lx + leftW + 4;
      int rw = this.block + this.table - 2 - rx;
      int top = this.drain;
      this.init_V_GuiGraphicsExtract_2(g, "Saved Profiles", lx, top, this.pop_I(), false, leftW);
      int listTop = top + lh;
      int newW = this.init_I_Str("New");
      int duplicateW = this.init_I_Str("Duplicate");
      boolean oneButtonRow = newW + 2 + duplicateW <= leftW;
      int listBottom = this.fill - (oneButtonRow ? ch : ch * 2 + 2) - 2;
      this.init_StateCache_StateCa = new StateCache.Inner_StateCache(lx, listTop, leftW, Math.max(1, listBottom - listTop));
      List<FieldCache> profiles = this.read_List();
      int pitch = update_I();
      int maxScroll = Math.max(0, init_I_I(profiles.size()) - this.init_StateCache_StateCa.write());
      this.prev = init_I_I(this.prev, 0, maxScroll);
      HashUtil.init_HashUtil().init(g, this.init_StateCache_StateCa.init_NetHelper());
      this.open_StateCache_StateCa = this.init_StateCache_StateCa;
      try {
         int py = listTop - this.prev;

         for (FieldCache profile : profiles) {
            if (py + pitch > listTop && py < listBottom) {
               boolean selected = profile.read_Str.equals(this.update_Str);
               this.init_V_GuiGraphicsExtract(
                  g,
                  lx,
                  py,
                  leftW,
                  write_I(),
                  profile.write_Str,
                  selected ? this.poll_I() : this.close_I(),
                  mx,
                  my,
                  active ? null : () -> this.load_V_FieldCache(profile)
               );
            }

            py += pitch;
         }
      } finally {
         this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.init_StateCache_StateCa, this.prev, maxScroll, mx, my, "profile", v -> this.prev = v);
      if (oneButtonRow) {
         this.init_V_GuiGraphicsExtract_2(g, lx, this.fill - ch, newW, ch, "New", this.close_I(), this.pop_I(), mx, my, active ? null : this::sync_V);
         this.init_V_GuiGraphicsExtract_2(
            g, lx + newW + 2, this.fill - ch, duplicateW, ch, "Duplicate", this.close_I(), this.pop_I(), mx, my, active ? null : this::poll_V
         );
      } else {
         this.init_V_GuiGraphicsExtract_2(g, lx, this.fill - ch * 2 - 2, newW, ch, "New", this.close_I(), this.pop_I(), mx, my, active ? null : this::sync_V);
         this.init_V_GuiGraphicsExtract_2(
            g, lx, this.fill - ch, duplicateW, ch, "Duplicate", this.close_I(), this.pop_I(), mx, my, active ? null : this::poll_V
         );
      }

      boolean savedFlash = !active && System.currentTimeMillis() - this.read_J < 1500L;
      int savedW = this.init_I_Str_I("Saved", this.poll_I());
      this.init_V_GuiGraphicsExtract_2(
         g, active ? "Active Batch" : "Profile Setup", rx, top, active ? this.poll_I() : this.pop_I(), false, savedFlash ? rw - savedW - 4 : rw
      );
      if (savedFlash) {
         this.init_V_GuiGraphicsExtract_2(g, "Saved", rx + rw - savedW, top, this.poll_I(), false, savedW);
      }

      int detailTop = top + lh;
      this.load_StateCache_StateCa = new StateCache.Inner_StateCache(rx, detailTop, rw, Math.max(1, this.fill - detailTop));
      int detailHeight = this.init_I_Z(active);
      this.valid = init_I_I(this.valid, 0, Math.max(0, detailHeight - this.load_StateCache_StateCa.write()));
      HashUtil.init_HashUtil().init(g, this.load_StateCache_StateCa.init_NetHelper());
      this.open_StateCache_StateCa = this.load_StateCache_StateCa;
      try {
         int cy = detailTop - this.valid;
         if (active) {
            FieldCache current = this.load_FieldCache;
            this.init_V_GuiGraphicsExtract_2(g, current == null ? "Multi" : current.write_Str, rx, cy, this.pop_I(), false, rw);
            cy += lh;
            this.init_V_GuiGraphicsExtract_2(g, current == null ? "" : current.update_Str, rx, cy, this.tick_I(), false, rw);
            cy += lh + 2;
            this.init_V_GuiGraphicsExtract_2(g, this.parse + "/" + this.push_List.size() + " ready", rx, cy, this.poll_I(), false, rw);
            cy += lh + 4;
            int disconnectW = Math.min(this.init_I_Str("Disconnect All"), rw);
            this.init_V_GuiGraphicsExtract_2(g, rx + (rw - disconnectW) / 2, cy, disconnectW, ch, "Disconnect All", this.push_I(), this.pop_I(), mx, my, () -> {
               PacketUtil.init_PacketUtil().load_V_Str("Disconnected by user");
               this.init_V_Str_I("Disconnected", -6645094);
            });
            cy += ch + 4;
            this.init_V_GuiGraphicsExtract_2(g, "Disconnect to edit config.", rx, cy, this.tick_I(), false, rw);
            cy += lh;
         } else {
            this.init_V_GuiGraphicsExtract(g, this.init_BitSetUtil, rx, cy, rw);
            cy += ch + 2;
            int useCurrentW = this.init_I_Str("Use Current");
            this.init_V_GuiGraphicsExtract(g, this.load_BitSetUtil, rx, cy, Math.max(10, rw - useCurrentW - 2));
            this.init_V_GuiGraphicsExtract_2(g, rx + rw - useCurrentW, cy, useCurrentW, ch, "Use Current", this.close_I(), this.pop_I(), mx, my, this::get);
            cy += ch + 2;
            int half = (rw - 2) / 2;
            if (this.init_FieldCache.init_FieldCache_CachePo == FieldCache.Inner_CachePool.load) {
               int third = Math.max(1, (rw - 4) / 3);
               this.init_V_SyncManager(
                  this.init_SyncManager,
                  rx,
                  cy,
                  third,
                  this.init_FieldCache.init_FieldCache_StreamU.ordinal(),
                  "Join speed: " + this.init_FieldCache.init_FieldCache_StreamU.name()
               );
               this.init_V_SyncManager(
                  this.load_SyncManager, rx + third + 2, cy, third, this.init_FieldCache.init_FieldCache_CachePo.ordinal(), "Proxy: " + this.init_Str()
               );
               this.init_V_SyncManager(
                  this.read_SyncManager,
                  rx + (third + 2) * 2,
                  cy,
                  rw - (third + 2) * 2,
                  load_I_I(this.init_FieldCache.update_I),
                  "<=" + this.init_FieldCache.update_I + "ms"
               );
            } else {
               this.init_V_SyncManager(
                  this.init_SyncManager,
                  rx,
                  cy,
                  half,
                  this.init_FieldCache.init_FieldCache_StreamU.ordinal(),
                  "Join speed: " + this.init_FieldCache.init_FieldCache_StreamU.name()
               );
               this.init_V_SyncManager(
                  this.load_SyncManager, rx + half + 2, cy, rw - half - 2, this.init_FieldCache.init_FieldCache_CachePo.ordinal(), "Proxy: " + this.init_Str()
               );
            }

            cy += ch + 2;
            autismclient.gui.f.b.SyncManager.load(g, this.init_Font, this.update_List, mx, my);
            this.init_V_GuiGraphicsExtract_2(g, rx, cy, rw, ch, "Passwords (login)", this.close_I(), this.pop_I(), mx, my, this::run_V);
            cy += ch + 2;
            if (this.init_FieldCache.init_FieldCache_StreamU == FieldCache.Inner_StreamUtil.update) {
               this.init_V_GuiGraphicsExtract(g, this.read_BitSetUtil, rx, cy, half);
               this.init_V_GuiGraphicsExtract(g, this.write_BitSetUtil, rx + half + 2, cy, rw - half - 2);
               cy += ch + 2;
            }

            this.init_V_GuiGraphicsExtract_2(
               g,
               this.init_FieldCache.init_List.size() + " accounts selected",
               rx,
               cy,
               this.init_FieldCache.init_List.isEmpty() ? this.push_I() : this.tick_I(),
               false,
               rw
            );
            cy += lh + 2;
            String deleteLabel = this.flush_Str.equals(this.update_Str) ? "Confirm" : "Delete";
            String resetLabel = this.flush_Str.equals("reset:" + this.update_Str) ? "Confirm" : "Reset";
            int resetW = this.init_I_Str(resetLabel);
            int deleteW = this.init_I_Str(deleteLabel);
            if (resetW + 2 + deleteW <= rw) {
               this.init_V_GuiGraphicsExtract_2(g, rx, cy, resetW, ch, resetLabel, this.close_I(), this.pop_I(), mx, my, this::flush_V);
               this.init_V_GuiGraphicsExtract_2(g, rx + resetW + 2, cy, deleteW, ch, deleteLabel, this.push_I(), this.pop_I(), mx, my, this::push_V);
            } else {
               int halfBtn = (rw - 2) / 2;
               this.init_V_GuiGraphicsExtract_2(g, rx, cy, halfBtn, ch, resetLabel, this.close_I(), this.pop_I(), mx, my, this::flush_V);
               this.init_V_GuiGraphicsExtract_2(g, rx + halfBtn + 2, cy, rw - halfBtn - 2, ch, deleteLabel, this.push_I(), this.pop_I(), mx, my, this::push_V);
            }

            cy += ch + 4;
            int launchW = Math.min(this.init_I_Str("Launch Multi"), rw);
            this.init_V_GuiGraphicsExtract_2(g, rx + (rw - launchW) / 2, cy, launchW, ch, "Launch Multi", this.poll_I(), this.pop_I(), mx, my, this::pop_V);
            cy += ch + 2;
         }

         if (!this.open_Str.isBlank()) {
            this.init_V_GuiGraphicsExtract_2(g, this.open_Str, rx, cy, this.flush_I(), false, rw);
         }
      } finally {
         this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(
         g,
         this.load_StateCache_StateCa,
         this.valid,
         Math.max(0, detailHeight - this.load_StateCache_StateCa.write()),
         mx,
         my,
         "setupDetail",
         v -> this.valid = v
      );
   }

   private int init_I_Z(boolean active) {
      int ch = read_I();
      int lh = sync_I();
      int section = 4;
      int statusH = this.open_Str.isBlank() ? 0 : lh;
      if (active) {
         return lh + lh + 2 + lh + section + ch + section + lh + statusH;
      }

      int height = (ch + 2) * 4 + lh + 2 + ch + section + ch + 2;
      if (this.init_FieldCache.init_FieldCache_StreamU == FieldCache.Inner_StreamUtil.update) {
         height += ch + 2;
      }

      return height + statusH;
   }

   private void load(GuiGraphicsExtractor g, int mx, int my) {
      boolean active = PacketUtil.init_PacketUtil().load_Z();
      FieldCache shownProfile = active && this.load_FieldCache != null ? this.load_FieldCache : this.init_FieldCache;
      int ch = read_I();
      int x = this.block + 2;
      int w = this.table - 4;
      int y = this.drain;
      int manageW = this.init_I_Str("Manage Accounts");
      this.init_V_GuiGraphicsExtract(g, this.sync_BitSetUtil, x, y, Math.max(10, w - manageW - 2));
      this.init_V_GuiGraphicsExtract_2(
         g, x + w - manageW, y, manageW, ch, "Manage Accounts", this.close_I(), this.pop_I(), mx, my, this.init_StateCache_CachePo::init_V
      );
      y += ch + 2;
      MethodUtil[] types = new MethodUtil[]{MethodUtil.init, MethodUtil.load, MethodUtil.read, MethodUtil.write, MethodUtil.update};
      String[] labels = new String[]{"Cracked", "Session", "Microsoft", "Altening", "Generated"};
      int each = (w - 8) / 5;
      int cx = x;

      for (int i = 0; i < types.length; i++) {
         MethodUtil type = types[i];
         this.init_V_GuiGraphicsExtract_2(g, cx, y, i == types.length - 1 ? x + w - cx : each, ch, labels[i], this.init_EnumSet.contains(type), mx, my, () -> {
            if (!this.init_EnumSet.remove(type)) {
               this.init_EnumSet.add(type);
            }

            this.size = 0;
         });
         cx += each + 2;
      }

      y += ch + 4;
      if (!active && shownProfile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read) {
         String commonProxy = init_Str_FieldCache(shownProfile);
         Map<String, Integer> profileUsage = init_Map_FieldCache(shownProfile);
         String bulkLabel = commonProxy == null ? "Set All Proxies (Mixed)..." : "Set All Proxies: " + this.init_Str_FieldCache_Str(shownProfile, commonProxy);
         this.init_V_GuiGraphicsExtract_2(
            g,
            x,
            y,
            w,
            ch,
            bulkLabel,
            this.close_I(),
            this.pop_I(),
            mx,
            my,
            () -> this.init_StateCache_CachePo.init_V_Str("Proxy for all selected accounts", shownProfile.update_Str, commonProxy, profileUsage, this::read_V)
         );
         y += ch + 2;
      }

      int pitch = update_I();
      int listH = Math.max(pitch, this.fill - y);
      this.read_StateCache_StateCa = new StateCache.Inner_StateCache(x, y, w, listH);
      List<StateCache.Inner_DataUtil> choices = this.init_List_FieldCache(shownProfile);
      int maxScroll = Math.max(0, init_I_I(choices.size()) - listH);
      this.size = init_I_I(this.size, 0, maxScroll);
      HashUtil.init_HashUtil().init(g, this.read_StateCache_StateCa.init_NetHelper());
      this.open_StateCache_StateCa = this.read_StateCache_StateCa;
      try {
         int ry = y - this.size;
         Map<String, Integer> profileUsage = !active && shownProfile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read
            ? init_Map_FieldCache(shownProfile)
            : Map.of();

         for (StateCache.Inner_DataUtil choice : choices) {
            if (ry + pitch > y && ry < y + listH) {
               FieldCache.Inner_BufferHelper spec = init_FieldCache_BufferH_FieldCache_Str(shownProfile, choice.init_Str());
               boolean blocked = choice.init_Z();
               boolean selected = spec != null;
               int proxyW = Math.min(116, Math.max(76, w / 3));
               int accountW = w - proxyW - 2;
               String label = choice.load() + (blocked ? " (Current)" : "");
               this.init_V_GuiGraphicsExtract(
                  g,
                  x,
                  ry,
                  accountW,
                  write_I(),
                  label,
                  blocked ? this.push_I() : (selected ? this.poll_I() : this.close_I()),
                  mx,
                  my,
                  !active && !blocked ? () -> this.load_V_Str(choice.init_Str()) : null
               );
               String proxy = spec == null ? "-" : this.init_Str_FieldCache_Str(shownProfile, spec.load_Str());
               boolean pickable = !active && spec != null && !choice.init_Z() && shownProfile.init_FieldCache_CachePo == FieldCache.Inner_CachePool.read;
               if (pickable) {
                  this.init_V_GuiGraphicsExtract_2(
                     g,
                     x + accountW + 2,
                     ry,
                     proxyW,
                     write_I(),
                     proxy,
                     this.close_I(),
                     this.pop_I(),
                     mx,
                     my,
                     () -> this.init_StateCache_CachePo
                        .init_V_Str(
                           "Proxy for " + choice.load(),
                           shownProfile.update_Str,
                           spec.load_Str(),
                           profileUsage,
                           proxyId -> this.init_V_Str_Str(choice.init_Str(), proxyId)
                        )
                  );
               } else {
                  this.init_V_GuiGraphicsExtract(g, x + accountW + 2, ry, proxyW, write_I(), proxy, this.close_I(), mx, my, null);
               }
            }

            ry += pitch;
         }
      } finally {
         this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
         autismclient.gui.f.b.SyncManager.load(g, this.init_Font, this.update_List, mx, my);
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.read_StateCache_StateCa, this.size, maxScroll, mx, my, "account", v -> this.size = v);
   }

   private void read(GuiGraphicsExtractor g, int mx, int my) {
      int x = this.block + 2;
      int y = this.drain;
      int w = this.table - 4;
      int h = this.fill - y;
      if (!PacketUtil.init_PacketUtil().load_Z()) {
         this.init_V_GuiGraphicsExtract_3(
            g, "No active batch", "Launch a profile from Setup.", x, y, w, h, mx, my, () -> this.init_StateCache_JsonUti = StateCache.Inner_JsonUtil.init
         );
      } else {
         List<ClassCache.Inner_FieldCache> snapshots = this.push_List;
         boolean twoPane = w >= 410;
         int listW = twoPane ? Math.max(128, Math.min(158, w / 3)) : w;
         int listH = twoPane ? h : Math.min(76, Math.max(40, h / 3));
         int split = 4;
         this.init_V_GuiGraphicsExtract_4(g, snapshots, x, y, listW, listH, mx, my);
         int chatX = twoPane ? x + listW + split : x;
         int chatY = twoPane ? y : y + listH + split;
         int chatW = twoPane ? w - listW - split : w;
         int chatH = twoPane ? h : h - listH - split;
         this.init_V_GuiGraphicsExtract(g, chatX, chatY, chatW, chatH, mx, my);
         this.write(g, mx, my);
      }
   }

   private void write(GuiGraphicsExtractor g, int mx, int my) {
      this.poll_List.clear();
      if (!this.pop_Str.isBlank()) {
         String[] items = new String[]{"TPA to Me", "TPA to Bot", "Trade"};
         int itemH = write_I();
         int menuW = 48;

         for (String item : items) {
            menuW = Math.max(menuW, this.init_I_Str_I(item, 0) + 10);
         }

         int menuH = items.length * itemH + 2;
         int menuX = Math.min(this.mark, this.block + this.table - menuW - 2);
         int menuY = Math.min(this.scan, this.chunk + this.store - menuH - 2);
         autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(menuX, menuY, menuW, menuH), 6, -267251178, this.open_I());

         for (int i = 0; i < items.length; i++) {
            int ry = menuY + 1 + i * itemH;
            boolean hover = mx >= menuX && mx < menuX + menuW && my >= ry && my < ry + itemH;
            if (hover) {
               autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(menuX + 1, ry, menuW - 2, itemH), 4, 872415231);
            }

            this.init_V_GuiGraphicsExtract_2(g, items[i], menuX + 5, this.init_I_I_I(ry, itemH), hover ? this.pop_I() : this.tick_I(), false, menuW - 8);
            this.poll_List.add(new int[]{menuX, ry, menuW, itemH, i});
         }
      }
   }

   private void init_V_I(int index) {
      String id = this.pop_Str;
      this.pop_Str = "";
      if (!id.isBlank()) {
         PacketUtil mm = PacketUtil.init_PacketUtil();
         String var10000;
         switch (index) {
            case 0:
               var10000 = mm.init_Str_Str(id);
               break;
            case 1:
               var10000 = mm.load_Str_Str(id);
               break;
            case 2:
               var10000 = mm.read_Str_Str(id);
               break;
            default:
               var10000 = "";
         }

         String result = var10000;
         if (!result.isBlank()) {
            this.init_V_Str_I(result, "Sent".equals(result) ? -13248397 : -6645094);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor g, List<ClassCache.Inner_FieldCache> snapshots, int x, int y, int w, int h, int mx, int my) {
      int ch = read_I();
      int lh = sync_I();
      int detailsW = this.init_I_Str("Details");
      this.init_V_GuiGraphicsExtract_2(
         g, "Sessions " + this.parse + "/" + snapshots.size(), x, this.init_I_I_I(y, lh - 1), this.pop_I(), false, w - detailsW - 2
      );
      this.init_V_GuiGraphicsExtract_2(g, x + w - detailsW, y, detailsW, lh - 1, "Details", this.load_Z, mx, my, () -> {
         this.load_Z = !this.load_Z;
         this.data = 0;
      });
      this.init_V_GuiGraphicsExtract_2(g, x, y + lh, w, ch, "Shared GUI", this.init_StateCache_CachePo.init_Z(), mx, my, this.init_StateCache_CachePo::load_V_2);
      int top = y + lh + ch + 2;
      int footer = y + h - ch;
      int listH = Math.max(16, footer - 2 - top);
      this.write_StateCache_StateCa = new StateCache.Inner_StateCache(x, top, w, listH);
      int totalH = 0;

      for (ClassCache.Inner_FieldCache snapshot : snapshots) {
         totalH += this.init_I_ClassCache_FieldCa(snapshot) + 2;
      }

      int maxScroll = Math.max(0, totalH - 2 - listH);
      this.data = init_I_I(this.data, 0, maxScroll);
      HashUtil.init_HashUtil().init(g, this.write_StateCache_StateCa.init_NetHelper());
      this.open_StateCache_StateCa = this.write_StateCache_StateCa;
      try {
         this.open_List.clear();
         int ry = top - this.data;
         int headH = init_I();

         for (ClassCache.Inner_FieldCache snapshot : snapshots) {
            int guiExtra = load_I_ClassCache_FieldCa(snapshot);
            int frameH = this.init_I_ClassCache_FieldCa(snapshot);
            if (ry + frameH > top && ry < top + listH) {
               int state = this.read(snapshot);
               boolean selected = this.init_LinkedHashSet.contains(snapshot.init_Str());
               autismclient.gui.f.PacketUtil.load(
                  g, autismclient.gui.f.NetHelper.init(x, ry, w, frameH), 4, selected ? load_I_I_I(-39220, 53) : 705827348, selected ? -39220 : state
               );
               this.read_List.add(new StateCache.Inner_BufferHelper(x, ry, w, frameH, () -> this.write_V(snapshot.init_Str())));
               if (this.init_Z_I(x, ry, w, frameH)) {
                  this.open_List.add(new StateCache.Inner_TaskQueue(x, ry, w, frameH, snapshot.init_Str()));
               }

               int guiW = 29;
               int guiX = x + w - guiW - 3;
               int povW = 29;
               boolean povActive = this.init_StateCache_CachePo.load_Z(snapshot.init_Str());
               boolean povOk = povActive || this.init_StateCache_CachePo.read_Z_Str(snapshot.init_Str());
               int povX = povOk ? guiX - povW - 2 : guiX;
               String ping = snapshot.init_I() >= 0 ? snapshot.init_I() + "ms" : "--ms";
               int pingW = this.init_I_Str_I(ping, this.tick_I()) + 5;
               int pingX = (povOk ? povX : guiX) - pingW - 2;
               int nameColor = state == -42406 ? -42406 : this.pop_I();
               int buttonY = ry + 2;
               int buttonH = headH - 3;
               int headTextY = this.init_I_I_I(buttonY, buttonH);
               this.init_V_GuiGraphicsExtract_2(g, snapshot.load_Str(), x + 5, headTextY, nameColor, false, Math.max(20, pingX - x - 7));
               this.init_V_GuiGraphicsExtract_2(g, ping, pingX, headTextY, snapshot.init_I() >= 0 ? this.tick_I() : this.close_I(), false, pingW);
               if (povOk) {
                  this.init_V_GuiGraphicsExtract_2(
                     g, povX, buttonY, povW, buttonH, "POV", povActive, mx, my, () -> this.init_StateCache_CachePo.load_V(snapshot.init_Str())
                  );
               }

               this.init_V_GuiGraphicsExtract_2(
                  g,
                  guiX,
                  buttonY,
                  guiW,
                  buttonH,
                  "GUI",
                  this.init_StateCache_CachePo.init_Z(snapshot.init_Str()),
                  mx,
                  my,
                  () -> this.init_StateCache_CachePo.init_V(snapshot.init_Str())
               );
               int macroY = ry + headH;
               this.init_V_GuiGraphicsExtract_2(g, snapshot, x + 5, macroY, w - 10);
               if (mx >= x && mx < x + w && my >= macroY - 1 && my < macroY + 11) {
                  this.poll_Str = ConfigStore.load(PacketUtil.init_PacketUtil(), snapshot);
               }

               if (guiExtra > 0) {
                  String gui = snapshot.sync();
                  String guiLabel = snapshot.read_Z() ? "GUI: CustomScreen" : "GUI: " + PacketUtil.update(gui == null ? "" : gui, 40);
                  this.init_V_GuiGraphicsExtract_2(g, guiLabel, x + 5, macroY + 11, this.tick_I(), false, w - 10);
               }

               if (this.load_Z) {
                  this.load(g, snapshot, x + 5, macroY + 11 + guiExtra, w - 10);
               }
            }

            ry += frameH + 2;
         }
      } finally {
         this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.write_StateCache_StateCa, this.data, maxScroll, mx, my, "session", v -> this.data = v);
      int retryW = this.init_I_Str("Retry");
      int stopW = this.init_I_Str("Stop");
      if (retryW + 2 + stopW <= w) {
         this.init_V_GuiGraphicsExtract_2(g, x, footer, retryW, ch, "Retry", this.close_I(), this.pop_I(), mx, my, this::reset);
         this.init_V_GuiGraphicsExtract_2(g, x + retryW + 2, footer, stopW, ch, "Stop", this.push_I(), this.pop_I(), mx, my, this::start);
      } else {
         int half = (w - 2) / 2;
         this.init_V_GuiGraphicsExtract_2(g, x, footer, half, ch, "Retry", this.close_I(), this.pop_I(), mx, my, this::reset);
         this.init_V_GuiGraphicsExtract_2(g, x + half + 2, footer, w - half - 2, ch, "Stop", this.push_I(), this.pop_I(), mx, my, this::start);
      }
   }

   private static int init_I() {
      return read_I() + 3;
   }

   private int init_I_ClassCache_FieldCa(ClassCache.Inner_FieldCache snapshot) {
      int details = this.load_Z ? 33 : 0;
      return init_I() + 11 + load_I_ClassCache_FieldCa(snapshot) + details + 1;
   }

   private static int load_I_ClassCache_FieldCa(ClassCache.Inner_FieldCache snapshot) {
      String gui = snapshot.sync();
      return snapshot.read_Z() || gui != null && !gui.isBlank() ? 11 : 0;
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, ClassCache.Inner_FieldCache snapshot, int x, int y, int width) {
      String assignedName = ConfigStore.init(PacketUtil.init_PacketUtil(), snapshot);
      String assigned = ConfigStore.init(assignedName);
      long now = System.currentTimeMillis();
      String trailing = ConfigStore.init_Str(snapshot, now);
      int trailingColor = -1526710;
      if (trailing.isBlank()) {
         trailing = ConfigStore.load(ConfigStore.init_Str(snapshot));
         trailingColor = -11013497;
      }

      if (trailing.isBlank()) {
         this.init_V_GuiGraphicsExtract_2(g, assigned, x, y, -6641998, false, width);
      } else {
         int gap = 7;
         int trailingW = Math.min(Math.max(42, width * 3 / 5), Math.max(1, this.init_I_Str_I(trailing, trailingColor)));
         int assignedW = Math.max(0, width - trailingW - gap);
         int cx = x;
         if (assignedW > 12) {
            this.init_V_GuiGraphicsExtract_2(g, assigned, cx, y, -6641998, false, assignedW);
            cx += assignedW + gap;
         }

         this.init_V_GuiGraphicsExtract_2(g, trailing, cx, y, trailingColor, false, Math.max(1, x + width - cx));
      }
   }

   private void load(GuiGraphicsExtractor g, ClassCache.Inner_FieldCache snapshot, int x, int y, int w) {
      int cx = x;
      g.blitSprite(RenderPipelines.GUI_TEXTURED, init_Identifier, cx, y - 1, 9, 9);
      cx += 11;
      String hp = init_Str_F(snapshot.init_F()) + "/" + init_Str_F(snapshot.load_F());
      this.init_V_GuiGraphicsExtract_2(g, hp, cx, y, this.tick_I(), false, w);
      cx += this.init_I_Str_I(hp, this.tick_I()) + 10;
      g.blitSprite(RenderPipelines.GUI_TEXTURED, load_Identifier, cx, y - 1, 9, 9);
      cx += 11;
      this.init_V_GuiGraphicsExtract_2(g, snapshot.read_I() + "/20", cx, y, this.tick_I(), false, Math.max(1, x + w - cx));
      String held = snapshot.flush() != null && !snapshot.flush().isBlank() ? snapshot.flush() : "empty";
      this.init_V_GuiGraphicsExtract_2(g, "Held " + held + "  (slot " + snapshot.load_I() + ")", x, y + 11, this.tick_I(), false, w);
      String dim = snapshot.close() != null && !snapshot.close().isBlank() ? snapshot.close() : "?";
      this.init_V_GuiGraphicsExtract_2(g, "World: " + dim, x, y + 22, this.tick_I(), false, w);
   }

   private static String init_Str_F(float value) {
      return value == Math.rint(value) ? Integer.toString((int)value) : String.format(Locale.ROOT, "%.1f", value);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my) {
      String scope = this.init_LinkedHashSet.isEmpty() ? "All" : this.init_LinkedHashSet.size() + " selected";
      this.init_V_GuiGraphicsExtract_2(g, "Console - " + scope, x, y, this.pop_I(), false, w);
      int ch = read_I();
      int inputY = y + h - ch;
      int sendW = this.init_I_Str("Send");
      this.init_V_GuiGraphicsExtract(g, this.close_BitSetUtil, x, inputY, Math.max(50, w - sendW - 2));
      this.init_V_GuiGraphicsExtract_2(
         g, x + w - sendW, inputY, sendW, ch, "Send", this.poll_I(), this.pop_I(), mx, my, () -> this.update_V_Str(this.close_BitSetUtil.init_Str())
      );
      int top = y + sync_I();
      int chatH = Math.max(10, inputY - top - 2);
      this.update_StateCache_StateCa = new StateCache.Inner_StateCache(x, top, w, chatH);
      autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, top, w, chatH), 6, 806359058, this.close_I());
      this.close_List.clear();
      int textX = x + 4;
      int timestampWidth = this.init_Font.width("[00:00:00] ");
      this.f_b_I = textX + timestampWidth;
      this.f_c_I = x + w - 4;
      this.f_d_I = Math.max(1, this.f_c_I - this.f_b_I);
      long chatRevision = PacketUtil.init_PacketUtil().read_J();
      String requestedScope = this.init_LinkedHashSet.isEmpty() ? "*" : String.join("|", this.init_LinkedHashSet);
      int total = this.load_I();
      if (chatRevision != this.flush_J || !requestedScope.equals(this.tick_Str) || this.f_d_I != this.bind || total != this.f_a_I) {
         boolean scopeChanged = !requestedScope.equals(this.tick_Str);
         int previousRows = this.tick_List.size();
         boolean preserveScroll = this.value > 0 && requestedScope.equals(this.tick_Str) && this.f_d_I == this.bind && total == this.f_a_I;
         if (scopeChanged) {
            this.value = 0;
         }

         this.flush_J = chatRevision;
         this.tick_Str = requestedScope;
         this.bind = this.f_d_I;
         this.f_a_I = total;
         this.tick_List = CachePool.init(
            this.init_Font, PacketUtil.init_PacketUtil().init_List_Set(this.init_LinkedHashSet), this.f_d_I, total, this::init_Str_Str, this.tick_I()
         );
         if (preserveScroll && this.tick_List.size() > previousRows) {
            this.value = this.value + (this.tick_List.size() - previousRows);
         }
      }

      List<CachePool.Inner_DataUtil> rows = this.tick_List;
      List<BufferHelper.Inner_DataUtil> selRows = new ArrayList<>(rows.size());

      for (CachePool.Inner_DataUtil row : rows) {
         selRows.add(
            new BufferHelper.Inner_DataUtil(row.init_PacketUtil_BufferH().init_J(), row.init_I(), BufferHelper.init_Str_FormattedText(row.init_FormattedText()))
         );
      }

      this.init_BufferHelper.init_V_List(selRows);
      int visible = Math.max(1, (chatH - 4) / 11);
      int chatMaxScroll = Math.max(0, rows.size() - visible);
      this.value = init_I_I(this.value, 0, chatMaxScroll);
      int end = rows.size() - this.value;
      int start = Math.max(0, end - visible);
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(x + 2, top + 2, Math.max(1, w - 4), Math.max(1, chatH - 4)));
      try {
         int cy = top + 3;

         for (int i = start; i < end; i++) {
            CachePool.Inner_DataUtil row = rows.get(i);
            String rowText = BufferHelper.init_Str_FormattedText(row.init_FormattedText());
            int[] range = this.init_BufferHelper.init_IArr_J(row.init_PacketUtil_BufferH().init_J(), row.init_I(), rowText.length());
            if (range != null && range[1] > range[0]) {
               int sx = this.f_b_I + BufferHelper.init_I_Font(this.init_Font, row.init_FormattedText(), range[0]);
               int ex = this.f_b_I + BufferHelper.init_I_Font(this.init_Font, row.init_FormattedText(), range[1]);
               autismclient.gui.f.PacketUtil.init(g, autismclient.gui.f.NetHelper.init(sx, cy - 1, Math.max(1, ex - sx), 11), 1430023925);
            }

            if (row.init_I() == 0) {
               g.text(this.init_Font, Component.literal(init_Str_J(row.init_PacketUtil_BufferH().load())), textX, cy, this.tick_I(), false);
            }

            g.text(this.init_Font, row.init_FormattedCharSeque(), this.f_b_I, cy, this.pop_I(), false);
            CachePool.init(g, this.init_Font, row.init_FormattedText(), this.f_b_I, cy);
            this.close_List.add(new StateCache.Inner_StreamUtil(row.init_PacketUtil_BufferH(), row.init_I(), cy, row.init_FormattedText(), rowText));
            cy += 11;
         }
      } finally {
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.update_StateCache_StateCa, this.value, chatMaxScroll, mx, my, "chat", v -> this.value = v);
      this.index();
      this.flush_List.clear();
      boolean freshSuggests = !this.sync_List.isEmpty()
         && this.close_BitSetUtil.load_Z()
         && (this.close_BitSetUtil.init_Str().equals(this.init_Str) || this.close_BitSetUtil.init_Str().equals(this.load_Str));
      if (freshSuggests) {
         int rowH = 11;
         int shown = Math.min(6, this.sync_List.size());
         int popH = shown * rowH + 2;
         int popY = inputY - popH - 1;
         autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, popY, w, popH), 6, -267251178, this.close_I());
         int first = init_I_I(this.clear - shown + 1, 0, Math.max(0, this.sync_List.size() - shown));

         for (int i = 0; i < shown; i++) {
            int idx = first + i;
            int ry = popY + 1 + i * rowH;
            boolean sel = idx == this.clear;
            if (sel) {
               autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x + 1, ry, w - 2, rowH), 4, 1090519039);
            }

            this.init_V_GuiGraphicsExtract_2(g, this.sync_List.get(idx), x + 3, ry + 1, sel ? this.pop_I() : this.tick_I(), false, w - 6);
            this.flush_List.add(new int[]{x, ry, w, rowH, idx});
         }
      }
   }

   private boolean init_Z_I_I(int mouseX, int mouseY) {
      if (this.update_StateCache_StateCa.init(mouseX, mouseY) && mouseX >= this.f_b_I && mouseX <= this.f_c_I) {
         for (StateCache.Inner_StreamUtil row : this.close_List) {
            if (mouseY >= row.load() && mouseY < row.load() + 11) {
               int relativeX = mouseX - this.f_b_I;
               ClickEvent click = CachePool.init(this.init_Font, row.init_FormattedText(), relativeX);
               if (click == null) {
                  return false;
               }

               this.init_V_PacketUtil_BufferH(row.init_PacketUtil_BufferH(), row.init_I(), relativeX, click);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean load_Z_I_I(int mouseX, int mouseY) {
      if (this.update_StateCache_StateCa.init(mouseX, mouseY) && !this.close_List.isEmpty()) {
         this.update_Z = true;
         this.init_V_I(mouseX, mouseY, true);
         return true;
      } else {
         this.init_BufferHelper.load_V();
         return false;
      }
   }

   private void init_V_I(int mouseX, int mouseY, boolean begin) {
      if (!this.close_List.isEmpty()) {
         StateCache.Inner_StreamUtil target = this.close_List.getFirst();

         for (StateCache.Inner_StreamUtil row : this.close_List) {
            if (mouseY >= row.load()) {
               target = row;
            }
         }

         int ch = BufferHelper.load_I_Font(this.init_Font, target.init_FormattedText(), mouseX - this.f_b_I);
         if (begin) {
            this.init_BufferHelper.init_V_J(target.init_PacketUtil_BufferH().init_J(), target.init_I(), ch);
         } else {
            this.init_BufferHelper.load_V_J(target.init_PacketUtil_BufferH().init_J(), target.init_I(), ch);
         }
      }
   }

   private void write_V() {
      String selected = this.init_BufferHelper.init_Str();
      if (!selected.isBlank() && this.init_Minecraft != null && this.init_Minecraft.keyboardHandler != null) {
         this.init_Minecraft.keyboardHandler.setClipboard(selected);
         this.init_V_Str_I("Copied " + selected.length() + " chars", -13248397);
      }
   }

   private void init_V_PacketUtil_BufferH(PacketUtil.Inner_BufferHelper line, int lineIndex, int relativeX, ClickEvent click) {
      switch (click) {
         case RunCommand command:
            int ran = CachePool.init(this.init_Font, line, lineIndex, relativeX, this.f_d_I, this.load_I(), this::init_Str_Str, this.tick_I(), command);
            this.init_V_Str_I(ran > 0 ? "Clicked " + ran : "Clicked", -13248397);
            break;
         case SuggestCommand suggest:
            this.close_BitSetUtil.init_BitSetUtil_Str(suggest.command());
            this.close_BitSetUtil.init_V_Z(true);
            break;
         case CopyToClipboard copy:
            this.init_Minecraft.keyboardHandler.setClipboard(copy.value());
            this.init_V_Str_I("Copied", -6645094);
            break;
         case OpenUrl url:
            CachePool.init(url.uri());
            this.init_V_Str_I("Opening link", -13248397);
            break;
         default:
      }
   }

   private int load_I() {
      return this.init_LinkedHashSet.isEmpty() ? Math.max(1, this.push_List.size()) : this.init_LinkedHashSet.size();
   }

   private String init_Str_Str(String accountId) {
      for (ClassCache.Inner_FieldCache snapshot : this.push_List) {
         if (snapshot.init_Str().equals(accountId)) {
            return snapshot.load_Str();
         }
      }

      return accountId;
   }

   private static String init_Str_J(long time) {
      return "[" + LocalTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(init_DateTimeFormatter) + "] ";
   }

   private void update(GuiGraphicsExtractor g, int mx, int my) {
      int x = this.block + 2;
      int y = this.drain;
      int w = this.table - 4;
      int ch = read_I();
      int rowStep = ch + 2;
      boolean active = PacketUtil.init_PacketUtil().load_Z();
      FieldCache profile = active ? this.load_FieldCache : this.init_FieldCache;
      if (profile != null) {
         String scope = this.init_LinkedHashSet.isEmpty() ? "All sessions" : this.init_LinkedHashSet.size() + " selected";
         this.init_V_GuiGraphicsExtract_2(g, "Quick Actions - " + scope, x, y, this.pop_I(), false, w);
         y += sync_I();
         int count = 5;
         int each = (w - 2 * (count - 1)) / count;
         int cx = x;
         int quickContentW = this.init_I_Str("Move");

         for (int i = 0; i < 4; i++) {
            quickContentW += this.init_I_Str(profile.init_ListPool_I(i).init_Str_I(i));
         }

         quickContentW += 2 * (count - 1);
         if (quickContentW <= w) {
            this.init_V_GuiGraphicsExtract_2(
               g,
               cx,
               y,
               this.init_I_Str("Move"),
               ch,
               "Move",
               this.poll_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().update_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            cx += this.init_I_Str("Move") + 2;

            for (int i = 0; i < 4; i++) {
               int index = i;
               ListPool action = profile.init_ListPool_I(i);
               int cw = this.init_I_Str(action.init_Str_I(i));
               this.init_V_GuiGraphicsExtract_2(
                  g, cx, y, cw, ch, action.init_Str_I(i), action.init_Z() ? this.close_I() : this.open_I(), this.pop_I(), mx, my, () -> {
                     if (active && !action.init_Z()) {
                        this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_ListPool_Set(action, this.init_LinkedHashSet));
                     } else {
                        this.update_V_I(index);
                     }
                  }
               );
               cx += cw + 2;
            }
         } else {
            this.init_V_GuiGraphicsExtract_2(
               g,
               cx,
               y,
               each,
               ch,
               "Move",
               this.poll_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().update_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            cx += each + 2;

            for (int i = 0; i < 4; i++) {
               int index = i;
               ListPool action = profile.init_ListPool_I(i);
               int cw = i == 3 ? x + w - cx : each;
               this.init_V_GuiGraphicsExtract_2(
                  g, cx, y, cw, ch, action.init_Str_I(i), action.init_Z() ? this.close_I() : this.open_I(), this.pop_I(), mx, my, () -> {
                     if (active && !action.init_Z()) {
                        this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_ListPool_Set(action, this.init_LinkedHashSet));
                     } else {
                        this.update_V_I(index);
                     }
                  }
               );
               cx += cw + 2;
            }
         }

         y += rowStep;
         int editSlotW = this.init_I_Str("Edit 1");
         cx = x;
         if (editSlotW * 4 + 6 <= w) {
            for (int i = 0; i < 4; i++) {
               int index = i;
               this.init_V_GuiGraphicsExtract_2(g, cx, y, editSlotW, ch, "Edit " + (i + 1), this.close_I(), this.pop_I(), mx, my, () -> this.update_V_I(index));
               cx += editSlotW + 2;
            }
         } else {
            int slotW = (w - 6) / 4;

            for (int i = 0; i < 4; i++) {
               int index = i;
               int cw = i == 3 ? x + w - cx : slotW;
               this.init_V_GuiGraphicsExtract_2(g, cx, y, cw, ch, "Edit " + (i + 1), this.close_I(), this.pop_I(), mx, my, () -> this.update_V_I(index));
               cx += cw + 2;
            }
         }

         y += ch + 4;
         String guiAccount = this.init_LinkedHashSet.size() == 1 ? this.init_LinkedHashSet.iterator().next() : "";
         int useW = this.init_I_Str("Use");
         int closeW = this.init_I_Str("Close GUI");
         int guiToggleW = this.init_I_Str("GUI");
         if (useW + closeW + guiToggleW + 4 <= w) {
            this.init_V_GuiGraphicsExtract_2(
               g,
               x,
               y,
               useW,
               ch,
               "Use",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().sync_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + useW + 2,
               y,
               closeW,
               ch,
               "Close GUI",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().flush_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + useW + closeW + 4,
               y,
               guiToggleW,
               ch,
               "GUI",
               this.init_StateCache_CachePo.init_Z(guiAccount),
               mx,
               my,
               this.init_LinkedHashSet.size() == 1 ? this::clear : null
            );
         } else {
            int third = (w - 4) / 3;
            this.init_V_GuiGraphicsExtract_2(
               g,
               x,
               y,
               third,
               ch,
               "Use",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().sync_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + third + 2,
               y,
               third,
               ch,
               "Close GUI",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               PacketUtil.init_PacketUtil().load_Z()
                  ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().flush_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
                  : null
            );
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + (third + 2) * 2,
               y,
               w - (third + 2) * 2,
               ch,
               "GUI",
               this.init_StateCache_CachePo.init_Z(guiAccount),
               mx,
               my,
               this.init_LinkedHashSet.size() == 1 ? this::clear : null
            );
         }

         y += rowStep;
         int half = (w - 2) / 2;
         IoWorker policy = profile.init_IoWorker;
         String gravityLabel = "Gravity: " + (policy.init_Z() ? "On" : "Off");
         String lookLabel = "Look: " + (policy.read() ? "On" : "Off");
         String swingLabel = "Swing: " + (policy.write() ? "On" : "Off");
         this.init_V_GuiGraphicsExtract_2(
            g,
            x,
            y,
            w,
            ch,
            mx,
            my,
            new StateCache.Inner_ConfigStore(
               gravityLabel, policy.init_Z() ? this.poll_I() : this.close_I(), () -> this.init_V_Consumer(p -> p.init_V_Z(!p.init_Z()))
            ),
            new StateCache.Inner_ConfigStore(lookLabel, policy.read() ? this.poll_I() : this.close_I(), () -> this.init_V_Consumer(p -> p.read(!p.read()))),
            new StateCache.Inner_ConfigStore(swingLabel, policy.write() ? this.poll_I() : this.close_I(), () -> this.init_V_Consumer(p -> p.write(!p.write())))
         );
         y += rowStep;
         boolean cancelling = policy.update();
         int c2sCount = policy.init_I_IoWorker_DataUtil(IoWorker.Inner_DataUtil.init);
         int s2cCount = policy.init_I_IoWorker_DataUtil(IoWorker.Inner_DataUtil.load);
         this.init_V_GuiGraphicsExtract_2(
            g,
            x,
            y,
            w,
            ch,
            mx,
            my,
            new StateCache.Inner_ConfigStore(
               "Canceller: " + (cancelling ? "On" : "Off"), cancelling ? this.poll_I() : this.close_I(), () -> this.init_V_Consumer(p -> p.update(!p.update()))
            ),
            new StateCache.Inner_ConfigStore(
               "Block C2S (" + c2sCount + ")",
               cancelling && c2sCount > 0 ? this.push_I() : this.close_I(),
               () -> this.init_V_IoWorker_DataUtil(IoWorker.Inner_DataUtil.init)
            ),
            new StateCache.Inner_ConfigStore(
               "Block S2C (" + s2cCount + ")",
               cancelling && s2cCount > 0 ? this.push_I() : this.close_I(),
               () -> this.init_V_IoWorker_DataUtil(IoWorker.Inner_DataUtil.load)
            ),
            new StateCache.Inner_ConfigStore("Import", this.close_I(), this::buffer)
         );
         y += rowStep;
         int resetActionsW = this.init_I_Str("Reset Actions");
         int clearBlocklistW = this.init_I_Str("Clear Blocklist");
         if (resetActionsW + 2 + clearBlocklistW <= w) {
            this.init_V_GuiGraphicsExtract_2(g, x, y, resetActionsW, ch, "Reset Actions", this.close_I(), this.pop_I(), mx, my, this::queue);
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + resetActionsW + 2,
               y,
               clearBlocklistW,
               ch,
               "Clear Blocklist",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               () -> this.init_V_Consumer(p -> p.init_V_List(List.of()))
            );
         } else {
            this.init_V_GuiGraphicsExtract_2(g, x, y, half, ch, "Reset Actions", this.close_I(), this.pop_I(), mx, my, this::queue);
            this.init_V_GuiGraphicsExtract_2(
               g,
               x + half + 2,
               y,
               w - half - 2,
               ch,
               "Clear Blocklist",
               this.close_I(),
               this.pop_I(),
               mx,
               my,
               () -> this.init_V_Consumer(p -> p.init_V_List(List.of()))
            );
         }

         y += ch + 4;
         this.init_V_GuiGraphicsExtract_2(g, x, y, w, ch, "Auto-Accept: TPA & Trade", this.open_I(), this.pop_I(), mx, my, this::tick_V);
         y += ch + 4;
         if (!this.open_Str.isBlank()) {
            this.init_V_GuiGraphicsExtract_2(g, this.open_Str, x, y, this.flush_I(), false, w);
         }
      }
   }

   private void sync(GuiGraphicsExtractor g, int mx, int my) {
      int ch = read_I();
      int lh = sync_I();
      int x = this.block + 2;
      int y = this.drain;
      int w = this.table - 4;
      int h = this.fill - y;
      int listW = Math.max(130, Math.min(180, w / 3));
      this.init_V_GuiGraphicsExtract(g, this.open_BitSetUtil, x, y, listW);
      y += ch + 2;
      List<cY.Inner_DataUtil> macros = this.load_List();
      int pitch = update_I();
      int listH = Math.max(30, h - ch - 2);
      this.sync_StateCache_StateCa = new StateCache.Inner_StateCache(x, y, listW, listH);
      int maxScroll = Math.max(0, init_I_I(macros.size()) - listH);
      this.cache = init_I_I(this.cache, 0, maxScroll);
      HashUtil.init_HashUtil().init(g, this.sync_StateCache_StateCa.init_NetHelper());
      this.open_StateCache_StateCa = this.sync_StateCache_StateCa;
      try {
         int ry = y - this.cache;

         for (cY.Inner_DataUtil item : macros) {
            aM macro = item.init_aM();
            if (ry + pitch > y && ry < y + listH) {
               if (item.init_Z()) {
                  autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, ry, listW, write_I());
                  autismclient.gui.f.b.TaskQueue.load(
                     autismclient.gui.f.UiHelper.init(g, this.init_Font, mx, my), bounds, item.init_Str(), item.load_Z(), bounds.init_Z(mx, my)
                  );
                  int hitTop = Math.max(ry, y);
                  int hitBottom = Math.min(ry + write_I(), y + listH);
                  if (hitBottom > hitTop) {
                     this.read_List
                        .add(new StateCache.Inner_BufferHelper(x, hitTop, listW - 6, hitBottom - hitTop, () -> aN.init_aN().init_V_Str(item.load_Str())));
                  }
               } else {
                  boolean selected = macro.init_Str.equals(this.sync_Str);
                  this.init_V_GuiGraphicsExtract(g, x, ry, listW, write_I(), macro.init_Str, selected ? this.poll_I() : this.close_I(), mx, my, () -> {
                     this.sync_Str = macro.init_Str;
                     this.close_Str = "";
                  });
               }
            }

            ry += pitch;
         }
      } finally {
         this.open_StateCache_StateCa = StateCache.Inner_StateCache.init_StateCache_StateCa;
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.sync_StateCache_StateCa, this.cache, maxScroll, mx, my, "macro", v -> this.cache = v);
      int dx = x + listW + 4;
      int dw = w - listW - 4;
      int cy = this.drain;
      String scope = this.init_LinkedHashSet.isEmpty() ? "All sessions" : this.init_LinkedHashSet.size() + " selected";
      this.init_V_GuiGraphicsExtract_2(g, scope, dx, cy, this.tick_I(), false, dw);
      cy += lh;
      this.init_V_GuiGraphicsExtract_2(g, this.sync_Str.isBlank() ? "No macro selected" : this.sync_Str, dx, cy, this.pop_I(), false, dw);
      cy += lh + 2;
      int assignW = this.init_I_Str("Assign");
      int runW = this.init_I_Str("Run") + 18;
      int stopMacroW = this.init_I_Str("Stop");
      if (assignW + runW + stopMacroW + 4 <= dw) {
         this.init_V_GuiGraphicsExtract_2(g, dx, cy, assignW, ch, "Assign", this.poll_I(), this.pop_I(), mx, my, this.sync_Str.isBlank() ? null : this::node);
         this.init_V_GuiGraphicsExtract(
            g, dx + assignW + 2, cy, runW, ch, "Run", this.poll_I(), this.pop_I(), mx, my, PacketUtil.init_PacketUtil().load_Z() ? this::entry : null, true
         );
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + assignW + runW + 4,
            cy,
            stopMacroW,
            ch,
            "Stop",
            this.push_I(),
            this.pop_I(),
            mx,
            my,
            PacketUtil.init_PacketUtil().load_Z()
               ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().write_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
               : null
         );
      } else {
         int third = (dw - 4) / 3;
         this.init_V_GuiGraphicsExtract_2(g, dx, cy, third, ch, "Assign", this.poll_I(), this.pop_I(), mx, my, this.sync_Str.isBlank() ? null : this::node);
         this.init_V_GuiGraphicsExtract(
            g, dx + third + 2, cy, third, ch, "Run", this.poll_I(), this.pop_I(), mx, my, PacketUtil.init_PacketUtil().load_Z() ? this::entry : null, true
         );
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + (third + 2) * 2,
            cy,
            dw - (third + 2) * 2,
            ch,
            "Stop",
            this.push_I(),
            this.pop_I(),
            mx,
            my,
            PacketUtil.init_PacketUtil().load_Z()
               ? () -> this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().write_PacketUtil_CachePo_Set(this.init_LinkedHashSet))
               : null
         );
      }

      cy += ch + 2;
      int newMacroW = this.init_I_Str("New");
      int editMacroW = this.init_I_Str("Edit");
      int deleteMacroW = this.init_I_Str("Delete");
      if (newMacroW + editMacroW + deleteMacroW + 4 <= dw) {
         this.init_V_GuiGraphicsExtract_2(g, dx, cy, newMacroW, ch, "New", this.close_I(), this.pop_I(), mx, my, () -> this.init_V_aM(null));
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + newMacroW + 2,
            cy,
            editMacroW,
            ch,
            "Edit",
            this.close_I(),
            this.pop_I(),
            mx,
            my,
            this.sync_Str.isBlank() ? null : () -> this.init_V_aM(aN.init_aN().init_aM_Str(this.sync_Str))
         );
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + newMacroW + editMacroW + 4,
            cy,
            deleteMacroW,
            ch,
            "Delete",
            this.push_I(),
            this.pop_I(),
            mx,
            my,
            this.sync_Str.isBlank() ? null : () -> this.close_Str = this.sync_Str
         );
      } else {
         int third = (dw - 4) / 3;
         this.init_V_GuiGraphicsExtract_2(g, dx, cy, third, ch, "New", this.close_I(), this.pop_I(), mx, my, () -> this.init_V_aM(null));
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + third + 2,
            cy,
            third,
            ch,
            "Edit",
            this.close_I(),
            this.pop_I(),
            mx,
            my,
            this.sync_Str.isBlank() ? null : () -> this.init_V_aM(aN.init_aN().init_aM_Str(this.sync_Str))
         );
         this.init_V_GuiGraphicsExtract_2(
            g,
            dx + (third + 2) * 2,
            cy,
            dw - (third + 2) * 2,
            ch,
            "Delete",
            this.push_I(),
            this.pop_I(),
            mx,
            my,
            this.sync_Str.isBlank() ? null : () -> this.close_Str = this.sync_Str
         );
      }

      cy += ch + 2;
      int clearAssignW = Math.min(this.init_I_Str("Clear Assignment"), dw);
      this.init_V_GuiGraphicsExtract_2(
         g, dx + (dw - clearAssignW) / 2, cy, clearAssignW, ch, "Clear Assignment", this.close_I(), this.pop_I(), mx, my, this::task
      );
      cy += ch + 4;
      int delayMs = RenderState.init_I();
      this.close_StateCache_StateCa = new StateCache.Inner_StateCache(dx, cy, dw, ch);
      int unitW = this.init_I_Str_I("s", this.tick_I()) + 2;
      int fieldW = Math.min(34, dw - unitW - 46);
      boolean typedBox = fieldW >= 18;
      int sliderW = typedBox ? dw - unitW - fieldW - 4 : dw;
      this.init_V_GuiGraphicsExtract(
         g,
         dx,
         cy,
         sliderW,
         ch,
         "Delay between bots",
         RenderState.init_D_I(delayMs),
         mx,
         my,
         (mouseX, trackX, trackW) -> RenderState.init_V_I(RenderState.init_I_D(mouseX, trackX, trackW)),
         RenderState::init_V
      );
      if (typedBox) {
         this.close(delayMs);
         this.init_V_GuiGraphicsExtract_2(g, this.flush_BitSetUtil, dx + sliderW + 4, cy, fieldW, ch);
         this.init_V_GuiGraphicsExtract_2(g, "s", dx + sliderW + 4 + fieldW + 2, this.init_I_I_I(cy, ch), this.tick_I(), false, unitW);
      } else {
         this.flush_BitSetUtil.init_V_Z(false);
      }

      boolean delayFocused = this.flush_BitSetUtil.load_Z();
      if (this.read_Z && !delayFocused) {
         RenderState.init_V();
      }

      this.read_Z = delayFocused;
      cy += ch + 4;
      List<String> compatibility = this.init_List_Str(this.sync_Str);
      if (compatibility.isEmpty()) {
         this.init_V_GuiGraphicsExtract_2(g, this.sync_Str.isBlank() ? "Select a macro." : "Fully compatible.", dx, cy, this.tick_I(), false, dw);
         cy += lh + 2;
      } else {
         int warnBottom = cy + Math.min(72, Math.max(20, (this.fill - 24 - cy) / 2));
         int shown = 0;
         boolean truncated = false;

         for (String warning : compatibility) {
            List<FormattedCharSequence> lines = this.init_Font.split(FormattedText.of(warning), Math.max(40, dw - 4));
            if (cy + lines.size() * 10 > warnBottom) {
               truncated = true;
               break;
            }

            for (FormattedCharSequence line : lines) {
               g.text(this.init_Font, line, dx, cy, this.run_I(), false);
               cy += 10;
            }

            shown++;
         }

         if (truncated) {
            this.init_V_GuiGraphicsExtract_2(
               g, "+" + (compatibility.size() - shown) + " more warning(s) - shown in console chat on Run", dx, cy, this.tick_I(), false, dw
            );
            cy += 10;
         }

         cy += 4;
      }

      this.init_V_GuiGraphicsExtract_2(g, "Progress", dx, cy, this.pop_I(), false, dw);
      cy += lh;
      int progressBottom = this.fill - (this.open_Str.isBlank() ? 0 : lh);
      this.flush_StateCache_StateCa = new StateCache.Inner_StateCache(dx, cy, dw, Math.max(1, progressBottom - cy));
      int progressCount = 0;

      for (ClassCache.Inner_FieldCache snapshot : this.push_List) {
         if (this.init_LinkedHashSet.isEmpty() || this.init_LinkedHashSet.contains(snapshot.init_Str())) {
            progressCount++;
         }
      }

      int progressMaxScroll = Math.max(0, progressCount * 13 - this.flush_StateCache_StateCa.write());
      this.state = init_I_I(this.state, 0, progressMaxScroll);
      HashUtil.init_HashUtil().init(g, this.flush_StateCache_StateCa.init_NetHelper());
      try {
         cy -= this.state;
         long now = System.currentTimeMillis();

         for (ClassCache.Inner_FieldCache snapshot : this.push_List) {
            if (this.init_LinkedHashSet.isEmpty() || this.init_LinkedHashSet.contains(snapshot.init_Str())) {
               this.init_V_GuiGraphicsExtract_3(g, snapshot, dx, cy, dw, now);
               cy += 13;
            }
         }
      } finally {
         HashUtil.init_HashUtil().init(g);
      }
      this.init_V_GuiGraphicsExtract_3(g, this.flush_StateCache_StateCa, this.state, progressMaxScroll, mx, my, "macroProgress", v -> this.state = v);
      if (!this.open_Str.isBlank()) {
         this.init_V_GuiGraphicsExtract_2(g, this.open_Str, dx, this.fill - lh, this.flush_I(), false, dw);
      }
   }

   private void flush(GuiGraphicsExtractor g, int mx, int my) {
      this.read_List.clear();
      this.write_List.clear();
      autismclient.gui.f.PacketUtil.init(g, autismclient.gui.f.NetHelper.init(this.block, this.chunk, this.table, this.store), -1342177280);
      int ch = read_I();
      int lh = sync_I();
      int mp = 6;
      int step = ch + 2;
      int wanted = mp + lh + step + step + lh + step + step + step + 4 + ch + mp;
      int mw = Math.min(330, this.table - 24);
      int mh = Math.min(wanted, this.store - 24);
      int x = this.block + (this.table - mw) / 2;
      int y = this.chunk + (this.store - mh) / 2;
      int ix = x + mp;
      int iw = mw - mp * 2;
      autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, mw, mh), 6, -267251180, this.open_I());
      int cy = y + mp;
      this.init_V_GuiGraphicsExtract_2(g, "Edit Action " + (this.count + 1), ix, cy, this.pop_I(), false, iw);
      cy += lh;
      this.init_V_GuiGraphicsExtract(g, this.poll_BitSetUtil, ix, cy, iw);
      cy += step;
      this.stop();
      String packet = this.init_ListPool.init_List.get(this.flags).init_Str();
      int chooseW = this.init_I_Str("Choose");
      this.init_V_GuiGraphicsExtract_2(
         g, "Packet " + (this.flags + 1) + "/" + this.init_ListPool.init_List.size(), ix, this.init_I_I_I(cy, ch), this.tick_I(), false, iw - chooseW - 2
      );
      this.init_V_GuiGraphicsExtract_2(g, ix + iw - chooseW, cy, chooseW, ch, "Choose", this.close_I(), this.pop_I(), mx, my, this::size);
      cy += step;
      this.init_V_GuiGraphicsExtract_2(g, packet.isBlank() ? "No packet" : ListPool.init_Str_Str(packet), ix, cy, this.pop_I(), false, iw);
      cy += lh;
      this.init_V_GuiGraphicsExtract(g, this.push_BitSetUtil, ix, cy, iw);
      cy += step;
      int by1 = cy;
      int prevW = this.init_I_Str("Prev");
      int nextW = this.init_I_Str("Next");
      int addW = this.init_I_Str("Add");
      int removeW = this.init_I_Str("Remove");
      if (prevW + nextW + addW + removeW + 6 <= iw) {
         int gx = ix + (iw - (prevW + nextW + addW + removeW + 6)) / 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by1, prevW, ch, "Prev", this.close_I(), this.pop_I(), mx, my, this.flags > 0 ? () -> this.sync(-1) : null);
         gx += prevW + 2;
         this.init_V_GuiGraphicsExtract_2(
            g,
            gx,
            by1,
            nextW,
            ch,
            "Next",
            this.close_I(),
            this.pop_I(),
            mx,
            my,
            this.flags + 1 < this.init_ListPool.init_List.size() ? () -> this.sync(1) : null
         );
         gx += nextW + 2;
         this.init_V_GuiGraphicsExtract_2(
            g, gx, by1, addW, ch, "Add", this.close_I(), this.pop_I(), mx, my, this.init_ListPool.init_List.size() < 6 ? this::prev : null
         );
         gx += addW + 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by1, removeW, ch, "Remove", this.push_I(), this.pop_I(), mx, my, this::valid);
      } else {
         int q = (iw - 6) / 4;
         this.init_V_GuiGraphicsExtract_2(g, ix, by1, q, ch, "Prev", this.close_I(), this.pop_I(), mx, my, this.flags > 0 ? () -> this.sync(-1) : null);
         this.init_V_GuiGraphicsExtract_2(
            g,
            ix + q + 2,
            by1,
            q,
            ch,
            "Next",
            this.close_I(),
            this.pop_I(),
            mx,
            my,
            this.flags + 1 < this.init_ListPool.init_List.size() ? () -> this.sync(1) : null
         );
         this.init_V_GuiGraphicsExtract_2(
            g, ix + (q + 2) * 2, by1, q, ch, "Add", this.close_I(), this.pop_I(), mx, my, this.init_ListPool.init_List.size() < 6 ? this::prev : null
         );
         this.init_V_GuiGraphicsExtract_2(g, ix + (q + 2) * 3, by1, iw - (q + 2) * 3, ch, "Remove", this.push_I(), this.pop_I(), mx, my, this::valid);
      }

      int by2 = by1 + step;
      int testW = this.init_I_Str("Test Send");
      int clearW = this.init_I_Str("Clear");
      if (testW + 2 + clearW <= iw) {
         int gx = ix + (iw - (testW + 2 + clearW)) / 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by2, testW, ch, "Test Send", this.open_I(), this.pop_I(), mx, my, this::value);
         this.init_V_GuiGraphicsExtract_2(g, gx + testW + 2, by2, clearW, ch, "Clear", this.push_I(), this.pop_I(), mx, my, this::cache);
      } else {
         int half = (iw - 2) / 2;
         this.init_V_GuiGraphicsExtract_2(g, ix, by2, half, ch, "Test Send", this.open_I(), this.pop_I(), mx, my, this::value);
         this.init_V_GuiGraphicsExtract_2(g, ix + half + 2, by2, iw - half - 2, ch, "Clear", this.push_I(), this.pop_I(), mx, my, this::cache);
      }

      int by3 = y + mh - mp - ch;
      int saveW = this.init_I_Str("Save");
      int resetAllW = this.init_I_Str("Reset All");
      int cancelW = this.init_I_Str("Cancel");
      if (saveW + resetAllW + cancelW + 4 <= iw) {
         int gx = ix + (iw - (saveW + resetAllW + cancelW + 4)) / 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by3, saveW, ch, "Save", this.poll_I(), this.pop_I(), mx, my, this::data);
         gx += saveW + 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by3, resetAllW, ch, "Reset All", this.close_I(), this.pop_I(), mx, my, this::queue);
         gx += resetAllW + 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by3, cancelW, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, this::state);
      } else {
         int third = (iw - 4) / 3;
         this.init_V_GuiGraphicsExtract_2(g, ix, by3, third, ch, "Save", this.poll_I(), this.pop_I(), mx, my, this::data);
         this.init_V_GuiGraphicsExtract_2(g, ix + third + 2, by3, third, ch, "Reset All", this.close_I(), this.pop_I(), mx, my, this::queue);
         this.init_V_GuiGraphicsExtract_2(g, ix + (third + 2) * 2, by3, iw - (third + 2) * 2, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, this::state);
      }
   }

   private void close(GuiGraphicsExtractor g, int mx, int my) {
      this.read_List.clear();
      this.write_List.clear();
      autismclient.gui.f.PacketUtil.init(g, autismclient.gui.f.NetHelper.init(this.block, this.chunk, this.table, this.store), -1342177280);
      int ch = read_I();
      int mp = 6;
      int w = Math.min(270, this.table - 30);
      int h = mp + sync_I() + 4 + ch + mp;
      int x = this.block + (this.table - w) / 2;
      int y = this.chunk + (this.store - h) / 2;
      int ix = x + mp;
      int iw = w - mp * 2;
      int buttonY = y + h - mp - ch;
      autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 6, -267251180, this.push_I());
      this.init_V_GuiGraphicsExtract_2(g, "Delete " + this.close_Str + "?", ix, y + mp, this.pop_I(), false, iw);
      int confirmDeleteW = this.init_I_Str("Delete");
      int cancelDeleteW = this.init_I_Str("Cancel");
      if (confirmDeleteW + 2 + cancelDeleteW <= iw) {
         int gx = ix + (iw - (confirmDeleteW + 2 + cancelDeleteW)) / 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, buttonY, confirmDeleteW, ch, "Delete", this.push_I(), this.pop_I(), mx, my, this::pool);
         this.init_V_GuiGraphicsExtract_2(
            g, gx + confirmDeleteW + 2, buttonY, cancelDeleteW, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, () -> this.close_Str = ""
         );
      } else {
         int half = (iw - 2) / 2;
         this.init_V_GuiGraphicsExtract_2(g, ix, buttonY, half, ch, "Delete", this.push_I(), this.pop_I(), mx, my, this::pool);
         this.init_V_GuiGraphicsExtract_2(
            g, ix + half + 2, buttonY, iw - half - 2, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, () -> this.close_Str = ""
         );
      }
   }

   private void update_V() {
      List<FieldCache> profiles = TaskTimer.init_TaskTimer().init_List();
      if (profiles.isEmpty()) {
         this.sync_V();
      } else {
         FieldCache shared = TaskTimer.init_TaskTimer().init_FieldCache_Str(TaskTimer.init_TaskTimer().read_Str());
         this.init_V_FieldCache(shared != null ? shared : profiles.getFirst());
      }
   }

   private void sync_V() {
      this.init_FieldCache = new FieldCache();
      this.init_FieldCache.write_Str = TaskTimer.init_TaskTimer().init_Str("New profile", this.init_FieldCache.read_Str);
      this.init_FieldCache.update_Str = this.load_Str();
      this.add();
      TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
      this.update_Str = this.init_FieldCache.read_Str;
      TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
      this.put();
      this.flush_Str = "";
      this.write_Str = this.init_FieldCache.init_CompoundTag().toString();
   }

   private void flush_V() {
      // Reset wipes the profile's accounts and settings, so it needs a second click like Delete.
      String pending = "reset:" + this.update_Str;
      if (!this.flush_Str.equals(pending)) {
         this.flush_Str = pending;
         this.init_V_Str_I("Press Confirm", -14249);
         return;
      }

      String server = this.load_Str();
      String existingId = this.init_FieldCache.read_Str;
      String existingName = this.init_FieldCache.write_Str;
      FieldCache reset = new FieldCache();
      if (existingId != null && !existingId.isBlank()) {
         reset.read_Str = existingId;
      }

      if (existingName != null && !existingName.isBlank()) {
         reset.write_Str = existingName;
      }

      reset.update_Str = server;
      this.init_FieldCache = reset;
      this.add();
      this.put();
      this.flush_Str = "";
      this.close_V();
      this.init_V_Str_I("Defaults restored", -6645094);
   }

   private void init_V_FieldCache(FieldCache profile) {
      if (profile != null) {
         this.init_FieldCache = new FieldCache(profile);
         this.update_Str = this.init_FieldCache.read_Str;
         TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
         this.valid = 0;
         this.put();
         this.flush_Str = "";
         this.write_Str = this.init_FieldCache.init_CompoundTag().toString();
         this.init_V_Str_I("Loaded", -6645094);
      }
   }

   private void load_V_FieldCache(FieldCache profile) {
      if (profile != null && !profile.read_Str.equals(this.update_Str)) {
         this.init_V_FieldCache(profile);
      }
   }

   private void close_V() {
      if (this.init_FieldCache != null) {
         this.set();
         this.init_FieldCache.init_V();
         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.update_Str = this.init_FieldCache.read_Str;
         TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
         String serialized = this.init_FieldCache.init_CompoundTag().toString();
         if (!serialized.equals(this.write_Str)) {
            this.read_J = System.currentTimeMillis();
         }

         this.write_Str = serialized;
      }
   }

   private boolean load_Z() {
      return this.init_BitSetUtil.load_Z()
         || this.load_BitSetUtil.load_Z()
         || this.read_BitSetUtil.load_Z()
         || this.write_BitSetUtil.load_Z()
         || this.update_BitSetUtil.load_Z();
   }

   private void open_V() {
      long now = System.currentTimeMillis();
      if (now - this.load_J >= 400L) {
         this.load_J = now;
         if (this.init_FieldCache != null && !this.load_Z()) {
            this.set();
            String state = this.init_FieldCache.init_CompoundTag().toString();
            if (this.write_Str == null) {
               this.write_Str = state;
            } else if (!state.equals(this.write_Str)) {
               this.close_V();
            }
         }
      }
   }

   public void load_V() {
      if (this.init_FieldCache != null) {
         this.set();
         String state = this.init_FieldCache.init_CompoundTag().toString();
         if (this.write_Str == null) {
            this.write_Str = state;
         } else if (!state.equals(this.write_Str)) {
            this.close_V();
         }
      }
   }

   private void poll_V() {
      this.set();
      FieldCache copy = new FieldCache(this.init_FieldCache);
      copy.read_Str = UUID.randomUUID().toString();
      copy.write_Str = TaskTimer.init_TaskTimer().init_Str(copy.write_Str, copy.read_Str);
      this.init_FieldCache = copy;
      this.put();
      this.close_V();
   }

   private void push_V() {
      if (this.update_Str.isBlank()) {
         this.init_V_Str_I("Unsaved profile", -6645094);
      } else if (!this.flush_Str.equals(this.update_Str)) {
         this.flush_Str = this.update_Str;
         this.init_V_Str_I("Press Confirm", -14249);
      } else {
         TaskTimer.init_TaskTimer().load(this.update_Str);
         this.flush_Str = "";
         List<FieldCache> left = TaskTimer.init_TaskTimer().init_List();
         if (left.isEmpty()) {
            this.sync_V();
         } else {
            this.init_V_FieldCache(left.getFirst());
         }

         this.init_V_Str_I("Deleted", -6645094);
      }
   }

   private void pop_V() {
      this.set();
      this.init_FieldCache.init_V();
      this.init_FieldCache.write_Str = TaskTimer.init_TaskTimer().init_Str(this.init_FieldCache.write_Str, this.init_FieldCache.read_Str);
      this.init_BitSetUtil.init_BitSetUtil_Str(this.init_FieldCache.write_Str);
      PacketUtil.Inner_TextFormat result = PacketUtil.init_PacketUtil().init_PacketUtil_TextFor_FieldCache(this.init_FieldCache);
      if (!result.init_Z()) {
         this.init_V_Str_I(result.init_Str(), -42149);
      } else {
         TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.update_Str = this.init_FieldCache.read_Str;
         this.write_Str = this.init_FieldCache.init_CompoundTag().toString();
         this.init_StateCache_JsonUti = StateCache.Inner_JsonUtil.read;
         this.init_V_Str_I("Launching", -13248397);
      }
   }

   private void load_V_I(int index) {
      if (index >= 0 && index < FieldCache.Inner_StreamUtil.values().length) {
         this.init_FieldCache.init_FieldCache_StreamU = FieldCache.Inner_StreamUtil.values()[index];
      }
   }

   private void read(int index) {
      if (index >= 0 && index < FieldCache.Inner_CachePool.values().length) {
         this.init_FieldCache.init_FieldCache_CachePo = FieldCache.Inner_CachePool.values()[index];
      }
   }

   private void tick_V() {
      boolean active = PacketUtil.init_PacketUtil().load_Z();
      FieldCache profile = active ? PacketUtil.init_PacketUtil().init_FieldCache() : this.init_FieldCache;
      String expectedId = this.update_Str;
      if (profile != null) {
         this.init_StateCache_CachePo.init_V_CachePool(profile.init_CachePool, cfg -> {
            boolean nowActive = PacketUtil.init_PacketUtil().load_Z();
            if (active) {
               if (nowActive) {
                  PacketUtil.init_PacketUtil().init_V_CachePool(cfg);
               }
            } else if (!nowActive && this.init_FieldCache != null && expectedId.equals(this.init_FieldCache.read_Str)) {
               this.init_FieldCache.init_CachePool = cfg;
               this.close_V();
            }
         }, active);
      }
   }

   private void run_V() {
      String expectedId = this.update_Str;
      Set<String> selected = this.init_LinkedHashSet.isEmpty()
         ? this.init_FieldCache.init_List.stream().map(FieldCache.Inner_BufferHelper::init_Str).collect(Collectors.toCollection(LinkedHashSet::new))
         : new LinkedHashSet<>(this.init_LinkedHashSet);
      this.init_StateCache_CachePo.init_V_FieldCache(new FieldCache(this.init_FieldCache), selected, updated -> {
         if (!PacketUtil.init_PacketUtil().load_Z() && this.init_FieldCache != null && expectedId.equals(this.init_FieldCache.read_Str)) {
            this.init_FieldCache = updated;
            this.put();
            this.close_V();
            this.init_V_Str_I("Passwords saved", -13248397);
         }
      });
   }

   private void write(int index) {
      if (index >= 0 && index < init_IArr.length) {
         this.init_FieldCache.update_I = init_IArr[index];
         this.update_BitSetUtil.init_BitSetUtil_Str(Integer.toString(this.init_FieldCache.update_I));
      }
   }

   private void get() {
      this.init_FieldCache.update_Str = this.load_Str();
      this.load_BitSetUtil.init_BitSetUtil_Str(this.init_FieldCache.update_Str);
   }

   private void set() {
      this.init_FieldCache.write_Str = update_Str_Str(this.init_BitSetUtil.init_Str());
      this.init_FieldCache.update_Str = update_Str_Str(this.load_BitSetUtil.init_Str());
      this.init_FieldCache.read_I = load_I_Str_I(this.read_BitSetUtil.init_Str(), this.init_FieldCache.read_I);
      this.init_FieldCache.write_I = load_I_Str_I(this.write_BitSetUtil.init_Str(), this.init_FieldCache.write_I);
      this.init_FieldCache.update_I = load_I_Str_I(this.update_BitSetUtil.init_Str(), this.init_FieldCache.update_I);
   }

   private void put() {
      this.init_BitSetUtil.init_BitSetUtil_Str(this.init_FieldCache.write_Str);
      this.load_BitSetUtil.init_BitSetUtil_Str(this.init_FieldCache.update_Str);
      this.read_BitSetUtil.init_BitSetUtil_Str(Integer.toString(this.init_FieldCache.read_I));
      this.write_BitSetUtil.init_BitSetUtil_Str(Integer.toString(this.init_FieldCache.write_I));
      this.update_BitSetUtil.init_BitSetUtil_Str(Integer.toString(this.init_FieldCache.update_I));
   }

   private void add() {
      if (this.init_FieldCache != null && this.init_FieldCache.init_List.isEmpty()) {
         if (!PacketUtil.init_Z_Str("default")) {
            this.init_FieldCache.init_List.add(new FieldCache.Inner_BufferHelper("default", ""));
         } else {
            for (autismclient.util.BitSetUtil account : TokenStore.init_TokenStore().init_List()) {
               if (!PacketUtil.init_Z_Str(account.write_Str())) {
                  this.init_FieldCache.init_List.add(new FieldCache.Inner_BufferHelper(account.write_Str(), ""));
                  return;
               }
            }
         }
      }
   }

   private void load_V_Str(String id) {
      if (PacketUtil.init_Z_Str(id)) {
         this.init_V_Str_I("Current account cannot join Multi", -42149);
      } else {
         FieldCache.Inner_BufferHelper spec = this.init_FieldCache_BufferH_Str(id);
         if (spec != null) {
            this.init_FieldCache.init_List.remove(spec);
         } else if (this.init_FieldCache.init_List.size() < 500) {
            this.init_FieldCache.init_List.add(new FieldCache.Inner_BufferHelper(id, ""));
         }
      }
   }

   private void init_V_Str_Str(String accountId, String proxyId) {
      FieldCache.Inner_BufferHelper spec = this.init_FieldCache_BufferH_Str(accountId);
      if (spec != null) {
         int index = this.init_FieldCache.init_List.indexOf(spec);
         this.init_FieldCache.init_List.set(index, new FieldCache.Inner_BufferHelper(spec.init_Str(), proxyId, spec.read()));
      }
   }

   private void read_V(String proxyId) {
      String id = proxyId == null ? "" : proxyId;

      for (int i = 0; i < this.init_FieldCache.init_List.size(); i++) {
         FieldCache.Inner_BufferHelper spec = this.init_FieldCache.init_List.get(i);
         this.init_FieldCache.init_List.set(i, new FieldCache.Inner_BufferHelper(spec.init_Str(), id, spec.read()));
      }
   }

   private void write_V(String id) {
      if (this.read_Z()) {
         if (!this.init_LinkedHashSet.remove(id)) {
            this.init_LinkedHashSet.add(id);
         }
      } else {
         if (this.write_Z() && !this.init_LinkedHashSet.isEmpty()) {
            List<String> order = PacketUtil.init_PacketUtil().load_List().stream().map(ClassCache.Inner_FieldCache::init_Str).toList();
            int a = order.indexOf(this.init_LinkedHashSet.iterator().next());
            int b = order.indexOf(id);
            if (a >= 0 && b >= 0) {
               this.init_LinkedHashSet.clear();

               for (int i = Math.min(a, b); i <= Math.max(a, b); i++) {
                  this.init_LinkedHashSet.add(order.get(i));
               }

               return;
            }
         }

         boolean only = this.init_LinkedHashSet.size() == 1 && this.init_LinkedHashSet.contains(id);
         this.init_LinkedHashSet.clear();
         if (!only) {
            this.init_LinkedHashSet.add(id);
         }
      }
   }

   private void clear() {
      if (this.init_LinkedHashSet.size() != 1) {
         this.init_V_Str_I("Select one session", -6645094);
      } else {
         this.init_StateCache_CachePo.init_V(this.init_LinkedHashSet.iterator().next());
      }
   }

   private void reset() {
      List<ClassCache.Inner_FieldCache> snapshots = PacketUtil.init_PacketUtil().load_List();
      int retried = 0;

      for (ClassCache.Inner_FieldCache snapshot : snapshots) {
         if ((this.init_LinkedHashSet.isEmpty() || this.init_LinkedHashSet.contains(snapshot.init_Str()))
            && PacketUtil.init_Z_ClassCache_PathHel(snapshot.init_ClassCache_PathHel())
            && PacketUtil.init_PacketUtil().init_PacketUtil_EventBu_Str(snapshot.init_Str()).init_Z()) {
            retried++;
         }
      }

      this.init_V_Str_I(retried > 0 ? "Retrying " + retried : "Nothing to retry", retried > 0 ? -13248397 : -6645094);
   }

   private void start() {
      for (ClassCache.Inner_FieldCache snapshot : PacketUtil.init_PacketUtil().load_List()) {
         if (this.init_LinkedHashSet.isEmpty() || this.init_LinkedHashSet.contains(snapshot.init_Str())) {
            PacketUtil.init_PacketUtil().init_V_Str(snapshot.init_Str());
         }
      }

      this.init_V_Str_I("Stopped", -42149);
   }

   private void update_V_Str(String text) {
      String line = update_Str_Str(text);
      if (!line.isBlank() && PacketUtil.init_PacketUtil().load_Z()) {
         this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_Str_Set(line, this.init_LinkedHashSet));
         PacketUtil.init_PacketUtil().read_V_Str(line);
         this.close_BitSetUtil.init_BitSetUtil_Str("");
         this.count();
      }
   }

   private void update_V_I(int index) {
      FieldCache profile = PacketUtil.init_PacketUtil().load_Z() ? PacketUtil.init_PacketUtil().init_FieldCache() : this.init_FieldCache;
      if (profile != null) {
         this.count = index;
         this.flags = 0;
         this.init_ListPool = profile.init_ListPool_I(index);
         if (this.init_ListPool.init_List.isEmpty()) {
            this.init_ListPool.init_List.add(new ListPool.Inner_DataUtil("", ""));
         }

         this.poll_BitSetUtil.init_BitSetUtil_Str(this.init_ListPool.init_Str);
         this.push_BitSetUtil.init_BitSetUtil_Str(this.init_ListPool.init_List.getFirst().load());
      }
   }

   private void stop() {
      if (this.init_ListPool == null) {
         this.init_ListPool = new ListPool();
      }

      if (this.init_ListPool.init_List.isEmpty()) {
         this.init_ListPool.init_List.add(new ListPool.Inner_DataUtil("", ""));
      }

      this.flags = init_I_I(this.flags, 0, this.init_ListPool.init_List.size() - 1);
   }

   private void next() {
      this.stop();
      this.init_ListPool.init_Str = update_Str_Str(this.poll_BitSetUtil.init_Str());
      ListPool.Inner_DataUtil current = this.init_ListPool.init_List.get(this.flags);
      this.init_ListPool.init_List.set(this.flags, new ListPool.Inner_DataUtil(current.init_Str(), this.push_BitSetUtil.init_Str()));
   }

   private void sync(int delta) {
      this.next();
      this.flags = init_I_I(this.flags + delta, 0, this.init_ListPool.init_List.size() - 1);
      this.push_BitSetUtil.init_BitSetUtil_Str(this.init_ListPool.init_List.get(this.flags).load());
   }

   private void prev() {
      this.next();
      if (this.init_ListPool.init_List.size() < 6) {
         this.init_ListPool.init_List.add(new ListPool.Inner_DataUtil("", ""));
         this.flags = this.init_ListPool.init_List.size() - 1;
         this.push_BitSetUtil.init_BitSetUtil_Str("");
      }
   }

   private void valid() {
      this.stop();
      this.init_ListPool.init_List.remove(this.flags);
      if (this.init_ListPool.init_List.isEmpty()) {
         this.init_ListPool.init_List.add(new ListPool.Inner_DataUtil("", ""));
      }

      this.flags = init_I_I(this.flags, 0, this.init_ListPool.init_List.size() - 1);
      this.push_BitSetUtil.init_BitSetUtil_Str(this.init_ListPool.init_List.get(this.flags).load());
   }

   private void size() {
      this.next();
      this.init_StateCache_CachePo.init(packet -> {
         this.stop();
         ListPool.Inner_DataUtil old = this.init_ListPool.init_List.get(this.flags);
         this.init_ListPool.init_List.set(this.flags, new ListPool.Inner_DataUtil(packet.getName(), old.load()));
         if (this.init_ListPool.init_Str.isBlank()) {
            this.init_ListPool.init_Str = ListPool.init_Str_Str(packet.getName());
            this.poll_BitSetUtil.init_BitSetUtil_Str(this.init_ListPool.init_Str);
         }
      });
   }

   private ListPool init_ListPool() {
      this.next();
      ListPool result = new ListPool(this.init_ListPool);
      result.init_V();
      return result;
   }

   private void data() {
      ListPool action = this.init_ListPool();
      if (PacketUtil.init_PacketUtil().load_Z()) {
         PacketUtil.init_PacketUtil().init_V_I_ListPool(this.count, action);
      } else {
         this.init_FieldCache.init(this.count, action);
      }

      this.init_V_Str_I("Saved", -13248397);
      this.state();
   }

   private void value() {
      if (!PacketUtil.init_PacketUtil().load_Z()) {
         this.init_V_Str_I("Launch first", -6645094);
      } else {
         this.init_V_PacketUtil_CachePo(PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_ListPool_Set(this.init_ListPool(), this.init_LinkedHashSet));
      }
   }

   private void cache() {
      if (PacketUtil.init_PacketUtil().load_Z()) {
         PacketUtil.init_PacketUtil().init_V_I(this.count);
      } else {
         this.init_FieldCache.init(this.count, new ListPool());
      }

      this.init_V_Str_I("Cleared", -6645094);
      this.state();
   }

   private void state() {
      this.count = -1;
      this.init_ListPool = null;
      this.flags = 0;
      this.poll_BitSetUtil.init_V_Z(false);
      this.push_BitSetUtil.init_V_Z(false);
   }

   private void queue() {
      if (PacketUtil.init_PacketUtil().load_Z()) {
         PacketUtil.init_PacketUtil().read_V();
      } else {
         this.init_FieldCache.load_V();
      }

      this.init_V_Str_I("Actions reset", -6645094);
      this.state();
   }

   private void init_V_Consumer(Consumer<IoWorker> change) {
      FieldCache profile = PacketUtil.init_PacketUtil().load_Z() ? PacketUtil.init_PacketUtil().init_FieldCache() : this.init_FieldCache;
      if (profile != null) {
         IoWorker policy = new IoWorker(profile.init_IoWorker);
         change.accept(policy);
         if (PacketUtil.init_PacketUtil().load_Z()) {
            PacketUtil.init_PacketUtil().init_V_IoWorker(policy);
         } else {
            this.init_FieldCache.init_IoWorker = policy;
         }
      }
   }

   private void buffer() {
      IoWorker.Inner_StreamUtil[] result = new IoWorker.Inner_StreamUtil[1];
      this.init_V_Consumer(policy -> result[0] = policy.init_IoWorker_StreamUti());
      if (result[0] != null) {
         FieldCache profile = PacketUtil.init_PacketUtil().load_Z() ? PacketUtil.init_PacketUtil().init_FieldCache() : this.init_FieldCache;
         boolean on = profile != null && profile.init_IoWorker.update();
         this.init_V_Str_I(result[0].init_Str() + (on ? "" : " - canceller is off"), on ? -13248397 : -6645094);
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, int x, int y, int w, int ch, int mx, int my, StateCache.Inner_ConfigStore... buttons) {
      int natural = 2 * (buttons.length - 1);

      for (StateCache.Inner_ConfigStore button : buttons) {
         natural += this.init_I_Str(button.init_Str());
      }

      boolean fits = natural <= w;
      int each = (w - 2 * (buttons.length - 1)) / buttons.length;
      int cx = x;

      for (int i = 0; i < buttons.length; i++) {
         StateCache.Inner_ConfigStore button = buttons[i];
         int bw = fits ? this.init_I_Str(button.init_Str()) : (i == buttons.length - 1 ? x + w - cx : each);
         this.init_V_GuiGraphicsExtract_2(g, cx, y, bw, ch, button.init_Str(), button.init_I(), this.pop_I(), mx, my, button.init_Runnable());
         cx += bw + 2;
      }
   }

   public void init_V_Str(String name) {
      for (StateCache.Inner_JsonUtil candidate : StateCache.Inner_JsonUtil.values()) {
         if (candidate.name().equalsIgnoreCase(name)) {
            this.init_StateCache_JsonUti = candidate;
         }
      }
   }

   private void init_V_IoWorker_DataUtil(IoWorker.Inner_DataUtil direction) {
      FieldCache profile = PacketUtil.init_PacketUtil().load_Z() ? PacketUtil.init_PacketUtil().init_FieldCache() : this.init_FieldCache;
      if (profile != null) {
         Set<Class<? extends Packet<?>>> selected = new LinkedHashSet<>();

         for (IoWorker.Inner_CachePool rule : profile.init_IoWorker.load_List()) {
            if (rule.init_IoWorker_DataUtil() == direction) {
               Class<? extends Packet<?>> packet = bw.init_Class_Str(rule.init_Str());
               if (packet != null) {
                  selected.add(packet);
               }
            }
         }

         this.init_StateCache_CachePo.init_V_IoWorker_DataUtil(direction, selected, (packetx, enabled) -> this.init_V_Consumer(policy -> {
            List<IoWorker.Inner_CachePool> rules = new ArrayList<>(policy.load_List());
            rules.removeIf(rulex -> rulex.init_IoWorker_DataUtil() == direction && rulex.init_Str().equals(packetx.getName()));
            if (enabled) {
               rules.add(new IoWorker.Inner_CachePool(direction, packetx.getName()));
            }

            policy.init_V_List(rules);
         }));
      }
   }

   private void entry() {
      PacketUtil m = PacketUtil.init_PacketUtil();
      if (!m.load_Z()) {
         this.init_V_Str_I("Connect first", -6645094);
      } else if (m.init_Z_Set(this.init_LinkedHashSet)) {
         this.init_V_PacketUtil_CachePo(m.read_PacketUtil_CachePo_Set(this.init_LinkedHashSet));
      } else if (!this.sync_Str.isBlank()) {
         aM macro = aN.init_aN().init_aM_Str(this.sync_Str);
         if (macro != null) {
            this.init_V_PacketUtil_CachePo(m.load(macro, this.init_LinkedHashSet));
         } else {
            this.init_V_Str_I("Macro not found", -42149);
         }
      } else {
         this.init_V_Str_I("Select or assign a macro first", -6645094);
      }
   }

   private void node() {
      if (this.sync_Str.isBlank()) {
         this.init_V_Str_I("Select a macro first", -6645094);
      } else {
         List<String[]> accounts = this.init_List();
         if (accounts.isEmpty()) {
            this.init_V_Str_I("No accounts", -6645094);
         } else if (accounts.size() == 1) {
            this.init_V_Collection(List.of(accounts.get(0)[0]));
            this.init_V_Str_I("Assigned to " + accounts.get(0)[1], -13248397);
         } else {
            this.load_LinkedHashSet.clear();
            this.load_LinkedHashSet.addAll(this.init_LinkedHashSet);
            this.stop = 0;
            this.init_Z = true;
         }
      }
   }

   private List<String[]> init_List() {
      List<String[]> out = new ArrayList<>();
      if (PacketUtil.init_PacketUtil().load_Z()) {
         for (ClassCache.Inner_FieldCache s : this.push_List) {
            out.add(new String[]{s.init_Str(), s.load_Str(), load_Str_Str(PacketUtil.init_PacketUtil().write_Str_Str(s.init_Str()))});
         }
      } else if (this.init_FieldCache != null) {
         for (FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
            String m = spec.read().isBlank() ? this.init_FieldCache.sync : spec.read();
            out.add(new String[]{spec.init_Str(), this.read_Str(spec.init_Str()), load_Str_Str(m)});
         }
      }

      return out;
   }

   private static String load_Str_Str(String s) {
      return s == null ? "" : s;
   }

   private String read_Str(String id) {
      if ("default".equals(id)) {
         return "Current account";
      }

      autismclient.util.BitSetUtil account = TokenStore.init_TokenStore().init_BitSetUtil(id);
      return account == null ? id : update_Str_Str(account.init_Str());
   }

   private void init_V_Collection(Collection<String> ids) {
      if (PacketUtil.init_PacketUtil().load_Z()) {
         PacketUtil.init_PacketUtil().init_I_Set_Str(new LinkedHashSet<>(ids), this.sync_Str);
      } else if (this.init_FieldCache != null) {
         for (int i = 0; i < this.init_FieldCache.init_List.size(); i++) {
            FieldCache.Inner_BufferHelper spec = this.init_FieldCache.init_List.get(i);
            if (ids.contains(spec.init_Str())) {
               this.init_FieldCache.init_List.set(i, spec.init_FieldCache_BufferH_Str(this.sync_Str));
            }
         }

         this.close_V();
      }
   }

   private void open(GuiGraphicsExtractor g, int mx, int my) {
      this.read_List.clear();
      this.write_List.clear();
      autismclient.gui.f.PacketUtil.init(g, autismclient.gui.f.NetHelper.init(this.block, this.chunk, this.table, this.store), -1342177280);
      List<String[]> accounts = this.init_List();
      int ch = read_I();
      int lh = sync_I();
      int mp = 6;
      int pitch = update_I();
      int chrome = mp + lh + 4 + ch + 2 + ch + mp;
      int listWanted = Math.max(3, accounts.size()) * pitch - 2;
      int w = Math.min(300, this.table - 24);
      int h = Math.min(chrome + listWanted, this.store - 24);
      int x = this.block + (this.table - w) / 2;
      int y = this.chunk + (this.store - h) / 2;
      int ix = x + mp;
      int iw = w - mp * 2;
      autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 6, -267251180, this.open_I());
      this.init_V_GuiGraphicsExtract_2(g, "Assign \"" + this.sync_Str + "\" to:", ix, y + mp, this.pop_I(), false, iw);
      int by2 = y + h - mp - ch;
      int by1 = by2 - 2 - ch;
      int listTop = y + mp + lh;
      int listBottom = by1 - 4;
      int listH = Math.max(pitch, listBottom - listTop);
      int maxScroll = Math.max(0, init_I_I(accounts.size()) - listH);
      this.stop = init_I_I(this.stop, 0, maxScroll);
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(ix, listTop, iw, listH));
      try {
         int ry = listTop - this.stop;

         for (String[] acc : accounts) {
            if (ry + pitch > listTop && ry < listBottom) {
               boolean sel = this.load_LinkedHashSet.contains(acc[0]);
               int outline = sel ? this.poll_I() : this.close_I();
               autismclient.gui.f.PacketUtil.load(
                  g, autismclient.gui.f.NetHelper.init(ix, ry, iw, write_I()), 4, sel ? load_I_I_I(this.poll_I(), 48) : 705827348, outline
               );
               String box = sel ? "[x] " : "[ ] ";
               String cur = acc[2].isBlank() ? "" : "  -> " + acc[2];
               this.init_V_GuiGraphicsExtract_2(g, box + acc[1] + cur, ix + 4, this.init_I_I_I(ry, write_I()), sel ? this.poll_I() : this.pop_I(), false, iw - 8);
               String id = acc[0];
               this.read_List.add(new StateCache.Inner_BufferHelper(ix, ry, iw, write_I(), () -> {
                  if (!this.load_LinkedHashSet.remove(id)) {
                     this.load_LinkedHashSet.add(id);
                  }
               }));
            }

            ry += pitch;
         }
      } finally {
         HashUtil.init_HashUtil().init(g);
      }
      int selectAllW = Math.min(this.init_I_Str("Select all without a macro"), iw);
      this.init_V_GuiGraphicsExtract_2(
         g, ix + (iw - selectAllW) / 2, by1, selectAllW, ch, "Select all without a macro", this.close_I(), this.pop_I(), mx, my, () -> {
            this.load_LinkedHashSet.clear();

            for (String[] acc : this.init_List()) {
               if (acc[2].isBlank()) {
                  this.load_LinkedHashSet.add(acc[0]);
               }
            }
         }
      );
      String saveLabel = "Save (" + this.load_LinkedHashSet.size() + ")";
      int saveW = this.init_I_Str(saveLabel);
      int cancelW = this.init_I_Str("Cancel");
      if (saveW + 2 + cancelW <= iw) {
         int gx = ix + (iw - (saveW + 2 + cancelW)) / 2;
         this.init_V_GuiGraphicsExtract_2(g, gx, by2, saveW, ch, saveLabel, this.poll_I(), this.pop_I(), mx, my, () -> {
            this.init_V_Collection(new LinkedHashSet<>(this.load_LinkedHashSet));
            this.init_Z = false;
            this.init_V_Str_I("Assigned to " + this.load_LinkedHashSet.size() + " account(s)", -13248397);
         });
         this.init_V_GuiGraphicsExtract_2(g, gx + saveW + 2, by2, cancelW, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, () -> this.init_Z = false);
      } else {
         int half = (iw - 2) / 2;
         this.init_V_GuiGraphicsExtract_2(g, ix, by2, half, ch, saveLabel, this.poll_I(), this.pop_I(), mx, my, () -> {
            this.init_V_Collection(new LinkedHashSet<>(this.load_LinkedHashSet));
            this.init_Z = false;
            this.init_V_Str_I("Assigned to " + this.load_LinkedHashSet.size() + " account(s)", -13248397);
         });
         this.init_V_GuiGraphicsExtract_2(g, ix + half + 2, by2, iw - half - 2, ch, "Cancel", this.close_I(), this.pop_I(), mx, my, () -> this.init_Z = false);
      }
   }

   private void task() {
      if (this.init_LinkedHashSet.isEmpty()) {
         PacketUtil.init_PacketUtil().write_V_Str("");
      } else {
         PacketUtil.init_PacketUtil().init_I_Set_Str(this.init_LinkedHashSet, "");
      }

      if (!PacketUtil.init_PacketUtil().load_Z()) {
         this.init_FieldCache.sync = "";
      }

      this.init_V_Str_I("Assignment cleared", -6645094);
   }

   private void init_V_aM(aM macro) {
      String oldName = macro != null && macro.init_Str != null ? macro.init_Str : "";
      this.init_StateCache_CachePo.init_V_aM_Consumer(macro, saved -> {
         if (saved != null && saved.init_Str != null) {
            if (!oldName.isBlank() && !oldName.equals(saved.init_Str)) {
               TaskTimer.init_TaskTimer().init_I(oldName, saved.init_Str);
               PacketUtil.init_PacketUtil().load_V(oldName, saved.init_Str);
            }

            this.sync_Str = saved.init_Str;
            this.init_V_Str_I("Macro saved", -13248397);
         }
      });
   }

   private void pool() {
      aM macro = aN.init_aN().init_aM_Str(this.close_Str);
      String name = this.close_Str;
      this.close_Str = "";
      if (macro != null) {
         aN.init_aN().read(macro);
         TaskTimer.init_TaskTimer().init_I(name, "");
         PacketUtil.init_PacketUtil().load_V(name, "");
         if (name.equals(this.sync_Str)) {
            this.sync_Str = "";
         }

         this.init_V_Str_I("Macro deleted", -6645094);
      }
   }

   public boolean init_Z_I_2(int mx, int my, int button) {
      this.index = mx;
      if (autismclient.gui.f.b.SyncManager.init_Z_List_2(this.update_List, mx, my, button)) {
         this.read_V();
         return true;
      }

      if (!this.pop_Str.isBlank()) {
         if (button == 0) {
            for (int[] rect : this.poll_List) {
               if (mx >= rect[0] && mx < rect[0] + rect[2] && my >= rect[1] && my < rect[1] + rect[3]) {
                  this.init_V_I(rect[4]);
                  return true;
               }
            }
         }

         this.pop_Str = "";
         return true;
      } else {
         if (button == 1 && this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.read) {
            for (StateCache.Inner_TaskQueue row : this.open_List) {
               if (row.init(mx, my)) {
                  this.pop_Str = row.init_Str();
                  this.mark = mx;
                  this.scan = my;
                  return true;
               }
            }
         }

         if (button == 0 && this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.read) {
            for (int[] rect : this.flush_List) {
               if (mx >= rect[0] && mx < rect[0] + rect[2] && my >= rect[1] && my < rect[1] + rect[3]) {
                  this.flush(rect[4]);
                  this.close_BitSetUtil.init_V_Z(true);
                  return true;
               }
            }
         }

         autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);

         for (BitSetUtil input : this.write_List) {
            if (input.init_Z_SyncManager_2(ctx, mx, my, button)) {
               for (BitSetUtil other : this.write_List) {
                  if (other != input) {
                     other.init_V_Z(false);
                  }
               }

               return true;
            }
         }

         if (button == 0 && this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.read) {
            if (this.init_Z_I_I(mx, my)) {
               return true;
            }

            if (this.load_Z_I_I(mx, my)) {
               return true;
            }
         }

         this.read_V();
         if (button == 0) {
            for (int i = this.read_List.size() - 1; i >= 0; i--) {
               StateCache.Inner_BufferHelper hit = this.read_List.get(i);
               if (hit.init(mx, my)) {
                  try {
                     hit.init_Runnable().run();
                  } catch (Throwable error) {
                     bi.init("Multi action failed", this.push_I());
                  }

                  return true;
               }
            }

            if (this.init_Z || this.count >= 0 || !this.close_Str.isBlank()) {
               return true;
            }

            if (this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.read) {
               this.init_LinkedHashSet.clear();
            } else if (this.init_StateCache_JsonUti == StateCache.Inner_JsonUtil.update && this.sync_StateCache_StateCa.init(mx, my)) {
               this.sync_Str = "";
               this.close_Str = "";
            }
         }

         return mx >= this.block && mx < this.block + this.table && my >= this.chunk && my < this.chunk + this.store;
      }
   }

   public boolean load(int mx, int my, int button) {
      this.push_Str = null;
      this.init_IntConsumer = null;
      if (this.init_StateCache_NetHelp != null) {
         Runnable release = this.init_Runnable;
         this.init_StateCache_NetHelp = null;
         this.init_Runnable = null;
         if (release != null) {
            release.run();
         }

         return true;
      } else {
         if (autismclient.gui.f.b.SyncManager.write(this.update_List)) {
            return true;
         }

         if (this.update_Z) {
            this.update_Z = false;
            this.init_BufferHelper.init_V();
            return true;
         }

         autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);
         boolean result = false;

         for (BitSetUtil input : this.write_List) {
            result |= input.load_Z_SyncManager(ctx, mx, my, button);
         }

         return result;
      }
   }

   public boolean init_Z_I(int mx, int my, int button, double dx, double dy) {
      if (autismclient.gui.f.b.SyncManager.load(this.update_List, mx, my, button)) {
         return true;
      }

      if (this.init_StateCache_NetHelp != null) {
         this.init_StateCache_NetHelp.apply(mx, this.task, this.pool);
         return true;
      }

      if (this.push_Str != null) {
         this.poll(my);
         return true;
      }

      if (this.update_Z) {
         this.init_V_I(mx, my, false);
         return true;
      }

      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);
      boolean result = false;

      for (BitSetUtil input : this.write_List) {
         result |= input.init_Z_SyncManager(ctx, mx, my, button, (float)dx, (float)dy);
      }

      return result;
   }

   public boolean init_Z_I(int mx, int my, double amount) {
      if (autismclient.gui.f.b.SyncManager.init_Z_List(this.update_List, mx, my, amount)) {
         return true;
      }

      if (mx < this.block || mx >= this.block + this.table || my < this.chunk || my >= this.chunk + this.store) {
         return false;
      }

      if (this.init_Z) {
         this.stop = Math.max(0, this.stop - (int)Math.signum(amount) * update_I() * 2);
         return true;
      }

      if (this.count < 0 && this.close_Str.isBlank()) {
         int direction = (int)Math.signum(amount);
         if (direction == 0) {
            return true;
         }

         int step = (int)Math.signum(amount) * update_I() * 2;
         switch (this.init_StateCache_JsonUti) {
            case init:
               if (this.init_StateCache_StateCa.init(mx, my)) {
                  this.prev = Math.max(0, this.prev - step);
               } else if (this.load_StateCache_StateCa.init(mx, my)) {
                  this.valid = Math.max(0, this.valid - step);
               }
               break;
            case load:
               if (this.read_StateCache_StateCa.init(mx, my)) {
                  this.size = Math.max(0, this.size - step);
               }
               break;
            case read:
               if (this.update_StateCache_StateCa.init(mx, my)) {
                  this.value = Math.max(0, this.value + direction * 3);
               } else if (this.write_StateCache_StateCa.init(mx, my)) {
                  this.data = Math.max(0, this.data - step);
               }
            case write:
            default:
               break;
            case update:
               if (this.close_StateCache_StateCa.init(mx, my)) {
                  this.open(direction);
               } else if (this.flush_StateCache_StateCa.init(mx, my)) {
                  this.state = Math.max(0, this.state - step);
               } else if (this.sync_StateCache_StateCa.init(mx, my)) {
                  this.cache = Math.max(0, this.cache - step);
               }
         }

         return true;
      } else {
         return true;
      }
   }

   public boolean read(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 67 && (modifiers & 2) != 0 && this.init_BufferHelper.load_Z() && !this.close_BitSetUtil.load_Z()) {
         this.write_V();
         return true;
      }

      if (keyCode == 256) {
         if (this.init_BufferHelper.load_Z()) {
            this.init_BufferHelper.load_V();
            return true;
         }

         if (autismclient.gui.f.b.SyncManager.read(this.update_List)) {
            return true;
         }

         if (this.count >= 0) {
            this.state();
            return true;
         }

         if (!this.close_Str.isBlank()) {
            this.close_Str = "";
            return true;
         }

         if (this.init_Z) {
            this.init_Z = false;
            return true;
         }

         if (this.close_BitSetUtil.load_Z() && !this.sync_List.isEmpty()) {
            this.count();
            return true;
         }
      }

      if (this.close_BitSetUtil.load_Z() && keyCode == 258) {
         this.init_Z_Z((modifiers & 1) != 0);
         return true;
      }

      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.level, this.frame);

      for (BitSetUtil input : this.write_List) {
         if (input.load_Z() && input.init_Z_SyncManager_3(ctx, keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   private void index() {
      if (this.close_BitSetUtil.load_Z()) {
         String value = this.close_BitSetUtil.init_Str();
         if (!value.equals(this.load_Str) && !value.equals(this.init_Str)) {
            if (value.startsWith("/")) {
               this.sync(value);
            } else if (DataUtil.init_Z(value)) {
               this.flush(value);
            } else {
               this.count();
            }
         }
      } else if (!this.sync_List.isEmpty() || this.init_Str != null || this.load_Str != null) {
         this.count();
      }
   }

   private void sync(String value) {
      String stripped = value.substring(1);
      long now = System.currentTimeMillis();
      if (!value.equals(this.read_Str) && now - this.init_J >= 60L) {
         PacketUtil.init_PacketUtil().init_V_Str_Set(stripped, this.init_LinkedHashSet);
         this.read_Str = value;
         this.init_J = now;
      }

      PacketUtil.Inner_ByteReader r = PacketUtil.init_PacketUtil().init_PacketUtil_ByteRea_Str(stripped);
      if (r != null) {
         this.init_V_Str(value, 1 + r.init_I(), r.load(), r.init_List());
      }
   }

   private void flush(String value) {
      this.read_Str = null;
      DataUtil.Inner_StreamUtil r = DataUtil.init_DataUtil_StreamUti(value);
      if (r != null) {
         this.init_V_Str(value, r.init_I(), r.load(), r.init_List());
      }
   }

   private void init_V_Str(String value, int absStart, int length, List<String> entries) {
      if (!value.equals(this.init_Str)) {
         this.clear = 0;
      }

      this.init_Str = value;
      this.reset = absStart;
      this.start = length;
      this.sync_List.clear();
      this.sync_List.addAll(entries);
      if (this.clear >= this.sync_List.size()) {
         this.clear = Math.max(0, this.sync_List.size() - 1);
      }
   }

   private void flush(int index) {
      if (index >= 0 && index < this.sync_List.size()) {
         this.clear = index;
         String base = this.init_Str == null ? this.close_BitSetUtil.init_Str() : this.init_Str;
         int start = Math.max(0, Math.min(this.reset, base.length()));
         int end = Math.max(start, Math.min(this.reset + this.start, base.length()));
         String full = base.substring(0, start) + this.sync_List.get(index) + base.substring(end);
         this.close_BitSetUtil.init_BitSetUtil_Str(full);
         this.close_BitSetUtil.init_BitSetUtil();
         this.load_Str = full;
      }
   }

   private boolean init_Z_Z(boolean backwards) {
      String value = this.close_BitSetUtil.init_Str();
      boolean cycling = value.equals(this.load_Str);
      if (!this.sync_List.isEmpty() && (value.equals(this.init_Str) || cycling)) {
         if (cycling) {
            this.clear = Math.floorMod(this.clear + (backwards ? -1 : 1), this.sync_List.size());
         }

         String base = this.init_Str == null ? value : this.init_Str;
         int start = Math.max(0, Math.min(this.reset, base.length()));
         int end = Math.max(start, Math.min(this.reset + this.start, base.length()));
         String full = base.substring(0, start) + this.sync_List.get(this.clear) + base.substring(end);
         this.close_BitSetUtil.init_BitSetUtil_Str(full);
         this.close_BitSetUtil.init_BitSetUtil();
         this.close_BitSetUtil.init_V_Z(true);
         this.load_Str = full;
         return true;
      } else {
         return false;
      }
   }

   private void count() {
      this.sync_List.clear();
      this.flush_List.clear();
      this.init_Str = null;
      this.load_Str = null;
      this.read_Str = null;
      this.clear = 0;
   }

   public boolean init_Z_C_I(char chr, int modifiers) {
      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.level, this.frame);

      for (BitSetUtil input : this.write_List) {
         if (input.load_Z() && input.init_Z_SyncManager_2(ctx, chr, modifiers)) {
            return true;
         }
      }

      return false;
   }

   private void close(String text) {
      if (!this.write_Z) {
         RenderState.init_V_I(RenderState.init(text, RenderState.init_I()));
      }
   }

   private void close(int delayMs) {
      if (!this.flush_BitSetUtil.load_Z()) {
         this.open(RenderState.init_Str_I(delayMs));
      }
   }

   private void open(String want) {
      if (!this.flush_BitSetUtil.init_Str().equals(want)) {
         this.write_Z = true;

         try {
            this.flush_BitSetUtil.init_BitSetUtil_Str(want);
         } finally {
            this.write_Z = false;
         }
      }
   }

   private void open(int direction) {
      RenderState.load_V(direction);
      this.open(RenderState.init_Str_I(RenderState.init_I()));
   }

   public boolean init_Z() {
      for (BitSetUtil input : this.write_List) {
         if (input.load_Z()) {
            return true;
         }
      }

      return false;
   }

   public void read_V() {
      if (this.flush_BitSetUtil.load_Z()) {
         RenderState.init_V();
      }

      this.init_BitSetUtil.init_V_Z(false);
      this.load_BitSetUtil.init_V_Z(false);
      this.read_BitSetUtil.init_V_Z(false);
      this.write_BitSetUtil.init_V_Z(false);
      this.update_BitSetUtil.init_V_Z(false);
      this.sync_BitSetUtil.init_V_Z(false);
      this.close_BitSetUtil.init_V_Z(false);
      this.open_BitSetUtil.init_V_Z(false);
      this.poll_BitSetUtil.init_V_Z(false);
      this.push_BitSetUtil.init_V_Z(false);
      this.flush_BitSetUtil.init_V_Z(false);
   }

   private autismclient.gui.f.c.SyncManager init_SyncManager_GuiGraphicsExtract(GuiGraphicsExtractor g, int mx, int my) {
      return new autismclient.gui.f.c.SyncManager(g, this.init_Font, RingBuffer.init_RingBuffer_F(1.0F), this.init_CharEncoder, mx, my, this.init_F);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, BitSetUtil input, int x, int y, int w) {
      this.init_V_GuiGraphicsExtract_2(g, input, x, y, w, read_I());
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, BitSetUtil input, int x, int y, int w, int h) {
      input.init_TableIndex_F(x, y, Math.max(10, w), h);
      input.init_V_SyncManager(this.init_SyncManager_GuiGraphicsExtract(g, this.level, this.frame));
      if (this.init_Z_I(x, y, Math.max(10, w), h)) {
         this.write_List.add(input);
      } else {
         input.init_V_Z(false);
      }
   }

   private void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int outline, int color, int mx, int my, Runnable action
   ) {
      this.init_V_GuiGraphicsExtract(g, x, y, w, h, label, outline, color, mx, my, action, false);
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int outline, int color, int mx, int my, Runnable action, boolean playIcon
   ) {
      if (w > 0 && h > 0) {
         boolean enabled = action != null;
         boolean hover = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
         autismclient.gui.f.b.CachePool.Inner_DataUtil tone = outline == this.poll_I()
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write
            : (outline == this.push_I() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.update : autismclient.gui.f.b.CachePool.Inner_DataUtil.init);
         autismclient.gui.f.b.CachePool.init(
            autismclient.gui.f.UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, h), label, tone, hover, false
         );
         if (!enabled) {
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 3, 1711276032);
         }

         if (playIcon) {
            autismclient.gui.f.PacketUtil.read(g, x + 5, y + Math.max(1, (h - 8) / 2), 8, enabled ? color : this.tick_I());
         }

         if (enabled && this.init_Z_I(x, y, w, h)) {
            this.read_List.add(new StateCache.Inner_BufferHelper(x, y, w, h, action));
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, boolean active, int mx, int my, Runnable action) {
      if (w > 0 && h > 0) {
         boolean enabled = action != null;
         boolean hover = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
         int fill = active ? load_I_I_I(this.poll_I(), hover ? 110 : 82) : (hover ? load_I_I_I(this.open_I(), 52) : 705827348);
         autismclient.gui.f.PacketUtil.load(
            g, autismclient.gui.f.NetHelper.init(x, y, w, h), 3, enabled ? fill : 403771667, active ? this.poll_I() : this.close_I()
         );
         this.init_V_GuiGraphicsExtract_2(g, label, x + w / 2, this.init_I_I_I(y, h), enabled ? this.pop_I() : this.tick_I(), true, w - 8);
         if (enabled && this.init_Z_I(x, y, w, h)) {
            this.read_List.add(new StateCache.Inner_BufferHelper(x, y, w, h, action));
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int outline, int mx, int my, Runnable action) {
      if (h > 0 && w > 0) {
         boolean hover = action != null && mx >= x && mx < x + w && my >= y && my < y + h;
         boolean emphasized = outline != this.close_I();
         int fill = emphasized ? load_I_I_I(outline, hover ? 110 : 82) : (hover ? load_I_I_I(outline, 48) : 705827348);
         if (emphasized) {
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill, outline);
         } else {
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill);
         }

         this.init_V_GuiGraphicsExtract_2(g, label, x + 5, this.init_I_I_I(y, h), action == null ? this.tick_I() : this.pop_I(), false, w - 10);
         if (action != null && this.init_Z_I(x, y, w, h)) {
            this.read_List.add(new StateCache.Inner_BufferHelper(x, y, w, h, action));
         }
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor g, String title, String sub, int x, int y, int w, int h, int mx, int my, Runnable action) {
      int lh = sync_I();
      int ch = read_I();
      int top = y + Math.max(0, (h - (lh * 2 + 4 + ch)) / 2);
      this.init_V_GuiGraphicsExtract_2(g, title, x + w / 2, top, this.pop_I(), true, w - 12);
      this.init_V_GuiGraphicsExtract_2(g, sub, x + w / 2, top + lh, this.tick_I(), true, w - 12);
      int openW = this.init_I_Str("Open Setup");
      this.init_V_GuiGraphicsExtract_2(g, x + w / 2 - openW / 2, top + lh * 2 + 4, openW, ch, "Open Setup", this.poll_I(), this.pop_I(), mx, my, action);
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, String value, int x, int y, int color, boolean centered, int maxWidth) {
      String safe = update_Str_Str(value);
      Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
      safe = k.load(this.init_Font, safe, Math.max(1, maxWidth), fontId, color);
      int dx = centered ? x - k.init(this.init_Font, safe, fontId, color) / 2 : x;
      k.init(g, this.init_Font, safe, fontId, color, dx, y, false);
   }

   private int init_I_Str_I(String value, int color) {
      return k.init(this.init_Font, update_Str_Str(value), this.init_CharEncoder.init_Identifier_l(l.read), color);
   }

   private int init_I_Str(String label) {
      return autismclient.gui.f.RenderState.load(this.init_I_Str_I(label, 0));
   }

   private static int read_I() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int write_I() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int update_I() {
      return write_I() + 2;
   }

   private static int sync_I() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private static int init_I_I(int rows) {
      return Math.max(0, rows * update_I() - 2);
   }

   private int init_I_I_I(int y, int h) {
      return j.init(y, h, k.init(this.init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private void init_V_GuiGraphicsExtract_3(
      GuiGraphicsExtractor g, StateCache.Inner_StateCache viewport, int offset, int maxOffset, int mx, int my, String id, IntConsumer setOffset
   ) {
      if (viewport != StateCache.Inner_StateCache.init_StateCache_StateCa && viewport.write() >= 12 && maxOffset > 0) {
         int trackH = viewport.write() - 4;
         int thumbH = Math.min(trackH, Math.max(8, trackH * trackH / Math.max(trackH + maxOffset, 1)));
         int travel = Math.max(0, trackH - thumbH);
         int trackX = viewport.init_I() + viewport.read() - 6;
         int trackY = viewport.load() + 2;
         int top = trackY + (int)((long)travel * init_I_I(offset, 0, maxOffset) / maxOffset);
         boolean hovered = mx >= trackX && mx < trackX + 6 && my >= trackY && my < trackY + trackH;
         boolean dragging = id.equals(this.push_Str);
         UtilCore.init(
            autismclient.gui.f.UiHelper.init(g, this.init_Font, mx, my),
            new UtilCore.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(trackX, trackY, 6, trackH), autismclient.gui.f.NetHelper.init(trackX, top, 6, thumbH), maxOffset
            ),
            hovered,
            dragging
         );
         this.read_List
            .add(new StateCache.Inner_BufferHelper(trackX, trackY, 6, trackH, () -> this.init_V_Str(id, my, top, thumbH, trackY, travel, maxOffset, setOffset)));
      }
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor g, int x, int y, int w, int h, String label, double ratio, int mx, int my, StateCache.Inner_NetHelper setter, Runnable onRelease
   ) {
      if (w > 0 && h > 0) {
         int gap = 4;
         int room = Math.min(w / 2, this.init_I_Str_I(label, this.tick_I()) + 2);
         boolean labelled = w - room - gap >= 12;
         int labelW = labelled ? room : 0;
         int trackX = labelled ? x + labelW + gap : x;
         int trackW = labelled ? w - labelW - gap : w;
         if (labelled) {
            this.init_V_GuiGraphicsExtract_2(g, label, x, this.init_I_I_I(y, h), this.tick_I(), false, labelW);
         }

         boolean dragging = this.init_StateCache_NetHelp != null && this.task == trackX && this.pool == trackW;
         boolean hovered = mx >= trackX && mx < trackX + trackW && my >= y && my < y + h;
         c.init(
            autismclient.gui.f.UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(trackX, y, trackW, h), ratio, hovered || dragging
         );
         if (this.init_Z_I(trackX, y, trackW, h)) {
            this.read_List.add(new StateCache.Inner_BufferHelper(trackX, y, trackW, h, () -> {
               this.init_StateCache_NetHelp = setter;
               this.init_Runnable = onRelease;
               this.task = trackX;
               this.pool = trackW;
               setter.apply(this.index, trackX, trackW);
            }));
         }
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor g, ClassCache.Inner_FieldCache snapshot, int x, int y, int w, long now) {
      ClassCache.Inner_UiHelper progress = snapshot.init_ClassCache_UiHelpe();
      ClassCache.Inner_RenderState queue = snapshot.init_ClassCache_RenderS();
      double fill = -1.0;
      String line;
      int color;
      if (progress != null && progress.init_Z()) {
         line = snapshot.load_Str() + "  " + progress.init_I() + "/" + progress.load_I() + "  L" + progress.read() + "  " + progress.load_Str();
         color = this.poll_I();
         fill = progress.load_I() <= 0 ? 0.0 : (double)progress.init_I() / progress.load_I();
      } else if (queue != null && queue.init_Z(now)) {
         line = snapshot.load_Str() + "  waiting - starts in " + RenderState.init_Str_J(queue.init_J(now));
         color = -1526710;
         fill = queue.init_D(now);
      } else if (progress != null && !progress.init_Str().isBlank() && !progress.load_Str().isBlank()) {
         line = snapshot.load_Str() + "  " + progress.load_Str();
         color = progress.load_Str().toLowerCase(Locale.ROOT).contains("error") ? this.push_I() : this.tick_I();
      } else {
         line = snapshot.load_Str() + "  idle";
         color = this.tick_I();
      }

      this.init_V_GuiGraphicsExtract_2(g, line, x, y, color, false, w);
      if (!(fill < 0.0)) {
         int filled = (int)Math.round(w * Math.max(0.0, Math.min(1.0, fill)));
         autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y + 9, w, 2), 1, -869256388);
         if (filled > 0) {
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y + 9, filled, 2), 1, color);
         }
      }
   }

   private void init_V_Str(String id, int my, int thumbTop, int thumbH, int trackY, int travel, int maxOffset, IntConsumer setOffset) {
      this.queue = my >= thumbTop && my < thumbTop + thumbH ? my - thumbTop : thumbH / 2;
      this.push_Str = id;
      this.buffer = trackY;
      this.entry = travel;
      this.node = maxOffset;
      this.init_IntConsumer = setOffset;
   }

   private void poll(int my) {
      if (this.push_Str != null && this.init_IntConsumer != null) {
         int thumbTop = Math.max(this.buffer, Math.min(this.buffer + this.entry, my - this.queue));
         int value = this.entry <= 0 ? 0 : (thumbTop - this.buffer) * this.node / this.entry;
         this.init_IntConsumer.accept(Math.max(0, Math.min(this.node, value)));
      }
   }

   private List<StateCache.Inner_DataUtil> init_List_FieldCache(FieldCache profile) {
      String query = update_Str_Str(this.sync_BitSetUtil.init_Str()).toLowerCase(Locale.ROOT);
      int filterMask = 0;

      for (MethodUtil filter : this.init_EnumSet) {
         filterMask |= 1 << filter.ordinal();
      }

      long accountRevision = TokenStore.init_TokenStore().load_J();
      if (this.f_e_I < 0 || accountRevision != this.open_J) {
         TokenStore accountManager = TokenStore.init_TokenStore();
         this.get_List = List.copyOf(accountManager.init_List());
         this.f_e_I = this.get_List.size();
         this.open_J = accountRevision;
         this.write_FieldCache = null;
      }

      int sessionCount = profile == null ? 0 : profile.init_List.size();
      long uiRevision = PacketUtil.init_PacketUtil().load_J();
      if (query.equals(this.get_Str) && filterMask == this.f_f_I && profile == this.write_FieldCache && sessionCount == this.f_g_I && uiRevision == this.poll_J
         )
       {
         return this.set_List;
      }

      List<StateCache.Inner_DataUtil> out = new ArrayList<>();
      String defaultName = autismclient.util.FieldCache.init_User().getName();
      StateCache.Inner_DataUtil current = new StateCache.Inner_DataUtil(
         "default",
         defaultName + " (Default)",
         autismclient.util.FieldCache.init_User().getAccessToken().isBlank() ? MethodUtil.init : MethodUtil.load,
         PacketUtil.init_Z_Str("default")
      );
      if (this.init_EnumSet.contains(current.init_MethodUtil()) && current.load().toLowerCase(Locale.ROOT).contains(query)) {
         out.add(current);
      }

      Set<String> knownAccountIds = new HashSet<>(this.get_List.size() * 2 + 1);

      for (autismclient.util.BitSetUtil account : this.get_List) {
         MethodUtil type = account.init_MethodUtil == null ? MethodUtil.init : account.init_MethodUtil;
         String label = update_Str_Str(account.init_Str());
         knownAccountIds.add(account.write_Str());
         if (this.init_EnumSet.contains(type) && label.toLowerCase(Locale.ROOT).contains(query)) {
            out.add(new StateCache.Inner_DataUtil(account.write_Str(), label, type, PacketUtil.init_Z_Str(account.write_Str())));
         }
      }

      for (FieldCache.Inner_BufferHelper spec : profile == null ? List.<FieldCache.Inner_BufferHelper>of() : profile.init_List) {
         boolean known = spec.init_Str().equals("default") || knownAccountIds.contains(spec.init_Str());
         if (!known && (query.isBlank() || spec.init_Str().toLowerCase(Locale.ROOT).contains(query))) {
            out.add(new StateCache.Inner_DataUtil(spec.init_Str(), "Missing: " + spec.init_Str(), MethodUtil.init, false));
         }
      }

      this.get_Str = query;
      this.f_f_I = filterMask;
      this.write_FieldCache = profile;
      this.f_g_I = sessionCount;
      this.poll_J = uiRevision;
      this.set_List = List.copyOf(out);
      return this.set_List;
   }

   private List<cY.Inner_DataUtil> load_List() {
      String query = update_Str_Str(this.open_BitSetUtil.init_Str()).toLowerCase(Locale.ROOT);
      long revision = aN.init_aN().init_J();
      if (revision == this.close_J && query.equals(this.run_Str)) {
         return this.run_List;
      }

      this.close_J = revision;
      this.run_Str = query;
      this.run_List = aN.init_aN().init_List_Str(query);
      return this.run_List;
   }

   private List<FieldCache> read_List() {
      TaskTimer manager = TaskTimer.init_TaskTimer();
      long revision = manager.init_J_2();
      if (revision != this.write_J) {
         this.write_J = revision;
         this.pop_List = manager.init_List();
      }

      return this.pop_List;
   }

   private FieldCache init_FieldCache_PacketUtil(PacketUtil manager) {
      long uiRevision = manager.load_J();
      long sessionRevision = manager.init_J();
      if (uiRevision != this.update_J || sessionRevision != this.sync_J) {
         this.update_J = uiRevision;
         this.sync_J = sessionRevision;
         this.read_FieldCache = manager.init_FieldCache();
      }

      return this.read_FieldCache;
   }

   private List<String> init_List_Str(String name) {
      long revision = aN.init_aN().init_J();
      String safeName = name == null ? "" : name;
      if (revision == this.push_J && safeName.equals(this.set_Str)) {
         return this.put_List;
      }

      this.push_J = revision;
      this.set_Str = safeName;
      this.put_List = PacketUtil.init_PacketUtil().init_List_Str(safeName);
      return this.put_List;
   }

   private FieldCache.Inner_BufferHelper init_FieldCache_BufferH_Str(String id) {
      return init_FieldCache_BufferH_FieldCache_Str(this.init_FieldCache, id);
   }

   private static FieldCache.Inner_BufferHelper init_FieldCache_BufferH_FieldCache_Str(FieldCache profile, String id) {
      if (profile == null) {
         return null;
      }

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         if (spec.init_Str().equals(id)) {
            return spec;
         }
      }

      return null;
   }

   private String write_Str(String id) {
      return this.init_Str_FieldCache_Str(this.init_FieldCache, id);
   }

   private String init_Str_FieldCache_Str(FieldCache profile, String id) {
      FieldCache.Inner_CachePool mode = profile == null ? FieldCache.Inner_CachePool.init : profile.init_FieldCache_CachePo;
      if (mode == FieldCache.Inner_CachePool.init) {
         return "Proxy Off";
      }

      if (mode == FieldCache.Inner_CachePool.load) {
         return "Best Proxy";
      }

      if (id == null || id.isBlank()) {
         return "Proxy Off";
      }

      if ("best".equals(id)) {
         return "Best Proxy";
      }

      ProxyEntry proxy = ProxyManager.init_bY().init_bW_Str(id);
      return proxy == null ? "Missing" : proxy.init_Str();
   }

   private String init_Str() {
      String var10000;
      switch (this.init_FieldCache.init_FieldCache_CachePo) {
         case init:
            var10000 = "Off";
            break;
         case load:
            var10000 = "Auto";
            break;
         case read:
            var10000 = "Manual";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void init_V_SyncManager(autismclient.gui.f.b.SyncManager dropdown, int x, int y, int width, int selected, String label) {
      dropdown.init_SyncManager_I(x, y, Math.max(1, width), read_I()).init_SyncManager_I(selected).init_SyncManager_Str(label);
      dropdown.init_Z = true;
      this.update_List.add(dropdown);
   }

   private static String init_Str_FieldCache(FieldCache profile) {
      if (profile != null && !profile.init_List.isEmpty()) {
         String common = profile.init_List.getFirst().load_Str();

         for (int i = 1; i < profile.init_List.size(); i++) {
            if (!Objects.equals(common, profile.init_List.get(i).load_Str())) {
               return null;
            }
         }

         return common == null ? "" : common;
      } else {
         return "";
      }
   }

   private static Map<String, Integer> init_Map_FieldCache(FieldCache profile) {
      if (profile != null && !profile.init_List.isEmpty()) {
         Map<String, Integer> usage = new LinkedHashMap<>();

         for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
            if (!spec.init_Z() && !spec.load_Z()) {
               usage.merge(spec.load_Str(), 1, Integer::sum);
            }
         }

         return Map.copyOf(usage);
      } else {
         return Map.of();
      }
   }

   private static List<String> write_List() {
      List<String> labels = new ArrayList<>(init_IArr.length);

      for (int option : init_IArr) {
         labels.add(option + "ms");
      }

      return labels;
   }

   private static int load_I_I(int value) {
      int index = 0;
      int best = 2147483647;

      for (int i = 0; i < init_IArr.length; i++) {
         int distance = Math.abs(init_IArr[i] - value);
         if (distance < best) {
            best = distance;
            index = i;
         }
      }

      return index;
   }

   private String load_Str() {
      return this.init_Minecraft != null && this.init_Minecraft.getCurrentServer() != null && this.init_Minecraft.getCurrentServer().ip != null
         ? update_Str_Str(this.init_Minecraft.getCurrentServer().ip)
         : "";
   }

   private int read(ClassCache.Inner_FieldCache snapshot) {
      int var10000;
      switch (snapshot.init_ClassCache_JsonUti(System.currentTimeMillis())) {
         case init:
            var10000 = this.poll_I();
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

   private void init_V_PacketUtil_CachePo(PacketUtil.Inner_CachePool result) {
      if (result == null) {
         this.init_V_Str_I("Failed", -42149);
      } else if (result.read() > 0) {
         this.init_V_Str_I("Failed " + result.read(), -42149);
      } else if (result.init_I() > 0) {
         this.init_V_Str_I("Sent " + result.init_I(), -13248397);
      } else if (result.load() > 0) {
         this.init_V_Str_I("Skipped", -6645094);
      } else {
         this.init_V_Str_I("Nothing sent", -6645094);
      }
   }

   private void init_V_Str_I(String text, int color) {
      this.open_Str = PacketUtil.update(text, 80);
      this.next = color;
   }

   private int flush_I() {
      if (this.next == -13248397) {
         return this.poll_I();
      } else if (this.next == -42149) {
         return this.push_I();
      } else {
         return this.next == -14249 ? this.run_I() : this.tick_I();
      }
   }

   private void flags() {
   }

   private int close_I() {
      return ct.init_I_I_ct_DataUtil(-12174784, ct.Inner_DataUtil.load);
   }

   private int open_I() {
      return ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.init);
   }

   private int poll_I() {
      return ct.init_I_I_ct_DataUtil(-13248397, ct.Inner_DataUtil.sync);
   }

   private int push_I() {
      return ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.flush);
   }

   private int pop_I() {
      return ct.init_I_I_ct_DataUtil(-855310, ct.Inner_DataUtil.read);
   }

   private int tick_I() {
      return ct.init_I_I_ct_DataUtil(-6645094, ct.Inner_DataUtil.read);
   }

   private int run_I() {
      return ct.init_I_I_ct_DataUtil(-14249, ct.Inner_DataUtil.init);
   }

   private static int load_I_I_I(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static int init_I_I(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private boolean init_Z_I(int x, int y, int w, int h) {
      return this.open_StateCache_StateCa == StateCache.Inner_StateCache.init_StateCache_StateCa || this.open_StateCache_StateCa.init(x, y, w, h);
   }

   private static int load_I_Str_I(String value, int fallback) {
      try {
         return Integer.parseInt(value.trim());
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static String update_Str_Str(String value) {
      return PacketUtil.update(value == null ? "" : value.replace(' ', ' '), 512);
   }

   private boolean read_Z() {
      if (this.init_Minecraft != null && this.init_Minecraft.getWindow() != null) {
         long window = this.init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(window, 341) == 1 || CompatGLFW.glfwGetKey(window, 345) == 1;
      } else {
         return false;
      }
   }

   private boolean write_Z() {
      if (this.init_Minecraft != null && this.init_Minecraft.getWindow() != null) {
         long window = this.init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(window, 340) == 1 || CompatGLFW.glfwGetKey(window, 344) == 1;
      } else {
         return false;
      }
   }

   private record Inner_BufferHelper(int init_I, int load, int read, int write, Runnable init_Runnable) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   public interface Inner_CachePool {
      void init_V();

      void init_V_Str(String var1, String var2, String var3, Map<String, Integer> var4, Consumer<String> var5);

      void init_V(String var1);

      boolean init_Z(String var1);

      void load_V(String var1);

      boolean load_Z(String var1);

      boolean read_Z_Str(String var1);

      void load_V_2();

      boolean init_Z();

      void init(Consumer<Class<? extends Packet<?>>> var1);

      void init_V_IoWorker_DataUtil(
         IoWorker.Inner_DataUtil var1, Collection<Class<? extends Packet<?>>> var2, BiConsumer<Class<? extends Packet<?>>, Boolean> var3
      );

      void init_V_aM_Consumer(aM var1, Consumer<aM> var2);

      void init_V_FieldCache(FieldCache var1, Set<String> var2, Consumer<FieldCache> var3);

      void init_V_CachePool(autismclient.util.g.CachePool var1, Consumer<autismclient.util.g.CachePool> var2, boolean var3);
   }

   private record Inner_ConfigStore(String init_Str, int init_I, Runnable init_Runnable) {
   }

   private record Inner_DataUtil(String init_Str, String load, MethodUtil init_MethodUtil, boolean init_Z) {
   }

   private enum Inner_JsonUtil {
      init,
      load,
      read,
      write,
      update;
   }

   private interface Inner_NetHelper {
      void apply(double var1, int var3, int var4);
   }

   private record Inner_StateCache(int init_I, int load, int read, int write) {
      private static final StateCache.Inner_StateCache init_StateCache_StateCa = new StateCache.Inner_StateCache(0, 0, 0, 0);

      boolean init(int mx, int my) {
         return this.read > 0 && this.write > 0 && mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }

      boolean init(int rx, int ry, int rw, int rh) {
         return rw > 0 && rh > 0 && rx >= this.init_I && ry >= this.load && rx + rw <= this.init_I + this.read && ry + rh <= this.load + this.write;
      }

      autismclient.gui.f.NetHelper init_NetHelper() {
         return autismclient.gui.f.NetHelper.init(this.init_I, this.load, Math.max(1, this.read), Math.max(1, this.write));
      }
   }

   private record Inner_StreamUtil(
      PacketUtil.Inner_BufferHelper init_PacketUtil_BufferH, int init_I, int load, FormattedText init_FormattedText, String init_Str
   ) {
   }

   private record Inner_TaskQueue(int init_I, int load, int read, int write, String init_Str) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
