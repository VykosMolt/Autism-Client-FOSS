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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static AutismFabricatorOverlay init_AutismFabricatorOv_2;
   private int flush_I = 204;
   private int close_I = 12;
   private int open_I = 72;
   private static final int poll_I = 6;
   private int push_I = 52;
   private static final int pop = 4;
   private static final int tick = 8;
   private static final int run = 4;
   private int get = 220;
   private int set = 5;
   private int put = this.flush_I;
   private int add = 240;
   private int clear = 100;
   private int reset = 13;
   private int start = 64;
   private int stop = -1;
   private float init_F_2 = 0.0F / 0.0F;
   private boolean write_Z = false;
   private double init_D = 0.0;
   private double load_D = 0.0;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private AbstractContainerScreen<?> init_AbstractContainerS;
   private final List<ThemedTextInputBox> init_List = new ArrayList<>();
   private ThemedTextInputBox init_k;
   private ThemedTextInputBox load_k;
   private ThemedTextInputBox read_k;
   private ThemedTextInputBox write_k;
   private int next = 0;
   private final AutismFabricatorOverlay.Inner_DataUtil[] init_AutismFabricatorOvArr = AutismFabricatorOverlay.Inner_DataUtil.values();
   private final List<String> load_List = Arrays.stream(this.init_AutismFabricatorOvArr).map(action -> action.init_Str).toList();
   private static final List<String> read_List = List.of("Left Click", "Right Click");
   private static final List<String> write_List = List.of("Left Click", "Right Click", "Quick Move");
   private static final List<String> update_List = List.of("Single", "Stack");
   private final List<autismclient.gui.ui.widgets.DropdownWidget> sync_List = new ArrayList<>();
   private autismclient.gui.ui.widgets.DropdownWidget init_SyncManager;
   private autismclient.gui.ui.widgets.DropdownWidget load_SyncManager;
   private autismclient.gui.ui.widgets.DropdownWidget read_SyncManager;
   private int prev = 0;
   private final String[] init_StrArr = new String[]{"Left Click", "Right Click", "Quick Move"};
   private boolean flush_Z = false;
   private ContainerClickSnapshot.Inner_StreamUtil init_bo_StreamUtil = null;
   private Integer init_Integer = null;
   private String init_Str_2 = "";
   private ChatFormatting init_ChatFormatting = ChatFormatting.GRAY;
   private long init_J = 0L;
   private final List<AutoCraftingEngine.CraftOption> flush_List = new ArrayList<>();
   private List<AutoCraftingEngine.CraftOption> close_List = new ArrayList<>();
   private int valid = 0;
   private int size = 0;
   private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private AutoCraftingEngine.CraftOption init_J_TaskQueue = null;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private final List<autismclient.util.macro.CraftAction.Inner_DataUtil> open_List = new ArrayList<>();
   private int data = -1;
   private int value = 0;
   private final autismclient.gui.ui.widgets.ScrollOffsetController load_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private static final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "autism-craft-scan");
      t.setDaemon(true);
      return t;
   });
   private final AtomicReference<AutismFabricatorOverlay.Inner_StreamUtil> init_AtomicReference = new AtomicReference<>();
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private volatile boolean close_Z;
   private int cache;
   private boolean open_Z;
   private volatile boolean poll_Z = false;
   private boolean push_Z = false;
   private int state = 0;
   private int queue = 0;
   private static final int buffer = 0;
   private static final int entry = 1;
   private static final int node = 2;
   private String load_Str = "0";
   private String read_Str = "";
   private autismclient.util.macro.MacroItemTarget init_ArrayUtil = new autismclient.util.macro.MacroItemTarget();
   private String write_Str = "1";
   private String update_Str = "";
   private int task = -1;
   private String sync_Str = "";
   private AutismFabricatorOverlay.Inner_DataUtil init_AutismFabricatorOv;

   private static int push_I_7() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private static int pop_I_7() {
      return push_I_7() + 2;
   }

   private int init_I_I_4(int rowY) {
      return UiLayoutMath.centerTextY(rowY, push_I_7(), init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), 0);
   }

   private int tick_I_5() {
      if (init_Minecraft.font == null) {
         return this.start;
      }

      float scale = autismclient.gui.sdf.MsdfTextRenderer.fontScale();
      if (this.stop > 0 && scale == this.init_F_2) {
         return this.stop;
      }

      int widest = 0;

      for (String label : List.of("Action", "Slot", "Item", "Click", "Drop Mode", "Times", "Items", "Amount")) {
         widest = Math.max(
            widest,
            autismclient.gui.f.b.k.init(
               init_Minecraft.font,
               label,
               init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
               init_CharEncoder.load(autismclient.gui.f.b.l.write)
            )
         );
      }

      this.stop = widest;
      this.init_F_2 = scale;
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
      return autismclient.gui.f.UiHelper.init(init_Minecraft.font).wrapLines(this.init_AutismFabricatorOv.init_Str(this.flush_Z), Math.max(1, this.put - 8));
   }

   private int clear_I_2() {
      return this.poll_Z_5() ? this.count() + this.open_I + 12 : this.prev_I() + push_I_7() + 4;
   }

   private int load_I(int floorY) {
      int relativeFloor = Math.max(0, floorY - this.set);
      return relativeFloor + 11 + this.init_List_4().size() * 10 + 3 + this.reset + 4;
   }

   private boolean poll_Z_5() {
      return this.init_AutismFabricatorOv.read();
   }

   private boolean push_Z_4() {
      return !this.poll_Z_5();
   }

   private boolean pop_Z_3() {
      return this.push_Z_4() && !this.init_AutismFabricatorOv.write();
   }

   private int reset_I_2() {
      return this.set + 14 + 4;
   }

   private int start_I_5() {
      return this.reset_I_2() + pop_I_7();
   }

   private int stop_I_2() {
      return this.start_I_5() + (this.pop_Z_3() ? pop_I_7() : 0);
   }

   private int next_I_2() {
      return this.stop_I_2() + (this.pop_Z_3() ? pop_I_7() : 0);
   }

   private int prev_I() {
      return !this.tick_Z_3() && !this.run_Z_2() ? this.stop_I_2() + pop_I_7() : this.next_I_2() + pop_I_7();
   }

   private int valid() {
      return this.node() + 12;
   }

   private int size() {
      return this.entry() + 12;
   }

   private int data() {
      return init_Minecraft.font == null
         ? 45
         : autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(
               init_Minecraft.font,
               "Use Max",
               init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               init_CharEncoder.load(autismclient.gui.f.b.l.read)
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
      return this.queue() + push_I_7() + 1;
   }

   private int entry() {
      return this.buffer() + this.push_I + 2;
   }

   private int node() {
      return this.size() + push_I_7() + 4;
   }

   private autismclient.gui.f.NetHelper init_NetHelper_2() {
      int listRight = this.flags() + this.level();
      int w = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(
            init_Minecraft.font,
            "Clear All",
            init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            init_CharEncoder.load(autismclient.gui.f.b.l.read)
         )
      );
      int h = 13;
      int y = this.queue() + Math.max(0, (init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.load, 1) - h) / 2) - 1;
      return autismclient.gui.f.NetHelper.init(listRight - w - 2, y, w, h);
   }

   private boolean tick_Z_3() {
      return this.init_AutismFabricatorOv.init();
   }

   private int task() {
      return this.init_AutismFabricatorOv.write() ? ContainerClickSnapshot.Inner_DataUtil.values().length : 2;
   }

   private ContainerClickSnapshot.Inner_DataUtil init_bo_DataUtil_2() {
      return ContainerClickSnapshot.Inner_DataUtil.init(this.prev);
   }

   private boolean run_Z_2() {
      return this.init_AutismFabricatorOv.load();
   }

   private int pool() {
      if (this.init_AutismFabricatorOv.write()) {
         return this.init_bo_DataUtil_2().init_I;
      }

      if (this.init_AutismFabricatorOv.load()) {
         return this.flush_Z ? 1 : 0;
      }

      ContainerClickType packetAction = this.init_AutismFabricatorOv.init_N(this.flush_Z);
      return packetAction.load() ? packetAction.init_I() : Math.min(this.prev, 1);
   }

   private String load_Str_5() {
      if (this.init_AutismFabricatorOv.write()) {
         return this.init_bo_DataUtil_2().init_Str;
      } else if (this.tick_Z_3()) {
         return this.init_StrArr[Math.min(this.prev, 1)];
      } else if (this.run_Z_2()) {
         return this.flush_Z ? "Whole Stack" : "Single Item";
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
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, init_Minecraft.font, x, y, w, h, label, variant, enabled, mouseX, mouseY);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean enabled, String animationKey, int mouseX, int mouseY) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, init_Minecraft.font, x, y, w, h, label, enabled, animationKey, mouseX, mouseY);
   }

   public AutismFabricatorOverlay(AbstractContainerScreen<?> parentScreen) {
      this.init_AutismFabricatorOv = AutismFabricatorOverlay.Inner_DataUtil.CLICK;
      this.init_AbstractContainerS = parentScreen;
      this.next_V_2();
      this.put = this.flush_I;
      this.add = this.fill();
   }

   public static synchronized AutismFabricatorOverlay init_AutismFabricatorOv_AbstractContainerS(AbstractContainerScreen<?> parentScreen) {
      if (init_AutismFabricatorOv_2 == null) {
         init_AutismFabricatorOv_2 = new AutismFabricatorOverlay(parentScreen);
         init_AutismFabricatorOv_2.write_V_8();
      } else {
         init_AutismFabricatorOv_2.init_AbstractContainerS = parentScreen;
      }

      return init_AutismFabricatorOv_2;
   }

   @Override
   public int getMinWidth() {
      return this.flush_I;
   }

   @Override
   public int getMinHeight() {
      return this.drain();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.get, this.set, this.put, this.add, this.sync_Z, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.get = clamped.x;
         this.set = clamped.y;
         this.put = Math.min(clamped.width, this.flush_I);
         this.add = clamped.height;
         this.sync_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
         if (this.sync_Z) {
            this.update_V_6();
         }
      }
   }

   public void load_V_7() {
      this.setVisible(!this.sync_Z);
   }

   @Override
   public void setVisible(boolean visible) {
      this.sync_Z = visible;
      if (visible) {
         this.update_V_6();
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.clear_V_2();
      }

      this.read_V_3();
   }

   @Override
   public boolean isVisible() {
      return this.sync_Z;
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
      if (!this.sync_Z) {
         return false;
      }

      int panelHeight = this.update_Z ? 14 : this.add;
      return mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + panelHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.sync_Z) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + 14 && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   @Override
   public boolean hasFocusedTextInput() {
      for (ThemedTextInputBox field : this.init_List) {
         if (field.isFocused()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void clearInputFocus() {
      for (ThemedTextInputBox field : this.init_List) {
         field.setFocused(false);
      }
   }

   public void read_V_3() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setFabricatorVisible(this.sync_Z);
      shared.setFabricatorX(this.get);
      shared.setFabricatorY(this.set);
      shared.setFabricatorSlotInput(this.load_Str);
      shared.setFabricatorItemInput(this.read_Str);
      shared.setFabricatorCountInput(this.write_Str);
      shared.setFabricatorActionIndex(this.next);
      shared.setFabricatorClickModeIndex(this.prev);
      shared.setFabricatorDropWholeStack(this.flush_Z);
      shared.setFabricatorUseMaxAmount(this.push_Z);
      shared.setFabricatorRecipeSearch(this.update_Str);
      shared.setFabricatorSelectedRecipeId(this.task);
      shared.setFabricatorSelectedRecipeKey(this.sync_Str);
      shared.setFabricatorRecipeScrollOffset(this.size);
      shared.setFabricatorCraftEntries(this.open_List);
      shared.setFabricatorSelectedCraftIndex(this.data);
      shared.setFabricatorCraftScrollOffset(this.value);
      this.saveBounds();
   }

   public void write_V_8() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.sync_Z = shared.isFabricatorVisible();
      this.get = shared.getFabricatorX();
      this.set = shared.getFabricatorY();
      this.load_Str = shared.getFabricatorSlotInput();
      this.read_Str = shared.getFabricatorItemInput();
      this.write_Str = shared.getFabricatorCountInput();
      this.next = shared.getFabricatorActionIndex();
      this.prev = shared.getFabricatorClickModeIndex();
      this.flush_Z = shared.isFabricatorDropWholeStack();
      this.push_Z = shared.isFabricatorUseMaxAmount();
      this.update_Str = shared.getFabricatorRecipeSearch();
      this.task = shared.getFabricatorSelectedRecipeId();
      this.sync_Str = shared.getFabricatorSelectedRecipeKey();
      this.size = shared.getFabricatorRecipeScrollOffset();
      this.init_CacheEntry.setOffsetUnclamped(this.size * this.close_I);
      this.open_List.clear();
      this.open_List.addAll(shared.getFabricatorCraftEntries());
      this.data = shared.getFabricatorSelectedCraftIndex();
      this.value = shared.getFabricatorCraftScrollOffset();
      this.load_CacheEntry.setOffsetUnclamped(this.value * 13);
      this.init_J_TaskQueue = null;
      this.init_ItemStack = ItemStack.EMPTY;
      if (this.next < 0 || this.next >= this.init_AutismFabricatorOvArr.length) {
         this.next = 0;
      }

      this.init_AutismFabricatorOv = this.init_AutismFabricatorOvArr[this.next];
      if (this.prev < 0 || this.prev >= this.init_StrArr.length) {
         this.prev = 0;
      }

      if (this.sync_Z) {
         this.update_V_6();
      }
   }

   private void update_V_6() {
      this.clear_V_2();
      if (!this.write_Z) {
         this.prev_V_2();
         this.add = this.poll_Z_5() ? this.fill() : this.drain();
      }

      int fieldX = this.run_I_5();
      int fieldWidth = this.get_I_5();
      this.init_AutismFabricatorOv = this.init_AutismFabricatorOvArr[this.next];
      this.init_k = new ThemedTextInputBox(init_Minecraft, init_Minecraft.font, fieldX, this.start_I_5(), fieldWidth, push_I_7(), false);
      this.init_k.setPlaceholder(Component.literal("Optional slot - right-click to fill"));
      this.init_k.setValue(this.load_Str);
      this.init_k.setMaxLength(5);
      this.init_k.setChangedListener(text -> {
         this.load_Str = text;
         this.reset_V_2();

         try {
            int stableSlot = Integer.parseInt(text);
            int slotId = InventorySlotHelper.resolveConfiguredSlot(init_Minecraft, stableSlot);
            autismclient.util.macro.MacroItemTarget slotTarget = this.init_ArrayUtil_I(slotId);
            String itemName = slotTarget.rawLabel();
            if (!itemName.isEmpty()) {
               this.init_ArrayUtil = slotTarget;
               this.read_Str = itemName;
               if (this.load_k != null) {
                  this.load_k.setValue(itemName);
               }
            }
         } catch (NumberFormatException var6x) {
         }
      });
      if (this.pop_Z_3()) {
         this.init_List.add(this.init_k);
      }

      this.load_k = new ThemedTextInputBox(init_Minecraft, init_Minecraft.font, fieldX, this.stop_I_2(), fieldWidth, push_I_7(), false);
      this.load_k.setPlaceholder(Component.literal("Optional item match").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY)));
      this.load_k.setValue(this.read_Str);
      this.load_k.setMaxLength(50);
      this.load_k.setDisplayTextProvider(value -> {
         Component rich = this.init_ArrayUtil == null ? null : this.init_ArrayUtil.restyleText(value);
         return rich != null && value != null && value.equals(rich.getString()) ? rich.copy() : null;
      });
      this.load_k.setChangedListener(text -> {
         this.read_Str = text;
         if (this.init_ArrayUtil != null && !text.equals(this.init_ArrayUtil.rawLabel())) {
            this.init_ArrayUtil = this.init_ArrayUtil.hasTextJson() ? this.init_ArrayUtil.withManualDisplay(text) : autismclient.util.macro.MacroItemTarget.parse(text);
         }

         this.reset_V_2();
      });
      if (this.pop_Z_3()) {
         this.init_List.add(this.load_k);
      }

      int timesY = this.poll_Z_5() ? this.size() : this.prev_I();
      int timesX = this.poll_Z_5() ? this.value() : fieldX;
      int timesWidth = this.poll_Z_5() ? this.state() : fieldWidth;
      this.read_k = new ThemedTextInputBox(init_Minecraft, init_Minecraft.font, timesX, timesY, timesWidth, push_I_7(), false);
      this.read_k.setPlaceholder(Component.literal("1"));
      this.read_k.setValue(this.write_Str);
      this.read_k.setMaxLength(5);
      this.read_k.setEditable(!this.poll_Z_5() || !this.push_Z);
      this.read_k.setChangedListener(text -> {
         this.write_Str = text;
         if (this.poll_Z_5() && this.data >= 0 && this.data < this.open_List.size()) {
            try {
               int val = Integer.parseInt(text.replaceAll("[^0-9]", ""));
               this.open_List.get(this.data).amount = Math.max(1, val);
            } catch (Exception var3x) {
            }
         }

         this.reset_V_2();
      });
      this.init_List.add(this.read_k);
      if (this.poll_Z_5()) {
         int searchY = this.valid();
         this.write_k = new ThemedTextInputBox(init_Minecraft, init_Minecraft.font, this.get + 4, searchY, this.put - 8, push_I_7(), false);
         this.write_k.setPlaceholder(Component.literal("Search crafting recipes..."));
         this.write_k.setValue(this.update_Str);
         this.write_k.setChangedListener(text -> {
            this.update_Str = text;
            this.reset_V_2();
            this.init_V_Str_Z(text, true);
            this.read_V_3();
         });
         this.init_List.add(this.write_k);
         this.sync_V_5();
      } else {
         this.write_k = null;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.sync_Z) {
         return false;
      }

      if (!this.sync_List.isEmpty()) {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.sync_List, mouseX, mouseY, button)) {
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.sync_List)) {
            return true;
         }
      }

      if (button == 0 && mouseX >= this.get && mouseX <= this.get + this.put && mouseY >= this.set && mouseY <= this.set + 14) {
         OverlayBounds bounds = new OverlayBounds(this.get, this.set, this.put, this.update_Z ? 14 : this.add, this.sync_Z, this.update_Z);
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         }

         this.write_Z = true;
         this.init_D = mouseX - this.get;
         this.load_D = mouseY - this.set;

         for (ThemedTextInputBox field : this.init_List) {
            field.setFocused(false);
         }

         return true;
      } else {
         if (this.update_Z) {
            return false;
         }

         MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, Compat263.newGlfwMouseButtonInfo(button, 0));
         boolean clickedField = false;

         for (ThemedTextInputBox field : this.init_List) {
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

         if (this.poll_Z_5()) {
            this.sync_V_5();
            int listX = this.flags();
            int listWidth = this.level();
            int planContentWidth = this.frame();
            int recipeContentWidth = this.block();
            int planListY = this.buffer();
            int listY = this.count();
            if (button == 0) {
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftPlanScrollbar = this.init_PoolEntry_DataUtil();
               if (craftPlanScrollbar.isScrollable() && craftPlanScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
                  this.state = 1;
                  this.queue = Math.max(0, (int)mouseY - craftPlanScrollbar.thumbY());
                  this.value = this.init_I_PoolEntry_DataUtil(craftPlanScrollbar, (int)mouseY, this.queue, 13, Math.max(0, this.open_List.size() - 4));
                  this.load_CacheEntry.jumpTo(this.value * 13, Math.max(0, this.open_List.size() * 13 - this.push_I));
                  this.read_V_3();
                  return true;
               }

               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftListScrollbar = this.load_PoolEntry_DataUtil();
               if (craftListScrollbar.isScrollable() && craftListScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
                  this.state = 2;
                  this.queue = Math.max(0, (int)mouseY - craftListScrollbar.thumbY());
                  this.size = this.init_I_PoolEntry_DataUtil(craftListScrollbar, (int)mouseY, this.queue, this.close_I, Math.max(0, this.close_List.size() - 6));
                  this.init_CacheEntry.jumpTo(this.size * this.close_I, Math.max(0, this.close_List.size() * this.close_I - this.open_I));
                  this.read_V_3();
                  return true;
               }
            }

            if (!this.open_List.isEmpty() && this.init_NetHelper_2().init_Z((int)mouseX, (int)mouseY) && button == 0) {
               this.open_List.clear();
               this.data = -1;
               this.value = 0;
               this.load_CacheEntry.jumpTo(0, 0);
               this.reset_V_2();
               this.read_V_3();
               return true;
            }

            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= planListY && mouseY <= planListY + this.push_I && button == 0) {
               int maxPlanScrollPx = Math.max(0, this.open_List.size() * 13 - this.push_I);
               int drawPlanScroll = this.load_CacheEntry.tickAndClamp(0.0F, maxPlanScrollPx);
               int firstIndex = drawPlanScroll / 13;
               int rowY = planListY - drawPlanScroll % 13;

               for (int index = firstIndex; index < this.open_List.size() && rowY < planListY + this.push_I; rowY += 13) {
                  if (!(mouseY < rowY) && !(mouseY >= rowY + 12)) {
                     if (mouseX >= listX + planContentWidth - 16 && mouseX < listX + planContentWidth - 2) {
                        this.load_V(index);
                        return true;
                     }

                     if (mouseX >= listX && mouseX < listX + planContentWidth) {
                        if (this.data == index) {
                           this.init_V_I_2(-1);
                        } else {
                           this.init_V_I_2(index);
                        }

                        for (ThemedTextInputBox field : this.init_List) {
                           field.setFocused(false);
                        }

                        return true;
                     }
                  }

                  index++;
               }

               if (mouseX >= listX && mouseX < listX + planContentWidth) {
                  this.init_V_I_2(-1);

                  for (ThemedTextInputBox field : this.init_List) {
                     field.setFocused(false);
                  }

                  return true;
               }
            }

            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + this.open_I && button == 0) {
               int maxRecipeScrollPx = Math.max(0, this.close_List.size() * this.close_I - this.open_I);
               int drawRecipeScroll = this.init_CacheEntry.tickAndClamp(0.0F, maxRecipeScrollPx);
               int firstIndex = drawRecipeScroll / this.close_I;
               int rowY = listY - drawRecipeScroll % this.close_I;

               for (int index = firstIndex; index < this.close_List.size() && rowY < listY + this.open_I; rowY += this.close_I) {
                  if (mouseX >= listX && mouseX < listX + recipeContentWidth && mouseY >= rowY && mouseY < rowY + this.close_I) {
                     this.init_V_J_TaskQueue(this.close_List.get(index));
                     this.set_V_3();

                     for (ThemedTextInputBox field : this.init_List) {
                        field.setFocused(false);
                     }

                     return true;
                  }

                  index++;
               }

               if (mouseX >= listX && mouseX < listX + recipeContentWidth) {
                  this.poll_V_3();

                  for (ThemedTextInputBox field : this.init_List) {
                     field.setFocused(false);
                  }

                  this.read_V_3();
                  return true;
               }
            }
         }

         if (this.poll_Z_5()) {
            int toggleX = this.cache();
            int toggleY = this.size();
            int toggleW = this.data();
            if (mouseY >= toggleY && mouseY < toggleY + push_I_7() && mouseX >= toggleX && mouseX < toggleX + toggleW && (button == 0 || button == 1)) {
               this.push_Z = !this.push_Z;
               if (this.read_k != null) {
                  this.read_k.setEditable(!this.push_Z);
               }

               if (this.data >= 0 && this.data < this.open_List.size()) {
                  this.open_List.get(this.data).useMaxAmount = this.push_Z;
               }

               this.reset_V_2();
               this.read_V_3();

               for (ThemedTextInputBox field : this.init_List) {
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

               for (ThemedTextInputBox field : this.init_List) {
                  field.setFocused(false);
               }

               return true;
            }

            bx += bw + gap;
            if (mouseX >= bx && mouseX < bx + bw) {
               this.load(true);

               for (ThemedTextInputBox field : this.init_List) {
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
                  this.init_V_AutismMacroEditorO(macroEditor);
               } else {
                  this.init_V_Str_ChatFormatting("Open a macro editor first!", ChatFormatting.RED);
               }

               for (ThemedTextInputBox field : this.init_List) {
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
      autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.sync_List);
      if (button == 0 && this.state != 0) {
         this.state = 0;
         this.read_V_3();
         return true;
      }

      if (button == 0) {
         if (this.write_Z) {
            this.read_V_3();
         }

         this.write_Z = false;
      }

      for (ThemedTextInputBox field : this.init_List) {
         if (field.mouseReleased(mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.sync_List, mouseX, mouseY, button)) {
         return true;
      }

      if (this.state == 1) {
         this.value = this.init_I_PoolEntry_DataUtil(this.init_PoolEntry_DataUtil(), (int)mouseY, this.queue, 13, Math.max(0, this.open_List.size() - 4));
         this.load_CacheEntry.jumpTo(this.value * 13, Math.max(0, this.open_List.size() * 13 - this.push_I));
         return true;
      }

      if (this.state == 2) {
         this.size = this.init_I_PoolEntry_DataUtil(
            this.load_PoolEntry_DataUtil(), (int)mouseY, this.queue, this.close_I, Math.max(0, this.close_List.size() - 6)
         );
         this.init_CacheEntry.jumpTo(this.size * this.close_I, Math.max(0, this.close_List.size() * this.close_I - this.open_I));
         return true;
      }

      if (this.write_Z) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.init_D), (int)(mouseY - this.load_D), this.put, this.add, this.sync_Z, this.update_Z)
         );
         this.get = nextBounds.x;
         this.set = nextBounds.y;
         this.update_V_6();
         return true;
      }

      for (ThemedTextInputBox field : this.init_List) {
         if (field.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.sync_Z && !this.update_Z) {
         if (!this.sync_List.isEmpty()
            && (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.sync_List) || autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.sync_List, mouseX, mouseY))
            && autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.sync_List, mouseX, mouseY, amount)) {
            return true;
         }

         if (this.poll_Z_5()) {
            int planX = this.flags();
            int planY = this.buffer();
            int listWidth = this.level();
            if (mouseX >= planX && mouseX <= planX + listWidth && mouseY >= planY && mouseY <= planY + this.push_I) {
               int maxPlanScroll = Math.max(0, this.open_List.size() - 4);
               this.value = Math.max(0, Math.min(maxPlanScroll, this.value - (int)Math.signum(amount)));
               this.load_CacheEntry.setOffset(this.value * 13, Math.max(0, this.open_List.size() * 13 - this.push_I));
               this.read_V_3();
               return true;
            }

            int listX = this.flags();
            int listY = this.count();
            if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + this.open_I) {
               int maxScroll = Math.max(0, this.close_List.size() - 6);
               this.size = Math.max(0, Math.min(maxScroll, this.size - (int)Math.signum(amount)));
               this.init_CacheEntry.setOffset(this.size * this.close_I, Math.max(0, this.close_List.size() * this.close_I - this.open_I));
               this.read_V_3();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String init_Str_I_2(int slotId) {
      autismclient.util.macro.MacroItemTarget target = this.init_ArrayUtil_I(slotId);
      return target.rawLabel();
   }

   private autismclient.util.macro.MacroItemTarget init_ArrayUtil_I(int slotId) {
      AbstractContainerMenu handler = this.init_AbstractContainerM();
      if (handler != null && slotId >= 0 && slotId < handler.slots.size()) {
         Slot slot = handler.slots.get(slotId);
         if (slot != null && !slot.getItem().isEmpty()) {
            int visibleSlot = InventorySlotHelper.toVisibleSlot(init_Minecraft, slot.index);
            return autismclient.util.macro.MacroItemTarget.fromItemStack(slot.getItem(), visibleSlot);
         } else {
            return new autismclient.util.macro.MacroItemTarget();
         }
      } else {
         return new autismclient.util.macro.MacroItemTarget();
      }
   }

   private void sync_V_5() {
      if (this.poll_Z_5() && init_Minecraft.player != null && init_Minecraft.level != null) {
         AutismFabricatorOverlay.Inner_StreamUtil result = this.init_AtomicReference.getAndSet(null);
         if (result != null && result.init_J == this.init_AtomicLong.get() && result.init_Obj == init_Minecraft.getConnection()) {
            this.init_V_List_2(result.init_List);
         }

         if (!this.close_Z) {
            int fingerprint = this.index();
            if (!this.open_Z || fingerprint != this.cache) {
               this.open_Z = true;
               this.cache = fingerprint;
               this.flush_V_5();
            }
         }
      }
   }

   private void flush_V_5() {
      long perfStart = bK.init_J();
      AutoCraftingEngine.CraftContext snapshot = AutoCraftingEngine.captureContext(init_Minecraft);
      bK.load("craft.snapshot", perfStart, 5000000L);
      if (snapshot != null) {
         long generation = this.init_AtomicLong.incrementAndGet();
         this.close_Z = true;
         init_ExecutorService.execute(() -> {
            try {
               List<AutoCraftingEngine.CraftOption> options = AutoCraftingEngine.scanOptions(snapshot);
               this.init_AtomicReference.set(new AutismFabricatorOverlay.Inner_StreamUtil(generation, snapshot.connection, options));
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] craft recipe scan failed", t);
            } finally {
               this.close_Z = false;
            }
         });
      }
   }

   private void init_V_List_2(List<AutoCraftingEngine.CraftOption> options) {
      this.flush_List.clear();
      this.flush_List.addAll(options);
      this.valid = 0;

      for (AutoCraftingEngine.CraftOption option : this.flush_List) {
         if (option.craftable) {
            this.valid++;
         }
      }

      this.init_V_Str_Z(this.write_k != null ? this.write_k.getValue() : this.update_Str, false);
      this.close_V_5();
      this.open_V_5();
   }

   private int index() {
      int hash = System.identityHashCode(init_Minecraft.getConnection());
      if (init_Minecraft.player == null) {
         return hash;
      }

      AbstractContainerMenu menu = init_Minecraft.player.containerMenu;
      if (menu != null) {
         hash = hash * 31 + menu.containerId;
         hash = hash * 31 + menu.getStateId();
      }

      Inventory inventory = init_Minecraft.player.getInventory();

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

   private void init_V_Str_Z(String query, boolean resetScroll) {
      this.update_Str = query == null ? "" : query;
      this.close_List = AutoCraftingEngine.filterOptions(this.flush_List, query);
      int maxScroll = Math.max(0, this.close_List.size() - 6);
      this.size = resetScroll ? 0 : Math.max(0, Math.min(maxScroll, this.size));
   }

   private void close_V_5() {
      if (this.init_J_TaskQueue != null) {
         for (AutoCraftingEngine.CraftOption option : this.flush_List) {
            if (!option.recipeKey.isBlank() && option.recipeKey.equalsIgnoreCase(this.init_J_TaskQueue.recipeKey)
               || option.recipeKeyHash >= 0 && option.recipeKeyHash == this.init_J_TaskQueue.recipeKeyHash
               || option.syncedRecipeIndex >= 0 && option.syncedRecipeIndex == this.init_J_TaskQueue.syncedRecipeIndex) {
               this.init_J_TaskQueue = option;
               this.init_ItemStack = option.outputStack.copy();
               this.task = option.recipeKeyHash;
               this.sync_Str = option.recipeKey;
               return;
            }
         }

         this.init_J_TaskQueue = null;
         this.init_ItemStack = ItemStack.EMPTY;
         this.task = -1;
         this.sync_Str = "";
      }
   }

   private void open_V_5() {
      if (this.sync_Str != null && !this.sync_Str.isBlank() || this.task >= 0) {
         if (this.init_J_TaskQueue != null) {
            if (!this.sync_Str.isBlank() && this.sync_Str.equalsIgnoreCase(this.init_J_TaskQueue.recipeKey)) {
               return;
            }

            if (this.task >= 0 && (this.init_J_TaskQueue.recipeKeyHash == this.task || this.init_J_TaskQueue.syncedRecipeIndex == this.task)) {
               return;
            }
         }

         for (AutoCraftingEngine.CraftOption option : this.flush_List) {
            if (!this.sync_Str.isBlank() && this.sync_Str.equalsIgnoreCase(option.recipeKey)
               || this.task >= 0 && option.recipeKeyHash == this.task
               || this.task >= 0 && option.syncedRecipeIndex == this.task) {
               this.init_J_TaskQueue = option;
               this.init_ItemStack = option.outputStack.copy();
               this.init_ArrayUtil = autismclient.util.macro.MacroItemTarget.fromItemStack(option.outputStack, -1);
               this.read_Str = option.outputName;
               this.sync_Str = option.recipeKey;
               this.task = option.recipeKeyHash;
               if (this.load_k != null) {
                  this.load_k.setValue(this.read_Str);
               }

               return;
            }
         }
      }
   }

   private int count() {
      return this.valid() + push_I_7() + 2;
   }

   private int flags() {
      return this.get + 4;
   }

   private int level() {
      return this.put - 8;
   }

   private int frame() {
      return Math.max(40, this.level() - (this.init_PoolEntry_DataUtil().isScrollable() ? 8 : 0));
   }

   private int block() {
      return Math.max(40, this.level() - (this.load_PoolEntry_DataUtil().isScrollable() ? 8 : 0));
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil() {
      int listX = this.flags();
      int listY = this.buffer();
      int listWidth = this.level();
      int contentHeight = this.open_List.size() * 13;
      int maxScroll = Math.max(0, contentHeight - this.push_I);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight, this.push_I, listX + listWidth - 5, listY, 3, this.push_I, this.load_CacheEntry.tickAndClamp(0.0F, maxScroll)
      );
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics load_PoolEntry_DataUtil() {
      int listX = this.flags();
      int listY = this.count();
      int listWidth = this.level();
      int contentHeight = this.close_List.size() * this.close_I;
      int maxScroll = Math.max(0, contentHeight - this.open_I);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight, this.open_I, listX + listWidth - 5, listY, 3, this.open_I, this.init_CacheEntry.tickAndClamp(0.0F, maxScroll)
      );
   }

   private int init_I_PoolEntry_DataUtil(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics, int mouseY, int grabOffset, int rowHeight, int maxScrollRows) {
      int pixelScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, mouseY, grabOffset);
      return Math.max(0, Math.min(maxScrollRows, Math.round((float)pixelScroll / rowHeight)));
   }

   private void init_V_J_TaskQueue(AutoCraftingEngine.CraftOption option) {
      if (option != null) {
         this.init_J_TaskQueue = option;
         this.init_ItemStack = option.outputStack.copy();
         this.init_ArrayUtil = autismclient.util.macro.MacroItemTarget.fromItemStack(option.outputStack, -1);
         this.read_Str = option.outputName;
         this.task = option.recipeKeyHash;
         this.sync_Str = option.recipeKey;
         if (this.load_k != null) {
            this.load_k.setValue(this.read_Str);
         }

         this.init_V_Str_ChatFormatting(
            "Selected craft: "
               + option.outputName
               + " | "
               + (option.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3 ? "Table" : "2x2")
               + " | "
               + AutoCraftingEngine.formatStatusLabel(option),
            option.craftable ? ChatFormatting.GREEN : ChatFormatting.RED
         );
         this.read_V_3();
      }
   }

   private void poll_V_3() {
      this.data = -1;
      this.init_J_TaskQueue = null;
      this.init_ItemStack = ItemStack.EMPTY;
      this.init_ArrayUtil = new autismclient.util.macro.MacroItemTarget();
      this.task = -1;
      this.sync_Str = "";
   }

   private AutoCraftingEngine.CraftOption init_J_TaskQueue_SyncManager_DataUt(autismclient.util.macro.CraftAction.Inner_DataUtil entry) {
      return entry == null ? null : AutoCraftingEngine.matchOptionInList(this.flush_List, entry.recipeKey, entry.recipeId);
   }

   private autismclient.util.macro.CraftAction.Inner_DataUtil init_SyncManager_DataUt() {
      if (this.init_J_TaskQueue != null && !this.init_ItemStack.isEmpty()) {
         int configuredAmount = this.store();
         return configuredAmount <= 0 ? null : autismclient.util.macro.CraftAction.Inner_DataUtil.init(this.init_J_TaskQueue, configuredAmount, this.push_Z);
      } else {
         return null;
      }
   }

   private autismclient.util.macro.CraftAction.Inner_DataUtil load_SyncManager_DataUt() {
      return this.data >= 0 && this.data < this.open_List.size() ? this.open_List.get(this.data) : null;
   }

   private void init_V_I_2(int index) {
      if (index >= 0 && index < this.open_List.size()) {
         this.data = index;
         autismclient.util.macro.CraftAction.Inner_DataUtil entry = this.open_List.get(index);
         if (entry != null) {
            this.push_Z = entry.useMaxAmount;
            this.write_Str = String.valueOf(Math.max(1, entry.amount));
            if (this.read_k != null) {
               this.read_k.setValue(this.write_Str);
               this.read_k.setEditable(!this.push_Z);
            }

            AutoCraftingEngine.CraftOption option = this.init_J_TaskQueue_SyncManager_DataUt(entry);
            if (option != null) {
               this.init_V_J_TaskQueue(option);
            } else {
               this.task = entry.recipeId;
               this.sync_Str = entry.recipeKey;
               this.init_J_TaskQueue = null;
               this.init_ItemStack = ItemStack.EMPTY;
            }
         }

         this.read_V_3();
      } else {
         this.data = -1;
         this.init_J_TaskQueue = null;
         this.init_ItemStack = ItemStack.EMPTY;
         this.task = -1;
         this.sync_Str = "";
         this.read_V_3();
      }
   }

   private void set_V_3() {
      autismclient.util.macro.CraftAction.Inner_DataUtil draft = this.init_SyncManager_DataUt();
      if (draft == null) {
         this.init_V_Str_ChatFormatting("Select a recipe and amount first", ChatFormatting.RED);
      } else {
         int existingIndex = -1;

         for (int i = 0; i < this.open_List.size(); i++) {
            autismclient.util.macro.CraftAction.Inner_DataUtil existing = this.open_List.get(i);
            if (existing != null
               && (!draft.recipeKey.isBlank() && draft.recipeKey.equalsIgnoreCase(existing.recipeKey) || draft.recipeId >= 0 && draft.recipeId == existing.recipeId)) {
               existingIndex = i;
               break;
            }
         }

         if (this.data >= 0 && this.data < this.open_List.size()) {
            this.open_List.set(this.data, draft);
         } else if (existingIndex >= 0) {
            this.open_List.set(existingIndex, draft);
            this.data = existingIndex;
         } else {
            this.open_List.add(draft);
            this.data = this.open_List.size() - 1;
         }

         int maxPlanScroll = Math.max(0, this.open_List.size() - 4);
         this.value = Math.max(0, Math.min(maxPlanScroll, this.data));
         this.read_V_3();
      }
   }

   private void load_V(int index) {
      if (index >= 0 && index < this.open_List.size()) {
         this.open_List.remove(index);
         if (this.data == index) {
            this.data = -1;
         } else if (this.data > index) {
            this.data--;
         }

         this.value = Math.max(0, Math.min(Math.max(0, this.open_List.size() - 4), this.value));
         this.read_V_3();
      }
   }

   private List<autismclient.util.macro.CraftAction.Inner_DataUtil> load_List_2() {
      if (!this.open_List.isEmpty()) {
         List<autismclient.util.macro.CraftAction.Inner_DataUtil> copies = new ArrayList<>(this.open_List.size());

         for (autismclient.util.macro.CraftAction.Inner_DataUtil entry : this.open_List) {
            if (entry != null && entry.isValid()) {
               copies.add(entry.copy());
            }
         }

         return copies;
      } else {
         autismclient.util.macro.CraftAction.Inner_DataUtil draft = this.init_SyncManager_DataUt();
         return draft == null ? List.of() : List.of(draft);
      }
   }

   private autismclient.util.macro.CraftAction init_SyncManager() {
      List<autismclient.util.macro.CraftAction.Inner_DataUtil> activeEntries = this.load_List_2();
      if (activeEntries.isEmpty()) {
         return null;
      }

      autismclient.util.macro.CraftAction action = new autismclient.util.macro.CraftAction();
      action.init(activeEntries);
      return action;
   }

   private Component init_Component_SyncManager_DataUt(autismclient.util.macro.CraftAction.Inner_DataUtil entry) {
      if (entry != null && entry.isValid()) {
         AutoCraftingEngine.CraftOption option = this.init_J_TaskQueue_SyncManager_DataUt(entry);
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
      if (this.sync_Z && !this.update_Z) {
         boolean anyFocused = false;

         for (ThemedTextInputBox field : this.init_List) {
            if (field.isFocused()) {
               anyFocused = true;
               break;
            }
         }

         if (anyFocused) {
            if (keyCode == 256) {
               for (ThemedTextInputBox field : this.init_List) {
                  field.setFocused(false);
               }

               return true;
            }

            KeyEvent input = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);

            for (ThemedTextInputBox field : this.init_List) {
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
      if (this.sync_Z && !this.update_Z) {
         CharacterEvent input = new CharacterEvent(codepoint);

         for (ThemedTextInputBox field : this.init_List) {
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
      if (this.sync_Z || this.isClosing()) {
         this.stop_V_2();
         int rawMouseX = mouseX;
         int rawMouseY = mouseY;
         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.sync_List)) {
            mouseX = -2147483648;
            mouseY = -2147483648;
         }

         this.sync_List.clear();
         this.prev_V_2();
         if (!this.write_Z) {
            this.add = this.poll_Z_5() ? this.fill() : this.drain();
         }

         int panelHeight = this.add;
         OverlayBounds bounds = new OverlayBounds(this.get, this.set, this.put, panelHeight, this.sync_Z, this.update_Z);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, "Fabricator", this.update_Z, this.write_Z);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
         } else {
            try {
               int labelX = this.get + 4;
               int fieldX = this.run_I_5();
               int fieldWidth = this.get_I_5();
               ThemedTextHelper.init(context, init_Minecraft.font, "Action", ThemedTextHelper.TextTone.MUTED, labelX, this.init_I_I_4(this.reset_I_2()), false);
               if (this.init_SyncManager == null) {
                  this.init_SyncManager = new autismclient.gui.ui.widgets.DropdownWidget(
                     fieldX, this.reset_I_2(), fieldWidth, push_I_7(), this.load_List, this.next, idx -> {
                        if (idx >= 0 && idx < this.init_AutismFabricatorOvArr.length && idx != this.next) {
                           this.next = idx;
                           this.init_AutismFabricatorOv = this.init_AutismFabricatorOvArr[idx];
                           this.reset_V_2();
                           this.update_V_6();
                           this.read_V_3();
                        }
                     }
                  );
               }

               this.init_SyncManager
                  .setBounds(fieldX, this.reset_I_2(), fieldWidth, push_I_7())
                  .setOptions(this.load_List)
                  .setSelectedIndex(this.next);
               this.sync_List.add(this.init_SyncManager);

               for (ThemedTextInputBox field : this.init_List) {
                  field.render(context, mouseX, mouseY, delta);
               }

               if (this.pop_Z_3()) {
                  ThemedTextHelper.init(context, init_Minecraft.font, "Slot", ThemedTextHelper.TextTone.MUTED, labelX, this.init_I_I_4(this.start_I_5()), false);
                  ThemedTextHelper.init(context, init_Minecraft.font, "Item", ThemedTextHelper.TextTone.MUTED, labelX, this.init_I_I_4(this.stop_I_2()), false);
               }

               if (this.push_Z_4()) {
                  if (this.tick_Z_3() || this.run_Z_2()) {
                     ThemedTextHelper.init(
                        context,
                        init_Minecraft.font,
                        this.run_Z_2() ? "Drop Mode" : "Click",
                        ThemedTextHelper.TextTone.MUTED,
                        labelX,
                        this.init_I_I_4(this.next_I_2()),
                        false
                     );
                     if (this.tick_Z_3()) {
                        boolean packetMode = this.init_AutismFabricatorOv.write();
                        List<String> options = packetMode ? write_List : read_List;
                        int clamped = Math.min(this.prev, options.size() - 1);
                        if (this.load_SyncManager == null) {
                           this.load_SyncManager = new autismclient.gui.ui.widgets.DropdownWidget(
                              fieldX, this.next_I_2(), fieldWidth, push_I_7(), options, clamped, idx -> {
                                 if (idx >= 0 && idx < 3) {
                                    this.prev = idx;
                                    this.reset_V_2();
                                    this.read_V_3();
                                 }
                              }
                           );
                        }

                        this.load_SyncManager
                           .setBounds(fieldX, this.next_I_2(), fieldWidth, push_I_7())
                           .setOptions(options)
                           .setSelectedIndex(clamped);
                        this.sync_List.add(this.load_SyncManager);
                     } else {
                        List<String> options = update_List;
                        int sel = this.flush_Z ? 1 : 0;
                        if (this.read_SyncManager == null) {
                           this.read_SyncManager = new autismclient.gui.ui.widgets.DropdownWidget(fieldX, this.next_I_2(), fieldWidth, push_I_7(), options, sel, idx -> {
                              this.flush_Z = idx == 1;
                              this.reset_V_2();
                              this.read_V_3();
                           });
                        }

                        this.read_SyncManager
                           .setBounds(fieldX, this.next_I_2(), fieldWidth, push_I_7())
                           .setOptions(options)
                           .setSelectedIndex(sel);
                        this.sync_List.add(this.read_SyncManager);
                     }
                  }

                  ThemedTextHelper.init(context, init_Minecraft.font, this.read_Str_4(), ThemedTextHelper.TextTone.MUTED, labelX, this.init_I_I_4(this.prev_I()), false);
               } else {
                  this.sync_V_5();
                  int listX = this.flags();
                  int listWidth = this.level();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, init_Minecraft.font, "Craft Plan", labelX, this.queue());
                  if (!this.open_List.isEmpty()) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderActionButton(context, init_Minecraft.font, "Clear All", true, listX, listWidth, this.queue(), mouseX, mouseY);
                  }

                  int planListY = this.buffer();
                  int planContentWidth = this.frame();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, planListY, listWidth, this.push_I, this.data >= 0);
                  if (this.open_List.isEmpty()) {
                     autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(
                        context, init_Minecraft.font, "(no plan - pick recipe and Add)", listX, planListY + 2, planContentWidth
                     );
                  } else {
                     int maxPlanScrollPx = Math.max(0, this.open_List.size() * 13 - this.push_I);
                     this.value = Math.max(0, Math.min(this.value, Math.max(0, this.open_List.size() - 4)));
                     this.load_CacheEntry.setOffset(this.value * 13, maxPlanScrollPx);
                     int drawPlanScroll = this.load_CacheEntry.tickAndClamp(delta, maxPlanScrollPx);
                     autismclient.gui.ui.UiClipStack.getInstance()
                        .push(context, autismclient.gui.f.NetHelper.init(listX + 1, planListY + 1, Math.max(0, listWidth - 2), Math.max(0, this.push_I - 2)));
                     int startIndex = drawPlanScroll / 13;
                     int rowY = planListY - drawPlanScroll % 13;

                     for (int index = startIndex; index < this.open_List.size() && rowY < planListY + this.push_I; rowY += 13) {
                        boolean hovered = mouseX >= listX && mouseX < listX + planContentWidth && mouseY >= rowY && mouseY < rowY + 12;
                        boolean selected = index == this.data;
                        autismclient.gui.ui.widgets.ListRowRenderer.drawComponentRow(
                           context,
                           init_Minecraft.font,
                           this.init_Component_SyncManager_DataUt(this.open_List.get(index)),
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

                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftPlanScrollbar = this.init_PoolEntry_DataUtil();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, craftPlanScrollbar, craftPlanScrollbar.containsTrack(mouseX, mouseY), this.state == 1);
                  if (this.init_ItemStack.isEmpty()) {
                     ThemedTextHelper.init(context, init_Minecraft.font, "Recipe: none selected", ThemedTextHelper.TextTone.MUTED, listX, this.entry(), false);
                  } else {
                     Component summaryText = Component.empty()
                        .append(this.init_ItemStack.getHoverName().copy())
                        .append(
                           Component.literal(
                              " x"
                                 + this.init_ItemStack.getCount()
                                 + (this.init_J_TaskQueue != null && this.init_J_TaskQueue.craftable ? " | x" + this.init_J_TaskQueue.maxCrafts : "")
                           )
                        );
                     ThemedTextHelper.init(context, init_Minecraft.font, summaryText.getString(), UiColorPalette.textMuted(), listX, this.entry(), false);
                  }

                  ThemedTextHelper.init(context, init_Minecraft.font, "Amount", ThemedTextHelper.TextTone.MUTED, labelX, this.init_I_I_4(this.size()), false);
                  this.init(context, this.cache(), this.size(), this.data(), push_I_7(), "Use Max", this.push_Z, "fabricator:use-max", mouseX, mouseY);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawHeaderTitle(context, init_Minecraft.font, "Recipes", listX, this.node());
                  int listY = this.count();
                  int recipeContentWidth = this.block();
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, listY, listWidth, this.open_I, this.init_J_TaskQueue != null);
                  autismclient.gui.ui.UiClipStack.getInstance()
                     .push(context, autismclient.gui.f.NetHelper.init(listX + 1, listY + 1, Math.max(0, listWidth - 2), Math.max(0, this.open_I - 2)));
                  if (this.close_List.isEmpty()) {
                     String emptyText;
                     if (init_Minecraft.player == null || init_Minecraft.level == null) {
                        emptyText = "Join a world to load crafting recipes.";
                     } else if (this.flush_List.isEmpty() && this.close_Z) {
                        emptyText = "Scanning crafting recipes...";
                     } else {
                        emptyText = "No crafting recipes match this search.";
                     }

                     autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, init_Minecraft.font, emptyText, listX, listY + 4, recipeContentWidth);
                  } else {
                     int maxRecipeScrollPx = Math.max(0, this.close_List.size() * this.close_I - this.open_I);
                     this.size = Math.max(0, Math.min(this.size, Math.max(0, this.close_List.size() - 6)));
                     this.init_CacheEntry.setOffset(this.size * this.close_I, maxRecipeScrollPx);
                     int drawRecipeScroll = this.init_CacheEntry.tickAndClamp(delta, maxRecipeScrollPx);
                     int startIndex = drawRecipeScroll / this.close_I;
                     int rowY = listY - drawRecipeScroll % this.close_I;

                     for (int index = startIndex; index < this.close_List.size() && rowY < listY + this.open_I; rowY += this.close_I) {
                        AutoCraftingEngine.CraftOption option = this.close_List.get(index);
                        boolean hovered = mouseX >= listX && mouseX <= listX + recipeContentWidth && mouseY >= rowY && mouseY < rowY + this.close_I;
                        boolean selected = this.init_J_TaskQueue != null && option.recipeKey.equalsIgnoreCase(this.init_J_TaskQueue.recipeKey);
                        Component richLabel = Component.empty()
                           .append(option.outputStack.getHoverName().copy())
                           .append(Component.literal(" x" + option.outputStack.getCount() + " | " + AutoCraftingEngine.formatStatusLabel(option)));
                        autismclient.gui.ui.widgets.ListRowRenderer.drawComponentRow(
                           context,
                           init_Minecraft.font,
                           richLabel,
                           listX + 2,
                           rowY,
                           recipeContentWidth - 2,
                           this.close_I,
                           hovered,
                           selected,
                           option.craftable ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.READY : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.MISSING,
                           true
                        );
                        autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, listX + 2, rowY + this.close_I - 1, recipeContentWidth - 2);
                        index++;
                     }
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(context);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics craftListScrollbar = this.load_PoolEntry_DataUtil();
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, craftListScrollbar, craftListScrollbar.containsTrack(mouseX, mouseY), this.state == 2);
                  String recipeCountLine = ThemedTextHelper.init(
                     init_Minecraft.font, this.valid + " ready / " + this.flush_List.size() + " total", listWidth, ThemedTextHelper.TextTone.MUTED
                  );
                  ThemedTextHelper.init(context, init_Minecraft.font, recipeCountLine, ThemedTextHelper.TextTone.MUTED, listX, listY + this.open_I + 2, false);
               }

               int btnY = this.set_I_5();
               int btnArea = this.put - 8;
               int gap = 2;
               int bw = (btnArea - gap * 2) / 3;
               int bx = this.get + 4;
               String sendLabel = this.poll_Z_5() ? (this.poll_Z ? "Crafting..." : "Craft") : "Send";
               this.init(context, bx, btnY, bw, this.reset, sendLabel, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               bx += bw + gap;
               this.init(context, bx, btnY, bw, this.reset, "Queue", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               bx += bw + gap;
               this.init(context, bx, btnY, bw, this.reset, "Macro", autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, true, mouseX, mouseY);
               int infoWidth = this.put - 8;
               int statusY = this.put_I_2();
               if (statusY + 9 < btnY) {
                  if (!this.init_Str_2.isEmpty()) {
                     int color = init_I_ChatFormatting(this.init_ChatFormatting);
                     autismclient.gui.f.b.k.init(
                        context,
                        init_Minecraft.font,
                        this.init_Str_2,
                        init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                        color,
                        this.get + 4,
                        statusY,
                        infoWidth,
                        true
                     );
                  } else {
                     autismclient.gui.f.b.k.init(
                        context,
                        init_Minecraft.font,
                        this.write_Str_2(),
                        init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                        init_CharEncoder.load(autismclient.gui.f.b.l.write),
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

                  ThemedTextHelper.init(context, init_Minecraft.font, actionLines.get(i), ThemedTextHelper.TextTone.MUTED, this.get + 4, lineY, false);
               }

               if (!this.sync_List.isEmpty()) {
                  autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(context, init_Minecraft.font, this.sync_List, rawMouseX, rawMouseY);
               }
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
            }

            if (!this.sync_List.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.sync_List)) {
               context.nextStratum();
               autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(context, init_Minecraft.font, this.sync_List, rawMouseX, rawMouseY);
            }
         }
      }
   }

   private static int init_I_ChatFormatting(ChatFormatting formatting) {
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

   private void put_V_2() {
      this.next = (this.next + 1) % this.init_AutismFabricatorOvArr.length;
      this.init_AutismFabricatorOv = this.init_AutismFabricatorOvArr[this.next];
      this.reset_V_2();
      this.update_V_6();
      this.read_V_3();
   }

   private void add_V_3() {
      this.next = (this.next - 1 + this.init_AutismFabricatorOvArr.length) % this.init_AutismFabricatorOvArr.length;
      this.init_AutismFabricatorOv = this.init_AutismFabricatorOvArr[this.next];
      this.reset_V_2();
      this.update_V_6();
      this.read_V_3();
   }

   private void clear_V_2() {
      this.init_List.clear();
      this.write_k = null;
   }

   public void init_V_Slot_I(Slot slot, int button) {
      if (slot != null && !this.poll_Z_5()) {
         int stableSlotId = InventorySlotHelper.toVisibleSlot(init_Minecraft, slot.index);
         autismclient.util.macro.MacroItemTarget clickedTarget = this.init_ArrayUtil_I(slot.index);
         String itemName = clickedTarget.rawLabel();
         if (this.init_AutismFabricatorOv.write()) {
            AbstractContainerMenu handler = this.init_AbstractContainerM();
            if (handler == null) {
               this.init_V_Str_ChatFormatting("No container is open to capture", ChatFormatting.RED);
            } else {
               String screenTitle = this.init_AbstractContainerS != null ? this.init_AbstractContainerS.getTitle().getString() : "";
               String menuClass = handler.getClass().getName();
               this.init_bo_StreamUtil = new ContainerClickSnapshot.Inner_StreamUtil(
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
               this.init_V_Str_ChatFormatting("Captured Packet " + this.init_bo_StreamUtil.init_Str(), ChatFormatting.GREEN);
               this.read_V_3();
            }
         } else {
            this.init_Integer = stableSlotId;
            this.load_Str = String.valueOf(stableSlotId);
            if (this.init_k != null) {
               this.init_k.setValue(this.load_Str);
            }

            if (!itemName.isEmpty()) {
               this.init_ArrayUtil = clickedTarget;
               this.read_Str = itemName;
               if (this.load_k != null) {
                  this.load_k.setValue(itemName);
               }

               this.init_V_Str_ChatFormatting("Selected slot " + stableSlotId + ": " + itemName, ChatFormatting.GREEN);
            } else {
               this.init_ArrayUtil = new autismclient.util.macro.MacroItemTarget();
               this.read_Str = "";
               if (this.load_k != null) {
                  this.load_k.setValue("");
               }

               this.init_V_Str_ChatFormatting("Selected slot " + stableSlotId + " (Empty)", ChatFormatting.YELLOW);
            }

            this.read_V_3();
         }
      }
   }

   private void init_V_AutismMacroEditorO(AutismMacroEditorOverlay macroEditor) {
      try {
         if (this.poll_Z_5()) {
            autismclient.util.macro.CraftAction craftAction = this.init_SyncManager();
            if (craftAction != null && craftAction.hasValidEntry()) {
               macroEditor.init_V_MacroAction_2(craftAction);
               return;
            }

            this.init_V_Str_ChatFormatting("Select a recipe or add a craft entry first!", ChatFormatting.RED);
            return;
         }

         Integer enteredSlot = this.init_Integer();
         String itemName = this.update_Str_2();
         int repeats = this.read(-1);
         if (repeats <= 0) {
            return;
         }

         if (enteredSlot == null && itemName.isEmpty() && !this.init_AutismFabricatorOv.write()) {
            this.init_V_Str_ChatFormatting("Pick a slot or enter an item name first!", ChatFormatting.RED);
            return;
         }

         if (this.init_AutismFabricatorOv.write()) {
            ContainerClickSnapshot.Inner_StreamUtil target = this.init_bo_StreamUtil();
            if (target == null) {
               this.init_V_Str_ChatFormatting("Right-click a GUI slot to capture a Packet target first!", ChatFormatting.RED);
               return;
            }

            autismclient.util.macro.PacketClickAction action = new autismclient.util.macro.PacketClickAction(target, repeats, false);
            macroEditor.init_V_MacroAction_2(action);
            this.init_V_Str_ChatFormatting("Added Packet Click action to macro!", ChatFormatting.GREEN);
            return;
         }

         AbstractContainerMenu handler = this.init_AbstractContainerM();
         int resolvedSlot = enteredSlot == null ? -1 : InventorySlotHelper.resolveConfiguredSlot(init_Minecraft, enteredSlot);
         if (enteredSlot != null && !this.init_Z_AbstractContainerM_I(handler, resolvedSlot)) {
            this.init_V_Str_ChatFormatting("Slot " + enteredSlot + " is not available in this screen!", ChatFormatting.RED);
            return;
         }

         if (this.init_AutismFabricatorOv == AutismFabricatorOverlay.Inner_DataUtil.DROP) {
            autismclient.util.macro.DropAction dropAction = new autismclient.util.macro.DropAction();
            dropAction.useHandlerSlots = true;
            dropAction.mode = this.flush_Z ? autismclient.util.macro.DropAction.Inner_DataUtil.ALL : autismclient.util.macro.DropAction.Inner_DataUtil.TIMES;
            dropAction.dropCount = this.flush_Z ? 0 : repeats;
            String entry = enteredSlot != null ? "#" + enteredSlot + (!itemName.isEmpty() ? "|" + itemName : "") : itemName;
            if (!entry.isBlank()) {
               autismclient.util.macro.MacroItemTarget target = this.init_ArrayUtil_Integer_Str(enteredSlot, itemName);
               entry = target.toSpecString();
               dropAction.itemTargets.add(target);
               dropAction.itemNames.add(entry);
               dropAction.itemCounts.add(this.flush_Z ? 0 : repeats);
            }

            macroEditor.init_V_MacroAction_2(dropAction);
            this.init_V_Str_ChatFormatting("Added Drop action to macro!", ChatFormatting.GREEN);
            return;
         }

         autismclient.util.macro.ItemAction itemAction = new autismclient.util.macro.ItemAction();
         itemAction.actionIndex = this.init_AutismFabricatorOv.init_N(this.flush_Z).ordinal();
         itemAction.button = this.pool();
         itemAction.times = repeats;
         if (enteredSlot != null) {
            itemAction.useSlot = true;
            itemAction.targetSlot = enteredSlot;
         }

         if (!itemName.isEmpty()) {
            autismclient.util.macro.MacroItemTarget target = this.init_ArrayUtil_Integer_Str(enteredSlot, itemName);
            itemAction.itemTargets.add(target);
            itemAction.itemNames.add(target.toSpecString());
            itemAction.itemTimes.add(repeats);
            itemAction.itemActionIdx.add(itemAction.actionIndex);
            itemAction.itemButtons.add(itemAction.button);
         }

         macroEditor.init_V_MacroAction_2(itemAction);
         this.init_V_Str_ChatFormatting("Added " + this.init_AutismFabricatorOv.init_Str + " action to macro!", ChatFormatting.GREEN);
      } catch (NumberFormatException e) {
         this.init_V_Str_ChatFormatting("Invalid value", ChatFormatting.RED);
      }
   }

   private autismclient.util.macro.MacroItemTarget init_ArrayUtil_Integer_Str(Integer enteredSlot, String itemName) {
      String safeName = itemName == null ? "" : itemName.trim();
      autismclient.util.macro.MacroItemTarget target = this.init_ArrayUtil != null && safeName.equals(this.init_ArrayUtil.rawLabel())
         ? this.init_ArrayUtil.copy()
         : autismclient.util.macro.MacroItemTarget.parse(safeName);
      if (enteredSlot != null) {
         target.slot = enteredSlot;
      }

      return target;
   }

   private ContainerClickSnapshot.Inner_StreamUtil init_bo_StreamUtil() {
      return this.init_bo_StreamUtil == null ? null : this.init_bo_StreamUtil.withMode(this.init_bo_DataUtil_2());
   }

   private Packet<?> init_Packet() {
      ContainerClickSnapshot.Inner_StreamUtil target = this.init_bo_StreamUtil();
      return target == null ? null : target.toPacket();
   }

   private Packet<?> init_Packet_I(int slot) {
      if (this.init_AutismFabricatorOv.write()) {
         return this.init_Packet();
      }

      if (init_Minecraft.player == null) {
         return null;
      }

      AbstractContainerMenu handler = init_Minecraft.player.containerMenu;
      if (handler == null) {
         return null;
      }

      ContainerInput actionType = this.init_AutismFabricatorOv.init_N(this.flush_Z).init_ContainerInput();
      return new ServerboundContainerClickPacket(
         handler.containerId, handler.getStateId(), (short)slot, (byte)this.pool(), actionType, new Int2ObjectArrayMap<>(), HashedStack.EMPTY
      );
   }

   private void load(boolean queue) {
      if (this.poll_Z_5()) {
         this.flush(queue);
      } else if (this.init_AutismFabricatorOv.write()) {
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

            ClientPacketListener handler = init_Minecraft.getConnection();
            if (handler == null) {
               return;
            }

            if (this.init_AutismFabricatorOv == AutismFabricatorOverlay.Inner_DataUtil.DROP && !queue) {
               int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);
               int sent = InventoryDropHelper.init(init_Minecraft, slot, this.flush_Z ? 0 : repeats);
               if (sent <= 0) {
                  this.init_V_Str_ChatFormatting("Nothing to drop", ChatFormatting.YELLOW);
               } else if (queuedCount > 0) {
                  this.init_V_Str_ChatFormatting("Sent " + queuedCount + " queued + drop action", ChatFormatting.GREEN);
               } else {
                  this.init_V_Str_ChatFormatting(this.flush_Z ? "Dropped stack" : "Dropped " + repeats + " item(s)", ChatFormatting.GREEN);
               }

               return;
            }

            if (!queue) {
               AutismSessionState.getInstance().flushDelayedPackets(handler);
               ContainerInput actionType = this.init_AutismFabricatorOv.init_N(this.flush_Z).init_ContainerInput();
               int effectiveButton = this.pool();

               for (int i = 0; i < repeats; i++) {
                  ContainerClickExecutor.init(init_Minecraft, slot, effectiveButton, actionType);
               }

               return;
            }

            this.init(slot, repeats, queue, handler);
         } catch (NumberFormatException e) {
            this.init_V_Str_ChatFormatting("Invalid number format", ChatFormatting.RED);
         }
      }
   }

   private void read(boolean queue) {
      try {
         ContainerClickSnapshot.Inner_StreamUtil target = this.init_bo_StreamUtil();
         if (target == null) {
            this.init_V_Str_ChatFormatting("Right-click a GUI slot to capture a Packet target first", ChatFormatting.RED);
            return;
         }

         int repeats = this.read(-1);
         if (repeats <= 0) {
            return;
         }

         ClientPacketListener handler = init_Minecraft.getConnection();
         if (handler == null) {
            this.init_V_Str_ChatFormatting("No network connection", ChatFormatting.RED);
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

            this.init_V_Str_ChatFormatting("Queued " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
            return;
         }

         int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);

         for (Packet<?> packet : packets) {
            AutismSessionState.getInstance().sendPacketBypassingInterception(handler, packet);
         }

         if (queuedCount > 0) {
            this.init_V_Str_ChatFormatting("Sent " + queuedCount + " queued + " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
         } else {
            this.init_V_Str_ChatFormatting("Sent " + packets.size() + " exact Packet Click(s)", ChatFormatting.GREEN);
         }
      } catch (NumberFormatException e) {
         this.init_V_Str_ChatFormatting("Invalid number format", ChatFormatting.RED);
      }
   }

   private void flush(boolean queue) {
      List<autismclient.util.macro.CraftAction.Inner_DataUtil> entries = this.load_List_2();
      if (entries.isEmpty()) {
         this.init_V_Str_ChatFormatting("Select a recipe or add a craft entry first", ChatFormatting.RED);
      } else if (!queue) {
         if (this.poll_Z) {
            this.init_V_Str_ChatFormatting("A craft is already running", ChatFormatting.YELLOW);
         } else {
            this.poll_Z = true;
            Thread craftThread = new Thread(() -> {
               AutoCraftingEngine.CraftResult result = null;

               for (autismclient.util.macro.CraftAction.Inner_DataUtil entryx : entries) {
                  AutoCraftingEngine.CraftOption optionx = AutoCraftingEngine.findOptionByKey(init_Minecraft, entryx.recipeKey);
                  if (optionx == null) {
                     optionx = AutoCraftingEngine.findOptionById(init_Minecraft, entryx.recipeId);
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

                  result = AutoCraftingEngine.craft(init_Minecraft, entryx.recipeKey, entryx.recipeId, desiredAmountx);
                  if (!result.success) {
                     break;
                  }
               }

               this.poll_Z = false;
               AutoCraftingEngine.CraftResult finalResult = result;
               if (finalResult != null && !finalResult.success) {
                  init_Minecraft.execute(() -> {
                     this.init_V_Str_ChatFormatting(finalResult.message, ChatFormatting.RED);
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
            AutoCraftingEngine.CraftOption option = AutoCraftingEngine.findOptionByKey(init_Minecraft, entry.recipeKey);
            if (option == null) {
               option = AutoCraftingEngine.findOptionById(init_Minecraft, entry.recipeId);
            }

            if (option == null) {
               String msg = "Recipe not found: " + entry.resultName;
               this.init_V_Str_ChatFormatting(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            int desiredAmount = AutoCraftingEngine.resolveTargetAmount(option, entry.amount, entry.useMaxAmount);
            if (desiredAmount <= 0) {
               String msg = "No space or materials for " + entry.resultName;
               this.init_V_Str_ChatFormatting(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            List<Packet<?>> packets = AutoCraftingEngine.buildCraftPackets(init_Minecraft, entry.recipeKey, entry.recipeId, desiredAmount);
            if (packets.isEmpty()) {
               String msg = option.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3
                     && (init_Minecraft.player == null || !(init_Minecraft.player.containerMenu instanceof CraftingMenu))
                  ? "Open a crafting table to queue " + entry.resultName + "."
                  : "Could not plan " + entry.resultName + " from this inventory.";
               this.init_V_Str_ChatFormatting(msg, ChatFormatting.RED);
               AutismClientMessaging.sendPrefixed("§c" + msg);
               return;
            }

            for (Packet<?> packet : packets) {
               AutismSessionState.getInstance().enqueueDelayedPacket(packet);
            }

            queuedClicks += packets.size();
         }

         this.init_V_Str_ChatFormatting("Queued " + queuedClicks + " packet(s) for " + entries.size() + " craft(s)", ChatFormatting.GREEN);
      }
   }

   private void init(int slot, int repeats, boolean queue, ClientPacketListener handler) {
      List<Packet<?>> packets = this.init_List_I_I(slot, repeats);
      if (packets.isEmpty()) {
         this.init_V_Str_ChatFormatting("Failed to build packet", ChatFormatting.RED);
      } else if (queue) {
         for (Packet<?> packet : packets) {
            AutismSessionState.getInstance().enqueueDelayedPacket(packet);
         }

         this.init_V_Str_ChatFormatting("Queued " + packets.size() + " packet(s)", ChatFormatting.GREEN);
      } else {
         int queuedCount = AutismSessionState.getInstance().flushDelayedPackets(handler);

         for (Packet<?> packet : packets) {
            DirectPacketSender.init(packet);
         }

         if (queuedCount > 0) {
            this.init_V_Str_ChatFormatting("Sent " + queuedCount + " queued + " + packets.size() + " new packet(s)", ChatFormatting.GREEN);
         } else {
            this.init_V_Str_ChatFormatting("Sent " + packets.size() + " packet(s)", ChatFormatting.GREEN);
         }
      }
   }

   private void init_V_Str_ChatFormatting(String message, ChatFormatting color) {
      this.init_Str_2 = message;
      this.init_ChatFormatting = color;
      this.init_J = 0L;
   }

   private void reset_V_2() {
      this.init_Str_2 = "";
      this.init_ChatFormatting = ChatFormatting.GRAY;
      this.init_J = 0L;
   }

   private void stop_V_2() {
      if (this.init_J > 0L && System.currentTimeMillis() >= this.init_J) {
         this.reset_V_2();
      }
   }

   private String read_Str_4() {
      if (this.poll_Z_5()) {
         return "Amount";
      } else {
         return this.run_Z_2() && !this.flush_Z ? "Items" : "Times";
      }
   }

   private String write_Str_2() {
      if (this.poll_Z_5()) {
         String planInfo = this.open_List.isEmpty() ? "Plan: none" : "Plan: " + this.open_List.size() + " entr" + (this.open_List.size() == 1 ? "y" : "ies");
         if (this.init_ItemStack.isEmpty()) {
            return planInfo + " | Recipe: none selected";
         }

         String source = this.init_J_TaskQueue != null && this.init_J_TaskQueue.gridSource == AutoCraftingEngine.GridSource.TABLE_3X3 ? "Table" : "2x2";
         String availability = this.init_J_TaskQueue != null ? AutoCraftingEngine.formatStatusLabel(this.init_J_TaskQueue) : "Unavailable";
         String amountMode = this.push_Z ? "Max" : "x" + Math.max(1, this.store());
         return planInfo + " | Recipe: " + this.init_ItemStack.getHoverName().getString() + " | " + source + " | " + availability + " | " + amountMode;
      } else if (this.init_AutismFabricatorOv.write()) {
         ContainerClickSnapshot.Inner_StreamUtil target = this.init_bo_StreamUtil();
         return target == null ? "Packet: right-click a GUI slot to capture" : "Packet: " + target.init_Str();
      } else {
         Integer slot = this.init_Integer();
         String itemName = this.update_Str_2();
         if (slot != null && !itemName.isEmpty()) {
            return "Target: '" + itemName + "' in exact slot " + slot;
         } else if (slot != null) {
            return "Target: exact slot " + slot;
         } else {
            return !itemName.isEmpty() ? "Target: best matching '" + itemName + "'" : "Target: choose a slot or enter an item name";
         }
      }
   }

   private AbstractContainerMenu init_AbstractContainerM() {
      if (init_Minecraft.player != null && init_Minecraft.player.containerMenu != null) {
         return init_Minecraft.player.containerMenu;
      } else {
         return this.init_AbstractContainerS != null ? ((AbstractContainerScreenAccessor)this.init_AbstractContainerS).autism$getMenu() : null;
      }
   }

   private Integer init_Integer() {
      if (this.init_k == null) {
         return null;
      }

      String text = this.init_k.getValue().trim();
      if (text.isEmpty()) {
         return null;
      }

      try {
         return Integer.parseInt(text);
      } catch (NumberFormatException ignored) {
         return null;
      }
   }

   private String update_Str_2() {
      return this.load_k == null ? "" : this.load_k.getValue().trim();
   }

   private boolean init_Z_AbstractContainerM_I(AbstractContainerMenu handler, int slotId) {
      return handler != null && slotId >= 0 && slotId < handler.slots.size();
   }

   private boolean init_Z_ItemStack_Str(ItemStack stack, String query) {
      return stack != null && !stack.isEmpty() && query != null && !query.isBlank()
         ? autismclient.util.macro.MacroItemTarget.parse(query).matchScore(stack, -1) >= 0
         : false;
   }

   private boolean init_Z_AbstractContainerM(AbstractContainerMenu handler, int slotId, String itemQuery) {
      if (!this.init_Z_AbstractContainerM_I(handler, slotId)) {
         return false;
      }

      Slot slot = handler.slots.get(slotId);
      return slot != null && this.init_Z_ItemStack_Str(slot.getItem(), itemQuery);
   }

   private int chunk() {
      AbstractContainerMenu handler = this.init_AbstractContainerM();
      if (handler == null) {
         this.init_V_Str_ChatFormatting("No screen is open right now", ChatFormatting.RED);
         return -1;
      }

      Integer enteredSlot = this.init_Integer();
      String itemName = this.update_Str_2();
      int resolvedSlot = enteredSlot == null ? -1 : InventorySlotHelper.resolveConfiguredSlot(init_Minecraft, enteredSlot);
      if (enteredSlot != null && !this.init_Z_AbstractContainerM_I(handler, resolvedSlot)) {
         this.init_V_Str_ChatFormatting("Slot " + enteredSlot + " is not available in this screen", ChatFormatting.RED);
         return -1;
      }

      if (enteredSlot != null && !itemName.isEmpty()) {
         if (this.init_Z_AbstractContainerM(handler, resolvedSlot, itemName)) {
            return resolvedSlot;
         }

         this.init_V_Str_ChatFormatting("Slot " + enteredSlot + " does not contain '" + itemName + "'", ChatFormatting.RED);
         return -1;
      } else {
         if (enteredSlot != null) {
            return resolvedSlot;
         }

         if (!itemName.isEmpty()) {
            Integer foundSlot = this.init_Integer_Str(itemName);
            if (foundSlot == null) {
               this.init_V_Str_ChatFormatting("No item matching '" + itemName + "' found", ChatFormatting.RED);
               return -1;
            } else {
               return foundSlot;
            }
         } else {
            this.init_V_Str_ChatFormatting("Pick a slot or enter an item name first", ChatFormatting.RED);
            return -1;
         }
      }
   }

   private int read(int slot) {
      int value;
      try {
         value = Integer.parseInt(this.read_k.getValue().trim());
      } catch (NumberFormatException e) {
         this.init_V_Str_ChatFormatting(this.init_AutismFabricatorOv.read() ? "Invalid amount" : "Invalid repeat count", ChatFormatting.RED);
         return -1;
      }

      if (value <= 0) {
         this.init_V_Str_ChatFormatting(
            this.init_AutismFabricatorOv.read() ? "Amount must be at least 1" : "Repeat count must be at least 1", ChatFormatting.RED
         );
         return -1;
      }

      if (!this.init_AutismFabricatorOv.read()) {
         return value;
      }

      if (this.init_J_TaskQueue != null && !this.init_ItemStack.isEmpty()) {
         if (init_Minecraft.player != null && init_Minecraft.player.containerMenu instanceof CraftingMenu craftingHandler) {
            int var7 = Math.max(1, this.init_ItemStack.getCount());
            return Math.max(1, (int)Math.ceil((double)value / var7));
         } else {
            this.init_V_Str_ChatFormatting("Open a crafting screen first", ChatFormatting.RED);
            return -1;
         }
      } else {
         this.init_V_Str_ChatFormatting("Select a recipe first", ChatFormatting.RED);
         return -1;
      }
   }

   private int table() {
      if (this.init_J_TaskQueue == null || this.init_ItemStack.isEmpty()) {
         this.init_V_Str_ChatFormatting("Select a recipe first", ChatFormatting.RED);
         return -1;
      }

      if (this.push_Z) {
         int maxAmount = this.init_J_TaskQueue.maxOutputItems;
         if (maxAmount <= 0) {
            this.init_V_Str_ChatFormatting(this.init_J_TaskQueue.hasMaterials ? "No room to store crafted items" : "Missing materials", ChatFormatting.RED);
            return -1;
         } else {
            return maxAmount;
         }
      } else {
         int value = this.store();
         if (value <= 0) {
            this.init_V_Str_ChatFormatting("Invalid craft amount", ChatFormatting.RED);
            return -1;
         } else {
            return value;
         }
      }
   }

   private int store() {
      if (this.read_k == null) {
         return 1;
      }

      try {
         int value = Integer.parseInt(this.read_k.getValue().trim());
         return Math.max(1, value);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   private List<Packet<?>> init_List_I_I(int slot, int repeats) {
      List<Packet<?>> packets = new ArrayList<>();

      for (int i = 0; i < repeats; i++) {
         Packet<?> built = this.init_Packet_I(slot);
         if (built == null) {
            return List.of();
         }

         packets.add(this.init_Packet_Packet(built));
      }

      return packets;
   }

   private Packet<?> init_Packet_Packet(Packet<?> packet) {
      if (this.init_AutismFabricatorOv.write()) {
         return packet;
      } else {
         return packet instanceof ServerboundContainerClickPacket clickSlotPacket ? PacketRegenerator.init(clickSlotPacket) : packet;
      }
   }

   private Integer init_Integer_Str(String searchName) {
      if (searchName != null && !searchName.trim().isEmpty()) {
         AbstractContainerMenu handler = this.init_AbstractContainerM();
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

   private void next_V_2() {
      this.flush_I = 204;
      this.close_I = 12;
      this.open_I = 6 * this.close_I;
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

   private void prev_V_2() {
      int listHeight = Math.max(this.close_I * 2, Math.min(6 * this.close_I, this.close_List.size() * this.close_I));
      int planHeight = Math.max(26, Math.min(52, this.open_List.size() * 13));
      if (listHeight != this.open_I || planHeight != this.push_I) {
         this.open_I = listHeight;
         this.push_I = planHeight;
         if (this.poll_Z_5()) {
            if (this.read_k != null) {
               this.read_k.setY(this.size());
            }

            if (this.write_k != null) {
               this.write_k.setY(this.valid());
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
         ContainerClickType init_N(boolean dropWholeStack) {
            return ContainerClickType.PICKUP;
         }
      },
      QUICK_MOVE("Quick Move", "Shift-click the resolved slot.") {
         @Override
         ContainerClickType init_N(boolean dropWholeStack) {
            return ContainerClickType.QUICK_MOVE;
         }
      },
      PICKUP_ALL("Pick Up All", "Double-click the resolved slot. Usually needs you to hold a matching stack first.") {
         @Override
         ContainerClickType init_N(boolean dropWholeStack) {
            return ContainerClickType.PICKUP_ALL;
         }
      },
      CRAFT_RESULT("Craft", "Choose a craftable item, set the total amount, then send, queue, or add it to a macro.") {
         @Override
         boolean read() {
            return true;
         }

         @Override
         ContainerClickType init_N(boolean dropWholeStack) {
            return ContainerClickType.QUICK_MOVE;
         }
      },
      DROP("Drop", "Drop from the resolved slot. If slot and item are both set, both must match.") {
         @Override
         boolean load() {
            return true;
         }

         @Override
         ContainerClickType init_N(boolean dropWholeStack) {
            return dropWholeStack ? ContainerClickType.DROP_STACK : ContainerClickType.DROP_ITEM;
         }

         @Override
         String init_Str(boolean dropWholeStack) {
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
         ContainerClickType init_N(boolean dropWholeStack) {
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

      String init_Str(boolean dropWholeStack) {
         return this.load_Str;
      }

      abstract ContainerClickType init_N(boolean var1);
   }

   private static final class Inner_StreamUtil {
      final long init_J;
      final Object init_Obj;
      final List<AutoCraftingEngine.CraftOption> init_List;

      Inner_StreamUtil(long generation, Object connection, List<AutoCraftingEngine.CraftOption> options) {
         this.init_J = generation;
         this.init_Obj = connection;
         this.init_List = options;
      }
   }
}
