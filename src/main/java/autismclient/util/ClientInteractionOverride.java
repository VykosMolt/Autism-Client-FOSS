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
   private static final Minecraft MC = Minecraft.getInstance();
   private static BlockHitResult pendingUseHit;
   private static InteractionHand pendingUseHand;
   private static RotationMath.Rotation pendingUseRotation;
   private static EntityHitResult pendingAttackHit;
   private static String pendingAttackOwner;
   private static String pendingMissOwner;
   private static String activeAttackOwner;
   private static BlockHitResult pendingMissHit;
   private static int pendingAttackClicks;
   private static int remainingAttackClicks;
   private static boolean pendingMissTap;
   private static BlockHitResult pendingHoldUseRay;
   private static InteractionHand pendingHoldUseHand;
   private static BlockHitResult pendingHoldAttackRay;
   private static boolean holdingUse;
   private static boolean holdingAttack;
   private static String pendingGuardOwner;
   private static String activeGuardOwner;
   private static InteractionHand guardHand;
   private static ItemStack guardItem = ItemStack.EMPTY;
   private static ItemStack guardMainHandItem = ItemStack.EMPTY;
   private static int guardHotbarSlot = -1;
   private static boolean guardUseDispatched;
   private static long guardAbortedCount;
   private static Player deferredReleasePlayer;
   private static InteractionHand deferredReleaseHand;
   private static ItemStack deferredReleaseItem = ItemStack.EMPTY;
   private static boolean useDispatchPending;
   private static boolean attackDispatchPending;
   private static boolean simulatedUsePress;
   private static boolean simulatedAttackPress;
   private static boolean suppressedUseKey;
   private static boolean suppressedAttackKey;
   private static Float savedPlayerYaw;
   private static float savedPlayerPitch;
   private static boolean deferredKeyResetPending;

   private ClientInteractionOverride() {
   }

   public static boolean queueUseBlock(BlockHitResult hit, InteractionHand hand) {
      if (hit != null
         && hand != null
         && !autismclient.modules.aD.read_Z()
         && pendingUseHit == null
         && pendingAttackHit == null
         && pendingMissHit == null
         && !isHoldingAnyKey()
         && !hasPendingHoldRay()) {
         pendingUseHit = hit;
         pendingUseHand = hand;
         return true;
      } else {
         return false;
      }
   }

   public static boolean queueUseBlock(BlockHitResult hit, InteractionHand hand, RotationMath.Rotation look) {
      if (look != null && queueUseBlock(hit, hand)) {
         pendingUseRotation = look;
         return true;
      } else {
         return false;
      }
   }

   public static boolean queueAttackEntity(String owner, EntityHitResult hit) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (pendingAttackHit != null && !Objects.equals(owner, pendingAttackOwner)) {
         return false;
      }

      if (!queueAttackEntity(hit)) {
         return false;
      }

      pendingAttackOwner = owner;
      return true;
   }

   public static boolean queueAttackEntity(EntityHitResult hit) {
      if (hit != null && pendingAttackHit != null && pendingAttackClicks < 2 && !autismclient.modules.aD.read_Z() && pendingAttackHit.getEntity() == hit.getEntity()
         )
       {
         pendingAttackClicks++;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init();
         }

         return true;
      } else if (hit != null
         && !autismclient.modules.aD.read_Z()
         && pendingUseHit == null
         && pendingAttackHit == null
         && pendingMissHit == null
         && !isHoldingAnyKey()
         && !hasPendingHoldRay()) {
         pendingAttackHit = hit;
         pendingAttackClicks = 1;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init();
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean queueAttackMiss(BlockHitResult miss) {
      return queueAttackMiss(miss, false);
   }

   public static boolean queueAttackMiss(String owner, BlockHitResult miss, boolean tap) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (!queueAttackMiss(miss, tap)) {
         return false;
      }

      pendingMissOwner = owner;
      return true;
   }

   public static void cancelAttackOwner(String owner) {
      if (!AutismLiteVariant.enabled() && owner != null) {
         if (owner.equals(pendingAttackOwner)) {
            pendingAttackHit = null;
            pendingAttackClicks = 0;
            pendingAttackOwner = null;
            CombatAttackDispatcher.read();
         }

         if (owner.equals(pendingMissOwner)) {
            pendingMissHit = null;
            pendingMissOwner = null;
         }

         if (owner.equals(activeAttackOwner)) {
            attackDispatchPending = false;
            remainingAttackClicks = 0;
            activeAttackOwner = null;
            CombatAttackDispatcher.load();
            if (simulatedAttackPress && MC != null && MC.options != null) {
               KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
               ((AutismKeyMappingAccessor)MC.options.keyAttack).autism$setClickCount(0);
               KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
               simulatedAttackPress = false;
            }
         }

         requestDeferredKeyReset();
      }
   }

   public static boolean queueAttackMiss(BlockHitResult miss, boolean tap) {
      if (miss != null
         && !autismclient.modules.aD.read_Z()
         && pendingUseHit == null
         && pendingAttackHit == null
         && pendingMissHit == null
         && !isHoldingAnyKey()
         && !hasPendingHoldRay()) {
         pendingMissHit = miss;
         pendingMissTap = tap;
         return true;
      } else {
         return false;
      }
   }

   public static boolean queueHoldAttack(BlockHitResult ray) {
      if (ray == null
         || MC == null
         || MC.player == null
         || MC.options == null
         || autismclient.modules.aD.read_Z()
         || pendingUseHit != null
         || pendingAttackHit != null
         || pendingMissHit != null
         || MC.gui.screen() != null
         || MC.gui.overlay() != null) {
         return false;
      } else if (!holdingUse && pendingHoldUseRay == null) {
         pendingHoldAttackRay = ray;
         return true;
      } else {
         return false;
      }
   }

   public static boolean queueHoldUse(BlockHitResult ray, InteractionHand hand) {
      return queueHoldUseInternal(ray, hand, (String)null);
   }

   private static boolean queueHoldUseInternal(BlockHitResult ray, InteractionHand hand, String guard) {
      if (ray == null
         || hand == null
         || MC == null
         || MC.player == null
         || MC.options == null
         || autismclient.modules.aD.read_Z()
         || pendingUseHit != null
         || pendingAttackHit != null
         || pendingMissHit != null
         || MC.gui.screen() != null
         || MC.gui.overlay() != null) {
         return false;
      }

      if (!holdingAttack && pendingHoldAttackRay == null) {
         if (getGuardOwner() != null && !getGuardOwner().equals(guard)) {
            return false;
         }

         pendingHoldUseRay = ray;
         pendingHoldUseHand = hand;
         return true;
      } else {
         return false;
      }
   }

   public static boolean queueShieldGuard(String owner, InteractionHand hand) {
      if (owner != null && MC != null && MC.player != null) {
         String existing = getGuardOwner();
         if ((!holdingUse && pendingHoldUseRay == null || existing != null) && (existing == null || existing.equals(owner))) {
            if (!queueHoldUseInternal(BlockHitResult.miss(MC.player.getEyePosition(), Direction.DOWN, MC.player.blockPosition()), hand, owner)) {
               return false;
            }

            pendingGuardOwner = owner;
            if (existing == null) {
               guardUseDispatched = false;
            }

            guardHand = hand;
            guardItem = MC.player.getItemInHand(hand);
            guardMainHandItem = MC.player.getMainHandItem();
            guardHotbarSlot = MC.player.getInventory().getSelectedSlot();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static String getGuardOwner() {
      return pendingGuardOwner != null ? pendingGuardOwner : activeGuardOwner;
   }

   public static boolean isGuardDispatchedFor(String owner) {
      return owner != null && owner.equals(getGuardOwner()) && guardUseDispatched;
   }

   public static void releaseGuard(String owner) {
      if (owner != null && owner.equals(getGuardOwner())) {
         releaseHoldOverrides();
      }
   }

   public static void scheduleDeferredReleaseUsingItem(InteractionHand hand, ItemStack item) {
      if (MC != null && MC.player != null) {
         deferredReleasePlayer = MC.player;
         deferredReleaseHand = hand;
         deferredReleaseItem = item;
      }
   }

   public static void flushDeferredReleaseUsingItem() {
      Player player = deferredReleasePlayer;
      InteractionHand hand = deferredReleaseHand;
      ItemStack item = deferredReleaseItem;
      deferredReleasePlayer = null;
      deferredReleaseHand = null;
      deferredReleaseItem = ItemStack.EMPTY;
      if (player != null
         && MC != null
         && MC.player == player
         && MC.gameMode != null
         && MC.getConnection() != null
         && (MC.options == null || !KeyMappingControl.of(MC.options.keyUse).autism$physicalForBody())
         && (!player.isUsingItem() || player.getUsedItemHand() == hand && ItemStack.isSameItem(player.getUseItem(), item))) {
         MC.gameMode.releaseUsingItem(player);
      }
   }

   public static long getGuardAbortedCount() {
      return guardAbortedCount;
   }

   public static void requestDeferredKeyReset() {
      deferredKeyResetPending = true;
   }

   private static void applyDeferredKeyReset() {
      if (deferredKeyResetPending && MC != null && MC.options != null) {
         deferredKeyResetPending = false;
         boolean attackOwned = holdingAttack || VirtualInputDispatcher.init_Z_Z(false) || !AutismLiteVariant.enabled() && autismclient.util.clicker.KeyClickOutput.init_Z_Z(false);
         boolean useOwned = holdingUse || VirtualInputDispatcher.init_Z_Z(true) || !AutismLiteVariant.enabled() && autismclient.util.clicker.KeyClickOutput.init_Z_Z(true);
         if (!attackOwned) {
            KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
         }

         if (!useOwned) {
            KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
         }
      }
   }

   public static void releaseHoldOverrides() {
      if (!AutismLiteVariant.enabled()) {
         SilentMiningCoordinator.init();
      }

      pendingGuardOwner = null;
      activeGuardOwner = null;
      guardHand = null;
      guardItem = ItemStack.EMPTY;
      guardMainHandItem = ItemStack.EMPTY;
      guardHotbarSlot = -1;
      guardUseDispatched = false;
      pendingHoldUseRay = null;
      pendingHoldUseHand = null;
      pendingHoldAttackRay = null;
      if (MC != null && MC.options != null) {
         if (holdingAttack) {
            KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
            KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
            holdingAttack = false;
         }

         if (holdingUse) {
            KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(false);
            KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
            holdingUse = false;
         }

         if (savedPlayerYaw != null && MC.player != null) {
            MC.player.setYRot(savedPlayerYaw);
            MC.player.setXRot(savedPlayerPitch);
            savedPlayerYaw = null;
         }
      } else {
         holdingUse = false;
         holdingAttack = false;
      }
   }

   public static boolean isHoldingAnyKey() {
      return holdingUse || holdingAttack;
   }

   private static boolean hasPendingHoldRay() {
      return pendingHoldUseRay != null || pendingHoldAttackRay != null;
   }

   public static boolean isMainHandUseableItem() {
      ItemStack main = MC != null && MC.player != null ? MC.player.getMainHandItem() : ItemStack.EMPTY;
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

   public static void clearAll() {
      if (activeGuardOwner != null
         && MC != null
         && MC.player != null
         && (
            guardUseDispatched
               || MC.player.isUsingItem()
                  && MC.player.getUsedItemHand() == guardHand
                  && ItemStack.isSameItem(MC.player.getUseItem(), guardItem)
         )) {
         scheduleDeferredReleaseUsingItem(guardHand, guardItem);
      }

      pendingUseHit = null;
      pendingUseHand = null;
      pendingUseRotation = null;
      pendingAttackHit = null;
      pendingAttackClicks = 0;
      activeAttackOwner = null;
      pendingMissOwner = null;
      pendingAttackOwner = null;
      if (!AutismLiteVariant.enabled()) {
         CombatAttackDispatcher.write();
      }

      if (!AutismLiteVariant.enabled()) {
         QueuedCombatAction.load();
      }

      remainingAttackClicks = 0;
      pendingMissHit = null;
      useDispatchPending = false;
      attackDispatchPending = false;
      releaseHoldOverrides();
      if (MC != null && MC.options != null) {
         if (simulatedUsePress) {
            KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)MC.options.keyUse).autism$setClickCount(0);
            KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
            simulatedUsePress = false;
         }

         if (simulatedAttackPress) {
            KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)MC.options.keyAttack).autism$setClickCount(0);
            KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
            simulatedAttackPress = false;
         }
      }

      if (savedPlayerYaw != null && MC != null && MC.player != null) {
         MC.player.setYRot(savedPlayerYaw);
         MC.player.setXRot(savedPlayerPitch);
         savedPlayerYaw = null;
      }
   }

   public static boolean isUseDispatchPending() {
      return useDispatchPending;
   }

   public static boolean consumeUseDispatch() {
      if (!useDispatchPending) {
         return false;
      }

      useDispatchPending = false;
      if (!AutismLiteVariant.enabled() && !QueuedCombatAction.init_Z()) {
         return false;
      }

      if (activeGuardOwner != null) {
         ItemStack current = guardHand != null && MC.player != null
            ? MC.player.getItemInHand(guardHand)
            : ItemStack.EMPTY;
         boolean sameMain = MC.player != null
            && (
               MC.player.getMainHandItem().isEmpty() && guardMainHandItem.isEmpty()
                  || ItemStack.isSameItem(MC.player.getMainHandItem(), guardMainHandItem)
            );
         if (MC.player == null
            || MC.gameMode == null
            || MC.gameMode.isDestroying()
            || MC.player.isHandsBusy()
            || MC.level == null
            || !current.isItemEnabled(MC.level.enabledFeatures())
            || !MC.player.getMainHandItem().isItemEnabled(MC.level.enabledFeatures())
            || MC.player.getInventory().getSelectedSlot() != guardHotbarSlot
            || !sameMain
            || !ItemStack.isSameItem(current, guardItem)
            || current.getUseAnimation() != ItemUseAnimation.BLOCK
            || MC.player.getCooldowns().isOnCooldown(current)) {
            guardAbortedCount++;
            releaseGuard(activeGuardOwner);
            return false;
         }

         guardUseDispatched = true;
      }

      return true;
   }

   public static boolean isAttackDispatchPending() {
      return attackDispatchPending;
   }

   public static boolean consumeAttackDispatch() {
      if (!attackDispatchPending) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && !QueuedCombatAction.init_Z()) {
         return false;
      }

      if (remainingAttackClicks > 0) {
         remainingAttackClicks--;
      } else {
         attackDispatchPending = false;
      }

      return true;
   }

   public static boolean ownsUseInput() {
      return pendingUseHit != null || useDispatchPending || simulatedUsePress || holdingUse || pendingHoldUseRay != null;
   }

   public static boolean ownsAttackInput() {
      return pendingAttackHit != null || pendingMissHit != null || attackDispatchPending || simulatedAttackPress || holdingAttack || pendingHoldAttackRay != null;
   }

   public static void applyBeforeInputTick() {
      if (!AutismLiteVariant.enabled() && autismclient.ai.aP.load_Z()) {
         clearAll();
      } else if (MC != null && MC.player != null && MC.options != null && !autismclient.modules.aD.read_Z()) {
         applyDeferredKeyReset();
         if (!AutismLiteVariant.enabled()) {
            QueuedCombatAction.init_V();
         }

         BlockHitResult useHit = pendingUseHit;
         RotationMath.Rotation useLook = pendingUseRotation;
         EntityHitResult attackHit = pendingAttackHit;
         if (!AutismLiteVariant.enabled()) {
            CombatAttackDispatcher.init_V(attackHit == null ? null : attackHit.getEntity());
         }

         activeAttackOwner = attackHit != null ? pendingAttackOwner : pendingMissOwner;
         pendingMissOwner = null;
         pendingAttackOwner = null;
         int attackClicks = Math.max(1, pendingAttackClicks);
         pendingAttackClicks = 0;
         BlockHitResult missHit = pendingMissHit;
         BlockHitResult holdAttackRay = pendingHoldAttackRay;
         BlockHitResult holdUseRay = pendingHoldUseRay;
         String guard = pendingGuardOwner;
         if (useHit == null && attackHit == null && missHit == null && holdAttackRay == null && holdUseRay == null) {
            if (holdingAttack || holdingUse) {
               releaseHoldOverrides();
            }
         } else if (MC.gui.screen() == null && MC.gui.overlay() == null) {
            pendingUseHit = null;
            pendingUseHand = null;
            pendingUseRotation = null;
            pendingAttackHit = null;
            pendingMissHit = null;
            pendingHoldAttackRay = null;
            pendingHoldUseRay = null;
            pendingHoldUseHand = null;
            pendingGuardOwner = null;
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            if (!AutismLiteVariant.enabled() && "kill-aura".equals(activeAttackOwner) && attackHit != null) {
               attackHit = autismclient.modules.KillAuraModule.init_EntityHitResult_EntityHitResult_ch_CachePool(attackHit, wire);
               if (attackHit == null) {
                  activeAttackOwner = null;
                  return;
               }
            }

            savedPlayerYaw = MC.player.getYRot();
            savedPlayerPitch = MC.player.getXRot();
            if (wire.initialized()) {
               MC.player.setYRot(wire.currentYaw());
               MC.player.setXRot(wire.currentPitch());
            }

            if (attackHit != null) {
               MC.hitResult = attackHit;
               drainVanillaClickCounts();
               suppressOpposingKey(MC.options.keyUse, true);
               KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(true);

               for (int i = 1; i < attackClicks; i++) {
                  KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
                  KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(true);
               }

               remainingAttackClicks = attackClicks - 1;
               simulatedAttackPress = true;
               attackDispatchPending = true;
            } else if (missHit != null) {
               boolean tap = pendingMissTap;
               pendingMissTap = false;
               MC.hitResult = missHit;
               drainVanillaClickCounts();
               suppressOpposingKey(MC.options.keyUse, true);
               KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(true);
               if (tap) {
                  KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
               }

               simulatedAttackPress = true;
               attackDispatchPending = true;
            } else if (useHit != null) {
               if (useLook != null) {
                  PlacementTickStamp.init_V();
                  MC.player.setYRot(useLook.yaw());
                  MC.player.setXRot(useLook.pitch());
               }

               MC.hitResult = useHit;
               drainVanillaClickCounts();
               suppressOpposingKey(MC.options.keyAttack, false);
               KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(true);
               simulatedUsePress = true;
               useDispatchPending = true;
            } else if (holdAttackRay != null) {
               MC.hitResult = holdAttackRay;
               drainVanillaClickCounts();
               suppressOpposingKey(MC.options.keyUse, true);
               if (!holdingAttack) {
                  KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
                  KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(true);
                  holdingAttack = true;
                  attackDispatchPending = true;
               }
            } else {
               MC.hitResult = holdUseRay;
               activeGuardOwner = guard;
               drainVanillaClickCounts();
               suppressOpposingKey(MC.options.keyAttack, false);
               if (!holdingUse) {
                  KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(false);
                  KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(true);
                  holdingUse = true;
               }

               useDispatchPending = true;
            }
         } else {
            clearAll();
         }
      } else {
         clearAll();
      }
   }

   private static void suppressOpposingKey(KeyMapping key, boolean use) {
      KeyMappingControl.of(key).autism$simulatePress(false);
      if (use) {
         suppressedUseKey = true;
      } else {
         suppressedAttackKey = true;
      }
   }

   private static void drainVanillaClickCounts() {
      while (MC.options.keyUse.consumeClick()) {
      }

      while (MC.options.keyAttack.consumeClick()) {
      }

      while (MC.options.keyPickItem.consumeClick()) {
      }
   }

   public static void cleanupAfterInputTick() {
      if (!AutismLiteVariant.enabled()) {
         QueuedCombatAction.load();
      }

      activeAttackOwner = null;
      pendingAttackHit = null;
      pendingAttackClicks = 0;
      pendingMissOwner = null;
      pendingAttackOwner = null;
      pendingMissHit = null;
      pendingMissTap = false;
      if (!AutismLiteVariant.enabled()) {
         CombatAttackDispatcher.write();
      }

      if (MC != null && MC.options != null) {
         if (simulatedUsePress) {
            KeyMappingControl.of(MC.options.keyUse).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)MC.options.keyUse).autism$setClickCount(0);
            KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
            simulatedUsePress = false;
         }

         if (simulatedAttackPress) {
            KeyMappingControl.of(MC.options.keyAttack).autism$simulatePress(false);
            ((AutismKeyMappingAccessor)MC.options.keyAttack).autism$setClickCount(0);
            KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
            simulatedAttackPress = false;
         }

         if (suppressedUseKey) {
            KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
            suppressedUseKey = false;
         }

         if (suppressedAttackKey) {
            KeyMappingControl.of(MC.options.keyAttack).autism$resetPressedState();
            suppressedAttackKey = false;
         }

         if (activeGuardOwner != null) {
            ((AutismKeyMappingAccessor)MC.options.keyUse).autism$setClickCount(0);
         }

         useDispatchPending = false;
         attackDispatchPending = false;
         remainingAttackClicks = 0;
         if (savedPlayerYaw != null && MC.player != null) {
            MC.player.setYRot(savedPlayerYaw);
            MC.player.setXRot(savedPlayerPitch);
            savedPlayerYaw = null;
         }

         applyDeferredKeyReset();
      } else {
         simulatedUsePress = false;
         simulatedAttackPress = false;
         suppressedUseKey = false;
         suppressedAttackKey = false;
         holdingUse = false;
         holdingAttack = false;
         pendingGuardOwner = null;
         activeGuardOwner = null;
         useDispatchPending = false;
         attackDispatchPending = false;
         remainingAttackClicks = 0;
         savedPlayerYaw = null;
      }
   }
}
