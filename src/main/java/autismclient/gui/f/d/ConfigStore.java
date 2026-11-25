package autismclient.gui.f.d;

import autismclient.api.module.Setting;
import autismclient.gui.f.JsonUtil;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.RenderState;
import autismclient.gui.f.RingBuffer;
import autismclient.gui.f.SyncManager;
import autismclient.gui.f.TableIndex;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.ClassCache;
import autismclient.gui.f.b.EventBus;
import autismclient.gui.f.b.FieldCache;
import autismclient.gui.f.b.MethodUtil;
import autismclient.gui.f.b.RefQueue;
import autismclient.gui.f.b.UtilCore;
import autismclient.gui.f.b.e;
import autismclient.gui.f.b.g;
import autismclient.gui.f.b.h;
import autismclient.gui.f.b.i;
import autismclient.gui.f.b.k;
import autismclient.modules.Module;
import autismclient.modules.ae;
import autismclient.modules.al;
import autismclient.modules.n;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.ValueSink;
import autismclient.util.aM;
import autismclient.util.bk;
import autismclient.util.ck;
import autismclient.util.cz;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public final class ConfigStore {
   private static final int init_I = 290;
   private static final int load_I = 344;
   private static final int read_I = 4;
   private static final int write_I = 3;
   private static final int update_I = 5;
   private static final int sync_I = 12;
   private static final int flush_I = 5;
   private static final String load_Str = "\u0000general";
   private static final String read_Str = "\u0000profiles";
   private static final String write_Str = "main-menu";
   private static final String update_Str = "settings-window";
   private static final String sync_Str = "search";
   private static final int close_I = 150;
   private static final int open_I = 360;
   private static final int poll_I = 14;
   private static final int push_I = 2;
   private static final long init_J = 350L;
   private static final int pop_I = -536870912;
   private static final List<String> init_List = new ArrayList<>();
   private static final ConfigStore.Inner_ConfigStore init_ConfigStore_Config = new ConfigStore.Inner_ConfigStore(
      "macros", "Macros", autismclient.gui.f.a.DataUtil.sync
   );
   private static final ConfigStore.Inner_ConfigStore load_ConfigStore_Config = new ConfigStore.Inner_ConfigStore(
      "admin", "Admin Tools", autismclient.gui.f.a.DataUtil.flush
   );
   private static final ConfigStore.Inner_ConfigStore read_ConfigStore_Config = new ConfigStore.Inner_ConfigStore(
      "ai", "AI Assistant", autismclient.gui.f.a.DataUtil.get
   );
   private static final ConfigStore.Inner_ConfigStore write_ConfigStore_Config = new ConfigStore.Inner_ConfigStore(
      "schematics", "Schematics", autismclient.gui.f.a.DataUtil.clear
   );
   private static final List<ConfigStore.Inner_ConfigStore> load_List = List.of(
      new ConfigStore.Inner_ConfigStore("lan", "LAN Sync", autismclient.gui.f.a.DataUtil.update),
      new ConfigStore.Inner_ConfigStore("queue", "Queue", autismclient.gui.f.a.DataUtil.poll),
      new ConfigStore.Inner_ConfigStore("logger", "Logger", autismclient.gui.f.a.DataUtil.open),
      new ConfigStore.Inner_ConfigStore("packets", "Packets", autismclient.gui.f.a.DataUtil.close),
      new ConfigStore.Inner_ConfigStore("server", "Server Info", autismclient.gui.f.a.DataUtil.push),
      new ConfigStore.Inner_ConfigStore("keys", "Settings", autismclient.gui.f.a.DataUtil.write),
      new ConfigStore.Inner_ConfigStore("multi", "Multi", autismclient.gui.f.a.DataUtil.put)
   );
   private static final ConfigStore.Inner_DataUtil[] init_ConfigStore_DataUtArr = new ConfigStore.Inner_DataUtil[]{
      new ConfigStore.Inner_DataUtil("flush", "Flush", autismclient.gui.f.b.CachePool.Inner_DataUtil.init),
      new ConfigStore.Inner_DataUtil("clear", "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init)
   };
   private static final Map<String, Float> init_Map = new HashMap<>();
   private static final Map<String, int[]> load_Map = new HashMap<>();
   private static final Map<String, Boolean> read_Map = new HashMap<>();
   private static String flush_Str = null;
   private static int tick = -1;
   private static int run = -1;
   private final ConfigStore.Inner_BufferHelper init_ConfigStore_Buffer;
   private final BufferHelper init_BufferHelper;
   private final List<ConfigStore.Inner_StreamUtil> read_List = new ArrayList<>();
   private final Deque<NetHelper> init_Deque = new ArrayDeque<>();
   private final Map<String, Float> write_Map = new HashMap<>();
   private int get;
   private int set;
   private boolean init_Z;
   private final RingBuffer init_RingBuffer = new RingBuffer(0.0F, 180);
   private int put = 17;
   private int add = 90;
   private int clear = 14;
   private int reset = 15;
   private int start = 13;
   private int stop;
   private int next;
   private boolean load_Z;
   private int prev;
   private int valid;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private final Map<String, RingBuffer> update_Map = new HashMap<>();
   private final RingBuffer load_RingBuffer = new RingBuffer(0.0F, 180);
   private boolean sync_Z;
   private int size;
   private int data;
   private String close_Str;
   private String open_Str;
   private int value;
   private int cache;
   private int state;
   private String poll_Str;
   private long load_J;
   private final Map<String, NetHelper> sync_Map = new HashMap<>();
   private int queue = -1;
   private int buffer;
   private int entry;
   private long read_J;
   private long write_J;
   private Module init_Module;
   private autismclient.gui.c.StreamUtil init_StreamUtil;
   private NetHelper load_NetHelper;
   private long update_J;
   private boolean flush_Z;
   private StreamUtil load_StreamUtil;
   private String push_Str = "";
   private NetHelper read_NetHelper = NetHelper.init(0, 0, 1, 1);
   private Module load_Module;
   private Module read_Module;
   private Setting<?, ?> init_Setting;
   private long sync_J;
   StreamUtil init_StreamUtil_2;
   NetHelper init_NetHelper;
   private boolean close_Z;
   private boolean open_Z;
   private RefQueue init_RefQueue;
   private Module write_Module;
   private Setting<?, ?> load_Setting;
   private autismclient.gui.f.b.NetHelper init_NetHelper_2;
   private CachePool init_CachePool;
   private Module update_Module;
   private Setting<?, ?> read_Setting;
   private int node = -1;
   private String pop_Str;
   private int task;
   String init_Str;
   private final Map<String, UtilCore.Inner_DataUtil> flush_Map = new HashMap<>();
   private Map<String, Integer> close_Map;
   private boolean poll_Z;

   public ConfigStore(ConfigStore.Inner_BufferHelper host) {
      this.init_ConfigStore_Buffer = host;
      this.init_BufferHelper = new BufferHelper(this);
      this.read_J = System.nanoTime();
      this.write_J = this.read_J;
   }

   public void init_V() {
      AutismConfig config = AutismConfig.getGlobal();
      if (tick < 0 && config.menuV2X >= 0) {
         tick = config.menuV2X;
         run = config.menuV2Y;
      }

      if (flush_Str == null) {
         flush_Str = "\u0000general";
      }
   }

   public boolean init_Z() {
      return this.init_StreamUtil_2 != null
         || this.load_StreamUtil != null
         || this.load_Module != null
         || this.init_Setting != null
         || this.load_Z()
         || this.sync_Z() && this.init_StreamUtil.load_Z();
   }

   public boolean load_Z() {
      return this.init_RefQueue != null || this.init_NetHelper_2 != null || this.init_CachePool != null || this.flush_Z();
   }

   public boolean read_Z() {
      return this.init_Module != null;
   }

   public void load_V() {
      this.init_V_Z(true);
      if (this.load_StreamUtil != null) {
         this.poll();
      }

      if (this.update_Module != null) {
         this.update_Module.persistConfiguredState();
      }

      this.update_Module = null;
      this.read_Setting = null;
      this.load_Z = false;
      this.close_Str = null;
      this.open_Str = null;
      this.pop_Str = null;
      this.update_Z = true;
      this.push();
      this.pop();
      this.flush_V();
   }

   public void read_V() {
      this.update_Z = false;
   }

   public void init_V_Str(String moduleId) {
      Module module = moduleId == null ? null : al.init_Module_Str(moduleId);
      if (module != null) {
         this.load_V_Module(module);
      }
   }

   public void load_V_Str(String categoryKeyOrLabel) {
      if ("profiles".equalsIgnoreCase(categoryKeyOrLabel)) {
         this.open("\u0000profiles");
      } else {
         if (categoryKeyOrLabel != null) {
            for (ae category : ae.init_List()) {
               if (category.init_Str().equalsIgnoreCase(categoryKeyOrLabel) || category.load().equalsIgnoreCase(categoryKeyOrLabel)) {
                  this.open(category.init_Str());
                  return;
               }
            }
         }

         this.open("\u0000general");
      }
   }

   public void init_V_Str_2(String id, boolean collapsed, boolean snap) {
      String key = id == null ? "single" : id;
      if (id == null) {
         this.write_Z = collapsed;
      } else {
         this.init_AutismConfig_Modul_Str(id).collapsed = collapsed;
      }

      if (snap) {
         this.update_Map.computeIfAbsent(key, ignored -> new RingBuffer(collapsed ? 0.0F : 1.0F, 180)).init_V_F(collapsed ? 0.0F : 1.0F);
      }
   }

   public Map<String, NetHelper> init_Map() {
      Map<String, NetHelper> out = new LinkedHashMap<>();

      for (ConfigStore.Inner_StreamUtil hit : this.read_List) {
         if (hit.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.init) {
            out.putIfAbsent(hit.init_Str == null ? "" : hit.init_Str, hit.init_NetHelper);
         }
      }

      return out;
   }

   public boolean init_Z_Str(String id) {
      return id != null && !id.isEmpty() ? this.init_AutismConfig_Modul_Str(id).collapsed : this.write_Z;
   }

   public void read_V_Str(String query) {
      this.update(query);
   }

   public void write_V() {
      ConfigStore.Inner_StreamUtil hit = this.read_List
         .stream()
         .filter(candidate -> candidate.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.update && "\u0000profiles".equals(candidate.init_Str))
         .findFirst()
         .orElseThrow();
      int x = hit.init_NetHelper.read() + hit.init_NetHelper.update() / 2;
      int y = hit.init_NetHelper.write() + hit.init_NetHelper.sync() / 2;
      this.init_Z_I_2(x, y, 0);
      this.load_Z_I(x, y, 0);
      if (!"\u0000profiles".equals(flush_Str)) {
         throw new IllegalStateException("Profiles rail item did not open");
      }
   }

   public void update_V() {
      NetHelper area = this.load_NetHelper;
      if (area == null) {
         throw new IllegalStateException("Profiles Library was not rendered");
      }

      int x = area.read() + area.update() / 4;
      int y = area.write() + RenderState.init(13) + 12;
      this.init_Z_I_2(x, y, 0);
      this.load_Z_I(x, y, 0);
   }

   public NetHelper init_NetHelper_Str(String optionId) {
      ConfigStore.Inner_StreamUtil hit = this.read_List
         .stream()
         .filter(
            candidate -> candidate.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.start
               && candidate.init_Setting != null
               && candidate.init_Setting.id().equals(optionId)
         )
         .findFirst()
         .orElseThrow();
      int x = hit.init_NetHelper.read() + hit.init_NetHelper.update() / 2;
      int y = hit.init_NetHelper.write() + hit.init_NetHelper.sync() / 2;
      this.init_Z_I_2(x, y, 0);
      this.load_Z_I(x, y, 0);
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         return this.init_RefQueue.read_NetHelper();
      } else {
         throw new IllegalStateException("Dropdown did not open");
      }
   }

   public void init_V_Str_D(String optionId, double fraction) {
      ConfigStore.Inner_StreamUtil hit = this.read_List
         .stream()
         .filter(
            candidate -> candidate.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.add
               && candidate.init_Setting != null
               && candidate.init_Setting.id().equals(optionId)
         )
         .findFirst()
         .orElseThrow();
      int x = hit.init_NetHelper.read() + (int)Math.round((hit.init_NetHelper.update() - 1) * Math.clamp(fraction, 0.0, 1.0));
      int y = hit.init_NetHelper.write() + hit.init_NetHelper.sync() / 2;
      this.init_Z_I_2(x, y, 0);
      this.load_Z_I(x, y, 0);
   }

   public void init_V_Str_Str(String moduleId, String group) {
      this.init_V_Str(moduleId);
      if (this.init_Module != null) {
         for (Setting<?, ?> option : this.init_Module.settings()) {
            if (option.group() != null) {
               read_Map.put(moduleId + "|" + option.group(), group != null && !group.equals(option.group()));
            }
         }

         this.init_V_Str_F("settings", 0.0F);
      }
   }

   public void sync_V() {
      String previousQuery = this.push_Str;
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str("main-menu");
      boolean previousCollapse = layout.collapsed;
      boolean custom = AutismConfig.getGlobal().classicMenuLayoutCustom;
      int previousX = layout.f_x_I;
      int previousY = layout.f_y_I;

      try {
         this.load_StreamUtil = new StreamUtil("search", "selection", value -> {});
         int start = this.read_NetHelper.read() + 4;
         int y = this.read_NetHelper.write() + this.read_NetHelper.sync() / 2;
         int end = start + k.init(this.init_ConfigStore_Buffer.init_Font(), "selec", autismclient.gui.f.a.DataUtil.init, 0);
         this.init_Z_I_2(start, y, 0);
         this.init_Z_I(end, y, 0, end - start, 0.0);
         this.load_Z_I(end, y, 0);
         if (this.load_StreamUtil == null || this.load_StreamUtil.init_I() != 0 || this.load_StreamUtil.load_I() != 5) {
            throw new IllegalStateException("Search drag selection did not select the expected text");
         }

         this.init_Z_I(start, y, 0, 1);
         this.load_Z_I(start, y, 0);
         if (this.load_StreamUtil.init_Z()) {
            throw new IllegalStateException("Shift-click did not preserve the anchor");
         }

         this.load_StreamUtil = null;
         ConfigStore.Inner_StreamUtil header = this.read_List
            .stream()
            .filter(hit -> hit.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.init && "main-menu".equals(hit.init_Str))
            .findFirst()
            .orElseThrow();
         int hx = header.init_NetHelper.read() + 32;
         int hy = header.init_NetHelper.write() + header.init_NetHelper.sync() / 2;
         this.init_Z_I_2(hx, hy, 0);
         this.init_Z_I(hx + 1, hy, 0, 1.0, 0.0);
         this.load_Z_I(hx + 1, hy, 0);
         if (layout.collapsed == previousCollapse) {
            throw new IllegalStateException("Header click with normal pointer jitter did not collapse");
         }

         layout.collapsed = previousCollapse;
         this.init_Z_I_2(hx, hy, 0);
         this.init_Z_I(hx + 12, hy, 0, 12.0, 0.0);
         this.load_Z_I(hx + 12, hy, 0);
         if (layout.collapsed != previousCollapse) {
            throw new IllegalStateException("Dragging a header changed collapse state");
         }
      } finally {
         this.load_StreamUtil = null;
         this.open_Z = false;
         this.push_Str = previousQuery;
         layout.collapsed = previousCollapse;
         layout.f_x_I = previousX;
         layout.f_y_I = previousY;
         AutismConfig.getGlobal().classicMenuLayoutCustom = custom;
         this.init_ConfigStore_Buffer.load();
      }
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int width, int height) {
      long now = System.nanoTime();
      float frameSeconds = Math.min(0.1F, (float)(now - this.write_J) / 1.0E9F);
      this.write_J = now;
      this.stop = width;
      this.next = height;
      this.init_Str = null;
      this.read_List.clear();
      this.init_Deque.clear();
      SyncManager popupContext = UiHelper.init(graphics, this.init_ConfigStore_Buffer.init_Font(), mouseX, mouseY).init_SyncManager();
      SyncManager context = this.load_Z() ? popupContext.load_SyncManager() : popupContext;
      this.load_NetHelper = null;
      TableIndex theme = context.init_TableIndex();
      this.put = RenderState.init(14) + 3;
      this.clear = RenderState.init(14);
      this.reset = RenderState.init(14) + 1;
      this.start = RenderState.init(13);
      this.init_BufferHelper.init_V_Z(this.write_Z());
      float settingsProgress = this.init_Module != null && !this.sync_Z ? 1.0F : 0.0F;
      if (this.sync_Z && settingsProgress <= 0.001F) {
         this.init_Module = null;
         this.sync_Z = false;
      }

      if (this.write_Z()) {
         this.close();
         this.init_V_SyncManager_2(context, frameSeconds, now);
         this.init_V_SyncManager_3(popupContext, mouseX, mouseY);
      } else {
         if (this.init_Module == null && "\u0000profiles".equals(flush_Str) && !AutismLiteVariant.enabled() && this.init_StreamUtil == null) {
            this.init_StreamUtil = new autismclient.gui.c.StreamUtil(this.init_ConfigStore_Buffer.init_Font());
         }

         this.add = Math.min(this.load_I_SyncManager(context), Math.max(45, width / 3));
         int contentW = init_I_I(344 + (this.init_Module instanceof n ? 140 : 0));
         int w = Math.min(this.add + 1 + contentW + 2, Math.max(160, width - 8));
         int contentWidth = w - this.add - 3;
         List<Module> modules = this.init_Module == null && this.init_StreamUtil == null && !this.update_Z() ? this.init_List() : List.of();
         int naturalHeight = this.init_Module != null
            ? this.init_BufferHelper.init_I_Module(this.init_Module)
            : (
               this.init_StreamUtil != null
                  ? this.init_StreamUtil.init(contentWidth)
                  : (this.update_Z() ? this.load_I_I(contentWidth) : this.init_I_I_I(modules.size(), read_I_I(contentWidth)))
            );
         int expandedH = Math.min(Math.min(290, Math.max(1, height - 8)), this.put + Math.max(this.load_I(), naturalHeight) + 1);
         if (this.init_RingBuffer.init_F() <= 0.0F) {
            this.init_RingBuffer.init_V_F(expandedH);
         }

         int easedHeight = Math.round(this.init_RingBuffer.init_F_F(expandedH));
         int h = this.put + this.init_I_Str("single", Math.max(0, easedHeight - this.put), this.write_Z);
         if (!this.init_Z) {
            this.get = tick >= 0 ? tick : (width - w) / 2;
            this.set = run >= 0 ? run : (height - Math.min(290, height - 8)) / 2;
            this.init_Z = true;
         }

         this.get = init_I_I_2(this.get, 4, Math.max(4, width - w - 4));
         this.set = init_I_I_2(this.set, 4, Math.max(4, height - h - 4));
         NetHelper window = NetHelper.init(this.get, this.set, w, h);
         PacketUtil.init_V_SyncManager(context, window, 8, theme.init_JsonUtil().update, theme.init_JsonUtil().get);
         NetHelper header = ClassCache.init(window, this.put);
         i.init(context, header, h <= this.put + 1, this.load_Z);
         this.init_V_ConfigStore_CacheP(
            ConfigStore.Inner_CachePool.init, NetHelper.init(window.read(), window.write(), window.update(), this.put), null, null, "", -1
         );
         this.load_V_SyncManager_NetHelper(context, window);
         if (h <= this.put) {
            this.init_V_SyncManager_3(popupContext, mouseX, mouseY);
         } else {
            NetHelper body = NetHelper.init(window.read() + 1, window.write() + this.put, window.update() - 2, window.sync() - this.put - 1);
            int targetBodyH = Math.max(0, expandedH - this.put - 1);
            NetHelper rail = NetHelper.init(body.read(), body.write(), this.add, targetBodyH);
            NetHelper content = NetHelper.init(rail.init() + 1, body.write(), body.update() - this.add - 1, targetBodyH);
            this.init_V_SyncManager_NetHelper(context, body);

            try {
               this.init(context, popupContext, rail, content, body, modules, frameSeconds, delta);
            } finally {
               this.init_V_SyncManager(context);
            }

            this.init_V_SyncManager_3(popupContext, mouseX, mouseY);
         }
      }
   }

   private void init(
      SyncManager context, SyncManager popupContext, NetHelper rail, NetHelper content, NetHelper body, List<Module> modules, float frameSeconds, float delta
   ) {
      this.read(context, rail);
      PacketUtil.load(context.init_GuiGraphicsExtract(), rail.init(), body.write(), body.sync(), context.init_TableIndex().init_JsonUtil().put);
      if (this.init_Module != null) {
         this.init_V_SyncManager_NetHelper(context, content);

         try {
            this.init_BufferHelper.init_V_SyncManager_2(context, content, this.init_Module, frameSeconds);
         } finally {
            this.init_V_SyncManager(context);
         }
      } else if (this.init_StreamUtil != null) {
         this.load_NetHelper = NetHelper.init(content.read(), content.write(), content.update(), Math.min(content.sync(), body.sync()));
         SyncManager profileContext = this.flush_Z() ? popupContext : context;
         if (!this.load_NetHelper.init_Z(profileContext.read(), profileContext.write())) {
            profileContext = profileContext.load_SyncManager();
         }

         this.init_StreamUtil.init_V_SyncManager(profileContext, content, delta);
      } else if (this.update_Z()) {
         this.init_V_SyncManager_4(context, content, frameSeconds);
      } else {
         this.init_V_SyncManager_3(context, content, modules, frameSeconds);
      }
   }

   private void init_V_SyncManager_3(SyncManager context, int mouseX, int mouseY) {
      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_V_SyncManager(context);
      }

      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.init_V_SyncManager(context);
      }

      if (this.init_CachePool != null) {
         this.init_CachePool.init(context);
      }

      if (this.init_Str != null && !this.init_Str.isBlank() && !this.init_Z_I_I(mouseX, mouseY)) {
         h.init(context, this.init_Str, mouseX, mouseY);
      }
   }

   private boolean init_Z_I_I(int mouseX, int mouseY) {
      if (this.load_Z || this.close_Str != null || this.open_Str != null) {
         return true;
      }

      if (bk.init_bk().write_Z()) {
         return true;
      }

      ConfigStore.Inner_StreamUtil hit = this.init_ConfigStore_Stream_I_I(mouseX, mouseY);
      return hit != null && hit.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.cache;
   }

   private void load_V_SyncManager_NetHelper(SyncManager context, NetHelper window) {
      JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      String title = this.init_Module != null
         ? this.init_Module.name()
         : (
            this.init_StreamUtil != null
               ? "Profiles Library"
               : (this.update_Z() ? "Main Menu" : (!this.push_Str.isBlank() ? "Search results" : (this.init_ae() != null ? this.init_ae().load() : "Modules")))
         );
      NetHelper bar = NetHelper.init(window.read(), window.write(), window.update(), this.put);
      int controlH = RenderState.init(13);
      int controlY = window.write() + Math.max(1, (this.put - controlH) / 2);
      NetHelper close = NetHelper.init(window.init() - 3 - controlH, controlY, controlH, controlH);
      int titleX = window.read() + this.add + 4 + 2;
      int titleEnd = close.read() - 4;
      if (this.init_StreamUtil == null || this.init_Module != null) {
         int searchW = Math.min(init_I_I(112), Math.max(44, (close.read() - titleX) / 2));
         this.read_NetHelper = NetHelper.init(titleEnd - searchW, controlY, searchW, controlH);
         titleEnd = this.read_NetHelper.read() - 4;
         this.init_V_SyncManager_Str(context, this.init_Module == null ? "Search modules" : "Search settings");
      }

      int titleY = context.init_PoolEntry().init_I(bar);
      context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), "AUTISM", window.read() + 4 + 3, titleY, this.add - 8, colors.valid);
      context.init_PoolEntry().read(context.init_GuiGraphicsExtract(), title, titleX, titleY, titleEnd - titleX, colors.prev);
      float closeT = autismclient.gui.f.DataUtil.init(autismclient.gui.f.DataUtil.init(close), close.init_Z(context.read(), context.write()));
      PacketUtil.read(context.init_GuiGraphicsExtract(), close.init(2), init_I_I(colors.valid, colors.data, closeT));
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.write, close, null, null, "", -1);
   }

   private void read(SyncManager context, NetHelper rail) {
      int y = rail.write() + 3 - this.init_I_NetHelper_I(rail, this.load_I());
      int x = rail.read() + 3;
      int w = rail.update() - 6;
      this.init_V_SyncManager_NetHelper(context, rail);

      try {
         for (ae category : ae.init_List()) {
            if (!init_List_ae(category).isEmpty()) {
               this.init_V_SyncManager_2(context, NetHelper.init(x, y, w, this.clear), category.load(), category.init_Str());
               y += this.clear + 1;
            }
         }

         PacketUtil.init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), x + 2, y + 1, w - 4, context.init_TableIndex().init_JsonUtil().put);
         y += 4;
         this.init_V_SyncManager_2(context, NetHelper.init(x, y, w, this.clear), "Profiles", "\u0000profiles");
         y += this.clear + 1;
         this.init_V_SyncManager_2(context, NetHelper.init(x, y, w, this.clear), "Main Menu", "\u0000general");
      } finally {
         this.init_V_SyncManager(context);
      }
   }

   private void init_V_SyncManager_2(SyncManager context, NetHelper row, String label, String key) {
      boolean selected = key.equals(flush_Str) && (!"\u0000general".equals(key) || this.update_Z());
      boolean hovered = row.init_Z(context.read(), context.write())
         && (this.init_Deque.peek() == null || this.init_Deque.peek().init_Z(context.read(), context.write()));
      float hoverT = autismclient.gui.f.DataUtil.init("rail:" + key, hovered);
      float selectT = autismclient.gui.f.BufferHelper.init("railsel:" + key, selected);
      JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      if (hoverT > 0.001F || selectT > 0.001F) {
         int fill = init_I_I(PacketUtil.init(colors.push, hoverT), colors.start, selectT);
         if (selectT > 0.001F) {
            PacketUtil.load(context.init_GuiGraphicsExtract(), row, 3, fill, PacketUtil.init(colors.clear, selectT * 0.65F));
         } else {
            PacketUtil.load(context.init_GuiGraphicsExtract(), row, 3, fill);
         }
      }

      int textColor = !selected && !hovered ? colors.valid : colors.prev;
      int iconSize = Math.min(12, row.sync() - 2);
      int iconX = row.read() + 5;
      int iconY = row.write() + (row.sync() - iconSize) / 2;
      cz.init(context.init_GuiGraphicsExtract(), init_Identifier_Str(key), iconX, iconY, iconSize, textColor);
      int labelX = iconX + iconSize + 5;
      context.init_PoolEntry().read(context.init_GuiGraphicsExtract(), label, labelX, context.init_PoolEntry().init_I(row), row.init() - 5 - labelX, textColor);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.update, row, null, null, key, -1);
   }

   private static Identifier init_Identifier_Str(String key) {
      Identifier var10000;
      switch (key) {
         case "\u0000general":
            var10000 = autismclient.gui.f.a.DataUtil.pop;
            break;
         case "\u0000profiles":
            var10000 = autismclient.gui.f.a.DataUtil.add;
            break;
         case "COMBAT":
            var10000 = autismclient.gui.f.a.DataUtil.reset;
            break;
         case "MOVEMENT":
            var10000 = autismclient.gui.f.a.DataUtil.start;
            break;
         case "PLAYER":
            var10000 = autismclient.gui.f.a.DataUtil.stop;
            break;
         case "MISC":
            var10000 = autismclient.gui.f.a.DataUtil.next;
            break;
         case "RENDER":
            var10000 = autismclient.gui.f.a.DataUtil.prev;
            break;
         default:
            var10000 = autismclient.gui.f.a.DataUtil.valid;
      }

      return var10000;
   }

   private List<Module> init_List() {
      String query = this.push_Str.strip();
      if (query.isEmpty()) {
         ae category = this.init_ae();
         return category == null ? List.of() : init_List_ae(category);
      }

      List<Module> matches = new ArrayList<>();

      for (ae category : ae.init_List()) {
         for (Module module : init_List_ae(category)) {
            if (this.init_Z_Str_Module(query, module)) {
               matches.add(module);
            }
         }
      }

      return matches;
   }

   private void init_V_SyncManager_3(SyncManager context, NetHelper content, List<Module> modules, float frameSeconds) {
      int columns = read_I_I(content.update());
      int contentHeight = this.init_I_I_I(modules.size(), columns);
      int scroll = this.init_I_Str("modules", content, contentHeight, frameSeconds);
      int width = content.update() - 8 - (contentHeight > content.sync() ? 6 : 0);
      int cardW = (width - (columns - 1) * 4) / columns;
      int pitch = this.read_I();
      int cardH = pitch - 4;
      this.init_V_SyncManager_NetHelper(context, content);

      try {
         for (int i = 0; i < modules.size(); i++) {
            int x = content.read() + 4 + i % columns * (cardW + 4);
            int y = content.write() + 4 + i / columns * pitch - scroll;
            if (y + cardH > content.write() && y < content.load()) {
               this.init_V_SyncManager_5(context, modules.get(i), NetHelper.init(x, y, cardW, cardH));
            }
         }

         if (modules.isEmpty() && !this.push_Str.isBlank()) {
            context.init_PoolEntry()
               .load(
                  context.init_GuiGraphicsExtract(),
                  "No matching modules",
                  NetHelper.init(content.read(), content.write() + 4, content.update(), this.clear),
                  context.init_TableIndex().init_JsonUtil().valid
               );
         }
      } finally {
         this.init_V_SyncManager(context);
      }

      this.init_V_SyncManager(context, "modules", content, contentHeight, scroll, frameSeconds);
   }

   private void init_V_SyncManager_5(SyncManager context, Module module, NetHelper card) {
      TableIndex theme = context.init_TableIndex();
      NetHelper clip = this.init_Deque.peek();
      boolean hovered = card.init_Z(context.read(), context.write()) && (clip == null || clip.init_Z(context.read(), context.write()));
      autismclient.gui.f.b.BufferHelper.init(context, card, "mod:" + module.id(), module.isEnabled(), hovered);
      NetHelper row = NetHelper.init(card.read(), card.write() + 1, card.update() - 2, this.reset);
      String label = this.load_Module == module ? "Press key" : module.name();
      boolean hasSettings = this.init_BufferHelper.init_Z_Module(module);
      int iconSize = Math.min(13, row.sync() - 2);
      NetHelper settings = NetHelper.init(row.init() - 1 - iconSize, row.write() + (row.sync() - iconSize) / 2, iconSize, iconSize);
      NetHelper pill = init_NetHelper_NetHelper_I(row, settings.read() - 4);
      int tail = module.hasActivationToggle() ? pill.read() : settings.read();
      int bindH = Math.max(13, row.sync() - 4);
      NetHelper bindButton = NetHelper.init(tail - 4 - 34, row.write() + (row.sync() - bindH) / 2, 34, bindH);
      int labelMax = Math.max(1, bindButton.read() - row.read() - 9);
      int nameColor = module.isEnabled() ? theme.init_JsonUtil().stop : theme.init_JsonUtil().prev;
      if (!this.push_Str.isBlank()) {
         this.init(context, label, row.read() + 5, context.init_PoolEntry().init_I(row), labelMax, nameColor, this.push_Str.trim());
      } else {
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), label, row.read() + 5, context.init_PoolEntry().init_I(row), labelMax, nameColor);
      }

      String description = module.description();
      NetHelper descriptionLine = NetHelper.init(card.read() + 5, row.load(), card.update() - 10, card.load() - 4 - row.load());
      context.init_PoolEntry()
         .read(
            context.init_GuiGraphicsExtract(),
            description,
            card.read() + 5,
            context.init_PoolEntry().init_I(descriptionLine),
            descriptionLine.update(),
            theme.init_JsonUtil().valid
         );
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.flush, card, module, null, "", -1);
      EventBus.init(context, bindButton, module.keybind(), this.load_Module == module, bindButton.init_Z(context.read(), context.write()));
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.close, bindButton, module, null, "", -1);
      if (module.hasActivationToggle()) {
         g.init(context, pill, module.isEnabled(), pill.init_Z(context.read(), context.write()), "module-toggle:" + module.id());
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.push, pill, module, null, "", -1);
      }

      if (hasSettings) {
         autismclient.gui.f.b.RingBuffer.init(
            context.init_GuiGraphicsExtract(),
            this.init_ConfigStore_Buffer.init_Font(),
            settings,
            cz.sync,
            settings.init_Z(context.read(), context.write()),
            true,
            false
         );
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.open, settings, module, null, "", -1);
      }

      if (hovered && description != null && context.init_PoolEntry().init(description) > card.update() - 10) {
         this.init_Str = description;
      }
   }

   private void init_V_SyncManager_4(SyncManager context, NetHelper content, float frameSeconds) {
      int contentHeight = this.load_I_I(content.update());
      int scroll = this.init_I_Str("general", content, contentHeight, frameSeconds);
      int gutter = contentHeight > content.sync() ? 6 : 0;
      this.init_V_SyncManager_NetHelper(context, content);

      try {
         this.init_I_SyncManager(context, content.read() + 4, content.write() + 4 - scroll, content.update() - 8 - gutter);
      } finally {
         this.init_V_SyncManager(context);
      }

      this.init_V_SyncManager(context, "general", content, contentHeight, scroll, frameSeconds);
   }

   private int load_I_I(int width) {
      return this.init_I_SyncManager(null, 0, 0, Math.max(1, width - 8)) + 8;
   }

   private int init_I_SyncManager(SyncManager context, int x, int top, int w) {
      boolean inWorld = !this.init_ConfigStore_Buffer.init_Z();
      int y = top;
      y = inWorld
         ? this.init_I_SyncManager_2(context, init_ConfigStore_Config, load_ConfigStore_Config, x, y, w)
         : this.init_I_SyncManager(context, init_ConfigStore_Config, x, y, w, false);

      for (int i = 0; i + 1 < load_List.size(); i += 2) {
         y = this.init_I_SyncManager_2(context, load_List.get(i), load_List.get(i + 1), x, y, w);
      }

      y = this.init_I_SyncManager_2(context, read_ConfigStore_Config, write_ConfigStore_Config, x, y, w);
      if (inWorld) {
         y = this.init_I_SyncManager_3(context, "PACKET", autismclient.gui.f.a.DataUtil.tick, x, y, w);
         if (context != null) {
            this.init_V_SyncManager(context, x, y, w);
         }

         y += this.start;
         y = this.init_I_SyncManager(context, init_ConfigStore_DataUtArr, x, y, w);
      }

      return y - top + 1;
   }

   private int init_I_SyncManager_2(SyncManager context, ConfigStore.Inner_ConfigStore left, ConfigStore.Inner_ConfigStore right, int x, int y, int w) {
      if (context != null) {
         NetHelper[] cells = init_NetHelperArr_I(x, y, w, this.start);
         autismclient.gui.f.b.CachePool.Inner_DataUtil leftTone = init_CachePool_DataUtil_ConfigStore_Config(left);
         autismclient.gui.f.b.CachePool.Inner_DataUtil rightTone = init_CachePool_DataUtil_ConfigStore_Config(right);
         this.init_V_SyncManager_2(context, left, leftTone, cells[0], MethodUtil.write);
         this.init_V_SyncManager_2(context, right, rightTone, cells[1], MethodUtil.write);
         this.init_V_SyncManager(context, cells[1].read(), y, leftTone, rightTone);
      }

      return y + this.start;
   }

   private int init_I_SyncManager(SyncManager context, ConfigStore.Inner_ConfigStore entry, int x, int y, int w, boolean bottom) {
      if (context != null) {
         this.init_V_SyncManager_2(
            context,
            entry,
            init_CachePool_DataUtil_ConfigStore_Config(entry),
            NetHelper.init(x, y, w, this.start),
            bottom ? MethodUtil.init(true, true) : MethodUtil.write
         );
      }

      return y + this.start;
   }

   private int init_I_SyncManager(SyncManager context, ConfigStore.Inner_DataUtil[] row, int x, int y, int w) {
      if (context != null) {
         NetHelper[] cells = init_NetHelperArr_I(x, y, w, this.start);
         this.init_V_SyncManager_4(context, row[0], cells[0], MethodUtil.init(true, false));
         this.init_V_SyncManager_4(context, row[1], cells[1], MethodUtil.init(false, true));
         this.init_V_SyncManager(context, cells[1].read(), y, row[0].init_CachePool_DataUtil(), row[1].init_CachePool_DataUtil());
      }

      return y + this.start;
   }

   private int init_I_SyncManager_3(SyncManager context, String label, Identifier icon, int x, int y, int w) {
      int labelH = RenderState.init(14);
      if (context != null) {
         MethodUtil.init(context, NetHelper.init(x, y, w, labelH), label, icon);
      }

      return y + labelH;
   }

   private void init_V_SyncManager(SyncManager context, int x, int y, int w) {
      NetHelper[] toggles = init_NetHelperArr_I(x, y, w, this.start);
      float sendT = autismclient.gui.f.BufferHelper.init("packet:send", ck.init_ck().read_Z());
      float delayT = autismclient.gui.f.BufferHelper.init("packet:delay", ck.init_ck().write_Z());
      MethodUtil.init(context, toggles[0], "Send", null, toggles[0].init_Z(context.read(), context.write()), sendT, MethodUtil.write);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.sync, toggles[0], null, null, "send", -1);
      MethodUtil.init(context, toggles[1], "Delay", null, toggles[1].init_Z(context.read(), context.write()), delayT, MethodUtil.write);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.sync, toggles[1], null, null, "delay", -1);
      MethodUtil.init(
         context,
         toggles[1].read(),
         y,
         this.start,
         MethodUtil.init(
            MethodUtil.init(context, sendT),
            MethodUtil.init_F_CachePool_DataUtil(autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, sendT),
            MethodUtil.init(context, delayT),
            MethodUtil.init_F_CachePool_DataUtil(autismclient.gui.f.b.CachePool.Inner_DataUtil.init, true, delayT)
         )
      );
   }

   private void init_V_SyncManager(
      SyncManager context, int x, int y, autismclient.gui.f.b.CachePool.Inner_DataUtil left, autismclient.gui.f.b.CachePool.Inner_DataUtil right
   ) {
      MethodUtil.init(
         context,
         x,
         y,
         this.start,
         MethodUtil.init(
            MethodUtil.init(context, left),
            MethodUtil.init_F_CachePool_DataUtil(left, false, 0.0F),
            MethodUtil.init(context, right),
            MethodUtil.init_F_CachePool_DataUtil(right, false, 0.0F)
         )
      );
   }

   private static NetHelper[] init_NetHelperArr_I(int x, int y, int w, int h) {
      int leftW = w / 2;
      return new NetHelper[]{NetHelper.init(x, y, leftW, h), NetHelper.init(x + leftW, y, w - leftW, h)};
   }

   private void init_V_SyncManager_2(
      SyncManager context,
      ConfigStore.Inner_ConfigStore entry,
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone,
      NetHelper cell,
      MethodUtil.Inner_DataUtil edges
   ) {
      MethodUtil.init(context, cell, init_Str_ConfigStore_Config(entry), entry.init_Identifier(), tone, cell.init_Z(context.read(), context.write()), edges);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.sync, cell, null, null, entry.init_Str(), -1);
   }

   private void init_V_SyncManager_4(SyncManager context, ConfigStore.Inner_DataUtil action, NetHelper cell, MethodUtil.Inner_DataUtil edges) {
      MethodUtil.init(context, cell, action.load(), null, action.init_CachePool_DataUtil(), cell.init_Z(context.read(), context.write()), edges);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.sync, cell, null, null, action.init_Str(), -1);
   }

   private static String init_Str_ConfigStore_Config(ConfigStore.Inner_ConfigStore entry) {
      if (entry == load_List.get(load_List.size() - 1)) {
         autismclient.util.g.PacketUtil multi = autismclient.util.g.PacketUtil.init_PacketUtil();
         if (multi.load_Z()) {
            return "Multi " + multi.read_Str();
         }
      }

      return entry.load();
   }

   private static autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil_ConfigStore_Config(ConfigStore.Inner_ConfigStore entry) {
      return entry == load_List.get(load_List.size() - 1) && autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write
         : autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
   }

   private int init_I_SyncManager(SyncManager context) {
      boolean inWorld = !this.init_ConfigStore_Buffer.init_Z();
      int pair = 54;
      List<ConfigStore.Inner_ConfigStore> iconCells = new ArrayList<>(load_List);
      iconCells.add(init_ConfigStore_Config);
      iconCells.add(read_ConfigStore_Config);
      iconCells.add(write_ConfigStore_Config);
      if (inWorld) {
         iconCells.add(load_ConfigStore_Config);
      }

      for (ConfigStore.Inner_ConfigStore entry : iconCells) {
         pair = Math.max(pair, init_I_SyncManager(context, entry.load(), true));
      }

      pair = Math.max(pair, init_I_SyncManager(context, "Multi 500", true));
      if (inWorld) {
         for (String label : new String[]{"Send", "Delay"}) {
            pair = Math.max(pair, init_I_SyncManager(context, label, false));
         }

         for (ConfigStore.Inner_DataUtil cell : init_ConfigStore_DataUtArr) {
            pair = Math.max(pair, init_I_SyncManager(context, cell.load(), false));
         }
      }

      int widest = Math.max(pair * 2, context.init_PoolEntry().init("Main Menu") + 11 + 3 + 16);
      return Math.max(120, widest) + 2;
   }

   private static int init_I_SyncManager(SyncManager context, String label, boolean icon) {
      return context.init_PoolEntry().init(label) + 10 + 2 + (icon ? 13 : 0);
   }

   public boolean init_Z_I_2(int mx, int my, int button) {
      return this.init_Z_I(mx, my, button, 0);
   }

   public boolean init_Z_I(int mx, int my, int button, int modifiers) {
      if (this.sync_Z) {
         return true;
      }

      if (this.init_Z_I(button)) {
         return true;
      }

      if (this.sync_Z()) {
         boolean overProfiles = this.load_NetHelper != null && this.load_NetHelper.init_Z(mx, my);
         if (overProfiles) {
            return this.init_StreamUtil.init_Z_I_2(mx, my, button);
         }

         if (this.flush_Z()) {
            return true;
         }

         this.init_StreamUtil.load_V();
      }

      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_EventBus_I_2(mx, my, button);
         if (!this.init_RefQueue.init_Z_2()) {
            this.push();
         }

         return true;
      } else if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.init_Z_I_2(mx, my, button);
         if (!this.init_NetHelper_2.init_Z()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else if (this.init_CachePool != null) {
         if (!this.init_CachePool.init(mx, my, button, modifiers)) {
            this.flush_V();
         }

         return true;
      } else {
         ConfigStore.Inner_StreamUtil clicked = this.init_ConfigStore_Stream_I_I(mx, my);
         boolean sameField = this.init_StreamUtil_2 != null
            && clicked != null
            && clicked.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.reset
            && clicked.init_Setting == this.init_StreamUtil_2.init_Obj;
         if (!sameField) {
            this.init_V_Z(true);
         }

         this.close_Z = false;
         this.open_Z = false;
         if (this.load_StreamUtil != null
            && (clicked == null || clicked.init_ConfigStore_CacheP != ConfigStore.Inner_CachePool.reset || !"search".equals(clicked.init_Str))) {
            this.poll();
         }

         for (int i = this.read_List.size() - 1; i >= 0; i--) {
            ConfigStore.Inner_StreamUtil hit = this.read_List.get(i);
            if (hit.init_NetHelper.init_Z(mx, my) && (hit.load == null || hit.load.init_Z(mx, my))) {
               if (button != 0
                  && hit.init_ConfigStore_CacheP != ConfigStore.Inner_CachePool.flush
                  && hit.init_ConfigStore_CacheP != ConfigStore.Inner_CachePool.init) {
                  return true;
               }

               switch (hit.init_ConfigStore_CacheP) {
                  case init:
                     if (this.write_Z()) {
                        this.init_V_Str(hit.init_Str, mx, my, button);
                        return true;
                     }

                     if (button != 0) {
                        return true;
                     }

                     this.load_Z = true;
                     this.prev = mx;
                     this.valid = my;
                     this.read_Z = false;
                     this.size = mx - this.get;
                     this.data = my - this.set;
                     return true;
                  case load:
                     if (this.write_Z()) {
                        this.flush(hit.init_Str);
                     }

                     return true;
                  case read:
                     if (button == 0) {
                        this.close(hit.init_Str);
                     }

                     return true;
                  case write:
                     if ("settings-window".equals(hit.init_Str)) {
                        this.open();
                     } else {
                        this.init_ConfigStore_Buffer.init_V();
                     }

                     return true;
                  case update:
                     this.open(hit.init_Str);
                     return true;
                  case sync:
                     if (button != 0) {
                        return true;
                     }

                     this.init_ConfigStore_Buffer.init(hit.init_Str);
                     return true;
                  case flush:
                     if (button == 1) {
                        this.load_V_Module(hit.init_Module);
                     } else if (button == 2) {
                        this.load_Module = hit.init_Module;
                     } else if (button == 0) {
                        if (this.init_BufferHelper.init_Z_Module(hit.init_Module)
                           && (hit.init_Module.opensSettingsOnClick() || !hit.init_Module.hasActivationToggle())) {
                           this.load_V_Module(hit.init_Module);
                        } else {
                           this.init_V_Module(hit.init_Module);
                        }
                     }

                     return true;
                  case close:
                     if (button == 0) {
                        this.load_Module = hit.init_Module;
                     }

                     return true;
                  case open:
                     if (button == 0) {
                        this.load_V_Module(hit.init_Module);
                     }

                     return true;
                  case poll:
                     this.open();
                     return true;
                  case push:
                     if (hit.init_Module.hasActivationToggle()) {
                        this.init_V_Module(hit.init_Module);
                     }

                     return true;
                  case pop:
                     this.load_Module = hit.init_Module;
                     return true;
                  case tick:
                     if (this.init_ConfigStore_Buffer.init_Z()) {
                        hit.init_Module.resetConfiguredSettings();
                     } else {
                        hit.init_Module.resetSettings();
                     }

                     this.read_Module = null;
                     this.init_Setting = null;
                     AutismClientMessaging.sendPrefixed(hit.init_Module.name() + " settings reset to defaults.");
                     return true;
                  case run:
                     Module module = hit.init_Module;
                     this.open();
                     this.init_ConfigStore_Buffer.init(module);
                     return true;
                  case get:
                     read_Map.put(hit.init_Module.id() + "|" + hit.init_Str, !read_Map.getOrDefault(hit.init_Module.id() + "|" + hit.init_Str, false));
                     return true;
                  case set:
                     this.init_BufferHelper.init_V(hit.init_Module, hit.init_Setting, button);
                     return true;
                  case put:
                     if (hit.init_Setting != null && hit.init_Setting.action() != null) {
                        hit.init_Setting.action().run();
                     }

                     return true;
                  case add:
                     this.update_Module = hit.init_Module;
                     this.read_Setting = hit.init_Setting;
                     this.node = -1;
                     this.init_BufferHelper.init_V_ConfigStore_Stream_I(hit, mx);
                     return true;
                  case clear:
                     this.update_Module = hit.init_Module;
                     this.read_Setting = hit.init_Setting;
                     this.node = this.init_BufferHelper.init_I_ConfigStore_Stream_I(hit, mx);
                     this.init_BufferHelper.init_V_ConfigStore_Stream_I(hit, mx);
                     return true;
                  case reset:
                     if (button != 0) {
                        return true;
                     }

                     boolean extend = (modifiers & 1) != 0;
                     if ("search".equals(hit.init_Str)) {
                        this.load_V_I_Z(mx, extend);
                     } else if (this.init_StreamUtil_2 != null && this.init_StreamUtil_2.init_Obj == hit.init_Setting) {
                        this.init_StreamUtil_2.init(this.init_I_StreamUtil(this.init_StreamUtil_2, mx, hit.init_NetHelper), extend);
                        this.close_Z = true;
                     } else {
                        this.init_V_Module(hit.init_Module, hit.init_Setting, mx, hit.init_NetHelper);
                     }

                     return true;
                  case start:
                     this.init_V_ConfigStore_Stream(hit);
                     return true;
                  case stop:
                     this.init_V_Module(hit.init_Module, hit.init_Setting, hit.init_NetHelper);
                     return true;
                  case next:
                     if (hit.init_Setting != null) {
                        switch (hit.init_Setting.kind()) {
                           case STRING_LIST:
                              this.init_ConfigStore_Buffer.load(hit.init_Module, hit.init_Setting);
                              break;
                           default:
                              this.init_ConfigStore_Buffer.read(hit.init_Module, hit.init_Setting);
                        }
                     }

                     return true;
                  case prev:
                     this.init_ConfigStore_Buffer.init(hit.init_Module, hit.init_Setting);
                     return true;
                  case valid:
                     this.load_V_Module(hit.init_Module, hit.init_Setting, hit.init_NetHelper);
                     return true;
                  case size:
                     this.read_Module = hit.init_Module;
                     this.init_Setting = hit.init_Setting;
                     return true;
                  case data:
                     this.init_V_Module_2(hit.init_Module, hit.init_Setting, "-1");
                     return true;
                  case value:
                     UtilCore.Inner_DataUtil metrics = this.init_UtilCore_DataUtil_Str(hit.init_Str);
                     if (metrics != null && metrics.init_I() > 0) {
                        this.task = metrics.load(mx, my) ? my - metrics.load().write() : metrics.load().sync() / 2;
                        this.pop_Str = hit.init_Str;
                        this.init_V_Str_F(hit.init_Str, UtilCore.init_I_UtilCore_DataUtil(metrics, my, this.task));
                     }

                     return true;
                  case cache:
                     this.init_V_ConfigStore_Stream(hit, mx, my);
                     return true;
               }
            }
         }

         return true;
      }
   }

   public boolean load_Z_I(int mx, int my, int button) {
      if (button != 0) {
         return true;
      }

      if (this.sync_Z() && this.init_StreamUtil.load(mx, my, button)) {
         return true;
      }

      Module releasedSlider = this.update_Module;
      this.close_Z = false;
      this.open_Z = false;
      this.pop_Str = null;
      this.update_Module = null;
      this.read_Setting = null;
      this.node = -1;
      if (releasedSlider != null) {
         releasedSlider.persistConfiguredState();
      }

      if (this.open_Str != null) {
         this.open_Str = null;
         this.init_ConfigStore_Buffer.load();
         return true;
      }

      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.load(mx, my, button);
         if (!this.init_NetHelper_2.init_Z()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else if (this.init_RefQueue != null) {
         this.init_RefQueue.load(mx, my, button);
         if (!this.init_RefQueue.init_Z_2()) {
            this.push();
         }

         return true;
      } else {
         if (this.init_CachePool != null) {
            this.init_CachePool.load(mx, my, button);
            return true;
         }

         if (button == 0 && this.load_Z) {
            this.load_Z = false;
            if (!this.read_Z && this.load_Z_I_I(mx, my)) {
               this.write_Z = !this.write_Z;
            }

            tick = this.get;
            run = this.set;
            AutismConfig config = AutismConfig.getGlobal();
            config.menuV2X = this.get;
            config.menuV2Y = this.set;
            this.init_ConfigStore_Buffer.load();
         }

         if (button == 0 && this.close_Str != null) {
            String releasedId = this.close_Str;
            this.close_Str = null;
            if (!this.read_Z && this.load_Z_I_I(mx, my)) {
               this.close(releasedId);
            } else if (this.read_Z) {
               AutismConfig.getGlobal().classicMenuLayoutCustom = true;
               this.init_ConfigStore_Buffer.load();
            }
         }

         return true;
      }
   }

   public boolean init_Z_I(int mx, int my, int button, double dx, double dy) {
      if (button != 0) {
         return true;
      }

      if (this.sync_Z() && this.init_StreamUtil.init_Z_I(mx, my, button, dx, dy)) {
         return true;
      }

      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.init_Z_I(mx, my, button, dx, dy);
         if (!this.init_NetHelper_2.init_Z()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else {
         if (this.init_RefQueue != null) {
            this.init_RefQueue.init_EventBus_I(mx, my, button, dx, dy);
            return true;
         }

         if (this.init_CachePool != null) {
            this.init_CachePool.init(mx, my, button, dx, dy);
            return true;
         }

         if (this.open_Z && this.load_StreamUtil != null) {
            this.load_StreamUtil.init(this.init_I_StreamUtil(this.load_StreamUtil, mx, this.read_NetHelper), true);
            return true;
         }

         if (this.close_Z && this.init_StreamUtil_2 != null && this.init_NetHelper != null) {
            this.init_V_I_Z(this.init_I_StreamUtil(this.init_StreamUtil_2, mx, this.init_NetHelper), true);
            return true;
         }

         if (this.pop_Str != null) {
            UtilCore.Inner_DataUtil metrics = this.init_UtilCore_DataUtil_Str(this.pop_Str);
            if (metrics != null) {
               this.init_V_Str_F(this.pop_Str, UtilCore.init_I_UtilCore_DataUtil(metrics, my, this.task));
            }

            return true;
         } else {
            if (this.open_Str != null) {
               this.init_V_I_I(mx, my);
               return true;
            }

            if (this.close_Str != null) {
               if (!this.read_Z && this.load_Z_I_I(mx, my)) {
                  return true;
               }

               this.read_Z = true;
               AutismConfig.getGlobal().classicMenuLayoutCustom = true;
               AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(this.close_Str);
               NetHelper current = this.sync_Map.get(this.close_Str);
               int w = current != null ? current.update() : init_I_I(150);
               int h = current != null ? current.sync() : this.put;
               layout.f_x_I = init_I_I_2(mx - this.size, 0, Math.max(0, this.stop - 4 - w));
               layout.f_y_I = init_I_I_2(my - this.data, 0, Math.max(0, this.next - 4 - h));
               return true;
            } else if (this.load_Z) {
               if (!this.read_Z && this.load_Z_I_I(mx, my)) {
                  return true;
               }

               this.read_Z = true;
               tick = this.get = mx - this.size;
               run = this.set = my - this.data;
               return true;
            } else if (this.update_Module != null && this.read_Setting != null) {
               ConfigStore.Inner_StreamUtil hit = this.init_ConfigStore_Stream_Module_Setting(this.update_Module, this.read_Setting);
               if (hit != null) {
                  this.init_BufferHelper.init_V_ConfigStore_Stream_I(hit, mx);
               }

               return true;
            } else {
               return true;
            }
         }
      }
   }

   public boolean init_Z_I(int mx, int my, double scrollY) {
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.init_Z_I(mx, my, scrollY);
         return true;
      }

      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_EventBus_I(mx, my, scrollY);
         return true;
      }

      if (this.init_CachePool != null) {
         this.init_CachePool.init_Z_I(mx, my, scrollY);
         return true;
      }

      String key;
      if (this.write_Z()) {
         key = this.load_Str_I_I(mx, my);
         if (key == null) {
            return true;
         }
      } else {
         if (this.flush_Z()) {
            return true;
         }

         if (mx < this.get + 1 + this.add) {
            key = "rail";
         } else {
            if (this.sync_Z()) {
               if (this.load_NetHelper != null && this.load_NetHelper.init_Z(mx, my)) {
                  this.init_StreamUtil.init_Z_I(mx, my, scrollY);
               }

               return true;
            }

            key = this.init_Module != null ? "settings" : (this.update_Z() ? "general" : "modules");
         }
      }

      int[] metrics = load_Map.get(key);
      if (metrics != null && metrics[1] > 0) {
         int current = Math.round(this.init_F_Str(key));
         int step = !this.write_Z() && "modules".equals(key) ? this.read_I() : this.reset + 1;
         int next = init_I_I_2(current - (int)Math.round(scrollY * step), 0, metrics[1]);
         this.init_V_Str_F(key, next);
      }

      return true;
   }

   public boolean read_Z_I(int key, int scanCode, int modifiers) {
      if (this.sync_Z || this.update_Z) {
         return true;
      }

      if (!this.sync_Z()) {
         long now = System.nanoTime();
         if (this.load_Module != null) {
            this.load_Module.setKeybind(EventBus.init(key));
            this.load_Module = null;
            this.sync_J = now;
            return true;
         }

         if (this.init_Setting != null) {
            this.init_V_Module_2(this.read_Module, this.init_Setting, Integer.toString(EventBus.init(key)));
            this.init_Setting = null;
            this.read_Module = null;
            this.sync_J = now;
            return true;
         }

         if (this.init_NetHelper_2 != null && this.init_NetHelper_2.read(key, scanCode, modifiers)) {
            return true;
         }

         if (this.init_CachePool != null) {
            if (key == 256) {
               this.flush_V();
            } else {
               this.init_CachePool.read(key, scanCode, modifiers);
            }

            return true;
         } else if (this.init_RefQueue != null) {
            if (key == 256) {
               this.push();
            }

            return true;
         } else if (this.init_StreamUtil_2 != null) {
            if (key == 256) {
               this.init_V_Z(false);
            } else {
               StreamUtil session = this.init_StreamUtil_2;
               boolean committed = key == 257 || key == 335;
               if (session.init(key, modifiers) && committed) {
                  this.init_V_Z(true);
               }
            }

            return true;
         } else if (this.load_StreamUtil == null) {
            if (key == 256) {
               if (this.init_Module != null) {
                  this.open();
               } else {
                  this.init_ConfigStore_Buffer.init_V();
               }

               return true;
            } else if (key == 70 && (modifiers & 10) != 0) {
               this.load_V_I(-1);
               if (this.load_StreamUtil != null) {
                  this.load_StreamUtil.init(0, false);
                  this.load_StreamUtil.init(this.load_StreamUtil.init_Str.length(), true);
               }

               return true;
            } else {
               return false;
            }
         } else {
            if (key == 256 && this.write_Z() && this.init_Module == null) {
               this.poll();
            } else if (key == 256) {
               if (!this.load_StreamUtil.init_Str.isEmpty()) {
                  this.load_StreamUtil.init_Str = "";
                  this.load_StreamUtil.init(0, false);
                  this.update("");
               } else {
                  this.load_StreamUtil = null;
               }
            } else if (key != 257 && key != 335) {
               this.load_StreamUtil.init(key, modifiers);
               this.update(this.load_StreamUtil.init_Str);
            } else {
               this.poll();
            }

            return true;
         }
      } else {
         if (this.init_StreamUtil.read(key, scanCode, modifiers)) {
            return true;
         }

         if (key == 256) {
            this.init_ConfigStore_Buffer.init_V();
         }

         return true;
      }
   }

   public boolean init_Z_C(char chr) {
      if (this.sync_Z || this.update_Z) {
         return true;
      }

      if (this.sync_Z()) {
         return this.init_StreamUtil.init_Z_C_I(chr, 0);
      }

      if (System.nanoTime() - this.sync_J < 100000000L) {
         return true;
      }

      if (this.init_RefQueue != null) {
         return true;
      }

      if (this.init_NetHelper_2 != null && this.init_NetHelper_2.init_Z_C(chr)) {
         return true;
      }

      if (this.init_CachePool != null) {
         this.init_CachePool.init(chr);
         return true;
      }

      if (this.init_StreamUtil_2 != null) {
         this.init_StreamUtil_2.init_V_C(chr);
         return true;
      }

      if (this.load_StreamUtil != null) {
         this.load_StreamUtil.init_V_C(chr);
         this.update(this.load_StreamUtil.init_Str);
         return true;
      }

      boolean typeToSearch = this.write_Z() ? (chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z') && !bk.init_bk().load_Z() : chr >= ' ' && chr != 127;
      if (typeToSearch && this.init_Module == null) {
         this.load_V_I(-1);
         if (this.load_StreamUtil != null) {
            this.load_StreamUtil.init_V_C(chr);
            this.update(this.load_StreamUtil.init_Str);
         }

         return true;
      } else {
         return false;
      }
   }

   void init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool type, NetHelper bounds, Module module, Setting<?, ?> option, String id, int index) {
      this.read_List.add(new ConfigStore.Inner_StreamUtil(type, bounds, module, option, id == null ? "" : id, index, this.init_Deque.peek()));
   }

   private ConfigStore.Inner_StreamUtil init_ConfigStore_Stream_I_I(int mx, int my) {
      for (int i = this.read_List.size() - 1; i >= 0; i--) {
         ConfigStore.Inner_StreamUtil hit = this.read_List.get(i);
         if (hit.init_NetHelper.init_Z(mx, my) && (hit.load == null || hit.load.init_Z(mx, my))) {
            return hit;
         }
      }

      return null;
   }

   void init_V_SyncManager_NetHelper(SyncManager context, NetHelper bounds) {
      NetHelper parent = this.init_Deque.peek();
      if (parent != null) {
         int x = Math.max(parent.read(), bounds.read());
         int y = Math.max(parent.write(), bounds.write());
         bounds = NetHelper.init(x, y, Math.min(parent.init(), bounds.init()) - x, Math.min(parent.load(), bounds.load()) - y);
      }

      this.init_Deque.push(bounds);
      context.init_GuiGraphicsExtract().enableScissor(bounds.read(), bounds.write(), bounds.init(), bounds.load());
   }

   void init_V_SyncManager(SyncManager context) {
      context.init_GuiGraphicsExtract().disableScissor();
      this.init_Deque.pop();
   }

   private ConfigStore.Inner_StreamUtil init_ConfigStore_Stream_Module_Setting(Module module, Setting<?, ?> option) {
      for (ConfigStore.Inner_StreamUtil hit : this.read_List) {
         if ((hit.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.add || hit.init_ConfigStore_CacheP == ConfigStore.Inner_CachePool.clear)
            && hit.init_Module == module
            && hit.init_Setting == option) {
            return hit;
         }
      }

      return null;
   }

   void write(String text) {
      this.init_Str = text;
   }

   boolean init_Z_Module(Module module) {
      return module != null && this.load_Module == module;
   }

   boolean init_Z_Module_Setting(Module module, Setting<?, ?> option) {
      return this.init_StreamUtil_2 != null && this.init_StreamUtil_2.init_Obj == option && option != null;
   }

   void init_V_Module(Module module, Setting<?, ?> option, int mx, NetHelper bounds) {
      this.init_V_Z(true);
      this.init_NetHelper = bounds;
      this.init_StreamUtil_2 = new StreamUtil(option, module.value(option.id()), value -> this.init_V_Module_2(module, option, value));
      if (mx >= 0) {
         this.init_StreamUtil_2.init(this.init_I_StreamUtil(this.init_StreamUtil_2, mx, bounds), false);
      }

      this.close_Z = true;
   }

   void init_V_Z(boolean commit) {
      if (this.init_StreamUtil_2 != null) {
         if (commit) {
            this.init_StreamUtil_2.init_V();
         }

         this.init_StreamUtil_2 = null;
         this.init_NetHelper = null;
         this.close_Z = false;
      }
   }

   private void init_V_I_Z(int position, boolean extend) {
      if (this.init_StreamUtil_2 != null) {
         this.init_StreamUtil_2.init(position, extend);
      }
   }

   private int init_I_StreamUtil(StreamUtil session, int mx, NetHelper bounds) {
      ToIntFunction<String> widths = value -> k.init(this.init_ConfigStore_Buffer.init_Font(), session.init_Str, value, autismclient.gui.f.a.DataUtil.init, 0);
      int scroll = session.read_I();
      return session.init_I_F(mx, bounds.read() + 4 - scroll, widths::applyAsInt);
   }

   void init_V_Module_2(Module module, Setting<?, ?> option, String value) {
      if (module != null && option != null) {
         if (this.init_ConfigStore_Buffer.init_Z()) {
            module.setConfiguredValue(option.id(), value);
         } else {
            module.setValue(option.id(), value);
         }
      }
   }

   void load_V_Module_2(Module module, Setting<?, ?> option, String value) {
      if (module != null && option != null) {
         if (this.init_ConfigStore_Buffer.init_Z()) {
            module.setConfiguredValueTransient(option.id(), value);
         } else {
            module.setValueTransient(option.id(), value);
         }
      }
   }

   ConfigStore.Inner_BufferHelper init_ConfigStore_Buffer() {
      return this.init_ConfigStore_Buffer;
   }

   void init_V_Module(Module module) {
      if (module != null) {
         if (this.init_ConfigStore_Buffer.init_Z()) {
            module.setConfiguredEnabled(!module.isEnabled());
         } else {
            module.toggle();
         }
      }
   }

   void load_V_Module(Module module) {
      if (module != null) {
         this.close();
         this.init_V_Z(true);
         this.load_StreamUtil = null;
         this.init_Module = module;
         this.sync_Z = false;
         this.load_RingBuffer.init_V_F(1.0F);
         this.update_J = System.nanoTime();
         this.flush_Z = true;
         if (this.write_Z()) {
            AutismConfig.ModuleCategoryLayout settingsLayout = this.init_AutismConfig_Modul_Str("settings-window");
            settingsLayout.f_x_I = -1;
            settingsLayout.f_y_I = -1;
            settingsLayout.collapsed = false;
            this.flush("settings-window");
         }

         this.init_BufferHelper.init_V_Module(module);
      }
   }

   private void open() {
      this.init_V_Z(true);
      this.load_Module = null;
      this.init_Setting = null;
      this.read_Module = null;
      this.push();
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2 = null;
      }

      this.flush_V();
      this.load_StreamUtil = null;
      this.open_Z = false;
      this.sync_Z = true;
      this.load_RingBuffer.init_V_F(0.0F);
      this.update_J = System.nanoTime();
      this.flush_Z = false;
   }

   boolean init_Z_Module_Str(Module module, String group) {
      return read_Map.getOrDefault(module.id() + "|" + group, false);
   }

   private void load_V_I(int mx) {
      this.load_V_I_Z(mx, false);
   }

   private void load_V_I_Z(int mx, boolean extend) {
      if (!this.write_Z() || this.init_Module == null || this.init_BufferHelper.init_Z_Module(this.init_Module)) {
         String initial = this.init_Module != null ? this.init_BufferHelper.init_Str() : this.push_Str;
         if (this.load_StreamUtil == null) {
            this.load_StreamUtil = new StreamUtil("search", initial, value -> {});
         }

         if (mx >= 0) {
            this.load_StreamUtil.init(this.init_I_StreamUtil(this.load_StreamUtil, mx, this.read_NetHelper), extend);
            this.open_Z = true;
         }

         if (this.write_Z() && this.init_Module == null) {
            AutismConfig.ModuleCategoryLayout search = this.init_AutismConfig_Modul_Str("search");
            if (search.collapsed) {
               search.collapsed = false;
               this.init_ConfigStore_Buffer.load();
            }

            this.flush("search");
         }
      }
   }

   private void poll() {
      this.update(this.load_StreamUtil == null ? "" : this.load_StreamUtil.init_Str);
      this.load_StreamUtil = null;
      this.open_Z = false;
   }

   private void update(String query) {
      if (this.init_Module != null) {
         this.init_BufferHelper.init_V_Str(query);
      } else {
         this.push_Str = query == null ? "" : query;
         this.init_V_Str_F("modules", 0.0F);
      }
   }

   private boolean init_Z_Str_Module(String query, Module module) {
      String needle = init_Str_Str(query);
      if (needle.isEmpty()) {
         return true;
      }

      if (init_Str_Str(module.name()).contains(needle)) {
         return true;
      }

      String description = module.description();
      return description != null && init_Str_Str(description).contains(needle);
   }

   private static String init_Str_Str(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
   }

   private void init(SyncManager context, String label, int x, int y, int maxWidth, int color, String query) {
      String safe = label == null ? "" : label;
      String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
      int match = needle.isEmpty() ? -1 : safe.toLowerCase(Locale.ROOT).indexOf(needle);
      if (match < 0) {
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), safe, x, y, maxWidth, color);
      } else {
         int accent = context.init_TableIndex().init_JsonUtil().clear;
         String before = safe.substring(0, match);
         String hit = safe.substring(match, match + needle.length());
         String after = safe.substring(match + needle.length());
         int used = 0;
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), before, x, y, maxWidth, color);
         used += context.init_PoolEntry().init(before);
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), hit, x + used, y, Math.max(1, maxWidth - used), accent);
         used += context.init_PoolEntry().init(hit);
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), after, x + used, y, Math.max(1, maxWidth - used), color);
      }
   }

   private void init_V_ConfigStore_Stream(ConfigStore.Inner_StreamUtil hit) {
      Module module = hit.init_Module;
      Setting<?, ?> option = hit.init_Setting;
      if (module != null && option != null) {
         this.pop();
         this.flush_V();
         this.update_Module = null;
         this.read_Setting = null;
         this.load_Z = false;
         this.write_Module = module;
         this.load_Setting = option;
         this.init_RefQueue = new RefQueue(hit.init_NetHelper, option.choices(), module.value(option.id()), value -> {
            if (this.write_Module == module && this.load_Setting == option) {
               this.init_V_Module_2(module, option, value);
            }
         });
         this.init_RefQueue.init_V_2();
      } else {
         this.push();
      }
   }

   private void push() {
      this.init_RefQueue = null;
      this.write_Module = null;
      this.load_Setting = null;
   }

   private void init_V_Module(Module module, Setting<?, ?> option, NetHelper anchor) {
      if (module != null && option != null) {
         this.push();
         this.flush_V();
         this.init_V_Z(true);
         int fallback = load_I_Str_I(option.defaultValue(), -1);
         int initial = load_I_Str_I(module.value(option.id()), fallback);
         this.init_NetHelper_2 = new autismclient.gui.f.b.NetHelper(
            anchor, initial, fallback, this.stop, this.next, argb -> this.init_V_Module_2(module, option, String.format(Locale.ROOT, "%08X", argb))
         );
      }
   }

   private void pop() {
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.init_V();
      }

      this.init_NetHelper_2 = null;
   }

   private void load_V_Module(Module module, Setting<?, ?> option, NetHelper anchor) {
      if (module != null && option != null) {
         this.push();
         this.pop();
         this.init_V_Z(true);
         this.init_CachePool = new CachePool(this, module, option, anchor);
      }
   }

   void flush_V() {
      this.init_CachePool = null;
   }

   void init_V_Module_Setting(Module module, Setting<?, ?> option) {
      this.flush_V();
      this.init_ConfigStore_Buffer.write(module, option);
   }

   private int init_I_NetHelper_I(NetHelper rail, int contentHeight) {
      int max = Math.max(0, contentHeight - rail.sync());
      int scroll = init_I_I_2(Math.round(this.init_F_Str("rail")), 0, max);
      load_Map.put("rail", new int[]{rail.sync(), max});
      return scroll;
   }

   private int load_I() {
      int rows = 2;

      for (ae category : ae.init_List()) {
         if (!init_List_ae(category).isEmpty()) {
            rows++;
         }
      }

      return 6 + rows * (this.clear + 1) + 4 - 1;
   }

   private int load_I_SyncManager(SyncManager context) {
      int widest = Math.max(context.init_PoolEntry().init("Main Menu"), context.init_PoolEntry().init("Profiles"));

      for (ae category : ae.init_List()) {
         if (!init_List_ae(category).isEmpty()) {
            widest = Math.max(widest, context.init_PoolEntry().init(category.load()));
         }
      }

      return widest + 12 + 5 + 16;
   }

   static int init_I_I(int px) {
      return Math.round(px * Math.max(1.0F, autismclient.gui.e.SyncManager.init_F()));
   }

   static NetHelper init_NetHelper_NetHelper_I(NetHelper row, int right) {
      int pillH = Math.min(11, Math.max(9, row.sync() - 3));
      int pillW = pillH * 2;
      return NetHelper.init(right - pillW, row.write() + (row.sync() - pillH) / 2, pillW, pillH);
   }

   private static int read_I_I(int width) {
      return width >= init_I_I(288) ? 2 : 1;
   }

   private int read_I() {
      int descriptionHeight = (int)Math.ceil(
         autismclient.gui.e.SyncManager.init(
            autismclient.gui.e.ConfigStore.init_ConfigStore, autismclient.gui.e.SyncManager.init_F_ConfigStore(autismclient.gui.e.ConfigStore.init_ConfigStore)
         )
      );
      return 1 + this.reset + Math.max(RenderState.init(12), descriptionHeight) + 4 + 4;
   }

   private int init_I_I_I(int count, int columns) {
      return (count + columns - 1) / columns * this.read_I() + 4;
   }

   private int init_I_Str(String key, NetHelper viewport, int contentHeight, float frameSeconds) {
      int max = Math.max(0, contentHeight - viewport.sync());
      this.init_V_Str(key, viewport.sync(), max);
      float target = init_F_F(this.init_F_Str(key), 0.0F, max);
      return Math.round(this.init_F_Str(key, target, frameSeconds));
   }

   void init_V_Str(String key, int viewHeight, int maxScroll) {
      load_Map.put(key, new int[]{viewHeight, maxScroll});
   }

   float init_F_Str(String key, float target, float frameSeconds) {
      Float render = this.write_Map.get(key);
      if (render == null) {
         render = target;
      }

      float speed = 1.0F - (float)Math.exp(-frameSeconds * 18.0F);
      render = render + (target - render) * speed;
      if (Math.abs(render - target) < 0.4F) {
         render = target;
      }

      this.write_Map.put(key, render);
      return render;
   }

   private void init_V_SyncManager(SyncManager context, String key, NetHelper viewport, int contentHeight, int scroll, float frameSeconds) {
      this.init_V_Str(key, context, viewport, contentHeight, scroll);
   }

   void init_V_Str(String key, SyncManager context, NetHelper viewport, int contentHeight, int scroll) {
      int[] metrics = load_Map.get(key);
      if (metrics != null && metrics[1] > 0) {
         NetHelper track = NetHelper.init(viewport.init() - 4, viewport.write() + 2, 3, viewport.sync() - 4);
         UtilCore.Inner_DataUtil m = UtilCore.init(track, contentHeight, viewport.sync(), scroll);
         this.init_V_Str_UtilCore_DataUtil(key, m);
         boolean hovered = m.init(context.read(), context.write());
         UtilCore.init(context, m, hovered, key.equals(this.pop_Str));
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.value, track, null, null, key, -1);
      }
   }

   int init_I() {
      return this.node;
   }

   private void init_V_Str_UtilCore_DataUtil(String key, UtilCore.Inner_DataUtil metrics) {
      this.flush_Map.put(key, metrics);
   }

   private UtilCore.Inner_DataUtil init_UtilCore_DataUtil_Str(String key) {
      return this.flush_Map.get(key);
   }

   float init_F_Str(String key) {
      return init_Map.getOrDefault(key, 0.0F);
   }

   void init_V_Str_F(String key, float value) {
      init_Map.put(key, value);
      this.write_Map.put(key, value);
   }

   private boolean write_Z() {
      return !"CSGO".equals(AutismConfig.getGlobal().moduleMenuStyle);
   }

   private void init_V_SyncManager_2(SyncManager base, float frameSeconds, long now) {
      String hoverOwner = this.init_Str_I_I(base.read(), base.write());
      this.sync_Map.clear();
      this.queue = this.init_I_SyncManager(base);
      this.tick();
      int rise = 0;
      if (this.init_Module != null) {
         PacketUtil.init(base.init_GuiGraphicsExtract(), NetHelper.init(0, 0, this.stop, this.next), base.init_TableIndex().init_JsonUtil().init);
         this.read_V_SyncManager(base, frameSeconds, rise);
      } else {
         SyncManager blocked = null;

         for (String id : this.load_List()) {
            SyncManager context = base;
            if (hoverOwner != null && !hoverOwner.equals(id)) {
               if (blocked == null) {
                  blocked = UiHelper.init(base.init_GuiGraphicsExtract(), this.init_ConfigStore_Buffer.init_Font(), -536870912, -536870912);
               }

               context = blocked;
            }

            if ("main-menu".equals(id)) {
               this.load_V_SyncManager(context, frameSeconds, rise);
            } else if ("search".equals(id)) {
               this.init_V_SyncManager(context, frameSeconds, rise);
            } else if ("settings-window".equals(id)) {
               if (this.init_Module != null) {
                  this.read_V_SyncManager(context, frameSeconds, rise);
               }
            } else {
               ae category = init_ae_Str(id);
               if (category != null) {
                  this.init_V_SyncManager_6(context, category, frameSeconds, rise);
               }
            }
         }
      }
   }

   private void init_V_SyncManager_6(SyncManager context, ae category, float frameSeconds, int rise) {
      List<Module> modules = init_List_ae(category);
      if (!modules.isEmpty()) {
         AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(category.init_Str());
         int rowStep = this.reset + 1;
         int contentH = modules.size() * rowStep + 3;
         int maxRows = layout.visibleRows > 0 ? layout.visibleRows : 14;
         int naturalBody = Math.min(contentH, maxRows * rowStep + 3);
         boolean automatic = !AutismConfig.getGlobal().classicMenuLayoutCustom;
         if (automatic) {
            naturalBody = Math.min(naturalBody, this.entry);
         }

         int bodyH = this.init_I_Str(category.init_Str(), naturalBody, layout.collapsed);
         NetHelper bounds = this.init_NetHelper_Str(category.init_Str(), layout, automatic && this.buffer > 0 ? this.buffer : init_I_I(150), bodyH, rise);
         this.init(context, category.init_Str(), bounds, category.load(), layout.collapsed, true, false);
         if (bodyH > 0) {
            NetHelper viewport = this.read(bounds, bodyH);
            NetHelper target = this.load_NetHelper_NetHelper_I(viewport, naturalBody);
            String key = "classic:" + category.init_Str();
            int scroll = this.init_I_Str(key, target, contentH, frameSeconds);
            boolean scrollbar = contentH > target.sync();
            int y = viewport.write() + 2 - scroll;
            this.init_V_SyncManager_NetHelper(context, viewport);

            try {
               for (Module module : modules) {
                  NetHelper row = NetHelper.init(viewport.read() + 2, y, viewport.update() - 4 - (scrollbar ? 6 : 0), this.reset);
                  if (row.load() >= viewport.write() && row.write() <= viewport.load()) {
                     this.init_V_SyncManager_5(context, module, row, false);
                  }

                  y += rowStep;
               }

               this.init_V_Str(key, context, target, contentH, scroll);
            } finally {
               this.init_V_SyncManager(context);
            }

            if (!layout.collapsed && modules.size() > 2 && bodyH >= target.sync()) {
               this.init_V_SyncManager_7(context, category.init_Str(), bounds, init_I_I_2((target.sync() - 3) / rowStep, 1, modules.size()));
            }
         }
      }
   }

   private void init_V_SyncManager_7(SyncManager context, String id, NetHelper bounds, int shownRows) {
      NetHelper grip = NetHelper.init(bounds.read() + 6, bounds.load() - 4, Math.max(1, bounds.update() - 12), 4);
      boolean active = id.equals(this.open_Str);
      boolean hover = !active && grip.init_Z(context.read(), context.write());
      JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      int color = active ? colors.clear : (hover ? colors.prev : colors.valid & 16777215 | 1929379840);
      int pillW = Math.min(20, Math.max(8, bounds.update() / 5));
      PacketUtil.load(context.init_GuiGraphicsExtract(), NetHelper.init(bounds.read() + (bounds.update() - pillW) / 2, bounds.load() - 2, pillW, 3), 1, color);
      if (hover || active) {
         context.init_GuiGraphicsExtract().requestCursor(CursorTypes.RESIZE_NS);
      }

      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.cache, grip, null, null, id, shownRows);
   }

   private void init_V_ConfigStore_Stream(ConfigStore.Inner_StreamUtil hit, int mx, int my) {
      String id = hit.init_Str;
      this.flush(id);
      long now = System.currentTimeMillis();
      if (id.equals(this.poll_Str) && now - this.load_J <= 350L) {
         this.poll_Str = null;
         this.sync(id);
      } else {
         this.poll_Str = id;
         this.load_J = now;
         this.open_Str = id;
         this.value = mx;
         this.cache = my;
         this.state = hit.init_I;
      }
   }

   private void init_V_I_I(int mx, int my) {
      ae category = init_ae_Str(this.open_Str);
      if (category != null) {
         this.init_V_ae_I(category, this.state + Math.round((float)(my - this.cache) / (this.reset + 1)));
      }
   }

   private void init_V_ae_I(ae category, int rows) {
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(category.init_Str());
      int rowStep = this.reset + 1;
      int fitRows = (this.next - 4 - Math.max(0, layout.f_y_I) - this.put - 1 - 3) / rowStep;
      rows = init_I_I_2(rows, 2, Math.max(2, Math.min(init_List_ae(category).size(), fitRows)));
      if (rows != layout.visibleRows) {
         AutismConfig.getGlobal().classicMenuLayoutCustom = true;
         layout.visibleRows = rows;
      }
   }

   public void init_V_I(int rows) {
      AutismConfig config = AutismConfig.getGlobal();
      if (rows < 0) {
         if (this.close_Map != null) {
            this.close_Map.forEach((id, saved) -> this.init_AutismConfig_Modul_Str(id).visibleRows = saved);
            config.classicMenuLayoutCustom = this.poll_Z;
            this.close_Map = null;
         }
      } else {
         if (this.close_Map == null) {
            this.close_Map = new LinkedHashMap<>();
            this.poll_Z = config.classicMenuLayoutCustom;

            for (ae category : ae.init_List()) {
               this.close_Map.put(category.init_Str(), this.init_AutismConfig_Modul_Str(category.init_Str()).visibleRows);
            }
         }

         for (ae category : ae.init_List()) {
            if (!init_List_ae(category).isEmpty()) {
               this.init_V_ae_I(category, rows);
            }
         }
      }
   }

   private void sync(String id) {
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(id);
      layout.visibleRows = 0;
      this.init_ConfigStore_Buffer.load();
   }

   private NetHelper load_NetHelper_NetHelper_I(NetHelper viewport, int targetBody) {
      int capped = Math.max(0, Math.min(targetBody, this.next - 4 - this.put - 1));
      return NetHelper.init(viewport.read(), viewport.write(), viewport.update(), capped);
   }

   private void init_V_SyncManager(SyncManager context, float frameSeconds, int rise) {
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str("search");
      boolean automatic = !AutismConfig.getGlobal().classicMenuLayoutCustom;
      int w = automatic && this.buffer > 0 ? this.buffer : init_I_I(150);
      String query = this.load_StreamUtil != null ? this.load_StreamUtil.init_Str : this.push_Str;
      boolean hasQuery = !query.isBlank();
      List<Module> results = hasQuery ? this.init_List_Str(query) : List.of();
      int rowStep = this.reset + 1;
      int fieldH = this.reset;
      int strip = fieldH + 6;
      int listH = hasQuery ? Math.max(1, results.size()) * rowStep + 3 : 0;
      int contentH = strip + listH;
      int maxBody = automatic && this.entry > 0
         ? Math.max(strip + rowStep, this.entry)
         : Math.max(strip + rowStep, this.next - 4 - Math.max(0, layout.f_y_I) - this.put - 1);
      int naturalBody = Math.min(contentH, maxBody);
      int bodyH = this.init_I_Str("search", naturalBody, layout.collapsed);
      NetHelper bounds = this.init_NetHelper_Str("search", layout, w, bodyH, rise);
      this.init(context, "search", bounds, "Search", layout.collapsed, true, false);
      if (bodyH > 0) {
         NetHelper viewport = this.read(bounds, bodyH);
         NetHelper target = this.load_NetHelper_NetHelper_I(viewport, naturalBody);
         this.init_V_SyncManager_NetHelper(context, viewport);

         try {
            this.read_NetHelper = NetHelper.init(viewport.read() + 3, viewport.write() + 3, viewport.update() - 6, fieldH);
            this.init_V_SyncManager_Str(context, "Search modules...");
            if (hasQuery) {
               NetHelper list = NetHelper.init(target.read(), target.write() + strip, target.update(), Math.max(0, target.sync() - strip));
               String key = "classic:search";
               int scroll = this.init_I_Str(key, list, listH, frameSeconds);
               boolean scrollbar = listH > list.sync();
               this.init_V_SyncManager_NetHelper(context, list);

               try {
                  int y = list.write() + 2 - scroll;
                  if (results.isEmpty()) {
                     NetHelper row = NetHelper.init(list.read() + 2, y, list.update() - 4, this.reset);
                     context.init_PoolEntry()
                        .load(
                           context.init_GuiGraphicsExtract(),
                           "No matches",
                           row.read() + 6,
                           context.init_PoolEntry().init_I(row),
                           Math.max(1, row.update() - 12),
                           context.init_TableIndex().init_JsonUtil().valid
                        );
                  }

                  for (Module module : results) {
                     NetHelper row = NetHelper.init(list.read() + 2, y, list.update() - 4 - (scrollbar ? 6 : 0), this.reset);
                     if (row.load() >= list.write() && row.write() <= list.load()) {
                        this.init_V_SyncManager_5(context, module, row, true);
                     }

                     y += rowStep;
                  }

                  this.init_V_Str(key, context, list, listH, scroll);
               } finally {
                  this.init_V_SyncManager(context);
               }
            }
         } finally {
            this.init_V_SyncManager(context);
         }
      }
   }

   private void init_V_SyncManager_5(SyncManager context, Module module, NetHelper row, boolean highlightQuery) {
      TableIndex theme = context.init_TableIndex();
      boolean hovered = row.init_Z(context.read(), context.write());
      float hoverT = autismclient.gui.f.DataUtil.init("cmod:" + module.id(), hovered);
      float onT = autismclient.gui.f.BufferHelper.init("modon:" + module.id(), module.isEnabled());
      PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, init_I_I(theme.init_JsonUtil().open, theme.init_JsonUtil().push, hoverT));
      if (onT > 0.001F) {
         int accent = theme.init_JsonUtil().clear;
         PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, PacketUtil.init(accent, (0.16F + 0.06F * hoverT) * onT));
         PacketUtil.load(context.init_GuiGraphicsExtract(), row, 4, 0, PacketUtil.init(accent, 0.85F * onT));
      }

      String label = this.load_Module == module ? "Press key" : module.name();
      int iconSize = Math.min(13, row.sync() - 2);
      int bindH = Math.max(13, row.sync() - 4);
      int tail = row.init() - 2;
      NetHelper settings = NetHelper.init(tail - iconSize, row.write() + (row.sync() - iconSize) / 2, iconSize, iconSize);
      tail = settings.read() - 2;
      NetHelper bindButton = NetHelper.init(tail - 34, row.write() + (row.sync() - bindH) / 2, 34, bindH);
      boolean showBind = row.update() >= init_I_I(110);
      int labelMax = Math.max(1, (showBind ? bindButton.read() : tail) - row.read() - 9);
      int nameColor = module.isEnabled() ? theme.init_JsonUtil().prev : theme.init_JsonUtil().valid;
      if (highlightQuery && !this.push_Str.isBlank()) {
         this.init(context, label, row.read() + 6, context.init_PoolEntry().init_I(row), labelMax, nameColor, this.push_Str.trim());
      } else {
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), label, row.read() + 6, context.init_PoolEntry().init_I(row), labelMax, nameColor);
      }

      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.flush, row, module, null, "", -1);
      if (showBind) {
         EventBus.init(context, bindButton, module.keybind(), this.load_Module == module, bindButton.init_Z(context.read(), context.write()));
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.close, bindButton, module, null, "", -1);
      }

      if (settings != null) {
         autismclient.gui.f.b.RingBuffer.init(
            context.init_GuiGraphicsExtract(),
            this.init_ConfigStore_Buffer.init_Font(),
            settings,
            cz.sync,
            settings.init_Z(context.read(), context.write()),
            true,
            false
         );
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.open, settings, module, null, "", -1);
      }

      if (hovered) {
         String description = module.description();
         this.init_Str = showBind ? description : module.name() + (description != null && !description.isBlank() ? "\n" + description : "");
      }
   }

   private void load_V_SyncManager(SyncManager context, float frameSeconds, int rise) {
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str("main-menu");
      this.queue = this.init_I_SyncManager(context);
      int w = this.queue;
      int contentH = this.init_I_SyncManager(null, 0, 0, w - 2);
      int maxBody = Math.max(40, this.next - 8 - this.put);
      int bodyH = this.init_I_Str("main-menu", Math.min(contentH, maxBody), layout.collapsed);
      NetHelper bounds = this.init_NetHelper_Str("main-menu", layout, w, bodyH, rise);
      this.init(context, "main-menu", bounds, "Main Menu", layout.collapsed, true, false);
      if (bodyH > 0) {
         NetHelper viewport = this.read(bounds, bodyH);
         NetHelper target = this.load_NetHelper_NetHelper_I(viewport, Math.min(contentH, maxBody));
         String scrollKey = "classic:main-menu";
         int scroll = this.init_I_Str(scrollKey, target, contentH, frameSeconds);
         int gutter = contentH > target.sync() ? 6 : 0;
         this.init_V_SyncManager_NetHelper(context, viewport);

         try {
            this.init_I_SyncManager(context, viewport.read(), viewport.write() - scroll, viewport.update() - gutter);
            this.init_V_Str(scrollKey, context, target, contentH, scroll);
         } finally {
            this.init_V_SyncManager(context);
         }
      }
   }

   private void read_V_SyncManager(SyncManager context, float frameSeconds, int rise) {
      Module module = this.init_Module;
      AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str("settings-window");
      boolean searchable = this.init_BufferHelper.init_Z_Module(module);
      int searchStrip = searchable ? RenderState.init(13) + 8 : 0;
      int maxW = Math.max(160, this.stop - 8);
      int w = Math.min(init_I_I((searchable ? 360 : 250) + (module instanceof n ? 140 : 0)), maxW);
      int maxBody = Math.max(60, this.next - 12 - this.put);
      int expandedBodyH = Math.min(this.init_BufferHelper.init_I_Module(module) + searchStrip, maxBody);
      int bodyH = Math.round(this.init_I_Str("settings-window", expandedBodyH, layout.collapsed) * this.load_RingBuffer.init_F());
      if (layout.f_x_I < 0 || layout.f_y_I < 0) {
         layout.f_x_I = Math.max(4, (this.stop - w) / 2);
         layout.f_y_I = Math.max(6, (this.next - (this.put + expandedBodyH + 1)) / 2);
      }

      NetHelper bounds = this.init_NetHelper_Str("settings-window", layout, w, bodyH, rise);
      this.init(context, "settings-window", bounds, module.name(), layout.collapsed, true, true);
      if (bodyH > 0) {
         NetHelper viewport = this.read(bounds, bodyH);
         this.init_V_SyncManager_NetHelper(context, viewport);

         try {
            if (searchable) {
               this.read_NetHelper = NetHelper.init(viewport.read() + 4, viewport.write() + 4, viewport.update() - 8, RenderState.init(13));
               this.load_V_SyncManager(context);
            }

            NetHelper fullContent = NetHelper.init(
               viewport.read(),
               viewport.write() + searchStrip,
               viewport.update(),
               Math.max(1, Math.min(expandedBodyH, Math.max(1, this.next - this.put - 5)) - searchStrip)
            );
            this.init_BufferHelper.init_V_SyncManager_2(context, fullContent, module, frameSeconds);
         } finally {
            this.init_V_SyncManager(context);
         }
      }
   }

   private void init(SyncManager context, String id, NetHelper bounds, String title, boolean collapsed, boolean collapse, boolean close) {
      int header = this.put;
      boolean headerHovered = ClassCache.read(bounds, header, context.read(), context.write());
      FieldCache.init(context, bounds, title, collapsed, collapse, close, headerHovered, true, 4, 4, header);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.load, bounds, null, null, id, -1);
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.init, ClassCache.init(bounds, header), null, null, id, -1);
      if (collapse) {
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.read, ClassCache.read(bounds, header), null, null, id, -1);
      }

      if (close) {
         this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.write, ClassCache.load(bounds, header), null, null, id, -1);
      }
   }

   private NetHelper init_NetHelper_Str(String id, AutismConfig.ModuleCategoryLayout layout, int w, int bodyH, int rise) {
      int h = this.put + bodyH + (bodyH > 0 ? 1 : 0);
      NetHelper bounds = NetHelper.init(Math.max(0, layout.f_x_I), Math.max(0, layout.f_y_I), w, h).init_NetHelper(this.stop - 4, this.next - 4);
      NetHelper drawn = NetHelper.init(bounds.read(), bounds.write() + rise, bounds.update(), bounds.sync());
      this.sync_Map.put(id, drawn);
      return drawn;
   }

   private int init_I_Str(String id, int expandedHeight, boolean collapsed) {
      RingBuffer transition = this.update_Map.computeIfAbsent(id, key -> new RingBuffer(collapsed ? 0.0F : 1.0F, 180));
      return Math.round(Math.max(0, expandedHeight) * transition.init_F_F(!collapsed && !this.update_Z ? 1.0F : 0.0F));
   }

   private NetHelper read(NetHelper bounds, int bodyH) {
      return NetHelper.init(bounds.read() + 1, bounds.write() + this.put, bounds.update() - 2, Math.min(bodyH, Math.max(0, bounds.sync() - this.put - 1)));
   }

   private void load_V_SyncManager(SyncManager context) {
      this.init_V_SyncManager_Str(context, "Search");
   }

   private void init_V_SyncManager_Str(SyncManager context, String placeholder) {
      boolean searchFocused = this.load_StreamUtil != null;
      String shown = searchFocused ? this.load_StreamUtil.init_Str : (this.init_Module != null ? this.init_BufferHelper.init_Str() : this.push_Str);
      e.init(
         context,
         this.read_NetHelper,
         shown,
         placeholder,
         searchFocused,
         searchFocused ? this.load_StreamUtil.init_I : shown.length(),
         searchFocused ? this.load_StreamUtil.init_I() : -1,
         searchFocused ? this.load_StreamUtil.load_I() : -1,
         searchFocused
            ? this.load_StreamUtil.init(Math.max(1, this.read_NetHelper.update() - 8), fragment -> context.init_PoolEntry().init(shown, fragment))
            : 0
      );
      this.init_V_ConfigStore_CacheP(ConfigStore.Inner_CachePool.reset, this.read_NetHelper, null, null, "search", -1);
   }

   private void tick() {
      AutismConfig config = AutismConfig.getGlobal();
      if (!config.classicMenuLayoutCustom) {
         this.run();
      } else {
         boolean broken = false;
         Set<Long> taken = new HashSet<>();
         List<String> ids = new ArrayList<>();
         ids.add("main-menu");

         for (ae category : ae.init_List()) {
            if (!init_List_ae(category).isEmpty()) {
               ids.add(category.init_Str());
            }
         }

         for (String id : ids) {
            AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(id);
            if (layout.f_x_I < 0 || layout.f_y_I < 0) {
               broken = true;
            }

            if (!taken.add((long)layout.f_x_I << 32 | layout.f_y_I & 4294967295L)) {
               broken = true;
            }
         }

         if (broken) {
            config.classicMenuLayoutCustom = false;
            this.run();
            this.init_ConfigStore_Buffer.load();
         } else {
            AutismConfig.ModuleCategoryLayout search = this.init_AutismConfig_Modul_Str("search");
            if (search.f_x_I < 0 || search.f_y_I < 0) {
               AutismConfig.ModuleCategoryLayout utility = this.init_AutismConfig_Modul_Str("main-menu");
               search.f_x_I = Math.max(4, utility.f_x_I);
               search.f_y_I = Math.min(
                  Math.max(4, utility.f_y_I) + this.put + this.init_I_SyncManager(null, 0, 0, this.queue - 2) + 1 + 4, Math.max(4, this.next - this.put - 40)
               );
               this.init_ConfigStore_Buffer.load();
            }
         }
      }

      this.get();
   }

   private void run() {
      int gap = 4;
      int rowStep = this.reset + 1;
      List<String> ids = new ArrayList<>();

      for (ae category : ae.init_List()) {
         if (!init_List_ae(category).isEmpty()) {
            ids.add(category.init_Str());
         }
      }

      ids.add("search");
      int utilityW = Math.max(1, this.queue);
      int categoryW = init_I_I(150);
      AutismConfig.ModuleCategoryLayout utility = this.init_AutismConfig_Modul_Str("main-menu");
      utility.f_x_I = gap;
      utility.f_y_I = gap;
      utility.visibleRows = 0;
      int left = gap + utilityW + gap;
      int[] grid = init_IArr_I(Math.max(categoryW + gap * 2, this.stop - left + gap), this.next, ids.size(), categoryW, this.put, rowStep);
      int columns = grid[0];
      int slotH = grid[2];
      this.buffer = grid[1];
      this.entry = Math.max(0, slotH - this.put - 1);

      for (int index = 0; index < ids.size(); index++) {
         AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(ids.get(index));
         layout.f_x_I = left + index % columns * (this.buffer + gap);
         layout.f_y_I = gap + index / columns * (slotH + gap);
         layout.visibleRows = Math.max(1, (this.entry - 3) / rowStep);
      }
   }

   static int[] init_IArr_I(int width, int height, int count, int desiredWidth, int header, int rowStep) {
      int gap = 4;
      count = Math.max(1, count);
      int maxRows = Math.max(1, (height - gap) / Math.max(1, header + rowStep + 8));
      int columns = Math.min(count, Math.max(Math.max(1, (width - gap) / Math.max(1, desiredWidth + gap)), (count + maxRows - 1) / maxRows));
      int rows = (count + columns - 1) / columns;
      return new int[]{columns, Math.max(1, Math.min(desiredWidth, (width - gap * (columns + 1)) / columns)), Math.max(1, (height - gap * (rows + 1)) / rows)};
   }

   private AutismConfig.ModuleCategoryLayout init_AutismConfig_Modul_Str(String id) {
      return AutismConfig.getGlobal().moduleCategoryLayouts.computeIfAbsent(id, ignored -> new AutismConfig.ModuleCategoryLayout());
   }

   private List<String> load_List() {
      this.get();
      return List.copyOf(init_List);
   }

   private void get() {
      List<String> valid = new ArrayList<>();
      valid.add("main-menu");

      for (ae category : ae.init_List()) {
         valid.add(category.init_Str());
      }

      valid.add("search");
      valid.add("settings-window");
      init_List.removeIf(idx -> !valid.contains(idx));

      for (String id : valid) {
         if (!init_List.contains(id)) {
            init_List.add(id);
         }
      }
   }

   private void flush(String id) {
      if (id != null && !id.isBlank()) {
         this.get();
         init_List.remove(id);
         init_List.add(id);
      }
   }

   private String init_Str_I_I(int mx, int my) {
      List<String> order = this.load_List();

      for (int i = order.size() - 1; i >= 0; i--) {
         NetHelper bounds = this.sync_Map.get(order.get(i));
         if (bounds != null && bounds.init_Z(mx, my)) {
            return order.get(i);
         }
      }

      return null;
   }

   private String load_Str_I_I(int mx, int my) {
      String id = this.init_Str_I_I(mx, my);
      if (id == null) {
         return null;
      } else if ("main-menu".equals(id)) {
         return "classic:main-menu";
      } else {
         return "settings-window".equals(id) ? "settings" : "classic:" + id;
      }
   }

   private void init_V_Str(String id, int mx, int my, int button) {
      this.flush(id);
      if (button == 1) {
         this.close(id);
      } else if (button == 0) {
         AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(id);
         this.close_Str = id;
         this.prev = mx;
         this.valid = my;
         this.read_Z = false;
         NetHelper drawn = this.sync_Map.get(id);
         this.size = mx - (drawn == null ? layout.f_x_I : drawn.read());
         this.data = my - (drawn == null ? layout.f_y_I : drawn.write());
      }
   }

   private boolean load_Z_I_I(int mx, int my) {
      return Math.abs(mx - this.prev) <= 3 && Math.abs(my - this.valid) <= 3;
   }

   private void close(String id) {
      if (id != null) {
         AutismConfig.ModuleCategoryLayout layout = this.init_AutismConfig_Modul_Str(id);
         layout.collapsed = !layout.collapsed;
         this.init_ConfigStore_Buffer.load();
      }
   }

   private static ae init_ae_Str(String id) {
      for (ae category : ae.init_List()) {
         if (category.init_Str().equals(id)) {
            return category;
         }
      }

      return null;
   }

   private List<Module> init_List_Str(String query) {
      String needle = load_Str_Str(query);
      if (needle.isEmpty()) {
         return List.of();
      }

      List<Module> out = new ArrayList<>();

      for (ae category : ae.init_List()) {
         for (Module module : init_List_ae(category)) {
            String name = load_Str_Str(module.name());
            if (name.contains(needle) || load_Str_Str(module.id()).contains(needle) || init_Z_Str_Str(needle, name)) {
               out.add(module);
            }
         }
      }

      return out;
   }

   private static String load_Str_Str(String value) {
      if (value == null) {
         return "";
      }

      StringBuilder out = new StringBuilder(value.length());

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c != ' ' && c != '-' && c != '_' && c != '.') {
            out.append(Character.toLowerCase(c));
         }
      }

      return out.toString();
   }

   private static boolean init_Z_Str_Str(String needle, String haystack) {
      if (needle.isEmpty()) {
         return false;
      }

      int i = 0;

      for (int j = 0; j < haystack.length() && i < needle.length(); j++) {
         if (haystack.charAt(j) == needle.charAt(i)) {
            i++;
         }
      }

      return i == needle.length();
   }

   private boolean update_Z() {
      return "\u0000general".equals(flush_Str) && this.push_Str.isBlank();
   }

   private boolean sync_Z() {
      return this.init_StreamUtil != null && !this.write_Z() && !this.write_Z;
   }

   private boolean flush_Z() {
      return this.sync_Z() && this.init_StreamUtil.init_Z();
   }

   public void close() {
      if (this.init_StreamUtil != null) {
         this.init_StreamUtil.init_V();
         this.init_StreamUtil = null;
         this.load_NetHelper = null;
      }
   }

   private ae init_ae() {
      if (flush_Str == null) {
         return null;
      }

      for (ae category : ae.init_List()) {
         if (category.init_Str().equals(flush_Str)) {
            return category;
         }
      }

      return null;
   }

   private void open(String key) {
      if (key != null && (!key.equals(flush_Str) || this.init_Module != null || !this.push_Str.isBlank())) {
         this.close();
         this.load_StreamUtil = null;
         this.push_Str = "";
         this.init_V_Str_F("modules", 0.0F);
         flush_Str = key;
         this.init_Module = null;
         this.sync_Z = false;
         this.load_RingBuffer.init_V_F(0.0F);
         this.update_J = System.nanoTime();
         this.flush_Z = false;
      }
   }

   private static List<Module> init_List_ae(ae category) {
      List<Module> out = new ArrayList<>();

      for (Module module : al.init_List_ae(category)) {
         if (module.showInModuleMenu()) {
            out.add(module);
         }
      }

      return out;
   }

   private boolean init_Z_I(int button) {
      if (this.load_Module != null) {
         this.load_Module.setKeybind(ValueSink.init_I(button));
         this.load_Module = null;
         this.sync_J = System.nanoTime();
         return true;
      } else if (this.init_Setting != null) {
         this.init_V_Module_2(this.read_Module, this.init_Setting, Integer.toString(ValueSink.init_I(button)));
         this.init_Setting = null;
         this.read_Module = null;
         this.sync_J = System.nanoTime();
         return true;
      } else {
         return false;
      }
   }

   private static GuiGraphicsExtractor init_GuiGraphicsExtract_SyncManager(SyncManager context) {
      return context.init_GuiGraphicsExtract();
   }

   static int init_I_I(int from, int to, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int aa = from >>> 24 & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      int ba = to >>> 24 & 0xFF;
      int r = Math.round(ar + (br - ar) * t);
      int g = Math.round(ag + (bg - ag) * t);
      int b = Math.round(ab + (bb - ab) * t);
      int a = Math.round(aa + (ba - aa) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   static int init_I_I_F(int color, float alphaScale) {
      int a = Math.round((color >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alphaScale)));
      return a << 24 | color & 16777215;
   }

   static int init_I_Str_I(String value, int fallback) {
      try {
         return Integer.parseInt(value.trim());
      } catch (Throwable t) {
         return fallback;
      }
   }

   static double init_D_Str_D(String value, double fallback) {
      try {
         return Double.parseDouble(value.trim());
      } catch (Throwable t) {
         return fallback;
      }
   }

   static int load_I_Str_I(String value, int fallback) {
      if (value == null) {
         return fallback;
      }

      String v = value.trim();
      if (v.startsWith("#")) {
         v = v.substring(1);
      }

      if (!v.isEmpty() && v.length() <= 8) {
         try {
            long parsed = Long.parseLong(v, 16);
            if (v.length() <= 6) {
               parsed |= 4278190080L;
            }

            return (int)(parsed & 4294967295L);
         } catch (Throwable t) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   static int init_I_I_2(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   static float init_F_F(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   public interface Inner_BufferHelper {
      boolean init_Z();

      Screen init_Screen();

      Font init_Font();

      void init_V();

      void load();

      void init(Module var1, Setting<?, ?> var2);

      void load(Module var1, Setting<?, ?> var2);

      void read(Module var1, Setting<?, ?> var2);

      void write(Module var1, Setting<?, ?> var2);

      void init(Module var1, Setting<?, ?> var2, aM var3);

      void init(String var1);

      void init(Module var1);
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run,
      get,
      set,
      put,
      add,
      clear,
      reset,
      start,
      stop,
      next,
      prev,
      valid,
      size,
      data,
      value,
      cache;
   }

   private record Inner_ConfigStore(String init_Str, String load, Identifier init_Identifier) {
   }

   private record Inner_DataUtil(String init_Str, String load, autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil) {
   }

   record Inner_StreamUtil(
      ConfigStore.Inner_CachePool init_ConfigStore_CacheP,
      NetHelper init_NetHelper,
      Module init_Module,
      Setting<?, ?> init_Setting,
      String init_Str,
      int init_I,
      NetHelper load
   ) {
   }
}
