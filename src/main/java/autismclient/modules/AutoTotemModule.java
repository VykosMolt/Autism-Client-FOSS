package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.mixin.accessor.AutismPlayerAccessor;
import autismclient.util.ck;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AutoTotemModule extends Module {
   private static final int init_I = 40;
   private static final int load_I = 0;
   private static final int read_I = 1;
   private static final int write_I = 2;
   private static final Direction[] init_DirectionArr = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private static final EquipmentSlot[] init_EquipmentSlotArr = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final float init_F = 0.91F;
   private static final float load_F = 0.98F;
   private static final float read_F = 0.99F;
   private static final float write_F = 0.98F;
   private static final List<BlockPos> init_List = init_List_F(10.0F);
   private static final long init_J = 2000L;
   private static final long load_J = 500L;
   private long read_J;
   private long write_J = -1L;
   private int update_I = 0;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int sync = -1;
   private long update_J = -1L;
   private Item init_Item = Items.AIR;
   private static final int flush = 0;
   private static final int close = 1;
   private static final int open = 2;
   private static final int poll = 3;
   private int push = 0;
   private int pop = -1;
   private int tick = -1;
   private int run;
   private int get;
   private int set;
   private int put;
   private int add = -1;
   private autismclient.gui.DataUtil init_DataUtil;
   private boolean init_Z;
   private int clear = -1;
   private static volatile int reset = -2147483648;
   private final Map<String, String> init_Map = new HashMap<>();
   private final Map<String, ValueRange> load_Map = new HashMap<>();
   private static volatile int start = -2147483648;
   private final Map<Long, Float> read_Map = new HashMap<>();
   private int stop = -2;

   AutoTotemModule() {
      super("auto-totem", "AutoTotem", ae.read, "Keeps a totem in offhand.");
      this.add(new ChoiceSetting("switch-mode", "Switch Mode", "Switch", "Smart", "Switch", "PickUp").description("How the totem is swapped.").build());
      this.add(new IntSetting("switch-delay", "Switch Delay (ms)", 0, 0, 500, 5).description("Delay between offhand swaps.").build());
      this.add(new IntSetting("switch-back-delay", "Switch Back Delay (ms)", 40, 0, 500, 5).description("Delay before switching back.").build());
      this.add(
         new RangeSetting("click-delay", "Click Delay", new ValueRange(1, 2), 0.0, 10.0, 1.0)
            .group("Timing")
            .unit("ticks")
            .description("Ticks between packets")
      );
      this.add(
         new RangeSetting("close-delay", "Close Delay", new ValueRange(1, 2), 0.0, 10.0, 1.0)
            .group("Timing")
            .unit("ticks")
            .description("Ticks before session close")
      );
      this.add(
         new RangeSetting("operation-delay", "Spacing", new ValueRange(0, 2), 0.0, 10.0, 1.0)
            .group("Timing")
            .unit("ticks")
            .description("Ticks between operations")
      );
      this.add(
         new RangeSetting("reaction", "Reaction", new ValueRange(0, 2), 0.0, 10.0, 1.0).group("Timing").unit("ticks").description("Reaction ticks after pop")
      );
      this.add(new RangeSetting("settle", "Settle", new ValueRange(1, 1), 0.0, 5.0, 1.0).group("Timing").unit("ticks").description("Ticks before first packet"));
      this.add(new BoolSetting("send-directly", "Send Directly", false).description("Swap even with screens open.").build());
      this.add(new BoolSetting("health", "Health", true).group("Health").description("Totem only when endangered.").build());
      this.add(
         new IntSetting("health-threshold", "Health Threshold", 14, 0, 20, 1)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Totem below this health.")
            .build()
      );
      this.add(
         new BoolSetting("safety", "Safety", true)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Stricter threshold inside holes.")
            .build()
      );
      this.add(
         new IntSetting("safe-health-threshold", "Safe Health Threshold", 10, 0, 20, 1)
            .group("Health")
            .visibleWhen(() -> this.bool("health") && this.bool("safety"))
            .description("Threshold while in holes.")
            .build()
      );
      this.add(
         new BoolSetting("subtract-calculated-damage", "Subtract Calculated Damage", false)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Subtract predicted damage too.")
            .build()
      );
      this.add(
         new BoolSetting("predict-explosions", "Predict Explosion Damage", true)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Predict entity explosion damage.")
            .build()
      );
      this.add(
         new BoolSetting("predict-block-explosions", "Predict Block Explosions", false)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Predict bed and anchor explosions.")
            .build()
      );
      this.add(
         new BoolSetting("predict-fall-damage", "Predict Fall Damage", true)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Predict incoming fall damage.")
            .build()
      );
      this.add(
         new BoolSetting("ignore-elytra", "Ignore Elytra", false)
            .group("Health")
            .visibleWhen(() -> this.bool("health") && this.bool("predict-fall-damage"))
            .description("Ignore fall damage while gliding.")
            .build()
      );
      this.add(
         new BoolSetting("missing-armor", "Missing Armor", true)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Totem when armor missing.")
            .build()
      );
      this.add(
         new BoolSetting("switch-back", "Switch Back", true)
            .group("Health")
            .visibleWhen(() -> this.bool("health"))
            .description("Restore item after healing.")
            .build()
      );
      this.add(new BoolSetting("pause-movement", "Pause Movement", true));
      this.add(new BoolSetting("no-render", "No Render", true).description("Hide the totem pop animation.").build());
      autismclient.aggressivepvp.Versions.autoTotemCreated(this);
   }

   @Override
   public String settingsHint() {
      String hint = autismclient.aggressivepvp.Versions.hint(this);
      return hint != null ? hint : super.settingsHint();
   }

   @Override
   public void onEnable() {
      if (autismclient.aggressivepvp.Versions.onEnable(autismclient.aggressivepvp.Versions.totem)) {
         reset = -2147483648;
         start = -2147483648;
         return;
      }

      this.init_V_5();
   }

   @Override
   public void onDisable() {
      if (autismclient.aggressivepvp.Versions.onDisable(autismclient.aggressivepvp.Versions.totem)) {
         reset = -2147483648;
         start = -2147483648;
         return;
      }

      this.init_V_5();
   }

   @Override
   public void onGameLeft() {
      if (autismclient.aggressivepvp.Versions.onGameLeft(autismclient.aggressivepvp.Versions.totem)) {
         reset = -2147483648;
         start = -2147483648;
         return;
      }

      this.init_V_5();
   }

   private void init_V_5() {
      this.update_I = 0;
      this.write_J = -1L;
      this.read_J = 0L;
      this.init_ItemStack = ItemStack.EMPTY;
      this.sync = -1;
      this.update_J = -1L;
      this.init_Item = Items.AIR;
      if (this.push != 0) {
         this.write_V_3();
      } else {
         this.update_V_3();
      }

      this.init_V_Z_2(false);
      this.read_Map.clear();
      this.stop = -2;
      this.put = 0;
      this.add = -1;
      reset = -2147483648;
      start = -2147483648;
      autismclient.util.ah.update_V(this.id());
   }

   @Override
   public void preMovementTick() {
      if (autismclient.aggressivepvp.Versions.preMovementTick(autismclient.aggressivepvp.Versions.totem)) {
         return;
      }

      if (MC != null && MC.player != null && MC.level != null) {
         if (aD.read_Z()) {
            boolean ghostHadClicks = this.push != 0;
            this.update_V_3();
            this.init_V_Z_2(ghostHadClicks);
         } else {
            this.load_V_4();
            if (this.push != 0) {
               this.read_V_3();
            } else {
               int now = ck.init_ck().state_I();
               boolean health = this.bool("health");
               boolean wantTotem = health ? !sync_Z() && this.pop() : true;
               int mode = wantTotem ? 1 : 0;
               if (mode == 0 && health && this.bool("switch-back") && this.update_I == 1) {
                  mode = 2;
               }

               if (mode == 0) {
                  this.add = -1;
               }

               if (mode != this.update_I && this.update_I == 1) {
                  if (this.write_J < 0L) {
                     this.write_J = System.currentTimeMillis();
                  }

                  if (System.currentTimeMillis() - this.write_J < this.integer("switch-back-delay")) {
                     this.sync_V();
                     return;
                  }
               }

               this.write_J = -1L;
               boolean timed = this.push();
               if (!timed || now >= this.put) {
                  boolean handedOver = mode == 1 && autismclient.util.ah.sync(this.id());
                  if (handedOver || System.currentTimeMillis() - this.read_J >= this.integer("switch-delay")) {
                     int fromSlot;
                     if (mode == 1) {
                        fromSlot = init_I_3();
                        if (fromSlot < 0) {
                           this.sync_V();
                           return;
                        }
                     } else {
                        if (mode != 2) {
                           this.sync_V();
                           return;
                        }

                        fromSlot = this.load_I_2();
                        if (fromSlot < 0) {
                           if (this.sync < 0) {
                              this.update_I = 0;
                           }

                           this.sync_V();
                           return;
                        }
                     }

                     int previousMode = this.update_I;
                     this.update_I = mode;
                     if (fromSlot == 40) {
                        this.sync_V();
                     } else {
                        if (timed) {
                           if (this.add < 0 && this.init_DataUtil == null) {
                              this.add = now + this.init_I_Str("reaction", 0, 2);
                           }

                           if (now < this.add) {
                              return;
                           }

                           this.add = -1;
                        }

                        if (timed) {
                           if (this.clear >= 0) {
                              if (this.init_DataUtil != null && MC.gui.screen() != this.init_DataUtil) {
                                 this.init_DataUtil = null;
                              }

                              if (this.init_DataUtil != null && now < this.clear) {
                                 init_V_I_2(this.write_I_2());
                                 return;
                              }

                              this.clear = -1;
                           } else if (this.init_DataUtil == null && MC.gui.screen() == null && this.init_Z_I_2(fromSlot)) {
                              this.init_DataUtil = new autismclient.gui.DataUtil(MC.player);
                              this.init_Z = true;
                              MC.gui.setScreen(this.init_DataUtil);
                              MC.mouseHandler.grabMouse();
                              this.clear = now + this.init_I_Str("settle", 1, 1);
                              init_V_I_2(this.write_I_2());
                              return;
                           }
                        }

                        ItemStack offhandBefore = MC.player.getOffhandItem().copy();
                        boolean switched = timed ? this.init_Z_I(fromSlot, mode == 1, offhandBefore) : this.init_Z_I_Z_2(fromSlot, mode == 1);
                        if (!switched) {
                           this.update_I = previousMode;
                           this.init_V_Z_2(false);
                        } else {
                           if (mode == 1 && health && this.bool("switch-back")) {
                              this.init_ItemStack = offhandBefore;
                              this.sync = fromSlot;
                           } else {
                              this.init_ItemStack = ItemStack.EMPTY;
                              this.sync = -1;
                           }

                           this.read_J = System.currentTimeMillis();
                           this.load_V_4();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void load_V_4() {
      boolean tripLive = this.write_Z_2();
      if (!tripLive && this.sync >= 0) {
         this.init_ItemStack = ItemStack.EMPTY;
         this.sync = -1;
      }

      if (tripLive) {
         autismclient.util.ah.init_Z(this.id(), this.sync);
      } else if (this.push != 0) {
         autismclient.util.ah.init_Z(this.id(), this.pop);
      } else {
         autismclient.util.ah.load_V(this.id());
      }

      if (!tripLive && this.push == 0 && !this.update_Z_2() && !init_Z_ItemStack_2(MC.player.getOffhandItem())) {
         autismclient.util.ah.init_V(this.id());
      } else {
         autismclient.util.ah.init_Z(this.id());
      }
   }

   private boolean write_Z_2() {
      if (this.sync < 0) {
         return false;
      } else if (!this.bool("health") || !this.bool("switch-back")) {
         return false;
      } else if (this.push != 0) {
         return true;
      } else {
         return this.load_I_2() >= 0 ? true : System.currentTimeMillis() - this.read_J < 2000L;
      }
   }

   private boolean update_Z_2() {
      if (this.update_J < 0L) {
         return false;
      }

      if (MC.player.getOffhandItem().getItem() == this.init_Item && System.currentTimeMillis() - this.update_J < 500L) {
         return true;
      }

      this.update_J = -1L;
      return false;
   }

   private static boolean sync_Z() {
      return MC.player.isCreative() || MC.player.isSpectator() || MC.player.isDeadOrDying();
   }

   private static int init_I_3() {
      LocalPlayer player = MC.player;
      if (init_Z_ItemStack_2(player.getOffhandItem())) {
         return 40;
      }

      for (int i = 0; i < 9; i++) {
         if (init_Z_ItemStack_2(player.getInventory().getItem(i))) {
            return i;
         }
      }

      for (int i = 9; i < 36; i++) {
         if (init_Z_ItemStack_2(player.getInventory().getItem(i))) {
            return i;
         }
      }

      return -1;
   }

   private static boolean init_Z_ItemStack_2(ItemStack stack) {
      return !stack.isEmpty() && stack.has(DataComponents.DEATH_PROTECTION);
   }

   private int load_I_2() {
      if (this.sync >= 0 && this.sync != 40) {
         ItemStack stack = MC.player.getInventory().getItem(this.sync);
         if (this.init_ItemStack.isEmpty()) {
            return stack.isEmpty() ? this.sync : -1;
         } else {
            return !stack.isEmpty() && stack.getItem() == this.init_ItemStack.getItem() ? this.sync : -1;
         }
      } else {
         return -1;
      }
   }

   private boolean init_Z_I_Z_2(int fromSlot, boolean isTotem) {
      if (!this.isEnabled()) {
         return false;
      }

      if (!MC.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      boolean foreign = MC.player.containerMenu != MC.player.inventoryMenu;
      if (!foreign || this.bool("send-directly") && isTotem) {
         if (this.update_Z_2()) {
            return false;
         }

         String switchMode = this.choice("switch-mode");
         boolean smart = foreign || "Smart".equals(switchMode) && fromSlot >= 0 && fromSlot < 9;
         if (isTotem && !autismclient.util.ah.init_Z(this.id())) {
            return false;
         }

         if (!autismclient.util.ah.write_Z(this.id())) {
            return false;
         }

         boolean switched;
         try {
            if (smart) {
               switched = this.write(fromSlot);
            } else {
               boolean var10000;
               switch (switchMode) {
                  case "Smart":
                  case "PickUp":
                     var10000 = read(fromSlot);
                     break;
                  default:
                     var10000 = load_Z_I(fromSlot);
               }

               switched = var10000;
            }
         } finally {
            autismclient.util.ah.read_V(this.id());
         }

         if (switched && this.bool("pause-movement")) {
            start = ck.init_ck().state_I() + 1;
         }

         return switched;
      } else {
         return false;
      }
   }

   private boolean init_Z_I(int fromSlot, boolean isTotem, ItemStack offhandBefore) {
      if (!this.isEnabled()) {
         return false;
      }

      if (!MC.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      boolean foreign = MC.player.containerMenu != MC.player.inventoryMenu;
      if (!foreign || this.bool("send-directly") && isTotem) {
         if (this.update_Z_2()) {
            return false;
         }

         if (BlinkPacketManager.load_Z()) {
            return false;
         }

         String switchMode = this.choice("switch-mode");
         boolean smart = foreign || "Smart".equals(switchMode) && fromSlot >= 0 && fromSlot < 9;
         boolean pickup = !smart && ("Smart".equals(switchMode) || "PickUp".equals(switchMode));
         if (smart) {
            return this.load(fromSlot, isTotem);
         }

         if (!pickup) {
            boolean switched = this.init_Z_I_Z_2(fromSlot, isTotem);
            if (switched) {
               int now = ck.init_ck().state_I();
               this.push = 3;
               this.pop = fromSlot;
               this.run = 0;
               this.get = 1;
               this.set = now + this.init_I_Str("close-delay", 1, 2);
               init_V_I_2(this.write_I_2());
               this.close_V();
            }

            return switched;
         } else {
            int from = autismclient.util.aw.load_I(MC, fromSlot);
            int offhand = autismclient.util.aw.load_I(MC, 40);
            if (from < 0 || offhand < 0) {
               return false;
            }

            if (isTotem && !autismclient.util.ah.init_Z(this.id())) {
               return false;
            }

            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            boolean clicked;
            try {
               clicked = autismclient.util.av.init(MC, from, 0, ContainerInput.PICKUP);
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            if (!clicked) {
               return false;
            }

            int now = ck.init_ck().state_I();
            this.push = 1;
            this.pop = fromSlot;
            this.run = 1;
            this.get = (offhandBefore.isEmpty() ? 2 : 3) + 1;
            this.set = now + this.init_I_Str("click-delay", 1, 2);
            init_V_I_2(this.write_I_2());
            this.close_V();
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean load(int fromSlot, boolean isTotem) {
      if (fromSlot >= 0 && fromSlot <= 8) {
         ClientPacketListener connection = MC.getConnection();
         if (connection == null || MC.gameMode == null) {
            return false;
         }

         if (isTotem && !autismclient.util.ah.init_Z(this.id())) {
            return false;
         }

         ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
         int now = ck.init_ck().state_I();
         int selected = MC.player.getInventory().getSelectedSlot();
         if (selected == fromSlot) {
            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            this.init_Item = MC.player.getOffhandItem().getItem();
            this.update_J = System.currentTimeMillis();
            init_V_I_2(this.write_I_2());
            this.put = now + this.init_I_Str("operation-delay", 0, 2);
            this.close_V();
            return true;
         } else {
            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               connection.send(new ServerboundSetCarriedItemPacket(fromSlot));
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            this.push = 2;
            this.pop = fromSlot;
            this.tick = selected;
            this.run = 1;
            this.get = 3;
            this.set = now + this.init_I_Str("click-delay", 1, 2);
            init_V_I_2(this.write_I_2());
            this.close_V();
            return true;
         }
      } else {
         return false;
      }
   }

   private void read_V_3() {
      int now = ck.init_ck().state_I();
      reset = now + this.write_I_2();
      if (this.init_DataUtil != null && MC.gui.screen() != this.init_DataUtil) {
         this.init_DataUtil = null;
      }

      if (now >= this.set) {
         if (!this.isEnabled() || this.flush_Z()) {
            this.write_V_3();
         } else if (!autismclient.util.ah.write_Z(this.id())) {
            this.set = now + 1;
         } else {
            boolean stepped;
            try {
               boolean var10000;
               switch (this.push) {
                  case 2:
                     var10000 = this.poll();
                     break;
                  case 3:
                     var10000 = this.open();
                     break;
                  default:
                     var10000 = this.close_Z();
               }

               stepped = var10000;
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            if (!stepped) {
               this.write_V_3();
            } else {
               this.run++;
               this.set = now + this.read_I_2();
               this.close_V();
               if (this.run >= this.get) {
                  this.put = now + this.init_I_Str("operation-delay", 0, 2);
                  this.update_V_3();
               }
            }
         }
      }
   }

   private boolean flush_Z() {
      if (BlinkPacketManager.load_Z()) {
         return true;
      } else if (this.push == 2) {
         return MC.getConnection() == null || MC.gameMode == null;
      } else if (this.push == 3) {
         return false;
      } else {
         return MC.player.containerMenu != MC.player.inventoryMenu ? true : this.run < this.get - 1 && MC.player.containerMenu.getCarried().isEmpty();
      }
   }

   private boolean close_Z() {
      if (this.run == this.get - 1) {
         this.flush_V();
         return true;
      } else {
         int handlerSlot = autismclient.util.aw.load_I(MC, this.run == 1 ? 40 : this.pop);
         return handlerSlot < 0 ? false : autismclient.util.av.init(MC, handlerSlot, 0, ContainerInput.PICKUP);
      }
   }

   private boolean open() {
      this.flush_V();
      return true;
   }

   private boolean poll() {
      ClientPacketListener connection = MC.getConnection();
      if (connection == null) {
         return false;
      }

      if (this.run == 1) {
         connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
         this.init_Item = MC.player.getOffhandItem().getItem();
         this.update_J = System.currentTimeMillis();
      } else {
         connection.send(new ServerboundSetCarriedItemPacket(this.tick));
      }

      return true;
   }

   private void write_V_3() {
      if (this.push == 2) {
         ClientPacketListener connection = MC.getConnection();
         if (connection != null && this.tick != this.pop && autismclient.util.ah.write_Z(this.id())) {
            try {
               connection.send(new ServerboundSetCarriedItemPacket(this.tick));
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         }
      } else if (MC.player != null && MC.player.containerMenu == MC.player.inventoryMenu && !MC.player.containerMenu.getCarried().isEmpty()) {
         int from = autismclient.util.aw.load_I(MC, this.pop);
         if (from >= 0 && autismclient.util.ah.write_Z(this.id())) {
            try {
               autismclient.util.av.init(MC, from, 0, ContainerInput.PICKUP);
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         }
      }

      this.put = ck.init_ck().state_I() + this.init_I_Str("operation-delay", 0, 2);
      this.init_V_Z_2(true);
      this.update_V_3();
   }

   private void update_V_3() {
      this.push = 0;
      this.pop = -1;
      this.tick = -1;
      this.run = 0;
      this.get = 0;
      this.set = 0;
   }

   private boolean init_Z_I_2(int fromSlot) {
      boolean foreign = MC.player.containerMenu != MC.player.inventoryMenu;
      String switchMode = this.choice("switch-mode");
      return !foreign && (!"Smart".equals(switchMode) || fromSlot < 0 || fromSlot >= 9);
   }

   private void sync_V() {
      this.init_V_Z_2(false);
   }

   private void init_V_Z_2(boolean humanShape) {
      this.init_Z = false;
      this.clear = -1;
      autismclient.gui.DataUtil screen = this.init_DataUtil;
      this.init_DataUtil = null;
      if (screen != null && MC.gui.screen() == screen) {
         if (humanShape && MC.getConnection() != null && MC.player != null) {
            screen.onClose();
         }

         if (MC.gui.screen() == screen && MC.player != null) {
            MC.gui.setScreen(null);
            MC.mouseHandler.grabMouse();
         }
      }
   }

   private static void init_V_I_2(int ticks) {
      reset = ck.init_ck().state_I() + ticks;
   }

   private ValueRange init_ValueRange_Str(String settingId, int fallbackMin, int fallbackMax) {
      String raw = this.value(settingId);
      String previous = this.init_Map.put(settingId, raw);
      if (previous == null || !previous.equals(raw)) {
         this.load_Map.put(settingId, ValueRange.parse(raw, new ValueRange(fallbackMin, fallbackMax)).clamp(0.0, 10.0));
      }

      return this.load_Map.get(settingId);
   }

   private int init_I_Str(String settingId, int fallbackMin, int fallbackMax) {
      return (int)Math.round(this.init_ValueRange_Str(settingId, fallbackMin, fallbackMax).random(ThreadLocalRandom.current()));
   }

   private int read_I_2() {
      boolean nextIsClose = this.push == 3 || this.push == 1 && this.run == this.get - 1;
      return nextIsClose ? this.init_I_Str("close-delay", 1, 2) : this.init_I_Str("click-delay", 1, 2);
   }

   private int write_I_2() {
      return Math.max(1, this.init_I_Str("click-delay", 1, 2));
   }

   private double init_D_Str_I(String settingId, int fallbackMax) {
      return this.init_ValueRange_Str(settingId, 0, fallbackMax).max();
   }

   private boolean push() {
      return this.init_D_Str_I("click-delay", 5) > 0.0
         || this.init_D_Str_I("close-delay", 5) > 0.0
         || this.init_D_Str_I("operation-delay", 5) > 0.0
         || this.init_D_Str_I("reaction", 7) > 0.0;
   }

   private void flush_V() {
      if (this.init_Z) {
         this.init_V_Z_2(true);
      } else if (MC.getConnection() != null && MC.gui.screen() == null && MC.player.containerMenu == MC.player.inventoryMenu) {
         MC.getConnection().send(new ServerboundContainerClosePacket(MC.player.containerMenu.containerId));
      }
   }

   public static boolean init_Z_6() {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.totem)) {
         return autismclient.aggressivepvp.Versions.aggressiveTotemBusy();
      }

      int until = reset;
      if (until != -2147483648 && MC != null && MC.player != null) {
         Module module = al.init_Module_Str("auto-totem");
         return module != null && module.isEnabled() ? ck.init_ck().state_I() <= until : false;
      } else {
         return false;
      }
   }

   public static boolean load_Z_2() {
      return al.init_Module_Str("auto-totem") instanceof AutoTotemModule totem && totem.isEnabled() && totem.bool("no-render");
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.totem) ? false : init_Z_6();
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.totem) ? false : init_Z_6();
   }

   private void close_V() {
      if (this.bool("pause-movement")) {
         start = ck.init_ck().state_I() + 1;
      }
   }

   public static boolean read_Z_3() {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.totem)) {
         return autismclient.aggressivepvp.Versions.aggressiveTotemBusy();
      }

      if (init_Z_6()) {
         return true;
      }

      int until = start;
      if (until == -2147483648) {
         return false;
      }

      int age = ck.init_ck().state_I() - until;
      return age <= 0 && age > -2;
   }

   public static Input init_Input_ClientInput_Input(ClientInput source, Input original) {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.totem)) {
         return original;
      } else if (original == null || MC == null || MC.player == null || MC.player.input != source) {
         return original;
      } else if (init_Z_6()) {
         return new Input(false, false, false, false, false, false, false);
      } else {
         return ck.init_ck().state_I() > start ? original : new Input(false, false, false, false, original.jump(), original.shift(), false);
      }
   }

   private static boolean load_Z_I(int fromSlot) {
      int handlerSlot = autismclient.util.aw.load_I(MC, fromSlot);
      return handlerSlot < 0 ? false : autismclient.util.av.init(MC, handlerSlot, 40, ContainerInput.SWAP);
   }

   private static boolean read(int fromSlot) {
      int from = autismclient.util.aw.load_I(MC, fromSlot);
      int offhand = autismclient.util.aw.load_I(MC, 40);
      if (from >= 0 && offhand >= 0) {
         boolean offhandHadItem = !MC.player.getOffhandItem().isEmpty();
         boolean ok = autismclient.util.av.init(MC, from, 0, ContainerInput.PICKUP);
         ok &= autismclient.util.av.init(MC, offhand, 0, ContainerInput.PICKUP);
         if (offhandHadItem) {
            ok &= autismclient.util.av.init(MC, from, 0, ContainerInput.PICKUP);
         }

         return ok;
      } else {
         return false;
      }
   }

   private boolean write(int fromSlot) {
      if (fromSlot >= 0 && fromSlot <= 8) {
         LocalPlayer player = MC.player;
         ClientPacketListener connection = MC.getConnection();
         if (connection != null && MC.gameMode != null) {
            ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
            int selected = player.getInventory().getSelectedSlot();
            if (selected != fromSlot) {
               connection.send(new ServerboundSetCarriedItemPacket(fromSlot));
            }

            connection.send(new ServerboundPlayerActionPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            if (selected != fromSlot) {
               connection.send(new ServerboundSetCarriedItemPacket(selected));
            }

            this.init_Item = player.getOffhandItem().getItem();
            this.update_J = System.currentTimeMillis();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean pop() {
      if (!this.bool("health")) {
         return true;
      }

      if (this.bool("missing-armor")) {
         for (EquipmentSlot slot : init_EquipmentSlotArr) {
            if (MC.player.getItemBySlot(slot).isEmpty()) {
               return true;
            }
         }
      }

      float health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      boolean safetyOperating = this.bool("safety") && (run() || tick_Z());
      float allowedDamage = health - (safetyOperating ? this.integer("safe-health-threshold") : this.integer("health-threshold"));
      if (allowedDamage <= 0.0F) {
         return true;
      }

      if (!this.bool("subtract-calculated-damage")) {
         allowedDamage = health;
      }

      float calculatedDamage = this.init_F_F(allowedDamage);
      if (calculatedDamage >= allowedDamage) {
         return true;
      }

      calculatedDamage = Math.max(calculatedDamage, this.load_F_F(allowedDamage));
      if (calculatedDamage >= allowedDamage) {
         return true;
      }

      calculatedDamage += this.init_F_2();
      return calculatedDamage >= allowedDamage;
   }

   private float init_F_F(float allowedDamage) {
      if (!this.bool("predict-explosions")) {
         return 0.0F;
      }

      float maxDamage = 0.0F;

      for (Entity entity : MC.level.entitiesForRendering()) {
         maxDamage = Math.max(maxDamage, this.init_F_Entity(entity, maxDamage));
         if (maxDamage >= allowedDamage) {
            return maxDamage;
         }
      }

      return maxDamage;
   }

   private float init_F_Entity(Entity entity, float floor) {
      if (!(entity instanceof EndCrystal) && !(entity instanceof PrimedTnt) && !(entity instanceof MinecartTNT) && !(entity instanceof Creeper)) {
         return 0.0F;
      }

      int now = ck.init_ck().state_I();
      if (now - this.stop >= 2) {
         this.stop = now;
         this.read_Map.clear();
      }

      Vec3 pos = entity.position();
      long key = (long)entity.getId() << 48 | ((long)(pos.x * 4.0) & 65535L) << 32 | ((long)(pos.y * 4.0) & 65535L) << 16 | (long)(pos.z * 4.0) & 65535L;
      Float cached = this.read_Map.get(key);
      if (cached != null) {
         return cached;
      }

      float damage = load_F_Entity(entity, floor);
      if (damage >= 0.0F) {
         this.read_Map.put(key, damage);
      }

      return damage;
   }

   private static float load_F_Entity(Entity entity, float floor) {
      if (entity instanceof EndCrystal) {
         return init(entity.position(), 6.0F, 12.0F, 144.0F, null, Explosion.getDefaultDamageSource(MC.level, entity), floor);
      } else if (entity instanceof PrimedTnt) {
         return init(entity.position().add(0.0, 0.0625, 0.0), 4.0F, 8.0F, 64.0F, null, Explosion.getDefaultDamageSource(MC.level, entity), floor);
      } else if (entity instanceof MinecartTNT cart) {
         float speed = (float)Math.min(Math.sqrt(cart.getDeltaMovement().horizontalDistanceSqr()), 5.0);
         float power = 4.0F + speed * 1.5F;
         float range = power * 2.0F;
         return init(cart.position(), power, range, range * range, null, Explosion.getDefaultDamageSource(MC.level, entity), floor);
      } else if (entity instanceof Creeper creeper) {
         float power = 3.0F * (creeper.isPowered() ? 2.0F : 1.0F);
         float range = power * 2.0F;
         return init(creeper.position(), power, range, range * range, null, Explosion.getDefaultDamageSource(MC.level, entity), floor);
      } else {
         return 0.0F;
      }
   }

   private float load_F_F(float allowedDamage) {
      if (!this.bool("predict-block-explosions")) {
         return 0.0F;
      }

      boolean bedsExplode = MC.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE) == BedRule.DESTROY_ON_USE;
      boolean anchorsExplode = !MC.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS);
      if (!bedsExplode && !anchorsExplode) {
         return 0.0F;
      }

      BlockPos center = MC.player.blockPosition();
      float maxDamage = 0.0F;

      for (BlockPos offset : init_List) {
         BlockPos pos = center.offset(offset);
         BlockState state = MC.level.getBlockState(pos);
         Block block = state.getBlock();
         boolean bedExplosion = bedsExplode && block instanceof BedBlock;
         boolean anchorExplosion = anchorsExplode && block instanceof RespawnAnchorBlock && state.getValue(RespawnAnchorBlock.CHARGE) > 0;
         if (bedExplosion || anchorExplosion) {
            List<BlockPos> exclude = bedExplosion ? List.of(pos, pos.relative(BedBlock.getConnectedDirection(state))) : List.of(pos);
            Vec3 posCenter = Vec3.atCenterOf(pos);
            maxDamage = Math.max(
               maxDamage, init(posCenter, 5.0F, 10.0F, 100.0F, exclude, MC.player.damageSources().badRespawnPointExplosion(posCenter), maxDamage)
            );
            if (maxDamage >= allowedDamage) {
               return maxDamage;
            }
         }
      }

      return maxDamage;
   }

   // Returns -1 when the rays were skipped because the result could not exceed floor.
   private static float init(Vec3 pos, float power, float range, float damageDistance, List<BlockPos> exclude, DamageSource source, float floor) {
      LocalPlayer player = MC.player;
      if (player.distanceToSqr(pos) > damageDistance) {
         return 0.0F;
      }

      double distanceDecay = 1.0 - Math.sqrt(player.distanceToSqr(pos)) / range;
      // Damage grows with exposure, so full exposure is an upper bound.
      if (auditExplosionDamage(1.0F, distanceDecay, range, source) <= floor) {
         return -1.0F;
      }

      float exposure = exclude == null ? ServerExplosion.getSeenPercent(pos, player) : init_F_LocalPlayer_2(player, pos, exclude);
      return auditExplosionDamage(exposure, distanceDecay, range, source);
   }

   private static float auditExplosionDamage(float exposure, double distanceDecay, float range, DamageSource source) {
      double pre1 = exposure * distanceDecay;
      double preprocessedDamage = (pre1 * pre1 + pre1) / 2.0 * 7.0 * range + 1.0;
      return preprocessedDamage == 0.0 ? 0.0F : init_F_DamageSource_F(source, (float)preprocessedDamage);
   }

   private static float init_F_LocalPlayer_2(LocalPlayer player, Vec3 source, List<BlockPos> exclude) {
      AABB box = player.getBoundingBox();
      double stepX = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
      double stepY = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
      double stepZ = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
      double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
      double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;
      if (!(stepX < 0.0) && !(stepY < 0.0) && !(stepZ < 0.0)) {
         int hits = 0;
         int totalRays = 0;

         for (double x = 0.0; x <= 1.0; x += stepX) {
            for (double y = 0.0; y <= 1.0; y += stepY) {
               for (double z = 0.0; z <= 1.0; z += stepZ) {
                  Vec3 sample = new Vec3(Mth.lerp(x, box.minX, box.maxX) + offsetX, Mth.lerp(y, box.minY, box.maxY), Mth.lerp(z, box.minZ, box.maxZ) + offsetZ);
                  BlockHitResult hit = MC.level.clip(new ClipContext(sample, source, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, player));
                  if (hit.getType() == Type.MISS || exclude.contains(hit.getBlockPos())) {
                     hits++;
                  }

                  totalRays++;
               }
            }
         }

         return (float)hits / totalRays;
      } else {
         return 0.0F;
      }
   }

   private float init_F_2() {
      LocalPlayer player = MC.player;
      if (this.bool("predict-fall-damage") && !(player.fallDistance <= 3.0)) {
         Module noFall = al.init_Module_Str("no-fall");
         if (noFall != null && noFall.isEnabled()) {
            return 0.0F;
         }

         if (this.bool("ignore-elytra") && player.isFallFlying() && player.hasPose(Pose.FALL_FLYING)) {
            return 0.0F;
         }

         BlockPos collision = AutoTotemModule.Inner_DataUtil.init(player, 20);
         float multiplier = init_F_BlockPos_LocalPlayer(collision, player);
         if (multiplier <= 0.0F) {
            return 0.0F;
         }

         float amount = ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(player.fallDistance, multiplier);
         return init_F_DamageSource_F(player.damageSources().fall(), amount);
      } else {
         return 0.0F;
      }
   }

   private static float init_F_BlockPos_LocalPlayer(BlockPos pos, LocalPlayer player) {
      if (pos == null) {
         return 1.0F;
      } else {
         Block block = MC.level.getBlockState(pos).getBlock();
         if (block == Blocks.WATER || block == Blocks.COBWEB || block == Blocks.POWDER_SNOW) {
            return 0.0F;
         } else if (block == Blocks.HAY_BLOCK || block == Blocks.HONEY_BLOCK) {
            return 0.2F;
         } else if (block == Blocks.SLIME_BLOCK) {
            return player.isSuppressingBounce() ? 1.0F : 0.0F;
         } else {
            return block instanceof BedBlock ? 0.5F : 1.0F;
         }
      }
   }

   private static float init_F_DamageSource_F(DamageSource source, float damage) {
      LocalPlayer player = MC.player;
      if (!((AutismEntityAccessor)player).autism$isInvulnerableToBase(source) && !player.isDeadOrDying()) {
         float amount = damage;
         if (player.getAbilities().invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0.0F;
         }

         if (source.scalesWithDifficulty()) {
            Difficulty difficulty = MC.level.getDifficulty();
            if (difficulty == Difficulty.PEACEFUL) {
               amount = 0.0F;
            } else if (difficulty == Difficulty.EASY) {
               amount = Math.min(amount / 2.0F + 1.0F, amount);
            } else if (difficulty == Difficulty.HARD) {
               amount = amount * 3.0F / 2.0F;
            }
         }

         if (amount == 0.0F) {
            return 0.0F;
         }

         if (source.is(DamageTypeTags.IS_FIRE) && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return 0.0F;
         }

         amount -= init_F_LocalPlayer(player, source, amount);
         if (amount == 0.0F) {
            return 0.0F;
         }

         amount = ((AutismLivingEntityAccessor)player).autism$getDamageAfterArmorAbsorb(source, amount);
         return ((AutismLivingEntityAccessor)player).autism$getDamageAfterMagicAbsorb(source, amount);
      } else {
         return 0.0F;
      }
   }

   private static float init_F_LocalPlayer(LocalPlayer player, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         ItemStack blocking = player.getItemBlockingWith();
         if (blocking == null) {
            return 0.0F;
         } else {
            BlocksAttacks blocksAttacks = blocking.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks == null) {
               return 0.0F;
            } else if (blocksAttacks.bypassedBy().isPresent() && blocksAttacks.bypassedBy().get().contains(source.typeHolder())) {
               return 0.0F;
            } else if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
               return 0.0F;
            } else {
               float horizontalAngle = 3.1415927F;
               Vec3 sourcePosition = source.getSourcePosition();
               if (sourcePosition != null) {
                  Vec3 view = init_Vec3_LocalPlayer(player);
                  Vec3 direction = sourcePosition.subtract(player.position());
                  direction = new Vec3(direction.x, 0.0, direction.z).normalize();
                  horizontalAngle = (float)Math.acos(Mth.clamp(direction.dot(view), -1.0, 1.0));
               }

               return blocksAttacks.resolveBlockedDamage(source, amount, horizontalAngle);
            }
         }
      }
   }

   private static Vec3 init_Vec3_LocalPlayer(LocalPlayer player) {
      float yaw = player.getYHeadRot() * 0.017453292F;
      return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
   }

   private static boolean tick_Z() {
      BlockPos feet = init_BlockPos();

      for (Direction direction : init_DirectionArr) {
         if (!init_Z_BlockPos_2(feet.relative(direction))) {
            return false;
         }
      }

      return true;
   }

   private static boolean run() {
      return init_Z_BlockPos_2(init_BlockPos());
   }

   private static boolean init_Z_BlockPos_2(BlockPos pos) {
      return MC.level.getBlockState(pos).getBlock().getExplosionResistance() >= 600.0F;
   }

   private static BlockPos init_BlockPos() {
      AABB box = MC.player.getBoundingBox();
      return new BlockPos(Mth.floor(Mth.lerp(0.5, box.minX, box.maxX)), Mth.ceil(box.minY), Mth.floor(Mth.lerp(0.5, box.minZ, box.maxZ)));
   }

   private static List<BlockPos> init_List_F(float radius) {
      List<BlockPos> out = new ArrayList<>();
      int r = Mth.ceil(radius);
      float rSq = radius * radius;

      for (int x = -r; x <= r; x++) {
         for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
               if (x * x + y * y + z * z <= rSq) {
                  out.add(new BlockPos(x, y, z));
               }
            }
         }
      }

      out.sort(Comparator.comparingInt(p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ()));
      return out;
   }

   private static final class Inner_DataUtil {
      private final LocalPlayer init_LocalPlayer;
      private double init_D;
      private double load;
      private double read;
      private double write;
      private double update;
      private double sync;
      private int init_I;

      private Inner_DataUtil(LocalPlayer player) {
         this.init_LocalPlayer = player;
         this.init_D = player.getX();
         this.load = player.getY();
         this.read = player.getZ();
         Vec3 delta = player.getDeltaMovement();
         this.write = delta.x;
         this.update = delta.y;
         this.sync = delta.z;
      }

      static BlockPos init(LocalPlayer player, int ticks) {
         AutoTotemModule.Inner_DataUtil sim = new AutoTotemModule.Inner_DataUtil(player);
         Vec3 rotationVec = player.getLookAngle();

         for (int i = 0; i < ticks; i++) {
            Vec3 start = new Vec3(sim.init_D, sim.load, sim.read);
            sim.init(rotationVec);
            Vec3 end = new Vec3(sim.init_D, sim.load, sim.read);
            AABB box = player.getDimensions(player.getPose()).makeBoundingBox(start).expandTowards(end.subtract(start));
            Optional<BlockPos> support = Module.MC.level.findSupportingBlock(player, box);
            if (support.isPresent()) {
               return support.get();
            }
         }

         return null;
      }

      private void init(Vec3 rotationVec) {
         if (this.init_LocalPlayer.isFallFlying()) {
            this.load(rotationVec);
         } else {
            this.init_V();
         }

         this.init_D = this.init_D + this.write;
         this.load = this.load + this.update;
         this.read = this.read + this.sync;
         this.init_I++;
      }

      private void init_V() {
         double gravity = this.init_LocalPlayer.getGravity();
         if (this.update <= 0.0 && this.init(MobEffects.SLOW_FALLING)) {
            this.update = this.update - Math.min(gravity, 0.01);
         } else {
            this.update -= gravity;
         }

         float speed = this.init_LocalPlayer.getSpeed() * 0.1F;
         if (speed > 0.0F) {
            Vec3 input = AutismEntityAccessor.autism$getInputVector(this.init_Vec3(), speed, this.init_LocalPlayer.getYRot());
            this.write = this.write + input.x;
            this.sync = this.sync + input.z;
         }

         this.write *= 0.9100000262260437;
         this.update *= 0.9800000190734863;
         this.sync *= 0.9100000262260437;
      }

      private void load(Vec3 rotationVec) {
         double gravity = 0.08;
         if (this.update <= 0.0 && this.init(MobEffects.SLOW_FALLING)) {
            gravity = 0.01;
         }

         double pitchRad = this.init_LocalPlayer.getXRot() * 0.017453292F;
         double k = Math.sqrt(rotationVec.x * rotationVec.x + rotationVec.z * rotationVec.z);
         double l = Math.sqrt(this.write * this.write + this.sync * this.sync);
         double m = rotationVec.length();
         double n = Mth.cos((float)pitchRad);
         n = n * n * Math.min(1.0, m / 0.4);
         Vec3 vec = new Vec3(this.write, this.update + gravity * (-1.0 + n * 0.75), this.sync);
         if (vec.y < 0.0 && k > 0.0) {
            double q = vec.y * -0.1 * n;
            vec = vec.add(rotationVec.x * q / k, q, rotationVec.z * q / k);
         }

         if (pitchRad < 0.0 && k > 0.0) {
            double q = l * -Mth.sin((float)pitchRad) * 0.04;
            vec = vec.add(-rotationVec.x * q / k, q * 3.2, -rotationVec.z * q / k);
         }

         if (k > 0.0) {
            vec = vec.add((rotationVec.x / k * l - vec.x) * 0.1, 0.0, (rotationVec.z / k * l - vec.z) * 0.1);
         }

         vec = vec.add(AutismEntityAccessor.autism$getInputVector(this.init_Vec3(), 0.02F, this.init_LocalPlayer.getYRot()));
         float blockSpeedFactor = ((AutismPlayerAccessor)this.init_LocalPlayer).autism$getBlockSpeedFactor();
         this.write = vec.x * 0.9900000095367432 * blockSpeedFactor;
         this.update = vec.y * 0.9800000190734863;
         this.sync = vec.z * 0.9900000095367432 * blockSpeedFactor;
      }

      private boolean init(Holder<MobEffect> effect) {
         MobEffectInstance instance = this.init_LocalPlayer.getEffect(effect);
         return instance != null && instance.getDuration() >= this.init_I;
      }

      private Vec3 init_Vec3() {
         Input keyPresses = this.init_LocalPlayer.input.keyPresses;
         float forward = (keyPresses.forward() ? 1.0F : 0.0F) - (keyPresses.backward() ? 1.0F : 0.0F);
         float sideways = (keyPresses.left() ? 1.0F : 0.0F) - (keyPresses.right() ? 1.0F : 0.0F);
         return new Vec3(sideways * 0.98, 0.0, forward * 0.98);
      }
   }
}
