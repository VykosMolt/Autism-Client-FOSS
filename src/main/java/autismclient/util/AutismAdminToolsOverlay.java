package autismclient.util;

import autismclient.api.module.Kind;
import autismclient.api.module.Setting;
import autismclient.compat263.CompatGLFW;
import autismclient.gui.ui.widgets.SectionCardRenderer;
import autismclient.gui.ui.widgets.SliderRenderer;
import autismclient.gui.ui.widgets.TabStripRenderer;
import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.modules.Module;
import autismclient.modules.f;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AutismAdminToolsOverlay extends DraggableOverlayWindow {
   private static volatile AutismAdminToolsOverlay INSTANCE;
   private static final int flush_I = 320;
   private static final int close_I = 200;
   private static final int open_I = 6;
   private static final int poll = 2;
   private static final int push = 14;
   private static final int pop = 4;
   private static final int tick = 4;
   private static final int run = 2;
   private static final int get = 2;
   private static final String[] MAIN_TABS = new String[]{"Fireballs", "Item Editor", "ForceOP"};
   private static final String[] ITEM_EDITOR_TABS = new String[]{"Item", "Visual", "Stats", "Raw"};
   private static final int set = 8;
   private static final String[] FIREBALL_PRESETS = new String[]{"Balanced", "Strong", "Max", "Beam"};
   private final List<AutismAdminToolsOverlay.Inner_ConfigStore> hitRegions = new ArrayList<>();
   private int put;
   private int add;
   private int clear;
   private int reset;
   private int start;
   private autismclient.gui.ui.widgets.DropdownMenu openDropdown;
   private Setting<?, ?> dropdownSetting;
   private AutismAdminToolsOverlay.Inner_StreamUtil activeEdit;
   private Setting<?, ?> bindingSetting;
   private Setting<?, ?> draggingSlider;
   private autismclient.gui.f.NetHelper bodyBounds;
   private autismclient.gui.f.NetHelper viewportBounds;
   private autismclient.gui.f.NetHelper editFieldBounds;
   private boolean draggingTextSelection;
   private boolean draggingScrollbar;
   private boolean draggingWindow;
   private int stop;
   private boolean capturingItem;
   private boolean windowVisibleBeforeCapture;
   private boolean reopenPreviousScreen;
   private Screen previousScreen;

   private AutismAdminToolsOverlay() {
      super("admin-tools-panel", 320, 200);
      this.windowX = 88;
      this.windowY = 32;
   }

   public static synchronized AutismAdminToolsOverlay getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AutismAdminToolsOverlay();
      }

      return INSTANCE;
   }

   public static AutismAdminToolsOverlay existingInstance() {
      return INSTANCE;
   }

   public void openItemEditorTab() {
      this.put = 1;
      this.add = 0;
      this.reset = 0;
      this.setVisible(true);
   }

   public void read_V_2() {
      this.put = 1;
      this.add = 3;
      this.reset = 0;
      this.setVisible(true);
   }

   @Override
   public int getMinWidth() {
      return 288;
   }

   @Override
   public int getMinHeight() {
      return 18 + tabStripHeight() + 4 + labeledRowHeight() * 2 + 4;
   }

   private static int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private static int tabStripHeight() {
      return controlHeight();
   }

   private static int labelLineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(10);
   }

   private static int labeledRowHeight() {
      return labelLineHeight() + 1 + controlHeight();
   }

   private static int infoLineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private static int set_I_2() {
      return Math.min(26, Math.max(20, controlHeight() * 2 - 4));
   }

   private static int put() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(10);
   }

   private static int add_I_2() {
      return 17 + 8 * put() + 3;
   }

   private static int clear() {
      return 18;
   }

   private int reset() {
      return this.windowY + 14 + 4 + tabStripHeight() + 4;
   }

   @Override
   public boolean isOverResizeGrip(double mouseX, double mouseY) {
      return this.windowVisible && !this.windowCollapsed
         ? mouseX >= this.windowX + this.windowWidth - 8
            && mouseX <= this.windowX + this.windowWidth
            && mouseY >= this.windowY + this.windowHeight - 8
            && mouseY <= this.windowY + this.windowHeight
         : false;
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.windowVisible || this.isClosing()) {
         this.recordMousePosition(mouseX, mouseY);
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            f.Inner_DataUtil module = this.adminToolsModule();
            if (module != null) {
               this.update_V_4();
               OverlayBounds bounds = this.clampCurrentBounds(this);
               this.windowX = bounds.x;
               this.windowY = bounds.y;
               this.windowWidth = bounds.width;
               this.windowHeight = bounds.height;
               boolean topPopup = this.openDropdown != null;
               int baseMouseX = topPopup ? -10000 : mouseX;
               int baseMouseY = topPopup ? -10000 : mouseY;
               autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, mc.font, baseMouseX, baseMouseY);
               this.renderWindowFrame(graphics, baseMouseX, baseMouseY, bounds, "Admin Tools", this.windowCollapsed, false);
               this.hitRegions.clear();
               if (!this.isVisuallyCollapsed(this.windowCollapsed)) {
                  boolean clipped = this.beginBodyClip(graphics, bounds, this.windowCollapsed);

                  try {
                     this.renderAdminBody(context, module, bounds, baseMouseX, baseMouseY);
                  } finally {
                     this.endBodyClip(graphics, clipped);
                     this.renderInactiveDim(graphics, bounds, this.windowCollapsed, false);
                  }

                  if (this.openDropdown != null) {
                     graphics.nextStratum();
                     this.openDropdown.render(autismclient.gui.f.UiHelper.init(graphics, mc.font, mouseX, mouseY));
                  }
               }
            }
         }
      }
   }

   private void renderAdminBody(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, OverlayBounds bounds, int mouseX, int mouseY) {
      int x = bounds.x + 4;
      int y = bounds.y + 14 + 4;
      int w = Math.max(1, bounds.width - 8);
      autismclient.gui.f.NetHelper strip = autismclient.gui.f.NetHelper.init(x, y, w, tabStripHeight());

      for (int i = 0; i < MAIN_TABS.length; i++) {
         autismclient.gui.f.NetHelper tab = TabStripRenderer.init(strip, MAIN_TABS.length, i, 2);
         boolean active = i == this.put;
         TabStripRenderer.init(context, tab, MAIN_TABS[i], active);
         this.hitRegions.add(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TAB, tab, null, Integer.toString(i)));
      }

      int bodyY = y + tabStripHeight() + 4;
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(x, bodyY, w, Math.max(1, bounds.y + bounds.height - bodyY - 4));
      this.bodyBounds = body;
      this.start = this.init_I_I_3(body.update());
      this.reset = clampInt(this.reset, 0, this.open(body.sync()));
      autismclient.gui.f.NetHelper viewport = this.start > body.sync()
         ? autismclient.gui.f.NetHelper.init(body.read(), body.write(), body.update() - 6 - 2, body.sync())
         : body;
      this.viewportBounds = viewport;
      autismclient.gui.ui.UiClipStack.getInstance().push(context.init_GuiGraphicsExtract(), viewport);

      try {
         int contentY = viewport.write() - this.reset;
         switch (this.put) {
            case 1:
               this.renderItemEditorTab(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
               break;
            case 2:
               this.read(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
               break;
            default:
               this.renderFireballsTab(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
         }
      } finally {
         autismclient.gui.ui.UiClipStack.getInstance().pop(context.init_GuiGraphicsExtract());
      }

      if (this.start > body.sync()) {
         autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(body.init() - 6, body.write(), 6, body.sync());
         autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, this.start, body.sync(), this.reset);
         autismclient.gui.ui.widgets.ScrollbarRenderer.draw(context, metrics, this.draggingScrollbar);
         this.hitRegions.add(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.SCROLLBAR, track, null, ""));
      }
   }

   private int init_I_I_3(int availableWidth) {
      int var10000;
      switch (this.put) {
         case 1:
            var10000 = this.write(Math.max(1, availableWidth - 6 - 2));
            break;
         case 2:
            var10000 = this.read(availableWidth);
            break;
         default:
            var10000 = this.stop();
      }

      return var10000;
   }

   private void update_V_4() {
      if (!this.windowCollapsed) {
         int bodyWidth = Math.max(1, this.windowWidth - 8);
         int desired = 18 + tabStripHeight() + 4 + this.init_I_I_3(bodyWidth) + 4;
         int screenHeight = cA.load_I();
         int maxHeight = screenHeight > 0 ? Math.max(this.getMinHeight(), screenHeight - 8) : Math.max(200, desired);
         this.windowHeight = clampInt(desired, this.getMinHeight(), maxHeight);
      }
   }

   private static int start_I_2() {
      return clear() + labelLineHeight() + 1;
   }

   private static int load_I_I_2(int index) {
      return start_I_2() + controlHeight() + 4 + index * (controlHeight() + 2);
   }

   private int stop() {
      return load_I_I_2(1) + controlHeight() + 4;
   }

   private int read(int width) {
      int innerW = Math.max(1, width - 8);
      int cursor = clear();
      cursor += (controlHeight() + 2) * 3;
      cursor += controlHeight() + 4;
      cursor += this.flush(innerW, 3) + 2;
      cursor += this.flush(innerW, 1) + 2;
      cursor += infoLineHeight();
      return cursor + 4;
   }

   private int write(int width) {
      int innerW = Math.max(1, width - 8);
      int tabHeight = tabStripHeight() + 4;
      int var10000;
      switch (this.add) {
         case 1:
            var10000 = this.sync(innerW);
            break;
         case 2:
            var10000 = this.flush(innerW);
            break;
         case 3:
            var10000 = this.close(innerW);
            break;
         default:
            var10000 = this.update(innerW);
      }

      int content = var10000;
      return clear() + tabHeight + content + 4;
   }

   private int update(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(infoLineHeight());
      if (width >= 270) {
         sizer.init(labeledRowHeight());
      } else {
         sizer.init(labeledRowHeight());
         sizer.init(labeledRowHeight());
      }

      sizer.init(labeledRowHeight());
      sizer.init(labeledRowHeight());
      sizer.init(labeledRowHeight());
      sizer.init(this.flush(width, 3));
      sizer.init(controlHeight());
      return sizer.init();
   }

   private int sync(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(labeledRowHeight());
      sizer.init(labeledRowHeight());
      sizer.init(width >= 280 ? labeledRowHeight() : labeledRowHeight() * 2 + 2);
      sizer.init(this.flush(width, 2));
      sizer.init(controlHeight());
      return sizer.init();
   }

   private int flush(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(labeledRowHeight());
      sizer.init(this.flush(width, 2));
      sizer.init(labeledRowHeight());
      sizer.init(labeledRowHeight());
      sizer.init(infoLineHeight());
      sizer.init(controlHeight());
      return sizer.init();
   }

   private int close(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(infoLineHeight());
      sizer.init(add_I_2());
      sizer.init(this.flush(width, 3));
      sizer.init(controlHeight());
      return sizer.init();
   }

   private void renderFireballsTab(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      this.drawSectionCard(context, "Fireballs", autismclient.gui.f.NetHelper.init(x, y, w, this.stop()));
      int innerX = x + 4;
      int innerY = y + clear();
      int innerW = Math.max(1, w - 8);
      int presetW = Math.min(110, Math.max(80, innerW / 3));
      int controlY = y + start_I_2();
      this.drawSecondaryText(context, "Strength", innerX + 2, innerY, presetW - 2);
      this.init_V_SyncManager(
         context, null, autismclient.gui.f.NetHelper.init(innerX, controlY, presetW, controlHeight()), FIREBALL_PRESETS[this.clear], List.of(FIREBALL_PRESETS), mouseX, mouseY
      );
      Setting<?, ?> delay = this.findSetting(module, "fireball-delay");
      int sliderX = innerX + presetW + 4;
      this.init_V_SyncManager_3(
         context,
         module,
         "fireball-delay",
         sliderX,
         controlY,
         Math.max(90, innerW - presetW - 4),
         this.optionLabelWidth(context, delay),
         mouseX,
         mouseY
      );
      String[] bindLabels = new String[]{"Stream Bind", "Firestorm Bind"};
      String[] bindIds = new String[]{"fireball-stream-bind", "firestorm-bind"};

      for (int i = 0; i < bindIds.length; i++) {
         this.init_V_SyncManager(context, module, innerX, y + load_I_I_2(i), innerW, bindLabels[i], bindIds[i], mouseX, mouseY);
      }
   }

   private void renderItemEditorTab(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      int cardH = this.write(w);
      this.drawSectionCard(context, "Held Item Editor", autismclient.gui.f.NetHelper.init(x, y, w, cardH));
      int tx = x + 4;
      int ty = y + clear();
      int tw = Math.max(1, w - 8);
      AutismAdminToolsOverlay.Inner_CachePool flow = new AutismAdminToolsOverlay.Inner_CachePool(tx, ty, tw);
      autismclient.gui.f.NetHelper strip = flow.nextRow(tabStripHeight());

      for (int i = 0; i < ITEM_EDITOR_TABS.length; i++) {
         autismclient.gui.f.NetHelper tab = TabStripRenderer.init(strip, ITEM_EDITOR_TABS.length, i, 2);
         boolean active = i == this.add;
         TabStripRenderer.init(context, tab, ITEM_EDITOR_TABS[i], active);
         this.addHitRegion(
            new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.ITEM_TAB, tab, null, Integer.toString(i))
         );
      }

      flow.addGap(2);
      switch (this.add) {
         case 1:
            this.load_V_SyncManager(context, module, flow, mouseX, mouseY);
            break;
         case 2:
            this.read(context, module, flow, mouseX, mouseY);
            break;
         case 3:
            this.write(context, module, flow, mouseX, mouseY);
            break;
         default:
            this.init_V_SyncManager_7(context, module, flow, mouseX, mouseY);
      }
   }

   private void init_V_SyncManager_7(
      autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY
   ) {
      autismclient.gui.f.NetHelper info = flow.nextRow(infoLineHeight());
      this.read(context, "Fill held, copy a targeted held item, or type an item id.", info.read(), info.write(), info.update());
      int gap = 2;
      if (flow.load >= 270) {
         autismclient.gui.f.NetHelper row = flow.nextRow(labeledRowHeight());
         int pickW = 52;
         int numericW = 54;
         int idW = Math.max(80, row.update() - pickW - gap - (numericW + gap) * 2);
         this.init_V_SyncManager_2(
            context, module, this.findSetting(module, "nbt-item-id"), row.read(), row.write(), idW, "Item ID", mouseX, mouseY
         );
         this.init_V_SyncManager_9(
            context,
            module,
            autismclient.gui.f.NetHelper.init(row.read() + idW + gap, row.write() + labelLineHeight() + 1, pickW, controlHeight()),
            this.commandAction("List", "pick-item"),
            mouseX,
            mouseY
         );
         this.init_V_SyncManager_2(
            context,
            module,
            this.findSetting(module, "nbt-item-count"),
            row.read() + idW + gap + pickW + gap,
            row.write(),
            numericW,
            "Count",
            mouseX,
            mouseY
         );
         this.init_V_SyncManager_2(
            context,
            module,
            this.findSetting(module, "nbt-max-stack"),
            row.read() + idW + gap + pickW + gap + numericW + gap,
            row.write(),
            numericW,
            "Max",
            mouseX,
            mouseY
         );
      } else {
         autismclient.gui.f.NetHelper itemRow = flow.nextRow(labeledRowHeight());
         int pickW = 52;
         int idW = Math.max(60, itemRow.update() - pickW - gap);
         this.init_V_SyncManager_2(
            context, module, this.findSetting(module, "nbt-item-id"), itemRow.read(), itemRow.write(), idW, "Item ID", mouseX, mouseY
         );
         this.init_V_SyncManager_9(
            context,
            module,
            autismclient.gui.f.NetHelper.init(itemRow.read() + idW + gap, itemRow.write() + labelLineHeight() + 1, pickW, controlHeight()),
            this.commandAction("List", "pick-item"),
            mouseX,
            mouseY
         );
         this.init_V_SyncManager_8(context, module, flow.nextRow(labeledRowHeight()), mouseX, mouseY, "nbt-item-count", "nbt-max-stack");
      }

      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-custom-data"), flow.nextRow(labeledRowHeight()), "Custom data SNBT", mouseX, mouseY
      );
      this.init_V_SyncManager_5(
         context, module, this.findSetting(module, "nbt-item-components"), flow.nextRow(labeledRowHeight()), "Generated component patch"
      );
      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-command"), flow.nextRow(labeledRowHeight()), "Optional embedded command", mouseX, mouseY
      );
      this.init_V_SyncManager_7(
         context,
         module,
         flow.nextRow(this.flush(flow.load, 3)),
         mouseX,
         mouseY,
         this.commandAction("Pick Item", "pick-inventory-item"),
         this.optionAction("Copy Target", "copy-target-item"),
         this.optionAction("Apply to Held", "apply-held-item")
      );
      this.init_V_SyncManager_6(context, module, flow.nextRow(controlHeight()), mouseX, mouseY);
   }

   private void load_V_SyncManager(
      autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY
   ) {
      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-item-name"), flow.nextRow(labeledRowHeight()), "Display name", mouseX, mouseY
      );
      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-item-lore"), flow.nextRow(labeledRowHeight()), "Lore lines separated with |", mouseX, mouseY
      );
      Setting<?, ?> glint = this.findSetting(module, "nbt-glint");
      Setting<?, ?> rarity = this.findSetting(module, "nbt-rarity");
      Setting<?, ?> unbreakable = this.findSetting(module, "nbt-unbreakable");
      if (flow.load >= 280) {
         autismclient.gui.f.NetHelper row = flow.nextRow(labeledRowHeight());
         int g = 4;
         int glintW = Math.max(76, (row.update() - g * 2) / 3);
         int rarityW = Math.max(86, (row.update() - g * 2) / 3);
         int fieldY = row.write() + labelLineHeight() + 1;
         this.drawSecondaryText(context, "Glint", row.read() + 2, row.write(), glintW - 4);
         this.init_V_SyncManager(
            context,
            glint,
            autismclient.gui.f.NetHelper.init(row.read(), fieldY, glintW, controlHeight()),
            module.value(glint.id()),
            glint.choices(),
            mouseX,
            mouseY
         );
         this.drawSecondaryText(context, "Rarity", row.read() + glintW + g + 2, row.write(), rarityW - 4);
         this.init_V_SyncManager(
            context,
            rarity,
            autismclient.gui.f.NetHelper.init(row.read() + glintW + g, fieldY, rarityW, controlHeight()),
            module.value(rarity.id()),
            rarity.choices(),
            mouseX,
            mouseY
         );
         this.read(
            context,
            module,
            unbreakable,
            autismclient.gui.f.NetHelper.init(row.read() + glintW + rarityW + g * 2, fieldY, Math.max(40, row.update() - glintW - rarityW - g * 2), controlHeight()),
            "Unbreakable",
            mouseX,
            mouseY
         );
      } else {
         autismclient.gui.f.NetHelper row = flow.nextRow(labeledRowHeight());
         int g = 4;
         this.load_V_SyncManager(
            context,
            module,
            glint,
            autismclient.gui.f.NetHelper.init(row.read(), row.write(), Math.max(1, (row.update() - g) / 2), labeledRowHeight()),
            "Glint",
            mouseX,
            mouseY
         );
         this.load_V_SyncManager(
            context,
            module,
            rarity,
            autismclient.gui.f.NetHelper.init(row.read() + (row.update() + g) / 2, row.write(), Math.max(1, (row.update() - g) / 2), labeledRowHeight()),
            "Rarity",
            mouseX,
            mouseY
         );
         this.read(context, module, unbreakable, flow.nextRow(labeledRowHeight()), "Unbreakable", mouseX, mouseY);
      }

      this.init_V_SyncManager_7(
         context,
         module,
         flow.nextRow(this.flush(flow.load, 2)),
         mouseX,
         mouseY,
         this.commandAction("Edit Lore", "edit-lore"),
         this.commandAction("Edit Full NBT", "edit-item-stack")
      );
      this.load_V_SyncManager(
         context,
         module,
         flow.nextRow(controlHeight()),
         mouseX,
         mouseY,
         this.commandAction("Clear", "item-clear"),
         this.commandAction("Pick Item", "pick-inventory-item"),
         this.optionAction("Fill Held", "fill-held-item"),
         this.optionAction("Copy Target", "copy-target-item"),
         this.optionAction("Apply to Held", "apply-held-item"),
         this.commandAction("Give", "give-item")
      );
   }

   private void read(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY) {
      this.init_V_SyncManager_8(context, module, flow.nextRow(labeledRowHeight()), mouseX, mouseY, "nbt-max-damage", "nbt-damage");
      this.init_V_SyncManager_7(
         context,
         module,
         flow.nextRow(this.flush(flow.load, 2)),
         mouseX,
         mouseY,
         this.commandAction("Edit Enchants", "edit-enchants"),
         this.commandAction("Edit Attributes", "edit-attributes")
      );
      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-enchants"), flow.nextRow(labeledRowHeight()), "Enchantments raw list", mouseX, mouseY
      );
      this.init_V_SyncManager_3(
         context, module, this.findSetting(module, "nbt-attributes"), flow.nextRow(labeledRowHeight()), "Attributes raw list", mouseX, mouseY
      );
      autismclient.gui.f.NetHelper info = flow.nextRow(infoLineHeight());
      this.read(context, "Raw lists stay editable for exact component control.", info.read(), info.write(), info.update());
      this.load_V_SyncManager(
         context,
         module,
         flow.nextRow(controlHeight()),
         mouseX,
         mouseY,
         this.commandAction("Clear", "item-clear"),
         this.commandAction("Pick Item", "pick-inventory-item"),
         this.optionAction("Fill Held", "fill-held-item"),
         this.optionAction("Copy Target", "copy-target-item"),
         this.optionAction("Apply to Held", "apply-held-item"),
         this.commandAction("Give", "give-item")
      );
   }

   private void write(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY) {
      autismclient.gui.f.NetHelper info = flow.nextRow(infoLineHeight());
      this.read(context, "Complete ItemStack SNBT. Click the preview to edit; raw edits are authoritative.", info.read(), info.write(), info.update());
      autismclient.gui.f.NetHelper preview = flow.nextRow(add_I_2());
      this.drawSectionCard(context, "Full ItemStack SNBT", preview);
      this.addHitRegion(
         new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.ACTION, preview, null, "edit-item-stack")
      );
      List<String> lines = ItemNbtInspector.prettyLines(module.value("nbt-item-stack"));
      int x = preview.read() + 6;
      int y = preview.write() + 14 + 3;
      int width = Math.max(1, preview.update() - 12);
      int lineH = put();
      int maxLines = Math.max(1, (preview.sync() - 14 - 6) / lineH);
      int shown = Math.min(maxLines, lines.size());

      for (int i = 0; i < shown; i++) {
         String line = i == shown - 1 && lines.size() > shown ? lines.get(i) + "  ... (" + (lines.size() - shown) + " more lines)" : lines.get(i);
         this.drawNbtTokens(context, line, x, y + i * lineH, width);
      }

      this.init_V_SyncManager_7(
         context,
         module,
         flow.nextRow(this.flush(flow.load, 3)),
         mouseX,
         mouseY,
         this.commandAction("Edit Raw NBT", "edit-item-stack"),
         this.optionAction("Fill Held", "fill-held-item"),
         this.optionAction("Apply to Held", "apply-held-item")
      );
      this.load_V_SyncManager(
         context,
         module,
         flow.nextRow(controlHeight()),
         mouseX,
         mouseY,
         this.commandAction("Copy Raw", "copy-item-stack-snbt"),
         this.commandAction("Clear", "item-clear"),
         this.commandAction("Give", "give-item")
      );
   }

   private void drawNbtTokens(autismclient.gui.f.SyncManager context, String line, int x, int y, int maxWidth) {
      int cursor = x;
      int remaining = maxWidth;

      for (ItemNbtInspector.Inner_TaskQueue token : ItemNbtInspector.highlight(line, context.init_TableIndex().colors().textPrimary)) {
         if (token != null && token.text() != null && !token.text().isEmpty() && remaining > 0) {
            String text = context.init_PoolEntry().ellipsize(token.text(), remaining);
            context.init_PoolEntry().draw(context.init_GuiGraphicsExtract(), text, cursor, y, token.color());
            int width = context.init_PoolEntry().measureWidth(text);
            cursor += width;
            remaining -= width;
            if (!text.equals(token.text())) {
               break;
            }
         }
      }
   }

   private void read(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      int innerX = x + 4;
      int innerW = Math.max(1, w - 8);
      int cursor = y + clear();
      this.drawSectionCard(context, "AuthMe ForceOP", autismclient.gui.f.NetHelper.init(x, y, w, this.read(w)));
      String[] sliders = new String[]{"forceop-delay", "forceop-min-length", "forceop-max-length"};
      int labelW = 0;

      for (String id : sliders) {
         labelW = Math.max(labelW, this.optionLabelWidth(context, this.findSetting(module, id)));
      }

      for (String id : sliders) {
         this.init_V_SyncManager_3(context, module, id, innerX, cursor, innerW, labelW, mouseX, mouseY);
         cursor += controlHeight() + 2;
      }

      Setting<?, ?> wait = this.findSetting(module, "forceop-wait");
      autismclient.gui.f.NetHelper waitRow = autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, controlHeight());
      autismclient.gui.f.NetHelper toggle = autismclient.gui.f.NetHelper.init(innerX, cursor, set_I_2(), controlHeight());
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(
         context, toggle, Boolean.parseBoolean(module.value(wait.id())), toggle.init_Z(mouseX, mouseY), "admintools:" + module.id() + "/" + wait.id()
      );
      this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.BOOLEAN, waitRow, wait, ""));
      this.drawSecondaryText(
         context, wait.label(), toggle.init() + 5, context.init_PoolEntry().centerTextY(waitRow), Math.max(1, waitRow.init() - toggle.init() - 7)
      );
      cursor += controlHeight() + 4;
      int actionH = this.flush(innerW, 3);
      this.init_V_SyncManager_7(
         context,
         module,
         autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, actionH),
         mouseX,
         mouseY,
         this.commandAction("Load TXT", "forceop-load"),
         this.commandAction("Unload", "forceop-unload"),
         this.commandAction("Preview", "forceop-preview")
      );
      cursor += actionH + 2;
      int runH = this.flush(innerW, 1);
      this.init_V_SyncManager_7(
         context,
         module,
         autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, runH),
         mouseX,
         mouseY,
         this.commandAction(module.read_Z_17() ? "Stop" : "Start", module.read_Z_17() ? "forceop-stop" : "forceop-start")
      );
      cursor += runH + 2;
      this.drawFittedText(
         context,
         "Passwords: " + module.init_I_26() + "   Attempt: " + module.load_I_14() + " / " + module.init_I_26(),
         innerX,
         cursor,
         innerW,
         module.read_Z_17() ? context.init_TableIndex().colors().accent : context.init_TableIndex().colors().textSecondary
      );
   }

   private void drawSectionCard(autismclient.gui.f.SyncManager context, String title, autismclient.gui.f.NetHelper bounds) {
      SectionCardRenderer.init(context, bounds, title);
   }

   private void init_V_SyncManager(
      autismclient.gui.f.SyncManager context, Module module, int x, int y, int w, int mouseX, int mouseY, AutismAdminToolsOverlay.Inner_DataUtil... actions
   ) {
      this.init_V_SyncManager_7(
         context, module, autismclient.gui.f.NetHelper.init(x, y, w, this.flush(w, actions == null ? 0 : actions.length)), mouseX, mouseY, actions
      );
   }

   private void init_V_SyncManager_7(
      autismclient.gui.f.SyncManager context,
      Module module,
      autismclient.gui.f.NetHelper bounds,
      int mouseX,
      int mouseY,
      AutismAdminToolsOverlay.Inner_DataUtil... actions
   ) {
      if (actions != null && actions.length != 0) {
         int gap = 2;
         int minCellW = 54;
         int x = bounds.read();
         int y = bounds.write();
         int w = bounds.update();
         int columns = Math.max(1, Math.min(actions.length, (w + gap) / Math.max(1, minCellW + gap)));

         for (int index = 0; index < actions.length; index++) {
            AutismAdminToolsOverlay.Inner_DataUtil action = actions[index];
            int rowIndex = index / columns;
            int colIndex = index % columns;
            int yOffset = rowIndex * (controlHeight() + gap);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y + yOffset, w, controlHeight());
            int countInRow = Math.min(columns, actions.length - rowIndex * columns);
            autismclient.gui.f.NetHelper cell = autismclient.gui.ui.widgets.CellSplitHelper.init(row, countInRow, colIndex, gap);
            autismclient.gui.f.b.CachePool.Inner_DataUtil tone = !action.init.toLowerCase(Locale.ROOT).contains("run") && !action.init.equals("Start")
               ? (
                  !action.init.equals("Stop") && !action.init.equals("Clear")
                     ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init
                     : autismclient.gui.f.b.CachePool.Inner_DataUtil.update
               )
               : autismclient.gui.f.b.CachePool.Inner_DataUtil.read;
            autismclient.gui.f.b.CachePool.init(context, cell, action.init, tone, cell.init_Z(mouseX, mouseY), false);
            this.addHitRegion(
               new AutismAdminToolsOverlay.Inner_ConfigStore(
                  AutismAdminToolsOverlay.Inner_TaskQueue.ACTION,
                  cell,
                  action.load == null ? null : this.findSetting(module, action.load),
                  action.read
               )
            );
         }
      }
   }

   private void load_V_SyncManager(
      autismclient.gui.f.SyncManager context,
      Module module,
      autismclient.gui.f.NetHelper bounds,
      int mouseX,
      int mouseY,
      AutismAdminToolsOverlay.Inner_DataUtil... actions
   ) {
      if (actions != null && actions.length != 0) {
         int gap = 2;
         int pad = 6;
         int n = actions.length;
         int[] want = new int[n];
         long total = (long)gap * (n - 1);
         long labelTotal = 0L;

         for (int i = 0; i < n; i++) {
            int label = context.init_PoolEntry().measureWidth(actions[i].init);
            labelTotal += label;
            want[i] = Math.max(1, label + pad);
            total += want[i];
         }

         int x = bounds.read();
         int usable = Math.max(1, bounds.update() - gap * (n - 1));

         for (int i = 0; i < n; i++) {
            int w;
            if (i == n - 1) {
               w = Math.max(1, bounds.init() - x);
            } else if (total <= bounds.update()) {
               int leftover = (int)(bounds.update() - total);
               w = want[i] + leftover / n;
            } else {
               w = (int)Math.max(1L, (long)usable * context.init_PoolEntry().measureWidth(actions[i].init) / Math.max(1L, labelTotal));
            }

            this.init_V_SyncManager_9(context, module, autismclient.gui.f.NetHelper.init(x, bounds.write(), w, controlHeight()), actions[i], mouseX, mouseY);
            x += w + gap;
         }
      }
   }

   private void init_V_SyncManager_6(autismclient.gui.f.SyncManager context, Module module, autismclient.gui.f.NetHelper bounds, int mouseX, int mouseY) {
      this.load_V_SyncManager(
         context,
         module,
         bounds,
         mouseX,
         mouseY,
         this.commandAction("Clear", "item-clear"),
         this.optionAction("Fill", "fill-held-item"),
         this.commandAction("Edit Custom Data", "edit-custom-data"),
         this.commandAction("Edit Full NBT", "edit-item-stack"),
         this.commandAction("Give", "give-item")
      );
   }

   private int flush(int width, int actionCount) {
      if (actionCount <= 0) {
         return 0;
      }

      int gap = 2;
      int minCellW = 54;
      int columns = Math.max(1, Math.min(actionCount, (Math.max(1, width) + gap) / Math.max(1, minCellW + gap)));
      int rows = (int)Math.ceil((double)actionCount / columns);
      return rows * controlHeight() + Math.max(0, rows - 1) * gap;
   }

   private void init_V_SyncManager_9(
      autismclient.gui.f.SyncManager context,
      Module module,
      autismclient.gui.f.NetHelper bounds,
      AutismAdminToolsOverlay.Inner_DataUtil action,
      int mouseX,
      int mouseY
   ) {
      if (action != null) {
         autismclient.gui.f.b.CachePool.init(
            context, bounds, action.init, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, bounds.init_Z(mouseX, mouseY), false
         );
         this.addHitRegion(
            new AutismAdminToolsOverlay.Inner_ConfigStore(
               AutismAdminToolsOverlay.Inner_TaskQueue.ACTION,
               bounds,
               action.load == null ? null : this.findSetting(module, action.load),
               action.read
            )
         );
      }
   }

   private void init_V_SyncManager(
      autismclient.gui.f.SyncManager context, Module module, int x, int y, int w, String label, String optionId, int mouseX, int mouseY
   ) {
      Setting<?, ?> option = this.findSetting(module, optionId);
      int clearW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(context.init_PoolEntry().measureWidth("Clear"));
      autismclient.gui.f.NetHelper clear = autismclient.gui.f.NetHelper.init(x + w - clearW, y, clearW, controlHeight());
      autismclient.gui.f.NetHelper bind = autismclient.gui.f.NetHelper.init(clear.read() - 2 - 78, y, 78, controlHeight());
      this.drawSecondaryText(
         context, label, x + 2, context.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, y, w, controlHeight())), Math.max(1, bind.read() - x - 6)
      );
      boolean focused = this.bindingSetting == option;
      autismclient.gui.f.b.CachePool.init(
         context,
         bind,
         focused ? "Press key" : ValueSink.init_Str(parseIntOr(module.value(option.id()), -1)),
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         bind.init_Z(mouseX, mouseY),
         focused
      );
      autismclient.gui.f.b.CachePool.init(context, clear, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, clear.init_Z(mouseX, mouseY), false);
      this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.KEYBIND, bind, option, ""));
      this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.CLEAR_KEYBIND, clear, option, ""));
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, Module module, int x, int y, int w, int mouseX, int mouseY, String... ids) {
      this.init_V_SyncManager_8(context, module, autismclient.gui.f.NetHelper.init(x, y, w, labeledRowHeight()), mouseX, mouseY, ids);
   }

   private void init_V_SyncManager_8(
      autismclient.gui.f.SyncManager context, Module module, autismclient.gui.f.NetHelper bounds, int mouseX, int mouseY, String... ids
   ) {
      int gap = 4;

      for (int index = 0; index < ids.length; index++) {
         String id = ids[index];
         autismclient.gui.f.NetHelper field = autismclient.gui.ui.widgets.RectSplitHelper.init(bounds, ids.length, index, gap);
         this.init_V_SyncManager_2(
            context,
            module,
            this.findSetting(module, id),
            field.read(),
            field.write(),
            field.update(),
            this.findSetting(module, id).label(),
            mouseX,
            mouseY
         );
      }
   }

   private void init_V_SyncManager_2(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, int x, int y, int w, String label, int mouseX, int mouseY
   ) {
      this.init_V_SyncManager_3(context, module, option, autismclient.gui.f.NetHelper.init(x, y, w, labeledRowHeight()), label, mouseX, mouseY);
   }

   private void init_V_SyncManager_3(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label, int mouseX, int mouseY
   ) {
      if (option != null) {
         this.drawSecondaryText(context, label, row.read() + 2, row.write(), row.update() - 4);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(row.read(), row.write() + labelLineHeight() + 1, row.update(), controlHeight());
         boolean focused = this.activeEdit != null && this.activeEdit.setting == option;
         TextFieldRenderer.init(
            context,
            bounds,
            focused ? this.activeEdit.text : module.value(option.id()),
            "",
            focused,
            focused ? this.activeEdit.cursor : module.value(option.id()).length(),
            focused ? this.activeEdit.selectionStart() : -1,
            focused ? this.activeEdit.selectionEnd() : -1,
            focused ? this.init_I_SyncManager_NetHelper(context, bounds) : 0
         );
         this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TEXT, bounds, option, ""));
      }
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, int x, int y, int w, String label) {
      this.init_V_SyncManager_5(context, module, option, autismclient.gui.f.NetHelper.init(x, y, w, labeledRowHeight()), label);
   }

   private void init_V_SyncManager_5(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label
   ) {
      this.drawSecondaryText(context, label, row.read() + 2, row.write(), row.update() - 4);
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(row.read(), row.write() + labelLineHeight() + 1, row.update(), controlHeight());
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(context, bounds, module.displayValue(option), "", false, 0);
   }

   private void load_V_SyncManager(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label, int mouseX, int mouseY
   ) {
      if (option != null) {
         this.drawSecondaryText(context, label, row.read() + 2, row.write(), row.update() - 4);
         this.init_V_SyncManager(
            context,
            option,
            autismclient.gui.f.NetHelper.init(row.read(), row.write() + labelLineHeight() + 1, row.update(), controlHeight()),
            module.value(option.id()),
            option.choices(),
            mouseX,
            mouseY
         );
      }
   }

   private void read(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper bounds, String label, int mouseX, int mouseY
   ) {
      if (option != null) {
         autismclient.gui.f.NetHelper toggle = autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), set_I_2(), controlHeight());
         autismclient.gui.ui.widgets.WidgetRenderFacade.init(
            context, toggle, Boolean.parseBoolean(module.value(option.id())), bounds.init_Z(mouseX, mouseY), "admintools:" + module.id() + "/" + option.id()
         );
         this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.BOOLEAN, bounds, option, ""));
         this.drawSecondaryText(
            context,
            label,
            toggle.init() + 5,
            context.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), controlHeight())),
            Math.max(1, bounds.init() - toggle.init() - 7)
         );
      }
   }

   private int optionLabelWidth(autismclient.gui.f.SyncManager context, Setting<?, ?> option) {
      return Math.min(96, context.init_PoolEntry().measureWidth(option == null ? "" : option.label()) + 2);
   }

   private void init_V_SyncManager_3(
      autismclient.gui.f.SyncManager context, Module module, String optionId, int x, int y, int w, int labelW, int mouseX, int mouseY
   ) {
      Setting<?, ?> option = this.findSetting(module, optionId);
      int controlX = x + labelW + 4;
      this.drawSecondaryText(context, option.label(), x + 2, context.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, y, w, controlHeight())), labelW);
      autismclient.gui.f.NetHelper control = autismclient.gui.f.NetHelper.init(controlX, y, Math.max(60, x + w - controlX), controlHeight());
      this.init_V_SyncManager_6(context, module, option, control, mouseX, mouseY);
   }

   private void init_V_SyncManager_6(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper control, int mouseX, int mouseY
   ) {
      int valueW = Math.min(48, Math.max(36, control.update() / 4));
      autismclient.gui.f.NetHelper slider = autismclient.gui.f.NetHelper.init(
         control.read(), control.write(), Math.max(40, control.update() - valueW - 4), control.sync()
      );
      autismclient.gui.f.NetHelper value = autismclient.gui.f.NetHelper.init(slider.init() + 4, control.write(), valueW, control.sync());
      double current = option.kind() == Kind.INTEGER
         ? parseIntOr(module.value(option.id()), parseIntOr(option.defaultValue(), 0))
         : parseDoubleOr(module.value(option.id()), parseDoubleOr(option.defaultValue(), 0.0));
      SliderRenderer.init(context, slider, SliderRenderer.ratio(current, option.sliderMin(), option.sliderMax()), slider.init_Z(mouseX, mouseY));
      boolean focused = this.activeEdit != null && this.activeEdit.setting == option;
      TextFieldRenderer.init(
         context,
         value,
         focused ? this.activeEdit.text : module.value(option.id()),
         "",
         focused,
         focused ? this.activeEdit.cursor : module.value(option.id()).length(),
         focused ? this.activeEdit.selectionStart() : -1,
         focused ? this.activeEdit.selectionEnd() : -1,
         focused ? this.init_I_SyncManager_NetHelper(context, value) : 0
      );
      this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.SLIDER, slider, option, ""));
      this.addHitRegion(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TEXT, value, option, ""));
   }

   private void init_V_SyncManager(
      autismclient.gui.f.SyncManager context,
      Setting<?, ?> option,
      autismclient.gui.f.NetHelper bounds,
      String label,
      List<String> choices,
      int mouseX,
      int mouseY
   ) {
      boolean open = this.openDropdown != null && this.dropdownSetting == option && this.openDropdown.getBounds().equals(bounds);
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(context, bounds, label, bounds.init_Z(mouseX, mouseY), open);
      this.addHitRegion(
         new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.DROPDOWN, bounds, option, String.join("\u0000", choices))
      );
      if (!open && this.openDropdown != null && this.openDropdown.getBounds().equals(bounds)) {
         this.dropdownSetting = option;
      }
   }

   private int init_I_SyncManager_NetHelper(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds) {
      this.editFieldBounds = bounds;
      this.activeEdit.load = TextFieldRenderer.init(
         this.activeEdit.text,
         this.activeEdit.cursor,
         bounds.update() - 8,
         fragment -> context.init_PoolEntry().measureFragmentWidth(this.activeEdit.text, fragment),
         this.activeEdit.load
      );
      return this.activeEdit.load;
   }

   private void drawSecondaryText(autismclient.gui.f.SyncManager context, String text, int x, int y, int width) {
      this.drawFittedText(context, text, x, y, width, context.init_TableIndex().colors().textSecondary);
   }

   private void read(autismclient.gui.f.SyncManager context, String text, int x, int y, int width) {
      this.drawFittedText(context, text, x, y, width, context.init_TableIndex().colors().textSecondary);
   }

   private void drawFittedText(autismclient.gui.f.SyncManager context, String text, int x, int y, int width, int color) {
      context.init_PoolEntry().drawFitted(context.init_GuiGraphicsExtract(), text == null ? "" : text, x, y, Math.max(1, width), color);
   }

   private void addHitRegion(AutismAdminToolsOverlay.Inner_ConfigStore hit) {
      if (this.viewportBounds == null || this.rectsOverlap(this.viewportBounds, hit.bounds)) {
         this.hitRegions.add(hit);
      }
   }

   private boolean rectsOverlap(autismclient.gui.f.NetHelper a, autismclient.gui.f.NetHelper b) {
      return b.init() > a.read() && b.read() < a.init() && b.load() > a.write() && b.write() < a.load();
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.windowVisible) {
         return false;
      }

      if (button == 0) {
         OverlayBounds bounds = this.getBounds();
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         }

         if (this.isOverHeader(mouseX, mouseY)) {
            this.draggingWindow = true;
            return true;
         }
      }

      if (this.windowCollapsed) {
         return true;
      }

      f.Inner_DataUtil module = this.adminToolsModule();
      if (module == null) {
         return true;
      }

      if (this.openDropdown != null) {
         this.openDropdown.onMouseClicked((int)mouseX, (int)mouseY, button);
         if (!this.openDropdown.isOpen()) {
            this.closeDropdown();
         }

         return true;
      } else if (this.activeEdit == null
         || this.editFieldBounds == null
         || !this.editFieldBounds.init_Z((int)mouseX, (int)mouseY)
         || this.viewportBounds != null && !this.viewportBounds.init_Z((int)mouseX, (int)mouseY)) {
         this.finishTextEdit(true);

         for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
            AutismAdminToolsOverlay.Inner_ConfigStore hit = this.hitRegions.get(i);
            if (hit.bounds.init_Z((int)mouseX, (int)mouseY)) {
               switch (hit.kind) {
                  case TAB:
                     this.put = clampInt(parseIntOr(hit.value, 0), 0, MAIN_TABS.length - 1);
                     this.reset = 0;
                     return true;
                  case ITEM_TAB:
                     this.add = clampInt(parseIntOr(hit.value, 0), 0, ITEM_EDITOR_TABS.length - 1);
                     if (this.add == 3) {
                        module.sync_V_9();
                     }

                     this.reset = 0;
                     return true;
                  case BOOLEAN:
                     module.setValue(hit.setting.id(), Boolean.toString(!Boolean.parseBoolean(module.value(hit.setting.id()))));
                     if (hit.setting.id().startsWith("nbt-")) {
                        module.sync_V_9();
                     }

                     return true;
                  case TEXT:
                     if (button == 0 && (this.viewportBounds == null || this.viewportBounds.init_Z((int)mouseX, (int)mouseY))) {
                        this.beginTextEditAt(module, hit.setting, (int)mouseX, hit.bounds);
                     }

                     return true;
                  case SLIDER:
                     this.draggingSlider = hit.setting;
                     this.updateDraggedSlider(module, (int)mouseX);
                     return true;
                  case DROPDOWN:
                     List<String> values = hit.value.isEmpty() ? List.of() : List.of(hit.value.split("\u0000", -1));
                     String selected = hit.setting == null ? FIREBALL_PRESETS[this.clear] : module.value(hit.setting.id());
                     this.dropdownSetting = hit.setting;
                     this.openDropdown = new autismclient.gui.ui.widgets.DropdownMenu(hit.bounds, values, selected, value -> {
                        if (hit.setting == null) {
                           this.clear = Math.max(0, values.indexOf(value));
                           this.applyFireballPreset(module, this.clear);
                        } else {
                           module.setValue(hit.setting.id(), value);
                           if (hit.setting.id().startsWith("nbt-")) {
                              module.sync_V_9();
                           }
                        }
                     });
                     this.openDropdown.open();
                     return true;
                  case KEYBIND:
                     this.bindingSetting = hit.setting;
                     return true;
                  case CLEAR_KEYBIND:
                     module.setValue(hit.setting.id(), "-1");
                     return true;
                  case ACTION:
                     this.activateHit(module, hit);
                     return true;
                  case SCROLLBAR:
                     autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = this.scrollbarBounds();
                     if (metrics != null && metrics.maxScroll() > 0) {
                        this.stop = metrics.load().init_Z((int)mouseX, (int)mouseY) ? (int)mouseY - metrics.load().write() : metrics.load().sync() / 2;
                        this.draggingScrollbar = true;
                        this.reset = autismclient.gui.ui.widgets.ScrollbarRenderer.scrollForDrag(metrics, (int)mouseY, this.stop);
                     }

                     return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      } else {
         if (button == 0) {
            long handle = Minecraft.getInstance().getWindow().handle();
            boolean shift = CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
            this.moveEditCursor(this.cursorIndexAt(this.activeEdit.text, (int)mouseX, this.editFieldBounds, this.activeEdit.load), shift);
            this.draggingTextSelection = true;
         }

         return true;
      }
   }

   private void activateHit(f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_ConfigStore hit) {
      if (hit.setting != null && hit.setting.action() != null) {
         hit.setting.action().run();
      } else {
         switch (hit.value) {
            case "give-item":
               module.flush();
               break;
            case "item-clear":
               this.resetItemEditorOptions(module);
               break;
            case "pick-item":
               this.openItemPicker(module, hit.bounds);
               break;
            case "pick-inventory-item":
               this.startItemCapture(module);
               break;
            case "edit-lore":
               this.openNbtTextEditor(module, "nbt-item-lore", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.LORE);
               break;
            case "edit-enchants":
               this.openEnchantmentEditor(module, "nbt-enchants", autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS);
               break;
            case "edit-attributes":
               this.openEnchantmentEditor(module, "nbt-attributes", autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil.ATTRIBUTES);
               break;
            case "edit-custom-data":
               this.openNbtTextEditor(module, "nbt-custom-data", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.RAW);
               break;
            case "edit-item-stack":
               module.sync_V_9();
               this.openNbtTextEditor(module, "nbt-item-stack", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.FULL_NBT);
               break;
            case "copy-item-stack-snbt":
               Minecraft mcx = Minecraft.getInstance();
               module.sync_V_9();
               String raw = module.value("nbt-item-stack");
               if (mcx != null && mcx.keyboardHandler != null && raw != null && !raw.isBlank()) {
                  mcx.keyboardHandler.setClipboard(raw);
                  bi.init("Copied full ItemStack NBT.");
               } else {
                  bi.write("No full ItemStack NBT to copy.");
               }
               break;
            case "forceop-load":
               module.init_V_37();
               break;
            case "forceop-unload":
               module.load_V_27();
               break;
            case "forceop-start":
               module.read_V_18();
               break;
            case "forceop-stop":
               module.write_V_17();
               break;
            case "forceop-preview":
               Minecraft mc = Minecraft.getInstance();
               if (mc != null) {
                  mc.gui.setScreen(new autismclient.gui.screens.ForceOpPreviewScreen(mc.gui.screen(), module.init_StrArr_2()));
               }
         }
      }
   }

   private void openNbtTextEditor(Module module, String optionId, autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil mode) {
      Minecraft mc = Minecraft.getInstance();
      Setting<?, ?> option = this.findSetting(module, optionId);
      if (mc != null && option != null) {
         mc.gui.setScreen(new autismclient.gui.screens.NbtTextEditorScreen(mc.gui.screen(), module, option, mode));
      }
   }

   private void openEnchantmentEditor(Module module, String optionId, autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil mode) {
      Minecraft mc = Minecraft.getInstance();
      Setting<?, ?> option = this.findSetting(module, optionId);
      if (mc != null && option != null) {
         mc.gui.setScreen(new autismclient.gui.screens.EnchantmentAttributeEditorScreen(mc.gui.screen(), module, option, mode));
      }
   }

   private void openItemPicker(Module module, autismclient.gui.f.NetHelper anchor) {
      this.closeDropdown();
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.gui.setScreen(new autismclient.gui.screens.PickItemScreen(mc.gui.screen(), module.value("nbt-item-id"), value -> {
            if (module instanceof f.Inner_DataUtil adminTools) {
               adminTools.init_V_2(value);
               adminTools.sync_V_9();
            } else {
               module.setValue("nbt-item-id", value);
            }
         }));
      }
   }

   private void startItemCapture(f.Inner_DataUtil module) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         this.finishTextEdit(true);
         this.closeDropdown();
         this.capturingItem = true;
         this.windowVisibleBeforeCapture = this.windowVisible;
         this.windowVisible = false;
         this.reopenPreviousScreen = !(mc.gui.screen() instanceof AbstractContainerScreen);
         this.previousScreen = this.reopenPreviousScreen ? mc.gui.screen() : null;
         AutismSessionState.getInstance().setTimedCaptureMode(true);
         AutismSessionState.getInstance().setCaptureCancelCallback(this::cancelItemCapture);
         bi.init("Right-click an inventory item to pick it.", -13248397);
         if (this.reopenPreviousScreen) {
            mc.execute(() -> {
               if (mc.player != null) {
                  mc.gui.setScreen(new InventoryScreen(mc.player));
               }
            });
         }
      } else {
         bi.read("Join a world first.");
      }
   }

   public boolean close_Z_2() {
      return this.capturingItem;
   }

   public boolean open_Z_2() {
      return this.capturingItem;
   }

   public String captureTitle() {
      return "Capturing NBT item";
   }

   public String captureHint() {
      return "Right-click a slot to fill the NBT item editor. Esc = cancel";
   }

   public String captureSlotLabel(Slot slot, String itemName, String registryId) {
      if (slot == null) {
         return "";
      }

      String itemText = registryId != null && !registryId.isBlank() ? registryId : itemName;
      if (itemText == null || itemText.isBlank()) {
         itemText = "Empty slot";
      }

      int visibleSlot = InventorySlotHelper.toVisibleSlot(Minecraft.getInstance(), slot);
      return "Hover: " + visibleSlot + " | " + itemText;
   }

   public boolean captureFromSlot(Slot slot) {
      if (!this.capturingItem) {
         return false;
      } else {
         ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
         f.Inner_DataUtil module = this.adminToolsModule();
         if (module != null && !stack.isEmpty()) {
            module.init(stack, false);
            bi.init("NBT editor filled from " + BuiltInRegistries.ITEM.getKey(stack.getItem()) + ".", -13248397);
            this.endItemCapture();
            return true;
         } else {
            bi.read("Pick a non-empty item slot.");
            return true;
         }
      }
   }

   public void cancelItemCapture() {
      if (this.capturingItem) {
         bi.read("Item pick cancelled.");
         this.endItemCapture();
      }
   }

   private void endItemCapture() {
      Minecraft mc = Minecraft.getInstance();
      this.capturingItem = false;
      this.windowVisible = this.windowVisibleBeforeCapture;
      this.windowVisibleBeforeCapture = false;
      AutismSessionState.getInstance().setTimedCaptureMode(false);
      AutismSessionState.getInstance().setCaptureCancelCallback(null);
      OverlayManager.getInstance().bringToFront(this);
      if (this.reopenPreviousScreen) {
         Screen restore = this.previousScreen;
         mc.execute(() -> {
            if (mc.gui.screen() instanceof InventoryScreen) {
               if (restore != null) {
                  mc.gui.setScreen(restore);
               }
            } else if (restore != null && mc.gui.screen() == null) {
               mc.gui.setScreen(restore);
            }
         });
      }

      this.reopenPreviousScreen = false;
      this.previousScreen = null;
   }

   private void resetItemEditorOptions(Module module) {
      module.setValue("nbt-item-id", "");
      module.setValue("nbt-item-count", "1");
      module.setValue("nbt-max-stack", "64");
      module.setValue("nbt-custom-data", "");
      module.setValue("nbt-item-name", "");
      module.setValue("nbt-item-lore", "");
      module.setValue("nbt-glint", "Default");
      module.setValue("nbt-rarity", "Default");
      module.setValue("nbt-unbreakable", "false");
      module.setValue("nbt-max-damage", "0");
      module.setValue("nbt-damage", "0");
      module.setValue("nbt-enchants", "");
      module.setValue("nbt-attributes", "");
      module.setValue("nbt-command", "");
      module.setValue("nbt-item-components", "");
      module.setValue("nbt-imported-components", "");
      module.setValue("nbt-imported-signature", "");
      module.setValue("nbt-item-stack", "");
      module.setValue("nbt-item-stack-signature", "");
   }

   private void applyFireballPreset(Module module, int preset) {
      String delay = module.value("fireball-delay");
      if (delay == null || delay.isBlank()) {
         delay = "1";
      }

      switch (preset) {
         case 1:
            this.setFireballOptions(module, "48", "10.0", "2.4", "5", delay, "2.5", "Cone", "true");
            break;
         case 2:
            this.setFireballOptions(module, "128", "16.0", "3.2", "8", delay, "3.0", "Cone", "true");
            break;
         case 3:
            this.setFireballOptions(module, "16", "0.0", "5.0", "6", delay, "2.5", "Look", "false");
            break;
         default:
            this.setFireballOptions(module, "12", "8.0", "1.8", "3", delay, "2.0", "Cone", "true");
      }
   }

   private void setFireballOptions(
      Module module, String count, String spread, String speed, String power, String delay, String distance, String aim, String randomize
   ) {
      module.setValue("fireball-count", count);
      module.setValue("fireball-spread", spread);
      module.setValue("fireball-speed", speed);
      module.setValue("fireball-power", power);
      module.setValue("fireball-delay", delay);
      module.setValue("fireball-distance", distance);
      module.setValue("fireball-aim", aim);
      module.setValue("fireball-randomize", randomize);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.openDropdown != null) {
         this.openDropdown.onMouseReleased((int)mouseX, (int)mouseY, button);
      }

      this.draggingWindow = false;
      this.draggingScrollbar = false;
      this.draggingTextSelection = false;
      this.draggingSlider = null;
      return this.windowVisible && this.containsPoint(mouseX, mouseY);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0 && this.draggingWindow) {
         OverlayBounds current = this.getBounds();
         this.setBounds(
            new OverlayBounds(
               current.x + (int)Math.round(deltaX), current.y + (int)Math.round(deltaY), current.width, current.height, current.visible, current.collapsed
            )
         );
         return true;
      }

      f.Inner_DataUtil module = this.adminToolsModule();
      if (module == null) {
         return false;
      }

      if (this.openDropdown != null) {
         this.openDropdown.onMouseDragged((int)mouseX, (int)mouseY, button, deltaX, deltaY);
         return true;
      }

      if (this.draggingScrollbar) {
         autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = this.scrollbarBounds();
         if (metrics != null) {
            this.reset = autismclient.gui.ui.widgets.ScrollbarRenderer.scrollForDrag(metrics, (int)mouseY, this.stop);
         }

         return true;
      } else if (this.draggingSlider != null) {
         this.updateDraggedSlider(module, (int)mouseX);
         return true;
      } else if (this.draggingTextSelection && button == 0 && this.activeEdit != null && this.editFieldBounds != null) {
         this.moveEditCursor(this.cursorIndexAt(this.activeEdit.text, (int)mouseX, this.editFieldBounds, this.activeEdit.load), true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.windowVisible || this.windowCollapsed) {
         return false;
      }

      if (this.openDropdown != null) {
         this.openDropdown.onMouseScrolled((int)mouseX, (int)mouseY, amount);
         return true;
      }

      if (!this.containsPoint(mouseX, mouseY)) {
         return false;
      }

      this.reset = clampInt(this.reset + (amount < 0.0 ? labeledRowHeight() + 4 : -(labeledRowHeight() + 4)), 0, this.open(this.currentBodyBounds().sync()));
      return true;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      f.Inner_DataUtil module = this.adminToolsModule();
      if (module == null) {
         return false;
      }

      if (this.bindingSetting != null) {
         module.setValue(this.bindingSetting.id(), Integer.toString(keyCode != 256 && keyCode != 259 && keyCode != 261 ? keyCode : -1));
         this.bindingSetting = null;
         return true;
      }

      if (this.activeEdit != null) {
         boolean ctrl = (modifiers & 10) != 0;
         boolean shift = (modifiers & 1) != 0;
         if (keyCode == 256) {
            this.finishTextEdit(true);
            return true;
         } else if (ctrl && keyCode == 65) {
            this.activeEdit.selectAll();
            return true;
         } else if (ctrl && keyCode == 67) {
            this.copyEditSelection();
            return true;
         } else if (ctrl && keyCode == 88) {
            this.copyEditSelection();
            this.deleteEditSelection();
            return true;
         } else if (ctrl && keyCode == 86) {
            this.pasteIntoEdit();
            return true;
         } else if (keyCode == 257 || keyCode == 335) {
            this.finishTextEdit(true);
            return true;
         } else if (keyCode == 259 && this.deleteEditSelection()) {
            return true;
         } else if (keyCode == 259 && this.activeEdit.cursor > 0) {
            int previous = autismclient.gui.ui.widgets.Utf16TextCursor.read(this.activeEdit.text, this.activeEdit.cursor);
            this.activeEdit.text = this.activeEdit.text.substring(0, previous)
               + this.activeEdit.text.substring(this.activeEdit.cursor);
            this.activeEdit.cursor = previous;
            this.activeEdit.clearSelection();
            return true;
         } else if (keyCode == 261 && this.deleteEditSelection()) {
            return true;
         } else if (keyCode == 261 && this.activeEdit.cursor < this.activeEdit.text.length()) {
            this.activeEdit.text = this.activeEdit.text.substring(0, this.activeEdit.cursor)
               + this.activeEdit
                  .text
                  .substring(autismclient.gui.ui.widgets.Utf16TextCursor.write(this.activeEdit.text, this.activeEdit.cursor));
            this.activeEdit.clearSelection();
            return true;
         } else if (keyCode == 263) {
            this.moveEditCursor(
               !shift && this.activeEdit.hasSelection()
                  ? this.activeEdit.selectionStart()
                  : autismclient.gui.ui.widgets.Utf16TextCursor.read(this.activeEdit.text, this.activeEdit.cursor),
               shift
            );
            return true;
         } else if (keyCode == 262) {
            this.moveEditCursor(
               !shift && this.activeEdit.hasSelection()
                  ? this.activeEdit.selectionEnd()
                  : autismclient.gui.ui.widgets.Utf16TextCursor.write(this.activeEdit.text, this.activeEdit.cursor),
               shift
            );
            return true;
         } else if (keyCode == 268) {
            this.moveEditCursor(0, shift);
            return true;
         } else if (keyCode == 269) {
            this.moveEditCursor(this.activeEdit.text.length(), shift);
            return true;
         } else {
            return true;
         }
      } else if (keyCode == 256 && this.openDropdown != null) {
         this.closeDropdown();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (this.activeEdit == null) {
         return false;
      }

      if (chr >= ' ' && chr != 127) {
         this.replaceEditSelection(Character.toString(chr));
      }

      return true;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.activeEdit != null || this.bindingSetting != null;
   }

   @Override
   public void clearInputFocus() {
      this.finishTextEdit(true);
      this.bindingSetting = null;
      this.closeDropdown();
   }

   private void beginTextEditAt(Module module, Setting<?, ?> option, int mouseX, autismclient.gui.f.NetHelper bounds) {
      String value = module.value(option.id());
      this.activeEdit = new AutismAdminToolsOverlay.Inner_StreamUtil(option, value, this.cursorIndexAt(value, mouseX, bounds, 0));
      this.editFieldBounds = bounds;
      this.draggingTextSelection = true;
   }

   private int cursorIndexAt(String value, int mouseX, autismclient.gui.f.NetHelper bounds, int scrollOffset) {
      int local = mouseX - bounds.read() - 4 + scrollOffset;
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.font != null) {
         int nearest = 0;
         int distance = Math.abs(local);
         int i = 0;

         while (i <= value.length()) {
            int advance = autismclient.gui.f.b.k.init(mc.font, value, value.substring(0, i), autismclient.gui.f.a.DataUtil.init, 0);
            int nextDistance = Math.abs(advance - local);
            if (nextDistance < distance) {
               nearest = i;
               distance = nextDistance;
            }

            if (i == value.length()) {
               break;
            }

            i = autismclient.gui.ui.widgets.Utf16TextCursor.write(value, i);
         }

         return nearest;
      } else {
         return value.length();
      }
   }

   private void beginTextEdit(Module module, Setting<?, ?> option) {
      if (module != null && option != null) {
         String value = module.value(option.id());
         this.activeEdit = new AutismAdminToolsOverlay.Inner_StreamUtil(option, value, value.length());
      }
   }

   private void moveEditCursor(int cursor, boolean extendSelection) {
      if (this.activeEdit != null) {
         if (extendSelection && this.activeEdit.read < 0) {
            this.activeEdit.read = this.activeEdit.cursor;
         }

         this.activeEdit.cursor = autismclient.gui.ui.widgets.Utf16TextCursor.clampIndex(this.activeEdit.text, cursor);
         if (!extendSelection) {
            this.activeEdit.clearSelection();
         }
      }
   }

   private boolean deleteEditSelection() {
      if (this.activeEdit != null && this.activeEdit.hasSelection()) {
         int start = this.activeEdit.selectionStart();
         int end = this.activeEdit.selectionEnd();
         this.activeEdit.text = this.activeEdit.text.substring(0, start)
            + this.activeEdit.text.substring(end);
         this.activeEdit.cursor = start;
         this.activeEdit.clearSelection();
         return true;
      } else {
         return false;
      }
   }

   private void replaceEditSelection(String replacement) {
      if (this.activeEdit != null) {
         String safe = replacement == null ? "" : replacement;
         int start = this.activeEdit.hasSelection() ? this.activeEdit.selectionStart() : this.activeEdit.cursor;
         int end = this.activeEdit.hasSelection() ? this.activeEdit.selectionEnd() : this.activeEdit.cursor;
         this.activeEdit.text = this.activeEdit.text.substring(0, start)
            + safe
            + this.activeEdit.text.substring(end);
         this.activeEdit.cursor = start + safe.length();
         this.activeEdit.clearSelection();
      }
   }

   private void copyEditSelection() {
      if (this.activeEdit != null && this.activeEdit.hasSelection()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.keyboardHandler != null) {
            mc.keyboardHandler
               .setClipboard(this.activeEdit.text.substring(this.activeEdit.selectionStart(), this.activeEdit.selectionEnd()));
         }
      }
   }

   private void pasteIntoEdit() {
      if (this.activeEdit != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.keyboardHandler != null) {
            String clipboard = mc.keyboardHandler.getClipboard();
            if (clipboard != null && !clipboard.isEmpty()) {
               this.replaceEditSelection(this.sanitizeSingleLine(clipboard));
            }
         }
      }
   }

   private String sanitizeSingleLine(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder out = new StringBuilder(text.length());

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t') {
               out.append(' ');
            } else if (c >= ' ' && c != 127) {
               out.append(c);
            }
         }

         return out.toString();
      } else {
         return "";
      }
   }

   private void finishTextEdit(boolean save) {
      if (this.activeEdit != null) {
         f.Inner_DataUtil module = this.adminToolsModule();
         String optionId = this.activeEdit.setting.id();
         if (save && module != null) {
            module.setValue(optionId, this.activeEdit.text);
            if ("nbt-item-id".equals(optionId)) {
               module.update_V_13();
            }

            if (optionId.startsWith("nbt-")) {
               module.sync_V_9();
            }
         }

         this.activeEdit = null;
         this.editFieldBounds = null;
         this.draggingTextSelection = false;
      }
   }

   private void updateDraggedSlider(Module module, int mouseX) {
      if (module != null && this.draggingSlider != null) {
         for (AutismAdminToolsOverlay.Inner_ConfigStore hit : this.hitRegions) {
            if (hit.kind == AutismAdminToolsOverlay.Inner_TaskQueue.SLIDER && hit.setting == this.draggingSlider) {
               double value = SliderRenderer.init(
                  mouseX,
                  hit.bounds.read(),
                  hit.bounds.update(),
                  this.draggingSlider.sliderMin(),
                  this.draggingSlider.sliderMax(),
                  this.draggingSlider.step()
               );
               if (this.draggingSlider.kind() == Kind.INTEGER) {
                  module.setValue(this.draggingSlider.id(), Integer.toString((int)Math.round(value)));
               } else {
                  module.setValue(this.draggingSlider.id(), String.format(Locale.ROOT, "%.2f", value));
               }

               return;
            }
         }
      }
   }

   private autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds scrollbarBounds() {
      autismclient.gui.f.NetHelper body = this.currentBodyBounds();
      if (this.start <= body.sync()) {
         return null;
      }

      autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(body.init() - 6, body.write(), 6, body.sync());
      return autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, this.start, body.sync(), this.reset);
   }

   private autismclient.gui.f.NetHelper currentBodyBounds() {
      if (this.bodyBounds != null) {
         return this.bodyBounds;
      }

      int y = this.reset();
      return autismclient.gui.f.NetHelper.init(this.windowX + 4, y, this.windowWidth - 8, Math.max(1, this.windowY + this.windowHeight - y - 4));
   }

   private int open(int viewHeight) {
      return Math.max(0, this.start - Math.max(1, viewHeight));
   }

   private void closeDropdown() {
      if (this.openDropdown != null) {
         this.openDropdown.close();
      }

      this.openDropdown = null;
      this.dropdownSetting = null;
   }

   private f.Inner_DataUtil adminToolsModule() {
      return autismclient.modules.al.init_Module_Str("admin-tools") instanceof f.Inner_DataUtil adminTools ? adminTools : null;
   }

   private Setting<?, ?> findSetting(Module module, String id) {
      if (module != null && id != null) {
         for (Setting<?, ?> option : module.settings()) {
            if (id.equals(option.id())) {
               return option;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private AutismAdminToolsOverlay.Inner_DataUtil optionAction(String label, String optionId) {
      return new AutismAdminToolsOverlay.Inner_DataUtil(label, optionId, "");
   }

   private AutismAdminToolsOverlay.Inner_DataUtil commandAction(String label, String command) {
      return new AutismAdminToolsOverlay.Inner_DataUtil(label, null, command);
   }

   private String activityLabel(boolean active) {
      return active ? "Active" : "Idle";
   }

   private static int parseIntOr(String text, int fallback) {
      try {
         return Integer.parseInt(text);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static double parseDoubleOr(String text, double fallback) {
      try {
         return Double.parseDouble(text);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static final class Inner_BufferHelper {
      private int height;
      private boolean first = true;

      private void init(int rowHeight) {
         if (rowHeight > 0) {
            if (!this.first) {
               this.height += 2;
            }

            this.height += rowHeight;
            this.first = false;
         }
      }

      private int init() {
         return this.height;
      }
   }

   private static final class Inner_CachePool {
      private final int init;
      private final int load;
      private int read;

      private Inner_CachePool(int x, int y, int w) {
         this.init = x;
         this.read = y;
         this.load = Math.max(1, w);
      }

      private autismclient.gui.f.NetHelper nextRow(int height) {
         int h = Math.max(0, height);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(this.init, this.read, this.load, h);
         this.read += h + 2;
         return bounds;
      }

      private void addGap(int gap) {
         this.read = this.read + Math.max(0, gap);
      }
   }

   private record Inner_ConfigStore(
      AutismAdminToolsOverlay.Inner_TaskQueue kind, autismclient.gui.f.NetHelper bounds, Setting<?, ?> setting, String value
   ) {
   }

   private record Inner_DataUtil(String init, String load, String read) {
   }

   private static final class Inner_StreamUtil {
      private final Setting<?, ?> setting;
      private String text;
      private int cursor;
      private int load;
      private int read = -1;

      private Inner_StreamUtil(Setting<?, ?> option, String text, int cursor) {
         this.setting = option;
         this.text = text == null ? "" : text;
         this.cursor = autismclient.gui.ui.widgets.Utf16TextCursor.clampIndex(this.text, cursor);
      }

      private boolean hasSelection() {
         return this.read >= 0 && this.read != this.cursor;
      }

      private int selectionStart() {
         return this.hasSelection() ? Math.min(this.read, this.cursor) : this.cursor;
      }

      private int selectionEnd() {
         return this.hasSelection() ? Math.max(this.read, this.cursor) : this.cursor;
      }

      private void clearSelection() {
         this.read = -1;
      }

      private void selectAll() {
         this.read = 0;
         this.cursor = this.text.length();
      }
   }

   private enum Inner_TaskQueue {
      TAB,
      ITEM_TAB,
      BOOLEAN,
      TEXT,
      SLIDER,
      DROPDOWN,
      KEYBIND,
      CLEAR_KEYBIND,
      ACTION,
      SCROLLBAR;
   }
}
