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
   private static volatile AutismAdminToolsOverlay init_AutismAdminToolsOv_2;
   private static final int flush_I = 320;
   private static final int close_I = 200;
   private static final int open_I = 6;
   private static final int poll = 2;
   private static final int push = 14;
   private static final int pop = 4;
   private static final int tick = 4;
   private static final int run = 2;
   private static final int get = 2;
   private static final String[] init_StrArr = new String[]{"Fireballs", "Item Editor", "ForceOP"};
   private static final String[] load_StrArr = new String[]{"Item", "Visual", "Stats", "Raw"};
   private static final int set = 8;
   private static final String[] read_StrArr = new String[]{"Balanced", "Strong", "Max", "Beam"};
   private final List<AutismAdminToolsOverlay.Inner_ConfigStore> init_List = new ArrayList<>();
   private int put;
   private int add;
   private int clear;
   private int reset;
   private int start;
   private autismclient.gui.ui.widgets.DropdownMenu init_RefQueue;
   private Setting<?, ?> init_Setting;
   private AutismAdminToolsOverlay.Inner_StreamUtil init_AutismAdminToolsOv;
   private Setting<?, ?> load_Setting;
   private Setting<?, ?> read_Setting;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private autismclient.gui.f.NetHelper load_NetHelper;
   private autismclient.gui.f.NetHelper read_NetHelper;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private int stop;
   private boolean flush_Z;
   private boolean close_Z;
   private boolean open_Z;
   private Screen init_Screen;

   private AutismAdminToolsOverlay() {
      super("admin-tools-panel", 320, 200);
      this.init_I_2 = 88;
      this.load_I = 32;
   }

   public static synchronized AutismAdminToolsOverlay init_AutismAdminToolsOv() {
      if (init_AutismAdminToolsOv_2 == null) {
         init_AutismAdminToolsOv_2 = new AutismAdminToolsOverlay();
      }

      return init_AutismAdminToolsOv_2;
   }

   public static AutismAdminToolsOverlay load_AutismAdminToolsOv() {
      return init_AutismAdminToolsOv_2;
   }

   public void load_V_4() {
      this.put = 1;
      this.add = 0;
      this.reset = 0;
      this.init_V_Z(true);
   }

   public void read_V_2() {
      this.put = 1;
      this.add = 3;
      this.reset = 0;
      this.init_V_Z(true);
   }

   @Override
   public int init_I() {
      return 288;
   }

   @Override
   public int load_I() {
      return 18 + pop_I_4() + 4 + run_I_2() * 2 + 4;
   }

   private static int push_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   private static int pop_I_4() {
      return push_I_4();
   }

   private static int tick_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.init(10);
   }

   private static int run_I_2() {
      return tick_I_2() + 1 + push_I_4();
   }

   private static int get_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.init(12);
   }

   private static int set_I_2() {
      return Math.min(26, Math.max(20, push_I_4() * 2 - 4));
   }

   private static int put() {
      return autismclient.gui.ui.UiDensityMetrics.init(10);
   }

   private static int add_I_2() {
      return 17 + 8 * put() + 3;
   }

   private static int clear() {
      return 18;
   }

   private int reset() {
      return this.load_I + 14 + 4 + pop_I_4() + 4;
   }

   @Override
   public boolean read_Z_D_D(double mouseX, double mouseY) {
      return this.init_Z && !this.load_Z
         ? mouseX >= this.init_I_2 + this.read_I - 8
            && mouseX <= this.init_I_2 + this.read_I
            && mouseY >= this.load_I + this.write - 8
            && mouseY <= this.load_I + this.write
         : false;
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.init_Z || this.a__Z()) {
         this.load_V_I_I(mouseX, mouseY);
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.font != null) {
            f.Inner_DataUtil module = this.init_f_DataUtil();
            if (module != null) {
               this.update_V_4();
               OverlayBounds bounds = this.init_cH_cX(this);
               this.init_I_2 = bounds.init_I;
               this.load_I = bounds.load_I;
               this.read_I = bounds.read;
               this.write = bounds.write;
               boolean topPopup = this.init_RefQueue != null;
               int baseMouseX = topPopup ? -10000 : mouseX;
               int baseMouseY = topPopup ? -10000 : mouseY;
               autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, mc.font, baseMouseX, baseMouseY);
               this.init_V_GuiGraphicsExtract_6(graphics, baseMouseX, baseMouseY, bounds, "Admin Tools", this.load_Z, false);
               this.init_List.clear();
               if (!this.init_Z_Z(this.load_Z)) {
                  boolean clipped = this.init_Z_GuiGraphicsExtract(graphics, bounds, this.load_Z);

                  try {
                     this.init_V_SyncManager_8(context, module, bounds, baseMouseX, baseMouseY);
                  } finally {
                     this.init_V_GuiGraphicsExtract_Z(graphics, clipped);
                     this.init_V_GuiGraphicsExtract_14(graphics, bounds, this.load_Z, false);
                  }

                  if (this.init_RefQueue != null) {
                     graphics.nextStratum();
                     this.init_RefQueue.init_V_SyncManager(autismclient.gui.f.UiHelper.init(graphics, mc.font, mouseX, mouseY));
                  }
               }
            }
         }
      }
   }

   private void init_V_SyncManager_8(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, OverlayBounds bounds, int mouseX, int mouseY) {
      int x = bounds.init_I + 4;
      int y = bounds.load_I + 14 + 4;
      int w = Math.max(1, bounds.read - 8);
      autismclient.gui.f.NetHelper strip = autismclient.gui.f.NetHelper.init(x, y, w, pop_I_4());

      for (int i = 0; i < init_StrArr.length; i++) {
         autismclient.gui.f.NetHelper tab = TabStripRenderer.init(strip, init_StrArr.length, i, 2);
         boolean active = i == this.put;
         TabStripRenderer.init(context, tab, init_StrArr[i], active);
         this.init_List.add(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TAB, tab, null, Integer.toString(i)));
      }

      int bodyY = y + pop_I_4() + 4;
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(x, bodyY, w, Math.max(1, bounds.load_I + bounds.write - bodyY - 4));
      this.init_NetHelper = body;
      this.start = this.init_I_I_3(body.update());
      this.reset = load_I_I(this.reset, 0, this.open(body.sync()));
      autismclient.gui.f.NetHelper viewport = this.start > body.sync()
         ? autismclient.gui.f.NetHelper.init(body.read(), body.write(), body.update() - 6 - 2, body.sync())
         : body;
      this.load_NetHelper = viewport;
      autismclient.gui.ui.UiClipStack.init_HashUtil().init(context.init_GuiGraphicsExtract(), viewport);

      try {
         int contentY = viewport.write() - this.reset;
         switch (this.put) {
            case 1:
               this.load_V_SyncManager_2(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
               break;
            case 2:
               this.read(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
               break;
            default:
               this.init_V_SyncManager_4(context, module, viewport.read(), contentY, viewport.update(), mouseX, mouseY);
         }
      } finally {
         autismclient.gui.ui.UiClipStack.init_HashUtil().init(context.init_GuiGraphicsExtract());
      }

      if (this.start > body.sync()) {
         autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(body.init() - 6, body.write(), 6, body.sync());
         autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, this.start, body.sync(), this.reset);
         autismclient.gui.ui.widgets.ScrollbarRenderer.init_V_SyncManager(context, metrics, this.update_Z);
         this.init_List.add(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.SCROLLBAR, track, null, ""));
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
      if (!this.load_Z) {
         int bodyWidth = Math.max(1, this.read_I - 8);
         int desired = 18 + pop_I_4() + 4 + this.init_I_I_3(bodyWidth) + 4;
         int screenHeight = cA.load_I();
         int maxHeight = screenHeight > 0 ? Math.max(this.load_I(), screenHeight - 8) : Math.max(200, desired);
         this.write = load_I_I(desired, this.load_I(), maxHeight);
      }
   }

   private static int start_I_2() {
      return clear() + tick_I_2() + 1;
   }

   private static int load_I_I_2(int index) {
      return start_I_2() + push_I_4() + 4 + index * (push_I_4() + 2);
   }

   private int stop() {
      return load_I_I_2(1) + push_I_4() + 4;
   }

   private int read(int width) {
      int innerW = Math.max(1, width - 8);
      int cursor = clear();
      cursor += (push_I_4() + 2) * 3;
      cursor += push_I_4() + 4;
      cursor += this.flush(innerW, 3) + 2;
      cursor += this.flush(innerW, 1) + 2;
      cursor += get_I_2();
      return cursor + 4;
   }

   private int write(int width) {
      int innerW = Math.max(1, width - 8);
      int tabHeight = pop_I_4() + 4;
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
      sizer.init(get_I_2());
      if (width >= 270) {
         sizer.init(run_I_2());
      } else {
         sizer.init(run_I_2());
         sizer.init(run_I_2());
      }

      sizer.init(run_I_2());
      sizer.init(run_I_2());
      sizer.init(run_I_2());
      sizer.init(this.flush(width, 3));
      sizer.init(push_I_4());
      return sizer.init();
   }

   private int sync(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(run_I_2());
      sizer.init(run_I_2());
      sizer.init(width >= 280 ? run_I_2() : run_I_2() * 2 + 2);
      sizer.init(this.flush(width, 2));
      sizer.init(push_I_4());
      return sizer.init();
   }

   private int flush(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(run_I_2());
      sizer.init(this.flush(width, 2));
      sizer.init(run_I_2());
      sizer.init(run_I_2());
      sizer.init(get_I_2());
      sizer.init(push_I_4());
      return sizer.init();
   }

   private int close(int width) {
      AutismAdminToolsOverlay.Inner_BufferHelper sizer = new AutismAdminToolsOverlay.Inner_BufferHelper();
      sizer.init(get_I_2());
      sizer.init(add_I_2());
      sizer.init(this.flush(width, 3));
      sizer.init(push_I_4());
      return sizer.init();
   }

   private void init_V_SyncManager_4(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      this.init_V_SyncManager(context, "Fireballs", autismclient.gui.f.NetHelper.init(x, y, w, this.stop()));
      int innerX = x + 4;
      int innerY = y + clear();
      int innerW = Math.max(1, w - 8);
      int presetW = Math.min(110, Math.max(80, innerW / 3));
      int controlY = y + start_I_2();
      this.load_V_SyncManager_2(context, "Strength", innerX + 2, innerY, presetW - 2);
      this.init_V_SyncManager(
         context, null, autismclient.gui.f.NetHelper.init(innerX, controlY, presetW, push_I_4()), read_StrArr[this.clear], List.of(read_StrArr), mouseX, mouseY
      );
      Setting<?, ?> delay = this.init_Setting_Module_Str(module, "fireball-delay");
      int sliderX = innerX + presetW + 4;
      this.init_V_SyncManager_3(
         context,
         module,
         "fireball-delay",
         sliderX,
         controlY,
         Math.max(90, innerW - presetW - 4),
         this.init_I_SyncManager_Setting(context, delay),
         mouseX,
         mouseY
      );
      String[] bindLabels = new String[]{"Stream Bind", "Firestorm Bind"};
      String[] bindIds = new String[]{"fireball-stream-bind", "firestorm-bind"};

      for (int i = 0; i < bindIds.length; i++) {
         this.init_V_SyncManager(context, module, innerX, y + load_I_I_2(i), innerW, bindLabels[i], bindIds[i], mouseX, mouseY);
      }
   }

   private void load_V_SyncManager_2(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      int cardH = this.write(w);
      this.init_V_SyncManager(context, "Held Item Editor", autismclient.gui.f.NetHelper.init(x, y, w, cardH));
      int tx = x + 4;
      int ty = y + clear();
      int tw = Math.max(1, w - 8);
      AutismAdminToolsOverlay.Inner_CachePool flow = new AutismAdminToolsOverlay.Inner_CachePool(tx, ty, tw);
      autismclient.gui.f.NetHelper strip = flow.init_NetHelper(pop_I_4());

      for (int i = 0; i < load_StrArr.length; i++) {
         autismclient.gui.f.NetHelper tab = TabStripRenderer.init(strip, load_StrArr.length, i, 2);
         boolean active = i == this.add;
         TabStripRenderer.init(context, tab, load_StrArr[i], active);
         this.init_V_AutismAdminToolsOv(
            new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.ITEM_TAB, tab, null, Integer.toString(i))
         );
      }

      flow.init_V(2);
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
      autismclient.gui.f.NetHelper info = flow.init_NetHelper(get_I_2());
      this.read(context, "Fill held, copy a targeted held item, or type an item id.", info.read(), info.write(), info.update());
      int gap = 2;
      if (flow.load >= 270) {
         autismclient.gui.f.NetHelper row = flow.init_NetHelper(run_I_2());
         int pickW = 52;
         int numericW = 54;
         int idW = Math.max(80, row.update() - pickW - gap - (numericW + gap) * 2);
         this.init_V_SyncManager_2(
            context, module, this.init_Setting_Module_Str(module, "nbt-item-id"), row.read(), row.write(), idW, "Item ID", mouseX, mouseY
         );
         this.init_V_SyncManager_9(
            context,
            module,
            autismclient.gui.f.NetHelper.init(row.read() + idW + gap, row.write() + tick_I_2() + 1, pickW, push_I_4()),
            this.load_AutismAdminToolsOv_Str_Str("List", "pick-item"),
            mouseX,
            mouseY
         );
         this.init_V_SyncManager_2(
            context,
            module,
            this.init_Setting_Module_Str(module, "nbt-item-count"),
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
            this.init_Setting_Module_Str(module, "nbt-max-stack"),
            row.read() + idW + gap + pickW + gap + numericW + gap,
            row.write(),
            numericW,
            "Max",
            mouseX,
            mouseY
         );
      } else {
         autismclient.gui.f.NetHelper itemRow = flow.init_NetHelper(run_I_2());
         int pickW = 52;
         int idW = Math.max(60, itemRow.update() - pickW - gap);
         this.init_V_SyncManager_2(
            context, module, this.init_Setting_Module_Str(module, "nbt-item-id"), itemRow.read(), itemRow.write(), idW, "Item ID", mouseX, mouseY
         );
         this.init_V_SyncManager_9(
            context,
            module,
            autismclient.gui.f.NetHelper.init(itemRow.read() + idW + gap, itemRow.write() + tick_I_2() + 1, pickW, push_I_4()),
            this.load_AutismAdminToolsOv_Str_Str("List", "pick-item"),
            mouseX,
            mouseY
         );
         this.init_V_SyncManager_8(context, module, flow.init_NetHelper(run_I_2()), mouseX, mouseY, "nbt-item-count", "nbt-max-stack");
      }

      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-custom-data"), flow.init_NetHelper(run_I_2()), "Custom data SNBT", mouseX, mouseY
      );
      this.init_V_SyncManager_5(
         context, module, this.init_Setting_Module_Str(module, "nbt-item-components"), flow.init_NetHelper(run_I_2()), "Generated component patch"
      );
      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-command"), flow.init_NetHelper(run_I_2()), "Optional embedded command", mouseX, mouseY
      );
      this.init_V_SyncManager_7(
         context,
         module,
         flow.init_NetHelper(this.flush(flow.load, 3)),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Pick Item", "pick-inventory-item"),
         this.init_AutismAdminToolsOv_Str_Str("Copy Target", "copy-target-item"),
         this.init_AutismAdminToolsOv_Str_Str("Apply to Held", "apply-held-item")
      );
      this.init_V_SyncManager_6(context, module, flow.init_NetHelper(push_I_4()), mouseX, mouseY);
   }

   private void load_V_SyncManager(
      autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY
   ) {
      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-item-name"), flow.init_NetHelper(run_I_2()), "Display name", mouseX, mouseY
      );
      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-item-lore"), flow.init_NetHelper(run_I_2()), "Lore lines separated with |", mouseX, mouseY
      );
      Setting<?, ?> glint = this.init_Setting_Module_Str(module, "nbt-glint");
      Setting<?, ?> rarity = this.init_Setting_Module_Str(module, "nbt-rarity");
      Setting<?, ?> unbreakable = this.init_Setting_Module_Str(module, "nbt-unbreakable");
      if (flow.load >= 280) {
         autismclient.gui.f.NetHelper row = flow.init_NetHelper(run_I_2());
         int g = 4;
         int glintW = Math.max(76, (row.update() - g * 2) / 3);
         int rarityW = Math.max(86, (row.update() - g * 2) / 3);
         int fieldY = row.write() + tick_I_2() + 1;
         this.load_V_SyncManager_2(context, "Glint", row.read() + 2, row.write(), glintW - 4);
         this.init_V_SyncManager(
            context,
            glint,
            autismclient.gui.f.NetHelper.init(row.read(), fieldY, glintW, push_I_4()),
            module.value(glint.id()),
            glint.choices(),
            mouseX,
            mouseY
         );
         this.load_V_SyncManager_2(context, "Rarity", row.read() + glintW + g + 2, row.write(), rarityW - 4);
         this.init_V_SyncManager(
            context,
            rarity,
            autismclient.gui.f.NetHelper.init(row.read() + glintW + g, fieldY, rarityW, push_I_4()),
            module.value(rarity.id()),
            rarity.choices(),
            mouseX,
            mouseY
         );
         this.read(
            context,
            module,
            unbreakable,
            autismclient.gui.f.NetHelper.init(row.read() + glintW + rarityW + g * 2, fieldY, Math.max(40, row.update() - glintW - rarityW - g * 2), push_I_4()),
            "Unbreakable",
            mouseX,
            mouseY
         );
      } else {
         autismclient.gui.f.NetHelper row = flow.init_NetHelper(run_I_2());
         int g = 4;
         this.load_V_SyncManager(
            context,
            module,
            glint,
            autismclient.gui.f.NetHelper.init(row.read(), row.write(), Math.max(1, (row.update() - g) / 2), run_I_2()),
            "Glint",
            mouseX,
            mouseY
         );
         this.load_V_SyncManager(
            context,
            module,
            rarity,
            autismclient.gui.f.NetHelper.init(row.read() + (row.update() + g) / 2, row.write(), Math.max(1, (row.update() - g) / 2), run_I_2()),
            "Rarity",
            mouseX,
            mouseY
         );
         this.read(context, module, unbreakable, flow.init_NetHelper(run_I_2()), "Unbreakable", mouseX, mouseY);
      }

      this.init_V_SyncManager_7(
         context,
         module,
         flow.init_NetHelper(this.flush(flow.load, 2)),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Edit Lore", "edit-lore"),
         this.load_AutismAdminToolsOv_Str_Str("Edit Full NBT", "edit-item-stack")
      );
      this.load_V_SyncManager(
         context,
         module,
         flow.init_NetHelper(push_I_4()),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Clear", "item-clear"),
         this.load_AutismAdminToolsOv_Str_Str("Pick Item", "pick-inventory-item"),
         this.init_AutismAdminToolsOv_Str_Str("Fill Held", "fill-held-item"),
         this.init_AutismAdminToolsOv_Str_Str("Copy Target", "copy-target-item"),
         this.init_AutismAdminToolsOv_Str_Str("Apply to Held", "apply-held-item"),
         this.load_AutismAdminToolsOv_Str_Str("Give", "give-item")
      );
   }

   private void read(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY) {
      this.init_V_SyncManager_8(context, module, flow.init_NetHelper(run_I_2()), mouseX, mouseY, "nbt-max-damage", "nbt-damage");
      this.init_V_SyncManager_7(
         context,
         module,
         flow.init_NetHelper(this.flush(flow.load, 2)),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Edit Enchants", "edit-enchants"),
         this.load_AutismAdminToolsOv_Str_Str("Edit Attributes", "edit-attributes")
      );
      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-enchants"), flow.init_NetHelper(run_I_2()), "Enchantments raw list", mouseX, mouseY
      );
      this.init_V_SyncManager_3(
         context, module, this.init_Setting_Module_Str(module, "nbt-attributes"), flow.init_NetHelper(run_I_2()), "Attributes raw list", mouseX, mouseY
      );
      autismclient.gui.f.NetHelper info = flow.init_NetHelper(get_I_2());
      this.read(context, "Raw lists stay editable for exact component control.", info.read(), info.write(), info.update());
      this.load_V_SyncManager(
         context,
         module,
         flow.init_NetHelper(push_I_4()),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Clear", "item-clear"),
         this.load_AutismAdminToolsOv_Str_Str("Pick Item", "pick-inventory-item"),
         this.init_AutismAdminToolsOv_Str_Str("Fill Held", "fill-held-item"),
         this.init_AutismAdminToolsOv_Str_Str("Copy Target", "copy-target-item"),
         this.init_AutismAdminToolsOv_Str_Str("Apply to Held", "apply-held-item"),
         this.load_AutismAdminToolsOv_Str_Str("Give", "give-item")
      );
   }

   private void write(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_CachePool flow, int mouseX, int mouseY) {
      autismclient.gui.f.NetHelper info = flow.init_NetHelper(get_I_2());
      this.read(context, "Complete ItemStack SNBT. Click the preview to edit; raw edits are authoritative.", info.read(), info.write(), info.update());
      autismclient.gui.f.NetHelper preview = flow.init_NetHelper(add_I_2());
      this.init_V_SyncManager(context, "Full ItemStack SNBT", preview);
      this.init_V_AutismAdminToolsOv(
         new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.ACTION, preview, null, "edit-item-stack")
      );
      List<String> lines = ItemNbtInspector.init_List_Str(module.value("nbt-item-stack"));
      int x = preview.read() + 6;
      int y = preview.write() + 14 + 3;
      int width = Math.max(1, preview.update() - 12);
      int lineH = put();
      int maxLines = Math.max(1, (preview.sync() - 14 - 6) / lineH);
      int shown = Math.min(maxLines, lines.size());

      for (int i = 0; i < shown; i++) {
         String line = i == shown - 1 && lines.size() > shown ? lines.get(i) + "  ... (" + (lines.size() - shown) + " more lines)" : lines.get(i);
         this.init_V_SyncManager_9(context, line, x, y + i * lineH, width);
      }

      this.init_V_SyncManager_7(
         context,
         module,
         flow.init_NetHelper(this.flush(flow.load, 3)),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Edit Raw NBT", "edit-item-stack"),
         this.init_AutismAdminToolsOv_Str_Str("Fill Held", "fill-held-item"),
         this.init_AutismAdminToolsOv_Str_Str("Apply to Held", "apply-held-item")
      );
      this.load_V_SyncManager(
         context,
         module,
         flow.init_NetHelper(push_I_4()),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Copy Raw", "copy-item-stack-snbt"),
         this.load_AutismAdminToolsOv_Str_Str("Clear", "item-clear"),
         this.load_AutismAdminToolsOv_Str_Str("Give", "give-item")
      );
   }

   private void init_V_SyncManager_9(autismclient.gui.f.SyncManager context, String line, int x, int y, int maxWidth) {
      int cursor = x;
      int remaining = maxWidth;

      for (ItemNbtInspector.Inner_TaskQueue token : ItemNbtInspector.init_List_Str_I(line, context.init_TableIndex().init_JsonUtil().prev)) {
         if (token != null && token.init_Str() != null && !token.init_Str().isEmpty() && remaining > 0) {
            String text = context.init_PoolEntry().load(token.init_Str(), remaining);
            context.init_PoolEntry().init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), text, cursor, y, token.init_I());
            int width = context.init_PoolEntry().init(text);
            cursor += width;
            remaining -= width;
            if (!text.equals(token.init_Str())) {
               break;
            }
         }
      }
   }

   private void read(autismclient.gui.f.SyncManager context, f.Inner_DataUtil module, int x, int y, int w, int mouseX, int mouseY) {
      int innerX = x + 4;
      int innerW = Math.max(1, w - 8);
      int cursor = y + clear();
      this.init_V_SyncManager(context, "AuthMe ForceOP", autismclient.gui.f.NetHelper.init(x, y, w, this.read(w)));
      String[] sliders = new String[]{"forceop-delay", "forceop-min-length", "forceop-max-length"};
      int labelW = 0;

      for (String id : sliders) {
         labelW = Math.max(labelW, this.init_I_SyncManager_Setting(context, this.init_Setting_Module_Str(module, id)));
      }

      for (String id : sliders) {
         this.init_V_SyncManager_3(context, module, id, innerX, cursor, innerW, labelW, mouseX, mouseY);
         cursor += push_I_4() + 2;
      }

      Setting<?, ?> wait = this.init_Setting_Module_Str(module, "forceop-wait");
      autismclient.gui.f.NetHelper waitRow = autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, push_I_4());
      autismclient.gui.f.NetHelper toggle = autismclient.gui.f.NetHelper.init(innerX, cursor, set_I_2(), push_I_4());
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(
         context, toggle, Boolean.parseBoolean(module.value(wait.id())), toggle.init_Z(mouseX, mouseY), "admintools:" + module.id() + "/" + wait.id()
      );
      this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.BOOLEAN, waitRow, wait, ""));
      this.load_V_SyncManager_2(
         context, wait.label(), toggle.init() + 5, context.init_PoolEntry().init_I(waitRow), Math.max(1, waitRow.init() - toggle.init() - 7)
      );
      cursor += push_I_4() + 4;
      int actionH = this.flush(innerW, 3);
      this.init_V_SyncManager_7(
         context,
         module,
         autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, actionH),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str("Load TXT", "forceop-load"),
         this.load_AutismAdminToolsOv_Str_Str("Unload", "forceop-unload"),
         this.load_AutismAdminToolsOv_Str_Str("Preview", "forceop-preview")
      );
      cursor += actionH + 2;
      int runH = this.flush(innerW, 1);
      this.init_V_SyncManager_7(
         context,
         module,
         autismclient.gui.f.NetHelper.init(innerX, cursor, innerW, runH),
         mouseX,
         mouseY,
         this.load_AutismAdminToolsOv_Str_Str(module.read_Z_17() ? "Stop" : "Start", module.read_Z_17() ? "forceop-stop" : "forceop-start")
      );
      cursor += runH + 2;
      this.init_V_SyncManager_10(
         context,
         "Passwords: " + module.init_I_26() + "   Attempt: " + module.load_I_14() + " / " + module.init_I_26(),
         innerX,
         cursor,
         innerW,
         module.read_Z_17() ? context.init_TableIndex().init_JsonUtil().clear : context.init_TableIndex().init_JsonUtil().valid
      );
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager context, String title, autismclient.gui.f.NetHelper bounds) {
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
            int yOffset = rowIndex * (push_I_4() + gap);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y + yOffset, w, push_I_4());
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
            this.init_V_AutismAdminToolsOv(
               new AutismAdminToolsOverlay.Inner_ConfigStore(
                  AutismAdminToolsOverlay.Inner_TaskQueue.ACTION,
                  cell,
                  action.load == null ? null : this.init_Setting_Module_Str(module, action.load),
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
            int label = context.init_PoolEntry().init(actions[i].init);
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
               w = (int)Math.max(1L, (long)usable * context.init_PoolEntry().init(actions[i].init) / Math.max(1L, labelTotal));
            }

            this.init_V_SyncManager_9(context, module, autismclient.gui.f.NetHelper.init(x, bounds.write(), w, push_I_4()), actions[i], mouseX, mouseY);
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
         this.load_AutismAdminToolsOv_Str_Str("Clear", "item-clear"),
         this.init_AutismAdminToolsOv_Str_Str("Fill", "fill-held-item"),
         this.load_AutismAdminToolsOv_Str_Str("Edit Custom Data", "edit-custom-data"),
         this.load_AutismAdminToolsOv_Str_Str("Edit Full NBT", "edit-item-stack"),
         this.load_AutismAdminToolsOv_Str_Str("Give", "give-item")
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
      return rows * push_I_4() + Math.max(0, rows - 1) * gap;
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
         this.init_V_AutismAdminToolsOv(
            new AutismAdminToolsOverlay.Inner_ConfigStore(
               AutismAdminToolsOverlay.Inner_TaskQueue.ACTION,
               bounds,
               action.load == null ? null : this.init_Setting_Module_Str(module, action.load),
               action.read
            )
         );
      }
   }

   private void init_V_SyncManager(
      autismclient.gui.f.SyncManager context, Module module, int x, int y, int w, String label, String optionId, int mouseX, int mouseY
   ) {
      Setting<?, ?> option = this.init_Setting_Module_Str(module, optionId);
      int clearW = autismclient.gui.ui.UiDensityMetrics.load(context.init_PoolEntry().init("Clear"));
      autismclient.gui.f.NetHelper clear = autismclient.gui.f.NetHelper.init(x + w - clearW, y, clearW, push_I_4());
      autismclient.gui.f.NetHelper bind = autismclient.gui.f.NetHelper.init(clear.read() - 2 - 78, y, 78, push_I_4());
      this.load_V_SyncManager_2(
         context, label, x + 2, context.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, y, w, push_I_4())), Math.max(1, bind.read() - x - 6)
      );
      boolean focused = this.load_Setting == option;
      autismclient.gui.f.b.CachePool.init(
         context,
         bind,
         focused ? "Press key" : ValueSink.init_Str(init_I_Str_I_2(module.value(option.id()), -1)),
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         bind.init_Z(mouseX, mouseY),
         focused
      );
      autismclient.gui.f.b.CachePool.init(context, clear, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, clear.init_Z(mouseX, mouseY), false);
      this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.KEYBIND, bind, option, ""));
      this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.CLEAR_KEYBIND, clear, option, ""));
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, Module module, int x, int y, int w, int mouseX, int mouseY, String... ids) {
      this.init_V_SyncManager_8(context, module, autismclient.gui.f.NetHelper.init(x, y, w, run_I_2()), mouseX, mouseY, ids);
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
            this.init_Setting_Module_Str(module, id),
            field.read(),
            field.write(),
            field.update(),
            this.init_Setting_Module_Str(module, id).label(),
            mouseX,
            mouseY
         );
      }
   }

   private void init_V_SyncManager_2(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, int x, int y, int w, String label, int mouseX, int mouseY
   ) {
      this.init_V_SyncManager_3(context, module, option, autismclient.gui.f.NetHelper.init(x, y, w, run_I_2()), label, mouseX, mouseY);
   }

   private void init_V_SyncManager_3(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label, int mouseX, int mouseY
   ) {
      if (option != null) {
         this.load_V_SyncManager_2(context, label, row.read() + 2, row.write(), row.update() - 4);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(row.read(), row.write() + tick_I_2() + 1, row.update(), push_I_4());
         boolean focused = this.init_AutismAdminToolsOv != null && this.init_AutismAdminToolsOv.init_Setting == option;
         TextFieldRenderer.init(
            context,
            bounds,
            focused ? this.init_AutismAdminToolsOv.init_Str : module.value(option.id()),
            "",
            focused,
            focused ? this.init_AutismAdminToolsOv.init_I : module.value(option.id()).length(),
            focused ? this.init_AutismAdminToolsOv.init_I() : -1,
            focused ? this.init_AutismAdminToolsOv.load_I() : -1,
            focused ? this.init_I_SyncManager_NetHelper(context, bounds) : 0
         );
         this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TEXT, bounds, option, ""));
      }
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, int x, int y, int w, String label) {
      this.init_V_SyncManager_5(context, module, option, autismclient.gui.f.NetHelper.init(x, y, w, run_I_2()), label);
   }

   private void init_V_SyncManager_5(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label
   ) {
      this.load_V_SyncManager_2(context, label, row.read() + 2, row.write(), row.update() - 4);
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(row.read(), row.write() + tick_I_2() + 1, row.update(), push_I_4());
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(context, bounds, module.displayValue(option), "", false, 0);
   }

   private void load_V_SyncManager(
      autismclient.gui.f.SyncManager context, Module module, Setting<?, ?> option, autismclient.gui.f.NetHelper row, String label, int mouseX, int mouseY
   ) {
      if (option != null) {
         this.load_V_SyncManager_2(context, label, row.read() + 2, row.write(), row.update() - 4);
         this.init_V_SyncManager(
            context,
            option,
            autismclient.gui.f.NetHelper.init(row.read(), row.write() + tick_I_2() + 1, row.update(), push_I_4()),
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
         autismclient.gui.f.NetHelper toggle = autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), set_I_2(), push_I_4());
         autismclient.gui.ui.widgets.WidgetRenderFacade.init(
            context, toggle, Boolean.parseBoolean(module.value(option.id())), bounds.init_Z(mouseX, mouseY), "admintools:" + module.id() + "/" + option.id()
         );
         this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.BOOLEAN, bounds, option, ""));
         this.load_V_SyncManager_2(
            context,
            label,
            toggle.init() + 5,
            context.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), push_I_4())),
            Math.max(1, bounds.init() - toggle.init() - 7)
         );
      }
   }

   private int init_I_SyncManager_Setting(autismclient.gui.f.SyncManager context, Setting<?, ?> option) {
      return Math.min(96, context.init_PoolEntry().init(option == null ? "" : option.label()) + 2);
   }

   private void init_V_SyncManager_3(
      autismclient.gui.f.SyncManager context, Module module, String optionId, int x, int y, int w, int labelW, int mouseX, int mouseY
   ) {
      Setting<?, ?> option = this.init_Setting_Module_Str(module, optionId);
      int controlX = x + labelW + 4;
      this.load_V_SyncManager_2(context, option.label(), x + 2, context.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, y, w, push_I_4())), labelW);
      autismclient.gui.f.NetHelper control = autismclient.gui.f.NetHelper.init(controlX, y, Math.max(60, x + w - controlX), push_I_4());
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
         ? init_I_Str_I_2(module.value(option.id()), init_I_Str_I_2(option.defaultValue(), 0))
         : init_D_Str_D(module.value(option.id()), init_D_Str_D(option.defaultValue(), 0.0));
      SliderRenderer.init(context, slider, SliderRenderer.init_D_D(current, option.sliderMin(), option.sliderMax()), slider.init_Z(mouseX, mouseY));
      boolean focused = this.init_AutismAdminToolsOv != null && this.init_AutismAdminToolsOv.init_Setting == option;
      TextFieldRenderer.init(
         context,
         value,
         focused ? this.init_AutismAdminToolsOv.init_Str : module.value(option.id()),
         "",
         focused,
         focused ? this.init_AutismAdminToolsOv.init_I : module.value(option.id()).length(),
         focused ? this.init_AutismAdminToolsOv.init_I() : -1,
         focused ? this.init_AutismAdminToolsOv.load_I() : -1,
         focused ? this.init_I_SyncManager_NetHelper(context, value) : 0
      );
      this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.SLIDER, slider, option, ""));
      this.init_V_AutismAdminToolsOv(new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.TEXT, value, option, ""));
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
      boolean open = this.init_RefQueue != null && this.init_Setting == option && this.init_RefQueue.init_NetHelper().equals(bounds);
      autismclient.gui.ui.widgets.WidgetRenderFacade.init(context, bounds, label, bounds.init_Z(mouseX, mouseY), open);
      this.init_V_AutismAdminToolsOv(
         new AutismAdminToolsOverlay.Inner_ConfigStore(AutismAdminToolsOverlay.Inner_TaskQueue.DROPDOWN, bounds, option, String.join("\u0000", choices))
      );
      if (!open && this.init_RefQueue != null && this.init_RefQueue.init_NetHelper().equals(bounds)) {
         this.init_Setting = option;
      }
   }

   private int init_I_SyncManager_NetHelper(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds) {
      this.read_NetHelper = bounds;
      this.init_AutismAdminToolsOv.load = TextFieldRenderer.init(
         this.init_AutismAdminToolsOv.init_Str,
         this.init_AutismAdminToolsOv.init_I,
         bounds.update() - 8,
         fragment -> context.init_PoolEntry().init(this.init_AutismAdminToolsOv.init_Str, fragment),
         this.init_AutismAdminToolsOv.load
      );
      return this.init_AutismAdminToolsOv.load;
   }

   private void load_V_SyncManager_2(autismclient.gui.f.SyncManager context, String text, int x, int y, int width) {
      this.init_V_SyncManager_10(context, text, x, y, width, context.init_TableIndex().init_JsonUtil().valid);
   }

   private void read(autismclient.gui.f.SyncManager context, String text, int x, int y, int width) {
      this.init_V_SyncManager_10(context, text, x, y, width, context.init_TableIndex().init_JsonUtil().valid);
   }

   private void init_V_SyncManager_10(autismclient.gui.f.SyncManager context, String text, int x, int y, int width, int color) {
      context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), text == null ? "" : text, x, y, Math.max(1, width), color);
   }

   private void init_V_AutismAdminToolsOv(AutismAdminToolsOverlay.Inner_ConfigStore hit) {
      if (this.load_NetHelper == null || this.init_Z_NetHelper_NetHelper(this.load_NetHelper, hit.init_NetHelper)) {
         this.init_List.add(hit);
      }
   }

   private boolean init_Z_NetHelper_NetHelper(autismclient.gui.f.NetHelper a, autismclient.gui.f.NetHelper b) {
      return b.init() > a.read() && b.read() < a.init() && b.load() > a.write() && b.write() < a.load();
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      if (button == 0) {
         OverlayBounds bounds = this.init_cH();
         if (this.write(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            return true;
         }

         if (this.load_Z_D_D(mouseX, mouseY)) {
            this.sync_Z = true;
            return true;
         }
      }

      if (this.load_Z) {
         return true;
      }

      f.Inner_DataUtil module = this.init_f_DataUtil();
      if (module == null) {
         return true;
      }

      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_EventBus_I_2((int)mouseX, (int)mouseY, button);
         if (!this.init_RefQueue.init_Z_2()) {
            this.open_V_3();
         }

         return true;
      } else if (this.init_AutismAdminToolsOv == null
         || this.read_NetHelper == null
         || !this.read_NetHelper.init_Z((int)mouseX, (int)mouseY)
         || this.load_NetHelper != null && !this.load_NetHelper.init_Z((int)mouseX, (int)mouseY)) {
         this.load_V_Z_2(true);

         for (int i = this.init_List.size() - 1; i >= 0; i--) {
            AutismAdminToolsOverlay.Inner_ConfigStore hit = this.init_List.get(i);
            if (hit.init_NetHelper.init_Z((int)mouseX, (int)mouseY)) {
               switch (hit.init_AutismAdminToolsOv) {
                  case TAB:
                     this.put = load_I_I(init_I_Str_I_2(hit.init_Str, 0), 0, init_StrArr.length - 1);
                     this.reset = 0;
                     return true;
                  case ITEM_TAB:
                     this.add = load_I_I(init_I_Str_I_2(hit.init_Str, 0), 0, load_StrArr.length - 1);
                     if (this.add == 3) {
                        module.sync_V_9();
                     }

                     this.reset = 0;
                     return true;
                  case BOOLEAN:
                     module.setValue(hit.init_Setting.id(), Boolean.toString(!Boolean.parseBoolean(module.value(hit.init_Setting.id()))));
                     if (hit.init_Setting.id().startsWith("nbt-")) {
                        module.sync_V_9();
                     }

                     return true;
                  case TEXT:
                     if (button == 0 && (this.load_NetHelper == null || this.load_NetHelper.init_Z((int)mouseX, (int)mouseY))) {
                        this.init_V_Module(module, hit.init_Setting, (int)mouseX, hit.init_NetHelper);
                     }

                     return true;
                  case SLIDER:
                     this.read_Setting = hit.init_Setting;
                     this.load_V_Module_I(module, (int)mouseX);
                     return true;
                  case DROPDOWN:
                     List<String> values = hit.init_Str.isEmpty() ? List.of() : List.of(hit.init_Str.split("\u0000", -1));
                     String selected = hit.init_Setting == null ? read_StrArr[this.clear] : module.value(hit.init_Setting.id());
                     this.init_Setting = hit.init_Setting;
                     this.init_RefQueue = new autismclient.gui.ui.widgets.DropdownMenu(hit.init_NetHelper, values, selected, value -> {
                        if (hit.init_Setting == null) {
                           this.clear = Math.max(0, values.indexOf(value));
                           this.init_V_Module_I(module, this.clear);
                        } else {
                           module.setValue(hit.init_Setting.id(), value);
                           if (hit.init_Setting.id().startsWith("nbt-")) {
                              module.sync_V_9();
                           }
                        }
                     });
                     this.init_RefQueue.init_V_2();
                     return true;
                  case KEYBIND:
                     this.load_Setting = hit.init_Setting;
                     return true;
                  case CLEAR_KEYBIND:
                     module.setValue(hit.init_Setting.id(), "-1");
                     return true;
                  case ACTION:
                     this.init_V_f_DataUtil_AutismAdminToolsOv(module, hit);
                     return true;
                  case SCROLLBAR:
                     autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = this.init_UtilCore_DataUtil_2();
                     if (metrics != null && metrics.init_I() > 0) {
                        this.stop = metrics.load().init_Z((int)mouseX, (int)mouseY) ? (int)mouseY - metrics.load().write() : metrics.load().sync() / 2;
                        this.update_Z = true;
                        this.reset = autismclient.gui.ui.widgets.ScrollbarRenderer.init_I_UtilCore_DataUtil(metrics, (int)mouseY, this.stop);
                     }

                     return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      } else {
         if (button == 0) {
            long handle = Minecraft.getInstance().getWindow().handle();
            boolean shift = CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
            this.init_V_I_Z(this.init_I_Str(this.init_AutismAdminToolsOv.init_Str, (int)mouseX, this.read_NetHelper, this.init_AutismAdminToolsOv.load), shift);
            this.write_Z = true;
         }

         return true;
      }
   }

   private void init_V_f_DataUtil_AutismAdminToolsOv(f.Inner_DataUtil module, AutismAdminToolsOverlay.Inner_ConfigStore hit) {
      if (hit.init_Setting != null && hit.init_Setting.action() != null) {
         hit.init_Setting.action().run();
      } else {
         switch (hit.init_Str) {
            case "give-item":
               module.flush();
               break;
            case "item-clear":
               this.init_V_Module(module);
               break;
            case "pick-item":
               this.init_V_Module_NetHelper(module, hit.init_NetHelper);
               break;
            case "pick-inventory-item":
               this.init_V_f_DataUtil(module);
               break;
            case "edit-lore":
               this.init_V_Module(module, "nbt-item-lore", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.LORE);
               break;
            case "edit-enchants":
               this.init_V_Module_2(module, "nbt-enchants", autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil.ENCHANTMENTS);
               break;
            case "edit-attributes":
               this.init_V_Module_2(module, "nbt-attributes", autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil.ATTRIBUTES);
               break;
            case "edit-custom-data":
               this.init_V_Module(module, "nbt-custom-data", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.RAW);
               break;
            case "edit-item-stack":
               module.sync_V_9();
               this.init_V_Module(module, "nbt-item-stack", autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil.FULL_NBT);
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

   private void init_V_Module(Module module, String optionId, autismclient.gui.screens.NbtTextEditorScreen.Inner_DataUtil mode) {
      Minecraft mc = Minecraft.getInstance();
      Setting<?, ?> option = this.init_Setting_Module_Str(module, optionId);
      if (mc != null && option != null) {
         mc.gui.setScreen(new autismclient.gui.screens.NbtTextEditorScreen(mc.gui.screen(), module, option, mode));
      }
   }

   private void init_V_Module_2(Module module, String optionId, autismclient.gui.screens.EnchantmentAttributeEditorScreen.Inner_DataUtil mode) {
      Minecraft mc = Minecraft.getInstance();
      Setting<?, ?> option = this.init_Setting_Module_Str(module, optionId);
      if (mc != null && option != null) {
         mc.gui.setScreen(new autismclient.gui.screens.EnchantmentAttributeEditorScreen(mc.gui.screen(), module, option, mode));
      }
   }

   private void init_V_Module_NetHelper(Module module, autismclient.gui.f.NetHelper anchor) {
      this.open_V_3();
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

   private void init_V_f_DataUtil(f.Inner_DataUtil module) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null) {
         this.load_V_Z_2(true);
         this.open_V_3();
         this.flush_Z = true;
         this.close_Z = this.init_Z;
         this.init_Z = false;
         this.open_Z = !(mc.gui.screen() instanceof AbstractContainerScreen);
         this.init_Screen = this.open_Z ? mc.gui.screen() : null;
         AutismSessionState.init_ck().cache(true);
         AutismSessionState.init_ck().read_V_Runnable(this::write_V_5);
         bi.init("Right-click an inventory item to pick it.", -13248397);
         if (this.open_Z) {
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
      return this.flush_Z;
   }

   public boolean open_Z_2() {
      return this.flush_Z;
   }

   public String load_Str_3() {
      return "Capturing NBT item";
   }

   public String read_Str_2() {
      return "Right-click a slot to fill the NBT item editor. Esc = cancel";
   }

   public String init_Str_Slot_2(Slot slot, String itemName, String registryId) {
      if (slot == null) {
         return "";
      }

      String itemText = registryId != null && !registryId.isBlank() ? registryId : itemName;
      if (itemText == null || itemText.isBlank()) {
         itemText = "Empty slot";
      }

      int visibleSlot = InventorySlotHelper.init_I(Minecraft.getInstance(), slot);
      return "Hover: " + visibleSlot + " | " + itemText;
   }

   public boolean init_Z_Slot(Slot slot) {
      if (!this.flush_Z) {
         return false;
      } else {
         ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
         f.Inner_DataUtil module = this.init_f_DataUtil();
         if (module != null && !stack.isEmpty()) {
            module.init(stack, false);
            bi.init("NBT editor filled from " + BuiltInRegistries.ITEM.getKey(stack.getItem()) + ".", -13248397);
            this.sync_V_3();
            return true;
         } else {
            bi.read("Pick a non-empty item slot.");
            return true;
         }
      }
   }

   public void write_V_5() {
      if (this.flush_Z) {
         bi.read("Item pick cancelled.");
         this.sync_V_3();
      }
   }

   private void sync_V_3() {
      Minecraft mc = Minecraft.getInstance();
      this.flush_Z = false;
      this.init_Z = this.close_Z;
      this.close_Z = false;
      AutismSessionState.init_ck().cache(false);
      AutismSessionState.init_ck().read_V_Runnable(null);
      OverlayManager.init_bk().update_V(this);
      if (this.open_Z) {
         Screen restore = this.init_Screen;
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

      this.open_Z = false;
      this.init_Screen = null;
   }

   private void init_V_Module(Module module) {
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

   private void init_V_Module_I(Module module, int preset) {
      String delay = module.value("fireball-delay");
      if (delay == null || delay.isBlank()) {
         delay = "1";
      }

      switch (preset) {
         case 1:
            this.init_V_Module(module, "48", "10.0", "2.4", "5", delay, "2.5", "Cone", "true");
            break;
         case 2:
            this.init_V_Module(module, "128", "16.0", "3.2", "8", delay, "3.0", "Cone", "true");
            break;
         case 3:
            this.init_V_Module(module, "16", "0.0", "5.0", "6", delay, "2.5", "Look", "false");
            break;
         default:
            this.init_V_Module(module, "12", "8.0", "1.8", "3", delay, "2.0", "Cone", "true");
      }
   }

   private void init_V_Module(
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
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (this.init_RefQueue != null) {
         this.init_RefQueue.load((int)mouseX, (int)mouseY, button);
      }

      this.sync_Z = false;
      this.update_Z = false;
      this.write_Z = false;
      this.read_Setting = null;
      return this.init_Z && this.init_Z_D_D(mouseX, mouseY);
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (button == 0 && this.sync_Z) {
         OverlayBounds current = this.init_cH();
         this.init_V_cH(
            new OverlayBounds(
               current.init_I + (int)Math.round(deltaX), current.load_I + (int)Math.round(deltaY), current.read, current.write, current.init_Z, current.load_Z
            )
         );
         return true;
      }

      f.Inner_DataUtil module = this.init_f_DataUtil();
      if (module == null) {
         return false;
      }

      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_EventBus_I((int)mouseX, (int)mouseY, button, deltaX, deltaY);
         return true;
      }

      if (this.update_Z) {
         autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds metrics = this.init_UtilCore_DataUtil_2();
         if (metrics != null) {
            this.reset = autismclient.gui.ui.widgets.ScrollbarRenderer.init_I_UtilCore_DataUtil(metrics, (int)mouseY, this.stop);
         }

         return true;
      } else if (this.read_Setting != null) {
         this.load_V_Module_I(module, (int)mouseX);
         return true;
      } else if (this.write_Z && button == 0 && this.init_AutismAdminToolsOv != null && this.read_NetHelper != null) {
         this.init_V_I_Z(this.init_I_Str(this.init_AutismAdminToolsOv.init_Str, (int)mouseX, this.read_NetHelper, this.init_AutismAdminToolsOv.load), true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (!this.init_Z || this.load_Z) {
         return false;
      }

      if (this.init_RefQueue != null) {
         this.init_RefQueue.init_EventBus_I((int)mouseX, (int)mouseY, amount);
         return true;
      }

      if (!this.init_Z_D_D(mouseX, mouseY)) {
         return false;
      }

      this.reset = load_I_I(this.reset + (amount < 0.0 ? run_I_2() + 4 : -(run_I_2() + 4)), 0, this.open(this.init_NetHelper().sync()));
      return true;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      f.Inner_DataUtil module = this.init_f_DataUtil();
      if (module == null) {
         return false;
      }

      if (this.load_Setting != null) {
         module.setValue(this.load_Setting.id(), Integer.toString(keyCode != 256 && keyCode != 259 && keyCode != 261 ? keyCode : -1));
         this.load_Setting = null;
         return true;
      }

      if (this.init_AutismAdminToolsOv != null) {
         boolean ctrl = (modifiers & 10) != 0;
         boolean shift = (modifiers & 1) != 0;
         if (keyCode == 256) {
            this.load_V_Z_2(true);
            return true;
         } else if (ctrl && keyCode == 65) {
            this.init_AutismAdminToolsOv.load_V();
            return true;
         } else if (ctrl && keyCode == 67) {
            this.flush_V_3();
            return true;
         } else if (ctrl && keyCode == 88) {
            this.flush_V_3();
            this.poll_Z_3();
            return true;
         } else if (ctrl && keyCode == 86) {
            this.close_V_3();
            return true;
         } else if (keyCode == 257 || keyCode == 335) {
            this.load_V_Z_2(true);
            return true;
         } else if (keyCode == 259 && this.poll_Z_3()) {
            return true;
         } else if (keyCode == 259 && this.init_AutismAdminToolsOv.init_I > 0) {
            int previous = autismclient.gui.ui.widgets.Utf16TextCursor.read(this.init_AutismAdminToolsOv.init_Str, this.init_AutismAdminToolsOv.init_I);
            this.init_AutismAdminToolsOv.init_Str = this.init_AutismAdminToolsOv.init_Str.substring(0, previous)
               + this.init_AutismAdminToolsOv.init_Str.substring(this.init_AutismAdminToolsOv.init_I);
            this.init_AutismAdminToolsOv.init_I = previous;
            this.init_AutismAdminToolsOv.init_V();
            return true;
         } else if (keyCode == 261 && this.poll_Z_3()) {
            return true;
         } else if (keyCode == 261 && this.init_AutismAdminToolsOv.init_I < this.init_AutismAdminToolsOv.init_Str.length()) {
            this.init_AutismAdminToolsOv.init_Str = this.init_AutismAdminToolsOv.init_Str.substring(0, this.init_AutismAdminToolsOv.init_I)
               + this.init_AutismAdminToolsOv
                  .init_Str
                  .substring(autismclient.gui.ui.widgets.Utf16TextCursor.write(this.init_AutismAdminToolsOv.init_Str, this.init_AutismAdminToolsOv.init_I));
            this.init_AutismAdminToolsOv.init_V();
            return true;
         } else if (keyCode == 263) {
            this.init_V_I_Z(
               !shift && this.init_AutismAdminToolsOv.init_Z()
                  ? this.init_AutismAdminToolsOv.init_I()
                  : autismclient.gui.ui.widgets.Utf16TextCursor.read(this.init_AutismAdminToolsOv.init_Str, this.init_AutismAdminToolsOv.init_I),
               shift
            );
            return true;
         } else if (keyCode == 262) {
            this.init_V_I_Z(
               !shift && this.init_AutismAdminToolsOv.init_Z()
                  ? this.init_AutismAdminToolsOv.load_I()
                  : autismclient.gui.ui.widgets.Utf16TextCursor.write(this.init_AutismAdminToolsOv.init_Str, this.init_AutismAdminToolsOv.init_I),
               shift
            );
            return true;
         } else if (keyCode == 268) {
            this.init_V_I_Z(0, shift);
            return true;
         } else if (keyCode == 269) {
            this.init_V_I_Z(this.init_AutismAdminToolsOv.init_Str.length(), shift);
            return true;
         } else {
            return true;
         }
      } else if (keyCode == 256 && this.init_RefQueue != null) {
         this.open_V_3();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      if (this.init_AutismAdminToolsOv == null) {
         return false;
      }

      if (chr >= ' ' && chr != 127) {
         this.init_V_Str_3(Character.toString(chr));
      }

      return true;
   }

   @Override
   public boolean write_Z() {
      return this.init_AutismAdminToolsOv != null || this.load_Setting != null;
   }

   @Override
   public void push_V() {
      this.load_V_Z_2(true);
      this.load_Setting = null;
      this.open_V_3();
   }

   private void init_V_Module(Module module, Setting<?, ?> option, int mouseX, autismclient.gui.f.NetHelper bounds) {
      String value = module.value(option.id());
      this.init_AutismAdminToolsOv = new AutismAdminToolsOverlay.Inner_StreamUtil(option, value, this.init_I_Str(value, mouseX, bounds, 0));
      this.read_NetHelper = bounds;
      this.write_Z = true;
   }

   private int init_I_Str(String value, int mouseX, autismclient.gui.f.NetHelper bounds, int scrollOffset) {
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

   private void init_V_Module_Setting(Module module, Setting<?, ?> option) {
      if (module != null && option != null) {
         String value = module.value(option.id());
         this.init_AutismAdminToolsOv = new AutismAdminToolsOverlay.Inner_StreamUtil(option, value, value.length());
      }
   }

   private void init_V_I_Z(int cursor, boolean extendSelection) {
      if (this.init_AutismAdminToolsOv != null) {
         if (extendSelection && this.init_AutismAdminToolsOv.read < 0) {
            this.init_AutismAdminToolsOv.read = this.init_AutismAdminToolsOv.init_I;
         }

         this.init_AutismAdminToolsOv.init_I = autismclient.gui.ui.widgets.Utf16TextCursor.init_I(this.init_AutismAdminToolsOv.init_Str, cursor);
         if (!extendSelection) {
            this.init_AutismAdminToolsOv.init_V();
         }
      }
   }

   private boolean poll_Z_3() {
      if (this.init_AutismAdminToolsOv != null && this.init_AutismAdminToolsOv.init_Z()) {
         int start = this.init_AutismAdminToolsOv.init_I();
         int end = this.init_AutismAdminToolsOv.load_I();
         this.init_AutismAdminToolsOv.init_Str = this.init_AutismAdminToolsOv.init_Str.substring(0, start)
            + this.init_AutismAdminToolsOv.init_Str.substring(end);
         this.init_AutismAdminToolsOv.init_I = start;
         this.init_AutismAdminToolsOv.init_V();
         return true;
      } else {
         return false;
      }
   }

   private void init_V_Str_3(String replacement) {
      if (this.init_AutismAdminToolsOv != null) {
         String safe = replacement == null ? "" : replacement;
         int start = this.init_AutismAdminToolsOv.init_Z() ? this.init_AutismAdminToolsOv.init_I() : this.init_AutismAdminToolsOv.init_I;
         int end = this.init_AutismAdminToolsOv.init_Z() ? this.init_AutismAdminToolsOv.load_I() : this.init_AutismAdminToolsOv.init_I;
         this.init_AutismAdminToolsOv.init_Str = this.init_AutismAdminToolsOv.init_Str.substring(0, start)
            + safe
            + this.init_AutismAdminToolsOv.init_Str.substring(end);
         this.init_AutismAdminToolsOv.init_I = start + safe.length();
         this.init_AutismAdminToolsOv.init_V();
      }
   }

   private void flush_V_3() {
      if (this.init_AutismAdminToolsOv != null && this.init_AutismAdminToolsOv.init_Z()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.keyboardHandler != null) {
            mc.keyboardHandler
               .setClipboard(this.init_AutismAdminToolsOv.init_Str.substring(this.init_AutismAdminToolsOv.init_I(), this.init_AutismAdminToolsOv.load_I()));
         }
      }
   }

   private void close_V_3() {
      if (this.init_AutismAdminToolsOv != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.keyboardHandler != null) {
            String clipboard = mc.keyboardHandler.getClipboard();
            if (clipboard != null && !clipboard.isEmpty()) {
               this.init_V_Str_3(this.init_Str_Str_2(clipboard));
            }
         }
      }
   }

   private String init_Str_Str_2(String text) {
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

   private void load_V_Z_2(boolean save) {
      if (this.init_AutismAdminToolsOv != null) {
         f.Inner_DataUtil module = this.init_f_DataUtil();
         String optionId = this.init_AutismAdminToolsOv.init_Setting.id();
         if (save && module != null) {
            module.setValue(optionId, this.init_AutismAdminToolsOv.init_Str);
            if ("nbt-item-id".equals(optionId)) {
               module.update_V_13();
            }

            if (optionId.startsWith("nbt-")) {
               module.sync_V_9();
            }
         }

         this.init_AutismAdminToolsOv = null;
         this.read_NetHelper = null;
         this.write_Z = false;
      }
   }

   private void load_V_Module_I(Module module, int mouseX) {
      if (module != null && this.read_Setting != null) {
         for (AutismAdminToolsOverlay.Inner_ConfigStore hit : this.init_List) {
            if (hit.init_AutismAdminToolsOv == AutismAdminToolsOverlay.Inner_TaskQueue.SLIDER && hit.init_Setting == this.read_Setting) {
               double value = SliderRenderer.init(
                  mouseX,
                  hit.init_NetHelper.read(),
                  hit.init_NetHelper.update(),
                  this.read_Setting.sliderMin(),
                  this.read_Setting.sliderMax(),
                  this.read_Setting.step()
               );
               if (this.read_Setting.kind() == Kind.INTEGER) {
                  module.setValue(this.read_Setting.id(), Integer.toString((int)Math.round(value)));
               } else {
                  module.setValue(this.read_Setting.id(), String.format(Locale.ROOT, "%.2f", value));
               }

               return;
            }
         }
      }
   }

   private autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds init_UtilCore_DataUtil_2() {
      autismclient.gui.f.NetHelper body = this.init_NetHelper();
      if (this.start <= body.sync()) {
         return null;
      }

      autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(body.init() - 6, body.write(), 6, body.sync());
      return autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, this.start, body.sync(), this.reset);
   }

   private autismclient.gui.f.NetHelper init_NetHelper() {
      if (this.init_NetHelper != null) {
         return this.init_NetHelper;
      }

      int y = this.reset();
      return autismclient.gui.f.NetHelper.init(this.init_I_2 + 4, y, this.read_I - 8, Math.max(1, this.load_I + this.write - y - 4));
   }

   private int open(int viewHeight) {
      return Math.max(0, this.start - Math.max(1, viewHeight));
   }

   private void open_V_3() {
      if (this.init_RefQueue != null) {
         this.init_RefQueue.load_V_2();
      }

      this.init_RefQueue = null;
      this.init_Setting = null;
   }

   private f.Inner_DataUtil init_f_DataUtil() {
      return autismclient.modules.al.init_Module_Str("admin-tools") instanceof f.Inner_DataUtil adminTools ? adminTools : null;
   }

   private Setting<?, ?> init_Setting_Module_Str(Module module, String id) {
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

   private AutismAdminToolsOverlay.Inner_DataUtil init_AutismAdminToolsOv_Str_Str(String label, String optionId) {
      return new AutismAdminToolsOverlay.Inner_DataUtil(label, optionId, "");
   }

   private AutismAdminToolsOverlay.Inner_DataUtil load_AutismAdminToolsOv_Str_Str(String label, String command) {
      return new AutismAdminToolsOverlay.Inner_DataUtil(label, null, command);
   }

   private String init_Str_Z_2(boolean active) {
      return active ? "Active" : "Idle";
   }

   private static int init_I_Str_I_2(String text, int fallback) {
      try {
         return Integer.parseInt(text);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static double init_D_Str_D(String text, double fallback) {
      try {
         return Double.parseDouble(text);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static int load_I_I(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static final class Inner_BufferHelper {
      private int init_I;
      private boolean init_Z = true;

      private void init(int rowHeight) {
         if (rowHeight > 0) {
            if (!this.init_Z) {
               this.init_I += 2;
            }

            this.init_I += rowHeight;
            this.init_Z = false;
         }
      }

      private int init() {
         return this.init_I;
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

      private autismclient.gui.f.NetHelper init_NetHelper(int height) {
         int h = Math.max(0, height);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(this.init, this.read, this.load, h);
         this.read += h + 2;
         return bounds;
      }

      private void init_V(int gap) {
         this.read = this.read + Math.max(0, gap);
      }
   }

   private record Inner_ConfigStore(
      AutismAdminToolsOverlay.Inner_TaskQueue init_AutismAdminToolsOv, autismclient.gui.f.NetHelper init_NetHelper, Setting<?, ?> init_Setting, String init_Str
   ) {
   }

   private record Inner_DataUtil(String init, String load, String read) {
   }

   private static final class Inner_StreamUtil {
      private final Setting<?, ?> init_Setting;
      private String init_Str;
      private int init_I;
      private int load;
      private int read = -1;

      private Inner_StreamUtil(Setting<?, ?> option, String text, int cursor) {
         this.init_Setting = option;
         this.init_Str = text == null ? "" : text;
         this.init_I = autismclient.gui.ui.widgets.Utf16TextCursor.init_I(this.init_Str, cursor);
      }

      private boolean init_Z() {
         return this.read >= 0 && this.read != this.init_I;
      }

      private int init_I() {
         return this.init_Z() ? Math.min(this.read, this.init_I) : this.init_I;
      }

      private int load_I() {
         return this.init_Z() ? Math.max(this.read, this.init_I) : this.init_I;
      }

      private void init_V() {
         this.read = -1;
      }

      private void load_V() {
         this.read = 0;
         this.init_I = this.init_Str.length();
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
