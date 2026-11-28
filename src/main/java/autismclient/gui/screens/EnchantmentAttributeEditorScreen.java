package autismclient.gui.screens;

import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.modules.Module;
import autismclient.modules.f;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.RegistryDisplayNameCache;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantmentAttributeEditorScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder STYLE_THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 540;
   private static final int load_I = 318;
   private static final List<String> OPERATIONS = List.of("add_value", "add_multiplied_base", "add_multiplied_total");
   private static final List<String> SLOTS = List.of("any", "mainhand", "offhand", "hand", "head", "chest", "legs", "feet", "armor", "body", "saddle");
   private final Screen parent;
   private final Module module;
   private final Setting<?, ?> setting;
   private final EnchantmentAttributeEditorScreen.EditorMode mode;
   private final autismclient.gui.ui.widgets.TextInputWidget searchInput = newInput("Search registry");
   private final autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil = newInput("1").setValue("1");
   private final autismclient.gui.ui.widgets.TextInputWidget amountInput = newInput("1").setValue("1");
   private final autismclient.gui.ui.widgets.TextInputWidget modifierIdInput = newInput("autism:modifier").setValue("autism:modifier");
   private final List<String> entries = new ArrayList<>();
   private autismclient.gui.ui.layout.LayoutContext layoutContext;
   private List<EnchantmentAttributeEditorScreen.AvailableRow> availableRows = List.of();
   private String lastFilter = null;
   private int availableScroll;
   private int entryScroll;
   private int selectedEntry = -1;
   private String operation = OPERATIONS.get(0);
   private String slot = "mainhand";
   private autismclient.gui.f.NetHelper headerRect = emptyRect();
   private autismclient.gui.f.NetHelper saveRect = emptyRect();
   private autismclient.gui.f.NetHelper cancelRect = emptyRect();
   private autismclient.gui.f.NetHelper rawRect = emptyRect();
   private autismclient.gui.f.NetHelper update_NetHelper = emptyRect();
   private autismclient.gui.f.NetHelper sync_NetHelper = emptyRect();
   private autismclient.gui.f.NetHelper flush = emptyRect();
   private autismclient.gui.f.NetHelper close = emptyRect();
   private autismclient.gui.f.NetHelper open = emptyRect();
   private final List<EnchantmentAttributeEditorScreen.RowHit> availableHits = new ArrayList<>();
   private final List<EnchantmentAttributeEditorScreen.RowHit> entryHits = new ArrayList<>();

   public EnchantmentAttributeEditorScreen(Screen parent, Module module, Setting<?, ?> option, EnchantmentAttributeEditorScreen.EditorMode mode) {
      super(Component.literal("Edit " + mode.init_Str));
      this.parent = parent;
      this.module = module;
      this.setting = option;
      this.mode = mode;
      this.entries.addAll(parseEntries(module != null && option != null ? module.value(option.id()) : ""));
   }

   private static autismclient.gui.ui.widgets.TextInputWidget newInput(String placeholder) {
      return new autismclient.gui.ui.widgets.TextInputWidget().setPlaceholder(placeholder).setMaxLength(4096).setHorizontalPadding(4).setFixedHeight(16);
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
         int sw = this.virtualWidth();
         int sh = this.virtualHeight();
         int margin = 6;
         int pad = 4;
         int headerH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
         int ctrlH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         int w = Math.min(540, Math.max(300, sw - margin * 2));
         int h = Math.min(318, Math.max(188, sh - margin * 2));
         int x = Math.max(margin, (sw - w) / 2);
         int y = Math.max(margin, (sh - h) / 2);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
         this.layoutContext = new autismclient.gui.ui.layout.LayoutContext(
            graphics, this.font, autismclient.gui.ui.layout.LayoutViewport.create(1.0F), STYLE_THEME, mx, my, delta
         );
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1728053248);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(x, y, w, h);
         autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
            ui, panel, headerH, "Edit " + this.mode.init_Str, mx >= x && mx < x + w && my >= y && my < y + headerH
         );
         this.headerRect = autismclient.gui.ui.widgets.DialogFrameRenderer.headerBounds(panel, headerH);
         int tipY = y + headerH + 2;
         List<String> tipLines = ui.init_PoolEntry().wrapLines(this.mode.load_Str, Math.max(1, w - pad * 2));
         int tipLineCount = Math.min(2, tipLines.size());
         int lineH = 9;

         for (int i = 0; i < tipLineCount; i++) {
            ui.init_PoolEntry().draw(graphics, tipLines.get(i), x + pad, tipY + i * lineH, ui.init_TableIndex().colors().textSecondary);
         }

         int searchY = tipY + Math.max(1, tipLineCount) * lineH + 2;
         this.searchInput.setBounds(x + pad, searchY, w - pad * 2, ctrlH);
         this.searchInput.render(this.layoutContext);
         this.refilter();
         int gap = 4;
         int contentY = searchY + ctrlH + 2;
         int footerY = y + h - pad - ctrlH;
         int contentBottom = footerY - 2;
         int contentH = Math.max(50, contentBottom - contentY);
         boolean stacked = w < 430 || contentH < 132;
         if (stacked) {
            int available = Math.max(96, contentH - gap);
            int resultH = Math.max(42, Math.min(available / 2, contentH - 48));
            this.close = autismclient.gui.f.NetHelper.init(x + pad, contentY, w - pad * 2, resultH);
            this.open = autismclient.gui.f.NetHelper.init(x + pad, this.close.load() + gap, w - pad * 2, Math.max(42, contentBottom - this.close.load() - gap));
         } else {
            int leftW = Math.max(128, (w - pad * 2 - gap) / 2);
            this.close = autismclient.gui.f.NetHelper.init(x + pad, contentY, leftW, contentH);
            this.open = autismclient.gui.f.NetHelper.init(this.close.init() + gap, contentY, Math.max(120, w - pad * 2 - gap - leftW), contentH);
         }

         this.init_V_SyncManager(ui, mx, my);
         this.load(ui, mx, my);
         int rawW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ui.init_PoolEntry().measureWidth("Raw"));
         int saveW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ui.init_PoolEntry().measureWidth("Save"));
         int cancelW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ui.init_PoolEntry().measureWidth("Cancel"));
         this.rawRect = autismclient.gui.f.NetHelper.init(x + pad, footerY, rawW, ctrlH);
         this.cancelRect = autismclient.gui.f.NetHelper.init(x + w - pad - cancelW, footerY, cancelW, ctrlH);
         this.saveRect = autismclient.gui.f.NetHelper.init(this.cancelRect.read() - 2 - saveW, footerY, saveW, ctrlH);
         autismclient.gui.f.b.CachePool.init(
            ui, this.rawRect, "Raw", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.rawRect.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.saveRect, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, this.saveRect.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.cancelRect, "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.cancelRect.init_Z(mx, my), false
         );
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int mx, int my) {
      autismclient.gui.f.PacketUtil.load(
         ui.init_GuiGraphicsExtract(), this.close, 5, ui.init_TableIndex().colors().popupSurface, ui.init_TableIndex().colors().borderSubtle
      );
      int pad = 4;
      int titleH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      int headerH = 2 + titleH;
      int rowH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(this.close.read(), this.close.write() + 2, this.close.update(), titleH);
      ui.init_PoolEntry()
         .drawFitted(
            ui.init_GuiGraphicsExtract(),
            "Available",
            title.read() + pad,
            ui.init_PoolEntry().centerTextY(title),
            title.update() - pad * 2,
            ui.init_TableIndex().colors().textSecondary
         );
      this.availableHits.clear();
      int visible = Math.max(1, (this.close.sync() - headerH - 2) / rowH);
      this.availableScroll = clamp(this.availableScroll, 0, Math.max(0, this.availableRows.size() - visible));

      for (int row = 0; row < visible; row++) {
         int index = this.availableScroll + row;
         if (index >= this.availableRows.size()) {
            break;
         }

         EnchantmentAttributeEditorScreen.AvailableRow entry = this.availableRows.get(index);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
            this.close.read() + 1, this.close.write() + headerH + row * rowH, this.close.update() - 2, rowH
         );
         autismclient.gui.f.PacketUtil.init(
            ui.init_GuiGraphicsExtract(), bounds, bounds.init_Z(mx, my) ? ui.init_TableIndex().colors().cardSurfaceElevated : ui.init_TableIndex().colors().cardSurface
         );
         ui.init_PoolEntry()
            .drawFitted(
               ui.init_GuiGraphicsExtract(),
               entry.load,
               bounds.read() + pad,
               ui.init_PoolEntry().centerTextY(bounds),
               bounds.update() - pad * 2,
               ui.init_TableIndex().colors().textPrimary
            );
         this.availableHits.add(new EnchantmentAttributeEditorScreen.RowHit(bounds, index));
      }
   }

   private void load(autismclient.gui.f.SyncManager ui, int mx, int my) {
      autismclient.gui.f.PacketUtil.load(
         ui.init_GuiGraphicsExtract(), this.open, 5, ui.init_TableIndex().colors().popupSurface, ui.init_TableIndex().colors().borderSubtle
      );
      int pad = 4;
      int gap = 2;
      int ctrlH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
      int step = ctrlH + gap;
      int x = this.open.read() + pad;
      int w = this.open.update() - pad * 2;
      int top = this.open.write() + pad;
      int controlsH;
      if (this.mode == EnchantmentAttributeEditorScreen.EditorMode.ENCHANTMENTS) {
         int labelW = ui.init_PoolEntry().measureWidth("Level");
         ui.init_PoolEntry()
            .drawFitted(
               ui.init_GuiGraphicsExtract(),
               "Level",
               x,
               ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, top, labelW, ctrlH)),
               labelW + 1,
               ui.init_TableIndex().colors().textSecondary
            );
         int fieldX = x + labelW + 4;
         this.load_BitSetUtil.setBounds(fieldX, top, Math.max(42, x + w - fieldX), ctrlH);
         this.load_BitSetUtil.render(this.layoutContext);
         this.sync_NetHelper = emptyRect();
         this.flush = emptyRect();
         controlsH = pad + step;
      } else {
         int labelW = ui.init_PoolEntry().measureWidth("Amount");
         ui.init_PoolEntry()
            .drawFitted(
               ui.init_GuiGraphicsExtract(),
               "Amount",
               x,
               ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, top, labelW, ctrlH)),
               labelW + 1,
               ui.init_TableIndex().colors().textSecondary
            );
         int fieldX = x + labelW + 4;
         this.amountInput.setBounds(fieldX, top, Math.max(38, x + w - fieldX), ctrlH);
         this.amountInput.render(this.layoutContext);
         this.modifierIdInput.setBounds(x, top + step, w, ctrlH);
         this.modifierIdInput.render(this.layoutContext);
         this.sync_NetHelper = autismclient.gui.f.NetHelper.init(x, top + step * 2, Math.max(42, (w - gap) / 2), ctrlH);
         this.flush = autismclient.gui.f.NetHelper.init(
            this.sync_NetHelper.init() + gap, top + step * 2, Math.max(38, w - this.sync_NetHelper.update() - gap), ctrlH
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.sync_NetHelper, this.operationLabel(), autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.sync_NetHelper.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.flush, this.slot, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.flush.init_Z(mx, my), false
         );
         controlsH = pad + step * 3;
      }

      this.update_NetHelper = autismclient.gui.f.NetHelper.init(x, this.open.write() + controlsH, w, ctrlH);
      autismclient.gui.f.b.CachePool.init(
         ui,
         this.update_NetHelper,
         this.selectedEntry >= 0 ? "Update Selected" : "Select an available entry",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
         this.update_NetHelper.init_Z(mx, my),
         false
      );
      int labelH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      int listY = this.update_NetHelper.load() + 4;
      ui.init_PoolEntry()
         .drawFitted(
            ui.init_GuiGraphicsExtract(),
            "Current entries",
            x,
            ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, listY, w, labelH)),
            w,
            ui.init_TableIndex().colors().textSecondary
         );
      listY += labelH;
      int rowH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int listH = Math.max(rowH, this.open.load() - listY - 2);
      this.entryHits.clear();
      int visible = Math.max(1, listH / rowH);
      this.entryScroll = clamp(this.entryScroll, 0, Math.max(0, this.entries.size() - visible));
      int delH = rowH - 2;
      int delW = delH + 4;

      for (int row = 0; row < visible; row++) {
         int index = this.entryScroll + row;
         if (index >= this.entries.size()) {
            break;
         }

         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(this.open.read() + 1, listY + row * rowH, this.open.update() - 2, rowH);
         boolean selected = this.selectedEntry == index;
         autismclient.gui.f.PacketUtil.init(
            ui.init_GuiGraphicsExtract(),
            bounds,
            selected
               ? ui.init_TableIndex().colors().accentSoft
               : (bounds.init_Z(mx, my) ? ui.init_TableIndex().colors().cardSurfaceElevated : ui.init_TableIndex().colors().cardSurface)
         );
         autismclient.gui.f.NetHelper remove = autismclient.gui.f.NetHelper.init(bounds.init() - gap - delW, bounds.write() + 1, delW, delH);
         ui.init_PoolEntry()
            .drawFitted(
               ui.init_GuiGraphicsExtract(),
               this.entries.get(index),
               bounds.read() + pad,
               ui.init_PoolEntry().centerTextY(bounds),
               Math.max(1, remove.read() - gap - bounds.read() - pad),
               ui.init_TableIndex().colors().textPrimary
            );
         autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButton(ui.init_GuiGraphicsExtract(), remove.read(), remove.write(), remove.update(), remove.sync(), remove.init_Z(mx, my));
         this.entryHits.add(new EnchantmentAttributeEditorScreen.RowHit(bounds, index, remove));
      }
   }

   private void refilter() {
      String filter = this.searchInput.getValue().trim().toLowerCase(Locale.ROOT);
      if (!filter.equals(this.lastFilter)) {
         this.lastFilter = filter;
         List<EnchantmentAttributeEditorScreen.AvailableRow> rows = new ArrayList<>();
         if (this.mode == EnchantmentAttributeEditorScreen.EditorMode.ATTRIBUTES) {
            for (Entry<ResourceKey<Attribute>, Attribute> entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
               Identifier id = entry.getKey().identifier();
               Attribute attribute = entry.getValue();
               String label = Component.translatable(attribute.getDescriptionId()).getString();
               this.init(rows, filter, id.toString(), label);
            }
         } else {
            Registry<Enchantment> registry = this.enchantmentRegistry();
            if (registry != null) {
               for (Entry<ResourceKey<Enchantment>, Enchantment> entry : registry.entrySet()) {
                  Identifier id = entry.getKey().identifier();
                  Enchantment enchantment = entry.getValue();
                  String label = enchantment.description() == null ? "" : enchantment.description().getString();
                  this.init(rows, filter, id.toString(), label);
               }
            }
         }

         rows.sort(Comparator.comparing(EnchantmentAttributeEditorScreen.AvailableRow::load, String.CASE_INSENSITIVE_ORDER).thenComparing(EnchantmentAttributeEditorScreen.AvailableRow::init));
         this.availableRows = List.copyOf(rows);
         this.availableScroll = 0;
      }
   }

   private void init(List<EnchantmentAttributeEditorScreen.AvailableRow> rows, String filter, String id, String label) {
      String resolved = label != null && !label.isBlank() ? label : RegistryDisplayNameCache.genericName(id);
      String searchable = (id + " " + resolved).toLowerCase(Locale.ROOT);
      if (filter.isEmpty() || searchable.contains(filter)) {
         rows.add(new EnchantmentAttributeEditorScreen.AvailableRow(id, resolved));
      }
   }

   private Registry<Enchantment> enchantmentRegistry() {
      try {
         RegistryAccess access;
         if (this.minecraft != null && this.minecraft.level != null) {
            access = this.minecraft.level.registryAccess();
         } else if (this.minecraft != null && this.minecraft.getConnection() != null) {
            access = this.minecraft.getConnection().registryAccess();
         } else {
            access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
         }

         Optional<Registry<Enchantment>> registry = access.lookup(Registries.ENCHANTMENT);
         return registry.orElse(null);
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) == 0 && this.headerRect.init_Z(mx, my)) {
         this.onClose();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.saveRect.init_Z(mx, my)) {
         this.read();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.cancelRect.init_Z(mx, my)) {
         this.onClose();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.rawRect.init_Z(mx, my)) {
         this.write();
         if (this.minecraft != null) {
            this.minecraft
               .gui
               .setScreen(
                  new NbtTextEditorScreen(
                     this.parent,
                     this.module,
                     this.setting,
                     this.mode == EnchantmentAttributeEditorScreen.EditorMode.ENCHANTMENTS
                        ? NbtTextEditorScreen.Inner_DataUtil.ENCHANTMENTS
                        : NbtTextEditorScreen.Inner_DataUtil.ATTRIBUTES
                  )
               );
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(event) == 0 && this.sync_NetHelper.init_Z(mx, my)) {
            this.operation = nextChoice(OPERATIONS, this.operation);
            return true;
         }

         if (Compat263.mouseEventGlfwButton(event) == 0 && this.flush.init_Z(mx, my)) {
            this.slot = nextChoice(SLOTS, this.slot);
            return true;
         }

         if (Compat263.mouseEventGlfwButton(event) == 0 && this.update_NetHelper.init_Z(mx, my) && this.selectedEntry >= 0 && this.selectedEntry < this.entries.size()) {
            this.entries.set(this.selectedEntry, this.read(this.entries.get(this.selectedEntry)));
            return true;
         }

         for (EnchantmentAttributeEditorScreen.RowHit hit : this.entryHits) {
            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.load.init_Z(mx, my)) {
               this.entries.remove(hit.index);
               if (this.selectedEntry == hit.index) {
                  this.selectedEntry = -1;
               } else if (this.selectedEntry > hit.index) {
                  this.selectedEntry--;
               }

               return true;
            }

            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.bounds.init_Z(mx, my)) {
               this.selectEntry(hit.index);
               return true;
            }
         }

         for (EnchantmentAttributeEditorScreen.RowHit hit : this.availableHits) {
            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.bounds.init_Z(mx, my)) {
               this.entries.add(this.load(this.availableRows.get(hit.index).init));
               this.selectedEntry = this.entries.size() - 1;
               this.selectEntry(this.selectedEntry);
               return true;
            }
         }

         if (this.layoutContext == null) {
            return false;
         }

         this.update();
         return this.searchInput.mouseClicked(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.load_BitSetUtil.mouseClicked(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.amountInput.mouseClicked(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.modifierIdInput.mouseClicked(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      int direction = scrollY < 0.0 ? 1 : -1;
      if (this.close.init_Z(mx, my)) {
         this.availableScroll = Math.max(0, this.availableScroll + direction);
         return true;
      } else if (this.open.init_Z(mx, my)) {
         this.entryScroll = Math.max(0, this.entryScroll + direction);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.layoutContext == null) {
         return false;
      }

      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      return this.searchInput.mouseReleased(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.load_BitSetUtil.mouseReleased(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.amountInput.mouseReleased(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.modifierIdInput.mouseReleased(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.layoutContext == null) {
         return false;
      }

      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      return this.searchInput.mouseDragged(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.load_BitSetUtil.mouseDragged(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.amountInput.mouseDragged(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.modifierIdInput.mouseDragged(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.layoutContext == null
            ? false
            : this.searchInput
                  .keyPressed(this.layoutContext, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.load_BitSetUtil
                  .keyPressed(this.layoutContext, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.amountInput
                  .keyPressed(this.layoutContext, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.modifierIdInput
                  .keyPressed(this.layoutContext, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.layoutContext == null) {
         return false;
      }

      char chr = (char)input.codepoint();
      return this.searchInput.charTyped(this.layoutContext, chr, 0)
         || this.load_BitSetUtil.charTyped(this.layoutContext, chr, 0)
         || this.amountInput.charTyped(this.layoutContext, chr, 0)
         || this.modifierIdInput.charTyped(this.layoutContext, chr, 0);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void read() {
      this.write();
      bi.init(this.mode.init_Str + " updated.", -13248397);
      this.onClose();
   }

   private void write() {
      if (this.module != null && this.setting != null) {
         this.module.setValue(this.setting.id(), String.join(",", this.entries));
         if (this.module instanceof f.Inner_DataUtil adminTools) {
            adminTools.sync_V_9();
         }
      }
   }

   private void selectEntry(int index) {
      if (index >= 0 && index < this.entries.size()) {
         this.selectedEntry = index;
         String entry = this.entries.get(index);
         if (this.mode == EnchantmentAttributeEditorScreen.EditorMode.ENCHANTMENTS) {
            int split = entry.lastIndexOf(58);
            this.load_BitSetUtil.setValue(split > 0 && split < entry.length() - 1 ? entry.substring(split + 1) : "1");
         } else {
            this.amountInput.setValue(entryValue(entry, "amount", "1"));
            this.operation = load(OPERATIONS, entryValue(entry, "operation", OPERATIONS.get(0)));
            this.slot = load(SLOTS, entryValue(entry, "slot", "mainhand"));
            this.modifierIdInput.setValue(entryValue(entry, "id", "autism:modifier"));
         }
      }
   }

   private String load(String id) {
      return this.mode == EnchantmentAttributeEditorScreen.EditorMode.ENCHANTMENTS
         ? write(id) + ":" + clamp(parseIntOr(this.load_BitSetUtil.getValue(), 1), 1, 255)
         : this.init_Str_Str_Str(id, null);
   }

   private String read(String existing) {
      if (this.mode == EnchantmentAttributeEditorScreen.EditorMode.ENCHANTMENTS) {
         int split = existing.lastIndexOf(58);
         String id = split > 0 ? existing.substring(0, split) : existing;
         return write(id) + ":" + clamp(parseIntOr(this.load_BitSetUtil.getValue(), 1), 1, 255);
      } else {
         return this.init_Str_Str_Str(entryValue(existing, "type", "minecraft:generic.attack_damage"), existing);
      }
   }

   private String init_Str_Str_Str(String type, String existing) {
      String value = read(this.amountInput.getValue(), "1");
      String modifierId = write(this.modifierIdInput.getValue(), "autism:modifier");
      String display = load(existing, "display");
      return "{type:\""
         + update(type)
         + "\",amount:"
         + value
         + ",operation:\""
         + update(this.operation)
         + "\",slot:\""
         + update(this.slot)
         + "\",id:\""
         + update(modifierId)
         + "\""
         + (display.isBlank() ? "" : ",display:" + display)
         + "}";
   }

   private static String load(String entry, String key) {
      if (entry != null && key != null && !key.isBlank()) {
         String text = entry.trim();
         if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1);
         }

         for (String part : parseEntries(text)) {
            int separator = init_I_Str_7(part);
            if (separator > 0) {
               String candidate = part.substring(0, separator).trim();
               if (candidate.startsWith("\"") && candidate.endsWith("\"") || candidate.startsWith("'") && candidate.endsWith("'")) {
                  candidate = candidate.substring(1, candidate.length() - 1);
               }

               if (key.equals(candidate)) {
                  return part.substring(separator + 1).trim();
               }
            }
         }

         return "";
      } else {
         return "";
      }
   }

   private static int init_I_Str_7(String text) {
      int depth = 0;
      char quote = 0;
      boolean escaped = false;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (quote != 0) {
            if (escaped) {
               escaped = false;
            } else if (c == '\\') {
               escaped = true;
            } else if (c == quote) {
               quote = 0;
            }
         } else if (c == '"' || c == '\'') {
            quote = c;
         } else if (c == '{' || c == '[' || c == '(') {
            depth++;
         } else if (c != '}' && c != ']' && c != ')') {
            if (c == ':' && depth == 0) {
               return i;
            }
         } else {
            depth = Math.max(0, depth - 1);
         }
      }

      return -1;
   }

   private String operationLabel() {
      String var10000;
      switch (this.operation) {
         case "add_multiplied_base":
            var10000 = "Multiply Base";
            break;
         case "add_multiplied_total":
            var10000 = "Multiply Total";
            break;
         default:
            var10000 = "Add Value";
      }

      return var10000;
   }

   private void update() {
      this.searchInput.setFocused(false);
      this.load_BitSetUtil.setFocused(false);
      this.amountInput.setFocused(false);
      this.modifierIdInput.setFocused(false);
   }

   private static String entryValue(String entry, String key, String fallback) {
      if (entry == null) {
         return fallback;
      }

      String needle = key + ":";
      int start = entry.indexOf(needle);
      if (start < 0) {
         return fallback;
      }

      start += needle.length();

      while (start < entry.length() && Character.isWhitespace(entry.charAt(start))) {
         start++;
      }

      if (start >= entry.length()) {
         return fallback;
      }

      char quote = entry.charAt(start);
      if (quote != '"' && quote != '\'') {
         int end = start;

         while (end < entry.length() && entry.charAt(end) != ',' && entry.charAt(end) != '}') {
            end++;
         }

         String value = entry.substring(start, end).trim();
         return value.isEmpty() ? fallback : value;
      } else {
         int end = entry.indexOf(quote, start + 1);
         return end > start ? entry.substring(start + 1, end) : fallback;
      }
   }

   private static List<String> parseEntries(String raw) {
      List<String> out = new ArrayList<>();
      if (raw != null && !raw.isBlank()) {
         String text = raw.trim();
         if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
         }

         StringBuilder current = new StringBuilder();
         int depth = 0;
         boolean quoted = false;
         char quote = 0;
         boolean escaped = false;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
               current.append(c);
               escaped = false;
            } else if (c == '\\') {
               current.append(c);
               escaped = true;
            } else if (quoted) {
               current.append(c);
               if (c == quote) {
                  quoted = false;
               }
            } else if (c != '\'' && c != '"') {
               if (c == '{' || c == '[' || c == '(') {
                  depth++;
               } else if (c == '}' || c == ']' || c == ')') {
                  depth = Math.max(0, depth - 1);
               }

               if (c == ',' && depth == 0) {
                  String value = current.toString().trim();
                  if (!value.isEmpty()) {
                     out.add(value);
                  }

                  current.setLength(0);
               } else {
                  current.append(c);
               }
            } else {
               current.append(c);
               quoted = true;
               quote = c;
            }
         }

         String value = current.toString().trim();
         if (!value.isEmpty()) {
            out.add(value);
         }

         return out;
      } else {
         return out;
      }
   }

   private static String nextChoice(List<String> choices, String current) {
      int index = choices.indexOf(current);
      return choices.get((index + 1 + choices.size()) % choices.size());
   }

   private static String load(List<String> choices, String value) {
      return choices.contains(value) ? value : choices.get(0);
   }

   private static String read(String value, String fallback) {
      try {
         double number = Double.parseDouble(value);
         return Double.isFinite(number) ? value : fallback;
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static String write(String value, String fallback) {
      return Identifier.tryParse(value) == null ? fallback : value;
   }

   private static String write(String id) {
      return id != null && id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
   }

   private static String update(String value) {
      return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private static autismclient.gui.f.NetHelper emptyRect() {
      return autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   }

   private static int parseIntOr(String value, int fallback) {
      try {
         return Integer.parseInt(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   private record RowHit(autismclient.gui.f.NetHelper bounds, int index, autismclient.gui.f.NetHelper load) {
      private RowHit(autismclient.gui.f.NetHelper bounds, int index) {
         this(bounds, index, EnchantmentAttributeEditorScreen.emptyRect());
      }
   }

   public enum EditorMode {
      ENCHANTMENTS("Enchantments", "Pick an enchantment, set its level, then add it."),
      ATTRIBUTES("Attributes", "Pick an attribute and keep the exact modifier controls editable.");

      private final String init_Str;
      private final String load_Str;

      EditorMode(String title, String tip) {
         this.init_Str = title;
         this.load_Str = tip;
      }
   }

   private record AvailableRow(String init, String load) {
   }
}
