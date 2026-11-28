package autismclient.gui.screens;

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
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int MAX_PANEL_WIDTH = 440;
   private static final int load_I = 12;
   private static final int read_I = 3;
   private final Screen parent;
   private final List<autismclient.gui.ui.widgets.OverlayActionButton> buttons = new ArrayList<>();
   private final List<PluginLibraryScreen.RowHit> rowHits = new ArrayList<>();
   private final List<PluginLibraryScreen.ListRow> visibleRows = new ArrayList<>();
   private final Set<String> expandedKeys = new HashSet<>();
   private EditBox searchField;
   private String searchQuery = "";
   private PluginLibraryScreen.GroupMode groupMode = PluginLibraryScreen.GroupMode.BY_SERVER;
   private List<PluginLibraryScreen.ListRow> filteredRows = List.of();
   private int headerCount;
   private int scrollOffset;
   private boolean draggingScrollbar;
   private int scrollGrabOffset;
   private long seenScanRevision = -9223372036854775808L;

   public PluginLibraryScreen(Screen parent) {
      super(Component.literal("Plugin Library"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      this.searchField = new MsdfEditBox(this.font, this.searchX(), this.searchY(), this.searchWidth(), this.controlHeight(), Component.literal("Search"));
      this.searchField.setHint(Component.literal(this.searchHint()));
      this.searchField.setMaxLength(128);
      this.searchField.setValue(this.searchQuery);
      this.searchField.setResponder(value -> {
         this.searchQuery = trim(value);
         this.scrollOffset = 0;
         this.rebuildButtons();
      });
      this.addRenderableWidget(this.searchField);
      this.seenScanRevision = AutismConfig.getGlobal().pluginScanRevision();
      this.rebuildButtons();
   }

   @Override
   public void tick() {
      long rev = AutismConfig.getGlobal().pluginScanRevision();
      if (rev != this.seenScanRevision) {
         this.seenScanRevision = rev;
         this.rebuildButtons();
      }
   }

   private String searchHint() {
      return this.groupMode == PluginLibraryScreen.GroupMode.BY_SERVER ? "Search servers or plugins..." : "Search plugins...";
   }

   private void setGroupMode(PluginLibraryScreen.GroupMode next) {
      if (this.groupMode != next) {
         this.groupMode = next;
         this.expandedKeys.clear();
         this.scrollOffset = 0;
         if (this.searchField != null) {
            this.searchField.setHint(Component.literal(this.searchHint()));
         }

         this.rebuildButtons();
      }
   }

   private void toggleExpanded(String key) {
      if (!this.expandedKeys.remove(key)) {
         this.expandedKeys.add(key);
      }

      this.rebuildButtons();
   }

   private List<PluginLibraryScreen.ServerEntry> serverEntries() {
      Map<String, PluginLibraryScreen.ServerEntry> byAddress = new LinkedHashMap<>();
      Map<String, AutismConfig.PluginScanCacheEntry> all = AutismConfig.getGlobal().allPluginScans();

      for (Entry<String, AutismConfig.PluginScanCacheEntry> item : all.entrySet()) {
         AutismConfig.PluginScanCacheEntry entry = item.getValue();
         if (entry != null && entry.plugins != null && !entry.plugins.isEmpty()) {
            String address = entry.serverAddress != null && !entry.serverAddress.isBlank() ? entry.serverAddress : addressFromScanKey(item.getKey());
            if (address != null && !address.isBlank()) {
               String lower = address.toLowerCase(Locale.ROOT);
               PluginLibraryScreen.ServerEntry existing = byAddress.get(lower);
               if (existing == null || existing.scannedAtMs < entry.scannedAtMs) {
                  String name = entry.serverName != null && !entry.serverName.isBlank() ? entry.serverName : address;
                  byAddress.put(lower, new PluginLibraryScreen.ServerEntry(address, name, sortedUnique(entry.plugins), entry.scannedAtMs));
               }
            }
         }
      }

      List<PluginLibraryScreen.ServerEntry> servers = new ArrayList<>(byAddress.values());
      servers.sort(Comparator.<PluginLibraryScreen.ServerEntry>comparingLong(r -> r.scannedAtMs).reversed());
      return servers;
   }

   private static String addressFromScanKey(String key) {
      if (key == null) {
         return "";
      }

      int cut = key.lastIndexOf(124);
      return cut > 0 ? key.substring(0, cut) : key;
   }

   private static List<String> sortedUnique(List<String> plugins) {
      Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      for (String p : plugins) {
         if (p != null && !p.isBlank()) {
            set.add(p.trim());
         }
      }

      return new ArrayList<>(set);
   }

   private List<PluginLibraryScreen.ListRow> buildRows() {
      String query = this.searchQuery.toLowerCase(Locale.ROOT);
      List<PluginLibraryScreen.ServerEntry> servers = this.serverEntries();
      return this.groupMode == PluginLibraryScreen.GroupMode.BY_SERVER ? this.rowsByServer(servers, query) : this.rowsByPlugin(servers, query);
   }

   private List<PluginLibraryScreen.ListRow> rowsByServer(List<PluginLibraryScreen.ServerEntry> servers, String query) {
      List<PluginLibraryScreen.ListRow> rows = new ArrayList<>();

      for (PluginLibraryScreen.ServerEntry server : servers) {
         boolean nameHit = query.isEmpty() || fuzzyMatches(server.name, query) || fuzzyMatches(server.address, query);
         boolean pluginHit = false;
         if (!query.isEmpty()) {
            for (String p : server.plugins) {
               if (fuzzyMatches(p, query)) {
                  pluginHit = true;
                  break;
               }
            }
         }

         if (query.isEmpty() || nameHit || pluginHit) {
            String key = "srv:" + server.address.toLowerCase(Locale.ROOT);
            boolean open = this.expandedKeys.contains(key);
            PluginLibraryScreen.ListRow row = PluginLibraryScreen.ListRow.groupRow(
               PluginLibraryScreen.RowType.SERVER,
               key,
               server.displayAddress,
               server.plugins.size() + (server.plugins.size() == 1 ? " plugin  " : " plugins  ") + ago(server.scannedAtMs),
               open
            );
            row.serverAddress = server.address;
            row.plugins = server.plugins;
            rows.add(row);
            if (open) {
               List<String> shown = server.plugins;
               if (!query.isEmpty()) {
                  List<String> matched = new ArrayList<>();

                  for (String plugin : server.plugins) {
                     if (fuzzyMatches(plugin, query)) {
                        matched.add(plugin);
                     }
                  }

                  if (!matched.isEmpty()) {
                     shown = matched;
                  }
               }

               for (String plugin : shown) {
                  rows.add(PluginLibraryScreen.ListRow.childRow(PluginLibraryScreen.RowType.SERVER_PLUGIN, plugin, ""));
               }
            }
         }
      }

      this.headerCount = rows.isEmpty() ? 0 : (int)rows.stream().filter(r -> r.type == PluginLibraryScreen.RowType.SERVER).count();
      return rows;
   }

   private List<PluginLibraryScreen.ListRow> rowsByPlugin(List<PluginLibraryScreen.ServerEntry> servers, String query) {
      Map<String, List<PluginLibraryScreen.ServerEntry>> byPlugin = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (PluginLibraryScreen.ServerEntry server : servers) {
         for (String plugin : server.plugins) {
            if (query.isEmpty() || fuzzyMatches(plugin, query)) {
               byPlugin.computeIfAbsent(plugin, k -> new ArrayList<>()).add(server);
            }
         }
      }

      List<Entry<String, List<PluginLibraryScreen.ServerEntry>>> ordered = new ArrayList<>(byPlugin.entrySet());
      ordered.sort(
         Comparator.<Entry<String, List<PluginLibraryScreen.ServerEntry>>>comparingInt(e -> e.getValue().size())
            .reversed()
            .thenComparing(e -> e.getKey().toLowerCase(Locale.ROOT))
      );
      List<PluginLibraryScreen.ListRow> rows = new ArrayList<>();

      for (Entry<String, List<PluginLibraryScreen.ServerEntry>> entry : ordered) {
         int count = entry.getValue().size();
         String key = "plg:" + entry.getKey().toLowerCase(Locale.ROOT);
         boolean open = this.expandedKeys.contains(key);
         PluginLibraryScreen.ListRow row = PluginLibraryScreen.ListRow.groupRow(
            PluginLibraryScreen.RowType.PLUGIN, key, entry.getKey(), "on " + count + (count == 1 ? " server" : " servers"), open
         );
         row.servers = entry.getValue();
         rows.add(row);
         if (open) {
            for (PluginLibraryScreen.ServerEntry server : entry.getValue()) {
               rows.add(PluginLibraryScreen.ListRow.childRow(PluginLibraryScreen.RowType.PLUGIN_SERVER, server.displayAddress, ago(server.scannedAtMs)));
            }
         }
      }

      this.headerCount = ordered.size();
      return rows;
   }

   private static boolean fuzzyMatches(String value, String queryLower) {
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

   private static String ago(long ms) {
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

   private void rebuildButtons() {
      this.buttons.clear();
      this.rowHits.clear();
      this.visibleRows.clear();
      int ctl = this.controlHeight();
      this.buttons.add(autismclient.gui.ui.widgets.OverlayActionButton.create(this.innerX(), this.cardY() + 4, this.backButtonWidth(), ctl, Component.literal("Back"), b -> this.onClose()));
      int toggleY = this.searchY();
      int toggleW = this.groupToggleWidth();
      int toggleX = this.innerX();
      this.buttons
         .add(
            autismclient.gui.ui.widgets.OverlayActionButton.create(
                  toggleX, toggleY, toggleW, ctl, Component.literal("By Server"), b -> this.setGroupMode(PluginLibraryScreen.GroupMode.BY_SERVER)
               )
               .setTone(
                  this.groupMode == PluginLibraryScreen.GroupMode.BY_SERVER
                     ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS
                     : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY
               )
         );
      this.buttons
         .add(
            autismclient.gui.ui.widgets.OverlayActionButton.create(
                  toggleX + toggleW + 2,
                  toggleY,
                  toggleW,
                  ctl,
                  Component.literal("By Plugin"),
                  b -> this.setGroupMode(PluginLibraryScreen.GroupMode.BY_PLUGIN)
               )
               .setTone(
                  this.groupMode == PluginLibraryScreen.GroupMode.BY_PLUGIN
                     ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS
                     : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY
               )
         );
      this.filteredRows = this.buildRows();
      int maxScroll = this.maxScroll(this.filteredRows.size());
      this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
      int pitch = this.rowPitch();
      int rowH = this.rowHeight();
      int top = this.listTop();
      int bottom = this.listBottom();
      int btnH = this.rowButtonHeight();
      int copyW = this.copyButtonWidth();
      int firstVisible = this.scrollOffset / pitch;
      int rowY = top - this.scrollOffset % pitch;

      for (int i = firstVisible; i < this.filteredRows.size() && rowY < bottom; rowY += pitch) {
         if (rowY + rowH > top) {
            PluginLibraryScreen.ListRow row = this.filteredRows.get(i);
            row.y = rowY;
            this.visibleRows.add(row);
            boolean fullyVisible = rowY >= top && rowY + rowH <= bottom;
            boolean parent = row.type == PluginLibraryScreen.RowType.SERVER || row.type == PluginLibraryScreen.RowType.PLUGIN;
            if (parent && fullyVisible) {
               int btnY = rowY + (rowH - btnH) / 2;
               int cursor = this.listRight() - 1;
               if (row.type == PluginLibraryScreen.RowType.SERVER) {
                  cursor -= btnH;
                  this.buttons
                     .add(
                        autismclient.gui.ui.widgets.OverlayActionButton.create(cursor, btnY, btnH, btnH, Component.empty(), b -> this.removeServerScan(row))
                           .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER)
                           .setIcon(GuiIconAtlas.DELETE)
                     );
                  cursor -= 2;
               }

               cursor -= copyW;
               this.buttons
                  .add(
                     autismclient.gui.ui.widgets.OverlayActionButton.create(cursor, btnY, copyW, btnH, Component.literal("Copy"), b -> this.copyRow(row))
                        .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
                  );
               this.rowHits.add(new PluginLibraryScreen.RowHit(this.listLeft(), rowY, Math.max(1, cursor - 4 - this.listLeft()), rowH, row.key));
            }
         }

         i++;
      }
   }

   private int backButtonWidth() {
      return this.buttonWidth("Back");
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, labelFont(), 0));
   }

   private int groupToggleWidth() {
      return Math.max(this.buttonWidth("By Server"), this.buttonWidth("By Plugin"));
   }

   private int copyButtonWidth() {
      return this.buttonWidth("Copy");
   }

   private void removeServerScan(PluginLibraryScreen.ListRow row) {
      if (row.type == PluginLibraryScreen.RowType.SERVER && row.serverAddress != null) {
         AutismConfig.getGlobal().removePluginScan(row.serverAddress);
         this.expandedKeys.remove(row.key);
         this.seenScanRevision = AutismConfig.getGlobal().pluginScanRevision();
         this.rebuildButtons();
      }
   }

   private void copyRow(PluginLibraryScreen.ListRow row) {
      String text;
      String note;
      if (row.type == PluginLibraryScreen.RowType.SERVER && row.plugins != null) {
         text = String.join("\n", row.plugins);
         note = "Copied " + row.plugins.size() + " plugins from " + row.label;
      } else {
         if (row.type != PluginLibraryScreen.RowType.PLUGIN || row.servers == null) {
            return;
         }

         List<String> names = new ArrayList<>();

         for (PluginLibraryScreen.ServerEntry s : row.servers) {
            names.add(s.displayAddress);
         }

         text = String.join("\n", names);
         note = "Copied " + names.size() + " servers running " + row.label;
      }

      if (!text.isBlank() && this.minecraft != null) {
         this.minecraft.keyboardHandler.setClipboard(text);
         this.toast(note, GuiColorPalette.open);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -15987181);
         this.drawCard(graphics, this.cardX(), this.cardY(), this.cardWidth(), this.headerCardHeight(), -401402080);
         this.drawCard(graphics, this.listCardX(), this.listCardY(), this.listCardWidth(), this.listCardHeight(), -1206840035);
         String count = this.headerCount
            + (
               this.groupMode == PluginLibraryScreen.GroupMode.BY_SERVER
                  ? (this.headerCount == 1 ? " server" : " servers")
                  : (this.headerCount == 1 ? " plugin" : " plugins")
            );
         int countW = k.init(this.font, count, labelFont(), GuiColorPalette.close);
         int countX = this.innerX() + this.innerWidth() - countW;
         int headerTextY = this.centeredTextY(this.cardY() + 4, this.controlHeight());
         int titleX = this.innerX() + this.backButtonWidth() + 4;
         this.drawClippedText(graphics, "Plugin Library", titleX, headerTextY, GuiColorPalette.flush, Math.max(1, countX - 8 - titleX));
         this.drawClippedText(graphics, count, countX, headerTextY, GuiColorPalette.close, countW + 4);
         String header = this.groupMode == PluginLibraryScreen.GroupMode.BY_SERVER ? "Servers you have scanned" : "Plugin -> servers running it";
         this.drawClippedText(graphics, header, this.listCardX() + 4, this.centeredTextY(this.listCardY() + 4, 12), GuiColorPalette.close, this.listCardWidth() - 8);
         String queryLower = this.searchQuery.toLowerCase(Locale.ROOT);
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(
               graphics,
               autismclient.gui.f.NetHelper.init(this.listLeft(), this.listTop(), Math.max(0, this.listRight() - this.listLeft()), Math.max(0, this.listBottom() - this.listTop()))
            );

         try {
            if (this.filteredRows.isEmpty()) {
               this.drawClippedText(
                  graphics, this.emptyMessage(), this.listLeft() + 4, this.centeredTextY(this.listTop(), this.rowHeight()), GuiColorPalette.close, this.listRight() - this.listLeft() - 4
               );
            } else {
               for (PluginLibraryScreen.ListRow row : this.visibleRows) {
                  this.drawRow(graphics, row, mx, my, queryLower);
               }
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         for (autismclient.gui.ui.widgets.OverlayActionButton b : this.buttons) {
            autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, b, mx, my);
         }

         if (!this.isCompact()) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics sb = this.scrollbarMetrics(this.filteredRows.size());
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, sb, sb.containsTrack(mx, my), this.draggingScrollbar);
         }

         super.extractRenderState(graphics, mx, my, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private String emptyMessage() {
      return this.searchQuery.isEmpty()
         ? "No saved scans yet. Join a server with Auto Probe on to record its plugins."
         : "Nothing matches \"" + this.searchQuery + "\".";
   }

   private void drawRow(GuiGraphicsExtractor graphics, PluginLibraryScreen.ListRow row, int mx, int my, String queryLower) {
      boolean child = row.type == PluginLibraryScreen.RowType.SERVER_PLUGIN || row.type == PluginLibraryScreen.RowType.PLUGIN_SERVER;
      int x = this.listLeft();
      int y = row.y;
      int w = this.listRight() - this.listLeft();
      int h = this.rowHeight();
      boolean hovered = my >= y && my < y + h && mx >= x && mx < this.listRight();
      if (!child) {
         int fill = hovered ? 620756991 : 352321535;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), fill);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 2, h), row.expanded ? GuiColorPalette.sync : 872415231);
      }

      int textY = this.centeredTextY(y, h);
      int textLeft = x + 6 + (child ? 12 : 0);
      if (!child) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + 5, y + (h - 8) / 2, 8, 8), row.expanded, GuiColorPalette.close);
         textLeft += 10;
      } else {
         this.drawClippedText(graphics, "-", x + 6 + 12 - 8, textY, GuiColorPalette.close, 8);
      }

      int baseColor = child ? -3158065 : GuiColorPalette.flush;
      int secondaryColor = GuiColorPalette.close;
      int secondaryW = row.secondary.isEmpty() ? 0 : k.init(this.font, row.secondary, labelFont(), secondaryColor);
      int rowActionReserve = child ? 0 : this.copyButtonWidth() + 2 + (row.type == PluginLibraryScreen.RowType.SERVER ? this.rowButtonHeight() + 2 : 0);
      int rightLimit = this.listRight() - rowActionReserve - 4;
      int secondaryX = Math.max(textLeft + 20, rightLimit - secondaryW);
      int primaryMax = Math.max(20, (row.secondary.isEmpty() ? rightLimit : secondaryX - 8) - textLeft);
      this.drawHighlightedText(graphics, row.label, textLeft, textY, primaryMax, baseColor, queryLower);
      if (!row.secondary.isEmpty() && secondaryX > textLeft) {
         this.drawClippedText(graphics, row.secondary, secondaryX, textY, secondaryColor, secondaryW + 2);
      }
   }

   private void drawHighlightedText(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int baseColor, String queryLower) {
      Identifier font = labelFont();
      int idx = queryLower.isEmpty() ? -1 : text.toLowerCase(Locale.ROOT).indexOf(queryLower);
      if (idx >= 0 && k.init(this.font, text, font, baseColor) <= maxWidth) {
         String pre = text.substring(0, idx);
         String mid = text.substring(idx, idx + queryLower.length());
         String post = text.substring(idx + queryLower.length());
         int accent = UiColorPalette.accent();
         k.init(graphics, this.font, pre, font, baseColor, x, y, false);
         int midX = x + k.init(this.font, pre, font, baseColor);
         k.init(graphics, this.font, mid, font, accent, midX, y, false);
         k.init(graphics, this.font, post, font, baseColor, midX + k.init(this.font, mid, font, accent), y, false);
      } else {
         k.init(graphics, this.font, text, font, baseColor, x, y, maxWidth, false);
      }
   }

   private void drawClippedText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      k.init(graphics, this.font, text, labelFont(), color, x, y, Math.max(1, maxWidth), false);
   }

   private static Identifier labelFont() {
      return THEME.init_Identifier_l(l.read);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isCompact()) {
         return super.mouseClicked(event, doubleClick);
      }

      MouseButtonEvent ve = toVirtual(event);
      double mx = ve.x();
      double my = ve.y();
      if (Compat263.mouseEventGlfwButton(ve) == 0 && !this.isCompact()) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics sb = this.scrollbarMetrics(this.filteredRows.size());
         if (sb.isScrollable() && sb.containsTrack(mx, my)) {
            this.draggingScrollbar = true;
            this.scrollGrabOffset = sb.containsThumb(mx, my) ? (int)Math.round(my) - sb.thumbY() : sb.thumbHeight() / 2;
            this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(sb, my, this.scrollGrabOffset);
            this.rebuildButtons();
            return true;
         }
      }

      for (autismclient.gui.ui.widgets.OverlayActionButton b : this.buttons) {
         if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(b, mx, my, Compat263.mouseEventGlfwButton(ve))) {
            return true;
         }
      }

      if (Compat263.mouseEventGlfwButton(ve) == 0) {
         for (PluginLibraryScreen.RowHit hit : this.rowHits) {
            if (hit.contains(mx, my)) {
               this.toggleExpanded(hit.key);
               return true;
            }
         }
      }

      return super.mouseClicked(ve, doubleClick);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      } else {
         return super.mouseReleased(toVirtual(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
      MouseButtonEvent ve = toVirtual(event);
      if (this.draggingScrollbar) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics sb = this.scrollbarMetrics(this.filteredRows.size());
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(sb, ve.y(), this.scrollGrabOffset);
         this.rebuildButtons();
         return true;
      } else {
         return super.mouseDragged(ve, cA.init_D_D(deltaX), cA.init_D_D(deltaY));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.isCompact()) {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      } else {
         double vy = cA.init_D_D(mouseY);
         double vx = cA.init_D_D(mouseX);
         if (!(vx < this.listCardX()) && !(vx > this.listCardX() + this.listCardWidth()) && !(vy < this.listTop()) && !(vy > this.listBottom())) {
            int maxScroll = this.maxScroll(this.filteredRows.size());
            int next = this.scrollOffset - (int)Math.signum(scrollY) * this.rowPitch();
            this.scrollOffset = Math.max(0, Math.min(next, maxScroll));
            this.rebuildButtons();
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
      this.minecraft.gui.setScreen(this.parent);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int rowButtonHeight() {
      return this.rowHeight() - 2;
   }

   private int centeredTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int cardWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 440);
   }

   private int cardX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.cardWidth(), 6);
   }

   private int innerX() {
      return this.cardX() + 4;
   }

   private int innerWidth() {
      return Math.max(1, this.cardWidth() - 8);
   }

   private int cardY() {
      return 6;
   }

   private int searchY() {
      return this.cardY() + 4 + this.controlHeight() + 2;
   }

   private int headerCardHeight() {
      return this.searchY() + this.controlHeight() + 4 - this.cardY();
   }

   private int searchX() {
      return this.innerX() + this.groupToggleWidth() * 2 + 2 + 4;
   }

   private int searchWidth() {
      return Math.max(40, this.innerX() + this.innerWidth() - this.searchX());
   }

   private int listCardY() {
      return this.cardY() + this.headerCardHeight() + 4;
   }

   private int listCardX() {
      return this.cardX();
   }

   private int listCardWidth() {
      return this.cardWidth();
   }

   private int listCardHeight() {
      int header = 18;
      int cap = Math.max(3, this.virtualHeight() * 65 / 100 / this.rowPitch());
      int shown = Math.max(3, Math.min(cap, this.filteredRows.size()));
      int content = header + shown * this.rowPitch() - 2 + 4;
      int available = Math.max(header + this.rowHeight() + 4, this.virtualHeight() - 6 - this.listCardY());
      return Math.min(available, content);
   }

   private int listTop() {
      return this.listCardY() + 4 + 12 + 2;
   }

   private int listBottom() {
      return this.listCardY() + this.listCardHeight() - 4;
   }

   private int listViewportHeight() {
      return Math.max(this.rowHeight(), this.listBottom() - this.listTop());
   }

   private int maxScroll(int rowCount) {
      return Math.max(0, rowCount * this.rowPitch() - 2 - this.listViewportHeight());
   }

   private int listLeft() {
      return this.listCardX() + 4;
   }

   private int listRight() {
      return this.scrollbarX() - 2;
   }

   private int scrollbarX() {
      return this.listCardX() + this.listCardWidth() - 4 - 6;
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int rowCount) {
      int contentPixels = Math.max(0, rowCount * this.rowPitch() - 2);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(contentPixels, this.listViewportHeight(), this.scrollbarX(), this.listTop(), 6, this.listViewportHeight(), this.scrollOffset);
   }

   private boolean isCompact() {
      return this.cardWidth() < 260 || this.virtualHeight() < this.listTop() + this.rowPitch() * 2;
   }

   private enum RowType {
      SERVER,
      SERVER_PLUGIN,
      PLUGIN,
      PLUGIN_SERVER;
   }

   private static final class RowHit {
      final int x;
      final int y;
      final int width;
      final int height;
      final String key;

      RowHit(int x, int y, int width, int height, String key) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.key = key;
      }

      boolean contains(double mx, double my) {
         return mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height;
      }
   }

   private static final class ServerEntry {
      final String address;
      final String name;
      final String displayAddress;
      final List<String> plugins;
      final long scannedAtMs;

      ServerEntry(String address, String name, List<String> plugins, long scannedAtMs) {
         this.address = address;
         this.name = name;
         this.displayAddress = address;
         this.plugins = plugins;
         this.scannedAtMs = scannedAtMs;
      }
   }

   private enum GroupMode {
      BY_SERVER,
      BY_PLUGIN;
   }

   private static final class ListRow {
      PluginLibraryScreen.RowType type;
      String key = "";
      String label = "";
      String secondary = "";
      boolean expanded;
      String serverAddress;
      List<String> plugins;
      List<PluginLibraryScreen.ServerEntry> servers;
      int y;

      static PluginLibraryScreen.ListRow groupRow(PluginLibraryScreen.RowType type, String key, String primary, String secondary, boolean expanded) {
         PluginLibraryScreen.ListRow row = new PluginLibraryScreen.ListRow();
         row.type = type;
         row.key = key;
         row.label = primary;
         row.secondary = secondary;
         row.expanded = expanded;
         return row;
      }

      static PluginLibraryScreen.ListRow childRow(PluginLibraryScreen.RowType type, String primary, String secondary) {
         PluginLibraryScreen.ListRow row = new PluginLibraryScreen.ListRow();
         row.type = type;
         row.label = primary;
         row.secondary = secondary;
         return row;
      }
   }
}
