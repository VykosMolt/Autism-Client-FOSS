package autismclient.gui.d;

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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 540;
   private static final int load_I = 318;
   private static final List<String> init_List = List.of("add_value", "add_multiplied_base", "add_multiplied_total");
   private static final List<String> load_List = List.of("any", "mainhand", "offhand", "hand", "head", "chest", "legs", "feet", "armor", "body", "saddle");
   private final Screen init_Screen;
   private final Module init_Module;
   private final Setting<?, ?> init_Setting;
   private final EnchantmentAttributeEditorScreen.Inner_DataUtil init_ConfigStore_DataUt;
   private final autismclient.gui.f.b.TextInputWidget init_BitSetUtil = init_BitSetUtil_Str("Search registry");
   private final autismclient.gui.f.b.TextInputWidget load_BitSetUtil = init_BitSetUtil_Str("1").init_BitSetUtil_Str("1");
   private final autismclient.gui.f.b.TextInputWidget read_BitSetUtil = init_BitSetUtil_Str("1").init_BitSetUtil_Str("1");
   private final autismclient.gui.f.b.TextInputWidget write_BitSetUtil = init_BitSetUtil_Str("autism:modifier").init_BitSetUtil_Str("autism:modifier");
   private final List<String> read_List = new ArrayList<>();
   private autismclient.gui.f.c.LayoutContext init_SyncManager;
   private List<EnchantmentAttributeEditorScreen.Inner_StreamUtil> write_List = List.of();
   private String init_Str = null;
   private int read_I;
   private int write_I;
   private int update_I = -1;
   private String load_Str = init_List.get(0);
   private String read_Str = "mainhand";
   private autismclient.gui.f.NetHelper init_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper load_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper read_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper write_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper update_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper sync_NetHelper = init_NetHelper();
   private autismclient.gui.f.NetHelper flush = init_NetHelper();
   private autismclient.gui.f.NetHelper close = init_NetHelper();
   private autismclient.gui.f.NetHelper open = init_NetHelper();
   private final List<EnchantmentAttributeEditorScreen.Inner_CachePool> update_List = new ArrayList<>();
   private final List<EnchantmentAttributeEditorScreen.Inner_CachePool> sync_List = new ArrayList<>();

   public EnchantmentAttributeEditorScreen(Screen parent, Module module, Setting<?, ?> option, EnchantmentAttributeEditorScreen.Inner_DataUtil mode) {
      super(Component.literal("Edit " + mode.init_Str));
      this.init_Screen = parent;
      this.init_Module = module;
      this.init_Setting = option;
      this.init_ConfigStore_DataUt = mode;
      this.read_List.addAll(init_List_Str_2(module != null && option != null ? module.value(option.id()) : ""));
   }

   private static autismclient.gui.f.b.TextInputWidget init_BitSetUtil_Str(String placeholder) {
      return new autismclient.gui.f.b.TextInputWidget().load_BitSetUtil_Str(placeholder).init_BitSetUtil_I(4096).load_BitSetUtil_I(4).read_BitSetUtil_I(16);
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
         int sw = this.init_I();
         int sh = this.load_I();
         int margin = 6;
         int pad = 4;
         int headerH = autismclient.gui.f.UiDensityMetrics.init(14);
         int ctrlH = autismclient.gui.f.UiDensityMetrics.init(13);
         int w = Math.min(540, Math.max(300, sw - margin * 2));
         int h = Math.min(318, Math.max(188, sh - margin * 2));
         int x = Math.max(margin, (sw - w) / 2);
         int y = Math.max(margin, (sh - h) / 2);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
         this.init_SyncManager = new autismclient.gui.f.c.LayoutContext(
            graphics, this.font, autismclient.gui.f.c.LayoutViewport.init_RingBuffer_F(1.0F), init_CharEncoder, mx, my, delta
         );
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1728053248);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(x, y, w, h);
         autismclient.gui.f.b.DialogFrameRenderer.init(
            ui, panel, headerH, "Edit " + this.init_ConfigStore_DataUt.init_Str, mx >= x && mx < x + w && my >= y && my < y + headerH
         );
         this.init_NetHelper = autismclient.gui.f.b.DialogFrameRenderer.init(panel, headerH);
         int tipY = y + headerH + 2;
         List<String> tipLines = ui.init_PoolEntry().init_List(this.init_ConfigStore_DataUt.load_Str, Math.max(1, w - pad * 2));
         int tipLineCount = Math.min(2, tipLines.size());
         int lineH = 9;

         for (int i = 0; i < tipLineCount; i++) {
            ui.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, tipLines.get(i), x + pad, tipY + i * lineH, ui.init_TableIndex().init_JsonUtil().valid);
         }

         int searchY = tipY + Math.max(1, tipLineCount) * lineH + 2;
         this.init_BitSetUtil.init_TableIndex_F(x + pad, searchY, w - pad * 2, ctrlH);
         this.init_BitSetUtil.init_V_SyncManager(this.init_SyncManager);
         this.load_V_8();
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
         int rawW = autismclient.gui.f.UiDensityMetrics.load(ui.init_PoolEntry().init("Raw"));
         int saveW = autismclient.gui.f.UiDensityMetrics.load(ui.init_PoolEntry().init("Save"));
         int cancelW = autismclient.gui.f.UiDensityMetrics.load(ui.init_PoolEntry().init("Cancel"));
         this.write_NetHelper = autismclient.gui.f.NetHelper.init(x + pad, footerY, rawW, ctrlH);
         this.read_NetHelper = autismclient.gui.f.NetHelper.init(x + w - pad - cancelW, footerY, cancelW, ctrlH);
         this.load_NetHelper = autismclient.gui.f.NetHelper.init(this.read_NetHelper.read() - 2 - saveW, footerY, saveW, ctrlH);
         autismclient.gui.f.b.CachePool.init(
            ui, this.write_NetHelper, "Raw", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.write_NetHelper.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.load_NetHelper, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, this.load_NetHelper.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.read_NetHelper, "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.read_NetHelper.init_Z(mx, my), false
         );
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ui, int mx, int my) {
      autismclient.gui.f.PacketUtil.load(
         ui.init_GuiGraphicsExtract(), this.close, 5, ui.init_TableIndex().init_JsonUtil().sync, ui.init_TableIndex().init_JsonUtil().get
      );
      int pad = 4;
      int titleH = autismclient.gui.f.UiDensityMetrics.init(12);
      int headerH = 2 + titleH;
      int rowH = autismclient.gui.f.UiDensityMetrics.init(12);
      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(this.close.read(), this.close.write() + 2, this.close.update(), titleH);
      ui.init_PoolEntry()
         .load(
            ui.init_GuiGraphicsExtract(),
            "Available",
            title.read() + pad,
            ui.init_PoolEntry().init_I(title),
            title.update() - pad * 2,
            ui.init_TableIndex().init_JsonUtil().valid
         );
      this.update_List.clear();
      int visible = Math.max(1, (this.close.sync() - headerH - 2) / rowH);
      this.read_I = init_I_I_2(this.read_I, 0, Math.max(0, this.write_List.size() - visible));

      for (int row = 0; row < visible; row++) {
         int index = this.read_I + row;
         if (index >= this.write_List.size()) {
            break;
         }

         EnchantmentAttributeEditorScreen.Inner_StreamUtil entry = this.write_List.get(index);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
            this.close.read() + 1, this.close.write() + headerH + row * rowH, this.close.update() - 2, rowH
         );
         autismclient.gui.f.PacketUtil.init(
            ui.init_GuiGraphicsExtract(), bounds, bounds.init_Z(mx, my) ? ui.init_TableIndex().init_JsonUtil().push : ui.init_TableIndex().init_JsonUtil().open
         );
         ui.init_PoolEntry()
            .load(
               ui.init_GuiGraphicsExtract(),
               entry.load,
               bounds.read() + pad,
               ui.init_PoolEntry().init_I(bounds),
               bounds.update() - pad * 2,
               ui.init_TableIndex().init_JsonUtil().prev
            );
         this.update_List.add(new EnchantmentAttributeEditorScreen.Inner_CachePool(bounds, index));
      }
   }

   private void load(autismclient.gui.f.SyncManager ui, int mx, int my) {
      autismclient.gui.f.PacketUtil.load(
         ui.init_GuiGraphicsExtract(), this.open, 5, ui.init_TableIndex().init_JsonUtil().sync, ui.init_TableIndex().init_JsonUtil().get
      );
      int pad = 4;
      int gap = 2;
      int ctrlH = autismclient.gui.f.UiDensityMetrics.init(13);
      int step = ctrlH + gap;
      int x = this.open.read() + pad;
      int w = this.open.update() - pad * 2;
      int top = this.open.write() + pad;
      int controlsH;
      if (this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS) {
         int labelW = ui.init_PoolEntry().init("Level");
         ui.init_PoolEntry()
            .load(
               ui.init_GuiGraphicsExtract(),
               "Level",
               x,
               ui.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, top, labelW, ctrlH)),
               labelW + 1,
               ui.init_TableIndex().init_JsonUtil().valid
            );
         int fieldX = x + labelW + 4;
         this.load_BitSetUtil.init_TableIndex_F(fieldX, top, Math.max(42, x + w - fieldX), ctrlH);
         this.load_BitSetUtil.init_V_SyncManager(this.init_SyncManager);
         this.sync_NetHelper = init_NetHelper();
         this.flush = init_NetHelper();
         controlsH = pad + step;
      } else {
         int labelW = ui.init_PoolEntry().init("Amount");
         ui.init_PoolEntry()
            .load(
               ui.init_GuiGraphicsExtract(),
               "Amount",
               x,
               ui.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, top, labelW, ctrlH)),
               labelW + 1,
               ui.init_TableIndex().init_JsonUtil().valid
            );
         int fieldX = x + labelW + 4;
         this.read_BitSetUtil.init_TableIndex_F(fieldX, top, Math.max(38, x + w - fieldX), ctrlH);
         this.read_BitSetUtil.init_V_SyncManager(this.init_SyncManager);
         this.write_BitSetUtil.init_TableIndex_F(x, top + step, w, ctrlH);
         this.write_BitSetUtil.init_V_SyncManager(this.init_SyncManager);
         this.sync_NetHelper = autismclient.gui.f.NetHelper.init(x, top + step * 2, Math.max(42, (w - gap) / 2), ctrlH);
         this.flush = autismclient.gui.f.NetHelper.init(
            this.sync_NetHelper.init() + gap, top + step * 2, Math.max(38, w - this.sync_NetHelper.update() - gap), ctrlH
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.sync_NetHelper, this.init_Str_3(), autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.sync_NetHelper.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.flush, this.read_Str, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.flush.init_Z(mx, my), false
         );
         controlsH = pad + step * 3;
      }

      this.update_NetHelper = autismclient.gui.f.NetHelper.init(x, this.open.write() + controlsH, w, ctrlH);
      autismclient.gui.f.b.CachePool.init(
         ui,
         this.update_NetHelper,
         this.update_I >= 0 ? "Update Selected" : "Select an available entry",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
         this.update_NetHelper.init_Z(mx, my),
         false
      );
      int labelH = autismclient.gui.f.UiDensityMetrics.init(12);
      int listY = this.update_NetHelper.load() + 4;
      ui.init_PoolEntry()
         .load(
            ui.init_GuiGraphicsExtract(),
            "Current entries",
            x,
            ui.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, listY, w, labelH)),
            w,
            ui.init_TableIndex().init_JsonUtil().valid
         );
      listY += labelH;
      int rowH = autismclient.gui.f.UiDensityMetrics.init(14);
      int listH = Math.max(rowH, this.open.load() - listY - 2);
      this.sync_List.clear();
      int visible = Math.max(1, listH / rowH);
      this.write_I = init_I_I_2(this.write_I, 0, Math.max(0, this.read_List.size() - visible));
      int delH = rowH - 2;
      int delW = delH + 4;

      for (int row = 0; row < visible; row++) {
         int index = this.write_I + row;
         if (index >= this.read_List.size()) {
            break;
         }

         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(this.open.read() + 1, listY + row * rowH, this.open.update() - 2, rowH);
         boolean selected = this.update_I == index;
         autismclient.gui.f.PacketUtil.init(
            ui.init_GuiGraphicsExtract(),
            bounds,
            selected
               ? ui.init_TableIndex().init_JsonUtil().start
               : (bounds.init_Z(mx, my) ? ui.init_TableIndex().init_JsonUtil().push : ui.init_TableIndex().init_JsonUtil().open)
         );
         autismclient.gui.f.NetHelper remove = autismclient.gui.f.NetHelper.init(bounds.init() - gap - delW, bounds.write() + 1, delW, delH);
         ui.init_PoolEntry()
            .load(
               ui.init_GuiGraphicsExtract(),
               this.read_List.get(index),
               bounds.read() + pad,
               ui.init_PoolEntry().init_I(bounds),
               Math.max(1, remove.read() - gap - bounds.read() - pad),
               ui.init_TableIndex().init_JsonUtil().prev
            );
         autismclient.gui.f.b.ListRowRenderer.load(ui.init_GuiGraphicsExtract(), remove.read(), remove.write(), remove.update(), remove.sync(), remove.init_Z(mx, my));
         this.sync_List.add(new EnchantmentAttributeEditorScreen.Inner_CachePool(bounds, index, remove));
      }
   }

   private void load_V_8() {
      String filter = this.init_BitSetUtil.init_Str().trim().toLowerCase(Locale.ROOT);
      if (!filter.equals(this.init_Str)) {
         this.init_Str = filter;
         List<EnchantmentAttributeEditorScreen.Inner_StreamUtil> rows = new ArrayList<>();
         if (this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ATTRIBUTES) {
            for (Entry<ResourceKey<Attribute>, Attribute> entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
               Identifier id = entry.getKey().identifier();
               Attribute attribute = entry.getValue();
               String label = Component.translatable(attribute.getDescriptionId()).getString();
               this.init(rows, filter, id.toString(), label);
            }
         } else {
            Registry<Enchantment> registry = this.init_Registry();
            if (registry != null) {
               for (Entry<ResourceKey<Enchantment>, Enchantment> entry : registry.entrySet()) {
                  Identifier id = entry.getKey().identifier();
                  Enchantment enchantment = entry.getValue();
                  String label = enchantment.description() == null ? "" : enchantment.description().getString();
                  this.init(rows, filter, id.toString(), label);
               }
            }
         }

         rows.sort(Comparator.comparing(EnchantmentAttributeEditorScreen.Inner_StreamUtil::load, String.CASE_INSENSITIVE_ORDER).thenComparing(EnchantmentAttributeEditorScreen.Inner_StreamUtil::init));
         this.write_List = List.copyOf(rows);
         this.read_I = 0;
      }
   }

   private void init(List<EnchantmentAttributeEditorScreen.Inner_StreamUtil> rows, String filter, String id, String label) {
      String resolved = label != null && !label.isBlank() ? label : RegistryDisplayNameCache.update(id);
      String searchable = (id + " " + resolved).toLowerCase(Locale.ROOT);
      if (filter.isEmpty() || searchable.contains(filter)) {
         rows.add(new EnchantmentAttributeEditorScreen.Inner_StreamUtil(id, resolved));
      }
   }

   private Registry<Enchantment> init_Registry() {
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
      if (Compat263.mouseEventGlfwButton(event) == 0 && this.init_NetHelper.init_Z(mx, my)) {
         this.onClose();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.load_NetHelper.init_Z(mx, my)) {
         this.read();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.read_NetHelper.init_Z(mx, my)) {
         this.onClose();
         return true;
      }

      if (Compat263.mouseEventGlfwButton(event) == 0 && this.write_NetHelper.init_Z(mx, my)) {
         this.write();
         if (this.minecraft != null) {
            this.minecraft
               .gui
               .setScreen(
                  new NbtTextEditorScreen(
                     this.init_Screen,
                     this.init_Module,
                     this.init_Setting,
                     this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS
                        ? NbtTextEditorScreen.Inner_DataUtil.ENCHANTMENTS
                        : NbtTextEditorScreen.Inner_DataUtil.ATTRIBUTES
                  )
               );
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(event) == 0 && this.sync_NetHelper.init_Z(mx, my)) {
            this.load_Str = init_Str_List_Str(init_List, this.load_Str);
            return true;
         }

         if (Compat263.mouseEventGlfwButton(event) == 0 && this.flush.init_Z(mx, my)) {
            this.read_Str = init_Str_List_Str(load_List, this.read_Str);
            return true;
         }

         if (Compat263.mouseEventGlfwButton(event) == 0 && this.update_NetHelper.init_Z(mx, my) && this.update_I >= 0 && this.update_I < this.read_List.size()) {
            this.read_List.set(this.update_I, this.read(this.read_List.get(this.update_I)));
            return true;
         }

         for (EnchantmentAttributeEditorScreen.Inner_CachePool hit : this.sync_List) {
            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.load.init_Z(mx, my)) {
               this.read_List.remove(hit.init_I);
               if (this.update_I == hit.init_I) {
                  this.update_I = -1;
               } else if (this.update_I > hit.init_I) {
                  this.update_I--;
               }

               return true;
            }

            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.init_NetHelper.init_Z(mx, my)) {
               this.init_V_I_3(hit.init_I);
               return true;
            }
         }

         for (EnchantmentAttributeEditorScreen.Inner_CachePool hit : this.update_List) {
            if (Compat263.mouseEventGlfwButton(event) == 0 && hit.init_NetHelper.init_Z(mx, my)) {
               this.read_List.add(this.load(this.write_List.get(hit.init_I).init));
               this.update_I = this.read_List.size() - 1;
               this.init_V_I_3(this.update_I);
               return true;
            }
         }

         if (this.init_SyncManager == null) {
            return false;
         }

         this.update();
         return this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.load_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.read_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
            || this.write_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      int direction = scrollY < 0.0 ? 1 : -1;
      if (this.close.init_Z(mx, my)) {
         this.read_I = Math.max(0, this.read_I + direction);
         return true;
      } else if (this.open.init_Z(mx, my)) {
         this.write_I = Math.max(0, this.write_I + direction);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.init_SyncManager == null) {
         return false;
      }

      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      return this.init_BitSetUtil.load_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.load_BitSetUtil.load_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.read_BitSetUtil.load_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event))
         || this.write_BitSetUtil.load_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.init_SyncManager == null) {
         return false;
      }

      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      return this.init_BitSetUtil.init_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.load_BitSetUtil.init_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.read_BitSetUtil.init_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy)
         || this.write_BitSetUtil.init_Z_SyncManager(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.init_SyncManager == null
            ? false
            : this.init_BitSetUtil
                  .init_Z_SyncManager_3(this.init_SyncManager, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.load_BitSetUtil
                  .init_Z_SyncManager_3(this.init_SyncManager, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.read_BitSetUtil
                  .init_Z_SyncManager_3(this.init_SyncManager, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))
               || this.write_BitSetUtil
                  .init_Z_SyncManager_3(this.init_SyncManager, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_SyncManager == null) {
         return false;
      }

      char chr = (char)input.codepoint();
      return this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, chr, 0)
         || this.load_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, chr, 0)
         || this.read_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, chr, 0)
         || this.write_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, chr, 0);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void read() {
      this.write();
      bi.init(this.init_ConfigStore_DataUt.init_Str + " updated.", -13248397);
      this.onClose();
   }

   private void write() {
      if (this.init_Module != null && this.init_Setting != null) {
         this.init_Module.setValue(this.init_Setting.id(), String.join(",", this.read_List));
         if (this.init_Module instanceof f.Inner_DataUtil adminTools) {
            adminTools.sync_V_9();
         }
      }
   }

   private void init_V_I_3(int index) {
      if (index >= 0 && index < this.read_List.size()) {
         this.update_I = index;
         String entry = this.read_List.get(index);
         if (this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS) {
            int split = entry.lastIndexOf(58);
            this.load_BitSetUtil.init_BitSetUtil_Str(split > 0 && split < entry.length() - 1 ? entry.substring(split + 1) : "1");
         } else {
            this.read_BitSetUtil.init_BitSetUtil_Str(init_Str_Str(entry, "amount", "1"));
            this.load_Str = load(init_List, init_Str_Str(entry, "operation", init_List.get(0)));
            this.read_Str = load(load_List, init_Str_Str(entry, "slot", "mainhand"));
            this.write_BitSetUtil.init_BitSetUtil_Str(init_Str_Str(entry, "id", "autism:modifier"));
         }
      }
   }

   private String load(String id) {
      return this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS
         ? write(id) + ":" + init_I_I_2(init_I_Str_I(this.load_BitSetUtil.init_Str(), 1), 1, 255)
         : this.init_Str_Str_Str(id, null);
   }

   private String read(String existing) {
      if (this.init_ConfigStore_DataUt == EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS) {
         int split = existing.lastIndexOf(58);
         String id = split > 0 ? existing.substring(0, split) : existing;
         return write(id) + ":" + init_I_I_2(init_I_Str_I(this.load_BitSetUtil.init_Str(), 1), 1, 255);
      } else {
         return this.init_Str_Str_Str(init_Str_Str(existing, "type", "minecraft:generic.attack_damage"), existing);
      }
   }

   private String init_Str_Str_Str(String type, String existing) {
      String value = read(this.read_BitSetUtil.init_Str(), "1");
      String modifierId = write(this.write_BitSetUtil.init_Str(), "autism:modifier");
      String display = load(existing, "display");
      return "{type:\""
         + update(type)
         + "\",amount:"
         + value
         + ",operation:\""
         + update(this.load_Str)
         + "\",slot:\""
         + update(this.read_Str)
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

         for (String part : init_List_Str_2(text)) {
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

   private String init_Str_3() {
      String var10000;
      switch (this.load_Str) {
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
      this.init_BitSetUtil.init_V_Z(false);
      this.load_BitSetUtil.init_V_Z(false);
      this.read_BitSetUtil.init_V_Z(false);
      this.write_BitSetUtil.init_V_Z(false);
   }

   private static String init_Str_Str(String entry, String key, String fallback) {
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

   private static List<String> init_List_Str_2(String raw) {
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

   private static String init_Str_List_Str(List<String> choices, String current) {
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

   private static autismclient.gui.f.NetHelper init_NetHelper() {
      return autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   }

   private static int init_I_Str_I(String value, int fallback) {
      try {
         return Integer.parseInt(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static int init_I_I_2(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   private record Inner_CachePool(autismclient.gui.f.NetHelper init_NetHelper, int init_I, autismclient.gui.f.NetHelper load) {
      private Inner_CachePool(autismclient.gui.f.NetHelper bounds, int index) {
         this(bounds, index, EnchantmentAttributeEditorScreen.init_NetHelper());
      }
   }

   public enum Inner_DataUtil {
      ENCHANTMENTS("Enchantments", "Pick an enchantment, set its level, then add it."),
      ATTRIBUTES("Attributes", "Pick an attribute and keep the exact modifier controls editable.");

      private final String init_Str;
      private final String load_Str;

      Inner_DataUtil(String title, String tip) {
         this.init_Str = title;
         this.load_Str = tip;
      }
   }

   private record Inner_StreamUtil(String init, String load) {
   }
}
