package autismclient.gui.a;

import autismclient.gui.f.HashUtil;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.BitSetUtil;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.PoolEntry;
import autismclient.gui.f.b.g;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.gui.f.c.RingBuffer;
import autismclient.gui.f.c.SyncManager;
import autismclient.modules.Module;
import autismclient.modules.ae;
import autismclient.modules.al;
import autismclient.util.AutismFilterViewOverlay;
import autismclient.util.AutismGuiViewOverlay;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.AutismModuleViewOverlay;
import autismclient.util.AutismPacketInspectOverlay;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.AutismSharePickerOverlay;
import autismclient.util.L;
import autismclient.util.aJ;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.bi;
import autismclient.util.bk;
import autismclient.util.cA;
import autismclient.util.cX;
import autismclient.util.cY;
import autismclient.util.ck;
import autismclient.util.ct;
import autismclient.util.o;
import autismclient.util.mm.ByteReader;
import autismclient.util.mm.EventBus;
import autismclient.util.mm.RenderState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class DataUtil {
   private static final int init_I = -200405488;
   private static final int load_I = -938208233;
   private static final int read_I = 705827348;
   private static final int write_I = 403771667;
   private static final int update_I = -14013906;
   private static final int sync_I = -14249;
   private static final int flush = -7429889;
   private static final int close = -13425624;
   private static final int open = -8759968;
   private static final int poll = -13248397;
   private static final int push = -855310;
   private static final int pop = -6645094;
   private static final int tick = -42149;
   private int run = -13425624;
   private int get = -8759968;
   private int set = -13248397;
   private int put = -855310;
   private int add = -6645094;
   private int clear = -42149;
   private final DataUtil.BufferHelper init_DataUtil_BufferHel;
   private final Font init_Font;
   private final CharEncoder init_CharEncoder = new CharEncoder();
   private final autismclient.util.mm.BufferHelper init_BufferHelper = autismclient.util.mm.BufferHelper.init_BufferHelper();
   private final EventBus init_EventBus = EventBus.init_EventBus();
   private final List<DataUtil.ConfigStore> init_List = new ArrayList<>();
   private DataUtil.JsonUtil init_DataUtil_JsonUtil;
   private DataUtil.TaskQueue init_DataUtil_TaskQueue;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private final int[] init_IArr;
   private final int[] load_IArr;
   private int reset;
   private int start;
   private int stop;
   private static final int next = 31;
   private int prev;
   private int valid;
   private int size;
   private PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil;
   private boolean write_Z;
   private int data;
   private ItemStack init_ItemStack;
   private int value;
   private int cache;
   private int state;
   private int queue;
   private int buffer;
   private int entry;
   private final BitSetUtil init_BitSetUtil;
   private final BitSetUtil load_BitSetUtil;
   private final BitSetUtil read_BitSetUtil;
   private final BitSetUtil write_BitSetUtil;
   private final BitSetUtil update_BitSetUtil;
   private final BitSetUtil sync_BitSetUtil;
   private final List<BitSetUtil> load_List;
   private AutismPacketInspectOverlay init_AutismPacketInspec;
   private AutismGuiViewOverlay init_AutismGuiViewOverl;
   private AutismFilterViewOverlay init_AutismFilterViewOv;
   private AutismModuleViewOverlay init_AutismModuleViewOv;
   private AutismSharePickerOverlay init_AutismSharePickerO;
   private static final int node = 64;
   private final Map<String, ItemStack> init_Map;
   private ct.Inner_StreamUtil init_ct_StreamUtil;
   private int task;
   private int pool;
   private int index;
   private long init_J;
   private int count;
   private String init_Str;
   private Map<String, String> load_Map;
   private static final int flags = 10;
   private static final int level = 3;
   private static final int frame = 26;
   private static final int block = 4;
   private static final int chunk = 2;
   private static final int table = 4;
   private static final int store = 6;
   private static final int drain = 10;
   private static final int fill = 264;
   private static final int mark = 8;
   private DataUtil.CachePool init_DataUtil_CachePool;
   private int scan;
   private int parse;
   private int bind;
   private float init_F;
   private int f_a_I;
   private int f_b_I;
   private int f_c_I;
   private int f_d_I;
   private int f_e_I;
   private int f_f_I;
   private final List<DataUtil.NetHelper> read_List;
   private final List<DataUtil.StreamUtil> write_List;
   private boolean update_Z;
   private int f_g_I;
   private int f_h_I;
   private String load_Str;
   private boolean sync_Z;

   public DataUtil(DataUtil.BufferHelper host, Font font) {
      this.init_DataUtil_JsonUtil = DataUtil.JsonUtil.init_DataUtil_JsonUtil;
      this.init_DataUtil_TaskQueue = DataUtil.TaskQueue.init;
      this.init_Z = true;
      this.load_Z = true;
      this.init_IArr = new int[DataUtil.JsonUtil.values().length];
      this.load_IArr = new int[DataUtil.JsonUtil.values().length];
      this.prev = -1;
      this.init_ItemStack = ItemStack.EMPTY;
      this.buffer = -2147483648;
      this.entry = 2147483647;
      this.init_BitSetUtil = this.init_BitSetUtil_Str("Message, /command, or paste a shared hash…", 100000, false);
      this.load_BitSetUtil = this.init_BitSetUtil_Str("Lobby name", 32, false);
      this.read_BitSetUtil = this.init_BitSetUtil_Str("Server (optional)", 48, false);
      this.write_BitSetUtil = this.init_BitSetUtil_Str("40", 4, true);
      this.update_BitSetUtil = this.init_BitSetUtil_Str("Paste the Room Key", 64, false);
      this.sync_BitSetUtil = this.init_BitSetUtil_Str("Paste the Room Code", 24, false);
      this.load_List = new ArrayList<>();
      this.init_Map = new LinkedHashMap<>();
      this.pool = -1;
      this.index = -1;
      this.init_J = -9223372036854775808L;
      this.count = -1;
      this.init_Str = "";
      this.load_Map = Map.of();
      this.init_DataUtil_CachePool = DataUtil.CachePool.init;
      this.read_List = new ArrayList<>();
      this.write_List = new ArrayList<>();
      this.load_Str = "";
      this.init_DataUtil_BufferHel = host;
      this.init_Font = font;
      this.load_Z = this.init_EventBus.flush();
      this.load_BitSetUtil.init_BitSetUtil_Str(this.init_EventBus.init_Str());
      this.read_BitSetUtil.init_BitSetUtil_Str(this.init_EventBus.load_Str());
      this.write_BitSetUtil.init_BitSetUtil_Str(this.init_EventBus.init_I() <= 0 ? "" : Integer.toString(this.init_EventBus.init_I()));
      this.load_BitSetUtil.init_BitSetUtil_Consumer(this.init_EventBus::init_V_Str);
      this.read_BitSetUtil.init_BitSetUtil_Consumer(this.init_EventBus::load_V);
      this.write_BitSetUtil.init_BitSetUtil_Consumer(v -> this.init_EventBus.init_V_I(init_I_Str(v)));
      this.init_BitSetUtil.update(true).sync(true);
      this.init_BitSetUtil.load_BitSetUtil_Consumer(v -> this.next());
      this.sync_BitSetUtil.load_BitSetUtil_Consumer(v -> this.open_V());
   }

   private BitSetUtil init_BitSetUtil_Str(String placeholder, int maxLen, boolean numeric) {
      BitSetUtil f = new BitSetUtil();
      f.load_BitSetUtil_Str(placeholder).init_BitSetUtil_I(maxLen).read_BitSetUtil_I(13);
      if (numeric) {
         f.init_BitSetUtil_Predicate(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
      }

      return f;
   }

   public void init_V() {
      this.init_BufferHelper.write_V();
   }

   public void load_V() {
      this.init_BufferHelper.update_V();
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int h, int mouseX, int mouseY, float partial) {
      this.write_V();
      this.init_F = partial;
      this.f_a_I = mouseX;
      this.f_b_I = mouseY;
      this.f_c_I = x;
      this.f_d_I = y;
      this.f_e_I = w;
      this.f_f_I = h;
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      if (!this.init_BufferHelper.init_Z() && (this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.load || this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.read)) {
         this.init_DataUtil_JsonUtil = DataUtil.JsonUtil.init_DataUtil_JsonUtil;
      }

      if (this.init_BufferHelper.init_Z() && this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.init_DataUtil_JsonUtil) {
         this.init_DataUtil_JsonUtil = DataUtil.JsonUtil.load;
      }

      if (this.init_DataUtil_JsonUtil != DataUtil.JsonUtil.read && this.update_Z) {
         this.valid();
      }

      PacketUtil.init(g, autismclient.gui.f.NetHelper.init(x, y, w, h), -200405488);
      DataUtil.CachePool gate = this.init_DataUtil_CachePool();
      this.init_DataUtil_CachePool = gate;
      if (gate == DataUtil.CachePool.load) {
         this.init_V_GuiGraphicsExtract(g, x, y, w, h, mouseX, mouseY);
      } else if (gate == DataUtil.CachePool.read) {
         this.init_V_GuiGraphicsExtract_2(g, x, y, w, h, mouseX, mouseY, this.init_BufferHelper.init_BufferHelper_Strea());
      } else if (gate == DataUtil.CachePool.write) {
         this.load(g, x, y, w, h, mouseX, mouseY);
      } else {
         autismclient.util.mm.DataUtil lb = this.init_BufferHelper.init_DataUtil();
         String status = this.init_BufferHelper.init_Z()
            ? "In lobby: "
               + lb.load_Str
               + (lb.load_Z ? "  [Announcement]" : "")
               + "   ("
               + (
                  this.init_BufferHelper.update_Z()
                     ? this.init_BufferHelper.init_I() + " users"
                     : this.init_BufferHelper.init_I() + "/" + init_Str_I(lb.init_I)
               )
               + ")"
            : "Not in a lobby";
         int textRow = flush_I();
         int statusY = y + 4;
         this.init_V_GuiGraphicsExtract_3(
            g, status, x + 4, this.init_I_I_I(statusY, textRow), this.init_BufferHelper.init_Z() ? this.set : this.add, false, w - 8
         );
         String subline = !this.init_BufferHelper.init_Z()
            ? "Browse public lobbies or create your own below."
            : (
               lb.load_Z && !this.init_BufferHelper.read_Z()
                  ? "Announcement lobby  •  Read-only"
                  : (
                     lb.load_Z
                        ? "Announcement lobby  •  You can send"
                        : (lb.init_Z ? "Invite with the Room Code in the Members tab." : "Invite with the Room Key in the Members tab.")
                  )
            );
         this.init_V_GuiGraphicsExtract_3(g, subline, x + 4, this.init_I_I_I(statusY + textRow, textRow), this.add, false, w - 8);
         int tabY = statusY + textRow * 2 + 2;
         int tabH = update_I();
         int n = DataUtil.JsonUtil.values().length;
         int tabW = (w - 8 - (n - 1) * 2) / n;
         int tx = x + 4;

         for (DataUtil.JsonUtil tab : DataUtil.JsonUtil.values()) {
            if (tab == DataUtil.JsonUtil.init_DataUtil_JsonUtil && this.init_BufferHelper.init_Z()) {
               boolean hover = mouseX >= tx && mouseX < tx + tabW && mouseY >= tabY && mouseY < tabY + tabH;
               PacketUtil.load(g, autismclient.gui.f.NetHelper.init(tx, tabY, tabW, tabH), 3, load_I_I_I(this.clear, hover ? 238 : 204), this.clear);
               this.init_V_GuiGraphicsExtract_3(g, "Leave", tx + tabW / 2, this.init_I_I_I(tabY, tabH), -1, true, tabW - 4);
               this.init_V_I(tx, tabY, tabW, tabH, this::sync_V);
            } else {
               boolean active = tab == this.init_DataUtil_JsonUtil;
               boolean usable = this.init_Z_DataUtil_JsonUtil(tab);
               this.init_V_GuiGraphicsExtract(g, tx, tabY, tabW, tabH, tab.init_Str, this.set, active, usable, mouseX, mouseY, usable ? () -> {
                  this.init_DataUtil_JsonUtil = tab;
                  this.read_V();
               } : null);
            }

            tx += tabW + 2;
         }

         int contentTop = this.init_I_GuiGraphicsExtract(g, x, tabY + tabH + 4, w, mouseX, mouseY);
         int contentBottom = y + h - 4;
         int cx = x + 4;
         int cw = w - 8;
         this.reset = contentTop - y;
         this.start = contentTop;
         this.stop = Math.max(0, contentBottom - contentTop);
         if (this.init_DataUtil_JsonUtil != DataUtil.JsonUtil.load) {
            this.init_IArr[this.init_DataUtil_JsonUtil.ordinal()] = Math.max(
               0, Math.min(this.init_IArr[this.init_DataUtil_JsonUtil.ordinal()], this.load_I_DataUtil_JsonUtil(this.init_DataUtil_JsonUtil))
            );
         }

         SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(g, mouseX, mouseY);
         switch (this.init_DataUtil_JsonUtil) {
            case init_DataUtil_JsonUtil:
               this.init_V_GuiGraphicsExtract_3(g, ctx, cx, contentTop, cw, contentBottom, mouseX, mouseY);
               break;
            case load:
               this.load_V(g, ctx, cx, contentTop, cw, contentBottom, mouseX, mouseY);
               break;
            case read:
               this.read(g, cx, contentTop, cw, contentBottom, mouseX, mouseY);
               break;
            case write:
               this.read(g, ctx, cx, contentTop, cw, contentBottom, mouseX, mouseY);
         }

         this.init_V_GuiGraphicsExtract(g, mouseX, mouseY);
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my) {
      int cx = x + w / 2;
      this.scan = 0;
      int ty = y + 4 + 2;
      ty = this.init_I_GuiGraphicsExtract_2(g, "Matchmaking", cx, ty, w, this.set, 14);
      String notice = L.init_Str();
      boolean gated = L.init_Z(notice);
      String[] lines;
      if (gated) {
         lines = new String[]{"Your shit's out of date.", "", "Grab the latest from autismclient.com,", "then restart Minecraft and sign back in."};
      } else {
         lines = new String[]{"Find players, share macros, and TPA across", "servers, all end to end encrypted.", "", "No sign-in. Everything is unlocked."};
      }

      for (int i = 0; i < lines.length; i++) {
         if (lines[i].isEmpty()) {
            ty += 5;
         } else {
            ty = this.init_I_GuiGraphicsExtract_2(g, lines[i], cx, ty, w, gated && i == 0 ? this.clear : this.put, 11);
         }
      }

      ty += 4;
      int bw2 = Math.min(Math.max(this.load_I_Str("Join our Discord"), gated ? this.load_I_Str("Get the latest version") : 0), w - 8);
      this.scan = Math.max(this.scan, bw2);
      int bxc = cx - bw2 / 2;
      int ctl = update_I();
      if (gated) {
         this.init_V_GuiGraphicsExtract(g, bxc, ty, bw2, ctl, "Get the latest version", this.set, this.put, mx, my, () -> aJ.init_V("https://autismclient.com"));
         ty += ctl + 2;
      }

      this.init_V_GuiGraphicsExtract(
         g, bxc, ty, bw2, ctl, "Join our Discord", this.run, this.put, mx, my, () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu")
      );
      this.parse = ty + ctl + 4 - y;
   }

   private void load(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my) {
      int cx = x + w / 2;
      this.scan = 0;
      int ty = y + 4 + 2;
      ty = this.init_I_GuiGraphicsExtract_2(g, "Matchmaking", cx, ty, w, this.set, 14);
      if (this.init_BufferHelper.init_BufferHelper_DataU() == autismclient.util.mm.BufferHelper.Inner_DataUtil.read) {
         if (L.init_Z(L.init_Str())) {
            ty = this.init_I_GuiGraphicsExtract_2(g, L.init_Str(L.init_Str()), cx, ty, w, this.clear, 12);
            String min = L.load_Str();
            if (!min.isEmpty()) {
               ty = this.init_I_GuiGraphicsExtract_2(g, "Minimum version: " + min, cx, ty, w, this.add, 11);
            }

            ty = this.init_I_GuiGraphicsExtract_2(g, "Get the latest download at autismclient.com", cx, ty, w, this.add, 11);
         } else {
            ty = this.init_I_GuiGraphicsExtract_2(g, "Matchmaking is unlocked.", cx, ty, w, this.clear, 12);
            ty = this.init_I_GuiGraphicsExtract_2(g, "No Discord account or sign-in needed.", cx, ty, w, this.add, 11);
            ty = this.init_I_GuiGraphicsExtract_2(g, "If the server is unreachable, try again.", cx, ty, w, this.add, 11);
            String err = this.init_BufferHelper.flush_Str();
            if (!err.isEmpty()) {
               ty = this.init_I_GuiGraphicsExtract_2(g, err, cx, ty, w, load_I_I_I(this.add, 170), 11);
            }
         }

         ty += 4;
         int bw2 = Math.min(Math.max(this.load_I_Str("Try again"), this.load_I_Str("Reset connection")), w - 8);
         this.scan = Math.max(this.scan, bw2);
         int bxc = cx - bw2 / 2;
         int ctl = update_I();
         this.init_V_GuiGraphicsExtract(g, bxc, ty, bw2, ctl, "Try again", this.set, this.put, mx, my, this.init_BufferHelper::init_V);
         ty += ctl + 2;
         this.init_V_GuiGraphicsExtract(g, bxc, ty, bw2, ctl, "Reset connection", this.run, this.put, mx, my, this::start);
         this.parse = ty + ctl + 4 - y;
      } else {
         long t = System.currentTimeMillis();
         String dots = ".".repeat((int)(t / 400L % 4L));
         this.scan = k.init(this.init_Font, "Connecting to matchmaking server...", this.init_CharEncoder.init_Identifier_l(l.read), this.put);
         ty = this.init_I_GuiGraphicsExtract_2(g, "Connecting to matchmaking server" + dots, cx, ty, w, this.put, 12);
         char spin = "|/-\\".charAt((int)(t / 120L % 4L));
         ty = this.init_I_GuiGraphicsExtract_2(g, String.valueOf(spin), cx, ty, w, this.set, 12);
         ty = this.init_I_GuiGraphicsExtract_2(g, "This only takes a moment.", cx, ty, w, this.add, 11);
         this.parse = ty + 4 - y;
      }
   }

   private void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my, autismclient.util.mm.BufferHelper.Inner_StreamUtil jp
   ) {
      int cx = x + w / 2;
      this.scan = 0;
      int ty = y + 4 + 2;
      ty = this.init_I_GuiGraphicsExtract_2(g, "Joining lobby", cx, ty, w, this.set, 14);
      int ctl = update_I();
      if (jp == autismclient.util.mm.BufferHelper.Inner_StreamUtil.write) {
         ty = this.init_I_GuiGraphicsExtract_2(g, this.init_Str_Str(this.init_BufferHelper.close_Str()), cx, ty, w, this.clear, 12);
         ty = this.init_I_GuiGraphicsExtract_2(g, "Nothing was joined.", cx, ty, w, this.add, 11);
         ty += 4;
         int bw2 = Math.min(this.load_I_Str("Back"), w - 8);
         this.init_V_GuiGraphicsExtract(g, cx - bw2 / 2, ty, bw2, ctl, "Back", this.set, this.put, mx, my, this.init_BufferHelper::read_V);
         this.parse = ty + ctl + 4 - y;
      } else {
         boolean reserving = jp == autismclient.util.mm.BufferHelper.Inner_StreamUtil.load;
         char spin = "|/-\\".charAt((int)(System.currentTimeMillis() / 120L % 4L));
         String[] steps = new String[]{"Checking membership", "Checking lobby ban", "Reserving a slot", "Connecting"};

         for (int i = 0; i < steps.length; i++) {
            boolean done = !reserving && i < 3;
            boolean active = reserving ? i < 3 : i == 3;
            String mark = done ? "[x] " : (active ? "[" + spin + "] " : "[ ] ");
            int col = done ? this.set : (active ? this.put : this.add);
            this.scan = Math.max(this.scan, k.init(this.init_Font, "[x] " + steps[i], this.init_CharEncoder.init_Identifier_l(l.read), col));
            this.init_V_GuiGraphicsExtract_3(g, mark + steps[i], cx, ty, col, true, w - 8);
            ty += 12;
         }

         ty += 4;
         int bw2 = Math.min(this.load_I_Str("Cancel"), w - 8);
         this.init_V_GuiGraphicsExtract(g, cx - bw2 / 2, ty, bw2, ctl, "Cancel", this.run, this.put, mx, my, this.init_BufferHelper::read_V);
         this.parse = ty + ctl + 4 - y;
      }
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor g, String text, int cx, int y, int w, int color, int advance) {
      this.init_V_GuiGraphicsExtract_3(g, text, cx, y, color, true, w - 8);
      this.scan = Math.max(this.scan, k.init(this.init_Font, text, this.init_CharEncoder.init_Identifier_l(l.read), color));
      return y + advance;
   }

   private DataUtil.CachePool init_DataUtil_CachePool() {
      if (!L.load_Z()) {
         return DataUtil.CachePool.load;
      } else if (this.init_BufferHelper.init_BufferHelper_Strea() != autismclient.util.mm.BufferHelper.Inner_StreamUtil.init) {
         return DataUtil.CachePool.read;
      } else {
         return !this.init_BufferHelper.open_Z() ? DataUtil.CachePool.write : DataUtil.CachePool.init;
      }
   }

   private String init_Str_Str(String err) {
      String var10000;
      switch (err == null ? "" : err) {
         case "no_such_lobby":
            var10000 = "No lobby found for that code.";
            break;
         case "full":
            var10000 = "That lobby is full.";
            break;
         case "lobby_banned":
            var10000 = "You're banned from that lobby.";
            break;
         case "too_many":
            var10000 = "You're in too many lobbies already.";
            break;
         case "busy":
            var10000 = "Server busy. Try again.";
            break;
         case "unauthorized":
            var10000 = "Sign in again.";
            break;
         case "old_version":
            var10000 = "Update the mod.";
            break;
         case "network":
            var10000 = "Can't reach matchmaking. Try again.";
            break;
         default:
            var10000 = "Couldn't join. Try again.";
      }

      return var10000;
   }

   private void write_V() {
      ct.Inner_StreamUtil st = ct.init_ct_StreamUtil();
      if (st != this.init_ct_StreamUtil) {
         this.init_ct_StreamUtil = st;
         this.run = ct.init_I_I_ct_DataUtil(-13425624, ct.Inner_DataUtil.load);
         this.get = ct.init_I_I_ct_DataUtil(-8759968, ct.Inner_DataUtil.load);
         this.set = ct.init_I_I_ct_DataUtil(-13248397, ct.Inner_DataUtil.init);
         this.put = ct.init_I_I_ct_DataUtil(-855310, ct.Inner_DataUtil.read);
         this.add = ct.init_I_I_ct_DataUtil(-6645094, ct.Inner_DataUtil.read);
         this.clear = ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.flush);
      }
   }

   private boolean init_Z_DataUtil_JsonUtil(DataUtil.JsonUtil tab) {
      boolean var10000;
      switch (tab) {
         case load:
         case read:
            var10000 = this.init_BufferHelper.init_Z();
            break;
         default:
            var10000 = true;
      }

      return var10000;
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
      autismclient.util.mm.DataUtil lb = this.init_BufferHelper.init_DataUtil();
      if (lb == null) {
         return y;
      } else {
         String ip = lb.read_Str == null ? "" : lb.read_Str.trim();
         if (!ip.isEmpty() && !autismclient.util.mm.BufferHelper.init_Z_Str(ip)) {
            int rowH = open_I();
            int joinW = this.load_I_Str("Join");
            autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x + 4, y, w - 8, rowH));
            this.init_V_GuiGraphicsExtract_3(
               g, "Server: " + autismclient.util.mm.ConfigStore.init_Str(ip), x + 4 + 6, this.init_I_I_I(y, rowH), -7429889, false, w - 8 - 6 - joinW - 1 - 4
            );
            String label = lb.load_Str;
            this.init_V_GuiGraphicsExtract(
               g,
               x + w - 4 - 1 - joinW,
               y + 1,
               joinW,
               rowH - 2,
               "Join",
               -7429889,
               this.put,
               mx,
               my,
               () -> autismclient.util.mm.TaskQueue.init(ip, label, this.init_DataUtil_BufferHel.init_Screen())
            );
            return y + rowH + 4;
         } else {
            return y;
         }
      }
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor g, SyncManager ctx, int x, int top, int w, int bottom, int mx, int my) {
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(x, top, w, Math.max(0, bottom - top)));
      this.buffer = top;
      this.entry = bottom;

      try {
         int top0 = top - this.init_I_DataUtil_JsonUtil(DataUtil.JsonUtil.init_DataUtil_JsonUtil);
         int segW = (w - 4) / 3;
         int segH = update_I();
         this.init_V_GuiGraphicsExtract(
            g,
            x,
            top0,
            segW,
            segH,
            "Browse",
            this.set,
            this.init_DataUtil_TaskQueue == DataUtil.TaskQueue.init,
            true,
            mx,
            my,
            () -> this.init_V_DataUtil_TaskQueue(DataUtil.TaskQueue.init)
         );
         this.init_V_GuiGraphicsExtract(
            g,
            x + segW + 2,
            top0,
            segW,
            segH,
            "Create",
            this.set,
            this.init_DataUtil_TaskQueue == DataUtil.TaskQueue.load,
            true,
            mx,
            my,
            () -> this.init_V_DataUtil_TaskQueue(DataUtil.TaskQueue.load)
         );
         this.init_V_GuiGraphicsExtract(
            g,
            x + (segW + 2) * 2,
            top0,
            w - (segW + 2) * 2,
            segH,
            "Join",
            this.set,
            this.init_DataUtil_TaskQueue == DataUtil.TaskQueue.read,
            true,
            mx,
            my,
            () -> this.init_V_DataUtil_TaskQueue(DataUtil.TaskQueue.read)
         );
         int paneTop = top0 + segH + 4;
         int var10000;
         switch (this.init_DataUtil_TaskQueue) {
            case init:
               var10000 = this.init_I_GuiGraphicsExtract(g, x, paneTop, w, top, bottom, mx, my);
               break;
            case load:
               var10000 = this.init_I_GuiGraphicsExtract_3(g, ctx, x, paneTop, w, bottom, mx, my);
               break;
            case read:
               var10000 = this.load_I(g, ctx, x, paneTop, w, bottom, mx, my);
               break;
            default:
               throw new MatchException(null, null);
         }

         int paneBottom = var10000;
         this.init_V_DataUtil_JsonUtil_I(DataUtil.JsonUtil.init_DataUtil_JsonUtil, paneBottom);
      } finally {
         this.buffer = -2147483648;
         this.entry = 2147483647;
         HashUtil.init_HashUtil().init(g);
      }
   }

   private void init_V_DataUtil_TaskQueue(DataUtil.TaskQueue pane) {
      this.init_DataUtil_TaskQueue = pane;
      this.read_V();
      this.init_IArr[DataUtil.JsonUtil.init_DataUtil_JsonUtil.ordinal()] = 0;
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int top, int bottom, int mx, int my) {
      int refreshW = this.load_I_Str("Refresh");
      int ctl = update_I();
      int textRow = flush_I();
      this.init_V_GuiGraphicsExtract_3(g, "Public lobbies", x, this.init_I_I_I(y, ctl), this.put, false, w - refreshW - 4);
      this.init_V_GuiGraphicsExtract(g, x + w - refreshW, y, refreshW, ctl, "Refresh", this.run, this.put, mx, my, this::flush_V);
      y += ctl;
      boolean refreshing = this.init_BufferHelper.poll_Z();
      boolean hasRefreshFeedback = refreshing || this.init_BufferHelper.init_J() > 0L;
      if (refreshing) {
         int barY = y + textRow / 2 - 1;
         PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, barY, w, 2), 1, load_I_I_I(this.add, 64));
         long t = System.currentTimeMillis();
         int segW = Math.max(24, w / 4);
         int off = (int)(t / 5L % (w + segW)) - segW;
         int sx = Math.max(x, x + off);
         int ex = Math.min(x + w, x + off + segW);
         if (ex > sx) {
            PacketUtil.load(g, autismclient.gui.f.NetHelper.init(sx, barY, ex - sx, 2), 1, this.set);
         }
      } else if (hasRefreshFeedback) {
         long ago = Math.max(0L, (System.currentTimeMillis() - this.init_BufferHelper.init_J()) / 1000L);
         this.init_V_GuiGraphicsExtract_3(g, "Refreshed " + ago + "s ago", x, this.init_I_I_I(y, textRow), this.add, false, w - 64);
      }

      y += (hasRefreshFeedback ? textRow : 0) + 2;
      List<autismclient.util.mm.StreamUtil> listings = this.init_BufferHelper.read_List();
      if (listings.isEmpty()) {
         Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
         y += this.init_I_GuiGraphicsExtract_4(g, "No public lobbies announced yet.", x + 2, y + 2, w - 4, w - 4, this.add, fontId) * 10 + 2;
         y += this.init_I_GuiGraphicsExtract_4(g, "Use Create to host one, or Join to enter a code/passphrase.", x + 2, y + 2, w - 4, w - 4, this.add, fontId)
            * 10;
         return y + 4;
      }

      int cardH = 26;

      for (autismclient.util.mm.StreamUtil l : listings) {
         boolean visible = y + cardH > top && y < bottom;
         if (visible) {
            boolean full = l.init();
            String joinLabel = full ? "Full" : "Join";
            int joinW = this.load_I_Str(joinLabel);
            int textW = w - 12 - joinW - 4;
            autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, cardH));
            this.init_V_GuiGraphicsExtract_3(g, l.load_Str, x + 6, y + 3, l.load_Z ? this.clear : this.put, false, textW);
            String tags = l.load_Z ? l.init_I + " users" : l.init_I + "/" + init_Str_I(l.load_I);
            if (l.read_Z) {
               tags = tags + "  •  Announcement";
            }

            if (l.read_I == 0) {
               tags = tags + "  •  dupe hunting";
            } else if (l.read_I == 1) {
               tags = tags + "  •  has dupe";
            }

            if (!l.read_Str.isBlank()) {
               tags = tags + "  •  " + l.read_Str;
            }

            this.init_V_GuiGraphicsExtract_3(g, tags, x + 6, y + 3 + 10 + 1, this.add, false, textW);
            this.init_V_GuiGraphicsExtract(
               g,
               x + w - 6 - joinW,
               y + (cardH - ctl) / 2,
               joinW,
               ctl,
               joinLabel,
               full ? this.run : this.set,
               full ? this.add : this.put,
               mx,
               my,
               full ? null : () -> {
                  this.init_BufferHelper.init_V_Str(l.init_Str);
                  this.update_V();
               }
            );
         }

         y += cardH + 2;
      }

      return y;
   }

   private int init_I_GuiGraphicsExtract_3(GuiGraphicsExtractor g, SyncManager ctx, int x, int y, int w, int bottom, int mx, int my) {
      boolean pub = this.load_Z;
      int ctl = update_I();
      int pitch = ctl + 2;
      int lw = this.init_I_StrArr("Visibility", "Name *", "Server", "Messaging", "Max", "Dupe *");
      int fx = x + lw;
      int fw = w - lw;
      int chipW = this.poll_I();
      this.init_V_GuiGraphicsExtract_3(g, "Visibility", x, this.init_I_I_I(y, ctl), this.add, false, lw);
      this.init_V_GuiGraphicsExtract(g, fx, y, chipW, ctl, "Public", this.set, pub, true, mx, my, () -> this.load_Z = true);
      this.init_V_GuiGraphicsExtract(g, fx + chipW + 2, y, chipW, ctl, "Private", -14249, !pub, true, mx, my, () -> this.load_Z = false);
      y += pitch;
      this.init_V_GuiGraphicsExtract_3(g, "Name *", x, this.init_I_I_I(y, ctl), this.add, false, lw);
      this.init_V_GuiGraphicsExtract_2(g, ctx, this.load_BitSetUtil, fx, y, fw);
      y += pitch;
      if (pub) {
         this.init_V_GuiGraphicsExtract_3(g, "Server", x, this.init_I_I_I(y, ctl), this.add, false, lw);
         this.init_V_GuiGraphicsExtract_2(g, ctx, this.read_BitSetUtil, fx, y, fw);
         y += pitch;
      }

      if (L.read_Z()) {
         this.init_V_GuiGraphicsExtract_3(g, "Messaging", x, this.init_I_I_I(y, ctl), this.add, false, lw);
         int mw = (fw - 2) / 2;
         this.init_V_GuiGraphicsExtract(g, fx, y, mw, ctl, "Everyone", this.set, !this.read_Z, true, mx, my, () -> this.read_Z = false);
         this.init_V_GuiGraphicsExtract(g, fx + mw + 2, y, fw - mw - 2, ctl, "Announcement", -14249, this.read_Z, true, mx, my, () -> this.read_Z = true);
         y += pitch;
      } else {
         this.read_Z = false;
      }

      this.init_V_GuiGraphicsExtract_3(g, "Max", x, this.init_I_I_I(y, ctl), this.add, false, lw);
      int maxW = 36;
      this.init_V_GuiGraphicsExtract_2(g, ctx, this.write_BitSetUtil, fx, y, maxW);
      int capLimit = write_I();
      this.init_V_GuiGraphicsExtract_3(
         g, "(blank = " + capLimit + ", max " + capLimit + ")", fx + maxW + 4, this.init_I_I_I(y, ctl), this.add, false, fw - maxW - 4
      );
      y += pitch;
      if (pub) {
         this.init_V_GuiGraphicsExtract_3(g, "Dupe *", x, this.init_I_I_I(y, ctl), this.add, false, lw);
         int dw = (fw - 2) / 2;
         this.init_V_GuiGraphicsExtract(
            g, fx, y, dw, ctl, "Dupe Hunting", -14249, this.init_EventBus.load_I() == 0, true, mx, my, () -> this.init_EventBus.load(0)
         );
         this.init_V_GuiGraphicsExtract(
            g, fx + dw + 2, y, fw - dw - 2, ctl, "Has dupe", this.set, this.init_EventBus.load_I() == 1, true, mx, my, () -> this.init_EventBus.load(1)
         );
         y += pitch;
      }

      y += 2;
      String createLabel = pub ? "Create public lobby" : "Create private lobby";
      int createW = this.load_I_Str(createLabel);
      this.init_V_GuiGraphicsExtract(g, x + (w - createW) / 2, y, createW, ctl, createLabel, this.set, this.put, mx, my, this::close_V);
      y += ctl + 2;
      int textRow = flush_I();
      this.init_V_GuiGraphicsExtract_3(
         g, pub ? "Name and dupe status are required." : "A secure Room Key is generated for you to share.", x, this.init_I_I_I(y, textRow), this.add, false, w
      );
      return y + textRow;
   }

   private int load_I(GuiGraphicsExtractor g, SyncManager ctx, int x, int y, int w, int bottom, int mx, int my) {
      int ctl = update_I();
      int pitch = ctl + 2;
      int lw = this.init_I_StrArr("Join by", "Room Code", "Room Key");
      int fx = x + lw;
      int fw = w - lw;
      int chipW = this.poll_I();
      this.init_V_GuiGraphicsExtract_3(g, "Join by", x, this.init_I_I_I(y, ctl), this.add, false, lw);
      this.init_V_GuiGraphicsExtract(g, fx, y, chipW, ctl, "Public", this.set, this.init_Z, true, mx, my, () -> {
         this.init_Z = true;
         this.read_V();
      });
      this.init_V_GuiGraphicsExtract(g, fx + chipW + 2, y, chipW, ctl, "Private", -14249, !this.init_Z, true, mx, my, () -> {
         this.init_Z = false;
         this.read_V();
      });
      y += pitch;
      int joinBtnW = this.load_I_Str("Join");
      int textRow = flush_I();
      if (this.init_Z) {
         this.init_V_GuiGraphicsExtract_3(g, "Room Code", x, this.init_I_I_I(y, ctl), this.add, false, lw);
         this.init_V_GuiGraphicsExtract_2(g, ctx, this.sync_BitSetUtil, fx, y, fw);
         int var18 = y + (pitch + 4 - 2);
         this.init_V_GuiGraphicsExtract(g, x + (w - joinBtnW) / 2, var18, joinBtnW, ctl, "Join", this.set, this.put, mx, my, this::open_V);
         y = var18 + ctl + 2;
         this.init_V_GuiGraphicsExtract_3(g, "Paste the public Room Code someone shared with you.", x, this.init_I_I_I(y, textRow), this.add, false, w);
      } else {
         this.init_V_GuiGraphicsExtract_3(g, "Room Key", x, this.init_I_I_I(y, ctl), this.add, false, lw);
         this.init_V_GuiGraphicsExtract_2(g, ctx, this.update_BitSetUtil, fx, y, fw);
         int var20 = y + (pitch + 4 - 2);
         this.init_V_GuiGraphicsExtract(g, x + (w - joinBtnW) / 2, var20, joinBtnW, ctl, "Join", this.set, this.put, mx, my, this::poll_V);
         y = var20 + ctl + 2;
         this.init_V_GuiGraphicsExtract_3(g, "Paste the private Room Key someone shared with you.", x, this.init_I_I_I(y, textRow), this.add, false, w);
      }

      return y + textRow;
   }

   private void update_V() {
      this.init_DataUtil_JsonUtil = DataUtil.JsonUtil.load;
      this.read_V();
   }

   private void sync_V() {
      this.init_BufferHelper.close_V();
      this.init_DataUtil_JsonUtil = DataUtil.JsonUtil.init_DataUtil_JsonUtil;
      this.init_DataUtil_TaskQueue = DataUtil.TaskQueue.init;
      this.init_IArr[DataUtil.JsonUtil.init_DataUtil_JsonUtil.ordinal()] = 0;
      this.init_BufferHelper.open_V();
      this.read_V();
   }

   private void flush_V() {
      this.init_BufferHelper.open_V();
   }

   private void close_V() {
      String name = this.load_BitSetUtil.init_Str().trim();
      if (name.isEmpty()) {
         bi.init("Enter a lobby name.", -14249);
      } else {
         int max = init_I_Str(this.write_BitSetUtil.init_Str());
         int capLimit = write_I();
         if (max <= 0) {
            max = capLimit;
         }

         if (max > capLimit) {
            max = capLimit;
            bi.init("Max players is capped at " + capLimit + ".", -14249);
         }

         if (this.load_Z) {
            if (this.init_EventBus.load_I() < 0) {
               bi.init("Pick a dupe status (needs or has).", -14249);
               return;
            }

            this.init_BufferHelper
               .init_V_CachePool(new autismclient.util.mm.CachePool(name, true, max, null, this.read_BitSetUtil.init_Str().trim(), "", this.read_Z));
         } else {
            this.init_BufferHelper.init_V_CachePool(new autismclient.util.mm.CachePool(name, false, max, null, "", "", this.read_Z));
         }

         this.init_EventBus.flush_V_Z(this.load_Z);
         this.update_V();
      }
   }

   private void open_V() {
      String code = this.sync_BitSetUtil.init_Str().trim();
      if (code.isEmpty()) {
         bi.init("Enter a lobby code.", -14249);
      } else {
         this.init_BufferHelper.init_V_Str(code);
         this.update_V();
      }
   }

   private void poll_V() {
      String key = this.update_BitSetUtil.init_Str().trim();
      if (key.isEmpty()) {
         bi.init("Paste the room key.", -14249);
      } else {
         this.init_BufferHelper.init_V_CArr(key.toCharArray());
         this.update_V();
      }
   }

   private static int init_I_Str(String s) {
      if (s != null && !s.isBlank()) {
         try {
            return Integer.parseInt(s.trim());
         } catch (NumberFormatException e) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static int write_I() {
      short var10000;
      switch (L.update_Str()) {
         case "god":
         case "admin":
            var10000 = 500;
            break;
         case "gold":
            var10000 = 50;
            break;
         case "aqua":
            var10000 = 50;
            break;
         case "notbroke":
            var10000 = 8;
            break;
         case "blue":
            var10000 = 4;
            break;
         default:
            var10000 = 4;
      }

      return var10000;
   }

   private void load_V(GuiGraphicsExtractor g, SyncManager ctx, int x, int top, int w, int bottom, int mx, int my) {
      boolean canWrite = this.init_BufferHelper.read_Z();
      this.init_BitSetUtil
         .init_BitSetUtil_Z(canWrite)
         .load_BitSetUtil_Str(canWrite ? "Message, /command, or paste a shared hash…" : "Announcement is read-only");
      int sendW = this.load_I_Str("Send");
      int ctl = update_I();
      int composerFieldW = w - sendW - 2;
      int composerRows = Math.max(1, Math.min(3, this.init_BitSetUtil.init_I_SyncManager_I(ctx, composerFieldW)));
      int composerH = composerRows <= 1 ? ctl : Math.max(ctl, this.init_BitSetUtil.load(ctx, composerRows));
      int composerY = bottom - composerH;
      int toolR1 = composerY - (ctl + 2) * 2;
      int chatBottom = toolR1 - 4;
      this.prev = bottom - chatBottom;
      Map<String, String> names = this.init_Map_List(this.init_BufferHelper.load_List());
      List<autismclient.util.mm.JsonUtil> lines = this.init_BufferHelper.write_List();
      this.bind = w;
      if (lines.isEmpty()) {
         this.init_I_GuiGraphicsExtract_4(
            g,
            "No messages yet. Say hi, type a /command, or use the Share buttons below.",
            x,
            top + 2,
            w,
            w,
            this.add,
            this.init_CharEncoder.init_Identifier_l(l.read)
         );
      }

      int viewPx = Math.max(0, chatBottom - top);
      int[] heights = this.init_IArr_List(lines, w, names);
      boolean reserveBar = init_I_IArr(heights) > viewPx;
      int logW = reserveBar ? w - 6 - 2 : w;
      if (reserveBar) {
         heights = this.init_IArr_List(lines, logW, names);
      }

      int logPx = init_I_IArr(heights);
      this.valid = viewPx;
      this.size = Math.max(0, logPx - this.valid);
      int sc = Math.max(0, Math.min(this.size, this.init_IArr[DataUtil.JsonUtil.load.ordinal()]));
      this.init_IArr[DataUtil.JsonUtil.load.ordinal()] = sc;
      this.init_ItemStack = ItemStack.EMPTY;
      this.state = top;
      this.queue = chatBottom;
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(x, top, w, this.valid));

      try {
         int yPos = chatBottom + sc;

         for (int i = lines.size() - 1; i >= 0; i--) {
            autismclient.util.mm.JsonUtil line = lines.get(i);
            int lh = heights[i];
            yPos -= lh;
            if (yPos < chatBottom) {
               if (yPos + lh <= top) {
                  break;
               }

               if (line.init_Z()) {
                  this.load(g, line, names, x, yPos, logW, mx, my);
               } else {
                  this.init_V_GuiGraphicsExtract_4(g, line, names, x, yPos, logW, mx, my);
               }
            }
         }
      } finally {
         HashUtil.init_HashUtil().init(g);
      }

      if (reserveBar) {
         int trackX = x + w - 3;
         this.init_PoolEntry_DataUtil = PoolEntry.init_PoolEntry_DataUtil_I_2(logPx, this.valid, trackX, top, 3, this.valid, this.size - sc);
         PoolEntry.init(g, this.init_PoolEntry_DataUtil, this.init_PoolEntry_DataUtil.init(mx, my), this.write_Z);
      } else {
         this.init_PoolEntry_DataUtil = null;
      }

      this.init_V_GuiGraphicsExtract(g, x, toolR1, w, mx, my);
      this.init_V_GuiGraphicsExtract_2(g, ctx, this.init_BitSetUtil, x, composerY, composerFieldW, composerH);
      this.init_V_GuiGraphicsExtract(
         g, x + w - sendW, bottom - ctl, sendW, ctl, "Send", this.set, this.put, mx, my, this.init_BufferHelper.init_Z() && canWrite ? this::next : null
      );
      if (this.init_ItemStack != null && !this.init_ItemStack.isEmpty()) {
         this.init_V_GuiGraphicsExtract(g, this.init_ItemStack, this.value, this.cache);
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, ItemStack stack, int mx, int my) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc == null) {
            return;
         }

         List<Component> lines = Screen.getTooltipFromItem(mc, stack);
         if (lines == null || lines.isEmpty()) {
            return;
         }

         int tw = 0;

         for (Component c : lines) {
            tw = Math.max(tw, this.init_Font.width(c));
         }

         int th = lines.size() == 1 ? 8 : lines.size() * 10 - 2;
         int sw = cA.init_I();
         int sh = cA.load_I();
         int tx = mx + 12;
         int ty = my - 12;
         if (tx + tw + 4 > sw) {
            tx = Math.max(4, mx - tw - 16);
         }

         if (ty + th + 4 > sh) {
            ty = sh - th - 4;
         }

         if (ty < 4) {
            ty = 4;
         }

         g.nextStratum();
         PacketUtil.init_V_GuiGraphicsExtract_3(g, autismclient.gui.f.NetHelper.init(tx - 3, ty - 3, tw + 6, th + 6), -267386864, 1347420320, 0);
         int ly = ty;

         for (Component c : lines) {
            g.text(this.init_Font, c.getVisualOrderText(), tx, ly, -1, true);
            ly += 10;
         }
      } catch (Throwable var16) {
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int r1, int w, int mx, int my) {
      boolean in = this.init_BufferHelper.init_Z();
      boolean canShare = in && this.init_BufferHelper.read_Z();
      Minecraft mc = Minecraft.getInstance();
      int r2 = r1 + update_I() + 2;
      int qn = ck.init_ck().load_List().size();
      boolean hasMacros = !aN.init_aN().init_List().isEmpty();
      boolean onServer = autismclient.util.mm.BufferHelper.update_Str() != null;
      this.init_V_GuiGraphicsExtract(
         g,
         x,
         r1,
         w,
         mx,
         my,
         new String[]{qn > 0 ? "Queue " + qn : "Queue", "Macro", "GUI", "Item", "Server"},
         new boolean[]{canShare && qn > 0, canShare && hasMacros, canShare && this.read_Z(), canShare && this.write_Z(), canShare && onServer},
         new Runnable[]{this::prev, this::push, this::tick, this::run, this::put}
      );
      this.init_V_GuiGraphicsExtract(
         g,
         x,
         r2,
         w,
         mx,
         my,
         new String[]{"Pos", "TPA inv", "Filter", "Module", "Clear"},
         new boolean[]{canShare && mc.player != null, canShare && onServer, canShare, canShare, this.init_BufferHelper.push_Z()},
         new Runnable[]{this::get, this::stop, this::set, this::pop, this::add}
      );
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my, String[] labels, boolean[] enabled, Runnable[] actions) {
      int n = labels.length;
      int[] widths = new int[n];
      int total = 2 * (n - 1);

      for (int i = 0; i < n; i++) {
         widths[i] = this.load_I_Str(labels[i]);
         total += widths[i];
      }

      if (total <= w) {
         int btnX = x;

         for (int i = 0; i < n; i++) {
            this.init_V_GuiGraphicsExtract_2(g, btnX, y, widths[i], labels[i], enabled[i], actions[i], mx, my);
            btnX += widths[i] + 2;
         }
      } else {
         int gap = 2;
         int colW = (w - gap * (n - 1)) / n;

         for (int i = 0; i < n; i++) {
            this.init_V_GuiGraphicsExtract_2(g, x + i * (colW + gap), y, colW, labels[i], enabled[i], actions[i], mx, my);
         }
      }
   }

   private void push() {
      if (this.init_AutismSharePickerO == null) {
         this.init_AutismSharePickerO = new AutismSharePickerOverlay(this.init_Font);
      }

      this.init_AutismSharePickerO.init_V_Str_11("Share Macro", "Search macros...", filter -> {
         List<AutismSharePickerOverlay.Inner_DataUtil> out = new ArrayList<>();

         for (cY.Inner_DataUtil row : aN.init_aN().init_List_Str(filter)) {
            if (row.init_Z()) {
               out.add(AutismSharePickerOverlay.Inner_DataUtil.init(row, () -> aN.init_aN().init_V_Str(row.load_Str())));
            } else {
               aM macro = row.init_aM();
               out.add(AutismSharePickerOverlay.Inner_DataUtil.init(macro.init_Str, macro.init_List.size() + " steps", () -> this.init_V_aM(macro)));
            }
         }

         return out;
      });
   }

   private void pop() {
      if (this.init_AutismSharePickerO == null) {
         this.init_AutismSharePickerO = new AutismSharePickerOverlay(this.init_Font);
      }

      this.init_AutismSharePickerO.init_V_Str_11("Share Module", "Search modules...", filter -> {
         List<AutismSharePickerOverlay.Inner_DataUtil> out = new ArrayList<>();
         String f = filter.toLowerCase(Locale.ROOT);
         List<Module> mods = new ArrayList<>();

         for (Module m : al.init_Collection()) {
            if (m.showInModuleMenu() && m.settingsShareable()) {
               mods.add(m);
            }
         }

         mods.sort(Comparator.comparing(mx -> init_Str_Module(mx) + " " + mx.name(), String.CASE_INSENSITIVE_ORDER));
         String lastCat = null;

         for (Module m : mods) {
            if (f.isEmpty() || m.name().toLowerCase(Locale.ROOT).contains(f)) {
               String cat = init_Str_Module(m);
               if (!cat.equals(lastCat)) {
                  out.add(AutismSharePickerOverlay.Inner_DataUtil.init(cat));
                  lastCat = cat;
               }

               Module module = m;
               out.add(AutismSharePickerOverlay.Inner_DataUtil.init(m.name(), null, () -> this.init_V_Module(module)));
            }
         }

         return out;
      });
   }

   private static String init_Str_Module(Module m) {
      ae c = m.category();
      return c == null ? "Other" : c.load();
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, int x, int y, int w, String label, boolean enabled, Runnable action, int mx, int my) {
      this.init_V_GuiGraphicsExtract(g, x, y, w, update_I(), label, enabled ? -7429889 : this.run, this.put, mx, my, enabled ? action : null);
   }

   private boolean read_Z() {
      return Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen;
   }

   private boolean write_Z() {
      LocalPlayer p = Minecraft.getInstance().player;
      return p != null && !p.getMainHandItem().isEmpty();
   }

   private void tick() {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.init_DataUtil_DataUtil();
      if (b == null) {
         bi.init("Open a container to share its GUI.", -14249);
      } else {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void run() {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.load();
      if (b == null) {
         bi.init("Hold an item to share it.", -14249);
      } else {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void get() {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.write();
      if (b != null) {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void set() {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.read();
      if (b != null) {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void put() {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.update();
      if (b == null) {
         bi.init("Join a server first.", -14249);
      } else {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void add() {
      this.init_BufferHelper.push_V();
   }

   private void clear() {
      this.init_V_Z(false);
   }

   private void init_V_Z(boolean copyLink) {
      if (!copyLink) {
         bi.init("Opening Discord sign-in in your browser…", -13248397);
      }

      L.init(err -> {
         if (err.isEmpty()) {
            this.init_BufferHelper.write_V();
            bi.init("Signed in to matchmaking.", -13248397);
         } else if (!L.init_Z(err)) {
            bi.init(L.init_Str(err), -42149);
         }
      }, copyLink ? url -> {
         this.read(url);
         bi.init("Discord sign-in link copied.");
      } : aJ::init_V);
   }

   private void reset() {
      boolean on = !this.init_EventBus.write_Z();
      this.init_EventBus.write(on);
      bi.init(on ? "Kill switch on. Sharing off." : "Kill switch off.", on ? -14249 : -13248397);
   }

   private void start() {
      this.init_BufferHelper.close_V();
      L.load_V();
      autismclient.a.DataUtil.load_V();
      bi.init("Signed out of matchmaking.", -14249);
   }

   private void stop() {
      String name = this.init_BufferHelper.open_Str();
      if (name != null && !name.isBlank()) {
         this.init_BufferHelper.read_V_Str("/tpa " + name);
      } else {
         bi.init("Can't read your username.", -14249);
      }
   }

   private void init_V_GuiGraphicsExtract_4(
      GuiGraphicsExtractor g, autismclient.util.mm.JsonUtil line, Map<String, String> names, int x, int y, int w, int mx, int my
   ) {
      Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
      if (line.load_Z) {
         this.init_I_GuiGraphicsExtract_4(g, "• " + line.read, x, y, w, w, this.add, fontId);
      } else {
         String who = this.init_Str_JsonUtil_Map(line, names);
         int nameColor = autismclient.util.mm.BufferHelper.init_I_Str_Z(line.init_Str, line.init_Z);
         int endX = this.init_I_GuiGraphicsExtract_2(g, who, x, y, line.init_Str, line.init_Z);
         k.init(g, this.init_Font, ": ", fontId, nameColor, endX, y, false);
         int nameW = endX - x + k.init(this.init_Font, ": ", fontId, nameColor);
         int rows = this.init_I_GuiGraphicsExtract_3(g, line.read, x, y, Math.max(1, w - nameW), w, this.put, fontId, x + nameW);
         int blockH = rows * 10 + 1;
         if (!line.read.isBlank() && my >= this.state && my < this.queue && mx >= x && mx < x + w && my >= y && my < y + blockH) {
            int bw = this.load_I_Str("Copy");
            this.init_V_GuiGraphicsExtract(g, x + w - bw, y, bw, 11, "Copy", this.run, this.put, mx, my, () -> this.init_V_Str(line.read));
         }
      }
   }

   private int init_I_GuiGraphicsExtract_4(GuiGraphicsExtractor g, String text, int x, int y, int firstW, int restW, int color, Identifier fontId) {
      return this.init_I_GuiGraphicsExtract_3(g, text, x, y, firstW, restW, color, fontId, x);
   }

   private int init_I_GuiGraphicsExtract_3(GuiGraphicsExtractor g, String text, int x, int y, int firstW, int restW, int color, Identifier fontId, int firstX) {
      List<int[]> segs = this.init_List_Str(text, firstW, restW);

      for (int r = 0; r < segs.size(); r++) {
         int[] seg = segs.get(r);
         boolean first = r == 0;
         boolean overflow = seg[2] == 1;
         int rx = first ? firstX : x;
         int rw = first ? firstW : restW;
         int ry = y + r * 10;
         if (overflow) {
            this.init_V_GuiGraphicsExtract_3(g, text.substring(seg[0]), rx, ry, color, false, rw);
         } else {
            k.init(g, this.init_Font, text.substring(seg[0], seg[1]), fontId, color, rx, ry, false);
         }
      }

      return segs.size();
   }

   private List<int[]> init_List_Str(String text, int firstW, int restW) {
      Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
      String src = text == null ? "" : text;
      autismclient.gui.f.ConfigStore.Inner_StreamUtil rw = (s, e) -> k.init(this.init_Font, src.substring(s, e), fontId, this.put);
      List<int[]> rows = new ArrayList<>();
      int cursor = 0;
      int len = src.length();

      while (cursor < len && rows.size() < 3) {
         int width = Math.max(1, rows.isEmpty() ? firstW : restW);
         int nl = src.indexOf(10, cursor);
         int limit = nl >= 0 ? nl : len;
         boolean hard = false;
         int end;
         if (cursor >= limit) {
            end = cursor;
            hard = true;
         } else {
            end = autismclient.gui.f.ConfigStore.init(src, cursor, limit, width, rw);
            if (end <= cursor) {
               end = Math.min(limit, cursor + 1);
            }

            if (end >= limit && nl >= 0) {
               hard = true;
            }
         }

         int renderEnd = end;

         while (renderEnd > cursor && Character.isWhitespace(src.charAt(renderEnd - 1))) {
            renderEnd--;
         }

         rows.add(new int[]{cursor, renderEnd, 0});
         if (hard) {
            cursor = nl + 1;
         } else {
            cursor = end;

            while (cursor < len && src.charAt(cursor) == ' ') {
               cursor++;
            }
         }
      }

      if (rows.isEmpty()) {
         rows.add(new int[]{0, 0, 0});
      }

      if (cursor < len) {
         rows.get(rows.size() - 1)[2] = 1;
      }

      return rows;
   }

   private int[] init_IArr_List(List<autismclient.util.mm.JsonUtil> lines, int logW, Map<String, String> names) {
      int[] h = new int[lines.size()];

      for (int i = 0; i < lines.size(); i++) {
         h[i] = this.init_I_JsonUtil(lines.get(i), logW, names);
      }

      return h;
   }

   private int init_I_JsonUtil(autismclient.util.mm.JsonUtil line, int logW, Map<String, String> names) {
      return line.init_Z() ? 28 : this.load(line, logW, names) * 10 + 1;
   }

   private int load(autismclient.util.mm.JsonUtil line, int logW, Map<String, String> names) {
      if (logW <= 0) {
         return 1;
      }

      if (line.load_Z) {
         String t = "• " + line.read;
         return t.isEmpty() ? 1 : this.init_List_Str(t, logW, logW).size();
      }

      if (line.read.isEmpty()) {
         return 1;
      }

      String who = this.init_Str_JsonUtil_Map(line, names);
      int nameW = k.init(
         this.init_Font,
         who + ": ",
         this.init_CharEncoder.init_Identifier_l(l.read),
         autismclient.util.mm.BufferHelper.init_I_Str_Z(line.init_Str, line.init_Z)
      );
      return this.init_List_Str(line.read, Math.max(1, logW - nameW), logW).size();
   }

   private static int init_I_IArr(int[] a) {
      int s = 0;

      for (int v : a) {
         s += v;
      }

      return s;
   }

   private void init_V_Str(String text) {
      this.read(text);
      bi.init("Copied to clipboard.");
   }

   private String init_Str_JsonUtil_Map(autismclient.util.mm.JsonUtil line, Map<String, String> names) {
      return line.init_Z
         ? names.getOrDefault(this.init_BufferHelper.read_Str(), this.init_BufferHelper.poll_Str()) + " (you)"
         : names.getOrDefault(line.init_Str, this.init_BufferHelper.init_Str_Str(line.init_Str));
   }

   private void load(GuiGraphicsExtractor g, autismclient.util.mm.JsonUtil line, Map<String, String> names, int x, int y, int w, int mx, int my) {
      boolean tpa = this.init_Z_JsonUtil(line);
      int accent = this.init_I_JsonUtil(line);
      autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, 26));
      ItemStack icon = line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.sync && "item".equals(line.init_DataUtil_DataUtil.init_Str)
         ? this.init_ItemStack_DataUtil_DataUtil(line.init_DataUtil_DataUtil)
         : ItemStack.EMPTY;
      int iconY = y + 5;
      int line1 = y + 3;
      int line2 = y + 3 + 10 + 1;
      if (icon != null && !icon.isEmpty()) {
         g.item(icon, x + 5, iconY);
         if (mx >= x + 5 && mx < x + 21 && my >= iconY && my < iconY + 16 && my >= this.state && my < this.queue) {
            this.init_ItemStack = icon;
            this.value = mx;
            this.cache = my;
         }
      } else {
         this.init_V_GuiGraphicsExtract_3(g, this.init_Str_JsonUtil_Z(line, tpa), x + 6, line1, accent, false, 44);
      }

      if (line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.sync && "server".equals(line.init_DataUtil_DataUtil.init_Str)) {
         this.init_V_GuiGraphicsExtract_3(
            g,
            autismclient.util.mm.ConfigStore.init_Str(autismclient.util.mm.ConfigStore.read_Str(line.init_DataUtil_DataUtil)),
            x + 54,
            line1,
            this.put,
            false,
            w - 150
         );
         this.init_V_GuiGraphicsExtract_3(g, this.init_Str_DataUtil_DataUtil(line.init_DataUtil_DataUtil), x + 54, line2, this.add, false, w - 150);
      } else {
         this.init_V_GuiGraphicsExtract_3(g, this.load_Str_JsonUtil_Z(line, tpa), x + 54, line1, this.put, false, w - 64);
         this.init_V_GuiGraphicsExtract_3(
            g,
            line.init_Z ? "you" : "from " + names.getOrDefault(line.init_Str, this.init_BufferHelper.init_Str_Str(line.init_Str)),
            x + 54,
            line2,
            this.add,
            false,
            w - 160
         );
      }

      int rightX = x + w - 6;
      rightX = this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Copy", this.run, mx, my, () -> this.init_V_JsonUtil(line));
      switch (line.init_JsonUtil_DataUtil) {
         case read:
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Inspect", -7429889, mx, my, () -> this.load_V_aM(o.init_aM(line.init_DataUtil_NetHelper.read)));
            break;
         case update:
            if (tpa) {
               this.init_I_GuiGraphicsExtract_2(
                  g, rightX, y, "Accept", this.set, mx, my, () -> this.init_BufferHelper.init_V_DataUtil_CachePool(line.init_DataUtil_CachePool)
               );
            } else {
               rightX = this.init_I_GuiGraphicsExtract_2(
                  g, rightX, y, "Fill", this.run, mx, my, () -> this.init_V_DataUtil_CachePool(line.init_DataUtil_CachePool)
               );
               this.init_I_GuiGraphicsExtract_2(
                  g, rightX, y, "Execute", this.set, mx, my, () -> this.init_BufferHelper.init_V_DataUtil_CachePool(line.init_DataUtil_CachePool)
               );
            }
            break;
         case write:
            rightX = this.init_I_GuiGraphicsExtract_2(
               g,
               rightX,
               y,
               "Inspect",
               -7429889,
               mx,
               my,
               () -> this.init_V_AutismPacketLogger(ByteReader.init_AutismPacketLogger(line.init_DataUtil_StateCach))
            );
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Add to queue", this.set, mx, my, () -> this.init_V_DataUtil_StateCach(line.init_DataUtil_StateCach));
            break;
         case sync:
            this.init_V_GuiGraphicsExtract_3(g, line.init_DataUtil_DataUtil, rightX, y, mx, my);
      }
   }

   private boolean init_Z_JsonUtil(autismclient.util.mm.JsonUtil line) {
      return line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.update
         && line.init_DataUtil_CachePool != null
         && line.init_DataUtil_CachePool.write == 0
         && line.init_DataUtil_CachePool.init_Str != null
         && line.init_DataUtil_CachePool.init_Str.toLowerCase(Locale.ROOT).startsWith("tpa ");
   }

   private int init_I_JsonUtil(autismclient.util.mm.JsonUtil line) {
      if (line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.sync) {
         int var4;
         switch (line.init_DataUtil_DataUtil.init_Str) {
            case "item":
            case "gui":
            case "steps":
               var4 = this.set;
               break;
            case "filter":
               var4 = -14249;
               break;
            default:
               var4 = -7429889;
         }

         return var4;
      } else {
         int var10000;
         switch (line.init_JsonUtil_DataUtil) {
            case read:
               var10000 = this.set;
               break;
            case update:
               var10000 = -14249;
               break;
            default:
               var10000 = -7429889;
         }

         return var10000;
      }
   }

   private String init_Str_JsonUtil_Z(autismclient.util.mm.JsonUtil line, boolean tpa) {
      if (line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.sync) {
         String var5;
         switch (line.init_DataUtil_DataUtil.init_Str) {
            case "gui":
               var5 = "GUI";
               break;
            case "item":
               var5 = "ITEM";
               break;
            case "filter":
               var5 = "FILTER";
               break;
            case "position":
               var5 = "POS";
               break;
            case "server":
               var5 = "SERVER";
               break;
            case "steps":
               var5 = "STEPS";
               break;
            case "module":
               var5 = "MODULE";
               break;
            default:
               var5 = "SHARE";
         }

         return var5;
      } else {
         String var10000;
         switch (line.init_JsonUtil_DataUtil) {
            case read:
               var10000 = "MACRO";
               break;
            case update:
               var10000 = tpa ? "TPA" : "CMD";
               break;
            case write:
               var10000 = "QUEUE";
               break;
            default:
               var10000 = "SHARE";
         }

         return var10000;
      }
   }

   private String load_Str_JsonUtil_Z(autismclient.util.mm.JsonUtil line, boolean tpa) {
      if (line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.update) {
         String rendered = autismclient.util.mm.BufferHelper.init_Str_DataUtil_CachePool(line.init_DataUtil_CachePool);
         return tpa ? "TPA invite → " + rendered : rendered;
      }

      if (line.init_JsonUtil_DataUtil == autismclient.util.mm.JsonUtil.Inner_DataUtil.sync) {
         String var10000;
         switch (line.init_DataUtil_DataUtil.init_Str) {
            case "gui":
               var10000 = line.init_DataUtil_DataUtil.load + "  (" + line.init_DataUtil_DataUtil.init_I + " slots)";
               break;
            case "filter":
               var10000 = "Packet Filter  (" + line.init_DataUtil_DataUtil.init_I + " types)";
               break;
            case "module":
               var10000 = line.init_DataUtil_DataUtil.load + "  (" + line.init_DataUtil_DataUtil.init_I + " settings)";
               break;
            default:
               var10000 = line.init_DataUtil_DataUtil.load;
         }

         return var10000;
      } else {
         return line.init_Str();
      }
   }

   private String init_Str_DataUtil_DataUtil(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      int players = autismclient.util.mm.ConfigStore.read_I(b);
      int max = autismclient.util.mm.ConfigStore.write_I(b);
      int ping = autismclient.util.mm.ConfigStore.update_I(b);
      StringBuilder sb = new StringBuilder();
      sb.append(players);
      if (max > 0) {
         sb.append("/").append(max);
      }

      sb.append(" online");
      if (ping >= 0) {
         sb.append("  ·  ").append(ping).append("ms");
      }

      return sb.toString();
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor g, autismclient.util.mm.b.DataUtil.Inner_DataUtil b, int rightX, int y, int mx, int my) {
      switch (b.init_Str) {
         case "gui":
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "View", this.set, mx, my, () -> this.update(b));
            break;
         case "item":
            this.init_I_GuiGraphicsExtract_2(
               g, rightX, y, "Inspect", this.set, mx, my, () -> this.init_V_ItemStack(autismclient.util.mm.ConfigStore.init_ItemStack(b))
            );
            break;
         case "filter":
            rightX = this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Inspect", -7429889, mx, my, () -> this.sync(b));
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Import", this.set, mx, my, () -> {
               int n = autismclient.util.mm.ConfigStore.init_I(b);
               bi.init("Imported " + n + " filtered packet(s).", this.set);
            });
            break;
         case "server":
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Join", this.set, mx, my, () -> this.write(b));
            break;
         case "steps":
            rightX = this.init_I_GuiGraphicsExtract_2(g, rightX, y, "To editor", -7429889, mx, my, () -> this.load_V_aM(o.init_aM(b.read)));
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Import", this.set, mx, my, () -> this.init_V_DataUtil_DataUtil(b));
            break;
         case "module":
            rightX = this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Preview", -7429889, mx, my, () -> this.load_V_DataUtil_DataUtil(b));
            this.init_I_GuiGraphicsExtract_2(g, rightX, y, "Apply", this.set, mx, my, () -> this.read(b));
      }
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor g, int rightX, int rowY, String label, int border, int mx, int my, Runnable action) {
      int bw = this.load_I_Str(label);
      int bx = rightX - bw;
      int h = update_I();
      this.init_V_GuiGraphicsExtract(g, bx, rowY + 26 - 2 - h, bw, h, label, border, this.put, mx, my, action);
      return bx - 2;
   }

   private void next() {
      if (!this.init_BufferHelper.init_Z()) {
         bi.init("Join a lobby first.", -14249);
      } else {
         String text = this.init_BitSetUtil.init_Str().trim();
         if (!text.isEmpty()) {
            String type = text.length() >= 24 ? o.init_Str(text) : null;
            if ("autism_macro_steps".equals(type)) {
               autismclient.util.mm.b.DataUtil.Inner_DataUtil steps = this.init_DataUtil_DataUtil_Str(text);
               if (steps != null) {
                  this.init_BufferHelper.init_Z_DataUtil_DataUtil(steps);
                  this.init_BitSetUtil.init_BitSetUtil_Str("");
                  return;
               }
            } else if (type != null && type.startsWith("autism_macro")) {
               aM macro = o.init_aM(text);
               autismclient.util.mm.b.DataUtil.NetHelper offer = ByteReader.init(macro);
               if (offer != null) {
                  this.init_BufferHelper.init_V_DataUtil_NetHelper(offer);
                  this.init_BitSetUtil.init_BitSetUtil_Str("");
                  return;
               }
            } else if ("packets".equals(type)) {
               List<ck.Inner_CachePool> q = o.init_List(text);
               int count = q == null ? 0 : q.size();
               autismclient.util.mm.b.DataUtil.StateCache offer = new autismclient.util.mm.b.DataUtil.StateCache();
               offer.init = count <= 0 ? "shared packets" : count + (count == 1 ? " packet" : " packets");
               offer.read = text;
               this.init_BufferHelper.init_V_DataUtil_StateCach(offer);
               this.init_BitSetUtil.init_BitSetUtil_Str("");
               return;
            }

            autismclient.util.mm.b.DataUtil.Inner_DataUtil blob = text.length() >= 24 ? autismclient.util.mm.ConfigStore.init_DataUtil_DataUtil(text) : null;
            if (blob != null) {
               this.init_BufferHelper.init_Z_DataUtil_DataUtil(blob);
               this.init_BitSetUtil.init_BitSetUtil_Str("");
            } else if (!text.startsWith("/") && !autismclient.commands.DataUtil.init_Z(text)) {
               this.init_BufferHelper.load_V_Str(text);
               this.init_BitSetUtil.init_BitSetUtil_Str("");
            } else {
               this.init_BufferHelper.read_V_Str(text);
               this.init_BitSetUtil.init_BitSetUtil_Str("");
            }
         }
      }
   }

   private void prev() {
      List<ck.Inner_CachePool> q = ck.init_ck().load_List();
      autismclient.util.mm.b.DataUtil.StateCache offer = ByteReader.init(q, q.size() + (q.size() == 1 ? " packet" : " packets"));
      if (offer == null) {
         bi.init("Your packet queue is empty.", -14249);
      } else {
         this.init_BufferHelper.init_V_DataUtil_StateCach(offer);
      }
   }

   private void init_V_aM(aM macro) {
      autismclient.util.mm.b.DataUtil.NetHelper offer = ByteReader.init(macro);
      if (offer != null) {
         this.init_BufferHelper.init_V_DataUtil_NetHelper(offer);
      }
   }

   private autismclient.util.mm.b.DataUtil.Inner_DataUtil init_DataUtil_DataUtil_Str(String stepsHash) {
      aM macro = o.init_aM(stepsHash);
      if (macro != null && macro.init_List != null && !macro.init_List.isEmpty()) {
         int count = macro.init_List.size();
         String label = count == 1 ? (macro.init_List.get(0) == null ? "1 step" : macro.init_List.get(0).getDisplayName()) : count + " macro steps";
         return new autismclient.util.mm.b.DataUtil.Inner_DataUtil("steps", label, count, stepsHash);
      } else {
         return null;
      }
   }

   private void init_V_Module(Module m) {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.ConfigStore.init(m);
      if (b == null) {
         bi.init("Could not capture module settings.", this.clear);
      } else {
         this.init_BufferHelper.init_Z_DataUtil_DataUtil(b);
      }
   }

   private void init_V_DataUtil_DataUtil(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      autismclient.util.mm.b.DataUtil.NetHelper offer = new autismclient.util.mm.b.DataUtil.NetHelper();
      offer.read = b.read;
      offer.init_Str = b.load;
      ByteReader.init(offer);
   }

   private void load_V_DataUtil_DataUtil(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (this.init_AutismModuleViewOv == null) {
         this.init_AutismModuleViewOv = new AutismModuleViewOverlay(this.init_Font);
      }

      if (this.init_AutismModuleViewOv.init(b)) {
         this.init_V_cX_Z(this.init_AutismModuleViewOv, true);
      }
   }

   private void read(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      int n = autismclient.util.mm.ConfigStore.load_I(b);
      if (n < 0) {
         bi.init("Can't apply. Bad data.", this.clear);
      } else {
         bi.init("Applied " + n + " setting(s) to " + autismclient.util.mm.ConfigStore.init_Str(b) + ".", this.set);
      }
   }

   private void init_V_DataUtil_StateCach(autismclient.util.mm.b.DataUtil.StateCache offer) {
      int n = ByteReader.init_I(offer);
      if (n < 0) {
         bi.init("Could not read shared queue.", this.clear);
      } else {
         bi.init("Added " + n + " packet(s) to your queue.", this.set);
      }
   }

   private void init_V_JsonUtil(autismclient.util.mm.JsonUtil line) {
      this.read(autismclient.util.mm.TaskQueue.init_Str(line));
      bi.init("Copied to clipboard.");
   }

   private void init_V_DataUtil_CachePool(autismclient.util.mm.b.DataUtil.CachePool c) {
      String text = autismclient.util.mm.BufferHelper.init_Str_DataUtil_CachePool(c);
      Minecraft.getInstance().gui.setScreen(new ChatScreen(text, false));
   }

   private void write(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      autismclient.util.mm.TaskQueue.init(
         autismclient.util.mm.ConfigStore.read_Str(b), autismclient.util.mm.ConfigStore.load_Str(b), this.init_DataUtil_BufferHel.init_Screen()
      );
   }

   private void read(GuiGraphicsExtractor g, int x, int top, int w, int bottom, int mx, int my) {
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(x, top, w, Math.max(0, bottom - top)));
      this.buffer = top;
      this.entry = bottom;

      int y;
      try {
         y = this.init_I_GuiGraphicsExtract(g, x, top, w, bottom, mx, my);
      } finally {
         this.buffer = -2147483648;
         this.entry = 2147483647;
         HashUtil.init_HashUtil().init(g);
      }

      this.init_V_DataUtil_JsonUtil_I(DataUtil.JsonUtil.read, y);
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int top, int w, int bottom, int mx, int my) {
      int y = top - this.init_I_DataUtil_JsonUtil(DataUtil.JsonUtil.read);
      String code = this.init_BufferHelper.init_Str();
      if (!code.isBlank()) {
         autismclient.util.mm.DataUtil clb = this.init_BufferHelper.init_DataUtil();
         boolean pub = clb != null && clb.init_Z;
         int rowH = open_I();
         autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, rowH));
         String codeLabel = pub ? "Room Code:" : "Room Key:";
         int codeX = x + 6 + k.init(this.init_Font, codeLabel, this.init_CharEncoder.init_Identifier_l(l.read), this.add) + 4;
         this.init_V_GuiGraphicsExtract_3(g, codeLabel, x + 6, this.init_I_I_I(y, rowH), this.add, false, codeX - x - 6);
         int copyW = this.load_I_Str("Copy");
         int copyX = x + w - 1 - copyW;
         this.init_V_GuiGraphicsExtract_3(g, code, codeX, this.init_I_I_I(y, rowH), this.set, false, Math.max(1, copyX - 4 - codeX));
         this.init_V_GuiGraphicsExtract(g, copyX, y + 1, copyW, rowH - 2, "Copy", this.set, this.put, mx, my, () -> {
            this.read(code);
            bi.init(pub ? "Room code copied." : "Room key copied.");
         });
         y += rowH + 4;
      }

      int groupRow = close_I();
      this.init_V_GuiGraphicsExtract_3(g, "Members", x, this.init_I_I_I(y, groupRow), this.put, false, w);
      y += groupRow + 2;
      List<RenderState> peers = this.init_BufferHelper.load_List();
      Map<String, String> names = this.init_Map_List(peers);
      autismclient.util.mm.DataUtil lb = this.init_BufferHelper.init_DataUtil();
      long now = System.currentTimeMillis();
      String selfLabel = names.getOrDefault(this.init_BufferHelper.read_Str(), this.init_BufferHelper.poll_Str());
      boolean selfHost = this.init_BufferHelper.load_Z();
      String selfServerLine = this.init_Str();
      String selfWorldLine = this.load_Str();
      String selfPosLine = this.read_Str();
      int selfExtra = (selfServerLine.isEmpty() ? 0 : 1) + (selfWorldLine.isEmpty() ? 0 : 1) + (selfPosLine.isEmpty() ? 0 : 1);
      int selfH = (1 + selfExtra) * 10 + 6;
      autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, selfH));
      String selfRole = lb != null && lb.load_Z ? (L.read_Z() ? "  [Admin]" : (this.init_BufferHelper.read_Z() ? "  [Speaker]" : "")) : "";
      int selfEndX = this.init_I_GuiGraphicsExtract_2(g, selfLabel, x + 6, y + 3, this.init_BufferHelper.read_Str(), true);
      this.init_V_GuiGraphicsExtract_3(
         g, "  (you)" + (selfHost ? "  (host)" : "") + selfRole, selfEndX, y + 3, this.add, false, Math.max(1, x + w - 6 - selfEndX)
      );
      int sly = y + 3 + 10;
      if (!selfServerLine.isEmpty()) {
         this.init_V_GuiGraphicsExtract_3(g, selfServerLine, x + 6, sly, -7429889, false, w - 12);
         sly += 10;
      }

      if (!selfWorldLine.isEmpty()) {
         this.init_V_GuiGraphicsExtract_3(g, selfWorldLine, x + 6, sly, -7429889, false, w - 12);
         sly += 10;
      }

      if (!selfPosLine.isEmpty()) {
         this.init_V_GuiGraphicsExtract_3(g, selfPosLine, x + 6, sly, -7429889, false, w - 12);
      }

      y += selfH + 2;
      if (peers.isEmpty()) {
         int textRow = flush_I();
         this.init_V_GuiGraphicsExtract_3(g, "No other members yet.", x + 4, this.init_I_I_I(y, textRow), this.add, false, w);
         y += textRow;
      }

      for (RenderState p : peers) {
         String serverLine = p.init_Z && !p.update_Str.isBlank() ? "Server: " + autismclient.util.mm.ConfigStore.init_Str(p.update_Str) : "";
         String worldLine = p.load_Z ? "world: " + load_Str_Str(p.sync) : "";
         String posLine = p.load_Z ? "position: " + (int)p.init_D + " " + (int)p.load_D + " " + (int)p.read_D : "";
         int extraLines = (serverLine.isEmpty() ? 0 : 1) + (worldLine.isEmpty() ? 0 : 1) + (posLine.isEmpty() ? 0 : 1);
         int rowH = (1 + extraLines) * 10 + 6;
         boolean visible = y + rowH > top && y < bottom;
         if (visible) {
            boolean isHost = p.init_Str.equals(this.init_BufferHelper.write_Str());
            autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, rowH));
            int dot = p.init_Z_J(now) ? this.set : this.add;
            PacketUtil.init_V_GuiGraphicsExtract(g, x + 9.0F, y + 8.0F, 3.0F, dot);
            String label = names.getOrDefault(p.init_Str, p.init());
            String suffix = (isHost ? "  (host)" : "")
               + (lb != null && lb.load_Z ? (p.read_Z ? "  [Admin]" : (p.write_Z ? "  [Speaker]" : "")) : "")
               + (p.update_Z ? "  (muted)" : "");
            int endX = this.init_I_GuiGraphicsExtract_2(g, label, x + 18, y + 3, p.init_Str, false);
            if (!suffix.isEmpty()) {
               this.init_V_GuiGraphicsExtract_3(g, suffix, endX, y + 3, this.add, false, Math.max(1, x + w - 6 - endX));
            }

            int ly = y + 3 + 10;
            if (!serverLine.isEmpty()) {
               this.init_V_GuiGraphicsExtract_3(g, serverLine, x + 18, ly, -7429889, false, w - 18 - 6);
               ly += 10;
            }

            if (!worldLine.isEmpty()) {
               this.init_V_GuiGraphicsExtract_3(g, worldLine, x + 18, ly, -7429889, false, w - 18 - 6);
               ly += 10;
            }

            if (!posLine.isEmpty()) {
               this.init_V_GuiGraphicsExtract_3(g, posLine, x + 18, ly, -7429889, false, w - 18 - 6);
            }

            String fpHex = p.init_Str;
            this.read_List.add(new DataUtil.NetHelper(x, y, w, rowH, fpHex, this.buffer, this.entry));
            int kx = x + w - 16;
            int kH = Math.min(rowH, 16);
            int menuY = y + kH - 1;
            boolean kHover = mx >= kx - 2 && mx < x + w && my >= y && my < y + kH;
            this.init_V_GuiGraphicsExtract_4(g, "⋮", kx + 3, y + 3, kHover ? this.put : this.add, false);
            this.init_V_I(kx - 2, y, 18, kH, () -> this.init_V_Str(fpHex, kx - 2, menuY));
         }

         y += rowH + 2;
      }

      return y;
   }

   private String init_Str() {
      if (!this.init_BufferHelper.sync_Z()) {
         return "";
      }

      ServerData sd = Minecraft.getInstance().getCurrentServer();
      return sd != null && sd.ip != null && !sd.ip.isBlank() ? "Server: " + autismclient.util.mm.ConfigStore.init_Str(sd.ip) : "";
   }

   private String load_Str() {
      if (!this.init_BufferHelper.flush_Z()) {
         return "";
      }

      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.player.level() != null ? "world: " + load_Str_Str(mc.player.level().dimension().identifier().toString()) : "";
   }

   private String read_Str() {
      if (!this.init_BufferHelper.flush_Z()) {
         return "";
      }

      Minecraft mc = Minecraft.getInstance();
      return mc.player == null ? "" : "position: " + (int)mc.player.getX() + " " + (int)mc.player.getY() + " " + (int)mc.player.getZ();
   }

   private Map<String, String> init_Map_List(List<RenderState> peers) {
      String self = this.init_BufferHelper.poll_Str();
      long rosterVersion = this.init_BufferHelper.load_J();
      if (rosterVersion == this.init_J && peers.size() == this.count && self.equals(this.init_Str)) {
         return this.load_Map;
      }

      List<String[]> order = new ArrayList<>();
      order.add(new String[]{this.init_BufferHelper.read_Str(), self});

      for (RenderState p : peers) {
         order.add(new String[]{p.init_Str, p.init()});
      }

      Map<String, Integer> seen = new HashMap<>();
      Map<String, String> out = new HashMap<>();

      for (String[] e : order) {
         String key = e[1].toLowerCase(Locale.ROOT);
         int count = seen.merge(key, 1, Integer::sum);
         out.put(e[0], count == 1 ? e[1] : e[1] + " (" + (count - 1) + ")");
      }

      this.init_J = rosterVersion;
      this.count = peers.size();
      this.init_Str = self;
      this.load_Map = Map.copyOf(out);
      return this.load_Map;
   }

   private void init_V_RenderState(RenderState peer) {
      String name = peer.write_Str != null && !peer.write_Str.isBlank() ? peer.write_Str : peer.init();
      autismclient.util.mm.TaskQueue.init(peer.update_Str, name, this.init_DataUtil_BufferHel.init_Screen());
   }

   private void load_V_Str(String name) {
      Minecraft.getInstance().gui.setScreen(new ChatScreen("/pay " + name + " ", false));
   }

   private static String load_Str_Str(String dim) {
      if (dim == null) {
         return "?";
      }

      int i = dim.indexOf(58);
      return i >= 0 ? dim.substring(i + 1) : dim;
   }

   private void read(GuiGraphicsExtractor g, SyncManager ctx, int x, int top, int w, int bottom, int mx, int my) {
      HashUtil.init_HashUtil().init(g, autismclient.gui.f.NetHelper.init(x, top, w, Math.max(0, bottom - top)));
      this.buffer = top;
      this.entry = bottom;

      try {
         int y = top - this.init_I_DataUtil_JsonUtil(DataUtil.JsonUtil.write);
         y = this.init_I_GuiGraphicsExtract_2(
            g, x, y, w, "Share my server", this.init_EventBus.init_Z(), mx, my, () -> this.init_EventBus.init_V_Z(!this.init_EventBus.init_Z())
         );
         y = this.init_I_GuiGraphicsExtract_2(
            g, x, y, w, "Share my position", this.init_EventBus.load_Z(), mx, my, () -> this.init_EventBus.load(!this.init_EventBus.load_Z())
         );
         y = this.init_I_GuiGraphicsExtract_2(g, x, y, w, "Kill switch (block sharing)", this.init_EventBus.write_Z(), mx, my, this::reset);
         y = this.init_I_GuiGraphicsExtract_2(
            g, x, y, w, "Debug logging (console)", this.init_EventBus.sync_Z(), mx, my, () -> this.init_EventBus.sync(!this.init_EventBus.sync_Z())
         );
         y += 4;
         int groupRow = close_I();
         this.init_V_GuiGraphicsExtract_3(g, "Public Settings", x, this.init_I_I_I(y, groupRow), this.put, false, w);
         y += groupRow + 2;
         y = this.init_I_GuiGraphicsExtract_2(
            g, x, y, w, "Auto-join public lobby", this.init_EventBus.read_Z(), mx, my, () -> this.init_EventBus.read(!this.init_EventBus.read_Z())
         );
         autismclient.util.mm.DataUtil olb = this.init_BufferHelper.update_Z() ? this.init_BufferHelper.init_DataUtil() : null;
         if (olb != null) {
            String olId = olb.init_Str;
            y = this.init_I_GuiGraphicsExtract_2(
               g,
               x,
               y,
               w,
               "Share my server here",
               this.init_EventBus.init_Z_Str(olId),
               mx,
               my,
               () -> this.init_EventBus.init_V_Str_Z(olId, !this.init_EventBus.init_Z_Str(olId))
            );
            y = this.init_I_GuiGraphicsExtract_2(
               g,
               x,
               y,
               w,
               "Share my position here",
               this.init_EventBus.load_Z(olId),
               mx,
               my,
               () -> this.init_EventBus.load_V_Str_Z(olId, !this.init_EventBus.load_Z(olId))
            );
         }

         int accH = open_I();
         autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, accH));
         int signOutW = this.load_I_Str("Sign out");
         this.init_V_GuiGraphicsExtract_3(
            g, "Signed in: " + this.init_BufferHelper.poll_Str(), x + 6, this.init_I_I_I(y, accH), this.add, false, w - 6 - 1 - signOutW - 4
         );
         this.init_V_GuiGraphicsExtract(g, x + w - 1 - signOutW, y + 1, signOutW, accH - 2, "Sign out", this.clear, this.put, mx, my, this::start);
         y += accH + 2;
         y = this.init_I_GuiGraphicsExtract(g, x, y, w, mx, my, "Blocked", this.init_EventBus.init_Map(), true);
         y = this.init_I_GuiGraphicsExtract(g, x, y, w, mx, my, "Banned (your lobbies)", this.init_EventBus.load_Map(), false);
         this.init_V_DataUtil_JsonUtil_I(DataUtil.JsonUtil.write, y);
      } finally {
         this.buffer = -2147483648;
         this.entry = 2147483647;
         HashUtil.init_HashUtil().init(g);
      }
   }

   private int init_I_GuiGraphicsExtract(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my, String title, Map<String, String> entries, boolean block) {
      y += 4;
      int groupRow = close_I();
      this.init_V_GuiGraphicsExtract_3(g, title, x, this.init_I_I_I(y, groupRow), this.put, false, w);
      y += groupRow + 2;
      if (entries.isEmpty()) {
         int textRow = flush_I();
         this.init_V_GuiGraphicsExtract_3(g, "None.", x + 6, this.init_I_I_I(y, textRow), this.add, false, w);
         return y + textRow;
      }

      int rowH = open_I();

      for (Entry<String, String> e : entries.entrySet()) {
         String id = e.getKey();
         String label = e.getValue() != null && !e.getValue().isBlank() ? e.getValue() : "Discord member";
         autismclient.gui.f.b.BufferHelper.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, rowH));
         String modLabel = block ? "Unblock" : "Unban";
         int modBtnW = this.load_I_Str(modLabel);
         this.init_V_GuiGraphicsExtract_3(g, label, x + 6, this.init_I_I_I(y, rowH), this.put, false, w - 6 - 1 - modBtnW - 4);
         this.init_V_GuiGraphicsExtract(g, x + w - 1 - modBtnW, y + 1, modBtnW, rowH - 2, modLabel, this.set, this.put, mx, my, () -> {
            if (block) {
               this.init_BufferHelper.open(id);
            } else {
               this.init_BufferHelper.push(id);
            }
         });
         y += rowH + 2;
      }

      return y;
   }

   private void init_V_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry == null) {
         bi.init("Could not rebuild packet to inspect.", this.clear);
      } else {
         if (this.init_AutismPacketInspec == null) {
            this.init_AutismPacketInspec = new AutismPacketInspectOverlay(this.init_Font);
         }

         this.init_AutismPacketInspec.init_V_AutismPacketLogger(entry, 60, 60);
         this.init_V_cX_Z(this.init_AutismPacketInspec, true);
      }
   }

   private void init_V_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         AutismItemNbtInspectOverlay ov = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(this.init_Font);
         if (ov != null) {
            ov.init_V_ItemStack(stack, 60, 60);
            this.init_V_cX_Z(ov, true);
         }
      } else {
         bi.init("Could not read shared item.", this.clear);
      }
   }

   private void update(autismclient.util.mm.b.DataUtil.Inner_DataUtil blob) {
      if (this.init_AutismGuiViewOverl == null) {
         this.init_AutismGuiViewOverl = new AutismGuiViewOverlay(this.init_Font);
      }

      if (this.init_AutismGuiViewOverl.init(blob)) {
         this.init_V_cX_Z(this.init_AutismGuiViewOverl, true);
      }
   }

   private void sync(autismclient.util.mm.b.DataUtil.Inner_DataUtil blob) {
      if (this.init_AutismFilterViewOv == null) {
         this.init_AutismFilterViewOv = new AutismFilterViewOverlay(this.init_Font);
      }

      if (this.init_AutismFilterViewOv.init(blob)) {
         this.init_V_cX_Z(this.init_AutismFilterViewOv, true);
      }
   }

   private void load_V_aM(aM macro) {
      if (macro == null) {
         bi.init("Could not read shared macro.", this.clear);
      } else {
         AutismMacroEditorOverlay ed = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
         if (ed != null) {
            ed.load_V_aM(macro);
            this.init_V_cX_Z(ed, false);
         }
      }
   }

   private void init_V_cX_Z(cX ov, boolean background) {
      if (ov != null) {
         if (background) {
            bk.init_bk().init_V_cX_cX_DataUtil(ov, cX.Inner_DataUtil.write);
         } else {
            bk.init_bk().init_V(ov);
         }

         bk.init_bk().update_V(ov);
      }
   }

   private void read(String s) {
      Minecraft.getInstance().keyboardHandler.setClipboard(s == null ? "" : s);
   }

   public boolean init_Z_I_2(int mx, int my, int button) {
      this.f_a_I = mx;
      this.f_b_I = my;
      if (this.update_Z && button == 0) {
         for (DataUtil.StreamUtil it : this.write_List) {
            if (it.init(mx, my)) {
               try {
                  it.init_Runnable.run();
               } catch (Throwable t) {
                  bi.init("Action failed: " + t.getClass().getSimpleName(), this.clear);
               }

               if (it.init_Z) {
                  this.valid();
               }

               return true;
            }
         }

         this.valid();
         return true;
      } else if (button == 1) {
         for (DataUtil.NetHelper r : this.read_List) {
            if (r.init(mx, my)) {
               this.init_V_Str(r.init_Str(), mx, my);
               return true;
            }
         }

         if (this.update_Z) {
            this.valid();
            return true;
         } else {
            return mx >= this.f_c_I && mx < this.f_c_I + this.f_e_I && my >= this.f_d_I && my < this.f_d_I + this.f_f_I;
         }
      } else {
         if (button == 0
            && this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.load
            && this.init_PoolEntry_DataUtil != null
            && this.init_PoolEntry_DataUtil.init(mx, my)) {
            this.write_Z = true;
            this.data = my - this.init_PoolEntry_DataUtil.update();
            this.read_V();
            return true;
         }

         SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);

         for (BitSetUtil in : this.load_List) {
            if (in.init_Z_SyncManager_2(ctx, mx, my, button)) {
               for (BitSetUtil other : this.load_List) {
                  if (other != in) {
                     other.init_V_Z(false);
                  }
               }

               return true;
            }
         }

         this.read_V();
         if (button == 0) {
            for (DataUtil.ConfigStore h : this.init_List) {
               if (h.init(mx, my)) {
                  try {
                     h.init_Runnable.run();
                  } catch (Throwable t) {
                     bi.init("Action failed: " + t.getClass().getSimpleName(), this.clear);
                  }

                  return true;
               }
            }
         }

         return mx >= this.f_c_I && mx < this.f_c_I + this.f_e_I && my >= this.f_d_I && my < this.f_d_I + this.f_f_I;
      }
   }

   public boolean load(int mx, int my, int button) {
      if (this.write_Z) {
         this.write_Z = false;
         return true;
      }

      SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);
      boolean any = false;

      for (BitSetUtil in : this.load_List) {
         any |= in.load_Z_SyncManager(ctx, mx, my, button);
      }

      return any;
   }

   public boolean init_Z_I(int mx, int my, int button, double dx, double dy) {
      if (this.write_Z && this.init_PoolEntry_DataUtil != null) {
         int topVal = PoolEntry.init(this.init_PoolEntry_DataUtil, my, this.data);
         this.init_IArr[DataUtil.JsonUtil.load.ordinal()] = Math.max(0, Math.min(this.size, this.size - topVal));
         return true;
      }

      SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);
      boolean any = false;

      for (BitSetUtil in : this.load_List) {
         any |= in.init_Z_SyncManager(ctx, mx, my, button, (float)dx, (float)dy);
      }

      return any;
   }

   public boolean init_Z_I(int mx, int my, double amount) {
      if (this.update_Z) {
         this.valid();
      }

      if (this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.load) {
         int max = this.size;
         this.init_IArr[DataUtil.JsonUtil.load.ordinal()] = Math.max(
            0, Math.min(max, this.init_IArr[DataUtil.JsonUtil.load.ordinal()] + (int)Math.signum(amount) * 18)
         );
         return true;
      }

      int max = this.load_I_DataUtil_JsonUtil(this.init_DataUtil_JsonUtil);
      if (max <= 0) {
         return false;
      }

      this.init_IArr[this.init_DataUtil_JsonUtil.ordinal()] = Math.max(
         0, Math.min(max, this.init_IArr[this.init_DataUtil_JsonUtil.ordinal()] - (int)Math.signum(amount) * (update_I() + 2))
      );
      return true;
   }

   private int init_I_DataUtil_JsonUtil(DataUtil.JsonUtil tab) {
      return tab == DataUtil.JsonUtil.load ? 0 : this.init_IArr[tab.ordinal()];
   }

   private void init_V_DataUtil_JsonUtil_I(DataUtil.JsonUtil tab, int renderedBottomY) {
      if (tab == this.init_DataUtil_JsonUtil) {
         this.load_IArr[tab.ordinal()] = Math.max(0, renderedBottomY + this.init_I_DataUtil_JsonUtil(tab) - this.start);
      }
   }

   private int load_I_DataUtil_JsonUtil(DataUtil.JsonUtil tab) {
      return tab == DataUtil.JsonUtil.load ? 0 : Math.max(0, this.load_IArr[tab.ordinal()] - this.stop);
   }

   public int init_I() {
      DataUtil.CachePool gate = this.init_DataUtil_CachePool();
      if (gate == DataUtil.CachePool.init) {
         if (this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.load) {
            int chrome = this.prev > 0 ? this.prev : 4 + (update_I() + 2) * 2 + update_I();
            return this.reset + chrome + Math.max(31, this.init_I_I_Map(this.bind, this.init_Map_List(this.init_BufferHelper.load_List()))) + 4;
         } else {
            return this.reset + Math.max(update_I() * 2, this.load_IArr[this.init_DataUtil_JsonUtil.ordinal()]) + 4;
         }
      } else {
         return gate == this.init_DataUtil_CachePool && this.parse > 0 ? this.parse : 120;
      }
   }

   public boolean init_Z() {
      return this.init_DataUtil_CachePool() != DataUtil.CachePool.init;
   }

   public int load_I() {
      if (this.init_DataUtil_CachePool() != DataUtil.CachePool.init) {
         return this.init_I();
      } else if (this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.load) {
         int chrome = this.prev > 0 ? this.prev : 4 + (update_I() + 2) * 2 + update_I();
         return this.reset + chrome + 31 + 4;
      } else {
         return Math.min(this.init_I(), this.reset + update_I() * 4 + 4);
      }
   }

   public int read_I() {
      DataUtil.CachePool gate = this.init_DataUtil_CachePool();
      if (gate != DataUtil.CachePool.init && gate == this.init_DataUtil_CachePool && this.scan > 0) {
         return this.scan + 24;
      }

      int row1 = this.load_I_Str("Queue 00") + this.load_I_Str("Macro") + this.load_I_Str("GUI") + this.load_I_Str("Item") + this.load_I_Str("Server") + 8;
      int row2 = this.load_I_Str("Pos") + this.load_I_Str("TPA inv") + this.load_I_Str("Filter") + this.load_I_Str("Module") + this.load_I_Str("Clear") + 8;
      return Math.max(264, Math.max(row1, row2) + 8);
   }

   private int init_I_I_Map(int logW, Map<String, String> names) {
      int v = this.init_BufferHelper.load_I();
      if (v != this.pool || logW != this.index) {
         int px = 0;

         for (autismclient.util.mm.JsonUtil line : this.init_BufferHelper.write_List()) {
            px += this.init_I_JsonUtil(line, logW, names);
         }

         this.task = px;
         this.pool = v;
         this.index = logW;
      }

      return this.task;
   }

   public boolean read(int keyCode, int scanCode, int modifiers) {
      SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.f_a_I, this.f_b_I);

      for (BitSetUtil in : this.load_List) {
         if (in.load_Z() && in.init_Z_SyncManager_3(ctx, keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   public boolean init_Z_C_I(char chr, int modifiers) {
      SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.f_a_I, this.f_b_I);

      for (BitSetUtil in : this.load_List) {
         if (in.load_Z() && in.init_Z_SyncManager_2(ctx, chr, modifiers)) {
            return true;
         }
      }

      return false;
   }

   public boolean load_Z() {
      for (BitSetUtil in : this.load_List) {
         if (in.load_Z()) {
            return true;
         }
      }

      return false;
   }

   public void read_V() {
      this.init_BitSetUtil.init_V_Z(false);
      this.load_BitSetUtil.init_V_Z(false);
      this.read_BitSetUtil.init_V_Z(false);
      this.write_BitSetUtil.init_V_Z(false);
      this.update_BitSetUtil.init_V_Z(false);
      this.sync_BitSetUtil.init_V_Z(false);
      this.valid();
   }

   private static int update_I() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int sync_I() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int flush_I() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private static int close_I() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private static int open_I() {
      return update_I() + 2;
   }

   private int load_I_Str(String label) {
      return autismclient.gui.f.RenderState.load(k.init(this.init_Font, label, this.init_CharEncoder.init_Identifier_l(l.read), this.put));
   }

   private int init_I_I_I(int boxY, int boxH) {
      return j.init(boxY, boxH, k.init(this.init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private int init_I_StrArr(String... labels) {
      Identifier f = this.init_CharEncoder.init_Identifier_l(l.read);
      int wmax = 0;

      for (String l : labels) {
         wmax = Math.max(wmax, k.init(this.init_Font, l, f, this.add));
      }

      return wmax + 8;
   }

   private int poll_I() {
      return Math.max(this.load_I_Str("Public"), this.load_I_Str("Private")) + 8;
   }

   private SyncManager init_SyncManager_GuiGraphicsExtract(GuiGraphicsExtractor g, int mx, int my) {
      return new SyncManager(g, this.init_Font, RingBuffer.init_RingBuffer_F(1.0F), this.init_CharEncoder, mx, my, this.init_F);
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, SyncManager ctx, BitSetUtil f, int x, int y, int w) {
      this.init_V_GuiGraphicsExtract_2(g, ctx, f, x, y, w, update_I());
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor g, SyncManager ctx, BitSetUtil f, int x, int y, int w, int h) {
      f.init_TableIndex_F(x, y, w, h);
      f.init_V_SyncManager(ctx);
      if (y + h > this.buffer && y < this.entry) {
         this.load_List.add(f);
      }
   }

   private void init_V_Str(String fp, int atX, int atY) {
      this.update_Z = true;
      this.load_Str = fp;
      this.f_g_I = atX;
      this.f_h_I = atY;
      this.sync_Z = false;
   }

   private void valid() {
      this.update_Z = false;
      this.sync_Z = false;
      this.write_List.clear();
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor g, int mx, int my) {
      this.write_List.clear();
      if (this.update_Z && this.init_DataUtil_JsonUtil == DataUtil.JsonUtil.read) {
         RenderState p = this.init_BufferHelper.init_RenderState_Str(this.load_Str);
         if (p == null) {
            this.update_Z = false;
         } else {
            record Inner_DataUtil(String init_Str, int init_I, Runnable init_Runnable, boolean init_Z) {
            }

            List<Inner_DataUtil> acts = new ArrayList<>();
            String tpaName = p.load();
            boolean coLocated = tpaName.matches("[A-Za-z0-9_]{1,16}") && autismclient.util.mm.BufferHelper.init_Z_RenderState(p);
            if (coLocated) {
               acts.add(new Inner_DataUtil("TPA", -7429889, () -> this.init_BufferHelper.write_V(tpaName), true));
               acts.add(new Inner_DataUtil("Trade", -7429889, () -> this.init_BufferHelper.update(tpaName), true));
               acts.add(new Inner_DataUtil("Pay", -7429889, () -> this.load_V_Str(tpaName), true));
            }

            if (p.init_Z && !p.update_Str.isBlank() && !autismclient.util.mm.BufferHelper.init_Z_Str(p.update_Str)) {
               acts.add(new Inner_DataUtil("Join server", -7429889, () -> this.init_V_RenderState(p), true));
            }

            if (p.load_Z) {
               acts.add(new Inner_DataUtil("Copy coords", this.put, () -> {
                  this.read((int)p.init_D + " " + (int)p.load_D + " " + (int)p.read_D);
                  bi.init("Coords copied.");
               }, true));
            }

            if (this.init_BufferHelper.write_Z() && !p.read_Z) {
               if (p.write_Z) {
                  acts.add(new Inner_DataUtil("Revoke sending", -14249, () -> this.init_BufferHelper.init_V_Str_Z(p.init_Str, false), true));
               } else {
                  acts.add(new Inner_DataUtil("Allow sending", this.set, () -> this.init_BufferHelper.init_V_Str_Z(p.init_Str, true), true));
               }
            }

            if (this.init_BufferHelper.load_Z()
               && !p.init_Str.equals(this.init_BufferHelper.read_Str())
               && !p.init_Str.equals(this.init_BufferHelper.write_Str())) {
               acts.add(new Inner_DataUtil("Make host", this.set, () -> this.init_BufferHelper.flush(p.init_Str), true));
            }

            if (p.update_Z) {
               acts.add(new Inner_DataUtil("Unblock", this.set, () -> this.init_BufferHelper.poll(p.init_Str), true));
            } else {
               acts.add(new Inner_DataUtil("Block", this.clear, () -> this.init_BufferHelper.close(p.init_Str), true));
            }

            if (this.init_BufferHelper.load_Z() && !p.init_Str.equals(this.init_BufferHelper.read_Str())) {
               if (this.sync_Z) {
                  acts.add(new Inner_DataUtil("Confirm ban", -14249, () -> this.init_BufferHelper.sync(p.init_Str), true));
               } else {
                  acts.add(new Inner_DataUtil("Ban", this.clear, () -> this.sync_Z = true, false));
               }
            }

            int itemH = sync_I();
            int pad = 6;
            int wMenu = 72;
            Identifier menuFont = this.init_CharEncoder.init_Identifier_l(l.read);

            for (Inner_DataUtil a : acts) {
               wMenu = Math.max(wMenu, k.init(this.init_Font, a.init_Str(), menuFont, a.init_I()) + pad * 2);
            }

            int hMenu = acts.size() * itemH + 4;
            int mxv = Math.max(this.f_c_I + 2, Math.min(this.f_g_I, this.f_c_I + this.f_e_I - wMenu - 2));
            int myv = Math.max(this.f_d_I + 2, Math.min(this.f_h_I, this.f_d_I + this.f_f_I - hMenu - 2));
            PacketUtil.init_V_GuiGraphicsExtract_3(g, autismclient.gui.f.NetHelper.init(mxv, myv, wMenu, hMenu), -233170402, this.get, 0);
            int iy = myv + 2;

            for (Inner_DataUtil a : acts) {
               boolean hover = mx >= mxv && mx < mxv + wMenu && my >= iy && my < iy + itemH;
               if (hover) {
                  PacketUtil.load(g, autismclient.gui.f.NetHelper.init(mxv + 1, iy, wMenu - 2, itemH), 4, load_I_I_I(a.init_I(), 51));
               }

               this.init_V_GuiGraphicsExtract_3(g, a.init_Str(), mxv + pad, this.init_I_I_I(iy, itemH), a.init_I(), false, wMenu - pad);
               this.write_List.add(new DataUtil.StreamUtil(mxv, iy, wMenu, itemH, a.init_Str(), a.init_I(), a.init_Runnable(), a.init_Z()));
               iy += itemH;
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int border, int textColor, int mx, int my, Runnable action
   ) {
      boolean enabled = action != null;
      boolean hover = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone = border != this.set && textColor != this.set
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         : autismclient.gui.f.b.CachePool.Inner_DataUtil.read;
      autismclient.gui.f.b.CachePool.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, h), label, tone, hover, false);
      if (!enabled) {
         PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 3, 1711276032);
      }

      if (enabled) {
         this.init_V_I(x, y, w, h, action);
      }
   }

   private void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int accentColor, boolean selected, boolean enabled, int mx, int my, Runnable action
   ) {
      boolean clickable = enabled && action != null;
      boolean hover = clickable && !selected && mx >= x && mx < x + w && my >= y && my < y + h;
      if (!clickable && !selected) {
         autismclient.gui.f.b.ConfigStore.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, h), label);
      } else {
         autismclient.gui.f.b.ConfigStore.init(
            UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, h), label, accentColor, selected, hover
         );
      }

      if (clickable) {
         this.init_V_I(x, y, w, h, action);
      }
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor g, int x, int y, int w, String label, boolean value, int mx, int my, Runnable onToggle) {
      int h = sync_I();
      boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
      PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, hover ? load_I_I_I(this.run, 51) : 705827348, this.run);
      autismclient.gui.f.b.g.init(UiHelper.init(g, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(x, y, w, h), label, value, hover, "mm:" + label);
      this.init_V_I(x, y, w, h, onToggle);
      return y + h + 2;
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor g, String text, int x, int y, int color, boolean center) {
      this.init_V_GuiGraphicsExtract_3(g, text, x, y, color, center, 2147483647);
   }

   private int init_I_GuiGraphicsExtract_2(GuiGraphicsExtractor g, String name, int x, int y, String fpHex, boolean self) {
      Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
      String value = name == null ? "" : name;
      if (!autismclient.util.mm.BufferHelper.init_Z_Str_Z(fpHex, self)) {
         int color = autismclient.util.mm.BufferHelper.init_I_Str_Z(fpHex, self);
         k.init(g, this.init_Font, value, fontId, color, x, y, false);
         return x + k.init(this.init_Font, value, fontId, color);
      }

      int cx = x;
      int len = value.length();

      for (int i = 0; i < len; i++) {
         String ch = String.valueOf(value.charAt(i));
         int color = autismclient.util.mm.BufferHelper.init(fpHex, self, i, len);
         k.init(g, this.init_Font, ch, fontId, color, cx, y, false);
         cx += k.init(this.init_Font, ch, fontId, color);
      }

      return cx;
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor g, String text, int x, int y, int color, boolean center, int maxWidth) {
      Identifier fontId = this.init_CharEncoder.init_Identifier_l(l.read);
      String value = text == null ? "" : text;
      if (maxWidth != 2147483647 && !center) {
         k.load(g, this.init_Font, value, fontId, color, x, y, Math.max(1, maxWidth), false);
      } else {
         if (maxWidth != 2147483647) {
            value = k.load(this.init_Font, value, maxWidth, fontId, color);
         }

         int wpx = k.init(this.init_Font, value, fontId, color);
         k.init(g, this.init_Font, value, fontId, color, center ? x - wpx / 2 : x, y, false);
      }
   }

   private ItemStack init_ItemStack_DataUtil_DataUtil(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      ItemStack cached = this.init_Map.get(b.read);
      if (cached != null) {
         return cached;
      }

      ItemStack st = autismclient.util.mm.ConfigStore.init_ItemStack(b);
      if (st == null) {
         st = ItemStack.EMPTY;
      }

      while (this.init_Map.size() >= 64) {
         Iterator<String> it = this.init_Map.keySet().iterator();
         if (!it.hasNext()) {
            break;
         }

         it.next();
         it.remove();
      }

      this.init_Map.put(b.read, st);
      return st;
   }

   private static int load_I_I_I(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static String init_Str_I(int max) {
      return max <= 0 ? "∞" : Integer.toString(max);
   }

   private void init_V_I(int x, int y, int w, int h, Runnable action) {
      this.init_List.add(new DataUtil.ConfigStore(x, y, w, h, this.buffer, this.entry, action));
   }

   public interface BufferHelper {
      Screen init_Screen();
   }

   private enum CachePool {
      init,
      load,
      read,
      write;
   }

   private static final class ConfigStore {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final int update;
      final int sync;
      final Runnable init_Runnable;

      ConfigStore(int x, int y, int w, int h, int gateTop, int gateBottom, Runnable action) {
         this.init_I = x;
         this.load = y;
         this.read = w;
         this.write = h;
         this.update = gateTop;
         this.sync = gateBottom;
         this.init_Runnable = action;
      }

      boolean init(double mx, double my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write && my >= this.update && my < this.sync;
      }
   }

   private enum JsonUtil {
      init_DataUtil_JsonUtil("Lobbies"),
      load("Chat"),
      read("Members"),
      write("Settings");

      final String init_Str;

      JsonUtil(String l) {
         this.init_Str = l;
      }
   }

   private record NetHelper(int init_I, int load, int read, int write, String init_Str, int update, int sync) {
      boolean init(double mx, double my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write && my >= this.update && my < this.sync;
      }
   }

   private static final class StreamUtil {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final String init_Str;
      final int update;
      final Runnable init_Runnable;
      final boolean init_Z;

      StreamUtil(int x, int y, int w, int h, String label, int color, Runnable action, boolean closes) {
         this.init_I = x;
         this.load = y;
         this.read = w;
         this.write = h;
         this.init_Str = label;
         this.update = color;
         this.init_Runnable = action;
         this.init_Z = closes;
      }

      boolean init(double mx, double my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum TaskQueue {
      init,
      load,
      read;
   }
}
