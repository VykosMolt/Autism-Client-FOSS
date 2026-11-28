package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class AnchorAuraModule extends Module implements SilentMovementCoordinator.Participant {
   public static final String MODULE_ID = "anchor-aura";
   private static final float ANCHOR_BLAST_POWER = 5.0F;
   private static final int init_I = 4;
   private static final int load_I = 4;
   private static final int MAX_EVALUATED_CANDIDATES = 32;
   private static final int LEAD_HORIZON_BASE_TICKS = 6;
   private static final double init_D = 2.0;
   private static final int RAY_BUDGET = 48;
   private static final int CELL_BAN_TICKS = 40;
   private static final int flush_I = 3;
   private static final int close_I = 3;
   private static final int open_I = 10;
   private static final int poll_I = 3;
   private static final int push_I = 3;
   private static final int pop_I = 2;
   private static final int tick = 4;
   private static final int run = 12;
   private static int[] cachedOffsets = new int[0];
   private static int cachedOffsetsRadius = -1;
   private final Random random = new Random();
   private long profileNanosTotal;
   private long profileNanosMax;
   private int profiledTicks;
   private final Map<BlockPos, Integer> bannedCells = new HashMap<>();
   private LivingEntity currentTarget;
   private int mayActTick = -2147483648;
   private int engagedTick = -2147483648;
   private final autismclient.util.ActionCooldownScheduler stepCooldowns = new autismclient.util.ActionCooldownScheduler();
   private final autismclient.util.CombatAimSpeedController aimSpeed = new autismclient.util.CombatAimSpeedController();
   private final autismclient.util.SessionContextWatcher sessionWatcher = new autismclient.util.SessionContextWatcher();
   private final autismclient.util.StickyPlacementSelector stickySelector = new autismclient.util.StickyPlacementSelector();
   private final autismclient.util.TargetLeadDamageEstimator leadEstimator = new autismclient.util.TargetLeadDamageEstimator();
   private final autismclient.util.ActivationModeLatch activationLatch = new autismclient.util.ActivationModeLatch();
   private BlockPos aimedCell;
   private long switchSettledAtNanos;
   private long pausedUntilNanos;
   private final Set<BlockPos> ownPlacedAnchors = new LinkedHashSet<>();
   private String entityFilterSource;
   private Set<String> entityFilterIds = Set.of();
   private int switchBackSlot = -1;
   private int switchedToSlot = -1;
   private int switchBackDelayTicks;
   private int lastSlotSwitchTick = -2147483648;
   private BlockPos activeAnchor;
   private LivingEntity activeTarget;
   private final autismclient.util.BlockSequenceSyncTracker sequenceSync = new autismclient.util.BlockSequenceSyncTracker();
   private int anchorDeadlineTick;
   private int staleReleaseTick;
   private boolean activeAnchorStale;
   private boolean detonateSent;
   private int detonateSentTick = -2147483648;
   private boolean chargeSent;
   private int chargeSentTick = -2147483648;
   private int placeAttempts;
   private BlockPos candidateCursor;
   private BlockPos budgetRefusedCell;
   private int budgetRefusals;
   private BlockPos plannedAnchor;
   private int plannedSlot = -1;
   private LivingEntity plannedTarget;
   private int planHoldUntilTick;
   private int planHoldMisses;
   private BlockPos plannedShield;
   private boolean plannedShieldObsidian;
   private BlockPos previewAnchor;
   private BlockPos previewSupport;
   private BlockPos previewShield;
   private int previewUntilTick = -2147483648;
   private BlockPos activeShield;
   private boolean activeShieldObsidian;
   private BlockPos plannedSupport;
   private BlockPos activeSupport;
   private int supportAttempts;
   private int shieldAttempts;
   private int emptyCellTicks;
   private autismclient.util.ExplosionDamageCalculator.SimOptions simOptions;
   private final Map<BlockPos, autismclient.util.ExplosionDamageCalculator.SimOptions> simOptionsByCell;
   private String statusText;

   private static int latencyTicks() {
      return autismclient.util.LatencyTickWindow.windowTicks(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
   }

   public AnchorAuraModule() {
      super("anchor-aura", "AnchorAura", ae.init_ae, "Blows targets up with respawn anchors.");
      this.simOptions = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT;
      this.simOptionsByCell = new HashMap<>();
      this.statusText = "";
      this.add(new BoolSetting("place", "Place", true).description("Place new respawn anchors.").build());
      this.add(new BoolSetting("charge", "Charge", true).description("Charge anchors with glowstone.").build());
      this.add(new BoolSetting("detonate", "Detonate", true).description("Detonate charged anchors.").build());
      this.add(
         new ChoiceSetting("aim-mode", "Aim Mode", "Auto", "Auto", "Aimed")
            .description("Auto chooses blast positions; Aimed starts at your crosshair and finishes that anchor.")
            .build()
      );
      this.add(autismclient.util.CombatAimSpeedController.speedSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(
         new BoolSetting("repeat", "Repeat", true)
            .description("Start another anchor cycle while activation remains active.")
            .group("Engagement")
            .visibleWhen(() -> !"Always".equals(this.choice("activation")))
            .build()
      );
      this.add(
         new BoolSetting("protect-friends", "Protect Friends", true)
            .description("Check collateral blast damage to friends and teammates, including follow-through.")
            .group("Damage")
            .build()
      );
      this.add(
         new DoubleSetting("max-friend-damage", "Max Friend Damage", 4.0, 0.0, 20.0, 0.5)
            .description("Maximum collateral damage; lethal damage is always refused.")
            .group("Damage")
            .visibleWhen(() -> this.bool("protect-friends"))
            .build()
      );
      this.add(new DoubleSetting("target-range", "Target Range", 8.0, 1.0, 16.0, 0.5).description("Enemy search radius.").build());
      this.add(new ChoiceSetting("targeting", "Targeting", "Distance", "Distance", "HP", "FOV").description("How the enemy is picked.").build());
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").description("Entity types to blast.").build());
      this.add(new IntSetting("hurt-time", "Hurt Time", 10, 0, 10, 1).description("Maximum hurt time.").build());
      this.add(
         new ChoiceSetting("activation", "Activation", "Always", "Always", "Hold", "Press")
            .description("Always run, hold a key, or toggle with a key press.")
            .group("Engagement")
            .build()
      );
      this.add(
         new KeybindSetting("activation-key", "Activation Key", -1)
            .description("Key used by Hold or Press activation.")
            .group("Engagement")
            .visibleWhen(() -> !"Always".equals(this.choice("activation")))
            .build()
      );
      this.add(
         new BoolSetting("stop-on-kill", "Stop On Kill", false).description("Pause before acquiring another target after a kill.").group("Engagement").build()
      );
      this.add(
         new IntSetting("stop-delay", "Stop Delay", 500, 0, 8000, 50)
            .description("Pause after a confirmed kill.")
            .unit("ms")
            .group("Engagement")
            .visibleWhen(() -> this.bool("stop-on-kill"))
            .build()
      );
      this.add(new BoolSetting("own-only", "Own Anchors Only", false).description("Adopt only anchors this module placed.").group("Engagement").build());
      this.add(new IntSetting("switch-back-delay", "Switch Back Delay", 20, 1, 100, 1).description("Idle ticks before switching back.").group("Place").build());
      this.add(new BoolSetting("only-above", "Only Above", false).description("Place above target.").group("Place").build());
      this.add(new BoolSetting("force-engage", "Force Engage", false).description("Engage anyway when nothing passes the damage gates.").group("Place").build());
      this.add(
         new BoolSetting("support-blocks", "Support Blocks", true)
            .description("Build a real obsidian support before placing an airborne anchor.")
            .group("Place")
            .build()
      );
      this.add(
         new DoubleSetting("min-target-damage", "Min Target Damage", 5.0, 0.0, 20.0, 0.5)
            .description("Minimum blast damage to target.")
            .group("Damage")
            .build()
      );
      this.add(
         new DoubleSetting("max-self-damage", "Max Self Damage", 4.0, 0.0, 20.0, 0.5).description("Maximum blast damage to self.").group("Damage").build()
      );
      this.add(new BoolSetting("efficient", "Efficient", true).description("Require efficient trades.").group("Damage").build());
      this.add(new BoolSetting("terrain", "Terrain", true).description("Model terrain damage.").group("Damage").build());
      this.add(
         new BoolSetting("glowstone-shield", "Blast Protection", true)
            .description("Build verified cover before charging when it improves the damage trade.")
            .group("Damage")
            .build()
      );
      this.add(
         new ChoiceSetting("cover-material", "Cover Material", "Adaptive", "Adaptive", "Durable", "Glowstone")
            .description("Adaptive uses obsidian when the next anchor still has a clear placement face. Otherwise it prefers glowstone.")
            .group("Damage")
            .visibleWhen(() -> this.bool("glowstone-shield"))
            .build()
      );
      this.add(
         new IntSetting("step-delay", "Default Step Delay", 0, 0, 1000, 10)
            .description("Fallback floor for stages without a custom delay.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new IntSetting("place-delay", "Place Delay", 0, 0, 1000, 10)
            .description("Floor between anchor and support placements.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(new IntSetting("charge-delay", "Charge Delay", 0, 0, 1000, 10).description("Floor between charge clicks.").unit("ms").group("Timing").build());
      this.add(new IntSetting("detonate-delay", "Detonate Delay", 0, 0, 1000, 10).description("Floor between detonations.").unit("ms").group("Timing").build());
      this.add(
         new IntSetting("switch-delay", "Switch Delay", 0, 0, 1000, 10)
            .description("Extra settling time after a hotbar change.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new ChoiceSetting("detonate-slot", "Detonate Slot", "Auto", "Auto", "1", "2", "3", "4", "5", "6", "7", "8", "9")
            .description("Hotbar slot for the trigger click; Auto picks any non-fuel slot.")
            .group("Timing")
            .build()
      );
      this.add(new IntSetting("step-jitter", "Step Jitter", 0, 0, 200, 5).description("Random extra delay per click.").unit("ms").group("Timing").build());
      this.add(new BoolSetting("preview", "Placement Preview", false).description("Show pending anchor, support, and shield cells.").group("Preview").build());
      this.add(
         new ColorSetting("preview-color", "Preview Color", 1728031283)
            .description("Fill and outline for placement previews.")
            .group("Preview")
            .visibleWhen(() -> this.bool("preview"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState();
      autismclient.util.SilentRotationManager.releaseOwner("anchor-aura");
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      if ("anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   private void resetState() {
      this.aimSpeed.reset();
      this.sessionWatcher.init();
      this.stickySelector.init();
      autismclient.util.QueuedCombatAction.init("anchor-aura");
      this.aimedCell = null;
      this.profileNanosTotal = 0L;
      this.profileNanosMax = 0L;
      this.profiledTicks = 0;
      this.currentTarget = null;
      this.mayActTick = -2147483648;
      this.engagedTick = -2147483648;
      this.stepCooldowns.init();
      this.leadEstimator.init();
      this.activationLatch.init();
      this.switchSettledAtNanos = 0L;
      this.pausedUntilNanos = 0L;
      this.ownPlacedAnchors.clear();
      this.switchBackSlot = -1;
      this.switchedToSlot = -1;
      this.switchBackDelayTicks = 0;
      this.lastSlotSwitchTick = -2147483648;
      this.activeAnchor = null;
      this.activeTarget = null;
      this.sequenceSync.init();
      this.activeAnchorStale = false;
      this.detonateSent = false;
      this.detonateSentTick = -2147483648;
      this.chargeSent = false;
      this.chargeSentTick = -2147483648;
      this.candidateCursor = null;
      this.budgetRefusedCell = null;
      this.budgetRefusals = 0;
      this.activeShield = null;
      this.activeShieldObsidian = false;
      this.plannedSupport = null;
      this.activeSupport = null;
      this.supportAttempts = 0;
      this.shieldAttempts = 0;
      this.previewAnchor = null;
      this.previewSupport = null;
      this.previewShield = null;
      this.previewUntilTick = -2147483648;
      this.emptyCellTicks = 0;
      this.clearPlan();
      this.bannedCells.clear();
      this.simOptionsByCell.clear();
      this.statusText = "";
      autismclient.util.InventorySlotArbiter.releaseAllClaims("anchor-aura");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return "anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && "anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.tick("anchor-aura", MC.player);
      }
   }

   @Override
   public String info() {
      if (!this.statusText.isEmpty()) {
         return this.statusText;
      }

      LivingEntity current = this.currentTarget;
      return current == null ? "" : current.getName().getString();
   }

   public LivingEntity getTarget() {
      return this.currentTarget;
   }

   LivingEntity currentTarget() {
      int age = AutismSessionState.getInstance().getClientTick() - this.engagedTick;
      return this.isEnabled() && age >= 0 && age <= 2 && this.canAct() ? this.currentTarget : null;
   }

   public static double averageTickMillis() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.profiledTicks > 0 ? aura.profileNanosTotal / aura.profiledTicks / 1000.0 : 0.0;
   }

   public static double maxTickMillis() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura ? aura.profileNanosMax / 1000.0 : 0.0;
   }

   public static List<EspHighlightBox> highlightBoxes() {
      if (al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() && aura.bool("preview")) {
         int color = am.init_I_Module(aura, "preview-color", 1728031283);
         List<EspHighlightBox> boxes = new ArrayList<>(3);
         BlockPos anchor = aura.plannedAnchor != null ? aura.plannedAnchor : aura.activeAnchor;
         BlockPos support = aura.plannedSupport != null ? aura.plannedSupport : aura.activeSupport;
         BlockPos shield = aura.plannedShield != null ? aura.plannedShield : aura.activeShield;
         int tick = AutismSessionState.getInstance().getClientTick();
         if (anchor == null && support == null && shield == null && tick - aura.previewUntilTick < 0) {
            anchor = aura.previewAnchor;
            support = aura.previewSupport;
            shield = aura.previewShield;
         }

         if (anchor != null) {
            boxes.add(new EspHighlightBox(new AABB(anchor), color));
         }

         if (support != null) {
            boxes.add(new EspHighlightBox(new AABB(support), color));
         }

         if (shield != null) {
            boxes.add(new EspHighlightBox(new AABB(shield), color));
         }

         return boxes;
      } else {
         return List.of();
      }
   }

   public static boolean init_Z_2() {
      if (al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled()) {
         int age = AutismSessionState.getInstance().getClientTick() - aura.mayActTick;
         return age >= 0 && age <= 5;
      } else {
         return false;
      }
   }

   public static boolean read_Z() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() ? aura.engagedTick == AutismSessionState.getInstance().getClientTick() : false;
   }

   public static boolean init_Z_I(int slot) {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() ? aura.switchBackSlot >= 0 && aura.switchedToSlot == slot : false;
   }

   public static boolean init_Z_BlockPos(BlockPos cell) {
      if (cell == null) {
         return false;
      } else {
         return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled()
            ? cell.equals(aura.plannedAnchor)
               || cell.equals(aura.activeAnchor)
               || cell.equals(aura.plannedShield)
               || cell.equals(aura.activeShield)
               || cell.equals(aura.plannedSupport)
               || cell.equals(aura.activeSupport)
            : false;
      }
   }

   @Override
   public boolean usesMoveFix() {
      return this.bool("move-fix");
   }

   @Override
   public boolean rotatesVisibly() {
      return !this.bool("silent");
   }

   @Override
   public boolean isSilentMovementActive() {
      boolean enabled = this.isEnabled();
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || enabled && this.canAct());
   }

   private boolean activationActive() {
      if (System.nanoTime() < this.pausedUntilNanos) {
         return false;
      }

      int bind = this.integer("activation-key");
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      return this.activationLatch.init(this.choice("activation"), down);
   }

   private void pauseAfterKill(LivingEntity previous) {
      if (previous != null
         && (!previous.isAlive() || previous.isRemoved() || !(autismclient.util.ExplosionDamageCalculator.effectiveHealth(previous) > 0.0))
         && this.bool("stop-on-kill")) {
         this.pausedUntilNanos = System.nanoTime() + this.integer("stop-delay") * 1000000L;
         this.activationLatch.init("Press");
      }
   }

   private boolean canAct() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && !MC.player.isSecondaryUseActive()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && !MacroExecutor.isRunning()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !BlinkPacketManager.isHoldingActionsOnly()
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !ScaffoldModule.update_Z_5()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.init_Z_20();
   }

   private static boolean higherPriorityActive() {
      return BedDefenderModule.init_Z_10() || SurroundModule.init_Z_27() || CrystalAuraModule.flush() && CrystalAuraModule.init_Z_51();
   }

   private boolean isFinishingStep(AnchorAuraModule.AnchorPlan plan) {
      return plan.step() == AnchorAuraModule.AnchorStep.DETONATE
         ? true
         : plan.step() == AnchorAuraModule.AnchorStep.CHARGE && this.activeAnchorStale && plan.cell().equals(this.activeAnchor);
   }

   @Override
   public void preMovementTick() {
      long profileStarted = System.nanoTime();

      try {
         if (AutismLiteVariant.enabled()) {
            return;
         }

         if (MC == null || MC.player == null || MC.level == null) {
            return;
         }

         this.sessionWatcher.init(MC.level, MC.player, MC.getConnection(), () -> {
            this.resetState();
            if ("anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               autismclient.util.SilentRotationManager.resetAll();
            }
         });
         int tick = AutismSessionState.getInstance().getClientTick();
         this.pruneBannedCells(tick);
         this.tickSwitchBack();
         this.tickAnchorDeadline(tick);
         if (!this.canAct()) {
            this.currentTarget = null;
            this.clearPlan();
            this.releaseControl();
            return;
         }

         this.pauseAfterKill(this.currentTarget);
         if (!this.activationActive()) {
            this.currentTarget = null;
            this.clearPlan();
            this.releaseControl();
            return;
         }

         LivingEntity enemy = this.findTarget();
         if (enemy != null) {
            this.leadEstimator.init(enemy, tick);
         }

         if (enemy == null || enemy != this.currentTarget) {
            this.emptyCellTicks = 0;
         }

         this.currentTarget = enemy;
         if (enemy == null) {
            this.clearPlan();
         }

         double range = MC.player.blockInteractionRange();
         this.aimedCell = null;
         if ("Aimed".equals(this.choice("aim-mode")) && autismclient.util.WeaponDamageCalculator.init() instanceof BlockHitResult hit) {
            BlockState pointed = MC.level.getBlockState(hit.getBlockPos());
            this.aimedCell = !(pointed.getBlock() instanceof RespawnAnchorBlock) && !pointed.canBeReplaced()
               ? hit.getBlockPos().relative(hit.getDirection())
               : hit.getBlockPos();
         }

         autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(48);
         this.refreshSimOptions();
         boolean higherRung = higherPriorityActive();
         boolean caPrime = CrystalAuraModule.flush();
         boolean caCommitted = caPrime && CrystalAuraModule.sync_Z_11();
         if (caPrime) {
            this.clearPlan();
         }

         AnchorAuraModule.AnchorPlan plan;
         try (autismclient.util.ExplosionDamageCalculator.SimScope pass = autismclient.util.ExplosionDamageCalculator.beginSimScope()) {
            plan = this.continueActiveAnchor(tick, range, budget);
            if (plan == null && this.activeAnchor == null && enemy != null && !caPrime && this.activationActive()) {
               plan = this.planNextStep(enemy, tick, range, budget, higherRung || caCommitted);
            }
         }

         if (plan == null) {
            if (!higherRung
               && !caPrime
               && !CrystalAuraModule.close()
               && (this.activeAnchor != null || this.bool("repeat"))
               && this.aimSpeed.init("anchor-aura", 20, MC.player, this.aimSubject(), this.wireRotation())) {
               this.statusText = "waiting";
               return;
            }

            this.releaseControl();
            return;
         }

         this.statusText = plan.step().name().toLowerCase(Locale.ROOT);
         boolean finishing = this.isFinishingStep(plan);
         boolean mayAct = !higherRung && !caCommitted || finishing;
         if (mayAct) {
            this.mayActTick = tick;
         }

         boolean releasedToDefend = mayAct && !finishing && !this.stepReady(plan.step()) && CrystalAuraModule.close();
         if (mayAct && !releasedToDefend) {
            this.engagedTick = tick;
         }

         autismclient.util.InventorySlotArbiter.claimInventoryClick("anchor-aura");
         RotationMath.Rotation wire = this.wireRotation();
         boolean allowed = wire != null && this.stepReady(plan.step()) && mayAct;
         boolean slotReady = allowed && this.ensureSlotSelected(plan.slot(), finishing);
         boolean finishSwitchRefused = finishing && allowed && !slotReady && MC.player.getInventory().getSelectedSlot() != plan.slot();
         boolean useStep = plan.step() == AnchorAuraModule.AnchorStep.CHARGE
            || plan.step() == AnchorAuraModule.AnchorStep.DETONATE;
         BlockHitResult alignedUse = wire != null && useStep ? this.hitForRotation(plan, wire) : null;
         BlockHitResult hit = slotReady ? (alignedUse != null ? alignedUse : this.hitForRotation(plan, wire)) : null;
         if (mayAct && !releasedToDefend && !finishSwitchRefused) {
            boolean pinCommittedFire = hit != null && (higherRung || caCommitted);
            boolean pin = hit != null || alignedUse != null;
            this.aimSpeed
               .init(
                  "anchor-aura",
                  pinCommittedFire ? 30 : 20,
                  MC.player,
                  this.aimSubject(),
                  this.decimal("rotation-speed-multiplier"),
                  pin ? wire : plan.placement().aim().goalRotation(),
                  pin
               );
         }

         if (hit == null) {
            return;
         }

         RotationMath.Rotation outgoing = SilentMovementCoordinator.getRenderRotation(MC.player);
         if (outgoing == null || !rotationsMatch(outgoing, wire)) {
            return;
         }

         this.queueStep(plan, hit, tick);
      } finally {
         long elapsed = System.nanoTime() - profileStarted;
         this.profileNanosTotal += elapsed;
         this.profileNanosMax = Math.max(this.profileNanosMax, elapsed);
         this.profiledTicks++;
      }
   }

   private static boolean rotationsMatch(RotationMath.Rotation first, RotationMath.Rotation second) {
      return Math.abs(RotationMath.angleDelta(first.yaw(), second.yaw())) <= 0.05F && Math.abs(first.pitch() - second.pitch()) <= 0.05F;
   }

   private void releaseControl() {
      this.aimSpeed.reset();
      this.statusText = "";
      if (this.activeAnchor == null) {
         autismclient.util.InventorySlotArbiter.releaseInventoryClick("anchor-aura");
         autismclient.util.InventorySlotArbiter.releaseAllSlots("anchor-aura");
      }

      if ("anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.releaseOwner("anchor-aura");
         autismclient.util.SilentRotationManager.tick("anchor-aura", MC.player);
      }
   }

   private RotationMath.Rotation wireRotation() {
      ServerRotationTracker.RotationSnapshot snapshot = ServerRotationTracker.getSnapshot();
      return !snapshot.initialized() ? null : new RotationMath.Rotation(snapshot.currentYaw(), snapshot.currentPitch());
   }

   private RotationMath.Rotation wireOrPlayerRotation() {
      RotationMath.Rotation wire = this.wireRotation();
      return wire != null ? wire : RotationMath.ofPlayer(MC.player);
   }

   private boolean stepReady(AnchorAuraModule.AnchorStep step) {
      int tick = AutismSessionState.getInstance().getClientTick();
      return this.stepCooldowns.isReady(step.name(), tick, System.nanoTime());
   }

   private void markStep(AnchorAuraModule.AnchorStep step) {
      int tick = AutismSessionState.getInstance().getClientTick();
      this.stepCooldowns
         .schedule(
            step.name(),
            AutismSessionState.getInstance().getClientTick(),
            System.nanoTime(),
            autismclient.util.ActionCooldownScheduler.jitteredDelayNanos(this.stepDelay(step), this.integer("step-jitter"), this.random)
         );
      this.switchBackDelayTicks = this.integer("switch-back-delay");
   }

   private int stepDelay(AnchorAuraModule.AnchorStep step) {
      String var10000;
      switch (step) {
         case SUPPORT:
         case PLACE:
         case SHIELD:
            var10000 = "place-delay";
            break;
         case CHARGE:
            var10000 = "charge-delay";
            break;
         case DETONATE:
            var10000 = "detonate-delay";
            break;
         default:
            throw new MatchException(null, null);
      }

      String id = var10000;
      int configured = this.integer(id);
      return configured == 75 ? this.integer("step-delay") : configured;
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      if (this.stepCooldowns.init(AutismSessionState.getInstance().getClientTick())) {
         return true;
      }

      if (this.activeAnchor != null && this.canAct() && hitResult instanceof BlockHitResult hit) {
         ItemStack held = MC.player.getItemInHand(hand);
         if (!isRespawnAnchor(held) && !isGlowstone(held) && !isObsidian(held)) {
            return false;
         }

         BlockPos cell = hit.getBlockPos();
         return (cell.equals(this.activeAnchor) || cell.equals(this.activeSupport) || cell.equals(this.activeShield))
            && MC.level.getBlockState(cell) != autismclient.util.ServerVerifiedBlockState.init(MC.level, cell);
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && "anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private AnchorAuraModule.AnchorPlan continueActiveAnchor(int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      BlockPos cell = this.activeAnchor;
      if (cell == null) {
         return null;
      }

      if (this.sequenceSync.init(MC.level)) {
         return null;
      }

      BlockState state = MC.level.getBlockState(cell);
      if (state.getBlock() instanceof RespawnAnchorBlock && !(autismclient.util.ServerVerifiedBlockState.init(MC.level, cell).getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      boolean anchor = state.getBlock() instanceof RespawnAnchorBlock;
      if (this.activeSupport != null) {
         if (MC.level.getBlockState(this.activeSupport).canBeReplaced()) {
            if (!this.activeAnchorStale && this.supportAttempts < 3) {
               return this.planSupport(this.activeAnchor, this.activeSupport, range, budget);
            }

            this.releaseActiveAnchor(true);
            return null;
         }

         if (autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeSupport).canBeReplaced()) {
            return null;
         }

         this.activeSupport = null;
      }

      if (!anchor) {
         if (this.detonateSent) {
            this.releaseActiveAnchor(false);
            return null;
         } else if (this.bool("place") && !this.activeAnchorStale && this.placeAttempts < 3) {
            this.chargeSent = false;
            this.chargeSentTick = -2147483648;
            return this.planPlaceAnchor(cell, range, budget);
         } else {
            this.releaseActiveAnchor(true);
            return null;
         }
      } else {
         int charge = state.getValue(RespawnAnchorBlock.CHARGE);
         if (this.activeShield != null
            && !MC.level.getBlockState(this.activeShield).canBeReplaced()
            && autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield).canBeReplaced()) {
            return null;
         }

         if (this.activeShield != null && MC.level.getBlockState(this.activeShield).canBeReplaced()) {
            if (!this.activeAnchorStale && this.shieldAttempts < 3 && this.hasShieldMaterial(this.activeShieldObsidian)) {
               AnchorAuraModule.AnchorPlan shield = this.planShield(this.activeShield, range, budget);
               if (charge <= 0 || shield != null && !this.selfDamageSafe(cell)) {
                  return shield;
               }

               this.activeShield = null;
               this.activeShieldObsidian = false;
            } else {
               if (charge <= 0 || autismclient.util.ServerVerifiedBlockState.init(MC.level, cell).getValue(RespawnAnchorBlock.CHARGE) <= 0) {
                  this.releaseActiveAnchor(true);
                  return null;
               }

               this.activeShield = null;
               this.activeShieldObsidian = false;
            }
         }

         if (charge <= 0) {
            if (!this.shieldedDetonationSafe(cell)) {
               if (this.activeShield != null && MC.level.getBlockState(this.activeShield) == autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield)) {
                  this.releaseActiveAnchor(true);
               }

               return null;
            } else if (!this.bool("charge")) {
               return null;
            } else {
               return this.chargeSent && tick - this.chargeSentTick < latencyTicks()
                  ? null
                  : this.planUseAnchor(cell, AnchorAuraModule.AnchorStep.CHARGE, range, budget);
            }
         } else if (this.bool("detonate") && this.canDetonate(cell, state)) {
            return this.detonateSent && tick - this.detonateSentTick < latencyTicks()
               ? null
               : this.planUseAnchor(cell, AnchorAuraModule.AnchorStep.DETONATE, range, budget);
         } else {
            return null;
         }
      }
   }

   private AnchorAuraModule.AnchorPlan planForEnemy(
      LivingEntity enemy, int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      BlockPos adopted = this.findAdoptableAnchor(enemy, range);
      if (adopted != null) {
         BlockState state = MC.level.getBlockState(adopted);
         AnchorAuraModule.AnchorStep step = state.getValue(RespawnAnchorBlock.CHARGE) > 0
            ? AnchorAuraModule.AnchorStep.DETONATE
            : AnchorAuraModule.AnchorStep.CHARGE;
         boolean usable = step == AnchorAuraModule.AnchorStep.DETONATE
            ? this.bool("detonate") && this.canDetonate(adopted, state)
            : this.canCycleWithGlowstone();
         boolean refused = true;
         if (usable) {
            autismclient.util.BlockPlacementPlanner.RejectReason[] outcome = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
            AnchorAuraModule.AnchorPlan plan = this.planUseAnchorWithReasons(adopted, step, range, budget, outcome);
            if (plan != null) {
               this.adoptAnchor(adopted, tick, null, false);
               return plan;
            }

            refused = outcome[0] != autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET || this.budgetRefusedThrice(adopted);
         }

         if (refused) {
            this.bannedCells.put(adopted, tick + 40);
         }
      }

      if (!this.bool("place")) {
         return null;
      }

      if (!this.canCycleWithGlowstone()) {
         return null;
      }

      List<BlockPos> cells = this.candidateCells(enemy, range);
      if (cells.isEmpty()) {
         if (this.bool("glowstone-shield")) {
            AnchorAuraModule.AnchorPlan shielded = this.planShieldedAnchor(enemy, range, budget);
            if (shielded != null) {
               return shielded;
            }
         }

         if (this.emptyCellTicks < 3) {
            this.emptyCellTicks++;
         }

         if (this.bool("force-engage") && this.emptyCellTicks >= 3) {
            cells = this.forcedCandidateCells(enemy, range);
         }
      } else {
         this.emptyCellTicks = 0;
      }

      if (cells.isEmpty() && this.bool("support-blocks")) {
         AnchorAuraModule.AnchorPlan supported = this.planSupportedAnchor(enemy, range, budget);
         if (supported != null) {
            return supported;
         }
      }

      if (cells.isEmpty()) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      int start = this.cursorStartIndex(cells);

      for (int step = 0; step < cells.size(); step++) {
         int index = start + step;
         if (index >= cells.size()) {
            index -= cells.size();
         }

         BlockPos cell = cells.get(index);
         outcome[0] = null;
         BlockPos cover = step == 0 && this.bool("glowstone-shield")
            ? this.findShieldCell(enemy, cell, this.decimal("min-target-damage"), this.decimal("max-self-damage"))
            : null;
         AnchorAuraModule.AnchorPlan plan = this.planPlaceAnchorWithShield(cell, cover, cover != null && this.coverMaterialFor(cover, cell) == 1, range, budget, outcome);
         if (plan != null) {
            this.candidateCursor = cell;
            return plan;
         }

         if (outcome[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            this.candidateCursor = cell;
            return null;
         }

         this.candidateCursor = cells.get(index + 1 == cells.size() ? 0 : index + 1);
      }

      return this.bool("support-blocks") ? this.planSupportedAnchor(enemy, range, budget) : null;
   }

   private int cursorStartIndex(List<BlockPos> cells) {
      if (this.candidateCursor == null) {
         return 0;
      }

      int index = cells.indexOf(this.candidateCursor);
      return index < 0 ? 0 : index;
   }

   private boolean budgetRefusedThrice(BlockPos cell) {
      if (cell.equals(this.budgetRefusedCell)) {
         this.budgetRefusals++;
      } else {
         this.budgetRefusedCell = cell.immutable();
         this.budgetRefusals = 1;
      }

      if (this.budgetRefusals < 3) {
         return false;
      }

      this.budgetRefusedCell = null;
      this.budgetRefusals = 0;
      return true;
   }

   private AnchorAuraModule.AnchorPlan planNextStep(LivingEntity enemy, int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget, boolean hotbarOwnedAbove) {
      if (this.plannedAnchor != null) {
         boolean valid = this.bool("place")
            && enemy == this.plannedTarget
            && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.plannedSlot, "anchor-aura")
            && (
               this.plannedSupport != null
                  ? isObsidian(MC.player.getInventory().getItem(this.plannedSlot))
                     && this.canPlaceAnchorAt(this.plannedAnchor, this.motionEnvelope())
                     && this.canPlaceBlockAt(Blocks.OBSIDIAN.defaultBlockState(), this.plannedSupport, this.motionEnvelope())
                  : isRespawnAnchor(MC.player.getInventory().getItem(this.plannedSlot))
                     && this.canPlaceBlockAt(Blocks.RESPAWN_ANCHOR.defaultBlockState(), this.plannedAnchor, this.motionEnvelope())
            )
            && (
               this.plannedShield == null
                  || this.hasShieldMaterial(this.plannedShieldObsidian) && this.canPlaceBlockAt(shieldState(this.plannedShieldObsidian), this.plannedShield, this.motionEnvelope())
            );
         valid = valid && this.planStillWorthIt(enemy, this.plannedAnchor, this.plannedSupport, this.plannedShield);
         if (valid) {
            if (hotbarOwnedAbove) {
               this.planHoldUntilTick = tick + this.switchDelay();
            }

            if (tick - this.planHoldUntilTick < 0) {
               AnchorAuraModule.AnchorPlan held = this.plannedSupport != null
                  ? this.planSupport(this.plannedAnchor, this.plannedSupport, range, budget)
                  : this.planPlaceAnchorWithShield(this.plannedAnchor, this.plannedShield, this.plannedShieldObsidian, range, budget, null);
               if (held != null) {
                  this.planHoldMisses = 0;
                  return held;
               }

               if (++this.planHoldMisses < 3) {
                  return null;
               }
            }

            this.bannedCells.put(this.plannedAnchor, tick + 10);
         }

         this.clearPlan();
      }

      AnchorAuraModule.AnchorPlan plan = this.planForEnemy(enemy, tick, range, budget);
      if (plan != null
         && (plan.step() == AnchorAuraModule.AnchorStep.PLACE || plan.step() == AnchorAuraModule.AnchorStep.SUPPORT)) {
         this.plannedAnchor = plan.step() == AnchorAuraModule.AnchorStep.SUPPORT ? plan.read() : plan.cell();
         this.plannedSupport = plan.step() == AnchorAuraModule.AnchorStep.SUPPORT ? plan.cell() : null;
         this.plannedShield = plan.load();
         this.plannedShieldObsidian = plan.shieldObsidian();
         this.plannedSlot = plan.slot();
         this.plannedTarget = enemy;
         this.planHoldUntilTick = tick + this.switchDelay();
      }

      return plan;
   }

   private boolean planStillWorthIt(LivingEntity enemy, BlockPos cell, BlockPos support, BlockPos shield) {
      if (enemy != null && enemy.isAlive() && this.anchorExplodes(cell)) {
         autismclient.util.ExplosionDamageCalculator.SimOptions verify = this.simOptionsFor(cell);
         if (shield != null || support != null) {
            verify = verify.withPlacedObsidian(shield != null ? shield : support);
         }

         autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankBlast(
            enemy, center(cell), verify, this.leadBox(enemy)
         );
         return this.rankAcceptable(rank)
            ? true
            : shield == null
               && this.bool("force-engage")
               && this.emptyCellTicks >= 3
               && this.rankAcceptable(
                  autismclient.util.TargetLeadDamageEstimator.init(enemy, center(cell), 5.0F, verify, autismclient.util.TargetLeadDamageEstimator.init(enemy))
               )
               && (!this.bool("protect-friends") || autismclient.util.WeaponDamageCalculator.init(center(cell), 5.0F, verify, this.decimal("max-friend-damage")));
      } else {
         return false;
      }
   }

   private void clearPlan() {
      this.plannedAnchor = null;
      this.plannedSlot = -1;
      this.plannedTarget = null;
      this.planHoldUntilTick = 0;
      this.planHoldMisses = 0;
      this.plannedShield = null;
      this.plannedShieldObsidian = false;
      this.plannedSupport = null;
   }

   private boolean canCycleWithGlowstone() {
      if (!this.bool("charge") || !this.bool("detonate")) {
         return false;
      } else if (this.findHotbarSlot(AnchorAuraModule::isGlowstone) < 0) {
         return false;
      } else {
         return isGlowstone(MC.player.getOffhandItem()) ? false : this.detonateSlot() >= 0;
      }
   }

   private int switchDelay() {
      return autismclient.util.ActionCooldownScheduler.timeoutTicks(
         this.stepDelay(AnchorAuraModule.AnchorStep.PLACE), this.integer("switch-delay"), autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs()
      );
   }

   private int cycleBudgetTicks(boolean shielded) {
      return autismclient.util.ActionCooldownScheduler.init(
         this.stepDelay(AnchorAuraModule.AnchorStep.PLACE),
         this.stepDelay(AnchorAuraModule.AnchorStep.CHARGE),
         this.stepDelay(AnchorAuraModule.AnchorStep.DETONATE),
         this.integer("switch-delay"),
         autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs(),
         shielded
      );
   }

   private void adoptAnchor(BlockPos anchorCell, BlockPos supportCell, int tick) {
      this.adoptAnchor(anchorCell, tick, null, false);
      this.activeSupport = supportCell.immutable();
      this.supportAttempts = 0;
      this.anchorDeadlineTick = tick + this.cycleBudgetTicks(true);
   }

   private void adoptAnchor(BlockPos cell, int tick, BlockPos shield, boolean shieldObsidian) {
      this.clearPlan();
      this.activeAnchor = cell.immutable();
      this.activeTarget = this.currentTarget;
      this.sequenceSync.init();
      this.activeShield = shield;
      this.activeShieldObsidian = shieldObsidian;
      this.shieldAttempts = 0;
      this.anchorDeadlineTick = tick + this.cycleBudgetTicks(shield != null);
      this.activeAnchorStale = false;
      this.detonateSent = false;
      this.detonateSentTick = -2147483648;
      this.chargeSent = false;
      this.chargeSentTick = -2147483648;
      this.placeAttempts = 0;
      this.emptyCellTicks = 0;
      this.candidateCursor = null;
   }

   private void tickAnchorDeadline(int tick) {
      if (this.activeAnchor != null) {
         if (!this.activeAnchorStale) {
            if (tick - this.anchorDeadlineTick > 0) {
               this.activeAnchorStale = true;
               this.staleReleaseTick = tick + this.cycleBudgetTicks(this.activeShield != null);
            }
         } else if (tick - this.staleReleaseTick > 0) {
            this.releaseActiveAnchor(true);
         }
      }
   }

   private void releaseActiveAnchor(boolean ban) {
      if (!ban && !this.bool("repeat")) {
         this.activationLatch.init(this.choice("activation"));
      }

      BlockPos cell = this.activeAnchor;
      this.aimSpeed.init(cell, !ban && this.bool("repeat") && this.currentTarget == this.activeTarget ? this.currentTarget : null);
      if (cell != null && ban) {
         this.bannedCells.put(cell, AutismSessionState.getInstance().getClientTick() + 40);
      }

      this.activeAnchor = null;
      this.activeTarget = null;
      this.sequenceSync.init();
      this.activeShield = null;
      this.activeShieldObsidian = false;
      this.plannedSupport = null;
      this.activeSupport = null;
      this.supportAttempts = 0;
      this.shieldAttempts = 0;
      this.activeAnchorStale = false;
      this.detonateSent = false;
      this.detonateSentTick = -2147483648;
      this.chargeSent = false;
      this.chargeSentTick = -2147483648;
      autismclient.util.InventorySlotArbiter.releaseAllSlots("anchor-aura");
      autismclient.util.InventorySlotArbiter.releaseInventoryClick("anchor-aura");
   }

   private void pruneBannedCells(int tick) {
      if (!this.bannedCells.isEmpty()) {
         Iterator<Entry<BlockPos, Integer>> entries = this.bannedCells.entrySet().iterator();

         while (entries.hasNext()) {
            if (tick - entries.next().getValue() >= 0) {
               entries.remove();
            }
         }
      }
   }

   private AnchorAuraModule.AnchorPlan planPlaceAnchor(BlockPos cell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      return this.planPlaceAnchorWithShield(cell, null, false, range, budget, null);
   }

   private AnchorAuraModule.AnchorPlan planPlaceAnchorWithShield(
      BlockPos cell,
      BlockPos shield,
      boolean shieldObsidian,
      double range,
      autismclient.util.BlockPlacementPlanner.RayBudget budget,
      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      if (!this.canPlaceBlockAt(Blocks.RESPAWN_ANCHOR.defaultBlockState(), cell, this.motionEnvelope())) {
         return null;
      }

      int slot = this.findHotbarSlot(AnchorAuraModule::isRespawnAnchor);
      if (slot < 0) {
         return null;
      }

      ItemStack material = MC.player.getInventory().getItem(slot);
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         this.placementQuery(cell, material, range).withRayBudget(budget), outcome
      );
      return candidate == null
         ? null
         : new AnchorAuraModule.AnchorPlan(cell, AnchorAuraModule.AnchorStep.PLACE, slot, candidate, range, shield, shieldObsidian, null);
   }

   private AnchorAuraModule.AnchorPlan planSupport(
      BlockPos anchorCell, BlockPos supportCell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      if (this.canPlaceAnchorAt(anchorCell, this.motionEnvelope()) && this.canPlaceBlockAt(Blocks.OBSIDIAN.defaultBlockState(), supportCell, this.motionEnvelope())
         )
       {
         int slot = this.findHotbarSlot(AnchorAuraModule::isObsidian);
         if (slot < 0) {
            return null;
         }

         ItemStack material = MC.player.getInventory().getItem(slot);
         autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
            this.placementQuery(supportCell, material, range).withRayBudget(budget)
         );
         return candidate == null
            ? null
            : new AnchorAuraModule.AnchorPlan(supportCell, AnchorAuraModule.AnchorStep.SUPPORT, slot, candidate, range, null, false, anchorCell);
      } else {
         return null;
      }
   }

   private AnchorAuraModule.AnchorPlan planUseAnchor(
      BlockPos cell, AnchorAuraModule.AnchorStep step, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      return this.planUseAnchorWithReasons(cell, step, range, budget, null);
   }

   private AnchorAuraModule.AnchorPlan planUseAnchorWithReasons(
      BlockPos cell,
      AnchorAuraModule.AnchorStep step,
      double range,
      autismclient.util.BlockPlacementPlanner.RayBudget budget,
      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      int slot = step == AnchorAuraModule.AnchorStep.CHARGE ? this.findHotbarSlot(AnchorAuraModule::isGlowstone) : this.detonateSlot();
      if (slot < 0) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = this.planAnchorClick(cell, range, budget, outcome);
      return candidate == null ? null : new AnchorAuraModule.AnchorPlan(cell, step, slot, candidate, range, null, false, null);
   }

   private AnchorAuraModule.AnchorPlan planShield(BlockPos cell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      boolean obsidian = this.activeShieldObsidian;
      if (!this.canPlaceBlockAt(shieldState(obsidian), cell, this.motionEnvelope())) {
         return null;
      }

      Predicate<ItemStack> material = obsidian ? AnchorAuraModule::isObsidian : AnchorAuraModule::isGlowstone;
      int slot = this.findHotbarSlot(material);
      if (slot < 0) {
         return null;
      }

      ItemStack stack = MC.player.getInventory().getItem(slot);
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         this.placementQuery(cell, stack, range, this.anchorHitValidator(this.activeAnchor, stack))
            .withRayBudget(budget)
      );
      return candidate == null
         ? null
         : new AnchorAuraModule.AnchorPlan(cell, AnchorAuraModule.AnchorStep.SHIELD, slot, candidate, range, null, obsidian, null);
   }

   private autismclient.util.BlockPlacementPlanner.Query placementQuery(BlockPos cell, ItemStack stack, double range) {
      return this.placementQuery(cell, stack, range, autismclient.util.BlockPlacementPlanner.blockItemValidator(stack, MC.player, InteractionHand.MAIN_HAND));
   }

   private autismclient.util.BlockPlacementPlanner.Query placementQuery(
      BlockPos cell, ItemStack stack, double range, autismclient.util.BlockPlacementPlanner.HitValidator placement
   ) {
      LocalPlayer player = MC.player;
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(cell, player.getEyePosition(), range, placement)
         .withFromRotation(this.wireOrPlayerRotation())
         .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
         .withLeadEye(player.getEyePosition().add(player.getDeltaMovement()))
         .withSneaking(player.isSecondaryUseActive())
         .withAutoSneak(false);
      return request;
   }

   private autismclient.util.BlockPlacementPlanner.Query anchorUseQuery(BlockPos anchor, double range) {
      LocalPlayer player = MC.player;
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(
            anchor, player.getEyePosition(), range, new AnchorAuraModule.AnchorHitValidator(anchor)
         )
         .withFromRotation(this.wireOrPlayerRotation())
         .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
         .withLeadEye(player.getEyePosition().add(player.getDeltaMovement()))
         .withSneaking(player.isSecondaryUseActive())
         .withAutoSneak(false);
      return request;
   }

   private autismclient.util.BlockPlacementPlanner.HitValidator anchorHitValidator(final BlockPos anchor, final ItemStack held) {
      final LocalPlayer player = MC.player;
      return new autismclient.util.BlockPlacementPlanner.HitValidator() {
         @Override
         public boolean lands(BlockHitResult hit, BlockPos cell) {
            if (hit != null && cell != null && held.getItem() instanceof BlockItem) {
               BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, held, hit);
               return context.canPlace() && context.getClickedPos().equals(cell);
            } else {
               return false;
            }
         }

         @Override
         public boolean init(BlockState state, BlockPos pos, boolean sneaking) {
            return !(state.getBlock() instanceof RespawnAnchorBlock)
               ? !autismclient.util.BlockPlacementPlanner.wouldInteractWithoutSneak(state, pos, sneaking)
               : anchor != null && anchor.equals(pos) && AnchorAuraModule.isObsidian(held) && state.getValue(RespawnAnchorBlock.CHARGE) <= 0;
         }
      };
   }

   private autismclient.util.BlockPlacementPlanner.PlacementPlan planAnchorClick(
      BlockPos anchor, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget, autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      BlockState state = MC.level.getBlockState(anchor);
      if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      if (!(autismclient.util.ServerVerifiedBlockState.init(MC.level, anchor).getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.Query request = this.anchorUseQuery(anchor, range).withRayBudget(budget);
      Vec3 eye = MC.player.getEyePosition();
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = outcome != null ? outcome : new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      List<AnchorAuraModule.SupportAim> aims = new ArrayList<>();

      for (Direction face : autismclient.util.BlockPlacementPlanner.DEFAULT_FACE_ORDER) {
         for (AABB rect : autismclient.util.BlockPlacementPlanner.extractFaceRects(state, anchor, face, 3)) {
            autismclient.util.BlockPlacementPlanner.SupportCandidate option = new autismclient.util.BlockPlacementPlanner.SupportCandidate(
               anchor,
               anchor,
               face,
               1,
               state,
               rect,
               faceArea(rect, face),
               autismclient.util.BlockPlacementPlanner.edgeMarginRatio(rect, face, eye),
               false,
               new autismclient.util.BlockPlacementPlanner.SupportFace(anchor, face)
            );
            autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(option, request, refusal);
            if (aim != null) {
               aims.add(new AnchorAuraModule.SupportAim(option, aim));
            }
         }
      }

      aims.sort(Comparator.comparingDouble(candidatex -> candidatex.aim().turnDegrees()));
      int start = this.stickySelector.init(aims, AnchorAuraModule.SupportAim::support);

      for (int step = 0; step < aims.size(); step++) {
         AnchorAuraModule.SupportAim aim = aims.get((start + step) % aims.size());
         autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.verifyPlacement(
            aim.support(), aim.aim(), request, refusal
         );
         if (candidate != null) {
            this.stickySelector.init();
            return candidate;
         }

         if (refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            this.stickySelector.init(aim.support());
            return null;
         }
      }

      this.stickySelector.init();
      return null;
   }

   private static double faceArea(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, true);
      Axis second = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private BlockHitResult hitForRotation(AnchorAuraModule.AnchorPlan plan, RotationMath.Rotation wire) {
      LocalPlayer player = MC.player;
      autismclient.util.BlockPlacementPlanner.Query var10000;
      switch (plan.step()) {
         case SUPPORT:
         case PLACE:
            var10000 = this.placementQuery(plan.cell(), player.getMainHandItem(), plan.range());
            break;
         case SHIELD:
            var10000 = this.placementQuery(
               plan.cell(),
               player.getMainHandItem(),
               plan.range(),
               this.anchorHitValidator(this.activeAnchor, player.getMainHandItem())
            );
            break;
         case CHARGE:
         case DETONATE:
            var10000 = this.anchorUseQuery(plan.cell(), plan.range());
            break;
         default:
            throw new MatchException(null, null);
      }

      autismclient.util.BlockPlacementPlanner.Query gate = var10000;
      return autismclient.util.BlockPlacementPlanner.verifyWithRotation(plan.placement(), wire, player.getEyePosition(), plan.range(), gate);
   }

   private void queueStep(AnchorAuraModule.AnchorPlan plan, BlockHitResult hit, int tick) {
      if (this.stepStillValid(plan)
         && !al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, "anchor-aura")
         && autismclient.util.QueuedCombatAction.init(
            "anchor-aura",
            hit,
            InteractionHand.MAIN_HAND,
            () -> this.canAct()
               && this.stepStillValid(plan)
               && this.wireRotation() != null
               && this.hitForRotation(plan, this.wireRotation()) != null,
            () -> this.onStepSent(plan, AutismSessionState.getInstance().getClientTick())
         )
         && !PerTickPlacementLock.init("anchor-aura")) {
         autismclient.util.ClientInteractionOverride.clearAll();
      }
   }

   private void onStepSent(AnchorAuraModule.AnchorPlan plan, int tick) {
      if (this.plannedAnchor != null
         || this.activeAnchor != null
         || this.plannedSupport != null
         || this.activeSupport != null
         || this.plannedShield != null
         || this.activeShield != null) {
         this.previewAnchor = this.plannedAnchor != null ? this.plannedAnchor : this.activeAnchor;
         this.previewSupport = this.plannedSupport != null ? this.plannedSupport : this.activeSupport;
         this.previewShield = this.plannedShield != null ? this.plannedShield : this.activeShield;
         this.previewUntilTick = tick + 10;
      }

      this.markStep(plan.step());
      if (plan.step() == AnchorAuraModule.AnchorStep.SUPPORT) {
         this.adoptAnchor(plan.read(), plan.cell(), tick);
         this.supportAttempts++;
      }

      if (plan.step() == AnchorAuraModule.AnchorStep.PLACE) {
         if (this.activeAnchor == null || !this.activeAnchor.equals(plan.cell())) {
            this.adoptAnchor(plan.cell(), tick, plan.load(), plan.shieldObsidian());
         }

         this.placeAttempts++;
         this.sequenceSync.init(MC.level, ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler().currentSequence());
         this.ownPlacedAnchors.add(plan.cell().immutable());

         while (this.ownPlacedAnchors.size() > 64) {
            this.ownPlacedAnchors.remove(this.ownPlacedAnchors.iterator().next());
         }
      }

      if (plan.step() == AnchorAuraModule.AnchorStep.SHIELD) {
         this.shieldAttempts++;
      }

      if (plan.step() == AnchorAuraModule.AnchorStep.CHARGE) {
         this.chargeSent = true;
         this.chargeSentTick = tick;
      }

      if (plan.step() == AnchorAuraModule.AnchorStep.DETONATE) {
         this.detonateSent = true;
         this.detonateSentTick = tick;
      }

      this.aimSpeed.load(this.aimSubject(), tick, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
   }

   private Object aimSubject() {
      return this.activeAnchor != null ? this.activeAnchor : this.currentTarget;
   }

   private boolean stepStillValid(AnchorAuraModule.AnchorPlan plan) {
      if (!AutismLiteVariant.enabled()
         && (
            plan.step() == AnchorAuraModule.AnchorStep.SUPPORT
               || plan.step() == AnchorAuraModule.AnchorStep.PLACE
               || plan.step() == AnchorAuraModule.AnchorStep.SHIELD
         )
         && BacktrackModule.init_Z_AABB_2(new AABB(plan.cell()))) {
         return false;
      }

      BlockState state = MC.level.getBlockState(plan.cell());
      boolean anchor = state.getBlock() instanceof RespawnAnchorBlock
         && autismclient.util.ServerVerifiedBlockState.init(MC.level, plan.cell()).getBlock() instanceof RespawnAnchorBlock;
      boolean var10000;
      switch (plan.step()) {
         case SUPPORT:
            var10000 = !anchor
               && isObsidian(MC.player.getMainHandItem())
               && this.canPlaceBlockAt(Blocks.OBSIDIAN.defaultBlockState(), plan.cell(), this.motionEnvelope())
               && this.canPlaceAnchorAt(plan.read(), this.motionEnvelope())
               && this.planStillWorthIt(this.currentTarget, plan.read(), plan.cell(), null);
            break;
         case PLACE:
            var10000 = !anchor
               && isRespawnAnchor(MC.player.getMainHandItem())
               && this.canPlaceBlockAt(Blocks.RESPAWN_ANCHOR.defaultBlockState(), plan.cell(), this.motionEnvelope())
               && this.planStillWorthIt(this.currentTarget, plan.cell(), null, plan.load());
            break;
         case SHIELD:
            var10000 = state.canBeReplaced()
               && this.canPlaceBlockAt(shieldState(plan.shieldObsidian()), plan.cell(), this.motionEnvelope())
               && (plan.shieldObsidian() ? isObsidian(MC.player.getMainHandItem()) : isGlowstone(MC.player.getMainHandItem()));
            break;
         case CHARGE:
            var10000 = !this.sequenceSync.init(MC.level)
               && anchor
               && state.getValue(RespawnAnchorBlock.CHARGE) < 4
               && isGlowstone(MC.player.getMainHandItem())
               && this.shieldedDetonationSafe(plan.cell());
            break;
         case DETONATE:
            var10000 = !this.sequenceSync.init(MC.level)
               && anchor
               && !isGlowstone(MC.player.getMainHandItem())
               && this.canDetonate(plan.cell(), state);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean canDetonate(BlockPos cell, BlockState state) {
      if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
         return false;
      } else {
         int charge = state.getValue(RespawnAnchorBlock.CHARGE);
         BlockState received = autismclient.util.ServerVerifiedBlockState.init(MC.level, cell);
         if (!(received.getBlock() instanceof RespawnAnchorBlock)
            || !autismclient.util.WeaponDamageCalculator.isAnchorChargeConfirmed(charge, received.getValue(RespawnAnchorBlock.CHARGE), isGlowstone(MC.player.getOffhandItem()))) {
            return false;
         } else if (charge <= 0) {
            return false;
         } else if (isGlowstone(MC.player.getOffhandItem()) && charge < 4) {
            return false;
         } else if (isGlowstone(MC.player.getMainHandItem()) && this.detonateSlot() < 0) {
            return false;
         } else {
            return !this.anchorExplodes(cell) ? false : this.selfDamageSafe(cell) && this.friendsSafe(cell);
         }
      }
   }

   public static void onBlockUpdate(BlockPos cell, BlockState state) {
      if (al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() && cell.equals(aura.activeAnchor)) {
         aura.sequenceSync.init(MC.level, state.getBlock() instanceof RespawnAnchorBlock);
      }
   }

   public static void onBlockChangedAck(int sequence) {
      if (al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled()) {
         aura.sequenceSync.load(MC.level, sequence);
      }
   }

   private boolean anchorExplodes(BlockPos pos) {
      Boolean works = MC.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, pos);
      return works == null || !works;
   }

   private LivingEntity findTarget() {
      LocalPlayer player = MC.player;
      double range = this.decimal("target-range");
      AABB search = player.getBoundingBox().inflate(range);
      List<LivingEntity> found = MC.level
         .getEntitiesOfClass(
            LivingEntity.class,
            search,
            entity -> entity != player
               && entity.isAlive()
               && !entity.isSpectator()
               && this.matchesEntityFilter(entity)
               && !AntiBotTracker.shouldIgnoreEntity(entity)
               && !TeamsModule.init(entity, "killaura")
               && autismclient.util.ExplosionDamageCalculator.effectiveHealth(entity) > 0.0
               && entity.hurtTime <= this.integer("hurt-time")
         );
      if (found.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      LivingEntity best = null;
      double bestScore = 1.7976931348623157E308;
      String mode = this.choice("targeting");

      for (LivingEntity entity : found) {
         double distanceSq = distanceSqToBox(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq)) {
            double score = this.targetScore(mode, entity, distanceSq, eye);
            if (score < bestScore) {
               bestScore = score;
               best = entity;
            }
         }
      }

      LivingEntity incumbent = this.currentTarget;
      if (incumbent != null && incumbent != best && found.contains(incumbent)) {
         double grace = range + 0.5;
         double incumbentDistanceSq = distanceSqToBox(incumbent.getBoundingBox(), eye);
         if (incumbentDistanceSq <= grace * grace
            && (best == null || !autismclient.util.TargetSwitchHysteresis.init(mode, bestScore, this.targetScore(mode, incumbent, incumbentDistanceSq, eye)))) {
            return incumbent;
         }
      }

      return best;
   }

   private double targetScore(String mode, LivingEntity entity, double distanceSq, Vec3 eye) {
      double var10000;
      switch (mode) {
         case "HP":
            var10000 = autismclient.util.ExplosionDamageCalculator.effectiveHealth(entity);
            break;
         case "FOV":
            var10000 = RotationMath.vectorAngleDegrees(RotationMath.ofPlayer(MC.player), RotationMath.lookAt(entity.getBoundingBox().getCenter(), eye));
            break;
         default:
            var10000 = distanceSq;
      }

      return var10000;
   }

   private boolean matchesEntityFilter(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.parsedEntityFilter();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> parsedEntityFilter() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.entityFilterSource)) {
         return this.entityFilterIds;
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

      this.entityFilterSource = source;
      this.entityFilterIds = Set.copyOf(normalized);
      return this.entityFilterIds;
   }

   private List<BlockPos> candidateCells(LivingEntity enemy, double range) {
      boolean efficient = this.bool("efficient");
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = offsetsForRadius(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double damageFloor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = this.motionEnvelope();
      AABB lead = this.leadBox(enemy);
      List<AnchorAuraModule.AnchorCandidate> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.matchesAimMode(cursor) && this.anchorExplodes(cursor) && this.canPlaceBlockAt(anchor, cursor, nextBox)) {
            Vec3 centre = center(cursor);
            if (!this.bool("only-above") || !(centre.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, centre, 5.0F, this.simOptionsFor(cursor).withTargetBox(lead));
                  if (!(bound < damageFloor)) {
                     candidates.add(new AnchorAuraModule.AnchorCandidate(cursor.immutable(), centre, bound, distanceSq));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return List.of();
      }

      candidates.sort(Comparator.comparingDouble(AnchorAuraModule.AnchorCandidate::damageBound).reversed());
      List<AnchorAuraModule.ScoredCell> accepted = new ArrayList<>();
      double bestValue = -1.0;
      int evaluated = 0;

      for (AnchorAuraModule.AnchorCandidate candidate : candidates) {
         if (accepted.size() >= 4 && candidate.damageBound() <= bestValue || evaluated >= 32) {
            break;
         }

         evaluated++;
         autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankBlast(
            enemy, candidate.center(), this.simOptionsFor(candidate.cell()), lead
         );
         if (this.rankAcceptable(rank)) {
            accepted.add(new AnchorAuraModule.ScoredCell(candidate.cell(), rank, candidate.load()));
            bestValue = Math.max(bestValue, tradeValue(rank, efficient));
         }
      }

      if (accepted.isEmpty()) {
         return List.of();
      }

      accepted.sort(
         (first, second) -> {
            int byValue = Double.compare(
               tradeValue(second.rank(), efficient), tradeValue(first.rank(), efficient)
            );
            if (byValue != 0) {
               return byValue;
            }

            int byPreference = compareRanks(first.rank(), second.rank());
            return byPreference != 0 ? byPreference : Double.compare(second.distanceSq(), first.distanceSq());
         }
      );
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (AnchorAuraModule.ScoredCell scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.cell());
      }

      return result;
   }

   private static double tradeValue(autismclient.util.ExplosionDamageCalculator.DamagePair rank, boolean efficient) {
      return efficient ? rank.load() - 2.5 * rank.selfDamage() : rank.load();
   }

   private AnchorAuraModule.AnchorPlan planSupportedAnchor(LivingEntity enemy, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      if (this.findHotbarSlot(AnchorAuraModule::isObsidian) >= 0 && this.findHotbarSlot(AnchorAuraModule::isRespawnAnchor) >= 0) {
         LocalPlayer player = MC.player;
         Vec3 eye = player.getEyePosition();
         BlockPos origin = player.blockPosition();
         int[] offsets = offsetsForRadius(Mth.ceil(range));
         BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
         BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
         double floor = this.decimal("min-target-damage");
         double rangeSq = range * range;
         AABB nextBox = this.motionEnvelope();
         AABB lead = this.leadBox(enemy);
         List<AnchorAuraModule.AnchorSupportPair> accepted = new ArrayList<>();
         MutableBlockPos cursor = new MutableBlockPos();
         int evaluated = 0;

         for (int i = 0; i + 2 < offsets.length; i += 3) {
            cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
            BlockPos anchorCell = cursor.above();
            if (this.matchesAimMode(anchorCell)
               && !this.bannedCells.containsKey(cursor)
               && !this.bannedCells.containsKey(anchorCell)
               && this.anchorExplodes(anchorCell)
               && this.canPlaceAnchorAt(anchorCell, nextBox)
               && this.canPlaceBlockAt(obsidian, cursor, nextBox)) {
               Vec3 centre = center(anchorCell);
               if (!(this.bool("only-above") && centre.y < enemy.getY())) {
                  double distanceSq = eye.distanceToSqr(centre);
                  if (!(distanceSq > rangeSq)) {
                     autismclient.util.ExplosionDamageCalculator.SimOptions supported = this.simOptionsFor(anchorCell).withPlacedObsidian(cursor.immutable());
                     if (!(autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, centre, 5.0F, supported.withTargetBox(lead)) < floor)) {
                        if (++evaluated > 32) {
                           break;
                        }

                        autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankBlast(enemy, centre, supported, lead);
                        if (this.rankAcceptable(rank)) {
                           accepted.add(new AnchorAuraModule.AnchorSupportPair(anchorCell.immutable(), cursor.immutable(), rank, distanceSq));
                        }
                     }
                  }
               }
            }
         }

         accepted.sort((first, second) -> {
            int preference = compareRanks(first.rank(), second.rank());
            return preference != 0 ? preference : Double.compare(second.distanceSq(), first.distanceSq());
         });
         int limit = Math.min(4, accepted.size());

         for (int i = 0; i < limit; i++) {
            AnchorAuraModule.AnchorSupportPair pair = accepted.get(i);
            AnchorAuraModule.AnchorPlan plan = this.planSupport(pair.cell(), pair.load(), range, budget);
            if (plan != null) {
               return plan;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private List<BlockPos> forcedCandidateCells(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = offsetsForRadius(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double rangeSq = range * range;
      AABB nextBox = this.motionEnvelope();
      AABB enemyRing = enemy.getBoundingBox().inflate(1.0);
      int feetY = Mth.floor(enemy.getY());
      List<AnchorAuraModule.ScoredCell> accepted = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.matchesAimMode(cursor) && this.anchorExplodes(cursor) && this.canPlaceBlockAt(anchor, cursor, nextBox)) {
            Vec3 centre = center(cursor);
            double distanceSq = eye.distanceToSqr(centre);
            if (!(distanceSq > rangeSq) && new AABB(cursor).intersects(enemyRing)) {
               if (evaluated >= 32) {
                  break;
               }

               evaluated++;
               autismclient.util.ExplosionDamageCalculator.DamagePair rank = autismclient.util.TargetLeadDamageEstimator.init(
                  enemy, centre, 5.0F, this.simOptionsFor(cursor), autismclient.util.TargetLeadDamageEstimator.init(enemy)
               );
               if (this.rankAcceptable(rank) && this.friendsSafe(cursor)) {
                  accepted.add(new AnchorAuraModule.ScoredCell(cursor.immutable(), rank, distanceSq));
               }
            }
         }
      }

      if (accepted.isEmpty()) {
         return List.of();
      }

      accepted.sort((first, second) -> {
         boolean firstLow = first.cell().getY() <= feetY;
         boolean secondLow = second.cell().getY() <= feetY;
         if (firstLow != secondLow) {
            return firstLow ? -1 : 1;
         }

         int byPreference = compareRanks(first.rank(), second.rank());
         return byPreference != 0 ? byPreference : Double.compare(second.distanceSq(), first.distanceSq());
      });
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (AnchorAuraModule.ScoredCell scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.cell());
      }

      return result;
   }

   private AnchorAuraModule.AnchorPlan planShieldedAnchor(LivingEntity enemy, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      if (!this.hasShieldMaterial(false) && !this.hasShieldMaterial(true)) {
         return null;
      }

      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = offsetsForRadius(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double damageFloor = this.decimal("min-target-damage");
      double selfCeiling = this.decimal("max-self-damage");
      double rangeSq = range * range;
      AABB nextBox = this.motionEnvelope();
      List<AnchorAuraModule.ScoredCell> needsShield = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.matchesAimMode(cursor) && this.anchorExplodes(cursor) && this.canPlaceBlockAt(anchor, cursor, nextBox)) {
            Vec3 centre = center(cursor);
            if (!this.bool("only-above") || !(centre.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq) && !(autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, centre, 5.0F, this.simOptionsFor(cursor)) < damageFloor)) {
                  if (evaluated >= 32) {
                     break;
                  }

                  evaluated++;
                  autismclient.util.ExplosionDamageCalculator.DamagePair rank = autismclient.util.TargetLeadDamageEstimator.init(
                     enemy, centre, 5.0F, this.simOptionsFor(cursor), autismclient.util.TargetLeadDamageEstimator.init(enemy)
                  );
                  if (!(rank.load() < damageFloor) && !(rank.selfDamage() <= selfCeiling)) {
                     needsShield.add(new AnchorAuraModule.ScoredCell(cursor.immutable(), rank, distanceSq));
                  }
               }
            }
         }
      }

      if (needsShield.isEmpty()) {
         return null;
      }

      needsShield.sort((first, second) -> {
         int byPreference = compareRanks(first.rank(), second.rank());
         return byPreference != 0 ? byPreference : Double.compare(second.distanceSq(), first.distanceSq());
      });
      int tried = 0;

      for (AnchorAuraModule.ScoredCell scored : needsShield) {
         if (tried >= 4) {
            break;
         }

         tried++;
         BlockPos shield = this.findShieldCell(enemy, scored.cell(), damageFloor, selfCeiling);
         if (shield != null) {
            int material = this.coverMaterialFor(shield, scored.cell());
            if (material >= 0) {
               boolean obsidian = material == 1;
               return this.planPlaceAnchorWithShield(scored.cell(), shield, obsidian, range, budget, null);
            }
         }
      }

      return null;
   }

   private BlockPos findShieldCell(LivingEntity enemy, BlockPos anchorCell, double damageFloor, double selfCeiling) {
      Vec3 centre = center(anchorCell);
      autismclient.util.ExplosionDamageCalculator.DamagePair before = autismclient.util.TargetLeadDamageEstimator.init(
         enemy, centre, 5.0F, this.simOptionsFor(anchorCell), autismclient.util.TargetLeadDamageEstimator.init(enemy)
      );
      if (before.selfDamage() < 2.0) {
         return null;
      }

      boolean obsidianAvailable = this.hasShieldMaterial(true);
      boolean glowstoneAvailable = this.hasShieldMaterial(false);
      if (!obsidianAvailable && !glowstoneAvailable) {
         return null;
      }

      BlockPos best = null;
      double bestValue = -1.0 / 0.0;

      for (BlockPos shieldCell : this.shieldCandidates(anchorCell)) {
         if (obsidianAvailable && this.durableCoverPlaceable(shieldCell, anchorCell) || glowstoneAvailable && this.hasAnySupport(shieldCell)) {
            autismclient.util.ExplosionDamageCalculator.SimOptions verify = this.simOptionsFor(anchorCell).withPlacedObsidian(shieldCell);
            autismclient.util.ExplosionDamageCalculator.DamagePair rank = autismclient.util.TargetLeadDamageEstimator.init(
               enemy, centre, 5.0F, verify, autismclient.util.TargetLeadDamageEstimator.init(enemy)
            );
            if (!autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(rank.selfDamage())
               && autismclient.util.BlastShieldPosFinder.init(before.load(), before.selfDamage(), rank.load(), rank.selfDamage(), damageFloor, selfCeiling, this.bool("efficient"))
               && (!this.bool("protect-friends") || autismclient.util.WeaponDamageCalculator.init(centre, 5.0F, verify, this.decimal("max-friend-damage")))) {
               double value = rank.load() - rank.selfDamage();
               if (value > bestValue) {
                  bestValue = value;
                  best = shieldCell;
               }
            }
         }
      }

      return best;
   }

   private List<BlockPos> shieldCandidates(BlockPos anchorCell) {
      LocalPlayer player = MC.player;
      AABB box = player.getBoundingBox();
      AABB nextBox = this.motionEnvelope();
      Vec3 playerCentre = box.getCenter();
      Vec3 toAnchor = center(anchorCell).subtract(playerCentre).normalize();
      List<BlockPos> cells = new ArrayList<>();

      for (BlockPos cell : autismclient.util.BlastShieldPosFinder.init(box, nextBox, center(anchorCell))) {
         if (!cell.equals(anchorCell) && this.canPlaceBlockAt(Blocks.GLOWSTONE.defaultBlockState(), cell, nextBox)) {
            cells.add(cell);
         }
      }

      cells.sort(
         (first, second) -> {
            boolean firstGround = this.hasGroundSupport(first);
            boolean secondGround = this.hasGroundSupport(second);
            if (firstGround != secondGround) {
               return firstGround ? -1 : 1;
            } else {
               return Double.compare(
                  center(second).subtract(playerCentre).normalize().dot(toAnchor),
                  center(first).subtract(playerCentre).normalize().dot(toAnchor)
               );
            }
         }
      );
      return cells.size() <= 12 ? cells : new ArrayList<>(cells.subList(0, 12));
   }

   private boolean hasGroundSupport(BlockPos cell) {
      BlockPos below = cell.below();
      return autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(below), below, false);
   }

   private boolean hasAnySupport(BlockPos cell) {
      for (Direction face : Direction.values()) {
         BlockPos support = cell.relative(face);
         if (autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(support), support, false)) {
            return true;
         }
      }

      return false;
   }

   private boolean durableCoverPlaceable(BlockPos cell, BlockPos anchorCell) {
      return this.hasAnySupport(cell) || cell.distManhattan(anchorCell) == 1;
   }

   private int coverMaterialFor(BlockPos cell, BlockPos anchor) {
      boolean durable = this.hasShieldMaterial(true) && this.durableCoverPlaceable(cell, anchor);
      boolean fuel = this.hasShieldMaterial(false) && this.hasAnySupport(cell);
      if (!durable
         || fuel && !"Durable".equals(this.choice("cover-material")) && (!"Adaptive".equals(this.choice("cover-material")) || !this.load(cell, anchor))) {
         return fuel ? 0 : (durable ? 1 : -1);
      } else {
         return 1;
      }
   }

   private boolean load(BlockPos cover, BlockPos anchor) {
      Vec3 eye = MC.player.getEyePosition();
      double range = MC.player.blockInteractionRange();
      AABB box = new AABB(cover);

      for (Direction side : Direction.values()) {
         BlockPos support = anchor.relative(side);
         if (!support.equals(cover) && autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(support), support, false)) {
            Direction face = side.getOpposite();
            Vec3 point = Vec3.atCenterOf(support).add(face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499);
            if (!(eye.distanceToSqr(point) > range * range) && !box.clip(eye, point).isPresent()) {
               BlockHitResult hit = MC.level.clip(new ClipContext(eye, point, Block.OUTLINE, Fluid.NONE, MC.player));
               if (hit.getBlockPos().equals(support) && hit.getDirection() == face && hit.getType() == Type.BLOCK) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private AABB motionEnvelope() {
      return autismclient.util.PlayerMotionEnvelope.init(MC.player);
   }

   private boolean canPlaceAnchorAt(BlockPos cell, AABB nextBox) {
      if (CrystalAuraModule.init_Z_BlockPos_6(cell) || CrystalAuraModule.load_Z_BlockPos_3(cell)) {
         return false;
      } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return false;
      } else if (!MC.level.isUnobstructed(Blocks.RESPAWN_ANCHOR.defaultBlockState(), cell, CollisionContext.placementContext(MC.player))) {
         return false;
      } else {
         return !AutismLiteVariant.enabled() && BacktrackModule.init_Z_AABB_2(new AABB(cell)) ? false : !nextBox.intersects(new AABB(cell));
      }
   }

   private boolean canPlaceBlockAt(BlockState anchor, BlockPos cell, AABB nextBox) {
      if (CrystalAuraModule.init_Z_BlockPos_6(cell)) {
         return false;
      } else if (CrystalAuraModule.load_Z_BlockPos_3(cell)) {
         return false;
      } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return false;
      } else if (!anchor.canSurvive(MC.level, cell)) {
         return false;
      } else {
         return !MC.level.isUnobstructed(anchor, cell, CollisionContext.placementContext(MC.player)) ? false : !nextBox.intersects(new AABB(cell));
      }
   }

   private BlockPos findAdoptableAnchor(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = offsetsForRadius(Mth.ceil(range));
      double rangeSq = range * range;
      BlockPos best = null;
      autismclient.util.ExplosionDamageCalculator.DamagePair bestRank = null;
      double bestDistanceSq = 0.0;
      AABB lead = this.leadBox(enemy);
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.matchesAimMode(cursor) && (!this.bool("own-only") || this.ownPlacedAnchors.contains(cursor))) {
            BlockState state = MC.level.getBlockState(cursor);
            if (state.getBlock() instanceof RespawnAnchorBlock && this.anchorExplodes(cursor)) {
               Vec3 centre = center(cursor);
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankBlast(
                     enemy, centre, this.simOptionsFor(cursor), lead
                  );
                  if (this.rankAcceptable(rank)) {
                     int preference = bestRank == null ? -1 : compareRanks(rank, bestRank);
                     if (preference < 0 || preference == 0 && distanceSq > bestDistanceSq) {
                        best = cursor.immutable();
                        bestRank = rank;
                        bestDistanceSq = distanceSq;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static boolean isRespawnAnchor(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.RESPAWN_ANCHOR;
   }

   private static boolean isGlowstone(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.GLOWSTONE;
   }

   private static boolean isObsidian(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.OBSIDIAN;
   }

   private static BlockState shieldState(boolean obsidian) {
      return (obsidian ? Blocks.OBSIDIAN : Blocks.GLOWSTONE).defaultBlockState();
   }

   private boolean hasShieldMaterial(boolean obsidian) {
      return !obsidian
         ? this.glowstoneCount() >= 2
         : this.findHotbarSlot(AnchorAuraModule::isGlowstone) >= 0 && this.findHotbarSlot(AnchorAuraModule::isObsidian) >= 0;
   }

   private int glowstoneCount() {
      LocalPlayer player = MC.player;
      int count = 0;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (isGlowstone(stack)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   private int detonateSlot() {
      LocalPlayer player = MC.player;
      String configured = this.choice("detonate-slot");
      if ("Auto".equals(configured)) {
         int selected = player.getInventory().getSelectedSlot();
         if (!isGlowstone(player.getMainHandItem()) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(selected, "anchor-aura")) {
            return selected;
         }

         int anchor = this.findHotbarSlot(AnchorAuraModule::isRespawnAnchor);
         return anchor >= 0 ? anchor : this.findHotbarSlot(stack -> !isGlowstone(stack));
      } else {
         int slot = Integer.parseInt(configured) - 1;
         return !isGlowstone(player.getInventory().getItem(slot)) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "anchor-aura") ? slot : -1;
      }
   }

   private boolean ensureSlotSelected(int slot, boolean finishingWork) {
      if (slot < 0) {
         return true;
      }

      LocalPlayer player = MC.player;
      if (!autismclient.util.InventorySlotArbiter.claimSlot("anchor-aura", slot)) {
         return false;
      }

      int held = player.getInventory().getSelectedSlot();
      if (held == slot) {
         if (System.nanoTime() < this.switchSettledAtNanos) {
            return false;
         }

         this.switchBackDelayTicks = this.integer("switch-back-delay");
         return true;
      } else {
         if (this.switchToSlot(slot, finishingWork)) {
            if (this.switchBackSlot < 0) {
               this.switchBackSlot = held;
            }

            this.switchedToSlot = slot;
         }

         this.switchBackDelayTicks = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && System.nanoTime() >= this.switchSettledAtNanos;
      }
   }

   private boolean switchToSlot(int slot, boolean finishingWork) {
      if (!BedDefenderModule.init_Z_10() && !SurroundModule.init_Z_27()) {
         if (finishingWork || !CrystalAuraModule.flush() || !CrystalAuraModule.init_Z_51() && !CrystalAuraModule.sync_Z_11()) {
            int tick = AutismSessionState.getInstance().getClientTick();
            if (tick == this.lastSlotSwitchTick) {
               return false;
            }

            if (!autismclient.util.InventorySlotArbiter.beginActionGroup("anchor-aura")) {
               return false;
            }

            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
               this.switchSettledAtNanos = System.nanoTime() + this.integer("switch-delay") * 1000000L;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup("anchor-aura");
            }

            this.lastSlotSwitchTick = tick;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int findHotbarSlot(Predicate<ItemStack> match) {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (match.test(player.getInventory().getItem(slot)) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "anchor-aura")) {
            int steps = Math.abs(slot - selected);
            steps = Math.min(steps, 9 - steps);
            if (steps < bestSteps) {
               bestSteps = steps;
               best = slot;
            }
         }
      }

      return best;
   }

   private void tickSwitchBack() {
      if (this.switchBackSlot >= 0 && MC.gui.screen() == null && MC.gui.overlay() == null) {
         if (this.switchedToSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.switchedToSlot) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!CrystalAuraModule.init_Z_I_10(selected) && !KillAuraModule.load_Z_I_3(selected)) {
               this.switchBackSlot = -1;
               this.switchedToSlot = -1;
               this.switchBackDelayTicks = 0;
            }
         } else if (this.switchBackDelayTicks > 0) {
            this.switchBackDelayTicks--;
         } else if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.switchBackSlot, "anchor-aura") && this.switchToSlot(this.switchBackSlot, false)) {
            this.switchBackSlot = -1;
            this.switchedToSlot = -1;
         }
      }
   }

   private void refreshSimOptions() {
      this.simOptions = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withIgnoreTerrain(this.bool("terrain")).withEstimateProtection(true);
      this.simOptionsByCell.clear();
   }

   private autismclient.util.ExplosionDamageCalculator.SimOptions simOptionsFor(BlockPos cell) {
      BlockPos key = cell.immutable();
      autismclient.util.ExplosionDamageCalculator.SimOptions cached = this.simOptionsByCell.get(key);
      if (cached != null) {
         return cached;
      }

      autismclient.util.ExplosionDamageCalculator.SimOptions options = this.simOptions.withExcludedBlocks(List.of(key)).withDamageSource(autismclient.util.ExplosionDamageCalculator.bedAnchorDamageSource(MC.level, center(key)));
      this.simOptionsByCell.put(key, options);
      return options;
   }

   private AABB leadBox(LivingEntity enemy) {
      int horizon = Math.min(12, 6 + (int)Math.ceil(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs() / 50.0));
      return this.leadEstimator.predictedBox(enemy, AutismSessionState.getInstance().getClientTick(), horizon, 2.0);
   }

   private autismclient.util.ExplosionDamageCalculator.DamagePair rankBlast(
      LivingEntity enemy, Vec3 centre, autismclient.util.ExplosionDamageCalculator.SimOptions options, AABB lead
   ) {
      if (this.bool("protect-friends") && !autismclient.util.WeaponDamageCalculator.init(centre, 5.0F, options, this.decimal("max-friend-damage"))) {
         return new autismclient.util.ExplosionDamageCalculator.DamagePair(0.0, 1.0 / 0.0);
      }

      double target = autismclient.util.ExplosionDamageCalculator.calculateDamageCached(enemy, centre, 5.0F, options.withTargetBox(lead));
      LocalPlayer player = MC.player;
      double self = enemy == player ? target : autismclient.util.ExplosionDamageCalculator.calculateSelfDamageCached(centre, 5.0F, options);
      return new autismclient.util.ExplosionDamageCalculator.DamagePair(target, self);
   }

   private boolean rankAcceptable(autismclient.util.ExplosionDamageCalculator.DamagePair rank) {
      return autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(rank.selfDamage())
         ? false
         : autismclient.util.WeaponDamageCalculator.init(rank.load(), rank.selfDamage(), this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"));
   }

   private boolean selfDamageSafe(BlockPos cell) {
      return !autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(this.selfDamage(cell));
   }

   private boolean matchesAimMode(BlockPos cell) {
      return !"Aimed".equals(this.choice("aim-mode")) || cell.equals(this.aimedCell);
   }

   private boolean friendsSafe(BlockPos cell) {
      return !this.bool("protect-friends")
         || autismclient.util.WeaponDamageCalculator.init(center(cell), 5.0F, this.selfSimOptions(cell), this.decimal("max-friend-damage"));
   }

   private double selfDamage(BlockPos cell) {
      return autismclient.util.ExplosionDamageCalculator.calculateSelfDamageCached(center(cell), 5.0F, this.selfSimOptions(cell));
   }

   private autismclient.util.ExplosionDamageCalculator.SimOptions selfSimOptions(BlockPos cell) {
      autismclient.util.ExplosionDamageCalculator.SimOptions options = this.simOptionsFor(cell);
      if (this.activeShield == null || !cell.equals(this.activeAnchor)) {
         return options;
      } else if (this.shieldInPlace(cell)) {
         return options.withPlacedObsidian(this.activeShield);
      } else {
         return MC.level.getBlockState(this.activeShield) != autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield)
            ? options.withExcludedBlocks(List.of(cell, this.activeShield))
            : options;
      }
   }

   private boolean shieldInPlace(BlockPos cell) {
      if (this.activeShield != null && cell.equals(this.activeAnchor)) {
         net.minecraft.world.level.block.Block material = shieldState(this.activeShieldObsidian).getBlock();
         return MC.level.getBlockState(this.activeShield).is(material) && autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield).is(material);
      } else {
         return false;
      }
   }

   private boolean shieldedDetonationSafe(BlockPos cell) {
      if (this.activeShield == null || !cell.equals(this.activeAnchor)) {
         return true;
      }

      if (!this.shieldInPlace(cell)) {
         return false;
      }

      double self = this.selfDamage(cell);
      return self <= this.decimal("max-self-damage") && !autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(self) && this.friendsSafe(cell);
   }

   private static int compareRanks(autismclient.util.ExplosionDamageCalculator.DamagePair first, autismclient.util.ExplosionDamageCalculator.DamagePair second) {
      int byBucket = Double.compare(damageBucket(second.load()), damageBucket(first.load()));
      return byBucket != 0 ? byBucket : Double.compare(first.selfDamage(), second.selfDamage());
   }

   private static double damageBucket(double targetDamage) {
      return Math.floor(targetDamage * 2.0);
   }

   private static Vec3 center(BlockPos pos) {
      return Vec3.atCenterOf(pos);
   }

   private static double distanceSqToBox(AABB box, Vec3 point) {
      double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
      double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
      double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] offsetsForRadius(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == cachedOffsetsRadius) {
         return cachedOffsets;
      }

      int limit = clamped * clamped;
      List<int[]> cells = new ArrayList<>();

      for (int x = -clamped; x <= clamped; x++) {
         for (int y = -clamped; y <= clamped; y++) {
            for (int z = -clamped; z <= clamped; z++) {
               int distance = x * x + y * y + z * z;
               if (distance <= limit) {
                  cells.add(new int[]{x, y, z, distance});
               }
            }
         }
      }

      cells.sort(Comparator.comparingInt(cellx -> cellx[3]));
      int[] flat = new int[cells.size() * 3];

      for (int i = 0; i < cells.size(); i++) {
         int[] cell = cells.get(i);
         flat[i * 3] = cell[0];
         flat[i * 3 + 1] = cell[1];
         flat[i * 3 + 2] = cell[2];
      }

      cachedOffsets = flat;
      cachedOffsetsRadius = clamped;
      return flat;
   }

   private record ScoredCell(BlockPos cell, autismclient.util.ExplosionDamageCalculator.DamagePair rank, double distanceSq) {
   }

   private record AnchorPlan(
      BlockPos cell,
      AnchorAuraModule.AnchorStep step,
      int slot,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      double range,
      BlockPos load,
      boolean shieldObsidian,
      BlockPos read
   ) {
   }

   private enum AnchorStep {
      SUPPORT,
      PLACE,
      SHIELD,
      CHARGE,
      DETONATE;
   }

   private record AnchorHitValidator(BlockPos init) implements autismclient.util.BlockPlacementPlanner.HitValidator {
      @Override
      public boolean lands(BlockHitResult hit, BlockPos cell) {
         return hit != null && this.init.equals(hit.getBlockPos());
      }

      @Override
      public boolean init(BlockState state, BlockPos pos, boolean sneaking) {
         return this.init.equals(pos) && state.getBlock() instanceof RespawnAnchorBlock;
      }
   }

   private record SupportAim(autismclient.util.BlockPlacementPlanner.SupportCandidate support, autismclient.util.BlockPlacementPlanner.AimSample aim) {
   }

   private record AnchorCandidate(BlockPos cell, Vec3 center, double damageBound, double load) {
   }

   private record AnchorSupportPair(BlockPos cell, BlockPos load, autismclient.util.ExplosionDamageCalculator.DamagePair rank, double distanceSq) {
   }
}
