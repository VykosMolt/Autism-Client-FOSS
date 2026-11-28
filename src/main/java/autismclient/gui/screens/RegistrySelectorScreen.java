package autismclient.gui.screens;

import autismclient.api.module.Kind;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.StorageEspCatalog;
import autismclient.util.DisplayItemStackFactory;
import autismclient.util.FarmCropCatalog;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.RegistryIdValidator;
import autismclient.util.PipeDelimitedListUtil;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RegistrySelectorScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder STYLE_THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 460;
   private static final int load_I = 4;
   private static final int read_I = 16;
   private static final Identifier EMPTY_SPAWN_EGG_TEXTURE = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/autism/empty_spawn_egg.png");
   private static final int write_I = -872086265;
   private static final int update = -1441722092;
   private static final int sync = -1441000940;
   private static final int flush = -791321;
   private static final int close = -4743522;
   private static final int open = -50373;
   private final Screen parent;
   private final Module module;
   private final Setting<?, ?> setting;
   private final List<RegistrySelectorScreen.RegistryEntry> entries;
   private final Map<String, RegistrySelectorScreen.RegistryEntry> entriesById = new HashMap<>();
   private final List<RegistrySelectorScreen.HitRegion> hitRegions = new ArrayList<>();
   private ThemedTextInputBox searchField;
   private int availableScroll;
   private int selectedScroll;
   private boolean availableScrollbarDragging;
   private boolean selectedScrollbarDragging;
   private int scrollGrabOffset;
   private String cachedRawValue;
   private String cachedNeedle;
   private Set<String> cachedSelectedIds = Set.of();
   private List<RegistrySelectorScreen.SelectedRow> cachedSelectedRows = List.of();
   private List<RegistrySelectorScreen.RegistryEntry> cachedAvailable = List.of();
   private int contentHeightKey = -1;
   private int cachedContentHeight;

   private static int textColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public RegistrySelectorScreen(Screen parent, Module module, Setting<?, ?> option) {
      super(Component.literal(option == null ? "Select" : "Select " + option.label()));
      this.parent = parent;
      this.module = module;
      this.setting = option;
      this.entries = this.buildEntries(option == null ? Kind.ITEM_LIST : option.kind());

      for (RegistrySelectorScreen.RegistryEntry entry : this.entries) {
         this.entriesById.put(entry.init_Str, entry);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.hitRegions.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -872086265);
         int x = this.panelX();
         int y = this.panelY();
         int panelW = this.panelWidth();
         int panelH = this.get();
         this.init(graphics, x, y, panelW, panelH, this.titleText(), mx, my);
         if (panelW < 260 || panelH < 100) {
            this.load(graphics, "Window too small.", x + 4, this.centeredTextY(y + this.headerHeight(), 12), textColor(), Math.max(0, panelW - 8));
            return;
         }

         this.ensureSearchField(x + 4, this.bodyTop(), panelW - 8, this.flush());
         this.searchField.render(graphics, mx, my, delta);
         RegistrySelectorScreen.PaneLayout pane = this.availablePane();
         this.init(graphics, pane, pane.init(), "Available", true, mx, my);
         this.init(graphics, pane, pane.load(), "Selected", false, mx, my);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, RegistrySelectorScreen.PaneLayout pane, int x, String title, boolean available, int mx, int my) {
      int y = pane.read();
      int w = pane.write();
      int listTop = pane.sync();
      int listH = pane.flush();
      this.init(graphics, x, y, w, pane.update(), 5, -1442248437, STYLE_THEME.set());
      this.load(graphics, title, x + 4, this.centeredTextY(y + 2, this.open()), -791321, w - 8);
      autismclient.gui.ui.UiClipStack.getInstance().push(graphics, autismclient.gui.f.NetHelper.init(x + 1, listTop, Math.max(0, w - 2), Math.max(0, listH)));

      try {
         if (available) {
            this.drawAvailablePane(graphics, x + 3, listTop, w - 12, listH, mx, my);
         } else {
            this.drawSelectedPane(graphics, x + 3, listTop, w - 12, listH, mx, my);
         }
      } finally {
         autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetricsAt(available, x, listTop, w, listH);
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, metrics.containsTrack(mx, my), available ? this.availableScrollbarDragging : this.selectedScrollbarDragging);
   }

   private void drawAvailablePane(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<RegistrySelectorScreen.RegistryEntry> rows = this.availableEntries();
      int maxScroll = Math.max(0, this.contentHeight(rows) - h);
      this.availableScroll = this.clamp(this.availableScroll, 0, maxScroll);
      int rowY = y - this.availableScroll;
      int rowH = this.rowHeight();
      int groupH = this.push();
      String lastGroup = "";

      for (RegistrySelectorScreen.RegistryEntry entry : rows) {
         if (isGroupedKind(this.setting) && !entry.read.equals(lastGroup)) {
            if (rowY + groupH > y && rowY < y + h) {
               this.init(graphics, entry.read, x, rowY, w, groupH);
            }

            rowY += groupH;
            lastGroup = entry.read;
         }

         if (rowY + rowH > y && rowY < y + h) {
            this.init(graphics, entry, x, rowY, w, RegistrySelectorScreen.RowAction.ADD, entry.init_Str, mx, my, false);
         }

         rowY += rowH;
      }
   }

   private void drawSelectedPane(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<RegistrySelectorScreen.SelectedRow> rows = this.selectedRows();
      int maxScroll = Math.max(0, this.selectedContentHeight() - h);
      this.selectedScroll = this.clamp(this.selectedScroll, 0, maxScroll);
      int rowH = this.rowHeight();
      if (rows.isEmpty()) {
         this.load(graphics, "Nothing selected.", x + 4, this.centeredTextY(y, rowH), textColor(), w - 8);
      } else {
         int first = this.clamp(this.selectedScroll / rowH, 0, Math.max(0, rows.size() - 1));
         int last = this.clamp((this.selectedScroll + h - 1) / rowH, 0, rows.size() - 1);

         for (int i = first; i <= last; i++) {
            RegistrySelectorScreen.SelectedRow row = rows.get(i);
            int rowY = y - this.selectedScroll + i * rowH;
            if (rowY + rowH > y && rowY < y + h) {
               this.init(graphics, row.entry(), x, rowY, w, RegistrySelectorScreen.RowAction.REMOVE, row.id, mx, my, row.init_Z);
            }
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, String label, int x, int y, int w, int groupH) {
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawSectionHeader(graphics, x, y, w, groupH - 1);
      this.load(graphics, label, x + 4, this.centeredTextY(y, groupH - 1), textColor(), w - 8);
   }

   private void init(
      GuiGraphicsExtractor graphics,
      RegistrySelectorScreen.RegistryEntry entry,
      int x,
      int y,
      int w,
      RegistrySelectorScreen.RowAction type,
      String value,
      int mx,
      int my,
      boolean invalid
   ) {
      String label = entry == null ? value : entry.load;
      String id = entry == null ? value : entry.init_Str;
      int rowH = this.rowHeight();
      boolean over = mx >= x && mx < x + w && my >= y && my < y + rowH;
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, y, w, rowH - 1, over ? -1441000940 : -1441722092);
      int textX = x + 4;
      if (entry != null && !invalid) {
         this.init(graphics, entry, x + 2, y + Math.max(0, (rowH - 1 - 16) / 2));
         textX = x + 2 + 16 + 4;
      }

      int actionW = 13;
      int actionX = x + w - actionW - 2;
      int actionY = y + (rowH - 1 - actionW) / 2;
      int textMax = actionX - 4 - textX;
      int textY = this.centeredTextY(y, rowH - 1);
      this.load(graphics, label, textX, textY, invalid ? ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.DANGER) : -791321, textMax);
      if (!id.equals(label)) {
         int labelW = Math.min(textMax, autismclient.gui.f.b.k.init(this.font, label, STYLE_THEME.init_Identifier_l(l.read), 0));
         int idX = textX + labelW + 4;
         int idMax = textX + textMax - idX;
         if (idMax >= 16) {
            this.load(graphics, id, idX, textY, textColor(), idMax);
         }
      }

      if (type == RegistrySelectorScreen.RowAction.ADD) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawSymbolButton(graphics, actionX, actionY, actionW, actionW, "+", over, false);
      } else {
         autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButton(graphics, actionX, actionY, actionW, actionW, over);
      }

      this.hitRegions.add(new RegistrySelectorScreen.HitRegion(type, x, y, w, rowH, value));
   }

   private void init(GuiGraphicsExtractor graphics, RegistrySelectorScreen.RegistryEntry entry, int x, int y) {
      if (entry.init_Z()) {
         graphics.blit(EMPTY_SPAWN_EGG_TEXTURE, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
      } else {
         ItemStack stack = entry.icon();
         if (stack != null && !stack.isEmpty()) {
            graphics.item(stack, x, y);
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.headerHeight();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.hitRegions.add(new RegistrySelectorScreen.HitRegion(RegistrySelectorScreen.RowAction.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.searchField != null && this.searchField.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics availableMetrics = this.scrollbarMetrics(true);
      if (availableMetrics.isScrollable() && availableMetrics.containsTrack(mx, my)) {
         this.availableScrollbarDragging = true;
         this.scrollGrabOffset = availableMetrics.containsThumb(mx, my) ? my - availableMetrics.thumbY() : availableMetrics.thumbHeight() / 2;
         this.availableScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(availableMetrics, my, this.scrollGrabOffset);
         return true;
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics selectedMetrics = this.scrollbarMetrics(false);
      if (selectedMetrics.isScrollable() && selectedMetrics.containsTrack(mx, my)) {
         this.selectedScrollbarDragging = true;
         this.scrollGrabOffset = selectedMetrics.containsThumb(mx, my) ? my - selectedMetrics.thumbY() : selectedMetrics.thumbHeight() / 2;
         this.selectedScroll = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(selectedMetrics, my, this.scrollGrabOffset));
         return true;
      }

      for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
         RegistrySelectorScreen.HitRegion hit = this.hitRegions.get(i);
         if (hit.contains(mx, my)
            && (hit.action != RegistrySelectorScreen.RowAction.ADD || this.isOverAvailablePane(mx, my))
            && (hit.action != RegistrySelectorScreen.RowAction.REMOVE || this.load(mx, my))) {
            switch (hit.action) {
               case ADD:
                  this.init_V_Str_6(hit.value);
                  break;
               case REMOVE:
                  this.load_V_2(hit.value);
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.availableScrollbarDragging = false;
      this.selectedScrollbarDragging = false;
      if (this.searchField != null) {
         this.searchField.mouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.availableScrollbarDragging) {
         this.availableScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(true), my, this.scrollGrabOffset);
         return true;
      } else if (this.selectedScrollbarDragging) {
         this.selectedScroll = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(false), my, this.scrollGrabOffset));
         return true;
      } else {
         return this.searchField != null && this.searchField.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy) ? true : true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      int amount = scrollY < 0.0 ? this.rowHeight() : -this.rowHeight();
      if (this.isOverAvailablePane(mx, my)) {
         this.availableScroll += amount;
      } else {
         if (!this.load(mx, my)) {
            return true;
         }

         this.selectedScroll += amount;
      }

      this.availableScroll = this.clamp(this.availableScroll, 0, this.scrollbarMetrics(true).maxScroll());
      this.selectedScroll = this.clamp(this.selectedScroll, 0, this.scrollbarMetrics(false).maxScroll());
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.searchField != null && this.searchField.keyPressed(input) ? true : true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.searchField != null) {
         this.searchField.charTyped(input);
      }

      return true;
   }

   private void ensureSearchField(int x, int y, int w, int h) {
      if (this.searchField == null) {
         this.searchField = new ThemedTextInputBox(Minecraft.getInstance(), this.font, x, y, w, h, false);
         this.searchField.setPlaceholder(Component.literal("Search"));
         this.searchField.setChangedListener(value -> this.clampScrolls());
         this.searchField.setFocused(true);
      }

      this.searchField.setX(x);
      this.searchField.setY(y);
      this.searchField.setWidth(w);
      this.searchField.setHeight(h);
   }

   private String searchText() {
      return this.searchField == null ? "" : this.searchField.getValue();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void init_V_Str_6(String id) {
      List<String> tokens = PipeDelimitedListUtil.split(this.module.value(this.setting.id()));
      LinkedHashSet<String> valid = this.validIds(tokens);
      List<String> invalid = this.init_List_List_2(tokens);
      valid.add(id);
      this.storeIds(valid, invalid);
      this.clampScrolls();
   }

   private void load_V_2(String id) {
      List<String> tokens = PipeDelimitedListUtil.split(this.module.value(this.setting.id()));
      LinkedHashSet<String> valid = this.validIds(tokens);
      List<String> invalid = this.init_List_List_2(tokens);
      String normalized = RegistryIdValidator.normalize(id);
      valid.remove(normalized);
      invalid.removeIf(token -> token.equals(id) || RegistryIdValidator.normalize(token).equals(normalized));
      this.storeIds(valid, invalid);
      this.clampScrolls();
   }

   private void storeIds(Set<String> valid, List<String> invalid) {
      List<String> out = new ArrayList<>(valid);
      out.addAll(invalid);
      String encoded = PipeDelimitedListUtil.join(out);
      if (this.parent instanceof AutismModulesScreen moduleScreen && moduleScreen.isTitleSetup()) {
         this.module.setConfiguredValue(this.setting.id(), encoded);
      } else {
         this.module.setValue(this.setting.id(), encoded);
      }
   }

   private Set<String> selectedIds() {
      this.refreshCache();
      return this.cachedSelectedIds;
   }

   private LinkedHashSet<String> validIds(List<String> tokens) {
      LinkedHashSet<String> out = new LinkedHashSet<>();

      for (String token : tokens) {
         String normalized = RegistryIdValidator.normalize(token);
         if (RegistryIdValidator.isValid(this.setting.kind(), normalized) && (!this.hasBlockFilter() || this.entriesById.containsKey(normalized))) {
            out.add(normalized);
         }
      }

      return out;
   }

   private List<String> init_List_List_2(List<String> tokens) {
      if (!this.hasBlockFilter()) {
         return RegistryIdValidator.invalidIds(this.setting.kind(), tokens);
      }

      List<String> invalid = new ArrayList<>();

      for (String token : tokens) {
         String normalized = RegistryIdValidator.normalize(token);
         if (!RegistryIdValidator.isValid(this.setting.kind(), normalized) || !this.entriesById.containsKey(normalized)) {
            invalid.add(token);
         }
      }

      return invalid;
   }

   private RegistryListSetting.BlockFilter blockFilter() {
      return this.setting instanceof RegistryListSetting registry ? registry.blockFilter() : RegistryListSetting.BlockFilter.NONE;
   }

   private boolean hasBlockFilter() {
      return this.blockFilter() != RegistryListSetting.BlockFilter.NONE;
   }

   private List<RegistrySelectorScreen.RegistryEntry> availableEntries() {
      this.refreshCache();
      return this.cachedAvailable;
   }

   private List<RegistrySelectorScreen.SelectedRow> selectedRows() {
      this.refreshCache();
      return this.cachedSelectedRows;
   }

   private void refreshCache() {
      String rawValue = this.module.value(this.setting.id());
      String needle = this.searchText().trim().toLowerCase(Locale.ROOT);
      if (!rawValue.equals(this.cachedRawValue) || !needle.equals(this.cachedNeedle)) {
         List<String> tokens = PipeDelimitedListUtil.split(this.module.value(this.setting.id()));
         LinkedHashSet<String> selected = this.validIds(tokens);
         List<RegistrySelectorScreen.SelectedRow> selectedRows = new ArrayList<>();

         for (String id : selected) {
            RegistrySelectorScreen.RegistryEntry entry = this.entryById(id);
            selectedRows.add(new RegistrySelectorScreen.SelectedRow(id, entry == null ? this.plainEntry(id) : entry, false));
         }

         for (String invalid : this.init_List_List_2(tokens)) {
            selectedRows.add(new RegistrySelectorScreen.SelectedRow(invalid, this.labeledEntry("Invalid: " + invalid, invalid), true));
         }

         List<RegistrySelectorScreen.RegistryEntry> available = new ArrayList<>();

         for (RegistrySelectorScreen.RegistryEntry entry : this.entries) {
            if (!selected.contains(entry.init_Str) && (needle.isEmpty() || entry.init_Str_2().contains(needle) || entry.init_Str.contains(needle))) {
               available.add(entry);
            }
         }

         if (!needle.isEmpty()) {
            available.sort(Comparator.comparingInt(entryx -> init_I_NodeWalker_DataUti_Str(entryx, needle)));
         }

         this.cachedRawValue = rawValue;
         this.cachedNeedle = needle;
         this.cachedSelectedIds = Set.copyOf(selected);
         this.cachedSelectedRows = List.copyOf(selectedRows);
         this.cachedAvailable = List.copyOf(available);
      }
   }

   private RegistrySelectorScreen.RegistryEntry entryById(String id) {
      return this.entriesById.get(id);
   }

   private static int init_I_NodeWalker_DataUti_Str(RegistrySelectorScreen.RegistryEntry entry, String needle) {
      String path = entry.load();
      int colon = path.indexOf(58);
      if (colon >= 0 && colon + 1 < path.length()) {
         path = path.substring(colon + 1);
      }

      if (path.equals(needle)) {
         return 0;
      } else if (path.startsWith(needle)) {
         return 1;
      } else if (path.contains(needle)) {
         return 2;
      } else {
         String label = entry.init_Str_2();
         if (label.equals(needle)) {
            return 3;
         } else {
            return label.startsWith(needle) ? 4 : 5;
         }
      }
   }

   private List<RegistrySelectorScreen.RegistryEntry> buildEntries(Kind type) {
      List<RegistrySelectorScreen.RegistryEntry> out = new ArrayList<>();
      switch (type) {
         case ITEM_LIST:
            BuiltInRegistries.ITEM
               .forEach(
                  item -> {
                     if (item != Items.AIR) {
                        out.add(
                           new RegistrySelectorScreen.RegistryEntry(
                              this.lowerId(BuiltInRegistries.ITEM.getKey(item)),
                              item.getName(item.getDefaultInstance()).getString(),
                              "",
                              DisplayItemStackFactory.displayStack(item),
                              false
                           )
                        );
                     }
                  }
               );
            break;
         case BLOCK_LIST:
            BuiltInRegistries.BLOCK.forEach(block -> {
               Identifier id = BuiltInRegistries.BLOCK.getKey(block);
               if (block != Blocks.AIR && block != Blocks.BARRIER && id != null && !id.getPath().endsWith("_wall_banner")) {
                  switch (this.blockFilter()) {
                     case NONE:
                        break;
                     case PLACEABLE:
                        if (!ScaffoldModule.isScaffoldBlock(block)) {
                           return;
                        }
                        break;
                     case ORE_SIM:
                        if (!autismclient.util.oresim.OreBlockCatalog.isOre(block)) {
                           return;
                        }
                        break;
                     case CROPS:
                        if (!FarmCropCatalog.isCrop(block)) {
                           return;
                        }
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  out.add(new RegistrySelectorScreen.RegistryEntry(this.lowerId(id), block.getName().getString(), "", DisplayItemStackFactory.forBlock(block), false));
               }
            });
            break;
         case ENTITY_TYPE_LIST:
            Map<EntityType<?>, ItemStack> spawnEggs = this.spawnEggsByType();
            BuiltInRegistries.ENTITY_TYPE
               .forEach(
                  typeEntry -> {
                     if (typeEntry != EntityTypes.ITEM) {
                        ItemStack iconx = spawnEggs.getOrDefault(typeEntry, ItemStack.EMPTY);
                        out.add(
                           new RegistrySelectorScreen.RegistryEntry(
                              this.lowerId(BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)typeEntry)),
                              typeEntry.getDescription().getString(),
                              this.entityGroup((EntityType<?>)typeEntry),
                              iconx,
                              iconx.isEmpty()
                           )
                        );
                     }
                  }
               );
            break;
         case SOUND_EVENT_LIST:
            BuiltInRegistries.SOUND_EVENT
               .forEach(
                  sound -> {
                     Identifier soundId = BuiltInRegistries.SOUND_EVENT.getKey(sound);
                     if (soundId != null) {
                        out.add(
                           new RegistrySelectorScreen.RegistryEntry(
                              this.lowerId(soundId),
                              this.displayName(soundId),
                              this.load(soundId),
                              this.soundIcon(soundId, this.spawnEggsByType()),
                              false
                           )
                        );
                     }
                  }
               );
            break;
         case STORAGE_LIST:
            for (StorageEspCatalog.StorageTarget target : StorageEspCatalog.TARGETS) {
               ItemStack icon = target.icon();
               out.add(new RegistrySelectorScreen.RegistryEntry(target.id, target.label, target.read, icon, icon.isEmpty()));
            }
      }

      out.sort(Comparator.<RegistrySelectorScreen.RegistryEntry, String>comparing(entry -> entry.read).thenComparing(RegistrySelectorScreen.RegistryEntry::init_Str_2));
      return out;
   }

   private Map<EntityType<?>, ItemStack> spawnEggsByType() {
      Map<EntityType<?>, ItemStack> out = new HashMap<>();
      BuiltInRegistries.ITEM.forEach(item -> {
         TypedEntityData<EntityType<?>> data = item.components().get(DataComponents.ENTITY_DATA);
         if (data != null) {
            out.put(data.type(), DisplayItemStackFactory.displayStack(item));
         }
      });
      return out;
   }

   private ItemStack soundIcon(Identifier soundId, Map<EntityType<?>, ItemStack> spawnEggs) {
      if (soundId == null) {
         return DisplayItemStackFactory.displayStack(Items.NOTE_BLOCK);
      }

      String path = soundId.getPath().toLowerCase(Locale.ROOT);
      String normalized = path.replace('.', '_');
      String[] parts = normalized.split("_+");
      ItemStack known = this.init_ItemStack_Str(normalized);
      if (!known.isEmpty()) {
         return known;
      }

      if (path.startsWith("entity.")) {
         ItemStack entity = this.init_ItemStack_StrArr_Map(parts, spawnEggs);
         if (!entity.isEmpty()) {
            return entity;
         }
      }

      if (path.startsWith("block.")) {
         ItemStack block = this.init_ItemStack_StrArr(parts);
         if (!block.isEmpty()) {
            return block;
         }
      }

      if (path.startsWith("item.")) {
         ItemStack item = this.load(parts);
         if (!item.isEmpty()) {
            return item;
         }
      }

      ItemStack registry = this.load(parts, spawnEggs);
      return registry.isEmpty() ? DisplayItemStackFactory.displayStack(Items.NOTE_BLOCK) : registry;
   }

   private ItemStack init_ItemStack_Str(String normalizedPath) {
      if (normalizedPath.contains("fishing_bobber")) {
         return DisplayItemStackFactory.displayStack(Items.FISHING_ROD);
      } else if (normalizedPath.contains("lava")) {
         return DisplayItemStackFactory.displayStack(Items.LAVA_BUCKET);
      } else if (normalizedPath.contains("water") || normalizedPath.contains("rain") || normalizedPath.contains("bubble")) {
         return DisplayItemStackFactory.displayStack(Items.WATER_BUCKET);
      } else if (normalizedPath.contains("bucket_empty_lava") || normalizedPath.contains("bucket_fill_lava")) {
         return DisplayItemStackFactory.displayStack(Items.LAVA_BUCKET);
      } else if (normalizedPath.contains("bucket_empty") || normalizedPath.contains("bucket_fill")) {
         return DisplayItemStackFactory.displayStack(Items.BUCKET);
      } else if (normalizedPath.contains("firework")) {
         return DisplayItemStackFactory.displayStack(Items.FIREWORK_ROCKET);
      } else if (normalizedPath.contains("experience_orb")) {
         return DisplayItemStackFactory.displayStack(Items.EXPERIENCE_BOTTLE);
      } else if (normalizedPath.contains("lightning") || normalizedPath.contains("thunder")) {
         return DisplayItemStackFactory.displayStack(Items.LIGHTNING_ROD.weathering().unaffected());
      } else if (normalizedPath.contains("music_disc_")) {
         return this.itemIcon(normalizedPath.substring(normalizedPath.indexOf("music_disc_")));
      } else if (normalizedPath.startsWith("music_") || normalizedPath.startsWith("record_")) {
         return DisplayItemStackFactory.displayStack(Items.JUKEBOX);
      } else if (normalizedPath.contains("goat_horn")) {
         return DisplayItemStackFactory.displayStack(Items.GOAT_HORN);
      } else if (normalizedPath.contains("note_block")) {
         return DisplayItemStackFactory.displayStack(Items.NOTE_BLOCK);
      } else if (normalizedPath.contains("ui_") || normalizedPath.startsWith("ui")) {
         return DisplayItemStackFactory.displayStack(Items.COMPASS);
      } else if (normalizedPath.contains("ambient_cave")) {
         return DisplayItemStackFactory.displayStack(Items.TORCH);
      } else {
         return normalizedPath.contains("ambient_underwater") ? DisplayItemStackFactory.displayStack(Items.WATER_BUCKET) : ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_StrArr_Map(String[] parts, Map<EntityType<?>, ItemStack> spawnEggs) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(candidate)).orElse(null);
         if (type != null) {
            ItemStack icon = spawnEggs.getOrDefault(type, ItemStack.EMPTY);
            if (!icon.isEmpty()) {
               return icon;
            }
         }
      }

      if (this.containsPart(parts, "player")) {
         return DisplayItemStackFactory.displayStack(Items.PLAYER_HEAD);
      } else if (this.containsPart(parts, "arrow")) {
         return DisplayItemStackFactory.displayStack(Items.ARROW);
      } else if (this.containsPart(parts, "trident")) {
         return DisplayItemStackFactory.displayStack(Items.TRIDENT);
      } else if (this.containsPart(parts, "boat")) {
         return DisplayItemStackFactory.displayStack(Items.OAK_BOAT);
      } else if (this.containsPart(parts, "minecart")) {
         return DisplayItemStackFactory.displayStack(Items.MINECART);
      } else {
         return this.containsPart(parts, "item") ? DisplayItemStackFactory.displayStack(Items.ITEM_FRAME) : ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_StrArr(String[] parts) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         ItemStack icon = this.read(candidate);
         if (!icon.isEmpty()) {
            return icon;
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack load(String[] parts) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         ItemStack icon = this.itemIcon(candidate);
         if (!icon.isEmpty()) {
            return icon;
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack load(String[] parts, Map<EntityType<?>, ItemStack> spawnEggs) {
      for (String candidate : this.init_List_StrArr_I(parts, 0)) {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(candidate)).orElse(null);
         if (type != null) {
            ItemStack icon = spawnEggs.getOrDefault(type, ItemStack.EMPTY);
            if (!icon.isEmpty()) {
               return icon;
            }
         }

         ItemStack item = this.itemIcon(candidate);
         if (!item.isEmpty()) {
            return item;
         }

         ItemStack block = this.read(candidate);
         if (!block.isEmpty()) {
            return block;
         }
      }

      return ItemStack.EMPTY;
   }

   private List<String> init_List_StrArr_I(String[] parts, int start) {
      List<String> out = new ArrayList<>();

      for (int len = Math.min(4, parts.length - start); len >= 1; len--) {
         for (int i = start; i + len <= parts.length; i++) {
            String candidate = this.init(parts, i, len);
            if (!candidate.isBlank() && !this.init_Z_Str_2(candidate)) {
               out.add(candidate);
            }
         }
      }

      return out;
   }

   private String init(String[] parts, int start, int len) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < len; i++) {
         String part = parts[start + i];
         if (!part.isBlank()) {
            if (!builder.isEmpty()) {
               builder.append('_');
            }

            builder.append(part);
         }
      }

      return builder.toString();
   }

   private boolean init_Z_Str_2(String candidate) {
      boolean var10000;
      switch (candidate) {
         case "ambient":
         case "angry":
         case "attack":
         case "big":
         case "break":
         case "breathe":
         case "burp":
         case "charge":
         case "click":
         case "clicking":
         case "close":
         case "crack":
         case "death":
         case "dig":
         case "drink":
         case "eat":
         case "empty":
         case "fall":
         case "fill":
         case "flap":
         case "fly":
         case "growl":
         case "hit":
         case "hurt":
         case "idle":
         case "land":
         case "loop":
         case "open":
         case "place":
         case "pop":
         case "ready":
         case "shoot":
         case "small":
         case "splash":
         case "step":
         case "swim":
         case "throw":
         case "use":
         case "walk":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean containsPart(String[] parts, String part) {
      for (String value : parts) {
         if (part.equals(value)) {
            return true;
         }
      }

      return false;
   }

   private ItemStack itemIcon(String path) {
      Item item = BuiltInRegistries.ITEM.getOptional(Identifier.withDefaultNamespace(path)).orElse(Items.AIR);
      return item == Items.AIR ? ItemStack.EMPTY : DisplayItemStackFactory.displayStack(item);
   }

   private ItemStack read(String path) {
      Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(path)).orElse(Blocks.AIR);
      return block != Blocks.AIR && block.asItem() != Items.AIR ? DisplayItemStackFactory.forBlock(block) : ItemStack.EMPTY;
   }

   private RegistrySelectorScreen.RegistryEntry plainEntry(String id) {
      return this.labeledEntry(id, id);
   }

   private RegistrySelectorScreen.RegistryEntry labeledEntry(String label, String id) {
      return new RegistrySelectorScreen.RegistryEntry(id, label, "", ItemStack.EMPTY, false);
   }

   private String entityGroup(EntityType<?> type) {
      MobCategory category = type.getCategory();
      String var10000;
      switch (category) {
         case CREATURE:
            var10000 = "Animals";
            break;
         case WATER_AMBIENT:
         case WATER_CREATURE:
         case UNDERGROUND_WATER_CREATURE:
         case AXOLOTLS:
            var10000 = "Water Animals";
            break;
         case MONSTER:
            var10000 = "Monsters";
            break;
         case AMBIENT:
            var10000 = "Ambient";
            break;
         default:
            var10000 = "Misc";
      }

      return var10000;
   }

   private String displayName(Identifier id) {
      if (id == null) {
         return "";
      }

      String path = id.getPath().replace('.', ' ').replace('_', ' ');
      String[] parts = path.split(" ");
      StringBuilder out = new StringBuilder();

      for (String part : parts) {
         if (!part.isBlank()) {
            if (!out.isEmpty()) {
               out.append(' ');
            }

            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
         }
      }

      return out.isEmpty() ? id.toString() : out.toString();
   }

   private String load(Identifier id) {
      if (id == null) {
         return "";
      } else {
         String path = id.getPath();
         if (path.startsWith("entity.")) {
            return "Entities";
         } else if (path.startsWith("block.")) {
            return "Blocks";
         } else if (path.startsWith("item.")) {
            return "Items";
         } else if (path.startsWith("music.") || path.startsWith("music_disc.") || path.startsWith("record.")) {
            return "Music";
         } else if (path.startsWith("weather.")) {
            return "Weather";
         } else if (path.startsWith("ambient.")) {
            return "Ambient";
         } else {
            return path.startsWith("ui.") ? "UI" : id.getNamespace();
         }
      }
   }

   private String lowerId(Identifier id) {
      return id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(boolean available) {
      RegistrySelectorScreen.PaneLayout pane = this.availablePane();
      return this.scrollbarMetricsAt(available, available ? pane.init() : pane.load(), pane.sync(), pane.write(), pane.flush());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetricsAt(boolean available, int paneX, int listTop, int paneW, int listH) {
      int contentH = available ? this.contentHeight(this.availableEntries()) : this.selectedContentHeight();
      int scroll = available ? this.availableScroll : this.selectedScroll;
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(contentH, Math.max(1, listH), paneX + paneW - 6, listTop, 4, Math.max(1, listH), scroll);
   }

   private int contentHeight(List<RegistrySelectorScreen.RegistryEntry> rows) {
      int rowH = this.rowHeight();
      int groupH = this.push();
      int height = 0;
      String lastGroup = "";

      for (RegistrySelectorScreen.RegistryEntry entry : rows) {
         if (this.setting != null && isGroupedKind(this.setting) && !entry.read.equals(lastGroup)) {
            height += groupH;
            lastGroup = entry.read;
         }

         height += rowH;
      }

      return rows.isEmpty() ? 0 : height + 2;
   }

   private int selectedContentHeight() {
      int size = this.selectedRows().size();
      return size == 0 ? 0 : size * this.rowHeight() + 2;
   }

   private int listContentHeight() {
      int key = this.rowHeight() * 1000 + this.push();
      if (key != this.contentHeightKey) {
         this.contentHeightKey = key;
         this.cachedContentHeight = this.contentHeight(this.entries);
      }

      return Math.max(this.cachedContentHeight, this.selectedContentHeight());
   }

   private static boolean isGroupedKind(Setting<?, ?> option) {
      return option.kind() == Kind.ENTITY_TYPE_LIST || option.kind() == Kind.SOUND_EVENT_LIST || option.kind() == Kind.STORAGE_LIST;
   }

   private boolean isOverAvailablePane(int mx, int my) {
      RegistrySelectorScreen.PaneLayout pane = this.availablePane();
      return mx >= pane.init() && mx < pane.init() + pane.write() && my >= pane.sync() && my < pane.sync() + pane.flush();
   }

   private boolean load(int mx, int my) {
      RegistrySelectorScreen.PaneLayout pane = this.availablePane();
      return mx >= pane.load() && mx < pane.load() + pane.write() && my >= pane.sync() && my < pane.sync() + pane.flush();
   }

   private RegistrySelectorScreen.PaneLayout availablePane() {
      int panelX = this.panelX();
      int panelY = this.panelY();
      int paneY = this.bodyTop() + this.flush() + 2;
      int paneH = panelY + this.get() - paneY - 4;
      int paneW = (this.panelWidth() - 8 - 4) / 2;
      int leftX = panelX + 4;
      int rightX = leftX + paneW + 4;
      int listTop = paneY + 2 + this.open();
      int listH = paneH - 4 - this.open();
      return new RegistrySelectorScreen.PaneLayout(leftX, rightX, paneY, paneW, paneH, listTop, listH);
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int flush() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int bodyTop() {
      return this.panelY() + this.headerHeight() + 4;
   }

   private int open() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(18);
   }

   private int push() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, STYLE_THEME.init_I_l(l.read), STYLE_THEME.flush());
   }

   private void clampScrolls() {
      this.availableScroll = this.clamp(this.availableScroll, 0, this.scrollbarMetrics(true).maxScroll());
      this.selectedScroll = this.clamp(this.selectedScroll, 0, this.scrollbarMetrics(false).maxScroll());
   }

   private int snapToRow(int offset) {
      int rowH = this.rowHeight();
      return Math.max(0, offset / rowH) * rowH;
   }

   private String titleText() {
      return this.setting == null ? "Select" : "Select " + this.setting.label();
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelY() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.get(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 460);
   }

   private int get() {
      int minList = 4 * this.rowHeight();
      int maxList = Math.max(minList, (int)(this.virtualHeight() * 0.65F));
      int list = this.clamp(this.listContentHeight(), minList, maxList);
      int chrome = this.headerHeight() + 4 + this.flush() + 2 + 4 + this.open() + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + list);
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int radius, int fill, int border) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), radius, fill, border);
   }

   private void load(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), STYLE_THEME.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, STYLE_THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String display = autismclient.gui.f.b.k.init(this.font, text == null ? "" : text, Math.max(0, w - 4), STYLE_THEME.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, display, STYLE_THEME.init_Identifier_l(l.read), color);
      int th = STYLE_THEME.init_I_l(l.read);
      int drawX = x + Math.max(2, (w - tw) / 2);
      int drawY = y + Math.max(1, (h - th + 1) / 2 + 1);
      autismclient.gui.f.b.k.init(graphics, this.font, display, STYLE_THEME.init_Identifier_l(l.read), color, drawX, drawY, false);
   }

   private record PaneLayout(int init, int load, int read, int write, int update, int sync, int flush) {
   }

   private enum RowAction {
      ADD,
      REMOVE,
      CLOSE;
   }

   private record SelectedRow(String id, RegistrySelectorScreen.RegistryEntry entry, boolean init_Z) {
   }

   private record RegistryEntry(String init_Str, String load, String read, ItemStack icon, boolean init_Z) {
      private RegistryEntry {
         load = load != null && !load.isBlank() ? load : init_Str;
         read = read == null ? "" : read;
         icon = icon == null ? ItemStack.EMPTY : icon;
      }

      String init_Str_2() {
         return this.load.toLowerCase(Locale.ROOT);
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }

      public String write() {
         return this.read;
      }
   }

   private record HitRegion(RegistrySelectorScreen.RowAction action, int x, int load, int read, int write, String value) {
      boolean contains(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
