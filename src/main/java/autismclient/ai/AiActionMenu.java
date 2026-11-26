package autismclient.ai;

import autismclient.gui.ui.widgets.SearchFilterList;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;

final class AiActionMenu {
   private static final int init_I = 2;
   private static final int load_I = 3;
   private static final long init_J = 3000000L;
   private static final long load_J = 2000L;
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   private final Runnable init_Runnable;
   private final AiIntents.Inner_StreamUtil init_ad_StreamUtil = AiService.init_ad_StreamUtil();
   final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget().init_BitSetUtil_I(64);
   final autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget()
      .init_BitSetUtil_I(4)
      .init_BitSetUtil_Predicate(s -> s.chars().allMatch(Character::isDigit));
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
   private final SearchFilterList<AiActionMenu.Inner_CachePool> init_a = new SearchFilterList<>(AiActionMenu.Inner_CachePool::search);
   private final Map<AiActionMenu.Inner_DataUtil, AiActionMenu.Inner_ConfigStore> init_Map = new EnumMap<>(AiActionMenu.Inner_DataUtil.class);
   private AiActionMenu.Inner_DataUtil init_StreamUtil_DataUti;
   private List<AiActionMenu.Inner_DataUtil> init_List;
   private Object init_Obj;
   private List<AiActionMenu.Inner_CachePool> load_List;
   private List<AiActionMenu.Inner_CachePool> read_List;
   private List<AiActionMenu.Inner_CachePool> write_List;
   private String init_Str;
   private String load_Str;
   private AiActionMenu.Inner_DataUtil load_StreamUtil_DataUti;
   private int read_I;
   private int write_I;
   private boolean init_Z;
   private static Map<EntityType<?>, ItemStack> load_Map;
   private AiActionMenu.Inner_BufferHelper init_StreamUtil_BufferH;
   private AiActionMenu.Inner_CachePool init_StreamUtil_CachePo;
   private final Map<String, AiActionMenu.Inner_CachePool> read_Map;
   private final Map<String, autismclient.gui.ui.widgets.TextInputWidget> write_Map;
   private List<String> update;
   private Object load_Obj;
   private boolean load_Z;
   private boolean read_Z;
   private String read_Str;
   private AiSchematicPlacement init_bW;
   private AiFarmPlacement init_E;
   private AiActionMenu.Inner_CachePool load_StreamUtil_CachePo;
   private boolean write_Z;
   private String write_Str;
   private final AiUiKit.Inner_StreamUtil load_cE_StreamUtil;
   private final AiUiKit.Inner_StreamUtil read_cE_StreamUtil;
   private AiActionMenu.Inner_BufferHelper load_StreamUtil_BufferH;

   AiActionMenu(AiService service, AiViewState view, AiAssistantOverlay host, Runnable onRun) {
      this.init_StreamUtil_DataUti = AiActionMenu.Inner_DataUtil.GET;
      this.write_List = List.of();
      this.init_Str = "";
      this.load_Str = "";
      this.write_I = 3;
      this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.HOME;
      this.read_Map = new LinkedHashMap<>();
      this.write_Map = new LinkedHashMap<>();
      this.update = List.of();
      this.read_Str = "";
      this.write_Str = "farmland";
      this.load_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
      this.read_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.init_Runnable = onRun;
      this.init_BitSetUtil.load_BitSetUtil_Consumer(text -> this.sync_V());
      this.load_BitSetUtil.load_BitSetUtil_Consumer(text -> this.sync_V());
   }

   List<autismclient.gui.ui.widgets.TextInputWidget> init_List() {
      ArrayList<autismclient.gui.ui.widgets.TextInputWidget> fields = new ArrayList<>(List.of(this.init_BitSetUtil, this.load_BitSetUtil));
      fields.addAll(this.write_Map.values());
      return fields;
   }

   void init_V_Str_Str(String actionName, String query) {
      this.load_StreamUtil_BufferH = null;
      if ("HOME".equals(actionName)) {
         this.open();
      } else {
         String[] parts = actionName.split(":", 2);
         if (parts[0].equals("FARM") && parts.length > 1 && parts[1].equals("REVIEW") && this.init_E != null && this.init_E.init_Z()) {
            this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.REVIEW;
         } else {
            this.init_V_StreamUtil_DataUti(AiActionMenu.Inner_DataUtil.valueOf(parts[0]));
            if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE && query != null && query.contains(",")) {
               this.update = AiMiningTargets.parse(query).types();
               this.init_BitSetUtil.init_BitSetUtil_Str("");
            } else {
               this.init_BitSetUtil.init_BitSetUtil_Str(query == null ? "" : query);
            }

            if (parts.length > 1) {
               this.load_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.valueOf(parts[1]);
            }
         }
      }
   }

   void init_V() {
      if (this.init_BitSetUtil.load_Z()) {
         this.init_BitSetUtil.init_V_Z(false);
      }

      if (this.load_BitSetUtil.load_Z()) {
         this.load_BitSetUtil.init_V_Z(false);
      }

      this.write_Map.values().forEach(field -> field.init_V_Z(false));
   }

   autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_C(char c) {
      return this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET && (Character.isLetterOrDigit(c) || c == ' ' || c == '-')
         ? this.init_BitSetUtil
         : null;
   }

   boolean init_Z() {
      return this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.HOME;
   }

   void load_V() {
      if (this.init_bW != null) {
         this.init_bW.read_V();
      }

      if (this.init_E != null) {
         this.init_E.read_V();
      }

      this.init_bW = null;
      this.init_E = null;
      if ((this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD || this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM)
         && this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.HOME) {
         this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.TARGET;
      }
   }

   int init_I() {
      int target = AiUiKit.init_I() * 2
         + AiUiKit.read() * 3
         + 3 * AiUiKit.load_I()
         + 10
         + 4
         + (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE ? AiUiKit.init_I() + 2 : 0);
      return this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.OPTIONS && this.init_StreamUtil_DataUti.init_Z
         ? Math.max(
            target,
            this.load_I()
               + (
                  this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                     ? Math.min(this.read_I(), AiUiKit.read() * 3 + AiUiKit.init_I() * 6 + 14)
                     : this.read_I()
               )
         )
         : target;
   }

   private int load_I() {
      return AiUiKit.read() * 3 + AiUiKit.init_I() + 8;
   }

   private int read_I() {
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM && this.write_Z) {
         return AiUiKit.read() * 4 + AiUiKit.init_I() * 2 + 12;
      } else {
         return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD
            ? AiUiKit.read() * 6 + AiUiKit.init_I() * 4 + 16
            : (
               this.init_StreamUtil_DataUti.init_Z
                  ? AiUiKit.read() * 2
                     + AiUiKit.init_I() * 3
                     + 6
                     + (
                        this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                           ? AiUiKit.read() + 2 + this.read_Map.size() * (AiUiKit.init_I() + 2)
                           : 0
                     )
                  : AiUiKit.read() * 2 + 2
            );
      }
   }

   int init_I_I(int width) {
      int h = AiUiKit.init_I();
      int line = AiUiKit.read();
      int var10000;
      switch (this.init_StreamUtil_BufferH) {
         case HOME:
            var10000 = line * 2 + 6 + (this.write_List().size() + (width >= 440 ? 2 : 1)) / (width >= 440 ? 3 : 2) * (h + AiUiKit.write() + 4 + 4);
            break;
         case TARGET:
            var10000 = 0;
            break;
         case OPTIONS:
            var10000 = this.load_I() + this.read_I();
            break;
         case REVIEW:
            var10000 = this.load_I()
               + line
                  * (
                     this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                        ? this.read_Map.size() + 3
                        : (this.init_E != null ? 7 : (this.init_bW == null ? 4 : 8 + this.init_bW.init_List().size()))
                  )
               + 8;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   int init_I_cE(AiUiKit kit, int width, int room) {
      return Math.max(this.init_I(), room);
   }

   void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (!this.update.isEmpty() && this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET) {
         List<AiActionMenu.Inner_CachePool> rows = this.read_List();

         for (int i = 0; i < rows.size(); i++) {
            String key = this.init_Str_StreamUtil_CachePo(rows.get(i));
            if (this.update.contains(key) && !this.read_Map.containsKey(key)) {
               this.load(i);
            }
         }

         if (this.read_Map.keySet().containsAll(this.update)) {
            this.update = List.of();
         }
      }

      if (this.load_StreamUtil_BufferH != null && this.update.isEmpty()) {
         if (this.init_StreamUtil_BufferH == this.load_StreamUtil_BufferH) {
            this.load_StreamUtil_BufferH = null;
         } else if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET) {
            if (this.init_StreamUtil_CachePo_List(this.read_List()) != null) {
               this.sync_V();
            }
         } else if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.OPTIONS
            && (this.init_bW == null || this.init_bW.load_Z())
            && (this.init_E == null || this.init_E.init_Z())) {
            this.sync_V();
         }
      }

      if (this.init_bW != null && !this.init_bW.load_Z() && !this.init_bW.init_Str_2().isEmpty()) {
         this.read_Str = this.init_bW.init_Str_2();
      }

      if (this.init_E != null && this.init_E.init_Z() && !this.init_E.load_Z()) {
         this.read_Str = this.init_E.write_Str();
      }

      if (this.load_Obj != null && this.load_Obj != Minecraft.getInstance().level) {
         this.open();
         this.read_Str = "World changed. Choose the action again.";
      }

      int h = AiUiKit.init_I();
      int line = AiUiKit.read();
      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), line);
      kit.init_V_Str_3(
         this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.HOME
            ? "What would you like to do?"
            : this.init_StreamUtil_DataUti.init_Str + " / " + this.init_Str(),
         title,
         title.read(),
         title.update(),
         kit.init_JsonUtil.textPrimary
      );
      int top = title.load() + 2;
      if (this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.HOME) {
         autismclient.gui.f.NetHelper trail = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), line);
         kit.init_V_Str_3("Choose action  /  Choose target  /  Options  /  Review", trail, trail.read(), trail.update(), kit.init_JsonUtil.textSecondary);
         top += line + 2;
      }

      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - h, area.update(), h);
      autismclient.gui.f.NetHelper note = autismclient.gui.f.NetHelper.init(
         area.read(), this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.HOME ? area.load() - line : footer.write() - 2 - line, area.update(), line
      );
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), Math.max(0, note.write() - 2 - top));
      kit.init_V_NetHelper(body);
      switch (this.init_StreamUtil_BufferH) {
         case HOME:
            this.init_V_cE_NetHelper(kit, body);
            break;
         case TARGET:
            int importW = this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD ? kit.load(this.init_Z ? "Loading..." : "Import") + 2 : 0;
            this.init_BitSetUtil.load_BitSetUtil_Str(this.init_StreamUtil_DataUti.read_Str);
            kit.init_V_BitSetUtil_NetHelper(this.init_BitSetUtil, autismclient.gui.f.NetHelper.init(body.read(), body.write(), body.update() - importW, h));
            if (importW > 0) {
               kit.init_V_NetHelper_2(
                  autismclient.gui.f.NetHelper.init(body.init() - importW + 2, body.write(), importW - 2, h),
                  this.init_Z ? "Loading..." : "Import",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                  this::pop
               );
            }

            int listTop = body.write() + h + 2;
            if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE) {
               int clearW = kit.load("Clear");
               int selectedW = kit.load(this.read_Z ? "Show all" : "Selected");
               autismclient.gui.f.NetHelper summary = autismclient.gui.f.NetHelper.init(
                  body.read(), listTop, Math.max(0, body.update() - clearW - selectedW - 4), h
               );
               String text = this.read_Map.isEmpty() ? "Click blocks to select them" : this.read_Map.size() + " selected: " + this.read_Str();
               kit.init_V_Str_3(text, summary, summary.read(), summary.update(), kit.init_JsonUtil.textSecondary);
               kit.init_V_NetHelper_Str(summary, text);
               kit.init_V_NetHelper_2(
                  autismclient.gui.f.NetHelper.init(body.init() - clearW - selectedW - 2, listTop, selectedW, h),
                  this.read_Z ? "Show all" : "Selected",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                  () -> {
                     this.read_Z = !this.read_Z;
                     this.init_cE_StreamUtil.load();
                  }
               );
               kit.init_V_NetHelper_2(
                  autismclient.gui.f.NetHelper.init(body.init() - clearW, listTop, clearW, h),
                  "Clear",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
                  () -> {
                     this.read_Map.clear();
                     this.write_Map.clear();
                     this.read_Str = "";
                  }
               );
               listTop += h + 2;
            }

            List<AiActionMenu.Inner_CachePool> rows = this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE && this.read_Z
               ? List.copyOf(this.read_Map.values())
               : this.read_List();
            this.write_I = Math.max(1, (body.load() - listTop) / AiUiKit.load_I());
            this.init_V_cE_2(kit, autismclient.gui.f.NetHelper.init(body.read(), listTop, body.update(), Math.max(0, body.load() - listTop)), rows);
            break;
         case OPTIONS:
            this.load(kit, body);
            break;
         case REVIEW:
            this.read(kit, body);
      }

      kit.init_V();
      String noteText = !this.read_Str.isEmpty()
         ? this.read_Str
         : (
            !update_Z()
               ? "Join a world to start an action."
               : (
                  this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET
                        && this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                     ? "Click to toggle blocks. Nearest reachable first; amounts apply per block type."
                     : (
                        this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET && this.init_StreamUtil_CachePo_List(this.write_List) != null
                           ? this.init_StreamUtil_CachePo_List(this.write_List).label()
                           : (
                              this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.HOME
                                 ? "Runs locally. No API needed."
                                 : "Review your choices before starting."
                           )
                     )
               )
         );
      kit.init_V_Str_3(noteText, note, note.read(), note.update(), this.read_Str.isEmpty() ? kit.init_JsonUtil.textSecondary : kit.init_JsonUtil.danger);
      kit.init_V_NetHelper_Str(note, noteText);
      if (this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.HOME) {
         int w = kit.load("Back");
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(footer.read(), footer.write(), w, h), "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::close
         );
         String label = this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.REVIEW ? (this.init_cF.init_Z() ? "Replace task" : "Start") : "Next";
         int nextW = kit.load(label);
         this.init(
            kit,
            autismclient.gui.f.NetHelper.init(footer.init() - nextW, footer.write(), nextW, h),
            label,
            update_Z()
               && (
                  this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.TARGET
                     || (
                        this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                           ? !this.read_Map.isEmpty()
                           : this.init_StreamUtil_CachePo_List(this.write_List) != null
                     )
               )
               && (this.init_bW == null || this.init_bW.load_Z() && !this.init_bW.init_Z_2())
               && (this.init_E == null || this.init_E.init_Z()),
            this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.REVIEW ? this::poll : this::sync_V
         );
      }
   }

   private String init_Str() {
      String var10000;
      switch (this.init_StreamUtil_BufferH) {
         case HOME:
            var10000 = "Actions";
            break;
         case TARGET:
            var10000 = "Choose target";
            break;
         case OPTIONS:
            var10000 = this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD ? "Placement" : "Options";
            break;
         case REVIEW:
            var10000 = "Review";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void init(AiUiKit kit, autismclient.gui.f.NetHelper b, String text, boolean enabled, Runnable action) {
      autismclient.gui.f.b.CachePool.init(
         kit.init_SyncManager,
         b,
         text,
         enabled ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         enabled && kit.init_Z_NetHelper(b),
         enabled
      );
      if (enabled) {
         kit.init_V_NetHelper(b, action, null, null);
      } else {
         kit.load_V(b);
      }
   }

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      List<AiActionMenu.Inner_DataUtil> actions = this.write_List();
      int columns = area.update() >= 440 ? 3 : 2;
      int rowH = AiUiKit.init_I() + AiUiKit.write() + 2 + 4;
      int rows = (actions.size() + columns - 1) / columns;
      this.load_cE_StreamUtil.init(area, rows * (rowH + 2));
      kit.init_V_cE_StreamUtil(this.load_cE_StreamUtil);
      int available = area.update() - (this.load_cE_StreamUtil.init_I() > 0 ? 7 : 0);
      int w = (available - 2 * (columns - 1)) / columns;

      for (int i = 0; i < actions.size(); i++) {
         AiActionMenu.Inner_DataUtil a = actions.get(i);
         autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(
            area.read() + i % columns * (w + 2), area.write() + i / columns * (rowH + 2) - this.load_cE_StreamUtil.init_I, w, rowH
         );
         if (b.load() > area.write() && b.write() < area.load()) {
            kit.init_V_NetHelper(
               autismclient.gui.f.NetHelper.init(b.read(), b.write(), b.update(), AiUiKit.init_I()),
               a.init_Str,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
               () -> this.init_V_StreamUtil_DataUti(a),
               a.write_Str
            );
            kit.init_V_Str_3(
               a.write_Str,
               autismclient.gui.f.NetHelper.init(b.read(), b.write() + AiUiKit.init_I(), b.update(), AiUiKit.write() + 4),
               b.read() + 3,
               b.update() - 6,
               kit.init_JsonUtil.textSecondary
            );
         }
      }

      kit.load(this.load_cE_StreamUtil);
   }

   private void load(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      int h = AiUiKit.init_I();
      int line = AiUiKit.read();
      int contentH = this.read_I();
      this.read_cE_StreamUtil.init(area, contentH);
      kit.init_V_cE_StreamUtil(this.read_cE_StreamUtil);
      int w = area.update() - (this.read_cE_StreamUtil.init_I() > 0 ? 7 : 0);
      int x = area.read();
      int y = area.write() - this.read_cE_StreamUtil.init_I;
      kit.init_V_Str_3(
         this.init_StreamUtil_CachePo == null ? this.init_StreamUtil_DataUti.write_Str : this.read_Str(),
         autismclient.gui.f.NetHelper.init(x, y, w, line),
         x,
         w,
         kit.init_JsonUtil.textPrimary
      );
      y += line + 2;
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM && this.write_Z) {
         kit.init_V_Str_3("Crop: " + this.read_Str(), autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textPrimary);
         int var23 = y + line + 2;
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(x, var23, w, h),
            "Soul sand for nether wart",
            AiFarmArea.init_Z(this.write_Str),
            "local-farm-substrate",
            () -> {
               this.write_Str = AiFarmArea.init_Z(this.write_Str) ? "farmland" : "soul_sand";
               if (AiFarmArea.init_Z(this.write_Str)) {
                  this.load_StreamUtil_CachePo = this.init_StreamUtil_CachePo;
                  this.init_StreamUtil_CachePo = init_StreamUtil_CachePo_Str_Item("nether wart", Items.NETHER_WART);
               } else {
                  this.init_StreamUtil_CachePo = this.load_StreamUtil_CachePo == null
                     ? init_StreamUtil_CachePo_Str_Item("wheat", Items.WHEAT_SEEDS)
                     : this.load_StreamUtil_CachePo;
               }

               if (this.init_E != null) {
                  this.init_E.read_V();
               }

               this.init_E = null;
               this.read_Str = "";
            }
         );
         int var24 = var23 + h + 2;
         this.init(
            kit,
            autismclient.gui.f.NetHelper.init(x, var24, w, h),
            this.init_E == null ? "Choose two corners" : "Change corners",
            !this.init_cF.init_Z() && !AiDirectOps.load_Z(),
            this::flush
         );
         int var25 = var24 + h + 2;
         kit.init_V_Str_3(
            this.init_E == null ? "Select the exact field footprint in the world." : this.init_E.write_Str(),
            autismclient.gui.f.NetHelper.init(x, var25, w, line),
            x,
            w,
            kit.init_JsonUtil.textSecondary
         );
         y = var25 + line + 2;
         kit.init_V_Str_3(
            AiFarmArea.init_Z(this.write_Str) ? "No water, slabs or hoe needed." : "Farmland crops use dirt, water and a hoe.",
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.init_JsonUtil.textSecondary
         );
      } else if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD && this.init_bW != null) {
         kit.init_V_Str_3(this.init_bW.write_Str_2(), autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textSecondary);
         y += line;
         kit.init_V_Str_3(
            "Size " + this.init_bW.update_Str() + " / origin " + this.load_Str(),
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.init_JsonUtil.textSecondary
         );
         y += line + 2;
         int half = (w - 2) / 2;
         this.init(
            kit,
            autismclient.gui.f.NetHelper.init(x, y, half, h),
            "Choose in world",
            this.init_bW.load_Z() && !this.init_cF.init_Z() && !AiDirectOps.load_Z(),
            () -> aP.init_V_bW_Runnable(this.init_bW, () -> {})
         );
         if (this.init_cF.init_Z() || AiDirectOps.load_Z()) {
            kit.init_V_NetHelper_Str(autismclient.gui.f.NetHelper.init(x, y, half, h), "Pause the current action before choosing a placement in world");
         }

         this.init(
            kit,
            autismclient.gui.f.NetHelper.init(x + half + 2, y, half, h),
            this.init_bW.init_Z_2() ? "Searching..." : "Best nearby site",
            this.init_bW.load_Z(),
            this.init_bW::flush
         );
         int var18 = y + h + 2;
         kit.init_V_Str_3(
            "Compare edits within 24 blocks. All four rotations.", autismclient.gui.f.NetHelper.init(x, var18, w, line), x, w, kit.init_JsonUtil.textSecondary
         );
         int var19 = var18 + line + 2;
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(x, var19, half, h),
            "Rotate: " + this.init_bW.init_I(),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            this.init_bW::sync_V
         );
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(x + half + 2, var19, half, h),
            "Mirror: " + this.init_bW.read_Str(),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            this.init_bW::init_V
         );
         int var20 = var19 + h + 2;
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(x, var20, w, h),
            "Clear natural terrain in empty space",
            this.init_bW.write_Z_2(),
            "local-terrain",
            () -> this.init_bW.load(!this.init_bW.write_Z_2())
         );
         int var21 = var20 + h + 2;
         autismclient.gui.f.NetHelper flyToggle = autismclient.gui.f.NetHelper.init(x, var21, w, h);
         kit.init_V_NetHelper_2(flyToggle, "Assume Fly", this.init_bW.update_Z(), "local-assume-fly", () -> this.init_bW.read(!this.init_bW.update_Z()));
         kit.init_V_NetHelper_Str(
            flyToggle,
            "Use native flight already granted in Survival, including singleplayer. Never grants flight or items. Without permission, use ground movement."
         );
         int var22 = var21 + h + 2;
         autismclient.gui.f.NetHelper flyHint = autismclient.gui.f.NetHelper.init(x, var22, w, line);
         kit.init_V_Str_3(this.init_bW.load_Str_2(), flyHint, x, w, kit.init_JsonUtil.textSecondary);
         kit.init_V_NetHelper_Str(flyHint, this.init_bW.load_Str_2());
         y = var22 + line + 2;
         kit.init_V_Str_3("Get missing materials before starting this build.", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textSecondary);
      } else if (this.init_StreamUtil_DataUti.init_Z) {
         kit.init_V_Str_3(
            (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE ? "Default per block" : "Amount")
               + " (1 to "
               + this.write_I()
               + ")",
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.init_JsonUtil.textSecondary
         );
         y += line;
         this.load_BitSetUtil.load_BitSetUtil_Str(this.init_StreamUtil_CachePo == null ? "Auto" : this.write_Str());
         kit.init_V_BitSetUtil_NetHelper(this.load_BitSetUtil, autismclient.gui.f.NetHelper.init(x, y, w, h));
         y += h + 2;
         String[] values = new String[]{"Auto", "1", "16", "64"};
         int cell = (w - 6) / 4;

         for (int i = 0; i < values.length; i++) {
            String value = values[i];
            kit.init_V_NetHelper_2(
               autismclient.gui.f.NetHelper.init(x + i * (cell + 2), y, cell, h), value, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, () -> {
                  this.load_BitSetUtil.init_BitSetUtil_Str(value.equals("Auto") ? "" : value);
                  this.read_Str = "";
               }
            );
         }

         y += h + 2;
         if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.GET) {
            kit.init_V_Str_3(
               "Always collect the requested amount in addition to inventory.", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textSecondary
            );
         } else if (this.read_Z()) {
            kit.init_V_NetHelper_2(
               autismclient.gui.f.NetHelper.init(x, y, w, h),
               this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.CRAFT ? "Craft extra items" : "Smelt extra items",
               this.load_Z,
               "local-additional",
               () -> this.load_Z = !this.load_Z
            );
         } else {
            kit.init_V_Str_3(
               this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                  ? "Each type has its own quota; nearest unfinished type first."
                  : "Uses the selected action's normal quantity rules.",
               autismclient.gui.f.NetHelper.init(x, y, w, line),
               x,
               w,
               kit.init_JsonUtil.textSecondary
            );
         }
      } else {
         kit.init_V_Str_3(this.init_StreamUtil_DataUti.write_Str, autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textSecondary);
      }

      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE) {
         y += line + 2;
         kit.init_V_Str_3("Individual amounts (optional)", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textSecondary);
         y += line + 2;
         int fieldW = Math.min(88, w / 3);

         for (Entry<String, AiActionMenu.Inner_CachePool> entry : this.read_Map.entrySet()) {
            autismclient.gui.ui.widgets.TextInputWidget field = this.init_BitSetUtil_Str(entry.getKey());
            field.load_BitSetUtil_Str(this.load_BitSetUtil.init_Str().isBlank() ? this.write_Str() : this.load_BitSetUtil.init_Str());
            kit.init_V_Str_3(entry.getValue().label(), autismclient.gui.f.NetHelper.init(x, y, w - fieldW - 2, h), x, w - fieldW - 2, kit.init_JsonUtil.textPrimary);
            kit.init_V_BitSetUtil_NetHelper(field, autismclient.gui.f.NetHelper.init(x + w - fieldW, y, fieldW, h));
            y += h + 2;
         }
      }

      kit.load(this.read_cE_StreamUtil);
   }

   private void read(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      int line = AiUiKit.read();
      int y = area.write();
      int x = area.read();
      int w = area.update();
      autismclient.gui.f.b.BufferHelper.init(kit.init_SyncManager, area);
      x += 5;
      y += 4;
      w -= 10;
      String[] lines;
      if (this.init_E != null) {
         AiFarmArea.Inner_DataUtil farmArea = this.init_E.init_C_DataUtil();
         lines = new String[]{
            this.init_StreamUtil_CachePo.label(),
            "Soil: " + (AiFarmArea.init_Z(this.write_Str) ? "soul sand" : "dirt and water"),
            "Area: " + farmArea.width() + "x" + farmArea.length() + " / " + farmArea.layout(this.write_Str).plots() + " plots",
            "First: " + this.init_Str_BlockPos(farmArea.from()),
            "Second: " + this.init_Str_BlockPos(farmArea.to()),
            this.init_E.write_Str(),
            this.init_cF.init_Z() ? "Starting replaces the current local task." : "Ready to start."
         };
      } else if (this.init_bW != null) {
         lines = new String[]{
            this.init_StreamUtil_CachePo.label(),
            "Origin: " + this.load_Str(),
            "Rotation: " + this.init_bW.init_I() + " / mirror: " + this.init_bW.read_Str(),
            this.init_bW.load_I() + " blocks / " + this.init_bW.update_Str(),
            this.init_bW.write_Z_2() ? "Clear natural terrain in schematic air" : "Keep terrain in schematic air",
            this.init_bW.load_Str_2(),
            "Assume Fly never grants flight or items",
            this.init_bW.write_Str_2()
         };
      } else {
         String quantity = this.init_StreamUtil_DataUti.init_Z
            ? "Amount: " + (this.load_BitSetUtil.init_Str().isBlank() ? this.write_Str() + " (automatic)" : this.load_BitSetUtil.init_Str())
            : this.init_StreamUtil_DataUti.write_Str;
         lines = new String[]{
            this.init_StreamUtil_DataUti.init_Str + (this.init_StreamUtil_CachePo == null ? "" : ": " + this.read_Str()),
            quantity,
            this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
               ? "Per block type; nearest unfinished target first"
               : (this.read_Z() ? (this.write_Z() ? "Quantity adds to the inventory" : "Quantity is the inventory target") : ""),
            this.init_cF.init_Z() ? "Starting replaces the current local task." : "Ready to start."
         };
      }

      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE) {
         ArrayList<String> extended = new ArrayList<>();
         extended.add("Mine amounts");

         try {
            JsonObject quotas = this.init_ad_BufferHelper().arguments().getAsJsonObject("counts");
            this.read_Map.forEach((key, row) -> extended.add(row.label() + ": " + AiJson.init_I_JsonObject(quotas, key, 1)));
         } catch (RuntimeException e) {
            extended.add(e.getMessage());
         }

         extended.add("Nearest unfinished target first");
         extended.add(this.init_cF.init_Z() ? "Starting replaces the current local task." : "Ready to start.");
         lines = extended.toArray(String[]::new);
      }

      if (this.init_bW != null) {
         List<String> extended = new ArrayList<>(List.of(lines));
         extended.addAll(this.init_bW.init_List());
         lines = extended.toArray(String[]::new);
      }

      this.read_cE_StreamUtil.init(area, lines.length * line + 8);
      kit.init_V_cE_StreamUtil(this.read_cE_StreamUtil);
      y -= this.read_cE_StreamUtil.init_I;

      for (String text : lines) {
         kit.init_V_Str_3(text, autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.init_JsonUtil.textPrimary);
         kit.init_V_NetHelper_Str(autismclient.gui.f.NetHelper.init(x, y, w, line), text);
         y += line;
      }

      kit.load(this.read_cE_StreamUtil);
   }

   private String load_Str() {
      BlockPos p = this.init_bW.init_BlockPos();
      return p.getX() + " " + p.getY() + " " + p.getZ();
   }

   private String init_Str_BlockPos(BlockPos p) {
      return p.getX() + " " + p.getY() + " " + p.getZ();
   }

   private boolean read_Z() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.GET
         || this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.CRAFT
         || this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.SMELT;
   }

   private String read_Str() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
         ? this.read_Map.values().stream().map(AiActionMenu.Inner_CachePool::label).collect(Collectors.joining(", "))
         : this.init_StreamUtil_CachePo.label();
   }

   private String write_Str() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE && this.read_Map.size() > 1
         ? "16"
         : this.init_StreamUtil_CachePo.fallback();
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_Str(String key) {
      return this.write_Map
         .computeIfAbsent(
            key,
            k -> {
               autismclient.gui.ui.widgets.TextInputWidget field = new autismclient.gui.ui.widgets.TextInputWidget()
                  .init_BitSetUtil_I(3)
                  .init_BitSetUtil_Predicate(s -> s.chars().allMatch(Character::isDigit));
               field.load_BitSetUtil_Consumer(text -> this.sync_V());
               return field;
            }
         );
   }

   private String init_Str_StreamUtil_CachePo(AiActionMenu.Inner_CachePool row) {
      return row.mineKey();
   }

   private List<AiActionMenu.Inner_CachePool> load_List() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE && this.read_Z
         ? List.copyOf(this.read_Map.values())
         : this.read_List();
   }

   private int write_I() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
         ? 640
         : (
            this.init_StreamUtil_DataUti != AiActionMenu.Inner_DataUtil.FIGHT
                  && this.init_StreamUtil_DataUti != AiActionMenu.Inner_DataUtil.CHOP
               ? (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM ? 128 : 2304)
               : 64
         );
   }

   private void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper frame, List<AiActionMenu.Inner_CachePool> rows) {
      if (frame.sync() >= 8) {
         autismclient.gui.ui.widgets.ListRowRenderer.init(
            kit.init_GuiGraphicsExtract, frame.read(), frame.write(), frame.update(), frame.sync(), this.init_BitSetUtil.load_Z()
         );
         autismclient.gui.f.NetHelper inner = frame.init(2);
         int rowH = AiUiKit.load_I();
         this.init_cE_StreamUtil.init(inner, rows.size() * rowH);
         kit.init_V_cE_StreamUtil(this.init_cE_StreamUtil);
         if (rows.isEmpty()) {
            autismclient.gui.ui.widgets.ListRowRenderer.init(kit.init_GuiGraphicsExtract, kit.init_Font, this.update_Str(), inner.read(), inner.write(), inner.update());
         } else {
            kit.init_V_NetHelper(inner);
            int w = inner.update() - (this.init_cE_StreamUtil.init_I() > 0 ? 7 : 0);

            for (int i = Math.max(0, this.init_cE_StreamUtil.init_I / rowH); i < rows.size(); i++) {
               int ry = inner.write() - this.init_cE_StreamUtil.init_I + i * rowH;
               if (ry >= inner.load()) {
                  break;
               }

               AiActionMenu.Inner_CachePool row = rows.get(i);
               autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(inner.read(), ry, w, rowH);
               autismclient.gui.ui.widgets.ListRowRenderer.init(
                  kit.init_GuiGraphicsExtract,
                  kit.init_Font,
                  Component.literal(row.label()),
                  row.icon(),
                  b.read(),
                  b.write(),
                  b.update(),
                  b.sync(),
                  kit.init_Z_NetHelper(b),
                  this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
                     ? this.read_Map.containsKey(this.init_Str_StreamUtil_CachePo(row))
                     : i == this.read_I,
                  kit.init_JsonUtil.textPrimary,
                  false
               );
               int index = i;
               kit.init_V_NetHelper(b, () -> this.load(index), null, null);
            }

            kit.init_V();
            kit.load(this.init_cE_StreamUtil);
         }
      }
   }

   private String update_Str() {
      if (!update_Z()) {
         return "Join a world first";
      } else if (this.init_Map.get(this.init_StreamUtil_DataUti) == null || this.init_Map.get(this.init_StreamUtil_DataUti).init() == null) {
         return "Loading...";
      } else if (this.sync_Str().isEmpty() && this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.GOTO) {
         return "Type x y z";
      } else {
         return this.sync_Str().isEmpty() && this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD ? "No schematics yet" : "No matches";
      }
   }

   boolean init_Z_I_Z(int key, boolean typing) {
      if (this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.HOME && this.init_StreamUtil_BufferH != AiActionMenu.Inner_BufferHelper.REVIEW) {
         switch (key) {
            case 257:
            case 335:
               if (!typing) {
                  return false;
               }

               this.sync_V();
               break;
            case 264:
               this.init_V_I(1);
               break;
            case 265:
               this.init_V_I(-1);
               break;
            case 266:
               this.init_V_I(-this.write_I);
               break;
            case 267:
               this.init_V_I(this.write_I);
               break;
            default:
               return false;
         }

         return true;
      } else {
         return false;
      }
   }

   boolean load_Z() {
      if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET && !this.init_BitSetUtil.init_Str().isEmpty()) {
         this.init_BitSetUtil.init_BitSetUtil_Str("");
         return true;
      }

      if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.HOME) {
         return false;
      }

      this.close();
      return true;
   }

   private void init_V_I(int delta) {
      if (this.init_StreamUtil_DataUti.init()) {
         List<AiActionMenu.Inner_CachePool> rows = this.load_List();
         if (!rows.isEmpty()) {
            this.read_I = Math.max(0, Math.min(rows.size() - 1, this.read_I + delta));
            int rowH = AiUiKit.load_I();
            int top = this.read_I * rowH;
            int shown = this.init_cE_StreamUtil.init_NetHelper().sync();
            if (top < this.init_cE_StreamUtil.init_I) {
               this.init_cE_StreamUtil.init_I = top;
            } else if (top + rowH > this.init_cE_StreamUtil.init_I + shown) {
               this.init_cE_StreamUtil.init_I = top + rowH - shown;
            }

            this.init_cE_StreamUtil.init_Z = false;
         }
      }
   }

   private void load(int index) {
      this.read_I = index;
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE) {
         List<AiActionMenu.Inner_CachePool> rows = this.load_List();
         if (index < 0 || index >= rows.size()) {
            return;
         }

         AiActionMenu.Inner_CachePool row = rows.get(index);
         String key = this.init_Str_StreamUtil_CachePo(row);
         if (!key.isEmpty()) {
            if (this.read_Map.remove(key) == null) {
               this.read_Map.put(key, row);
               this.init_BitSetUtil_Str(key);
            } else {
               this.write_Map.remove(key);
            }
         }

         this.load_Obj = this.read_Map.isEmpty() ? null : Minecraft.getInstance().level;
         this.read_Str = "";
      }

      this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
   }

   private void init_V_StreamUtil_DataUti(AiActionMenu.Inner_DataUtil next) {
      if (this.init_bW != null) {
         this.init_bW.read_V();
      }

      if (this.init_E != null) {
         this.init_E.read_V();
      }

      this.init_bW = null;
      this.init_E = null;
      this.init_StreamUtil_CachePo = null;
      this.load_Obj = null;
      this.read_Str = "";
      this.load_Z = false;
      this.read_Map.clear();
      this.write_Map.clear();
      this.read_Z = false;
      this.update = List.of();
      if (next != this.init_StreamUtil_DataUti) {
         this.init_StreamUtil_DataUti = next;
         this.init_BitSetUtil.init_BitSetUtil_Str("");
         this.load_BitSetUtil.init_BitSetUtil_Str("");
         this.read_I = 0;
         this.write_List = List.of();
         this.read_List = null;
         this.init_cE_StreamUtil.load();
      }

      this.init_StreamUtil_BufferH = next.init() ? AiActionMenu.Inner_BufferHelper.TARGET : AiActionMenu.Inner_BufferHelper.REVIEW;
      this.read_cE_StreamUtil.load();
      if (next.init()) {
         this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
      } else {
         this.init_V();
      }
   }

   private void sync_V() {
      this.read_Str = "";
      if (!update_Z()) {
         this.read_Str = "Join a world first";
      } else {
         if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.TARGET) {
            this.init_StreamUtil_CachePo = this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE
               ? this.read_Map.values().stream().findFirst().orElse(null)
               : this.init_StreamUtil_CachePo_List(this.read_List());
            if (this.init_StreamUtil_CachePo == null) {
               this.read_Str = "Choose a target first";
               return;
            }

            this.load_Obj = Minecraft.getInstance().level;
            if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM) {
               AiIntents.Inner_BufferHelper farmStep = this.init_ad_BufferHelper_Str_Str(
                  this.init_StreamUtil_CachePo.request().apply(null), this.init_StreamUtil_DataUti.load_Str
               );
               if (farmStep == null) {
                  this.read_Str = "Choose the crop again";
                  return;
               }

               this.write_Z = "build".equals(AiJson.init_Str_JsonObject(farmStep.arguments(), "action", "build"));
               this.load_StreamUtil_CachePo = null;
               this.write_Str = AiFarmArea.init_Str(AiJson.init_Str_JsonObject(farmStep.arguments(), "crop", "minecraft:wheat"));
               if (this.init_E != null) {
                  this.init_E.read_V();
               }

               this.init_E = null;
            }

            if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD) {
               AiIntents.Inner_BufferHelper step = this.init_ad_BufferHelper_Str_Str(
                  this.init_StreamUtil_CachePo.request().apply(null), this.init_StreamUtil_DataUti.load_Str
               );
               if (step == null) {
                  this.read_Str = "Schematic is no longer available";
                  return;
               }

               try {
                  this.init_bW = new AiSchematicPlacement(AiJson.init_Str_JsonObject(step.arguments(), "schematic", ""));
               } catch (RuntimeException e) {
                  this.read_Str = e.getMessage();
                  return;
               }
            }

            this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.OPTIONS;
         } else if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.OPTIONS) {
            try {
               if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM && this.write_Z && (this.init_E == null || !this.init_E.init_Z())) {
                  this.flush();
                  return;
               }

               this.init_ad_BufferHelper();
               if (this.init_bW != null) {
                  String issue = this.init_bW.sync_Str();
                  if (!issue.isEmpty()) {
                     this.read_Str = issue;
                     return;
                  }
               }

               this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.REVIEW;
            } catch (RuntimeException e) {
               this.read_Str = e.getMessage();
            }
         }

         this.init_V();
         this.read_cE_StreamUtil.load();
      }
   }

   private static AiActionMenu.Inner_CachePool init_StreamUtil_CachePo_Str_Item(String crop, Item icon) {
      return new AiActionMenu.Inner_CachePool(
         "Build " + crop + " farm",
         crop + " farm build",
         autismclient.util.DisplayItemStackFactory.init_ItemStack(icon),
         n -> "build a " + crop + " farm" + (n == null ? "" : " of " + n + " crops"),
         "crop",
         0,
         null
      );
   }

   private void flush() {
      if (!this.init_cF.init_Z() && !AiDirectOps.load_Z()) {
         try {
            AiIntents.Inner_BufferHelper step = this.init_ad_BufferHelper_Str_Str(
               this.init_StreamUtil_CachePo.request().apply(null), this.init_StreamUtil_DataUti.load_Str
            );
            if (step == null) {
               this.read_Str = "Choose the crop again";
               return;
            }

            String crop = AiJson.init_Str_JsonObject(step.arguments(), "crop", "minecraft:wheat");
            AiFarmArea.init_Str(AiJson.init_JsonObject_ObjArr("substrate", this.write_Str), crop);
            if (this.init_E == null || !this.init_E.init_Str().equals(crop) || !this.init_E.load_Str().equals(this.write_Str)) {
               this.init_E = new AiFarmPlacement(crop, this.write_Str);
            }

            aP.init_V_E_Runnable(this.init_E, () -> {});
         } catch (RuntimeException e) {
            this.read_Str = e.getMessage();
         }
      } else {
         this.read_Str = "Pause the current action before choosing farm corners";
      }
   }

   private void close() {
      this.read_Str = "";
      this.init_V();
      this.read_cE_StreamUtil.load();
      if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.REVIEW) {
         this.init_StreamUtil_BufferH = this.init_StreamUtil_DataUti.init() ? AiActionMenu.Inner_BufferHelper.OPTIONS : AiActionMenu.Inner_BufferHelper.HOME;
      } else if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.OPTIONS) {
         if (this.init_bW != null) {
            this.init_bW.read_V();
         }

         if (this.init_E != null) {
            this.init_E.read_V();
         }

         this.init_bW = null;
         this.init_E = null;
         this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.TARGET;
      } else {
         this.open();
      }
   }

   private void open() {
      if (this.init_bW != null) {
         this.init_bW.read_V();
      }

      if (this.init_E != null) {
         this.init_E.read_V();
      }

      this.init_bW = null;
      this.init_E = null;
      this.init_StreamUtil_BufferH = AiActionMenu.Inner_BufferHelper.HOME;
      this.init_StreamUtil_CachePo = null;
      this.load_Obj = null;
      this.read_Str = "";
      this.init_V();
      this.read_Map.clear();
      this.write_Map.clear();
      this.read_Z = false;
      this.update = List.of();
   }

   private AiIntents.Inner_BufferHelper init_ad_BufferHelper() {
      Integer amount = this.init_StreamUtil_DataUti.init_Z ? AiLocalRequest.init(this.load_BitSetUtil.init_Str(), this.write_I()) : null;
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.MINE) {
         List<JsonObject> targets = this.read_Map
            .values()
            .stream()
            .map(row -> this.init_ad_BufferHelper_Str_Str(row.request().apply(null), this.init_StreamUtil_DataUti.load_Str))
            .map(stepx -> {
               if (stepx == null) {
                  throw new IllegalArgumentException("A selected block is no longer available");
               }

               JsonObject argsx = stepx.arguments().deepCopy();
               String key = AiJson.init_Str_JsonObject(argsx, "block", "");
               Integer custom = AiLocalRequest.init(this.init_BitSetUtil_Str(key).init_Str(), 640);
               if (custom != null) {
                  argsx.addProperty("mineAmount", custom);
               }

               return argsx;
            })
            .toList();
         JsonObject args = AiLocalRequest.init(targets, amount);
         String label = "Mine "
            + args.getAsJsonObject("counts").entrySet().stream().map(e -> e.getValue().getAsInt() + " " + e.getKey()).collect(Collectors.joining(", "));
         return new AiIntents.Inner_BufferHelper(label, this.init_StreamUtil_DataUti.load_Str, args, label);
      }

      String text = this.init_StreamUtil_DataUti.init() ? this.init_StreamUtil_CachePo.request().apply(amount) : this.init_StreamUtil_DataUti.update_Str;
      AiIntents.Inner_BufferHelper step = this.init_ad_BufferHelper_Str_Str(text, this.init_StreamUtil_DataUti.load_Str);
      if (step == null) {
         throw new IllegalArgumentException("That option is unavailable. Choose the target again.");
      }

      JsonObject args = this.init_bW == null ? step.arguments().deepCopy() : this.init_bW.load_JsonObject_2();
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM && this.write_Z) {
         if (this.init_E == null || !this.init_E.init_Z()) {
            throw new IllegalArgumentException("Choose both farm corners first");
         }

         JsonObject selectedArea = this.init_E.init_JsonObject();
         selectedArea.entrySet().forEach(entry -> args.add(entry.getKey(), entry.getValue()));
         String crop = AiJson.init_Str_JsonObject(args, "crop", "minecraft:wheat");
         AiFarmArea.init_Str(args, crop);
         AiFarmArea.Inner_DataUtil area = AiFarmArea.init_C_DataUtil(args);
         String issue = area.validate(this.write_Str);
         if (!issue.isEmpty()) {
            throw new IllegalArgumentException(issue);
         }
      }

      if (this.read_Z()) {
         args.addProperty("additional", this.write_Z());
      }

      return new AiIntents.Inner_BufferHelper(text, this.init_StreamUtil_DataUti.load_Str, args, step.describe());
   }

   private void poll() {
      if (this.init_StreamUtil_BufferH == AiActionMenu.Inner_BufferHelper.REVIEW && update_Z()) {
         if (this.load_Obj != null && this.load_Obj != Minecraft.getInstance().level) {
            this.open();
            this.read_Str = "World changed. Choose the action again.";
         } else {
            try {
               if (this.init_bW != null) {
                  String issue = this.init_bW.sync_Str();
                  if (!issue.isEmpty()) {
                     this.read_Str = issue;
                     return;
                  }
               }

               AiIntents.Inner_BufferHelper step = this.init_ad_BufferHelper();
               if (this.read_Z() && this.write_Z()) {
                  JsonObject args = AiLocalRequest.init(
                     step.arguments(), AiItems.init_I(AiItems.load_af_DataUtil(AiJson.init_Str_JsonObject(step.arguments(), "item", ""))), this.write_I()
                  );
                  step = new AiIntents.Inner_BufferHelper(step.clause() + " more", step.capability(), args, step.describe());
               }

               String label = this.init_E != null
                  ? this.init_StreamUtil_CachePo.label() + " / " + this.init_E.write_Str()
                  : (
                     this.init_bW == null
                        ? step.clause()
                        : "Build "
                           + this.init_StreamUtil_CachePo.label()
                           + " at "
                           + this.load_Str()
                           + " ("
                           + this.init_bW.init_I()
                           + " degrees, mirror "
                           + this.init_bW.read_Str()
                           + ")"
                  );
               this.init_cc.init_V_Str_2(label, step.capability(), step.arguments());
               if (this.init_E != null) {
                  this.init_E.read_V();
                  this.init_E = null;
               }

               if (this.init_bW != null) {
                  this.init_bW.update_V();
                  this.init_bW = null;
               }

               this.open();
               this.init_Runnable.run();
               this.init_cF.init_V();
            } catch (RuntimeException e) {
               this.read_Str = e.getMessage();
            }
         }
      }
   }

   void read_V() {
      if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.FARM && this.write_Z) {
         this.flush();
      } else if (this.init_bW != null && this.init_bW.load_Z()) {
         aP.init_V_bW_Runnable(this.init_bW, () -> {});
      } else if (this.init_E != null) {
         aP.init_V_E_Runnable(this.init_E, () -> {});
      }
   }

   void write_V() {
      if (this.init_bW != null) {
         this.init_bW.flush();
      }
   }

   void update_V() {
      this.poll();
   }

   JsonObject init_JsonObject() {
      return this.init_E != null ? this.init_E.load_JsonObject() : (this.init_bW == null ? new JsonObject() : this.init_bW.init_JsonObject_2());
   }

   private boolean write_Z() {
      return this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.GET || this.load_Z;
   }

   private void push() {
      this.init_cc.update_V();
      this.init_cF.init_V();
   }

   private void pop() {
      if (!this.init_Z) {
         this.init_Z = true;
         CompletableFuture.<AiAttachments.Inner_DataUtil>supplyAsync(() -> {
            try {
               String path = AiAttachments.init();
               return path != null && !path.isBlank() ? AiAttachments.init(Path.of(path), AiSchematic.init()) : null;
            } catch (Exception e) {
               throw new CompletionException(e);
            }
         }).whenComplete((file, error) -> Minecraft.getInstance().execute(() -> {
            this.init_Z = false;
            if (error != null) {
               Throwable cause = error.getCause() == null ? error : error.getCause();
               autismclient.util.bi.write("Import failed: " + cause.getMessage());
            } else if (file != null) {
               this.init_Map.remove(AiActionMenu.Inner_DataUtil.BUILD);
               if (this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.BUILD) {
                  this.init_BitSetUtil.init_BitSetUtil_Str(file.name());
                  this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
               }
            }
         }));
      }
   }

   private static boolean update_Z() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null;
   }

   private AiActionMenu.Inner_CachePool init_StreamUtil_CachePo_List(List<AiActionMenu.Inner_CachePool> rows) {
      return rows.isEmpty() ? null : rows.get(Math.max(0, Math.min(rows.size() - 1, this.read_I)));
   }

   private String sync_Str() {
      return this.init_BitSetUtil.init_Str().trim().toLowerCase(Locale.ROOT);
   }

   private List<AiActionMenu.Inner_CachePool> read_List() {
      List<AiActionMenu.Inner_CachePool> base = update_Z() ? this.init_List_StreamUtil_DataUti(this.init_StreamUtil_DataUti) : null;
      if (base == null) {
         base = List.of();
      }

      if (base != this.load_List) {
         this.init_a.init_V_List(base);
         this.load_List = base;
      }

      String q = this.sync_Str();
      this.init_a.init_V_Str(q);
      List<AiActionMenu.Inner_CachePool> filtered = this.init_a.init_List();
      if (filtered != this.read_List || !q.equals(this.init_Str)) {
         List<AiActionMenu.Inner_CachePool> out = new ArrayList<>(filtered);
         if (!q.isEmpty()) {
            out.sort(Comparator.comparingInt(row -> init_I_Str_Str(row.label().toLowerCase(Locale.ROOT), q)));
         }

         AiActionMenu.Inner_CachePool place = this.init_StreamUtil_DataUti == AiActionMenu.Inner_DataUtil.GOTO
            ? this.init_StreamUtil_CachePo_Str(this.init_BitSetUtil.init_Str().trim())
            : null;
         if (place != null) {
            out.add(0, place);
         }

         this.write_List = List.copyOf(out);
         this.read_List = filtered;
         this.init_Str = q;
      }

      if (!q.equals(this.load_Str) || this.init_StreamUtil_DataUti != this.load_StreamUtil_DataUti) {
         this.read_I = 0;
         this.init_cE_StreamUtil.load();
         this.load_Str = q;
         this.load_StreamUtil_DataUti = this.init_StreamUtil_DataUti;
      }

      if (this.read_I >= this.write_List.size()) {
         this.read_I = Math.max(0, this.write_List.size() - 1);
      }

      return this.write_List;
   }

   private static int init_I_Str_Str(String label, String q) {
      return label.startsWith(q) ? 0 : (label.contains(" " + q) ? 1 : 2);
   }

   private AiActionMenu.Inner_CachePool init_StreamUtil_CachePo_Str(String typed) {
      if (!typed.isEmpty() && !typed.chars().noneMatch(Character::isDigit)) {
         String text = "go to " + typed;
         return this.init_ad_BufferHelper_Str_Str(text, AiActionMenu.Inner_DataUtil.GOTO.load_Str) == null
            ? null
            : new AiActionMenu.Inner_CachePool("Go to " + typed, "", autismclient.util.DisplayItemStackFactory.init_ItemStack(Items.COMPASS), n -> text, "", 0, "");
      } else {
         return null;
      }
   }

   private List<AiActionMenu.Inner_CachePool> init_List_StreamUtil_DataUti(AiActionMenu.Inner_DataUtil a) {
      Minecraft mc = Minecraft.getInstance();
      AiActionMenu.Inner_ConfigStore v = this.init_Map.get(a);
      long now = System.currentTimeMillis();
      Object world = mc.getConnection();
      if (v == null || v.init_Obj != world || init_Z_StreamUtil_DataUti(a, v, now)) {
         List<AiActionMenu.Inner_CachePool> previous = v != null && v.init_Obj == world ? v.init() : null;
         boolean recipes = a == AiActionMenu.Inner_DataUtil.GET
            || a == AiActionMenu.Inner_DataUtil.CRAFT
            || a == AiActionMenu.Inner_DataUtil.SMELT;
         boolean missing = recipes && (a == AiActionMenu.Inner_DataUtil.SMELT ? AiToolsSuggest.load() : AiToolsSuggest.init()).isEmpty();
         v = new AiActionMenu.Inner_ConfigStore(world, now, this.load(a), previous, missing);
         this.init_Map.put(a, v);
      }

      if (v.write == null) {
         this.init_V_StreamUtil_DataUti_StreamUtil_ConfigS(a, v);
      }

      return v.init();
   }

   private static boolean init_Z_StreamUtil_DataUti(AiActionMenu.Inner_DataUtil a, AiActionMenu.Inner_ConfigStore v, long now) {
      return v.write != null && now - v.init_J >= 2000L ? a == AiActionMenu.Inner_DataUtil.GOTO || a == AiActionMenu.Inner_DataUtil.BUILD || v.init_Z : false;
   }

   private void init_V_StreamUtil_DataUti_StreamUtil_ConfigS(AiActionMenu.Inner_DataUtil a, AiActionMenu.Inner_ConfigStore v) {
      long until = System.nanoTime() + 3000000L;
      boolean dedupe = a == AiActionMenu.Inner_DataUtil.GET || a == AiActionMenu.Inner_DataUtil.MINE;

      while (v.init_I < v.init_List.size()) {
         AiActionMenu.Inner_CachePool row = this.init_StreamUtil_CachePo_StreamUtil_DataUti(a, v.init_List.get(v.init_I++), dedupe ? v.init_Set : null);
         if (row != null) {
            v.read.add(row);
         }

         if (System.nanoTime() > until) {
            return;
         }
      }

      v.read.sort(Comparator.comparingInt(AiActionMenu.Inner_CachePool::order).thenComparing(rowx -> rowx.label().toLowerCase(Locale.ROOT)));
      v.write = List.copyOf(v.read);
   }

   private AiActionMenu.Inner_CachePool init_StreamUtil_CachePo_StreamUtil_DataUti(AiActionMenu.Inner_DataUtil a, AiActionMenu.Inner_StreamUtil c, Set<String> seen) {
      for (Function<Integer, String> form : c.forms()) {
         AiIntents.Inner_BufferHelper step = this.init_ad_BufferHelper_Str_Str(form.apply(null), a.load_Str);
         if (step != null && (c.expect() == null || init_Z_Str_Str(AiJson.init_Str_JsonObject(step.arguments(), c.key(), ""), c.expect()))) {
            if (seen != null && !seen.add(init_Str_ad_BufferHelper_Str(step, c.key()))) {
               return null;
            }

            int amount;
            try {
               amount = AiJson.init_I_JsonObject(step.arguments(), "count", -1);
            } catch (RuntimeException e) {
               amount = -1;
            }

            return new AiActionMenu.Inner_CachePool(
               c.label(),
               c.label() + " " + c.search(),
               c.icon(),
               form,
               amount > 0 ? Integer.toString(amount) : "Auto",
               c.order(),
               a == AiActionMenu.Inner_DataUtil.MINE ? AiJson.init_Str_JsonObject(step.arguments(), "block", "") : ""
            );
         }
      }

      return null;
   }

   private AiIntents.Inner_BufferHelper init_ad_BufferHelper_Str_Str(String text, String capability) {
      try {
         AiIntents.Inner_CachePool plan = AiIntents.init_ad_CachePool(text, this.init_ad_StreamUtil);
         if (plan.steps().size() == 1 && plan.handledAll()) {
            AiIntents.Inner_BufferHelper step = plan.steps().get(0);
            return capability.equals(step.capability()) && step.asks().isEmpty() ? step : null;
         } else {
            return null;
         }
      } catch (RuntimeException e) {
         return null;
      }
   }

   private static boolean init_Z_Str_Str(String a, String b) {
      return a.replace("minecraft:", "").equalsIgnoreCase(b.replace("minecraft:", ""));
   }

   private static String init_Str_ad_BufferHelper_Str(AiIntents.Inner_BufferHelper step, String key) {
      JsonObject args = step.arguments().deepCopy();
      args.remove("count");
      String value = key == null ? null : AiJson.init_Str_JsonObject(args, key, null);
      if (value != null) {
         try {
            AiItems.ItemSpec target = AiItems.init_af_DataUtil(value);
            args.addProperty(key, String.join(",", target.itemIds()) + "|" + String.join(",", target.blockIds()));
         } catch (RuntimeException var5) {
         }
      }

      return step.capability() + args;
   }

   private List<AiActionMenu.Inner_DataUtil> write_List() {
      Object level = Minecraft.getInstance().level;
      if (this.init_List != null && this.init_Obj == level) {
         return this.init_List;
      }

      List<AiActionMenu.Inner_DataUtil> out = new ArrayList<>();

      for (AiActionMenu.Inner_DataUtil a : AiActionMenu.Inner_DataUtil.values()) {
         try {
            if (AiDirectOps.init_AiCatalog_Entry(a.load_Str) == null) {
               continue;
            }
         } catch (RuntimeException e) {
            continue;
         }

         if (a.init() || this.init_ad_BufferHelper_Str_Str(a.update_Str, a.load_Str) != null) {
            out.add(a);
         }
      }

      this.init_Obj = level;
      if (out.remove(AiActionMenu.Inner_DataUtil.BUILD)) {
         out.add(Math.min(1, out.size()), AiActionMenu.Inner_DataUtil.BUILD);
      }

      return this.init_List = List.copyOf(out);
   }

   private List<AiActionMenu.Inner_StreamUtil> load(AiActionMenu.Inner_DataUtil a) {
      List<AiActionMenu.Inner_StreamUtil> out = new ArrayList<>();
      switch (a) {
         case GET:
            Set<String> craft = new HashSet<>(AiToolsSuggest.init());
            Set<String> smelt = new HashSet<>(AiToolsSuggest.load());
            Set<String> mined = AiObtain.init();

            for (Item item : BuiltInRegistries.ITEM) {
               if (item != Items.AIR) {
                  Identifier id = BuiltInRegistries.ITEM.getKey(item);
                  String shortId = AiToolsSuggest.init(id);
                  boolean route = craft.contains(shortId)
                     || smelt.contains(shortId)
                     || !AiItems.load(item).isEmpty()
                     || mined.contains(id.toString()) && AiItems.init(item).stream().anyMatch(AiToolsSuggest::init);
                  if (route) {
                     out.add(init_StreamUtil_StreamU_Str_2("get", item, "item"));
                  }
               }
            }

            init_V_List(out);
            List<AiActionMenu.Inner_StreamUtil> families = new ArrayList<>();

            for (String family : AiItems.init_List()) {
               families.add(init_StreamUtil_StreamU_Str("get", family, "item"));
            }

            init_V_List(families);
            out.addAll(families);
            break;
         case MINE:
            List<AiActionMenu.Inner_StreamUtil> familiesMine = new ArrayList<>();

            for (String id : AiToolsSuggest.write()) {
               Block block = init_Block_Str(id);
               if (block != null) {
                  out.add(init_StreamUtil_StreamU_Str_Block("mine", block));
               } else {
                  familiesMine.add(init_StreamUtil_StreamU_Str("mine", id, "block"));
               }
            }

            init_V_List(out);
            init_V_List(familiesMine);
            out.addAll(familiesMine);
            break;
         case CHOP:
            out.add(
               new AiActionMenu.Inner_StreamUtil(
                  "Nearest tree",
                  "tree log wood",
                  autismclient.util.DisplayItemStackFactory.init_ItemStack(Items.OAK_SAPLING),
                  List.of(n -> "chop " + init_Str_Integer(n) + "logs"),
                  "block",
                  null,
                  0
               )
            );

            for (String id : AiToolsSuggest.update()) {
               Block block = id.endsWith("_log") && !id.startsWith("stripped_") ? init_Block_Str(id) : null;
               if (block != null) {
                  Identifier key = BuiltInRegistries.BLOCK.getKey(block);
                  String species = key.getPath().substring(0, key.getPath().length() - 4).replace('_', ' ');
                  out.add(
                     new AiActionMenu.Inner_StreamUtil(
                        autismclient.util.RegistryDisplayNameCache.load(key.toString()),
                        species + " tree wood",
                        autismclient.util.DisplayItemStackFactory.init(block),
                        List.of(n -> "chop " + init_Str_Integer(n) + species + " logs"),
                        "block",
                        key.toString(),
                        1
                     )
                  );
               }
            }
            break;
         case CRAFT:
            for (String id : AiToolsSuggest.init()) {
               Item item = init_Item_Str(id);
               if (item != null) {
                  out.add(init_StreamUtil_StreamU_Str_2("craft", item, "item"));
               }
            }
            break;
         case SMELT:
            for (String id : AiToolsSuggest.load()) {
               Item item = init_Item_Str(id);
               if (item != null) {
                  out.add(init_StreamUtil_StreamU_Str_2("smelt", item, "item"));
               }
            }
            break;
         case GOTO:
            Minecraft mc = Minecraft.getInstance();

            for (AutismWaypoints.Waypoint wp : AutismWaypoints.init_AutismWaypoints().init_List_Str(AuditWaypointScope.current(mc))) {
               String name = wp.name();
               out.add(
                  new AiActionMenu.Inner_StreamUtil(
                     name + " (" + wp.x() + " " + wp.y() + " " + wp.z() + ")",
                     name,
                     autismclient.util.DisplayItemStackFactory.init_ItemStack(wp.death() ? Items.RECOVERY_COMPASS : Items.COMPASS),
                     List.of(n -> "go to " + name),
                     null,
                     null,
                     1
                  )
               );
            }
            break;
         case FIGHT:
            for (String id : AiToolsSuggest.sync()) {
               if (id.equals("hostiles")) {
                  out.add(
                     new AiActionMenu.Inner_StreamUtil(
                        "Hostile mobs",
                        "hostiles monsters",
                        autismclient.util.DisplayItemStackFactory.init_ItemStack(Items.ZOMBIE_HEAD),
                        List.of(n -> "fight " + init_Str_Integer(n) + "hostiles"),
                        "target",
                        "hostiles",
                        0
                     )
                  );
               } else {
                  Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
                  if (key != null) {
                     String words = key.getPath().replace('_', ' ');
                     EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
                     out.add(
                        new AiActionMenu.Inner_StreamUtil(
                           autismclient.util.RegistryDisplayNameCache.read(key.toString()),
                           words,
                           type == null ? ItemStack.EMPTY : init_ItemStack_EntityType(type),
                           List.of(n -> "fight " + init_Str_Integer(n) + words),
                           "target",
                           key.toString(),
                           1
                        )
                     );
                  }
               }
            }
            break;
         case FARM:
            for (String option : AiIntents.init_List) {
               String crop = option.toLowerCase(Locale.ROOT);
               String one = AiIntents.sync(crop);
               String id = AiIntents.update(crop);
               Block block = id == null ? null : init_Block_Str(id);
               ItemStack icon = block == null ? ItemStack.EMPTY : autismclient.util.DisplayItemStackFactory.init(block);
               out.add(
                  new AiActionMenu.Inner_StreamUtil(
                     "Build " + one + " farm",
                     crop + " farm build",
                     icon,
                     List.of(n -> "build a " + one + " farm" + (n == null ? "" : " of " + n + " crops")),
                     "crop",
                     id,
                     0
                  )
               );
               out.add(
                  new AiActionMenu.Inner_StreamUtil(
                     "Harvest " + crop, crop + " harvest", icon, List.of(n -> "harvest " + init_Str_Integer(n) + crop), "crop", id, 1
                  )
               );
            }
            break;
         case BUILD:
            Path dir = AiSchematic.init();
            if (Files.isDirectory(dir)) {
               try (Stream<Path> files = Files.list(dir)) {
                  files.filter(x$0 -> Files.isRegularFile(x$0))
                     .map(p -> p.getFileName().toString())
                     .filter(AiActionMenu::init_Z_Str)
                     .forEach(
                        namex -> out.add(
                           new AiActionMenu.Inner_StreamUtil(namex, namex, ItemStack.EMPTY, List.of(n -> "build \"" + namex + "\""), "schematic", namex, 1)
                        )
                     );
               } catch (Exception var18) {
               }
            }
      }

      return out;
   }

   private static boolean init_Z_Str(String name) {
      String lower = name.toLowerCase(Locale.ROOT);

      for (String extension : AiSchematic.init_StrArr) {
         if (lower.endsWith(extension)) {
            return true;
         }
      }

      return false;
   }

   private static String init_Str_Integer(Integer n) {
      return n == null ? "" : n + " ";
   }

   private static void init_V_List(List<AiActionMenu.Inner_StreamUtil> list) {
      list.sort(Comparator.comparing(c -> c.label().toLowerCase(Locale.ROOT)));
   }

   private static AiActionMenu.Inner_StreamUtil init_StreamUtil_StreamU_Str_2(String verb, Item item, String key) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      String path = id.getPath();
      String words = path.replace('_', ' ');
      return new AiActionMenu.Inner_StreamUtil(
         autismclient.util.RegistryDisplayNameCache.init(id.toString()),
         words,
         autismclient.util.DisplayItemStackFactory.init_ItemStack(item),
         List.of(n -> verb + " " + init_Str_Integer(n) + words, n -> verb + " " + init_Str_Integer(n) + path),
         key,
         id.toString(),
         1
      );
   }

   private static AiActionMenu.Inner_StreamUtil init_StreamUtil_StreamU_Str_Block(String verb, Block block) {
      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      String path = id.getPath();
      String words = path.replace('_', ' ');
      return new AiActionMenu.Inner_StreamUtil(
         autismclient.util.RegistryDisplayNameCache.load(id.toString()),
         words,
         autismclient.util.DisplayItemStackFactory.init(block),
         List.of(n -> verb + " " + init_Str_Integer(n) + words, n -> verb + " " + init_Str_Integer(n) + path),
         "block",
         id.toString(),
         1
      );
   }

   private static AiActionMenu.Inner_StreamUtil init_StreamUtil_StreamU_Str(String verb, String family, String key) {
      String words = family.replace('_', ' ');
      ItemStack icon = ItemStack.EMPTY;

      try {
         AiItems.ItemSpec target = AiItems.init_af_DataUtil(family);
         if (!target.items().isEmpty()) {
            icon = autismclient.util.DisplayItemStackFactory.init_ItemStack(target.items().iterator().next());
         }
      } catch (RuntimeException var6) {
      }

      return new AiActionMenu.Inner_StreamUtil(
         "Any " + words,
         words + " any",
         icon,
         List.of(n -> verb + " " + init_Str_Integer(n) + words, n -> verb + " " + init_Str_Integer(n) + family),
         key,
         null,
         1
      );
   }

   private static Item init_Item_Str(String id) {
      Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
      return key == null ? null : BuiltInRegistries.ITEM.getOptional(key).filter(item -> item != Items.AIR).orElse(null);
   }

   private static Block init_Block_Str(String id) {
      Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
      return key == null ? null : BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
   }

   private static ItemStack init_ItemStack_EntityType(EntityType<?> type) {
      if (load_Map == null) {
         Map<EntityType<?>, ItemStack> map = new HashMap<>();

         for (Item item : BuiltInRegistries.ITEM) {
            TypedEntityData<EntityType<?>> data = item.components().get(DataComponents.ENTITY_DATA);
            if (data != null) {
               map.putIfAbsent(data.type(), autismclient.util.DisplayItemStackFactory.init_ItemStack(item));
            }
         }

         load_Map = map;
      }

      return load_Map.getOrDefault(type, ItemStack.EMPTY);
   }

   enum Inner_BufferHelper {
      HOME,
      TARGET,
      OPTIONS,
      REVIEW;
   }

   private record Inner_CachePool(String label, String search, ItemStack icon, Function<Integer, String> request, String fallback, int order, String mineKey) {
   }

   private static final class Inner_ConfigStore {
      final Object init_Obj;
      final long init_J;
      final List<AiActionMenu.Inner_StreamUtil> init_List;
      final List<AiActionMenu.Inner_CachePool> load;
      final List<AiActionMenu.Inner_CachePool> read = new ArrayList<>();
      final Set<String> init_Set = new HashSet<>();
      final boolean init_Z;
      int init_I;
      List<AiActionMenu.Inner_CachePool> write;

      Inner_ConfigStore(
         Object world, long builtAt, List<AiActionMenu.Inner_StreamUtil> candidates, List<AiActionMenu.Inner_CachePool> previous, boolean recipesMissing
      ) {
         this.init_Obj = world;
         this.init_J = builtAt;
         this.init_List = candidates;
         this.load = previous;
         this.init_Z = recipesMissing;
      }

      List<AiActionMenu.Inner_CachePool> init() {
         return this.write != null ? this.write : this.load;
      }
   }

   enum Inner_DataUtil {
      GET("Get", "game:obtain", true, "Search items", "Obtain any item"),
      MINE("Mine", "game:mine", true, "Search blocks", "Mine nearby block types"),
      CHOP("Chop", "game:chop", true, "Search trees", "Fell trees for logs"),
      CRAFT("Craft", "game:craft", true, "Search recipes", "Craft an item"),
      SMELT("Smelt", "game:smelt_auto", true, "Search smelt results", "Smelt in a furnace"),
      GOTO("Go to", "game:goto", false, "Waypoint or x y z", "Walk to a place"),
      FIGHT("Fight", "game:fight", true, "Search mobs", "Fight a mob type"),
      FARM("Farm", "game:farm", true, "Search crops", "Build or harvest farms"),
      BUILD("Schematic", "game:build", false, "Search schematics", "Preview and build a schematic"),
      EAT("Eat", "game:eat", "eat", "Eat the best food"),
      SLEEP("Sleep", "game:sleep", "sleep", "Sleep in a bed"),
      FISH("Fish", "game:fish", "fish", "Fish until a catch"),
      TIDY("Tidy", "game:tidy", "clean my inventory", "Drop junk, keep valuables"),
      SURFACE("Surface", "game:surface", "surface", "Climb to the surface"),
      RECOVER("Recover", "game:recover", "recover my items", "Pick up death drops"),
      BEAT("Beat the game", "game:speedrun", "beat the game", "Play until the dragon dies");

      final String init_Str;
      final String load_Str;
      final String read_Str;
      final String write_Str;
      final String update_Str;
      final boolean init_Z;

      Inner_DataUtil(String label, String capability, boolean counted, String hint, String tip) {
         this.init_Str = label;
         this.load_Str = capability;
         this.init_Z = counted;
         this.read_Str = hint;
         this.write_Str = tip;
         this.update_Str = null;
      }

      Inner_DataUtil(String label, String capability, String fixed, String tip) {
         this.init_Str = label;
         this.load_Str = capability;
         this.init_Z = false;
         this.read_Str = "";
         this.write_Str = tip;
         this.update_Str = fixed;
      }

      boolean init() {
         return this.update_Str == null;
      }
   }

   private record Inner_StreamUtil(String label, String search, ItemStack icon, List<Function<Integer, String>> forms, String key, String expect, int order) {
   }
}
