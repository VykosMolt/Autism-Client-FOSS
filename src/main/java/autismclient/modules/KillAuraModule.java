package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.ValueRange;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.SprintStateTracker;
import autismclient.util.EntityMotionTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public final class KillAuraModule extends Module implements SilentMovementCoordinator.Participant {
   static final int init_I = 10;
   static final double init_D = 0.5;
   static final double load_D = 2.0;
   static final double read_D = 3.0;
   static final double write_D = 0.0;
   static final int load_I = 20;
   static final int read_I = 2;
   static final long IDLE_CLICK_MS = 1000L;
   static final ValueRange SWING_OFFSET_DEFAULT = new ValueRange(-100, 50);
   static final int write_I = 0;
   static final ValueRange DEFAULT_CPS_RANGE = new ValueRange(9, 13);
   static final ValueRange PRE_REACH_DEFAULT = TriggerBotModule.PRE_REACH_DEFAULT;
   static final int update_I = 20;
   static final int sync_I = 10;
   static final int flush_I = 100;
   static final int USE_ITEM_COOLDOWN_TICKS = 3;
   static final int HIT_LANDING_WATCH_TICKS = 8;
   static final double update_D = 0.1;
   static final double sync_D = 0.2;
   static final double flush_D = 0.1;
   static final double close_D = 0.02;
   static final long POST_ATTACK_WINDOW_MS = 60L;
   static final float ATTACK_WINDOW_YAW_CAP = 12.0F;
   static final float ATTACK_WINDOW_PITCH_CAP = 6.0F;
   static final float THROTTLED_YAW_CAP = 5.5F;
   static final float THROTTLED_PITCH_CAP_MAX = 1.2F;
   static final double AIM_FRACTION_MIN_XZ = 0.12;
   static final double poll_D = 0.2;
   private static final double[] AIM_SAMPLE_XS = new double[]{0.12, 0.31, 0.5, 0.69, 0.88};
   private static final double[] AIM_SAMPLE_YS = new double[]{0.2, 0.35, 0.5, 0.65, 0.8};
   private static long lastAttackMillis;
   private static final SoundEvent HIT_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("autismclient", "hitsound"));
   private final Random random = new Random();
   private final KillAuraModule.AimWander aimWander = new KillAuraModule.AimWander(new Random());
   private final KillAuraModule.ClickScheduler clickScheduler = new KillAuraModule.ClickScheduler();
   private final ClickerSettingsGroup combatClicker;
   private final Random missRandom = new Random();
   private final KillAuraModule.MissState missState = new KillAuraModule.MissState();
   private final KillAuraModule.PostAttackTracker postAttackTracker = new KillAuraModule.PostAttackTracker();
   private final EntityMotionTracker motionTracker = new EntityMotionTracker();
   private autismclient.util.CombatAimEngine aimEngine;
   private final autismclient.util.CombatAimEngine.AimFrame aimTuning = new autismclient.util.CombatAimEngine.AimFrame();
   private Vec3 humanAimAnchor;
   private int lastSteppedTargetId = -1;
   private int groundYTargetId = -1;
   private double targetGroundY = 0.0 / 0.0;
   private int trackedTargetId = -1;
   private double trackedTargetX;
   private double trackedTargetZ;
   private boolean targetMovedSlightly;
   private boolean targetMovedFast;
   private int ticksSinceLastAttack = 1000;
   private boolean attackDispatchPending;
   private final autismclient.util.clicker.DoubleClickSampler doubleClickSampler = new autismclient.util.clicker.DoubleClickSampler(new Random());
   private int doubleClickTick = -2147483648;
   private int doubleClickTargetId = -1;
   private int ticksSinceMissSwing = 1000;
   static final double RELAXED_MAX_ACCEL_UP = 30.0;
   private LivingEntity currentTarget;
   private double closestTargetDistanceSqr;
   private double scanRangeMargin = this.rollScanRangeMargin();
   private int switchBackSlot = -1;
   private int borrowedSlot = -1;
   private int switchBackDelayTicks;
   private int slotSwitchTick = -2147483648;
   private int shieldSeenTick = -2147483648;
   private int shieldSeenTargetId = -1;
   private int shieldBrokenTick = -2147483648;
   private int shieldBrokenTargetId = -1;
   private int itemUseCooldownTicks;
   private static final int value = 4;
   private int critWaitTargetId = -1;
   private int critWaitStartTick = -2147483648;
   private int hitLandingTargetId = -1;
   private int hitLandingHurtTime;
   private int hitLandingTicksLeft;
   private final ShieldHitWindowHelper.HitWindowTracker shieldHitWindow = new ShieldHitWindowHelper.HitWindowTracker();
   private InteractionHand blockHitHand;
   private ItemStack blockHitStack = ItemStack.EMPTY;
   private int blockHitSlot = -1;
   private int blockHitThreatId = -1;
   private InteractionHand plannedBlockHand;
   private LivingEntity blockThreat;
   private int lastUnblockTick = -2147483648;
   private InteractionHand pendingUnblockHand;
   private ItemStack pendingUnblockStack = ItemStack.EMPTY;
   private int pendingUnblockSlot = -1;
   private int pendingUnblockReleaseCount;
   private boolean inPreMovementTick;
   private String entityFilterSource = "";
   private Set<String> entityFilterIds = Set.of();
   private final HeldItemFilter heldItemFilter;
   private final KillAuraModule.TickCachedFlag heldItemFilterCache = new KillAuraModule.TickCachedFlag();
   private LivingEntity bedBudgetThreat;
   private int bedBudgetThreatTick = -2147483648;
   private int bedNukerBusyTick = -2147483648;
   private double preReachRoll = 0.0 / 0.0;
   private int preReachTargetId = -2147483648;
   private static final double reset_D = 0.25;
   private static final double NEAR_MISS_RAY_LENGTH = 3.995;
   private static final double stop_D = 0.1;
   private static final int block = 10;
   private static final int FAKE_MISS_MIN_TARGET_AGE = 200;
   static final double AIM_PITCH_SLACK_DEG = 12.0;
   static final double tick_D = 4.0;
   private int fallTicksCacheTick = -2147483648;
   private int cachedTicksUntilFalling = 2147483647;
   private final KillAuraModule.TickCachedFlag allowedItemCache = new KillAuraModule.TickCachedFlag();
   private boolean lastChanceHit;
   static final int STALL_TICKS = 12;
   private int readyStallTicks;
   private static final float update_F = 1.0E-4F;
   private float unusedSwingFactor = 1.0F;
   private final autismclient.util.clicker.SwingTimingModel swingTiming = new autismclient.util.clicker.SwingTimingModel();
   private float swingOffset = this.rollSwingOffset();

   static float randomThrottledStepCap(double lastStep, Random random) {
      float low = 0.90000004F;
      float cap = low + (1.2F - low) * random.nextFloat();
      if (lastStep > 0.0 && Math.abs(cap - lastStep) <= 0.1 * Math.max(cap, lastStep)) {
         cap = lastStep > 0.5 * (low + 1.2F) ? low : 1.2F;
      }

      return cap;
   }

   public KillAuraModule() {
      super("kill-aura", "KillAura", ae.init_ae, "Automatically attacks configured entities.");
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").build());
      this.add(new ChoiceSetting("attack-mode", "Attack Mode", "Real Input", "Real Input", "Packet").description("How attacks are sent").build());
      if (!AutismLiteVariant.enabled()) {
         this.add(
            new ChoiceSetting("interactions", "Interactions", "Legit", "Legit", "Instant", "Block").description("Choose human, instant, or block.").build()
         );
      }

      this.add(
         new ChoiceSetting("targeting", "Targeting", "Smart", "Smart", "Type", "HP", "Distance", "FOV", "Hurt Time", "Age")
            .description("Who gets hit first")
            .build()
      );
      this.add(new IntSetting("fov", "FOV", 360, 10, 360, 10).group("Aim").description("Attack cone in degrees").build());
      this.add(new BoolSetting("fov-circle", "FOV Circle", false).group("Aim").description("Draw the attack cone when its edge is on screen").build());
      this.add(new ChoiceSetting("aim", "Aim", "Human", "Human", "Classic").group("Aim").description("Human: flicks, tracking, timing").build());
      this.add(autismclient.util.CombatAimSpeedController.speedSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(new IntSetting("miss-chance", "Miss Chance", 0, 0, 20, 1).description("Miss rate after the opening hit; 0 = off").build());
      this.add(
         new RangeSetting("swing-offset", "Swing Offset", SWING_OFFSET_DEFAULT, -150.0, 300.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay after full charge")
            .build()
      );
      this.combatClicker = new ClickerSettingsGroup(this, "1.8 Combat", ClickerSettingsGroup.DEFAULT_TECHNIQUE, () -> true, ClickerSettingsGroup.DEFAULT_CPS);
      this.add(
         new RangeSetting("pre-reach", "Pre-Reach", PRE_REACH_DEFAULT, 0.0, 1.5, 0.05).group("1.8 Combat").description("Swing before reach, blocks").build()
      );
      this.add(new BoolSetting("criticals", "Criticals", false).description("Smart critical hits").build());
      this.heldItemFilter = new HeldItemFilter(this);
      this.add(new BoolSetting("auto-sword", "Auto Sword", true).visibleWhen(() -> !this.heldItemFilter.isEnabled()).build());
      this.add(new BoolSetting("shield-break", "Shield Break", true).build());
      AutismConfig.ModuleState saved = AutismConfig.getGlobal().modules.get(this.id());
      if (saved != null && !saved.settings.containsKey("block-hit")) {
         saved.settings.put("block-hit", Boolean.toString(isBlockHitEnabled(saved.settings)));
      }

      this.add(new BoolSetting("block-hit", "Block Hit", false).description("Holds block between damaging hits.").build());
      this.add(
         new BoolSetting("block-hit-cps", "Respect CPS", false).description("Keep 1.8 CPS between blocks").visibleWhen(() -> this.bool("block-hit")).build()
      );
      this.add(
         new BoolSetting("block-hit-after-hit", "After First Hit", true)
            .description("Requires hit within 60 ticks.")
            .visibleWhen(() -> this.bool("block-hit"))
            .build()
      );
      this.add(new BoolSetting("switch-back", "Switch Back", false).visibleWhen(() -> this.bool("auto-sword") && !this.heldItemFilter.isEnabled()).build());
      this.add(new ChoiceSetting("throwables", "Throwables", "MainHand", "MainHand", "BothHands").description("Hands checked for throwables").build());
      this.add(new BoolSetting("hit-marker", "Render", true).build());
      this.add(new BoolSetting("hitsound", "Hitsound", true).build());
      this.combatClicker.registerSections();
   }

   static boolean isBlockHitEnabled(Map<String, String> settings) {
      String current = settings.get("block-hit");
      return current != null
         ? Boolean.parseBoolean(current)
         : Boolean.parseBoolean(settings.get("auto-block")) || Boolean.parseBoolean(settings.get("perfect-hit"));
   }

   @Override
   protected String externalSettingValue(String settingId) {
      if (!"auto-block".equals(settingId) && !"perfect-hit".equals(settingId)) {
         return this.combatClicker == null ? null : this.combatClicker.settingValue(settingId);
      } else {
         return this.value("block-hit");
      }
   }

   @Override
   protected boolean setExternalSettingValue(String settingId, String value) {
      if (!"auto-block".equals(settingId) && !"perfect-hit".equals(settingId)) {
         return this.combatClicker != null && this.combatClicker.setSettingValue(settingId, value);
      }

      this.setValueTransient("block-hit", value);
      return true;
   }

   @Override
   public void onInputFrame() {
      if (MC != null && MC.player != null) {
         this.clickScheduler.feedFrame(System.nanoTime());
      }
   }

   @Override
   public void onEnable() {
      this.resetCombatState();
   }

   @Override
   public void onDisable() {
      int back = this.switchBackSlot;
      int borrowed = this.borrowedSlot;
      this.resetCombatState();
      if (back >= 0 && borrowed >= 0 && this.switchBackEnabled()) {
         // Switch Back finishes in tick() once no screen is open.
         this.switchBackSlot = back;
         this.borrowedSlot = borrowed;
      }

      autismclient.util.ClientInteractionOverride.cancelAttackOwner(this.id());
      autismclient.util.ClientInteractionOverride.requestDeferredKeyReset();
      autismclient.util.SilentRotationManager.releaseOwner("kill-aura");
   }

   @Override
   public void onGameLeft() {
      this.resetCombatState();
      if ("kill-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      this.postAttackTracker.reset();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (!AutismLiteVariant.enabled() && "interactions".equals(settingId)) {
         autismclient.util.CombatInteractionController.reset();
      }

      if ("block-hit".equals(settingId)) {
         if (!this.bool("block-hit")) {
            this.stopBlockHit(false);
         }

         this.shieldHitWindow.load();
      }

      if ("block-hit-after-hit".equals(settingId)) {
         this.stopBlockHit(false);
      }

      if ("block-hit-cps".equals(settingId)) {
         this.stopBlockHit(false);
         this.clickScheduler.reset();
      }

      if ("entities".equals(settingId)) {
         this.entityFilterSource = null;
      }

      if ("auto-sword".equals(settingId) && !this.bool("auto-sword") || "held-items-only".equals(settingId) && this.heldItemFilter != null && this.heldItemFilter.isEnabled()) {
         if (this.switchBackSlot >= 0
            && MC != null
            && MC.player != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !BlinkPacketManager.isHoldingActionsOnly()
            && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, this.switchBackSlot);
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }
         }

         this.switchBackSlot = -1;
         this.switchBackDelayTicks = 0;
      }

      if ("switch-back".equals(settingId) && !this.bool("switch-back")) {
         this.switchBackSlot = -1;
         this.switchBackDelayTicks = 0;
      }

      if ("shield-break".equals(settingId) && !this.bool("shield-break")) {
         if (this.switchBackSlot >= 0
            && MC != null
            && MC.player != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !BlinkPacketManager.isHoldingActionsOnly()
            && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, this.switchBackSlot);
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }
         }

         this.switchBackSlot = -1;
         this.borrowedSlot = -1;
         this.switchBackDelayTicks = 0;
         this.shieldSeenTargetId = -1;
         this.shieldSeenTick = -2147483648;
         this.shieldBrokenTick = -2147483648;
         this.shieldBrokenTargetId = -1;
      }
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null) {
         autismclient.util.SilentRotationManager.tick("kill-aura", MC.player);
         if (this.switchBackSlot >= 0) {
            if (!this.switchBackEnabled()) {
               this.switchBackSlot = -1;
               this.borrowedSlot = -1;
            } else if (MC.gui.screen() == null && MC.gui.overlay() == null && !BlinkPacketManager.isHoldingActionsOnly()) {
               this.restoreSwitchBackSlot();
            }
         }
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.SilentRotationManager.hasActiveRotation() || this.switchBackSlot >= 0;
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null) {
         this.inPreMovementTick = true;

         try {
            this.runPreMovementTick();
         } finally {
            this.inPreMovementTick = false;
         }
      }
   }

   private void runPreMovementTick() {
      this.shieldHitWindow.init(this.timerSpeedRatio());
      this.releasePendingUnblock();
      if (MC.gui.screen() == null && MC.gui.overlay() == null && (AutismLiteVariant.enabled() || !autismclient.ai.aP.load_Z())) {
         if (!SilentMovementCoordinator.isSafeWalkActive() && !ScaffoldModule.update_Z_5()) {
            if (!AutismLiteVariant.enabled() && autismclient.util.CombatInteractionController.write()) {
               this.stopBlockHit(false);
               this.currentTarget = null;
               this.humanAimAnchor = null;
               this.missState.clear();
               this.clickScheduler.suspend();
               this.pollHitLanding();
               if (MC.player.isUsingItem()) {
                  this.itemUseCooldownTicks = 3;
               }

               this.postAttackTracker.clearLastRotation();
            } else {
               this.maintainBlockHit();
               if (!AutismLiteVariant.enabled() && !BedNukerModule.init_Z_23() && BedNukerModule.sync() && Double.isFinite(init_D_9()) && BedNukerModule.flush()) {
                  this.bedNukerBusyTick = AutismSessionState.getInstance().getClientTick();
               }

               if (MC.gui.screen() == null && MC.missTime > 10) {
                  MC.missTime = 0;
               }

               boolean usingItem = this.isBusyUsingItem();
               if (usingItem) {
                  this.itemUseCooldownTicks = 3;
               } else if (this.itemUseCooldownTicks > 0) {
                  this.itemUseCooldownTicks--;
               }

               boolean throwable = this.holdsAllowedItem();
               if (autismclient.util.KeyMappingControl.of(MC.options.keyAttack).autism$isActuallyDown() && this.candidateInReach()) {
                  autismclient.util.RecentCombatTracker.init();
               }

               boolean wrongItem = !this.heldItemPassesFilter();
               if (!autismclient.util.ManualInteractionYield.isYielding() && !usingItem && !throwable && !wrongItem) {
                  if (!AutismLiteVariant.enabled() && KBDisplacementModule.init_Z_24()) {
                     this.stopBlockHit(false);
                     this.clickScheduler.suspend();
                     this.pollHitLanding();
                     this.trackTargetMotion(this.currentTarget);
                  } else {
                     this.clickScheduler.tick();
                     this.pollHitLanding();
                     this.ticksSinceLastAttack = Math.min(this.ticksSinceLastAttack + 1, 1000);
                     this.ticksSinceMissSwing = Math.min(this.ticksSinceMissSwing + 1, 1000);
                     boolean postAttackWindow = this.postAttackTracker.inPostAttackWindow();
                     if (this.canInteract()) {
                        this.selectTarget(postAttackWindow);
                     } else {
                        this.stopBlockHit(false);
                        this.currentTarget = null;
                        this.humanAimAnchor = null;
                        this.missState.clear();
                     }

                     this.trackTargetMotion(this.currentTarget);
                     this.clickScheduler.setActive(this.currentTarget != null && this.withinPreReach(this.currentTarget));
                     if (this.currentTarget != null && this.withinPreReach(this.currentTarget)) {
                        autismclient.util.RecentCombatTracker.init();
                     }

                     this.tickSwitchBack();
                     boolean inAttackWindow = postAttackWindow || this.canHitCurrentTargetNow();
                     boolean throttle = inAttackWindow && this.postAttackTracker.turnSpeedTooHigh();
                     this.aimTuning.speedScale = autismclient.util.CombatAimSpeedController.clampMultiplier(this.decimal("rotation-speed-multiplier"));
                     if ("Human".equals(this.choice("aim"))) {
                        this.tickAim(inAttackWindow);
                     }

                     float turnCap = autismclient.util.CombatAimSpeedController.turnCap(this.aimTuning.speedScale);
                     double[] lastStep = autismclient.util.SilentRotationManager.getLastStepDeltas();
                     autismclient.util.SilentRotationManager.tick(
                        "kill-aura",
                        MC.player,
                        !inAttackWindow ? turnCap : (throttle ? 5.5F : 12.0F),
                        !inAttackWindow ? turnCap : (throttle ? randomThrottledStepCap(lastStep[1], this.random) : 6.0F)
                     );
                     this.planBlockHit();
                     if (this.canInteract()) {
                        this.tickAttack();
                        this.runScheduledDoubleClick();
                        this.swingNearMiss();
                     } else {
                        this.clickScheduler.noteLostReason("canRun");
                     }

                     this.updateBlockHit();
                     this.tickStallCounter();
                     if (!this.attackDispatchPending) {
                        this.recordSentRotation();
                     }
                  }
               } else {
                  this.stopBlockHit(this.playerWantsToUseItem());
                  this.currentTarget = null;
                  this.missState.clear();
                  autismclient.util.SilentRotationManager.releaseOwner("kill-aura");
                  this.clickScheduler.suspend();
                  this.pollHitLanding();
                  autismclient.util.SilentRotationManager.tick("kill-aura", MC.player);
                  this.recordSentRotation();
               }
            }
         } else {
            if (!AutismLiteVariant.enabled()) {
               autismclient.util.CombatInteractionController.reset();
            }

            this.stopBlockHit(false);
            this.currentTarget = null;
            this.missState.clear();
            this.clickScheduler.setActive(false);
            if ("kill-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               autismclient.util.SilentRotationManager.resetAll();
            }

            this.postAttackTracker.clearLastRotation();
         }
      } else {
         autismclient.util.ClientInteractionOverride.cancelAttackOwner(this.id());
         this.stopBlockHit(false);
         if (!AutismLiteVariant.enabled()) {
            autismclient.util.CombatInteractionController.reset();
         }

         this.currentTarget = null;
         this.humanAimAnchor = null;
         this.lastSteppedTargetId = -1;
         this.aimWander.reset();
         this.missState.clear();
         this.readyStallTicks = 0;
         this.critWaitTargetId = -1;
         this.doubleClickTargetId = -1;
         this.clickScheduler.reset();
         this.pollHitLanding();
         autismclient.util.SilentRotationManager.releaseOwner("kill-aura");
         autismclient.util.SilentRotationManager.tick("kill-aura", MC.player);
         this.recordSentRotation();
      }
   }

   private double preReachFor(Entity target) {
      if (target.getId() != this.preReachTargetId || Double.isNaN(this.preReachRoll)) {
         this.preReachTargetId = target.getId();
         this.preReachRoll = ValueRange.parse(this.value("pre-reach"), PRE_REACH_DEFAULT).random(this.random);
      }

      return this.preReachRoll;
   }

   private boolean withinPreReach(Entity target) {
      double reach = this.attackReach() + ValueRange.parse(this.value("pre-reach"), PRE_REACH_DEFAULT).max() + 0.5;
      return target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach;
   }

   private double targetAngularRadiusDeg(LivingEntity target) {
      if (target != null && MC.player != null) {
         double distance = Math.max(0.5, MC.player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()));
         double radius = target.getBbWidth() * 0.5;
         return Math.max(1.5, Math.min(20.0, Math.toDegrees(Math.atan2(radius, distance))));
      } else {
         return 4.0;
      }
   }

   private void tickAim(boolean inAttackWindow) {
      LivingEntity target = this.currentTarget;
      if (target != null && this.humanAimAnchor != null) {
         autismclient.util.CombatAimEngine hand = this.getAimEngine();
         boolean opening = autismclient.util.RecentCombatTracker.init(target);
         hand.setMissChance(this.integer("miss-chance") * 0.01);
         Vec3 eyes = MC.player.getEyePosition();
         AABB targetBox = target.getBoundingBox();
         Vec3 anchor = autismclient.util.CombatAimEngine.AimFrame.clampAnchorToReach(eyes, targetBox, this.humanAimAnchor, this.attackReach());
         AABB region = autismclient.util.CombatAimEngine.AimFrame.shrinkRegionToReach(eyes, targetBox, anchor, this.attackReach());
         AABB receivedBox = null;
         Vec3 heldPose = BacktrackModule.init_Vec3_LivingEntity_Vec3(target, eyes);
         Vec3 sampled = heldPose != null ? heldPose : (this.motionTracker.isTracking(target) ? this.sampledReceivedPosition() : null);
         if (sampled != null) {
            receivedBox = targetBox.move(sampled.subtract(target.position()));
            double bottom = Math.max(region.minY, receivedBox.minY);
            double top = Math.min(region.maxY, receivedBox.maxY);
            if (top > bottom + 0.04) {
               region = new AABB(region.minX, bottom, region.minZ, region.maxX, top, region.maxZ);
               anchor = new Vec3(anchor.x, Mth.clamp(anchor.y, bottom + 0.02, top - 0.02), anchor.z);
            }
         }

         this.aimTuning.setTarget(eyes, targetBox, region, anchor, sampled, receivedBox);
         this.aimTuning.allowBoundedRests = true;
         this.aimTuning.strafeYawBias = 0.0;
         autismclient.util.SilentRotationManager.setExactYawHints(this.aimTuning.centerYawHint, this.aimTuning.nearestYawHint);
         this.aimTuning.compensateObserverMotion = true;
         this.aimTuning.boundedPitchMode = true;
         this.aimTuning.pitchSlackDeg = 12.0;
         this.aimTuning.maxPitchRidingCapDeg = 4.0;
         this.aimTuning.allowPromptFlick = true;
         this.aimTuning.instantPromptFlick = true;
         this.aimTuning.prioritizeStrike = true;
         boolean stalled = this.readyStallTicks >= 12;
         if (stalled) {
            this.missState.clear();
         }

         double ready = this.ticksUntilReady(target);
         boolean heldHitDue = heldPose != null && ready <= 2.0;
         this.aimTuning.allowExcursion = ready > 3.0 && !opening && !stalled && !this.missState.isPending() && !this.postAttackTracker.aimErrorTooHigh();
         this.aimTuning.clampInsideBox = opening || stalled || heldHitDue;
         this.aimTuning.ticksUntilAttack = Math.max(0.0, ready - 1.0);
         this.aimTuning.comboCount = this.ticksSinceLastAttack;
         this.aimTuning.inAttackWindow = inAttackWindow;
         this.aimTuning.gcdStep = RotationMath.gcdStep();
         this.aimTuning.legacyNoCooldown = !this.isModernCombat();
         this.aimTuning.maxAccelUp = this.ticksSinceLastAttack >= 4 && ready >= 3.5 ? 30.0 : 18.0;
         this.aimTuning.forceWhiff = !opening
            && !stalled
            && !heldHitDue
            && this.integer("miss-chance") > 0
            && this.fakeMissPlausible(target, MC.player.getEyePosition())
            && (this.missState.isPending() || this.missState.canPlanMiss() && hand.shouldTriggerMiss(this.aimTuning));
         int id = target.getId();
         autismclient.util.SilentRotationManager.setCustomHandStepper("kill-aura", (at, maxYaw, maxPitch) -> {
            this.aimTuning.targetChanged = id != this.lastSteppedTargetId;
            this.lastSteppedTargetId = id;
            return hand.computeStep(this.aimTuning, at.yaw(), at.pitch(), maxYaw, maxPitch);
         }, this.aimTuning.maxAccelUp);
      } else {
         if (target == null || this.humanAimAnchor == null) {
            this.lastSteppedTargetId = -1;
         }

         autismclient.util.SilentRotationManager.enableHumanizedAim("kill-aura", this.targetAngularRadiusDeg(target));
      }
   }

   public static void init_V_27() {
      if (!AutismLiteVariant.enabled()) {
         KillAuraModule aura = activeInstance();
         if (aura != null && aura.holdsOwnBlockHit()) {
            aura.stopBlockHit(false);
         }
      }
   }

   public static boolean init_Z_ce_DataUtil(RotationMath.Rotation goal, autismclient.util.CombatAimEngine.AimFrame frame, boolean pinQuiet) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      KillAuraModule aura = activeInstance();
      if (aura != null && MC.player != null) {
         autismclient.util.SilentRotationManager.requestRotation("kill-aura", 10, goal);
         if (!"kill-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
            return false;
         }

         aura.lastSteppedTargetId = -1;
         if (!pinQuiet) {
            autismclient.util.SilentRotationManager.setExactYawHints(frame.centerYawHint, frame.nearestYawHint);
            autismclient.util.SilentRotationManager.setCustomHandStepper("kill-aura", (at, yaw, pitch) -> aura.getAimEngine().computeStep(frame, at.yaw(), at.pitch(), yaw, pitch), frame.maxAccelUp);
         }

         autismclient.util.SilentRotationManager.tickPinQuiet("kill-aura", MC.player, pinQuiet);
         return true;
      } else {
         return false;
      }
   }

   private void scheduleDoubleClick(Entity target, boolean lastChance) {
      int gap = lastChance ? 0 : this.doubleClickSampler.sampleFollowUps();
      this.doubleClickTick = gap > 0 ? AutismSessionState.getInstance().getClientTick() + gap : -2147483648;
      this.doubleClickTargetId = target.getId();
   }

   private void runScheduledDoubleClick() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.doubleClickTick) {
         this.doubleClickTick = -2147483648;
         LivingEntity target = this.currentTarget;
         if (!autismclient.util.ClientInteractionOverride.ownsAttackInput()
            && this.isModernCombat()
            && target != null
            && target.getId() == this.doubleClickTargetId
            && this.ticksSinceLastAttack != 0
            && this.ticksSinceMissSwing != 0
            && !this.holdsOwnBlockHit()
            && this.blockThreat == null
            && tick != this.lastUnblockTick
            && tick != this.slotSwitchTick
            && !MC.player.isUsingItem()
            && !this.missState.isPending()) {
            RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
            if (rotation == null) {
               rotation = RotationMath.ofPlayer(MC.player);
            }

            EntityHitResult hit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
            if (hit != null && this.inAttackRange(MC.player.getMainHandItem(), hit.getLocation()) && this.sendAttack(target, hit, true)) {
            }
         }
      }
   }

   private void swingNearMiss() {
      if (!autismclient.util.ClientInteractionOverride.ownsAttackInput()) {
         LivingEntity target = this.currentTarget;
         if (this.blockThreat == null
            && AutismSessionState.getInstance().getClientTick() != this.lastUnblockTick
            && !this.holdsOwnBlockHit()
            && (target == null || this.blockHitAllowsHit(target))
            && this.aimEngine != null
            && target != null
            && this.humanAimAnchor != null
            && this.aimTuning.forceWhiff
            && !autismclient.util.RecentCombatTracker.init(target)
            && this.ticksSinceLastAttack != 0
            && this.ticksSinceMissSwing != 0
            && this.missState.canPlanMiss()
            && "Human".equals(this.choice("aim"))
            && (this.isModernCombat() || this.clickScheduler.clickDueNow())) {
            RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
            if (rotation != null && "kill-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               Vec3 eyes = MC.player.getEyePosition();
               AABB box = target.getBoundingBox();
               if (this.fakeMissPlausible(target, eyes) && rotationHitsBox(eyes, box, rotation) && rotationNearlyHitsBox(eyes, box, rotation)) {
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  autismclient.util.ClickRateTracker.recordLeftClick();
                  this.clickScheduler.consumeClick("nearMiss");
                  this.resetMissCycle();
               }
            }
         }
      }
   }

   private void resetMissCycle() {
      if (this.aimEngine != null) {
         this.aimEngine.resetMissCycle();
      }

      this.ticksSinceMissSwing = 0;
      this.missState.markSpent();
   }

   static boolean rotationHitsBox(Vec3 eyes, AABB box, RotationMath.Rotation rotation) {
      Vec3 direction = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
      Vec3 end = eyes.add(direction.scale(4.0));
      return !box.inflate(0.05).contains(eyes) && box.inflate(0.05).clip(eyes, end).isEmpty();
   }

   static boolean rotationNearlyHitsBox(Vec3 eyes, AABB box, RotationMath.Rotation rotation) {
      Vec3 direction = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
      Vec3 end = eyes.add(direction.scale(3.995));
      return box.inflate(0.25).clip(eyes, end).isPresent();
   }

   private boolean fakeMissPlausible(LivingEntity target, Vec3 eyes) {
      return this.targetMovedFast && this.ticksSinceLastAttack <= 10 ? target.tickCount >= 200 : false;
   }

   private autismclient.util.CombatAimEngine getAimEngine() {
      if (this.aimEngine == null) {
         this.aimEngine = autismclient.util.CombatAimEngine.createDefault();
      }

      return this.aimEngine;
   }

   private double ticksUntilReady(LivingEntity target) {
      double ticks;
      if (this.isModernCombat()) {
         float delay = this.attackCooldownTicks();
         float offset = this.swingOffsetFor(delay, target);
         ticks = Math.max(0.0, delay * this.unusedSwingFactor + offset - 0.5 - this.attackStrengthTicker() - 1.0E-4F * delay);
      } else {
         ticks = this.clickScheduler.ticksUntilNextClick();
      }

      if (target.hurtTime > 1) {
         ticks = Math.max(ticks, target.hurtTime - 1);
      }

      return ticks;
   }

   private void trackTargetMotion(LivingEntity target) {
      this.motionTracker.track(target, AutismSessionState.getInstance().getClientTick());
      if (target == null) {
         this.trackedTargetId = -1;
         this.targetMovedSlightly = false;
         this.targetMovedFast = false;
      } else {
         if (target.getId() != this.trackedTargetId) {
            this.trackedTargetId = target.getId();
            this.targetMovedSlightly = false;
            this.targetMovedFast = false;
         } else {
            double moved = Math.hypot(target.getX() - this.trackedTargetX, target.getZ() - this.trackedTargetZ);
            this.targetMovedSlightly = moved >= 0.05;
            this.targetMovedFast = moved >= 0.1;
         }

         this.trackedTargetX = target.getX();
         this.trackedTargetZ = target.getZ();
         if (target.getId() == this.groundYTargetId && !Double.isNaN(this.targetGroundY)) {
            if (target.onGround() || target.getY() < this.targetGroundY) {
               this.targetGroundY = target.getY();
            }
         } else {
            this.groundYTargetId = target.getId();
            this.targetGroundY = target.getY();
         }
      }
   }

   private Vec3 humanAimAnchorFor(LivingEntity entity, double range) {
      AABB box = entity.getBoundingBox();
      Vec3 eyes = MC.player.getEyePosition();
      double ground = entity.getId() == this.groundYTargetId && !Double.isNaN(this.targetGroundY) ? Math.min(this.targetGroundY, entity.getY()) : entity.getY();
      Vec3 anchor = autismclient.util.CombatAimEngine.AimFrame.clampAnchorToReach(
         eyes, box, autismclient.util.CombatAimEngine.AimFrame.computeAnchorAtHeight(box, ground + this.getAimEngine().getPreferredAimHeightOffset()), this.attackReach()
      );
      return this.rotationToBoxPoint(eyes, box, anchor, range * range) != null ? anchor : this.findVisibleAimPoint(entity, range, anchor);
   }

   private void recordSentRotation() {
      RotationMath.Rotation sent = autismclient.util.SilentRotationManager.getCurrentRotation();
      if (sent == null) {
         sent = RotationMath.ofPlayer(MC.player);
      }

      float perfectYaw = this.yawTo(this.currentTarget);
      this.postAttackTracker.sampleRotation(sent.yaw(), sent.pitch(), perfectYaw);
      if (this.aimEngine != null) {
         boolean sampled = this.postAttackTracker.elapsedSinceAttack() <= 1050L && this.targetMovedSlightly;
         this.aimEngine.recordRealizedSample(sent.yaw(), sent.pitch(), perfectYaw, sampled);
      }
   }

   private float yawTo(LivingEntity target) {
      if (target != null && MC.player != null) {
         double diffX = target.getX() - MC.player.getX();
         double diffZ = target.getZ() - MC.player.getZ();
         return (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      } else {
         return 0.0F / 0.0F;
      }
   }

   private boolean isBusyUsingItem() {
      return MC.player != null && (MC.player.isUsingItem() && !this.holdsOwnBlockHit() || this.playerWantsToUseItem());
   }

   private boolean playerWantsToUseItem() {
      if (!AutismLiteVariant.enabled()) {
         if (autismclient.util.CombatInteractionController.load()) {
            return false;
         }

         if (autismclient.util.CombatInteractionController.init_Z()) {
            return false;
         }

         if (autismclient.util.CombatInteractionController.read()) {
            return true;
         }
      }

      return MC != null && MC.options != null && autismclient.util.KeyMappingControl.of(MC.options.keyUse).autism$physicalForBody();
   }

   private boolean holdsOwnBlockHit() {
      if (this.blockHitHand != null && this.id().equals(autismclient.util.ClientInteractionOverride.getGuardOwner()) && MC.player != null) {
         return !MC.player.isUsingItem()
            ? true
            : MC.player.getUsedItemHand() == this.blockHitHand && ItemStack.isSameItem(MC.player.getUseItem(), this.blockHitStack);
      } else {
         return false;
      }
   }

   private void clearPendingUnblock() {
      this.pendingUnblockHand = null;
      this.pendingUnblockStack = ItemStack.EMPTY;
      this.pendingUnblockSlot = -1;
   }

   private void releasePendingUnblock() {
      if (this.pendingUnblockHand != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         long age = (long)tick - this.lastUnblockTick;
         if (!AutismLiteVariant.enabled() && FreecamController.isAiControllingBody()) {
            this.clearPendingUnblock();
         } else if (!this.isBlockHitCpsMode()
            || !this.canInteract(true)
            || !autismclient.util.InventorySlotArbiter.canBeginActionGroup(this.id())
            || age < 0L
            || age >= 40L
            || this.playerWantsToUseItem()
            || MC.player.getInventory().getSelectedSlot() != this.pendingUnblockSlot
            || !ItemStack.isSameItem(MC.player.getItemInHand(this.pendingUnblockHand), this.pendingUnblockStack)) {
            this.clearPendingUnblock();
         } else if (this.blockHitHand == null
            && MC.player.isUsingItem()
            && MC.player.getUsedItemHand() == this.pendingUnblockHand
            && ItemStack.isSameItem(MC.player.getUseItem(), this.pendingUnblockStack)) {
            MC.gameMode.releaseUsingItem(MC.player);
            this.lastUnblockTick = tick;
            this.pendingUnblockReleaseCount++;
         }
      }
   }

   private void maintainBlockHit() {
      if (this.blockHitHand != null) {
         if (this.playerWantsToUseItem()) {
            this.stopBlockHit(true);
         } else if (this.bool("block-hit")
            && ItemStack.isSameItem(MC.player.getItemInHand(this.blockHitHand), this.blockHitStack)
            && this.canBlockWith(MC.player.getItemInHand(this.blockHitHand))
            && (this.blockHitHand != InteractionHand.MAIN_HAND || MC.player.getInventory().getSelectedSlot() == this.blockHitSlot)
            && this.id().equals(autismclient.util.ClientInteractionOverride.getGuardOwner())) {
            this.blockHitStack = MC.player.getItemInHand(this.blockHitHand);
            if (this.isBlockHitCpsMode() && autismclient.util.ClientInteractionOverride.isGuardDispatchedFor(this.id()) && !MC.player.isUsingItem()) {
               this.stopBlockHit(false);
            }
         } else {
            this.stopBlockHit(false);
         }
      }
   }

   public static boolean init_Z_31() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      KillAuraModule aura = activeInstance();
      if (aura != null && MC.player != null && MC.level != null) {
         HitResult hit = MC.hitResult;
         if (hit instanceof EntityHitResult npc && AntiBotTracker.isBot(npc.getEntity())) {
            return false;
         } else if (hit != null && hit.getType() == Type.BLOCK) {
            return false;
         } else {
            return hit instanceof EntityHitResult entity && !(entity.getEntity() instanceof Player) ? false : aura.pickBlockingHand() != null;
         }
      } else {
         return false;
      }
   }

   public static boolean read_Z_14() {
      return al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && aura.isEnabled() && aura.holdsOwnBlockHit() && !aura.playerWantsToUseItem();
   }

   public static void load_V_20() {
      if (read_Z_14()) {
         ((KillAuraModule)al.init_Module_Str("kill-aura")).stopBlockHitNow(false, true);
      }
   }

   private void stopBlockHit(boolean handToPlayer) {
      this.stopBlockHitNow(handToPlayer, this.inPreMovementTick);
   }

   private void stopBlockHitNow(boolean handToPlayer, boolean tickHead) {
      InteractionHand hand = this.blockHitHand;
      ItemStack stack = this.blockHitStack;
      boolean dispatched = autismclient.util.ClientInteractionOverride.isGuardDispatchedFor(this.id());
      if (hand != null && dispatched && MC.player != null) {
         this.lastUnblockTick = AutismSessionState.getInstance().getClientTick();
      }

      if (hand != null) {
         if (!handToPlayer && dispatched && this.isBlockHitCpsMode()) {
            this.pendingUnblockHand = hand;
            this.pendingUnblockStack = stack.copy();
            this.pendingUnblockSlot = this.blockHitSlot;
         } else {
            this.clearPendingUnblock();
         }
      }

      this.blockHitHand = null;
      this.blockHitStack = ItemStack.EMPTY;
      this.blockHitSlot = -1;
      this.blockHitThreatId = -1;
      autismclient.util.ClientInteractionOverride.releaseGuard(this.id());
      if (!handToPlayer
         && hand != null
         && MC != null
         && MC.player != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && (
            dispatched && !MC.player.isUsingItem()
               || MC.player.isUsingItem() && MC.player.getUsedItemHand() == hand && ItemStack.isSameItem(MC.player.getUseItem(), stack)
         )) {
         if (tickHead) {
            MC.gameMode.releaseUsingItem(MC.player);
         } else {
            autismclient.util.ClientInteractionOverride.scheduleDeferredReleaseUsingItem(hand, stack);
         }
      }
   }

   private InteractionHand pickBlockingHand() {
      ItemStack main = MC.player.getMainHandItem();
      if (this.canBlockWith(main)) {
         return InteractionHand.MAIN_HAND;
      }

      boolean useOnOnlyTool = main.is(ItemTags.AXES) || main.is(ItemTags.SHOVELS) || main.is(ItemTags.HOES);
      return this.canBlockWith(MC.player.getOffhandItem()) && (useOnOnlyTool || !autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) ? InteractionHand.OFF_HAND : null;
   }

   private boolean canBlockWith(ItemStack stack) {
      return !stack.isEmpty()
         && stack.getUseAnimation() == ItemUseAnimation.BLOCK
         && !MC.player.getCooldowns().isOnCooldown(stack)
         && stack.isItemEnabled(MC.level.enabledFeatures());
   }

   private static int blockDelayTicks(ItemStack stack) {
      BlocksAttacks blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
      return blocks == null ? 0 : Math.max(0, blocks.blockDelayTicks());
   }

   private void updateBlockHit() {
      if (this.bool("block-hit")
         && this.canInteract()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && this.ticksSinceLastAttack != 0
         && this.ticksSinceMissSwing != 0
         && AutismSessionState.getInstance().getClientTick() != this.slotSwitchTick
         && !autismclient.util.ClientInteractionOverride.ownsAttackInput()) {
         InteractionHand hand = this.plannedBlockHand;
         LivingEntity threat = this.blockThreat;
         if (threat == null || hand == null) {
            this.stopBlockHit(false);
         } else if (!AutismLiteVariant.enabled() && ClutchModule.read_Z_20()) {
            this.stopBlockHit(false);
         } else {
            if (this.blockHitHand != null && hand != this.blockHitHand) {
               this.stopBlockHit(false);
            }

            int raise = blockDelayTicks(MC.player.getItemInHand(hand));
            int ownHit = this.ticksUntilOwnHit();
            ShieldHitWindowHelper.ShieldAction decision = this.isBlockHitCpsMode()
               ? ShieldHitWindowHelper.init(true, this.blockHitHand != null, raise, ownHit)
               : ShieldHitWindowHelper.init_l_CachePool_Z(true, this.blockHitHand != null, MC.player.hurtTime, raise, ownHit);
            if (AutismSessionState.getInstance().getClientTick() == this.lastUnblockTick) {
               decision = ShieldHitWindowHelper.ShieldAction.DOWN;
            }

            if (decision == ShieldHitWindowHelper.ShieldAction.DOWN) {
               this.stopBlockHit(false);
            } else if (this.blockHitHand != null
               && MC.player.isUsingItem()
               && MC.player.getUseItem().has(DataComponents.CONSUMABLE)
               && MC.player.getUseItemRemainingTicks() <= 2) {
               this.stopBlockHit(false);
            } else if (autismclient.util.InventorySlotArbiter.canBeginActionGroup(this.id())
               && (hand != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()))
               && autismclient.util.ClientInteractionOverride.queueShieldGuard(this.id(), hand)) {
               this.blockHitHand = hand;
               this.blockHitStack = MC.player.getItemInHand(hand);
               this.blockHitSlot = MC.player.getInventory().getSelectedSlot();
               this.blockHitThreatId = threat.getId();
            } else {
               this.stopBlockHit(false);
            }
         }
      } else {
         this.stopBlockHit(this.playerWantsToUseItem());
      }
   }

   private int ticksUntilOwnHit() {
      LivingEntity target = this.currentTarget;
      if (this.isBlockHitCpsMode()) {
         return target == null ? 1 : Math.max(1, this.clickScheduler.ticksUntilNextClick());
      }

      if (target != null && target.isAlive()) {
         RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
         if (rotation == null) {
            rotation = RotationMath.ofPlayer(MC.player);
         }

         EntityHitResult hit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
         int tick = AutismSessionState.getInstance().getClientTick();
         int open = this.shieldHitWindow.init_I(target.getUUID(), target.hurtTime, tick, this.estimateAttackDamage());
         int falling = this.ticksUntilFalling();
         boolean strongerSoon = falling <= 1
            && this.shieldHitWindow.init_I(target.getUUID(), target.hurtTime, tick, this.estimateAttackDamage(true)) == 0
            && this.inAttackRange(MC.player.getMainHandItem(), ShieldHitWindowHelper.closestPoint(target.getBoundingBox(), MC.player.getEyePosition()));
         if (strongerSoon) {
            open = Math.min(open, falling + 1);
         }

         if (hit != null && this.inAttackRange(MC.player.getMainHandItem(), hit.getLocation())
            || strongerSoon && this.canSeeBox(MC.player, MC.player.getEyePosition(), target.getBoundingBox())) {
            double click;
            if (this.isModernCombat()) {
               float delay = this.attackCooldownTicks();
               click = Math.max(0.0, delay * this.unusedSwingFactor + this.swingOffsetFor(delay, target) - 0.5 - this.attackStrengthTicker() - 1.0E-4F * delay);
            } else {
               click = this.clickScheduler.ticksUntilNextClick();
            }

            return Math.max(1, (int)Math.ceil(Math.max(open, click)));
         } else {
            return 2147483647;
         }
      } else {
         return 2147483647;
      }
   }

   private void planBlockHit() {
      this.plannedBlockHand = this.bool("block-hit") && this.canInteract() ? this.pickBlockingHand() : null;
      this.blockThreat = this.plannedBlockHand == null ? null : this.findBlockThreat(MC.player.getItemInHand(this.plannedBlockHand));
      if (this.blockThreat == null) {
         this.stopBlockHit(this.playerWantsToUseItem());
      }

      if (this.currentTarget == null && this.blockThreat == null) {
         this.shieldHitWindow.init();
      }
   }

   private static double[] enemyAttackRange(LivingEntity enemy) {
      AttackRange range = enemy.getMainHandItem().get(DataComponents.ATTACK_RANGE);
      double reach = enemy.getAttribute(Attributes.ENTITY_INTERACTION_RANGE) == null ? 3.0 : enemy.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
      boolean creativeReach = enemy instanceof Player p && p.hasInfiniteMaterials();
      double minimum = range == null ? 0.0 : (creativeReach ? range.minCreativeReach() : range.minReach());
      if (range != null) {
         reach = creativeReach ? range.maxCreativeReach() : range.maxReach() * (enemy instanceof Player ? 1.0F : range.mobFactor());
      }

      double margin = range == null ? 0.0 : range.hitboxMargin();
      return new double[]{minimum, reach, margin};
   }

   private static boolean isSwordBlocker(ItemStack guard) {
      return guard.is(ItemTags.SWORDS) && isSwordStyleBlocking(guard.get(DataComponents.BLOCKS_ATTACKS));
   }

   static boolean isSwordStyleBlocking(BlocksAttacks blocks) {
      if (blocks != null && blocks.blockDelayTicks() == 0 && blocks.damageReductions().size() == 1) {
         DamageReduction reduction = blocks.damageReductions().getFirst();
         return reduction.type().isEmpty() && Math.abs(reduction.base() + 0.5F) < 1.0E-4F && Math.abs(reduction.factor() - 0.5F) < 1.0E-4F;
      } else {
         return false;
      }
   }

   static boolean blocksAtAngle(BlocksAttacks blocks, Vec3 from, Vec3 threat, float yaw) {
      double angle = Math.abs(Mth.wrapDegrees(RotationMath.lookAt(threat, from).yaw() - yaw));

      for (DamageReduction reduction : blocks.damageReductions()) {
         if (angle <= reduction.horizontalBlockingAngle()) {
            return true;
         }
      }

      return false;
   }

   private LivingEntity findBlockThreat(ItemStack guard) {
      BlocksAttacks blocks = guard.get(DataComponents.BLOCKS_ATTACKS);
      boolean everySide = isSwordBlocker(guard) || blocks == null;
      RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
      float yaw = rotation == null ? MC.player.getYRot() : rotation.yaw();
      AABB body = MC.player.getBoundingBox();
      LivingEntity best = null;
      double bestDistance = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity enemy && this.isAttackable(enemy)) {
            Vec3 eye = observedPosition(enemy).add(0.0, enemy.getEyeHeight(), 0.0);
            Vec3 view = enemyLookDirection(enemy);
            double[] reach = enemyAttackRange(enemy);
            double distance = body.distanceToSqr(eye);
            if (ShieldHitWindowHelper.isInReachWindow(eye, body, reach[0], reach[1] + 0.5, reach[2])
               && ShieldHitWindowHelper.isLookingAt(eye, view, body)
               && this.canSeeBox(enemy, eye, body)
               && this.shieldHitWindow.init_Z(enemy.getUUID(), enemy.hurtTime, AutismSessionState.getInstance().getClientTick(), this.bool("block-hit-after-hit"))
               && (everySide || blocksAtAngle(blocks, MC.player.getEyePosition(), eye, yaw))
               && distance < bestDistance) {
               best = enemy;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   private static Vec3 observedPosition(LivingEntity enemy) {
      return EntityMotionTracker.observedPosition(enemy);
   }

   private static Vec3 enemyLookDirection(LivingEntity enemy) {
      InterpolationHandler interpolation = enemy.getInterpolation();
      boolean live = interpolation != null && interpolation.hasActiveInterpolation();
      float yaw = enemy instanceof Player ? (live ? Compat263.interpYRot(interpolation) : enemy.getYRot()) : enemy.getYHeadRot();
      return Vec3.directionFromRotation(live ? Compat263.interpXRot(interpolation) : enemy.getXRot(), yaw);
   }

   private boolean canSeeBox(LivingEntity enemy, Vec3 eye, AABB victim) {
      Vec3 nearest = ShieldHitWindowHelper.closestPoint(victim, eye);
      Vec3 center = victim.getCenter();
      Vec3[] points = new Vec3[]{
         nearest,
         center,
         new Vec3(center.x, victim.maxY - 0.1, center.z),
         new Vec3(center.x, victim.minY + 0.1, center.z),
         new Vec3(victim.minX + 0.05, center.y, victim.minZ + 0.05),
         new Vec3(victim.maxX - 0.05, center.y, victim.maxZ - 0.05)
      };

      for (Vec3 point : points) {
         if (MC.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, enemy)).getType() == Type.MISS) {
            return true;
         }
      }

      return false;
   }

   private double timerSpeedRatio() {
      double clientTickMs = MC.level.tickRateManager().millisecondsPerTick() / Math.max(0.05, MovementModuleHooks.speedTimerMultiplier());
      return ShieldHitWindowHelper.tickScale(clientTickMs);
   }

   private boolean isBlockHitCpsMode() {
      return this.bool("block-hit") && this.bool("block-hit-cps") && !this.isModernCombat();
   }

   private boolean blockHitAllowsHit(LivingEntity target) {
      return this.bool("block-hit") && !this.isBlockHitCpsMode() && !this.shieldBreakApplies(target)
         ? this.shieldHitWindow.load(target.getUUID(), target.hurtTime, AutismSessionState.getInstance().getClientTick(), this.estimateAttackDamage())
         : true;
   }

   private double estimateAttackDamage() {
      return this.estimateAttackDamage(false);
   }

   private double estimateAttackDamage(boolean forceCritical) {
      ItemStack stack = MC.player.getMainHandItem();
      double base = MC.player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      double charge = this.isModernCombat() ? MC.player.getAttackStrengthScale(0.5F) : 1.0;
      base *= 0.2 + charge * charge * 0.8;
      if (forceCritical || this.critConditionsMet() && MC.player.fallDistance > 0.0 && (!this.isModernCombat() || !MC.player.isSprinting())) {
         base *= 1.5;
      }

      int sharp = this.enchantmentLevel(stack, Enchantments.SHARPNESS);
      return base + (sharp == 0 ? 0.0 : (this.isModernCombat() ? (0.5 * sharp + 0.5) * charge : 1.25 * sharp));
   }

   private int ticksUntilFalling() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.fallTicksCacheTick) {
         return this.cachedTicksUntilFalling;
      }

      this.fallTicksCacheTick = tick;
      this.cachedTicksUntilFalling = 2147483647;
      Vec3 motion = MC.player.getDeltaMovement();
      if (this.critPhysicsAllowed(false) && (!this.isModernCombat() || !(MC.player.getAttackStrengthScale(0.5F) <= 0.9F) && !MC.player.isSprinting())) {
         if (MC.player.onGround() && MC.options.keyJump.isDown() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() <= 1) {
            motion = new Vec3(motion.x, Math.max(motion.y, ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower()), motion.z);
         }

         return this.cachedTicksUntilFalling = ShieldHitWindowHelper.init(MC.player.getBoundingBox(), motion, (body, step) -> Entity.collideBoundingBox(MC.player, step, body, MC.level, List.of()));
      } else {
         return this.cachedTicksUntilFalling;
      }
   }

   public boolean isBlockHitActive() {
      return this.holdsOwnBlockHit();
   }

   public int blockThreatId() {
      return this.blockHitThreatId;
   }

   public int pendingUnblockReleases() {
      return this.pendingUnblockReleaseCount;
   }

   static boolean init_Z_ItemStack_6(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      Item item = stack.getItem();
      return item instanceof EnderpearlItem
         || item instanceof SnowballItem
         || item instanceof EggItem
         || item instanceof ExperienceBottleItem
         || item instanceof ThrowablePotionItem
         || item instanceof WindChargeItem;
   }

   private boolean holdsAllowedItemNow() {
      if (MC.player == null) {
         return false;
      } else {
         ItemStack mainHand = MC.player.getMainHandItem();
         if (init_Z_ItemStack_6(mainHand)) {
            return true;
         } else {
            return !"BothHands".equals(this.choice("throwables")) && !mainHand.isEmpty() ? false : init_Z_ItemStack_6(MC.player.getOffhandItem());
         }
      }
   }

   private boolean holdsAllowedItem() {
      return this.allowedItemCache.init(AutismSessionState.getInstance().getClientTick(), this::holdsAllowedItemNow);
   }

   private boolean heldItemPassesFilter() {
      return this.heldItemFilterCache.init(AutismSessionState.getInstance().getClientTick(), this::heldItemPassesFilterNow);
   }

   private boolean heldItemPassesFilterNow() {
      if (MC.player != null && this.heldItemFilter.isEnabled()) {
         return this.switchBackSlot >= 0 && this.borrowedSlot == MC.player.getInventory().getSelectedSlot()
            ? true
            : this.heldItemFilter.accepts(MC.player.getMainHandItem());
      } else {
         return true;
      }
   }

   private boolean autoSwordActive() {
      return this.bool("auto-sword") && !this.heldItemFilter.isEnabled();
   }

   private boolean switchBackEnabled() {
      return this.heldItemFilter.isEnabled() ? this.bool("shield-break") : this.bool("switch-back");
   }

   private boolean canInteract() {
      return this.canInteract(false);
   }

   private boolean canInteract(boolean allowReleasedUse) {
      return this.canInteract(allowReleasedUse, false);
   }

   private boolean canInteract(boolean allowReleasedUse, boolean bedBudget) {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && (AutismLiteVariant.enabled() || !autismclient.ai.aP.load_Z())
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && (AutismLiteVariant.enabled() || !autismclient.util.CombatInteractionController.update())
         && (allowReleasedUse || !this.isBusyUsingItem())
         && !this.holdsAllowedItem()
         && (bedBudget ? this.heldItemPassesBedBudgetFilter() : this.heldItemPassesFilter())
         && (bedBudget || this.bedNukerBusyTick != AutismSessionState.getInstance().getClientTick())
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && !MacroExecutor.isRunning()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !f.load()
         && (AutismLiteVariant.enabled() || !DangerModule.sync_Z_12() && !ClutchModule.init_Z_49())
         && !ScaffoldModule.write_Z_5()
         && !ScaffoldModule.flush_Z_5()
         && !ScaffoldModule.update_Z_5()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !CrystalAuraModule.isRecentlyActive()
         && !AnchorAuraModule.isRecentlyActive()
         && !autoTrapOwnsRotation()
         && (AutismLiteVariant.enabled() || bedBudget || !KBDisplacementModule.init_Z_24())
         && (AutismLiteVariant.enabled() || bedBudget || !BedNukerModule.init_Z_23())
         && !BlinkPacketManager.isHoldingActionsOnly()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.init_Z_20();
   }

   private static boolean hasMissCooldown() {
      return ((AutismMinecraftAccessor)MC).autism$getMissTime() > 0;
   }

   private static boolean autoTrapOwnsRotation() {
      return "auto-trap".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   static boolean shouldRun(boolean windingDown, boolean enabled, boolean canRun, boolean scaffoldOwnsRotation) {
      return !scaffoldOwnsRotation && (windingDown || enabled && canRun);
   }

   @Override
   public boolean isSilentMovementActive() {
      if (!AutismLiteVariant.enabled() && autismclient.util.CombatInteractionController.update()) {
         return !SilentMovementCoordinator.isSafeWalkActive();
      }

      boolean enabled = this.isEnabled();
      return shouldRun(autismclient.util.SilentRotationManager.isReturningToPlayer(), enabled, enabled && this.canInteract(), SilentMovementCoordinator.isSafeWalkActive());
   }

   public static boolean moveFixEnabled() {
      return !(al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && !aura.bool("move-fix"));
   }

   @Override
   public boolean usesMoveFix() {
      return moveFixEnabled();
   }

   @Override
   public boolean rotatesVisibly() {
      return this.isEnabled() && !this.bool("silent");
   }

   public static void renderHud(GuiGraphicsExtractor graphics) {
      if (!AutismLiteVariant.enabled()) {
         KillAuraModule aura = activeInstance();
         if (aura != null && aura.bool("fov-circle") && MC.player != null && !MC.player.isSpectator() && autismclient.e.HashUtil.init()) {
            CameraRenderState camera = MC.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
            if (camera.initialized) {
               Window window = MC.getWindow();
               int cone = aura.integer("fov");
               float radiusX = coneRadiusPixels(cone, window.getWidth(), camera.projectionMatrix.m00());
               float radiusY = coneRadiusPixels(cone, window.getHeight(), camera.projectionMatrix.m11());
               if (radiusX > 0.0F && radiusY > 0.0F && !(Math.hypot(window.getWidth() / (2.0F * radiusX), window.getHeight() / (2.0F * radiusY)) < 0.99)) {
                  graphics.pose().pushMatrix();

                  try {
                     float pixel = 1.0F / window.getGuiScale();
                     graphics.pose().scale(pixel, pixel);
                     CrosshairVectorPipeline.init_V_GuiGraphicsExtract(graphics, window.getWidth() / 2.0F, window.getHeight() / 2.0F, radiusX, radiusY);
                  } finally {
                     graphics.pose().popMatrix();
                  }

                  graphics.nextStratum();
               }
            }
         }
      }
   }

   static float coneRadiusPixels(int cone, int pixels, float projectionScale) {
      return cone > 0 && cone < 180 && pixels > 0 && projectionScale > 0.0F
         ? (float)(Math.tan(Math.toRadians(cone / 2.0)) * pixels / 2.0 * projectionScale)
         : 0.0F;
   }

   private static KillAuraModule activeInstance() {
      return al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && aura.isEnabled() ? aura : null;
   }

   public LivingEntity getTarget() {
      return this.currentTarget;
   }

   LivingEntity activeTarget() {
      return this.isEnabled() && this.canInteract() ? this.currentTarget : null;
   }

   boolean init_Z_I_7(int holdMillis) {
      return this.currentTarget != null && BacktrackModule.attackWithinHold(this.ticksUntilReady(this.currentTarget), holdMillis);
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return this.isEnabled() && this.currentTarget != null && this.canInteract() ? hitResult == null || hitResult.getType() != Type.BLOCK : false;
   }

   private void selectTarget(boolean inAttackWindow) {
      boolean allowExcursion = !this.postAttackTracker.aimErrorTooHigh() && !this.missState.isPending();
      double interactionRange = this.attackReach();
      double normalRangeSq = interactionRange * interactionRange;
      List<LivingEntity> targets = this.collectCandidates();
      double maximumRange = effectiveScanRange(this.closestTargetDistanceSqr, interactionRange, this.scanRange());
      double keepRange = maximumRange + 0.5;
      List<LivingEntity> filtered = new ArrayList<>();

      for (LivingEntity entity : targets) {
         if (withinTargetRange(this.pickDistanceSqr(entity), maximumRange, entity == this.currentTarget)) {
            filtered.add(entity);
         }
      }

      filtered.sort(Comparator.comparingInt(entityx -> this.pickDistanceSqr(entityx) <= normalRangeSq ? 0 : 1));
      LivingEntity chosen = null;
      RotationMath.Rotation chosenRotation = null;
      boolean human = "Human".equals(this.choice("aim"));
      this.humanAimAnchor = null;
      int bestRangeBucket = filtered.isEmpty() ? 2147483647 : (this.pickDistanceSqr(filtered.get(0)) <= normalRangeSq ? 0 : 1);
      if (this.currentTarget != null) {
         for (LivingEntity entity : filtered) {
            if (entity == this.currentTarget) {
               int currentRangeBucket = this.pickDistanceSqr(entity) <= normalRangeSq ? 0 : 1;
               if (currentRangeBucket == bestRangeBucket) {
                  if (human) {
                     Vec3 anchor = this.humanAimAnchorFor(entity, keepRange);
                     if (anchor != null) {
                        chosen = entity;
                        this.humanAimAnchor = anchor;
                        chosenRotation = RotationMath.lookAt(anchor, MC.player.getEyePosition());
                     }
                  } else {
                     Vec3 preferred = this.aimPointFor(entity, inAttackWindow, allowExcursion);
                     chosenRotation = this.rotationToPoint(entity, keepRange, preferred);
                     if (chosenRotation != null) {
                        chosen = entity;
                     }
                  }
               }
               break;
            }
         }
      }

      if (chosen == null) {
         for (LivingEntity entity : filtered) {
            if (entity != this.currentTarget) {
               AABB box = entity.getBoundingBox();
               Vec3 acquisition = boxPoint(box, 0.5, 0.56, 0.5);
               RotationMath.Rotation rotation = this.rotationTo(entity, maximumRange, acquisition);
               if (rotation != null) {
                  if (!human) {
                     Vec3 preferred = this.aimPointFor(entity, inAttackWindow, allowExcursion);
                     RotationMath.Rotation tracked = this.rotationToPoint(entity, maximumRange, preferred);
                     chosen = entity;
                     chosenRotation = tracked != null ? tracked : rotation;
                     break;
                  }

                  Vec3 anchor = this.humanAimAnchorFor(entity, maximumRange);
                  if (anchor != null) {
                     chosen = entity;
                     this.humanAimAnchor = anchor;
                     chosenRotation = RotationMath.lookAt(anchor, MC.player.getEyePosition());
                     break;
                  }
               }
            }
         }
      }

      if (chosenRotation != null) {
         chosenRotation = this.applyMissChance(chosen, chosenRotation);
         autismclient.util.SilentRotationManager.requestKillAuraRotation(chosenRotation);
      } else {
         this.aimWander.reset();
         this.missState.clear();
      }

      this.currentTarget = chosen;
   }

   private Vec3 aimPointFor(LivingEntity entity, boolean inAttackWindow, boolean allowExcursion) {
      boolean ready = this.ticksUntilReady(entity) <= 3.0;
      AABB box = entity.getBoundingBox();
      Vec3 point = this.aimWander.nextPoint(entity.getId(), box, MC.player.getEyePosition(), inAttackWindow || ready, allowExcursion && !ready);
      return ready ? relativeBoxPoint(box, point) : point;
   }

   private boolean candidateInReach() {
      Vec3 eyes = MC.player.getEyePosition();
      double reach = this.attackReach() + 1.0;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && living.getBoundingBox().distanceToSqr(eyes) <= reach * reach && this.isValidNonPriorityCandidate(living, eyes)) {
            return true;
         }
      }

      return false;
   }

   static double init_D_9() {
      KillAuraModule aura = activeInstance();
      if (aura == null) {
         return 1.0 / 0.0;
      }

      if (aura.canInteract(false, true) && !autismclient.util.ManualInteractionYield.isYielding()) {
         int tick = AutismSessionState.getInstance().getClientTick();
         Vec3 eyes = MC.player.getEyePosition();
         double range = aura.attackReach();

         for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living
               && living.isAlive()
               && aura.isValidCandidate(living, eyes, true)
               && living.getBoundingBox().distanceToSqr(eyes) <= range * range
               && MC.player.hasLineOfSight(living)) {
               aura.bedBudgetThreat = living;
               aura.bedBudgetThreatTick = tick;
               break;
            }
         }

         LivingEntity threat = aura.bedBudgetThreat;
         boolean relevant = threat != null
            && threat.isAlive()
            && threat.level() == MC.level
            && aura.isValidCandidate(threat, eyes, true)
            && threat.getBoundingBox().distanceToSqr(eyes) <= (range + 1.0) * (range + 1.0);
         if (!recentlySeen(relevant, (long)tick - aura.bedBudgetThreatTick)) {
            aura.bedBudgetThreat = null;
            return 1.0 / 0.0;
         } else {
            return bedBudgetTicks(MC.player.hurtTime);
         }
      } else {
         aura.bedBudgetThreat = null;
         return 1.0 / 0.0;
      }
   }

   private boolean heldItemPassesBedBudgetFilter() {
      return AutismLiteVariant.enabled() ? this.heldItemPassesFilterNow() : !this.heldItemFilter.isEnabled() || this.heldItemPassesFilterNow() || this.heldItemFilter.accepts(BedNukerModule.init_ItemStack());
   }

   static boolean recentlySeen(boolean relevant, long ticksSinceSeen) {
      return relevant && ticksSinceSeen >= 0L && ticksSinceSeen <= 6L;
   }

   static int bedBudgetTicks(int hurtTime) {
      return hurtTime > 2 ? Math.min(6, hurtTime - 2) : 2;
   }

   static boolean withinTargetRange(double boxedDistanceSqr, double range, boolean alreadyTargeted) {
      double limit = alreadyTargeted ? range + 0.5 : range;
      return boxedDistanceSqr <= limit * limit;
   }

   static double effectiveScanRange(double closestDistanceSqr, double interactionRange, double scanRange) {
      return closestDistanceSqr > interactionRange * interactionRange ? scanRange : interactionRange;
   }

   private List<LivingEntity> collectCandidates() {
      List<LivingEntity> entities = new ArrayList<>();
      Vec3 eyes = MC.player.getEyePosition();

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && this.isValidNonPriorityCandidate(living, eyes)) {
            entities.add(living);
         }
      }

      if (entities.isEmpty()) {
         return entities;
      }

      entities.sort(this.targetComparator());
      double closest = 1.7976931348623157E308;

      for (LivingEntity entity : entities) {
         closest = Math.min(closest, this.pickDistanceSqr(entity));
      }

      this.closestTargetDistanceSqr = closest;
      return entities;
   }

   private boolean isValidNonPriorityCandidate(LivingEntity entity, Vec3 eyes) {
      return this.isValidCandidate(entity, eyes, false);
   }

   private boolean isValidCandidate(LivingEntity entity, Vec3 eyes, boolean priorityThreat) {
      if (entity == MC.player) {
         return false;
      } else if (entity.isRemoved()) {
         return false;
      } else if (!priorityThreat && entity.hurtTime > 10) {
         return false;
      } else {
         return !this.isAttackable(entity) ? false : this.angleToCenter(entity, eyes) <= this.integer("fov") * 0.5F;
      }
   }

   private boolean isAttackable(LivingEntity entity) {
      if (!(entity instanceof Attackable)) {
         return false;
      } else if (!EntitySelector.CAN_BE_PICKED.test(entity)) {
         return false;
      } else if (entity == MC.player || entity.hasPassenger(MC.player)) {
         return false;
      } else if (AntiBotTracker.shouldIgnoreEntity(entity)) {
         return false;
      } else if (TeamsModule.init(entity, "killaura")) {
         return false;
      } else if (!entity.isAlive()) {
         return false;
      } else {
         return entity instanceof Player player && player.isSleeping() ? false : this.matchesEntityFilter(entity);
      }
   }

   private float angleToCenter(Entity entity, Vec3 eyes) {
      RotationMath.Rotation toCenter = RotationMath.lookAt(entity.getBoundingBox().getCenter(), eyes);
      return RotationMath.vectorAngleDegrees(RotationMath.ofPlayer(MC.player), toCenter);
   }

   private boolean matchesEntityFilter(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.parsedEntityFilter();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> parsedEntityFilter() {
      String source = this.value("entities");
      if (Objects.equals(source, this.entityFilterSource)) {
         return this.entityFilterIds;
      }

      List<String> entries = this.list("entities");
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

      this.entityFilterSource = source;
      this.entityFilterIds = Set.copyOf(normalized);
      return this.entityFilterIds;
   }

   private int targetPriorityClass(LivingEntity entity) {
      if (entity instanceof Player) {
         return 0;
      }

      if (entity instanceof Enemy) {
         return 1;
      }

      if (entity instanceof NeutralMob neutral) {
         EntityReference<LivingEntity> angerTarget = neutral.getPersistentAngerTarget();
         if (angerTarget != null && angerTarget.matches(MC.player)) {
            return 2;
         }
      }

      return 2147483647;
   }

   private Comparator<LivingEntity> targetComparator() {
      Comparator var10000;
      switch (this.choice("targeting")) {
         case "Smart":
            var10000 = Comparator.comparingDouble(this::smartTargetScore);
            break;
         case "HP":
            var10000 = Comparator.comparingDouble(this::belowNameScore);
            break;
         case "Distance":
            var10000 = Comparator.comparingDouble(this::pickDistanceSqr);
            break;
         case "FOV":
            var10000 = Comparator.<LivingEntity>comparingDouble(entity -> this.angleToCenter(entity, MC.player.getEyePosition()));
            break;
         case "Hurt Time":
            var10000 = Comparator.<LivingEntity>comparingInt(entity -> entity.hurtTime);
            break;
         case "Age":
            var10000 = Comparator.<LivingEntity>comparingInt(entity -> -entity.tickCount);
            break;
         default:
            var10000 = Comparator.comparingInt(this::targetPriorityClass);
      }

      return var10000;
   }

   private double smartTargetScore(LivingEntity entity) {
      RotationMath.Rotation look = "kill-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) ? autismclient.util.SilentRotationManager.getCurrentRotation() : null;
      if (look == null) {
         look = RotationMath.ofPlayer(MC.player);
      }

      return autismclient.util.CombatTargetScorer.score(entity, MC.player, look);
   }

   private float belowNameScore(LivingEntity entity) {
      try {
         Scoreboard scoreboard = entity.level().getScoreboard();
         Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
         if (objective != null) {
            String displayName = objective.getDisplayName().getString();
            if (displayName.contains("❤")
               || displayName.contains("HP")
               || displayName.contains("Health")
               || displayName.contains("Здоровья")
               || displayName.contains("Здоровье")) {
               ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(entity, objective);
               if (score != null) {
                  return (float)score.value();
               }
            }
         }
      } catch (Throwable var6) {
      }

      return entity.getHealth();
   }

   private double pickDistanceSqr(Entity entity) {
      return entity.getBoundingBox().inflate(entity.getPickRadius()).distanceToSqr(MC.player.getEyePosition());
   }

   private RotationMath.Rotation rotationTo(Entity entity, double range, Vec3 preferredPoint) {
      Vec3 point = this.findVisibleAimPoint(entity, range, preferredPoint);
      return point == null ? null : RotationMath.lookAt(point, MC.player.getEyePosition());
   }

   private Vec3 findVisibleAimPoint(Entity entity, double range, Vec3 preferredPoint) {
      Vec3 eyes = MC.player.getEyePosition();
      AABB box = entity.getBoundingBox();
      double rangeSq = range * range;
      Vec3 preferred = relativeBoxPoint(box, preferredPoint);
      if (this.rotationToBoxPoint(eyes, box, preferred, rangeSq) != null) {
         return preferred;
      }

      RotationMath.Rotation reference = autismclient.util.SilentRotationManager.getCurrentRotation();
      if (reference == null) {
         reference = RotationMath.ofPlayer(MC.player);
      }

      Vec3 best = null;
      double bestScore = 1.0 / 0.0;
      double diagonal = Math.sqrt(box.getXsize() * box.getXsize() + box.getYsize() * box.getYsize() + box.getZsize() * box.getZsize());
      diagonal = Math.max(diagonal, 1.0E-6);

      for (double x : AIM_SAMPLE_XS) {
         for (double y : AIM_SAMPLE_YS) {
            for (double z : AIM_SAMPLE_XS) {
               Vec3 point = boxPoint(box, x, y, z);
               RotationMath.Rotation candidate = this.rotationToBoxPoint(eyes, box, point, rangeSq);
               if (candidate != null) {
                  double pointDistance = Math.sqrt(point.distanceToSqr(preferred)) / diagonal;
                  double score = RotationMath.vectorAngleDegrees(reference, candidate) + pointDistance * 8.0;
                  if (score < bestScore) {
                     best = point;
                     bestScore = score;
                  }
               }
            }
         }
      }

      return best;
   }

   private RotationMath.Rotation rotationToPoint(Entity entity, double range, Vec3 point) {
      return !entity.getBoundingBox().contains(point) ? RotationMath.lookAt(point, MC.player.getEyePosition()) : this.rotationTo(entity, range, point);
   }

   private RotationMath.Rotation rotationToBoxPoint(Vec3 eyes, AABB box, Vec3 point, double rangeSq) {
      if (eyes.distanceToSqr(point) <= 1.0E-12) {
         return null;
      } else {
         Vec3 hit = box.contains(eyes) ? eyes : clipBox(box, eyes, fma(eyes, 1.25, point.subtract(eyes)));
         if (hit != null && eyes.distanceToSqr(hit) < rangeSq) {
            return !this.hasLineOfSight(eyes, hit) ? null : RotationMath.lookAt(point, eyes);
         } else {
            return null;
         }
      }
   }

   static Vec3 boxPoint(AABB box, double x, double y, double z) {
      double safeX = Mth.clamp(x, 0.12, 0.88);
      double safeY = Mth.clamp(y, 0.2, 0.8);
      double safeZ = Mth.clamp(z, 0.12, 0.88);
      return new Vec3(Math.fma(box.getXsize(), safeX, box.minX), Math.fma(box.getYsize(), safeY, box.minY), Math.fma(box.getZsize(), safeZ, box.minZ));
   }

   static Vec3 relativeBoxPoint(AABB box, Vec3 point) {
      return point == null
         ? boxPoint(box, 0.5, 0.56, 0.5)
         : boxPoint(box, normalize(point.x, box.minX, box.maxX), normalize(point.y, box.minY, box.maxY), normalize(point.z, box.minZ, box.maxZ));
   }

   private static double normalize(double value, double min, double max) {
      double size = max - min;
      return size > 1.0E-9 ? (value - min) / size : 0.5;
   }

   private static Vec3 clipBox(AABB box, Vec3 from, Vec3 to) {
      return (box.contains(from) ? box.clip(to, from) : box.clip(from, to)).orElse(null);
   }

   private static Vec3 fma(Vec3 base, double scale, Vec3 other) {
      return new Vec3(Math.fma(scale, other.x, base.x), Math.fma(scale, other.y, base.y), Math.fma(scale, other.z, base.z));
   }

   private boolean hasLineOfSight(Vec3 eyes, Vec3 point) {
      return this.hasLineOfSight(eyes, point, MC.player);
   }

   private boolean hasLineOfSight(Vec3 eyes, Vec3 point, Entity entity) {
      return MC.level.clip(new ClipContext(eyes, point, Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   private EntityHitResult raycastEntity(double range, RotationMath.Rotation rotation, Predicate<Entity> predicate) {
      Entity camera = MC.getCameraEntity();
      if (camera == null) {
         return null;
      } else {
         Vec3 eyes = camera.getEyePosition();
         Vec3 direction = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
         AttackRange itemRange = MC.player.getMainHandItem().get(DataComponents.ATTACK_RANGE);
         if (itemRange != null) {
            Vec3 from = eyes.add(direction.scale(itemRange.effectiveMinRange(MC.player)));
            Vec3 to = eyes.add(direction.scale(autismclient.util.AttackReachHelper.reachAlong(MC.player, direction)));
            AABB area = camera.getBoundingBox().expandTowards(to.subtract(eyes)).inflate(1.0);
            return ProjectileUtil.getManyEntityHitResult(
                  MC.level, camera, from, to, area, EntitySelector.CAN_BE_PICKED.and(predicate), itemRange.hitboxMargin(), Block.OUTLINE, true, true
               )
               .stream()
               .min(Comparator.comparingDouble(hit -> eyes.distanceToSqr(hit.getLocation())))
               .orElse(null);
         } else {
            Vec3 end = eyes.add(direction.x * range, direction.y * range, direction.z * range);
            AABB search = camera.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0, 1.0, 1.0);
            return ProjectileUtil.getEntityHitResult(camera, eyes, end, search, EntitySelector.CAN_BE_PICKED.and(predicate), range * range);
         }
      }
   }

   private EntityHitResult raycastTarget(Entity target, RotationMath.Rotation rotation, double range, double wallsRange) {
      Entity camera = MC.getCameraEntity();
      if (camera == null) {
         return null;
      }

      EntityHitResult hit = this.raycastEntity(range, rotation, entity -> entity == target);
      if (hit != null && hit.getEntity() == target) {
         Vec3 eyes = camera.getEyePosition();
         double distanceSq = eyes.distanceToSqr(hit.getLocation());
         if (MC.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)) {
            range = Math.max(range, autismclient.util.AttackReachHelper.reachAlong(MC.player, Vec3.directionFromRotation(rotation.pitch(), rotation.yaw())));
         }

         return distanceSq <= wallsRange * wallsRange || distanceSq <= range * range && this.hasLineOfSight(eyes, hit.getLocation(), camera) ? hit : null;
      } else {
         return null;
      }
   }

   private void tickAttack() {
      LivingEntity target = this.currentTarget;
      if (target == null) {
         this.clickScheduler.noteLostReason("noTarget");
      } else if (!this.blockHitAllowsHit(target)) {
         this.missState.clear();
         this.clickScheduler.carryPending();
      } else if (ScaffoldModule.flush_Z_5()) {
         this.clickScheduler.noteLostReason("scaffold");
         this.missState.clear();
      } else if (this.holdsOwnBlockHit() || !this.missState.isPending() || !this.tryWhiffSwing(target)) {
         RotationMath.Rotation rotation = this.plannedRotation();
         this.attemptAttack(target, rotation);
      }
   }

   private RotationMath.Rotation plannedRotation() {
      RotationMath.Rotation planned = autismclient.util.SilentRotationManager.getCurrentRotation();
      if (planned == null) {
         planned = RotationMath.ofPlayer(MC.player);
      }

      return serverRotationOr(planned, ServerRotationTracker.getSnapshot());
   }

   static RotationMath.Rotation serverRotationOr(RotationMath.Rotation planned, ServerRotationTracker.RotationSnapshot wire) {
      return wire.initialized() ? new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch()) : planned;
   }

   public static EntityHitResult init_EntityHitResult_EntityHitResult_ch_CachePool(EntityHitResult queued, ServerRotationTracker.RotationSnapshot wire) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      KillAuraModule aura = activeInstance();
      if (aura != null
         && aura.canInteract()
         && queued != null
         && queued.getEntity() == aura.currentTarget
         && aura.currentTarget != null
         && aura.currentTarget.isAlive()) {
         RotationMath.Rotation look = serverRotationOr(RotationMath.ofPlayer(MC.player), wire);
         if (aura.currentTarget instanceof Player player && !aura.hitMatchesReceivedPoseFrom(player, look, wire.position())) {
            return null;
         } else {
            EntityHitResult hit = aura.raycastTarget(aura.currentTarget, look, aura.attackReach(), 0.0);
            return hit != null && aura.inAttackRange(MC.player.getMainHandItem(), hit.getLocation()) ? hit : null;
         }
      } else {
         return null;
      }
   }

   private boolean tryWhiffSwing(LivingEntity target) {
      if (AutismSessionState.getInstance().getClientTick() != this.lastUnblockTick && this.blockThreat == null) {
         RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
         if (rotation == null) {
            rotation = RotationMath.ofPlayer(MC.player);
         }

         EntityHitResult hit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
         if (hit != null && this.inAttackRange(MC.player.getMainHandItem(), hit.getLocation())) {
            this.missState.markSpent();
            return false;
         }

         Vec3 eyes = MC.player.getEyePosition();
         double reach = this.attackReach();
         if (this.fakeMissPlausible(target, eyes)
            && rotationHitsBox(eyes, target.getBoundingBox(), rotation)
            && rotationNearlyHitsBox(eyes, target.getBoundingBox(), rotation)
            && !(target.getBoundingBox().distanceToSqr(eyes) > reach * reach)
            && this.canAttackWith(target, MC.player.getMainHandItem())) {
            if (!this.isModernCombat()) {
               if (!this.clickScheduler.clickDueNow()) {
                  return false;
               }

               if (!autismclient.util.ClientInteractionOverride.queueAttackMiss(this.id(), BlockHitResult.miss(eyes, Direction.DOWN, MC.player.blockPosition()), true)) {
                  this.missState.markSpent();
                  this.clickScheduler.noteLostReason("whiffQueue");
                  return false;
               }
            } else {
               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.ClickRateTracker.recordLeftClick();
            }

            this.clickScheduler.consumeClick("whiff");
            this.resetMissCycle();
            return true;
         } else {
            this.missState.markSpent();
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean canHitCurrentTargetNow() {
      LivingEntity target = this.currentTarget;
      if (target != null && AutismSessionState.getInstance().getClientTick() != this.slotSwitchTick) {
         boolean shieldBreak = this.isModernCombat() && this.shieldBreakApplies(target);
         boolean lastChance = !shieldBreak && this.lastChanceToHit(target);
         if (!hasMissCooldown() && (shieldBreak || lastChance || this.clickScheduler.canClickNow())) {
            ItemStack stack = MC.player.getMainHandItem();
            boolean previousLastChance = this.lastChanceHit;

            try {
               this.lastChanceHit = lastChance;
               if (!this.canAttackWith(target, stack)) {
                  return false;
               }
            } finally {
               this.lastChanceHit = previousLastChance;
            }

            RotationMath.Rotation rotation = this.plannedRotation();
            EntityHitResult hit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
            return hit != null
               && this.inAttackRange(stack, hit.getLocation())
               && !(target instanceof Player player && !this.hitMatchesReceivedPose(player, rotation));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void attemptAttack(Entity target, RotationMath.Rotation rotation) {
      EntityHitResult attackHit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
      boolean isInRange = attackHit != null && this.inAttackRange(MC.player.getMainHandItem(), attackHit.getLocation());
      if (!isInRange) {
         if (!this.isModernCombat() && !this.holdsOwnBlockHit()) {
            Vec3 eyes = MC.player.getEyePosition();
            double distance = Math.sqrt(target.getBoundingBox().distanceToSqr(eyes));
            boolean legalSwing = distance > this.attackReach() || rotationHitsBox(eyes, target.getBoundingBox(), rotation);
            if (!autismclient.util.RecentCombatTracker.init(target) && this.clickScheduler.clickDueNow() && legalSwing && distance <= this.attackReach() + this.preReachFor(target)
               )
             {
               boolean nearMiss = distance <= this.attackReach();

               while (this.clickScheduler.clickDueNow()) {
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  autismclient.util.ClickRateTracker.recordLeftClick();
                  this.clickScheduler.consumeClick(nearMiss ? "nearMiss" : "poke");
               }

               if (nearMiss) {
                  this.resetMissCycle();
               }
            } else if (!legalSwing) {
               this.clickScheduler.carryOrNoteLoss("ring");
            } else {
               this.clickScheduler.noteLostReason("far");
            }
         }

         this.prepareWeapon(target);
      } else if (target instanceof Player player && !this.hitMatchesReceivedPose(player, rotation)) {
         this.clickScheduler.carryOrNoteLoss("trackedYaw");
      } else if (AutismSessionState.getInstance().getClientTick() != this.slotSwitchTick && !this.trySwitchToWeapon(target) && (this.holdBorrowedWeapon(target) || !this.restoreSwitchBackSlot())) {
         if (target instanceof Player && SprintStateTracker.isDesynced() && MC.player.getAttackStrengthScale(0.5F) > 0.9F) {
            this.clickScheduler.carryPending();
         } else {
            ItemStack mainHandStack = MC.player.getMainHandItem();
            boolean shieldBreakHit = target instanceof LivingEntity living && this.shieldBreakApplies(living);
            this.lastChanceHit = !shieldBreakHit && this.lastChanceToHit(target);
            boolean offSchedule = this.isModernCombat() && (shieldBreakHit || this.lastChanceHit);

            try {
               if ((this.clickScheduler.clickDueNow() || offSchedule) && this.canAttackWith(target, mainHandStack)) {
                  this.clickScheduler.bypassNextClick = shieldBreakHit || this.lastChanceHit;
                  this.queueAttackClicks(target, attackHit, mainHandStack, shieldBreakHit);
                  return;
               }

               this.clickScheduler.noteLostReason("cannotAttack");
            } finally {
               this.lastChanceHit = false;
            }

            return;
         }
      } else {
         this.clickScheduler.carryPending();
      }
   }

   public String clickDebugSummary(boolean reset) {
      return this.clickScheduler.debugSummary(reset);
   }

   private boolean lastChanceToHit(Entity target) {
      if (this.isModernCombat() && !this.attackChargedIn(0) && !(MC.player.getAttackStrengthScale(0.5F) <= 0.9F)) {
         Vec3 motion = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
         return autismclient.util.AttackReachHelper.init(
            MC.player.getEyePosition(), target.getBoundingBox(), ShieldHitWindowHelper.sanitizeMotion(MC.player.getDeltaMovement()), ShieldHitWindowHelper.sanitizeMotion(motion), this.attackReach()
         );
      } else {
         return false;
      }
   }

   private boolean hitMatchesReceivedPose(Player target, RotationMath.Rotation look) {
      return this.hitMatchesReceivedPoseFrom(target, look, ServerRotationTracker.getSnapshot().position());
   }

   private boolean hitMatchesReceivedPoseFrom(Player target, RotationMath.Rotation look, Vec3 sentPosition) {
      if (!this.motionTracker.isTracking(target)) {
         return true;
      }

      Vec3 eye = sentPosition == null ? MC.player.position() : sentPosition;
      Vec3 eyes = eye.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 sampled = BacktrackModule.init_Vec3_LivingEntity_Vec3(target, eyes);
      if (sampled == null) {
         sampled = this.sampledReceivedPosition();
      }

      if (bearingWithinTolerance(eye, sampled, look.yaw()) && bearingWithinTolerance(eye, target.position(), look.yaw())) {
         AABB body = target.getBoundingBox();
         AABB received = body.move(sampled.subtract(target.position()));
         Vec3 bodyHit = rayBoxHitPoint(eyes, look, this.attackReach(), body);
         Vec3 receivedHit = rayBoxHitPoint(eyes, look, this.attackReach(), received);
         if (bodyHit != null && receivedHit != null && this.hasLineOfSight(eyes, receivedHit, MC.player)) {
            RotationMath.Rotation planned = autismclient.util.SilentRotationManager.getCurrentRotation();
            if (planned == null) {
               return true;
            }

            Vec3 nowEyes = MC.player.getEyePosition();
            Vec3 motion = MC.player.getDeltaMovement();
            double rise = motion.y;
            if (MC.player.onGround()) {
               rise = Math.max(0.0, rise);
               if (MC.options.keyJump.isDown() && !MC.player.isInWater() && !MC.player.isInLava()) {
                  rise = Math.max(rise, ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower());
               }
            }

            Vec3 nextEyes = nowEyes.add(motion.x, rise, motion.z);
            return rayBoxHitPoint(nowEyes, planned, this.attackReach(), body) != null
               && rayBoxHitPoint(nowEyes, planned, this.attackReach(), received) != null
               && rayBoxHitPoint(nextEyes, planned, this.attackReach(), body) != null
               && rayBoxHitPoint(nextEyes, planned, this.attackReach(), received) != null
               && bearingWithinTolerance(nextEyes, sampled, planned.yaw());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static Vec3 rayBoxHitPoint(Vec3 eyes, RotationMath.Rotation look, double reach, AABB box) {
      AABB inset = box.deflate(0.003);
      if (inset.contains(eyes)) {
         return eyes;
      }

      Vec3 end = eyes.add(Vec3.directionFromRotation(look.pitch(), look.yaw()).scale(Math.max(0.0, reach)));
      return inset.clip(eyes, end).orElse(null);
   }

   private Vec3 sampledReceivedPosition() {
      int latency = 0;
      if (MC.getConnection() != null && MC.player != null) {
         PlayerInfo info = MC.getConnection().getPlayerInfo(MC.player.getUUID());
         if (info != null) {
            latency = Math.max(0, info.getLatency());
         }
      }

      int age = 2 + Math.min(10, latency / 100);
      return this.motionTracker.positionAt(AutismSessionState.getInstance().getClientTick(), age);
   }

   static boolean bearingWithinTolerance(Vec3 eye, Vec3 target, float yaw) {
      double dx = target.x - eye.x;
      double dz = target.z - eye.z;
      if (dx * dx + dz * dz < 1.0E-8) {
         return true;
      }

      double bearing = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
      return Math.abs(Mth.wrapDegrees(bearing - yaw)) <= Math.toDegrees(0.3);
   }

   private void queueAttackClicks(Entity target, EntityHitResult attackHit, ItemStack mainHandStack, boolean shieldBreakHit) {
      this.runAttackClicks(() -> !this.canAttackWith(target, mainHandStack) ? false : this.sendPrimaryAttack(target, attackHit));
   }

   private RotationMath.Rotation applyMissChance(LivingEntity target, RotationMath.Rotation rotation) {
      if (this.holdsOwnBlockHit() || !this.blockHitAllowsHit(target)) {
         this.missState.clear();
         return rotation;
      }

      if (autismclient.util.RecentCombatTracker.init(target)) {
         this.missState.clear();
         return rotation;
      }

      if (this.missState.isPending()) {
         if (this.integer("miss-chance") <= 0 || this.missState.isLegacy() == this.isModernCombat()) {
            this.missState.clear();
            return rotation;
         } else if (!this.missState.isMissTarget(target.getId())) {
            this.missState.clear();
            return rotation;
         } else if (this.missState.missDue(AutismSessionState.getInstance().getClientTick())) {
            this.missState.markSpent();
            return rotation;
         } else {
            this.missState.trackTargetCenter(target.getBoundingBox().getCenter());
            return "Human".equals(this.choice("aim")) ? rotation : RotationMath.lookAt(this.missState.getMissPoint(), MC.player.getEyePosition());
         }
      } else {
         if (!this.missState.canPlanMiss() || this.integer("miss-chance") <= 0 || this.readyStallTicks >= 12 || !this.clickScheduler.canClickNow()) {
            return rotation;
         }

         if (!this.canAttackWith(target, MC.player.getMainHandItem())) {
            return rotation;
         }

         if (this.isLowHealthForDamage(target)) {
            return rotation;
         }

         if (!this.shieldBreakApplies(target) && !this.lastChanceToHit(target)) {
            EntityHitResult reachable = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
            if (reachable != null && this.inAttackRange(MC.player.getMainHandItem(), reachable.getLocation())) {
               if (this.missRandom.nextDouble() >= this.integer("miss-chance") * 0.01) {
                  return rotation;
               }

               this.missState
                  .planMiss(
                     target.getId(),
                     randomAimPoint(target.getBoundingBox(), MC.player.getEyePosition(), this.missRandom),
                     !this.isModernCombat(),
                     AutismSessionState.getInstance().getClientTick()
                  );
               this.missState.trackTargetCenter(target.getBoundingBox().getCenter());
               return "Human".equals(this.choice("aim")) ? rotation : RotationMath.lookAt(this.missState.getMissPoint(), MC.player.getEyePosition());
            } else {
               return rotation;
            }
         } else {
            return rotation;
         }
      }
   }

   private void tickStallCounter() {
      LivingEntity target = this.currentTarget;
      double reach = this.attackReach();
      boolean ready = target != null
         && this.ticksSinceLastAttack > 0
         && target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach
         && (this.isModernCombat() ? this.attackChargedIn(0) : this.ticksSinceLastAttack > 6);
      this.readyStallTicks = ready ? Math.min(this.readyStallTicks + 1, 1000) : 0;
      if (this.readyStallTicks >= 12 && MC.gui.screen() == null && hasMissCooldown()) {
         MC.missTime = 0;
      }
   }

   private boolean isLowHealthForDamage(LivingEntity target) {
      return target.getHealth() <= MC.player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5;
   }

   static Vec3 randomAimPoint(AABB box, Vec3 eyes, Random random) {
      Vec3 center = box.getCenter();
      double halfWidth = Math.min(box.getXsize(), box.getZsize()) * 0.5;
      double lateral = halfWidth + 0.1 + random.nextDouble() * 0.1;
      if (random.nextBoolean()) {
         lateral = -lateral;
      }

      double depth = (random.nextDouble() * 2.0 - 1.0) * 0.1;
      double dy = (random.nextDouble() * 2.0 - 1.0) * 0.02;
      if (dy == 0.0) {
         dy = 0.01;
      }

      double dx = center.x - eyes.x;
      double dz = center.z - eyes.z;
      double horizontal = Math.max(Math.sqrt(dx * dx + dz * dz), 0.3);
      double dirX = dx / horizontal;
      double dirZ = dz / horizontal;
      return new Vec3(center.x + dirX * depth - dirZ * lateral, center.y + dy, center.z + dirZ * depth + dirX * lateral);
   }

   private boolean trySwitchToWeapon(Entity target) {
      if (target instanceof LivingEntity living) {
         boolean shieldBreak = this.shouldBreakShield(living);
         if (!this.autoSwordActive() && !shieldBreak) {
            return false;
         }

         Integer slot = this.findWeaponSlot(living, shieldBreak);
         if (slot != null && !this.usingConsumableMainHand()) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (selected == slot) {
               return false;
            }

            if (this.switchBackEnabled()) {
               if (this.switchBackSlot < 0) {
                  this.switchBackSlot = selected;
               }

               this.switchBackDelayTicks = 20;
            }

            int tick = AutismSessionState.getInstance().getClientTick();
            if (tick == this.slotSwitchTick) {
               return true;
            }

            if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
               return true;
            }

            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
               this.borrowedSlot = slot;
               this.slotSwitchTick = tick;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void prepareWeapon(Entity target) {
      if (!this.trySwitchToWeapon(target)) {
         int selected = MC.player.getInventory().getSelectedSlot();
         if (this.switchBackEnabled() && this.switchBackSlot >= 0 && this.borrowedSlot == selected) {
            this.switchBackDelayTicks = 20;
         }
      }
   }

   private boolean holdBorrowedWeapon(Entity target) {
      if (this.switchBackEnabled() && this.switchBackSlot >= 0 && this.borrowedSlot == MC.player.getInventory().getSelectedSlot()) {
         boolean engaged = this.autoSwordActive() || this.switchBackDelayTicks > 0 && target instanceof LivingEntity living && this.shouldBreakShield(living);
         if (!engaged) {
            return false;
         }

         this.switchBackDelayTicks = 20;
         return true;
      } else {
         return false;
      }
   }

   private boolean shouldBreakShield(LivingEntity target) {
      if (!this.bool("shield-break")) {
         return false;
      } else {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (this.attackWouldBeBlocked(target)) {
            this.shieldSeenTick = tick;
            this.shieldSeenTargetId = target.getId();
            return true;
         } else {
            return target instanceof Player && target.getId() == this.shieldBrokenTargetId && tick - this.shieldBrokenTick <= 100
               ? false
               : target.getId() == this.shieldSeenTargetId && tick - this.shieldSeenTick <= 10;
         }
      }
   }

   private boolean shieldBreakApplies(LivingEntity target) {
      return this.bool("shield-break") && this.attackWouldBeBlocked(target);
   }

   private boolean restoreSwitchBackSlot() {
      if (this.switchBackEnabled() && this.switchBackSlot >= 0 && this.switchBackDelayTicks <= 0) {
         if (this.borrowedSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.borrowedSlot) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!CrystalAuraModule.hasBorrowedSlot(selected) && !AnchorAuraModule.hasSwitchedToSlot(selected)) {
               this.switchBackSlot = -1;
               this.borrowedSlot = -1;
               return false;
            } else {
               return false;
            }
         } else {
            int back = this.switchBackSlot;
            if (MC.player.getInventory().getSelectedSlot() == back) {
               this.switchBackSlot = -1;
               this.borrowedSlot = -1;
               return false;
            }

            int tick = AutismSessionState.getInstance().getClientTick();
            if (tick == this.slotSwitchTick) {
               return false;
            }

            if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
               return false;
            }

            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, back);
               this.slotSwitchTick = tick;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }

            this.switchBackSlot = -1;
            this.borrowedSlot = -1;
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean canAttackWith(Entity target, ItemStack stack) {
      if (target instanceof LivingEntity living && !this.blockHitAllowsHit(living)) {
         return false;
      } else if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      } else if (MC.player.cannotAttackWithItem(stack, 0)) {
         return false;
      } else if (ScaffoldModule.flush_Z_5()) {
         return false;
      } else if (this.itemUseCooldownTicks > 0) {
         return false;
      } else if (this.ticksSinceMissSwing == 0) {
         return false;
      } else {
         return this.lastChanceHit
            ? true
            : !(this.bool("criticals") && target instanceof LivingEntity living) || MC.player.isFallFlying() || this.shieldBreakApplies(living) || !this.holdingForCrit(living);
      }
   }

   private boolean holdingForCrit(LivingEntity target) {
      if (!this.critReachableByFall()) {
         this.critWaitTargetId = -1;
         this.critWaitStartTick = -2147483648;
         return false;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.critWaitTargetId != target.getId()) {
         this.critWaitTargetId = target.getId();
         this.critWaitStartTick = tick;
      }

      return tick - this.critWaitStartTick < 4;
   }

   private boolean critReachableByFall() {
      double motionY = MC.player.getDeltaMovement().y;
      if (this.critPhysicsInAir() && !(motionY < -0.08)) {
         float ticksTillCrit = Math.max(this.ticksUntilNearlyCharged(), (float)(motionY / 0.08));
         float damageOnCrit = 0.375F;
         return damageOnCrit <= this.attackStrengthAfter(ticksTillCrit) ? false : this.canFallFreely((int)(ticksTillCrit * 1.3F));
      } else {
         return false;
      }
   }

   private float ticksUntilNearlyCharged() {
      return Math.max(this.attackCooldownTicks() * 0.9F - 0.5F - this.attackStrengthTicker(), 0.0F);
   }

   private float attackStrengthAfter(float ticks) {
      float base = (ticks + 0.5F) / this.attackCooldownTicks();
      return Math.min(0.2F + base * base * 0.8F, 1.0F);
   }

   private boolean canFallFreely(int ticks) {
      double motionY = MC.player.getDeltaMovement().y;
      AABB box = MC.player.getBoundingBox();

      for (int i = 0; i < ticks; i++) {
         motionY = (motionY - 0.08) * 0.98;
         box = box.move(0.0, motionY, 0.0);
         if (!MC.level.noCollision(MC.player, box)) {
            return false;
         }
      }

      return true;
   }

   private boolean critClickImminent() {
      return this.bool("criticals")
         && MC.player != null
         && !MC.player.onGround()
         && this.currentTarget != null
         && this.clickScheduler.clickDueWithin(1)
         && !(this.currentTarget instanceof LivingEntity living && this.shieldBreakApplies(living));
   }

   public static boolean close_Z_6() {
      KillAuraModule aura = activeInstance();
      return aura != null && aura.critClickImminent();
   }

   public static boolean load_Z_I_3(int slot) {
      return al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && aura.isEnabled() ? aura.switchBackSlot >= 0 && aura.borrowedSlot == slot : false;
   }

   private void runAttackClicks(BooleanSupplier attack) {
      if (this.clickScheduler.canClickNow()) {
         if (this.holdsOwnBlockHit()) {
            this.stopBlockHit(false);
            this.clickScheduler.carryPending();
         } else if (AutismSessionState.getInstance().getClientTick() == this.lastUnblockTick) {
            this.clickScheduler.carryPending();
         } else if (!MC.player.isBlocking() && !MC.player.isUsingItem()) {
            this.clickScheduler.runClicks(attack);
         }
      }
   }

   private boolean sendPrimaryAttack(Entity target, EntityHitResult hit) {
      return this.sendAttack(target, hit, false);
   }

   private boolean sendAttack(Entity target, EntityHitResult hit, boolean followUp) {
      if (AutismLiteVariant.enabled()) {
         return false;
      } else {
         RotationMath.Rotation look = this.plannedRotation();
         if (target instanceof Player player && !this.hitMatchesReceivedPose(player, look)) {
            return false;
         } else {
            ItemStack stack = MC.player.getMainHandItem();
            double damage = this.estimateAttackDamage();
            boolean brokeShield = target instanceof LivingEntity living && this.shieldBreakApplies(living) && stack.is(ItemTags.AXES);
            boolean lastChance = this.lastChanceHit;
            Runnable dispatched = () -> this.onAttackDispatched(target, damage, followUp, brokeShield, lastChance);
            PiercingWeapon piercing = stack.get(DataComponents.PIERCING_WEAPON);
            if (piercing != null && followUp) {
               return false;
            }

            if (piercing != null && !MC.gameMode.isSpectator()) {
               Compat263.piercingAttack(MC.gameMode, piercing);
               autismclient.util.RecentCombatTracker.init(MC.player, target);
               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.ClickRateTracker.recordLeftClick();
               dispatched.run();
               return true;
            }

            if (!this.isAttackableEntity(target)) {
               return false;
            }

            if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
               return false;
            }

            try {
               ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
            }

            if ("Packet".equals(this.choice("attack-mode"))) {
               if (MC.gameMode.isSpectator()) {
                  MC.getConnection().send(new ServerboundAttackPacket(target.getId()));
                  autismclient.util.RecentCombatTracker.init(MC.player, target);
               } else {
                  autismclient.util.CombatAttackDispatcher.init(target, () -> MC.gameMode.attack(MC.player, target), dispatched);
               }

               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.ClickRateTracker.recordLeftClick();
               if (MC.gameMode.isSpectator()) {
                  dispatched.run();
               }

               return true;
            } else {
               if (!autismclient.util.CombatAttackDispatcher.init(this.id(), hit, dispatched)) {
                  return false;
               }

               this.attackDispatchPending = true;
               return true;
            }
         }
      }
   }

   private void onAttackDispatched(Entity target, double damage, boolean followUp, boolean brokeShield, boolean lastChance) {
      if (followUp) {
         this.postAttackTracker.markAttack();
         this.ticksSinceLastAttack = 0;
         this.swingOffset = -1.0F / 0.0F;
      } else {
         this.trackHitLanding(target, damage);
         this.readyStallTicks = 0;
         if (brokeShield) {
            this.shieldBrokenTick = AutismSessionState.getInstance().getClientTick();
            this.shieldBrokenTargetId = target.getId();
         }

         this.postAttackTracker.markAttack();
         this.ticksSinceLastAttack = 0;
         this.critWaitTargetId = -1;
         this.critWaitStartTick = -2147483648;
         if (this.aimEngine != null) {
            this.aimEngine.recordSwing();
         }

         this.missState.clearOnAttack();
         if (this.isModernCombat()) {
            this.scheduleDoubleClick(target, lastChance);
         }

         if (this.switchBackEnabled() && this.switchBackSlot >= 0) {
            this.switchBackDelayTicks = brokeShield ? 0 : 20;
         }

         this.scanRangeMargin = this.rollScanRangeMargin();
         this.rerollSwingOffset();
         lastAttackMillis = System.currentTimeMillis();
         this.clickScheduler.ticksSinceAttack = 0;
      }
   }

   public static void read_V_14() {
      if (!AutismLiteVariant.enabled()) {
         KillAuraModule aura = activeInstance();
         if (aura != null && aura.attackDispatchPending && MC.player != null) {
            aura.attackDispatchPending = false;
            aura.recordSentRotation();
         }
      }
   }

   private boolean isAttackableEntity(Entity target) {
      return target != null
         && target != MC.player
         && !(target instanceof ItemEntity)
         && !(target instanceof ExperienceOrb)
         && (!(target instanceof AbstractArrow) || target.isAttackable());
   }

   private void trackHitLanding(Entity target, double damage) {
      if (target instanceof LivingEntity living) {
         this.shieldHitWindow.init_V(living.getUUID(), living.hurtTime, AutismSessionState.getInstance().getClientTick(), damage);
         this.hitLandingTargetId = living.getId();
         this.hitLandingHurtTime = living.hurtTime;
         this.hitLandingTicksLeft = 8;
      }
   }

   private void pollHitLanding() {
      if (this.hitLandingTargetId >= 0) {
         Entity entity = MC.level.getEntity(this.hitLandingTargetId);
         boolean landed = entity instanceof LivingEntity living && (living.hurtTime > this.hitLandingHurtTime || this.hitLandingHurtTime >= 10 && living.hurtTime >= 10);
         if (landed) {
            if (entity instanceof LivingEntity living && living.hurtTime > this.hitLandingHurtTime) {
               this.shieldHitWindow.init_V_UUID_I(living.getUUID(), AutismSessionState.getInstance().getClientTick());
            }

            if (this.bool("hitsound") || this.bool("hit-marker")) {
               this.showHitMarker(entity);
               this.playHitSound();
            }
         }

         if (landed || entity == null || --this.hitLandingTicksLeft <= 0) {
            this.hitLandingTargetId = -1;
         }
      }
   }

   private void showHitMarker(Entity target) {
      if (this.bool("hit-marker")) {
         autismclient.util.KillAuraHitMarkerRenderer.markHit(target.getBoundingBox().inflate(target.getPickRadius()));
      }
   }

   private void playHitSound() {
      if (this.bool("hitsound")) {
         MC.getSoundManager().play(SimpleSoundInstance.forUI(HIT_SOUND, 1.0F, 0.7F));
      }
   }

   private boolean unusedCritFalling() {
      return this.critConditionsMet() && MC.player.fallDistance > 0.0;
   }

   private boolean critConditionsMet() {
      return this.critPhysicsInAir() && (!this.isModernCombat() || MC.player.getAttackStrengthScale(0.5F) > 0.9F);
   }

   private boolean critPhysicsInAir() {
      return this.critPhysicsAllowed(true);
   }

   private boolean critPhysicsAllowed(boolean requireAir) {
      Module flight = al.init_Module_Str("flight");
      boolean flyRunning = flight != null && flight.isEnabled();
      return !flyRunning
         && !MC.player.isInLiquid()
         && !MC.player.isPassenger()
         && !this.inCobweb()
         && !MC.player.hasEffect(MobEffects.LEVITATION)
         && !MC.player.hasEffect(MobEffects.BLINDNESS)
         && !MC.player.hasEffect(MobEffects.SLOW_FALLING)
         && !MC.player.onClimbable()
         && !MC.player.isNoGravity()
         && !MC.player.isHandsBusy()
         && !MC.player.getAbilities().flying
         && (!requireAir || !MC.player.onGround());
   }

   private boolean inCobweb() {
      return MC.level.getBlockStates(MC.player.getBoundingBox()).anyMatch(state -> state.getBlock() instanceof WebBlock);
   }

   private double attackReach() {
      return autismclient.util.AttackReachHelper.maxReach(MC.player);
   }

   private double scanRange() {
      return Math.max(this.attackReach(), 0.0) + this.scanRangeMargin;
   }

   private boolean inAttackRange(ItemStack stack, Vec3 pos) {
      if (stack.has(DataComponents.PIERCING_WEAPON)) {
         return true;
      }

      AttackRange attackRange = stack.get(DataComponents.ATTACK_RANGE);
      if (attackRange == null) {
         attackRange = AttackRange.defaultFor(MC.player);
      }

      return attackRange.isInRange(MC.player, pos);
   }

   private double rollScanRangeMargin() {
      return 2.0 + this.random.nextDouble() * 1.0;
   }

   private boolean isModernCombat() {
      return !autismclient.util.LegacyCombatDetector.isLegacyCombat(MC.player);
   }

   private float attackCooldownTicks() {
      double attackSpeed = MC.player.getAttributeValue(Attributes.ATTACK_SPEED);
      if (this.autoSwordActive() && this.isModernCombat() && this.borrowedSlot < 0) {
         Integer slot = this.findWeaponSlot(null, false);
         if (slot != null) {
            attackSpeed = this.attributeValue(
               MC.player.getInventory().getItem(slot), Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED)
            );
         }
      }

      return (float)(1.0 / attackSpeed * 20.0);
   }

   private int attackStrengthTicker() {
      return ((AutismLivingEntityAccessor)MC.player).autism$getAttackStrengthTicker();
   }

   private boolean attackChargedIn(int ticks) {
      if (!this.isModernCombat()) {
         return true;
      }

      float delay = this.attackCooldownTicks();
      float offset = this.swingOffsetFor(delay, this.currentTarget);
      return (this.attackStrengthTicker() + ticks + 0.5F) / delay >= this.unusedSwingFactor + offset / delay - 1.0E-4F;
   }

   private float swingOffsetFor(float delay, Entity target) {
      return autismclient.util.RecentCombatTracker.init(target) ? 0.0F : Math.max(this.swingOffset, -0.08F * delay);
   }

   private void rerollSwingOffset() {
      this.unusedSwingFactor = 1.0F;
      this.swingOffset = this.rollSwingOffset();
   }

   private float rollSwingOffset() {
      ValueRange offset = ValueRange.parse(this.value("swing-offset"), SWING_OFFSET_DEFAULT);
      return (float)(this.swingTiming.sampleOffset(offset.min(), offset.max(), 0.0) / 50.0);
   }

   private boolean attackWouldBeBlocked(LivingEntity target) {
      DamageSource source = target.level().damageSources().playerAttack(MC.player);
      return this.damageAfterBlocking(target, source, 1.0F) > 0.0F;
   }

   private float damageAfterBlocking(LivingEntity target, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         ItemStack blockingStack = target.getItemBlockingWith();
         if (blockingStack == null) {
            return 0.0F;
         } else {
            BlocksAttacks blocksAttacks = blockingStack.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks == null) {
               return 0.0F;
            } else if (blocksAttacks.bypassedBy().map(tag -> tag.contains(source.typeHolder())).orElse(false)) {
               return 0.0F;
            } else if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
               return 0.0F;
            } else {
               double horizontalAngle = 3.141592653589793;
               Vec3 sourcePosition = source.getSourcePosition();
               if (sourcePosition != null) {
                  Vec3 view = LivingEntity.calculateViewVector(0.0F, target.getYHeadRot());
                  Vec3 to = sourcePosition.subtract(target.position());
                  Vec3 sourceDirection = new Vec3(to.x, 0.0, to.z).normalize();
                  horizontalAngle = Math.acos(sourceDirection.dot(view));
               }

               return blocksAttacks.resolveBlockedDamage(source, amount, horizontalAngle);
            }
         }
      }
   }

   static void fillClickCycle(int[] cycle, Random random, int cpsMin, int cpsMax) {
      if (cycle.length != 0) {
         int clicks = Math.min(cycle.length, cpsMin + random.nextInt(cpsMax - cpsMin + 1));
         int[] gaps = new int[clicks];
         int baseGap = cycle.length / clicks;
         int remainder = cycle.length % clicks;
         Arrays.fill(gaps, baseGap);

         for (int i = 0; i < remainder; i++) {
            gaps[i]++;
         }

         shuffle(gaps, random);
         boolean allEqual = true;

         for (int i = 1; i < gaps.length; i++) {
            if (gaps[i] != gaps[0]) {
               allEqual = false;
               break;
            }
         }

         if (allEqual && gaps.length > 1 && gaps[0] > 1) {
            int donor = random.nextInt(gaps.length);
            int receiver = (donor + 1 + random.nextInt(gaps.length - 1)) % gaps.length;
            gaps[donor]--;
            gaps[receiver]++;
         }

         int transfers = random.nextInt(gaps.length + 1) + random.nextInt(gaps.length + 1);

         for (int i = 0; i < transfers; i++) {
            int donor = random.nextInt(gaps.length);
            int receiver = random.nextInt(gaps.length);
            if (donor != receiver && gaps[donor] > 1) {
               gaps[donor]--;
               gaps[receiver]++;
            }
         }

         shuffle(gaps, random);
         int position = random.nextInt(cycle.length);

         for (int gap : gaps) {
            cycle[position]++;
            position = (position + gap) % cycle.length;
         }
      }
   }

   private static void shuffle(int[] values, Random random) {
      for (int i = values.length - 1; i > 0; i--) {
         int other = random.nextInt(i + 1);
         int value = values[i];
         values[i] = values[other];
         values[other] = value;
      }
   }

   private Integer findWeaponSlot(LivingEntity target, boolean enforceShield) {
      boolean requiresShield = enforceShield || target != null && this.attackWouldBeBlocked(target);
      boolean requiresMace = this.canSmashAttack() && (this.hotbarHasMace() || !requiresShield);
      Integer bestSlot = null;
      ItemStack bestStack = null;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               boolean eligible = requiresMace ? stack.getItem() instanceof MaceItem : (requiresShield ? stack.is(ItemTags.AXES) : stack.is(ItemTags.SWORDS));
               if (eligible
                  && (bestStack == null || (requiresMace ? this.compareMaces(stack, bestStack) > 0 : this.compareWeapons(stack, bestStack) > 0))) {
                  bestSlot = slot;
                  bestStack = stack;
               }
            }
         }
      }

      return bestSlot;
   }

   private boolean hotbarHasMace() {
      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id()) && MC.player.getInventory().getItem(slot).getItem() instanceof MaceItem) {
            return true;
         }
      }

      return false;
   }

   private boolean canSmashAttack() {
      return MaceItem.canSmashAttack(MC.player);
   }

   private boolean usingConsumableMainHand() {
      return MC.player.isUsingItem() && MC.player.getUsedItemHand() == InteractionHand.MAIN_HAND && MC.player.getUseItem().has(DataComponents.CONSUMABLE);
   }

   private void tickSwitchBack() {
      if (this.switchBackEnabled() && this.switchBackSlot >= 0 && this.switchBackDelayTicks > 0) {
         if (this.borrowedSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.borrowedSlot) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!CrystalAuraModule.hasBorrowedSlot(selected) && !AnchorAuraModule.hasSwitchedToSlot(selected)) {
               this.switchBackSlot = -1;
               this.borrowedSlot = -1;
               this.switchBackDelayTicks = 0;
            }
         } else {
            this.switchBackDelayTicks--;
            if (this.switchBackDelayTicks == 0
               && (!this.canInteract() || this.rotationMissesTarget(this.currentTarget))
               && !BlinkPacketManager.isHoldingActionsOnly()
               && (AutismLiteVariant.enabled() || !BedNukerModule.init_Z_23())
               && !CrystalAuraModule.isRecentlyActive()
               && !CrystalAuraModule.hasPlannedSupport()
               && !AnchorAuraModule.isRecentlyActive()
               && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.switchBackSlot, this.id())) {
               int tick = AutismSessionState.getInstance().getClientTick();
               if (tick != this.slotSwitchTick && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
                  try {
                     int back = this.switchBackSlot;
                     this.switchBackSlot = -1;
                     this.borrowedSlot = -1;
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, back);
                     this.slotSwitchTick = tick;
                  } finally {
                     autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
                  }
               }
            }
         }
      }
   }

   private boolean rotationMissesTarget(LivingEntity target) {
      if (target == null) {
         return true;
      }

      RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
      if (rotation == null) {
         rotation = RotationMath.ofPlayer(MC.player);
      }

      EntityHitResult hit = this.raycastTarget(target, rotation, this.attackReach(), 0.0);
      return hit == null || !this.inAttackRange(MC.player.getMainHandItem(), hit.getLocation());
   }

   private int compareWeapons(ItemStack first, ItemStack second) {
      int result = Double.compare(this.weaponScore(first), this.weaponScore(second));
      if (result != 0) {
         return result;
      }

      result = Double.compare(this.weaponTiebreakScore(first), this.weaponTiebreakScore(second));
      if (result != 0) {
         return result;
      }

      result = Boolean.compare(first.is(ItemTags.SWORDS), second.is(ItemTags.SWORDS));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(remainingDurability(first), remainingDurability(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(enchantability(first), enchantability(second));
      return result != 0 ? result : Integer.compare(first.hashCode(), second.hashCode());
   }

   private int compareMaces(ItemStack first, ItemStack second) {
      int result = Double.compare(this.maceScore(first), this.maceScore(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(remainingDurability(first), remainingDurability(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(enchantability(first), enchantability(second));
      return result != 0 ? result : Integer.compare(first.hashCode(), second.hashCode());
   }

   private double weaponScore(ItemStack stack) {
      double damage = this.attributeValue(stack, Attributes.ATTACK_DAMAGE, MC.player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
      int sharpness = this.enchantmentLevel(stack, Enchantments.SHARPNESS);
      if (sharpness > 0) {
         damage += 0.5 * sharpness + 0.5;
      }

      double speed = this.attributeValue(stack, Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
      double probability = Math.pow(0.85, 0.05);
      double adjusted = Math.pow(probability, Math.ceil(20.0 / speed * 0.9));
      double fire = Math.max(0.0, this.enchantmentLevel(stack, Enchantments.FIRE_ASPECT) * 4.0 - 1.0) * 0.33;
      double factor = this.enchantmentLevel(stack, Enchantments.SMITE) * 0.2
         + this.enchantmentLevel(stack, Enchantments.BANE_OF_ARTHROPODS) * 0.2
         + this.enchantmentLevel(stack, Enchantments.KNOCKBACK) * 0.2;
      return damage * speed * adjusted * (1.0 + factor) + fire;
   }

   private double maceScore(ItemStack stack) {
      double damage = this.attributeValue(stack, Attributes.ATTACK_DAMAGE, MC.player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
      int sharpness = this.enchantmentLevel(stack, Enchantments.SHARPNESS);
      if (sharpness > 0) {
         damage += 0.5 * sharpness + 0.5;
      }

      double speed = this.attributeValue(stack, Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
      double probability = Math.pow(0.85, 0.05);
      double adjusted = Math.pow(probability, Math.ceil(20.0 / speed * 0.9));
      double factor = this.enchantmentLevel(stack, Enchantments.DENSITY) * 0.5
         + this.enchantmentLevel(stack, Enchantments.BREACH) * 0.15
         + this.enchantmentLevel(stack, Enchantments.SMITE) * 0.2
         + this.enchantmentLevel(stack, Enchantments.BANE_OF_ARTHROPODS) * 0.2
         + this.enchantmentLevel(stack, Enchantments.WIND_BURST) * 0.2;
      return damage * speed * adjusted + factor + 29.0;
   }

   private double weaponTiebreakScore(ItemStack stack) {
      return this.enchantmentLevel(stack, Enchantments.LOOTING) * 0.05
         + this.enchantmentLevel(stack, Enchantments.UNBREAKING) * 0.05
         + this.enchantmentLevel(stack, Enchantments.MENDING) * 0.1
         - this.enchantmentLevel(stack, Enchantments.VANISHING_CURSE) * 0.1
         + this.enchantmentLevel(stack, Enchantments.SWEEPING_EDGE) * 0.2
         + this.enchantmentLevel(stack, Enchantments.KNOCKBACK) * 0.25;
   }

   private static int remainingDurability(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static int enchantability(ItemStack stack) {
      return stack.has(DataComponents.ENCHANTABLE) ? stack.get(DataComponents.ENCHANTABLE).value() : 0;
   }

   private double attributeValue(ItemStack stack, Holder<Attribute> attribute, double base) {
      ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      return modifiers == null
         ? attribute.value().sanitizeValue(base)
         : attribute.value().sanitizeValue(modifiers.compute(attribute, base, EquipmentSlot.MAINHAND));
   }

   private int enchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
      try {
         Holder<Enchantment> holder = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
         return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
      } catch (Throwable ignored) {
         return 0;
      }
   }

   private void resetCombatState() {
      this.bedBudgetThreat = null;
      this.bedBudgetThreatTick = this.bedNukerBusyTick = -2147483648;
      if (!AutismLiteVariant.enabled()) {
         autismclient.util.CombatInteractionController.reset();
      }

      this.stopBlockHit(this.playerWantsToUseItem());
      this.clearPendingUnblock();
      this.pendingUnblockReleaseCount = 0;
      this.plannedBlockHand = null;
      this.blockThreat = null;
      this.shieldHitWindow.load();
      this.fallTicksCacheTick = -2147483648;
      this.cachedTicksUntilFalling = 2147483647;
      this.currentTarget = null;
      this.attackDispatchPending = false;
      this.switchBackSlot = -1;
      this.borrowedSlot = -1;
      this.switchBackDelayTicks = 0;
      this.shieldSeenTick = -2147483648;
      this.shieldSeenTargetId = -1;
      this.shieldBrokenTick = -2147483648;
      this.shieldBrokenTargetId = -1;
      this.itemUseCooldownTicks = 0;
      this.critWaitTargetId = -1;
      this.critWaitStartTick = -2147483648;
      this.readyStallTicks = 0;
      this.hitLandingTargetId = -1;
      this.aimWander.reset();
      this.missState.clear();
      this.humanAimAnchor = null;
      this.lastSteppedTargetId = -1;
      this.groundYTargetId = -1;
      this.targetGroundY = 0.0 / 0.0;
      this.trackedTargetId = -1;
      this.motionTracker.reset();
      this.targetMovedSlightly = false;
      this.ticksSinceLastAttack = 1000;
      this.ticksSinceMissSwing = 1000;
      this.clickScheduler.reset();
      if (this.aimEngine != null) {
         this.aimEngine.reset();
      }

      autismclient.util.KillAuraHitMarkerRenderer.clear();
   }

   static final class FrameCatchUp {
      private static final long init = 1000000L;
      private static final long MAX_CATCH_UP_NANOS = 150000000L;
      private long lastFrameNanos = -9223372036854775808L;

      void markFrameNanos(long now) {
         this.lastFrameNanos = now;
      }

      void clearFrameNanos() {
         this.lastFrameNanos = -9223372036854775808L;
      }

      void catchUpClicks(
         autismclient.util.clicker.ClickPatternEngine engine,
         long now,
         boolean active,
         autismclient.util.clicker.ClickProfile profile,
         autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper sink
      ) {
         if (active && this.lastFrameNanos != -9223372036854775808L && now >= this.lastFrameNanos && now - this.lastFrameNanos <= 150000000L) {
            while (this.lastFrameNanos < now) {
               this.lastFrameNanos = Math.min(now, this.lastFrameNanos + 1000000L);
               engine.init(this.lastFrameNanos, true, profile, sink);
            }
         } else {
            this.lastFrameNanos = now;
            engine.init(now, active, profile, sink);
         }
      }
   }

   private final class ClickScheduler {
      private final KillAuraModule.ClickCycle clickCycle = new KillAuraModule.ClickCycle(20, 2);
      private final autismclient.util.clicker.ClickPatternEngine patternEngine = new autismclient.util.clicker.ClickPatternEngine();
      private final KillAuraModule.CarriedClicks carriedClicks = new KillAuraModule.CarriedClicks();
      private final KillAuraModule.FrameCatchUp frameCatchUp = new KillAuraModule.FrameCatchUp();
      private boolean active;
      private int pendingClicks;
      private int clicksThisTick;
      private boolean pendingWereCarried;
      private int ticksSinceAttack;
      private boolean bypassNextClick;
      private final Map<String, Integer> reasonCounts = new TreeMap<>();
      private String lastReason = "";

      ClickScheduler() {
         this.patternEngine.setSpeedMultiplier(2.0);
         this.rebuildClickCycle();
      }

      void tick() {
         this.bypassNextClick = false;
         this.ticksSinceAttack++;
         this.dropPendingClicks();
         boolean stale = this.carriedClicks.isStale(System.nanoTime());
         this.pendingWereCarried = this.carriedClicks.holdsCarried;
         int taken = this.carriedClicks.takeFresh(System.nanoTime());
         this.pendingClicks = KillAuraModule.this.isModernCombat() ? 0 : taken;
         if (stale && !KillAuraModule.this.isModernCombat()) {
            this.countReason("lost:stale");
         }

         if (KillAuraModule.this.isModernCombat() && this.clickCycle.advance(1)) {
            int[] cycle = new int[20];
            KillAuraModule.fillClickCycle(cycle, KillAuraModule.this.random, this.minCps(), this.maxCps());
            this.clickCycle.fillCycle(cycle);
         }
      }

      void suspend() {
         this.bypassNextClick = false;
         this.dropPendingClicks();
         if (this.carriedClicks.count > 0 && !KillAuraModule.this.isModernCombat()) {
            this.countReason("lost:paused");
         }

         this.pendingClicks = 0;
         this.carriedClicks.clearCarried();
         this.active = false;
         if (KillAuraModule.this.isModernCombat() && this.clickCycle.advance(1)) {
            int[] cycle = new int[20];
            KillAuraModule.fillClickCycle(cycle, KillAuraModule.this.random, this.minCps(), this.maxCps());
            this.clickCycle.fillCycle(cycle);
         }
      }

      void feedFrame(long now) {
         if (KillAuraModule.this.combatClicker != null) {
            this.patternEngine.setPersonality(KillAuraModule.this.combatClicker.personality());
            autismclient.util.clicker.ClickProfile profile = KillAuraModule.this.combatClicker.profile();
            this.carriedClicks.capacity = (profile.cpsHigh > 20.000000001 ? 2 : 1) + (KillAuraModule.this.isBlockHitCpsMode() ? 1 : 0);
            if (KillAuraModule.this.isBlockHitCpsMode()) {
               this.frameCatchUp.catchUpClicks(this.patternEngine, now, this.active, profile, this.carriedClicks);
            } else {
               this.frameCatchUp.clearFrameNanos();
               this.patternEngine.init(now, this.active && !KillAuraModule.this.isModernCombat(), profile, this.carriedClicks);
            }
         }
      }

      void setActive(boolean active) {
         if (active && !this.active) {
            this.frameCatchUp.markFrameNanos(System.nanoTime());
         }

         this.active = active;
      }

      void consumeClick(String how) {
         if (this.pendingClicks > 0) {
            this.countReason(how);
            this.pendingClicks--;
            this.clicksThisTick++;
            if (KillAuraModule.this.isBlockHitCpsMode() && this.pendingClicks > 0 && this.clicksThisTick >= this.maxClicksPerTick()) {
               this.carryPending();
            }
         }
      }

      private int maxClicksPerTick() {
         return SprintStateTracker.isServerSprinting() ? 1 : 2;
      }

      void noteLostReason(String why) {
         if (this.pendingClicks > 0) {
            this.lastReason = why;
         }
      }

      private void dropPendingClicks() {
         for (int i = 0; i < this.pendingClicks; i++) {
            this.countReason("lost:" + (this.lastReason.isEmpty() ? "unknown" : this.lastReason));
         }

         this.pendingClicks = 0;
         this.clicksThisTick = 0;
         this.lastReason = "";
      }

      private void countReason(String key) {
         this.reasonCounts.merge(key, 1, Integer::sum);
      }

      String debugSummary(boolean reset) {
         String out = this.reasonCounts.toString()
            + " engine accepted="
            + this.patternEngine.read_J()
            + " refused="
            + this.patternEngine.update()
            + " obsolete="
            + this.patternEngine.sync();
         if (reset) {
            this.reasonCounts.clear();
         }

         return out;
      }

      void reset() {
         this.patternEngine.init_V_J_DataUtil_NetHelper(System.nanoTime(), this.carriedClicks);
         this.frameCatchUp.clearFrameNanos();
         this.carriedClicks.clearCarried();
         this.pendingClicks = 0;
         this.pendingWereCarried = false;
         this.bypassNextClick = false;
         this.active = false;
      }

      void carryPending() {
         if (this.pendingClicks > 0 && !KillAuraModule.this.isModernCombat()) {
            this.carriedClicks.carryClicks(System.nanoTime(), this.pendingClicks);
            this.countReason("carried");
         }

         this.pendingClicks = 0;
      }

      void carryOrNoteLoss(String why) {
         if (this.pendingClicks > 0) {
            if (this.pendingWereCarried) {
               this.noteLostReason(why);
            } else {
               this.carryPending();
            }
         }
      }

      boolean clickDueWithin(int ticks) {
         return KillAuraModule.this.isModernCombat() ? this.hasClickAt(ticks) : this.ticksUntilNextClick() <= ticks;
      }

      private void rebuildClickCycle() {
         this.clickCycle.clearCycle();
         int[] cycle = new int[20];

         for (int i = 0; i < this.clickCycle.iterations; i++) {
            Arrays.fill(cycle, 0);
            KillAuraModule.fillClickCycle(cycle, KillAuraModule.this.random, this.minCps(), this.maxCps());
            this.clickCycle.fillCycle(cycle);
            this.clickCycle.advance(20);
         }
      }

      int clicksAvailableAt(int tick) {
         if (!KillAuraModule.this.isModernCombat()) {
            return tick != 0 ? 0 : (KillAuraModule.this.isBlockHitCpsMode() ? Math.min(this.pendingClicks, Math.max(0, this.maxClicksPerTick() - this.clicksThisTick)) : this.pendingClicks);
         } else {
            return this.chargedOrIdle() ? 1 : this.clickCycle.clicksAt(tick);
         }
      }

      int ticksUntilNextClick() {
         if (!KillAuraModule.this.isModernCombat()) {
            if (this.pendingClicks > 0) {
               return 0;
            } else {
               long nanos = this.patternEngine.nextClickNanos(System.nanoTime());
               if (nanos == 9223372036854775807L) {
                  return this.active ? 1 : 20;
               } else {
                  return (int)Math.min(20L, (nanos + 49999999L) / 50000000L);
               }
            }
         } else {
            if (this.chargedOrIdle()) {
               return 0;
            }

            for (int tick = 0; tick < 20; tick++) {
               if (this.clickCycle.clicksAt(tick) > 0) {
                  return tick;
               }
            }

            return 20;
         }
      }

      private int minCps() {
         return (int)Math.round(KillAuraModule.DEFAULT_CPS_RANGE.min());
      }

      private int maxCps() {
         return (int)Math.round(KillAuraModule.DEFAULT_CPS_RANGE.max());
      }

      private boolean chargedOrIdle() {
         if (!KillAuraModule.this.isModernCombat()) {
            return false;
         } else {
            return KillAuraModule.this.attackChargedIn(0) ? true : System.currentTimeMillis() - KillAuraModule.lastAttackMillis >= 1000L;
         }
      }

      boolean hasClickAt(int tick) {
         return this.clicksAvailableAt(tick) > 0;
      }

      boolean clickDueNow() {
         return this.hasClickAt(0);
      }

      private boolean bypassActive() {
         return this.bypassNextClick && KillAuraModule.this.isModernCombat();
      }

      boolean canClickNow() {
         if (!this.bypassActive() && this.clicksAvailableAt(0) <= 0) {
            return false;
         } else if (KillAuraModule.hasMissCooldown()) {
            this.noteLostReason("missTime");
            return false;
         } else {
            return this.bypassActive() || KillAuraModule.this.attackChargedIn(0);
         }
      }

      void runClicks(BooleanSupplier attack) {
         boolean bypass = this.bypassActive();
         int amount = KillAuraModule.this.isModernCombat() ? (!bypass && this.clicksAvailableAt(0) <= 0 ? 0 : 1) : this.clicksAvailableAt(0);

         for (int i = 0; i < amount; i++) {
            if (KillAuraModule.hasMissCooldown()) {
               this.noteLostReason("missTime");
            } else if ((bypass || KillAuraModule.this.attackChargedIn(0)) && attack.getAsBoolean()) {
               this.consumeClick("attack");
            } else {
               this.noteLostReason("attackRefused");
            }
         }
      }
   }

   static final class MissState {
      static final int MISS_EXPIRE_TICKS = 2;
      private static final long MISS_EXPIRE_NANOS = 150000000L;
      private boolean pending;
      private boolean legacy;
      private boolean missSpent;
      private int missEntityId = -1;
      private Vec3 missPoint;
      private Vec3 lastTargetCenter;
      private int missTick;
      private long missNanos;

      boolean isPending() {
         return this.pending;
      }

      boolean canPlanMiss() {
         return !this.pending && !this.missSpent;
      }

      void planMiss(int entityId, Vec3 missPoint, boolean legacy, int tick) {
         this.planMissAt(entityId, missPoint, legacy, tick, System.nanoTime());
      }

      void planMissAt(int entityId, Vec3 missPoint, boolean legacy, int tick, long now) {
         this.legacy = legacy;
         this.pending = true;
         this.missEntityId = entityId;
         this.missPoint = missPoint;
         this.lastTargetCenter = null;
         this.missTick = tick;
         this.missNanos = now;
      }

      boolean missDue(int tick) {
         return this.missDueAt(tick, System.nanoTime());
      }

      boolean missDueAt(int tick, long now) {
         return this.pending && (tick - this.missTick >= 2 || now - this.missNanos >= 150000000L);
      }

      void trackTargetCenter(Vec3 center) {
         if (this.pending) {
            if (this.lastTargetCenter != null) {
               this.missPoint = this.missPoint.add(center.subtract(this.lastTargetCenter));
            }

            this.lastTargetCenter = center;
         }
      }

      boolean isMissTarget(int entityId) {
         return this.missEntityId == entityId;
      }

      Vec3 getMissPoint() {
         return this.missPoint;
      }

      void clearOnAttack() {
         this.clear();
      }

      boolean isLegacy() {
         return this.legacy;
      }

      void markSpent() {
         this.clear();
         this.missSpent = true;
      }

      void clear() {
         this.pending = false;
         this.missSpent = false;
         this.missEntityId = -1;
         this.missPoint = null;
         this.lastTargetCenter = null;
         this.missTick = 0;
         this.missNanos = 0L;
      }
   }

   static final class PostAttackTracker {
      static final double SETTLED_YAW_SPEED = 5.0;
      static final int WINDOW_SAMPLES = 21;
      static final int IDLE_RESET_SAMPLES = 30;
      static final double AVERAGE_LIMIT = 8.5;
      static final double TURN_SPEED_FILL = 6.699999809265137;
      static final double AIM_ERROR_FILL = 6.5;
      private final LongSupplier clock;
      private long lastAttackTime = -9223372036854775808L;
      private int sampleCount;
      private double rotationSpeedSum;
      private double yawErrorSum;
      private float lastYaw;
      private float lastPitch;
      private boolean hasLastRotation;
      private int idleSamples;

      PostAttackTracker() {
         this(System::currentTimeMillis);
      }

      PostAttackTracker(LongSupplier clock) {
         this.clock = clock;
      }

      void markAttack() {
         this.lastAttackTime = this.clock.getAsLong();
      }

      long elapsedSinceAttack() {
         return this.lastAttackTime == -9223372036854775808L ? 9223372036854775807L : this.clock.getAsLong() - this.lastAttackTime;
      }

      boolean inPostAttackWindow() {
         return this.lastAttackTime != -9223372036854775808L && this.clock.getAsLong() - this.lastAttackTime <= 60L;
      }

      void sampleRotation(float yaw, float pitch, float perfectYaw) {
         if (!this.hasLastRotation) {
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            this.hasLastRotation = true;
         } else {
            double yawSpeed = Math.abs(Mth.wrapDegrees(yaw - this.lastYaw));
            double pitchSpeed = Math.abs(Mth.wrapDegrees(pitch - this.lastPitch));
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            if (!(yawSpeed <= 5.0) && !Float.isNaN(perfectYaw) && this.inPostAttackWindow()) {
               this.idleSamples = 0;
               if (this.sampleCount >= 21) {
                  this.sampleCount = 0;
                  this.rotationSpeedSum = 0.0;
                  this.yawErrorSum = 0.0;
               }

               this.sampleCount++;
               this.rotationSpeedSum += yawSpeed + pitchSpeed;
               this.yawErrorSum = this.yawErrorSum + Math.abs(Mth.wrapDegrees(yaw - perfectYaw));
            } else {
               this.idleSamples++;
            }
         }
      }

      void clearLastRotation() {
         this.hasLastRotation = false;
      }

      boolean turnSpeedTooHigh() {
         return !this.resetIfIdle() && this.paddedAverage(this.rotationSpeedSum, 6.699999809265137) > 8.5;
      }

      boolean aimErrorTooHigh() {
         return !this.resetIfIdle() && this.paddedAverage(this.yawErrorSum, 6.5) > 8.5;
      }

      private boolean resetIfIdle() {
         if (this.sampleCount > 0 && this.idleSamples > 30) {
            this.sampleCount = 0;
            this.rotationSpeedSum = 0.0;
            this.yawErrorSum = 0.0;
            return true;
         } else {
            return false;
         }
      }

      private double paddedAverage(double sum, double remainingSample) {
         return this.sampleCount <= 0 ? 0.0 : (sum + (21 - this.sampleCount) * remainingSample) / 21.0;
      }

      void reset() {
         this.lastAttackTime = -9223372036854775808L;
         this.sampleCount = 0;
         this.rotationSpeedSum = 0.0;
         this.yawErrorSum = 0.0;
         this.hasLastRotation = false;
         this.idleSamples = 0;
      }
   }

   static final class TickCachedFlag {
      private int cachedTick = -2147483648;
      private boolean cachedValue;

      boolean init(int clientTick, BooleanSupplier live) {
         if (clientTick != this.cachedTick) {
            this.cachedTick = clientTick;
            this.cachedValue = live.getAsBoolean();
         }

         return this.cachedValue;
      }
   }

   static final class ClickCycle {
      private final int cycleSize;
      final int iterations;
      private final int[] cycle;
      private int cyclePosition;

      ClickCycle(int cycleLength, int iterations) {
         this.cycleSize = cycleLength;
         this.iterations = iterations;
         this.cycle = new int[cycleLength * iterations];
      }

      int clicksAt(int relativeIndex) {
         return this.cycle[(this.cyclePosition + relativeIndex) % this.cycle.length];
      }

      boolean advance(int amount) {
         this.cyclePosition = (this.cyclePosition + amount) % this.cycle.length;
         return this.cyclePosition % this.cycleSize == 0;
      }

      void clearCycle() {
         Arrays.fill(this.cycle, 0);
         this.cyclePosition = 0;
      }

      void fillCycle(int[] cycle) {
         if (cycle.length != this.cycleSize) {
            throw new IllegalArgumentException("Array size must match cycle length");
         }

         if (this.cyclePosition == 0) {
            System.arraycopy(cycle, 0, this.cycle, this.cycleSize, this.cycleSize);
         } else {
            if (this.cyclePosition != this.cycleSize) {
               throw new IllegalStateException("Head must be at 0 or cycle length");
            }

            System.arraycopy(cycle, 0, this.cycle, 0, this.cycleSize);
         }
      }

      int unusedWindowSum(int offset) {
         int total = 0;

         for (int index = offset; index < offset + this.cycleSize; index++) {
            total += this.cycle[index];
         }

         return total;
      }
   }

   static final class AimWander {
      private static final double read_D = 0.3;
      private static final double YAW_NOISE_BASE = 1.45;
      private static final double YAW_NOISE_SIGMA = 1.45 * Math.sqrt(0.6);
      private static final double MIN_WANDER_AMPLITUDE = 2.0;
      private static final double MAX_WANDER_AMPLITUDE = 3.4;
      private static final double close_D = 0.3;
      private static final int SIDE_SWITCH_MIN_TICKS = 20;
      private static final int SIDE_SWITCH_SPAN_TICKS = 41;
      private static final double SOFT_CAP_HALF_WIDTH_SCALE = 1.2;
      private static final double SOFT_CAP_MAX_DEG = 6.0;
      private static final double EDGE_INSET = 0.04;
      private static final double FIXATE_CHANCE = 0.05;
      private static final double FIXATE_CHANCE_NEAR_CENTER = 0.06;
      private static final double run_D = 1.0;
      private static final int read_I = 2;
      private static final int RETARGET_FIXATE_COOLDOWN = 10;
      private static final double VERTICAL_PULL = 0.18;
      private static final double set_D = 0.55;
      private static final double VERTICAL_NOISE_SCALE = 0.55 * Math.sqrt(0.36);
      private static final double MAX_VERTICAL_STEP = 0.45;
      private static final double MAX_VERTICAL_OFFSET = 2.5;
      private static final int EXCURSION_MIN_TICKS = 26;
      private static final int EXCURSION_SPAN_TICKS = 15;
      private static final int flush_I = 1;
      private static final int close_I = 2;
      private static final int MAX_EXCURSION_DEFERRALS = 8;
      private static final double EXCURSION_MIN_DEG = 12.5;
      private static final double EXCURSION_RANGE_DEG = 5.5;
      private static final double stop = 0.55;
      static final double FREE_STEP_CAP_DEG = 30.0;
      static final double ATTACK_WINDOW_STEP_CAP_DEG = 4.0;
      private final Random random;
      private int targetEntityId = -2147483648;
      private double lateralOffset = 0.0 / 0.0;
      private double lateralSide = 1.0;
      private double baseLateral = 2.7;
      private int sideSwitchTicks;
      private int fixateTicks;
      private int fixateCooldown;
      private int excursionCountdown;
      private int excursionLegs;
      private int excursionDeferrals;
      private double excursionDegrees;
      private double verticalOffset;
      private double smoothX;
      private double smoothY;
      private double smoothZ;

      AimWander(Random random) {
         this.random = random;
      }

      Vec3 peekPoint(int targetEntityId, AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         if (this.targetEntityId != targetEntityId) {
            this.retarget(targetEntityId, box);
         }

         return this.smoothedPoint(box, eyes, inAttackWindow, allowExcursion);
      }

      Vec3 nextPoint(int targetEntityId, AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         if (this.targetEntityId != targetEntityId) {
            return this.peekPoint(targetEntityId, box, eyes, inAttackWindow, allowExcursion);
         }

         this.advanceSchedule(inAttackWindow, allowExcursion);
         return this.smoothedPoint(box, eyes, inAttackWindow, allowExcursion);
      }

      void reset() {
         this.targetEntityId = -2147483648;
      }

      private void retarget(int targetEntityId, AABB box) {
         this.targetEntityId = targetEntityId;
         this.lateralSide = this.random.nextBoolean() ? 1.0 : -1.0;
         this.baseLateral = 2.0 + this.random.nextDouble() * 1.4;
         this.sideSwitchTicks = rollSideSwitchTicks(this.random);
         this.fixateTicks = 0;
         this.fixateCooldown = 10;
         this.excursionCountdown = rollExcursionCountdown(this.random);
         this.excursionLegs = 0;
         this.excursionDeferrals = 0;
         this.excursionDegrees = 0.0;
         this.lateralOffset = 0.0 / 0.0;
         this.verticalOffset = (this.random.nextDouble() - 0.5) * 2.0 * 0.55;
         Vec3 center = box.getCenter();
         this.smoothX = center.x;
         this.smoothY = center.y;
         this.smoothZ = center.z;
      }

      private void advanceSchedule(boolean inAttackWindow, boolean allowExcursion) {
         if (this.fixateCooldown > 0) {
            this.fixateCooldown--;
         }

         boolean excursion = allowExcursion && this.excursionLegs > 0;
         if (excursion && Math.abs(this.lateralOffset) >= this.excursionDegrees - 1.0E-9) {
            this.excursionLegs--;
         }

         if (--this.excursionCountdown <= 0) {
            if (!allowExcursion || inAttackWindow && this.excursionDeferrals < 8) {
               this.excursionCountdown = 1;
               this.excursionDeferrals++;
            } else {
               this.excursionLegs = 1 + this.random.nextInt(3);
               this.excursionDegrees = rollExcursionDegrees(this.random);
               this.excursionCountdown = rollExcursionCountdown(this.random);
               this.excursionDeferrals = 0;
               this.fixateTicks = 0;
            }
         }

         if (!excursion && --this.sideSwitchTicks <= 0) {
            this.lateralSide = -this.lateralSide;
            this.sideSwitchTicks = rollSideSwitchTicks(this.random);
         }

         this.baseLateral = this.baseLateral + (this.random.nextDouble() - 0.5) * 0.3;
         this.baseLateral = Math.max(2.0, Math.min(3.4, this.baseLateral));
         if (this.fixateTicks <= 0 && !excursion && this.fixateCooldown <= 0) {
            double p = 0.05 + (Math.abs(this.lateralOffset) < 1.0 ? 0.06 : 0.0);
            if (this.random.nextDouble() < p) {
               this.fixateTicks = 1 + this.random.nextInt(2);
            }
         }
      }

      private Vec3 smoothedPoint(AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         Vec3 live = box.getCenter();
         this.smoothX = this.smoothX + 0.55 * (live.x - this.smoothX);
         this.smoothY = this.smoothY + 0.55 * (live.y - this.smoothY);
         this.smoothZ = this.smoothZ + 0.55 * (live.z - this.smoothZ);
         double dx = this.smoothX - eyes.x;
         double dz = this.smoothZ - eyes.z;
         double horizontal = Math.max(Math.sqrt(dx * dx + dz * dz), 0.3);
         double halfWidth = Math.min(box.getXsize(), box.getZsize()) * 0.5;
         double hwa = halfAngleDegrees(halfWidth, horizontal);
         boolean fixated = this.fixateTicks > 0;
         if (fixated) {
            this.fixateTicks--;
         }

         double signed = fixated ? this.lateralOffset : this.nextLateral(hwa, inAttackWindow, allowExcursion);
         double vertical = fixated ? this.verticalOffset : this.nextVerticalOffset();
         double perpX = -dz / horizontal;
         double perpZ = dx / horizontal;
         double lateral = horizontal * Math.tan(Math.toRadians(signed));
         double dy = horizontal * Math.tan(Math.toRadians(vertical));
         return new Vec3(this.smoothX + perpX * lateral, this.smoothY + dy, this.smoothZ + perpZ * lateral);
      }

      private double nextLateral(double hwa, boolean inAttackWindow, boolean allowExcursion) {
         if (Double.isNaN(this.lateralOffset)) {
            this.lateralOffset = this.lateralSide * this.baseLateral;
            return this.lateralOffset;
         }

         double cap = inAttackWindow ? 4.0 : 30.0;
         if (allowExcursion && this.excursionLegs > 0) {
            double goal = this.lateralSide * this.excursionDegrees;
            double delta = goal - this.lateralOffset;
            this.lateralOffset = Math.abs(delta) > cap ? this.lateralOffset + Math.copySign(cap, delta) : goal;
            return this.lateralOffset;
         }

         double goal = this.lateralSide * this.baseLateral;
         double next = this.lateralOffset + 0.3 * (goal - this.lateralOffset) + YAW_NOISE_SIGMA * this.random.nextGaussian();
         double softCap = Math.min(6.0, 1.2 * hwa);
         if (Math.abs(next) > softCap && Math.abs(next) > Math.abs(this.lateralOffset)) {
            next = Math.copySign(softCap, next);
         }

         double step = next - this.lateralOffset;
         if (Math.abs(step) > cap) {
            next = this.lateralOffset + Math.copySign(cap, step);
         }

         this.lateralOffset = next;
         return this.lateralOffset;
      }

      private double nextVerticalOffset() {
         double delta = -0.18 * this.verticalOffset + VERTICAL_NOISE_SCALE * this.random.nextGaussian();
         if (Math.abs(delta) > 0.45) {
            delta = Math.copySign(0.45, delta);
         }

         this.verticalOffset += delta;
         if (Math.abs(this.verticalOffset) > 2.5) {
            this.verticalOffset = Math.copySign(2.5, this.verticalOffset);
         }

         return this.verticalOffset;
      }

      static double halfAngleDegrees(double halfWidth, double distance) {
         return Math.toDegrees(Math.atan(Math.max(halfWidth - 0.04, 0.01) / Math.max(distance, 0.3)));
      }

      static int rollSideSwitchTicks(Random random) {
         return 20 + random.nextInt(41);
      }

      static int rollExcursionCountdown(Random random) {
         return 26 + random.nextInt(15);
      }

      static double rollExcursionDegrees(Random random) {
         return 12.5 + random.nextDouble() * 5.5;
      }
   }

   static final class CarriedClicks implements autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper {
      private static final long STALE_NANOS = 150000000L;
      int capacity = 1;
      private int count;
      private boolean holdsCarried;
      private long carriedAt;

      @Override
      public boolean init_Z_J(long now) {
         if (this.count >= this.capacity) {
            return false;
         }

         if (this.count == 0) {
            this.carriedAt = now;
            this.holdsCarried = false;
         }

         this.count++;
         return true;
      }

      @Override
      public void init_V_J(long now) {
      }

      boolean isStale(long now) {
         return this.count > 0 && now - this.carriedAt > 150000000L;
      }

      int takeFresh(long now) {
         int fresh = this.count > 0 && now - this.carriedAt <= 150000000L ? this.count : 0;
         this.count = 0;
         return fresh;
      }

      void carryClicks(long now, int clicks) {
         this.count = Math.max(this.count, Math.min(Math.max(1, clicks), this.capacity));
         this.holdsCarried = true;
         this.carriedAt = now;
      }

      void clearCarried() {
         this.count = 0;
      }
   }
}
