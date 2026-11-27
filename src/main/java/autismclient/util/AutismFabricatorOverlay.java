package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.mixin.accessor.AbstractContainerScreenAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutismFabricatorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static AutismFabricatorOverlay instance;
   private int defaultWidth = 204;
   private int rowHeight = 12;
   private int listHeight = 72;
   private static final int poll_I = 6;
   private int push_I = 52;
   private static final int pop = 4;
   private static final int tick = 8;
   private static final int run = 4;
   private int get = 220;
   private int set = 5;
   private int put = this.defaultWidth;
   private int add = 240;
   private int clear = 100;
   private int reset = 13;
   private int start = 64;
   private int stop = -1;
   private float cachedFontScale = 0.0F / 0.0F;
   private boolean write_Z = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private boolean update_Z = false;
   private boolean visible = false;
   private AbstractContainerScreen<?> parentScreen;
   private final List<ThemedTextInputBox> textFields = new ArrayList<>();
   private ThemedTextInputBox slotField;
   private ThemedTextInputBox itemField;
   private ThemedTextInputBox timesField;
   private ThemedTextInputBox searchField;
   private int next = 0;
   private final AutismFabricatorOverlay.Inner_DataUtil[] actions = AutismFabricatorOverlay.Inner_DataUtil.values();
   private final List<String> actionLabels = Arrays.stream(this.actions).map(action -> action.init_Str).toList();
   private static final List<String> CLICK_OPTIONS = List.of("Left Click", "Right Click");
   private static final List<String> PACKET_CLICK_OPTIONS = List.of("Left Click", "Right Click", "Quick Move");
   private static final List<String> AMOUNT_MODES = List.of("Single", "Stack");
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private autismclient.gui.ui.widgets.DropdownWidget actionDropdown;
   private autismclient.gui.ui.widgets.DropdownWidget clickDropdown;
   private autismclient.gui.ui.widgets.DropdownWidget amountDropdown;
   private int prev = 0;
   private final String[] clickLabels = new String[]{"Left Click", "Right Click", "Quick Move"};
   private boolean dropWholeStack = false;
   private ContainerClickSnapshot.Inner_StreamUtil capturedClick = null;
   private Integer capturedSlotId = null;
   private String statusMessage = "";
   private ChatFormatting statusColor = ChatFormatting.GRAY;
   private long statusExpiresAt = 0L;
   private final List<AutoCraftingEngine.CraftOption> craftOptions = new ArrayList<>();
   private List<AutoCraftingEngine.CraftOption> filteredOptions = new ArrayList<>();
   private int valid = 0;
   private int size = 0;
   private final autismclient.gui.ui.widgets.ScrollOffsetController craftListScroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private AutoCraftingEngine.CraftOption selectedOption = null;
   private ItemStack selectedOutput = ItemStack.EMPTY;
   private final List<autismclient.util.macro.CraftAction.Inner_DataUtil> craftPlan = new ArrayList<>();
   private int data = -1;
   private int value = 0;
   private final autismclient.gui.ui.widgets.ScrollOffsetController planScroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private static final ExecutorService RECIPE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-craft-scan");
      t.setDaemon(true);
      return t;
   });
   private final AtomicReference<AutismFabricatorOverlay.Inner_StreamUtil> pendingRecipeResult = new AtomicReference<>();
   private final AtomicLong recipeRequestId = new AtomicLong();
   private volatile boolean close_Z;
   private int cache;
   private boolean open_Z;
   private volatile boolean crafting = false;
   private boolean useMaxAmount = false;
   private int state = 0;
   private int queue = 0;
   private static final int buffer = 0;
   private static final int entry = 1;
   private static final int node = 2;
   private String slotInput = "0";
   private String itemInput = "";
   private autismclient.util.macro.MacroItemTarget slotTarget = new autismclient.util.macro.MacroItemTarget();
   private String countInput = "1";
   private String recipeSearch = "";
   private int task = -1;
   private String selectedRecipeKey = "";
   private AutismFabricatorOverlay.Inner_DataUtil action;

   private static int fieldHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private static int fieldPitch() {
      return fieldHeight() + 2;
   }

   private int labelY(int rowY) {
      return UiLayoutMath.centerTextY(rowY, fieldHeight(), THEME.init_I_l(autismclient.gui.f.b.l.write), 0);
   }

   private int tick_I_5() {
      if (MINECRAFT.font == null) {
         return this.start;
      }

      float scale = autismclient.gui.sdf.MsdfTextRenderer.fontScale();
      if (this.stop > 0 && scale == this.cachedFontScale) {
         return this.stop;
      }

      int widest = 0;

      for (String label : List.of("Action", "Slot", "Item", "Click", "Drop Mode", "Times", "Items", "Amount")) {
         widest = Math.max(
            widest,
            autismclient.gui.f.b.k.init(
               MINECRAFT.font,
               label,
               THEME.init_Identifier_l(autismclient.gui.f.b.l.write),
               THEME.load(autismclient.gui.f.b.l.write)
            )
         );
      }

      this.stop = widest;
      this.cachedFontScale = scale;
      return widest;
   }

   private int run_I_5() {
      return this.get + 4 + this.tick_I_5() + 4;
   }

   private int get_I_5() {
      return Math.max(80, this.get + this.put - 4 - this.run_I_5());
   }

   private int set_I_5() {
      return this.set + this.add - this.reset - 4;
   }

   private int put_I_2() {
      return Math.max(this.clear_I_2(), this.add_I_5() - 11);
   }

   private int add_I_5() {
      return Math.max(this.clear_I_2() + 11, this.set_I_5() - 3 - this.init_List_4().size() * 10);
   }

   private List<String> init_List_4() {
      return autismclient.gui.f.UiHelper.init(MINECRAFT.font).wrapLines(this.action.description(this.dropWholeStack), Math.max(1, this.put - 8));
   }

   private int clear_I_2() {
      return this.isCraftMode() ? this.count() + this.listHeight + 12 : this.prev_I() + fieldHeight() + 4;
   }

   private int load_I(int floorY) {
      int relativeFloor = Math.max(0, floorY - this.set);
      return relativeFloor + 11 + this.init_List_4().size() * 10 + 3 + this.reset + 4;
   }

   private boolean isCraftMode() {
      return this.action.read();
   }

   private boolean isClickMode() {
      return !this.isCraftMode();
   }

   private boolean pop_Z_3() {
      return this.isClickMode() && !this.action.write();
   }

   private int reset_I_2() {
      return this.set + 14 + 4;
   }

   private int start_I_5() {
      return this.reset_I_2() + fieldPitch();
   }

   private int stop_I_2() {
      return this.start_I_5() + (this.pop_Z_3() ? fieldPitch() : 0);
   }

   private int next_I_2() {
      return this.stop_I_2() + (this.pop_Z_3() ? fieldPitch() : 0);
   }

   private int prev_I() {
      return !this.tick_Z_3() && !this.run_Z_2() ? this.stop_I_2() + fieldPitch() : this.next_I_2() + fieldPitch();
   }

   private int valid() {
      return this.node() + 12;
   }

   private int size() {
      return this.entry() + 12;
   }

   private int data() {
      return MINECRAFT.font == null
         ? 45
         : autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(
               MINECRAFT.font,
               "Use Max",
               THEME.init_Identifier_l(autismclient.gui.f.b.l.read),
               THEME.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private int value() {
      return this.run_I_5();
   }

   private int cache() {
      return this.value() + this.state() + 4;
   }

   private int state() {
      return Math.max(40, this.get_I_5() - this.data() - 4);
   }

   private int queue() {
      return this.start_I_5() + 1;
   }

   private int buffer() {
      return this.queue() + fieldHeight() + 1;
   }

   private int entry() {
      return this.buffer() + this.push_I + 2;
   }

   private int node() {
      return this.size() + fieldHeight() + 4;
   }

   private autismclient.gui.f.NetHelper init_NetHelper_2() {
      int listRight = this.flags() + this.level();
      int w = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(
            MINECRAFT.font,
            "Clear All",
            THEME.init_Identifier_l(autismclient.gui.f.b.l.read),
            THEME.load(autismclient.gui.f.b.l.read)
         )
      );
      int h = 13;
      int y = this.queue() + Math.max(0, (THEME.init_I_l_I(autismclient.gui.f.b.l.load, 1) - h) / 2) - 1;
      return autismclient.gui.f.NetHelper.init(listRight - w - 2, y, w, h);
   }

   private boolean tick_Z_3() {
      return this.action.init();
   }

   private int task() {
      return this.action.write() ? ContainerClickSnapshot.Inner_DataUtil.values().length : 2;
   }

   private ContainerClickSnapshot.Inner_DataUtil init_bo_DataUtil_2() {
      return ContainerClickSnapshot.Inner_DataUtil.init(this.prev);
   }

   private boolean run_Z_2() {
      return this.action.load();
   }

   private int pool() {
      if (this.action.write()) {
         return this.init_bo_DataUtil_2().init_I;
      }

      if (this.action.load()) {
         return this.dropWholeStack ? 1 : 0;
      }

      ContainerClickType packetAction = this.action.clickType(this.dropWholeStack);
      return packetAction.load() ? packetAction.defaultButton() : Math.min(this.prev, 1);
   }

   private String load_Str_5() {
      if (this.action.write()) {
         return this.init_bo_DataUtil_2().init_Str;
      } else if (this.tick_Z_3()) {
         return this.clickLabels[Math.min(this.prev, 1)];
      } else if (this.run_Z_2()) {
         return this.dropWholeStack ? "Whole Stack" : "Single Item";
      } else {
         return "Auto";
      }
   }

   private void init(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, MINECRAFT.font, x, y, w, h, label, variant, enabled, mouseX, mouseY);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean enabled, String animationKey, int mouseX, int mouseY) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, MINECRAFT.font, x, y, w, h, label, enabled, animationKey, mouseX, mouseY);
   }

   public AutismFabricatorOverlay(AbstractContainerScreen<?> parentScreen) {
      this.action = AutismFabricatorOverlay.Inner_DataUtil.CLICK;
      this.parentScreen = parentScreen;
      this.resetLayout();
      this.put = this.defaultWidth;
      this.add = this.fill();
   }

   public static synchronized AutismFabricatorOverlay getOrCreate(AbstractContainerScreen<?> parentScreen) {
      if (instance == null) {
         instance = new AutismFabricatorOverlay(parentScreen);
         instance.restoreState();
      } else {
         instance.parentScreen = parentScreen;
      }

      return instance;
   }

   @Override
   public int getMinWidth() {
      return this.defaultWidth;
   }

   @Override
   public int getMinHeight() {
      return this.drain();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.get, this.set, this.put, this.add, this.visible, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.get = clamped.x;
         this.set = clamped.y;
         this.put = Math.min(clamped.width, this.defaultWidth);
         this.add = clamped.height;
         this.visible = clamped.visible;
         this.update_Z = clamped.collapsed;
         if (this.visible) {
            this.rebuildWidgets();
         }
      }
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (visible) {
         this.rebuildWidgets();
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.clearTextFields();
      }

      this.saveState();
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.update_Z;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      int panelHeight = this.update_Z ? 14 : this.add;
      return mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + panelHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + 14 && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean hasFocusedTextInput() {
      for (ThemedTextInputBox field : this.textFields) {
         if (field.isFocused()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void clearInputFocus() {
      for (ThemedTextInputBox field : this.textFields) {
         field.setFocused(false);
      }
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setFabricatorVisible(this.visible);
      shared.setFabricatorX(this.get);
      shared.setFabricatorY(this.set);
      shared.setFabricatorSlotInput(this.slotInput);
      shared.setFabricatorItemInput(this.itemInput);
      shared.setFabricatorCountInput(this.countInput);
      shared.setFabricatorActionIndex(this.next);
      shared.setFabricatorClickModeIndex(this.prev);
      shared.setFabricatorDropWholeStack(this.dropWholeStack);
      shared.setFabricatorUseMaxAmount(this.useMaxAmount);
      shared.setFabricatorRecipeSearch(this.recipeSearch);
      shared.setFabricatorSelectedRecipeId(this.task);
      shared.setFabricatorSelectedRecipeKey(this.selectedRecipeKey);
      shared.setFabricatorRecipeScrollOffset(this.size);
      shared.setFabricatorCraftEntries(this.craftPlan);
      shared.setFabricatorSelectedCraftIndex(this.data);
      shared.setFabricatorCraftScrollOffset(this.value);
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.visible = shared.isFabricatorVisible();
      this.get = shared.getFabricatorX();
      this.set = shared.getFabricatorY();
      this.slotInput = shared.getFabricatorSlotInput();
      this.itemInput = shared.getFabricatorItemInput();
      this.countInput = shared.getFabricatorCountInput();
      this.next = shared.getFabricatorActionIndex();
      this.prev = shared.getFabricatorClickModeIndex();
      this.dropWholeStack = shared.isFabricatorDropWholeStack();
      this.useMaxAmount = shared.isFabricatorUseMaxAmount();
      this.recipeSearch = shared.getFabricatorRecipeSearch();
      this.task = shared.getFabricatorSelectedRecipeId();
      this.selectedRecipeKey = shared.getFabricatorSelectedRecipeKey();
      this.size = shared.getFabricatorRecipeScrollOffset();
      this.craftListScroll.setOffsetUnclamped(this.size * this.rowHeight);
      this.craftPlan.clear();
      this.craftPlan.addAll(shared.getFabricatorCraftEntries());
      this.data = shared.getFabricatorSelectedCraftIndex();
      this.value = shared.getFabricatorCraftScrollOffset();
      this.planScroll.setOffsetUnclamped(this.value * 13);
      this.selectedOption = null;
      this.selectedOutput = ItemStack.EMPTY;
      if (this.next < 0 || this.next >= this.actions.length) {
         this.next = 0;
      }

      this.action = this.actions[this.next];
      if (this.prev < 0 || this.prev >= this.clickLabels.length) {
         this.prev = 0;
      }

      if (this.visible) {
         this.rebuildWidgets();
      }
   }

   private void rebuildWidgets() {
      this.clearTextFields();
      if (!this.write_Z) {
         this.computeLayout();
         this.add = this.isCraftMode() ? this.fill() : this.drain();
      }

      int fieldX = this.run_I_5();
      int fieldWidth = this.get_I_5();
      this.action = this.actions[this.next];
      this.slotField = new ThemedTextInputBox(MINECRAFT, MINECRAFT.font, fieldX, this.start_I_5(), fieldWidth, fieldHeight(), false);
      this.slotField.setPlaceholder(Component.literal("Optional slot - right-click to fill"));
      this.slotField.setValue(this.slotInput);
      this.slotField.setMaxLength(5);
      this.slotField.setChangedListener(text -> {
         this.slotInput = text;
         this.clearStatus();

         try {
            int stableSlot = Integer.parseInt(text);
            int slotId = InventorySlotHelper.resolveConfiguredSlot(MINECRAFT, stableSlot);
            autismclient.util.macro.MacroItemTarget slotTarget = this.targetForSlot(slotId);
            String itemName = slotTarget.rawLabel();
            if (!itemName.isEmpty()) {
               this.slotTarget = slotTarget;
               this.itemInput = itemName;
               if (this.itemField != null) {
                  this.itemField.setValue(itemName);
               }
            }
         } catch (NumberFormatException var6x) {
         }
      });
      if (this.pop_Z_3()) {
         this.textFields.add(this.slotField);
      }

      this.itemField = new ThemedTextInputBox(MINECRAFT, MINECRAFT.font, fieldX, this.stop_I_2(), fieldWidth, fieldHeight(), false);
      this.itemField.setPlaceholder(Component.literal("Optional item match").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY)));
      this.itemField.setValue(this.itemInput);
      this.itemField.setMaxLength(50);
      this.itemField.setDisplayTextProvider(value -> {
         Component rich = this.slotTarget == null ? null : this.slotTarget.restyleText(value);
         return rich != null && value != null && value.equals(rich.getString()) ? rich.copy() : null;
      });
      this.itemField.setChangedListener(text -> {
         this.itemInput = text;
         if (this.slotTarget != null && !text.equals(this.slotTarget.rawLabel())) {
            this.slotTarget = this.slotTarget.hasTextJson() ? this.slotTarget.withManualDisplay(text) : autismclient.util.macro.MacroItemTarget.parse(text);
         }

         this.clearStatus();
      });
      if (this.pop_Z_3()) {
         this.textFields.add(this.itemField);
      }

      int timesY = this.isCraftMode() ? this.size() : this.prev_I();
      int timesX = this.isCraftMode() ? this.value() : fieldX;
      int timesWidth = this.isCraftMode() ? this.state() : fieldWidth;
      this.timesField = new ThemedTextInputBox(MINECRAFT, MINECRAFT.font, timesX, timesY, timesWidth, fieldHeight(), false);
      this.timesField.setPlaceholder(Component.literal("1"));
      this.timesField.setValue(this.countInput);
      this.timesField.setMaxLength(5);
      this.timesField.setEditable(!this.isCraftMode() || !this.useMaxAmount);
      this.timesField.setChangedListener(text -> {
         this.countInput = text;
         if (this.isCraftMode() && this.data >= 0 && this.data < this.craftPlan.size()) {
            try {
               int val = Integer.parseInt(text.replaceAll("[^0-9]", ""));
               this.craftPlan.get(this.data).amount = Math.max(1, val);
            } catch (Exception var3x) {
            }
         }

         this.clearStatus();
      });
      this.textFields.add(this.timesField);
      if (this.isCraftMode()) {
         int searchY = this.valid();
         this.searchField = new ThemedTextInputBox(MINECRAFT, MINECRAFT.font, this.get + 4, searchY, this.put - 8, fieldHeight(), false);
         this.searchField.setPlaceholder(Component.literal("Search crafting recipes..."));
         this.searchField.setValue(this.recipeSearch);
         this.searchField.setChangedListener(text -> {
            this.recipeSearch = text;
            this.clearStatus();
            this.applySearch(text, true);
            this.saveState();
         });
         this.textFields.add(this.searchField);
         this.pollRecipeResults();
      } else {
         this.searchField = null;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      if (!this.dropdowns.isEmpty()) {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, mouseX, mouseY, button)) {
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            return true;
         }
      }

      if (button == 0 && mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + 14) {
         OverlayBounds bounds = new OverlayBounds(this.get, this.set, this.put, this.update_Z ? 14 : this.add, this.visible, this.update_Z);
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         }

         this.write_Z = true;
         this.dragOffsetX = mouseX - this.get;
         this.dragOffsetY = mouseY - this.set;

         for (ThemedTextInputBox field : this.textFields) {
            field.setFocused(false);
         }

         return true;
      } else {
         if (this.update_Z) {
            return false;
         }

         MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, Compat263.newGlfwMouseButtonInfo(button, 0));
         boolean clickedField = false;

         for (ThemedTextInputBox field : this.textFields) {
            boolean wasClicked = field.mouseClicked(click, false);
            if (wasClicked) {
               field.setFocused(true);
               clickedField = true;
            } else {
               field.setFocused(false);
            }
         }

         if (clickedField) {
            return true;
         }

         if (this.isCraftMode()) {
            this.pollRecipeResults();
            int listX = this.flags();
            int listWidth = this.level();
            int planContentWidth = this.frame();
            int recipeContentWidth = this.block();
            int planListY = this.buffer();
            int listY = this.count();
            if (button == 0) {
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftPlanScrollbar = this.planScrollbar();
               if (craftPlanScrollbar.isScrollable() && craftPlanScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
                  this.state = 1;
                  this.queue = Math.max(0, (int)mouseY - craftPlanScrollbar.thumbY());
                  this.value = this.dragScrollOffset(craftPlanScrollbar, (int)mouseY, this.queue, 13, Math.max(0, this.craftPlan.size() - 4));
                  this.planScroll.jumpTo(this.value * 13, Math.max(0, this.craftPlan.size() * 13 - this.push_I));
                  this.saveState();
                  return true;
               }

               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftListScrollbar = this.craftListScrollbar();
               if (craftListScrollbar.isScrollable() && craftListScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
                  this.state = 2;
                  this.queue = Math.max(0, (int)mouseY - craftListScrollbar.thumbY());
                  this.size = this.dragScrollOffset(craftListScrollbar, (int)mouseY, this.queue, this.rowHeight, Math.max(0, this.filteredOptions.size() - 6));
                  this.craftListScroll.jumpTo(this.size * this.rowHeight, Math.max(0, this.filteredOptions.size() * this.rowHeight - this.listHeight));
                  this.saveState();
                  return true;
               }
            }

            if (!this.craftPlan.isEmpty() && this.init_NetHelper_2().init_Z((int)mouseX, (int)mouseY) && button == 0) {
               this.craftPlan.clear();
               this.data = -1;
               this.value = 0;
               this.planScroll.jumpTo(0, 0);
               this.clearStatus();
               this.saveState();
               return true;
            }

            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= planListY && mouseY <= planListY + this.push_I && button == 0) {
               int maxPlanScrollPx = Math.max(0, this.craftPlan.size() * 13 - this.push_I);
               int drawPlanScroll = this.planScroll.tickAndClamp(0.0F, maxPlanScrollPx);
               int firstIndex = drawPlanScroll / 13;
               int rowY = planListY - drawPlanScroll % 13;

               for (int index = firstIndex; index < this.craftPlan.size() && rowY < planListY + this.push_I; rowY += 13) {
                  if (!(mouseY < rowY) && !(mouseY >= rowY + 12)) {
                     if (mouseX >= listX + planContentWidth - 16 && mouseX < listX + planContentWidth - 2) {
                        this.removePlanEntry(index);
                        return true;
                     }

                     if (mouseX >= listX && mouseX < listX + planContentWidth) {
                        if (this.data == index) {
                           this.selectPlanEntry(-1);
                        } else {
                           this.selectPlanEntry(index);
                        }

                        for (ThemedTextInputBox field : this.textFields) {
                           field.setFocused(false);
                        }

                        return true;
                     }
                  }

                  index++;
               }

               if (mouseX >= listX && mouseX < listX + planContentWidth) {
                  this.selectPlanEntry(-1);

                  for (ThemedTextInputBox field : this.textFields) {
                     field.setFocused(false);
                  }

                  return true;
               }
            }

            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + this.listHeight && button == 0) {
               int maxRecipeScrollPx = Math.max(0, this.filteredOptions.size() * this.rowHeight - this.listHeight);
               int drawRecipeScroll = this.craftListScroll.tickAndClamp(0.0F, maxRecipeScrollPx);
               int firstIndex = drawRecipeScroll / this.rowHeight;
               int rowY = listY - drawRecipeScroll % this.rowHeight;

               for (int index = firstIndex; index < this.filteredOptions.size() && rowY < listY + this.listHeight; rowY += this.rowHeight) {
                  if (mouseX >= listX && mouseX < listX + recipeContentWidth && mouseY >= rowY && mouseY < rowY + this.rowHeight) {
                     this.selectOption(this.filteredOptions.get(index));
                     this.addDraftToPlan();

                     for (ThemedTextInputBox field : this.textFields) {
                        field.setFocused(false);
                     }

                     return true;
                  }

                  index++;
               }

               if (mouseX >= listX && mouseX < listX + recipeContentWidth) {
                  this.clearSelection();

                  for (ThemedTextInputBox field : this.textFields) {
                     field.setFocused(false);
                  }

                  this.saveState();
                  return true;
               }
            }
         }

         if (this.isCraftMode()) {
            int toggleX = this.cache();
            int toggleY = this.size();
            int toggleW = this.data();
            if (mouseY >= toggleY && mouseY < toggleY + fieldHeight() && mouseX >= toggleX && mouseX < toggleX + toggleW && (button == 0 || button == 1)) {
               this.useMaxAmount = !this.useMaxAmount;
               if (this.timesField != null) {
                  this.timesField.setEditable(!this.useMaxAmount);
               }

               if (this.data >= 0 && this.data < this.craftPlan.size()) {
                  this.craftPlan.get(this.data).useMaxAmount = this.useMaxAmount;
               }

               this.clearStatus();
               this.saveState();

               for (ThemedTextInputBox field : this.textFields) {
                  field.setFocused(false);
               }

               return true;
            }
         }

         int btnY = this.set_I_5();
         int btnArea = this.put - 8;
         int gap = 2;
         int bw = (btnArea - gap * 2) / 3;
         int bx = this.get + 4;
         if (mouseY >= btnY && mouseY < btnY + this.reset && button == 0) {
            if (mouseX >= bx && mouseX < bx + bw) {
               this.load(false);

               for (ThemedTextInputBox field : this.textFields) {
                  field.setFocused(false);
               }

               return true;
            }

            bx += bw + gap;
            if (mouseX >= bx && mouseX < bx + bw) {
               this.load(true);

               for (ThemedTextInputBox field : this.textFields) {
                  field.setFocused(false);
               }

               return true;
            }

            bx += bw + gap;
            if (mouseX >= bx && mouseX < bx + bw) {
               AutismMacroEditorOverlay macroEditor = null;

               for (AutismOverlay ov : OverlayManager.getInstance().getOverlays()) {
                  if (ov instanceof AutismMacroEditorOverlay) {
                     macroEditor = (AutismMacroEditorOverlay)ov;
                     break;
                  }
               }

               if (macroEditor != null && macroEditor.isVisible()) {
                  this.sendToMacroEditor(macroEditor);
               } else {
                  this.showStatus("Open a macro editor first!", ChatFormatting.RED);
               }

               for (ThemedTextInputBox field : this.textFields) {
                  field.setFocused(false);
               }

               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns);
      if (button == 0 && this.state != 0) {
         this.state = 0;
         this.saveState();
         return true;
      }

      if (button == 0) {
         if (this.write_Z) {
            this.saveState();
         }

         this.write_Z = false;
      }

      for (ThemedTextInputBox field : this.textFields) {
         if (field.mouseReleased(mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, mouseX, mouseY, button)) {
         return true;
      }

      if (this.state == 1) {
         this.value = this.dragScrollOffset(this.planScrollbar(), (int)mouseY, this.queue, 13, Math.max(0, this.craftPlan.size() - 4));
         this.planScroll.jumpTo(this.value * 13, Math.max(0, this.craftPlan.size() * 13 - this.push_I));
         return true;
      }

      if (this.state == 2) {
         this.size = this.dragScrollOffset(
            this.craftListScrollbar(), (int)mouseY, this.queue, this.rowHeight, Math.max(0, this.filteredOptions.size() - 6)
         );
         this.craftListScroll.jumpTo(this.size * this.rowHeight, Math.max(0, this.filteredOptions.size() * this.rowHeight - this.listHeight));
         return true;
      }

      if (this.write_Z) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.put, this.add, this.visible, this.update_Z)
         );
         this.get = nextBounds.x;
         this.set = nextBounds.y;
         this.rebuildWidgets();
         return true;
      }

      for (ThemedTextInputBox field : this.textFields) {
         if (field.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && !this.update_Z) {
         if (!this.dropdowns.isEmpty()
            && (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns) || autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.dropdowns, mouseX, mouseY))
            && autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, mouseX, mouseY, amount)) {
            return true;
         }

         if (this.isCraftMode()) {
            int planX = this.flags();
            int planY = this.buffer();
            int listWidth = this.level();
            if (mouseX >= planX && mouseX <= planX + listWidth && mouseY >= planY && mouseY <= planY + this.push_I) {
               int maxPlanScroll = Math.max(0, this.craftPlan.size() - 4);
               this.value = Math.max(0, Math.min(maxPlanScroll, this.value - (int)Math.signum(amount)));
               this.planScroll.setOffset(this.value * 13, Math.max(0, this.craftPlan.size() * 13 - this.push_I));
               this.saveState();
               return true;
            }

            int listX = this.flags();
            int listY = this.count();
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + this.listHeight) {
               int maxScroll = Math.max(0, this.filteredOptions.size() - 6);
               this.size = Math.max(0, Math.min(maxScroll, this.size - (int)Math.signum(amount)));
               this.craftListScroll.setOffset(this.size * this.rowHeight, Math.max(0, this.filteredOptions.size() * this.rowHeight - this.listHeight));
               this.saveState();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String slotLabel(int slotId) {
      autismclient.util.macro.MacroItemTarget target = this.targetForSlot(slotId);
      return target.rawLabel();
   }

   private autismclient.util.macro.MacroItemTarget targetForSlot(int slotId) {
      AbstractContainerMenu handler = this.openMenu();
      if (handler != null && slotId >= 0 && slotId < handler.slots.size()) {
         Slot slot = handler.slots.get(slotId);
         if (slot != null && !slot.getItem().isEmpty()) {
            int visibleSlot = InventorySlotHelper.toVisibleSlot(MINECRAFT, slot.index);
            return autismclient.util.macro.MacroItemTarget.fromItemStack(slot.getItem(), visibleSlot);
         } else {
            return new autismclient.util.macro.MacroItemTarget();
         }
      } else {
         return new autismclient.util.macro.MacroItemTarget();
      }
   }

   private void pollRecipeResults() {
      if (this.isCraftMode() && MINECRAFT.player != null && MINECRAFT.level != null) {
         AutismFabricatorOverlay.Inner_StreamUtil result = this.pendingRecipeResult.getAndSet(null);
         if (result != null && result.requestId == this.recipeRequestId.get() && result.connection == MINECRAFT.getConnection()) {
            this.applyCraftOptions(result.options);
         }

         if (!this.close_Z) {
            int fingerprint = this.index();
            if (!this.open_Z || fingerprint != this.cache) {
               this.open_Z = true;
               this.cache = fingerprint;
               this.startRecipeScan();
            }
         }
      }
   }

   private void startRecipeScan() {
      long perfStart = bK.init_J();
      AutoCraftingEngine.CraftContext snapshot = AutoCraftingEngine.captureContext(MINECRAFT);
      bK.load("craft.snapshot", perfStart, 5000000L);
      if (snapshot != null) {
         long generation = this.recipeRequestId.incrementAndGet();
         this.close_Z = true;
         RECIPE_EXECUTOR.execute(() -> {
            try {
               List<AutoCraftingEngine.CraftOption> options = AutoCraftingEngine.scanOptions(snapshot);
               this.pendingRecipeResult.set(new AutismFabricatorOverlay.Inner_StreamUtil(generation, snapshot.connection, options));
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] craft recipe scan failed", t);
            } finally {
               this.close_Z = false;
            }
         });
      }
   }

   private void applyCraftOptions(List<AutoCraftingEngine.CraftOption> options) {
      this.craftOptions.clear();
      this.craftOptions.addAll(options);
      this.valid = 0;

      for (AutoCraftingEngine.CraftOption option : this.craftOptions) {
         if (option.craftable) {
            this.valid++;
         }
      }

      this.applySearch(this.searchField != null ? this.searchField.getValue() : this.recipeSearch, false);
      this.close_V_5();
      this.open_V_5();
   }

   private int index() {
      int hash = System.identityHashCode(MINECRAFT.getConnection());
      if (MINECRAFT.player == null) {
         return hash;
      }

      AbstractContainerMenu menu = MINECRAFT.player.containerMenu;
      if (menu != null) {
         hash = hash * 31 + menu.containerId;
         hash = hash * 31 + menu.getStateId();
      }

      Inventory inventory = MINECRAFT.player.getInventory();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         hash = hash * 31 + (stack != null && !stack.isEmpty() ? System.identityHashCode(stack.getItem()) + stack.getCount() * 31 : 0);
      }

      if (menu instanceof AbstractCraftingMenu crafting) {
         hash = hash * 31 + crafting.getGridWidth();
         hash = hash * 31 + crafting.getGridHeight();

         for (Slot slot : crafting.getInputGridSlots()) {
            ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
            hash = hash * 31 + (stack.isEmpty() ? 0 : System.identityHashCode(stack.getItem()) + stack.getCount() * 31);
         }
      }

      return hash;
   }

   private void applySearch(String query, boolean resetScroll) {
      this.recipeSearch = query == null ? "" : query;
      this.filteredOptions = AutoCraftingEngine.filterOptions(this.craftOptions, query);
      int maxScroll = Math.max(0, this.filteredOptions.size() - 6);
      this.size = resetScroll ? 0 : Math.max(0, Math.min(maxScroll, this.size));
   }

   private void close_V_5() {
      if (this.selectedOption != null) {
         for (AutoCraftingEngine.CraftOption option : this.craftOptions) {
            if (!option.recipeKey.isBlank() && option.recipeKey.equalsIgnoreCase(this.selectedOption.recipeKey)
               || option.recipeKeyHash >= 0 && option.recipeKeyHash == this.selectedOption.recipeKeyHash
               || option.syncedRecipeIndex >= 0 && option.syncedRecipeIndex == this.selectedOption.syncedRecipeIndex) {
               this.selectedOption = option;
               this.selectedOutput = option.outputStack.copy();
               this.task = option.recipeKeyHash;
               this.selectedRecipeKey = option.recipeKey;
               return;
            }
         }

         this.selectedOption = null;
         this.selectedOutput = ItemStack.EMPTY;
         this.task = -1;
         this.selectedRecipeKey = "";
      }
   }

   private void open_V_5() {
      if (this.selectedRecipeKey != null && !this.selectedRecipeKey.isBlank() || this.task >= 0) {
         if (this.selectedOption != null) {
            if (!this.selectedRecipeKey.isBlank() && this.selectedRecipeKey.equalsIgnoreCase(this.selectedOption.recipeKey)) {
               return;
            }

            if (this.task >= 0 && (this.selectedOption.recipeKeyHash == this.task || this.selectedOption.syncedRecipeIndex == this.task)) {
               return;
            }
         }

         for (AutoCraftingEngine.CraftOption option : this.craftOptions) {
            if (!this.selectedRecipeKey.isBlank() && this.selectedRecipeKey.equalsIgnoreCase(option.recipeKey)
               || this.task >= 0 && option.recipeKeyHash == this.task
               || this.task >= 0 && option.syncedRecipeIndex == this.task) {
               this.selectedOption = option;
               this.selectedOutput = option.outputStack.copy();
               this.slotTarget = autismclient.util.macro.MacroItemTarget.fromItemStack(option.outputStack, -1);
               this.itemInput = option.outputName;
               this.selectedRecipeKey = option.recipeKey;
               this.task = option.recipeKeyHash;
               if (this.itemField != null) {
                  this.itemField.setValue(this.itemInput);
               }

               return;
            }
         }
      }
   }

   private int count() {
      return this.valid() + fieldHeight() + 2;
   }

   private int flags() {
      return this.get + 4;
   }

   private int level() {
      return this.put - 8;
   }

   private int frame() {
      return Math.max(40, this.level() - (this.planScrollbar().isScrollable() ? 8 : 0));
   }

   private int block() {
      return Math.max(40, this.level() - (this.craftListScrollbar().isScrollable() ? 8 : 0));
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics planScrollbar() {
      int listX = this.flags();
      int listY = this.buffer();
      int listWidth = this.level();
      int contentHeight = this.craftPlan.size() * 13;
      int maxScroll = Math.max(0, contentHeight - this.push_I);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight, this.push_I, listX + listWidth - 5, listY, 3, this.push_I, this.planScroll.tickAndClamp(0.0F, maxScroll)
      );
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftListScrollbar() {
      int listX = this.flags();
      int listY = this.count();
      int listWidth = this.level();
      int contentHeight = this.filteredOptions.size() * this.rowHeight;
      int maxScroll = Math.max(0, contentHeight - this.listHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight, this.listHeight, listX + listWidth - 5, listY, 3, this.listHeight, this.craftListScroll.tickAndClamp(0.0F, maxScroll)
      );
   }

   private int dragScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics, int mouseY, int grabOffset, int rowHeight, int maxScrollRows) {
      int pixelScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, grabOffset);
      return Math.max(0, Math.min(maxScrollRows, Math.round((float)pixelScroll / rowHeight)));
   }

   private void selectOption(AutoCraftingEngine.CraftOption option) {
      if (option != null) {
         this.selectedOption = option;
         this.selectedOutput = option.outputStack.copy();
         this.slotTarget = autismclient.util.macro.MacroItemTarget.fromItemStack(option.outputStack, -1);
         this.itemInput = option.outputName;
         this.task = option.recipeKeyHash;
         this.selectedRecipeKey = option.recipeKey;
         if (this.itemField != null) {
            this.itemField.setValue(this.itemInput);
         }

         this.showStatus(
            "Selected craft: "
               + option.outputName
               + " | "
               + (option.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3 ? "Table" : "2x2")
               + " | "
               + AutoCraftingEngine.formatStatusLabel(option),
            option.craftable ? ChatFormatting.GREEN : ChatFormatting.RED
         );
         this.saveState();
      }
   }

   private void clearSelection() {
      this.data = -1;
      this.selectedOption = null;
      this.selectedOutput = ItemStack.EMPTY;
      this.slotTarget = new autismclient.util.macro.MacroItemTarget();
      this.task = -1;
      this.selectedRecipeKey = "";
   }

   private AutoCraftingEngine.CraftOption optionFor(autismclient.util.macro.CraftAction.Inner_DataUtil entry) {
      return entry == null ? null : AutoCraftingEngine.matchOptionInList(this.craftOptions, entry.recipeKey, entry.recipeId);
   }

   private autismclient.util.macro.CraftAction.Inner_DataUtil draftEntry() {
      if (this.selectedOption != null && !this.selectedOutput.isEmpty()) {
         int configuredAmount = this.store();
         return configuredAmount <= 0 ? null : autismclient.util.macro.CraftAction.Inner_DataUtil.init(this.selectedOption, configuredAmount, this.useMaxAmount);
      } else {
         return null;
      }
   }

   private autismclient.util.macro.CraftAction.Inner_DataUtil selectedPlanEntry() {
      return this.data >= 0 && this.data < this.craftPlan.size() ? this.craftPlan.get(this.data) : null;
   }

   private void selectPlanEntry(int index) {
      if (index >= 0 && index < this.craftPlan.size()) {
         this.data = index;
         autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.craftPlan.get(index);
         if (entry != null) {
            this.useMaxAmount = entry.useMaxAmount;
            this.countInput = String.valueOf(Math.max(1, entry.amount));
            if (this.timesField != null) {
               this.timesField.setValue(this.countInput);
               this.timesField.setEditable(!this.useMaxAmount);
            }

            AutoCraftingEngine.CraftOption option = this.optionFor(entry);
            if (option != null) {
               this.selectOption(option);
            } else {
               this.task = entry.recipeId;
               this.selectedRecipeKey = entry.recipeKey;
               this.selectedOption = null;
               this.selectedOutput = ItemStack.EMPTY;
            }
         }

         this.saveState();
      } else {
         this.data = -1;
         this.selectedOption = null;
         this.selectedOutput = ItemStack.EMPTY;
         this.task = -1;
         this.selectedRecipeKey = "";
         this.saveState();
      }
   }

   private void addDraftToPlan() {
      autismclient.util.macro.CraftAction.Inner_DataUtil draft = this.draftEntry();
      if (draft == null) {
         this.showStatus("Select a recipe and amount first", ChatFormatting.RED);
      } else {
         int existingIndex = -1;

         for (int i = 0; i < this.craftPlan.size(); i++) {
            autismclient.util.macro.CraftAction.Inner_DataUtil existing = this.craftPlan.get(i);
            if (existing != null
               && (!draft.recipeKey.isBlank() && draft.recipeKey.equalsIgnoreCase(existing.recipeKey) || draft.recipeId >= 0 && draft.recipeId == existing.recipeId)) {
               existingIndex = i;
               break;
            }
         }

         if (this.data >= 0 && this.data < this.craftPlan.size()) {
            this.craftPlan.set(this.data, draft);
         } else if (existingIndex >= 0) {
            this.craftPlan.set(existingIndex, draft);
            this.data = existingIndex;
         } else {
            this.craftPlan.add(draft);
            this.data = this.craftPlan.size() - 1;
         }

         int maxPlanScroll = Math.max(0, this.craftPlan.size() - 4);
         this.value = Math.max(0, Math.min(maxPlanScroll, this.data));
         this.saveState();
      }
   }

   private void removePlanEntry(int index) {
      if (index >= 0 && index < this.craftPlan.size()) {
         this.craftPlan.remove(index);
         if (this.data == index) {
            this.data = -1;
         } else if (this.data > index) {
            this.data--;
         }

         this.value = Math.max(0, Math.min(Math.max(0, this.craftPlan.size() - 4), this.value));
         this.saveState();
      }
   }

   private List<autismclient.util.macro.CraftAction.Inner_DataUtil> activeEntries() {
      if (!this.craftPlan.isEmpty()) {
         List<autismclient.util.macro.CraftAction.Inner_DataUtil> copies = new ArrayList<>(this.craftPlan.size());

         for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : this.craftPlan) {
            if (entry != null && entry.isValid()) {
               copies.add(entry.copy());
            }
         }

         return copies;
      } else {
         autismclient.util.macro.CraftAction.Inner_DataUtil draft = this.draftEntry();
         return draft == null ? List.of() : List.of(draft);
      }
   }

   private autismclient.util.macro.CraftAction buildCraftAction() {
      List<autismclient.util.macro.CraftAction.Inner_DataUtil> activeEntries = this.activeEntries();
      if (activeEntries.isEmpty()) {
         return null;
      }

      autismclient.util.macro.CraftAction action = new autismclient.util.macro.CraftAction();
      action.init(activeEntries);
      return action;
   }

   private Component entryLabel(autismclient.util.macro.CraftAction.Inner_DataUtil entry) {
      if (entry != null && entry.isValid()) {
         AutoCraftingEngine.CraftOption option = this.optionFor(entry);
         String amount = entry.useMaxAmount ? "Max" : "x" + Math.max(1, entry.amount);
         Component resultName = option != null ? option.outputStack.getHoverName().copy() : Component.literal(entry.resultName);
         return Component.empty()
            .append(resultName)
            .append(Component.literal(" | " + amount + (option != null && option.craftable ? " | x" + option.maxCrafts : "")));
      } else {
         return Component.literal("(empty)");
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.visible && !this.update_Z) {
         boolean anyFocused = false;

         for (ThemedTextInputBox field : this.textFields) {
            if (field.isFocused()) {
               anyFocused = true;
               break;
            }
         }

         if (anyFocused) {
            if (keyCode == 256) {
               for (ThemedTextInputBox field : this.textFields) {
                  field.setFocused(false);
               }

               return true;
            }

            KeyEvent input = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);

            for (ThemedTextInputBox field : this.textFields) {
               if (field.isFocused()) {
                  field.keyPressed(input);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      int codepoint = chr;
      if (this.visible && !this.update_Z) {
         CharacterEvent input = new CharacterEvent(codepoint);

         for (ThemedTextInputBox field : this.textFields) {
            if (field.isFocused() && field.charTyped(input)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         this.expireStatus();
         int rawMouseX = mouseX;
         int rawMouseY = mouseY;
         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            mouseX = -2147483648;
            mouseY = -2147483648;
         }

         this.dropdowns.clear();
         this.computeLayout();
         if (!this.write_Z) {
            this.add = this.isCraftMode() ? this.fill() : this.drain();
         }

         int panelHeight = this.add;
         OverlayBounds bounds = new OverlayBounds(this.get, this.set, this.put, panelHeight, this.visible, this.update_Z);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Fabricator", this.update_Z, this.write_Z);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
         } else {
            try {
               int labelX = this.get + 4;
               int fieldX = this.run_I_5();
               int fieldWidth = this.get_I_5();
               ThemedTextHelper.init(context, MINECRAFT.font, "Action", ThemedTextHelper.TextTone.MUTED, labelX, this.labelY(this.reset_I_2()), false);
               if (this.actionDropdown == null) {
                  this.actionDropdown = new autismclient.gui.ui.widgets.DropdownWidget(
                     fieldX, this.reset_I_2(), fieldWidth, fieldHeight(), this.actionLabels, this.next, idx -> {
                        if (idx >= 0 && idx < this.actions.length && idx != this.next) {
                           this.next = idx;
                           this.action = this.actions[idx];
                           this.clearStatus();
                           this.rebuildWidgets();
                           this.saveState();
                        }
                     }
                  );
               }

               this.actionDropdown
                  .setBounds(fieldX, this.reset_I_2(), fieldWidth, fieldHeight())
                  .setOptions(this.actionLabels)
                  .setSelectedIndex(this.next);
               this.dropdowns.add(this.actionDropdown);

               for (ThemedTextInputBox field : this.textFields) {
                  field.render(context, mouseX, mouseY, delta);
               }

               if (this.pop_Z_3()) {
                  ThemedTextHelper.init(context, MINECRAFT.font, "Slot", ThemedTextHelper.TextTone.MUTED, labelX, this.labelY(this.start_I_5()), false);
                  ThemedTextHelper.init(context, MINECRAFT.font, "Item", ThemedTextHelper.TextTone.MUTED, labelX, this.labelY(this.stop_I_2()), false);
               }

               if (this.isClickMode()) {
                  if (this.tick_Z_3() || this.run_Z_2()) {
                     ThemedTextHelper.init(
                        context,
                        MINECRAFT.font,
                        this.run_Z_2() ? "Drop Mode" : "Click",
                        ThemedTextHelper.TextTone.MUTED,
                        labelX,
                        this.labelY(this.next_I_2()),
                        false
                     );
                     if (this.tick_Z_3()) {
                        boolean packetMode = this.action.write();
                        List<String> options = packetMode ? PACKET_CLICK_OPTIONS : CLICK_OPTIONS;
                        int clamped = Math.min(this.prev, options.size() - 1);
                        if (this.clickDropdown == null) {
                           this.clickDropdown = new autismclient.gui.ui.widgets.DropdownWidget(
                              fieldX, this.next_I_2(), fieldWidth, fieldHeight(), options, clamped, idx -> {
                                 if (idx >= 0 && idx < 3) {
                                    this.prev = idx;
                                    this.clearStatus();
                                    this.saveState();
                                 }
                              }
                           );
                        }

                        this.clickDropdown
                           .setBounds(fieldX, this.next_I_2(), fieldWidth, fieldHeight())
                           .setOptions(options)
                           .setSelectedIndex(clamped);
                        this.dropdowns.add(this.clickDropdown);
                     } else {
                        List<String> options = AMOUNT_MODES;
                        int sel = this.dropWholeStack ? 1 : 0;
                        if (this.amountDropdown == null) {
                           this.amountDropdown = new autismclient.gui.ui.widgets.DropdownWidget(fieldX, this.next_I_2(), fieldWidth, fieldHeight(), options, sel, idx -> {
                              this.dropWholeStack = idx == 1;
                              this.clearStatus();
                              this.saveState();
                           });
                        }

                        this.amountDropdown
                           .setBounds(fieldX, this.next_I_2(), fieldWidth, fieldHeight())
                           .setOptions(options)
                           .setSelectedIndex(sel);
                        this.dropdowns.add(this.amountDropdown);
                     }
                  }

                  ThemedTextHelper.init(context, MINECRAFT.font, this.amountLabel(), ThemedTextHelper.TextTone.MUTED, labelX, this.labelY(this.prev_I()), false);
               } else {
                  this.pollRecipeResults();
                  int listX = this.flags();
                  int listWidth = this.level();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, MINECRAFT.font, "Craft Plan", labelX, this.queue());
                  if (!this.craftPlan.isEmpty()) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderActionButton(context, MINECRAFT.font, "Clear All", true, listX, listWidth, this.queue(), mouseX, mouseY);
                  }

                  int planListY = this.buffer();
                  int planContentWidth = this.frame();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, planListY, listWidth, this.push_I, this.data >= 0);
                  if (this.craftPlan.isEmpty()) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(
                        context, MINECRAFT.font, "(no plan - pick recipe and Add)", listX, planListY + 2, planContentWidth
                     );
                  } else {
                     int maxPlanScrollPx = Math.max(0, this.craftPlan.size() * 13 - this.push_I);
                     this.value = Math.max(0, Math.min(this.value, Math.max(0, this.craftPlan.size() - 4)));
                     this.planScroll.setOffset(this.value * 13, maxPlanScrollPx);
                     int drawPlanScroll = this.planScroll.tickAndClamp(delta, maxPlanScrollPx);
                     autismclient.gui.ui.UiClipStack.getInstance()
                        .push(context, autismclient.gui.f.NetHelper.init(listX + 1, planListY + 1, Math.max(0, listWidth - 2), Math.max(0, this.push_I - 2)));
                     int startIndex = drawPlanScroll / 13;
                     int rowY = planListY - drawPlanScroll % 13;

                     for (int index = startIndex; index < this.craftPlan.size() && rowY < planListY + this.push_I; rowY += 13) {
                        boolean hovered = mouseX >= listX && mouseX < listX + planContentWidth && mouseY >= rowY && mouseY < rowY + 12;
                        boolean selected = index == this.data;
                        autismclient.gui.ui.widgets.ListRowRenderer.drawComponentRow(
                           context,
                           MINECRAFT.font,
                           this.entryLabel(this.craftPlan.get(index)),
                           listX,
                           rowY,
                           planContentWidth,
                           12,
                           hovered,
                           selected,
                           autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL,
                           true
                        );
                        boolean removeHovered = mouseX >= listX + planContentWidth - 16
                           && mouseX < listX + planContentWidth - 2
                           && mouseY >= rowY
                           && mouseY < rowY + 12;
                        autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButtonSquare(context, listX + planContentWidth - 14, rowY, 12, removeHovered);
                        index++;
                     }

                     autismclient.gui.ui.UiClipStack.getInstance().pop(context);
                  }

                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftPlanScrollbar = this.planScrollbar();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, craftPlanScrollbar, craftPlanScrollbar.containsTrack(mouseX, mouseY), this.state == 1);
                  if (this.selectedOutput.isEmpty()) {
                     ThemedTextHelper.init(context, MINECRAFT.font, "Recipe: none selected", ThemedTextHelper.TextTone.MUTED, listX, this.entry(), false);
                  } else {
                     Component summaryText = Component.empty()
                        .append(this.selectedOutput.getHoverName().copy())
                        .append(
                           Component.literal(
                              " x"
                                 + this.selectedOutput.getCount()
                                 + (this.selectedOption != null && this.selectedOption.craftable ? " | x" + this.selectedOption.maxCrafts : "")
                           )
                        );
                     ThemedTextHelper.init(context, MINECRAFT.font, summaryText.getString(), UiColorPalette.textMuted(), listX, this.entry(), false);
                  }

                  ThemedTextHelper.init(context, MINECRAFT.font, "Amount", ThemedTextHelper.TextTone.MUTED, labelX, this.labelY(this.size()), false);
                  this.init(context, this.cache(), this.size(), this.data(), fieldHeight(), "Use Max", this.useMaxAmount, "fabricator:use-max", mouseX, mouseY);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, MINECRAFT.font, "Recipes", listX, this.node());
                  int listY = this.count();
                  int recipeContentWidth = this.block();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, listY, listWidth, this.listHeight, this.selectedOption != null);
                  autismclient.gui.ui.UiClipStack.getInstance()
                     .push(context, autismclient.gui.f.NetHelper.init(listX + 1, listY + 1, Math.max(0, listWidth - 2), Math.max(0, this.listHeight - 2)));
                  if (this.filteredOptions.isEmpty()) {
                     String emptyText;
                     if (MINECRAFT.player == null || MINECRAFT.level == null) {
                        emptyText = "Join a world to load crafting recipes.";
                     } else if (this.craftOptions.isEmpty() && this.close_Z) {
                        emptyText = "Scanning crafting recipes...";
                     } else {
                        emptyText = "No crafting recipes match this search.";
                     }

                     autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, MINECRAFT.font, emptyText, listX, listY + 4, recipeContentWidth);
                  } else {
                     int maxRecipeScrollPx = Math.max(0, this.filteredOptions.size() * this.rowHeight - this.listHeight);
                     this.size = Math.max(0, Math.min(this.size, Math.max(0, this.filteredOptions.size() - 6)));
                     this.craftListScroll.setOffset(this.size * this.rowHeight, maxRecipeScrollPx);
                     int drawRecipeScroll = this.craftListScroll.tickAndClamp(delta, maxRecipeScrollPx);
                     int startIndex = drawRecipeScroll / this.rowHeight;
                     int rowY = listY - drawRecipeScroll % this.rowHeight;

                     for (int index = startIndex; index < this.filteredOptions.size() && rowY < listY + this.listHeight; rowY += this.rowHeight) {
                        AutoCraftingEngine.CraftOption option = this.filteredOptions.get(index);
                        boolean hovered = mouseX >= listX && mouseX <= listX + recipeContentWidth && mouseY >= rowY && mouseY < rowY + this.rowHeight;
                        boolean selected = this.selectedOption != null && option.recipeKey.equalsIgnoreCase(this.selectedOption.recipeKey);
                        Component richLabel = Component.empty()
                           .append(option.outputStack.getHoverName().copy())
                           .append(Component.literal(" x" + option.outputStack.getCount() + " | " + AutoCraftingEngine.formatStatusLabel(option)));
                        autismclient.gui.ui.widgets.ListRowRenderer.drawComponentRow(
                           context,
                           MINECRAFT.font,
                           richLabel,
                           listX + 2,
                           rowY,
                           recipeContentWidth - 2,
                           this.rowHeight,
                           hovered,
                           selected,
                           option.craftable ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.READY : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.MISSING,
                           true
                        );
                        autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, listX + 2, rowY + this.rowHeight - 1, recipeContentWidth - 2);
                        index++;
                     }
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(context);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftListScrollbar = this.craftListScrollbar();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, craftListScrollbar, craftListScrollbar.containsTrack(mouseX, mouseY), this.state == 2);
                  String recipeCountLine = ThemedTextHelper.init(
                     MINECRAFT.font, this.valid + " ready / " + this.craftOptions.size() + " total", listWidth, ThemedTextHelper.TextTone.MUTED
                  );
                  ThemedTextHelper.init(context, MINECRAFT.font, recipeCountLine, ThemedTextHelper.TextTone.MUTED, listX, listY + this.listHeight + 2, false);
               }

               int btnY = this.set_I_5();
               int btnArea = this.put - 8;
               int gap = 2;
               int bw = (btnArea - gap * 2) / 3;
               int bx = this.get + 4;
               String sendLabel = this.isCraftMode() ? (this.crafting ? "Crafting..." : "Craft") : "Send";
               this.init(context, bx, btnY, bw, this.reset, sendLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               bx += bw + gap;
               this.init(context, bx, btnY, bw, this.reset, "Queue", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               bx += bw + gap;
               this.init(context, bx, btnY, bw, this.reset, "Macro", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               int infoWidth = this.put - 8;
               int statusY = this.put_I_2();
               if (statusY + 9 < btnY) {
                  if (!this.statusMessage.isEmpty()) {
                     int color = colorOf(this.statusColor);
                     autismclient.gui.f.b.k.init(
                        context,
                        MINECRAFT.font,
                        this.statusMessage,
                        THEME.init_Identifier_l(autismclient.gui.f.b.l.read),
                        color,
                        this.get + 4,
                        statusY,
                        infoWidth,
                        true
                     );
                  } else {
                     autismclient.gui.f.b.k.init(
                        context,
                        MINECRAFT.font,
                        this.write_Str_2(),
                        THEME.init_Identifier_l(autismclient.gui.f.b.l.write),
                        THEME.load(autismclient.gui.f.b.l.write),
                        this.get + 4,
                        statusY,
                        infoWidth,
                        false
                     );
                  }
               }

               List<String> actionLines = this.init_List_4();

               for (int i = 0; i < actionLines.size(); i++) {
                  int lineY = this.add_I_5() + i * 10;
                  if (lineY + 9 >= btnY) {
                     break;
                  }

                  ThemedTextHelper.init(context, MINECRAFT.font, actionLines.get(i), ThemedTextHelper.TextTone.MUTED, this.get + 4, lineY, false);
               }

               if (!this.dropdowns.isEmpty()) {
                  autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(context, MINECRAFT.font, this.dropdowns, rawMouseX, rawMouseY);
               }
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
            }

            if (!this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
               context.nextStratum();
               autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(context, MINECRAFT.font, this.dropdowns, rawMouseX, rawMouseY);
            }
         }
      }
   }

   private static int colorOf(ChatFormatting formatting) {
      if (formatting == null) {
         return -1;
      }

      int var10000;
      switch (formatting) {
         case BLACK:
            var10000 = -16777216;
            break;
         case DARK_BLUE:
            var10000 = -16777046;
            break;
         case DARK_GREEN:
            var10000 = -16733696;
            break;
         case DARK_AQUA:
            var10000 = -16733526;
            break;
         case DARK_RED:
            var10000 = -5636096;
            break;
         case DARK_PURPLE:
            var10000 = -5635926;
            break;
         case GOLD:
            var10000 = -22016;
            break;
         case GRAY:
            var10000 = -5592406;
            break;
         case DARK_GRAY:
            var10000 = -11184811;
            break;
         case BLUE:
            var10000 = -11184641;
            break;
         case GREEN:
            var10000 = -11141291;
            break;
         case AQUA:
            var10000 = -11141121;
            break;
         case RED:
            var10000 = -43691;
            break;
         case LIGHT_PURPLE:
            var10000 = -43521;
            break;
         case YELLOW:
            var10000 = -171;
            break;
         case WHITE:
            var10000 = -1;
            break;
         default:
            var10000 = -1;
      }

      return var10000;
   }

   private void nextAction() {
      this.next = (this.next + 1) % this.actions.length;
      this.action = this.actions[this.next];
      this.clearStatus();
      this.rebuildWidgets();
      this.saveState();
   }

   private void previousAction() {
      this.next = (this.next - 1 + this.actions.length) % this.actions.length;
      this.action = this.actions[this.next];
      this.clearStatus();
      this.rebuildWidgets();
      this.saveState();
   }

   private void clearTextFields() {
      this.textFields.clear();
      this.searchField = null;
   }

   public void captureSlotClick(Slot slot, int button) {
      if (slot != null && !this.isCraftMode()) {
         int stableSlotId = InventorySlotHelper.toVisibleSlot(MINECRAFT, slot.index);
         autismclient.util.macro.MacroItemTarget clickedTarget = this.targetForSlot(slot.index);
         String itemName = clickedTarget.rawLabel();
         if (this.action.write()) {
            AbstractContainerMenu handler = this.openMenu();
            if (handler == null) {
               this.showStatus("No container is open to capture", ChatFormatting.RED);
            } else {
               String screenTitle = this.parentScreen != null ? this.parentScreen.getTitle().getString() : "";
               String menuClass = handler.getClass().getName();
               this.capturedClick = new ContainerClickSnapshot.Inner_StreamUtil(
                  handler.containerId,
                  handler.getStateId(),
                  slot.index,
                  stableSlotId,
                  screenTitle,
                  menuClass,
                  itemName == null ? "" : itemName,
                  this.init_bo_DataUtil_2(),
                  System.currentTimeMillis()
               );
               this.showStatus("Captured Packet " + this.capturedClick.init_Str(), ChatFormatting.GREEN);
               this.saveState();
            }
         } else {
            this.capturedSlotId = stableSlotId;
            this.slotInput = String.valueOf(stableSlotId);
            if (this.slotField != null) {
               this.slotField.setValue(this.slotInput);
            }

            if (!itemName.isEmpty()) {
               this.slotTarget = clickedTarget;
               this.itemInput = itemName;
               if (this.itemField != null) {
                  this.itemField.setValue(itemName);
               }

               this.showStatus("Selected slot " + stableSlotId + ": " + itemName, ChatFormatting.GREEN);
            } else {
               this.slotTarget = new autismclient.util.macro.MacroItemTarget();
               this.itemInput = "";
               if (this.itemField != null) {
                  this.itemField.setValue("");
               }

               this.showStatus("Selected slot " + stableSlotId + " (Empty)", ChatFormatting.YELLOW);
            }

            this.saveState();
         }
      }
   }

   private void sendToMacroEditor(AutismMacroEditorOverlay macroEditor) {
      try {
         if (this.isCraftMode()) {
            autismclient.util.macro.CraftAction craftAction = this.buildCraftAction();
            if (craftAction != null && craftAction.hasValidEntry()) {
               macroEditor.appendAction(craftAction);
               return;
            }

            this.showStatus("Select a recipe or add a craft entry first!", ChatFormatting.RED);
            return;
         }

         Integer enteredSlot = this.enteredSlot();
         String itemName = this.enteredItemName();
         int repeats = this.read(-1);
         if (repeats <= 0) {
            return;
         }

         if (enteredSlot == null && itemName.isEmpty() && !this.action.write()) {
            this.showStatus("Pick a slot or enter an item name first!", ChatFormatting.RED);
            return;
         }

         if (this.action.write()) {
            ContainerClickSnapshot.Inner_StreamUtil target = this.getCapturedClick();
            if (target == null) {
               this.showStatus("Right-click a GUI slot to capture a Packet target first!", ChatFormatting.RED);
               return;
            }

            autismclient.util.macro.PacketClickAction action = new autismclient.util.macro.PacketClickAction(target, repeats, false);
            macroEditor.appendAction(action);
            this.showStatus("Added Packet Click action to macro!", ChatFormatting.GREEN);
            return;
         }

         AbstractContainerMenu handler = this.openMenu();
         int resolvedSlot = enteredSlot == null ? -1 : InventorySlotHelper.resolveConfiguredSlot(MINECRAFT, enteredSlot);
         if (enteredSlot != null && !this.isValidSlot(handler, resolvedSlot)) {
            this.showStatus("Slot " + enteredSlot + " is not available in this screen!", ChatFormatting.RED);
            return;
         }

         if (this.action == AutismFabricatorOverlay.Inner_DataUtil.DROP) {
            autismclient.util.macro.DropAction dropAction = new autismclient.util.macro.DropAction();
            dropAction.useHandlerSlots = true;
            dropAction.mode = this.dropWholeStack ? autismclient.util.macro.DropAction.Inner_DataUtil.ALL : autismclient.util.macro.DropAction.Inner_DataUtil.TIMES;
            dropAction.dropCount = this.dropWholeStack ? 0 : repeats;
            String entry = enteredSlot != null ? "#" + enteredSlot + (!itemName.isEmpty() ? "|" + itemName : "") : itemName;
            if (!entry.isBlank()) {
               autismclient.util.macro.MacroItemTarget target = this.buildItemTarget(enteredSlot, itemName);
               entry = target.toSpecString();
               dropAction.itemTargets.add(target);
               dropAction.itemNames.add(entry);
               dropAction.itemCounts.add(this.dropWholeStack ? 0 : repeats);
            }

            macroEditor.appendAction(dropAction);
            this.showStatus("Added Drop action to macro!", ChatFormatting.GREEN);
            return;
         }

         autismclient.util.macro.ItemAction itemAction = new autismclient.util.macro.ItemAction();
         itemAction.actionIndex = this.action.clickType(this.dropWholeStack).ordinal();
         itemAction.button = this.pool();
         itemAction.times = repeats;
         if (enteredSlot != null) {
            itemAction.useSlot = true;
            itemAction.targetSlot = enteredSlot;
         }

         if (!itemName.isEmpty()) {
            autismclient.util.macro.MacroItemTarget target = this.buildItemTarget(enteredSlot, itemName);
            itemAction.itemTargets.add(target);
            itemAction.itemNames.add(target.toSpecString());
            itemAction.itemTimes.add(repeats);
            itemAction.itemActionIdx.add(itemAction.actionIndex);
            itemAction.itemButtons.add(itemAction.button);
         }

         macroEditor.appendAction(itemAction);
         this.showStatus("Added " + this.action.init_Str + " action to macro!", ChatFormatting.GREEN);
      } catch (NumberFormatException e) {
         this.showStatus("Invalid value", ChatFormatting.RED);
      }
   }

   private autismclient.util.macro.MacroItemTarget buildItemTarget(Integer enteredSlot, String itemName) {
      String safeName = itemName == null ? "" : itemName.trim();
      autismclient.util.macro.MacroItemTarget target = this.slotTarget != null && safeName.equals(this.slotTarget.rawLabel())
         ? this.slotTarget.copy()
         : autismclient.util.macro.MacroItemTarget.parse(safeName);
      if (enteredSlot != null) {
         target.slot = enteredSlot;
      }

      return target;
   }

   private ContainerClickSnapshot.Inner_StreamUtil getCapturedClick() {
      return this.capturedClick == null ? null : this.capturedClick.withMode(this.init_bo_DataUtil_2());
   }

   private Packet<?> capturedPacket() {
      ContainerClickSnapshot.Inner_StreamUtil target = this.getCapturedClick();
      return target == null ? null : target.toPacket();
   }

   private Packet<?> buildPacket(int slot) {
      if (this.action.write()) {
         return this.capturedPacket();
      }

      if (MINECRAFT.player == null) {
         return null;
      }

      AbstractContainerMenu handler = MINECRAFT.player.containerMenu;
      if (handler == null) {
         return null;
      }

      ContainerInput actionType = this.action.clickType(this.dropWholeStack).toContainerInput();
      return new ServerboundContainerClickPacket(
         handler.containerId, handler.getStateId(), (short)slot, (byte)this.pool(), actionType, new Int2ObjectArrayMap<>(), HashedStack.EMPTY
      );
   }

   private void load(boolean queue) {
      if (this.isCraftMode()) {
         this.flush(queue);
      } else if (this.action.write()) {
         this.read(queue);
      } else {
         try {
            int slot = this.chunk();
            if (slot < 0) {
               return;
            }

            int repeats = this.read(slot);
            if (repeats <= 0) {
               return;
            }

            ClientPacketListener handler = MINECRAFT.getConnection();
            if (handler == null) {
               return;
            }

            if (this.action == AutismFabricatorOverlay.Inner_DataUtil.DROP && !queue) {
               int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);
               int sent = InventoryDropHelper.init(MINECRAFT, slot, this.dropWholeStack ? 0 : repeats);
               if (sent <= 0) {
                  this.showStatus("Nothing to drop", ChatFormatting.YELLOW);
               } else if (queuedCount > 0) {
                  this.showStatus("Sent " + queuedCount + " queued + drop action", ChatFormatting.GREEN);
               } else {
                  this.showStatus(this.dropWholeStack ? "Dropped stack" : "Dropped " + repeats + " item(s)", ChatFormatting.GREEN);
               }

               return;
            }

            if (!queue) {
               AutismSessionState.getInstance().flushDelayedPackets(handler);
               ContainerInput actionType = this.action.clickType(this.dropWholeStack).toContainerInput();
               int effectiveButton = this.pool();

               for (int i = 0; i < repeats; i++) {
                  ContainerClickExecutor.init(MINECRAFT, slot, effectiveButton, actionType);
               }

               return;
            }

            this.init(slot, repeats, queue, handler);
         } catch (NumberFormatException e) {
            this.showStatus("Invalid number format", ChatFormatting.RED);
         }
      }
   }

   private void read(boolean queue) {
      try {
         ContainerClickSnapshot.Inner_StreamUtil target = this.getCapturedClick();
         if (target == null) {
            this.showStatus("Right-click a GUI slot to capture a Packet target first", ChatFormatting.RED);
            return;
         }

         int repeats = this.read(-1);
         if (repeats <= 0) {
            return;
         }

         ClientPacketListener handler = MINECRAFT.getConnection();
         if (handler == null) {
            this.showStatus("No network connection", ChatFormatting.RED);
            return;
         }

         List<Packet<?>> packets = new ArrayList<>(repeats);

         for (int i = 0; i < repeats; i++) {
            packets.add(target.toPacket());
         }

         if (queue) {
            for (Packet<?> packet : packets) {
               AutismSessionState.getInstance().enqueueExactPacket(packet);
            }

            this.showStatus("Queued " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
            return;
         }

         int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);

         for (Packet<?> packet : packets) {
            AutismSessionState.getInstance().sendPacketBypassingInterception(handler, packet);
         }

         if (queuedCount > 0) {
            this.showStatus("Sent " + queuedCount + " queued + " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
         } else {
            this.showStatus("Sent " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
         }
      } catch (NumberFormatException e) {
         this.showStatus("Invalid number format", ChatFormatting.RED);
      }
   }

   private void flush(boolean queue) {
      List<autismclient.util.macro.CraftAction.Inner_DataUtil> entries = this.activeEntries();
      if (entries.isEmpty()) {
         this.showStatus("Select a recipe or add a craft entry first", ChatFormatting.RED);
      } else if (!queue) {
         if (this.crafting) {
            this.showStatus("A craft is already running", ChatFormatting.YELLOW);
         } else {
            this.crafting = true;
            Thread craftThread = new Thread(() -> {
               AutoCraftingEngine.CraftResult result = null;

               for (autismclient.util.macro.CraftAction.Inner_DataUtil entryx : entries) {
                  AutoCraftingEngine.CraftOption optionx = AutoCraftingEngine.findOptionByKey(MINECRAFT, entryx.recipeKey);
                  if (optionx == null) {
                     optionx = AutoCraftingEngine.findOptionById(MINECRAFT, entryx.recipeId);
                  }

                  if (optionx == null) {
                     result = AutoCraftingEngine.CraftResult.failed("Recipe not found: " + entryx.resultName);
                     break;
                  }

                  int desiredAmountx = AutoCraftingEngine.resolveTargetAmount(optionx, entryx.amount, entryx.useMaxAmount);
                  if (desiredAmountx <= 0) {
                     result = AutoCraftingEngine.CraftResult.failed("No space or materials for " + entryx.resultName + ".");
                     break;
                  }

                  result = AutoCraftingEngine.craft(MINECRAFT, entryx.recipeKey, entryx.recipeId, desiredAmountx);
                  if (!result.success) {
                     break;
                  }
               }

               this.crafting = false;
               AutoCraftingEngine.CraftResult finalResult = result;
               if (finalResult != null && !finalResult.success) {
                  MINECRAFT.execute(() -> {
                     this.showStatus(finalResult.message, ChatFormatting.RED);
                     AutismClientMessaging.sendPrefixed("§c" + finalResult.message);
                  });
               }
            }, "Autism-Fabricator-Craft");
            craftThread.setDaemon(true);
            craftThread.start();
         }
      } else {
         int queuedClicks = 0;

         for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : entries) {
            AutoCraftingEngine.CraftOption option = AutoCraftingEngine.findOptionByKey(MINECRAFT, entry.recipeKey);
            if (option == null) {
               option = AutoCraftingEngine.findOptionById(MINECRAFT, entry.recipeId);
            }

            if (option == null) {
               String msg = "Recipe not found: " + entry.resultName;
               this.showStatus(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            int desiredAmount = AutoCraftingEngine.resolveTargetAmount(option, entry.amount, entry.useMaxAmount);
            if (desiredAmount <= 0) {
               String msg = "No space or materials for " + entry.resultName;
               this.showStatus(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            List<Packet<?>> packets = AutoCraftingEngine.buildCraftPackets(MINECRAFT, entry.recipeKey, entry.recipeId, desiredAmount);
            if (packets.isEmpty()) {
               String msg = option.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3
                     && (MINECRAFT.player == null || !(MINECRAFT.player.containerMenu instanceof CraftingMenu))
                  ? "Open a crafting table to queue " + entry.resultName + "."
                  : "Could not plan " + entry.resultName + " from this inventory.";
               this.showStatus(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            for (Packet<?> packet : packets) {
               AutismSessionState.getInstance().enqueueDelayedPacket(packet);
            }

            queuedClicks += packets.size();
         }

         this.showStatus("Queued " + queuedClicks + " packet(s) for " + entries.size() + " craft(s)", ChatFormatting.GREEN);
      }
   }

   private void init(int slot, int repeats, boolean queue, ClientPacketListener handler) {
      List<Packet<?>> packets = this.buildPackets(slot, repeats);
      if (packets.isEmpty()) {
         this.showStatus("Failed to build packet", ChatFormatting.RED);
      } else if (queue) {
         for (Packet<?> packet : packets) {
            AutismSessionState.getInstance().enqueueDelayedPacket(packet);
         }

         this.showStatus("Queued " + packets.size() + " packet(s)", ChatFormatting.GREEN);
      } else {
         int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);

         for (Packet<?> packet : packets) {
            DirectPacketSender.init(packet);
         }

         if (queuedCount > 0) {
            this.showStatus("Sent " + queuedCount + " queued + " + packets.size() + " new packet(s)", ChatFormatting.GREEN);
         } else {
            this.showStatus("Sent " + packets.size() + " packet(s)", ChatFormatting.GREEN);
         }
      }
   }

   private void showStatus(String message, ChatFormatting color) {
      this.statusMessage = message;
      this.statusColor = color;
      this.statusExpiresAt = 0L;
   }

   private void clearStatus() {
      this.statusMessage = "";
      this.statusColor = ChatFormatting.GRAY;
      this.statusExpiresAt = 0L;
   }

   private void expireStatus() {
      if (this.statusExpiresAt > 0L && System.currentTimeMillis() >= this.statusExpiresAt) {
         this.clearStatus();
      }
   }

   private String amountLabel() {
      if (this.isCraftMode()) {
         return "Amount";
      } else {
         return this.run_Z_2() && !this.dropWholeStack ? "Items" : "Times";
      }
   }

   private String write_Str_2() {
      if (this.isCraftMode()) {
         String planInfo = this.craftPlan.isEmpty() ? "Plan: none" : "Plan: " + this.craftPlan.size() + " entr" + (this.craftPlan.size() == 1 ? "y" : "ies");
         if (this.selectedOutput.isEmpty()) {
            return planInfo + " | Recipe: none selected";
         }

         String source = this.selectedOption != null && this.selectedOption.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3 ? "Table" : "2x2";
         String availability = this.selectedOption != null ? AutoCraftingEngine.formatStatusLabel(this.selectedOption) : "Unavailable";
         String amountMode = this.useMaxAmount ? "Max" : "x" + Math.max(1, this.store());
         return planInfo + " | Recipe: " + this.selectedOutput.getHoverName().getString() + " | " + source + " | " + availability + " | " + amountMode;
      } else if (this.action.write()) {
         ContainerClickSnapshot.Inner_StreamUtil target = this.getCapturedClick();
         return target == null ? "Packet: right-click a GUI slot to capture" : "Packet: " + target.init_Str();
      } else {
         Integer slot = this.enteredSlot();
         String itemName = this.enteredItemName();
         if (slot != null && !itemName.isEmpty()) {
            return "Target: '" + itemName + "' in exact slot " + slot;
         } else if (slot != null) {
            return "Target: exact slot " + slot;
         } else {
            return !itemName.isEmpty() ? "Target: best matching '" + itemName + "'" : "Target: choose a slot or enter an item name";
         }
      }
   }

   private AbstractContainerMenu openMenu() {
      if (MINECRAFT.player != null && MINECRAFT.player.containerMenu != null) {
         return MINECRAFT.player.containerMenu;
      } else {
         return this.parentScreen != null ? ((AbstractContainerScreenAccessor)this.parentScreen).autism$getMenu() : null;
      }
   }

   private Integer enteredSlot() {
      if (this.slotField == null) {
         return null;
      }

      String text = this.slotField.getValue().trim();
      if (text.isEmpty()) {
         return null;
      }

      try {
         return Integer.parseInt(text);
      } catch (NumberFormatException ignored) {
         return null;
      }
   }

   private String enteredItemName() {
      return this.itemField == null ? "" : this.itemField.getValue().trim();
   }

   private boolean isValidSlot(AbstractContainerMenu handler, int slotId) {
      return handler != null && slotId >= 0 && slotId < handler.slots.size();
   }

   private boolean stackMatches(ItemStack stack, String query) {
      return stack != null && !stack.isEmpty() && query != null && !query.isBlank()
         ? autismclient.util.macro.MacroItemTarget.parse(query).matchScore(stack, -1) >= 0
         : false;
   }

   private boolean slotMatches(AbstractContainerMenu handler, int slotId, String itemQuery) {
      if (!this.isValidSlot(handler, slotId)) {
         return false;
      }

      Slot slot = handler.slots.get(slotId);
      return slot != null && this.stackMatches(slot.getItem(), itemQuery);
   }

   private int chunk() {
      AbstractContainerMenu handler = this.openMenu();
      if (handler == null) {
         this.showStatus("No screen is open right now", ChatFormatting.RED);
         return -1;
      }

      Integer enteredSlot = this.enteredSlot();
      String itemName = this.enteredItemName();
      int resolvedSlot = enteredSlot == null ? -1 : InventorySlotHelper.resolveConfiguredSlot(MINECRAFT, enteredSlot);
      if (enteredSlot != null && !this.isValidSlot(handler, resolvedSlot)) {
         this.showStatus("Slot " + enteredSlot + " is not available in this screen", ChatFormatting.RED);
         return -1;
      }

      if (enteredSlot != null && !itemName.isEmpty()) {
         if (this.slotMatches(handler, resolvedSlot, itemName)) {
            return resolvedSlot;
         }

         this.showStatus("Slot " + enteredSlot + " does not contain '" + itemName + "'", ChatFormatting.RED);
         return -1;
      } else {
         if (enteredSlot != null) {
            return resolvedSlot;
         }

         if (!itemName.isEmpty()) {
            Integer foundSlot = this.findSlotByName(itemName);
            if (foundSlot == null) {
               this.showStatus("No item matching '" + itemName + "' found", ChatFormatting.RED);
               return -1;
            } else {
               return foundSlot;
            }
         } else {
            this.showStatus("Pick a slot or enter an item name first", ChatFormatting.RED);
            return -1;
         }
      }
   }

   private int read(int slot) {
      int value;
      try {
         value = Integer.parseInt(this.timesField.getValue().trim());
      } catch (NumberFormatException e) {
         this.showStatus(this.action.read() ? "Invalid amount" : "Invalid repeat count", ChatFormatting.RED);
         return -1;
      }

      if (value <= 0) {
         this.showStatus(
            this.action.read() ? "Amount must be at least 1" : "Repeat count must be at least 1", ChatFormatting.RED
         );
         return -1;
      }

      if (!this.action.read()) {
         return value;
      }

      if (this.selectedOption != null && !this.selectedOutput.isEmpty()) {
         if (MINECRAFT.player != null && MINECRAFT.player.containerMenu instanceof CraftingMenu craftingHandler) {
            int var7 = Math.max(1, this.selectedOutput.getCount());
            return Math.max(1, (int)Math.ceil((double)value / var7));
         } else {
            this.showStatus("Open a crafting screen first", ChatFormatting.RED);
            return -1;
         }
      } else {
         this.showStatus("Select a recipe first", ChatFormatting.RED);
         return -1;
      }
   }

   private int table() {
      if (this.selectedOption == null || this.selectedOutput.isEmpty()) {
         this.showStatus("Select a recipe first", ChatFormatting.RED);
         return -1;
      }

      if (this.useMaxAmount) {
         int maxAmount = this.selectedOption.maxOutputItems;
         if (maxAmount <= 0) {
            this.showStatus(this.selectedOption.hasMaterials ? "No room to store crafted items" : "Missing materials", ChatFormatting.RED);
            return -1;
         } else {
            return maxAmount;
         }
      } else {
         int value = this.store();
         if (value <= 0) {
            this.showStatus("Invalid craft amount", ChatFormatting.RED);
            return -1;
         } else {
            return value;
         }
      }
   }

   private int store() {
      if (this.timesField == null) {
         return 1;
      }

      try {
         int value = Integer.parseInt(this.timesField.getValue().trim());
         return Math.max(1, value);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   private List<Packet<?>> buildPackets(int slot, int repeats) {
      List<Packet<?>> packets = new ArrayList<>();

      for (int i = 0; i < repeats; i++) {
         Packet<?> built = this.buildPacket(slot);
         if (built == null) {
            return List.of();
         }

         packets.add(this.init_Packet_Packet(built));
      }

      return packets;
   }

   private Packet<?> init_Packet_Packet(Packet<?> packet) {
      if (this.action.write()) {
         return packet;
      } else {
         return packet instanceof ServerboundContainerClickPacket clickSlotPacket ? PacketRegenerator.init(clickSlotPacket) : packet;
      }
   }

   private Integer findSlotByName(String searchName) {
      if (searchName != null && !searchName.trim().isEmpty()) {
         AbstractContainerMenu handler = this.openMenu();
         if (handler == null) {
            return null;
         }

         autismclient.util.macro.MacroItemTarget target = autismclient.util.macro.MacroItemTarget.parse(searchName);
         Integer bestMatch = null;
         int bestScore = -1;

         for (Slot slot : handler.slots) {
            if (slot != null && !slot.getItem().isEmpty()) {
               int score = target.matchScore(slot.getItem(), -1);
               if (score > bestScore) {
                  bestScore = score;
                  bestMatch = slot.index;
               }
            }
         }

         return bestScore >= 0 ? bestMatch : null;
      } else {
         return null;
      }
   }

   private void resetLayout() {
      this.defaultWidth = 204;
      this.rowHeight = 12;
      this.listHeight = 6 * this.rowHeight;
      this.push_I = 52;
      this.clear = 100;
      this.reset = 13;
      this.start = 64;
   }

   private int drain() {
      return Math.max(80, this.load_I(this.clear_I_2()));
   }

   private int fill() {
      return Math.max(120, this.load_I(this.clear_I_2()));
   }

   private void computeLayout() {
      int listHeight = Math.max(this.rowHeight * 2, Math.min(6 * this.rowHeight, this.filteredOptions.size() * this.rowHeight));
      int planHeight = Math.max(26, Math.min(52, this.craftPlan.size() * 13));
      if (listHeight != this.listHeight || planHeight != this.push_I) {
         this.listHeight = listHeight;
         this.push_I = planHeight;
         if (this.isCraftMode()) {
            if (this.timesField != null) {
               this.timesField.setY(this.size());
            }

            if (this.searchField != null) {
               this.searchField.setY(this.valid());
            }
         }
      }
   }

   private enum Inner_DataUtil {
      CLICK("Click", "Use Left or Right on the resolved slot.") {
         @Override
         boolean init() {
            return true;
         }

         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return ContainerClickType.PICKUP;
         }
      },
      QUICK_MOVE("Quick Move", "Shift-click the resolved slot.") {
         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return ContainerClickType.QUICK_MOVE;
         }
      },
      PICKUP_ALL("Pick Up All", "Double-click the resolved slot. Usually needs you to hold a matching stack first.") {
         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return ContainerClickType.PICKUP_ALL;
         }
      },
      CRAFT_RESULT("Craft", "Choose a craftable item, set the total amount, then send, queue, or add it to a macro.") {
         @Override
         boolean read() {
            return true;
         }

         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return ContainerClickType.QUICK_MOVE;
         }
      },
      DROP("Drop", "Drop from the resolved slot. If slot and item are both set, both must match.") {
         @Override
         boolean load() {
            return true;
         }

         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return dropWholeStack ? ContainerClickType.DROP_STACK : ContainerClickType.DROP_ITEM;
         }

         @Override
         String description(boolean dropWholeStack) {
            return dropWholeStack ? "Drops the whole stack from the selected slot." : "Drops one item at a time from the selected slot.";
         }
      },
      PACKET("Packet", "Replay a captured slot click with the captured container id and state.") {
         @Override
         boolean init() {
            return true;
         }

         @Override
         boolean write() {
            return true;
         }

         @Override
         ContainerClickType clickType(boolean dropWholeStack) {
            return ContainerClickType.PICKUP;
         }
      };

      final String init_Str;
      final String load_Str;

      Inner_DataUtil(String displayName, String description) {
         this.init_Str = displayName;
         this.load_Str = description;
      }

      boolean init() {
         return false;
      }

      boolean load() {
         return false;
      }

      boolean read() {
         return false;
      }

      boolean write() {
         return false;
      }

      String description(boolean dropWholeStack) {
         return this.load_Str;
      }

      abstract ContainerClickType clickType(boolean var1);
   }

   private static final class Inner_StreamUtil {
      final long requestId;
      final Object connection;
      final List<AutoCraftingEngine.CraftOption> options;

      Inner_StreamUtil(long generation, Object connection, List<AutoCraftingEngine.CraftOption> options) {
         this.requestId = generation;
         this.connection = connection;
         this.options = options;
      }
   }
}
