package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import autismclient.util.AutismConfig;
import autismclient.util.ck;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;

public final class AutoArmorModule extends Module {
   private static final EquipmentSlot[] init_EquipmentSlotArr = autismclient.util.TaskTimer.init_EquipmentSlotArr;
   private static final Map<EquipmentSlot, Integer> init_Map = new EnumMap<>(EquipmentSlot.class);
   private int init_I;
   private int load_I = -1;
   private boolean init_Z;
   private static final int read_I = 4;
   private int write = -4;
   private int update = -1;
   private final Map<String, String> load_Map = new HashMap<>();
   private final Map<String, ValueRange> read_Map = new HashMap<>();
   private boolean load_Z;
   private boolean read_Z;
   private int sync;
   private int flush;
   private int close;
   private int open = -1;
   private static volatile int poll = -2147483648;
   private static volatile int push = -2147483648;

   private ValueRange init_ValueRange(String settingId, int fallbackMin, int fallbackMax) {
      String raw = this.value(settingId);
      String previous = this.load_Map.put(settingId, raw);
      if (previous == null || !previous.equals(raw)) {
         this.read_Map.put(settingId, ValueRange.parse(raw, new ValueRange(fallbackMin, fallbackMax)).clamp(0.0, 10.0));
      }

      return this.read_Map.get(settingId);
   }

   AutoArmorModule() {
      super("auto-armor", "AutoArmor", ae.init_ae, "Equips your best armor.");
      this.add(new BoolSetting("prefer-elytra", "Prefer Elytra", false).description("Wear elytra over chestplate.").build());
      this.add(new BoolSetting("allow-cursed", "Allow Cursed", false).description("Equip curse-of-binding armor.").build());
      this.add(
         new RangeSetting("click-delay", "Click Delay", new ValueRange(3, 5), 0.0, 10.0, 1.0).group("Timing").unit("ticks").description("Ticks between moves")
      );
      this.add(
         new RangeSetting("close-delay", "Close Delay", new ValueRange(3, 5), 0.0, 10.0, 1.0)
            .group("Timing")
            .unit("ticks")
            .description("Ticks before session close")
      );
      this.add(
         new RangeSetting("operation-delay", "Spacing", new ValueRange(3, 5), 0.0, 10.0, 1.0)
            .group("Timing")
            .unit("ticks")
            .description("Ticks between sessions")
      );
      this.add(
         new RangeSetting("reaction", "Reaction", new ValueRange(3, 7), 0.0, 10.0, 1.0).group("Timing").unit("ticks").description("Ticks before first move")
      );
      this.add(new BoolSetting("no-movement", "No Movement", true).group("Constraints").description("Only swap while still.").build());
      this.add(new BoolSetting("not-using-item", "Not Using Item", true).group("Constraints").description("Not while using item.").build());
      this.add(new BoolSetting("hotbar", "Hotbar", true).group("Hotbar").description("Equip via hotbar swap.").build());
      this.add(
         new BoolSetting("can-swap-armor", "Can Swap Armor", false)
            .group("Hotbar")
            .visibleWhen(() -> this.bool("hotbar"))
            .description("Direct armor swap.")
            .build()
      );
      this.add(new BoolSetting("save-armor", "Save Armor", false).group("Save Armor").description("Swap before armor breaks.").build());
      this.add(
         new IntSetting("durability-threshold", "Durability Threshold", 24, 0, 100, 1)
            .group("Save Armor")
            .visibleWhen(() -> this.bool("save-armor"))
            .description("Save below this durability.")
            .build()
      );
      this.add(
         new BoolSetting("auto-open-inventory", "Auto Open Inventory", true)
            .group("Save Armor")
            .visibleWhen(() -> this.bool("save-armor"))
            .description("Open inventory to save.")
            .build()
      );
      this.add(new BoolSetting("pause-movement", "Pause Movement", true));
      boolean firstInstall = !AutismConfig.getGlobal().modules.containsKey("auto-armor");
      if (firstInstall) {
         this.setEnabledSilently(true);
      }
   }

   @Override
   public void onDisable() {
      this.init_V_18();
      this.close();
   }

   @Override
   public void onGameLeft() {
      this.init_V_18();
      this.init_Z = false;
   }

   private void init_V_18() {
      if (this.load_Z && this.sync > 0) {
         this.update_V_8();
      }

      this.init_I = 0;
      this.load_I = -1;
      this.load_Z = false;
      this.read_Z = false;
      this.sync = 0;
      this.flush = 0;
      this.close = 0;
      this.open = -1;
      poll = -2147483648;
      push = -2147483648;
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null && !aD.read_Z() && !MC.player.isSpectator()) {
         this.sync_V_4();
         if (this.load_Z) {
            if (AutoTotemModule.init_Z_6()) {
               this.init(true);
            } else {
               this.read_V_9();
            }
         } else if (!AutoTotemModule.init_Z_6()) {
            if (!this.update_Z_4()) {
               this.load_V_15();
            } else if (MC.player.containerMenu != MC.player.inventoryMenu) {
               this.flush_V_4();
            } else if (!this.bool("not-using-item") || !MC.player.isUsingItem()) {
               if (this.bool("no-movement") && flush_Z_4()) {
                  if (this.read_Z_9()) {
                     MC.gui.setScreen(new InventoryScreen(MC.player));
                     this.init_Z = true;
                  }
               } else if (!BlinkPacketManager.load_Z()) {
                  int now = ck.init_ck().state_I();
                  if (now >= this.close) {
                     int timesChanged = MC.player.getInventory().getTimesChanged();
                     if (timesChanged != this.update || now - this.write >= 4) {
                        this.update = timesChanged;
                        this.write = now;
                        if (this.init_TableIndex_DataUti() == null) {
                           if (this.init_Z) {
                              this.close();
                           }

                           this.open = -1;
                        } else {
                           if (this.open < 0) {
                              this.open = now + this.init_I("reaction", 3, 7);
                           }

                           if (now >= this.open) {
                              this.open = -1;
                              this.load_Z = true;
                              this.read_Z = false;
                              this.sync = 0;
                              this.read_V_9();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void load_V_15() {
      if (this.init_I > 0) {
         this.init_I--;
      } else if (MC.player.containerMenu != MC.player.inventoryMenu) {
         this.flush_V_4();
      } else if (!this.bool("not-using-item") || !MC.player.isUsingItem()) {
         if (this.bool("no-movement") && flush_Z_4()) {
            if (this.read_Z_9()) {
               MC.gui.setScreen(new InventoryScreen(MC.player));
               this.init_Z = true;
            }
         } else if (!BlinkPacketManager.load_Z()) {
            this.write_V_9();
         }
      }
   }

   private void read_V_9() {
      int now = ck.init_ck().state_I();
      poll = now + this.init_I_12();
      if (now >= this.flush) {
         if (this.isEnabled()
            && !aD.read_Z()
            && MC.player.containerMenu == MC.player.inventoryMenu
            && !BlinkPacketManager.load_Z()
            && (!this.bool("not-using-item") || !MC.player.isUsingItem())) {
            if (this.read_Z) {
               AutoArmorModule.Inner_DataUtil rest = this.init_TableIndex_DataUti();
               if (rest == null) {
                  if (this.sync > 0) {
                     this.update_V_8();
                  }

                  this.init(false);
                  return;
               }

               this.read_Z = false;
            }

            AutoArmorModule.Inner_DataUtil move = this.init_TableIndex_DataUti();
            if (move == null) {
               this.read_Z = true;
               this.flush = now + this.init_I("close-delay", 3, 5);
            } else if (!this.init(move.init_EquipmentSlot(), move.init_TaskTimer_DataUtil(), move.init_ItemStack())) {
               this.flush = now + 1;
            } else {
               this.sync++;
               if (this.init_TableIndex_DataUti() != null) {
                  this.flush = now + this.init_I("click-delay", 3, 5);
               } else {
                  this.read_Z = true;
                  this.flush = now + this.init_I("close-delay", 3, 5);
               }
            }
         } else {
            this.init(true);
         }
      }
   }

   private void init(boolean aborted) {
      if (aborted && this.sync > 0) {
         this.update_V_8();
      }

      this.load_Z = false;
      this.read_Z = false;
      this.sync = 0;
      this.close = ck.init_ck().state_I() + this.init_I("operation-delay", 3, 5);
   }

   private AutoArmorModule.Inner_DataUtil init_TableIndex_DataUti() {
      int threshold = this.bool("save-armor") ? this.integer("durability-threshold") : -2147483648;
      Map<EquipmentSlot, autismclient.util.TaskTimer.Inner_DataUtil> best = this.init(threshold);
      if (this.bool("prefer-elytra")) {
         autismclient.util.TaskTimer.Inner_DataUtil elytra = this.init_TaskTimer_DataUtil();
         if (elytra != null) {
            best.put(EquipmentSlot.CHEST, elytra);
         }
      }

      for (EquipmentSlot slot : init_EquipmentSlotArr) {
         autismclient.util.TaskTimer.Inner_DataUtil candidate = best.get(slot);
         if (candidate != null
            && candidate.init_TaskTimer_StreamUt() != autismclient.util.TaskTimer.Inner_StreamUtil.ARMOR
            && (candidate.init_I() < 0 || !autismclient.util.ah.init(candidate.init_I(), this.id()))) {
            ItemStack worn = MC.player.getItemBySlot(slot);
            if (worn.isEmpty() || !worn.is(Items.ELYTRA) && !autismclient.util.TaskTimer.load_Z(worn)) {
               return new AutoArmorModule.Inner_DataUtil(slot, candidate, worn);
            }
         }
      }

      return null;
   }

   private boolean read_Z_9() {
      return this.bool("save-armor") && this.bool("auto-open-inventory") && MC.gui.screen() == null && !write_Z_4() && this.sync_Z_4();
   }

   private static boolean write_Z_4() {
      Module module = al.init_Module_Str("inv-move");
      return module != null && module.isEnabled();
   }

   private void write_V_9() {
      AutoArmorModule.Inner_DataUtil move = this.init_TableIndex_DataUti();
      if (move != null && this.init(move.init_EquipmentSlot(), move.init_TaskTimer_DataUtil(), move.init_ItemStack())) {
         this.init_I = 1;
      } else {
         if (this.init_Z) {
            this.close();
         }

         this.init_I = 1;
      }
   }

   private boolean init(EquipmentSlot slot, autismclient.util.TaskTimer.Inner_DataUtil candidate, ItemStack worn) {
      boolean occupied = !worn.isEmpty();
      int armorMenuSlot = init_Map.get(slot);
      boolean hotbarFast = candidate.init_TaskTimer_StreamUt() == autismclient.util.TaskTimer.Inner_StreamUtil.HOTBAR && this.bool("hotbar");
      if (!occupied) {
         return hotbarFast ? this.init(armorMenuSlot, candidate.init_I(), ContainerInput.SWAP) : this.init(this.init(candidate), 0, ContainerInput.QUICK_MOVE);
      } else if (hotbarFast && this.bool("can-swap-armor")) {
         return this.init(armorMenuSlot, candidate.init_I(), ContainerInput.SWAP);
      } else {
         return MC.player.getInventory().getFreeSlot() >= 0
            ? this.init(armorMenuSlot, 0, ContainerInput.QUICK_MOVE)
            : this.init(armorMenuSlot, 1, ContainerInput.THROW);
      }
   }

   private boolean init(int menuSlot, int button, ContainerInput input) {
      if (menuSlot < 0) {
         return false;
      }

      if (!autismclient.util.ah.write_Z(this.id())) {
         return false;
      }

      try {
         boolean clicked = autismclient.util.av.init(MC, menuSlot, button, input);
         if (clicked && this.bool("pause-movement")) {
            push = ck.init_ck().state_I() + 1;
         }

         return clicked;
      } finally {
         autismclient.util.ah.read_V(this.id());
      }
   }

   private int init_I(String settingId, int fallbackMin, int fallbackMax) {
      return (int)Math.round(this.init_ValueRange(settingId, fallbackMin, fallbackMax).random(ThreadLocalRandom.current()));
   }

   private double init_D_Str_I_2(String settingId, int fallbackMax) {
      return this.init_ValueRange(settingId, 0, fallbackMax).max();
   }

   private boolean update_Z_4() {
      return this.init_D_Str_I_2("click-delay", 5) > 0.0
         || this.init_D_Str_I_2("close-delay", 5) > 0.0
         || this.init_D_Str_I_2("operation-delay", 5) > 0.0
         || this.init_D_Str_I_2("reaction", 7) > 0.0;
   }

   private int init_I_12() {
      return Math.max(1, this.init_I("click-delay", 3, 5));
   }

   private void update_V_8() {
      if (MC != null && MC.player != null && MC.getConnection() != null && MC.gui.screen() == null) {
         if (MC.player.containerMenu == MC.player.inventoryMenu) {
            MC.getConnection().send(new ServerboundContainerClosePacket(MC.player.containerMenu.containerId));
         }
      }
   }

   public static boolean init_Z_20() {
      int until = poll;
      if (until != -2147483648 && MC != null && MC.player != null) {
         Module module = al.init_Module_Str("auto-armor");
         return module != null && module.isEnabled() ? ck.init_ck().state_I() <= until : false;
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return init_Z_20();
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return init_Z_20();
   }

   public static boolean load_Z_9() {
      if (init_Z_20()) {
         return true;
      }

      int until = push;
      if (until == -2147483648) {
         return false;
      }

      Module module = al.init_Module_Str("auto-armor");
      if (module == null || !module.isEnabled()) {
         return false;
      }

      int age = ck.init_ck().state_I() - until;
      return age <= 0 && age > -2;
   }

   public static Input init_Input_ClientInput_Input_3(ClientInput source, Input original) {
      if (original == null || MC == null || MC.player == null || MC.player.input != source) {
         return original;
      } else if (init_Z_20()) {
         return new Input(false, false, false, false, false, false, false);
      } else {
         Module module = al.init_Module_Str("auto-armor");
         if (module == null || !module.isEnabled() || push == -2147483648) {
            return original;
         }
         return ck.init_ck().state_I() > push ? original : new Input(false, false, false, false, original.jump(), original.shift(), false);
      }
   }

   private int init(autismclient.util.TaskTimer.Inner_DataUtil candidate) {
      return autismclient.util.aw.load_I(MC, candidate.init_I());
   }

   private autismclient.util.TaskTimer.Inner_DataUtil init_TaskTimer_DataUtil() {
      ItemStack worn = MC.player.getItemBySlot(EquipmentSlot.CHEST);
      if (worn.is(Items.ELYTRA)) {
         return new autismclient.util.TaskTimer.Inner_DataUtil(worn, EquipmentSlot.CHEST, autismclient.util.TaskTimer.Inner_StreamUtil.ARMOR, -1);
      }

      for (int inv = 0; inv < 36; inv++) {
         ItemStack stack = MC.player.getInventory().getItem(inv);
         if (stack.is(Items.ELYTRA)) {
            return new autismclient.util.TaskTimer.Inner_DataUtil(
               stack, EquipmentSlot.CHEST, inv < 9 ? autismclient.util.TaskTimer.Inner_StreamUtil.HOTBAR : autismclient.util.TaskTimer.Inner_StreamUtil.INVENTORY, inv
            );
         }
      }

      if (!autismclient.util.ah.load_Z(this.id())) {
         ItemStack offhand = MC.player.getItemBySlot(EquipmentSlot.OFFHAND);
         if (offhand.is(Items.ELYTRA)) {
            return new autismclient.util.TaskTimer.Inner_DataUtil(offhand, EquipmentSlot.CHEST, autismclient.util.TaskTimer.Inner_StreamUtil.INVENTORY, 40);
         }
      }

      return null;
   }

   private void sync_V_4() {
      if (!this.bool("save-armor")) {
         this.load_I = -1;
      } else {
         int current = MC.player.getArmorValue();
         boolean handledScreen = MC.gui.screen() instanceof AbstractContainerScreen && !(MC.gui.screen() instanceof InventoryScreen);
         if (handledScreen && this.load_I >= 0 && current < this.load_I && this.bool("auto-open-inventory")) {
            Screen screen = MC.gui.screen();
            if (screen != null) {
               screen.onClose();
            }
         }

         this.load_I = current;
      }
   }

   private void flush_V_4() {
      if (this.bool("save-armor") && this.bool("auto-open-inventory") && this.sync_Z_4()) {
         Screen screen = MC.gui.screen();
         if (screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen)) {
            screen.onClose();
            this.init_I = Math.max(1, this.init_I("click-delay", 3, 5));
         }
      }
   }

   private boolean sync_Z_4() {
      int threshold = this.integer("durability-threshold");

      for (EquipmentSlot slot : init_EquipmentSlotArr) {
         ItemStack worn = MC.player.getItemBySlot(slot);
         if (!worn.isEmpty()
            && autismclient.util.TaskTimer.init_Z_ItemStack(worn)
            && autismclient.util.TaskTimer.init_I_ItemStack(worn) <= threshold
            && !autismclient.util.TaskTimer.load_Z(worn)) {
            for (int inv = 0; inv < 36; inv++) {
               ItemStack stack = MC.player.getInventory().getItem(inv);
               if (!stack.isEmpty()
                  && autismclient.util.TaskTimer.init_Z_ItemStack(stack)
                  && (this.bool("allow-cursed") || !autismclient.util.TaskTimer.load_Z(stack))
                  && slot.equals(autismclient.util.TaskTimer.init_EquipmentSlot_ItemStack(stack))
                  && autismclient.util.TaskTimer.init_I_ItemStack(stack) > threshold) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void close() {
      if (this.init_Z) {
         this.init_Z = false;
         if (MC != null && MC.gui.screen() instanceof InventoryScreen screen) {
            screen.onClose();
         }
      }
   }

   private Map<EquipmentSlot, autismclient.util.TaskTimer.Inner_DataUtil> init(int threshold) {
      return autismclient.util.TaskTimer.init_Map_Map_I(this.init_Map(), threshold);
   }

   private Map<EquipmentSlot, List<autismclient.util.TaskTimer.Inner_DataUtil>> init_Map() {
      return autismclient.util.TaskTimer.init_Map_TaskTimer_BufferHe(new autismclient.util.TaskTimer.Inner_BufferHelper() {
         @Override
         public ItemStack init(int index) {
            return Module.MC.player.getInventory().getItem(index);
         }

         @Override
         public ItemStack init(EquipmentSlot slot) {
            return Module.MC.player.getItemBySlot(slot);
         }
      }, this.bool("allow-cursed"), !autismclient.util.ah.load_Z(this.id()));
   }

   private static boolean flush_Z_4() {
      Input input = MC.player.input.keyPresses;
      return input.forward() || input.backward() || input.left() || input.right() || input.jump();
   }

   static {
      init_Map.put(EquipmentSlot.HEAD, 5);
      init_Map.put(EquipmentSlot.CHEST, 6);
      init_Map.put(EquipmentSlot.LEGS, 7);
      init_Map.put(EquipmentSlot.FEET, 8);
   }

   private record Inner_DataUtil(EquipmentSlot init_EquipmentSlot, autismclient.util.TaskTimer.Inner_DataUtil init_TaskTimer_DataUtil, ItemStack init_ItemStack) {
   }
}
