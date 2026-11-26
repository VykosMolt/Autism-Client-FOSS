package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.cA;
import autismclient.util.GuiIconAtlas;
import autismclient.util.UiColorPalette;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class PluginLibraryScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 440;
   private static final int load_I = 12;
   private static final int read_I = 3;
   private final Screen init_Screen;
   private final List<autismclient.gui.f.b.OverlayActionButton> init_List = new ArrayList<>();
   private final List<PluginLibraryScreen.Inner_CachePool> load_List = new ArrayList<>();
   private final List<PluginLibraryScreen.Inner_StreamUtil> read_List = new ArrayList<>();
   private final Set<String> init_Set = new HashSet<>();
   private EditBox init_EditBox;
   private String init_Str = "";
   private PluginLibraryScreen.Inner_DataUtil init_MapStore_DataUtil = PluginLibraryScreen.Inner_DataUtil.BY_SERVER;
   private List<PluginLibraryScreen.Inner_StreamUtil> write_List = List.of();
   private int write_I;
   private int update;
   private boolean init_Z;
   private int sync;
   private long init_J = -9223372036854775808L;

   public PluginLibraryScreen(Screen parent) {
      super(Component.literal("Plugin Library"));
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      this.init_EditBox = new MsdfEditBox(this.font, this.put(), this.get(), this.add(), this.sync(), Component.literal("Search"));
      this.init_EditBox.setHint(Component.literal(this.init_Str_5()));
      this.init_EditBox.setMaxLength(128);
      this.init_EditBox.setValue(this.init_Str);
      this.init_EditBox.setResponder(value -> {
         this.init_Str = init_Str_Str(value);
         this.update = 0;
         this.load_V_13();
      });
      this.addRenderableWidget(this.init_EditBox);
      this.init_J = AutismConfig.getGlobal().pluginScanRevision();
      this.load_V_13();
   }

   @Override
   public void tick() {
      long rev = AutismConfig.getGlobal().pluginScanRevision();
      if (rev != this.init_J) {
         this.init_J = rev;
         this.load_V_13();
      }
   }

   private String init_Str_5() {
      return this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_SERVER ? "Search servers or plugins..." : "Search plugins...";
   }

   private void init_V_MapStore_DataUtil(PluginLibraryScreen.Inner_DataUtil next) {
      if (this.init_MapStore_DataUtil != next) {
         this.init_MapStore_DataUtil = next;
         this.init_Set.clear();
         this.update = 0;
         if (this.init_EditBox != null) {
            this.init_EditBox.setHint(Component.literal(this.init_Str_5()));
         }

         this.load_V_13();
      }
   }

   private void init_V_Str_5(String key) {
      if (!this.init_Set.remove(key)) {
         this.init_Set.add(key);
      }

      this.load_V_13();
   }

   private List<PluginLibraryScreen.Inner_ConfigStore> init_List_7() {
      Map<String, PluginLibraryScreen.Inner_ConfigStore> byAddress = new LinkedHashMap<>();
      Map<String, AutismConfig.PluginScanCacheEntry> all = AutismConfig.getGlobal().allPluginScans();

      for (Entry<String, AutismConfig.PluginScanCacheEntry> item : all.entrySet()) {
         AutismConfig.PluginScanCacheEntry entry = item.getValue();
         if (entry != null && entry.plugins != null && !entry.plugins.isEmpty()) {
            String address = entry.serverAddress != null && !entry.serverAddress.isBlank() ? entry.serverAddress : load_Str_Str_4(item.getKey());
            if (address != null && !address.isBlank()) {
               String lower = address.toLowerCase(Locale.ROOT);
               PluginLibraryScreen.Inner_ConfigStore existing = byAddress.get(lower);
               if (existing == null || existing.init_J < entry.scannedAtMs) {
                  String name = entry.serverName != null && !entry.serverName.isBlank() ? entry.serverName : address;
                  byAddress.put(lower, new PluginLibraryScreen.Inner_ConfigStore(address, name, init_List_List(entry.plugins), entry.scannedAtMs));
               }
            }
         }
      }

      List<PluginLibraryScreen.Inner_ConfigStore> servers = new ArrayList<>(byAddress.values());
      servers.sort(Comparator.<PluginLibraryScreen.Inner_ConfigStore>comparingLong(r -> r.init_J).reversed());
      return servers;
   }

   private static String load_Str_Str_4(String key) {
      if (key == null) {
         return "";
      }

      int cut = key.lastIndexOf(124);
      return cut > 0 ? key.substring(0, cut) : key;
   }

   private static List<String> init_List_List(List<String> plugins) {
      Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      for (String p : plugins) {
         if (p != null && !p.isBlank()) {
            set.add(p.trim());
         }
      }

      return new ArrayList<>(set);
   }

   private List<PluginLibraryScreen.Inner_StreamUtil> load_List_3() {
      String query = this.init_Str.toLowerCase(Locale.ROOT);
      List<PluginLibraryScreen.Inner_ConfigStore> servers = this.init_List_7();
      return this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_SERVER ? this.init_List_List_Str(servers, query) : this.load(servers, query);
   }

   private List<PluginLibraryScreen.Inner_StreamUtil> init_List_List_Str(List<PluginLibraryScreen.Inner_ConfigStore> servers, String query) {
      List<PluginLibraryScreen.Inner_StreamUtil> rows = new ArrayList<>();

      for (PluginLibraryScreen.Inner_ConfigStore server : servers) {
         boolean nameHit = query.isEmpty() || init_Z_Str_Str(server.load, query) || init_Z_Str_Str(server.init_Str, query);
         boolean pluginHit = false;
         if (!query.isEmpty()) {
            for (String p : server.init_List) {
               if (init_Z_Str_Str(p, query)) {
                  pluginHit = true;
                  break;
               }
            }
         }

         if (query.isEmpty() || nameHit || pluginHit) {
            String key = "srv:" + server.init_Str.toLowerCase(Locale.ROOT);
            boolean open = this.init_Set.contains(key);
            PluginLibraryScreen.Inner_StreamUtil row = PluginLibraryScreen.Inner_StreamUtil.init(
               PluginLibraryScreen.Inner_BufferHelper.SERVER,
               key,
               server.read,
               server.init_List.size() + (server.init_List.size() == 1 ? " plugin  " : " plugins  ") + init_Str_J(server.init_J),
               open
            );
            row.write = server.init_Str;
            row.init_List = server.init_List;
            rows.add(row);
            if (open) {
               List<String> shown = server.init_List;
               if (!query.isEmpty()) {
                  List<String> matched = new ArrayList<>();

                  for (String plugin : server.init_List) {
                     if (init_Z_Str_Str(plugin, query)) {
                        matched.add(plugin);
                     }
                  }

                  if (!matched.isEmpty()) {
                     shown = matched;
                  }
               }

               for (String plugin : shown) {
                  rows.add(PluginLibraryScreen.Inner_StreamUtil.init(PluginLibraryScreen.Inner_BufferHelper.SERVER_PLUGIN, plugin, ""));
               }
            }
         }
      }

      this.write_I = rows.isEmpty() ? 0 : (int)rows.stream().filter(r -> r.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER).count();
      return rows;
   }

   private List<PluginLibraryScreen.Inner_StreamUtil> load(List<PluginLibraryScreen.Inner_ConfigStore> servers, String query) {
      Map<String, List<PluginLibraryScreen.Inner_ConfigStore>> byPlugin = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (PluginLibraryScreen.Inner_ConfigStore server : servers) {
         for (String plugin : server.init_List) {
            if (query.isEmpty() || init_Z_Str_Str(plugin, query)) {
               byPlugin.computeIfAbsent(plugin, k -> new ArrayList<>()).add(server);
            }
         }
      }

      List<Entry<String, List<PluginLibraryScreen.Inner_ConfigStore>>> ordered = new ArrayList<>(byPlugin.entrySet());
      ordered.sort(
         Comparator.<Entry<String, List<PluginLibraryScreen.Inner_ConfigStore>>>comparingInt(e -> e.getValue().size())
            .reversed()
            .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT))
      );
      List<PluginLibraryScreen.Inner_StreamUtil> rows = new ArrayList<>();

      for (Entry<String, List<PluginLibraryScreen.Inner_ConfigStore>> entry : ordered) {
         int count = entry.getValue().size();
         String key = "plg:" + entry.getKey().toLowerCase(Locale.ROOT);
         boolean open = this.init_Set.contains(key);
         PluginLibraryScreen.Inner_StreamUtil row = PluginLibraryScreen.Inner_StreamUtil.init(
            PluginLibraryScreen.Inner_BufferHelper.PLUGIN, key, entry.getKey(), "on " + count + (count == 1 ? " server" : " servers"), open
         );
         row.load_List = entry.getValue();
         rows.add(row);
         if (open) {
            for (PluginLibraryScreen.Inner_ConfigStore server : entry.getValue()) {
               rows.add(PluginLibraryScreen.Inner_StreamUtil.init(PluginLibraryScreen.Inner_BufferHelper.PLUGIN_SERVER, server.read, init_Str_J(server.init_J)));
            }
         }
      }

      this.write_I = ordered.size();
      return rows;
   }

   private static boolean init_Z_Str_Str(String value, String queryLower) {
      if (queryLower.isEmpty()) {
         return true;
      }

      if (value == null) {
         return false;
      }

      String v = value.toLowerCase(Locale.ROOT);
      if (v.contains(queryLower)) {
         return true;
      }

      int vi = 0;
      int qi = 0;
      int misses = 0;
      int maxMisses = Math.max(1, queryLower.length() / 3);

      while (vi < v.length() && qi < queryLower.length()) {
         if (v.charAt(vi) == queryLower.charAt(qi)) {
            qi++;
         } else if (qi > 0) {
            misses++;
         }

         if (misses > maxMisses) {
            return false;
         }

         vi++;
      }

      return qi == queryLower.length();
   }

   private static String init_Str_J(long ms) {
      if (ms <= 0L) {
         return "unknown";
      }

      long d = System.currentTimeMillis() - ms;
      if (d < 60000L) {
         return "just now";
      }

      long mins = d / 60000L;
      if (mins < 60L) {
         return mins + "m ago";
      }

      long hrs = mins / 60L;
      if (hrs < 24L) {
         return hrs + "h ago";
      }

      long days = hrs / 24L;
      if (days < 30L) {
         return days + "d ago";
      }

      long months = days / 30L;
      return months < 12L ? months + "mo ago" : months / 12L + "y ago";
   }

   private void load_V_13() {
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      int ctl = this.sync();
      this.init_List.add(autismclient.gui.f.b.OverlayActionButton.init(this.pop(), this.run() + 4, this.read(), ctl, Component.literal("Back"), b -> this.onClose()));
      int toggleY = this.get();
      int toggleW = this.write();
      int toggleX = this.pop();
      this.init_List
         .add(
            autismclient.gui.f.b.OverlayActionButton.init(
                  toggleX, toggleY, toggleW, ctl, Component.literal("By Server"), b -> this.init_V_MapStore_DataUtil(PluginLibraryScreen.Inner_DataUtil.BY_SERVER)
               )
               .init_ByteReader_ByteReader_StreamU(
                  this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_SERVER
                     ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS
                     : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY
               )
         );
      this.init_List
         .add(
            autismclient.gui.f.b.OverlayActionButton.init(
                  toggleX + toggleW + 2,
                  toggleY,
                  toggleW,
                  ctl,
                  Component.literal("By Plugin"),
                  b -> this.init_V_MapStore_DataUtil(PluginLibraryScreen.Inner_DataUtil.BY_PLUGIN)
               )
               .init_ByteReader_ByteReader_StreamU(
                  this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_PLUGIN
                     ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS
                     : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY
               )
         );
      this.write_List = this.load_List_3();
      int maxScroll = this.init_I_I_7(this.write_List.size());
      this.update = Math.max(0, Math.min(this.update, maxScroll));
      int pitch = this.close();
      int rowH = this.flush();
      int top = this.next();
      int bottom = this.prev();
      int btnH = this.open();
      int copyW = this.update();
      int firstVisible = this.update / pitch;
      int rowY = top - this.update % pitch;

      for (int i = firstVisible; i < this.write_List.size() && rowY < bottom; rowY += pitch) {
         if (rowY + rowH > top) {
            PluginLibraryScreen.Inner_StreamUtil row = this.write_List.get(i);
            row.init_I = rowY;
            this.read_List.add(row);
            boolean fullyVisible = rowY >= top && rowY + rowH <= bottom;
            boolean parent = row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER || row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.PLUGIN;
            if (parent && fullyVisible) {
               int btnY = rowY + (rowH - btnH) / 2;
               int cursor = this.data() - 1;
               if (row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER) {
                  cursor -= btnH;
                  this.init_List
                     .add(
                        autismclient.gui.f.b.OverlayActionButton.init(cursor, btnY, btnH, btnH, Component.empty(), b -> this.init_V_MapStore_StreamUti(row))
                           .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER)
                           .init_ByteReader_Identifier(GuiIconAtlas.open)
                     );
                  cursor -= 2;
               }

               cursor -= copyW;
               this.init_List
                  .add(
                     autismclient.gui.f.b.OverlayActionButton.init(cursor, btnY, copyW, btnH, Component.literal("Copy"), b -> this.load_V_MapStore_StreamUti(row))
                        .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY)
                  );
               this.load_List.add(new PluginLibraryScreen.Inner_CachePool(this.size(), rowY, Math.max(1, cursor - 4 - this.size()), rowH, row.init_Str));
            }
         }

         i++;
      }
   }

   private int read() {
      return this.init_I_Str_11("Back");
   }

   private int init_I_Str_11(String label) {
      return autismclient.gui.f.UiDensityMetrics.load(k.init(this.font, label, init_Identifier(), 0));
   }

   private int write() {
      return Math.max(this.init_I_Str_11("By Server"), this.init_I_Str_11("By Plugin"));
   }

   private int update() {
      return this.init_I_Str_11("Copy");
   }

   private void init_V_MapStore_StreamUti(PluginLibraryScreen.Inner_StreamUtil row) {
      if (row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER && row.write != null) {
         AutismConfig.getGlobal().removePluginScan(row.write);
         this.init_Set.remove(row.init_Str);
         this.init_J = AutismConfig.getGlobal().pluginScanRevision();
         this.load_V_13();
      }
   }

   private void load_V_MapStore_StreamUti(PluginLibraryScreen.Inner_StreamUtil row) {
      String text;
      String note;
      if (row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER && row.init_List != null) {
         text = String.join("\n", row.init_List);
         note = "Copied " + row.init_List.size() + " plugins from " + row.load_Str;
      } else {
         if (row.init_MapStore_BufferHel != PluginLibraryScreen.Inner_BufferHelper.PLUGIN || row.load_List == null) {
            return;
         }

         List<String> names = new ArrayList<>();

         for (PluginLibraryScreen.Inner_ConfigStore s : row.load_List) {
            names.add(s.read);
         }

         text = String.join("\n", names);
         note = "Copied " + names.size() + " servers running " + row.load_Str;
      }

      if (!text.isBlank() && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(text);
         this.init_V_Str_I(note, GuiColorPalette.open);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -15987181);
         this.init_V_GuiGraphicsExtract_2(graphics, this.push(), this.run(), this.poll(), this.set(), -401402080);
         this.init_V_GuiGraphicsExtract_2(graphics, this.reset(), this.clear(), this.start(), this.stop(), -1206840035);
         String count = this.write_I
            + (
               this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_SERVER
                  ? (this.write_I == 1 ? " server" : " servers")
                  : (this.write_I == 1 ? " plugin" : " plugins")
            );
         int countW = k.init(this.font, count, init_Identifier(), GuiColorPalette.close);
         int countX = this.pop() + this.tick_I_9() - countW;
         int headerTextY = this.init_I_I_I_5(this.run() + 4, this.sync());
         int titleX = this.pop() + this.read() + 4;
         this.init(graphics, "Plugin Library", titleX, headerTextY, GuiColorPalette.flush, Math.max(1, countX - 8 - titleX));
         this.init(graphics, count, countX, headerTextY, GuiColorPalette.close, countW + 4);
         String header = this.init_MapStore_DataUtil == PluginLibraryScreen.Inner_DataUtil.BY_SERVER ? "Servers you have scanned" : "Plugin -> servers running it";
         this.init(graphics, header, this.reset() + 4, this.init_I_I_I_5(this.clear() + 4, 12), GuiColorPalette.close, this.start() - 8);
         String queryLower = this.init_Str.toLowerCase(Locale.ROOT);
         autismclient.gui.f.UiClipStack.init_HashUtil()
            .init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.size(), this.next(), Math.max(0, this.data() - this.size()), Math.max(0, this.prev() - this.next()))
            );

         try {
            if (this.write_List.isEmpty()) {
               this.init(
                  graphics, this.load_Str_2(), this.size() + 4, this.init_I_I_I_5(this.next(), this.flush()), GuiColorPalette.close, this.data() - this.size() - 4
               );
            } else {
               for (PluginLibraryScreen.Inner_StreamUtil row : this.read_List) {
                  this.init(graphics, row, mx, my, queryLower);
               }
            }
         } finally {
            autismclient.gui.f.UiClipStack.init_HashUtil().init(graphics);
         }

         for (autismclient.gui.f.b.OverlayActionButton b : this.init_List) {
            autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, b, mx, my);
         }

         if (!this.init_Z_6()) {
            autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_I_3(this.write_List.size());
            autismclient.gui.f.b.ScrollbarMetricsHelper.init(graphics, sb, sb.init(mx, my), this.init_Z);
         }

         super.extractRenderState(graphics, mx, my, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private String load_Str_2() {
      return this.init_Str.isEmpty()
         ? "No saved scans yet. Join a server with Auto Probe on to record its plugins."
         : "Nothing matches \"" + this.init_Str + "\".";
   }

   private void init(GuiGraphicsExtractor graphics, PluginLibraryScreen.Inner_StreamUtil row, int mx, int my, String queryLower) {
      boolean child = row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER_PLUGIN || row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.PLUGIN_SERVER;
      int x = this.size();
      int y = row.init_I;
      int w = this.data() - this.size();
      int h = this.flush();
      boolean hovered = my >= y && my < y + h && mx >= x && mx < this.data();
      if (!child) {
         int fill = hovered ? 620756991 : 352321535;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), fill);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 2, h), row.init_Z ? GuiColorPalette.sync : 872415231);
      }

      int textY = this.init_I_I_I_5(y, h);
      int textLeft = x + 6 + (child ? 12 : 0);
      if (!child) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + 5, y + (h - 8) / 2, 8, 8), row.init_Z, GuiColorPalette.close);
         textLeft += 10;
      } else {
         this.init(graphics, "-", x + 6 + 12 - 8, textY, GuiColorPalette.close, 8);
      }

      int baseColor = child ? -3158065 : GuiColorPalette.flush;
      int secondaryColor = GuiColorPalette.close;
      int secondaryW = row.read.isEmpty() ? 0 : k.init(this.font, row.read, init_Identifier(), secondaryColor);
      int rowActionReserve = child ? 0 : this.update() + 2 + (row.init_MapStore_BufferHel == PluginLibraryScreen.Inner_BufferHelper.SERVER ? this.open() + 2 : 0);
      int rightLimit = this.data() - rowActionReserve - 4;
      int secondaryX = Math.max(textLeft + 20, rightLimit - secondaryW);
      int primaryMax = Math.max(20, (row.read.isEmpty() ? rightLimit : secondaryX - 8) - textLeft);
      this.init(graphics, row.load_Str, textLeft, textY, primaryMax, baseColor, queryLower);
      if (!row.read.isEmpty() && secondaryX > textLeft) {
         this.init(graphics, row.read, secondaryX, textY, secondaryColor, secondaryW + 2);
      }
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int baseColor, String queryLower) {
      Identifier font = init_Identifier();
      int idx = queryLower.isEmpty() ? -1 : text.toLowerCase(Locale.ROOT).indexOf(queryLower);
      if (idx >= 0 && k.init(this.font, text, font, baseColor) <= maxWidth) {
         String pre = text.substring(0, idx);
         String mid = text.substring(idx, idx + queryLower.length());
         String post = text.substring(idx + queryLower.length());
         int accent = UiColorPalette.init();
         k.init(graphics, this.font, pre, font, baseColor, x, y, false);
         int midX = x + k.init(this.font, pre, font, baseColor);
         k.init(graphics, this.font, mid, font, accent, midX, y, false);
         k.init(graphics, this.font, post, font, baseColor, midX + k.init(this.font, mid, font, accent), y, false);
      } else {
         k.init(graphics, this.font, text, font, baseColor, x, y, maxWidth, false);
      }
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      k.init(graphics, this.font, text, init_Identifier(), color, x, y, Math.max(1, maxWidth), false);
   }

   private static Identifier init_Identifier() {
      return init_CharEncoder.init_Identifier_l(l.read);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.init_Z_6()) {
         return super.mouseClicked(event, doubleClick);
      }

      MouseButtonEvent ve = init_MouseButtonEvent_MouseButtonEvent(event);
      double mx = ve.x();
      double my = ve.y();
      if (Compat263.mouseEventGlfwButton(ve) == 0 && !this.init_Z_6()) {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_I_3(this.write_List.size());
         if (sb.init_Z() && sb.init(mx, my)) {
            this.init_Z = true;
            this.sync = sb.load(mx, my) ? (int)Math.round(my) - sb.update() : sb.sync() / 2;
            this.update = autismclient.gui.f.b.ScrollbarMetricsHelper.init(sb, my, this.sync);
            this.load_V_13();
            return true;
         }
      }

      for (autismclient.gui.f.b.OverlayActionButton b : this.init_List) {
         if (autismclient.gui.f.b.OverlayActionButton.init(b, mx, my, Compat263.mouseEventGlfwButton(ve))) {
            return true;
         }
      }

      if (Compat263.mouseEventGlfwButton(ve) == 0) {
         for (PluginLibraryScreen.Inner_CachePool hit : this.load_List) {
            if (hit.init(mx, my)) {
               this.init_V_Str_5(hit.init_Str);
               return true;
            }
         }
      }

      return super.mouseClicked(ve, doubleClick);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      MouseButtonEvent ve = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_I_3(this.write_List.size());
         this.update = autismclient.gui.f.b.ScrollbarMetricsHelper.init(sb, ve.y(), this.sync);
         this.load_V_13();
         return true;
      } else {
         return super.mouseDragged(ve, cA.init_D_D(deltaX), cA.init_D_D(deltaY));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.init_Z_6()) {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      } else {
         double vy = cA.init_D_D(mouseY);
         double vx = cA.init_D_D(mouseX);
         if (!(vx < this.reset()) && !(vx > this.reset() + this.start()) && !(vy < this.next()) && !(vy > this.prev())) {
            int maxScroll = this.init_I_I_7(this.write_List.size());
            int next = this.update - (int)Math.signum(scrollY) * this.close();
            this.update = Math.max(0, Math.min(next, maxScroll));
            this.load_V_13();
            return true;
         } else {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
         }
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private int sync() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int flush() {
      return autismclient.gui.f.UiDensityMetrics.init(14);
   }

   private int close() {
      return this.flush() + 2;
   }

   private int open() {
      return this.flush() - 2;
   }

   private int init_I_I_I_5(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).init_I(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int poll() {
      return autismclient.gui.f.c.PanelLayoutMath.init(this.init_I(), 6, 440);
   }

   private int push() {
      return autismclient.gui.f.c.PanelLayoutMath.load(this.init_I(), this.poll(), 6);
   }

   private int pop() {
      return this.push() + 4;
   }

   private int tick_I_9() {
      return Math.max(1, this.poll() - 8);
   }

   private int run() {
      return 6;
   }

   private int get() {
      return this.run() + 4 + this.sync() + 2;
   }

   private int set() {
      return this.get() + this.sync() + 4 - this.run();
   }

   private int put() {
      return this.pop() + this.write() * 2 + 2 + 4;
   }

   private int add() {
      return Math.max(40, this.pop() + this.tick_I_9() - this.put());
   }

   private int clear() {
      return this.run() + this.set() + 4;
   }

   private int reset() {
      return this.push();
   }

   private int start() {
      return this.poll();
   }

   private int stop() {
      int header = 18;
      int cap = Math.max(3, this.load_I() * 65 / 100 / this.close());
      int shown = Math.max(3, Math.min(cap, this.write_List.size()));
      int content = header + shown * this.close() - 2 + 4;
      int available = Math.max(header + this.flush() + 4, this.load_I() - 6 - this.clear());
      return Math.min(available, content);
   }

   private int next() {
      return this.clear() + 4 + 12 + 2;
   }

   private int prev() {
      return this.clear() + this.stop() - 4;
   }

   private int valid() {
      return Math.max(this.flush(), this.prev() - this.next());
   }

   private int init_I_I_7(int rowCount) {
      return Math.max(0, rowCount * this.close() - 2 - this.valid());
   }

   private int size() {
      return this.reset() + 4;
   }

   private int data() {
      return this.value() - 2;
   }

   private int value() {
      return this.reset() + this.start() - 4 - 6;
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_I_3(int rowCount) {
      int contentPixels = Math.max(0, rowCount * this.close() - 2);
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(contentPixels, this.valid(), this.value(), this.next(), 6, this.valid(), this.update);
   }

   private boolean init_Z_6() {
      return this.poll() < 260 || this.load_I() < this.next() + this.close() * 2;
   }

   private enum Inner_BufferHelper {
      SERVER,
      SERVER_PLUGIN,
      PLUGIN,
      PLUGIN_SERVER;
   }

   private static final class Inner_CachePool {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final String init_Str;

      Inner_CachePool(int x, int y, int width, int height, String key) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_Str = key;
      }

      boolean init(double mx, double my) {
         return mx >= this.init_I && mx <= this.init_I + this.read && my >= this.load && my <= this.load + this.write;
      }
   }

   private static final class Inner_ConfigStore {
      final String init_Str;
      final String load;
      final String read;
      final List<String> init_List;
      final long init_J;

      Inner_ConfigStore(String address, String name, List<String> plugins, long scannedAtMs) {
         this.init_Str = address;
         this.load = name;
         this.read = address;
         this.init_List = plugins;
         this.init_J = scannedAtMs;
      }
   }

   private enum Inner_DataUtil {
      BY_SERVER,
      BY_PLUGIN;
   }

   private static final class Inner_StreamUtil {
      PluginLibraryScreen.Inner_BufferHelper init_MapStore_BufferHel;
      String init_Str = "";
      String load_Str = "";
      String read = "";
      boolean init_Z;
      String write;
      List<String> init_List;
      List<PluginLibraryScreen.Inner_ConfigStore> load_List;
      int init_I;

      static PluginLibraryScreen.Inner_StreamUtil init(PluginLibraryScreen.Inner_BufferHelper type, String key, String primary, String secondary, boolean expanded) {
         PluginLibraryScreen.Inner_StreamUtil row = new PluginLibraryScreen.Inner_StreamUtil();
         row.init_MapStore_BufferHel = type;
         row.init_Str = key;
         row.load_Str = primary;
         row.read = secondary;
         row.init_Z = expanded;
         return row;
      }

      static PluginLibraryScreen.Inner_StreamUtil init(PluginLibraryScreen.Inner_BufferHelper type, String primary, String secondary) {
         PluginLibraryScreen.Inner_StreamUtil row = new PluginLibraryScreen.Inner_StreamUtil();
         row.init_MapStore_BufferHel = type;
         row.load_Str = primary;
         row.read = secondary;
         return row;
      }
   }
}
