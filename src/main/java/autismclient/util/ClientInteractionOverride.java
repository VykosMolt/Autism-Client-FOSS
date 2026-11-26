package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemFrameItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public final class ClientInteractionOverride {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static BlockHitResult init_BlockHitResult;
   private static InteractionHand init_InteractionHand;
   private static RotationMath.Rotation init_ce_DataUtil;
   private static EntityHitResult init_EntityHitResult;
   private static String init_Str;
   private static String load_Str;
   private static String read_Str;
   private static BlockHitResult load_BlockHitResult;
   private static int init_I;
   private static int load_I;
   private static boolean init_Z;
   private static BlockHitResult read_BlockHitResult;
   private static InteractionHand load_InteractionHand;
   private static BlockHitResult write_BlockHitResult;
   private static boolean load_Z;
   private static boolean read_Z;
   private static String write_Str;
   private static String update_Str;
   private static InteractionHand read_InteractionHand;
   private static ItemStack init_ItemStack = ItemStack.EMPTY;
   private static ItemStack load_ItemStack = ItemStack.EMPTY;
   private static int read_I = -1;
   private static boolean write_Z;
   private static long init_J;
   private static Player init_Player;
   private static InteractionHand write_InteractionHand;
   private static ItemStack read_ItemStack = ItemStack.EMPTY;
   private static boolean update_Z;
   private static boolean sync;
   private static boolean flush;
   private static boolean close;
   private static boolean open;
   private static boolean poll;
   private static Float init_Float;
   private static float init_F;
   private static boolean push;

   private ClientInteractionOverride() {
   }

   public static boolean init(BlockHitResult hit, InteractionHand hand) {
      if (hit != null
         && hand != null
         && !autismclient.modules.aD.read_Z()
         && init_BlockHitResult == null
         && init_EntityHitResult == null
         && load_BlockHitResult == null
         && !init_Z()
         && !open()) {
         init_BlockHitResult = hit;
         init_InteractionHand = hand;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(BlockHitResult hit, InteractionHand hand, RotationMath.Rotation look) {
      if (look != null && init(hit, hand)) {
         init_ce_DataUtil = look;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(String owner, EntityHitResult hit) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (init_EntityHitResult != null && !Objects.equals(owner, init_Str)) {
         return false;
      }

      if (!init(hit)) {
         return false;
      }

      init_Str = owner;
      return true;
   }

   public static boolean init(EntityHitResult hit) {
      if (hit != null && init_EntityHitResult != null && init_I < 2 && !autismclient.modules.aD.read_Z() && init_EntityHitResult.getEntity() == hit.getEntity()
         )
       {
         init_I++;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init();
         }

         return true;
      } else if (hit != null
         && !autismclient.modules.aD.read_Z()
         && init_BlockHitResult == null
         && init_EntityHitResult == null
         && load_BlockHitResult == null
         && !init_Z()
         && !open()) {
         init_EntityHitResult = hit;
         init_I = 1;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init();
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean init(BlockHitResult miss) {
      return init(miss, false);
   }

   public static boolean init(String owner, BlockHitResult miss, boolean tap) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (!init(miss, tap)) {
         return false;
      }

      load_Str = owner;
      return true;
   }

   public static void init_V(String owner) {
      if (!AutismLiteVariant.enabled() && owner != null) {
         if (owner.equals(init_Str)) {
            init_EntityHitResult = null;
            init_I = 0;
            init_Str = null;
            CombatAttackDispatcher.read();
         }

         if (owner.equals(load_Str)) {
            load_BlockHitResult = null;
            load_Str = null;
         }

         if (owner.equals(read_Str)) {
            sync = false;
            load_I = 0;
            read_Str = null;
            CombatAttackDispatcher.load();
            if (close && init_Minecraft != null && init_Minecraft.options != null) {
               KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
               ((AutismKeyMappingAccessor)init_Minecraft.options.keyAttack).autism$setClickCount(0);
               KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
               close = false;
            }
         }

         load_V();
      }
   }

   public static boolean init(BlockHitResult miss, boolean tap) {
      if (miss != null
         && !autismclient.modules.aD.read_Z()
         && init_BlockHitResult == null
         && init_EntityHitResult == null
         && load_BlockHitResult == null
         && !init_Z()
         && !open()) {
         load_BlockHitResult = miss;
         init_Z = tap;
         return true;
      } else {
         return false;
      }
   }

   public static boolean load(BlockHitResult ray) {
      if (ray == null
         || init_Minecraft == null
         || init_Minecraft.player == null
         || init_Minecraft.options == null
         || autismclient.modules.aD.read_Z()
         || init_BlockHitResult != null
         || init_EntityHitResult != null
         || load_BlockHitResult != null
         || init_Minecraft.gui.screen() != null
         || init_Minecraft.gui.overlay() != null) {
         return false;
      } else if (!load_Z && read_BlockHitResult == null) {
         write_BlockHitResult = ray;
         return true;
      } else {
         return false;
      }
   }

   public static boolean load(BlockHitResult ray, InteractionHand hand) {
      return init(ray, hand, (String)null);
   }

   private static boolean init(BlockHitResult ray, InteractionHand hand, String guard) {
      if (ray == null
         || hand == null
         || init_Minecraft == null
         || init_Minecraft.player == null
         || init_Minecraft.options == null
         || autismclient.modules.aD.read_Z()
         || init_BlockHitResult != null
         || init_EntityHitResult != null
         || load_BlockHitResult != null
         || init_Minecraft.gui.screen() != null
         || init_Minecraft.gui.overlay() != null) {
         return false;
      }

      if (!read_Z && write_BlockHitResult == null) {
         if (init_Str() != null && !init_Str().equals(guard)) {
            return false;
         }

         read_BlockHitResult = ray;
         load_InteractionHand = hand;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(String owner, InteractionHand hand) {
      if (owner != null && init_Minecraft != null && init_Minecraft.player != null) {
         String existing = init_Str();
         if ((!load_Z && read_BlockHitResult == null || existing != null) && (existing == null || existing.equals(owner))) {
            if (!init(BlockHitResult.miss(init_Minecraft.player.getEyePosition(), Direction.DOWN, init_Minecraft.player.blockPosition()), hand, owner)) {
               return false;
            }

            write_Str = owner;
            if (existing == null) {
               write_Z = false;
            }

            read_InteractionHand = hand;
            init_ItemStack = init_Minecraft.player.getItemInHand(hand);
            load_ItemStack = init_Minecraft.player.getMainHandItem();
            read_I = init_Minecraft.player.getInventory().getSelectedSlot();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static String init_Str() {
      return write_Str != null ? write_Str : update_Str;
   }

   public static boolean init_Z(String owner) {
      return owner != null && owner.equals(init_Str()) && write_Z;
   }

   public static void load(String owner) {
      if (owner != null && owner.equals(init_Str())) {
         read_V();
      }
   }

   public static void init(InteractionHand hand, ItemStack item) {
      if (init_Minecraft != null && init_Minecraft.player != null) {
         init_Player = init_Minecraft.player;
         write_InteractionHand = hand;
         read_ItemStack = item;
      }
   }

   public static void init_V() {
      Player player = init_Player;
      InteractionHand hand = write_InteractionHand;
      ItemStack item = read_ItemStack;
      init_Player = null;
      write_InteractionHand = null;
      read_ItemStack = ItemStack.EMPTY;
      if (player != null
         && init_Minecraft != null
         && init_Minecraft.player == player
         && init_Minecraft.gameMode != null
         && init_Minecraft.getConnection() != null
         && (init_Minecraft.options == null || !KeyMappingControl.of(init_Minecraft.options.keyUse).autism$physicalForBody())
         && (!player.isUsingItem() || player.getUsedItemHand() == hand && ItemStack.isSameItem(player.getUseItem(), item))) {
         init_Minecraft.gameMode.releaseUsingItem(player);
      }
   }

   public static long init_J() {
      return init_J;
   }

   public static void load_V() {
      push = true;
   }

   private static void flush_V() {
      if (push && init_Minecraft != null && init_Minecraft.options != null) {
         push = false;
         boolean attackOwned = read_Z || VirtualInputDispatcher.init_Z_Z(false) || !AutismLiteVariant.enabled() && autismclient.util.clicker.KeyClickOutput.init_Z_Z(false);
         boolean useOwned = load_Z || VirtualInputDispatcher.init_Z_Z(true) || !AutismLiteVariant.enabled() && autismclient.util.clicker.KeyClickOutput.init_Z_Z(true);
         if (!attackOwned) {
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
         }

         if (!useOwned) {
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
         }
      }
   }

   public static void read_V() {
      if (!AutismLiteVariant.enabled()) {
         SilentMiningCoordinator.init();
      }

      write_Str = null;
      update_Str = null;
      read_InteractionHand = null;
      init_ItemStack = ItemStack.EMPTY;
      load_ItemStack = ItemStack.EMPTY;
      read_I = -1;
      write_Z = false;
      read_BlockHitResult = null;
      load_InteractionHand = null;
      write_BlockHitResult = null;
      if (init_Minecraft != null && init_Minecraft.options != null) {
         if (read_Z) {
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
            read_Z = false;
         }

         if (load_Z) {
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(false);
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
            load_Z = false;
         }

         if (init_Float != null && init_Minecraft.player != null) {
            init_Minecraft.player.setYRot(init_Float);
            init_Minecraft.player.setXRot(init_F);
            init_Float = null;
         }
      } else {
         load_Z = false;
         read_Z = false;
      }
   }

   public static boolean init_Z() {
      return load_Z || read_Z;
   }

   private static boolean open() {
      return read_BlockHitResult != null || write_BlockHitResult != null;
   }

   public static boolean load_Z() {
      ItemStack main = init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getMainHandItem() : ItemStack.EMPTY;
      if (main.isEmpty()) {
         return false;
      } else if (main.getItem() instanceof BlockItem) {
         return true;
      } else if (main.getUseAnimation() != ItemUseAnimation.NONE) {
         return true;
      } else if (main.has(DataComponents.EQUIPPABLE)) {
         return true;
      } else {
         return !main.is(Items.EXPERIENCE_BOTTLE)
               && !main.is(Items.ENDER_PEARL)
               && !main.is(Items.SNOWBALL)
               && !main.is(Items.EGG)
               && !main.is(Items.FIREWORK_ROCKET)
               && !main.is(Items.FISHING_ROD)
               && !(main.getItem() instanceof BucketItem)
               && !main.is(Items.WIND_CHARGE)
               && !main.is(Items.FIRE_CHARGE)
               && !main.is(Items.ENDER_EYE)
               && !main.is(Items.SPLASH_POTION)
               && !main.is(Items.LINGERING_POTION)
               && !main.is(Items.FLINT_AND_STEEL)
               && !main.is(Items.BONE_MEAL)
               && !main.is(Items.GLASS_BOTTLE)
               && !main.is(Items.CARROT_ON_A_STICK)
            ? main.getItem() instanceof SpawnEggItem
               || main.getItem() instanceof BoatItem
               || main.getItem() instanceof MinecartItem
               || main.getItem() instanceof ItemFrameItem
               || Compat263.isShovel(main.getItem())
               || Compat263.isAxe(main.getItem())
               || Compat263.isHoe(main.getItem())
               || main.getItem() instanceof ShearsItem
            : true;
      }
   }

   public static void write_V() {
      if (update_Str != null
         && init_Minecraft != null
         && init_Minecraft.player != null
         && (
            write_Z
               || init_Minecraft.player.isUsingItem()
                  && init_Minecraft.player.getUsedItemHand() == read_InteractionHand
                  && ItemStack.isSameItem(init_Minecraft.player.getUseItem(), init_ItemStack)
         )) {
         init(read_InteractionHand, init_ItemStack);
      }

      init_BlockHitResult = null;
      init_InteractionHand = null;
      init_ce_DataUtil = null;
      init_EntityHitResult = null;
      init_I = 0;
      read_Str = null;
      load_Str = null;
      init_Str = null;
      if (!AutismLiteVariant.enabled()) {
         CombatAttackDispatcher.write();
      }

      if (!AutismLiteVariant.enabled()) {
         QueuedCombatAction.load();
      }

      load_I = 0;
      load_BlockHitResult = null;
      update_Z = false;
      sync = false;
      read_V();
      if (init_Minecraft != null && init_Minecraft.options != null) {
         if (flush) {
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)init_Minecraft.options.keyUse).autism$setClickCount(0);
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
            flush = false;
         }

         if (close) {
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)init_Minecraft.options.keyAttack).autism$setClickCount(0);
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
            close = false;
         }
      }

      if (init_Float != null && init_Minecraft != null && init_Minecraft.player != null) {
         init_Minecraft.player.setYRot(init_Float);
         init_Minecraft.player.setXRot(init_F);
         init_Float = null;
      }
   }

   public static boolean read_Z() {
      return update_Z;
   }

   public static boolean write_Z() {
      if (!update_Z) {
         return false;
      }

      update_Z = false;
      if (!AutismLiteVariant.enabled() && !QueuedCombatAction.init_Z()) {
         return false;
      }

      if (update_Str != null) {
         ItemStack current = read_InteractionHand != null && init_Minecraft.player != null
            ? init_Minecraft.player.getItemInHand(read_InteractionHand)
            : ItemStack.EMPTY;
         boolean sameMain = init_Minecraft.player != null
            && (
               init_Minecraft.player.getMainHandItem().isEmpty() && load_ItemStack.isEmpty()
                  || ItemStack.isSameItem(init_Minecraft.player.getMainHandItem(), load_ItemStack)
            );
         if (init_Minecraft.player == null
            || init_Minecraft.gameMode == null
            || init_Minecraft.gameMode.isDestroying()
            || init_Minecraft.player.isHandsBusy()
            || init_Minecraft.level == null
            || !current.isItemEnabled(init_Minecraft.level.enabledFeatures())
            || !init_Minecraft.player.getMainHandItem().isItemEnabled(init_Minecraft.level.enabledFeatures())
            || init_Minecraft.player.getInventory().getSelectedSlot() != read_I
            || !sameMain
            || !ItemStack.isSameItem(current, init_ItemStack)
            || current.getUseAnimation() != ItemUseAnimation.BLOCK
            || init_Minecraft.player.getCooldowns().isOnCooldown(current)) {
            init_J++;
            load(update_Str);
            return false;
         }

         write_Z = true;
      }

      return true;
   }

   public static boolean update_Z() {
      return sync;
   }

   public static boolean sync_Z() {
      if (!sync) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && !QueuedCombatAction.init_Z()) {
         return false;
      }

      if (load_I > 0) {
         load_I--;
      } else {
         sync = false;
      }

      return true;
   }

   public static boolean flush_Z() {
      return init_BlockHitResult != null || update_Z || flush || load_Z || read_BlockHitResult != null;
   }

   public static boolean close_Z() {
      return init_EntityHitResult != null || load_BlockHitResult != null || sync || close || read_Z || write_BlockHitResult != null;
   }

   public static void update_V() {
      if (!AutismLiteVariant.enabled() && autismclient.ai.aP.load_Z()) {
         write_V();
      } else if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.options != null && !autismclient.modules.aD.read_Z()) {
         flush_V();
         if (!AutismLiteVariant.enabled()) {
            QueuedCombatAction.init_V();
         }

         BlockHitResult useHit = init_BlockHitResult;
         RotationMath.Rotation useLook = init_ce_DataUtil;
         EntityHitResult attackHit = init_EntityHitResult;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init_V(attackHit == null ? null : attackHit.getEntity());
         }

         read_Str = attackHit != null ? init_Str : load_Str;
         load_Str = null;
         init_Str = null;
         int attackClicks = Math.max(1, init_I);
         init_I = 0;
         BlockHitResult missHit = load_BlockHitResult;
         BlockHitResult holdAttackRay = write_BlockHitResult;
         BlockHitResult holdUseRay = read_BlockHitResult;
         String guard = write_Str;
         if (useHit == null && attackHit == null && missHit == null && holdAttackRay == null && holdUseRay == null) {
            if (read_Z || load_Z) {
               read_V();
            }
         } else if (init_Minecraft.gui.screen() == null && init_Minecraft.gui.overlay() == null) {
            init_BlockHitResult = null;
            init_InteractionHand = null;
            init_ce_DataUtil = null;
            init_EntityHitResult = null;
            load_BlockHitResult = null;
            write_BlockHitResult = null;
            read_BlockHitResult = null;
            load_InteractionHand = null;
            write_Str = null;
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            if (!AutismLiteVariant.enabled() && "kill-aura".equals(read_Str) && attackHit != null) {
               attackHit = autismclient.modules.KillAuraModule.init_EntityHitResult_EntityHitResult_ch_CachePool(attackHit, wire);
               if (attackHit == null) {
                  read_Str = null;
                  return;
               }
            }

            init_Float = init_Minecraft.player.getYRot();
            init_F = init_Minecraft.player.getXRot();
            if (wire.initialized()) {
               init_Minecraft.player.setYRot(wire.currentYaw());
               init_Minecraft.player.setXRot(wire.currentPitch());
            }

            if (attackHit != null) {
               init_Minecraft.hitResult = attackHit;
               close_V();
               init(init_Minecraft.options.keyUse, true);
               KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(true);

               for (int i = 1; i < attackClicks; i++) {
                  KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
                  KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(true);
               }

               load_I = attackClicks - 1;
               close = true;
               sync = true;
            } else if (missHit != null) {
               boolean tap = init_Z;
               init_Z = false;
               init_Minecraft.hitResult = missHit;
               close_V();
               init(init_Minecraft.options.keyUse, true);
               KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(true);
               if (tap) {
                  KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
               }

               close = true;
               sync = true;
            } else if (useHit != null) {
               if (useLook != null) {
                  PlacementTickStamp.init_V();
                  init_Minecraft.player.setYRot(useLook.yaw());
                  init_Minecraft.player.setXRot(useLook.pitch());
               }

               init_Minecraft.hitResult = useHit;
               close_V();
               init(init_Minecraft.options.keyAttack, false);
               KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(true);
               flush = true;
               update_Z = true;
            } else if (holdAttackRay != null) {
               init_Minecraft.hitResult = holdAttackRay;
               close_V();
               init(init_Minecraft.options.keyUse, true);
               if (!read_Z) {
                  KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
                  KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(true);
                  read_Z = true;
                  sync = true;
               }
            } else {
               init_Minecraft.hitResult = holdUseRay;
               update_Str = guard;
               close_V();
               init(init_Minecraft.options.keyAttack, false);
               if (!load_Z) {
                  KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(false);
                  KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(true);
                  load_Z = true;
               }

               update_Z = true;
            }
         } else {
            write_V();
         }
      } else {
         write_V();
      }
   }

   private static void init(KeyMapping key, boolean use) {
      KeyMappingControl.of(key).autism$simulatePress(false);
      if (use) {
         open = true;
      } else {
         poll = true;
      }
   }

   private static void close_V() {
      while (init_Minecraft.options.keyUse.consumeClick()) {
      }

      while (init_Minecraft.options.keyAttack.consumeClick()) {
      }

      while (init_Minecraft.options.keyPickItem.consumeClick()) {
      }
   }

   public static void sync_V() {
      if (!AutismLiteVariant.enabled()) {
         QueuedCombatAction.load();
      }

      read_Str = null;
      init_EntityHitResult = null;
      init_I = 0;
      load_Str = null;
      init_Str = null;
      load_BlockHitResult = null;
      init_Z = false;
      if (!AutismLiteVariant.enabled()) {
         CombatAttackDispatcher.write();
      }

      if (init_Minecraft != null && init_Minecraft.options != null) {
         if (flush) {
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)init_Minecraft.options.keyUse).autism$setClickCount(0);
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
            flush = false;
         }

         if (close) {
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)init_Minecraft.options.keyAttack).autism$setClickCount(0);
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
            close = false;
         }

         if (open) {
            KeyMappingControl.of(init_Minecraft.options.keyUse).autism$resetPressedState();
            open = false;
         }

         if (poll) {
            KeyMappingControl.of(init_Minecraft.options.keyAttack).autism$resetPressedState();
            poll = false;
         }

         if (update_Str != null) {
            ((AutismKeyMappingAccessor)init_Minecraft.options.keyUse).autism$setClickCount(0);
         }

         update_Z = false;
         sync = false;
         load_I = 0;
         if (init_Float != null && init_Minecraft.player != null) {
            init_Minecraft.player.setYRot(init_Float);
            init_Minecraft.player.setXRot(init_F);
            init_Float = null;
         }

         flush_V();
      } else {
         flush = false;
         close = false;
         open = false;
         poll = false;
         load_Z = false;
         read_Z = false;
         write_Str = null;
         update_Str = null;
         update_Z = false;
         sync = false;
         load_I = 0;
         init_Float = null;
      }
   }
}
