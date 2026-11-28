package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import autismclient.util.AutismConfig;
import autismclient.util.AutismSessionState;
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
   private static final EquipmentSlot[] ARMOR_SLOTS = autismclient.util.ArmorEvaluationHelper.ARMOR_SLOTS;
   private static final Map<EquipmentSlot, Integer> ARMOR_MENU_SLOTS = new EnumMap<>(EquipmentSlot.class);
   private int cooldownTicks;
   private int lastArmorValue = -1;
   private boolean init_Z;
   private static final int read_I = 4;
   private int write = -4;
   private int update = -1;
   private final Map<String, String> rawRangeValues = new HashMap<>();
   private final Map<String, ValueRange> rangeCache = new HashMap<>();
   private boolean load_Z;
   private boolean read_Z;
   private int sync;
   private int flush;
   private int close;
   private int open = -1;
   private static volatile int poll = -2147483648;
   private static volatile int push = -2147483648;

   private ValueRange rangeSetting(String settingId, int fallbackMin, int fallbackMax) {
      String raw = this.value(settingId);
      String previous = this.rawRangeValues.put(settingId, raw);
      if (previous == null || !previous.equals(raw)) {
         this.rangeCache.put(settingId, ValueRange.parse(raw, new ValueRange(fallbackMin, fallbackMax)).clamp(0.0, 10.0));
      }

      return this.rangeCache.get(settingId);
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
      this.resetState();
      this.close();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      this.init_Z = false;
   }

   private void resetState() {
      if (this.load_Z && this.sync > 0) {
         this.closeInventory();
      }

      this.cooldownTicks = 0;
      this.lastArmorValue = -1;
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
         this.trackArmorValue();
         if (this.load_Z) {
            if (AutoTotemModule.init_Z_6()) {
               this.init(true);
            } else {
               this.read_V_9();
            }
         } else if (!AutoTotemModule.init_Z_6()) {
            if (!this.hasAnyDelay()) {
               this.load_V_15();
            } else if (MC.player.containerMenu != MC.player.inventoryMenu) {
               this.flush_V_4();
            } else if (!this.bool("not-using-item") || !MC.player.isUsingItem()) {
               if (this.bool("no-movement") && isMovingInput()) {
                  if (this.shouldAutoOpenInventory()) {
                     MC.gui.setScreen(new InventoryScreen(MC.player));
                     this.init_Z = true;
                  }
               } else if (!BlinkPacketManager.isHoldingActionsOnly()) {
                  int now = AutismSessionState.getInstance().getClientTick();
                  if (now >= this.close) {
                     int timesChanged = MC.player.getInventory().getTimesChanged();
                     if (timesChanged != this.update || now - this.write >= 4) {
                        this.update = timesChanged;
                        this.write = now;
                        if (this.findBestMove() == null) {
                           if (this.init_Z) {
                              this.close();
                           }

                           this.open = -1;
                        } else {
                           if (this.open < 0) {
                              this.open = now + this.randomDelay("reaction", 3, 7);
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
      if (this.cooldownTicks > 0) {
         this.cooldownTicks--;
      } else if (MC.player.containerMenu != MC.player.inventoryMenu) {
         this.flush_V_4();
      } else if (!this.bool("not-using-item") || !MC.player.isUsingItem()) {
         if (this.bool("no-movement") && isMovingInput()) {
            if (this.shouldAutoOpenInventory()) {
               MC.gui.setScreen(new InventoryScreen(MC.player));
               this.init_Z = true;
            }
         } else if (!BlinkPacketManager.isHoldingActionsOnly()) {
            this.tryEquipBest();
         }
      }
   }

   private void read_V_9() {
      int now = AutismSessionState.getInstance().getClientTick();
      poll = now + this.clickDelay();
      if (now >= this.flush) {
         if (this.isEnabled()
            && !aD.read_Z()
            && MC.player.containerMenu == MC.player.inventoryMenu
            && !BlinkPacketManager.isHoldingActionsOnly()
            && (!this.bool("not-using-item") || !MC.player.isUsingItem())) {
            if (this.read_Z) {
               AutoArmorModule.ArmorMove rest = this.findBestMove();
               if (rest == null) {
                  if (this.sync > 0) {
                     this.closeInventory();
                  }

                  this.init(false);
                  return;
               }

               this.read_Z = false;
            }

            AutoArmorModule.ArmorMove move = this.findBestMove();
            if (move == null) {
               this.read_Z = true;
               this.flush = now + this.randomDelay("close-delay", 3, 5);
            } else if (!this.init(move.slot(), move.candidate(), move.stack())) {
               this.flush = now + 1;
            } else {
               this.sync++;
               if (this.findBestMove() != null) {
                  this.flush = now + this.randomDelay("click-delay", 3, 5);
               } else {
                  this.read_Z = true;
                  this.flush = now + this.randomDelay("close-delay", 3, 5);
               }
            }
         } else {
            this.init(true);
         }
      }
   }

   private void init(boolean aborted) {
      if (aborted && this.sync > 0) {
         this.closeInventory();
      }

      this.load_Z = false;
      this.read_Z = false;
      this.sync = 0;
      this.close = AutismSessionState.getInstance().getClientTick() + this.randomDelay("operation-delay", 3, 5);
   }

   private AutoArmorModule.ArmorMove findBestMove() {
      int threshold = this.bool("save-armor") ? this.integer("durability-threshold") : -2147483648;
      Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate> best = this.init(threshold);
      if (this.bool("prefer-elytra")) {
         autismclient.util.ArmorEvaluationHelper.ArmorCandidate elytra = this.init_TaskTimer_DataUtil();
         if (elytra != null) {
            best.put(EquipmentSlot.CHEST, elytra);
         }
      }

      for (EquipmentSlot slot : ARMOR_SLOTS) {
         autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate = best.get(slot);
         if (candidate != null
            && candidate.kind() != autismclient.util.ArmorEvaluationHelper.SourceKind.ARMOR
            && (candidate.inventorySlot() < 0 || !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(candidate.inventorySlot(), this.id()))) {
            ItemStack worn = MC.player.getItemBySlot(slot);
            if (worn.isEmpty() || !worn.is(Items.ELYTRA) && !autismclient.util.ArmorEvaluationHelper.hasBindingCurse(worn)) {
               return new AutoArmorModule.ArmorMove(slot, candidate, worn);
            }
         }
      }

      return null;
   }

   private boolean shouldAutoOpenInventory() {
      return this.bool("save-armor") && this.bool("auto-open-inventory") && MC.gui.screen() == null && !isInvMoveEnabled() && this.hasLowDurabilityArmor();
   }

   private static boolean isInvMoveEnabled() {
      Module module = al.init_Module_Str("inv-move");
      return module != null && module.isEnabled();
   }

   private void tryEquipBest() {
      AutoArmorModule.ArmorMove move = this.findBestMove();
      if (move != null && this.init(move.slot(), move.candidate(), move.stack())) {
         this.cooldownTicks = 1;
      } else {
         if (this.init_Z) {
            this.close();
         }

         this.cooldownTicks = 1;
      }
   }

   private boolean init(EquipmentSlot slot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate, ItemStack worn) {
      boolean occupied = !worn.isEmpty();
      int armorMenuSlot = ARMOR_MENU_SLOTS.get(slot);
      boolean hotbarFast = candidate.kind() == autismclient.util.ArmorEvaluationHelper.SourceKind.HOTBAR && this.bool("hotbar");
      if (!occupied) {
         return hotbarFast ? this.init(armorMenuSlot, candidate.inventorySlot(), ContainerInput.SWAP) : this.init(this.init(candidate), 0, ContainerInput.QUICK_MOVE);
      } else if (hotbarFast && this.bool("can-swap-armor")) {
         return this.init(armorMenuSlot, candidate.inventorySlot(), ContainerInput.SWAP);
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

      if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         return false;
      }

      try {
         boolean clicked = autismclient.util.ContainerClickExecutor.init(MC, menuSlot, button, input);
         if (clicked && this.bool("pause-movement")) {
            push = AutismSessionState.getInstance().getClientTick() + 1;
         }

         return clicked;
      } finally {
         autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
      }
   }

   private int randomDelay(String settingId, int fallbackMin, int fallbackMax) {
      return (int)Math.round(this.rangeSetting(settingId, fallbackMin, fallbackMax).random(ThreadLocalRandom.current()));
   }

   private double rangeMax(String settingId, int fallbackMax) {
      return this.rangeSetting(settingId, 0, fallbackMax).max();
   }

   private boolean hasAnyDelay() {
      return this.rangeMax("click-delay", 5) > 0.0
         || this.rangeMax("close-delay", 5) > 0.0
         || this.rangeMax("operation-delay", 5) > 0.0
         || this.rangeMax("reaction", 7) > 0.0;
   }

   private int clickDelay() {
      return Math.max(1, this.randomDelay("click-delay", 3, 5));
   }

   private void closeInventory() {
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
         return module != null && module.isEnabled() ? AutismSessionState.getInstance().getClientTick() <= until : false;
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

      int age = AutismSessionState.getInstance().getClientTick() - until;
      return age <= 0 && age > -2;
   }

   public static Input overrideInput(ClientInput source, Input original) {
      if (original == null || MC == null || MC.player == null || MC.player.input != source) {
         return original;
      } else if (init_Z_20()) {
         return new Input(false, false, false, false, false, false, false);
      } else {
         Module module = al.init_Module_Str("auto-armor");
         if (module == null || !module.isEnabled() || push == -2147483648) {
            return original;
         }
         return AutismSessionState.getInstance().getClientTick() > push ? original : new Input(false, false, false, false, original.jump(), original.shift(), false);
      }
   }

   private int init(autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate) {
      return autismclient.util.InventorySlotHelper.toScreenSlot(MC, candidate.inventorySlot());
   }

   private autismclient.util.ArmorEvaluationHelper.ArmorCandidate init_TaskTimer_DataUtil() {
      ItemStack worn = MC.player.getItemBySlot(EquipmentSlot.CHEST);
      if (worn.is(Items.ELYTRA)) {
         return new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(worn, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.SourceKind.ARMOR, -1);
      }

      for (int inv = 0; inv < 36; inv++) {
         ItemStack stack = MC.player.getInventory().getItem(inv);
         if (stack.is(Items.ELYTRA)) {
            return new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(
               stack, EquipmentSlot.CHEST, inv < 9 ? autismclient.util.ArmorEvaluationHelper.SourceKind.HOTBAR : autismclient.util.ArmorEvaluationHelper.SourceKind.INVENTORY, inv
            );
         }
      }

      if (!autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id())) {
         ItemStack offhand = MC.player.getItemBySlot(EquipmentSlot.OFFHAND);
         if (offhand.is(Items.ELYTRA)) {
            return new autismclient.util.ArmorEvaluationHelper.ArmorCandidate(offhand, EquipmentSlot.CHEST, autismclient.util.ArmorEvaluationHelper.SourceKind.INVENTORY, 40);
         }
      }

      return null;
   }

   private void trackArmorValue() {
      if (!this.bool("save-armor")) {
         this.lastArmorValue = -1;
      } else {
         int current = MC.player.getArmorValue();
         boolean handledScreen = MC.gui.screen() instanceof AbstractContainerScreen && !(MC.gui.screen() instanceof InventoryScreen);
         if (handledScreen && this.lastArmorValue >= 0 && current < this.lastArmorValue && this.bool("auto-open-inventory")) {
            Screen screen = MC.gui.screen();
            if (screen != null) {
               screen.onClose();
            }
         }

         this.lastArmorValue = current;
      }
   }

   private void flush_V_4() {
      if (this.bool("save-armor") && this.bool("auto-open-inventory") && this.hasLowDurabilityArmor()) {
         Screen screen = MC.gui.screen();
         if (screen instanceof AbstractContainerScreen && !(screen instanceof InventoryScreen)) {
            screen.onClose();
            this.cooldownTicks = Math.max(1, this.randomDelay("click-delay", 3, 5));
         }
      }
   }

   private boolean hasLowDurabilityArmor() {
      int threshold = this.integer("durability-threshold");

      for (EquipmentSlot slot : ARMOR_SLOTS) {
         ItemStack worn = MC.player.getItemBySlot(slot);
         if (!worn.isEmpty()
            && autismclient.util.ArmorEvaluationHelper.isArmor(worn)
            && autismclient.util.ArmorEvaluationHelper.remainingDurability(worn) <= threshold
            && !autismclient.util.ArmorEvaluationHelper.hasBindingCurse(worn)) {
            for (int inv = 0; inv < 36; inv++) {
               ItemStack stack = MC.player.getInventory().getItem(inv);
               if (!stack.isEmpty()
                  && autismclient.util.ArmorEvaluationHelper.isArmor(stack)
                  && (this.bool("allow-cursed") || !autismclient.util.ArmorEvaluationHelper.hasBindingCurse(stack))
                  && slot.equals(autismclient.util.ArmorEvaluationHelper.slotOf(stack))
                  && autismclient.util.ArmorEvaluationHelper.remainingDurability(stack) > threshold) {
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

   private Map<EquipmentSlot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate> init(int threshold) {
      return autismclient.util.ArmorEvaluationHelper.bestBySlot(this.armorCandidates(), threshold);
   }

   private Map<EquipmentSlot, List<autismclient.util.ArmorEvaluationHelper.ArmorCandidate>> armorCandidates() {
      return autismclient.util.ArmorEvaluationHelper.candidatesBySlot(new autismclient.util.ArmorEvaluationHelper.StackSource() {
         @Override
         public ItemStack init(int index) {
            return Module.MC.player.getInventory().getItem(index);
         }

         @Override
         public ItemStack init(EquipmentSlot slot) {
            return Module.MC.player.getItemBySlot(slot);
         }
      }, this.bool("allow-cursed"), !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()));
   }

   private static boolean isMovingInput() {
      Input input = MC.player.input.keyPresses;
      return input.forward() || input.backward() || input.left() || input.right() || input.jump();
   }

   static {
      ARMOR_MENU_SLOTS.put(EquipmentSlot.HEAD, 5);
      ARMOR_MENU_SLOTS.put(EquipmentSlot.CHEST, 6);
      ARMOR_MENU_SLOTS.put(EquipmentSlot.LEGS, 7);
      ARMOR_MENU_SLOTS.put(EquipmentSlot.FEET, 8);
   }

   private record ArmorMove(EquipmentSlot slot, autismclient.util.ArmorEvaluationHelper.ArmorCandidate candidate, ItemStack stack) {
   }
}
