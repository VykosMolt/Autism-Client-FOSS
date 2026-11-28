package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AutoTrapModule extends Module implements SilentMovementCoordinator.Participant {
   private static final double init_D = 10.0;
   private static final int init_I = 25;
   private static final int load_I = 10;
   private static final int read_I = 5;
   private static final double load_D = 1.5;
   private static final int write_I = 4;
   private static final double read_D = 0.001;
   private static final int update_I = 5;
   private static final double write_D = 30.0;
   private static final int sync_I = 3;
   private static final double update_D = 0.98;
   private static final int flush = 40;
   private static final int close = 10;
   private static final int open = 10;
   private static final int poll = 10;
   private static final int push = 30;
   private static final int pop = 200;
   private static final long init_J = 100L;
   private static final int tick = 25;
   private static final long load_J = 3L;
   private static final int run = 2;
   private static final float init_F = 0.05F;
   private static final double sync_D = 1.0;
   private static final Set<Item> WEB_ITEMS = Set.of(Items.COBWEB);
   private static final Set<Block> WEB_BLOCKS = Set.of(Blocks.COBWEB);
   private static final Set<Item> FIRE_ITEMS = Set.of(Items.LAVA_BUCKET, Items.FLINT_AND_STEEL);
   private static final Set<Block> FIRE_BLOCKS = Set.of(Blocks.LAVA, Blocks.FIRE);
   private final Map<Integer, ArrayDeque<AutoTrapModule.Inner_StreamUtil>> motionHistory = new LinkedHashMap<>();
   private final Map<Integer, Integer> missStreaks = new LinkedHashMap<>();
   private final Random random = new Random();
   private volatile AutoTrapModule.Inner_BufferHelper planned;
   private int plannedStaleTicks;
   private int lockedTargetId = -1;
   private AutoTrapModule.Inner_CachePool lastPlacement;
   private int igniteTargetId = -1;
   private int igniteWaitTicks;
   private int auraHoldTicks;
   private int delayTicks;
   private int lastPlaceTick = -2147483648;
   private long lastPlacedAtNanos = -9223372036854775808L;
   private int placeJitterMs;
   private int switchBackSlot = -1;
   private int requestedSlot = -1;
   private int switchBackDelay;
   private int lastSwitchTick = -2147483648;
   private String entityListSource;
   private Set<String> entityIdCache = Set.of();

   AutoTrapModule() {
      super("auto-trap", "AutoTrap", ae.init_ae, "Traps enemies in blocks.");
      this.add(new IntSetting("delay", "Delay", 20, 0, 400, 5).unit("ticks").description("Cooldown after a placed trap").build());
      this.add(new BoolSetting("switch-back", "Switch Back", true).description("Return to previous hotbar slot").build());
      this.add(new BoolSetting("web", "AutoWeb", true).group("AutoWeb").description("Place cobwebs; tried before Ignite").build());
      this.add(new BoolSetting("ignite", "Ignite", true).group("Ignite").description("Place lava or set fire").build());
      this.add(new BoolSetting("predict", "Predict Landing", true).group("Target").description("Trap the predicted landing spot").build());
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").group("Target").description("Entity types to trap").build());
      this.add(new IntSetting("fov", "FOV", 180, 0, 180, 5).group("Target").unit("degrees").description("Cone width around crosshair").build());
      this.add(new IntSetting("hurt-time", "Hurt Time", 10, 0, 10, 1).group("Target").unit("ticks").description("Maximum target hurt time").build());
      this.add(new ChoiceSetting("priority", "Priority", "Type", "Type", "Health", "Distance").group("Target").description("Main target sort key").build());
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
      autismclient.util.SilentRotationManager.releaseOwner(this.id());
      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
   }

   private void resetState() {
      this.planned = null;
      this.plannedStaleTicks = 0;
      this.lockedTargetId = -1;
      this.lastPlacement = null;
      this.igniteTargetId = -1;
      this.igniteWaitTicks = 0;
      this.auraHoldTicks = 0;
      this.delayTicks = 0;
      this.switchBackSlot = -1;
      this.requestedSlot = -1;
      this.switchBackDelay = 0;
      this.lastSwitchTick = -2147483648;
      this.motionHistory.clear();
      this.missStreaks.clear();
   }

   @Override
   public String info() {
      AutoTrapModule.Inner_BufferHelper plan = this.planned;
      return plan == null ? "" : (plan.load_Z() ? "Pickup" : (plan.web() ? "Web" : "Ignite"));
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && this.ownsRotation()) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.SilentRotationManager.hasActiveRotation() && this.ownsRotation();
   }

   @Override
   public boolean isSilentMovementActive() {
      return SilentMovementCoordinator.isSafeWalkActive() ? false : autismclient.util.SilentRotationManager.isReturningToPlayer() || this.isEnabled() && this.canAct();
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null && MC.gameMode != null) {
         this.tickSwitchBack();
         this.updateMotionHistory();
         if (!this.canAct()) {
            this.resetPlan();
         } else {
            if (this.igniteTargetId >= 0 && ++this.igniteWaitTicks > 10) {
               this.igniteTargetId = -1;
               this.delayTicks = this.integer("delay");
            }

            if (this.delayTicks > 0) {
               this.delayTicks--;
               this.planned = null;
               this.plannedStaleTicks = 0;
               this.requestRotationGoal(null);
               this.scheduleSwitchBack();
            } else {
               AutoTrapModule.Inner_BufferHelper plan = this.planned;
               boolean holdingForAura = plan != null && this.holdForAura();
               if (plan != null && !holdingForAura) {
                  RotationMath.Rotation placed = this.rotationFor(plan);
                  if (placed != null) {
                     this.auraHoldTicks = 0;
                     this.plannedStaleTicks = 0;
                     this.planned = null;
                     if (plan.load_Z()) {
                        this.igniteTargetId = -1;
                     } else if (plan.web() && this.bool("ignite") && this.findSlot(FIRE_ITEMS) != null && !this.isTargetOnFire(plan.targetId())) {
                        this.igniteTargetId = plan.targetId();
                        this.igniteWaitTicks = 0;
                     } else {
                        this.igniteTargetId = -1;
                        this.delayTicks = this.integer("delay");
                     }

                     this.requestRotation(placed, true);
                     this.scheduleSwitchBack();
                     return;
                  }
               }

               AutoTrapModule.Inner_BufferHelper planned = this.selectPlan();
               if (planned != null) {
                  this.planned = planned;
                  this.plannedStaleTicks = 0;
               } else if (this.planned != null && (this.isPlanDone(this.planned) || ++this.plannedStaleTicks >= 10)) {
                  this.planned = null;
                  this.lockedTargetId = -1;
                  this.plannedStaleTicks = 0;
               }

               AutoTrapModule.Inner_BufferHelper live = this.planned;
               this.requestRotationGoal(!holdingForAura && live != null ? live.rotation() : null);
               this.scheduleSwitchBack();
            }
         }
      } else {
         this.resetState();
      }
   }

   private boolean canAct() {
      if (MC == null || MC.player == null || MC.level == null || MC.gameMode == null) {
         return false;
      } else if (aD.init_Z() || FreecamController.isActive()) {
         return false;
      } else if (!MC.player.isAlive() || MC.player.isSpectator()) {
         return false;
      } else if (SilentMovementCoordinator.isSafeWalkActive() || ScaffoldModule.update_Z_5()) {
         return false;
      } else if (!MC.player.isUsingItem() && !MC.player.isHandsBusy()) {
         return MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null ? !AutoTotemModule.init_Z_6() && !AutoArmorModule.init_Z_20() : false;
      } else {
         return false;
      }
   }

   private void resetPlan() {
      this.planned = null;
      this.plannedStaleTicks = 0;
      this.auraHoldTicks = 0;
      this.lockedTargetId = -1;
      this.igniteTargetId = -1;
      this.scheduleSwitchBack();
      if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
         if (MC != null && MC.player != null) {
            autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
         }
      }
   }

   private boolean ownsRotation() {
      return this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   private void requestRotationGoal(RotationMath.Rotation goal) {
      this.requestRotation(goal, false);
   }

   private void requestRotation(RotationMath.Rotation goal, boolean pinQuiet) {
      if (goal != null) {
         autismclient.util.SilentRotationManager.requestRotation(this.id(), 15, goal);
      } else if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
      }

      if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.tickPinQuiet(this.id(), MC.player, pinQuiet);
      }
   }

   private boolean holdForAura() {
      boolean wait = this.auraHasTarget() && (MC.player.getAttackStrengthScale(0.5F) > 0.9F || this.isFallingForCrit());
      this.auraHoldTicks = wait ? this.auraHoldTicks + 1 : 0;
      return wait && this.auraHoldTicks < 40;
   }

   private boolean auraHasTarget() {
      return al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && aura.isEnabled() && aura.getTarget() != null;
   }

   private boolean isFallingForCrit() {
      return MC.player.fallDistance > 0.0
         && !MC.player.onGround()
         && !MC.player.onClimbable()
         && !MC.player.isInWater()
         && !MC.player.hasEffect(MobEffects.BLINDNESS)
         && !MC.player.isPassenger();
   }

   private AutoTrapModule.Inner_BufferHelper selectPlan() {
      List<LivingEntity> enemies = this.findEnemies();
      if (this.lockedTargetId >= 0) {
         boolean stillValid = false;

         for (LivingEntity enemy : enemies) {
            if (enemy.getId() == this.lockedTargetId) {
               stillValid = true;
               break;
            }
         }

         if (!stillValid) {
            this.lockedTargetId = -1;
         }
      }

      AutoTrapModule.Inner_BufferHelper pickup = this.followUpPlan();
      if (pickup != null) {
         return pickup;
      }

      if (enemies.isEmpty()) {
         return null;
      }

      if (this.igniteTargetId < 0) {
         AutoTrapModule.Inner_BufferHelper plan = this.bool("web") ? this.planFor(enemies, true) : null;
         if (plan == null && this.bool("ignite")) {
            plan = this.planFor(enemies, false);
         }

         return plan;
      } else {
         LivingEntity combo = null;

         for (LivingEntity enemy : enemies) {
            if (enemy.getId() == this.igniteTargetId) {
               combo = enemy;
               break;
            }
         }

         if (combo != null && !combo.isOnFire() && this.bool("ignite")) {
            return this.planFor(List.of(combo), false);
         }

         this.igniteTargetId = -1;
         this.delayTicks = this.integer("delay");
         return null;
      }
   }

   private boolean isTargetOnFire(int targetId) {
      return MC.level.getEntity(targetId) instanceof LivingEntity living && living.isOnFire();
   }

   private AutoTrapModule.Inner_BufferHelper followUpPlan() {
      AutoTrapModule.Inner_CachePool source = this.lastPlacement;
      if (source == null) {
         return null;
      }

      BlockPos cell = source.placed();
      if (!isLavaSource(MC.level.getBlockState(cell))) {
         this.lastPlacement = null;
         return null;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick - source.read() >= 200) {
         this.lastPlacement = null;
         return null;
      }

      if (!this.isTargetOnFire(source.targetId()) && MC.level.getEntity(source.targetId()) != null && tick - source.read() < 30) {
         return null;
      }

      ItemStack stack = source.hand() == InteractionHand.OFF_HAND
         ? MC.player.getOffhandItem()
         : MC.player.getInventory().getItem(source.load());
      if (!stack.is(Items.BUCKET)) {
         this.lastPlacement = null;
         return null;
      }

      if (source.hand() == InteractionHand.OFF_HAND) {
         if (autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()) || autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
            return null;
         }
      } else if (autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(source.load(), this.id())) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      if (Vec3.atCenterOf(cell).distanceToSqr(eye) > this.reach() * this.reach()) {
         return null;
      }

      RotationMath.Rotation rotation = RotationMath.lookAt(Vec3.atCenterOf(cell), eye);
      return Math.abs(rotation.pitch()) > autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees()
         ? null
         : new AutoTrapModule.Inner_BufferHelper(false, source.targetId(), new AABB(cell), source.load(), source.hand(), rotation, true);
   }

   private static boolean isLavaSource(BlockState state) {
      return state.is(Blocks.LAVA) && state.getFluidState().isSource();
   }

   private boolean isPlanDone(AutoTrapModule.Inner_BufferHelper plan) {
      if (plan.load_Z()) {
         return this.lastPlacement == null;
      } else if (MC.level.getEntity(plan.targetId()) instanceof LivingEntity living && living.isAlive()) {
         boolean webDone = !this.bool("web") || this.findSlot(WEB_ITEMS) == null || this.boxHasWeb(living.getBoundingBox());
         boolean igniteDone = !this.bool("ignite") || this.findSlot(FIRE_ITEMS) == null || living.isOnFire();
         return webDone && igniteDone;
      } else {
         return true;
      }
   }

   private boolean boxHasWeb(AABB box) {
      BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
      BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

      for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
         if (WEB_BLOCKS.contains(MC.level.getBlockState(pos).getBlock())) {
            return true;
         }
      }

      return false;
   }

   private AutoTrapModule.Inner_BufferHelper planFor(List<LivingEntity> enemies, boolean web) {
      AutoTrapModule.Inner_ConfigStore slot = this.findSlot(web ? WEB_ITEMS : FIRE_ITEMS);
      if (slot == null) {
         return null;
      }

      Set<Block> trapBlocks = web ? WEB_BLOCKS : FIRE_BLOCKS;
      BlockState placedState = this.placedStateFor(web, slot.offhand());
      Vec3 eye = MC.player.getEyePosition();
      double reach = this.reach();

      for (LivingEntity target : enemies) {
         if (web || !target.isOnFire()) {
            Vec3 trapPos = this.predictTrapPos(target, target.getId() == this.lockedTargetId);
            if (trapPos != null) {
               trapPos = this.groundBelow(trapPos);
               BlockPos origin = BlockPos.containing(trapPos);
               if (!trapBlocks.contains(MC.level.getBlockState(origin).getBlock())) {
                  EntityDimensions dimensions = target.getDimensions(web ? Pose.STANDING : target.getPose());
                  boolean mustBeOnGround = slot.offhand().is(web ? Items.COBWEB : Items.FLINT_AND_STEEL);
                  Vec3 velocity = target.position().subtract(target.oldPosition());

                  for (BlockPos offset : this.trapCells(trapPos, dimensions, velocity, mustBeOnGround, web, trapBlocks, eye)) {
                     RotationMath.Rotation rotation = this.placementRotation(origin.offset(offset), placedState, eye, reach, slot.offhand(), slot.hand());
                     if (rotation != null) {
                        this.lockedTargetId = target.getId();
                        return new AutoTrapModule.Inner_BufferHelper(
                           web, target.getId(), dimensions.makeBoundingBox(trapPos), slot.slot(), slot.hand(), rotation, false
                        );
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private double reach() {
      return MC.player.blockInteractionRange();
   }

   private BlockState placedStateFor(boolean web, ItemStack stack) {
      if (web) {
         return Blocks.COBWEB.defaultBlockState();
      } else {
         return stack.is(Items.LAVA_BUCKET) ? Blocks.LAVA.defaultBlockState() : Blocks.FIRE.defaultBlockState();
      }
   }

   private AutoTrapModule.Inner_ConfigStore findSlot(Set<Item> items) {
      ItemStack offhand = MC.player.getOffhandItem();
      if (items.contains(offhand.getItem())
         && (this.lastPlacement == null || !offhand.is(Items.LAVA_BUCKET))
         && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id())
         && !autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return new AutoTrapModule.Inner_ConfigStore(-1, InteractionHand.OFF_HAND, offhand);
      }

      int selected = MC.player.getInventory().getSelectedSlot();
      int best = -1;
      int bestDistance = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (items.contains(stack.getItem()) && (this.lastPlacement == null || !stack.is(Items.LAVA_BUCKET))) {
               int distance = Math.abs(slot - selected);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = slot;
               }
            }
         }
      }

      return best < 0 ? null : new AutoTrapModule.Inner_ConfigStore(best, InteractionHand.MAIN_HAND, MC.player.getInventory().getItem(best));
   }

   private List<LivingEntity> findEnemies() {
      double minimum = 1.0;
      double reach = this.reach();
      double maximum = reach * reach;
      Vec3 eyes = MC.player.getEyePosition();
      List<LivingEntity> found = new ArrayList<>();

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && this.isValidTarget(living, eyes, minimum, maximum)) {
            found.add(living);
         }
      }

      if (found.size() > 1) {
         found.sort(this.targetOrder());
      }

      return found;
   }

   private boolean isValidTarget(LivingEntity entity, Vec3 eyes, double minimumSqr, double maximumSqr) {
      if (entity == MC.player || entity.isRemoved() || !entity.isAlive()) {
         return false;
      } else if (!(entity instanceof Attackable)) {
         return false;
      } else if (!EntitySelector.CAN_BE_PICKED.test(entity)) {
         return false;
      } else if (entity.hasPassenger(MC.player)) {
         return false;
      } else if (entity.hurtTime > this.integer("hurt-time")) {
         return false;
      } else if (AntiBotTracker.shouldIgnoreEntity(entity)) {
         return false;
      } else if (TeamsModule.init(entity, "killaura")) {
         return false;
      } else if (entity instanceof Player player && player.isSleeping()) {
         return false;
      } else {
         if (!this.isTargetType(entity)) {
            return false;
         }

         double distanceSqr = this.distanceSqrTo(entity);
         return !(distanceSqr < minimumSqr) && !(distanceSqr > maximumSqr)
            ? this.integer("fov") >= 180 || this.angleTo(entity, eyes) <= this.integer("fov") * 0.5F
            : false;
      }
   }

   private boolean isTargetType(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.targetEntityIds();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> targetEntityIds() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.entityListSource)) {
         return this.entityIdCache;
      }

      Set<String> normalized = new LinkedHashSet<>();

      for (String entry : entries) {
         if (entry != null) {
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty()) {
               normalized.add(value);
               int separator = value.indexOf(58);
               if (separator >= 0 && separator + 1 < value.length()) {
                  normalized.add(value.substring(separator + 1));
               }
            }
         }
      }

      this.entityListSource = source;
      this.entityIdCache = Set.copyOf(normalized);
      return this.entityIdCache;
   }

   private Comparator<LivingEntity> targetOrder() {
      Comparator<LivingEntity> byType = Comparator.comparingInt(entity -> entity instanceof Player ? 0 : 1);
      Comparator<LivingEntity> byHealth = Comparator.comparingDouble(entity -> entity.getHealth() + entity.getAbsorptionAmount());
      Comparator<LivingEntity> byDistance = Comparator.comparingDouble(this::distanceSqrTo);
      Comparator var10000;
      switch (this.choice("priority")) {
         case "Health":
            var10000 = byHealth.thenComparing(byType).thenComparing(byDistance);
            break;
         case "Distance":
            var10000 = byDistance.thenComparing(byType).thenComparing(byHealth);
            break;
         default:
            var10000 = byType.thenComparing(byHealth).thenComparing(byDistance);
      }

      return var10000;
   }

   private double distanceSqrTo(Entity entity) {
      return entity.getBoundingBox().inflate(entity.getPickRadius()).distanceToSqr(MC.player.getEyePosition());
   }

   private float angleTo(Entity entity, Vec3 eyes) {
      RotationMath.Rotation toCenter = RotationMath.lookAt(entity.getBoundingBox().getCenter(), eyes);
      return RotationMath.vectorAngleDegrees(RotationMath.ofPlayer(MC.player), toCenter);
   }

   private void updateMotionHistory() {
      if (!this.bool("predict")) {
         this.motionHistory.clear();
      } else {
         Set<Integer> seen = new HashSet<>();
         double maximumSqr = 100.0;

         for (Player player : MC.level.players()) {
            if (player != MC.player && !player.isRemoved() && player.isAlive() && !(player.distanceToSqr(MC.player) > maximumSqr)) {
               seen.add(player.getId());
               ArrayDeque<AutoTrapModule.Inner_StreamUtil> history = this.motionHistory.computeIfAbsent(player.getId(), key -> new ArrayDeque<>());
               history.addLast(this.sampleMotion(player));

               while (history.size() > 10) {
                  history.removeFirst();
               }
            }
         }

         this.motionHistory.keySet().retainAll(seen);
         this.missStreaks.keySet().retainAll(seen);
      }
   }

   private AutoTrapModule.Inner_StreamUtil sampleMotion(Player player) {
      Vec3 current = player.position();
      Vec3 velocity = current.subtract(player.oldPosition());
      boolean stationary = velocity.lengthSqr() < 0.001;
      if (player.onGround() && stationary) {
         Vec3[] still = new Vec3[4];
         Arrays.fill(still, current);
         return new AutoTrapModule.Inner_StreamUtil(null, 0, current, true, still);
      }

      AABB box = player.getBoundingBox();
      Vec3 position = current;
      double gravity = player.getAttributeValue(Attributes.GRAVITY);
      boolean wasAirborne = !player.onGround();
      boolean freeFalling = !player.onClimbable() && !player.isInWater();
      Vec3[] leadPath = new Vec3[4];
      Vec3 landing = null;
      int landingTick = 0;

      for (int tick = 1; tick <= 25; tick++) {
         List<VoxelShape> shapes = this.blockCollisions(player, box, velocity);
         double dy = Shapes.collide(Axis.Y, box, shapes, velocity.y);
         box = box.move(0.0, dy, 0.0);
         double dx = Shapes.collide(Axis.X, box, shapes, velocity.x);
         box = box.move(dx, 0.0, 0.0);
         double dz = Shapes.collide(Axis.Z, box, shapes, velocity.z);
         box = box.move(0.0, 0.0, dz);
         position = new Vec3(position.x + dx, position.y + dy, position.z + dz);
         if (tick <= 4) {
            leadPath[tick - 1] = position;
         }

         boolean onGround = velocity.y <= 0.0 && dy != velocity.y;
         if (wasAirborne && onGround && landing == null) {
            landing = position;
            landingTick = tick;
            if (tick >= 4) {
               return new AutoTrapModule.Inner_StreamUtil(landing, landingTick, current, false, leadPath);
            }
         }

         wasAirborne = !onGround;
         double verticalSpeed = onGround ? 0.0 : (freeFalling ? (velocity.y - gravity) * 0.98 : velocity.y);
         velocity = new Vec3(dx == velocity.x ? velocity.x : 0.0, verticalSpeed, dz == velocity.z ? velocity.z : 0.0);
         if (onGround && velocity.horizontalDistanceSqr() < 1.0E-6) {
            break;
         }
      }

      for (int i = 0; i < leadPath.length; i++) {
         if (leadPath[i] == null) {
            leadPath[i] = position;
         }
      }

      return new AutoTrapModule.Inner_StreamUtil(landing, landingTick, current, stationary, leadPath);
   }

   private List<VoxelShape> blockCollisions(Entity entity, AABB box, Vec3 velocity) {
      List<VoxelShape> shapes = new ArrayList<>();

      for (VoxelShape shape : MC.level.getBlockCollisions(entity, box.expandTowards(velocity))) {
         shapes.add(shape);
      }

      return shapes;
   }

   private Vec3 predictTrapPos(LivingEntity target, boolean locked) {
      if (target instanceof Player && this.bool("predict")) {
         ArrayDeque<AutoTrapModule.Inner_StreamUtil> history = this.motionHistory.get(target.getId());
         if (history != null && !history.isEmpty()) {
            AutoTrapModule.Inner_StreamUtil last = history.peekLast();
            if (isStationary(history)) {
               this.missStreaks.remove(target.getId());
               return last.load();
            }

            int leadTicks = leadTicks(history);
            boolean longFall = last.landing() != null && last.landingTick() > 4;
            List<Vec3> positions = new ArrayList<>();

            for (AutoTrapModule.Inner_StreamUtil entry : history) {
               Vec3 future = longFall ? entry.landing() : entry.leadPositionAt(leadTicks);
               if (future != null) {
                  positions.add(future);
               }
            }

            if (positions.size() < 5) {
               return locked ? last.load() : this.fallbackAfterMisses(target, last, leadTicks);
            }

            Vec3 average = Vec3.ZERO;

            for (Vec3 position : positions) {
               average = average.add(position);
            }

            average = average.scale(1.0 / positions.size());
            double squared = 0.0;

            for (Vec3 position : positions) {
               squared += position.subtract(average).lengthSqr();
            }

            double spread = Math.sqrt(squared / positions.size());
            if (spread < 1.5) {
               this.missStreaks.remove(target.getId());
               return positions.get(positions.size() - 1);
            } else {
               return this.fallbackAfterMisses(target, last, leadTicks);
            }
         } else {
            return locked ? target.position() : this.fallbackAfterMisses(target, null, 4);
         }
      } else {
         return target.position();
      }
   }

   private static boolean isStationary(ArrayDeque<AutoTrapModule.Inner_StreamUtil> history) {
      if (history.peekLast().stationary()) {
         return true;
      }

      int elapsed = history.size() - 1;
      if (elapsed <= 0) {
         return false;
      }

      double perTickSqr = history.peekFirst().load().distanceToSqr(history.peekLast().load()) / (elapsed * elapsed);
      return perTickSqr < 0.001;
   }

   private static int leadTicks(ArrayDeque<AutoTrapModule.Inner_StreamUtil> history) {
      Vec3 previous = null;
      double walked = 0.0;
      double displacementX = 0.0;
      double displacementZ = 0.0;

      for (AutoTrapModule.Inner_StreamUtil entry : history) {
         Vec3 current = entry.load();
         if (previous != null) {
            double dx = current.x - previous.x;
            double dz = current.z - previous.z;
            walked += Math.sqrt(dx * dx + dz * dz);
            displacementX += dx;
            displacementZ += dz;
         }

         previous = current;
      }

      if (walked < 1.0E-6) {
         return 4;
      }

      double straight = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ) / walked;
      return 1 + (int)Math.round(straight * 3.0);
   }

   private Vec3 fallbackAfterMisses(LivingEntity target, AutoTrapModule.Inner_StreamUtil last, int leadTicks) {
      int misses = this.missStreaks.getOrDefault(target.getId(), 0) + 1;
      this.missStreaks.put(target.getId(), misses);
      if (misses < 10) {
         return null;
      } else if (last == null) {
         return target.position();
      } else {
         return last.landing() != null && last.landingTick() > 4 ? last.landing() : last.leadPositionAt(leadTicks);
      }
   }

   private List<BlockPos> trapCells(Vec3 position, EntityDimensions dimensions, Vec3 velocity, boolean mustBeOnGround, boolean web, Set<Block> trapBlocks, Vec3 eye) {
      BlockPos origin = BlockPos.containing(position);
      AABB start = dimensions.makeBoundingBox(position).move(-origin.getX(), -origin.getY(), -origin.getZ());
      AABB end = start.move(velocity.x * 5.0, 0.0, velocity.z * 5.0);
      if (velocity.horizontalDistance() * 5.0 > 30.0) {
         return this.isTrapCell(origin, BlockPos.ZERO, mustBeOnGround, web, trapBlocks) ? List.of(BlockPos.ZERO) : List.of();
      }

      record Inner_DataUtil(BlockPos init_BlockPos, double init_D, double load) {
      }

      List<Inner_DataUtil> ranked = new ArrayList<>();
      int minX = Mth.floor(start.minX);
      int maxX = Mth.ceil(start.maxX) - 1;
      int minY = Mth.floor(start.minY);
      int maxY = Mth.ceil(start.maxY) - 1;
      int minZ = Mth.floor(start.minZ);
      int maxZ = Mth.ceil(start.maxZ) - 1;

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos offset = new BlockPos(x, y, z);
               AABB unit = new AABB(offset);
               if ((start.intersects(unit) || end.intersects(unit)) && this.isTrapCell(origin, offset, mustBeOnGround, web, trapBlocks)) {
                  double rank = overlapVolume(start, unit) + overlapVolume(end, unit) * 0.5;
                  ranked.add(new Inner_DataUtil(offset, rank, Vec3.atCenterOf(origin.offset(offset)).distanceToSqr(eye)));
               }
            }
         }
      }

      ranked.sort(Comparator.comparingDouble(Inner_DataUtil::init_D).reversed().thenComparingDouble(Inner_DataUtil::load));
      List<BlockPos> offsets = new ArrayList<>(ranked.size());

      for (Inner_DataUtil entry : ranked) {
         offsets.add(entry.init_BlockPos());
      }

      return offsets;
   }

   private Vec3 groundBelow(Vec3 position) {
      BlockPos pos = BlockPos.containing(position);

      int drop;
      for (drop = 0; drop < 3 && !MC.level.isOutsideBuildHeight(pos.below()) && MC.level.getBlockState(pos.below()).isAir(); drop++) {
         pos = pos.below();
      }

      return drop == 0 ? position : position.subtract(0.0, drop, 0.0);
   }

   private boolean isTrapCell(BlockPos origin, BlockPos offset, boolean mustBeOnGround, boolean web, Set<Block> trapBlocks) {
      BlockPos cell = origin.offset(offset);
      if (MC.level.isOutsideBuildHeight(cell)) {
         return false;
      } else {
         BlockState state = MC.level.getBlockState(cell);
         if (!trapBlocks.contains(state.getBlock()) && state.canBeReplaced()) {
            return web && !state.getFluidState().isEmpty() ? false : !mustBeOnGround || !MC.level.getBlockState(cell.below()).isAir();
         } else {
            return false;
         }
      }
   }

   private static double overlapVolume(AABB box, AABB unit) {
      return box.intersects(unit) ? box.intersect(unit).getSize() : 0.0;
   }

   private RotationMath.Rotation placementRotation(BlockPos cell, BlockState placedState, Vec3 eye, double reach, ItemStack material, InteractionHand hand) {
      if (MC.level.isOutsideBuildHeight(cell)) {
         return null;
      }

      BlockState cellState = MC.level.getBlockState(cell);
      if (!cellState.canBeReplaced()) {
         return null;
      }

      if (placedState.is(Blocks.COBWEB) && !cellState.getFluidState().isEmpty()) {
         return null;
      }

      if (Vec3.atCenterOf(cell).distanceToSqr(eye) < 1.0) {
         return null;
      }

      if (!MC.level.isUnobstructed(placedState, cell, CollisionContext.empty())) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         new autismclient.util.BlockPlacementPlanner.Query(cell, eye, reach, this.hitValidatorFor(material, hand))
            .withFromRotation(RotationMath.ofPlayer(MC.player))
            .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
            .withSneaking(MC.player.isSecondaryUseActive())
            .withAutoSneak(false)
            .withRayBudget(new autismclient.util.BlockPlacementPlanner.RayBudget(48))
      );
      return candidate == null ? null : candidate.aim().goalRotation();
   }

   private autismclient.util.BlockPlacementPlanner.HitValidator hitValidatorFor(ItemStack material, InteractionHand hand) {
      return material.getItem() instanceof BlockItem
         ? autismclient.util.BlockPlacementPlanner.blockItemValidator(material, MC.player, hand)
         : (hit, target) -> hit.getBlockPos().relative(hit.getDirection()).equals(target);
   }

   private ItemStack planStack(AutoTrapModule.Inner_BufferHelper plan) {
      return plan.hand() == InteractionHand.OFF_HAND ? MC.player.getOffhandItem() : MC.player.getInventory().getItem(plan.slot());
   }

   private BlockPos placedPosFor(BlockHitResult ray, ItemStack stack) {
      return stack.getItem() instanceof BlockItem
         ? new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, ray).getClickedPos()
         : ray.getBlockPos().relative(ray.getDirection());
   }

   private boolean wouldPlaceAt(BlockHitResult hit, BlockPos cell, ItemStack stack) {
      if (!(stack.getItem() instanceof BlockItem)) {
         return true;
      }

      BlockPlaceContext context = new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit);
      return context.canPlace() && context.getClickedPos().equals(cell);
   }

   private static boolean isBelowPitchLimit(float pitch) {
      return ScaffoldModule.isBelowPitchLimit(Math.abs(pitch));
   }

   private RotationMath.Rotation rotationFor(AutoTrapModule.Inner_BufferHelper plan) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.lastPlaceTick) {
         return null;
      }

      if (BedDefenderModule.init_Z_10()) {
         return null;
      }

      if (SurroundModule.init_Z_27()) {
         return null;
      }

      if (CrystalAuraModule.isRecentlyActive()) {
         return null;
      }

      if (AnchorAuraModule.isRecentlyActive()) {
         return null;
      }

      if (!this.placeCooldownElapsed()) {
         return null;
      }

      if (BlinkPacketManager.isHoldingActionsOnly()) {
         return null;
      }

      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      if (!wire.initialized()) {
         return null;
      }

      RotationMath.Rotation wireRotation = new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch());
      if (!isBelowPitchLimit(wireRotation.pitch())) {
         return null;
      }

      RotationMath.Rotation silent = SilentMovementCoordinator.getRenderRotation(MC.player);
      if (silent != null && !sameRotation(silent, wireRotation)) {
         return null;
      }

      if (plan.load_Z()) {
         return this.lavaFollowUpRotation(plan, tick, wireRotation);
      }

      double reach = this.reach();
      BlockHitResult ray = ScaffoldModule.raycast(MC.player.getEyePosition(), wireRotation, reach, MC.level, MC.player);
      if (!this.placementFitsPlan(plan, ray)) {
         return null;
      }

      BlockPos clicked = ray.getBlockPos();
      if (!autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(clicked), clicked, MC.player.isSecondaryUseActive())) {
         return null;
      }

      if (!this.ensurePlanSlot(plan)) {
         return null;
      }

      if (plan.hand() == InteractionHand.OFF_HAND && autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return null;
      }

      ItemStack stack = MC.player.getItemInHand(plan.hand());
      if (!this.trapItemsFor(plan.web()).contains(stack.getItem())) {
         return null;
      }

      BlockPos placed = this.placedPosFor(ray, stack);
      if (plan.web() && !MC.level.getBlockState(placed).getFluidState().isEmpty()) {
         return null;
      }

      if (Vec3.atCenterOf(placed).distanceToSqr(MC.player.getEyePosition()) < 1.0) {
         return null;
      }

      if (!MC.level.isUnobstructed(this.placedStateFor(plan.web(), stack), placed, CollisionContext.empty())) {
         return null;
      }

      if (al.init_Z_HitResult(ray, plan.hand(), this.id())) {
         return null;
      }

      this.lastPlaceTick = tick;
      this.lastPlacedAtNanos = System.nanoTime();
      this.placeJitterMs = this.random.nextInt(26);
      if (!this.queuePlacement(plan, ray, stack)) {
         return null;
      }

      if (!plan.web() && stack.is(Items.LAVA_BUCKET) && !MC.player.hasInfiniteMaterials()) {
         this.lastPlacement = new AutoTrapModule.Inner_CachePool(placed, plan.targetId(), plan.slot(), plan.hand(), tick);
      }

      this.lockedTargetId = plan.targetId();
      return wireRotation;
   }

   private RotationMath.Rotation lavaFollowUpRotation(AutoTrapModule.Inner_BufferHelper plan, int tick, RotationMath.Rotation wireRotation) {
      AutoTrapModule.Inner_CachePool source = this.lastPlacement;
      if (source == null) {
         return null;
      }

      BlockHitResult ray = this.raycast(wireRotation);
      if (ray == null || !ray.getBlockPos().equals(source.placed())) {
         return null;
      }

      if (!isLavaSource(MC.level.getBlockState(source.placed()))) {
         this.lastPlacement = null;
         return null;
      }

      if (!this.ensurePlanSlot(plan)) {
         return null;
      }

      if (plan.hand() == InteractionHand.OFF_HAND && autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return null;
      }

      ItemStack stack = MC.player.getItemInHand(plan.hand());
      if (!stack.is(Items.BUCKET)) {
         return null;
      }

      if (al.init_Z_HitResult(ray, plan.hand(), this.id())) {
         return null;
      }

      this.lastPlaceTick = tick;
      this.lastPlacedAtNanos = System.nanoTime();
      this.placeJitterMs = this.random.nextInt(26);
      return !this.queuePlacement(plan, ray, stack) ? null : wireRotation;
   }

   private BlockHitResult raycast(RotationMath.Rotation rotation) {
      Vec3 eye = MC.player.getEyePosition();
      Vec3 end = eye.add(Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(this.reach()));
      HitResult result = MC.level
         .clip(new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, CollisionContext.of(MC.player)));
      return result instanceof BlockHitResult hit && result.getType() == Type.BLOCK ? hit : null;
   }

   private boolean placementFitsPlan(AutoTrapModule.Inner_BufferHelper plan, BlockHitResult ray) {
      if (ray == null) {
         return false;
      }

      BlockPos placed = this.placedPosFor(ray, this.planStack(plan));
      if (!new AABB(placed).intersects(plan.targetBox())) {
         return false;
      }

      if (MC.level.isOutsideBuildHeight(placed)) {
         return false;
      }

      BlockState state = MC.level.getBlockState(placed);
      return plan.web() && !state.getFluidState().isEmpty() ? false : state.canBeReplaced() && !this.trapBlocksFor(plan.web()).contains(state.getBlock());
   }

   private Set<Item> trapItemsFor(boolean web) {
      return web ? WEB_ITEMS : FIRE_ITEMS;
   }

   private Set<Block> trapBlocksFor(boolean web) {
      return web ? WEB_BLOCKS : FIRE_BLOCKS;
   }

   private boolean placeCooldownElapsed() {
      if (this.lastPlacedAtNanos == -9223372036854775808L) {
         return true;
      }

      long elapsedMs = (System.nanoTime() - this.lastPlacedAtNanos) / 1000000L;
      return elapsedMs >= 100L + this.placeJitterMs - 3L;
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.lastPlaceTick == AutismSessionState.getInstance().getClientTick();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private boolean ensurePlanSlot(AutoTrapModule.Inner_BufferHelper plan) {
      if (plan.hand() == InteractionHand.OFF_HAND) {
         return true;
      }

      int selected = MC.player.getInventory().getSelectedSlot();
      if (selected == plan.slot()) {
         return true;
      }

      if (!this.canSwitchSlot(plan.slot(), selected)) {
         return false;
      }

      this.requestedSlot = plan.slot();
      return false;
   }

   private boolean canSwitchSlot(int slot, int selected) {
      if (!BedDefenderModule.init_Z_10() && !SurroundModule.init_Z_27() && !CrystalAuraModule.isRecentlyActive() && !AnchorAuraModule.isRecentlyActive()) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick == this.lastSwitchTick) {
            return false;
         }

         if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
            return false;
         }

         try {
            if (this.switchBackSlot < 0) {
               this.switchBackSlot = selected;
            }

            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }

         this.lastSwitchTick = tick;
         return true;
      } else {
         return false;
      }
   }

   private boolean queuePlacement(AutoTrapModule.Inner_BufferHelper plan, BlockHitResult ray, ItemStack stack) {
      if (!stack.isEmpty() && stack.isItemEnabled(MC.level.enabledFeatures())) {
         return !PerTickPlacementLock.init(this.id()) ? false : autismclient.util.ClientInteractionOverride.queueUseBlock(ray, plan.hand());
      } else {
         return false;
      }
   }

   private static boolean sameRotation(RotationMath.Rotation first, RotationMath.Rotation second) {
      return Math.abs(RotationMath.angleDelta(first.yaw(), second.yaw())) <= 0.05F && Math.abs(first.pitch() - second.pitch()) <= 0.05F;
   }

   private void scheduleSwitchBack() {
      if (this.switchBackSlot >= 0 && this.planned == null && this.switchBackDelay <= 0) {
         this.switchBackDelay = 2;
      }
   }

   private void tickSwitchBack() {
      if (this.switchBackSlot >= 0) {
         if (!this.bool("switch-back")) {
            this.switchBackSlot = -1;
            this.requestedSlot = -1;
            this.switchBackDelay = 0;
         } else if (MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null) {
            if (this.requestedSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.requestedSlot) {
               this.switchBackSlot = -1;
               this.requestedSlot = -1;
               this.switchBackDelay = 0;
            } else if (this.switchBackDelay > 0 && --this.switchBackDelay <= 0) {
               if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.switchBackSlot, this.id()) && this.canSwitchSlot(this.switchBackSlot, MC.player.getInventory().getSelectedSlot())) {
                  this.switchBackSlot = -1;
                  this.requestedSlot = -1;
               } else {
                  this.switchBackDelay = 1;
               }
            }
         }
      }
   }

   private record Inner_BufferHelper(
      boolean web, int targetId, AABB targetBox, int slot, InteractionHand hand, RotationMath.Rotation rotation, boolean load_Z
   ) {
   }

   private record Inner_CachePool(BlockPos placed, int targetId, int load, InteractionHand hand, int read) {
   }

   private record Inner_ConfigStore(int slot, InteractionHand hand, ItemStack offhand) {
   }

   private record Inner_StreamUtil(Vec3 landing, int landingTick, Vec3 load, boolean stationary, Vec3[] leadPath) {
      Vec3 leadPositionAt(int ticks) {
         int index = ticks < 1 ? 0 : (ticks > this.leadPath.length ? this.leadPath.length - 1 : ticks - 1);
         return this.leadPath[index];
      }
   }
}
